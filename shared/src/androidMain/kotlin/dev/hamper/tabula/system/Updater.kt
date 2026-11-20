package dev.hamper.tabula.system

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.hamper.tabula.Env
import dev.hamper.tabula.store
import dev.hamper.tabula.system.UpdatePhase as Phase
import dev.hamper.tabula.state.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.format
import dev.hamper.tabula.compat.nanoTime

/**
 * Updates from the app's server: the check asks GET /api/fitest/latest?since=<this versionCode> (for the notes of every newer
 * version) and sends nothing else (no install id, a plain User-Agent); a newer build is offered on the home screens only. "Aktualizovať" downloads it here with progress,
 * checks its SHA-256 and size, and hands it to the system installer, which asks the student. Never installs by itself.
 * The same signing key keeps the progress (DataStore survives an update).
 */
actual object Updater {

    actual var phase by mutableStateOf<Phase>(Phase.Idle)
    /** The sheet asked for from Settings ("Skontrolovať teraz" found a newer build). */
    actual var asked by mutableStateOf(false)
    /** The sheet is on screen: a finished download opens the installer only then, never over a lesson someone opened meanwhile. */
    actual var shown = false

    val ActionStatus = "${Env.APPLICATION_ID}.UPDATE_STATUS"
    private const val TAG = "FITestUpdate"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var staging: Job? = null

    actual fun current(): Int = Env.VERSION_CODE

    private fun dir(ctx: Context) = File(ctx.filesDir, "update")
    /** The verified download of exactly this release (version and checksum), so a newer or re-published build never reuses it. */
    private fun fileFor(ctx: Context, r: Release) = File(dir(ctx), "FITest-${r.versionCode}-${r.sha256.take(12)}.apk")

    /** The phase's file, when it is the download of [r]; anything else starts over. */
    private fun fileOf(p: Phase): File? = when (p) { is Phase.Ready -> p.file; is Phase.NeedsPermission -> p.file; is Phase.Installing -> p.file; else -> null }

    /** On start: forget builds already installed and other downloads; ask the server when due and on a network. */
    fun onStart(ctx: Context, store: Store) {
        val r = store.release
        if (r != null && r.versionCode <= current()) { store.release = null; store.save() }
        val keep = store.release?.let { fileFor(ctx, it) }
        dir(ctx).listFiles()?.forEach { if (it != keep && phase !is Phase.Downloading) it.delete() }
        if (fileOf(phase)?.let { it != keep } == true) phase = Phase.Idle
        if (phase == Phase.Idle) keep?.takeIf { it.isFile }?.let { phase = Phase.Ready(it) }
        if (!UpdateCore.due(store.autoUpdate, store.updateCheckedAt, currentTimeMillis(), failures, failedAt, coldStart = !answered)) return
        if (online(ctx)) check(ctx, store, manual = false) else whenOnline(ctx, store)
    }

    /** Failed automatic checks in a row and when the last one failed (kept while the app runs; a new start asks at once). */
    private var failures = 0
    private var failedAt = 0L
    /** This run of the app has had an answer: until then every start and resume asks (a cold start always does). */
    private var answered = false
    private var waiting: ConnectivityManager.NetworkCallback? = null

    /** No validated network now: ask as soon as there is one (once; the next start or resume tries again anyway). */
    private fun whenOnline(ctx: Context, store: Store) {
        if (waiting != null) return
        val app = ctx.applicationContext
        val cm = app.getSystemService(ConnectivityManager::class.java) ?: return
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) return
                runCatching { cm.unregisterNetworkCallback(this) }
                Handler(Looper.getMainLooper()).post {
                    waiting = null
                    if (UpdateCore.due(store.autoUpdate, store.updateCheckedAt, currentTimeMillis(), failures, failedAt, coldStart = !answered)) check(app, store, manual = false)
                }
            }
        }
        if (runCatching { cm.registerDefaultNetworkCallback(cb) }.isSuccess) waiting = cb
    }

    /**
     * A failed automatic check is asked again after its pause (1, 5, 15 minutes) while the app runs, at most three
     * times: a student who stays in the app after a slow first answer still gets the offer, without a check every
     * quarter of an hour forever.
     */
    private fun retryLater(ctx: Context, store: Store) {
        if (failures > UpdateCore.RetryAfter.size) return
        val app = ctx.applicationContext
        Handler(Looper.getMainLooper()).postDelayed({
            if (UpdateCore.due(store.autoUpdate, store.updateCheckedAt, currentTimeMillis(), failures, failedAt, coldStart = !answered)) check(app, store, manual = false)
        }, UpdateCore.retryAfter(failures))
    }

    private fun online(ctx: Context): Boolean {
        val cm = ctx.getSystemService(ConnectivityManager::class.java) ?: return false
        return cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }

    /** Asks the server. [manual]: from Settings, so the answer is said there and a newer build opens the sheet. */
    fun check(ctx: Context, store: Store, manual: Boolean) {
        if (job?.isActive == true) return
        if (manual) phase = Phase.Checking
        job = scope.launch {
            val r = runCatching { UpdateCore.parse(get(UpdateCore.checkUrl(Env.UPDATE_URL, current()))) ?: error("odpoveď bez verzie") }
                .onFailure { Log.i(TAG, "check failed: ${it.message}") }.getOrNull()
            withContext(Dispatchers.Main) {
                // only an answer counts for the minute (UpdateCore.CheckEvery); a failure is asked again after 1, 5, 15 minutes (UpdateCore.retryAfter)
                if (r == null) { failures++; failedAt = currentTimeMillis(); if (manual) phase = Phase.Failed(if (online(ctx)) "Server teraz neodpovedá. Skús to o chvíľu." else "Si bez internetu. Skús to, keď budeš pripojený.") else retryLater(ctx, store); return@withContext }
                failures = 0
                answered = true
                store.updateCheckedAt = currentTimeMillis()
                store.release = r.takeIf { it.versionCode > current() }
                store.save()
                // a download of another build (an older offer) is not this one
                val keep = store.release?.let { fileFor(ctx, it) }
                if (fileOf(phase)?.let { it != keep } == true) phase = Phase.Idle
                if (manual) {
                    if (r.versionCode > current()) { phase = fileFor(ctx, r).takeIf { it.isFile }?.let { Phase.Ready(it) } ?: Phase.Idle; asked = true }
                    else phase = Phase.UpToDate
                } else if (phase is Phase.Failed || phase == Phase.UpToDate) phase = Phase.Idle
            }
        }
    }

    /** "Neskôr": that version stays hidden for three days (a newer one shows at once). */
    actual fun later(store: Store) {
        store.release?.let { store.snoozedCode = it.versionCode; store.snoozedUntil = currentTimeMillis() + UpdateCore.SnoozeFor }
        store.resumeInstallUntil = 0L // later means later, also after granting the permission
        store.save()
        asked = false
        if (phase is Phase.Failed || phase == Phase.UpToDate) phase = Phase.Idle
    }

    actual fun cancel() { job?.cancel(); phase = Phase.Idle }

    /** "Aktualizovať": download (or reuse a verified download), then the installer. */
    fun update(activity: Activity, r: Release) {
        val ctx = activity.applicationContext
        if (phase is Phase.Downloading || staging?.isActive == true) return
        // only a checksum-verified download ever gets this name: reuse it (also after a failed install)
        fileFor(ctx, r).takeIf { it.isFile }?.let { return install(activity, it) }
        phase = Phase.Downloading(0, r.size)
        job = scope.launch {
            val file = fileFor(ctx, r)
            val ok = runCatching { download(UpdateCore.resolve(Env.UPDATE_URL, r.url), file, r) }
                .onFailure { Log.i(TAG, "download failed: ${it.message}") }
            withContext(Dispatchers.Main) {
                ok.fold(
                    onSuccess = {
                        // the offer may have moved on while this downloaded (a newer build): only the current one's file counts
                        val current = dev.hamper.tabula.Tabula.store(ctx).release?.let { fileFor(ctx, it) }
                        if (file != current) { file.delete(); phase = Phase.Idle }
                        else {
                            phase = Phase.Ready(file)
                            // on screen and in front: from the background Android would not let the confirmation open
                            val front = (activity as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) == true
                            if (shown && front) install(activity, file)
                        }
                    },
                    onFailure = { e ->
                        if (e is kotlinx.coroutines.CancellationException) return@fold
                        phase = Phase.Failed(if (e is Mismatch) "Stiahnutý súbor nesedí s tým, čo hlási server. Skús to znova." else if (!online(ctx)) "Si bez internetu, sťahovanie sa prerušilo. Skús to, keď budeš pripojený." else "Sťahovanie sa prerušilo. Skús to znova.")
                    },
                )
            }
        }
    }

    private class Mismatch : Exception("checksum")

    private suspend fun download(url: String, target: File, r: Release) {
        target.parentFile?.mkdirs()
        target.parentFile?.listFiles()?.forEach { if (it != target) it.delete() } // one build at a time
        val part = File(target.path + ".part" + nanoTime()) // a cancelled download still stuck in read() can't delete the next one's file
        val digest = MessageDigest.getInstance("SHA-256")
        val c = open(url)
        try {
            if (c.responseCode != 200) error("HTTP ${c.responseCode}")
            var done = 0L
            var shown = 0L
            c.inputStream.use { input ->
                part.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        kotlin.coroutines.coroutineContext.ensureActive()
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n); digest.update(buf, 0, n); done += n
                        if (done > r.size) throw Mismatch()
                        if (done - shown >= r.size / 100 || done == r.size) { shown = done; val d = done; withContext(Dispatchers.Main) { phase = Phase.Downloading(d, r.size) } }
                    }
                }
            }
            val sha = digest.digest().joinToString("") { "%02x".format(it) }
            if (done != r.size || sha != r.sha256) throw Mismatch()
            if (!part.renameTo(target)) error("rename")
        } finally {
            c.disconnect()
            part.delete()
        }
    }

    private fun open(url: String): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000; readTimeout = 30_000
        useCaches = false
        // nothing about the phone: Android's default User-Agent names the model and the system version
        setRequestProperty("User-Agent", "FITest")
    }

    private fun get(url: String): String {
        val c = open(url)
        try {
            if (c.responseCode != 200) error("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }

    /** Whether Android lets this app hand APKs to the installer ("Inštalovať neznáme aplikácie" for FITest). */
    fun allowed(ctx: Context) = ctx.packageManager.canRequestPackageInstalls()

    /** The Settings page with the one switch for this app. */
    fun permissionIntent(ctx: Context) = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${ctx.packageName}"))

    /**
     * Back from Settings: with the permission on, the install goes on by itself. Turning the permission on can kill
     * the process, so the wish to install is kept in the store for a few minutes and the install resumes after a restart.
     */
    fun afterPermission(activity: Activity) {
        val store = dev.hamper.tabula.Tabula.store(activity)
        val p = phase
        if (!allowed(activity)) return
        if (p is Phase.NeedsPermission) return install(activity, p.file)
        val r = store.release ?: return
        if (currentTimeMillis() < store.resumeInstallUntil && p !is Phase.Installing) {
            store.resumeInstallUntil = 0L; store.save()
            fileFor(activity, r).takeIf { it.isFile }?.let { install(activity, it) }
        }
    }

    private fun install(activity: Activity, file: File) {
        if (!file.isFile) { phase = Phase.Idle; return }
        if (!allowed(activity)) {
            phase = Phase.NeedsPermission(file)
            dev.hamper.tabula.Tabula.store(activity).run { resumeInstallUntil = currentTimeMillis() + 10 * 60_000L; save() }
            return
        }
        phase = Phase.Installing(file)
        dev.hamper.tabula.Tabula.store(activity).run { if (resumeInstallUntil != 0L) { resumeInstallUntil = 0L; save() } }
        // writing the APK into the installer session is a copy of megabytes: off the main thread
        staging = scope.launch {
            val failed = stage(activity, file).exceptionOrNull()
            if (failed != null) withContext(Dispatchers.Main) { Log.w(TAG, "install session failed", failed); phase = Phase.Failed("Inštalátor sa nepodarilo otvoriť. Skús to znova.") }
        }
    }

    private fun stage(activity: Activity, file: File) = runCatching {
            val installer = activity.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(activity.packageName)
                setSize(file.length())
                // the system always asks; this app never installs on its own
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
            }
            val id = installer.createSession(params)
            installer.openSession(id).use { s ->
                s.openWrite("base.apk", 0, file.length()).use { out -> file.inputStream().use { it.copyTo(out) }; s.fsync(out) }
                val status = Intent(activity, activity::class.java).setAction(ActionStatus)
                // the installer's answer opens our activity, which then shows its confirmation: on Android 14+ the
                // creator (this visible app) has to allow that start, the installer sends without that privilege
                val opts = if (android.os.Build.VERSION.SDK_INT >= 34) android.app.ActivityOptions.makeBasic().apply {
                    @Suppress("DEPRECATION")
                    setPendingIntentCreatorBackgroundActivityStartMode(
                        if (android.os.Build.VERSION.SDK_INT >= 36) android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
                        else android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
                    )
                }.toBundle() else null
                val pi = PendingIntent.getActivity(activity, 31, status, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE, opts)
                s.commit(pi.intentSender)
            }
        }

    /** The installer's answer, delivered to MainActivity. */
    fun onStatus(activity: Activity, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // the sheet went away meanwhile (Neskôr, or a link opened a lesson): no installer over it, the file stays ready
                if (!shown) {
                    runCatching { activity.packageManager.packageInstaller.abandonSession(intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)) }
                    phase = lastReady(activity)
                } else intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)?.let { runCatching { activity.startActivity(it) } }
            }
            PackageInstaller.STATUS_SUCCESS -> phase = Phase.Idle
            PackageInstaller.STATUS_FAILURE_ABORTED -> phase = lastReady(activity)
            PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE ->
                phase = Phase.Failed("Táto verzia sa nedá nainštalovať cez tú, čo máš (iný podpis). Stiahni ju zo stránky FITagoras.")
            else -> {
                Log.i(TAG, "install status $status: ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}")
                phase = Phase.Failed("Inštalácia sa nepodarila. Skús to znova.")
            }
        }
    }

    /** After a cancelled install the verified file is still there: "Aktualizovať" opens the installer again. */
    private fun lastReady(ctx: Context): Phase =
        dev.hamper.tabula.Tabula.store(ctx).release?.let { fileFor(ctx, it) }?.takeIf { it.isFile }?.let { Phase.Ready(it) } ?: Phase.Idle
}

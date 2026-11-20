package dev.hamper.tabula.system

import android.annotation.SuppressLint
import android.content.Context as AndroidContext
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.hamper.tabula.Env
import dev.hamper.tabula.data.ClipDef
import dev.hamper.tabula.screens.ClipKey
import dev.hamper.tabula.screens.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Anonymous statistics and "Nahlásiť chybu" (README.md: what is sent and how long it is kept). What each call
 * sends is decided in [Tracker] (jvmCommonMain); this side keeps the queue off the main thread, sends through
 * WorkManager when there is a network, and adds what only Android knows: the phone's context (maker, model, Android,
 * screen, text size, dark mode, language, time zone, network, emulator or not), the phone's hash ([deviceHash] of
 * ANDROID_ID: one per phone and app key, so an update and a reinstall continue the same history; no serial number, no
 * account), and crashes, written down as the app dies and sent at the next start. No name, no e-mail, no photos.
 * [enabled] off (Nastavenia) stops it all, drops what waits and forgets the install id.
 */
actual object Telemetry {
    private var prefs: SharedPreferences? = null
    private var appCtx: AndroidContext? = null
    private var dir: File? = null
    private var tracker: Tracker? = null

    /** "Posielať štatistiky", on unless switched off. */
    actual var enabled by mutableStateOf(true)

    /** Queue files are written here, never on the main thread. */
    private val disk = Executors.newSingleThreadExecutor()
    private val sending = Mutex()

    @Synchronized
    fun init(ctx: AndroidContext) {
        if (tracker != null) return
        val app = ctx.applicationContext
        appCtx = app
        val p = app.getSharedPreferences("telemetry", AndroidContext.MODE_PRIVATE)
        prefs = p
        val d = File(app.filesDir, "telemetry")
        dir = d
        // the old builds kept "enabled" as a boolean
        if (p.all["enabled"] is Boolean) p.edit().putString("enabled", if (p.getBoolean("enabled", true)) "1" else "0").apply()
        val t = Tracker(d, AndroidPrefs(p), enqueue = { job -> disk.execute(job) }, queued = { soon -> schedule(if (soon) 0 else BatchMinutes) })
        tracker = t
        enabled = t.enabled
        // a crash: written down now (the app is dying, nothing else runs), sent at the next start
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            if (t.enabled) Tracker.writeCrash(d, e)
            previous?.uncaughtException(thread, e)
        }
        // left over from a send that gave up (offline for long): try again now that the app runs
        disk.execute { if (t.reports.all().isNotEmpty() || t.events.all().isNotEmpty()) schedule(0, replace = false) }
    }

    /** At the start of the app, once the course is known: an update since last time, a crash from last time. */
    fun started(courseId: String) {
        val t = tracker ?: return
        val d = dir ?: return
        if (Env.VERSION_NAME.isNotEmpty()) t.started(Env.VERSION_NAME, courseId)
        disk.execute { Tracker.readCrash(d)?.let { (kind, message, stack) -> t.crashed(kind, message, stack, courseId) } }
    }

    private val ready get() = tracker != null && ClipKey != null
    private inline fun on(f: Tracker.() -> Unit) { if (ready) tracker?.f() }

    actual fun switch(on: Boolean) { enabled = on; tracker?.switch(on) }
    actual fun opened(courseId: String, parallel: String?) = on { opened(courseId, parallel) }
    actual fun lessonStarted(lessonId: String) = on { lessonStarted(lessonId) }
    actual fun stepShown(lessonId: String, pos: Int) = on { stepShown(lessonId, pos) }
    actual fun skipped(how: String) = on { skipped(how) }
    actual fun stepLeft() = on { stepLeft() }
    actual fun lessonLeft() = on { lessonLeft() }
    actual fun lessonFinished(lessonId: String) = on { lessonFinished(lessonId) }
    actual fun given(key: String, text: String) = on { given(key, text) }
    actual fun seed(key: String, seed: String) = on { seed(key, seed) }
    actual fun answer(key: String, ok: Boolean, aided: Boolean, attempt: Int, courseId: String, fallbackText: String?) =
        on { answer(key, ok, aided, attempt, courseId, fallbackText) }
    actual fun help(kind: String, key: String?) = on { help(kind, key) }
    actual fun clipPlayed(clip: ClipDef, watched: Double, lecture: String) = on { clipPlayed(clip, watched, lecture) }
    actual fun mockTest(courseId: String, paper: String, score: Int, max: Int) = on { mockTest(courseId, paper, score, max) }
    actual fun update(stage: String, version: String?) = on { update(stage, version) }
    actual fun report(item: String, text: String, courseId: String) = on { report(item, text, courseId) }

    /* ---- the phone ------------------------------------------------------------------------------------------------ */

    @SuppressLint("HardwareIds") // ANDROID_ID is per app key and phone; only a salted hash leaves the phone
    private fun device(ctx: AndroidContext): String? =
        runCatching { Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) }.getOrNull()?.takeIf { it.isNotBlank() }?.let(::deviceHash)

    private fun context(ctx: AndroidContext): Context {
        val cfg = ctx.resources.configuration
        val net = runCatching {
            val cm = ctx.getSystemService(ConnectivityManager::class.java)
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            when {
                caps == null -> "none"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "other"
            }
        }.getOrNull()
        val emulator = Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.contains("emulator") || Build.MODEL.startsWith("sdk_gphone") ||
            Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish") || Build.PRODUCT.contains("sdk")
        return Context(
            maker = Build.MANUFACTURER, model = Build.MODEL, os = Build.VERSION.RELEASE, sdk = Build.VERSION.SDK_INT,
            screen = "${cfg.screenWidthDp}x${cfg.screenHeightDp}", font = cfg.fontScale,
            dark = (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES,
            lang = Locale.getDefault().toLanguageTag().take(12), tz = TimeZone.getDefault().id, net = net, emulator = emulator,
        )
    }

    /* ---- sending ---------------------------------------------------------------------------------------------- */

    private const val BatchMinutes = 15L
    private const val Work = "telemetry-send"

    /** [replace]: a report goes now, pulling a waiting batch forward; otherwise join what is already planned. */
    private fun schedule(delayMinutes: Long, replace: Boolean = delayMinutes == 0L) {
        val ctx = appCtx ?: return
        val req = OneTimeWorkRequestBuilder<TelemetryWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        runCatching { WorkManager.getInstance(ctx).enqueueUniqueWork(Work, if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP, req) }
    }

    /** Sends what waits, one sender at a time (a replaced worker may still be finishing). -> false: try again later. */
    internal suspend fun flush(): Boolean = sending.withLock {
        withContext(Dispatchers.IO) {
            val t = tracker ?: return@withContext true
            val key = ClipKey ?: return@withContext true
            val ctx = appCtx ?: return@withContext true
            t.send(Env.VERSION_NAME, Env.DEBUG, device(ctx), context(ctx)) { path, body -> Tracker.post(Server, key, path, body) }
        }
    }
}

private class AndroidPrefs(private val p: SharedPreferences) : Prefs {
    override fun get(key: String): String? = p.all[key]?.toString()
    override fun put(key: String, value: String?) { p.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply() }
    override fun keys(): Set<String> = p.all.keys
}

class TelemetryWorker(ctx: AndroidContext, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        Telemetry.init(applicationContext)
        return if (Telemetry.flush()) Result.success() else if (runAttemptCount < 8) Result.retry() else Result.success()
    }
}

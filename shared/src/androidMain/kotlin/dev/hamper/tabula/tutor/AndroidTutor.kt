package dev.hamper.tabula.tutor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.Composable
import dev.hamper.tabula.tutor.siwc.HttpResponses
import dev.hamper.tabula.tutor.siwc.Siwc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.withLock

/**
 * The tutor on Android: the ChatGPT connection (Siwc with the Keystore vault and the loopback listener kept alive by a
 * foreground service while the browser is in front), the client, the in-app camera (TutorFotak.kt). One per process.
 * [ikona]: the app's small notification icon.
 */
class AndroidTutor private constructor(private val app: Context, private val ikona: Int) : TutorSluzba {
    companion object {
        @Volatile private var one: AndroidTutor? = null
        fun get(context: Context, ikona: Int): AndroidTutor = one ?: synchronized(this) { one ?: AndroidTutor(context.applicationContext, ikona).also { one = it } }
    }

    private val log = Zaznam { u, t -> Log.i("Tutor", "[$u] $t") }
    private val siwc = Siwc(KeystoreTrezor(app), "FITagoras", "intent://tutor#Intent;scheme=tabula;package=${app.packageName};end", log)
    private val prefs = app.getSharedPreferences("tutor", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stav = MutableStateFlow(siwc.stav())
    override val pripojenie: StateFlow<Pripojenie> = stav
    @Volatile private var cachedModely: List<String>? = null
    private var posluchac: Siwc.Posluchac? = null

    /** One sign-in at a time: a new tap ends the previous attempt (its listener, its wait), never the other way round. */
    private val pokusy = kotlinx.coroutines.sync.Mutex()
    @Volatile private var aktualny = 0

    override fun pripoj(suhlas: Boolean) {
        val moj = ++aktualny
        posluchac?.zavri()
        scope.launch {
            pokusy.withLock {
                if (moj != aktualny) return@withLock
                val (p, url) = try { siwc.pripravPrihlasenie(suhlas) } catch (e: Exception) { log.log('!', "Prihlásenie sa nedá začať: ${e.javaClass.simpleName}"); stav.value = siwc.stav(); return@withLock }
                // cancelled while the attempt was being prepared: nothing waits, nothing opens
                if (moj != aktualny) { p.zavri(); siwc.zrusPokus(p.pokus); stav.value = siwc.stav(); return@withLock }
                posluchac = p
                stav.value = Pripojenie.Pripaja
                drz("Čakám na prihlásenie do ChatGPT")
                otvor(url)
                try {
                    // the student may leave the browser without signing in: give up after 10 minutes
                    val cb = withTimeoutOrNull(10 * 60_000L) { p.vysledok.await() }
                    if (cb != null && moj == aktualny) siwc.dokonci(p.pokus, cb) else siwc.zrusPokus(p.pokus)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException && moj != aktualny) log.log('i', "Prihlásenie nahradilo nové") else
                        log.log('!', "Prihlásenie zlyhalo: ${(e as? TutorChyba)?.popis() ?: e.javaClass.simpleName}")
                    siwc.zrusPokus(p.pokus)
                } finally {
                    p.zavri()
                    if (posluchac === p) posluchac = null
                    cachedModely = null
                    stav.value = siwc.stav()
                    drz(null) // every attempt releases what it took, replaced or not
                }
            }
        }
    }

    /** The wait ends at once (emulator, 19. 11.: before, only a restart of the app ended it); the attempt's finally resets the state. */
    override fun zrusPrihlasenie() {
        ++aktualny
        posluchac?.zavri() ?: run { stav.value = siwc.stav() }
    }

    override fun odpoj() {
        ++aktualny
        posluchac?.zavri()
        scope.launch {
            siwc.odhlas()
            cachedModely = null
            stav.value = siwc.stav()
        }
    }

    override fun klient(): ResponsesKlient? {
        if (stav.value !is Pripojenie.Pripojeny) return null
        val http = HttpResponses(siwc, log)
        // a revoked or refused token turns the tutor off everywhere at once
        return object : ResponsesKlient {
            override suspend fun odpoved(telo: dev.hamper.tabula.compat.json.JSONObject, onText: (String) -> Unit) =
                try { http.odpoved(telo, onText) } catch (e: TutorChyba) {
                    if (e.druh == TutorChyba.Druh.ODPOJENE || e.druh == TutorChyba.Druh.NEPOVOLENE) stav.value = siwc.stav()
                    throw e
                }
            override suspend fun modely() = http.modely()
        }
    }

    override suspend fun modely(): List<String> = cachedModely ?: HttpResponses(siwc, log).modely().also { cachedModely = it }

    override fun spravovatVyuzitie() = otvor(Siwc.USAGE_URL)

    override var poucenyOPlane: Boolean
        get() = prefs.getBoolean("poucenie", false)
        set(v) { prefs.edit().putBoolean("poucenie", v).apply() }

    override var vyvojar: Boolean
        get() = prefs.getBoolean("vyvojar", false)
        set(v) { prefs.edit().putBoolean("vyvojar", v).apply() }

    /** Reasons the service is held for (sign-in, a request); it stops when the last one ends. */
    private var drzi = 0

    @Synchronized
    override fun drz(dovod: String?) {
        // the stop goes through startForegroundService too: the service then always calls startForeground before it
        // stops, so a start and a stop in the same instant can't crash the app ("did not call startForeground")
        val i = Intent(app, TutorService::class.java).putExtra("ikona", ikona)
        if (dovod != null) drzi++ else if (drzi > 0) drzi-- else return
        if (dovod == null && drzi > 0) return
        runCatching { app.startForegroundService(if (dovod == null) i.setAction(TutorService.STOP) else i.putExtra("text", dovod)) }
            .onFailure { log.log('!', "Služba sa nespustila: ${it.javaClass.simpleName}") }
    }

    // ---- pages collected for the harness by the earlier tutor screen (developer mode): still exportable, no longer added

    private val zbierka get() = java.io.File(app.filesDir, "tutor-zbierka")

    override fun zbierkaPocet(): Int = zbierka.listFiles()?.count { java.io.File(it, "zaznam.json").exists() } ?: 0

    override fun exportujZbierku() {
        scope.launch {
            runCatching {
                val out = java.io.File(app.cacheDir, "tutor-export").apply { mkdirs() }
                out.listFiles()?.forEach { it.delete() }
                val zip = java.io.File(out, "fitagoras-zbierka-${System.currentTimeMillis()}.zip")
                java.util.zip.ZipOutputStream(zip.outputStream()).use { z ->
                    zbierka.walkTopDown().filter { it.isFile }.forEach { f ->
                        z.putNextEntry(java.util.zip.ZipEntry(f.relativeTo(zbierka).invariantSeparatorsPath)); f.inputStream().use { it.copyTo(z) }; z.closeEntry()
                    }
                }
                val uri = androidx.core.content.FileProvider.getUriForFile(app, "${app.packageName}.tutor", zip)
                val send = Intent(Intent.ACTION_SEND).setType("application/zip").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                app.startActivity(Intent.createChooser(send, "Zbierka pre harness").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure { log.log('!', "Zbierka: export zlyhal ${it.javaClass.simpleName}") }
        }
    }

    override fun vymazZbierku() { scope.launch { zbierka.deleteRecursively(); java.io.File(app.cacheDir, "tutor-export").deleteRecursively() } }

    private fun otvor(url: String) {
        runCatching { app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { log.log('!', "Prehliadač sa neotvoril: ${it.javaClass.simpleName}") }
    }

    @Composable
    override fun Fotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit) = TutorFotak(maxStran, onHotovo, onZavri)
}

/**
 * Keeps the process running while the browser is in front during sign-in (without it Android freezes the app and the
 * 127.0.0.1 listener never answers; the prototype showed it) and while a tutor request runs with the app in the
 * background. It only holds the process, the work runs in AndroidTutor.
 */
class TutorService : Service() {
    companion object { const val STOP = "dev.hamper.tabula.tutor.STOP" }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("tutor", "Pytagoras", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "tutor")
            .setContentTitle("FITagoras").setContentText(intent?.getStringExtra("text") ?: "Pytagoras pracuje")
            .setSmallIcon(intent?.getIntExtra("ikona", android.R.drawable.stat_sys_upload) ?: android.R.drawable.stat_sys_upload)
            .setOngoing(true).build()
        startForeground(41, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        if (intent?.action == STOP) { stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
        return START_NOT_STICKY
    }
}

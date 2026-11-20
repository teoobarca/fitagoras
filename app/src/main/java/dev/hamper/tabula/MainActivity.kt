package dev.hamper.tabula

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.content.res.Configuration
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dev.hamper.tabula.compat.media.ExoClipPlayer
import dev.hamper.tabula.compat.media.PlayerSurface
import dev.hamper.tabula.compat.media.SURFACE_TYPE_TEXTURE_VIEW
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.system.MockTimerService
import dev.hamper.tabula.system.Notifications
import dev.hamper.tabula.ui.*

class MainActivity : ComponentActivity() {
    private val app = AppState()
    private lateinit var clips: ClipHost

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
        )
        // UI is Slovak whatever the phone is set to, so system-drawn pickers should match it.
        getSystemService(android.app.LocaleManager::class.java).run {
            if (applicationLocales.isEmpty) applicationLocales = android.os.LocaleList.forLanguageTags("sk-SK")
        }
        ClipLibrary.load()
        val store = Tabula.store(this)
        // short sounds, off until switched on in Nastavenia (ui/Sound.kt); loaded once for the process
        if (dev.hamper.tabula.ui.Sounds.player == null) AndroidSfx(applicationContext).let { sfx -> dev.hamper.tabula.ui.Sounds.player = sfx; dev.hamper.tabula.ui.Sounds.muted = sfx::muted }
        clips = ClipHost(ExoClipPlayer(this), onPip = { enterPip() }, onFull = ::fullscreen)
        val platform = AndroidPlatform(this)
        // the tutor is an add-on: a failure to set it up leaves the app without it, never without the app
        val tutor = runCatching { dev.hamper.tabula.tutor.AndroidTutor.get(this, R.drawable.ic_stat) }
            .onFailure { android.util.Log.w("Tutor", "Tútor sa nespustil: ${it.javaClass.simpleName}") }.getOrNull()
        Notifications.channels(this)
        // A restored activity must not replay the link that originally opened it.
        if (savedInstanceState == null) {
            if (intent?.action == dev.hamper.tabula.system.Updater.ActionStatus) dev.hamper.tabula.system.Updater.onStatus(this, intent) else handle(intent)
        }
        setContent {
            TabulaTheme(store.theme) {
                val light = !T.c.dark
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = light
                        isAppearanceLightNavigationBars = light
                    }
                }
                // in picture-in-picture the app stays composed (the clip step keeps playing) and the clip's one picture fills the window
                CompositionLocalProvider(LocalPlatform provides platform, dev.hamper.tabula.tutor.LocalTutor provides tutor) { App(app, store, clips, ::startMock, ::stopMock) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == dev.hamper.tabula.system.Updater.ActionStatus) dev.hamper.tabula.system.Updater.onStatus(this, intent) else handle(intent)
    }

    /** tabula://lesson, tabula://lesson/obal, tabula://paper/sova-2025/2 ... */
    private fun handle(i: Intent?) {
        val data = i?.data ?: return
        // tabula://tutor: the ChatGPT sign-in page sends the student back; the app just comes to the front
        if (data.scheme == "tabula" && data.host == "tutor") return
        if (data.getQueryParameter("mock") == "stop") stopMock()
        if (data.scheme == "tabula") app.open((listOf(data.host ?: "") + data.pathSegments).joinToString(":"), Tabula.store(this))
    }

    /**
     * Fullscreen: landscape by the sensor, system bars hidden (a swipe shows them for a moment). Back: portrait until
     * the phone is held upright again, so a phone still held sideways does not jump straight back into fullscreen.
     */
    private fun fullscreen(on: Boolean) {
        val bars = WindowCompat.getInsetsController(window, window.decorView)
        if (on) {
            upright.disable()
            bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            bars.hide(WindowInsetsCompat.Type.systemBars())
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            bars.show(WindowInsetsCompat.Type.systemBars())
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (upright.canDetectOrientation()) upright.enable() else requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private val upright by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(angle: Int) {
                if (angle != ORIENTATION_UNKNOWN && (angle < 25 || angle > 335)) { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; disable() }
            }
        }
    }

    private fun enterPip() { clips.player.play(); enterPictureInPictureMode(pipParams()) }
    private fun pipParams() = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).setSeamlessResizeEnabled(true).apply { clips.bounds?.let { setSourceRectHint(android.graphics.Rect(it.left, it.top, it.right, it.bottom)) } }.build()

    /** In the background without picture-in-picture a clip stops, so the music it paused plays on. */
    override fun onStop() { super.onStop(); if (!isInPictureInPictureMode) clips.leave() }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (clips.open && clips.player.isPlaying) enterPictureInPictureMode(pipParams())
    }

    override fun onPictureInPictureModeChanged(isInPip: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPip, newConfig)
        clips.pip = isInPip
    }

    /** The clock is already running (AppState.startMock, on SystemClock.elapsedRealtime): its end goes to the notification. */
    private fun startMock() {
        Tabula.mockPaper = app.mockPaper
        MockTimerService.start(this, app.mockEnd)
    }

    private fun stopMock() { app.mockEnd = 0L; MockTimerService.stop(this) }

    override fun onResume() {
        super.onResume()
        val store = Tabula.store(this)
        dev.hamper.tabula.system.Telemetry.opened(store.course.id, store.parallels[store.course.id])
        // an update since the last start, a crash last time: sent once
        dev.hamper.tabula.system.Telemetry.started(store.course.id)
        // back from "Inštalovať neznáme aplikácie" with it on: the install goes on; else the check when it is due
        dev.hamper.tabula.system.Updater.afterPermission(this)
        dev.hamper.tabula.system.Updater.onStart(this, store)
    }

    override fun onDestroy() { upright.disable(); clips.release(); super.onDestroy() }
}

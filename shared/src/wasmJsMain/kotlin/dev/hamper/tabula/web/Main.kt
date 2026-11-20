@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.web

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.ComposeViewport
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Env
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tab
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.compat.SystemBack
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.platform.LocalStorageStateFile
import dev.hamper.tabula.platform.WebPlatform
import dev.hamper.tabula.platform.preloadRest
import dev.hamper.tabula.platform.preloadWeb
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.WebTutor
import dev.hamper.tabula.ui.SfxPlayer
import dev.hamper.tabula.ui.Sounds
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.AppWidth
import dev.hamper.tabula.ui.around
import dev.hamper.tabula.ui.aroundLine
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/** The version the build script writes into index.html's meta "fitagoras-version" (scripts/web.sh), else "web". */
private fun version(): String = document.querySelector("meta[name=fitagoras-version]")?.getAttribute("content") ?: "web"

/**
 * The web's own clip key, which scripts/web.sh writes into index.html's meta "fitagoras-clips" from secrets.properties
 * (clipsWebToken). Public by design, like the APK's: the server opens only lecture clips with it and can swap it on its
 * own. Without it (a development build) clips are read as captions.
 */
private fun clipKey(): String = document.querySelector("meta[name=fitagoras-clips]")?.getAttribute("content").orEmpty()

/**
 * FITagoras in the browser: the same screens and progress document as the phone (docs/KMP.md, "The web"). Lecture
 * clips play in a <video> under the canvas (WebClipPlayer); there are no statistics, reports, reminders, widget or
 * in-app updates; the tutor signs in by a pasted address (tutor/WebTutor).
 */
fun main() {
    MainScope().launch {
        try {
            start()
        } catch (e: Throwable) {
            // before the first frame the splash says it in words, with "Skúsiť znova" (resources/web.js)
            jsFailed(e.toString())
            throw e
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private suspend fun start() {
    Env.install(debug = false, clipsGuestToken = clipKey(), versionName = version())
    preloadWeb()
    ClipLibrary.load()
    // the sounds play through Web Audio, from the app's own files next to index.html (zvuky/)
    Sounds.player = SfxPlayer { s -> jsPlay(s.file, s.gain.toDouble()) }
    val store = Tabula.store { LocalStorageStateFile() }
    val app = AppState()
    // a timed test still running comes back on top of whatever the address says (a reload, a tab the browser dropped)
    // this tab's own run: handing it in removes only that one (another tab may have started one since)
    var myMock = if (restoreMock(app)) localStorage.getItem(MOCK) else null
    if (myMock != null) app.open("mockrun", store)
    else window.location.hash.removePrefix("#").takeIf { it.isNotEmpty() }?.let { app.open(it, store) }
    // the browser's back (the iOS edge swipe, Android's back) is the app's back: a lesson's own handler first,
    // then one screen down; on the home screen it does nothing rather than leave the app
    jsOnBack { if (!SystemBack.press()) app.pop() }
    jsOnRoute { app.open(it, store) }
    // one dp is one CSS pixel here (Compose takes the device pixel ratio as its density)
    jsColumn(AppWidth.value.toDouble())
    ComposeViewport(document.getElementById("app")!!) {
        val clips = remember {
            val player = WebClipPlayer()
            lateinit var host: ClipHost
            // fullscreen: the browser's (the iPhone's own player, with the captions as a text track), and the app's layer
            host = ClipHost(player, onPip = player::pip, onFull = { on -> if (on) player.captions(host.clip, host.captions); player.full(on) }, canPip = player.canPip, fullOnTurn = player.phone)
            player.onFullExit = { host.goFull(false) }
            host
        }
        TabulaTheme(store.theme) {
            // the page under the app and the browser's bars take the app's background, light or dark; a window wider
            // than the app's column the tone around it (ui/AppColumn.kt), as the splash had them (index.html)
            val c = T.c
            SideEffect { jsChrome(css(c.bg), css(c.around), css(c.aroundLine)) }
            CompositionLocalProvider(LocalPlatform provides WebPlatform, LocalTutor provides WebTutor) {
                App(app, store, clips,
                    { myMock = app.mockPaper?.let { "$it ${app.mockEnd}" }?.also { localStorage.setItem(MOCK, it) } },
                    { if (localStorage.getItem(MOCK) == myMock) localStorage.removeItem(MOCK); myMock = null })
            }
            // the screen on top stays in the address, so a reload or a later visit comes back to it (web.js route)
            LaunchedEffect(Unit) { snapshotFlow { routeOf(app.stack.lastOrNull(), app.tab) }.collect { jsRoute(it) } }
            // the splash (index.html) fades out only once the first screen is drawn, so nothing blinks between them
            LaunchedEffect(Unit) { withFrameNanos {}; withFrameNanos {}; jsReady() }
        }
    }
    preloadRest()
    // ask the browser to keep this site's storage (Safari grants it to an app on the home screen)
    runCatching { jsPersist() }
}

/** A colour as CSS writes it, "#rrggbb". */
private fun css(c: androidx.compose.ui.graphics.Color): String = "#" + (c.toArgb() and 0xFFFFFF).toString(16).padStart(6, '0')

/** The timed test that is on: "<paper> <end>", written at its start, removed when it is handed in (App's mock hooks). */
private const val MOCK = "fitest.mock"

/**
 * The timed test left running before this page load, back in [app] with its end: the clock is the wall clock
 * (compat/SystemClock), so the time away counts. One left on for half a day is dropped rather than resumed at 0:00.
 */
private fun restoreMock(app: AppState): Boolean {
    val saved = localStorage.getItem(MOCK) ?: return false
    val paper = saved.substringBeforeLast(' ')
    val end = saved.substringAfterLast(' ').toLongOrNull()
    if (end == null || dev.hamper.tabula.compat.SystemClock.elapsedRealtime() > end + 12 * 3_600_000L || runCatching { dev.hamper.tabula.screens.paperById(paper) }.isFailure) {
        localStorage.removeItem(MOCK)
        return false
    }
    app.mockPaper = paper
    app.mockEnd = end
    return true
}

/**
 * The route ([AppState.open]) that opens [top] again: the screens worth coming back to after a reload. A lesson goes on
 * where it was left; a screen without a route of its own (a lesson's end, a timed test) gives the tab under it.
 */
private fun routeOf(top: Nav?, tab: Tab): String = when (top) {
    is Nav.Lesson -> "lesson:${top.id}"
    is Nav.Item -> top.route
    is Nav.Template -> "gen:${top.gen}"
    is Nav.Paper -> "paper:${top.paper}:${top.n}"
    is Nav.TestProblem -> "priklad:${top.n}"
    is Nav.Library -> "library:${top.section}"
    Nav.Review -> "review"
    Nav.Practice -> "cvicenia"
    Nav.Topics -> "temy"
    Nav.Settings -> "settings"
    Nav.Gem -> "gem"
    Nav.Counter -> "counter"
    else -> when (tab) { Tab.Dnes -> ""; Tab.Cesta -> "cesta"; Tab.Nacvik -> "nacvik" }
}

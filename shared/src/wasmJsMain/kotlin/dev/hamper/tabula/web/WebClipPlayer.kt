@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import dev.hamper.tabula.compat.media.HttpDataSource
import dev.hamper.tabula.compat.media.OwnSurface
import dev.hamper.tabula.compat.media.PlaybackException
import dev.hamper.tabula.compat.media.PlaybackParameters
import dev.hamper.tabula.compat.media.Player
import dev.hamper.tabula.data.ClipDef

/**
 * The clip player in the browser: the one <video> of resources/web.js (fitWeb.clip), under the app's canvas, behind
 * the same [Player] the lesson screen drives on Android (ExoClipPlayer). The screen code is the same; what differs:
 * - the clip key goes in the address (`&key=`), since a <video> cannot send a header; the server takes only its guest
 *   keys there. The address is this page's own server (the app is served from it), so the path is kept and the origin
 *   dropped.
 * - [Surface] clears the canvas where the picture is, and web.js lays the video exactly under that hole.
 * - an error of the video says nothing about why: web.js asks the server for one byte and reports its status, which
 *   becomes the same [HttpDataSource.InvalidResponseCodeException] ExoPlayer gives (null cause: no answer at all).
 * - play() outside a tap may be refused by the browser (a reload straight into the clip): it stays paused, the button
 *   says play, and the user's tap starts it.
 */
class WebClipPlayer : Player, OwnSurface {
    private val listeners = mutableListOf<Player.Listener>()
    private var src: String? = null
    private var wantPlay = false
    private var error: PlaybackException? = null
    private var lastState = Player.STATE_IDLE
    private var lastPlaying = false
    private var lastWant = false
    /** Fullscreen ended by the browser (the native player's Done, Esc, the back button): ClipHost goes back to the lesson. */
    var onFullExit: () -> Unit = {}

    init {
        jsClipOn { e -> event(e.toString()) }
    }

    private fun status(): List<Int> = jsClipStatus().split(' ').map { it.toIntOrNull() ?: 0 }

    override val isPlaying: Boolean
        get() {
            if (src == null || error != null) return false
            val (paused, ready, ended, seeking) = status()
            return paused == 0 && ended == 0 && seeking == 0 && ready >= 3
        }
    override val playWhenReady get() = wantPlay
    override val playbackState: Int
        get() {
            if (src == null || error != null) return Player.STATE_IDLE
            val (_, ready, ended, seeking) = status()
            return when {
                ended == 1 -> Player.STATE_ENDED
                // nothing loaded yet: a wait while play is asked for (the server may still be cutting); paused (a reload
                // straight into the clip, iOS loads nothing ahead) it is a clip waiting for its tap, not a spinner
                ready == 0 -> if (wantPlay) Player.STATE_BUFFERING else Player.STATE_IDLE
                (wantPlay || seeking == 1) && ready < 3 -> Player.STATE_BUFFERING
                else -> Player.STATE_READY
            }
        }
    override val currentPosition: Long get() = (jsClipTime() * 1000).toLong()
    override val duration: Long get() = jsClipDuration().let { if (it < 0) -1L else (it * 1000).toLong() }
    override val playerError: PlaybackException? get() = error
    override var playbackParameters = PlaybackParameters(1f)
        set(v) { field = v; jsClipRate(v.speed.toDouble()) }

    override fun play() {
        wantPlay = true
        if (src != null && error == null) jsClipPlay()
        changed()
    }

    override fun pause() {
        wantPlay = false
        jsClipPause()
        changed()
    }

    override fun prepare() {
        val s = src ?: return
        if (error != null) { error = null; jsClipLoad(s); jsClipRate(playbackParameters.speed.toDouble()) }
        changed()
    }

    override fun seekTo(positionMs: Long) = jsClipSeek(positionMs.coerceAtLeast(0) / 1000.0)
    override fun setPlaybackSpeed(speed: Float) { playbackParameters = PlaybackParameters(speed) }
    override fun addListener(listener: Player.Listener) { listeners += listener }
    override fun removeListener(listener: Player.Listener) { listeners -= listener }
    override fun release() { src = null; jsClipUnload(); changed() }

    override fun load(url: String, headers: Map<String, String>) {
        val key = headers["Authorization"]?.removePrefix("Bearer ")?.trim().orEmpty()
        val path = url.substringAfter("://").let { r -> r.indexOf('/').let { if (it < 0) "/" else r.substring(it) } }
        val s = if (key.isEmpty()) path else path + (if ('?' in path) "&" else "?") + "key=" + jsEncode(key)
        src = s
        error = null
        jsClipLoad(s)
        jsClipRate(playbackParameters.speed.toDouble())
        changed()
    }

    /** The captions of [clip] for the native fullscreen player (iPhone), shown there when [on]. */
    fun captions(clip: ClipDef?, on: Boolean) = jsClipCaptions(clip?.let(::vtt) ?: "", on)

    val canPip: Boolean get() = jsClipCanPip()
    /** A phone (a touch screen, small): turning it sideways means fullscreen, as on Android; a desktop window is wide anyway. */
    val phone: Boolean get() = jsClipPhone()
    fun pip() = jsClipPip()

    /** Into the browser's fullscreen (the iPhone: its own player) or out of it; the app's layer goes fullscreen anyway. */
    fun full(on: Boolean) { jsClipFull(on) }

    private fun event(e: String) {
        when {
            e.startsWith("error:") -> {
                val code = e.removePrefix("error:").toIntOrNull() ?: 0
                // -1: the server answers fine, the video itself failed; 0: no answer (offline, the server down)
                val cause = if (code > 0) HttpDataSource.InvalidResponseCodeException(code) else null
                error = PlaybackException("clip failed ($code)", cause, code)
                val err = error!!
                listeners.toList().forEach { it.onPlayerError(err) }
            }
            e == "blocked" -> wantPlay = false // the browser wants a tap first
            e == "fullexit" -> onFullExit()
            // the native player's own buttons (iPhone fullscreen, PiP) play and pause too
            e == "play" -> wantPlay = true
            e == "pause" -> if (status()[2] == 0) wantPlay = false
        }
        changed()
    }

    private fun changed() {
        val state = playbackState
        val playing = isPlaying
        val want = wantPlay
        if (want != lastWant) { lastWant = want; listeners.toList().forEach { it.onPlayWhenReadyChanged(want, 1) } }
        if (state != lastState) { lastState = state; listeners.toList().forEach { it.onPlaybackStateChanged(state) } }
        if (playing != lastPlaying) { lastPlaying = playing; listeners.toList().forEach { it.onIsPlayingChanged(playing) } }
    }

    /** The frame of the picture: a hole in the canvas, with the video laid under it by web.js. */
    @Composable
    override fun Surface(modifier: Modifier) {
        DisposableEffect(Unit) { onDispose { jsClipHide() } }
        Box(
            modifier
                .onGloballyPositioned { c -> val p = c.positionInWindow(); jsClipPlace(p.x.toDouble(), p.y.toDouble(), c.size.width.toDouble(), c.size.height.toDouble()) }
                .drawBehind { drawRect(Color.Black, blendMode = BlendMode.Clear) },
        )
    }
}

/** WebVTT of the clip's captions, times from the clip's start. */
internal fun vtt(clip: ClipDef): String = buildString {
    append("WEBVTT\n\n")
    fun t(s: Double): String {
        val ms = (s.coerceAtLeast(0.0) * 1000).toLong()
        fun two(n: Long) = n.toString().padStart(2, '0')
        return "${two(ms / 3_600_000)}:${two(ms / 60_000 % 60)}:${two(ms / 1000 % 60)}.${(ms % 1000).toString().padStart(3, '0')}"
    }
    clip.cues.forEachIndexed { i, c ->
        val end = maxOf(c.t1, c.t0 + 0.5)
        append(i + 1).append('\n').append(t(c.t0)).append(" --> ").append(t(end)).append('\n')
        append(c.text.replace("-->", "→")).append("\n\n")
    }
}

private fun jsClipOn(cb: (JsString) -> Unit): Unit = js("window.fitWeb.clip.on(cb)")
private fun jsClipLoad(src: String): Unit = js("window.fitWeb.clip.load(src)")
private fun jsClipUnload(): Unit = js("window.fitWeb.clip.unload()")
private fun jsClipPlay(): Unit = js("window.fitWeb.clip.play()")
private fun jsClipPause(): Unit = js("window.fitWeb.clip.pause()")
private fun jsClipSeek(s: Double): Unit = js("window.fitWeb.clip.seek(s)")
private fun jsClipRate(r: Double): Unit = js("window.fitWeb.clip.rate(r)")
private fun jsClipStatus(): String = js("window.fitWeb.clip.status()")
private fun jsClipTime(): Double = js("window.fitWeb.clip.time()")
private fun jsClipDuration(): Double = js("window.fitWeb.clip.duration()")
private fun jsClipPlace(x: Double, y: Double, w: Double, h: Double): Unit = js("window.fitWeb.clip.place(x, y, w, h)")
private fun jsClipHide(): Unit = js("window.fitWeb.clip.hide()")
private fun jsClipCaptions(vtt: String, on: Boolean): Unit = js("window.fitWeb.clip.captions(vtt, on)")
private fun jsClipCanPip(): Boolean = js("window.fitWeb.clip.canPip()")
private fun jsClipPip(): Unit = js("window.fitWeb.clip.pip()")
private fun jsClipPhone(): Boolean = js("window.fitWeb.clip.phone()")
private fun jsClipFull(on: Boolean): Boolean = js("window.fitWeb.clip.full(on)")

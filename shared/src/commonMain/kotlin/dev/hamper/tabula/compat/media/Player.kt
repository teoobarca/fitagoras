package dev.hamper.tabula.compat.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.hamper.tabula.system.ClipFocus

/*
 * The clip player as the lesson screen sees it: media3's Player, narrowed to what the screen calls (same names, same
 * constants). Android wraps ExoPlayer (shared/src/androidMain, ExoClipPlayer); the browser will wrap an HTML video.
 */
interface Player {
    interface Listener {
        fun onPlayerError(error: PlaybackException) {}
        fun onPlaybackStateChanged(playbackState: Int) {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {}
    }

    val isPlaying: Boolean
    val playWhenReady: Boolean
    val playbackState: Int
    val currentPosition: Long
    val duration: Long
    val playerError: PlaybackException?
    var playbackParameters: PlaybackParameters

    fun play()
    fun pause()
    fun prepare()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
    fun release()

    /** Streams [url] with these request headers (on Android: progressive HTTP, the clip server's timeouts). */
    fun load(url: String, headers: Map<String, String>)

    /** Audio focus for a playing clip (Android: AudioManager, see system/ClipFocus); elsewhere always granted. */
    fun focusPort(onPause: () -> Unit, onResume: () -> Unit): ClipFocus.Port = object : ClipFocus.Port {
        override fun request() = true
        override fun abandon() {}
        override fun pause() = onPause()
        override fun resume() = onResume()
    }

    companion object {
        const val STATE_IDLE = 1
        const val STATE_BUFFERING = 2
        const val STATE_READY = 3
        const val STATE_ENDED = 4
    }
}

/** media3's PlaybackException; [cause] is an [HttpDataSource.InvalidResponseCodeException] when the server said no. */
open class PlaybackException(message: String?, cause: Throwable?, val errorCode: Int) : Exception(message, cause)

data class PlaybackParameters(val speed: Float)

object HttpDataSource {
    class InvalidResponseCodeException(val responseCode: Int, message: String? = null) : Exception(message ?: "Response code: $responseCode")
}

const val SURFACE_TYPE_SURFACE_VIEW = 1
const val SURFACE_TYPE_TEXTURE_VIEW = 2

/** media3's PlayerSurface: the video of [player]. */
@Composable
expect fun PlayerSurface(player: Player, modifier: Modifier = Modifier, surfaceType: Int = SURFACE_TYPE_SURFACE_VIEW)

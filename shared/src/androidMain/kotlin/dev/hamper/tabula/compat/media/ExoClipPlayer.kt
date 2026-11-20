package dev.hamper.tabula.compat.media

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

/** The clip player on Android: ExoPlayer, set up and called exactly as the clip screen did before. */
class ExoClipPlayer(private val context: Context) : Player {
    /**
     * A clip is small (55 s is about 1 MB) and its keyframes are 10 s apart, so the whole clip stays in memory, ahead and
     * behind: a seek anywhere in it never downloads again. Seeks stay exact (CLOSEST_SYNC would land up to 5 s off the
     * tapped line); decoding from the keyframe before is the only wait left.
     */
    val exo: ExoPlayer = ExoPlayer.Builder(context)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(60_000, 600_000, 1_000, 1_500)
                .setBackBuffer(600_000, true)
                .build(),
        )
        // spoken media; the player does not touch audio focus itself (it would take it for good), ClipFocus does
        // headphones pulled out: the clip pauses instead of playing out loud
        .setHandleAudioBecomingNoisy(true)
        .setAudioAttributes(androidx.media3.common.AudioAttributes.Builder().setUsage(androidx.media3.common.C.USAGE_MEDIA).setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_SPEECH).build(), false)
        .build()
    private val listeners = HashMap<Player.Listener, androidx.media3.common.Player.Listener>()

    override val isPlaying get() = exo.isPlaying
    override val playWhenReady get() = exo.playWhenReady
    override val playbackState get() = exo.playbackState
    override val currentPosition get() = exo.currentPosition
    override val duration get() = exo.duration
    override val playerError get() = exo.playerError?.let(::wrap)
    override var playbackParameters: PlaybackParameters
        get() = PlaybackParameters(exo.playbackParameters.speed)
        set(v) { exo.playbackParameters = androidx.media3.common.PlaybackParameters(v.speed) }

    override fun play() = exo.play()
    override fun pause() = exo.pause()
    override fun prepare() = exo.prepare()
    override fun seekTo(positionMs: Long) = exo.seekTo(positionMs)
    override fun setPlaybackSpeed(speed: Float) = exo.setPlaybackSpeed(speed)
    override fun release() = exo.release()

    override fun addListener(listener: Player.Listener) {
        val l = object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) = listener.onPlayerError(wrap(error))
            override fun onPlaybackStateChanged(playbackState: Int) = listener.onPlaybackStateChanged(playbackState)
            override fun onIsPlayingChanged(isPlaying: Boolean) = listener.onIsPlayingChanged(isPlaying)
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = listener.onPlayWhenReadyChanged(playWhenReady, reason)
        }
        listeners[listener] = l
        exo.addListener(l)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)?.let { exo.removeListener(it) }
    }

    override fun load(url: String, headers: Map<String, String>) {
        val http = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(headers)
            .setConnectTimeoutMs(15_000).setReadTimeoutMs(180_000).setAllowCrossProtocolRedirects(false)
        exo.setMediaSource(ProgressiveMediaSource.Factory(http).createMediaSource(MediaItem.fromUri(url)))
        exo.prepare()
    }

    override fun focusPort(onPause: () -> Unit, onResume: () -> Unit): dev.hamper.tabula.system.ClipFocus.Port =
        dev.hamper.tabula.system.AndroidClipFocus(context, onPause, onResume)

    private fun wrap(e: androidx.media3.common.PlaybackException): PlaybackException {
        val http = e.cause as? androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
        return PlaybackException(e.message, http?.let { HttpDataSource.InvalidResponseCodeException(it.responseCode, it.message) } ?: e.cause, e.errorCode)
    }
}

@Composable
actual fun PlayerSurface(player: Player, modifier: Modifier, surfaceType: Int) {
    val exo = (player as ExoClipPlayer).exo
    androidx.media3.ui.compose.PlayerSurface(
        player = exo, modifier = modifier,
        surfaceType = if (surfaceType == SURFACE_TYPE_TEXTURE_VIEW) androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW else androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW,
    )
}

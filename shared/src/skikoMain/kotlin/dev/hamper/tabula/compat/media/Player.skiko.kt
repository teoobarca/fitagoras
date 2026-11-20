package dev.hamper.tabula.compat.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** A player that draws its own picture (the web's <video>, web/WebClipPlayer.kt). */
interface OwnSurface {
    @Composable
    fun Surface(modifier: Modifier)
}

/** The desktop has no video: the frame where it plays. The web's player places its <video> there. */
@Composable
actual fun PlayerSurface(player: Player, modifier: Modifier, surfaceType: Int) {
    if (player is OwnSurface) player.Surface(modifier) else Box(modifier.background(Color.Black))
}

/** A player that never plays: screenshots and tests on the desktop. */
class SilentPlayer : Player {
    override val isPlaying = false
    override val playWhenReady = false
    override val playbackState = Player.STATE_IDLE
    override val currentPosition = 0L
    override val duration = 0L
    override val playerError: PlaybackException? = null
    override var playbackParameters = PlaybackParameters(1f)
    override fun play() {}
    override fun pause() {}
    override fun prepare() {}
    override fun seekTo(positionMs: Long) {}
    override fun setPlaybackSpeed(speed: Float) {}
    override fun addListener(listener: Player.Listener) {}
    override fun removeListener(listener: Player.Listener) {}
    override fun release() {}
    override fun load(url: String, headers: Map<String, String>) {}
}

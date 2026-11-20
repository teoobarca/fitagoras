package dev.hamper.tabula.system

import dev.hamper.tabula.compat.AudioManager


/**
 * Audio focus of a lecture clip. While a clip plays it holds TRANSIENT focus, so the music app pauses and resumes by
 * itself once the clip stops (permanent focus would make Spotify or YT Music stop for good). Losing focus pauses the
 * clip: after a short loss (a call, a navigation prompt) it plays on when focus comes back, after a permanent one it
 * stays paused. Pure logic; the platform is behind [Port] so it can be tested (Android: AndroidClipFocus, shared/src/androidMain).
 */
class ClipFocus(private val port: Port) {
    interface Port {
        /** Asks for transient focus; true when granted. */
        fun request(): Boolean
        fun abandon()
        /** Pauses the clip because focus went away. */
        fun pause()
        /** Plays the clip again because focus came back. */
        fun resume()
        /** The ClipFocus this port reports focus changes to (Android: AudioManager's listener). */
        fun attach(focus: ClipFocus) {}
    }

    var held = false
        private set
    /** Paused by a short loss: plays again on gain. */
    var waiting = false
        private set

    /** The clip wants to play (play pressed, or loading to play) or stopped wanting to (paused, ended, step left, background). */
    fun playing(on: Boolean) {
        if (on) {
            // play pressed while a short loss lasts: ask again, the other sound may still be on
            if (waiting) { waiting = false; held = false }
            if (held) return
            held = port.request()
            if (!held) { port.abandon(); port.pause() }
        } else {
            // our own pause after a short loss keeps the request, so the system hands focus back to us
            if (waiting) return
            if (held) { held = false; port.abandon() }
        }
    }

    /** The clip is gone for now (step left, app in the background without picture-in-picture): focus goes back whatever happened. */
    fun stop() {
        waiting = false
        if (held) { held = false; port.abandon() }
    }

    fun changed(change: Int) {
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                waiting = false
                if (held) { held = false; port.abandon() }
                port.pause()
            }
            // speech is paused, never ducked under someone else's sound
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (!held) return
                waiting = true
                port.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> if (waiting) { waiting = false; port.resume() }
        }
    }
}

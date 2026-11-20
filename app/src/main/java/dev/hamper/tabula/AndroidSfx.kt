package dev.hamper.tabula

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import dev.hamper.tabula.ui.Sfx
import dev.hamper.tabula.ui.SfxPlayer

/**
 * The app's sounds (shared/.../ui/Sound.kt) through one SoundPool: every file decoded once at start (16-bit WAV from
 * assets/zvuky, by role), played at once. They go on the media volume (USAGE_GAME) and never ask for the audio focus,
 * so music or a podcast keeps playing under them. The media volume decides, as in any game; the phone's silent mode
 * mutes them (vibrate does not: 1.5.1 played only in the ringer's normal mode, so on vibrate Teo heard nothing). In
 * headphones a notch quieter. Clicks play at once and never pile up: a new one stops the one before, and a verdict in
 * the same touch stops it (Skontrolovať, an option judged when tapped: only the verdict is heard).
 *
 * `adb shell setprop log.tag.FeelSound DEBUG` makes any build log each sound it plays (FeelSound: role, stream, ms).
 */
class AndroidSfx(context: Context) : SfxPlayer {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build()
    private val loaded = HashSet<Int>()
    private val ids: Map<Sfx, Int>

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) synchronized(loaded) { loaded += id } }
        ids = Sfx.entries.associateWith { s -> context.assets.openFd("zvuky/${s.file}.wav").use { fd -> pool.load(fd, 1) } }
    }

    // the last tap or button sound and when it started: a verdict in the same touch stops it (a tap or a button
    // decided at once, Skontrolovať or an option that is judged: only the verdict is heard)
    private var lastClick = 0
    /** The last streams started (a stopped or finished one is ignored by SoundPool), for the end sounds to cut. */
    private val recent = ArrayDeque<Int>()
    private var clickAt = 0L

    override fun play(s: Sfx) {
        if (audio?.ringerMode == AudioManager.RINGER_MODE_SILENT) { log(s, "silent mode"); return }
        when (s) {
            // two clicks in a row never pile up: the new one stops the one before
            Sfx.Tap, Sfx.Button -> { if (lastClick != 0) pool.stop(lastClick); lastClick = now(s); clickAt = SystemClock.uptimeMillis() }
            // the end of a lesson or a test's result has the stage alone: the last verdict's or button's tail stops
            Sfx.LessonDone, Sfx.Score -> {
                for (st in recent) pool.stop(st)
                if (recent.isNotEmpty()) log(s, "stops ${recent.size} sound(s) still playing")
                recent.clear(); lastClick = 0
                now(s)
            }
            else -> {
                if (lastClick != 0 && SystemClock.uptimeMillis() - clickAt < 80) { pool.stop(lastClick); log(s, "stops the click of the same touch") }
                lastClick = 0
                now(s)
            }
        }
    }

    /** The media volume is at 0: nothing would be heard (Nastavenia say so under "Zvuky"). */
    fun muted(): Boolean = audio?.getStreamVolume(AudioManager.STREAM_MUSIC) == 0

    private val headphoneTypes = setOf(
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_BLE_HEADSET,
    )

    private fun headphones(): Boolean = audio?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.any { it.type in headphoneTypes } == true

    private fun now(s: Sfx): Int {
        val id = ids[s] ?: return 0
        if (synchronized(loaded) { id !in loaded }) { log(s, "not loaded yet"); return 0 }
        val v = s.gain * if (headphones()) .7f else 1f
        val stream = pool.play(id, v, v, 1, 0, 1f)
        log(s, if (stream != 0) "stream $stream" else "not played")
        if (stream != 0) { recent.addLast(stream); while (recent.size > 8) recent.removeFirst() }
        return stream
    }

    private fun log(s: Sfx, what: String) {
        if (dev.hamper.tabula.Env.DEBUG || Log.isLoggable(TAG, Log.DEBUG)) Log.i(TAG, "${s.file}: $what at ${SystemClock.uptimeMillis()}")
    }

    private companion object { const val TAG = "FeelSound" }
}

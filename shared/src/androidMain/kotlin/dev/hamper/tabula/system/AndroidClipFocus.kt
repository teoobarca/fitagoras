package dev.hamper.tabula.system

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/** The platform side: AudioManager with a transient request for spoken media. */
class AndroidClipFocus(context: Context, private val onPause: () -> Unit, private val onResume: () -> Unit) : ClipFocus.Port {
    private val audio = context.getSystemService(AudioManager::class.java)
    lateinit var focus: ClipFocus
    override fun attach(focus: ClipFocus) { this.focus = focus }
    private val req: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
        .setWillPauseWhenDucked(true)
        .setOnAudioFocusChangeListener({ c -> log("change ${name(c)}"); focus.changed(c) }, Handler(Looper.getMainLooper()))
        .build()

    override fun request(): Boolean = (audio.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED).also { log("request GAIN_TRANSIENT (media, speech) -> ${if (it) "granted" else "denied"}") }
    override fun abandon() { audio.abandonAudioFocusRequest(req); log("abandon") }
    override fun pause() { log("pause clip"); onPause() }
    override fun resume() { log("resume clip"); onResume() }

    private fun log(s: String) { if (dev.hamper.tabula.Env.DEBUG) Log.i("ClipFocus", s) }
    private fun name(c: Int) = when (c) {
        AudioManager.AUDIOFOCUS_GAIN -> "GAIN"; AudioManager.AUDIOFOCUS_LOSS -> "LOSS"
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "LOSS_TRANSIENT"; AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "LOSS_TRANSIENT_CAN_DUCK"
        else -> "$c"
    }
}

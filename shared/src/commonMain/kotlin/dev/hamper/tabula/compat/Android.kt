package dev.hamper.tabula.compat

import androidx.compose.runtime.Composable

/*
 * The few Android framework calls the common UI makes, under their Android names (common code imports them
 * from here). On Android each one is the framework call itself, with the same arguments; on the desktop and the web
 * they do nothing or the nearest equivalent.
 */

/**
 * android.os.SystemClock: milliseconds that keep counting while the device sleeps, for the mock timer. Android: since
 * boot (it survives clock changes); the desktop and the web: the wall clock (skikoMain, it survives a reload).
 */
expect object SystemClock {
    fun elapsedRealtime(): Long
}

/** The part of android.view.View the UI uses: haptic feedback and keeping the screen on during a mock test. */
interface View {
    fun performHapticFeedback(feedbackConstant: Int): Boolean
    var keepScreenOn: Boolean
}

/** androidx.compose.ui.platform.LocalView, narrowed to [View]. */
object LocalView {
    val current: View @Composable get() = platformView()
}

@Composable
internal expect fun platformView(): View

/** android.view.HapticFeedbackConstants: on Android the framework's own values. */
expect object HapticFeedbackConstants {
    val SEGMENT_FREQUENT_TICK: Int
    val SEGMENT_TICK: Int
    val CLOCK_TICK: Int
    val KEYBOARD_TAP: Int
    val KEYBOARD_PRESS: Int
    val VIRTUAL_KEY: Int
    val LONG_PRESS: Int
    val CONTEXT_CLICK: Int
    val CONFIRM: Int
    val REJECT: Int
    val TOGGLE_ON: Int
    val TOGGLE_OFF: Int
    val GESTURE_START: Int
    val GESTURE_END: Int
    val GESTURE_THRESHOLD_ACTIVATE: Int
    val GESTURE_THRESHOLD_DEACTIVATE: Int
    val DRAG_START: Int
}

/** androidx.activity.compose.BackHandler on Android; nothing elsewhere (no system back). */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

/** android.net.Uri.encode: percent-encodes everything but letters, digits and _-!.~'()*. */
object Uri {
    private const val Allowed = "_-!.~'()*"
    fun encode(s: String): String = buildString {
        for (b in s.encodeToByteArray()) {
            val c = (b.toInt() and 0xFF).toChar()
            if (c.code < 0x80 && (c.isLetterOrDigit() || c in Allowed)) append(c)
            else append('%').append(((b.toInt() and 0xFF) shr 4).toString(16).uppercase()).append((b.toInt() and 0xF).toString(16).uppercase())
        }
    }
}

/** The part of android.content.res.Configuration the UI reads: the orientation (Android's values). */
class Configuration(val orientation: Int) {
    companion object {
        const val ORIENTATION_PORTRAIT = 1
        const val ORIENTATION_LANDSCAPE = 2
    }
}

/** androidx.compose.ui.platform.LocalConfiguration, narrowed to [Configuration]. */
object LocalConfiguration {
    val current: Configuration @Composable get() = platformConfiguration()
}

@Composable
internal expect fun platformConfiguration(): Configuration

// android.os.VibrationEffect.Composition primitives (the framework's values; ui/Feel.kt composes them)
const val PRIMITIVE_CLICK = 1
const val PRIMITIVE_THUD = 2
const val PRIMITIVE_SPIN = 3
const val PRIMITIVE_QUICK_RISE = 4
const val PRIMITIVE_SLOW_RISE = 5
const val PRIMITIVE_QUICK_FALL = 6
const val PRIMITIVE_TICK = 7
const val PRIMITIVE_LOW_TICK = 8

/** android.media.AudioManager's focus constants (the framework's values; system/ClipFocus.kt reads them). */
object AudioManager {
    const val AUDIOFOCUS_GAIN = 1
    const val AUDIOFOCUS_GAIN_TRANSIENT = 2
    const val AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3
    const val AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE = 4
    const val AUDIOFOCUS_LOSS = -1
    const val AUDIOFOCUS_LOSS_TRANSIENT = -2
    const val AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3
    const val AUDIOFOCUS_REQUEST_FAILED = 0
    const val AUDIOFOCUS_REQUEST_GRANTED = 1
    const val AUDIOFOCUS_REQUEST_DELAYED = 2
}

/** Whether TalkBack (touch exploration) is on: AccessibilityManager on Android, off elsewhere. */
@Composable
expect fun rememberTouchExploration(): Boolean

/** androidx.compose.ui.window.DialogProperties with Android's options; the desktop and the web have no system bars to fit. */
expect fun DialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = true,
    decorFitsSystemWindows: Boolean = true,
): androidx.compose.ui.window.DialogProperties

/** Inside a full-screen dialog drawn under the system bars: their icons dark on a light page, light on a dark one
 *  (Android sets the dialog window's own bars; the desktop and the web have none). */
@Composable
expect fun DialogBars(light: Boolean)

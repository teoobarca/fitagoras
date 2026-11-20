package dev.hamper.tabula.compat

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * The wall clock, not the monotonic one: the timed test's end must hold across a reload of the web page (the browser's
 * monotonic clock starts at 0 with every page) and while the device sleeps. Its only use is that clock (screens/Papers.kt).
 */
actual object SystemClock {
    actual fun elapsedRealtime(): Long = currentTimeMillis()
}

private object NoView : View {
    override fun performHapticFeedback(feedbackConstant: Int) = false
    override var keepScreenOn = false
}

@Composable
internal actual fun platformView(): View = NoView

/** Android's values, so a log line reads the same; nothing vibrates here. */
actual object HapticFeedbackConstants {
    actual val SEGMENT_FREQUENT_TICK: Int = 27
    actual val SEGMENT_TICK: Int = 26
    actual val CLOCK_TICK: Int = 4
    actual val KEYBOARD_TAP: Int = 3
    actual val KEYBOARD_PRESS: Int = 3
    actual val VIRTUAL_KEY: Int = 1
    actual val LONG_PRESS: Int = 0
    actual val CONTEXT_CLICK: Int = 6
    actual val CONFIRM: Int = 16
    actual val REJECT: Int = 17
    actual val TOGGLE_ON: Int = 21
    actual val TOGGLE_OFF: Int = 22
    actual val GESTURE_START: Int = 12
    actual val GESTURE_END: Int = 13
    actual val GESTURE_THRESHOLD_ACTIVATE: Int = 23
    actual val GESTURE_THRESHOLD_DEACTIVATE: Int = 24
    actual val DRAG_START: Int = 25
}

/**
 * The system back of the desktop and the web, as Android's dispatcher works: the newest enabled handler answers.
 * Nothing presses it in the app yet (Esc could); tests press it to walk back through a lesson without an emulator.
 */
object SystemBack {
    private class Handler(val enabled: () -> Boolean, val onBack: () -> Unit)
    private val handlers = mutableListOf<Handler>()
    /** One press; false when no handler took it (on Android the activity would close). */
    fun press(): Boolean {
        val h = handlers.lastOrNull { it.enabled() } ?: return false
        h.onBack(); return true
    }
    @Composable
    internal fun Register(enabled: Boolean, onBack: () -> Unit) {
        val on = androidx.compose.runtime.rememberUpdatedState(enabled)
        val act = androidx.compose.runtime.rememberUpdatedState(onBack)
        androidx.compose.runtime.DisposableEffect(Unit) {
            val h = Handler({ on.value }, { act.value() })
            handlers += h
            onDispose { handlers -= h }
        }
    }
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = SystemBack.Register(enabled, onBack)

/** Locked like android.util.LruCache: the formula caches are read from whichever thread lays out or draws. */
actual class LruCache<K, V> actual constructor(private val maxSize: Int) {
    private val map = LinkedHashMap<K, V>()
    actual fun get(key: K): V? = synchronized(map) {
        val v = map.remove(key) ?: return null
        map[key] = v
        v
    }
    actual fun put(key: K, value: V): V? = synchronized(map) {
        val old = map.remove(key)
        map[key] = value
        while (map.size > maxSize) map.remove(map.keys.first())
        old
    }
}

/** Landscape when the window is wider than tall (a phone-sized browser tab or desktop window). */
@Composable
internal actual fun platformConfiguration(): Configuration = LocalWindowInfo.current.containerSize.let {
    Configuration(if (it.width > it.height) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT)
}

@Composable
actual fun rememberTouchExploration(): Boolean = false

actual fun DialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean,
): androidx.compose.ui.window.DialogProperties = androidx.compose.ui.window.DialogProperties(
    dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = dismissOnClickOutside, usePlatformDefaultWidth = usePlatformDefaultWidth,
)

@Composable
actual fun DialogBars(light: Boolean) {}

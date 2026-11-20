package dev.hamper.tabula.compat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual object SystemClock {
    actual fun elapsedRealtime(): Long = android.os.SystemClock.elapsedRealtime()
}

internal class AndroidView(val v: android.view.View) : View {
    override fun performHapticFeedback(feedbackConstant: Int): Boolean = v.performHapticFeedback(feedbackConstant)
    override var keepScreenOn: Boolean
        get() = v.keepScreenOn
        set(on) { v.keepScreenOn = on }
}

@Composable
internal actual fun platformView(): View {
    val v = androidx.compose.ui.platform.LocalView.current
    return remember(v) { AndroidView(v) }
}

actual object HapticFeedbackConstants {
    actual val SEGMENT_FREQUENT_TICK: Int = android.view.HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
    actual val SEGMENT_TICK: Int = android.view.HapticFeedbackConstants.SEGMENT_TICK
    actual val CLOCK_TICK: Int = android.view.HapticFeedbackConstants.CLOCK_TICK
    actual val KEYBOARD_TAP: Int = android.view.HapticFeedbackConstants.KEYBOARD_TAP
    actual val KEYBOARD_PRESS: Int = android.view.HapticFeedbackConstants.KEYBOARD_PRESS
    actual val VIRTUAL_KEY: Int = android.view.HapticFeedbackConstants.VIRTUAL_KEY
    actual val LONG_PRESS: Int = android.view.HapticFeedbackConstants.LONG_PRESS
    actual val CONTEXT_CLICK: Int = android.view.HapticFeedbackConstants.CONTEXT_CLICK
    actual val CONFIRM: Int = android.view.HapticFeedbackConstants.CONFIRM
    actual val REJECT: Int = android.view.HapticFeedbackConstants.REJECT
    actual val TOGGLE_ON: Int = android.view.HapticFeedbackConstants.TOGGLE_ON
    actual val TOGGLE_OFF: Int = android.view.HapticFeedbackConstants.TOGGLE_OFF
    actual val GESTURE_START: Int = android.view.HapticFeedbackConstants.GESTURE_START
    actual val GESTURE_END: Int = android.view.HapticFeedbackConstants.GESTURE_END
    actual val GESTURE_THRESHOLD_ACTIVATE: Int = android.view.HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
    actual val GESTURE_THRESHOLD_DEACTIVATE: Int = android.view.HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE
    actual val DRAG_START: Int = android.view.HapticFeedbackConstants.DRAG_START
}

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) = androidx.activity.compose.BackHandler(enabled, onBack)

actual typealias LruCache<K, V> = android.util.LruCache<K, V>

@Composable
internal actual fun platformConfiguration(): Configuration = Configuration(androidx.compose.ui.platform.LocalConfiguration.current.orientation)

@Composable
actual fun rememberTouchExploration(): Boolean {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return remember { ctx.getSystemService(android.view.accessibility.AccessibilityManager::class.java)?.isTouchExplorationEnabled == true }
}

actual fun DialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    decorFitsSystemWindows: Boolean,
): androidx.compose.ui.window.DialogProperties = androidx.compose.ui.window.DialogProperties(
    dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth, decorFitsSystemWindows = decorFitsSystemWindows,
)

@Composable
actual fun DialogBars(light: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window ?: return
    androidx.compose.runtime.SideEffect {
        androidx.core.view.WindowCompat.getInsetsController(window, view).apply { isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light }
    }
}

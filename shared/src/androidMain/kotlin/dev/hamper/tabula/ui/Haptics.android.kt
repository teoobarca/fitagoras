package dev.hamper.tabula.ui

import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.hamper.tabula.compat.AndroidView
import dev.hamper.tabula.compat.View

private const val TAG = "FeelHaptic"

/** Whether this phone plays the rich patterns; asked once. */
private var richCache: Boolean? = null

private fun rich(v: Vibrator?): Boolean = richCache ?: (v != null && v.hasVibrator() && v.areAllPrimitivesSupported(*Buzz.primitives)).also { richCache = it }

actual class Haptics actual constructor(private val view: View, actual val enabled: () -> Boolean) {
    private val vibrator: Vibrator? = (view as AndroidView).v.context.getSystemService(VibratorManager::class.java)?.defaultVibrator

    actual fun play(b: Buzz) {
        // the moment, for the sound log (FeelSound: `adb shell setprop log.tag.FeelSound DEBUG` in any build)
        if (b != Buzz.Tick && (dev.hamper.tabula.Env.DEBUG || Log.isLoggable("FeelSound", Log.DEBUG))) Log.i("FeelSound", "event ${b.name} at ${android.os.SystemClock.uptimeMillis()}")
        Sounds.forBuzz(b) // its own switch ("Zvuky"), independent of "Haptika"
        val debug = dev.hamper.tabula.Env.DEBUG && b != Buzz.Tick
        if (!enabled()) { if (debug) Log.i(TAG, "${b.name}: off (Haptika)"); return }
        val r = rich(vibrator)
        if (r && b.rich.isNotEmpty()) {
            val comp = VibrationEffect.startComposition()
            b.rich.forEach { comp.addPrimitive(it.id, it.scale, it.delay) }
            vibrator!!.vibrate(comp.compose(), touch)
        } else view.performHapticFeedback(b.fallback)
        if (debug) Log.i(TAG, "${b.name}: ${b.describe(r)}" + if (!r && b.rich.isNotEmpty()) "  [on Pixel: ${b.describe(true)}]" else "")
    }

    actual fun tap() = play(Buzz.Tap)
    actual fun key() = play(Buzz.Key)
    actual fun tick() = play(Buzz.Tick)
    actual fun right() = play(Series.right())
    actual fun wrong() { Series.wrong(); play(Buzz.Wrong) }
    actual fun cell() = play(Buzz.Cell)
    actual fun expand() = play(Buzz.Expand)
    actual fun land() = play(Buzz.Land)
    actual fun finish() = play(Buzz.Finish)

    private companion object { val touch: VibrationAttributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH) }
}

@Composable
actual fun calm(): Boolean {
    val ctx = LocalContext.current
    return remember { Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

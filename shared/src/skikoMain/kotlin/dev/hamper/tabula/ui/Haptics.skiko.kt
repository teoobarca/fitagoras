package dev.hamper.tabula.ui

import androidx.compose.runtime.Composable
import dev.hamper.tabula.compat.View

/**
 * Nothing vibrates on the desktop or the web; which pattern would play is handed to [HapticTrace] (the JVM tests
 * check which event plays which pattern, the feel itself is checked on a phone).
 */
actual class Haptics actual constructor(private val view: View, actual val enabled: () -> Boolean) {
    actual fun play(b: Buzz) { Sounds.forBuzz(b); if (enabled()) HapticTrace.sink?.invoke(b) }
    actual fun tap() = play(Buzz.Tap)
    actual fun key() = play(Buzz.Key)
    actual fun tick() = play(Buzz.Tick)
    actual fun right() = play(Series.right())
    actual fun wrong() { Series.wrong(); play(Buzz.Wrong) }
    actual fun cell() = play(Buzz.Cell)
    actual fun expand() = play(Buzz.Expand)
    actual fun land() = play(Buzz.Land)
    actual fun finish() = play(Buzz.Finish)
}

@Composable
actual fun calm(): Boolean = false

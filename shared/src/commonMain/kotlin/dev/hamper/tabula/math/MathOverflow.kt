package dev.hamper.tabula.math

import androidx.compose.ui.unit.Constraints
import kotlin.concurrent.Volatile

/**
 * A formula drawn wider than the room it was given would run past the edge of its card. Off in the app; the overflow
 * scan (snap/OverflowScan.kt) turns it on, renders every lesson step at 320 dp and 200 % and fails on what it records.
 */
object MathOverflow {
    @Volatile var on = false
    val found: MutableList<String> = dev.hamper.tabula.compat.synchronizedList(ArrayList())

    /**
     * Display formulas that did not fit their room at full size, so they were shrunk, broken into rows or scrolled
     * (Teo, 16. 11.: a formula of a statement must not break on a 320 dp phone; restructure it, do not shrink it).
     */
    val squeezed: MutableList<String> = dev.hamper.tabula.compat.synchronizedList(ArrayList())

    /** Records a display formula [tex] that is [width] px wide in [room] px, when it does not fit. */
    fun fit(tex: String, width: Float, room: Float) {
        if (on && width > room + 1.5f) squeezed += "${width.toInt()} px in ${room.toInt()} px: $tex"
    }

    /** Records [tex] when [width] (px) is wider than [c] allows, beyond a pixel of rounding. */
    fun check(tex: String, width: Float, c: Constraints) {
        if (on && c.hasBoundedWidth && width > c.maxWidth + 1.5f) found += "${width.toInt()} px in ${c.maxWidth} px: $tex"
    }
}

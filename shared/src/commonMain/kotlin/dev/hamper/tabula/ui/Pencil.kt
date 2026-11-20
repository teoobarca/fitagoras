package dev.hamper.tabula.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import kotlin.math.sin

/**
 * A line as a hand draws it with a pencil: the path resampled every [step] and pushed off its course by a small,
 * smooth, repeatable wobble ([amp] at most; [seed] makes each line its own). The same wobble draws the notebook
 * pictures (screens/Art.kt) and Pytagoras' outline (Mascot.kt), so the app has one hand.
 */
fun wobble(path: Path, amp: Float, seed: Int, step: Float = 1.6f): Path {
    val m = PathMeasure()
    m.setPath(path, false)
    val len = m.length
    if (len <= 0f) return path
    val out = Path()
    val n = (len / step).toInt().coerceAtLeast(2)
    val s = seed * 1.37f
    for (i in 0..n) {
        val d = len * i / n
        val p = m.getPosition(d)
        val t = m.getTangent(d)
        // a slow sway and a quicker tremor, both fading at the ends (a stroke starts where the pencil lands)
        val u = d / len
        val ends = (minOf(u, 1f - u) * 8f).coerceIn(0f, 1f)
        val off = amp * ends * (sin(d * .21f + s) * .7f + sin(d * .67f + s * 2.1f) * .3f)
        val q = Offset(p.x - t.y * off, p.y + t.x * off)
        if (i == 0) out.moveTo(q.x, q.y) else out.lineTo(q.x, q.y)
    }
    return out
}

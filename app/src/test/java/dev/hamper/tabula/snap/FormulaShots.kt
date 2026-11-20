package dev.hamper.tabula.snap

import androidx.compose.runtime.CompositionLocalProvider
import dev.hamper.tabula.FormulaProbe
import dev.hamper.tabula.FormulaSheet
import dev.hamper.tabula.SkriptaFormulas
import dev.hamper.tabula.math.LocalMathFace
import org.junit.Test

/**
 * Each specimen formula, for the overlay with the MathJax goldens: `-Psnap.test=FormulaShots -Psnap.height=2800
 * -Psnap.width=480`. `-Psnap.only=0,.01` renders the app's face with those stroke boosts instead (files b0-…, b.01-…).
 */
class FormulaShots : Shots() {
    @Test fun formulas() {
        val boosts = Snap.get("only")?.split(',')?.map { it.trim() }
        for (b in boosts ?: listOf(null)) {
            val name = if (b == null) "app" else "b$b"
            val face = if (b == null) dev.hamper.tabula.ui.Math else dev.hamper.tabula.ui.Math.copy(boost = b.toFloat())
            // one tall capture with every formula; single captures flake after the first in a JVM
            steady("$name-all") { CompositionLocalProvider(LocalMathFace provides face) { FormulaSheet() } }
            val only = Snap.get("route") ?: continue
            for ((id, _, _) in SkriptaFormulas.filter { it.first.startsWith(only) }) steady("$name-$id") { CompositionLocalProvider(LocalMathFace provides face) { FormulaProbe(id) } }
        }
    }
}

/**
 * [shoot] with resume: a shot whose files are there and show content is skipped; an empty capture fails the run at once,
 * because after one the test JVM keeps capturing empty windows (tools/snap_inner.sh starts a fresh JVM and resumes).
 */
internal fun steady(name: String, content: @androidx.compose.runtime.Composable () -> Unit) {
    val files = Snap.looks().map { java.io.File(Snap.out, "$name-${it.tag}.png") }
    if (files.all { it.exists() && !empty(it) }) return
    try { shoot(name, Snap.looks(), Snap.out, content) } catch (e: AssertionError) { }
    val bad = files.filter { !it.exists() || empty(it) }
    if (bad.isNotEmpty()) { bad.forEach { it.delete() }; throw AssertionError("snap: $name came out empty, resume in a fresh JVM") }
}

/** The window's own grey or one colour over the top of the screen, where the content sits: nothing was drawn. */
private fun empty(f: java.io.File): Boolean {
    val img = javax.imageio.ImageIO.read(f) ?: return true
    val first = img.getRGB(0, 0)
    for (y in 0 until minOf(img.height, 600) step 3) for (x in 0 until img.width step 3) if (img.getRGB(x, y) != first) return false
    return true
}

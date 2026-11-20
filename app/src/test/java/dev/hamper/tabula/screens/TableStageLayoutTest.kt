package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.allFormulas
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Euclid and the REA as tables (Teo, 16. 11., body 32 a 34): on a 320 dp phone nothing wraps and nothing scrolls
 * sideways, and through every step the table stays exactly where it is (real layout, Robolectric, native text).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w320dp-h700dp-xxhdpi")
class TableStageLayoutTest {
    @get:Rule val rule = createComposeRule()

    @Test fun tablesFit320dpAndNeverMove() = fitAndStay(1f)

    /** Teo (17. 11.): also at 200 % text, nothing wraps and nothing scrolls on a 320 dp phone. */
    @Test fun tablesFit320dpAt200PercentText() = fitAndStay(2f)

    private fun fitAndStay(fontScale: Float) {
        val tables = allFormulas(seeds = 4).mapNotNull { (w, f) -> f.table?.let { w to it } }
        assertTrue("tables found: ${tables.size}", tables.size >= 8)
        var t by mutableStateOf(tables.first().second)
        // a goal card on a 320 dp phone, its content indented after the check: about 230 dp
        rule.setContent {
            val d = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(d.density, fontScale)) {
                TabulaTheme(ThemeMode.Light) { Box(Modifier.width(230.dp)) { key(t) { TableWalkView(t) } } }
            }
        }
        for ((where, w) in tables) {
            t = w; rule.waitForIdle()
            val grid = rule.onNodeWithTag("table-grid", useUnmergedTree = true).getBoundsInRoot()
            val ruleBox = rule.onNodeWithTag("table-rule", useUnmergedTree = true).getBoundsInRoot()
            assertTrue("$where at ${fontScale}x: ${grid.right - grid.left} wide in 230 dp", grid.right <= 230.dp && grid.left >= 0.dp)
            assertTrue("$where at ${fontScale}x: the rule above is ${ruleBox.right - ruleBox.left} wide in 230 dp", ruleBox.right <= 230.dp && ruleBox.left >= 0.dp)
            // the bounds above are clipped by the sideways scroll: the canvases themselves must fit too
            val room = with(rule.density) { 230.dp.roundToPx() }
            for (tag in listOf("table-grid", "table-rule")) {
                val w = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().size.width
                assertTrue("$where at ${fontScale}x: $tag is $w px for $room px", w <= room)
            }
            repeat(tableStageSteps(w)) { s ->
                rule.onNodeWithTag("table-next", useUnmergedTree = true).performClick(); rule.waitForIdle()
                assertEquals("$where after ${s + 1} steps: the table moved", grid, rule.onNodeWithTag("table-grid", useUnmergedTree = true).getBoundsInRoot())
                assertEquals("$where after ${s + 1} steps: the rule above moved", ruleBox, rule.onNodeWithTag("table-rule", useUnmergedTree = true).getBoundsInRoot())
                // the counter under the table stays on one line
                val lines = ArrayList<androidx.compose.ui.text.TextLayoutResult>()
                rule.onNodeWithTag("table-counter", useUnmergedTree = true).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(lines)
                assertEquals("$where at ${fontScale}x: the counter wraps", 1, lines.first().lineCount)
                // the step really moved (a squeezed arrow would leave it at the start and pass everything above)
                val total = tableStageSteps(w)
                val want = if (s + 1 == total && w.result.isNotEmpty()) "Výsledok" else "${s + 1} z $total"
                assertEquals("$where at ${fontScale}x", want, lines.first().layoutInput.text.text)
                for (tag in listOf("table-prev", "table-next")) {
                    val b = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot()
                    assertTrue("$where at ${fontScale}x: $tag is ${b.right - b.left} wide", b.right - b.left >= 39.dp && b.right <= 230.dp)
                }
            }
        }
    }
}

package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.math.MathFonts
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * The answer steps the emulator walker could not finish (its taps landed while the list still scrolled), driven through
 * the real composables: the right picks pass, one left out gives "Skúsiť znova". Picking options (dml-mz-mnoziny, lesson
 * "Množiny a operácie"), many seeds.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class AnswerFlowTest {
    @get:Rule val rule = createComposeRule()

    private fun descContains(s: String) = SemanticsMatcher("contentDescription contains $s") { n ->
        n.config.getOrElseNullable(SemanticsProperties.ContentDescription) { null }?.any { s in it } == true
    }

    private fun all(m: SemanticsMatcher): List<SemanticsNodeInteraction> {
        val n = rule.onAllNodes(m, useUnmergedTree = true).fetchSemanticsNodes().size
        return (0 until n).map { rule.onAllNodes(m, useUnmergedTree = true)[it] }
    }

    private fun tryAgainShown() = rule.onAllNodesWithText("Skúsiť znova").fetchSemanticsNodes().isNotEmpty()

    private fun run(gen: String, seeds: IntRange, mistake: Boolean) {
        var seed by mutableStateOf(seeds.first)
        rule.setContent {
            TabulaTheme(ThemeMode.Light) {
                Box(Modifier.fillMaxSize()) {
                    androidx.compose.runtime.key(seed) {
                        ExerciseView(Generators.getValue(gen)(Random(seed)), solo = true, frame = Frame(1, 1, {}, {})) { _, _ -> }
                    }
                }
            }
        }
        for (s in seeds) {
            seed = s; rule.waitForIdle()
            val right = all(descContains("dbg:pick"))
            assertTrue("$gen/$s: no marked right answer", right.isNotEmpty())
            // one right option left out is a wrong answer (or a disabled check when fewer than asked)
            (if (mistake) right.drop(1) else right).forEach { it.performScrollTo(); it.performClick() }
            rule.waitForIdle()
            val check = rule.onNodeWithText("Skontrolovať")
            if (mistake && check.fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)) continue
            check.performClick(); rule.waitForIdle()
            if (mistake) assertTrue("$gen/$s: a wrong answer must give Skúsiť znova", tryAgainShown())
            else assertFalse("$gen/$s: the right answer gave Skúsiť znova", tryAgainShown())
        }
    }

    @Test fun set_picks_right_pass() = run("dml-mz-mnoziny", 0..11, mistake = false)
    @Test fun set_picks_missing_one_fails() = run("dml-mz-mnoziny", 0..5, mistake = true)
}

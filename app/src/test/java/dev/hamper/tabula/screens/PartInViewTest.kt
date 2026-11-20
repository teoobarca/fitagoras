package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.math.MathFonts
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * After "Ďalšia časť" the new part is on screen: below the open "Ako na to" card (the first try of a type) the number
 * fields of the next part stood under the keypad with nothing in sight (gate 1.3.4, Frobenius, step 17 of 19).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class PartInViewTest {
    @get:Rule val rule = createComposeRule()

    private fun desc(f: (String) -> Boolean) = SemanticsMatcher("contentDescription") { n ->
        n.config.getOrElseNullable(SemanticsProperties.ContentDescription) { null }?.any(f) == true
    }

    @Test fun next_part_comes_into_view() {
        dev.hamper.tabula.platform.AndroidContext.install(ApplicationProvider.getApplicationContext())
        var seed by mutableStateOf(0)
        rule.setContent {
            TabulaTheme(ThemeMode.Light) {
                Box(Modifier.fillMaxSize()) {
                    androidx.compose.runtime.key(seed) {
                        ExerciseView(Generators.getValue("la1-k4-solve")(Random(seed)), solo = false, frame = Frame(1, 1, {}, {}), firstTime = true) { _, _ -> }
                    }
                }
            }
        }
        var checked = 0
        for (s in 0..7) {
            seed = s; rule.waitForIdle()
            rule.onNode(desc { it == "dbg:correct" }, useUnmergedTree = true).performClick(); rule.waitForIdle()
            val next = rule.onAllNodesWithText("Ďalšia časť")
            if (next.fetchSemanticsNodes().isEmpty()) continue // no solution: the exercise ends after the first part
            next[0].performClick()
            rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()
            rule.onAllNodes(desc { it.startsWith("dbg:field0=") }, useUnmergedTree = true)[0].assertIsDisplayed()
            checked++
        }
        assertTrue("no seed reached a second part", checked > 0)
    }
}

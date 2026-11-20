package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.data.allGemWalks
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.math.MathFonts
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
 * Teo's report (16. 11.): between GEM steps the matrix jumped, so he could not see what changed. The stage of a walk is
 * laid out from all its matrices at once; this measures the real layout (Robolectric, native text) on every step of
 * every GEM walk the app can show and fails if the matrix or the operation slot moves or resizes by a single pixel.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class GemStageLayoutTest {
    @get:Rule val rule = createComposeRule()

    private fun box(tag: String): DpRect = rule.onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot()

    @Test fun theMatrixNeverMovesBetweenSteps() = everyStep(big = true)
    @Test fun theCompactMatrixNeverMovesBetweenSteps() = everyStep(big = false)

    private fun everyStep(big: Boolean) {
        val walks = allGemWalks(seeds = 3)
        assertTrue(walks.size > 40)
        var walk by mutableStateOf(walks.first().second)
        var k by mutableIntStateOf(0)
        rule.setContent { TabulaTheme(ThemeMode.Light) { Box(Modifier.width(411.dp)) { GemStage(walk, k, big = big) } } }
        for ((where, w) in walks) {
            walk = w; k = 0; rule.waitForIdle()
            val m0 = box("gem-matrix"); val o0 = box("gem-ops")
            assertTrue("$where: empty matrix", m0.right > m0.left && m0.bottom > m0.top)
            for (s in 1..w.size) {
                k = s; rule.waitForIdle()
                assertEquals("$where, step $s of ${w.size} (big=$big): the matrix moved", m0, box("gem-matrix"))
                assertEquals("$where, step $s (big=$big): the operation slot moved", o0, box("gem-ops"))
            }
        }
    }

    /**
     * The lesson picture as the student uses it (the "Krokuj GEM" steps): its own caption, counter and buttons around
     * the stage. Fails if the picture centres the matrix again or puts anything that changes above it.
     */
    @Test fun lessonPictureKeepsTheMatrixInPlace() {
        val pics = dev.hamper.tabula.data.AllLessons.flatMap { l -> l.steps.filterIsInstance<dev.hamper.tabula.data.Step.Explore>().mapNotNull { s -> (s.pic as? dev.hamper.tabula.data.Pic.Gem)?.let { "${l.id}/${s.id}" to it } } }
        assertTrue(pics.size >= 5)
        var pic by mutableStateOf(pics.first().second)
        rule.setContent { TabulaTheme(ThemeMode.Light) { Box(Modifier.width(379.dp).height(560.dp)) { androidx.compose.runtime.key(pic) { PicView(pic, Modifier.fillMaxSize()) } } } }
        for ((where, p) in pics) {
            pic = p; rule.waitForIdle()
            val m0 = box("gem-matrix")
            var s = 0
            while (rule.onAllNodesWithText("Ďalší krok").fetchSemanticsNodes().isNotEmpty() && s < 12) {
                rule.onNodeWithText("Ďalší krok").performClick(); rule.waitForIdle(); s++
                assertEquals("$where after $s steps: the matrix moved", m0, box("gem-matrix"))
            }
            assertTrue("$where: no steps walked", s > 0)
        }
    }

    /** The same through the component's own buttons, in the compact form used inside solutions. */
    @Test fun stepButtonsKeepTheMatrixInPlace() {
        val walks: List<Pair<String, GemWalk>> = allGemWalks(seeds = 2).filter { it.second.size > 0 }
        var walk by mutableStateOf(walks.first().second)
        rule.setContent { TabulaTheme(ThemeMode.Dark) { Box(Modifier.width(360.dp)) { androidx.compose.runtime.key(walk) { GemWalkView(walk) } } } }
        for ((where, w) in walks) {
            walk = w; rule.waitForIdle()
            val m0 = box("gem-matrix")
            repeat(w.size) { s ->
                rule.onNodeWithTag("gem-next", useUnmergedTree = true).performClick(); rule.waitForIdle()
                assertEquals("$where after ${s + 1} clicks", m0, box("gem-matrix"))
            }
            repeat(w.size) { rule.onNodeWithTag("gem-prev", useUnmergedTree = true).performClick() }
            rule.waitForIdle()
            assertEquals("$where back at the start", m0, box("gem-matrix"))
        }
    }
}

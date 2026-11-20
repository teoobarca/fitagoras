package dev.hamper.tabula.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.media.SilentPlayer
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.LocalHapticsOn
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A proof in parts on a phone-sized window: after every wrong pick the picked option stands above the "Prečo nie" panel.
 * Found on the emulator (19. 11., DML "Lineárne kongruencie", the proof of the cancelling theorem, part 2): the panel
 * covered a long option low in the part, only its red edge was in sight.
 */
@OptIn(ExperimentalTestApi::class)
class ProofPanelUiTest {
    private fun ComposeUiTest.node(m: SemanticsMatcher): SemanticsNode = onAllNodes(m).fetchSemanticsNodes().first()

    @Test fun wrongPickStaysAbovePanel() = runDesktopComposeUiTest(width = 411, height = 891) {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets(); ClipLibrary.load()
        val l = LessonById.getValue("dml-tc-lkon")
        val at = l.steps.indexOfFirst { it is dev.hamper.tabula.data.Step.Prove && it.proof == "dml-tc-p-kraceni" }
        val store = Store(MemoryStateFile())
        store.restartLesson(l, at)
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalHapticsOn provides { false }) {
            LessonFlow(l, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}, {}) {}
        } } }
        waitForIdle()
        val correct = hasContentDescription("dbg:correct")
        var checked = 0
        var rounds = 0
        while (onAllNodes(correct).fetchSemanticsNodes().isNotEmpty() && rounds++ < 6) {
            val c = node(correct).boundsInRoot
            // the other options of the part: clickable, in the same column as the right one
            val others = onAllNodes(hasClickAction()).fetchSemanticsNodes().filter { n ->
                val b = n.boundsInRoot
                kotlin.math.abs(b.left - c.left) < 2f && kotlin.math.abs(b.right - c.right) < 2f && b != c &&
                    // an option is its text only (not the pinned statement above the parts, which says what it does)
                    n.config.getOrNull(SemanticsProperties.ContentDescription) == null
            }.map { it.boundsInRoot.top }
            for (top in others) {
                val opt = SemanticsMatcher("option at $top") { n -> n.config.contains(SemanticsActions.OnClick) && kotlin.math.abs(n.boundsInRoot.left - c.left) < 2f && kotlin.math.abs(n.boundsInRoot.right - c.right) < 2f }
                // the option whose top was [top] before the pick (the body may scroll after it)
                onAllNodes(opt).fetchSemanticsNodes().indexOfFirst { kotlin.math.abs(it.boundsInRoot.top - top) < 2f }.takeIf { it >= 0 }?.let { onAllNodes(opt)[it].performClick() } ?: continue
                mainClock.advanceTimeBy(1500); waitForIdle()
                if (onAllNodes(hasText("Prečo nie", substring = true)).fetchSemanticsNodes().isEmpty()) continue
                val panel = node(hasText("Prečo nie", substring = true)).boundsInRoot.top
                // the picked option is the one left enabled
                val picked = onAllNodes(opt and SemanticsMatcher("enabled") { n -> !n.config.contains(SemanticsProperties.Disabled) }).fetchSemanticsNodes().first().boundsInRoot
                assertTrue("part $rounds: the picked option (bottom ${picked.bottom}) stands above the panel (top $panel)", picked.bottom <= panel)
                checked++
                onAllNodes(hasText("Skúsiť znova") and hasClickAction()).fetchSemanticsNodes().let { if (it.isNotEmpty()) { onAllNodes(hasText("Skúsiť znova") and hasClickAction())[0].performClick(); waitForIdle() } }
            }
            onAllNodes(correct)[0].performClick(); mainClock.advanceTimeBy(800); waitForIdle()
        }
        assertTrue("wrong picks checked: $checked", checked >= 2)
    }
}

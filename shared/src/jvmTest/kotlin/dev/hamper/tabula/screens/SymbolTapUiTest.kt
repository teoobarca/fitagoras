package dev.hamper.tabula.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.SystemBack
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
 * "Symboly vo vzorci, napríklad φ, sa dajú ťuknúť" under a definition: a finger on that φ opens its sheet. Found on the
 * emulator (19. 11., DML "Veľké mocniny", Definícia 6.10): a tap on φ opened nothing.
 */
@OptIn(ExperimentalTestApi::class)
class SymbolTapUiTest {
    private fun actions(n: SemanticsNode) = n.config.getOrNull(SemanticsActions.CustomActions).orEmpty().map { it.label }

    /** Taps across [n] in a fine grid until the sheet of the symbol read [read] opens; true if it did. */
    private fun ComposeUiTest.scan(m: SemanticsMatcher, index: Int, read: String): Boolean {
        val b = onAllNodes(m, useUnmergedTree = true).fetchSemanticsNodes()[index].boundsInRoot
        fun open() = onAllNodes(hasText(read, substring = true)).fetchSemanticsNodes().size > baseline
        var y = 6f
        while (y < b.height) {
            var x = 3f
            while (x < b.width) {
                onAllNodes(m, useUnmergedTree = true)[index].performTouchInput { click(Offset(x, y)) }
                waitForIdle()
                if (open()) { SystemBack.press(); waitForIdle(); return true }
                x += 6f
            }
            y += 8f
        }
        return false
    }
    private var baseline = 0

    @Test fun advertisedSymbolOpens() = runDesktopComposeUiTest(width = 411, height = 1400) {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets(); ClipLibrary.load()
        val l = LessonById.getValue("dml-tc-mocniny")
        val at = l.steps.indexOfFirst { it is dev.hamper.tabula.data.Step.Def && it.concept == "dml-tc-euler" }
        val store = Store(MemoryStateFile())
        store.restartLesson(l, at)
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalHapticsOn provides { false }) {
            LessonFlow(l, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}, {}) {}
        } } }
        waitForIdle()
        val read = "nesúdeliteľných"
        baseline = onAllNodes(hasText(read, substring = true)).fetchSemanticsNodes().size
        val any = SemanticsMatcher("any") { true }
        val all = onAllNodes(any, useUnmergedTree = true).fetchSemanticsNodes()
        val owners = all.indices.filter { i -> actions(all[i]).any { it.contains("Eulerova funkcia") } }
        assertTrue("some text on the screen owns φ", owners.isNotEmpty())
        assertTrue("a tap on φ in the line opens its sheet", owners.any { scan(any, it, read) })
        // and on φ in the display formula under that line, where the finger goes first
        val line = all[owners.first()].boundsInRoot
        val now = onAllNodes(any, useUnmergedTree = true).fetchSemanticsNodes()
        val display = now.indices.firstOrNull { i ->
            val n = now[i]; val b = n.boundsInRoot
            n.config.getOrNull(SemanticsProperties.ContentDescription) != null && n.config.getOrNull(SemanticsProperties.Text) == null &&
                b.top >= line.bottom - 1f && b.top < line.bottom + 120f && b.width > 100f
        }
        assertTrue("the display formula of the definition", display != null)
        assertTrue("a tap on φ in the display formula opens its sheet", scan(any, display!!, read))
    }
}

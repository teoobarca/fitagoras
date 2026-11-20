package dev.hamper.tabula.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.graphics.toAwtImage
import dev.hamper.tabula.Env
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.MockResult
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * The page about the 2nd LA1 test on a profile with a record, not a fresh one: a definition done without help, a
 * topic begun, a lesson finished, a paper written. The page says each as it is (data/TestPage.kt, the exam's topics).
 * The same screen draws the DML page from its own asset. The rendered pages are left in build/reports/test-page-*.png.
 */
@OptIn(ExperimentalTestApi::class)
class TestPageUiTest {
    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
    }

    @Test fun statesFollowTheRecord() = runDesktopComposeUiTest(width = 400, height = 14000) {
        val store = Store(MemoryStateFile())
        store.pickCourse("la1")
        repeat(2) { store.record("concept:ln", ok = true, aided = false) }   // done without help
        repeat(2) { store.record("concept:tn", ok = true, aided = false) }
        store.record("gen:arith", ok = false, aided = false)                 // T^n: one of two begun → the topic is begun
        store.record("gen:param", ok = true, aided = true)                   // with help: begun, never done
        store.finishLesson("obal")
        store.mocks += MockResult("veverka-2024", 0L, listOf(listOf(5), listOf(8), listOf(8)), 21, 30)
        // every fold open: the states are checked on all of it (the page is layered, Teo 18. 11.)
        setContent { TabulaTheme(ThemeMode.Light) { androidx.compose.runtime.CompositionLocalProvider(dev.hamper.tabula.ui.LocalUnfoldAll provides true) { TestTopicsScreen(store, {}, {}, examId = "la1-test2") } } }
        waitForIdle()
        // the page keeps "1 z 17" and "21 z 30 b" together with no-break spaces: compare the words, not the spaces
        fun texts(n: androidx.compose.ui.semantics.SemanticsNode) = androidx.compose.ui.semantics.SemanticsProperties.Text.let { k -> if (k in n.config) n.config[k] else emptyList() }.map { it.text.replace('\u00A0', ' ') }
        fun shows(t: String) = onRoot().fetchSemanticsNode().let { r -> generateSequence(listOf(r)) { l -> l.flatMap { it.children }.takeIf { it.isNotEmpty() } }.flatten().any { n -> texts(n).any { t in it } } }
        fun count(t: String) = onRoot().fetchSemanticsNode().let { r -> generateSequence(listOf(r)) { l -> l.flatMap { it.children }.takeIf { it.isNotEmpty() } }.flatten().count { n -> texts(n).any { it == t } } }
        File("build/reports").mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File("build/reports/test-page-progress.png"))
        assertTrue("the LN definition topic is done", shows("1 z 17 zvládnutých bez pomoci"))
        assertTrue("done rows: the LN definition (twice: problem 3, one evening)", count("zvládnuté bez pomoci") >= 3)
        assertTrue("begun: T^n, the parameter, the span lesson", count("rozrobené") >= 3)
        assertTrue("a written paper says its points", shows("napísané: 21 z 30 b"))
    }

    @Test fun dmlPageFollowsTheRecord() = runDesktopComposeUiTest(width = 400, height = 14000) {
        val store = Store(MemoryStateFile())
        store.pickCourse("dml")
        repeat(2) { store.record("concept:dml-mz-slozeni", ok = true, aided = false) }   // done without help
        store.record("gen:dml-tc-tower", ok = false, aided = false)                       // begun
        store.mocks += MockResult("dml-2025-k", 0L, listOf(listOf(6), listOf(7), listOf(5)), 18, 30)
        setContent { TabulaTheme(ThemeMode.Light) { androidx.compose.runtime.CompositionLocalProvider(dev.hamper.tabula.ui.LocalUnfoldAll provides true) { TestTopicsScreen(store, {}, {}, examId = "dml-pisomka2") } } }
        waitForIdle()
        fun shows(t: String) = onAllNodes(hasText(t, substring = true)).fetchSemanticsNodes().isNotEmpty()
        fun count(t: String) = onAllNodes(hasText(t)).fetchSemanticsNodes().size
        File("build/reports").mkdirs()
        ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File("build/reports/test-page-dml.png"))
        assertTrue("the DML page, not the LA1 one", shows("Čo býva na 2. písomke") && !shows("Čo býva na 2. teste"))
        assertTrue("composition done: in the definitions and in one evening", count("zvládnuté bez pomoci") >= 2)
        assertTrue("two remainders begun", count("rozrobené") >= 1)
        assertTrue("a written paper says its points", shows("napísané: 18 z 30 b"))
    }
}

package dev.hamper.tabula.screens

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.compat.media.ExoClipPlayer
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.store
import dev.hamper.tabula.ui.LekciaNova
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * What the screenshots of the new lesson look (ui/LekciaVzhlad.kt) cannot show, tapped through in virtual time: a worked
 * example brings each new step into view, a walk goes from its start to its end with one main button at a time, the
 * clip's frame plays the clip as its button does. A small phone (360 × 640 dp), where the steps run below the fold.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w360dp-h640dp-xxhdpi")
class LekciaFlowTest {
    @get:Rule val rule = createComposeRule()

    private val now = SemanticsMatcher("stateDescription Teraz") { n -> n.config.getOrElseNullable(SemanticsProperties.StateDescription) { null } == "Teraz" }

    private fun open(id: String, step: Int) {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        dev.hamper.tabula.platform.AndroidContext.install(ctx)
        ClipLibrary.load()
        assertTrue("the new look is on", LekciaNova)
        rule.setContent {
            val c = LocalContext.current
            val store = remember { Tabula.store(c) }
            val app = remember {
                AppState().also {
                    val l = LessonById.getValue(id)
                    store.courseId = if (id.startsWith("dml")) "dml" else "la1"
                    LessonById.keys.forEach { k -> store.finishLesson(k) }
                    store.statsNoticed = true; store.symbolHintSeen = true
                    store.restartLesson(l, step); store.save()
                    it.push(Nav.Lesson(id))
                }
            }
            val clips = remember { ClipHost(ExoClipPlayer(c), onPip = {}) }
            DisposableEffect(clips) { onDispose { clips.release() } }
            App(app, store, clips, {}, {})
        }
        rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()
    }

    private fun idx(id: String, k: Int, p: (Step) -> Boolean) = LessonById.getValue(id).steps.withIndex().filter { p(it.value) }[k].index

    private fun next() { rule.onAllNodesWithText("Ďalší krok")[0].performClick(); rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }

    /** Every "Ďalší krok" of a worked example: the newest step (framed, "Teraz") is on screen, to the last one. */
    private fun workedFollows(id: String, step: Int) {
        open(id, step)
        val n = (LessonById.getValue(id).steps[step] as Step.Worked).solution.postup.aims.size
        for (i in 2..n) {
            next()
            if (i < n) rule.onNode(now, useUnmergedTree = true).assertIsDisplayed()
        }
        rule.onAllNodesWithText("Pokračovať")[0].assertIsDisplayed()
    }

    @Test fun worked_example_dml_brings_each_step_into_view() = workedFollows("dml-tc-delitelnost", 7)
    @Test fun worked_example_la1_brings_each_step_into_view() = workedFollows("la1-k3-regularita", 4)

    /** Euclid's divisions inside a worked step: the table walks from "Začiatok" to its end, nothing shown ahead. */
    @Test fun table_walk_from_start_to_end() {
        val id = "dml-tc-rea"
        open(id, idx(id, 0) { it is Step.Worked })
        next() // the step with the walk
        rule.onAllNodes(hasTestTag("table-counter"), useUnmergedTree = true)[0].assertTextIs("Začiatok")
        var guard = 0
        while (rule.onAllNodes(hasTestTag("table-next").and(SemanticsMatcher("enabled") { n -> !n.config.contains(SemanticsProperties.Disabled) }), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() && guard++ < 40) {
            rule.onAllNodes(hasTestTag("table-next"), useUnmergedTree = true)[0].performClick(); rule.mainClock.advanceTimeBy(600); rule.waitForIdle()
        }
        assertTrue("the walk ended within 40 taps", guard < 40)
        val end = rule.onAllNodes(hasTestTag("table-counter"), useUnmergedTree = true)[0].text()
        assertTrue("at the end: $end", end == "Výsledok" || end.contains(" z "))
    }

    /** GEM walked through: "Ďalší krok" is the one main button until the end, then "Pokračovať" (and only then). */
    @Test fun gem_walk_from_start_to_end() {
        val id = "la1-k3-hodnost"
        open(id, LessonById.getValue(id).steps.indexOfFirst { it is Step.Explore && it.pic is Pic.Gem })
        assertEquals("no way on while walking", 0, rule.onAllNodesWithText("Pokračovať").fetchSemanticsNodes().size)
        var taps = 0
        while (rule.onAllNodesWithText("Pokračovať").fetchSemanticsNodes().isEmpty() && taps++ < 30) next()
        assertTrue("the walk ended within 30 taps", taps < 30)
        rule.onAllNodesWithText("Pokračovať")[0].assertIsDisplayed()
        assertEquals("one 'Ďalší krok' at most after the end", true, rule.onAllNodesWithText("Ďalší krok").fetchSemanticsNodes().size <= 1)
    }

    /** The clip's frame does what "Pustiť klip" does: the question screen gives way to the player. */
    @Test fun clip_frame_plays_the_clip() {
        open("dml-tc-kongruencie", 2)
        rule.onAllNodesWithText("Radšej len prečítať titulky")[0].assertIsDisplayed()
        rule.onAllNodes(SemanticsMatcher("click label Pustiť klip") { n -> n.config.getOrElseNullable(androidx.compose.ui.semantics.SemanticsActions.OnClick) { null }?.label == "Pustiť klip" })[0].performClick(); rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()
        assertEquals("the intro is gone after tapping the frame", 0, rule.onAllNodesWithText("Radšej len prečítať titulky").fetchSemanticsNodes().size)
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.text(): String =
        fetchSemanticsNode().config.getOrElseNullable(SemanticsProperties.Text) { null }?.joinToString("") { it.text } ?: ""
    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertTextIs(s: String) = assertEquals(s, text())
}

package dev.hamper.tabula.snap

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.math.MathFonts
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.ExerciseView
import dev.hamper.tabula.screens.Frame
import dev.hamper.tabula.screens.StepView
import dev.hamper.tabula.ui.AnswerTag
import dev.hamper.tabula.ui.QuestionTag
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import dev.hamper.tabula.ui.wholeOnScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import kotlin.random.Random
import dev.hamper.tabula.store

/**
 * `./gradlew :app:snapFold`: on Teo's phone (411 × 914 dp, 100 % font, minus the status bar with the camera cutout and
 * the gesture bar) the first answer option of every question step is whole on the first screen, without scrolling
 * (docs/CONTENT.md, "The answers are on the first screen"). Every Ask and Check step of both courses, and every
 * generator over several seeds as a first encounter, the worst case: "Ako na to?" wants to be open then.
 */
@Config(qualifiers = "w411dp-h1000dp-xxhdpi")
class FoldTest : Shots() {
    @get:Rule val rule = createComposeRule()

    private val seeds = Snap.get("seeds")?.toInt() ?: 5
    private val only = Snap.get("only")

    @Test
    fun answersOnTheFirstScreen() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        dev.hamper.tabula.platform.AndroidContext.install(ctx); ClipLibrary.load()
        val store = Tabula.store(ctx)
        val clips = ClipHost(dev.hamper.tabula.compat.media.ExoClipPlayer(ctx), {})
        val frame = Frame(3, 12, {}, {})
        val items = buildList<Pair<String, @Composable () -> Unit>> {
            for (c in Courses) for (l in c.lessons) l.steps.forEachIndexed { i, s ->
                if ((s is Step.Ask || s is Step.Check) && (only == null || l.id.contains(only))) add("lesson:${l.id}:$i" to { StepView(s, frame, store, clips) {} })
            }
            for ((id, g) in Generators) if (only == null || id.contains(only)) for (seed in 0 until seeds) {
                val ex = g(Random(seed * 7919 + id.hashCode()))
                add("gen:$id seed $seed" to { ExerciseView(ex, false, frame, firstTime = true) { _, _ -> } })
            }
        }
        var current by mutableIntStateOf(0)
        rule.setContent {
            TabulaTheme(ThemeMode.Light) {
                Box(Modifier.size(411.dp, (914 - 52 - 24).dp).clipToBounds().background(T.c.bg)) { key(current) { items[current].second() } }
            }
        }
        val bad = mutableListOf<String>()
        for (i in items.indices) {
            rule.runOnIdle { current = i }
            rule.waitForIdle()
            val first = rule.onAllNodesWithTag(AnswerTag, useUnmergedTree = true).fetchSemanticsNodes().minByOrNull { it.positionInWindow.y }
            // options in the footer are always in view; then the question above them must be
            rule.onAllNodesWithTag(QuestionTag, useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()?.let { q ->
                if (!q.layoutInfo.coordinates.wholeOnScreen()) bad += "${items[i].first}: the question above the options is cut by the fold"
            }
            when {
                first == null -> bad += "${items[i].first}: no answer marked with Modifier.firstAnswer()"
                !first.layoutInfo.coordinates.wholeOnScreen() -> bad += "${items[i].first}: the first answer starts at ${(first.positionInWindow.y / first.layoutInfo.density.density).toInt()} dp, below the fold"
            }
        }
        println("snapFold: ${items.size} question screens, ${bad.size} with the answers below the fold")
        assertTrue("${bad.size} question screens need scrolling before the answers:\n" + bad.joinToString("\n"), bad.isEmpty())
    }
}

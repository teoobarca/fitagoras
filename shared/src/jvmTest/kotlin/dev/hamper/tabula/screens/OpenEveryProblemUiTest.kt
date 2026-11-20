package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.UkazkovyTutor
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Every problem opens (Teo, 18. 11.: "keď kliknem na doplnenie na bázu príklad, tak to spadne"): every generator over
 * several seeds, each of its parts as the screen shows it and its whole solution; every problem of every past and
 * generated paper, its screen and the solution of each part. Drawn headless with the real screens, so a problem that
 * throws while it is built, laid out or set never reaches a release.
 */
@OptIn(ExperimentalTestApi::class)
class OpenEveryProblemUiTest {
    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
    }

    /** A right answer of a part, so the next part can be built on it. */
    private fun right(a: Answer): Any = when (a) {
        is Answer.Choice -> a.correct
        is Answer.Numbers -> a.expected
        is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }
        is Answer.Vectors -> a.example
        is Answer.Paper -> 1
    }

    /** Phone widths and font sizes every problem is drawn at: the narrowest supported, the emulator's Pixel, large font. */
    private val Phones = listOf(Phone(320, 1f, false), Phone(411, 1.3f, true))

    /** [tutor]: ChatGPT connected, so the screen also builds what the tutor would get (its buttons, the GEM tasks). */
    private data class Phone(val widthDp: Int, val fontScale: Float, val tutor: Boolean)

    /** One screen to draw: what it is, and what draws it. */
    private class Shot(val what: String, val draw: @Composable () -> Unit)

    /**
     * Draws [shots] one after another in one window; the first that throws is reported and the rest of the group is
     * drawn in a fresh window (a composition that threw is not reused).
     */
    private fun drawAll(shots: List<Shot>, bad: MutableList<String>) {
        for (ph in Phones) drawAll(shots, bad, ph)
    }

    private fun drawAll(shots: List<Shot>, bad: MutableList<String>, ph: Phone) {
        val (widthDp, fontScale) = ph
        var from = 0
        while (from < shots.size) {
            var at = from
            try {
                runComposeUiTest {
                    var i by mutableIntStateOf(from)
                    setContent {
                        // a phone: its width and font size decide where formulas break, so the problem is drawn as there
                        val d = LocalDensity.current
                        CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale), LocalTutor provides if (ph.tutor) UkazkovyTutor("", "x") else null) {
                            TabulaTheme(ThemeMode.Light) { Box(Modifier.requiredWidth(widthDp.dp).fillMaxHeight()) { key(i) { shots[i].draw() } } }
                        }
                    }
                    waitForIdle()
                    while (at < shots.lastIndex) { at++; i = at; waitForIdle() }
                }
                from = shots.size
            } catch (e: Throwable) {
                bad += "${shots[at].what} at $widthDp dp, font $fontScale${if (ph.tutor) ", tutor on" else ""}: $e\n    " + e.stackTrace.take(8).joinToString("\n    ")
                from = at + 1
            }
        }
    }

    @Test fun everyProblemOpensAndShowsItsSolution() {
        val bad = ArrayList<String>()
        val store = Store(MemoryStateFile())
        val frame = Frame(0, 0, {}, {}, "Zavrieť")

        for ((id, gen) in Generators) {
            val shots = ArrayList<Shot>()
            for (seed in listOf(0, 1, 7, 42)) {
                val ex = try { gen(Random(seed)) } catch (e: Throwable) { bad += "gen:$id seed $seed does not build: $e"; continue }
                val answers = ArrayList<Any>()
                for (k in ex.parts.indices) {
                    val part = try { ex.parts[k](answers.toList()) } catch (e: Throwable) { bad += "gen:$id seed $seed part $k: $e"; break }
                    val before = answers.toList()
                    // the part as its screen shows it, with the parts before it answered right
                    val one: Exercise = ex.copy(parts = listOf { _ -> ex.parts[k](before) })
                    shots += Shot("gen:$id seed $seed part $k") { ExerciseView(one, solo = false, frame = frame, doneLabel = "Hotovo", firstTime = k == 0, onDone = { _, _ -> }) }
                    answers += right(part.answer)
                }
                val sol = try { ex.solution(answers.toList()) } catch (e: Throwable) { bad += "gen:$id seed $seed solution: $e"; null }
                if (sol != null) shots += Shot("gen:$id seed $seed solution") { Column(Modifier.verticalScroll(rememberScrollState())) { SolutionView(sol) } }
            }
            drawAll(shots, bad)
        }

        for (c in Courses) {
            val shots = ArrayList<Shot>()
            for (paper in c.papers + (0 until 3).map { s -> c.generatedPaper(s) }) for (p in paper.problems) {
                shots += Shot("paper:${paper.id}:${p.n}") { PaperProblemScreen(paper, p, store, {}) {} }
                for (u in p.units) u.solution?.let { sol -> shots += Shot("paper:${paper.id}:${p.n} ${u.label} solution") { Column(Modifier.verticalScroll(rememberScrollState())) { SolutionView(sol) } } }
            }
            drawAll(shots, bad)
        }

        assertEquals("problems that throw when opened (${bad.size}):\n" + bad.joinToString("\n"), emptyList<String>(), bad)
    }
}

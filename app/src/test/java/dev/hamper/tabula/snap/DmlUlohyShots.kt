package dev.hamper.tabula.snap

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.data.dml.*
import dev.hamper.tabula.data.dml.numbers.*
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.ExerciseView
import dev.hamper.tabula.screens.Frame
import dev.hamper.tabula.screens.SolutionView
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import java.io.File
import kotlin.random.Random

/*
 * The four number theory types of 19. 11. (word problem, interval, why no other solutions, inverse through Euler): the
 * question, a typical wrong answer with its feedback, the solution.
 * `./gradlew :app:snap -Psnap.test='DmlUlohy*' -Psnap.out=<dir>`.
 */

/** The tasks the shots show: the first seed of each type with the mistake worth showing. */
internal object DmlUlohy {
    fun <T> seed(make: (Random) -> T, ok: (T) -> Boolean): T = (0 until 500).map { make(Random(it)) }.first(ok)
    val word get() = seed(::tcWordTask) { it.story == 0 && TcSaid.MISSING in it.how }
    val comet get() = seed(::tcWordTask) { it.story == 4 }
    val interval get() = seed(::tcIntervalTask) { t ->
        val ps = t.cs.map { it.solved }
        ps.all { it != null } && crt(ps.map { it!!.x0 to it.n })?.let { s -> t.cs.fold(1L) { a, c -> lcm(a, c.m) } != s.mod && (t.lo..t.hi).count { x -> Math.floorMod(x - s.x, s.mod) == 0L } == 1 } == true
    }
    val noSolution get() = seed(::tcIntervalTask) { it.cs.any { c -> c.solved == null } }
    val only get() = seed(::tcOnlyTask) { t -> t.cs.size == 2 && t.cs.any { it.solved!!.g > 1 } }
    val euler get() = seed(::tcInvEulerTask) { it.m == 24L && gcd(it.a, it.m) == 1L }
    val sequence get() = seed(::miSeqTask) { it.from == 4 }

    /** One part of [ex] as its screen shows it, the parts before answered right. */
    fun partOf(ex: Exercise, k: Int): Exercise {
        val answers = ArrayList<Any>()
        for (i in 0 until k) answers += right(ex.parts[i](answers).answer)
        val before = answers.toList()
        return ex.copy(parts = listOf { _ -> ex.parts[k](before) })
    }
    fun right(a: Answer): Any = when (a) { is Answer.Choice -> a.correct; is Answer.Numbers -> a.expected; is Answer.Paper -> 1; else -> error("no") }
    fun answersOf(ex: Exercise): List<Any> { val answers = ArrayList<Any>(); for (b in ex.parts) answers += right(b(answers).answer); return answers }
    fun lcmOf(t: TcIntervalTask) = t.cs.fold(1L) { a, c -> lcm(a, c.m) }
    fun lcmOf(t: TcOnlyTask) = t.cs.fold(1L) { a, c -> lcm(a, c.m) }
    fun sol(cs: List<TcCong>) = crt(cs.map { it.solved!!.let { l -> l.x0 to l.n } })!!
}

/** Static screens: the question (light, dark, 427 and 360 dp) and the whole solution. */
class DmlUlohyShots : Shots() {
    private val frame = Frame(0, 0, {}, {})
    private fun question(name: String, ex: Exercise) =
        shoot(name, Snap.looks(theme = "light,dark", width = "pixel,360")) { ExerciseView(ex, false, frame, firstTime = true) { _, _ -> } }
    private fun solution(name: String, ex: Exercise) {
        val sol = ex.solution(DmlUlohy.answersOf(ex))
        val looks = listOf(Look(false, Pixel.first, 2600, 1f), Look(true, Pixel.first, 2600, 1f))
        shoot(name, looks) { Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) { SolutionView(sol) } }
    }
    @Test fun slovna1Otazka() = question("slovna-1-otazka", tcWord(DmlUlohy.word))
    @Test fun slovna3Riesenie() = solution("slovna-3-riesenie", tcWord(DmlUlohy.word))
    @Test fun slovna4Kometa() = question("slovna-4-kometa", tcWord(DmlUlohy.comet))
    @Test fun interval1Otazka() = question("interval-1-otazka", tcInterval(DmlUlohy.interval))
    @Test fun interval3Riesenie() = solution("interval-3-riesenie", tcInterval(DmlUlohy.interval))
    @Test fun jedine1Otazka() = question("jedine-1-otazka", DmlUlohy.partOf(tcOnly(DmlUlohy.only), 1))
    @Test fun jedine4Riesenie() = solution("jedine-4-riesenie", tcOnly(DmlUlohy.only))
    @Test fun euler1Otazka() = question("euler-1-otazka", tcInvEuler(DmlUlohy.euler))
    @Test fun euler4Riesenie() = solution("euler-4-riesenie", tcInvEuler(DmlUlohy.euler))
    @Test fun postupnost1Otazka() = question("postupnost-1-otazka", miSeq(DmlUlohy.sequence))
    @Test fun postupnost4Riesenie() = solution("postupnost-4-riesenie", miSeq(DmlUlohy.sequence))
}

/** Answered on the screen: a typical wrong answer and its feedback, the paper step's model; light and dark. */
@Config(qualifiers = "w411dp-h1000dp-xxhdpi")
class DmlUlohyTaps : Shots() {
    @get:Rule val rule = createComposeRule()
    private val frame = Frame(0, 0, {}, {})

    // the debug build names every answer field (ui/Components.kt, dbg)
    private fun field(i: Int) = SemanticsMatcher("field $i") { n -> n.config.getOrNull(SemanticsProperties.ContentDescription)?.any { it.startsWith("dbg:field$i=") } == true }

    /** Types [values] into the fields of the part on screen and checks them. */
    private fun type(values: List<Long>) {
        values.forEachIndexed { i, v ->
            if (i > 0) rule.onNode(field(i), useUnmergedTree = true).performScrollTo().performClick()
            if (v < 0) rule.onNodeWithTag("key:−").performClick()
            for (ch in "${kotlin.math.abs(v)}") rule.onNodeWithTag("key:$ch").performClick()
        }
        rule.onNodeWithText("Skontrolovať").performClick()
        rule.waitForIdle()
    }

    /** [ex] on screen in both themes; [act] answers it, then the screen is captured as <name>-<theme>.png. */
    private fun interact(name: String, ex: Exercise, act: () -> Unit) {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        dev.hamper.tabula.Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        dev.hamper.tabula.platform.AndroidContext.install(ctx); ClipLibrary.load()
        var dark by mutableStateOf(false)
        var round by mutableIntStateOf(0)
        rule.setContent {
            key(round) {
                TabulaTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) {
                    Box(Modifier.size(411.dp, (914 - 52 - 24).dp).clipToBounds().background(T.c.bg)) { ExerciseView(ex, false, frame, firstTime = false) { _, _ -> } }
                }
            }
        }
        for (d in listOf(false, true)) {
            rule.runOnIdle { dark = d; round++ }
            rule.waitForIdle()
            act()
            // the feedback that names the mistake is behind "Prečo?" (first sentence rule): open it for the shot
            rule.onAllNodesWithContentDescription("Prečo? Rozbaliť vysvetlenie").let { w -> if (w.fetchSemanticsNodes().isNotEmpty()) { w[0].performClick(); rule.waitForIdle() } }
            rule.onRoot().captureRoboImage(File(Snap.out, "$name-${if (d) "dark" else "light"}.png").path,
                com.github.takahirom.roborazzi.RoborazziOptions(taskType = com.github.takahirom.roborazzi.RoborazziTaskType.Record, recordOptions = com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions(resizeScale = 1.0)))
        }
    }

    @Test fun slovnaZla() { val t = DmlUlohy.word; interact("slovna-2-zla", tcWord(t)) { type(t.mods.indices.map { if (t.how[it] == TcSaid.MISSING) t.said[it] else t.res[it] }) } }
    @Test fun intervalZlaModul() {
        val t = DmlUlohy.interval; val s = DmlUlohy.sol(t.cs)
        interact("interval-2-zla-modul", DmlUlohy.partOf(tcInterval(t), 1)) { type(listOf(s.x, DmlUlohy.lcmOf(t))) }
    }
    @Test fun intervalZlaBezRiesenia() = interact("interval-4-zla-bez-riesenia", tcInterval(DmlUlohy.noSolution)) { rule.onNodeWithText("Áno").performClick(); rule.waitForIdle() }
    @Test fun jedineVzor() = interact("jedine-2-vzor", DmlUlohy.partOf(tcOnly(DmlUlohy.only), 1)) { rule.onNodeWithText("Mám napísané, ukáž vzor").performClick(); rule.waitForIdle() }
    @Test fun jedineZla() = interact("jedine-3-zla", DmlUlohy.partOf(tcOnly(DmlUlohy.only), 1)) {
        rule.onNodeWithText("Mám napísané, ukáž vzor").performClick(); rule.waitForIdle()
        rule.onNodeWithText("Nemám").performScrollTo().performClick(); rule.waitForIdle()
        rule.onNodeWithText("Pokračovať").performClick(); rule.waitForIdle()
    }
    @Test fun jedineZlaModul() {
        val t = DmlUlohy.only; val s = DmlUlohy.sol(t.cs)
        interact("jedine-5-zla-modul", tcOnly(t)) { type(listOf(s.x, DmlUlohy.lcmOf(t))) }
    }
    @Test fun eulerZlaPhi() = interact("euler-2-zla-phi", DmlUlohy.partOf(tcInvEuler(DmlUlohy.euler), 1)) { type(listOf(4)) }
    @Test fun postupnostZla() = interact("postupnost-2-zla", miSeq(DmlUlohy.sequence)) { type(listOf(3)) }
    @Test fun postupnostVzor() = interact("postupnost-3-vzor", DmlUlohy.partOf(miSeq(DmlUlohy.sequence), 2)) { rule.onNodeWithText("Mám napísané, ukáž vzor").performClick(); rule.waitForIdle() }
    @Test fun eulerZlaExponent() = interact("euler-3-zla-exponent", DmlUlohy.partOf(tcInvEuler(DmlUlohy.euler), 2)) { type(listOf(8)) }
}

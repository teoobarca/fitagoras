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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.data.dml.*
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.ExerciseView
import dev.hamper.tabula.screens.Frame
import dev.hamper.tabula.screens.SolutionView
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import dev.hamper.tabula.ui.T
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import java.io.File
import kotlin.random.Random

/*
 * The drills that close the proseminar gaps of the 2nd DML test (NumbersDrills.kt, SetsDrills.kt, InductionDrills.kt):
 * each type's question at 360 dp in both themes, its solution, and the model on paper.
 * `./gradlew :app:snap -Psnap.test='DmlMedzery*' -Psnap.out=<dir>`.
 */

internal object DmlMedzery {
    private fun <T> seed(make: (Random) -> T, ok: (T) -> Boolean): T = (0 until 800).map { make(Random(it)) }.first(ok)
    val divTrue get() = tcDivProof(TcDivClaims.first { it.id == "kratenie" }, Random(1))
    val divFalse get() = tcDivProof(TcDivClaims.first { it.id == "euklid" }, Random(3))
    val allNPoly get() = tcAllN(seed(::tcAllNTask) { it.kind == 0 && it.d == 6L }, Random(2))
    val allNProduct get() = tcAllN(TcAllNTask(1, 24, k = 4), Random(5))
    val allNPowers get() = tcAllN(seed(::tcAllNTask) { it.kind == 2 && it.pw!!.n0 == 1 }, Random(0))
    val factor get() = tcFactor(seed(::tcFactorTask) { it.a < 0 || it.b < 0 })
    val reduce get() = tcReduce(seed(::tcReduceTask) { it.m == 243L })
    val division get() = tcDiv(seed(::tcDivTask) { it.kind == 2 })
    val hourglass get() = tcWordLdr(TcWordLdrTask(2, 11, 5, 7))
    val camp get() = tcWordLdr(TcWordLdrTask(3, 11, 6, 350, 10))
    val buy get() = tcWordLdr(seed(::tcWordLdrTask) { it.kind == 0 && it.least == 1L })
    val inclusionTrue get() = inclusionClaim(InclusionClaims.first { it.id == "prienik" }, Random(0))
    val inclusionFalse get() = inclusionClaim(InclusionClaims.first { it.id == "doplnky-zle" }, Random(4))
    val domain get() = (0 until 800).map { domainRangeExercise(Random(it)) }.first { "podíl" in it.prompt }
    val setEven get() = miSet(MiSetTask(0, 6, 8))
    val setStep get() = miSet(MiSetTask(2, -3, 4))
    val stepGood get() = miStep(MiStepTask(1, 2, listOf(1, 2), 37))
    val stepMissing get() = miStep(MiStepTask(0, 3, listOf(0, 2), 20))
    val flawPower get() = miFlaw(MiFlawTask(2))
    val flawSum get() = miFlaw(MiFlawTask(1, 1, -13))

    val all: List<Pair<String, () -> Exercise>> = listOf(
        "tc-delitelnost-plati" to { divTrue }, "tc-delitelnost-neplati" to { divFalse },
        "tc-vsetky-n-polynom" to { allNPoly }, "tc-vsetky-n-sucin" to { allNProduct }, "tc-vsetky-n-mocniny" to { allNPowers },
        "tc-rozklad" to { factor }, "tc-redukcia" to { reduce }, "tc-deleni" to { division },
        "tc-hodiny" to { hourglass }, "tc-tabor" to { camp }, "tc-nakup" to { buy },
        "mz-inkluzia-plati" to { inclusionTrue }, "mz-inkluzia-neplati" to { inclusionFalse }, "mz-obory" to { domain },
        "mi-mnozina-parne" to { setEven }, "mi-mnozina-krok" to { setStep }, "mi-krok-dobre" to { stepGood }, "mi-krok-chyba" to { stepMissing },
        "mi-chyba-mocnina" to { flawPower }, "mi-chyba-suma" to { flawSum },
    )
}

/** The question at 360 dp, light and dark, and the whole solution. */
class DmlMedzeryShots : Shots() {
    private val frame = Frame(0, 0, {}, {})

    @Test fun questions() {
        for ((name, ex) in DmlMedzery.all) shoot("$name-1-otazka", Snap.looks(theme = "light,dark", width = "360")) { ExerciseView(ex(), false, frame, firstTime = true) { _, _ -> } }
    }

    @Test fun solutions() {
        val looks = listOf(Look(false, Pixel.first, 2600, 1f), Look(true, Pixel.first, 2600, 1f))
        for ((name, ex) in DmlMedzery.all) {
            val e = ex()
            val sol = e.solution(DmlUlohy.answersOf(e))
            shoot("$name-3-riesenie", looks) { Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) { SolutionView(sol) } }
        }
    }
}

/** The part written on paper after "Mám napísané, ukáž vzor", light and dark, at 411 dp. */
@Config(qualifiers = "w411dp-h1000dp-xxhdpi")
class DmlMedzeryPaper : Shots() {
    @get:Rule val rule = createComposeRule()
    private val frame = Frame(0, 0, {}, {})

    @Test fun models() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        dev.hamper.tabula.Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        dev.hamper.tabula.platform.AndroidContext.install(ctx); ClipLibrary.load()
        var dark by mutableStateOf(false)
        var round by mutableIntStateOf(0)
        var ex by mutableStateOf(DmlMedzery.divTrue)
        rule.setContent {
            key(round) {
                TabulaTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) {
                    Box(Modifier.size(411.dp, (914 - 52 - 24).dp).clipToBounds().background(T.c.bg)) { ExerciseView(ex, false, frame, firstTime = false) { _, _ -> } }
                }
            }
        }
        for ((name, make) in DmlMedzery.all) for (d in listOf(false, true)) {
            val e = make()
            rule.runOnIdle { ex = DmlUlohy.partOf(e, e.parts.size - 1); dark = d; round++ }
            rule.waitForIdle()
            val tap = rule.onAllNodesWithTextSafe("Mám napísané, ukáž vzor")
            if (!tap) continue
            rule.onNodeWithText("Mám napísané, ukáž vzor").performClick(); rule.waitForIdle()
            rule.onRoot().captureRoboImage(File(Snap.out, "$name-2-vzor-${if (d) "dark" else "light"}.png").path,
                com.github.takahirom.roborazzi.RoborazziOptions(taskType = com.github.takahirom.roborazzi.RoborazziTaskType.Record, recordOptions = com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions(resizeScale = 1.0)))
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextSafe(text: String) =
        onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().isNotEmpty()
}

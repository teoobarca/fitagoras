package dev.hamper.tabula.snap

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.PorovnanieSPapierom
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.UkazkovyTutor
import dev.hamper.tabula.ui.H1
import dev.hamper.tabula.ui.SourceCard
import dev.hamper.tabula.ui.SourceLines
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import java.io.File

/**
 * A definition written from memory and photographed, for the app's web page (its Pytagoras section): the linear span
 * (Definice 2.33) with the coefficients' "α_i ∈ T" left out. The step as Steps.kt draws it after writing ("Porovnaj so
 * skriptami.", the textbook's card, the paper's check), "Odfoť riešenie" and "Čo som prečítal" tapped. The judgement
 * is a real one: gpt-6.2-sol at low effort (Nastavenie.z) gave "neuznal_by" 14 times of 14 on this paper (20. 11.);
 * the text is one of those runs word for word, the one that quotes what the student wrote in its first sentence. `./gradlew :app:snap -Psnap.test=TutorDefiniciaShots -Psnap.out=DIR`:
 * tutor-definicia-<light|dark>-427-1.0.png, 1281 px wide like the Pixel's captures.
 */
@Config(qualifiers = "w427dp-h1400dp-xxhdpi")
class TutorDefiniciaShots : Shots() {
    @get:Rule val rule = createComposeRule()

    private val obal = ConceptById.getValue("obal")
    private val prepis = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"riadky":["Lineárny obal súboru §(x_1,\\dots,x_m)§ z §T^n§ je množina","§\\langle x_1,\\dots,x_m\\rangle = \\{\\sum_{i=1}^m \\alpha_i x_i\\}§."],"nejasne":[]}""".replace('§', '$')
    private val posudok = """{"verdikt":"neuznal_by","text":"Neuznal by som to ako úplnú definíciu, lebo v zápise „§\\{\\sum_{i=1}^m \\alpha_i x_i\\}§“ nehovoríš, aké hodnoty môžu koeficienty nadobúdať. Doplň, že každý koeficient §\\alpha_i§ nezávisle prebieha celé §T§. Tým bude jasné, že do obalu patria všetky takéto súčty, nie iba jeden.","body_splnene":[1],"istota":"vysoka"}""".replace('§', '$')

    @Test fun definicia() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        dev.hamper.tabula.Env.install(debug = false, clipsGuestToken = "", versionName = "test")
        dev.hamper.tabula.platform.AndroidContext.install(ctx); ClipLibrary.load()
        var dark by mutableStateOf(false)
        var round by mutableIntStateOf(0)
        rule.setContent {
            key(round) {
                TabulaTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) {
                    CompositionLocalProvider(LocalTutor provides UkazkovyTutor("", vyvojar = false, nastroj = "posud_papier", prepisTextu = prepis, posudok = posudok)) {
                        Column(Modifier.fillMaxSize().background(T.c.bg).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                            Spacer(Modifier.height(60.dp))
                            H1("Porovnaj so skriptami.")
                            Spacer(Modifier.height(10.dp))
                            SourceCard(obal.src, statement = true) { SourceLines(obal.czech) }
                            Spacer(Modifier.height(14.dp))
                            PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto(obal) }, obal.units, null, { _, _ -> }, obal.extra)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
        for (d in listOf(false, true)) {
            rule.runOnIdle { dark = d; round++ }
            rule.waitForIdle()
            rule.onNodeWithText("Odfoť riešenie, Pytagoras ho skontroluje").performScrollTo().performClick()
            rule.mainClock.advanceTimeBy(30_000)
            rule.waitForIdle()
            // its click action, not a touch: a touch leaves the platform's ripple (drawn in real time) on the capture
            rule.onNodeWithText("Čo som prečítal").performScrollTo().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
            rule.mainClock.advanceTimeBy(2_000)
            rule.waitForIdle()
            rule.onRoot().captureRoboImage(File(Snap.out, "tutor-definicia-${if (d) "dark" else "light"}-427-1.0.png").path,
                com.github.takahirom.roborazzi.RoborazziOptions(taskType = com.github.takahirom.roborazzi.RoborazziTaskType.Record, recordOptions = com.github.takahirom.roborazzi.RoborazziOptions.RecordOptions(resizeScale = 1.0)))
        }
    }
}

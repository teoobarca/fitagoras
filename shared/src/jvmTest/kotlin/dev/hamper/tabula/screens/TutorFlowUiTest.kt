package dev.hamper.tabula.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.intMat
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.Kontext
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.UkazkovyTutor
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The tutor's panel tapped through on the desktop JVM with a fake model (UkazkovyTutor) and the real engine: Teo's
 * own photographed chain (gem-2, a wrong 0 in the 3rd step) gives the marked step and the right one; a question after
 * it carries the first answer in the history; a used-up plan says so; an unsure cell is asked, not marked.
 */
@OptIn(ExperimentalTestApi::class)
class TutorFlowUiTest {
    @Before fun setUp() { Env.install(debug = true, clipsGuestToken = "", versionName = "test"); installJvmAssets() }

    private val uloha = GemUloha(Field.R, intMat(listOf(2, 3, 2, 2), listOf(1, 1, 1, 0), listOf(3, 1, 2, -1), listOf(4, 2, 1, 5)))
    private val kontext = Kontext("Spočtěte hodnost matice $\\mathbf{A}$.", "\$h(\\mathbf{A})=\$", "4", "3", "Priveľa: v HST ti ostal nenulový riadok.", listOf("Uprav na HST"), listOf(uloha))

    private fun prepis(d: String = "0") = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"bloky":[{"druh":"gem","strana":1,"popis":null,
        "matice":[{"riadky":[["2","3","2","2"],["1","1","1","0"],["3","1","2","-1"],["4","2","1","5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["2","3","2","2"],["3","1","2","-1"],["0","-2","-3","5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],["0","+2","+1","+1"],["0","2","3","-5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],["0","0","$d","-3"],["0","0","3","-9"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],["0","0","3","-9"],["0","0","0","1"]],"ciara":null,"preskrtnuta":false,"oblast":null}],
        "operacie":[null,null,null,null],"nejasne":[],"zaver":null}]}"""

    private val miesto get() = dev.hamper.tabula.tutor.MiestoAppky("Hodnosť matice", "la1", "Príklad: hodnosť matice.", gem = kontext, odpoved = "4")

    private fun ComposeUiTest.open(t: UkazkovyTutor) = setContent {
        TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) { TutorPanel(miesto, true, { _, _ -> }, {}) } }
    }

    // the typesetter keeps "v 3." together with a no-break space
    private fun ComposeUiTest.shows(s: String) = onAllNodes(hasText(s.replace("v ", "v\u00a0"), substring = true)).fetchSemanticsNodes().isNotEmpty() ||
        onAllNodes(hasText(s, substring = true)).fetchSemanticsNodes().isNotEmpty()

    @Test fun mistakeMarkedExplainedAndAskedAgain() = runComposeUiTest {
        val t = UkazkovyTutor(prepis(), "V 3. úprave si od 3. riadku odčítal dvojnásobok druhého.", nastroj = "over_postup_gem")
        open(t)
        waitForIdle()
        assertTrue(onRoot(useUnmergedTree = true).printToString(), shows("Chyba je v 3. úprave, v 3. riadku: najskôr má byť"))
        assertTrue(shows("Správne by bolo"))
        assertTrue(shows("dvojnásobok"))
        // the photo was read once, by the reading call; the model's words got the engine's facts, never the photo
        assertTrue(t.volania.last().toString().contains("FAKTY"))
        assertTrue(t.volania.drop(1).none { "input_image" in it.toString() })
        // a further question carries the first exchange in the history, and nothing is read again
        onNode(hasSetTextAction()).performTextInput("Prečo práve 1?")
        onNode(hasContentDescription("Poslať")).performClick()
        waitForIdle()
        val last = t.volania.last()
        assertTrue(last.getJSONArray("input").length() >= 3)
        assertTrue("input_image" !in last.toString())
    }

    @Test fun usedUpPlan() = runComposeUiTest {
        val t = UkazkovyTutor(prepis(), chyba = TutorChyba(TutorChyba.Druh.LIMIT, "subscription_sharing_usage_limit_exceeded", "limit", 429), nastroj = "over_postup_gem")
        open(t)
        waitForIdle()
        assertTrue(onRoot(useUnmergedTree = true).printToString(), shows("dosiahol si limit"))
        assertEquals(1, t.volania.size)
    }

    @Test fun unsureCellIsAskedNotMarked() = runComposeUiTest {
        // a misreading the next step contradicts: the 3rd matrix read with 9 instead of 1, the 4th follows from 1
        val t = UkazkovyTutor(prepis("1").replace("""["0","+2","+1","+1"]""", """["0","+2","+9","+1"]"""), "x", nastroj = "over_postup_gem")
        open(t)
        waitForIdle()
        assertTrue(onRoot(useUnmergedTree = true).printToString(), shows("Neviem rozlíšiť jedno číslo"))
        onAllNodes(hasText("1") and hasClickAction()).onFirst().performClick()
        waitForIdle()
        // the student said 1: the engine goes on and finds the next step that doesn't follow (the last matrix of this
        // chain was computed from a 0 there)
        assertTrue(onRoot(useUnmergedTree = true).printToString(), shows("Chyba je v 4. úprave"))
    }

    /** The button sits under the app's own feedback after a wrong answer, only with ChatGPT connected; it opens the tutor. */
    @Test fun buttonAfterWrongAnswer() = runComposeUiTest {
        val ex = dev.hamper.tabula.data.Generators.getValue("ln")(kotlin.random.Random(3))
        val a = ex.parts.first()(emptyList()).answer as dev.hamper.tabula.data.Answer.Choice
        val t = UkazkovyTutor(prepis(), "x")
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) { ExerciseView(ex, false, Frame(1, 1, {}, {})) { _, _ -> } } } }
        waitForIdle()
        assertTrue(!shows("Odfoť postup"))
        onAllNodes(hasText(a.options[1 - a.correct]) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Tentoraz nie"))
        onAllNodes(hasText("Odfoť postup", substring = true) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        // the fake camera hands a page over at once: the app's GEM tool read it once (the ln exercise's matrix differs
        // from this chain), then the model wrote, without the photo
        assertEquals(2, t.volania.size)
        assertTrue("input_image" in t.volania[0].toString() && "input_image" !in t.volania[1].toString())
    }

    /** Without ChatGPT the button is there too (Teo, 18. 11.): its first tap is the sign-in screen, no call. */
    @Test fun withoutChatGptTheButtonOffersSignIn() = runComposeUiTest {
        val ex = dev.hamper.tabula.data.Generators.getValue("ln")(kotlin.random.Random(3))
        val a = ex.parts.first()(emptyList()).answer as dev.hamper.tabula.data.Answer.Choice
        val t = UkazkovyTutor(prepis(), "x", pripojenie = dev.hamper.tabula.tutor.Pripojenie.Nepripojeny)
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) { ExerciseView(ex, false, Frame(1, 1, {}, {})) { _, _ -> } } } }
        onAllNodes(hasText(a.options[1 - a.correct]) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        onAllNodes(hasText("Odfoť postup", substring = true) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Prihlásiť sa cez ChatGPT"))
        assertEquals(0, t.volania.size)
    }

    private val dokaz = dev.hamper.tabula.data.ProofById.getValue("p-dim3")
    private val prepisDokazu = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"riadky":["Báza = LN a generuje, LN je predpoklad.","Ak by negeneroval, pridám v a mám 4 LN vektory v P, spor s dim P = 3."],"nejasne":[]}"""
    private val posudokDokazu = """{"verdikt":"uznal_by","text":"Uznal by som to: spor s dimenziou je presne to, čo treba.","body_splnene":[1,2,3,4],"istota":"vysoka"}"""

    /**
     * The paper's check in the step (Teo, 18. 11.: the task must stay in view): the camera alone, the judgement in
     * place with its verdict, the checklist ticked; the conversation only on "Opýtať sa ďalej", with that turn.
     */
    @Test fun paperJudgedInTheStep() = runComposeUiTest {
        val t = UkazkovyTutor("", "Lebo LN súbor dĺžky dim P je báza.", nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)
        var ticked = emptyList<Int>()
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) {
            androidx.compose.foundation.layout.Column { PapierKontrola({ dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)) }) { ps, _ -> ticked = ps.splnene.sorted() } }
        } } }
        onAllNodes(hasText("Odfoť riešenie", substring = true) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("dimenziou je presne to"))
        assertTrue(shows("Pytagoras by to uznal"))
        assertEquals(listOf(0, 1, 2, 3), ticked)
        assertTrue(!shows("Na čo sa chceš opýtať"))
        // the follow-up: the panel opens with the paper's turn in it, and the question goes on in the same history
        onAllNodes(hasText("Opýtať sa ďalej") and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Uznal by som"))
        val pred = t.volania.size
        onAllNodes(hasSetTextAction()).onFirst().performTextInput("Prečo?")
        onAllNodes(hasContentDescription("Poslať")).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("súbor dĺžky dim"))
        assertTrue(t.volania.size > pred && "spor s dimenziou" in t.volania.last().toString())
    }

    /** The first use from the step: the sign-in screen, no call, and no card in the step. */
    @Test fun paperInTheStepWithoutChatGpt() = runComposeUiTest {
        val t = UkazkovyTutor("", pripojenie = dev.hamper.tabula.tutor.Pripojenie.Nepripojeny)
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) {
            androidx.compose.foundation.layout.Column { PapierKontrola({ dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)) }) { _, _ -> } }
        } } }
        onAllNodes(hasText("Odfoť riešenie", substring = true) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Prihlásiť sa cez ChatGPT"))
        assertEquals(0, t.volania.size)
    }

    /** Without the tutor the paper is one tap, the checklist only background to unfold, never ticked (Teo, 18. 11.). */
    @Test fun paperWithoutTutorIsOneTap() = runComposeUiTest {
        var h: Int? = null
        setContent { TabulaTheme(ThemeMode.Light) {
            androidx.compose.foundation.layout.Column { PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)) }, dev.hamper.tabula.tutor.polozkyDokazu(dokaz), h, { x, _ -> h = x }) }
        } }
        assertTrue(shows("Máš to na papieri?"))
        assertTrue(!shows("Odfoť riešenie"))
        assertTrue(!shows("Začiatok"))
        onAllNodes(hasText("Mám to") and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertEquals(Hodnotenie.MAM, h)
        onAllNodes(hasText("Na čo sa pozerá cvičiaci") and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("Začiatok"))
    }

    /** Review 18. 11.: a verdict the tutor is unsure of records nothing; the student still says it with one tap. */
    @Test fun anUnsureVerdictDecidesNothing() = runComposeUiTest {
        val t = UkazkovyTutor("", nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu.replace("\"vysoka\"", "\"nizka\""))
        var h: Int? = null
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) {
            androidx.compose.foundation.layout.Column { PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)) }, dev.hamper.tabula.tutor.polozkyDokazu(dokaz), h, { x, _ -> h = x }) }
        } } }
        onAllNodes(hasText("Odfoť riešenie", substring = true) and hasClickAction()).onFirst().performClick()
        waitForIdle()
        assertTrue(shows("dimenziou je presne to"))
        assertEquals(null, h)
        assertTrue(shows("Mám to"))
        // a sure one decides
    }
}

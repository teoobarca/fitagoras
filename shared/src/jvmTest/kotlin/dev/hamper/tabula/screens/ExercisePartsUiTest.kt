package dev.hamper.tabula.screens

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.Part
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * The hint and "Ako na to?" belong to the part on screen (Teo, 17. 11.): opened in one part, they are closed when the
 * next part comes, and the hint there is that part's own when it has one.
 */
@OptIn(ExperimentalTestApi::class)
class ExercisePartsUiTest {
    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
    }

    private val postup = Generators.values.first()(Random(0)).postup
    private val ex = Exercise(
        "test", "Príklad", "Test", "Zadanie.", parts = listOf(
            { _ -> Part("Prvá otázka?", Answer.Choice(listOf("Áno prvá", "Nie prvá"), 0, "Lebo."), hint = "Rada prvej časti.", aim = 0) },
            { _ -> Part("Druhá otázka?", Answer.Choice(listOf("Áno druhá", "Nie druhá"), 0, "Lebo."), hint = "Rada druhej časti.", aim = 1) },
            { _ -> Part("Tretia otázka?", Answer.Choice(listOf("Áno tretia", "Nie tretia"), 0, "Lebo."), aim = 1) },
        ),
        postup = postup, solution = { dev.hamper.tabula.data.Solution(postup, emptyList()) }, hint = "Celková rada príkladu.",
    )

    private fun ComposeUiTest.tap(text: String) { onAllNodes(hasText(text, substring = true) and hasClickAction()).onFirst().performClick(); waitForIdle() }
    /** "Ako na to?" or "Nápoveda": straight beside the question, or through "Pomoc" where the two fold into one (narrow line). */
    private fun ComposeUiTest.help(text: String) {
        if (onAllNodes(hasText(text) and hasClickAction()).fetchSemanticsNodes().isEmpty()) tap("Pomoc")
        tap(text)
    }
    private fun ComposeUiTest.shows(text: String) = onAllNodes(hasText(text, substring = true) or hasContentDescription(text, substring = true)).fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.methodOpen() = onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Rozbalené")).fetchSemanticsNodes().isNotEmpty()
    private fun ComposeUiTest.nowCount() = onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Teraz"), useUnmergedTree = true).fetchSemanticsNodes().size

    @Test fun hintAndMethodCloseForTheNextPart() = runComposeUiTest {
        setContent { TabulaTheme(ThemeMode.Light) { ExerciseView(ex, false, Frame(1, 1, {}, {})) { _, _ -> } } }
        waitForIdle()
        help("Nápoveda")
        assertTrue("the first part's own hint", shows("Rada prvej časti."))
        assertFalse("never the exercise's common hint in a longer exercise", shows("Celková rada príkladu."))
        help("Ako na to?")
        assertTrue("the method opens", methodOpen())
        assertEquals("one goal marked: the first part's", 1, nowCount())
        tap("Áno prvá")
        tap("Ďalšia časť")
        assertTrue(shows("Druhá otázka?"))
        assertFalse("the first part's hint is closed", shows("Rada prvej časti."))
        assertFalse("the method is closed", methodOpen())
        help("Nápoveda")
        assertTrue("the second part's own hint", shows("Rada druhej časti."))
        help("Ako na to?")
        assertTrue(methodOpen())
        assertEquals(1, nowCount())
        tap("Áno druhá")
        tap("Ďalšia časť")
        assertTrue(shows("Tretia otázka?"))
        assertFalse("a part without its own hint offers none", onAllNodes(hasText("Nápoveda") and hasClickAction()).fetchSemanticsNodes().isNotEmpty())
    }

    @Test fun theMethodOpensOnThePartsGoal() = runComposeUiTest {
        setContent { TabulaTheme(ThemeMode.Light) { ExerciseView(ex, false, Frame(1, 1, {}, {})) { _, _ -> } } }
        waitForIdle()
        tap("Áno prvá")
        tap("Ďalšia časť")
        tap("Ako na to?")
        // the second part works on goal 2: that row carries "Teraz", goal 1 does not
        val marked = onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Teraz"), useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals(1, marked.size)
        val texts = onAllNodes(hasText("2", substring = false), useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("goal number 2 lies inside the marked row", texts.any { t -> marked[0].boundsInRoot.contains(t.boundsInRoot.center) })
    }

    /**
     * The help of a problem on the keypad is above the keypad at a phone's width, beside or right under the question
     * (design review 18. 11.: at 360 dp the keypad covered it), and it opens the hint whether it is shown straight or folded
     * into "Pomoc".
     */
    @Test fun helpAboveTheKeypadAt320() = helpAboveTheKeypad(320)
    @Test fun helpAboveTheKeypadAt360() = helpAboveTheKeypad(360)
    @Test fun helpAboveTheKeypadAt411() = helpAboveTheKeypad(411)

    private fun helpAboveTheKeypad(w: Int) {
        androidx.compose.ui.test.runDesktopComposeUiTest(width = w, height = 780) {
            val rea = Generators.getValue("dml-tc-rea")(Random(3))
            setContent { TabulaTheme(ThemeMode.Light) { ExerciseView(rea, false, Frame(1, 1, {}, {})) { _, _ -> } } }
            waitForIdle()
            val key = onAllNodes(androidx.compose.ui.test.hasTestTag("key:1")).fetchSemanticsNodes().single().boundsInRoot
            val link = onAllNodes((hasText("Pomoc") or hasText("Nápoveda")) and hasClickAction()).fetchSemanticsNodes().first().boundsInRoot
            assertTrue("$w dp: the help ($link) stands above the keypad ($key)", link.bottom <= key.top && link.top >= 0f)
            help("Nápoveda")
            assertTrue("$w dp: the hint opened, its link is gone", onAllNodes(hasText("Nápoveda") and hasClickAction()).fetchSemanticsNodes().isEmpty())
        }
    }
}

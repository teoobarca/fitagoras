package dev.hamper.tabula.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.store
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The paper review after writing (Teo, 18. 11.): a part opens at its top with the whole model solution, not at its
 * end (an earlier change jumped to the end of the solution when the part opened), and the verdict of the paper is one
 * tap away: "K hodnoteniu papiera" in the footer scrolls to it. A long part of the sample paper, on Teo's phone.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class PaperReviewTest {
    @get:Rule val rule = createComposeRule()

    @Test fun opensAtTheTopAndTheVerdictIsOneTapAway() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        dev.hamper.tabula.platform.AndroidContext.install(ctx)
        val store = Tabula.store(ctx)
        rule.setContent { TabulaTheme(ThemeMode.Light) { MockGradeScreen(paperById("sova-2025"), store) {} } }
        rule.waitForIdle()
        // the part opens at its start: the heading of its model solution on screen
        rule.onNodeWithText("Vzorové riešenie").assertIsDisplayed()
        // the verdict is below the fold of this long part, and the footer goes there in one tap
        rule.onNodeWithText("K hodnoteniu papiera").performClick()
        rule.mainClock.advanceTimeBy(1500); rule.waitForIdle()
        val top = rule.onNodeWithTag(PaperVerdictTag, useUnmergedTree = true).getBoundsInRoot().top
        assertTrue("the verdict's heading in view after the jump ($top)", top.value in 0f..891f)
    }
}

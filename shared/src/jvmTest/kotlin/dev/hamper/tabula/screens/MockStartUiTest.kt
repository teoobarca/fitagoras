package dev.hamper.tabula.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.hamper.tabula.Env
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.platform.Platform
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The whole test on time promises its clock in a notification and on the lock screen: on Android 13+ that needs the
 * notification permission, which a fresh install never had asked (found on the emulator, 19. 11.). "Spustiť" asks for it
 * once when it is missing and starts the test whatever the answer; with it granted it starts at once.
 */
@OptIn(ExperimentalTestApi::class)
class MockStartUiTest {
    private class FakePlatform(val allowed: Boolean, val answer: Boolean) : Platform {
        var asked = 0
        override fun remindersAllowed() = allowed
        @Composable
        override fun rememberReminderPermission(onResult: (Boolean) -> Unit): () -> Unit = { asked++; onResult(answer) }
    }

    private fun run(allowed: Boolean, answer: Boolean, expectAsked: Int) = runComposeUiTest {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
        val paper = Courses.first { it.id == "dml" }.papers.first()
        val platform = FakePlatform(allowed, answer)
        var started = 0
        setContent { TabulaTheme(ThemeMode.Light) { CompositionLocalProvider(LocalPlatform provides platform) { MockIntroScreen(paper, {}) { started++ } } } }
        waitForIdle()
        onAllNodes(hasText("Spustiť", substring = true) and hasClickAction()).onFirst().performClick(); waitForIdle()
        assertEquals("asked for the notification permission", expectAsked, platform.asked)
        assertEquals("the test started once", 1, started)
    }

    @Test fun asksWhenMissingAndStartsOnYes() = run(allowed = false, answer = true, expectAsked = 1)
    @Test fun asksWhenMissingAndStartsOnNo() = run(allowed = false, answer = false, expectAsked = 1)
    @Test fun grantedStartsWithoutAsking() = run(allowed = true, answer = true, expectAsked = 0)
}

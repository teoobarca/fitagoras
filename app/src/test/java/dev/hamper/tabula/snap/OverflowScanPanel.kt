package dev.hamper.tabula.snap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.ui.StepFrame
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.Text
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * A step whose content is longer than the screen, with the verdict panel open (Teo, 18. 11.: a solution list cut flat
 * after its fourth line, an empty band under it, then the panel): the step's window reaches down to the panel, so no
 * band stays between them, and the content's end scrolls into view above the panel. The resting footer (the keypad
 * of a numbers answer) is taller than the panel, the case that left the band. 360 and 411 dp, font 100 and 130 %.
 * Run with the overflow check: `-Psnap.test=OverflowScan*`.
 */
@Config(qualifiers = "w411dp-h914dp-xxhdpi")
class OverflowScanPanel : Shots() {
    @get:Rule val rule = createComposeRule()

    @Test fun longContentReadsToItsEndAboveTheVerdictPanel() {
        var width by mutableStateOf(360)
        var scale by mutableStateOf(1f)
        var verdict by mutableStateOf(false)
        rule.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, scale)) {
                TabulaTheme(ThemeMode.Light) {
                    Box(Modifier.size(width.dp, 800.dp)) {
                        StepFrame(18, 18, {}, footerTint = if (verdict) T.c.badTint else null, footer = {
                            if (verdict) Text("Hotovo, s chybou.", Modifier.testTag("panel")) else Box(Modifier.fillMaxWidth().height(300.dp))
                        }) {
                            Column(Modifier.testTag("content")) {
                                repeat(40) { Text("Riadok riešenia číslo $it, dosť dlhý, aby sa zalomil na úzkom telefóne.") }
                                Text("Koniec riešenia", Modifier.testTag("end"))
                            }
                        }
                    }
                }
            }
        }
        val bad = ArrayList<String>()
        for (w in listOf(360, 411)) for (s in listOf(1f, 1.3f)) {
            rule.runOnIdle { width = w; scale = s; verdict = false }
            rule.waitForIdle()
            rule.runOnIdle { verdict = true }
            rule.mainClock.advanceTimeBy(1500)
            rule.waitForIdle()
            val density = rule.onNodeWithTag("content").fetchSemanticsNode().layoutInfo.density.density
            // the panel's top: the text's top less the panel's top padding (20 dp) is at most where the window ends
            val panelText = rule.onNodeWithTag("panel", useUnmergedTree = true).fetchSemanticsNode().layoutInfo.coordinates.boundsInWindow()
            val shown = rule.onNodeWithTag("content").fetchSemanticsNode().layoutInfo.coordinates.boundsInWindow()
            val band = (panelText.top - 20 * density - shown.bottom) / density
            if (band > 8f) bad += "$w dp, ${(s * 100).toInt()} %: ${band.toInt()} dp of empty page between the content and the panel"
            rule.onNodeWithTag("end").performScrollTo()
            rule.waitForIdle()
            val end = rule.onNodeWithTag("end").fetchSemanticsNode().layoutInfo.coordinates.boundsInWindow()
            if (end.bottom > panelText.top - 20 * density + 1) bad += "$w dp, ${(s * 100).toInt()} %: the content's end stays under the panel"
        }
        assertTrue(bad.joinToString("\n"), bad.isEmpty())
    }
}

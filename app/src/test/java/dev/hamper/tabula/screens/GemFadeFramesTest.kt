package dev.hamper.tabula.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import dev.hamper.tabula.data.allGemWalks
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Teo (17. 11.): "when I switch steps, the numbers that change kind of blink". Frame by frame through a step and back:
 * the first frame after a step is still the matrix before (the blink was the new matrix for one frame, then the old one,
 * then the cross-fade), the middle frames differ, the last is the new matrix, and the stage never moves. The frames are
 * written to build/gem-frames for a look (a strip, docs/CONTENT.md "GEM krok po kroku").
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class GemFadeFramesTest {
    @get:Rule val rule = createComposeRule()

    private fun pixels(img: ImageBitmap): IntArray { val b = img.asAndroidBitmap(); return IntArray(b.width * b.height).also { b.getPixels(it, 0, b.width, 0, 0, b.width, b.height) } }
    private fun shot(name: String): ImageBitmap {
        val img = rule.onNodeWithTag("gem-stage", useUnmergedTree = true).captureToImage()
        val dir = File(System.getProperty("gem.frames") ?: "build/gem-frames").also { it.mkdirs() }
        File(dir, "$name.png").outputStream().use { img.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        return img
    }

    @Test fun aStepIsACalmCrossFadeWithoutABlink() {
        val walk = allGemWalks(seeds = 0).first { it.first.startsWith("ln/") && it.first.endsWith("explore") }.second
        var k by mutableIntStateOf(0)
        rule.setContent { TabulaTheme(ThemeMode.Light) { Box(Modifier.width(411.dp)) { GemStage(walk, k, big = true, modifier = Modifier.testTagStage()) } } }
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false
        val before = pixels(shot("0-before"))
        k = 1
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        // this frame is composed with step 1 (TalkBack already reads its operation) and still shows the matrix before
        val said = rule.onNodeWithTag("gem-matrix", useUnmergedTree = true).fetchSemanticsNode().config[SemanticsProperties.ContentDescription].joinToString()
        assertTrue("the frame after the tap is composed with the step: $said", said.startsWith(walk.moves[0].kindTex))
        val first = pixels(shot("1-first-frame"))
        assertTrue("the first frame after a step must still show the matrix before (no jump, no blink)", first.contentEquals(before))
        val mids = listOf(70L, 70L, 70L).mapIndexed { i, dt -> rule.mainClock.advanceTimeBy(dt); pixels(shot("${2 + i}-mid-${(i + 1) * 70}ms")) }
        for (m in mids) assertFalse("a middle frame is in between", m.contentEquals(before))
        rule.mainClock.advanceTimeBy(400); rule.waitForIdle()
        val after = pixels(shot("5-after"))
        assertFalse(after.contentEquals(before))
        // back: the reverse, starting again from what is on the screen
        k = 0
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        assertTrue("back starts from the matrix on the screen", pixels(shot("6-back-first-frame")).contentEquals(after))
        rule.mainClock.advanceTimeBy(140); shot("7-back-mid")
        rule.mainClock.advanceTimeBy(400); rule.waitForIdle()
        assertTrue("back ends at the matrix before", pixels(shot("8-back-after")).contentEquals(before))
        // a quick Späť halfway through a step: the same change in reverse, from exactly what is on the screen
        k = 1
        rule.mainClock.advanceTimeByFrame(); rule.mainClock.advanceTimeBy(100)
        val halfway = pixels(shot("9-halfway"))
        k = 0
        Snapshot.sendApplyNotifications()
        rule.mainClock.advanceTimeByFrame()
        assertTrue("turning back halfway does not jump", pixels(shot("10-turned-back")).contentEquals(halfway))
        rule.mainClock.advanceTimeBy(400); rule.waitForIdle()
        assertTrue("and ends at the matrix before", pixels(shot("11-turned-back-after")).contentEquals(before))
    }
}

private fun Modifier.testTagStage() = this.then(Modifier.testTag("gem-stage"))

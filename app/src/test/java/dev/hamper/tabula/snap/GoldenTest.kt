package dev.hamper.tabula.snap

import com.github.takahirom.roborazzi.RoborazziTaskType
import org.junit.Test
import java.io.File

/**
 * `./gradlew :app:snapGolden`: a few screens that change only on purpose, compared with app/src/test/snapshots.
 * A difference fails and leaves `<name>_compare.png` (reference | diff | new) in app/build/outputs/roborazzi.
 * When the change is intended: `-Psnap.record=true` rewrites the references; commit them with the change.
 * Not part of the ordinary test run: a deliberate content or style change would fail it for everyone.
 */
class GoldenTest : Shots() {
    private val dir = File(System.getProperty("snap.golden") ?: "src/test/snapshots")
    private val light = Look(dark = false, widthDp = Pixel.first, heightDp = Pixel.second, fontScale = 1f)
    private val dark = light.copy(dark = true)

    private fun golden(name: String, look: Look, route: String) {
        task = if (System.getProperty("snap.record") == "true") RoborazziTaskType.Record else RoborazziTaskType.Verify
        try { shoot(File(dir, "$name-${look.tag}.png"), look) { AppAt(route) } } finally { task = RoborazziTaskType.Record }
    }

    /** Text with math in both fonts, a source card. */
    @Test fun definition() { golden("definition", light, "lesson:dml-tc-delitelnost:2"); golden("definition", dark, "lesson:dml-tc-delitelnost:2") }

    /** Canvas drawing: the cube of Z_2^3 with math labels. */
    @Test fun canvas() = golden("cube", light, "lesson:podpriestory-zp:8")

    /** Answer buttons with math, narrow phone, large font. */
    @Test fun narrowLargeFont() = golden("ask", light.copy(widthDp = Narrow.first, heightDp = Narrow.second, fontScale = 1.3f), "lesson:dml-tc-delitelnost:4")
}

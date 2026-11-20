package dev.hamper.tabula.ui

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The register of moments (ui/Sound.kt, ui/Feel.kt): every moment that sounds has its haptic too, moving around is
 * silent, the series climbs and a mistake resets it, the switch, and every sound's file is shipped.
 */
class SoundTest {
    private val played = mutableListOf<Sfx>()

    @After fun reset() { Sounds.player = null; Sounds.on = { true }; Series.n = 0 }

    /** Moments that only move around: a haptic, no sound (Teo, 18. 11.: a tap sounds only where an answer is chosen). */
    private val silent = setOf(Buzz.Tap, Buzz.Tick, Buzz.Expand)

    @Test fun everyMomentHasItsHapticAndItsSound() {
        for (b in Buzz.entries) {
            assertTrue("$b has no haptic", b.rich.isNotEmpty() || b.fallback != 0)
            if (b in silent) assertEquals("$b moves around and must stay silent", null, Sounds.of(b))
            else assertTrue("$b has no sound", Sounds.of(b) != null)
        }
        // a tap where an answer is chosen, the button its own, never the verdict's
        for (b in listOf(Buzz.Choose, Buzz.Select, Buzz.Key, Buzz.Cell)) assertEquals(Sfx.Tap, Sounds.of(b))
        assertEquals(Sfx.Button, Sounds.of(Buzz.Button))
        assertEquals(Sfx.LessonDone, Sounds.of(Buzz.Finish))
    }

    @Test fun theSeriesClimbsAndAMistakeResetsIt() {
        fun right() = Sounds.of(Series.right())
        assertEquals(listOf(Sfx.Correct, Sfx.Combo1, Sfx.Combo2, Sfx.Combo3, Sfx.Combo4, Sfx.Combo5, Sfx.Combo5), List(7) { right() })
        Series.wrong()
        assertEquals(Sfx.Correct, right())
        // right after a miss: the quieter sound, and the series stays where the miss put it
        Series.wrong()
        assertEquals(Sfx.CorrectAgain, Sounds.of(Buzz.RightAgain))
        assertEquals(0, Series.n)
        assertTrue("nothing louder than correct", Sfx.CorrectAgain.gain <= Sfx.Correct.gain)
    }

    @Test fun theSwitch() {
        Sounds.player = SfxPlayer { played += it }
        Sounds.on = { false }
        Sounds.play(Sfx.Correct)
        assertTrue(played.isEmpty())
        Sounds.on = { true }
        Sounds.play(Sfx.Correct)
        assertEquals(listOf(Sfx.Correct), played)
    }

    /** Every sound's file is in the app (app/src/main/assets/zvuky), with its provenance note. */
    @Test fun everyFileIsShipped() {
        val dir = listOf(java.io.File("../app/src/main/assets/zvuky"), java.io.File("app/src/main/assets/zvuky")).first { it.isDirectory }
        val have = dir.list()!!.toSet()
        val missing = Sfx.entries.map { it.file + ".wav" }.filter { it !in have }
        assertTrue("missing: $missing", missing.isEmpty())
        val povod = dir.resolve("POVOD.md").readText()
        assertTrue("every sound named in POVOD.md", Sfx.entries.map { it.file }.filter { !it.startsWith("combo_") }.all { "| $it |" in povod })
    }
}

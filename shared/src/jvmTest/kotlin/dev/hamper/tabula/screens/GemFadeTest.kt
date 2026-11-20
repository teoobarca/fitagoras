package dev.hamper.tabula.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Teo (17. 11.): the changing numbers blinked. The step change is a function of progress alone; these are its rules. */
class GemFadeTest {
    private val ps = (0..100).map { it / 100f }

    @Test fun aChangedCellIsNeverEmptyNorDoubled() {
        for (p in ps) assertEquals("p=$p", 1f, GemFade.old(p) + GemFade.new(p), 1e-6f)
    }

    @Test fun itStartsAtTheMatrixBeforeAndEndsAtTheNewOne() {
        assertEquals(1f, GemFade.old(0f)); assertEquals(0f, GemFade.new(0f))
        assertEquals(0f, GemFade.old(1f)); assertEquals(1f, GemFade.new(1f))
    }

    @Test fun whatStaysDoesNotFlicker() {
        for (p in ps) assertEquals(1f, GemFade.presence(was = true, now = true, p = p))
        for (p in ps) assertEquals(0f, GemFade.presence(was = false, now = false, p = p))
        for (p in ps) assertEquals(1f, GemFade.presence(true, false, p) + GemFade.presence(false, true, p), 1e-6f)
    }

    @Test fun calmAndReadableTiming() {
        assertTrue(GemFade.MS in 250..300)
        assertTrue(GemFade.CALM_MS < GemFade.MS)
    }
}

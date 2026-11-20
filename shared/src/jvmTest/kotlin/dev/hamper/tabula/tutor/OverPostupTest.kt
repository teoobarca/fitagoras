package dev.hamper.tabula.tutor

import dev.hamper.tabula.tutor.harness.behK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The engine's gate K on a sample (the full run, ≥ 10 000 cases, is `scripts/tutor.sh data k` before a hand-over):
 * every generated chain with one known mistake is found at exactly its matrix and rows, a right one is never marked.
 */
class OverPostupTest {
    @Test fun generatedChains() {
        val v = behK(1500, seed = 7)
        assertTrue("generátory s GEM: ${v.generatory}", v.generatory.size >= 8)
        assertEquals(v.zlyhania.joinToString("\n"), v.pripady, v.spravne)
        assertEquals("nahlásená bunka musí byť tá pravá", 0, v.bunkyChybne)
    }
}

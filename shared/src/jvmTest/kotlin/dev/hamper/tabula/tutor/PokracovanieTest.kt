package dev.hamper.tabula.tutor

import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A chain the reader split in two blocks because it went on on the next line (Teo's page of 18. 11., 104347482): the
 * halves are one chain, so the verdict is about the whole, never "the last matrix is not finished" for the first half.
 */
class PokracovanieTest {
    private fun m(vararg r: String) = PrepisMatica(r.map { row -> row.trim().split(Regex("\\s+")).map { Citanie(it) } })
    private val a = m("1 1 3 0", "2 0 2 1", "0 0 -2 1", "1 -1 3 -1")
    private val b = m("1 1 3 0", "0 -2 -4 1", "0 0 -2 1", "0 -2 0 -1")
    private val c = m("1 1 3 0", "0 -2 -4 1", "0 0 -2 1", "0 0 4 -2")
    private val d = m("1 1 3 0", "0 2 4 -1", "0 0 2 -1", "0 0 0 0")
    private val uloha = GemUloha(Field.R, Mat(listOf(listOf(1, 1, 3, 0), listOf(2, 0, 2, 1), listOf(0, 0, -2, 1), listOf(1, -1, 3, -1)).map { r -> r.map { Frac.of(it.toLong()) } }))
    private fun prepis(vararg bl: List<PrepisMatica>) = Prepis(listOf(Strana(1, Strana.Citatelnost.OK, 0, null)), bl.map { Blok("gem", 1, null, GemPrepis(it)) })

    @Test fun aChainSplitInTwoIsJoined() {
        val v = vyhodnot(listOf(uloha), prepis(listOf(a, b), listOf(c, d)))
        assertEquals(1, v.prepis.gemy.size)
        assertEquals(Verdikt.Stav.BEZ_CHYBY, v.hlavny?.stav)
    }

    /** A redo from the start is no continuation: the pivots would move left, so the two attempts stay apart. */
    @Test fun aRedoFromTheStartStaysApart() {
        val v = vyhodnot(listOf(uloha), prepis(listOf(a, b, c, d), listOf(a, b)))
        assertEquals(2, v.prepis.gemy.size)
    }
}

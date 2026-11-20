package dev.hamper.tabula.data.la1

import dev.hamper.tabula.la.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.hamper.tabula.data.spanElements
import kotlin.random.Random

/** The numbers written by hand in the kapitola 4 lessons, recomputed, and the exam systems solved twice. */
class Chapter4ContentTest {
    private val R = Field.R

    @Test fun small_frobenius_example() {
        val a = R.mat(listOf(listOf(1, 2, -1), listOf(2, 4, 1), listOf(1, 2, 2)))
        val s = R.solveAll(a, vec(2, 7, 5))
        assertEquals(2, s.rankA); assertEquals(2, s.rankAb)
        assertEquals(vec(3, 0, 1), s.particular); assertEquals(listOf(vec(-2, 1, 0)), s.kernel)
        assertNull(R.solveAll(a, vec(2, 7, 6)).particular)
        val e = R.echelon(Mat(a.rows.indices.map { a.rows[it] + listOf(vec(2, 7, 5)[it]) }, 3), 3)
        assertEquals(R.mat(listOf(listOf(1, 2, -1, 2), listOf(0, 0, 3, 3), listOf(0, 0, 0, 0)), 3), e.result)
    }

    @Test fun example_4_12_pivots_and_textbook_basis() {
        val a = R.mat(listOf(listOf(1, 3, 2, 0, 3), listOf(1, 1, 1, -1, 5), listOf(2, 8, 5, 3, 7), listOf(3, 9, 6, 2, 12)))
        val s = R.solveAll(a, vec(3, -2, 13, 11))
        assertEquals(listOf(0, 1, 3), s.bound); assertEquals(listOf(2, 4), s.free)
        for (z in listOf(vec(-1, -1, 2, 0, 0), vec(-33, 7, 0, -6, 4))) assertTrue(R.solves(a, vec(0, 0, 0, 0), z))
    }

    @Test fun exam_frobenius_2025_and_bolzano_bonus() {
        val f = Field.Zp(5)
        val s = f.solveAll(FrobeniusA, FrobeniusB)
        assertEquals(4, s.rankA); assertEquals(4, s.rankAb); assertEquals(listOf(1, 3, 5), s.free)
        assertTrue(f.solves(FrobeniusA, FrobeniusB, s.particular!!))
        for (z in s.kernel) assertTrue(f.solves(FrobeniusA, List(4) { Frac.ZERO }, z))
        // Bolzano 2. 2. 2026: four pivots left of the bar, every right side is solvable; with b4 = 0 there are 5^3
        val bol = f.mat(listOf(listOf(2, 0, 2, 1, 1, 0, 1), listOf(0, 0, 3, 1, 0, 1, 1), listOf(0, 0, 0, 0, 2, 4, 1), listOf(0, 0, 0, 0, 0, 0, 2)))
        assertEquals(4, f.rankOf(bol))
        var count = 0
        for (b1 in 0..4) for (b2 in 0..4) for (b3 in 0..4) if (f.solveAll(bol, f.vec(listOf(b1, b2, b3, 0))).particular != null) count++
        assertEquals(125, count)
    }

    @Test fun exam_hilbert_variety_in_z3() {
        val f = Field.Zp(3)
        val s = f.solveAll(HilbertVA, HilbertVB)
        assertEquals(2, s.rankA); assertEquals(3, s.kernel.size)
        assertTrue(f.solves(HilbertVA, HilbertVB, s.particular!!))
    }

    @Test fun example_4_33_textbook_equations() {
        val a = R.mat(listOf(listOf(0, -1, 0, 1), listOf(-3, 1, 2, 0)))
        val b = vec(-2, 1)
        assertEquals(b, R.mulVec(a, vec(1, 2, 1, 0)))
        for (p in listOf(vec(1, 2, 1, 0), vec(2, 3, 2, 1), vec(3, 2, 4, 0))) assertTrue(R.solves(a, b, p))
        for (z in listOf(vec(0, -1, 0, 1), vec(-3, 1, 2, 0))) { assertTrue(R.solves(R.mat(listOf(listOf(1, 1, 1, 1), listOf(2, 0, 3, 0))), vec(0, 0), z)) }
    }

    @Test fun marast_14158_points_on_both_systems() {
        val f = Field.Zp(5)
        val (a0, b0, c0) = MarastPts
        val (am, bv) = f.equationsOf(a0, listOf(f.plus(b0, f.scale(-Frac.ONE, a0)), f.plus(c0, f.scale(-Frac.ONE, a0))))
        val marast = f.mat(listOf(listOf(1, 4, 0, 0), listOf(0, 4, 1, 4)))
        for (p in MarastPts) { assertTrue(f.solves(am, bv, p)); assertTrue(f.solves(marast, f.vec(listOf(0, 4)), p)) }
        assertEquals(2, f.rankOf(am))
    }

    /**
     * la1-k4-sustava: the judge of the rows of (A|b) for a + P accepts exactly the systems whose solution set is a + P,
     * by enumerating Z_p^n: the engine's own answer (Postup 4.30), the same answer rescaled or recombined, and broken
     * copies (one cell, b = θ, a repeated row).
     */
    @Test fun variety_system_judge_matches_brute_force() {
        val r = Random(23)
        var checked = 0
        while (checked < 150) {
            val p = listOf(3, 5, 7).random(r); val f = Field.Zp(p)
            val n = if (p == 7) 4 else r.nextInt(4, 6); val k = r.nextInt(1, 4)
            val basis = List(k) { f.vec(List(n) { r.nextInt(p) }) }
            if (!f.isLN(basis)) continue
            val a = if (r.nextBoolean()) null else f.vec(List(n) { r.nextInt(p) })
            val shift = a ?: List(n) { Frac.ZERO }
            val w = f.spanElements(basis).map { f.plus(it, shift) }.toSet()
            val (am, bv) = f.equationsOf(shift, basis)
            val good = am.rows.mapIndexed { i, row -> row + bv[i] }
            fun nz() = Frac.of(r.nextInt(1, p).toLong())
            val i = r.nextInt(good.size); val j = (i + 1) % good.size; val cell = r.nextInt(n + 1)
            val candidates = listOf(
                good,
                good.mapIndexed { t, row -> if (t != i) row else f.plus(f.scale(nz(), row), f.scale(nz(), good[j])).takeIf { good.size > 1 } ?: f.scale(nz(), row) },
                good.mapIndexed { t, row -> if (t != i) row else row.mapIndexed { c, x -> if (c == cell) f.norm(x + Frac.ONE) else x } },
                good.map { row -> row.take(n) + Frac.ZERO },
                good.mapIndexed { t, row -> if (t == j && good.size > 1) good[i] else row },
                good.mapIndexed { t, row -> if (t != i) row else f.vec(List(n) { r.nextInt(p) }).let { v -> v + f.mulVec(Mat(listOf(v)), shift)[0] } },
            )
            for (c in candidates) {
                val rows = c.map { if (a == null) it.take(n) else it }
                val sols = HashSet<Vec>()
                for (code in 0 until pow(p, n)) {
                    val x = List(n) { t -> Frac.of(((code / pow(p, t)) % p).toLong()) }
                    if (rows.all { row -> f.mulVec(Mat(listOf(row.take(n))), x)[0] == (if (a == null) Frac.ZERO else row[n]) }) sols += x
                }
                assertEquals("$basis $a $rows", sols == w, judgeSustava(f, basis, a, rows.map { listOf(it) }).ok)
            }
            assertTrue(judgeSustava(f, basis, a, (if (a == null) am.rows else good).map { listOf(it) }).ok)
            checked++
        }
    }

    private fun pow(p: Int, e: Int): Int = (0 until e).fold(1) { acc, _ -> acc * p }

    @Test fun small_facts_in_checks() {
        // (1,0)+<(1,1)> and (2,1)+<(2,2)> are the same line (Věta 4.18, bod 4)
        assertTrue(R.inSpan(listOf(vec(1, 1)), vec(1, 1))); assertEquals(1, R.rank(listOf(vec(1, 1), vec(2, 2))))
        assertEquals(vec(4, 1), R.combo(listOf(1.q(), 2.q()), listOf(vec(2, 1), vec(1, 0))))
        assertTrue(!R.inSpan(listOf(vec(1, 2), vec(2, 4)), vec(3, 1)))
    }
}

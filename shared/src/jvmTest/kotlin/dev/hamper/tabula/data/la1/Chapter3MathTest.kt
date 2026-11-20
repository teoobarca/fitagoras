package dev.hamper.tabula.data.la1

import dev.hamper.tabula.la.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** The matrix code against the textbook's own examples (kapitola 3 and 4) and against the definitions. */
class Chapter3MathTest {
    private val R = Field.R
    private fun m(f: Field, vararg rows: List<Int>) = f.mat(rows.toList())
    private fun r(vararg xs: Int) = xs.toList()

    @Test fun rank_of_example_3_8() {
        assertEquals(3, R.rankOf(m(R, r(2, 1, 2, 1, 0), r(-1, 0, 1, 0, 1), r(3, 1, 0, 1, 1), r(6, 2, 1, 2, 0))))
        assertEquals(2, R.rankOf(m(R, r(1, 3, 1), r(2, 1, -1)))) // Příklad 3.3
    }

    @Test fun inverse_of_example_3_38_in_z7() {
        val f = Field.Zp(7)
        val a = m(f, r(1, 3, 2), r(2, 3, 1), r(3, 4, 5))
        assertEquals(m(f, r(2, 0, 2), r(0, 3, 5), r(3, 6, 2)), f.inverse(a))
        assertEquals(identity(3), f.times(a, f.inverse(a)!!))
    }

    @Test fun inverse_both_sides_and_singular() {
        val rnd = Random(1)
        repeat(200) {
            val f: Field = if (it % 2 == 0) R else Field.Zp(listOf(3, 5, 7)[it % 3])
            val n = 2 + it % 3
            val a = f.mat(List(n) { List(n) { rnd.nextInt(-3, 4) } })
            val inv = f.inverse(a)
            if (f.rankOf(a) == n) {
                assertEquals(identity(n), f.times(a, inv!!)); assertEquals(identity(n), f.times(inv, a))
            } else assertNull(inv)
        }
    }

    @Test fun question_after_definition_3_23() {
        // A = (1 1; -1 0): B = (1 1; 2 0) is not its inverse, C = (0 -1; 1 1) is
        val a = m(R, r(1, 1), r(-1, 0))
        assertEquals(m(R, r(3, 1), r(-1, -1)), R.times(a, m(R, r(1, 1), r(2, 0))))
        assertEquals(m(R, r(0, -1), r(1, 1)), R.inverse(a))
    }

    @Test fun homogeneous_example_4_6_and_particular_4_12() {
        val a = m(R, r(1, 3, 2, 0, 3), r(1, 1, 1, -1, 5), r(2, 8, 5, 3, 7), r(3, 9, 6, 2, 12))
        val s0 = R.kernelBasis(a)
        assertEquals(listOf(listOf(Frac.of(-1, 2), Frac.of(-1, 2), Frac.ONE, Frac.ZERO, Frac.ZERO), listOf(Frac.of(-33, 4), Frac.of(7, 4), Frac.ZERO, Frac.of(-3, 2), Frac.ONE)), s0)
        val s = R.solveAll(a, vec(3, -2, 13, 11))
        assertEquals(vec(-3, 2, 0, 1, 0), s.particular)
        assertEquals(3, s.rankA); assertEquals(3, s.rankAb); assertEquals(listOf(2, 4), s.free)
    }

    @Test fun no_solution_when_ranks_differ() {
        val s = R.solveAll(m(R, r(1, 1), r(2, 2)), vec(1, 3))
        assertNull(s.particular); assertEquals(1, s.rankA); assertEquals(2, s.rankAb)
    }

    @Test fun subspace_and_variety_to_equations_4_25_4_27() {
        val f = Field.Zp(5)
        val us = listOf(f.vec(listOf(1, 1, 0, 2)), f.vec(listOf(2, 1, 1, 1)))
        assertEquals(listOf(f.vec(listOf(4, 1, 1, 0)), f.vec(listOf(1, 2, 0, 1))), f.kernelBasis(Mat(us)))
        val (a, b) = f.equationsOf(f.vec(listOf(1, 2, 0, 3)), us)
        assertEquals(f.vec(listOf(1, 3)), b)
        assertEquals(2, f.rankOf(a))
    }

    @Test fun example_4_33_and_marast_14158() {
        val (a, b) = R.equationsOf(vec(1, 2, 1, 0), listOf(vec(1, 1, 1, 1), vec(2, 0, 3, 0)))
        for (p in listOf(vec(1, 2, 1, 0), vec(2, 3, 2, 1), vec(3, 2, 4, 0), vec(1, 4, 0, 2))) assertTrue(R.solves(a, b, p))
        assertTrue(!R.solves(a, b, vec(1, 1, -1, 2))) // Otázka after Příklad 4.33
        // MARAST 14158: the plane through (1,1,1,1), (1,1,2,2), (2,2,2,1) in Z_5^4 is x+4y=0, 4y+z+4u=4
        val f = Field.Zp(5)
        val p0 = f.vec(listOf(1, 1, 1, 1))
        val (a5, b5) = f.equationsOf(p0, listOf(f.vec(listOf(0, 0, 1, 1)), f.vec(listOf(1, 1, 1, 0))))
        val marast = f.mat(listOf(listOf(1, 4, 0, 0), listOf(0, 4, 1, 4)))
        val mb = f.vec(listOf(0, 4))
        for (x in f.solveAll(a5, b5).let { s -> listOf(s.particular!!) + s.kernel.map { k -> f.plus(s.particular!!, k) } }) assertTrue(f.solves(marast, mb, x))
        assertEquals(2, f.rankOf(a5))
    }

    @Test fun rank_with_parameter_hilbert_2026() {
        // Zkouška 26. 1. 2026 (Hilbert), př. 2: h(A) = 3 except a = 1 and a = -3, where it is 2
        fun p(b: Int, k: Int = 0) = Poly.lin(b, k)
        val rows = listOf(listOf(p(-1), p(1, 1), p(-2, 1)), listOf(p(1), p(-2), p(1)), listOf(p(0), p(-1, -1), p(2)))
        val res = paramRank(rows)!!
        assertEquals(3, res.generic)
        assertEquals(mapOf(Frac.of(-3) to 2, Frac.ONE to 2), res.special)
        for (a in listOf(-3, -1, 0, 1, 2, 5)) assertEquals(rankAt(rows, a.q()), if (a.q() in res.special) res.special[a.q()] else res.generic)
        // inverse for a = 0
        val a0 = Mat(rows.map { r -> r.map { it.eval(Frac.ZERO) } })
        assertEquals(identity(3), R.times(a0, R.inverse(a0)!!))
    }
}

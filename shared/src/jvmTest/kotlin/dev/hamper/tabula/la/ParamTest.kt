package dev.hamper.tabula.la

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Problem 1 of the past tests, solved by the app's GEM with a parameter and checked by plain rank. */
class ParamTest {
    private fun c(vararg xs: Int) = xs.map { Poly.of(it) }
    private val a = Poly.A

    private fun check(kind: ParamKind, vs: List<PVec>, x: PVec? = null): ParamResult {
        val r = paramSolve(kind, vs, x)
        assertNotNull("GEM s parametrom nevyšiel pre $vs", r)
        r!!
        val probes = r.critical + (-6..6).map { it.q() } + listOf(Frac.of(1, 2), Frac.of(-7, 3))
        for (t in probes) assertEquals("a = $t", paramHoldsByRank(kind, vs, x, t), r.holds(t))
        return r
    }

    @Test fun slon_2024() { // ((3,-1,3),(0,a,1),(1,1,0)) báze R³
        val r = check(ParamKind.BASIS, listOf(c(3, -1, 3), listOf(Poly.of(0), a, Poly.of(1)), c(1, 1, 0)))
        assertEquals(listOf(Frac.of(-4, 3)), r.critical)
    }

    @Test fun sova_2025() { // ((0,2,1,2),(2,-1,a+1,1),(-1,2,2,1)) LN v R⁴
        val r = check(ParamKind.LN, listOf(c(0, 2, 1, 2), listOf(Poly.of(2), Poly.of(-1), Poly.lin(1, 1), Poly.of(1)), c(-1, 2, 2, 1)))
        assertTrue(r.critical.size <= 1)
    }

    @Test fun veverka_2024() { // ((2,0,-1),(1,2,a),(2,3,2)) báze R³
        check(ParamKind.BASIS, listOf(c(2, 0, -1), listOf(Poly.of(1), Poly.of(2), a), c(2, 3, 2)))
    }

    @Test fun medved_2024() { // (2,-1,a) ∈ ⟨(3,-1,2),(3,2,-1)⟩
        val r = check(ParamKind.SPAN, listOf(c(3, -1, 2), c(3, 2, -1)), listOf(Poly.of(2), Poly.of(-1), a))
        assertEquals(1, r.critical.size)
    }

    @Test fun mys_2023() { // (1,2,a) ∈ ⟨(2,1,2),(3,2,1),(2,2,-2)⟩
        check(ParamKind.SPAN, listOf(c(2, 1, 2), c(3, 2, 1), c(2, 2, -2)), listOf(Poly.of(1), Poly.of(2), a))
    }

    @Test fun sova_2023() { // ((2,1,-1),(1,2,a),(2,3,2)) LN
        check(ParamKind.LN, listOf(c(2, 1, -1), listOf(Poly.of(1), Poly.of(2), a), c(2, 3, 2)))
    }

    @Test fun vcelka_2023() { // ((1,3,-1),(1,2,a),(-1,2,2)) generuje R³
        check(ParamKind.GENERATES, listOf(c(1, 3, -1), listOf(Poly.of(1), Poly.of(2), a), c(-1, 2, 2)))
    }

    @Test fun slepice_2023() { // ((0,1,5),(1,2,a),(-1,0,2),(2,1,1)) generuje R³
        check(ParamKind.GENERATES, listOf(c(0, 1, 5), listOf(Poly.of(1), Poly.of(2), a), c(-1, 0, 2), c(2, 1, 1)))
    }

    @Test fun kocka_2023_note_says_only_a_4() { // (2,-2,a) ∈ ⟨(1,-1,2),(3,2,1),(1,3,-2)⟩, the wiki note: "pouze pro a=4"
        val r = check(ParamKind.SPAN, listOf(c(1, -1, 2), c(3, 2, 1), c(1, 3, -2)), listOf(Poly.of(2), Poly.of(-2), a))
        assertEquals(listOf(4.q()), r.critical)
    }

    @Test fun poly_tex() {
        assertEquals("a+1", Poly.lin(1, 1).tex())
        assertEquals("-a", Poly.lin(0, -1).tex())
        assertEquals("2a-3", Poly.lin(-3, 2).tex())
    }
}

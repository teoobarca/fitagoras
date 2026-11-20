package dev.hamper.tabula.la

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.hamper.tabula.compat.BigInteger
import dev.hamper.tabula.compat.*

/**
 * The model of Z_p^n against brute force, not against its own formula: subspaces are found as the closed subsets of
 * the space (Definice 2.7 checked on every subset) or as the distinct spans of all tuples of vectors.
 */
class FiniteSpaceTest {

    /** Every subset of Z_p^n that is a subspace, by checking the definition on all 2^(p^n) subsets. */
    private fun closedSubsets(s: FiniteSpace): List<Set<ZVec>> {
        val all = s.all()
        require(all.size <= 16)
        return (0 until (1 shl all.size)).map { m -> all.filterIndexed { i, _ -> m shr i and 1 == 1 }.toSet() }.filter(s::isSubspace)
    }

    /** Sizes of subspaces by dimension, from the closed subsets: |P| = p^d, so d = log_p |P|. */
    private fun byDimension(s: FiniteSpace, sets: List<Set<ZVec>>): List<Int> =
        (0..s.n).map { d -> sets.count { it.size == s.vectorsIn(d).toInt() } }

    @Test fun z2_cubed_has_1_7_7_1_subspaces_by_brute_force() {
        val s = FiniteSpace(2, 3)
        val closed = closedSubsets(s)
        assertEquals(16, closed.size)
        assertEquals(listOf(1, 7, 7, 1), byDimension(s, closed))
        // the model's enumeration is exactly the set of closed subsets, each once
        assertEquals(closed.toSet(), s.allSubspaces().map { it.elements }.toSet())
        assertEquals(closed.size, s.allSubspaces().size)
        assertEquals((0..3).map { s.subspaceCount(it).toInt() }, listOf(1, 7, 7, 1))
    }

    @Test fun z3_squared_has_1_4_1_subspaces_by_brute_force() {
        val s = FiniteSpace(3, 2)
        val closed = closedSubsets(s)
        assertEquals(listOf(1, 4, 1), byDimension(s, closed))
        assertEquals(closed.toSet(), s.allSubspaces().map { it.elements }.toSet())
        assertEquals(listOf(1, 4, 1), (0..2).map { s.subspaceCount(it).toInt() })
    }

    @Test fun z2_to_the_4_and_z2_squared_by_brute_force() {
        for (n in 2..4) {
            val s = FiniteSpace(2, n)
            val closed = closedSubsets(s)
            assertEquals("Z_2^$n", byDimension(s, closed), (0..n).map { s.subspaceCount(it).toInt() })
            assertEquals("Z_2^$n", closed.toSet(), s.allSubspaces().map { it.elements }.toSet())
        }
        assertEquals(listOf(1, 15, 35, 15, 1), (0..4).map { FiniteSpace(2, 4).subspaceCount(it).toInt() })
    }

    /** Larger spaces: distinct spans of every d-tuple of vectors, compared with the count and the enumeration. */
    @Test fun distinct_spans_of_all_tuples_match_the_count() {
        for ((p, n) in listOf(5 to 2, 7 to 2, 3 to 3, 5 to 3, 2 to 5)) {
            val s = FiniteSpace(p, n)
            val all = s.all()
            for (d in 1..minOf(2, n)) {
                val tuples = if (d == 1) all.map { listOf(it) } else all.flatMap { a -> all.map { b -> listOf(a, b) } }
                val spans = tuples.filter { s.rank(it) == d }.map { t -> spanByCombinations(s, t) }.toSet()
                assertEquals("Z_$p^$n dim $d", spans.size, s.subspaceCount(d).toInt())
                assertEquals("Z_$p^$n dim $d enumeration", spans, s.subspaces(d).map { it.elements }.toSet())
                assertEquals("Z_$p^$n dim $d each once", s.subspaces(d).size, s.subspaces(d).toSet().size)
                // the ordered LN d-tuples counted one by one
                assertEquals("Z_$p^$n LN $d-tuples", tuples.count { s.isLN(it) }.toBigInteger(), s.orderedLN(d))
            }
        }
    }

    /** The span written out as all combinations, without the echelon form the model uses. */
    private fun spanByCombinations(s: FiniteSpace, vs: List<ZVec>): Set<ZVec> {
        val out = HashSet<ZVec>()
        val total = (0 until vs.size).fold(1) { a, _ -> a * s.p }
        for (k in 0 until total) { var r = k; out += s.combo(List(vs.size) { val x = r % s.p; r /= s.p; x }, vs) }
        return out
    }

    @Test fun different_bases_give_the_same_subspace() {
        val s = FiniteSpace(3, 2)
        assertEquals(s.span(listOf(s.vec(1, 2))), s.span(listOf(s.vec(2, 1))))
        assertNotEquals(s.span(listOf(s.vec(1, 2))), s.span(listOf(s.vec(1, 1))))
        val c = FiniteSpace(2, 3)
        val a = c.span(listOf(c.vec(1, 1, 0), c.vec(0, 1, 1)))
        val b = c.span(listOf(c.vec(1, 0, 1), c.vec(1, 1, 0)))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(setOf(c.zero, c.vec(1, 1, 0), c.vec(0, 1, 1), c.vec(1, 0, 1)), a.elements)
        // the "slanted" plane is closed under addition, a face of the cube not through θ is not a subspace
        assertTrue(c.isSubspace(a.elements))
        assertFalse(c.isSubspace(setOf(c.vec(0, 0, 1), c.vec(1, 0, 1), c.vec(0, 1, 1), c.vec(1, 1, 1))))
        // dependent generators: their span is a line
        assertEquals(1, c.span(listOf(c.vec(1, 1, 1), c.vec(1, 1, 1))).dim)
    }

    @Test fun counts_from_the_textbook() {
        // Skriptá, otázky pri Poznámke 2.67: a plane of Z_5^3 has 5^2 vectors, Z_5^3 has (5^3-1)(5^3-5)/((5^2-1)(5^2-5)) planes
        val s = FiniteSpace(5, 3)
        assertEquals(bigIntegerOf(25), s.vectorsIn(2))
        assertEquals(bigIntegerOf(124L * 120), s.orderedLN(2))
        assertEquals(bigIntegerOf(24L * 20), s.basesOf(2))
        assertEquals(bigIntegerOf(31), s.subspaceCount(2))
        // otázka pri Vete 2.60: ordered bases of Z_5^3
        assertEquals(bigIntegerOf(124L * 120 * 100), s.basesOf(3))
        assertEquals(BigIntegerOne, s.basesOf(0))
        assertEquals(bigIntegerOf(16), FiniteSpace(2, 3).allSubspaceCount())
    }

    @Test fun membership_and_labels() {
        val c = FiniteSpace(2, 3)
        val line = c.span(listOf(c.vec(1, 1, 1)))
        assertTrue(line.contains(c.vec(1, 1, 1))); assertFalse(line.contains(c.vec(1, 0, 0)))
        assertEquals("\\{\\theta,(1,1,1)\\}", line.setTex())
        assertEquals("101", c.label(c.vec(1, 0, 1)))
        assertEquals(c.vec(1, 0, 1), c.vector(c.index(c.vec(1, 0, 1))))
    }
}

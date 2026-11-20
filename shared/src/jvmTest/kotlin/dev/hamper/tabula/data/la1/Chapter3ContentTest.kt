package dev.hamper.tabula.data.la1

import dev.hamper.tabula.la.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The numbers written by hand in the kapitola 3 lessons, recomputed. */
class Chapter3ContentTest {
    private val R = Field.R
    private val Z5 = Field.Zp(5)

    @Test fun rank_example_over_z5() {
        val a = Z5.mat(listOf(listOf(1, 2, 0, 3), listOf(2, 1, 1, 0), listOf(3, 3, 1, 3)))
        val e = Z5.echelon(a)
        assertEquals(Z5.mat(listOf(listOf(1, 2, 0, 3), listOf(0, 2, 1, 4), listOf(0, 0, 0, 0))), e.result)
        assertEquals(2, e.rank)
    }

    @Test fun span_dimension_and_membership_example() {
        val xs = listOf(vec(1, 2, 0, 1), vec(2, 3, 1, 0), vec(0, 1, -1, 2))
        assertEquals(2, R.rank(xs))
        assertTrue(R.inSpan(xs, vec(1, 1, 1, -1)))
        assertEquals(vec(1, 1, 1, -1), R.combo(listOf((-1).q(), 1.q()), xs.take(2)))
        assertEquals(2, R.rank(listOf(vec(1, 2, 0, 1), vec(0, -1, 1, -2), vec(1, 1, 1, -1))))
    }

    @Test fun small_facts_in_checks() {
        assertEquals(2, R.rankOf(R.mat(listOf(listOf(1, 3, 1), listOf(2, 1, -1)))))
        assertEquals(vec(3, 7), R.mulVec(R.mat(listOf(listOf(1, 2), listOf(3, 4))), vec(1, 1)))
        assertEquals(vec(4, 5), R.mulVec(R.mat(listOf(listOf(2, 1), listOf(1, 2))), vec(1, 2)))
        assertEquals(R.mat(listOf(listOf(1, -3, 4))), R.times(R.mat(listOf(listOf(1, -1))), R.mat(listOf(listOf(2, 0, 5), listOf(1, 3, 1)))))
        assertEquals(1, Field.Zp(5).rank(listOf(Field.Zp(5).vec(listOf(1, 2)), Field.Zp(5).vec(listOf(3, 1)))))
        assertEquals(identity(2), R.times(R.mat(listOf(listOf(1, 1), listOf(-1, 0))), R.mat(listOf(listOf(0, -1), listOf(1, 1)))))
        assertEquals(Mat(listOf(vec(0, 0), vec(0, 0))), R.times(R.mat(listOf(listOf(1, 0), listOf(0, 0))), R.mat(listOf(listOf(0, 0), listOf(0, 1)))))
    }

    @Test fun example_3_33_gem_matrix() {
        val p = R.times(R.times(Mat(listOf(vec(1, 0), listOf(Frac.ZERO, Frac.of(1, 3)))), R.mat(listOf(listOf(1, 0), listOf(-3, 1)))), R.mat(listOf(listOf(0, 1), listOf(1, 0))))
        assertEquals(Mat(listOf(vec(0, 1), listOf(Frac.of(1, 3), -Frac.ONE))), p)
        assertEquals(R.mat(listOf(listOf(2, 4, 1), listOf(0, 1, -1))), R.times(p, R.mat(listOf(listOf(6, 15, 0), listOf(2, 4, 1)))))
    }

    @Test fun example_3_38_steps_in_z7() {
        val f = Field.Zp(7)
        val start = f.mat(listOf(listOf(1, 3, 2, 1, 0, 0), listOf(2, 3, 1, 0, 1, 0), listOf(3, 4, 5, 0, 0, 1)))
        // every matrix the lesson writes is reachable by row operations: same row space as the start
        val shown = listOf(
            listOf(listOf(1, 3, 2, 1, 0, 0), listOf(0, 4, 4, 5, 1, 0), listOf(0, 2, 6, 4, 0, 1)),
            listOf(listOf(1, 3, 2, 1, 0, 0), listOf(0, 4, 4, 5, 1, 0), listOf(0, 0, 4, 5, 3, 1)),
            listOf(listOf(1, 3, 0, 2, 2, 3), listOf(0, 4, 0, 0, 5, 6), listOf(0, 0, 4, 5, 3, 1)),
            listOf(listOf(1, 0, 0, 2, 0, 2), listOf(0, 1, 0, 0, 3, 5), listOf(0, 0, 1, 3, 6, 2)),
        ).map { f.mat(it) }
        for (m in shown) assertEquals(3, f.rank(start.rows + m.rows))
    }

    @Test fun hilbert_worked_solution_is_computed() {
        val pc = hilbertCase()
        assertEquals(mapOf((-3).q() to 2, Frac.ONE to 2), pc.res.special)
        assertTrue(rankParamSolution(pc).problems().isEmpty())
    }
}

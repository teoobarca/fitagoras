package dev.hamper.tabula.la

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Answers from the textbook (chap-03, kapitola 2), MARAST lekce 292 and the past tests, recomputed. */
class CoreTest {
    private val R = Field.R
    private val Z5 = Field.Zp(5)
    private val Z7 = Field.Zp(7)

    @Test fun fractions() {
        assertEquals(Frac.of(-4, 3), Frac.of(8, -6))
        assertEquals("-\\tfrac{4}{3}", Frac.of(-4, 3).tex(true))
        assertEquals(Frac.of(3), Z5.norm(Frac.of(-2)))
        assertEquals(Frac.of(2), Z5.inv(Frac.of(3)))
        assertEquals(Frac.of(4), Z5.norm(Frac.of(2, 3))) // 2·3⁻¹ = 2·2 = 4
    }

    @Test fun example_2_28_LN_in_Z5() {
        assertTrue(Z5.isLN(listOf(vec(1, 2, 3), vec(4, 2, 3), vec(3, 4, 2))))
    }

    @Test fun example_2_29_LZ_in_R4() {
        val vs = listOf(vec(1, 0, 1, 0), vec(2, 2, 0, 0), vec(2, 0, 1, 1), vec(1, 1, 2, -2))
        assertFalse(R.isLN(vs))
        // the footnote's "α=12, β=1, γ=4, δ=2" is not a solution: the app must not quote it
        assertEquals(vec(24, 4, 20, 0), R.combo(vec(12, 1, 4, 2), vs))
        assertEquals(vec(0, 0, 0, 0), R.combo(vec(-8, -1, 4, 2), vs))
        val d = R.dependency(vs)!!
        assertTrue(R.combo(d, vs).isZero() && !d.isZero())
    }

    @Test fun example_2_71_coordinates() {
        val b = listOf(vec(1, 1, 1), vec(1, 1, 2), vec(1, 2, 3))
        assertEquals(vec(3, 3, -2), R.coords(b, vec(4, 2, 3)))
    }

    @Test fun textbook_questions_on_coordinates() {
        val b = listOf(vec(1, 0, 0), vec(1, 1, 0), vec(0, 0, 1))
        assertEquals(vec(-1, 2, 3), R.coords(b, vec(1, 2, 3)))
        assertEquals(vec(0, -1, 2), R.fromCoords(b, vec(1, -1, 2)))
    }

    @Test fun marast_292_problem_1() {
        val x = listOf(vec(1, 1, 1, 1), vec(1, 2, 1, 1), vec(1, 1, 2, 1), vec(1, 3, 2, 3))
        assertTrue(R.isLN(x))
        assertEquals(vec(2, -3, 1, 1), R.coords(x, vec(1, 0, 3, 3)))
    }

    @Test fun marast_292_problem_2() {
        val m = listOf(vec(1, 1, 1, 0), vec(1, -1, 0, 1), vec(2, 0, 1, 1), vec(4, 6, 5, -1))
        assertEquals(2, R.rank(m))
        assertEquals(listOf(0, 1), R.pickBasis(m))
    }

    @Test fun marast_13488_complete_to_basis() {
        val add = Z5.completeWithUnitVectors(listOf(vec(1, 0, 1, 1), vec(0, 0, 1, 1)))
        assertEquals(listOf(1, 2), add) // columns give e2,e3; MARAST shows e2,e4 (rows), also a basis
        assertEquals(4, Z5.rank(listOf(vec(1, 0, 1, 1), vec(0, 0, 1, 1), unit(4, 1), unit(4, 3))))
    }

    @Test fun test_2024_slon_problem_2() {
        val start = listOf(Z5.vec(listOf(2, 1, 1, 3)), Z5.vec(listOf(2, 0, 1, 3)))
        val add = Z5.completeWithUnitVectors(start)
        assertEquals(listOf(0, 2), add)
        val b = start + add.map { unit(4, it) }
        assertEquals(4, Z5.rank(b))
        assertEquals(vec(3, 1, 4, 2), Z5.fromCoords(b, vec(1, 3, 0, 0)))
        assertEquals(vec(1, 3, 3, 2), Z5.coords(b, vec(1, 1, 1, 2)))
    }

    @Test fun test_2025_sova_problem_2() {
        val start = listOf(vec(1, 2, 0, 4), vec(3, 3, 0, 1))
        val add = Z5.completeWithUnitVectors(start)
        assertEquals(listOf(1, 2), add)
        val b = start + add.map { unit(4, it) }
        assertEquals(vec(4, 4, 0, 1), Z5.coords(b, vec(1, 0, 1, 0)))
        // y makes the set LZ exactly when y is in the span of the two: 5² vectors, 24 of them nonzero
        assertEquals(24L, pow(5, Z5.rank(start)) - 1)
    }

    @Test fun test_2023_slepice_problem_2_in_Z7() {
        val start = listOf(vec(1, 2, 2, 3), vec(2, 3, 3, 1))
        val add = Z7.completeWithUnitVectors(start)
        val b = start + add.map { unit(4, it) }
        assertEquals(4, Z7.rank(b))
        val c = Z7.coords(b, vec(0, 5, 5, 4))!!
        assertEquals(vec(0, 5, 5, 4), Z7.fromCoords(b, c))
    }

    @Test fun test_2024_medved_problem_2() {
        val m = listOf(vec(1, 4, 0, 1), vec(1, 1, 3, 1), vec(3, 4, 3, 3))
        val r = Z5.rank(m)
        assertEquals(2, r)
        val basis = Z5.pickBasis(m).map { m[it] }
        assertNotNull(Z5.coords(basis, vec(1, 3, 1, 1)))
    }

    @Test fun test_2023_mys_problem_2_counts() {
        val m = listOf(vec(1, 2, 1, 0), vec(1, 4, 0, 2), vec(2, 1, 2, 0), vec(3, 1, 0, 1))
        val d = Z5.rank(m)
        assertEquals(pow(5, d), pow(5, Z5.pickBasis(m).size))
    }

    @Test fun textbook_example_2_37_span_in_Z2_and_Z3() {
        val z2 = Field.Zp(2)
        val all = (0..1).flatMap { a -> (0..1).map { b -> z2.combo(listOf(a.q(), b.q()), listOf(vec(1, 0, 1, 0), vec(0, 1, 1, 0))) } }.toSet()
        assertEquals(setOf(vec(0, 0, 0, 0), vec(0, 1, 1, 0), vec(1, 0, 1, 0), vec(1, 1, 0, 0)), all)
    }

    @Test fun no_solution_is_null() {
        assertNull(R.solve(listOf(vec(1, 2), vec(2, 4)), vec(3, 1)))
    }

    @Test fun echelon_ops_are_replayable() {
        // replaying the recorded operations on the start gives the recorded result: the worked steps don't lie
        val m = Mat.rows(listOf(vec(2, 3, 1), vec(3, -1, 4), vec(5, 2, 5)))
        val e = R.echelon(m)
        var rows = m.rows
        for (s in e.steps) {
            for (op in s.ops) rows = apply(R, rows, op)
            assertEquals(s.after.rows, rows)
        }
        assertEquals(2, e.rank)
    }

    companion object {
        fun apply(f: Field, rows: List<Vec>, op: RowOp): List<Vec> = when (op) {
            is Swap -> rows.toMutableList().also { val t = it[op.i]; it[op.i] = it[op.j]; it[op.j] = t }
            is Combine -> rows.mapIndexed { i, r -> if (i != op.target) r else r.indices.map { j -> f.add(f.mul(op.a, r[j]), f.mul(op.b, rows[op.src][j])) } }
            is Scale -> rows.mapIndexed { i, r -> if (i != op.i) r else r.map { f.mul(op.k, it) } }
            else -> error("not a number op")
        }
    }
}

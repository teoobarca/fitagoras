package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.dml.numbers.euclidWalk
import dev.hamper.tabula.data.dml.numbers.rea
import dev.hamper.tabula.data.dml.numbers.reaWalk
import dev.hamper.tabula.data.dml.numbers.powWalk
import dev.hamper.tabula.data.dml.numbers.powMod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Euclid and the REA as tables (Teo, 16. 11., body 32 a 34): computed, aligned, the numbers that move really move. */
class NumbersWalksTest {
    @Test fun euclidOf254And158IsTheTextbooks() {
        val w = euclidWalk(rea(254, 158))
        assertEquals(listOf("254", "=", "1", "\\cdot", "158", "+", "96"), w.rows.first())
        assertEquals(listOf("4", "=", "2", "\\cdot", "2", "+", "0"), w.rows.last())
        assertEquals(8, w.rows.size)
        assertEquals(listOf("\\gcd(254,158)=2"), w.resultLabel)
        // kurz/dml/kap6 B.2: r_{i-2} = q_{i-1} r_{i-1} + r_i, the first quotient is q_2
        assertEquals(listOf("q_2=\\lfloor 254/158\\rfloor=1"), w.startLabel)
        assertEquals(listOf("q_{3}=\\lfloor 158/96\\rfloor=1"), w.steps[0].label)
        // the divisor and the remainder become the next dividend and divisor: what slides is the same number
        for ((k, st) in w.steps.withIndex()) for ((from, to) in st.slides) assertEquals("step ${k + 1}", w.rows[from.first][from.second], w.rows[to.first][to.second])
    }

    @Test fun reaOf254And158IsTheTextbooks() {
        // Teo (17. 11.): as in the skriptá, r | α β | q, the row number only in the margin; q_i written with its row
        val w = reaWalk(rea(254, 158))
        assertEquals(listOf("r_i", "\\alpha_i", "\\beta_i", "q_i"), w.header)
        assertEquals(setOf(1, 3), w.rules)
        assertEquals(listOf("254", "1", "0", ""), w.rows[0])
        assertEquals(listOf("158", "0", "1", "1"), w.rows[1])
        assertEquals(listOf("4", "-23", "37", "1"), w.rows[7])
        assertEquals(listOf("2", "28", "-45", "2"), w.rows[8])
        assertEquals(listOf("0", "", "", ""), w.rows[9])
        assertEquals((1..10).map { "$it" }, w.rowLabels)
        // row 8 = row 6 - 4 * row 7: q_7 = 4 taken from row 7 (marked), the new row's own q_8 = 1
        assertEquals(listOf("\\text{ř}8=\\text{ř}6-4\\cdot\\text{ř}7", "q_{8}=\\lfloor 6/4\\rfloor=1"), w.steps[5].label)
        assertEquals(setOf(6 to 3), w.steps[5].marks)
        assertEquals("4", w.rows[6][3])
        assertEquals("r_{10}=0", w.steps.last().label[1])
        assertEquals(listOf("r_i=\\alpha_i\\cdot 254+\\beta_i\\cdot 158", "q_2=\\lfloor 254/158\\rfloor=1"), w.startLabel)
        assertEquals("2=28\\cdot 254+(-45)\\cdot 158", w.resultLabel[0])
        assertEquals(setOf(8 to 0, 8 to 1, 8 to 2), w.result)
    }

    @Test fun reaInTheOrderOfAnEquation() {
        // Příklad 6.5, 7x+13y: α at 7, β at 13
        val w = reaWalk(rea(13, 7), swap = true)
        assertEquals(listOf(listOf("13", "0", "1", ""), listOf("7", "1", "0", "1"), listOf("6", "-1", "1", "1"), listOf("1", "2", "-1", "6")), w.rows.take(4))
        assertEquals("1=2\\cdot 7+(-1)\\cdot 13", w.resultLabel[0])
    }

    @Test fun everyRowIsTheRule() {
        for ((a, b) in listOf(254L to 158L, 431L to 420L, 116L to 32L, 84L to 58L, 13L to 7L)) {
            val t = rea(a, b)
            for (row in t.rows.dropLast(1)) assertEquals("$a,$b", row.r, row.alpha * a + row.beta * b)
        }
    }

    @Test fun binaryPoweringOfThe644Example() {
        // 3^644 mod 645 (Příklad 6.16): 644 = 512 + 128 + 4
        val w = powWalk(3, 644, 645)
        assertEquals(10, w.rows.size)
        assertEquals(listOf(2, 7, 9), w.rows.indices.filter { w.rows[it][3] == "1" })
        assertTrue(w.resultLabel[0], w.resultLabel[0].endsWith("\\equiv ${powMod(3, 644, 645)}\\pmod{645}"))
        for (j in 1 until w.rows.size) { val p = w.rows[j - 1][2].toLong(); assertEquals((p * p) % 645, w.rows[j][2].toLong()) }
    }

    @Test fun aGeneratedRea116With32EndsInTheSignsOfItsProblem() {
        // Teo (16. 11.): -116 and 32; the table works with |a|, its result must end in the form of the problem
        val ex = tcRea(TcReaTask(-116, 32))
        val walk = ex.solution(emptyList()).steps.flatMap { it.math }.firstNotNullOf { it.table }
        assertEquals("4=3\\cdot (-116)+11\\cdot 32", walk.resultLabel.last())
    }

    @Test fun edgeCases() {
        // b divides a: one division, the gcd is the divisor
        val one = euclidWalk(rea(12, 6))
        assertEquals(setOf(0 to 4), one.result)
        // a negative base is bracketed: (-3)^2, not -3^2; the first row is the base itself, not (-3)^1
        val rows = powWalk(-3, 2, 5).rows
        assertEquals("(-3)", rows[0][1])
        assertTrue(rows.drop(1).all { it[1].startsWith("(-3)^") })
    }
}

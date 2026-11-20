package dev.hamper.tabula.data

import dev.hamper.tabula.data.TemplatesTest.Companion.checkText
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.compat.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Every wrong option says why it is wrong: in lessons (guesses and checks), definitions (yes/no examples), proofs,
 * clip tasks and generated exercises. A wrong number in an exercise always gets a diagnosis.
 */
class ExplanationsTest {
    private fun text(where: String, s: String?) {
        assertTrue("$where: missing explanation", !s.isNullOrBlank())
        checkText(where, s!!.replace('§', '$'))
        assertTrue("$where: em dash", '—' !in s)
    }

    private fun covers(where: String, options: Int, correct: Int, wrong: Map<Int, String>) {
        assertEquals("$where: explanations for exactly the wrong options", (0 until options).toSet() - correct, wrong.keys)
        wrong.forEach { (k, v) -> text("$where option $k", v) }
    }

    @Test fun lesson_steps() {
        var n = 0
        for (s in La1Lessons.flatMap { it.steps }) when (s) {
            is Step.Ask -> { covers(s.id, s.options.size, s.correct, s.wrong); n += s.wrong.size }
            is Step.Check -> { covers(s.id, s.options.size, s.correct, s.wrong); n += s.wrong.size }
            else -> {}
        }
        assertTrue("lessons: $n wrong options", n >= 70)
    }

    @Test fun definition_examples_and_traps() {
        for (k in La1Concepts) {
            k.judge.forEachIndexed { i, j -> text("${k.id} judge $i", j.wrong); assertTrue("${k.id} judge $i: wrong repeats because", j.wrong != j.because) }
            k.trap?.let { text("${k.id} trap", it.because) } // the trap's only wrong answer is "Áno"; because says why
        }
    }

    @Test fun proof_rounds() {
        for (p in La1Proofs) p.rounds.forEachIndexed { r, round ->
            round.options.forEachIndexed { i, o -> if (i != round.correct) text("${p.id} round $r option $i", o.why) }
        }
    }

    @Test fun clip_tasks() {
        val f = listOf(File("src/commonMain/composeResources/files/clips.json"), File("shared/src/commonMain/composeResources/files/clips.json")).first { it.exists() }
        val a = JSONArray(f.readText())
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i); val t = o.getJSONObject("task")
            val w = t.getJSONObject("wrong")
            covers(o.getString("id"), t.getJSONArray("options").length(), t.getInt("correct"), w.keys().asSequence().associate { it.toInt() to w.getString(it) })
        }
    }

    @Test fun generated_exercises() {
        for ((name, gen) in Generators) repeat(120) { seed ->
            val ex = gen(Random(seed * 104729 + name.hashCode()))
            val answers = ArrayList<Any>()
            for ((pi, build) in ex.parts.withIndex()) {
                val where = "$name/$seed part $pi"
                when (val a = build(answers).answer) {
                    is Answer.Choice -> { covers(where, a.options.size, a.correct, a.wrong); answers.add(a.correct) }
                    is Answer.Numbers -> {
                        val d = a.diagnose
                        assertTrue("$where: numbers without diagnosis", d != null)
                        val r = Random(seed)
                        repeat(6) {
                            val given = wrongNumbers(a, r)
                            text("$where wrong ${given}", d!!(given))
                        }
                        answers.add(a.expected)
                    }
                    is Answer.Pick -> {
                        val n = a.options.size
                        val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                        sets.filterNot(a.ok).forEach { text("$where pick $it", a.why(it)) }
                        answers.add(sets.first(a.ok))
                    }
                    is Answer.Vectors -> {
                        for ((what, list) in wrongLists(a, Random(seed))) {
                            val res = a.check(list)
                            assertTrue("$where: $what passes: $list", !res.ok)
                            text("$where $what head", res.head)
                            res.each.forEachIndexed { i, e -> e?.let { text("$where $what entry $i", it) } }
                        }
                        answers.add(a.example)
                    }
                    is Answer.Paper -> { text("$where paper miss", a.miss); answers.add(1) }
                }
            }
        }
    }

    /**
     * Wrong lists the keypad can produce from the right one: an entry left out (an enumeration), an entry written twice,
     * one cell changed. Only those the check really rejects are returned (a changed cell may give another right answer).
     */
    private fun wrongLists(a: Answer.Vectors, r: Random): List<Pair<String, List<List<dev.hamper.tabula.la.Vec>>>> = buildList {
        val ex = a.example
        if (a.open && ex.size - 1 in a.count) add("one left out" to ex.drop(1))
        if (a.open && ex.size + 1 in a.count) add("one twice" to ex + listOf(ex[0]))
        repeat(4) {
            val i = r.nextInt(ex.size); val k = r.nextInt(ex[i].size); val j = r.nextInt(a.cells.size)
            val old = ex[i][k][j]
            val new = if (a.p != null) Frac.of((old.num + 1 + r.nextInt(a.p - 1)) % a.p) else old + Frac.of(r.nextInt(1, 4).toLong())
            val changed = ex.mapIndexed { ii, e -> if (ii != i) e else e.mapIndexed { kk, v -> if (kk != k) v else v.mapIndexed { jj, x -> if (jj == j) new else x } } }
            if (!a.check(changed).ok) add("cell changed" to changed)
        }
    }.distinctBy { it.second }

    /** A wrong answer the keypad can produce: one or more cells changed. */
    private fun wrongNumbers(a: Answer.Numbers, r: Random): List<Frac> {
        while (true) {
            val g = a.expected.map { e ->
                if (r.nextInt(3) > 0) e
                else if (a.p != null) Frac.of(r.nextInt(a.p)) else e + Frac.of(r.nextInt(-3, 4).toLong())
            }
            if (g != a.expected) return g
        }
    }

    /** The mistakes the diagnosis promises to name, on fixed cases checked by hand. */
    @Test fun diagnoses_name_the_mistake() {
        val zp = dev.hamper.tabula.la.Field.Zp(5)
        val r = dev.hamper.tabula.la.Field.R
        fun v(vararg x: Int) = x.map { Frac.of(it) }
        // 2·(1,4,3)+1·(4,4,2) in Z_5^3 = (1,2,3); forgetting the coefficient 2 gives (0,3,0)
        assertTrue(arithDiagnosis(zp, Frac.of(2), v(1, 4, 3), Frac.ONE, v(4, 4, 2), v(1, 2, 3))(v(0, 3, 0)).startsWith("Prvý vektor si nevynásobil"))
        // a wrong cell names the component and its computation modulo 5
        assertTrue(arithDiagnosis(zp, Frac.of(2), v(1, 4, 3), Frac.ONE, v(4, 4, 2), v(1, 2, 3))(v(1, 4, 3)).contains("x_{2}"))
        // 1·(1,0)−2·(0,1) over R: the sign of −2 lost
        assertTrue(arithDiagnosis(r, Frac.ONE, v(1, 0), Frac.of(-2), v(0, 1), v(1, -2))(v(1, 2)).startsWith("Znamienko"))
        // basis ((1,0,0),(1,1,0),(0,0,1)), coordinates (1,−1,2) → (0,−1,2); typing the coordinates back is the swap
        val b = listOf(v(1, 0, 0), v(1, 1, 0), v(0, 0, 1))
        assertTrue(vectorFromCoordsDiagnosis(r, b, v(1, -1, 2), v(0, -1, 2))(v(1, -1, 2)).startsWith("Zamenil si súradnice a vektor"))
        // (2,−1,2) is also what solving the system instead of substituting gives: that reading wins
        assertTrue(vectorFromCoordsDiagnosis(r, b, v(1, -1, 2), v(0, -1, 2))(v(2, -1, 2)).startsWith("Počítal si opačným smerom"))
        // basis ((1,0,0),(1,1,0),(0,1,1)), coordinates (1,−1,2) → (0,1,2); 1+1 in the first component is the lost sign
        val b3 = listOf(v(1, 0, 0), v(1, 1, 0), v(0, 1, 1))
        assertTrue(vectorFromCoordsDiagnosis(r, b3, v(1, -1, 2), v(0, 1, 2))(v(2, 1, 2)).startsWith("Znamienko v zložke"))
        // coordinates of (3,2) in ((1,0),(1,1)) are (1,2); typing (3,2) is the swap, (2,1) the order
        val b2 = listOf(v(1, 0), v(1, 1))
        assertTrue(coordsDiagnosis(r, b2, v(3, 2), v(1, 2))(v(3, 2)).startsWith("Zamenil si vektor a súradnice"))
        assertTrue(coordsDiagnosis(r, b2, v(3, 2), v(1, 2))(v(2, 1)).contains("opačn"))
        // basis in rows: ((1,0),(1,1)) as rows solves (1,1;0,1)ᵀ… gives (3,−1)
        assertTrue(coordsDiagnosis(r, b2, v(3, 2), v(1, 2))(v(3, -1)).startsWith("Vektory bázy si dal do riadkov"))
        // |P| in Z_7^5, dim 2
        assertTrue(sizeDiagnosis(7, 5, 2)(v(14)).startsWith("Násobil si"))
        assertTrue(sizeDiagnosis(7, 5, 2)(v(48)).startsWith("Nulový vektor"))
        assertTrue(lzCountDiagnosis(5, 4, 2)(v(25)).startsWith("Rátal si aj"))
        // dim of 3 vectors of rank 2 in T^3
        assertTrue(dimDiagnosis(3, 3, 2)(v(3)).startsWith("Spočítal si vektory"))
        assertTrue(dimDiagnosis(4, 3, 2)(v(3)).startsWith("§3§ je dimenzia celého"))
        assertTrue(dimDiagnosis(4, 3, 2)(v(1)).startsWith("Hlavných stĺpcov je viac"))
    }
}

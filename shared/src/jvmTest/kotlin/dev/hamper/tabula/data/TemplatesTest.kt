package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

/** Every template, many seeds: it terminates, its own answer passes its own check, the text is well formed. */
class TemplatesTest {

    @Test fun every_template_many_seeds() {
        for ((name, gen) in Generators) {
            repeat(150) { seed ->
                val ex = gen(Random(seed * 7919 + name.hashCode()))
                checkText("$name/$seed prompt", ex.prompt)
                val answers = ArrayList<Any>()
                for ((i, build) in ex.parts.withIndex()) {
                    val part = build(answers)
                    checkText("$name/$seed part $i", part.q)
                    part.after?.let { checkText("$name/$seed after $i", it) }
                    when (val a = part.answer) {
                        is Answer.Choice -> {
                            assertTrue("$name/$seed: correct index", a.correct in a.options.indices)
                            assertEquals("$name/$seed: options unique ${a.options}", a.options.size, a.options.toSet().size)
                            a.options.forEach { checkText("$name/$seed option", it) }
                            checkText("$name/$seed why", a.why)
                            answers.add(a.correct)
                        }
                        is Answer.Numbers -> {
                            assertEquals("$name/$seed: labels vs expected", a.labels.size, a.expected.size)
                            if (a.p != null) assertTrue("$name/$seed: Z_p answer in range", a.expected.all { it.isInt && it.num in 0 until a.p })
                            checkText("$name/$seed shown", a.shown)
                            answers.add(a.expected)
                        }
                        is Answer.Pick -> {
                            val n = a.options.size
                            val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                            val good = sets.filter(a.ok)
                            if (good.isEmpty()) fail("$name/$seed: no valid pick for ${a.options}")
                            sets.forEach { checkText("$name/$seed pick why", a.why(it)) }
                            answers.add(good.random(Random(seed)))
                        }
                        is Answer.Vectors -> {
                            assertTrue("$name/$seed: example size ${a.example.size} in ${a.count}", a.example.size in a.count && a.example.all { it.size == a.group && it.all { v -> v.size == a.cells.size } })
                            if (a.p != null) assertTrue("$name/$seed: Z_p example in range", a.example.flatten().flatten().all { it.isInt && it.num in 0 until a.p })
                            val res = a.check(a.example)
                            assertTrue("$name/$seed: its own example fails its check: ${res.head}", res.ok)
                            checkText("$name/$seed vectors head", res.head)
                            answers.add(a.example)
                        }
                        is Answer.Paper -> {
                            assertTrue("$name/$seed: a paper part without its model", a.sample.isNotEmpty() && a.look.isNotEmpty())
                            (a.sample + a.look + a.miss).forEach { checkText("$name/$seed paper", it) }
                            answers.add(1)
                        }
                    }
                }
                checkSolution("$name/$seed", ex.solution(answers))
                ex.solution(emptyList())
            }
        }
    }

    /** Arithmetic in T^n over every field: both vectors stay in the task (a coefficient that is 0 in Z_p, e.g. 3 in Z_3,
     *  made "Spočítaj 0 v Z_3^3"), so the formula has two tuples. */
    @Test fun arithmeticKeepsBothVectorsInEveryField() {
        for (zp in listOf(false, true)) repeat(1500) { seed ->
            val ex = arithExercise(Random(seed), zp)
            val formula = ex.prompt.replace('§', '$').split('$')[1]
            assertTrue("seed $seed: ${ex.prompt}", formula.count { it == '(' } >= 2)
        }
    }


    /** Read the generated premise, then enumerate in Z_p without using Field's arithmetic or the stored answer. */
    @Test fun matrix_product_answers_agree_with_brute_force_in_each_field() {
        val seen = mutableSetOf<Pair<Int?, Int>>()
        repeat(300) { seed ->
            val ex = Generators.getValue("la1-k3-product")(Random(seed))
            val prior = if (ex.parts.size == 1) emptyList() else listOf<Any>(0)
            val answer = ex.parts.last()(prior).answer as Answer.Numbers
            val p = answer.p
            seen += p to ex.parts.size
            assertTrue(ex.prompt, if (p == null) "\\mathbb{R}" in ex.prompt else "\\mathbb{Z}_{$p}" in ex.prompt)
            fun residue(x: Int) = if (p == null) x else Math.floorMod(x, p)
            fun ints(text: String) = Regex("-?\\d+").findAll(text).map { it.value.toInt() }.toList()
            val expected: List<Int>
            if (ex.parts.size == 1) {
                val matrix = ex.prompt.substringAfter("\\begin{pmatrix}").substringBefore("\\end{pmatrix}")
                val a = matrix.split("\\\\").map(::ints)
                val x = ints(ex.prompt.substringAfter("\\mathbf{x}=(").substringBefore(")"))
                assertEquals(2, a.size); assertTrue(a.all { it.size == 3 }); assertEquals(3, x.size)
                val target = a.map { row -> residue(row.indices.sumOf { row[it] * x[it] }) }
                // Enumerate every possible output, retaining exactly the one satisfying both component equations.
                expected = if (p == null) target else (0 until p).flatMap { u -> (0 until p).map { v -> listOf(u, v) } }
                    .single { y -> y.indices.all { y[it] == target[it] } }
            } else {
                val rows = ex.prompt.substringAfter("má v prvním řádku ").removeSuffix(".")
                    .split(", v druhém řádku ", " a v třetím řádku ")
                assertEquals(3, rows.size)
                fun coefficients(text: String): List<Int> = when {
                    text == "nulový řádek" -> listOf(0, 0)
                    "prvního řádku" in text || "první řádek" in text -> listOf(if ("násobek" in text) ints(text).single() else 1, 0)
                    "druhého řádku" in text || "druhý řádek" in text -> listOf(0, if ("násobek" in text) ints(text).single() else 1)
                    else -> (1..2).map { j ->
                        val term = Regex("([+-]?\\d*)\\\\mathbf\\{A}_\\{$j:}").find(text)
                        when (val c = term?.groupValues?.get(1)) { null -> 0; "", "+" -> 1; "-" -> -1; else -> c.toInt() }
                    }
                }
                expected = rows.flatMap { text ->
                    val target = coefficients(text).map(::residue)
                    // On A=(e1,e2), enumerate all coefficient pairs; the requested row uniquely determines B's row.
                    if (p == null) target else (0 until p).flatMap { u -> (0 until p).map { v -> listOf(u, v) } }
                        .single { c -> (0..1).all { j -> residue(c[0] * (if (j == 0) 1 else 0) + c[1] * (if (j == 1) 1 else 0)) == target[j] } }
                }
            }
            assertEquals("seed $seed: ${ex.prompt}", expected.map { it.toLong() }, answer.expected.map { it.num })
            assertTrue(answer.expected.all { it.isInt })
        }
        assertEquals(setOf(null, 3, 5, 7).flatMap { p -> listOf(p to 1, p to 2) }.toSet(), seen)
    }

    companion object {
        fun checkText(where: String, s0: String) {
            // a display formula ($$...$$) is one formula; == around a key word is markup, not text
            assertTrue("$where: unbalanced display formula (\$\$) in: $s0", Regex("""\$\$""").findAll(s0.replace('§', '$')).count() % 2 == 0)
            assertTrue("$where: unbalanced key word (==) in: $s0", Regex("==").findAll(s0).count() % 2 == 0)
            val s = s0.replace('§', '$').replace("$$", "$")
            val dollars = s.count { it == '$' }
            assertTrue("$where: odd number of \$ in: $s", dollars % 2 == 0)
            assertTrue("$where: em dash in: $s", '—' !in s)
            assertTrue("$where: unresolved template in: $s", !s.contains("\${") && !s.contains("null"))
            s.split('$').filterIndexed { i, _ -> i % 2 == 1 }.forEach { checkMath(where, it) }
            // a formula typed as plain text (T^n, \\mathbb, Z_m) is shown raw: it must sit between $ or §
            s.replace('§', '$').split('$').filterIndexed { i, _ -> i % 2 == 0 }.forEach { plain ->
                assertTrue("$where: formula outside math markup in: $s", '^' !in plain && '\\' !in plain && !Regex("""\p{L}_\p{L}""").containsMatchIn(plain))
            }
        }

        /** Every sentence, note and formula of a solution is well formed. */
        fun checkSolution(where: String, sol: Solution) {
            for (s in sol.steps) {
                checkText("$where solution", s.say)
                for (f in s.math) { checkMath("$where formula", f.tex); f.note?.let { checkText("$where note", it) } }
            }
            sol.postup.aims.forEach { checkText("$where postup", it.title); checkText("$where postup", it.how) }
        }

        fun checkMath(where: String, t: String) {
            var depth = 0
            for (c in t) { if (c == '{') depth++; if (c == '}') depth--; assertTrue("$where: unbalanced braces in $t", depth >= 0) }
            assertEquals("$where: unbalanced braces in $t", 0, depth)
            assertEquals("$where: \\begin/\\end in $t", Regex("\\\\begin").findAll(t).count(), Regex("\\\\end").findAll(t).count())
        }
    }
}

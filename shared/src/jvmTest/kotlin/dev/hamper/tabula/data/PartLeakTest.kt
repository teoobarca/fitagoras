package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * A question must not give away what is still to be decided (Teo, 17. 11.): "Báza a dimenzia obalu" asked "Súradnice x
 * v tvojej báze:" right away when x was in M, and "Má x súradnice?" when it was not, so the question told the answer.
 * Here, for every generator, the wording of each part (numbers left out) may depend only on the assignment and on what
 * the student already answered in the parts before it. A generator whose parts vary at random for another reason
 * (a choice of sub-questions that tells nothing about the answer) is listed with that reason.
 */
class PartLeakTest {
    /** Generators whose part wording varies with a random variant that is not an answer, and why it gives nothing away. */
    private val variants = mapOf(
        "zp-pocet" to "the first question names the dimension d of the assignment (a formula, left out of the comparison): one vector for d = 1, LN d-tuples otherwise",
    )

    /** The words of a text: formulas (the numbers and sets of this instance) as one mark. */
    private fun shape(s: String) = s.replace(Regex("\\$[^$]*\\$"), "§").replace(Regex("-?[0-9]+"), "#")
    private fun answer(a: Answer): Any = when (a) {
        is Answer.Choice -> a.correct
        is Answer.Numbers -> a.expected
        is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }
        is Answer.Vectors -> a.example
        is Answer.Paper -> 1
    }
    /** What the student knows after a part: a choice's right option (as words), nothing for a computed number. */
    private fun decided(p: Part): String = (p.answer as? Answer.Choice)?.let { shape(it.options[it.correct]) } ?: "·"

    @Test fun noQuestionTellsTheBranch() {
        val bad = ArrayList<String>()
        for ((name, gen) in Generators) {
            if (name in variants) continue
            // (prompt, what was decided before part i) -> the wordings part i had
            val seen = HashMap<Pair<String, List<String>>, MutableSet<String>>()
            repeat(60) { seed ->
                val ex = gen(Random(seed * 13 + 1))
                if (ex.parts.size < 2) return@repeat
                val answers = ArrayList<Any>()
                val before = ArrayList<String>()
                ex.parts.forEachIndexed { i, b ->
                    val p = b(answers)
                    seen.getOrPut(shape(ex.prompt) to before.toList() + "#$i") { HashSet() } += shape(p.q)
                    answers += answer(p.answer); before += decided(p)
                }
            }
            for ((k, v) in seen) if (v.size > 1) bad += "$name, part ${k.second.last()} after ${k.second.dropLast(1)}: ${v.joinToString(" | ")}"
        }
        assertTrue("questions that depend on something not yet answered:\n" + bad.take(30).joinToString("\n"), bad.isEmpty())
    }
}

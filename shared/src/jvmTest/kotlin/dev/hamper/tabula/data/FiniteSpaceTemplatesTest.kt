package dev.hamper.tabula.data

import dev.hamper.tabula.la.FiniteSpace
import dev.hamper.tabula.la.Frac
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import dev.hamper.tabula.compat.*

/**
 * The Z_p^n generators: Czech numerals agree with their nouns, counts are the model's, and a list of subspaces is
 * accepted exactly when it has each subspace once.
 */
class FiniteSpaceTemplatesTest {

    /** Czech numerals agree with the noun (2 až 4 vektory, 5 vektorů), and no "1 vektormi" or "1-tice" in the Slovak. */
    @Test fun numerals_agree_with_their_nouns() {
        val few = Regex("""\$[2-4]\$ (vektorů|prvků|různých|bází)""")
        val many = Regex("""\$([5-9]|\d{2,})\$ (vektory|prvky|různé podprostory|různé báze)""")
        val one = Regex("""\$1\$( vektormi|-t[ií]c| LN vektorov)""")
        val skFew = Regex("""=[2-4]\$ vektorov""")
        for (gen in FiniteSpaceGenerators.keys) repeat(300) { seed ->
            val ex = FiniteSpaceGenerators.getValue(gen)(Random(seed))
            val answers = ArrayList<Any>()
            val texts = ArrayList<String>()
            for (b in ex.parts) {
                val part = b(answers); texts += part.q
                when (val a = part.answer) {
                    is Answer.Numbers -> answers.add(a.expected)
                    is Answer.Pick -> answers.add(setOf(0))
                    is Answer.Choice -> answers.add(a.correct)
                    is Answer.Vectors -> { val res = a.check(a.example); texts += listOf(res.head); answers.add(a.example) }
                    is Answer.Paper -> answers.add(1)
                }
            }
            texts += ex.solution(answers).steps.flatMap { st -> listOf(st.say) + st.math.flatMap { listOfNotNull(it.tex, it.note) } }
            for (t in texts) {
                assertFalse("$gen/$seed: $t", few.containsMatchIn(t))
                assertFalse("$gen/$seed: $t", many.containsMatchIn(t))
                assertFalse("$gen/$seed: $t", one.containsMatchIn(t))
                assertFalse("$gen/$seed: $t", skFew.containsMatchIn(t))
            }
        }
    }

    @Test fun count_exercises_ask_the_models_numbers() {
        repeat(200) { seed ->
            val ex = zpCountExercise(Random(seed))
            val answers = ArrayList<Any>()
            for (b in ex.parts) {
                val a = b(answers).answer as Answer.Numbers
                assertTrue("$seed fits the keypad: ${a.expected}", a.expected.all { it.isInt && it.num in 1..999_999 })
                answers.add(a.expected)
            }
        }
        // the textbook's question: planes of Z_5^3 are 14880 / 480 = 31
        val s = FiniteSpace(5, 3)
        assertEquals(Frac.of(31), Frac.of(s.subspaceCount(2).toLong()))
    }

    @Test fun a_list_counts_each_subspace_once() {
        repeat(100) { seed ->
            val ex = zpListExercise(Random(seed))
            val pick = ex.parts[1](listOf(Any())).answer as Answer.Pick
            val n = pick.options.size
            val sets = (1 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }
            val good = sets.filter(pick.ok)
            assertTrue("$seed: some list is right", good.isNotEmpty())
            // every right list has the same size (the number of subspaces) and adding any other option breaks it
            val size = good.first().size
            assertTrue(good.all { it.size == size })
            for (g in good) for (o in 0 until n) if (o !in g) assertFalse("$seed: $g + $o repeats a subspace", pick.ok(g + o))
            // there are repeated generators among the options, and the feedback names a repeat as the same subspace
            assertTrue("$seed: options with another basis of a listed subspace", n > size)
            val dup = (0 until n).firstNotNullOf { o -> good.first().let { g -> if (o !in g) g + o else null } }
            assertTrue(pick.why(dup), pick.why(dup).contains("ten istý podpriestor"))
        }
    }
}

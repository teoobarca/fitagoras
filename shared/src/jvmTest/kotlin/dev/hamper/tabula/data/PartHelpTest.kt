package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * In an exercise of several parts, help belongs to the part on screen (Teo, 17. 11.): every part names the goal of the
 * method it works on ("Ako na to?" opens there) and a part's hint is its own. A part may go without a hint (none is
 * offered then), but no longer exercise is left with none at all.
 */
class PartHelpTest {
    private fun answer(a: Answer): Any = when (a) {
        is Answer.Choice -> a.correct
        is Answer.Numbers -> a.expected
        is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }
        is Answer.Vectors -> a.example
        is Answer.Paper -> 1
    }

    @Test fun everyPartOfALongerExerciseHasItsGoalAndHint() {
        val bad = ArrayList<String>()
        for ((name, gen) in Generators) repeat(30) { seed ->
            val ex = gen(Random(seed * 7 + 3))
            if (ex.parts.size < 2) return@repeat
            val answers = ArrayList<Any>()
            val parts = ex.parts.map { b -> b(answers).also { answers += answer(it.answer) } }
            parts.forEachIndexed { i, p ->
                if (p.aim == null || p.aim !in ex.postup.aims.indices) bad += "$name/$seed part $i: aim ${p.aim} (method has ${ex.postup.aims.size} goals): ${p.q}"
                if (p.hint != null && p.hint.isBlank()) bad += "$name/$seed part $i: blank hint"
            }
            if (parts.all { it.hint == null }) bad += "$name/$seed: no part has a hint"
        }
        assertTrue("parts without their goal or hint:\n" + bad.distinct().take(60).joinToString("\n"), bad.isEmpty())
    }
}

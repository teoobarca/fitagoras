package dev.hamper.tabula.data

import dev.hamper.tabula.la.Frac
import java.io.File
import kotlin.random.Random

/** A text a student can see, and whether it is a formula on its own (raw TeX) or text with $math$. */
data class Seen(val where: String, val text: String, val math: Boolean = false)

/**
 * Everything one generated exercise can show, its parts answered right in order: question, options, feedback, a few
 * diagnoses of wrong numbers, every pick's verdict, hint, method and the solution (for these answers and for none).
 */
fun exerciseTexts(name: String, ex: Exercise, seed: Int): List<Seen> = buildList {
    val w = "$name/$seed"
    add(Seen("$w prompt", ex.prompt)); add(Seen("$w label", ex.label)); add(Seen("$w hint", ex.hint))
    ex.visual?.let { add(Seen("$w visual", it, math = true)) }
    ex.postup.aims.forEachIndexed { i, a -> add(Seen("$w postup $i title", a.title)); add(Seen("$w postup $i how", a.how)) }
    val answers = ArrayList<Any>()
    for ((pi, build) in ex.parts.withIndex()) {
        val part = build(answers)
        add(Seen("$w part $pi", part.q)); part.after?.let { add(Seen("$w after $pi", it)) }
        when (val a = part.answer) {
            is Answer.Choice -> {
                a.options.forEach { add(Seen("$w option", it)) }
                add(Seen("$w why", a.why)); a.wrong.forEach { (k, v) -> add(Seen("$w wrong $k", v)) }
                answers.add(a.correct)
            }
            is Answer.Numbers -> {
                a.labels.forEach { add(Seen("$w label", it, math = true)) }
                add(Seen("$w shown", a.shown))
                val r = Random(seed)
                a.diagnose?.let { d ->
                    repeat(4) {
                        val g = a.expected.map { e -> if (r.nextBoolean()) e else if (a.p != null) Frac.of(r.nextInt(a.p)) else e + Frac.of(r.nextInt(-3, 4).toLong()) }
                        if (g != a.expected) add(Seen("$w diagnose $g", d(g)))
                    }
                }
                answers.add(a.expected)
            }
            is Answer.Pick -> {
                a.options.forEach { add(Seen("$w pick option", it)) }
                val n = a.options.size
                val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                sets.forEach { add(Seen("$w pick $it", a.why(it))) }
                answers.add(sets.first(a.ok))
            }
            is Answer.Vectors -> {
                a.cells.forEach { add(Seen("$w cell", it, math = true)) }
                val res = a.check(a.example)
                add(Seen("$w vectors ok", res.head))
                if (a.open && a.example.size > 1) add(Seen("$w vectors missing", a.check(a.example.drop(1)).head))
                answers.add(a.example)
            }
            is Answer.Paper -> {
                a.sample.forEach { add(Seen("$w paper sample", it)) }
                a.look.forEach { add(Seen("$w paper look", it)) }
                add(Seen("$w paper miss", a.miss))
                answers.add(1)
            }
        }
    }
    for ((tag, sol) in listOf("solution" to ex.solution(answers), "solution(none)" to ex.solution(emptyList()))) addAll(solutionTexts("$w $tag", sol))
}

fun solutionTexts(where: String, sol: Solution): List<Seen> = buildList {
    sol.steps.forEachIndexed { i, s ->
        add(Seen("$where step $i (${s.aim.title})", s.say))
        s.math.forEach { f -> add(Seen("$where step $i formula", f.tex, math = true)); f.note?.let { add(Seen("$where step $i note", it)) } }
    }
}

/** Every generator's exercises over [seeds] seeds. */
fun allGeneratedTexts(seeds: Int): Sequence<Pair<Pair<String, Exercise>, List<Seen>>> = sequence {
    for ((name, gen) in Generators) for (seed in 0 until seeds) {
        val ex = gen(Random(seed * 7919 + name.hashCode()))
        yield((name to ex) to exerciseTexts(name, ex, seed))
    }
}

/** The main source folder, from the module or the repository root. */
fun mainSources(): File = listOf(File("src/commonMain"), File("shared/src/commonMain")).first { it.exists() }

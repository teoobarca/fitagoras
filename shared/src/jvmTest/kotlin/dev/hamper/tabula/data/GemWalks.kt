package dev.hamper.tabula.data

import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.la.echelon
import dev.hamper.tabula.la.q
import kotlin.random.Random

/** A formula somewhere a student can see it. */
data class Seen2(val where: String, val f: Formula)

/** Every formula of every worked example, explore picture, paper solution and (sampled) generated solution. */
fun allFormulas(seeds: Int = 6): List<Seen2> = buildList {
    fun sol(where: String, s: Solution?) { s?.steps?.forEachIndexed { i, st -> st.math.forEach { add(Seen2("$where step $i", it)) } } }
    for (l in AllLessons) for (s in l.steps) if (s is Step.Worked) sol("${l.id}/${s.id}", s.solution)
    for (c in Courses) for (paper in c.papers + (1..3).map { c.generatedPaper(it) }) for (p in paper.problems) {
        sol("${paper.id} ${p.n}", p.solution); p.parts.forEach { sol("${paper.id} ${p.n} ${it.label}", it.solution) }
    }
    for ((name, gen) in Generators) for (seed in 0 until seeds) {
        val ex = gen(Random(seed * 7919 + name.hashCode()))
        sol("$name/$seed", ex.solution(emptyList()))
    }
}

/** Every GEM walk the app can show: the explore pictures of the lessons and every chain in a formula. */
fun allGemWalks(seeds: Int = 6): List<Pair<String, GemWalk>> = buildList {
    for (l in AllLessons) for (s in l.steps) if (s is Step.Explore && s.pic is Pic.Gem) {
        val p = s.pic as Pic.Gem
        val f: Field = if (p.p == 0) Field.R else Field.Zp(p.p)
        add("${l.id}/${s.id} explore" to GemWalk.of(f.echelon(Mat(p.rows.map { r -> r.map { it.q() } }, p.bar))))
    }
    for ((w, f) in allFormulas(seeds)) f.gem?.let { add(w to it) }
}

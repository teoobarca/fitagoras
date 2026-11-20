package dev.hamper.tabula.data

import dev.hamper.tabula.compat.synchronized

/*
 * The method of a problem type and its solutions (docs/CONTENT.md, "Postup a riešenie").
 *
 * A Postup is 2 to 5 named subgoals in the order a student does them ("Obsahuje θ?", "Súčet ostane v množine?", ...).
 * Every generator declares one per problem type, every worked example in a lesson has one. A Solution is a list of
 * steps and each step belongs to one subgoal of its Postup: one short Slovak sentence saying what is checked and why
 * it follows, then the formulas written on paper. A step without a subgoal cannot be made. The app shows the Postup
 * as "Ako na to?" before the first attempt and the solution as the same numbered goals, checked.
 * Subgoal-labelled worked examples: Catrambone 1998, Margulieux and Catrambone 2016.
 */

/**
 * One subgoal: [title] a short question or task in the student's words ("Súčet ostane v množine?"), [how] one
 * sentence on how it is done, shown on the "Ako na to?" card. Compared by identity: two methods' "Záver" differ.
 */
class Aim(title: String, how: String) {
    val title = title.replace('§', '$')
    val how = how.replace('§', '$')
    override fun toString() = title
}

/** The method: 2 to 5 subgoals in order. Destructure it to name them: `val (theta, sum, mult, end) = SubspacePostup`. */
class Postup(val aims: List<Aim>) {
    operator fun get(i: Int) = aims[i]
    operator fun component1() = aims[0]
    operator fun component2() = aims[1]
    operator fun component3() = aims[2]
    operator fun component4() = aims[3]
    operator fun component5() = aims[4]
    val size get() = aims.size
    fun indexOf(a: Aim) = aims.indexOfFirst { it === a }

    /** A solution along this method: `solve { theta("Nulový vektor rovnicu spĺňa.", "0=0") }`. */
    fun solve(block: SolutionScope.() -> Unit): Solution = SolutionScope(this).apply(block).build()
}

/** A method from `"title" to "how"` pairs. */
fun postup(vararg aims: Pair<String, String>) = Postup(aims.map { (t, h) -> Aim(t, h) })

/**
 * A formula on its own line (raw TeX, no §), with an optional short note beside it (text with §math§). A GEM chain is
 * a [gem] walk instead (la/GemWalk.kt): the component shows it step by step with the matrix in place, [tex] is the
 * whole chain on paper for the tests.
 */
class Formula(val tex: String, note: String? = null, val gem: dev.hamper.tabula.la.GemWalk? = null, val table: dev.hamper.tabula.la.TableWalk? = null) {
    val note = note?.replace('§', '$')

    /** A GEM chain: the walk carries its own operations, one kind per ∼ ([note] stays for a remark beside it). */
    constructor(gem: dev.hamper.tabula.la.GemWalk, note: String? = null) : this(gem.tex(), note, gem)

    /** A table that grows by rows (Euclid's divisions, the REA), shown step by step by screens/TableWalk.kt. */
    constructor(table: dev.hamper.tabula.la.TableWalk, note: String? = null) : this(table.tex(), note, null, table)
}

/**
 * One step of a solution, under its subgoal: [say] one short sentence (first sentence at most 20 words) saying what
 * is checked and why it follows, text with §math§; [math] what is written on paper, in order.
 */
class SolStep(val aim: Aim, say: String, val math: List<Formula> = emptyList()) {
    val say = say.replace('§', '$')
}

/** A worked solution: steps in the order of [postup]'s subgoals. [problems] lists what breaks the rules (tests). */
class Solution(val postup: Postup, val steps: List<SolStep>) {
    /** Consecutive steps of one subgoal, in order: what one goal card shows. */
    val groups: List<Pair<Aim, List<SolStep>>> by lazy {
        val out = ArrayList<Pair<Aim, MutableList<SolStep>>>()
        for (s in steps) if (out.lastOrNull()?.first === s.aim) out.last().second += s else out += s.aim to mutableListOf(s)
        out
    }

    /** Rule breaks: an empty solution, a step under a subgoal of another method, subgoals out of order, no conclusion. */
    fun problems(): List<String> = buildList {
        if (postup.size !in 2..5) add("a Postup has 2 to 5 subgoals, this one ${postup.size}")
        if (steps.isEmpty()) add("no steps")
        val idx = steps.map { postup.indexOf(it.aim) }
        if (idx.any { it < 0 }) add("a step under a subgoal of another Postup: ${steps.filter { postup.indexOf(it.aim) < 0 }.map { it.aim.title }}")
        if (idx.zipWithNext().any { (a, b) -> b < a }) add("steps out of the Postup's order: ${steps.map { it.aim.title }}")
        if (idx.firstOrNull() != 0) add("the first step is not under the first subgoal (${postup[0].title})")
        if (idx.lastOrNull() != postup.size - 1) add("the last step is not under the last subgoal (${postup.aims.last().title})")
        steps.forEach { if (it.say.isBlank()) add("a step under ${it.aim.title} without its sentence") }
    }

    /** This solution followed by [other] (same method), e.g. two variants written one after the other. */
    operator fun plus(other: Solution): Solution { require(other.postup === postup); return Solution(postup, steps + other.steps) }
}

/** Builds a Solution: `aim("Sentence.", "formula", ...)` or `aim("Sentence.", Formula("tex", "note"))`. */
class SolutionScope(val postup: Postup) {
    private val steps = ArrayList<SolStep>()

    operator fun Aim.invoke(say: String, vararg math: String) { steps += SolStep(this, say, math.map { Formula(it) }) }
    operator fun Aim.invoke(say: String, math: List<Formula>) { steps += SolStep(this, say, math) }
    operator fun Aim.invoke(say: String, first: Formula, vararg more: Formula) { steps += SolStep(this, say, listOf(first) + more) }
    fun Aim.step(say: String, vararg math: Formula) { steps += SolStep(this, say, math.toList()) }

    /** Formulas for the last step (a GEM chain, a long computation): they belong to its sentence. */
    fun more(vararg math: Formula) { val l = steps.removeAt(steps.lastIndex); steps += SolStep(l.aim, l.say, l.math + math) }
    fun more(math: List<Formula>) { val l = steps.removeAt(steps.lastIndex); steps += SolStep(l.aim, l.say, l.math + math) }

    fun build() = Solution(postup, steps.toList())
}

/* ---- Proofs as solutions (a paper's proof problem): the proof's goals are its subgoals ---------------------------- */

private val proofPostups = HashMap<String, Postup>()

/** A proof's method: one subgoal per round (its goal title, its question as the how), then "Záver" if there is room. */
fun proofPostup(p: Proof): Postup = synchronized(proofPostups) {
    proofPostups.getOrPut(p.id) {
        val aims = p.rounds.mapIndexed { i, r -> Aim(p.goals.getOrNull(i) ?: "Krok ${i + 1}", r.prompt) }
        Postup(if (aims.size <= 4) aims + Aim("Záver", "Napíš, čo je dokázané.") else aims)
    }
}

/** A proof written out: what is given, each round's line (its assumption and right option) under its goal, the conclusion. */
fun proofSolution(p: Proof): Solution {
    val pp = proofPostup(p)
    return pp.solve {
        p.rounds.forEachIndexed { i, r -> pp[i](if (i == 0) p.given.trimEnd() + " " + r.line else r.line) }
        pp.aims.last()(p.end)
    }
}

/** Two proofs of one paper problem, one subgoal each. */
fun proofsSolution(a: Proof, b: Proof): Solution {
    val pp = postup("Prvý dôkaz: ${a.title}" to a.task, "Druhý dôkaz: ${b.title}" to b.task)
    return pp.solve {
        for ((k, p) in listOf(a, b).withIndex()) {
            pp[k](p.given)
            p.rounds.forEach { r -> pp[k](r.line) }
            pp[k](p.end)
        }
    }
}

/**
 * A problem in parts a), b), c) whose parts are solved by other methods (a paper problem made of generated exercises):
 * one subgoal per part, titled by [parts]' labels, each part's steps under it.
 */
fun parts(vararg parts: Pair<String, Solution>): Solution {
    val pp = Postup(parts.map { (label, s) -> Aim(label, s.postup.aims.joinToString(", ") { it.title }) })
    return Solution(pp, parts.withIndex().flatMap { (i, p) -> p.second.steps.map { SolStep(pp[i], it.say, it.math) } })
}

package dev.hamper.tabula.data.dsl

import dev.hamper.tabula.data.*
import kotlin.random.Random

/*
 * The content DSL for topic files (docs/CONTENT.md). Text is written with § as the math delimiter; m() turns it into $.
 * The same builders LA1 kapitola 2 uses, public so a topic file needs nothing outside itself.
 */

/** Steps of a lesson in order; step ids are "<lesson id>-<n>". */
class LessonBuilder(val id: String) {
    val steps = ArrayList<Step>()
    private var n = 0
    private fun next() = "$id-${n++}"
    fun ask(q: String, vararg o: String, correct: Int, after: String, pic: Pic? = null, sub: String? = null, wrong: Map<Int, String> = emptyMap()) { steps += Step.Ask(next(), m(q), o.map(::m), correct, m(after), pic, sub?.let(::m), wrong.mapValues { m(it.value) }) }
    fun explore(title: String, sub: String, pic: Pic, done: String, body: String? = null) { steps += Step.Explore(next(), m(title), m(sub), pic, m(done), body?.let(::m)) }
    /**
     * [cards]: ids of the definitions and theorems the explanation rests on, each drawn as its own card; [formal] only
     * for a Czech quote that is none of them (a remark, a test's wording) from [src].
     */
    fun explain(title: String, body: String, pic: Pic? = null, formal: List<Line>? = null, src: String? = null, more: String? = null, cards: List<String> = emptyList()) { steps += Step.Explain(next(), m(title), m(body), pic, formal, src, more?.let(::m), cards) }
    /**
     * A worked example along [postup]: each step under one subgoal, a short Slovak sentence (what is done and why) and
     * the formulas. `worked("...", "...", "Skriptá, ...", SubspacePostup) { theta("Sentence.", eq("tex", "note")) }`
     */
    fun worked(title: String, problem: String, src: String?, postup: Postup, solve: SolutionScope.() -> Unit) { steps += Step.Worked(next(), m(title), m(problem), postup.solve(solve), src) }
    /** A worked example whose solution a generator writes (the same Postup as its practice). */
    fun worked(title: String, problem: String, src: String?, solution: Solution) { steps += Step.Worked(next(), m(title), m(problem), solution, src) }
    fun check(q: String, vararg o: String, correct: Int, why: String, hint: String? = null, pic: Pic? = null, sub: String? = null, wrong: Map<Int, String> = emptyMap()) { steps += Step.Check(next(), m(q), o.map(::m), correct, m(why), hint?.let(::m), pic, wrong.mapValues { m(it.value) }, sub?.let(::m)) }
    fun practice(gen: String, solo: Boolean, title: String? = null) { steps += Step.Practice(next(), gen, solo, title) }
    fun clip(c: String) { steps += Step.Clip(next(), c) }
    fun def(c: String) { steps += Step.Def(next(), c) }
    /** A theorem with its proof; [also]: numbered statements shown after it, a card each (Věta 2.60 after 2.59). */
    fun thm(t: String, vararg also: String, proof: String? = null) { steps += Step.Thm(next(), t, also.toList(), proof) }
    fun prove(p: String) { steps += Step.Prove(next(), p) }
}


/** A lesson. [topics]: schedule codes it teaches (MI2, TČ1, S3), so "Kde sme" ties it to the lectures. [art]: 0..8. */
fun lesson(id: String, name: String, minutes: Int, learn: String, art: Int, topics: List<String>, body: LessonBuilder.() -> Unit) =
    LessonDef(id, name, minutes, m(learn), LessonBuilder(id).apply(body).steps, art, topics)

/** A display formula of a worked step (raw TeX) with an optional short note (text with §math§). */
fun eq(tex: String, note: String? = null) = Formula(tex.trimIndent(), note?.let(::m))
/** Czech paragraphs (the formal text under an explanation). */
fun cz(vararg s: String) = s.map { Para(m(it)) }

/** Lines of Czech definitions and theorems: paragraph, item, display block (raw TeX). */
fun p(t: String) = Para(m(t))
fun i(t: String) = Item(m(t))
fun b(t: String) = Block(t.trimIndent())
/** A definition's "why": the Czech piece and why it is there. */
fun w(part: String, because: String) = Why(m(part), m(because))
/** Is this an example? [because] after the right pick, [wrong] after the wrong one (what it gets wrong). */
fun j(tex: String, yes: Boolean, because: String, wrong: String) = Judge(m(tex), yes, m(because), m(wrong))
/** A proof line to choose; a wrong one says [why]. */
fun c(t: String, why: String? = null) = ProofChoice(m(t), why?.let { m(it) })
fun r(prompt: String, correct: Int, vararg o: ProofChoice, given: String? = null) = ProofRound(m(prompt), o.toList(), correct, given?.let { m(it) })

fun Random.pick(lo: Int, hi: Int) = nextInt(lo, hi + 1)
fun Random.nonzero(lo: Int, hi: Int): Int { while (true) { val v = pick(lo, hi); if (v != 0) return v } }

/* ---- Statements (docs/CONTENT.md, "Statements") ------------------------------------------------------------- */

/**
 * The wording of a statement: [assume] the hypotheses and [claim] the claim with its quantifiers, both word for word from the source,
 * cut where the hypotheses end; [core] the claim as one display formula for a 320 dp card; [parts] proved on their own.
 */
fun wording(assume: List<Line>, claim: List<Line>, core: String? = null, parts: List<ClaimPart> = emptyList(), rest: List<Line> = emptyList()) =
    Wording(assume, claim, core?.trimIndent(), parts, rest)
/** A part of a claim proved on its own: Slovak [label], the part as a formula [tex] (raw TeX), the proof round it starts at. */
fun part(label: String, tex: String, from: Int = 0) = ClaimPart(m(label), tex.trimIndent(), from)
/** Proved line by line; [more]: proofs of its other parts, each a step of its own. */
fun proved(p: Proof, vararg more: Proof) = Proven.Lines(p, more.toList())
/** Proved in a few sentences ([argument], text with §math§) from [src]. */
fun shortProof(argument: String, src: String) = Proven.Short(m(argument), src)
/** Not proved in the course: [why] who decided and how we know. */
fun noProof(why: String, src: String) = Proven.Omitted(m(why), src)
/** A numbered remark of the textbook shown with a theorem, a card of its own. */
fun quote(src: String, vararg lines: Line) = Quote(src, lines.toList())

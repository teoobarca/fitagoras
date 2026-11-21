package dev.hamper.tabula.data

import dev.hamper.tabula.la.Frac

/*
 * The content model. Czech text (definitions, theorems, test tasks) is copied word for word from the textbook
 * source (bi-la1 chap-03.woo, kapitola 2, numbering of the rendered textbook) and the past tests on fit-wiki.
 * Slovak text is ours. Every item says where it comes from in [src].
 */

/** A definition in three layers: the exact Czech text, why each condition is there, examples and non-examples. */
data class Concept(
    val id: String,
    val name: String,
    /** "Skriptá, Definice 2.23" */
    val src: String,
    val czech: List<Line>,
    /** Slovak unpacking: a piece of the Czech text and why it has to be there. */
    val why: List<Why>,
    /** Is this an example of the notion? Each with the reason. */
    val judge: List<Judge>,
    /**
     * Idea units for the self-check after writing it from memory: only what [ask] asks for. "Z pamäti" and the review
     * count these; one unticked sends the definition back to the review.
     */
    val units: List<String>,
    /** A true sentence that people write instead of the definition, if there is a known one. */
    val trap: Trap? = null,
    /** "Napíš definíciu ..." as the tests ask it. */
    val ask: String,
    /** What the examples ask, in the concept's own words: "Je to podpriestor?", not a generic "Platí to?". */
    val question: String = "",
    /**
     * Its short name after the word "definícia" in any case, so it never declines: "podpriestoru", "LZ". A reference
     * "Definíciu 2.7" reads "definíciu podpriestoru (Def. 2.7)".
     */
    val short: String = "",
    /**
     * Navyše zo skrípt: what the textbook's text around it says but [ask] does not ask (the words "dělitel, násobek",
     * a remark). Shown after the check from memory, never counted.
     */
    val extra: List<String> = emptyList(),
    /** Numbered remarks needed beside this definition, each shown as its own source card. */
    val remarks: List<Quote> = emptyList(),
)

data class Why(val part: String, val because: String)
/** [wrong]: what the opposite answer gets wrong, shown before [because] after a wrong pick. */
data class Judge(val tex: String, val yes: Boolean, val because: String, val wrong: String)
data class Trap(val sentence: String, val because: String, val src: String)

/**
 * The wording of a numbered statement as the textbook states it, in parts every screen draws from (docs/CONTENT.md, "Statements and proofs"): never a
 * list of lines that a screen has to guess at. [assume] and [claim] are the Czech text word for word, cut where the
 * hypotheses end; joined with [rest] they are the whole numbered text ([lines]).
 */
data class Wording(
    /** Predpoklady: "Nechť §a \in \mathbb{Z}, d \in \mathbb{N}§." Empty when the claim has none of its own. */
    val assume: List<Line>,
    /** Tvrdenie s kvantifikátormi: "Pak existují jednoznačně určená čísla §q, r \in \mathbb{Z}§ taková, že ..." */
    val claim: List<Line>,
    /**
     * Jadro: the claim as one display formula (raw TeX) that fits a 320 dp card unbroken. The collapsed "Dokazuješ" bar
     * shows it, and a long statement that has one leads with it (the whole text one tap away). At most five rows: a
     * list of laws has no core and is shown whole. Required when the statement is proved line by line.
     */
    val core: String? = null,
    /** Časti tvrdenia proved on their own (existencia a jednoznačnosť, ⇒ a ⇐, body 1 až 3), in the proof's order. */
    val parts: List<ClaimPart> = emptyList(),
    /** The rest of the numbered text after the claim ("Speciálně, ...", an equivalent form), word for word. */
    val rest: List<Line> = emptyList(),
) {
    /** The whole numbered text, word for word. */
    val lines: List<Line> get() = assume + claim + rest
    /** Long enough that a card leads with [core] and the whole text unfolds on a tap. */
    val long: Boolean get() = lines.size > 4 || lines.sumOf { plainLength(it) } > LongWording
}

/** A wording longer than this (letters of its text and TeX) leads with its core on a card. */
const val LongWording = 320

private fun plainLength(l: Line) = when (l) { is Para -> l.tex.length; is Item -> l.tex.length; is Block -> l.tex.length }

/**
 * One part of a claim proved on its own: [label] in Slovak ("Existencia", "Jednoznačnosť", "(⇒)"), [tex] what the part
 * says as a formula (raw TeX, formal, the same notation as the textbook), [from] the round of the proof it starts at.
 */
data class ClaimPart(val label: String, val tex: String, val from: Int = 0)

/**
 * How the course proves a theorem, said every time: a proof missing without a reason is how a student ends up learning
 * a theorem he cannot justify (Teo, 16. 11., Lemma 6.1).
 */
sealed interface Proven {
    /**
     * Proved in the app line by line, from the textbook's own proof; [more]: proofs of other parts the course proves
     * as separate steps (Frobenius: the necessary condition), each also a proof of this theorem.
     */
    data class Lines(val proof: Proof, val more: List<Proof> = emptyList()) : Proven
    /**
     * The source's proof is a few sentences, or a direct consequence: [argument] (text with §math§) shown under the
     * statement as it is, [src] where it comes from ("Skriptá, dôkaz Lemmatu 6.1").
     */
    data class Short(val argument: String, val src: String) : Proven
    /** The course does not prove it: [why] who decided and how we know ("Skriptá ho uvádzajú bez dôkazu."), [src]. */
    data class Omitted(val why: String, val src: String) : Proven
}

/**
 * A numbered statement of the textbook (Věta, Lemma, Tvrzení, Důsledek, Pozorování, Postup): one number, one card.
 * [src] names exactly that one number; a remark the card needs next to it goes to [remarks], each a card of its own.
 */
data class Theorem(
    val id: String,
    val name: String,
    val src: String,
    val wording: Wording,
    /** In plain words, Slovak. */
    val idea: String,
    val proven: Proven,
    /** Why the course cares: "Na teste: ..." */
    val note: String? = null,
    /** Its short name after the word "veta" in any case: "o prieniku podpriestorov" ("vetu o prieniku podpriestorov (Veta 2.18)"). */
    val short: String = "",
    /** Numbered remarks of the textbook shown with it (Varování 2.40), each its own card with its own number. */
    val remarks: List<Quote> = emptyList(),
) {
    /** The whole numbered text, word for word. */
    val czech: List<Line> get() = wording.lines
    /** The line-by-line proof, when the course proves it so. */
    val proof: Proof? get() = (proven as? Proven.Lines)?.proof
    /** Every line-by-line proof of it: the main one and those of its other parts. */
    val proofs: List<Proof> get() = (proven as? Proven.Lines)?.let { listOf(it.proof) + it.more }.orEmpty()
}

/** A numbered piece of the textbook quoted as it is ([src] "Skriptá, Varování 2.40"), a card of its own. */
data class Quote(val src: String, val lines: List<Line>)

/**
 * A proof built line by line. What it proves is its theorem's [Wording] (a proof of a theorem has no [wording] of
 * its own) or, for a proof task of a past test, [wording]: the Czech task word for word, cut into its parts. Each
 * round offers the next line, wrong ones say why. Ends with writing it on paper against [checklist].
 */
data class Proof(
    val id: String,
    val title: String,
    val src: String,
    /** A test's proof task, word for word; null for the proof of a theorem (its statement is the theorem's). */
    val wording: Wording?,
    val foot: String? = null,
    /** "Začiatok": the proof's first line, Slovak. */
    val given: String,
    val rounds: List<ProofRound>,
    val end: String,
    val checklist: List<String>,
    val minutes: Int = 10,
    /** A short title per round, what that round proves ("Neprázdnosť", "Druhá inklúzia"): the goals of the proof. */
    val goals: List<String> = emptyList(),
    /** What the theorem does not say, shown under the conclusion ("A zjednotenie? Nemusí byť podpriestor ..."). */
    val warn: String? = null,
) {
    /** What it proves: the test's task or its theorem's statement. */
    val what: Wording? get() = wording ?: theoremOf(this)?.wording
    /** What it proves as one text (search, coverage). */
    val task: String get() = what?.lines?.joinToString(" ", transform = ::lineText).orEmpty()
}

/** A line as text: a display block as a display formula. */
fun lineText(l: Line) = when (l) { is Para -> l.tex; is Item -> l.tex; is Block -> "$$" + l.tex + "$$" }

data class ProofChoice(val tex: String, val why: String? = null)
/**
 * One round: [prompt] asks for the next line, [options] offer it. [given] is what the student writes on paper before
 * that line when the prompt carries it: the round's assumption or heading ("(⇐): nech napr. …"). The
 * prompt disappears once the round is done; [line] is what stays, in the solution and on paper.
 */
data class ProofRound(val prompt: String, val options: List<ProofChoice>, val correct: Int, val given: String? = null) {
    /** The round as written on paper: its assumption, then its right line. */
    val line: String get() = listOfNotNull(given?.trimEnd(), options[correct].tex).joinToString(" ")
}

sealed interface Line
data class Para(val tex: String) : Line
data class Item(val tex: String) : Line
data class Block(val tex: String) : Line

/* ---- Lessons ------------------------------------------------------------------------------------------------ */

data class LessonDef(
    val id: String,
    val name: String,
    val minutes: Int,
    val learn: String,
    val steps: List<Step>,
    /** The art in the path and on Today, 0..8 as in the mockup. */
    val art: Int,
    /** Topic codes of the course schedule this lesson teaches (S2, MI2, TČ1): ties it to the lectures. */
    val topics: List<String> = emptyList(),
)

/**
 * One screen of a lesson. The order in a lesson follows the approved spec: a question first, an interactive
 * picture, the explanation, a worked example, your attempt, then a different problem without help.
 */
sealed interface Step {
    val id: String

    /** A guess before any explanation. No wrong answers count. [wrong]: what a wrong guess gets wrong, shown before [after]. */
    data class Ask(override val id: String, val q: String, val options: List<String>, val correct: Int, val after: String, val pic: Pic? = null, val sub: String? = null, val wrong: Map<Int, String> = emptyMap()) : Step

    /** Play with the picture until the goal is reached; then the reveal. */
    data class Explore(override val id: String, val title: String, val sub: String, val pic: Pic, val doneTitle: String, val doneBody: String? = null) : Step

    /**
     * Short explanation. [cards]: the definitions and theorems it rests on (ids), each drawn as its own card with its
     * own number; [formal] is a Czech quote that is not one of them (a remark, a test's wording), from [src].
     */
    data class Explain(override val id: String, val title: String, val body: String, val pic: Pic? = null, val formal: List<Line>? = null, val src: String? = null, val more: String? = null, val cards: List<String> = emptyList()) : Step

    /** A worked example revealed step by step, under the subgoals of its method (data/Postup.kt). */
    data class Worked(override val id: String, val title: String, val problem: String, val solution: Solution, val src: String? = null) : Step

    /** A check with feedback; [hint] makes the attempt "aided". [wrong]: per wrong option, the misconception behind it. */
    data class Check(override val id: String, val q: String, val options: List<String>, val correct: Int, val why: String, val hint: String? = null, val pic: Pic? = null, val wrong: Map<Int, String> = emptyMap(), val sub: String? = null) : Step

    /** Generated exercise. [solo]: no hint, no shown solution before answering. */
    data class Practice(override val id: String, val gen: String, val solo: Boolean, val title: String? = null) : Step

    /** A lecture clip: question before, captions, a task after. */
    data class Clip(override val id: String, val clip: String) : Step

    /** The definition in three layers, then from memory. */
    data class Def(override val id: String, val concept: String) : Step

    /**
     * A theorem and, if it has one, its proof line by line; [also] numbered statements shown after it, a card each.
     * [proof]: the proof this step drills when it is not the theorem's own (a test's proof of one of its points, p-nula
     * for Pozorování 2.25, or the proof of a statement in [also]).
     */
    data class Thm(override val id: String, val theorem: String, val also: List<String> = emptyList(), val proof: String? = null) : Step

    /** A proof from a past test, line by line, then on paper. */
    data class Prove(override val id: String, val proof: String) : Step
}

/** Interactive pictures. */
sealed interface Pic {
    /**
     * A picture a topic brings along (a Venn diagram, arrows of a mapping, a Euclid table): it draws itself and calls
     * onGoal when its goal is met (at once for a still picture). Implement it in the topic's own file under screens/.
     */
    interface Custom : Pic {
        @androidx.compose.runtime.Composable
        fun Draw(modifier: androidx.compose.ui.Modifier, onGoal: () -> Unit)
        /** What a screen reader says about it. */
        val describe: String
    }

    /** Two vectors with coefficient sliders; reach the target if there is one. */
    data class Plane(val u: Pair<Int, Int>, val v: Pair<Int, Int>, val target: Pair<Int, Int>?, val range: Int = 3, val goal: Goal = Goal.Reach) : Pic
    /** Tap the points that belong to the span. */
    data class TapSpan(val u: Pair<Int, Int>, val v: Pair<Int, Int>?, val points: List<Pair<Int, Int>>) : Pic
    /** Is this set closed? Pick two of its points, see the sum and a multiple. */
    data class Closure(val set: SetKind) : Pic
    /**
     * Z_p^n as dots: n = 2 a p×p grid (pick vectors, see everything they generate, a single vector's multiples
     * numbered with the wrap-around modulo p), n = 3 with p = 2 the eight labelled corners of a cube. With
     * [Goal.Subspaces] the student finds every subspace of dimension [dim], each once: another basis of a subspace
     * already found is named as the same subspace, not counted.
     */
    data class Grid(val p: Int, val start: List<Pair<Int, Int>> = emptyList(), val goal: Goal = Goal.Free, val target: Int? = null, val n: Int = 2, val dim: Int = 1) : Pic
    /** A basis grid: find the coordinates of x by sliding along u and v. */
    data class Coords(val u: Pair<Int, Int>, val v: Pair<Int, Int>, val x: Pair<Int, Int>) : Pic
    /** A fixed picture: vectors and an optional span line. */
    data class Still(val vectors: List<Pair<Int, Int>>, val span: Pair<Int, Int>? = null, val target: Pair<Int, Int>? = null, val labels: List<String> = emptyList()) : Pic
    /** GEM walked step by step on a given matrix, over R (p = 0) or Z_p. */
    data class Gem(val rows: List<List<Int>>, val p: Int, val bar: Int? = null, val caption: String? = null) : Pic
}

/**
 * Reach: hit the target. Zero: a nontrivial combination equal to θ. Explore: look around (done after a few moves).
 * Basis: pick a basis of the grid. Subspaces: find every subspace of the grid's dimension, each once.
 */
enum class Goal { Reach, Zero, Explore, Free, Count, Basis, Subspaces }

enum class SetKind(val tex: String, val closed: Boolean) {
    LineThroughZero("y=2x", true),
    LineOffZero("y=x+1", false),
    Cross("x=0\\ \\text{alebo}\\ y=0", false),
    Abs("|x|=|y|", false),
    HalfPlane("x\\geq 1", false),
    Quadrant("x\\geq 0,\\ y\\geq 0", false),
}

/* ---- Exercises (generated, checked by the same code that made them) ---------------------------------------- */

/**
 * An exercise of a test template. [parts] are answered in order; a later part may depend on what you picked
 * earlier (your own basis), so each part is built from the answers so far.
 */
data class Exercise(
    val template: String,
    val label: String,
    val src: String,
    val prompt: String,
    val visual: String? = null,
    val parts: List<(List<Any>) -> Part>,
    /** The method of this problem type: shown as "Ako na to?" and as the goals of the solution. */
    val postup: Postup,
    /** The full solution, as it would go on paper, step by step under the subgoals of [postup]. */
    val solution: (List<Any>) -> Solution,
    /** The hint of a single-part exercise; parts of a longer one carry their own ([Part.hint]). */
    val hint: String,
)

/**
 * One part of an exercise. [aim]: the goal of the exercise's method (index into [Exercise.postup]) this part works on,
 * "Ako na to?" opens on it. [hint]: the hint for this part only; a part of a multi-part exercise without one offers no
 * hint (Teo, 17. 11.: the hint is about the part on screen). A single-part exercise uses [Exercise.hint].
 */
data class Part(val q: String, val answer: Answer, val after: String? = null, val hint: String? = null, val aim: Int? = null)

sealed interface Answer {
    /** [wrong]: per wrong option, the misconception behind it. */
    data class Choice(val options: List<String>, val correct: Int, val why: String, val wrong: Map<Int, String> = emptyMap()) : Answer
    /**
     * Numbers typed on a keypad, compared exactly with [expected]; [shown] is how the answer is written. [diagnose]
     * names the likely mistake behind a wrong answer, or the step to check when no known mistake fits.
     */
    data class Numbers(val labels: List<String>, val p: Int?, val expected: List<Frac>, val shown: String, val fractions: Boolean = false, val diagnose: ((List<Frac>) -> String)? = null) : Answer
    /** Pick some of the options; [ok] judges the set (several sets can be right). */
    data class Pick(val options: List<String>, val count: IntRange, val ok: (Set<Int>) -> Boolean, val why: (Set<Int>) -> String) : Answer
    /**
     * The student's own vectors, written component by component: every vector of a span, every subspace (each by a
     * basis of [group] vectors), another basis, the rows of [A|b]. [cells] label one vector's components; [p] the
     * Z_p keypad, null for integers and fractions. [count]: how many entries; an open range means the student decides
     * how many (an enumeration, judged for completeness). [entry] names one entry in the accusative ("prvok", "riadok").
     * [check] judges the whole list by its property, never against one sample answer; [example] is one right answer
     * (tests, the debug walker). Helpers in data/Vectors.kt.
     */
    data class Vectors(
        val cells: List<String>, val p: Int?, val count: IntRange, val group: Int = 1, val entry: String,
        val check: (List<List<dev.hamper.tabula.la.Vec>>) -> VectorsResult, val example: List<List<dev.hamper.tabula.la.Vec>>,
        /** Vectors one tap puts in the list ("e₁" to (1,0,0,0)): the usual entries, typed cell by cell otherwise. */
        val quick: List<Pair<String, dev.hamper.tabula.la.Vec>> = emptyList(),
    ) : Answer {
        /** The student decides how many entries: an enumeration. */
        val open get() = count.first != count.last
    }
    /**
     * Written on paper and compared with a model, one way as everywhere on paper (screens/NaPapier.kt): photograph it for
     * the tutor's verdict, or say "Mám to" / "Nemám". Nothing to tick. [sample]: the model lines, shown only once the
     * student has written theirs; [look]: what a grader looks for (folded background, the tutor's checklist); [miss]:
     * the feedback after "Nemám", its first sentence what the text must contain. The value kept for a done part is 1.
     */
    data class Paper(val sample: List<String>, val look: List<String>, val miss: String) : Answer
}

/** The verdict on the student's own list: [head] the summary (its first sentence the verdict), [each] per entry what is wrong with it (null = fine). */
data class VectorsResult(val ok: Boolean, val head: String, val each: List<String?>)

/* ---- Test papers ------------------------------------------------------------------------------------------ */

/**
 * A problem of a test paper. [text] is the Czech task word for word; [checklist] the rubric lines adding up to [pts].
 * [parts] are the separate things the task asks (a sentence or a few of [text] each, in order, joined by spaces they
 * give [text]); build a split problem with [paperProblem]. Without parts the whole problem is one part ([units]).
 * [note]: the paper's own instruction for this problem ("V tomto příkladu nemusíte své výpočty komentovat."), shown
 * under it instead of the course's header. Rules: docs/CONTENT.md, "Past papers: problems and parts".
 */
data class PaperProblem(
    val n: Int, val pts: Int, val title: String, val text: String, val minutes: Int, val checklist: List<Rubric>, val answer: String,
    val drill: String? = null, val solution: Solution? = null, val parts: List<PaperPart> = emptyList(), val note: String? = null,
    val own: OwnBasis? = null,
) {
    /** The parts to show and grade: [parts], or the whole problem as one part. */
    val units: List<PaperPart> get() = parts.ifEmpty { listOf(PaperPart("", text, pts, checklist, solution, answer, estimate = false)) }

    /** The problem with its later parts computed in the student's own basis, from the vectors they entered ([OwnBasis]). */
    fun inBasis(entered: List<dev.hamper.tabula.la.Vec>): PaperProblem =
        own?.let { o -> paperProblem(n, title, minutes, o.build(o.basis(entered)), drill, note, o) } ?: this
}

/**
 * A part whose result is one of many right ones (a completion to a basis, a basis of a span) and later parts that
 * are computed from it (Teo, 18. 11.: "ja som vypočítal inú bázu ... ako ja mám potvrdiť, že to mám dobre"). The
 * student may enter their own after part [part]: [input] takes and checks it (the vectors added to the given ones, or
 * a whole basis), [basis] makes the basis of what was entered, [build] all the problem's parts in a basis (the
 * points and checklists stay, the solutions follow the basis), [gem] shows by GEM why a dependent set is no basis (null for an LN one). Built in
 * data/Papers.kt; one mechanism for every past and generated problem of this kind.
 */
class OwnBasis(
    val part: Int, val input: Answer.Vectors, val basis: (List<dev.hamper.tabula.la.Vec>) -> List<dev.hamper.tabula.la.Vec>,
    val build: (List<dev.hamper.tabula.la.Vec>) -> List<PaperPart>, val gem: (List<dev.hamper.tabula.la.Vec>) -> Solution?,
)

/**
 * One thing a problem asks, graded on its own: [text] is its slice of the Czech task, word for word. [pts] is its share
 * of the problem's points with [checklist] adding up to it; [estimate] says the share is ours (the papers give points
 * per problem only), false when the paper or the graders state it. [solution] is the model solution revealed step by
 * step along its Postup (null: no written solution, only [answer]), [answer] the result in one line. [review]: the route of what to go through again when the part costs points
 * ("concept:ln", "proof:p-lzlk", "gen:complete", "lesson:obal:12"); null = from the first rubric line with a fix.
 */
data class PaperPart(
    val label: String, val text: String, val pts: Int, val checklist: List<Rubric>, val solution: Solution?,
    val answer: String? = null, val estimate: Boolean = true, val review: String? = null,
)

/** The parts' solutions as one: a single part's own, or one subgoal per part ("a) ...", "b) ..."). */
private fun partsSolution(ps: List<PaperPart>): Solution? {
    val sols = ps.mapNotNull { p -> p.solution?.let { "${p.label} ${it.postup[0].title}".trim() to it } }
    return when (sols.size) { 0 -> null; 1 -> sols[0].second; else -> parts(*sols.toTypedArray()) }
}

/**
 * A problem made of parts: labels a), b), ... when there is more than one, the text, points, checklist, answer and
 * solution put together from the parts.
 */
fun paperProblem(n: Int, title: String, minutes: Int, parts: List<PaperPart>, drill: String? = null, note: String? = null, own: OwnBasis? = null): PaperProblem {
    val ps = if (parts.size > 1) parts.mapIndexed { i, p -> p.copy(label = "${'a' + i})") } else parts.map { it.copy(label = "") }
    return PaperProblem(
        n, ps.sumOf { it.pts }, title, ps.joinToString(" ") { it.text }, minutes, ps.flatMap { it.checklist },
        ps.mapNotNull { p -> p.answer?.let { if (p.label.isEmpty()) it else "**${p.label}** $it" } }.joinToString(" "),
        drill, partsSolution(ps), ps, note, own,
    )
}
data class Rubric(val text: String, val pts: Int, val fix: String? = null)
/** [exam]: the Exam id this paper is a past version of; null = the course's first exam the app prepares for. */
/** [missing]: problems left out until their lecture (Shown.offered), named for the note on the paper. */
data class TestPaper(val id: String, val name: String, val src: String, val problems: List<PaperProblem>, val generated: Boolean = false, val exam: String? = null, val missing: List<PaperProblem> = emptyList())

package dev.hamper.tabula.data

import dev.hamper.tabula.math.Seg
import dev.hamper.tabula.math.segments
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Statements are structured, so every screen draws them whole and right (docs/CONTENT.md, "Statements and proofs"):
 * the hypotheses apart from the claim, a core for the narrow card and the "Dokazuješ" bar, the parts a proof proves on
 * its own, how each theorem is proved (never silently not), one number per card, a definition's check from memory
 * only over what its task asks, and every numbered reference in any text opening a card.
 *
 * Each rule lists every item that breaks it, so one run is the whole to-do list.
 */
class WordingTest {
    private fun lineTex(l: Line) = when (l) { is Para -> l.tex; is Item -> l.tex; is Block -> l.tex }

    /** "Skriptá, Věta 2.18" has one number; "Věty 2.56, 2.57 a Důsledek 2.58" three. */
    private fun numbers(src: String) = Regex("""\d+\.\d+""").findAll(src).count() + Regex("""\d+\.\d+\s+až\s+\d+\.\d+""").findAll(src).count()

    /** Words a Czech statement opens its hypotheses with. */
    private val opensHypotheses = Regex("""^\s*(Nechť|Buď|Buďte|Mějme|Je-li|Jsou-li|Pokud|Jestliže|Uvažujme)\b""")

    private fun check(rule: String, bad: List<String>) = assertEquals("$rule (${bad.size}):\n" + bad.joinToString("\n"), emptyList<String>(), bad)

    private val theorems get() = Courses.flatMap { it.theorems }.distinctBy { it.id }
    private val proofs get() = Courses.flatMap { it.proofs }.distinctBy { it.id }
    private val concepts get() = Courses.flatMap { it.concepts }.distinctBy { it.id }

    @Test fun every_theorem_says_how_it_is_proved() = check("a theorem without a stated proof (Proven.Lines, Short with its argument, Omitted with who decided)",
        theorems.mapNotNull { t ->
            when (val p = t.proven) {
                is Proven.Lines -> null
                is Proven.Short -> if (p.argument.isBlank() || p.src.isBlank()) "${t.id}: short proof without its argument or source" else null
                is Proven.Omitted -> if (p.why.isBlank() || p.src.isBlank()) "${t.id} (${t.src}): no proof and no reason" else null
            }
        })

    @Test fun one_number_per_card() = check("a card names more than one numbered statement (split it: one card per number, Step.Thm also / explain cards)",
        theorems.filter { numbers(it.src) > 1 }.map { "${it.id}: ${it.src}" } +
            concepts.filter { numbers(it.src) > 1 }.map { "${it.id}: ${it.src}" } +
            theorems.flatMap { t -> t.remarks.filter { numbers(it.src) != 1 }.map { "${t.id} remark: ${it.src}" } })

    @Test fun hypotheses_apart_from_the_claim() = check("the claim opens with its hypotheses (move them to assume, word for word)",
        (theorems.map { it.id to it.wording } + proofs.mapNotNull { p -> p.wording?.let { p.id to it } }).mapNotNull { (id, w) ->
            val first = w.claim.firstOrNull()?.let(::lineTex).orEmpty()
            when {
                w.claim.isEmpty() -> "$id: no claim"
                w.assume.isEmpty() && opensHypotheses.containsMatchIn(first) -> "$id: ${first.take(70)}"
                else -> null
            }
        })

    @Test fun a_core_where_a_card_or_a_bar_needs_one() = check("no core (one display formula of the claim, unbroken at 320 dp)",
        theorems.filter { it.wording.core.isNullOrBlank() && it.proof != null }.map { "${it.id}: proved line by line" } +
            proofs.filter { p -> p.wording != null && p.wording.core.isNullOrBlank() }.map { "${it.id}: a test's proof task" })

    /** A core is the gist one glance takes in, not the statement again: a list of laws has none and is shown whole. */
    @Test fun a_core_is_short() = check("a core or part longer than five rows (leave the core out, the card shows the whole wording)",
        (theorems.map { it.id to it.wording } + proofs.mapNotNull { p -> p.wording?.let { p.id to it } }).flatMap { (id, w) ->
            (listOfNotNull(w.core) + w.parts.map { it.tex }).filter { it.split("\\\\").size > 5 }.map { "$id: $it" }
        })

    @Test fun core_and_parts_are_formulas() = check("a core or part that is not one formula",
        (theorems.map { it.id to it.wording } + proofs.mapNotNull { p -> p.wording?.let { p.id to it } }).flatMap { (id, w) ->
            (listOfNotNull(w.core) + w.parts.map { it.tex }).mapNotNull { tex ->
                when {
                    '§' in tex || '$' in tex -> "$id: math markup inside raw TeX: $tex"
                    Regex("""\\\\(?![a-zA-Z])""").containsMatchIn(tex.replace(Regex("""\\begin\{(aligned|array|[pv]?matrix)\}.*?\\end\{(aligned|array|[pv]?matrix)\}""", RegexOption.DOT_MATCHES_ALL), "")) -> "$id: a line break outside aligned/array: $tex"
                    else -> null
                }
            }
        })

    @Test fun parts_follow_the_proof() = check("parts of a claim out of step with its proof",
        theorems.flatMap { t ->
            val w = t.wording; val p = t.proof
            buildList {
                if (w.parts.size == 1) add("${t.id}: a single part is the claim itself")
                w.parts.forEach { if (it.label.isBlank() || it.tex.isBlank()) add("${t.id}: part without label or formula") }
                if (p != null && w.parts.isNotEmpty()) {
                    val from = w.parts.map { it.from }
                    if (from.first() != 0) add("${t.id}: the first part starts at round ${from.first()}, not 0")
                    if (from.zipWithNext().any { (a, b) -> b <= a }) add("${t.id}: parts' rounds not increasing: $from")
                    if (from.last() >= p.rounds.size) add("${t.id}: a part starts after the last round")
                }
            }
        })

    @Test fun what_a_proof_proves_is_stated_once() = check("a proof's statement given twice or not at all",
        proofs.mapNotNull { p ->
            val th = theoremOf(p)
            when {
                th != null && p.wording != null -> "${p.id}: proves ${th.id}, so its wording is the theorem's (statement = null)"
                th == null && p.wording == null -> "${p.id}: a test's proof task without its wording"
                else -> null
            }
        })

    @Test fun a_display_formula_per_statement_at_most() = check("more than one key formula in a claim (the core is the one line)",
        theorems.filter { t -> t.wording.claim.sumOf { l -> segments(lineTex(l).replace('§', '$')).count { it is Seg.Display } + (if (l is Block) 1 else 0) } > 3 }.map { it.id })

    @Test fun units_are_what_the_task_asks() = check("a definition's units: what its task asks, the rest as extra",
        concepts.flatMap { k ->
            buildList {
                if (k.units.isEmpty()) add("${k.id}: no units")
                (k.units intersect k.extra.toSet()).forEach { add("${k.id}: '$it' both required and extra") }
                if (k.units.size > 6) add("${k.id}: ${k.units.size} units; a definition asked on a test has a few ideas, the rest is extra")
            }
        })

    @Test fun cards_on_a_step_are_known_and_quotes_are_not_cards() {
        val bad = ArrayList<String>()
        for (c in Courses) {
            val ix = RefIndexes.getValue(c.id)
            for (l in c.lessons) for (s in l.steps) when (s) {
                is Step.Explain -> {
                    s.cards.map { dev.hamper.tabula.ui.cardId(it) }.filter { it !in ConceptById && it !in TheoremById }.forEach { bad += "${s.id}: unknown card $it" }
                    val src = s.src
                    if (s.formal != null && src != null) {
                        if (numbers(src) > 1) bad += "${s.id}: one quote, several numbers: $src"
                        // a numbered statement the app has a card for is shown as that card, never copied (a remark's quote
                        // is no card: the step's own quote is what its references open)
                        ix.scan(src).firstOrNull { it.target != null && it.target !is RefTarget.Quote }?.let { bad += "${s.id}: quotes ${it.word} ${it.number}; show its card (cards = listOf(\"${it.target!!.id}\"))" }
                    }
                }
                is Step.Thm -> {
                    s.also.filter { it !in ConceptById && it !in TheoremById }.forEach { bad += "${s.id}: unknown card $it" }
                    s.proof?.let { if (it !in ProofById) bad += "${s.id}: unknown proof $it" }
                }
                else -> {}
            }
        }
        check("cards of a step", bad)
    }

    @Test fun nsd_and_nsn_are_gcd_and_lcm() = check("NSD or NSN (the course writes gcd and lcm, Definice 6.3; Dombek)",
        Courses.flatMap { c ->
            c.lessons.map { it.id to it.name } + c.concepts.flatMap { k -> listOf(k.id to k.name, k.id to k.short, k.id to k.question) + k.units.map { k.id to it } } +
                c.theorems.flatMap { t -> listOf(t.id to t.name, t.id to t.short, t.id to t.idea, t.id to (t.note ?: "")) }
        }.filter { (_, s) -> Regex("""\bNS[DN]\b""").containsMatchIn(s) }.map { (id, s) -> "$id: $s" } +
            RefNames.values.flatten().filter { Regex("""\bNS[DN]\b""").containsMatchIn(it.short) }.map { "ref ${it.kind} ${it.number}: ${it.short}" })
}

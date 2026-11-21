package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.dml.MiPowers
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/** Domains belong to the premise, never to a revealed answer (ZASADY, Teleso a obor v zadaní). */
class DomainTest {
    private val concrete = Regex("""\\begin\{[pbv]?matrix(?:\|\d+)?\}\s*-?\d|\(\s*-?\d+\s*,\s*-?\d+|\d+x\s*\+\s*\d+y\s*=""")
    private val numberDomain = Regex("""\\mathbb\{[RZNCQ]\}|[ℝℤℕℂℚ]|(?i:celočísel|celých čísel|reáln|diofant|diophant|lineární kongruenc|lineárn[ae] kongruenc)|\\pmod|modulo(?:\s|§|\$)+\d+""")
    private val fieldDomain = Regex("""\\mathbb\{[RQC]\}|\\mathbb\{Z\}_|[ℝℂℚ]|ℤ[_₀-₉]|\\in\s*T|T\^|(?i:reáln|nad.*teles|nad.*těles)""")
    private val integerContract = Regex("""\\(?:gcd|lcm|varphi|bmod)|(?i:nesúdeliteľ|nesouděln|Euklid|Bézout|REA|[Nn]ejvětší společný dělitel|delenie so zvyškom|dělení se zbytkem)""")

    /** Only genuinely undecidable cases go here, with the mathematical reason. Stale exceptions fail. */
    private val allowed = mapOf(
        "concept:dml-mz-kart/judge:2" to "Cartesian products of explicitly enumerated sets, no scalar arithmetic.",
        "concept:dml-mz-kart/why:0" to "Order of a pair in a Cartesian product is independent of a number field.",
        "concept:dml-mz-vice/judge:2" to "Membership in a product of three explicitly enumerated singleton sets.",
        "concept:dml-mz-konecne/judge:2" to "The open interval (0,1) uses the course's usual real-interval notation.",
        "concept:dml-mz-konecne/why:2" to "The example (0,1) is a real interval, not a pair of vector coordinates.",
        "dml-tc-diofant-0" to "Whole coins, with change allowed, specify signed integer counts.",
        "dml-tc-diofant-5" to "Whole runs of the two hourglasses specify integer combinations of durations.",
        "gen:dml-tc-slovna" to "Calendar periods and whole counts in the stated word problems are integers.",
        "gen:dml-tc-slovna-ldr" to "Whole purchased items, coins and complete hourglass runs specify integer counts.",
        "clip:k3-nasobenie-stlpce-ab/task" to "Symbolic column-product identity holds over every field; (2,2) is a position.",
        "clip:k3-hodnost-sucinu/task" to "Symbolic rank inequality and the 0/1 counterexample hold over every field.",
    )
    private val used = mutableSetOf<String>()
    private val bad = linkedSetOf<String>()

    private fun inspect(id: String, visible: String, evidence: String = visible, typed: Boolean = false) {
        if (!typed && !concrete.containsMatchIn(evidence)) return
        val algebra = typed && !id.contains("dml") ||
            Regex("""\\begin\{[pbv]matrix""").containsMatchIn(evidence) ||
            (!id.contains("dml") && concrete.containsMatchIn(evidence))
        if (if (algebra) fieldDomain.containsMatchIn(visible) else numberDomain.containsMatchIn(visible) || integerContract.containsMatchIn(visible)) return
        val exception = if (id in allowed) id else id.substringBefore("/part:")
        allowed[exception]?.let { used += exception; return }
        bad += "$id: missing visible domain: ${visible.take(180)}"
    }

    private fun caption(p: Pic?): String = (p as? Pic.Gem)?.caption.orEmpty()
    private fun mathematical(p: Pic?) = p is Pic.Plane || p is Pic.TapSpan || p is Pic.Closure || p is Pic.Coords || p is Pic.Gem || p is Pic.Grid || p is Pic.Still || p is MiPowers

    private fun fieldMatches(id: String, visible: String, p: Int) {
        val plain = visible.replace("{", "").replace("}", "").replace(" ", "")
        val declared = if (p == 0) "\\mathbbR" in plain || "ℝ" in plain
            else Regex("""\\mathbbZ_${p}(?![0-9])""").containsMatchIn(plain)
        if (!declared) bad += "$id: picture/answer field ${if (p == 0) "R" else "Z_$p"} is not declared in its visible premise"
    }

    private fun pictureField(s: Step) {
        val (pic, visible) = when (s) {
            is Step.Ask -> s.pic to listOfNotNull(s.q, s.sub, caption(s.pic)).joinToString(" ")
            is Step.Check -> s.pic to listOfNotNull(s.q, s.sub, caption(s.pic)).joinToString(" ")
            is Step.Explore -> s.pic to "${s.title} ${s.sub} ${caption(s.pic)}"
            else -> return
        }
        val p = when (pic) {
            is Pic.Gem -> pic.p
            is Pic.Grid -> pic.p
            is Pic.Plane, is Pic.Coords, is Pic.Closure, is Pic.TapSpan, is Pic.Still, is MiPowers -> 0
            else -> return
        }
        fieldMatches(s.id, visible, p)
    }

    private fun step(s: Step) {
        pictureField(s)
        when (s) {
            is Step.Ask -> inspect(s.id, listOfNotNull(s.q, s.sub, caption(s.pic)).joinToString(" "), (listOfNotNull(s.q, s.sub, s.after) + s.options + s.wrong.values).joinToString(" "), mathematical(s.pic))
            is Step.Check -> inspect(s.id, listOfNotNull(s.q, s.sub, caption(s.pic)).joinToString(" "), (listOfNotNull(s.q, s.sub, s.why) + s.options + s.wrong.values).joinToString(" "), mathematical(s.pic))
            is Step.Explore -> inspect(s.id, "${s.title} ${s.sub} ${caption(s.pic)}", "${s.title} ${s.sub} ${s.doneTitle} ${s.doneBody}", mathematical(s.pic))
            is Step.Worked -> inspect(s.id, "${s.title} ${s.problem}", s.problem + s.solution.steps.joinToString { it.say })
            // Resolved below through the full registries, including unoffered topics.
            else -> Unit
        }
    }

    private fun concept(c: Concept) {
        // ConceptFlow shows a judge on its own screen: the earlier formal definition is not its premise.
        val formal = (c.czech + c.remarks.flatMap { it.lines }).joinToString(" ", transform = ::lineText)
        c.judge.forEachIndexed { i, j -> inspect("concept:${c.id}/judge:$i", c.name + " " + c.question + " " + j.tex, j.tex + j.because + j.wrong) }
        c.why.forEachIndexed { i, w -> inspect("concept:${c.id}/why:$i", c.name + " " + w.part, w.part + w.because) }
        c.trap?.let { inspect("concept:${c.id}/trap", c.name + " " + it.sentence, it.sentence + it.because) }
        inspect("concept:${c.id}/memory", formal + " " + c.ask + " " + c.units.joinToString(" "))
    }

    private fun exercise(id: String, ex: Exercise) {
        // The prompt and visual remain above every part in ExerciseView. Feedback, hints and solution are excluded.
        val premise = ex.prompt + " " + ex.visual.orEmpty()
        var paths: List<List<Any>> = listOf(emptyList())
        for ((i, build) in ex.parts.withIndex()) {
            paths = paths.flatMap { prior ->
                val p = build(prior)
                val a = p.answer
                val typed = a is Answer.Numbers || a is Answer.Vectors
                inspect("gen:$id/part:$i", premise + " " + p.q, typed = typed)
                when (a) {
                    is Answer.Vectors -> fieldMatches("gen:$id/part:$i", premise + " " + p.q, a.p ?: 0)
                    is Answer.Numbers -> a.p?.let { fieldMatches("gen:$id/part:$i", premise + " " + p.q, it) }
                    else -> Unit
                }
                valid(a).map { prior + listOf(it) }
            }
        }
    }

    /** Every valid discrete pick, plus alternate valid bases (order and nonzero scaling), not only the sample. */
    private fun valid(a: Answer): List<Any> = when (a) {
        is Answer.Choice -> listOf(a.correct)
        is Answer.Numbers -> listOf(a.expected)
        is Answer.Paper -> listOf(1)
        is Answer.Pick -> (0 until (1 shl a.options.size)).map { mask -> a.options.indices.filter { mask shr it and 1 != 0 }.toSet() }.filter { it.size in a.count && a.ok(it) }
        is Answer.Vectors -> {
            val f = a.p?.let(Field::Zp) ?: Field.R
            val candidates = listOf(a.example, a.example.reversed(), a.example.map { group -> group.map { f.scale(Frac.of(2), it) } })
            candidates.distinct().filter { a.check(it).ok }
        }
    }

    private fun paper(id: String, p: PaperProblem) {
        // PaperView keeps the whole original problem visible while its parts are answered.
        val context = p.text + " " + p.note.orEmpty()
        p.units.forEachIndexed { i, u -> inspect("paper:$id/${p.n}/$i", context + " " + u.text) }
        p.own?.let { own ->
            val entered = own.input.example.map { it.single() }
            for (e in listOf(entered, entered.reversed())) {
                if (own.input.check(e.map { listOf(it) }).ok) {
                    val variant = p.inBasis(e)
                    variant.units.forEachIndexed { i, u -> inspect("paper:$id/${p.n}/own:$i", variant.text + " " + u.text) }
                }
            }
        }
    }

    @Test fun every_mathematical_task_declares_its_domain_before_answering() {
        installJvmAssets()
        ClipLibrary.load()
        Courses.flatMap { it.lessons }.flatMap { it.steps }.forEach(::step)
        Courses.flatMap { it.concepts }.forEach(::concept)
        for (c in Courses) {
            for (t in c.theorems) inspect("theorem:${t.id}", t.czech.joinToString(" ", transform = ::lineText))
            for (p in c.proofs) for ((i, r) in p.rounds.withIndex()) {
                val context = p.task + " " + p.given + " " + r.given.orEmpty() + " " + r.prompt
                // Universally quantified vector-space statements retain their formal hypotheses in the proof bar.
                inspect("proof:${p.id}/$i", context, context + r.options.joinToString { it.tex })
            }
            for (p in c.papers) p.problems.forEach { paper(p.id, it) }
            repeat(20) { seed -> c.generatedPaper(seed).let { p -> p.problems.forEach { paper(p.id, it) } } }
        }
        for (c in ClipLibrary.all) {
            // 'before' is a separate earlier phase. The question after playback must carry its own context.
            inspect("clip:${c.id}/before", c.before)
            inspect("clip:${c.id}/task", c.task.q, c.task.q + c.task.options.joinToString(" ") + c.task.why + c.task.wrong.values.joinToString(" "))
        }
        for ((id, gen) in Generators) repeat(150) { seed -> exercise(id, gen(Random(seed * 7919 + id.hashCode()))) }
        assertTrue(bad.joinToString("\n"), bad.isEmpty())
        assertEquals("Stale domain exceptions", allowed.keys, used)
    }

    @Test fun feedback_and_hidden_metadata_cannot_supply_a_domain() {
        val q = "§h\\begin{pmatrix}1&3&1\\\\2&1&-1\\end{pmatrix}=§"
        step(Step.Check("missing", q, listOf("1", "2", "3"), 1, "Nad ℝ má hodnosť 2.", sub = null))
        step(Step.Ask("ask", q, listOf("1", "2"), 1, "Nad ℝ.", wrong = mapOf(0 to "Nad ℝ.")))
        step(Step.Explore("picture", "Krokuj", "Matica", Pic.Gem(listOf(listOf(1)), 5), "Nad ℤ_5"))
        assertEquals(setOf("missing", "ask", "picture"), bad.map { it.substringBefore(':') }.toSet())
    }
    @Test fun domains_in_reveals_do_not_repair_other_task_families() {
        val q = "h = ?"
        val worked = Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>().first()
        step(worked.copy(id = "worked", title = q, problem = "$(1,2)+(2,3)$"))
        val c = ConceptById.getValue("la1-k3-hodnost")
        concept(c.copy(czech = listOf(Para("Matica v \$T^{m,n}$.")), judge = listOf(Judge(
            "Hodnosť $(1,2)$ je 1.", true, "Nad ℝ.", "Nad ℝ."))))
        val ex = generateSequence(0) { it + 1 }.map { Generators.getValue("la1-k3-product")(Random(it)) }.first { it.parts.size == 1 }
        exercise("mutation", ex.copy(prompt = q, visual = null, parts = listOf({ _ -> Part(
            "Súčin", Answer.Numbers(listOf("x"), null, listOf(Frac.ONE), "Nad ℝ: 1"), after = "Nad ℝ.") })))
        inspect("clip", "Hodnosť $(1,2)$?", "Hodnosť $(1,2)$? Nad ℝ.")
        assertTrue(bad.any { it.startsWith("worked:") })
        assertTrue(bad.any { it.startsWith("concept:${c.id}/judge:0:") })
        assertTrue(bad.any { it.startsWith("gen:mutation/part:0:") })
        assertTrue(bad.any { it.startsWith("clip:") })
    }

    @Test fun rendered_domains_match_the_typed_field() {
        for (p in listOf(0, 2, 3, 5, 7)) {
            val f = if (p == 0) Field.R else Field.Zp(p)
            val visible = fieldTask(f, "Urči hodnosť.")
            fieldMatches("right-$p", visible, p)
            assertTrue(bad.joinToString(), bad.isEmpty())
            fieldMatches("wrong-$p", visible, if (p == 0) 5 else 0)
            assertTrue(bad.any { it.startsWith("wrong-$p:") })
            bad.clear()
        }
    }

}

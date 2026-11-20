package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * A number never stands alone (Teo: "pre bežného smrteľníka nič nehovoria"): every reference to a definition or a
 * theorem in any text of a course has a name in the course's registry, and every definition and theorem has its own.
 */
class RefNamesTest {
    private fun lineTex(l: Line) = when (l) { is Para -> l.tex; is Item -> l.tex; is Block -> "" }

    /** Every text a course shows, with where it is. Generated exercises with a few seeds. */
    private fun texts(c: Course): List<Pair<String, String>> {
        val out = ArrayList<Pair<String, String>>()
        fun add(w: String, s: String?) { if (!s.isNullOrBlank()) out += w to s }
        fun sol(w: String, so: Solution?) { so ?: return; so.steps.forEach { st -> add(w, st.say); st.math.forEach { add(w, it.note) } }; so.postup.aims.forEach { add(w, it.title); add(w, it.how) } }
        for (l in c.lessons) for (s in l.steps) when (s) {
            is Step.Ask -> { add(s.id, s.q); s.options.forEach { add(s.id, it) }; add(s.id, s.after); add(s.id, s.sub); s.wrong.values.forEach { add(s.id, it) } }
            is Step.Explore -> { add(s.id, s.title); add(s.id, s.sub); add(s.id, s.doneTitle); add(s.id, s.doneBody) }
            is Step.Explain -> { add(s.id, s.title); add(s.id, s.body); add(s.id, s.more); add(s.id, s.src); s.formal?.forEach { add(s.id, lineTex(it)) } }
            is Step.Thm -> {}
            is Step.Worked -> { add(s.id, s.title); add(s.id, s.problem); add(s.id, s.src); sol(s.id, s.solution) }
            is Step.Check -> { add(s.id, s.q); s.options.forEach { add(s.id, it) }; add(s.id, s.why); add(s.id, s.hint); add(s.id, s.sub); s.wrong.values.forEach { add(s.id, it) } }
            else -> {}
        }
        for (k in c.concepts) {
            add(k.id, k.src); k.czech.forEach { add(k.id, lineTex(it)) }; k.why.forEach { add(k.id, it.part); add(k.id, it.because) }
            k.judge.forEach { add(k.id, it.tex); add(k.id, it.because); add(k.id, it.wrong) }; k.units.forEach { add(k.id, it) }; k.extra.forEach { add(k.id, it) }
            k.trap?.let { add(k.id, it.sentence); add(k.id, it.because); add(k.id, it.src) }; add(k.id, k.ask)
        }
        for (t in c.theorems) {
            add(t.id, t.src); t.czech.forEach { add(t.id, lineTex(it)) }; add(t.id, t.idea); add(t.id, t.note)
            when (val p = t.proven) { is Proven.Short -> { add(t.id, p.argument); add(t.id, p.src) }; is Proven.Omitted -> add(t.id, p.why); else -> {} }
            t.remarks.forEach { q -> add(t.id, q.src); q.lines.forEach { add(t.id, lineTex(it)) } }
        }
        for (p in c.proofs) {
            listOf(p.src, p.task, p.foot, p.given, p.end, p.warn).forEach { add(p.id, it) }; p.checklist.forEach { add(p.id, it) }; p.goals.forEach { add(p.id, it) }
            p.rounds.forEach { r -> add(p.id, r.prompt); r.options.forEach { o -> add(p.id, o.tex); add(p.id, o.why) } }
        }
        for (paper in c.papers) for (pp in paper.problems) {
            val w = "${paper.id}/${pp.n}"
            add(w, pp.text); add(w, pp.answer); add(w, pp.drill); pp.checklist.forEach { add(w, it.text); add(w, it.fix) }; sol(w, pp.solution)
            pp.parts.forEach { part -> add(w, part.label); add(w, part.text); part.checklist.forEach { add(w, it.text); add(w, it.fix) }; sol(w, part.solution) }
        }
        for (ti in c.templates) {
            val gen = Generators[ti.gen] ?: continue
            repeat(4) { seed ->
                val ex = gen(Random(seed * 7919 + ti.gen.hashCode()))
                val w = "${ti.gen}/$seed"
                add(w, ex.prompt); add(w, ex.src); add(w, ex.hint)
                val answers = ArrayList<Any>()
                for (build in ex.parts) {
                    val part = build(answers)
                    add(w, part.q); add(w, part.after)
                    when (val a = part.answer) {
                        is Answer.Choice -> { a.options.forEach { add(w, it) }; add(w, a.why); a.wrong.values.forEach { add(w, it) }; answers.add(a.correct) }
                        is Answer.Numbers -> answers.add(a.expected)
                        is Answer.Vectors -> { add(w, a.check(a.example).head); answers.add(a.example) }
                        is Answer.Paper -> { (a.sample + a.look + a.miss).forEach { add(w, it) }; answers.add(1) }
                        is Answer.Pick -> {
                            val sets = (0 until (1 shl a.options.size)).map { m -> a.options.indices.filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                            val ok = sets.first(a.ok); add(w, a.why(ok)); answers.add(ok)
                        }
                    }
                }
                sol(w, ex.solution(answers))
            }
        }
        return out
    }

    @Test fun every_definition_and_theorem_has_a_short_name() {
        val missing = Courses.flatMap { c -> c.concepts.filter { it.short.isBlank() }.map { "${c.id} ${it.id}" } + c.theorems.filter { it.short.isBlank() }.map { "${c.id} ${it.id}" } }
        assertEquals("short names missing", emptyList<String>(), missing)
    }

    @Test fun no_reference_without_a_name() {
        val missing = LinkedHashSet<String>()
        for (c in Courses) {
            val ix = RefIndexes.getValue(c.id)
            for ((w, s) in texts(c)) for (h in ix.scan(s.replace('§', '$'))) {
                if (h.entry == null || h.entry.short.isBlank()) missing += "${c.id} ${h.kind} ${h.number}  ($w: ${h.word} ${h.number})"
            }
        }
        assertTrue("references without a name in the registry:\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    /**
     * Every number opens something (Teo, 17. 11.: "Tvrzení 6.3 sa nedá ťuknúť"): a reference to a numbered statement
     * the app has no card for is a dead end. Give the statement a card (its wording from the textbook) instead of a
     * name only.
     */
    @Test fun every_reference_opens_a_card() {
        val missing = LinkedHashSet<String>()
        for (c in Courses) {
            val ix = RefIndexes.getValue(c.id)
            for ((w, s) in texts(c)) for (h in ix.scan(s.replace('§', '$'))) if (h.target == null) missing += "${c.id} ${h.kind} ${h.number}  ($w)"
        }
        assertTrue("${missing.size} references open nothing:\n" + missing.joinToString("\n"), missing.isEmpty())
    }

    @Test fun no_list_of_numbers_after_one_slovak_word() {
        // "Vety 2.39 a 2.42" would give 2.42 the name of 2.39: each number gets its own word ("Veta 2.39 a Veta 2.42")
        val list = Regex("""(?<![\p{L}])(Vet|Definíci|Pozorovan|Dôsled|Tvrden|Lem)\p{L}*\s+\d+\.\d+(\s*,\s*|\s+a\s+|\s+až\s+)\d+\.\d+""")
        val bad = Courses.flatMap { c -> texts(c).filter { (_, s) -> !s.startsWith("Skriptá") && list.containsMatchIn(s) }.map { "${c.id} ${it.first}: ${list.find(it.second)!!.value}" } }.distinct()
        assertTrue("lists of numbers:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test fun no_abbreviated_reference_in_slovak_text() {
        // "Def. 2.7" has no case, so its name could not be declined with it: write the word out ("Definície 2.7")
        val bad = Courses.flatMap { c -> texts(c).filter { (_, s) -> Regex("""Def\.\s+\d+\.\d+""").containsMatchIn(s) }.map { "${c.id} ${it.first}: ${it.second.take(80)}" } }.distinct()
        assertTrue("abbreviated references:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test fun rendering() {
        val k = Concept("x", "Lineárna závislosť", "Skriptá, Definice 2.23", emptyList(), emptyList(), emptyList(), emptyList(), ask = "", short = "LZ")
        val t = Theorem("t", "LZ a obal", "Skriptá, Věta 2.39", Wording(emptyList(), emptyList()), "", Proven.Omitted("", ""), short = "o LZ a lineárnej kombinácii")
        val ix = RefIndex.of(listOf(k), listOf(t))
        fun r(s: String, label: Boolean = false) = ix.scan(s).fold(s) { acc, _ -> acc }.let { src ->
            val hits = ix.scan(src); val sb = StringBuilder(); var p = 0
            for (h in hits) { sb.append(src, p, h.range.first); sb.append(refText(h, src, label)); p = h.range.last + 1 }
            sb.append(src.substring(p)).toString()
        }
        assertEquals("Kurz chce definíciu LZ (Def. 2.23).", r("Kurz chce Definíciu 2.23."))
        assertEquals("To je veta o LZ a lineárnej kombinácii (Veta 2.39).", r("To je Veta 2.39."))
        assertEquals("Veta o LZ a lineárnej kombinácii (Veta 2.39) hovorí", r("Veta 2.39 hovorí"))
        assertEquals("text (veta o LZ a lineárnej kombinácii, 2.39)", r("text (Veta 2.39)"))
        assertEquals("Skriptá, veta o LZ a lineárnej kombinácii, 2.39", r("Skriptá, Věta 2.39", label = true))
        // a name the text already gives is replaced, not repeated
        assertEquals("Skriptá, definícia LZ, 2.23", r("Skriptá, Definícia 2.23 (lineárna závislosť)", label = true))
        assertEquals("podľa definície LZ (Def. 2.23).", r("podľa Definície 2.23 (lineárna závislosť)."))
        // Czech "Definicí" is Czech
        assertEquals("Otázka za Definicí 2.23", r("Otázka za Definicí 2.23"))
        // a label keeps the case of a word inside it, and parentheses are never nested
        assertEquals("Otázka pri vete o LZ a lineárnej kombinácii (Veta 2.39)", r("Otázka pri Vete 2.39", label = true))
        assertEquals("(báza je LN, veta o LZ a lineárnej kombinácii, 2.39)", r("(báza je LN, Veta 2.39)"))
        assertEquals("(veta o LZ a lineárnej kombinácii, 2.39: text)", r("(Veta 2.39: text)"))
        // a Czech quote keeps its words
        assertEquals("podle Věty 2.39 platí", r("podle Věty 2.39 platí"))
    }
}

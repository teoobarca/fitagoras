package dev.hamper.tabula.data

import dev.hamper.tabula.math.segments
import dev.hamper.tabula.math.Seg
import dev.hamper.tabula.math.tapSymbols
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reading aids keep their promises (docs/CONTENT.md, "Reading markup"): the glossary only links definitions that
 * exist, every proof names its goals, every definition asks its own question, and the markup stays sparse.
 */
class ReadingTest {
    private fun lineTex(l: Line) = when (l) { is Para -> l.tex; is Item -> l.tex; is Block -> "" }
    private fun math(s: String) = s.replace('§', '$').replace("$$", "$").split('$').filterIndexed { i, _ -> i % 2 == 1 }

    @Test fun glossary_links_only_definitions_of_its_course() {
        for (c in Courses) {
            val g = Glossaries[c.id]
            assertTrue("${c.id} has a glossary", !g.isNullOrEmpty())
            assertEquals("${c.id}: glossary ids unique", g!!.size, g.map { it.id }.toSet().size)
            for (s in g) {
                assertTrue("${c.id}/${s.id}: definition ${s.concept} is not a concept of the course", c.concepts.any { it.id == s.concept })
                assertTrue("${c.id}/${s.id}: read and say", s.read.isNotBlank() && s.say.isNotBlank())
                TemplatesTest.checkText("${c.id}/${s.id}", s.say)
                TemplatesTest.checkMath("${c.id}/${s.id}", s.tex)
                // the symbol is really in the course's reading screens, and wrapping it keeps the formula well formed
                val tex = (c.theorems.flatMap { t -> t.czech.map(::lineTex) + t.idea } + c.concepts.flatMap { k -> k.czech.map(::lineTex) + k.judge.map { it.tex } } +
                    c.proofs.flatMap { p -> listOf(p.task, p.given, p.end) + p.rounds.flatMap { r -> r.options.map { it.tex } + r.prompt } }).flatMap(::math)
                val hits = tex.filter { s.regex.containsMatchIn(it) }
                assertTrue("${c.id}/${s.id}: ${s.match} matches nothing in the course", hits.isNotEmpty())
                hits.forEach { t -> TemplatesTest.checkMath("${c.id}/${s.id} wrapped", tapSymbols(t, listOf(s)) { true }) }
            }
        }
    }

    @Test fun every_proof_names_a_goal_per_round() {
        for (c in Courses) for (p in c.proofs) {
            assertEquals("${p.id}: one goal per round", p.rounds.size, p.goals.size)
            p.goals.forEach { g ->
                assertTrue("${p.id}: goal blank", g.isNotBlank())
                assertTrue("${p.id}: goal '$g' is a title, short and without a period", g.length <= 48 && !g.trimEnd().endsWith("."))
                TemplatesTest.checkText(p.id, g)
            }
            p.warn?.let { TemplatesTest.checkText("${p.id} warn", it) }
        }
    }

    @Test fun every_concept_asks_its_own_question() {
        for (c in Courses) for (k in c.concepts) {
            assertTrue("${k.id}: question", k.question.isNotBlank() && k.question.trimEnd().endsWith("?"))
            assertTrue("${k.id}: not the generic question", k.question != "Platí to?")
            TemplatesTest.checkText(k.id, k.question)
        }
    }

    @Test fun markup_is_sparse() {
        fun keys(ls: List<Line>) = ls.sumOf { l -> Regex("==").findAll(lineTex(l)).count() / 2 }
        fun displays(ls: List<Line>) = ls.sumOf { l -> segments(lineTex(l).replace('§', '$')).count { it is Seg.Display } }
        for (c in Courses) {
            for (t in c.theorems) {
                assertTrue("${t.id}: at most 2 key words per card", keys(t.czech) <= 2)
                assertTrue("${t.id}: at most 2 display formulas", displays(t.czech) <= 2)
            }
            for (k in c.concepts) {
                assertTrue("${k.id}: at most 2 key words per card", keys(k.czech) <= 2)
                assertTrue("${k.id}: at most 2 display formulas", displays(k.czech) <= 2)
                for (q in k.remarks) {
                    assertTrue("${k.id}/${q.src}: at most 2 key words per card", keys(q.lines) <= 2)
                    assertTrue("${k.id}/${q.src}: at most 2 display formulas", displays(q.lines) <= 2)
                }
            }
            // a colour per named object only where a statement has two or three of them
            for (t in c.theorems) {
                val inks = t.czech.flatMap { l -> Regex("""\\c([abc])\{""").findAll(lineTex(l)).map { it.groupValues[1] }.toList() }.toSet()
                assertTrue("${t.id}: colours ${inks}", inks.isEmpty() || inks.size in 2..3)
            }
        }
    }

    @Test fun key_words_and_displays_only_in_quotes() {
        // explanations stay plain: the marker belongs to the Czech statements only
        for (c in Courses) for (k in c.concepts) {
            (k.why.flatMap { listOf(it.part, it.because) } + k.judge.flatMap { listOf(it.because, it.wrong) }).forEach {
                assertTrue("${k.id}: key-word markup outside the quote: $it", !it.contains("=="))
            }
        }
    }
}

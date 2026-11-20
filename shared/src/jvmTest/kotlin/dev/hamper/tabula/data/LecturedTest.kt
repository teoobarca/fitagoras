package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teo's rule (docs/CONTENT.md): lessons only for topics some parallel has already lectured. Every lesson the app
 * offers has a lecture on each of its topics by [LecturesCheckedAt]; what is built but not lectured yet is in
 * [NotYetLectured] and nothing offered leads to it.
 */
class LecturedTest {
    /**
     * The app as it is now and, so that the leaving out stays checked when nothing is pending, the DML course with its
     * last lessons pending: the hiding is the same code whatever [NotYetLectured] holds.
     */
    private val samplePending = setOf("dml-tc-mocniny", "dml-tc-cvoz")
    private fun cases(): List<Shown> = Courses.map { it.shown() } + Shown(Dml, samplePending)

    @Test fun every_offered_lesson_has_a_lecture_behind_it() {
        for (c in Courses) for (l in c.shown().lessons) for (code in l.topics) {
            val first = c.firstLectureEnd(code)
            assertTrue("${l.id}: no lecture on $code by $LecturesCheckedAt (first $first): add it to NotYetLectured", first != null && first <= LecturesCheckedAt)
        }
        val all = Courses.flatMap { it.lessons }.map { it.id }.toSet()
        for (id in NotYetLectured) assertTrue("NotYetLectured names a lesson that does not exist: $id", id in all)
    }

    @Test fun nothing_offered_leads_to_a_left_out_lesson() {
        for (sh in cases()) {
            val c = sh.course
            val open = sh.lessons.flatMap(::lessonRefs).toSet()
            val all = c.lessons.flatMap(::lessonRefs).toSet()
            val routes = sh.concepts.map { "concept:${it.id}" } + sh.theorems.map { "theorem:${it.id}" } + sh.proofs.map { "proof:${it.id}" } + sh.templates.map { "gen:${it.gen}" }
            for (r in routes) assertTrue("$r offered though only a left-out lesson leads to it", r in open || r !in all)
            for (cp in sh.checkpoints) assertTrue("checkpoint after ${cp.after}", sh.lessons.any { it.id == cp.after })
            for (p in c.papers.filter(sh::paperShown)) for (q in p.problems) for (f in q.checklist.mapNotNull { it.fix }) assertTrue("${p.id} offered with $f left out", sh.shows(f))
            for (r in sh.planPapers) assertTrue(r, r.split(':').let { (_, id, n) -> sh.problemShown(id, n.toInt()) })
        }
    }

    @Test fun past_papers_come_without_the_pending_problem() {
        for (sh in listOf(Dml.shown(), Shown(Dml, samplePending))) for (full in Dml.papers + (1..10).map { Dml.generatedPaper(it) }) {
            val p = sh.offered(full)
            assertTrue("${full.id} offered", p != null)
            assertTrue("${full.id}: nothing pending on it", p!!.problems.all(sh::problemShown))
            assertEquals("${full.id}: every problem is offered or named missing", full.problems.map { it.n }.sorted(), (p.problems + p.missing).map { it.n }.sorted())
            assertTrue("${full.id}: induction and sets stay", p.problems.size >= 2)
            if (sh === Dml.shown()) assertEquals(p.problems.map { it.n }, dev.hamper.tabula.screens.paperById(full.id).problems.map { it.n })
        }
        assertTrue("a pending lesson leaves its problem out", Shown(Dml, samplePending).offered(Dml.papers.first { it.id == "dml-2025-k" })!!.missing.isNotEmpty())
        // the minutes follow the points: the whole paper gets the exam's minutes, a paper without its pending problem less
        val k = Dml.papers.first { it.id == "dml-2025-k" }
        val offeredPts = Dml.shown().offered(k)!!.problems.sumOf { it.pts }
        val minutes = dev.hamper.tabula.screens.paperMinutes("dml-2025-k")
        assertTrue("the minutes follow the points: $minutes for $offeredPts", kotlin.math.abs(minutes * 30 - 75 * offeredPts) <= 30)
    }

    @Test fun review_and_links_never_offer_a_pending_item() {
        val sh = Shown(Dml, samplePending)
        val pendingGens = Dml.lessons.filter { it.id in samplePending }.flatMap(::lessonRefs).filter { it.startsWith("gen:") && it !in sh.lessons.flatMap(::lessonRefs) }
        assertTrue("some generators belong only to pending lessons", pendingGens.isNotEmpty())
        for (g in pendingGens) assertTrue(g, !sh.offersKey(g))
        val pendingStep = Dml.lessons.first { it.id in samplePending }.steps.first().id
        assertTrue(!sh.offersKey("step:$pendingStep"))
        assertTrue(sh.offersKey("step:" + sh.lessons.first().steps.first().id))
        // the LA1 exam plan does not deal 2nd-test papers once the exam is the target
        assertTrue(La1.planPapers.isNotEmpty())
    }

    @Test fun what_is_offered_on_15_november() {
        val dml = Dml.shown().lessons.map { it.id }
        assertTrue(dml.containsAll(listOf("dml-tc-delitelnost", "dml-tc-rea", "dml-tc-diofant")))
        assertTrue(dml.none { it in NotYetLectured })
        assertTrue("all of induction and sets are lectured", Dml.lessons.filter { it.id.startsWith("dml-mi") || it.id.startsWith("dml-mz") }.all { it.id in dml })
        // LA1: kapitola 3 and 4 were lectured by 10. 11. (Kleprlík: Frobenius and varieties, captions 10. to 96. min)
        assertEquals(La1.lessons.map { it.id }, La1.shown().lessons.map { it.id })
    }
}

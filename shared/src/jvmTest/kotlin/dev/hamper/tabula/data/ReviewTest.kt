package dev.hamper.tabula.data

import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.of
import dev.hamper.tabula.compat.time.plusDays
import dev.hamper.tabula.state.Attempt
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.Store
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Opakovanie na dnes (data/Review.kt): what comes back today, why, and when today is done. */
class ReviewTest {
    // moments in hours since the start of day 0; day 10 is today
    private val base = LocalDate.of(2026, 11, 9)
    private val today = base.plusDays(10)
    private fun at(day: Int, hour: Int = 10) = (day * 24L + hour) * 3600_000L
    private val dayOf: (Long) -> LocalDate? = { ms -> base.plusDays(ms / (24 * 3600_000L)) }
    private fun wrong(k: String, day: Int) = Mistake(k, k, "", k, at(day))

    private fun dose(
        due: List<String>, mistakes: List<Mistake> = emptyList(), attempts: List<Attempt> = emptyList(),
        unwritten: List<String> = emptyList(), vouched: List<String> = emptyList(), reviewed: List<String> = emptyList(),
    ) = reviewDose(due, mistakes, attempts, unwritten, vouched, reviewed, today, dayOf)

    @Test fun atMostFiveMistakesFirstNewestFirst() {
        val ms = listOf(wrong("gen:a", 3), wrong("gen:b", 9), wrong("gen:c", 6))
        val d = dose(listOf("gen:x", "gen:a", "gen:y", "gen:b", "gen:c", "gen:z", "gen:w"), ms)
        assertEquals(listOf("gen:b", "gen:c", "gen:a", "gen:x", "gen:y"), d.items.map { it.key })
        assertTrue(d.items.take(3).all { it.back == Back.Wrong })
        assertEquals(base.plusDays(9), d.items.first().wrongOn)
    }

    @Test fun answeredTodayWaitsForTomorrowRightOrWrong() {
        // wrong at 9:00 today: not back at 9:05; another one answered right today in a lesson: not back either
        val ms = listOf(wrong("gen:a", 10))
        val tries = listOf(Attempt("gen:a", at(10, 9), false, false), Attempt("gen:b", at(10, 8), true, false), Attempt("gen:c", at(9, 22), false, false))
        val d = dose(listOf("gen:a", "gen:b", "gen:c"), ms, tries)
        assertEquals(listOf("gen:c"), d.items.map { it.key })
        // tomorrow all three are due again
        assertEquals(3, d.tomorrow)
        // a definition missed today through one of its questions (the answer under another key): tomorrow too
        assertEquals(emptyList<String>(), dose(listOf("concept:k"), listOf(wrong("concept:k", 10)), listOf(Attempt("judge:k:0", at(10, 9), false, false))).items.map { it.key })
    }

    @Test fun oneThingOnPaperADay() {
        val due = listOf("concept:k1", "proof:p1", "gen:a", "concept:k2")
        val d = dose(due, unwritten = listOf("concept:k1", "proof:p1", "concept:k2"))
        assertEquals(1, d.items.count { onPaper(it.key) })
        assertEquals(Back.Paper, d.items.first { onPaper(it.key) }.back)
        // one written in today's round already: no other one today, and none is lost (tomorrow it comes)
        val after = dose(due, attempts = listOf(Attempt("concept:k1", at(10), true, false)), unwritten = listOf("proof:p1", "concept:k2"), reviewed = listOf("concept:k1"))
        assertEquals(listOf("gen:a"), after.items.map { it.key })
        assertTrue(after.tomorrow >= 2)
    }

    @Test fun eachThingSaysWhyItIsBack() {
        val due = listOf("gen:w", "gen:h", "gen:v", "gen:n", "concept:u")
        val d = dose(
            due, listOf(wrong("gen:w", 9)), listOf(Attempt("gen:h", at(8), true, true), Attempt("gen:n", at(7), true, false)),
            unwritten = listOf("concept:u"), vouched = listOf("gen:v"),
        )
        assertEquals(mapOf("gen:w" to Back.Wrong, "gen:h" to Back.Aided, "gen:v" to Back.Vouched, "gen:n" to Back.NotTwice, "concept:u" to Back.Paper), d.items.associate { it.key to it.back })
    }

    @Test fun theDoseEndsForTodayAndSaysWhatComesTomorrow() {
        val due = (1..8).map { "gen:$it" }
        val first = dose(due)
        assertEquals(5, first.items.size)
        assertFalse(first.finished)
        // the five done in today's round (answered today): today is done, the queue is still there for tomorrow
        val five = first.items.map { it.key }
        val d = dose(due, attempts = five.map { Attempt(it, at(10, 11), true, false) }, reviewed = five)
        assertTrue(d.finished)
        assertEquals(0, d.items.size)
        assertEquals(5, d.done)
        assertEquals(5, d.tomorrow)
        // two done so far: three left, never more than five a day
        val two = dose(due, attempts = five.take(2).map { Attempt(it, at(10, 11), true, false) }, reviewed = five.take(2))
        assertEquals(3, two.items.size)
    }

    @Test fun recordsWithoutDatesLoseNothing() {
        // an older install: a mistake and answers with no time (at == 0)
        val d = dose(listOf("gen:old", "gen:new", "gen:x"), listOf(Mistake("gen:old", "t", "", "gen:old", 0L), wrong("gen:new", 8)), listOf(Attempt("gen:x", 0L, false, false), Attempt("gen:old", 0L, false, false)))
        assertEquals(listOf("gen:new", "gen:old", "gen:x"), d.items.map { it.key })
        assertNull(d.items.first { it.key == "gen:old" }.wrongOn)
        // a day function that fails on a strange moment hides nothing either
        val broken = reviewDose(listOf("gen:a"), listOf(wrong("gen:a", 2)), listOf(Attempt("gen:a", at(2), false, false)), emptyList(), emptyList(), emptyList(), today) { error("no zone") }
        assertEquals(listOf("gen:a"), broken.items.map { it.key })
    }

    /** A record written by an older build (no today's round, a mistake without a time) loads, and the dose shows it. */
    @Test fun anOlderInstallLoadsAndItsMistakeIsInTheDose() {
        val tmpl = TemplateOf.getValue(Dml.shown().lessons.first { l -> l.steps.any { it is Step.Practice } }.steps.filterIsInstance<Step.Practice>().first().gen)
        val k = "gen:$tmpl"
        val lesson = Dml.shown().lessons.first { l -> l.steps.any { it is Step.Practice && TemplateOf[it.gen] == tmpl } }
        val old = """{"v":2,"course":"dml","lessonsDone":["${lesson.id}"],"attempts":[{"k":"$k","t":0,"ok":false,"aided":false}],"mistakes":[{"k":"$k","title":"Príklad","detail":"","route":"$k","t":0}]}"""
        try {
            val s = Store(MemoryStateFile(old))
            val d = reviewToday(s, today)
            assertEquals(k, d.items.first().key)
            assertEquals(Back.Wrong, d.items.first().back)
            // finishing it in today's round is kept across a restart, and nothing of the record is rewritten
            s.reviewedNow(k)
            assertEquals(1, s.mistakes.size)
            assertEquals(0L, s.attempts.single().at)
        } finally { Sittings.picked = { null } } // the store sets it process-wide
    }
}

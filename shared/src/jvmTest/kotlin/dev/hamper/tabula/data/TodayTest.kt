package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Test

/** What Dnes puts first and lists as unfinished (data/Today.kt): the situations the rule was written for. */
class TodayTest {
    private val day = 24L * 3600 * 1000
    private val now = 100 * day
    private fun l(id: String, done: Boolean = false, begun: Boolean = false, touched: Long? = null, gaps: Boolean = false, forTest: Boolean = false) =
        LessonState(id, done, begun, touched, 5, gaps, forTest)

    @Test fun theLessonWorkedOnLastLeads() {
        // Teo, 18. 11.: A left unfinished, then B begun → B leads, A is a row
        val c = todayChoice(listOf(l("a", begun = true, touched = now - 3 * 3600_000), l("b", begun = true, touched = now - 600_000), l("c")), now, 10)
        assertEquals(TodayChoice("b", listOf("a")), c)
        // the same from an install without times: the later lesson in order counts as the newer one
        assertEquals(TodayChoice("b", listOf("a")), todayChoice(listOf(l("a", begun = true), l("b", begun = true), l("c")), now, 10))
    }

    @Test fun aFinishedLastLessonGivesTheNextNotBegun() {
        val c = todayChoice(listOf(l("a", begun = true, touched = now - day), l("b", done = true, touched = now - 60_000), l("c"), l("d")), now, 10)
        assertEquals(TodayChoice("c", listOf("a")), c)
        // lessons ticked in Nastavenia without a time do not count as worked on
        assertEquals(TodayChoice("a", emptyList()), todayChoice(listOf(l("a", begun = true), l("b", done = true), l("c")), now, 10))
    }

    @Test fun atMostTwoRowsAndAWeekUntouchedLeavesDnes() {
        val c = todayChoice(listOf(
            l("a", begun = true, touched = now - 8 * day), l("b", begun = true, touched = now - 3 * day),
            l("c", begun = true, touched = now - 2 * day), l("d", begun = true, touched = now - day), l("e", begun = true, touched = now),
        ), now, 10)
        assertEquals(TodayChoice("e", listOf("d", "c")), c)
        assertEquals(TodayChoice("b", emptyList()), todayChoice(listOf(l("a", begun = true, touched = now - 8 * day), l("b", begun = true, touched = now)), now, 10))
    }

    @Test fun nearTheTestItsLessonsLead() {
        val ls = listOf(l("t1", done = true, forTest = true), l("t2", forTest = true), l("t3", forTest = true), l("x", begun = true, touched = now))
        // the day before and the day of the test: the first test lesson left; the other matter becomes a row
        assertEquals(TodayChoice("t2", listOf("x")), todayChoice(ls, now, 1))
        assertEquals(TodayChoice("t2", listOf("x")), todayChoice(ls, now, 0))
        // a test lesson begun comes before the first one left
        assertEquals(TodayChoice("t3", listOf("x")), todayChoice(ls.map { if (it.id == "t3") it.copy(begun = true, touched = now - day) else it }, now, 1))
        // a test lesson ended with jumped steps does not lead while another one is left
        assertEquals(TodayChoice("t3", listOf("t2", "x")), todayChoice(ls.map { if (it.id == "t2") it.copy(begun = true, gaps = true, touched = now + 1) else it }, now, 1))
        // further from the test the rule is the usual one
        assertEquals(TodayChoice("x", emptyList()), todayChoice(ls, now, 2))
        // all the test's lessons done: the usual rule
        assertEquals(TodayChoice("x", emptyList()), todayChoice(ls.map { if (it.forTest) it.copy(done = true) else it }, now, 1))
    }

    @Test fun aLessonEndedWithJumpedStepsIsARow() {
        val c = todayChoice(listOf(l("a", begun = true, gaps = true, touched = now), l("b")), now, 10)
        assertEquals(TodayChoice("b", listOf("a")), c)
        // the hero only when nothing else is left
        assertEquals(TodayChoice("a", emptyList()), todayChoice(listOf(l("a", begun = true, gaps = true, touched = now), l("b", done = true)), now, 10))
    }
}

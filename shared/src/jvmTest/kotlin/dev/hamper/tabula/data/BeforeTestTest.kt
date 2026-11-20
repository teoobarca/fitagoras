package dev.hamper.tabula.data

import dev.hamper.tabula.screens.anyLesson
import dev.hamper.tabula.screens.quickLesson
import dev.hamper.tabula.screens.testProblems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The window before the test (data/BeforeTest.kt): what the tab Test and Dnes put first, and the quick ways themselves. */
class BeforeTestTest {
    // the pages about the tests are files (files/test/<exam>.json)
    init { dev.hamper.tabula.platform.installJvmAssets() }
    private fun p(n: Int, missing: Boolean = true, share: Double = .5, done: Boolean = false, quick: Boolean = true) = ProblemGap(n, missing, share, done, quick)
    private val three = listOf(p(1, share = .4), p(2, share = .9), p(3, share = .6))

    @Test fun theWindowIsTwoDaysAndTheTestDay() {
        // three days before: the usual order (lessons first), as before the window existed
        assertNull(beforeTest(3, false, three))
        assertEquals(BeforeTest.Quick(2), beforeTest(2, false, three))
        assertEquals(BeforeTest.Quick(2), beforeTest(0, false, three))
        assertNull(beforeTest(-1, false, three))
    }

    @Test fun theMostFrequentProblemWithMissingLessonsFirst() {
        // problem 2 has its lessons: not offered however frequent it is
        assertEquals(BeforeTest.Quick(3), beforeTest(1, false, listOf(p(1, share = .4), p(2, missing = false, share = .9), p(3, share = .6))))
        // a tie goes to the lower number; a problem with no type a lesson practises has no quick way
        assertEquals(BeforeTest.Quick(1), beforeTest(1, false, listOf(p(1), p(2), p(3, share = .9, quick = false))))
    }

    @Test fun aQuickWayDoneMovesOnThenTheWholeTest() {
        assertEquals(BeforeTest.Quick(3), beforeTest(1, false, listOf(p(1, share = .4), p(2, share = .9, done = true), p(3, share = .6))))
        val allDone = listOf(p(1, done = true), p(2, done = true), p(3, missing = false))
        assertEquals(BeforeTest.Whole, beforeTest(1, false, allDone))
        // nothing missing at all: the whole test, as before
        assertEquals(BeforeTest.Whole, beforeTest(2, false, three.map { it.copy(missing = false) }))
    }

    @Test fun aWholeTestWrittenRecently() {
        // what it showed missing still gets its quick way; with nothing missing the usual order decides (no second whole test)
        assertEquals(BeforeTest.Quick(2), beforeTest(1, true, three))
        assertNull(beforeTest(1, true, three.map { it.copy(missing = false) }))
    }

    @Test fun quickIdsReadBack() {
        assertEquals("la1-test2" to 2, quickOf(quickId("la1-test2", 2)))
        assertNull(quickOf("la1-k3-hodnost"))
    }

    /**
     * Every quick way of both courses' tests: a worked example of its type first when the lesson has one, then that
     * type's exercises (the last one without help when the lesson has one), one piece with "krok N z M", and its id
     * opens it again (a link, a restart of the app).
     */
    @Test fun everyQuickWayIsAWorkedExampleAndItsType() {
        var n = 0
        for (course in Courses) for (p in testProblems(course)) {
            val q = quickLesson(course, p) ?: continue
            n++
            val practice = q.steps.filterIsInstance<Step.Practice>()
            assertTrue("${q.id}: exercises of the type", practice.isNotEmpty() && practice.map { TemplateOf[it.gen] ?: it.gen }.distinct().size == 1)
            assertTrue("${q.id}: the worked example first", q.steps.count { it is Step.Worked } <= 1 && q.steps.drop(1).none { it is Step.Worked })
            assertTrue("${q.id}: help before none", practice.size < 2 || (!practice[0].solo && practice[1].solo))
            assertEquals(q.id, listOf(0 until q.steps.size), pieces(q))
            assertEquals(q.id, q.steps.map { it.id }, anyLesson(q.id)?.steps?.map { it.id })
        }
        assertTrue("no quick way at all", n > 0)
    }
}

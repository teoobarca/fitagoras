package dev.hamper.tabula.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The lesson's one history (LessonRun): back is always exactly one screen, a step's own screens included; nothing
 * done is lost by going back, jumping, or saving and reading the run again (the app killed and opened again).
 */
class LessonRunTest {
    @Test fun backIsOneScreenAndNothingIsLost() {
        // a question, a definition with three screens of its own, a practice step the lesson leaves out, a check
        val run = LessonRun.start(listOf("ask", "def", "practice", "check"))
        run.put("ask", "pick", "1")
        assertTrue(run.next())
        run.open(100); run.open(200); run.open(201)               // why, first example, second example
        run.put("def", "judge0", "1")
        assertTrue(run.next { it == 2 })                           // the mastered practice step is passed over
        assertEquals(3, run.at)

        // back: one screen each time, through the definition's own screens, never over two at once
        val seen = mutableListOf<Pair<Int, Int>>()
        while (run.back()) seen += run.at to run.sub()
        assertEquals(listOf(1 to 201, 1 to 200, 1 to 100, 1 to 0, 0 to 0), seen)
        assertFalse("on the first screen back leaves the lesson", run.back())

        // the answers are still there; forward again finds each step as it was left
        assertEquals("1", run.get("ask", "pick"))
        assertEquals("1", run.get("def", "judge0"))
        assertTrue(run.next()); assertEquals(1 to 0, run.at to run.sub())

        // on from there: the left-out practice stays out
        assertTrue(run.next()); assertEquals(3, run.at)

        // the contents: anywhere, a left-out step too
        assertTrue(run.jump(1)); assertTrue(run.jump(2)); assertTrue(run.jump(3)); assertEquals(3, run.at)

        // a new version with a step inserted before: the saved place is the same step, found by its id
        val moved = LessonRun.parse(listOf("intro") + run.ids, run.json())
        assertEquals("check", moved.id)

        // saved and read again (the app killed): the same place, the same history, the same answers
        val again = LessonRun.parse(run.ids, run.json())
        assertEquals(3 to 3, again.at to again.furthest)
        assertTrue(again.back()); assertEquals(1, again.at)        // over the left-out step, back to the definition
        assertEquals("1", again.get("def", "judge0"))
        assertEquals(listOf(0), again.trail(1))                    // its screens were gone back over before the save
    }

    @Test fun onlyAStepGoneOnFromIsDone() {
        val ids = listOf("a", "b", "c", "d", "e")
        val run = LessonRun.start(ids)
        run.next()                                                 // a done, b looked at
        run.jump(3)                                                // d looked at, c never
        run.jump(4); assertFalse(run.next())                       // e done, b, c and d not
        assertEquals(listOf(1, 2, 3), run.left())
        assertEquals(run.left(), LessonRun.parse(ids, run.json()).left())

        // at the end, a step jumped over is left out by the same rule as when walked past
        run.jump(4); assertFalse(run.next { it == 2 }); assertEquals(listOf(1, 3), run.left())

        // a run saved by a version without "done": what it counted stays, the step it stands on is still to do
        val old = LessonRun.start(ids).json().apply { remove("done"); put("seen", dev.hamper.tabula.compat.json.JSONArray(listOf("a", "b", "c"))); put("at", 2); put("atId", "c"); put("furthest", 2); put("furthestId", "c") }
        assertEquals(listOf(2, 3, 4), LessonRun.parse(ids, old).left())
    }
}

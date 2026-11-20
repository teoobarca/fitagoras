package dev.hamper.tabula.system

import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.data.LessonById
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** What the statistics send for what the student does, and what survives an update or a switch off (Tracker). */
class TrackerTest {
    @get:Rule val tmp = TemporaryFolder()
    private var clock = 1_763_400_000_000L
    private val prefs = MapPrefs()
    private fun tracker() = Tracker(tmp.root, prefs, now = { clock }, today = { LocalDate.parse("2026-11-17") })

    @Before fun env() { Env.install(debug = true, clipsGuestToken = "", versionName = "test") }

    private val rea = LessonById.getValue("dml-tc-rea")

    @Test fun aLessonStepByStep() {
        val t = tracker()
        t.opened("dml", "2")
        t.lessonStarted(rea.id)
        t.stepShown(rea.id, 3)
        clock += 12_000
        t.stepLeft()
        t.stepShown(rea.id, 4)
        t.help("hint", "step:${rea.steps[4].id}")
        t.given("gen:dml-tc-rea", "7")
        clock += 5_000
        t.answer("gen:dml-tc-rea", ok = false, aided = true, attempt = 1, courseId = "dml", fallbackText = null)
        t.answer("gen:dml-tc-rea", ok = true, aided = false, attempt = 2, courseId = "dml", fallbackText = null)
        t.skipped("known")
        t.stepLeft()
        t.stepShown(rea.id, 5)
        clock += 60L * 60 * 1000 // the phone lay on the table: no time for this step
        t.lessonLeft()
        val e = t.events.all()
        assertEquals(listOf("open", "lesson_started", "step", "help", "answer", "answer", "step", "lesson_left"), e.map { it.type })
        assertEquals("BI-DML.21", e[0].course); assertEquals("2", e[0].parallel)
        assertEquals(rea.steps.size, e[1].of)
        with(e[2]) { assertEquals(3, pos); assertEquals("done", how); assertEquals(12_000L, ms); assertEquals(rea.steps[3].id, step); assertEquals(Tracker.kindOf(rea.steps[3]), kind) }
        with(e[3]) { assertEquals("hint", kind); assertEquals(4, pos); assertEquals(rea.id, lesson) }
        with(e[4]) { assertEquals("7", given); assertEquals(1, attempt); assertEquals(4, pos); assertEquals(5_000L, ms) }
        assertNull("a right answer is never sent as entered", e[5].given)
        assertEquals("known", e[6].how)
        with(e[7]) { assertEquals(5, pos); assertNull(ms) }
        assertTrue(e.all { it.at != null && it.day == "2026-11-17" })
    }

    @Test fun aSittingIsOneOpenUntilHalfAnHourAway() {
        val t = tracker()
        t.opened("dml", null); clock += 10 * 60_000; t.opened("dml", null)   // back after 10 min: the same sitting
        t.opened("dml", "3")                                                  // another parallel: says so
        clock += 31 * 60_000; t.opened("dml", "3")                            // after 31 min: a new sitting
        assertEquals(3, t.events.all().count { it.type == "open" })
    }

    @Test fun anUpdateKeepsTheInstallAndSaysFromWhich() {
        val before = tracker()
        before.started("1.3.14", "dml")
        val id = before.installId()
        before.opened("dml", null)
        // the new version starts: the same app data, so the same id; it says it came from 1.3.14
        val after = tracker()
        after.started("1.4.0", "dml")
        assertEquals(id, after.installId())
        val upd = after.events.all().last()
        assertEquals("update", upd.type); assertEquals("installed", upd.kind); assertEquals("1.3.14", upd.version)
        after.started("1.4.0", "dml")
        assertEquals("once", 1, after.events.all().count { it.type == "update" })
    }

    @Test fun switchedOffNothingGoesAndOnAgainIsANewHistory() {
        val t = tracker()
        t.opened("dml", null)
        val id = t.installId()
        t.switch(false)
        t.opened("dml", null)
        assertTrue(t.events.all().isEmpty())
        t.switch(true)
        assertNotEquals(id, t.installId())
        val sent = mutableListOf<String>()
        t.opened("dml", null)
        assertTrue(t.send("1.4.0", false, deviceHash("phone"), Context(model = "x")) { path, body -> sent += body; assertTrue(path.startsWith("/api/telemetry/")); 204 })
        val (forget, events) = sent
        assertEquals("the server is told to drop the phone of the old id", id, JSONObject(forget).getString("install"))
        assertEquals(deviceHash("phone"), JSONObject(forget).getString("device"))
        assertFalse("the phone's hash would tie it to the history switched off", JSONObject(events).has("device"))
        sent.clear()
        assertTrue(t.send("1.4.0", false, deviceHash("phone"), null) { _, body -> sent += body; 204 })
        assertTrue("forgotten once", sent.isEmpty())
    }

    /** Nastavenia promise: switched off, nothing goes on its own (no event, no update, no crash), only what the student
     *  sends himself (a report, under a one-off id), plus the one request that tells the server to forget the old id. */
    @Test fun switchedOffOnlyTheStudentsOwnReportGoes() {
        val t = tracker()
        t.opened("dml", null)
        val id = t.installId()
        t.switch(false)
        prefs.put("version", "0.9")
        t.started("test", "dml")
        t.opened("dml", null)
        t.lessonStarted(rea.id)
        t.stepShown(rea.id, 0)
        t.answer(rea.steps[0].id, false, false, 1, "dml", "x")
        t.help("hint", rea.steps[0].id)
        t.update("offered", "9.9.9")
        t.crashed("java.lang.IllegalStateException", "boom", "at dev.hamper.tabula.X", "dml")
        t.report(rea.steps[0].id, "Zlý príklad", "dml")
        val sent = mutableListOf<Pair<String, String>>()
        assertTrue(t.send("1.6.2", false, deviceHash("phone"), Context(model = "x")) { path, body -> sent += path to body; 204 })
        assertEquals(listOf("/api/telemetry/forget", "/api/telemetry/reports"), sent.map { it.first })
        val report = JSONObject(sent[1].second)
        assertNotEquals("a report sent with statistics off is not tied to the old history", id, report.getString("install"))
        assertTrue(t.events.all().isEmpty())
    }

    @Test fun sendingKeepsWhatTheServerDidNotTake() {
        val t = tracker()
        t.opened("dml", null)
        assertFalse(t.send("1.4.0", false, null, null) { _, _ -> null })   // offline
        assertEquals(1, t.events.all().size)
        assertTrue(t.send("1.4.0", false, null, null) { _, _ -> 400 })     // refused for good: dropped
        assertTrue(t.events.all().isEmpty())
    }

    @Test fun aCrashIsWrittenAndSentOnce() {
        Tracker.writeCrash(tmp.root, IllegalStateException("step 3\nsecond line", IllegalArgumentException("typed: 42 / Novák")))
        val (kind, message, stack) = Tracker.readCrash(tmp.root)!!
        assertEquals("java.lang.IllegalStateException", kind)
        assertEquals("step 3 second line", message)
        assertTrue(stack.contains("at dev.hamper.tabula"))
        assertTrue(stack.contains("Caused by: java.lang.IllegalArgumentException") && "Novák" !in stack)
        assertNull(Tracker.readCrash(tmp.root))
        val t = tracker()
        t.opened("dml", null)
        t.crashed(kind, message, stack, "dml")
        assertEquals("java.lang.IllegalStateException", t.events.all().last().kind)
    }
}

package dev.hamper.tabula.system

import dev.hamper.tabula.compat.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import dev.hamper.tabula.compat.time.LocalDate
import java.util.UUID
import dev.hamper.tabula.compat.time.*

class TelemetryCoreTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun ev(n: Int, day: String = "2026-11-15") = Event(UUID.randomUUID().toString(), day, "answer", "BI-DML.21", lesson = "dml-mi-slaba", item = "step:q$n", ok = n % 2 == 0, aided = false)
    private fun queue(file: File = File(tmp.root, "q.jsonl"), cap: Int = 10) = JsonQueue(file, cap, Event::json, Event::from, Event::id)

    @Test fun coursesMapToTheServersCodes() {
        assertEquals("BI-LA1.21", serverCourse("la1"))
        assertEquals("BI-DML.21", serverCourse("dml"))
        assertNull(serverCourse("pa1"))
    }

    @Test fun eventRoundTripsThroughJson() {
        val e = Event("id1", "2026-11-15", "mock_test", "BI-LA1.21", test = "la1-2025-k", score = 14, max = 20)
        assertEquals(e, Event.from(JSONObject(e.json().toString())))
        val a = ev(2)
        assertEquals(a, Event.from(a.json()))
        val o = Event("id2", "2026-11-15", "open", "BI-DML.21", parallel = "3")
        assertEquals(o, Event.from(JSONObject(o.json().toString())))
        val c = Event("id3", "2026-11-15", "clip_played", "BI-DML.21", clip = "dml-c1", watched = 0.42, text = "Čo je zjednotenie?", source = "P2")
        assertEquals(c, Event.from(JSONObject(c.json().toString())))
    }

    @Test fun aReportKeepsOnThePhoneThatStatisticsWereOffButDoesNotSendIt() {
        val r = ErrorReport("r1", 1L, "BI-DML.21", "zle", anon = true)
        assertEquals(r, ErrorReport.from(JSONObject(r.stored().toString())))
        assertEquals(false, r.json().has("anon"))
        assertEquals(false, ErrorReport.from(JSONObject(ErrorReport("r2", 1L, "BI-DML.21", "x").json().toString())).anon)
    }

    @Test fun clipShareStaysWithinZeroAndOne() {
        assertEquals(1.0, Event("i", "2026-11-15", "clip_played", "BI-DML.21", clip = "c", watched = 1.3).json().getDouble("watched"), 0.0)
    }

    @Test fun parallelsTheServerWouldRefuseAreLeftOut() {
        assertEquals("2", safeParallel("2"))
        assertEquals("101a", safeParallel("101a"))
        assertNull(safeParallel("2; drop"))
        assertNull(safeParallel(""))
    }

    @Test fun anOlderEventCarriesOnlyWhatItHad() {
        val keys = ev(1).json().keys().asSequence().toSet()
        assertEquals(setOf("id", "day", "type", "course", "lesson", "item", "ok", "aided"), keys)
    }

    @Test fun aQueueLineFromAnOlderVersionStillReads() {
        // what 1.3.x left in events.jsonl must go out after the update, not be lost or block the queue
        val old = """{"id":"7c0c1c55-6f53-4a56-9e8c-3c3b1b0e7f11","day":"2026-11-15","type":"answer","course":"BI-DML.21","lesson":"dml-mi-slaba","item":"step:q1","ok":true,"aided":false}"""
        val e = Event.from(JSONObject(old))
        assertEquals("step:q1", e.item); assertNull(e.at); assertNull(e.attempt)
        assertEquals(JSONObject(old).keys().asSequence().toSet(), e.json().keys().asSequence().toSet())
    }

    @Test fun newFieldsRoundTripAndAreCapped() {
        val e = Event("i", "2026-11-17", "answer", "BI-DML.21", item = "gen:x", ok = false, at = 1_763_400_000_000, step = "dml-1-3", pos = 3, of = 9,
            kind = "Practice", ms = 7 * 3600 * 1000L, given = "x".repeat(500), attempt = 2, seed = "-81")
        val back = Event.from(JSONObject(e.json().toString()))
        assertEquals(MaxLessonMs, back.ms)
        assertEquals(MaxGiven, back.given!!.length)
        assertEquals(e.copy(ms = MaxLessonMs, given = "x".repeat(MaxGiven)), back)
    }

    @Test fun theEnvelopeCarriesThePhoneOnlyWhenGiven() {
        val plain = JSONObject(eventsBody("inst", "1.4.0", listOf(ev(1))))
        assertFalse(plain.has("device") || plain.has("ctx"))
        val full = JSONObject(eventsBody("inst", "1.4.0", listOf(ev(1)), device = deviceHash("abc"), ctx = Context(model = "Pixel 8", sdk = 35, font = 1.1500001f, dark = true)))
        assertEquals(64, full.getString("device").length)
        assertEquals(setOf("model", "sdk", "font", "dark"), full.getJSONObject("ctx").keys().asSequence().toSet())
        assertEquals(1.15, full.getJSONObject("ctx").getDouble("font"), 0.001)
        assertTrue(deviceHash("abc") != deviceHash("abd") && deviceHash("abc") == deviceHash("abc"))
    }

    @Test fun longItemTextIsCut() {
        val e = ev(1).copy(text = "x".repeat(5000))
        assertEquals(MaxItemText, e.json().getString("text").length)
    }

    @Test fun queueSurvivesAReopenAndKeepsTheNewest() {
        val f = File(tmp.root, "events.jsonl")
        val q = queue(f, cap = 3)
        val all = (1..5).map { ev(it) }
        all.forEach { q.add(it) }
        assertEquals(all.takeLast(3), queue(f, cap = 3).all())
    }

    @Test fun removeTakesOnlyWhatWasSent() {
        val q = queue(cap = 4)
        (1..3).forEach { q.add(ev(it)) }
        val batch = q.all()
        val later = (7..9).map { ev(it) }
        later.forEach { q.add(it) } // logged while the batch was on its way; the cap trimmed two sent ones meanwhile
        q.remove(batch)
        q.remove(batch) // a second sender with the same batch removes nothing more
        assertEquals(later, q.all())
    }

    @Test fun aBatchStaysUnderTheServersSizeLimit() {
        val long = (1..300).map { ev(it).copy(text = "§\\frac{a}{b}§ ".repeat(40)) }
        val b = batch(long)
        assertTrue(b.isNotEmpty() && b.size < BatchSize)
        assertTrue(eventsBody(UUID.randomUUID().toString(), "1.3.0", b).toByteArray().size < 64 * 1024)
        assertEquals(BatchSize, batch((1..300).map { ev(it) }).size)
        assertEquals(1, batch(listOf(ev(1).copy(text = "x".repeat(MaxItemText)))).size)
    }

    @Test fun idsTheServerWouldRefuseAreLeftOut() {
        assertEquals("step:dml-tc-phi-4", safeId("step:dml-tc-phi-4"))
        assertEquals("3675636329@sirius.fit.cvut.cz", safeId("3675636329@sirius.fit.cvut.cz"))
        assertNull(safeId("step:a b"))
        assertNull(safeId("x".repeat(101)))
        assertNull(safeId(null))
    }

    @Test fun aBrokenLineIsSkippedNotFatal() {
        val f = File(tmp.root, "q.jsonl")
        val good = ev(1)
        f.writeText("{not json\n" + good.json() + "\n")
        assertEquals(listOf(good), queue(f).all())
    }

    @Test fun clearEmptiesTheQueue() {
        val q = queue()
        q.add(ev(1)); q.clear()
        assertTrue(q.all().isEmpty())
    }

    @Test fun serverAnswers() {
        assertEquals(Outcome.Sent, outcome(204))
        assertEquals(Outcome.Drop, outcome(400))
        assertEquals(Outcome.Drop, outcome(413))
        assertEquals(Outcome.Drop, outcome(401))
        assertEquals(Outcome.Retry, outcome(429))
        assertEquals(Outcome.Retry, outcome(503))
        assertEquals(Outcome.Retry, outcome(null)) // offline
    }

    @Test fun staleEventsAreNotSent() {
        val today = LocalDate.of(2026, 11, 15)
        assertTrue(fresh(ev(1, "2026-11-15"), today))
        assertTrue(fresh(ev(1, today.minusDays(MaxAgeDays).toString()), today))
        assertFalse(fresh(ev(1, today.minusDays(MaxAgeDays + 1).toString()), today))
        assertFalse(fresh(ev(1, "garbage"), today))
    }

    @Test fun bodiesMatchTheServer() {
        val install = UUID.randomUUID().toString()
        val b = JSONObject(eventsBody(install, "1.3.0", listOf(ev(1), ev(2))))
        assertEquals(install, b.getString("install"))
        assertEquals("1.3.0", b.getString("app"))
        assertEquals(2, b.getJSONArray("events").length())
        val r = ErrorReport(UUID.randomUUID().toString(), 1763200000000, "BI-DML.21", "Má byť 12.", "step:dml-tc-phi-4", "Koľko je φ(36)?", "dml-tc-phi",
            "dml-c4", "3675636329@sirius.fit.cvut.cz", 2214.0, "P7")
        val rb = JSONObject(reportBody(install, "1.3.0", r))
        assertEquals(install, rb.getString("install"))
        assertEquals("3675636329@sirius.fit.cvut.cz", rb.getString("recording"))
        assertEquals(2214.0, rb.getDouble("t"), 0.0)
        assertEquals(r, ErrorReport.from(JSONObject(r.json().toString())))
    }

    @Test fun debug_builds_mark_what_they_send_as_test() {
        val r = ErrorReport("r3", 1L, "BI-LA1.21", "x", item = "step:tn-0")
        assertTrue(JSONObject(reportBody("i", "1.3.0", r, test = true)).getBoolean("test"))
        assertTrue(!JSONObject(reportBody("i", "1.3.0", r)).has("test"))
        assertTrue(JSONObject(eventsBody("i", "1.3.0", emptyList(), test = true)).getBoolean("test"))
        assertTrue(!JSONObject(eventsBody("i", "1.3.0", emptyList())).has("test"))
    }
}

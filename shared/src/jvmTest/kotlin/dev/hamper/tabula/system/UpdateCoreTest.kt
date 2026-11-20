package dev.hamper.tabula.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCoreTest {
    private val sha = "fab3c16c07ebaa2d9ea55b97a380b9f3c6f968e369789f09cca625e7f85a0ff7"
    private val json = """{"versionCode":7,"versionName":"1.3.0","date":"2026-11-16","sha256":"${sha.uppercase()}","size":7674560,"url":"/api/fitest/LA1-1.3.0.apk","notes":" Nastavenia po skupinách.\n"}"""
    private val r = UpdateCore.parse(json)!!

    @Test fun parsesWhatTheServerSends() {
        assertEquals(Release(7, "1.3.0", "2026-11-16", sha, 7674560, "/api/fitest/LA1-1.3.0.apk", "Nastavenia po skupinách."), r)
        assertEquals(r, UpdateCore.parse(r.json()))
    }

    @Test fun refusesAnswersADownloadCannotTrust() {
        assertNull(UpdateCore.parse(null))
        assertNull(UpdateCore.parse("<html>"))
        assertNull(UpdateCore.parse(json.replace(sha.uppercase(), "abc")))
        assertNull(UpdateCore.parse(json.replace("7674560", "0")))
        assertNull(UpdateCore.parse("""{"versionCode":7,"versionName":"1.3.0","sha256":"$sha","size":1}"""))
    }

    @Test fun aColdStartAlwaysAsksAResumeAfterAMinuteAndOnlyWhenOn() {
        val h = 3600_000L; val m = 60_000L
        assertTrue(UpdateCore.due(true, 0, 10 * h))
        // Teo, 19. 11.: the app stayed in memory, answered 5 minutes before 1.6.5 came out; back in the app it has to ask
        assertTrue(UpdateCore.due(true, 10 * h, 10 * h + 5 * m))
        // but not again within a minute of an answer (back from the camera, the installer, the sign-in in the browser)
        assertFalse(UpdateCore.due(true, 10 * h, 10 * h + 59_000))
        assertTrue(UpdateCore.due(true, 10 * h, 10 * h + m))
        // Teo, 16. 11.: answered in the morning, a release at noon, the app opened again in the afternoon: a new run asks
        assertTrue(UpdateCore.due(true, 10 * h, 10 * h + 5 * m, coldStart = true))
        assertTrue(UpdateCore.due(true, 10 * h, 5 * h)) // clock set back
        assertFalse(UpdateCore.due(false, 0, 100 * h))
        assertFalse(UpdateCore.due(false, 0, 100 * h, coldStart = true))
        // a cold start still keeps the pause after failures of this run
        assertFalse(UpdateCore.due(true, 0, 100 * h, failures = 1, lastFail = 100 * h - 30_000, coldStart = true))
    }

    @Test fun aFailedCheckIsAskedAgainInMinutesNotHours() {
        val m = 60_000L; val h = 3600_000L
        // never answered (lastOk 0) and one failure a moment ago: wait a minute, then ask again
        assertFalse(UpdateCore.due(true, 0, 100 * h, failures = 1, lastFail = 100 * h - 30_000))
        assertTrue(UpdateCore.due(true, 0, 100 * h, failures = 1, lastFail = 100 * h - m))
        // the pauses grow 1, 5, 15 minutes and stay at 15
        assertEquals(listOf(0L, m, 5 * m, 15 * m, 15 * m, 15 * m), (0..5).map { UpdateCore.retryAfter(it) })
        assertFalse(UpdateCore.due(true, 0, 100 * h, failures = 2, lastFail = 100 * h - 4 * m))
        assertTrue(UpdateCore.due(true, 0, 100 * h, failures = 2, lastFail = 100 * h - 5 * m))
        assertFalse(UpdateCore.due(true, 0, 100 * h, failures = 7, lastFail = 100 * h - 14 * m))
        assertTrue(UpdateCore.due(true, 0, 100 * h, failures = 7, lastFail = 100 * h - 15 * m))
        // a failure never waits hours: an answer a moment ago still throttles, a failure since does not add to it
        assertFalse(UpdateCore.due(true, 100 * h - 30_000, 100 * h, failures = 0))
        // a clock set back ends the pause
        assertTrue(UpdateCore.due(true, 0, 100 * h, failures = 3, lastFail = 101 * h))
        // switched off stays off
        assertFalse(UpdateCore.due(false, 0, 100 * h, failures = 1, lastFail = 0))
    }

    @Test fun offersOnlyNewerAndRespectsLaterForThatVersion() {
        val now = 1_000_000L
        assertEquals(r, UpdateCore.offer(r, 6, 0, 0, now))
        assertNull(UpdateCore.offer(r, 7, 0, 0, now))
        assertNull(UpdateCore.offer(r, 8, 0, 0, now))
        assertNull(UpdateCore.offer(r, 6, 7, now + 1, now))
        assertEquals(r, UpdateCore.offer(r, 6, 7, now, now)) // three days are over
        assertEquals(r, UpdateCore.offer(r, 6, 6, now + 1, now)) // Neskôr was for an older version
        assertNull(UpdateCore.offer(null, 6, 0, 0, now))
    }

    @Test fun resolvesTheDownloadAgainstTheCheck() {
        assertEquals("https://fitagoras.hamper.dev/api/fitest/LA1-1.3.0.apk", UpdateCore.resolve("https://fitagoras.hamper.dev/api/fitest/latest", "/api/fitest/LA1-1.3.0.apk"))
        assertEquals("http://localhost:8097/api/fitest/LA1-1.3.0.apk", UpdateCore.resolve("http://localhost:8097/api/fitest/latest", "/api/fitest/LA1-1.3.0.apk"))
        assertEquals("https://x.dev/a.apk", UpdateCore.resolve("http://localhost/api", "https://x.dev/a.apk"))
    }

    @Test fun saysDayAndSizeTheSlovakWay() {
        assertEquals("16. 11. 2026", UpdateCore.day("2026-11-16"))
        assertEquals("7,3 MB", UpdateCore.megabytes(7674560))
    }

    @Test fun anOddDateNeverBreaksTheSheet() {
        assertEquals("", UpdateCore.parse(json.replace("2026-11-16", "2026-xx-16"))!!.date)
        assertEquals("2026-xx-16", UpdateCore.day("2026-xx-16"))
        assertEquals("", UpdateCore.day(""))
    }
    @Test fun asksWithItsOwnVersion() {
        assertEquals("https://x.cz/api/fitest/latest?since=19", UpdateCore.checkUrl("https://x.cz/api/fitest/latest", 19))
        assertEquals("https://x.cz/latest?k=1&since=19", UpdateCore.checkUrl("https://x.cz/latest?k=1", 19))
    }

    @Test fun listsTheNotesOfEveryNewerVersion() {
        // the server's answer to ?since=17: the newest and the two versions between, newest first
        val h = """{"versionCode":20,"versionName":"1.3.13","date":"2026-11-17","sha256":"$sha","size":9,"url":"/a.apk","notes":"Nové","history":[""" +
            """{"versionCode":20,"versionName":"1.3.13","date":"2026-11-17","notes":"Nové"},""" +
            """{"versionCode":19,"versionName":"1.3.12","date":"2026-11-16","notes":"- Oprava\n- Druhá"},""" +
            """{"versionCode":18,"versionName":"1.3.11","date":"bad","notes":""},""" +
            """{"versionName":"bez kódu"},""" +
            """{"versionCode":17,"versionName":"1.3.10","date":"2026-11-15","notes":"Stará"}]}"""
        val rel = UpdateCore.parse(h)!!
        assertEquals(listOf(20, 19, 18, 17), rel.history.map { it.versionCode }) // the odd entry left out, not the answer
        assertEquals("", rel.history[2].date)
        assertEquals(rel, UpdateCore.parse(rel.json())) // a stored offer keeps its history
        // an app on 17 sees 1.3.13 and 1.3.12 (1.3.11 has no notes), never its own version's
        assertEquals(listOf("1.3.13", "1.3.12"), UpdateCore.changes(rel, 17).map { it.versionName })
        assertEquals(listOf("1.3.13"), UpdateCore.changes(rel, 19).map { it.versionName })
        // an older server without history: the newest notes as before
        assertEquals(listOf("Nastavenia po skupinách."), UpdateCore.changes(r, 5).map { it.notes })
        // the answer's own notes win for the newest version, even when its history entry has none
        val bare = rel.copy(history = listOf(VersionNotes(20, "1.3.13", "2026-11-17", "")) + rel.history.drop(1))
        assertEquals(listOf("Nové", "- Oprava\n- Druhá"), UpdateCore.changes(bare, 18).map { it.notes })
        // a newest version without notes leaves the older ones, under their own heading in the sheet
        assertEquals(listOf(19), UpdateCore.changes(rel.copy(notes = "", history = rel.history.drop(1)), 18).map { it.versionCode })
        // a history without the newest version still starts with it
        assertEquals(listOf(20, 19), UpdateCore.changes(rel.copy(history = rel.history.drop(1)), 18).map { it.versionCode })
    }
}

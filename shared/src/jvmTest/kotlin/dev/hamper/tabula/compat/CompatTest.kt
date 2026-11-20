package dev.hamper.tabula.compat

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The compat layer must give common code the results the Android app got from org.json and java.time, so progress
 * saved by FITest 1.3.x reads and writes the same after the update.
 */
class CompatTest {
    /** A progress document as FITest 1.3.1 writes it (Android's org.json: insertion order, "/" escaped, no ".0"). */
    private val stored = """{"v":2,"theme":"Dark","haptics":true,"clipSpeed":1.25,"reminder":false,"course":"dml","parallels":{"la1":"2","dml":"1"},""" +
        """"terms":{"la1-test2":"2026-11-18T14:30","dml-test2":"2026-11-25T00:00:01"},"lessonAt":{"ln":4,"obal":12},""" +
        """"lessonsDone":["ln"],"attempts":[{"k":"step:ln-3","t":1763220000123,"ok":true,"aided":false}],""" +
        """"mistakes":[{"k":"gen:param","title":"Parameter \"a\"","detail":"1\/2 á \\ ok\nnext","route":"gen:param","t":1763220000999}],""" +
        """"mocks":[{"paper":"sova-2025","t":1763220001000,"total":21,"max":40,"got":[[2,3],[0]]}],""" +
        """"reports":[{"item":"step:ln-3","text":"Chyba v zadaní: x\/y","t":1763220002000}]}"""

    @Test fun json_round_trip_is_byte_identical() {
        val o = JSONObject(stored)
        assertEquals(stored.replace("\\u00e1", "á"), o.toString())
        assertEquals("Dark", o.optString("theme"))
        assertEquals("", o.optString("missing"))
        assertEquals(1763220000123L, o.getJSONArray("attempts").getJSONObject(0).getLong("t"))
        assertEquals(listOf("la1", "dml"), o.getJSONObject("parallels").keys().asSequence().toList())
        assertEquals("1/2 á \\ ok\nnext", o.getJSONArray("mistakes").getJSONObject(0).getString("detail"))
        assertEquals(2, o.optInt("v", 1))
        assertTrue(o.optBoolean("haptics", false))
    }

    @Test fun json_numbers_and_coercion_follow_android() {
        val o = JSONObject().put("a", 2.0).put("b", 0.5).put("c", 3L).put("d", "7").put("e", null)
        assertEquals("""{"a":2,"b":0.5,"c":3}""".dropLast(1) + ""","d":"7"}""", o.toString())
        assertEquals(7, o.getInt("d"))
        assertEquals("3", o.getString("c"))
        assertFalse(o.has("e"))
        assertEquals("[1,\"x\",[2]]", JSONArray(listOf(1, "x", listOf(2))).toString())
        assertEquals(12.5, JSONObject("""{"x":12.5}""").getDouble("x"), 0.0)
    }

    @Test fun store_reads_a_stored_document_and_writes_it_back_unchanged() = runBlocking {
        try { storeRoundTrip() } finally { dev.hamper.tabula.data.Sittings.picked = { null } } // the store sets it process-wide
    }

    private suspend fun storeRoundTrip() {
        val file = MemoryStateFile(stored)
        val s = Store(file)
        assertEquals("dml", s.courseId)
        assertEquals(LocalDateTime.of(2026, 11, 18, 14, 30), s.terms["la1-test2"])
        assertEquals(12, s.lessonAt["obal"])
        assertEquals(listOf(listOf(2, 3), listOf(0)), s.mocks.single().got)
        s.save()
        repeat(100) { if (file.read() != stored) return@repeat; delay(10) }
        delay(200)
        // map order follows Compose's state maps, as on the phone; every stored value is written back the same
        // (keys this build adds with their defaults may come along)
        val before = JSONObject(stored); val after = JSONObject(file.read()!!)
        for (k in before.keys()) assertEquals(k, before.get(k), after.get(k))
        assertTrue(file.read()!!.contains("Chyba v zadaní: x\\/y"))
    }

    @Test fun local_date_time_text_is_java_time_text() {
        assertEquals("2026-11-18T14:30", LocalDateTime.of(2026, 11, 18, 14, 30).toString())
        assertEquals("2026-11-25T00:00:01", LocalDateTime.of(2026, 11, 25, 0, 0, 1).toString())
        assertEquals(LocalDateTime.of(2026, 11, 18, 14, 30), LocalDateTime.parse("2026-11-18T14:30"))
        assertEquals("14:30", LocalTime.of(14, 30).toString())
        assertEquals("2026-11-05", LocalDate.of(2026, 11, 5).toString())
    }

    @Test fun date_arithmetic_matches_java_time() {
        assertEquals(LocalDateTime.of(2026, 11, 19, 0, 10), LocalDateTime.of(2026, 11, 18, 23, 40).plusMinutes(30))
        assertEquals(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 28).plusDays(1))
        assertEquals(7L, ChronoUnit.DAYS.between(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 18)))
        assertEquals(-1L, ChronoUnit.WEEKS.between(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 10)))
        assertEquals(0L, ChronoUnit.WEEKS.between(LocalDate.of(2026, 11, 18), LocalDate.of(2026, 11, 12)))
        assertEquals("18. 11.", LocalDate.of(2026, 11, 18).format(DateTimeFormatter.ofPattern("d. M.")))
        assertEquals("05.03.2026 09:07", LocalDateTime.of(2026, 3, 5, 9, 7).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
        assertEquals(3, LocalDate.of(2026, 11, 18).dayOfWeek.value)
        assertTrue(LocalDateTime.MAX > LocalDateTime.of(9999, 12, 31, 23, 59))
        assertEquals(LocalTime.MIDNIGHT, LocalDateTime.of(2026, 1, 1, 0, 0).toLocalTime())
        val ms = LocalDate.of(2026, 11, 18).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(1794960000000L, ms)
        assertEquals(LocalDate.of(2026, 11, 18), Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
    }

    @Test fun lang_matches_the_jvm() {
        val en = java.util.Locale.ROOT // the browser's formatter has no locale: compared with the JVM's in ROOT
        assertEquals(java.lang.String.format(en, "%d:%02d", 5, 7), "%d:%02d".formatPortable(5, 7))
        assertEquals(java.lang.String.format("%d:%02d", 5, 7), "%d:%02d".format(5, 7))
        assertEquals(java.lang.String.format(en, "%d:%02d:%02d", 1, 0, 59), "%d:%02d:%02d".formatPortable(1, 0, 59))
        assertEquals(java.lang.String.format(en, "%.1f %5s|%-4d|", 2.25, "ab", 3), "%.1f %5s|%-4d|".formatPortable(2.25, "ab", 3))
        assertEquals(java.lang.String.format(en, "%+d %+d % d|%+05d|%.2f", 3, -3, 4, 7, 0.15), "%+d %+d % d|%+05d|%.2f".formatPortable(3, -3, 4, 7, 0.15))
        assertEquals(String(Character.toChars(0x1D6FC)), String(JCharacter.toChars(0x1D6FC)))
        assertEquals(Integer.numberOfLeadingZeros(37), JInteger.numberOfLeadingZeros(37))
        assertEquals(Math.floorMod(-7L, 3L), JMath.floorMod(-7L, 3L))
        assertEquals(runCatching { Math.multiplyExact(Long.MAX_VALUE, 2L) }.isFailure, runCatching { JMath.multiplyExact(Long.MAX_VALUE, 2L) }.isFailure)
        assertEquals(java.math.BigInteger.valueOf(7).pow(30), bigIntegerOf(7).pow(30))
        assertEquals(android("BI-LA1 2026-10-14/č. 5"), Uri.encode("BI-LA1 2026-10-14/č. 5"))
    }

    /** android.net.Uri.encode, as documented: letters, digits and _-!.~'()* stay, everything else as UTF-8 %XX. */
    private fun android(s: String) = java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace("%21", "!")
        .replace("%27", "'").replace("%28", "(").replace("%29", ")").replace("%7E", "~")
}

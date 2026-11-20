package dev.hamper.tabula.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.ui.plynulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Real ChatGPT streams (recorded by the harness: `scripts/tutor.sh tutor … --zaznam FILE`, the events as the screen
 * gets them, with their times) replayed on a virtual clock through the screen's writing ([plynulo]): every text on
 * screen is a prefix of the answer, never inside a formula or a tag, and the last one is the whole answer.
 */
@OptIn(ExperimentalTestApi::class)
class StreamZaznamyTest {
    private val zaznamy = File("src/jvmTest/resources/tutor/stream").listFiles { f -> f.extension == "json" }.orEmpty().sortedBy { it.name }

    /** Open formula or tag at the end of what is shown: a piece the screen must never show. */
    private fun rozsypane(s: String): String? {
        var mat = 0; var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\') { i += 2; continue }
            if (c == '$' && s.getOrNull(i + 1) == '$') { mat = if (mat == 2) 0 else if (mat == 0) 2 else mat; i += 2; continue }
            if (c == '$') mat = if (mat == 1) 0 else if (mat == 0) 1 else mat
            i++
        }
        if (mat != 0) return "otvorený vzorec"
        val tag = s.lastIndexOf('<')
        if (tag >= 0 && s.getOrNull(tag + 1)?.isLetter() == true && s.indexOf('>', tag) < 0) return "otvorená značka"
        return null
    }

    @Test fun realStreamsAreWrittenWhole() {
        assertTrue("žiadne záznamy v src/jvmTest/resources/tutor/stream", zaznamy.isNotEmpty())
        for (f in zaznamy) runComposeUiTest {
            val z = JSONObject(f.readText())
            val ud = z.getJSONArray("udalosti")
            var cel by mutableStateOf("")
            var koniec by mutableStateOf(false)
            val videne = ArrayList<Pair<String, String>>()
            setContent { val s = plynulo(cel, true, koniec, hned = false); if (videne.lastOrNull()?.first != s) videne += s to cel }
            mainClock.autoAdvance = false
            var t = 0L
            for (k in 0 until ud.length()) {
                val e = ud.getJSONObject(k)
                val dt = e.getLong("t") - t
                if (dt > 0) { mainClock.advanceTimeBy(dt); t = e.getLong("t") }
                when (e.getString("e")) {
                    "text" -> cel += e.optString("d")
                    "reset" -> cel = ""
                }
            }
            koniec = true
            mainClock.advanceTimeBy(30_000)
            val text = if (z.isNull("text")) cel else z.getString("text")
            // what the stream wrote after its last reset is the answer the app keeps
            if (!z.optBoolean("zastavene")) assertEquals(f.name, text, cel)
            assertEquals(f.name + ": posledný text na obrazovke", cel, videne.last().first)
            for ((s, vtedy) in videne) {
                assertTrue(f.name + ": nie je začiatok prijatého textu: " + s.takeLast(60), vtedy.startsWith(s))
                rozsypane(s)?.let { throw AssertionError(f.name + ": " + it + " na konci: " + s.takeLast(80)) }
                // what the screen draws of it: a tag is a card or nothing, never its markup in the text
                val kusy = dev.hamper.tabula.tutor.kusy(dev.hamper.tabula.tutor.vysaditelne(s), dev.hamper.tabula.tutor.Artefakty(), "la1")
                kusy.filterIsInstance<dev.hamper.tabula.tutor.Kus.Text>().forEach { k -> assertTrue(f.name + ": značka v texte: " + k.t.takeLast(80), "<veta" !in k.t && "<krokovac" !in k.t) }
            }
        }
    }
}

package dev.hamper.tabula.math

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** The TeX metrics the formula layout reads: every glyph the parser can emit has a box, the file parses as written. */
class TexMetricsTest {
    private val tsv = File("src/commonMain/composeResources/files/mathjax-tex-metrics.tsv").readText()

    @Test fun linesParseWithLeadingDotsNegativesAndOptionalSkew() {
        val m = TexFont.parseMetrics("normal\t2C6\t.694\t-.531\t.5\t0\nnormal\t1D434\t.716\t0\t.75\t0\t.175\r\nbad line\n")
        assertArrayEquals(floatArrayOf(.694f, -.531f, .5f, 0f, 0f), m.getValue("normal").getValue(0x2C6), 1e-6f)
        assertArrayEquals(floatArrayOf(.716f, 0f, .75f, 0f, .175f), m.getValue("normal").getValue(0x1D434), 1e-6f)
        assertEquals(2, m.getValue("normal").size)
    }

    @Test fun theShippedFontHasEveryLetterAlphabetAndTheCalligraphicCapitals() {
        val m = TexFont.parseMetrics(tsv)
        val normal = m.getValue("normal")
        // italic a–z (h is U+210E), A–Z, bold upright, italic Greek, ℝ ℤ ℕ ℚ ℂ, the √ and the two accents
        val need = (0x1D44E..0x1D467).filter { it != 0x1D455 } + 0x210E + (0x1D434..0x1D44D) + (0x1D400..0x1D433) +
            (0x1D6FC..0x1D714) + listOf(0x211D, 0x2124, 0x2115, 0x211A, 0x2102, 0x221A, 0x2C6, 0x2DC)
        assertEquals(emptyList<String>(), need.filter { it !in normal }.map { "U+%04X".format(it) })
        assertEquals(26, ('A'..'Z').count { it.code in m.getValue("cal") })
    }

    @Test fun glyphsTheFontLacksAreDrawnAsMathJaxComposesThem() {
        val c = TexFont.parseComposites(tsv)
        assertEquals("≡\u0338", c[0x2262])
        assertEquals("⊂\u0338", c[0x2284])
        assertEquals("∃\u0338", c[0x2204])
        assertEquals("′′", c[0x2033])
    }
}

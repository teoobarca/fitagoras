package dev.hamper.tabula.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A big operator with limits in running text: limits to the side, and the line box covers every glyph. Metrics are
 * close to those of Noto Sans Math ∑ and Google Sans Flex at 48 px (approximate: Paint does not run in a JVM test).
 */
class InlineSlotTest {
    private val px = 48f
    private val fontAscent = 50.7f; private val fontDescent = 13.4f   // the text font's own ascent and descent
    private fun sum(scale: Float) = Ext(.83f * px * scale, .77f * px * scale, .25f * px * scale)
    private val lim = Ext(.9f * px * .7f, .52f * px * .7f, .12f * px * .7f)        // "k=1" as a script
    private val top = Ext(.4f * px * .7f, .52f * px * .7f, 0f)                      // "n"

    @Test fun inlineSumPutsLimitsToTheSideAndStaysAboutALineTall() {
        val inText = bigOpPlaces(sum(1.2f), top, lim, px, px * .27f, text = true)
        val display = bigOpPlaces(sum(1.5f), top, lim, px, px * .27f, text = false)
        // limits right of the symbol, not above or below it
        assertTrue(inText.sup!!.first >= sum(1.2f).w && inText.sub!!.first >= sum(1.2f).w)
        // much shorter than with limits above and below
        assertTrue(inText.ext.asc + inText.ext.desc < .7f * (display.ext.asc + display.ext.desc))
        // the display form keeps limits above and below, centred
        assertTrue(display.sup!!.second < display.op.second - sum(1.5f).asc)
        assertTrue(display.sub!!.second > display.op.second + sum(1.5f).desc)
    }

    @Test fun lineBoxCoversTheGlyphBoundsOfAnInlineSum() {
        val e = bigOpPlaces(sum(1.2f), top, lim, px, px * .27f, text = true).ext
        val slot = inlineSlot(e.asc, e.desc, px, fontAscent, fontDescent)
        // y down from the baseline. A centred slot sits on the text's centre, like PlaceholderVerticalAlign.TextCenter.
        val yc = (fontDescent - fontAscent) / 2
        val slotTop = if (slot.centered) yc - slot.height / 2 else -slot.height
        val slotBottom = if (slot.centered) yc + slot.height / 2 else 0f
        // the line box is the text's own box grown by the slot
        val lineTop = minOf(-fontAscent, slotTop); val lineBottom = maxOf(fontDescent, slotBottom)
        assertTrue("top: line ${lineTop} glyphs ${-e.asc}", lineTop <= -e.asc)
        assertTrue("bottom: line $lineBottom glyphs ${e.desc}", lineBottom >= e.desc)
        // and the baseline inside the slot puts the glyphs where they are measured from
        assertEquals(0f, slotTop + slot.baseline, .001f)
    }

    @Test fun ordinaryFormulaKeepsTheCapitalHighSlot() {
        val slot = inlineSlot(.7f * px, .2f * px, px, fontAscent, fontDescent)
        assertFalse(slot.centered)
        assertEquals(minOf(.7f * px, px * .74f), slot.height, .001f)
    }

    @Test fun aBracketAHairDeeperThanTheFontStaysInTheLinesFixedHeight() {
        // the TeX font's ( reaches .291 em under the baseline, Google Sans Flex .286: the line's leading has room for it
        val lead = (26f / 17f * px - fontAscent - fontDescent) / 2
        assertFalse(inlineSlot(.873f * px, fontDescent + .005f * px, px, fontAscent, fontDescent, lead).centered)
        assertTrue(inlineSlot(.873f * px, fontDescent + lead + 1f, px, fontAscent, fontDescent, lead).centered)
    }

    @Test fun aFormulaDeeperThanTheTextGrowsTheLineDownward() {
        val slot = inlineSlot(.5f * px, .6f * px, px, fontAscent, fontDescent)
        assertTrue(slot.centered)
        val yc = (fontDescent - fontAscent) / 2
        assertTrue(yc + slot.height / 2 >= .6f * px)
    }
}

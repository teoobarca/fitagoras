package dev.hamper.tabula.math

import dev.hamper.tabula.data.Symbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarkupTest {
    private val ssub = Symbol("ssub", "\\ssub", """\\ssub""", "je podpriestor", "…", "podpriestor")
    private val tn = Symbol("tn", "T^n", """(?<![A-Za-z\\])T\^(?:n|\{n\})""", "aritmetický priestor", "…", "tn")
    private val obal = Symbol("obal", "\\langle\\cdot\\rangle", """\\langle.*?\\rangle""", "lineárny obal", "…", "obal")

    @Test fun display_formulas_split_the_text_and_take_their_period() {
        assertEquals(
            listOf(Seg.Text("Průnik je podprostor, tedy vždy platí"), Seg.Display("P\\cap Q\\ssub T^n\\text{.}")),
            segments("Průnik je podprostor, tedy vždy platí $\$P\\cap Q\\ssub T^n$$."),
        )
        assertEquals(
            listOf(Seg.Text("Pak"), Seg.Display("a=b\\text{,}"), Seg.Text("kde \$a\$ je číslo.")),
            segments("Pak $\$a=b$$, kde \$a\$ je číslo."),
        )
        assertEquals(listOf(Seg.Text("Bez vzorca \$x\$.")), segments("Bez vzorca \$x\$."))
    }

    @Test fun plain_markup_drops_only_the_markup() {
        assertEquals("tedy vždy platí \$x\$.", plainMarkup("tedy ==vždy== platí $\$x$$."))
    }

    @Test fun symbols_are_wrapped_once_and_only_when_granted() {
        val syms = listOf(ssub, tn, obal)
        assertEquals("P\\tap{0}{\\ssub} \\tap{1}{T^n}", tapSymbols("P\\ssub T^n", syms) { true })
        // the second ⊂⊂ in the same formula stays plain: only the first occurrence is marked
        assertEquals("\\tap{0}{\\ssub}\\ssub", tapSymbols("\\ssub\\ssub", syms) { true })
        assertEquals("P\\ssub \\tap{1}{T^n}", tapSymbols("P\\ssub T^n", syms) { it != 0 })
        // a command is never cut: \ssub is not found inside \ssubx
        assertEquals("\\ssubx", tapSymbols("\\ssubx", syms) { true })
        // a letter inside a longer name is not the symbol
        assertEquals("\\mathbb{Z}_p^n", tapSymbols("\\mathbb{Z}_p^n", syms) { true })
        assertEquals("\\tap{2}{\\langle\\mathbf{x},\\mathbf{y}\\rangle}=L", tapSymbols("\\langle\\mathbf{x},\\mathbf{y}\\rangle=L", syms) { true })
    }

    @Test fun a_match_that_would_cut_a_brace_group_is_skipped() {
        val bad = Symbol("bad", "x", """x\}""", "x", "x", "x")
        assertEquals("\\mathbf{x}", tapSymbols("\\mathbf{x}", listOf(bad)) { true })
    }
}

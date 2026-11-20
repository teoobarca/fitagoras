package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RefsTest {
    private val la1 = RefIndexes.getValue("la1")
    private val dml = RefIndexes.getValue("dml")

    private fun ids(ix: RefIndex, s: String) = ix.find(s).map { it.target!!.id }

    @Test fun slovak_and_czech_forms_resolve() {
        assertEquals(listOf("podpriestor"), ids(la1, "Definícia 2.7, Príklady 2.11 až 2.17"))
        assertEquals(listOf("podpriestor"), ids(la1, "podľa Definície 2.7 stačí overiť tri veci"))
        assertEquals(listOf("podpriestor"), ids(la1, "Def. 2.7"))
        assertEquals(listOf("t2-18"), ids(la1, "(Veta 2.18)"))
        assertEquals(listOf("t2-18"), ids(la1, "Skriptá, Věta 2.18"))
        // a remark the app quotes opens its quote; an example is not in the app, nothing to open
        assertEquals(listOf("t2-8", "ref:poznamka 2.10"), ids(la1, "Pozorovanie 2.8, Poznámka 2.10"))
        assertEquals(listOf("ref:poznamka 2.10"), ids(la1, "Příklad 2.11 a Poznámka 2.10"))
        // every number of a Czech list opens its own card
        assertEquals(listOf("t2-59", "t2-60"), ids(la1, "Věty 2.59 a 2.60"))
    }

    @Test fun the_range_covers_the_word_and_the_number() {
        val s = "Pozri Vetu 2.18 hore."
        val h = la1.find(s).single()
        assertEquals("Vetu 2.18", s.substring(h.range))
    }

    @Test fun courses_resolve_on_their_own_numbering() {
        // DML numbers each kind on its own: Definice 5.1 and Věta 5.5 are different things than in LA1
        assertTrue(dml.find("Definice 5.1").single().target is RefTarget.Def)
        assertTrue(ids(la1, "Definice 5.1").isEmpty())
    }

    @Test fun every_numbered_item_is_reachable() {
        for (c in Courses) {
            val ix = RefIndexes.getValue(c.id)
            for (k in c.concepts) if (Regex("""Definice\s+\d+\.\d+""").containsMatchIn(k.src))
                assertTrue("${k.id} (${k.src}) not reachable by its own source", ix.find(k.src).any { it.target!!.id == k.id })
            for (t in c.theorems) if (Regex("""(Věta|Věty|Pozorování|Tvrzení|Lemma|Důsledek|Postup)\s+\d+\.\d+""").containsMatchIn(t.src))
                assertTrue("${t.id} (${t.src}) not reachable by its own source", ix.find(t.src).isNotEmpty())
        }
    }
}

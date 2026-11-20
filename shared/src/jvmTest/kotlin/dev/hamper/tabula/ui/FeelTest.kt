package dev.hamper.tabula.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SentenceTest {
    private fun split(t: String) = firstSentence(t)

    @Test fun oneSentenceHasNoRest() {
        assertEquals("Správne." to null, split("Správne."))
        assertNull(split("Báza má tri vektory").second)
    }

    @Test fun splitsAtTheFirstEnd() {
        assertEquals("Prvá veta." to "Druhá veta. Tretia.", split("Prvá veta. Druhá veta. Tretia."))
        assertEquals("Naozaj?" to "Áno.", split("Naozaj? Áno."))
        assertEquals("Pozor!" to "Čo ďalej.", split("Pozor! Čo ďalej."))
    }

    @Test fun abbreviationsDoNotEnd() {
        assertNull(split("Platí to napr. Pre každý vektor z tohto priestoru").second)
        assertEquals("Je to tzv. Gaussova eliminácia." to "Ide po riadkoch.", split("Je to tzv. Gaussova eliminácia. Ide po riadkoch."))
        assertEquals("Pozri Def. 2.9 v skriptách." to "Tam je to celé.", split("Pozri Def. 2.9 v skriptách. Tam je to celé."))
        assertNull(split("Je to lineárne, t. j. Zachováva súčet").second)
        assertNull(split("Platí to, resp. Neplatí").second)
    }

    @Test fun numbersDoNotEnd() {
        assertEquals("Veta 2.7 hovorí presne toto." to "Preto stačí jeden.", split("Veta 2.7 hovorí presne toto. Preto stačí jeden."))
        assertNull(split("Hodnota 3.5 je mimo").second)
        // an ordinal before a lower-case word is not an end
        assertNull(split("Pozri 2. vetu kapitoly").second)
    }

    @Test fun mathIsNeverSplit() {
        assertEquals("Platí §a. B = c§ pre všetky." to "Ďalej.", split("Platí §a. B = c§ pre všetky. Ďalej."))
        assertEquals("Ak \$x. Y\$ je nula, áno." to "Inak nie.", split("Ak \$x. Y\$ je nula, áno. Inak nie."))
        // an unclosed formula: no split at all, the whole text shows
        assertNull(split("Platí §a. B je. Ďalšia.").second)
    }

    @Test fun formulaAfterThePeriodStartsASentence() {
        assertEquals("Súbor je LZ." to "§\\mathbf{z}§ je iný.", split("Súbor je LZ. §\\mathbf{z}§ je iný."))
        assertEquals("Hodnota je §2§." to "Ďalej.", split("Hodnota je §2§. Ďalej."))
    }

    @Test fun boldIsNeverSplit() {
        assertEquals("**Pozor. Toto** platí vždy." to "Ďalej.", split("**Pozor. Toto** platí vždy. Ďalej."))
    }

    @Test fun closingQuoteStaysWithItsSentence() {
        assertEquals("Hovorí sa „báza.“" to "Potom ďalej.", split("Hovorí sa „báza.“ Potom ďalej."))
    }
}

class BuzzTest {
    @Test fun rightAndWrongDiffer() {
        assertNotEquals(Buzz.Right.rich, Buzz.Wrong.rich)
        assertNotEquals(Buzz.Right.fallback, Buzz.Wrong.fallback)
    }

    @Test fun threeStrengthsAndAudibleGaps() {
        for (b in Buzz.entries) for (p in b.rich) {
            assertTrue("$b scale ${p.scale}", p.scale in setOf(.5f, .7f, 1f))
            assertTrue("$b gap ${p.delay}", p.delay == 0 || p.delay >= 50)
        }
    }

    @Test fun describesForTheLog() {
        assertEquals("TICK 0.5 → +60ms CLICK 1.0", Buzz.Right.describe(true))
        assertEquals("REJECT", Buzz.Wrong.describe(false))
    }
}

package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.La1
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tutor checks a student's own basis with the app's own functions, the same as the box "Máš inú bázu?" (Teo's case,
 * 18. 11.: sova 2025, příklad 2, completing ((1,2,0,4),(3,3,0,1)) to a basis of Z_5^4 with other vectors than the sample).
 */
class VlastnaBazaTest {
    private val priklad = La1.papers.first { it.id == "sova-2025" }.problems[1]
    private val m = miesto("Sova 2025, príklad 2", priklad.units[0], "la1", priklad.text, priklad)
    private fun nastroj(n: String) = Nastroje.first { it.nazov == n }
    private fun spusti(n: String, a: JSONObject) = runBlocking {
        nastroj(n).spusti(VolanieKontext(m, emptyList(), UkazkovyTutor("").klient(), Nastavenie("gpt-6.2-sol", "low", "gpt-6.2-sol"), Artefakty()) {}, a)
    }
    private fun vektory(vararg v: List<Int>) = JSONArray().apply { v.forEach { x -> put(JSONArray().apply { x.forEach { put(it.toString()) } }) } }

    @Test fun ownCompletionIsCheckedAndTheLaterPartsComputedInIt() {
        assertTrue(nastroj("vlastna_baza").dostupny(m, false))
        val ok = spusti("vlastna_baza", JSONObject().put("vektory", vektory(listOf(0, 0, 1, 0), listOf(0, 0, 0, 1))))
        assertTrue(ok, ok.startsWith("Kontrola appky (tá istá") && "Ďalšie časti v tejto báze" in ok)
        // e1, e3 with the given two: dependent over Z_5, the app's own words say why
        val zle = spusti("vlastna_baza", JSONObject().put("vektory", vektory(listOf(1, 0, 0, 0), listOf(0, 0, 1, 0))))
        assertTrue(zle, zle.startsWith("Kontrola appky: nie je to správne."))
    }

    @Test fun onlyWhereThePaperHasManyRightBases() {
        val bez = miesto("Sova 2025, príklad 2", priklad.units[0], "la1")
        assertTrue(!nastroj("vlastna_baza").dostupny(bez, false))
    }

    @Test fun isBasisUsesTheAppsJudge() {
        val a = JSONObject().put("otazka", "je_baza").put("teleso", "Z5").put("vektory", vektory(listOf(1, 2, 0, 4), listOf(3, 3, 0, 1), listOf(0, 0, 1, 0), listOf(0, 0, 0, 1)))
        assertTrue(spusti("la", a).startsWith("Áno"))
        val lz = JSONObject().put("otazka", "je_baza").put("teleso", "Z5").put("vektory", vektory(listOf(1, 2, 0, 4), listOf(3, 3, 0, 1), listOf(1, 0, 0, 0), listOf(0, 0, 1, 0)))
        assertTrue(spusti("la", lz).startsWith("Nie"))
        val malo = JSONObject().put("otazka", "je_baza").put("teleso", "Z5").put("vektory", vektory(listOf(1, 2, 0, 4), listOf(3, 3, 0, 1)))
        assertTrue(spusti("la", malo).startsWith("Nie: súbor je LN"))
    }

    /** Review 18. 11.: the same coordinates in another basis still come with that basis' own computation. */
    @Test fun aPartWithTheSameResultButAnotherComputationIsShown() {
        val o = spusti("vlastna_baza", JSONObject().put("vektory", vektory(listOf(0, 2, 0, 0), listOf(0, 0, 1, 0))))
        assertTrue(o, o.startsWith("Kontrola appky (tá istá") && "Ďalšie časti v tejto báze" in o && "b)" in o)
    }

    /** The header's "Opýtať sa" on a paper problem has the tool too (the place by its route, as the app opens it). */
    @Test fun theRouteOfAPaperProblemCarriesIt() {
        val r = miestoZRouty("paper:sova-2025:2:0")!!
        assertTrue(nastroj("vlastna_baza").dostupny(r, false))
    }
}

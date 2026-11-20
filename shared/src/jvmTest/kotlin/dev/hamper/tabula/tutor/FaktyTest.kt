package dev.hamper.tabula.tutor

import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import org.junit.Assert.assertTrue
import org.junit.Test

/** The engine's facts the judges found wrong on set 2468 (18. 11.), each as it should be now. */
class FaktyTest {
    private fun mat(f: Field, vararg r: List<Int>) = r.map { row -> row.map { f.norm(Frac.of(it.toLong())) } }
    private fun prepis(vararg m: List<List<Int>>, ciara: Int? = null) =
        GemPrepis(m.map { x -> PrepisMatica(x.map { row -> row.map { Citanie(it.toString()) } }, ciara) })
    private fun fakty(u: GemUloha, p: GemPrepis, k: (List<GemUloha>) -> Kontext): List<String> {
        val ulohy = listOf(u)
        val vys = Vysledok(Prepis(listOf(Strana(1, Strana.Citatelnost.OK, 0, null)), listOf(Blok("gem", 1, null, p))), listOf(overPostup(ulohy, p)))
        return fakty(k(ulohy), vys, vys.hlavny)
    }
    private fun kontext(ulohy: List<GemUloha>) = Kontext("Úloha", "Otázka", "x", "y", null, emptyList(), ulohy)

    /** v157: rows in another order turn the columns of e_i into other-looking ones; which e_i to add follows the assignment. */
    @Test fun unitColumnsUnderARowPermutation() {
        val f = Field.Zp(5)
        val start = Mat(mat(f, listOf(2, 2, 1, 0, 0, 0), listOf(2, 4, 0, 1, 0, 0), listOf(3, 0, 0, 0, 1, 0), listOf(3, 3, 0, 0, 0, 1)))
        val p = prepis(
            listOf(listOf(3, 3, 0, 0, 0, 1), listOf(3, 0, 0, 0, 1, 0), listOf(2, 2, 1, 0, 0, 0), listOf(2, 4, 0, 1, 0, 0)),
            listOf(listOf(2, 4, 0, 1, 0, 0), listOf(3, 0, 0, 0, 1, 0), listOf(2, 2, 1, 0, 0, 0), listOf(3, 3, 0, 0, 0, 1)),
            listOf(listOf(2, 4, 0, 1, 0, 0), listOf(0, 4, 0, 1, 1, 0), listOf(0, 3, 1, 4, 0, 0), listOf(0, 2, 0, 1, 0, 1)),
            listOf(listOf(2, 4, 0, 1, 0, 0), listOf(0, 4, 0, 1, 1, 0), listOf(0, 0, 1, 2, 3, 0), listOf(0, 0, 0, 3, 2, 1)),
        )
        val fk = fakty(GemUloha(f, start), p, ::kontext)
        assertTrue(fk.joinToString("\n"), fk.any { "3. stĺpec je \$\\mathbf{e}_{1}\$ zo zadania" in it && "poradie stĺpcov v zadaní" in it })
    }

    /** (A|E) of a singular A: the nonzero rows are counted in the left block, not across E's image. */
    @Test fun augmentedWithIdentityCountsTheLeftBlock() {
        val f = Field.R
        val start = Mat(mat(f, listOf(1, 2, 1, 0), listOf(2, 4, 0, 1)), 2)
        val p = prepis(listOf(listOf(1, 2, 1, 0), listOf(2, 4, 0, 1)), listOf(listOf(1, 2, 1, 0), listOf(0, 0, -2, 1)), ciara = 2)
        val fk = fakty(GemUloha(f, start), p, ::kontext)
        assertTrue(fk.joinToString("\n"), fk.any { "v ľavom bloku je 1 nenulový riadok" in it })
        // rows in another order in an (A|E): no word about which e_i to add, that is no completion
        val prehodene = prepis(listOf(listOf(2, 4, 0, 1), listOf(1, 2, 1, 0)), listOf(listOf(1, 2, 1, 0), listOf(2, 4, 0, 1)), listOf(listOf(1, 2, 1, 0), listOf(0, 0, -2, 1)), ciara = 2)
        assertTrue(fakty(GemUloha(f, start), prehodene, ::kontext).none { "zo zadania" in it })
        assertTrue(fk.joinToString("\n"), fk.none { "sú 2 nenulové riadky" in it })
    }

    private fun preklep(ulohy: List<GemUloha>) = Kontext("Úloha", "$\\mathbf{x}=$", "3, 4", "(2,4)", null, emptyList(), ulohy,
        ocakavane = listOf(Frac.of(2), Frac.of(4)), menovky = listOf("x_1", "x_2"), zle = listOf(0))

    /** v72: x stands on paper only as a solution read off a column (the identity left of the bar), never by chance. */
    @Test fun theAnswerInAColumnOnlyAsASolution() {
        val f = Field.R
        val solved = fakty(GemUloha(f, Mat(mat(f, listOf(1, 1, 6), listOf(0, 1, 4)), 2)), prepis(listOf(listOf(1, 1, 6), listOf(0, 1, 4)), listOf(listOf(1, 0, 2), listOf(0, 1, 4)), ciara = 2), ::preklep)
        assertTrue(solved.joinToString("\n"), solved.any { "je na papieri celé" in it })
        val chance = fakty(GemUloha(f, Mat(mat(f, listOf(1, 2, 5), listOf(1, 6, 13)), 2)), prepis(listOf(listOf(1, 2, 5), listOf(1, 6, 13)), listOf(listOf(1, 2, 5), listOf(0, 4, 8)), ciara = 2), ::preklep)
        assertTrue(chance.joinToString("\n"), chance.none { "je na papieri celé" in it })
    }
}

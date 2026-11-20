package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import dev.hamper.tabula.data.la1.jordan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GEM is shown one way (docs/CONTENT.md, "GEM krok po kroku"): every chain is a walk the component steps through,
 * written as the course writes it (konvencie 15. 11.). These rules fail if a hand-typed chain or another notation
 * comes back.
 */
class GemWalkTest {
    private val formulas = allFormulas()
    private val walks = allGemWalks()

    @Test fun everyGemChainIsAWalk() {
        val raw = formulas.filter { (_, f) -> f.gem == null && "pmatrix" in f.tex && "\\sim" in f.tex }
        assertTrue("GEM chains typed as TeX (use Field.walk / GemWalk.of):\n" + raw.joinToString("\n") { it.where + ": " + it.f.tex.take(80) }, raw.isEmpty())
        assertTrue("walks found: ${walks.size}", walks.size > 40)
    }

    @Test fun oneKindOfOperationPerTilde() {
        for ((w, walk) in walks) for ((k, mv) in walk.moves.withIndex())
            assertTrue("$w ∼$k: ${mv.ops.map { it.kind }}", mv.ops.isNotEmpty() && mv.kind in setOf("G1", "G2", "G3") && mv.ops.all { it.kind == mv.kind })
    }

    @Test fun eachStepChangesOnlyTheRowsItNames() {
        for ((w, walk) in walks) for (k in 1..walk.size) {
            val named = walk.moves[k - 1].ops.flatMap { o -> when (o) { is Swap -> listOf(o.i, o.j); is Combine -> listOf(o.target); is Scale -> listOf(o.i); is PolyCombine -> listOf(o.target); else -> emptyList() } }.toSet()
            assertTrue("$w ∼$k changes ${walk.changed(k)} but names $named", named.containsAll(walk.changed(k)))
        }
    }

    @Test fun courseNotationOnly() {
        // "R_3 → R_3 − R_1" was the old app's notation; the course writes ř3 − ř1 under the ∼ (konvencie.md)
        val bad = Regex("""R_\{?\d\}?\s*\\(to|rightarrow|leftrightarrow)|\\to\s*R_""")
        val texts = formulas.flatMap { (w, f) -> listOf(w to f.tex) + listOfNotNull(f.note?.let { w to it }) } +
            walks.flatMap { (w, walk) -> walk.moves.flatMap { mv -> mv.ops.map { w to it.tex() } } }
        val hits = texts.filter { bad.containsMatchIn(it.second) }
        assertTrue("old row notation:\n" + hits.joinToString("\n") { "${it.first}: ${it.second.take(80)}" }, hits.isEmpty())
        for ((w, walk) in walks) for (mv in walk.moves) for (o in mv.ops) assertTrue("$w: ${o.tex()}", "\\text{ř}" in o.tex())
    }

    @Test fun lessonPicturesEndInHst() {
        for ((w, walk) in walks.filter { it.first.endsWith("explore") }) assertTrue(w, walk.endsInHst)
    }

    /** The hand-typed chains, now computed: their matrices are the skriptá's (Příklad 2.28, 3.38, 4.12). */
    @Test fun computedChainsMatchTheTextbook() {
        val z5 = Field.Zp(5)
        val p228 = z5.walk(intMat(listOf(1, 4, 3, 0), listOf(2, 2, 4, 0), listOf(3, 3, 2, 0), bar = 3), listOf(rowSub(2, 2, 1), rowSub(3, 3, 1)), listOf(rowSwap(2, 3)), listOf(rowSub(3, 4, 2)))
        assertEquals(listOf(listOf("1", "4", "3", "0"), listOf("0", "4", "3", "0"), listOf("0", "1", "3", "0")), p228.frames[1].cells)
        assertEquals(listOf(listOf("1", "4", "3", "0"), listOf("0", "1", "3", "0"), listOf("0", "0", "1", "0")), p228.frames.last().cells)
        val j = Field.R.jordan(intMat(listOf(1, 3, 2, 0, 3, 3), listOf(1, 1, 1, -1, 5, -2), listOf(2, 8, 5, 3, 7, 13), listOf(3, 9, 6, 2, 12, 11), bar = 5), 5)
        assertEquals("\\begin{pmatrix|5}1&0&\\tfrac{1}{2}&0&\\tfrac{33}{4}&-3\\\\0&1&\\tfrac{1}{2}&0&-\\tfrac{7}{4}&2\\\\0&0&0&1&\\tfrac{3}{2}&1\\\\0&0&0&0&0&0\\end{pmatrix}",
            GemWalk.of(j.start, j.steps).frames.last().tex())
        val z7 = Field.Zp(7)
        val b = z7.walk(intMat(listOf(1, 3, 2, 1, 0, 0), listOf(0, 4, 4, 5, 1, 0), listOf(0, 0, 4, 5, 3, 1), bar = 3),
            listOf(rowSub(1, 4, 3), rowSub(2, 1, 3)), listOf(rowSub(1, 6, 2)), listOf(rowTimes(2, 2.q()), rowTimes(3, 2.q())))
        assertEquals(listOf(listOf("1", "0", "0", "2", "0", "2"), listOf("0", "1", "0", "0", "3", "5"), listOf("0", "0", "1", "3", "6", "2")), b.frames.last().cells)
    }

    @Test fun fractionFreeStepIsG2ThenG3() {
        // the engine's 2·ř2 - 3·ř1 is (G2) 2·ř2, then (G3) ř2 - 3·ř1 on paper
        val w = GemWalk.of(Field.R.echelon(intMat(listOf(2, 0), listOf(3, 1))))
        assertEquals(listOf("G2", "G3"), w.moves.map { it.kind })
        assertEquals(listOf(listOf("2", "0"), listOf("6", "2")), w.frames[1].cells)
        assertEquals(listOf(listOf("2", "0"), listOf("0", "2")), w.frames[2].cells)
    }

    @Test fun mixedStepsSplitIntoOneKindEach() {
        // the engine swaps and subtracts in one step; on paper that is two ∼, (G1) then (G3)
        val e = Field.R.echelon(intMat(listOf(0, 1, 2), listOf(1, 1, 1), listOf(2, 3, 4)))
        val w = GemWalk.of(e)
        assertTrue(w.moves.first().kind == "G1")
        assertEquals(e.result.rows.map { r -> r.map { it.tex(true) } }, w.frames.last().cells)
    }

    /**
     * The chains that were typed by hand before 16. 11. (as in the skriptá), now computed from their operations: every
     * matrix they showed is still there, in order (a walk may add the matrix between two kinds of operation).
     */
    @Test fun lessonWalksKeepTheTextbookMatrices() {
        val lessonWalks = AllLessons.flatMap { l -> l.steps.filterIsInstance<Step.Worked>().flatMap { w -> w.solution.steps.flatMap { it.math.mapNotNull { f -> f.gem } } } }
            .map { w -> w.frames.map { it.tex() } }
        for (old in oldChains) {
            val found = lessonWalks.any { frames -> var i = 0; for (f in frames) if (i < old.size && f == old[i]) i++; i == old.size }
            assertTrue("no lesson walk shows these matrices in order: ${old.first()}", found)
        }
    }

    private val oldChains = listOf(
        listOf("""\begin{pmatrix|2}1&1&1\\1&3&5\end{pmatrix}""", """\begin{pmatrix|2}1&1&1\\0&2&4\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&4&3&0\\2&2&4&0\\3&3&2&0\end{pmatrix}""", """\begin{pmatrix|3}1&4&3&0\\0&4&3&0\\0&1&3&0\end{pmatrix}""", """\begin{pmatrix|3}1&4&3&0\\0&1&3&0\\0&0&1&0\end{pmatrix}"""),
        listOf("""\begin{pmatrix|2}1&0&1\\1&1&0\\1&-1&3\end{pmatrix}""", """\begin{pmatrix|2}1&0&1\\0&1&-1\\0&-1&2\end{pmatrix}""", """\begin{pmatrix|2}1&0&1\\0&1&-1\\0&0&1\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&4&3&v_1\\2&2&4&v_2\\3&3&2&v_3\end{pmatrix}""", """\begin{pmatrix|3}1&4&3&v_1\\0&4&3&3v_1+v_2\\0&1&3&2v_1+v_3\end{pmatrix}""", """\begin{pmatrix|3}1&4&3&v_1\\0&1&3&2v_1+v_3\\0&0&1&v_2+v_3\end{pmatrix}"""),
        listOf("""\begin{pmatrix}1&0&1\\1&1&0\\0&1&1\end{pmatrix}""", """\begin{pmatrix}1&0&1\\0&1&-1\\0&1&1\end{pmatrix}""", """\begin{pmatrix}1&0&1\\0&1&-1\\0&0&2\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&1&1&4\\1&1&2&2\\1&2&3&3\end{pmatrix}""", """\begin{pmatrix|3}1&1&1&4\\0&0&1&-2\\0&1&2&-1\end{pmatrix}""", """\begin{pmatrix|3}1&1&1&4\\0&1&2&-1\\0&0&1&-2\end{pmatrix}"""),
        listOf("""\begin{pmatrix}1&2&0&3\\2&1&1&0\\3&3&1&3\end{pmatrix}""", """\begin{pmatrix}1&2&0&3\\0&2&1&4\\0&2&1&4\end{pmatrix}""", """\begin{pmatrix}1&2&0&3\\0&2&1&4\\0&0&0&0\end{pmatrix}"""),
        listOf("""\begin{pmatrix}1&2&0&1\\2&3&1&0\\0&1&-1&2\end{pmatrix}""", """\begin{pmatrix}1&2&0&1\\0&-1&1&-2\\0&1&-1&2\end{pmatrix}""", """\begin{pmatrix}1&2&0&1\\0&-1&1&-2\\0&0&0&0\end{pmatrix}"""),
        listOf("""\begin{pmatrix}1&2&0&1\\0&-1&1&-2\\1&1&1&-1\end{pmatrix}""", """\begin{pmatrix}1&2&0&1\\0&-1&1&-2\\0&-1&1&-2\end{pmatrix}""", """\begin{pmatrix}1&2&0&1\\0&-1&1&-2\\0&0&0&0\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&3&2&1&0&0\\2&3&1&0&1&0\\3&4&5&0&0&1\end{pmatrix}""", """\begin{pmatrix|3}1&3&2&1&0&0\\0&4&4&5&1&0\\0&2&6&4&0&1\end{pmatrix}""", """\begin{pmatrix|3}1&3&2&1&0&0\\0&4&4&5&1&0\\0&0&4&5&3&1\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&3&0&2&2&3\\0&4&0&0&5&6\\0&0&4&5&3&1\end{pmatrix}""", """\begin{pmatrix|3}1&0&0&2&0&2\\0&1&0&0&3&5\\0&0&1&3&6&2\end{pmatrix}"""),
        listOf("""\begin{pmatrix|3}1&2&-1&2\\2&4&1&7\\1&2&2&5\end{pmatrix}""", """\begin{pmatrix|3}1&2&-1&2\\0&0&3&3\\0&0&3&3\end{pmatrix}""", """\begin{pmatrix|3}1&2&-1&2\\0&0&3&3\\0&0&0&0\end{pmatrix}"""),
        listOf("""\begin{pmatrix|4}1&1&1&1&0\\2&0&3&0&0\end{pmatrix}""", """\begin{pmatrix|4}1&1&1&1&0\\0&-2&1&-2&0\end{pmatrix}"""),
    )
}

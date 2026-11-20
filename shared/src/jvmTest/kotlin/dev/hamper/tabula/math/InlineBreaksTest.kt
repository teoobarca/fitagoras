package dev.hamper.tabula.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Where an inline formula may wrap. Pieces are shown with each item as its glyph, a group (a vector letter, a
 * power, a fraction) as □ and a gap as a space, so a test reads like the formula on the screen.
 */
class InlineBreaksTest {
    private fun pieces(tex: String): List<String> {
        val toks = formulaTokens(tex)
        val cuts = inlineBreaks(toks).toSet()
        val out = ArrayList<String>()
        val cur = StringBuilder()
        toks.forEachIndexed { i, t ->
            cur.append(if (t.gap) " " else t.s.ifEmpty { "□" })
            if (i in cuts) { out += cur.toString(); cur.clear() }
        }
        out += cur.toString()
        return out
    }

    @Test fun setOfVectorsBreaksBetweenVectorsOnly() {
        // The formula of the screenshot (sova 2025, problem 3).
        assertEquals(
            listOf("{(0,0,0,0),", "(1,0,0,0),", "(0,1,0,0),", "(1,1,0,0),", "(0,0,1,0)}"),
            pieces("""\{(0,0,0,0),(1,0,0,0),(0,1,0,0),(1,1,0,0),(0,0,1,0)\}"""),
        )
    }

    @Test fun aVectorNeverBreaks() {
        assertEquals(listOf("(1,2,0,4)"), pieces("(1,2,0,4)"))
        assertEquals(listOf("(□,□,□)"), pieces("""(\mathbf{x},\mathbf{y},\mathbf{z})"""))
    }

    @Test fun listOfTuplesBreaksAfterItsOwnCommas() {
        assertEquals(listOf("((1,2,0,4),", "(3,3,0,1),", "□)"), pieces("""((1,2,0,4),(3,3,0,1),\mathbf{y})"""))
    }

    @Test fun spanBreaksAfterEqualsAndBetweenGenerators() {
        assertEquals(
            listOf("M=", "⟨(1,4,0,1),", "(1,1,3,1),", "(3,4,3,3)⟩"),
            pieces("""M=\langle(1,4,0,1),(1,1,3,1),(3,4,3,3)\rangle"""),
        )
    }

    @Test fun breaksBeforeMembershipAfterOtherRelations() {
        assertEquals(listOf("□,", "□,", "□", "∈□"), pieces("""\mathbf{x},\mathbf{y},\mathbf{z}\in\mathbb{R}^n"""))
        assertEquals(listOf("(2,−1,a)", "∈□"), pieces("""(2,-1,a)\in\mathbb{R}^3"""))
        assertEquals(listOf("□", "∉M"), pieces("""\mathbf{x}\notin M"""))
        assertEquals(listOf("P⊂", "T"), pieces("""P\subset T"""))
    }

    @Test fun breaksAfterOperationsButNeverAfterASign() {
        assertEquals(listOf("x=", "−1+", "y"), pieces("x=-1+y"))
        assertEquals(listOf("a+", "b−", "c"), pieces("a+b-c"))
    }

    @Test fun fractionsFencesMatricesAndGroupsStayWhole() {
        assertEquals(listOf("□+", "1"), pieces("""\frac{a+b}{c}+1"""))
        // a \\left( \\right) pair is opened for breaking but keeps the rules of a plain bracket
        assertEquals(listOf("(a+b,", "c)+", "c"), pieces("""\left(a+b,c\right)+c"""))
        assertEquals(listOf("A=", "□"), pieces("""A=\begin{pmatrix}1&2\\3&4\end{pmatrix}"""))
        assertEquals(listOf("□"), pieces("""{a+b=c}"""))
    }

    @Test fun setBuilderBreaksAtItsBarAndBeforeMembership() {
        val ps = pieces("""T^n=\big\{(x_1,\dots,x_n)\mid x_i\in T\big\}""")
        assertEquals("□=", ps[0])
        // the n-tuple inside stays whole
        assertTrue(ps.any { it.startsWith("{(□,…,□)") })
        assertTrue(ps.any { it.startsWith("∈") })
    }

    @Test fun aTupleOfExpressionsBreaksAfterItsCommasOnly() {
        assertEquals(
            listOf("(α+4β+3γ, ", "2α+2β+4γ)=", "(0,0,0)"),
            pieces("""(\alpha+4\beta+3\gamma,\ 2\alpha+2\beta+4\gamma)=(0,0,0)"""),
        )
        // a vector with a parameter inside a list of vectors stays whole
        assertEquals(listOf("((0,2,1,2),", "(2,−1,a+1,1))"), pieces("((0,2,1,2),(2,-1,a+1,1))"))
    }

    @Test fun quantifiersBreakBetweenTheirBracketsAndInsideASizedBody() {
        assertEquals(
            listOf("(∃k∈□)", "(□≠", "0∧", "x=", "θ)"),
            pieces("""(\exists k\in\hat{m})\Big(\alpha_k\neq 0\wedge x=\theta\Big)"""),
        )
    }

    @Test fun aSpanOrSetWrittenWithLeftRightBreaksBetweenItsItemsButNotInsideAVector() {
        // LA1 kapitola 4: a solution set, the vectors written with \left( \right)
        assertEquals(
            listOf("S=", "(−3,2,0)+", "⟨(−□,1),", "(□,0)⟩"),
            pieces("""S=(-3,2,0)+\left\langle\left(-\tfrac12,1\right),\left(\tfrac74,0\right)\right\rangle"""),
        )
        // DML: a set builder written with \left\{ ... \Big| ... \right\}
        val ps = pieces("""\left\{x\bmod m\ \Big|\ k\in\{0,1\}\right\}""")
        assertTrue(ps.toString(), ps.size >= 3 && ps.any { it.startsWith("∈") })
    }

    @Test fun aBreakFollowsTheGapNotPrecedesIt() {
        assertEquals(listOf("a, ", "b"), pieces("""a,\ b"""))
    }

    @Test fun piecesPackGreedilyAndAPieceWiderThanALineGetsItsOwn() {
        assertEquals(listOf(listOf(0, 1), listOf(2), listOf(3, 4)), packLines(listOf(3f, 3f, 5f, 2f, 2f), 6f))
        // the first line has less room: the text before the formula took some
        assertEquals(listOf(listOf(0), listOf(1, 2)), packLines(listOf(3f, 3f, 3f), 6f, first = 4f))
        // nothing fits on what is left of the first line: start on the next one
        assertEquals(listOf(listOf(0, 1)), packLines(listOf(3f, 3f), 6f, first = 1f))
        assertEquals(listOf(listOf(0), listOf(1)), packLines(listOf(9f, 1f), 6f))
    }

    @Test fun shrinkingIsTheLastResortAndOnlyToTheLine() {
        assertEquals(1f, fitScale(5f, 6f))
        assertEquals(1f, fitScale(6f, 6f))
        assertEquals(.5f, fitScale(12f, 6f))
    }

    @Test fun aSumBreaksOnlyWhereItsSideOfTheRelationIsWiderThanALine() {
        val toks = formulaTokens("a+b=c+d")
        val kinds = inlineBreakKinds(toks)
        // after +: weak; after =: strong
        assertEquals(listOf(true, false, true), kinds.toSortedMap().values.toList())
        // pieces "a+" "b=" "c+" "d", each 1 wide: a line of 2 keeps each side whole
        val glue = { s: List<String> -> s.joinToString("") }
        assertEquals(listOf("a+b=", "c+d"), joinWeakPieces(listOf("a+", "b=", "c+", "d"), listOf(true, false, true), 2.5f, { it.length / 2f }, glue))
        // a line narrower than a side: that side keeps its weak breaks, the other is joined
        assertEquals(listOf("a+", "b=", "c+d"), joinWeakPieces(listOf("a+", "b=", "c+", "d"), listOf(true, false, true), 1.6f, { if (it == "d") .5f else 1f }, glue))
        // a product of brackets is weak, the brackets of quantifiers strong
        assertEquals(listOf(true), inlineBreakKinds(formulaTokens("(n+1)(n+2)")).values.filter { true }.takeLast(1))
        assertEquals(false, inlineBreakKinds(formulaTokens("""(\forall x)(\exists y)""")).values.last())
        // also the body after a quantifier's group: (∀x, y ∈ P) | (x + y ∈ P), never inside the group first
        val q = formulaTokens("""(\forall x,y\in P)(x+y\in P)""")
        assertEquals(false, inlineBreakKinds(q)[q.indexOfFirst { it.s == ")" }])
        // no weak break at all: unchanged
        assertEquals(listOf("x=", "y"), joinWeakPieces(listOf("x=", "y"), listOf(false), 9f, { 1f }, glue))
    }

    /** Teo, 18. 11.: a set stays whole while it fits; too wide, it wraps at its bar and relations before its sums. */
    @Test fun aBracketWrapsAtItsRelationsBeforeItsSums() {
        val levels = inlineBreakLevels(formulaTokens("""A=\{x\in X\mid x+y=0\}""")).toSortedMap().values.toList()
        assertEquals("top-level = first, then inside: before ∈, the bar, + (weak), =", listOf(1, 3, 2, 4, 3), levels)
        val glue = { s: List<String> -> s.joinToString("") }
        // "{(x,y)∈ℝ²|" 6, "x+" 2, "2y=0}" 3 on a line of 8: the sum stays together, the cut is at the bar
        val w = mapOf("{(x,y)∈ℝ²|" to 6f, "x+" to 2f, "2y=0}" to 3f)
        assertEquals(listOf("{(x,y)∈ℝ²|", "x+2y=0}"), joinLevelPieces(listOf("{(x,y)∈ℝ²|", "x+", "2y=0}"), listOf(1, 2), 8f, { w.getValue(it) }, glue))
        // the whole set fits a wider line: one piece
        assertEquals(listOf("{(x,y)∈ℝ²|x+2y=0}"), joinLevelPieces(listOf("{(x,y)∈ℝ²|", "x+", "2y=0}"), listOf(1, 2), 12f, { w.getValue(it) }, glue))
        // a top-level cut always stays
        assertEquals(listOf("A=", "{x}"), joinLevelPieces(listOf("A=", "{x}"), listOf(0), 99f, { 1f }, glue))
    }

    /** Final LLM check, 18. 11.: "x" alone before "∈ A ∨ …", "(f(x) =" before "f(y) ⇒ x = y)", "f(m,n) =" at a line's end. */
    @Test fun aFormulaBreaksAtItsLogicBeforeItsRelations() {
        fun levels(tex: String) = inlineBreakLevels(formulaTokens(tex)).toSortedMap().values.toList()
        // before ∈ (a relation) 1, after ∨ 0
        assertEquals(listOf(1, 0), levels("""x\in A\vee\neg(x\in B)"""))
        // the comma inside the quantifier 2, between the quantifier and its body 0, inside the body ⇒ 2, = 3
        assertEquals(listOf(2, 0, 3, 2, 3), levels("""(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)"""))
        // a top-level = is cut only when the whole does not fit; the comma between two formulas always
        assertEquals(listOf(1, 2), levels("f(m,n)=(m,m+2n)"))
        assertEquals(listOf(1, 0, 1, 4), levels("""f:\mathbb{Z}\to\mathbb{Z},\quad f(m)=-m+1"""))
    }

    /**
     * Large text (modBreak): the break before a modulus comes first among the top level's relations ("665 ≡ −1 /
     * (mod 666)", inspector, 18. 11.), and every other level keeps its order, so a bracket that fits still stays whole
     * (review of the merge, 18. 11.: relations had joined the commas inside brackets).
     */
    @Test fun largeTextBreaksBeforeTheModulusAndKeepsTheOrder() {
        fun levels(tex: String) = inlineBreakLevels(formulaTokens(tex), modBreak = true).toSortedMap().values.toList()
        assertEquals("before (mod 666) first, then after ≡", listOf(2, 1), levels("""665\equiv -1\pmod{666}"""))
        assertEquals("the set's order as without it, one level down", listOf(2, 4, 3, 5, 4), levels("""A=\{x\in X\mid x+y=0\}"""))
        assertEquals(listOf(2, 3), levels("f(m,n)=(m,m+2n)"))
    }

    /** Nácvik DML, 18. 11.: a line of a chain that starts with its sign keeps it with what follows. */
    @Test fun aLeadingSignNeverEndsALineAlone() {
        assertTrue(inlineBreaks(formulaTokens("""=\begin{pmatrix}1&2\\3&4\end{pmatrix}""")).isEmpty())
        // "=a+b=c": no break after the first "=", the one after the second stays
        val bs = inlineBreaks(formulaTokens("=a+b=c"))
        assertTrue("no break right after the leading =: $bs", 0 !in bs)
        assertTrue("the second = still breaks: $bs", bs.isNotEmpty())
    }
}

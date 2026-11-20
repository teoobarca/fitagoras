package dev.hamper.tabula.math

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayLinesTest {
    private fun lines(tex: String, width: Float) = displayLines(tex, width) { it.length.toFloat() }

    @Test fun softBreaksPackLowestLevelFirstAndIndentTheirContinuations() {
        // one unit per drawn character: the breaks and spaces draw nothing
        val tex = """AAAA\softbreak BB\softbreak[1]CC\softbreak DD"""
        fun soft(w: Float) = softLines(tex, w, 1f) { it.replace(Regex("""\\softbreak(\[\d])?|\s|\{}"""), "").length.toFloat() }.map { it.tex.removePrefix("{}").trim() to it.indent }
        assertEquals(listOf("AAAA BBCC DD" to 0), soft(10f))
        // breaks of level 0 first, greedily; the one of level 1 stays unused while BBCC fits
        assertEquals(listOf("AAAA" to 0, "BBCC" to 0, "DD" to 0), soft(4f))
        assertEquals(listOf("AAAA" to 0, "BB" to 0, "CC" to 1, "DD" to 0), soft(3f))
    }

    @Test fun preservesFittingSourceExactly() {
        val tex = """a,\quad b,\qquad c,\ d"""
        assertEquals(listOf(tex), lines(tex, tex.length.toFloat()))
    }

    @Test fun wrapsAllSupportedSeparatorsAndKeepsCommas() {
        assertEquals(listOf("a,", "b,", "c,", "d"), lines("""a,\quad b,\qquad c,\ d""", 2f))
    }

    @Test fun greedilyPreservesSpacingWithinEachLine() {
        assertEquals(listOf("""a,\ b,""", "c"), lines("""a,\ b,\ c""", 6f))
    }

    @Test fun neverBreaksNestedSeparators() {
        for (nested in listOf(
            """{a,\quad b}""", """(a,\quad b)""", """[a,\quad b]""",
            """\{a,\quad b\}""", """\langle a,\quad b\rangle""",
            """\left.a,\quad b\right)""", """\left\{a,\quad b\right\}""",
            """\begin{matrix}a,\quad b\end{matrix}""",
        )) {
            assertEquals(listOf("$nested,", "c"), lines("$nested,\\quad c", 1f))
        }
    }

    @Test fun leavesUnbreakableSourceAndOtherOperatorsAlone() {
        for (tex in listOf("a+b,c+d", """a,\qquadruple b""", """a\,b""")) {
            assertEquals(listOf(tex), lines(tex, 1f))
        }
    }

    // Copied verbatim from La1Concepts.kt, concept "tn", after "Množinu §n§-tic".
    private val cartesianPower = """T^n=\big\{(x_1,\dots,x_n)\mid x_i\in T\ \text{pro každé}\ i\in\hat{n}\big\}"""

    @Test fun realCartesianPowerWrapsAtFullSizeWithCardWidthAndIndent() {
        // Recorded using MathEngine.layout at 108 px, app face scale .97 (desktop font adapter).
        // Widths are injected because Android Paint cannot run in a local JVM test.
        val widths = mapOf(
            cartesianPower to 1790.1133f,
            cartesianPower.substringBefore("\\mid") to 675.7457f,
            cartesianPower.substring(cartesianPower.indexOf("\\mid")) to 1088.1776f,
            """\mid x_i\in T\ """ to 332.86526f,
            """\mid x_i\in T\ \text{pro každé}""" to 802.6173f,
            """\mid x_i\in T\ \text{pro každé}\ i""" to 857.928f,
            """\in\hat{n}\big\}""" to 204.05962f,
        )
        // Exercise both sides of the old 62% gate with the same layout constraints.
        for (widthFactor in listOf(1f, .9f)) {
            val measure: (String) -> Float = { source ->
                requireNotNull(widths[source]) { "Unmeasured fragment: $source" } * widthFactor
            }
            val result = displayLines(cartesianPower, 1064f, 162f, measure)
            assertEquals(listOf(
                cartesianPower.substringBefore("\\mid"),
                """\mid x_i\in T\ \text{pro každé}\ i""",
                """\in\hat{n}\big\}""",
            ), result)
            assertEquals(cartesianPower, result.joinToString(""))
            result.forEachIndexed { index, line ->
                assertTrue("Must fit at full size: $line", measure(line) <= if (index == 0) 1064f else 902f)
            }
        }
        assertEquals(listOf(cartesianPower), displayLines(cartesianPower, 2000f, 162f) { widths.getValue(it) })
    }

    @Test fun sizedSetBracesWrapAndFittingSourcesStayExact() {
        for ((open, close) in listOf(
            """\{""" to """\}""", """\big\{""" to """\big\}""",
            """\Big\{""" to """\Big\}""", """\bigl\{""" to """\bigr\}""",
            """\left\{""" to """\right\}""",
        )) {
            val tex = "${open}aaaaaaaa\\mid b\\in c$close"
            assertEquals(listOf(tex), lines(tex, 100f))
            val result = lines(tex, 20f)
            assertEquals(2, result.size)
            assertTrue(result[1].startsWith("\\mid"))
            if (open == "\\left\\{" || open == "\\bigl\\{") {
                assertEquals(listOf("""\big\{aaaaaaaa""", """\mid b\in c\big\}"""), result)
            } else assertEquals(tex, result.joinToString(""))
        }
    }

    @Test fun cartesianPowerWrapsIntoBalancedLinesAtNarrowWidth() {
        val tex = """T^n=\{(x_1,\dots,x_n)\mid x_i\in T \text{ pro každé } i\in\hat{n}\}"""
        val result = displayLines(tex, 35f, continuationIndent = 3f) { it.length.toFloat() }
        assertEquals(result, displayLines(tex, 70f, continuationIndent = 6f) { it.length * 2f })
        assertTrue(result.size >= 3)
        assertEquals(tex, result.joinToString(""))
        assertTrue(result.any { it.startsWith("\\mid") })
        result.forEachIndexed { index, line ->
            assertTrue("Overflow: $line", line.length <= if (index == 0) 35 else 32)
        }
        for (line in result) {
            // Visible set braces span lines; TeX argument groups and tuples stay balanced.
            val arguments = line.replace("\\{", "").replace("\\}", "")
            for ((open, close) in listOf('{' to '}', '(' to ')')) {
                var depth = 0
                for (char in arguments) {
                    if (char == open) depth++
                    if (char == close) depth--
                    assertTrue("Unbalanced line: $line", depth >= 0)
                }
                assertEquals("Unbalanced line: $line", 0, depth)
            }
        }
    }

    @Test fun oversizedSetBreaksBeforeMidBeforeTryingOtherBoundaries() {
        val tex = """\{aaaaaaaa\mid b\in c\}"""
        assertEquals(listOf("""\{aaaaaaaa""", """\mid b\in c\}"""), lines(tex, 13f))
        assertEquals(tex, lines(tex, 6f).joinToString(""))
        assertTrue(lines(tex, 6f).any { it.startsWith("\\in") })
    }

    @Test fun oversizedSetCanBreakAfterWholeText() {
        val tex = """\{aa\mid b\text{xyz} ccccc\}"""
        val result = lines(tex, 16f)
        assertEquals(listOf("""\{aa""", """\mid b\text{xyz}""", """ ccccc\}"""), result)
        assertEquals(tex, result.joinToString(""))
    }

    @Test fun oversizedSetCanBreakBeforeWholeText() {
        val tex = """\{a\mid bbbbb\text{xyz} c\}"""
        val result = lines(tex, 14f)
        assertEquals(listOf("""\{a""", """\mid bbbbb""", """\text{xyz} c\}"""), result)
        assertEquals(tex, result.joinToString(""))
    }

    @Test fun setRelationsStayWholeAndBreakBeforeTheRelation() {
        for (relation in listOf("=", ":=", "<=", ">=", "∈", """\in""", """\subseteq""")) {
            val tex = "\\{a\\mid b${relation} cccc\\}"
            val result = lines(tex, 1f)
            assertEquals(tex, result.joinToString(""))
            assertEquals(listOf("\\{a", "\\mid b", "${relation} cccc\\}"), result)
        }
    }

    @Test fun setFallbackProtectsNestedContent() {
        for (nested in listOf(
            """(a\mid b=c)""", """[a\mid b=c]""", """{a\mid b=c}""",
            """\left(a\mid b=c\right)""", """\text{a(\mid b=c}""",
            """\{a\mid b=c\}""", """\langle a\mid b=c\rangle""",
            """\begin{matrix}a\mid b=c\end{matrix}""",
            """\big\{a\mid b=c\big\}""", """\Big\{a\mid b=c\Big\}""",
            """\bigl\{a\mid b=c\bigr\}""", """\left\{a\mid b=c\right\}""",
        )) {
            val tex = "\\{$nested\\}"
            assertEquals(listOf(tex), lines(tex, 1f))
            val withMid = "\\{a\\mid $nested\\}"
            val result = lines(withMid, 1f)
            assertEquals(withMid, result.joinToString(""))
            assertTrue("Split protected content: $result", result.any { it.contains(nested) })
        }
    }

    @Test fun secondaryBreaksOnlyWhenMinimumScaleCannotFit() {
        val tex = "a=b=c"
        assertEquals(listOf(tex), lines(tex, tex.length * displayMinScale))
        assertEquals(listOf("a", "=b", "=c"), lines(tex, 2f))
    }

    @Test fun secondaryBreaksOversizedCommaPieceOnly() {
        assertEquals(listOf("a,", "b", "=c", "=d"), lines("""a,\quad b=c=d""", 2f))
        assertEquals(listOf("a=b,", "c"), lines("""a=b,\quad c""", 3f))
    }

    @Test fun breaksBeforeBarsAndBeforeWholeRelations() {
        for (bar in listOf("|", """\mid """)) {
            assertEquals(listOf("abc", "${bar}def"), lines("abc${bar}def", 3f))
        }
        for (relation in listOf("=", ":=", "<=", ">=", """\in""", """\subseteq""", """\le""")) {
            assertEquals(listOf("a", "$relation b"), lines("a$relation b", 1f))
        }
    }

    /** Nácvik DML, 18. 11.: a chain on screen goes "A = B" / "≝ C" / "= D", never with a lone "=" above its matrix. */
    @Test fun aChainBreaksBeforeItsRelationsAndKeepsEachMatrixWithItsSign() {
        val m = """\begin{pmatrix}1&2\\3&4\end{pmatrix}"""
        assertEquals(listOf("A^{n+1}=A^n\\cdot A", "\\stackrel{IP}{=}$m$m"), lines("A^{n+1}=A^n\\cdot A\\stackrel{IP}{=}$m$m", 70f))
        // a negated relation stays whole: the break goes before \not
        assertEquals(listOf("a", "\\not\\equiv b"), lines("a\\not\\equiv b", 1f))
        assertEquals(listOf("a", "\\not= b"), lines("a\\not= b", 1f))
        // a line that starts with its sign does not lose it: there is nothing before it to break off
        assertEquals(listOf("=$m"), lines("=$m", 10f))
    }

    @Test fun relationsNeverBreakInsideProtectedGroups() {
        for (tex in listOf(
            "{a=b}", "(a=b)", "[a=b]", """\{a\in b\}""",
            """\left.a=b\right)""", """\left\{a=b\right\}""",
            """\text{a=b | c}""", """\langle a=b\rangle""",
            """\begin{matrix}a=b\end{matrix}""", """a\infty""",
        )) assertEquals(listOf(tex), lines(tex, 1f))
    }

    @Test fun continuationIndentIsIncludedWhenChoosingBreaks() {
        assertEquals(
            listOf("a", "=b", "=c"),
            displayLines("a=b=c", 3f, continuationIndent = 1f) { it.length.toFloat() * 2f },
        )
    }

    @Test fun vectorOperationsWrapAtBothFontScales() {
        val first = """\mathbf{x}+\mathbf{y}:=(x_1+y_1,\dots,x_n+y_n),"""
        val second = """\alpha\cdot\mathbf{x}:=(\alpha x_1,\dots,\alpha x_n)"""
        val tex = "$first\\quad $second"
        // Inject widths: production supplies font measurements, without Android in this test.
        for (scale in listOf(1f, 2f)) {
            val result = displayLines(tex, 1280f) {
                when (it) {
                    first -> 700f * scale
                    second -> 600f * scale
                    else -> 1360f * scale
                }
            }
            assertEquals(listOf(first, second), result)
        }
    }
}

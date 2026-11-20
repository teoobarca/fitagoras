package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Teo (16. 11., príčina C): a worked example may not carry matrices or tables typed by hand next to the engine that
 * computes them (a GEM written row by row, a REA table, a product of matrices): they drift from what the engine and
 * the generator show, and a typo stays. In the body of every `worked(...)` the matrices and tables come from the
 * engine: a walk (Field.walk, GemWalk.of, euclidWalk, reaWalk) or `Mat.tex()` of a computed matrix. The problem text
 * (the second argument) may show its matrix, as the skriptá do. docs/CONTENT.md, "Vzorový príklad počíta engine".
 */
class WorkedFromEngineTest {
    private val matrixEnv = Regex("""\\begin\{((p|b|v)?matrix|array)\}(\|\d+)?(\{[^}]*\})?(.*?)\\end\{\1\}""", RegexOption.DOT_MATCHES_ALL)
    private val euclidStep = Regex("""\d+=\d+\\cdot ?\d+\+\d+""")

    /**
     * A typed matrix of numbers (a symbolic one, A^n with n in it, is reasoning and may be typed), or a typed chain of
     * Euclid's divisions (euclidWalk shows them).
     */
    private fun typed(s: String): String? {
        for (m in matrixEnv.findAll(s)) {
            val cells = m.groupValues[5].replace(Regex("""\\[td]?frac"""), "")
            if (cells.none { it.isLetter() }) return "matrix ${m.value.take(60)}"
        }
        if (euclidStep.findAll(s).count() >= 2) return "Euclid chain ${s.take(60)}"
        return null
    }

    @Test fun workedExamplesComputeTheirMatricesAndTables() {
        val bad = ArrayList<String>()
        var bodies = 0
        val files = mainSources().walkTopDown().filter { it.extension == "kt" && "/data/" in it.path }.associateWith { it.readText() }
        fun scan(where: String, body: String) { for (s in KotlinStrings(body).all()) typed(s)?.let { bad += "$where: $it" } }
        for ((f, src) in files) {
            for (call in Regex("""\bworked\(""").findAll(src)) {
                bodies++
                val line = src.substring(0, call.range.first).count { it == '\n' } + 1
                val body = bodyOf(src, call.range.last + 1)
                if (body != null) { scan("${f.name}:$line", body); continue }
                // a solution passed by value (a val or a function defined elsewhere): scan its definition
                val name = lastArgName(src, call.range.last + 1) ?: continue
                val def = files.entries.firstNotNullOfOrNull { (g, gs) -> Regex("""\b(val|fun)\s+$name\b""").find(gs)?.let { g to (gs to it.range.last) } } ?: continue
                val (g, at) = def
                val brace = at.second.let { i -> at.first.indexOf('{', i) }.takeIf { it >= 0 } ?: continue
                blockFrom(at.first, brace)?.let { scan("${g.name} ($name, for ${f.name}:$line)", it) }
            }
        }
        assertTrue("worked examples found: $bodies", bodies > 30)
        assertTrue("Matrices, tables or Euclid chains typed by hand in worked examples (compute them: docs/CONTENT.md):\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test fun theGuardSeesWhatItMustSee() {
        assertTrue(typed("A^2=\\begin{pmatrix}1&-6\\\\0&1\\end{pmatrix}") != null)
        assertTrue(typed("\\begin{pmatrix}1&\\tfrac{1}{3}\\end{pmatrix}") != null)
        assertTrue(typed("A^n=\\begin{pmatrix}(-1)^n&3n\\\\0&1\\end{pmatrix}") == null)
        assertTrue(typed("158=1\\cdot 96+62,\\quad 96=1\\cdot 62+34") != null)
        assertTrue(typed("254=1\\cdot 158+96") == null)
    }

    /** The examples converted on 17. 11. still show the skriptá's numbers, now computed. */
    @Test fun convertedExamplesKeepTheirNumbers() {
        fun texts(title: String) = AllLessons.flatMap { it.steps }.filterIsInstance<Step.Worked>().first { it.title.startsWith(title) }
            .solution.steps.flatMap { it.math }.joinToString(" ") { it.tex }
        fun m(vararg r: List<Int>, bar: Int? = null) = dev.hamper.tabula.la.intMat(*r, bar = bar).tex()
        val expect = mapOf(
            "Inverzia v" to m(listOf(2, 0, 2), listOf(0, 3, 5), listOf(3, 6, 2)),
            "Rovina cez tri body" to m(listOf(-3, 1, 2, 0), listOf(0, -1, 0, 1)),
            "Matica, ktorá kombinuje" to m(listOf(1, 1), listOf(0, 0), listOf(2, 0)),
            "Sústava so zadanými" to m(listOf(1, 1, 0, 0, 0), listOf(1, 0, 0, 2, 1), bar = 4),
        )
        for ((title, tex) in expect) assertTrue("$title: $tex", tex in texts(title))
        val p = texts("Matica, ktorá robí GEM")
        assertTrue(p, "=\\begin{pmatrix}0&1\\\\\\tfrac{1}{3}&-1\\end{pmatrix}" in p)
    }

    /** A comment or a `'}'` in a worked body must not end it early (a typed matrix after it would go unseen). */
    @Test fun bracesInCommentsAndCharactersDoNotEndTheBody() {
        val src = "worked(\"t\", \"p\") {\n // }\n /* } */ val c = '}'\n eq(\"X\")\n}\n"
        val body = bodyOf(src, src.indexOf('(') + 1)!!
        assertTrue(body, body.contains("eq(\"X\")"))
    }

    /** The `{ ... }` block after the call's arguments, or null when the call has none (a solution from a function). */
    internal fun bodyOf(src: String, open: Int): String? {
        var i = open; var depth = 1
        while (i < src.length && depth > 0) {
            val skip = skipNonCode(src, i); if (skip != i) { i = skip; continue }
            when (src[i]) { '(' -> depth++; ')' -> depth-- }; i++
        }
        while (i < src.length && src[i].isWhitespace()) i++
        if (i >= src.length || src[i] != '{') return null
        val start = i; depth = 0
        while (i < src.length) {
            val skip = skipNonCode(src, i); if (skip != i) { i = skip; continue }
            when (src[i]) { '{' -> depth++; '}' -> { depth--; if (depth == 0) return src.substring(start, i + 1) } }
            i++
        }
        return null
    }

    /** The name the last argument of the call starts with (`maticaESolution`, `marastSolution()`), or null. */
    private fun lastArgName(src: String, open: Int): String? {
        var i = open; var depth = 1; var lastStart = open
        while (i < src.length && depth > 0) {
            val skip = skipNonCode(src, i); if (skip != i) { i = skip; continue }
            when (src[i]) { '(', '[', '{' -> depth++; ')', ']', '}' -> depth--; ',' -> if (depth == 1) lastStart = i + 1 }
            i++
        }
        return Regex("""^\s*([A-Za-z_][A-Za-z0-9_]*)""").find(src.substring(lastStart, i - 1))?.groupValues?.get(1)
    }

    /** The `{ ... }` block starting at [brace]. */
    private fun blockFrom(src: String, brace: Int): String? {
        var i = brace; var depth = 0
        while (i < src.length) {
            val skip = skipNonCode(src, i); if (skip != i) { i = skip; continue }
            when (src[i]) { '{' -> depth++; '}' -> { depth--; if (depth == 0) return src.substring(brace, i + 1) } }
            i++
        }
        return null
    }

    /** Index after a string, a character literal or a comment starting at [i]; [i] itself when none starts there. */
    private fun skipNonCode(src: String, i: Int): Int = when {
        src[i] == '"' -> skipString(src, i)
        src.startsWith("//", i) -> src.indexOf('\n', i).let { if (it < 0) src.length else it + 1 }
        src.startsWith("/*", i) -> src.indexOf("*/", i + 2).let { if (it < 0) src.length else it + 2 }
        src[i] == '\'' -> { var j = i + 1; while (j < src.length && src[j] != '\'') { if (src[j] == '\\') j++; j++ }; j + 1 }
        else -> i
    }

    /** Index after the string literal starting at [i] (raw or plain); braces inside strings do not count. */
    private fun skipString(src: String, i: Int): Int {
        if (src.startsWith("\"\"\"", i)) { val e = src.indexOf("\"\"\"", i + 3); return if (e < 0) src.length else e + 3 }
        var j = i + 1
        while (j < src.length && src[j] != '"') { if (src[j] == '\\') j++; j++ }
        return j + 1
    }
}

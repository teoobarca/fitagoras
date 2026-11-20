package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Generated math reads as a person writes it: every generator, many seeds, every text it can show (question, options,
 * feedback, diagnoses, hints, method, solution). Coefficients and signs go through data/Terms.kt; this scan fails if
 * "1x", "-1x", "0x", "+ -", "- -", "x^1", "1(…)" or "·1" come back, or if math stands outside $…$.
 * Every solution and worked example follows its Postup (data/Postup.kt).
 */
class MathStyleTest {

    companion object {
        // a "1" right after "{" (a numerator, a group) is a coefficient too: \frac{1(x+1)}{2}; after _ or ^ it is an index
        private val notName = """(?<![\p{L}\d.,}_^\\]|[_^]\{)"""
        private val letterAfter = """(?=\s*(?:[a-zA-Z(]|\\(?:mathbf|alpha|beta|gamma|delta|lambda|ell|varphi)|\\(?:big|Big|left)\s*[(\[]))"""
        /** Pattern, what it is. Applied to TeX only. */
        val rules: List<Pair<Regex, String>> = listOf(
            Regex("${notName}1$letterAfter") to "coefficient 1 written out (1x, 1(…))",
            Regex("${notName}0$letterAfter") to "coefficient 0 written out (0x)",
            Regex("""\+\s*-""") to "plus minus (+ -)",
            Regex("""-\s*-""") to "minus minus (- -)",
            Regex("""-\s*\+""") to "minus plus (- +)",
            Regex("""\+\s*\+""") to "plus plus",
            Regex("""(?:[\da-zA-Z}\)])\^\s*(?:1(?![\d])|\{\s*1\s*\})""") to "power 1 (x^1)",
            // numbers times numbers are arithmetic shown on paper (1·4+3·4); a letter or a vector times 1 is not
            Regex("""(?:[a-zA-Z}\)])\s*\\cdot\s*1(?![\d.,/])""") to "times 1 (·1)",
            Regex("""(?<![\d.,])1\s*\\cdot\s*(?=[a-zA-Z(\\])""") to "1 times something (1·x)",
            // a zero term inside a sum; "0+0=0" and the last line of the REA "4=2\cdot 2+0" are fine
            Regex("""(?<=[a-zA-Z}])\s*[+-]\s*0(?![\d.,]|\s*\\cdot)|(?:^|[=(,])\s*0\s*\+\s*(?=[a-zA-Z\\])""") to "a zero term (x+0, 0+x)",
        )
        /** What may not stand in plain text: TeX commands, sub- and superscripts, relations, a number glued to a letter. */
        private val outside = Regex("""[\\^_=<>≤≥≠≡∈∉⊆⊂∪∩·×]|(?<![\p{L}\d])\d+[a-zA-Z](?![\p{L}])""")

        /** The TeX parts of a text ($…$ and $$…$$), with markup that is not math removed. */
        fun mathParts(s0: String): Pair<List<String>, List<String>> {
            val s = s0.replace('§', '$').replace("$$", "$")
            val pieces = s.split('$')
            return pieces.filterIndexed { i, _ -> i % 2 == 1 } to pieces.filterIndexed { i, _ -> i % 2 == 0 }
        }

        /** What breaks the style in one shown text. */
        fun problems(sh: Seen): List<String> {
            val (math, plain) = if (sh.math) listOf(sh.text) to emptyList() else mathParts(sh.text)
            val out = ArrayList<String>()
            for (m0 in math) {
                // an identity like A^1=A states the base case of a power; it is not a coefficient
                // a negative number in parentheses is a number: (-2)\cdot 1 is arithmetic like 2\cdot 1
                val m = m0.replace(Regex("""\\tap\{\d+\}"""), "").replace(Regex("""\\text\{ř\}\d+"""), "R").replace(Regex("""\\text\{[^}]*\}"""), " ").replace(Regex("""\(-\d+\)"""), "7")
                    // a space of 1-tuples (\mathbb{Z}_2^1, T^1) is notation, not a power
                    .replace(Regex("""(\\mathbb\{[A-Z]\}(_\{?\d+\}?)?|T)\^\{?1\}?"""), "$1")
                for ((re, what) in rules) re.find(m)?.let { hit ->
                    if (what.startsWith("power 1") && Regex("""([A-Za-z])\^\{?1\}?=\1(?![\w^])""").containsMatchIn(m)) return@let
                    // likewise a·1 = a and 1·a = a state that 1 is neutral (the k = 1 of a | a, 1 | z), not a coefficient
                    if ((what.startsWith("times 1") || what.startsWith("1 times")) && Regex("""(?<![\w}])([A-Za-z])\s*\\cdot\s*1=\1(?![\w^])|(?<![\w.,])1\s*\\cdot\s*([A-Za-z])=\2(?![\w^])""").containsMatchIn(m)) return@let
                    out += "$what: …${m.substring(maxOf(0, hit.range.first - 20), minOf(m.length, hit.range.last + 20))}…"
                }
            }
            for (p in plain) outside.find(p.replace("**", "").replace("==", ""))?.let { out += "math outside \$…\$: “${it.value}” in …${p.take(80)}…" }
            return out
        }
    }

    @Test fun generated_math_reads_as_written_by_hand() {
        val bad = ArrayList<String>()
        for ((_, texts) in allGeneratedTexts(60)) for (t in texts) problems(t).forEach { bad += "${t.where}: $it" }
        // the whole list for whoever fixes it: build/reports/mathstyle.txt
        java.io.File("build/reports").apply { mkdirs() }.resolve("mathstyle.txt").writeText(bad.distinct().joinToString("\n"))
        assertTrue("${bad.size} style problems, first 40:\n" + bad.distinct().take(40).joinToString("\n"), bad.isEmpty())
    }

    @Test fun lesson_worked_examples_read_as_written_by_hand() {
        val bad = ArrayList<String>()
        for (s in Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>())
            for (t in solutionTexts(s.id, s.solution)) problems(t).forEach { bad += "${t.where}: $it" }
        assertTrue("${bad.size} style problems:\n" + bad.take(40).joinToString("\n"), bad.isEmpty())
    }

    /** Every solution follows its method: steps under its subgoals, in order, from the first to the last. */
    @Test fun every_solution_follows_its_postup() {
        val bad = ArrayList<String>()
        for ((ne, _) in allGeneratedTexts(60)) {
            val (name, ex) = ne
            val answers = ArrayList<Any>()
            for (b in ex.parts) {
                when (val a = b(answers).answer) {
                    is Answer.Choice -> answers.add(a.correct)
                    is Answer.Numbers -> answers.add(a.expected)
                    is Answer.Pick -> { val n = a.options.size; answers.add((0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }) }
                    is Answer.Vectors -> answers.add(a.example)
                    is Answer.Paper -> answers.add(1)
                }
            }
            for (sol in listOf(ex.solution(answers), ex.solution(emptyList()))) {
                if (sol.postup !== ex.postup) bad += "$name: the solution follows another Postup than the exercise declares"
                sol.problems().forEach { bad += "$name: $it" }
            }
        }
        for (s in Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>()) s.solution.problems().forEach { bad += "${s.id}: $it" }
        for (c in Courses) for (p in c.papers + (0 until 8).map { c.generatedPaper(it) }) for (q in p.problems) q.solution?.problems()?.forEach { bad += "${p.id}/${q.n}: $it" }
        assertTrue(bad.distinct().take(40).joinToString("\n"), bad.isEmpty())
    }

    /**
     * The reason comes in words, the theorem after it (Teo, 15. 11.: "if I don't know the theorems by heart, how do I
     * justify it?"). A sentence of a step that cites a theorem, an observation or a definition must say why in its own
     * words before the citation: a causal clause (lebo, pretože, keďže, preto, teda, takže, ...) before the reference.
     * Definitions may be cited as they are: a step checks a definition, it does not lean on it.
     * Fails on "Hlavné stĺpce sú bázou (Veta 2.59)." and on "Podľa Vety 2.55 je to báza."; build/reports/why.txt lists all.
     */
    @Test fun a_reason_before_every_theorem() {
        val bad = ArrayList<String>()
        fun check(where: String, say: String) { reasonProblems(say).forEach { bad += "$where: $it" } }
        for ((_, texts) in allGeneratedTexts(20)) texts.filter { " step " in it.where && !it.math && !it.where.endsWith(" note") }.forEach { check(it.where, it.text) }
        for (s in Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>()) s.solution.steps.forEach { check(s.id, it.say) }
        java.io.File("build/reports").apply { mkdirs() }.resolve("why.txt").writeText(bad.distinct().joinToString("\n"))
        assertTrue("${bad.distinct().size} citations without a reason in words before them:\n" + bad.distinct().take(40).joinToString("\n"), bad.isEmpty())
    }

    @Test fun the_reason_check_itself() {
        assertEquals(1, reasonProblems("Dimenzia je počet vektorov bázy (Veta 2.57), teda počet hlavných stĺpcov: §2§.").size)
        assertEquals(1, reasonProblems("Podľa Vety 2.55 je to báza.").size)
        assertEquals(1, reasonProblems("Hlavné stĺpce sú 1., 2. Tvoria bázu obalu (Veta 2.59).").size)
        assertEquals(0, reasonProblems("Je to už báza, lebo vektor mimo obalu by dal dlhší LN súbor (Veta 2.55).").size)
        assertEquals(0, reasonProblems("Všetky tri podmienky Definície 2.7 platia, množina je podpriestor.").size)
    }

    /** A step's first sentence stands alone and is short (docs/CONTENT.md): at most 20 words. */
    @Test fun step_sentences_are_short() {
        val bad = ArrayList<String>()
        fun check(where: String, say: String) {
            val first = say.replace('§', '$').let { s -> firstSentenceOf(s) }
            val words = first.replace(Regex("""\$[^$]*\$"""), "X").split(Regex("""\s+""")).count { it.isNotBlank() }
            if (words > 20) bad += "$where ($words words): $first"
        }
        for ((ne, texts) in allGeneratedTexts(20)) texts.filter { " step " in it.where && !it.math && !it.where.endsWith(" note") }.forEach { check(it.where, it.text) }
        for (s in Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>()) s.solution.steps.forEach { check(s.id, it.say) }
        val postups = allGeneratedTexts(20).map { it.first.second.postup } + Courses.flatMap { it.lessons }.flatMap { it.steps }.filterIsInstance<Step.Worked>().map { it.solution.postup }
        for (p in postups.distinct()) p.aims.forEach { a -> check("postup ${a.title}", a.how) }
        java.io.File("build/reports").apply { mkdirs() }.resolve("long.txt").writeText(bad.distinct().joinToString("\n"))
        assertTrue("${bad.size} long first sentences:\n" + bad.distinct().take(40).joinToString("\n"), bad.isEmpty())
    }

    @Test fun the_formatter() {
        val f = dev.hamper.tabula.la.Frac
        fun q(n: Int) = f.of(n)
        assertTrue(Terms.lin(listOf<Int>(1, -1, 0, 2), listOf("x", "y", "z", "w")) == "x-y+2w")
        assertTrue(Terms.lin(listOf<Int>(-1, 1), listOf("x", "y")) == "-x+y")
        assertTrue(Terms.lin(listOf<Int>(0, 0), listOf("x", "y")) == "0")
        assertTrue(Terms.times(q(1), "x_1+x_2") == "x_1+x_2")
        assertTrue(Terms.times(q(3), "x_1+x_2") == "3(x_1+x_2)")
        assertTrue(Terms.times(q(-1), "x_1+x_2") == "-(x_1+x_2)")
        assertTrue(Terms.times(q(2), "7") == "2\\cdot 7")
        assertTrue(Terms.times(q(2), "-7") == "2\\cdot (-7)")
        assertTrue(Terms.sum("3x", "-2y", "0", "5") == "3x-2y+5")
        assertTrue(Terms.sum("-x", "+y") == "-x+y")
        assertTrue(Terms.pow("x", 1) == "x" && Terms.pow("x", 0) == "1" && Terms.pow("x", 3) == "x^{3}" && Terms.pow(-2L, 3) == "(-2)^{3}")
        assertTrue(Terms.plus(q(-3)) == "-3" && Terms.plus(q(3)) == "+3" && Terms.plus(q(0)) == "")
        // the lint catches what the formatter prevents
        for (bad in listOf("y=1x", "-1x", "0x+y", "a+-b", "a--b", "x^1", "x^{1}", "7^1-1", "1(x+y)", "x\\cdot 1", "1\\cdot x", "1\\cdot(1,2)", "x+0+y", "0+x", "\\frac{1(3^{n}-1)}{2}"))
            assertTrue("lint misses $bad", problems(Seen("t", bad, math = true)).isNotEmpty())
        for (ok in listOf("x_1+x_2", "(1,0)+(0,1)=(1,1)", "\\frac{1}{2}x", "10x", "13=1\\cdot 7+6", "A^1=A", "2^{10}", "x\\geq 0", "a_{11}x", "4=2\\cdot 2+0", "0+0=0", "1\\cdot 4+3\\cdot 4", "(-2)\\cdot 1+1\\cdot (-1)", "\\mathbb{Z}_{2}^{1}", "0+0+0=0", "(-1)\\cdot(1,0,0)", "\\theta=0\\cdot\\mathbf{u}+0\\cdot\\mathbf{v}", "a\\cdot\\big(6^{2n}-1\\big)"))
            assertTrue("lint flags $ok: ${problems(Seen("t", ok, math = true))}", problems(Seen("t", ok, math = true)).isEmpty())
    }
}

// a citation of a statement (a definition is what a step checks against, so citing it is not a missing reason)
private val reference = Regex("""(?<![\p{L}\d])(?:Vet(?:a|e|y|u|ou)|Viet|Vět(?:a|e|y|u|ou)|Pozorovani[ea]|Pozorovaním|Pozorování|Poznámk(?:a|e|y|u|ou)|Důsled(?:ek|ku|kem)|Dôsled(?:ok|ku|kom|ky)|Tvrzení|Tvrdeni[ea]|Lemm?a)\s+\d+\.\d+""", RegexOption.IGNORE_CASE)
// a reason in words: a causal connective before the citation
private val because = Regex("""(?<![\p{L}\d])(lebo|pretože|keďže|preto|teda|takže|totiž|čiže|inak|keby)(?![\p{L}\d])""", RegexOption.IGNORE_CASE)

/** What breaks the rule "the reason in words, the theorem after it" in one step text: each offending sentence. */
fun reasonProblems(say: String): List<String> {
    val text = say.replace('§', '$').replace(Regex("""\$[^$]*\$"""), "X")
    // sentences end at ". " (not at the dot inside "Veta 2.66" or "1. stĺpec")
    return text.split(Regex("""(?<=[.!?])\s+(?=[\p{Lu}§*(])""")).map { it.trim() }.filter { sentence ->
        val ref = reference.find(sentence) ?: return@filter false
        val reason = because.find(sentence)
        // the citation must follow a reason given in words; a sentence that is only "X holds (Veta n)" has none before it
        reason == null || reason.range.first > ref.range.first
    }.map { "„$it“" }
}

/** The first sentence: up to the first ". " that is not inside $…$ and not after an abbreviation or a number. */
fun firstSentenceOf(s: String): String {
    var inMath = false
    for (i in s.indices) {
        val ch = s[i]
        if (ch == '$') inMath = !inMath
        if (!inMath && (ch == '.' || ch == '!' || ch == '?') && (i + 1 == s.length || s[i + 1] == ' ')) {
            val before = s.substring(0, i).takeLastWhile { it.isLetterOrDigit() }
            if (before in setOf("napr", "tzv", "Def", "resp", "t. j", "č") || before.all { it.isDigit() } && before.isNotEmpty()) continue
            return s.substring(0, i + 1)
        }
    }
    return s
}

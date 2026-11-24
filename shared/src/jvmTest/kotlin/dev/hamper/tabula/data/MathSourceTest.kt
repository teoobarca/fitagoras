package dev.hamper.tabula.data

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.math.formulaTokens
import dev.hamper.tabula.math.Cls
import dev.hamper.tabula.math.unknownCommands
import dev.hamper.tabula.math.displayLines
import dev.hamper.tabula.math.softLines
import dev.hamper.tabula.math.softRows
import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Every formula the app can show is drawable: no command, environment or delimiter the math engine does not know
 * (it would print "sim" or nothing), and minus signs written so the engine can tell a sign from a subtraction.
 * Covers all courses (lessons, definitions, theorems, proofs, past and generated papers), every generator over many
 * seeds with its parts answered, and every clip file.
 */
class MathSourceTest {
    /** Text shown by Rich (inline math between § or $) and display formulas (raw TeX), each with where it is from. */
    private class Corpus {
        val texts = ArrayList<Pair<String, String>>()
        val display = ArrayList<Pair<String, String>>()
        fun t(where: String, s: String?) { if (s != null) texts += where to s }
        fun d(where: String, s: String?) { if (s != null) display += where to s }
        fun line(where: String, l: Line) { when (l) { is Para -> t(where, l.tex); is Item -> t(where, l.tex); is Block -> d(where, l.tex) } }
        fun w(where: String, sol: Solution?) {
            sol ?: return
            for (st in sol.steps) { t(where, st.say); st.math.forEach { f -> d(where, f.tex); t(where, f.note) } }
            sol.postup.aims.forEach { t(where, it.title); t(where, it.how) }
        }
    }

    // built once per test JVM: JUnit makes a new instance for every test method, and the corpus walks every course,
    // generator and paper
    private companion object {
        val corpus: Corpus by lazy {
            val c = Corpus()
            for (course in Courses) {
                for (l in course.lessons) {
                    c.t(l.id, l.learn); c.t(l.id, l.name)
                    for (s in l.steps) when (s) {
                        is Step.Ask -> { c.t(s.id, s.q); s.options.forEach { c.t(s.id, it) }; c.t(s.id, s.after); c.t(s.id, s.sub); s.wrong.values.forEach { c.t(s.id, it) } }
                        is Step.Explore -> { c.t(s.id, s.title); c.t(s.id, s.sub); c.t(s.id, s.doneTitle); c.t(s.id, s.doneBody) }
                        is Step.Explain -> { c.t(s.id, s.title); c.t(s.id, s.body); s.formal?.forEach { c.line(s.id, it) }; c.t(s.id, s.more) }
                        is Step.Worked -> { c.t(s.id, s.title); c.t(s.id, s.problem); c.w(s.id, s.solution) }
                        is Step.Check -> { c.t(s.id, s.q); s.options.forEach { c.t(s.id, it) }; c.t(s.id, s.why); c.t(s.id, s.hint); s.wrong.values.forEach { c.t(s.id, it) }; c.t(s.id, s.sub) }
                        is Step.Practice -> c.t(s.id, s.title)
                        else -> {}
                    }
                }
                for (k in course.concepts) {
                    k.czech.forEach { c.line(k.id, it) }; k.remarks.forEach { q -> q.lines.forEach { c.line(k.id, it) } }; k.why.forEach { c.t(k.id, it.part); c.t(k.id, it.because) }
                    k.judge.forEach { c.t(k.id, it.tex); c.t(k.id, it.because); c.t(k.id, it.wrong) }
                    k.units.forEach { c.t(k.id, it) }; c.t(k.id, k.ask); c.t(k.id, k.name)
                    k.trap?.let { c.t(k.id, it.sentence); c.t(k.id, it.because) }
                }
                // what the statement model puts together as well: the core, the parts and their labels (the Dokazuješ bar), the
                // remarks shown with a theorem, and a proof's own wording
                fun wording(where: String, w: Wording?) {
                    w ?: return
                    w.lines.forEach { c.line(where, it) }; c.d(where, w.core)
                    w.parts.forEach { c.d(where, it.tex); c.t(where, it.label) }
                }
                for (t in course.theorems) {
                    t.czech.forEach { c.line(t.id, it) }; c.t(t.id, t.idea); c.t(t.id, t.note); c.t(t.id, t.name); c.t(t.id, t.short)
                    wording(t.id, t.wording); t.remarks.forEach { q -> q.lines.forEach { c.line(t.id, it) } }
                }
                for (p in course.proofs) {
                    c.t(p.id, p.task); c.t(p.id, p.foot); c.t(p.id, p.given); c.t(p.id, p.end); c.t(p.id, p.title)
                    p.rounds.forEach { r -> c.t(p.id, r.prompt); c.t(p.id, r.given); r.options.forEach { o -> c.t(p.id, o.tex); c.t(p.id, o.why) } }
                    p.checklist.forEach { c.t(p.id, it) }
                    wording(p.id, p.what)
                }
                course.templates.forEach { c.t(it.gen, it.title); c.t(it.gen, it.sub) }
                for (paper in course.papers + (1..60).map { course.generatedPaper(it) }) for (p in paper.problems) {
                    val where = "${paper.id}:${p.n}"
                    c.t(where, p.title); c.t(where, p.text); c.t(where, p.answer); c.t(where, p.note)
                    for (u in p.units) { c.t(where, u.text); c.t(where, u.answer); u.checklist.forEach { c.t(where, it.text) }; c.w(where, u.solution) }
                }
            }
            for (seed in 1..200) for (p in la1GeneratedPaper(seed).problems) for (u in p.units) { c.t("gen-$seed", u.text); c.w("gen-$seed", u.solution); c.t("gen-$seed", u.answer) }
            for ((name, gen) in Generators) repeat(60) { seed ->
                val ex = gen(Random(seed * 7919 + name.hashCode()))
                val where = "$name/$seed"
                c.t(where, ex.prompt); c.t(where, ex.hint); c.t(where, ex.label); c.d(where, ex.visual)
                val answers = ArrayList<Any>()
                for (build in ex.parts) {
                    val part = build(answers)
                    c.t(where, part.q); c.t(where, part.after)
                    when (val a = part.answer) {
                        is Answer.Choice -> { a.options.forEach { c.t(where, it) }; c.t(where, a.why); a.wrong.values.forEach { c.t(where, it) }; answers.add(a.correct) }
                        is Answer.Numbers -> {
                            a.labels.forEach { c.d(where, it) }; c.t(where, a.shown)
                            a.diagnose?.let { d -> c.t(where, d(a.expected.map { it + Frac.ONE })) }
                            answers.add(a.expected)
                        }
                        is Answer.Pick -> {
                            a.options.forEach { c.t(where, it) }
                            val n = a.options.size
                            val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                            sets.forEach { c.t(where, a.why(it)) }
                            answers.add(sets.first(a.ok))
                        }
                        is Answer.Vectors -> { c.t(where, a.check(a.example).head); answers.add(a.example) }
                        is Answer.Paper -> { (a.sample + a.look + a.miss).forEach { c.t(where, it) }; answers.add(1) }
                    }
                }
                c.w(where, ex.solution(answers))
            }
            val assets = listOf(File("src/commonMain/composeResources/files"), File("shared/src/commonMain/composeResources/files")).first { it.exists() }
            fun walk(where: String, v: Any?) {
                when (v) {
                    is String -> c.t(where, v)
                    is JSONObject -> v.keys().forEach { walk(where, v.get(it)) }
                    is JSONArray -> for (i in 0 until v.length()) walk(where, v.get(i))
                }
            }
            assets.listFiles { f -> f.name.startsWith("clips") && f.name.endsWith(".json") }!!.forEach { walk(it.name, JSONArray(it.readText())) }
            c
        }
    }

    private val inline = Regex("""\$([^$]+)\$""")
    private fun formulas(): List<Pair<String, String>> =
        corpus.texts.flatMap { (w, t) -> inline.findAll(t.replace('§', '$')).map { w to it.groupValues[1] }.toList() } + corpus.display

    @Test fun everyFormulaIsDrawable() {
        val all = formulas()
        assertTrue("too few formulas found: ${all.size}", all.size > 3000)
        val bad = all.mapNotNull { (w, tex) -> unknownCommands(tex).takeIf { it.isNotEmpty() }?.let { "$w: ${it.distinct()} in $tex" } }.distinct()
        val kinds = all.flatMap { (_, tex) -> unknownCommands(tex) }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }
        assertTrue("${bad.size} formulas with something the engine cannot draw (${kinds.joinToString { "${it.key} ${it.value}x" }}):\n" + bad.take(60).joinToString("\n"), bad.isEmpty())
        // a display formula too wide for its card is broken into lines, first at its soft breaks (TexBlock, no try/catch): at 320 dp and 200 %
        // nearly every one is, and each line is drawn on its own, so each must still be TeX the engine can draw.
        // Widths are estimated from the source (~0.55 em a symbol at 36 dp); the screenshots check the real pixels.
        val px = 36f; val avail = 320f - 2 * (20f + 16f + 12f)
        fun width(t: String) = t.replace(Regex("""\\[a-zA-Z]+"""), "x").count { !it.isWhitespace() && it != '{' && it != '}' } * .55f * px
        val broken = corpus.display.mapNotNull { (w, tex) ->
            val lines = runCatching { softLines(softRows(tex, avail) { width(it) }, avail, px) { width(it) }.flatMap { l -> displayLines(l.tex, avail - l.indent * px, px * 1.5f) { width(it) } } }.getOrElse { return@mapNotNull "$w: line breaking failed ($it) in $tex" }
            val parts = lines.filter { unknownCommands(it).isNotEmpty() }
            if (parts.isEmpty()) null else "$w: ${parts.map { "$it ${unknownCommands(it)}" }} in $tex"
        }.distinct()
        assertTrue("${broken.size} display formulas whose lines at 320 dp and 200 % cannot be drawn:\n" + broken.take(40).joinToString("\n"), broken.isEmpty())
    }

    /**
     * After "aj", "teda" or "čiže" a formula never starts with a relation: "x + y ∈ P aj ∈ Q, teda ∈ P ∩ Q" drops what
     * is in Q (the lead, 16. 11.); write "aj x + y ∈ Q". Continuation lines of a calculation ("= 3^n + …"), a predicate
     * with its subject in the sentence ("Tie sú ≥ 4") and the Czech definitions verbatim are not this.
     */
    @Test fun noFormulaStartsWithARelation() {
        val joint = Regex("""(?:^|[\s,(])(?:aj|teda|čiže)\s*$""")
        val bad = corpus.texts.flatMap { (w, t) ->
            val text = t.replace('§', '$')
            inline.findAll(text).mapNotNull { m ->
                val toks = formulaTokens(m.groupValues[1])
                if (toks.size > 1 && toks[0].cls == Cls.Rel && joint.containsMatchIn(text.substring(0, m.range.first))) "$w: ${m.value} in ${t.take(140)}" else null
            }.toList()
        }.distinct()
        assertTrue("${bad.size} formulas start with a relation, the subject is missing:\n" + bad.take(40).joinToString("\n"), bad.isEmpty())
    }

    /**
     * A chain of statements in aligned ("a ≡ b, ⇔ a − b ≡ 0, ⇔ …") hangs its arrow in front of the column, so every
     * statement starts at one edge: "\\ \Leftrightarrow\ &a-b…". With "\\ &\Leftrightarrow a-b" the arrow lands in the
     * statements' column after amsmath's empty atom, and the lines start at three different places (judge, 18. 11.).
     */
    @Test fun chainArrowsHangBeforeTheColumn() {
        val inside = Regex("""\\\\\s*&\s*(?:\\ )?\\(?:Leftrightarrow|Rightarrow|iff|implies)(?![a-zA-Z])""")
        // only a chain whose statements all start at the column's edge ("\begin{aligned}&a…"); "a&=b\\&\Rightarrow c"
        // aligns the arrow under the relation, which is right
        val chain = Regex("""\\begin\{aligned\}&.*?\\end\{aligned\}""")
        val bad = corpus.display.filter { (_, tex) -> chain.findAll(tex).any { inside.containsMatchIn(it.value) } }.map { (w, tex) -> "$w: $tex" }.distinct()
        assertTrue("${bad.size} aligned chains with the arrow inside the column (write \\\\\\Leftrightarrow\\ &…):\n" + bad.take(40).joinToString("\n"), bad.isEmpty())
    }

    @Test fun minusIsWrittenSoTheEngineCanTellASignFromASubtraction() {
        // in math, a minus is written "-": the engine makes it U+2212 and decides sign or subtraction; a typed "−" is always a sign
        val typed = formulas().filter { (_, tex) -> '−' in tex }.map { (w, tex) -> "$w: $tex" }.distinct()
        assertTrue("typed U+2212 inside math:\n" + typed.take(40).joinToString("\n"), typed.isEmpty())
        // outside math a negative number would show a hyphen: it belongs in math
        val plain = Regex("""(^|[\s(=,;:])-\d""")
        val text = corpus.texts.mapNotNull { (w, t) ->
            val outside = t.replace('§', '$').replace(inline, " ")
            if (plain.containsMatchIn(outside)) "$w: $t" else null
        }.distinct()
        assertTrue("negative numbers outside math:\n" + text.take(40).joinToString("\n"), text.isEmpty())
    }

    @Test fun signMinusHasNoOperatorSpacingSubtractionHas() {
        fun minus(tex: String) = formulaTokens(tex).filter { it.s == "−" }.map { it.cls }
        assertEquals(listOf(Cls.Ord), minus("""-\tfrac{3}{2}"""))
        assertEquals(listOf(Cls.Ord), minus("(-1,2)"))
        assertEquals(listOf(Cls.Ord), minus("x=-1"))
        assertEquals(listOf(Cls.Ord), minus("""2\cdot(-3)"""))
        assertEquals(listOf(Cls.Bin), minus("a-b"))
        assertEquals(listOf(Cls.Bin, Cls.Ord), minus("a-(-b)"))
    }

    @Test fun rowEquivalenceIsARelation() {
        assertTrue(unknownCommands("""A\sim B""").isEmpty())
        assertEquals(listOf(Cls.Ord, Cls.Rel, Cls.Ord), formulaTokens("""A\sim B""").map { it.cls })
        assertEquals(listOf("\\foo"), unknownCommands("""a\foo b"""))
        assertEquals(listOf("\\begin{bmatrix}"), unknownCommands("""\begin{bmatrix}1\end{bmatrix}"""))
        // what the courses write: \tilde, \ell, \stackrel, \not\in, \square
        assertTrue(unknownCommands("""\tilde{\mathbf{x}}+\ell\stackrel{IP}{=}\square,\ x\not\in M""").isEmpty())
        assertEquals(listOf(Cls.Ord, Cls.Rel, Cls.Ord), formulaTokens("""a\stackrel{IP}{\leq}b""").map { it.cls })
        assertEquals("∉", formulaTokens("""x\not\in M""")[1].s)
        // \not combines with the relation after it, however it is written; never the word "not"
        for ((tex, glyph) in listOf("""x\not \in M""" to "∉", """x\not{\in}M""" to "∉", """a\not=b""" to "≠", """A\not\subseteq B""" to "⊈", """a\not\leq b""" to "≰", """a\not\equiv b""" to "≢")) {
            assertTrue(tex, unknownCommands(tex).isEmpty())
            assertEquals(tex, listOf(Cls.Ord, Cls.Rel, Cls.Ord), formulaTokens(tex).map { it.cls })
            assertEquals(tex, glyph, formulaTokens(tex)[1].s)
        }
        assertEquals(listOf("\\not models"), unknownCommands("""a\not\models b"""))
    }

    /**
     * A command that only starts like \right or \end is not one: \rightarrow ended the formula, so "f:A\rightarrow B"
     * showed as "f:A" in the DML chapter 5 cards (LLM check 17. 11.). The whole formula is drawn, and a formula the
     * parser stops in early is reported by [everyFormulaIsDrawable].
     */
    @Test fun aCommandStartingLikeRightIsNotTheEndOfTheFormula() {
        for (tex in listOf("""f:A\rightarrow B""", """f|_M:M\rightarrow B,\ f|_M(a):=f(a)""", """\left(f:A\rightarrow B\right)""")) {
            assertTrue(tex, unknownCommands(tex).isEmpty())
        }
        assertEquals(listOf("f", ":", "A", "→", "B"), formulaTokens("""f:A\rightarrow B""").mapNotNull { it.s })
        assertEquals(listOf("not drawn: \\right) b"), unknownCommands("""a\right) b"""))
    }
}

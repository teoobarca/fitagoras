package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.Answer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.hamper.tabula.compat.BigInteger
import kotlin.random.Random
import dev.hamper.tabula.compat.*

/**
 * The induction generators, checked by brute force: every claim holds for the first values of n, and every formula
 * shown to the student (closed forms, the n+1 versions, the lines of the step, the wrong options) is evaluated from its
 * TeX text by an independent little evaluator below and compared with sums, powers and recurrences computed directly.
 */
class InductionTest {

    /* ---- exact rationals and a small evaluator for the TeX our generators write ---------------------------------- */

    data class Q(val n: BigInteger, val d: BigInteger) {
        companion object {
            fun of(n: BigInteger, d: BigInteger = BigIntegerOne): Q {
                require(d.signum() != 0)
                val g = n.gcd(d).let { if (it.signum() == 0) BigIntegerOne else it }
                val s = if (d.signum() < 0) -1 else 1
                return Q(n.divide(g).multiply(bigIntegerOf(s.toLong())), d.divide(g).multiply(bigIntegerOf(s.toLong())))
            }
            fun of(n: Long) = of(bigIntegerOf(n))
        }
        operator fun plus(o: Q) = of(n * o.d + o.n * d, d * o.d)
        operator fun minus(o: Q) = of(n * o.d - o.n * d, d * o.d)
        operator fun times(o: Q) = of(n * o.n, d * o.d)
        operator fun div(o: Q) = of(n * o.d, d * o.n)
        operator fun unaryMinus() = Q(n.negate(), d)
        fun pow(e: Q): Q { require(e.d == BigIntegerOne && e.n.signum() >= 0) { "exponent $e" }; return of(n.pow(e.n.toInt()), d.pow(e.n.toInt())) }
    }

    /** Evaluates +, -, \cdot, implicit products, ^{..}, \frac{..}{..}, (..), \big(..\big) with the given variables. */
    class Tex(src: String, private val vars: Map<Char, Q>) {
        private val s = src.replace("\\big(", "(").replace("\\big)", ")").replace("\\Big(", "(").replace("\\Big)", ")").replace(" ", "")
        private var i = 0
        fun value(): Q { val v = expr(); require(i == s.length) { "unparsed '${s.substring(i)}' in $s" }; return v }
        private fun peek(t: String) = s.startsWith(t, i)
        private fun eat(t: String) { require(peek(t)) { "expected $t at $i in $s" }; i += t.length }
        private fun expr(): Q {
            var v = if (peek("-")) { eat("-"); -term() } else term()
            while (i < s.length && (peek("+") || peek("-"))) { if (peek("+")) { eat("+"); v += term() } else { eat("-"); v -= term() } }
            return v
        }
        private fun term(): Q {
            var v = power()
            while (i < s.length) {
                if (peek("\\cdot")) { eat("\\cdot"); v *= power() }
                else if (s[i].isDigit() || s[i] in vars || peek("(") || peek("\\frac")) v *= power()
                else break
            }
            return v
        }
        private fun power(): Q {
            val b = atom()
            if (peek("^{")) { eat("^{"); val e = expr(); eat("}"); return b.pow(e) }
            if (peek("^") ) { eat("^"); val e = Q.of(s[i].digitToInt().toLong()); i++; return b.pow(e) }
            return b
        }
        private fun atom(): Q = when {
            peek("\\frac{") -> { eat("\\frac{"); val a = expr(); eat("}{"); val b = expr(); eat("}"); a / b }
            peek("(") -> { eat("("); val v = expr(); eat(")"); v }
            peek("-") -> { eat("-"); -power() }
            s[i] in vars -> vars.getValue(s[i++])
            s[i].isDigit() -> { val st = i; while (i < s.length && s[i].isDigit()) i++; Q.of(s.substring(st, i).toLong()) }
            else -> error("unexpected '${s[i]}' at $i in $s")
        }
    }

    private fun ev(tex: String, n: Long, k: Long? = null) = Tex(tex, buildMap { put('n', Q.of(n)); if (k != null) put('k', Q.of(k)) }).value()

    /** A matrix as TeX: an optional scalar factor, then \begin{pmatrix}..\end{pmatrix}. */
    private fun evMat(tex: String, n: Long): List<List<Q>> {
        val at = tex.indexOf("\\begin{pmatrix}")
        val scalar = if (at > 0) ev(tex.substring(0, at), n) else Q.of(1)
        val body = tex.substring(at + "\\begin{pmatrix}".length, tex.indexOf("\\end{pmatrix}"))
        return body.split("\\\\").map { row -> row.split("&").map { scalar * ev(it, n) } }
    }

    private fun q(m: List<List<Long>>) = m.map { r -> r.map { Q.of(it) } }

    /* ---- sums ------------------------------------------------------------------------------------------------- */

    @Test fun sums_hold_and_every_shown_formula_says_what_it_should() {
        var families = 0
        repeat(400) { seed ->
            val f = miDrawSum(Random(seed))
            families++
            var acc = Q.of(0)
            for (n in 1L..12L) {
                acc += ev(f.fTex("k"), n, k = n)
                assertEquals("seed $seed: F($n) as shown ${f.bigTex}", acc, ev(f.bigTex, n))
                assertEquals("seed $seed: F($n) as computed", acc, Q.of(f.big(n).num.toBigInteger(), f.big(n).den.toBigInteger()))
                val next = acc + ev(f.fTex("k"), n + 1, k = n + 1)
                assertEquals("seed $seed: F(n+1) ${f.nextTex}", next, ev(f.nextTex, n))
                assertEquals("seed $seed: f(n+1) ${f.fNext}", ev(f.fTex("k"), n + 1, k = n + 1), ev(f.fNext, n))
                f.middle.forEach { line -> assertEquals("seed $seed: step line $line at n=$n", next, ev(line, n)) }
            }
            // the wrong ends of the step are wrong for some n, and the wrong added term too
            assertTrue("seed $seed: ${f.partialTex}", (1L..6L).any { ev(f.partialTex, it) != ev(f.nextTex, it) })
            assertTrue("seed $seed: ${f.thirdTex}", (1L..6L).any { ev(f.thirdTex, it) != ev(f.nextTex, it) })
            assertTrue("seed $seed: f(n) vs f(n+1)", (1L..6L).any { ev(f.fTex("n"), it) != ev(f.fNext, it) })
        }
        assertTrue(families > 0)
    }

    @Test fun sum_formulas_by_hand() {
        // Σ(3k-2) = n(3n-1)/2, Σ 2^{k-1} = 2^n - 1, Σ 1/((2k-1)(2k+1)) = n/(2n+1), Σ k(k+1) = n(n+1)(n+2)/3
        for (n in 1L..10L) {
            assertEquals(Q.of(n * (3 * n - 1), 2), (1L..n).fold(Q.of(0)) { a, k -> a + Q.of(3 * k - 2) })
            assertEquals(Q.of(n, 2 * n + 1), (1L..n).fold(Q.of(0)) { a, k -> a + Q.of(1, (2 * k - 1) * (2 * k + 1)) })
        }
        assertEquals(Q.of(1, 3), ev("""\frac{n}{2n+1}""", 1))
        assertEquals(Q.of(-12), ev("""3n\cdot (-1)^{n-1}""", 4))
        assertEquals("3n-2", miLin(3, -2)); assertEquals("-n", miLin(-1, 0)); assertEquals("n+1", miLin(1, 1)); assertEquals("5", miLin(0, 5))
        assertEquals("(-2)^{n}", miPow(-2, "n")); assertEquals("1", miPow(1, "n")); assertEquals("-3\\cdot 2^{n}", miTimes(-3, "2^{n}"))
        assertEquals("2n^{2}-n+3", miPoly(listOf(2, -1, 3)))
    }

    private fun Q.Companion.of(n: Long, d: Long) = of(bigIntegerOf(n), bigIntegerOf(d))

    /* ---- divisibility ----------------------------------------------------------------------------------------- */

    @Test fun divisibility_holds_by_brute_force_and_the_split_is_exact() {
        repeat(400) { seed ->
            val dv = miDrawDiv(Random(seed))
            val d = bigIntegerOf(dv.d)
            fun term(t: MiTerm, n: Int): BigInteger = bigIntegerOf(t.coef) * bigIntegerOf(t.base).pow(t.e * n + t.s)
            for (n in 1..40) {
                val v = term(dv.t1, n) + term(dv.t2, n)
                assertEquals("seed $seed: $d | ${dv.tex()} at n=$n", BigIntegerZero, v.mod(d))
                if (n <= 8) {
                    assertEquals("seed $seed: shown ${dv.tex()} at n=$n", Q.of(v), ev(dv.tex(), n.toLong()))
                    assertEquals("seed $seed: shown ${dv.tex("n+1")} at n=$n", Q.of(term(dv.t1, n + 1) + term(dv.t2, n + 1)), ev(dv.tex("n+1"), n.toLong()))
                    // T(n+1) = g·T(n) + k·t2(n), and d | k
                    assertEquals(term(dv.t1, n + 1) + term(dv.t2, n + 1), bigIntegerOf(dv.g) * v + bigIntegerOf(dv.k) * term(dv.t2, n))
                }
            }
            assertEquals("seed $seed: d | k", 0L, dv.k % dv.d)
            assertTrue("seed $seed: not true term by term", term(dv.t1, 1).mod(d).signum() != 0 || term(dv.t2, 1).mod(d).signum() != 0)
        }
    }

    /* ---- matrix powers ---------------------------------------------------------------------------------------- */

    @Test fun matrix_guesses_match_powers_and_wrong_guesses_fail() {
        for (kind in 0..3) repeat(150) { seed ->
            val f = miDrawMatrix(Random(seed * 7 + kind), kind)
            for (n in 1..7) {
                val p = q(miMatPow(f.a, n))
                assertEquals("kind $kind seed $seed: A^$n vs ${f.right.tex("n")}", p, evMat(f.right.tex("n"), n.toLong()))
                assertEquals("kind $kind seed $seed: A^${n + 1} vs ${f.right.tex("n+1")}", q(miMatPow(f.a, n + 1)), evMat(f.right.tex("n+1"), n.toLong()))
                // the step: (guess for n)·A multiplied out, written for the student, equals A^{n+1}
                f.middle.filter { it.contains("pmatrix") && !it.contains("A") }.forEach { line ->
                    assertEquals("kind $kind seed $seed: step $line", q(miMatPow(f.a, n + 1)), evMat(line, n.toLong()))
                }
                f.middle.filter { !it.contains("pmatrix") }.forEach { line ->
                    // scalar times A (rank one): t^{n-1}A^2 and t^{n-1}·tA are t^n·A
                    val squared = line.endsWith("A^2")
                    val factor = ev(line.removeSuffix("A^2").removeSuffix("A"), n.toLong())
                    val base = q(miMatPow(f.a, if (squared) 2 else 1))
                    assertEquals("kind $kind seed $seed: $line", q(miMatPow(f.a, n + 1)), base.map { r -> r.map { it * factor } })
                }
            }
            for ((w, _) in f.wrong) {
                assertTrue("kind $kind seed $seed: wrong guess ${w.tex("n")} is right", (1..4).any { q(miMatPow(f.a, it)) != evMat(w.tex("n"), it.toLong()) })
                for (n in 1..5) assertEquals("kind $kind seed $seed: wrong guess as shown vs as computed", q(w.at(n)), evMat(w.tex("n"), n.toLong()))
            }
        }
    }

    @Test fun the_past_papers_matrices_by_hand() {
        val e = listOf(listOf(-1L, 3L), listOf(0L, -1L))
        for (n in 1..9) {
            val s = if (n % 2 == 0) 1L else -1L
            assertEquals(listOf(listOf(s, -3L * n * s), listOf(0L, s)), miMatPow(e, n))
        }
        val d = listOf(listOf(1L, -2L), listOf(-1L, 2L))
        for (n in 1..9) assertEquals(d.map { r -> r.map { it * miIpow(3, n - 1) } }, miMatPow(d, n))
        val rr = listOf(listOf(1L, 0L, 1L), listOf(0L, 3L, 0L), listOf(0L, 0L, 1L))
        for (n in 1..9) assertEquals(listOf(listOf(1L, 0L, n.toLong()), listOf(0L, miIpow(3, n), 0L), listOf(0L, 0L, 1L)), miMatPow(rr, n))
    }

    /* ---- recurrences ------------------------------------------------------------------------------------------ */

    @Test fun recurrences_hold_and_the_closed_form_is_shown_right() {
        repeat(400) { seed ->
            val rc = miDrawRec(Random(seed))
            val a = arrayListOf(0L, rc.closed(1), rc.closed(2))
            while (a.size <= 14) a += rc.p * a[a.size - 1] + rc.q * a[a.size - 2]
            for (n in 1..13) {
                assertEquals("seed $seed: ${rc.closedTex()} at $n", Q.of(a[n]), ev(rc.closedTex(), n.toLong()))
                assertEquals("seed $seed: ${rc.closedTex(1)} at $n", Q.of(a[n + 1]), ev(rc.closedTex(1), n.toLong()))
                if (n >= 2) assertEquals("seed $seed: ${rc.closedTex(-1)} at $n", Q.of(a[n - 1]), ev(rc.closedTex(-1), n.toLong()))
            }
            // the identity the step rests on: p·r + q = r² for both bases
            assertEquals(rc.r1 * rc.r1, rc.p * rc.r1 + rc.q); assertEquals(rc.r2 * rc.r2, rc.p * rc.r2 + rc.q)
        }
        // 2023 (A), 2022: the papers' sequences
        val a = arrayListOf(0L, -1L, 7L); while (a.size < 20) a += a[a.size - 1] + 6 * a[a.size - 2]
        for (n in 1..19) assertEquals(miIpow(3, n - 1) + miIpow(-2, n), a[n])
        val c = arrayListOf(0L, -1L, 25L); while (c.size < 16) c += 12 * c[c.size - 2] - c[c.size - 1]
        for (n in 1..15) assertEquals(miIpow(3, n) + miIpow(-4, n), c[n])
    }

    @Test fun the_2025_k_sequence_is_below_four_exactly_from_n_4() {
        val x = DoubleArray(40); x[1] = 5.0; x[2] = 5.0
        for (n in 1..37) x[n + 2] = kotlin.math.sqrt(1 + 2 * x[n] + x[n + 1])
        assertEquals(4.0, x[3], 0.0)
        for (n in 1..39) assertEquals("x_$n", n >= 4, x[n] < 4)
        assertTrue(kotlin.math.sqrt(13.0) < 4)
        // the generated sequences of the same shape: below the bound exactly from n_0, the step closes, small numbers
        repeat(300) { seed ->
            val t = miSeqTask(Random(seed))
            val x = t.terms(40)
            for (n in 1..40) assertEquals("$t x_$n", n >= t.from, x[n - 1] < t.k)
            assertTrue("$t step", t.step < t.k * t.k && t.sq3 <= 120 && t.k <= 6)
        }
    }

    /* ---- every exercise: the answer the generator expects is the one its own check accepts ----------------------- */

    @Test fun exercises_answers_are_consistent() {
        for ((name, gen) in MiGenerators) repeat(60) { seed ->
            val ex = gen(Random(seed))
            for (build in ex.parts) {
                val part = build(emptyList())
                when (val a = part.answer) {
                    is Answer.Numbers -> assertTrue("$name/$seed: integers expected", a.fractions || a.expected.all { it.isInt })
                    is Answer.Choice -> assertTrue("$name/$seed", a.correct in a.options.indices)
                    else -> {}
                }
            }
        }
        repeat(60) { seed -> val p = miGeneratedProblem(Random(seed)); assertEquals(10, p.pts); assertEquals(10, p.checklist.sumOf { it.pts }) }
    }

    /* ---- the proseminar gaps (InductionDrills.kt) -------------------------------------------------------------------- */

    private fun numbers(e: dev.hamper.tabula.data.Exercise): List<Long> {
        val answers = ArrayList<Any>()
        return e.parts.flatMap { b -> when (val a = b(answers).answer) {
            is Answer.Choice -> { answers.add(a.correct); emptyList() }
            is Answer.Numbers -> { answers.add(a.expected); a.expected.map { it.num } }
            else -> { answers.add(1); emptyList() }
        } }
    }

    /** The set the rules make, by applying them inside a window until nothing new comes. */
    private fun closure(t: MiSetTask, lo: Long, hi: Long): Set<Long> {
        val s = HashSet<Long>(if (t.kind == 2) listOf(t.a) else listOf(t.a, t.b))
        while (true) {
            val new = HashSet<Long>()
            if (t.kind == 2) for (x in s) { new += x + t.b; new += x - t.b }
            else for (m in s) for (n in s) { new += m - n; if (t.kind == 0) new += m + n }
            val add = new.filter { it in lo..hi && it !in s }
            if (add.isEmpty()) return s
            s += add
        }
    }

    @Test fun inductive_sets_are_what_the_rules_make() {
        // Cvičení 4.12: 6, 8 and m ± n make the even numbers
        assertEquals((-40L..40L).filter { it % 2 == 0L }.toSet(), closure(MiSetTask(0, 6, 8), -200, 200).filter { it in -40..40 }.toSet())
        val kinds = HashSet<Int>()
        repeat(300) { seed ->
            val t = miSetTask(Random(seed)); kinds += t.kind
            val made = closure(t, -150, 150).filter { it in -60..60 }.toSet()
            val v = numbers(miSet(t)).single()
            val claimed = if (t.kind == 2) (-60L..60L).filter { Math.floorMod(it - v, t.b) == 0L } else (-60L..60L).filter { it % v == 0L }
            assertEquals("$seed: $t", claimed.toSet(), made)
        }
        assertEquals(setOf(0, 1, 2), kinds)
    }

    @Test fun a_step_by_k_reaches_what_its_base_cases_allow() {
        val seen = HashSet<Boolean>()
        repeat(300) { seed ->
            val t = miStepTask(Random(seed))
            val reached = HashSet<Int>()
            for (b in t.bases) { var n = b; while (n <= t.n0 + 80) { reached += n; n += t.k } }
            val miss = (t.n0..t.n0 + 80).firstOrNull { it !in reached }
            val e = miStep(t)
            val ch = e.parts[0](emptyList()).answer as Answer.Choice
            assertEquals("$seed: covers every n", miss == null, ch.correct == 0); seen += miss == null
            val v = numbers(e).single()
            if (miss == null) { assertTrue("$seed: base case $v", v.toInt() in t.bases); assertEquals(0, (t.asked - v.toInt()) % t.k) }
            else assertEquals("$seed: first n not reached", miss.toLong(), v)
        }
        assertEquals(setOf(true, false), seen)
    }

    @Test fun flawed_inductions_fail_where_they_say() {
        for (fam in 0 until 4) for (n in 1L..40L) {
            // the closed form is right, a shifted one never is, and the step holds for both
            assertEquals(miFlawLeft(fam, n), miFlawRight(fam, 0, n))
            for (c in listOf(-13L, -1L, 7L)) {
                assertTrue(miFlawLeft(fam, n) != miFlawRight(fam, c, n))
                assertEquals(miFlawRight(fam, c, n + 1) - miFlawRight(fam, c, n), miFlawLeft(fam, n + 1) - miFlawLeft(fam, n))
            }
        }
        // Cvičení 4.11 b, c: (n-1)(n+2)/2 and n^2+n-13
        assertEquals((1L..20L).map { (it - 1) * (it + 2) / 2 }, (1L..20L).map { miFlawRight(0, -1, it) })
        assertEquals((1L..20L).map { it * it + it - 13 }, (1L..20L).map { miFlawRight(1, -13, it) })
        val verdicts = HashSet<Int>()
        repeat(300) { seed ->
            val t = miFlawTask(Random(seed))
            val ch = miFlaw(t).parts[0](emptyList()).answer as Answer.Choice
            verdicts += ch.correct
            assertEquals("$seed: $t", when (t.kind) { 0 -> 0; 1, 3 -> 1; else -> 2 }, ch.correct)
        }
        assertEquals(setOf(0, 1, 2), verdicts)
    }
}

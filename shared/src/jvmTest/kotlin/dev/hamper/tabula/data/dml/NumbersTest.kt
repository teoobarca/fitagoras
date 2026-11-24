package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.TemplatesTest.Companion.checkMath
import dev.hamper.tabula.data.TemplatesTest.Companion.checkText
import dev.hamper.tabula.data.dml.numbers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.hamper.tabula.compat.BigInteger
import kotlin.random.Random
import kotlin.math.abs
import dev.hamper.tabula.compat.*
import dev.hamper.tabula.compat.JMath

/**
 * Number theory: the exact procedures against brute force, and every template's expected answers re-derived
 * independently (search over Z_m, BigInteger powers), so a generator can never ask for a wrong number.
 */
class NumbersTest {
    private fun bi(n: Long) = bigIntegerOf(n)
    private fun bruteGcd(a: Long, b: Long): Long { val x = JMath.abs(a); val y = JMath.abs(b); if (x == 0L) return y; if (y == 0L) return x; return (minOf(x, y) downTo 1).first { x % it == 0L && y % it == 0L } }

    @Test fun rea_rows_are_combinations_and_end_in_the_gcd() {
        for (a in 1L..160) for (b in 1L..a) {
            val t = rea(a, b)
            t.rows.forEach { assertEquals("$a,$b row $it", it.r, it.alpha * a + it.beta * b) }
            assertEquals(0L, t.rows.last().r)
            assertEquals("gcd($a,$b)", bruteGcd(a, b), t.gcd)
            assertEquals(t.gcd, t.alpha * a + t.beta * b)
        }
    }

    @Test fun textbook_examples() {
        // Příklad 6.3: gcd(254,158) = 2 = 28·254 + (-45)·158, the table of the textbook row by row
        val t = rea(254, 158)
        assertEquals(listOf(254L, 158, 96, 62, 34, 28, 6, 4, 2, 0), t.rows.map { it.r })
        assertEquals(listOf(1L, 0, 1, -1, 2, -3, 5, -23, 28), t.rows.dropLast(1).map { it.alpha })
        assertEquals(listOf(0L, 1, -1, 2, -3, 5, -8, 37, -45), t.rows.dropLast(1).map { it.beta })
        assertEquals(listOf(1L, 1, 1, 1, 1, 4, 1, 2), t.rows.drop(1).dropLast(1).map { it.q })
        // Cvičení 6.5: 21 = 5·420 + (-9)·231; 12 = 5·(-60) + (-2)·(-156); 1 = 10·118 + 9·(-131)
        assertEquals(Triple(21L, 5L, -9L), tcBezout(420, 231))
        assertEquals(Triple(12L, 5L, -2L), tcBezout(-60, -156))
        assertEquals(Triple(1L, 10L, 9L), tcBezout(118, -131))
        // Příklad 6.5 and 6.6: 7x + 13y = 125
        val l = ldr(7, 13, 125)!!
        assertEquals(250L to -125L, l.x0 to l.y0)
        assertEquals(3L to 8L, l.xMin to l.yMin)
        assertEquals(16L to 1L, (l.x0 - 18 * l.sx) to (l.y0 - 18 * l.sy))
        // Příklady 6.11, 6.14, 6.16; Cvičení 6.22 i, j
        assertEquals(24L, powMod(5, 203, 101)); assertEquals(7L, powMod(2, 268, 9)); assertEquals(36L, powMod(3, 644, 645))
        assertEquals(88L, powMod(360240142, 123, 120))
        assertEquals(51L, bi(242162).modPow(bi(123).pow(234), bi(121)).toLong())
        // Příklad 6.13
        assertEquals(40L, phi(100)); assertEquals(96L, phi(360))
        // Příklady 6.17, 6.19, 6.20; Cvičení 6.28
        assertEquals(listOf(3L, 8, 13), lkon(9, 12, 15)!!.inZm)
        assertEquals(Crt(23, 30), crt(listOf(1L to 2L, 2L to 3L, 3L to 5L)))
        assertEquals(Crt(23, 30), crt(listOf(5L to 6L, 3L to 10L, 8L to 15L)))
        val c1 = lkon(powMod(3, 1338, 28), 1, 28)!!; val c2 = lkon(powMod(5, 1032, 45), 25, 45)!!
        assertEquals(Crt(169, 252), crt(listOf(c1.x0 to c1.n, c2.x0 to c2.n)))
        // the tests: 2023 A, 2023 D, 2024 R, 2025 K, 2025 E
        val a1 = lkon(6, 36, 195)!!; val a2 = lkon(5, 4, 91)!!
        assertEquals(6L to 65L, a1.x0 to a1.n); assertEquals(19L to 91L, a2.x0 to a2.n)
        assertEquals(Crt(201, 455), crt(listOf(a1.x0 to a1.n, a2.x0 to a2.n)))
        assertEquals(5L, powMod(425, 181, 42)); assertEquals(32L, bi(862).modPow(bi(425).pow(181), bi(43)).toLong())
        assertEquals(3L, powMod(463, 221, 46)); assertEquals(14L, bi(947).modPow(bi(463).pow(221), bi(47)).toLong())
        assertEquals(7L, powMod(667, 325, 60)); assertEquals(6L, bi(917).modPow(bi(667).pow(325), bi(61)).toLong())
        assertEquals(431L, tcSmallestModulus(420)); assertEquals(641L, tcSmallestModulus(630))
        assertEquals(235L, inverse(420, 431)); assertEquals(233L, inverse(630, 641))
        // Spěvák, 8. prednáška (18. 11. 2026), the numbers on the board and in the lessons built from it
        val sp = lkon(12, 18, 75)!!
        assertEquals(14L to 25L, sp.x0 to sp.n); assertEquals(listOf(14L, 39, 64), sp.inZm)
        assertEquals(19L, inverse(4, 25)); assertEquals(13L, inverse(2, 25)); assertEquals(14L, lkon(2, 3, 25)!!.x0)
        assertEquals(30L, lcm(lcm(6, 10), 15))
        assertEquals(Crt(46, 84), crt(listOf(2L to 4L, 4L to 6L, 4L to 21L)))
        assertEquals(Crt(7, 12), crt(listOf(3L to 4L, 1L to 6L))); assertEquals(null, crt(listOf(1L to 6L, 2L to 4L)))
        assertEquals(listOf(18L, 54L, 4L, 10L, 12L), listOf(phi(27), phi(81), phi(12), phi(22), phi(28)))
        assertEquals(2L, phi(2) * phi(6))
        assertEquals(4L, inverse(7, 9)); assertEquals(4L, powMod(7, 5, 9)); assertEquals(3L, gcd(3, 645))
        assertEquals(4L, powMod(3, 100, 7)); assertEquals(1L, powMod(3, 1338, 28)); assertEquals(listOf(4L, 9), lkon(6, 4, 10)!!.inZm)
    }

    /** Tvrzení 6.4 (and its proof in the app): m | a - b exactly when a and b have the same remainder, also below zero. */
    @Test fun congruence_is_equal_remainders_by_brute_force() {
        for (m in 2L..12L) for (a in -40L..40L) for (b in -40L..40L) {
            val divides = (a - b) % m == 0L
            assertEquals("$a, $b mod $m", divides, md(a, m) == md(b, m))
            assertEquals("$a, $b mod $m: a = b + km", divides, (-80L..80L).any { k -> a == b + k * m })
        }
    }

    @Test fun inverse_lkon_phi_pow_against_brute_force() {
        for (m in 2L..90) {
            assertEquals("phi($m)", (1..m).count { bruteGcd(it, m) == 1L }.toLong(), phi(m))
            for (a in 0 until m) {
                val inv = inverse(a, m)
                val brute = (0 until m).firstOrNull { (a * it) % m == 1L }
                assertEquals("inverse $a mod $m", brute, inv)
                for (b in 0 until m) {
                    val sols = (0 until m).filter { md(a * it - b, m) == 0L }
                    val s = lkon(a, b, m)
                    if (sols.isEmpty()) assertNull("$a x = $b mod $m", s) else assertEquals("$a x = $b mod $m", sols, s!!.inZm)
                }
                for (e in listOf(0L, 1, 2, 7, 64, 99, 1000)) assertEquals(bi(a).modPow(bi(e), bi(m)).toLong(), powMod(a, e, m))
            }
        }
    }

    @Test fun ldr_and_crt_against_brute_force() {
        for (a in 1L..30) for (b in 1L..30) for (c in -40L..40) {
            val s = ldr(a, b, c)
            val exists = (-200L..200).any { x -> (c - a * x) % b == 0L }
            assertEquals("$a x + $b y = $c", exists, s != null)
            if (s != null) {
                assertEquals(c, a * s.x0 + b * s.y0); assertEquals(c, a * s.xMin + b * s.yMin)
                assertTrue(s.xMin in 0 until s.sx)
            }
        }
        val r = Random(7)
        repeat(3000) {
            val ms = List(r.nextInt(2, 4)) { r.nextLong(2, 25) }
            val rs = ms.map { r.nextLong(0, it) }
            val l = ms.fold(1L) { x, y -> lcm(x, y) }
            val sols = (0 until l).filter { x -> ms.indices.all { md(x - rs[it], ms[it]) == 0L } }
            val s = crt(rs.zip(ms))
            if (sols.isEmpty()) assertNull(s) else assertEquals("$rs mod $ms", Crt(sols.single(), l), s)
        }
    }

    /** Runs a generated exercise, answering every part right; returns the expected numbers of each part. */
    private fun run(ex: Exercise, where: String): List<Any> {
        checkText("$where prompt", ex.prompt)
        val answers = ArrayList<Any>()
        for (build in ex.parts) {
            val part = build(answers)
            checkText("$where q", part.q)
            when (val a = part.answer) {
                is Answer.Choice -> { assertEquals("$where: wrong options explained", a.options.indices.toSet() - a.correct, a.wrong.keys); answers.add(a.correct) }
                is Answer.Numbers -> { assertNotNull(a.diagnose); assertTrue("$where integers", a.expected.all { it.isInt }); answers.add(a.expected.map { it.num }) }
                is Answer.Paper -> { assertTrue("$where: paper part without a model", a.sample.isNotEmpty() && a.look.isNotEmpty()); answers.add(1) }
                else -> error("no picks in number theory")
            }
        }
        TemplatesTest.checkSolution(where, ex.solution(answers))
        return answers
    }

    @Suppress("UNCHECKED_CAST") private fun nums(x: Any) = x as List<Long>

    @Test fun every_template_answers_agree_with_brute_force() {
        repeat(200) { seed ->
            val r = Random(seed)
            tcReaTask(r).let { t ->
                val ans = run(tcRea(t), "rea $t")
                val g = bruteGcd(t.a, t.b)
                assertEquals(listOf(g), nums(ans[0]))
                val (m, n) = nums(ans[1]); assertEquals("bezout $t", g, m * t.a + n * t.b)
                assertEquals(listOf(JMath.abs(t.a * t.b) / g), nums(ans[2]))
            }
            tcLdrTask(r).let { t ->
                val ans = run(tcLdr(t), "ldr $t")
                val g = bruteGcd(t.a, t.b)
                val solvable = (0 until t.b).any { x -> (t.c - t.a * x) % t.b == 0L }
                assertEquals("ldr $t", if (solvable) 0 else 1, ans[0])
                if (solvable) {
                    val x = (0L until t.b / g).single { x -> (t.c - t.a * x) % t.b == 0L }
                    assertEquals(listOf(x, (t.c - t.a * x) / t.b), nums(ans[1]))
                    assertEquals(listOf(t.b / g, t.a / g), nums(ans[2]))
                }
            }
            tcInvTask(r).let { t ->
                val ans = run(tcInv(t), "inv $t")
                if (t.bound) {
                    val m = (t.a + 2..t.a + 400).first { bruteGcd(it, t.a) == 1L }
                    assertEquals(listOf(m), nums(ans[1]))
                    assertEquals(listOf((0 until m).single { (t.a * it) % m == 1L }), nums(ans[2]))
                } else {
                    val brute = (0 until t.m).firstOrNull { (t.a * it) % t.m == 1L }
                    assertEquals(if (brute != null) 0 else 1, ans[0])
                    if (brute != null) assertEquals(listOf(brute), nums(ans[1]))
                }
            }
            tcPowTask(r).let { t ->
                val ans = run(tcPow(t), "pow $t")
                val res = bi(t.base).modPow(bi(t.n), bi(t.m)).toLong()
                assertEquals(listOf(res), nums(ans.last()))
                // a coprime base (a shared factor is dml-tc-binary): φ, the exponent, then the power left
                assertEquals("pow $t: coprime base", 1L, bruteGcd(t.base, t.m))
                val ph = (1..t.m).count { bruteGcd(it, t.m) == 1L }.toLong()
                assertEquals(listOf(ph), nums(ans[0])); assertEquals(listOf(t.n % ph), nums(ans[1]))
            }
            tcPowTask(r, big = true).let { t ->
                val ans = run(tcPow(t, "dml-tc-pow-binary"), "pow big $t")
                assertEquals(listOf(bi(t.base).modPow(bi(t.n), bi(t.m)).toLong()), nums(ans.last()))
                val ph = (1..t.m).count { bruteGcd(it, t.m) == 1L }.toLong()
                assertTrue("pow big $t: a power left beyond the small ones", t.n % ph > TcSmallPower)
            }
            tcBinaryTask(r).let { t ->
                val ans = run(tcBinary(t), "binary $t")
                // a shared factor, the largest power of two in the exponent, its square row and the power, by BigInteger
                val g = bruteGcd(t.base, t.m)
                assertTrue("binary $t: base shares a factor", g > 1L)
                assertEquals(listOf(g), nums(ans[0]))
                val top = (0..62).map { 1L shl it }.last { it <= t.n }
                assertEquals(listOf(top), nums(ans[1]))
                assertEquals(listOf(bi(t.base).modPow(bi(top), bi(t.m)).toLong()), nums(ans[2]))
                assertEquals(listOf(bi(t.base).modPow(bi(t.n), bi(t.m)).toLong()), nums(ans[3]))
            }
            tcTowerTask(r).let { t ->
                val ans = run(tcTower(t), "tower $t")
                val e = bi(t.a).pow(t.n.toInt())
                assertEquals(listOf(e.mod(bi(t.p - 1)).toLong()), nums(ans[1]))
                assertEquals(listOf(bi(t.c).modPow(e, bi(t.p)).toLong()), nums(ans[2]))
            }
            tcLkonTask(r).let { t ->
                val ans = run(tcLkon(t), "lkon $t")
                val sols = (0 until t.m).filter { md(t.a * it - t.b, t.m) == 0L }
                assertEquals(if (sols.isNotEmpty()) 0 else 1, ans[0])
                if (sols.isNotEmpty()) {
                    val (x0, n) = nums(ans[1])
                    assertEquals(sols, (0 until t.m).filter { md(it - x0, n) == 0L })
                    assertTrue(x0 in 0 until n)
                    assertEquals(listOf(sols.size.toLong()), nums(ans[2]))
                }
            }
            tcCrtTask(r).let { t ->
                val ans = run(tcCrt(t), "crt $t")
                val prod = t.parts.fold(1L) { a, p -> a * p.second }
                val sols = (0 until prod).filter { x -> t.parts.all { (rr, m) -> md(x - rr, m) == 0L } }
                assertEquals(if (sols.isNotEmpty()) 0 else 1, ans[0])
                if (sols.isNotEmpty()) {
                    val (x0, mm) = nums(ans[1])
                    assertEquals(sols, (0 until prod).filter { md(it - x0, mm) == 0L })
                    assertEquals(listOf(sols.first() - (if (sols.size > 1) sols[1] - sols[0] else prod)), nums(ans[2]))
                }
            }
            tcComboTask(r).let { t ->
                val ans = run(tcCombo(t), "combo $t")
                val a2 = if (t.power) bi(t.base).modPow(bi(t.exp), bi(t.m2)).toLong() else t.a2v
                val l = t.m1 * t.m2
                val sols = (0 until l).filter { x -> md(t.a1v * x - t.b1, t.m1) == 0L && md(a2 * x - t.b2, t.m2) == 0L }
                val (x0, mm) = nums(ans[2])
                assertEquals("combo $t", sols, (0 until l).filter { md(it - x0, mm) == 0L })
                assertEquals(listOf(x0 - mm), nums(ans[3]))
                assertTrue(x0 - mm < 0 && x0 >= 0)
            }
            // the four types of 19. 11.: every answer checked against brute force over the original congruences, and every
            // number a student writes on paper stays small (moduli, the solution, the interval)
            fun solves(cs: List<TcCong>, x: Long) = cs.all { c -> md(c.a * x - c.b, c.m) == 0L }
            tcWordTask(r).let { t ->
                val ans = run(tcWord(t), "word $t")
                assertEquals("word $t residues", t.mods.map { md(t.n, it) }, nums(ans[0]))
                val (x0, mm) = nums(ans[1])
                val l = t.mods.fold(1L) { a, b -> lcm(a, b) }
                assertEquals("word $t modulus", l, mm)
                assertTrue("word $t x0", x0 in 0 until l && t.mods.indices.all { md(x0 - t.res[it], t.mods[it]) == 0L })
                val n = nums(ans[2]).single()
                assertEquals("word $t answer", t.n, n)
                // the answer is the only number in the story's window that fits every sentence
                val fits = (t.lo..t.hi).filter { x -> t.mods.indices.all { md(x - t.res[it], t.mods[it]) == 0L } }
                assertEquals("word $t only answer", listOf(n), if (Stories_FIRST(t)) fits.take(1) else fits)
                assertTrue("word $t sizes", l <= 1200 && t.hi <= 3600 && t.mods.all { it <= 30 })
                // what the text says is what the residue is
                t.mods.indices.forEach { i ->
                    when (t.how[i]) {
                        TcSaid.MISSING -> assertEquals(0L, md(t.n + t.said[i], t.mods[i]))
                        TcSaid.YEAR -> assertTrue(md(t.said[i] - t.n, t.mods[i]) == 0L && t.said[i] <= 2026)
                        TcSaid.NONE -> assertEquals(0L, md(t.n, t.mods[i]))
                        else -> assertEquals(md(t.n, t.mods[i]), t.said[i])
                    }
                }
            }
            tcIntervalTask(r).let { t ->
                val ans = run(tcInterval(t), "interval $t")
                val big = t.cs.fold(1L) { a, c -> lcm(a, c.m) }
                val sols = (0 until big).filter { solves(t.cs, it) }
                assertEquals("interval $t solvable", if (sols.isNotEmpty()) 0 else 1, ans[0])
                if (sols.isNotEmpty()) {
                    val (x0, mm) = nums(ans[1])
                    assertEquals("interval $t all", sols, (0 until big).filter { md(it - x0, mm) == 0L })
                    val inside = (t.lo..t.hi).filter { solves(t.cs, it) }
                    assertEquals("interval $t count", listOf(inside.size.toLong()), nums(ans[2]))
                    if (inside.isNotEmpty()) assertEquals(listOf(inside.first()), nums(ans[3]))
                }
                assertTrue("interval $t sizes", t.cs.all { it.m <= 60 } && t.hi <= 600)
            }
            tcOnlyTask(r).let { t ->
                val ans = run(tcOnly(t), "only $t")
                val big = t.cs.fold(1L) { a, c -> lcm(a, c.m) }
                val sols = (0 until big).filter { solves(t.cs, it) }
                val (x0, mm) = nums(ans[0])
                assertEquals("only $t", sols, (0 until big).filter { md(it - x0, mm) == 0L })
                assertEquals(1, ans[1])
                assertTrue("only $t sizes", t.cs.all { it.m <= 200 } && mm <= 500)
            }
            tcInvEulerTask(r).let { t ->
                val ans = run(tcInvEuler(t), "euler $t")
                val brute = (0 until t.m).firstOrNull { md(t.a * it, t.m) == 1L }
                assertEquals("euler $t exists", if (brute != null) 0 else 1, ans[0])
                if (brute != null) {
                    val ph = (1..t.m).count { bruteGcd(it, t.m) == 1L }.toLong()
                    assertEquals(listOf(ph), nums(ans[1])); assertEquals(listOf(ph - 1), nums(ans[2]))
                    assertEquals(listOf(brute), nums(ans[3]))
                    assertTrue("euler $t: the power is small", ph - 1 <= TcSmallPower)
                }
            }
        }
    }

    /** A story asking for the first moment: the smallest nonnegative solution, so later ones are fine to exist. */
    private fun Stories_FIRST(t: TcWordTask) = t.lo == 0L && t.hi == t.n

    @Test fun feedback_starts_with_a_short_sentence() {
        fun first(s: String) = s.split(Regex("(?<=[.!?])\\s+(?=[A-ZÁČĎÉÍĽĹŇÓÔŔŠŤÚÝŽ§])"), limit = 2)[0]
        fun words(s: String) = s.replace(Regex("\\$[^$]*\\$"), "X").split(Regex("\\s+")).count { it.isNotBlank() }
        val r = Random(3)
        for ((name, gen) in TcGenerators) repeat(60) {
            val ex = gen(r); val answers = ArrayList<Any>()
            for (build in ex.parts) {
                val part = build(answers)
                when (val a = part.answer) {
                    is Answer.Choice -> { (a.wrong.values + a.why).forEach { assertTrue("$name: long first sentence: $it", words(first(it)) <= 18) }; answers.add(a.correct) }
                    is Answer.Numbers -> {
                        val bad = a.expected.map { dev.hamper.tabula.la.Frac.of(it.num + 1) }
                        assertTrue("$name: long first sentence: ${a.diagnose!!(bad)}", words(first(a.diagnose!!(bad))) <= 18)
                        answers.add(a.expected.map { it.num })
                    }
                    else -> {}
                }
            }
        }
    }

    /* ---- the proseminar gaps (NumbersDrills.kt): every answer re-derived by brute force ------------------------------ */

    private fun divides(a: Long, b: Long) = if (a == 0L) b == 0L else b % a == 0L
    private fun ex(gen: String, seed: Int) = Generators.getValue(gen)(Random(seed * 7919 + 17))
    private fun numbersOf(e: dev.hamper.tabula.data.Exercise): List<Answer.Numbers> {
        val answers = ArrayList<Any>()
        return e.parts.mapNotNull { b -> val a = b(answers).answer; when (a) { is Answer.Choice -> answers.add(a.correct); is Answer.Numbers -> answers.add(a.expected); else -> answers.add(1) }; a as? Answer.Numbers }
    }
    private fun longs(a: Answer.Numbers) = a.expected.map { assertTrue(it.isInt); it.num }

    /** Each claim of the pool as the statement it shows, checked on every tuple of small integers, zero and negatives too. */
    private val divClaimTruth: Map<String, (Long, Long, Long, Long) -> Boolean> = mapOf(
        "refl" to { a, _, _, _ -> divides(a, a) },
        "jedna-nula" to { z, _, _, _ -> divides(1, z) && divides(z, 0) },
        "tranzitivita" to { a, b, c, _ -> !(divides(a, b) && divides(b, c)) || divides(a, c) },
        "antisymetria" to { a, b, _, _ -> a < 0 || b < 0 || !(divides(a, b) && divides(b, a)) || a == b },
        "antisymetria-z" to { a, b, _, _ -> !(divides(a, b) && divides(b, a)) || a == b },
        "absolutne" to { x, y, _, _ -> !(divides(x, y) && divides(y, x)) || abs(x) == abs(y) },
        "zvysok" to { a, b, _, _ -> a <= 0 || divides(a, b) == (Math.floorMod(b, a) == 0L) },
        "sucin" to { a, b, c, d -> !(divides(a, b) && divides(c, d)) || divides(a * c, b * d) },
        "kratenie" to { a, b, c, _ -> !(divides(a * c, b * c) && c != 0L) || divides(a, b) },
        "kratenie-nula" to { a, b, c, _ -> !divides(a * c, b * c) || divides(a, b) },
        "euklid" to { a, b, c, _ -> !divides(a, b * c) || divides(a, b) || divides(a, c) },
        "abs-hodnoty" to { a, b, _, _ -> divides(a, b) == divides(abs(a), abs(b)) },
        "odhad" to { a, b, _, _ -> !(divides(a, b) && b != 0L) || abs(a) <= abs(b) },
        "odhad-nula" to { a, b, _, _ -> !divides(a, b) || abs(a) <= abs(b) },
        "sucet" to { a, b, c, _ -> !(divides(a, b) && divides(a, c)) || divides(a, b + c) },
        "nasobok" to { a, b, n, _ -> !divides(a, b) || divides(a, n * b) },
        "rozdiel" to { a, b, c, _ -> !(divides(a, b + c) && divides(a, b)) || divides(a, c) },
        "scitanec" to { a, b, c, _ -> !divides(a, b + c) || divides(a, b) },
        "kombinacie" to { a, b, c, _ -> (divides(a, b) && divides(a, c)) == (-3L..3L).all { m -> (-3L..3L).all { n -> divides(a, m * b + n * c) } } },
    )

    @Test fun div_claims_hold_or_fail_by_brute_force() {
        assertEquals("every claim of the pool has its statement here", TcDivClaims.map { it.id }.toSet(), divClaimTruth.keys)
        val r = -6L..6L
        for (c in TcDivClaims) {
            val p = divClaimTruth.getValue(c.id)
            val all = r.all { a -> r.all { b -> r.all { cc -> r.all { d -> p(a, b, cc, d) } } } }
            assertEquals("${c.id}: holds for every small tuple", c.holds, all)
            // a false claim: its drawn counterexample satisfies the hypothesis and breaks the claim
            if (!c.holds) repeat(200) { seed ->
                val n = c.counter!!(Random(seed)).nums
                assertFalse("${c.id} counterexample $n", p(n.getValue("a"), n.getValue("b"), n["c"] ?: 0L, 0L))
            }
        }
        // the generator offers both kinds and its choice says which
        val seen = HashSet<Boolean>()
        repeat(300) { seed -> val e = ex("dml-tc-delitelnost", seed); val ch = e.parts[0](emptyList()).answer as Answer.Choice; seen += ch.correct == 0; assertTrue(e.parts[1](listOf(ch.correct)).answer is Answer.Paper) }
        assertEquals(setOf(true, false), seen)
    }

    @Test fun divisibility_for_every_n_by_brute_force() {
        val kinds = HashSet<Int>()
        repeat(600) { seed ->
            val r = Random(seed)
            val t = tcAllNTask(r)
            kinds += t.kind
            val e = tcAllN(t, Random(seed))
            val first = e.parts[0](emptyList()).answer as Answer.Numbers
            val v = longs(first).single()
            when (t.kind) {
                0 -> {
                    for (n in -40L..120L) assertEquals("$seed: ${t.d} | P($n)", 0L, java.math.BigInteger.valueOf(0).let { _ -> t.poly.fold(java.math.BigInteger.ZERO) { acc, (c, e) -> acc + java.math.BigInteger.valueOf(c) * java.math.BigInteger.valueOf(n).pow(e) } }.mod(java.math.BigInteger.valueOf(t.d)).toLong())
                    val at = Regex("""P\((\d+)\)""").find(first.labels[0])!!.groupValues[1].toLong()
                    assertEquals("$seed: P($at)", t.poly.sumOf { (c, e) -> c * (1..e).fold(1L) { acc, _ -> acc * at } }, v)
                }
                1 -> {
                    for (n in -60L..60L) { var p = java.math.BigInteger.ONE; for (i in 0 until t.k) p *= java.math.BigInteger.valueOf(n + i); assertEquals("$seed: ${t.d} | product at $n", 0L, p.mod(java.math.BigInteger.valueOf(t.d)).toLong()) }
                    val (asked, mod) = Regex("""n\\equiv (\d+)\\pmod\{(\d+)\}""").find(e.parts[0](emptyList()).q)!!.groupValues.drop(1).map { it.toLong() }
                    assertTrue("$seed: j=$v below k", v in 0 until t.k)
                    assertEquals("$seed: n+j is a multiple of $mod", 0L, (asked + v) % mod)
                }
                else -> {
                    val pw = t.pw!!
                    for (n in pw.n0..40) {
                        val cn = java.math.BigInteger.valueOf(pw.g1).pow(pw.k1 * n + pw.s1) + java.math.BigInteger.valueOf(pw.c2) * java.math.BigInteger.valueOf(pw.g2).pow(pw.k2 * n + pw.s2)
                        assertEquals("$seed: ${t.d} | c_$n", 0L, cn.mod(java.math.BigInteger.valueOf(t.d)).toLong())
                    }
                    assertEquals("$seed: both bases leave the same remainder", Math.floorMod(pw.b2, t.d), v)
                    assertEquals(Math.floorMod(pw.b1, t.d), v)
                }
            }
        }
        assertEquals(setOf(0, 1, 2), kinds)
        // the textbook's and the proseminars' own instances are among what the generator can draw
        assertEquals(0L, (0L..30L).maxOf { n -> java.math.BigInteger.valueOf(2).pow(3 * n.toInt() + 1).add(java.math.BigInteger.valueOf(3).pow(n.toInt() + 1)).mod(java.math.BigInteger.valueOf(5)).toLong() })
        assertEquals(0L, (1L..30L).maxOf { n -> java.math.BigInteger.valueOf(4).pow(n.toInt() + 1).add(java.math.BigInteger.valueOf(5).pow(2 * n.toInt() - 1)).mod(java.math.BigInteger.valueOf(21)).toLong() })
    }

    @Test fun gcd_and_lcm_from_factorisations_by_brute_force() {
        assertEquals(listOf(21L, 4620L), numbersOf(tcFactor(TcFactorTask(420, 231))).flatMap(::longs))
        assertEquals(listOf(12L, 780L), numbersOf(tcFactor(TcFactorTask(-60, -156))).flatMap(::longs))
        repeat(500) { seed ->
            val t = tcFactorTask(Random(seed))
            val (g, l) = numbersOf(tcFactor(t)).flatMap(::longs)
            assertEquals("$seed gcd", bruteGcd(t.a, t.b), g)
            assertEquals("$seed lcm", (1L..abs(t.b)).map { abs(t.a) * it }.first { it % t.b == 0L }, l)
        }
    }

    @Test fun reduction_modulo_m_by_brute_force() {
        repeat(500) { seed ->
            val t = tcReduceTask(Random(seed))
            val x = t.xs.map { java.math.BigInteger.valueOf(it) }
            val e = when (t.shape) { 0 -> x[0] * x[1] + x[2] * x[3]; 1 -> x[0] * x[1] - x[2] * x[3]; 2 -> x[0] * x[1] + x[2].pow(t.e); else -> (x[0] + x[1]) * x[2] }
            val m = java.math.BigInteger.valueOf(t.m)
            val (res, opp) = numbersOf(tcReduce(t)).flatMap(::longs)
            assertEquals("$seed: value mod ${t.m}", e.mod(m).toLong(), res)
            assertEquals("$seed: opposite", e.negate().mod(m).toLong(), opp)
            // every representative the solution writes is congruent to its number and small
            t.xs.forEach { val q = tcRep(it, t.m); assertEquals(0L, Math.floorMod(it - q, t.m)); assertTrue(abs(q) < t.m) }
        }
    }

    @Test fun quotient_and_remainder_by_brute_force() {
        assertEquals(listOf(-6L, 1L), numbersOf(tcDiv(TcDivTask(0, 4, -23))).flatMap(::longs))
        assertEquals(listOf(636L, 2L), numbersOf(tcDiv(TcDivTask(2, 23, 14630, "x"))).flatMap(::longs))
        repeat(500) { seed ->
            val t = tcDivTask(Random(seed))
            val (q, r) = numbersOf(tcDiv(t)).flatMap(::longs)
            val rr = (0 until t.d).first { (t.a - it) % t.d == 0L }
            assertEquals("$seed: remainder of ${t.a} by ${t.d}", rr, r)
            assertEquals("$seed: quotient", (t.a - rr) / t.d, q)
            if (t.kind == 2) {
                // the expression as shown, evaluated from its TeX
                val ns = Regex("""-?\d+""").findAll(t.expr.replace("\\cdot", "*")).map { it.value.toLong() }.toList()
                val minus = t.expr.contains("-")
                val inner = ns[0] * ns[1] * ns[2] + ns[3] * ns[4] + (if (minus) -1 else 1) * abs(ns[5]) * ns[6]
                assertEquals("$seed: ${t.expr}", inner * ns[7], t.a)
            }
        }
    }

    @Test fun word_problems_on_a_diophantine_equation_by_brute_force() {
        // Cvičení 6.10: 11x + 6y = 350 has 5 solutions; with at least 10 of each the most children is 18; Cvičení 6.9: 11 and 5 minutes
        assertEquals(5, (0L..350L).count { x -> (350 - 11 * x).let { it >= 0 && it % 6 == 0L } })
        assertEquals(listOf(18L), numbersOf(tcWordLdr(TcWordLdrTask(3, 11, 6, 350, 10))).flatMap(::longs))
        assertEquals(listOf(2L, -3L, 22L), numbersOf(tcWordLdr(TcWordLdrTask(2, 11, 5, 7))).flatMap(::longs))
        val kinds = HashSet<Int>()
        repeat(600) { seed ->
            val t = tcWordLdrTask(Random(seed))
            kinds += t.kind
            val e = tcWordLdr(t)
            val got = numbersOf(e).flatMap(::longs)
            val sols = (t.least..t.c / t.a).mapNotNull { x -> val rest = t.c - t.a * x; if (rest >= t.b * t.least && rest % t.b == 0L) x to rest / t.b else null }
            when (t.kind) {
                0, 1 -> {
                    assertEquals("$seed count", sols.size.toLong(), got[0])
                    if (sols.isNotEmpty()) { val m = sols.minOf { it.first + it.second }; assertEquals("$seed fewest", m, got[1] + got[2]); assertEquals(t.c, t.a * got[1] + t.b * got[2]) }
                }
                3 -> assertEquals("$seed children", sols.maxOf { minOf(it.first, it.second) }, got[0])
                else -> {
                    val all = (-80L..80L).flatMap { x -> (-80L..80L).filter { y -> t.a * x + t.b * y == t.c }.map { x to it } }
                    val ch = e.parts[0](emptyList()).answer as Answer.Choice
                    assertEquals("$seed solvable", all.isNotEmpty(), ch.correct == 0)
                    if (all.isNotEmpty()) {
                        val best = all.minOf { abs(it.first) + abs(it.second) }
                        assertEquals("$seed one best", 1, all.count { abs(it.first) + abs(it.second) == best })
                        val (x, y) = got
                        assertEquals(t.c, t.a * x + t.b * y); assertEquals(best, abs(x) + abs(y))
                        assertEquals("$seed time", if (x >= 0 && y >= 0) t.c else maxOf(abs(x) * t.a, abs(y) * t.b), got[2])
                    }
                }
            }
        }
        assertEquals(setOf(0, 1, 2, 3), kinds)
    }

    /** Tvrzení 6.5 and 6.6, whose proofs the app builds line by line, on every small case. */
    @Test fun congruence_properties_by_brute_force() {
        for (m in 2L..9L) {
            fun eq(a: Long, b: Long) = Math.floorMod(a - b, m) == 0L
            val r = -12L..12L
            for (a in r) {
                assertTrue(eq(a, a))
                for (b in r) {
                    assertEquals(eq(a, b), eq(b, a))
                    for (c in r) if (eq(a, b) && eq(b, c)) assertTrue(eq(a, c))
                    if (eq(a, b)) for (c in r) for (d in r) if (eq(c, d)) {
                        assertTrue(eq(a + c, b + d)); assertTrue(eq(a - c, b - d)); assertTrue(eq(a * c, b * d))
                    }
                    if (eq(a, b)) for (k in 1..6) assertTrue(eq((1..k).fold(1L) { x, _ -> x * a }, (1..k).fold(1L) { x, _ -> x * b }))
                }
            }
        }
    }
}

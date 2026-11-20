package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.q
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Množiny a zobrazenia: the logic against the textbook's own answers (cvičenia 5.3 to 5.11) and the past tests, and
 * every generator checked by brute force on small universes and small integer boxes.
 */
class SetsTest {

    /* ---- set identities ---------------------------------------------------------------------------------------- */

    /** The textbook's verdicts (Řešení 5.3 až 5.24) and the past tests' identities, solved by hand once. */
    private val known = mapOf(
        "A - B <= A" to true, "A - B = A & ~B" to true, "A & (B - A) = 0" to true, "(A - B) & (B - C) = 0" to true,
        "(A - B) - C <= A - C" to true, "A | (B - A) = A | B" to true, "(A | B) - C = (A - C) | (B - C)" to true,
        "A - (B | C) = (A - B) & (A - C)" to true, "A - (B & C) = (A - B) | (A - C)" to true, "A & (B - C) = (A & B) - (A & C)" to true,
        "(X - Y) & (Y - X) = 0" to true, "X - (Y | Z) = (X - Y) | Z" to false, "~(X - Y) = ~(Y - X)" to false,
        "~(X & Y) <= X" to false, "(X & Y) | (Y - X) = X" to false, "X & (Y - Z) = (X & Y) - (X & Z)" to true,
        "X * (Y | Z) = (X * Y) | (X * Z)" to true, "X * (Y - Z) = (X * Y) - (X * Z)" to true, "X * 0 = 0" to true, "~(X * Y) = ~X * ~Y" to false,
        "(C - B) & (C - A) = C - (A | B)" to true, "(C - B) | (C - A) = C - (A | B)" to false, "~X * ~Y <= ~(X * Y)" to true,
        "(X - Y) * (X - Z) = (X - Z) * (X - Y)" to false, "(A & B & ~C) <= (A | B) - C" to true,
        "(A * B) & (A * ~(C & D)) = A * ((B - C) | (B - D))" to true, "(X - W) - (Z - Y) = (X - Z) - (W - Y)" to false,
    )

    @Test fun identities_as_the_textbook_decides_them() {
        for ((s, v) in known) assertEquals(s, v, identity(s).holds())
        assertFalse("Řešení 5.21: not defined", identity("X - (Y * Z) = (X - Y) * (X - Z)").defined)
        assertFalse("Řešení 5.22: not defined", identity("X & (Y * Z) = (X & Y) * (X & Z)").defined)
    }

    @Test fun pool_verdicts_agree_with_concrete_universes() {
        // the formula check against brute force over every choice of subsets of a 3-element universe
        for (p in IdentityPool) {
            val id = identity(p.s)
            if (!id.defined) continue
            val u = listOf("1", "2", "3")
            val subsets = (0 until 8).map { m -> u.filterIndexed { i, _ -> m shr i and 1 == 1 }.toSet() }
            val vs = id.vars
            var all = true
            fun rec(i: Int, acc: Map<String, Set<String>>) {
                if (i == vs.size) { if (!id.holdsOn(Concrete(u, acc))) all = false; return }
                for (s in subsets) rec(i + 1, acc + (vs[i] to s))
            }
            rec(0, emptyMap())
            assertEquals(p.s, all, id.holds())
            if (id.holds()) assertTrue("${p.s}: a valid identity needs its closing step", p.key.isNotBlank())
        }
    }

    @Test fun the_textbook_counterexample_works() {
        // Řešení 5.14: X={1}, Y={2}, Z={3} in U={1,2,3}
        val c = Concrete(listOf("1", "2", "3"), mapOf("X" to setOf("1"), "Y" to setOf("2"), "Z" to setOf("3")))
        assertFalse(identity("X - (Y | Z) = (X - Y) | Z").holdsOn(c))
        // Řešení 5.24: X={a}, Y={a,b} in U={a,b}: complement of X×Y is {(b,a),(b,b)}, X̄×Ȳ is empty
        val d = Concrete(listOf("a", "b"), mapOf("X" to setOf("a"), "Y" to setOf("a", "b")))
        assertEquals(setOf("b" to "a", "b" to "b"), se("~(X * Y)").eval(d).pairs)
        assertEquals(emptySet<Pair<String, String>>(), se("~X * ~Y").eval(d).pairs)
    }

    @Test fun unfolding_keeps_every_line_equivalent() {
        for (p in IdentityPool) {
            val id = identity(p.s)
            if (!id.defined) continue
            for (side in listOf(id.lhs, id.rhs)) {
                val ch = unfold(side)
                val pair = side.level() == 2
                for (k in 1 until ch.size) assertTrue("${p.s}: line $k", equivalent(ch[k - 1], ch[k], id.vars, pair))
                assertTrue(ch.last().tex(), !ch.last().tex().contains("cap") && !ch.last().tex().contains("cup") && !ch.last().tex().contains("setminus") && !ch.last().tex().contains("overline"))
            }
        }
    }

    @Test fun tex_of_expressions() {
        assertEquals("(A \\setminus B) \\cup C", se("(A - B) | C").tex())
        assertEquals("\\overline{X \\times Y}", se("~(X * Y)").tex())
        assertEquals("\\overline{X} \\times \\overline{Y}", se("~X * ~Y").tex())
    }

    /* ---- finite maps ------------------------------------------------------------------------------------------- */

    @Test fun finite_maps_by_brute_force() {
        // Pozorování 5.2: no injection from a bigger set, no surjection onto a bigger set; same size: inj ⇔ surj
        assertEquals(0, countMaps(4, 3) { injective(it) })
        assertEquals(0, countMaps(2, 3) { surjective(it, 3) })
        assertEquals(6, countMaps(3, 3) { injective(it) })
        assertEquals(countMaps(3, 3) { injective(it) }, countMaps(3, 3) { surjective(it, 3) })
        assertEquals(36, countMaps(4, 3) { surjective(it, 3) })
        val f = intArrayOf(1, 2, 0)
        val inv = inverse(f, 3)!!
        assertTrue(compose(f, inv).withIndex().all { it.index == it.value })
        assertTrue(compose(inv, f).withIndex().all { it.index == it.value })
        assertNull(inverse(intArrayOf(0, 0, 1), 3))
        assertEquals(setOf(0, 1), preimage(intArrayOf(0, 0, 1), setOf(0)))
        assertEquals(setOf(0), image(intArrayOf(0, 0, 1), setOf(0, 1)))
        // arrows: two arrows from one point is not a map; a point without an arrow is not total
        assertFalse(Arrows(2, 2, setOf(0 to 0, 0 to 1, 1 to 1)).isMap)
        assertFalse(Arrows(2, 2, setOf(0 to 0)).isTotal)
        assertTrue(Arrows(2, 2, setOf(0 to 1, 1 to 0)).bijective)
    }

    /* ---- affine maps on Z^k and R^k ---------------------------------------------------------------------------- */

    private fun aff(m: List<List<Long>>, c: List<Long>, real: Boolean = false) = Affine(m, c, real)

    @Test fun affine_maps_as_the_textbook_decides_them() {
        // Cvičení 5.10 d: (m+n, m-n) injective, not surjective
        aff(listOf(listOf(1L, 1L), listOf(1L, -1L)), listOf(0L, 0L)).let { assertTrue(it.injective); assertFalse(it.surjective) }
        // 5.10 e: 2m-n not injective, surjective; g: m+n+13 likewise
        aff(listOf(listOf(2L, -1L)), listOf(0L)).let { assertFalse(it.injective); assertTrue(it.surjective) }
        aff(listOf(listOf(1L, 1L)), listOf(13L)).let { assertFalse(it.injective); assertTrue(it.surjective) }
        // 5.9 c: 13n injective, not surjective; a: n+1 bijective
        aff(listOf(listOf(13L)), listOf(0L)).let { assertTrue(it.injective); assertFalse(it.surjective) }
        aff(listOf(listOf(1L)), listOf(1L)).let { assertTrue(it.injective); assertTrue(it.surjective) }
        // Příklad 5.7 over R: (m, m+2n) surjective; Příklad 5.8 over Z: not
        assertTrue(aff(listOf(listOf(1L, 0L), listOf(1L, 2L)), listOf(0L, 0L), real = true).surjective)
        aff(listOf(listOf(1L, 0L), listOf(1L, 2L)), listOf(0L, 0L)).let { assertFalse(it.surjective); assertTrue(it.provablyNoPreimage(listOf(0L, 1L))) }
        // sample paper 2025: (2m-3n+2, 5m-7n+3) is a bijection of Z^2
        aff(listOf(listOf(2L, -3L), listOf(5L, -7L)), listOf(2L, 3L)).let { assertTrue(it.injective); assertTrue(it.surjective) }
        // 2024 R-2c: (-x-y+z, 2x+2y-2z) on R^3 → R^2: neither
        aff(listOf(listOf(-1L, -1L, 1L), listOf(2L, 2L, -2L)), listOf(0L, 0L), real = true).let { assertFalse(it.injective); assertFalse(it.surjective) }
    }

    /** Small box search: does y have an integer preimage with all coordinates in -b..b? */
    private fun hitInBox(f: Affine, y: List<Long>, b: Long = 30): Boolean {
        fun vecs(n: Int): Sequence<List<Long>> = if (n == 0) sequenceOf(emptyList()) else vecs(n - 1).flatMap { v -> (-b..b).asSequence().map { v + it } }
        return vecs(f.k).any { f.at(it) == y }
    }

    @Test fun affine_generator_claims_hold_by_brute_force() {
        for (seed in 0 until 300) {
            val r = Random(seed)
            for (shape in AffShape.entries) {
                val f = drawAffine(r, shape)
                f.kernel()?.let { d -> assertTrue("$f kernel", d.any { it != 0L }); assertEquals("$f same image", f.at(List(f.k) { 0L }), f.at(d)) }
                assertEquals("$f injective iff no kernel", f.injective, f.kernel() == null)
                val y0 = f.noPreimage()
                if (f.surjective) assertNull(y0) else {
                    assertNotNull("$f has a point without preimage", y0)
                    assertTrue(f.provablyNoPreimage(y0!!))
                    if (!f.real && f.k <= 2) assertFalse("$f: $y0 has no integer preimage", hitInBox(f, y0))
                }
                if (f.surjective && !f.real) for (t in 0 until 5) {
                    val y = List(f.l) { r.nextInt(-9, 10).toLong() }
                    val pre: List<Frac> = if (f.k == f.l) f.preimageQ(y.map { it.q() }) else { val (a, b) = bezout(f.m[0][0], f.m[0][1]); listOf((a * (y[0] - f.c[0])).q(), (b * (y[0] - f.c[0])).q()) }
                    assertTrue("$f: integer preimage of $y", pre.all { it.isInt })
                    assertEquals("$f maps $pre to $y", y.map { it.q() }, f.atQ(pre))
                }
                // the solution lines are produced and well formed
                InjSurjPostup.solve { injectivity(f, InjSurjPostup[0]); surjectivity(f, InjSurjPostup[1]) }.let { sol -> assertTrue(sol.problems().toString(), sol.problems().isEmpty()) }
            }
        }
    }

    @Test fun surjective_preimage_formulas_hit_the_target() {
        // over Z^2 → Z^2 with det ±1 the preimage from Cramer is integral and maps back
        for (seed in 0 until 200) {
            val f = drawAffine(Random(seed), AffShape.Z22)
            if (!f.surjective) continue
            val y = listOf(Random(seed).nextInt(-20, 20).toLong(), 7L)
            val p = f.preimageQ(y.map { it.q() })
            assertTrue(p.all { it.isInt })
            assertEquals(y.map { it.q() }, f.atQ(p))
        }
    }

    /* ---- generators -------------------------------------------------------------------------------------------- */

    @Test fun generators_answer_exactly() {
        for (seed in 0 until 150) {
            for ((name, gen) in DmlSets.generators) {
                val ex = gen(Random(seed * 31 + name.hashCode()))
                val answers = ArrayList<Any>()
                for (build in ex.parts) {
                    val part = build(answers)
                    when (val a = part.answer) {
                        is Answer.Choice -> { assertTrue(a.correct in a.options.indices); assertEquals("$name options unique", a.options.size, a.options.toSet().size); answers.add(a.correct) }
                        is Answer.Pick -> {
                            val good = (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count && a.ok(it) }
                            assertEquals("$name: exactly one right pick set", 1, good.size)
                            answers.add(good[0])
                        }
                        is Answer.Numbers -> answers.add(a.expected)
                        is Answer.Vectors -> error("no vectors in sets")
                        is Answer.Paper -> answers.add(1)
                    }
                }
                assertTrue(ex.solution(answers).steps.isNotEmpty())
            }
        }
    }

    @Test fun identity_counterexamples_break_and_others_hold() {
        for (seed in 0 until 300) {
            val ex = identityExercise(Random(seed))
            val first = ex.parts[0](emptyList()).answer as Answer.Choice
            if (first.correct != 1) continue
            // the right counterexample option is in the why; the others say they keep the identity
            if (ex.parts.size > 1) {
                val second = ex.parts[1](listOf(1)).answer as Answer.Choice
                assertTrue(second.why.startsWith("Táto voľba vzťah porušuje"))
                second.wrong.values.forEach { assertTrue(it.startsWith("Táto voľba vzťah spĺňa")) }
            }
        }
    }

    @Test fun identity_explanations_use_the_exercise_letters() {
        val single = Regex("(?<![\\\\A-Za-z])[A-Z](?![A-Za-z])")
        for (seed in 0 until 300) {
            val ex = identityExercise(Random(seed))
            val vars = Regex("\\$?([A-Z](,[A-Z])*)\\$? jsou libovolné").find(ex.prompt)!!.groupValues[1].split(",").toSet()
            val texts = ex.parts.flatMap { p -> (p(listOf(1)).answer as Answer.Choice).let { listOf(it.why) + it.wrong.values } } + ex.solution(emptyList()).steps.flatMap { s -> listOf(s.say) + s.math.map { "$" + it.tex + "$" } }
            for (t in texts) for ((i, part) in t.split('$').withIndex()) if (i % 2 == 1)
                single.findAll(part).forEach { assertTrue("$seed: ${it.value} not among $vars in: $t", it.value in vars + "U") }
        }
    }

    @Test fun cardinality_answers_by_enumeration() {
        for (seed in 0 until 300) {
            val ex = cardinalityExercise(Random(seed))
            val p = Regex("""\|C\\times D\|=(\d+)""").find(ex.prompt)?.groupValues?.get(1)?.toInt()
            val s = Regex("""\|C\|\+\|D\|=(\d+)""").find(ex.prompt)?.groupValues?.get(1)?.toInt()
            val a = ex.parts[0](emptyList()).answer
            if (s != null && p != null) {
                val exists = (0..s).any { it * (s - it) == p }
                assertEquals(ex.prompt, if (exists) 0 else 1, (a as Answer.Choice).correct)
            } else if (p != null) {
                // C ⊆ D: enumerate |C| ≤ |D| with product p
                val values = (1..p).flatMap { c -> (c..p).filter { c * it == p }.map { it - c } }.toSet()
                a as Answer.Pick
                val good = (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { a.ok(it) }
                assertEquals(values, good.map { a.options[it].filter(Char::isDigit).toInt() }.toSet())
            } else {
                val k = Regex("""právě \$(\d+)\$""").find(ex.prompt)!!.groupValues[1].toInt()
                val exists = (0..10).any { 1 shl it == k }
                assertEquals(if (exists) 0 else 1, (a as Answer.Choice).correct)
            }
        }
    }

    @Test fun paper_2025_k_counterexamples() {
        // f(x,y,z) = (√(yz), √(xz), √(yz)) on (R^+)^3, compared through the squares (yz, xz, yz), exactly
        fun sq(x: Frac, y: Frac, z: Frac) = listOf(y * z, x * z, y * z)
        val h = Frac.of(1, 2)
        assertEquals(sq(1.q(), 2.q(), 2.q()), sq(2.q(), 4.q(), 1.q()))   // the app's: f(1,2,2) = f(2,4,1) = (2,√2,2)
        assertEquals(sq(1.q(), 1.q(), 1.q()), sq(h, h, 2.q()))           // the lead's: f(1,1,1) = f(1/2,1/2,2) = (1,1,1)
        // first and third components are always equal, so (1,1,2) and (1,2,3) have no preimage
        for (x in 1..5) for (y in 1..5) for (z in 1..5) { val s = sq(x.q(), y.q(), z.q()); assertEquals(s[0], s[2]) }
    }

    @Test fun generated_problem_adds_up() {
        for (seed in 0 until 40) {
            val p = setsGeneratedProblem(Random(seed))
            assertEquals(10, p.pts); assertEquals(10, p.checklist.sumOf { it.pts })
        }
    }

    @Suppress("unused") private fun frac(n: Int) = Frac.of(n)

    /* ---- the proseminar gaps (SetsDrills.kt) ------------------------------------------------------------------------- */

    /** The right side of each claim as the statement it shows, on concrete sets of a universe u. */
    private val inclusionTruth: Map<String, (Set<Int>, Set<Int>, Set<Int>) -> Boolean> = mapOf(
        "prienik" to { _, a, b -> a.intersect(b) == a }, "zjednotenie" to { _, a, b -> a.union(b) == b },
        "rozdiel" to { _, a, b -> (a - b).isEmpty() }, "doplnky" to { u, a, b -> (u - a).containsAll(u - b) },
        "prienik-doplnok" to { u, a, b -> a.intersect(u - b).isEmpty() }, "doplnok-zjednotenie" to { u, a, b -> (u - a).union(b) == u },
        "opacna" to { _, a, b -> a.containsAll(b) }, "prienik-b" to { _, a, b -> a.intersect(b) == b }, "zjednotenie-a" to { _, a, b -> a.union(b) == a },
        "doplnky-zle" to { u, a, b -> (u - b).containsAll(u - a) }, "rozdiel-zle" to { _, a, b -> (b - a).isEmpty() },
    )

    @Test fun inclusion_equivalences_by_every_pair_of_subsets() {
        assertEquals(InclusionClaims.map { it.id }.toSet(), inclusionTruth.keys)
        val u = (1..4).toSet()
        val subsets = (0 until 16).map { m -> u.filter { (m shr (it - 1)) and 1 == 1 }.toSet() }
        for (c in InclusionClaims) {
            val p = inclusionTruth.getValue(c.id)
            val always = subsets.all { a -> subsets.all { b -> b.containsAll(a) == p(u, a, b) } }
            assertEquals("${c.id}: A ⊆ B ⇔ ${c.right} for every A, B ⊆ {1,2,3,4}", c.holds, always)
        }
        repeat(300) { seed ->
            val (uu, a, b) = inclusionCounter(Random(seed))
            assertTrue("$seed: A ⊊ B ⊊ U", a.isNotEmpty() && b.containsAll(a) && b != a && uu.containsAll(b) && b != uu)
            for (c in InclusionClaims.filter { !it.holds }) assertFalse("$seed ${c.id}: the counterexample breaks it", inclusionTruth.getValue(c.id)(uu, a, b))
        }
    }

    @Test fun domains_and_ranges_by_computing_the_maps() {
        val seen = HashSet<String>()
        repeat(400) { seed ->
            val w = wordMap(Random(seed))
            seen += w.src
            assertEquals("one right domain", 1, w.domain.count { it.wrong == null })
            assertEquals("one right range", 1, w.range.count { it.wrong == null })
            val v = w.values ?: return@repeat
            val got = v()
            for (x in w.window) assertEquals("${w.text}: $x in the range", w.claimed!!(x), x in got)
        }
        assertTrue("every map of Cvičení 5.8 is drawn", seen.size >= 10)
    }
}

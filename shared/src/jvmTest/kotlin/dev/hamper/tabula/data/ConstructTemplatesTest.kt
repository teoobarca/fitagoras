package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random
import dev.hamper.tabula.compat.*
import dev.hamper.tabula.compat.JMath

/**
 * The constructing and listing exercises of kapitola 2 (data/ConstructTemplates.kt) and the judges of the student's
 * own vectors (data/Vectors.kt), each against brute force over Z_p: every coefficient tuple, every set of vectors.
 * Nothing here trusts the GEM code that the exercises use.
 */
class ConstructTemplatesTest {

    /* ---- brute force over Z_p, independent of la/Gem.kt ---- */

    private fun tuples(p: Int, k: Int): List<List<Int>> = if (k == 0) listOf(emptyList()) else tuples(p, k - 1).flatMap { t -> (0 until p).map { t + it } }
    private fun combo(p: Int, c: List<Int>, vs: List<List<Int>>): List<Int> = List(vs[0].size) { j -> JMath.floorMod(c.indices.sumOf { c[it] * vs[it][j] }, p) }
    private fun spanBrute(p: Int, vs: List<List<Int>>): Set<List<Int>> = tuples(p, vs.size).map { combo(p, it, vs) }.toSet()
    private fun lnBrute(p: Int, vs: List<List<Int>>): Boolean = tuples(p, vs.size).none { c -> c.any { it != 0 } && combo(p, c, vs).all { it == 0 } }
    private fun fr(v: List<Int>): Vec = v.map { Frac.of(it) }
    private fun ints(v: Vec): List<Int> = v.map { it.num.toInt() }
    private fun Random.vec(p: Int, n: Int) = List(n) { nextInt(p) }

    @Test fun span_list_judge_matches_brute_force() {
        val r = Random(1)
        repeat(400) {
            val p = listOf(2, 3, 5).random(r); val n = r.nextInt(2, 5); val k = r.nextInt(1, if (p == 5) 3 else 4)
            val gens = List(k) { r.vec(p, n) }
            val f = Field.Zp(p)
            val all = spanBrute(p, gens).toList().shuffled(r)
            assertEquals("the model's span is the brute-force span", all.toSet(), f.spanElements(gens.map(::fr)).map(::ints).toSet())
            val list = all.map { listOf(fr(it)) }
            assertTrue("the whole span, each once", judgeSpanList(f, gens.map(::fr), list).ok)
            if (all.size > 1) {
                val res = judgeSpanList(f, gens.map(::fr), list.drop(1))
                assertFalse("one left out", res.ok); assertTrue(res.head, res.head.startsWith("Chýba"))
            }
            val twice = judgeSpanList(f, gens.map(::fr), list + listOf(list[0]))
            assertFalse("one twice", twice.ok); assertTrue("the repeat is named", twice.each.last() != null && twice.each.dropLast(1).all { it == null })
            val outside = tuples(p, n).firstOrNull { it !in all }
            if (outside != null) {
                val res = judgeSpanList(f, gens.map(::fr), list + listOf(listOf(fr(outside))))
                assertFalse("a vector outside", res.ok); assertTrue(res.each.last() != null)
            }
        }
    }

    @Test fun subspace_list_judge_matches_brute_force() {
        val r = Random(2)
        for ((p, n, d) in listOf(Triple(2, 2, 1), Triple(3, 2, 1), Triple(5, 2, 1), Triple(7, 2, 1), Triple(2, 3, 1), Triple(2, 3, 2), Triple(3, 3, 1))) {
            val s = FiniteSpace(p, n)
            // every d-dimensional subspace as a set: spans of every LN d-tuple, by brute force
            val byBasis = tuples(p, n * d).map { flat -> flat.chunked(n) }.filter { lnBrute(p, it) }
            val sets = byBasis.groupBy { spanBrute(p, it) }
            assertEquals("Z_$p^$n, dim $d: count", s.subspaceCount(d).toInt(), sets.size)
            repeat(20) {
                val list = sets.values.map { bases -> bases.random(r).map(::fr) }.shuffled(r)
                assertTrue("each subspace once by any basis", judgeSubspaceList(s, d, list).ok)
                assertFalse("one left out", judgeSubspaceList(s, d, list.drop(1)).ok)
                // a subspace twice under another basis (over Z_2 a line has only one basis: then the same one)
                val other = sets.values.firstOrNull { it.size > 1 } ?: sets.values.first()
                val again = judgeSubspaceList(s, d, list + listOf(other.last().map(::fr)))
                assertFalse("a subspace twice", again.ok)
                assertTrue("the repeat is named", again.each.last()!!.contains("už v zozname"))
                if (d >= 1) {
                    val dep = List(d) { List(n) { 0 } }.map(::fr)
                    assertFalse("θ is not a basis", judgeSubspaceList(s, d, list + listOf(dep)).ok)
                }
            }
        }
    }

    @Test fun basis_judge_matches_brute_force() {
        val r = Random(3)
        repeat(600) {
            val p = listOf(2, 3, 5).random(r); val n = r.nextInt(2, 5)
            val gens = List(r.nextInt(1, 4)) { r.vec(p, n) }
            val m = spanBrute(p, gens).toList()
            val d = Field.Zp(p).rank(gens.map(::fr))
            if (d == 0) return@repeat
            // d vectors: some from M, sometimes one from outside
            val pick = List(d) { if (r.nextInt(6) == 0) r.vec(p, n) else m.random(r) }
            val brute = pick.all { it in m } && lnBrute(p, pick)
            val res = judgeBasis(Field.Zp(p), gens.map(::fr), pick.map { listOf(fr(it)) }) { null }
            assertEquals("$gens / $pick", brute, res.ok)
        }
    }

    @Test fun system_rows_judge_matches_brute_force() {
        val r = Random(4)
        repeat(800) {
            val p = listOf(3, 5).random(r)
            val u = r.vec(p, 4); val w = r.vec(p, 4)
            val rows = List(2) { r.vec(p, 5) }.let { rr -> if (r.nextBoolean()) rr else rr.map { row -> row.take(4) + JMath.floorMod(row.take(4).indices.sumOf { row[it] * u[it] }, p) } }
            fun solves(x: List<Int>) = rows.all { row -> JMath.floorMod(row.take(4).indices.sumOf { row[it] * x[it] }, p) == row[4] }
            val brute = solves(u) && solves(w) && lnBrute(p, rows.map { it.take(4) })
            assertEquals("$u $w $rows", brute, judgeSystemRows(Field.Zp(p), fr(u), fr(w), rows.map { listOf(fr(it)) }).ok)
        }
    }

    @Test fun lc_ways_counts_every_combination() {
        val r = Random(5)
        repeat(500) {
            val p = listOf(2, 3, 5).random(r); val n = r.nextInt(2, 4); val m = r.nextInt(1, 5)
            if (Math.pow(p.toDouble(), m.toDouble()) > 700) return@repeat
            val zs = List(m) { r.vec(p, n) }; val v = r.vec(p, n)
            val brute = tuples(p, m).count { combo(p, it, zs) == v }.toLong()
            assertEquals("$zs -> $v", brute, lcWays(Field.Zp(p), zs.map(::fr), fr(v)))
        }
    }

    @Test fun parameter_over_zp_read_off_the_gem_agrees_with_brute_force() {
        val r = Random(6)
        var edges = 0
        repeat(3000) {
            val p = listOf(2, 3, 5, 7).random(r)
            val kind = listOf(ParamKind.BASIS, ParamKind.GENERATES, ParamKind.LN).random(r)
            val m = when (kind) { ParamKind.BASIS -> 3; ParamKind.GENERATES -> r.nextInt(3, 5); else -> r.nextInt(2, 4) }
            val numeric = List(m - 1) { fr(r.vec(p, 3)) }
            val base = fr(r.vec(p, 3)); val at = r.nextInt(3); val shift = r.nextInt(p)
            val dec = paramZpDecide(p, kind, numeric, base, at, shift)
            // brute force: put every α in and test LN or generating by enumerating combinations
            val brute = (0 until p).filter { a ->
                val vs = (numeric + listOf(base.mapIndexed { j, x -> if (j == at) Frac.of(JMath.floorMod(a + shift, p)) else x })).map(::ints)
                when (kind) { ParamKind.GENERATES -> spanBrute(p, vs).size == tuples(p, 3).size; ParamKind.BASIS -> vs.size == 3 && lnBrute(p, vs); else -> lnBrute(p, vs) }
            }.toSet()
            assertEquals("$kind p=$p $numeric $base at=$at shift=$shift", brute, dec.truth)
            assertEquals("the GEM reading, $kind p=$p $numeric $base", brute, dec.symbolic)
            if (brute.isEmpty() || brute.size == p) edges++
        }
        assertTrue("edge cases (none or every α) were drawn: $edges", edges > 50)
    }

    @Test fun parameter_over_zp_exercise_accepts_exactly_the_right_values() {
        repeat(300) { seed ->
            val ex = paramZpExercise(Random(seed))
            val a = ex.parts.single()(emptyList()).answer as Answer.Pick
            val n = a.options.size
            val good = (0 until (1 shl n)).map { mk -> (0 until n).filter { mk shr it and 1 == 1 }.toSet() }.filter(a.ok)
            assertEquals("seed $seed: exactly one right set", 1, good.size)
        }
    }

    @Test fun basis_from_coordinates_has_exactly_the_expected_solution() {
        repeat(300) { seed ->
            val c = fromCoordsCase(Random(seed))
            val p = c.p
            val sols = tuples(p, 4).filter { t ->
                val x = t.take(2); val y = t.drop(2)
                combo(p, ints(c.c1), listOf(x, y)) == ints(c.u) && combo(p, ints(c.c2), listOf(x, y)) == ints(c.w)
            }
            assertEquals("seed $seed: one solution", listOf(ints(c.x) + ints(c.y)), sols)
            assertTrue("x, y is a basis", lnBrute(p, listOf(ints(c.x), ints(c.y))))
            val a = basisFromCoordsExercise(c).parts.single()(emptyList()).answer as Answer.Numbers
            assertEquals(c.x + c.y, a.expected)
        }
    }

    @Test fun coordinates_of_a_combination_by_a_real_basis() {
        repeat(400) { seed ->
            val r = Random(seed + 1000)
            when (val c = coordsLcCase(Random(seed))) {
                is CoordsLcCase.Named -> {
                    val f = Field.R
                    var b: List<Vec>
                    do { b = List(3) { List(3) { Frac.of(r.nextInt(-3, 4)) } } } while (!f.isLN(b))
                    val v = f.combo(c.k, b)
                    assertEquals("seed $seed", c.answer, f.coords(c.order.map { b[it] }, v))
                }
                is CoordsLcCase.Given -> {
                    val f = c.f; val d = c.cv.size
                    var b: List<Vec>
                    do { b = List(d) { List(c.n) { f.norm(Frac.of(r.nextInt(-3, 6))) } } } while (!f.isLN(b))
                    val v = f.combo(c.cv, b); val w = f.combo(c.cw, b)
                    val t = f.plus(f.scale(c.a, v), f.scale(c.b, w))
                    assertEquals("seed $seed", c.answer, f.coords(b, t))
                }
            }
            val a = coordsLcExercise(Random(seed)).parts.single()(emptyList()).answer as Answer.Numbers
            assertEquals(when (val c = coordsLcCase(Random(seed))) { is CoordsLcCase.Named -> c.answer; is CoordsLcCase.Given -> c.answer }, a.expected)
        }
    }

    /** The set of four vectors: the check accepts a set exactly when its triples are bases as in the example's. */
    @Test fun set_with_given_bases_judged_by_its_triples() {
        val triples = listOf(listOf(0, 1, 2), listOf(0, 1, 3), listOf(0, 2, 3), listOf(1, 2, 3))
        repeat(150) { seed ->
            val ex = setBasesExercise(Random(seed))
            val a = ex.parts[0](emptyList()).answer as Answer.Vectors
            val p = a.p!!
            fun pattern(zs: List<List<Int>>) = triples.map { t -> lnBrute(p, t.map { zs[it] }) }
            val want = pattern(a.example.map { ints(it[0]) })
            assertTrue("seed $seed: a basis and a non-basis asked", want.any { it } && want.any { !it })
            val r = Random(seed)
            repeat(40) {
                val zs = List(4) { r.vec(p, 3) }
                assertEquals("seed $seed: $zs", pattern(zs) == want, a.check(zs.map { listOf(fr(it)) }).ok)
            }
            // the count asked next: brute force over the example
            val count = ex.parts[1](listOf(a.example)).answer as Answer.Numbers
            val target = Regex("vytvořit vektor \\$\\(([0-9,]+)\\)").find(ex.prompt)!!.groupValues[1].split(',').map { it.toInt() }
            assertEquals(tuples(p, 4).count { combo(p, it, a.example.map { e -> ints(e[0]) }) == target }.toLong(), count.expected[0].num)
        }
    }

    /** The worked examples of the lessons (Lessons.kt), recomputed: their numbers are not typed by hand. */
    @Test fun worked_examples_in_the_lessons() {
        // test 15. 11. 2022 (A): ((2,1,1),(2,2,α),(3,3,2)) is a basis of Z_5^3 for α in {0,1,2,4}
        val a1 = paramZpDecide(5, ParamKind.BASIS, listOf(fr(listOf(2, 1, 1)), fr(listOf(3, 3, 2))), fr(listOf(2, 2, 0)), 2, 0)
        assertEquals(setOf(0, 1, 2, 4), a1.truth); assertEquals(a1.truth, a1.symbolic)
        // test 9. 11. 2022 (6A): x = (0,4), y = (2,3)
        val sols = tuples(5, 4).filter { t -> combo(5, listOf(2, 1), t.chunked(2)) == listOf(2, 1) && combo(5, listOf(4, 4), t.chunked(2)) == listOf(3, 3) }
        assertEquals(listOf(listOf(0, 4, 2, 3)), sols)
        // test listopad 2022, príklad 5: rows (1,1,0,0 | 0), (1,0,0,2 | 1) over Z_3
        assertTrue(judgeSystemRows(Field.Zp(3), fr(listOf(1, 2, 2, 0)), fr(listOf(2, 1, 1, 1)), listOf(listOf(fr(listOf(1, 1, 0, 0, 0))), listOf(fr(listOf(1, 0, 0, 2, 1))))).ok)
        // test 15. 11. 2022 (9A): z = e1, e2, e3, e3 over Z_3, (1,1,1) in 3 ways
        val z = listOf(listOf(1, 0, 0), listOf(0, 1, 0), listOf(0, 0, 1), listOf(0, 0, 1))
        assertEquals(3, tuples(3, 4).count { combo(3, it, z) == listOf(1, 1, 1) })
        assertEquals(listOf(true, true, false, false), listOf(listOf(0, 1, 2), listOf(0, 1, 3), listOf(0, 2, 3), listOf(1, 2, 3)).map { t -> lnBrute(3, t.map { z[it] }) })
    }

    /** Every listing exercise is solved by its own example and asks for a list the student writes, not a pick. */
    @Test fun listings_are_the_students_own_lists() {
        for (gen in listOf("obal-vypis", "zp-zoznam")) repeat(100) { seed ->
            val ex = Generators.getValue(gen)(Random(seed))
            val a = ex.parts.single()(emptyList()).answer as Answer.Vectors
            assertTrue("$gen: an open list", a.open)
            assertTrue("$gen/$seed: ${a.check(a.example).head}", a.check(a.example).ok)
            assertTrue("$gen/$seed: room for a wrong extra entry", a.count.last > a.example.size)
        }
    }
}

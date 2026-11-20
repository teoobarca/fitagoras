package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.la.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * The two drills of Chapter4Drills.kt against brute force, read from what the student sees (the prompt): over Z_p both
 * solution sets are enumerated, over R the two conditions of Věta 4.18 bod 4 are checked by rank; the verdict, the
 * rank asked and the reason marked right must agree. The list of points of a line is judged against every vector of
 * Z_p^n, for random lists with repeats, outsiders and gaps.
 */
class Chapter4DrillsTest {
    private fun parts(ex: Exercise): List<Answer> {
        val answers = ArrayList<Any>()
        return ex.parts.map { b ->
            val a = b(answers).answer
            answers.add(when (a) { is Answer.Choice -> a.correct; is Answer.Numbers -> a.expected; is Answer.Vectors -> a.example; else -> Unit })
            a
        }
    }

    private val vecRe = Regex("""\(([-\d,]+)\)""")
    private fun vecs(s: String) = vecRe.findAll(s).map { m -> m.groupValues[1].split(",").map { Frac.of(it.toLong()) } }.toList()

    @Test fun equal_solution_sets_agree_with_brute_force() {
        val kinds = IntArray(3); var zp = 0; var r = 0
        repeat(300) { seed ->
            val ex = k4RovnostExercise(Random(seed))
            val (num, ch) = parts(ex).let { it[0] as Answer.Numbers to it[1] as Answer.Choice }
            // read S1, S2 back from the prompt: u, y.., v, z..
            val body = ex.prompt.substringAfter("\\begin{aligned}").substringBefore("\\end{aligned}")
            val (l1, l2) = body.split("\\\\")
            val v1 = vecs(l1); val v2 = vecs(l2)
            val p = Regex("""mathbb\{Z\}_\{(\d+)\}""").find(ex.prompt)?.groupValues?.get(1)?.toInt()
            val f: Field = if (p != null) Field.Zp(p) else Field.R
            val u = v1[0]; val y = v1.drop(1); val v = v2[0]; val z = v2.drop(1)
            val k = y.size
            assertTrue(f.isLN(y) && f.isLN(z) && z.size == k)
            val d = u.indices.map { f.sub(u[it], v[it]) }
            assertEquals(f.rank(y + z + listOf(d)), num.expected[0].num.toInt())
            val equal: Boolean; val sameDir: Boolean
            if (f is Field.Zp) {
                zp++
                val s1 = f.spanElements(y).map { s -> f.plus(u, s) }.toSet()
                val s2 = f.spanElements(z).map { s -> f.plus(v, s) }.toSet()
                equal = s1 == s2
                sameDir = f.spanElements(y) == f.spanElements(z)
            } else {
                r++
                sameDir = f.rank(y + z) == k
                equal = sameDir && f.inSpan(y, d)
            }
            val expected = if (equal) 0 else if (!sameDir) 1 else 2
            assertEquals("seed $seed", expected, ch.correct)
            assertEquals("seed $seed rank verdict", equal, num.expected[0] == Frac.of(k.toLong()))
            kinds[expected]++
            ex.solution(listOf(ch.correct))
        }
        assertTrue("every verdict and both fields drawn: ${kinds.toList()}, Z_p $zp, R $r", kinds.all { it > 30 } && zp > 50 && r > 50)
    }

    @Test fun line_points_judge_matches_brute_force() {
        repeat(300) { seed ->
            val ex = k4BodyExercise(Random(seed))
            val ps = parts(ex)
            val list = ps[0] as Answer.Vectors
            val cnt = ps.getOrNull(1) as Answer.Numbers?
            val p = list.p!!; val f = Field.Zp(p); val n = list.cells.size
            val a = vecs(ex.prompt.substringAfter("mathbf{a}="))[0]; val u = vecs(ex.prompt.substringAfter("mathbf{u}="))[0]
            val space = FiniteSpace(p, n)
            val all = space.all().map { it.frac() }
            val w = all.filter { x -> f.inSpan(listOf(u), x.indices.map { f.sub(x[it], a[it]) }) }.toSet()
            assertEquals(p, w.size)
            assertEquals(w, list.example.map { it[0] }.toSet())
            val r = Random(seed)
            // the judge against brute force: random lists of vectors, ok iff exactly W with no repeat
            repeat(200) {
                val size = r.nextInt(1, p + 4)
                val pick = if (r.nextBoolean()) w.shuffled(r).take(size) + List(maxOf(0, size - p)) { all.random(r) } else List(size) { if (r.nextInt(3) == 0) all.random(r) else w.random(r) }
                val res = judgeLinePoints(f, a, u, pick.map { listOf(it) })
                val truth = pick.toSet() == w && pick.size == w.size
                assertEquals("seed $seed $pick", truth, res.ok)
                if (!truth) assertTrue(res.head.isNotBlank())
                // each entry flagged iff it is outside W or a repeat
                pick.forEachIndexed { i, x -> assertEquals("seed $seed entry $i", x !in w || pick.take(i).contains(x), res.each[i] != null) }
            }
            assertTrue(list.check(list.example).ok)
            assertTrue(!list.check(list.example.drop(1)).ok)
            // the plane: count its points
            assertEquals(n == 3, cnt != null)
            if (cnt == null) { ex.solution(emptyList()); return@repeat }
            val wv = vecs(ex.parts[1](listOf(list.example)).q.substringAfter("mathbf{w}="))[0]
            val plane = (0 until p).flatMap { al -> (0 until p).map { be -> f.plus(f.plus(a, f.scale(Frac.of(al.toLong()), u)), f.scale(Frac.of(be.toLong()), wv)) } }.toSet()
            assertEquals(plane.size.toLong(), cnt.expected[0].num)
            ex.solution(emptyList())
        }
    }
}

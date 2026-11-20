package dev.hamper.tabula.data.la1

import dev.hamper.tabula.la.*

/*
 * Exact matrix work for kapitola 3 and 4 of the textbook: products, transpose, Gauss-Jordan to the identity and the inverse
 * from (A|E), rank with a parameter, the full solution set of a system (Frobeniova veta) and the change between the
 * parametric and the nonparametric form of a variety. Everything the chapter shows or checks is computed here, over R as
 * exact rationals or over Z_p.
 */

fun Mat.transpose(): Mat = Mat(List(n) { j -> rows.map { it[j] } })

fun identity(n: Int): Mat = Mat(List(n) { i -> List(n) { j -> if (i == j) Frac.ONE else Frac.ZERO } })

fun Field.mat(rows: List<List<Int>>, bar: Int? = null): Mat = Mat(rows.map { r -> r.map { norm(it.q()) } }, bar)

/** A·B by the definition: (AB)_ij = sum_k a_ik b_kj. */
fun Field.times(a: Mat, b: Mat): Mat {
    require(a.n == b.m) { "rozmery nesedia" }
    return Mat(a.rows.map { r -> (0 until b.n).map { j -> r.indices.fold(Frac.ZERO) { acc, k -> add(acc, mul(r[k], b[k, j])) } } })
}

/** A·x for a column x written as a vector. */
fun Field.mulVec(a: Mat, x: Vec): Vec = a.rows.map { r -> r.indices.fold(Frac.ZERO) { acc, k -> add(acc, mul(r[k], x[k])) } }

fun Field.rankOf(a: Mat): Int = echelon(Mat(a.rows)).rank

/** (A|B) side by side, the bar before B. */
fun beside(a: Mat, b: Mat): Mat = Mat(a.rows.indices.map { a.rows[it] + b.rows[it] }, a.n)

fun Mat.left(k: Int): Mat = Mat(rows.map { it.take(k) })
fun Mat.right(k: Int): Mat = Mat(rows.map { it.drop(k) })

/**
 * Gauss-Jordan as in the proof of Věta 3.35: GEM to the upper echelon form, every pivot row divided by its pivot,
 * then zeros above the pivots from the last pivot up. Steps as they go on paper.
 */
data class Jordan(val start: Mat, val steps: List<GemStep>, val result: Mat, val pivots: List<Int>, val forward: Int, val pole: Field? = null)

fun Field.jordan(start: Mat, pivotLimit: Int = start.n): Jordan {
    val e = echelon(start, pivotLimit)
    val piv = e.pivotsLeft
    val rows = e.result.rows.map { it.toMutableList() }.toMutableList()
    val steps = ArrayList(e.steps)
    val scale = piv.mapIndexedNotNull { r, c -> val v = rows[r][c]; if (v == Frac.ONE) null else Scale(r, inv(v)) }
    for (op in scale) rows[op.i] = rows[op.i].map { mul(op.k, it) }.toMutableList()
    if (scale.isNotEmpty()) steps += GemStep(scale, Mat(rows.map { it.toList() }, start.bar))
    for (k in piv.indices.reversed()) {
        val c = piv[k]
        val ops = ArrayList<RowOp>()
        for (r in 0 until k) {
            val q = rows[r][c]
            if (q.isZero) continue
            val op = Combine(r, Frac.ONE, k, neg(q))
            rows[r] = rows[r].indices.map { j -> add(rows[r][j], mul(op.b, rows[k][j])) }.toMutableList()
            ops += op
        }
        if (ops.isNotEmpty()) steps += GemStep(ops, Mat(rows.map { it.toList() }, start.bar))
    }
    return Jordan(start, steps, Mat(rows.map { it.toList() }, start.bar), piv, e.steps.size, this)
}

/** Postup 3.37: (A|E) ~ (E|A^-1), or null when the left block has a zero row in the echelon form (A singular). */
fun Field.inverse(a: Mat): Mat? {
    if (a.m != a.n) return null
    val j = jordan(beside(a, identity(a.n)), a.n)
    if (j.pivots.size < a.n) return null
    return j.result.right(a.n)
}

/* ---- Frobeniova veta: the whole solution set ---------------------------------------------------------------- */

/**
 * The solution set of (A|b) by Postup 4.10: GEM, bound unknowns at the pivot columns, free ones [free]; [particular]
 * with all free unknowns zero, [kernel] one vector per free unknown (that one 1, the others 0), Postup 4.5.
 * [particular] is null when h(A) < h(A|b).
 */
data class SystemSolution(val e: Echelon, val rankA: Int, val rankAb: Int, val bound: List<Int>, val free: List<Int>, val particular: Vec?, val kernel: List<Vec>)

fun Field.solveAll(a: Mat, b: Vec): SystemSolution {
    val n = a.n
    val e = echelon(Mat(a.rows.indices.map { a.rows[it] + listOf(norm(b[it])) }, n), n)
    val bound = e.pivotsLeft
    val free = (0 until n).filter { it !in bound }
    val rankA = bound.size
    val rankAb = e.rank
    fun back(freeValues: Map<Int, Frac>, rhs: Boolean): Vec {
        val x = MutableList(n) { freeValues[it] ?: Frac.ZERO }
        for (k in bound.indices.reversed()) {
            val row = e.result.rows[k]; val c = bound[k]
            var s = if (rhs) row[n] else Frac.ZERO
            for (j in c + 1 until n) s = sub(s, mul(row[j], x[j]))
            x[c] = div(s, row[c])
        }
        return x
    }
    val particular = if (e.consistent) back(emptyMap(), true) else null
    val kernel = free.map { f -> back(free.associateWith { if (it == f) Frac.ONE else Frac.ZERO }, false) }
    return SystemSolution(e, rankA, rankAb, bound, free, particular, kernel)
}

/** A basis of the solutions of (M|θ), Postup 4.5. */
fun Field.kernelBasis(m: Mat): List<Vec> = solveAll(m, List(m.m) { Frac.ZERO }).kernel

/** Postup 4.30: a system (A|b) whose solutions are a + <us>. The rows of A are a basis of the solutions of (U|θ). */
fun Field.equationsOf(a: Vec, us: List<Vec>): Pair<Mat, Vec> {
    val n = a.size
    val k = rank(us)
    return when {
        us.isEmpty() || k == 0 -> identity(n) to a
        k == n -> Mat(listOf(List(n) { Frac.ZERO })) to listOf(Frac.ZERO)
        else -> {
            // over R every row times the lcm of its denominators: still a basis of the solutions, equations without fractions
            fun gcd(x: Long, y: Long): Long = if (y == 0L) kotlin.math.abs(x) else gcd(y, x % y)
            val rows = kernelBasis(Mat(us)).map { r -> if (this != Field.R) r else r.fold(1L) { l, x -> l / gcd(l, x.den) * x.den }.let { l -> r.map { it * Frac.of(l) } } }
            val am = Mat(rows); am to mulVec(am, a)
        }
    }
}

/** Is x a solution of A x = b. */
fun Field.solves(a: Mat, b: Vec, x: Vec): Boolean = mulVec(a, x).let { y -> y.indices.all { y[it] == norm(b[it]) } }

/* ---- rank with a parameter a over R ------------------------------------------------------------------------ */

/**
 * h(A) as a function of a. GEM with constant pivots only (every step valid for every a), then the rest: rows without a
 * pivot, in the columns without one. h(A) = pivots + h(rest). The rest has at most two rows or two columns, so its rank
 * changes only where an entry or a 2×2 minor vanishes; those values are [special] with their rank, [generic] elsewhere.
 */
data class ParamRank(val start: PMat, val steps: List<PStep>, val result: PMat, val pivots: Int, val pivotCols: List<Int>, val restRows: List<Int>, val restCols: List<Int>, val generic: Int, val special: Map<Frac, Int>)

fun paramRank(rows: List<PVec>): ParamRank? {
    val m = rows.size; val n = rows[0].size
    val start = PMat(rows)
    val w = rows.map { it.toMutableList() }.toMutableList()
    val steps = ArrayList<PStep>()
    val pivotCols = ArrayList<Int>()
    var top = 0
    for (col in 0 until n) {
        if (top >= m) break
        val cand = (top until m).filter { w[it][col].isConst && !w[it][col].isZero }
        if (cand.isEmpty()) continue
        val ops = ArrayList<RowOp>()
        val best = cand.minBy { r -> val v = w[r][col].constValue; if (v == Frac.ONE) -1.0 else if (v == -Frac.ONE) -.5 else kotlin.math.abs(v.num.toDouble() / v.den) }
        if (best != top) { val t = w[top]; w[top] = w[best]; w[best] = t; ops += Swap(top, best) }
        val p = w[top][col].constValue
        for (r in top + 1 until m) {
            val q = w[r][col]
            if (q.isZero) continue
            if (q.isConst && (q.constValue / p).isInt) {
                val k = -(q.constValue / p)
                w[r] = w[r].indices.map { j -> w[r][j] + w[top][j] * k }.toMutableList()
                ops += Combine(r, Frac.ONE, top, k)
            } else {
                val pa = if (p < Frac.ZERO) -p else p
                val qa = if (p < Frac.ZERO) q else -q
                w[r] = w[r].indices.map { j -> w[r][j] * pa + w[top][j] * qa }.toMutableList()
                ops += if (qa.isConst) Combine(r, pa, top, qa.constValue) else PolyCombine(r, pa, top, qa)
            }
        }
        if (ops.isNotEmpty()) steps += PStep(ops, PMat(w.map { it.toList() }))
        pivotCols += col
        top++
    }
    val restRows = (top until m).toList()
    val restCols = (0 until n).filter { it !in pivotCols }
    val rest = restRows.map { r -> restCols.map { w[r][it] } }
    if (restRows.size > 2 && restCols.size > 2) return null
    val polys = ArrayList<Poly>()
    rest.forEach { r -> r.forEach { if (!it.isZero) polys += it } }
    for (r1 in rest.indices) for (r2 in r1 + 1 until rest.size) for (c1 in restCols.indices) for (c2 in c1 + 1 until restCols.size) {
        val d = rest[r1][c1] * rest[r2][c2] - rest[r1][c2] * rest[r2][c1]
        if (!d.isZero) polys += d
    }
    val cands = polys.flatMap { it.rationalRoots() ?: return null }.distinct().sorted()
    fun rankAt(a: Frac) = top + Field.R.rankOf(Mat(rest.map { r -> r.map { it.eval(a) } }).let { if (it.rows.isEmpty() || it.n == 0) Mat(listOf(listOf(Frac.ZERO))) else it })
    var probe = Frac.of(1000, 7)
    while (probe in cands) probe += Frac.ONE
    val generic = rankAt(probe)
    val special = cands.associateWith { rankAt(it) }.filter { it.value != generic }
    return ParamRank(start, steps, PMat(w.map { it.toList() }), top, pivotCols, restRows, restCols, generic, special)
}

/** The plain rank of A(a) for one value, straight from the definition through GEM: the independent check. */
fun rankAt(rows: List<PVec>, a: Frac): Int = Field.R.rankOf(Mat(rows.map { r -> r.map { it.eval(a) } }))

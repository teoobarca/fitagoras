package dev.hamper.tabula.la

/** A polynomial in the parameter a with rational coefficients: c[i] is the coefficient of a^i. */
data class Poly(val c: List<Frac>) {
    companion object {
        fun const(x: Frac) = Poly(listOf(x)).trim()
        fun of(x: Int) = const(x.q())
        /** b + k·a */
        fun lin(b: Int, k: Int) = Poly(listOf(b.q(), k.q())).trim()
        val A = lin(0, 1)
    }

    fun trim(): Poly { var k = c.size; while (k > 0 && c[k - 1].isZero) k--; return Poly(c.take(k)) }
    val isZero get() = c.all { it.isZero }
    val degree get() = trim().c.size - 1
    val isConst get() = degree <= 0
    val constValue get() = trim().c.firstOrNull() ?: Frac.ZERO

    operator fun plus(o: Poly) = Poly(List(maxOf(c.size, o.c.size)) { (c.getOrNull(it) ?: Frac.ZERO) + (o.c.getOrNull(it) ?: Frac.ZERO) }).trim()
    operator fun unaryMinus() = Poly(c.map { -it })
    operator fun minus(o: Poly) = this + (-o)
    operator fun times(o: Poly): Poly {
        if (isZero || o.isZero) return Poly(emptyList())
        val r = MutableList(c.size + o.c.size - 1) { Frac.ZERO }
        for (i in c.indices) for (j in o.c.indices) r[i + j] = r[i + j] + c[i] * o.c[j]
        return Poly(r).trim()
    }
    operator fun times(k: Frac) = Poly(c.map { it * k }).trim()

    fun eval(a: Frac): Frac = c.foldRight(Frac.ZERO) { coef, acc -> acc * a + coef }

    /** Rational roots of a polynomial of degree 1 or 2. Null when we can't say (irrational or higher degree). */
    fun rationalRoots(): List<Frac>? {
        val p = trim()
        return when (p.degree) {
            -1 -> null // zero polynomial: every a
            0 -> emptyList()
            1 -> listOf(-p.c[0] / p.c[1])
            2 -> {
                val (z, b, a2) = p.c
                val disc = b * b - 4.q() * a2 * z
                if (disc < Frac.ZERO) return emptyList()
                val s = sqrtExact(disc) ?: return null
                listOf((-b - s) / (2.q() * a2), (-b + s) / (2.q() * a2)).distinct().sorted()
            }
            else -> null
        }
    }

    /** "2a-3", "a+1", "-a", "5" */
    fun tex(v: String = "a"): String {
        val p = trim()
        if (p.c.isEmpty()) return "0"
        val b = StringBuilder()
        for (i in p.c.indices.reversed()) {
            val k = p.c[i]
            if (k.isZero) continue
            val neg = k < Frac.ZERO
            val abs = if (neg) -k else k
            if (b.isEmpty()) { if (neg) b.append("-") } else b.append(if (neg) "-" else "+")
            val coef = if (i > 0 && abs == Frac.ONE) "" else abs.tex(true)
            b.append(coef)
            if (i == 1) b.append(v) else if (i > 1) b.append("$v^{$i}")
        }
        return b.toString()
    }
}

private fun sqrtExact(x: Frac): Frac? {
    fun isq(n: Long): Long? { val r = kotlin.math.sqrt(n.toDouble()).toLong(); return (maxOf(0, r - 1)..r + 1).firstOrNull { it * it == n } }
    val n = isq(x.num) ?: return null
    val d = isq(x.den) ?: return null
    return Frac.of(n, d)
}

typealias PVec = List<Poly>

data class PMat(val rows: List<PVec>, val bar: Int? = null) {
    fun tex(): String {
        val spec = if (bar == null) "" else "|$bar"
        return "\\begin{pmatrix$spec}" + rows.joinToString("\\\\") { r -> r.joinToString("&") { it.tex() } } + "\\end{pmatrix}"
    }
    fun at(a: Frac) = Mat(rows.map { r -> r.map { it.eval(a) } }, bar)
}

data class PStep(val ops: List<RowOp>, val after: PMat)

enum class ParamKind { LN, BASIS, GENERATES, SPAN }

/**
 * "For which a ..." over R. GEM with constant pivots only (so every step is valid for every a), until the rest is
 * a single column or row of polynomials in a: those decide.
 */
data class ParamResult(
    val kind: ParamKind, val start: PMat, val steps: List<PStep>, val result: PMat,
    /** Order of the vectors in the columns (indexes into the given list): the one with a goes last. */
    val order: List<Int>,
    /** The polynomials that decide, read from the rest of the matrix. */
    val deciders: List<Poly>,
    /** The values of a where the deciders are all zero. */
    val critical: List<Frac>,
    /** True when the property holds exactly for a in [critical] (SPAN); false when it holds for a outside it. */
    val holdsOnCritical: Boolean,
) {
    fun holds(a: Frac) = (a in critical) == holdsOnCritical
}

fun paramSolve(kind: ParamKind, vs: List<PVec>, x: PVec? = null): ParamResult? {
    val n = vs[0].size
    val m = vs.size
    // LN does not depend on the order (Pozorování 2.25): the vector with a goes last, so pivots stay numbers
    val order = if (kind == ParamKind.SPAN) vs.indices.toList() else vs.indices.sortedBy { i -> if (vs[i].any { !it.isConst }) 1 else 0 }
    val ordered = order.map { vs[it] }
    val cols = if (kind == ParamKind.SPAN) ordered + listOf(x!!) else ordered
    val start = PMat((0 until n).map { j -> cols.map { it[j] } }, if (kind == ParamKind.SPAN) m else null)
    val rows = start.rows.map { it.toMutableList() }.toMutableList()
    val steps = ArrayList<PStep>()
    var top = 0
    val pivotCols = ArrayList<Int>()
    for (col in 0 until m) {
        if (top >= n) break
        val cand = (top until n).filter { rows[it][col].isConst && !rows[it][col].isZero }
        if (cand.isEmpty()) continue // a free column, or one only a can decide: left for the end
        val ops = ArrayList<RowOp>()
        val best = cand.minBy { r -> val v = rows[r][col].constValue; if (v == Frac.ONE) -1.0 else if (v == -Frac.ONE) -.5 else kotlin.math.abs(v.num.toDouble() / v.den) }
        if (best != top) { val t = rows[top]; rows[top] = rows[best]; rows[best] = t; ops += Swap(top, best) }
        val p = rows[top][col].constValue
        for (r in top + 1 until n) {
            val q = rows[r][col]
            if (q.isZero) continue
            val op: Combine
            val newRow: List<Poly>
            if (q.isConst && (q.constValue / p).isInt) {
                val k = -(q.constValue / p)
                op = Combine(r, Frac.ONE, top, k)
                newRow = rows[r].indices.map { j -> rows[r][j] + rows[top][j] * k }
            } else {
                // R_r := |p| R_r - sign(p) q R_top: valid for every a, the factor of R_r is a nonzero number
                val pa = if (p < Frac.ZERO) -p else p
                val qa = if (p < Frac.ZERO) q else -q
                if (!qa.isConst) {
                    rows[r] = rows[r].indices.map { j -> rows[r][j] * pa + rows[top][j] * qa }.toMutableList()
                    ops += PolyCombine(r, pa, top, qa)
                    continue
                }
                op = Combine(r, pa, top, qa.constValue)
                newRow = rows[r].indices.map { j -> rows[r][j] * pa + rows[top][j] * qa }
            }
            rows[r] = newRow.toMutableList()
            ops += op
        }
        if (ops.isNotEmpty()) steps += PStep(ops, PMat(rows.map { it.toList() }, start.bar))
        pivotCols += col
        top++
    }
    // a row left with the parameter that is a number times an earlier such row: (G3) with that number clears it, so
    // the last matrix is in HST (two rows "0 0 -2a-7" would both start in the same column)
    run {
        val ops = ArrayList<RowOp>()
        for (r in top + 1 until n) {
            if (rows[r].all { it.isZero }) continue
            for (s in top until r) {
                if (rows[s].all { it.isZero }) continue
                val j = rows[s].indexOfFirst { !it.isZero }
                if (rows[r][j].isZero) continue
                val k = rows[r][j].c.last() / rows[s][j].c.last()
                if (rows[r].indices.any { !(rows[r][it] - rows[s][it] * k).isZero }) continue
                rows[r] = rows[r].indices.map { c -> rows[r][c] + rows[s][c] * -k }.toMutableList()
                ops += Combine(r, Frac.ONE, s, -k)
                break
            }
        }
        if (ops.isNotEmpty()) steps += PStep(ops, PMat(rows.map { it.toList() }, start.bar))
    }
    val rest = rows.drop(top)
    val free = (0 until m).filter { it !in pivotCols }
    val deciders: List<Poly>
    val holdsOnCritical: Boolean
    when (kind) {
        ParamKind.BASIS, ParamKind.LN -> {
            if (kind == ParamKind.BASIS && m != n) return null
            // LN exactly when the one column without a pivot gets one: it decides when all of it is zero
            if (free.size != 1) return null
            deciders = rest.map { it[free[0]] }
            holdsOnCritical = false
        }
        ParamKind.GENERATES -> {
            // one row left: generates exactly when it isn't all zero
            if (rest.size != 1) return null
            deciders = rest[0].take(m)
            holdsOnCritical = false
        }
        ParamKind.SPAN -> {
            // the left part of the rest must be zero; then x is in the span exactly when its column is zero there
            if (rest.any { r -> (0 until m).any { !r[it].isZero } }) return null
            deciders = rest.map { it[m] }
            holdsOnCritical = true
        }
    }
    if (deciders.isEmpty()) return null
    val nonzero = deciders.filter { !it.isZero }
    if (nonzero.isEmpty()) return null // fails or holds for every a: not a parameter problem
    val sets = nonzero.map { it.rationalRoots() ?: return null }
    val critical = sets.reduce { acc, s -> acc.filter { it in s } }
    return ParamResult(kind, start, steps, PMat(rows.map { it.toList() }, start.bar), order, deciders, critical, holdsOnCritical)
}

/** ř_t := a ř_t + q(a) ř_s, the multiple of the row above being a polynomial in a; written as the course does. */
data class PolyCombine(val target: Int, val a: Frac, val src: Int, val q: Poly) : RowOp {
    override fun tex(): String {
        val t = rowTex(target)
        val first = if (a == Frac.ONE) t else "${a.tex(true)}\\cdot $t"
        val neg = q.c.lastOrNull()?.let { it < Frac.ZERO } == true
        val body = (if (neg) -q else q).tex()
        return "$first${if (neg) "-" else "+"}($body)\\cdot ${rowTex(src)}"
    }
    override val kind get() = if (a == Frac.ONE) "G3" else "G2, G3"
}

/** Independent check by plain rank at a given a: what the textbook definition says, no shortcut. */
fun paramHoldsByRank(kind: ParamKind, vs: List<PVec>, x: PVec?, a: Frac): Boolean {
    val f = Field.R
    val nv = vs.map { v -> v.map { it.eval(a) } }
    return when (kind) {
        ParamKind.LN -> f.isLN(nv)
        ParamKind.BASIS -> f.isLN(nv) && f.rank(nv) == nv[0].size
        ParamKind.GENERATES -> f.rank(nv) == nv[0].size
        ParamKind.SPAN -> f.inSpan(nv, x!!.map { it.eval(a) })
    }
}

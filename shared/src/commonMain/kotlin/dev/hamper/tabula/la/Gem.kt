package dev.hamper.tabula.la

/*
 * One row operation, written as the course writes it (skriptá LA1, kap. 1 a 3; konvencie 15. 11.): the kind (G1), (G2),
 * (G3) and the operation with "ř" and the row number, "ř2 - 2\cdot ř1", "ř1 \leftrightarrow ř3". Rows are numbered from 1.
 */
sealed interface RowOp {
    /** The operation, TeX: "\text{ř}2-2\cdot\text{ř}1". */
    fun tex(): String
    /** The kind of operation in the skriptá: "G1", "G2", "G3" (or "G2, G3" for a multiple of the row itself plus another). */
    val kind: String
}

/** "ř2" as TeX. */
fun rowTex(i: Int) = "\\text{ř}${i + 1}"

/** k·ř as the course writes a multiple of a row: "2\cdot ř1", "ř1", "\tfrac{1}{3}\cdot ř2" (sign handled by the caller). */
private fun multiple(k: Frac, row: Int) = if (k == Frac.ONE) rowTex(row) else "${k.tex(true)}\\cdot ${rowTex(row)}"

data class Swap(val i: Int, val j: Int) : RowOp {
    override fun tex() = "${rowTex(i)}\\leftrightarrow ${rowTex(j)}"
    override val kind get() = "G1"
}

/** ř_t := a ř_t + b ř_s. With a = 1 it is (G3), "subtract a multiple". */
data class Combine(val target: Int, val a: Frac, val src: Int, val b: Frac) : RowOp {
    override fun tex(): String {
        val first = if (a < Frac.ZERO) "-" + multiple(-a, target) else multiple(a, target)
        val second = if (b < Frac.ZERO) "-" + multiple(-b, src) else "+" + multiple(b, src)
        return first + second
    }
    override val kind get() = if (a == Frac.ONE) "G3" else "G2, G3"
}

/**
 * A step written on paper whose operations nobody named (the tutor shows a student's own chain): no kind, no note.
 * Never computed with.
 */
data class Nenapisana(val note: String = "") : RowOp {
    override fun tex() = note
    override val kind get() = ""
}

data class Scale(val i: Int, val k: Frac) : RowOp {
    override fun tex() = if (k < Frac.ZERO) "-" + multiple(-k, i) else multiple(k, i)
    override val kind get() = "G2"
}

/** The operations of one GEM step as a note: "(G3) ř2-2·ř1, ř3-ř1", the kind once per run of the same kind. */
fun opsNote(ops: List<RowOp>): String {
    val out = StringBuilder()
    var last: String? = null
    for (o in ops) {
        if (out.isNotEmpty()) out.append(", ")
        if (o.kind != last) { out.append("(${o.kind}) "); last = o.kind }
        out.append("$").append(o.tex()).append("$")
    }
    return out.toString()
}

/** A matrix with an optional bar before column [bar] (an augmented matrix). */
data class Mat(val rows: List<Vec>, val bar: Int? = null) {
    val m get() = rows.size
    val n get() = rows.firstOrNull()?.size ?: 0
    operator fun get(i: Int, j: Int) = rows[i][j]

    fun tex(): String {
        val spec = if (bar == null) "" else "|$bar"
        return "\\begin{pmatrix$spec}" + rows.joinToString("\\\\") { r -> r.joinToString("&") { it.tex(true) } } + "\\end{pmatrix}"
    }

    companion object {
        /** Vectors as columns: the system α1 x1 + ... + αm xm = rhs, as the textbook writes it (Observation 2.30). */
        fun columns(vs: List<Vec>, rhs: List<Vec> = emptyList()): Mat {
            val all = vs + rhs
            return Mat(all[0].indices.map { j -> all.map { it[j] } }, if (rhs.isEmpty()) null else vs.size)
        }
        fun rows(vs: List<Vec>) = Mat(vs)
    }
}

/** One "~" on paper: the operations done at once and the matrix after them. */
data class GemStep(val ops: List<RowOp>, val after: Mat)

/** [pole]: the field it was computed in, so a walk made from it knows its numbers (the tutor checks paper against it). */
data class Echelon(val start: Mat, val steps: List<GemStep>, val result: Mat, val pivots: List<Int>, val pole: Field? = null) {
    /** Pivot columns left of the bar: these are the "hlavné stĺpce" among the unknowns. */
    val pivotsLeft get() = pivots.filter { result.bar == null || it < result.bar }
    val rank get() = pivots.size
    /** With a bar: a pivot in a right-hand column means no solution. */
    val consistent get() = result.bar == null || pivots.none { it >= result.bar }
}

/**
 * GEM to row echelon form, step by step. Over R it stays in integers when it can (R_t := pR_t - qR_s), the way
 * people do it by hand; over Z_p it subtracts the right multiple mod p.
 */
fun Field.echelon(start: Mat, pivotLimit: Int = start.n): Echelon {
    val rows = start.rows.map { r -> r.map { norm(it) }.toMutableList() }.toMutableList()
    val steps = ArrayList<GemStep>()
    val pivots = ArrayList<Int>()
    var top = 0
    for (col in 0 until pivotLimit) {
        if (top >= rows.size) break
        val candidates = (top until rows.size).filter { !rows[it][col].isZero }
        if (candidates.isEmpty()) continue
        val ops = ArrayList<RowOp>()
        // a one is the friendliest pivot, then the smallest number
        val best = candidates.minBy { r -> val v = rows[r][col]; if (v == Frac.ONE) -1.0 else if (v == -Frac.ONE) -.5 else kotlin.math.abs(v.num.toDouble() / v.den) }
        if (best != top) {
            val t = rows[top]; rows[top] = rows[best]; rows[best] = t
            ops += Swap(top, best)
        }
        val p = rows[top][col]
        for (r in top + 1 until rows.size) {
            val q = rows[r][col]
            if (q.isZero) continue
            val op: Combine = if (this is Field.Zp || (q / p).isInt) {
                Combine(r, Frac.ONE, top, neg(div(q, p)))
            } else {
                // R_r := pR_r - qR_top, kept free of fractions; the sign makes the first factor positive
                val pa = if (p < Frac.ZERO) -p else p
                val qa = if (p < Frac.ZERO) q else -q
                Combine(r, pa, top, qa)
            }
            rows[r] = rows[r].indices.map { j -> add(mul(op.a, rows[r][j]), mul(op.b, rows[top][j])) }.toMutableList()
            ops += op
        }
        pivots += col
        top++
        if (ops.isNotEmpty()) steps += GemStep(ops, Mat(rows.map { it.toList() }, start.bar))
    }
    // pivots right of the limit (a right-hand side that sticks out)
    if (pivotLimit < start.n) {
        for (r in top until rows.size) {
            val c = (pivotLimit until start.n).firstOrNull { !rows[r][it].isZero }
            if (c != null) { pivots += c; break }
        }
    }
    return Echelon(start, steps, Mat(rows.map { it.toList() }, start.bar), pivots, this)
}

fun Field.rank(vs: List<Vec>): Int = echelon(Mat.rows(vs)).rank
fun Field.isLN(vs: List<Vec>): Boolean = vs.isNotEmpty() && rank(vs) == vs.size
fun Field.inSpan(vs: List<Vec>, x: Vec): Boolean = rank(vs + listOf(x)) == rank(vs)

/**
 * Solves A·c = b from the vectors (columns) and the right side. Null when there is no solution; a particular one
 * (free unknowns zero) otherwise.
 */
fun Field.solve(vs: List<Vec>, b: Vec): Vec? {
    val e = echelon(Mat.columns(vs, listOf(b)), vs.size)
    if (!e.consistent) return null
    return backSub(e.result, e.pivotsLeft, vs.size, b = true)
}

/** A nontrivial combination equal to θ, or null if the set is LN. */
fun Field.dependency(vs: List<Vec>): Vec? {
    val e = echelon(Mat.columns(vs), vs.size)
    val free = (0 until vs.size).firstOrNull { it !in e.pivotsLeft } ?: return null
    // free unknown = 1, the other free ones 0, back-substitute the pivots
    val x = MutableList(vs.size) { Frac.ZERO }
    x[free] = Frac.ONE
    val piv = e.pivotsLeft
    for (k in piv.indices.reversed()) {
        val r = e.result.rows[k]; val c = piv[k]
        var s = Frac.ZERO
        for (j in c + 1 until vs.size) s = add(s, mul(r[j], x[j]))
        x[c] = div(neg(s), r[c])
    }
    return x
}

private fun Field.backSub(m: Mat, piv: List<Int>, unknowns: Int, b: Boolean): Vec {
    val x = MutableList(unknowns) { Frac.ZERO }
    for (k in piv.indices.reversed()) {
        val r = m.rows[k]; val c = piv[k]
        var s = if (b) r[unknowns] else Frac.ZERO
        for (j in c + 1 until unknowns) s = sub(s, mul(r[j], x[j]))
        x[c] = div(s, r[c])
    }
    return x
}

/** Coordinates of x with respect to a basis: the unique solution (Theorem 2.66). */
fun Field.coords(basis: List<Vec>, x: Vec): Vec? = solve(basis, x)

/** The vector with the given coordinates. */
fun Field.fromCoords(basis: List<Vec>, c: Vec): Vec = combo(c, basis)

/** Theorem 2.59: the original vectors behind the pivot columns form a basis of the span. */
fun Field.pickBasis(vs: List<Vec>): List<Int> = echelon(Mat.columns(vs)).pivots

/** The vectors and after them the whole standard basis, as columns: Theorem 2.60 "na jistotu" as Klouda teaches it (6th lecture). */
fun completionMatrix(vs: List<Vec>): Mat = Mat.columns(vs + vs[0].indices.map { unit(vs[0].size, it) })

/** Theorem 2.60 done with columns: GEM of [completionMatrix]; the pivot columns among e_1..e_n complete the set to a basis. */
fun Field.completeWithUnitVectors(vs: List<Vec>): List<Int> {
    val e = echelon(completionMatrix(vs))
    require(e.pivots.take(vs.size) == vs.indices.toList()) { "súbor nie je LN" }
    return e.pivots.filter { it >= vs.size }.map { it - vs.size }
}

fun unit(n: Int, j: Int): Vec = List(n) { if (it == j) Frac.ONE else Frac.ZERO }

fun pow(p: Int, d: Int): Long = (1..d).fold(1L) { acc, _ -> acc * p }

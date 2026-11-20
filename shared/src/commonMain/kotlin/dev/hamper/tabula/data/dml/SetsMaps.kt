package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.gcd
import dev.hamper.tabula.la.q

/*
 * Maps, decided exactly. Finite maps are drawn as arrows between small sets (Definice 5.7 to 5.13). Maps on Z^k and
 * R^k given by an affine formula are decided by integer linear algebra: injective iff the columns are independent,
 * surjective onto Z^l iff the l×l minors have gcd 1 (for l = k: determinant ±1), onto R^l iff the rank is l.
 */

/* ---- Arrows between small sets ---------------------------------------------------------------------------------- */

/** Arrows a → b drawn between A = 0 until [a] and B = 0 until [b]. Not necessarily a map. */
data class Arrows(val a: Int, val b: Int, val arrows: Set<Pair<Int, Int>>) {
    fun out(x: Int) = arrows.filter { it.first == x }.map { it.second }
    fun into(y: Int) = arrows.filter { it.second == y }.map { it.first }
    /** Definice 5.7: every a has at most one image. */
    val isMap get() = (0 until a).all { out(it).size <= 1 }
    /** Totální: every a has exactly one image. In BI-DML a map is total unless said otherwise. */
    val isTotal get() = (0 until a).all { out(it).size == 1 }
    val injective get() = isTotal && (0 until b).all { into(it).size <= 1 }
    val surjective get() = isTotal && (0 until b).all { into(it).isNotEmpty() }
    val bijective get() = injective && surjective
    /** The map as an array, for total maps. */
    fun table(): IntArray { require(isTotal); return IntArray(a) { out(it).single() } }
}

fun arrowsOf(f: IntArray, b: Int) = Arrows(f.size, b, f.withIndex().map { it.index to it.value }.toSet())

/** f[M] = {f(a) | a ∈ M} (Definice 5.8). */
fun image(f: IntArray, m: Set<Int>): Set<Int> = m.map { f[it] }.toSet()
/** f^{-1}[N] = {a ∈ A | f(a) ∈ N} (Definice 5.8). */
fun preimage(f: IntArray, n: Set<Int>): Set<Int> = f.indices.filter { f[it] in n }.toSet()
/** (f ∘ g)(a) = f(g(a)) for g: A → B, f: B → C (Definice 5.10). */
fun compose(f: IntArray, g: IntArray): IntArray = IntArray(g.size) { f[g[it]] }
fun injective(f: IntArray) = f.toSet().size == f.size
fun surjective(f: IntArray, b: Int) = f.toSet().size == b
/** The inverse when f is a bijection (Věta 5.4), else null. */
fun inverse(f: IntArray, b: Int): IntArray? = if (f.size == b && injective(f)) IntArray(b) { y -> f.indexOf(y) } else null

/** Number of maps A → B with |A| = a, |B| = b and the given property, by brute force. */
fun countMaps(a: Int, b: Int, ok: (IntArray) -> Boolean): Int {
    var n = 0
    val f = IntArray(a)
    fun rec(i: Int) { if (i == a) { if (ok(f)) n++; return }; for (y in 0 until b) { f[i] = y; rec(i + 1) } }
    rec(0)
    return n
}

/* ---- Affine maps on Z^k or R^k ---------------------------------------------------------------------------------- */

/** f(x) = M x + c with integer entries, from Z^k (or R^k when [real]) to Z^l (or R^l). Rows are the output coordinates. */
data class Affine(val m: List<List<Long>>, val c: List<Long>, val real: Boolean) {
    val k get() = m[0].size
    val l get() = m.size
    fun at(x: List<Long>): List<Long> = m.mapIndexed { i, row -> row.indices.sumOf { j -> row[j] * x[j] } + c[i] }
    fun atQ(x: List<Frac>): List<Frac> = m.mapIndexed { i, row -> row.indices.fold(c[i].q()) { s, j -> s + row[j].q() * x[j] } }
    val rank: Int get() = rankOf(m)
    val injective: Boolean get() = rank == k
    val surjective: Boolean get() = if (real) rank == l else (rank == l && minorsGcd() == 1L)
    /** gcd of all l×l minors; the image of Z^k is all of Z^l iff it is 1. */
    fun minorsGcd(): Long {
        var g = 0L
        for (cols in combos(k, l)) g = gcd(g, kotlin.math.abs(det(m.map { r -> cols.map { r[it] } })))
        return g
    }
}

fun det(a: List<List<Long>>): Long = when (a.size) {
    1 -> a[0][0]
    2 -> a[0][0] * a[1][1] - a[0][1] * a[1][0]
    else -> a[0].indices.sumOf { j -> (if (j % 2 == 0) 1 else -1) * a[0][j] * det(a.drop(1).map { r -> r.filterIndexed { i, _ -> i != j } }) }
}

fun combos(n: Int, r: Int): List<List<Int>> = if (r == 0) listOf(emptyList()) else (0 until n).flatMap { i -> combos(n, r - 1).filter { it.isEmpty() || it[0] > i }.map { listOf(i) + it } }.filter { it.size == r }

fun rankOf(m: List<List<Long>>): Int {
    val a = m.map { r -> r.map { it.q() }.toMutableList() }.toMutableList()
    var rank = 0
    for (col in a[0].indices) {
        val p = (rank until a.size).firstOrNull { !a[it][col].isZero } ?: continue
        a[rank] = a[p].also { a[p] = a[rank] }
        for (i in a.indices) if (i != rank && !a[i][col].isZero) {
            val f = a[i][col] / a[rank][col]
            a[i] = a[i].indices.map { j -> a[i][j] - f * a[rank][j] }.toMutableList()
        }
        rank++
    }
    return rank
}

/** A nonzero integer vector d with M d = 0 (two points with the same image: x and x + d), or null when injective. */
fun Affine.kernel(): List<Long>? {
    if (injective) return null
    fun norm(d: List<Long>): List<Long> { val g = d.fold(0L) { s, v -> gcd(s, kotlin.math.abs(v)) }; val v = d.map { it / g }; return if (v.first { it != 0L } < 0) v.map { -it } else v }
    val cands = buildList {
        for (row in m) {
            if (k == 2) add(listOf(row[1], -row[0]))
            if (k == 3) { add(listOf(row[1], -row[0], 0L)); add(listOf(row[2], 0L, -row[0])); add(listOf(0L, row[2], -row[1])) }
        }
        if (k == 3 && l >= 2) add(listOf(m[0][1] * m[1][2] - m[0][2] * m[1][1], m[0][2] * m[1][0] - m[0][0] * m[1][2], m[0][0] * m[1][1] - m[0][1] * m[1][0]))
    }
    return cands.filter { d -> d.any { it != 0L } && m.all { row -> row.indices.sumOf { row[it] * d[it] } == 0L } }.map(::norm)
        .minByOrNull { d -> d.sumOf { kotlin.math.abs(it) } } ?: error("kernel of $this not found")
}

/** For square maps with det ≠ 0: the unique rational preimage of y. */
fun Affine.preimageQ(y: List<Frac>): List<Frac> {
    require(k == l && det(m) != 0L)
    val d = det(m).q()
    // Cramer
    return (0 until k).map { j ->
        val mj = m.indices.map { i -> m[i].indices.map { jj -> if (jj == j) y[i] - c[i].q() else m[i][jj].q() } }
        detQ(mj) / d
    }
}

private fun detQ(a: List<List<Frac>>): Frac = when (a.size) {
    1 -> a[0][0]
    2 -> a[0][0] * a[1][1] - a[0][1] * a[1][0]
    else -> a[0].indices.fold(Frac.ZERO) { s, j -> val t = a[0][j] * detQ(a.drop(1).map { r -> r.filterIndexed { i, _ -> i != j } }); if (j % 2 == 0) s + t else s - t }
}

/**
 * A point of Z^l without an integer preimage when the map is not onto Z^l, chosen small; null if surjective.
 * Proof that it has none: the rational preimage is unique and not integral (det ≠ 0), or every image satisfies an
 * integer relation the point breaks (rank < l), or the image values are all multiples of g > 1 (l = 1).
 */
fun Affine.noPreimage(): List<Long>? {
    if (surjective) return null
    val r = 3L
    fun vecs(n: Int): Sequence<List<Long>> = if (n == 0) sequenceOf(emptyList()) else vecs(n - 1).flatMap { v -> (-r..r).asSequence().map { v + it } }
    return vecs(l).sortedBy { v -> v.sumOf { kotlin.math.abs(it) } * 10 + v.count { it < 0 } }.firstOrNull { provablyNoPreimage(it) }
}

/**
 * True only when y provably has no preimage in Z^k (R^k when real): y is off the rational image (rank grows), or the
 * unique rational preimage is not integral (square, det ≠ 0), or y - c is not a multiple of the gcd g (l = 1).
 */
fun Affine.provablyNoPreimage(y: List<Long>): Boolean {
    val t = y.indices.map { y[it] - c[it] }
    if (rankOf(m.mapIndexed { i, row -> row + t[i] }) != rank) return true
    if (real) return false
    if (k == l && det(m) != 0L) return !preimageQ(y.map { it.q() }).all { it.isInt }
    if (l == 1) { val g = m[0].fold(0L) { s, v -> gcd(s, kotlin.math.abs(v)) }; return if (g == 0L) t[0] != 0L else t[0] % g != 0L }
    return false
}

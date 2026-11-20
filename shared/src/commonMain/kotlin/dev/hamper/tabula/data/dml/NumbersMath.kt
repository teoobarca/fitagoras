package dev.hamper.tabula.data.dml.numbers

import kotlin.math.abs
import dev.hamper.tabula.compat.JMath

/*
 * Number theory for the DML templates, exact: Long with overflow checks (Math.*Exact throws instead of giving a
 * wrong answer). Every function follows the textbook's procedure (Skriptá DML, kapitola 6) so that its steps can be
 * shown as the solution and checked one by one. Moduli in the templates stay below 10^4, so products fit easily;
 * exponents are never expanded, powers go by square and multiply.
 */

/** a mod m in {0, ..., m-1}, also for negative a (Definice 6.2). */
fun md(a: Long, m: Long): Long { require(m >= 1); val r = a % m; return if (r < 0) r + m else r }

fun gcd(a: Long, b: Long): Long { var x = abs(a); var y = abs(b); while (y != 0L) { val t = x % y; x = y; y = t }; return x }
fun lcm(a: Long, b: Long): Long = if (a == 0L || b == 0L) 0 else JMath.multiplyExact(abs(a) / gcd(a, b), abs(b))

/** One row of the REA table: r_i = alpha_i * a + beta_i * b, and q_i = floor(r_{i-1} / r_i) (null in the first row and in the zero row). */
data class ReaRow(val r: Long, val alpha: Long, val beta: Long, val q: Long?)

/**
 * The REA table of Poznámka 6.2 (REA tabulkou) for a >= b > 0, rows down to r_{k+1} = 0. The zero row keeps its
 * alpha, beta (the matrix form shows them) but the table leaves them out.
 */
data class Rea(val a: Long, val b: Long, val rows: List<ReaRow>) {
    /** The last nonzero row, k-th: gcd and the Bézout coefficients. */
    val last get() = rows[rows.size - 2]
    val gcd get() = last.r
    val alpha get() = last.alpha
    val beta get() = last.beta
}

fun rea(a: Long, b: Long): Rea {
    require(a >= b && b > 0) { "REA chce a >= b > 0" }
    val rows = ArrayList<ReaRow>()
    rows += ReaRow(a, 1, 0, null)
    rows += ReaRow(b, 0, 1, a / b)
    while (true) {
        val p = rows[rows.size - 2]; val c = rows[rows.size - 1]
        val q = c.q!!
        val r = p.r - q * c.r
        val al = JMath.subtractExact(p.alpha, JMath.multiplyExact(q, c.alpha))
        val be = JMath.subtractExact(p.beta, JMath.multiplyExact(q, c.beta))
        if (r == 0L) { rows += ReaRow(0, al, be, null); break }
        rows += ReaRow(r, al, be, c.r / r)
    }
    return Rea(a, b, rows)
}

/** Linear Diophantine ax + by = c (Věta 6.7, 6.8): null when gcd(a,b) does not divide c. */
data class Ldr(val a: Long, val b: Long, val c: Long, val g: Long, val x0: Long, val y0: Long) {
    /** All solutions: (x0 + sx k, y0 + sy k). */
    val sx get() = b / g
    val sy get() = -a / g
    /** The solution with the smallest x >= 0: a canonical answer, whatever particular solution one started from. */
    val xMin get() = md(x0, abs(sx))
    val yMin get() = (c - a * xMin) / b
}

/**
 * Solves ax + by = c for a, b > 0 by the REA on (max, min) as the textbook does, the coefficients put back in the
 * order of the equation (Příklad 6.5 swaps the columns the same way).
 */
fun ldr(a: Long, b: Long, c: Long): Ldr? {
    require(a > 0 && b > 0)
    val t = if (a >= b) rea(a, b) else rea(b, a)
    val (al, be) = if (a >= b) t.alpha to t.beta else t.beta to t.alpha
    val g = t.gcd
    if (c % g != 0L) return null
    val k = c / g
    return Ldr(a, b, c, g, JMath.multiplyExact(al, k), JMath.multiplyExact(be, k))
}

/**
 * The multiplicative inverse of a in Z_m (Věta 6.12) by the REA on (m, a mod m): the coefficient at a, reduced.
 * Null when gcd(a, m) != 1.
 */
fun inverse(a: Long, m: Long): Long? {
    val r = md(a, m)
    if (r == 0L) return if (m == 1L) 0 else null
    val t = rea(m, r)
    if (t.gcd != 1L) return null
    return md(t.beta, m)
}

/** Prime factorisation, ascending: n = prod p^e. */
fun factor(n: Long): List<Pair<Long, Int>> {
    require(n >= 1)
    val out = ArrayList<Pair<Long, Int>>()
    var x = n; var p = 2L
    while (p * p <= x) {
        if (x % p == 0L) { var e = 0; while (x % p == 0L) { x /= p; e++ }; out += p to e }
        p++
    }
    if (x > 1) out += x to 1
    return out
}

fun isPrime(n: Long) = n >= 2 && factor(n).let { it.size == 1 && it[0].second == 1 }

/** Euler phi from the factorisation (Věta 6.16). */
fun phi(n: Long): Long = factor(n).fold(1L) { acc, (p, e) -> acc * (pow(p, e - 1) * (p - 1)) }

fun pow(b: Long, e: Int): Long { var r = 1L; repeat(e) { r = JMath.multiplyExact(r, b) }; return r }

/** b^e mod m by square and multiply (Příklad 6.16); e may be huge, nothing is expanded. */
fun powMod(b: Long, e: Long, m: Long): Long {
    require(e >= 0 && m >= 1)
    var base = md(b, m); var x = e; var r = 1L % m
    while (x > 0) {
        if (x and 1L == 1L) r = JMath.multiplyExact(r, base) % m
        base = JMath.multiplyExact(base, base) % m
        x = x shr 1
    }
    return r
}

/** The squares b, b^2, b^4, ... mod m up to the highest power of two <= e: the table of the binary method. */
fun squares(b: Long, e: Long, m: Long): List<Pair<Long, Long>> {
    val out = ArrayList<Pair<Long, Long>>()
    var k = 1L; var v = md(b, m)
    while (k <= e) { out += k to v; v = JMath.multiplyExact(v, v) % m; if (k > Long.MAX_VALUE / 2) break; k *= 2 }
    return out
}

/** Linear congruence ax ≡ b (mod m), Věta 6.17 and 6.18: all solutions are x ≡ x0 (mod n), n = m / gcd(a, m). */
data class Lkon(val a: Long, val b: Long, val m: Long, val g: Long, val x0: Long, val n: Long) {
    /** The gcd(a, m) solutions in Z_m. */
    val inZm get() = (0 until g).map { x0 + it * n }
}

fun lkon(a: Long, b: Long, m: Long): Lkon? {
    require(m >= 2)
    val g = gcd(a, m)
    if (md(b, g) != 0L) return null
    val n = m / g
    if (n == 1L) return Lkon(a, b, m, g, 0, 1)
    val inv = inverse(md(a / g, n), n)!!
    return Lkon(a, b, m, g, md(JMath.multiplyExact(inv, md(b / g, n)), n), n)
}

/**
 * A system x ≡ r_i (mod m_i) by substitution (Příklad 6.20), which works with or without coprime moduli (ZČVOZ,
 * Věta 6.20). Null when it has no solution; else x ≡ x (mod lcm).
 */
data class Crt(val x: Long, val mod: Long)

fun crt(parts: List<Pair<Long, Long>>): Crt? {
    var x = 0L; var mod = 1L
    for ((r, m) in parts) {
        // x + mod*t ≡ r (mod m)  ->  mod*t ≡ r - x (mod m)
        if (m == 1L) continue
        if (mod == 1L) { x = md(r, m); mod = m; continue }
        val s = lkon(mod, r - x, m) ?: return null
        val newMod = lcm(mod, m)
        x = md(x + JMath.multiplyExact(mod, s.x0), newMod)
        mod = newMod
    }
    return Crt(x, mod)
}

package dev.hamper.tabula.la

import kotlin.math.abs
import dev.hamper.tabula.compat.JMath

/**
 * Exact numbers for everything the app computes. A rational for R (the tests only ever use rationals in R), and the
 * same type holding 0..p-1 for Z_p. Overflow throws instead of giving a wrong answer.
 */
data class Frac private constructor(val num: Long, val den: Long) : Comparable<Frac> {
    companion object {
        val ZERO = Frac(0, 1)
        val ONE = Frac(1, 1)
        fun of(n: Long, d: Long = 1): Frac {
            require(d != 0L) { "delenie nulou" }
            if (n == 0L) return ZERO
            val g = gcd(abs(n), abs(d))
            val s = if (d < 0) -1 else 1
            return Frac(s * n / g, s * d / g)
        }
        fun of(n: Int) = of(n.toLong())
    }

    operator fun plus(o: Frac) = of(JMath.addExact(JMath.multiplyExact(num, o.den), JMath.multiplyExact(o.num, den)), JMath.multiplyExact(den, o.den))
    operator fun minus(o: Frac) = this + (-o)
    operator fun times(o: Frac) = of(JMath.multiplyExact(num, o.num), JMath.multiplyExact(den, o.den))
    operator fun div(o: Frac): Frac { require(!o.isZero) { "delenie nulou" }; return of(JMath.multiplyExact(num, o.den), JMath.multiplyExact(den, o.num)) }
    operator fun unaryMinus() = Frac(-num, den)
    val isZero get() = num == 0L
    val isInt get() = den == 1L
    override fun compareTo(other: Frac) = (num * other.den).compareTo(other.num * den)

    /** "3", "-4/3": plain, for logs, tests and TalkBack. */
    override fun toString() = if (den == 1L) "$num" else "$num/$den"

    /** In a formula: an integer as it is, a fraction as \frac with the sign in front. */
    fun tex(small: Boolean = false): String = when {
        den == 1L -> "$num"
        else -> (if (num < 0) "-" else "") + (if (small) "\\tfrac" else "\\frac") + "{${abs(num)}}{$den}"
    }
}

fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

fun Int.q() = Frac.of(this)
fun Long.q() = Frac.of(this)

/** The field the numbers live in. R is exact rationals; Z_p keeps every result in 0..p-1. */
sealed class Field {
    abstract fun norm(x: Frac): Frac
    abstract val tex: String
    abstract val say: String
    fun add(a: Frac, b: Frac) = norm(a + b)
    fun sub(a: Frac, b: Frac) = norm(a - b)
    fun mul(a: Frac, b: Frac) = norm(a * b)
    fun neg(a: Frac) = norm(-a)
    abstract fun inv(a: Frac): Frac
    fun div(a: Frac, b: Frac) = mul(a, inv(b))
    open val finite: Int? get() = null

    data object R : Field() {
        override fun norm(x: Frac) = x
        override fun inv(a: Frac) = Frac.ONE / a
        override val tex = "\\mathbb{R}"
        override val say = "R"
    }

    data class Zp(val p: Int) : Field() {
        init { require(p in 2..97 && (2 until p).none { p % it == 0 }) { "p musí byť prvočíslo" } }
        override val finite get() = p
        fun of(n: Long): Frac = Frac.of(JMath.floorMod(n, p.toLong()))
        override fun norm(x: Frac): Frac {
            if (x.isInt) return of(x.num)
            // n/d in Z_p is n times the inverse of d
            return mul(of(x.num), inv(of(x.den)))
        }
        override fun inv(a: Frac): Frac {
            val v = norm(a).num.toInt()
            require(v != 0) { "nula nemá inverz" }
            return Frac.of((1 until p).first { it * v % p == 1 }.toLong())
        }
        override val tex get() = "\\mathbb{Z}_{$p}"
        override val say get() = "Z $p"
    }
}

typealias Vec = List<Frac>

fun vec(vararg xs: Int): Vec = xs.map { it.q() }
fun Vec.tex(): String = "(" + joinToString(",") { it.tex(small = true) } + ")"
fun Vec.isZero() = all { it.isZero }
fun Field.vec(xs: List<Int>): Vec = xs.map { norm(it.q()) }
fun Field.combo(coefs: List<Frac>, vs: List<Vec>): Vec = vs[0].indices.map { j -> coefs.indices.fold(Frac.ZERO) { acc, i -> add(acc, mul(coefs[i], vs[i][j])) } }
fun Field.scale(k: Frac, v: Vec): Vec = v.map { mul(k, it) }
fun Field.plus(a: Vec, b: Vec): Vec = a.indices.map { add(a[it], b[it]) }

package dev.hamper.tabula.data

import dev.hamper.tabula.la.Frac
import kotlin.jvm.JvmName

/*
 * The one formatter for coefficients, signs and linear combinations in generated TeX. Every generator writes numbers
 * next to letters through it, so no text shows "1x", "-1x", "0x", "+ -", "- -", "x^1", "1(…)" or "·1"
 * (MathStyleTest scans every generator's text for them). Numbers stay exact: Frac or Long.
 */
object Terms {
    /** A number as a factor or an operand after an operator: negative in parentheses, "(-3)". */
    fun paren(c: Frac): String = if (c < Frac.ZERO) "(${c.tex(true)})" else c.tex(true)
    fun paren(c: Long): String = if (c < 0) "($c)" else "$c"

    /**
     * [c] times [x] as the first term of an expression: "x", "-x", "2x", "\tfrac{1}{2}x", "0" for c = 0. A compound
     * [x] (a sum, "x_1+x_2") gets parentheses when a coefficient stands before it: "2(x_1+x_2)", "-(x_1+x_2)".
     * A number or a tuple [x] ("7", "(1,2)", a matrix) gets a dot: "3\cdot 7", "2\cdot(1,2)"; a bracketed sum
     * "(u_1+v_1)" does not: "2(u_1+v_1)".
     */
    fun times(c: Frac, x: String): String {
        if (c.isZero) return "0"
        val body = if (compound(x)) "($x)" else x
        val dot = x.firstOrNull()?.isDigit() == true || Regex("-\\d.*").matches(x) || x.startsWith("\\begin") || (x.startsWith("(") && ',' in x) || x.startsWith("(-")
        return when {
            c == Frac.ONE -> x
            c == -Frac.ONE -> if (x.startsWith("-")) "-($x)" else "-$body"
            dot -> "${c.tex(true)}\\cdot " + (if (x.startsWith("-")) "($x)" else x)
            else -> c.tex(true) + body
        }
    }
    fun times(c: Long, x: String) = times(Frac.of(c), x)
    fun times(c: Int, x: String) = times(Frac.of(c), x)

    /** [c]·[x] as a later term of a sum, with its sign: "+2x", "-x", "" for c = 0. */
    fun plus(c: Frac, x: String): String = when {
        c.isZero -> ""
        c < Frac.ZERO -> times(c, x)
        else -> "+" + times(c, x)
    }
    fun plus(c: Long, x: String) = plus(Frac.of(c), x)
    fun plus(c: Int, x: String) = plus(Frac.of(c), x)

    /** A constant term after others: "+3", "-3", "" for 0. */
    fun plus(c: Frac): String = if (c.isZero) "" else if (c < Frac.ZERO) c.tex(true) else "+" + c.tex(true)
    fun plus(c: Long): String = plus(Frac.of(c))
    fun plusConst(c: Int): String = plus(Frac.of(c))

    /** A linear combination Σ c_i·x_i: zero terms dropped, signs merged, "0" when all are zero. */
    fun lin(cs: List<Frac>, xs: List<String>): String {
        require(cs.size == xs.size)
        val b = StringBuilder()
        for (i in cs.indices) {
            if (cs[i].isZero) continue
            b.append(if (b.isEmpty()) times(cs[i], xs[i]) else plus(cs[i], xs[i]))
        }
        return b.ifEmpty { "0" }.toString()
    }
    @JvmName("linLong") fun lin(cs: List<Long>, xs: List<String>) = lin(cs.map { Frac.of(it) }, xs)
    @JvmName("linInt") fun lin(cs: List<Int>, xs: List<String>) = lin(cs.map { Frac.of(it) }, xs)

    /**
     * Terms joined into one expression: each term is written as if alone ("3x", "-2y", "5", "0"); signs merge and
     * zero terms drop. sum("3x", "-2y", "0", "5") = "3x-2y+5".
     */
    fun sum(vararg terms: String): String = sum(terms.toList())
    fun sum(terms: List<String>): String {
        val b = StringBuilder()
        for (t0 in terms) {
            val t = t0.trim()
            if (t.isEmpty() || t == "0" || t == "+0" || t == "-0") continue
            if (b.isEmpty()) b.append(t.removePrefix("+"))
            else if (t.startsWith("-") || t.startsWith("+")) b.append(t) else b.append("+").append(t)
        }
        return b.ifEmpty { "0" }.toString()
    }

    /** [base] to the power [e]: "1" for 0, the base for 1, "b^{e}" else; a compound or negative base in parentheses. */
    fun pow(base: String, e: Int): String = when (e) {
        0 -> "1"
        1 -> base
        else -> (if (compound(base) || base.startsWith("-")) "($base)" else base) + "^{$e}"
    }
    fun pow(base: Long, e: Int): String = pow(if (base < 0) "($base)" else "$base", e)
    fun pow(base: Long, e: String): String = pow(if (base < 0) "($base)" else "$base", e)
    /** A symbolic exponent ("n", "n+1", "2n-1"); "1" and "0" fall back to the numeric rule. */
    fun pow(base: String, e: String): String = e.trim().toIntOrNull()?.let { pow(base, it) }
        ?: ((if (compound(base) || base.startsWith("-")) "($base)" else base) + "^{$e}")

    /** True for an expression that needs parentheses before a factor: a sum or difference at the top level. */
    fun compound(x: String): Boolean {
        var depth = 0
        for ((i, ch) in x.withIndex()) {
            when (ch) {
                '{', '(', '[' -> depth++
                '}', ')', ']' -> depth--
                '+', '-' -> if (depth == 0 && i > 0) return true
            }
        }
        return false
    }
}

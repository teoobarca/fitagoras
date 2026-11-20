package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.q
import kotlin.random.Random
import dev.hamper.tabula.compat.JMath

/*
 * Generators of Matematická indukce (BI-DML, 2nd test). Every claim is drawn so that it is true, and true is checked
 * here by computing both sides exactly for the first values of n; nothing a student is told is typed by hand.
 * Shapes: sums and divisibility as in Cvičení 4.1, 4.2 and 4.4 of the textbook, matrix powers as the 2nd tests of
 * 2023 to 2025, a two-step recurrence as the tests of 2022 and 2023.
 */

private fun Random.pickOf(vararg xs: Int) = xs[nextInt(xs.size)]

internal fun miIpow(b: Long, e: Int): Long { require(e >= 0); var r = 1L; repeat(e) { r = JMath.multiplyExact(r, b) }; return r }
private fun gcdL(a: Long, b: Long): Long = if (b == 0L) kotlin.math.abs(a) else gcdL(b, a % b)

/** Weak induction as the course writes it (Dombek, Skriptá kapitola 4): the claim, ZK, IK with the IP marked, the conclusion. */
val MiWeakPostup = postup(
    "Tvrdenie §V(n)§" to "Napíš, čo dokazuješ, ako výrok §V(n)§ a od ktorého §n§ má platiť.",
    "Základný krok (ZK)" to "Over §V§ pre prvé §n§, od ktorého má tvrdenie platiť (zvyčajne §n=1§); obe strany zvlášť.",
    "Indukčný krok (IK)" to "Pre pevné ľubovoľné §n§ predpokladaj §V(n)§ (IP) a dokáž §V(n+1)§; vyznač, kde použiješ IP.",
    "Záver" to "Podľa princípu MI platí §V(n)§ pre všetky §n§ od toho, kde začal ZK.",
)

/** A matrix power: guess from the first powers, then weak induction. */
val MiMatrixPostup = postup(
    "Odhad z mocnín" to "Spočítaj §A^2§ a §A^3§ a hľadaj, čo sa mení s §n§; odhad napíš ako §V(n)§.",
    "Základný krok (ZK)" to "Over, že odhad pre §n=1§ dá maticu §A§.",
    "Indukčný krok (IK)" to "§A^{n+1}=A^n\\cdot A§, za §A^n§ dosaď odhad z IP a vynásob.",
    "Záver" to "Podľa princípu MI platí odhad pre všetky §n\\in\\mathbb{N}§.",
)

/** Strong induction (a recurrence, coins): as many base cases as the step reaches back. */
val MiStrongPostup = postup(
    "Tvrdenie §V(n)§" to "Napíš tvrdenie ako výrok §V(n)§ a od ktorého §n§ má platiť.",
    "Základné kroky" to "Over toľko prvých hodnôt, koľko krok siaha späť; pri rekurencii z dvoch členov §n=1§ a §n=2§.",
    "Indukčný krok, silná indukcia" to "Predpokladaj §V(k)§ pre všetky §k§ od začiatku po §n§ a dokáž §V(n+1)§; vyznač, kde použiješ IP.",
    "Záver" to "Podľa silného princípu MI platí §V(n)§ pre všetky §n§ od toho, kde začali základné kroky.",
)

/** a·v + b: "3n-2", "n+1", "-n", "2n", "5" (the shared formatter). */
internal fun miLin(a: Long, b: Long, v: String = "n"): String = Terms.sum(Terms.times(a, v), "$b")

/** base^{exp} with the base in brackets when negative; base 1 is 1. */
internal fun miPow(base: Long, exp: String): String = if (base == 1L) "1" else Terms.pow(base, exp)

/** k·expr with the sign in front: "3\cdot 2^{n}", "-2^{n}", "2^{n}". */
internal fun miTimes(k: Long, expr: String): String = Terms.times(k, expr)

/** "+ x" or "- x" to append a signed term written by [miTimes]. */
private fun plusTerm(t: String) = if (t.startsWith("-")) t else "+$t"

private fun <T> Random.shuffled(correct: T, wrong: List<T>): Pair<List<T>, Int> {
    val all = (listOf(correct) + wrong).shuffled(this)
    return all to all.indexOf(correct)
}

/* ---- 1. Sums: Σ f(k) = F(n) --------------------------------------------------------------------------------------- */

internal class MiSumFamily(
    val f: (Long) -> Frac,
    val big: (Long) -> Frac,
    /** f(k) written with the variable [v]. */
    val fTex: (String) -> String,
    /** f(n+1), simplified. */
    val fNext: String,
    /** f(k) needs brackets after a sum sign. */
    val paren: Boolean,
    val bigTex: String,
    val nextTex: String,
    /** F(n+1) with n+1 put in only part of the places: the most common slip. */
    val partialTex: String,
    val partialWhy: String,
    /** Another wrong end of the step. */
    val thirdTex: String,
    val thirdWhy: String,
    /** F(n) + f(n+1) over a common denominator, before it is shown to be F(n+1). */
    val middle: List<String>,
    val fractions: Boolean,
    val src: String,
)

private fun factorTex(a: Long, b: Long, v: String) = if (b == 0L) miLin(a, 0, v) else "(" + miLin(a, b, v) + ")"

internal fun miLinSum(r: Random): MiSumFamily? = miLinSumOf(r.pick(1, 5).toLong(), r.pick(-2, 4).toLong())

/** §\sum_{k=1}^{n}(ak+b)§; a = 1, b = 0 is §1+2+\dots+n§ of Příklad 4.9. Null when the closed form degenerates. */
internal fun miLinSumOf(a: Long, b: Long): MiSumFamily? {
    val c = a + 2 * b
    // F(n) = n(an + c)/2; written over 1 when a is even
    val even = a % 2 == 0L
    val ia = if (even) a / 2 else a; val ib = if (even) c / 2 else c
    if (ib == 0L || ia + ib == 0L || a + b == 0L) return null
    fun big(pre: String, x: Long, y: Long) = if (even) "$pre(${miLin(x, y)})" else "\\frac{$pre(${miLin(x, y)})}{2}"
    val c2 = a; val c1 = 3 * a + 2 * b; val c0 = 2 * a + 2 * b
    val poly = miPoly(listOf(c2, c1, c0))
    return MiSumFamily(
        f = { k -> (a * k + b).q() }, big = { n -> Frac.of(n * (a * n + c), 2) },
        fTex = { v -> miLin(a, b, v) }, fNext = miLin(a, a + b), paren = true,
        bigTex = big("n", ia, ib), nextTex = big("(n+1)", ia, ia + ib), partialTex = big("(n+1)", ia, ib),
        partialWhy = "Za §n§ treba dosadiť §n+1§ všade, aj do druhej zátvorky.",
        middle = listOf(
            (if (even) big("n", ia, ib) else "\\frac{n(${miLin(a, c)})}{2}") + "+(" + miLin(a, a + b) + ")",
            if (even) miPoly(listOf(c2 / 2, c1 / 2, c0 / 2)) else "\\frac{$poly}{2}",
        ),
        thirdTex = big("n", ia, ib) + "+1", thirdWhy = PlusOne,
        fractions = false, src = "Skriptá, Cvičenie 4.4 a), Príklad 4.9",
    )
}

internal fun miGeoSum(r: Random): MiSumFamily {
    val q = r.pickOf(2, 3, 4, 5).toLong(); val c = r.pick(1, 4).toLong()
    val g = gcdL(c, q - 1); val cc = c / g; val dd = (q - 1) / g
    fun big(e: String): String {
        val num = if (cc == 1L) "$q^{$e}-1" else "$cc($q^{$e}-1)"
        return if (dd == 1L) num else "\\frac{$num}{$dd}"
    }
    val wrongNum = if (cc == 1L) "$q^{n}+1-1" else "$cc($q^{n}+1-1)"
    return MiSumFamily(
        f = { k -> (c * miIpow(q, (k - 1).toInt())).q() }, big = { n -> Frac.of(c * (miIpow(q, n.toInt()) - 1), q - 1) },
        fTex = { v -> miTimes(c, "$q^{$v-1}") }, fNext = miTimes(c, "$q^{n}"), paren = false,
        bigTex = big("n"), nextTex = big("n+1"),
        partialTex = if (dd == 1L) wrongNum else "\\frac{$wrongNum}{$dd}",
        partialWhy = "Exponent sa zvýši o jedna: §$q^{n+1}§, nie §$q^{n}+1§.",
        middle = if (q == 2L) listOf(
            (if (c == 1L) "2^{n}-1" else "$c(2^{n}-1)") + "+" + miTimes(c, "2^{n}"),
            if (c == 1L) "2\\cdot 2^{n}-1" else "$c(2\\cdot 2^{n}-1)",
        ) else listOf(
            "\\frac{${Terms.times(c, "($q^{n}-1)")}}{${q - 1}}+" + miTimes(c, "$q^{n}"),
            "\\frac{${Terms.times(c, "($q^{n}-1)")}+${q - 1}\\cdot ${Terms.times(c, "$q^{n}")}}{${q - 1}}",
            "\\frac{${Terms.times(c, "($q\\cdot $q^{n}-1)")}}{${q - 1}}",
        ),
        thirdTex = "$q\\cdot" + (if (dd == 1L && cc == 1L) "(${big("n")})" else if (dd == 1L) big("n") else big("n")),
        thirdWhy = "Celé §F(n)§ krát §$q§ nie je §F(n+1)§. Pre §n=1§ by vyšlo §${q * c}§, ale §F(2)=${c + c * q}§. Dosaď §n+1§ do vzorca, nenásob ho.",
        fractions = false, src = "Geometrický súčet, šablóna v štýle Cvičení 4.4",
    )
}

internal fun miTeleSum(r: Random): MiSumFamily {
    val a = r.pick(1, 3).toLong(); val b = r.pick((1 - a).toInt(), 3).toLong()
    val s = a + b // ≥ 1
    fun den(x: Long, y: Long): String {
        val f2 = miLin(x, y)
        return if (s == 1L) f2 else "$s($f2)"
    }
    val e2 = a; val e1 = 2 * a + b; val e0 = s
    val pre = if (s == 1L) "" else "$s"
    return MiSumFamily(
        f = { k -> Frac.of(1, (a * k + b) * (a * k + a + b)) }, big = { n -> Frac.of(n, s * (a * n + s)) },
        fTex = { v -> "\\frac{1}{" + factorTex(a, b, v) + factorTex(a, a + b, v) + "}" },
        fNext = "\\frac{1}{(${miLin(a, s)})(${miLin(a, a + s)})}", paren = false,
        bigTex = "\\frac{n}{${den(a, s)}}", nextTex = "\\frac{n+1}{${den(a, a + s)}}", partialTex = "\\frac{n+1}{${den(a, s)}}",
        partialWhy = "Za §n§ treba dosadiť §n+1§ aj do menovateľa.",
        middle = listOf(
            "\\frac{n}{${den(a, s)}}+\\frac{1}{(${miLin(a, s)})(${miLin(a, a + s)})}",
            "\\frac{n(${miLin(a, a + s)})+$s}{$pre(${miLin(a, s)})(${miLin(a, a + s)})}",
            "\\frac{${miPoly(listOf(e2, e1, e0))}}{$pre(${miLin(a, s)})(${miLin(a, a + s)})}",
            "\\frac{(n+1)(${miLin(a, s)})}{$pre(${miLin(a, s)})(${miLin(a, a + s)})}",
        ),
        thirdTex = "\\frac{n}{${den(a, s)}}+1", thirdWhy = PlusOne,
        fractions = true, src = "Skriptá, Cvičenie 4.4 c); test 3. 12. 2021, príklad 2",
    )
}

internal fun miSquareSum(r: Random): MiSumFamily {
    val c = r.pick(1, 4).toLong()
    val odd = c % 2 == 1L
    fun big(p: String, shift: Long) = if (odd) "\\frac{$p(${miLin(1, (1 + 3 * c) / 2 + shift)})}{3}" else "\\frac{$p(${miLin(2, 1 + 3 * c + 2 * shift)})}{6}"
    return MiSumFamily(
        f = { k -> (k * (k + c)).q() }, big = { n -> Frac.of(n * (n + 1) * (2 * n + 1 + 3 * c), 6) },
        fTex = { v -> "$v($v+$c)" }, fNext = "(n+1)(n+${1 + c})", paren = false,
        bigTex = big("n(n+1)", 0), nextTex = big("(n+1)(n+2)", 1), partialTex = big("(n+1)(n+2)", 0),
        partialWhy = "Za §n§ treba dosadiť §n+1§ do všetkých troch zátvoriek.",
        middle = listOf(
            "\\frac{n(n+1)(${miLin(2, 1 + 3 * c)})}{6}+(n+1)(n+${1 + c})",
            "\\frac{(n+1)\\big(n(${miLin(2, 1 + 3 * c)})+6(n+${1 + c})\\big)}{6}",
            "\\frac{(n+1)(${miPoly(listOf(2, 7 + 3 * c, 6 + 6 * c))})}{6}",
            "\\frac{(n+1)(n+2)(${miLin(2, 3 + 3 * c)})}{6}",
        ),
        thirdTex = big("n(n+1)", 0) + "+1", thirdWhy = PlusOne,
        fractions = false, src = "Skriptá, Cvičenie 4.4 b)",
    )
}

private const val PlusOne = "Pravá strana nerastie o jedna. Je to ten istý vzorec s §n+1§ namiesto §n§."

/** c2 n^2 + c1 n + c0 as tex. */
internal fun miPoly(cs: List<Long>): String {
    val deg = cs.size - 1
    val b = StringBuilder()
    for ((i, c) in cs.withIndex()) {
        if (c == 0L) continue
        val p = deg - i
        val v = when (p) { 0 -> ""; 1 -> "n"; else -> "n^{$p}" }
        val abs = kotlin.math.abs(c)
        val body = if (v.isEmpty()) "$abs" else if (abs == 1L) v else "$abs$v"
        if (b.isEmpty()) { if (c < 0) b.append("-") } else b.append(if (c < 0) "-" else "+")
        b.append(body)
    }
    return b.ifEmpty { "0" }.toString()
}

internal fun miDrawSum(r: Random): MiSumFamily {
    while (true) {
        val fam = when (r.nextInt(4)) { 0 -> miLinSum(r); 1 -> miGeoSum(r); 2 -> miTeleSum(r); else -> miSquareSum(r) } ?: continue
        // the claim is true: both sides agree for n = 1..10
        var acc = Frac.ZERO
        if ((1L..10L).all { n -> acc += fam.f(n); acc == fam.big(n) }) return fam
    }
}

fun miSumExercise(r: Random): Exercise = miSum(r).first

internal fun miSum(r: Random): Pair<Exercise, String> {
    val s = miDrawSum(r)
    val claim = miSumClaim(s)
    val l1 = s.f(1)
    val (stepOpts, stepOk) = r.shuffled(s.fNext, listOf(s.fTex("n"), s.nextTex))
    val (endOpts, endOk) = r.shuffled(s.nextTex, listOf(s.partialTex, s.thirdTex))
    return Exercise(
        "dml-mi-suma", "Súčet indukciou", s.src,
        m("""Dokažte matematickou indukcí, že pro všechna §n\in\mathbb{N}§ platí §$claim§."""),
        parts = listOf(
            { _ -> Part(m("Základný krok, §n=1§: koľko je ľavá strana?"), Answer.Numbers(listOf("L(1)"), null, listOf(l1), m("§${l1.tex()}§"), fractions = s.fractions,
                diagnose = { g -> if (g.firstOrNull() == s.big(2)) m("To je súčet pre §n=2§. Pre §n=1§ má súčet jediný člen, §k=1§.") else m("Pre §n=1§ má súčet jediný člen. Dosaď §k=1§ do §${s.fTex("k")}§.") }),
                after = m("Správne. Pravá strana pre §n=1§ je tiež §${s.big(1).tex()}§, základný krok platí."),
                hint = m("Pre §n=1§ má súčet jediný člen. Dosaď §k=1§ do výrazu za znakom súčtu."), aim = 1) },
            { _ -> Part(m("Indukčný krok: ktorý člen pribudne v súčte pre §n+1§ oproti súčtu pre §n§?"), Answer.Choice(stepOpts.map { m("§$it§") }, stepOk,
                m("Pribudne člen pre §k=n+1§. Súčet do §n+1§ je súčet do §n§ plus tento posledný člen."),
                wrong = stepOpts.withIndex().filter { it.index != stepOk }.associate { (i, o) ->
                    i to if (o == s.nextTex) m("To je pravá strana pre §n+1§, teda cieľ kroku. Pribudne len jeden člen súčtu, ten pre §k=n+1§.")
                    else m("Tento člen už v súčte do §n§ je. Nový je člen pre §k=n+1§.")
                }), hint = m("Súčet do §n+1§ je súčet do §n§ a jeden posledný člen. Ktoré §k§ v súčte do §n§ ešte nebolo?"), aim = 2) },
            { _ -> Part(m("Čo musí vyjsť na konci indukčného kroku?"), Answer.Choice(endOpts.map { m("§$it§") }, endOk,
                m("Pravá strana s §n+1§ namiesto §n§. Dosadiť treba na každé miesto, kde je §n§."),
                wrong = endOpts.withIndex().filter { it.index != endOk }.associate { (i, o) ->
                    i to m(if (o == s.partialTex) s.partialWhy else s.thirdWhy)
                }), hint = m("Indukčný krok dokazuje §V(n+1)§. Ako vyzerá pravá strana tvrdenia, keď do nej za §n§ dosadíš §n+1§?"), aim = 2) },
        ),
        postup = MiWeakPostup,
        solution = { miSumSolution(s) },
        hint = m("Súčet do §n+1§ rozdeľ na súčet do §n§ a posledný člen. Na súčet do §n§ použi IP."),
    ) to m("Rovnosť §$claim§ platí pre všetky §n\\in\\mathbb{N}§.")
}

/** A term after the sum sign: in brackets only when it is a sum itself ("(2k-1)", but "k", "2^{k-1}"). */
private fun MiSumFamily.term(t: String) = if (paren && (t.contains('+') || t.drop(1).contains('-'))) "($t)" else t

internal fun miSumClaim(s: MiSumFamily) = "\\sum_{k=1}^{n}" + s.term(s.fTex("k")) + "=" + s.bigTex

/** The solution of a sum by weak induction, the same for the generator and the lesson's worked example (Zásada 13). */
internal fun miSumSolution(s: MiSumFamily): Solution {
    fun term(t: String) = s.term(t)
    val claim = miSumClaim(s)
    val l1 = s.f(1)
    val (claimAim, zk, ik, end) = MiWeakPostup
    return MiWeakPostup.solve {
        claimAim("Dokazujem slabou indukciou výrok §V(n)§ pre všetky §n\\in\\mathbb{N}§.", claim)
        zk("Pre §n=1§ má súčet jediný člen: ľavá strana je §${l1.tex()}§, pravá §${s.big(1).tex()}§. Rovnajú sa.")
        ik("Nech §n\\in\\mathbb{N}§ je pevné ľubovoľné a platí §V(n)§ (IP). Súčet do §n+1§ rozdelím na súčet do §n§ a posledný člen.",
            Formula("\\sum_{k=1}^{n+1}" + term(s.fTex("k")) + "=\\sum_{k=1}^{n}" + term(s.fTex("k")) + "+" + term(s.fNext)))
        ik("Na súčet do §n§ použijem IP a upravím, kým nevyjde pravá strana §V(n+1)§.",
            listOf(Formula("\\stackrel{IP}{=}" + s.middle.first(), "Tu som použil IP.")) + s.middle.drop(1).map { Formula("=$it") } + Formula("=" + s.nextTex, "To je pravá strana §V(n+1)§."))
        end("Podľa slabého princípu MI platí §V(n)§ pre všetky §n\\in\\mathbb{N}§.")
    }
}

/* ---- 2. Divisibility: d | T(n) = x·g^{en+s} ± y·h^{fn+t} ------------------------------------------------------------ */

/** coef · base^{e·n + s}; base 1 is a constant term. */
internal data class MiTerm(val coef: Long, val base: Long, val e: Int, val s: Int) {
    val step: Long get() = miIpow(base, e)
    fun at(n: Long): Long = JMath.multiplyExact(coef, miIpow(base, (e * n + s).toInt()))
    fun tex(v: String = "n"): String {
        if (base == 1L || e == 0) return "${coef * miIpow(base, s)}"
        val exp = if (v == "n") miLin(e.toLong(), s.toLong()) else miLin(e.toLong(), (e + s).toLong())
        return miTimes(coef, miPow(base, exp))
    }
}

internal data class MiDiv(val t1: MiTerm, val t2: MiTerm, val d: Long) {
    fun at(n: Long) = t1.at(n) + t2.at(n)
    fun tex(v: String = "n") = t1.tex(v) + plusTerm(t2.tex(v))
    /** T(n+1) = G·T(n) + K·T2(n)/coef2-free: K multiplies t2 as written. */
    val g: Long get() = t1.step
    val k: Long get() = t2.step - t1.step
}

internal fun miDrawDiv(r: Random): MiDiv {
    while (true) {
        val g = r.pick(2, 7).toLong(); val e = r.pickOf(1, 1, 2)
        val s = if (e == 2) r.pick(-1, 1) else r.pick(0, 2)
        val x = if (s == 0) r.pickOf(1, 1, 2, 3, 5, 6).toLong() else 1L
        val t1 = MiTerm(x, g, e, s)
        val sign = if (r.nextBoolean()) 1L else -1L
        val t2 = if (r.nextInt(3) == 0) MiTerm(sign * r.pick(1, 9), 1, 0, 0) else {
            val h = r.pick(2, 7).toLong(); val f = r.pickOf(1, 1, 2); val t = if (f == 2) r.pick(-1, 1) else r.pick(0, 2)
            MiTerm(sign * (if (t == 0) r.pickOf(1, 1, 2, 3).toLong() else 1L), h, f, t)
        }
        if (t1.step == t2.step) continue
        val d0 = gcdL(t2.step - t1.step, t1.at(1) + t2.at(1))
        if (d0 !in 3..60) continue
        if (kotlin.math.abs(t1.at(1) + t2.at(1)) > 3000) continue
        // not trivial: d must not divide both terms on their own
        if (t1.at(1) % d0 == 0L && t2.at(1) % d0 == 0L) continue
        val dv = MiDiv(t1, t2, d0)
        if ((1L..8L).all { dv.at(it) % d0 == 0L }) return dv
    }
}

fun miDivExercise(r: Random): Exercise = miDiv(r).first

internal fun miDiv(r: Random): Pair<Exercise, String> {
    val dv = miDrawDiv(r)
    val d = dv.d; val t = dv.tex(); val t1v = dv.at(1)
    // T(n+1) = G·T(n) + K·(t2 written for n)
    val kk = dv.k
    val rest = if (dv.t2.base == 1L) "${kk * dv.t2.coef}" else miTimes(kk * dv.t2.coef, miPow(dv.t2.base, miLin(dv.t2.e.toLong(), dv.t2.s.toLong())))
    val (ipOpts, ipOk) = r.shuffled(
        "Nech §n\\in\\mathbb{N}§ je pevné a §$d\\mid $t§, teda §$t=$d\\ell§ pre nejaké §\\ell\\in\\mathbb{Z}§.",
        listOf("Predpokladajme, že §$d\\mid $t§ pre všetky §n\\in\\mathbb{N}§.", "Predpokladajme, že §$d\\mid ${dv.tex("n+1")}§."),
    )
    return Exercise(
        "dml-mi-del", "Deliteľnosť indukciou", "Skriptá, Cvičenie 4.1 a 4.2",
        m("""Dokažte matematickou indukcí, že §$t§ je dělitelné §$d§ pro všechna §n\in\mathbb{N}§."""),
        parts = listOf(
            { _ -> Part(m("Základný krok: koľko je §$t§ pre §n=1§?"), Answer.Numbers(listOf("T(1)"), null, listOf(t1v.q()), m("§$t1v§"),
                diagnose = { gv -> if (dv.t1.s >= 0 && dv.t2.s >= 0 && gv.firstOrNull() == dv.at(0).q()) m("To je hodnota pre §n=0§. Tvrdenie je pre §n\\in\\mathbb{N}§, začína sa §n=1§.") else m("Hodnota pre §n=1§ nesedí. Dosaď §n=1§ do každého exponentu zvlášť a vypočítaj celý výraz aj so znamienkom medzi členmi.") }),
                after = m("§$t1v=$d\\cdot${t1v / d}§, deliteľné §$d§: základný krok platí."),
                hint = m("Dosaď §n=1§ do každého exponentu zvlášť a vypočítaj celý výraz aj so znamienkom medzi členmi."), aim = 1) },
            { _ -> Part(m("Indukčný krok: §${dv.tex("n+1")}=a\\cdot\\big($t\\big)+b${if (dv.t2.base == 1L) "" else "\\cdot " + miPow(dv.t2.base, miLin(dv.t2.e.toLong(), dv.t2.s.toLong()))}§. Aké sú §a§ a §b§?"),
                Answer.Numbers(listOf("a", "b"), null, listOf(dv.g.q(), (kk * dv.t2.coef).q()), m("§a=${dv.g},\\ b=${kk * dv.t2.coef}§"),
                    diagnose = { gv -> when {
                        gv.firstOrNull() == dv.t2.step.q() -> m("Vytkol si násobok druhého člena. Tu sa vytýka §${dv.g}§, lebo §${dv.t1.tex("n+1")}=${dv.g}\\cdot ${dv.t1.tex()}§.")
                        gv.firstOrNull() != dv.g.q() -> m("§a§ je číslo, ktorým sa násobí prvý člen pri §n\\mapsto n+1§. Exponent stúpne o §${dv.t1.e}§, teda §a=${Terms.pow(dv.t1.base, dv.t1.e)}§.")
                        else -> m("§a§ máš dobre, §b§ nie. Od §${dv.t2.tex("n+1")}§ odpočítaj §${dv.g}\\cdot\\big(${dv.t2.tex()}\\big)§.")
                    } }),
                after = m("§b=${kk * dv.t2.coef}=$d\\cdot${kk * dv.t2.coef / d}§ je násobok §$d§."),
                hint = m("Prvý člen pre §n+1§ napíš ako násobok prvého člena pre §n§ a vytkni celý výraz z IP. Čo ostane navyše, je §b§."), aim = 2) },
            { _ -> Part(m("Ako znie indukčný predpoklad?"), Answer.Choice(ipOpts.map(::m), ipOk,
                m("Pre jedno pevné, ľubovoľné §n§. Z neho dokazuješ tvrdenie pre §n+1§."),
                wrong = ipOpts.withIndex().filter { it.index != ipOk }.associate { (i, o) ->
                    i to m(if (o.contains("všetky")) "To je celé tvrdenie, ktoré máš dokázať. Dombek: opravujúci sa tu zastaví, „to jsme měli dokázat“." else "To je záver kroku, ten predpokladať nesmieš. Predpokladáš tvrdenie pre §n§.")
                }), hint = m("IP je to, z čoho v indukčnom kroku dokazuješ §V(n+1)§. Pre aké §n§ ho smieš predpokladať?"), aim = 2) },
        ),
        postup = MiWeakPostup,
        solution = {
            val (claimAim, zk, ik, end) = MiWeakPostup
            MiWeakPostup.solve {
                claimAim("Dokazujem slabou indukciou výrok §V(n)§ pre všetky §n\\in\\mathbb{N}§.", "$d\\mid $t")
                zk("Pre §n=1§ vyjde §$t1v§, a to je násobok §$d§.", "$t1v=$d\\cdot ${Terms.paren(t1v / d)}")
                ik("Nech §n\\in\\mathbb{N}§ je pevné ľubovoľné a §$t=$d\\ell§ pre nejaké §\\ell\\in\\mathbb{Z}§ (IP). Výraz pre §n+1§ napíšem ako násobok výrazu pre §n§ a zvyšok.",
                    Formula(dv.tex("n+1") + "=" + Terms.times(dv.g, "\\big($t\\big)") + plusTerm(rest), "Prvý člen vytknem, zvyšok dopočítam."))
                ik("Za výraz v zátvorke dosadím z IP a vytknem §$d§; číslo v zátvorke je celé.",
                    Formula("\\stackrel{IP}{=}" + Terms.times(dv.g, "$d\\ell") + plusTerm(rest) + "=$d\\Big(" + Terms.times(dv.g, "\\ell") + plusTerm(if (dv.t2.base == 1L) "${kk * dv.t2.coef / d}" else miTimes(kk * dv.t2.coef / d, miPow(dv.t2.base, miLin(dv.t2.e.toLong(), dv.t2.s.toLong())))) + "\\Big)", "Tu som použil IP."))
                ik("Teda §$d\\mid ${dv.tex("n+1")}§, a to je §V(n+1)§.")
                end("Podľa slabého princípu MI je §$t§ deliteľné §$d§ pre všetky §n\\in\\mathbb{N}§.")
            }
        },
        hint = m("Napíš člen pre §n+1§ ako násobok člena pre §n§. Vytkni výraz z IP a pozri, čo zvýši."),
    ) to m("§$d\\mid $t§ pre všetky §n\\in\\mathbb{N}§.")
}

/* ---- 3. Matrix powers: guess A^n, prove it ------------------------------------------------------------------------ */

typealias MiMat = List<List<Long>>

internal fun miMul(a: MiMat, b: MiMat): MiMat = a.indices.map { i -> b[0].indices.map { j -> a[0].indices.fold(0L) { s, k -> JMath.addExact(s, JMath.multiplyExact(a[i][k], b[k][j])) } } }
internal fun miMatPow(a: MiMat, n: Int): MiMat { var r = a; repeat(n - 1) { r = miMul(r, a) }; return r }
internal fun miPmat(rows: List<List<String>>) = "\\begin{pmatrix}" + rows.joinToString("\\\\") { it.joinToString("&") } + "\\end{pmatrix}"
private fun miPmatL(a: MiMat) = miPmat(a.map { r -> r.map { "$it" } })

/** A guess of A^n: its tex for n and n+1, and its value. */
internal class MiMatForm(val tex: (String) -> String, val at: (Int) -> MiMat)

internal class MiMatFamily(
    val a: MiMat,
    val right: MiMatForm,
    /** Wrong guesses with the reason, the first sentence of which is filled in by the generator. */
    val wrong: List<Pair<MiMatForm, String>>,
    /** The step after A^n·A =IP= (right form)·A: the entries multiplied out, then simplified. */
    val middle: List<String>,
    val src: String,
)

/** "n" → "n-1", "n+1" → "n": the exponent one lower. */
private fun lower(v: String) = if (v == "n") "n-1" else "n"
private fun vfac(v: String) = if (v == "n") "n" else "(n+1)"
private fun cdot(vararg xs: String) = xs.filter { it.isNotEmpty() }.joinToString("\\cdot ")
/** An entry of a matrix product, x times the number k, written as on paper: "x\cdot(k)", no "1\cdot" and no "\cdot(1)". */
private fun mul(x: String, k: Long): String = when {
    k == 0L || x == "0" -> "0"
    x == "1" -> Terms.paren(k)
    k == 1L -> x
    else -> "$x\\cdot ${Terms.paren(k)}"
}

private fun jordan(r: Random): MiMatFamily {
    val a = r.pickOf(-2, -1, 1, 2, 3).toLong(); val b = r.pickOf(-3, -2, -1, 1, 2, 3, 4).toLong()
    return miJordan(a, b, r.nextBoolean())
}

/** The family of the matrix with rows (a, b), (0, a) (its transpose when [low]): guess, wrong guesses and the step, all computed. */
internal fun miJordan(a: Long, b: Long, low: Boolean): MiMatFamily {
    fun place(d: String, o: String) = if (low) listOf(listOf(d, "0"), listOf(o, d)) else listOf(listOf(d, o), listOf("0", d))
    fun placeL(d: Long, o: Long) = if (low) listOf(listOf(d, 0L), listOf(o, d)) else listOf(listOf(d, o), listOf(0L, d))
    fun off(v: String): String {
        val coef = when (b) { 1L -> ""; -1L -> "-"; else -> "$b" }
        val pw = if (a == 1L) "" else miPow(a, lower(v))
        return if (pw.isEmpty()) coef + vfac(v) else coef + vfac(v) + "\\cdot " + pw
    }
    val right = MiMatForm({ v -> miPmat(place(miPow(a, v), off(v))) }, { n -> placeL(miIpow(a, n), b * n * miIpow(a, n - 1)) })
    val wrong = listOf(
        MiMatForm({ v -> miPmat(place(miPow(a, v), miLin(b, 0, v).let { if (v == "n") it else miTimes(b, "(n+1)") })) }, { n -> placeL(miIpow(a, n), b * n) }) to "Chýba mocnina §${miPow(a, "n-1")}§: mimodiagonálny prvok sa pri každom násobení násobí aj §$a§.",
        MiMatForm({ v -> miPmat(place(miPow(a, v), miTimes(b, miPow(a, lower(v))))) }, { n -> placeL(miIpow(a, n), b * miIpow(a, n - 1)) }) to "Chýba činiteľ §n§: mimodiagonálny prvok rastie aj lineárne.",
        MiMatForm({ v -> miPmat(place(miPow(a, v), miPow(b, v))) }, { n -> placeL(miIpow(a, n), miIpow(b, n)) }) to "Matica sa neumocňuje po prvkoch.",
        MiMatForm({ v -> miPmat(place(miPow(a, v), cdot(miTimes(b, vfac(v)), if (a == 1L) "" else miPow(a, v)))) }, { n -> placeL(miIpow(a, n), b * n * miIpow(a, n)) }) to "Mocnina pri §$a§ je o jedna menšia, §${miPow(a, "n-1")}§.",
    )
    val x = miPow(a, "n"); val y = off("n")
    return MiMatFamily(
        if (low) listOf(listOf(a, 0), listOf(b, a)) else listOf(listOf(a, b), listOf(0, a)), right, wrong,
        listOf(miPmat(place(mul(x, a), Terms.sum(mul(x, b), mul(if (y.startsWith("-") || Terms.compound(y)) "($y)" else y, a)))), right.tex("n+1")),
        "Test 4. 12. 2025 (variant E), príklad 1; Cvičenie 4.9 a)",
    )
}

private fun rankOne(r: Random): MiMatFamily? {
    val u = listOf(r.pick(-2, 3).toLong(), r.pick(-2, 3).toLong()); val w = listOf(r.pick(-2, 3).toLong(), r.pick(-2, 3).toLong())
    val t = u[0] * w[0] + u[1] * w[1]
    if (t !in listOf(-3L, -2L, -1L, 2L, 3L, 4L)) return null
    val a = listOf(listOf(u[0] * w[0], u[0] * w[1]), listOf(u[1] * w[0], u[1] * w[1]))
    if (a.flatten().count { it != 0L } < 3) return null
    val at = miPmatL(a)
    val right = MiMatForm({ v -> miPow(t, lower(v)).let { if (it == "1") at else "$it$at" } }, { n -> a.map { row -> row.map { it * miIpow(t, n - 1) } } })
    val wrong = listOf(
        MiMatForm({ v -> "${miPow(t, v)}$at" }, { n -> a.map { row -> row.map { it * miIpow(t, n) } } }) to "Exponent je o jedna menší, §${miPow(t, "n-1")}§: pre §n=1§ musí vyjsť §A§.",
        MiMatForm({ _ -> at }, { _ -> a }) to "§A^2§ sa nerovná §A§, ale §${Terms.times(t, "A")}§.",
        MiMatForm({ v -> miPmat(a.map { row -> row.map { miPow(it, v).let { s -> if (it == 0L) "0" else s } } }) }, { n -> a.map { row -> row.map { miIpow(it, n) } } }) to "Matica sa neumocňuje po prvkoch.",
    )
    return MiMatFamily(a, right, wrong,
        listOf("${miPow(t, "n-1")}A^2", "${miPow(t, "n-1")}\\cdot ${if (t < 0) "($t)" else "$t"}A", right.tex("n+1")),
        "Písomka 8. 12. 2023 (variant D), príklad 1; Skriptá, Cvičení 4.18 b)",
    )
}

private fun triangular(r: Random): MiMatFamily? {
    val a = r.pickOf(-2, -1, 1, 2, 3).toLong(); val c = r.pickOf(-2, -1, 1, 2, 3).toLong()
    if (a == c) return null
    val k = r.pickOf(-2, -1, 1, 2).toLong(); val b = k * (a - c)
    if (kotlin.math.abs(b) > 6) return null
    fun off(v: String): String { val inner = miPow(a, v) + "-" + miPow(c, v); return when (k) { 1L -> inner; -1L -> "-($inner)"; else -> "$k($inner)" } }
    fun mat(d1: String, o: String, d2: String) = miPmat(listOf(listOf(d1, o), listOf("0", d2)))
    val right = MiMatForm({ v -> mat(miPow(a, v), off(v), miPow(c, v)) }, { n -> listOf(listOf(miIpow(a, n), k * (miIpow(a, n) - miIpow(c, n))), listOf(0L, miIpow(c, n))) })
    val wrong = listOf(
        MiMatForm({ v -> mat(miPow(a, v), miPow(b, v), miPow(c, v)) }, { n -> listOf(listOf(miIpow(a, n), miIpow(b, n)), listOf(0L, miIpow(c, n))) }) to "Matica sa neumocňuje po prvkoch.",
        MiMatForm({ v -> mat(miPow(a, v), miLin(b, 0, v).let { if (v == "n") it else miTimes(b, "(n+1)") }, miPow(c, v)) }, { n -> listOf(listOf(miIpow(a, n), b * n), listOf(0L, miIpow(c, n))) }) to "Prvok vpravo hore nerastie lineárne, keď sú na diagonále rôzne čísla.",
        MiMatForm({ v -> mat(miPow(a, v), miTimes(b, miPow(a, lower(v))), miPow(c, v)) }, { n -> listOf(listOf(miIpow(a, n), b * miIpow(a, n - 1)), listOf(0L, miIpow(c, n))) }) to "Pri násobení prispieva do prvku vpravo hore aj §$c§ z diagonály, nielen §$a§.",
    )
    val x = miPow(a, "n"); val y = off("n"); val z = miPow(c, "n")
    return MiMatFamily(listOf(listOf(a, b), listOf(0, c)), right, wrong,
        listOf(mat(mul(x, a), Terms.sum(mul(x, b), mul("($y)", c)), mul(z, c)), right.tex("n+1")),
        "Šablóna testov 2023 až 2025, príklad 1",
    )
}

private fun threeByThree(r: Random): MiMatFamily {
    val c = r.pickOf(-2, -1, 2, 3).toLong(); val b = r.pickOf(-3, -2, -1, 1, 2, 3).toLong()
    val low = r.nextBoolean()
    fun m3(o: String, d: String) = if (low) miPmat(listOf(listOf("1", "0", "0"), listOf("0", d, "0"), listOf(o, "0", "1"))) else miPmat(listOf(listOf("1", "0", o), listOf("0", d, "0"), listOf("0", "0", "1")))
    fun m3L(o: Long, d: Long): MiMat = if (low) listOf(listOf(1L, 0, 0), listOf(0L, d, 0), listOf(o, 0, 1)) else listOf(listOf(1L, 0, o), listOf(0L, d, 0), listOf(0L, 0, 1))
    fun nb(v: String) = if (v == "n") miLin(b, 0) else miTimes(b, "(n+1)")
    val right = MiMatForm({ v -> m3(nb(v), miPow(c, v)) }, { n -> m3L(b * n, miIpow(c, n)) })
    val wrong = listOf(
        MiMatForm({ v -> m3(miPow(b, v), miPow(c, v)) }, { n -> m3L(miIpow(b, n), miIpow(c, n)) }) to "Matica sa neumocňuje po prvkoch.",
        MiMatForm({ v -> m3(nb(v), miLin(c, 0, v).let { if (v == "n") it else miTimes(c, "(n+1)") }) }, { n -> m3L(b * n, c * n) }) to "Prvok na diagonále sa násobí sám sebou: §${miPow(c, "n")}§.",
        MiMatForm({ _ -> m3("$b", miPow(c, "n")) }, { n -> m3L(b, miIpow(c, n)) }) to "Prvok §$b§ sa pri každom násobení pripočíta znova.",
    )
    return MiMatFamily(m3L(b, c), right, wrong,
        listOf(m3(Terms.sum("$b", nb("n")), mul(miPow(c, "n"), c)), right.tex("n+1")),
        if (low) "Vzorová písomka 4. 12. 2025, príklad 1" else "Test 6. 12. 2024 (variant R), príklad 1",
    )
}

internal fun miDrawMatrix(r: Random, kind: Int = r.nextInt(4)): MiMatFamily {
    while (true) {
        val fam = when (kind) { 0 -> jordan(r); 1 -> rankOne(r); 2 -> triangular(r); else -> threeByThree(r) } ?: continue
        // the guess is right for n = 1..8, and every wrong guess fails for some n in 1..4
        if ((1..8).any { miMatPow(fam.a, it) != fam.right.at(it) }) error("matrix family is wrong: ${fam.a}")
        val bad = fam.wrong.filter { (w, _) -> (1..4).any { miMatPow(fam.a, it) != w.at(it) } }
        val distinct = (listOf(fam.right.tex("n")) + bad.map { it.first.tex("n") }).distinct()
        if (bad.size < 2 || distinct.size != bad.size + 1) continue
        return MiMatFamily(fam.a, fam.right, bad.shuffled(r).take(3), fam.middle, fam.src)
    }
}

fun miMatrixExercise(r: Random, kind: Int = r.nextInt(4)): Exercise = miMatrix(r, kind).first

internal fun miMatrix(r: Random, kind: Int = r.nextInt(4)): Pair<Exercise, String> {
    val f = miDrawMatrix(r, kind)
    val sz = f.a.size
    val a2 = miMatPow(f.a, 2); val a3 = miMatPow(f.a, 3)
    val labels = (1..sz).flatMap { i -> (1..sz).map { j -> "a_{$i$j}" } }
    val (opts, ok) = r.shuffled(f.right.tex("n"), f.wrong.map { it.first.tex("n") })
    val wrongWhy = f.wrong.associate { (w, why) ->
        val k = (1..4).first { miMatPow(f.a, it) != w.at(it) }
        w.tex("n") to "Pre §n=$k§ nesedí s §${Terms.pow("A", k)}=${miPmatL(miMatPow(f.a, k))}§. $why"
    }
    val (ikOpts, ikOk) = r.shuffled(
        "§A^{n+1}=A^n\\cdot A§, za §A^n§ dosadím tvar z IP.",
        listOf("§A^{n+1}=A^n+A§, za §A^n§ dosadím tvar z IP.", "Predpokladám, že tvar platí pre všetky §n\\in\\mathbb{N}§.", "V tvare §A^n§ nahradím §n§ výrazom §n+1§."),
    )
    val ikWhy = mapOf(
        "§A^{n+1}=A^n+A§, za §A^n§ dosadím tvar z IP." to "Mocnina je opakovaný súčin, nie súčet. Správne §A^{n+1}=A^n\\cdot A§.",
        "Predpokladám, že tvar platí pre všetky §n\\in\\mathbb{N}§." to "To je celé tvrdenie, ktoré máš dokázať. Predpokladáš ho len pre jedno pevné §n§ (Dombek, 19. 10.).",
        "V tvare §A^n§ nahradím §n§ výrazom §n+1§." to "To je cieľ kroku, nie jeho dôkaz. Treba ho vypočítať ako §A^n\\cdot A§.",
    )
    return Exercise(
        "dml-mi-matica", "Mocnina matice", f.src,
        m("""Uvažujme matici §\mathbf{A}=${miPmatL(f.a)}\in\mathbb{R}^{$sz,$sz}§. Výpočtem pro několik prvních hodnot §n\in\mathbb{N}§ odhadněte, čemu se rovná §\mathbf{A}^n§ pro obecné §n\in\mathbb{N}§ a tento svůj odhad dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."""),
        parts = listOf(
            { _ -> Part(m("Spočítaj §A^2§."), Answer.Numbers(labels, null, a2.flatten().map { it.q() }, m("§A^2=${miPmatL(a2)}§"),
                diagnose = { g -> when {
                    g == f.a.flatten().map { (it * it).q() } -> m("Matice sa nenásobia po prvkoch. Prvok §(A^2)_{ij}§ je §i§-tý riadok krát §j§-tý stĺpec.")
                    else -> { val i = g.indices.firstOrNull { g[it] != a2.flatten()[it].q() } ?: 0; m("Prvok §${labels[i]}§ nesedí. Je to §${i / sz + 1}§. riadok §A§ krát §${i % sz + 1}§. stĺpec §A§.") }
                } }),
                after = m("Správne, §A^2=A\\cdot A§. Ďalej §A^3=A^2\\cdot A=${miPmatL(a3)}§."),
                hint = m("Matice sa nenásobia po prvkoch: prvok §(A^2)_{ij}§ je §i§-tý riadok §A§ krát §j§-tý stĺpec §A§."), aim = 0) },
            { _ -> Part(m("Aký je odhad §A^n§?"), Answer.Choice(opts.map { m("§$it§") }, ok,
                m("Sedí s §A§, §A^2§ aj §A^3§. Či platí pre každé §n§, ukáže až indukcia."),
                wrong = opts.withIndex().filter { it.index != ok }.associate { (i, o) -> i to m(wrongWhy.getValue(o)) }),
                hint = m("Porovnaj §A§, §A^2§ a §A^3§: ktoré prvky sa nemenia a ako rastú ostatné s §n§? Odhad musí pre §n=1§ dať §A§."), aim = 0) },
            { _ -> Part(m("Indukčný krok: ako začneš?"), Answer.Choice(ikOpts.map(::m), ikOk,
                m("Rozložíš §A^{n+1}§ na §A^n\\cdot A§ a použiješ IP. IP hovorí len o §A^n§, preto ju z §A^{n+1}§ vyčleníš a dosadíš za ňu odhad; zostane vynásobiť ho maticou §A§ a porovnať s odhadom pre §n+1§."),
                wrong = ikOpts.withIndex().filter { it.index != ikOk }.associate { (i, o) -> i to m(ikWhy.getValue(o)) }),
                hint = m("Mocnina je opakovaný súčin. Zapíš §A^{n+1}§ cez §A^n§ tak, aby si za §A^n§ mohol dosadiť IP."), aim = 2) },
        ),
        postup = MiMatrixPostup,
        solution = {
            val (guess, zk, ik, end) = MiMatrixPostup
            MiMatrixPostup.solve {
                guess("Najprv spočítam niekoľko mocnín a hľadám, čo sa mení s §n§.", "A^2=${miPmatL(a2)},\\quad A^3=${miPmatL(a3)}")
                guess("Odhad zapíšem ako výrok §V(n)§ pre všetky §n\\in\\mathbb{N}§.", "A^n=${f.right.tex("n")}")
                zk("Pre §n=1§ dá odhad maticu §${miPmatL(f.right.at(1))}§, a to je §A^1=A§.")
                ik("Nech §n\\in\\mathbb{N}§ je pevné ľubovoľné a platí §A^n=${f.right.tex("n")}§ (IP). §A^{n+1}§ rozložím na §A^n\\cdot A§ a za §A^n§ dosadím odhad.",
                    listOf(Formula("A^{n+1}=A^n\\cdot A\\stackrel{IP}{=}${f.right.tex("n")}\\cdot ${miPmatL(f.a)}", "Tu som použil IP.")) + f.middle.map { Formula("=$it") })
                ik("Vyšiel tvar odhadu s §n+1§ namiesto §n§, teda §V(n+1)§.")
                end("Podľa slabého princípu MI platí §A^n=${f.right.tex("n")}§ pre všetky §n\\in\\mathbb{N}§.")
            }
        },
        hint = m("Spočítaj §A^2§ a §A^3§ a hľadaj, čo sa mení s §n§. V kroku rozlož §A^{n+1}=A^n\\cdot A§."),
    ) to m("§A^n=${f.right.tex("n")}§ pre všetky §n\\in\\mathbb{N}§.")
}

/* ---- 4. A two-step recurrence, strong induction -------------------------------------------------------------------- */

internal data class MiRec(val r1: Long, val r2: Long, val d1: Int, val d2: Int) {
    val p get() = r1 + r2
    val q get() = -r1 * r2
    fun closed(n: Long): Long = miIpow(r1, (n - d1).toInt()) + miIpow(r2, (n - d2).toInt())
    fun closedTex(shift: Int = 0) = miPow(r1, miLin(1, (shift - d1).toLong())) + "+" + miPow(r2, miLin(1, (shift - d2).toLong()))
    /** The formula at a number: "4+7", "4^{2}+(-3)^{1}" never, the exponent 1 and 0 written out as the number. */
    fun valueTex(n: Int) = Terms.sum(Terms.pow(r1, n - d1), Terms.pow(r2, n - d2))
    /** A_1, A_2 from the formula, then the recurrence. */
    fun seq(upto: Int): List<Long> { val a = arrayListOf(0L, closed(1), closed(2)); while (a.size <= upto) a += p * a[a.size - 1] + q * a[a.size - 2]; return a }
    fun recTex(): String {
        val t1 = when (p) { 1L -> "A_{n+1}"; -1L -> "-A_{n+1}"; else -> "${p}A_{n+1}" }
        val t2 = when (q) { 1L -> "A_n"; -1L -> "-A_n"; else -> "${q}A_n" }
        return "A_{n+2}=" + if (p == 0L) t2 else t1 + plusTerm(t2)
    }
}

/** The coefficient in front of a term, through the shared formatter: "" for 1, "-" for -1, "3" else. */
private fun cf(k: Long) = Terms.times(k, "X").removeSuffix("X")

internal fun miDrawRec(r: Random): MiRec {
    while (true) {
        val rr = MiRec(r.pickOf(2, 3, 4, 5).toLong(), r.pickOf(-4, -3, -2, -1, 2, 3).toLong(), r.pick(0, 1), r.pick(0, 1))
        if (rr.r1 == rr.r2 || rr.p == 0L) continue
        if (kotlin.math.abs(rr.closed(2)) > 60) continue
        val s = rr.seq(10)
        if ((1..10).all { s[it] == rr.closed(it.toLong()) }) return rr
    }
}

fun miRecExercise(r: Random): Exercise = miRec(r).first

internal fun miRec(r: Random): Pair<Exercise, String> {
    val rc = miDrawRec(r)
    val a1 = rc.closed(1); val a2 = rc.closed(2); val a3 = rc.p * a2 + rc.q * a1
    val text = "Mějme nekonečnou posloupnost §(A_1,A_2,A_3,\\dots)§, jejíž členy pro každé §n\\in\\mathbb{N}§ splňují vztah §${rc.recTex()}§ a dále platí §A_1=$a1§, §A_2=$a2§. Dokažte matematickou indukcí, že pro každé §n\\in\\mathbb{N}§ platí §A_n=${rc.closedTex()}§."
    val three = "§n=1,2,3§, lebo bez §A_3§ krok nezačne"
    val (zkOpts, zkOk) = r.shuffled("§n=1§ a §n=2§", listOf("Len §n=1§", three))
    val zkWhy = mapOf(
        "Len §n=1§" to "Jedna hodnota nestačí. Krok siaha dva členy späť, pre §A_2§ by potreboval §A_0§, ktoré nie je.",
        three to "Krok začne aj bez §A_3§, ten dá krok pre §n=2§ z §A_1§ a §A_2§. Overiť ho navyše nie je chyba, ale netreba.",
    )
    val (whyOpts, whyOk) = r.shuffled("§A_{n+1}§ závisí od §A_n§ aj §A_{n-1}§, IP treba pre oba.", listOf("Slabá by stačila, §A_{n+1}§ závisí len od §A_n§.", "Lebo vzorec obsahuje dve mocniny."))
    val whyWhy = mapOf(
        "Slabá by stačila, §A_{n+1}§ závisí len od §A_n§." to "Nie, rekurencia siaha dva členy späť. Na §A_{n-1}§ by slabá indukcia nemala predpoklad.",
        "Lebo vzorec obsahuje dve mocniny." to "Počet mocnín nerozhoduje. Rozhoduje, koľko predošlých členov potrebuje rekurencia.",
    )
    val e1 = miLin(1, (-1 - rc.d1).toLong()); val e2 = miLin(1, (-1 - rc.d2).toLong())
    val pr1 = rc.p * rc.r1 + rc.q; val pr2 = rc.p * rc.r2 + rc.q
    return Exercise(
        "dml-mi-rek", "Rekurencia silnou indukciou", "Písomka 25. 11. 2022, príklad 4; písomka 6. 12. 2023 (variant A), príklad 1; Skriptá, Cvičení 4.6",
        m(text),
        parts = listOf(
            { _ -> Part(m("Z rekurencie spočítaj §A_3§."), Answer.Numbers(listOf("A_3"), null, listOf(a3.q()), m("§A_3=$a3§"),
                diagnose = { g -> if (g.firstOrNull() == (rc.p * a1 + rc.q * a2).q()) m("Koeficienty si prehodil. §${rc.p}§ násobí §A_2§, §${rc.q}§ násobí §A_1§.") else m("§A_3§ nesedí s rekurenciou. Dosaď §n=1§: §A_3=${cf(rc.p)}A_2${plusTerm(cf(rc.q) + "A_1")}§.") }),
                after = m("Správne, a vzorec dáva tiež §${rc.closed(3)}§. Dôkaz to však ešte nie je."),
                hint = m("Do rekurencie dosaď §n=1§: §A_3§ vyjadríš z §A_2§ a §A_1§."), aim = 1) },
            { _ -> Part(m("Ktoré hodnoty stačí overiť v základnom kroku?"), Answer.Choice(zkOpts.map(::m), zkOk,
                m("Dve, lebo rekurencia siaha dva členy späť. Krok potom ide pre §n\\geq 2§."),
                wrong = zkOpts.withIndex().filter { it.index != zkOk }.associate { (i, o) -> i to m(zkWhy.getValue(o)) }),
                hint = m("Pozri, koľko predošlých členov potrebuje rekurencia na výpočet ďalšieho. Toľko prvých hodnôt over."), aim = 1) },
            { _ -> Part(m("Prečo silná indukcia?"), Answer.Choice(whyOpts.map(::m), whyOk,
                m("Lebo krok potrebuje §A_n§ aj §A_{n-1}§. Slabý IP hovorí len o §A_n§."),
                wrong = whyOpts.withIndex().filter { it.index != whyOk }.associate { (i, o) -> i to m(whyWhy.getValue(o)) }),
                hint = m("§A_{n+1}§ rozpíš rekurenciou. Pre ktoré predošlé členy potom potrebuješ IP?"), aim = 2) },
        ),
        postup = MiStrongPostup,
        solution = { miRecSolution(rc) },
        hint = m("§A_{n+1}§ rozpíš rekurenciou na §A_n§ a §A_{n-1}§, na oba použi IP."),
    ) to m("§A_n=${rc.closedTex()}§ pre všetky §n\\in\\mathbb{N}§, silnou indukciou so ZK pre §n=1,2§.")
}

/** The solution of a two-step recurrence by strong induction, the same for the generator and the lesson's worked example (Zásada 13); [L] the sequence's letter. */
internal fun miRecSolution(rc: MiRec, L: String = "A"): Solution {
    val a1 = rc.closed(1); val a2 = rc.closed(2)
    val e1 = miLin(1, (-1 - rc.d1).toLong()); val e2 = miLin(1, (-1 - rc.d2).toLong())
    val pr1 = rc.p * rc.r1 + rc.q; val pr2 = rc.p * rc.r2 + rc.q
    val (claimAim, zk, ik, end) = MiStrongPostup
    return MiStrongPostup.solve {
        claimAim("Dokazujem silnou indukciou výrok §V(n)§ pre všetky §n\\in\\mathbb{N}§.", "${L}_n=${rc.closedTex()}")
        zk("Rekurencia siaha dva členy späť, preto overím §n=1§ aj §n=2§ dosadením do vzorca.",
            "${rc.valueTex(1)}=$a1=${L}_1,\\qquad ${rc.valueTex(2)}=$a2=${L}_2")
        ik("Nech §n\\geq 2§ je pevné ľubovoľné a §V(k)§ platí pre všetky §k\\in\\{1,\\dots,n\\}§ (IP). §${L}_{n+1}§ rozpíšem rekurenciou a na oba členy použijem IP.",
            Formula("${L}_{n+1}=${cf(rc.p)}${L}_n" + plusTerm(cf(rc.q) + "${L}_{n-1}"), "Rekurencia platí, lebo §n-1\\geq 1§."),
            Formula("\\stackrel{IP}{=}${cf(rc.p)}\\big(${rc.closedTex()}\\big)" + plusTerm("${cf(rc.q)}\\big(${rc.closedTex(-1)}\\big)"), "IP pre §k=n§ aj §k=n-1§, oba sú v §\\{1,\\dots,n\\}§."))
        ik("Vytknem nižšiu mocninu každého základu a dopočítam zátvorky.",
            Formula("=" + miPow(rc.r1, e1) + "\\big(${rc.p}\\cdot ${Terms.paren(rc.r1)}${plusTerm("${rc.q}")}\\big)+" + miPow(rc.r2, e2) + "\\big(${rc.p}\\cdot ${Terms.paren(rc.r2)}${plusTerm("${rc.q}")}\\big)"),
            Formula("=" + miTimes(pr1, miPow(rc.r1, e1)) + plusTerm(miTimes(pr2, miPow(rc.r2, e2))), "§$pr1=${miPow(rc.r1, "2")}§ a §$pr2=${miPow(rc.r2, "2")}§."),
            Formula("=" + rc.closedTex(1), "To je pravá strana §V(n+1)§."))
        end("Podľa silného princípu MI platí §${L}_n=${rc.closedTex()}§ pre všetky §n\\in\\mathbb{N}§.")
    }
}

/** This topic's generators by id. */
val MiGenerators: Map<String, (Random) -> Exercise> = mapOf(
    "dml-mi-suma" to ::miSumExercise,
    "dml-mi-del" to ::miDivExercise,
    "dml-mi-matica" to { r -> miMatrixExercise(r) },
    "dml-mi-matica-jordan" to { r -> miMatrixExercise(r, 0) },
    "dml-mi-rek" to ::miRecExercise,
    "dml-mi-postupnost" to { r -> miSeq(miSeqTask(r)) },
    "dml-mi-induktivna" to { r -> miSet(miSetTask(r)) },
    "dml-mi-krok" to { r -> miStep(miStepTask(r)) },
    "dml-mi-chyba" to { r -> miFlaw(miFlawTask(r)) },
)

/* ---- 5. Problem 1 of a generated paper, 10 points ------------------------------------------------------------------ */

private fun rubric(vararg r: Pair<String, Int>, drill: String) = r.map { (t, p) -> Rubric(m(t), p, drill) }

/** Mostly the matrix power (problem 1 of every paper 2023 to 2025), sometimes a recurrence, a sum or divisibility. */
fun miGeneratedProblem(r: Random): PaperProblem {
    val roll = r.nextInt(10)
    val (gen, built) = when {
        roll < 6 -> "dml-mi-matica" to miMatrix(r)
        roll < 8 -> "dml-mi-rek" to miRec(r)
        roll < 9 -> "dml-mi-suma" to miSum(r)
        else -> "dml-mi-del" to miDiv(r)
    }
    val (ex, answer) = built
    val checks = when (gen) {
        "dml-mi-matica" -> rubric("Niekoľko mocnín spočítaných a odhad §A^n§" to 3, "ZK pre §n=1§" to 1, "IK: IP pre pevné ľubovoľné §n§ a cieľ §V(n+1)§" to 2,
            "§A^{n+1}=A^n\\cdot A§ s vyznačeným IP a úprava na odhad" to 3, "Záver podľa princípu MI" to 1, drill = gen)
        "dml-mi-rek" -> rubric("ZK pre §n=1§ aj §n=2§" to 2, "Silná indukcia: IP pre §k\\in\\{1,\\dots,n\\}§, §n\\geq 2§" to 2,
            "Rekurencia a IP pre §A_n§ aj §A_{n-1}§, vyznačené" to 3, "Úprava na tvar pre §n+1§" to 2, "Záver podľa silného princípu MI" to 1, drill = gen)
        else -> rubric("ZK s výpočtom pre §n=1§" to 2, "IP pre pevné ľubovoľné §n§" to 2, "Rozklad výrazu pre §n+1§ s vyznačeným IP" to 4, "Záver podľa princípu MI" to 2, drill = gen)
    }
    return PaperProblem(1, 10, ex.label, ex.prompt, 25, checks, answer, gen, ex.solution(emptyList()))
}

package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * Exercises of kapitola 4 with new numbers: the whole solution set by Frobeniova veta (skúška 17. 12. 2025 a 2. 2. 2026,
 * př. 2), equations of a variety to its parametric form (26. 1. 2026, př. 3D) and points of a plane to its equations
 * (MARAST 14158, Příklad 4.33). Everything is computed by Chapter3Math.kt.
 */

private fun vars(n: Int) = if (n <= 5) listOf("x", "y", "z", "u", "v").take(n) else List(n) { "x_{${it + 1}}" }

/** One equation as the exam writes it: "x+2z+4u=1". */
internal fun equationTex(row: Vec, rhs: Frac, names: List<String>): String = Terms.lin(row, names) + "=" + rhs.tex(true)

/**
 * Parametric equations of the variety a + <dirs> as Poznámka 4.31 defines them: x = a + Σ α_i u_i written out
 * component by component, one equation per coordinate, then the parameters and their field.
 */
internal fun parametricTex(f: Field, names: List<String>, a: Vec, dirs: List<Vec>, params: List<String>): String =
    "\\begin{aligned}" + names.indices.joinToString("\\\\") { i ->
        names[i] + "&=" + Terms.sum(listOf(a[i].tex(true)) + dirs.indices.map { j -> Terms.times(dirs[j][i], params[j]) })
    } + "\\\\" + params.joinToString(",") + "&\\in " + fieldName(f) + "\\end{aligned}"

private fun varietyTex(x: Vec, ks: List<Vec>) = x.tex() + (if (ks.isEmpty()) "" else "+\\langle " + ks.joinToString(",") { it.tex() } + "\\rangle")

/** The method of Postup 4.10: the whole solution set by Frobeniova veta. */
val FrobeniusPostup = postup(
    "Uprav §(\\mathbf{A}\\mid\\mathbf{b})§ na HST" to "Rozšírenú maticu uprav GEM na horný stupňovitý tvar.",
    "Porovnaj hodnosti" to "§h(\\mathbf{A})<h(\\mathbf{A}\\mid\\mathbf{b})§: riešenie nie je. Rovnosť: je, a §\\dim S_0=n-h(\\mathbf{A})§.",
    "Partikulárne riešenie" to "Voľné premenné polož §0§, viazané dopočítaj spätným dosadením z §(\\mathbf{A}\\mid\\mathbf{b})§.",
    "Báza §S_0§" to "Z §(\\mathbf{A}\\mid\\theta)§: postupne jednu voľnú premennú §1§, ostatné §0§.",
    "Zapíš §S§" to "§S=\\tilde{\\mathbf{x}}+S_0§, varieta: bod plus obal bázy §S_0§.",
)

/** Which subgoal each part of Postup 4.10 goes under, when it is a part of a longer method. */
internal class FrobeniusAims(val hst: Aim, val ranks: Aim, val part: Aim, val kernel: Aim, val set: Aim) {
    companion object { val own get() = FrobeniusPostup.let { (h, r, p, k, s) -> FrobeniusAims(h, r, p, k, s) } }
}

/** The steps of Postup 4.10 for the system (A|b), all numbers from [s]. */
internal fun SolutionScope.frobenius(f: Field, a: Mat, s: SystemSolution, names: List<String> = (1..a.n).map { "x_{$it}" }, aims: FrobeniusAims = FrobeniusAims.own) {
    val n = a.n
    val mod = if (f is Field.Zp) " Počítam modulo §${f.p}§." else ""
    if (s.e.steps.isEmpty()) aims.hst("Rozšírená matica už je v HST, upravovať netreba.$mod", gemLines(s.e))
    else aims.hst("Rozšírenú maticu upravím GEM na HST.$mod", gemLines(s.e))
    if (s.particular == null) {
        aims.ranks("§h(\\mathbf{A})=${s.rankA}<h(\\mathbf{A}\\mid\\mathbf{b})=${s.rankAb}§: posledný stĺpec je hlavný, podľa Frobeniovej vety sústava nemá riešenie.")
        aims.set("Množina riešení je prázdna.", "S=\\emptyset")
        return
    }
    aims.ranks("§h(\\mathbf{A})=h(\\mathbf{A}\\mid\\mathbf{b})=${s.rankA}§, sústava je riešiteľná (Frobeniova veta, bod 1). Dimenzia §S_0§ je počet voľných premenných (bod 3).",
        "\\dim S_0=n-h(\\mathbf{A})=$n-${s.rankA}=${n - s.rankA}")
    if (s.free.isEmpty()) {
        aims.part("Voľné premenné nie sú, riešenie je jediné.", "\\tilde{\\mathbf{x}}=${s.particular.tex()}")
        aims.set("Množina riešení má jediný prvok.", "S=\\{\\tilde{\\mathbf{x}}\\}")
        return
    }
    aims.part("Viazané premenné sú pri hlavných stĺpcoch: §${s.bound.joinToString(",") { names[it] }}§; voľné sú §${s.free.joinToString(",") { names[it] }}§. Voľné položím §0§ a viazané dopočítam spätným dosadením z §(\\mathbf{A}\\mid\\mathbf{b})§, zdola nahor.",
        backSubst(f, s.e, names).first + Formula("\\tilde{\\mathbf{x}}=${s.particular.tex()}"))
    for ((k, fr) in s.free.withIndex())
        aims.kernel("Vektor bázy §S_0§ počítam z §(\\mathbf{A}\\mid\\theta)§, nie z §(\\mathbf{A}\\mid\\mathbf{b})§ (Varování 4.11): §${names[fr]}=1§, ostatné voľné §0§; viazané dopočítam zdola.", backSubst(f, s.e, names, s.free.associateWith { if (it == fr) Frac.ONE else Frac.ZERO }, homogeneous = true).first + Formula("\\mathbf{z}_{${k + 1}}=${s.kernel[k].tex()}"))
    val d = s.free.size
    aims.set((if (d == 1) "Vypočítaný vektor je nenulový, teda LN" else "Vypočítané vektory sú LN, lebo vo voľných premenných majú štandardnú bázu") +
        ". Je ich §${d}=\\dim S_0§, preto tvoria bázu §S_0§ (Věta 2.55). Podľa Frobeniovej vety (bod 2) sú všetky riešenia partikulárne plus vektory z §S_0§" + (if (f is Field.Zp) ", koeficienty z §\\mathbb{Z}_{${f.p}}§." else "."),
        "S=\\tilde{\\mathbf{x}}+S_0=${varietyTex(s.particular, s.kernel)}")
}

/** The whole solution of (A|b) along Postup 4.10. */
internal fun frobeniusSolution(f: Field, a: Mat, s: SystemSolution, names: List<String> = (1..a.n).map { "x_{$it}" }): Solution =
    FrobeniusPostup.solve { frobenius(f, a, s, names) }

/** A wrong solution vector: the first equation it breaks. */
private fun rowCheck(f: Field, a: Mat, rhs: Vec, given: List<Frac>, homogeneous: Boolean): String {
    val g = given.map { f.norm(it) }
    val y = f.mulVec(a, g)
    val i = y.indices.firstOrNull { y[it] != f.norm(rhs[it]) } ?: return "Sústavu to spĺňa, ale voľné premenné nemajú zadané hodnoty. Dosaď ich presne podľa zadania a viazané dopočítaj."
    return "Nesedí §${i + 1}§. rovnica: ľavá strana dá §${y[i].tex(true)}§, má byť §${f.norm(rhs[i]).tex(true)}§." + (if (homogeneous) " Pri báze §S_0§ je pravá strana nulová." else "")
}

/** Back substitution that adds the moved terms instead of subtracting them: a sign slip. */
private fun signSlip(f: Field, e: Echelon, bound: List<Int>, n: Int, freeValues: Map<Int, Frac>, rhs: Boolean): Vec {
    val x = MutableList(n) { freeValues[it] ?: Frac.ZERO }
    for (k in bound.indices.reversed()) {
        val row = e.result.rows[k]; val c = bound[k]
        var s = if (rhs) row[n] else Frac.ZERO
        for (j in c + 1 until n) s = f.add(s, f.mul(row[j], x[j]))
        x[c] = f.div(s, row[c])
    }
    return x
}

/* ---- 1. the whole solution set of a system in HST over Z_p (skúška 17. 12. 2025 a 2. 2. 2026, př. 2) ---------- */

internal class Hst(val f: Field, val a: Mat, val b: Vec, val s: SystemSolution)

internal fun Random.hstSystem(): Hst {
    while (true) {
        val p = listOf(5, 5, 3, 7).random(this)
        val f = Field.Zp(p)
        val n = pick(5, 7); val m = pick(3, 4)
        val solvable = nextInt(5) > 0
        val pivots = (0 until n).shuffled(this).take(if (solvable) m else m - 1).sorted()
        if (pivots.size == m && pivots.last() == n - 1 && nextBoolean()) continue
        val rows = List(m) { r ->
            List(n) { j ->
                when {
                    r >= pivots.size -> 0
                    j < pivots[r] -> 0
                    j == pivots[r] -> pick(1, p - 1)
                    else -> if (nextInt(3) == 0) 0 else pick(0, p - 1)
                }
            }
        }
        val b = List(m) { r -> if (r >= pivots.size) pick(1, p - 1) else pick(0, p - 1) }
        val a = f.mat(rows)
        val bv = f.vec(b)
        if (bv.isZero()) continue // a homogeneous system has S = S_0, and the questions assume a nonzero right side
        val s = f.solveAll(a, bv)
        if ((s.particular != null) != solvable || s.e.steps.isNotEmpty()) continue
        if (solvable && (s.free.size < 2 || s.free.size > 3)) continue
        return Hst(f, a, bv, s)
    }
}

fun k4SolveExercise(r: Random): Exercise {
    val h = r.hstSystem()
    val f = h.f as Field.Zp; val a = h.a; val b = h.b; val s = h.s
    val n = a.n
    val aug = Mat(a.rows.indices.map { a.rows[it] + listOf(b[it]) }, n)
    val text = "S využitím Frobeniovy věty popište množinu řešení soustavy nad tělesem $\\mathbb{Z}_{${f.p}}$, jejíž rozšířená matice je \$${aug.tex()}$. Řešení zapište ve tvaru variety."
    val parts = ArrayList<(List<Any>) -> Part>()
    val solvable = s.particular != null
    parts += { _ -> Part("Je sústava riešiteľná?", Answer.Choice(listOf("Áno", "Nie"), if (solvable) 0 else 1,
        if (solvable) "Áno: §h(\\mathbf{A})=h(\\mathbf{A}\\mid\\mathbf{b})=${s.rankA}§.".replace('§', '$') else "Nie: §h(\\mathbf{A})=${s.rankA}<h(\\mathbf{A}\\mid\\mathbf{b})=${s.rankAb}§.".replace('§', '$'),
        wrong = if (solvable) mapOf(1 to "Pravá strana hodnosť nezvýši: v každom nenulovom riadku je prvý nenulový prvok vľavo od čiary. Hodnosti sa rovnajú, riešenie existuje (Frobeniova veta).")
        else mapOf(0 to "Posledný nenulový riadok má vľavo samé nuly a vpravo nenulové číslo. To je rovnica §0=c§ s §c\\neq 0§, teda §h(\\mathbf{A})<h(\\mathbf{A}\\mid\\mathbf{b})§.")),
        hint = m("Rozšírená matica už je v HST. Porovnaj §h(\\mathbf{A})§ a §h(\\mathbf{A}\\mid\\mathbf{b})§: sústava má riešenie práve vtedy, keď sa rovnajú (Frobeniova veta)."), aim = 1) }
    if (solvable) {
        val d = n - s.rankA
        val names = (1..n).map { "x_{$it}" }
        parts += { _ -> Part("§\\dim S_0=§".replace('§', '$'), Answer.Numbers(listOf("\\dim S_0"), null, listOf(d.q()), "\$n-h(\\mathbf{A})=$n-${s.rankA}=$d$", diagnose = diagnosis(Field.R, listOf(d.q()), listOf(
            listOf(s.rankA.q()) to "To je hodnosť §\\mathbf{A}§. Dimenzia §S_0§ je §n-h(\\mathbf{A})§, počet voľných premenných.",
            listOf(n.q()) to "§n§ je počet neznámych. Od neho treba odčítať hodnosť (Frobeniova veta, bod 3).",
            listOf(a.m.q()) to "To je počet riadkov. Dimenzia §S_0§ je počet voľných premenných, §n-h(\\mathbf{A})§.",
        )) { "Dimenzia §S_0§ je počet voľných premenných. Spočítaj vedľajšie stĺpce vľavo od čiary." }),
            hint = m("Dimenzia §S_0§ je §n-h(\\mathbf{A})§: počet neznámych mínus hodnosť, teda počet voľných premenných."), aim = 1) }
        val part = s.particular!!
        val slip = signSlip(f, s.e, s.bound, n, emptyMap(), true)
        parts += { _ -> Part("Partikulárne riešenie s voľnými premennými §${s.free.joinToString(",") { names[it] }}§ rovnými §0§:".replace('§', '$'), Answer.Numbers(names, f.p, part, "$\\tilde{\\mathbf{x}}=${part.tex()}$", diagnose = diagnosis(f, part, listOf(
            slip to "Znamienko: člen presunutý na pravú stranu sa odčíta. Pri spätnom dosadení §x_c=(b-\\sum a_jx_j)/a_c§.",
        )) { g -> rowCheck(f, a, b, g, false) }),
            hint = m("Voľné premenné polož §0§ a viazané dopočítaj spätným dosadením z §(\\mathbf{A}\\mid\\mathbf{b})§, zdola nahor."), aim = 2) }
        val z1 = s.kernel[0]
        val withB = f.solveAll(a, b).let { ss -> signSlip(f, ss.e, ss.bound, n, mapOf(s.free[0] to Frac.ONE), true) }
        val withBRight = run {
            val x = MutableList(n) { if (it == s.free[0]) Frac.ONE else Frac.ZERO }
            for (k in s.bound.indices.reversed()) { val row = s.e.result.rows[k]; val c = s.bound[k]; var t = row[n]; for (j in c + 1 until n) t = f.sub(t, f.mul(row[j], x[j])); x[c] = f.div(t, row[c]) }
            x
        }
        val slipK = signSlip(f, s.e, s.bound, n, mapOf(s.free[0] to Frac.ONE), false)
        parts += { _ -> Part("Vektor bázy §S_0§ pre §${names[s.free[0]]}=1§, ostatné voľné §0§:".replace('§', '$'), Answer.Numbers(names, f.p, z1, "$\\mathbf{z}_1=${z1.tex()}$", diagnose = diagnosis(f, z1, listOf(
            withBRight to "Počítal si s pravou stranou §\\mathbf{b}§. Bázu §S_0§ počítaj z §(\\mathbf{A}\\mid\\theta)§ (Varování 4.11).",
            withB to "Počítal si s pravou stranou §\\mathbf{b}§ a so zlým znamienkom. Bázu §S_0§ počítaj z §(\\mathbf{A}\\mid\\theta)§ a presunuté členy odčítaj.",
            slipK to "Znamienko: člen presunutý na druhú stranu sa odčíta, §x_c=-\\sum a_jx_j/a_c§.",
        )) { g -> rowCheck(f, a, List(a.m) { Frac.ZERO }, g, true) }),
            hint = m("Počítaj z §(\\mathbf{A}\\mid\\theta)§, bez pravej strany: zvolenú voľnú premennú polož §1§, ostatné voľné §0§ a viazané dopočítaj zdola nahor."), aim = 3) }
        val orderS = listOf(0, 1, 2).shuffled(r)
        val forms = listOf("\$S=\\tilde{\\mathbf{x}}+\\langle\\mathbf{z}_1,\\dots,\\mathbf{z}_{$d}\\rangle$", "\$S=\\langle\\tilde{\\mathbf{x}},\\mathbf{z}_1,\\dots,\\mathbf{z}_{$d}\\rangle$", "\$S=\\langle\\mathbf{z}_1,\\dots,\\mathbf{z}_{$d}\\rangle$")
        parts += { _ -> Part("Ako zapíšeš množinu riešení?", Answer.Choice(orderS.map { forms[it] }, orderS.indexOf(0),
            "Zápis platí: všetky riešenia sú partikulárne riešenie plus vektory z §S_0§ (Frobeniova veta, bod 2).".replace('§', '$'),
            wrong = mapOf(orderS.indexOf(1) to "Obal by obsahoval aj §\\theta§ a násobky §\\tilde{\\mathbf{x}}§, ktoré sústavu neriešia. §\\tilde{\\mathbf{x}}§ sa pripočíta, nekombinuje.",
                orderS.indexOf(2) to "To je len §S_0§, riešenia homogénnej sústavy. Pravá strana je nenulová, treba pripočítať §\\tilde{\\mathbf{x}}§.")),
            hint = m("K partikulárnemu riešeniu sa pripočítavajú riešenia homogénnej sústavy. Môže §\\theta§ patriť do §S§, keď je pravá strana nenulová?"), aim = 4) }
    }
    return Exercise(
        "la1-k4-solve", "Množina riešení cez Frobeniovu vetu", "Šablóna zo skúšky 17. 12. 2025 (variant Frobenius) a 2. 2. 2026 (variant Bolzano), príklad 2",
        text, parts = parts, postup = FrobeniusPostup,
        solution = { frobeniusSolution(f, a, s) },
        hint = "Porovnaj hodnosti s pravou stranou a bez nej. Partikulárne: voľné §0§. Báza §S_0§: bez pravej strany, voľné postupne §1§.".replace('§', '$'),
    )
}

/* ---- 2. equations of a variety to parameters (skúška 26. 1. 2026, př. 3D) ------------------------------------- */

fun k4ParamExercise(r: Random): Exercise {
    while (true) {
        val f: Field = listOf(Field.Zp(3), Field.Zp(5), Field.R).random(r)
        val n = r.pick(4, 5); val m = 2
        val lo = if (f is Field.Zp) 0 else -2; val hi = if (f is Field.Zp) f.p - 1 else 2
        val a = Mat(List(m) { f.vec(List(n) { r.pick(lo, hi) }) })
        if (f.rankOf(a) != m || a.rows.any { row -> row.count { !it.isZero } < 2 }) continue
        val b = f.vec(List(m) { r.pick(lo, hi + 1) })
        val s = f.solveAll(a, b)
        val part = s.particular ?: continue
        if (s.e.steps.isEmpty()) continue
        if (f is Field.R && (part + s.kernel.flatten()).any { !it.isInt || kotlin.math.abs(it.num) > 9 }) continue
        val names = vars(n)
        val eqs = a.rows.indices.joinToString(",\\quad ") { equationTex(a.rows[it], b[it], names) }
        val d = n - s.rankA
        val inside = r.nextBoolean()
        val v = if (inside) f.plus(part, f.combo(List(d) { r.pick(1, 2).q() }, s.kernel)) else f.plus(part, f.vec(List(n) { if (it == s.free[0]) 1 else 0 }))
        if (f.solves(a, b, v) != inside) continue
        val sp = if (f is Field.Zp) "\\mathbb{Z}_{${f.p}}^{$n}" else "\\mathbb{R}^{$n}"
        val p = (f as? Field.Zp)?.p
        return Exercise(
            "la1-k4-param", "Z rovníc parametricky", "Šablóna zo skúšky 26. 1. 2026 (variant Hilbert), príklad 3D, 6 b",
            "Uvažujte varietu v \$$sp$ zadanou neparametricky: \$$eqs$. Napište její parametrické vyjádření. Jakou má varieta dimenzi? Rozhodněte, zda tato varieta obsahuje vektor \$${v.tex()}$.",
            parts = listOf(
                { _ -> Part("§\\dim W=§".replace('§', '$'), Answer.Numbers(listOf("\\dim W"), null, listOf(d.q()), "\$${n}-$m=$d$", diagnose = diagnosis(Field.R, listOf(d.q()), listOf(
                    listOf(m.q()) to "To je počet rovníc, teda §h(\\mathbf{A})§. Dimenzia variety je §n-h(\\mathbf{A})§ (Věta 4.26).",
                    listOf(n.q()) to "§n§ je dimenzia celého priestoru. Každá nezávislá rovnica ju zníži o §1§.",
                )) { "Dimenzia variety je §n-h(\\mathbf{A})§: počet neznámych mínus hodnosť matice sústavy." }),
                    hint = "Dimenzia variety je §n-h(\\mathbf{A})§: počet neznámych mínus hodnosť matice sústavy.".replace('§', '$'), aim = 1) },
                { _ -> Part("Bod variety s voľnými premennými §${s.free.joinToString(",") { names[it] }}=0§:".replace('§', '$'), Answer.Numbers(names, p, part, "\$${part.tex()}\$", fractions = p == null, diagnose = diagnosis(f, part, emptyList()) { g -> rowCheck(f, a, b, g, false) }),
                    hint = "Rovnice sú sústava: uprav ju GEM na HST, voľné premenné polož §0§ a viazané dopočítaj spätným dosadením.".replace('§', '$'), aim = 0) },
                { _ -> Part("Obsahuje varieta §${v.tex()}§?".replace('§', '$'), Answer.Choice(listOf("Áno", "Nie"), if (inside) 0 else 1,
                    if (inside) "Áno: vektor spĺňa obe rovnice." else "Nie: vektor niektorú rovnicu nespĺňa.",
                    wrong = if (inside) mapOf(1 to "Dosaď do rovníc: obe platia. Ľavé strany dajú po dosadení " + f.mulVec(a, v).joinToString(" a ") { "§${f.norm(it).tex(true)}§" } + ", presne pravé strany, takže vektor je riešením sústavy a varieta ho obsahuje.")
                    else mapOf(0 to "Dosaď do rovníc: ${rowCheck(f, a, b, v, false)}")),
                    hint = "Dosaď vektor do rovníc. Varieta ho obsahuje, len ak platia všetky.", aim = 2) },
            ),
            postup = K4ParamPostup,
            solution = {
                val (solve, param, member) = K4ParamPostup
                K4ParamPostup.solve {
                    solve("Neparametrické rovnice sú sústava §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§; jej množina riešení je varieta (Postup 4.10).")
                    frobenius(f, a, s, names, FrobeniusAims(solve, solve, solve, solve, solve))
                    val ts = s.kernel.indices.map { "t_{${it + 1}}" }
                    param("Parametrický tvar je bod plus kombinácie bázy §S_0§; dimenzia variety je počet parametrov, §n-h(\\mathbf{A})=$d§. Parametrické rovnice z nej dostanem rozpisom po zložkách, lebo tak ich definuje Poznámka 4.31.",
                        listOf(Formula("(${names.joinToString(",")})=${part.tex()}" + s.kernel.indices.joinToString("") { "+${ts[it]}${s.kernel[it].tex()}" }),
                            Formula(parametricTex(f, names, part, s.kernel, ts))))
                    member(if (inside) "Dosadím §${v.tex()}§ do oboch rovníc: obe platia, varieta vektor obsahuje." else "Dosadím §${v.tex()}§ do rovníc a niektorá neplatí, varieta ho neobsahuje. ${rowCheck(f, a, b, v, false)}")
                }
            },
            hint = "Vyrieš sústavu: bod (voľné 0) plus báza riešení homogénnej sústavy. Dimenzia je n mínus hodnosť.",
        )
    }
}

/* ---- 3. three points of a plane to its equations (MARAST 14158, Příklad 4.33) ------------------------------------ */

/** Postup 4.30 for the plane through three points [pts], every number from the engine (generator and Příklad 4.33). */
internal fun planeSolution(f: Field, pts: List<Vec>): Solution {
    val p = (f as? Field.Zp)?.p
    val us = listOf(f.plus(pts[1], f.scale(-Frac.ONE, pts[0])), f.plus(pts[2], f.scale(-Frac.ONE, pts[0])))
    val (am, bv) = f.equationsOf(pts[0], us)
    val (dirs, homog, rows, eqs) = PlanePostup
    return PlanePostup.solve {
        dirs("Vektor posunutia je prvý bod §\\mathbf{a}=${pts[0].tex()}§, smerové vektory sú rozdiely druhého a tretieho bodu od neho" + (if (p != null) " modulo §$p§." else "."),
            "\\begin{aligned}\\mathbf{u}&=${us[0].tex()}\\\\\\mathbf{w}&=${us[1].tex()}\\end{aligned}")
        dirs("Smerové vektory sú LN, body teda neležia na priamke a §W=\\mathbf{a}+\\langle\\mathbf{u},\\mathbf{w}\\rangle§ je rovina.")
        homog("Podľa Postupu 4.30 riešim homogénnu sústavu so smerovými vektormi v riadkoch.", gemLines(f.echelon(Mat(us.map { it + Frac.ZERO }, us[0].size))))
        val hs = f.solveAll(Mat(us), List(2) { Frac.ZERO })
        val vn = vars(4)
        homog("Voľné premenné sú §${hs.free.joinToString(",") { vn[it] }}§: postupne jednu položím §1§, ostatné §0§, a viazané dopočítam. Tak dostanem bázu riešení.", hs.kernel.map { Formula(it.tex()) })
        if (am.rows != hs.kernel) rows("Každý vektor bázy vynásobím tak, aby zmizli zlomky; nenulový násobok vektora bázy dá opäť bázu.", am.rows.map { Formula(it.tex()) })
        rows("Vektory bázy riešení idú do riadkov §\\mathbf{A}§ a pravá strana je §\\mathbf{A}\\mathbf{a}§, aby §\\mathbf{a}§ bolo riešenie.", listOf(Formula("\\mathbf{A}=${am.tex()}"), Formula("\\mathbf{d}=\\mathbf{A}\\mathbf{a}=${bv.tex()}^T")))
        val names = vars(4)
        eqs("Riadky §(\\mathbf{A}\\mid\\mathbf{d})§ prepíšem na rovnice.", "\\begin{aligned}" + am.rows.indices.joinToString(",\\\\") { equationTex(am.rows[it], bv[it], names).replaceFirst("=", "&=") } + "\\end{aligned}")
        eqs("Skúška: všetky tri body rovnice spĺňajú a §h(\\mathbf{A})=2=4-\\dim W§, teda riešenia tvoria rovinu dimenzie §2§ a nič navyše (Věta 4.26). Iná báza riešení dá iné, rovnako správne rovnice.")
    }
}

fun k4EquationsExercise(r: Random): Exercise {
    while (true) {
        val f: Field = if (r.nextBoolean()) Field.Zp(5) else Field.R
        val lo = if (f is Field.Zp) 0 else -1; val hi = if (f is Field.Zp) 4 else 3
        val pts = List(3) { f.vec(List(4) { r.pick(lo, hi) }) }
        val us = listOf(f.plus(pts[1], f.scale(-Frac.ONE, pts[0])), f.plus(pts[2], f.scale(-Frac.ONE, pts[0])))
        if (f.rank(us) != 2) continue
        val (am, bv) = f.equationsOf(pts[0], us)
        if (f is Field.R && (am.rows.flatten() + bv).any { !it.isInt || kotlin.math.abs(it.num) > 9 }) continue
        fun isW(a: Mat, b: Vec) = pts.all { f.solves(a, b, it) } && f.rankOf(a) == 2 && f.solveAll(a, b).particular != null
        if (!isW(am, bv)) error("Postup 4.30 nevyšiel")
        val wrongTheta = am to List(2) { Frac.ZERO }
        val wrongRows = Mat(us) to f.mulVec(Mat(us), pts[0])
        if (listOf(wrongTheta, wrongRows).any { (a, b) -> isW(a, b) }) continue
        if (wrongTheta.second == bv) continue
        fun sys(a: Mat, b: Vec) = Mat(a.rows.indices.map { a.rows[it] + listOf(b[it]) }, 4).tex()
        val opts = listOf(sys(am, bv), sys(wrongTheta.first, wrongTheta.second), sys(wrongRows.first, wrongRows.second))
        val order = listOf(0, 1, 2).shuffled(r)
        val sp = if (f is Field.Zp) "\\mathbb{Z}_5^4" else "\\mathbb{R}^4"
        val p = (f as? Field.Zp)?.p
        val negB = bv.map { f.neg(it) }
        return Exercise(
            "la1-k4-equations", "Rovnice roviny z troch bodov", "Šablóna podľa MARAST, ukážková skúšková písomka, príklad 14158, a Príkladu 4.33",
            "Najděte neparametrické rovnice roviny v \$$sp$, která obsahuje vektory $\\mathbf{a}=${pts[0].tex()}$, $\\mathbf{b}=${pts[1].tex()}$ a $\\mathbf{c}=${pts[2].tex()}$.",
            parts = listOf(
                { _ -> Part("Smerové vektory: §\\mathbf{b}-\\mathbf{a}§ (prvé štyri čísla) a §\\mathbf{c}-\\mathbf{a}§:".replace('§', '$'), Answer.Numbers(listOf("u_1", "u_2", "u_3", "u_4", "w_1", "w_2", "w_3", "w_4"), p, us[0] + us[1], "$\\mathbf{u}=${us[0].tex()}$, $\\mathbf{w}=${us[1].tex()}$", fractions = false, diagnose = diagnosis(f, us[0] + us[1], listOf(
                    (f.plus(pts[0], f.scale(-Frac.ONE, pts[1])) + f.plus(pts[0], f.scale(-Frac.ONE, pts[2]))) to "Odčítal si opačne. Aj to sú smerové vektory, ale úloha chce §\\mathbf{b}-\\mathbf{a}§ a §\\mathbf{c}-\\mathbf{a}§.",
                    (pts[1] + pts[2]) to "Samotné body nemusia byť smerové vektory. Smer je rozdiel dvoch rôznych bodov variety.",
                )) { "Smerový vektor je rozdiel dvoch rôznych bodov, po zložkách. " + (if (p != null) "Ber modulo $p." else "Skontroluj znamienka.") }),
                    hint = m("Smerový vektor je rozdiel dvoch bodov: od zložiek §\\mathbf{b}§ odčítaj zložky §\\mathbf{a}§, rovnako pri §\\mathbf{c}§."), aim = 0) },
                { _ -> Part("Pre maticu §\\mathbf{A}=${am.tex()}§ dopočítaj pravú stranu §\\mathbf{d}§:".replace('§', '$'), Answer.Numbers(listOf("d_1", "d_2"), p, bv, "$\\mathbf{d}=\\mathbf{A}\\mathbf{a}=${bv.tex()}^T$", diagnose = diagnosis(f, bv, listOf(
                    List(2) { Frac.ZERO } to "Nulová pravá strana dá zameranie, nie rovinu cez §\\mathbf{a}§. Pravá strana je §\\mathbf{A}\\mathbf{a}§ (Věta 4.26).",
                    negB to "Vyšlo ti všetko s opačným znamienkom. Pravá strana je §\\mathbf{A}\\mathbf{a}§, nie §-\\mathbf{A}\\mathbf{a}§.",
                )) { "Pravá strana je §\\mathbf{A}\\mathbf{a}§: dosaď bod §\\mathbf{a}§ do ľavých strán rovníc." }),
                    hint = m("Pravá strana je §\\mathbf{d}=\\mathbf{A}\\mathbf{a}§, aby bod §\\mathbf{a}§ sústavu spĺňal. Vynásob riadky §\\mathbf{A}§ vektorom §\\mathbf{a}§."), aim = 2) },
                { _ -> Part("Ktorá sústava má rovinu za množinu riešení?", Answer.Choice(order.map { "$" + opts[it] + "$" }, order.indexOf(0),
                    "Táto sústava má za riešenia práve zadanú rovinu. Riadky §\\mathbf{A}§ sú báza riešení sústavy so smerovými vektormi v riadkoch, pravá strana §\\mathbf{A}\\mathbf{a}§ (Postup 4.30).".replace('§', '$'),
                    wrong = mapOf(order.indexOf(1) to "S nulovou pravou stranou je to zameranie, podpriestor cez §\\theta§. Rovina cez §\\mathbf{a}§ chce pravú stranu §\\mathbf{A}\\mathbf{a}§.",
                        order.indexOf(2) to "Smerové vektory nepatria do riadkov §\\mathbf{A}§. Riadky §\\mathbf{A}§ sú riešenia sústavy, ktorá má smerové vektory v riadkoch.")),
                    hint = m("Dosaď body §\\mathbf{a}§, §\\mathbf{b}§ a §\\mathbf{c}§ do rovníc každej sústavy. Rovinu opisuje tá, ktorú spĺňajú všetky tri."), aim = 3) },
            ),
            postup = PlanePostup,
            solution = { planeSolution(f, pts) },
            hint = "Smerové vektory sú rozdiely bodov. Dáš ich do riadkov homogénnej sústavy, báza jej riešení sú riadky §\\mathbf{A}§. Pravá strana je §\\mathbf{d}=\\mathbf{A}\\mathbf{a}§.".replace('§', '$'),
        )
    }
}

/* ---- 4. a system whose solutions are a given subspace or variety (Postup 4.30; Klouda, 8. prednáška) ------------ */

/** "2\cdot 1+3\cdot(-1)=-1": the left side of a row's equation after substituting [x], the zero terms left out. */
private fun substTex(f: Field, row: Vec, x: Vec): String {
    val idx = row.indices.filter { !row[it].isZero && !x[it].isZero }
    if (idx.isEmpty()) return "0"
    val raw = idx.fold(Frac.ZERO) { acc, j -> acc + row[j] * x[j] }
    val value = f.norm(raw)
    val terms = Terms.lin(idx.map { row[it] }, idx.map { Terms.paren(x[it]) })
    return when {
        idx.size == 1 && row[idx[0]] == Frac.ONE && raw == value -> terms
        raw == value -> "$terms=${value.tex(true)}"
        else -> "$terms=${raw.tex(true)}\\equiv ${value.tex(true)}\\pmod{${(f as Field.Zp).p}}"
    }
}

/**
 * The rows of (A|b), or of A alone when [a] is null (the subspace), for a system whose solution set is exactly
 * a + ⟨[basis]⟩. Judged by the property, never against one sample: every row nonzero, every basis vector solves its
 * homogeneous equation, b_i is the left side at [a], the rows of A LN. With n − k rows that is exactly the condition
 * (Věta 4.23 and 4.26): P ⊆ S_0, dim S_0 = n − h(A) = k, so S_0 = P, and a solves the system.
 */
internal fun judgeSustava(f: Field, basis: List<Vec>, a: Vec?, entries: List<List<Vec>>): VectorsResult {
    val n = basis[0].size; val k = basis.size; val rn = n - k
    fun res(ok: Boolean, head: String, each: List<String?>) = VectorsResult(ok, m(head), each.map { it?.let(::m) })
    fun value(row: Vec, x: Vec) = f.norm(row.indices.fold(Frac.ZERO) { acc, j -> f.add(acc, f.mul(row[j], x[j])) })
    val rows = entries.map { e -> e[0].map(f::norm) }
    // b = θ everywhere is the subspace's system: the one slip worth naming; a single zero is just a wrong number
    val allZero = a != null && rows.all { it.getOrNull(n)?.isZero == true }
    val set = if (a == null) "P" else "W"
    val dirs = if (a == null) "bázy §P§" else "bázy zamerania"
    if (rows.size != rn) return res(false, "Riadkov má byť §$rn§, máš §${rows.size}§. LN riadky dajú §h(\\mathbf{A})§ a §\\dim S_0=n-h(\\mathbf{A})§ má byť §$k§.", rows.map { null })
    val each = rows.mapIndexed { i, row ->
        val ar = row.take(n); val r = i + 1
        val bad = basis.firstOrNull { value(ar, it) != Frac.ZERO }
        when {
            ar.isZero() -> "Riadok §$r§ matice §\\mathbf{A}§ je nulový, s ním sú riadky LZ. Taká rovnica nič nevylúči, a s nenulovou pravou stranou nemá riešenie vôbec."
            bad != null -> "Vektor §${bad.tex()}§ nespĺňa homogénnu rovnicu riadku §$r§: dosadenie dá §${substTex(f, ar, bad)}§, nie §0§. Každý vektor $dirs musí riešiť §\\mathbf{A}\\mathbf{x}=\\theta§, inak nie je v §S_0§."
            a != null && row[n] != value(ar, a) -> if (allZero)
                "Pravá strana §0§ dá zameranie, nie varietu §W§. Bod §\\mathbf{a}=${a.tex()}§ musí sústavu spĺňať, preto §b_{$r}=${substTex(f, ar, a)}§."
            else "Pravá strana riadku §$r§ nesedí: dosadenie §\\mathbf{a}=${a.tex()}§ dá §${substTex(f, ar, a)}§, nie §${row[n].tex(true)}§. Bod §\\mathbf{a}§ musí byť riešením, preto §b_{$r}§ je ľavá strana po jeho dosadení."
            else -> null
        }
    }
    if (each.any { it != null }) return res(false, "Takto to nesedí. Pri riadkoch je napísané prečo.", each)
    val as0 = rows.map { it.take(n) }
    if (f.rank(as0) < rn) {
        // the first row that is a combination of the rows before it (none is zero, so it is not the first)
        val j = as0.indices.first { f.rank(as0.take(it + 1)) <= it }
        val c = f.solve(as0.take(j), as0[j])!!
        return res(false, "Riadky §\\mathbf{A}§ sú LZ, a zadanie chce LN: §${as0[j].tex()}=${Terms.lin(c, as0.take(j).map { it.tex() })}§. Taký riadok nepridá podmienku, preto §h(\\mathbf{A})<$rn§ a množina riešení by bola väčšia než §$set§.",
            each.mapIndexed { i, e -> if (i == j) "${if (j == 1) "Tento riadok je násobkom 1. riadku." else "Tento riadok je kombináciou predošlých."} Skús namiesto neho iné riešenie homogénnej sústavy s bázou v riadkoch." else e })
    }
    return res(true, "Správne, množina riešení tejto sústavy je práve §$set§. " +
        (if (rn == 1) "Riadok je nenulový, teda LN" else "Riadky sú LN a je ich §$rn§") + ", preto §\\dim S_0=n-h(\\mathbf{A})=$n-$rn=$k§ (Frobeniova veta). " +
        (if (k == 1) "Vektor $dirs je nenulový, teda LN, rieši §\\mathbf{A}\\mathbf{x}=\\theta§ a §\\dim S_0=1§, preto tvorí bázu §S_0§ (Věta 2.55)."
        else "Vektory $dirs sú LN riešenia §\\mathbf{A}\\mathbf{x}=\\theta§ a je ich §$k§, preto tvoria bázu §S_0§ (Věta 2.55).") +
        (if (a != null) " Bod §\\mathbf{a}§ sústavu spĺňa, preto podľa Frobeniovej vety §S=\\mathbf{a}+S_0=W§." else ""), each)
}

/** Postup 4.30 for a variety a + P: the system Ax = b (Klouda, 8. prednáška; Věta 4.23 a 4.26). */
val SustavaPostup = postup(
    "Báza do riadkov" to "Bázu zamerania zapíš do riadkov homogénnej sústavy a uprav ju GEM na HST.",
    "Vyrieš homogénnu sústavu" to "Voľné premenné postupne §1§, ostatné §0§; viazané dopočítaj spätným dosadením.",
    "Riešenia do riadkov §\\mathbf{A}§" to "Vektory bázy riešení zapíš do riadkov §\\mathbf{A}§; je ich §n-k§.",
    "Pravá strana §\\mathbf{b}=\\mathbf{A}\\mathbf{a}§" to "Dosaď bod §\\mathbf{a}§ do ľavých strán, aby bol riešením.",
    "Prečo je to presne §W§" to "Riadky sú LN, preto §\\dim S_0=n-h(\\mathbf{A})=k§; vektory bázy zamerania sú LN riešenia, je ich §k§, teda tvoria bázu §S_0§.",
)

/** Postup 4.30 for a subspace P: the homogeneous system Ax = θ (Věta 4.23). */
val SustavaPodpriestorPostup = postup(
    "Báza do riadkov" to "Bázu §P§ zapíš do riadkov homogénnej sústavy a uprav ju GEM na HST.",
    "Vyrieš homogénnu sústavu" to "Voľné premenné postupne §1§, ostatné §0§; viazané dopočítaj spätným dosadením.",
    "Riešenia do riadkov §\\mathbf{A}§" to "Vektory bázy riešení zapíš do riadkov §\\mathbf{A}§; je ich §n-k§.",
    "Prečo je to presne §P§" to "Riadky sú LN, preto §\\dim S_0=n-h(\\mathbf{A})=k§; vektory bázy §P§ sú LN riešenia, je ich §k§, teda tvoria bázu §S_0§.",
)

/**
 * Postup 4.30, every number from the engine: the basis of P in the rows of (U|θ), GEM, the basis of its solutions by
 * back substitution, those vectors as the rows of A (over R scaled to whole numbers by [equationsOf]), b = Aa when [a]
 * is given, and why the solution set is exactly a + P (Věta 4.23 a 4.26).
 */
internal fun sustavaSolution(f: Field, basis: List<Vec>, a: Vec?): Solution {
    val n = basis[0].size; val k = basis.size; val rn = n - k
    val postup = if (a == null) SustavaPodpriestorPostup else SustavaPostup
    val rowsAim = postup[0]; val solveAim = postup[1]; val aAim = postup[2]; val why = postup.aims.last()
    val (am, bv) = f.equationsOf(a ?: List(n) { Frac.ZERO }, basis)
    val hs = f.solveAll(Mat(basis), List(k) { Frac.ZERO })
    val cs = (1..n).map { "c_{$it}" }
    val xs = (1..n).map { "x_{$it}" }
    val mod = if (f is Field.Zp) " Počítam modulo §${f.p}§." else ""
    val dirs = if (a == null) "bázy §P§" else "bázy zamerania"
    return postup.solve {
        rowsAim("Každý vektor $dirs musí riešiť §\\mathbf{A}\\mathbf{x}=\\theta§, preto riadok §(c_1,\\dots,c_{$n})§ matice §\\mathbf{A}§ rieši homogénnu sústavu s bázou v riadkoch." +
            (if (hs.e.steps.isEmpty()) " Jej matica už je v HST.$mod" else " Upravím ju GEM na HST.$mod"), gemLines(hs.e))
        for ((i, fr) in hs.free.withIndex()) {
            val lines = backSubst(f, hs.e, cs, hs.free.associateWith { if (it == fr) Frac.ONE else Frac.ZERO }, homogeneous = true).first + Formula("\\mathbf{c}_{${i + 1}}=${hs.kernel[i].tex()}")
            if (i == 0 && hs.free.size == 1) solveAim("Voľná premenná je len §${cs[fr]}§, lebo §n-k=$n-$k=1§. Položím §${cs[fr]}=1§ a viazané dopočítam zdola nahor.", lines)
            else if (i == 0) solveAim("Voľné premenné sú §${hs.free.joinToString(",") { cs[it] }}§, je ich §$n-$k=$rn§. Položím §${cs[fr]}=1§, ostatné voľné §0§, a viazané dopočítam zdola nahor.", lines)
            else solveAim("Teraz §${cs[fr]}=1§, ostatné voľné §0§, a znova dopočítam viazané.", lines)
        }
        if (am.rows != hs.kernel) aAim("Každý vektor bázy riešení vynásobím tak, aby zmizli zlomky; nenulový násobok riešenia je zase riešenie a vektory ostanú LN.", am.rows.map { Formula(it.tex()) })
        aAim(if (rn == 1) "Vektor bázy riešení je jediný riadok §\\mathbf{A}§: po dosadení ktoréhokoľvek vektora $dirs dá nulu." else "Vektory bázy riešení zapíšem do riadkov §\\mathbf{A}§: každý dá po dosadení ktoréhokoľvek vektora $dirs nulu.", Formula("\\mathbf{A}=${am.tex()}"))
        if (a != null) postup[3]("Bod §\\mathbf{a}=${a.tex()}§ musí sústavu spĺňať, preto pravá strana každého riadku je jeho ľavá strana po dosadení §\\mathbf{a}§.",
            am.rows.mapIndexed { i, row -> Formula("b_{${i + 1}}=${substTex(f, row, a)}") } + Formula("\\mathbf{b}=\\mathbf{A}\\mathbf{a}=${bv.tex()}^T"))
        why((if (rn == 1) "Riadok §\\mathbf{A}§ je nenulový, teda LN." else "Riadky §\\mathbf{A}§ sú LN, lebo každý má nenulové číslo vo svojej voľnej premennej, kde ostatné majú §0§.") + " Preto §h(\\mathbf{A})=$rn§ a podľa Frobeniovej vety §\\dim S_0=n-h(\\mathbf{A})=$k§.",
            "\\dim S_0=$n-$rn=$k")
        why((if (k == 1) "Vektor $dirs je nenulový, teda LN, rieši §\\mathbf{A}\\mathbf{x}=\\theta§ a §\\dim S_0=1§, preto tvorí bázu §S_0§ (Věta 2.55)."
            else "Vektory $dirs sú LN riešenia §\\mathbf{A}\\mathbf{x}=\\theta§ a je ich §$k=\\dim S_0§, preto tvoria bázu §S_0§ (Věta 2.55).") +
            (if (a == null) " Teda §S_0=P§: množina riešení homogénnej sústavy je práve §P§." else " Teda §S_0§ je zameranie §W§; bod §\\mathbf{a}§ sústavu spĺňa, preto podľa Frobeniovej vety §S=\\mathbf{a}+S_0=W§."))
        why("Hľadaná sústava, rozpísaná na rovnice" + (if (f is Field.Zp) " nad §\\mathbb{Z}_{${f.p}}§:" else ":"),
            "\\begin{aligned}" + am.rows.indices.joinToString(",\\\\") { equationTex(am.rows[it], bv[it], xs).replaceFirst("=", "&=") } + "\\end{aligned}")
    }
}

fun k4SustavaExercise(r: Random): Exercise {
    while (true) {
        val f: Field = listOf(Field.Zp(5), Field.Zp(5), Field.Zp(3), Field.Zp(7), Field.R, Field.R).random(r)
        val p = (f as? Field.Zp)?.p
        // n − k rows of n (+1) cells each: at most 3 rows to type
        val n = r.pick(4, 5)
        val k = if (n == 4) r.pick(1, 3) else r.pick(2, 3)
        val subspace = r.nextInt(5) < 2
        fun draw() = if (p != null) r.pick(0, p - 1) else r.pick(-2, 2)
        val basis = List(k) { f.vec(List(n) { draw() }) }
        if (!f.isLN(basis) || basis.any { v -> v.count { !it.isZero } < 2 }) continue
        val hs = f.solveAll(Mat(basis), List(k) { Frac.ZERO })
        if (k > 1 && hs.e.steps.isEmpty()) continue
        val a = if (subspace) null else f.vec(List(n) { draw() })
        val (am, bv) = f.equationsOf(a ?: List(n) { Frac.ZERO }, basis)
        if (a != null && bv.all { it.isZero }) continue // a in P: then W = P, the subspace variant
        if (f is Field.R && (am.rows.flatten().any { !it.isInt || kotlin.math.abs(it.num) > 6 } || bv.any { !it.isInt || kotlin.math.abs(it.num) > 12 })) continue
        val rn = n - k
        val sp = if (p != null) "\\mathbb{Z}_{$p}^{$n}" else "\\mathbb{R}^{$n}"
        val span = "\\langle " + basis.joinToString(",") { it.tex() } + "\\rangle"
        val set = if (a == null) "P" else "W"
        val text = if (a == null) m("Najděte matici §\\mathbf{A}§ s lineárně nezávislými řádky takovou, že množina řešení homogenní soustavy §\\mathbf{A}\\mathbf{x}=\\theta§ je právě podprostor §P=$span\\subseteq $sp§.")
            else m("Najděte soustavu §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§ s lineárně nezávislými řádky matice §\\mathbf{A}§, jejíž množina řešení je právě varieta §W=${a.tex()}+$span\\subseteq $sp§.")
        val cells = (1..n).map { "a_{i$it}" } + (if (a == null) emptyList() else listOf("b_i"))
        val example = am.rows.mapIndexed { i, row -> listOf(if (a == null) row else row + bv[i]) }
        val dim = if (a == null) "\\dim P" else "\\dim W"
        return Exercise(
            "la1-k4-sustava", "Sústava k variete", "Klouda, 8. prednáška (18. 11. 2026); Věta 4.23 a 4.26, Postup 4.30; skúška 26. 1. 2026 (variant Hilbert), príklad 3C (dôkaz existencie)",
            text,
            parts = listOf(
                { _ -> Part(m("Koľko riadkov má §\\mathbf{A}§?"), Answer.Numbers(listOf("m"), null, listOf(rn.q()), "\$m=n-$dim=$n-$k=$rn$", diagnose = diagnosis(Field.R, listOf(rn.q()), listOf(
                    listOf(k.q()) to "To je §$dim§. Riadkov je §n-$dim§, lebo každý LN riadok zníži dimenziu riešení o §1§.",
                    listOf(n.q()) to "§n§ je počet neznámych, teda stĺpcov §\\mathbf{A}§. Riadky sú LN, ich počet je §h(\\mathbf{A})§, a §\\dim S_0=n-h(\\mathbf{A})§ má byť §$k§.",
                )) { "Riadky sú LN, ich počet je §h(\\mathbf{A})§. Podľa Frobeniovej vety §\\dim S_0=n-h(\\mathbf{A})§, a to má byť §$dim=$k§." }),
                    hint = m("Riadky sú LN, takže ich počet je §h(\\mathbf{A})§. Frobeniova veta: §\\dim S_0=n-h(\\mathbf{A})§, a §S_0§ má byť ${if (a == null) "§P§" else "zameranie §W§"} s dimenziou §$k§."), aim = 2) },
                { _ -> Part(
                    // one wording for every instance: the number of rows is the previous answer, so "riadok"/"riadky" would tell it (PartLeakTest)
                    if (a == null) m("Napíš riadky §\\mathbf{A}§ homogénnej sústavy, ktorej množina riešení je práve §P§.")
                    else m("Napíš riadky §(\\mathbf{A}\\mid\\mathbf{b})§ sústavy, ktorej množina riešení je práve §W§: v každom najprv riadok §\\mathbf{A}§, na konci pravú stranu."),
                    Answer.Vectors(cells, p, rn..rn, entry = "riadok", check = { e -> judgeSustava(f, basis, a, e) }, example = example),
                    hint = m("Bázu zapíš do riadkov homogénnej sústavy a vyrieš ju; vektory bázy jej riešení sú riadky §\\mathbf{A}§." + (if (a == null) "" else " Pravá strana je §\\mathbf{b}=\\mathbf{A}\\mathbf{a}§.")),
                    aim = 2) },
            ),
            postup = if (a == null) SustavaPodpriestorPostup else SustavaPostup,
            solution = { sustavaSolution(f, basis, a) },
            hint = m("Bázu daj do riadkov homogénnej sústavy a vyrieš ju. Vektory bázy riešení sú riadky §\\mathbf{A}§, pravá strana §\\mathbf{b}=\\mathbf{A}\\mathbf{a}§."),
        )
    }
}

/** A variety given by equations to its parametric form (skúška 26. 1. 2026, príklad 3D). */
val K4ParamPostup = postup(
    "Vyrieš sústavu" to "Rovnice sú sústava; vyrieš ju podľa Frobeniovej vety (Postup 4.10).",
    "Parametrický tvar a dimenzia" to "Bod plus kombinácie bázy §S_0§; dimenzia je počet parametrov, §n-h(\\mathbf{A})§.",
    "Obsahuje vektor?" to "Dosaď vektor do rovníc: musia platiť všetky.",
)

/** Equations of a variety through given points (Postup 4.30). */
val PlanePostup = postup(
    "Posunutie a smerové vektory" to "Jeden bod je posunutie, rozdiely bodov sú smerové vektory; over, že sú LN.",
    "Homogénna sústava" to "Smerové vektory zapíš do riadkov homogénnej sústavy a vyrieš ju.",
    "Riadky §\\mathbf{A}§ a pravá strana" to "Báza riešení sú riadky §\\mathbf{A}§; pravá strana je §\\mathbf{A}\\mathbf{a}§.",
    "Rovnice a skúška" to "Prepíš riadky na rovnice a dosaď do nich dané body.",
)

internal val K4Generators: Map<String, (Random) -> Exercise> = mapOf(
    "la1-k4-solve" to ::k4SolveExercise,
    "la1-k4-param" to ::k4ParamExercise,
    "la1-k4-equations" to ::k4EquationsExercise,
    "la1-k4-sustava" to ::k4SustavaExercise,
)

internal val K4Templates = listOf(
    TemplateInfo("la1-k4-solve", "Množina riešení cez Frobeniovu vetu", "Sústava v HST nad §\\mathbb{Z}_p§ ako varieta; skúška, 7 b"),
    TemplateInfo("la1-k4-param", "Z rovníc parametricky", "Varieta zadaná rovnicami, dimenzia, obsahuje vektor?; skúška, 6 b"),
    TemplateInfo("la1-k4-equations", "Rovnice roviny z troch bodov", "Postup 4.30, MARAST 14158"),
    TemplateInfo("la1-k4-sustava", "Sústava k variete", "Věta 4.23 a 4.26, Postup 4.30; Klouda, 8. prednáška"),
)

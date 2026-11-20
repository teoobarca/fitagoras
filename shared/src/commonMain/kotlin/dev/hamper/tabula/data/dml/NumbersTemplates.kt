package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dml.numbers.*
import kotlin.math.abs
import kotlin.random.Random

/*
 * Number theory templates for the DML 2nd test (Skriptá DML, kapitola 6; Cvičení 6.5 to 6.28; tests 2023 to 2025 on
 * fit-wiki). Each template draws its numbers as a task, then everything asked is computed by tc/Nt.kt; each part is
 * one step of the paper solution, so a wrong step is caught where it happens. Feedback starts with one short
 * sentence: the app shows only that one until "Prečo?".
 */

private fun Random.r(lo: Int, hi: Int) = nextInt(lo, hi + 1)
private fun Random.rl(lo: Long, hi: Long) = nextLong(lo, hi + 1)
private fun tx(s: String) = s.replace('§', '$')
private fun disp(s: String, note: String? = null) = Formula(s, note?.let(::tx))

/** gcd, Bézout coefficients and lcm by the REA (Poznámka 6.2, Věta 6.5, Tvrzení 6.3). */
val TcReaPostup = postup(
    "Tabuľka REA" to "Riadky §(a,1,0)§ a §(b,0,1)§; každý ďalší je predpredošlý mínus §q§-násobok predošlého.",
    "§\\gcd§ a Bézoutove koeficienty" to "§\\gcd§ je posledný nenulový zvyšok, koeficienty sú v jeho riadku.",
    "§\\lcm§" to "§\\lcm(a,b)=|a|\\cdot|b|/\\gcd(a,b)§.",
)

/** A linear Diophantine equation (Věty 6.7 a 6.8, Důsledek 6.1). */
val TcLdrPostup = postup(
    "§\\gcd§ cez REA" to "REA dá §\\gcd(a,b)§ aj jeho Bézoutove koeficienty.",
    "Má rovnica riešenie?" to "Riešenie existuje práve vtedy, keď §\\gcd(a,b)§ delí pravú stranu.",
    "Jedno riešenie" to "Bézoutove koeficienty vynásob číslom §c/\\gcd§.",
    "Všetky riešenia" to "Pripočítaj §k\\cdot(b/\\gcd,-a/\\gcd)§; najmenšie §x\\geq 0§ dostaneš zvyškom.",
)

/** The inverse in Z_m (Věta 6.12). */
val TcInvPostup = postup(
    "Existuje inverzia?" to "Inverzia §a§ v §\\mathbb{Z}_m§ existuje práve vtedy, keď §\\gcd(a,m)=1§.",
    "REA na §m§ a §a§" to "§ax\\equiv 1\\pmod{m}§ je §ax+my=1§; REA dá koeficient pri §a§.",
    "Inverzia a skúška" to "Koeficient pri §a§ vezmi modulo §m§ a over §a\\cdot a^{-1}\\equiv 1§.",
)

/** The least modulus in a range for which a has an inverse, and the inverse (testy 2024 a 2025). */
val TcInvMinPostup = postup(
    "Kedy inverzia existuje" to "Inverzia existuje práve pre §m§ nesúdeliteľné s §a§; rozlož §a§ na prvočísla.",
    "Najmenší vhodný modul" to "Skúšaj moduly od najmenšieho a vyraď tie, ktoré delí niektoré prvočíslo z rozkladu §a§.",
    "Inverzia cez REA" to "REA na §m§ a §a§ dá koeficient pri §a§.",
    "Skúška" to "§a\\cdot a^{-1}§ musí dať zvyšok §1§.",
)

/** A big power modulo m with the base coprime to m (Věty 6.14, 6.15, Příklad 6.14; Spěvák, 8. prednáška). */
val TcPowPostup = postup(
    "Zredukuj základ" to "Základ nahraď jeho zvyškom modulo §m§.",
    "Zmenši exponent" to "Pri §\\gcd(a,m)=1§ dovolí Eulerova veta (pri prvočísle MFV) brať exponent modulo §\\varphi(m)§.",
    "Dopočítaj malú mocninu" to "Násob základom a po každom kroku redukuj modulo §m§.",
)

/** A remainder of a tower c^(a^n) modulo p, through a^n modulo p-1 (testy 2023 až 2025). */
val TcTowerPostup = postup(
    "a) Eulerova veta" to "Základ nesúdeliteľný s modulom: exponent stačí brať modulo §\\varphi§.",
    "a) Malá mocnina" to "Násob základom a po každom kroku redukuj modulo §p-1§.",
    "b) MFV pre exponent" to "Pri prvočísle §p§ stačí exponent modulo §p-1§, a ten je výsledok a).",
    "b) Malá mocnina" to "Násob základom a po každom kroku redukuj modulo §p§.",
)

/** A linear congruence (Věty 6.13, 6.17, 6.18). */
val TcLkonPostup = postup(
    "Má kongruencia riešenie?" to "Riešenie existuje práve vtedy, keď §\\gcd(a,m)§ delí §b§.",
    "Krátenie" to "Vydeľ §a§, §b§ aj modul číslom §\\gcd(a,m)§.",
    "Vynásob inverziou" to "Po krátení je koeficient invertovateľný; obe strany vynásob jeho inverziou.",
    "Všetky riešenia v §\\mathbb{Z}_m§" to "Riešení v §\\mathbb{Z}_m§ je §\\gcd(a,m)§, líšia sa o nový modul.",
)

/** A system of congruences (Věty 6.19 a 6.20, Příklad 6.20). */
val TcCrtPostup = postup(
    "Má sústava riešenie?" to "Nesúdeliteľné moduly: vždy (ČVOZ). Inak §\\gcd(m_i,m_j)§ musí deliť §a_i-a_j§.",
    "Postupné dosadzovanie" to "Z prvej kongruencie §x=a_1+m_1t§, dosaď do ďalšej a vyrieš pre §t§.",
    "Výsledok a skúška" to "Riešenie je jediné modulo §\\lcm§ modulov; dosaď ho do každej kongruencie.",
)

/** Two congruences solved separately, then their system (test 6. 12. 2023 (variant A), príklad 3). */
val TcComboPostup = postup(
    "Prvá kongruencia" to "Vyrieš ju zvlášť: krátenie a inverzia.",
    "Druhá kongruencia" to "Rovnako; veľkú mocninu v koeficiente najprv zredukuj Eulerovou vetou.",
    "Sústava" to "Riešenia oboch spoj postupným dosadzovaním.",
    "Najväčšie záporné riešenie" to "Od najmenšieho nezáporného odčítaj modul sústavy.",
)

/** The same when the congruence with the power is written first (2024 F, 2026 Ř); without the last subgoal when only all solutions are asked (2026 Ř). */
val TcComboPowerFirstPostup = postup(
    "Prvá kongruencia" to "Veľkú mocninu v koeficiente zredukuj Eulerovou vetou, potom inverzia.",
    "Druhá kongruencia" to "Vyrieš ju zvlášť: krátenie a inverzia.",
    "Sústava" to "Riešenia oboch spoj postupným dosadzovaním.",
    "Najväčšie záporné riešenie" to "Od najmenšieho nezáporného odčítaj modul sústavy.",
)

val TcComboAllPostup = postup(
    "Prvá kongruencia" to "Veľkú mocninu v koeficiente zredukuj Eulerovou vetou, potom inverzia.",
    "Druhá kongruencia" to "Vyrieš ju zvlášť: krátenie a inverzia.",
    "Sústava a všetky riešenia" to "Riešenia oboch spoj postupným dosadzovaním; všetky riešenia sú jedna trieda modulo §\\lcm§.",
)

/*
 * The small power left after Euler or Fermat, computed as Spěvák computes it (8. prednáška, 2^4 = 16 ≡ 7 mod 9): multiply
 * by the base and reduce after every step (Tvrzení 6.6). The binary method (Příklad 6.16) is the algorithm he announced
 * for the next lecture (3^644 mod 645); until it is lectured the generators draw only powers that need no more than this.
 */
const val TcSmallPower = 7

/** b^2, b^3, ..., b^e modulo m, one product a row: "b^3 ≡ r·b = P ≡ R (mod m)". */
internal fun chainFormula(b: Long, e: Long, m: Long): Formula {
    val bb = md(b, m)
    var r = bb
    val rows = (2..e).map { k ->
        val prod = r * bb
        val row = Terms.pow(bb, k.toInt()) + (if (k == 2L) "&=$bb\\cdot $bb" else if (r == 1L) "&\\equiv $bb" else "&\\equiv $r\\cdot $bb") +
            (if (k == 2L || r != 1L) "=$prod" else "") + (if (prod >= m) "\\equiv ${md(prod, m)}" else "")
        r = md(prod, m)
        row
    }
    return disp("\\begin{aligned}" + rows.joinToString(",\\\\") + "\\pmod{$m}\\end{aligned}", "Každý riadok je predošlý krát §$bb§, hneď zredukovaný.")
}
/** The same chain in one line, for a diagnosis: "7^2\equiv 49,\ 7^3\equiv 43,\ 7^4\equiv 1,\ 7^5\equiv 7". */
internal fun stepChain(b: Long, e: Long, m: Long): String {
    val bb = md(b, m)
    if (e <= 1L) return "${Terms.pow(bb, e.toInt())}\\equiv ${powMod(b, e, m)}"
    return (2..e).joinToString(",\\ ") { k -> val prev = powMod(b, k - 1, m); "${Terms.pow(bb, k.toInt())}\\equiv " + (if (prev == 1L) "" else "$prev\\cdot $bb=${prev * bb}\\equiv ") + "${powMod(b, k, m)}" }
}
private fun p(n: Long) = if (n < 0) "($n)" else "$n"
private const val MOD = "\\bmod "

/** A numeric answer; [diag] gets the typed integers (fractions never come: the keypad has no slash). */
private fun nums(labels: List<String>, expected: List<Long>, shown: String, diag: (List<Long>) -> String) =
    Answer.Numbers(labels, null, expected.map { Frac.of(it) }, tx(shown), diagnose = { g -> tx(diag(g.map { if (it.isInt) it.num else Long.MIN_VALUE })) })

/** The first candidate equal to the answer names the mistake (never one equal to the right answer), else [fallback]. */
private fun pickDiag(expected: List<Long>, given: List<Long>, cands: List<Pair<List<Long>?, String>>, fallback: () -> String): String =
    cands.firstOrNull { (c, _) -> c != null && c != expected && c == given }?.second ?: fallback()

private fun yesNo(yes: Boolean, whyYes: String, whyNo: String, wrongYes: String, wrongNo: String) =
    Answer.Choice(listOf("Áno", "Nie"), if (yes) 0 else 1, tx(if (yes) whyYes else whyNo), wrong = mapOf((if (yes) 1 else 0) to tx(if (yes) wrongNo else wrongYes)))

/** "2^2\cdot 3\cdot 5" */
fun tcFactorTex(n: Long) = factor(n).joinToString("\\cdot ") { (q, e) -> if (e == 1) "$q" else "$q^{$e}" }

/** phi(n) written as Věta 6.16 computes it, factor by factor. */
fun tcPhiTex(n: Long): String {
    val f = factor(n)
    if (f.size == 1 && f[0].second == 1) return "\\varphi($n)=$n-1=${n - 1}"
    val parts = f.joinToString("\\cdot ") { (q, e) -> if (e == 1) "($q-1)" else "($q^{$e}-$q^{${e - 1}})".replace("^{1}", "") }
    return "\\varphi($n)=\\varphi(${tcFactorTex(n)})=$parts=${phi(n)}"
}

/** The same chain as a display formula, one equality per line, so it fits a 320 dp card and never wraps in a sentence. */
fun tcPhiDisp(n: Long): String = "\\begin{aligned}" + tcPhiTex(n).split('=').let { e -> e[0] + "&=" + e.drop(1).joinToString("\\\\&=") } + "\\end{aligned}"

/* ---- 1. gcd, Bézout coefficients, lcm by the REA (Cvičení 6.5) ------------------------------------------------- */

data class TcReaTask(val a: Long, val b: Long)

fun tcReaTask(r: Random): TcReaTask {
    while (true) {
        val g = listOf(1L, 1L, 2L, 3L, 4L, 6L, 7L, 12L, 14L, 21L).random(r)
        val x = r.rl(4, 60); val y = r.rl(3, 60)
        if (x == y || gcd(x, y) != 1L) continue
        val a = g * x; val b = g * y
        if (a !in 20..420 || b !in 20..420) continue
        if (rea(maxOf(a, b), minOf(a, b)).rows.size < 6) continue
        val sa = if (r.nextInt(4) == 0) -1L else 1L
        val sb = if (r.nextInt(4) == 0) -1L else 1L
        return TcReaTask(sa * a, sb * b)
    }
}

/** gcd(a,b) = m a + n b with the REA's coefficients, mapped back to the order and signs of the task. */
fun tcBezout(a: Long, b: Long): Triple<Long, Long, Long> {
    val x = abs(a); val y = abs(b)
    val t = if (x >= y) rea(x, y) else rea(y, x)
    val (ca, cb) = if (x >= y) t.alpha to t.beta else t.beta to t.alpha
    return Triple(t.gcd, ca * (if (a < 0) -1 else 1), cb * (if (b < 0) -1 else 1))
}

fun tcRea(task: TcReaTask): Exercise {
    val (a, b) = task
    val x = abs(a); val y = abs(b)
    val t = if (x >= y) rea(x, y) else rea(y, x)
    val swapped = x < y
    val (g, m, n) = tcBezout(a, b)
    val l = lcm(a, b)
    val prev = t.rows[t.rows.size - 3]
    val zero = t.rows.last()
    fun mapBack(al: Long, be: Long): List<Long> { val (ca, cb) = if (swapped) be to al else al to be; return listOf(ca * (if (a < 0) -1 else 1), cb * (if (b < 0) -1 else 1)) }
    val unsigned = (if (swapped) listOf(t.beta, t.alpha) else listOf(t.alpha, t.beta))
    val signed = a < 0 || b < 0
    return Exercise(
        "dml-tc-rea", "gcd a Bézoutove koeficienty", "Šablóna Cvičení 6.5; Věta 6.5, Poznámka 6.2",
        tx("Pro §a=$a§ a §b=$b§ najděte §\\gcd(a,b)§ rozšířeným Eukleidovým algoritmem a vyjádřete jej jako lineární kombinaci hodnot §a§ a §b§ (tj. nalezněte Bézoutovy koeficienty). S pomocí získaných hodnot určete §\\lcm(a,b)§."),
        parts = listOf(
            { _ -> Part(tx("§\\gcd($a,$b)§:"), nums(listOf("\\gcd"), listOf(g), "§\\gcd($a,$b)=$g§") { gv ->
                val v = gv[0]
                when {
                    v == prev.r && prev.r != g -> "Skončil si o riadok skôr. §\\gcd§ je posledný nenulový zvyšok, pod ním musí byť §0§ (Věta 6.3)."
                    v < 0 -> "§\\gcd§ nie je záporný. Definice 6.3 ho berie z §\\mathbb{N}_0§; znamienka §a,b§ nehrajú rolu (Lemma 6.1)."
                    v > 0 && x % v == 0L && y % v == 0L -> "§$v§ je spoločný deliteľ, ale nie najväčší. REA pokračuj, kým nevyjde zvyšok §0§."
                    else -> "§$v§ nedelí obe čísla, preto to nie je ani spoločný deliteľ. Prepočítaj zvyšky: §r_i=r_{i-2}-q_{i-1}r_{i-1}§."
                }
            }, aim = 0, hint = tx("Vyplň tabuľku §r_i,\\alpha_i,\\beta_i,q_i§ od riadkov §(a,1,0)§ a §(b,0,1)§; znamienka §a,b§ nehrajú rolu. §\\gcd§ je posledný nenulový zvyšok, pod ním je §0§.")) },
            { _ -> Part(tx("Bézoutove koeficienty z tabuľky REA, §\\gcd(a,b)=m\\cdot a+n\\cdot b§:"), nums(listOf("m", "n"), listOf(m, n), "§$g=${p(m)}\\cdot ${p(a)}+${p(n)}\\cdot ${p(b)}§") { gv ->
                pickDiag(listOf(m, n), gv, listOf(
                    listOf(n, m) to "Prehodil si koeficienty. Prvý patrí k §a=$a§, druhý k §b=$b§ (Poznámka 6.2: pozor na poradie).",
                    (if (signed) unsigned else null) to "Zabudol si na znamienko. Tabuľka počíta s §|a|,|b|§; pri zápornom čísle otoč znamienko jeho koeficientu.",
                    mapBack(prev.alpha, prev.beta) to "To je riadok nad posledným nenulovým. Ten dáva §${prev.r}§, nie §\\gcd=$g§.",
                    mapBack(zero.alpha, zero.beta) to "To je riadok so zvyškom §0§: kombinácia dá §0§, nie §\\gcd§. Koeficienty ber z riadku nad ním.",
                )) {
                    val v = gv[0] * a + gv[1] * b
                    if (v == g) "Platná Bézoutova dvojica, ale nie tá z tabuľky REA. Koeficienty nie sú jednoznačné (Věta 6.4); tabuľka dá §m=$m§, §n=$n§."
                    else "Skúška nesedí: §${p(gv[0])}\\cdot ${p(a)}+${p(gv[1])}\\cdot ${p(b)}=$v\\neq $g§. Prepočítaj §\\alpha_i=\\alpha_{i-2}-q_{i-1}\\alpha_{i-1}§."
                }
            }, aim = 1, hint = tx("Koeficienty sú v riadku posledného nenulového zvyšku; pozor, §m§ patrí k §a§ a §n§ k §b§. Pri zápornom čísle otoč znamienko jeho koeficientu.")) },
            { _ -> Part(tx("§\\lcm($a,$b)§:"), nums(listOf("\\lcm"), listOf(l), "§\\lcm=\\frac{$x\\cdot $y}{$g}=$l§") { gv ->
                pickDiag(listOf(l), gv, listOf(
                    listOf(x * y) to "Zabudol si deliť §\\gcd§. Platí §\\lcm(a,b)=|a|\\cdot|b|/\\gcd(a,b)§ (Tvrzení 6.3).",
                    listOf(-l) to "§\\lcm§ nie je záporný: Definice 6.3 ho berie z §\\mathbb{N}_0§.",
                    listOf(a * b) to "Zabudol si deliť §\\gcd§ a aj na absolútne hodnoty (Tvrzení 6.3).",
                )) { "Výsledok nezodpovedá vzorcu §\\lcm(a,b)=|a|\\cdot|b|/\\gcd(a,b)§. Použi §\\lcm(a,b)=|a|\\cdot|b|/\\gcd(a,b)=$x\\cdot $y/$g§ (Tvrzení 6.3)." }
            }, aim = 2, hint = tx("Použi §\\gcd§ z prvej časti: §\\lcm(a,b)=|a|\\cdot|b|/\\gcd(a,b)§.")) },
        ),
        postup = TcReaPostup,
        solution = { _ ->
            val (table, bez, lcmAim) = TcReaPostup
            TcReaPostup.solve {
                if (a < 0 || b < 0 || swapped) table(tx("REA chce §a\\geq b>0§, preto počítam s §${maxOf(x, y)}§ a §${minOf(x, y)}§ (Lemma 6.1: §\\gcd(a,b)=\\gcd(|a|,|b|)=\\gcd(|b|,|a|)§). Koeficienty potom vrátim k pôvodným číslam."))
                table(tx("Každý nový riadok je predpredošlý mínus §q§-násobok predošlého, vo všetkých troch stĺpcoch; §q§ je celočíselný podiel."),
                    Formula(reaWalk(t, back = if (signed || swapped) "$g=${p(m)}\\cdot ${p(a)}+${p(n)}\\cdot ${p(b)}" else null), tx("§r_i=r_{i-2}-q_{i-1}r_{i-1}§, rovnako §\\alpha_i§ a §\\beta_i§.")))
                bez(tx("Posledný nenulový zvyšok je §\\gcd($a,$b)=$g§ a jeho riadok dáva Bézoutove koeficienty."), "$g=${p(t.alpha)}\\cdot ${t.a}+${p(t.beta)}\\cdot ${t.b}")
                if (signed || swapped) bez(tx("Späť k zadaniu, so znamienkami a poradím ako v ňom:"), "$g=${p(m)}\\cdot ${p(a)}+${p(n)}\\cdot ${p(b)}")
                lcmAim(tx("§\\lcm§ dostanem ako §|a|\\cdot|b|/\\gcd(a,b)§. Pri každom prvočísle je totiž súčet exponentov v §a§ a §b§ súčtom menšieho, ktorý má §\\gcd§, a väčšieho, ktorý má §\\lcm§ (Tvrzení 6.3)."), "\\lcm($a,$b)=\\frac{$x\\cdot $y}{$g}=$l")
            }
        },
        hint = tx("Tabuľka §r_i,\\alpha_i,\\beta_i,q_i§; prvé dva riadky §(a,1,0)§ a §(b,0,1)§. Výsledok je v poslednom riadku pred nulou."),
    )
}

/* ---- 2. Linear Diophantine equation (Cvičení 6.8, 6.29) -------------------------------------------------------- */

data class TcLdrTask(val a: Long, val b: Long, val c: Long)

fun tcLdrTask(r: Random): TcLdrTask {
    while (true) {
        val g = listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 9L).random(r)
        val a = g * r.rl(2, 30); val b = g * r.rl(2, 30)
        if (a == b || a > 150 || b > 150) continue
        val gg = gcd(a, b)
        val c = if (r.nextInt(5) == 0) {
            if (gg == 1L) continue
            gg * r.rl(2, 40) + r.rl(1, gg - 1)
        } else gg * r.rl(2, 60)
        return TcLdrTask(a, b, c)
    }
}

fun tcLdr(task: TcLdrTask): Exercise {
    val (a, b, c) = task
    val g = gcd(a, b)
    val s = ldr(a, b, c)
    val t = if (a >= b) rea(a, b) else rea(b, a)
    val (al, be) = if (a >= b) t.alpha to t.beta else t.beta to t.alpha
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("Má rovnica celočíselné riešenie?"), yesNo(s != null,
        "Áno: §\\gcd($a,$b)=$g§ delí §$c§ (Věta 6.7).",
        "Nie: §\\gcd($a,$b)=$g§ nedelí §$c§ (Věta 6.7).",
        "Ľavá strana je vždy násobok §$g§, ale §$c§ nie je. Preto riešenie neexistuje (Věta 6.7, smer §\\Rightarrow§).",
        "Riešenie existuje, lebo §\\gcd=$g§ delí §$c§. Nemusí ho deliť §a§ ani §b§, stačí §\\gcd§ (Věta 6.7)."),
        aim = 1, hint = if (s != null) tx("Najprv §\\gcd(a,b)§ cez REA. Riešenie existuje práve vtedy, keď §\\gcd§ delí pravú stranu §c§.") else null) }
    if (s != null) {
        val k = c / g
        parts += { _ -> Part(tx("Riešenie s najmenším §x\\geq 0§:"), nums(listOf("x", "y"), listOf(s.xMin, s.yMin), "§(x,y)=(${s.xMin},${s.yMin})§") { gv ->
            val v = gv[0] * a + gv[1] * b
            pickDiag(listOf(s.xMin, s.yMin), gv, listOf(
                (if (k != 1L) listOf(al, be) else null) to "To sú Bézoutove koeficienty pre §$g§, nie pre §$c§. Vynásob ich §c/\\gcd=$k§ (Důsledek 6.1).",
                listOf(be, al) to "Prehodil si koeficienty: prvý patrí k §a=$a§.",
            )) {
                when {
                    v == c && gv[0] >= 0 -> "Je to riešenie, len §x§ nie je najmenšie nezáporné. Odčítaj §k§-násobok §${s.sx}§ od §x§ (Věta 6.8)."
                    v == c -> "Je to riešenie, ale §x<0§. Pripočítaj násobok §${s.sx}§ k §x§ a uprav §y§ (Věta 6.8)."
                    v == -c -> "Znamienko: dosadením vyjde §$v=-c§. Skontroluj znamienka Bézoutových koeficientov."
                    else -> "Nie je to riešenie: §$a\\cdot ${p(gv[0])}+$b\\cdot ${p(gv[1])}=$v\\neq $c§."
                }
            } }, aim = 2, hint = tx("Bézoutove koeficienty vynásob číslom §c/\\gcd§. Potom §x§ zmenši zvyškom po delení §b/\\gcd§ a §y§ dopočítaj z rovnice.")) }
        parts += { _ -> Part(tx("Všetky riešenia sú §(x,y)+k\\cdot(s,-t)§, §k\\in\\mathbb{Z}§. Zadaj §s,t>0§:"), nums(listOf("s", "t"), listOf(b / g, a / g), "§s=\\frac{$b}{$g}=${b / g}§, §t=\\frac{$a}{$g}=${a / g}§") { gv ->
            pickDiag(listOf(b / g, a / g), gv, listOf(
                listOf(b, a) to "Nevydelil si §\\gcd=$g§. Bez krátenia vynecháš riešenia (Poznámka 6.5: §6x+4y=0§ má aj §(2,-3)§).",
                listOf(a / g, b / g) to "Prehodil si: pri §x§ je §b/\\gcd§, pri §y§ je §-a/\\gcd§ (Věta 6.8).",
                listOf(a, b) to "Prehodené a nekrátené. Pri §x§ je §b/\\gcd§, pri §y§ je §-a/\\gcd§ (Věta 6.8).",
            )) { "Krok musí riešiť §${a}x+${b}y=0§ a byť najmenší: §(b/\\gcd,-a/\\gcd)§ (Věta 6.8)." }
        }, aim = 3, hint = tx("Rozdiel dvoch riešení rieši §ax+by=0§. Najmenší taký krok dostaneš, keď §a§ aj §b§ vydelíš ich §\\gcd§.")) }
    }
    return Exercise(
        "dml-tc-ldr", "Diofantická rovnica", "Šablóna Cvičení 6.8; Věty 6.7 a 6.8, Důsledek 6.1",
        tx("Řešte diofantickou rovnici §$a\\cdot x+$b\\cdot y=$c§."),
        parts = parts,
        postup = TcLdrPostup,
        solution = { _ ->
            val (gcdAim, exists, one, all) = TcLdrPostup
            TcLdrPostup.solve {
                gcdAim(tx("REA na §${t.a}§ a §${t.b}§ dá §\\gcd§ aj jeho Bézoutove koeficienty. Každý zvyšok si totiž pamätám ako kombináciu oboch čísel (Poznámka 6.2)."), Formula(reaWalk(t)), disp("\\gcd($a,$b)=$g=${p(al)}\\cdot $a+${p(be)}\\cdot $b"))
                if (s == null) {
                    exists(tx("§$g§ nedelí §$c§, a ľavá strana je vždy násobok §$g§. Rovnica preto nemá celočíselné riešenie (Věta 6.7)."))
                    all(tx("Množina riešení je prázdna."))
                } else {
                    val k = c / g
                    exists(tx("§$g§ delí §$c§, preto riešenie existuje: Bézoutovu rovnosť pre §$g§ stačí vynásobiť číslom §$c/$g§ (Věta 6.7)."))
                    one(tx("Bézoutovu rovnosť vynásobím číslom §c/\\gcd=$k§, lebo tak sa ľavá strana §$g§ zmení na §$c§ (Důsledek 6.1)."), "$c=${p(al * k)}\\cdot $a+${p(be * k)}\\cdot $b")
                    all(tx("Všetky riešenia dostanem pripočítaním násobkov najmenšieho riešenia homogénnej rovnice, lebo rozdiel dvoch riešení rieši homogénnu rovnicu. Po vydelení §\\gcd§ sú jej koeficienty nesúdeliteľné, preto je každé jej riešenie násobkom toho najmenšieho (Věta 6.8)."), "(x,y)=(${s.x0},${s.y0})+k\\cdot(${s.sx},${s.sy}),\\quad k\\in\\mathbb{Z}")
                    all(tx("Najmenšie §x\\geq 0§ je zvyšok §${s.x0}§ po delení §${s.sx}§, a §y§ dopočítam z rovnice."), "x=${s.x0}\\bmod ${s.sx}=${s.xMin},\\quad y=\\frac{$c-$a\\cdot ${p(s.xMin)}}{$b}=${s.yMin}")
                }
            }
        },
        hint = tx("Najprv §\\gcd§ cez REA. Delí §c§? Potom Bézoutovu rovnosť vynásob §c/\\gcd§."),
    )
}

/* ---- 3. Multiplicative inverse; the smallest modulus (tests 2024 N, 2025 E, ukážka 2025) --------------------- */

data class TcInvTask(val a: Long, val m: Long, val bound: Boolean)

/** Numbers like 420 = 2^2·3·5·7 from the tests: many small primes, so the first moduli above them fail. */
private val SmallPrimeProducts = listOf<Long>(210, 330, 390, 420, 462, 510, 546, 570, 630, 660, 690, 714, 770, 780, 798, 840, 858, 870, 910, 924, 930, 966, 990)

fun tcInvTask(r: Random): TcInvTask {
    if (r.nextInt(2) == 0) return TcInvTask(SmallPrimeProducts.random(r), 0, true)
    while (true) {
        val m = r.rl(30, 400); val a = r.rl(2, m - 2)
        val coprime = gcd(a, m) == 1L
        if (!coprime && r.nextInt(3) != 0) continue
        if (coprime && rea(m, a).rows.size < 6) continue
        return TcInvTask(a, m, false)
    }
}

/** The smallest m >= a + 2 with gcd(a, m) = 1. */
fun tcSmallestModulus(a: Long): Long { var m = a + 2; while (gcd(a, m) != 1L) m++; return m }

private fun invDiag(a: Long, m: Long, inv: Long, t: Rea): (List<Long>) -> String = { gv ->
    val v = gv[0]
    when {
        v == t.beta && v != inv -> "Správne až na redukciu: §$v\\equiv $inv\\pmod{$m}§. Odpoveď má byť prvok §\\mathbb{Z}_{$m}§."
        v == md(t.alpha, m) && v != inv -> "To je koeficient pri §m=$m§, nie pri §a§. Inverzia je koeficient pri §a§."
        v == m - md(a, m) -> "To je opačný prvok §-a§ (§a+x\\equiv 0§), nie inverzia (§a\\cdot x\\equiv 1§)."
        v == md(-inv, m) -> "Opačné znamienko: §$v\\equiv -$inv§. Skontroluj znamienko koeficientu pri §a§."
        v !in 0 until m -> "Odpoveď má byť z §\\mathbb{Z}_{$m}=\\{0,\\dots,${m - 1}\\}§; tvoje §$v§ zredukuj a over."
        else -> "Skúška nesedí: §$a\\cdot $v=${a * v}\\equiv ${md(a * v, m)}\\pmod{$m}§, má vyjsť §1§."
    }
}

fun tcInv(task: TcInvTask): Exercise {
    val a = task.a
    if (!task.bound) {
        val m = task.m
        val inv = inverse(a, m)
        val g = gcd(a, m)
        val t = if (inv != null) rea(m, md(a, m)) else null
        val parts = ArrayList<(List<Any>) -> Part>()
        parts += { _ -> Part(tx("Existuje §$a^{-1}§ v §\\mathbb{Z}_{$m}§?"), yesNo(inv != null,
            "Áno: §\\gcd($a,$m)=1§ (Věta 6.12).", "Nie: §\\gcd($a,$m)=$g\\neq 1§ (Věta 6.12).",
            "Inverzia neexistuje, lebo §\\gcd($a,$m)=$g§: §$a\\cdot x§ je vždy násobok §$g§, nikdy §\\equiv 1§.",
            "Inverzia existuje: §\\gcd($a,$m)=1§. Netreba, aby §$m§ bolo prvočíslo (Věta 6.12)."),
            aim = 0, hint = if (inv != null) tx("Inverzia §a§ v §\\mathbb{Z}_m§ existuje práve vtedy, keď §\\gcd(a,m)=1§; prvočíselný modul netreba.") else null) }
        if (inv != null) parts += { _ -> Part(tx("§$a^{-1}§ v §\\mathbb{Z}_{$m}§:"), nums(listOf("a^{-1}"), listOf(inv), "§$a^{-1}=$inv§, lebo §$a\\cdot $inv=${a * inv}\\equiv 1\\pmod{$m}§", invDiag(a, m, inv, t!!)),
            aim = 1, hint = tx("REA na §m§ a §a§; inverzia je koeficient pri §a§, zredukovaný do §\\mathbb{Z}_m§.")) }
        return Exercise(
            "dml-tc-inv", "Inverzia v §\\mathbb{Z}_m§".let(::tx), "Šablóna Cvičení 6.15; Věta 6.12",
            tx("Určete multiplikativní inverzi §a^{-1}§ (pokud existuje) v §\\mathbb{Z}_{$m}§ pro §a=$a§."),
            parts = parts,
            postup = TcInvPostup,
            solution = { _ ->
                val (exists, reaAim, result) = TcInvPostup
                TcInvPostup.solve {
                    if (inv == null) {
                        exists(tx("§\\gcd($a,$m)=$g\\neq 1§, preto inverzia neexistuje: z §$a x\\equiv 1\\pmod{$m}§ by §$g§ delilo §$a x§ aj §$a x-1§, teda aj §1§ (Věta 6.12)."))
                        result(tx("Prvok §$a§ v §\\mathbb{Z}_{$m}§ inverziu nemá."))
                    } else {
                        exists(tx("§\\gcd($a,$m)=1§, preto inverzia existuje: z Bézoutovej rovnosti §1=\\alpha\\cdot $a+\\beta\\cdot $m§ je §\\alpha\\cdot $a\\equiv 1\\pmod{$m}§ (Věta 6.12)."))
                        reaAim(tx("Hľadám §x§ so §${Terms.times(a, "x")}\\equiv 1\\pmod{$m}§, teda §${Terms.times(a, "x")}+${Terms.times(m, "y")}=1§ (dôkaz Věty 6.12). REA na §$m§ a §$a§:"),
                            Formula(reaWalk(t!!)))
                        result(tx("Koeficient pri §$a§ je inverzia; vezmem ho modulo §$m§ a overím súčinom."), "$a^{-1}=${t.beta}\\bmod $m=$inv,\\qquad $a\\cdot $inv=${a * inv}\\equiv 1\\pmod{$m}")
                    }
                }
            },
            hint = tx("REA na §m§ a §a§; inverzia je koeficient pri §a§, zredukovaný do §\\mathbb{Z}_m§."),
        )
    }
    val lo = a + 2
    val m = tcSmallestModulus(a)
    val inv = inverse(a, m)!!
    val t = rea(m, a)
    val primes = factor(a).map { it.first }
    val primesTex = primes.joinToString(",\\ ")
    return Exercise(
        "dml-tc-inv-min", "Najmenší modul s inverziou", "Šablóna testov 6. 12. 2024 (variant N) a 4. 12. 2025 (variant E), príklad 3",
        tx("Co musí celé číslo §m\\geq $lo§ splňovat, aby číslo §a=$a§ mělo v §\\mathbb{Z}_m§ multiplikativní inverzi? Nalezněte nejmenší modul §m\\geq $lo§, který toto splňuje, a v něm §$a^{-1}§ vypočítejte. Své kroky poctivě zdůvodňujte."),
        parts = listOf(
            { _ -> Part(tx("Podmienka na §m§:"), Answer.Choice(listOf(
                tx("§\\gcd($a,m)=1§, teda §m§ nie je deliteľné žiadnym z §$primesTex§"), tx("§m§ musí byť prvočíslo"), tx("stačí §m>$a§")), 0,
                tx("Inverzia existuje práve pri §\\gcd(a,m)=1§ (Věta 6.12). §$a=${tcFactorTex(a)}§."),
                wrong = mapOf(
                    1 to tx("Prvočíslo stačí, ale netreba ho. Podmienka je §\\gcd($a,m)=1§; vyhovie aj zložené §m§ (Věta 6.12)."),
                    2 to tx("Veľkosť nestačí: §m=${a + 2}§ je väčšie, ale §\\gcd($a,${a + 2})=${gcd(a, a + 2)}§. Treba §\\gcd(a,m)=1§."),
                )), aim = 0, hint = tx("Inverzia znamená §a\\cdot x\\equiv 1\\pmod{m}§, teda Bézoutovu rovnosť §ax+my=1§. Kedy taká rovnosť existuje?")) },
            { _ -> Part(tx("Najmenšie vyhovujúce §m\\geq $lo§:"), nums(listOf("m"), listOf(m), "§m=$m§") { gv ->
                val v = gv[0]
                when {
                    v < lo -> "Zadanie chce §m\\geq $lo§."
                    gcd(a, v) > 1 -> "§\\gcd($a,$v)=${gcd(a, v)}§, inverzia tam neexistuje. Over deliteľnosť každým z §$primesTex§."
                    v > m -> "Vyhovuje, ale menšie existuje: §m=$m§. Prejdi čísla od §$lo§ jedno po druhom."
                    else -> "Pred týmto číslom je menšie vyhovujúce §m§, alebo toto nevyhovuje. Hľadaj od §$lo§ prvé číslo nedeliteľné žiadnym z §$primesTex§."
                }
            }, aim = 1, hint = tx("Rozlož §a§ na prvočísla a skúšaj §m§ od §a+2§ nahor; vyraď každé, ktoré niektoré z nich delí.")) },
            { _ -> Part(tx("§$a^{-1}§ v §\\mathbb{Z}_{$m}§:"), nums(listOf("a^{-1}"), listOf(inv), "§$a^{-1}=$inv§", invDiag(a, m, inv, t)),
                aim = 2, hint = tx("REA na §m§ a §a§; inverzia je koeficient pri §a§, zredukovaný do §\\mathbb{Z}_m§. Skúška: §a\\cdot a^{-1}§ dá zvyšok §1§.")) },
        ),
        postup = TcInvMinPostup,
        solution = { _ ->
            val (whenAim, least, reaAim, check) = TcInvMinPostup
            TcInvMinPostup.solve {
                whenAim(tx("Inverzia existuje práve vtedy, keď §\\gcd($a,m)=1§, lebo vtedy ju dá Bézoutova rovnosť a inak spoločný deliteľ nedelí §1§ (Věta 6.12). §$a=${tcFactorTex(a)}§, teda §m§ nesmie byť deliteľné §$primesTex§."))
                least(tx("Skúšam moduly od najmenšieho a vyradím tie, ktoré majú spoločné prvočíslo s §$a§."),
                    (lo until m).map { k -> disp("$k=${tcFactorTex(k)}\\ \\Rightarrow\\ ${factor(k).first { a % it.first == 0L }.first}\\mid $k", null) } + disp("$m=${tcFactorTex(m)}\\ \\Rightarrow\\ m=$m", "Prvý modul nesúdeliteľný s §$a§."))
                reaAim(tx("REA na §$m§ a §$a§; koeficient pri §$a§ je inverzia."), Formula(reaWalk(t)), disp("1=${p(t.alpha)}\\cdot $m+${p(t.beta)}\\cdot $a\\ \\Rightarrow\\ $a^{-1}\\equiv ${t.beta}\\equiv $inv\\pmod{$m}"))
                check(tx("Súčin §$a\\cdot $inv§ musí dať zvyšok §1§ po delení §$m§."), "$a\\cdot $inv=${a * inv}=${(a * inv) / m}\\cdot $m+1")
            }
        },
        hint = tx("§a^{-1}§ existuje práve pri §\\gcd(a,m)=1§. Rozlož §a§ na prvočísla a skúšaj §m§ od §a+2§."),
    )
}

/** The small power as one step under [aim]: its sentence and the chain. */
private fun SolutionScope.smallPower(aim: Aim, b: Long, e: Long, m: Long) {
    if (e == 0L) { aim("Exponent je §0§, mocnina je §1§."); return }
    if (e == 1L) { aim("Exponent je §1§, zvyšok je samotný základ §${md(b, m)}§."); return }
    aim("Malú mocninu dopočítam postupne: násobím základom a hneď redukujem, lebo kongruencia sa pri násobení zachová (Tvrzení 6.6).", chainFormula(b, e, m))
}

/* ---- 4. A large power modulo m (Cvičení 6.22, Příklady 6.11, 6.14; Spěvák, 8. prednáška) ---------------------- */

data class TcPowTask(val base: Long, val n: Long, val m: Long)

/**
 * The base is coprime to m and the exponent leaves at most [TcSmallPower] after Euler, as on the tests (K 2025: 7^5 and
 * 2^7). A base sharing a factor with m needs the binary method (3^644 mod 645), which Spěvák left for his 9. prednáška.
 */
fun tcPowTask(r: Random): TcPowTask {
    while (true) {
        val m = r.rl(9, 99)
        val b0 = r.rl(2, m - 1)
        val ph = phi(m)
        if (gcd(b0, m) != 1L || ph < 4) continue
        val base = if (r.nextBoolean()) b0 + m * r.rl(1, 30) else b0
        val e = r.rl(1, minOf(TcSmallPower.toLong(), ph - 1))
        val n = ph * r.rl(maxOf(1L, 100L / ph), 2000L / ph) + e
        return TcPowTask(base, n, m)
    }
}

fun tcPow(task: TcPowTask): Exercise {
    val (b, n, m) = task
    require(gcd(b, m) == 1L) { "tcPow: the base must be coprime to the modulus" }
    val res = powMod(b, n, m)
    val ph = phi(m)
    val e = n % ph
    val prime = isPrime(m)
    val bb = md(b, m)
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("§\\varphi($m)§:"), nums(listOf("\\varphi"), listOf(ph), "§${tcPhiTex(m)}§") { gv ->
        pickDiag(listOf(ph), gv, listOf(
            listOf(m - 1) to "§$m§ nie je prvočíslo, §\\varphi($m)\\neq ${m - 1}§. Rozlož §$m=${tcFactorTex(m)}§ (Věta 6.16).",
            listOf(m) to "§\\varphi(m)§ počíta len čísla nesúdeliteľné s §m§, nie všetky (Definice 6.10).",
        )) { "§\\varphi($m)=$ph§ podľa rozkladu §$m=${tcFactorTex(m)}§. §\\varphi§ je multiplikatívna pre nesúdeliteľné činitele (Tvrzení 6.9, 6.10)." }
    }, aim = 1, hint = tx("Rozlož §m§ na prvočísla; §\\varphi§ je multiplikatívna pre nesúdeliteľné činitele a §\\varphi(q^k)=q^k-q^{k-1}§.")) }
    parts += { _ -> Part(tx("Exponent po redukcii, §$n$MOD\\varphi($m)§:"), nums(listOf("e"), listOf(e), "§$n=${n / ph}\\cdot $ph+$e§") { gv ->
        pickDiag(listOf(e), gv, listOf(
            listOf(n % m) to "Exponent sa redukuje modulo §\\varphi(m)=$ph§, nie modulo §m§. Modulo §m§ sa redukuje len základ.",
            (if (!prime) listOf(n % (m - 1)) else null) to "Redukoval si modulo §m-1§, to platí len pre prvočíslo (MFV). Tu treba §\\varphi($m)=$ph§.",
        )) { "Zvyšok exponentu po delení §$ph§ je iný: §$n=${n / ph}\\cdot $ph+$e§." }
    }, aim = 1, hint = tx("§\\gcd(a,m)=1§, preto podľa Eulerovej vety stačí exponent modulo §\\varphi(m)§, nie modulo §m§.")) }
    parts += { _ -> Part(tx("§$b^{$n}$MOD$m§:"), nums(listOf("x"), listOf(res), "§$b^{$n}\\equiv $res\\pmod{$m}§") { gv ->
        pickDiag(listOf(res), gv, listOf(
            listOf(powMod(b, n % m, m)) to "Exponent si redukoval modulo §m§. Správne je modulo §\\varphi($m)=$ph§ (Věta 6.15).",
            (if (!prime) listOf(powMod(b, n % (m - 1), m)) else null) to "Exponent si redukoval modulo §m-1§; to je MFV a §$m§ nie je prvočíslo. Treba §\\varphi($m)=$ph§.",
            listOf(bb) to "To je len redukovaný základ. Ešte ho umocni na §$e§.",
        )) { "§${Terms.pow(bb, e.toInt())}\\bmod $m=$res§, tvoje číslo sa líši. Over medzivýsledky: §${stepChain(b, e, m)}§." }
    }, aim = 2, hint = tx("Základ zredukuj modulo §m§ a umocni ho na exponent z predošlej časti: násob základom a po každom kroku redukuj.")) }
    return Exercise(
        "dml-tc-pow", "Veľká mocnina modulo §m§".let(::tx), "Šablóna Cvičení 6.22; Věty 6.14, 6.15, Příklad 6.14",
        tx("Vypočtěte §$b^{$n}$MOD$m§."),
        parts = parts,
        postup = TcPowPostup,
        solution = { _ ->
            val (base, smaller, small) = TcPowPostup
            TcPowPostup.solve {
                if (b >= m) base(tx("Základ nahradím jeho zvyškom, lebo kongruencia sa pri násobení zachová (Tvrzení 6.6)."), "$b\\equiv $bb\\pmod{$m}")
                else base(tx("Základ §$b§ je už menší než modul §$m§, nič nemením."))
                smaller(tx("§\\gcd($bb,$m)=1§, " + (if (prime) "a §$m§ je prvočíslo: podľa MFV je §${Terms.pow(bb, (m - 1).toInt())}\\equiv 1§." else "preto platí Eulerova veta: §${Terms.pow(bb, ph.toInt())}\\equiv 1\\pmod{$m}§, kde §\\varphi($m)=$ph§.") + " Exponent stačí brať modulo §$ph§."),
                    *(if (prime) emptyArray<String>() else arrayOf(tcPhiDisp(m))),
                    "\\begin{aligned}${Terms.pow(bb, n.toInt())}&=${Terms.pow("\\left(${Terms.pow(bb, ph.toInt())}\\right)", (n / ph).toInt())}" + (if (e == 0L) "" else "\\cdot ${Terms.pow(bb, e.toInt())}") + "\\\\&\\equiv ${Terms.pow(bb, e.toInt())}\\pmod{$m}\\end{aligned}")
                smallPower(small, b, e, m)
            }
        },
        hint = tx("Najprv základ modulo §m§. Pri §\\gcd(a,m)=1§ redukuj exponent modulo §\\varphi(m)§."),
    )
}

/* ---- 5. Two remainders, the second reuses the first (tests 2023 D, 2024 R, 2025 K) ---------------------------- */

data class TcTowerTask(val a: Long, val n: Long, val c: Long, val p: Long)

/** Both small powers need at most [TcSmallPower] factors, as on the tests (K 2025: 7^5 mod 60, then 2^7 mod 61). */
fun tcTowerTask(r: Random): TcTowerTask {
    val primes = (37L..103L).filter(::isPrime)
    while (true) {
        val p = primes.random(r); val mm = p - 1
        val a0 = r.rl(2, mm - 1)
        if (gcd(a0, mm) != 1L) continue
        val a = a0 + mm * r.rl(2, 900 / mm)
        val ph = phi(mm)
        val e = r.rl(1, minOf(TcSmallPower.toLong(), ph - 1))
        val n = ph * r.rl(maxOf(1L, 100L / ph), 400L / ph) + e
        val c0 = r.rl(2, p - 1)
        val c = c0 + p * r.rl(2, 900 / p)
        if (a >= 1000 || c >= 1000 || a < 100 || c < 100) continue
        val r1 = powMod(a, n, mm)
        if (r1 <= 1L || r1 > TcSmallPower) continue
        return TcTowerTask(a, n, c, p)
    }
}

fun tcTower(task: TcTowerTask): Exercise {
    val (a, n, c, p) = task
    val mm = p - 1
    val ph = phi(mm)
    val e = n % ph
    val r1 = powMod(a, n, mm)
    val r2 = powMod(c, r1, p)
    return Exercise(
        "dml-tc-tower", "Dva zvyšky, druhý z prvého", "Šablóna testov 8. 12. 2023 (variant D), 6. 12. 2024 (variant R), 5. 12. 2025 (variant K)",
        tx("Určete hodnoty následujících dvou zbytků. Tip: při výpočtu toho druhého využijte výsledek a). a) §$a^{$n}$MOD$mm§ b) §$c^{($a^{$n})}$MOD$p§."),
        parts = listOf(
            { _ -> Part(tx("a) Najprv §\\varphi($mm)§:"), nums(listOf("\\varphi"), listOf(ph), "§${tcPhiTex(mm)}§") { gv ->
                pickDiag(listOf(ph), gv, listOf(listOf(mm - 1) to "§$mm§ nie je prvočíslo, §\\varphi($mm)\\neq ${mm - 1}§. Rozlož §$mm=${tcFactorTex(mm)}§ (Věta 6.16).")) { "§\\varphi($mm)=$ph§, lebo §$mm=${tcFactorTex(mm)}§. Tvoje číslo sa líši; §\\varphi§ počítaj po prvočíslach (Věta 6.16)." }
            }, aim = 0, hint = tx("Rozlož modul z časti a) na prvočísla; §\\varphi§ je multiplikatívna a §\\varphi(q^k)=q^k-q^{k-1}§.")) },
            { _ -> Part(tx("a) §$a^{$n}$MOD$mm§:"), nums(listOf("a"), listOf(r1), "§$a^{$n}\\equiv ${Terms.pow(md(a, mm), e.toInt())}\\equiv $r1\\pmod{$mm}§") { gv ->
                pickDiag(listOf(r1), gv, listOf(
                    listOf(powMod(a, n % mm, mm)) to "Exponent si redukoval modulo §$mm§. Správne modulo §\\varphi($mm)=$ph§ (Věta 6.15).",
                    listOf(powMod(a, n % (mm - 1), mm)) to "Exponent si redukoval modulo §${mm - 1}§ ako pri prvočísle. §$mm§ nie je prvočíslo: modulo §\\varphi($mm)=$ph§.",
                    listOf(md(a, mm)) to "To je len základ modulo §$mm§. Ešte ho umocni na §$e§.",
                )) { "§${Terms.pow(md(a, mm), e.toInt())}\\bmod $mm=$r1§, lebo §$n\\equiv $e\\pmod{$ph}§. Exponent smieš zmenšiť, lebo §\\gcd(${md(a, mm)},$mm)=1§ a podľa Eulerovej vety §${Terms.pow(md(a, mm), ph.toInt())}\\equiv 1\\pmod{$mm}§. Malú mocninu dopočítaj násobením a redukciou: §${stepChain(a, e, mm)}§." }
            }, aim = 1, hint = tx("Základ nahraď jeho zvyškom, exponent ber podľa Eulerovej vety modulo §\\varphi§ z predošlej časti. Malú mocninu potom dopočítaj: násob základom a redukuj.")) },
            { _ -> Part(tx("b) §$c^{($a^{$n})}$MOD$p§:"), nums(listOf("b"), listOf(r2), "§$c^{($a^{$n})}\\equiv ${Terms.pow(md(c, p), r1.toInt())}\\equiv $r2\\pmod{$p}§") { gv ->
                pickDiag(listOf(r2), gv, listOf(
                    listOf(powMod(c, n, p)) to "Exponent nie je §$n§, ale §$a^{$n}§. Jeho zvyšok modulo §$mm§ je výsledok a).",
                    listOf(powMod(c, powMod(a, n, p), p)) to "Exponent si redukoval modulo §$p§. MFV dáva §c^{${p - 1}}\\equiv 1§, teda exponent modulo §p-1=$mm§.",
                    listOf(powMod(c, r1, mm)) to "Počítal si modulo §$mm§. Časť b) je modulo §$p§.",
                    listOf(powMod(c, md(a, mm), p)) to "Za exponent si dal len §$a$MOD$mm§. Treba celé §$a^{$n}$MOD$mm=$r1§ z časti a).",
                )) { "Výsledok b) nesedí: exponent ber z časti a). §$p§ je prvočíslo, MFV: exponent stačí modulo §$mm§, teda §${md(c, p)}^{$r1}$MOD$p§." }
            }, aim = 2, hint = tx("Modul v b) je prvočíslo §p§ a modul v a) je §p-1§: podľa MFV stačí exponent modulo §p-1§, a to je výsledok a). Potom dopočítaj malú mocninu.")) },
        ),
        postup = TcTowerPostup,
        solution = { _ ->
            val (euler, smallA, fermat, smallB) = TcTowerPostup
            val am = md(a, mm); val cp = md(c, p)
            TcTowerPostup.solve {
                euler(tx("§$a\\equiv $am\\pmod{$mm}§ a §\\gcd($am,$mm)=1§, preto platí Eulerova veta. Exponent stačí brať modulo §\\varphi($mm)=$ph§."),
                    tcPhiDisp(mm),
                    "\\begin{aligned}${Terms.pow(a, n.toInt())}&\\equiv ${Terms.pow(am, n.toInt())}\\\\&=${Terms.pow("\\left(${Terms.pow(am, ph.toInt())}\\right)", (n / ph).toInt())}" + (if (e == 0L) "" else "\\cdot ${Terms.pow(am, e.toInt())}") + "\\\\&\\equiv ${Terms.pow(am, e.toInt())}\\pmod{$mm}\\end{aligned}")
                smallPower(smallA, a, e, mm)
                fermat(tx("§$p§ je prvočíslo a §$c\\equiv $cp\\pmod{$p}§ nie je jeho násobok, podľa MFV §${Terms.pow(cp, mm.toInt())}\\equiv 1\\pmod{$p}§. Exponent stačí poznať modulo §$mm§, a ten je z a): §${Terms.pow(a, n.toInt())}=$mm\\cdot t+$r1§."),
                    "\\begin{aligned}$c^{(${Terms.pow(a, n.toInt())})}&\\equiv $cp^{$mm t+$r1}\\\\&=\\left($cp^{$mm}\\right)^{t}\\cdot ${Terms.pow(cp, r1.toInt())}\\\\&\\equiv ${Terms.pow(cp, r1.toInt())}\\pmod{$p}\\end{aligned}")
                smallPower(smallB, c, r1, p)
            }
        },
        hint = tx("Modul v b) je prvočíslo §p§ a modul v a) je §p-1§: výsledok a) je exponent pre b)."),
    )
}

/* ---- 6. Linear congruence (Cvičení 6.23, Příklady 6.17, 6.18) ------------------------------------------------- */

data class TcLkonTask(val a: Long, val b: Long, val m: Long)

fun tcLkonTask(r: Random): TcLkonTask {
    while (true) {
        val g = listOf(1L, 2L, 3L, 3L, 4L, 5L, 6L, 7L).random(r)
        val n = r.rl(5, 60); val m = g * n
        if (m !in 12..250) continue
        val a1 = r.rl(2, n + 20)
        if (gcd(a1, n) != 1L || gcd(g * a1, m) != g) continue
        val a = g * a1
        val b = if (g > 1 && r.nextInt(5) == 0) g * r.rl(1, n - 1) + r.rl(1, g - 1) else g * r.rl(1, n - 1)
        return TcLkonTask(a, b, m)
    }
}

fun tcLkon(task: TcLkonTask): Exercise {
    val (a, b, m) = task
    val g = gcd(a, m)
    val s = lkon(a, b, m)
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("Má kongruencia riešenie?"), yesNo(s != null,
        "Áno: §\\gcd($a,$m)=$g§ delí §$b§ (Věta 6.17).", "Nie: §\\gcd($a,$m)=$g§ nedelí §$b§ (Věta 6.17).",
        "§${a}x-$b§ by musel byť násobok §$m§, teda aj §$g§; §${a}x§ je násobok §$g§, §$b§ nie.",
        "Riešenie existuje: §\\gcd($a,$m)=$g§ delí §$b§. Nemusí platiť §\\gcd=1§ (Věta 6.17)."),
        aim = 0, hint = if (s != null) tx("Riešenie existuje práve vtedy, keď §\\gcd(a,m)§ delí §b§.") else null) }
    if (s != null) {
        val n = s.n
        parts += { _ -> Part(tx("Všetky riešenia §x\\equiv x_0\\pmod{n}§, kde §0\\leq x_0<n§:"), nums(listOf("x_0", "n"), listOf(s.x0, n), "§x\\equiv ${s.x0}\\pmod{$n}§") { gv ->
            val (x0, nn) = gv
            when {
                nn == m && n != m && md(a * x0 - b, m) == 0L -> "Modul si nezmenšil. Pri krátení §$g§ sa modul delí §\\gcd(m,c)§ (Věta 6.13): §$m/$g=$n§."
                nn == n && md(x0, n) == s.x0 -> "Správne až na redukciu: §x_0§ má byť z §\\{0,\\dots,${n - 1}\\}§, teda §${s.x0}§."
                nn == m && n != m -> "Modul si nezmenšil: po krátení §$g§ je §n=$m/$g=$n§ (Věta 6.13)."
                nn != n -> "Modul nesedí: riešenia sa opakujú s krokom §m/\\gcd(a,m)=$n§ (Věta 6.17)."
                else -> "Skúška nesedí: §$a\\cdot $x0=${a * x0}\\equiv ${md(a * x0, m)}\\pmod{$m}§, má byť §${md(b, m)}§."
            }
        }, aim = if (g > 1) 1 else 2, hint = tx("Vydeľ §a§, §b§ aj §m§ číslom §\\gcd(a,m)§ (Věta 6.13). Potom obe strany vynásob inverziou nového koeficientu.")) }
        parts += { _ -> Part(tx("Koľko riešení má v §\\mathbb{Z}_{$m}§?"), nums(listOf("\\#"), listOf(g), "§\\gcd($a,$m)=$g§: §" + s.inZm.joinToString(",\\ ") + "§") { gv ->
            pickDiag(listOf(g), gv, listOf(
                listOf(1L) to "V §\\mathbb{Z}_{$m}§ ich je §\\gcd(a,m)=$g§, nie jedno (Věta 6.18): §${s.x0},\\ ${s.x0}+$n,\\dots§",
                listOf(n) to "§$n§ je krok medzi riešeniami. Počet v §\\mathbb{Z}_{$m}§ je §m/n=\\gcd(a,m)=$g§ (Věta 6.18).",
                listOf(m) to "Nie každé §x§ je riešenie. V §\\mathbb{Z}_{$m}§ ich je §\\gcd(a,m)=$g§ (Věta 6.18).",
            )) { "Počet riešení v §\\mathbb{Z}_m§ je §\\gcd(a,m)§ (Věta 6.18)." }
        }, aim = 3, hint = tx("Riešenia sa opakujú s krokom nového modulu §n§. Spočítaj, koľko ich padne do §\\{0,\\dots,m-1\\}§.")) }
    }
    return Exercise(
        "dml-tc-lkon", "Lineárna kongruencia", "Šablóna Cvičení 6.23; Věty 6.13, 6.17, 6.18",
        tx("Řešte lineární kongruenci §$a\\cdot x\\equiv $b\\pmod{$m}§."),
        parts = parts,
        postup = TcLkonPostup,
        solution = { _ ->
            val (exists, cut, invAim, all) = TcLkonPostup
            TcLkonPostup.solve {
                if (s == null) {
                    exists(tx("Kongruencia nemá riešenie, lebo §$g=\\gcd($a,$m)§ delí §$m§ aj §$a x§, a teda by muselo deliť aj §$b§. Lenže §$g§ nedelí §$b§, preto riešenie nie je (Věta 6.17)."))
                    all(tx("V §\\mathbb{Z}_{$m}§ nie je žiadne riešenie."))
                } else {
                    val n = s.n
                    val a1 = md(a / g, n); val b1 = md(b / g, n)
                    exists(tx("§\\gcd($a,$m)=$g§ delí §$b§, preto riešenie existuje: po krátení §$g§ je koeficient nesúdeliteľný s modulom a má inverziu (Věta 6.17)."))
                    if (g > 1) cut(tx("Vydelím koeficient, pravú stranu aj modul číslom §$g§, lebo §$m\\mid ${Terms.sum(Terms.times(a, "x"), "${-b}")}§ práve vtedy, keď §$n\\mid ${Terms.sum(Terms.times(a / g, "x"), "${-b / g}")}§ (Věta 6.13)."), "\\begin{aligned}&${Terms.times(a, "x")}\\equiv $b\\pmod{$m}\\\\\\Leftrightarrow\\ &${Terms.times(a / g, "x")}\\equiv ${b / g}\\pmod{$n}\\end{aligned}")
                    else cut(tx("§\\gcd§ je §1§, krátiť netreba."))
                    if (n > 1) {
                        val inv = inverse(a1, n)!!
                        invAim(tx("Koeficient §$a1§ je invertovateľný modulo §$n§: §$a1^{-1}\\equiv $inv§ (REA alebo skúšaním pri malom §n§). Obe strany ním vynásobím; úprava je ekvivalentná, lebo násobením §$a1§ sa vrátim späť (Poznámka 6.9)."),
                            (if (a / g != a1 || b / g != b1) listOf(disp("${Terms.times(a1, "x")}\\equiv $b1\\pmod{$n}", "Koeficienty redukujem modulo §$n§.")) else emptyList()) +
                                disp("$a1\\cdot $inv=${a1 * inv}\\equiv 1\\pmod{$n}", "skúška inverzie") + disp("x\\equiv $inv\\cdot $b1=${inv * b1}\\equiv ${s.x0}\\pmod{$n}"))
                    } else invAim(tx("Po krátení je modul §1§ a vyhovuje každé §x§."))
                    all(tx("Riešenia sa v §\\mathbb{Z}_{$m}§ líšia o §$n§, preto ich je §$m/$n=$g§ (Věta 6.18)."), s.inZm.joinToString(",\\ "))
                }
            }
        },
        hint = tx("§\\gcd(a,m)§ musí deliť §b§. Krátiš ním §a§, §b§ aj §m§ (Věta 6.13), potom inverzia."),
    )
}

/* ---- 7. A system of congruences: ČVOZ / ZČVOZ (Cvičení 6.24, Příklady 6.19, 6.20) --------------------------- */

data class TcCrtTask(val parts: List<Pair<Long, Long>>)

fun tcCrtTask(r: Random): TcCrtTask {
    val coprime = listOf(listOf(5L, 3L), listOf(5L, 7L, 11L), listOf(7L, 9L), listOf(4L, 9L, 5L), listOf(8L, 15L), listOf(11L, 13L), listOf(3L, 7L, 10L), listOf(9L, 16L), listOf(12L, 25L))
    val shared = listOf(listOf(8L, 12L, 15L), listOf(6L, 8L, 20L), listOf(6L, 10L, 15L), listOf(12L, 18L), listOf(10L, 14L), listOf(9L, 15L, 21L), listOf(20L, 30L), listOf(14L, 21L, 6L))
    val kind = r.nextInt(5)
    val mods = (if (kind <= 1) coprime else shared).random(r).shuffled(r)
    val l = mods.fold(1L) { acc, x -> lcm(acc, x) }
    val x = r.rl(0, l - 1)
    val rs = mods.map { x % it }.toMutableList()
    if (kind == 4) {
        // break one congruence so that some gcd(m_i, m_j) no longer divides r_i - r_j
        val i = r.nextInt(mods.size)
        val j = (mods.indices - i).first { gcd(mods[it], mods[i]) > 1 }
        rs[i] = (rs[i] + 1 + r.rl(0, gcd(mods[i], mods[j]) - 2).coerceAtLeast(0)) % mods[i]
    }
    return TcCrtTask(rs.zip(mods))
}

fun tcSystemTex(ps: List<Pair<Long, Long>>) = "\\begin{matrix} " + ps.joinToString(" \\\\ ") { (r, m) -> "x\\equiv $r\\pmod{$m}" } + " \\end{matrix}"

fun tcCrt(task: TcCrtTask): Exercise {
    val ps = task.parts
    val sol = crt(ps)
    val mods = ps.map { it.second }
    val coprime = mods.indices.all { i -> mods.indices.all { j -> i == j || gcd(mods[i], mods[j]) == 1L } }
    val prod = mods.fold(1L) { a, b -> a * b }
    val parts = ArrayList<(List<Any>) -> Part>()
    val bad = mods.indices.flatMap { i -> mods.indices.filter { j -> j > i } .map { j -> i to j } }.firstOrNull { (i, j) -> md(ps[i].first - ps[j].first, gcd(mods[i], mods[j])) != 0L }
    parts += { _ -> Part(tx("Má sústava riešenie?"), yesNo(sol != null,
        if (coprime) "Áno: moduly sú navzájom nesúdeliteľné (Věta 6.19)." else "Áno: §\\gcd(m_i,m_j)§ delí §a_i-a_j§ pre každú dvojicu (Věta 6.20).",
        bad?.let { (i, j) -> "Nie: §\\gcd(${mods[i]},${mods[j]})=${gcd(mods[i], mods[j])}§ nedelí §${ps[i].first}-${ps[j].first}§ (Věta 6.20)." } ?: "",
        bad?.let { (i, j) -> "Dve kongruencie si odporujú modulo §${gcd(mods[i], mods[j])}§. Moduly sú súdeliteľné, treba overiť podmienku Věty 6.20." } ?: "",
        if (coprime) "Moduly sú navzájom nesúdeliteľné, ČVOZ zaručí riešenie (Věta 6.19)." else "Moduly sú súdeliteľné, ale podmienka Věty 6.20 platí: riešenie existuje."),
        aim = 0, hint = if (sol != null) tx("Pri navzájom nesúdeliteľných moduloch riešenie existuje vždy (ČVOZ). Inak over, či §\\gcd(m_i,m_j)§ delí §a_i-a_j§ pre každú dvojicu.") else null) }
    if (sol != null) {
        parts += { _ -> Part(tx("Všetky riešenia §x\\equiv x_0\\pmod{M}§, kde §0\\leq x_0<M§:"), nums(listOf("x_0", "M"), listOf(sol.x, sol.mod), "§x\\equiv ${sol.x}\\pmod{${sol.mod}}§") { gv ->
            val (x0, mm) = gv
            val ok = ps.all { (r, m) -> md(x0 - r, m) == 0L }
            when {
                mm == prod && !coprime && ok -> "Modul je §\\lcm=${sol.mod}§, nie súčin: moduly sú súdeliteľné (Věta 6.20)."
                ok && mm == sol.mod -> "Správne až na redukciu: §x_0=${md(x0, sol.mod)}§."
                ok -> "§x_0§ sedí, modul nie: riešenie je jednoznačné modulo §${if (coprime) "M=" else "\\lcm="}${sol.mod}§."
                else -> ps.first { (r, m) -> md(x0 - r, m) != 0L }.let { (r, m) -> "Skúška nesedí: §$x0\\equiv ${md(x0, m)}\\pmod{$m}§, má byť §$r§." }
            }
        }, aim = 1, hint = tx("Z prvej kongruencie §x=a_1+m_1t§, dosaď do ďalšej a vyrieš pre §t§. Riešenie je jediné modulo §\\lcm§ modulov.")) }
        parts += { _ -> Part(tx("Najväčšie záporné riešenie:"), nums(listOf("x"), listOf(sol.x - sol.mod), "§${sol.x}-${sol.mod}=${sol.x - sol.mod}§") { gv ->
            val v = gv[0]
            when {
                v == 0L && sol.x == 0L -> "Nula je riešenie, ale nie záporné. Odčítaj §M§: §${-sol.mod}§."
                v == -sol.x -> "Opačné číslo riešením nie je. Od §x_0§ odčítaj §M=${sol.mod}§."
                v < 0 && md(v - sol.x, sol.mod) == 0L -> "Je to riešenie, ale nie najväčšie záporné. Stačí raz odčítať §M§: §${sol.x - sol.mod}§."
                v == sol.x - prod && prod != sol.mod -> "Odčítal si súčin modulov. Krok riešení je §\\lcm=${sol.mod}§."
                else -> "Riešenia sú §${sol.x}+${sol.mod}k§; najväčšie záporné je pre §k=-1§."
            }
        }, aim = 2, hint = tx("Všetky riešenia sú §x_0+kM§, §k\\in\\mathbb{Z}§; vyber z nich najväčšie záporné.")) }
    }
    return Exercise(
        "dml-tc-crt", "Sústava kongruencií", "Šablóna Cvičení 6.24; Věty 6.19 a 6.20, Příklad 6.20",
        tx("Řešte soustavu kongruencí §" + tcSystemTex(ps) + "§."),
        parts = parts,
        postup = TcCrtPostup,
        solution = { _ ->
            val (exists, subst, result) = TcCrtPostup
            TcCrtPostup.solve {
                if (coprime) exists(tx("Moduly sú navzájom nesúdeliteľné, preto je riešenie jediné v §\\mathbb{Z}_{$prod}§. Dve riešenia sa totiž líšia o násobok všetkých modulov, teda ich súčinu, a jedno nájdem postupným dosadzovaním, lebo moduly sú navzájom invertovateľné (Věta 6.19, ČVOZ)."))
                else if (sol != null) exists(tx("Moduly sú súdeliteľné, ale §\\gcd(m_i,m_j)§ delí §a_i-a_j§ pre všetky dvojice: " + mods.indices.flatMap { i -> (i + 1 until mods.size).map { j -> "§${gcd(mods[i], mods[j])}\\mid ${ps[i].first}-${p(ps[j].first)}§" } }.joinToString(", ") + ". Preto riešenie existuje a je jediné modulo §\\lcm=${sol.mod}§, lebo dve riešenia sa líšia o spoločný násobok modulov (Věta 6.20)."))
                if (sol == null) {
                    bad?.let { (i, j) -> exists(tx("§\\gcd(${mods[i]},${mods[j]})=${gcd(mods[i], mods[j])}§ nedelí §${ps[i].first}-${ps[j].first}=${ps[i].first - ps[j].first}§, preto sústava nemá riešenie: z oboch kongruencií by bol rozdiel pravých strán násobkom §\\gcd§ modulov (Věta 6.20).")) }
                    result(tx("Žiadne §x§ nespĺňa všetky kongruencie naraz."))
                    return@solve
                }
                // a new letter for every parameter, as Spěvák writes them (t, then u; 8. prednáška)
                val letters = listOf("t", "u", "v", "w")
                var x = ps[0].first; var mod = ps[0].second; var at = 0
                val fs = ArrayList<Formula>()
                fs += disp("x=" + Terms.sum("$x", Terms.times(mod, letters[at])), "z prvej kongruencie")
                for ((r, m) in ps.drop(1)) {
                    val t = letters[at]; val rhs = md(r - x, m)
                    val s = lkon(mod, rhs, m)!!
                    val head = "\\begin{aligned}&" + Terms.sum("$x", Terms.times(mod, t)) + "\\equiv $r\\pmod{$m}\\\\\\Rightarrow\\ &${Terms.times(mod, t)}\\equiv $rhs\\pmod{$m}"
                    if (s.n == 1L) { fs += disp("$head\\end{aligned}", "Platí pre každé §$t§, lebo §$m§ delí §$mod§ aj §$rhs§; táto kongruencia nič nepridá."); continue }
                    val a1 = md(mod / s.g, s.n); val inv = inverse(a1, s.n)!!
                    val cut = if (s.g > 1) "\\\\\\Rightarrow\\ &${Terms.times(mod / s.g, t)}\\equiv ${rhs / s.g}\\pmod{${s.n}}" else ""
                    fs += disp("$head$cut\\\\\\Rightarrow\\ &$t\\equiv ${s.x0}\\pmod{${s.n}}\\end{aligned}",
                        (if (s.g > 1) "Krátim §${s.g}§ aj s modulom: §$m/${s.g}=${s.n}§ (Věta 6.13). " else "") + (if (s.n > 1) "Inverzia: §${a1}\\cdot $inv=${a1 * inv}\\equiv 1\\pmod{${s.n}}§." else ""))
                    val nm = lcm(mod, m); val nx = md(x + mod * s.x0, nm); val u = letters[at + 1]
                    fs += disp("x=" + Terms.sum("$x", Terms.times(mod, "(" + Terms.sum("${s.x0}", Terms.times(s.n, u)) + ")")) + "=" + Terms.sum("$nx", Terms.times(nm, u)) + ",\\quad $u\\in\\mathbb{Z}")
                    x = nx; mod = nm; at++
                }
                subst(tx("Z prvej kongruencie vyjadrím §x§ s parametrom, dosadím do ďalšej a vyriešim ju pre parameter. Dostanem teda §x§, ktoré spĺňa obe naraz (Poznámka 6.12, Příklad 6.20)."), fs)
                result(tx("Všetky riešenia sú §x\\equiv ${sol.x}\\pmod{${sol.mod}}§, najväčšie záporné je §${sol.x - sol.mod}§. Skúška dosadením:"), "\\begin{aligned}" + ps.joinToString(",\\\\") { (r, m) -> "${sol.x}&\\equiv $r\\pmod{$m}" } + "\\end{aligned}")
            }
        },
        hint = tx("Moduly nesúdeliteľné: ČVOZ. Inak over §\\gcd(m_i,m_j)\\mid a_i-a_j§ a dosadzuj: §x=a_1+m_1t§ do ďalšej."),
    )
}

/* ---- 8. Combined: two congruences to reduce, then the system (test 6. 12. 2023 A, Cvičení 6.28) --------------- */

/**
 * [base]^[exp] is the second coefficient when [exp] > 0 (Cvičení 6.28), else the plain [a2v]. [powerFirst]: the paper
 * writes the congruence with the power first (2024 F, 2026 Ř), so the solution goes in the paper's order. [negative]:
 * the paper asks for the largest negative solution too (2023 A, 2024 F; not 2026 Ř).
 */
data class TcComboTask(val a1v: Long, val b1: Long, val m1: Long, val a2v: Long, val b2: Long, val m2: Long, val base: Long = 0, val exp: Long = 0, val powerFirst: Boolean = false, val negative: Boolean = true) {
    val power get() = exp > 0
    val a2 get() = if (power) "$base^{$exp}" else "$a2v"
}

fun tcComboTask(r: Random): TcComboTask {
    while (true) {
        // first: g1 x-coefficient factor shared with the modulus, as 6x ≡ 36 (mod 195)
        val n1 = r.rl(5, 70); val g1 = r.rl(2, 5); val m1 = n1 * g1
        val u = r.rl(2, 15)
        if (gcd(u, n1) != 1L || gcd(u * g1, m1) != g1 || m1 > 300) continue
        val m2 = r.rl(7, 99)
        val l = lcm(n1, m2)
        if (l > 2000 || l == n1 * m2 && r.nextBoolean()) continue
        val x = r.rl(1, l - 1)
        val a1v = u * g1; val b1 = md(a1v * x, m1)
        // second: a power as the coefficient (Cvičení 6.28) or a plain one
        val power = r.nextBoolean()
        val c = r.rl(2, 9); val ph2 = phi(m2)
        val e = ph2 * r.rl(maxOf(1L, 300L / ph2), 2000L / ph2) + r.rl(1, minOf(TcSmallPower.toLong(), ph2 - 1))
        val a2v = if (power) powMod(c, e, m2) else r.rl(2, m2 - 1)
        if (gcd(a2v, m2) != 1L || a2v <= 1L) continue
        if (power && gcd(c, m2) != 1L) continue
        val b2 = md(a2v * x, m2)
        return TcComboTask(a1v, b1, m1, a2v, b2, m2, if (power) c else 0, if (power) e else 0)
    }
}

fun tcCombo(t: TcComboTask): Exercise {
    val s1 = lkon(t.a1v, t.b1, t.m1)!!
    val s2 = lkon(t.a2v, t.b2, t.m2)!!
    val sol = crt(listOf(s1.x0 to s1.n, s2.x0 to s2.n))!!
    val power = t.power; val base = t.base; val exp = t.exp
    val pf = t.powerFirst && power
    require(t.negative || pf) { "a combo without the largest negative solution is only the power-first one (2026 Ř)" }
    // the paper's order: (r_1, n_1) is the congruence written first
    val (w1, w2) = if (pf) "Druhá" to "Prvá" else "Prvá" to "Druhá"
    val (u, v) = if (pf) s2 to s1 else s1 to s2
    val first = "${t.a1v}x\\equiv ${t.b1}\\pmod{${t.m1}}"; val second = "${t.a2}x\\equiv ${t.b2}\\pmod{${t.m2}}"
    val reducePart: (List<Any>) -> Part = { _ -> Part(tx((if (pf) "$w1 ako §x\\equiv r_2\\pmod{n_2}§:" else "$w1 kongruencia ako §x\\equiv r_1\\pmod{n_1}§, §0\\leq r_1<n_1§:")),
        nums(if (pf) listOf("r_2", "n_2") else listOf("r_1", "n_1"), listOf(s1.x0, s1.n), "§x\\equiv ${s1.x0}\\pmod{${s1.n}}§") { gv ->
        when {
            gv[1] == t.m1 -> "Modul si nezmenšil. Krátenie §${s1.g}§ delí aj modul (Věta 6.13): §${t.m1}/${s1.g}=${s1.n}§."
            gv[1] == s1.n && md(gv[0], s1.n) == s1.x0 -> "Správne až na redukciu: §${if (pf) "r_2" else "r_1"}=${s1.x0}§."
            gv[1] != s1.n -> "Modul nesedí: §\\gcd(${t.a1v},${t.m1})=${s1.g}§, teda §${if (pf) "n_2" else "n_1"}=${s1.n}§."
            else -> "Skúška: §${t.a1v}\\cdot ${gv[0]}\\equiv ${md(t.a1v * gv[0], t.m1)}\\pmod{${t.m1}}§, má byť §${t.b1}§."
        }
    }, aim = if (pf) 1 else 0, hint = tx("Vydeľ koeficient, pravú stranu aj modul číslom §\\gcd§ koeficientu a modulu (Věta 6.13); zmenší sa aj modul. Potom vynásob inverziou koeficientu.")) }
    val invertPart: (List<Any>) -> Part = { _ -> Part(tx((if (pf) "$w2 kongruencia ako §x\\equiv r_1\\pmod{n_1}§, §0\\leq r_1<n_1§:" else "$w2 ako §x\\equiv r_2\\pmod{n_2}§:")),
        nums(if (pf) listOf("r_1", "n_1") else listOf("r_2", "n_2"), listOf(s2.x0, s2.n), "§x\\equiv ${s2.x0}\\pmod{${s2.n}}§") { gv ->
        when {
            power && gv[1] == t.m2 && gv[0] == md(t.b2 * (inverse(powMod(base, exp % t.m2, t.m2), t.m2) ?: -1), t.m2) -> "Exponent si redukoval modulo §${t.m2}§. Správne modulo §\\varphi(${t.m2})=${phi(t.m2)}§: §${t.a2}\\equiv ${t.a2v}§."
            gv[1] == s2.n && md(gv[0], s2.n) == s2.x0 -> "Správne až na redukciu: §${if (pf) "r_1" else "r_2"}=${s2.x0}§."
            gv[1] != s2.n -> "Modul nesedí: §\\gcd(${t.a2v},${t.m2})=1§, modul ostáva §${t.m2}§."
            else -> "Skúška: §${t.a2v}\\cdot ${gv[0]}\\equiv ${md(t.a2v * gv[0], t.m2)}\\pmod{${t.m2}}§, má byť §${t.b2}§." + (if (power) " Koeficient §${t.a2}\\equiv ${t.a2v}§." else "")
        }
    }, aim = if (pf) 0 else 1, hint = tx((if (power) "Mocninu v koeficiente najprv zredukuj: podľa Eulerovej vety stačí exponent modulo §\\varphi§ modulu. " else "") + "Over §\\gcd§ koeficientu a modulu, potom obe strany vynásob inverziou koeficientu.")) }
    return Exercise(
        "dml-tc-combo", "Kongruencie a sústava naraz", "Šablóna testu 6. 12. 2023 (variant A), príklad 3, a Cvičení 6.28",
        tx("Nalezněte všechna řešení soustavy kongruencí. " + (if (t.negative) "Které z těchto řešení je největší záporné? Všechna svá tvrzení zdůvodněte." else "Každý svůj krok pečlivě vysvětlujte.") + " §" +
            "\\begin{matrix} " + (if (pf) "$second \\\\ $first" else "$first \\\\ $second") + " \\end{matrix}" + "§"),
        parts = (if (pf) listOf(invertPart, reducePart) else listOf(reducePart, invertPart)) + listOf<(List<Any>) -> Part>(
            { _ -> Part(tx("Sústava: §x\\equiv x_0\\pmod{M}§:"), nums(listOf("x_0", "M"), listOf(sol.x, sol.mod), "§x\\equiv ${sol.x}\\pmod{${sol.mod}}§") { gv ->
                val ok = md(gv[0] - s1.x0, s1.n) == 0L && md(gv[0] - s2.x0, s2.n) == 0L
                when {
                    ok && gv[1] == s1.n * s2.n && sol.mod != gv[1] -> "Modul je §\\lcm(${u.n},${v.n})=${sol.mod}§, nie súčin: §\\gcd=${gcd(s1.n, s2.n)}§ (Věta 6.20)."
                    ok && gv[1] == sol.mod -> "Správne až na redukciu: §x_0=${sol.x}§."
                    ok -> "§x_0§ sedí, modul je §\\lcm(${u.n},${v.n})=${sol.mod}§."
                    else -> "Skúška nesedí: §${gv[0]}§ nespĺňa obe kongruencie §x\\equiv ${u.x0}\\pmod{${u.n}}§, §x\\equiv ${v.x0}\\pmod{${v.n}}§."
                }
            }, aim = 2, hint = tx("Riešenie prvej §x=r_1+n_1t§ dosaď do druhej a vyrieš pre §t§. Modul sústavy je §\\lcm(n_1,n_2)§.")) },
        ) + (if (!t.negative) emptyList() else listOf<(List<Any>) -> Part>(
            { _ -> Part(tx("Najväčšie záporné riešenie:"), nums(listOf("x"), listOf(sol.x - sol.mod), "§${sol.x - sol.mod}§") { gv ->
                if (gv[0] == -sol.x) "Opačné číslo riešením nie je. Od §${sol.x}§ odčítaj §${sol.mod}§." else "Riešenia sú §${sol.x}+${sol.mod}k§; najväčšie záporné je pre §k=-1§."
            }, aim = 3, hint = tx("Všetky riešenia sú §x_0+kM§, §k\\in\\mathbb{Z}§; vyber z nich najväčšie záporné.")) },
        )),
        postup = if (!t.negative) TcComboAllPostup else if (pf) TcComboPowerFirstPostup else TcComboPostup,
        solution = { _ ->
            val pp = if (!t.negative) TcComboAllPostup else if (pf) TcComboPowerFirstPostup else TcComboPostup
            val one = pp[0]; val two = pp[1]; val system = pp[2]
            val reduceAim = if (pf) two else one; val invertAim = if (pf) one else two
            pp.solve {
                fun reduce() {
                    reduceAim(tx((if (pf) "" else "Každú kongruenciu najprv vyriešim zvlášť, lebo sústava platí, len keď platí každá. ") + "V ${if (pf) "druhej" else "prvej"} §\\gcd(${t.a1v},${t.m1})=${s1.g}§ delí §${t.b1}§, preto ju celú aj s modulom vydelím §${s1.g}§: §${t.m1}\\mid ${Terms.sum(Terms.times(t.a1v, "x"), "${-t.b1}")}§ práve vtedy, keď §${s1.n}\\mid ${Terms.sum(Terms.times(t.a1v / s1.g, "x"), "${-t.b1 / s1.g}")}§ (Věta 6.13)."),
                        disp("\\begin{aligned}&${Terms.times(t.a1v, "x")}\\equiv ${t.b1}\\pmod{${t.m1}}\\\\\\Leftrightarrow\\ &${Terms.times(t.a1v / s1.g, "x")}\\equiv ${t.b1 / s1.g}\\pmod{${s1.n}}\\end{aligned}"))
                    reduceAim(tx("Koeficient vynásobím jeho inverziou modulo §${s1.n}§ (REA, pri malom module aj z hlavy); skúška je v druhom riadku."), "\\begin{aligned}${md(t.a1v / s1.g, s1.n)}^{-1}&\\equiv ${inverse(md(t.a1v / s1.g, s1.n), s1.n)}\\pmod{${s1.n}}\\\\${md(t.a1v / s1.g, s1.n)}\\cdot ${inverse(md(t.a1v / s1.g, s1.n), s1.n)}&=${md(t.a1v / s1.g, s1.n) * inverse(md(t.a1v / s1.g, s1.n), s1.n)!!}\\equiv 1\\\\x&\\equiv ${s1.x0}\\pmod{${s1.n}}\\end{aligned}")
                }
                fun invert() {
                    if (power) {
                        val ph = phi(t.m2)
                        invertAim(tx((if (pf) "Každú kongruenciu najprv vyriešim zvlášť, lebo sústava platí, len keď platí každá. V prvej k" else "K") + "oeficient §${t.a2}§ najprv zredukujem: §\\gcd($base,${t.m2})=1§, preto exponent stačí brať modulo §\\varphi(${t.m2})§ (Věta 6.15)."),
                            *(listOf(tcPhiDisp(t.m2), "$exp\\equiv ${exp % ph}\\pmod{$ph}") + (if (base >= t.m2) listOf("$base\\equiv ${md(base, t.m2)}\\pmod{${t.m2}}") else emptyList())).toTypedArray())
                        smallPower(invertAim, base, exp % ph, t.m2)
                    }
                    if (t.a2v == 1L) invertAim(tx("Koeficient je §1§, kongruencia už má tvar §x\\equiv ${s2.x0}\\pmod{${s2.n}}§."), "x\\equiv ${t.b2}\\pmod{${t.m2}}")
                    else invertAim(tx("${if (pf) "Prvú" else "Druhú"} kongruenciu vynásobím inverziou koeficientu (REA, pri malom module aj z hlavy); skúška je v treťom riadku."), "\\begin{aligned}${Terms.times(t.a2v, "x")}&\\equiv ${t.b2}\\pmod{${t.m2}}\\\\${t.a2v}^{-1}&\\equiv ${inverse(t.a2v, t.m2)}\\pmod{${t.m2}}\\\\${t.a2v}\\cdot ${inverse(t.a2v, t.m2)}&=${t.a2v * inverse(t.a2v, t.m2)!!}\\equiv 1\\\\x&\\equiv ${s2.x0}\\pmod{${s2.n}}\\end{aligned}")
                }
                if (pf) { invert(); reduce() } else { reduce(); invert() }
                val g = gcd(u.n, v.n)
                system(tx(if (g == 1L) "Moduly §${u.n},${v.n}§ sú nesúdeliteľné, preto je riešenie jediné modulo §${sol.mod}§. Dve riešenia sa totiž líšia o násobok oboch modulov, teda ich súčinu, a jedno nájdem dosadením, lebo prvý modul je invertovateľný modulo druhého (Věta 6.19, ČVOZ)." else "§\\gcd(${u.n},${v.n})=$g§ delí §${u.x0}-${v.x0}§, preto riešenie existuje. Je jediné modulo §\\lcm=${sol.mod}§, lebo dve riešenia sa líšia o spoločný násobok modulov (Věta 6.20)."))
                val tt = lkon(u.n, v.x0 - u.x0, v.n)!!
                system(tx("Riešenie prvej dosadím do druhej a vyriešim pre parameter."),
                    disp("\\begin{aligned}&x=" + Terms.sum("${u.x0}", Terms.times(u.n, "t")) + ":\\\\&${Terms.times(u.n, "t")}\\equiv ${v.x0 - u.x0}\\pmod{${v.n}}\\\\\\Rightarrow\\ &t\\equiv ${tt.x0}\\pmod{${tt.n}}\\end{aligned}", if (tt.n != v.n) "Krátim §${tt.g}§: modul §${v.n}/${tt.g}=${tt.n}§ (Věta 6.13)." else null),
                    disp("x\\equiv " + Terms.sum("${u.x0}", Terms.times(tt.x0, "${u.n}")) + "\\equiv ${sol.x}\\pmod{${sol.mod}}"))
                if (t.negative) pp[3](tx("Najväčšie záporné riešenie dostanem odčítaním modulu."), "${sol.x}-${sol.mod}=${sol.x - sol.mod}")
                else system(tx("Všetky riešenia sú §x=${sol.x}+${sol.mod}k§, §k\\in\\mathbb{Z}§."))
            }
        },
        hint = tx("Najprv každú kongruenciu zvlášť na tvar §x\\equiv r\\pmod{n}§; pozor na krátenie modulu. Potom sústava."),
    )
}

/* ---- Registration ------------------------------------------------------------------------------------------------ */

val TcGenerators: Map<String, (Random) -> Exercise> = mapOf(
    "dml-tc-rea" to { r -> tcRea(tcReaTask(r)) },
    "dml-tc-ldr" to { r -> tcLdr(tcLdrTask(r)) },
    "dml-tc-inv" to { r -> var t = tcInvTask(r); while (t.bound) t = tcInvTask(r); tcInv(t) },
    "dml-tc-inv-min" to { r -> tcInv(TcInvTask(SmallPrimeProducts.random(r), 0, true)) },
    "dml-tc-pow" to { r -> tcPow(tcPowTask(r)) },
    "dml-tc-tower" to { r -> tcTower(tcTowerTask(r)) },
    "dml-tc-lkon" to { r -> tcLkon(tcLkonTask(r)) },
    "dml-tc-crt" to { r -> tcCrt(tcCrtTask(r)) },
    "dml-tc-combo" to { r -> tcCombo(tcComboTask(r)) },
    "dml-tc-slovna" to { r -> tcWord(tcWordTask(r)) },
    "dml-tc-interval" to { r -> tcInterval(tcIntervalTask(r)) },
    "dml-tc-jedine" to { r -> tcOnly(tcOnlyTask(r)) },
    "dml-tc-inv-euler" to { r -> tcInvEuler(tcInvEulerTask(r)) },
    "dml-tc-deleni" to { r -> tcDiv(tcDivTask(r)) },
    "dml-tc-delitelnost" to ::tcDivProofExercise,
    "dml-tc-vsetky-n" to { r -> tcAllN(tcAllNTask(r), r) },
    "dml-tc-rozklad" to { r -> tcFactor(tcFactorTask(r)) },
    "dml-tc-slovna-ldr" to { r -> tcWordLdr(tcWordLdrTask(r)) },
    "dml-tc-redukcia" to { r -> tcReduce(tcReduceTask(r)) },
)

val TcTemplateList = listOf(
    TemplateInfo("dml-tc-deleni", "Podiel a zvyšok", "Aj záporné číslo a výraz; Věta 6.2"),
    TemplateInfo("dml-tc-delitelnost", "Vlastnosti deliteľnosti", "Dôkaz z definície alebo protipríklad; Cvičení 6.1, 6.2"),
    TemplateInfo("dml-tc-vsetky-n", "Deliteľnosť pre každé n", "Bez indukcie: zvyšky, kongruencie; Cvičení 6.3, 6.4"),
    TemplateInfo("dml-tc-rozklad", "gcd a lcm z rozkladov", "Menší a väčší exponent; Pozorování 6.1"),
    TemplateInfo("dml-tc-rea", "gcd, Bézout, lcm", "REA tabuľkou; Cvičení 6.5"),
    TemplateInfo("dml-tc-ldr", "Diofantická rovnica", "Riešiteľnosť a všetky riešenia"),
    TemplateInfo("dml-tc-slovna-ldr", "Slovná diofantická rovnica", "Nákup, mince, presýpacie hodiny; Cvičení 6.9 až 6.11"),
    TemplateInfo("dml-tc-redukcia", "Redukcia výrazu modulo m", "Zvyšky aj záporné, opačný prvok; Příklad 6.10"),
    TemplateInfo("dml-tc-inv-min", "Najmenší modul s inverziou", "Príklad 3 z testov 2024 a 2025"),
    TemplateInfo("dml-tc-inv", "Inverzia v $\\mathbb{Z}_m$", "REA na m a a"),
    TemplateInfo("dml-tc-inv-euler", "Inverzia cez Eulerovu vetu", "\$a^{\\varphi(m)-1}\$; Tvrzení 6.7 a 6.11"),
    TemplateInfo("dml-tc-tower", "Dva zvyšky, druhý z prvého", "Príklad 3 z testov 2023 až 2025"),
    TemplateInfo("dml-tc-pow", "Veľká mocnina modulo m", "Fermat, Euler, binárne mocnenie"),
    TemplateInfo("dml-tc-lkon", "Lineárna kongruencia", "Krátenie modulu, riešenia v $\\mathbb{Z}_m$"),
    TemplateInfo("dml-tc-crt", "Sústava kongruencií", "ČVOZ aj súdeliteľné moduly"),
    TemplateInfo("dml-tc-combo", "Kongruencie a sústava naraz", "Test 2023 a Cvičení 6.28"),
    TemplateInfo("dml-tc-slovna", "Slovná úloha na sústavu", "Zvyšky, cykly, balenie; Cvičení 6.26, 6.27"),
    TemplateInfo("dml-tc-interval", "Sústava a riešenia v intervale", "Koľko ich je, práve jedno?"),
    TemplateInfo("dml-tc-jedine", "Prečo iné riešenia nie sú", "Riešenie a zdôvodnenie na papier; Cvičení 6.32"),
)

/* ---- Methods of the worked examples in the lessons ------------------------------------------------------------- */

val TcDivisionPostup = postup(
    "Deľ absolútnu hodnotu" to "Vydeľ §|a|§ so zvyškom a vráť znamienko.",
    "Oprav zvyšok" to "Zvyšok musí byť v §\\{0,\\dots,d-1\\}§; záporný oprav: podiel o §1§ menší, zvyšok o §d§ väčší.",
    "Skúška" to "§q\\cdot d+r§ musí dať §a§.",
)

val TcEuclidPostup = postup(
    "Deľ so zvyškom" to "Väčšie číslo vydeľ menším: §a=q\\cdot b+r§.",
    "Opakuj" to "Deľ predošlého deliteľa predošlým zvyškom, kým nevyjde zvyšok §0§.",
    "Posledný nenulový zvyšok" to "Ten je §\\gcd§.",
)

val TcBezoutPostup = postup(
    "Tabuľka REA" to "Riadky §(a,1,0)§ a §(b,0,1)§; každý ďalší je predpredošlý mínus §q§-násobok predošlého.",
    "§\\gcd§ a koeficienty" to "Riadok nad nulou: §\\gcd=\\alpha\\cdot a+\\beta\\cdot b§.",
    "Skúška" to "Dosaď koeficienty a vypočítaj kombináciu.",
)

val TcLdrOnePostup = postup(
    "§\\gcd§ cez REA" to "REA dá §\\gcd(a,b)§ aj jeho Bézoutove koeficienty.",
    "Má rovnica riešenie?" to "Riešenie existuje práve vtedy, keď §\\gcd(a,b)§ delí pravú stranu.",
    "Jedno riešenie" to "Bézoutove koeficienty vynásob číslom §c/\\gcd§ a urob skúšku.",
)

val TcLdrNonnegPostup = postup(
    "Všetky riešenia" to "Jedno riešenie plus §k\\cdot(b/\\gcd,-a/\\gcd)§.",
    "Podmienky na §k§" to "Z požiadaviek na §x§ a §y§ dostaneš nerovnosti pre §k§.",
    "Vyhovujúce riešenia" to "Dosaď prípustné §k§.",
)

val TcReducePostup = postup(
    "Zvyšky čísel" to "Každé číslo nahraď jeho zvyškom (Tvrzení 6.6).",
    "Počítaj so zvyškami" to "Sčítaj a násob zvyšky, výsledok znova zredukuj.",
)

package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dml.numbers.*
import kotlin.random.Random

/*
 * Systems of congruences beyond a ready system, as the textbook and the 2nd test ask them (Spěvák, 8. prednáška; Cvičení
 * 6.26, 6.27 a 6.32; test 6. 12. 2023 (variant A), príklad 3; Tvrzení 6.7 a 6.11): a word problem turned into a system,
 * the solutions in an interval, why no other solutions exist (written on paper), the inverse through Euler's theorem.
 * Numbers are drawn, everything asked is computed by NumbersMath.kt; moduli and products stay small enough for paper.
 */

private fun Random.r(lo: Int, hi: Int) = nextInt(lo, hi + 1)
private fun Random.rl(lo: Long, hi: Long) = nextLong(lo, hi + 1)
private fun tx(s: String) = s.replace('§', '$')
private fun disp(s: String, note: String? = null) = Formula(s, note?.let(::tx))
private fun p(n: Long) = if (n < 0) "($n)" else "$n"

private fun nums(labels: List<String>, expected: List<Long>, shown: String, diag: (List<Long>) -> String) =
    Answer.Numbers(labels, null, expected.map { Frac.of(it) }, tx(shown), diagnose = { g -> tx(diag(g.map { if (it.isInt) it.num else Long.MIN_VALUE })) })

private fun yesNo(yes: Boolean, whyYes: String, whyNo: String, wrongYes: String, wrongNo: String) =
    Answer.Choice(listOf("Áno", "Nie"), if (yes) 0 else 1, tx(if (yes) whyYes else whyNo), wrong = mapOf((if (yes) 1 else 0) to tx(if (yes) wrongNo else wrongYes)))

private fun lcmOf(ms: List<Long>) = ms.fold(1L) { a, b -> lcm(a, b) }

/** Solutions of x ≡ x0 (mod M) in [lo, hi]. */
private fun inRange(x0: Long, mod: Long, lo: Long, hi: Long): List<Long> {
    var x = lo + md(x0 - lo, mod)
    val out = ArrayList<Long>()
    while (x <= hi) { out += x; x += mod }
    return out
}

/* ---- One linear congruence ax ≡ b (mod m), as Spěvák reduces it ------------------------------------------------ */

/** a x ≡ b (mod m) of a system; a = 1 is written "x ≡ b". */
data class TcCong(val a: Long, val b: Long, val m: Long) {
    val tex get() = Terms.times(a, "x") + "\\equiv $b\\pmod{$m}"
    val solved get() = lkon(a, b, m)
}

/**
 * The reduction of one congruence to x ≡ r (mod n), as a chain with a note: cancel the gcd with the modulus (Věta 6.13),
 * then cancel a coefficient coprime to the modulus or multiply by its inverse. Null when it has no solution.
 */
private fun reduceFormula(c: TcCong): Formula? {
    val s = c.solved ?: return null
    val (a, b, m) = c
    if (md(a, m) == 1L) return if (b in 0 until m) null else disp("x\\equiv $b\\equiv ${s.x0}\\pmod{$m}", "Pravú stranu zredukujem.")
    val rows = arrayListOf("&${c.tex}")
    val notes = ArrayList<String>()
    var aa = a; var bb = b; var n = m
    if (s.g > 1) {
        aa = a / s.g; bb = b / s.g; n = s.n
        rows += "\\Leftrightarrow\\ &${Terms.times(aa, "x")}\\equiv $bb\\pmod{$n}"
        notes += "Krátim §${s.g}§ aj s modulom: §$m/${s.g}=$n§ (Věta 6.13)."
    }
    if (n > 1 && md(aa, n) != 1L) {
        val a1 = md(aa, n)
        if (md(bb, a1) == 0L && bb >= 0 && a1 == aa) {
            rows += "\\Leftrightarrow\\ &x\\equiv ${bb / a1}\\pmod{$n}"
            notes += "Krátim §$a1§ bez zmeny modulu, lebo §\\gcd($a1,$n)=1§ (Věta 6.13)."
            if (bb / a1 != s.x0) rows += "\\Leftrightarrow\\ &x\\equiv ${s.x0}\\pmod{$n}"
        } else {
            val inv = inverse(a1, n)!!
            rows += if (md(bb, n) <= 1L) "\\Leftrightarrow\\ &x\\equiv ${s.x0}\\pmod{$n}" else "\\Leftrightarrow\\ &x\\equiv $inv\\cdot $bb=${inv * bb}\\equiv ${s.x0}\\pmod{$n}"
            notes += "Vynásobím inverziou §$a1^{-1}\\equiv $inv§, skúška §$a1\\cdot $inv=${a1 * inv}\\equiv 1\\pmod{$n}§."
        }
    } else if (bb != s.x0) rows += "\\Leftrightarrow\\ &x\\equiv ${s.x0}\\pmod{$n}"
    return disp("\\begin{aligned}" + rows.joinToString("\\\\") + "\\end{aligned}", notes.joinToString(" "))
}

/** The first pair of reduced congruences that contradict each other (ZČVOZ, Věta 6.20), or null. */
private fun clash(ps: List<Pair<Long, Long>>): Pair<Int, Int>? =
    ps.indices.flatMap { i -> (i + 1 until ps.size).map { j -> i to j } }.firstOrNull { (i, j) -> md(ps[i].first - ps[j].first, gcd(ps[i].second, ps[j].second)) != 0L }

/** Substitution step by step (Příklad 6.20; a new letter for every parameter, as Spěvák writes them). */
private fun substitution(ps: List<Pair<Long, Long>>): List<Formula> {
    val letters = listOf("t", "u", "v", "w")
    var x = md(ps[0].first, ps[0].second); var mod = ps[0].second; var at = 0
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
            (if (s.g > 1) "Krátim §${s.g}§ aj s modulom: §$m/${s.g}=${s.n}§ (Věta 6.13). " else "") + "Inverzia: §${a1}\\cdot $inv=${a1 * inv}\\equiv 1\\pmod{${s.n}}§.")
        val nm = lcm(mod, m); val nx = md(x + mod * s.x0, nm); val u = letters[at + 1]
        fs += disp("x=" + Terms.sum("$x", Terms.times(mod, "(" + Terms.sum("${s.x0}", Terms.times(s.n, u)) + ")")) + "=" + Terms.sum("$nx", Terms.times(nm, u)) + ",\\quad $u\\in\\mathbb{Z}")
        x = nx; mod = nm; at++
    }
    return fs
}

/** The sentence why the system has a solution and modulo what it is unique (Věty 6.19 a 6.20). */
private fun existsSentence(ps: List<Pair<Long, Long>>, mod: Long): String {
    val ms = ps.map { it.second }
    val coprime = ms.indices.all { i -> ms.indices.all { j -> i == j || gcd(ms[i], ms[j]) == 1L } }
    return if (coprime) "Moduly sú navzájom nesúdeliteľné, preto riešenie existuje a je jediné modulo ich súčinu §$mod§ (Věta 6.19, ČVOZ)."
    else "Moduly sú súdeliteľné, ale §\\gcd§ každých dvoch delí rozdiel zvyškov: " +
        ps.indices.flatMap { i -> (i + 1 until ps.size).map { j -> "§${gcd(ms[i], ms[j])}\\mid ${ps[i].first}-${p(ps[j].first)}§" } }.joinToString(", ") +
        ". Preto riešenie existuje a je jediné modulo §\\lcm=$mod§, lebo dve riešenia sa líšia o spoločný násobok modulov (Věta 6.20)."
}

/** The diagnosis of a typed x0 (mod M) of a system [ps] whose answer is [sol]; [orig] the moduli before cancelling. */
private fun systemDiag(ps: List<Pair<Long, Long>>, sol: Crt, orig: List<Long>?, g: List<Long>): String {
    val (x0, mm) = g
    val ms = ps.map { it.second }
    val prod = ms.fold(1L) { a, b -> a * b }
    val ok = ps.all { (r, m) -> md(x0 - r, m) == 0L }
    val origL = orig?.let(::lcmOf)
    return when {
        origL != null && origL != sol.mod && mm == origL -> "Moduly si nezmenšil krátením. Krátiš aj modul (Věta 6.13), preto je krok §\\lcm(${ms.joinToString(",")})=${sol.mod}§, nie §$origL§."
        ok && mm == prod && prod != sol.mod -> "Modul je §\\lcm=${sol.mod}§, nie súčin. Moduly sú súdeliteľné, dve riešenia sa líšia o spoločný násobok (Věta 6.20)."
        ok && mm == sol.mod -> "Správne až na redukciu: §x_0=${md(x0, sol.mod)}§ z §\\{0,\\dots,${sol.mod - 1}\\}§."
        ok -> "§x_0§ sedí, modul nie. Riešenie je jediné modulo §\\lcm=${sol.mod}§."
        else -> ps.first { (r, m) -> md(x0 - r, m) != 0L }.let { (r, m) -> "Skúška nesedí: §$x0\\equiv ${md(x0, m)}\\pmod{$m}§, má byť §$r§." }
    }
}

/* ---- 1. A word problem leading to a system (Cvičení 6.26, 6.27) -------------------------------------------------- */

/** Turn a story into a system and answer it (Cvičení 6.26, 6.27; Věty 6.19, 6.20). */
val TcWordPostup = postup(
    "Sústava z textu" to "Každá veta je zvyšok po delení veľkosťou skupiny; „chýba §k§“ je zvyšok §-k§, teda §m-k§.",
    "Má sústava riešenie?" to "Nesúdeliteľné moduly: vždy (ČVOZ). Inak §\\gcd§ dvoch modulov musí deliť rozdiel zvyškov.",
    "Postupné dosadzovanie" to "Z prvej §x=r_1+m_1t§, dosaď do ďalšej a vyrieš pre §t§.",
    "Odpoveď z príbehu" to "Všetky riešenia sú §x_0+kM§; vyber to, ktoré spĺňa podmienku príbehu.",
)

/** How a story says one congruence: the remainder, the missing count, a year or a first second. */
enum class TcSaid { LEFT, MISSING, NONE, YEAR, FIRST }

/**
 * A story's numbers: the moduli, the remainders, how each is said and the number the text says ([said]: the remainder,
 * the count missing, the year), the answer [n] and the window [lo, hi] in which it is the only solution.
 */
data class TcWordTask(val story: Int, val mods: List<Long>, val said: List<Long>, val how: List<TcSaid>, val n: Long, val lo: Long, val hi: Long) {
    val res get() = mods.indices.map { i -> md(n, mods[i]) }
}

/** Czech number agreement: 1 výrobek, 2 výrobky, 5 výrobků. */
private fun cz(k: Long, one: String, few: String, many: String) = "$k " + when { k == 1L -> one; k in 2..4 -> few; else -> many }
/** "zbude 1 výrobek", "zbudou 2 výrobky", "zbude 5 výrobků" */
private fun zbude(k: Long, one: String, few: String, many: String) = (if (k in 2..4) "zbudou " else "zbude ") + cz(k, one, few, many)

private class Story(
    val sets: List<List<Long>>,
    /** RANGE: the only solution in [lo, hi]; MOST: at most hi (from 1); AFTER: the first year after 2026; FIRST: the first second. */
    val ask: String,
    val missing: Boolean,
    /** What the answer is, in Slovak, for the last part ("Počet výrobkov"). */
    val what: String,
    val text: (TcWordTask) -> String,
)

private const val YEAR = 2026L

private fun phr(t: TcWordTask, i: Int, left: (Long) -> String, miss: (Long) -> String, none: String) = when (t.how[i]) {
    TcSaid.LEFT -> left(t.said[i]); TcSaid.MISSING -> miss(t.said[i]); TcSaid.NONE -> none; else -> ""
}

/** "Když je balíme po 8, zbudou 2 výrobky, po 9 chybí 1 výrobek, po 5 zbudou 3 výrobky." */
private fun clauses(t: TcWordTask, head: String, by: (Int) -> String, left: (Long) -> String, miss: (Long) -> String, none: String) =
    t.mods.indices.joinToString(", ") { i -> (if (i == 0) head else "") + by(i) + (if (i == 0 && head.startsWith("Když")) "," else "") + " " + phr(t, i, left, miss, none) } + "."

private val Stories: List<Story> = listOf(
    // Cvičení 6.26: a production line packing into boxes
    Story(listOf(listOf(8, 9, 5), listOf(12, 10, 9), listOf(16, 9), listOf(25, 12), listOf(14, 20), listOf(12, 16, 10), listOf(15, 8, 7)).map { it.map(Int::toLong) }, "RANGE", true, "Počet výrobkov") { t ->
        "Na lince se denně vyrobí stejný počet výrobků. " + clauses(t, "Když je balíme ", { "po ${t.mods[it]} kusech" }, { zbude(it, "výrobek", "výrobky", "výrobků") }, { "chybí do poslední krabice " + cz(it, "výrobek", "výrobky", "výrobků") }, "nezbude nic") +
            " Denně se vyrobí ${t.lo} až ${t.hi} výrobků. Kolik jich je? Navrhněte co nejmenší zvýšení výroby, aby při žádném z těchto balení nic nezbylo."
    },
    // Cvičení 6.27: eggs laid out in rows
    Story(listOf(listOf(3, 4, 5), listOf(4, 5, 7), listOf(3, 5, 7), listOf(5, 6, 7), listOf(4, 6, 7), listOf(3, 7, 8), listOf(5, 8, 9)).map { it.map(Int::toLong) }, "MOST", true, "Počet vajec") { t ->
        "Vesničan vezl na trh vejce. " + clauses(t, "Když je skládal ", { "po ${t.mods[it]}" },
            { k -> when { k == 1L -> "zbylo 1 vejce"; k in 2..4 -> "zbyla $k vejce"; else -> "zbylo $k vajec" } },
            { k -> "do plné řady " + when { k == 1L -> "chybělo 1 vejce"; k in 2..4 -> "chyběla $k vejce"; else -> "chybělo $k vajec" } }, "nezbylo nic") +
            " Vajec bylo nejvýše ${t.hi}. Kolik jich vezl?"
    },
    // soldiers in rows
    Story(listOf(listOf(9, 10, 11), listOf(8, 9, 7), listOf(12, 15, 18), listOf(8, 10, 12), listOf(7, 11, 13), listOf(10, 12, 9)).map { it.map(Int::toLong) }, "RANGE", true, "Počet vojakov") { t ->
        "Velitel řadí vojáky do řad. " + clauses(t, "V řadách ", { "po ${t.mods[it]}" }, { zbude(it, "voják", "vojáci", "vojáků") }, { "chybí v poslední řadě " + cz(it, "voják", "vojáci", "vojáků") }, "nikdo nezbude") +
            " Vojáků je ${t.lo} až ${t.hi}. Kolik jich je?"
    },
    // stairs taken several at a time
    Story(listOf(listOf(4, 5, 6), listOf(3, 5, 7), listOf(6, 7, 8), listOf(5, 8, 9), listOf(4, 9, 5), listOf(6, 10, 7)).map { it.map(Int::toLong) }, "MOST", false, "Počet schodov") { t ->
        "Petr vybíhá schody a bere jich vždy stejně najednou. " + clauses(t, "Když je bere ", { "po ${t.mods[it]}" }, { "na konci " + zbude(it, "schod", "schody", "schodů") }, { "" }, "nic nezbude") +
            " Schodiště má nejvýše ${t.hi} schodů. Kolik jich má?"
    },
    // two comets
    Story(listOf(listOf(8, 10), listOf(6, 10), listOf(9, 12), listOf(7, 10), listOf(12, 15), listOf(10, 14), listOf(11, 13), listOf(14, 21)).map { it.map(Int::toLong) }, "AFTER", false, "Rok") { t ->
        "Kometa A se vrací každých ${t.mods[0]} let a naposledy byla vidět v roce ${t.said[0]}, kometa B každých ${t.mods[1]} let a naposledy v roce ${t.said[1]}. Ve kterém nejbližším roce po roce $YEAR budou vidět obě?"
    },
    // lighthouses blinking
    Story(listOf(listOf(6, 8, 9), listOf(10, 12, 15), listOf(8, 12, 18), listOf(9, 12, 15), listOf(5, 8, 12), listOf(7, 9, 12)).map { it.map(Int::toLong) }, "FIRST", false, "Sekunda") { t ->
        "Tři majáky blikají každých ${t.mods[0]}, ${t.mods[1]} a ${t.mods[2]} sekund. Po zapnutí bliknou poprvé v sekundách ${t.said[0]}, ${t.said[1]} a ${t.said[2]}. Ve které sekundě bliknou poprvé všechny současně?"
    },
    // sweets shared among grandchildren
    Story(listOf(listOf(5, 6, 8), listOf(6, 7, 9), listOf(8, 9, 10), listOf(6, 10, 14), listOf(5, 7, 9), listOf(9, 12, 7)).map { it.map(Int::toLong) }, "RANGE", true, "Počet cukríkov") { t ->
        "Babička dělí bonbony rovným dílem. " + clauses(t, "Při dělení ", { "mezi ${t.mods[it]} vnoučat" }, { "jí " + zbude(it, "bonbon", "bonbony", "bonbonů") }, { "jí do dalšího kola chybí " + cz(it, "bonbon", "bonbony", "bonbonů") }, "jí nic nezbude") +
            " Bonbonů je ${t.lo} až ${t.hi}. Kolik jich je?"
    },
    // coins stacked in columns
    Story(listOf(listOf(5, 9, 12), listOf(8, 10, 15), listOf(10, 12, 25), listOf(9, 10, 16), listOf(6, 16, 9), listOf(8, 15, 7)).map { it.map(Int::toLong) }, "RANGE", true, "Počet mincí") { t ->
        "Jana skládá mince do sloupků. " + clauses(t, "Ve sloupcích ", { "po ${t.mods[it]}" }, { "jí " + zbude(it, "mince", "mince", "mincí") }, { "jí do posledního sloupku chybí " + cz(it, "mince", "mince", "mincí") }, "jí nic nezbude") +
            " Mincí je ${t.lo} až ${t.hi}. Kolik jich má?"
    },
)

/** A story drawn at random, then its numbers: the answer first, the remainders and the window from it. */
fun tcWordTask(r: Random): TcWordTask {
    val si = r.nextInt(Stories.size)
    val st = Stories[si]
    while (true) {
        val mods = st.sets.random(r)
        val l = lcmOf(mods)
        if (l > 1200 || l < 30) continue
        val n: Long; val lo: Long; val hi: Long
        when (st.ask) {
            "MOST" -> { n = r.rl(mods.max() + 1, l - 1); hi = minOf(l - 1, ((n + r.rl(0, l - n - 1)) / 10) * 10).let { if (it < n) n else it }; lo = 1 }
            "AFTER" -> { n = YEAR + r.rl(1, l); lo = YEAR + 1; hi = YEAR + l }
            "FIRST" -> { n = r.rl(1, l - 1); lo = 0; hi = n }
            else -> {
                n = r.rl(maxOf(l, 60), 2 * l + 300)
                val from = n - r.rl(0, l - 1)
                var a = (from / 10) * 10; if (a <= n - l) a += 10
                val b = ((a + l - 1) / 10) * 10
                lo = a; hi = if (b >= n) b else a + l - 1
            }
        }
        val res = mods.map { md(n, it) }
        // the window holds this one solution only (the first second: none before it)
        if (inRange(n, l, lo, hi) != listOf(n) && st.ask != "FIRST") continue
        if (st.ask == "FIRST" && (res.toSet().size < 2 || n != md(n, l))) continue
        val how = mods.indices.map { i ->
            when {
                st.ask == "AFTER" -> TcSaid.YEAR
                st.ask == "FIRST" -> TcSaid.FIRST
                res[i] == 0L -> TcSaid.NONE
                st.missing && r.nextInt(3) == 0 -> TcSaid.MISSING
                else -> TcSaid.LEFT
            }
        }
        if (how.count { it == TcSaid.NONE } > 1) continue
        val said = mods.indices.map { i ->
            when (how[i]) {
                TcSaid.MISSING -> mods[i] - res[i]
                TcSaid.YEAR -> YEAR - md(YEAR - res[i], mods[i])
                TcSaid.NONE -> 0L
                else -> res[i]
            }
        }
        return TcWordTask(si, mods, said, how, n, lo, hi)
    }
}

fun tcWord(t: TcWordTask): Exercise {
    val st = Stories[t.story]
    val mods = t.mods; val res = t.res
    val ps = res.zip(mods)
    val sol = crt(ps)!!
    val l = sol.mod
    val k = mods.size
    val labels = (1..k).map { "r_$it" }
    val ansQ = when (st.ask) {
        "AFTER" -> "Prvý rok po roku §$YEAR§:"
        "FIRST" -> "Prvá sekunda, keď bliknú všetky:"
        "MOST" -> "${st.what}, najviac §${t.hi}§:"
        else -> "${st.what} medzi §${t.lo}§ a §${t.hi}§:"
    }
    return Exercise(
        "dml-tc-slovna", "Slovná úloha na sústavu", "Šablóna Cvičení 6.26 a 6.27; Věty 6.19 a 6.20",
        tx(st.text(t)),
        parts = listOf<(List<Any>) -> Part>(
            { _ -> Part(tx("Zvyšky §r_i§ v §x\\equiv r_i\\pmod{m_i}§ pre §m_i=${mods.joinToString(",\\ ")}§:"), nums(labels, res, "§" + ps.joinToString(",\\ ") { (r, m) -> "x\\equiv $r\\pmod{$m}" } + "§") { g ->
                val i = mods.indices.firstOrNull { g.getOrNull(it) != res[it] } ?: 0
                val v = g.getOrNull(i) ?: Long.MIN_VALUE
                val m = mods[i]
                when {
                    t.how[i] == TcSaid.MISSING && v == t.said[i] -> "„Chýba ${t.said[i]}“ je zvyšok §-${t.said[i]}\\equiv ${res[i]}\\pmod{$m}§. Do plnej skupiny chýba, teda §x+${t.said[i]}§ je násobok §$m§."
                    t.how[i] == TcSaid.YEAR && v == t.said[i] -> "Rok §${t.said[i]}§ zredukuj modulo §$m§: §${t.said[i]}\\equiv ${res[i]}§. Zvyšok má byť menší než modul."
                    v == m && res[i] == 0L -> "„Nič nezostane“ je zvyšok §0§, nie §$m§. Zvyšok je z §\\{0,\\dots,${m - 1}\\}§."
                    v != res[i] && res.indices.any { j -> j != i && res[j] == v } -> "Zvyšky patria k iným modulom. Pri module §$m§ je zvyšok §${res[i]}§."
                    v >= m || v < 0 -> "Zvyšok musí byť z §\\{0,\\dots,${m - 1}\\}§. Pri module §$m§ je to §${res[i]}§."
                    else -> "Pri module §$m§ je zvyšok §${res[i]}§. Prečítaj vetu o skupinách po §$m§ ešte raz."
                }
            }, aim = 0, hint = tx("Skupiny po §m§ a zvyšok §r§ dávajú §x\\equiv r\\pmod{m}§. Ak do plnej skupiny §k§ chýba, je §x\\equiv -k\\equiv m-k§.")) },
            { _ -> Part(tx("Všetky riešenia sústavy, §x\\equiv x_0\\pmod{M}§, §0\\leq x_0<M§:"), nums(listOf("x_0", "M"), listOf(sol.x, l), "§x\\equiv ${sol.x}\\pmod{$l}§") { g -> systemDiag(ps, sol, null, g) },
                aim = 2, hint = tx("Z prvej kongruencie §x=r_1+m_1t§, dosaď do ďalšej a vyrieš pre §t§. Riešenie je jediné modulo §\\lcm§ modulov.")) },
            { _ -> Part(tx(ansQ), nums(listOf("x"), listOf(t.n), "§${t.n}§") { g ->
                val v = g[0]
                val solves = md(v - sol.x, l) == 0L
                when {
                    st.ask == "AFTER" && solves && v <= YEAR -> "Rok §$v§ nie je po roku §$YEAR§. Pripočítavaj §M=$l§, kým neprekročíš §$YEAR§."
                    st.ask == "AFTER" && solves -> "Obe kométy vtedy vidno, ale nie prvýkrát po roku §$YEAR§. Odčítaj §M=$l§."
                    st.ask == "FIRST" && solves -> "Vtedy bliknú všetky, ale nie prvýkrát. Odčítaj §M=$l§."
                    solves && v == sol.x && v !in t.lo..t.hi -> "§$v§ rieši sústavu, ale nie je v rozsahu príbehu. Pripočítavaj §M=$l§, kým doňho nepadneš."
                    solves && v > t.hi -> "Je to riešenie sústavy, ale väčšie než §${t.hi}§. Odčítaj §M=$l§."
                    solves && v < t.lo -> "Je to riešenie sústavy, ale menšie než §${t.lo}§. Pripočítaj §M=$l§."
                    else -> ps.firstOrNull { (r, m) -> md(v - r, m) != 0L }?.let { (r, m) -> "Nesedí so zadaním: §$v\\equiv ${md(v, m)}\\pmod{$m}§, má byť §$r§." } ?: "Riešenia sú §${sol.x}+${l}k§. Vyber to, ktoré spĺňa podmienku príbehu."
                }
            }, aim = 3, hint = tx("Všetky riešenia sú §x_0+kM§, §k\\in\\mathbb{Z}§. Vyber to, ktoré spĺňa podmienku z textu.")) },
        ) + (if (t.story != 0) emptyList() else listOf<(List<Any>) -> Part>({ _ -> Part(tx("Najmenšie zvýšenie výroby, aby nič nezostalo pri žiadnom balení:"), nums(listOf("d"), listOf(md(-t.n, l)), "§${t.n}+${md(-t.n, l)}=${t.n + md(-t.n, l)}=${(t.n + md(-t.n, l)) / l}\\cdot $l§") { g ->
            val v = g[0]
            val clear = mods.filter { m -> md(t.n + v, m) == 0L }
            when {
                v == t.n + md(-t.n, l) -> "To je nová výroba, nie zvýšenie. Zvýšenie je §${t.n + md(-t.n, l)}-${t.n}=${md(-t.n, l)}§."
                clear.isNotEmpty() && clear.size < mods.size -> "Takto nič nezostane len pri balení po §${clear.joinToString(",\\ ")}§. Nová výroba musí byť násobok všetkých, teda §\\lcm=$l§."
                v > 0 && md(t.n + v, l) == 0L -> "Vyhovuje, ale menšie zvýšenie existuje. Najbližší násobok §$l§ nad §${t.n}§ je §${t.n + md(-t.n, l)}§."
                else -> "Nová výroba musí byť násobok každej veľkosti krabice, teda §\\lcm=$l§. Najbližší nad §${t.n}§ je §${t.n + md(-t.n, l)}§."
            }
        }, aim = 3, hint = tx("Nič nezostane, keď je výroba násobok každej veľkosti krabice, teda násobok ich §\\lcm§.")) })),
        postup = TcWordPostup,
        solution = { _ ->
            val (model, exists, subst, answer) = TcWordPostup
            TcWordPostup.solve {
                val notes = mods.indices.mapNotNull { i ->
                    when (t.how[i]) {
                        TcSaid.MISSING -> "do plnej skupiny po §${mods[i]}§ chýba §${t.said[i]}§, teda §x\\equiv -${t.said[i]}\\equiv ${res[i]}§"
                        TcSaid.YEAR -> "§${t.said[i]}=${t.said[i] / mods[i]}\\cdot ${mods[i]}" + (if (res[i] > 0) "+${res[i]}" else "") + "§"
                        TcSaid.NONE -> "nič nezostane pri §${mods[i]}§, teda §x\\equiv 0§"
                        else -> null
                    }
                }
                val says = when (st.ask) {
                    "AFTER" -> "Kometa je viditeľná v rokoch, ktoré dávajú rovnaký zvyšok ako rok jej návratu, lebo sa vracia s periódou."
                    "FIRST" -> "Maják bliká v sekundách, ktoré dávajú po delení jeho periódou zvyšok prvého bliknutia."
                    else -> "Rozdelenie na skupiny po §m§ so zvyškom §r§ znamená §x\\equiv r\\pmod{m}§."
                }
                model(tx(says + (if (notes.isEmpty()) "" else " Tu: " + notes.joinToString("; ") + ".")), tcSystemTex(ps))
                exists(tx(existsSentence(ps, l)))
                subst(tx("Z prvej kongruencie vyjadrím §x§ s parametrom a dosadím do ďalšej, takže dostanem §x§, ktoré spĺňa obe naraz (Příklad 6.20)."), substitution(ps))
                val why = when (st.ask) {
                    "AFTER" -> "Prvý rok po §$YEAR§ je §${t.n}§, lebo predošlé riešenie §${t.n - l}§ je ešte pred ním."
                    "FIRST" -> "Najmenšie nezáporné riešenie je §${t.n}§; skoršie spoločné bliknutie by bolo tiež riešením."
                    "MOST" -> "Kladné riešenia sú §${t.n},\\ ${t.n + l},\\dots§; najviac §${t.hi}§ je len §${t.n}§, lebo ďalšie je o §$l§ väčšie."
                    else -> "V rozsahu §${t.lo}§ až §${t.hi}§ je len §${t.n}§, lebo susedné riešenia sú o §$l§ ďalej."
                }
                val k = (t.n - sol.x) / l
                answer(tx("Všetky riešenia sú §${sol.x}+${l}k§, §k\\in\\mathbb{Z}§. $why"), if (k == 0L) "x=${t.n}" else "x=" + Terms.sum("${sol.x}", Terms.times(k, "$l")) + "=${t.n}")
                if (t.story == 0) answer(tx("Pri žiadnom balení nič nezostane, keď je výroba násobok každej veľkosti krabice, teda ich §\\lcm=$l§. Najbližší násobok nad §${t.n}§ dá najmenšie zvýšenie."),
                    "${t.n + md(-t.n, l)}-${t.n}=${md(-t.n, l)}")
            }
        },
        hint = tx("Každá veta je kongruencia §x\\equiv r\\pmod{m}§. Sústavu vyrieš dosadzovaním a vyber riešenie podľa textu."),
    )
}

/* ---- 2. The solutions of a system in an interval (rozstřel 2026; test 6. 12. 2023 A) --------------------------- */

/** Solutions of a system in an interval (Věty 6.13, 6.17, 6.20). */
val TcIntervalPostup = postup(
    "Každá kongruencia zvlášť" to "Krátenie aj s modulom, potom inverzia: tvar §x\\equiv r\\pmod{n}§, alebo zistíš, že riešenie nie je.",
    "Má sústava riešenie?" to "§\\gcd§ každých dvoch nových modulov musí deliť rozdiel zvyškov.",
    "Riešenie sústavy" to "Dosadzovaním §x\\equiv x_0\\pmod{M}§, kde §M§ je §\\lcm§ nových modulov.",
    "Riešenia v intervale" to "Prvé riešenie §\\geq L§ je §L+((x_0-L)\\bmod M)§; ďalšie sú o §M§ väčšie, kým neprekročia §U§.",
)

data class TcIntervalTask(val cs: List<TcCong>, val lo: Long, val hi: Long)

private val IntervalCoprime = listOf(listOf(5, 7), listOf(4, 9), listOf(7, 8), listOf(5, 12), listOf(3, 5, 7), listOf(4, 5, 3), listOf(9, 10), listOf(8, 3, 5)).map { it.map(Int::toLong) }
private val IntervalShared = listOf(listOf(6, 10), listOf(10, 12), listOf(8, 12), listOf(6, 9, 15), listOf(4, 6, 10), listOf(12, 18), listOf(6, 14), listOf(10, 15), listOf(6, 8, 5)).map { it.map(Int::toLong) }

/** A congruence with the reduced form x ≡ x (mod n): a coefficient sharing [g] with the modulus n·g, or none. */
private fun congFor(r: Random, x: Long, n: Long, g: Long): TcCong {
    while (true) {
        val u = r.rl(1, maxOf(2L, n - 1))
        if (gcd(u, n) != 1L) continue
        val a = g * u; val m = n * g
        if (gcd(a, m) != g) continue
        return TcCong(a, md(a * x, m), m)
    }
}

fun tcIntervalTask(r: Random): TcIntervalTask {
    while (true) {
        val ns = (if (r.nextBoolean()) IntervalCoprime else IntervalShared).random(r).shuffled(r)
        val l = lcmOf(ns)
        if (l > 200) continue
        val x = r.rl(0, l - 1)
        val gs = ns.map { n -> if (r.nextInt(3) == 0 && n <= 15) listOf(2L, 3L).random(r) else 1L }
        val cs = ns.indices.map { i -> congFor(r, x, ns[i], gs[i]) }.toMutableList()
        // about one in four has no solution: one congruence alone (gcd does not divide the right side) or two that clash
        when (r.nextInt(8)) {
            0 -> gs.indexOfFirst { it > 1 }.takeIf { it >= 0 }?.let { i -> val c = cs[i]; cs[i] = c.copy(b = md(c.b + r.rl(1, gs[i] - 1), c.m)) }
            1 -> if (ns.any { a -> ns.any { b -> a != b && gcd(a, b) > 1 } }) {
                val i = ns.indices.first { i -> ns.indices.any { j -> j != i && gcd(ns[i], ns[j]) > 1 } }
                val c = cs[i]; cs[i] = c.copy(b = md(c.b + gs[i] * r.rl(1, 2), c.m))
            }
        }
        if (cs.any { it.m > 60 }) continue
        // the interval: from 0 or a round number, as long as about 0 to 3 periods
        val lo = if (r.nextInt(3) == 0) 10L * r.rl(1, 20) else 0L
        val hi = lo + listOf(l / 2, l, l + l / 2, 2 * l, 3 * l - 1).random(r).let { ((it + 9) / 10) * 10 }.coerceAtLeast(20)
        if (hi > 600) continue
        return TcIntervalTask(cs, lo, hi)
    }
}

fun tcInterval(task: TcIntervalTask): Exercise {
    val (cs, lo, hi) = task
    val reduced = cs.map { it.solved }
    val bad1 = reduced.indexOfFirst { it == null }
    val ps = reduced.filterNotNull().map { it.x0 to it.n }
    val pair = if (bad1 < 0) clash(ps) else null
    val sol = if (bad1 < 0) crt(ps) else null
    val sys = "\\begin{matrix} " + cs.joinToString(" \\\\ ") { it.tex } + " \\end{matrix}"
    val parts = ArrayList<(List<Any>) -> Part>()
    val noWhy = when {
        bad1 >= 0 -> cs[bad1].let { c -> "§${c.tex}§ nemá riešenie, lebo §\\gcd(${c.a},${c.m})=${gcd(c.a, c.m)}§ nedelí §${c.b}§ (Věta 6.17)." }
        pair != null -> pair.let { (i, j) -> "Nie: §\\gcd(${ps[i].second},${ps[j].second})=${gcd(ps[i].second, ps[j].second)}§ nedelí §${ps[i].first}-${p(ps[j].first)}§ (Věta 6.20)." }
        else -> ""
    }
    parts += { _ -> Part(tx("Má sústava riešenie?"), yesNo(sol != null,
        "Áno: každá kongruencia má riešenie a nové moduly si neodporujú (Věta 6.20).",
        if (bad1 >= 0) "Nie: jedna kongruencia sama nemá riešenie, preto ani sústava. " + noWhy else noWhy,
        if (sol != null) "" else if (bad1 >= 0) cs[bad1].let { c -> "Zabudol si overiť každú kongruenciu zvlášť. §${c.tex}§: §\\gcd(${c.a},${c.m})=${gcd(c.a, c.m)}§ nedelí §${c.b}§, riešenie nie je (Věta 6.17)." }
        else pair!!.let { (i, j) -> "Po krátení si dve kongruencie odporujú. §\\gcd(${ps[i].second},${ps[j].second})=${gcd(ps[i].second, ps[j].second)}§ nedelí §${ps[i].first}-${p(ps[j].first)}§ (Věta 6.20)." },
        "Riešenie existuje: každá kongruencia ho má a nové moduly si neodporujú. Over §\\gcd§ až po krátení."),
        aim = 1, hint = if (sol != null) tx("Najprv každú kongruenciu zvlášť: §\\gcd§ koeficientu a modulu musí deliť pravú stranu. Potom porovnaj nové zvyšky.") else null) }
    if (sol != null) {
        val l = sol.mod
        val inside = inRange(sol.x, l, lo, hi)
        val origL = lcmOf(cs.map { it.m })
        parts += { _ -> Part(tx("Všetky riešenia, §x\\equiv x_0\\pmod{M}§, §0\\leq x_0<M§:"), nums(listOf("x_0", "M"), listOf(sol.x, l), "§x\\equiv ${sol.x}\\pmod{$l}§") { g -> systemDiag(ps, sol, cs.map { it.m }, g) },
            aim = 2, hint = tx("Každú kongruenciu zvlášť na tvar §x\\equiv r\\pmod{n}§, potom dosadzovanie. §M§ je §\\lcm§ nových modulov.")) }
        parts += { _ -> Part(tx("Koľko riešení leží v §\\langle $lo,$hi\\rangle§?"), nums(listOf("\\#"), listOf(inside.size.toLong()), "§" + (if (inside.isEmpty()) "0" else "${inside.size}" + ":\\ " + inside.joinToString(",\\ ")) + "§") { g ->
            val v = g[0]
            val prod = ps.fold(1L) { a, b -> a * b.second }
            when {
                origL != l && v == inRange(sol.x, origL, lo, hi).size.toLong() -> "Počítal si s krokom §$origL§ pôvodných modulov. Po krátení je krok §\\lcm=$l§ (Věta 6.13)."
                prod != l && v == inRange(sol.x, prod, lo, hi).size.toLong() -> "Počítal si s krokom súčinu §$prod§. Riešenia sa opakujú s krokom §\\lcm=$l§."
                v == inside.size + 1L || v == inside.size - 1L -> "Over krajné body. " + (if (inside.isEmpty()) "Prvé riešenie od §$lo§ je §${lo + md(sol.x - lo, l)}§, to je už za §$hi§." else "Prvé riešenie od §$lo§ je §${inside.first()}§, posledné do §$hi§ je §${inside.last()}§.")
                else -> "Riešenia sú §${sol.x}+${l}k§. V §\\langle $lo,$hi\\rangle§ ich je §${inside.size}§."
            }
        }, aim = 3, hint = tx("Riešenia sú §x_0+kM§. Nájdi prvé, ktoré je aspoň §$lo§, a pripočítavaj §M§, kým nepresiahneš §$hi§.")) }
        if (inside.isNotEmpty()) parts += { _ -> Part(tx("Najmenšie riešenie v §\\langle $lo,$hi\\rangle§:"), nums(listOf("x"), listOf(inside.first()), "§${inside.first()}§") { g ->
            val v = g[0]
            val solves = md(v - sol.x, l) == 0L
            when {
                solves && v == sol.x && v < lo -> "§${sol.x}§ je zvyšok, ale leží mimo intervalu. Pripočítaj §M=$l§, kým nebudeš aspoň na §$lo§."
                solves && v < lo -> "Je to riešenie, ale menšie než §$lo§. Pripočítaj §M=$l§."
                solves && v > hi -> "Je to riešenie, ale za §$hi§."
                solves -> "Je to riešenie v intervale, ale nie najmenšie. Odčítaj §M=$l§: §${v - l}§ je ešte aspoň §$lo§."
                else -> ps.firstOrNull { (r, m) -> md(v - r, m) != 0L }?.let { (r, m) -> "§$v§ nie je riešenie: §$v\\equiv ${md(v, m)}\\pmod{$m}§, má byť §$r§." } ?: "Riešenia sú §${sol.x}+${l}k§."
            }
        }, aim = 3, hint = tx("Prvé riešenie aspoň §$lo§ dostaneš, keď k §x_0§ pripočítaš (alebo odčítaš) správny násobok §M§.")) }
    }
    return Exercise(
        "dml-tc-interval", "Sústava a riešenia v intervale", "Šablóna rozstrelu 29. 1. 2026 a testu 6. 12. 2023 (variant A); Věty 6.13, 6.17, 6.20",
        tx("Najděte všechna řešení soustavy kongruencí v intervalu §\\langle $lo,$hi\\rangle§: §$sys§. Má soustava v tomto intervalu právě jedno řešení?"),
        parts = parts,
        postup = TcIntervalPostup,
        solution = { _ ->
            val (each, exists, system, interval) = TcIntervalPostup
            TcIntervalPostup.solve {
                each(tx("Každú kongruenciu vyriešim zvlášť, lebo sústava platí, len keď platí každá. Krátim aj modul, takže úprava je ekvivalentná (Věta 6.13)."),
                    cs.mapNotNull(::reduceFormula).ifEmpty { listOf(disp(sys)) })
                if (bad1 >= 0) {
                    exists(tx(noWhy.replaceFirst(Regex("^Nie: "), "") + " Sústava preto nemá riešenie."))
                    interval(tx("Sústava nemá žiadne riešenie, preto nemá ani žiadne v §\\langle $lo,$hi\\rangle§."))
                    return@solve
                }
                if (pair != null) {
                    val (i, j) = pair
                    exists(tx("Sústava nemá riešenie, lebo §\\gcd(${ps[i].second},${ps[j].second})=${gcd(ps[i].second, ps[j].second)}§ nedelí §${ps[i].first}-${p(ps[j].first)}§. Rozdiel zvyškov by pritom musel byť jeho násobkom (Věta 6.20)."))
                    interval(tx("Sústava nemá žiadne riešenie, preto nemá ani žiadne v §\\langle $lo,$hi\\rangle§."))
                    return@solve
                }
                val l = sol!!.mod
                exists(tx(existsSentence(ps, l)))
                if (ps.size > 1) system(tx("Dosadzujem z prvej kongruencie do ďalších, takže dostanem všetky §x§, ktoré spĺňajú všetky naraz (Příklad 6.20)."), substitution(ps))
                system(tx("Všetky riešenia sú §x\\equiv ${sol.x}\\pmod{$l}§."))
                val inside = inRange(sol.x, l, lo, hi)
                val first = lo + md(sol.x - lo, l)
                interval(tx(when (inside.size) {
                    0 -> "Prvé riešenie aspoň §$lo§ je §$first§, a to je už za §$hi§. V intervale preto nie je žiadne riešenie."
                    1 -> "Prvé riešenie aspoň §$lo§ je §$first§ a ďalšie §${first + l}§ je už za §$hi§. V intervale je preto práve jedno riešenie."
                    else -> "Prvé riešenie aspoň §$lo§ je §$first§, ďalšie sú o §$l§ väčšie. V intervale ich je §${inside.size}§, teda nie práve jedno."
                }), "\\langle $lo,$hi\\rangle\\cap\\{${sol.x}+${l}k\\}=\\{" + inside.joinToString(",\\ ") + "\\}")
            }
        },
        hint = tx("Najprv každú kongruenciu zvlášť (krátiš aj modul), potom sústavu. Riešenia §x_0+kM§ v intervale spočítaj."),
    )
}

/* ---- 3. Why there are no other solutions, on paper (Cvičení 6.32) ------------------------------------------------ */

/** All solutions of a system and the reason there are no others (Cvičení 6.32; Věty 6.13 a 6.20). */
val TcOnlyPostup = postup(
    "Každá kongruencia zvlášť" to "Ekvivalentnými úpravami na §x\\equiv r\\pmod{n}§: krátenie aj s modulom, inverzia.",
    "Riešenie sústavy" to "Dosadzovaním §x\\equiv x_0\\pmod{M}§, kde §M=\\lcm§ nových modulov.",
    "Iné riešenia nie sú" to "Každý nový modul delí rozdiel dvoch riešení, teda ho delí aj ich §\\lcm§; a naopak každé §x_0+kM§ sústavu spĺňa.",
)

data class TcOnlyTask(val cs: List<TcCong>)

fun tcOnlyTask(r: Random): TcOnlyTask {
    while (true) {
        val ns = (if (r.nextInt(3) == 0) IntervalCoprime else IntervalShared + listOf(listOf(10L, 36L, 60L), listOf(65L, 91L), listOf(12L, 20L, 15L))).random(r).shuffled(r)
        val l = lcmOf(ns)
        if (l > 500) continue
        val x = r.rl(0, l - 1)
        // at least one congruence needs cancelling with the modulus, so the uniqueness modulus is not the given moduli's lcm
        val gs = ns.map { n -> if (r.nextInt(2) == 0) listOf(2L, 3L, 5L).filter { n * it <= 200 }.randomOrNull(r) ?: 1L else 1L }
        if (gs.all { it == 1L }) continue
        val cs = ns.indices.map { i -> congFor(r, x, ns[i], gs[i]) }
        if (cs.any { it.m > 200 }) continue
        return TcOnlyTask(cs)
    }
}

fun tcOnly(task: TcOnlyTask): Exercise {
    val cs = task.cs
    val ps = cs.map { it.solved!! }.map { it.x0 to it.n }
    val sol = crt(ps)!!
    val l = sol.mod
    val ns = ps.map { it.second }
    val sys = "\\begin{matrix} " + cs.joinToString(" \\\\ ") { it.tex } + " \\end{matrix}"
    val nsTex = ns.joinToString(",")
    val origL = lcmOf(cs.map { it.m })
    val sample = listOf(
        "Každú kongruenciu som nahradil ekvivalentnou: krátenie aj s modulom a násobenie inverziou sa dajú vrátiť. Sústava má teda presne tie isté riešenia ako §" + ps.joinToString(",\\ ") { (r, n) -> "x\\equiv $r\\pmod{$n}" } + "§.",
        "Nech §x§ a §y§ sú dve riešenia. Odčítaním kongruencií s rovnakou pravou stranou dostanem §x\\equiv y§ modulo §$nsTex§, teda tieto moduly delia §x-y§.",
        "Spoločný násobok čísel §$nsTex§ je násobkom ich §\\lcm=$l§, preto §$l\\mid x-y§ a každé riešenie je §${sol.x}+${l}k§.",
        "Naopak každé §${sol.x}+${l}k§ sústavu spĺňa, lebo §$l§ je násobok každého z modulov §$nsTex§. Iné riešenia preto nie sú (Věta 6.20).",
    )
    val look = listOf(
        "Úpravy sú ekvivalentné (krátenie aj s modulom, inverzia), riešenia sa nestratia ani nepribudnú",
        "Každý z modulov §$nsTex§ delí rozdiel dvoch riešení",
        "Preto rozdiel delí aj §\\lcm=$l§, nie nutne súčin ani §\\lcm§ pôvodných modulov",
        "Naopak každé §${sol.x}+${l}k§ je riešenie",
    )
    return Exercise(
        "dml-tc-jedine", "Prečo iné riešenia nie sú", "Šablóna Cvičení 6.32; Věty 6.13 a 6.20",
        tx("Nalezněte všechna celočíselná řešení následující soustavy kongruencí: §$sys§. S využitím teorie vysvětlete, proč žádná jiná řešení neexistují."),
        parts = listOf(
            { _ -> Part(tx("Všetky riešenia, §x\\equiv x_0\\pmod{M}§, §0\\leq x_0<M§:"), nums(listOf("x_0", "M"), listOf(sol.x, l), "§x\\equiv ${sol.x}\\pmod{$l}§") { g -> systemDiag(ps, sol, cs.map { it.m }, g) },
                aim = 1, hint = tx("Každú kongruenciu zvlášť na tvar §x\\equiv r\\pmod{n}§ (krátiš aj modul), potom dosadzovanie. §M§ je §\\lcm§ nových modulov.")) },
            { _ -> Part(tx("Na papier: prečo iné riešenia ako §${sol.x}+${l}k§ nie sú?"), Answer.Paper(sample.map(::tx), look.map(::tx),
                tx("Zdôvodnenie musí povedať, prečo §\\lcm=$l§ delí rozdiel dvoch riešení. " +
                    "Každý z modulov §$nsTex§ delí rozdiel, preto ho delí aj ich §\\lcm§; a naopak každé §${sol.x}+${l}k§ je riešenie." +
                    (if (origL != l) " Pozor, modul §$origL§ pôvodných kongruencií to nie je: po krátení sa moduly zmenšili." else ""))),
                aim = 2, hint = tx("Vezmi dve riešenia §x,y§. Ktoré čísla delia ich rozdiel? Prečo potom aj §\\lcm§? A je každé §x_0+kM§ riešením?")) },
        ),
        postup = TcOnlyPostup,
        solution = { _ ->
            val (each, system, only) = TcOnlyPostup
            TcOnlyPostup.solve {
                each(tx("Každú kongruenciu nahradím ekvivalentnou, lebo krátenie aj s modulom (Věta 6.13) aj násobenie inverziou sa dajú vrátiť."), cs.mapNotNull(::reduceFormula).ifEmpty { listOf(disp(sys)) })
                system(tx(existsSentence(ps, l)), substitution(ps) + disp("x\\equiv ${sol.x}\\pmod{$l}"))
                only(tx("Dve riešenia §x,y§ dávajú §x\\equiv y§ modulo každého z §$nsTex§, teda každý z nich delí rozdiel §x-y§. Preto ho delí aj §\\lcm=$l§."), "$l\\mid x-y\\ \\Rightarrow\\ x\\in\\{${sol.x}+${l}k: k\\in\\mathbb{Z}\\}")
                only(tx("Naopak každé §${sol.x}+${l}k§ sústavu spĺňa, lebo §$l§ je násobok každého modulu. Iné riešenia preto nie sú (Věta 6.20)."))
            }
        },
        hint = tx("Najprv riešenie §x\\equiv x_0\\pmod{M}§. Zdôvodnenie: všetky moduly delia rozdiel dvoch riešení, teda ho delí aj §\\lcm§."),
    )
}

/* ---- 4. The inverse through Euler's theorem (Tvrzení 6.7, 6.11; Spěvák, 8. prednáška) -------------------------- */

/** The inverse as a power, a^(φ(m)−1) (Tvrzení 6.11; for a prime m Tvrzení 6.7). */
val TcInvEulerPostup = postup(
    "Smie sa použiť Eulerova veta?" to "Len pri §\\gcd(a,m)=1§; inak inverzia neexistuje.",
    "§\\varphi(m)§" to "Rozlož §m§ na prvočísla; §\\varphi(q^k)=q^k-q^{k-1}§ a súčin len pre nesúdeliteľné činitele.",
    "Inverzia ako mocnina" to "§a\\cdot a^{\\varphi(m)-1}=a^{\\varphi(m)}\\equiv 1§, teda §a^{-1}\\equiv a^{\\varphi(m)-1}§; násob a redukuj.",
    "Skúška" to "§a\\cdot a^{-1}§ musí dať zvyšok §1§.",
)

data class TcInvEulerTask(val a: Long, val m: Long)

/** Moduli with φ(m) ≤ 8, so the power left is at most [TcSmallPower] factors, as Spěvák's 7^5 in Z_9. */
private val EulerModuli = listOf<Long>(5, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 24, 30)

fun tcInvEulerTask(r: Random): TcInvEulerTask {
    while (true) {
        val m = EulerModuli.random(r)
        val a0 = r.rl(2, m - 1)
        val coprime = gcd(a0, m) == 1L
        if (!coprime && r.nextInt(4) != 0) continue
        if (coprime && inverse(a0, m) == a0 && r.nextBoolean()) continue
        val a = if (r.nextInt(3) == 0) a0 + m * r.rl(1, 6) else a0
        return TcInvEulerTask(a, m)
    }
}

/** φ as a student gets it by a wrong rule, with the sentence that names the rule (none equal to the right φ). */
private fun phiTraps(m: Long): List<Pair<Long, String>> {
    val f = factor(m)
    val out = ArrayList<Pair<Long, String>>()
    if (f.size > 1 || f[0].second > 1) out += (m - 1) to "§$m§ nie je prvočíslo, §\\varphi($m)\\neq ${m - 1}§. Rozlož §$m=${tcFactorTex(m)}§ (Věta 6.16)."
    for (d in 2 until m) if (m % d == 0L) {
        val e = m / d
        if (d <= e && gcd(d, e) > 1) out += (phi(d) * phi(e)) to "§\\varphi§ je multiplikatívna len pre nesúdeliteľné činitele. §$m=$d\\cdot $e§, ale §\\gcd($d,$e)=${gcd(d, e)}§ (Tvrzení 6.10)."
    }
    if (f.any { it.second > 1 }) {
        val wrong = f.fold(1L) { acc, (q, k) -> acc * (if (k > 1) pow(q, k) - 1 else q - 1) }
        out += wrong to "§\\varphi(q^k)=q^k-q^{k-1}§, nie §q^k-1§: mocnina prvočísla nie je prvočíslo (Tvrzení 6.9)."
        val wrong2 = f.fold(1L) { acc, (q, k) -> acc * pow(q - 1, k) }
        out += wrong2 to "§\\varphi(q^k)=q^k-q^{k-1}§, nie §(q-1)^k§ (Tvrzení 6.9)."
    }
    out += m to "§\\varphi(m)§ počíta len čísla nesúdeliteľné s §m§, nie všetky (Definice 6.10)."
    return out.filter { it.first != phi(m) }
}

fun tcInvEuler(task: TcInvEulerTask): Exercise {
    val (a, m) = task
    val g = gcd(a, m)
    val ph = phi(m)
    val e = ph - 1
    val aa = md(a, m)
    val inv = if (g == 1L) powMod(a, e, m) else null
    check(inv == null || inverse(a, m) == inv)
    val prime = isPrime(m)
    val traps = phiTraps(m)
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("Existuje §$a^{-1}§ v §\\mathbb{Z}_{$m}§?"), yesNo(g == 1L,
        "Áno: §\\gcd($a,$m)=1§, preto platí Eulerova veta (Věta 6.15).",
        "Nie: §\\gcd($a,$m)=$g\\neq 1§, inverzia neexistuje (Věta 6.12).",
        "Eulerova veta tu neplatí, lebo §\\gcd($a,$m)=$g§. Každá mocnina §$a§ je násobok §$g§, nikdy §\\equiv 1§ (Věta 6.12).",
        "§\\gcd($a,$m)=1§, preto inverzia existuje. Netreba, aby §$m§ bolo prvočíslo (Věta 6.15)."),
        aim = 0, hint = if (g == 1L) tx("Eulerova veta platí len pre §\\gcd(a,m)=1§; vtedy inverzia existuje.") else null) }
    if (g == 1L) {
        parts += { _ -> Part(tx("§\\varphi($m)§:"), nums(listOf("\\varphi"), listOf(ph), "§${tcPhiTex(m)}§") { gv ->
            traps.firstOrNull { it.first == gv[0] }?.second ?: "§\\varphi($m)=$ph§ podľa rozkladu §$m=${tcFactorTex(m)}§ (Věta 6.16)."
        }, aim = 1, hint = tx("Rozlož §m§ na prvočísla. §\\varphi(q^k)=q^k-q^{k-1}§ a súčin smieš brať len pre nesúdeliteľné činitele.")) }
        parts += { _ -> Part(tx("§$a^{-1}\\equiv $a^{e}\\pmod{$m}§, kde §e=§"), nums(listOf("e"), listOf(e), "§e=\\varphi($m)-1=$e§") { gv ->
            when (gv[0]) {
                ph -> "§$a^{$ph}\\equiv 1§ je jednotka, nie inverzia. Inverzia je o jednu mocninu menej: §a\\cdot a^{\\varphi-1}=a^{\\varphi}\\equiv 1§."
                m - 2 -> "§m-2§ platí len pre prvočíselný modul (Tvrzení 6.7). Tu je exponent §\\varphi($m)-1=$e§ (Tvrzení 6.11)."
                m - 1 -> "Exponent nie je §m-1§. Inverzia je §a^{\\varphi(m)-1}§, tu §a^{$e}§ (Tvrzení 6.11)."
                -1L -> "§a^{-1}§ chceme vyjadriť kladnou mocninou. Z §a^{\\varphi}\\equiv 1§ je to §a^{\\varphi-1}§."
                else -> "Inverzia je §a^{\\varphi(m)-1}§, lebo §a\\cdot a^{\\varphi(m)-1}=a^{\\varphi(m)}\\equiv 1§. Tu §e=$e§."
            }
        }, aim = 2, hint = tx("Eulerova veta: §a^{\\varphi(m)}\\equiv 1§. Ktorá mocnina vynásobená §a§ dá §a^{\\varphi(m)}§?")) }
        parts += { _ -> Part(tx("§$a^{-1}§ v §\\mathbb{Z}_{$m}§:"), nums(listOf("a^{-1}"), listOf(inv!!), "§$a^{-1}\\equiv ${Terms.pow(aa, e.toInt())}\\equiv $inv§") { gv ->
            val v = gv[0]
            val trapPh = traps.firstOrNull { (w, _) -> w > 1 && powMod(a, w - 1, m) == v && powMod(a, w - 1, m) != inv }
            when {
                v == 1L -> "§1§ je §a^{\\varphi(m)}§, nie inverzia. Inverzia je o jednu mocninu menej, §${Terms.pow(aa, e.toInt())}§."
                !prime && v == powMod(a, m - 2, m) -> "Počítal si §a^{m-2}§ ako pri prvočísle. §$m§ nie je prvočíslo, exponent je §\\varphi($m)-1=$e§."
                trapPh != null -> "Mocnina s nesprávnym §\\varphi=${trapPh.first}§. Správne §\\varphi($m)=$ph§, exponent §$e§."
                v == md(-inv, m) -> "Opačné znamienko: §$v\\equiv -$inv§. Prepočítaj súčiny po krokoch."
                v !in 0 until m -> "Odpoveď má byť z §\\mathbb{Z}_{$m}=\\{0,\\dots,${m - 1}\\}§. Zredukuj ju."
                else -> "Skúška nesedí: §$aa\\cdot $v=${aa * v}\\equiv ${md(aa * v, m)}\\pmod{$m}§. Medzivýsledky: §${stepChain(a, e, m)}§."
            }
        }, aim = 2, hint = tx("Základ zredukuj modulo §m§ a umocni na §\\varphi(m)-1§: násob základom a po každom kroku redukuj.")) }
    }
    return Exercise(
        "dml-tc-inv-euler", "Inverzia cez Eulerovu vetu", "Šablóna príkladov skrípt o inverzii cez MFV a Eulerovu vetu; Tvrzení 6.7 a 6.11",
        tx("Pomocí Eulerovy věty najděte multiplikativní inverzi čísla §$a§ v §\\mathbb{Z}_{$m}§, pokud existuje."),
        parts = parts,
        postup = TcInvEulerPostup,
        solution = { _ ->
            val (may, phiAim, power, check) = TcInvEulerPostup
            TcInvEulerPostup.solve {
                if (g != 1L) {
                    may(tx("§\\gcd($a,$m)=$g\\neq 1§, preto Eulerova veta neplatí a inverzia neexistuje: §$a\\cdot x§ je vždy násobok §$g§, a §1§ nie je (Věta 6.12)."))
                    check(tx("Prvok §$a§ v §\\mathbb{Z}_{$m}§ inverziu nemá."))
                    return@solve
                }
                may(tx("§\\gcd($a,$m)=1§, preto platí Eulerova veta: §${Terms.pow(aa, ph.toInt())}\\equiv 1\\pmod{$m}§ (Věta 6.15)."))
                phiAim(tx(if (prime) "§$m§ je prvočíslo, preto §\\varphi($m)=${m - 1}§ (Tvrzení 6.8)." else "§\\varphi§ počítam z rozkladu, lebo je multiplikatívna pre nesúdeliteľné činitele (Věta 6.16)."), tcPhiDisp(m))
                val steps = ArrayList<Formula>()
                if (a >= m) steps += disp("$a\\equiv $aa\\pmod{$m}", "Základ najprv zredukujem.")
                steps += disp("$aa\\cdot ${Terms.pow(aa, e.toInt())}=${Terms.pow(aa, ph.toInt())}\\equiv 1\\ \\Rightarrow\\ $aa^{-1}\\equiv ${Terms.pow(aa, e.toInt())}\\pmod{$m}")
                if (e >= 2) steps += chainFormula(aa, e, m)
                power(tx("Z §a^{\\varphi(m)}\\equiv 1§ je §a\\cdot a^{\\varphi(m)-1}\\equiv 1§, preto je inverzia mocnina §a^{\\varphi(m)-1}§ (Tvrzení 6.11)."), steps)
                check(tx("Súčin s inverziou musí dať zvyšok §1§."), "$aa\\cdot $inv=${aa * inv!!}" + (if (aa * inv >= m) "=${(aa * inv) / m}\\cdot $m+1" else "") + "\\equiv 1\\pmod{$m}")
            }
        },
        hint = tx("§\\gcd(a,m)=1§? Potom §a^{-1}\\equiv a^{\\varphi(m)-1}§: spočítaj §\\varphi(m)§ z rozkladu a mocninu po krokoch."),
    )
}

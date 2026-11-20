package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * Exercises of kapitola 3 with new numbers. Numbers are drawn, everything asked and checked is computed by
 * Chapter3Math.kt (exact rationals, Z_p). Shapes follow the exam (skúška 17. 12. 2025, 26. 1. 2026, 2. 2. 2026) and the
 * textbook's examples.
 */

internal fun fieldName(f: Field) = if (f is Field.Zp) "\\mathbb{Z}_{${f.p}}" else "\\mathbb{R}"
internal fun matSpace(f: Field, m: Int, n: Int) = "${fieldName(f)}^{$m,$n}"
internal fun mtex(a: Mat) = a.tex()

/** GEM of a parametric matrix as formulas, the row operations beside each step. */
internal fun pLines(start: PMat, steps: List<PStep>): List<Formula> = listOf(Formula(GemWalk.of(start, steps)))

/** Steps [from, to) of a GEM chain from [start]; a later part starts from the matrix it continues. */
internal fun chainLines(start: Mat, steps: List<GemStep>, from: Int = 0, to: Int = steps.size, pole: Field? = null): List<Formula> = listOf(Formula(GemWalk.of(start, steps, from, to, pole)))

internal fun jordanLines(j: Jordan): List<Formula> = chainLines(j.start, j.steps, pole = j.pole)

/** A random matrix of the given rank: [r] independent rows, the rest their small combinations, shuffled. */
internal fun Random.matrixOfRank(f: Field, m: Int, n: Int, r: Int, lo: Int = -3, hi: Int = 4, bound: Int = 9): Mat {
    while (true) {
        val base = List(r) { f.vec(List(n) { pick(lo, hi) }) }
        if (f.rank(base) != r) continue
        val rest = List(m - r) { f.combo(List(r) { pick(-2, 2).q() }, base) }
        if (rest.any { it.isZero() }) continue
        val all = (base + rest).shuffled(this)
        if (f is Field.R && all.any { v -> v.any { kotlin.math.abs(it.num) > bound } }) continue
        return Mat(all)
    }
}

private fun nums(a: Mat) = a.rows.flatten()
private fun fromNums(xs: List<Frac>, n: Int) = Mat(xs.chunked(n))

/** A wrong matrix for A: check A·X against E and name the first entry that fails. */
internal fun inverseDiagnosis(f: Field, a: Mat, inv: Mat): (List<Frac>) -> String {
    val n = a.n
    return diagnosis(f, nums(inv), listOf(
        nums(inv.transpose()) to "Vyšla ti transponovaná inverzia. Skontroluj, či si bloky neprepisoval po stĺpcoch.",
        nums(a) to "Toto je zadaná matica §\\mathbf{A}§. Inverzia je pravý blok až po úprave ľavého na §\\mathbf{E}§.",
        nums(Mat(inv.rows.map { r -> r.map { f.neg(it) } })) to "Vyšla ti inverzia s opačnými znamienkami. Skontroluj znamienka pri pripočítavaní násobkov.",
    )) { given ->
        val x = fromNums(given.map { f.norm(it) }, n)
        val p = f.times(a, x)
        val (i, j) = (0 until n).flatMap { i -> (0 until n).map { i to it } }.first { (i, j) -> p[i, j] != (if (i == j) Frac.ONE else Frac.ZERO) }
        "Skúška nesedí: §(\\mathbf{A}\\mathbf{X})_{${i + 1}${j + 1}}=${p[i, j].tex(true)}§, má byť §${if (i == j) 1 else 0}§. Vynásob §${i + 1}§. riadok §\\mathbf{A}§ tvojím §${j + 1}§. stĺpcom a nájdi chybný krok."
    }
}

internal fun rankDiagnosis(m: Int, n: Int, r: Int): (List<Frac>) -> String = diagnosis(Field.R, listOf(r.q()), listOf(
    listOf(m.q()) to "Hodnosť nie je počet riadkov. Riadky sú LZ: po GEM vznikne nulový riadok.",
    listOf(n.q()) to "Hodnosť nie je počet stĺpcov. Navyše §h(\\mathbf{A})\\leq\\min\\{m,n\\}§ (Důsledek 3.13).",
)) { given ->
    val g = given[0]
    if (g > minOf(m, n).q()) "Viac než §${minOf(m, n)}§ nejde: §h(\\mathbf{A})\\leq\\min\\{$m,$n\\}§ (Důsledek 3.13)."
    else if (g < r.q()) "Málo: niektorý nenulový riadok si asi vynuloval chybou. Skontroluj každý krok GEM dosadením."
    else "Priveľa: v HST ti ostal nenulový riadok, ktorý mal vyjsť nulový. Skontroluj posledný krok GEM."
}

/* ---- 1. rank by GEM ------------------------------------------------------------------------------------------- */

fun k3RankExercise(r: Random): Exercise {
    val f: Field = listOf(Field.R, Field.R, Field.Zp(5), Field.Zp(7), Field.Zp(3)).random(r)
    val m = r.pick(3, 4); val n = r.pick(3, 5).coerceAtLeast(m - 1)
    val rank = r.pick(2, minOf(m, n) - (if (r.nextInt(4) == 0) 0 else 1)).coerceAtLeast(1)
    val a = r.matrixOfRank(f, m, n, rank, lo = if (f is Field.Zp) 0 else -2, hi = if (f is Field.Zp) f.p - 1 else 3)
    val e = f.echelon(a)
    val ln = rank == m
    return Exercise(
        "la1-k3-rank", "Hodnosť matice", "Šablóna podľa Príkladu 3.8 a skúšky 26. 1. 2026, príklad 2A",
        "Spočtěte hodnost matice $\\mathbf{A}=${a.tex()}\\in ${matSpace(f, m, n)}$.",
        parts = listOf(
            { _ -> Part("§h(\\mathbf{A})=§".replace('§', '$'), Answer.Numbers(listOf("h(\\mathbf{A})"), null, listOf(rank.q()), "\$h(\\mathbf{A})=$rank$", diagnose = rankDiagnosis(m, n, rank)),
                hint = "Uprav maticu GEM na HST a spočítaj nenulové riadky. Úpravy GEM hodnosť nemenia.", aim = 0) },
            { _ -> Part("Je súbor riadkov §\\mathbf{A}§ LN?".replace('§', '$'), Answer.Choice(listOf("Áno", "Nie"), if (ln) 0 else 1,
                if (ln) "Áno: hodnosť sa rovná počtu riadkov, takže obal riadkov má dimenziu $m." else "Nie: $m riadky generujú obal dimenzie len $rank.",
                wrong = if (ln) mapOf(1 to "Hodnosť $rank sa rovná počtu riadkov $m. Riadky preto generujú podpriestor dimenzie $m, a $m vektorov v ňom je LN (Věta 2.55).")
                else mapOf(0 to "Riadky sú $m, ale ich obal má dimenziu len $rank. Súbor dlhší než dimenzia obalu je LZ.")),
                hint = "Riadky sú LN práve vtedy, keď sa hodnosť rovná ich počtu.", aim = 2) },
        ),
        postup = K3RankPostup,
        solution = {
            val (gem, count, rows) = K3RankPostup
            K3RankPostup.solve {
                gem(Reasons.rankGem + " Maticu preto upravím na HST" + (if (f is Field.Zp) " a počítam modulo §${f.p}§." else "."), gemLines(e))
                count(Reasons.rankHst + " Tu sú §$rank§, teda §h(\\mathbf{A})=$rank§.")
                rows(if (ln) "Hodnosť §$rank§ sa rovná počtu riadkov, riadky generujú podpriestor dimenzie §$m§ a sú LN."
                    else "Riadkov je §$m§, ale ich obal má dimenziu len §$rank§, takže sú LZ.")
            }
        },
        hint = "GEM do HST a spočítaj nenulové riadky. Hodnosť sa GEM-om nemení.",
    )
}

val K3RankPostup = postup(
    "Uprav na HST" to "Úpravy GEM nemenia hodnosť; uprav maticu na horný stupňovitý tvar.",
    "Spočítaj nenulové riadky" to "V HST je hodnosť počet nenulových riadkov.",
    "Sú riadky LN?" to "Riadky sú LN práve vtedy, keď sa hodnosť rovná ich počtu.",
)

/* ---- 2. rank with a parameter, regularity, inverse for one value (skúška 26. 1. 2026, př. 2) ------------------ */

internal class ParamCase(val rows: List<PVec>, val res: ParamRank, val inv0: Mat)

internal fun Random.paramCase(): ParamCase {
    val f = Field.R
    while (true) {
        val ints = List(3) { List(3) { pick(-3, 3) } }
        val cells = (0 until 9).shuffled(this).take(pick(2, 3))
        val rows = ints.mapIndexed { i, row -> row.mapIndexed { j, c -> if (i * 3 + j in cells) Poly.lin(c, if (nextBoolean()) 1 else -1) else Poly.of(c) } }
        if (rows.any { row -> row.all { it.isZero } }) continue
        val res = paramRank(rows) ?: continue
        // two main columns: the rest is one entry, so each branch finishes to HST by plain row operations (no determinant, chapter 5)
        if (res.pivots != 2 || res.generic != 3 || res.special.isEmpty() || res.special.size > 2) continue
        if (res.special.keys.any { !it.isInt || kotlin.math.abs(it.num) > 6 } || Frac.ZERO in res.special) continue
        if (res.special.values.any { it != 2 }) continue
        val probes = listOf(-7, -3, -2, -1, 0, 1, 2, 3, 7).map { it.q() } + res.special.keys
        if (probes.any { rankAt(rows, it) != (res.special[it] ?: res.generic) }) continue
        val a0 = Mat(rows.map { row -> row.map { it.eval(Frac.ZERO) } })
        val inv = f.inverse(a0) ?: continue
        if (inv.rows.flatten().any { kotlin.math.abs(it.num) > 12 || it.den > 6 }) continue
        return ParamCase(rows, res, a0)
    }
}

private fun setTex(s: Collection<Frac>) = "\\{" + s.sorted().joinToString(",") { it.tex(true) } + "\\}"

/** Rank with a parameter, regularity and the inverse for one value (skúška 26. 1. 2026, príklad 2). */
val K3RankParamPostup = postup(
    "A) GEM, nuluj len číslom" to "Zvyšok stĺpca nuluj nenulovým číslom, nie výrazom s §a§; riadok nenásob výrazom s §a§, ten môže byť nulový.",
    "A) Rozober hodnoty §a§" to "Zisti, pre ktoré §a§ je zvyšok s parametrom nulový, a tie hodnoty dosaď zvlášť.",
    "B) Regularita" to "Matica §3\\times 3§ je regulárna práve pri hodnosti §3§ (Věta 3.35).",
    "C) Inverzia pre §a=0§" to "Uprav §(\\mathbf{A}\\mid\\mathbf{E})§ na §(\\mathbf{E}\\mid\\mathbf{A}^{-1})§ a urob skúšku.",
)

internal fun rankParamSolution(pc: ParamCase): Solution {
    val f = Field.R
    val sp = pc.res.special.keys.sorted()
    val s = setTex(sp)
    val inv = f.inverse(pc.inv0)!!
    val (gem, cases, regular, inverse) = K3RankParamPostup
    return K3RankParamPostup.solve {
        gem("GEM robím tak, aby každý krok platil pre každé §a§: zvyšok stĺpca nulujem nenulovým číslom. Riadok nenásobím výrazom s §a§, ten môže byť nulový; pripočítať §a§-násobok iného riadku smiem.",
            pLines(pc.res.start, pc.res.steps))
        require(pc.res.pivots == 2 && pc.res.restCols.size == 1) { "hodnosť s parametrom: dva hlavné stĺpce a jeden prvok navyše" }
        val rc = pc.res.restCols.single()
        val rows = pc.res.result.rows
        val fa = rows[2][rc]
        val col = "${rc + 1}."
        val (p1, p2) = pc.res.pivotCols.map { "${it + 1}." }
        cases("Prvé dva riadky majú každý nenulové číslo s nulami pod ním, a to pre každé §a§. Prvý ho má v ${p1} stĺpci, druhý v ${p2} stĺpci. Tretí riadok má jediný nenulový prvok, §${fa.tex()}§ v ${col} stĺpci, a ten je nulový práve pre §a\\in $s§.")
        // the branch a ∉ s, computed: (G3) with the third row clears its column, (G1) orders the rows by their first nonzero element
        val cleared = rows.mapIndexed { i, r -> if (i == 2) r else r.mapIndexed { j, x -> if (j == rc) Poly.of(0) else x } }
        val lead = { r: PVec -> r.indexOfFirst { !it.isZero } }
        val hst = PMat(cleared.sortedBy(lead))
        val clear = rows.take(2).any { !it[rc].isZero }
        val swap = hst.rows != cleared
        val how = listOfNotNull(
            if (clear) "(G3) tretím riadkom vynulujem zvyšok ${col} stĺpca; delím pritom §${fa.tex()}§, čo v tejto vetve smiem, a inde sa nič nezmení, lebo tretí riadok má inde nuly" else null,
            if (swap) "(G1) usporiadam riadky tak, aby prvé nenulové prvky išli zľava doprava" else null,
        )
        cases("Pre §a\\notin $s§ je §${fa.tex()}\\neq 0§. " + (if (how.isEmpty()) "Matica je už v HST" else how.joinToString(". Potom ").replaceFirstChar { it.uppercase() }) +
            ". V HST sú tri nenulové riadky, teda §h(\\mathbf{A})=3§ (Věty 3.6 a 3.7).", Formula(hst.tex(), "HST pre §a\\notin $s§"))
        for (c in sp) {
            val at = Mat(pc.rows.map { row -> row.map { it.eval(c) } })
            cases("Pre §a=${c.tex(true)}§ dosadím a upravím znova; vyjdú dva nenulové riadky, teda §h(\\mathbf{A})=2§.", gemLines(f.echelon(at)))
        }
        regular("Regulárna je práve pri §h(\\mathbf{A})=3§. Vtedy totiž GEM z §(\\mathbf{A}\\mid\\mathbf{E})§ dá vľavo §\\mathbf{E}§, pri menšej hodnosti nulový riadok (Věta 3.35). Teda pre §a\\notin $s§.")
        inverse("Pre §a=0§ upravím §(\\mathbf{A}\\mid\\mathbf{E})§ na §(\\mathbf{E}\\mid\\mathbf{A}^{-1})§ podľa Postupu 3.37.", jordanLines(f.jordan(beside(pc.inv0, identity(3)), 3)))
        inverse("Vpravo je inverzia. Skúška: súčin §\\mathbf{A}\\mathbf{A}^{-1}§ musí dať §\\mathbf{E}§.", "\\mathbf{A}^{-1}=${inv.tex()}")
    }
}

fun k3RankParamExercise(r: Random): Exercise {
    val pc = r.paramCase()
    val f = Field.R
    val sp = pc.res.special.keys.sorted()
    val pm = PMat(pc.rows)
    val text = "Mějme matici s parametrem \$a\\in\\mathbb{R}$: $\\mathbf{A}=${pm.tex()}\\in\\mathbb{R}^{3,3}$. A) Spočtěte hodnost \$\\mathbf{A}$ v závislosti na parametru. B) Určete, pro které hodnoty parametru \$a$ je matice \$\\mathbf{A}$ regulární. C) Spočtěte inverzní matici k \$\\mathbf{A}$ pro hodnotu \$a=0$."
    val s = setTex(sp)
    val right = "§h(\\mathbf{A})=3§ pre §a\\notin $s§, §h(\\mathbf{A})=2§ pre §a\\in $s§"
    val all3 = "§h(\\mathbf{A})=3§ pre všetky §a\\in\\mathbb{R}§"
    val fewer = if (sp.size == 2) "§h(\\mathbf{A})=3§ pre §a\\neq ${sp[0].tex(true)}§, §h(\\mathbf{A})=2§ pre §a=${sp[0].tex(true)}§"
        else "§h(\\mathbf{A})=3§ pre §a\\notin $s§, §h(\\mathbf{A})=1§ pre §a\\in $s§"
    val order = listOf(0, 1, 2).shuffled(r)
    val orderB = listOf(0, 1, 2).shuffled(r)
    val opts = listOf(right, all3, fewer)
    val inv = f.inverse(pc.inv0)!!
    return Exercise(
        "la1-k3-rank-param", "Hodnosť s parametrom", "Šablóna zo skúšky 26. 1. 2026 (variant Hilbert), príklad 2, 10 b",
        text,
        parts = listOf(
            { _ -> Part("A) Hodnosť v závislosti od §a§:".replace('§', '$'), Answer.Choice(order.map { opts[it].replace('§', '$') }, order.indexOf(0),
                "Po GEM, kde stĺpce nulujem len číslami, rozhoduje zvyšok s parametrom: hodnosť klesne práve pre §a\\in $s§.".replace('§', '$'),
                wrong = mapOf(
                    order.indexOf(1) to "Parameter si prehliadol: pre §a\\in $s§ vznikne po GEM nulový riadok. Delil si výrazom s §a§ bez rozboru prípadov?",
                    order.indexOf(2) to (if (sp.size == 2) "Chýba hodnota §a=${sp[1].tex(true)}§: aj pre ňu je zvyšok po GEM nulový riadok. Rozober každý koreň."
                        else "Pre §a\\in $s§ vypadne len jeden riadok, nie dva. Dosaď a urob GEM: ostanú dva nenulové riadky."),
                )),
                hint = m("Stĺpce nuluj len nenulovými číslami, výrazy s §a§ nechaj na koniec. Potom zisti, pre ktoré §a§ je zvyšok nulový, a tie hodnoty dosaď zvlášť."), aim = 0) },
            { _ -> Part("B) §\\mathbf{A}§ je regulárna práve pre".replace('§', '$'), Answer.Choice(orderB.map { listOf("\$a\\notin $s$", "\$a\\in $s$", "všetky \$a\\in\\mathbb{R}$")[it] }, orderB.indexOf(0),
                "Regulárna je práve pri hodnosti §3§ (Věta 3.35).".replace('§', '$'),
                wrong = mapOf(orderB.indexOf(1) to "Obrátil si to: pre §a\\in $s§ je §h(\\mathbf{A})=2<3§, matica je singulárna (Věta 3.35).",
                    orderB.indexOf(2) to "Pre §a\\in $s§ je §h(\\mathbf{A})=2§, a regulárna je len matica s hodnosťou §3§ (Věta 3.35).")),
                hint = m("Matica §3\\times 3§ je regulárna práve vtedy, keď má hodnosť §3§. Použi hodnosť z časti A)."), aim = 2) },
            { _ -> Part("C) §\\mathbf{A}^{-1}§ pre §a=0§ (po riadkoch):".replace('§', '$'), Answer.Numbers(List(9) { "x_{${it / 3 + 1}${it % 3 + 1}}" }, null, nums(inv), "$\\mathbf{A}^{-1}=${inv.tex()}$", fractions = true, diagnose = inverseDiagnosis(f, pc.inv0, inv)),
                hint = m("Dosaď §a=0§ a uprav §(\\mathbf{A}\\mid\\mathbf{E})§ na §(\\mathbf{E}\\mid\\mathbf{A}^{-1})§. Nakoniec urob skúšku §\\mathbf{A}\\mathbf{A}^{-1}=\\mathbf{E}§."), aim = 3) },
        ),
        postup = K3RankParamPostup,
        solution = { rankParamSolution(pc) },
        hint = "Stĺpce nuluj len nenulovými číslami, výrazy s a nechaj na koniec. Potom rozober, kedy je zvyšok nulový.",
    )
}

/* ---- 3. inverse by (A|E) --------------------------------------------------------------------------------------- */

fun k3InverseExercise(r: Random): Exercise {
    while (true) {
        val f: Field = listOf(Field.R, Field.Zp(5), Field.Zp(7), Field.R).random(r)
        val n = if (f is Field.R && r.nextBoolean()) 2 else 3
        val singular = r.nextInt(4) == 0
        val a = if (singular) r.matrixOfRank(f, n, n, n - 1, lo = if (f is Field.Zp) 0 else -2, hi = if (f is Field.Zp) f.p - 1 else 3)
            else Mat(List(n) { f.vec(List(n) { if (f is Field.Zp) r.pick(0, f.p - 1) else r.pick(-2, 3) }) })
        val inv = f.inverse(a)
        if (singular != (inv == null)) continue
        if (inv != null && f is Field.R && inv.rows.flatten().any { kotlin.math.abs(it.num) > 9 || it.den > 4 }) continue
        if (a.rows.any { row -> row.all { it.isZero } }) continue
        val sp = matSpace(f, n, n)
        val j = f.jordan(beside(a, identity(n)), n)
        val parts = ArrayList<(List<Any>) -> Part>()
        parts += { _ -> Part("Je §\\mathbf{A}§ regulárna?".replace('§', '$'), Answer.Choice(listOf("Áno", "Nie"), if (singular) 1 else 0,
            if (singular) "Nie: v ľavom bloku vznikne nulový riadok, §h(\\mathbf{A})<$n§.".replace('§', '$') else "Áno: ľavý blok sa dá upraviť na §\\mathbf{E}§, §h(\\mathbf{A})=$n§.".replace('§', '$'),
            wrong = if (singular) mapOf(0 to "V HST ľavého bloku vznikne nulový riadok. Hodnosť je menšia než §$n§, matica je singulárna (Věta 3.35).")
            else mapOf(1 to "Ľavý blok má po GEM §$n§ nenulových riadkov, teda §h(\\mathbf{A})=$n§. Taká matica je regulárna (Věta 3.35).")),
            hint = m("Vedľa §\\mathbf{A}§ napíš §\\mathbf{E}§ a ľavý blok uprav GEM na HST. Nulový riadok vľavo znamená, že §\\mathbf{A}§ je singulárna."), aim = 0) }
        if (inv != null) parts += { _ -> Part("§\\mathbf{A}^{-1}§ (po riadkoch):".replace('§', '$'), Answer.Numbers(List(n * n) { "x_{${it / n + 1}${it % n + 1}}" }, (f as? Field.Zp)?.p, nums(inv), "$\\mathbf{A}^{-1}=${inv.tex()}$", fractions = f is Field.R, diagnose = inverseDiagnosis(f, a, inv)),
            hint = m("Pokračuj z HST: prvé nenulové prvky riadkov vydeľ na jednotky a nad nimi nuluj, kým vľavo nie je §\\mathbf{E}§. Vpravo je potom §\\mathbf{A}^{-1}§."), aim = 2) }
        return Exercise(
            "la1-k3-inverse", "Inverzná matica", if (f is Field.Zp) "Šablóna podľa Príkladu 3.38 (§\\mathbb{Z}_7^{3,3}§)".replace('§', '$') else "Šablóna podľa Postupu 3.37 a skúšky 26. 1. 2026, príklad 2C",
            "Ověřte, zda je matice $\\mathbf{A}=${a.tex()}\\in $sp$ regulární, a pokud je, nalezněte k ní matici inverzní.",
            parts = parts,
            postup = K3InversePostup,
            solution = {
                val (hst, regular, back, result) = K3InversePostup
                K3InversePostup.solve {
                    val mod = if (f is Field.Zp) " Počítam modulo §${f.p}§." else ""
                    if (inv == null) {
                        hst("Vedľa §\\mathbf{A}§ napíšem §\\mathbf{E}§ a ľavý blok upravím na HST riadkovými úpravami (Postup 3.37).$mod", gemLines(f.echelon(beside(a, identity(n)), n)))
                        regular("V ľavom bloku vznikol nulový riadok, teda §h(\\mathbf{A})<$n§ a §\\mathbf{A}§ je singulárna (Věta 3.35).")
                        result("Singulárna matica inverziu nemá.")
                    } else {
                        hst("Vedľa §\\mathbf{A}§ napíšem §\\mathbf{E}§ a ľavý blok upravím na HST riadkovými úpravami (Postup 3.37).$mod", chainLines(j.start, j.steps, 0, j.forward, j.pole))
                        regular("Ľavý blok má §$n§ nenulových riadkov, §h(\\mathbf{A})=$n§. Hodnosť je plná, preto GEM dostane vľavo §\\mathbf{E}§ a tie isté úpravy urobia vpravo z §\\mathbf{E}§ inverziu. Matica je teda regulárna (Věta 3.35).")
                        if (j.forward < j.steps.size) back("Prvé nenulové prvky vydelím na jednotky a nad nimi vynulujem, kým vľavo nie je §\\mathbf{E}§.", chainLines(j.start, j.steps, j.forward, pole = j.pole))
                        result("Vľavo je §\\mathbf{E}§, vpravo inverzia. Skúška: §\\mathbf{A}\\mathbf{A}^{-1}=\\mathbf{E}§, stačí jeden súčin, lebo pri štvorcových maticiach z §\\mathbf{A}\\mathbf{X}=\\mathbf{E}§ plynie aj §\\mathbf{X}\\mathbf{A}=\\mathbf{E}§ (Věta 3.42).", "\\mathbf{A}^{-1}=${inv.tex()}")
                    }
                }
            },
            hint = "(A|E), GEM do HST; nulový riadok vľavo znamená singulárnu. Inak vydeľ prvé nenulové prvky riadkov a nuluj nahor.",
        )
    }
}

val K3InversePostup = postup(
    "Uprav §(\\mathbf{A}\\mid\\mathbf{E})§ na HST" to "Vedľa §\\mathbf{A}§ napíš §\\mathbf{E}§ a ľavý blok uprav riadkovými úpravami na HST.",
    "Je §\\mathbf{A}§ regulárna?" to "Nulový riadok vľavo znamená hodnosť menšiu než §n§: singulárna, inverzia nie je.",
    "Dopočítaj §(\\mathbf{E}\\mid\\mathbf{A}^{-1})§" to "Vydeľ prvé nenulové prvky na jednotky a nad nimi vynuluj.",
    "Výsledok a skúška" to "Pravý blok je §\\mathbf{A}^{-1}§; over §\\mathbf{A}\\mathbf{A}^{-1}=\\mathbf{E}§.",
)

/* ---- 4. dimension of a span and membership through rank (Poznámka 3.9) -------------------------------------- */

fun k3SpanRankExercise(r: Random): Exercise {
    while (true) {
        val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(5)
        val k = r.pick(2, 3)
        val xm = r.matrixOfRank(f, 3, 4, k, lo = if (f is Field.Zp) 0 else -2, hi = if (f is Field.Zp) 4 else 3)
        val xs = xm.rows
        val inside = r.nextBoolean()
        val y = if (inside) f.combo(List(3) { r.pick(-2, 2).q() }, xs) else f.vec(List(4) { if (f is Field.Zp) r.pick(0, 4) else r.pick(-3, 3) })
        if (y.isZero() || f.inSpan(xs, y) != inside) continue
        if (f is Field.R && y.any { kotlin.math.abs(it.num) > 9 }) continue
        val sp = if (f is Field.Zp) "\\mathbb{Z}_5^4" else "\\mathbb{R}^4"
        val hy = f.rank(xs + listOf(y))
        return Exercise(
            "la1-k3-span-rank", "Obal cez hodnosť", "Poznámka 3.9, body 1 a 2",
            "Jsou dány vektory $\\mathbf{x}_1=${xs[0].tex()}$, $\\mathbf{x}_2=${xs[1].tex()}$, $\\mathbf{x}_3=${xs[2].tex()}$ a $\\mathbf{y}=${y.tex()}$ z $$sp$. Určete $\\dim\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle$ a rozhodněte, zda $\\mathbf{y}\\in\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle$.",
            parts = listOf(
                { _ -> Part("$\\dim\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle=$", Answer.Numbers(listOf("\\dim"), null, listOf(k.q()), "$$k$", diagnose = diagnosis(Field.R, listOf(k.q()), listOf(listOf(3.q()) to "Vektory sú tri, ale LZ: po GEM vznikne nulový riadok. Dimenzia je počet nenulových riadkov.", listOf(4.q()) to "§4§ je dimenzia celého priestoru, nie obalu troch vektorov. Obal troch vektorov má dimenziu najviac §3§.")) { "Dimenzia obalu je hodnosť matice s vektormi v riadkoch. Po GEM spočítaj nenulové riadky a skontroluj každý krok." }),
                    hint = m("Vektory zapíš do riadkov a maticu uprav GEM na HST. Dimenzia obalu je počet nenulových riadkov."), aim = 0) },
                { _ -> Part("Patrí §\\mathbf{y}§ do obalu?".replace('§', '$'), Answer.Choice(listOf("Áno", "Nie"), if (inside) 0 else 1,
                    if (inside) "Áno: pridaním §\\mathbf{y}§ hodnosť ostane §$k§.".replace('§', '$') else "Nie: s §\\mathbf{y}§ hodnosť stúpne na §$hy§.".replace('§', '$'),
                    wrong = if (inside) mapOf(1 to "S riadkom §\\mathbf{y}§ ostane hodnosť §$k§. Rovnaká dimenzia znamená, že §\\mathbf{y}§ v obale je (Důsledek 2.64).")
                    else mapOf(0 to "S riadkom §\\mathbf{y}§ stúpne hodnosť z §$k§ na §$hy§. Obal sa zväčšil, §\\mathbf{y}§ v ňom nebol (Důsledek 2.64).")),
                    hint = m("Pridaj §\\mathbf{y}§ ako ďalší riadok a hodnosť spočítaj znova. Ak sa nezmenila, §\\mathbf{y}§ v obale je; ak stúpla, nie je."), aim = 1) },
            ),
            postup = K3SpanRankPostup,
            solution = {
                val (dim, add, end) = K3SpanRankPostup
                K3SpanRankPostup.solve {
                    dim("Vektory píšem do **riadkov**. Hodnosť je dimenzia obalu riadkov, teda dimenzia tohto obalu (Poznámka 3.10). Nič neriešim, len počítam nenulové riadky.", gemLines(f.echelon(Mat(xs))))
                    dim("V HST sú §$k§ nenulové riadky. Riadkové úpravy obal riadkov nemenia, lebo každá je vratná a nový riadok je kombináciou starých (Pozorování 3.5). " +
                        Reasons.rankHst + " Teda §\\dim\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle=$k§ a nenulové riadky HST (po transponovaní) tvoria bázu tohto obalu.")
                    add("Pridám riadok §\\mathbf{y}§ a hodnosť spočítam znova.", gemLines(f.echelon(Mat(xs + listOf(y)))))
                    end(if (inside) "Hodnosť ostala §$k§, dimenzie obalov sa rovnajú. Preto §\\mathbf{y}\\in\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle§ (Důsledek 2.64)."
                        else "Hodnosť stúpla na §$hy§, obal sa zväčšil. Preto §\\mathbf{y}\\notin\\langle\\mathbf{x}_1,\\mathbf{x}_2,\\mathbf{x}_3\\rangle§ (Důsledek 2.64).")
                }
            },
            hint = "Vektory do riadkov. Dimenzia obalu je hodnosť. y patrí do obalu, keď ho pridanie nezmení.",
        )
    }
}

val K3SpanRankPostup = postup(
    "Dimenzia obalu" to "Vektory do riadkov, GEM; dimenzia obalu je počet nenulových riadkov.",
    "Pridaj §\\mathbf{y}§" to "Pridaj §\\mathbf{y}§ ako ďalší riadok a hodnosť spočítaj znova.",
    "Záver" to "Hodnosť sa nezmenila: §\\mathbf{y}§ v obale je. Stúpla: nie je.",
)

/* ---- 5. product as combinations (Věty 3.15 a 3.18, Otázka za Větou 3.18) --------------------------------------- */

fun k3ProductExercise(r: Random): Exercise {
    val f = Field.R
    val a = Mat(List(2) { vec(*IntArray(3) { r.pick(-2, 4) }) })
    if (r.nextBoolean()) {
        val x = vec(*IntArray(3) { r.nonzero(-2, 3) })
        val ax = f.mulVec(a, x)
        val asRows = (0 until 2).map { i -> (0 until 3).fold(Frac.ZERO) { acc, j -> acc + x[j] * a[i, j] } } // the same, here for the text
        val cols = (0 until 3).map { j -> a.rows.map { it[j] } }
        return Exercise(
            "la1-k3-product", "Súčin ako kombinácia stĺpcov", "Věta 3.15, Pozorování 3.16",
            "Spočtěte $\\mathbf{A}\\mathbf{x}$ pro $\\mathbf{A}=${a.tex()}$ a $\\mathbf{x}=${x.tex()}^T$ jako lineární kombinaci sloupců $\\mathbf{A}$.",
            parts = listOf({ _ -> Part("$\\mathbf{A}\\mathbf{x}=$", Answer.Numbers(listOf("y_1", "y_2"), null, ax, "$${ax.tex()}^T$", diagnose = diagnosis(f, ax, listOf(
                listOf(cols.indices.fold(Frac.ZERO) { acc, j -> acc + cols[j][0] }, cols.indices.fold(Frac.ZERO) { acc, j -> acc + cols[j][1] }) to "Zabudol si koeficienty: §\\mathbf{A}\\mathbf{x}=x_1\\mathbf{A}_{:1}+x_2\\mathbf{A}_{:2}+x_3\\mathbf{A}_{:3}§.",
            )) { g -> val i = if (g[0] != ax[0]) 0 else 1; "Nesedí §y_${i + 1}§: má byť §${(0 until 3).joinToString("+") { j -> "(${x[j].tex(true)})\\cdot(${a[i, j].tex(true)})" }}=${ax[i].tex(true)}§." })) }),
            postup = K3ColumnsPostup,
            solution = {
                val (split, sum) = K3ColumnsPostup
                K3ColumnsPostup.solve {
                    split("§\\mathbf{A}\\mathbf{x}§ je kombinácia stĺpcov §\\mathbf{A}§ s koeficientmi zo §\\mathbf{x}§, lebo §i§-tá zložka súčinu je §a_{i1}x_1+a_{i2}x_2+a_{i3}x_3§ (Věta 3.15).", "\\mathbf{A}\\mathbf{x}=x_1\\mathbf{A}_{:1}+x_2\\mathbf{A}_{:2}+x_3\\mathbf{A}_{:3}")
                    sum("Dosadím zložky §\\mathbf{x}§ a stĺpce, potom sčítam po zložkách.", Terms.lin(x, cols.map { it.tex() + "^T" }) + "=${asRows.tex()}^T")
                }
            },
            hint = "Koeficienty z x násobia stĺpce A.",
        )
    }
    // a matrix B with B·A having prescribed rows: combinations of the rows of A
    val coefs = List(3) { vec(r.pick(-2, 2), r.pick(-2, 2)) }
    val b = Mat(coefs)
    fun say(c: Vec): String = when {
        c.isZero() -> "nulový řádek"
        c[1].isZero -> if (c[0] == Frac.ONE) "první řádek matice \$\\mathbf{A}$" else "\$(${c[0].tex(true)})\$-násobek prvního řádku matice \$\\mathbf{A}$"
        c[0].isZero -> if (c[1] == Frac.ONE) "druhý řádek matice \$\\mathbf{A}$" else "\$(${c[1].tex(true)})\$-násobek druhého řádku matice \$\\mathbf{A}$"
        else -> "\$${comboTexF(c, listOf("\\mathbf{A}_{1:}", "\\mathbf{A}_{2:}"))}$"
    }
    val text = "Mějme matici \$\\mathbf{A}\\in\\mathbb{R}^{2,3}$. Napište matici \$\\mathbf{B}$ takovou, že součin \$\\mathbf{B}\\mathbf{A}$ má v prvním řádku ${say(coefs[0])}, v druhém řádku ${say(coefs[1])} a v třetím řádku ${say(coefs[2])}."
    val expected = nums(b)
    return Exercise(
        "la1-k3-product", "Matica, ktorá kombinuje riadky", "Otázka za Větou 3.18, šablóna",
        text,
        parts = listOf(
            { _ -> Part("Typ matice §\\mathbf{B}§ a poradie súčinu:".replace('§', '$'), Answer.Choice(listOf("\$\\mathbf{B}\\in\\mathbb{R}^{3,2}$, súčin \$\\mathbf{B}\\mathbf{A}$", "\$\\mathbf{B}\\in\\mathbb{R}^{2,3}$, súčin \$\\mathbf{A}\\mathbf{B}$", "\$\\mathbf{B}\\in\\mathbb{R}^{3,3}$, súčin \$\\mathbf{B}\\mathbf{A}$"), 0,
                "§\\mathbf{B}§ je typu §3\\times 2§ a stojí vľavo. Riadky súčinu kombinujú riadky pravého činiteľa, teda §\\mathbf{A}§.".replace('§', '$'),
                wrong = mapOf(1 to "Súčin §\\mathbf{A}\\mathbf{B}§ by tu nebol definovaný: §\\mathbf{A}§ má 3 stĺpce, §\\mathbf{B}§ 2 riadky. Riadky §\\mathbf{A}§ kombinuje §\\mathbf{B}\\mathbf{A}§ s §\\mathbf{B}\\in\\mathbb{R}^{3,2}§ (Věta 3.18).",
                    2 to "Typ §3\\times 3§ sa s §\\mathbf{A}\\in\\mathbb{R}^{2,3}§ nedá vynásobiť. §\\mathbf{B}§ potrebuje toľko stĺpcov, koľko má §\\mathbf{A}§ riadkov.")),
                hint = m("Riadky súčinu sú kombinácie riadkov pravého činiteľa. Koľko riadkov má mať súčin a koľko stĺpcov musí mať ľavý činiteľ?"), aim = 0) },
            { _ -> Part("§\\mathbf{B}§ (po riadkoch):".replace('§', '$'), Answer.Numbers(List(6) { "b_{${it / 2 + 1}${it % 2 + 1}}" }, null, expected, "$\\mathbf{B}=${b.tex()}$", diagnose = diagnosis(f, expected, listOf(
                nums(Mat(b.rows.map { it.reversed() })) to "Koeficienty sú prehodené. V §i§-tom riadku §\\mathbf{B}§ je najprv koeficient pri §\\mathbf{A}_{1:}§, potom pri §\\mathbf{A}_{2:}§.",
            )) { g -> val i = (0 until 6).first { g[it] != expected[it] } / 2; "Nesedí §${i + 1}§. riadok §\\mathbf{B}§. Jeho prvky sú koeficienty, ktorými sa kombinujú riadky §\\mathbf{A}§ (Věta 3.15)." }),
                hint = m("§i§-tý riadok §\\mathbf{B}\\mathbf{A}§ je §i§-tý riadok §\\mathbf{B}§ krát §\\mathbf{A}§. V §i§-tom riadku §\\mathbf{B}§ sú teda koeficienty pri §\\mathbf{A}_{1:}§ a §\\mathbf{A}_{2:}§."), aim = 1) },
        ),
        postup = K3RowsPostup,
        solution = {
            val (which, coefs) = K3RowsPostup
            K3RowsPostup.solve {
                which("Riadok súčinu je riadok ľavého činiteľa krát pravý činiteľ. Prvok na mieste §(i,j)§ je totiž súčin §i§-teho riadku ľavého činiteľa so §j§-tým stĺpcom pravého (Věta 3.18). Celý riadok súčinu je preto kombináciou riadkov pravého činiteľa s koeficientmi z riadku ľavého (Věta 3.15). Preto §\\mathbf{A}§ ide vpravo a §\\mathbf{B}\\in\\mathbb{R}^{3,2}§.",
                    "(\\mathbf{B}\\mathbf{A})_{i:}=\\mathbf{B}_{i:}\\mathbf{A}=b_{i1}\\mathbf{A}_{1:}+b_{i2}\\mathbf{A}_{2:}")
                coefs("V §i§-tom riadku §\\mathbf{B}§ sú koeficienty, ktorými sa kombinujú riadky §\\mathbf{A}§ do §i§-teho riadku súčinu.", "\\mathbf{B}=${b.tex()}")
            }
        },
        hint = "§i§-tý riadok §\\mathbf{B}\\mathbf{A}§ je §i§-tý riadok §\\mathbf{B}§ krát §\\mathbf{A}§: koeficienty pri riadkoch §\\mathbf{A}§.".replace('§', '$'),
    )
}

val K3ColumnsPostup = postup(
    "Rozpíš súčin na stĺpce" to "§\\mathbf{A}\\mathbf{x}§ je kombinácia stĺpcov §\\mathbf{A}§ s koeficientmi zo §\\mathbf{x}§.",
    "Vynásob a sčítaj" to "Každý stĺpec vynásob jeho koeficientom a sčítaj po zložkách.",
)

val K3RowsPostup = postup(
    "Ktorý činiteľ kombinuje riadky?" to "Riadky súčinu sú kombinácie riadkov pravého činiteľa; matica s koeficientmi ide vľavo.",
    "Riadky §\\mathbf{B}§ sú koeficienty" to "§i§-ty riadok §\\mathbf{B}§ sú koeficienty pri riadkoch §\\mathbf{A}§.",
)

/* ---- 6. algebra of inverses (skúška 26. 1. 2026, př. 3B: upravte (3BB^T)^-1) --------------------------------- */

private data class Alg(val expr: String, val right: String, val wrong: List<Pair<String, String>>)

fun k3AlgebraExercise(r: Random): Exercise {
    val k = listOf(2, 3, 5, -2, -3).random(r)
    val ki = "\\tfrac{1}{$k}".replace("\\tfrac{1}{-", "-\\tfrac{1}{").replace("}{-", "}{")
    val kInv = if (k < 0) "-\\tfrac{1}{${-k}}" else "\\tfrac{1}{$k}"
    val cases = listOf(
        Alg("($k\\mathbf{B}\\mathbf{B}^T)^{-1}", "$kInv(\\mathbf{B}^{-1})^T\\mathbf{B}^{-1}", listOf(
            "$kInv\\mathbf{B}^{-1}(\\mathbf{B}^{-1})^T" to "Poradie sa pri inverzii súčinu obracia: §(\\mathbf{X}\\mathbf{Y})^{-1}=\\mathbf{Y}^{-1}\\mathbf{X}^{-1}§ (Věta 3.27).",
            "$k(\\mathbf{B}^{-1})^T\\mathbf{B}^{-1}" to "Skalár sa tiež invertuje: §(k\\mathbf{X})^{-1}=k^{-1}\\mathbf{X}^{-1}§, lebo §(k\\mathbf{X})(k^{-1}\\mathbf{X}^{-1})=\\mathbf{E}§.",
        )),
        Alg("(\\mathbf{A}\\mathbf{B}^T)^{-1}", "(\\mathbf{B}^{-1})^T\\mathbf{A}^{-1}", listOf(
            "\\mathbf{A}^{-1}(\\mathbf{B}^{-1})^T" to "Poradie sa pri inverzii súčinu obracia (Věta 3.27). V súčine §\\mathbf{A}\\mathbf{B}^T(\\mathbf{B}^T)^{-1}\\mathbf{A}^{-1}§ stojí každý činiteľ vedľa svojej inverzie a súčin dá §\\mathbf{E}§. V tvojom poradí stojí §\\mathbf{B}^T§ vedľa §\\mathbf{A}^{-1}§ a nezruší sa.",
            "(\\mathbf{B}^T)^{-1}\\mathbf{A}^T" to "Matica §\\mathbf{A}§ nemá byť transponovaná, ale invertovaná. Transpozícia patrí len k §\\mathbf{B}§.",
        )),
        Alg("((\\mathbf{A}\\mathbf{B})^T)^{-1}", "(\\mathbf{A}^{-1})^T(\\mathbf{B}^{-1})^T", listOf(
            "(\\mathbf{B}^{-1})^T(\\mathbf{A}^{-1})^T" to "Poradie si obrátil dvakrát nesprávne: §(\\mathbf{A}\\mathbf{B})^T=\\mathbf{B}^T\\mathbf{A}^T§ a jej inverzia je §(\\mathbf{A}^T)^{-1}(\\mathbf{B}^T)^{-1}§.",
            "\\mathbf{B}^{-1}\\mathbf{A}^{-1}" to "Transpozícia sa stratila. §((\\mathbf{A}\\mathbf{B})^T)^{-1}=((\\mathbf{A}\\mathbf{B})^{-1})^T§ (Věta 3.28), nie §(\\mathbf{A}\\mathbf{B})^{-1}§.",
        )),
        Alg("(\\mathbf{A}^{-1}\\mathbf{B})^{-1}", "\\mathbf{B}^{-1}\\mathbf{A}", listOf(
            "\\mathbf{A}\\mathbf{B}^{-1}" to "Poradie sa obracia (Věta 3.27): najprv §\\mathbf{B}^{-1}§, potom §(\\mathbf{A}^{-1})^{-1}=\\mathbf{A}§.",
            "\\mathbf{B}^{-1}\\mathbf{A}^{-1}" to "§(\\mathbf{A}^{-1})^{-1}=\\mathbf{A}§ (Pozorování 3.26). Definícia inverzie je symetrická: §\\mathbf{A}\\mathbf{A}^{-1}=\\mathbf{A}^{-1}\\mathbf{A}=\\mathbf{E}§ zároveň hovorí, že §\\mathbf{A}§ je inverzná k §\\mathbf{A}^{-1}§.",
        )),
        Alg("($k\\mathbf{A}^T)^{-1}", "$kInv(\\mathbf{A}^{-1})^T", listOf(
            "$k(\\mathbf{A}^{-1})^T" to "Skalár sa tiež invertuje: §(k\\mathbf{X})^{-1}=k^{-1}\\mathbf{X}^{-1}§.",
            "$kInv\\mathbf{A}^{-1}" to "Transpozícia sa stratila. §(\\mathbf{A}^T)^{-1}=(\\mathbf{A}^{-1})^T§ (Věta 3.28).",
        )),
    )
    val cs = cases.random(r)
    val opts = listOf(cs.right) + cs.wrong.map { it.first }
    val order = listOf(0, 1, 2).shuffled(r)
    check(ki.isNotEmpty())
    return Exercise(
        "la1-k3-algebra", "Úprava výrazu s inverziou", "Šablóna zo skúšky 26. 1. 2026 (variant Hilbert), príklad 3B, 4 b",
        "Uvažujme regulární matice \$\\mathbf{A},\\mathbf{B}$ nad \$\\mathbb{R}$ stejného typu. Upravte výraz \$${cs.expr}$.",
        parts = listOf({ _ -> Part("${'$'}${cs.expr}=${'$'}", Answer.Choice(order.map { "$" + opts[it] + "$" }, order.indexOf(0),
            "Inverzia súčinu obráti poradie, skalár sa invertuje, transpozícia sa s inverziou prehodí.",
            wrong = mapOf(order.indexOf(1) to cs.wrong[0].second, order.indexOf(2) to cs.wrong[1].second))) }),
        postup = K3AlgebraPostup,
        solution = {
            val (rules, apply, check) = K3AlgebraPostup
            K3AlgebraPostup.solve {
                rules("Použijem štyri pravidlá: inverzia súčinu obracia poradie, transpozícia a inverzia sa dajú prehodiť, skalár sa invertuje.",
                    Formula("(\\mathbf{X}\\mathbf{Y})^{-1}=\\mathbf{Y}^{-1}\\mathbf{X}^{-1},\\qquad (\\mathbf{X}^T)^{-1}=(\\mathbf{X}^{-1})^T", "Věta 3.27 a Věta 3.28"),
                    Formula("(\\mathbf{X}^{-1})^{-1}=\\mathbf{X}", "Pozorování 3.26"),
                    Formula("(k\\mathbf{X})^{-1}=k^{-1}\\mathbf{X}^{-1}\\ (k\\neq 0)", "Z definície inverzie: §(k\\mathbf{X})(k^{-1}\\mathbf{X}^{-1})=kk^{-1}\\,\\mathbf{X}\\mathbf{X}^{-1}=\\mathbf{E}§, v druhom poradí rovnako"))
                apply("Pravidlá použijem zvonka dovnútra.", "${cs.expr}=${cs.right}")
                check("Výsledok vynásobím pôvodnou maticou v zátvorke a upravím na §\\mathbf{E}§. Stačí jedno poradie, lebo pri štvorcových maticiach z §\\mathbf{X}\\mathbf{Y}=\\mathbf{E}§ plynie aj §\\mathbf{Y}\\mathbf{X}=\\mathbf{E}§ (Věta 3.42).")
            }
        },
        hint = "Inverzia súčinu obracia poradie; transpozícia a inverzia sa dajú prehodiť.",
    )
}

val K3AlgebraPostup = postup(
    "Pravidlá" to "Inverzia súčinu obracia poradie, transpozícia a inverzia sa prehodia, skalár sa invertuje.",
    "Uprav výraz" to "Pravidlá použi zvonka dovnútra, po jednom.",
    "Skúška" to "Vynásob výsledok pôvodnou maticou; musí vyjsť §\\mathbf{E}§.",
)

/* ---- 7. counting in Z_p (skúška 17. 12. 2025, př. 3A; 26. 1. 2026, př. 3E) ------------------------------------ */

fun k3CountExercise(r: Random): Exercise {
    val p = listOf(2, 3, 5, 7).random(r)
    return if (r.nextBoolean()) {
        val cnt = (p * p - 1L) * (p * p - p)
        Exercise(
            "la1-k3-count", "Počet regulárnych matíc", "Poznámka 3.36 a Veta 2.42; jednoduchšia verzia bonusov skúšok 26. 1. 2026 (variant Hilbert), príklad 3E, a 2. 2. 2026 (variant Bolzano), príklad 4E: regulárne matice 2×2 nad ℤ₇ s podmienkami navyše",
            "Kolik existuje regulárních matic v \$\\mathbb{Z}_{$p}^{2,2}$?",
            parts = listOf({ _ -> Part("Počet:", Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(cnt)), "$($p^2-1)($p^2-$p)=$cnt$", diagnose = diagnosis(Field.R, listOf(Frac.of(cnt)), listOf(
                listOf(Frac.of((p * p - 1L) * (p * p - 1L))) to "Druhý riadok nesmie byť ani násobkom prvého, nielen nulový. Násobkov prvého riadku je §$p§, nie §1§.",
                listOf(Frac.of(p.toLong() * p * p * p)) to "To sú všetky matice §2\\times 2§. Regulárne majú LN riadky (Poznámka 3.36).",
                listOf(Frac.of((p * p - 1L) * (p * p - 2L))) to "Takmer: druhý riadok musí byť mimo celého §\\langle\\mathbf{r}_1\\rangle§. Ten má §$p§ prvkov, nie len §\\theta§ a §\\mathbf{r}_1§.",
            )) { "Regulárna má LN riadky: prvý nenulový, druhý mimo obalu prvého. Spočítaj obe voľby a vynásob." })) }),
            postup = K3CountRegularPostup,
            solution = {
                val (rows, first, second, product) = K3CountRegularPostup
                K3CountRegularPostup.solve {
                    rows("§\\mathbf{A}§ je regulárna práve pri hodnosti §2§. Hodnosť je dimenzia obalu riadkov, preto je §2§ práve vtedy, keď sú oba riadky LN (Poznámka 3.36).")
                    first("Prvý riadok môže byť ľubovoľný nenulový vektor zo §\\mathbb{Z}_{$p}^2§.", "$p^2-1=${p * p - 1}")
                    second("Druhý musí byť mimo §\\langle\\mathbf{r}_1\\rangle§, inak by bol násobkom prvého. Obal má §$p§ prvkov, lebo každý je §\\alpha\\mathbf{r}_1§ pre jediné §\\alpha\\in\\mathbb{Z}_{$p}§ (Poznámka 2.67).", "$p^2-$p=${p * p - p}")
                    product("Počty možností pre oba riadky vynásobím.", "($p^2-1)($p^2-$p)=${p * p - 1}\\cdot ${p * p - p}=$cnt")
                }
            },
            hint = "Riadky musia byť LN: prvý nenulový, druhý nie násobok prvého.",
        )
    } else {
        val cnt = p - 1L
        Exercise(
            "la1-k3-count", "Matice hodnosti 1 v §\\mathbb{Z}_p^{1,1}§".replace('§', '$'), "Skúška 17. 12. 2025 (variant Frobenius), príklad 3A",
            "Definujte hodnost matice. Kolik existuje matic v \$\\mathbb{Z}_{$p}^{1,1}$ s hodností 1?",
            parts = listOf({ _ -> Part("Počet:", Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(cnt)), "$$cnt$", diagnose = diagnosis(Field.R, listOf(Frac.of(cnt)), listOf(
                listOf(Frac.of(p.toLong())) to "Matica §(0)§ má hodnosť §0§: jej riadok generuje len §\\{\\theta\\}§. Nulu treba vynechať.",
                listOf(Frac.ONE) to "Hodnosť §1§ má každá nenulová matica §(c)§, nie len §(1)§. Riadok §(c)§ s §c\\neq 0§ generuje celé §\\mathbb{Z}_{$p}^1§.",
            )) { "Matica §1\\times 1§ je jedno číslo. Hodnosť §1§ má, keď je jej riadok nenulový." })) }),
            postup = K3CountRankPostup,
            solution = {
                val (def, which, count) = K3CountRankPostup
                K3CountRankPostup.solve {
                    def("Hodnosť je dimenzia obalu riadkov (Definice 3.1); matica §(c)§ má jediný riadok §(c)§.")
                    which("Pre §c\\neq 0§ je §\\langle(c)\\rangle=\\mathbb{Z}_{$p}^1§ dimenzie §1§, pre §c=0§ je to §\\{\\theta\\}§ dimenzie §0§.")
                    count("Hodnosť §1§ majú teda všetky nenulové čísla zo §\\mathbb{Z}_{$p}§.", "$p-1=$cnt")
                }
            },
            hint = "Definícia: dimenzia obalu riadkov. Kedy je obal jedného čísla celý priestor?",
        )
    }
}

val K3CountRegularPostup = postup(
    "Kedy je regulárna?" to "Regulárna matica má LN riadky.",
    "Prvý riadok" to "Prvý riadok je ľubovoľný nenulový.",
    "Druhý riadok" to "Druhý musí byť mimo obalu prvého, a ten má §p§ prvkov.",
    "Vynásob" to "Počty možností pre jednotlivé riadky vynásob.",
)

val K3CountRankPostup = postup(
    "Definícia hodnosti" to "Hodnosť je dimenzia obalu riadkov.",
    "Ktoré matice majú hodnosť §1§?" to "Riadok generuje priestor dimenzie §1§ práve vtedy, keď je nenulový.",
    "Spočítaj" to "Spočítaj také matice.",
)

internal val K3Generators: Map<String, (Random) -> Exercise> = mapOf(
    "la1-k3-rank" to ::k3RankExercise,
    "la1-k3-rank-param" to ::k3RankParamExercise,
    "la1-k3-inverse" to ::k3InverseExercise,
    "la1-k3-span-rank" to ::k3SpanRankExercise,
    "la1-k3-product" to ::k3ProductExercise,
    "la1-k3-algebra" to ::k3AlgebraExercise,
    "la1-k3-count" to ::k3CountExercise,
)

internal val K3Templates = listOf(
    TemplateInfo("la1-k3-rank", "Hodnosť matice", "GEM do HST nad §\\mathbb{R}§ aj §\\mathbb{Z}_p§"),
    TemplateInfo("la1-k3-rank-param", "Hodnosť s parametrom", "Hodnosť, regularita, inverzia; skúška, 10 b"),
    TemplateInfo("la1-k3-inverse", "Inverzná matica", "§(\\mathbf{A}\\mid\\mathbf{E})\\sim(\\mathbf{E}\\mid\\mathbf{A}^{-1})§"),
    TemplateInfo("la1-k3-span-rank", "Obal cez hodnosť", "Dimenzia obalu, patrí vektor do obalu"),
    TemplateInfo("la1-k3-product", "Súčin a kombinácie", "Stĺpce, riadky, matica úprav"),
    TemplateInfo("la1-k3-algebra", "Úprava výrazu s inverziou", "§(k\\mathbf{B}\\mathbf{B}^T)^{-1}§ a podobné; skúška, 4 b"),
    TemplateInfo("la1-k3-count", "Počty matíc v §\\mathbb{Z}_p§", "Regulárne, hodnosť 1"),
)

/** The matrix of a sequence of row operations (Příklad 3.33). */
val K3OpsMatrixPostup = postup(
    "Matica každej úpravy" to "Urob tú istú úpravu na jednotkovej matici §\\mathbf{E}§.",
    "Vynásob ich" to "Matice úprav vynásob tak, aby posledná úprava stála najviac vľavo.",
    "Kontrola" to "Tie isté úpravy urobené na §\\mathbf{E}§ dajú tú istú maticu.",
)

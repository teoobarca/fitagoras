package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * Short drills of chapter 2 that the course page lists for the 2nd test ("Co bude v druhé zápočtové písemce?") next
 * to the big problems: is a given set a basis of T^n, what a span in R^2 and R^3 looks like, the standard basis.
 * Numbers drawn, everything decided by the engine (la/Gem.kt), as in Templates.kt.
 */

private fun Random.pick(lo: Int, hi: Int) = nextInt(lo, hi + 1)

/* ---- Is the given set a basis of T^n? (Definícia 2.44, Postup 2.45, Veta 2.55; MARAST #13489, #13615) ---------- */

/** The four answers to "is it a basis", as (LN, generates), in the order of the options. */
private val basisClaims = listOf(true to true, true to false, false to true, false to false)

fun basisDecideExercise(r: Random): Exercise {
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(listOf(3, 5, 5, 7).random(r))
    val p = (f as? Field.Zp)?.p
    val n = 3
    fun draw() = f.vec(List(n) { if (p != null) r.pick(0, p - 1) else r.pick(-2, 3) })
    fun coef() = if (p != null) Frac.of(r.pick(1, p - 1)) else r.pick(-2, 2).let { if (it == 0) 1 else it }.q()
    while (true) {
        // the four answers about equally often; a LZ set by a multiple, a combination, the zero vector or one vector too many
        val case = r.nextInt(4)
        val vs: List<Vec> = when (case) {
            0 -> List(3) { draw() }
            1 -> List(2) { draw() }
            2 -> List(3) { draw() }.let { b -> b + listOf(f.combo(List(3) { coef() }, b)) }
            else -> when (r.nextInt(4)) {
                0 -> List(2) { draw() }.let { b -> b + listOf(f.combo(listOf(coef(), coef()), b)) }
                1 -> draw().let { u -> listOf(u, f.scale(coef(), u)) }
                2 -> List(2) { draw() } + listOf(List(n) { Frac.ZERO })
                else -> List(2) { draw() }.let { b -> b + List(2) { f.combo(listOf(coef(), coef()), b) } }
            }
        }.shuffled(r)
        val ln = f.isLN(vs)
        val gen = f.rank(vs) == n
        if ((ln to gen) != basisClaims[case]) continue
        if (vs.toSet().size < vs.size || vs.count { it.isZero() } > 1) continue
        if (vs.any { v -> !v.isZero() && v.count { it.isZero } > 1 }) continue
        if (p == null && vs.any { v -> v.any { kotlin.math.abs(it.num) > 9 } }) continue
        return basisDecide(f, vs, ln, gen)
    }
}

private fun basisDecide(f: Field, vs: List<Vec>, ln: Boolean, gen: Boolean): Exercise {
    val n = vs[0].size
    val m = vs.size
    val sp = fieldPow(f, n)
    val e = f.echelon(Mat.columns(vs))
    val texts = vs.map { vtex(it) }
    val hasZero = vs.any { it.isZero() }
    // a combination equal to θ (from the engine) and a standard vector outside the span
    val dep = f.dependency(vs)
    val outside = (0 until n).firstOrNull { !f.inSpan(vs, unit(n, it)) }
    val count = if (m == 2) "dva vektory" else "§$m§ vektory"

    val lnTrue = "Súbor je LN: po GEM sú všetky stĺpce hlavné, jediná kombinácia rovná §\\theta§ je triviálna."
    val lnFalse by lazy { "Súbor je LZ: " + when {
        hasZero -> "obsahuje nulový vektor. Ten s koeficientom §1§ a ostatné s §0§ dajú netriviálnu kombináciu rovnú §\\theta§."
        m > n -> "$count v §$sp§ sú vždy LZ. Hlavných stĺpcov je najviac toľko ako riadkov, teda §$n§, takže jeden ostane vedľajší (Pozorovanie 2.32)."
        else -> "netriviálna kombinácia §${Terms.lin(dep!!, texts)}=\\theta§."
    } }
    val genTrue = "Súbor generuje §$sp§: v každom riadku HST je hlavný stĺpec, sústava má riešenie pre každú pravú stranu."
    val genFalse by lazy { "Súbor negeneruje §$sp§: " + if (m < n) "$count nestačia. Generujúci súbor má aspoň toľko vektorov, aká je dimenzia, teda §$n§ (Tvrdenie 2.54)."
        else "v HST ostane nulový riadok, preto napríklad §\\mathbf{e}_${outside!! + 1}§ nie je kombináciou súboru." }
    val together = "Pri §$n§ vektoroch v §$sp§ platia LN a generovanie buď obe, alebo ani jedno, lebo §\\dim $sp=$n§ (Veta 2.55)."

    val options = listOf(
        m("Áno: je LN a generuje §$sp§."),
        m("Nie: je LN, ale negeneruje §$sp§."),
        m("Nie: generuje §$sp§, ale je LZ."),
        m("Nie: je LZ a negeneruje §$sp§."),
    )
    val correct = basisClaims.indexOf(ln to gen)
    val verdict = when (ln to gen) {
        true to true -> "Je to báza: súbor je LN a generuje §$sp§."
        true to false -> "Nie je to báza, lebo súbor negeneruje §$sp§."
        false to true -> "Nie je to báza, lebo súbor je LZ."
        else -> "Nie je to báza: súbor je LZ a negeneruje §$sp§."
    }
    val why = listOf(verdict, if (ln) lnTrue else lnFalse, if (gen) genTrue else genFalse, "Báza je LN súbor, ktorý generuje (Definícia 2.44).").joinToString(" ")
    val wrong = basisClaims.indices.filter { it != correct }.associateWith { o ->
        val (claimLn, claimGen) = basisClaims[o]
        buildList {
            if (claimLn != ln) add(if (ln) lnTrue else lnFalse)
            if (claimGen != gen) add(if (gen) genTrue else genFalse)
            if (m == n && claimLn != claimGen) add(together)
        }.joinToString(" ").let(::m)
    }

    return Exercise(
        "baza-overit", "Je súbor báza?", "Definícia 2.44 a Postup 2.45; MARAST #13489 a #13615",
        m("Rozhodněte, zda soubor §${vstex(vs)}§ vektorů z §$sp§ je bází §$sp§."),
        parts = listOf({ _ -> Part("Je súbor bázou?", Answer.Choice(options, correct, m(why), wrong)) }),
        postup = BasisDecidePostup,
        solution = { _ ->
            val (gem, isLn, generates, end) = BasisDecidePostup
            val alphas = List(m) { "\\alpha_${it + 1}" }
            BasisDecidePostup.solve {
                gem("Vektory dám do stĺpcov, lebo hľadám koeficienty ich kombinácie: pre LN s pravou stranou §\\theta§, pre generovanie s ľubovoľnou. Obe sústavy majú tú istú maticu, stačí jeden GEM.", gemLines(e))
                if (m == n) gem("Vektorov je §$m§, toľko, aká je dimenzia §$sp§, takže by stačilo overiť jednu vlastnosť (Veta 2.55). Overím obe, nech je vidno, že vyjdú rovnako.")
                if (ln) isLn((if (m == 2) "Oba stĺpce sú hlavné" else "Všetky §$m§ stĺpce sú hlavné") + ", žiadna neznáma nie je voľná. Homogénna sústava má len triviálne riešenie, preto je súbor LN.")
                else {
                    // a zero vector's column is never a pivot: its coefficient set to 1 is the combination the next line names
                    val free = if (hasZero) vs.indexOfFirst { it.isZero() } else (0 until m).first { it !in e.pivots }
                    val (lines, d) = backSubst(f, e, alphas, mapOf(free to Frac.ONE), homogeneous = true)
                    check(f.combo(d, vs).isZero())
                    isLn("Stĺpec ${free + 1} je vedľajší, jeho neznáma §${alphas[free]}§ je voľná. Zvolím §${alphas[free]}=1§ a ostatné dopočítam zdola:", lines + Formula("(${alphas.joinToString(",")})=${vtex(d)}"))
                    if (hasZero) isLn("Koeficient §1§ pri nulovom vektore a §0§ pri ostatných dá §\\theta§: netriviálna kombinácia, preto je súbor LZ.")
                    else isLn("Netriviálna kombinácia je rovná §\\theta§, preto je súbor LZ.", Terms.lin(d, texts) + "=\\theta")
                    if (m > n) isLn("Dalo sa to čakať, lebo $count v §$sp§ sú vždy LZ: hlavných stĺpcov je najviac §$n§ (Pozorovanie 2.32).")
                }
                if (gen) generates("V každom z §$n§ riadkov HST je hlavný stĺpec, nulový riadok nevznikne. Sústava s ľubovoľnou pravou stranou má preto vždy riešenie a súbor generuje §$sp§.")
                else {
                    val k = outside!!
                    generates("V HST ostal nulový riadok, preto pre niektoré pravé strany riešenie nie je. Napríklad pre §\\mathbf{e}_${k + 1}§ vyjde riadok §0=c§ s §c\\neq 0§:", gemLines(f.echelon(Mat.columns(vs, listOf(unit(n, k))), m)))
                    generates("Posledný stĺpec je hlavný, sústava nemá riešenie, teda §\\mathbf{e}_${k + 1}§ nie je kombináciou daných vektorov. Súbor §$sp§ negeneruje.")
                    if (m < n) generates("Dalo sa to čakať, lebo generujúci súbor §$sp§ má aspoň §$n§ vektory, toľko, aká je dimenzia (Tvrdenie 2.54).")
                }
                end(when (ln to gen) {
                    true to true -> "Súbor je LN a generuje §$sp§, preto je to báza (Definícia 2.44)."
                    true to false -> "Súbor je LN, ale negeneruje §$sp§, preto báza nie je (Definícia 2.44)."
                    false to true -> "Súbor generuje §$sp§, ale je LZ, preto báza nie je (Definícia 2.44)."
                    else -> "Súbor je LZ a negeneruje §$sp§, preto báza nie je (Definícia 2.44)."
                })
            }
        },
        hint = "Vektory do stĺpcov, GEM. LN: všetky stĺpce hlavné. Generuje: v každom riadku hlavný stĺpec.",
    )
}

/** Postup 2.45 for both properties at once, with the shortcut of Veta 2.55. */
val BasisDecidePostup = postup(
    "Vektory do stĺpcov, GEM" to "Vektory zapíš do stĺpcov a uprav GEM do HST; tá istá matica rozhodne o LN aj o generovaní.",
    "Je súbor LN?" to "LN je, keď sú všetky stĺpce hlavné: homogénna sústava má len triviálne riešenie.",
    "Generuje §T^n§?" to "Generuje, keď je v každom riadku hlavný stĺpec: sústava má riešenie pre každú pravú stranu.",
    "Záver" to "Báza je LN súbor, ktorý generuje. Pri §n§ vektoroch v §T^n§ stačí jedna vlastnosť, lebo §\\dim T^n=n§ (Veta 2.55).",
)

/* ---- What a span in R^2 and R^3 looks like (Poznámka 2.36; Kleprlík a Klouda, 5. prednáška) ---------------------- */

fun spanGeomExercise(r: Random): Exercise {
    val f = Field.R
    val n = if (r.nextInt(5) < 2) 2 else 3
    val zero = List(n) { Frac.ZERO }
    fun draw() = f.vec(List(n) { r.pick(-3, 3) })
    while (true) {
        // the dimension first, then at most one vector that adds nothing (the zero vector or a combination)
        val d = if (r.nextInt(10) == 0) 0 else r.pick(1, n)
        val base = List(d) { draw() }
        if (d > 0 && !f.isLN(base)) continue
        val extra = if (d == 0 || d == 3 || r.nextBoolean()) emptyList()
            else listOf(if (r.nextInt(3) == 0) zero else f.combo(List(d) { r.pick(-2, 2).let { if (it == 0) 2 else it }.q() }, base))
        val vs = (if (d == 0) listOf(zero) else base + extra).shuffled(r)
        if (vs.toSet().size < vs.size) continue
        if (vs.any { v -> v.any { kotlin.math.abs(it.num) > 9 } }) continue
        if (vs.any { v -> !v.isZero() && v.count { it.isZero } > 1 }) continue
        return spanGeom(f, n, vs, d)
    }
}

private fun spanGeom(f: Field, n: Int, vs: List<Vec>, d: Int): Exercise {
    val m = vs.size
    val sp = "\\mathbb{R}^$n"
    val span = "\\langle " + vs.joinToString(",") { vtex(it) } + "\\rangle"
    val e = f.echelon(Mat.columns(vs))
    val pivots = if (d == 0) emptyList() else e.pivots
    /** The vector that adds nothing, with why: the zero vector, or a combination of the others. */
    val redundant: Pair<Int, String>? = (0 until m).firstOrNull { it !in pivots && d > 0 }?.let { j ->
        if (vs[j].isZero()) j to "§${vtex(vs[j])}§ je nulový vektor, k obalu nič nepridá, lebo jeho násobky sú len §\\theta§."
        else {
            val c = f.solve(pivots.map { vs[it] }, vs[j])!!
            j to "§${vtex(vs[j])}=${Terms.lin(c, pivots.map { vtex(vs[it]) })}§ je " + (if (pivots.size == 1) "násobkom druhého vektora" else "kombináciou ostatných") + ", preto k obalu nič nepridá."
        }
    }
    val names = listOf("len počiatok §\\{\\theta\\}§", "priamka cez počiatok", if (n == 2) "celá rovina §$sp§" else "rovina cez počiatok", "celý priestor §$sp§")
    val options = names.take(n + 1).map(::m)
    val shape = when (d) {
        0 -> "Obal nulového vektora je množina len s počiatkom."
        1 -> "Obal nenulového vektora sú všetky jeho násobky, teda priamka cez počiatok."
        2 -> if (n == 2) "Obal dvoch LN vektorov z §\\mathbb{R}^2§ je celé §\\mathbb{R}^2§." else "Obal dvoch LN vektorov z §\\mathbb{R}^3§ je rovina, ktorá obsahuje počiatok."
        else -> "Obal troch LN vektorov z §\\mathbb{R}^3§ je celé §\\mathbb{R}^3§."
    }
    val dimWhy = when {
        d == 0 -> "Každá kombinácia nulového vektora je §\\theta§."
        d == 1 && m == 1 -> "Vektor je nenulový."
        d == 2 && m == 2 -> "Vektory nie sú jeden násobkom druhého, sú teda LN."
        d == 1 -> "Po GEM vyjde jeden hlavný stĺpec."
        else -> "Po GEM vyjdú §$d§ hlavné stĺpce."
    }
    fun bigger(k: Int) = if (k == 0) "Obal nie je len §\\{\\theta\\}§: obsahuje aj dané nenulové vektory." else "Obal je väčší: vektory nemajú len " + (if (k == 1) "jeden smer" else "dva smery") + ". $dimWhy"
    fun smaller() = redundant?.second ?: when {
        d == 0 -> "Daný vektor je nulový, každý jeho násobok je §\\theta§."
        m == 1 -> "Jeden nenulový vektor dá len svoje násobky."
        else -> "Obal je menší: §$m§ LN vektory dajú obal dimenzie §$m§, nie viac."
    }
    val why = listOf(names[d].replaceFirstChar { it.uppercase() } + ", lebo obal má dimenziu §$d§.", dimWhy, redundant?.second, shape).filterNotNull().joinToString(" ")
    // with the zero vector the shape only repeats the verdict: say instead what a line or a plane would need
    val zeroWhy = "Priamku cez počiatok dá až nenulový vektor a rovinu dva LN vektory; nulový vektor nedá ani jeden smer."
    val wrong = options.indices.filter { it != d }.associateWith { k -> m((if (k < d) bigger(k) else smaller()) + " " + (if (d == 0) zeroWhy else shape)) }

    return Exercise(
        "obal-geom", "Ako vyzerá obal", "Poznámka 2.36; Kleprlík a Klouda, 5. prednáška",
        m("Popište geometricky lineární obal §$span§ v prostoru §$sp§."),
        parts = listOf({ _ -> Part("Čo je to za množinu?", Answer.Choice(options, d, m(why), wrong)) }),
        postup = SpanGeomPostup,
        solution = { _ ->
            val (dim, look) = SpanGeomPostup
            SpanGeomPostup.solve {
                when {
                    d == 0 -> dim("Daný vektor je nulový a každý jeho násobok je §\\theta§, preto obal obsahuje len počiatok.", "$span=\\{\\theta\\}")
                    m == 1 -> dim("Obal jedného vektora sú všetky jeho násobky (Definícia 2.33). Vektor je nenulový, preto má obal dimenziu §1§.", "$span=\\{\\alpha${vtex(vs[0])}\\mid\\alpha\\in\\mathbb{R}\\}")
                    else -> {
                        dim("Vektory dám do stĺpcov a upravím GEM. " + Reasons.gemColumns, gemLines(e))
                        dim(Reasons.mainColumns(pivots) + " " + Reasons.dim("\\dim $span=$d"))
                        redundant?.let { (_, say) -> dim(say) }
                    }
                }
                look(when (d) {
                    0 -> "Obal je teda len počiatok §\\{\\theta\\}§, lebo obal nulového vektora je množina len s počiatkom (Poznámka 2.36)."
                    1 -> "Obal má dimenziu §1§, preto je to priamka cez počiatok: násobky jedného nenulového vektora ležia na jednej priamke (Poznámka 2.36)."
                    2 -> if (n == 2) "Obal má dimenziu §2§ ako celé §\\mathbb{R}^2§, preto je to celá rovina §\\mathbb{R}^2§ (Poznámka 2.36)."
                        else "Obal má dimenziu §2§, preto je to rovina cez počiatok, v ktorej ležia oba LN vektory (Poznámka 2.36)."
                    else -> "Obal má dimenziu §3§ ako celé §\\mathbb{R}^3§, preto je to celý priestor §\\mathbb{R}^3§ (Poznámka 2.36)."
                })
            }
        },
        hint = "Koľko z vektorov je naozaj LN? Nula smerov je len počiatok, jeden priamka, dva rovina.",
    )
}

/** Dimension of the span, then its shape (Poznámka 2.36). */
val SpanGeomPostup = postup(
    "Dimenzia obalu" to "Vektory do stĺpcov, GEM: počet hlavných stĺpcov je dimenzia. Nulový vektor a kombinácia ostatných nič nepridajú.",
    "Ako vyzerá" to "Dimenzia §0§ je len počiatok, §1§ priamka cez počiatok, §2§ rovina cez počiatok, §3§ celé §\\mathbb{R}^3§.",
)

/* ---- The standard basis of T^n and coordinates in it (Definícia 2.48; Klouda, 5. prednáška) ---------------------- */

fun standardBasisExercise(r: Random): Exercise {
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(listOf(2, 3, 5, 7).random(r))
    val p = (f as? Field.Zp)?.p
    val n = r.pick(2, 3) // E_4 as one tuple would not fit a 320 dp option
    val sp = fieldPow(f, n)
    val units = List(n) { unit(n, it) }
    val x = generateSequence { f.vec(List(n) { if (p != null) r.pick(0, p - 1) else r.pick(-5, 7) }) }.first { v -> !v.isZero() && v.count { it.isZero } <= 1 && v.toSet().size > 1 }
    // the standard basis, the same vectors in reverse, one missing, ones on and above the diagonal
    val sets = listOf(units, units.reversed(), units.dropLast(1), List(n) { i -> List(n) { j -> if (j <= i) Frac.ONE else Frac.ZERO } })
    val order = (0 until 4).shuffled(r)
    val en = "\\mathbf{e}_$n"
    val all = "(" + (1..n).joinToString(",") { "\\mathbf{e}_$it" } + ")"
    val wrongBy = mapOf(
        1 to "Je to báza, ale nie štandardná: poradie je obrátené. Báza je usporiadaný súbor a štandardná je §$all§ práve v tomto poradí (Definícia 2.48).",
        2 to "Chýba §$en§, takže to nie je ani báza: " + (if (n == 2) "jeden vektor §$sp§ negeneruje" else "§${n - 1}§ vektory §$sp§ negenerujú") + ", lebo jeho dimenzia je §$n§ (Tvrdenie 2.54).",
        3 to "Je to báza, ale nie štandardná: vektor štandardnej bázy má jedinú jednotku a inde nuly (Definícia 2.48).",
    )
    val (pick, coords) = StandardBasisPostup
    val c = Formula(Terms.lin(x, List(n) { "\\mathbf{e}_${it + 1}" }) + "=" + vtex(x))
    return Exercise(
        "standardna-baza", "Štandardná báza", "Definícia 2.48; Klouda, 5. prednáška; Krásenský, 6. prednáška",
        m("Napište standardní bázi prostoru §$sp§ a souřadnice vektoru §\\mathbf{x}=${vtex(x)}§ vzhledem k ní."),
        parts = listOf(
            { _ -> Part(m("Ktorý súbor je štandardná báza §$sp§?"), Answer.Choice(order.map { m("§${vstex(sets[it])}§") }, order.indexOf(0),
                m("Štandardná báza je §$all§: §\\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly (Definícia 2.48). Obrátené poradie je iný súbor, súbor bez §$en§ negeneruje a vektory s viacerými jednotkami nie sú §\\mathbf{e}_i§."),
                wrongBy.mapKeys { (k, _) -> order.indexOf(k) }.mapValues { m(it.value) }),
                hint = m("§\\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly; záleží aj na poradí."), aim = 0) },
            { _ -> Part(m("Súradnice §\\mathbf{x}=${vtex(x)}§ vzhľadom na štandardnú bázu:"), Answer.Numbers(List(n) { "\\alpha_${it + 1}" }, p, x, m("§${vtex(x)}§"),
                diagnose = diagnosis(f, x, listOf(x.reversed() to "Súradnice si zapísal v opačnom poradí: §i§-tá súradnica patrí k §\\mathbf{e}_i§.")) { _ ->
                    "V štandardnej báze sú súradnice zložky vektora: §\\mathbf{x}=x_1\\mathbf{e}_1+\\dots+x_${n}\\mathbf{e}_$n§, koeficient pri §\\mathbf{e}_i§ je §x_i§."
                }),
                hint = m("Napíš §\\mathbf{x}§ ako kombináciu §\\mathbf{e}_1,\\dots,$en§; koeficienty sú súradnice."), aim = 1) },
        ),
        postup = StandardBasisPostup,
        solution = { _ ->
            StandardBasisPostup.solve {
                pick("Vektor §\\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly (Definícia 2.48). Štandardná báza je súbor týchto vektorov v poradí od §\\mathbf{e}_1§.", "\\mathcal{E}_$n=${vstex(units)}")
                coords("Kombinácia štandardnej bázy so zložkami §\\mathbf{x}§ ako koeficientmi dá §\\mathbf{x}§, preto sú súradnice zložky vektora.", c, Formula("(\\mathbf{x})_{\\mathcal{E}_$n}=${vtex(x)}"))
            }
        },
        hint = m("§\\mathbf{e}_i§ má jednotku na §i§-tom mieste. Súradnice v štandardnej báze sú zložky vektora."),
    )
}

val StandardBasisPostup = postup(
    "Štandardná báza" to "§\\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly; báza je §(\\mathbf{e}_1,\\dots,\\mathbf{e}_n)§ v tomto poradí.",
    "Súradnice v nej" to "Súradnice vektora v štandardnej báze sú jeho zložky, lebo §\\mathbf{x}=x_1\\mathbf{e}_1+\\dots+x_n\\mathbf{e}_n§.",
)

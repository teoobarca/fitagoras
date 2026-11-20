package dev.hamper.tabula.data

import dev.hamper.tabula.la.FiniteSpace
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Subspace
import dev.hamper.tabula.la.ZVec
import kotlin.random.Random
import dev.hamper.tabula.compat.*

/*
 * Counting and listing in Z_p^n, from one model (la/FiniteSpace.kt): how many vectors, bases and subspaces, and all
 * subspaces of a small space without repeats. Every number asked is computed by the model, so it can never disagree
 * with the solution. Each solution says what is counted and what counts as the same object: vectors are
 * points, a basis is an ordered tuple of vectors, a subspace is a set of vectors that many bases generate.
 *
 * Sources: Skriptá, kapitola 2, otázky pri Poznámke 2.67 (koľko prvkov má a koľko je podpriestorov dimenzie 2
 * v Z_5^3) a pri Vete 2.60 (koľko je báz Z_5^3); MARAST #13691 (všetky netriviálne podpriestory Z_3^2), #14159
 * (počet podpriestorov Z_3^2). Statements are generated variants only: no item of a graded quiz that is still open
 * (docs/CONTENT.md; OpenQuizTest), and Z_2^3 is left out of the statements.
 *
 * Solutions are written under named subgoals ("**Čo počítam?** ..."), one bold title per subgoal in order, so the
 * subgoal-labelled solutions (Postup) can take them over line by line.
 */

/** A line of a solution as written below: a sentence (a bold title opens the next subgoal) or a formula. */
private sealed interface Ln
private class Txt(val s: String) : Ln
private class Disp(val tex: String, val note: String?) : Ln
private fun t(s: String) : Ln = Txt(m(s))
private fun x(s: String, note: String? = null): Ln = Disp(s, note?.let(::m))

/**
 * The lines under the subgoals of [this] Postup in order: a sentence opening with a bold title ("**Čo počítam?** ...")
 * starts the next subgoal (the title itself is the goal card's), a numbered one ("**2. Platí.**") stays under the
 * current one, formulas belong to the sentence before them.
 */
private fun Postup.from(lines: List<Ln>): Solution = solve {
    var at = -1
    for (l in lines) when (l) {
        is Txt -> {
            val title = Regex("""^\*\*(.+?)\*\*\s*""").find(l.s)
            val numbered = title != null && title.groupValues[1].first().isDigit()
            if (title != null && !numbered) at++
            val say = if (title != null && !numbered) l.s.substring(title.range.last + 1).ifBlank { title.groupValues[1] } else l.s
            aims[at.coerceIn(0, aims.lastIndex)](say)
        }
        is Disp -> more(Formula(l.tex, l.note))
    }
}

/** How many subspaces of a dimension: bases in the whole space divided by the bases of one subspace. */
val ZpChainPostup = postup(
    "Čo počítam?" to "Podpriestory ako množiny vektorov; dve bázy tej istej množiny sú jeden podpriestor.",
    "LN súbory v celom priestore" to "Prvý vektor nenulový, každý ďalší mimo obalu predchádzajúcich; voľby vynásob.",
    "Bázy jedného podpriestoru" to "Rovnako, ale len z vektorov jedného podpriestoru §P§.",
    "Záver" to "Vydeľ: každý podpriestor je v počte LN súborov toľkokrát, koľko má báz.",
)

val ZpVsPostup = postup(
    "Čo počítam?" to "Vektor je bod, podpriestor je celá množina bodov: dve rôzne otázky.",
    "Vektory v §P§" to "Podpriestor dimenzie §d§ v §\\mathbb{Z}_p^n§ má §p^d§ vektorov.",
    "LN súbory v celom priestore" to "Prvý vektor nenulový, každý ďalší mimo obalu predchádzajúcich; voľby vynásob.",
    "Bázy jedného podpriestoru" to "Rovnako, ale len z vektorov jedného podpriestoru.",
    "Počet podpriestorov" to "Vydeľ: každý podpriestor je zarátaný toľkokrát, koľko má báz.",
)

val ZpAllPostup = postup(
    "Čo počítam?" to "Podpriestory ako množiny, každý raz, všetkých dimenzií.",
    "Triviálne podpriestory" to "§\\{\\theta\\}§ a celý priestor, po jednom.",
    "Každá dimenzia zvlášť" to "LN súbory v celom priestore vydeľ bázami jedného podpriestoru.",
    "Záver" to "Sčítaj počty všetkých dimenzií.",
)

val ZpListPostup = postup(
    "Čo hľadám?" to "Podpriestory ako množiny; rôzne bázy tej istej množiny sú jeden podpriestor.",
    "Koľko ich je?" to "Spočítaj ich vopred, aby si vedel, kedy je zoznam úplný.",
    "Zoznam" to "Pri každom obale vypíš jeho množinu a porovnaj množiny, nie generátory.",
    "Úplnosť" to "Počet rôznych množín sa musí rovnať vypočítanému počtu.",
)

private fun n1(v: Long) = listOf(Frac.of(v))
private fun Random.of(vararg xs: Pair<Int, Int>) = xs[nextInt(xs.size)]

/** Small spaces only: every count fits the keypad (6 digits) and every enumeration stays readable. */
private val CountSpaces = listOf(2 to 2, 2 to 3, 2 to 4, 3 to 2, 3 to 3, 5 to 2, 5 to 3, 7 to 2)

private fun dimWord(d: Int) = "dimenzie §$d§"
private fun dimCz(d: Int) = "dimenze §$d§"
/** Slovak numeral agreement: 1 vektor, 2 až 4 vektory, 5 a viac vektorov. */
private fun skVectors(k: Int) = when (k) { 1 -> "vektor"; in 2..4 -> "vektory"; else -> "vektorov" }
/** What the numerator of the subspace count counts: nonzero vectors for lines, ordered LN d-tuples otherwise. */
private fun tuplesWord(d: Int) = if (d == 1) "nenulové vektory" else "usporiadané LN §$d§-tice"
private fun pl(k: Long, one: String, few: String, other: String) = when (k) { 1L -> one; in 2L..4L -> few; else -> other }

/** "(p^n-p^0)(p^n-p^1)..." with the values: the ordered LN d-tuples in a space of dimension m. */
private fun productTex(p: Int, m: Int, d: Int): String = if (d == 1) "${Terms.pow(p.toLong(), m)}-1" else (0 until d).joinToString("") { i -> "(${Terms.pow(p.toLong(), m)}-${powTex(p, i)})" }
private fun powTex(p: Int, i: Int) = when (i) { 0 -> "1"; 1 -> "$p"; else -> "${Terms.pow(p.toLong(), i)}" }
/** "a/b" for products a and b in running text, each side bracketed when it is not one bracket already, so the division is unambiguous ("(3^2-1)/(3-1)", not "3^2-1/3-1"). */
private fun ratioTex(a: String, b: String): String {
    fun one(x: String) = x.startsWith("(") && x.endsWith(")") && x.count { it == '(' } == 1
    val num = if (a.startsWith("(")) a else "($a)"
    val den = if (one(b)) b else if (b.startsWith("(")) "[$b]" else "($b)"
    return "$num/$den"
}


/* ---- 1. How many: vectors, ordered bases, subspaces ------------------------------------------------------------- */

private const val SrcCount = "Skriptá, otázky pri Poznámke 2.67 a pri Vete 2.60; MARAST #14159"

fun zpCountExercise(r: Random): Exercise {
    val (p, n) = CountSpaces[r.nextInt(CountSpaces.size)]
    val s = FiniteSpace(p, n)
    return when (r.nextInt(10)) {
        in 0..5 -> subspaceChain(s, r.nextInt(1, n))
        in 6..7 -> vectorsVersusSubspaces(s, r.nextInt(1, n))
        else -> allSubspaces(s)
    }
}

/** Ordered LN d-tuples, the bases of one subspace, and the subspaces: the textbook's way, each number named. */
private fun subspaceChain(s: FiniteSpace, d: Int): Exercise {
    val p = s.p; val n = s.n
    val ln = s.orderedLN(d).toLong(); val bases = s.basesOf(d).toLong(); val count = s.subspaceCount(d).toLong()
    val sp = s.spaceTex()
    val tuple = if (d == 1) "nenulových vektorov" else "usporiadaných LN §$d§-tíc"
    return Exercise(
        "zp-pocet", "Koľko podpriestorov", SrcCount,
        m("Kolik existuje podprostorů ${dimCz(d)} v prostoru §$sp§?"),
        parts = listOf(
            { _ -> Part(m(if (d == 1) "Koľko je nenulových vektorov v §$sp§? (Každý je báza nejakej priamky.)" else "Koľko je usporiadaných LN §$d§-tíc vektorov v §$sp§? (Každá je báza nejakého podpriestoru ${dimWord(d)}.)"),
                Answer.Numbers(listOf("\\#"), null, n1(ln), "$${productTex(p, n, d)}=$ln$", diagnose = lnTuplesDiagnosis(s, d)),
                hint = m(if (d == 1) "Báza priamky je jeden nenulový vektor: rátaj všetky vektory okrem §\\theta§." else "Prvý vektor je ľubovoľný nenulový, každý ďalší mimo obalu predchádzajúcich; počty volieb vynásob."), aim = 1) },
            { _ -> Part(m("Koľko báz má **jeden** podpriestor §P§ ${dimWord(d)}?"),
                Answer.Numbers(listOf("\\#"), null, n1(bases), "$${productTex(p, d, d)}=$bases$", diagnose = basesOfOneDiagnosis(s, d)),
                hint = m("Rovnako ako v celom priestore, ale vektory vyberáš len z §P§; ten má §p^d§ prvkov."), aim = 2) },
            { _ -> Part(m("Koľko je teda podpriestorov ${dimWord(d)} v §$sp§?"),
                Answer.Numbers(listOf("\\#"), null, n1(count), "$$ln/$bases=$count$", diagnose = subspaceCountDiagnosis(s, d)),
                after = m("Pri počítaní §$ln§ $tuple sme každý podpriestor zarátali §$bases§-krát, raz za každú jeho bázu."),
                hint = m("V počte z prvej časti je každý podpriestor zarátaný toľkokrát, koľko má báz; vydeľ."), aim = 3) },
        ),
        postup = ZpChainPostup,
        solution = { ZpChainPostup.from(chainLines(s, d)) },
        hint = "Počítaj bázy, nie podpriestory: koľko LN súborov je v celom priestore a koľko z nich generuje ten istý podpriestor.",
    )
}

private fun chainLines(s: FiniteSpace, d: Int): List<Ln> {
    val p = s.p; val n = s.n; val sp = s.spaceTex()
    val ln = s.orderedLN(d); val bases = s.basesOf(d); val count = s.subspaceCount(d)
    val example = s.subspaces(d).last()
    return listOf(
        t("**Čo počítam?** Podpriestory ${dimWord(d)}, teda množiny vektorov. Dve rôzne bázy, ktoré generujú tú istú množinu, dávajú **ten istý** podpriestor."),
        t("Napríklad §${example.spanTex()}=${if (example.elements.size <= 9) example.setTex() else "P"}§ má §${bases}§ ${pl(bases.toLong(), "bázu", "bázy", "báz")}, ale je to jeden podpriestor."),
        t(if (d == 1) "**Koľko je nenulových vektorov v celom priestore?** Báza priamky je jeden nenulový vektor a každý nenulový vektor je bázou svojej priamky:"
            else "**Koľko je usporiadaných LN §$d§-tíc v celom priestore?** Každý podpriestor ${dimWord(d)} má bázu s §$d§ vektormi a každý LN súbor §$d§ vektorov je bázou svojho obalu. Prvý vektor je ľubovoľný nenulový, každý ďalší mimo obalu predchádzajúcich, lebo vektor v obale by súbor urobil LZ a vektor mimo neho LN zachová (Veta 2.42):"),
        x("${productTex(p, n, d)}=$ln", if (d == 1) "nenulové vektory §$sp§" else "usporiadané LN §$d§-tice v §$sp§"),
        t(if (d == 1) "**Koľkokrát je každá priamka zarátaná?** Toľkokrát, koľko má nenulových vektorov: §${p}-1§. Pre nenulový §\\mathbf{v}§ všetky §\\alpha\\mathbf{v}§ s §\\alpha\\neq 0§ generujú tú istú priamku:"
            else "**Koľkokrát je každý podpriestor zarátaný?** Toľkokrát, koľko má báz. Podpriestor ${dimWord(d)} má §${Terms.pow(p.toLong(), d)}§ prvkov a jeho bázy sa vyberajú rovnako, len v ňom:"),
        x("${productTex(p, d, d)}=$bases", "bázy jedného podpriestoru"),
        t("**Záver.** Podpriestorov ${dimWord(d)} je"),
        x("\\frac{$ln}{$bases}=$count", if (p == 5 && n == 3 && d == 2) "podpriestorov dimenzie §2§ v §\\mathbb{Z}_5^3§, ako v skriptách" else "rovnaký postup ako v skriptách pre §\\mathbb{Z}_5^3§"),
    )
}

/** The count of subspaces of dimension [d] of Z_p^n step by step: the generator and the lesson's worked example. */
fun zpChainSolution(p: Int, n: Int, d: Int): Solution = ZpChainPostup.from(chainLines(FiniteSpace(p, n), d))

/** |P| for one subspace against the number of such subspaces: two different numbers about two different objects. */
private fun vectorsVersusSubspaces(s: FiniteSpace, d: Int): Exercise {
    val p = s.p; val sp = s.spaceTex()
    val size = s.vectorsIn(d).toLong(); val count = s.subspaceCount(d).toLong()
    return Exercise(
        "zp-pocet", "Vektory alebo podpriestory", SrcCount,
        m("Uvažujme §P\\ssub $sp§, §\\dim P=$d§. Kolik vektorů obsahuje §P§? Kolik existuje různých podprostorů ${dimCz(d)} v §$sp§?"),
        parts = listOf(
            { _ -> Part(m("Počet **vektorov** v §P§:"), Answer.Numbers(listOf("|P|"), null, n1(size), "$${Terms.pow(p.toLong(), d)}=$size$", diagnose = sizeDiagnosis(p, s.n, d)),
                hint = m("Každý vektor §P§ má v báze jediné súradnice: vektorov je toľko, koľko je §d§-tíc súradníc zo §\\mathbb{Z}_p§."), aim = 1) },
            { _ -> Part(m("Počet **podpriestorov** ${dimWord(d)} v §$sp§:"), Answer.Numbers(listOf("\\#"), null, n1(count), "$$count$", diagnose = subspaceCountDiagnosis(s, d)),
                hint = m("Spočítaj usporiadané LN súbory v celom priestore a vydeľ ich počtom báz jedného podpriestoru: každý podpriestor je v nich zarátaný toľkokrát, koľko má báz."), aim = 4) },
        ),
        postup = ZpVsPostup,
        solution = { ZpVsPostup.from(listOf(
            t("**Čo počítam?** Prvá otázka počíta vektory v jednej množine §P§, druhá počíta celé množiny. Sú to iné objekty a iné čísla."),
            t("**Vektory v §P§.** Každý vektor §P§ má v báze jediné súradnice, lebo báza generuje a je LN (Veta 2.66). Vektorov je preto toľko ako možných súradníc: §${Terms.pow(p.toLong(), d)}=$size§ (Poznámka 2.67)."),
        ) + chainLines(s, d).drop(2)) },
        hint = "Vektor je bod, podpriestor je celá množina bodov. Pri podpriestoroch počítaj bázy a vydeľ.",
    )
}

/** All subspaces of a small space, trivial ones included, by dimension. */
private fun allSubspaces(s: FiniteSpace): Exercise {
    val sp = s.spaceTex()
    val counts = (0..s.n).map { s.subspaceCount(it).toLong() }
    val total = counts.sum()
    return Exercise(
        "zp-pocet", "Všetky podpriestory", "MARAST #14159; Skriptá, Definice 2.9 (triviálne podpriestory)",
        m("Kolik existuje všech podprostorů prostoru §$sp§ (včetně triviálních)?"),
        parts = listOf({ _ -> Part(m("Počet všetkých podpriestorov §$sp§:"), Answer.Numbers(listOf("\\#"), null, n1(total), "$${counts.joinToString("+")}=$total$", diagnose = allCountDiagnosis(s))) }),
        postup = ZpAllPostup,
        solution = { ZpAllPostup.from(listOf(
            t("**Čo počítam?** Podpriestory ako množiny, každý raz. Dimenzia môže byť §0,1,\\dots,${s.n}§."),
            t("**Triviálne.** §\\{\\theta\\}§ (dimenzia §0§) a celý §$sp§ (dimenzia §${s.n}§): po jednom."),
        ) + (1 until s.n).map { d -> t((if (d == 1) "**Každá dimenzia zvlášť.** " else "") + "Dimenzia §$d§: §${ratioTex(productTex(s.p, s.n, d), productTex(s.p, d, d))}=${counts[d]}§: ${tuplesWord(d)} v celom priestore delené bázami jedného podpriestoru.") } + listOf(
            t("**Záver.** Spolu §${counts.joinToString("+")}=$total§."),
        )) },
        hint = "Rozdeľ podľa dimenzie a nezabudni na {θ} a celý priestor.",
    )
}

/* ---- diagnoses: every candidate is a number the model computes for a named mistake ---------------------------- */

private fun lnTuplesDiagnosis(s: FiniteSpace, d: Int): (List<Frac>) -> String {
    val p = s.p.toLong(); val pn = s.vectorsIn(s.n).toLong()
    return diagnosis(dev.hamper.tabula.la.Field.R, n1(s.orderedLN(d).toLong()), listOf(
        n1((1..d).fold(1L) { a, _ -> a * pn }) to "Rátal si aj §\\theta§ a závislé súbory. Prvý vektor musí byť nenulový a každý ďalší mimo obalu predchádzajúcich.",
        n1((1..d).fold(1L) { a, _ -> a * (pn - 1) }) to "Nenulový nestačí: druhý vektor nesmie byť násobkom prvého, inak súbor nie je LN (Veta 2.42).",
        n1(s.orderedLN(d).toLong() / (1..d).fold(1L) { a, k -> a * k }) to "Delil si poradím. Báza je usporiadaný súbor, §(\\mathbf{u},\\mathbf{v})§ a §(\\mathbf{v},\\mathbf{u})§ sú dve bázy.",
        n1(s.vectorsIn(d).toLong()) to "§${Terms.pow(p.toLong(), d)}§ je počet vektorov jedného podpriestoru, nie počet LN súborov.",
    )) { if (d == 1) "Nenulových vektorov je §${s.p}^{${s.n}}-1§: všetky okrem §\\theta§." else "Prvý vektor: §${s.p}^{${s.n}}-1§ nenulových. Každý ďalší mimo obalu tých, čo už máš: §${s.p}^{${s.n}}-${s.p}^{k}§ možností pri §k§ vybraných." }
}

private fun basesOfOneDiagnosis(s: FiniteSpace, d: Int): (List<Frac>) -> String {
    val p = s.p
    return diagnosis(dev.hamper.tabula.la.Field.R, n1(s.basesOf(d).toLong()), listOf(
        n1(1) to "Báza podpriestoru nie je jediná. Jediné sú súradnice vektora v pevnej báze, nie báza.",
        n1(s.vectorsIn(d).toLong()) to "§${Terms.pow(p.toLong(), d)}§ je počet vektorov v §P§. Báz je toľko, koľko ${if (d == 1) "nenulových vektorov v ňom je" else "LN §$d§-tíc sa z nich dá zostaviť"}.",
        n1(s.orderedLN(d).toLong()) to "To sú ${if (d == 1) "nenulové vektory" else "LN §$d§-tice"} v celom §${s.spaceTex()}§. Bázy jedného §P§ vyberáš len z jeho §${Terms.pow(p.toLong(), d)}§ vektorov.",
        n1(s.vectorsIn(d).toLong() - 1) to "To je len voľba prvého vektora. Ďalšie musia byť mimo obalu predchádzajúcich, voľby sa násobia.",
    )) { "Rovnako ako v celom priestore, len v §P§ s §${Terms.pow(p.toLong(), d)}§ vektormi: §${productTex(p, d, d)}§." }
}

private fun subspaceCountDiagnosis(s: FiniteSpace, d: Int): (List<Frac>) -> String {
    val p = s.p
    val ln = s.orderedLN(d).toLong(); val bases = s.basesOf(d).toLong()
    return diagnosis(dev.hamper.tabula.la.Field.R, n1(s.subspaceCount(d).toLong()), listOf(
        n1(ln) to "To je počet ${if (d == 1) "nenulových vektorov" else "usporiadaných LN §$d§-tíc"}, nie podpriestorov. Každý podpriestor je v ňom zarátaný §$bases§-krát, raz za každú svoju bázu.",
        n1(1) to "Podpriestor ${dimWord(d)} nie je jediný. Napríklad §\\langle\\mathbf{e}_1,\\dots,\\mathbf{e}_{$d}\\rangle§ a §\\langle\\mathbf{e}_2,\\dots,\\mathbf{e}_{${d + 1}}\\rangle§ sú dva rôzne.",
        n1(s.vectorsIn(d).toLong()) to "§${Terms.pow(p.toLong(), d)}§ je počet vektorov v jednom podpriestore. Pýtame sa, koľko je rôznych podpriestorov.",
        n1(s.vectorsIn(s.n).toLong() - 1) to "Nenulových vektorov je toľko, ale nie každý dáva iný podpriestor. Násobky §\\alpha\\mathbf{v}§ s §\\alpha\\neq 0§ generujú ten istý.",
        n1(ln / (1..d).fold(1L) { a, k -> a * k }) to "Delil si len poradím vektorov. Ten istý podpriestor generujú aj úplne iné súbory, nielen preusporiadané.",
    )) { "${tuplesWord(d).replaceFirstChar { it.uppercase() }} v celom priestore (§$ln§) vydeľ počtom báz jedného podpriestoru (§$bases§)." }
}

private fun allCountDiagnosis(s: FiniteSpace): (List<Frac>) -> String {
    val counts = (0..s.n).map { s.subspaceCount(it).toLong() }
    val total = counts.sum()
    return diagnosis(dev.hamper.tabula.la.Field.R, n1(total), listOf(
        n1(total - 2) to "Chýbajú triviálne podpriestory §\\{\\theta\\}§ a celý priestor. Zadanie ich chce tiež.",
        n1(total - 1) to "Jeden triviálny podpriestor chýba. Rátaj aj §\\{\\theta\\}§ aj celý priestor.",
        n1(s.vectorsIn(s.n).toLong()) to "To je počet vektorov, nie podpriestorov. Podpriestor je množina vektorov.",
    )) { "Spočítaj podpriestory každej dimenzie zvlášť: §${counts.joinToString(", ")}§ pre dimenzie §0§ až §${s.n}§, a sčítaj." }
}

/* ---- 2. All subspaces, each exactly once ---------------------------------------------------------------------- */

private const val SrcList = "MARAST #13691 (2. písomka, ukážka); Skriptá, Definice 2.9"

/** A random generating tuple of [sub] (a different basis than the canonical one when there is one). */
private fun randomBasis(s: FiniteSpace, sub: Subspace, r: Random): List<ZVec> {
    val els = sub.elements.toList()
    while (true) {
        val b = List(sub.dim) { els[r.nextInt(els.size)] }
        if (s.rank(b) == sub.dim) return b
    }
}

private fun genTex(s: FiniteSpace, b: List<ZVec>) = b.joinToString(",", "\\langle ", "\\rangle") { s.tex(it) }

fun zpListExercise(r: Random): Exercise {
    // (p, n, d): Z_3^2 and Z_5^2 lines, Z_2^3 planes. Z_2 lines have one generator each, so nothing could repeat.
    val (p, n, d) = listOf(Triple(3, 2, 1), Triple(3, 2, 1), Triple(5, 2, 1), Triple(2, 3, 2)).random(r)
    val s = FiniteSpace(p, n)
    val subs = s.subspaces(d).shuffled(r)
    // one option per subspace, then up to 3 more generating a subspace already listed, with another basis
    val opts = ArrayList<Pair<List<ZVec>, Subspace>>()
    for (u in subs) opts += randomBasis(s, u, r) to u
    val dupN = minOf(3, (10 - opts.size).coerceAtLeast(2))
    var tries = 0
    while (opts.size < subs.size + dupN && tries++ < 500) {
        val u = subs[r.nextInt(subs.size)]
        val b = randomBasis(s, u, r)
        if (opts.none { genTex(s, it.first) == genTex(s, b) }) opts += b to u
    }
    opts.shuffle(r)
    val texts = opts.map { "$" + genTex(s, it.first) + "$" }
    val sp = s.spaceTex()
    val all = subs.toSet()
    val whole = if (n == 2) "netriviální podprostory" else "podprostory dimenze §$d§"
    fun why(sel: Set<Int>): String {
        val picked = sel.map { opts[it] }
        val repeats = picked.groupBy { it.second }.filter { it.value.size > 1 }
        val missing = all - picked.map { it.second }.toSet()
        return when {
            repeats.isNotEmpty() -> repeats.entries.first().let { (u, ps) ->
                "§${genTex(s, ps[0].first)}§ a §${genTex(s, ps[1].first)}§ sú ten istý podpriestor. Oba sú množina §${u.setTex()}§, len s inou bázou."
            }
            missing.isNotEmpty() -> "Niektoré chýbajú, napríklad §${missing.first().setTex()}§. Chýbajúcich je §${missing.size}§ zo §${all.size}§ a každý má byť v zozname práve raz."
            else -> "Správne, každý podpriestor je v zozname práve raz. Žiaden nechýba: počet §${all.size}§ sedí s výpočtom."
        }.let(::m)
    }
    return Exercise(
        "zp-vypis", "Všetky podpriestory, každý raz", SrcList,
        m("Najděte všechny $whole prostoru §$sp§."),
        parts = listOf(
            { _ -> Part(m("Koľko ich je?"), Answer.Numbers(listOf("\\#"), null, n1(all.size.toLong()), "$${all.size}$", diagnose = subspaceCountDiagnosis(s, d)),
                hint = m(if (d == 1) "Každý nenulový vektor leží v práve jednej priamke a každá priamka má §p-1§ nenulových vektorov." else "Usporiadané LN súbory v celom priestore vydeľ počtom báz jedného podpriestoru."), aim = 1) },
            { _ -> Part(m("Vyber zoznam, v ktorom je každý z nich **práve raz**. Niektoré obaly sú ten istý podpriestor s inou bázou."),
                Answer.Pick(texts, 1..texts.size, { sel -> sel.map { opts[it].second }.let { it.size == all.size && it.toSet() == all } }, ::why),
                hint = m("Pre každý obal vypíš jeho množinu a porovnaj množiny, nie generátory. Rôznych množín má byť toľko, koľko si vypočítal."), aim = 2) },
        ),
        postup = ZpListPostup,
        solution = { ZpListPostup.from(listOf(
            t("**Čo hľadám?** Množiny vektorov uzavreté na súčet a násobok, ${if (n == 2) "okrem §\\{\\theta\\}§ a celého §$sp§ (tie sú triviálne, Definice 2.9)" else "s dimenziou §$d§"}. Rôzne bázy tej istej množiny sú jeden podpriestor."),
            t("**Koľko ich je?** ${if (d == 1) "Každý nenulový vektor leží v práve jednej priamke a každá priamka má §${p}-1§ nenulových vektorov: §(${Terms.pow(p.toLong(), n)}-1)/(${p}-1)=${all.size}§." else "§${ratioTex(productTex(p, n, d), productTex(p, d, d))}=${all.size}§: ${tuplesWord(d)} v celom priestore delené bázami jedného podpriestoru."}"),
            t("**Zoznam.** Každý podpriestor ako množina (z nej je vidno, ktoré obaly sú ten istý):"),
        ) + s.subspaces(d).map { u -> x(u.spanTex() + "=" + u.setTex()) } + listOf(
            t("**Úplnosť.** Podpriestorov je §${all.size}§, žiadne dva nie sú rovnaké a výpočet hovorí §${all.size}§: nič nechýba."),
        )) },
        hint = "Pre každý obal vypíš jeho množinu a porovnaj množiny, nie generátory.",
    )
}

val FiniteSpaceGenerators: Map<String, (Random) -> Exercise> = mapOf(
    "zp-pocet" to ::zpCountExercise,
    "zp-vypis" to ::zpListExercise,
)

/** Every subspace of dimension [d], listed as sets, with the count that shows nothing is missing (the student's own list, zp-zoznam). */
fun subspaceListSolution(s: FiniteSpace, d: Int): Solution = ZpListPostup.solve {
    val (what, howMany, list, complete) = ZpListPostup
    val p = s.p; val n = s.n; val sp = s.spaceTex()
    val all = s.subspaces(d)
    what(if (n == 2 && d == 1) "Netriviálne podpriestory §$sp§ sú tie s dimenziou §0<d<2§, teda priamky §\\langle\\mathbf{v}\\rangle§ s §\\mathbf{v}\\neq\\theta§ (Definice 2.9)."
        else "Hľadám podpriestory dimenzie §$d§ ako množiny vektorov. Rôzne bázy tej istej množiny sú jeden podpriestor.")
    howMany(if (d == 1) "Každý nenulový vektor leží v práve jednej priamke a priamka má §${p - 1}§ ${pl((p - 1).toLong(), "nenulový vektor", "nenulové vektory", "nenulových vektorov")}."
        else "Usporiadané LN dvojice v celom priestore delím počtom báz jednej roviny.",
        Formula(if (d == 1) "(${Terms.pow(p.toLong(), n)}-1)/(${p}-1)=${all.size}" else "${ratioTex(productTex(p, n, d), productTex(p, d, d))}=${all.size}"))
    list("Každý podpriestor raz, s jeho prvkami; z množiny je vidno, ktoré obaly sú ten istý.", all.map { u -> Formula(u.spanTex() + "=" + u.setTex()) })
    complete("Podpriestorov je §${all.size}§, žiadne dva nie sú rovnaké a výpočet hovorí §${all.size}§: nič nechýba.")
}

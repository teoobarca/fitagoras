package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * Past 2nd tests of BI-LA1 (fit-wiki.cz transcriptions in the course sources outside the repository, zdroje/testy-la1-2.md), the task
 * text word for word. Every number in an answer or a solution is computed below by the same code as the exercises,
 * not typed in. The papers give points per problem only: the split of a problem's points among its parts and the
 * rubric lines are our estimate from the official instructions, not the graders' key.
 *
 * A task is split into parts where ¦ stands (between sentences, or where the task itself moves to the next thing);
 * the parts joined by spaces are the task word for word (PapersTest checks it).
 */

const val Header = "Řádně vysvětlete svůj postup tak, aby jej pochopil čtenář, který pouze zná definice a tvrzení z přednášky: Pokud budete například řešit soustavu rovnic, napište, co jsou proměnné a odkud se vzaly koeficienty. Vysvětlete také, jak přesně z výpočtu plynou závěry, které uděláte."
const val HeaderP1 = "V tomto příkladu nemusíte své výpočty komentovat. Avšak znalý čtenář ve vašich výpočtech musí být schopen poznat, jak jste se k výsledku dobrali."

private fun pc(vararg xs: Int) = xs.map { Poly.of(it) }

/** The Czech task cut where ¦ stands. */
private fun cut(t: String): List<String> = m(t).split('¦').map { it.trim() }

private fun units(vs: List<Int>) = vs.joinToString(",") { "\\mathbf{e}_${it + 1}" }
private fun zp(f: Field.Zp, n: Int = 4) = "\\mathbb{Z}_{${f.p}}^{$n}"

/* ---- Problem 1, 7 points: the parameter --------------------------------------------------------------------- */

/** What to go through again for each kind of problem 1: the lesson step that teaches it. */
private fun paramReview(kind: ParamKind) = when (kind) {
    ParamKind.LN -> "theorem:t2-27"
    ParamKind.BASIS -> "gen:param-basis"
    ParamKind.GENERATES -> "gen:param-gen"
    ParamKind.SPAN -> "gen:param-span"
}

private fun paramRubric(critical: String) = listOf(
    Rubric("Matica: vektory v stĺpcoch, neznáme sú koeficienty", 2, "param"),
    Rubric("GEM, ktorý sa dá sledovať", 3, "param"),
    Rubric("Kritická hodnota $critical", 1, "param"),
    Rubric("Záver pre všetky \$a$ a prečo z výpočtu plynie", 1, "param"),
)

/** One sentence, one thing asked: a single part worth the paper's 7 points ([readable]: the photo shows them). */
private fun p1(kind: ParamKind, vs: List<PVec>, x: PVec?, text: String, readable: Boolean = true): PaperProblem {
    val r = paramSolve(kind, vs, x)!!
    val part = PaperPart("", text, 7, paramRubric("\$a=${r.critical[0].tex(true)}$"), paramSolution(kind, r, vs), paramConclusion(kind, r.critical[0]), estimate = !readable, review = paramReview(kind))
    return paperProblem(1, "Parameter \$a$", 16, listOf(part), "param", note = HeaderP1)
}

/* ---- Problem 2, 12 points: parts built from the computed data ----------------------------------------------- */

/* The methods of the parts (data/Postup.kt): each part's model solution follows one. */

val CompletePartPostup = postup(
    "Vektory a štandardná báza do stĺpcov, GEM" to "Hlavné stĺpce sú báza; vedľajšie sú kombináciou predošlých.",
    "Vyber hlavné stĺpce" to "Vezmi dané vektory a jednotkové vektory v hlavných stĺpcoch a zdôvodni, že je to báza.",
)

val CoordsInBasisPostup = postup(
    "Zostav sústavu" to "Súradnice sú koeficienty: stĺpce sú vektory bázy, pravá strana daný vektor.",
    "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar.",
    "Súradnice, alebo vektor v §M§ nie je" to "Hlavná pravá strana: riešenie nie je a vektor v §M§ nie je. Inak spätne dosaď.",
)

val LzVectorPostup = postup(
    "Vezmi §\\mathbf{y}§ z obalu" to "Vektor z obalu daných vektorov, napríklad ich súčet, urobí súbor LZ.",
    "Ukáž netriviálnu kombináciu" to "Napíš kombináciu rovnú §\\theta§, ktorá nemá všetky koeficienty nulové.",
)

val SensePostup = postup(
    "Ktorá rovnosť má zmysel?" to "Súradníc je toľko, koľko prvkov má báza.",
    "Vektor zo súradníc" to "Súradnice sú koeficienty: vynásob nimi vektory bázy a sčítaj.",
)

val FromCoordsPostup = postup(
    "Súradnice sú koeficienty" to "Vektor so súradnicami §(c_1,\\dots,c_n)§ je §c_1\\mathbf{b}_1+\\dots+c_n\\mathbf{b}_n§.",
    "Vynásob a sčítaj" to "Po zložkách, v §\\mathbb{Z}_p§ modulo §p§.",
)

val PickBasisPostup = postup(
    "Vektory do stĺpcov, GEM" to "Vektory pri hlavných stĺpcoch tvoria bázu obalu.",
    "Báza a dimenzia" to "Dimenzia je počet vektorov bázy.",
)

val OtherBasisPostup = postup(
    "Zmeň jeden vektor bázy" to "Vynásob jeden vektor bázy číslom rôznym od §0§ aj §1§: ostane v §M§ a súbor sa zmení.",
    "Over LN" to "Vektory do stĺpcov, GEM; všetky stĺpce musia byť hlavné, potom má homogénna sústava len triviálne riešenie.",
    "Je to báza" to "LN súbor v §M§ s počtom vektorov rovným §\\dim M§ je báza.",
)

val DefinitionPostup = postup(
    "Presné znenie" to "Napíš definíciu so všetkými množinami a kvantifikátormi.",
    "Skontroluj podmienky" to "Over, že máš každú časť definície.",
)

val Z7SpanPostup = postup(
    "Dimenzia obalu" to "Obal dvoch vektorov má dimenziu najviac §2§.",
    "Počet prvkov" to "Podpriestor dimenzie §d§ v §\\mathbb{Z}_7^3§ má §7^d§ prvkov.",
    "Príklad pre každú možnosť" to "Ku každej dimenzii nájdi konkrétne §\\mathbf{x},\\mathbf{y}§.",
)

/** Complete [start] by unit vectors to a basis of Z_p^4 and show it is one. Returns the part and the basis. */
private fun completePart(f: Field.Zp, start: List<Vec>, text: String, pts: Int = 4): Pair<PaperPart, List<Vec>> {
    val extra = f.completeWithUnitVectors(start)
    val b = start + extra.map { unit(4, it) }
    val e = f.echelon(completionMatrix(start))
    val (gem, fill) = CompletePartPostup
    val sol = CompletePartPostup.solve {
        gem(Reasons.completeColumns, gemLines(e))
        fill("Hlavné sú stĺpce oboch daných vektorov a §${units(extra)}§. " + Reasons.pivotColumnsLN)
        fill(Reasons.lnOfDim + " Báza je §\\mathcal{B}=${vstex(b)}§.")
    }
    return PaperPart("", text, pts, listOf(Rubric("Doplnenie a prečo je výsledok báza", pts, "complete")), sol, "Napr. §\\mathcal{B}=${vstex(b)}§.") to b
}

/** Coordinates of [x] in the basis [b] of a subspace of dimension b.size: the system, GEM, the result or "x is not in it". */
/** [xName], [bName], [vName]: the names the task uses for the vector, the basis and its vectors (MARAST: y in 𝒳 = (x₁, …)). */
private fun coordsLines(f: Field, b: List<Vec>, x: Vec, xName: String = "\\mathbf{x}", bName: String = "\\mathcal{B}", vName: String = "\\mathbf{b}"): Pair<Solution, Vec?> {
    val d = b.size
    val c = f.coords(b, x)
    val (system, gem, result) = CoordsInBasisPostup
    val sol = CoordsInBasisPostup.solve {
        system("Hľadám §${(1..d).joinToString(",") { "\\alpha_$it" }}\\in ${f.tex}§ so §\\sum\\alpha_i${vName}_i=$xName§; stĺpce sú vektory bázy, pravá strana §$xName§.")
        gem("Úpravami GEM dostanem horný stupňovitý tvar rozšírenej matice.", gemLines(f.echelon(Mat.columns(b, listOf(x)))))
        if (c == null) result("Posledný stĺpec je hlavný, sústava nemá riešenie. §$xName§ nie je kombináciou bázy, teda §$xName\\notin M§ a súradnice vzhľadom na bázu §M§ nemá.")
        else {
            result("Riešenie je jediné. Dosadzujem zdola:", backSubst(f, f.echelon(Mat.columns(b, listOf(x))), (1..d).map { "\\alpha_$it" }).first + Formula("($xName)_{$bName}=${c.tex()}"))
            result("Skúška: kombinácia bázy s týmito koeficientmi dá §$xName§.", "${Terms.lin(c, (1..d).map { "${vName}_$it" })}=${f.fromCoords(b, c).tex()}")
        }
    }
    return sol to c
}

private fun coordsPart(f: Field, b: List<Vec>, x: Vec, text: String, rubric: List<Rubric>, xName: String = "\\mathbf{x}", bName: String = "\\mathcal{B}", vName: String = "\\mathbf{b}"): PaperPart {
    val (sol, c) = coordsLines(f, b, x, xName, bName, vName)
    return PaperPart("", text, rubric.sumOf { it.pts }, rubric, sol, if (c != null) "§($xName)_{$bName}=${c.tex()}§." else "§$xName\\notin M§, súradnice nemá.")
}

/** A nonzero y making (u, v, y) LZ: y = u + v. */
private fun lzVectorPart(f: Field.Zp, u: Vec, v: Vec, text: String): PaperPart {
    val y = f.plus(u, v)
    val (take, combo) = LzVectorPostup
    return PaperPart("", text, 2, listOf(Rubric("Vektor §\\mathbf{y}§ a prečo je súbor LZ", 2, "ln")), LzVectorPostup.solve {
        take("Označím §\\mathbf{u}=${u.tex()}§, §\\mathbf{v}=${v.tex()}§ a vezmem ich súčet; ten v obale leží.", "\\mathbf{y}=\\mathbf{u}+\\mathbf{v}=${y.tex()}")
        combo("Táto kombinácia je netriviálna a rovná sa §\\theta§, súbor je teda LZ.", "\\mathbf{u}+\\mathbf{v}-\\mathbf{y}=\\theta")
    }, "Napr. §\\mathbf{y}=\\mathbf{u}+\\mathbf{v}=${y.tex()}§.")
}

/** How many nonzero y make (u, v, y) LZ, for an LN pair (u, v) in Z_p^n: all nonzero vectors of their span. */
private fun lzCountPart(p: Int, text: String): PaperPart {
    val cnt = p * p - 1
    val (whenLz, size, nonzero) = CountLzPostup
    return PaperPart("", text, 2, listOf(Rubric("Počet takých §\\mathbf{y}§ a prečo", 2, "count")), CountLzPostup.solve {
        whenLz("Súbor §(\\mathbf{u},\\mathbf{v},\\mathbf{y})§ je LZ práve vtedy, keď §\\mathbf{y}\\in\\langle\\mathbf{u},\\mathbf{v}\\rangle§. Vektory §\\mathbf{u},\\mathbf{v}§ sú totiž LN: vektor v obale je ich kombináciou a vektor mimo obalu súbor LN nechá (Veta 2.39 a Veta 2.42).")
        size("Obal má dimenziu §2§, teda toľko prvkov (Poznámka 2.67):", "$p^2=${p * p}")
        nonzero("Bez nulového vektora ich je §$cnt§.")
    }, "§$p^2-1=$cnt§ vektorov: všetky nenulové prvky §\\langle\\mathbf{u},\\mathbf{v}\\rangle§.")
}

/** Which of a), b), c) makes sense for a basis of [b].size vectors, and the x of that equality. */
private fun sensePart(f: Field.Zp, b: List<Vec>, options: List<Vec>, text: String, rubric: List<Rubric>): PaperPart {
    val k = options.indexOfFirst { it.size == b.size }
    val label = "${'a' + k})"
    val x = f.fromCoords(b, options[k].map { f.norm(it) })
    val sum = Terms.lin(options[k], options[k].indices.map { "\\mathbf{b}_${it + 1}" })
    val (sense, from) = SensePostup
    return PaperPart("", text, rubric.sumOf { it.pts }, rubric, SensePostup.solve {
        sense("Báza má §${b.size}§ prvky, súradnice vzhľadom na ňu sú §${b.size}§-tica (Definícia 2.68). Zmysel má preto len $label.")
        from("Súradnice sú koeficienty kombinácie vektorov bázy §\\mathcal{B}=${vstex(b)}§.", "\\mathbf{x}=$sum=${x.tex()}")
    }, "Zmysel má len $label, §\\mathbf{x}=${x.tex()}§.")
}

/** The vector with coordinates [c] in the basis [b]. */
private fun fromCoordsPart(f: Field.Zp, b: List<Vec>, c: Vec, text: String, rubric: List<Rubric>): PaperPart {
    val x = f.fromCoords(b, c)
    val sum = Terms.lin(c, c.indices.map { "\\mathbf{b}_${it + 1}" })
    val (coef, mult) = FromCoordsPostup
    return PaperPart("", text, rubric.sumOf { it.pts }, rubric, FromCoordsPostup.solve {
        coef("Vektor so súradnicami §${c.tex()}§ je kombinácia vektorov bázy s týmito koeficientmi (Definícia 2.68).")
        mult("Vynásobím a sčítam po zložkách.", "$sum=${x.tex()}")
    }, "§${x.tex()}§.")
}

/** A basis of M = <gens> picked by GEM in columns, and its dimension. Returns the part and the basis. */
private fun spanBasisPart(f: Field, gens: List<Vec>, text: String, rubric: List<Rubric>): Pair<PaperPart, List<Vec>> {
    val piv = f.pickBasis(gens)
    val b = piv.map { gens[it] }
    val (gem, basis) = PickBasisPostup
    val sol = PickBasisPostup.solve {
        val k = gens.size
        // where the matrix comes from, as the task asks (MARAST: "napište, co jsou proměnné a odkud se vzaly koeficienty")
        gem("Hľadám všetky §\\alpha_1,\\dots,\\alpha_$k\\in ${f.tex}§ s §\\alpha_1\\mathbf{v}_1+\\dots+\\alpha_$k\\mathbf{v}_$k=\\theta§, kde §\\mathbf{v}_i§ sú dané vektory. Po zložkách je to homogénna sústava pre neznáme §\\alpha_i§: jej koeficienty sú zložky vektorov, dané vektory sú jej stĺpce.")
        gem("Upravím ju GEM do HST. " + Reasons.gemColumns, gemLines(f.echelon(Mat.columns(gens))))
        basis(Reasons.mainColumns(piv) + " Báza je napríklad §${vstex(b)}§. " + Reasons.dim("\\dim M=${b.size}"))
    }
    return PaperPart("", text, rubric.sumOf { it.pts }, rubric, sol, "Báza napr. §${vstex(b)}§, §\\dim M=${b.size}§.") to b
}

/** Another basis of M: the first vector multiplied, so it is not one of [given]. LN by GEM, length = dimension. */
private fun otherBasisPart(f: Field, b: List<Vec>, given: List<Vec>, text: String, pts: Int): PaperPart {
    val ks = if (f is Field.Zp) (2..f.p - 1).toList() else listOf(2, -1, 3)
    // the first vector of the basis times a number, or the next one when all those multiples are given (a student's own basis)
    val (i, first) = b.indices.firstNotNullOf { i -> ks.map { f.scale(it.q(), b[i]) }.firstOrNull { it !in given }?.let { i to it } }
    val nb = b.mapIndexed { j, v -> if (j == i) first else v }
    val which = if (i == 0) "Prvý vektor bázy" else "${i + 1}. vektor bázy"
    val e = f.echelon(Mat.columns(nb))
    check(e.pivots.size == b.size)
    val (change, ln, isBasis) = OtherBasisPostup
    val sol = OtherBasisPostup.solve {
        change("$which vynásobím číslom rôznym od §0§ aj §1§, ostatné nechám: §${vstex(nb)}§. Vektor §${first.tex()}§ v zadanom súbore nie je a v §M§ leží.")
        ln("Vektory dám do stĺpcov a upravím GEM. " + Reasons.gemColumns, gemLines(e))
        isBasis("Všetky §${b.size}§ stĺpce sú hlavné, homogénna sústava má len triviálne riešenie, preto je súbor LN. Má §${b.size}§ vektory z §M§ a §\\dim M=${b.size}§, preto je to báza §M§ (Veta 2.55).")
    }
    return PaperPart("", text, pts, listOf(Rubric("Iná báza a prečo je to báza", pts, "span-basis")), sol, "Napr. §${vstex(nb)}§.")
}

/** The number of elements of a subspace of dimension d of Z_p^4. */
private fun spanCountPart(p: Int, d: Int, text: String): PaperPart {
    val (match, count) = CountElementsPostup
    return PaperPart("", text, 2, listOf(Rubric("Počet prvkov §M§ a prečo", 2, "count")), CountElementsPostup.solve {
        match("Každý prvok §M§ má v báze §$d§-ticu súradníc zo §\\mathbb{Z}_{$p}§ a každá §$d§-tica dá prvok §M§. " + Reasons.coords)
        count("Prvkov je preto toľko ako §$d§-tíc a každú zo §$d§ súradníc volím zo §$p§ prvkov (Poznámka 2.67).", "|M|=${Terms.pow(p.toLong(), d)}=${pow(p, d)}")
    }, "§${Terms.pow(p.toLong(), d)}=${pow(p, d)}§.")
}

/* ---- The student's own basis: a completion or a basis of a span has many right answers (Model.kt, OwnBasis) ------- */

private val OwnGemPostup = postup("GEM so stĺpcami tvojej bázy" to "Vedľajší stĺpec je kombináciou predošlých, súbor je potom LZ.")

private fun ownGem(f: Field, b: List<Vec>): Solution? {
    if (f.rank(b) == b.size) return null
    val (gem) = OwnGemPostup
    return OwnGemPostup.solve { gem("Vektory dám do stĺpcov a upravím GEM. Pri LN súbore by boli všetky stĺpce hlavné.", gemLines(f.echelon(Mat.columns(b)))) }
}

/** Part a) completes [start] to a basis of F^n, [sample] is ours; the student enters the vectors they added. */
private fun completionOwn(f: Field, start: List<Vec>, sample: List<Vec>, build: (List<Vec>) -> List<PaperPart>): OwnBasis {
    val n = start[0].size
    val k = n - start.size
    return OwnBasis(0, Answer.Vectors(List(n) { "x_{${it + 1}}" }, (f as? Field.Zp)?.p, k..k, entry = "vektor",
        check = { e -> judgeCompletion(f, start, e) }, example = sample.drop(start.size).map { listOf(it) },
        quick = List(n) { "\\mathbf{e}_{${it + 1}}" to unit(n, it) }), { e -> start + e }, build) { b -> ownGem(f, b) }
}

/** Part a) finds a basis of the span of [gens], [sample] is ours; the student enters their whole basis. */
private fun spanOwn(f: Field, gens: List<Vec>, sample: List<Vec>, build: (List<Vec>) -> List<PaperPart>): OwnBasis =
    OwnBasis(0, Answer.Vectors(List(gens[0].size) { "x_{${it + 1}}" }, (f as? Field.Zp)?.p, sample.size..sample.size, entry = "vektor",
        check = { e -> judgeBasis(f, gens, e) { null } }, example = sample.map { listOf(it) },
        quick = gens.map { it.tex() to it }), { it }, build) { b -> ownGem(f, b) }

private fun f5() = Field.Zp(5)

private fun sova2025P2(): PaperProblem {
    val f = f5()
    val s = listOf(vec(1, 2, 0, 4), vec(3, 3, 0, 1))
    val t = cut("""Doplňte soubor vektorů §((1,2,0,4),(3,3,0,1))§ na bázi §\mathbb{Z}_5^4§.¦ Ve vámi nalezené bázi najděte souřadnice vektoru §\mathbf{x}=(1,0,1,0)§.¦ Nalezněte nějaký nenulový vektor §\mathbf{y}§ tak, aby soubor §((1,2,0,4),(3,3,0,1),\mathbf{y})§ byl LZ.¦ Kolik takových vektorů existuje?""")
    val (a, b) = completePart(f, s, t[0])
    val build = { b: List<Vec> -> listOf(
        a,
        coordsPart(f, b, vec(1, 0, 1, 0), t[1], listOf(Rubric("Sústava pre súradnice: čo sú neznáme, odkiaľ stĺpce", 2, "coords"), Rubric("Súradnice §(\\mathbf{x})_{\\mathcal{B}}§", 2, "coords"))),
        lzVectorPart(f, s[0], s[1], t[2]),
        lzCountPart(5, t[3]),
    ) }
    return paperProblem(2, "Doplnenie na bázu", 28, build(b), "complete", own = completionOwn(f, s, b, build))
}

private val senseOptions = listOf(vec(1, 2), vec(1, 2, 1))

private fun slon2024P2(): PaperProblem {
    val f = f5()
    val s = listOf(vec(2, 1, 1, 3), vec(2, 0, 1, 3))
    val t = cut("""Doplňte soubor §((2,1,1,3),(2,0,1,3))§ na bázi §\mathbb{Z}_5^4§. Označme §\mathcal{B}§ vámi nalezenou bázi.¦ Nechť §\mathbf{x}\in\mathbb{Z}_5^4§. Rozhodněte, která z následujících rovností je smysluplná, a pro ni spočítejte odpovídající §\mathbf{x}§: §§${coordChoicesTex("(1,2)", "(1,2,1)", "(1,3,0,0)")}§§¦ Spočtěte také §((1,1,1,2))_{\mathcal{B}}§.""")
    val (a, b) = completePart(f, s, t[0])
    val build = { b: List<Vec> ->
        val (cs, c) = coordsLines(f, b, vec(1, 1, 1, 2), "(1,1,1,2)")
        listOf(
            a,
            sensePart(f, b, senseOptions + listOf(vec(1, 3, 0, 0)), t[1], listOf(Rubric("Zmysel má len c) a prečo", 3, "coords"), Rubric("Vektor §\\mathbf{x}§ pre c)", 2, "coords"))),
            PaperPart("", t[2], 3, listOf(Rubric("Súradnice §(1,1,1,2)§", 3, "coords")), cs, "§((1,1,1,2))_{\\mathcal{B}}=${c!!.tex()}§."),
        )
    }
    return paperProblem(2, "Báza a súradnice", 28, build(b), "complete", own = completionOwn(f, s, b, build))
}

private fun medved2024P2(): PaperProblem {
    val f = f5()
    val gens = listOf(vec(1, 4, 0, 1), vec(1, 1, 3, 1), vec(3, 4, 3, 3))
    val t = cut("""Ve vektorovém prostoru §\mathbb{Z}_5^4§ najděte bázi podprostoru §M=\langle(1,4,0,1),(1,1,3,1),(3,4,3,3)\rangle§. Jakou má §M§ dimenzi?¦ Pokud je to možné, nalezněte souřadnice vektoru §\mathbf{x}=(1,3,1,1)§ vzhledem k vámi nalezené bázi. Pokud to možné není, napište, co to pro daný vektor znamená.¦ Najděte ještě nějakou jinou bázi §M§, která obsahuje alespoň jeden vektor, který nepatří do souboru §((1,4,0,1),(1,1,3,1),(3,4,3,3))§. Pokud to není zřejmé, vysvětlete, proč je to také báze §M§.""")
    val (a, b) = spanBasisPart(f, gens, t[0], listOf(Rubric("Matica a GEM, hlavné stĺpce", 3, "span-basis"), Rubric("Báza a dimenzia so zdôvodnením", 3, "span-basis")))
    val build = { b: List<Vec> -> listOf(
        a,
        coordsPart(f, b, vec(1, 3, 1, 1), t[1], listOf(Rubric("Súradnice §\\mathbf{x}§, alebo že §\\mathbf{x}\\notin M§", 3, "span-basis"))),
        otherBasisPart(f, b, gens, t[2], 3),
    ) }
    return paperProblem(2, "Báza obalu a súradnice", 28, build(b), "span-basis", own = spanOwn(f, gens, b, build))
}

private fun sova2023P2(): PaperProblem {
    val f = Field.Zp(3)
    val gens = listOf(vec(1, 1, 0, 1), vec(1, 0, 2, 0), vec(2, 1, 2, 1), vec(0, 2, 0, 1))
    val t = cut("""Ve vektorovém prostoru §\mathbb{Z}_3^4§ najděte bázi a dimenzi podprostoru §M=\langle(1,1,0,1),(1,0,2,0),(2,1,2,1),(0,2,0,1)\rangle§. Označme §\mathcal{B}§ vámi nalezenou bázi.¦ Nechť §\mathbf{x}\in M§. Rozhodněte, která z následujících rovností je smysluplná, a pro ni spočítejte odpovídající §\mathbf{x}§: §§${coordChoicesTex("(1,2)", "(1,2,1)", "(1,2,0,1)")}§§¦ Vymyslete ještě nějakou jinou bázi §M§ a vysvětlete, proč je to taky báze.""")
    val (a, b) = spanBasisPart(f, gens, t[0], listOf(Rubric("Matica, GEM, hlavné stĺpce", 3, "span-basis"), Rubric("Báza a dimenzia so zdôvodnením", 3, "span-basis")))
    val build = { b: List<Vec> -> listOf(
        a,
        sensePart(f, b, senseOptions + listOf(vec(1, 2, 0, 1)), t[1], listOf(Rubric("Ktorá rovnosť má zmysel a prečo", 2, "coords"), Rubric("Vektor §\\mathbf{x}§", 2, "coords"))),
        otherBasisPart(f, b, gens, t[2], 2),
    ) }
    return paperProblem(2, "Báza a dimenzia v §\\mathbb{Z}_3^4§", 28, build(b), "span-basis", own = spanOwn(f, gens, b, build))
}

private fun slepice2023P2(): PaperProblem {
    val f = Field.Zp(7)
    val s = listOf(vec(1, 2, 2, 3), vec(2, 3, 3, 1))
    val t = cut("""Doplňte soubor vektorů §((1,2,2,3),(2,3,3,1))§ na bázi §\mathbb{Z}_7^4§.¦ Ve vámi nalezené bázi najděte souřadnice vektoru §\mathbf{x}=(0,5,5,4)§.¦ Který vektor má souřadnice §(1,0,1,0)§ vzhledem k vaší bázi?""")
    val (a, b) = completePart(f, s, t[0])
    val build = { b: List<Vec> -> listOf(
        a,
        coordsPart(f, b, vec(0, 5, 5, 4), t[1], listOf(Rubric("Sústava pre súradnice a výsledok", 4, "coords"))),
        fromCoordsPart(f, b, vec(1, 0, 1, 0), t[2], listOf(Rubric("Vektor so súradnicami §(1,0,1,0)§", 4, "coords"))),
    ) }
    return paperProblem(2, "Doplnenie v §\\mathbb{Z}_7^4§", 28, build(b), "complete", own = completionOwn(f, s, b, build))
}

/* ---- The official sample 2nd test (MARAST, lecture 292): problems 1 and 2 over R, 3 with its own part ----------- */

private const val MarastSrc = "MARAST, Ukázková druhá zápočtová písemka (lekce 292)"

/** Show that [b] is a basis of R^4: LN by GEM in columns, as many vectors as the dimension. */
private fun isBasisPart(f: Field, b: List<Vec>, text: String, pts: Int): PaperPart {
    val e = f.echelon(Mat.columns(b))
    check(e.pivots.size == b.size)
    val (count, ln, end) = BasisPostup
    return PaperPart("", text, pts, listOf(Rubric("Matica s vektormi v stĺpcoch a GEM do HST", pts - 2, "concept:baza"), Rubric("Záver: LN a ${b.size} vektory, teda báza (Veta 2.55)", 2, "concept:baza")),
        BasisPostup.solve {
            count("Vektorov je §${b.size}§, toľko, aká je dimenzia §${fieldPow(f, b.first().size)}§. Stačí preto overiť, že sú LN (Veta 2.55).")
            ln("Vektory dám do stĺpcov homogénnej sústavy pre koeficienty kombinácie a upravím GEM. " + Reasons.gemColumns, gemLines(e))
            ln("Všetky stĺpce sú hlavné, homogénna sústava má len triviálne riešenie, preto je súbor LN (Definícia 2.23).")
            end("LN súbor §${b.size}§ vektorov v priestore dimenzie §${b.size}§ je báza (Veta 2.55).")
        }, "Súbor je LN a má §${b.size}§ vektory, je to báza §${fieldPow(f, b.first().size)}§.")
}

/** All nontrivial subspaces of Z_3^2: the four lines, each the span of one nonzero vector. */
private fun z3LinesPart(text: String): PaperPart {
    val (what, howMany, list, all) = ZpListPostup
    return PaperPart("", text, 4, listOf(Rubric("Štyri podpriestory dimenzie §1§ aj s prvkami", 3, "zp-vypis"), Rubric("Prečo sú to všetky; triviálne sú §\\{\\theta\\}§ a §\\mathbb{Z}_3^2§", 1, "zp-vypis")),
        ZpListPostup.solve {
            what("Triviálne podpriestory sú §\\{\\theta\\}§ a celé §\\mathbb{Z}_3^2§ (Definícia 2.9). Dimenziu §0§ má len §\\{\\theta\\}§ (Veta 2.53) a podpriestor dimenzie §2§ v §\\mathbb{Z}_3^2§ je celý priestor (Dôsledok 2.63). Netriviálne majú teda dimenziu §1§: každý je obal jedného nenulového vektora.")
            howMany("Nenulových vektorov je §3^2-1=8§. Podpriestor dimenzie §1§ má §3^1=3§ prvky, z toho §2§ nenulové, a obal každého z nich je ten istý podpriestor. Podpriestorov je preto §8/2=4§.")
            list("Vypíšem ich ako množiny:", listOf(
                Formula("\\langle(1,0)\\rangle=\\{(0,0),(1,0),(2,0)\\}"), Formula("\\langle(0,1)\\rangle=\\{(0,0),(0,1),(0,2)\\}"),
                Formula("\\langle(1,1)\\rangle=\\{(0,0),(1,1),(2,2)\\}"), Formula("\\langle(1,2)\\rangle=\\{(0,0),(1,2),(2,1)\\}"),
            ))
            all("Každý z §8§ nenulových vektorov leží práve v jednej z týchto množín, iné podpriestory dimenzie §1§ preto nie sú.")
        }, "Štyri: §\\langle(1,0)\\rangle§, §\\langle(0,1)\\rangle§, §\\langle(1,1)\\rangle§, §\\langle(1,2)\\rangle§.")
}

/** How many (ordered) bases a subspace of dimension 2 of Z_p^n has: (p^2 - 1)(p^2 - p). */
private fun countBases2Part(p: Int, n: Int, text: String, pts: Int): PaperPart {
    val (oneByOne, choices, multiply) = CountBasesPostup
    val all = p * p
    return PaperPart("", text, pts, listOf(Rubric("Počet báz §(${all}-1)(${all}-$p)§", 2, "count-all"), Rubric("Zdôvodnenie: prvý vektor nenulový z §P§, druhý mimo obalu prvého", pts - 2, "count-all")),
        CountBasesPostup.solve {
            oneByOne("Báza §P§ je LN súbor §(\\mathbf{u},\\mathbf{v})§ vektorov z §P§ (§\\dim P=2§, Veta 2.55). §P§ má §$p^2=$all§ prvkov (Poznámka 2.67).")
            choices("Za §\\mathbf{u}§ smiem vziať každý nenulový vektor §P§: §$all-1=${all - 1}§ možností. Za §\\mathbf{v}§ každý vektor §P§ mimo §\\langle\\mathbf{u}\\rangle§, ktorý má §$p§ prvkov: §$all-$p=${all - p}§ možností. Práve vtedy je §(\\mathbf{u},\\mathbf{v})§ LN (Veta 2.42).")
            multiply("Rôzne voľby dajú rôzne (usporiadané) súbory, počty vynásobím.", "(${all}-1)(${all}-$p)=${all - 1}\\cdot ${all - p}=${(all - 1) * (all - p)}")
        }, "§(${all}-1)(${all}-$p)=${(all - 1) * (all - p)}§ báz.")
}

private const val Instr2 = "Pokyn zo zadania: „Řádně vysvětlete svůj postup tak, aby jej pochopil čtenář, který pouze zná definice a tvrzení z přednášky: Pokud budete například řešit soustavu rovnic, napište, co jsou proměnné a odkud se vzaly koeficienty. Vysvětlete také, jak přesně z výpočtu plynou závěry, které uděláte. V tomto příkladě byste měli demonstrovat své nabyté vědomosti z BI-LA1.“"

private fun veverka2024(): TestPaper {
    val f = f5()
    val t2 = cut("""Doplňte soubor vektorů §((2,1,1,1),(1,3,1,1))§ na bázi §\mathbb{Z}_5^4§.¦ Ve vámi nalezené bázi najděte souřadnice vektoru §\mathbf{x}=(0,0,1,1)§.¦ Pokud je to možné, nalezněte souřadnice vektoru §(1,0,4,0)§ vzhledem k vaší bázi. Pokud to možné není, napište, co to pro daný vektor znamená.""")
    val s2 = listOf(vec(2, 1, 1, 1), vec(1, 3, 1, 1))
    val (a, b) = completePart(f, s2, t2[0])
    val build = { b: List<Vec> -> listOf(
        a,
        coordsPart(f, b, vec(0, 0, 1, 1), t2[1], listOf(Rubric("Sústava pre súradnice: čo sú neznáme, odkiaľ stĺpce", 2, "coords"), Rubric("Súradnice §(\\mathbf{x})_{\\mathcal{B}}§", 2, "coords"))),
        coordsPart(f, b, vec(1, 0, 4, 0), t2[2], listOf(Rubric("Rozhodnutie: báza celého §\\mathbb{Z}_5^4§, súradnice preto existujú", 2, "coords"), Rubric("Súradnice §(1,0,4,0)§", 2, "coords")), "(1,0,4,0)"),
    ) }
    val p2 = paperProblem(2, "Doplnenie na bázu a súradnice", 28, build(b), "complete", note = m(Instr2), own = completionOwn(f, s2, b, build))
    val t3 = cut("""Napište definici podprostoru.¦ Napište definici báze podprostoru.¦ Jaký prvek obsahuje každý podprostor? Dokažte vaše tvrzení.¦ Mějme §P§ podprostor §\mathbb{Z}_3^5§, kde §\dim P=2§. Kolik různých bází podprostoru §P§ existuje? Dokažte vaše tvrzení.""")
    val p3 = paperProblem(3, "Definície a dôkazy", 26, listOf(
        defPart(concept("podpriestor"), t3[0]), defPart(concept("baza"), t3[1]),
        proofPart(proof("p-theta"), t3[2], 3), countBases2Part(3, 5, t3[3], 4),
    ).map { it.copy(estimate = false) }, note = m("""Pokyn a body 2 + 2 + 3 + 4 sú z prepisu na fit-wiki (hárok 3. príkladu nemáme): „Při důkazu můžete využít jakékoliv věty a definice z přednášky.“"""))
    return TestPaper("veverka-2024", "Test 22. 11. 2024 (variant Veverka)", "Zadanie z fit-wiki: príklady 1 a 2 overené podľa fotky, príklad 3 podľa prepisu na fit-wiki (s bodmi)", listOf(
        p1(ParamKind.BASIS, listOf(pc(2, 0, -1), listOf(Poly.of(1), Poly.of(2), Poly.A), pc(2, 3, 2)), null,
            m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ je soubor vektorů §((2,0,-1),(1,2,a),(2,3,2))§ bází §\mathbb{R}^3§.""")),
        p2, p3,
    ))
}

private fun vcelka2023(): TestPaper {
    val r = Field.R
    val gens = listOf(vec(2, 1, -2, 1), vec(-2, 1, 2, -1), vec(2, 5, -2, 1), vec(-2, 3, 2, -1))
    val t2 = cut("""Ve vektorovém prostoru §\mathbb{R}^4§ najděte bázi a dimenzi lineárního obalu §M=\langle(2,1,-2,1),(-2,1,2,-1),(2,5,-2,1),(-2,3,2,-1)\rangle§.¦ Spočítejte souřadnice vektoru §\mathbf{x}=(3,-2,2,-1)§ vzhledem k vámi nalezené bázi.¦ Vymyslete ještě nějakou jinou bázi §M§ a vysvětlete, proč je to taky báze.""")
    val (a, b) = spanBasisPart(r, gens, t2[0], listOf(Rubric("Sústava: neznáme sú koeficienty kombinácie, stĺpce sú dané vektory; GEM", 3, "span-basis"), Rubric("Báza z hlavných stĺpcov a dimenzia so zdôvodnením", 3, "span-basis")))
    val build = { b: List<Vec> -> listOf(
        a,
        coordsPart(r, b, vec(3, -2, 2, -1), t2[1], listOf(Rubric("Sústava pre súradnice a záver: §\\mathbf{x}\\notin M§, súradnice nemá", 3, "coords"))),
        otherBasisPart(r, b, gens, t2[2], 3),
    ) }
    val p2 = paperProblem(2, "Báza a dimenzia obalu v §\\mathbb{R}^4§", 28, build(b), "span-basis", own = spanOwn(r, gens, b, build), note = m("""Vektor §\mathbf{x}=(3,-2,2,-1)§ v §M§ neleží (overené výpočtom aj na fotke zadania): správna odpoveď je, že súradnice vzhľadom na bázu §M§ nemá. Pokyn pod zadaním na fotke nie je."""))
    val t3 = cut("""Napište definici báze podprostoru.¦ Napište definici souřadnic vzhledem k bázi podprostoru.¦ Uvažujme §P\ssub\mathbb{Z}_7^5§, pro který platí §\dim P=2§. Kolik prvků §P§ má? Dokažte své tvrzení.¦ Dokažte, že ve vektorovém prostoru §\mathbb{R}^n§ platí: Je-li soubor §\mathcal{B}=(\mathbf{x},\mathbf{y},\mathbf{z})§ bází podprostoru §P§, potom jsou souřadnice vektoru §\mathbf{z}\in P§ vzhledem k bázi §\mathcal{B}§ určeny jednoznačně. K důkazu vám postačí definice daných pojmů. Rozhodně se neodvolávejte na obecnější větu, která říká totéž pro libovolný počet vektorů.""")
    val p3 = paperProblem(3, "Definície a dôkazy", 26, listOf(
        defPart(concept("baza"), t3[0]), defPart(concept("suradnice"), t3[1]),
        proofPart(proof("p-pd"), t3[2], 3), proofPart(proof("p-suradnice"), t3[3], 4),
    ), note = m("""Pozor, príklad pokračuje na druhom liste („Na tomto listu nesmí být nic, co nepatří k příkladu 3!“). V poslednej úlohe je §\mathbf{z}§ zároveň tretí vektor bázy aj „vektor z §P§“; bezpečnejšie je dokázať jednoznačnosť pre ľubovoľný §\mathbf{v}\in P§."""))
    return TestPaper("vcelka-2023", "Test 24. 11. 2023 (variant Včelka)", "Zadanie z fit-wiki, overené podľa fotky; body na fotke nečitateľné, 7/12/11 je odhad podľa testov 2024 a 2025", listOf(
        p1(ParamKind.GENERATES, listOf(pc(1, 3, -1), listOf(Poly.of(1), Poly.of(2), Poly.A), pc(-1, 2, 2)), null,
            m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ soubor vektorů §((1,3,-1),(1,2,a),(-1,2,2))§ generuje §\mathbb{R}^3§."""), readable = false),
        p2, p3,
    ))
}

private fun marast2(): TestPaper {
    val r = Field.R
    val xs = listOf(vec(1, 1, 1, 1), vec(1, 2, 1, 1), vec(1, 1, 2, 1), vec(1, 3, 2, 3))
    val t1 = cut("""Ukažte, že soubor vektorů §\mathcal{X}=(\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3,\mathbf{x}_4)§, kde §\mathbf{x}_1=(1,1,1,1)§, §\mathbf{x}_2=(1,2,1,1)§, §\mathbf{x}_3=(1,1,2,1)§, §\mathbf{x}_4=(1,3,2,3)§, je báze §\mathbb{R}^4§¦ a spočítejte souřadnice §\mathbf{y}§ v §\mathcal{X}§, je-li §\mathbf{y}=(1,0,3,3)§.""")
    val p1 = paperProblem(1, "Báza §\\mathbb{R}^4§ a súradnice", 15, listOf(
        isBasisPart(r, xs, t1[0], 4),
        coordsPart(r, xs, vec(1, 0, 3, 3), t1[1], listOf(Rubric("Sústava pre súradnice a GEM", 2, "coords"), Rubric("Súradnice §(\\mathbf{y})_{\\mathcal{X}}§", 1, "coords")), "\\mathbf{y}", "\\mathcal{X}", "\\mathbf{x}"),
    ), "coords", note = m("""Pokyn zo zadania: „V tomto příkladu nemusíte své výpočty komentovat. Nicméně znalý čtenář ve vašich výpočtech musí být schopen poznat, jak jste se k výsledku dobrali.“"""))
    val gens = listOf(vec(1, 1, 1, 0), vec(1, -1, 0, 1), vec(2, 0, 1, 1), vec(4, 6, 5, -1))
    val t2 = m("""Ve vektorovém prostoru §\mathbb{R}^4§ najděte bázi a dimenzi lineárního obalu §M=\langle(1,1,1,0),(1,-1,0,1),(2,0,1,1),(4,6,5,-1)\rangle§. Řádně vysvětlete svůj postup: Pokud budete například řešit soustavu rovnic, napište, co jsou proměnné a odkud se vzaly koeficienty. Vysvětlete také, jak z výpočtu plynou závěry, které uděláte.""")
    val (a2, _) = spanBasisPart(r, gens, t2, listOf(
        Rubric("Sústava: neznáme sú koeficienty kombinácie, stĺpce sú dané vektory", 4, "span-basis"),
        Rubric("GEM do HST", 3, "span-basis"),
        Rubric("Z HST báza: vektory pri hlavných stĺpcoch, prečo (vedľajšie sú kombináciou predošlých, vybrané LN)", 3, "span-basis"),
        Rubric("Dimenzia ako počet vektorov bázy", 2, "span-basis"),
    ))
    val p2 = paperProblem(2, "Báza a dimenzia obalu v §\\mathbb{R}^4§", 25, listOf(a2.copy(estimate = true)), "span-basis",
        note = m("""MARAST pri tomto príklade upozorňuje, že samotný výpočet bez vysvetlenia (ani videá) nestačí na plný počet bodov: treba povedať, odkiaľ je matica, čo znamená HST a ako z nej plynie záver."""))
    val t3 = cut("""Napište definici podprostoru¦ a ukažte, že množina řešení homogenní soustavy lineárních rovnic je podprostor.¦ Najděte všechny netriviální podprostory prostoru §\mathbb{Z}_3^2§.""")
    val p3 = paperProblem(3, "Definícia, dôkaz a podpriestory §\\mathbb{Z}_3^2§", 30, listOf(
        defPart(concept("podpriestor"), t3[0]),
        proofPart(proof("p-homog"), t3[1], 5, skip = setOf("Definícia podpriestoru so všetkými predpokladmi")),
        z3LinesPart(t3[2]),
    ), note = m("""Pokyn zo zadania: definície a vety formulujte správne aj s predpokladmi; ekvivalentné tvrdenie nenahrádza požadovanú definíciu („Dimenze je rovna počtu prvků báze“ nie je definícia dimenzie)."""))
    return TestPaper("marast-ukazka-2", "Oficiálna ukážková 2. písomka (MARAST)", "$MarastSrc; zadania a odpovede overené, body za časti sú náš odhad", listOf(p1, p2, p3))
}

/* ---- Problem 3, 11 points: definitions (2 each), maybe a small task, proofs sharing the rest ----------------------- */

private fun concept(id: String) = La1ConceptById.getValue(id)
private fun proof(id: String) = La1ProofById.getValue(id)

/** A definition: the model solution is the exact Czech text, then what to check it for. */
/** [units]: what this task asks for, when it asks for only a part of the definition (LN, or LZ, of the shared LN/LZ card). */
private fun defPart(c: Concept, text: String, units: List<String> = c.units): PaperPart {
    val (exact, check) = DefinitionPostup
    return PaperPart(
        "", text, 2, listOf(Rubric("Definícia: ${c.name}, so všetkými predpokladmi", 2, "concept:${c.id}")),
        DefinitionPostup.solve {
            exact("Presné znenie zo skrípt (${c.src.removePrefix("Skriptá, ")}):")
            for (l in c.czech) when (l) {
                is Para -> exact(l.tex)
                is Item -> exact("• " + l.tex)
                is Block -> more(Formula(l.tex))
            }
            check("Skontroluj, či tam máš: " + units.joinToString("; ") + ".")
        },
        "Presné znenie: ${c.src.removePrefix("Skriptá, ")}.",
    )
}

/** The LN/LZ card's checklist cut to what a task asks for: only LN, or only LZ. */
private val lnOnly get() = concept("ln").units.filterNot { it.startsWith("LZ") }
private val lzOnly get() = listOf(concept("ln").units.first(), "LZ: existuje netriviálna lineárna kombinácia rovná §\\theta§")

/**
 * Rubric lines of one proof sharing [pts] points. With at least a point a line, the remainder goes to the first line;
 * with fewer points than lines, neighbouring lines share a point, so no line is worth nothing.
 */
private fun proofRubric(proof: Proof, pts: Int): List<Rubric> {
    val k = proof.checklist.size
    if (pts >= k) return proof.checklist.mapIndexed { i, s -> Rubric("Dôkaz: $s", if (i == 0) pts - (k - 1) * (pts / k) else pts / k, "proof:${proof.id}") }
    return proof.checklist.withIndex().groupBy { it.index * pts / k }.values.map { g -> Rubric("Dôkaz: " + g.joinToString("; ") { it.value }, 1, "proof:${proof.id}") }
}

/** A proof: the model solution is the proof of the app, line by line, each line after the step it answers. */
/** [skip]: checklist lines another part of the same problem already scores (a definition asked separately). */
private fun proofPart(p: Proof, text: String, pts: Int, skip: Set<String> = emptySet()) = PaperPart(
    "", text, pts, proofRubric(p.copy(checklist = p.checklist - skip), pts),
    // the proof's goals are the subgoals: each line under the goal it settles
    proofSolution(p),
    "Dôkaz: ${p.end}",
)

private fun p3(parts: List<PaperPart>) = paperProblem(3, "Definície a dôkaz", 26, parts)

/** Sova 2025: is a set of five vectors of Z_2^4 a subspace? A pair whose sum falls out, found by computing. */
private fun z2SetPart(text: String): PaperPart {
    val set = listOf(vec(0, 0, 0, 0), vec(1, 0, 0, 0), vec(0, 1, 0, 0), vec(1, 1, 0, 0), vec(0, 0, 1, 0))
    val f = Field.Zp(2)
    val (u, v) = set.flatMapIndexed { i, a -> set.drop(i + 1).map { a to it } }.first { (a, b) -> f.plus(a, b) !in set }
    val w = f.plus(u, v)
    val (theta, sum, _, end) = SubspacePostup
    return PaperPart("", text, 2, listOf(Rubric("Množina v §\\mathbb{Z}_2^4§: protipríklad na sčítanie", 2, "gen:subspace")), SubspacePostup.solve {
        theta("Nulový vektor §(0,0,0,0)§ v množine je.")
        sum("Podpriestor musí s každými dvoma vektormi obsahovať aj ich súčet (Definícia 2.7). Protipríklad: §${u.tex()}§ a §${v.tex()}§ v množine sú, ich súčet nie.", "${u.tex()}+${v.tex()}=${w.tex()}")
        end("Množina nie je uzavretá na sčítanie, a preto nie je podpriestorom §\\mathbb{Z}_2^4§.")
    }, "Množina podpriestorom nie je: §${u.tex()}+${v.tex()}=${w.tex()}§ v nej nie je.")
}

/** Slon 2024: how many vectors can <x, y> in Z_7^3 have, with an example for each. */
private fun z7SpanPart(text: String): PaperPart {
    val (dim, count, examples) = Z7SpanPostup
    return PaperPart("", text, 2, listOf(Rubric("Možnosti §1,7,49§ s príkladmi a prečo (§7^{\\dim}§)", 2, "gen:count")), Z7SpanPostup.solve {
        dim(Reasons.spanIsSubspace + " Dimenzia §d§ obalu §\\langle\\mathbf{x},\\mathbf{y}\\rangle§ je najviac §2§, lebo ho generuje súbor §(\\mathbf{x},\\mathbf{y})§ dĺžky §2§ a dimenzia nie je väčšia než dĺžka generujúceho súboru (Tvrzení 2.54). Platí to aj pre §\\mathbf{x}=\\mathbf{y}=\\theta§, keď je obal §\\{\\theta\\}§ s dimenziou §0§ a bázu nemá.")
        count("Pre §d=0§ je obal §\\{\\theta\\}§ a má §1=7^0§ prvok. Pre §d\\geq 1§ má §7^d§ prvkov, lebo každý prvok určuje jediná §d§-tica súradníc zo §\\mathbb{Z}_7§ (Poznámka 2.67). Obal má teda §1§, §7§ alebo §49§ vektorov.")
        examples("Pre §1§ prvok: §\\mathbf{x}=\\mathbf{y}=\\theta§, obal je §\\{\\theta\\}§.")
        examples("Pre §7§: napríklad §\\mathbf{x}=(1,0,0)§ a §\\mathbf{y}=(2,0,0)=2\\mathbf{x}§; obal sú násobky §\\mathbf{x}§, dimenzia §1§.")
        examples("Pre §49§: napríklad §\\mathbf{x}=(1,0,0)§ a §\\mathbf{y}=(0,1,0)§; súbor je LN, dimenzia §2§.")
    }, "§\\langle\\mathbf{x},\\mathbf{y}\\rangle§ má §7^d§ prvkov pre §d=\\dim\\langle\\mathbf{x},\\mathbf{y}\\rangle\\in\\{0,1,2\\}§: §1§ (§\\mathbf{x}=\\mathbf{y}=\\theta§), §7§ (napr. §\\mathbf{y}=2\\mathbf{x}\\neq\\theta§), §49§ (napr. §\\mathbf{e}_1,\\mathbf{e}_2§).")
}

/** Sova 2023: a subset of R^3 that is not a subspace, and why. */
private fun notSubspacePart(text: String): PaperPart {
    val (theta, _, _, end) = SubspacePostup
    return PaperPart("", text, 2, listOf(Rubric("Podmnožina, ktorá nie je podpriestor, a prečo", 2, "gen:subspace")), SubspacePostup.solve {
        theta("Napríklad §M=\\{(1,0,0)\\}§. Každý podpriestor obsahuje nulový vektor, lebo s §\\mathbf{x}§ obsahuje aj §0\\cdot\\mathbf{x}§ (Pozorovanie 2.8). Ale §(0,0,0)\\notin M§.", "0\\cdot(1,0,0)=(0,0,0)\\notin M")
        end("Množina nie je uzavretá ani na násobenie skalárom, preto §M§ nie je podpriestorom §\\mathbb{R}^3§.")
    }, "Napr. §\\{(1,0,0)\\}§: neobsahuje §\\theta§.")
}

/* ---- The papers ---------------------------------------------------------------------------------------------- */

val La1Papers: List<TestPaper> by lazy {
    listOf(
        marast2(),
        veverka2024(),
        vcelka2023(),
        TestPaper("sova-2025", "Test 21. 11. 2025 (variant Sova)", "Zadanie z fit-wiki, overené podľa fotky", listOf(
            p1(ParamKind.LN, listOf(pc(0, 2, 1, 2), listOf(Poly.of(2), Poly.of(-1), Poly.lin(1, 1), Poly.of(1)), pc(-1, 2, 2, 1)), null,
                m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ je soubor vektorů §((0,2,1,2),(2,-1,a+1,1),(-1,2,2,1))§ z prostoru §\mathbb{R}^4§ lineárně nezávislý.""")),
            sova2025P2(),
            cut("""Napište definici lineární nezávislosti.¦ Napište definici souřadnic vzhledem k bázi.¦ Rozhodněte, zda množina §\{(0,0,0,0),(1,0,0,0),(0,1,0,0),(1,1,0,0),(0,0,1,0)\}§ je podprostorem §\mathbb{Z}_2^4§. Tzn. ověřte splnění podmínek z definice podprostoru, nebo odůvodněte, proč množina podprostorem není.¦ Uvažujme §\mathbf{x},\mathbf{y},\mathbf{z}§ z §T^n§. Ukažte z definice, že §(\mathbf{x},\mathbf{y},\mathbf{z})§ je lineárně závislý, právě když je jeden vektor ze souboru lineární kombinací ostatních. Dokažte přímo z definice. Rozhodně se neodvolávejte na obecnější větu, která říká totéž pro libovolný počet vektorů, či její důsledky.""").let { t ->
                p3(listOf(defPart(concept("ln"), t[0], lnOnly), defPart(concept("suradnice"), t[1]), z2SetPart(t[2]), proofPart(proof("p-lzlk"), t[3], 5)))
            },
        )),
        TestPaper("slon-2024", "Test 20. 11. 2024 (variant Slon)", "Zadanie z fit-wiki, overené podľa fotky", listOf(
            p1(ParamKind.BASIS, listOf(pc(3, -1, 3), listOf(Poly.of(0), Poly.A, Poly.of(1)), pc(1, 1, 0)), null,
                m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ je soubor §((3,-1,3),(0,a,1),(1,1,0))§ bází §\mathbb{R}^3§.""")),
            slon2024P2(),
            cut("""Napište definici lineárně závislého souboru vektorů.¦ Napište definici lineárního obalu souboru vektorů.¦ Uvažujme §\mathbf{x},\mathbf{y}§ ze §\mathbb{Z}_7^3§. Kolik vektorů může obsahovat §\langle\mathbf{x},\mathbf{y}\rangle§? Uveďte pro každou možnost odpovídající příklad takových vektorů §\mathbf{x}§ a §\mathbf{y}§.¦ Dokažte, že ve vektorovém prostoru §\mathbb{R}^n§ platí: Máme-li vektory §\mathbf{x},\mathbf{y},\mathbf{z}\in\mathbb{R}^n§ takové, že §\mathbf{z}\in\langle\mathbf{x},\mathbf{y}\rangle§, potom §\langle\mathbf{x},\mathbf{y}\rangle=\langle\mathbf{x},\mathbf{y},\mathbf{z}\rangle§. K důkazu vám postačí definice daných pojmů. Rozhodně se neodvolávejte na obecnější větu, z které plyne totéž, jen pro více vektorů, a ani na její důsledky.""").let { t ->
                p3(listOf(defPart(concept("ln"), t[0], lzOnly), defPart(concept("obal"), t[1]), z7SpanPart(t[2]), proofPart(proof("p-obal"), t[3], 5)))
            },
        )),
        TestPaper("medved-2024", "Test 14. 11. 2024 (variant Medvěd)", "Zadanie z fit-wiki, overené podľa fotky", listOf(
            p1(ParamKind.SPAN, listOf(pc(3, -1, 2), pc(3, 2, -1)), listOf(Poly.of(2), Poly.of(-1), Poly.A),
                m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ patří vektor §(2,-1,a)\in\mathbb{R}^3§ do podprostoru §\langle(3,-1,2),(3,2,-1)\rangle\ssub\mathbb{R}^3§.""")),
            medved2024P2(),
            cut("""Napište definici lineárně nezávislého souboru.¦ Napište definici báze podprostoru.¦ Dokažte, že v §T^n§ nemůže být soubor vektorů §(\mathbf{x},\mathbf{y},\mathbf{x}-\mathbf{y})§ bází žádného §P\ssub T^n§. Pokud o souboru budete tvrdit nějakou vlastnost, dokažte ji přímo z definice.¦ Dokažte, že ve vektorovém prostoru §\mathbb{R}^n§ platí: Máme-li soubor §(\mathbf{u},\mathbf{v},\mathbf{w})§, který generuje podprostor §P\ssub\mathbb{R}^n§, kde §\dim P=3§, potom je tento soubor bází tohoto podprostoru. Při důkazu se neodvolávejte na obecnější větu, která říká totéž, jen pro soubory libovolné délky, a ani na žádnou větu, která z ní plyne.""").let { t ->
                p3(listOf(defPart(concept("ln"), t[0], lnOnly), defPart(concept("baza"), t[1]), proofPart(proof("p-xy"), t[2], 4), proofPart(proof("p-gen3"), t[3], 3)))
            },
        )),
        TestPaper("sova-2023", "Test 22. 11. 2023 (variant Sova)", "Zadanie z fit-wiki, overené podľa fotky; na origináli 7/7/7 bodov, tu 7/12/11 ako od roku 2024", listOf(
            p1(ParamKind.LN, listOf(pc(2, 1, -1), listOf(Poly.of(1), Poly.of(2), Poly.A), pc(2, 3, 2)), null,
                m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ je soubor vektorů §((2,1,-1),(1,2,a),(2,3,2))§ z prostoru §\mathbb{R}^3§ lineárně nezávislý.""")),
            sova2023P2(),
            cut("""Napište definici podprostoru.¦ Napište definici dimenze podprostoru.¦ Napište nějakou podmnožinu §\mathbb{R}^3§, která není jeho podprostorem. Zdůvodněte, proč daná podmnožina není podprostorem.¦ Dokažte: Mějme §(\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3)§ soubor vektorů z §P\ssub\mathbb{R}^n§, kde §\dim P=3§. Je-li tento soubor lineárně nezávislý, tak je bází podprostoru §P§. (Neodvolávat se na obecnější větu.)""").let { t ->
                p3(listOf(defPart(concept("podpriestor"), t[0]), defPart(concept("dimenzia"), t[1]), notSubspacePart(t[2]), proofPart(proof("p-dim3"), t[3], 5)))
            },
        )),
        TestPaper("slepice-2023", "Test 24. 11. 2023 (variant Slepice)", "Zadanie z fit-wiki, overené podľa fotky", listOf(
            p1(ParamKind.GENERATES, listOf(pc(0, 1, 5), listOf(Poly.of(1), Poly.of(2), Poly.A), pc(-1, 0, 2), pc(2, 1, 1)), null,
                m("""Zjistěte, pro které hodnoty §a\in\mathbb{R}§ soubor vektorů §((0,1,5),(1,2,a),(-1,0,2),(2,1,1))§ generuje §\mathbb{R}^3§.""")),
            slepice2023P2(),
            cut("""Napište definici lineárně závislého souboru.¦ Napište definici podprostoru.¦ Dokažte, že je-li §(\mathbf{x},\mathbf{y},\mathbf{z})§ lineárně závislý soubor vektorů z §\mathbb{R}^n§ a §\mathbf{v}\in\mathbb{R}^n§, potom je i soubor §(\mathbf{x},\mathbf{y},\mathbf{z},\mathbf{v})§ lineárně závislý.¦ Dokažte, že množina řešení homogenní soustavy lineárních rovnic je podprostorem.""").let { t ->
                p3(listOf(defPart(concept("ln"), t[0], lzOnly), defPart(concept("podpriestor"), t[1]), proofPart(proof("p-pridaj"), t[2], 4), proofPart(proof("p-homog"), t[3], 3, skip = setOf("Definícia podpriestoru so všetkými predpokladmi"))))
            },
        )),
    )
}

/* ---- Generated papers ------------------------------------------------------------------------------------------ */

private fun Random.pick(lo: Int, hi: Int) = nextInt(lo, hi + 1)

/** Problem 2 with new numbers, completing to a basis: the wording of sova 2025 or of slon 2024. */
private fun generatedComplete(r: Random): PaperProblem {
    val p = if (r.nextInt(4) == 0) 7 else 5
    val f = Field.Zp(p)
    while (true) {
        val u = f.vec(List(4) { r.pick(0, p - 1) }); val v = f.vec(List(4) { r.pick(0, p - 1) })
        if (u.isZero() || v.isZero() || !f.isLN(listOf(u, v))) continue
        if (u.count { it.isZero } > 1 || v.count { it.isZero } > 2) continue
        val x = f.vec(List(4) { r.pick(0, p - 1) })
        if (x.isZero()) continue
        val start = listOf(u, v)
        val z = zp(f)
        return if (r.nextBoolean()) {
            val t = cut("Doplňte soubor vektorů §${vstex(start)}§ na bázi §$z§.¦ Ve vámi nalezené bázi najděte souřadnice vektoru §\\mathbf{x}=${x.tex()}§.¦ Nalezněte nějaký nenulový vektor §\\mathbf{y}§ tak, aby soubor §(${u.tex()},${v.tex()},\\mathbf{y})§ byl LZ.¦ Kolik takových vektorů existuje?")
            val (a, b) = completePart(f, start, t[0])
            val build = { b: List<Vec> -> listOf(
                a,
                coordsPart(f, b, x, t[1], listOf(Rubric("Sústava pre súradnice: čo sú neznáme, odkiaľ stĺpce", 2, "coords"), Rubric("Súradnice §(\\mathbf{x})_{\\mathcal{B}}§", 2, "coords"))),
                lzVectorPart(f, u, v, t[2]),
                lzCountPart(p, t[3]),
            ) }
            paperProblem(2, "Doplnenie na bázu", 28, build(b), "complete", own = completionOwn(f, start, b, build))
        } else {
            val c3 = f.vec(listOf(1, 3, 0, 0))
            val t = cut("Doplňte soubor §${vstex(start)}§ na bázi §$z§. Označme §\\mathcal{B}§ vámi nalezenou bázi.¦ Nechť §\\mathbf{x}\\in $z§. Rozhodněte, která z následujících rovností je smysluplná, a pro ni spočítejte odpovídající §\\mathbf{x}§: §§${coordChoicesTex("(1,2)", "(1,2,1)", c3.tex())}§§¦ Spočtěte také §(${x.tex()})_{\\mathcal{B}}§.")
            val (a, b) = completePart(f, start, t[0])
            val build = { b: List<Vec> ->
                val (cs, c) = coordsLines(f, b, x, x.tex())
                listOf(
                    a,
                    sensePart(f, b, senseOptions + listOf(c3), t[1], listOf(Rubric("Zmysel má len c) a prečo", 3, "coords"), Rubric("Vektor §\\mathbf{x}§ pre c)", 2, "coords"))),
                    PaperPart("", t[2], 3, listOf(Rubric("Súradnice §${x.tex()}§", 3, "coords")), cs, "§(${x.tex()})_{\\mathcal{B}}=${c!!.tex()}§."),
                )
            }
            paperProblem(2, "Báza a súradnice", 28, build(b), "complete", own = completionOwn(f, start, b, build))
        }
    }
}

/** Problem 2 with new numbers: basis and dimension of a span in Z_p^4, its size, coordinates if possible. */
private fun generatedSpan(r: Random): PaperProblem {
    val p = listOf(3, 5, 5, 7).random(r)
    val f = Field.Zp(p)
    while (true) {
        val d = r.pick(2, 3)
        val gens = List(d) { f.vec(List(4) { r.pick(0, p - 1) }) }
        if (!f.isLN(gens)) continue
        val extra = List(4 - d) { val k = List(d) { r.pick(0, p - 1).q() }; f.combo(k, gens) }
        if (extra.any { it.isZero() }) continue
        val all = (gens + extra).shuffled(r)
        if (all.toSet().size < 4 || all.any { v -> v.count { it.isZero } > 2 }) continue
        val inside = r.nextBoolean()
        val x = if (inside) f.combo(List(d) { r.pick(1, p - 1).q() }, gens) else f.vec(List(4) { r.pick(0, p - 1) })
        if (!inside && f.inSpan(all, x)) continue
        val mtex = "M=\\langle " + all.joinToString(",") { it.tex() } + "\\rangle"
        val t = cut("Ve vektorovém prostoru §\\mathbb{Z}_{$p}^{4}§ najděte bázi a dimenzi podprostoru §$mtex§.¦ Kolik prvků má podprostor §M§ a proč?¦ Pokud je to možné, najděte souřadnice vektoru §\\mathbf{x}=${x.tex()}§ vzhledem k vámi nalezené bázi. Pokud to možné není, napište, co to pro daný vektor znamená.")
        val (a, b) = spanBasisPart(f, all, t[0], listOf(Rubric("Matica a GEM, hlavné stĺpce", 3, "span-basis"), Rubric("Báza a dimenzia so zdôvodnením", 3, "span-basis")))
        val build = { b: List<Vec> -> listOf(
            a,
            spanCountPart(p, b.size, t[1]),
            coordsPart(f, b, x, t[2], listOf(Rubric("Sústava pre súradnice: čo sú neznáme, odkiaľ stĺpce", 2, "coords"), Rubric("Súradnice §\\mathbf{x}§, alebo že §\\mathbf{x}\\notin M§", 2, "coords"))),
        ) }
        return paperProblem(2, "Báza a dimenzia obalu", 28, build(b), "span-basis", own = spanOwn(f, all, b, build))
    }
}

/** A whole new test from the templates: problem 1 and 2 with new numbers, problem 3 from the pool of definitions and proofs. */
fun la1GeneratedPaper(seed: Int): TestPaper {
    val r = Random(seed)
    val e1 = paramExercise(r)
    val p2 = if (r.nextBoolean()) generatedComplete(r) else generatedSpan(r)
    val defs = La1Concepts.filter { it.id != "trivialny" && it.id != "standardna" }.shuffled(r).take(2)
    val pf = La1Proofs.filter { it.minutes >= 6 }.random(r)
    val w1 = e1.solution(emptyList())
    val p1 = paperProblem(1, "Parameter \$a$", 16, listOf(
        PaperPart("", e1.prompt, 7, paramRubric("\$a$"), w1, w1.steps.last().say, estimate = false, review = "gen:param"),
    ), "param", note = HeaderP1)
    val p3 = p3(listOf(defPart(defs[0], m(defs[0].ask)), defPart(defs[1], m(defs[1].ask)), proofPart(pf, pf.task + (pf.foot?.let { " ($it)" } ?: ""), 7)))
    return TestPaper("gen-$seed", "Nové čísla", "Šablóny z testov 2023 až 2025", listOf(p1, p2, p3), generated = true)
}

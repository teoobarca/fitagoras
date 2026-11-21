package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import kotlin.random.Random
import dev.hamper.tabula.compat.JMath

/*
 * Test templates with new numbers every time. Numbers are drawn, then everything the student is asked is computed
 * by the exact code in la/ and checked by it; nothing is typed by hand. The shapes follow the past tests on fit-wiki
 * (2. zápočtový test 2023 to 2025) and MARAST lekce 292.
 */

fun vtex(v: Vec) = v.tex()
fun vstex(vs: List<Vec>) = "(" + vs.joinToString(",") { it.tex() } + ")"
fun ptex(v: PVec) = "(" + v.joinToString(",") { it.tex() } + ")"
fun fieldPow(f: Field, n: Int) = if (f is Field.Zp) "\\mathbb{Z}_{${f.p}}^{$n}" else "\\mathbb{R}^{$n}"

/** A numerical task carries the same field that its engine uses; callers cannot omit it. */
fun fieldTask(field: Field, task: String): String {
    val tex = if (field is Field.Zp) "\\mathbb{Z}_{${field.p}}" else "\\mathbb{R}"
    return "Nad $$tex$: $task"
}

/** The GEM chain as one walk (screens/GemWalk.kt): one kind of operation per ∼, the matrix in place from step to step. */
fun gemLines(e: Echelon): List<Formula> = listOf(Formula(GemWalk.of(e)))

/**
 * Back substitution from a matrix in HST, written out (the fresh-eyes rule: no value appears without its line): one line
 * per unknown, from the bottom row up, "the row's equation ⇒ the unknown". Free unknowns get [free] (default 0); with
 * [homogeneous] the right side is θ. Returns the lines and the values. A row 0 = c (no solution) is skipped.
 */
fun backSubst(f: Field, e: Echelon, names: List<String>, free: Map<Int, Frac> = emptyMap(), homogeneous: Boolean = false): Pair<List<Formula>, Vec> {
    val m = e.result
    val n = m.bar ?: m.n
    val x = MutableList(n) { free[it] ?: Frac.ZERO }
    val mod = if (f is Field.Zp) "\\pmod{${f.p}}" else ""
    val lines = ArrayList<Formula>()
    for (k in m.rows.indices.reversed()) {
        val row = m.rows[k]
        val c = (0 until n).firstOrNull { !row[it].isZero } ?: continue
        val rhs = if (homogeneous || m.bar == null) Frac.ZERO else row[n]
        var s = rhs
        for (j in c + 1 until n) s = f.sub(s, f.mul(row[j], x[j]))
        x[c] = f.div(s, row[c])
        val eq = Terms.lin((c until n).map { row[it] }, (c until n).map { names[it] }) + "=" + rhs.tex(true) + mod
        val known = (c + 1 until n).filter { !row[it].isZero }
        lines += Formula(eq + "\\ \\Rightarrow\\ " + names[c] + "=" + x[c].tex(true),
            if (known.isEmpty()) null else "dosadím " + known.joinToString(", ") { "§" + names[it] + "=" + x[it].tex(true) + "§" })
    }
    return lines to x
}

fun pgemLines(r: ParamResult): List<Formula> = listOf(Formula(GemWalk.of(r.start, r.steps)))

private fun Random.pick(lo: Int, hi: Int) = nextInt(lo, hi + 1)
private fun Random.nonzero(lo: Int, hi: Int): Int { while (true) { val x = pick(lo, hi); if (x != 0) return x } }

/* ---- 1. Parameter a: LN, basis, generates, membership ----------------------------------------------------- */

fun paramExercise(r: Random, kind: ParamKind = ParamKind.entries.random(r)): Exercise {
    while (true) {
        val n = 3
        val m = when (kind) { ParamKind.LN -> r.pick(2, 3); ParamKind.BASIS -> 3; ParamKind.GENERATES -> r.pick(3, 4); ParamKind.SPAN -> 2 }
        val ints = List(m) { List(n) { r.pick(-3, 4) } }
        val pv = ints.map { v -> v.map { Poly.of(it) } }.toMutableList()
        var x: PVec? = null
        if (kind == ParamKind.SPAN) {
            val xi = List(n) { r.pick(-4, 4) }
            val at = r.pick(0, n - 1)
            x = xi.mapIndexed { j, c -> if (j == at) Poly.lin(if (r.nextBoolean()) 0 else c, 1) else Poly.of(c) }
        } else {
            val which = r.pick(1, m - 1); val at = r.pick(0, n - 1)
            pv[which] = pv[which].mapIndexed { j, p -> if (j == at) Poly.lin(if (r.nextInt(3) == 0) ints[which][j] else 0, 1) else p }
        }
        if (pv.any { v -> v.all { it.isZero } }) continue
        val res = paramSolve(kind, pv, x) ?: continue
        if (res.critical.size != 1) continue
        val c = res.critical[0]
        if (kotlin.math.abs(c.num) > 12 || c.den > 4) continue
        val probes = listOf(c, c + Frac.ONE, c - Frac.ONE, Frac.ZERO, 7.q(), Frac.of(1, 3))
        if (probes.any { paramHoldsByRank(kind, pv, x, it) != res.holds(it) }) continue
        if (kind == ParamKind.LN && m == 3 && n == 3) return build(kind, pv, x, res, "LN v $\\mathbb{R}^3$, šablóna z testu 22. 11. 2023 (variant Sova)")
        return build(kind, pv, x, res, when (kind) {
            ParamKind.LN -> "Šablóna z testu 21. 11. 2025 (variant Sova), príklad 1"
            ParamKind.BASIS -> "Šablóna z testu 20. 11. 2024 (variant Slon), príklad 1"
            ParamKind.GENERATES -> "Šablóna z testu 24. 11. 2023 (variant Slepice), príklad 1"
            ParamKind.SPAN -> "Šablóna z testu 14. 11. 2024 (variant Medvěd), príklad 1"
        })
    }
}

fun paramText(kind: ParamKind, pv: List<PVec>, x: PVec?): String {
    val set = "(" + pv.joinToString(",") { ptex(it) } + ")"
    val n = pv[0].size
    return when (kind) {
        ParamKind.LN -> "Zjistěte, pro které hodnoty \$a\\in\\mathbb{R}$ je soubor vektorů $$set$ z prostoru $\\mathbb{R}^{$n}$ lineárně nezávislý."
        ParamKind.BASIS -> "Zjistěte, pro které hodnoty \$a\\in\\mathbb{R}$ je soubor $$set$ bází $\\mathbb{R}^{$n}$."
        ParamKind.GENERATES -> "Zjistěte, pro které hodnoty \$a\\in\\mathbb{R}$ soubor vektorů $$set$ generuje $\\mathbb{R}^{$n}$."
        ParamKind.SPAN -> "Zjistěte, pro které hodnoty \$a\\in\\mathbb{R}$ patří vektor $${ptex(x!!)}\\in\\mathbb{R}^{$n}$ do podprostoru $\\langle " + pv.joinToString(",") { ptex(it) } + "\\rangle\\ssub\\mathbb{R}^{$n}$."
    }
}

fun paramConclusion(kind: ParamKind, c: Frac): String {
    val ct = c.tex(true)
    return when (kind) {
        ParamKind.LN -> "Súbor je LN práve pre \$a\\neq $ct$."
        ParamKind.BASIS -> "Súbor je bázou práve pre \$a\\neq $ct$."
        ParamKind.GENERATES -> "Súbor generuje práve pre \$a\\neq $ct$."
        ParamKind.SPAN -> "Vektor patrí do obalu práve pre \$a=$ct$."
    }
}

/**
 * The options a), b), c) of "Rozhodněte, která z následujících rovností je smysluplná" one under another (ZASADY 11:
 * parallel things under each other, never a), b), c) running through a paragraph), word for word as the test has them.
 */
fun coordChoicesTex(vararg rhs: String): String =
    "\\begin{aligned}" + rhs.mapIndexed { i, r -> "&\\text{${'a' + i}) }(\\mathbf{x})_{\\mathcal{B}}=$r" }.joinToString(",\\\\") + ".\\end{aligned}"

/** What the flipped conclusion gets wrong: the property fails exactly where the deciding entry is zero. */
fun paramWrongFlip(kind: ParamKind, c: Frac): String = when (kind) {
    ParamKind.SPAN -> "Obrátil si podmienku. Vektor patrí do obalu, keď pravá strana po GEM **nie je** hlavný stĺpec, a to je práve vtedy, keď je rozhodujúci prvok nulový, teda len pre §a=${c.tex(true)}§."
    else -> "Obrátil si podmienku. Pre §a=${c.tex(true)}§ je rozhodujúci prvok po GEM nulový, stĺpec ostane vedľajší a vlastnosť vtedy **neplatí**; pre ostatné §a§ platí."
}

/** What "for every a" gets wrong: the deciding entry is zero for one value. */
fun paramWrongAll(kind: ParamKind, c: Frac): String = when (kind) {
    ParamKind.SPAN -> "Pre §a\\neq ${c.tex(true)}§ vektor do obalu nepatrí. Pravá strana po GEM je vtedy hlavný stĺpec a sústava nemá riešenie."
    ParamKind.LN -> "Pre §a=${c.tex(true)}§ je rozhodujúci prvok po GEM nulový, takže vlastnosť pre túto hodnotu neplatí. Posledný stĺpec ostane vedľajší, homogénna sústava má aj netriviálne riešenie a súbor je LZ."
    ParamKind.BASIS -> "Pre §a=${c.tex(true)}§ je rozhodujúci prvok po GEM nulový, takže vlastnosť pre túto hodnotu neplatí. Posledný stĺpec ostane vedľajší, súbor je LZ, a tak nie je bázou."
    ParamKind.GENERATES -> "Pre §a=${c.tex(true)}§ je rozhodujúci prvok po GEM nulový, takže vlastnosť pre túto hodnotu neplatí. Posledný riadok matice sústavy je vtedy nulový, a tak pre niektoré pravé strany sústava riešenie nemá."
}

/** The method of a parameter problem: a system whose last column decides, read after GEM. */
val ParamPostups: Map<ParamKind, Postup> = ParamKind.entries.associateWith { kind ->
    postup(
        "Zostav sústavu" to when (kind) {
            ParamKind.SPAN -> "Vektory obalu zapíš do stĺpcov, §\\mathbf{x}§ ako pravú stranu: hľadáš koeficienty kombinácie."
            ParamKind.GENERATES -> "Vektory zapíš do stĺpcov; pravá strana je ľubovoľná, o riešiteľnosti rozhodne matica sústavy."
            else -> "Vektory zapíš do stĺpcov homogénnej sústavy: hľadáš koeficienty kombinácie rovnej §\\theta§."
        },
        "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar; nedeľ výrazom s §a§ a riadky s parametrom nechaj nakoniec.",
        "Kedy je rozhodujúci prvok nula?" to when (kind) {
            ParamKind.SPAN -> "Sústava má riešenie, keď pravá strana nie je hlavný stĺpec; nájdi §a§, pre ktoré je jej prvok nula."
            ParamKind.GENERATES -> "Generuje, keď je hlavný stĺpec v každom riadku; nájdi §a§, pre ktoré posledný riadok vyjde nulový."
            else -> "Nájdi §a§, pre ktoré posledný prvok vyjde nula: vtedy ostane vedľajší stĺpec."
        },
        "Záver" to "Napíš, pre ktoré §a§ vlastnosť platí a pre ktoré nie.",
    )
}

fun paramSolution(kind: ParamKind, res: ParamResult, pv: List<PVec>): Solution {
    val (system, gem, decide, end) = ParamPostups.getValue(kind)
    val c = res.critical[0]
    val ct = c.tex(true)
    val reordered = res.order != res.order.sorted()
    val d = res.deciders.filter { !it.isZero }
    val dt = d.joinToString(",\\ ") { it.tex() }
    return ParamPostups.getValue(kind).solve {
        when (kind) {
            ParamKind.SPAN -> system("Hľadám §${(1..pv.size).joinToString(",") { "\\alpha_$it" }}\\in\\mathbb{R}§ s §\\alpha_1\\mathbf{x}_1+\\dots=\\mathbf{x}§, preto dané vektory idú do stĺpcov. Pravá strana je §\\mathbf{x}§.")
            ParamKind.GENERATES -> system("Súbor generuje §\\mathbb{R}^{${pv[0].size}}§ práve vtedy, keď sústava §\\sum\\alpha_i\\mathbf{x}_i=\\mathbf{b}§ má riešenie pre každé §\\mathbf{b}§. Vektory píšem do stĺpcov a upravujem len maticu sústavy: či je riešiteľná pre každú pravú stranu, rozhodne ona." +
                (if (reordered) " Vektor s parametrom dávam do posledného stĺpca, poradie vektorov na obale nič nemení." else ""))
            else -> system("Z definície LN hľadám všetky §\\alpha_i§ so §\\sum\\alpha_i\\mathbf{x}_i=\\theta§, preto vektory píšem do stĺpcov. Sústava je homogénna, pravú stranu nepíšem." +
                (if (reordered) " Vektor s parametrom dávam do posledného stĺpca. Na poradí pri LN nezáleží, lebo prehodením vektorov sa nezmení, ktoré kombinácie dajú §\\theta§ (Pozorovanie 2.25)." else ""))
        }
        gem("Upravujem bez delenia výrazom s §a§, ten môže byť nulový. Na konci o všetkom rozhodnú prvky s parametrom v posledných riadkoch.", pgemLines(res))
        when (kind) {
            ParamKind.BASIS -> decide("Posledný stĺpec je hlavný práve vtedy, keď §$dt\\neq 0§, teda pre §a\\neq $ct§. Vtedy má homogénna sústava jediné riešenie a súbor je LN. Súbor troch LN vektorov má dĺžku §3=\\dim\\mathbb{R}^3§. " + Reasons.lnOfDim)
            ParamKind.LN -> decide("Pre §a\\neq $ct§ je aj posledný stĺpec hlavný, sústava má len triviálne riešenie a súbor je LN. Pre §a=$ct§ ostane vedľajší stĺpec, existuje netriviálne riešenie a súbor je LZ.")
            ParamKind.GENERATES -> decide("Súbor generuje §\\mathbb{R}^3§, keď má sústava riešenie pre každú pravú stranu, teda keď je hlavný stĺpec v každom riadku. Posledný riadok je nenulový práve pre §a\\neq $ct§.")
            ParamKind.SPAN -> decide("Sústava má riešenie práve vtedy, keď posledný stĺpec nie je hlavný. To nastane, keď §$dt=0§, teda pre §a=$ct§.")
        }
        end(paramConclusion(kind, c))
    }
}

private fun build(kind: ParamKind, pv: List<PVec>, x: PVec?, res: ParamResult, src: String): Exercise {
    val c = res.critical[0]
    val text = paramText(kind, pv, x)
    val askValue = when (kind) {
        ParamKind.SPAN -> "Pre ktoré \$a$ vektor do obalu patrí?"
        else -> "Pre ktorú hodnotu \$a$ to neplatí?"
    }
    val concl = listOf(
        paramConclusion(kind, c),
        when (kind) {
            ParamKind.SPAN -> "Vektor patrí do obalu pre každé \$a\\neq ${c.tex(true)}$."
            else -> paramConclusion(kind, c).replace("\\neq", "=")
        },
        when (kind) { ParamKind.SPAN -> "Vektor patrí do obalu pre každé \$a\\in\\mathbb{R}$."; else -> paramConclusion(kind, c).substringBefore(" práve") + " pre každé \$a\\in\\mathbb{R}$." },
    )
    val order = listOf(0, 1, 2).shuffled(Random(c.num * 31 + c.den))
    val valueHint = m(when (kind) {
        ParamKind.SPAN -> "Vektory obalu do stĺpcov, §\\mathbf{x}§ ako pravú stranu, GEM tak, aby parameter ostal až na konci. Vektor do obalu patrí, keď pravá strana nie je hlavný stĺpec, teda keď je jej prvok s parametrom nula."
        ParamKind.GENERATES -> "Vektory do stĺpcov, GEM tak, aby parameter ostal až na konci. Súbor negeneruje, keď posledný riadok vyjde nulový: nájdi §a§, pre ktoré sa to stane."
        else -> "Vektory do stĺpcov, GEM tak, aby parameter ostal až na konci. Vlastnosť neplatí, keď posledný prvok s parametrom vyjde nula a ostane vedľajší stĺpec."
    })
    return Exercise(
        template = "param", label = "Parameter \$a$", src = src, prompt = text,
        parts = listOf(
            { _ -> Part(askValue, Answer.Numbers(listOf("a"), null, listOf(c), "\$a=${c.tex(true)}$", fractions = true, diagnose = paramDiagnosis(c, res.deciders)), hint = valueHint, aim = 2) },
            { _ -> Part("Ako znie záver?", Answer.Choice(order.map { concl[it] }, order.indexOf(0), "Rozhoduje posledný riadok po GEM: ${paramConclusion(kind, c)}",
                wrong = mapOf(order.indexOf(1) to paramWrongFlip(kind, c), order.indexOf(2) to paramWrongAll(kind, c))),
                hint = m("Pri hodnote z predošlej časti je rozhodujúci prvok nula, pri všetkých ostatných §a§ nie; podľa toho rozhodni o oboch prípadoch."), aim = 3) },
        ),
        postup = ParamPostups.getValue(kind),
        solution = { paramSolution(kind, res, pv) },
        hint = "Vektory do stĺpcov, GEM tak, aby parameter ostal až na konci. Potom rozober, kedy je posledné číslo nula.",
    )
}

/* ---- 2. Complete to a basis of Z_p^4, coordinates, LZ vectors or which equality makes sense -------------- */

fun completeExercise(r: Random): Exercise {
    if (r.nextInt(3) == 0) return completeRealExercise(r)
    val p = if (r.nextInt(4) == 0) 7 else 5
    val f = Field.Zp(p)
    val n = 4
    while (true) {
        val u = f.vec(List(n) { r.pick(0, p - 1) }); val v = f.vec(List(n) { r.pick(0, p - 1) })
        if (u.isZero() || v.isZero() || !f.isLN(listOf(u, v))) continue
        if (u.count { it.isZero } > 1 || v.count { it.isZero } > 2) continue
        val start = listOf(u, v)
        val sure = f.completeWithUnitVectors(start)
        val variant = r.nextInt(2)
        val x = f.vec(List(n) { r.pick(0, p - 1) })
        if (x.isZero()) continue
        val zp = "\\mathbb{Z}_{$p}^{4}"
        val text = if (variant == 0)
            "Doplňte soubor vektorů $${vstex(start)}$ na bázi $$zp$. Ve vámi nalezené bázi najděte souřadnice vektoru $\\mathbf{x}=${vtex(x)}$. Nalezněte nějaký nenulový vektor $\\mathbf{y}$ tak, aby soubor $(${u.tex()},${v.tex()},\\mathbf{y})$ byl LZ. Kolik takových vektorů existuje?"
        else {
            "Doplňte soubor $${vstex(start)}$ na bázi $$zp$. Označme $\\mathcal{B}$ vámi nalezenou bázi. Nechť $\\mathbf{x}\\in $zp$. Rozhodněte, která z následujících rovností je smysluplná, a pro ni spočítejte odpovídající $\\mathbf{x}$: $$${coordChoicesTex("(1,2)", "(1,2,1)", vtex(f.vec(listOf(1, 3, 0, 0))))}$$ Spočtěte také $(${vtex(x)})_{\\mathcal{B}}$."
        }
        fun basisOf(prev0: Any?) = start + added(prev0)
        val parts = ArrayList<(List<Any>) -> Part>()
        parts += { _ -> completionPart(f, start, sure) }
        if (variant == 0) {
            parts += { prev ->
                val b = basisOf(prev[0])
                val c = f.coords(b, x)!!
                Part("Súradnice $\\mathbf{x}=${vtex(x)}$ v tvojej báze:", Answer.Numbers(listOf("\\alpha_1", "\\alpha_2", "\\alpha_3", "\\alpha_4"), p, c, "$(\\mathbf{x})_{\\mathcal{B}}=${vtex(c)}$", diagnose = coordsDiagnosis(f, b, x, c)),
                    hint = m("Rieš sústavu: stĺpce sú vektory tvojej bázy, pravá strana §\\mathbf{x}§."), aim = 1)
            }
            parts += { _ ->
                val cnt = pow(p, 2) - 1
                Part("Koľko nenulových $\\mathbf{y}$ urobí súbor LZ?", Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(cnt)), "$$cnt$", diagnose = lzCountDiagnosis(p, n, 2)),
                    "Súbor je LZ práve vtedy, keď $\\mathbf{y}\\in\\langle\\mathbf{u},\\mathbf{v}\\rangle$. Plynie to z Viet 2.39 a 2.42. Ten obal má dimenziu 2, teda $${p}^2=${p * p}$ prvkov (Poznámka 2.67), bez nuly $${cnt}$.",
                    hint = m("Súbor je LZ práve vtedy, keď §\\mathbf{y}§ leží v obale daných dvoch vektorov. Obal dimenzie §d§ má §p^d§ prvkov; nulový vektor nerátaj."), aim = 3)
            }
        } else {
            parts += { _ -> Part("Ktorá rovnosť je zmysluplná?", Answer.Choice(listOf("a)", "b)", "c)"), 2, "Len c): báza $$zp$ má 4 prvky, súradnice sú preto štvorica. Súradnice sú koeficienty lineárnej kombinácie vektorov bázy, jeden pri každom z nich (Definícia 2.68). Dvojica a trojica nemajú pri štyroch vektoroch bázy zmysel.",
                wrong = mapOf(
                    0 to "Dvojica by sedela pre bázu s dvoma prvkami, napríklad podpriestoru dimenzie §2§. Súradníc je toľko, koľko prvkov má báza.",
                    1 to "Trojica by sedela pre bázu s tromi prvkami, napríklad podpriestoru dimenzie §3§. Súradníc je toľko, koľko prvkov má báza.",
                )), hint = m("Súradníc je toľko, koľko prvkov má báza."), aim = 1) }
            parts += { prev ->
                val b = basisOf(prev[0])
                val xx = f.fromCoords(b, f.vec(listOf(1, 3, 0, 0)))
                Part("Ktoré $\\mathbf{x}$ má súradnice $${vtex(f.vec(listOf(1, 3, 0, 0)))}$?", Answer.Numbers(listOf("x_1", "x_2", "x_3", "x_4"), p, xx, "$\\mathbf{x}=${vtex(xx)}$", diagnose = vectorFromCoordsDiagnosis(f, b, f.vec(listOf(1, 3, 0, 0)), xx)),
                    hint = m("Súradnice sú koeficienty: vynásob nimi vektory tvojej bázy a sčítaj."), aim = 2)
            }
            parts += { prev ->
                val b = basisOf(prev[0])
                val c = f.coords(b, x)!!
                Part("A súradnice $${vtex(x)}$ v tvojej báze:", Answer.Numbers(listOf("\\alpha_1", "\\alpha_2", "\\alpha_3", "\\alpha_4"), p, c, "$${vtex(c)}$", diagnose = coordsDiagnosis(f, b, x, c)),
                    hint = m("Rieš sústavu: stĺpce sú vektory tvojej bázy, pravá strana daný vektor."), aim = 3)
            }
        }
        val postup = if (variant == 0) CompletePostup else CompleteSensePostup
        return Exercise(
            "complete", "Doplnenie na bázu", if (variant == 0) "Šablóna z testu 21. 11. 2025 (variant Sova), príklad 2" else "Šablóna z testu 20. 11. 2024 (variant Slon), príklad 2",
            text, parts = parts, postup = postup,
            solution = { prev ->
                val picked = added(prev.getOrNull(0)).takeIf { it.size == 2 && f.rank(start + it) == 4 } ?: sure.map { unit(n, it) }
                val b = start + picked
                val ech = f.echelon(completionMatrix(start))
                val names = (1..4).map { "\\mathbf{b}_$it" }
                postup.solve {
                    completeBasisSteps(postup[0], f, ech, picked, sure, b)
                    if (variant == 0) {
                        val (_, coords, lz, count) = postup
                        val cx = f.coords(b, x)!!
                        coords("Súradnice sú koeficienty kombinácie, preto hľadám §\\alpha_1,\\dots,\\alpha_4\\in\\mathbb{Z}_$p§ so §\\sum\\alpha_i\\mathbf{b}_i=\\mathbf{x}§. Stĺpce sú vektory bázy.", gemLines(f.echelon(Mat.columns(b, listOf(x)))))
                        coords("Dosadzujem zdola: z posledného riadku jednu neznámu, potom ďalšie.", backSubst(f, f.echelon(Mat.columns(b, listOf(x))), (1..4).map { "\\alpha_$it" }).first + Formula("(\\mathbf{x})_{\\mathcal{B}}=${vtex(cx)}"))
                        coords("Skúška: kombinácia bázy s týmito koeficientmi dá §\\mathbf{x}§.", "${Terms.lin(cx, names)}=${vtex(f.fromCoords(b, cx))}")
                        val y = f.plus(u, v)
                        lz("Stačí vziať §\\mathbf{y}§ z obalu §\\mathbf{u},\\mathbf{v}§, napríklad ich súčet. Kombinácia nižšie je potom netriviálna, súbor je LZ.",
                            "\\mathbf{y}=\\mathbf{u}+\\mathbf{v}=${vtex(y)},\\quad \\mathbf{u}+\\mathbf{v}-\\mathbf{y}=\\theta")
                        count("Súbor §(\\mathbf{u},\\mathbf{v},\\mathbf{y})§ je LZ práve vtedy, keď §\\mathbf{y}\\in\\langle\\mathbf{u},\\mathbf{v}\\rangle§. Vektory §\\mathbf{u},\\mathbf{v}§ sú totiž LN: vektor v obale je ich kombináciou a vektor mimo obalu súbor LN nechá (Veta 2.39 a Veta 2.42). Obal má dimenziu §2§, teda §${p}^2§ prvkov; bez nulového vektora ich je §${p * p - 1}§.")
                    } else {
                        val (_, sense, fromCoords, coords) = postup
                        sense("Súradníc je toľko, koľko prvkov má báza, a báza §\\mathbb{Z}_{$p}^{4}§ má štyri. Zmysel má preto len c), štvorica.")
                        val xx = f.fromCoords(b, f.vec(listOf(1, 3, 0, 0)))
                        fromCoords("Súradnice sú koeficienty, takže vektor je kombinácia bázy s nimi.", "\\mathbf{x}=${Terms.lin(listOf(1.q(), 3.q(), 0.q(), 0.q()), names)}=${Terms.lin(listOf(1.q(), 3.q()), listOf(vtex(b[0]), vtex(b[1])))}=${vtex(xx)}")
                        coords("Súradnice daného vektora: sústava so stĺpcami z bázy a pravou stranou z neho, potom spätné dosadenie.", gemLines(f.echelon(Mat.columns(b, listOf(x)))))
                        coords("Riešenie je jediné, lebo báza je LN. Dosadzujem zdola:", backSubst(f, f.echelon(Mat.columns(b, listOf(x))), (1..4).map { "\\alpha_$it" }).first + Formula("(${vtex(x)})_{\\mathcal{B}}=${vtex(f.coords(b, x)!!)}"))
                    }
                }
            },
            hint = "Dané vektory a za ne štandardnú bázu do stĺpcov, GEM. Hlavné stĺpce sú báza. Súradnice: sústava so stĺpcami z bázy a pravou stranou x.",
        )
    }
}

val CompletePostup = postup(
    "Doplň na bázu" to "Dané vektory a za ne štandardnú bázu do stĺpcov, GEM; hlavné stĺpce sú báza.",
    "Súradnice §\\mathbf{x}§" to "Rieš sústavu: stĺpce sú vektory bázy, pravá strana §\\mathbf{x}§.",
    "Vektor §\\mathbf{y}§, s ktorým je súbor LZ" to "Vezmi §\\mathbf{y}§ z obalu daných vektorov, napríklad ich súčet.",
    "Koľko je takých §\\mathbf{y}§?" to "LZ robia práve nenulové prvky obalu; obal dimenzie §d§ má §p^d§ prvkov.",
)

/** The vectors the student added in the first part (an Answer.Vectors list), empty before it. */
private fun added(prev0: Any?): List<Vec> = (prev0 as? List<*>)?.mapNotNull { e -> (e as? List<*>)?.firstOrNull()?.let { v -> (v as List<*>).map { it as Frac } } } ?: emptyList()

/** e_k for a unit vector, the vector itself otherwise. */
private fun vecName(v: Vec): String {
    val i = v.indexOfFirst { !it.isZero }
    return if (i >= 0 && v.count { !it.isZero } == 1 && v[i] == Frac.ONE) "\\mathbf{e}_{${i + 1}}" else v.tex()
}

/**
 * Complete (u, v) to a basis: the student writes the two vectors they add, any that make the set LN; e₁ … e₄ are one
 * tap each, other vectors are typed (Teo, 18. 11.: a completion has many right answers, and the next parts follow it).
 */
private fun completionPart(f: Field, start: List<Vec>, sure: List<Int>): Part {
    val n = start[0].size
    return Part(
        "Ktoré dva vektory doplníš?",
        Answer.Vectors(List(n) { "x_{${it + 1}}" }, (f as? Field.Zp)?.p, 2..2, entry = "vektor", check = { e -> judgeCompletion(f, start, e) },
            example = sure.map { listOf(unit(n, it)) }, quick = List(n) { "\\mathbf{e}_{${it + 1}}" to unit(n, it) }),
        hint = m("Dané vektory a za ne štandardnú bázu zapíš do stĺpcov a uprav GEM. Hlavné stĺpce sú báza: doplň vektory §\\mathbf{e}_i§, ktoré sú pri nich. Správne je aj iné doplnenie, s ktorým je súbor LN."),
        aim = 0,
    )
}

/** The completion of (u, v) by the whole standard basis and GEM, with the student's own completion checked by GEM if it differs. */
private fun SolutionScope.completeBasisSteps(basis: Aim, f: Field, ech: Echelon, picked: List<Vec>, sure: List<Int>, b: List<Vec>) {
    basis(Reasons.completeColumns, gemLines(ech))
    val names = picked.joinToString(",", transform = ::vecName)
    if (picked == sure.sorted().map { unit(b[0].size, it) }) basis("Hlavné stĺpce sú §\\mathbf{u},\\mathbf{v},$names§. Vedľajšie stĺpce sú kombináciou predošlých, tie vynechám. " + Reasons.pivotColumnsLN)
    else basis("Hlavné stĺpce sú §\\mathbf{u},\\mathbf{v},${sure.sorted().joinToString(",") { "\\mathbf{e}_${it + 1}" }}§. Tvoje doplnenie §$names§ overím GEM: všetky štyri stĺpce musia byť hlavné, potom má homogénna sústava len triviálne riešenie a súbor je LN.", gemLines(f.echelon(Mat.columns(b))))
    basis(Reasons.lnOfDim + " Báza je §\\mathcal{B}=${vstex(b)}§.")
}

/**
 * The same problem over R, as MARAST #13494 asks it ("doplňte jej na bázi prostoru ℝ⁴"): complete, then coordinates.
 * Every 2×2 minor of (u v) is 0 or ±1, so the coordinates in any right completion (u, v, e_i, e_j) are integers.
 */
private fun completeRealExercise(r: Random): Exercise {
    val f = Field.R
    val n = 4
    while (true) {
        val u = f.vec(List(n) { r.pick(-1, 2) }); val v = f.vec(List(n) { r.pick(-1, 2) })
        if (u.count { it.isZero } > 1 || v.count { it.isZero } > 2) continue
        val minors = (0 until n).flatMap { i -> (i + 1 until n).map { j -> u[i] * v[j] - u[j] * v[i] } }
        if (minors.all { it.isZero } || minors.any { kotlin.math.abs(it.num) > 1 }) continue
        val start = listOf(u, v)
        fun unitsBasis(picked: Set<Int>) = start + picked.sorted().map { unit(n, it) }
        fun basisOf(prev0: Any?) = start + added(prev0)
        val x = f.vec(List(n) { r.pick(-3, 4) })
        if (x.isZero()) continue
        val good = (0 until n).flatMap { i -> (i + 1 until n).map { j -> setOf(i, j) } }.filter { f.rank(unitsBasis(it)) == n }
        if (good.any { s -> f.coords(unitsBasis(s), x)!!.any { kotlin.math.abs(it.num) > 12 } }) continue
        val sure = f.completeWithUnitVectors(start)
        val parts = listOf<(List<Any>) -> Part>(
            { _ -> completionPart(f, start, sure) },
            { prev ->
                val b = basisOf(prev[0])
                val c = f.coords(b, x)!!
                Part(m("Súradnice §\\mathbf{x}=${vtex(x)}§ v tvojej báze:"), Answer.Numbers(listOf("\\alpha_1", "\\alpha_2", "\\alpha_3", "\\alpha_4"), null, c, m("§(\\mathbf{x})_{\\mathcal{B}}=${vtex(c)}§"), fractions = true, diagnose = coordsDiagnosis(f, b, x, c)),
                    hint = m("Rieš sústavu: stĺpce sú vektory tvojej bázy, pravá strana §\\mathbf{x}§."), aim = 1)
            },
        )
        return Exercise(
            "complete", "Doplnenie na bázu", "Šablóna z testu 21. 11. 2025 (variant Sova), príklad 2, nad §\\mathbb{R}^4§ ako MARAST #13494".replace('§', '$'),
            m("Doplňte soubor vektorů §${vstex(start)}§ na bázi §\\mathbb{R}^4§. Ve vámi nalezené bázi najděte souřadnice vektoru §\\mathbf{x}=${vtex(x)}§."),
            parts = parts, postup = CompleteRealPostup,
            solution = { prev ->
                val picked = added(prev.getOrNull(0)).takeIf { it.size == 2 && f.rank(start + it) == n } ?: sure.map { unit(n, it) }
                val b = start + picked
                val names = (1..4).map { "\\mathbf{b}_$it" }
                val (basis, coords) = CompleteRealPostup
                CompleteRealPostup.solve {
                    completeBasisSteps(basis, f, f.echelon(completionMatrix(start)), picked, sure, b)
                    val cx = f.coords(b, x)!!
                    val e = f.echelon(Mat.columns(b, listOf(x)))
                    coords("Súradnice sú koeficienty kombinácie, preto hľadám §\\alpha_1,\\dots,\\alpha_4\\in\\mathbb{R}§ so §\\sum\\alpha_i\\mathbf{b}_i=\\mathbf{x}§. Stĺpce sú vektory bázy.", gemLines(e))
                    coords("Dosadzujem zdola: z posledného riadku jednu neznámu, potom ďalšie.", backSubst(f, e, (1..4).map { "\\alpha_$it" }).first + Formula("(\\mathbf{x})_{\\mathcal{B}}=${vtex(cx)}"))
                    coords("Skúška: kombinácia bázy s týmito koeficientmi dá §\\mathbf{x}§.", "${Terms.lin(cx, names)}=${vtex(f.fromCoords(b, cx))}")
                }
            },
            hint = "Dané vektory a za ne štandardnú bázu do stĺpcov, GEM. Hlavné stĺpce sú báza. Súradnice: sústava so stĺpcami z bázy a pravou stranou x.",
        )
    }
}

val CompleteRealPostup = postup(
    "Doplň na bázu" to "Dané vektory a za ne štandardnú bázu do stĺpcov, GEM; hlavné stĺpce sú báza.",
    "Súradnice §\\mathbf{x}§" to "Rieš sústavu: stĺpce sú vektory bázy, pravá strana §\\mathbf{x}§.",
)

val CompleteSensePostup = postup(
    "Doplň na bázu" to "Dané vektory a za ne štandardnú bázu do stĺpcov, GEM; hlavné stĺpce sú báza.",
    "Ktorá rovnosť má zmysel?" to "Súradníc je toľko, koľko prvkov má báza.",
    "Vektor zo súradníc" to "Súradnice sú koeficienty: vynásob nimi vektory bázy a sčítaj.",
    "Súradnice daného vektora" to "Rieš sústavu: stĺpce sú vektory bázy, pravá strana daný vektor.",
)

/* ---- 3. Basis and dimension of a span in Z_p^4 or R^4, its size, coordinates if possible, another basis --------------------------- */

fun spanBasisExercise(r: Random): Exercise {
    // over R as on the test 24. 11. 2023 (variant Včelka) and MARAST #13493, over Z_p as on the tests 2022 to 2024
    val f: Field = if (r.nextInt(3) == 0) Field.R else Field.Zp(listOf(3, 5, 5, 7).random(r))
    val p = (f as? Field.Zp)?.p
    fun draw() = if (p != null) Frac.of(r.pick(0, p - 1)) else r.pick(-2, 3).q()
    while (true) {
        val d = r.pick(2, 3)
        val gens = List(d) { List(4) { draw() } }
        if (!f.isLN(gens)) continue
        val extra = List(4 - d) { val k = List(d) { if (p != null) Frac.of(r.pick(0, p - 1)) else r.pick(-2, 2).q() }; f.combo(k, gens) }
        if (extra.any { it.isZero() }) continue
        val all = (gens + extra).shuffled(r)
        if (all.toSet().size < 4 || all.any { v -> v.count { it.isZero } > 2 }) continue
        if (p == null && all.any { v -> v.any { kotlin.math.abs(it.num) > 9 } }) continue
        // over R the test asks the coordinates outright, so x is in M there
        val inside = p == null || r.nextBoolean()
        val x = if (inside) f.combo(List(d) { if (p != null) Frac.of(r.pick(1, p - 1)) else r.nonzero(-2, 2).q() }, gens) else List(4) { draw() }
        if (!inside && f.inSpan(all, x)) continue
        if (p == null && x.any { kotlin.math.abs(it.num) > 12 }) continue
        val mtex = "M=\\langle " + all.joinToString(",") { it.tex() } + "\\rangle"
        val sp = fieldPow(f, 4)
        val text = m(if (p != null) "Ve vektorovém prostoru §$sp§ najděte bázi a dimenzi podprostoru §$mtex§. Kolik prvků má podprostor §M§ a proč? Pokud je to možné, najděte souřadnice vektoru §\\mathbf{x}=${vtex(x)}§ vzhledem k vámi nalezené bázi. Pokud to možné není, napište, co to pro daný vektor znamená. Vymyslete ještě nějakou jinou bázi §M§ a vysvětlete, proč je to taky báze."
            else "Ve vektorovém prostoru §$sp§ najděte bázi a dimenzi lineárního obalu §$mtex§. Spočítejte souřadnice vektoru §\\mathbf{x}=${vtex(x)}§ vzhledem k vámi nalezené bázi. Vymyslete ještě nějakou jinou bázi §M§ a vysvětlete, proč je to taky báze.")
        val names = all.indices.map { m("§\\mathbf{v}_${it + 1}=${all[it].tex()}§") }
        @Suppress("UNCHECKED_CAST") fun chosen(prev: List<Any>) = (prev.getOrNull(1) as? Set<Int>)?.sorted()?.takeIf { it.size == d && f.rank(it.map { i -> all[i] }) == d } ?: f.pickBasis(all)
        // another basis: a vector of the basis times a number other than 0 and 1 (2, or -1 over R)
        val k = if (p == null && r.nextBoolean()) Frac.of(-1) else Frac.of(2)
        fun other(b: List<Vec>) = listOf(f.scale(k, b[0])) + b.drop(1)
        val parts = ArrayList<(List<Any>) -> Part>()
        parts += { _ -> Part(m("Aká je dimenzia §M§?"), Answer.Numbers(listOf("\\dim M"), null, listOf(Frac.of(d)), m("§\\dim M=$d§"), diagnose = dimDiagnosis(all.size, 4, d)),
            hint = m("Vektory do stĺpcov, GEM. Dimenzia je počet vektorov bázy, teda počet hlavných stĺpcov."), aim = 1) }
        parts += { _ ->
            Part(m("Vyber bázu §M§ z daných vektorov."), Answer.Pick(names, 1..4, { s -> s.size == d && f.rank(s.map { all[it] }) == d }) { s ->
                m(if (s.size != d) "Báza §M§ má $d prvky, toľko, aká je dimenzia. Tak hovorí Dôsledok 2.58." else "Vybrané vektory sú LZ, negenerujú celé §M§.")
            }, hint = m("Vektory do stĺpcov, GEM; vektory pri hlavných stĺpcoch tvoria bázu."), aim = 0)
        }
        if (p != null) parts += { _ -> Part(m("Koľko prvkov má §M§?"), Answer.Numbers(listOf("|M|"), null, listOf(Frac.of(pow(p, d))), m("§${pw(p, d)}=${pow(p, d)}§"), diagnose = sizeDiagnosis(p, 4, d)), m("Každý prvok §M§ zodpovedá jedinej $d-tici súradníc z §\\mathbb{Z}_{$p}§, preto ich je §${pw(p, d)}§. Súradnice sú jednoznačné podľa Vety 2.66, počet dáva Poznámka 2.67."),
            hint = m("Každý prvok §M§ má v báze jedinú §d§-ticu súradníc zo §\\mathbb{Z}_p§; prvkov je toľko, koľko je takých §d§-tíc."), aim = 1) }
        // Over Z_p the test asks "if it is possible": the decision comes first, the same question whether x is in M or
        // not, and only then (when it is) the coordinates; the question never tells the branch (Teo, 17. 11.).
        // Over R the test asks the coordinates outright (x is in M there), so they come at once.
        if (p != null) parts += { _ ->
            Part(m("Má §\\mathbf{x}§ súradnice v tvojej báze?"), Answer.Choice(listOf(m("Áno, §\\mathbf{x}\\in M§"), m("Nie, §\\mathbf{x}\\notin M§")), if (inside) 0 else 1,
                if (inside) m("Sústava so stĺpcami z bázy a pravou stranou §\\mathbf{x}§ má riešenie, teda §\\mathbf{x}§ je kombináciou bázy a leží v §M§. Jej koeficienty sú súradnice (Definícia 2.68).")
                else m("Vektor §\\mathbf{x}§ neleží v §M§, preto súradnice vzhľadom na bázu §M§ nemá. Sústava so stĺpcami z bázy nemá riešenie: §\\mathbf{x}§ nie je kombináciou bázy."),
                wrong = mapOf(if (inside) 1 to "Sústava so stĺpcami z bázy a pravou stranou §\\mathbf{x}§ riešenie má: po GEM je pravá strana vedľajší stĺpec. Preto §\\mathbf{x}\\in M§ a súradnice má."
                    else 0 to "Súradnice vzhľadom na bázu §M§ má len vektor z §M§, a §\\mathbf{x}§ v ňom nie je. Súradnice (Definícia 2.68) sú koeficienty kombinácie bázy, a tá tu neexistuje: po GEM so stĺpcami z bázy a pravou stranou §\\mathbf{x}§ je pravá strana hlavný stĺpec.")),
                hint = m("Rieš sústavu so stĺpcami z bázy a pravou stranou §\\mathbf{x}§. Má riešenie práve vtedy, keď §\\mathbf{x}\\in M§."), aim = 2)
        }
        if (inside) parts += { prev ->
            val b = chosen(prev).map { all[it] }
            val c = f.coords(b, x)!!
            Part(m(if (p != null) "Aké sú súradnice §\\mathbf{x}§ v tvojej báze?" else "Súradnice §\\mathbf{x}§ v tvojej báze:"), Answer.Numbers(List(d) { "\\alpha_${it + 1}" }, p, c, m("§${vtex(c)}§"), fractions = p == null, diagnose = coordsDiagnosis(f, b, x, c)),
                hint = m("Rieš sústavu so stĺpcami z tvojej bázy a pravou stranou §\\mathbf{x}§."), aim = 2)
        }
        parts += { prev ->
            val b = chosen(prev).map { all[it] }
            Part(m("Vymysli ešte nejakú inú bázu §M§: $d vektory."), Answer.Vectors(List(4) { "x_{${it + 1}}" }, p, d..d, entry = "vektor",
                check = { e -> judgeBasis(f, all, e) { vs -> if (vs.toSet() == b.toSet()) m("Sú to tie isté vektory ako v báze, ktorú si vybral, len prípadne v inom poradí. Formálne je aj prehodené poradie iná báza (súbor je usporiadaný), ale test to tak nemyslí: 14. 11. 2024 výslovne chcel vektor, ktorý v pôvodnom súbore nie je. Vymeň aspoň jeden, napríklad ho vynásob číslom rôznym od §0§ aj §1§ alebo k nemu pripočítaj iný.") else null } },
                example = other(b).map { listOf(it) }),
                hint = m("Vynásob jeden vektor bázy číslom rôznym od §0§ aj §1§; §d§ LN vektorov v §M§ je báza."), aim = 3)
        }
        return Exercise(
            "span-basis", "Báza a dimenzia obalu", if (p != null) "Šablóna z testu 14. 11. 2024 (variant Medvěd) a 22. 11. 2023 (variant Myš), príklad 2" else "Šablóna z testu 24. 11. 2023 (variant Včelka), príklad 2; MARAST #13493", text, mtex,
            parts = parts, postup = SpanBasisPostup,
            solution = { prev ->
                val b = chosen(prev).map { all[it] }
                val (basis, size, coords, another) = SpanBasisPostup
                SpanBasisPostup.solve {
                    basis("Vektory dám do stĺpcov a upravím GEM. " + Reasons.gemColumns, gemLines(f.echelon(Mat.columns(all))))
                    val piv = f.pickBasis(all)
                    basis(Reasons.mainColumns(piv) + " Báza je napríklad §${vstex(piv.map { all[it] })}§. " + Reasons.dim("\\dim M=$d"))
                    if (p != null) size("Každý prvok §M§ má v báze §$d§-ticu súradníc zo §\\mathbb{Z}_{$p}§. " + Reasons.coords + " Naopak každá §$d§-tica dá prvok §M§, prvkov je preto toľko ako §$d§-tíc.", "|M|=${pw(p, d)}=${pow(p, d)}")
                    else size("Nad §\\mathbb{R}§ má podpriestor dimenzie §$d§ nekonečne veľa prvkov.", "\\dim M=$d")
                    val c = f.coords(b, x)
                    val e = f.echelon(Mat.columns(b, listOf(x)), d)
                    coords("Súradnice §\\mathbf{x}§ v báze §\\mathcal{B}=${vstex(b)}§ sú riešenie sústavy so stĺpcami z bázy a pravou stranou §\\mathbf{x}§.", gemLines(e))
                    if (c == null) coords("Posledný stĺpec je hlavný, sústava nemá riešenie. Teda §\\mathbf{x}\\notin M§ a súradnice vzhľadom na bázu §M§ nemá.")
                    else coords("Posledný stĺpec je vedľajší a ostatné hlavné, riešenie je jediné. Dosadzujem zdola:", backSubst(f, e, (1..d).map { "\\alpha_$it" }).first + Formula("(\\mathbf{x})_{\\mathcal{B}}=${vtex(c)}"))
                    val o = other(b)
                    another("Prvý vektor bázy vynásobím číslom §${k.tex(true)}§: násobok prvku §M§ je v §M§, takže dostanem §$d§ vektory z §M§.", "${vstex(b)}\\ \\to\\ ${vstex(o)}")
                    another("Sú LN: z §\\alpha(${Terms.times(k, "\\mathbf{b}_1")})+\\dots=\\theta§ a LN bázy plynie §${Terms.times(k, "\\alpha")}=0§, teda §\\alpha=0§, a ostatné koeficienty sú nulové. Je ich §$d§, toľko ako §\\dim M§. " + Reasons.lnOfDim)
                }
            },
            hint = m("Vektory do stĺpcov, GEM. Hlavné stĺpce povedia, ktoré vektory tvoria bázu, ich počet je dimenzia. Nad §\\mathbb{Z}_p§ je prvkov §p^d§, nad §\\mathbb{R}§ nekonečne veľa. Inú bázu dostaneš napríklad vynásobením vektora bázy číslom rôznym od §0§ aj §1§."),
        )
    }
}

val SpanBasisPostup = postup(
    "Vyber bázu" to "Vektory do stĺpcov, GEM; vektory pri hlavných stĺpcoch tvoria bázu.",
    "Dimenzia a počet prvkov" to "Dimenzia je počet vektorov bázy §d§; v §\\mathbb{Z}_p§ má podpriestor §p^d§ prvkov, v §\\mathbb{R}§ pri §d>0§ nekonečne veľa.",
    "Súradnice §\\mathbf{x}§, ak sa dá" to "Rieš sústavu so stĺpcami z bázy a pravou stranou §\\mathbf{x}§; bez riešenia §\\mathbf{x}§ v §M§ nie je.",
    "Iná báza" to "Vynásob jeden vektor bázy číslom rôznym od §0§ aj §1§; §d§ LN vektorov v §M§ je báza.",
)

/* ---- 4. Coordinates both ways in R^3 or Z_p^3 -------------------------------------------------------------- */

fun coordsExercise(r: Random): Exercise {
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(5)
    while (true) {
        val b = List(3) { f.vec(List(3) { r.pick(-1, 3).let { if (f is Field.Zp) JMath.floorMod(it, 5) else it } }) }
        if (!f.isLN(b)) continue
        if (b.any { v -> v.count { it.isZero } > 1 }) continue
        val c = f.vec(List(3) { r.pick(-2, 3) })
        val x = f.fromCoords(b, c)
        if (f is Field.R && x.any { kotlin.math.abs(it.num) > 12 }) continue
        val back = f.vec(List(3) { r.pick(-2, 3) })
        val w = f.fromCoords(b, back)
        val sp = fieldPow(f, 3)
        val text = "Uvažujme bázi $\\mathcal{B}=${vstex(b)}$ prostoru $$sp$. Který vektor má souřadnice $${vtex(c)}$ vzhledem k bázi $\\mathcal{B}$? Jaké souřadnice má vektor $${vtex(w)}$ vzhledem k bázi $\\mathcal{B}$?"
        val pp = (f as? Field.Zp)?.p
        return Exercise(
            "coords", "Súradnice oboma smermi", "Šablóna z testu 24. 11. 2023 (variant Slepice), príklad 2; Otázky k Definícii 2.68", text,
            parts = listOf(
                { _ -> Part("Ktorý vektor má súradnice $${vtex(c)}$?", Answer.Numbers(listOf("x_1", "x_2", "x_3"), pp, x, "$${vtex(x)}$", fractions = pp == null, diagnose = vectorFromCoordsDiagnosis(f, b, c, x)),
                    hint = m("Súradnice sú koeficienty: vynásob nimi vektory bázy a sčítaj."), aim = 0) },
                { _ -> Part("Súradnice $${vtex(w)}$:", Answer.Numbers(listOf("\\alpha_1", "\\alpha_2", "\\alpha_3"), pp, back, "$${vtex(back)}$", fractions = pp == null, diagnose = coordsDiagnosis(f, b, w, back)),
                    hint = m("Rieš sústavu: stĺpce sú vektory bázy, pravá strana daný vektor."), aim = 1) },
            ),
            postup = CoordsPostup,
            solution = { _ ->
                val (toVec, toCoords, check) = CoordsPostup
                val names = (1..3).map { "\\mathbf{b}_$it" }
                CoordsPostup.solve {
                    toVec("Súradnice sú koeficienty kombinácie bázy (Definícia 2.68), preto nimi vynásobím vektory bázy a sčítam.",
                        "\\mathbf{x}=${Terms.lin(c, names)}=${Terms.lin(c, b.map { vtex(it) })}=${vtex(x)}")
                    toCoords("Opačne hľadám koeficienty §\\alpha_1,\\alpha_2,\\alpha_3§ s §\\alpha_1\\mathbf{b}_1+\\alpha_2\\mathbf{b}_2+\\alpha_3\\mathbf{b}_3=${vtex(w)}§. To je sústava so stĺpcami z bázy.", gemLines(f.echelon(Mat.columns(b, listOf(w)))))
                    toCoords("Všetky tri stĺpce bázy sú hlavné, teda sústava nemá voľné neznáme a riešenie je jediné. Dosadzujem zdola: z posledného riadku jednu neznámu, potom ďalšie.",
                        backSubst(f, f.echelon(Mat.columns(b, listOf(w))), listOf("\\alpha_1", "\\alpha_2", "\\alpha_3")).first + Formula("(${vtex(w)})_{\\mathcal{B}}=${vtex(back)}"))
                    check("Skúška: kombinácia bázy s nájdenými súradnicami musí dať pôvodný vektor.", "${Terms.lin(back, names)}=${vtex(f.fromCoords(b, back))}")
                    check("Pozor na smer: §(\\mathbf{x})_{\\mathcal{B}}§ je vo všeobecnosti iná §n§-tica než §\\mathbf{x}§. Pre každý vektor sa rovnajú len v štandardnej báze; pri zámene sa dá počítať „úspešne“ a predsa zle (Discord #la1, 10. 12. 2024).")
                }
            },
            hint = "Súradnice → vektor: násob a sčítaj. Vektor → súradnice: rieš sústavu, stĺpce sú vektory bázy.",
        )
    }
}

val CoordsPostup = postup(
    "Zo súradníc vektor" to "Súradnice sú koeficienty: vynásob nimi vektory bázy a sčítaj.",
    "Z vektora súradnice" to "Rieš sústavu: stĺpce sú vektory bázy, pravá strana daný vektor.",
    "Skúška" to "Dosaď nájdené súradnice späť; musí vyjsť pôvodný vektor.",
)

/* ---- 5. Counting elements in Z_p^n ------------------------------------------------------------------------ */

fun countExercise(r: Random, bases: Boolean = true): Exercise {
    val p = listOf(2, 3, 5, 7).random(r)
    val n = r.pick(3, 5)
    val d = r.pick(1, n - 1)
    fun pw(e: Int) = Terms.pow(p.toLong(), e)
    return when (r.nextInt(if (bases) 3 else 2)) {
        0 -> Exercise(
            "count", "Počet prvkov", "Šablóna z testu 24. 11. 2023 (variant Včelka), príklad 3",
            "Uvažujme \$P\\ssub\\mathbb{Z}_{$p}^{$n}$, pro který platí $\\dim P=$d$. Kolik prvků \$P$ má?",
            parts = listOf({ _ -> Part("Počet prvkov \$P$:", Answer.Numbers(listOf("|P|"), null, listOf(Frac.of(pow(p, d))), "$" + listOf(pw(d), "${pow(p, d)}").distinct().joinToString("=") + "$", diagnose = sizeDiagnosis(p, n, d))) }),
            postup = CountElementsPostup,
            solution = {
                val (match, count) = CountElementsPostup
                CountElementsPostup.solve {
                    match("Podľa definície dimenzie je v §P§ LN súbor " + (if (d == 1) "s jedným vektorom" else "§$d§ vektorov") + ". Je to báza §P§, lebo vektor mimo jeho obalu by dal dlhší LN súbor (Veta 2.56). " + Reasons.coords,
                        "\\mathbf{v}\\in P\\quad\\to\\quad " + (if (d == 1) "\\alpha_1\\in\\mathbb{Z}_{$p}" else if (d == 2) "(\\alpha_1,\\alpha_2)\\in\\mathbb{Z}_{$p}^{2}" else "(\\alpha_1,\\dots,\\alpha_$d)\\in\\mathbb{Z}_{$p}^{$d}"))
                    match("Naopak každá §$d§-tica súradníc dá vektor z §P§, takže prvky §P§ a §$d§-tice si zodpovedajú jedna k jednej.")
                    count((if (d == 1) "Jedinú súradnicu vyberám" else "Každú z §$d§ súradníc vyberám") + " z §$p§ prvkov §\\mathbb{Z}_{$p}§, teda (Poznámka 2.67):", "|P|=" + listOf(pw(d), "${pow(p, d)}").distinct().joinToString("="))
                }
            },
            hint = "Každý prvok P zodpovedá jednej d-tici súradníc.",
        )
        1 -> {
            val m = r.pick(1, n - 1)
            // one vector is (x₁), two (x₁,x₂); "x₁,…,xₘ" only from three on, never "x₁,…,x₁"
            val xs = (1..m).let { if (m <= 2) it.joinToString(",") { i -> "\\mathbf{x}_$i" } else "\\mathbf{x}_1,\\dots,\\mathbf{x}_$m" }
            val cnt = pow(p, m) - 1
            Exercise(
                "count", "Koľko vektorov urobí súbor LZ", "Šablóna z testu 21. 11. 2025 (variant Sova), príklad 2",
                "Soubor $($xs)$ vektorů z $\\mathbb{Z}_{$p}^{$n}$ je LN. Kolik existuje nenulových vektorů $\\mathbf{y}$, pro které je $($xs,\\mathbf{y})$ LZ?",
                parts = listOf({ _ -> Part("Počet:", Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(cnt)), "$${pw(m)}-1=$cnt$", diagnose = lzCountDiagnosis(p, n, m))) }),
                postup = CountLzPostup,
                solution = {
                    val (whenLz, size, nonzero) = CountLzPostup
                    CountLzPostup.solve {
                        whenLz("Ak §\\mathbf{y}\\notin\\langle$xs\\rangle§, súbor ostane LN, lebo v kombinácii rovnej §\\theta§ musí mať §\\mathbf{y}§ nulový koeficient (Veta 2.42). Ak §\\mathbf{y}§ v obale je, je kombináciou ostatných, preto je súbor LZ (Veta 2.39).")
                        size("LZ teda robia práve prvky obalu. Jeho bázou je §($xs)§, má dimenziu §$m§ a toľko prvkov:", "|\\langle$xs\\rangle|=" + listOf(pw(m), "${pow(p, m)}").distinct().joinToString("="))
                        nonzero("Pýtajú sa na nenulové §\\mathbf{y}§, nulový vektor preto odčítam.", "${pow(p, m)}-1=$cnt")
                    }
                },
                hint = "Kedy pridanie y pokazí LN? Práve keď y je v obale.",
            )
        }
        else -> {
            val bases = (0 until d).fold(1L) { acc, i -> acc * (pow(p, d) - pow(p, i)) }
            Exercise(
                "count", "Počet báz", "Otázka v skriptách pri Vete 2.60; test 15. 11. 2022 (variant A), príklad 3",
                "Mějme \$P\\ssub\\mathbb{Z}_{$p}^{$n}$, který má dimenzi $$d$. Kolik jeho různých bází existuje?",
                parts = listOf({ _ -> Part("Počet báz (usporiadaných súborov):", Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(bases)), "$$bases$", diagnose = basesDiagnosis(p, n, d))) }),
                postup = CountBasesPostup,
                solution = {
                    val (oneByOne, choices, product) = CountBasesPostup
                    CountBasesPostup.solve {
                        oneByOne("Bázu skladám po jednom vektore, kým nemám LN súbor §$d§ vektorov z §P§. " + Reasons.lnOfDim)
                        choices("Prvý vektor môže byť ľubovoľný nenulový, to je §${pw(d)}-1§ možností.")
                        if (d > 1) choices("Keď už mám §k§ vektorov, ďalší musí byť mimo ich obalu, inak by bol súbor LZ. Mimo obalu súbor ostane LN, lebo nový vektor musí mať v kombinácii rovnej §\\theta§ nulový koeficient (Veta 2.42). Obal má §${p}^k§ prvkov, ostáva §${pw(d)}-${p}^k§ možností.")
                        product("Počty možností v jednotlivých krokoch vynásobím.",
                            (0 until d).joinToString("\\cdot ") { "(${pw(d)}-${pw(it)})" } + "=" + (0 until d).joinToString("\\cdot ") { "${pow(p, d) - pow(p, it)}" }.let { if (d == 1) "" else "$it=" } + "$bases")
                    }
                },
                hint = "Vyberaj vektory po jednom: vždy mimo obalu tých, čo už máš.",
            )
        }
    }
}

val CountElementsPostup = postup(
    "Prvok ako súradnice" to "Každý prvok podpriestoru má v báze jediné súradnice, a naopak.",
    "Spočítaj súradnice" to "Každú z §d§ súradníc vyberáš z §p§ prvkov §\\mathbb{Z}_p§, spolu §p^d§.",
)

val CountLzPostup = postup(
    "Kedy je súbor LZ?" to "Pridaný vektor robí súbor LZ práve vtedy, keď leží v obale ostatných.",
    "Koľko prvkov má obal?" to "Obal §m§ LN vektorov má dimenziu §m§, teda §p^m§ prvkov.",
    "Len nenulové" to "Ak sa pýtajú na nenulové vektory, nulový odčítaj.",
)

val CountBasesPostup = postup(
    "Bázu skladaj po vektore" to "Báza je LN súbor; vektory vyberaj postupne.",
    "Koľko možností má každý krok?" to "Ďalší vektor musí byť mimo obalu predchádzajúcich, a ten má §p^k§ prvkov.",
    "Vynásob" to "Počty možností v krokoch vynásob.",
)
/* ---- 6. LN or LZ by GEM, membership in a span, simple drills ------------------------------------------------ */

fun lnExercise(r: Random): Exercise {
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(5)
    while (true) {
        val dep = r.nextBoolean()
        val n = 3
        val a = f.vec(List(n) { r.pick(-2, 3) }); val b = f.vec(List(n) { r.pick(-2, 3) })
        if (!f.isLN(listOf(a, b))) continue
        val c = if (dep) f.combo(listOf(r.nonzero(-2, 2).q(), r.nonzero(-2, 2).q()), listOf(a, b)) else f.vec(List(n) { r.pick(-2, 3) })
        val vs = listOf(a, b, c)
        if (f.isLN(vs) == dep || c.isZero()) continue
        if (f is Field.R && c.any { kotlin.math.abs(it.num) > 9 }) continue
        val ln = f.isLN(vs)
        val sp = fieldPow(f, 3)
        val e = f.echelon(Mat.columns(vs))
        return Exercise(
            "ln", "LN alebo LZ", "Postup 2.27, Príklady 2.28 a 2.29",
            "Je soubor $${vstex(vs)}$ v $$sp$ lineárně nezávislý?",
            parts = listOf({ _ -> Part("Súbor je", Answer.Choice(listOf("LN", "LZ"), if (ln) 0 else 1, if (ln) "Homogénna sústava má len triviálne riešenie." else "Existuje netriviálna kombinácia rovná $\\theta$.",
                wrong = if (ln) mapOf(1 to "LZ by chcela netriviálne riešenie homogénnej sústavy. Po GEM sú však všetky tri stĺpce hlavné, voľná neznáma nie je a jediné riešenie je §(0,0,0)§.")
                else mapOf(0 to "Súbor je LZ: po GEM ostane vedľajší stĺpec, teda voľná neznáma. Homogénna sústava má preto aj netriviálne riešenie, napríklad §(\\alpha,\\beta,\\gamma)=${vtex(f.dependency(vs)!!)}§.")
            )) }),
            postup = LnPostup,
            solution = {
                val (system, gem, side, end) = LnPostup
                LnPostup.solve {
                    system("Z definície LN hľadám všetky §\\alpha,\\beta,\\gamma§ s §\\alpha\\mathbf{x}_1+\\beta\\mathbf{x}_2+\\gamma\\mathbf{x}_3=\\theta§. Vektory idú do stĺpcov, sústava je homogénna.")
                    gem("Úpravami GEM dostanem horný stupňovitý tvar; pravá strana ostáva nulová, preto ju nepíšem.", gemLines(e))
                    if (ln) {
                        side("Všetky tri stĺpce sú hlavné, žiadna neznáma nie je voľná. Jediné riešenie je §(0,0,0)§.")
                        end("Len triviálna kombinácia dá §\\theta§, súbor je LN.")
                    } else {
                        val nm = listOf("\\alpha", "\\beta", "\\gamma")
                        val freeCol = (0 until 3).first { it !in e.pivots }
                        val (lines, d) = backSubst(f, e, nm, mapOf(freeCol to Frac.ONE), homogeneous = true)
                        side("Ostal vedľajší stĺpec, jeho neznáma §${nm[freeCol]}§ je voľná. Zvolím §${nm[freeCol]}=1§ a ostatné dosadím zdola:", lines + Formula("(\\alpha,\\beta,\\gamma)=${vtex(d)}"))
                        end("Netriviálna kombinácia rovná §\\theta§ existuje, súbor je LZ.", Terms.lin(d, listOf("\\mathbf{x}_1", "\\mathbf{x}_2", "\\mathbf{x}_3")) + "=\\theta")
                    }
                }
            },
            hint = "Vektory do stĺpcov, homogénna sústava. Je nejaký stĺpec vedľajší?",
        )
    }
}

val LnPostup = postup(
    "Zostav sústavu" to "Vektory zapíš do stĺpcov homogénnej sústavy: hľadáš koeficienty kombinácie rovnej §\\theta§.",
    "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar.",
    "Je niektorý stĺpec vedľajší?" to "Vedľajší stĺpec znamená voľnú neznámu, a tým aj netriviálne riešenie.",
    "Záver" to "Len triviálne riešenie: LN. Aj netriviálne: LZ, a jedno ukáž.",
)

fun memberExercise(r: Random): Exercise {
    val f = Field.R
    while (true) {
        val u = f.vec(List(3) { r.pick(-2, 3) }); val v = f.vec(List(3) { r.pick(-2, 3) })
        if (!f.isLN(listOf(u, v))) continue
        val inside = r.nextBoolean()
        val k1 = r.nonzero(-2, 3); val k2 = r.nonzero(-2, 2)
        var x = f.combo(listOf(k1.q(), k2.q()), listOf(u, v))
        if (!inside) x = x.toMutableList().also { it[r.pick(0, 2)] = it[r.pick(0, 2)] + r.nonzero(-2, 2).q() }
        if (f.inSpan(listOf(u, v), x) != inside || x.any { kotlin.math.abs(it.num) > 10 }) continue
        val sol = f.solve(listOf(u, v), x)
        return Exercise(
            "member", "Patrí do obalu?", "Postup 2.45, test 14. 11. 2024 (variant Medvěd), príklad 1",
            "Patří vektor $\\mathbf{x}=${vtex(x)}$ do $\\langle${u.tex()},${v.tex()}\\rangle$ v $\\mathbb{R}^3$?",
            parts = listOf({ _ -> Part("Odpoveď:", Answer.Choice(listOf("Áno", "Nie"), if (inside) 0 else 1, if (inside) "Sústava má riešenie." else "Sústava nemá riešenie.",
                wrong = if (inside) mapOf(1 to "Vektor do obalu patrí: pravá strana po GEM nie je hlavný stĺpec. Sústava má riešenie §\\alpha=${sol!![0].tex(true)}§, §\\beta=${sol[1].tex(true)}§, teda §\\alpha\\mathbf{u}+\\beta\\mathbf{v}=\\mathbf{x}§. Nulový riadok sám nič neznamená.")
                else mapOf(0 to "Vektor §\\mathbf{x}§ do obalu nepatrí: po GEM vyjde riadok §0=c§ s §c\\neq 0§. Pravá strana je teda hlavný stĺpec, sústava nemá riešenie a §\\mathbf{x}§ nie je kombináciou §\\mathbf{u},\\mathbf{v}§.")
            )) }),
            postup = MemberPostup,
            solution = {
                val (system, gem, side, end) = MemberPostup
                MemberPostup.solve {
                    system("Vektor patrí do obalu, keď je kombináciou §\\mathbf{u},\\mathbf{v}§: hľadám §\\alpha,\\beta\\in\\mathbb{R}§ s §\\alpha\\mathbf{u}+\\beta\\mathbf{v}=\\mathbf{x}§. Stĺpce sú §\\mathbf{u},\\mathbf{v}§, pravá strana §\\mathbf{x}§.")
                    gem("Úpravami GEM dostanem horný stupňovitý tvar rozšírenej matice.", gemLines(f.echelon(Mat.columns(listOf(u, v), listOf(x)), 2)))
                    if (sol != null) {
                        side("Posledný stĺpec nie je hlavný, sústava má riešenie. Dosadzujem zdola:", backSubst(f, f.echelon(Mat.columns(listOf(u, v), listOf(x))), listOf("\\alpha", "\\beta")).first)
                        end("Teda §\\mathbf{x}\\in\\langle\\mathbf{u},\\mathbf{v}\\rangle§, lebo §\\mathbf{x}§ je ich kombinácia.", Terms.lin(sol, listOf("\\mathbf{u}", "\\mathbf{v}")) + "=\\mathbf{x}")
                    } else {
                        side("Posledný stĺpec je hlavný: vyšiel riadok §0=c§ s §c\\neq 0§, sústava nemá riešenie.")
                        end("Žiadna kombinácia §\\mathbf{u},\\mathbf{v}§ nedá §\\mathbf{x}§, teda §\\mathbf{x}\\notin\\langle\\mathbf{u},\\mathbf{v}\\rangle§.")
                    }
                }
            },
            hint = "x je v obale, keď je kombináciou. Sústava s pravou stranou x.",
        )
    }
}

val MemberPostup = postup(
    "Zostav sústavu" to "Vektory obalu do stĺpcov, daný vektor ako pravá strana: hľadáš koeficienty kombinácie.",
    "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar rozšírenej matice.",
    "Je pravá strana hlavný stĺpec?" to "Hlavná pravá strana (riadok §0=c§, §c\\neq 0§) znamená, že sústava nemá riešenie.",
    "Záver" to "Má riešenie: vektor v obale je, ukáž kombináciu. Nemá: nie je.",
)

/** A linear combination of [names] with coefficients [k], through the shared formatter. */
fun comboTexF(k: Vec, names: List<String>): String = Terms.lin(k, names)

/** Lesson 1 and 3 drills: arithmetic in T^n. */
fun arithExercise(r: Random, zp: Boolean = r.nextBoolean()): Exercise {
    val f: Field = if (zp) Field.Zp(listOf(3, 5, 7).random(r)) else Field.R
    val n = r.pick(2, 4)
    val x = f.vec(List(n) { r.pick(-3, 6) }); val y = f.vec(List(n) { r.pick(-3, 6) })
    // both coefficients nonzero in the field itself: ±3 is 0 in Z_3, and "Spočítaj 0 v Z_3^3" or one vector alone is no
    // exercise (web walk, 19. 11.)
    fun coef(lo: Int, hi: Int): Frac { while (true) { val k = r.nonzero(lo, hi).q(); if (!f.norm(k).isZero) return k } }
    val a = coef(-3, 4); val b = coef(-2, 3)
    val na = f.norm(a); val nb = f.norm(b)
    val res = f.plus(f.scale(na, x), f.scale(nb, y))
    val pp = (f as? Field.Zp)?.p
    val at = if (pp != null) "v $\\mathbb{Z}_{$pp}^{$n}$" else "v $\\mathbb{R}^{$n}$"
    val expr = Terms.lin(listOf(na, nb), listOf(vtex(x), vtex(y)))
    return Exercise(
        "arith", "Počítanie po zložkách", "Definícia 2.2 (vektorový priestor \$T^n$)",
        "Spočítaj $$expr$ $at.",
        parts = listOf({ _ -> Part("Výsledok:", Answer.Numbers(List(n) { "x_${it + 1}" }, pp, res, "$${vtex(res)}$", diagnose = arithDiagnosis(f, na, x, nb, y, res))) }),
        postup = if (pp != null) ArithZpPostup else ArithPostup,
        solution = { (if (pp != null) ArithZpPostup else ArithPostup).solve { arithSteps(f, na, x, nb, y) } },
        hint = if (pp != null) "Každú zložku zvlášť, potom zvyšok po delení $pp. Záporné číslo: pripočítavaj $pp." else "Každú zložku zvlášť.",
    )
}

/** The solution of `a·x + b·y` in [f] by components: the generator and the lessons' worked examples (Definícia 2.2). */
fun SolutionScope.arithSteps(f: Field, na: Frac, x: Vec, nb: Frac, y: Vec) {
    val pp = (f as? Field.Zp)?.p
    val (scale, add) = if (pp != null) ArithZpPostup else ArithPostup
    // raw integers first, then modulo p: the student sees both
    fun raw(k: Frac, v: Vec) = v.map { it * k }
    fun chain(vararg parts: String) = parts.toList().distinct().joinToString("=")
    val scaled = listOf(na to x, nb to y).filter { it.first != Frac.ONE }
    if (scaled.isEmpty()) scale("Oba koeficienty sú §1§, vektory sa nemenia.")
    else scale("Násobenie skalárom ide po zložkách (Definícia 2.2): každú zložku vynásobím koeficientom." + (if (pp != null) " Výsledok beriem modulo §$pp§." else ""),
        scaled.map { (k, v) -> Formula(chain(Terms.times(k, vtex(v)), vtex(raw(k, v)), vtex(f.scale(k, v)))) })
    val sx = f.scale(na, x); val sy = f.scale(nb, y)
    val sum = sx.indices.map { sx[it] + sy[it] }
    add("Sčítanie ide tiež po zložkách: sčítam zložky na rovnakých miestach." + (if (pp != null) " Každú zložku nakoniec vezmem modulo §$pp§." else ""),
        chain("${vtex(sx)}+${vtex(sy)}", vtex(sum), vtex(f.plus(sx, sy))))
}

val ArithPostup = postup(
    "Vynásob skalármi" to "Každú zložku vektora vynásob jeho koeficientom.",
    "Sčítaj po zložkách" to "Sčítaj zložky na rovnakých miestach.",
)

val ArithZpPostup = postup(
    "Vynásob skalármi" to "Každú zložku vektora vynásob jeho koeficientom a vezmi modulo §p§.",
    "Sčítaj po zložkách" to "Sčítaj zložky na rovnakých miestach a výsledok vezmi modulo §p§.",
)

fun dimExercise(r: Random): Exercise {
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(5)
    while (true) {
        val m = r.pick(2, 4)
        val vs = List(m) { f.vec(List(3) { r.pick(-1, 3) }) }
        if (vs.any { it.isZero() }) continue
        val d = f.rank(vs)
        if (d == m && r.nextInt(3) > 0) continue
        val sp = fieldPow(f, 3)
        val e = f.echelon(Mat.columns(vs))
        return Exercise(
            "dim", "Dimenzia obalu", "Definícia 2.49, Veta 2.59",
            "Aká je dimenzia $\\langle " + vs.joinToString(",") { it.tex() } + "\\rangle\\ssub$sp$?",
            parts = listOf({ _ -> Part("Dimenzia:", Answer.Numbers(listOf("\\dim"), null, listOf(Frac.of(d)), "$$d$", diagnose = dimDiagnosis(m, 3, d))) }),
            postup = DimPostup,
            solution = {
                val (gem, main, dim) = DimPostup
                DimPostup.solve {
                    gem("Vektory zapíšem do stĺpcov a úpravami GEM dostanem HST. " + Reasons.gemColumns, gemLines(e))
                    main(Reasons.mainColumns(e.pivots))
                    dim("Báza z hlavných stĺpcov má §$d§ " + (if (d == 1) "vektor" else if (d in 2..4) "vektory" else "vektorov") + ". " + Reasons.dim("\\dim=$d"))
                }
            },
            hint = "Koľko z nich je naozaj nových smerov? GEM a počet hlavných stĺpcov.",
        )
    }
}

val DimPostup = postup(
    "Uprav GEM" to "Vektory zapíš do stĺpcov a úpravami GEM dostaň horný stupňovitý tvar.",
    "Hlavné stĺpce" to "Vektory pri hlavných stĺpcoch tvoria bázu obalu.",
    "Dimenzia" to "Dimenzia je počet vektorov bázy, teda počet hlavných stĺpcov.",
)

/** All generators by name, as lessons and the practice tab refer to them. */
val La1Generators: Map<String, (Random) -> Exercise> = mapOf(
    "param" to { r -> paramExercise(r) },
    "param-ln" to { r -> paramExercise(r, ParamKind.LN) },
    "param-basis" to { r -> paramExercise(r, ParamKind.BASIS) },
    "param-gen" to { r -> paramExercise(r, ParamKind.GENERATES) },
    "param-span" to { r -> paramExercise(r, ParamKind.SPAN) },
    "complete" to ::completeExercise,
    "span-basis" to ::spanBasisExercise,
    "coords" to ::coordsExercise,
    "count" to { r -> countExercise(r, bases = false) },
    "count-all" to { r -> countExercise(r, bases = true) },
    "ln" to ::lnExercise,
    "member" to ::memberExercise,
    "arith" to { r -> arithExercise(r) },
    "arith-zp" to { r -> arithExercise(r, true) },
    "dim" to ::dimExercise,
    "subspace" to ::subspaceExercise,
    "baza-overit" to ::basisDecideExercise,
    "obal-geom" to ::spanGeomExercise,
    "standardna-baza" to ::standardBasisExercise,
) + FiniteSpaceGenerators + ConstructGenerators

/** What the practice tab lists, with the test it comes from. */
data class TemplateInfo(val gen: String, val title: String, val sub: String)

val La1Templates = listOf(
    TemplateInfo("param", "Parameter \$a$", "LN, báza, generuje, patrí do obalu; príklad 1, 7 b"),
    TemplateInfo("complete", "Doplnenie na bázu", "$\\mathbb{Z}_p^4$ aj $\\mathbb{R}^4$, súradnice, zmysluplná rovnosť; príklad 2, 12 b"),
    TemplateInfo("span-basis", "Báza a dimenzia obalu", "$\\mathbb{Z}_p^4$ aj $\\mathbb{R}^4$, súradnice, iná báza; príklad 2, 12 b"),
    TemplateInfo("baza-ina", "Iná báza toho istého \$M$", "Aj s daným vektorom alebo s vektorom mimo súboru"),
    TemplateInfo("baza-zo-suradnic", "Báza zo súradníc", "Poznáš súradnice dvoch vektorov, nájdi $\\mathbf{x},\\mathbf{y}$"),
    TemplateInfo("param-zp", "Parameter nad $\\mathbb{Z}_p$", "Pre ktoré $\\alpha\\in\\mathbb{Z}_p$ je to báza, generuje, je LN"),
    TemplateInfo("obal-vypis", "Všetky prvky obalu", "Vypíš ich sám, appka skontroluje úplnosť"),
    TemplateInfo("zp-zoznam", "Všetky podpriestory sám", "Netriviálne podpriestory $\\mathbb{Z}_p^2$, priamky a roviny $\\mathbb{Z}_2^3$"),
    TemplateInfo("suradnice-lk", "Súradnice kombinácie", "Bez poznania bázy"),
    TemplateInfo("lk-pocet", "Koľko LK dá vektor", "Počet riešení sústavy nad $\\mathbb{Z}_p$"),
    TemplateInfo("soubor-baze", "Súbor so zadanými bázami", "Štyri vektory, ktoré trojice sú bázy"),
    TemplateInfo("soustava-body", "Sústava so zadanými riešeniami", "Matica \$A$ a $\\mathbf{b}$, riadky LN"),
    TemplateInfo("coords", "Súradnice oboma smermi", "Zo súradníc vektor a naopak"),
    TemplateInfo("count-all", "Počty v $\\mathbb{Z}_p^n$", "Prvky podpriestoru, LZ vektory, bázy"),
    TemplateInfo("zp-pocet", "Koľko podpriestorov v $\\mathbb{Z}_p^n$", "Vektory, bázy a podpriestory: čo sa ráta a čo je to isté"),
    TemplateInfo("zp-vypis", "Všetky podpriestory, každý raz", "Netriviálne podpriestory $\\mathbb{Z}_3^2$, roviny $\\mathbb{Z}_2^3$"),
    TemplateInfo("ln", "LN alebo LZ cez GEM", "$\\mathbb{R}^3$ aj $\\mathbb{Z}_5^3$"),
    TemplateInfo("baza-overit", "Je súbor báza?", "LN aj generovanie jedným GEM, $\\mathbb{R}^3$ aj $\\mathbb{Z}_p^3$"),
    TemplateInfo("member", "Patrí do obalu?", "Sústava s pravou stranou"),
    TemplateInfo("obal-geom", "Ako vyzerá obal", "Bod, priamka, rovina alebo celý priestor v $\\mathbb{R}^2$ a $\\mathbb{R}^3$"),
    TemplateInfo("standardna-baza", "Štandardná báza", "Ktorý súbor to je, súradnice v nej"),
    TemplateInfo("dim", "Dimenzia obalu", "Koľko smerov naozaj je"),
    TemplateInfo("subspace", "Je to podpriestor?", "Dôkaz z definície alebo protipríklad"),
)

/* ---- 7. Is it a subspace? ------------------------------------------------------------------------------------------ */

fun subspaceExercise(r: Random): Exercise {
    val a = r.nonzero(-3, 3); val b = r.nonzero(-3, 3); val cc = r.pick(-2, 3)
    val cs = listOf(a, b, cc)
    val (theta, sum, mult, end) = SubspacePostup
    fun lin(x: String, y: String, z: String) = Terms.lin(cs, listOf(x, y, z))
    /** A set, the answer, the short why, the solution along the method, what the wrong answer gets wrong. */
    class S(val set: String, val yes: Boolean, val why: String, val wrong: String, val solve: SolutionScope.() -> Unit)
    val d = r.nonzero(-4, 4)
    val ax = Terms.times(a, "x")
    fun chain(vararg p: String) = p.toList().distinct().joinToString("=")
    val sets = listOf(
        S("\\{(x,y,z)\\in\\mathbb{R}^3\\mid ${lin("x", "y", "z")}=0\\}", true, "Homogénna rovnica: obsahuje §\\theta§ a súčet aj násobok riešení sú riešenia.",
            "Je to podpriestor: rovnica je homogénna, takže rovina prechádza počiatkom a je uzavretá na obe operácie. Súčet aj násobok riešení rovnicu spĺňajú, takže platia všetky tri podmienky Definície 2.7.") {
            theta("Dosadím §\\theta=(0,0,0)§: každý člen ľavej strany je násobok nuly, rovnica §0=0§ platí.")
            sum("Nech §\\mathbf{u}=(u_1,u_2,u_3)§ a §\\mathbf{v}=(v_1,v_2,v_3)§ rovnicu spĺňajú. Ľavá strana pre ich súčet sa rozpadne na súčet ľavých strán, a ten je nula.",
                "${lin("(u_1+v_1)", "(u_2+v_2)", "(u_3+v_3)")}=(${lin("u_1", "u_2", "u_3")})+(${lin("v_1", "v_2", "v_3")})=0+0=0")
            mult("Pre §\\alpha\\mathbf{u}§ sa ľavá strana vynásobí číslom §\\alpha§, takže ostane nulová.",
                "${lin("\\alpha u_1", "\\alpha u_2", "\\alpha u_3")}=\\alpha(${lin("u_1", "u_2", "u_3")})=\\alpha\\cdot 0=0")
            end("Všetky tri podmienky Definície 2.7 platia, množina je podpriestor.")
        },
        S("\\{(x,y,z)\\in\\mathbb{R}^3\\mid ${lin("x", "y", "z")}=$d\\}", false, "§\\theta§ rovnicu nespĺňa (vyšlo by §0=$d§), a každý podpriestor obsahuje §\\theta§.",
            "Nie je: §\\theta§ rovnicu nespĺňa, lebo pravá strana je nenulová. Rovnica vyzerá ako pri podpriestore, ale podpriestor §\\theta§ obsahovať musí (Pozorovanie 2.8).") {
            theta("Dosadím §\\theta=(0,0,0)§: ľavá strana je §0§, pravá §$d§, rovnica neplatí.", "0\\neq $d")
            end(Reasons.theta + " Táto množina ho neobsahuje, preto nie je podpriestor a ďalšie podmienky netreba overovať.")
        },
        S("\\{(x,y)\\in\\mathbb{R}^2\\mid xy=0\\}", false, "§(1,0)§ a §(0,1)§ v nej sú, ich súčet §(1,1)§ nie (zjednotenie osí).",
            "Nie je uzavretá na sčítanie: §(1,0)+(0,1)=(1,1)§ v nej nie je. Obsahuje §\\theta§ a je uzavretá na násobenie, ale je to zjednotenie dvoch osí a zjednotenie podpriestorov podpriestorom byť nemusí (Veta 2.18).") {
            theta("Pre §\\theta=(0,0)§ je §xy=0\\cdot 0=0§, takže §\\theta§ v množine je.")
            sum("Množina sú dve osi, preto vezmem bod z každej: §(1,0)§ aj §(0,1)§ podmienku spĺňajú, ich súčet nie.", "(1,0)+(0,1)=(1,1),\\qquad 1\\cdot 1=1\\neq 0")
            end("Množina nie je uzavretá na sčítanie, preto nie je podpriestor. Je to zjednotenie dvoch osí a také zjednotenie podpriestorom nie je, lebo súčet bodov z rôznych osí neleží na žiadnej z nich (Veta 2.18).")
        },
        S("\\{(x,y)\\in\\mathbb{R}^2\\mid y=$ax\\}", true, "Priamka cez počiatok: §\\theta§ v nej je a súčet aj násobok bodov na nej ostanú.",
            "Priamka cez počiatok splní všetky tri podmienky Definície 2.7. Podpriestor nemusí byť celý priestor, stačí, že sa z neho operáciami nedá dostať von.") {
            theta("Dosadím §x=y=0§: obe strany rovnice §y=$ax§ sú nulové, takže §\\theta§ v množine je.")
            sum("Nech §(x_1,y_1)§ a §(x_2,y_2)§ v množine sú, teda §y_1=${Terms.times(a, "x_1")}§ a §y_2=${Terms.times(a, "x_2")}§. Pre súčet platí rovnica znova:",
                chain("y_1+y_2", Terms.sum(Terms.times(a, "x_1"), Terms.times(a, "x_2")), Terms.times(a, "x_1+x_2")))
            mult("Pre §\\alpha(x,y)=(\\alpha x,\\alpha y)§ vynásobím rovnicu §y=$ax§ číslom §\\alpha§:",
                chain("\\alpha y", Terms.times(a, "\\alpha x")))
            end("Všetky tri podmienky Definície 2.7 platia, priamka cez počiatok je podpriestor.")
        },
        S("\\{(x,y)\\in\\mathbb{R}^2\\mid y=x^2\\}", false, "§(1,1)§ v nej je, ale §(1,1)+(1,1)=(2,2)§ nie, lebo §2^2=4\\neq 2§.",
            "Parabola nie je uzavretá na sčítanie ani na násobenie: §(1,1)§ na nej leží, §(1,1)+(1,1)=(2,2)§ nie. Obsahuje síce §\\theta§, ale to nestačí.") {
            theta("Pre §\\theta=(0,0)§ platí §0=0^2§, takže §\\theta§ v množine je.")
            sum("Podmienka §y=x^2§ nie je lineárna, preto skúsim bod a jeho dvojnásobok. Bod §(1,1)§ na parabole leží, súčet so sebou samým už nie.", "(1,1)+(1,1)=(2,2),\\qquad 2^2=4\\neq 2")
            end("Množina nie je uzavretá na sčítanie, preto nie je podpriestor.")
        },
        S("\\{(x,y,z)\\in\\mathbb{R}^3\\mid x\\geq 0\\}", false, "§(1,0,0)§ v nej je, ale §(-1)\\cdot(1,0,0)§ nie: násobiť smieš aj záporným.",
            "Nie je uzavretá na násobenie záporným číslom: §(-1)\\cdot(1,0,0)§ v nej nie je. Na sčítanie a násobenie nezáporným číslom uzavretá je, ale Definícia 2.7 chce §\\alpha\\mathbf{x}\\in P§ pre **každé** §\\alpha\\in\\mathbb{R}§, aj záporné.") {
            theta("Prvá zložka §\\theta§ je §0§ a §0\\geq 0§, takže §\\theta§ v množine je.")
            sum("Súčet dvoch nezáporných prvých zložiek je nezáporný, súčet v množine ostane.", "x_1\\geq 0,\\ x_2\\geq 0\\ \\Rightarrow\\ x_1+x_2\\geq 0")
            mult("Nerovnosť sa pri násobení záporným číslom otočí, preto skúsim §\\alpha=-1§; násobiť smiem každým §\\alpha\\in\\mathbb{R}§.", "(-1)\\cdot(1,0,0)=(-1,0,0),\\qquad -1<0")
            end("Množina nie je uzavretá na násobenie skalárom, preto nie je podpriestor.")
        },
        S("\\langle(1,${a},0),(0,1,${b})\\rangle", true, "Lineárny obal je vždy podpriestor. Pozri Vetu 2.38.",
            "Je to podpriestor: lineárny obal je podpriestor vždy, nech sú vektory akékoľvek. Obsahuje §\\theta§ a súčet aj násobok kombinácií sú zase kombinácie (Veta 2.38).") {
            theta("Označím §\\mathbf{u}=(1,$a,0)§, §\\mathbf{v}=(0,1,$b)§. Nulový vektor je ich kombinácia s nulovými koeficientmi, preto v obale je.", "\\theta=0\\cdot\\mathbf{u}+0\\cdot\\mathbf{v}")
            sum("Súčet dvoch kombinácií §\\mathbf{u},\\mathbf{v}§ je znova ich kombinácia.", "(a_1\\mathbf{u}+a_2\\mathbf{v})+(b_1\\mathbf{u}+b_2\\mathbf{v})=(a_1+b_1)\\mathbf{u}+(a_2+b_2)\\mathbf{v}")
            mult("Násobok kombinácie je tiež kombinácia.", "\\alpha(a_1\\mathbf{u}+a_2\\mathbf{v})=(\\alpha a_1)\\mathbf{u}+(\\alpha a_2)\\mathbf{v}")
            end(Reasons.spanIsSubspace)
        },
        S("\\{(x,y,z)\\in\\mathbb{Z}_5^3\\mid x+y+z=0\\}", true, "Homogénna rovnica nad §\\mathbb{Z}_5§: rovnaký dôvod ako nad §\\mathbb{R}§.",
            "Je to podpriestor: rovnica je homogénna, presne ako nad §\\mathbb{R}§. §\\theta§ ju spĺňa a súčet aj násobok riešení sú riešenia, počítané modulo §5§.") {
            theta("Pre §\\theta=(0,0,0)§ je §0+0+0=0§, rovnica platí.")
            sum("Nech §\\mathbf{u}§ a §\\mathbf{v}§ rovnicu spĺňajú. Pre súčet preskupím členy, počítam v §\\mathbb{Z}_5§:",
                "(u_1+v_1)+(u_2+v_2)+(u_3+v_3)=(u_1+u_2+u_3)+(v_1+v_2+v_3)=0+0=0")
            mult("Pre §\\alpha\\mathbf{u}§ vyberiem §\\alpha§ pred zátvorku.", "\\alpha u_1+\\alpha u_2+\\alpha u_3=\\alpha(u_1+u_2+u_3)=\\alpha\\cdot 0=0")
            end("Všetky tri podmienky Definície 2.7 platia, množina je podpriestor §\\mathbb{Z}_5^3§.")
        },
    )
    val s = sets.random(r)
    return Exercise(
        "subspace", "Je to podpriestor?", "Definícia 2.7, Príklady 2.11 až 2.17",
        // of which space: the set's own (a span of triples lives in ℝ³)
        "Je množina §${s.set}§ podpriestorom §${listOf("\\mathbb{Z}_5^3", "\\mathbb{R}^2").firstOrNull { it in s.set } ?: "\\mathbb{R}^3"}§?".replace('§', '$'),
        parts = listOf({ _ -> Part("Odpoveď:", Answer.Choice(listOf("Áno", "Nie"), if (s.yes) 0 else 1, s.why.replace('§', '$'), wrong = mapOf((if (s.yes) 1 else 0) to s.wrong))) }),
        postup = SubspacePostup,
        solution = { SubspacePostup.solve(s.solve) },
        hint = "Je v nej nulový vektor? Ak áno, skús súčet dvoch bodov a násobok záporným číslom.",
    )
}

/** Definícia 2.7 as a method: the three conditions in order, a counterexample ends it early. */
val SubspacePostup = postup(
    "Obsahuje §\\theta§?" to "Dosaď nulový vektor do podmienky množiny; ak v nej nie je, podpriestor to nie je.",
    "Súčet ostane v množine?" to "Vezmi dva ľubovoľné prvky a over podmienku pre ich súčet, alebo nájdi protipríklad.",
    "Násobok ostane v množine?" to "Over podmienku pre §\\alpha\\mathbf{u}§ s každým §\\alpha§, aj záporným, alebo nájdi protipríklad.",
    "Záver" to "Platia všetky tri: podpriestor. Jedna neplatí: nie je, a protipríklad to ukazuje.",
)

/* ---- Methods of the worked examples that no generator practises --------------------------------------------------- */

val SpanListPostup = postup(
    "Všeobecný prvok obalu" to "Napíš všeobecnú lineárnu kombináciu daných vektorov.",
    "Vypíš všetky voľby" to "V §\\mathbb{Z}_p§ prejdi všetky hodnoty koeficientov.",
)

val GeneratesPostup = postup(
    "Zostav sústavu" to "Vektory do stĺpcov, pravá strana je ľubovoľný vektor §\\mathbf{v}§ s premennými.",
    "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar; pravú stranu upravuj spolu s riadkami.",
    "Riešenie pre každú pravú stranu?" to "Ak je hlavný stĺpec v každom riadku vľavo od čiary, riešenie existuje vždy.",
    "Záver" to "Riešenie pre každé §\\mathbf{v}§: generuje. Inak nie.",
)

val BasisPostup = postup(
    "Počet vektorov a dimenzia" to "Ak je vektorov toľko, aká je dimenzia, stačí overiť jednu vlastnosť (Veta 2.55).",
    "Over LN" to "Vektory do stĺpcov homogénnej sústavy, GEM; všetky stĺpce hlavné znamená LN.",
    "Záver" to "LN súbor s počtom vektorov rovným dimenzii je báza.",
)

val VecToCoordsPostup = postup(
    "Zostav sústavu" to "Vektory bázy do stĺpcov, daný vektor ako pravá strana.",
    "Uprav ju GEM" to "Úpravami GEM dostaň horný stupňovitý tvar.",
    "Spätné dosadenie" to "Neznáme vypočítaj zdola nahor; to sú súradnice.",
    "Skúška" to "Kombinácia bázy so súradnicami musí dať pôvodný vektor.",
)

package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * Two drills of kapitola 4 as the 8. prednáška lectured them: whether two written solution sets are the same set
 * (Pozorování 4.14, Věta 4.18 bod 4; Klouda and Kleprlík, 8. prednáška) and every point of a line over Z_p (Klouda,
 * 8. prednáška: "nad konečným tělesem přímka jsou tři body"). Numbers are drawn, everything asked and checked is
 * computed by exact code; the student's list of points is judged by its property (each point in W, none twice, all
 * of them), never against one sample answer.
 */

private fun Field.drawEntry(r: Random): Frac = if (this is Field.Zp) Frac.of(r.pick(0, p - 1).toLong()) else Frac.of(r.pick(-2, 3).toLong())
private fun Field.drawVector(r: Random, n: Int): Vec = List(n) { drawEntry(r) }
private fun Field.minus(a: Vec, b: Vec): Vec = a.indices.map { sub(a[it], b[it]) }
private fun spanOf(vs: List<Vec>) = vs.joinToString(",", "\\langle ", "\\rangle") { it.tex() }
/** The same span in a display formula that may break between its vectors on a narrow phone (docs/CONTENT.md, soft breaks). */
private fun spanBreaking(vs: List<Vec>) = vs.joinToString(",\\softbreak[2]", "\\langle ", "\\rangle") { it.tex() }
private fun pl(n: Int, one: String, few: String, other: String) = when (n) { 1 -> one; in 2..4 -> few; else -> other }
private fun mod(f: Field) = if (f is Field.Zp) " Počítam modulo §${f.p}§." else ""

/* ---- 1. Are two written solution sets the same set? (Pozorování 4.14, Věta 4.18 bod 4) ------------------------- */

private const val SrcRovnost = "Klouda a Kleprlík, 8. prednáška; Pozorování 4.14"

/** How the second description differs from the first: not at all, in the direction space, or only in the point. */
private enum class Differs { NOTHING, DIRECTION, POINT }

/** S1 = u + ⟨y⟩ and S2 = v + ⟨z⟩, both bases LN with k vectors. */
private class TwoSets(val f: Field, val u: Vec, val y: List<Vec>, val v: Vec, val z: List<Vec>) {
    val k = y.size
    val d = f.minus(u, v)
    val rows = y + z + listOf(d)
    val rank = f.rank(rows)
    /** The rank of the two bases alone, without the row u − v. */
    val rankDirs = f.rank(y + z)
    val sameDir = rankDirs == k
    val dInDir = f.inSpan(y, d)
    val equal = rank == k
    val differs = when { !sameDir -> Differs.DIRECTION; !dInDir -> Differs.POINT; else -> Differs.NOTHING }
}

private fun Random.twoSets(): TwoSets {
    while (true) {
        val f: Field = listOf(Field.R, Field.R, Field.Zp(3), Field.Zp(5), Field.Zp(7)).random(this)
        val k = pick(1, 2)
        val n = pick(3, 4) // ℝ⁵ with a sign does not fit one line of a 360 dp phone
        val want = listOf(Differs.NOTHING, Differs.NOTHING, Differs.DIRECTION, Differs.POINT).random(this)
        val y = List(k) { f.drawVector(this, n) }
        if (!f.isLN(y) || y.any { v -> v.count { it.isZero } > n - 2 }) continue
        val u = f.drawVector(this, n)
        // another basis of ⟨y⟩: z_i = Σ_j M_ij y_j with an invertible k×k matrix M
        val mm = List(k) { List(k) { if (f is Field.Zp) Frac.of(pick(0, f.p - 1).toLong()) else Frac.of(pick(-2, 2).toLong()) } }
        val zSame = mm.map { row -> f.combo(row, y) }
        if (!f.isLN(zSame) || zSame.toSet() == y.toSet()) continue
        val off = f.drawVector(this, n)
        val z = if (want == Differs.DIRECTION) {
            if (f.inSpan(y, off)) continue
            if (k == 1) listOf(off) else listOf(zSame[0], off).shuffled(this)
        } else zSame
        // the shift v − u: a nonzero vector of ⟨y⟩, or (a different point) one outside it
        val inDir = f.combo(List(k) { if (f is Field.Zp) Frac.of(pick(1, f.p - 1).toLong()) else Frac.of(listOf(-2, -1, 1, 2).random(this).toLong()) }, y)
        val shift = when (want) {
            Differs.NOTHING -> inDir
            Differs.POINT -> f.drawVector(this, n)
            Differs.DIRECTION -> if (nextBoolean()) inDir else f.drawVector(this, n)
        }
        if (shift.isZero()) continue
        val v = f.plus(u, shift)
        val t = TwoSets(f, u, y, v, z)
        if (t.differs != want) continue
        if (f is Field.R && (z.flatten() + v).any { !it.isInt || kotlin.math.abs(it.num) > 9 }) continue
        return t
    }
}

/** The method of Pozorování 4.14 (kap4 balík, postup E). */
val RovnostPostup = postup(
    "Zostav maticu" to "Do riadkov napíš vektory oboch báz a rozdiel bodov posunutia §\\mathbf{u}-\\mathbf{v}§.",
    "Hodnosť cez GEM" to "Maticu uprav GEM na HST; hodnosť je počet nenulových riadkov.",
    "Zamerania a posunutie" to "Rovnajú sa zamerania a leží §\\mathbf{u}-\\mathbf{v}§ v zameraní? To sú dve podmienky rovnosti variet.",
    "Záver" to "Hodnosť rovná počtu vektorov jednej bázy: množiny sa rovnajú. Väčšia: nerovnajú sa.",
)

/** "§(2,4)=2\cdot(1,2)§", or that the vector is one of [y] itself (no "(1,2)=(1,2)"); [x] must lie in ⟨y⟩. */
private fun comboOfY(f: Field, y: List<Vec>, x: Vec): String {
    val c = f.solve(y, x)!!
    val lin = Terms.lin(c, y.map { it.tex() })
    return if (lin == x.tex()) "§${x.tex()}§ je priamo vektor prvej bázy" else "§${x.tex()}=$lin§"
}

private fun names(v: String, k: Int) = (1..k).map { "\\mathbf{$v}_{$it}" }

private fun SolutionScope.rovnostSteps(t: TwoSets) {
    val (build, gem, why, end) = RovnostPostup
    val f = t.f; val k = t.k
    val ys = names("y", k); val zs = names("z", k)
    val yy = ys.joinToString(",", "\\langle ", "\\rangle"); val zz = zs.joinToString(",", "\\langle ", "\\rangle")
    val ln = if (k == 1) "Oba vektory báz sú nenulové, teda oba súbory sú LN" else "V oboch bázach ani jeden vektor nie je násobkom druhého, teda sú LN"
    build("Označím §S_1=\\mathbf{u}+$yy§ a §S_2=\\mathbf{v}+$zz§. $ln a majú po §$k§ ${pl(k, "vektore", "vektoroch", "vektoroch")}, preto smiem použiť Pozorování 4.14.",
        listOf(Formula("\\mathbf{u}-\\mathbf{v}=${t.u.tex()}-${t.v.tex()}=${t.d.tex()}", if (f is Field.Zp) "v §\\mathbb{Z}_{${f.p}}§" else null)))
    build("Do riadkov matice dám §${(ys + zs).joinToString(",")}§ a nakoniec §\\mathbf{u}-\\mathbf{v}§; spolu §${2 * k + 1}§ ${pl(2 * k + 1, "riadok", "riadky", "riadkov")}.")
    val e = f.echelon(Mat(t.rows))
    gem((if (e.steps.isEmpty()) "Matica už je v HST, upravovať netreba." else "Maticu upravím GEM na HST.") + mod(f), gemLines(e))
    gem("V HST ${pl(t.rank, "ostal", "ostali", "ostalo")} §${t.rank}§ ${pl(t.rank, "nenulový riadok", "nenulové riadky", "nenulových riadkov")}. ${Reasons.rankHst}", "h=${t.rank}")
    if (t.sameDir) {
        why("Každý vektor druhej bázy je kombináciou prvej, takže §$zz\\subseteq$yy§; oba obaly majú dimenziu §$k§, preto sa rovnajú.",
            t.z.indices.map { i -> Formula("\\mathbf{z}_{${i + 1}}=${t.z[i].tex()}=" + Terms.lin(f.solve(t.y, t.z[i])!!, ys)) })
        if (t.dInDir) why("Aj §\\mathbf{u}-\\mathbf{v}§ je kombináciou §${ys.joinToString(",")}§, teda leží v zameraní a platia obe podmienky Věty 4.18 (bod 4).",
            "\\mathbf{u}-\\mathbf{v}=" + Terms.lin(f.solve(t.y, t.d)!!, ys))
        else why("Bez riadku §\\mathbf{u}-\\mathbf{v}§ je hodnosť §$k§, lebo ostatné riadky ležia v §$yy§ dimenzie §$k§. Riadok §\\mathbf{u}-\\mathbf{v}§ ju zvýšil na §${t.rank}§, preto v zameraní neleží a druhá podmienka Věty 4.18 (bod 4) neplatí.")
    } else {
        val j = t.z.indices.first { !f.inSpan(t.y, t.z[it]) }
        why("Vektor §\\mathbf{z}_{${j + 1}}=${t.z[j].tex()}§ nie je kombináciou §${ys.joinToString(",")}§: matica s týmito riadkami má hodnosť §${k + 1}§." + mod(f), gemLines(f.echelon(Mat(t.y + listOf(t.z[j])))))
        why("Preto §$zz\\neq$yy§, zamerania sa líšia a prvá podmienka Věty 4.18 (bod 4) neplatí; posunutie už netreba skúmať.")
    }
    if (t.equal) end("Hodnosť je §$k§, teda všetky riadky ležia v jednom podpriestore dimenzie §$k§ a množiny sa rovnajú (Pozorování 4.14). Správnosť voči sústave to neoverí: obaja študenti majú rovnako dobre, alebo rovnako zle.", "S_1=S_2")
    else end("Hodnosť je §${t.rank}>$k§, preto sa množiny nerovnajú (Pozorování 4.14).", "S_1\\neq S_2")
}

fun k4RovnostExercise(r: Random): Exercise {
    val t = r.twoSets()
    val f = t.f; val k = t.k; val n = t.u.size
    val sp = fieldPow(f, n)
    val text = m("Dva studenti řešili tutéž soustavu v §$sp§ a množinu řešení zapsali takto: §§\\begin{aligned}S_1&=${t.u.tex()}+${spanBreaking(t.y)}\\\\S_2&=${t.v.tex()}+${spanBreaking(t.z)}\\end{aligned}§§ Rozhodněte, zda popisují stejnou množinu.")
    val dRow = "§${t.d.tex()}§"
    val rankDiag = diagnosis(Field.R, listOf(t.rank.q()), listOf(
        listOf(t.rankDirs.q()) to "To je hodnosť bez riadku §\\mathbf{u}-\\mathbf{v}=${t.d.tex()}§. Len ten riadok zachytí, či rozdiel bodov posunutia leží v zameraní, preto ho Pozorování 4.14 do matice pridáva.",
        listOf(f.rank(t.y + t.z + listOf(t.u, t.v)).q()) to "Vyšlo by to s bodmi §\\mathbf{u}§ a §\\mathbf{v}§ ako dvoma riadkami. Do matice patrí jediný riadok, ich rozdiel $dRow.",
        listOf(f.rank(t.y + t.z + listOf(f.plus(t.u, t.v))).q()) to "Vyšlo by to s riadkom §\\mathbf{u}+\\mathbf{v}§. Posledný riadok je rozdiel bodov posunutia, $dRow.",
        listOf((2 * k + 1).q()) to "To je počet riadkov matice. Hodnosť je počet nenulových riadkov po úprave GEM na HST.",
    )) { g ->
        val x = g[0]
        when {
            x > Frac.of((2 * k + 1).toLong()) -> "Matica má len §${2 * k + 1}§ ${pl(2 * k + 1, "riadok", "riadky", "riadkov")}, hodnosť nemôže byť väčšia. Hodnosť je počet nenulových riadkov v HST."
            x < Frac.of(k.toLong()) -> "Hodnosť je aspoň §$k§, lebo už vektory prvej bázy sú LN. Skontroluj úpravy GEM."
            x < t.rank.q() -> "Nenulových riadkov v HST ostane viac. Riadok sa vynuluje, len ak je kombináciou ostatných; skontroluj úpravy GEM" + (if (f is Field.Zp) " modulo §${f.p}§." else ".")
            else -> "Nenulových riadkov v HST ostane menej. Skontroluj, či si nevynechal úpravu, ktorá riadok vynuluje" + (if (f is Field.Zp) ", a počítaj modulo §${f.p}§." else ".")
        }
    }
    val correct = when (t.differs) { Differs.NOTHING -> 0; Differs.DIRECTION -> 1; Differs.POINT -> 2 }
    val zOut = t.z.firstOrNull { !f.inSpan(t.y, it) }
    val wrong: Map<Int, String> = when (t.differs) {
        Differs.NOTHING -> mapOf(
            1 to "Zamerania sú rovnaké: vektory druhej bázy sú kombináciami prvej. Napríklad ${comboOfY(f, t.y, t.z[0])}; obe bázy majú §$k§ LN ${pl(k, "vektor", "vektory", "vektorov")}, teda generujú ten istý podpriestor.",
            2 to "Rozdiel bodov posunutia v zameraní leží: ${comboOfY(f, t.y, t.d)}. Preto riadok §\\mathbf{u}-\\mathbf{v}§ hodnosť nezvýšil.",
        )
        Differs.DIRECTION -> mapOf(
            0 to "Hodnosť je §${t.rank}§, nie §$k§, preto sa množiny nerovnajú. Vektor §${zOut!!.tex()}§ druhej bázy nie je kombináciou prvej, preto sú zamerania rôzne (Pozorování 4.14).",
            2 to "Zamerania nie sú rovnaké: §${zOut!!.tex()}§ nie je kombináciou vektorov prvej bázy. Riadky oboch báz majú spolu hodnosť §${t.rankDirs}§, väčšiu než §$k§.",
        )
        Differs.POINT -> mapOf(
            0 to "Hodnosť je §${t.rank}§, nie §$k§: zvýšil ju riadok $dRow. Rozdiel bodov posunutia neleží v zameraní, preto sa množiny nerovnajú (Věta 4.18, bod 4).",
            1 to "Zamerania sú rovnaké: bez riadku $dRow má matica hodnosť §$k§. Líši sa len posunutie, lebo rozdiel bodov v zameraní neleží.",
        )
    }
    val why = when (t.differs) {
        Differs.NOTHING -> "Áno: zamerania sa rovnajú a rozdiel bodov posunutia v nich leží, preto sa množiny rovnajú (Věta 4.18, bod 4). Ukazuje to hodnosť §$k§ (Pozorování 4.14). Správnosť voči sústave to neoverí: obaja majú rovnako dobre, alebo rovnako zle."
        Differs.DIRECTION -> "Nie: zamerania sa líšia, lebo §${zOut!!.tex()}§ nie je kombináciou prvej bázy. Preto je hodnosť §${t.rank}>$k§ (Pozorování 4.14)."
        Differs.POINT -> "Nie: zamerania sú rovnaké, ale rozdiel bodov $dRow v nich neleží. Riadok s ním zvýšil hodnosť z §$k§ na §${t.rank}§."
    }
    return Exercise(
        "la1-k4-rovnost", "Ten istý zápis riešenia?", SrcRovnost, text,
        parts = listOf(
            { _ -> Part(m("Hodnosť matice z oboch báz a rozdielu bodov posunutia:"),
                Answer.Numbers(listOf("h"), null, listOf(t.rank.q()), "\$h=${t.rank}$", diagnose = rankDiag),
                hint = m("Riadky sú §${2 * k + 1}§: vektory prvej bázy, vektory druhej bázy a rozdiel bodov posunutia. Uprav ich GEM na HST a spočítaj nenulové riadky."), aim = 1) },
            { _ -> Part("Popisujú §S_1§ a §S_2§ tú istú množinu?".replace('§', '$'),
                Answer.Choice(listOf("Áno, rovnajú sa", "Nie, zamerania sa líšia", "Nie, zamerania sú rovnaké, ale rozdiel bodov posunutia v nich neleží"), correct, m(why), wrong.mapValues { m(it.value) }),
                hint = m("Množiny sa rovnajú práve vtedy, keď je hodnosť §$k§. Ak je väčšia, porovnaj hodnosť matice bez riadku §\\mathbf{u}-\\mathbf{v}§: tá rozhodne o zameraniach."), aim = 3) },
        ),
        postup = RovnostPostup,
        solution = { RovnostPostup.solve { rovnostSteps(t) } },
        hint = m("Do riadkov daj obe bázy a rozdiel bodov posunutia. Hodnosť rovná počtu vektorov jednej bázy znamená rovnosť množín."),
    )
}

/* ---- 2. Every point of a line over Z_p (Klouda, 8. prednáška) ------------------------------------------------- */

private const val SrcBody = "Klouda, 8. prednáška (39:59–42:45); skriptá, kapitola 4, variety nad konečným telesom; Definice 4.16"

/**
 * Every point of the line W = [a] + ⟨[u]⟩ ⊂ Z_p^n, each written once. Wrong entries: a repeat, a vector of ⟨u⟩ (the
 * shift a forgotten), any other vector outside W (v − a not a multiple of u). The list is complete when nothing is
 * missing; a itself is named when it is left out (α = 0, the most common slip).
 */
internal fun judgeLinePoints(f: Field.Zp, a: Vec, u: Vec, entries: List<List<Vec>>): VectorsResult {
    val p = f.p
    val pts = (0 until p).map { f.plus(a, f.scale(Frac.of(it.toLong()), u)) }.toSet()
    val dir = f.spanElements(listOf(u))
    val written = HashMap<Vec, Int>()
    val good = HashSet<Vec>()
    val each = entries.mapIndexed { i, e ->
        val v = e[0].map(f::norm)
        val earlier = written[v]
        if (earlier == null) written[v] = i
        when {
            earlier != null -> "Tento bod už máš ako ${earlier + 1}. prvok. Množina obsahuje každý bod raz, opakovanie nič nepridá."
            v !in pts && v in dir -> "${entryTex(v)} leží v zameraní §\\langle\\mathbf{u}\\rangle=\\langle ${u.tex()}\\rangle§, nie vo §W§. Chýba pripočítaný bod posunutia: body §W§ sú §\\mathbf{a}+\\alpha\\mathbf{u}§."
            v !in pts -> "${entryTex(v)} do §W§ nepatrí. Rozdiel §${v.tex()}-\\mathbf{a}=${f.minus(v, a).tex()}§ nie je násobkom §\\mathbf{u}=${u.tex()}§."
            else -> { good += v; null }
        }
    }
    val missing = pts - good
    val bad = each.count { it != null }
    val size = "Priamka nad §\\mathbb{Z}_{$p}§ má §$p§ ${pl(p, "bod", "body", "bodov")}: §\\mathbf{a}+\\alpha\\mathbf{u}§ pre každé §\\alpha\\in\\mathbb{Z}_{$p}§, a rôzne §\\alpha§ dajú rôzne body, lebo §\\mathbf{u}\\neq\\theta§."
    val head = when {
        missing.isEmpty() && bad == 0 -> "Správne, ${pl(p, "jediný", "všetky", "všetkých")} §$p§ ${pl(p, "bod", "body", "bodov")} priamky, každý raz. $size"
        missing.isEmpty() -> "Všetky body máš, ale $bad ${pl(bad, "zápis je navyše", "zápisy sú navyše", "zápisov je navyše")}. Pri každom je napísané prečo. $size"
        else -> {
            val k = missing.size
            (when (k) { 1 -> "Chýba ešte §1§ bod."; in 2..4 -> "Chýbajú ešte §$k§ body."; else -> "Chýba ešte §$k§ bodov." }) +
                (if (a.map(f::norm) in missing) " Aj samotné §\\mathbf{a}=${a.tex()}§ je bod §W§: dostaneš ho pre §\\alpha=0§." else "") +
                (if (bad > 0) " Navyše $bad ${pl(bad, "zápis nesedí", "zápisy nesedia", "zápisov nesedí")}, pri každom je napísané prečo." else "") + " $size"
        }
    }
    return VectorsResult(missing.isEmpty() && bad == 0, m(head), each.map { it?.let(::m) })
}

/** Every point of a + ⟨u⟩ over Z_p. */
val BodyPostup = postup(
    "Všeobecný bod" to "Bod §W§ je §\\mathbf{a}+\\alpha\\mathbf{u}§ pre nejaké §\\alpha\\in\\mathbb{Z}_p§.",
    "Dosaď všetky §\\alpha§" to "Pre §\\alpha=0,1,\\dots,p-1§ spočítaj §\\mathbf{a}+\\alpha\\mathbf{u}§ po zložkách modulo §p§.",
    "Počet bodov" to "Rôzne §\\alpha§ dajú rôzne body, lebo §\\mathbf{u}\\neq\\theta§; v rovine dvojice §(\\alpha,\\beta)§.",
)

private fun SolutionScope.bodySteps(f: Field.Zp, a: Vec, u: Vec, w: Vec?) {
    val (general, all, count) = BodyPostup
    val p = f.p; val n = a.size
    val zp = "\\mathbb{Z}_{$p}"
    val byComponent = (0 until n).joinToString(",", "(", ")") { j -> Terms.sum(listOf(a[j].tex(true), Terms.times(u[j], "\\alpha"))) }
    general("Prvok variety je bod posunutia plus vektor zo zamerania, teda §\\mathbf{a}+\\alpha\\mathbf{u}§ pre §\\alpha\\in $zp§ (Definice 4.16).",
        Formula("W=\\{$byComponent\\mid\\alpha\\in $zp\\}"))
    val pts = (0 until p).map { al -> f.plus(a, f.scale(Frac.of(al.toLong()), u)) }
    all("Dosadím postupne §\\alpha=${(0 until p).joinToString(",")}§ a počítam modulo §$p§; §\\alpha=0§ dá samotný bod §\\mathbf{a}§.",
        (0 until p).map { al -> Formula("\\alpha=$al:\\ " + (if (al == 0) a.tex() else Terms.sum(listOf(a.tex(), Terms.times(Frac.of(al.toLong()), u.tex()))) + "=" + pts[al].tex())) })
    all("Množina má §$p§ ${pl(p, "prvok", "prvky", "prvkov")}.", "W=\\{" + pts.joinToString(",") { it.tex() } + "\\}")
    count("Rôzne §\\alpha§ dajú rôzne body: z §\\mathbf{a}+\\alpha\\mathbf{u}=\\mathbf{a}+\\beta\\mathbf{u}§ plynie §(\\alpha-\\beta)\\mathbf{u}=\\theta§, a keďže §\\mathbf{u}\\neq\\theta§, je §\\alpha=\\beta§. Priamka má teda §$p§ ${pl(p, "bod", "body", "bodov")}.")
    if (w != null) count("V rovine §\\mathbf{a}+\\langle\\mathbf{u},\\mathbf{w}\\rangle§ sú §\\mathbf{u},\\mathbf{w}=${w.tex()}§ LN, preto rôzne dvojice §(\\alpha,\\beta)§ dajú rôzne body.",
        "|\\mathbf{a}+\\langle\\mathbf{u},\\mathbf{w}\\rangle|=$p\\cdot $p=${p * p}")
}

fun k4BodyExercise(r: Random): Exercise {
    val p = listOf(2, 3, 3, 5, 5).random(r)
    val n = listOf(2, 3, 3).random(r)
    val f = Field.Zp(p)
    while (true) {
        val a = f.drawVector(r, n); val u = f.drawVector(r, n); val w = f.drawVector(r, n)
        // a outside ⟨u⟩: W is then not a subspace, so forgetting the shift is a visible slip
        if (u.isZero() || f.inSpan(listOf(u), a) || !f.isLN(listOf(u, w))) continue
        val pts = (0 until p).map { al -> f.plus(a, f.scale(Frac.of(al.toLong()), u)) }
        val sp = "\\mathbb{Z}_{$p}^{$n}"
        val text = m("Napište všechny prvky variety §W=\\mathbf{a}+\\langle\\mathbf{u}\\rangle§ v §$sp§, kde §\\mathbf{a}=${a.tex()}§ a §\\mathbf{u}=${u.tex()}§.")
        val plane = (p * p).toLong()
        val cells = List(n) { "x_{${it + 1}}" }
        return Exercise(
            "la1-k4-body", "Všetky body priamky v §\\mathbb{Z}_p^n§", SrcBody, text,
            parts = listOfNotNull<(List<Any>) -> Part>(
                { _ -> Part(m("Všetky body priamky §W§, každý raz:"),
                    Answer.Vectors(cells, p, 1..(p + 3), entry = "bod", check = { e -> judgeLinePoints(f, a, u, e) }, example = pts.shuffled(r).map { listOf(it) }),
                    hint = m("Bod §W§ je §\\mathbf{a}+\\alpha\\mathbf{u}§. Dosaď každé §\\alpha\\in\\mathbb{Z}_{$p}§, aj §\\alpha=0§, a počítaj modulo §$p§."), aim = 1) },
                // in Z_p^2 a plane is the whole space: the count is asked in Z_p^3 only
                if (n < 3) null else { _ -> Part(m("Koľko bodov (vektorov) má rovina §\\mathbf{a}+\\langle\\mathbf{u},\\mathbf{w}\\rangle§ v §$sp§ pre §\\mathbf{w}=${w.tex()}§?"),
                    Answer.Numbers(listOf("\\#"), null, listOf(Frac.of(plane)), "\$$p^2=$plane$", diagnose = diagnosis(Field.R, listOf(Frac.of(plane)), listOf(
                        listOf(Frac.of(p.toLong())) to "To je počet bodov priamky. Rovina má dva LN smerové vektory a každá dvojica koeficientov §(\\alpha,\\beta)§ dá iný bod.",
                        listOf(Frac.of(2L * p)) to "Voľby §\\alpha§ a §\\beta§ sa násobia, nesčítajú. Každá dvojica §(\\alpha,\\beta)§ dá iný bod, dvojíc je §$p\\cdot $p§.",
                        listOf(Frac.of(pow(p, n))) to "§${Terms.pow(p.toLong(), n)}§ je počet prvkov celého §$sp§. Rovina má dimenziu §2§, jej body zodpovedajú dvojiciam §(\\alpha,\\beta)§.",
                        listOf(Frac.of(plane - 1)) to "Aj samotný bod posunutia patrí do roviny. Dostaneš ho pre §\\alpha=\\beta=0§.",
                    )) { "Bod roviny je §\\mathbf{a}+\\alpha\\mathbf{u}+\\beta\\mathbf{w}§. Vektory §\\mathbf{u},\\mathbf{w}§ sú LN, preto rôzne dvojice §(\\alpha,\\beta)\\in\\mathbb{Z}_{$p}^2§ dajú rôzne body." }),
                    hint = m("Bod roviny je §\\mathbf{a}+\\alpha\\mathbf{u}+\\beta\\mathbf{w}§ s §\\alpha,\\beta\\in\\mathbb{Z}_{$p}§. Koľko je dvojíc §(\\alpha,\\beta)§?"), aim = 2) },
            ),
            postup = BodyPostup,
            solution = { BodyPostup.solve { bodySteps(f, a, u, if (n < 3) null else w) } },
            hint = m("Dosaď do §\\mathbf{a}+\\alpha\\mathbf{u}§ každé §\\alpha\\in\\mathbb{Z}_{$p}§ a počítaj modulo §$p§; §\\alpha=0§ dá samotné §\\mathbf{a}§."),
        )
    }
}

internal val K4DrillGenerators: Map<String, (Random) -> Exercise> = mapOf(
    "la1-k4-rovnost" to ::k4RovnostExercise,
    "la1-k4-body" to ::k4BodyExercise,
)

internal val K4DrillTemplates = listOf(
    TemplateInfo("la1-k4-rovnost", "Ten istý zápis riešenia?", "Dva zápisy variety: hodnosť s rozdielom bodov posunutia; Pozorování 4.14"),
    TemplateInfo("la1-k4-body", "Všetky body priamky v §\\mathbb{Z}_p^n§", "Priamka nad §\\mathbb{Z}_p§ má §p§ bodov; Klouda, 8. prednáška"),
)

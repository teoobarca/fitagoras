package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import kotlin.random.Random
import dev.hamper.tabula.compat.JMath

/*
 * Kapitola 2, what the 2nd tests ask the student to make or list rather than compute (2. testy 2022 až 2024, MARAST
 * #13691, skriptá kapitola 2): another basis of M (also one containing a given vector), the unknown basis from known
 * coordinates, a matrix A and b with two given solutions and LN rows, a set of four vectors with given bases among its
 * triples, every element of a finite span, every subspace of a small Z_p^n, the parameter over Z_p, coordinates of a
 * combination and how many combinations give a vector. Numbers are drawn, everything else is computed by exact code;
 * whatever the student writes is judged by its property (data/Vectors.kt), never against one sample answer.
 * ConstructTemplatesTest checks every generator over many seeds against brute force.
 *
 * Every problem type has its method (a Postup, data/Postup.kt, named next to the generator) and every solution is
 * written as steps under its subgoals.
 */

private fun Random.pick(lo: Int, hi: Int) = nextInt(lo, hi + 1)
private fun Random.nonzero(lo: Int, hi: Int): Int { while (true) { val x = pick(lo, hi); if (x != 0) return x } }
private fun n1(v: Long) = listOf(Frac.of(v))
private fun cells(n: Int, v: String = "x") = List(n) { "${v}_{${it + 1}}" }
private fun Field.pp() = (this as? Field.Zp)?.p
private fun Field.draw(r: Random, lo: Int = -2, hi: Int = 3): Frac = if (this is Field.Zp) Frac.of(r.pick(0, p - 1)) else Frac.of(r.pick(lo, hi))
private fun Field.drawVec(r: Random, n: Int, lo: Int = -2, hi: Int = 3): Vec = List(n) { draw(r, lo, hi) }
private fun pl(n: Long, one: String, few: String, other: String) = when (n) { 1L -> one; in 2L..4L -> few; else -> other }

/** "(\mathbf{b}_1,\mathbf{b}_2)" */
private fun names(v: String, k: Int) = (1..k).joinToString(",") { "\\mathbf{$v}_{$it}" }

/* ---- 1. Another basis of M: of a given basis, containing a given vector, with a vector outside the given set ---- */

private const val SrcOther = "Testy 22. 11. 2023 (varianty Myš, Sova), 24. 11. 2023 (variant Včelka), 15. 11. 2022 (variant A) a 14. 11. 2024 (variant Medvěd), príklad 2"

fun otherBasisExercise(r: Random): Exercise {
    val f: Field = if (r.nextInt(5) < 2) Field.R else Field.Zp(listOf(3, 5, 5, 7).random(r))
    val variant = r.nextInt(3)
    val p = f.pp()
    while (true) {
        val n = if (f is Field.R) r.pick(3, 4) else 4
        val d = if (variant == 2) 2 else r.pick(2, minOf(3, n - 1))
        val b = List(d) { f.drawVec(r, n) }
        if (!f.isLN(b) || b.any { v -> v.count { it.isZero } > n - 2 }) continue
        val sp = fieldPow(f, n)
        val bTex = vstex(b)
        when (variant) {
            0 -> {
                val example = listOf(f.scale(2.q(), b[0])) + b.drop(1)
                val text = m("Ve vektorovém prostoru §$sp§ je soubor §\\mathcal{B}=$bTex§ bází podprostoru §M§. Vymyslete ještě nějakou jinou bázi §M§ a vysvětlete, proč je to taky báze.")
                return otherBasis(f, b, text, "Iná báza §M§", example, d,
                    extra = { vs -> if (vs.toSet() == b.toSet()) m("Sú to tie isté vektory ako v §\\mathcal{B}§, len prípadne v inom poradí. Formálne je aj prehodené poradie iná báza (súbor je usporiadaný), ale test to tak nemyslí: 14. 11. 2024 výslovne chcel vektor, ktorý v pôvodnom súbore nie je. Vymeň aspoň jeden, napríklad ho vynásob nenulovým číslom alebo k nemu pripočítaj iný vektor bázy.") else null },
                    postup = OtherBasisGivenPostup,
                    solution = { OtherBasisGivenPostup.solve {
                        val (need, change, ln, end) = OtherBasisGivenPostup
                        need("Treba §$d§ LN vektory z §M§. Taký súbor je totiž už báza: vektor mimo jeho obalu by dal LN súbor dlhší, než dovolí §\\dim M=$d§ (Veta 2.55).")
                        change("Prvý vektor bázy vynásobím dvoma; násobok prvku §M§ je v §M§ a ostatné vektory sú z §\\mathcal{B}§.", "${vstex(b)}\\ \\to\\ ${vstex(example)}")
                        ln("Kombinácia nového súboru je kombináciou §\\mathcal{B}§ s koeficientmi §2\\alpha," + (2..d).joinToString(",") { "\\beta_{$it}" } + "§, a §\\mathcal{B}§ je LN.",
                            Formula("\\alpha(2\\mathbf{b}_1)+" + (2..d).joinToString("+") { "\\beta_{$it}\\mathbf{b}_{$it}" } + "=\\theta\\ \\Rightarrow\\ 2\\alpha=0," + (2..d).joinToString(",") { "\\ \\beta_{$it}=0" }, if (p != null) "§2§ má v §\\mathbb{Z}_{$p}§ inverz, teda §\\alpha=0§" else "teda §\\alpha=0§"))
                        end("Sú to §$d§ LN vektory z §M§ a §\\dim M=$d§, takže je to báza §M§, iná než §\\mathcal{B}§ (Veta 2.55).")
                    } })
            }
            1 -> {
                val c = List(d) { if (f is Field.R) r.nonzero(-2, 2).q() else Frac.of(r.pick(1, p!! - 1)) }.toMutableList()
                if (d == 3 && r.nextBoolean()) c[r.nextInt(3)] = Frac.ZERO
                val z = f.combo(c, b)
                if (z.isZero() || z in b || c.count { !it.isZero } < 2 || (f is Field.R && z.any { kotlin.math.abs(it.num) > 9 })) continue
                val k = c.indexOfFirst { !it.isZero }
                val example = b.mapIndexed { i, v -> if (i == k) z else v }
                val text = m("Ve vektorovém prostoru §$sp§ je soubor §\\mathcal{B}=$bTex§ bází podprostoru §M§. Najděte bázi §M§, která obsahuje vektor §\\mathbf{z}=${z.tex()}§, a vysvětlete, proč je to báze.")
                return otherBasis(f, b, text, "Báza s daným vektorom", example, d,
                    extra = { vs -> if (vs.none { it == z }) m("V tvojej báze nie je vektor §\\mathbf{z}=${z.tex()}§, a zadanie ho tam chce. Báza §M§ to je, ale nie tá, ktorú zadanie pýta.") else null },
                    postup = BasisWithVectorPostup,
                    solution = { BasisWithVectorPostup.solve {
                        val (where, swap, why) = BasisWithVectorPostup
                        val e = f.echelon(Mat.columns(b, listOf(z)), d)
                        where("Súradnice §\\mathbf{z}§ v §\\mathcal{B}§ sú riešenie sústavy so stĺpcami z §\\mathcal{B}§ a pravou stranou §\\mathbf{z}§.", gemLines(e))
                        where("Riešenie je jediné, dosadzujem zdola:", backSubst(f, e, (1..d).map { "\\alpha_$it" }).first + Formula("\\mathbf{z}=" + comboTexF(c, (1..d).map { "\\mathbf{b}_{$it}" })))
                        swap("Vymením vektor s nenulovým koeficientom, napríklad §\\mathbf{b}_{${k + 1}}§.", "${vstex(b)}\\ \\to\\ ${vstex(example)}")
                        why("Z rovnosti vyjadrím §\\mathbf{b}_{${k + 1}}§ (delím §${c[k].tex(true)}\\neq 0§), takže nový súbor generuje celé §M§. Má §$d=\\dim M§ prvkov, preto je to báza (Veta 2.55).")
                    } })
            }
            else -> {
                // M given by three generators, the third a combination of the first two (test 14. 11. 2024, medvěd)
                val k1 = f.draw(r, -2, 2).let { if (it.isZero) Frac.ONE else it }; val k2 = f.draw(r, -2, 2).let { if (it.isZero) Frac.ONE else it }
                val g3 = f.combo(listOf(k1, k2), b)
                if (g3.isZero() || g3 in b || (f is Field.R && g3.any { kotlin.math.abs(it.num) > 9 })) continue
                val gens = listOf(b[0], b[1], g3).shuffled(r)
                val piv = f.pickBasis(gens)
                val chosen = piv.map { gens[it] }
                val candidates = listOf(listOf(f.scale(2.q(), chosen[0]), chosen[1]), listOf(f.plus(chosen[0], chosen[1]), chosen[1]), listOf(chosen[0], f.plus(chosen[0], f.scale(2.q(), chosen[1]))))
                val outside = { vs: List<Vec> -> if (vs.all { it in gens }) m("Všetky tvoje vektory sú zo zadaného súboru, a zadanie chce aspoň jeden iný. Báza §M§ to je, ale nie tá, ktorú zadanie pýta; skús súčet dvoch generátorov alebo nenulový násobok.") else null }
                val example = candidates.firstOrNull { cand -> judgeBasis(f, gens, cand.map { listOf(it) }, outside).ok } ?: continue
                val gTex = "(" + gens.joinToString(",") { it.tex() } + ")"
                val text = m("Ve vektorovém prostoru §$sp§ je dán podprostor §M=\\langle " + gens.joinToString(",") { it.tex() } + "\\rangle§. Určete jeho dimenzi a najděte nějakou bázi §M§, která obsahuje alespoň jeden vektor, který nepatří do souboru §$gTex§. Pokud to není zřejmé, vysvětlete, proč je to také báze §M§.")
                return otherBasis(f, gens, text, "Báza s novým vektorom", example, 2, extra = outside, askDim = true,
                    postup = BasisNewVectorPostup,
                    solution = { BasisNewVectorPostup.solve {
                        val (basis, fresh, why) = BasisNewVectorPostup
                        basis("Generátory dám do stĺpcov a upravím GEM. " + Reasons.gemColumns, gemLines(f.echelon(Mat.columns(gens))))
                        basis(Reasons.mainColumns(piv) + " Báza je §${vstex(chosen)}§. " + Reasons.dim("\\dim M=2"))
                        fresh("Návrh obsahuje vektor, ktorý v zadanom súbore nie je, a oba vektory sú kombináciou bázy, teda ležia v §M§.", "${vstex(chosen)}\\ \\to\\ ${vstex(example)}")
                        why("Návrh vznikol z bázy úpravou, ktorá sa dá vrátiť (násobenie nenulovým číslom, pripočítanie násobku druhého), takže generuje to isté §M§. Dva vektory, ktoré generujú §M§ s §\\dim M=2§, sú LN, lebo inak by §M§ generoval jeden vektor a dimenzia by bola najviac §1§. Tvoria teda bázu (Veta 2.55).")
                    } })
            }
        }
    }
}

val OtherBasisGivenPostup = postup(
    "Čo treba?" to "§\\dim M§ LN vektorov z §M§ je báza (Veta 2.55).",
    "Zmeň jeden vektor" to "Vynásob jeden vektor bázy číslom rôznym od §0§ aj §1§: ostane v §M§.",
    "Je to LN?" to "Kombinácia nového súboru je kombinácia pôvodnej bázy; tá je LN.",
    "Záver" to "LN súbor v §M§ s §\\dim M§ vektormi je báza.",
)

val BasisWithVectorPostup = postup(
    "Kde leží §\\mathbf{z}§?" to "Nájdi súradnice §\\mathbf{z}§ v danej báze: sústava so stĺpcami z bázy.",
    "Ktorý vektor vymeniť?" to "Vymeň za §\\mathbf{z}§ vektor bázy s nenulovým koeficientom.",
    "Prečo je to báza?" to "Vymenený vektor sa dá vyjadriť späť, nový súbor generuje §M§ a má §\\dim M§ prvkov.",
)

val BasisNewVectorPostup = postup(
    "Báza a dimenzia" to "Generátory do stĺpcov, GEM; vektory pri hlavných stĺpcoch tvoria bázu.",
    "Nový vektor" to "Uprav bázu vratnou úpravou: násobok nenulovým číslom alebo súčet s iným vektorom.",
    "Prečo je to báza?" to "Vratná úprava nemení obal; §\\dim M§ vektorov, ktoré generujú §M§, je báza.",
)

/** [gens] generate M; [askDim]: a first part asks dim M (the set given is LZ). */
private fun otherBasis(
    f: Field, gens: List<Vec>, text: String, label: String, example: List<Vec>, d: Int,
    extra: (List<Vec>) -> String?, postup: Postup, solution: () -> Solution, askDim: Boolean = false,
): Exercise {
    val n = gens[0].size
    val parts = ArrayList<(List<Any>) -> Part>()
    if (askDim) parts += { _ -> Part(m("Aká je dimenzia §M§?"), Answer.Numbers(listOf("\\dim M"), null, n1(d.toLong()), "$\\dim M=$d$", diagnose = dimDiagnosis(gens.size, n, d)),
        hint = m("Generátory do stĺpcov, GEM; dimenzia je počet hlavných stĺpcov."), aim = 0) }
    parts += { _ ->
        Part(m("Napíš svoju bázu §M§: §$d§ vektory."),
            Answer.Vectors(cells(n), f.pp(), d..d, entry = "vektor", check = { e -> judgeBasis(f, gens, e, extra) }, example = example.map { listOf(it) }),
            // with the dimension asked first the exercise has two parts, so this one needs its own hint and goal
            hint = if (askDim) m("Vezmi bázu z hlavných stĺpcov a uprav ju vratnou úpravou: vynásob vektor nenulovým číslom alebo k nemu pripočítaj iný.") else null,
            aim = if (askDim) 1 else null)
    }
    return Exercise("baza-ina", label, SrcOther, text, parts = parts, postup = postup, solution = { solution() },
        hint = m("Báza v podpriestore dimenzie §d§: §d§ LN vektorov z neho. Vynásob vektor bázy nenulovým číslom, pripočítaj k nemu iný, alebo vymeň vektor s nenulovým koeficientom."))
}

/* ---- 2. The basis from coordinates: (u)_B and (w)_B known, find x and y ------------------------------------------- */

private const val SrcFromCoords = "Testy 9. 11. 2022 (variant 6A) a 15. 11. 2022 (variant B), príklad 4; test listopad 2022, príklad 5"

/** The unknown basis (x, y) of Z_p^2, and the coordinates c1 of u and c2 of w with respect to it. */
internal data class FromCoordsCase(val p: Int, val x: Vec, val y: Vec, val c1: Vec, val c2: Vec, val u: Vec, val w: Vec)

internal fun fromCoordsCase(r: Random): FromCoordsCase {
    val p = listOf(3, 5, 5, 5, 7).random(r)
    val f = Field.Zp(p)
    while (true) {
        val xv = f.drawVec(r, 2); val yv = f.drawVec(r, 2)
        if (!f.isLN(listOf(xv, yv)) || (xv == unit(2, 0) && yv == unit(2, 1))) continue
        val c1 = f.drawVec(r, 2); val c2 = f.drawVec(r, 2)
        if (!f.isLN(listOf(c1, c2)) || (c1.any { it.isZero } && c2.any { it.isZero })) continue
        val b = listOf(xv, yv)
        val u = f.fromCoords(b, c1); val w = f.fromCoords(b, c2)
        if (u == c1 && w == c2) continue
        return FromCoordsCase(p, xv, yv, c1, c2, u, w)
    }
}

/** A paper's case: [c1] are the coordinates of [u], [c2] of [w]; the basis (x, y) solved component by component. */
internal fun fromCoordsCase(p: Int, c1: Vec, u: Vec, c2: Vec, w: Vec): FromCoordsCase {
    val f = Field.Zp(p)
    // component j: c1·(x_j, y_j) = u_j and c2·(x_j, y_j) = w_j, the columns of the system are (c1_0, c2_0) and (c1_1, c2_1)
    val cols = listOf(listOf(c1[0], c2[0]), listOf(c1[1], c2[1]))
    val xy = (0 until 2).map { j -> f.coords(cols, listOf(u[j], w[j]))!! }
    return FromCoordsCase(p, xy.map { it[0] }, xy.map { it[1] }, c1, c2, u, w)
}

fun basisFromCoordsExercise(r: Random): Exercise = basisFromCoordsExercise(fromCoordsCase(r))

internal fun basisFromCoordsExercise(case: FromCoordsCase): Exercise {
    val (p, xv, yv, c1, c2, u, w) = case
    val f = Field.Zp(p)
    run {
        val sp = "\\mathbb{Z}_{$p}^{2}"
        val text = m("Mějme soubor §(\\mathbf{x},\\mathbf{y})§ vektorů z §$sp§, o kterém víme, že tvoří bázi §$sp§. Souřadnice vektoru §${u.tex()}§ vzhledem k této bázi jsou §${c1.tex()}§ a souřadnice vektoru §${w.tex()}§ jsou §${c2.tex()}§. Určete, jak vypadají vektory §\\mathbf{x}§ a §\\mathbf{y}§.")
        val expected = xv + yv
        return Exercise(
            "baza-zo-suradnic", "Báza zo súradníc", SrcFromCoords, text,
            parts = listOf({ _ ->
                Part(m("Vektory §\\mathbf{x}§ a §\\mathbf{y}§:"), Answer.Numbers(listOf("x_1", "x_2", "y_1", "y_2"), p, expected, "$\\mathbf{x}=${xv.tex()},\\ \\mathbf{y}=${yv.tex()}$", diagnose = fromCoordsDiagnosis(f, c1, u, c2, w, xv, yv)))
            }),
            postup = BasisFromCoordsPostup,
            solution = { BasisFromCoordsPostup.solve { basisFromCoordsSteps(case) } },
            hint = m("Súradnice §(a,b)§ vektora §\\mathbf{u}§ znamenajú §a\\mathbf{x}+b\\mathbf{y}=\\mathbf{u}§. Dve také rovnice, neznáme sú vektory §\\mathbf{x}§ a §\\mathbf{y}§."),
        )
    }
}

val BasisFromCoordsPostup = postup(
    "Čo znamenajú súradnice?" to "Súradnice §(a,b)§ vektora §\\mathbf{u}§ znamenajú §a\\mathbf{x}+b\\mathbf{y}=\\mathbf{u}§.",
    "Sústava pre §\\mathbf{x},\\mathbf{y}§" to "Dve rovnice, neznáme sú vektory; GEM s pravými stranami po zložkách.",
    "Vyrieš" to "Z posledného riadku jeden vektor, dosadením druhý.",
    "Skúška" to "Dosaď do oboch rovníc.",
)

private fun fromCoordsDiagnosis(f: Field.Zp, c1: Vec, u: Vec, c2: Vec, w: Vec, xv: Vec, yv: Vec): (List<Frac>) -> String {
    // coordinates read as columns instead of rows: c1[0]x + c2[0]y = u, c1[1]x + c2[1]y = w, solved component by component
    val cols = (0 until 2).map { j -> f.solve(listOf(listOf(c1[0], c1[1]), listOf(c2[0], c2[1])), listOf(u[j], w[j])) }
    val transposed = if (cols.all { it != null }) listOf(cols[0]!![0], cols[1]!![0], cols[0]!![1], cols[1]!![1]) else null
    val swap = yv + xv
    val xy = listOf("\\mathbf{x}", "\\mathbf{y}")
    return diagnosis(f, xv + yv, listOf(
        transposed to "Súradnice si zapísal do stĺpcov namiesto riadkov. §(\\mathbf{u})_{\\mathcal{B}}=(a,b)§ znamená jednu rovnicu §a\\mathbf{x}+b\\mathbf{y}=\\mathbf{u}§, teda §a,b§ sú koeficienty jedného riadku sústavy.",
        swap to "Zamenil si §\\mathbf{x}§ a §\\mathbf{y}§: prvá súradnica patrí k prvému vektoru bázy, teda k §\\mathbf{x}§.",
        (c1 + c2) to "Súradnice nie sú vektory bázy. §(\\mathbf{u})_{\\mathcal{B}}=(a,b)§ znamená §a\\mathbf{x}+b\\mathbf{y}=\\mathbf{u}§; z dvoch takých rovníc treba §\\mathbf{x},\\mathbf{y}§ vypočítať.",
        (u + w) to "Zadané vektory nie sú §\\mathbf{x}§ a §\\mathbf{y}§: sú to kombinácie bázy so zadanými súradnicami. Z dvoch rovníc §a\\mathbf{x}+b\\mathbf{y}=\\mathbf{u}§ treba §\\mathbf{x},\\mathbf{y}§ vypočítať.",
    )) { given ->
        val gx = given.take(2); val gy = given.drop(2)
        val back = f.plus(f.scale(c1[0], gx), f.scale(c1[1], gy))
        if (back != u) "Skúška nesedí: s tvojimi vektormi je §${Terms.lin(c1, xy)}=${back.tex()}§, nie §${u.tex()}§. Skontroluj GEM v §\\mathbb{Z}_{${f.p}}§."
        else "Prvá rovnica sedí, druhá nie: §${Terms.lin(c2, xy)}=${f.plus(f.scale(c2[0], gx), f.scale(c2[1], gy)).tex()}§, nie §${w.tex()}§. Riešenie musí splniť obe rovnice naraz."
    }
}


/** x and y from the coordinates of two vectors, through GEM in Z_p: the generator and the lesson's worked example. */
internal fun SolutionScope.basisFromCoordsSteps(case: FromCoordsCase) {
    val (p, xv, yv, c1, c2, u, w) = case
    val f = Field.Zp(p)
    val e = f.echelon(Mat(listOf(c1 + u, c2 + w), 2), 2)
    val (meaning, system, solve, test) = BasisFromCoordsPostup
    val xy = listOf("\\mathbf{x}", "\\mathbf{y}")
    meaning("Súradnice sú koeficienty pri vektoroch bázy (Definícia 2.68), každá dvojica dá jednu rovnicu pre neznáme vektory.",
        Formula(Terms.lin(c1, xy) + "=${u.tex()}"), Formula(Terms.lin(c2, xy) + "=${w.tex()}"))
    system("Neznáme sú §\\mathbf{x},\\mathbf{y}§, koeficienty sú súradnice; napravo je každá zložka jeden stĺpec. Upravím GEM v §\\mathbb{Z}_{$p}§.", gemLines(e))
    solve("Z druhého riadku dostanem §\\mathbf{y}§, dosadením do prvého §\\mathbf{x}§.", Formula("\\mathbf{x}=${xv.tex()},\\ \\mathbf{y}=${yv.tex()}"))
    test("Dosadím do oboch rovníc v §\\mathbb{Z}_{$p}§; vektory sú LN, takže naozaj tvoria bázu.",
        Formula(Terms.lin(c1, listOf(xv.tex(), yv.tex())) + "=${u.tex()}"), Formula(Terms.lin(c2, listOf(xv.tex(), yv.tex())) + "=${w.tex()}"))
}

/* ---- 3. The parameter over Z_p: for which α is the set a basis, generates, is LN ------------------------------- */

private const val SrcParamZp = "Testy 15. 11. 2022 (variant A) a (variant 9A), príklad 1"

/** c·α + d over Z_p, an entry of the column with the parameter during GEM. */
internal data class Lin(val c: Int, val d: Int) {
    fun at(a: Int, p: Int) = JMath.floorMod(c * a + d, p)
    fun tex(v: String): String = when {
        c == 0 -> "$d"
        d == 0 -> if (c == 1) v else "$c$v"
        else -> (if (c == 1) v else "$c$v") + "+$d"
    }
}

/** Replays the row operations of a GEM on the column with the parameter (exact over Z_p). */
internal fun replay(ops: List<RowOp>, col: List<Lin>, p: Int): List<Lin> {
    val v = col.toMutableList()
    fun k(x: Frac) = JMath.floorMod(x.num, p.toLong()).toInt()
    for (op in ops) when (op) {
        is Swap -> { val tmp = v[op.i]; v[op.i] = v[op.j]; v[op.j] = tmp }
        is Combine -> { val a = k(op.a); val b = k(op.b); val s = v[op.src]; val t = v[op.target]; v[op.target] = Lin(JMath.floorMod(a * t.c + b * s.c, p), JMath.floorMod(a * t.d + b * s.d, p)) }
        is Scale -> { val a = k(op.k); val t = v[op.i]; v[op.i] = Lin(JMath.floorMod(a * t.c, p), JMath.floorMod(a * t.d, p)) }
        else -> error("unknown row operation $op")
    }
    return v
}

/**
 * For which α the set [numeric] plus the vector [base] with α + [shift] at [at] has the property: [truth] by brute force
 * (the rank for every α), [symbolic] read off the GEM as on paper (the operations on the numbers replayed on the
 * column with α). ConstructTemplatesTest checks they always agree.
 */
internal class ParamZpDecision(val truth: Set<Int>, val symbolic: Set<Int>, val echelon: Echelon, val deciding: List<Lin>, val rank: Int, val need: Int)

internal fun paramZpDecide(p: Int, kind: ParamKind, numeric: List<Vec>, base: Vec, at: Int, shift: Int): ParamZpDecision {
    val f = Field.Zp(p)
    val n = base.size
    val m = numeric.size + 1
    fun withA(a: Int): Vec = base.mapIndexed { j, x -> if (j == at) Frac.of(JMath.floorMod(a + shift, p)) else x }
    fun holds(vs: List<Vec>) = when (kind) { ParamKind.GENERATES -> f.rank(vs) == n; else -> f.rank(vs) == vs.size }
    val truth = (0 until p).filter { a -> holds(numeric + listOf(withA(a))) }.toSet()
    // GEM on the numbers, the column with the parameter last; replay the same operations on it
    val e = f.echelon(Mat.columns(numeric))
    val col = replay(e.steps.flatMap { it.ops }, base.mapIndexed { j, x -> if (j == at) Lin(1, shift) else Lin(0, x.num.toInt()) }, p)
    val rk = e.rank
    val need = when (kind) { ParamKind.GENERATES -> n; else -> m }
    val deciding = (rk until n).map { col[it] }
    val symbolic = (0 until p).filter { a -> rk >= need || (rk == need - 1 && deciding.any { it.at(a, p) != 0 }) }.toSet()
    return ParamZpDecision(truth, symbolic, e, deciding, rk, need)
}

val ParamZpPostup = postup(
    "Matica" to "Neznáme sú koeficienty kombinácie, dané vektory stĺpce sústavy; ten s parametrom posledný, GEM v §\\mathbb{Z}_p§.",
    "Čo rozhoduje?" to "Čísla s parametrom v riadkoch, kde je vľavo nula: nenulové znamená hlavný stĺpec.",
    "Každá hodnota" to "Nad §\\mathbb{Z}_p§ je hodnôt len §p§: dosaď každú.",
    "Záver" to "Vymenuj hodnoty, pre ktoré vlastnosť platí.",
)

/**
 * The GEM of the numbers with the column of the parameter beside them, as a walk of the shared GEM component
 * (la/GemWalk.kt): the operations come from the numbers, the column with α follows them exactly over Z_p.
 */
internal fun paramZpWalk(e: Echelon, column: List<Lin>, p: Int, v: String): GemWalk {
    fun frame(mat: Mat, pc: List<Lin>) = GemFrame(mat.rows.indices.map { i -> mat.rows[i].map { it.tex(true) } + pc[i].tex(v) })
    var cur = column
    val steps = e.steps.map { st -> cur = replay(st.ops, cur, p); st.ops to frame(st.after, cur) }
    return GemWalk.fromSteps(frame(e.start, column), steps)
}

/** The same walk from the vectors as the task gives them: [numeric] without the parameter, [base] with α + [shift] at [at]. */
fun paramZpWalk(p: Int, numeric: List<Vec>, base: Vec, at: Int, shift: Int, v: String = "\\alpha"): GemWalk =
    paramZpWalk(Field.Zp(p).echelon(Mat.columns(numeric)), base.mapIndexed { j, x -> if (j == at) Lin(1, shift) else Lin(0, x.num.toInt()) }, p, v)

/**
 * Where the matrix comes from (the course wants it written: README LA1, zhrnutie bod 1): the unknowns are the coefficients
 * of a combination, the given vectors are the columns. The coefficients are β, the parameter keeps the task's letter.
 */
fun paramZpSystem(kind: ParamKind, m: Int, p: Int): String {
    val bs = if (m <= 3) (1..m).joinToString(",") { "\\beta_$it" } else "\\beta_1,\\dots,\\beta_$m"
    // what the order of the columns does not change: for generating, the right sides with a solution; for LN, the combinations giving θ
    val order = " Vektor s parametrom dám ako posledný stĺpec; na poradí nezáleží, prehodenie vektorov nezmení, " + (if (kind == ParamKind.GENERATES) "pre ktoré §\\mathbf{b}§ má sústava riešenie" else "ktoré kombinácie dajú §\\theta§") + ". Počítam v §\\mathbb{Z}_{$p}§."
    return when (kind) {
        ParamKind.GENERATES -> "Súbor generuje §\\mathbb{Z}_{$p}^{3}§, keď pre každé §\\mathbf{b}§ existujú §$bs§ s §\\beta_1\\mathbf{x}_1+\\dots+\\beta_$m\\mathbf{x}_$m=\\mathbf{b}§. Po zložkách je to sústava s danými vektormi v stĺpcoch, o riešiteľnosti pre každé §\\mathbf{b}§ rozhodne jej matica." + order
        else -> "Z definície LN hľadám všetky §$bs§ s §\\beta_1\\mathbf{x}_1+\\dots+\\beta_$m\\mathbf{x}_$m=\\theta§. Po zložkách je to homogénna sústava, dané vektory sú jej stĺpce." + order
    }
}

/** What a nonzero deciding number means, in words, for each property (the reason before the theorem). */
fun paramZpRead(kind: ParamKind): String = when (kind) {
    ParamKind.LN -> "Nenulové číslo tam znamená, že aj posledný stĺpec je hlavný: homogénna sústava má len triviálne riešenie a súbor je LN."
    ParamKind.BASIS -> "Nenulové číslo tam znamená, že aj posledný stĺpec je hlavný: homogénna sústava má len triviálne riešenie a súbor je LN. " + Reasons.lnOfDim
    else -> "Nenulové číslo tam znamená hlavný stĺpec v každom riadku, sústava má riešenie pre každú pravú stranu a súbor generuje."
}

fun paramZpExercise(r: Random): Exercise {
    val p = listOf(3, 5, 5, 7).random(r)
    val f = Field.Zp(p)
    val n = 3
    while (true) {
        val kind = listOf(ParamKind.BASIS, ParamKind.BASIS, ParamKind.GENERATES, ParamKind.LN).random(r)
        val m = when (kind) { ParamKind.BASIS -> 3; ParamKind.GENERATES -> r.pick(3, 4); else -> r.pick(2, 3) }
        val numeric = List(m - 1) { f.drawVec(r, n) }
        if (numeric.any { it.isZero() }) continue
        val base = f.drawVec(r, n)
        val at = r.nextInt(n)
        val shift = if (r.nextBoolean()) 0 else r.pick(1, p - 1)
        fun withA(a: Int): Vec = base.mapIndexed { j, x -> if (j == at) Frac.of(JMath.floorMod(a + shift, p)) else x }
        val dec = paramZpDecide(p, kind, numeric, base, at, shift)
        val truth = dec.truth
        val edge = truth.isEmpty() || truth.size == p
        if (edge && r.nextInt(6) > 0) continue
        if (dec.symbolic != truth) continue // never (ConstructTemplatesTest): the solution reads the same GEM
        val e = dec.echelon; val rk = dec.rank; val need = dec.need; val deciding = dec.deciding
        val pos = r.nextInt(m)
        val shown = numeric.take(pos) + listOf(withA(0)) + numeric.drop(pos)
        val v = if (kind == ParamKind.GENERATES) "a" else "\\alpha"
        val setTex = "(" + shown.mapIndexed { i, s -> if (i == pos) "(" + s.indices.joinToString(",") { j -> if (j == at) Lin(1, shift).tex(v) else s[j].tex() } + ")" else s.tex() }.joinToString(",") + ")"
        val text = m(when (kind) {
            ParamKind.BASIS -> "Zjistěte, pro které hodnoty §$v\\in\\mathbb{Z}_{$p}§ je soubor vektorů §$setTex§ bází prostoru §\\mathbb{Z}_{$p}^{3}§."
            ParamKind.GENERATES -> "Zjistěte, pro které hodnoty §$v\\in\\mathbb{Z}_{$p}§ soubor vektorů §$setTex§ generuje prostor §\\mathbb{Z}_{$p}^{3}§."
            else -> "Zjistěte, pro které hodnoty §$v\\in\\mathbb{Z}_{$p}§ je soubor vektorů §$setTex§ z prostoru §\\mathbb{Z}_{$p}^{3}§ lineárně nezávislý."
        })
        val prop = when (kind) { ParamKind.BASIS -> "je bázou"; ParamKind.GENERATES -> "generuje"; else -> "je LN" }
        val propNot = when (kind) { ParamKind.BASIS -> "nie je bázou"; ParamKind.GENERATES -> "negeneruje"; else -> "je LZ" }
        fun why(a: Int): String = when {
            rk < need - 1 -> "Už vektory bez parametra ${if (kind == ParamKind.GENERATES) "generujú len podpriestor dimenzie §$rk§, jeden vektor navyše na celé §\\mathbb{Z}_{$p}^{3}§ nestačí" else "sú LZ, a pridaný vektor to nezmení"}."
            rk >= need -> "Už vektory bez parametra generujú celé §\\mathbb{Z}_{$p}^{3}§, nech je §$v§ akékoľvek."
            else -> {
                val vals = deciding.map { it.at(a, p) }
                val k = deciding.first { it.at(a, p) != 0 || it == deciding.last() }
                val shown = if (k.c == 0) "§${k.d}§" else "§${k.tex(v)}=${k.at(a, p)}§"
                if (vals.any { it != 0 }) "Pre §$v=$a§ je rozhodujúce číslo $shown, nenulové, súbor $prop."
                else "Pre §$v=$a§ je rozhodujúce číslo $shown, súbor $propNot."
            }
        }
        val summary = when {
            truth.isEmpty() -> "Neexistuje hodnota §$v§, pre ktorú súbor $prop."
            truth.size == p -> "Súbor $prop pre každé §$v\\in\\mathbb{Z}_{$p}§."
            else -> "Súbor $prop práve pre §$v\\in\\{${truth.sorted().joinToString(",")}\\}§."
        }
        val options = (0 until p).map { "§$v=$it§".replace('§', '$') }
        return Exercise(
            "param-zp", "Parameter nad §\\mathbb{Z}_p§".replace('§', '$'), SrcParamZp, text,
            parts = listOf({ _ ->
                Part(m("Pre ktoré §$v§ ${when (kind) { ParamKind.BASIS -> "je súbor bázou"; ParamKind.GENERATES -> "súbor generuje §\\mathbb{Z}_{$p}^{3}§"; else -> "je súbor LN" }}? Označ všetky (aj žiadne)."), Answer.Pick(options, 0..p, { it == truth }) { picked ->
                    if (picked == truth) m(summary + " " + why(truth.firstOrNull() ?: 0))
                    else {
                        val wrong = ((picked - truth) + (truth - picked)).sorted()
                        m("Nesedí to pre §$v\\in\\{${wrong.joinToString(",")}\\}§. " + summary + " " + wrong.joinToString(" ") { why(it) })
                    }
                })
            }),
            postup = ParamZpPostup,
            solution = { ParamZpPostup.solve {
                val (matrix, read, each, end) = ParamZpPostup
                matrix(paramZpSystem(kind, m, p), Formula(paramZpWalk(e, base.mapIndexed { j, x -> if (j == at) Lin(1, shift) else Lin(0, x.num.toInt()) }, p, v)))
                read(when {
                    rk < need - 1 -> "Vektory bez parametra majú len §$rk§ hlavné stĺpce, ani s posledným vektorom ich nebude §$need§."
                    rk >= need -> "Vektory bez parametra majú §$rk§ hlavné stĺpce, teda generujú §\\mathbb{Z}_{$p}^{3}§ bez ohľadu na §$v§."
                    else -> "Rozhoduje posledný stĺpec v riadkoch, kde je vľavo nula: " + deciding.joinToString(", ") { "§${it.tex(v)}§" } + ". " + paramZpRead(kind)
                })
                each("Hodnôt §$v§ je len §$p§, dosadím každú.", (0 until p).map { a ->
                    val shown = if (rk == need - 1) ":\\ " + deciding.joinToString(",\\ ") { "${it.tex(v)}=${it.at(a, p)}" } else ""
                    Formula("$v=$a$shown", if (a in truth) prop else propNot)
                })
                end(summary)
            } },
            hint = m("Vektory do stĺpcov, parameter do posledného. Po GEM pozri riadky, kde je vľavo nula: kedy je číslo s parametrom nenulové v §\\mathbb{Z}_p§? Dosaď všetky hodnoty, je ich len §p§."),
        )
    }
}

/* ---- 4. Coordinates of a combination, without knowing the basis --------------------------------------------- */

private const val SrcCoordsLc = "Otázka v skriptách pri Definícii 2.68; test 15. 11. 2022 (variant 9A), príklad 3"

val CoordsOfCombinationPostup = postup(
    "Čo vieme?" to "Súradnice sú koeficienty pri vektoroch bázy v jej poradí.",
    "Dosaď a zoskup" to "Pri každom vektore bázy sčítaj koeficienty; súradnice kombinácie sú tá istá kombinácia súradníc.",
    "Záver" to "Súradnice sú jediné (báza je LN).",
)

/** What is asked: a combination of the basis vectors in another order, or of two vectors with known coordinates. */
internal sealed interface CoordsLcCase {
    /** B = (x, y, z) a basis of R^3; the vector Σ k_i (x, y, z)_i, coordinates with respect to the basis in [order]. */
    data class Named(val k: Vec, val order: List<Int>) : CoordsLcCase { val answer get() = order.map { k[it] } }
    /** B a basis of P ⊂⊂ F^n, dim P = d; (v)_B = cv, (w)_B = cw; coordinates of a·v + b·w. */
    data class Given(val f: Field, val n: Int, val cv: Vec, val cw: Vec, val a: Frac, val b: Frac) : CoordsLcCase {
        val answer get() = cv.indices.map { f.add(f.mul(a, cv[it]), f.mul(b, cw[it])) }
    }
}

internal fun coordsLcCase(r: Random): CoordsLcCase {
    if (r.nextInt(3) == 0) while (true) {
        val k = List(3) { r.pick(-2, 3).q() }
        if (k.count { !it.isZero } < 2) continue
        return CoordsLcCase.Named(k, if (r.nextBoolean()) listOf(0, 1, 2) else listOf(0, 1, 2).shuffled(r))
    }
    val f: Field = if (r.nextBoolean()) Field.R else Field.Zp(listOf(5, 7).random(r))
    val p = f.pp()
    while (true) {
        val d = r.pick(2, 3)
        val n = d + r.pick(0, 2)
        val cv = List(d) { if (p != null) Frac.of(r.pick(0, p - 1)) else r.pick(-2, 2).q() }
        val cw = List(d) { if (p != null) Frac.of(r.pick(0, p - 1)) else r.pick(-2, 2).q() }
        if (cv.isZero() || cw.isZero() || cv == cw) continue
        val a = if (p != null) Frac.of(r.pick(1, p - 1)) else r.nonzero(-2, 3).q()
        val b = if (p != null) Frac.of(r.pick(1, p - 1)) else r.nonzero(-2, 3).q()
        if (a == Frac.ONE && b == Frac.ONE && r.nextBoolean()) continue
        return CoordsLcCase.Given(f, n, cv, cw, a, b)
    }
}

fun coordsLcExercise(r: Random): Exercise = coordsLcExercise(coordsLcCase(r))

internal fun coordsLcExercise(case: CoordsLcCase): Exercise = when (case) {
    is CoordsLcCase.Named -> {
        val names = listOf("\\mathbf{x}", "\\mathbf{y}", "\\mathbf{z}")
        val (k, order) = case
        val expr = comboTexF(k, names)
        val basis = "(" + order.joinToString(",") { names[it] } + ")"
        val ans = case.answer
        val text = m("Nechť §\\mathcal{B}=(\\mathbf{x},\\mathbf{y},\\mathbf{z})§ je báze §\\mathbb{R}^3§. Jaké souřadnice má vektor §$expr§ vzhledem k bázi §$basis§?")
        Exercise("suradnice-lk", "Súradnice kombinácie", SrcCoordsLc, text,
            parts = listOf({ _ -> Part(m("Súradnice:"), Answer.Numbers(listOf("\\alpha_1", "\\alpha_2", "\\alpha_3"), null, ans, "$${ans.tex()}$", fractions = true, diagnose = diagnosis(Field.R, ans, listOf(
                (if (order != listOf(0, 1, 2)) k else null) to "To sú koeficienty v poradí §(\\mathbf{x},\\mathbf{y},\\mathbf{z})§, ale báza je §$basis§. Súradnice idú v poradí vektorov bázy.",
            )) { given -> "Tieto súradnice nesedia. Súradnice sú koeficienty pri vektoroch bázy v jej poradí: pri " + order.joinToString(", ") { "§${names[it]}§" } + " stoja " + ans.joinToString(", ") { "§${it.tex(true)}§" } + ". Tvoje §${given.tex()}§ dáva iný vektor." })) }),
            postup = CoordsOfCombinationPostup,
            solution = { CoordsOfCombinationPostup.solve {
                val (known, group, end) = CoordsOfCombinationPostup
                known("Súradnice sú koeficienty kombinácie vektorov bázy v jej poradí (Definícia 2.68); vektor, ktorý vo výraze chýba, má koeficient §0§.")
                group("Prepíšem výraz s vektormi bázy v poradí §$basis§.", "$expr=" + comboTexF(ans, order.map { names[it] }))
                end("Súradnice sú jediné, lebo báza je LN (Veta 2.66).", "($expr)_{$basis}=${ans.tex()}")
            } },
            hint = "Koeficient pri každom vektore bázy, v poradí bázy. Chýbajúci vektor má nulu.")
    }
    is CoordsLcCase.Given -> {
        val (f, n, cv, cw, a, b) = case
        val p = f.pp()
        val d = cv.size
        val ans = case.answer
        val expr = comboTexF(listOf(a, b), listOf("\\mathbf{v}", "\\mathbf{w}"))
        val sp = fieldPow(f, n)
        val text = m("Nechť §\\mathcal{B}§ je báze podprostoru §P\\ssub $sp§ a §\\dim P=$d§. Vektor §\\mathbf{v}§ má vzhledem k bázi §\\mathcal{B}§ souřadnice §${cv.tex()}§ a vektor §\\mathbf{w}§ souřadnice §${cw.tex()}§. Jaké souřadnice má vektor §$expr§?")
        val labels = List(d) { "\\alpha_${it + 1}" }
        val forgotA = cv.indices.map { f.add(cv[it], f.mul(b, cw[it])) }
        val forgotB = cv.indices.map { f.add(f.mul(a, cv[it]), cw[it]) }
        val plain = cv.indices.map { f.add(cv[it], cw[it]) }
        val bs = (1..d).map { "\\mathbf{b}_{$it}" }
        Exercise("suradnice-lk", "Súradnice kombinácie", SrcCoordsLc, text,
            parts = listOf({ _ -> Part(m("Súradnice §$expr§ vzhľadom na §\\mathcal{B}§:"), Answer.Numbers(labels, p, ans, "$${ans.tex()}$", fractions = p == null, diagnose = diagnosis(f, ans, listOf(
                forgotA to "Koeficient pri §\\mathbf{v}§ si vynechal. Súradnice kombinácie sú tá istá kombinácia súradníc: §${Terms.lin(listOf(a, b), listOf("(\\mathbf{v})_{\\mathcal{B}}", "(\\mathbf{w})_{\\mathcal{B}}"))}§.",
                forgotB to "Koeficient pri §\\mathbf{w}§ si vynechal. Súradnice kombinácie sú tá istá kombinácia súradníc: §${Terms.lin(listOf(a, b), listOf("(\\mathbf{v})_{\\mathcal{B}}", "(\\mathbf{w})_{\\mathcal{B}}"))}§.",
                plain to "Sčítal si súradnice bez koeficientov. Každú súradnicu treba vynásobiť koeficientom pri svojom vektore.",
            )) { given -> "Tieto súradnice nesedia. Súradnice kombinácie sú tá istá kombinácia súradníc, po zložkách: §${Terms.lin(listOf(a, b), listOf(cv.tex(), cw.tex()))}=${ans.tex()}§" + (if (p != null) " v §\\mathbb{Z}_{$p}§" else "") + ". Tvoje §${given.tex()}§ to nie je." })) }),
            postup = CoordsOfCombinationPostup,
            solution = { CoordsOfCombinationPostup.solve {
                val (known, group, end) = CoordsOfCombinationPostup
                known("Zo súradníc poznám §\\mathbf{v}§ aj §\\mathbf{w}§ ako kombinácie bázy §\\mathcal{B}=(${names("b", d)})§; samotné vektory bázy netreba (Definícia 2.68).",
                    Formula("\\mathbf{v}=" + comboTexF(cv, bs)), Formula("\\mathbf{w}=" + comboTexF(cw, bs)))
                group("Dosadím a zoskupím pri každom §\\mathbf{b}_i§: koeficient je tá istá kombinácia §i§-tých súradníc" + (if (p != null) ", počítaná v §\\mathbb{Z}_{$p}§." else "."),
                    Formula("$expr=" + comboTexF(ans, bs).let { if (it == "0") "\\theta" else it }))
                end("Súradnice sú jediné, lebo báza je LN (Veta 2.66).", "($expr)_{\\mathcal{B}}=${ans.tex()}")
            } },
            hint = "Súradnice kombinácie sú tá istá kombinácia súradníc. Bázu netreba poznať.")
    }
}

/* ---- 5. How many combinations of a set give a vector -------------------------------------------------------- */

private const val SrcLcWays = "Test 15. 11. 2022 (variant 9A), príklad 4"

/** Number of coefficient tuples with Σ c_i z_i = v over Z_p: p^(m-h) when solvable (h = rank), else 0. */
fun lcWays(f: Field.Zp, zs: List<Vec>, v: Vec): Long = if (f.inSpan(zs, v)) pow(f.p, zs.size - f.rank(zs)) else 0L

private fun freeWord(k: Int) = when (k) { 1 -> "voľnú neznámu"; in 2..4 -> "voľné neznáme"; else -> "voľných neznámych" }

private fun lcWaysDiagnosis(p: Int, m: Int, h: Int, ans: Long): (List<Frac>) -> String =
    if (ans == 0L) { _ -> "Zadaný vektor v obale nie je, preto ho nedá žiadna kombinácia. Po GEM je pravá strana hlavný stĺpec: sústava pre koeficienty nemá riešenie." }
    else diagnosis(Field.R, n1(ans), listOf(
        n1(1) to "Tento počet nesedí. Jediný spôsob je len pri LN súbore. Tento súbor má hodnosť §$h§ a §$m§ ${pl(m.toLong(), "vektor", "vektory", "vektorov")}, sústava má §${m - h}§ ${freeWord(m - h)}.",
        n1(pow(p, m)) to "Tento počet nesedí. §${pw(p, m)}§ je počet všetkých §$m§-tíc koeficientov, ale nie každá dá zadaný vektor. Rátajú sa len riešenia sústavy.",
        n1(pow(p, h)) to "Tento počet nesedí. §${pw(p, h)}§ je počet prvkov obalu. Rátajú sa koeficienty, ktoré dajú jeden konkrétny vektor: §${pw(p, m - h)}§, po jednej voľbe pre každú voľnú neznámu.",
        n1(0) to "Tento počet nesedí. Zadaný vektor v obale je: po GEM pravá strana nie je hlavný stĺpec, sústava pre koeficienty riešenie má.",
    )) { _ -> "Tento počet nesedí. " + if (m == h) "Súbor je LN, takže koeficienty sú jednoznačné: vektor v obale dá práve jedna kombinácia." else "Spôsobov je toľko, koľko riešení má sústava: §${m - h}§ ${freeWord(m - h).replace("voľnú neznámu", "voľná neznáma")}, každá s §$p§ hodnotami, teda §${pw(p, m - h)}=$ans§." }

val LcWaysPostup = postup(
    "Čo počítam?" to "Usporiadané §m§-tice koeficientov, ktoré dajú zadaný vektor.",
    "Sústava pre koeficienty" to "Vektory do stĺpcov, zadaný vektor ako pravá strana, GEM.",
    "Má riešenie?" to "Ak je pravá strana hlavný stĺpec, nie: žiadny spôsob.",
    "Koľko riešení?" to "Každá voľná neznáma má §p§ hodnôt: §p^{m-h}§.",
)

fun lcCountExercise(r: Random): Exercise {
    val p = listOf(2, 3, 3, 5).random(r)
    val f = Field.Zp(p)
    while (true) {
        val n = r.pick(2, 3)
        val m = r.pick(2, 4)
        val h = r.pick(1, minOf(m, n))
        // m vectors of rank h: h random LN ones and combinations of them
        val basis = List(h) { f.drawVec(r, n) }
        if (!f.isLN(basis)) continue
        val zs = (basis + List(m - h) { f.combo(List(h) { Frac.of(r.pick(0, p - 1)) }, basis) }).shuffled(r)
        if (zs.any { it.isZero() } && r.nextInt(3) > 0) continue
        val inside = h == n || r.nextInt(10) < 7
        val v = if (inside) f.combo(List(h) { Frac.of(r.pick(0, p - 1)) }, basis) else f.drawVec(r, n)
        if (f.inSpan(zs, v) != inside || (v.isZero() && r.nextBoolean())) continue
        val ans = lcWays(f, zs, v)
        val sp = "\\mathbb{Z}_{$p}^{$n}"
        val text = m("Kolika různými způsoby lze vytvořit vektor §${v.tex()}§ jako LK souboru §${vstex(zs)}§ vektorů z §$sp§?")
        val e = f.echelon(Mat.columns(zs, listOf(v)), m)
        return Exercise("lk-pocet", "Koľko kombinácií dá vektor", SrcLcWays, text,
            parts = listOf({ _ -> Part(m("Počet rôznych LK:"), Answer.Numbers(listOf("\\#"), null, n1(ans), "$$ans$", diagnose = lcWaysDiagnosis(p, m, f.rank(zs), ans))) }),
            postup = LcWaysPostup,
            solution = { LcWaysPostup.solve {
                val (what, gem, solvable, count) = LcWaysPostup
                what("Rátam usporiadané §$m§-tice koeficientov so §\\sum\\alpha_i\\mathbf{z}_i=${v.tex()}§: dve LK sú rôzne, keď sa líšia koeficientmi.")
                gem("Sú to riešenia sústavy so stĺpcami §\\mathbf{z}_i§ a pravou stranou §${v.tex()}§; upravím ju GEM v §\\mathbb{Z}_{$p}§.", gemLines(e))
                val hh = e.pivotsLeft.size
                if (!e.consistent) {
                    solvable("Pravá strana je hlavný stĺpec, sústava nemá riešenie: vektor v obale nie je.")
                    count("Žiadna kombinácia ho nedá.", "0")
                } else {
                    solvable("Pravá strana nie je hlavný stĺpec, riešenie existuje. Hlavných stĺpcov je §$hh§, voľných neznámych §${m - hh}§.")
                    if (hh == m) count("Voľné neznáme nie sú, riešenie je jediné.", "1")
                    else count("Každá voľná neznáma nadobudne §$p§ ${pl(p.toLong(), "hodnotu", "hodnoty", "hodnôt")} a hlavné sa z nich dopočítajú jednoznačne.", "${pw(p, m - hh)}=$ans")
                }
            } },
            hint = m("Spôsoby sú riešenia sústavy pre koeficienty. Každá voľná neznáma má §p§ hodnôt."))
    }
}

/* ---- 6. Four vectors with given bases among their triples, then the number of combinations -------------------- */

private const val SrcSetBases = "Test 15. 11. 2022 (variant 9A), príklad 4"

private val Triples = listOf(listOf(0, 1, 2), listOf(0, 1, 3), listOf(0, 2, 3), listOf(1, 2, 3))
private fun trTex(t: List<Int>) = "(" + t.joinToString(",") { "\\mathbf{z}_{${it + 1}}" } + ")"

val SetBasesPostup = postup(
    "Ktorý vektor je kombináciou?" to "Trojica bez §\\mathbf{z}_j§ je báza, keď má §\\mathbf{z}_j§ v závislosti nenulový koeficient.",
    "Návrh" to "Trojicu-bázu zvoľ LN, štvrtý vektor ako kombináciu tých, ktoré ho môžu nahradiť.",
    "Overenie" to "Každú trojicu over GEM: LN je báza, LZ nie.",
    "Koľko LK?" to "Sústava pre štyri koeficienty s hodnosťou §3§ má §p§ riešení.",
)

fun setBasesExercise(r: Random): Exercise {
    val p = listOf(2, 3, 3, 5).random(r)
    val f = Field.Zp(p)
    while (true) {
        // the triple without z_j0 is a basis; z_j0 is a combination of those in [support], nonzero coefficients
        val j0 = r.nextInt(4)
        val others = (0 until 4).filter { it != j0 }
        val size = listOf(1, 1, 2, 2, 0).random(r)
        val support = others.shuffled(r).take(size)
        val b = List(3) { f.drawVec(r, 3) }
        if (!f.isLN(b)) continue
        val z = MutableList<Vec>(4) { List(3) { Frac.ZERO } }
        others.forEachIndexed { i, j -> z[j] = b[i] }
        z[j0] = support.fold(List(3) { Frac.ZERO }) { acc, j -> f.plus(acc, f.scale(Frac.of(r.pick(1, p - 1)), z[j])) }
        val isBasis = Triples.map { t -> f.rank(t.map { z[it] }) == 3 }
        val yes = Triples.indices.filter { isBasis[it] }; val no = Triples.indices.filter { !isBasis[it] }
        if (no.isEmpty()) continue
        val yesTex = yes.map { "§${trTex(Triples[it])}§" }.let { l -> if (l.size == 1) "${l[0]} je báze" else l.dropLast(1).joinToString(", ") + " a ${l.last()} jsou báze" }
        val noTex = no.map { "§${trTex(Triples[it])}§" }.let { l -> if (l.size == 1) "${l[0]} není" else l.dropLast(1).joinToString(", ") + " ani ${l.last()} nejsou" }
        val sp = "\\mathbb{Z}_{$p}^{3}"
        val target = f.drawVec(r, 3).let { if (it.isZero()) List(3) { Frac.ONE } else it }
        val text = m("Najděte soubor čtyř vektorů §(\\mathbf{z}_1,\\mathbf{z}_2,\\mathbf{z}_3,\\mathbf{z}_4)§ z prostoru §$sp§ takový, že $yesTex, $noTex. Kolika různými způsoby lze vytvořit vektor §${target.tex()}§ jako LK souboru §(\\mathbf{z}_1,\\mathbf{z}_2,\\mathbf{z}_3,\\mathbf{z}_4)§?")
        val check = { e: List<List<Vec>> ->
            val zz = e.map { it[0].map(f::norm) }
            val bad = Triples.indices.filter { (f.rank(Triples[it].map { i -> zz[i] }) == 3) != isBasis[it] }
            if (bad.isEmpty()) VectorsResult(true, m("Správne, každá trojica je báza práve vtedy, keď má byť. Tri LN vektory v §$sp§ sú báza (Veta 2.55), tri LZ nie."), zz.map { null })
            else VectorsResult(false, m((if (bad.size == 1) "Jedna podmienka nesedí." else "Nesedia §${bad.size}§ podmienky.") + " " + bad.joinToString(" ") { k ->
                val tt = Triples[k].map { zz[it] }
                if (isBasis[k]) "§${trTex(Triples[k])}§ má byť báza, ale je LZ: " + (f.dependency(tt)?.let { dd -> "§" + comboTexF(dd, Triples[k].map { "\\mathbf{z}_{${it + 1}}" }) + "=\\theta§." } ?: "")
                else "§${trTex(Triples[k])}§ nemá byť báza, ale je LN, a tri LN vektory v §$sp§ bázu tvoria."
            }), zz.map { null })
        }
        return Exercise("soubor-baze", "Súbor so zadanými bázami", SrcSetBases, text,
            parts = listOf(
                { _ -> Part(m("Napíš súbor štyroch vektorov §\\mathbf{z}_1,\\mathbf{z}_2,\\mathbf{z}_3,\\mathbf{z}_4§ v tomto poradí."), Answer.Vectors(cells(3), p, 4..4, entry = "vektor §\\mathbf{z}_i§".replace('§', '$'), check = check, example = z.map { listOf(it) }),
                    hint = m("Trojicu, ktorá má byť bázou, zvoľ LN (napríklad §\\mathbf{e}_1,\\mathbf{e}_2,\\mathbf{e}_3§). Štvrtý vektor sprav ako kombináciu tých, s ktorými ho trojice-bázy môžu vymeniť."), aim = 1) },
                { prev ->
                    @Suppress("UNCHECKED_CAST") val mine = (prev.getOrNull(0) as? List<List<Vec>>)?.map { it[0] } ?: z
                    val h = f.rank(mine); val ans = lcWays(f, mine, target)
                    Part(m("Koľkými rôznymi spôsobmi sa dá vektor §${target.tex()}§ zapísať ako LK tvojho súboru?"), Answer.Numbers(listOf("\\#"), null, n1(ans), "$$p^{4-$h}=$ans$", diagnose = lcWaysDiagnosis(p, 4, h, ans)),
                        hint = m("Je to sústava pre štyri koeficienty: tvoje vektory do stĺpcov, zadaný vektor ako pravá strana, GEM. Každá voľná neznáma má §p§ hodnôt."), aim = 3)
                },
            ),
            postup = SetBasesPostup,
            solution = { prev ->
                @Suppress("UNCHECKED_CAST") val mine = (prev.getOrNull(0) as? List<List<Vec>>)?.map { it[0] } ?: z
                SetBasesPostup.solve { setBasesSteps(f, z, j0, mine, target) }
            },
            hint = m("Zvoľ trojicu, ktorá má byť bázou, LN (napríklad §\\mathbf{e}_1,\\mathbf{e}_2,\\mathbf{e}_3§). Štvrtý vektor sprav ako kombináciu tých, s ktorými ho trojice-bázy môžu vymeniť."),
        )
    }
}

/** The set [z] whose triple without z_[j0] is a basis and z_[j0] a combination of the others; the count for [mine]. */
fun SolutionScope.setBasesSteps(f: Field.Zp, z: List<Vec>, j0: Int, mine: List<Vec>, target: Vec) {
    val (which, propose, check, count) = SetBasesPostup
    val p = f.p; val sp = "\\mathbb{Z}_{$p}^{3}"
    val zn = (1..4).map { "\\mathbf{z}_{$it}" }
    val others = (0 until 4).filter { it != j0 }
    val yes = Triples.indices.filter { k -> f.rank(Triples[k].map { z[it] }) == 3 }
    which("Štyri vektory v §$sp§ sú LZ; keď je niektorá trojica báza, každá ich závislosť je násobkom jednej. Trojica bez §\\mathbf{z}_j§ je báza práve vtedy, keď má §\\mathbf{z}_j§ v tejto závislosti nenulový koeficient.",
        Formula("\\text{bázy: bez }" + yes.map { k -> (0 until 4).first { it !in Triples[k] } }.joinToString(",\\ ") { zn[it] }))
    val coef = f.coords(others.map { z[it] }, z[j0])!!
    propose("Trojicu bez §${zn[j0]}§ zvolím LN a §${zn[j0]}§ ako kombináciu tých, s ktorými ho bázy môžu vymeniť.",
        others.map { Formula("${zn[it]}=${z[it].tex()}") } + Formula("${zn[j0]}=" + comboTexF(coef, others.map { zn[it] }).let { if (it == "0") "\\theta" else it } + "=${z[j0].tex()}"))
    check("Kde §${zn[j0]}§ nahradí vektor s nenulovým koeficientom, trojica generuje celé §$sp§ a je báza. Kde vektor s nulovým koeficientom, je §${zn[j0]}§ kombináciou zvyšných dvoch a trojica je LZ.")
    val h = f.rank(mine); val ans = lcWays(f, mine, target)
    count("Súbor obsahuje bázu §$sp§, preto má sústava pre koeficienty hodnosť §$h§ a riešenie. Voľná je jedna neznáma a každá jej hodnota zo §\\mathbb{Z}_{$p}§ dá jedno riešenie.", "$p^{4-$h}=$ans")
}

/* ---- 7. A system with two given solutions and LN rows ----------------------------------------------------------- */

private const val SrcSystem = "Test listopad 2022, príklad 5 (8 b), prvá fotografia"

val SystemPointsPostup = postup(
    "Čo musí platiť?" to "Ak §\\mathbf{u}§ aj §\\mathbf{w}§ riešia §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§, tak §\\mathbf{A}(\\mathbf{u}-\\mathbf{w})=\\theta§.",
    "Riadky §\\mathbf{A}§" to "Dva LN riadky §(a_1,\\dots,a_n)§ so §\\sum_j a_j(u_j-w_j)=0§.",
    "Pravá strana" to "§b_i§ je ľavá strana §i§-tej rovnice po dosadení §\\mathbf{u}§.",
    "Skúška" to "Dosaď §\\mathbf{w}§ a over LN riadkov.",
)

/** "a_1x_1+…" with the numbers of [a] and [x] where both are nonzero, then the sum and, over Z_p, its remainder. */
private fun rowTimesTex(f: Field, a: Vec, x: Vec): String {
    val idx = a.indices.filter { !a[it].isZero && !x[it].isZero }
    val raw = idx.fold(Frac.ZERO) { acc, i -> acc + a[i] * x[i] }
    val terms = if (idx.isEmpty()) "0" else idx.joinToString("+") { "${a[it].tex(true)}\\cdot ${x[it].tex(true)}" }
    val value = f.norm(raw)
    return when {
        idx.isEmpty() -> "0"
        raw == value -> "$terms=${value.tex(true)}"
        else -> "$terms=${raw.tex(true)}=${value.tex(true)}"
    }
}

/**
 * The solution of "a system with the solutions [u] and [w] and LN rows" for the chosen [rows] (two LN solutions of the
 * row equation). The lesson's worked example and the generator share it. No product of two vectors: the course does not
 * define one (Varování 2.5), so every row is written as its equation, component by component.
 */
fun SolutionScope.systemPointsSteps(f: Field, u: Vec, w: Vec, rows: List<Vec>) {
    val (must, rowsAim, rhs, test) = SystemPointsPostup
    val n = u.size
    val zp = if (f is Field.Zp) "\\mathbb{Z}_{${f.p}}" else "\\mathbb{R}"
    val dv = u.indices.map { f.sub(u[it], w[it]) }
    fun value(a: Vec, x: Vec) = f.norm(a.indices.fold(Frac.ZERO) { acc, i -> acc + a[i] * x[i] })
    must("Označím dané riešenia §\\mathbf{u}=${u.tex()}§ a §\\mathbf{w}=${w.tex()}§. Riadok §(a_1,\\dots,a_$n)§ s pravou stranou §b§ je rovnica, ktorú spĺňa §\\mathbf{u}§ aj §\\mathbf{w}§. Odčítaním oboch dosadení §b§ zmizne:",
        Formula("\\sum_{j=1}^{$n}a_jx_j=b"), Formula("\\sum_{j=1}^{$n}a_j(u_j-w_j)=0"), Formula("\\mathbf{u}-\\mathbf{w}=${u.tex()}-${w.tex()}=${dv.tex()}", "v §$zp§"))
    rowsAim("Rovnica pre riadky má §$n§ neznáme, jej riešenia tvoria podpriestor dimenzie §${n - 1}§; vyberiem z neho dva LN vektory.",
        listOf(Formula(comboTexF(dv, (1..n).map { "a_{$it}" }) + "=0")) + rows.map { Formula("(a_1,\\dots,a_$n)=${it.tex()}", "dosadené: §${rowTimesTex(f, it, dv)}§") })
    rhs("Pravá strana riadku je jeho ľavá strana po dosadení §\\mathbf{u}§.",
        rows.mapIndexed { i, a -> Formula("b_${i + 1}=${rowTimesTex(f, a, u)}") } + Formula("[A|\\mathbf{b}]=" + Mat(rows.map { a -> a + listOf(value(a, u)) }, n).tex()))
    test("Riadky nie sú násobkami jeden druhého, sú teda LN; a dosadenie §\\mathbf{w}§ dá tú istú pravú stranu.",
        rows.mapIndexed { i, a -> Formula("${rowTimesTex(f, a, w)}=b_${i + 1}") })
}

fun systemPointsExercise(r: Random): Exercise {
    val p = listOf(3, 3, 5).random(r)
    val f = Field.Zp(p)
    val n = 4
    while (true) {
        val u = f.drawVec(r, n); val w = f.drawVec(r, n)
        if (u == w || u.isZero() && w.isZero()) continue
        val dv = u.indices.map { f.sub(u[it], w[it]) }
        val j = dv.indexOfFirst { !it.isZero }
        // the rows a with a·(u-w) = 0: e_k - (d_k/d_j) e_j for k ≠ j, two of them
        val ks = (0 until n).filter { it != j }.shuffled(r).take(2).sorted()
        val rows = ks.map { k -> List(n) { i -> when (i) { k -> Frac.ONE; j -> f.neg(f.div(dv[k], dv[j])); else -> Frac.ZERO } } }
        fun dot(a: Vec, x: Vec) = a.indices.fold(Frac.ZERO) { acc, i -> f.add(acc, f.mul(a[i], x[i])) }
        val ex = rows.map { a -> listOf(a + listOf(dot(a, u))) }
        val zp = "\\mathbb{Z}_{$p}"
        val text = m("Najděte matici §\\mathbf{A}\\in $zp^{2,4}§ a vektor §\\mathbf{b}\\in $zp^{2}§ takové, že soustava §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§ má v množině řešení vektor §${u.tex()}§ i vektor §${w.tex()}§. Řádky matice §\\mathbf{A}§ chápané jako soubor vektorů ze §$zp^{4}§ musí být navíc lineárně nezávislé.")
        return Exercise("soustava-body", "Sústava so zadanými riešeniami", SrcSystem, text,
            parts = listOf({ _ ->
                Part(m("Napíš dva riadky §[\\mathbf{A}|\\mathbf{b}]§: štyri čísla riadku §\\mathbf{A}§ a jeho pravú stranu."),
                    Answer.Vectors(listOf("a_{i1}", "a_{i2}", "a_{i3}", "a_{i4}", "b_i"), p, 2..2, entry = "riadok", check = { e -> judgeSystemRows(f, u, w, e) }, example = ex))
            }),
            postup = SystemPointsPostup,
            solution = { SystemPointsPostup.solve { systemPointsSteps(f, u, w, rows) } },
            hint = m("Ak §\\mathbf{u}§ aj §\\mathbf{w}§ riešia §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§, tak §\\mathbf{A}(\\mathbf{u}-\\mathbf{w})=\\theta§. Riadky §\\mathbf{A}§ sú dva LN vektory §(a_1,\\dots,a_4)§ so §\\sum_j a_j(u_j-w_j)=0§, pravá strana §\\mathbf{b}=\\mathbf{A}\\mathbf{u}§."),
        )
    }
}

/* ---- 8. Every element of a finite span ---------------------------------------------------------------------- */

private const val SrcSpanList = "Test 22. 11. 2023 (variant Myš), príklad 3; Príklad v skriptách (kapitola 2, lineárny obal nad telesami); Krásenský, 5. prednáška"

fun spanListExercise(r: Random): Exercise {
    data class Shape(val p: Int, val n: Int, val k: Int, val rank: Int)
    val shape = listOf(Shape(2, 4, 2, 2), Shape(2, 4, 2, 2), Shape(2, 3, 3, 2), Shape(2, 4, 3, 2), Shape(3, 3, 1, 1), Shape(3, 4, 2, 2), Shape(3, 3, 2, 2), Shape(5, 3, 1, 1)).random(r)
    val (p, n, k, rk) = shape
    val f = Field.Zp(p)
    while (true) {
        val basis = List(rk) { f.drawVec(r, n) }
        if (!f.isLN(basis) || basis.any { v -> v.count { it.isZero } > n - 1 }) continue
        val gens = (basis + List(k - rk) { f.combo(List(rk) { Frac.ONE }, basis) }).shuffled(r)
        if (gens.toSet().size < gens.size) continue
        val all = f.spanElements(gens)
        val sorted = all.sortedBy { v -> v.fold(0L) { acc, x -> acc * p + x.num } }
        val sp = "\\mathbb{Z}_{$p}^{$n}"
        val text = m("Napište všechny prvky z §\\langle " + gens.joinToString(",") { it.tex() } + "\\rangle\\ssub $sp§.")
        return Exercise("obal-vypis", "Všetky prvky obalu", SrcSpanList, text,
            parts = listOf({ _ ->
                Part(m("Prvky obalu, každý raz:"), Answer.Vectors(cells(n), p, 1..(all.size + 4), entry = "prvok", check = { e -> judgeSpanList(f, gens, e) }, example = sorted.map { listOf(it) }))
            }),
            postup = SpanListPostup,
            solution = { SpanListPostup.solve { spanListSteps(f, gens) } },
            hint = m("Prejdi všetky kombinácie koeficientov zo §\\mathbb{Z}_p§. Nezabudni na nulové koeficienty (dajú §\\theta§) a opakovania vynechaj."),
        )
    }
}

/** Every element of ⟨[gens]⟩ over Z_p, all coefficient choices listed: the generator and the lesson's worked example. */
fun SolutionScope.spanListSteps(f: Field.Zp, gens: List<Vec>) {
    val (general, list) = SpanListPostup
    val p = f.p; val k = gens.size; val rk = f.rank(gens)
    val all = f.spanElements(gens)
    val sorted = all.sortedBy { v -> v.fold(0L) { acc, x -> acc * p + x.num } }
    val coef = (1..k).joinToString(",") { "\\alpha_{$it}" }
    // the general element written out by components too: (α₁, α₂, α₁+α₂, 0) as in Příklad 2.37
    val names = (1..k).map { "\\alpha_{$it}" }
    val byComponent = gens[0].indices.joinToString(",", "(", ")") { j -> Terms.lin(gens.map { it[j] }, names) }
    general("Prvok obalu je kombinácia generátorov s koeficientmi zo §\\mathbb{Z}_{$p}§ (Definícia 2.33); po zložkách ju sčítam.", Formula((1..k).joinToString("+") { "\\alpha_{$it}${gens[it - 1].tex()}" } + "\\softbreak[1]=" + byComponent, "§$coef\\in\\mathbb{Z}_{$p}§"))
    val combos = (0 until pow(p, k).toInt()).map { c -> var q = c; List(k) { val d = q % p; q /= p; Frac.of(d) } }
    list("Prejdem všetky voľby koeficientov" + (if (rk < k) "; generátory sú LZ, takže sa vektory opakujú a do množiny píšem každý raz." else "."),
        combos.map { c -> Formula("(" + c.joinToString(",") { it.tex() } + ")\\mapsto " + f.combo(c, gens).tex()) })
    list("Obal má dimenziu §$rk§, teda §${pw(p, rk)}=${all.size}§ ${pl(all.size.toLong(), "prvok", "prvky", "prvkov")} (Poznámka 2.67).", Formula("\\{" + sorted.joinToString(",") { it.tex() } + "\\}"))
}

/* ---- 9. Every subspace of a small Z_p^n, written by the student --------------------------------------------- */

private const val SrcSubspaceList = "MARAST #13691 (Ukázková druhá zápočtová písemka, príklad 3); MARAST blog 45 (T. Kalvoda)"

fun subspaceListExercise(r: Random): Exercise {
    data class Ask(val p: Int, val n: Int, val d: Int)
    val ask = listOf(Ask(3, 2, 1), Ask(3, 2, 1), Ask(2, 2, 1), Ask(5, 2, 1), Ask(7, 2, 1), Ask(2, 3, 1), Ask(2, 3, 2)).random(r)
    val s = FiniteSpace(ask.p, ask.n)
    val f = Field.Zp(ask.p)
    val subs = s.subspaces(ask.d)
    val sp = s.spaceTex()
    val text = m(if (ask.n == 2) "Najděte všechny netriviální podprostory prostoru §$sp§." else "Najděte všechny podprostory dimenze §${ask.d}§ prostoru §$sp§.")
    // the example: each subspace by a random basis of it, so a test never relies on the canonical one
    val example = subs.map { sub -> randomBasisOf(s, sub, r).map { it.frac() } }
    val q = if (ask.d == 1) "Každý podpriestor zapíš jedným nenulovým vektorom, ktorý ho generuje." else "Každú rovinu zapíš dvoma LN vektormi, ktoré ju generujú."
    return Exercise("zp-zoznam", "Všetky podpriestory, vlastný zoznam", SrcSubspaceList, text,
        parts = listOf({ _ -> Part(m(q), Answer.Vectors(cells(ask.n), ask.p, 1..(subs.size + 3), group = ask.d, entry = if (ask.d == 1) "generátor" else "dvojicu generátorov, vektor po vektore", check = { e -> judgeSubspaceList(s, ask.d, e) }, example = example)) }),
        postup = ZpListPostup,
        solution = { subspaceListSolution(s, ask.d) },
        hint = if (ask.d == 1) "Prejdi nenulové vektory po poradí. Každý vygeneruje priamku; ak už je v zozname (iný generátor, tá istá množina), preskoč ho."
            else "Prejdi LN dvojice vektorov. Každá generuje rovinu; dvojice s rovnakým obalom zapíš iba raz.",
    )
}

private fun randomBasisOf(s: FiniteSpace, sub: Subspace, r: Random): List<ZVec> {
    while (true) {
        val pick = List(sub.dim) { sub.elements.random(r) }
        if (s.rank(pick) == sub.dim) return pick
    }
}

val ConstructGenerators: Map<String, (Random) -> Exercise> = mapOf(
    "baza-ina" to ::otherBasisExercise,
    "baza-zo-suradnic" to ::basisFromCoordsExercise,
    "param-zp" to ::paramZpExercise,
    "suradnice-lk" to ::coordsLcExercise,
    "lk-pocet" to ::lcCountExercise,
    "soubor-baze" to ::setBasesExercise,
    "soustava-body" to ::systemPointsExercise,
    "obal-vypis" to ::spanListExercise,
    "zp-zoznam" to ::subspaceListExercise,
)

package dev.hamper.tabula.data

import dev.hamper.tabula.la.*

/*
 * Feedback for a wrong number in a generated exercise: the likely mistake when the answer is exactly what that
 * mistake gives, else the step to check. Every candidate is computed by the same exact code that made the exercise,
 * so a message never names a mistake the student did not make (a candidate equal to the right answer is skipped).
 */

private fun same(f: Field, a: List<Frac>, b: List<Frac>) = a.size == b.size && a.indices.all { f.norm(a[it]) == f.norm(b[it]) }

/** The first candidate the answer equals names the mistake; otherwise [fallback] says what to check. */
fun diagnosis(f: Field, expected: List<Frac>, candidates: List<Pair<List<Frac>?, String>>, fallback: (List<Frac>) -> String): (List<Frac>) -> String = { given ->
    candidates.firstOrNull { (c, _) -> c != null && !same(f, c, expected) && same(f, c, given) }?.second ?: fallback(given)
}

private fun one(n: Long) = listOf(Frac.of(n))
private fun par(x: Frac) = if (x < Frac.ZERO) "(" + x.tex(true) + ")" else x.tex(true)
private fun mod(f: Field) = (f as? Field.Zp)?.let { "\\pmod{${it.p}}" } ?: ""

/** "k_1\cdot v_1+k_2\cdot v_2=raw" and, in Z_p, "\equiv value (mod p)". */
private fun sumTex(f: Field, ks: List<Frac>, vs: List<Frac>): String {
    val raw = ks.indices.fold(Frac.ZERO) { acc, i -> acc + ks[i] * vs[i] }
    val terms = ks.indices.joinToString("+") { "${par(ks[it])}\\cdot ${par(vs[it])}" }
    return if (f is Field.Zp && f.norm(raw) != raw) "$terms=${raw.tex(true)}\\equiv ${f.norm(raw).tex(true)}${mod(f)}" else "$terms=${f.norm(raw).tex(true)}"
}

private fun firstWrong(f: Field, given: List<Frac>, expected: List<Frac>) = given.indices.firstOrNull { f.norm(given[it]) != f.norm(expected[it]) } ?: 0

/** a·x + b·y in T^n, component by component (Definícia 2.2). */
fun arithDiagnosis(f: Field, a: Frac, x: Vec, b: Frac, y: Vec, expected: Vec): (List<Frac>) -> String {
    val signs = buildList {
        if (f is Field.R && b < Frac.ZERO) add(f.plus(f.scale(a, x), f.scale(-b, y)) to "Znamienko: koeficient §${b.tex(true)}§ násobí druhý vektor aj so znamienkom, takže jeho zložky odčítaš.")
        if (f is Field.R && a < Frac.ZERO) add(f.plus(f.scale(-a, x), f.scale(b, y)) to "Znamienko: koeficient §${a.tex(true)}§ obráti znamienko každej nenulovej zložky prvého vektora. Prvý vektor násobí aj so znamienkom.")
    }
    return diagnosis(f, expected, signs + listOf(
        // with a coefficient 1 the "one vector not scaled" mistake is the same as forgetting both: the specific message wins
        (if (a != Frac.ONE) f.plus(x, f.scale(b, y)) else null) to "Prvý vektor si nevynásobil jeho koeficientom §${a.tex(true)}§. Skalár násobí každú zložku svojho vektora (Definícia 2.2).",
        (if (b != Frac.ONE) f.plus(f.scale(a, x), y) else null) to "Druhý vektor si nevynásobil jeho koeficientom §${b.tex(true)}§. Skalár násobí každú zložku svojho vektora (Definícia 2.2).",
        f.plus(x, y) to "Zabudol si na koeficienty: skalár najprv násobí každú zložku svojho vektora, až potom sa sčítava. Pozri Definíciu 2.2.",
        expected.map { f.neg(it) } to "Vyšlo ti všetko s opačným znamienkom. Skontroluj znamienka koeficientov.",
    )) { given ->
        val i = firstWrong(f, given, expected)
        val flipped = f is Field.R && (given[i] == a * x[i] - b * y[i] || given[i] == -(a * x[i]) + b * y[i])
        (if (flipped) "Znamienko v zložke §x_{${i + 1}}§: " else "Nesedí zložka §x_{${i + 1}}§: ") + "§${sumTex(f, listOf(a, b), listOf(x[i], y[i]))}§."
    }
}

/** From coordinates [c] to the vector Σ c_i b_i (Definícia 2.68). */
fun vectorFromCoordsDiagnosis(f: Field, b: List<Vec>, c: Vec, expected: Vec): (List<Frac>) -> String {
    val n = expected.size
    // x_k = Σ_i c_i (b_i)_k; with the basis in the rows it would be Σ_i c_i (b_k)_i
    val transposed = if (b.size == n) List(n) { k -> f.norm(c.indices.fold(Frac.ZERO) { acc, i -> acc + c[i] * b[k][i] }) } else null
    val absC = if (f is Field.R && c.any { it < Frac.ZERO }) f.fromCoords(b, c.map { if (it < Frac.ZERO) -it else it }) else null
    return diagnosis(f, expected, listOf(
        c to "Zamenil si súradnice a vektor. Súradnice sú koeficienty, vektor je §c_1\\mathbf{b}_1+\\dots+c_${b.size}\\mathbf{b}_${b.size}§; v štandardnej báze sa každý vektor zhoduje so svojimi súradnicami, v inej báze vo všeobecnosti nie.",
        transposed to "Vektory bázy si použil ako riadky. §k§-tá zložka vektora je §c_1(\\mathbf{b}_1)_k+\\dots+c_${b.size}(\\mathbf{b}_${b.size})_k§: z každého vektora bázy jeho §k§-tá zložka.",
        (if (b.size == n) f.coords(b, c) else null) to "Počítal si opačným smerom: riešil si sústavu, akoby súradnice boli vektor. Zo súradníc sa vektor dostane dosadením do §\\sum c_i\\mathbf{b}_i§.",
        absC to "Znamienko: záporná súradnica znamená, že príslušný vektor bázy odčítaš.",
    )) { given ->
        val k = firstWrong(f, given, expected)
        // the sign of a negative coordinate lost in this component only
        val flipped = f is Field.R && c.any { it < Frac.ZERO } && given[k] == c.indices.fold(Frac.ZERO) { acc, i -> acc + (if (c[i] < Frac.ZERO) -c[i] else c[i]) * b[i][k] }
        (if (flipped) "Znamienko v zložke §x_{${k + 1}}§: záporná súradnica znamená, že príslušný vektor bázy odčítaš. " else "Nesedí zložka §x_{${k + 1}}§: ") + "§${sumTex(f, c, b.map { it[k] })}§."
    }
}

/** From a vector [w] to its coordinates in the basis [b]: the system with the basis in the columns (Definícia 2.68). */
fun coordsDiagnosis(f: Field, b: List<Vec>, w: Vec, expected: Vec): (List<Frac>) -> String {
    val square = b.size == w.size
    val rows = if (square) f.solve(List(w.size) { j -> b.map { it[j] } }, w) else null
    return diagnosis(f, expected, listOf(
        (if (square) w else null) to "Zamenil si vektor a súradnice. Súradnice sú koeficienty §\\alpha_i§, pre ktoré §\\sum\\alpha_i\\mathbf{b}_i§ dá ten vektor, nie jeho zložky.",
        (if (square) f.fromCoords(b, w) else null) to "Počítal si opačným smerom: zložky vektora si dosadil ako koeficienty. Súradnice dá sústava so stĺpcami z bázy a s vektorom na pravej strane.",
        rows to "Vektory bázy si dal do riadkov matice. Neznáme sú koeficienty pri vektoroch bázy, preto vektory bázy patria do stĺpcov.",
        expected.reversed() to "Súradnice si zapísal v opačnom poradí: §i§-tá súradnica patrí k §i§-tému vektoru bázy.",
    )) { given ->
        val back = f.fromCoords(b, given)
        "Skúška dosadením: s tvojimi číslami je §\\sum\\alpha_i\\mathbf{b}_i=${back.tex()}§, nie §${w.tex()}§. Skontroluj GEM so stĺpcami z bázy a spätné dosadenie" + (if (f is Field.Zp) ", všetko v §\\mathbb{Z}_{${f.p}}§." else ".")
    }
}

/** |P| = p^d for P ⊂⊂ Z_p^n of dimension d (Poznámka 2.67). */
fun sizeDiagnosis(p: Int, n: Int, d: Int): (List<Frac>) -> String = diagnosis(Field.R, one(pow(p, d)), listOf(
    one(p.toLong() * d) to "Násobil si §${p}\\cdot ${d}§. Prvok podpriestoru je daný §$d§-ticou súradníc a pri každej súradnici je počet možností §$p§; voľby sa násobia.",
    one(pow(d, p)) to "Zamenil si základ a exponent: základ je počet prvkov telesa §\\mathbb{Z}_{$p}§, exponent dimenzia.",
    one(pow(p, n)) to "§${Terms.pow(p.toLong(), n)}§ je počet prvkov celého §\\mathbb{Z}_{$p}^{$n}§. Podpriestor dimenzie §$d§ zodpovedá §$d§-ticiam súradníc, nie §$n§-ticiam.",
    one(pow(p, d) - 1) to "Nulový vektor do podpriestoru tiež patrí, nevynechávaj ho.",
)) { "Prvkov je §${Terms.pow(p.toLong(), d)}§: každý zodpovedá jedinej §$d§-tici súradníc zo §\\mathbb{Z}_{$p}§. Súradnice sú jednoznačné (Veta 2.66), počet dáva Poznámka 2.67." }

/** Nonzero y that make an LN set of m vectors from Z_p^n dependent: the nonzero elements of its span. */
fun lzCountDiagnosis(p: Int, n: Int, m: Int): (List<Frac>) -> String = diagnosis(Field.R, one(pow(p, m) - 1), listOf(
    one(pow(p, m)) to "Rátal si aj §\\mathbf{y}=\\theta§, zadanie chce nenulové §\\mathbf{y}§.",
    one(pow(p, n) - pow(p, m)) to "Toľko je vektorov mimo obalu, s nimi súbor ostane LN. Tak hovorí Veta 2.42; LZ ho urobia práve tie v obale.",
    one(pow(p, n) - 1) to "Nie každý nenulový §\\mathbf{y}§ urobí súbor LZ, len ten v obale daných vektorov. Pozri Vetu 2.39.",
    one(p.toLong() * m - 1) to "Obal §$m§ LN vektorov má §${Terms.pow(p.toLong(), m)}§ prvkov, nie §${p}\\cdot $m§. Každý prvok je daný §$m§-ticou koeficientov.",
    one(p.toLong() * m) to "Obal §$m§ LN vektorov má §${Terms.pow(p.toLong(), m)}§ prvkov, nie §${p}\\cdot $m§. Každý prvok je daný §$m§-ticou koeficientov.",
)) { "Súbor urobí LZ práve §\\mathbf{y}§ z obalu daných vektorov. Plynie to z Viet 2.39 a 2.42. Obal LN súboru §$m§ vektorov má §${Terms.pow(p.toLong(), m)}§ prvkov a jeden z nich je §\\theta§." }

/** Ordered bases of P ⊂⊂ Z_p^n of dimension d: Π (p^d − p^i), i = 0..d−1. */
fun basesDiagnosis(p: Int, n: Int, d: Int): (List<Frac>) -> String {
    val right = (0 until d).fold(1L) { acc, i -> acc * (pow(p, d) - pow(p, i)) }
    val fact = (1..d).fold(1L) { acc, i -> acc * i }
    return diagnosis(Field.R, one(right), listOf(
        one((0 until d).fold(1L) { acc, _ -> acc * (pow(p, d) - 1) }) to "Každý ďalší vektor musí byť mimo obalu predošlých, nestačí, že je nenulový. Len tak ostane súbor LN (Veta 2.42).",
        (if (right % fact == 0L) one(right / fact) else null) to "Báza je usporiadaný súbor: iné poradie tých istých vektorov je iná báza, preto sa §$d!§ nedelí.",
        one((0 until d).fold(1L) { acc, i -> acc * (pow(p, n) - pow(p, i)) }) to "Vektory vyberáš z podpriestoru, nie z celého §\\mathbb{Z}_{$p}^{$n}§: počítaj s §${Terms.pow(p.toLong(), d)}§ prvkami.",
        (if (d > 1) one((1 until d).fold(1L) { acc, i -> acc * (pow(p, d) - pow(p, i)) }) else null) to "Vynechal si prvý vektor: ten môže byť ľubovoľný nenulový, má §${Terms.pow(p.toLong(), d)}-1§ možností.",
    )) { "Vyberaj vektory po jednom, každý ďalší mimo obalu tých, čo už máš. Keď ich máš §i§, ten obal má §${p}^i§ prvkov. Možnosti sa násobia." }
}

/** dim ⟨v_1, …, v_m⟩ ⊂⊂ T^n is the number of pivot columns after GEM with the vectors in the columns. */
fun dimDiagnosis(m: Int, n: Int, d: Int): (List<Frac>) -> String = { given ->
    val g = given[0]
    when {
        g == Frac.of(m) && m != d -> "Spočítal si vektory, nie hlavné stĺpce. Daných §$m§ vektorov je LZ: niektorý je kombináciou ostatných a novú dimenziu nepridá."
        g == Frac.of(n) && n != d -> "§$n§ je dimenzia celého priestoru. Obal je celý priestor, len ak je po GEM §$n§ hlavných stĺpcov."
        g > Frac.of(n) -> "Podpriestor priestoru dimenzie §$n§ má dimenziu najviac §$n§. Každých §${n + 1}§ vektorov je tam LZ (Pozorovanie 2.32)."
        g == Frac.ZERO -> "Dimenziu §0§ má len §\\{\\theta\\}§, a tu je medzi vektormi nenulový. Pozri Vetu 2.53."
        g < Frac.of(d) -> "Hlavných stĺpcov je viac. Hlavný je každý stĺpec, v ktorom je prvý nenulový prvok niektorého riadku v HST; skontroluj, či si za taký prvok nevzal nulu."
        else -> "Hlavných stĺpcov je menej. Stĺpec bez schodu je vedľajší, aj keď sú v ňom nenulové čísla; skontroluj, či je pod prvým nenulovým prvkom každého riadku nula."
    }
}

/** The critical value of a parameter: where the deciding entry after GEM is zero. */
fun paramDiagnosis(c: Frac, deciders: List<Poly>): (List<Frac>) -> String {
    val d = deciders.filter { !it.isZero }
    val what = if (d.isEmpty()) "Výsledok sa mení len pre §a=${c.tex(true)}§." else if (d.size == 1) "Rozhodujúci prvok po GEM je §${d[0].tex()}§ a je nulový práve pre §a=${c.tex(true)}§." else "Rozhodujúce prvky po GEM sú §${d.joinToString(",\\ ") { it.tex() }}§; výsledok sa mení len pre §a=${c.tex(true)}§."
    return diagnosis(Field.R, listOf(c), listOf(
        listOf(-c) to "Znamienko: $what Pri prenášaní na druhú stranu rovnice sa znamienko mení.",
        (if (!c.isZero) listOf(Frac.ONE / c) else null) to "Prevrátená hodnota: z §k\\cdot a=m§ je §a=\\tfrac{m}{k}§, nie §\\tfrac{k}{m}§. $what",
    )) { "$what Ak ti vyšlo iné §a§, skontroluj, či si nedelil prvkom s parametrom: taký krok platí len pre §a§, pri ktorom je ten prvok nenulový." }
}

package dev.hamper.tabula.data

import dev.hamper.tabula.la.*
import dev.hamper.tabula.compat.*

/*
 * Judging the student's own vectors (Answer.Vectors). Every judge checks the property the task asks (the vector is in
 * the span, the list has every element once, the set is a basis of M, the rows solve the system), never equality with
 * one sample answer. Each entry gets its own reason when it is wrong; an enumeration also says how many are missing.
 * The same functions feed the screen and the tests (VectorsTest checks them against brute force).
 */

private fun pl(n: Int, one: String, few: String, other: String) = when (n) { 1 -> one; in 2..4 -> few; else -> other }

/** [base] to the power [e] in TeX: "1", the base, or "b^{e}" (never b^{1}; data/Terms.kt). */
fun pw(base: Int, e: Int): String = Terms.pow(base.toLong(), e)

/** A verdict with §math§ turned into the app's markup. */
private fun res(ok: Boolean, head: String, each: List<String?>) = VectorsResult(ok, m(head), each.map { it?.let(::m) })

/** "Chýba ešte 1 prvok.", "Chýbajú ešte 2 prvky.", "Chýba ešte 5 prvkov.": the verb agrees with the numeral. */
private fun missingText(k: Int, one: String, few: String, many: String) = when (k) { 1 -> "Chýba ešte §1§ $one."; in 2..4 -> "Chýbajú ešte §$k§ $few."; else -> "Chýba ešte §$k§ $many." }

/** Every vector of ⟨gens⟩ over Z_p, each once (la/FiniteSpace.kt enumerates it). */
fun Field.Zp.spanElements(gens: List<Vec>): Set<Vec> {
    val s = FiniteSpace(p, gens[0].size)
    return s.span(gens.map { v -> v.map { norm(it).num.toInt() } }).elements.map { z -> z.map { Frac.of(it) } }.toSet()
}

/** A vector of the model as exact numbers, and back. */
fun ZVec.frac(): Vec = map { Frac.of(it) }
fun Vec.zvec(f: Field.Zp): ZVec = map { f.norm(it).num.toInt() }

/** "(1,0,2)" for an entry, "θ" never: in a list the student writes the zeros, so we show them too. */
fun entryTex(v: Vec) = "§${v.tex()}§"

/**
 * Every element of ⟨[gens]⟩ ⊂⊂ Z_p^n, each written once. Wrong entries: not in the span, or a repeat. The list is
 * complete when nothing is missing; θ is named when it is the one left out (the most common slip).
 */
fun judgeSpanList(f: Field.Zp, gens: List<Vec>, entries: List<List<Vec>>): VectorsResult {
    val all = f.spanElements(gens)
    val first = HashMap<Vec, Int>()
    // a repeat is named as a repeat even when the vector is outside the span (its first entry says that)
    val written = HashMap<Vec, Int>()
    val each = entries.mapIndexed { i, e ->
        val v = e[0].map(f::norm)
        val earlier = written[v]
        if (earlier == null) written[v] = i
        when {
            earlier != null -> "Tento vektor už máš ako ${earlier + 1}. prvok. Množina obsahuje každý prvok raz, opakovanie nič nepridá."
            v !in all -> "${entryTex(v)} do obalu nepatrí, nie je kombináciou generátorov. Sústava s generátormi v stĺpcoch a týmto vektorom na pravej strane nemá riešenie."
            else -> { first[v] = i; null }
        }
    }
    val missing = all - first.keys
    val bad = each.count { it != null }
    val d = f.rank(gens)
    val size = "Obal má §${pw(f.p, d)}=${all.size}§ ${pl(all.size, "prvok", "prvky", "prvkov")}: " + (if (d == 1) "jeden pre každý koeficient z §\\mathbb{Z}_{${f.p}}§." else "jeden pre každú §$d§-ticu súradníc vzhľadom na bázu obalu.")
    val head = when {
        missing.isEmpty() && bad == 0 -> "Správne, ${pl(all.size, "jediný", "všetky", "všetkých")} §${all.size}§ ${pl(all.size, "prvok", "prvky", "prvkov")} obalu, každý raz. $size"
        missing.isEmpty() -> "Všetky prvky obalu máš, ale $bad ${pl(bad, "zápis je navyše", "zápisy sú navyše", "zápisov je navyše")}. Pri každom je napísané prečo. $size"
        else -> {
            val k = missing.size
            val zero = List(gens[0].size) { Frac.ZERO }
            missingText(k, "prvok", "prvky", "prvkov") +
                (if (zero in missing) " Aj nulový vektor §${zero.tex()}§: je to kombinácia s nulovými koeficientmi." else "") +
                (if (bad > 0) " Navyše $bad ${pl(bad, "zápis nesedí", "zápisy nesedia", "zápisov nesedí")}, pri každom je napísané prečo." else "") + " $size"
        }
    }
    return res(missing.isEmpty() && bad == 0, head, each)
}

/** Lines through θ and planes: the Slovak name of a subspace of dimension [d], singular and plural. */
private fun objWords(d: Int) = when (d) { 1 -> Triple("priamka", "priamky", "priamok"); 2 -> Triple("rovina", "roviny", "rovín"); else -> Triple("podpriestor", "podpriestory", "podpriestorov") }

/**
 * Every subspace of dimension [d] of [s], each given by a basis of [d] vectors and listed once. Wrong entries: LZ
 * vectors (they generate a smaller subspace), or a subspace already listed under another basis (the same set).
 */
fun judgeSubspaceList(s: FiniteSpace, d: Int, entries: List<List<Vec>>): VectorsResult {
    val f = Field.Zp(s.p)
    val total = s.subspaceCount(d).toInt()
    val (one, few, many) = objWords(d)
    val seen = HashMap<Subspace, Int>()
    val each = entries.mapIndexed { i, e ->
        val z = e.map { it.zvec(f) }
        val r = s.rank(z)
        val sub = s.span(z)
        when {
            r == 0 -> "Nulový vektor generuje len §\\{\\theta\\}§. To je triviálny podpriestor dimenzie §0§, nie ${one}."
            r < d -> "Tieto vektory sú LZ, generujú podpriestor dimenzie §$r§. ${one.replaceFirstChar { it.uppercase() }} potrebuje §$d§ LN vektory."
            sub in seen && entries[seen.getValue(sub)].map { v -> v.zvec(f) } == z -> "Tento zápis už v zozname je ako ${seen.getValue(sub) + 1}. položka. Každý podpriestor stačí napísať raz."
            sub in seen -> "Tento podpriestor už v zozname je. Máš ho ako ${seen.getValue(sub) + 1}. položku, iba s inou bázou; oba zápisy označujú tú istú množinu §${sub.setTex()}§."
            else -> { seen[sub] = i; null }
        }
    }
    val bad = each.count { it != null }
    val k = total - seen.size
    val why = if (d == 1) "Každý nenulový vektor leží v práve jednej priamke a priamka má §${s.p - 1}§ ${pl(s.p - 1, "nenulový vektor", "nenulové vektory", "nenulových vektorov")}: §(${pw(s.p, s.n)}-1)/(${s.p}-1)=$total§."
        else "Usporiadaných LN §$d§-tíc je §${s.orderedLN(d)}§ a každá ${one} má §${s.basesOf(d)}§ báz: §${s.orderedLN(d)}/${s.basesOf(d)}=$total§."
    val head = when {
        k == 0 && bad == 0 -> "Správne, ${pl(total, "jediná", "všetky", "všetkých")} §$total§ ${pl(total, one, few, many)}, každá raz. $why"
        k == 0 -> "Všetky podpriestory máš, ale $bad ${pl(bad, "zápis je navyše", "zápisy sú navyše", "zápisov je navyše")}. Pri každom je napísané prečo. $why"
        else -> missingText(k, one, few, many) +
            (if (bad > 0) " Navyše $bad ${pl(bad, "zápis nesedí", "zápisy nesedia", "zápisov nesedí")}, pri každom je napísané prečo." else "") + " $why"
    }
    return res(k == 0 && bad == 0, head, each)
}

/**
 * A basis of M = ⟨[gens]⟩ the student made up: each vector in M and nonzero, no repeats, LN, and as many as dim M
 * (the count is fixed by the answer). [extra] says what the task wants on top (another basis than the given one, a
 * given vector inside, a vector outside the given set), null when it holds.
 */
fun judgeBasis(f: Field, gens: List<Vec>, entries: List<List<Vec>>, extra: (List<Vec>) -> String?): VectorsResult {
    val vs = entries.map { e -> e[0].map(f::norm) }
    val d = f.rank(gens)
    val first = HashMap<Vec, Int>()
    val each = vs.mapIndexed { i, v ->
        val earlier = first[v]
        if (earlier == null) first[v] = i
        when {
            v.isZero() -> "Nulový vektor do bázy nepatrí. Súbor s §\\theta§ je vždy LZ: §1\\cdot\\theta=\\theta§ je netriviálna kombinácia."
            earlier != null -> "Rovnaký vektor ako ${earlier + 1}. v súbore. Súbor s dvoma rovnakými vektormi je LZ, bázou byť nemôže."
            !f.inSpan(gens, v) -> "${entryTex(v)} neleží v §M§, nie je kombináciou generátorov §M§. Báza §M§ musí byť z prvkov §M§."
            else -> null
        }
    }
    if (each.any { it != null }) return res(false, "Takto to báza §M§ nie je. Pri vektoroch je napísané prečo.", each)
    if (f.rank(vs) < vs.size) {
        val dep = f.dependency(vs)!!
        return res(false, "Tvoje vektory sú LZ, takže bázou nie sú. Netriviálna kombinácia s koeficientmi §${dep.tex()}§ dá §\\theta§. Pri §$d§ vektoroch z podpriestoru dimenzie §$d§ stačí overiť LN (Veta 2.55).", each)
    }
    extra(vs)?.let { return res(false, it, each) }
    return res(true, "Správne, je to báza §M§. Tvoje vektory ležia v §M§, sú LN a je ich §\\dim M=$d§, takže tvoria bázu (Veta 2.55).", each)
}

/**
 * The vectors the student added to the LN set [start] to complete it to a basis of the whole space F^n: as many as
 * are missing, nonzero, none twice or one of [start], and LN together with [start]. A dependent one is named with the
 * combination of the vectors before it, so the student sees why, not only that.
 */
fun judgeCompletion(f: Field, start: List<Vec>, entries: List<List<Vec>>): VectorsResult {
    val vs = entries.map { e -> e[0].map(f::norm) }
    val n = start[0].size
    val given = start.map { v -> v.map(f::norm) }
    val first = HashMap<Vec, Int>()
    val each = vs.mapIndexed { i, v ->
        val earlier = first[v]
        if (earlier == null) first[v] = i
        when {
            v.isZero() -> "Nulový vektor do bázy nepatrí. Súbor s §\\theta§ je vždy LZ: §1\\cdot\\theta=\\theta§ je netriviálna kombinácia."
            earlier != null -> "Rovnaký vektor ako ${earlier + 1}. v súbore. Súbor s dvoma rovnakými vektormi je LZ, bázou byť nemôže."
            v in given -> "To je jeden z daných vektorov. Doplniť treba vektory, ktoré v súbore ešte nie sú."
            else -> null
        }
    }
    if (each.any { it != null }) return res(false, "Takto to báza nie je. Pri vektoroch je napísané prečo.", each)
    val all = given + vs
    if (f.rank(all) < n) {
        // the first vector that is a combination of the ones before it; the given ones are LN, so it is one of the added
        val j = all.indices.first { f.rank(all.take(it + 1)) <= it }
        val c = f.solve(all.take(j), all[j])!!
        val k = j - given.size
        return res(false, "Súbor je LZ, bázou preto nie je: §${all[j].tex()}=${Terms.lin(c, all.take(j).map { it.tex() })}§ je kombinácia vektorov pred ním.",
            each.mapIndexed { i, e -> if (i == k) "Tento vektor je kombináciou predošlých. Skús namiesto neho iný." else e })
    }
    return res(true, "Správne, je to báza §${f.tex}^{$n}§. Dané vektory s tvojimi sú LN a je ich §$n§, toľko, aká je dimenzia, takže tvoria bázu (Veta 2.55).", each)
}

/**
 * The rows of [A|b] (each entry: a_{i1}..a_{in}, b_i) such that both [u] and [w] solve Ax = b and the rows of A are LN.
 */
fun judgeSystemRows(f: Field, u: Vec, w: Vec, entries: List<List<Vec>>): VectorsResult {
    val n = u.size
    fun dot(a: Vec, x: Vec) = a.indices.fold(Frac.ZERO) { acc, j -> f.add(acc, f.mul(a[j], x[j])) }
    val rows = entries.map { it[0].map(f::norm) }
    val each = rows.mapIndexed { i, row ->
        val a = row.take(n); val b = row[n]
        val bu = dot(a, u); val bw = dot(a, w)
        val k = i + 1
        when {
            a.isZero() -> "Riadok §$k§ matice §A§ je nulový. Súbor riadkov s nulovým vektorom je LZ, a to zadanie zakazuje."
            bu != b -> "Vektor §\\mathbf{u}=${u.tex()}§ rovnicu riadku §$k§ nespĺňa: ľavá strana dá §${bu.tex()}§, nie §b_{$k}=${b.tex()}§."
            bw != b -> "Vektor §\\mathbf{w}=${w.tex()}§ rovnicu riadku §$k§ nespĺňa: ľavá strana dá §${bw.tex()}§, nie §b_{$k}=${b.tex()}§."
            else -> null
        }
    }
    if (each.any { it != null }) return res(false, "Takto to nesedí. Pri riadkoch je napísané prečo.", each)
    val a = rows.map { it.take(n) }
    if (f.rank(a) < a.size) return res(false, "Riadky matice §A§ sú LZ, a zadanie chce LN. Jeden je násobkom druhého, takže nový riadok nepridal žiadnu podmienku.", each)
    return res(true, "Správne, oba vektory sú riešenia a riadky §A§ sú LN. Každý riadok §(a_1,\\dots,a_4)§ spĺňa §\\sum_j a_j(u_j-w_j)=0§ a pravá strana je §\\sum_j a_ju_j§.", each)
}

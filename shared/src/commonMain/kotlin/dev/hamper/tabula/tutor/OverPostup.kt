package dev.hamper.tabula.tutor

import dev.hamper.tabula.la.Combine
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.la.Scale
import dev.hamper.tabula.la.Swap
import dev.hamper.tabula.la.Vec
import dev.hamper.tabula.la.echelon
import dev.hamper.tabula.la.rank

/*
 * The engine's check of a GEM chain from paper (docs/TUTOR.md, "Kontrola"). The model only reads; this decides. A step
 * M_k ∼ M_k+1 is right exactly when both matrices have the same row space (row operations are invertible, so two
 * matrices of one shape are row equivalent iff their row spaces agree). That accepts any order and mix of operations
 * and an unwritten or misread note; the note is used only to point at a cell. A wrong row is one outside the previous
 * row space; the cell comes from the operation that explains all other cells of that row. Whether a wrong value is the
 * student's mistake or our misreading is decided by the next step: a mistake carries on, a misreading does not.
 * Nothing here throws on what the model returns.
 */

/** One GEM the exercise asks for on paper: the field, the starting matrix as the course writes it, and where it ends. */
data class GemUloha(val pole: Field, val start: Mat, val ciel: Ciel = Ciel.HST, val nazov: String = "") {
    enum class Ciel {
        /** Horný stupňovitý tvar (rank, LN, span, systems with back substitution). */
        HST,
        /** The identity left of the bar, (A | E) ∼ (E | A⁻¹). */
        JEDNOTKOVA,
    }
}

/** A cell the student wrote, as read: the main reading and the others the model thought possible. */
data class Citanie(val v: String, val alt: List<String> = emptyList())

/** One matrix of a chain on paper. [oblast]: where on the page (0–1, x0 y0 x1 y1), for a crop. */
data class PrepisMatica(val riadky: List<List<Citanie>>, val ciara: Int? = null, val preskrtnuta: Boolean = false, val strana: Int = 1, val oblast: List<Double>? = null)

/** One chain of matrices on paper with the notes under each "∼" (null: none written). */
data class GemPrepis(val matice: List<PrepisMatica>, val operacie: List<String?> = emptyList(), val zaver: String? = null)

/** A cell in the chain: matrix [matica] (0 = the first one written), row and column from 0. */
data class Miesto(val matica: Int, val riadok: Int, val stlpec: Int)

/**
 * A wrong cell: what is written, what the student's own operation gives, and the arithmetic behind it (TeX). [iste]:
 * no other operation could explain the row with another cell; false: the cell under the operation the rest of the
 * step points at (the same row subtracted as in the other rows), which is how a person reads it, but not proof.
 */
data class ChybnaBunka(val miesto: Miesto, val napisane: Frac, val maloByt: Frac, val odvodenie: String?, val iste: Boolean = true)

/** The question the app asks instead of marking: "Je tu 4 alebo 9?" ([moznosti] in the order to show). */
data class Otazka(val miesto: Miesto, val moznosti: List<Frac>)

data class Verdikt(
    val stav: Stav,
    /** The chain as checked (crossed out matrices left out), exact; a cell that couldn't be read is null. */
    val matice: List<List<List<Frac?>>> = emptyList(),
    val ciara: Int? = null,
    /** Per step k (matica k ∼ k+1): the operations as the engine understands them (written or inferred), null unknown. */
    val upravy: List<List<RowOp>?> = emptyList(),
    /** The matrix where it went wrong (0: the assignment copied wrong), its kind, rows and cells. */
    val krok: Int? = null,
    val typ: Typ? = null,
    val riadky: Set<Int> = emptySet(),
    val bunky: List<ChybnaBunka> = emptyList(),
    /** The right matrix in place of [krok], from the last right one by the student's own operations. */
    val spravne: List<List<Frac>>? = null,
    val spravneUpravy: List<RowOp>? = null,
    val otazka: Otazka? = null,
    /** The mistake is in the last matrix: no later step confirms it, so the app offers "Prečítal som zle?". */
    val overitCitanie: Boolean = false,
    /** The last matrix is in the form the exercise ends in (HST, or E left of the bar). */
    val hotovo: Boolean = false,
    /** The first matrix is a later stage of the exercise (a photo of the second half), not its assignment. */
    val pokracovanie: Boolean = false,
    /** The first matrix is the assignment with its rows (the vectors' components) in another order: fine. */
    val preusporiadane: Boolean = false,
    /** When the student's own operation can't be named: the engine's next GEM step from the last right matrix. */
    val odporucane: List<List<Frac>>? = null,
    val odporucaneUpravy: List<RowOp>? = null,
    /** The vectors are in rows where the exercise has them in columns. */
    val transponovane: Boolean = false,
    val uloha: Int? = null,
    /** Readings the engine took instead of the model's first one, and other remarks for the explanation. */
    val poznamky: List<String> = emptyList(),
) {
    /**
     * NESUDRZNE: each step is row equivalent, but one is no GEM step (a first nonzero element moved left): almost always
     * rows of neighbouring matrices read into one another. The app says it can't read the photo, never "úpravy sedia".
     */
    enum class Stav { BEZ_CHYBY, CHYBA, OTAZKA, NEDOKONCENE, INE_ZADANIE, NECITATELNE, NESUDRZNE }
    enum class Typ {
        /** A wrong number in a row the step computed. */
        ARITMETIKA,
        /** A row the step did not change, copied with a different number. */
        ODPIS,
        /** Each new row is a combination of the old ones, but the rank fell (a multiple by 0, two rows from each other at once). */
        NEPOVOLENA,
        /** The first matrix is the assignment with a number copied wrong. */
        ZADANIE,
    }
}

/**
 * Checks one chain against the GEMs of the exercise (the best matching one). [potvrdene]: cells the student confirmed
 * as written (matrix index among the kept ones): never asked about again, a value there that doesn't fit is a mistake.
 */
fun overPostup(ulohy: List<GemUloha>, prepis: GemPrepis, potvrdene: Set<Miesto> = emptySet()): Verdikt {
    val kept = prepis.matice.withIndex().filter { !it.value.preskrtnuta }
    if (kept.isEmpty() || ulohy.isEmpty()) return Verdikt(Verdikt.Stav.NECITATELNE)
    // the note under the "∼" before each kept matrix; before a rewritten one (its first try crossed out) the note
    // may stand before the crossed-out try
    val notes = (1 until kept.size).map { t ->
        var j = kept[t].index - 1
        var note = prepis.operacie.getOrNull(j)
        while (note == null && j > 0 && prepis.matice[j].preskrtnuta) { j--; note = prepis.operacie.getOrNull(j) }
        note
    }
    val chain = kept.map { it.value }
    // each task as a candidate; the one whose start fits best wins
    val results = ulohy.mapIndexed { i, u ->
        runCatching { over(u, chain, notes, potvrdene).copy(uloha = i) }.getOrElse { Verdikt(Verdikt.Stav.NECITATELNE, poznamky = listOf("čísla mimo rozsahu výpočtu"), uloha = i) }
    }
    return results.minBy { r ->
        when (r.stav) {
            Verdikt.Stav.INE_ZADANIE -> 3
            Verdikt.Stav.NECITATELNE -> 4
            else -> if (r.typ == Verdikt.Typ.ZADANIE) 1 else 0
        }
    }
}

private class Ch(val pole: Field, val m: List<List<List<Frac>>>) // candidate values per cell: first is the main reading

private fun over(u: GemUloha, chain: List<PrepisMatica>, notes: List<String?>, potvrdene: Set<Miesto>): Verdikt {
    val f = u.pole
    val sirka = chain.firstOrNull()?.riadky?.firstOrNull()?.size ?: 0
    val ciara = u.start.bar?.takeIf { it < sirka } ?: chain.firstNotNullOfOrNull { it.ciara }?.takeIf { it in 1 until sirka }
    // every cell's readings in the field; a cell nobody can read is an empty list
    val readings: List<List<List<List<Frac>>>> = chain.map { mat -> mat.riadky.map { r -> r.map { c -> (listOf(c.v) + c.alt).mapNotNull { bunka(it, f) }.distinct() } } }
    val primary: List<List<List<Frac?>>> = readings.map { mat -> mat.map { r -> r.map { it.firstOrNull() } } }
    val base = Verdikt(Verdikt.Stav.BEZ_CHYBY, matice = primary, ciara = ciara)
    // a matrix must be a rectangle of numbers to be checked
    for ((k, mat) in primary.withIndex()) {
        val n = mat.firstOrNull()?.size ?: 0
        if (mat.isEmpty() || n == 0 || mat.any { it.size != n }) return base.copy(stav = Verdikt.Stav.NECITATELNE, krok = k, poznamky = listOf("matica ${k + 1} nemá tvar obdĺžnika"))
        val unread = mat.withIndex().flatMap { (i, r) -> r.withIndex().filter { it.value == null }.map { Miesto(k, i, it.index) } }
        if (unread.isNotEmpty()) return base.copy(stav = Verdikt.Stav.NECITATELNE, krok = k, poznamky = listOf("nečitateľné bunky v matici ${k + 1}"))
    }
    val mats: MutableList<List<List<Frac>>> = primary.map { m -> m.map { r -> r.map { it!! } } }.toMutableList()
    val notes2 = mats.indices.drop(1).map { k -> notes.getOrNull(k - 1) }
    val poznamky = ArrayList<String>()

    // ---- the start: the assignment, its transpose, a row permutation of either, or a later stage of the same GEM
    val a = u.start.rows
    val at = a[0].indices.map { j -> a.map { it[j] } }
    val m0 = mats[0]
    var transponovane = false
    var pokracovanie = false
    var preusporiadane = false
    // the assignment (or its transpose) with its rows in the order closest to the first matrix on paper
    val options = listOfNotNull(a.takeIf { sameShape(m0, a) }?.let { false to align(m0, it) },
        at.takeIf { u.start.bar == null && sameShape(m0, at) && at != a }?.let { true to align(m0, it) })
    val best = options.minByOrNull { (_, t) -> diff(m0, t).size }
    val d = best?.let { diff(m0, it.second) } ?: emptyList()
    // a later stage of the same GEM: every row that differs is a combination of rows of the assignment (one step of
    // elimination), or the matrices are row equivalent and differ in more than a copying slip would
    val laterStep = best != null && d.isNotEmpty() && rowEq(f, m0, best.second) && d.map { it.first }.distinct().all { i -> krokEliminacie(best.second[i], m0[i], f) }
    when {
        best != null && d.isEmpty() -> { transponovane = best.first; preusporiadane = best.second != (if (best.first) at else a) }
        best != null && laterStep && rowEq(f, m0, best.second) -> { pokracovanie = true; transponovane = best.first }
        best != null && d.size <= 2 -> {
            // a cell or two different: a misreading (another reading fits), or the assignment copied wrong
            val (tr, target) = best
            transponovane = tr
            val readable = d.all { (i, j) -> target[i][j] in readings[0][i][j] }
            val next = mats.getOrNull(1)
            when {
                readable -> { mats[0] = target; poznamky += "zadanie prečítané v inom čítaní bunky" }
                // the student went on from the right numbers: the assignment is fine, the paper just reads oddly
                next != null && sameShape(target, next) && rowEq(f, target, next) && !rowEq(f, m0, next) -> { mats[0] = target; poznamky += "zadanie na papieri inak, ďalej počítané so správnym" }
                else -> return base.copy(
                    stav = Verdikt.Stav.CHYBA, krok = 0, typ = Verdikt.Typ.ZADANIE, riadky = d.map { it.first }.toSet(), transponovane = transponovane,
                    bunky = d.map { (i, j) -> ChybnaBunka(Miesto(0, i, j), m0[i][j], target[i][j], null) }, spravne = target,
                    matice = mats.toList(), upravy = emptyList(), overitCitanie = mats.size == 1,
                )
            }
        }
        sameShape(m0, a) && rowEq(f, m0, a) -> pokracovanie = true
        u.start.bar == null && sameShape(m0, at) && rowEq(f, m0, at) -> { pokracovanie = true; transponovane = true }
        else -> return base.copy(stav = Verdikt.Stav.INE_ZADANIE, poznamky = listOf("prvá matica nie je zadanie tejto úlohy"))
    }

    // a zero row left out (the course allows it): every shorter matrix counts with zero rows at the bottom, so the
    // steps and the look-ahead compare matrices of one shape
    for (i in 1 until mats.size) {
        val m = mats[i]
        if (m.isNotEmpty() && m.size < mats[0].size && m[0].size == mats[0][0].size) {
            mats[i] = m + List(mats[0].size - m.size) { List(m[0].size) { Frac.ZERO } }
            if (poznamky.none { it.startsWith("nulové riadky") }) poznamky += "nulové riadky vynechané (počítam s nimi dole)"
        }
    }

    // ---- the steps
    val upravy = ArrayList<List<RowOp>?>()
    var k = 0
    while (k < mats.size - 1) {
        val o = mats[k]
        var n = mats[k + 1]
        if (!sameShape(o, n)) return base.copy(stav = Verdikt.Stav.NECITATELNE, krok = k + 1, matice = mats.toList(),
            upravy = upravy, transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky + "matica ${k + 2} má iný tvar než ${k + 1}")
        val written = upravy(notes2.getOrNull(k), o.size)
        if (rowEq(f, o, n)) {
            // a written step that gives exactly this matrix is the student's own (e.g. ř1 + ř2 to get a nonzero pivot)
            if (!postupGem(o, n) && f.applyWritten(o, written).none { it == n }) return base.copy(stav = Verdikt.Stav.NESUDRZNE, krok = k + 1, matice = mats.toList(), upravy = upravy,
                transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky + "matica ${k + 2} nevznikla krokom GEM")
            upravy += vysvetli(f, o, n, written)
            k++; continue
        }
        // another reading of a cell may make the step right; the step after decides whether that reading is the true one
        val alt = altFix(f, o, n, readings[k + 1])?.takeIf { Miesto(k + 1, it.second.riadok, it.second.stlpec) !in potvrdene }
        val after = mats.getOrNull(k + 2)
        if (alt != null) {
            val keepsWritten = after != null && sameShape(n, after) && rowEq(f, n, after)
            val keepsAlt = after != null && sameShape(alt.first, after) && rowEq(f, alt.first, after)
            if (keepsAlt && !keepsWritten) {
                mats[k + 1] = alt.first; poznamky += "bunka ${alt.second.riadok + 1},${alt.second.stlpec + 1} v matici ${k + 2} prečítaná inak (sedí s ďalším krokom)"
                continue
            }
            if (after == null) {
                val mi = Miesto(k + 1, alt.second.riadok, alt.second.stlpec)
                return base.copy(stav = Verdikt.Stav.OTAZKA, otazka = Otazka(mi, listOf(n[mi.riadok][mi.stlpec], alt.first[mi.riadok][mi.stlpec])), krok = k + 1,
                    matice = mats.toList(), upravy = upravy, transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky)
            }
        }
        val chyba = lokalizuj(f, o, n, written)
        val corrected = chyba.spravne
        // a misreading does not carry on: the next matrix follows from the corrected one, not from what we read
        if (after != null && corrected != null && chyba.bunky.size == 1 && chyba.iste && Miesto(k + 1, chyba.bunky[0].first, chyba.bunky[0].second) !in potvrdene && sameShape(corrected, after) && rowEq(f, corrected, after) && !rowEq(f, n, after)) {
            val c = chyba.bunky.single()
            return base.copy(stav = Verdikt.Stav.OTAZKA, otazka = Otazka(Miesto(k + 1, c.first, c.second), listOf(n[c.first][c.second], corrected[c.first][c.second])), krok = k + 1,
                matice = mats.toList(), upravy = upravy, transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky)
        }
        return base.copy(
            stav = Verdikt.Stav.CHYBA, krok = k + 1, typ = chyba.typ, riadky = chyba.riadky,
            bunky = chyba.bunky.map { (i, j, d) -> ChybnaBunka(Miesto(k + 1, i, j), n[i][j], corrected!![i][j], d, chyba.iste) },
            spravne = corrected ?: spravnyDalsi(f, o, chyba.ops), spravneUpravy = chyba.ops,
            odporucane = if (corrected == null && spravnyDalsi(f, o, chyba.ops) == null) nextEchelonStep(f, o, ciara, u.ciel)?.second else null,
            odporucaneUpravy = if (corrected == null && spravnyDalsi(f, o, chyba.ops) == null) nextEchelonStep(f, o, ciara, u.ciel)?.first else null,
            overitCitanie = after == null, matice = mats.toList(), upravy = upravy,
            transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky,
        )
    }
    val last = mats.last()
    val hotovo = when (u.ciel) {
        GemUloha.Ciel.HST -> isHst(last)
        GemUloha.Ciel.JEDNOTKOVA -> isIdentityLeft(last, u.start.bar ?: last.size)
    }
    val m = mats.toList()
    if (!hotovo) {
        val step = nextEchelonStep(f, last, ciara, u.ciel)
        return base.copy(stav = Verdikt.Stav.NEDOKONCENE, krok = mats.size - 1, matice = m, upravy = upravy, spravne = step?.second, spravneUpravy = step?.first,
            transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky)
    }
    return base.copy(stav = Verdikt.Stav.BEZ_CHYBY, matice = m, upravy = upravy, hotovo = true, transponovane = transponovane, pokracovanie = pokracovanie, preusporiadane = preusporiadane, poznamky = poznamky)
}

/* ---- Linear algebra on the student's matrices ------------------------------------------------------------------ */

private fun sameShape(x: List<List<Frac>>, y: List<List<Frac>>) = x.size == y.size && x.isNotEmpty() && x[0].size == y[0].size

/** Same row space (same shape assumed): row equivalent. */
internal fun rowEq(f: Field, x: List<List<Frac>>, y: List<List<Frac>>): Boolean {
    val rx = f.rank(x); val ry = f.rank(y)
    return rx == ry && f.rank(x + y) == rx
}

private fun inSpan(f: Field, rows: List<Vec>, v: Vec) = f.rank(rows + listOf(v)) == f.rank(rows)

private fun diff(x: List<List<Frac>>, y: List<List<Frac>>): List<Pair<Int, Int>> =
    x.indices.flatMap { i -> x[i].indices.filter { j -> x[i][j] != y[i][j] }.map { i to it } }

/** [t]'s rows in the order closest to [m] (fewest different cells): the student may list the vectors in any order. */
private fun align(m: List<List<Frac>>, t: List<List<Frac>>): List<List<Frac>> {
    if (m.size > 7) return t
    var best = t
    var bestD = diff(m, t).size
    fun go(prefix: List<Int>, rest: List<Int>) {
        if (bestD == 0) return
        if (rest.isEmpty()) { val c = prefix.map { t[it] }; val d = diff(m, c).size; if (d < bestD) { best = c; bestD = d }; return }
        for (x in rest) go(prefix + x, rest - x)
    }
    go(emptyList(), t.indices.toList())
    return best
}

/**
 * [row] in place of [old] looks like elimination: it has more zeros in front. In a matrix row equivalent to the
 * assignment, a copying slip that does this can't be told from a photo of a later stage by anyone, the engine
 * included (the harness's simulator does not generate such slips).
 */
fun krokEliminacie(old: List<Frac>, row: List<Frac>, f: Field): Boolean {
    fun lead(r: List<Frac>) = r.indexOfFirst { !it.isZero }.let { if (it < 0) r.size else it }
    if (lead(row) > lead(old)) return true
    // or a multiple of the row (G2): every cell the same multiple
    val c = old.indices.firstOrNull { !old[it].isZero } ?: return false
    if (row[c].isZero) return false
    val k = f.div(row[c], old[c])
    return old.indices.all { row[it] == f.mul(old[it], k) }
}

/**
 * [n] can follow [o] in a GEM: sorted, the columns of the rows' first nonzero elements never move left (elimination
 * moves them right, a swap or a multiple keeps them). Rows mixed up between neighbouring matrices break this.
 */
internal fun postupGem(o: List<List<Frac>>, n: List<List<Frac>>): Boolean {
    fun leads(m: List<List<Frac>>) = m.map { r -> r.indexOfFirst { !it.isZero }.let { if (it < 0) r.size else it } }.sorted()
    val a = leads(o); val b = leads(n)
    return a.size == b.size && a.indices.all { b[it] >= a[it] }
}

/** One other reading of one cell of [n] that makes the step right: the corrected matrix and the cell. */
private fun altFix(f: Field, o: List<List<Frac>>, n: List<List<Frac>>, r: List<List<List<Frac>>>): Pair<List<List<Frac>>, Miesto>? {
    for (i in n.indices) for (j in n[i].indices) for (v in r.getOrNull(i)?.getOrNull(j).orEmpty().drop(1)) {
        val c = n.mapIndexed { a, row -> if (a != i) row else row.mapIndexed { b, x -> if (b == j) v else x } }
        if (rowEq(f, o, c)) return c to Miesto(0, i, j)
    }
    return null
}

/** Applies operations that all take their rows from [o] (one "∼" on paper). */
private fun Field.applySimultaneous(o: List<List<Frac>>, ops: List<RowOp>): List<List<Frac>>? {
    val rows = o.toMutableList()
    for (op in ops) when (op) {
        is Swap -> { val t = rows[op.i]; rows[op.i] = rows[op.j]; rows[op.j] = t }
        is Scale -> rows[op.i] = o[op.i].map { mul(op.k, it) }
        is Combine -> rows[op.target] = o[op.target].indices.map { j -> add(mul(op.a, o[op.target][j]), mul(op.b, o[op.src][j])) }
        else -> return null
    }
    return rows
}

/** Applies operations one after the other (a student who uses the row just changed). */
private fun Field.applySequential(o: List<List<Frac>>, ops: List<RowOp>): List<List<Frac>>? {
    val rows = o.toMutableList()
    for (op in ops) when (op) {
        is Swap -> { val t = rows[op.i]; rows[op.i] = rows[op.j]; rows[op.j] = t }
        is Scale -> rows[op.i] = rows[op.i].map { mul(op.k, it) }
        is Combine -> { val t = rows[op.target]; val s = rows[op.src]; rows[op.target] = t.indices.map { j -> add(mul(op.a, t[j]), mul(op.b, s[j])) } }
        else -> return null
    }
    return rows
}

private fun Field.applyWritten(o: List<List<Frac>>, ops: List<RowOp>?): List<List<List<Frac>>> {
    if (ops.isNullOrEmpty()) return emptyList()
    return listOfNotNull(applySimultaneous(o, ops), applySequential(o, ops)).distinct()
}

/**
 * The operations of a right step: the written ones when they give exactly [n], else one per changed row inferred from the
 * numbers (a multiple of a row plus a multiple of another one), else null (right, but we can't name how).
 */
private fun vysvetli(f: Field, o: List<List<Frac>>, n: List<List<Frac>>, written: List<RowOp>?): List<RowOp>? {
    if (f.applyWritten(o, written).any { it == n }) return written
    val ops = ArrayList<RowOp>()
    val used = HashSet<Int>()
    for (i in n.indices) {
        if (n[i] == o[i]) continue
        val e = najlepsie(f, o, n[i], i).firstOrNull { it.zhody == n[i].size } ?: return null
        if (e.t != i && e.b.isZero && e.a == Frac.ONE) {
            // a row moved here from elsewhere: a swap, named once
            if (o[i] == n[e.t] && i !in used && e.t !in used) { ops += Swap(minOf(i, e.t), maxOf(i, e.t)); used += i; used += e.t }
            else if (i !in used) return null
            continue
        }
        if (e.t != i) return null
        ops += if (e.b.isZero) Scale(i, e.a) else Combine(i, e.a, e.s, e.b)
    }
    // the kind order a student writes them in: swaps, multiples, combinations
    return ops.sortedBy { when (it) { is Swap -> 0; is Scale -> 1; else -> 2 } }
}

/** new row ≈ a·o[t] + b·o[s]: how many cells agree, and which don't. */
private class Vyklad(val t: Int, val a: Frac, val s: Int, val b: Frac, val zhody: Int, val zle: List<Int>)

/**
 * The combinations of one or two rows of [o] that best explain [row] (all its cells, or all but a few); the ones on
 * the same row index first. A single wrong cell leaves the others to pin the coefficients down.
 */
private fun najlepsie(f: Field, o: List<List<Frac>>, row: List<Frac>, i: Int): List<Vyklad> {
    val n = row.size
    val out = ArrayList<Vyklad>()
    fun add(t: Int, a: Frac, s: Int, b: Frac) {
        if (f.norm(a).isZero) return
        val v = o[t].indices.map { j -> f.add(f.mul(a, o[t][j]), if (b.isZero) Frac.ZERO else f.mul(b, o[s][j])) }
        val zle = (0 until n).filter { v[it] != row[it] }
        out += Vyklad(t, f.norm(a), s, f.norm(b), n - zle.size, zle)
    }
    for (t in o.indices) {
        // a multiple of one row: the coefficient from any cell where that row isn't 0
        for (c in 0 until n) if (!o[t][c].isZero) { add(t, f.div(row[c], o[t][c]), t, Frac.ZERO) }
        for (s in o.indices) if (s != t) {
            for (c1 in 0 until n) for (c2 in c1 + 1 until n) {
                val det = f.sub(f.mul(o[t][c1], o[s][c2]), f.mul(o[s][c1], o[t][c2]))
                if (det.isZero) continue
                // Cramer on the two columns
                val a = f.div(f.sub(f.mul(row[c1], o[s][c2]), f.mul(o[s][c1], row[c2])), det)
                val b = f.div(f.sub(f.mul(o[t][c1], row[c2]), f.mul(row[c1], o[t][c2])), det)
                add(t, a, s, b)
            }
        }
    }
    return out.distinctBy { listOf(it.t, it.a, it.s, it.b) }
        .sortedWith(compareByDescending<Vyklad> { it.zhody }.thenBy { if (it.t == i) 0 else 1 }.thenBy { if (it.b.isZero) 0 else 1 })
}

private class Lokalizacia(
    val typ: Verdikt.Typ, val riadky: Set<Int>, val bunky: List<Triple<Int, Int, String?>>,
    val spravne: List<List<Frac>>?, val ops: List<RowOp>?, val iste: Boolean = true,
)

/**
 * Where a wrong step went wrong: the rows outside the old row space; a cell only when it is certain. With the
 * student's written operations (when they give every right row exactly) the cells are where the row differs from them.
 * Without them a cell is named only when every combination of old rows that fits all but one cell of the row points
 * at the same cell with the same value; otherwise only the row (never a wrong cell).
 */
private fun lokalizuj(f: Field, o: List<List<Frac>>, n: List<List<Frac>>, written: List<RowOp>?): Lokalizacia {
    val wrong = n.indices.filter { !inSpan(f, o, n[it]) }
    if (wrong.isEmpty()) {
        // every row is a combination of the old ones, yet the rank fell: the rows whose restoring brings it back
        val r0 = f.rank(o)
        val changed = n.indices.filter { n[it] != o[it] }
        val blame = changed.filter { i -> f.rank(n.mapIndexed { a, row -> if (a == i) o[i] else row }) == r0 }.ifEmpty { changed }
        return Lokalizacia(Verdikt.Typ.NEPOVOLENA, blame.toSet(), emptyList(), null, written?.takeIf { it.isNotEmpty() })
    }
    val right = n.indices.filter { it !in wrong }
    // the student's own operations: of the readings (all at once / one after another) that give every right row, the one
    // closest to the wrong rows
    val fits = f.applyWritten(o, written).filter { e -> right.all { e[it] == n[it] } }
    fun far(e: List<List<Frac>>) = wrong.sumOf { i -> n[i].indices.count { e[i][it] != n[i][it] } }
    val closest = fits.minOfOrNull(::far)
    // two readings of the note equally close but meaning different rows: the note can't name the cell
    val byWritten = fits.filter { far(it) == closest }.takeIf { c -> c.map { e -> wrong.map { e[it] } }.distinct().size == 1 }?.first()
    val cells = ArrayList<Triple<Int, Int, String?>>()
    val corrected = n.map { it.toMutableList() }
    var allCells = true
    var iste = true
    var typ = Verdikt.Typ.ARITMETIKA
    for (i in wrong) {
        if (byWritten != null) {
            // a misread or mis-written note fits the right rows of a one-row step trivially: its cells count only when
            // the numbers agree, or as likely when it names a single cell
            val zle = n[i].indices.filter { byWritten[i][it] != n[i][it] }
            val sure = jedinaBunka(f, o, n[i])
            when {
                zle.size == 1 && sure != null && sure.first == zle[0] && sure.second == byWritten[i][zle[0]] -> {}
                zle.size == 1 -> iste = false
                else -> { allCells = false; continue }
            }
            for (j in zle) { cells += Triple(i, j, odvodenieZ(f, o, written!!, i, j, byWritten[i][j])); corrected[i][j] = byWritten[i][j] }
            if (byWritten[i] == o[i] || o.any { it == byWritten[i] }) typ = Verdikt.Typ.ODPIS
            continue
        }
        // a step that moves no row (every right row comes from its own old row) changes a row only with that row in it
        val vlastny = right.all { r -> n[r] == o[r] || najlepsie(f, o, n[r], r).any { it.zhody == n[r].size && (it.t == r || it.s == r) && !(it.b.isZero && it.t != r) } }
        // the row the other rows of this ∼ were eliminated with: the wrong one used the same, as one ∼ clears one column
        val zdroj = if (!vlastny) null else right.filter { n[it] != o[it] }.map { r ->
            // the course's (G3) keeps the row itself as it is (ř3 − 2·ř2): that reading first, any other only without it
            val exact = najlepsie(f, o, n[r], r).filter { it.zhody == n[r].size && it.t == r && !it.b.isZero && it.s != r }
            (exact.filter { it.a == Frac.ONE }.ifEmpty { exact }).map { it.s }.toSet()
        }.takeIf { it.isNotEmpty() }?.reduce { a, b -> a intersect b }?.singleOrNull()
        // certain: one cell whichever operation it was; else likely: the one cell under the operation the rest of the ∼
        // points at (the row itself kept, the same row subtracted as in the other rows)
        val certain = jedinaBunka(f, o, n[i])
        val c = certain ?: zdroj?.let { jedinaBunka(f, o, n[i], i, it) }
        if (c == null) { allCells = false; continue }
        if (certain == null) iste = false
        corrected[i][c.first] = c.second
        cells += Triple(i, c.first, c.third)
        if (o.any { row -> row.indices.all { j -> j == c.first || row[j] == n[i][j] } && row[c.first] == c.second }) typ = Verdikt.Typ.ODPIS
    }
    val fixed = corrected.map { it.toList() }
    // the corrected matrix must itself be a right step, else we only know the rows
    val ok = allCells && rowEq(f, o, fixed)
    val ops = if (byWritten != null) written else vysvetli(f, o, if (ok) fixed else n, null)
    // a wrong row the step computes (a combination targets it) is arithmetic, not copying, whatever its numbers equal
    if (typ == Verdikt.Typ.ODPIS && ops?.any { op -> op is Combine && op.target in wrong } == true) typ = Verdikt.Typ.ARITMETIKA
    return Lokalizacia(typ, wrong.toSet(), if (ok) cells else emptyList(), if (ok) fixed else null, ops, iste)
}

/**
 * The one cell of [row] that keeps it from being a combination of at most two rows of [o]: its column, the value that
 * fits, and the arithmetic. Null when no single cell does it, or when two explanations disagree on the cell or value.
 */
private fun jedinaBunka(f: Field, o: List<List<Frac>>, row: List<Frac>, vlastny: Int? = null, zdroj: Int? = null): Triple<Int, Frac, String?>? {
    val n = row.size
    if (n < 3) return null
    var found: Triple<Int, Frac, String?>? = null
    // [vlastny]: only combinations that keep the row itself in them (ř_i := a·ř_i + b·ř_s)
    val pairs = o.indices.flatMap { t -> listOf(t to t) + o.indices.filter { it > t }.map { t to it } }.filter { (t, s) -> vlastny == null || t == vlastny || s == vlastny }
        .filter { (t, s) -> zdroj == null || setOf(t, s) == setOf(vlastny, zdroj) || (t == s && t == vlastny) }
    for ((t, s) in pairs) for (w in 0 until n) {
        val cols = (0 until n).filter { it != w }
        val basis = if (t == s) listOf(cols.map { o[t][it] }) else listOf(cols.map { o[t][it] }, cols.map { o[s][it] })
        val rb = f.rank(basis)
        if (rb == 0) continue
        val target = cols.map { row[it] }
        if (f.rank(basis + listOf(target)) != rb) continue
        // the rest of the row fits this pair; the value of cell w must be pinned down by it
        if (rb < basis.size) return null
        val (a, b) = koeficienty(f, basis, target) ?: return null
        val expect = if (t == s) f.mul(a, o[t][w]) else f.add(f.mul(a, o[t][w]), f.mul(b, o[s][w]))
        if (expect == row[w]) continue // the whole row fits: not this pair's doing
        // nothing of the row kept (0·ř), or (with [vlastny]) the row itself dropped: not an operation anyone does
        if (a.isZero) continue
        if (vlastny != null && ((t == vlastny && a.isZero) || (s == vlastny && t != s && b.isZero))) continue
        // a copied row (a swap, an unchanged row) has no arithmetic to show
        val d = when {
            (t == s || b.isZero) && a == Frac.ONE -> null
            t == s || b.isZero -> odvodenie(f, a, o[t][w], Frac.ZERO, Frac.ZERO, expect)
            else -> odvodenie(f, a, o[t][w], b, o[s][w], expect)
        }
        when {
            found == null -> found = Triple(w, expect, d)
            found.first != w || found.second != expect -> return null
        }
    }
    return found
}

/** a, b with a·u + b·v = target (or a·u for one row); the rows are independent. */
private fun koeficienty(f: Field, basis: List<List<Frac>>, target: List<Frac>): Pair<Frac, Frac>? {
    val u = basis[0]
    if (basis.size == 1) { val c = u.indices.firstOrNull { !u[it].isZero } ?: return null; return f.div(target[c], u[c]) to Frac.ZERO }
    val v = basis[1]
    for (c1 in u.indices) for (c2 in c1 + 1 until u.size) {
        val det = f.sub(f.mul(u[c1], v[c2]), f.mul(v[c1], u[c2]))
        if (det.isZero) continue
        val a = f.div(f.sub(f.mul(target[c1], v[c2]), f.mul(v[c1], target[c2])), det)
        val b = f.div(f.sub(f.mul(u[c1], target[c2]), f.mul(target[c1], u[c2])), det)
        return a to b
    }
    return null
}

/** The cell's arithmetic, TeX: "4-3\cdot 3=-5", in Z_p with "\equiv x \pmod p". */
private fun odvodenie(f: Field, a: Frac, x: Frac, b: Frac, y: Frac, vysledok: Frac): String? {
    fun t(v: Frac) = if (v < Frac.ZERO) "(${v.tex(true)})" else v.tex(true)
    val first = if (a == Frac.ONE) t(x) else "${t(a)}\\cdot ${t(x)}"
    val raw = a * x + b * y
    // the arithmetic shown must give the value claimed (an operation read in another order would not): else none
    if (runCatching { f.norm(raw) }.getOrNull() != vysledok) return null
    val second = when {
        b.isZero -> ""
        b < Frac.ZERO && f !is Field.Zp -> "-" + (if (-b == Frac.ONE) t(y) else "${(-b).tex(true)}\\cdot ${t(y)}")
        else -> "+" + (if (b == Frac.ONE) t(y) else "${t(b)}\\cdot ${t(y)}")
    }
    val tail = if (f is Field.Zp && raw != vysledok) "=${raw.tex(true)}\\equiv ${vysledok.tex(true)}\\pmod{${f.p}}" else "=${vysledok.tex(true)}"
    return first + second + tail
}

/** The arithmetic of the written operation for cell (i, j), only when it gives [cil] (the value the step should have). */
private fun odvodenieZ(f: Field, o: List<List<Frac>>, ops: List<RowOp>, i: Int, j: Int, cil: Frac): String? {
    val op = ops.lastOrNull { (it is Combine && it.target == i) || (it is Scale && it.i == i) } ?: return null
    return when (op) {
        is Combine -> odvodenie(f, op.a, o[i][j], op.b, o[op.src][j], cil)
        is Scale -> odvodenie(f, op.k, o[i][j], Frac.ZERO, Frac.ZERO, cil)
        else -> null
    }
}

/** The right matrix after a wrong step: the student's operations on the last right matrix, or null. */
private fun spravnyDalsi(f: Field, o: List<List<Frac>>, ops: List<RowOp>?): List<List<Frac>>? {
    if (ops.isNullOrEmpty()) return null
    return f.applySimultaneous(o, ops)?.takeIf { rowEq(f, o, it) }
}

private fun isHst(m: List<List<Frac>>): Boolean {
    val l = m.map { r -> r.indexOfFirst { !it.isZero }.takeIf { it >= 0 } }
    val nz = l.takeWhile { it != null }
    if (l.drop(nz.size).any { it != null }) return false
    return nz.zipWithNext().all { (a, b) -> a!! < b!! }
}

private fun isIdentityLeft(m: List<List<Frac>>, bar: Int): Boolean =
    m.indices.all { i -> (0 until minOf(bar, m[i].size)).all { j -> m[i][j] == if (i == j) Frac.ONE else Frac.ZERO } }

/** The engine's next ∼ from a matrix that isn't finished: what to do next. */
private fun nextEchelonStep(f: Field, m: List<List<Frac>>, bar: Int?, ciel: GemUloha.Ciel): Pair<List<RowOp>, List<List<Frac>>>? {
    if (ciel == GemUloha.Ciel.JEDNOTKOVA && isHst(m)) {
        // above the first nonzero elements: clear one column from the bottom up, then the ones on the diagonal
        val n = m.size
        for (p in n - 1 downTo 0) {
            val c = m[p].indexOfFirst { !it.isZero }.takeIf { it in 0 until (bar ?: n) } ?: continue
            if (m[p][c] != Frac.ONE) { val ops = listOf<RowOp>(Scale(p, f.inv(m[p][c]))); return ops to f.applySimultaneous(m, ops)!! }
            val ops = (0 until p).filter { !m[it][c].isZero }.map { Combine(it, Frac.ONE, p, f.neg(m[it][c])) }
            if (ops.isNotEmpty()) return ops to f.applySimultaneous(m, ops)!!
        }
        return null
    }
    val limit = if (ciel == GemUloha.Ciel.HST) m[0].size else bar ?: m[0].size
    // the student's own pivot stays: where the next column to clear already has a nonzero element on top, eliminate
    // under it (the engine's GEM would prefer a 1 and swap, undoing a swap the student just made)
    var top = 0
    for (col in 0 until limit) {
        if (top >= m.size) break
        val nz = (top until m.size).filter { !m[it][col].isZero }
        if (nz.isEmpty()) continue
        if (nz == listOf(top)) { top++; continue }
        if (m[top][col].isZero) break
        val p = m[top][col]
        val ops = nz.drop(1).map { r ->
            val q = m[r][col]
            if (f is Field.Zp || (q / p).isInt) Combine(r, Frac.ONE, top, f.neg(f.div(q, p)))
            else Combine(r, if (p < Frac.ZERO) -p else p, top, if (p < Frac.ZERO) q else -q)
        }
        return f.applySimultaneous(m, ops)?.let { ops to it }
    }
    val e = f.echelon(Mat(m, bar), limit)
    val st = e.steps.firstOrNull() ?: return null
    return st.ops to st.after.rows
}

package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.la.Combine
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.la.Scale
import dev.hamper.tabula.la.Swap
import dev.hamper.tabula.la.rank
import dev.hamper.tabula.tutor.Citanie
import dev.hamper.tabula.tutor.GemPrepis
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.PrepisMatica
import dev.hamper.tabula.tutor.Verdikt
import kotlin.math.abs
import kotlin.random.Random

/*
 * The student simulator of the harness (docs/TUTOR.md, "Harness", K): a GEM done by hand the many ways students do it,
 * with at most one mistake of a known kind at a known place, and everything after it computed on from the wrong matrix
 * (a real mistake carries on). Its output is the exact transcription (what a perfect reader would return), so the
 * engine is measured alone; the same cases, drawn as handwriting, measure the reading.
 */

/** The kinds of mistakes (from the chapter packs' "Časté chyby" and Discord), docs/TUTOR.md lists them. */
enum class Chyba(val popis: String) {
    E1("aritmetika v počítanej bunke (znamienko, ±1)"),
    E2("ℤ_p: zlá redukcia záporného čísla"),
    E3("zle odpísaný nemenený riadok"),
    E4("dve súčasné úpravy, každá z pôvodného druhého riadku"),
    E5("násobok 0"),
    E6("nedokončený HST vyhlásený za hotový"),
    E9("zle odpísané zadanie"),
}

/** What the engine must find: nothing, or the matrix, the rows, the cell (and its right value) and the kind. */
data class Pravda(
    val chyba: Chyba?, val krok: Int? = null, val riadky: Set<Int> = emptySet(), val bunka: Pair<Int, Int>? = null,
    val maloByt: Frac? = null, val stav: Verdikt.Stav = Verdikt.Stav.BEZ_CHYBY, val typ: Verdikt.Typ? = null,
)

data class Pripad(val id: String, val uloha: GemUloha, val prepis: GemPrepis, val pravda: Pravda, val styl: Set<String>)

private class Krok(val ops: List<RowOp>, val after: List<List<Frac>>, val note: String?, val simultaneous: Boolean = true)

class Student(private val r: Random) {
    private val styly = HashSet<String>()

    /** One case for [u] with mistake [e] (null: a right chain); null when this task can't carry that mistake. */
    fun pripad(id: String, u: GemUloha, e: Chyba?): Pripad? {
        styly.clear()
        val f = u.pole
        var start = u.start.rows.map { row -> row.map { f.norm(it) } }
        if (u.start.bar == null && r.nextInt(10) < 3) { start = start[0].indices.map { j -> start.map { it[j] } }; styly += "transponovane" }
        if (r.nextInt(10) < 2 && start.size > 1) { start = start.shuffled(r); styly += "poradie_riadkov" }
        val bar = if (styly.contains("transponovane")) null else u.start.bar
        var pravda = Pravda(null)
        var m0 = start
        if (e == Chyba.E9) {
            val (i, j) = r.nextInt(m0.size) to r.nextInt(m0[0].size)
            val v = zla(f, m0[i][j]) ?: return null
            m0 = m0.mapIndexed { a, row -> if (a != i) row else row.mapIndexed { b, x -> if (b == j) v else x } }
            // a slip that two arrangements of the assignment explain equally (equal rows, a transpose that differs in
            // one cell too) has no single place: nobody can say which cell was miscopied
            if (miestaPrepisu(f, u, m0).size > 1) return null
            // a slip that reads as a legal first elimination step is indistinguishable from a photo of a later stage
            if (dev.hamper.tabula.tutor.krokEliminacie(start[i], m0[i], f) && f.rank(m0) == f.rank(start) && f.rank(m0 + start) == f.rank(start)) return null
            pravda = Pravda(e, 0, setOf(i), i to j, start[i][j], Verdikt.Stav.CHYBA, Verdikt.Typ.ZADANIE)
        }
        val ciel = u.ciel
        // the right chain first; a mistake goes on one of its steps and the rest is done again from the wrong matrix
        val right = chain(f, m0, bar, ciel) ?: return null
        val steps = ArrayList<Krok>()
        if (e == null || e == Chyba.E9 || e == Chyba.E6) steps += right
        else {
            if (right.isEmpty()) return null
            val at = r.nextInt(right.size)
            steps += right.take(at)
            val before = if (at == 0) m0 else right[at - 1].after
            val bad = pokaz(f, before, right[at], e) ?: return null
            steps += bad.first
            pravda = bad.second.copy(krok = steps.size)
            steps += chain(f, bad.first.after, bar, ciel) ?: return null
        }
        if (e != null && pravda.chyba == null && e != Chyba.E6) return null
        if (e == Chyba.E6) {
            if (steps.size < 2) return null
            val cut = 1 + r.nextInt(steps.size - 1)
            while (steps.size > cut) steps.removeAt(steps.size - 1)
            pravda = Pravda(e, steps.size, stav = Verdikt.Stav.NEDOKONCENE)
        }
        // the paper: matrices as written, the notes, a crossed-out false start now and then, sometimes only the second half
        val mats = ArrayList<PrepisMatica>()
        val notes = ArrayList<String?>()
        mats += matica(f, m0, bar)
        for ((idx, s) in steps.withIndex()) {
            if (r.nextInt(14) == 0 && idx != (pravda.krok ?: -5) - 1) {
                val wrong = s.after.mapIndexed { a, row -> if (a != s.after.size - 1) row else row.map { x -> f.add(x, Frac.ONE) } }
                mats += matica(f, wrong, bar).copy(preskrtnuta = true); notes += s.note; styly += "preskrtnute"
                notes += null
            } else notes += s.note
            mats += matica(f, s.after, bar)
        }
        var prepis = GemPrepis(mats, notes)
        if (e == null && steps.size >= 2 && r.nextInt(12) == 0) {
            // a photo of the second half: starts at a later matrix
            val from = 1 + r.nextInt(steps.size - 1)
            val keptIdx = mats.withIndex().filter { !it.value.preskrtnuta }.map { it.index }
            val cut = keptIdx[from]
            prepis = GemPrepis(mats.drop(cut), notes.drop(cut)); styly += "druha_polovica"
        }
        return Pripad(id, u, prepis, pravda, styly.toSet())
    }

    /** The cells where [m0] differs from some row order of the assignment (or its transpose) in exactly one cell. */
    private fun miestaPrepisu(f: Field, u: GemUloha, m0: List<List<Frac>>): Set<Pair<Int, Int>> {
        val a = u.start.rows.map { r -> r.map { f.norm(it) } }
        val opts = listOfNotNull(a, if (u.start.bar == null) a[0].indices.map { j -> a.map { it[j] } } else null).filter { it.size == m0.size && it[0].size == m0[0].size }
        val out = HashSet<Pair<Int, Int>>()
        fun perms(l: List<Int>): List<List<Int>> = if (l.size <= 1) listOf(l) else l.flatMap { x -> perms(l - x).map { listOf(x) + it } }
        for (t in opts) for (p in perms(t.indices.toList())) {
            val d = m0.indices.flatMap { i -> m0[i].indices.filter { j -> m0[i][j] != t[p[i]][j] }.map { i to it } }
            if (d.size == 1) out += d[0]
        }
        return out
    }

    private fun velke(x: Frac) = abs(x.num) > 400 || x.den > 60

    /** A wrong value instead of [v]: a sign, ±1, a digit off. */
    private fun zla(f: Field, v: Frac): Frac? {
        repeat(6) {
            val w = when (r.nextInt(4)) {
                0 -> -v
                1 -> v + Frac.ONE
                2 -> v - Frac.ONE
                else -> v + Frac.of(if (r.nextBoolean()) 2 else -2)
            }
            val n = f.norm(w)
            if (n != f.norm(v)) return n
        }
        return null
    }

    /** One step done wrong: the step and what the engine must report. */
    private fun pokaz(f: Field, o: List<List<Frac>>, right: Krok, e: Chyba): Pair<Krok, Pravda>? {
        val changed = o.indices.filter { right.after[it] != o[it] }
        fun cellErr(rows: List<Int>, typ: Verdikt.Typ, reduce: Boolean): Pair<Krok, Pravda>? {
            if (rows.isEmpty()) return null
            val i = rows.random(r)
            val cols = right.after[i].indices.shuffled(r)
            for (j in cols) {
                val v = right.after[i][j]
                val w = if (reduce) zlaRedukcia(f, o, right, i, j) ?: continue else zla(f, v) ?: continue
                val bad = right.after.mapIndexed { a, row -> if (a != i) row else row.mapIndexed { b, x -> if (b == j) w else x } }
                // a mistake must change the row space, else it is no mistake at all
                if (f.rank(o + listOf(bad[i])) == f.rank(o)) continue
                return Krok(right.ops, bad, right.note, right.simultaneous) to Pravda(e, 0, setOf(i), i to j, v, Verdikt.Stav.CHYBA, typ)
            }
            return null
        }
        return when (e) {
            Chyba.E1 -> cellErr(changed, Verdikt.Typ.ARITMETIKA, false)
            Chyba.E2 -> if (f !is Field.Zp) null else cellErr(changed, Verdikt.Typ.ARITMETIKA, true)
            Chyba.E3 -> cellErr(o.indices.filter { it !in changed }, Verdikt.Typ.ODPIS, false)
            Chyba.E4 -> {
                // ř_i := ř_i + ř_j and ř_j := ř_j + ř_i, both from the old rows: two equal rows
                if (o.size < 2) return null
                val (i, j) = o.indices.shuffled(r).take(2).sorted()
                val after = o.toMutableList()
                after[i] = o[i].indices.map { f.add(o[i][it], o[j][it]) }
                after[j] = o[i].indices.map { f.add(o[j][it], o[i][it]) }
                if (f.rank(after) >= f.rank(o)) return null
                val note = zapis(listOf(Combine(i, Frac.ONE, j, Frac.ONE), Combine(j, Frac.ONE, i, Frac.ONE)))
                Krok(listOf(Combine(i, Frac.ONE, j, Frac.ONE), Combine(j, Frac.ONE, i, Frac.ONE)), after, note) to
                    Pravda(e, 0, setOf(i, j), stav = Verdikt.Stav.CHYBA, typ = Verdikt.Typ.NEPOVOLENA)
            }
            Chyba.E5 -> {
                val cand = o.indices.filter { i -> !o[i].all { it.isZero } && f.rank(o.filterIndexed { a, _ -> a != i }) < f.rank(o) }
                if (cand.isEmpty()) return null
                val i = cand.random(r)
                val after = o.mapIndexed { a, row -> if (a == i) row.map { Frac.ZERO } else row }
                Krok(listOf(Scale(i, Frac.ZERO)), after, if (r.nextBoolean()) zapis(listOf(Scale(i, Frac.ZERO))) else null) to
                    Pravda(e, 0, setOf(i), stav = Verdikt.Stav.CHYBA, typ = Verdikt.Typ.NEPOVOLENA)
            }
            else -> null
        }
    }

    /** ℤ_p: a negative intermediate reduced as if positive (−3 written as 3 in ℤ_5). */
    private fun zlaRedukcia(f: Field, o: List<List<Frac>>, right: Krok, i: Int, j: Int): Frac? {
        val op = right.ops.lastOrNull { it is Combine && it.target == i } as? Combine ?: return null
        // the student thinks of the multiple as negative ("ř2 − 2·ř1"), so the raw number can be negative
        val p = (f as Field.Zp).p
        val b = if (op.b * Frac.of(2) > Frac.of(p.toLong())) op.b - Frac.of(p.toLong()) else op.b
        val raw = op.a * o[i][j] + b * o[op.src][j]
        if (raw >= Frac.ZERO || !raw.isInt) return null
        val w = f.norm(-raw)
        return w.takeIf { it != f.norm(raw) }
    }

    /* ---- the right GEM, done as a student does it ----------------------------------------------------------- */

    /** A whole right GEM from [m], step by step; null when the numbers grow beyond what anyone writes by hand. */
    private fun chain(f: Field, m: List<List<Frac>>, bar: Int?, ciel: GemUloha.Ciel): List<Krok>? {
        val out = ArrayList<Krok>()
        var cur = m
        while (true) {
            val next = gem(f, cur, bar, ciel)?.firstOrNull() ?: break
            out += next; cur = next.after
            if (out.size > 14 || cur.flatten().any { velke(it) }) return null
        }
        return out
    }

    /** The next step of a right GEM from [m] (a list of one, or empty when it is done). */
    private fun gem(f: Field, m: List<List<Frac>>, bar: Int?, ciel: GemUloha.Ciel): List<Krok>? {
        val rows = m.map { it.toMutableList() }.toMutableList()
        // HST is over the whole matrix (a row 0 … 0 | c counts); the identity only left of the bar
        val limit = if (ciel == GemUloha.Ciel.JEDNOTKOVA) (bar ?: m[0].size) else m[0].size
        var top = 0
        for (c in 0 until limit) {
            if (top >= rows.size) break
            val cand = (top until rows.size).filter { !rows[it][c].isZero }
            if (cand.isEmpty()) continue
            val below = (top + 1 until rows.size).filter { !rows[it][c].isZero }
            val pivotOk = !rows[top][c].isZero
            if (pivotOk && below.isEmpty()) { top++; continue }
            // pick a pivot row: a 1 if there is one, else any
            val ones = cand.filter { rows[it][c] == Frac.ONE || rows[it][c] == -Frac.ONE }
            val p = if (ones.isNotEmpty() && r.nextInt(3) > 0) ones.random(r) else cand.random(r)
            val ops = ArrayList<RowOp>()
            var cur = rows.map { it.toList() }
            if (p != top) {
                val sw = Swap(minOf(p, top), maxOf(p, top))
                cur = applyOps(f, cur, listOf(sw))
                // a swap on its own ∼, or together with the eliminations below it
                if (r.nextBoolean()) return listOf(Krok(listOf(sw), cur, zapisMaybe(listOf(sw)), simultaneous = false))
                ops += sw
            }
            // sometimes first make the pivot 1 (G2) or flip a sign
            if (r.nextInt(8) == 0 && cur[top][c] != Frac.ONE && (f is Field.Zp || cur[top].all { (it / cur[top][c]).den <= 3 })) {
                val sc = Scale(top, f.inv(cur[top][c]))
                val after = applyOps(f, cur, listOf(sc))
                return listOf(Krok(ops + sc, after, zapisMaybe(ops + sc), simultaneous = false))
            }
            val elim = ArrayList<RowOp>()
            for (i in top + 1 until cur.size) {
                val q = cur[i][c]
                if (q.isZero) continue
                val pv = cur[top][c]
                val ratio = f.div(q, pv)
                elim += when {
                    f is Field.Zp -> Combine(i, Frac.ONE, top, f.neg(ratio))
                    ratio.isInt || r.nextInt(3) == 0 -> Combine(i, Frac.ONE, top, -ratio)
                    // keep integers: p·ř_i − q·ř_top
                    else -> { val pa = if (pv < Frac.ZERO) -pv else pv; Combine(i, pa, top, if (pv < Frac.ZERO) q else -q) }
                }
            }
            if (elim.isEmpty()) {
                if (ops.isNotEmpty()) return listOf(Krok(ops, cur, zapisMaybe(ops), simultaneous = false))
                top++; continue
            }
            // all eliminations at once, or only some of them in this ∼
            val now = if (elim.size > 1 && r.nextInt(4) == 0) elim.take(1 + r.nextInt(elim.size - 1)) else elim
            val all = ops + now
            val after = applyOps(f, cur.let { if (ops.isEmpty()) rows.map { it.toList() } else rows.map { it.toList() } }, all)
            return listOf(Krok(all, after, zapisMaybe(all), simultaneous = ops.isEmpty()))
        }
        if (ciel == GemUloha.Ciel.JEDNOTKOVA) {
            val n = bar ?: return emptyList()
            val cur = rows.map { it.toList() }
            for (p in cur.indices.reversed()) {
                val c = cur[p].indexOfFirst { !it.isZero }.takeIf { it in 0 until n } ?: continue
                if (cur[p][c] != Frac.ONE) { val sc = Scale(p, f.inv(cur[p][c])); return listOf(Krok(listOf(sc), applyOps(f, cur, listOf(sc)), zapisMaybe(listOf(sc)))) }
                val ops = (0 until p).filter { !cur[it][c].isZero }.map { Combine(it, Frac.ONE, p, f.neg(cur[it][c])) }
                if (ops.isNotEmpty()) return listOf(Krok(ops, applyOps(f, cur, ops), zapisMaybe(ops)))
            }
        }
        return emptyList()
    }

    /** A swap first (on the rows as they are), then the rest from the rows after the swap: how one ∼ reads on paper. */
    private fun applyOps(f: Field, o: List<List<Frac>>, ops: List<RowOp>): List<List<Frac>> {
        var rows = o
        val swaps = ops.filterIsInstance<Swap>()
        for (s in swaps) rows = rows.toMutableList().also { val t = it[s.i]; it[s.i] = it[s.j]; it[s.j] = t }
        val base = rows
        val out = rows.toMutableList()
        for (op in ops) when (op) {
            is Scale -> out[op.i] = base[op.i].map { f.mul(op.k, it) }
            is Combine -> out[op.target] = base[op.target].indices.map { j -> f.add(f.mul(op.a, base[op.target][j]), f.mul(op.b, base[op.src][j])) }
            else -> {}
        }
        return out
    }

    /* ---- how it looks on paper ------------------------------------------------------------------------------ */

    private fun zapisMaybe(ops: List<RowOp>): String? = if (r.nextInt(100) < 35) { styly += "bez_uprav"; null } else zapis(ops)

    private fun zapis(ops: List<RowOp>): String {
        val style = r.nextInt(6)
        styly += "zapis$style"
        fun row(i: Int) = when (style) {
            1 -> listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII")[i]
            2 -> "R${i + 1}"
            3 -> "r${i + 1}"
            else -> "ř${i + 1}"
        }
        val minus = if (style == 0 || style == 4) "−" else "-"
        fun k(x: Frac) = if (x.isInt) "${x.num}" else "${x.num}/${x.den}"
        val items = ops.map { op ->
            when (op) {
                is Swap -> "${row(op.i)}↔${row(op.j)}"
                is Scale -> if (op.k == -Frac.ONE) "${minus}${row(op.i)}" else if (style == 3) "${row(op.i)}·(${k(op.k)})" else "${k(op.k)}${row(op.i)}"
                is Combine -> {
                    val first = (if (op.a == Frac.ONE) "" else k(op.a)) + row(op.target)
                    val sign = if (op.b < Frac.ZERO) minus else "+"
                    val bb = if (op.b < Frac.ZERO) -op.b else op.b
                    val second = (if (bb == Frac.ONE) "" else k(bb)) + row(op.src)
                    if (style == 5) "${row(op.target)} := $first $sign $second" else "$first $sign $second"
                }
                else -> "?"
            }
        }
        val kind = if (style == 4) "(G${ops.map { when (it) { is Swap -> 1; is Scale -> 2; else -> 3 } }.distinct().joinToString(", G")}) " else ""
        return kind + items.joinToString(", ")
    }

    private fun matica(f: Field, m: List<List<Frac>>, bar: Int?): PrepisMatica {
        val zp = f is Field.Zp
        val unreduced = zp && r.nextInt(10) == 0
        val unicode = r.nextBoolean()
        val tex = r.nextInt(5) == 0
        if (unreduced) styly += "nezredukovane"
        return PrepisMatica(m.map { row ->
            row.map { x ->
                val v = if (unreduced && r.nextInt(3) == 0) x + Frac.of((f as Field.Zp).p.toLong()) else x
                val s = when {
                    v.isInt -> "${v.num}"
                    tex -> (if (v.num < 0) "-" else "") + "\\frac{${abs(v.num)}}{${v.den}}"
                    else -> "${v.num}/${v.den}"
                }
                Citanie(if (unicode) s.replace("-", "−") else s)
            }
        }, ciara = bar)
    }
}

/** Does the verdict say exactly what the truth says? (K1: the matrix and the rows; cells and kinds are scored apart.) */
data class Hodnotenie(val ok: Boolean, val bunkaOk: Boolean?, val typOk: Boolean, val dovod: String, val pravdepodobnaOk: Boolean? = null)

fun ohodnot(p: Pripad, v: Verdikt): Hodnotenie {
    val t = p.pravda
    if (t.chyba == null) {
        val ok = v.stav == Verdikt.Stav.BEZ_CHYBY
        return Hodnotenie(ok, null, ok, if (ok) "" else "falošný poplach: ${v.stav} krok ${v.krok} riadky ${v.riadky} ${v.poznamky}")
    }
    if (t.stav == Verdikt.Stav.NEDOKONCENE) {
        val ok = v.stav == Verdikt.Stav.NEDOKONCENE
        return Hodnotenie(ok, null, ok, if (ok) "" else "čakal som NEDOKONCENE, je ${v.stav} krok ${v.krok}")
    }
    val krokOk = v.stav == Verdikt.Stav.CHYBA && v.krok == t.krok && v.riadky == t.riadky
    // a reported cell must be the right one with the right value; not reporting one is allowed (only the row)
    fun sedi() = v.bunky.size == 1 && v.bunky[0].miesto.riadok == t.bunka!!.first && v.bunky[0].miesto.stlpec == t.bunka.second && v.bunky[0].maloByt == t.maloByt
    // a certain cell must be right; a likely one is scored apart ([pravdepodobnaOk])
    val bunkaOk = if (v.bunky.isEmpty() || t.bunka == null || !v.bunky.all { it.iste }) null else sedi()
    val pravdepodobnaOk = if (v.bunky.isEmpty() || t.bunka == null || v.bunky.all { it.iste }) null else sedi()
    val typOk = v.typ == t.typ
    val ok = krokOk && bunkaOk != false
    val start = " | zadanie ${p.uloha.start.rows} papier ${p.prepis.matice.firstOrNull()?.riadky?.map { r -> r.map { it.v } }}"
    return Hodnotenie(ok, bunkaOk, typOk, pravdepodobnaOk = pravdepodobnaOk, dovod = if (ok) "" else start + " čakal som ${t.chyba} krok ${t.krok} riadky ${t.riadky} bunka ${t.bunka}=${t.maloByt}; je ${v.stav} krok ${v.krok} riadky ${v.riadky} bunky ${v.bunky.map { "${it.miesto.riadok},${it.miesto.stlpec}=${it.maloByt}" }} ${v.poznamky}")
}

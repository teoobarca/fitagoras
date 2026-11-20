package dev.hamper.tabula.tutor

import dev.hamper.tabula.la.Combine
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.la.Scale
import dev.hamper.tabula.la.echelon

/*
 * What the student probably did, from the numbers alone. The lessons' feedback on a wrong answer says "you probably did
 * X" only when X gives exactly the answer typed (data/Diagnose.kt); this is the same for one cell of a GEM: a slip is
 * named only when it reproduces the written number and no other common slip does. The engine computes it, the
 * explanation only says it ("asi si…"), and it stays a guess in the facts' own words.
 */

private fun Frac.t() = "$" + tex(true) + "$"
private fun Frac.p() = if (this < Frac.ZERO) "(${tex(true)})" else tex(true)

/** ℤp's numbers as people write them in an operation: 4 in ℤ5 is "−1". */
private fun signed(f: Field, b: Frac): Frac = if (f is Field.Zp && b.isInt && b.num * 2 > f.p) b - Frac.of(f.p.toLong()) else b

/**
 * One wrong cell computed by a combination (row r := a·ř_r + b·ř_s): the slip that gives the written number, or null.
 * [o] the matrix before the step, [n] the student's matrix after it.
 */
fun diagnozaBunky(f: Field, o: List<List<Frac?>>, n: List<List<Frac?>>, ops: List<RowOp>?, b: ChybnaBunka): String? {
    val r = b.miesto.riadok; val c = b.miesto.stlpec
    val mine = ops?.filter { (it is Combine && it.target == r) || (it is Scale && it.i == r) } ?: return null
    val op = mine.singleOrNull() as? Combine ?: return null
    val x = o.getOrNull(r)?.getOrNull(c) ?: return null
    val y = o.getOrNull(op.src)?.getOrNull(c) ?: return null
    val a = op.a
    val k = signed(f, op.b)
    if (f.norm(a * x + k * y) != b.maloByt) return null
    val w = b.napisane
    val first = if (a == Frac.ONE) x.p() else "${a.p()}\\cdot ${x.p()}"
    fun term(sign: Boolean, m: Frac, v: Frac = y) = (if (sign) "+" else "-") + (if (m == Frac.ONE) v.p() else "${m.tex(true)}\\cdot ${v.p()}")
    val kAbs = if (k < Frac.ZERO) -k else k
    val zp = f is Field.Zp
    fun rovno(raw: Frac) = if (zp && raw != f.norm(raw)) "=${raw.tex(true)}\\equiv ${f.norm(raw).tex(true)}\\pmod{${(f as Field.Zp).p}}" else "=${raw.tex(true)}"
    val pribeh = ArrayList<Pair<Frac, String>>()
    // the multiple with the other sign: the commonest slip in (G3)
    if (!(k * y).isZero && (!zp || k == op.b)) {
        val raw = a * x - k * y
        pribeh += f.norm(raw) to (if (k < Frac.ZERO) "v tomto stĺpci násobok ${op.src + 1}. riadku pripočítaný namiesto odčítaný" else "v tomto stĺpci násobok ${op.src + 1}. riadku odčítaný namiesto pripočítaný") +
            ": $" + first + term(k < Frac.ZERO, kAbs) + rovno(raw) + "$"
    }
    // the row added without its multiple
    if (kAbs != Frac.ONE && !y.isZero) {
        val raw = a * x + (if (k < Frac.ZERO) -y else y)
        pribeh += f.norm(raw) to "${op.src + 1}. riadok ${if (k < Frac.ZERO) "odčítaný" else "pripočítaný"} bez násobku ${kAbs.t()}: $" + first + term(k > Frac.ZERO, Frac.ONE) + rovno(raw) + "$"
    }
    // ℤp: a negative number reduced as if it were positive (−3 written as 3 in ℤ5)
    if (zp) {
        val raw = a * x + k * y
        if (raw < Frac.ZERO && raw.isInt && f.norm(-raw) != f.norm(raw))
            pribeh += f.norm(-raw) to "záporné číslo zredukované, akoby bolo kladné: $" + first + term(k > Frac.ZERO, kAbs) + "=${raw.tex(true)}$, a ${raw.t()} v $\\mathbb{Z}_{${(f as Field.Zp).p}}$ je ${f.norm(raw).t()}, nie ${f.norm(-raw).t()}"
    }
    // the other row already changed in this same step, used instead of the old one
    val y2 = n.getOrNull(op.src)?.getOrNull(c)
    if (y2 != null && y2 != y && n.getOrNull(op.src) != o.getOrNull(op.src)) {
        val raw = a * x + k * y2
        pribeh += f.norm(raw) to "použitý ${op.src + 1}. riadok už po jeho úprave v tom istom kroku (v ${c + 1}. stĺpci ${y2.t()} namiesto ${y.t()}): $" +
            first + term(k > Frac.ZERO, kAbs, y2) + rovno(raw) + "$"
    }
    // the number left as it was
    if (x != b.maloByt) pribeh += f.norm(x) to "číslo v ${c + 1}. stĺpci ponechané z predošlej matice (${x.t()}), akoby sa tam nič nepočítalo"
    val hits = pribeh.filter { it.first == w }.map { it.second }.distinct()
    return hits.singleOrNull()
}

/**
 * A row that should have been copied or a cell of the assignment copied wrong: the slip that gives the written number
 * when only one does. A number that merely equals some other cell is no story: small numbers coincide too often.
 */
fun diagnozaOdpisu(b: ChybnaBunka): String? =
    if (!b.napisane.isZero && b.napisane == -b.maloByt) "správne číslo ${b.maloByt.t()} s opačným znamienkom" else null

/** Nonzero rows, pivot columns (left of [bar]) and whether the right-hand side is a pivot, of the echelon form of [m]. */
data class Struktura(val riadky: Int, val hlavne: List<Int>, val pravaHlavna: Boolean)

fun struktura(f: Field, m: List<List<Frac>>, bar: Int?, lenLavy: Boolean): Struktura {
    val limit = bar ?: m[0].size
    val e = f.echelon(dev.hamper.tabula.la.Mat(m, bar), if (lenLavy) limit else m[0].size)
    val left = e.pivots.filter { it < limit }
    return Struktura(if (lenLavy) left.size else e.pivots.size, left, bar != null && !lenLavy && e.pivots.any { it >= limit })
}

/** The same, read straight off a matrix already in HST (its own first nonzero elements). */
fun strukturaHst(m: List<List<Frac?>>, bar: Int?, lenLavy: Boolean): Struktura {
    val limit = bar ?: (m.firstOrNull()?.size ?: 0)
    val lead = m.mapNotNull { r -> r.indexOfFirst { it != null && !it.isZero }.takeIf { it >= 0 } }
    val left = lead.filter { it < limit }
    return Struktura(if (lenLavy) left.size else lead.size, left, bar != null && !lenLavy && lead.any { it >= limit })
}

package dev.hamper.tabula.screens

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.GemFrame
import dev.hamper.tabula.la.GemMove
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.la.Nenapisana
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.tutor.Verdikt

/*
 * The engine's verdict on a photographed GEM as the app's own steppers (the tutor's panel draws them, TutorPanel.kt):
 * the student's chain as read, and the step done right.
 */

/** The student's chain as a walk: their matrices as read, the operations the engine understood (none: an empty ∼). */
fun studentovPostup(v: Verdikt): GemWalk? {
    if (v.matice.isEmpty()) return null
    val n = v.matice[0].firstOrNull()?.size ?: return null
    if (v.matice.any { m -> m.any { it.size != n } || m.size != v.matice[0].size }) return null
    fun frame(m: List<List<Frac?>>) = GemFrame(m.map { r -> r.map { it?.tex(true) ?: "?" } }, v.ciara)
    val moves = (1 until v.matice.size).map { k ->
        val ops: List<RowOp> = v.upravy.getOrNull(k - 1)?.takeIf { it.isNotEmpty() } ?: listOf(Nenapisana())
        // one kind per ∼ in the course's notation; a mix the engine inferred keeps the first kind as its label
        GemMove(ops, frame(v.matice[k]))
    }
    return GemWalk(frame(v.matice[0]), moves)
}

/** The step done right: from the last right matrix with the student's (or the engine's) operations. */
fun spravnyKrok(v: Verdikt): GemWalk? {
    val right = v.spravne ?: return null
    val k = v.krok ?: return null
    val from = if (v.stav == Verdikt.Stav.NEDOKONCENE) v.matice.getOrNull(k) else v.matice.getOrNull(k - 1)
    from ?: return null
    if (v.stav == Verdikt.Stav.CHYBA && v.typ == Verdikt.Typ.ZADANIE) return null
    fun frame(m: List<List<Frac?>>) = GemFrame(m.map { r -> r.map { it?.tex(true) ?: "?" } }, v.ciara)
    val ops = v.spravneUpravy?.takeIf { it.isNotEmpty() } ?: listOf(Nenapisana())
    return GemWalk(frame(from), listOf(GemMove(ops, frame(right))))
}

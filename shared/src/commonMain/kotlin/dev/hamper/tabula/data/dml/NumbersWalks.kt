package dev.hamper.tabula.data.dml.numbers

import dev.hamper.tabula.data.Terms
import dev.hamper.tabula.la.Align
import dev.hamper.tabula.la.TableStep
import dev.hamper.tabula.la.TableWalk

/*
 * Euclid's algorithm and the REA as tables that grow step by step (la/TableWalk.kt), computed from rea(): every worked
 * example and every solution shows them through the one component, never as typed TeX (docs/CONTENT.md, "Vzorový
 * príklad počíta engine").
 */

private fun s(n: Long) = if (n < 0) "-${-n}" else "$n"
private fun par(n: Long) = if (n < 0) "(${s(n)})" else s(n)

/**
 * Euclid's divisions of Příklad 6.1, one equation a = q·b + r per row, aligned: the divisor and the remainder move to
 * the next row as its dividend and divisor (Věta 6.3). The first division is shown, each step adds the next one; the
 * end frames the last nonzero remainder, the gcd.
 */
fun euclidWalk(t: Rea): TableWalk {
    val r = t.rows
    val rows = (2 until r.size).map { i -> listOf(s(r[i - 2].r), "=", s(r[i - 1].q!!), "\\cdot", s(r[i - 1].r), "+", s(r[i].r)) }
    val steps = (1 until rows.size).map { j ->
        val i = j + 2
        TableStep(listOf("q_{${j + 2}}=\\lfloor ${r[i - 2].r}/${r[i - 1].r}\\rfloor=${r[i - 1].q}"), sources = listOf(j - 1),
            slides = listOf((j - 1 to 4) to (j to 0), (j - 1 to 6) to (j to 4)))
    }
    val g = t.gcd
    return TableWalk(null, rows, listOf(Align.End, Align.Center, Align.End, Align.Center, Align.End, Align.Center, Align.End), start = 1, steps = steps,
        // the last nonzero remainder; with a single division (b divides a) that is b itself, the divisor
        result = setOf(if (rows.size == 1) 0 to 4 else rows.size - 2 to 6), resultLabel = listOf("\\gcd(${t.a},${t.b})=$g"), quiet = setOf(1, 3, 5),
        startLabel = listOf("q_2=\\lfloor ${r[0].r}/${r[1].r}\\rfloor=${r[1].q}"))
}

/**
 * The REA table as the skriptá write it (Poznámka 6.2, kurz/dml/kap6 C): r_i | α_i β_i | q_i, the row number i small in
 * the margin. A step takes q from the row above (marked), subtracts its multiple of that row from the one two above
 * (ř8=ř6-4·ř7) and writes the new row with its own q_i=⌊r_{i-1}/r_i⌋, used by the next step. The end frames the row of
 * the gcd. [swap]: the coefficients in the order of an equation bx + ay (Příklad 6.5: α at the smaller number). Teo, 17. 11.
 */
fun reaWalk(t: Rea, swap: Boolean = false, back: String? = null): TableWalk {
    val rows = t.rows.map { w ->
        val (x, y) = if (swap) w.beta to w.alpha else w.alpha to w.beta
        if (w.r == 0L) listOf("0", "", "", "") else listOf(s(w.r), s(x), s(y), w.q?.let { "$it" } ?: "")
    }
    val steps = (2 until t.rows.size).map { i ->
        val n = i + 1; val q = t.rows[i - 1].q!!; val w = t.rows[i]
        // the whole row (r, α and β alike) in the app's notation for a row operation, with q from the row above;
        // then the new row's own q (or the end at zero)
        val next = if (w.r == 0L) "r_{$n}=0" else "q_{$n}=\\lfloor ${t.rows[i - 1].r}/${w.r}\\rfloor=${w.q}"
        TableStep(listOf("\\text{ř}$n=\\text{ř}${n - 2}-$q\\cdot\\text{ř}${n - 1}", next), sources = listOf(i - 2, i - 1), marks = setOf(i - 1 to 3))
    }
    val k = t.rows.size - 2
    val (first, second) = if (swap) t.b to t.a else t.a to t.b
    val (c1, c2) = if (swap) t.beta to t.alpha else t.alpha to t.beta
    return TableWalk(listOf("r_i", "\\alpha_i", "\\beta_i", "q_i"), rows, listOf(Align.End, Align.End, Align.End, Align.End), rules = setOf(1, 3),
        start = 2, steps = steps, result = setOf(k to 0, k to 1, k to 2),
        resultLabel = listOfNotNull("${t.gcd}=${par(c1)}\\cdot $first+${par(c2)}\\cdot $second", back),
        startLabel = listOf("r_i=\\alpha_i\\cdot $first+\\beta_i\\cdot $second", "q_2=\\lfloor ${t.rows[0].r}/${t.rows[1].r}\\rfloor=${t.rows[1].q}"),
        rowLabels = t.rows.indices.map { "${it + 1}" })
}

/**
 * Binary powering of Příklad 6.16 as a table: j | b^{2^j} mod m | a_j, each row the square of the one above reduced
 * mod m; the end frames the rows of the 1-bits and multiplies them. Dombek lectured it on the same example
 * (9. prednáška, 23. 11. 2026); used by the lesson dml-tc-mocnenie and every solution whose remaining power is too big
 * to multiply out one factor at a time.
 */
fun powWalk(b: Long, e: Long, m: Long): TableWalk {
    require(e >= 1) { "binárne mocnenie má zmysel pre exponent aspoň 1" }
    val sq = squares(b, e, m)
    val bb = par(b) // (-3)^2, not -3^2
    val bits = sq.indices.map { j -> (e shr j) and 1L }
    val rows = sq.mapIndexed { j, (k, v) -> listOf("$j", Terms.pow(bb, k.toString()), "$v", "${bits[j]}") }
    val steps = (1 until sq.size).map { j ->
        val prev = sq[j - 1].second
        TableStep(listOf("${prev}^2=${prev * prev}\\equiv ${sq[j].second}\\pmod{$m}"), sources = listOf(j - 1))
    }
    val ones = sq.indices.filter { bits[it] == 1L }
    val product = ones.joinToString("\\cdot ") { "${sq[it].second}" }
    return TableWalk(listOf("j", "b^{2^j}", "\\bmod $m", "a_j"), rows, listOf(Align.End, Align.End, Align.End, Align.End), rules = setOf(1, 3),
        start = 1, steps = steps, result = ones.map { it to 2 }.toSet() + ones.map { it to 3 },
        resultLabel = listOf("$bb^{$e}\\equiv $product\\equiv ${powMod(b, e, m)}\\pmod{$m}"))
}

package dev.hamper.tabula.tutor

import dev.hamper.tabula.la.Combine
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.la.Scale
import dev.hamper.tabula.la.Swap

/*
 * What a student writes on paper, read into exact values: a matrix cell ("−3", "4/3", "\frac{1}{2}", "6" in Z_5) and the
 * row operations under a "∼" ("(G3) ř2−2ř1, ř3−3ř1", "II−2I", "R2-2R1", "2ř3+ř1", "ř2↔ř3", "ř2·(−1)"). Both never throw:
 * whatever can't be read is null, and the checker treats it as unknown, never as a mistake.
 */

/** A cell as written, in the field; null when it is not a number we can read (a letter, an empty cell, a smudge). */
fun bunka(s: String, pole: Field): Frac? {
    val t = normalizuj(s)
    if (t.isEmpty()) return null
    val f = zlomok(t) ?: return null
    return runCatching { pole.norm(f) }.getOrNull()
}

private fun normalizuj(s: String): String = s.trim()
    .replace('−', '-').replace('–', '-').replace('—', '-').replace('﹣', '-').replace('－', '-')
    .replace("\\left", "").replace("\\right", "").replace("\\,", "").replace("\\!", "").replace("\\ ", "")
    .replace(" ", "").replace("$", "")
    .removePrefix("+")

private val fracTex = Regex("^(-?)\\\\[dt]?frac\\{(-?\\d+)\\}\\{(-?\\d+)\\}$")
private val plain = Regex("^-?\\d+$")
private val slash = Regex("^(-?\\d+)/(-?\\d+)$")
private val paren = Regex("^\\((.*)\\)$")
private val decimal = Regex("^(-?)(\\d*)[.,](\\d+)$")

private fun zlomok(t0: String): Frac? = runCatching {
    var t = t0
    paren.matchEntire(t)?.let { t = it.groupValues[1] }
    if (t.length > 24) return null
    when {
        plain.matches(t) -> Frac.of(t.toLong())
        slash.matches(t) -> slash.matchEntire(t)!!.groupValues.let { Frac.of(it[1].toLong(), it[2].toLong()) }
        fracTex.matches(t) -> fracTex.matchEntire(t)!!.groupValues.let { g -> Frac.of(it(g[2]) * (if (g[1] == "-") -1 else 1), it(g[3])) }
        decimal.matches(t) -> decimal.matchEntire(t)!!.groupValues.let { g ->
            val den = (1..g[3].length).fold(1L) { a, _ -> a * 10 }
            val n = (g[2].ifEmpty { "0" }.toLong() * den + g[3].toLong()) * (if (g[1] == "-") -1 else 1)
            Frac.of(n, den)
        }
        else -> null
    }
}.getOrNull()

private fun it(s: String) = s.toLong()

/* ---- Row operations ---------------------------------------------------------------------------------------------- */

private val roman = listOf("X" to 10, "IX" to 9, "VIII" to 8, "VII" to 7, "VI" to 6, "V" to 5, "IV" to 4, "III" to 3, "II" to 2, "I" to 1)

/** One term of an operation: coefficient × row (rows from 0). */
private data class Clen(val k: Frac, val riadok: Int)

/**
 * The operations of one "∼" as written; null when any part can't be read (then the step is checked without them). An
 * empty note is an empty list. Rows on paper count from 1, the result from 0.
 */
fun upravy(note: String?, m: Int): List<RowOp>? {
    if (note == null) return emptyList()
    var t = note.replace("\\text{ř}", "ř").replace("\\text{r}", "ř").replace("\\cdot", "·").replace("\\leftrightarrow", "↔")
        .replace("\\to", "→").replace("\\rightarrow", "→").replace("\\sim", "").replace("~", "").replace("∼", "")
        .replace('−', '-').replace('–', '-').replace('*', '·').replace("<->", "↔").replace("<>", "↔").replace("$", "")
        .replace("\\", "").replace("{", "").replace("}", "")
    // the kind of operation, "(G3)", says nothing the operations don't
    t = t.replace(Regex("\\(\\s*G\\s*[123](\\s*,\\s*G\\s*[123])*\\s*\\)"), " ").replace(Regex("\\bG[123]\\b"), " ")
    if (t.isBlank()) return emptyList()
    val out = ArrayList<RowOp>()
    for (raw in t.split(',', ';', '\n').map { it.trim() }.filter { it.isNotEmpty() }) {
        out += jedna(raw, m) ?: return null
    }
    return out
}

private fun jedna(s0: String, m: Int): RowOp? {
    val s = s0.replace(" ", "")
    if ('↔' in s || "<=>" in s) {
        val (a, b) = s.split('↔', limit = 2).takeIf { it.size == 2 } ?: return null
        val i = riadok(a, m) ?: return null; val j = riadok(b, m) ?: return null
        return if (i != j) Swap(minOf(i, j), maxOf(i, j)) else null
    }
    var target: Int? = null
    var expr = s
    for (sep in listOf(":=", "←", "→", "=")) if (sep in s) {
        val (l, r) = s.split(sep, limit = 2)
        // "ř2 := ř2 − 2ř1" names the row on the left; "ř2 − 2ř1 → ř2" on the right
        val li = riadok(l, m); val ri = riadok(r, m)
        if (sep == "→" && ri != null && li == null) { target = ri; expr = l } else { target = li ?: return null; expr = r }
        break
    }
    val terms = cleny(expr, m) ?: return null
    if (terms.isEmpty() || terms.size > 2) return null
    val t = target ?: terms[0].riadok
    val own = terms.filter { it.riadok == t }
    val other = terms.filter { it.riadok != t }
    if (own.size > 1 || other.size > 1) return null
    return when {
        other.isEmpty() -> own.firstOrNull()?.let { if (it.k.isZero) Scale(t, Frac.ZERO) else Scale(t, it.k) }
        own.isEmpty() -> null // "ř2 := ř1" is not a row operation
        else -> Combine(t, own[0].k, other[0].riadok, other[0].k)
    }
}

/** "2ř1", "-ř3", "1/2·ř2", "ř2·(-1)", "ř2/3", "-2II": terms with their signs. */
private fun cleny(e: String, m: Int): List<Clen>? {
    if (e.isEmpty()) return null
    val parts = ArrayList<String>()
    var cur = StringBuilder()
    var depth = 0
    for ((i, ch) in e.withIndex()) {
        if (ch == '(') depth++
        if (ch == ')') depth--
        if ((ch == '+' || ch == '-') && depth == 0 && i > 0 && cur.isNotEmpty() && cur.last() !in "·/(") { parts += cur.toString(); cur = StringBuilder() }
        cur.append(ch)
    }
    parts += cur.toString()
    return parts.map { clen(it, m) ?: return null }
}

private val rowToken = Regex("(ř|Ř|r|R)_?(\\d+)|([IVX]+)")

private fun clen(p0: String, m: Int): Clen? {
    var p = p0
    var sign = Frac.ONE
    while (p.startsWith("+") || p.startsWith("-")) { if (p[0] == '-') sign = -sign; p = p.substring(1) }
    val match = rowToken.find(p) ?: return null
    val row = riadok(match.value, m) ?: return null
    val before = p.substring(0, match.range.first).trimEnd('·', '(').trimStart('(')
    var after = p.substring(match.range.last + 1).trimStart(')')
    var k = if (before.isEmpty()) Frac.ONE else (koef(before) ?: return null)
    if (after.isNotEmpty()) {
        when {
            after.startsWith("·") -> k *= (koef(after.removePrefix("·")) ?: return null)
            after.startsWith("/") -> { val d = koef(after.removePrefix("/")) ?: return null; if (d.isZero) return null; k /= d }
            else -> return null
        }
        after = ""
    }
    return Clen(sign * k, row)
}

private fun koef(s: String): Frac? = zlomok(s.removeSurrounding("(", ")").replace("·", ""))

/** "ř2", "r2", "R_2", "II" → 1 (rows from 0); null when it isn't a row of an m-row matrix. */
private fun riadok(s: String, m: Int): Int? {
    val t = s.trim().trim('(', ')')
    Regex("^(ř|Ř|r|R)_?(\\d+)$").matchEntire(t)?.let { val i = (it.groupValues[2].toIntOrNull() ?: return null) - 1; return i.takeIf { it in 0 until m } }
    if (t.isNotEmpty() && t.all { it in "IVX" }) {
        var rest = t; var v = 0
        for ((r, n) in roman) while (rest.startsWith(r)) { v += n; rest = rest.removePrefix(r) }
        return if (rest.isEmpty()) (v - 1).takeIf { it in 0 until m } else null
    }
    return null
}

package dev.hamper.tabula.math

import dev.hamper.tabula.data.Symbol

/*
 * Reading markup in content text (after § became $):
 *   $$tex$$   the key formula of a statement, on its own centered line
 *   ==word==  a word that decides true or false on a test, quietly marked
 * and inside TeX: \ca{P} \cb{Q} \cc{R} the named objects of a statement, \tap{k}{...} a tappable symbol (set by the app,
 * never by content). Nothing here changes a word of the text.
 */

sealed interface Seg {
    data class Text(val s: String) : Seg
    data class Display(val tex: String) : Seg
}

private val display = Regex("""\$\$(.+?)\$\$""")
private val tail = Regex("""^[.,;:]+(?=\s|$)""")

/**
 * The text split at its display formulas. Punctuation right after a display formula ends that formula, as in print,
 * so no line starts with a lone period. Empty text around is dropped.
 */
fun segments(text: String): List<Seg> {
    val out = ArrayList<Seg>()
    var at = 0
    for (m in display.findAll(text)) {
        text.substring(at, m.range.first).trim().takeIf { it.isNotEmpty() }?.let { out += Seg.Text(it) }
        var tex = m.groupValues[1].trim()
        at = m.range.last + 1
        tail.find(text.substring(at))?.let { p -> tex += "\\text{${p.value}}"; at += p.value.length }
        out += Seg.Display(tex)
    }
    text.substring(at).trim().takeIf { it.isNotEmpty() }?.let { out += Seg.Text(it) }
    return out
}


/** The text without reading markup, for places that show it plain (a screen reader, a search, a log). */
fun plainMarkup(s: String): String = s.replace("$$", "$").replace("§§", "§").replace("==", "")

/**
 * Wraps the first match of each symbol in [tex] as `\tap{k}{...}`, for the symbols [take] grants (the first
 * occurrence on a screen). Overlapping matches are skipped; a match that would cut a brace group is skipped too.
 */
fun tapSymbols(tex: String, symbols: List<Symbol>, take: (Int) -> Boolean): String {
    if (symbols.isEmpty()) return tex
    val hits = ArrayList<Pair<Int, IntRange>>()
    symbols.forEachIndexed { k, s ->
        val m = s.regex.find(tex) ?: return@forEachIndexed
        if (m.range.isEmpty() || !balanced(m.value) || cutsName(tex, m.range)) return@forEachIndexed
        if (hits.any { (_, r) -> r.first <= m.range.last && m.range.first <= r.last }) return@forEachIndexed
        if (!take(k)) return@forEachIndexed
        hits += k to m.range
    }
    if (hits.isEmpty()) return tex
    val sb = StringBuilder(tex)
    for ((k, r) in hits.sortedByDescending { it.second.first }) {
        sb.insert(r.last + 1, "}")
        sb.insert(r.first, "\\tap{$k}{")
    }
    return sb.toString()
}

/** True if [r] starts or ends inside a command or a word ("\\ssub" inside "\\ssubx", "T" inside "\\Theta"). */
private fun cutsName(tex: String, r: IntRange): Boolean {
    val before = tex.getOrNull(r.first - 1); val first = tex[r.first]
    val last = tex[r.last]; val after = tex.getOrNull(r.last + 1)
    if (first.isLetter() && before != null && (before.isLetter() || before == '\\')) return true
    return last.isLetter() && after != null && after.isLetter()
}

private fun balanced(s: String): Boolean {
    var d = 0
    var i = 0
    while (i < s.length) {
        when (s[i]) {
            '\\' -> i++ // \{ and \} are characters, not groups
            '{' -> d++
            '}' -> { d--; if (d < 0) return false }
        }
        i++
    }
    return d == 0
}

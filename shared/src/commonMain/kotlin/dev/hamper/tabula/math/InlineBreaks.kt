package dev.hamper.tabula.math

import dev.hamper.tabula.compat.sortedSetOf

/**
 * One item of a formula row as the line breaker sees it: its class, its glyph when it is a single symbol ("(", ",",
 * "∈"), whether it is a gap (\quad, \ ), whether it is a sized delimiter (\big\{, which the engine classes as ordinary).
 */
internal data class Tok(val cls: Cls, val s: String = "", val gap: Boolean = false, val sized: Boolean = false)

/** The token of a \pmod (the line breaker may break before it). */
internal const val PMOD = "\\pmod"

private val opens = setOf("(", "[", "{", "⟨", "⌊", "⌈")
private val closes = setOf(")", "]", "}", "⟩", "⌋", "⌉")
private fun Tok.isOpen() = cls == Cls.Open || (sized && s in opens)
private fun Tok.isClose() = cls == Cls.Close || (sized && s in closes)

/**
 * Where a long inline formula may wrap: index i means "between item i and item i + 1". The rules:
 * - after a relation (=, ⊂, ≤, ⇒) or a binary operation (+, −, ⋅), but **before** ∈ and ∉, so "∈ ℤ₂⁴" stays one unit;
 * - after a comma between the items of the formula itself ("x, y, z"), of a set {…}, of a span ⟨…⟩ and of a list of
 *   tuples ((…), (…)), and only when every bracket around it allows breaks as well;
 * - after the commas of a tuple of expressions ("(α+4β, 2α+β)"), never inside one of its entries;
 * - between two brackets side by side, as in the quantifiers "(∀x ∈ T)(∃y ∈ T)";
 * - never inside a vector (a bracket with plain entries, "(1, 0, 1, 0)"), and never inside a fraction, a fence, a
 *   matrix or a braced group, which reach the breaker as single items;
 * - after a gap rather than before it, so a new line does not start with a space.
 * A minus that is a sign is ordinary by the time it gets here, so it never breaks.
 */
internal fun inlineBreaks(toks: List<Tok>): List<Int> = inlineBreakKinds(toks).keys.toList()

/**
 * The breaks of [inlineBreaks] with their strength: true for a weak one, after a binary operation (+, −, ⋅) or between
 * two brackets of a product; false for a strong one, after a relation or a comma, before ∈, or between the brackets of
 * quantifiers ((∀x ∈ T)(∃y ∈ T)). A line breaks at a
 * weak point only where a strong piece alone is wider than the line (docs/ZASADY.md, bod 11: a sum is not torn apart
 * when the formula can break at its = instead).
 */
/** Logical connectives and the bar of a set builder: they bind weaker than any relation, so a formula breaks there first. */
private val logical = setOf("⇒", "⇔", "⇐", "⟹", "⟺", "⟸", "∧", "∨", "∣")

/**
 * Is the gap between the bracket closing at [i] and the one opening after it a gap of quantifiers: between two of
 * them ((∀x ∈ T)(∃y ∈ T)) or between the last of them and the body ((∀x ∈ A)(f(x) = y))?
 */
private fun quantifierGap(toks: List<Tok>, i: Int): Boolean {
    if (toks.getOrNull(i + 2)?.s in setOf("∀", "∃")) return true
    var depth = 0
    for (j in i downTo 0) {
        val t = toks[j]
        if (t.isClose()) depth++ else if (t.isOpen() && --depth == 0) return toks.getOrNull(j + 1)?.s in setOf("∀", "∃")
    }
    return false
}

/**
 * Is the break at [k] (inlineBreakKinds) one of the logical structure: after a connective (⇒, ∧, the bar of a set), after
 * a comma, or between the brackets of quantifiers? Those come before the relations (=, ∈, ≤, →) of the same depth.
 */
private fun logicalBreak(toks: List<Tok>, k: Int): Boolean {
    var b = k; while (b > 0 && toks[b].gap) b--
    val t = toks[b]
    return t.s in logical || (t.cls == Cls.Punct && (t.s == "," || t.s == ";")) ||
        (t.isClose() && b + 1 < toks.size && toks[b + 1].isOpen() && quantifierGap(toks, b))
}

internal fun inlineBreakKinds(toks: List<Tok>, modBreak: Boolean = false): Map<Int, Boolean> {
    val n = toks.size
    // Pass 1: the bracket each item sits in, and which brackets hold another bracket directly (lists of tuples).
    val parent = IntArray(n) { -1 }
    val owner = IntArray(n) { -1 }
    val nested = BooleanArray(n)
    val stack = ArrayDeque<Int>()
    for (i in 0 until n) {
        val t = toks[i]
        owner[i] = stack.lastOrNull() ?: -1
        if (t.isOpen()) {
            stack.lastOrNull()?.let { nested[it] = true }
            parent[i] = stack.lastOrNull() ?: -1
            stack.addLast(i)
        } else if (t.isClose() && stack.isNotEmpty()) {
            stack.removeLast()
            owner[i] = stack.lastOrNull() ?: -1
        }
    }
    // Which brackets hold an operation or a relation of their own: a tuple of expressions, a logical body.
    val ops = BooleanArray(n)
    for (i in 0 until n) { val o = owner[i]; if (o >= 0 && (toks[i].cls == Cls.Bin || toks[i].cls == Cls.Rel)) ops[o] = true }
    // Pass 2: a set, a span, a list of tuples and a sized bracket (\big( of a long logical formula) let their own items
    // break, and so must every bracket around them.
    val memo = HashMap<Int, Boolean>()
    fun breakable(frame: Int): Boolean = frame < 0 || memo.getOrPut(frame) {
        val t = toks[frame]
        (t.s == "{" || t.s == "⟨" || ((t.s == "(" || t.s == "[") && (nested[frame] || (t.sized && ops[frame])))) && breakable(parent[frame])
    }
    // A tuple of expressions standing on its own ((α+4β, 2α+β, ...) = θ) may break after its commas, never inside an
    // entry. A vector never breaks, also one with a parameter inside a list ((2, −1, a+1, 1) in ((…), (…))).
    fun commasOnly(frame: Int) = frame >= 0 && parent[frame] < 0 && ops[frame] && (toks[frame].s == "(" || toks[frame].s == "[")
    val out = sortedSetOf<Int>()
    val weak = HashSet<Int>()
    for (i in 0 until n) {
        val t = toks[i]
        if (t.gap) continue
        if (!breakable(owner[i])) {
            if (t.cls == Cls.Punct && (t.s == "," || t.s == ";") && commasOnly(owner[i])) out += i
            continue
        }
        when {
            t.cls == Cls.Rel -> if (t.s == "∈" || t.s == "∉") { if (i > 0) out += i - 1 } else out += i
            t.cls == Cls.Bin -> { out += i; if (t.s !in logical) weak += i }
            t.cls == Cls.Punct -> if (t.s == "," || t.s == ";") out += i
            // two brackets side by side, like the quantifiers of (∀x ∈ T)(∃y ∈ T)(...): between them
            // strong only for a quantifier's group; a product of brackets ((n+1)(n+1)^n) is weak like ⋅
            t.isClose() && i + 1 < n && toks[i + 1].isOpen() -> { out += i; if (!quantifierGap(toks, i)) weak += i }
        }
    }
    // [modBreak] (large text, few letters a line): before the modulus of a congruence, "a ≡ b / (mod m)", as a strong
    // break, so the line does not tear the congruence after its ≡ ("665 ≡ / −1 (mod 666)", inspector, 18. 11.)
    if (modBreak) for (i in 0 until n - 1) if (toks[i + 1].s == PMOD && breakable(owner[i + 1])) out += i
    // a sign the formula starts with ("= (…)", "≝ (…)(…)", a line of a chain) never ends a line on its own: nothing
    // before it to keep, so a break after it would leave a lone "=" above its matrix (Nácvik DML, 18. 11.)
    val lead = (0 until n).firstOrNull { !toks[it].gap }?.takeIf { toks[it].cls == Cls.Rel }
    // a break moves past the gaps after it; where a strong and a weak break meet, the strong one counts
    val kinds = LinkedHashMap<Int, Boolean>()
    for (b in out) {
        if (b == lead) continue
        var k = b; while (k + 1 < n && toks[k + 1].gap) k++
        if (k !in 0 until n - 1) continue
        kinds[k] = (kinds[k] ?: true) && b in weak
    }
    return kinds
}

/**
 * The breaks of [inlineBreakKinds] in levels (Teo, 18. 11.; final LLM check 18. 11.): at the top level of the formula
 * 0 after a logical connective, a comma or the brackets of quantifiers, 1 after another relation (=, ≤, →) or before ∈;
 * inside a bracket, a set or a span the same two as 2 and 3; 4 weak (after +, −, ⋅, between the brackets of a product).
 * Only level 0 always cuts: a stretch that fits the line stays whole, so "f(m,n) = (m, m+2n)" moves to the next line
 * rather than break at its =, "x ∈ A ∨ ¬(…)" breaks at ∨ and not before ∈, and "(∀x,y ∈ A)(f(x) = f(y) ⇒ x = y)" after
 * its quantifier and then at ⇒. A bracket stays whole on a line when it fits ("{(x,y,z) ∈ ℝ³ | 2x−y+2z=3}"); when not, it
 * wraps at its connectives and commas, then its relations, before its sums.
 */
internal fun inlineBreakLevels(toks: List<Tok>, modBreak: Boolean = false): Map<Int, Int> {
    val kinds = inlineBreakKinds(toks, modBreak)
    var depth = 0
    val inside = BooleanArray(toks.size)
    for (i in toks.indices) {
        val t = toks[i]
        if (t.isClose()) depth = maxOf(0, depth - 1)
        inside[i] = depth > 0
        if (t.isOpen()) depth++
    }
    val levels = kinds.mapValues { (i, weak) -> if (weak) 4 else (if (inside[i]) 2 else 0) + (if (logicalBreak(toks, i)) 0 else 1) }
    if (!modBreak) return levels
    // [modBreak]: the break before a modulus comes first among the top level's relations, so a congruence that does not
    // fit goes "a ≡ b / (mod m)" rather than "a ≡ / b (mod m)"; every other level moves one down, in the same order, so
    // a bracket that fits still stays whole (joinLevelPieces takes any numbers)
    return levels.mapValues { (i, l) -> if (l == 1 && toks.getOrNull(i + 1)?.s == PMOD) 1 else if (l == 0) 0 else l + 1 }
}

/**
 * [pieces] with the cuts between them at [levels] (inlineBreakLevels): the cuts of level 0 always stay; between them a
 * stretch that fits [lineWidth] is joined into one piece, else it is cut at its level-1 cuts and each part joined when
 * it fits, else at its level-2 cuts.
 */
fun <B> joinLevelPieces(pieces: List<B>, levels: List<Int>, lineWidth: Float, width: (B) -> Float, join: (List<B>) -> B): List<B> {
    if (pieces.size < 2 || levels.size != pieces.size - 1) return pieces
    fun build(from: Int, to: Int, s: Int): List<B> {
        if (from == to) return listOf(pieces[from])
        val seg = pieces.subList(from, to + 1)
        if (s > 0 && seg.sumOf { width(it).toDouble() } <= lineWidth) return listOf(join(seg))
        val out = ArrayList<B>()
        var start = from
        for (i in from until to) if (levels[i] <= s) { out += build(start, i, s + 1); start = i + 1 }
        out += build(start, to, s + 1)
        return out
    }
    return build(0, pieces.lastIndex, 0)
}

/**
 * The pieces a line of text may wrap between: [pieces] cut at every break, [weak] for each cut between piece i and
 * i + 1 whether it is a weak one. Pieces between two strong cuts are joined into one whenever together they fit
 * [lineWidth], so a sum or a product breaks only when its whole side of the relation is wider than a line.
 */
fun <B> joinWeakPieces(pieces: List<B>, weak: List<Boolean>, lineWidth: Float, width: (B) -> Float, join: (List<B>) -> B): List<B> {
    if (pieces.size < 2 || weak.size != pieces.size - 1 || weak.none { it }) return pieces
    val out = ArrayList<B>()
    var seg = ArrayList<B>()
    fun flush() {
        if (seg.isEmpty()) return
        if (seg.size > 1 && seg.sumOf { width(it).toDouble() } <= lineWidth) out += join(seg) else out += seg
        seg = ArrayList()
    }
    pieces.forEachIndexed { i, p -> seg += p; if (i == pieces.lastIndex || !weak[i]) flush() }
    return out
}

/**
 * Greedy packing of piece widths into lines [width] wide; [first] is what is left on the first line. A piece wider
 * than a whole line gets a line of its own (it will be shrunk to fit, see [fitScale]).
 */
internal fun packLines(widths: List<Float>, width: Float, first: Float = width): List<List<Int>> {
    val lines = ArrayList<List<Int>>()
    var cur = ArrayList<Int>()
    var left = first
    for ((i, w) in widths.withIndex()) {
        if (w > left && (cur.isNotEmpty() || left < width)) {
            if (cur.isNotEmpty()) lines += cur
            cur = ArrayList(); left = width
        }
        cur += i; left -= w
    }
    if (cur.isNotEmpty()) lines += cur
    return lines
}

/** How much a piece has to shrink to fit a line: 1 when it fits. The last resort after every allowed break. */
fun fitScale(pieceWidth: Float, lineWidth: Float): Float =
    if (pieceWidth <= lineWidth || pieceWidth <= 0f || lineWidth <= 0f) 1f else lineWidth / pieceWidth

/** Extents of a box from its baseline, y down: [asc] above, [desc] below. */
internal data class Ext(val w: Float, val asc: Float, val desc: Float)

/** Where a big operator's limits go and how big the whole is: (x, baseline y) of op, sup, sub, then the extent. */
internal data class OpPlaces(val op: Pair<Float, Float>, val sup: Pair<Float, Float>?, val sub: Pair<Float, Float>?, val ext: Ext)

/**
 * A big operator (∑, ⋃, ⋂, ∏) with limits. In a display formula the limits sit above and below the symbol; inside
 * running text ([text]), as TeX's \textstyle does, they go to its right like sub and superscripts, so the formula
 * stays about one line tall. [size] is the font size, [axis] how far above the baseline the math axis is.
 */
internal fun bigOpPlaces(op: Ext, sup: Ext?, sub: Ext?, size: Float, axis: Float, text: Boolean): OpPlaces {
    val cy = -axis + (op.asc - op.desc) / 2
    if (text) {
        val x = op.w + size * .04f
        val supY = cy - op.asc + size * .3f
        val subY = cy + op.desc + size * .12f
        val w = x + maxOf(sup?.w ?: 0f, sub?.w ?: 0f) + (if (sup != null || sub != null) size * .05f else 0f)
        val asc = maxOf(op.asc - cy, if (sup != null) sup.asc - supY else 0f)
        val desc = maxOf(op.desc + cy, if (sub != null) sub.desc + subY else 0f)
        return OpPlaces(0f to cy, sup?.let { x to supY }, sub?.let { x to subY }, Ext(w, asc, desc))
    }
    val w = maxOf(op.w, sup?.w ?: 0f, sub?.w ?: 0f)
    val g = size * .1f
    var asc = op.asc - cy; var desc = op.desc + cy
    val supAt = sup?.let { val y = cy - op.asc - g - it.desc; asc = it.asc - y; (w - it.w) / 2 to y }
    val subAt = sub?.let { val y = cy + op.desc + g + it.asc; desc = it.desc + y; (w - it.w) / 2 to y }
    return OpPlaces((w - op.w) / 2 to cy, supAt, subAt, Ext(w, asc, desc))
}

/** The slot an inline formula takes in a line of text: its height and where its baseline is from the slot's top. */
internal data class Slot(val height: Float, val baseline: Float, val centered: Boolean)

/**
 * The slot of an inline formula of extent [asc]/[desc] in text of size [px] whose font has [fontAscent] and
 * [fontDescent] (both positive, from the baseline). An ordinary formula takes a slot as tall as a capital and hangs
 * over it a little, so lines keep an even rhythm. One that reaches beyond the font's own ascent or descent (a sum with
 * limits, a fraction, a matrix) gets a slot centred on the text that covers all of it, so its line grows instead of
 * drawing over the neighbouring lines. [lead] is half the room the style's fixed line height leaves over the font's
 * own ascent and descent (the text sits centred in its line): a formula may reach into it without growing the line,
 * as a bracket or a power of the TeX font does (its box is a hair deeper and taller than Google Sans Flex's).
 */
internal fun inlineSlot(asc: Float, desc: Float, px: Float, fontAscent: Float, fontDescent: Float, lead: Float = 0f): Slot {
    if (asc <= fontAscent + lead && desc <= fontDescent + lead) { val top = minOf(asc, px * .74f); return Slot(top, top, false) }
    // y down from the baseline: the text's centre is at yc (above the baseline, so negative)
    val yc = (fontDescent - fontAscent) / 2
    val half = maxOf(asc + yc, desc - yc)
    return Slot(2 * half, half - yc, true)
}

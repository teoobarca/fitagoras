package dev.hamper.tabula.math

private val displaySeparator = Regex(""",\s*\\(?:qquad(?![a-zA-Z])|quad(?![a-zA-Z])| )\s*""")

/**
 * The smallest a display formula may get: 75 % of its size. Below that it is hard to read, so a line that is still
 * too wide scrolls sideways inside its own box instead (see [displayPlan]).
 */
internal const val displayMinScale = .75f

private val softEdgeStart = Regex("""^(?:\s|\\q?quad(?![a-zA-Z])|\\[ ,;:])+""")
private val softEdgeEnd = Regex("""(?:\s|\\q?quad(?![a-zA-Z])|\\[ ,;:])+$""")

/** A line of a display formula with soft breaks: its TeX and how many quads it is indented ([softLines]). */
data class SoftLine(val tex: String, val indent: Int)

/**
 * The pieces of [tex] between its top-level soft breaks (`\softbreak`, `\softbreak[k]`), each with the level k of the
 * break before it (0 for the first); null when there is none. Top level: outside braces, \left…\right and environments,
 * so a break may sit inside a \Big( … \Big) pair (its pieces then draw the same delimiter sizes on their own lines).
 */
internal fun softSegments(tex: String): List<Pair<String, Int>>? {
    if (!tex.contains("\\softbreak")) return null
    val out = ArrayList<Pair<String, Int>>()
    var start = 0; var level = 0
    var braces = 0; var fences = 0; var envs = 0
    var i = 0
    fun word(at: Int): String { var e = at; while (e < tex.length && tex[e].isLetter()) e++; return tex.substring(at, e) }
    while (i < tex.length) {
        when (tex[i]) {
            '{' -> braces++
            '}' -> braces--
            '\\' -> {
                val w = word(i + 1)
                if (w.isEmpty()) { i += 2; continue }
                when (w) {
                    "left" -> fences++; "right" -> fences--; "begin" -> envs++; "end" -> envs--
                    "softbreak" -> if (braces == 0 && fences == 0 && envs == 0) {
                        out += tex.substring(start, i) to level
                        i += 1 + w.length
                        level = 0
                        if (tex.getOrNull(i) == '[') { val e = tex.indexOf(']', i); level = tex.substring(i + 1, e).trim().toIntOrNull() ?: 0; i = e + 1 }
                        start = i
                        continue
                    }
                }
                i += 1 + w.length
                continue
            }
        }
        i++
    }
    out += tex.substring(start) to level
    return out.takeIf { it.size > 1 }
}

private val softInRow = Regex("""\\softbreak(?![a-zA-Z])(?:\[\d+])?""")

/**
 * Soft breaks inside an aligned table (a row of the textbook too long for a narrow phone, "∀n ≥ 3: rₙ = …"): while
 * the table fits [avail] they are nothing; when it does not, each starts a new row there. Top-level ones are [softLines]'.
 */
fun softRows(tex: String, avail: Float, measure: (String) -> Float): String {
    if (!tex.contains("\\begin{aligned}") || !tex.contains("\\softbreak")) return tex
    var depth = 0
    val inside = ArrayList<IntRange>()
    var i = 0
    while (i < tex.length) {
        when {
            tex.startsWith("\\begin{aligned}", i) -> { depth++; i += 15 }
            tex.startsWith("\\end{aligned}", i) -> { depth--; i += 13 }
            depth > 0 && tex[i] == '\\' -> { val m = softInRow.matchAt(tex, i); if (m != null) { inside += m.range; i = m.range.last + 1 } else i += 2 }
            else -> i++
        }
    }
    if (inside.isEmpty() || measure(tex) <= avail) return tex
    var out = tex
    for (r in inside.asReversed()) out = out.replaceRange(r, "\\\\")
    return out
}

/**
 * The lines of a display formula with soft breaks for a room [avail] px wide (Teo, 18. 11.: a statement breaks as
 * little as it can and only where its logic does): the fewest lines that fit, and of those the one whose breaks have
 * the lowest levels (a level says how deep in the statement the break is: after the quantifiers 0, before an
 * implication inside the body 1, inside a list 2). A line after a break of level k is indented k quads ([em] px each).
 * Only a piece between two breaks may be wider than the room on its own; [displayLines] and [displayPlan] take it then.
 * Without soft breaks the formula is one line.
 */
fun softLines(tex: String, avail: Float, em: Float, measure: (String) -> Float): List<SoftLine> {
    val segs = softSegments(tex) ?: return listOf(SoftLine(tex, 0))
    val n = segs.size
    // the space around a break (",\\quad\\softbreak") separates only pieces on one line: a line drops it at its edges;
    // a continuation starts with an empty atom, so a ∨ or ⇒ there keeps its space as in TeX's "{}\\vee"
    fun join(a: Int, b: Int) = (a until b).joinToString("") { segs[it].first }
        .let { if (a > 0) "{}" + it.replace(softEdgeStart, "") else it }.let { if (b < n) it.replace(softEdgeEnd, "") else it }
    fun indent(a: Int) = if (a == 0) 0 else segs[a].second
    // best[i]: the lines of the pieces before i as (lines, sum of break levels, where the last line starts)
    val best = arrayOfNulls<Triple<Int, Int, Int>>(n + 1)
    best[0] = Triple(0, 0, -1)
    for (i in 1..n) for (j in 0 until i) {
        val prev = best[j] ?: continue
        if (i - j > 1 && measure(join(j, i)) + indent(j) * em > avail) continue
        val c = Triple(prev.first + 1, prev.second + indent(j), j)
        val cur = best[i]
        // on a tie the earlier lines take more (j grows)
        if (cur == null || c.first < cur.first || (c.first == cur.first && c.second <= cur.second)) best[i] = c
    }
    val out = ArrayList<SoftLine>()
    var i = n
    while (i > 0) { val j = best[i]!!.third; out += SoftLine(join(j, i), indent(j)); i = j }
    return out.reversed()
}

/** One row of a display formula on screen: line [line] whole or its [pieces], at [scale], scrolling when it must. */
data class DisplayRow(val line: Int, val pieces: List<Int>?, val scale: Float, val scroll: Boolean)

/**
 * How the lines of a display formula ([displayLines]) go on screen [avail] wide: a line that fits at no less than
 * [displayMinScale] is shrunk at most that much; a wider one wraps by the inline rules into rows of its pieces
 * ([pieceWidths], the line's pieces as [inlineBreaks] cuts it); a row still wider than its room at the floor keeps
 * the floor and scrolls sideways inside its own box. No row is ever drawn smaller than the floor.
 */
fun displayPlan(
    lines: List<String>, avail: Float, indent: Float, full: (String) -> Float, pieceWidths: (String) -> List<Float>,
): List<DisplayRow> = lines.flatMapIndexed { i, line ->
    val room = (avail - if (i > 0) indent else 0f).coerceAtLeast(1f)
    val w = full(line)
    if (w * displayMinScale <= room) listOf(DisplayRow(i, null, minOf(1f, room / w), false))
    else {
        val ws = pieceWidths(line)
        val cont = (avail - indent).coerceAtLeast(1f)
        packLines(ws, cont, room).mapIndexed { r, row ->
            val rw = row.sumOf { ws[it].toDouble() }.toFloat()
            val k = fitScale(rw, if (r == 0) room else cont)
            if (k < displayMinScale) DisplayRow(i, row, displayMinScale, true) else DisplayRow(i, row, k, false)
        }
    }
}

private val sizedSetOpen = Regex("""\\(?:big|Big|bigl|left)\s*\\\{""")
private val sizedSetClose = Regex("""\\(?:big|Big|bigr|right)\s*\\\}""")

private val displayRelations = setOf(
    "in", "notin", "subset", "subseteq", "supset", "supseteq", "le", "leq", "ge", "geq",
    "ne", "neq", "equiv", "sim", "simeq", "approx", "to", "mapsto",
    "Rightarrow", "Leftrightarrow", "implies", "impliedby", "iff",
)

/** Where a break before the relation at [at] goes: before a \\not that negates it ("\\not\\equiv" stays whole). */
private fun notBefore(tex: String, at: Int): Int {
    var k = at; while (k > 0 && tex[k - 1] == ' ') k--
    return if (tex.startsWith("\\not", k - 4) && k >= 4) k - 4 else at
}

/** A relation with a mark over it, "\stackrel{IP}{=}": breaks like the relation it carries. */
private val stackedRelations = setOf("stackrel", "overset")

/** Commas, then relations, then the contents of otherwise overflowing set builders. */
fun displayLines(
    tex: String, available: Float, continuationIndent: Float = 0f, measure: (String) -> Float,
): List<String> {
    if (measure(tex) <= available) return listOf(tex)
    val breaks = ArrayList<Pair<Int, Int>>()
    val fallbackBreaks = ArrayList<Pair<Int, Int>>()
    val setBreaks = ArrayList<Pair<Int, Int>>()
    val setContentBreaks = ArrayList<Pair<Int, Int>>()
    val standaloneFences = ArrayList<IntRange>()
    var setStart: Int? = null
    var setMids = ArrayList<Pair<Int, Int>>()
    var setContent = ArrayList<Pair<Int, Int>>()
    var groups = 0
    var delimiters = 0
    var fences = 0
    var environments = 0
    var i = 0
    while (i < tex.length) {
        val topLevel = groups == 0 && delimiters == 0 && fences == 0 && environments == 0
        val setLevel = setStart != null && groups == 0 && delimiters == 1 && fences == 0 && environments == 0
        if (setLevel && tex[i] in "=<>∈∉⊂⊆⊃⊇≤≥≠≡≈") {
            // Break before a relation, keeping :=, <= and >= intact.
            if (tex.getOrNull(i - 1) !in listOf(':', '<', '>', '=')) setContent += i to i
            else if (tex[i] == '=' && tex[i - 1] == ':') setContent += (i - 1) to (i - 1)
        }
        if (topLevel && tex[i] == ',') {
            val separator = displaySeparator.matchAt(tex, i)
            if (separator != null && separator.range.last + 1 < tex.length) {
                breaks += (i + 1) to (separator.range.last + 1)
                i = separator.range.last + 1
                continue
            }
        }
        if (topLevel) {
            when (tex[i]) {
                '|' -> fallbackBreaks.add(i to i)
                '=', '<', '>', '∈', '∉', '⊂', '⊆', '⊃', '⊇', '≤', '≥', '≠', '≡', '≈' -> {
                    // Before the relation, so a chain reads "A = B" / "= C" and a lone "=" never ends a line (Nácvik
                    // DML, 18. 11.: "=" stood alone above its matrix; docs/ZASADY.md 11: the break goes before ⇒).
                    // Compound relations stay whole: :=, <= and >= break before their first sign.
                    val prev = tex.getOrNull(i - 1)
                    when {
                        tex[i] == '=' && prev == ':' -> fallbackBreaks.add((i - 1) to (i - 1))
                        tex[i] == '=' && (prev == '<' || prev == '>') -> {}
                        else -> notBefore(tex, i).let { fallbackBreaks.add(it to it) }
                    }
                }
            }
        }
        when (tex[i]) {
            '{' -> groups++
            '}' -> groups--
            '(', '[' -> delimiters++
            ')', ']' -> delimiters--
            '\\' -> {
                val start = ++i
                while (i < tex.length && tex[i].isLetter()) i++
                if (i == start) {
                    // Escaped braces are visible delimiters, not TeX groups.
                    when (tex.getOrNull(i)) {
                        '{' -> {
                            if (topLevel) {
                                setStart = start - 1
                                setMids = ArrayList()
                                setContent = ArrayList()
                            }
                            delimiters++
                        }
                        '}' -> {
                            if (setLevel) {
                                // Only complete set builders with a top-level mid may be opened up.
                                if (setMids.isNotEmpty()) {
                                    setBreaks += setMids
                                    setContentBreaks += setContent
                                }
                                setStart = null
                            }
                            delimiters--
                        }
                    }
                    i++
                } else {
                    val command = tex.substring(start, i)
                    val sizedOpen = sizedSetOpen.matchAt(tex, start - 1)
                    val sizedClose = sizedSetClose.matchAt(tex, start - 1)
                    if (sizedOpen != null && (topLevel || command != "left")) {
                        if (topLevel) {
                            setStart = start - 1
                            if (command == "left" || command == "bigl") {
                                standaloneFences += (start - 1)..sizedOpen.range.last
                            }
                            setMids = ArrayList()
                            setContent = ArrayList()
                        }
                        delimiters++
                        i = sizedOpen.range.last + 1
                        continue
                    }
                    if (sizedClose != null && (setLevel || command != "right")) {
                        if (setLevel) {
                            if (command == "right" || command == "bigr") {
                                standaloneFences += (start - 1)..sizedClose.range.last
                            }
                            if (setMids.isNotEmpty()) {
                                setBreaks += setMids
                                setContentBreaks += setContent
                            }
                            setStart = null
                        }
                        delimiters--
                        i = sizedClose.range.last + 1
                        continue
                    }
                    if (setLevel) {
                        if (command == "mid") setMids += (start - 1) to (start - 1)
                        else if (command in displayRelations) setContent += (start - 1) to (start - 1)
                    }
                    if (topLevel) {
                        // before \mid, a relation command and a sign stacked over one (\stackrel{IP}{=})
                        if (command == "mid" || command in displayRelations || command in stackedRelations) notBefore(tex, start - 1).let { fallbackBreaks += it to it }
                    }
                    when (command) {
                        "text" -> {
                            if (setLevel) setContent += (start - 1) to (start - 1)
                            // Text is opaque, even if it contains parentheses or relation symbols.
                            var at = i
                            while (tex.getOrNull(at)?.isWhitespace() == true) at++
                            if (tex.getOrNull(at) == '{') {
                                var depth = 1
                                at++
                                while (at < tex.length && depth > 0) {
                                    when (tex[at]) {
                                        '\\' -> at++
                                        '{' -> depth++
                                        '}' -> depth--
                                    }
                                    at++
                                }
                                i = at
                                if (setLevel && depth == 0) setContent += i to i
                            }
                        }
                        "left", "right" -> {
                            if (command == "left") fences++ else fences--
                            // Consume the delimiter, including invisible/asymmetric fences.
                            while (tex.getOrNull(i)?.isWhitespace() == true) i++
                            if (tex.getOrNull(i) == '\\') {
                                i++
                                if (tex.getOrNull(i)?.isLetter() == true) {
                                    while (tex.getOrNull(i)?.isLetter() == true) i++
                                } else i++
                            } else if (i < tex.length) i++
                        }
                        "begin" -> environments++
                        "end" -> environments--
                        "langle", "lbrace" -> delimiters++
                        "rangle", "rbrace" -> delimiters--
                    }
                }
                continue
            }
        }
        i++
    }
    fun width(lineIndex: Int) = (available - if (lineIndex > 0) continuationIndent else 0f).coerceAtLeast(0f)

    fun fit(
        source: String, boundaries: List<Pair<Int, Int>>, firstLine: Int,
        render: (String) -> String = { it },
    ): List<String> {
        val lines = ArrayList<String>()
        var start = 0
        var previous: Pair<Int, Int>? = null
        for (boundary in boundaries + (source.length to source.length)) {
            val last = previous
            if (last != null && measure(render(source.substring(start, boundary.first))) > width(firstLine + lines.size)) {
                lines += source.substring(start, last.first)
                start = last.second
            }
            previous = boundary
        }
        lines += source.substring(start)
        return lines
    }

    val primary = fit(tex, breaks, 0)
    val result = ArrayList<String>()
    var offset = 0
    for (piece in primary) {
        // Locate source ranges without changing whitespace or existing comma-break behavior.
        offset = tex.indexOf(piece, offset)
        val hasSet = setBreaks.any { (end, _) -> end > offset && end < offset + piece.length }
        if (!hasSet && measure(piece) * displayMinScale > width(result.size)) {
            val secondary = fallbackBreaks.filter { (end, next) ->
                end > offset && next < offset + piece.length &&
                    tex.substring(offset, end).isNotBlank() && tex.substring(next, offset + piece.length).isNotBlank()
            }.map { (end, next) -> (end - offset) to (next - offset) }
            result += fit(piece, secondary, result.size)
        } else result += piece
        offset += piece.length
    }
    // Open overflowing set builders at full size before considering any shrinking.
    // Fitting formulas return unchanged above, including their delimiter commands.
    val expanded = ArrayList<String>()
    offset = 0
    for (piece in result) {
        offset = tex.indexOf(piece, offset)
        fun within(boundaries: List<Pair<Int, Int>>, start: Int, source: String) = boundaries.filter { (end, next) ->
            end > start && next < start + source.length &&
                tex.substring(start, end).isNotBlank() && tex.substring(next, start + source.length).isNotBlank()
        }.map { (end, next) -> (end - start) to (next - start) }
        val mids = within(setBreaks, offset, piece)
        if (mids.isNotEmpty() && measure(piece) > width(expanded.size)) {
            // A left/right fence cannot span independent MathEngine layouts. Use the
            // standalone sized brace only on the pieces of a fence that actually wraps.
            // Likewise normalize bigl/bigr only when wrapping, preserving fitting sources.
            fun fragment(source: String): String {
                val start = tex.indexOf(source, offset)
                var rendered = source
                for (range in standaloneFences.asReversed()) {
                    if (range.first >= start && range.last < start + source.length) {
                        val brace = tex[range.last]
                        rendered = rendered.replaceRange(range.first - start, range.last + 1 - start, "\\big\\$brace")
                    }
                }
                return rendered
            }
            var innerOffset = offset
            for (part in fit(piece, mids, expanded.size, ::fragment)) {
                if (measure(fragment(part)) > width(expanded.size)) {
                    expanded += fit(part, within(setContentBreaks, innerOffset, part), expanded.size, ::fragment).map(::fragment)
                } else expanded += fragment(part)
                innerOffset += part.length
            }
        } else expanded += piece
        offset += piece.length
    }
    return expanded
}

package dev.hamper.tabula.math

import dev.hamper.tabula.ui.nabehKonca
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.withLink
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import dev.hamper.tabula.compat.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalWindowInfo
import dev.hamper.tabula.compat.graphics.*

val LocalMathFace = compositionLocalOf { SkriptaMath }
val LocalMathInks = compositionLocalOf { MathInks(0, 0, 0, 0) }

/** A formula on its own, with a real baseline. [say] is what TalkBack reads. */
@Composable
fun Tex(
    tex: String, modifier: Modifier = Modifier, size: TextUnit = 20.sp, color: Color = LocalContentColor.current,
    weight: Int = 400, bold: Boolean = false, say: String? = null,
) {
    val d = LocalDensity.current
    val px = with(d) { size.toPx() }
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val box = remember(tex, px, face, inks, weight, bold) { MathEngine.layout(tex, px, face, inks, weight, bold) }
    val ink = color.toArgb()
    val gloss = LocalGloss.current
    val m = modifier.semantics { contentDescription = say ?: speak(tex) }
        .tapMarks(box, box.asc, gloss)
        .drawBehind {
            // a formula given less room than it needs (a label in a narrow cell) is drawn scaled to the room
            val room = this.size.width; val k = if (room < ceil(box.w)) room / ceil(box.w) else 1f
            drawIntoCanvas { val cv = it.nativeCanvas; cv.save(); cv.scale(k, k); box.draw(cv, 0f, box.asc, ink); cv.restore() }
        }
    Layout(modifier = m) { _, c ->
        val w = ceil(box.w)
        val k = if (c.hasBoundedWidth && c.maxWidth in 1 until w.toInt()) c.maxWidth / w else 1f
        MathOverflow.check(tex, w * k, c)
        layout((w * k).toInt(), ceil((box.asc + box.desc) * k).toInt(), mapOf(FirstBaseline to (box.asc * k).toInt())) {}
    }
}

/**
 * A formula with soft breaks (`\softbreak[k]`) is first cut into lines at them, as few as the width allows ([softLines]).
 * Wrap at top-level separators and, when necessary, relations; preserve whole-formula semantics. A line shrinks to
 * [displayMinScale] (75 %) at most; one that still does not fit wraps by the inline rules ([inlineBreaks]); a row
 * still too wide keeps 75 % and scrolls sideways inside its own box ([displayPlan]). Nothing is cut off and nothing
 * gets so small it cannot be read.
 */
@Composable
fun TexBlock(
    tex0: String, modifier: Modifier = Modifier, size: TextUnit = 20.sp, color: Color = LocalContentColor.current, say: String? = null,
    center: Boolean = false, actions: List<CustomAccessibilityAction> = emptyList(), mark: ((String) -> String)? = null,
) {
    val d = LocalDensity.current
    val px = with(d) { size.toPx() }
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    BoxWithConstraints(modifier) {
        val avail = with(d) { maxWidth.toPx() }
        // a chain's line numbers ("&&\text{(1)}", from the skriptá's list) stand in a column after the lines; where
        // that column does not fit (large text), each number moves before its line: the text, the step and the
        // answers name the lines by them, so they are never dropped (inspector, 18. 11.)
        val tagged = remember(tex0, px, avail, face, inks) {
            if (lineTags.containsMatchIn(tex0) && MathEngine.layout(tex0, px, face, inks).w > avail) tagsBefore(tex0) else tex0
        }
        // numbered lines are one block and never wrap: with their numbers before them they may get smaller than a display
        // line (to 60 %, at 320 dp and 200 % about 19 sp), never scroll sideways and never lose the numbers
        if (tagged !== tex0) {
            val w = MathEngine.layout(tagged, px, face, inks).w
            val k = if (w + 1f > avail) maxOf(.6f, (avail - 1f) / (w + 1f)) else 1f
            if (k > .6f || w * k + 1f <= avail) {
                Box((if (center) Modifier.fillMaxWidth() else Modifier).clearAndSetSemantics { contentDescription = say ?: speak(tagged); if (actions.isNotEmpty()) customActions = actions }, contentAlignment = if (center) Alignment.Center else Alignment.CenterStart) {
                    Tex(mark?.invoke(tagged) ?: tagged, size = (size.value * k).sp, color = color, say = say)
                }
                return@BoxWithConstraints
            }
        }
        val gap = with(d) { (px * .35f).toDp() }
        val tex = remember(tagged, px, avail, face, inks) { softRows(tagged, avail) { MathEngine.layout(it, px, face, inks).w } }
        // a quad of indent per level, but at a large font on a narrow phone no more than a twelfth of the width
        val em = minOf(px, avail / 12f)
        val soft = remember(tex, px, avail, face, inks) { softLines(tex, avail, em) { MathEngine.layout(it, px, face, inks).w } }
        if (soft.size > 1) {
            // a statement broken at its soft breaks: one block, its lines flush left under each other (continuations
            // indented), the block centred as a whole like an aligned one
            if (MathOverflow.on) soft.firstOrNull { MathEngine.layout(it.tex, px, face, inks).w + it.indent * em > avail }
                ?.let { MathOverflow.fit(tex, MathEngine.layout(it.tex, px, face, inks).w + it.indent * em, avail) }
            Box(
                (if (center) Modifier.fillMaxWidth() else Modifier).clearAndSetSemantics { contentDescription = say ?: speak(tex); if (actions.isNotEmpty()) customActions = actions },
                contentAlignment = if (center) Alignment.TopCenter else Alignment.TopStart,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                    for (l in soft) {
                        val start = with(d) { (em * l.indent).toDp() }
                        Box(Modifier.padding(start = start)) { DisplayRows(l.tex, avail - em * l.indent, px, size, color, say, false, mark = mark) }
                    }
                }
            }
            return@BoxWithConstraints
        }
        if (MathOverflow.on) MathOverflow.fit(tex, MathEngine.layout(tex, px, face, inks).w, avail)
        DisplayRows(
            tex, avail, px, size, color, say, center,
            (if (center) Modifier.fillMaxWidth() else Modifier).clearAndSetSemantics { contentDescription = say ?: speak(tex); if (actions.isNotEmpty()) customActions = actions },
            mark,
        )
    }
}

/** One line of a display formula in [avail] px: whole, shrunk to 75 % at most, broken into rows, or scrolling ([displayPlan]). */
@Composable
private fun DisplayRows(tex: String, avail: Float, px: Float, size: TextUnit, color: Color, say: String?, center: Boolean, modifier: Modifier = Modifier, mark: ((String) -> String)? = null) {
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val indent = with(d) { (px * 1.5f).toDp() }
    val gap = with(d) { (px * .35f).toDp() }
    // [mark]: the tappable symbols go in after the lines are cut, so a marked relation (≡) is still a place to break
    val lines = remember(tex, px, avail, face, inks, mark) {
        displayLines(tex, avail, continuationIndent = px * 1.5f) { MathEngine.layout(it, px, face, inks).w }.map { mark?.invoke(it) ?: it }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap), horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start) {
        val plan = remember(lines, px, avail, face, inks) {
            displayPlan(lines, avail, px * 1.5f, { MathEngine.layout(it, px, face, inks).w }) { l -> MathEngine.pieces(l, px, face, inks).map { it.w } }
        }
        for ((r, row) in plan.withIndex()) {
            val line = lines[row.line]
            val first = r == 0 || plan[r - 1].line != row.line
            val start = if (row.line > 0 || !first) indent else 0.dp
            if (row.pieces == null) {
                Box(Modifier.padding(start = start)) { Tex(line, size = (size.value * row.scale).sp, color = color, say = say) }
            } else {
                val pieces = remember(line, px, face, inks) { MathEngine.pieces(line, px, face, inks) }
                val boxes = row.pieces.map { pieces[it] }
                if (row.scroll) SideScroll(Modifier.padding(start = start)) { BoxLine(boxes, row.scale, color) }
                else Box(Modifier.padding(start = start)) { BoxLine(boxes, row.scale, color) }
            }
        }
    }
}

/**
 * A row too wide even at the smallest display size: it scrolls sideways inside its own box, and the edge that hides
 * more fades out, so it reads as "there is more", not as cut off. The page itself never scrolls sideways.
 */
@Composable
private fun SideScroll(modifier: Modifier, content: @Composable () -> Unit) {
    val state = rememberScrollState()
    val fade = with(LocalDensity.current) { 28.dp.toPx() }
    Box(
        modifier.fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (state.canScrollForward) drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = size.width - fade, endX = size.width), blendMode = BlendMode.DstOut)
                if (state.canScrollBackward) drawRect(Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = 0f, endX = fade), blendMode = BlendMode.DstOut)
            }
            .horizontalScroll(state),
    ) { content() }
}

/** Formula pieces side by side on one baseline, scaled by [k]. */
@Composable
private fun BoxLine(boxes: List<MBox>, k: Float, color: Color) {
    val ink = color.toArgb()
    val w = boxes.sumOf { it.w.toDouble() }.toFloat()
    val asc = boxes.maxOf { it.asc }; val desc = boxes.maxOf { it.desc }
    Layout(modifier = Modifier.tapMarks(boxes, LocalGloss.current, k).drawBehind {
        drawIntoCanvas {
            val cv = it.nativeCanvas
            cv.save(); cv.translate(0f, asc * k); cv.scale(k, k)
            var x = 0f
            for (b in boxes) { b.draw(cv, x, 0f, ink); x += b.w }
            cv.restore()
        }
    }) { _, c ->
        MathOverflow.check("display row", ceil(w * k), c)
        layout(ceil(w * k).toInt(), ceil((asc + desc) * k).toInt(), mapOf(FirstBaseline to (asc * k).toInt())) {}
    }
}

private val lineTags = Regex("""&&\\text\{\(\d+\)\}""")
private val taggedRow = Regex("""^(.*?)&(.*)&&(\\text\{\(\d+\)\})$""")

/**
 * An aligned chain with its numbers before the lines instead of after them: "⇔ & a ≡ b && (1)" becomes
 * "(1) && ⇔ & a ≡ b", the number right-aligned in a column of its own, a quad before the chain.
 */
internal fun tagsBefore(tex: String): String {
    val open = "\\begin{aligned}"; val close = "\\end{aligned}"
    val a = tex.indexOf(open); val b = tex.lastIndexOf(close)
    if (a < 0 || b < a) return tex.replace(lineTags, "")
    val rows = tex.substring(a + open.length, b).split("\\\\")
    val moved = rows.map { r -> taggedRow.find(r)?.let { m -> "${m.groupValues[3]}&&${m.groupValues[1]}&${m.groupValues[2]}" } ?: "&&$r" }
    return tex.substring(0, a + open.length) + moved.joinToString("\\\\") + tex.substring(b)
}
/** A formula that starts with a relation: it continues the sentence before it ("súčin je ≡ −7"). */
private val opensWithRelation = Regex("""^\s*(?:\\(?:equiv|not\\equiv|neq?|leq?|geq?|approx|sim|cong|Leftrightarrow|Rightarrow|iff|implies)(?![a-zA-Z])|=|<|>)""")
private val tokens = Regex("""\$([^$]+)\$|\*\*|==""")
private val oneLetter = Regex("""(?<![\p{L}\p{N}])([aikosuvzAIKOSUVZ]) """)

// a date ("19. 11.", "2. 12. 2026"), a range ("1 až 3", "(1) až (3)", "9 až 10 b"), a number and its word ("10 b",
// "30 bodov"), a name and its number ("Věta 6.2", "Body 1", "krok 6"): each read as one thing, so no line ends inside it
private val keptDate = Regex("""(?<=\d\.) (?=\d)""")
private val keptRange = Regex("""([\d)]\.?) až (?=[\d(])""")
// a count out of a whole ("6 z 25", "23 zo 40 b", "4× z 9"): one quantity, never "23" / "zo 40"
private val keptOf = Regex("""(?<=[\d×]) (zo?) (?=\d)""")
// a number and what it counts ("10 b", "15 min", "30 bodov", "75 minút", "5 lekcií"): a unit or a counted thing of the
// app, so a count never ends a line (inspector, 18. 11.: "30 / bodov", "75 / minút"); "príklad 1 / písomiek" may break
private val keptUnit = Regex("""(?<=\d) (?=(?:b|min|h|s|%|×|bod|body|bodov|bodu|minút|minúty|minúta|hodín|hodiny|hodina|sekúnd|dní|dni|deň|lekcia|lekcie|lekcií|otázka|otázky|otázok|príklad|príklady|príkladov|krok|kroky|krokov)(?![\p{L}\p{N}]))""")
// a statement's number ("Věta 6.2", "Def. 2.23") or a point, step or problem by its number ("Body 1", "krok 6")
private val keptNumber = Regex("""(?<=\p{L}\p{L}\.?) (?=\d+\.\d)|(?<=\b(?:[Bb]od|[Bb]ody|[Bb]odu|[Kk]rok|[Kk]roku|[Pp]ríklad|[Pp]ríkladu|[Čč]asť|[Kk]apitola|[Pp]rednáška|[Pp]aralelka)) (?=\(?\d)""")

/**
 * Slovak and Czech typography for every text the app draws (Rich, labels, button labels): a one-letter word never
 * ends a line, a date, a range, a count out of a whole, a number with its unit or a name with its number never break
 * inside, and a line never starts with the separator "·" (it stays with the word before it; [Parts] drops it at a break).
 */
fun String.nb(): String = replace(keptOf, "\u00A0$1\u00A0").replace(oneLetter, "$1\u00A0").replace(keptRange, "$1\u00A0až\u00A0").replace(keptDate, "\u00A0")
    .replace(keptUnit, "\u00A0").replace(keptNumber, "\u00A0").replace(" · ", "\u00A0· ")

/** Where the lines of a paragraph start and the last one ends (px from its top), for a screen that must not scroll through one. */
val LocalLineEdges = androidx.compose.runtime.compositionLocalOf<((List<Float>) -> Unit)?> { null }

/** A line that ends in [left] and goes on with [right] breaks what [nb] keeps together (tests: LabelAudit). */
fun splitsKept(left: String, right: String): Boolean {
    val l = left.trimEnd().takeLast(16); val r = right.trimStart().take(16)
    if (l.isEmpty() || r.isEmpty()) return false
    return "$l $r".nb()[l.length] == '\u00A0'
}

/**
 * A paragraph with $inline math$ and **bold**. Math sits on the text baseline and the line height never moves. A
 * long formula wraps where a reader expects it (see [inlineBreaks]); a piece that still does not fit a whole line is
 * shrunk to the line. A statement's key formula ($$…$$) stands on its own line; symbols and references are tappable.
 */
@Composable
fun Rich(
    text: String, modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current, color: Color = LocalContentColor.current,
    boldColor: Color? = null, textAlign: androidx.compose.ui.text.style.TextAlign? = null,
) {
    val t = text.replace('§', '$')
    if (!t.contains("$$")) { RichLine(t, modifier, style, color, boldColor, textAlign); return }
    // a statement's key formula stands on its own centered line, a little larger, with air above and below
    val segs = remember(t) { segments(t) }
    val fs = if (style.fontSize.isUnspecified) 16.sp else style.fontSize
    Column(modifier) {
        for (sg in segs) when (sg) {
            is Seg.Text -> RichLine(sg.s, Modifier, style, color, boldColor, textAlign)
            is Seg.Display -> DisplayTex(sg.tex, fs, color)
        }
    }
}

/** A display formula of a statement; its symbols are tappable like the ones in the text. */
@Composable
private fun DisplayTex(tex0: String, fs: TextUnit, color: Color) =
    StatementTex(tex0, Modifier.fillMaxWidth().padding(vertical = (fs.value * .45f).dp), (fs.value * 1.2f).sp, color, center = true)

/**
 * A display formula of a statement (a definition's or theorem's own line in the skriptá's words, a key formula in the
 * text): every glossary symbol in it answers a tap, also when a line above already had it. The finger goes to the big
 * formula, and the hint under a statement says "Symboly vo vzorci, napríklad φ" (QA on the emulator, 19. 11.: φ in the
 * display of Definícia 6.10 opened nothing; display lines of a source card were never marked at all). The claim only
 * decides which text offers the symbol to TalkBack, once a screen.
 */
@Composable
fun StatementTex(tex0: String, modifier: Modifier, size: TextUnit, color: Color, center: Boolean = false) {
    val gloss = LocalGloss.current
    val owner = remember { Any() }
    if (gloss != null) DisposableEffect(gloss, owner) { onDispose { gloss.release(owner) } }
    // TalkBack: the first text on the screen with a symbol offers it
    remember(tex0, gloss, gloss?.version) { if (gloss != null) { gloss.forget(owner); tapSymbols(tex0, gloss.symbols) { k -> gloss.claim("sym:$k", owner) } } }
    val acts = gloss?.let { g -> g.ownedSymbols(owner).map { k -> CustomAccessibilityAction(sayWhat(g, k)) { g.open = Sheet.Sym(g.symbols[k]); true } } }.orEmpty()
    // the finger: every line marks every symbol it has, after the lines are cut
    val mark = remember(gloss) { gloss?.let { g -> { line: String -> tapSymbols(line, g.symbols) { true } } } }
    TexBlock(tex0, modifier, size = size, color = color, center = center, actions = acts, mark = mark)
}

private fun sayWhat(g: GlossState, k: Int) = "Vysvetlenie značky: ${g.symbols[k].read}"

@Composable
private fun RichLine(
    text: String, modifier: Modifier, style: TextStyle, color: Color,
    boldColor: Color?, textAlign: androidx.compose.ui.text.style.TextAlign?,
) {
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val fs = if (style.fontSize.isUnspecified) 16.sp else style.fontSize
    val px = with(d) { fs.toPx() }
    val weight = style.fontWeight?.weight ?: 400
    val heavy = weight >= 600
    val ink = color.toArgb()
    // The width the text gets; until the first measure, the app's width (the window, at most its column) less the usual
    // screen margins.
    val window = minOf(LocalWindowInfo.current.containerSize.width.toFloat(), with(d) { dev.hamper.tabula.ui.AppWidth.toPx() })
    var lineWidth by remember { mutableFloatStateOf((window - with(d) { 40.dp.toPx() }).coerceAtLeast(px * 8f)) }
    val gloss = LocalGloss.current
    val text = if (LocalRefLabel.current && text.startsWith("Skriptá, ")) "Skriptá · " + text.removePrefix("Skriptá, ") else text
    val owner = remember { Any() }
    if (gloss != null) DisposableEffect(gloss, owner) { onDispose { gloss.release(owner) } }
    val keys = remember(text) { ArrayList<IntRange>() }
    val refs = remember(text) { ArrayList<IntRange>() }
    val refIndex = dev.hamper.tabula.ui.LocalRefs.current
    val refLabel = LocalRefLabel.current
    val laidFor = lineWidth
    // A fixed line height would clip a tall formula into the lines around it: such a paragraph lets its lines grow,
    // and then no formula of it may reach into the leading the fixed height had.
    val tallHere = remember(text, px, face, inks, weight, style.lineHeight) { hasTallIn(text, px, face, inks, weight) { fa, fd -> d.halfLead(style, px, fa, fd) } }
    val (annotated, inline, widest) = remember(text, px, face, inks, weight, ink, boldColor, lineWidth, gloss, gloss?.version, refIndex, refLabel, tallHere) {
        gloss?.forget(owner)
        val map = LinkedHashMap<String, InlineTextContent>()
        var widest = 0f to ""
        var n = 0
        keys.clear(); refs.clear()
        val here = HashSet<Int>()
        val hereRefs = HashSet<String>()
        val s = buildAnnotatedString {
            var at = 0
            var bold = false
            var key = -1
            var afterMath = false
            // ** toggles bold, so a formula can sit inside bold text; a hyphen right after a formula never breaks
            fun plain(t0: String, before: String) {
                var t = t0.nb()
                if (afterMath && t.startsWith("-")) t = "\u2060\u2011" + t.drop(1)
                // punctuation right after a formula stays on its line: a formula is an object character, and the line may
                // break after one, so a word joiner holds the comma or full stop to it
                else if (afterMath && t.isNotEmpty() && t[0] in ",.;:!?)") t = "\u2060" + t
                if (t.isEmpty()) return
                fun put(u: String) {
                    if (u.isEmpty()) return
                    if (bold) withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor ?: Color.Unspecified)) { append(u) }
                    else if (key >= 0) withStyle(SpanStyle(fontWeight = FontWeight(maxOf(500, weight + 100)))) { append(u) }
                    else append(u)
                }
                // a reference reads with its name, the number behind ("definíciu LZ (Def. 2.23)"); one the app has is a
                // link with the symbols' thin line, once a screen, never to what the screen itself shows
                val hits = refIndex?.scan(t).orEmpty()
                var p = 0
                for (h in hits) {
                    put(t.substring(p, h.range.first))
                    // on the card of the item itself its source just says what it is in the book ("Skriptá · Veta 2.18"): the
                    // title is right above
                    val self = refLabel && h.target != null && gloss != null && h.target!!.id in gloss.self
                    val shown = (if (self) "${dev.hamper.tabula.data.refNoun(h.kind).replaceFirstChar { it.uppercase() }} ${h.number}" else dev.hamper.tabula.data.refText(h, t, refLabel, before)).nb()
                    val target = h.target
                    // once a screen, so also once a paragraph (Veta 2.59 and Veta 2.60 are one card)
                    if (gloss != null && target != null && target.id !in gloss.self && target.id !in hereRefs && gloss.claim("ref:${target.id}", owner).also { if (it) hereRefs += target.id }) {
                        val start = length
                        // the link keeps the text's own ink; the only mark is the thin line drawn under it (no link colour, no underline)
                        withLink(LinkAnnotation.Clickable("ref:${target.id}", androidx.compose.ui.text.TextLinkStyles(SpanStyle(color = color, textDecoration = androidx.compose.ui.text.style.TextDecoration.None))) { gloss.open = Sheet.Ref(target) }) { put(shown) }
                        refs += start until length
                    } else put(shown)
                    p = h.range.last + 1
                }
                put(t.substring(p))
            }
            for (m in tokens.findAll(text)) {
                // a formula that opens with a relation ("je §\equiv -7\equiv 659§") goes on with the word before it: a line
                // never starts with ≡ or = (judge, 18. 11.: "riadok začína relačným znakom"); the whole chain moves down
                val before = text.substring(at, m.range.first).let { b ->
                    if (m.groups[1]?.value?.let { opensWithRelation.containsMatchIn(it) } == true && b.length > 1 && b.endsWith(' ') && b[b.length - 2].isLetterOrDigit()) b.dropLast(1) + "\u00A0" else b
                }
                plain(before, text.substring(0, at))
                at = m.range.last + 1
                // == opens and closes a key word: medium weight and a thin low marker under it
                if (m.value == "==") { if (key < 0) key = length else { if (length > key) keys += key until length; key = -1 }; continue }
                val tex0 = m.groups[1]?.value
                if (tex0 == null) { bold = !bold; afterMath = false; continue }
                // once per screen, and so also once in this paragraph: a later formula here does not mark it again
                val tex = if (gloss == null) tex0 else tapSymbols(tex0, gloss.symbols) { k -> k !in here && gloss.claim("sym:$k", owner).also { if (it) here += k } }
                val w8 = if (bold) 700 else weight.coerceAtMost(700)
                val hv = heavy || bold
                // Short formulas stay whole; a long one, or one wider than the line, comes in pieces the line may wrap between:
                // at its relations and commas, inside a sum or a product only when that alone is wider than the line.
                val w0 = MathEngine.layout(tex, px, face, inks, w8, hv, text = true)
                // punctuation right after it is held to its last piece (below), so that piece leaves room for it
                val room = lineWidth - (if (text.getOrNull(m.range.last + 1)?.let { it in ",.;:!?)" } == true) px * .4f else 0f)
                // On a line of few letters (large text, a headline) a formula that fits a line of its own goes there whole,
                // a little smaller if it must (to 85 %), with the punctuation after it: "Koľko je / 665 · 7 mod 666?",
                // never "665 · / 7 mod 666?" (inspector, 18. 11.)
                val trail = px * .55f * text.substring(m.range.last + 1).takeWhile { !it.isWhitespace() && it != '$' && it != '§' }.length
                val short = w0.w < px * 11f && w0.w <= room
                val alone = !short && d.fontScale > 1.3f && lineWidth < px * 16f && ceil(w0.w) + 1f + trail <= (lineWidth - 1f) / .85f
                val ps = if (short || alone) listOf(w0) else MathEngine.inlinePieces(tex, px, face, inks, w8, hv, room - 1f, modBreak = d.fontScale > 1.3f)
                val lastRoom = if (alone) lineWidth - 1f - trail else room - 1f
                val (fa, fd) = MathEngine.textMetrics(px, w8)
                val sayIt = speak(tex).ifBlank { if (tex.contains("cdot")) "krát" else "vzorec" }
                ps.forEachIndexed { pi, box ->
                    if (pi > 0) append("\u200B")
                    val id = "m${n++}"
                    appendInlineContent(id, if (pi == 0) sayIt else " ")
                    // The last resort: a piece wider than a whole line shrinks to it.
                    val k = fitScale(ceil(box.w) + 1f, if (pi == ps.lastIndex) lastRoom else lineWidth - 1f)
                    // An ordinary formula's slot is as tall as a capital and the rest hangs over, so lines stay even; a
                    // taller one (a sum with limits, a fraction) gets a slot covering all of it, and its line grows.
                    val slot = inlineSlot(box.asc * k, box.desc * k, px, fa, fd, if (tallHere) 0f else d.halfLead(style, px, fa, fd))
                    val w = (ceil(box.w) + 1f) * k
                    if (w > widest.first) widest = w to tex
                    map[id] = with(d) {
                        InlineTextContent(Placeholder(w.toSp(), ceil(slot.height).toSp(), if (slot.centered) PlaceholderVerticalAlign.TextCenter else PlaceholderVerticalAlign.AboveBaseline)) {
                            val base = if (slot.centered) slot.baseline else ceil(slot.height)
                            Layout(modifier = Modifier.tapMarks(box, base, gloss, k).drawBehind {
                                drawIntoCanvas {
                                    val cv = it.nativeCanvas
                                    // the baseline inside the slot, measured from its top
                                    val b = if (slot.centered) slot.baseline * size.height / ceil(slot.height) else size.height
                                    cv.save(); cv.translate(0f, b); cv.scale(k, k); box.draw(cv, 0f, 0f, ink); cv.restore()
                                }
                            }) { _, c ->
                                layout(c.maxWidth, c.maxHeight) {}
                            }
                        }
                    }
                }
                afterMath = true
            }
            plain(text.substring(at), text.substring(0, at))
        }
        Triple(s, map.toMap(), widest)
    }
    // key words: a low marker in the paper tint, from a little above the baseline to just under it
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val marker = LocalReading.current.marker
    val line = Color(LocalMathInks.current.mark)
    val m = if (keys.isEmpty() && refs.isEmpty()) modifier else modifier.drawBehind {
        val l = layout ?: return@drawBehind
        // references: the same light dots as a tappable symbol (MarkBox), under the letters' descent
        for (r in refs) {
            if (r.last >= l.layoutInput.text.length) continue
            val a = l.getLineForOffset(r.first); val b = l.getLineForOffset(r.last)
            val th = maxOf(1.6f, px * .05f)
            val pitch = th * 2.4f
            for (ln in a..b) {
                val x0 = if (ln == a) l.getHorizontalPosition(r.first, true) else l.getLineLeft(ln)
                val x1 = if (ln == b) l.getHorizontalPosition(r.last + 1, true) else l.getLineRight(ln)
                val y = l.getLineBaseline(ln) + px * .17f + th / 2
                val w = kotlin.math.abs(x1 - x0)
                val n = maxOf(3, ((w - px * .08f) / pitch).toInt() + 1)
                val s0 = minOf(x0, x1) + (w - (n - 1) * pitch) / 2
                for (i in 0 until n) drawCircle(if (line.alpha > 0f) line else color.copy(alpha = .35f), th / 2, Offset(s0 + i * pitch, y))
            }
        }
        for (r in keys) {
            if (r.last >= l.layoutInput.text.length) continue
            val a = l.getLineForOffset(r.first); val b = l.getLineForOffset(r.last)
            for (line in a..b) {
                val x0 = if (line == a) l.getHorizontalPosition(r.first, true) else l.getLineLeft(line)
                val x1 = if (line == b) l.getHorizontalPosition(r.last + 1, true) else l.getLineRight(line)
                val base = l.getLineBaseline(line)
                drawRoundRect(marker, Offset(minOf(x0, x1) - px * .06f, base - px * .3f), Size(kotlin.math.abs(x1 - x0) + px * .12f, px * .42f), CornerRadius(px * .08f))
            }
        }
    }
    val says = gloss?.ownedSymbols(owner).orEmpty()
    val m2 = if (says.isEmpty() || gloss == null) m else m.semantics {
        customActions = says.map { k -> CustomAccessibilityAction(sayWhat(gloss, k)) { gloss.open = Sheet.Sym(gloss.symbols[k]); true } }
    }
    val st = if (tallHere) style.copy(lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified) else style
    val measured = m2.layout { mm, c ->
        if (c.hasBoundedWidth && c.maxWidth.toFloat() != lineWidth) lineWidth = c.maxWidth.toFloat()
        // once the formulas were laid out for this very width, none may be wider than it
        else if (c.hasBoundedWidth && laidFor == c.maxWidth.toFloat()) MathOverflow.check(widest.second, widest.first - 1f, c)
        val p = mm.measure(c)
        layout(p.width, p.height) { p.place(0, 0) }
    }
    val edges = LocalLineEdges.current
    val laid = { r: TextLayoutResult -> layout = r; dev.hamper.tabula.ui.LabelAudit.split(r); edges?.invoke((0 until r.lineCount).map { r.getLineTop(it) } + r.size.height.toFloat()); Unit }
    // an answer being written: the words added to its end fade in (ui/Pisanie.kt)
    val shown = if (dev.hamper.tabula.ui.LocalNabehSlov.current) measured.nabehKonca(annotated.length) { layout } else measured
    if (textAlign != null) Text(annotated, shown, style = st, color = color, inlineContent = inline, textAlign = textAlign, onTextLayout = laid)
    else Text(annotated, shown, style = st, color = color, inlineContent = inline, onTextLayout = laid)
}

/** Does a formula of [text] need a taller slot than the text's own line ([inlineSlot] centred)? [lead]: [halfLead]. */
private fun hasTallIn(text: String, px: Float, face: MathFace, inks: MathInks, weight: Int, lead: (Float, Float) -> Float): Boolean {
    val w8 = weight.coerceAtMost(700)
    val (fa, fd) = MathEngine.textMetrics(px, w8)
    val room = lead(fa, fd)
    return tokens.findAll(text).any { m ->
        m.groups[1]?.value?.let { tex -> MathEngine.layout(tex, px, face, inks, w8, weight >= 600, text = true).let { inlineSlot(it.asc, it.desc, px, fa, fd, room).centered } } == true
    }
}

/** Half the room [style]'s fixed line height leaves over the text font's ascent [fa] and descent [fd] at [px]; 0 without one. */
private fun androidx.compose.ui.unit.Density.halfLead(style: TextStyle, px: Float, fa: Float, fd: Float): Float {
    val lh = style.lineHeight
    val h = when { lh.isSp -> lh.toPx(); lh.isEm -> lh.value * px; else -> return 0f }
    return maxOf(0f, (h - fa - fd) / 2)
}

/** What the bottom sheet of a screen shows: a tapped symbol, or a referenced definition or theorem. */
sealed interface Sheet {
    data class Sym(val symbol: dev.hamper.tabula.data.Symbol) : Sheet
    data class Ref(val target: dev.hamper.tabula.data.RefTarget) : Sheet
}

/**
 * The tappable things of one screen: which symbols and references are marked there (only the first occurrence of
 * each) and what is open in the sheet. Every step frame makes one; [self] are the ids the screen itself shows (a
 * theorem's card does not link to itself).
 */
class GlossState(val symbols: List<dev.hamper.tabula.data.Symbol>, val refs: dev.hamper.tabula.data.RefIndex?, val self: Set<String> = emptySet()) {
    private val claimed = HashMap<String, Any>()
    var open by mutableStateOf<Sheet?>(null)
    /**
     * Bumped when a text leaves the screen with marks: every text recomputes, so the next occurrence takes the mark
     * (a proof's settled line, the next round's prompt) instead of the screen losing it.
     */
    var version by mutableIntStateOf(0)
        private set
    /** True if [owner] may mark [key] ("sym:3", "ref:t2-18"): nobody on this screen marked it yet, or [owner] already did. */
    fun claim(key: String, owner: Any): Boolean = claimed.getOrPut(key) { owner } === owner
    fun ownedSymbols(owner: Any): List<Int> = claimed.filter { it.value === owner && it.key.startsWith("sym:") }.keys.mapNotNull { it.removePrefix("sym:").toIntOrNull() }.sorted()
    /** Drops what [owner] marked before it marks again (its text changed); the others recompute on the next release. */
    fun forget(owner: Any) { claimed.values.removeAll { it === owner } }
    /** [owner] left the screen with marks: every text claims again, top to bottom, so the first occurrence gets them. */
    fun release(owner: Any) { if (claimed.values.any { it === owner }) { claimed.clear(); version++ } }
}

val LocalGloss = compositionLocalOf<GlossState?> { null }

/** A source label ("Skriptá, Věta 2.18"): its references read as "veta o prieniku podpriestorov, 2.18". */
val LocalRefLabel = compositionLocalOf { false }

/** Colours of the reading aids that are not ink: the key-word marker. */
data class Reading(val marker: Color)
val LocalReading = compositionLocalOf { Reading(Color(0x33C8A050)) }

/** A tap near a marked symbol of [box] (drawn with its baseline at [base], scaled by [k]) opens it in the screen's sheet. */
private fun Modifier.tapMarks(box: MBox, base: Float, gloss: GlossState?, k: Float = 1f): Modifier = tapMarks(box.marks(), box, gloss, k)

/** Pieces of a formula side by side (a display line drawn in pieces): their marks moved by where each piece starts. */
private fun Modifier.tapMarks(boxes: List<MBox>, gloss: GlossState?, k: Float): Modifier {
    var x = 0f
    val marks = boxes.flatMap { b -> val dx = x; x += b.w; b.marks().map { (i, r) -> i to dev.hamper.tabula.compat.graphics.RectF(r.left + dx, r.top, r.right + dx, r.bottom) } }
    return tapMarks(marks, boxes, gloss, k)
}

private fun Modifier.tapMarks(marks: List<Pair<Int, dev.hamper.tabula.compat.graphics.RectF>>, key: Any, gloss: GlossState?, k: Float): Modifier {
    if (gloss == null) return this
    if (marks.isEmpty()) return this
    return pointerInput(key, gloss) {
        detectTapGestures { p ->
            val slack = 24.dp.toPx()
            val x = p.x / k
            val hit = marks.minByOrNull { (_, r) -> if (x < r.left) r.left - x else if (x > r.right) x - r.right else 0f }
            if (hit != null) {
                val r = hit.second
                val dx = (if (x < r.left) r.left - x else if (x > r.right) x - r.right else 0f) * k
                if (dx <= slack && hit.first in gloss.symbols.indices) gloss.open = Sheet.Sym(gloss.symbols[hit.first])
            }
        }
    }
}

/** What TalkBack says instead of LaTeX source. Good enough for these formulas. */
fun speak(t: String): String {
    var s = t
    for ((a, b) in listOf(
        "\\\\tap\\{\\d+\\}" to "",
        "\\\\softbreak(\\[\\d+\\])?" to " ",
        "\\\\U" to "u", "\\\\V" to "v", "\\\\alpha" to "alfa", "\\\\beta" to "beta", "\\\\gamma" to "gama", "\\\\delta" to "delta",
        "\\\\varepsilon" to "epsilon", "\\\\langle" to " obal ", "\\\\rangle" to " ", "\\\\notin" to " nepatrí do ", "\\\\in" to " patrí do ", "\\\\subset" to " je podmnožina ",
        "\\\\ssub" to " je podpriestor ", "\\\\mathbb\\{Z\\}_5\\^4" to "Z päť na štvrtú", "\\\\mathbb\\{Z\\}_5" to "Z päť", "\\\\mathbb\\{R\\}" to "R",
        "\\\\dots" to " až ", "\\\\dim" to "dimenzia", "-" to " mínus ", "=" to " rovná sa ", "\\+" to " plus ",
    )) s = s.replace(Regex(a), b)
    return s.replace(Regex("""\\[a-zA-Z]+"""), " ").replace(Regex("[{}\\\\_^]"), " ").replace(Regex("""\s+"""), " ").trim()
}

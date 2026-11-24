package dev.hamper.tabula.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.nb
import kotlin.math.abs
import kotlin.math.roundToInt
import dev.hamper.tabula.res.*
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun Icon24(id: DrawableResource, desc: String? = null, tint: Color = LocalContentColor.current, modifier: Modifier = Modifier.size(24.dp)) {
    Icon(painterResource(id), desc, modifier, tint)
}

/**
 * A card: flat, a 1 dp line, radius 24; on paper a soft shadow under it, on the board none (the style notes, 1 and 3).
 * [edge] and [depth] are kept for old callers and no longer drawn: there is no pressed lower edge any more.
 */
fun Modifier.edgeCard(
    fill: Color, line: Color, @Suppress("UNUSED_PARAMETER") edge: Color = line, radius: Dp = 24.dp, @Suppress("UNUSED_PARAMETER") depth: Dp = 0.dp, stroke: Dp = 1.dp,
    shadow: Boolean = false,
): Modifier = this
    .then(if (shadow) Modifier.paperShadow(RoundedCornerShape(radius)) else Modifier)
    .background(fill, RoundedCornerShape(radius))
    .border(stroke, line, RoundedCornerShape(radius))

/** `0 1px 0 rgba(60,45,20,.04), 0 10px 26px -16px rgba(60,45,20,.24)`: a hairline and a low, wide shadow. */
fun Modifier.paperShadow(shape: Shape): Modifier = this
    .dropShadow(shape, androidx.compose.ui.graphics.shadow.Shadow(radius = 26.dp, color = Color(0xFF3C2D14), spread = (-16).dp, offset = androidx.compose.ui.unit.DpOffset(0.dp, 10.dp), alpha = .24f))
    .dropShadow(shape, androidx.compose.ui.graphics.shadow.Shadow(radius = 0.dp, color = Color(0xFF3C2D14), offset = androidx.compose.ui.unit.DpOffset(0.dp, 1.dp), alpha = .04f))

@Composable
fun Modifier.card(radius: Dp = 24.dp, fill: Color = T.c.card, @Suppress("UNUSED_PARAMETER") depth: Dp = 0.dp): Modifier {
    val c = T.c
    return edgeCard(fill, c.line, c.line, radius, shadow = !c.dark)
}

/** A surface you read, not press: flat, a 1 dp line. */
@Composable
fun Modifier.panel(radius: Dp = 20.dp, fill: Color = T.c.card): Modifier =
    background(fill, RoundedCornerShape(radius)).border(1.dp, T.c.line, RoundedCornerShape(radius))

/**
 * Something to press: flat (no lower edge, the style notes, 4), its frame [line] [stroke] wide. Under the finger it gives a
 * little (98 %) and darkens a touch, and springs back.
 */
@Composable
fun PressBox(
    onClick: () -> Unit, modifier: Modifier = Modifier, fill: Color, @Suppress("UNUSED_PARAMETER") edge: Color = fill, line: Color = fill, radius: Dp = 16.dp,
    @Suppress("UNUSED_PARAMETER") depth: Dp = 0.dp, enabled: Boolean = true, role: Role = Role.Button, contentAlignment: Alignment = Alignment.Center,
    say: String? = null, stroke: Dp = 1.5.dp, content: @Composable BoxScope.() -> Unit,
) {
    val c = T.c
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val still = calm()
    val give by animateFloatAsState(if (pressed && !still) 1f else 0f, spring(stiffness = 1400f), label = "give")
    val shape = RoundedCornerShape(radius)
    Box(
        modifier
            .semantics(mergeDescendants = true) { if (say != null) contentDescription = say }
            .graphicsLayer { val k = 1f - .02f * give; scaleX = k; scaleY = k }
            .clip(shape).background(fill)
            .drawBehind { if (give > 0f) drawRect(c.ink.copy(alpha = .06f * give)) }
            .then(if (stroke > 0.dp && line != Color.Transparent) Modifier.border(stroke, line, shape) else Modifier)
            .clickable(src, indication = null, enabled = enabled, role = role, onClick = onClick),
        contentAlignment = contentAlignment, content = content,
    )
}

/** The one main action of a screen: the accent, 52 dp, radius 16, 17/700. [hue] is kept for old callers: one accent. */
@Composable
fun Cta(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, icon: DrawableResource? = null,
    @Suppress("UNUSED_PARAMETER") hue: Hue = T.hue,
) {
    val c = T.c
    val hx = rememberHaptics()
    val fill by animateColorAsState(if (enabled) c.lesson.main else c.sand, tween(180), label = "ctaFill")
    val ink by animateColorAsState(if (enabled) c.onAcc else c.ink3, tween(180), label = "ctaInk")
    PressBox({ hx.button(); onClick() }, modifier.fillMaxWidth().heightIn(min = 52.dp), fill, enabled = enabled, stroke = 0.dp) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null && !bigText()) { Icon24(icon, tint = ink); Spacer(Modifier.width(10.dp)) }
            Label1(text, MaterialTheme.typography.labelLarge, ink)
        }
    }
}

/** A large font (over 130 %): a button's icon steps aside, a 24 dp mark beside a giant label only takes its room. */
@Composable
private fun bigText() = LocalDensity.current.fontScale > 1.3f

/** Second action ("ghost"): no fill, a frame in [line] (the rule colour; on a mistake's sheet its own). */
@Composable
fun Second(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: DrawableResource? = null, enabled: Boolean = true, line: Color? = null) {
    val c = T.c
    val hx = rememberHaptics()
    val ink = if (enabled) c.ink else c.ink3
    PressBox({ hx.tap(); onClick() }, modifier.fillMaxWidth().heightIn(min = 52.dp), Color.Transparent, line = line ?: c.edge, enabled = enabled) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null && !bigText()) { Icon24(icon, tint = ink); Spacer(Modifier.width(10.dp)) }
            Label1(text, MaterialTheme.typography.labelLarge, ink)
        }
    }
}

/**
 * A button's label, never cut: one line, a long one shrinks a step (to 80 %); when even that does not fit it wraps,
 * at the largest size (down to 60 %) at which its longest word still fits a line, so no word is broken, and rather
 * on two lines a step smaller (down to 80 %) than on three. The button
 * grows, its [ButtonRow] neighbours with it. All versions are composed, the layout places the one that fits; only the
 * outer node carries the text for TalkBack.
 */
@Composable
private fun Label1(label: String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    // a date, a range, a number with its unit stay on one line ("Ďalšia lekcia: …, 15 min")
    val text = remember(label) { label.nb() }
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val small = style.copy(fontSize = style.fontSize * .8f)
    // a wrapped label is balanced: "Prečo je tam / každá časť", never one word alone on its line (inspector, 18. 11.)
    val wraps = remember(style) { listOf(1f, .9f, .8f, .7f, .6f).map { style.copy(fontSize = style.fontSize * it, lineBreak = androidx.compose.ui.text.style.LineBreak.Heading) } }
    val longest = remember(text) { text.split(' ').maxByOrNull { it.length } ?: text }
    androidx.compose.ui.layout.Layout(
        content = {
            Text(
                text, Modifier.clearAndSetSemantics {}, style = style, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = small.fontSize, maxFontSize = style.fontSize, stepSize = .5.sp),
                onTextLayout = { LabelAudit.check(text, it) },
            )
            for (w in wraps) Text(text, Modifier.clearAndSetSemantics {}, style = w, color = color, textAlign = TextAlign.Center, onTextLayout = { LabelAudit.check(text, it) })
        },
        modifier = Modifier.semantics { this.text = androidx.compose.ui.text.AnnotatedString(label) },
    ) { ms, c ->
        fun width(t: String, st: androidx.compose.ui.text.TextStyle) = measurer.measure(t, st, maxLines = 1, softWrap = false).size.width
        fun lines(st: androidx.compose.ui.text.TextStyle) = measurer.measure(text, st, constraints = androidx.compose.ui.unit.Constraints(maxWidth = c.maxWidth)).lineCount
        val whole = wraps.indexOfFirst { width(longest, it) <= c.maxWidth }.takeIf { it >= 0 } ?: wraps.lastIndex
        // three lines of a big label read worse than two a step smaller: down to 80 % it takes a size that needs two
        val pick = if (width(text, small) <= c.maxWidth) 0 else 1 + ((whole..2).firstOrNull { lines(wraps[it]) <= 2 } ?: whole)
        val p = ms[pick].measure(c.copy(minWidth = 0))
        layout(p.width, p.height) { p.place(0, 0) }
    }
}

/** Tests only: collects every button label laid out cut (ellipsis or overflow) or with a word broken. Off in the app. */
object LabelAudit {
    var on = false
    val cut = mutableSetOf<String>()
    /** Labels whose wrap broke a word in two. */
    val broken = mutableSetOf<String>()
    fun check(text: String, r: androidx.compose.ui.text.TextLayoutResult) {
        if (!on) return
        // width only: a capital with a caron (Ď) rises above its line box, which is not a cut label
        if (r.didOverflowWidth || (0 until r.lineCount).any { r.isLineEllipsized(it) }) cut += text
        if ((0 until r.lineCount - 1).any { i -> r.getLineEnd(i).let { e -> e in 1 until text.length && text[e - 1].isLetter() && text[e].isLetter() } }) broken += text
        split(r)
    }
    /** Texts with a line break inside what must stay together: a date, a range, a number and its unit or name. */
    val split = mutableSetOf<String>()
    fun split(r: androidx.compose.ui.text.TextLayoutResult) {
        if (!on) return
        // a formula stands in the text as its spoken words ("v Z päť 3"): masked, so only the words of the text count
        val src = r.layoutInput.text
        val t = src.getStringAnnotations("androidx.compose.foundation.text.inlineContent", 0, src.length)
            .fold(StringBuilder(src.text)) { b, a -> for (k in a.start until a.end) b[k] = '\uFFFC'; b }.toString()
        for (i in 0 until r.lineCount - 1) { val e = r.getLineEnd(i); if (e in 1 until t.length && dev.hamper.tabula.math.splitsKept(t.substring(0, e), t.substring(e))) split += t.replace('\u00A0', ' ') }
    }
}

/**
 * Buttons side by side: equal widths and one height, the tallest one's, at any width and font size; over 130 % text
 * they stack instead. Every row of Cta / Second buttons goes through here, so no label can make its button taller
 * than its neighbour.
 */
@Composable
fun ButtonRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.ui.layout.Layout(content, modifier.fillMaxWidth()) { ms, c ->
        val gap = 10.dp.roundToPx()
        // very large text: the buttons stack, each the full width, the main one last (nearest the thumb)
        if (fontScale > 1.3f && ms.size > 1) {
            val ps = ms.map { it.measure(androidx.compose.ui.unit.Constraints(c.maxWidth, c.maxWidth, 0, c.maxHeight)) }
            val gapV = 8.dp.roundToPx()
            return@Layout layout(c.maxWidth, ps.sumOf { it.height } + gapV * (ps.size - 1)) { var y = 0; ps.forEach { it.place(0, y); y += it.height + gapV } }
        }
        val n = ms.size.coerceAtLeast(1)
        val w = ((c.maxWidth - gap * (n - 1)) / n).coerceAtLeast(0)
        val h = ms.maxOfOrNull { it.maxIntrinsicHeight(w) } ?: 0
        val ps = ms.map { it.measure(androidx.compose.ui.unit.Constraints.fixed(w, h)) }
        layout(c.maxWidth, h) { var x = 0; ps.forEach { it.place(x, 0); x += w + gap } }
    }
}

@Composable
fun Quiet(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = T.c.ink2, icon: DrawableResource? = null) {
    Box(
        modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // a lesson or step name may carry math ("Zopakovať: Parameter $a$"): it is set, never shown as markup
            if ('$' in text || '§' in text) Rich(text, style = MaterialTheme.typography.titleSmall, color = color, textAlign = TextAlign.Center)
            else Text(text, style = MaterialTheme.typography.titleSmall, color = color, textAlign = TextAlign.Center, onTextLayout = { LabelAudit.check(text, it) })
            // where it leads (opens below, goes on), as an icon of the app, never a text arrow
            if (icon != null) { Spacer(Modifier.width(2.dp)); Icon24(icon, tint = color, modifier = Modifier.size(20.dp)) }
        }
    }
}

enum class Pick { Idle, Picked, Right, Wrong }

/**
 * Answer option. Carries math. Idle: paper with a thin line; [Pick.Picked]: the accent's soft tint and a thick accent
 * frame; [Pick.Right]: full accent with a check badge on its corner; [Pick.Wrong]: terracotta frame and tint with an x
 * badge. Colours, frame and badge move between the states (180 ms), so a pick reads as one motion.
 */
@Composable
fun Choice(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, state: Pick = Pick.Idle, center: Boolean = false, enabled: Boolean = true,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium, minHeight: Dp = 60.dp, tag: String? = null,
    reveal: Boolean = false, hint: Boolean = false, quiet: Boolean = false, dim: Boolean = false,
) {
    val c = T.c
    val hx = rememberHaptics()
    // [quiet]: an option left over once the question is answered steps back into the page, its text in the second ink,
    // still read at 4.5 : 1 and more (judge, 18. 11.: "−7" and "7" at half opacity were too faint)
    val fill by animateColorAsState(when (state) { Pick.Right -> c.good; Pick.Wrong -> c.badTint; Pick.Picked -> c.lesson.tint2; else -> if (quiet) androidx.compose.ui.graphics.lerp(c.card, c.bg, .6f) else c.card }, tween(180), label = "fill")
    val line by animateColorAsState(when (state) { Pick.Right -> c.good; Pick.Wrong -> c.bad; Pick.Picked -> c.lesson.main; else -> c.line }, tween(180), label = "line")
    val ink by animateColorAsState(when (state) { Pick.Right -> c.onAcc; Pick.Wrong -> c.badInk; Pick.Picked -> c.lesson.text; else -> if (quiet) c.ink2 else c.ink }, tween(180), label = "ink")
    val stroke by animateDpAsState(when (state) { Pick.Picked -> 2.5.dp; Pick.Right, Pick.Wrong -> 2.dp; else -> 1.5.dp }, tween(180), label = "stroke")
    val shake = remember { Animatable(0f) }
    val swell = remember { Animatable(1f) }
    val still = calm()
    // the motion starts in the frame the haptic plays: the caller buzzes in the same click that sets [state]
    LaunchedEffect(state) {
        if (still) return@LaunchedEffect
        if (state == Pick.Wrong) shake.animateTo(0f, keyframes { durationMillis = 320; -6f at 60; 5f at 130; -3f at 200; 2f at 260 })
        if (state == Pick.Right) { swell.animateTo(1.03f, tween(90)); swell.animateTo(1f, Motion.pop()) }
    }
    // the answer just checked ([reveal]) is never left under the feedback sheet: it tells the step where it ends, and
    // the step scrolls the body until it stands above the sheet (StepFrame)
    val sink = LocalReveal.current
    val shown = reveal && (state == Pick.Right || state == Pick.Wrong)
    Box(modifier.then(if (sink != null && shown) Modifier.onGloballyPositioned { sink(it.positionInRoot().y + it.size.height) } else Modifier).graphicsLayer { translationX = shake.value * density; scaleX = swell.value; scaleY = swell.value }) {
        // [dim]: a pick tried and rejected before steps back. Only the button fades: a faded layer around the whole
        // option would clip the badge standing out over its corner (Teo, 18. 11.: the x was cut off)
        PressBox(
            { hx.choose(); onClick() }, Modifier.fillMaxWidth().heightIn(min = minHeight).then(if (dim) Modifier.graphicsLayer { alpha = .5f } else Modifier), fill, line = line, stroke = stroke,
            radius = 16.dp, enabled = enabled, contentAlignment = if (center) Alignment.Center else Alignment.CenterStart, say = dbg(tag),
        ) {
            Row(Modifier.padding(horizontal = if (center) 8.dp else 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                // an answer holds no marks: a tap anywhere on it picks it
                CompositionLocalProvider(dev.hamper.tabula.math.LocalGloss provides null) {
                    // [hint]: an empty field's name as a question ("gcd = ?"), quiet like any placeholder: a bare "gcd"
                    // in the answer's own type read as a value typed in (inspector, 18. 11.)
                    Rich(if (hint) asked(text) else text, Modifier.weight(1f, fill = !center), style = if (hint) style.copy(fontWeight = FontWeight.Normal, fontSize = style.fontSize * .85f) else style,
                        color = if (hint) c.ink2 else ink, textAlign = if (center) TextAlign.Center else null)
                }
                // a long answer keeps room on its right for the badge on its corner
                if (!center && (state == Pick.Right || state == Pick.Wrong)) Spacer(Modifier.width(12.dp))
            }
        }
        if (state == Pick.Right || state == Pick.Wrong) AnswerBadge(state == Pick.Right, Modifier.align(Alignment.TopEnd).offset(7.dp, (-7).dp))
    }
}

/** A field's name as the question it waits for: "$gcd$" -> "$gcd = ?$" (a name that already has its "=" stays). */
private fun asked(name: String): String {
    val t = name.trim()
    if ('=' in t || !(t.startsWith('$') && t.endsWith('$') && t.length > 2)) return name
    return "$" + t.substring(1, t.length - 1) + " = {?}$"
}

/** The badge on the corner of a checked answer: a check on paper ringed in the accent, or an x on terracotta. */
@Composable
fun AnswerBadge(ok: Boolean, modifier: Modifier = Modifier) {
    val c = T.c
    Box(
        modifier.popIn(ok).size(26.dp).clip(CircleShape).background(c.bg).padding(2.dp).clip(CircleShape)
            .background(if (ok) c.good else c.bad).padding(if (ok) 2.dp else 0.dp).clip(CircleShape).background(if (ok) c.card else c.bad),
        contentAlignment = Alignment.Center,
    ) { Icon24(if (ok) Res.drawable.ic_check else Res.drawable.ic_x, tint = if (ok) c.good else c.onBad, modifier = Modifier.size(14.dp)) }
}

/** A remark next to the picture: small, soft, one idea. */
@Composable
fun Bubble(text: String, modifier: Modifier = Modifier, fill: Color = T.c.card, line: Color = T.c.line, color: Color = T.c.ink, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)) {
    Box(
        modifier.background(fill, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 5.dp))
            .border(1.5.dp, line, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 5.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) { Rich(text, style = style, color = color) }
}

/**
 * The verdict of an answer, at the top of its sheet: 26/750 in the accent's or terracotta's text colour, what it means
 * under it. Pytagoras only at a right answer, the moment worth marking: beside a mistake his portrait taught nothing
 * and read as decoration (judge, 18. 11.: "smutný profesor v karte chyby je dekorácia"); there the explanation is all.
 */
@Composable
fun Verdict(
    title: String, body: String?, ok: Boolean, modifier: Modifier = Modifier, bodyStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    cheer: Boolean = ok,
) {
    val c = T.c
    // [cheer] false: right, but not at the first try; no one celebrates a second attempt (Teo, 18. 11.)
    SheetHead(title, if (ok && cheer) Mood.Pleased else null, if (ok) c.lesson.text else c.badInk, ok, modifier) {
        // on a mistake's sheet "Prečo?" is terracotta too: one colour per sheet
        if (body != null) { Spacer(Modifier.height(6.dp)); Explain(body, style = bodyStyle, color = c.ink, accent = if (ok) c.lesson.text else c.badInk) }
    }
}

/**
 * A sheet's head: the title next to Pytagoras in [mood] (none: the title alone), what follows under it. It rises into place: a right answer
 * with a little bounce, anything else settles without.
 */
@Composable
fun SheetHead(
    title: String, mood: Mood?, color: Color, good: Boolean, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = VerdictStyle,
    rest: @Composable ColumnScope.() -> Unit = {},
) {
    val still = calm()
    val up = remember { Animatable(if (still) 0f else 1f) }
    LaunchedEffect(Unit) { if (!still) up.animateTo(0f, if (good) Motion.rise() else Motion.settle()) }
    val inCard = LocalSheetCard.current
    Column(modifier.fillMaxWidth().then(if (inCard) Modifier.padding(start = 18.dp, end = 14.dp, top = 14.dp, bottom = 10.dp) else Modifier.padding(bottom = 8.dp)).graphicsLayer { translationY = up.value * 24.dp.toPx(); alpha = (1f - up.value * 1.6f).coerceIn(0f, 1f) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // a long head is never set in the verdict's large type: it would wrap over lines and tear its formula;
            // content heads go through verdictHead, this keeps any other one readable
            val long = style == VerdictStyle && shownLength(title) > VerdictHeadMax
            Rich(title, Modifier.weight(1f), style = if (long) MaterialTheme.typography.titleMedium else style, color = color)
            // part of the sheet, beside the verdict; 64 dp, the size the character sheet is checked at
            // B lets the math speak: no character beside the verdict
            if (mood != null && SmerNow != Smer.Sadzba) { Spacer(Modifier.width(12.dp)); Pytagoras(mood, Modifier.popIn(good), bg = if (mood == Mood.Thinking && !good) T.c.art.mascotBad else T.c.art.mascotGood, size = 64.dp) }
        }
        rest()
    }
}

/**
 * Lesson progress: one continuous bar, 6 dp, the sunk track and the accent (the style notes, 4). A new step fills on from where
 * the last one stood; a proof inside the step fills its share of the step part by part.
 */
@Composable
fun StepBar(step: Int, total: Int, modifier: Modifier = Modifier, part: StepPart? = null, pieces: List<IntRange>? = null) {
    val c = T.c
    val still = calm()
    val inStep = if (part != null && part.total > 1) part.done.toFloat() / part.total else 1f
    // the step on screen counts as begun: step 1 of 25 shows its share, never an empty bar
    val target = (step - 1 + if (part != null && part.total > 1) inStep else 1f).coerceAtLeast(0f)
    // every screen draws its own bar, so the bar starts where the last one stood: on (grows from there) or back (at
    // once). Never from the step number alone: a proof's part or a definition's screen inside a step stood between two
    // whole steps, and a bar starting at the whole step jumped ahead and back (Teo, 18. 11.: "preblikne")
    val fill = remember(total) { Animatable(BarMemory.from(total, target)) }
    val sheen = remember { Animatable(0f) }
    LaunchedEffect(step, total, target) {
        BarMemory.shown(total, target)
        if (still || fill.value > target) { fill.snapTo(target); return@LaunchedEffect }
        if (fill.value == target) return@LaunchedEffect
        delay(120)
        // no bounce: a spring past the target and back read as the bar jumping ahead and back
        fill.animateTo(target, androidx.compose.animation.core.spring(androidx.compose.animation.core.Spring.DampingRatioNoBouncy, 380f))
        sheen.snapTo(1f); sheen.animateTo(0f, tween(450))
    }
    // a lesson's bar is cut into its pieces, each as long as its steps: the fill runs through them as through one bar
    val cuts = pieces?.takeIf { it.size > 1 }
    val where = cuts?.let { ps -> ps.indexOfFirst { step - 1 in it }.let { i -> "Kúsok ${i + 1} z ${ps.size}, " } } ?: ""
    if (cuts != null) Box(
        modifier.semantics { contentDescription = where + "krok $step z $total" + (part?.let { ", dôkaz ${it.done} z ${it.total}" } ?: "") }
            .fillMaxWidth().height(6.dp)
            .drawBehind {
                BarTrace.sink?.invoke(fill.value)
                val gap = 3.dp.toPx()
                val room = size.width - gap * (cuts.size - 1)
                val r = CornerRadius(size.height / 2)
                var x = 0f
                for (p in cuts) {
                    val w = room * p.count() / total.coerceAtLeast(1)
                    drawRoundRect(c.sand, Offset(x, 0f), Size(w, size.height), r)
                    val f = w * ((fill.value - p.first) / p.count()).coerceIn(0f, 1f)
                    if (f > 0f) {
                        drawRoundRect(c.lesson.main, Offset(x, 0f), Size(f, size.height), r)
                        if (sheen.value > 0f) drawRoundRect(Color.White.copy(alpha = .4f * sheen.value), Offset(x, 0f), Size(f, size.height), r)
                    }
                    x += w + gap
                }
            },
    ) else Box(
        modifier.semantics { contentDescription = "Krok $step z $total" + (part?.let { ", dôkaz ${it.done} z ${it.total}" } ?: "") }
            .fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.sand)
            .drawBehind {
                val w = size.width * (fill.value / total.coerceAtLeast(1)).coerceIn(0f, 1f)
                BarTrace.sink?.invoke(fill.value)
                if (w > 0f) {
                    drawRoundRect(c.lesson.main, size = Size(w, size.height), cornerRadius = CornerRadius(size.height / 2))
                    // the share just filled catches the light once
                    if (sheen.value > 0f) drawRoundRect(Color.White.copy(alpha = .4f * sheen.value), size = Size(w, size.height), cornerRadius = CornerRadius(size.height / 2))
                }
            },
    )
}

/**
 * Where the step bar stood last (in steps), so the next screen's bar starts there instead of jumping. A lesson
 * [reset]s it when it opens: its first bar starts at its own place, not at another lesson's.
 */
object BarMemory {
    private var total = -1; private var value = 0f
    /** Where a bar heading for [target] starts: the last place, or [target] when that was further (going back). */
    fun from(total: Int, target: Float): Float = if (total == this.total) minOf(value, target) else target
    fun shown(total: Int, value: Float) { this.total = total; this.value = value }
    fun reset() { total = -1 }
}

/** Tests: every value the step bar draws, frame by frame (the bar never jumps ahead and back). */
object BarTrace { var sink: ((Float) -> Unit)? = null }

/** In a lesson: what a tap on the step bar does (opens the list of steps reached). Null elsewhere. */
val LocalStepBarTap = compositionLocalOf<(() -> Unit)?> { null }

/** Steps of a lesson tell their frame how they come and go: the header stays put, the rest moves. */
val LocalStepMotion = compositionLocalOf<StepMotion?> { null }

/** The step's place in its AnimatedContent and which way the lesson goes. */
class StepMotion(val scope: AnimatedVisibilityScope, val forward: Boolean, val outer: StepMotion? = null)

/** Inside a step's AnimatedContent: hands the frame its motion. */
@Composable
fun AnimatedVisibilityScope.StepMotionOf(forward: Boolean, content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalStepMotion provides StepMotion(this, forward, LocalStepMotion.current), content = content)

/** A step's own AnimatedContent spec: nothing moves as a whole; the frame's parts move themselves (see [LocalStepMotion]). */
val StepSwap: ContentTransform get() = EnterTransition.None togetherWith ExitTransition.None

@Composable
private fun Modifier.stepPart(): Modifier {
    if (calm()) return this
    // a step inside a step (a definition's phases) moves with both: the lesson's change and its own
    var out = this
    var m = LocalStepMotion.current
    while (m != null) { out = out.stepPartOf(m); m = m.outer }
    return out
}

private fun Modifier.stepPartOf(m: StepMotion): Modifier {
    return with(m.scope) {
        animateEnterExit(
            enter = fadeIn(tween(180)) + slideInHorizontally(Motion.settle()) { if (m.forward) it / 10 else -it / 10 },
            exit = fadeOut(tween(90)),
        )
    }
}

@Composable
private fun Modifier.stepHeader(): Modifier {
    // the old header leaves at once and the new one is already there: the bar seems never to move
    var out = this
    var m = LocalStepMotion.current
    while (m != null) { out = with(m.scope) { out.animateEnterExit(enter = EnterTransition.None, exit = fadeOut(snap())) }; m = m.outer }
    return out
}

/** Pops in on first appearance: with a bounce for [good] news, settling for bad. */
@Composable
fun Modifier.popIn(good: Boolean): Modifier {
    val still = calm()
    val a = remember { Animatable(if (still) 1f else .4f) }
    LaunchedEffect(Unit) { if (!still) a.animateTo(1f, if (good) Motion.pop() else Motion.settle()) }
    return graphicsLayer { scaleX = a.value; scaleY = a.value }
}

/**
 * Frame of every step. The footer is always in the same place, so the thumb knows where to go.
 *
 * Feedback (a step with a [footerTint]) never moves the picture: the body keeps the height it had above the resting
 * footer, and the footer becomes a panel as tall as what it says, at the bottom; a taller one rises over the bottom of
 * the body. A scrolling body gets room at its end to scroll out from under the panel.
 */
/**
 * The pieces of the lesson on screen (data/Pieces.kt) as ranges of its steps: its header's bar is cut into them and
 * says nothing in words. Null outside a lesson.
 */
val LocalStepPieces = androidx.compose.runtime.compositionLocalOf<List<IntRange>?> { null }

/** Why the thing on screen is back in today's review ("Vracia sa: **včera zle**"); [wrong]: it went wrong. */
data class StepReason(val text: String, val wrong: Boolean)
/** Set by the review round (screens/Home.kt, ReviewScreen): one line under the step's header, none elsewhere. */
val LocalStepReason = androidx.compose.runtime.compositionLocalOf<StepReason?> { null }

@Composable
fun StepFrame(
    step: Int, total: Int, onClose: () -> Unit, modifier: Modifier = Modifier, closeLabel: String = "Zavrieť", trailing: (@Composable RowScope.() -> Unit)? = null,
    scroll: Boolean = true, footerTint: Color? = null, part: StepPart? = null, unit: String = "krok", verdictCard: Boolean = false,
    footer: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit,
) {
    val c = T.c
    // a screen marks its references (and on a reading screen its symbols); a tapped one opens in the bottom panel,
    // over a dimmed screen
    val symbols = LocalGlossary.current.orEmpty()
    val refs = LocalRefs.current
    val self = LocalReadingSelf.current
    val gloss = remember(symbols, refs, self) { if (symbols.isEmpty() && refs == null) null else dev.hamper.tabula.math.GlossState(symbols, refs, self) }
    val open = gloss?.open
    if (open != null) dev.hamper.tabula.compat.BackHandler { gloss.open = null }
    // one call either way, so the body keeps its scroll and state while the sheet is up
    // the full card of a reference opens over this screen; closing it comes back to the sheet
    var card by remember { mutableStateOf<String?>(null) }
    val full = LocalFullCard.current
    val sheet: (@Composable ColumnScope.() -> Unit)? = open?.let { o -> { ReadingSheet(o, { gloss.open = it }, { card = it }) { gloss.open = null } } }
    card?.let { r -> if (full != null) androidx.compose.ui.window.Dialog({ card = null }, dev.hamper.tabula.compat.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // a screen of its own: not a step of the lesson under it (no shared step motion), so it has its own background
        // nor does it keep its state in the lesson's step under it
        CompositionLocalProvider(LocalStepMotion provides null, LocalStepNav provides null, LocalStepBarTap provides null, LocalStepPieces provides null, LocalStepReason provides null) { AppColumn { full(r) { card = null } } }
    } }
    CompositionLocalProvider(dev.hamper.tabula.math.LocalGloss provides gloss) {
        StepFrameBody(step, total, onClose, modifier, closeLabel, trailing, scroll, if (sheet != null) c.card else footerTint, part, unit, sheet ?: footer, sheet != null, { gloss?.open = null }, verdictCard && sheet == null, content)
    }
}

@Composable
private fun StepFrameBody(
    step: Int, total: Int, onClose: () -> Unit, modifier: Modifier, closeLabel: String, trailing: (@Composable RowScope.() -> Unit)?,
    scroll: Boolean, footerTint: Color?, part: StepPart?, unit: String,
    footer: (@Composable ColumnScope.() -> Unit)?, dim: Boolean, onDim: () -> Unit, verdictCard: Boolean, content: @Composable ColumnScope.() -> Unit,
) {
    val c = T.c
    val d = LocalDensity.current
    val still = calm()
    val feedback = footerTint != null && footer != null
    // the resting footer's height (what the body leaves free) and the footer's height now
    var restPx by remember { mutableIntStateOf(0) }
    var footPx by remember { mutableIntStateOf(0) }
    var bottomAt by remember { mutableFloatStateOf(0f) }
    var contentEnd by remember { mutableFloatStateOf(0f) }
    val panelTop = if (bottomAt > 0f && footPx > 0) bottomAt - footPx else 0f
    val rest = if (footer == null) 0 else restPx
    val overlap = with(d) { (if (feedback) (footPx - rest).coerceAtLeast(0) else 0).toDp() }
    val bodyScroll = rememberScrollState()
    // where the body's window starts and where the question stands (root px), so a scroll never cuts its letters
    var viewTop by remember { mutableFloatStateOf(0f) }
    var viewBottom by remember { mutableFloatStateOf(0f) }
    var whole by remember { mutableStateOf<Pair<Float, List<Float>>?>(null) }
    val keepWhole = remember { { top: Float, edges: List<Float> -> if (edges.size > 1) whole = top to edges } }
    // inside a lesson the screen behind already has the background: without its own, the step leaving shows through
    // the one arriving, so the two crossfade instead of the new one covering the old with an empty page
    Box(modifier.fillMaxSize().then(if (LocalStepMotion.current == null) Modifier.background(c.bg) else Modifier)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Column(Modifier.stepHeader().fillMaxWidth()) {
            // outside a lesson, where you are in words: "časť 2 z 5" (a bare "2 z 5" said nothing; design review 18. 11.); at a large font
            // the words keep the row and the bar goes under it at full width, so neither breaks nor squeezes the other
            val bigHead = d.fontScale >= 1.5f
            val barTap = LocalStepBarTap.current
            val pieces = LocalStepPieces.current?.takeIf { it.isNotEmpty() && it.last().last + 1 == total }
            val bar = @Composable { m: Modifier ->
                // in a lesson the whole bar is one big target: it lists the steps reached so far
                Box(
                    m.clip(RoundedCornerShape(14.dp)).then(if (barTap != null) Modifier.clickable(role = Role.Button, onClickLabel = "Kroky lekcie", onClick = barTap) else Modifier),
                    contentAlignment = Alignment.Center,
                ) { StepBar(step, total, Modifier.fillMaxWidth(), part, pieces) }
            }
            // in a lesson the bar alone, cut into the lesson's pieces: one quiet measure, not a bar and a count of
            // pieces or steps beside it (Teo, 19. 11.)
            val count = if (pieces != null) null else "$unit\u00A0$step\u00A0z\u00A0$total"
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 6.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose).semantics { contentDescription = closeLabel }, contentAlignment = Alignment.Center) {
                    Icon24(Res.drawable.ic_close, tint = c.ink)
                }
                Spacer(Modifier.width(4.dp))
                if (total > 0 && count == null) {
                    bar(Modifier.weight(1f).heightIn(min = 48.dp))
                    Spacer(Modifier.width(12.dp))
                } else if (total > 0 && count != null && !bigHead) {
                    bar(Modifier.weight(1f).heightIn(min = 48.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(count, style = MaterialTheme.typography.labelMedium, color = c.ink2, softWrap = false)
                    Spacer(Modifier.width(2.dp))
                } else if (total > 0 && count != null) {
                    Text(count, Modifier.weight(1f).padding(start = 6.dp), style = MaterialTheme.typography.labelMedium, color = c.ink2)
                } else Spacer(Modifier.weight(1f))
                // the tutor, wherever the screen says where the student is (docs/TUTOR.md)
                dev.hamper.tabula.screens.TutorVstup()
                trailing?.invoke(this)
                if (trailing == null && total > 0) Spacer(Modifier.width(48.dp))
            }
            if (total > 0 && bigHead && count != null) bar(Modifier.fillMaxWidth().padding(horizontal = 20.dp).heightIn(min = 24.dp))
            // in review: why a mistake is back, under where it stands in today's dose
            LocalStepReason.current?.let { r ->
                Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp).clip(RoundedCornerShape(12.dp)).background(if (r.wrong) c.badTint else c.sand).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Rich(r.text, style = MaterialTheme.typography.bodySmall, color = c.ink)
                }
            }
            }
            // the answer just checked stays above the feedback panel: at a large font the panel is tall enough to cover
            // it (inspector, 18. 11.), so the body scrolls by what the panel covers and a little air; at 100 % it
            // covers nothing and nothing moves
            var revealAt by remember { mutableFloatStateOf(0f) }
            val reveal = remember { { y: Float -> revealAt = y } }
            LaunchedEffect(feedback) { if (!feedback) revealAt = 0f }
            LaunchedEffect(feedback, panelTop, revealAt > 0f, whole, viewTop) {
                if (!feedback || !scroll || panelTop <= 0f || revealAt <= 0f) return@LaunchedEffect
                // at once, while the panel rises: the answer never passes under it
                var by = (revealAt + with(d) { 12.dp.toPx() } - panelTop).coerceAtLeast(0f)
                if (by <= 1f && bodyScroll.value == 0) return@LaunchedEffect
                // the edge of the window falls between two lines of the question, never through its letters
                // (inspector, 18. 11.: "665 · 7 mod 666?" cut in half at 360 dp, 200 %): a whole line more goes
                whole?.let { (top, edges) ->
                    val cut = viewTop - (top - by)
                    // the first line edge at or below the cut (1 px of slack, so a cut on an edge stays there)
                    if (cut > 1f && cut < edges.last() - 1f) edges.firstOrNull { it >= cut - 1f }?.let { by += it - cut }
                }
                if (by > 1f) bodyScroll.scrollBy(by)
            }
            // more below the window's edge: the page fades out there, so a card cut by the edge never reads as its end
            // (Teo, 18. 11.: a list of seven subspaces cut flat after the fourth, as if that were all)
            val fade = if (!scroll) Modifier else Modifier.drawWithContent {
                drawContent()
                if (bodyScroll.canScrollForward) {
                    val h = 32.dp.toPx()
                    drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(c.bg.copy(alpha = 0f), c.bg), startY = size.height - h, endY = size.height), androidx.compose.ui.geometry.Offset(0f, size.height - h), androidx.compose.ui.geometry.Size(size.width, h))
                }
            }
            val body = Modifier.weight(1f).fillMaxWidth().stepPart().onGloballyPositioned { viewTop = it.positionInRoot().y; viewBottom = viewTop + it.size.height }.then(fade).then(if (scroll) Modifier.verticalScroll(bodyScroll) else Modifier).padding(horizontal = 20.dp)
            // after a scroll the step made itself (an answer brought into view), the window's top edge moves on to the
            // next line edge of the text it cut, so no line stands cut in half under the header (inspector, 18. 11.)
            // a body scrolled to its end gets the room the move needs under its content
            var alignRoom by remember { mutableFloatStateOf(0f) }
            val alignTop: suspend () -> Unit = remember {
                suspend {
                    repeat(2) { androidx.compose.runtime.withFrameNanos { } }
                    whole?.let { (top, edges) ->
                        val cut = viewTop - top
                        if (cut > 1f && cut < edges.last() - 1f) edges.firstOrNull { it >= cut - 1f }?.let { e ->
                            val by = e - cut
                            if (by > 1f) {
                                val room = (bodyScroll.maxValue - bodyScroll.value).toFloat()
                                if (by > room) { alignRoom = by - room + 1f; repeat(2) { androidx.compose.runtime.withFrameNanos { } } }
                                bodyScroll.scrollBy(by)
                            }
                        }
                    }
                }
            }
            CompositionLocalProvider(LocalFooterOverlap provides overlap, LocalFooterMeasured provides (footer == null || restPx > 0), LocalReveal provides reveal, LocalKeepWhole provides keepWhole, LocalAlignTop provides alignTop, LocalSeenUntil provides (if (panelTop > 0f) panelTop else viewBottom)) {
                Column(body) {
                    content()
                    Spacer(Modifier.onGloballyPositioned { contentEnd = it.positionInRoot().y })
                    // room for the scroll past the panel, and for one line more of the question (the scroll stops on its edge)
                    val line = whole?.second?.zipWithNext { a, b -> b - a }?.maxOrNull() ?: 0f
                    if (scroll && overlap > 0.dp) Spacer(Modifier.height(overlap + with(d) { line.toDp() }))
                    if (alignRoom > 0f) Spacer(Modifier.height(with(d) { alignRoom.toDp() }))
                }
            }
            // under a feedback panel shorter than the resting footer the window reaches down to the panel: a scrolling
            // body keeps its top (nothing moves) and shows more; before, the band the resting footer had left stayed
            // empty and a long solution stood cut above it (Teo, 18. 11.). A body that does not scroll keeps its height.
            // the comparison's variant of the tutor's entry: a quiet line above the buttons (screenshots only)
            if (footer != null && !feedback) Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) { dev.hamper.tabula.screens.TutorRiadok() }
            val under = if (feedback && scroll && footPx in 1 until rest) footPx else rest
            if (footer != null) Spacer(Modifier.height(with(d) { under.toDp() })) else Spacer(Modifier.navigationBarsPadding())
        }
        // under an open symbol sheet the screen dims; a tap on it closes the sheet
        val scrim by animateColorAsState(if (dim) Color.Black.copy(alpha = if (c.dark) .45f else .22f) else Color.Transparent, if (still) snap() else tween(200), label = "scrim")
        if (dim) Box(Modifier.fillMaxSize().background(scrim).clickable(remember { MutableInteractionSource() }, indication = null, onClickLabel = "Zavrieť vysvetlenie", onClick = onDim))
        // where the panel's top rests, from the layout (its slide in is a layer that moves it without a new layout)
        if (footer != null) Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 52.dp).onGloballyPositioned { bottomAt = it.positionInRoot().y + it.size.height }, contentAlignment = Alignment.BottomCenter) {
            val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            // the verdict's tint fades in under it and the panel grows upward to fit it; with nothing under it
            // before (a check in the body), the panel slides up from the bottom edge
            // the directions of the look (ui/Smer.kt): A the sheet on squared paper, B no fill and a rule in the verdict's
            // colour, C a right answer's whole panel in it with white type (a mistake keeps the light panel: in full
            // terracotta, seen many times, it outshouted the math; critic, 18. 11.)
            val smer = SmerNow
            val bad = footerTint != null && footerTint == c.badTint
            // only a right answer fills its panel: a guess and a shown solution are no verdict, their panels keep the
            // light tint they ask for (Teo, 24. 11.: a guess stood in the page's dark ink on the full green)
            val filled = smer.bold && feedback && footerTint == c.goodTint
            val solid = if (bad) c.bad else if (smer.bold) goodFill(c) else c.good
            val tint by animateColorAsState(if (filled) solid else if (smer == Smer.Sadzba && feedback) c.bg else footerTint ?: c.bg, if (still) snap() else tween(200), label = "footerTint")
            val top by animateDpAsState(if (feedback) 20.dp else 8.dp, if (still) snap() else Motion.settle(), label = "footerTop")
            val slide = remember(feedback) { Animatable(if (feedback && rest == 0 && !still) 1f else 0f) }
            LaunchedEffect(feedback) { if (slide.value > 0f) slide.animateTo(0f, Motion.settle()) }
            // a check whose answers end high on a tall screen (judge, 18. 11.: an empty band of page between the answers
            // and the sheet): the verdict is a card right under the answers, the way on stays at the bottom, where the
            // thumb is, on the page like any step's button (round 8 tried the buttons right under the card: a judge read
            // them as out of the thumb's reach with an unfinished empty bottom under them; the old app's panel at the
            // thumb won over it)
            val short = bodyScroll.value == 0 && contentEnd > 0f && contentEnd <= viewBottom
            val meet = if (verdictCard && feedback && short && bottomAt > 0f) with(d) { (bottomAt - contentEnd - 8.dp.toPx()).toDp() } else 0.dp
            val card = meet > 120.dp && smer == Smer.Dnes
            val arrange = remember { HeadTop() }
            Column(
                Modifier.stepPart().fillMaxWidth()
                    .heightIn(min = if (card) meet else 0.dp)
                    .onSizeChanged { footPx = it.height; if (!feedback) restPx = it.height }
                    .graphicsLayer { translationY = slide.value * size.height }
                    .background(if (feedback && !card) tint else c.bg, if (smer == Smer.Sadzba) androidx.compose.ui.graphics.RectangleShape else shape)
                    .then(if (feedback && smer == Smer.Zosit) Modifier.clip(shape).squaredPaper(c.art.grid.copy(alpha = .7f)) else Modifier)
                    .then(if (feedback && smer == Smer.Sadzba) Modifier.drawBehind { drawRect(solid, size = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx())) } else Modifier)
                    .then(if (card) Modifier.drawBehind {
                        val x = 20.dp.toPx(); val y = top.toPx()
                        drawRoundRect(tint, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Size(size.width - 2 * x, arrange.headEnd.toFloat()), androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()))
                    } else Modifier)
                    // the panel takes the taps that land on it; nothing under it reacts
                    .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
                    .then(if (still) Modifier else Modifier.growUpward())
                    // never over the header: taller than the room under it (huge font, an opened Prečo?), it scrolls
                    // anchored at the bottom: while the panel grows (an opened Prečo?) the buttons stay where they are
                    // only as tall as what it says: a short "Správne." is a short panel, never a tall empty green one
                    .verticalScroll(rememberScrollState(), reverseScrolling = true)
                    .navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = top, bottom = 14.dp),
                // the way on sits at the bottom, where the thumb is, however tall the panel
                verticalArrangement = arrange, content = { CompositionLocalProvider(LocalSheetCard provides card, LocalPalette provides (if (filled) solidOn(c, solid) else c)) { footer(this) } },
            )
        }
    }
}

/**
 * A panel's parts 8 dp apart at its bottom, where the thumb is; in a panel taller than what it says, its first part
 * (the verdict) stays at the top, under the answer it is about. [headEnd]: where that first part ends, for its card.
 */
private class HeadTop : Arrangement.Vertical {
    var headEnd = 0
    override val spacing = 8.dp
    override fun androidx.compose.ui.unit.Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        val gap = spacing.roundToPx()
        var y = totalSize
        for (i in sizes.indices.reversed()) { y -= sizes[i]; outPositions[i] = y; y -= gap }
        if (sizes.size > 1 && outPositions[0] > 0) outPositions[0] = 0
        headEnd = if (sizes.isEmpty()) 0 else outPositions[0] + sizes[0]
    }
}

/** Reports where the question stands (root px) and its line edges (px from its top): the step never scrolls through its letters. */
val LocalKeepWhole = compositionLocalOf<((Float, List<Float>) -> Unit)?> { null }

/** After the step scrolled by itself: moves the window's top on to the next line edge of the text it reported ([KeepWhole]). */
val LocalAlignTop = compositionLocalOf<(suspend () -> Unit)?> { null }

/** The verdict stands in a card of its own under the answers, the way on at the bottom of the page (a short check step). */
val LocalSheetCard = compositionLocalOf { false }

/** The resting footer has been measured, so the body has its final height (the first frame lays it out without one). */
val LocalFooterMeasured = compositionLocalOf { true }

/** Where the step's body stops being seen (root px): the top of its panel, else the body's bottom; 0 unknown. */
val LocalSeenUntil = compositionLocalOf { 0f }

/** How far the step's feedback panel reaches over the body, for a body that scrolls by itself. */
val LocalFooterOverlap = compositionLocalOf { 0.dp }

/** Where the answer just checked ends on screen (root px): the step keeps it above its feedback panel. */
val LocalReveal = compositionLocalOf<((Float) -> Unit)?> { null }

/**
 * [text] at the first of [styles] (largest first) at which each of its words fits the width on one line, so no word
 * breaks inside ("Kongruen/cie"); the last when none fits. Words are what a space separates: "a\u00A0inverzia" (nb())
 * stays one unit.
 */
@Composable
fun WholeWordsText(text: String, styles: List<androidx.compose.ui.text.TextStyle>, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val tm = androidx.compose.ui.text.rememberTextMeasurer()
    BoxWithConstraints(modifier) {
        val words = text.split(' ').filter { it.isNotEmpty() }
        val st = styles.firstOrNull { s -> words.all { tm.measure(it, s, softWrap = false).size.width <= constraints.maxWidth } } ?: styles.last()
        Text(text, style = st, color = color)
    }
}

@Composable
fun H1(text: String, modifier: Modifier = Modifier, color: Color = T.c.ink) {
    val style = MaterialTheme.typography.headlineSmall
    KeepWhole(modifier) { m -> Rich(varLast(text), m, style = style, color = color) }
}

/**
 * A text the step's scrolling never cuts through: it reports its place and its line edges to the step frame, which
 * stops a scroll of its own on a line edge. [content] draws the text with the modifier it is given.
 */
@Composable
fun KeepWhole(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    val keep = LocalKeepWhole.current
    if (keep == null) { content(modifier); return }
    val edges = remember { floatArrayOf(0f).toMutableList() }
    var top by remember { mutableFloatStateOf(0f) }
    CompositionLocalProvider(dev.hamper.tabula.math.LocalLineEdges provides { e -> edges.clear(); edges += e; keep(top, edges.toList()) }) {
        content(modifier.onGloballyPositioned { top = it.positionInRoot().y; keep(top, edges.toList()) })
    }
}

/**
 * A name that ends in a variable ("Kongruencia modulo m") sets it as in the text, in math italic, and never leaves it
 * alone on a line. Slovak never ends a title with a one-letter word, so a lone Latin letter there is a variable.
 */
fun varLast(text: String): String =
    if ('$' in text || '§' in text) text else text.replace(Regex("""(?<=\p{L}) ([a-zA-Z])$"""), "\u00A0§$1§")

@Composable
fun Sub(text: String, modifier: Modifier = Modifier, color: Color = T.c.ink2) {
    Rich(text, modifier, style = MaterialTheme.typography.bodySmall, color = color)
}

/**
 * Short facts on one line, "krok 6 z 25 · asi 14 min": each part whole while it fits a line, a dot only between two
 * parts on the same line, so no line ends with a hanging "·" or starts with one (inspector, 18. 11.). A part longer
 * than the whole line wraps inside, by [nb].
 */
@Composable
fun Parts(parts: List<String>, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.LocalTextStyle.current, color: Color = LocalContentColor.current) {
    val ps = parts.filter { it.isNotBlank() }
    androidx.compose.ui.layout.Layout(
        content = {
            ps.forEach { Text(it.nb(), style = style, color = color, onTextLayout = { r -> LabelAudit.split(r) }) }
            repeat((ps.size - 1).coerceAtLeast(0)) { Text("\u00A0·\u00A0", style = style, color = color) }
        },
        // read as one text, the parts as written (the dots between them, no hidden breaks)
        modifier = modifier.clearAndSetSemantics { text = androidx.compose.ui.text.AnnotatedString(ps.joinToString(" · ")) },
    ) { ms, cons ->
        val n = ps.size
        val max = if (cons.hasBoundedWidth) cons.maxWidth else Int.MAX_VALUE
        val dots = ms.drop(n).map { it.measure(androidx.compose.ui.unit.Constraints()) }
        val dw = dots.firstOrNull()?.width ?: 0
        // which row each part goes on: the next part joins the row if it fits whole after a dot
        val placed = ArrayList<Triple<androidx.compose.ui.layout.Placeable, Int, Int>>() // part, x, row
        val rows = ArrayList<Int>() // row heights
        var x = 0; var row = -1
        ms.take(n).forEachIndexed { i, m ->
            val whole = m.maxIntrinsicWidth(androidx.compose.ui.unit.Constraints.Infinity)
            val p = m.measure(androidx.compose.ui.unit.Constraints(maxWidth = max))
            if (row >= 0 && x + dw + whole <= max) { x += dw } else { row++; rows += 0; x = 0 }
            placed += Triple(p, x, row)
            rows[row] = maxOf(rows[row], p.height)
            x += p.width
        }
        val tops = rows.runningFold(0) { a, h -> a + h }
        val width = if (cons.hasBoundedWidth) cons.maxWidth.coerceAtLeast(cons.minWidth) else placed.maxOfOrNull { it.second + it.first.width } ?: 0
        layout(width.coerceIn(cons.minWidth, cons.maxWidth), tops.last().coerceIn(cons.minHeight, cons.maxHeight)) {
            placed.forEachIndexed { i, (p, px, r) ->
                if (i > 0 && placed[i - 1].third == r) dots[i - 1].place(px - dw, tops[r])
                p.place(px, tops[r])
            }
        }
    }
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier, color: Color = T.c.ink2) {
    // through Rich when it has math or may hold a reference ("Skriptá, Definice 2.7"), so the reference is tappable
    val text = varLast(text)
    if (text.contains('$') || text.contains('§') || LocalRefs.current != null) Rich(text, modifier, style = MaterialTheme.typography.labelMedium, color = color)
    else Text(text.nb(), modifier, style = MaterialTheme.typography.labelMedium, color = color, onTextLayout = { LabelAudit.split(it) })
}

/** Czech source text: quiet card, the same voice as everything else, nothing pretends to be a textbook. */
@Composable
fun SourceCard(label: String?, modifier: Modifier = Modifier, fill: Color = T.c.card, plain: Boolean = false, statement: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    // [statement]: a lesson's statement with its source as the label (the new look puts the source under it); other
    // cards keep their label as a heading on top (the Test tab, the tutor, exercises)
    if (statement && lekcia()) { StatementBlock(label, modifier, fill, plain, content); return }
    Column(modifier.fillMaxWidth().panel(fill = fill).padding(horizontal = 16.dp, vertical = 14.dp)) {
        // [plain]: a label already in its final words (a card's own number), not a reference to expand or link
        if (label != null) {
            if (plain) CompositionLocalProvider(LocalRefs provides null) { Label(label) }
            else CompositionLocalProvider(dev.hamper.tabula.math.LocalRefLabel provides true) { Label(label) }
            Spacer(Modifier.height(6.dp))
        }
        content()
    }
}

/** Thumb of [CoefSlider]: as big as the finger that moves it. scripts/walk.py taps by this size. */
val SliderThumb = 48.dp

/**
 * Slider with a zero mark and a tick at every integer: the bar grows from zero, so sign and size read at a
 * glance, and the ticks are the scale, numbered at both ends and at zero. [textColor] is for the value in front
 * (a text-safe shade of [color]); a [locked] slider keeps showing its value but no longer moves.
 */
@Composable
fun CoefSlider(
    label: String, value: Float, range: ClosedFloatingPointRange<Float>, color: Color, edge: Color, say: String, modifier: Modifier = Modifier, step: Float = .1f,
    valueText: String, tag: String? = null, textColor: Color = color, locked: Boolean = false, onChange: (Float) -> Unit,
) {
    val c = T.c
    val hx = rememberHaptics()
    val span = range.endInclusive - range.start
    fun snap(v: Float) = ((v / step).roundToInt() * step).coerceIn(range.start, range.endInclusive)
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val tickStyle = MaterialTheme.typography.labelSmall.copy(color = c.ink2, fontFeatureSettings = "tnum")
    fun num(x: Int) = if (x < 0) "−${-x}" else "$x"
    val marks = remember(range, tickStyle, measurer) {
        listOfNotNull(kotlin.math.ceil(range.start).toInt(), 0.takeIf { range.start < 0f && range.endInclusive > 0f }, kotlin.math.floor(range.endInclusive).toInt())
            .distinct().map { it to measurer.measure(num(it), tickStyle) }
    }
    val labelH = with(androidx.compose.ui.platform.LocalDensity.current) { (marks.maxOfOrNull { it.second.size.height } ?: 0).toDp() }
    val boxH = maxOf(56.dp, 44.dp + labelH)
    Row(modifier.fillMaxWidth().heightIn(min = boxH), verticalAlignment = Alignment.Top) {
        // the value sits level with the bar, not with the numbers under it
        Box(Modifier.widthIn(min = 76.dp).heightIn(min = 53.dp), contentAlignment = Alignment.CenterStart) {
            Rich(valueText, style = MaterialTheme.typography.titleMedium, color = textColor)
        }
        Spacer(Modifier.width(4.dp))
        var w by remember { mutableIntStateOf(1) }
        val latest by rememberUpdatedState(value)
        val onCh by rememberUpdatedState(onChange)
        val still by rememberUpdatedState(locked)
        fun drag(x: Float, thumb: Float) {
            if (still) return
            val v = snap(range.start + ((x - thumb / 2) / (w - thumb)).coerceIn(0f, 1f) * span)
            if (v != latest) { hx.tick(); onCh(v) }
        }
        Box(
            Modifier.weight(1f).height(boxH)
                .onSizeChanged { w = it.width }
                .semantics {
                    contentDescription = say + (dbg(tag)?.let { " $it;min=${range.start};max=${range.endInclusive}" } ?: ""); stateDescription = valueText.replace("$", "")
                    progressBarRangeInfo = ProgressBarRangeInfo(value, range)
                    if (locked) disabled() else setProgress { v -> onCh(snap(v)); true }
                }
                .pointerInput(Unit) {
                    val t = SliderThumb.toPx()
                    detectTapGestures { drag(it.x, t) }
                }
                .pointerInput(Unit) {
                    val t = SliderThumb.toPx()
                    detectDragGestures(onDragStart = { drag(it.x, t) }) { ch, _ -> ch.consume(); drag(ch.position.x, t) }
                }
                .drawBehind {
                    val t = SliderThumb.toPx(); val h = 8.dp.toPx(); val cy = 28.dp.toPx() - 1.5.dp.toPx()
                    val usable = size.width - t
                    fun xOf(v: Float) = t / 2 + (v - range.start) / span * usable
                    drawRoundRect(c.line, Offset(t / 2 - h / 2, cy - h / 2), Size(usable + h, h), CornerRadius(h / 2))
                    // the scale: a tick at every integer, zero taller
                    val tw = 2.dp.toPx()
                    var i = kotlin.math.ceil(range.start).toInt()
                    while (i <= range.endInclusive) {
                        val x = xOf(i.toFloat())
                        if (i != 0) drawRoundRect(c.axis, Offset(x - tw / 2, cy + h / 2 + 4.dp.toPx()), Size(tw, 6.dp.toPx()), CornerRadius(tw / 2))
                        i++
                    }
                    val x0 = xOf(0f.coerceIn(range.start, range.endInclusive)); val xv = xOf(value)
                    // and its numbers, under the ends and under zero; the one the thumb stands on is said by the value in front
                    for ((m, txt) in marks) {
                        val xm = xOf(m.toFloat())
                        if (abs(xm - xv) < t / 2 + txt.size.width / 2f) continue
                        drawText(txt, topLeft = Offset((xm - txt.size.width / 2f).coerceIn(0f, size.width - txt.size.width), cy + h / 2 + 12.dp.toPx()))
                    }
                    drawRoundRect(color, Offset(minOf(x0, xv), cy - h / 2), Size(abs(xv - x0), h), CornerRadius(h / 2))
                    drawRoundRect(c.ink2, Offset(x0 - tw / 2, cy - 10.dp.toPx()), Size(tw, 20.dp.toPx()), CornerRadius(tw / 2))
                    // the thumb is the full 48 dp; locked, it sits flat on the bar (no raised edge) and the dot fills it
                    val r = t / 2
                    if (!locked) drawCircle(edge, r, Offset(xv, cy + 3.dp.toPx()))
                    drawCircle(c.card, r, Offset(xv, cy))
                    drawCircle(color, r - 1.5.dp.toPx(), Offset(xv, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
                    drawCircle(color, if (locked) 9.dp.toPx() else 5.dp.toPx(), Offset(xv, cy))
                },
        )
    }
}


/** Tappable row used for self-checks. */
@Composable
fun CheckRow(text: String, on: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier, trailing: String? = null, tag: String? = null) {
    val c = T.c
    val hx = rememberHaptics()
    val hue = T.hue
    val fill by animateColorAsState(if (on) hue.tint2 else c.card, tween(160), label = "cf")
    val line by animateColorAsState(if (on) hue.main else c.line, tween(160), label = "cl")
    Row(
        modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(16.dp)).background(fill).border(1.5.dp, line, RoundedCornerShape(16.dp))
            .toggleable(on, role = Role.Checkbox) { hx.play(Buzz.Select); onToggle(it) }.then(dbg(tag)?.let { d -> Modifier.semantics { contentDescription = d } } ?: Modifier).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(if (on) (if (c.dark) hue.main else hue.main) else Color.Transparent).border(2.dp, if (on) hue.main else c.axis, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) { if (on) Icon24(Res.drawable.ic_check, tint = c.card, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        CompositionLocalProvider(dev.hamper.tabula.math.LocalGloss provides null) { Rich(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = c.ink) }
        if (trailing != null) { Spacer(Modifier.width(8.dp)); Text(trailing, style = MaterialTheme.typography.labelMedium, color = c.ink2) }
    }
}

/** Number pad for the fields we actually need. */
@Composable
fun NumberPad(keys: List<String>, onKey: (String) -> Unit, modifier: Modifier = Modifier, columns: Int = keys.size, enabled: Boolean = true) {
    val c = T.c
    val hx = rememberHaptics()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { k ->
                    PressBox(
                        { hx.key(); onKey(k) }, Modifier.weight(1f).height(if (columns <= 5 && keys.size <= 5) 68.dp else 60.dp), c.card, c.edge, c.line, radius = 17.dp, depth = 4.dp, enabled = enabled, say = if (k == "⌫") "Zmazať" else k,
                    ) {
                        if (k == "⌫") Icon24(Res.drawable.ic_backspace, tint = c.ink)
                        else Text(k, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium), color = c.ink)
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * The number pad of an answer: [rows] as laid out (screens/Exercise.kt, Pad), "" a gap. Every key answers the
 * thumb at once: a light tick, and the key gives a little under it and springs back. A key in [off] keeps its place,
 * dimmed and silent, so the layout never changes between fields (ℤ₇ greys 7, 8, 9).
 */
@Composable
fun KeyPad(rows: List<List<String>>, onKey: (String) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, off: Set<String> = emptySet()) {
    val hx = rememberHaptics()
    // a short window (a phone in landscape, an iPhone in Safari with its bars: 550 to 670 dp) gets lower keys, still a
    // full finger high, so the question and its fields keep room above them; a phone held upright never is that short
    val height = LocalWindowInfo.current.containerSize.height
    val short = height > 0 && with(LocalDensity.current) { height.toDp() } < 700.dp
    val gap = if (short) 6.dp else 8.dp
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { k ->
                    if (k.isEmpty()) Spacer(Modifier.weight(1f))
                    else PadKey(k, Modifier.weight(1f).height(if (short) 44.dp else 54.dp).testTag("key:$k"), enabled && k !in off, dim = k in off) { hx.key(); onKey(k) }
                }
            }
        }
    }
}

@Composable
private fun PadKey(k: String, modifier: Modifier, enabled: Boolean, dim: Boolean = false, onClick: () -> Unit) {
    val c = T.c
    val still = calm()
    val press = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    PressBox(
        { onClick(); if (!still) scope.launch { press.snapTo(.94f); press.animateTo(1f, Motion.pop()) } },
        modifier.graphicsLayer { scaleX = press.value; scaleY = press.value; alpha = if (dim) .35f else 1f }, if (k == "⌫" || k == "−" || k == "/") c.bg else c.card, c.edge, c.line,
        radius = 16.dp, depth = 4.dp, enabled = enabled, say = when (k) { "⌫" -> "Zmazať"; "−" -> "Mínus"; "/" -> "Zlomok"; else -> k },
    ) {
        if (k == "⌫") Icon24(Res.drawable.ic_backspace, tint = c.ink)
        else Text(k, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium), color = c.ink)
    }
}

/** A field that just changed (a digit typed) swells a touch and settles: the number lands where the eye is. */
@Composable
fun Modifier.bump(value: Any?): Modifier {
    val still = calm()
    val a = remember { Animatable(1f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(value) { if (first) { first = false; return@LaunchedEffect }; if (!still) { a.snapTo(1.04f); a.animateTo(1f, Motion.pop()) } }
    return graphicsLayer { scaleX = a.value; scaleY = a.value }
}

/** "Nahlásiť chybu": in the top bar of every step, the report is stored on the phone with the item's id. */
@Composable
fun ReportButton(onClick: () -> Unit) {
    // the web sends no reports (Telemetry is off there, Platform.isWeb): a flag would only keep a local copy
    if (dev.hamper.tabula.platform.LocalPlatform.current.isWeb) return
    val c = T.c
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = "Nahlásiť chybu" },
        contentAlignment = Alignment.Center,
    ) { Icon24(Res.drawable.ic_report, tint = c.ink2) }
}

/**
 * The first answer option of a question step (docs/CONTENT.md, "The answers are on the first screen"): tagged for
 * FoldTest, and [onSeen] hears once it is laid out whether it is whole on screen, not cut by the bottom of the body.
 */
const val AnswerTag = "answer"
/** A question whose options sit in the footer: the question itself must be whole on screen above them. */
const val QuestionTag = "question"
fun Modifier.firstAnswer(onSeen: ((Boolean) -> Unit)? = null): Modifier =
    testTag(AnswerTag).then(if (onSeen == null) Modifier else Modifier.onGloballyPositioned { onSeen(it.wholeOnScreen()) })

/** Not cut by any parent: the visible part (the body scrolls, the footer covers the rest) is the whole height. */
fun LayoutCoordinates.wholeOnScreen(): Boolean = size.height > 0 && boundsInWindow().height >= size.height - 1f

/**
 * Two answers side by side only when each fits half a phone (about 16 letters, a formula counted by a third of its
 * source); longer ones go under each other, so a word never breaks inside a half-width button (the audit, 17. 11.).
 */
fun sideBySide(options: List<String>): Boolean = options.all { o ->
    o.replace('§', '$').replace(Regex("""\$[^$]*\$""")) { "x".repeat((it.value.length / 3).coerceAtLeast(1)) }.length <= 16
}

/**
 * Options side by side as tiles only when they are very short and each fits its share of the width at the font size
 * on the phone (measured, with room to spare); otherwise full-width options under each other, big for the thumb.
 * [texts] in the order shown; [item] draws option i with its modifier and whether it sits in a row.
 */
@Composable
fun ChoiceGroup(texts: List<String>, rowWanted: Boolean, style: androidx.compose.ui.text.TextStyle, item: @Composable (Int, Modifier, Boolean) -> Unit) {
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val d = LocalDensity.current
        val gap = 8.dp
        val slot = (maxWidth - gap * (texts.size - 1)) / texts.size.coerceAtLeast(1)
        // a formula is set wider than its letters (spaces around =): its plain letters are measured with a margin; a bare
        // number ("659", "−7") is set as wide as its digits, so it needs only the tile's padding (inspector, 18. 11.:
        // three short numbers stood under each other at 360 dp and 200 % and pushed the question off the screen)
        val fits = rowWanted && texts.size in 2..4 && texts.all { t ->
            val plain = t.replace('§', '$').replace("$", "").replace(Regex("""\\[a-zA-Z]+"""), "x").replace(Regex("[{}]"), "")
            val w = with(d) { measurer.measure(plain, style).size.width.toDp() }
            if (plain.matches(bareNumber)) w * 1.05f + 24.dp <= slot else w * 1.35f + 28.dp <= slot
        }
        if (fits) Row(horizontalArrangement = Arrangement.spacedBy(gap)) { texts.indices.forEach { item(it, Modifier.weight(1f), true) } }
        else Column(verticalArrangement = Arrangement.spacedBy(gap)) { texts.indices.forEach { item(it, Modifier.fillMaxWidth(), false) } }
    }
}

private val bareNumber = Regex("""[−-]?\d{1,6}""")

/** Answers always sit in the same place, at the thumb. Once answered, only your pick stays, so nothing jumps. */
@Composable
fun AnswerBar(options: List<String>, correct: Int, pick: Int?, onPick: (Int) -> Unit) {
    val st = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
    fun state(k: Int) = if (pick == k) (if (k == correct) Pick.Right else Pick.Wrong) else Pick.Idle
    if (pick != null) {
        Choice(options[pick], {}, Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp), state = state(pick), enabled = false, center = options[pick].length < 12, minHeight = 56.dp, style = st)
    } else {
        val long = options.any { it.length > 40 }
        ChoiceGroup(options, (options.size == 2 && sideBySide(options)) || (options.size <= 4 && options.all { it.length <= 6 }), st) { k, m, row ->
            Choice(options[k], { onPick(k) }, m.then(if (k == 0) Modifier.firstAnswer() else Modifier), center = row, minHeight = if (row) 64.dp else 60.dp,
                style = if (long) MaterialTheme.typography.bodyLarge else st, tag = if (k == correct) "correct" else null)
        }
    }
}

/** Czech source text, line by line: paragraphs, items and display formulas. */
@Composable
fun SourceLines(lines: List<dev.hamper.tabula.data.Line>) {
    val c = T.c
    val st = MaterialTheme.typography.bodyLarge
    lines.forEachIndexed { i, l ->
        // display formulas get air: a sum's limits must not touch the line above
        if (i > 0) Spacer(Modifier.height(if (l is dev.hamper.tabula.data.Block || lines[i - 1] is dev.hamper.tabula.data.Block) (if (lekcia()) 16.dp else 14.dp) else (if (lekcia()) 8.dp else 6.dp)))
        when (l) {
            is dev.hamper.tabula.data.Para -> Rich(l.tex, style = st, color = c.ink)
            is dev.hamper.tabula.data.Item -> Row { Text("•  ", style = st, color = c.ink2); Rich(l.tex, Modifier.weight(1f), style = st, color = c.ink) }
            // its symbols answer a tap like the ones in the lines around it (math/Compose.kt, StatementTex)
            is dev.hamper.tabula.data.Block -> if (lekcia()) dev.hamper.tabula.math.StatementTex(l.tex, Modifier, size = 19.sp, color = c.ink, center = true) else dev.hamper.tabula.math.StatementTex(l.tex, Modifier, size = 18.sp, color = c.ink)
        }
    }
}


/** Debug builds only: marks for the adb walker (scripts/walk.py) in the accessibility tree. Release builds get null. */
fun dbg(tag: String?): String? = if (dev.hamper.tabula.Env.DEBUG && tag != null) "dbg:$tag" else null

/**
 * A panel anchored at the bottom changes height calmly, its content kept at the bottom edge while it grows or shrinks:
 * the buttons are in place at once and the panel opens upward over them (Teo, 17. 11.: animateContentSize clipped from
 * the top, so after "Správne" the panel showed an empty strip for a few frames and the button cut in half).
 */
@Composable
fun Modifier.growUpward(): Modifier {
    val scope = rememberCoroutineScope()
    val h = remember { Animatable(-1f) }
    return clipToBounds().layout { m, c ->
        val p = m.measure(c)
        val target = p.height.toFloat()
        if (h.value < 0f) scope.launch { h.snapTo(target) }
        else if (h.targetValue != target) scope.launch { h.animateTo(target, androidx.compose.animation.core.spring(1f, 500f)) }
        val now = (if (h.value < 0f) target else h.value).roundToInt().coerceIn(c.minHeight, c.maxHeight)
        layout(p.width, now) { p.place(0, now - p.height) }
    }
}

package dev.hamper.tabula.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import dev.hamper.tabula.ui.calm
import dev.hamper.tabula.ui.Icon24
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import dev.hamper.tabula.compat.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.MBox
import dev.hamper.tabula.math.MathEngine
import dev.hamper.tabula.math.speak
import dev.hamper.tabula.ui.T
import kotlin.math.max
import dev.hamper.tabula.compat.graphics.*
import dev.hamper.tabula.res.*
import androidx.compose.ui.graphics.graphicsLayer

/*
 * The one GEM component (docs/CONTENT.md, "GEM krok po kroku"). Its layout is a property of the walk, not of the step
 * shown: every cell is as wide as the widest value it ever takes, every row as tall as its tallest, the place for the
 * kind (G3), the ∼ and the operations is reserved from the first step, and the matrix is anchored at the top. Between
 * steps nothing moves: a changed entry turns like a counter inside its cell (GemFade), the changed rows are tinted, the
 * first nonzero element a (G3) step subtracts is framed. Swipe moves a step, press and hold shows the matrix before.
 */

/** Sizes of a walk's stage: the same for every step (GemWalkTest checks it on the real layout). */
private class StageGeometry(
    val cells: List<List<List<MBox>>>, // frame, row, column
    val colW: FloatArray, val rowAsc: FloatArray, val rowDesc: FloatArray,
    val padX: Float, val padY: Float, val paren: Float, val barGap: Float, val bar: Int?, val hv: Float,
    val labels: List<OpLabel>, val slotW: Float, val slotAbove: Float, val slotBelow: Float, val tilde: MBox,
    val hvBoxes: Pair<MBox, MBox>, val gap: Float,
) {
    val rowH get() = FloatArray(rowAsc.size) { rowAsc[it] + rowDesc[it] + 2 * padY }
    val rowsH get() = rowH.sum()
    fun colX(j: Int): Float = paren + (0 until j).sumOf { colW[it].toDouble() }.toFloat() + (if (bar != null && j >= bar) barGap else 0f)
    val matrixW get() = paren * 2 + colW.sum() + (if (bar != null) barGap else 0f)
    val matrixH get() = rowsH + hv
    fun rowY(i: Int): Float { var y = 0f; for (r in 0 until i) y += rowH[r]; return y }
    val tildeH get() = tilde.asc + tilde.desc
    /** Room above the matrix for the kind when the matrix is low (one or two rows): the same on every step. */
    val lift get() = max(0f, slotAbove + tildeH / 2f - rowsH / 2f)
    val slotH get() = lift + rowsH / 2f + max(rowsH / 2f, tildeH / 2f + slotBelow)
}

/** What goes with one ∼: the kind above it, the operations under it. */
private class OpLabel(val kind: MBox, val ops: List<MBox>)

@Composable
private fun rememberGeometry(walk: GemWalk, big: Boolean, scale: Float = 1f): StageGeometry {
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    return remember(walk, big, scale, d, face, inks) { geometry(walk, big, scale, d, face, inks) }
}

private fun geometry(walk: GemWalk, big: Boolean, scale: Float, d: androidx.compose.ui.unit.Density, face: dev.hamper.tabula.math.MathFace, inks: dev.hamper.tabula.math.MathInks): StageGeometry {
    val px = with(d) { (if (big) 26.sp else 19.sp).toPx() } * scale
    val small = with(d) { (if (big) 16.sp else 14.sp).toPx() } * maxOf(scale, .85f)
    return run {
        val cache = HashMap<String, MBox>()
        fun box(t: String) = cache.getOrPut(t) { MathEngine.layout(t, px, face, inks) }
        val cells = walk.frames.map { f -> f.cells.map { r -> r.map { box(it) } } }
        val m = walk.start.m; val n = walk.start.n
        val padX = px * .42f; val padY = px * .2f
        val colW = FloatArray(n) { j -> cells.maxOf { f -> f.maxOf { r -> r[j].w } } + 2 * padX }
        val rowAsc = FloatArray(m) { i -> cells.maxOf { f -> f[i].maxOf { it.asc } } }
        val rowDesc = FloatArray(m) { i -> cells.maxOf { f -> f[i].maxOf { it.desc } } }
        val labels = walk.moves.map { mv ->
            OpLabel(MathEngine.layout(mv.kindTex, small * .92f, face, inks, weight = 600, text = true), mv.ops.map { MathEngine.layout(it.tex(), small, face, inks) })
        }
        val tilde = MathEngine.layout("\\sim", px * 1.05f, face, inks)
        val lineGap = small * .45f
        val slotW = (labels.flatMap { l -> listOf(l.kind.w) + l.ops.map { it.w } } + tilde.w).max() + small * .8f
        val slotAbove = labels.maxOfOrNull { it.kind.asc + it.kind.desc + lineGap } ?: 0f
        val slotBelow = labels.maxOfOrNull { l -> l.ops.sumOf { (it.asc + it.desc + lineGap / 2f).toDouble() }.toFloat() + lineGap / 2f } ?: 0f
        StageGeometry(cells, colW, rowAsc, rowDesc, padX, padY, px * .42f, px * .5f, walk.start.bar,
            if (walk.endsInHst && walk.size > 0) small * 1.5f else 0f, labels, slotW, slotAbove, slotBelow, tilde,
            MathEngine.layout("H", small, face, inks, weight = 700, text = true) to MathEngine.layout("V", small, face, inks, weight = 700, text = true), lineGap)
    }
}

/** The stage's width: callers let a swipe step only when the stage fits, otherwise the drag scrolls it sideways. */
@Composable
fun gemStageWidth(walk: GemWalk, big: Boolean, scale: Float = 1f): androidx.compose.ui.unit.Dp {
    val g = rememberGeometry(walk, big, scale)
    return with(LocalDensity.current) { (g.slotW + g.matrixW).toDp() }
}

/**
 * Optical centring (Teo, 17. 11.): the light part on the left of a stage (GEM's operation slot "(G1) ∼ ř3↔ř4", a table's
 * row numbers) counts only by [weight] against the heavy matrix or table, so a stage that fits its [room] moves left by
 * that much of the light part, never past the left edge. Fixed for the whole walk: the matrix never moves when stepping.
 */
fun opticalShift(light: androidx.compose.ui.unit.Dp, weight: Float, stage: androidx.compose.ui.unit.Dp, room: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
    minOf(light * weight, maxOf(0.dp, (room - stage) / 2))

/** The width of a walk's matrix alone (no operation slot) at [scale]. */
@Composable
fun gemMatrixWidth(walk: GemWalk, big: Boolean, scale: Float = 1f): androidx.compose.ui.unit.Dp {
    val g = rememberGeometry(walk, big, scale)
    return with(LocalDensity.current) { g.matrixW.toDp() }
}

/** The width of a walk's operation slot (left of the matrix) at [scale]. */
@Composable
fun gemSlotWidth(walk: GemWalk, big: Boolean, scale: Float = 1f): androidx.compose.ui.unit.Dp {
    val g = rememberGeometry(walk, big, scale)
    return with(LocalDensity.current) { g.slotW.toDp() }
}

/** How far a GEM stage moves left of the geometric centre: the slot counts at 40 % against the matrix (moves left by 60 % of it). */
const val GemSlotWeight = .6f

/**
 * The stage of a walk at step [k] (0 = the given matrix): the operation slot left of the matrix, the matrix, and H and
 * V under its columns once the last matrix (in HST) is reached. Fixed size for the whole walk.
 */
@Composable
fun GemStage(walk: GemWalk, k: Int, modifier: Modifier = Modifier, big: Boolean = true, scale: Float = 1f, onStep: ((Int) -> Unit)? = null, stacked: Boolean = false, znacky: GemZnacky? = null,
    onCell: ((matica: Int, riadok: Int, stlpec: Int) -> Unit)? = null) {
    val step by rememberUpdatedState(k)
    val c = T.c
    val g = rememberGeometry(walk, big, scale)
    val d = LocalDensity.current
    var peek by remember(walk) { mutableStateOf(false) }
    val shown = if (peek && k > 0) k - 1 else k
    val fade = rememberStepFade(walk, shown)
    val prev = fade.from
    val forward = fade.to >= fade.from
    val slide = if (calm()) 0f else g.rowAsc.maxOrNull()?.let { it * GemFade.SLIDE } ?: 0f
    val ink = c.ink; val ink2 = c.ink2; val hue = c.gem
    // press and hold: the matrix before the last operation; a swipe steps (only when the caller lets it: a stage
    // wider than the screen scrolls sideways with the same drag instead)
    // only a real hold peeks: a tap, the start of a swipe or of a scroll must not flash the old matrix
    val peekHold = Modifier.pointerInput(walk) { detectTapGestures(onPress = {
        if (step > 0 && kotlinx.coroutines.withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { tryAwaitRelease() } == null) {
            peek = true; tryAwaitRelease(); peek = false
        }
    }) }
    val gestures = if (onStep == null) peekHold else peekHold.pointerInput(walk, k) {
        var dx = 0f
        detectHorizontalDragGestures(onDragStart = { dx = 0f }, onDragEnd = {
            val limit = 48.dp.toPx()
            if (dx < -limit && k < walk.size) onStep(k + 1) else if (dx > limit && k > 0) onStep(k - 1)
        }) { ch, a -> ch.consume(); dx += a }
    }
    // [stacked]: a stage too wide for the screen even at its smallest puts the operation slot under the matrix, so
    // the whole matrix stays readable (an augmented matrix lost its right side); the same for every step
    val tildeH = g.tilde.asc + g.tilde.desc
    val slotHeight = if (stacked) g.slotAbove + tildeH + g.slotBelow else g.slotH
    val stage: @Composable (@Composable () -> Unit) -> Unit = { content ->
        if (stacked) Column(modifier.then(gestures), horizontalAlignment = Alignment.CenterHorizontally) { content() }
        else Row(modifier.then(gestures), verticalAlignment = Alignment.Top) { content() }
    }
    // stacked: the matrix stays at the top and its operations go under it (at a large font the room reserved for
    // them above pushed the matrix out of the card, 360 dp at 200 %); beside: the operations on its left
    val slot: @Composable () -> Unit = {
            // the operation slot: reserved on step 0 too, so the matrix never shifts right when the first ∼ appears
            Canvas(Modifier.size(with(d) { (if (stacked) max(g.slotW, g.matrixW) else g.slotW).toDp() }, with(d) { slotHeight.toDp() }).testTag("gem-ops").clearAndSetSemantics { }) {
                val t = fade.p.value // read while drawing: a step change redraws, it does not recompose
                val mid = if (stacked) g.slotAbove + tildeH / 2f else g.lift + g.rowsH / 2f // the ∼ on the matrix's midline, or under the kind
                val tTop = mid - (g.tilde.asc + g.tilde.desc) / 2f
                // the ∼ itself: steady between two operations, fades only when the given matrix comes or goes
                val tildeA = GemFade.presence(prev > 0, shown > 0, t)
                if (tildeA > 0f) drawMath(g.tilde, (size.width - g.tilde.w) / 2f, tTop + g.tilde.asc, ink2.copy(alpha = tildeA))
                fun label(at: Int, a: Float) {
                    if (at == 0 || a <= 0f) return
                    val l = g.labels[at - 1]
                    drawMath(l.kind, (size.width - l.kind.w) / 2f, tTop - g.slotAbove + l.kind.asc, ink2.copy(alpha = a))
                    var y = tTop + g.tilde.asc + g.tilde.desc + g.gap
                    for (o in l.ops) { drawMath(o, (size.width - o.w) / 2f, y + o.asc, hue.text.copy(alpha = a)); y += o.asc + o.desc + g.gap / 2f }
                }
                if (prev != shown) label(prev, GemFade.old(t))
                label(shown, if (prev != shown) GemFade.new(t) else 1f)
            }
    }
    val matrix: @Composable () -> Unit = {
            // [onCell]: a tap on a cell of the matrix shown (the tutor's "toto som nenapísal")
            val cellTap = if (onCell == null) Modifier else Modifier.pointerInput(walk, step, g) {
                detectTapGestures(onTap = { p ->
                    val j = (0 until walk.start.n).firstOrNull { p.x >= g.colX(it) && p.x < g.colX(it) + g.colW[it] }
                    val i = (0 until walk.start.m).firstOrNull { p.y >= g.rowY(it) && p.y < g.rowY(it) + g.rowH[it] }
                    if (i != null && j != null) onCell(step, i, j)
                })
            }
            Canvas(Modifier.padding(top = with(d) { (if (stacked) 0f else g.lift).toDp() }).size(with(d) { g.matrixW.toDp() }, with(d) { g.matrixH.toDp() }).then(cellTap).testTag("gem-matrix")
                .semantics {
                    // the operation and the matrix after it, read as one: the slot beside it is silent
                    val op = if (shown == 0) "" else walk.moves[shown - 1].let { mv -> mv.kindTex + " " + mv.ops.joinToString(", ") { speak(it.tex()) } + ": " }
                    contentDescription = op + speak(walk.frames[shown].tex()); liveRegion = LiveRegionMode.Polite
                }) {
                val t = fade.p.value
                val stroke = 1.4.dp.toPx()
                val frame = walk.frames[shown]
                val changed = walk.changed(shown)
                val changedBefore = walk.changed(prev)
                // tinted rows: what this step changed, fading in with its numbers (and out with the step before)
                for (i in 0 until frame.m) {
                    val a = if (prev == shown) (if (i in changed) 1f else 0f) else GemFade.presence(i in changedBefore, i in changed, t)
                    if (a > 0f) drawRoundRect(hue.tint.copy(alpha = a), Offset(g.paren * .6f, g.rowY(i) + 1f), Size(g.matrixW - g.paren * 1.2f, g.rowH[i] - 2f), CornerRadius(6.dp.toPx()))
                }
                // the tutor's mark on a student's chain: the wrong rows in red over the step's tint, the wrong cells framed
                if (znacky != null && znacky.matica == shown) {
                    val bad = if (znacky.neiste) c.ink3 else c.bad
                    for (i in znacky.riadky) if (i < frame.m) drawRoundRect((if (znacky.neiste) c.sand else c.badTint), Offset(g.paren * .6f, g.rowY(i) + 1f), Size(g.matrixW - g.paren * 1.2f, g.rowH[i] - 2f), CornerRadius(6.dp.toPx()))
                    for ((i, j) in znacky.bunky) if (i < frame.m && j < frame.n) drawRoundRect(bad, Offset(g.colX(j) + g.padX * .2f, g.rowY(i) + g.padY * .2f),
                        Size(g.colW[j] - g.padX * .4f, g.rowH[i] - g.padY * .4f), CornerRadius(5.dp.toPx()), style = Stroke(stroke * 1.6f))
                }
                // the first nonzero element of every row, framed (skriptá: "první nenulový prvek")
                val framedNow = walk.framed(shown); val framedBefore = if (prev == shown) framedNow else walk.framed(prev)
                for ((i, j) in framedNow + framedBefore) {
                    val a = GemFade.presence((i to j) in framedBefore, (i to j) in framedNow, t)
                    if (a > 0f) drawRoundRect(hue.main.copy(alpha = a), Offset(g.colX(j) + g.padX * .35f, g.rowY(i) + g.padY * .35f),
                            Size(g.colW[j] - g.padX * .7f, g.rowH[i] - g.padY * .7f), CornerRadius(5.dp.toPx()), style = Stroke(stroke))
                }
                // entries: unchanged ones stay put; a changed one rolls: the old value slides a little and fades out while
                // the new one comes from the other side, their opacities always adding up to one (never an empty cell)
                for (i in 0 until frame.m) {
                    val base = g.rowY(i) + g.padY + g.rowAsc[i]
                    for (j in 0 until frame.n) {
                        val now = g.cells[shown][i][j]; val was = g.cells[prev][i][j]
                        fun at(b: MBox, a: Float, dy: Float) = drawMath(b, g.colX(j) + (g.colW[j] - b.w) / 2f, base + dy, ink.copy(alpha = a))
                        if (prev == shown || now === was) at(now, 1f, 0f) else clipRect(g.colX(j), g.rowY(i), g.colX(j) + g.colW[j], g.rowY(i) + g.rowH[i]) {
                            // like a counter turning, inside its own cell: nothing crosses into the next row
                            val dir = if (forward) 1f else -1f
                            at(was, GemFade.old(t), -dir * slide * t)
                            at(now, GemFade.new(t), dir * slide * (1f - t))
                        }
                    }
                }
                // parentheses and the bar of an augmented matrix
                parens(g.paren, g.rowsH, size.width, ink, stroke)
                g.bar?.let { b -> val x = g.colX(b) - g.barGap / 2f; drawLine(ink, Offset(x, g.padY), Offset(x, g.rowsH - g.padY), stroke) }
                // H and V under the columns of the last matrix (hlavní a vedlejší sloupce)
                if (g.hv > 0f) {
                    val a = if (prev == shown) (if (shown == walk.size) 1f else 0f) else GemFade.presence(prev == walk.size, shown == walk.size, t)
                    if (a > 0f) walk.mainColumns.forEachIndexed { j, main ->
                        val b = if (main) g.hvBoxes.first else g.hvBoxes.second
                        drawMath(b, g.colX(j) + (g.colW[j] - b.w) / 2f, g.rowsH + g.hv * .75f, (if (main) hue.text else ink2).copy(alpha = a))
                    }
                }
            }
    }
    stage { if (stacked) { matrix(); slot() } else { slot(); matrix() } }
}

/**
 * The change from the step shown before to [shown], for every stage that steps (GEM, tables). It starts in the frame of
 * the step: made while composing, at progress 0, so the first frame after a tap still shows the state before (no jump to
 * the new one and back, Teo 17. 11.). Turning back before it ends plays the same change in reverse from what is on the
 * screen; elsewhere it continues from the state that is mostly visible. 280 ms ease; under "Remove animations" a 120 ms
 * cross-fade on its own clock.
 */
@Composable
fun rememberStepFade(key: Any, shown: Int): GemFade {
    val still = calm()
    val last = remember(key) { arrayOfNulls<GemFade>(1) } // a plain holder: writing it must not recompose
    val fade = remember(key, shown) {
        val before = last[0]
        val p = before?.p?.value ?: 1f
        when {
            before == null -> GemFade(shown, shown, Animatable(1f))
            p < 1f && shown == before.from && before.from != before.to -> GemFade(before.to, before.from, Animatable(1f - p))
            else -> {
                val from = if (p >= .5f) before.to else before.from
                GemFade(from, shown, Animatable(if (from == shown) 1f else 0f))
            }
        }
    }
    SideEffect { last[0] = fade }
    LaunchedEffect(fade) {
        // a reversed change has only its rest to go, at the same pace
        val rest = 1f - fade.p.value
        val ms = ((if (still) GemFade.CALM_MS else GemFade.MS) * rest).toInt().coerceAtLeast(1)
        // "Remove animations" sets the system's animation scale to 0, which would make even this cross-fade a cut
        if (still) withContext(GemFade.RealTime) { fade.p.animateTo(1f, tween(ms)) }
        else fade.p.animateTo(1f, tween(ms, easing = FastOutSlowInEasing))
    }
    return fade
}

/**
 * One step change of a stage: from the matrix shown before to [to], progress [p] 0..1 (eased). The opacities are a
 * function of p alone (GemFadeTest): an old value and its new one always add up to one, so a changed cell is never
 * empty or doubled; what stays (an unchanged entry, a tint on a row changed in both steps) does not flicker.
 */
class GemFade(val from: Int, val to: Int, val p: Animatable<Float, AnimationVector1D>) {
    companion object {
        /** Calm and readable: long enough to see the number change, short enough not to wait for it. */
        const val MS = 280
        /** "Remove animations": a plain quick cross-fade, no slide. */
        const val CALM_MS = 120
        /** How far a changed value moves while it changes, as a part of the tallest ascent of the matrix's rows. */
        const val SLIDE = .22f
        fun old(p: Float) = 1f - p
        fun new(p: Float) = p
        /** Something present before ([was]) and/or after ([now]): steady if both, fading out, fading in, or absent. */
        fun presence(was: Boolean, now: Boolean, p: Float) = when { was && now -> 1f; now -> p; was -> 1f - p; else -> 0f }
        /** A clock at the real pace, for the calm fade under "Remove animations". */
        val RealTime = object : androidx.compose.ui.MotionDurationScale { override val scaleFactor = 1f }
    }
}

private fun DrawScope.drawMath(b: MBox, x: Float, baseline: Float, color: Color) {
    val ink = color.toArgb()
    drawIntoCanvas { b.draw(it.nativeCanvas, x, baseline, ink) }
}

/** Tall parentheses as on paper: thin at the ends, a little thicker in the middle. */
private fun DrawScope.parens(w: Float, h: Float, total: Float, color: Color, stroke: Float) {
    val inset = h * .02f
    for (side in 0..1) {
        val x0 = if (side == 0) w * .85f else total - w * .85f
        val bulge = if (side == 0) w * .15f else total - w * .15f
        val p = Path().apply { moveTo(x0, inset); quadraticTo(bulge - (if (side == 0) w * .25f else -w * .25f), h / 2f, x0, h - inset) }
        drawPath(p, color, style = Stroke(stroke * 1.1f))
    }
}

/**
 * How a walk's stage goes into [room], one rule for a lesson's GEM step (GemPic) and a chain in a solution: the
 * operations beside the matrix when that keeps it large enough, else under it with the matrix alone fitted, and below
 * the smallest size the stage scrolls sideways. The smallest size is 70 % of the stage at the phone's normal text, so
 * a larger font never shrinks it under what a normal font shows (at 200 % a 5-column matrix beside its operations took
 * 70 % of its doubled size and scrolled, three columns in view: inspector, 18. 11.). Beside wins while it shows the
 * matrix at least 70 % as large as stacked would.
 */
class GemFit(val scale: Float, val stacked: Boolean, val width: androidx.compose.ui.unit.Dp)

@Composable
fun gemFit(walk: GemWalk, big: Boolean, room: androidx.compose.ui.unit.Dp): GemFit {
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    return remember(walk, big, room, d, face, inks) {
        // the smallest size: 70 % of what the phone's normal text would show. Android enlarges large text less than its
        // font scale (at 200 % a 26 sp entry is about 1.4× as large, not 2×), so the floor comes from the real size
        val base = if (big) 26f else 19f
        val floor = minOf(1f, .7f * base * d.density / with(d) { base.sp.toPx() })
        val limit = with(d) { room.toPx() } * .99f
        // one layout at full size: the matrix scales with the size, the operations' small text stops shrinking at 85 %
        val g = geometry(walk, big, 1f, d, face, inks)
        fun width(k: Float, stacked: Boolean): Float {
            val m = g.matrixW * k; val ops = g.slotW * maxOf(k, .85f)
            return if (stacked) maxOf(m, ops) else m + ops
        }
        // the largest size from [floor] to 1 at which the stage fits; [floor] when none does
        fun largest(stacked: Boolean): Float {
            if (width(1f, stacked) <= limit) return 1f
            if (width(floor, stacked) > limit) return floor
            var lo = floor; var hi = 1f
            repeat(16) { val mid = (lo + hi) / 2; if (width(mid, stacked) <= limit) lo = mid else hi = mid }
            return lo
        }
        val beside = largest(false)
        val under = largest(true)
        val stacked = !(width(beside, false) <= limit && beside >= .7f * under)
        val k = if (stacked) under else beside
        GemFit(k, stacked, with(d) { width(k, stacked).toDp() })
    }
}

/**
 * A walk with its own small controls, for a GEM chain inside a solution or a worked example: the stage, then
 * "‹  Úprava 2 z 3  ›". Too wide for the screen, the stage scrolls sideways as a whole, never wraps inside a matrix.
 */
@Composable
fun GemWalkView(walk: GemWalk, modifier: Modifier = Modifier, big: Boolean = false, znacky: GemZnacky? = null, otvor: Int? = null,
    onCell: ((matica: Int, riadok: Int, stlpec: Int) -> Unit)? = null) {
    val c = T.c
    // the step survives the goal card around it changing (open, then done): a solution keeps it per walk
    val saved = LocalGemSteps.current
    val opening = LocalGemStep.current
    var k by remember(walk) { mutableIntStateOf(saved?.get(walk) ?: (otvor ?: opening).coerceIn(0, walk.size)) }
    LaunchedEffect(walk, k) { saved?.set(walk, k) }
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val fit = gemFit(walk, big, maxWidth)
            val fits = fit.width <= maxWidth
            val shift = if (fits && !fit.stacked) opticalShift(gemSlotWidth(walk, big, fit.scale), GemSlotWeight, fit.width, maxWidth) else 0.dp
            Box(Modifier.offset(x = -shift).horizontalScroll(rememberScrollState())) { GemStage(walk, k, big = big, scale = fit.scale, onStep = if (fits) ({ k = it }) else null, stacked = fit.stacked, znacky = znacky, onCell = onCell) }
        }
        if (walk.size > 0) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepArrow(true, "Predchádzajúca úprava", k > 0, Modifier.testTag("gem-prev")) { k-- }
                Text(if (k == 0) "Zadanie · ${walk.size} ${upravy(walk.size)}" else "Úprava $k z ${walk.size}",
                    style = MaterialTheme.typography.labelMedium, color = c.ink2, modifier = Modifier.padding(horizontal = 10.dp).widthIn(min = 120.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                StepArrow(false, "Ďalšia úprava", k < walk.size, Modifier.testTag("gem-next")) { k++ }
            }
        }
    }
}

/**
 * Marks on one matrix of a walk (the tutor's verdict on a student's chain): rows tinted red, cells framed; [neiste]:
 * grey instead of red (a mark the app is not sure of, e.g. in the last matrix before the student confirms it).
 */
data class GemZnacky(val matica: Int, val riadky: Set<Int>, val bunky: Set<Pair<Int, Int>>, val neiste: Boolean = false)

/** The step a walk opens on (0 on the phone); JVM screenshots of later steps set it (snap/GemShots). */
val LocalGemStep = compositionLocalOf { 0 }

/** The step of each walk in a solution, kept above the goal cards that hold them. */
val LocalGemSteps = compositionLocalOf<MutableMap<Any, Int>?> { null }

/** 1 úprava, 2 úpravy, 5 úprav. */
fun upravy(n: Int) = when { n == 1 -> "úprava"; n in 2..4 -> "úpravy"; else -> "úprav" }

@Composable
private fun StepArrow(back: Boolean, say: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = T.c
    Box(modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).clickable(enabled = enabled, role = Role.Button, onClickLabel = say, onClick = onClick)
        .semantics { contentDescription = say }, contentAlignment = Alignment.Center) {
        // the app's chevron, turned for back
        Icon24(Res.drawable.ic_chevron, tint = if (enabled) c.lesson.text else c.ink3.copy(alpha = .5f),
            modifier = Modifier.size(26.dp).then(if (back) Modifier.graphicsLayer { rotationZ = 180f } else Modifier))
    }
}

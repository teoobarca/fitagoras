package dev.hamper.tabula.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.compat.graphics.nativeCanvas
import dev.hamper.tabula.la.Align
import dev.hamper.tabula.la.TableWalk
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.MBox
import dev.hamper.tabula.math.MathEngine
import dev.hamper.tabula.math.speak
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.lekcia
import dev.hamper.tabula.ui.tableStage
import kotlin.math.abs
import kotlin.math.max

/*
 * The one component for a table that grows by rows (la/TableWalk.kt): Euclid's divisions, the REA. Like the GEM stage
 * its size is a property of the walk: every column as wide as its widest cell, every row reserved, the rule line above
 * reserved for the widest rule; nothing moves between steps. A step brings its row in with the GEM step change (calm,
 * from the frame of the tap, reversed going back): the rows it comes from tinted lightly, numbers that move to the new
 * row (Euclid's divisor and remainder) sliding down to their place, the rest of the row fading in. The step after the
 * last row frames the result and says it above the table.
 */

private class TableGeometry(
    val cells: List<List<MBox>>, val head: List<MBox>?, val colW: FloatArray, val rowH: Float, val asc: Float,
    val padX: Float, val padY: Float, val labels: List<List<MBox>>, val labelH: Float, val labelGap: Float, val headH: Float,
    /** Row numbers in the margin (small, grey) and the margin's width; 0 when there are none. */
    val rowLabels: List<MBox>?, val margin: Float,
) {
    val tableW get() = colW.sum()
    /** The grid with its margin, and the whole stage (the widest rule may be wider). */
    val gridW get() = margin + tableW
    val stageW get() = max(gridW, labels.maxOf { l -> l.maxOfOrNull { it.w } ?: 0f })
    fun colX(j: Int) = (0 until j).sumOf { colW[it].toDouble() }.toFloat()
    fun rowY(i: Int) = headH + i * rowH
    fun tableH(rows: Int) = headH + rows * rowH
}

/** Labels per stage step: 0 none, 1..size the steps' rules, size + 1 the result. */
private fun stageLabels(t: TableWalk): List<List<String>> = listOf(t.startLabel) + t.steps.map { it.label } + (if (t.result.isNotEmpty()) listOf(t.resultLabel) else emptyList())

/** How strong the rows a step comes from are tinted, against the new row's full tint. */
private const val SOURCE_TINT = .6f

/** Steps of the stage: one per row after the first ones, and one for the result when there is one. */
fun tableStageSteps(t: TableWalk) = t.size + if (t.result.isNotEmpty()) 1 else 0

@Composable
private fun rememberTableGeometry(t: TableWalk, big: Boolean, scale: Float): TableGeometry {
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val px = with(d) { (if (big) 21.sp else 18.sp).toPx() } * scale
    // the rule above scales with the table: a floor here made it wider than the card at 200 % text
    val small = with(d) { (if (big) 16.sp else 14.sp).toPx() } * scale
    return remember(t, px, small, face, inks) {
        val cache = HashMap<String, MBox>()
        fun box(s: String, size: Float = px) = cache.getOrPut("$size|$s") { MathEngine.layout(s.ifEmpty { "\\," }, size, face, inks) }
        val cells = t.rows.map { r -> r.map { box(it) } }
        val head = t.header?.map { box(it, small) }
        val padX = px * .38f; val padY = px * .16f
        val colW = FloatArray(t.n) { j -> (cells.maxOf { it[j].w }).coerceAtLeast(head?.get(j)?.w ?: 0f) + 2 * padX }
        val asc = cells.maxOf { r -> r.maxOf { it.asc } }
        val rowH = asc + cells.maxOf { r -> r.maxOf { it.desc } } + 2 * padY
        val labels = stageLabels(t).map { l -> l.map { box(it, small) } }
        val labelGap = small * .3f
        val labelH = labels.maxOf { l -> l.sumOf { (it.asc + it.desc + labelGap).toDouble() }.toFloat() } + small * .4f
        val headH = head?.let { h -> h.maxOf { it.asc + it.desc } + padY * 2.4f } ?: 0f
        val rowLabels = t.rowLabels?.map { box(it, small * .85f) }
        val margin = rowLabels?.let { r -> r.maxOf { it.w } + padX * .8f } ?: 0f
        TableGeometry(cells, head, colW, rowH, asc, padX, padY, labels, labelH, labelGap, headH, rowLabels, margin)
    }
}

/** The width of the row numbers in the table's left margin at [scale] (opticalShift). */
@Composable
fun tableMarginWidth(t: TableWalk, big: Boolean, scale: Float = 1f): Dp {
    val g = rememberTableGeometry(t, big, scale)
    return with(LocalDensity.current) { g.margin.toDp() }
}

/** The stage's natural width: the table or its widest rule, whichever is wider. */
@Composable
fun tableStageWidth(t: TableWalk, big: Boolean, scale: Float = 1f): Dp {
    val g = rememberTableGeometry(t, big, scale)
    return with(LocalDensity.current) { g.stageW.toDp() }
}

/**
 * A scale that fits the walk into [room]: from the walk, never the step, so the stage keeps one size. It goes down to
 * .75 of the table at 100 % text; with a larger text setting (200 %) that is a smaller part of it, so the table still fits
 * a 320 dp phone without wrapping or scrolling (Teo, 17. 11.).
 */
@Composable
fun tableFitScale(t: TableWalk, big: Boolean, room: Dp): Float {
    val w = tableStageWidth(t, big)
    val floor = .75f / max(1f, LocalDensity.current.fontScale)
    return if (w <= room) 1f else max(floor, room / w * .98f)
}

/** The table at stage step [k] (0: the first rows; size + 1: the result), fixed size for the whole walk. */
@Composable
fun TableStage(t: TableWalk, k: Int, modifier: Modifier = Modifier, big: Boolean = false, scale: Float = 1f, onStep: ((Int) -> Unit)? = null) {
    val c = T.c
    val g = rememberTableGeometry(t, big, scale)
    val d = LocalDensity.current
    val total = tableStageSteps(t)
    val fade = rememberStepFade(t, k)
    val still = dev.hamper.tabula.ui.calm()
    val width = g.stageW
    val ink = c.ink; val ink2 = c.ink2; val ink3 = c.ink3; val hue = c.gem
    val gestures = if (onStep == null) Modifier else Modifier.pointerInput(t, k) {
        var dx = 0f
        detectHorizontalDragGestures(onDragStart = { dx = 0f }, onDragEnd = {
            val limit = 48.dp.toPx()
            if (dx < -limit && k < total) onStep(k + 1) else if (dx > limit && k > 0) onStep(k - 1)
        }) { ch, a -> ch.consume(); dx += a }
    }
    Column(modifier.then(gestures).testTag("table-stage")) {
        // the rule of the step above the table: reserved for the tallest one, so the table never moves down
        Canvas(Modifier.size(with(d) { width.toDp() }, with(d) { g.labelH.toDp() }).testTag("table-rule").semantics {
            // announced on each step: its rule and the row it adds (not the whole growing table again)
            val newest = if (k in 1..t.size) "; " + say(t, t.visible(k) - 1) else ""
            contentDescription = stageLabels(t)[k].joinToString(", ") { sayTex(it) } + newest
            liveRegion = LiveRegionMode.Polite
        }) {
            val tt = fade.p.value
            fun rule(at: Int, a: Float) {
                if (a <= 0f) return
                var y = 0f
                for (b in g.labels[at]) { drawBox(b, (size.width - b.w) / 2f, y + b.asc, (if (at == total && t.result.isNotEmpty()) hue.text else ink2).copy(alpha = a)); y += b.asc + b.desc + g.labelGap }
            }
            if (fade.from != fade.to) rule(fade.from, GemFade.old(tt))
            rule(fade.to, if (fade.from != fade.to) GemFade.new(tt) else 1f)
        }
        val h = g.tableH(t.rows.size)
        Canvas(Modifier.size(with(d) { width.toDp() }, with(d) { h.toDp() }).testTag("table-grid")
            .semantics { contentDescription = (0 until t.visible(k.coerceAtMost(t.size))).joinToString("; ") { say(t, it) } }) {
            val tt = fade.p.value
            val x0 = (size.width - g.gridW) / 2f + g.margin
            val stroke = 1.3.dp.toPx()
            val gap = 1.5.dp.toPx()
            fun tintRow(i: Int, col: Color) = drawRoundRect(col, Offset(x0 + g.padX * .3f, g.rowY(i) + gap), Size(g.tableW - g.padX * .6f, g.rowH - 2 * gap), CornerRadius(6.dp.toPx()))
            val a = fade.from; val b = fade.to
            fun rowsAt(s: Int) = t.visible(s.coerceAtMost(t.size))
            val rowsFrom = rowsAt(a); val rowsTo = rowsAt(b)
            // the change that adds or removes one row (a single step): its row, the rows it comes from, its slides
            val single = abs(a - b) == 1 && max(a, b) <= t.size
            val toward = if (b > a) tt else 1f - tt // how far the new row has come in
            val step = if (single) t.steps[max(a, b) - 1] else null
            val newRow = if (single) t.visible(max(a, b)) - 1 else -1
            val slideTargets = step?.slides?.map { it.second }?.toSet() ?: emptySet()
            // tints of a step: its new row full, the rows it comes from lighter; between two steps (also back, also a
            // double tap) the one before fades out while the one after fades in, as GEM does
            fun tints(s: Int): Map<Int, Float> = if (s !in 1..t.size) emptyMap()
                else t.steps[s - 1].sources.associateWith { SOURCE_TINT } + (t.visible(s) - 1 to 1f)
            val tFrom = tints(a); val tTo = tints(b)
            for (i in tFrom.keys + tTo.keys) {
                val ta = if (a == b) tTo[i] ?: 0f else (tFrom[i] ?: 0f) * (1f - tt) + (tTo[i] ?: 0f) * tt
                if (ta > 0f) tintRow(i, hue.tint.copy(alpha = ta))
            }
            // row numbers in the margin, with their rows
            g.rowLabels?.forEachIndexed { i, lb ->
                val ra = GemFade.presence(i < rowsFrom, i < rowsTo, tt)
                if (ra > 0f) drawBox(lb, x0 - g.padX * .5f - lb.w, g.rowY(i) + g.padY + g.asc, ink2.copy(alpha = ra))
            }
            // the head and its rule
            g.head?.let { hd ->
                for (j in 0 until t.n) drawBox(hd[j], x0 + cellX(g, t, j, hd[j]), g.headH - g.padY * 1.4f - hd[j].desc, ink2)
                drawLine(ink2, Offset(x0, g.headH - stroke), Offset(x0 + g.tableW, g.headH - stroke), stroke)
            }
            // vertical rules down to the rows shown (they grow with the table)
            val shownRows = rowsFrom + (rowsTo - rowsFrom) * tt
            for (j in t.rules) { val x = x0 + g.colX(j); drawLine(ink2, Offset(x, if (g.head != null) 0f else g.padY), Offset(x, g.rowY(0) + shownRows * g.rowH), stroke) }
            // the numbers that move to the new row, coloured at both ends while their step is shown (Euclid)
            val shownStep = if (b in 1..t.size) t.steps[b - 1] else step
            val moving = shownStep?.slides?.flatMap { listOf(it.first, it.second) }?.toSet() ?: emptySet()
            // the number a step takes from the row above (the REA's q): coloured and ringed while the step is shown
            fun marksOf(s: Int) = if (s in 1..t.size) t.steps[s - 1].marks else emptySet()
            val mFrom = marksOf(a); val mTo = marksOf(b)
            for ((i, j) in mFrom + mTo) {
                val ma = if (a == b) 1f else GemFade.presence((i to j) in mFrom, (i to j) in mTo, tt)
                if (ma > 0f) drawRoundRect(hue.main.copy(alpha = ma), Offset(x0 + g.colX(j) + g.padX * .4f, g.rowY(i) + g.padY * .3f),
                    Size(g.colW[j] - g.padX * .8f, g.rowH - g.padY * .6f), CornerRadius(5.dp.toPx()), style = Stroke(stroke))
            }
            // the marked q takes the hue with its ring, at the same pace
            fun markColour(i: Int, j: Int): Color? = if ((i to j) !in mFrom && (i to j) !in mTo) null else
                androidx.compose.ui.graphics.lerp(ink, hue.text, if (a == b) 1f else GemFade.presence((i to j) in mFrom, (i to j) in mTo, tt))
            // the cells: rows present on both sides stay; a row coming or going fades; moving numbers slide
            for (i in t.rows.indices) {
                val was = i < rowsFrom; val now = i < rowsTo
                val alpha = GemFade.presence(was, now, tt)
                if (alpha <= 0f && !(i == newRow && slideTargets.isNotEmpty())) continue
                val base = g.rowY(i) + g.padY + g.asc
                for (j in 0 until t.n) {
                    if (i == newRow && (i to j) in slideTargets) continue // drawn as it slides, below
                    val bx = g.cells[i][j]
                    val col = markColour(i, j) ?: when { (i to j) in moving -> hue.text; j in t.quiet -> ink3; else -> ink }
                    drawBox(bx, x0 + cellX(g, t, j, bx), base, col.copy(alpha = alpha))
                }
            }
            // Euclid: the divisor and the remainder slide down a row to become the next dividend and divisor
            step?.slides?.forEach { (from, to) ->
                val bx = g.cells[to.first][to.second]
                val fx = x0 + cellX(g, t, from.second, g.cells[from.first][from.second]); val fy = g.rowY(from.first) + g.padY + g.asc
                val tx = x0 + cellX(g, t, to.second, bx); val ty = g.rowY(to.first) + g.padY + g.asc
                // "Remove animations": no flight, the number just appears in its place
                if (still) drawBox(bx, tx, ty, hue.text.copy(alpha = toward))
                else drawBox(bx, fx + (tx - fx) * toward, fy + (ty - fy) * toward, hue.text.copy(alpha = if (toward <= 0f) 0f else 1f))
            }
            // the result framed (the gcd, its row)
            if (t.result.isNotEmpty()) {
                val ra = GemFade.presence(a == total, b == total, tt)
                // one frame per row around its result cells (the gcd; its row with α and β)
                if (ra > 0f) for ((i, cells) in t.result.groupBy { it.first }) {
                    val j0 = cells.minOf { it.second }; val j1 = cells.maxOf { it.second }
                    drawRoundRect(hue.main.copy(alpha = ra), Offset(x0 + g.colX(j0) + g.padX * .5f, g.rowY(i) + g.padY * .3f),
                        Size(g.colX(j1) + g.colW[j1] - g.colX(j0) - g.padX * .6f, g.rowH - g.padY * .6f), CornerRadius(6.dp.toPx()), style = Stroke(stroke * 1.1f))
                }
            }
        }
    }
}

/** A row for TalkBack with its number and column names: "riadok 9, r i 2, alfa i 28, beta i mínus 45, q i 2". */
private fun say(t: TableWalk, i: Int, r: List<String> = t.rows[i]) = (t.rowLabels?.let { "riadok ${it[i]}, " } ?: "") +
    r.indices.filter { r[it].isNotEmpty() }.joinToString(", ") { j -> (t.header?.let { sayTex(it[j]) + " " } ?: "") + sayTex(r[j]) }

/** speak() with the commands of these tables read as words. */
private fun sayTex(s: String) = speak(s.replace("\\cdot", " krát ").replace("\\lfloor", " dolná celá časť z ").replace("\\rfloor", " ")
    .replace("\\gcd", " gcd ").replace("\\text{ř}", " riadok ").replace("\\equiv", " je kongruentné ").replace("\\pmod", " modulo "))

private fun cellX(g: TableGeometry, t: TableWalk, j: Int, b: MBox): Float = g.colX(j) + when (t.align[j]) {
    Align.Start -> g.padX
    Align.Center -> (g.colW[j] - b.w) / 2f
    Align.End -> g.colW[j] - g.padX - b.w
}

private fun DrawScope.drawBox(b: MBox, x: Float, baseline: Float, color: Color) {
    val ink = color.toArgb()
    drawIntoCanvas { b.draw(it.nativeCanvas, x, baseline, ink) }
}

/**
 * A table walk with its own small controls, in a worked example or a solution: the stage, then "‹  Krok 2 z 8  ›". It
 * fits the width (down to .75 of its size, then it scrolls sideways as a whole, never wraps).
 */
@Composable
fun TableWalkView(t: TableWalk, modifier: Modifier = Modifier, big: Boolean = false) {
    val c = T.c
    val total = tableStageSteps(t)
    val saved = LocalGemSteps.current
    val opening = LocalGemStep.current
    var k by remember(t) { mutableIntStateOf(saved?.get(t)?.coerceIn(0, total) ?: opening.coerceIn(0, total)) }
    LaunchedEffect(t, k) { saved?.set(t, k) }
    // new look: the table's reserved room is a stage of its own, the arrows on its bottom edge (ui/LekciaVzhlad.kt)
    Column(modifier.fillMaxWidth().then(if (lekcia()) Modifier.tableStage() else Modifier), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val scale = tableFitScale(t, big, maxWidth)
            val stage = tableStageWidth(t, big, scale)
            val fits = stage <= maxWidth
            // centred by eye: the grey row numbers in the margin count for nothing, the table itself is centred
            val shift = if (fits) opticalShift(tableMarginWidth(t, big, scale), .5f, stage, maxWidth) else 0.dp
            Box(Modifier.offset(x = -shift).horizontalScroll(rememberScrollState())) { TableStage(t, k, big = big, scale = scale, onStep = if (fits) ({ k = it }) else null) }
        }
        if (total > 0) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Arrow("‹", "Predchádzajúci krok", k > 0, Modifier.testTag("table-prev")) { k-- }
                // one line also at 200 % text on a 320 dp phone: short labels, the count shows from the first step
                // short, one line, and never at the arrows' cost: the counter takes only the room left between them
                Text(when { k == 0 -> "Začiatok"; k == total && t.result.isNotEmpty() -> "Výsledok"; else -> "$k z $total" },
                    style = MaterialTheme.typography.labelMedium, color = c.ink2, textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                    modifier = Modifier.weight(1f, fill = false).padding(horizontal = 6.dp).widthIn(min = 72.dp).testTag("table-counter"))
                Arrow("›", "Ďalší krok", k < total, Modifier.testTag("table-next")) { k++ }
            }
        }
    }
}

/** 1 krok, 2 kroky, 5 krokov. */
fun kroky(n: Int) = when { n == 1 -> "krok"; n in 2..4 -> "kroky"; else -> "krokov" }

@Composable
private fun Arrow(sym: String, say: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = T.c
    Box(modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).clickable(enabled = enabled, role = Role.Button, onClickLabel = say, onClick = onClick)
        .semantics { contentDescription = say }, contentAlignment = Alignment.Center) {
        Text(sym, style = MaterialTheme.typography.titleLarge, color = if (enabled) c.lesson.text else c.ink3.copy(alpha = .5f))
    }
}

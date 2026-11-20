package dev.hamper.tabula.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import dev.hamper.tabula.compat.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.MathEngine
import dev.hamper.tabula.math.MathFace
import dev.hamper.tabula.math.MathInks
import dev.hamper.tabula.ui.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin
import dev.hamper.tabula.compat.graphics.*

/** Where the drawing is on the canvas: world (x, y) to pixels. */
data class View(val o: Offset, val unit: Float) {
    fun at(x: Float, y: Float) = o + Offset(x * unit, -y * unit)
}

fun viewOf(w: Float, h: Float, xMin: Float, xMax: Float, yMin: Float, yMax: Float, pad: Float = 0f): View {
    val unit = min((w - pad * 2) / (xMax - xMin), (h - pad * 2) / (yMax - yMin))
    val cx = (w - unit * (xMax - xMin)) / 2 - xMin * unit
    val cy = (h - unit * (yMax - yMin)) / 2 + yMax * unit
    return View(Offset(cx, cy), unit)
}

fun DrawScope.planeGrid(v: View, c: Palette, dots: Boolean = true) {
    val r = 1.5.dp.toPx()
    if (dots) {
        var i = -30
        while (i <= 30) {
            var j = -30
            while (j <= 30) {
                val p = v.at(i.toFloat(), j.toFloat())
                if (p.x in 0f..size.width && p.y in 0f..size.height && i != 0 && j != 0) drawCircle(c.dot, r, p)
                j++
            }
            i++
        }
    }
    val a = 1.5.dp.toPx()
    drawLine(c.axis, Offset(0f, v.o.y), Offset(size.width, v.o.y), a, StrokeCap.Round)
    drawLine(c.axis, Offset(v.o.x, 0f), Offset(v.o.x, size.height), a, StrokeCap.Round)
}

/** Clean vector: straight shaft, solid rounded head. */
fun DrawScope.arrow(from: Offset, to: Offset, color: Color, width: Float = 5.dp.toPx(), head: Float = width * 3.3f) {
    val len = hypot(to.x - from.x, to.y - from.y)
    if (len < 1f) return
    val ang = atan2(to.y - from.y, to.x - from.x)
    val h = min(head, len * .7f)
    val base = Offset(to.x - cos(ang) * h * .9f, to.y - sin(ang) * h * .9f)
    drawLine(color, from, base, width, StrokeCap.Round)
    val p = Path().apply {
        moveTo(to.x, to.y)
        lineTo(to.x - h * cos(ang - .42f), to.y - h * sin(ang - .42f))
        lineTo(to.x - h * cos(ang + .42f), to.y - h * sin(ang + .42f))
        close()
    }
    drawPath(p, color)
    drawPath(p, color, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
}

/** A span drawn as a soft band with a hairline through it. */
fun DrawScope.spanBand(v: View, dx: Float, dy: Float, hue: Hue, line: Color, width: Float = 22.dp.toPx(), alpha: Float = 1f) {
    val a = v.at(-40 * dx, -40 * dy); val b = v.at(40 * dx, 40 * dy)
    drawLine(hue.tint, a, b, width, StrokeCap.Butt, alpha = alpha)
    drawLine(line, a, b, 1.5.dp.toPx(), alpha = alpha)
}

fun DrawScope.target(p: Offset, c: Palette, label: String?, face: MathFace, inks: MathInks, hit: Boolean = false, below: Boolean = true) {
    val ink = if (hit) c.good else c.target
    drawCircle(ink, 17.dp.toPx(), p, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx()))))
    drawCircle(c.card, 8.dp.toPx(), p)
    drawCircle(ink, 8.dp.toPx(), p, style = Stroke(2.5.dp.toPx()))
    if (label != null) drawMath(label, 15.sp.toPx(), p + Offset(0f, if (below) 34.dp.toPx() else -28.dp.toPx()), ink, face, inks, center = true)
}

/**
 * A formula label in a picture, its baseline at [at]: from [at] to the right, centred on it ([center]) or ending at it
 * ([end]). It never runs more than [bleed] past the picture's left or right edge (the padding of the card around it),
 * where the card would cut it off: a label's width follows the math font, so a fixed offset can't promise that.
 */
fun DrawScope.drawMath(tex: String, px: Float, at: Offset, ink: Color, face: MathFace, inks: MathInks, center: Boolean = false, end: Boolean = false, bleed: Float = 0f) {
    val b = MathEngine.layout(tex, px, face, inks)
    val x = (at.x - if (end) b.w else if (center) b.w / 2 else 0f).coerceIn(-bleed, maxOf(-bleed, size.width + bleed - b.w))
    drawIntoCanvas { b.draw(it.nativeCanvas, x, at.y, ink.toArgb()) }
}

fun tenth(x: Float) = round(x * 10f) / 10f
/** Slovak decimal comma inside a formula. */
fun num(x: Float): String {
    val r = tenth(x)
    val s = if (r == r.toInt().toFloat()) abs(r).toInt().toString() else abs(r).toString().replace(".", "{,}")
    return (if (r < 0) "-" else "") + s
}
fun numPlain(x: Float) = num(x).replace("{,}", ",").replace("-", "−")

/** The lesson's world: u=(1,2), v=(2,4). Everything reachable is s·(1,2). (3,1) is not. */
class PlaneState {
    var alpha by mutableFloatStateOf(1f)
    var beta by mutableFloatStateOf(0f)
    var sMin by mutableFloatStateOf(1f)
    var sMax by mutableFloatStateOf(1f)
    val s get() = alpha + 2f * beta
    fun touch() { sMin = min(sMin, s); sMax = max(sMax, s) }
    val explored get() = sMax - sMin
}

const val AlphaMax = 1f
const val BetaMax = .8f

private const val XMin = -3.4f; private const val XMax = 5.6f
private const val YMin = -5.7f; private const val YMax = 5.7f

@Composable
fun VectorPlane(st: PlaneState, revealed: Boolean, modifier: Modifier = Modifier) {
    val c = T.c
    val hue = c.lesson
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val hx = rememberHaptics()
    Canvas(
        modifier.clipToBounds()
            .semantics { contentDescription = "Rovina s vektormi u a v. Výsledok je ${numPlain(st.s)} krát (1, 2). Cieľ (3, 1) leží mimo priamky." }
            .pointerInput(Unit) {
                val v = viewOf(size.width.toFloat(), size.height.toFloat(), XMin, XMax, YMin, YMax)
                var grab = 2
                detectDragGestures(
                    onDragStart = { p ->
                        val hu = v.at(st.alpha, 2 * st.alpha); val hw = v.at(st.s, 2 * st.s)
                        grab = if ((p - hu).getDistance() < (p - hw).getDistance()) 1 else 2
                    },
                    onDrag = { ch, d ->
                        ch.consume()
                        val ds = (d.x + -d.y * 2f) / 5f / v.unit
                        val a0 = st.alpha; val b0 = st.beta
                        if (grab == 1) st.alpha = (st.alpha + ds).coerceIn(-AlphaMax, AlphaMax)
                        else st.beta = (st.beta + ds / 2f).coerceIn(-BetaMax, BetaMax)
                        if (tenth(a0) != tenth(st.alpha) || tenth(b0) != tenth(st.beta)) hx.tick()
                        st.touch()
                    },
                )
            },
    ) {
        val v = viewOf(size.width, size.height, XMin, XMax, YMin, YMax)
        planeGrid(v, c)
        spanBand(v, 1f, 2f, hue, if (c.dark) hue.edge else Color(0xFF8DB7A7), width = 24.dp.toPx(), alpha = if (revealed) 1f else .55f)
        val a = st.alpha; val s = st.s
        val tipU = v.at(a, 2 * a); val tipW = v.at(s, 2 * s)
        target(v.at(3f, 1f), c, "(3,1)", face, inks)
        val sw = 5.dp.toPx()
        if (abs(st.beta) > .02f) arrow(tipU, tipW, c.v, sw)
        if (abs(a) > .02f) arrow(v.o, tipU, c.u, sw)
        drawCircle(c.u, 5.dp.toPx(), v.o)
        drawCircle(c.card, 9.5.dp.toPx(), tipW)
        drawCircle(c.ink, 6.5.dp.toPx(), tipW)
        val side = if (a >= 0) -1 else 1
        if (abs(a) > .2f) drawMath("u", 18.sp.toPx(), (v.o + tipU) / 2f + Offset(side * 15.dp.toPx() - 6.dp.toPx(), 2.dp.toPx()), c.u, face, inks)
        if (abs(st.beta) > .1f) drawMath("v", 18.sp.toPx(), (tipU + tipW) / 2f + Offset(14.dp.toPx(), 6.dp.toPx()), c.vText, face, inks)
    }
}

/** A small still plane for covers and steps: arrows and a span, nothing to touch. */
@Composable
fun StillPlane(
    modifier: Modifier = Modifier, xMin: Float = -1.6f, xMax: Float = 4.6f, yMin: Float = -1.2f, yMax: Float = 4.6f,
    span: Pair<Float, Float>? = 1f to 2f, full: Float = 0f, vectors: List<Triple<Pair<Float, Float>, Pair<Float, Float>, Color>> = emptyList(),
    targetAt: Pair<Float, Float>? = null, targetLabel: String? = null, labels: List<Triple<Pair<Float, Float>, String, Color>> = emptyList(), bandHue: Hue = T.c.lesson,
) {
    val c = T.c
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    Canvas(modifier.clipToBounds()) {
        val v = viewOf(size.width, size.height, xMin, xMax, yMin, yMax, 6.dp.toPx())
        if (full > 0f) drawRect(bandHue.tint, alpha = full)
        planeGrid(v, c)
        span?.let { spanBand(v, it.first, it.second, bandHue, if (c.dark) bandHue.edge else Color(0xFF8DB7A7), alpha = 1f - full) }
        targetAt?.let { target(v.at(it.first, it.second), c, targetLabel, face, inks) }
        for ((a, b, col) in vectors) arrow(v.at(a.first, a.second), v.at(b.first, b.second), col)
        if (vectors.isNotEmpty()) drawCircle(vectors.last().third, 4.5.dp.toPx(), v.at(vectors.last().first.first, vectors.last().first.second))
        for ((p, l, col) in labels) drawMath(l, 16.sp.toPx(), v.at(p.first, p.second), col, face, inks)
    }
}

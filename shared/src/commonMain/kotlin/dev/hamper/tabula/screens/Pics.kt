package dev.hamper.tabula.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.Goal
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.data.SetKind
import dev.hamper.tabula.la.*
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.Tex
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.ui.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import dev.hamper.tabula.compat.*

/** Every interactive picture reports when its goal is reached; [goalMet] starts true for still pictures. */
@Composable
fun PicView(pic: Pic, modifier: Modifier = Modifier, onGoal: () -> Unit = {}) {
    when (pic) {
        is Pic.Plane -> PlanePic(pic, modifier, onGoal)
        is Pic.TapSpan -> TapSpanPic(pic, modifier, onGoal)
        is Pic.Closure -> ClosurePic(pic.set, modifier, onGoal)
        is Pic.Grid -> GridPic(pic, modifier, onGoal)
        is Pic.Coords -> CoordsPic(pic, modifier, onGoal)
        is Pic.Still -> { LaunchedEffect(Unit) { onGoal() }; StillPic(pic, modifier) }
        is Pic.Gem -> GemPic(pic, modifier, onGoal)
        is Pic.Custom -> Box(modifier.semantics { contentDescription = pic.describe }) {
            pic.Draw(Modifier.fillMaxSize(), onGoal)
            // debug builds: the lesson walker's last resort when it cannot play a topic's picture (it logs that it did)
            if (dev.hamper.tabula.Env.DEBUG) Box(Modifier.align(Alignment.TopEnd).size(4.dp).clickable { onGoal() }.semantics { contentDescription = "dbg:goal" })
        }
    }
}

private fun f(p: Pair<Int, Int>) = p.first.toFloat() to p.second.toFloat()

private fun fmt(x: Float): String {
    val r = (x * 10).roundToInt() / 10f
    val s = if (r == r.toInt().toFloat()) abs(r).toInt().toString() else abs(r).toString().replace(".", "{,}")
    return (if (r < 0) "-" else "") + s
}

/* ---- Plane: a·u + b·v with two sliders ----------------------------------------------------------------- */

@Composable
private fun PlanePic(p: Pic.Plane, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val smooth = p.goal == Goal.Explore
    val step = if (smooth) .1f else 1f
    var a by remember(p) { mutableFloatStateOf(if (p.goal == Goal.Zero) 1f else 0f) }
    var b by remember(p) { mutableFloatStateOf(0f) }
    var moved by remember(p) { mutableFloatStateOf(0f) }
    val (ux, uy) = f(p.u); val (vx, vy) = f(p.v)
    val rx = a * ux + b * vx; val ry = a * uy + b * vy
    val parallel = p.u.first * p.v.second - p.u.second * p.v.first == 0
    val hit = when (p.goal) {
        Goal.Reach -> p.target != null && abs(rx - p.target.first) < .01f && abs(ry - p.target.second) < .01f
        Goal.Zero -> abs(rx) < .01f && abs(ry) < .01f && (abs(a) > .01f || abs(b) > .01f)
        else -> moved > (if (parallel) 3f else 6f)
    }
    // An LN pair never reaches θ nontrivially: after enough tries the step lets you go on and says why.
    val gaveUp = p.goal == Goal.Zero && !parallel && moved >= 8f
    LaunchedEffect(hit, gaveUp) { if (hit) hx.right(); if (hit || gaveUp) onGoal() }
    val goalAB = remember(p) { planeGoal(p) }
    // a reached target is an answer: the sliders stop, so the picture and the feedback keep saying the same thing
    val locked = hit && p.goal != Goal.Explore
    Column(modifier) {
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp))) {
            Canvas(Modifier.fillMaxSize().clipToBounds().semantics { contentDescription = "Rovina: ${fmt(a)} krát u plus ${fmt(b)} krát v je (${fmt(rx)}, ${fmt(ry)})." }) {
                val v = if (p.goal == Goal.Reach) {
                    // Frame what the task is about: zero, u, v, u+v, the target and the way there. Empty quadrants stay out.
                    val pts = buildList {
                        add(0f to 0f); add(ux to uy); add(vx to vy); add(ux + vx to uy + vy)
                        p.target?.let { add(it.first.toFloat() to it.second.toFloat()) }
                        goalAB?.let { add(it.first * ux to it.first * uy) }
                    }
                    val m = 1.5f
                    viewOf(size.width, size.height, pts.minOf { it.first } - m, pts.maxOf { it.first } + m, pts.minOf { it.second } - m, pts.maxOf { it.second } + m)
                } else {
                    // Zoom to what matters: the target and the vectors, not every corner the sliders could reach.
                    val lu = kotlin.math.hypot(ux, uy); val lv = kotlin.math.hypot(vx, vy)
                    val lt = p.target?.let { kotlin.math.hypot(it.first.toFloat(), it.second.toFloat()) } ?: 0f
                    val span = (maxOf(lu, lv, lt) * 2.4f).coerceIn(6f, 11f)
                    val cx = (p.target?.first ?: 0) / 2f; val cy = (p.target?.second ?: 0) / 2f
                    viewOf(size.width, size.height, cx - span * .62f, cx + span * .62f, cy - span * .62f, cy + span * .62f)
                }
                // labels in the picture grow with the font, but only so far: they must not cover what they name
                val lab = 15.dp.toPx() * min(fontScale, 1.3f)
                planeGrid(v, c)
                if (parallel && (ux != 0f || uy != 0f)) spanBand(v, ux, uy, c.lesson, if (c.dark) c.lesson.edge else Color(0xFF8DB7A7), alpha = if (p.goal == Goal.Explore && hit) 1f else .55f)
                p.target?.let { t ->
                    val at = v.at(t.first.toFloat(), t.second.toFloat())
                    target(at, c, null, face, inks, hit)
                    // the label goes on past the target, in the direction the last arrow arrives from, so the arrowhead never lands on it
                    val from = goalAB?.takeIf { it.second != 0 }?.let { v.at(it.first * ux, it.first * uy) } ?: v.o
                    sideLabel("(${t.first},${t.second})", at, at - from, lab, if (hit) c.good else c.target, face, inks)
                }
                if (p.goal == Goal.Zero) {
                    target(v.o, c, null, face, inks, hit)
                    sideLabel("\\theta", v.o, v.o - v.at(ux + vx, uy + vy), lab, if (hit) c.good else c.target, face, inks)
                }
                val tipU = v.at(a * ux, a * uy); val tip = v.at(rx, ry)
                val sw = 4.5.dp.toPx()
                // A coefficient at 0 still shows its vector where it would go, dashed: a preview of what the slider adds.
                if (abs(a) <= .01f) {
                    ghostArrow(v.o, v.at(ux, uy), c.u, sw * .6f)
                    drawMath("\\U", lab, (v.o + v.at(ux, uy)) / 2f + Offset(-20.dp.toPx(), 2.dp.toPx()), c.u, face, inks)
                }
                if (abs(b) <= .01f) {
                    val gv = v.at(a * ux + vx, a * uy + vy)
                    ghostArrow(tipU, gv, c.vText, sw * .6f)
                    drawMath("\\V", lab, (tipU + gv) / 2f + Offset(10.dp.toPx(), 14.dp.toPx()), c.vText, face, inks)
                }
                if (abs(b) > .01f) arrow(tipU, tip, c.v, sw)
                if (abs(a) > .01f) arrow(v.o, tipU, c.u, sw)
                drawCircle(c.u, 4.dp.toPx(), v.o)
                // the result off the edge: its dot waits at the edge, in the direction where it is
                val inset = 12.dp.toPx()
                val seen = Offset(tip.x.coerceIn(inset, size.width - inset), tip.y.coerceIn(inset, size.height - inset))
                if (seen != tip) { drawCircle(c.ink, 6.dp.toPx(), seen, alpha = .5f) }
                drawCircle(c.card, 8.5.dp.toPx(), tip); drawCircle(c.ink, 6.dp.toPx(), tip)
                if (abs(a) > .2f) drawMath("\\U", lab, (v.o + tipU) / 2f + Offset(-20.dp.toPx(), 2.dp.toPx()), c.u, face, inks)
                if (abs(b) > .2f) drawMath("\\V", lab, (tipU + tip) / 2f + Offset(10.dp.toPx(), 14.dp.toPx()), c.vText, face, inks)
            }
        }
        // what u and v are, and the sum the sliders make, sit under the picture, never on it: at any font size the
        // arrows and the target stay clear
        val sep = if (listOf(a, b, rx, ry).any { abs(it - it.roundToInt()) > .01f }) ";" else ","
        val formula = "${fmt(a)}\\U ${if (b < 0) "-" else "+"} ${fmt(abs(b))}\\V=(${fmt(rx)}$sep${fmt(ry)})"
        Spacer(Modifier.height(8.dp))
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(6.dp), itemVerticalAlignment = Alignment.CenterVertically) {
            Tex("\\U=(${p.u.first},${p.u.second}),\\ \\V=(${p.v.first},${p.v.second})", Modifier.padding(start = 4.dp, end = 12.dp), size = 15.sp, color = c.ink2)
            Box(Modifier.background(if (locked) c.goodTint else c.card, RoundedCornerShape(12.dp)).border(1.5.dp, if (locked) c.good else c.line, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Tex(formula, size = 16.sp, color = c.ink)
            }
        }
        Spacer(Modifier.height(4.dp))
        val r = p.range.toFloat()
        CoefSlider("alfa", a, -r..r, c.u, c.lesson.edge, "Koeficient alfa pri u", step = step, valueText = "$\\alpha=${fmt(a)}$", tag = goalAB?.let { "val=${it.first}" } ?: "wiggle", locked = locked) { a = it; moved += step }
        CoefSlider("beta", b, -r..r, c.v, c.paper.edge, "Koeficient beta pri v", step = step, valueText = "$\\beta=${fmt(b)}$", tag = goalAB?.let { "val=${it.second}" } ?: "wiggle", textColor = c.vText, locked = locked) { b = it; moved += step }
    }
}

/** A vector that is not added yet: dashed shaft, open head, full colour so it reads, but plainly not the real thing. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.ghostArrow(from: Offset, to: Offset, color: Color, width: Float) {
    val len = kotlin.math.hypot(to.x - from.x, to.y - from.y)
    if (len < 1f) return
    val ang = kotlin.math.atan2(to.y - from.y, to.x - from.x)
    val h = min(width * 4.2f, len * .6f)
    val base = Offset(to.x - kotlin.math.cos(ang) * h * .8f, to.y - kotlin.math.sin(ang) * h * .8f)
    drawLine(color, from, base, width, StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(width * 2.2f, width * 2f)))
    val head = androidx.compose.ui.graphics.Path().apply {
        moveTo(to.x - h * kotlin.math.cos(ang - .45f), to.y - h * kotlin.math.sin(ang - .45f))
        lineTo(to.x, to.y)
        lineTo(to.x - h * kotlin.math.cos(ang + .45f), to.y - h * kotlin.math.sin(ang + .45f))
    }
    drawPath(head, color, style = Stroke(width, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}

/** A label next to a point, pushed out along [dir] (screen space) and kept inside the picture. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.sideLabel(tex: String, at: Offset, dir: Offset, px: Float, ink: Color, face: dev.hamper.tabula.math.MathFace, inks: dev.hamper.tabula.math.MathInks) {
    val b = dev.hamper.tabula.math.MathEngine.layout(tex, px, face, inks)
    val hgt = b.asc + b.desc
    val n = kotlin.math.hypot(dir.x, dir.y).takeIf { it > 1f }?.let { dir / it } ?: Offset(0f, 1f)
    // from the centre: the dashed ring, a gap, then half the label measured along the direction
    val reach = 17.dp.toPx() + 6.dp.toPx() + abs(n.x) * b.w / 2 + abs(n.y) * hgt / 2
    val m = 6.dp.toPx()
    val cx = (at.x + n.x * reach).coerceIn(m + b.w / 2, size.width - m - b.w / 2)
    val cy = (at.y + n.y * reach).coerceIn(m + hgt / 2, size.height - m - hgt / 2)
    drawMath(tex, px, Offset(cx - b.w / 2, cy - hgt / 2 + b.asc), ink, face, inks)
}

/** Debug walker only: the slider values that reach the goal, if there are integer ones. */
private fun planeGoal(p: Pic.Plane): Pair<Int, Int>? {
    val u = vec(p.u.first, p.u.second); val v = vec(p.v.first, p.v.second)
    return when (p.goal) {
        Goal.Reach -> p.target?.let { t -> Field.R.solve(listOf(u, v), vec(t.first, t.second))?.let { s -> if (s.all { it.isInt }) s[0].num.toInt() to s[1].num.toInt() else null } }
        Goal.Zero -> Field.R.dependency(listOf(u, v))?.let { d -> val k = d.fold(1L) { acc, f -> acc * f.den / gcd(acc, f.den) }; (d[0].num * (k / d[0].den)).toInt() to (d[1].num * (k / d[1].den)).toInt() }
        else -> null
    }
}

/* ---- Tap the points of a span ---------------------------------------------------------------------------- */

@Composable
private fun TapSpanPic(p: Pic.TapSpan, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val picked = remember(p) { mutableStateListOf<Pair<Int, Int>>() }
    var checked by remember(p) { mutableStateOf(false) }
    val gens = listOfNotNull(p.u, p.v).map { vec(it.first, it.second) }
    fun inside(q: Pair<Int, Int>) = Field.R.inSpan(gens, vec(q.first, q.second))
    val correct = p.points.all { (it in picked) == inside(it) }
    LaunchedEffect(checked, correct) { if (checked && correct) { hx.right(); onGoal() } }
    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 280.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp))) {
            val wPx = constraints.maxWidth.toFloat(); val hPx = constraints.maxHeight.toFloat()
            val xsAll = p.points.map { it.first } + 0; val ysAll = p.points.map { it.second } + 0
            val v = viewOf(wPx, hPx, xsAll.min() - 1.4f, xsAll.max() + 1.6f, ysAll.min() - 1.2f, ysAll.max() + 1.2f)
            Canvas(Modifier.fillMaxSize()) {
                planeGrid(v, c)
                if (checked) { if (p.v == null || gens.size == 1 || Field.R.rank(gens) == 1) spanBand(v, p.u.first.toFloat(), p.u.second.toFloat(), c.lesson, if (c.dark) c.lesson.edge else Color(0xFF8DB7A7)) else drawRect(c.lesson.tint, alpha = .35f) }
                arrow(v.o, v.at(p.u.first.toFloat(), p.u.second.toFloat()), c.u); drawCircle(c.u, 4.dp.toPx(), v.o)
                p.v?.let { arrow(v.o, v.at(it.first.toFloat(), it.second.toFloat()), c.v) }
                for (q in p.points) {
                    val at = v.at(q.first.toFloat(), q.second.toFloat())
                    val on = q in picked
                    val bad = checked && on != inside(q)
                    val col = when { bad -> c.bad; on -> c.ink; else -> c.ink2 }
                    drawCircle(c.card, 11.dp.toPx(), at)
                    if (on) drawCircle(col, 9.dp.toPx(), at) else drawCircle(col, 8.dp.toPx(), at, style = Stroke(2.5.dp.toPx()))
                    drawMath("(${q.first},${q.second})", 14.sp.toPx(), at + Offset(15.dp.toPx(), if (q.second == 0) -9.dp.toPx() else 5.dp.toPx()), if (bad) c.bad else c.ink, face, inks)
                }
            }
            val d = LocalDensity.current
            for (q in p.points) {
                val at = v.at(q.first.toFloat(), q.second.toFloat())
                with(d) {
                    Box(
                        Modifier.offset(at.x.toDp() - 24.dp, at.y.toDp() - 24.dp).size(48.dp).clip(CircleShape)
                            .clickable(role = Role.Checkbox) { hx.play(Buzz.Select); checked = false; if (q in picked) picked.remove(q) else picked.add(q) }
                            .semantics { contentDescription = "Bod ${q.first}, ${q.second}" + (if (inside(q)) dbg("in")?.let { " $it" } ?: "" else ""); selected = q in picked },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (checked && !correct) {
            val w = p.points.first { (it in picked) != inside(it) }
            Bubble("Pozri sa na $(${w.first},${w.second})$. " + if (inside(w)) "Ten v obale je." else "Ten v obale nie je.", Modifier.fillMaxWidth(), fill = c.badTint, line = c.bad)
            Spacer(Modifier.height(6.dp))
        }
        if (!(checked && correct)) Second(if (checked) "Skontrolovať znova" else "Skontrolovať", { checked = true; if (!correct) hx.wrong() })
    }
}

/* ---- Closure: is this set closed under + and scalar multiples? -------------------------------------------- */

private fun SetKind.contains(x: Float, y: Float): Boolean = when (this) {
    SetKind.LineThroughZero -> abs(y - 2 * x) < .01f
    SetKind.LineOffZero -> abs(y - x - 1) < .01f
    SetKind.Cross -> abs(x) < .01f || abs(y) < .01f
    SetKind.Abs -> abs(abs(x) - abs(y)) < .01f
    SetKind.HalfPlane -> x >= 1f - .01f
    SetKind.Quadrant -> x >= -.01f && y >= -.01f
}

private fun SetKind.points(): List<Pair<Int, Int>> = when (this) {
    SetKind.LineThroughZero -> listOf(-1 to -2, 1 to 2, 2 to 4, -2 to -4)
    SetKind.LineOffZero -> listOf(0 to 1, 1 to 2, -1 to 0, 2 to 3)
    SetKind.Cross -> listOf(1 to 0, 0 to 1, -2 to 0, 0 to -1)
    SetKind.Abs -> listOf(1 to 1, 1 to -1, -2 to 2, 2 to 2)
    SetKind.HalfPlane -> listOf(1 to 1, 2 to -1, 1 to -2)
    SetKind.Quadrant -> listOf(1 to 1, 2 to 0, 0 to 2)
}

@Composable
private fun ClosurePic(set: SetKind, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val picked = remember(set) { mutableStateListOf<Pair<Int, Int>>() }
    val pts = set.points()
    val sum = if (picked.size == 2) (picked[0].first + picked[1].first) to (picked[0].second + picked[1].second) else null
    val k = if (set == SetKind.Quadrant || set == SetKind.HalfPlane) -1 else 2
    val mul = picked.firstOrNull()?.let { (k * it.first) to (k * it.second) }
    val sumIn = sum?.let { set.contains(it.first.toFloat(), it.second.toFloat()) }
    val mulIn = mul?.let { set.contains(it.first.toFloat(), it.second.toFloat()) }
    LaunchedEffect(sum) { if (sum != null) { if (sumIn == true && mulIn == true) hx.right() else hx.wrong(); onGoal() } }
    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 280.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp))) {
            val v = viewOf(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), -4.6f, 4.6f, -5.2f, 5.2f)
            Canvas(Modifier.fillMaxSize().clipToBounds().semantics { contentDescription = "Množina $set. Vybrané body a ich súčet." }) {
                val band = c.lesson.tint; val line = if (c.dark) c.lesson.edge else Color(0xFF8DB7A7)
                when (set) {
                    SetKind.LineThroughZero -> spanBand(v, 1f, 2f, c.lesson, line)
                    SetKind.LineOffZero -> { drawLine(band, v.at(-10f, -9f), v.at(10f, 11f), 22.dp.toPx()); drawLine(line, v.at(-10f, -9f), v.at(10f, 11f), 1.5.dp.toPx()) }
                    SetKind.Cross -> { spanBand(v, 1f, 0f, c.lesson, line); spanBand(v, 0f, 1f, c.lesson, line) }
                    SetKind.Abs -> { spanBand(v, 1f, 1f, c.lesson, line); spanBand(v, 1f, -1f, c.lesson, line) }
                    SetKind.HalfPlane -> drawRect(band, topLeft = Offset(v.at(1f, 0f).x, 0f), size = androidx.compose.ui.geometry.Size(size.width, size.height))
                    SetKind.Quadrant -> drawRect(band, topLeft = Offset(v.o.x, 0f), size = androidx.compose.ui.geometry.Size(size.width - v.o.x, v.o.y))
                }
                planeGrid(v, c)
                drawCircle(c.ink2, 3.5.dp.toPx(), v.o)
                for (q in pts) {
                    val at = v.at(q.first.toFloat(), q.second.toFloat())
                    val on = q in picked
                    drawCircle(c.card, 11.dp.toPx(), at)
                    if (on) drawCircle(c.u, 9.dp.toPx(), at) else drawCircle(c.ink2, 8.dp.toPx(), at, style = Stroke(2.5.dp.toPx()))
                }
                if (sum != null) {
                    val a = v.at(picked[0].first.toFloat(), picked[0].second.toFloat()); val b = v.at(picked[1].first.toFloat(), picked[1].second.toFloat()); val s = v.at(sum.first.toFloat(), sum.second.toFloat())
                    val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 5.dp.toPx()))
                    drawLine(c.ink2, a, s, 1.5.dp.toPx(), pathEffect = dash); drawLine(c.ink2, b, s, 1.5.dp.toPx(), pathEffect = dash)
                    arrow(v.o, s, if (sumIn == true) c.good else c.bad, 4.dp.toPx())
                    drawMath("(${sum.first},${sum.second})", 14.sp.toPx(), s + Offset(10.dp.toPx(), -8.dp.toPx()), if (sumIn == true) c.good else c.bad, face, inks)
                }
                if (mul != null && sum != null) {
                    val m = v.at(mul.first.toFloat(), mul.second.toFloat())
                    drawCircle(if (mulIn == true) c.good else c.bad, 6.dp.toPx(), m)
                    drawMath("${k}\\cdot", 13.sp.toPx(), m + Offset(9.dp.toPx(), 14.dp.toPx()), c.ink2, face, inks)
                }
            }
            val d = LocalDensity.current
            for (q in pts) {
                val at = v.at(q.first.toFloat(), q.second.toFloat())
                with(d) {
                    Box(
                        Modifier.offset(at.x.toDp() - 24.dp, at.y.toDp() - 24.dp).size(48.dp).clip(CircleShape)
                            .clickable(role = Role.Checkbox) { hx.play(Buzz.Select); if (q in picked) picked.remove(q) else { if (picked.size == 2) picked.removeAt(0); picked.add(q) } }
                            .semantics { contentDescription = "Bod ${q.first}, ${q.second}" + (if (q in pts.take(2)) dbg("in")?.let { " $it" } ?: "" else ""); selected = q in picked },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val txt = when {
            sum == null -> "Ťukni na ${2 - picked.size} ${if (picked.size == 1) "bod" else "body"} množiny $${set.tex}$."
            else -> "Súčet $(${sum.first},${sum.second})$ " + (if (sumIn == true) "je" else "nie je") + " v množine, $${k}\\cdot(${picked[0].first},${picked[0].second})$ " + (if (mulIn == true) "je." else "nie je.")
        }
        Bubble(txt, Modifier.fillMaxWidth())
    }
}

/* ---- Z_p^2 grid: pick vectors, see the span, count, LN; or find every line once ---------------------------- */

@Composable
private fun GridPic(p: Pic.Grid, modifier: Modifier, onGoal: () -> Unit) {
    if (p.n == 3) { CubePic(p, modifier, onGoal); return }
    val c = T.c
    val hx = rememberHaptics()
    val fld = Field.Zp(p.p)
    val space = remember(p.p) { FiniteSpace(p.p, 2) }
    val hunt = p.goal == Goal.Subspaces
    val picked = remember(p) { mutableStateListOf<Pair<Int, Int>>().apply { addAll(p.start) } }
    var taps by remember(p) { mutableIntStateOf(0) }
    // Subspaces: the lines found so far, in order, and what the last tap did
    val found = remember(p) { mutableStateListOf<Subspace>() }
    var repeat by remember(p) { mutableStateOf<Subspace?>(null) }
    val tried = remember(p) { mutableStateListOf<Pair<Int, Int>>() }
    var sameVector by remember(p) { mutableStateOf(false) }
    val vs = picked.map { fld.vec(listOf(it.first, it.second)) }
    val span: Set<Pair<Int, Int>> = remember(picked.toList()) {
        if (vs.isEmpty()) setOf(0 to 0) else {
            val out = HashSet<Pair<Int, Int>>()
            val n = vs.size
            val total = (0 until n).fold(1) { a, _ -> a * p.p }
            for (k in 0 until total) {
                var r = k; val co = (0 until n).map { val x = r % p.p; r /= p.p; x.q() }
                val v = fld.combo(co, vs); out += v[0].num.toInt() to v[1].num.toInt()
            }
            out
        }
    }
    val ln = vs.isNotEmpty() && fld.isLN(vs)
    val all = remember(p.p, p.dim) { space.subspaceCount(p.dim).toInt() }
    val goal = when (p.goal) {
        Goal.Basis -> picked.size == 2 && ln && span.size == p.p * p.p
        Goal.Free -> picked.size >= 3
        Goal.Subspaces -> found.size == all
        // "Ťukni na vektor": the way on opens after the first tap (it waited for a second one, with nothing saying so)
        else -> taps >= 1
    }
    LaunchedEffect(goal) { if (goal) { if (p.goal == Goal.Basis || hunt) hx.right(); onGoal() } }
    val lineColors = huntColors(c)
    // one picked vector: its multiples 0v, 1v, ..., (p-1)v in order, so the jumps over the edge (mod p) show
    val multiples = if (picked.size == 1) (0 until p.p).map { k -> (k * picked[0].first % p.p) to (k * picked[0].second % p.p) } else emptyList()
    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 260.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp)).padding(12.dp)) {
            val side = min(constraints.maxWidth, constraints.maxHeight).toFloat()
            val cell = side / (p.p + 1.4f)
            val d = LocalDensity.current
            val ox = (constraints.maxWidth - cell * p.p) / 2f + cell * .35f; val oy = (constraints.maxHeight - cell * p.p) / 2f - cell * .35f
            val face = LocalMathFace.current; val inks = LocalMathInks.current
            fun at(x: Int, y: Int) = Offset(ox + (x + .5f) * cell, oy + (p.p - y - .5f) * cell)
            val say = if (hunt) "Mriežka Z${p.p} na druhú. Nájdených ${found.size} z $all priamok." else "Mriežka Z${p.p} na druhú. ${slovakPlural(picked.size, "Vybraný", "Vybrané", "Vybraných")} ${picked.size} ${slovakPlural(picked.size, "vektor", "vektory", "vektorov")}, obal má ${span.size} ${slovakPlural(span.size, "bod", "body", "bodov")}."
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = say }) {
                // lines found before: a ring in their colour on each of their points
                if (hunt) found.forEachIndexed { i, line ->
                    for (v in line.elements) if (v != space.zero) drawCircle(lineColors[i % lineColors.size], cell * .3f, at(v[0], v[1]), style = Stroke(2.5.dp.toPx()))
                }
                for (x in 0 until p.p) for (y in 0 until p.p) {
                    val q = at(x, y)
                    val inSpan = (x to y) in span
                    if (inSpan) drawCircle(c.lesson.tint, cell * .36f, q)
                    drawCircle(if (inSpan) c.lesson.main else c.dot, if (inSpan) 5.dp.toPx() else 3.dp.toPx(), q)
                }
                // the multiples in order: a dashed path θ → v → 2v → ..., each point numbered by its k
                if (multiples.size > 1) {
                    for (k in 1 until multiples.size) {
                        val a = at(multiples[k - 1].first, multiples[k - 1].second); val b = at(multiples[k].first, multiples[k].second)
                        drawLine(c.ink3, a, b, 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx())))
                    }
                    for (k in 2 until multiples.size) {
                        val q = at(multiples[k].first, multiples[k].second)
                        drawMath("${k}\\mathbf{v}", 12.sp.toPx(), Offset(q.x + cell * .26f, q.y - cell * .22f), c.ink2, face, inks)
                    }
                }
                picked.forEachIndexed { i, q ->
                    val col = listOf(c.u, c.v, c.bad)[i % 3]
                    arrow(at(0, 0), at(q.first, q.second), col, 3.5.dp.toPx())
                }
                drawCircle(c.ink, 4.dp.toPx(), at(0, 0))
                // coordinates 0..p-1 along both axes, so every dot has a name
                for (k in 0 until p.p) {
                    drawMath("$k", 13.sp.toPx(), Offset(at(k, 0).x, at(k, 0).y + cell * .78f), c.ink2, face, inks, center = true)
                    drawMath("$k", 13.sp.toPx(), Offset(at(0, k).x - cell * .7f, at(0, k).y + 5.dp.toPx()), c.ink2, face, inks, center = true)
                }
            }
            val walk = remember(p) { if (hunt) space.subspaces(p.dim).map { it.basis[0].let { b -> b[0] to b[1] } } else gridGoal(p) }
            for (x in 0 until p.p) for (y in 0 until p.p) {
                if (x == 0 && y == 0) continue
                val q = at(x, y)
                val sz = with(d) { cell.toDp() }.coerceAtLeast(48.dp)
                with(d) {
                    Box(
                        Modifier.offset(q.x.toDp() - sz / 2, q.y.toDp() - sz / 2).size(sz).clip(CircleShape)
                            .clickable(role = Role.Checkbox) {
                                hx.choose(); taps++
                                val v = x to y
                                if (hunt) {
                                    picked.clear(); picked.add(v)
                                    sameVector = v in tried; if (!sameVector) tried.add(v)
                                    val line = space.span(listOf(space.vec(x, y)))
                                    if (line in found) repeat = line else { repeat = null; found.add(line) }
                                } else if (v in picked) picked.remove(v) else { if (picked.size >= 3) picked.removeAt(0); picked.add(v) }
                            }
                            .semantics { contentDescription = "Vektor $x, $y" + (if ((x to y) in walk) dbg("in")?.let { " $it" } ?: "" else ""); selected = (x to y) in picked },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (hunt) {
            val last = picked.firstOrNull()?.let { space.span(listOf(space.vec(it.first, it.second))) }
            val vTex = picked.firstOrNull()?.let { "(${it.first},${it.second})" }
            Bubble(when {
                last == null -> "Ťukni na nenulový vektor §\\mathbf{v}§: rozsvieti sa priamka §\\langle\\mathbf{v}\\rangle§, všetky jeho násobky. Nájdi všetky, každú raz."
                repeat != null -> "§\\langle $vTex\\rangle=${last.setTex()}§: túto priamku už máš. ${if (sameVector) "Ten istý vektor" else "Iný vektor"}, ten istý podpriestor. **${found.size} z $all.**"
                else -> "§\\langle $vTex\\rangle=${last.setTex()}§: nová priamka. **${found.size} z $all.**"
            }.replace('§', '$'), Modifier.fillMaxWidth())
        } else {
            val list = if (picked.isEmpty()) "Nič nevybrané" else "Súbor $(" + picked.joinToString(",") { "(${it.first},${it.second})" } + ")$"
            val status = if (picked.isEmpty()) "" else if (ln) " je LN" else " je LZ"
            Bubble("$list$status. Obal má **${span.size}** ${slovakPlural(span.size, "bod", "body", "bodov")} z ${p.p * p.p}.", Modifier.fillMaxWidth())
        }
    }
}

/** Debug walker only: cells that reach the grid's goal. */
private fun gridGoal(p: Pic.Grid): List<Pair<Int, Int>> = when (p.goal) {
    Goal.Basis -> if (p.start.isEmpty()) listOf(1 to 0, 0 to 1) else listOf(1 to 0)
    Goal.Free -> listOf(1 to 0, 0 to 1, 1 to 1)
    Goal.Count -> listOf(1 to 1)
    else -> listOf(1 to 2, 2 to 1)
}

/* ---- Coordinates on the grid of a basis -------------------------------------------------------------------- */

@Composable
private fun CoordsPic(p: Pic.Coords, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    var a by remember(p) { mutableFloatStateOf(0f) }
    var b by remember(p) { mutableFloatStateOf(0f) }
    val (ux, uy) = f(p.u); val (vx, vy) = f(p.v)
    val rx = a * ux + b * vx; val ry = a * uy + b * vy
    val hit = abs(rx - p.x.first) < .01f && abs(ry - p.x.second) < .01f
    LaunchedEffect(hit) { if (hit) { hx.right(); onGoal() } }
    Column(modifier) {
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp))) {
            Canvas(Modifier.fillMaxSize().clipToBounds().semantics { contentDescription = "Mriežka bázy u, v a bod x." }) {
                val pts = listOf(p.x, p.u, p.v, 0 to 0)
                val v = viewOf(size.width, size.height, pts.minOf { it.first } - 1.6f, pts.maxOf { it.first } + 1.6f, pts.minOf { it.second } - 1.4f, pts.maxOf { it.second } + 1.4f)
                planeGrid(v, c)
                for (k in -12..12) {
                    val kf = k.toFloat()
                    drawLine(c.u.copy(alpha = .18f), v.at(kf * vx - 30f * ux, kf * vy - 30f * uy), v.at(kf * vx + 30f * ux, kf * vy + 30f * uy), 1.dp.toPx())
                    drawLine(c.v.copy(alpha = .18f), v.at(kf * ux - 30f * vx, kf * uy - 30f * vy), v.at(kf * ux + 30f * vx, kf * uy + 30f * vy), 1.dp.toPx())
                }
                target(v.at(p.x.first.toFloat(), p.x.second.toFloat()), c, "\\mathbf{x}", face, inks, hit)
                // the basis itself, faint, always: at α = β = 0 the labels u and v would otherwise point at nothing
                arrow(v.o, v.at(ux, uy), c.u.copy(alpha = .35f), 3.dp.toPx())
                arrow(v.o, v.at(vx, vy), c.v.copy(alpha = .35f), 3.dp.toPx())
                val tipU = v.at(a * ux, a * uy)
                if (abs(a) > .01f) arrow(v.o, tipU, c.u, 4.dp.toPx())
                if (abs(b) > .01f) arrow(tipU, v.at(rx, ry), c.v, 4.dp.toPx())
                drawCircle(c.card, 8.dp.toPx(), v.at(rx, ry)); drawCircle(c.ink, 5.5.dp.toPx(), v.at(rx, ry))
                drawMath("\\U", 15.sp.toPx(), v.at(ux, uy) + Offset(6.dp.toPx(), -6.dp.toPx()), c.u, face, inks)
                drawMath("\\V", 15.sp.toPx(), v.at(vx, vy) + Offset(6.dp.toPx(), 16.dp.toPx()), c.vText, face, inks)
            }
            Box(Modifier.align(Alignment.BottomEnd).padding(10.dp).background(c.card, RoundedCornerShape(12.dp)).border(1.5.dp, c.line, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Tex("(${a.toInt()},${b.toInt()})\\mapsto(${rx.toInt()},${ry.toInt()})", size = 16.sp, color = c.ink)
            }
        }
        Spacer(Modifier.height(6.dp))
        val goal = remember(p) { Field.R.solve(listOf(vec(p.u.first, p.u.second), vec(p.v.first, p.v.second)), vec(p.x.first, p.x.second)) }
        CoefSlider("alfa", a, -4f..4f, c.u, c.lesson.edge, "Krokov po u", step = 1f, valueText = "$\\alpha=${a.toInt()}$", tag = goal?.let { "val=${it[0]}" }) { a = it }
        CoefSlider("beta", b, -4f..4f, c.v, c.paper.edge, "Krokov po v", step = 1f, valueText = "$\\beta=${b.toInt()}$", tag = goal?.let { "val=${it[1]}" }) { b = it }
    }
}

/* ---- A still plane ------------------------------------------------------------------------------------------ */

@Composable
private fun StillPic(p: Pic.Still, modifier: Modifier) {
    val c = T.c
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    Box(modifier.fillMaxWidth().heightIn(min = 200.dp).panel(24.dp).clip(RoundedCornerShape(24.dp))) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = "Rovina s vektormi." }) {
            val pts = p.vectors + listOfNotNull(p.target) + (0 to 0)
            val v = viewOf(size.width, size.height, pts.minOf { it.first } - 1.6f, pts.maxOf { it.first } + 1.8f, pts.minOf { it.second } - 1.2f, pts.maxOf { it.second } + 1.2f)
            planeGrid(v, c)
            p.span?.let { spanBand(v, it.first.toFloat(), it.second.toFloat(), c.lesson, if (c.dark) c.lesson.edge else Color(0xFF8DB7A7)) }
            p.target?.let { target(v.at(it.first.toFloat(), it.second.toFloat()), c, "(${it.first},${it.second})", face, inks) }
            p.vectors.forEachIndexed { i, q ->
                val col = if (i % 2 == 0) c.u else c.v
                arrow(v.o, v.at(q.first.toFloat(), q.second.toFloat()), col, 4.5.dp.toPx())
                // the first, third… label ends left of its tip, the others start right of it
                p.labels.getOrNull(i)?.let { drawMath(it, 15.sp.toPx(), v.at(q.first.toFloat(), q.second.toFloat()) + Offset(if (i % 2 == 0) -16.dp.toPx() else 8.dp.toPx(), if (i % 2 == 0) 18.dp.toPx() else -4.dp.toPx()), if (i % 2 == 0) c.u else c.vText, face, inks, end = i % 2 == 0) }
            }
            drawCircle(c.u, 4.dp.toPx(), v.o)
        }
    }
}

/* ---- GEM walked step by step --------------------------------------------------------------------------------- */

@Composable
private fun GemPic(p: Pic.Gem, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val fld: Field = if (p.p == 0) Field.R else Field.Zp(p.p)
    val walk = remember(p) { dev.hamper.tabula.la.GemWalk.of(fld.echelon(Mat(p.rows.map { r -> r.map { it.q() } }, p.bar))) }
    val opening = LocalGemStep.current
    var k by remember(p) { mutableIntStateOf(opening.coerceIn(0, walk.size)) }
    val done = k >= walk.size
    // walked to the end once: the lesson's "Pokračovať" is on from then on, so the walk's own buttons stay second
    var reachedOnce by remember(p) { mutableStateOf(false) }
    LaunchedEffect(done) { if (done) { reachedOnce = true; hx.right(); onGoal() } }
    Column(modifier) {
        // anchored at the top: the caption, then the stage, whose size is the same on every step (screens/GemWalk.kt)
        // the card fills the room above the buttons; when a large font leaves too little of it, its content scrolls
        // inside it rather than shrink the matrix or run into the buttons (360 dp at 200 %)
        // new look: the card as tall as the matrix, centred in the room above the buttons, which stay at the bottom
        // where the thumb expects them: the matrix is the middle of the screen, no empty card under it
        val card = @Composable { m: Modifier ->
        Column(m.panel(24.dp).verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            p.caption?.let { Label(it); Spacer(Modifier.height(12.dp)) }
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                // a swipe steps only when the stage fits; a wider one scrolls sideways with the same drag
                // the same fit as a GEM chain in a solution (gemFit): at a large font the operations go under the matrix
                val fit = gemFit(walk, true, maxWidth)
                val fits = fit.width <= maxWidth
                // centred by eye: the light operation slot on the left counts less than the matrix (opticalShift)
                val shift = if (fits && !fit.stacked) opticalShift(gemSlotWidth(walk, true, fit.scale), GemSlotWeight, fit.width, maxWidth) else 0.dp
                Box(Modifier.offset(x = -shift).horizontalScroll(rememberScrollState())) { GemStage(walk, k, big = true, scale = fit.scale, onStep = if (fits) ({ hx.tick(); k = it }) else null, stacked = fit.stacked) }
            }
            Spacer(Modifier.height(12.dp))
            Text(if (k == 0) "Zadanie · ${walk.size} ${upravy(walk.size)}" else "Úprava $k z ${walk.size}", style = MaterialTheme.typography.labelMedium, color = c.ink2)
            if (done) {
                Spacer(Modifier.height(8.dp))
                val main = walk.mainColumns
                val n = p.bar ?: main.size
                val h = (0 until n).filter { main[it] }; val v = (0 until n).filter { !main[it] }
                Rich("Hlavné stĺpce: **${h.joinToString(", ") { "${it + 1}." }}**" + (if (v.isNotEmpty()) ", vedľajšie: " + v.joinToString(", ") { "${it + 1}." } else ""), style = MaterialTheme.typography.bodyLarge, color = c.ink)
            }
        }
        }
        if (lekcia()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { card(Modifier.fillMaxWidth()) }
        else card(Modifier.fillMaxWidth().weight(1f).heightIn(min = 200.dp))
        Spacer(Modifier.height(if (lekcia()) 12.dp else 8.dp))
        // new look: the walk's step on is the screen's main button while it runs (the lesson's way on waits hidden)
        if (lekcia() && !done && !reachedOnce) MainAndQuiet("Späť", { if (k > 0) { hx.tick(); k-- } }, "Ďalší krok", { hx.tick(); k++ })
        else ButtonRow {
            Second("Späť", { if (k > 0) { hx.tick(); k-- } })
            Second(if (done) "Odznova" else "Ďalší krok", { hx.tick(); k = if (done) 0 else k + 1 })
        }
    }
}

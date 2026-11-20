package dev.hamper.tabula.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.LessonDef
import dev.hamper.tabula.ui.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/*
 * The pictures of the app. One switch, [ArtStyleNow], decides the hand that draws all of them: the hero card of Dnes,
 * the small pictures of lessons and exercises, the clock in congruences (and Pytagoras' outline follows it). Each
 * picture is a motif drawn through a [Pen]; a style is a pen and a background, so changing the style changes
 * every picture at once and no motif knows which style draws it.
 *
 * Pictures stand only where they teach (the clock in congruences) or mark a moment (the hero card, a lesson on the
 * path, an exercise in Nácvik); a plain question stays clean: its statement, the formula, the answers.
 */

/** The hand that draws the pictures. */
enum class ArtStyle {
    /** A pencil sketch in a squared notebook: lines with a little wobble, a highlighter under what matters (default). */
    Zosit,
    /** Flat shapes of one green family on tiles, the clock as a dark board with chalk (design review 17. 11.). */
    Tabula,
}

/** The one place to change the look of every picture (Teo decides between the two; 17. 11. night: Zosit). */
val ArtStyleNow = ArtStyle.Zosit

/** What a small picture shows. One per kind of problem, never one picture for everything. */
enum class Motif {
    Clock, Coins, Euclid, Crt, Domino, Stairs, Sum, Ineq, Writing, Matrix, Venn, Map, Compose, Power,
    Vectors, Plane, Span, Basis, Coords, Gem, Inverse, Solutions, Param, Count,
    Paper, Review, Clip, Card, Proof, Counter,
}

/** The picture of a lesson, a generator or a drill by its id or route: the topic decides, the id refines. */
fun motifFor(key: String): Motif {
    val k = key.substringAfter(':')
    fun has(vararg s: String) = s.any { k.contains(it) }
    return when {
        has("tc-crt", "tc-combo", "tc-cvoz") -> Motif.Crt
        has("tc-ldr", "tc-diofant") -> Motif.Coins
        has("tc-rea", "tc-delitelnost") -> Motif.Euclid
        k.startsWith("dml-tc") -> Motif.Clock
        has("mi-silna", "mi-rek") -> Motif.Stairs
        has("mi-matic") -> Motif.Matrix
        has("mi-suma") -> Motif.Sum
        has("mi-nerovnosti") -> Motif.Ineq
        has("mi-zapis") -> Motif.Writing
        k.startsWith("dml-mi") -> Motif.Domino
        has("mz-zlozenie", "mz-skladanie") -> Motif.Compose
        has("mz-velkosti", "mz-mohutnost") -> Motif.Power
        has("mz-sipky", "mz-zobrazenia", "mz-injsurj") -> Motif.Map
        k.startsWith("dml-mz") -> Motif.Venn
        has("k3-inverse", "k3-algebra", "k3-inverzia", "k3-regularita") -> Motif.Inverse
        has("k3-product", "k3-nasobenie") -> Motif.Matrix
        has("k3-span-rank", "k3-aplikacie") -> Motif.Span
        has("k3-count", "count-all", "zp-pocet", "zp-vypis", "zp-zoznam", "podpriestory-zp", "obal-vypis") -> Motif.Count
        k == "gem" || k == "ln" || has("k3-") -> Motif.Gem
        has("k4-", "soustava") -> Motif.Solutions
        has("param") -> Motif.Param
        has("coords", "suradnice") -> Motif.Coords
        has("complete", "baza", "soubor", "hladanie", "dimenzia") || k == "dim" -> Motif.Basis
        has("span", "member", "obal") -> Motif.Span
        has("subspace", "podpriestor") -> Motif.Plane
        has("counter") -> Motif.Counter
        has("paper") -> Motif.Paper
        has("review") -> Motif.Review
        has("clips") -> Motif.Clip
        has("proofs") -> Motif.Proof
        has("library", "defs") -> Motif.Card
        else -> Motif.Vectors
    }
}

/* ---- pens ------------------------------------------------------------------------------------------------------ */

/** What a mark is in its picture: the thing itself, what is around it, what moves or is the goal, a mark on a shape. */
private enum class Role { Main, Soft, Accent, Mark }

/** Draws the motifs in a 48 × 48 box. */
private abstract class Pen(val d: DrawScope) {
    abstract fun shape(p: Path, role: Role)
    abstract fun line(a: Offset, b: Offset, role: Role, w: Float = 2.2f)
    abstract fun curve(p: Path, role: Role, w: Float = 2f)
    abstract fun dot(c: Offset, r: Float, role: Role)
    /** A wide soft band (a span, a track). */
    abstract fun band(a: Offset, b: Offset, w: Float)
    /** Fills [p] inside [clip] (the overlap of two sets). */
    abstract fun overlap(clip: Path, p: Path)
    /** The goal: a dashed ring on the board, circled in green in the notebook. */
    abstract fun goal(c: Offset, r: Float)
    abstract fun head(tip: Offset, ux: Float, uy: Float, size: Float, role: Role)
    fun arrow(a: Offset, b: Offset, role: Role, w: Float = 2.6f, size: Float = 6.5f) {
        val dx = b.x - a.x; val dy = b.y - a.y; val l = hypot(dx, dy); val ux = dx / l; val uy = dy / l
        line(a, Offset(b.x - ux * size * .55f, b.y - uy * size * .55f), role, w)
        head(b, ux, uy, size, role)
    }
}

private class Ink(val shape: Color, val soft: Color, val mark: Color, val glow: Color)

/** Flat shapes, one green family (ArtStyle.Tabula). */
private class TilePen(d: DrawScope, val k: Ink) : Pen(d) {
    fun col(r: Role) = when (r) { Role.Main -> k.shape; Role.Soft -> k.soft; Role.Accent -> k.glow; Role.Mark -> k.mark }
    override fun shape(p: Path, role: Role) = d.drawPath(p, col(role))
    override fun line(a: Offset, b: Offset, role: Role, w: Float) = d.drawLine(col(role), a, b, w, StrokeCap.Round)
    override fun curve(p: Path, role: Role, w: Float) = d.drawPath(p, col(role), style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    override fun dot(c: Offset, r: Float, role: Role) = d.drawCircle(col(role), r, c)
    override fun band(a: Offset, b: Offset, w: Float) = d.drawLine(k.soft, a, b, w, StrokeCap.Round)
    override fun overlap(clip: Path, p: Path) = d.clipPath(clip) { drawPath(p, k.shape) }
    override fun goal(c: Offset, r: Float) = d.drawCircle(k.mark, r, c, style = Stroke(1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 1.6f))))
    override fun head(tip: Offset, ux: Float, uy: Float, size: Float, role: Role) {
        val base = Offset(tip.x - ux * size, tip.y - uy * size)
        d.drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(base.x - uy * size * .55f, base.y + ux * size * .55f); lineTo(base.x + uy * size * .55f, base.y - ux * size * .55f); close() }, col(role))
    }
}

/**
 * A pencil in a notebook (ArtStyle.Zosit): every shape an outline with a little wobble, the highlighter under the main
 * thing, the goal circled in green. [wk] scales the line widths (a large picture keeps a pencil's line), [amp] the
 * wobble.
 */
private class PencilPen(d: DrawScope, val a: dev.hamper.tabula.ui.Art, val wk: Float = 1f, val amp: Float = .45f, val fade: Float = 1f) : Pen(d) {
    private var seed = 3
    private fun pencil(alpha: Float = 1f) = a.pencil.copy(alpha = alpha * fade)
    private fun col(r: Role) = when (r) { Role.Main, Role.Mark -> pencil(); Role.Soft -> pencil(.5f); Role.Accent -> a.circled.copy(alpha = fade) }
    private fun stroke(w: Float) = Stroke(w * wk, cap = StrokeCap.Round, join = StrokeJoin.Round)
    override fun shape(p: Path, role: Role) {
        when (role) {
            Role.Main -> {
                d.withTransform({ translate(.7f, .9f) }) { drawPath(p, a.marker.copy(alpha = .95f * fade)) }
                d.drawPath(wobble(p, amp, seed++), pencil(), style = stroke(1.7f))
            }
            Role.Soft -> d.drawPath(wobble(p, amp, seed++), pencil(.55f), style = stroke(1.3f))
            Role.Accent -> {
                d.drawPath(p, a.marker.copy(alpha = .6f * fade))
                d.drawPath(wobble(p, amp, seed++), a.circled.copy(alpha = fade), style = stroke(1.8f))
            }
            Role.Mark -> d.drawPath(p, pencil())
        }
    }
    override fun line(a: Offset, b: Offset, role: Role, w: Float) =
        d.drawPath(wobble(Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y) }, amp * .8f, seed++), col(role), style = stroke(if (role == Role.Soft) 1.3f else 1.8f))
    override fun curve(p: Path, role: Role, w: Float) = d.drawPath(wobble(p, amp * .8f, seed++), col(role), style = stroke(if (role == Role.Soft) 1.3f else 1.8f))
    override fun dot(c: Offset, r: Float, role: Role) = d.drawCircle(col(role), (r * .75f).coerceIn(1.1f, 2.4f), c)
    override fun band(a: Offset, b: Offset, w: Float) = d.drawLine(this.a.marker.copy(alpha = .95f * fade), a, b, w, StrokeCap.Round)
    override fun overlap(clip: Path, p: Path) = d.clipPath(clip) { drawPath(p, this@PencilPen.a.marker.copy(alpha = fade)) }
    override fun goal(c: Offset, r: Float) {
        val ring = Path().apply { addOval(Rect(c, r + 1.2f)) }
        d.drawPath(wobble(ring, amp * 1.4f, seed++), a.circled.copy(alpha = fade), style = stroke(1.6f))
    }
    override fun head(tip: Offset, ux: Float, uy: Float, size: Float, role: Role) {
        val s = size * .85f
        val p = Path().apply {
            moveTo(tip.x - ux * s - uy * s * .6f, tip.y - uy * s + ux * s * .6f); lineTo(tip.x, tip.y)
            lineTo(tip.x - ux * s + uy * s * .6f, tip.y - uy * s - ux * s * .6f)
        }
        d.drawPath(p, col(role), style = stroke(1.8f))
    }
}

private fun o(x: Float, y: Float) = Offset(x, y)
private fun circle(c: Offset, r: Float) = Path().apply { addOval(Rect(c, r)) }
private fun oval(x: Float, y: Float, w: Float, h: Float) = Path().apply { addOval(Rect(x, y, x + w, y + h)) }
private fun box(x: Float, y: Float, w: Float, h: Float, r: Float = 2.4f) = Path().apply { addRoundRect(RoundRect(x, y, x + w, y + h, CornerRadius(r))) }
private fun open(a: Offset, b: Offset, c: Offset, d: Offset? = null) = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); d?.let { lineTo(it.x, it.y) } }
private fun poly(a: Offset, b: Offset, c: Offset, d: Offset? = null) = open(a, b, c, d).apply { close() }
private fun quad(a: Offset, c: Offset, b: Offset) = Path().apply { moveTo(a.x, a.y); quadraticTo(c.x, c.y, b.x, b.y) }

/** Every motif, in a 48 × 48 box, through the pen of the style. */
private fun Pen.motif(m: Motif) {
    when (m) {
        Motif.Clock -> {
            shape(circle(o(24f, 24f), 18f), Role.Main)
            val pts = (0 until 7).map { i -> val a = -PI / 2 + i * 2 * PI / 7; o(24f + 12.5f * cos(a).toFloat(), 24f + 12.5f * sin(a).toFloat()) }
            line(pts[0], pts[3], Role.Soft, 1.8f)
            arrow(pts[3], pts[6], Role.Mark, 2f, 4.5f)
            pts.forEachIndexed { i, p -> dot(p, if (i == 6) 2.6f else 1.5f, Role.Mark) }
            goal(pts[1], 3.4f)
        }
        Motif.Coins -> {
            // two coins: a, and b given back (Bézout: a·x + b·y)
            shape(circle(o(17f, 29f), 11f), Role.Soft)
            curve(circle(o(17f, 29f), 7f), Role.Soft, 1.6f)
            line(o(14.5f, 29f), o(19.5f, 29f), Role.Main, 1.8f)
            shape(circle(o(31f, 19f), 11f), Role.Main)
            curve(circle(o(31f, 19f), 7f), Role.Mark, 1.6f)
            line(o(28.5f, 19f), o(33.5f, 19f), Role.Mark, 1.8f); line(o(31f, 16.5f), o(31f, 21.5f), Role.Mark, 1.8f)
        }
        Motif.Euclid -> {
            // a rectangle cut into the largest squares: Euclid's algorithm drawn
            shape(box(6f, 12f, 22f, 22f), Role.Main)
            shape(box(29.5f, 12f, 12f, 12f), Role.Main)
            shape(box(29.5f, 25.5f, 8.5f, 8.5f), Role.Soft)
            shape(box(39.5f, 25.5f, 2.5f, 8.5f, 1.2f), Role.Accent)
        }
        Motif.Crt -> {
            for (r in 0..2) for (cc in 0..2) shape(box(8f + cc * 11f, 8f + r * 11f, 9.5f, 9.5f, 2.2f), if (r == 1 && cc == 2) Role.Main else Role.Soft)
            dot(o(34.75f, 23.75f), 2f, Role.Mark)
        }
        Motif.Domino -> {
            line(o(3f, 38.2f), o(45f, 38.2f), Role.Soft, 2.4f)
            fun piece(x: Float, r: Float) = d.rotate(r, o(x + 6.5f, 37f)) {
                shape(box(x, 13f, 6.5f, 24f, 1.8f), Role.Main)
                dot(o(x + 3.25f, 19f), 1.3f, Role.Mark); dot(o(x + 3.25f, 30f), 1.3f, Role.Mark)
            }
            piece(31f, 0f); piece(22f, 0f); piece(13f, 24f); piece(4f, 46f)
            curve(quad(o(5f, 9.5f), o(10f, 5.5f), o(15f, 9.5f)), Role.Accent, 1.8f)
            head(o(15f, 9.5f), .78f, .62f, 3.4f, Role.Accent)
        }
        Motif.Stairs -> {
            // each step from the two before it
            listOf(8f, 13f, 19f, 27f).forEachIndexed { i, h -> shape(box(6f + i * 9.5f, 40f - h, 7.5f, h, 1.8f), if (i == 3) Role.Main else Role.Soft) }
            curve(quad(o(19f, 24f), o(26f, 8f), o(34f, 10.5f)), Role.Accent, 1.8f)
            curve(quad(o(28.5f, 18f), o(31f, 11.5f), o(34f, 11f)), Role.Accent, 1.8f)
            head(o(34.4f, 10.8f), .95f, .1f, 3.6f, Role.Accent)
        }
        Motif.Sum -> {
            // 1 + 2 + 3 + 4: a triangle of stones, the newest row marked
            for (r in 0..3) for (i in 0..r) shape(circle(o(24f - r * 4f + i * 8f, 11f + r * 8.4f), 3.1f), if (r == 3) Role.Main else Role.Soft)
            line(o(6f, 43f), o(42f, 43f), Role.Accent, 1.8f)
        }
        Motif.Ineq -> {
            // two columns, the right one always higher: n² against 2ⁿ
            shape(box(8f, 26f, 10f, 15f, 2f), Role.Soft)
            shape(box(30f, 9f, 10f, 32f, 2f), Role.Main)
            curve(open(o(26.5f, 21f), o(21f, 25.5f), o(26.5f, 30f)), Role.Accent, 2.2f)
        }
        Motif.Writing -> {
            // a proof written out on paper, line by line, and the pencil
            shape(box(7f, 6f, 25f, 34f, 3f), Role.Main)
            for (i in 0..3) line(o(11.5f, 13f + i * 6.5f), o(if (i == 3) 20f else 27.5f, 13f + i * 6.5f), Role.Mark, 1.6f)
            d.rotate(35f, o(36f, 30f)) { shape(box(33.5f, 12f, 5f, 24f, 1.4f), Role.Accent); shape(poly(o(33.5f, 36f), o(38.5f, 36f), o(36f, 41f)), Role.Accent) }
        }
        Motif.Matrix -> {
            curve(open(o(11f, 10f), o(7.5f, 10f), o(7.5f, 38f), o(11f, 38f)), Role.Main, 2.2f)
            curve(open(o(31f, 10f), o(34.5f, 10f), o(34.5f, 38f), o(31f, 38f)), Role.Main, 2.2f)
            for (r in 0..1) for (cc in 0..1) shape(circle(o(15f + cc * 12f, 18f + r * 12f), 4f), if (r == cc) Role.Main else Role.Soft)
            shape(box(37.5f, 5f, 6.5f, 6.5f, 1.6f), Role.Accent)
        }
        Motif.Venn -> {
            val a = circle(o(18f, 24f), 12f); val b = circle(o(30f, 24f), 12f)
            overlap(a, b)
            shape(a, Role.Soft); shape(b, Role.Soft)
            dot(o(24f, 24f), 1.8f, Role.Mark)
        }
        Motif.Map -> {
            shape(oval(5.5f, 9.5f, 15f, 29f), Role.Main)
            shape(oval(27.5f, 13f, 15f, 22f), Role.Main)
            listOf(15.4f to 19.2f, 24f to 20f, 32.6f to 28.8f).forEach { (y1, y2) -> line(o(15.6f, y1), o(32.4f, y2), Role.Accent, 1.6f) }
            listOf(15f, 24f, 33f).forEach { dot(o(13f, it), 2.2f, Role.Mark) }
            listOf(19f, 29f).forEach { dot(o(35f, it), 2.2f, Role.Mark) }
        }
        Motif.Compose -> {
            shape(oval(3f, 15f, 11f, 18f), Role.Soft); shape(oval(18.5f, 13f, 11f, 22f), Role.Main); shape(oval(34f, 15f, 11f, 18f), Role.Soft)
            arrow(o(10f, 21f), o(21f, 21f), Role.Accent, 1.6f, 4f); arrow(o(27f, 27f), o(38f, 27f), Role.Accent, 1.6f, 4f)
            curve(quad(o(9f, 35f), o(24f, 46f), o(39f, 35f)), Role.Main, 1.8f)
        }
        Motif.Power -> {
            // all subsets of a three-element set: 1, 3, 3, 1
            listOf(1, 3, 3, 1).forEachIndexed { r, n -> for (i in 0 until n) shape(box(24f - n * 5f + i * 10f + 1f, 6f + r * 9.5f, 8f, 7f, 1.8f), if (r == 3) Role.Main else Role.Soft) }
        }
        Motif.Vectors -> {
            line(o(8f, 38f), o(40f, 18f), Role.Soft, 2f)
            arrow(o(8f, 38f), o(19f, 13f), Role.Main); arrow(o(19f, 13f), o(40f, 18f), Role.Accent)
            dot(o(8f, 38f), 2.6f, Role.Main)
        }
        Motif.Plane -> {
            shape(poly(o(4f, 30f), o(30f, 41f), o(44f, 19f), o(18f, 8f)), Role.Soft)
            line(o(13f, 35f), o(35f, 13f), Role.Main, 2.6f); shape(circle(o(24f, 24f), 3.2f), Role.Main); dot(o(24f, 24f), 1.3f, Role.Mark)
        }
        Motif.Span -> {
            band(o(10f, 44f), o(38f, 4f), 13f)
            arrow(o(13f, 39f), o(23f, 24f), Role.Main); arrow(o(23f, 24f), o(35f, 7f), Role.Accent)
        }
        Motif.Basis -> {
            val a = o(12f, -5f); val b = o(6f, 11f); val og = o(6f, 16f)
            for (x in 0..1) for (y in 0..1) {
                val q = og + a * x.toFloat() + b * y.toFloat()
                shape(poly(q, q + a, q + a + b, q + b), if ((x + y) % 2 == 0) Role.Main else Role.Soft)
            }
            dot(og + a * 2f + b * 2f, 2.6f, Role.Accent)
        }
        Motif.Coords -> {
            val a = o(9f, 0f); val b = o(4.5f, -10f); val og = o(5f, 41f)
            for (n in 0..3) { line(og + b * n.toFloat(), og + b * n.toFloat() + a * 3.6f, Role.Soft, 1.2f); line(og + a * n.toFloat(), og + a * n.toFloat() + b * 3.4f, Role.Soft, 1.2f) }
            arrow(og, og + a * 2f + b * 3f, Role.Main); dot(og + a * 2f + b * 3f, 3f, Role.Accent)
        }
        Motif.Gem -> {
            // row echelon form: a staircase of entries, zeros under it
            for (r in 0..2) for (cc in 0..3) {
                val x = 6f + cc * 9.5f; val y = 10f + r * 10f
                if (cc >= r) shape(box(x, y, 8f, 8f, 2f), if (cc == r) Role.Main else Role.Soft) else dot(o(x + 4f, y + 4f), 1.8f, Role.Soft)
            }
            line(o(6f, 41f), o(42f, 41f), Role.Accent, 1.8f)
        }
        Motif.Inverse -> {
            for (r in 0..1) for (cc in 0..1) shape(box(5f + cc * 9f, 14f + r * 9f, 7.5f, 7.5f, 1.8f), Role.Main)
            line(o(24f, 11f), o(24f, 34f), Role.Soft, 1.6f)
            for (r in 0..1) for (cc in 0..1) shape(box(27.5f + cc * 9f, 14f + r * 9f, 7.5f, 7.5f, 1.8f), if (r == cc) Role.Accent else Role.Soft)
        }
        Motif.Solutions -> {
            // the solutions: a line through zero moved by one particular solution
            line(o(4f, 40f), o(36f, 8f), Role.Soft, 2.2f)
            line(o(12f, 44f), o(44f, 12f), Role.Main, 2.6f)
            arrow(o(20f, 24f), o(28.5f, 28f), Role.Accent, 2f, 5f)
            dot(o(20f, 24f), 2.6f, Role.Main)
        }
        Motif.Param -> {
            line(o(6f, 24f), o(42f, 24f), Role.Soft, 3f); line(o(6f, 24f), o(27f, 24f), Role.Main, 3f)
            shape(circle(o(27f, 24f), 7f), Role.Main); dot(o(27f, 24f), 2.4f, Role.Mark)
            line(o(10f, 33f), o(10f, 36f), Role.Main, 1.6f); line(o(38f, 33f), o(38f, 36f), Role.Main, 1.6f)
        }
        Motif.Count -> {
            // ℤ₃²: nine points, one subspace through zero
            line(o(8f, 40f), o(40f, 8f), Role.Accent, 1.8f)
            for (r in 0..2) for (cc in 0..2) shape(circle(o(12f + cc * 12f, 12f + r * 12f), 3.4f), if (r == 2 - cc) Role.Main else Role.Soft)
        }
        Motif.Paper -> {
            shape(box(10f, 5f, 26f, 36f, 3f), Role.Main)
            for (i in 0..3) line(o(15f, 13f + i * 6f), o(if (i == 3) 24f else 31f, 13f + i * 6f), Role.Mark, 1.6f)
            curve(open(o(27f, 37f), o(31f, 41f), o(41f, 30f)), Role.Accent, 2.4f)
        }
        Motif.Review -> {
            curve(Path().apply { arcTo(Rect(10f, 10f, 38f, 38f), -80f, 290f, true) }, Role.Main, 3f)
            head(o(29.2f, 10.4f), .96f, .28f, 5f, Role.Main)
            dot(o(24f, 24f), 3f, Role.Accent)
        }
        Motif.Clip -> {
            shape(box(5f, 10f, 38f, 26f, 4f), Role.Main)
            shape(poly(o(20f, 16.5f), o(30f, 23f), o(20f, 29.5f)), Role.Mark)
            line(o(5f, 40.7f), o(43f, 40.7f), Role.Soft, 2.4f); line(o(5f, 40.7f), o(20f, 40.7f), Role.Accent, 2.4f)
        }
        Motif.Card -> {
            shape(box(9f, 8f, 30f, 32f, 3f), Role.Main)
            line(o(13f, 14.5f), o(27f, 14.5f), Role.Mark, 3f)
            for (i in 0..2) line(o(13f, 23f + i * 5.5f), o(if (i == 2) 26f else 34f, 23f + i * 5.5f), Role.Mark, 1.6f)
        }
        Motif.Proof -> {
            for (i in 0..3) line(o(7f, 10f + i * 7f), o(if (i == 3) 25f else 41f, 10f + i * 7f), if (i == 3) Role.Main else Role.Soft, 2.2f)
            shape(box(32f, 32f, 9f, 9f, 1.2f), Role.Accent)
        }
        Motif.Counter -> {
            arrow(o(7f, 36f), o(41f, 14f), Role.Soft, 2.4f)
            arrow(o(7f, 36f), o(24f, 25f), Role.Main, 2.8f)
            line(o(33f, 31f), o(41f, 39f), Role.Accent, 2.2f); line(o(41f, 31f), o(33f, 39f), Role.Accent, 2.2f)
        }
    }
}

/** Draws in a 48 × 48 box scaled to [size] at [at] (top left). */
private inline fun DrawScope.in48(at: Offset, size: Float, crossinline block: DrawScope.() -> Unit) {
    val k = size / 48f
    withTransform({ translate(at.x, at.y); scale(k, k, Offset.Zero) }) { block() }
}

/* ---- small pictures --------------------------------------------------------------------------------------------- */

/**
 * A small picture of a lesson or an exercise. In the notebook a light pencil sketch on the card, no tile; on the
 * board a tile with a flat shape. [muted]: a lesson not reached yet.
 */
@Composable
fun MotifTile(m: Motif, modifier: Modifier = Modifier, size: Dp = 56.dp, muted: Boolean = false) {
    val c = T.c
    when (ArtStyleNow) {
        ArtStyle.Zosit -> Canvas(modifier.size(size).alpha(if (muted) .45f else 1f).graphicsLayer().clearAndSetSemantics {}) {
            in48(Offset(size.toPx() * .06f, size.toPx() * .06f), size.toPx() * .88f) { PencilPen(this, c.art, wk = 1.05f).motif(m) }
        }
        ArtStyle.Tabula -> {
            val ink = if (muted) Ink(c.ink3.copy(alpha = .55f), c.line, c.card, c.ink3.copy(alpha = .55f))
                else Ink(c.art.shape, c.art.r4.copy(alpha = if (c.dark) 1f else .75f), c.art.shapeDot, c.art.glowOn)
            Box(modifier.size(size).clip(RoundedCornerShape(16.dp)).background(if (muted) c.sand else c.art.tile).clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(size * .8f)) { in48(Offset.Zero, this.size.minDimension) { TilePen(this, ink).motif(m) } }
            }
        }
    }
}

/** The lesson's picture on the path. */
@Composable
fun LessonThumb(l: LessonDef, modifier: Modifier = Modifier, size: Dp = 56.dp, muted: Boolean = false) = MotifTile(motifFor("lesson:${l.id}"), modifier, size, muted)

/**
 * A thing for today on Dnes: its sketch on a scrap of the hero's squared paper (the grid, a 1 dp line around), so the
 * rows under the hero speak the hero's picture language. On the board the tile of [MotifTile].
 */
@Composable
fun NoteTile(m: Motif, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    val c = T.c
    if (ArtStyleNow != ArtStyle.Zosit) { MotifTile(m, modifier, size); return }
    Canvas(modifier.size(size).graphicsLayer().clearAndSetSemantics {}) {
        val u = density
        val r = CornerRadius(14f * u)
        drawRoundRect(c.art.paper, cornerRadius = r)
        clipPath(Path().apply { addRoundRect(RoundRect(0f, 0f, this@Canvas.size.width, this@Canvas.size.height, r)) }) {
            val g = 9f * u
            var x = g * .5f; while (x < this.size.width) { drawLine(c.art.grid, Offset(x, 0f), Offset(x, this.size.height), u / 1.4f); x += g }
            var y = g * .5f; while (y < this.size.height) { drawLine(c.art.grid, Offset(0f, y), Offset(this.size.width, y), u / 1.4f); y += g }
        }
        drawRoundRect(c.line, cornerRadius = r, style = Stroke(u))
        val s = this.size.width * .78f
        in48(Offset((this.size.width - s) / 2f, (this.size.height - s) / 2f), s) { PencilPen(this, c.art, wk = 1.1f).motif(m) }
    }
}

/* ---- the clock modulo n ------------------------------------------------------------------------------------------ */

/**
 * The clock of [n] places a lesson teaches with: the walk [jumps] (older jumps fainter, the last one marked), [mark]
 * where you stand, [target] the goal, [end] where it landed, [good] the goal reached. [label] draws place i at a point
 * [labelAt] past the circle (more at a large font, so a number never sits on a ring). In the notebook style it is drawn precisely, not sketched: a picture that teaches must be exact
 * (the blind comparison of 17. 11. preferred the clean dial); on the board it is the chalk disk.
 */
fun DrawScope.clockFace(
    center: Offset, r: Float, n: Int, art: dev.hamper.tabula.ui.Art, jumps: List<Int> = emptyList(), mark: Int? = null, target: Int? = null,
    end: Int? = null, good: Int? = null, fade: Boolean = false, goal: Color = art.circled, labelAt: Float = 20f * density,
    label: (DrawScope.(Int, Offset) -> Unit)? = null,
) = when (ArtStyleNow) {
    ArtStyle.Zosit -> inkClock(center, r, n, art, jumps, mark, target, end, good, goal, labelAt, label)
    ArtStyle.Tabula -> chalkClock(center, r, n, art, jumps, mark, target, end, good, fade, label)
}

/**
 * The exact clock in the notebook's colours: a thin quiet ring and places, the walk as chords, where you stand a green
 * dot on a highlighter, the goal a ring in [goal] (a colour of its own, so "here" and "there" never read as one thing;
 * design review 18. 11.).
 */
private fun DrawScope.inkClock(
    center: Offset, r: Float, n: Int, art: dev.hamper.tabula.ui.Art, jumps: List<Int>, mark: Int?, target: Int?, end: Int?, good: Int?,
    goal: Color, labelAt: Float, label: (DrawScope.(Int, Offset) -> Unit)?,
) {
    val u = density
    fun at(k: Int): Offset { val a = -PI / 2 + k * 2 * PI / n; return Offset(center.x + r * cos(a).toFloat(), center.y + r * sin(a).toFloat()) }
    // the ring and the places quiet, but on the dark card still about 3.5 : 1 (the light ones stay as they were)
    val dark = art.pencil.luminance() > .5f
    drawCircle(art.pencil.copy(alpha = if (dark) .5f else .3f), r, center, style = Stroke(2f * u))
    for (i in 0 until jumps.size - 1) {
        val a = at(jumps[i]); val b = at(jumps[i + 1])
        if (a == b) continue
        val last = i == jumps.size - 2
        val dx = b.x - a.x; val dy = b.y - a.y; val l = hypot(dx, dy); val ux = dx / l; val uy = dy / l
        val sh = 9f * u; val ex = b.x - ux * sh; val ey = b.y - uy * sh; val s = 7f * u
        val col = if (last) art.circled else art.pencil.copy(alpha = .3f + .3f * (i + 1) / (jumps.size - 1))
        drawLine(col, Offset(a.x + ux * sh * .6f, a.y + uy * sh * .6f), Offset(ex, ey), (if (last) 2.4f else 1.8f) * u, StrokeCap.Round)
        drawPath(Path().apply { moveTo(ex - ux * s - uy * s * .55f, ey - uy * s + ux * s * .55f); lineTo(ex, ey); lineTo(ex - ux * s + uy * s * .55f, ey - uy * s - ux * s * .55f) }, col, style = Stroke((if (last) 2.4f else 1.8f) * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    for (k in 0 until n) {
        val p = at(k)
        when (k) {
            good -> { drawCircle(art.marker, 14f * u, p); drawCircle(art.circled, 8f * u, p) }
            mark -> { drawCircle(art.marker, 14f * u, p); drawCircle(art.circled, 8f * u, p) }
            end -> drawCircle(art.pencil, 6f * u, p, style = Stroke(2f * u))
            else -> drawCircle(art.pencil.copy(alpha = if (dark) .8f else .5f), (if (k == 0) 4.4f else 3.6f) * u, p)
        }
        if (k == target && k != good) { drawCircle(goal.copy(alpha = .14f), 12f * u, p); drawCircle(goal, 12f * u, p, style = Stroke(2.6f * u)) }
        if (label != null) {
            val a = -PI / 2 + k * 2 * PI / n
            label(k, Offset(center.x + (r + labelAt) * cos(a).toFloat(), center.y + (r + labelAt) * sin(a).toFloat()))
        }
    }
}

/** The notebook's clock: a circle drawn twice in pencil, ticks, the jumps as bowed arcs, the goal circled in green. */
private fun DrawScope.pencilClock(
    center: Offset, r: Float, n: Int, art: dev.hamper.tabula.ui.Art, jumps: List<Int>, mark: Int?, target: Int?, end: Int?, good: Int?,
    label: (DrawScope.(Int, Offset) -> Unit)?,
) {
    val u = density
    fun at(k: Int, rr: Float = r * .8f): Offset { val a = -PI / 2 + k * 2 * PI / n; return Offset(center.x + rr * cos(a).toFloat(), center.y + rr * sin(a).toFloat()) }
    fun bow(a: Offset, b: Offset, k: Float): Offset { val m = (a + b) / 2f; val dx = b.x - a.x; val dy = b.y - a.y; return Offset(m.x - dy * k, m.y + dx * k) }
    val pen = Stroke(2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // the highlighter under the last jump
    if (jumps.size >= 2) {
        val a = at(jumps[jumps.size - 2]); val b = at(jumps.last())
        if (a != b) drawPath(Path().apply { moveTo(a.x, a.y); bow(a, b, -.16f).let { quadraticTo(it.x, it.y, b.x, b.y) } }, art.marker, style = Stroke(10f * u, cap = StrokeCap.Round))
    }
    drawPath(wobble(circle(center, r), .6f * u, 5, 2.4f * u), art.pencil, style = pen)
    drawPath(wobble(Path().apply { arcTo(Rect(center, r - 1.4f * u), 60f, 170f, true) }, .9f * u, 9, 2f * u), art.pencil.copy(alpha = .45f), style = Stroke(1f * u, cap = StrokeCap.Round))
    for (k in 0 until n) {
        val a = at(k, r - 5f * u); val b = at(k, r + 4f * u)
        drawLine(art.pencil, a, b, 1.8f * u, StrokeCap.Round)
    }
    for (i in 0 until jumps.size - 1) {
        val a = at(jumps[i]); val b = at(jumps[i + 1])
        if (a == b) continue
        val last = i == jumps.size - 2
        val q = bow(a, b, -.16f)
        val col = art.pencil.copy(alpha = if (last) 1f else (.3f + .35f * (i + 1) / (jumps.size - 1)))
        drawPath(wobble(Path().apply { moveTo(a.x, a.y); quadraticTo(q.x, q.y, b.x, b.y) }, .7f * u, 13 + i, 2f * u), col, style = Stroke((if (last) 2.2f else 1.8f) * u, cap = StrokeCap.Round))
        val dx = b.x - q.x; val dy = b.y - q.y; val l = hypot(dx, dy); val ux = dx / l; val uy = dy / l; val s = 8f * u
        drawPath(Path().apply { moveTo(b.x - ux * s - uy * s * .6f, b.y - uy * s + ux * s * .6f); lineTo(b.x, b.y); lineTo(b.x - ux * s + uy * s * .6f, b.y - uy * s - ux * s * .6f) }, col, style = Stroke((if (last) 2.2f else 1.8f) * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    for (k in 0 until n) {
        val p = at(k)
        when (k) {
            good -> { drawCircle(art.marker, 9f * u, p); drawCircle(art.circled, 4.6f * u, p) }
            mark -> drawCircle(art.pencil, 4f * u, p)
            end -> drawCircle(art.pencil, 4f * u, p, style = Stroke(1.8f * u))
            else -> drawCircle(art.pencil.copy(alpha = .8f), (if (k == 0) 2.6f else 2f) * u, p)
        }
        val lp = at(k, r + 15f * u)
        if (k == target && k != good) drawPath(wobble(circle(lp, 11f * u), .9f * u, 21, 2f * u), art.circled, style = Stroke(1.9f * u, cap = StrokeCap.Round))
        if (label != null) label(k, lp)
    }
}

/**
 * The clock at the size of the Dnes card's sketch (96 dp): only what reads there (judges, 18. 11., round 5: the full
 * notebook clock with its ticks, second line, highlighter and the circled number was crowded at this size). A pencil
 * ring with a dot for each place, the walk as bowed arrows (older ones faint), where you stand a pencil dot, the goal
 * a green ring on its place, filled when [reached] (then only the last jump into it: the five chords of the whole walk
 * crossed into a knot). The digits stand upright: a tilted "1" read as an italic one.
 */
private fun DrawScope.sketchClock(
    center: Offset, r: Float, n: Int, art: dev.hamper.tabula.ui.Art, jumps: List<Int>, goal: Int, reached: Boolean,
    label: DrawScope.(Int, Offset) -> Unit,
) {
    val u = density
    fun at(k: Int, rr: Float = r): Offset { val a = -PI / 2 + k * 2 * PI / n; return Offset(center.x + rr * cos(a).toFloat(), center.y + rr * sin(a).toFloat()) }
    fun bow(a: Offset, b: Offset, k: Float): Offset { val m = (a + b) / 2f; val dx = b.x - a.x; val dy = b.y - a.y; return Offset(m.x - dy * k, m.y + dx * k) }
    drawPath(wobble(circle(center, r), .22f * u, 5, 2.4f * u), art.pencil.copy(alpha = .55f), style = Stroke(1.6f * u, cap = StrokeCap.Round))
    for (i in 0 until jumps.size - 1) {
        val a = at(jumps[i]); val b = at(jumps[i + 1])
        if (a == b) continue
        val last = i == jumps.size - 2
        // a long walk keeps its older jumps light and without heads, so the one into the goal is what reads
        val col = art.pencil.copy(alpha = if (last) 1f else if (reached) .2f else .45f)
        val w = (if (last) 2f else 1.6f) * u
        val q = bow(a, b, -.14f)
        // the arrow stops short of the place, so its head never sits on the dot
        val dx = b.x - q.x; val dy = b.y - q.y; val l = hypot(dx, dy); val ux = dx / l; val uy = dy / l
        val e = Offset(b.x - ux * 4.5f * u, b.y - uy * 4.5f * u)
        drawPath(wobble(Path().apply { moveTo(a.x, a.y); quadraticTo(q.x, q.y, e.x, e.y) }, .5f * u, 13 + i, 2f * u), col, style = Stroke(w, cap = StrokeCap.Round))
        if (last || !reached) {
            val s = 5.5f * u
            drawPath(Path().apply { moveTo(e.x - ux * s - uy * s * .6f, e.y - uy * s + ux * s * .6f); lineTo(e.x, e.y); lineTo(e.x - ux * s + uy * s * .6f, e.y - uy * s - ux * s * .6f) }, col, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
    val here = if (reached) null else jumps.lastOrNull()
    for (k in 0 until n) {
        val p = at(k)
        when {
            k == goal && reached -> { drawCircle(art.marker, 7f * u, p); drawCircle(art.circled, 4f * u, p) }
            k == goal -> { drawCircle(art.paper, 5f * u, p); drawCircle(art.circled, 5f * u, p, style = Stroke(1.8f * u)) }
            k == here -> drawCircle(art.pencil, 3.4f * u, p)
            else -> drawCircle(art.pencil.copy(alpha = .75f), (if (k == 0) 2.4f else 1.9f) * u, p)
        }
        label(k, at(k, r + 12f * u))
    }
}

/** The board's clock (ArtStyle.Tabula): a dark disk with a soft light, chalk dots and chords, a dashed goal. */
private fun DrawScope.chalkClock(
    center: Offset, r: Float, n: Int, art: dev.hamper.tabula.ui.Art, jumps: List<Int>, mark: Int?, target: Int?, end: Int?, good: Int?, fade: Boolean,
    label: (DrawScope.(Int, Offset) -> Unit)?,
) {
    val u = density
    val big = r > 50f * u
    val rr = r * .78f
    fun at(k: Int): Offset { val a = -PI / 2 + k * 2 * PI / n; return Offset(center.x + rr * cos(a).toFloat(), center.y + rr * sin(a).toFloat()) }
    drawCircle(Brush.radialGradient(listOf(art.diskHi, art.disk), Offset(center.x - r * .32f, center.y - r * .44f), r * 1.6f), r, center)
    drawCircle(art.chalk.copy(alpha = .18f), rr, center, style = Stroke(1.2f * u))
    for (i in 0 until jumps.size - 1) {
        val a = at(jumps[i]); val b = at(jumps[i + 1])
        if (a == b) continue
        val dx = b.x - a.x; val dy = b.y - a.y; val l = hypot(dx, dy); val ux = dx / l; val uy = dy / l
        val sh = (if (big) 9f else 7f) * u; val ah = (if (big) 9f else 7f) * u; val aw = (if (big) 5f else 4f) * u
        val ax = b.x - ux * sh; val ay = b.y - uy * sh; val bx = ax - ux * ah; val by = ay - uy * ah
        val last = i == jumps.size - 2
        val sw = (if (r >= 60f * u) 3.6f else if (big) 3f else 2.4f) * u
        val op = if (last) 1f else if (fade) .16f else (.34f + .4f * (i + 1) / (jumps.size - 1))
        val col = if (last) art.glow else art.chalk
        if (last) drawLine(art.glow.copy(alpha = .28f), Offset(a.x + ux * sh, a.y + uy * sh), Offset(ax, ay), sw * 2.8f, StrokeCap.Round)
        drawLine(col.copy(alpha = op), Offset(a.x + ux * sh, a.y + uy * sh), Offset(bx, by), sw, StrokeCap.Round)
        drawPath(Path().apply { moveTo(ax, ay); lineTo(bx - uy * aw, by + ux * aw); lineTo(bx + uy * aw, by - ux * aw); close() }, col.copy(alpha = op))
    }
    for (k in 0 until n) {
        val p = at(k)
        if (k == target && k != good) {
            drawCircle(art.chalk.copy(alpha = .14f), (if (big) 10f else 8.5f) * u, p)
            drawCircle(art.chalk, (if (big) 10f else 8.5f) * u, p, style = Stroke(2f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.4f * u, 2.4f * u))))
        }
        if (k == good || k == mark) drawCircle(art.glow.copy(alpha = .35f), (if (big) 11f else 9f) * u, p)
        var rad = (if (big) 3f else 2.5f) * u; var col = art.chalk.copy(alpha = .8f)
        if (k == 0) { rad += .9f * u; col = art.chalk }
        if (k == mark) { rad = (if (big) 6.4f else 5f) * u; col = art.glow }
        if (k == good || k == end) { rad = (if (big) 6.4f else 5.4f) * u; col = if (k == good) art.glow else art.chalk }
        drawCircle(col, rad, p)
        if (label != null) {
            val a = -PI / 2 + k * 2 * PI / n
            label(k, Offset(center.x + (r + 13f * u) * cos(a).toFloat(), center.y + (r + 13f * u) * sin(a).toFloat()))
        }
    }
}

/* ---- the hero card of Dnes -------------------------------------------------------------------------------------- */

/**
 * The hero card's picture: the lesson's topic (congruences: the clock, induction: dominoes, …). In the notebook a
 * pencil sketch on squared paper, nothing else on the page (a pencil lying there said nothing: judges, 18. 11.); on
 * the board the board rising in rings. A clock
 * shows only the start of a walk (0 → 3 → 6, the goal 1), never the answer of the current step.
 */
@Composable
fun HeroArt(l: LessonDef, modifier: Modifier = Modifier) = TopicArt(motifFor("lesson:${l.id}"), modifier)

@Composable
fun TopicArt(m: Motif, modifier: Modifier = Modifier) {
    val c = T.c
    val tm = rememberTextMeasurer()
    val digits = TextStyle(color = if (ArtStyleNow == ArtStyle.Zosit) c.art.pencil else c.art.label, fontFamily = TexDigits, fontSize = 13.sp)
    Canvas(modifier.clipToBounds().graphicsLayer().clearAndSetSemantics {}) {
        val u = density
        fun num(s: String, at: Offset, col: Color = digits.color, size: Float = 13f, turn: Float = 0f) {
            val t = tm.measure(s, digits.copy(color = col, fontSize = size.sp))
            rotate(turn, at) { drawText(t, topLeft = Offset(at.x - t.size.width / 2f, at.y - t.size.height / 2f)) }
        }
        when (ArtStyleNow) {
            ArtStyle.Zosit -> {
                drawRect(c.art.paper)
                val g = 13f * u
                var x = (size.width * .42f) % g; while (x < size.width) { drawLine(c.art.grid, Offset(x, 0f), Offset(x, size.height), 1f * u / 1.4f); x += g }
                var y = (size.height * .5f) % g; while (y < size.height) { drawLine(c.art.grid, Offset(0f, y), Offset(size.width, y), 1f * u / 1.4f); y += g }
                drawLine(c.art.margin, Offset(size.width - 34f * u, 0f), Offset(size.width - 34f * u, size.height), 1.2f * u)
                val cx = size.width * .38f; val cy = size.height * .5f
                if (m == Motif.Clock) {
                    // a low band (design review 18. 11.: the tall one pushed Pokračovať and "Dnes ešte" down): the clock as
                    // large as the band lets its numbers stay whole
                    val r = size.height * .28f; val cy = size.height * .51f
                    pencilClock(Offset(cx, cy), r, 7, c.art, listOf(0, 3, 6), 6, 1, null, null) { k, at ->
                        // upright, every digit the same: a tilted "1" read as italic among the others (inspector, 18. 11.)
                        num("$k", at, if (k == 1) c.art.circled else c.art.pencil, if (k == 1) 15f else 13f)
                    }
                    // the margin note: +3, +3, +3?
                    listOf("+3", "+3", "+3 ?").forEachIndexed { i, s -> num(s, Offset(cx + r + 50f * u + i * 6f * u, cy - r * .55f + i * 20f * u), c.art.pencil.copy(alpha = if (i == 2) .5f else .8f), 14f, -6f) }
                } else {
                    val s = size.height * .82f
                    in48(Offset(cx - s / 2f, cy - s / 2f), s) { PencilPen(this, c.art, wk = 48f * 1.15f * u / s, amp = .3f).motif(m) }
                }
            }
            ArtStyle.Tabula -> {
                drawRect(c.lesson.tint)
                val r = 64.dp.toPx(); val cx = size.width * .6f; val cy = size.height * .53f
                listOf(196, 140, 94, 34).forEachIndexed { i, dd -> drawCircle(listOf(c.art.r1, c.art.r2, c.art.r3, c.art.r4)[i], r + dd.dp.toPx(), Offset(cx, cy)) }
                if (m == Motif.Clock) chalkClock(Offset(cx, cy), r, 7, c.art, listOf(0, 3, 6), 6, 1, null, null, false) { k, at -> num("$k", at) }
                else {
                    drawCircle(Brush.radialGradient(listOf(c.art.diskHi, c.art.disk), Offset(cx - r * .32f, cy - r * .44f), r * 1.6f), r, Offset(cx, cy))
                    val s = r * 1.45f
                    in48(Offset(cx - s / 2f, cy - s / 2f), s) { TilePen(this, Ink(c.art.chalk.copy(alpha = .92f), c.art.chalk.copy(alpha = .3f), c.art.disk, c.art.glow)).motif(m) }
                }
            }
        }
    }
}

/**
 * The hero card's sketch beside the lesson's name: a scrap of the squared paper the rows of "Dnes ešte" stand on, a
 * size larger (judges, 18. 11., round 3: a band over the card took the height Pokračovať needs, and its right two
 * thirds were empty paper; text left and the picture right keeps the card low and the picture's character). A clock
 * shows the start of a walk (0 → 3 → 6, the goal 1), [done]: the walk reaches the goal. Its digits keep their size at
 * any font (the card drops the sketch from 150 %).
 */
@Composable
fun LessonSketch(m: Motif, modifier: Modifier = Modifier, size: Dp = 96.dp, done: Boolean = false) {
    val c = T.c
    if (ArtStyleNow != ArtStyle.Zosit) { MotifTile(m, modifier, size); return }
    val tm = rememberTextMeasurer()
    val d = androidx.compose.ui.platform.LocalDensity.current
    val digits = TextStyle(color = c.art.pencil, fontFamily = TexDigits, fontSize = with(d) { 11.dp.toSp() })
    Canvas(modifier.size(size).graphicsLayer().clearAndSetSemantics {}) {
        val u = density
        val w = this.size.width
        val r = CornerRadius(16f * u)
        drawRoundRect(c.art.paper, cornerRadius = r)
        clipPath(Path().apply { addRoundRect(RoundRect(0f, 0f, w, this@Canvas.size.height, r)) }) {
            val g = 9f * u
            var x = g * .5f; while (x < w) { drawLine(c.art.grid, Offset(x, 0f), Offset(x, this.size.height), u / 1.4f); x += g }
            var y = g * .5f; while (y < this.size.height) { drawLine(c.art.grid, Offset(0f, y), Offset(w, y), u / 1.4f); y += g }
        }
        drawRoundRect(c.line, cornerRadius = r, style = Stroke(u))
        val mid = Offset(w / 2f, this.size.height / 2f)
        if (m == Motif.Clock) {
            // the numbers sit 11 dp past the ring, all inside the scrap; the ring as large as that lets it be
            val rr = w / 2f - 22f * u
            sketchClock(mid, rr, 7, c.art, if (done) listOf(5, 1) else listOf(0, 3, 6), goal = 1, reached = done) { k, at ->
                val t = tm.measure("$k", digits.copy(color = if (k == 1) c.art.circled else c.art.pencil))
                drawText(t, topLeft = Offset(at.x - t.size.width / 2f, at.y - t.size.height / 2f))
            }
        } else {
            val s = w * .74f
            in48(Offset(mid.x - s / 2f, mid.y - s / 2f), s) { PencilPen(this, c.art, wk = 48f * 1.1f * u / s, amp = .3f).motif(m) }
            // done: a green tick in the corner, as a teacher marks a page
            if (done) {
                val tx = w - 30f * u; val ty = this.size.height - 22f * u
                val tick = Path().apply { moveTo(tx, ty); lineTo(tx + 6f * u, ty + 7f * u); lineTo(tx + 19f * u, ty - 10f * u) }
                drawPath(wobble(tick, .4f * u, 31, 2f * u), c.art.circled, style = Stroke(2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

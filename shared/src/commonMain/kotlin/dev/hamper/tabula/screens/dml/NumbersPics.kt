package dev.hamper.tabula.screens.dml

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.screens.clockFace
import dev.hamper.tabula.screens.ArtStyle
import dev.hamper.tabula.screens.ArtStyleNow
import dev.hamper.tabula.ui.Bubble
import dev.hamper.tabula.ui.Choice
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.card
import dev.hamper.tabula.ui.rememberHaptics
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import dev.hamper.tabula.compat.JMath

/*
 * The interactive pictures of the number theory lessons (data/dml/Numbers.kt). Each has a goal the student reaches by
 * tapping 48 dp buttons; the formula under the picture says what the picture shows, so it reads without the drawing.
 */

private fun md(a: Int, m: Int) = ((a % m) + m) % m
private fun neg(n: Int) = if (n < 0) "($n)" else "$n"

private fun DrawScope.label(tm: TextMeasurer, s: String, at: Offset, style: TextStyle) {
    val l = tm.measure(s, style)
    drawText(l, topLeft = Offset(at.x - l.size.width / 2f, at.y - l.size.height / 2f))
}

@Composable
private fun Frame(modifier: Modifier, say: String, picture: DrawScope.(TextMeasurer, TextStyle) -> Unit, controls: @Composable () -> Unit) {
    val tm = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelMedium.copy(color = T.c.ink2, fontSize = 13.sp)
    Column(modifier) {
        Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = 150.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp)).padding(12.dp)) {
            Canvas(Modifier.fillMaxSize()) { picture(tm, style) }
        }
        Spacer(Modifier.height(8.dp))
        Bubble(say.replace('§', '$'), Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        controls()
    }
}

@Composable
private fun Buttons(vararg b: Pair<String, () -> Unit>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for ((t, f) in b) Choice(t.replace('§', '$'), f, Modifier.weight(1f), center = true, minHeight = 48.dp)
    }
}

/* ---- Division with remainder on a number line (Věta 6.2) ------------------------------------------------------- */

/** a = q·d + r: jump by d from 0 until the rest r lands in {0, ..., d-1}. Works for negative a too. */
data class DivisionPic(val a: Int, val d: Int) : Pic.Custom {
    override val describe get() = "Číselná os. Skoky dĺžky $d od nuly k číslu $a, zvyšok je vzdialenosť od posledného skoku."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c; val hx = rememberHaptics()
        var q by remember(this) { mutableIntStateOf(0) }
        val r = a - q * d
        val ok = r in 0 until d
        LaunchedEffect(ok) { if (ok) { hx.right(); onGoal() } }
        val lo = min(min(0, a), q * d) - d; val hi = max(max(0, a), q * d) + d
        Frame(modifier, "§$a=${neg(q)}\\cdot $d+${neg(r)}§. " + (if (ok) "Zvyšok §$r§ leží v §\\{0,\\dots,${d - 1}\\}§: §q=$q§, §r=$r§." else "Zvyšok musí byť z §\\{0,\\dots,${d - 1}\\}§; §${neg(r)}§ nie je."), { tm, st ->
            val y = size.height * .55f
            fun X(v: Int) = size.width * (v - lo) / (hi - lo).toFloat()
            drawLine(c.axis, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
            var k = JMath.floorDiv(lo, d)
            while (k * d <= hi) { drawCircle(c.dot, 4.dp.toPx(), Offset(X(k * d), y)); k++ }
            // the q jumps from 0
            val step = if (q >= 0) 1 else -1
            for (i in 0 until abs(q)) {
                val x0 = X(i * step * d); val x1 = X((i + 1) * step * d)
                drawArc(c.u, 180f, 180f, false, Offset(min(x0, x1), y - abs(x1 - x0) / 2f), Size(abs(x1 - x0), abs(x1 - x0)), style = Stroke(2.5.dp.toPx()))
            }
            drawLine(if (ok) c.good else c.bad, Offset(X(q * d), y + 10.dp.toPx()), Offset(X(a), y + 10.dp.toPx()), 5.dp.toPx())
            drawCircle(c.ink, 5.dp.toPx(), Offset(X(0), y)); label(tm, "0", Offset(X(0), y + 26.dp.toPx()), st)
            drawCircle(c.v, 7.dp.toPx(), Offset(X(a), y)); label(tm, "$a", Offset(X(a), y - 22.dp.toPx()), st.copy(color = c.vText))
            if (q != 0) label(tm, "${q * d}", Offset(X(q * d), y + 26.dp.toPx()), st)
        }) { Buttons("O skok späť" to { hx.tap(); q-- }, "O skok ďalej" to { hx.tap(); q++ }) }
    }
}

/* ---- Euclid's algorithm as cutting squares off a rectangle (Věta 6.3) ------------------------------------------ */

/** An a×b rectangle; each tap cuts the biggest squares: one division a = q·b + r. The last square's side is gcd(a, b). */
data class EuclidPic(val a: Int, val b: Int) : Pic.Custom {
    override val describe get() = "Obdĺžnik $a krát $b. Každé ťuknutie odreže čo najviac štvorcov, to je jedno delenie so zvyškom."
    private data class Sq(val x: Int, val y: Int, val s: Int, val step: Int)
    private val plan: Pair<List<Sq>, List<String>> by lazy {
        val sq = ArrayList<Sq>(); val lines = ArrayList<String>()
        var x = 0; var y = 0; var w = a; var h = b; var step = 0
        while (w > 0 && h > 0) {
            if (w >= h) { val q = w / h; repeat(q) { sq += Sq(x + it * h, y, h, step) }; lines += "$w=$q\\cdot $h+${w % h}"; x += q * h; w %= h }
            else { val q = h / w; repeat(q) { sq += Sq(x, y + it * w, w, step) }; lines += "$h=$q\\cdot $w+${h % w}"; y += q * w; h %= w }
            step++
        }
        sq to lines
    }
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c; val hx = rememberHaptics()
        var k by remember(this) { mutableIntStateOf(0) }
        val (sq, lines) = plan
        val done = k >= lines.size
        val g = sq.last().s
        LaunchedEffect(done) { if (done) { hx.right(); onGoal() } }
        val say = if (k == 0) "Obdĺžnik §$a\\times $b§. Odrež čo najviac štvorcov so stranou kratšej strany." else
            "§" + lines.take(k).last() + "§. " + (if (done) "Zvyšok §0§: posledné štvorce majú stranu §\\gcd($a,$b)=$g§." else "Ostal obdĺžnik zo zvyšku, pokračuj.")
        Frame(modifier, say, { _, _ ->
            val sc = min(size.width / a, size.height / b); val ox = (size.width - a * sc) / 2f; val oy = (size.height - b * sc) / 2f
            drawRect(c.sand, Offset(ox, oy), Size(a * sc, b * sc))
            for (s in sq) if (s.step < k) {
                val col = if (s.step == lines.size - 1) c.good else if (s.step % 2 == 0) c.lesson.tint else c.gem.tint
                drawRect(col, Offset(ox + s.x * sc, oy + s.y * sc), Size(s.s * sc, s.s * sc))
                drawRect(c.edge, Offset(ox + s.x * sc, oy + s.y * sc), Size(s.s * sc, s.s * sc), style = Stroke(1.dp.toPx()))
            }
            drawRect(c.ink2, Offset(ox, oy), Size(a * sc, b * sc), style = Stroke(2.dp.toPx()))
        }) { Buttons((if (done) "Znova" else "Odrež štvorce") to { hx.tap(); k = if (done) 0 else k + 1 }) }
    }
}

/* ---- Coins: which amounts a·x + b·y can be paid (Věta 6.7) -------------------------------------------------------- */

/** Coins worth a and b, negative counts are change given back; reach the amount c. */
data class CoinsPic(val a: Int, val b: Int, val c: Int) : Pic.Custom {
    override val describe get() = "Mince v hodnote $a a $b. Záporný počet znamená mince, ktoré dostaneš späť. Cieľ: suma $c."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val p = T.c; val hx = rememberHaptics()
        var x by remember(this) { mutableIntStateOf(0) }
        var y by remember(this) { mutableIntStateOf(0) }
        val v = a * x + b * y
        val ok = v == c
        LaunchedEffect(ok) { if (ok) { hx.right(); onGoal() } }
        val span = max(abs(c) * 2, 4 * max(a, b))
        Frame(modifier, "§$a\\cdot ${neg(x)}+$b\\cdot ${neg(y)}=$v§" + (if (ok) ". Suma §$c§ zaplatená." else ", cieľ §$c§."), { tm, st ->
            val yy = size.height * .5f
            fun X(t: Int) = size.width * (t + span / 2f) / span
            drawLine(p.axis, Offset(0f, yy), Offset(size.width, yy), 2.dp.toPx())
            drawCircle(p.ink, 4.dp.toPx(), Offset(X(0), yy)); label(tm, "0", Offset(X(0), yy + 22.dp.toPx()), st)
            drawCircle(p.v, 8.dp.toPx(), Offset(X(c), yy), style = Stroke(3.dp.toPx())); label(tm, "$c", Offset(X(c), yy - 22.dp.toPx()), st.copy(color = p.vText))
            val vv = v.coerceIn(-span / 2, span / 2)
            drawLine(if (ok) p.good else p.u, Offset(X(0), yy), Offset(X(vv), yy), 5.dp.toPx())
            drawCircle(if (ok) p.good else p.u, 6.dp.toPx(), Offset(X(vv), yy))
        }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Buttons("§-$a§" to { hx.tap(); x-- }, "§+$a§" to { hx.tap(); x++ })
                Buttons("§-$b§" to { hx.tap(); y-- }, "§+$b§" to { hx.tap(); y++ })
            }
        }
    }
}

/* ---- Z_m as a clock: multiples or powers of a (Věty 6.12, 6.15, 6.17) ------------------------------------------- */

enum class ClockMode { Multiples, Powers }

/** k·a or a^k modulo m walked on a clock of m dots; the goal is the dot [target]. */
data class ClockPic(val m: Int, val a: Int, val mode: ClockMode, val target: Int) : Pic.Custom {
    override val describe get() = "Hodiny so zvyškami 0 až ${m - 1}. " + (if (mode == ClockMode.Multiples) "Násobky čísla $a modulo $m." else "Mocniny čísla $a modulo $m.") + " Cieľ: $target."
    private fun value(k: Int): Int = if (mode == ClockMode.Multiples) md(a * k, m) else { var v = 1; repeat(k) { v = md(v * a, m) }; v }
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c; val hx = rememberHaptics()
        var k by remember(this) { mutableIntStateOf(0) }
        val v = value(k)
        val ok = k > 0 && v == target
        LaunchedEffect(ok) { if (ok) { hx.right(); onGoal() } }
        val formula = if (mode == ClockMode.Multiples) "$a\\cdot $k=${a * k}\\equiv $v\\pmod{$m}" else "$a^{$k}\\equiv $v\\pmod{$m}"
        Frame(modifier, "§$formula§" + (if (ok) ". Cieľ §$target§ dosiahnutý pre §k=$k§." else "."), { tm, st ->
            // the board's clock (Art.kt): the walk so far as chords, the last one glowing, the target a dashed ring
            // the clock fills the card: what is left around it is the labels' room, not an empty frame (design review 18. 11.)
            // The numbers grow with the font, the clock does not: at a large font they stand further out and the
            // clock shrinks to leave them room, so no number sits on a ring or touches the card (inspector, 18. 11.,
            // 200 %). At 100 % nothing of this applies: the picture is the one the judges chose.
            val digits = st.copy(fontFamily = dev.hamper.tabula.ui.TexDigits, fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
            val half = tm.measure("${m - 1}", digits).size.let { max(it.width, it.height) } / 2f
            val grow = if (fontScale > 1.05f) half * (1f - 1f / fontScale) else 0f
            val labelAt = 20.dp.toPx() + grow
            val side = min(size.width, size.height)
            val rad = min(min(side * .39f, 140.dp.toPx()), if (grow > 0f) side / 2f - labelAt - half - 2.dp.toPx() else Float.MAX_VALUE)
            val ctr = Offset(size.width / 2f, size.height / 2f)
            // on the board a light halo under the numbers, so the disk sits in the card instead of filling it
            if (ArtStyleNow == ArtStyle.Tabula) drawCircle(c.lesson.tint, rad + 30.dp.toPx(), ctr)
            val seen = (0..k).map(::value)
            // where you stand in the accent, the goal in the pictures' second colour (v, as the goal of the coins), the
            // other places quiet: "here" and "there" are two things at a glance
            clockFace(ctr, rad, m, c.art, seen, mark = if (ok) null else v, target = target, good = if (ok) v else null, goal = c.v, labelAt = labelAt) { i, at ->
                val col = if (ArtStyleNow != ArtStyle.Zosit) c.art.label else when (i) { target -> if (ok) c.lesson.text else c.vText; v -> c.lesson.text; else -> c.ink2 }
                label(tm, "$i", at, digits.copy(color = col))
            }
        }) { Buttons("§k-1§" to { hx.tap(); if (k > 0) k-- }, "§k+1§" to { hx.tap(); k++ }) }
    }
}

/* ---- Chinese remainder theorem: x in a table of (x mod m1, x mod m2) (Věta 6.19) -------------------------------- */

/** Every x in 0..m1·m2-1 sits in the cell (x mod m1, x mod m2); find the one in the cell (r1, r2). */
data class CrtPic(val m1: Int, val m2: Int, val r1: Int, val r2: Int) : Pic.Custom {
    override val describe get() = "Tabuľka: riadok je zvyšok modulo $m1, stĺpec zvyšok modulo $m2. Hľadáš číslo v riadku $r1 a stĺpci $r2."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c; val hx = rememberHaptics()
        var x by remember(this) { mutableIntStateOf(0) }
        var far by remember(this) { mutableIntStateOf(0) }
        val ok = md(x, m1) == r1 && md(x, m2) == r2
        LaunchedEffect(ok) { if (ok) { hx.right(); onGoal() } }
        val mm = m1 * m2
        Frame(modifier, "§x=$x§: §x\\bmod $m1=${md(x, m1)}§, §x\\bmod $m2=${md(x, m2)}§" + (if (ok) ". To je bunka §($r1,$r2)§." else ". Cieľ: bunka §($r1,$r2)§."), { tm, st ->
            val cw = size.width / (m2 + 1); val ch = size.height / (m1 + 1); val s = min(cw, ch)
            val ox = (size.width - s * (m2 + 1)) / 2f; val oy = (size.height - s * (m1 + 1)) / 2f
            fun cell(i: Int, j: Int) = Offset(ox + (j + 1.5f) * s, oy + (i + 1.5f) * s)
            for (j in 0 until m2) label(tm, "$j", Offset(ox + (j + 1.5f) * s, oy + .5f * s), st)
            for (i in 0 until m1) label(tm, "$i", Offset(ox + .5f * s, oy + (i + 1.5f) * s), st)
            drawRect(c.v.copy(alpha = .25f), cell(r1, r2) - Offset(s / 2, s / 2), Size(s, s))
            for (t in 0..max(x, far)) if (t < mm) {
                val q = cell(md(t, m1), md(t, m2))
                label(tm, "$t", q, st.copy(color = if (t == x) (if (ok) c.good else c.u) else c.ink3, fontSize = if (t == x) 15.sp else 12.sp))
            }
            drawRect(if (ok) c.good else c.u, cell(md(x, m1), md(x, m2)) - Offset(s / 2, s / 2), Size(s, s), style = Stroke(2.5.dp.toPx()))
        }) {
            Buttons("§x+1§" to { hx.tap(); x = (x + 1) % mm; far = max(far, x) }, "§x+$m1§" to { hx.tap(); x = (x + m1) % mm; far = max(far, x) }, "§x=0§" to { hx.tap(); x = 0 })
        }
    }
}


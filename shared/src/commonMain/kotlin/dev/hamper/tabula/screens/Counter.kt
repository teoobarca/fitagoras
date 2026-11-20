package dev.hamper.tabula.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.gcd
import dev.hamper.tabula.la.q
import dev.hamper.tabula.la.rank as rankOf
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.*

private val Slots = listOf("x", "y", "z")

/** Claim that sounds right: build what breaks it by tapping points. The check is real. */
@Composable
fun CounterScreen(onClose: () -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.mem) {
        val c = T.c; val h = T.hue
        val hx = rememberHaptics()
        val face = LocalMathFace.current; val inks = LocalMathInks.current
        val pts = remember { mutableStateListOf<Pair<Int, Int>?>(null, null, null) }
        var active by remember { mutableIntStateOf(0) }
        val full = pts.all { it != null }
        val rows = pts.filterNotNull().map { intArrayOf(it.first, it.second) }
        // Three vectors in the plane are always dependent, so the only question is z.
        val zOut = full && rank(rows) > rank(rows.take(2))
        LaunchedEffect(zOut) { if (zOut) hx.right() }
        val colors = listOf(c.u, c.v, h.main.let { if (c.dark) c.mem.main else c.bad })
        StepFrame(0, 0, onClose, closeLabel = "Zavrieť protipríklad", footerTint = if (zOut) c.goodTint else null, footer = {
            if (zOut) {
                val x = rows[0]; val y = rows[1]
                val relation = when {
                    x.all { it == 0 } -> "$1\\mathbf{x}+0\\mathbf{y}+0\\mathbf{z}=\\theta$"
                    y.all { it == 0 } -> "$0\\mathbf{x}+1\\mathbf{y}+0\\mathbf{z}=\\theta$"
                    else -> { val p = if (x[0] != 0) y[0] else y[1]; val q = if (x[0] != 0) x[0] else x[1]; val g = gcd(p.toLong(), q.toLong()).toInt().coerceAtLeast(1); "$" + comboTex(listOf(p / g to "\\mathbf{x}", -q / g to "\\mathbf{y}")) + "=\\theta$" }
                }
                Verdict("Tvrdenie padlo.", "Súbor je LZ, lebo $relation, ale $\\mathbf{z}\\notin\\langle\\mathbf{x},\\mathbf{y}\\rangle$. Závislý je iný vektor než $\\mathbf{z}$.", true)
                Cta("Hotovo", onClose)
            } else {
                Status(full, rows)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Slots.forEachIndexed { i, s ->
                        val p = pts[i]
                        Choice(
                            if (p == null) "$\\mathbf{$s}$" else "$\\mathbf{$s}=(${p.first},${p.second})$", { active = i }, Modifier.weight(1f), center = true,
                            state = if (active == i) Pick.Picked else Pick.Idle, minHeight = 56.dp, style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }) {
            Label("Protipríklad")
            Spacer(Modifier.height(4.dp))
            Rich("Ak je $(\\mathbf{x},\\mathbf{y},\\mathbf{z})$ LZ, tak $\\mathbf{z}$ je kombináciou $\\mathbf{x},\\mathbf{y}$.", style = MaterialTheme.typography.headlineMedium, color = c.ink)
            Spacer(Modifier.height(4.dp))
            Sub("Neplatí to. Vyber vektor dole a ťukni do mriežky, kým tvrdenie nepadne.")
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f).card(radius = 24.dp).clip(RoundedCornerShape(24.dp))) {
                val v = viewOf(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), -3.5f, 3.5f, -3.5f, 3.5f)
                Canvas(Modifier.fillMaxSize()) {
                    planeGrid(v, c)
                    if (full && !zOut) {
                        val d = rows.firstOrNull { it.any { a -> a != 0 } }
                        if (d != null) spanBand(v, d[0].toFloat(), d[1].toFloat(), c.mem, c.mem.main, alpha = .6f)
                    } else if (rows.size >= 2 && pts[0] != null && pts[1] != null && rank(rows.take(2)) == 1) {
                        val d = rows.take(2).first { it.any { a -> a != 0 } }
                        spanBand(v, d[0].toFloat(), d[1].toFloat(), c.mem, c.mem.main)
                    }
                    pts.forEachIndexed { i, p ->
                        if (p != null) {
                            arrow(v.o, v.at(p.first.toFloat(), p.second.toFloat()), colors[i], 4.dp.toPx())
                            val at = v.at(p.first.toFloat(), p.second.toFloat())
                            drawMath(Slots[i], 16.sp.toPx(), at + Offset(9.dp.toPx(), -9.dp.toPx()), colors[i], face, inks)
                        }
                    }
                    drawCircle(c.ink2, 3.5.dp.toPx(), v.o)
                }
                val d = LocalDensity.current
                for (x in -3..3) for (y in -3..3) {
                    val at = v.at(x.toFloat(), y.toFloat())
                    val cell = with(d) { (v.unit * .96f).toDp() }.coerceAtMost(48.dp)
                    with(d) {
                        Box(
                            Modifier.offset(at.x.toDp() - cell / 2, at.y.toDp() - cell / 2).size(cell).clip(CircleShape)
                                .clickable(role = Role.Button) {
                                    hx.choose()
                                    pts[active] = x to y
                                    val next = (1..3).map { (active + it) % 3 }.firstOrNull { pts[it] == null }
                                    if (next != null) active = next
                                }
                                .semantics { contentDescription = "Bod $x, $y" },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Status(full: Boolean, rows: List<IntArray>) {
    val c = T.c
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Check("Súbor je LZ", full, Modifier.weight(1f))
        Check("$\\mathbf{z}$ je mimo obalu", false, Modifier.weight(1f))
    }
    if (full) Rich("Skús dať $\\mathbf{x}$ a $\\mathbf{y}$ na jednu priamku.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
}

@Composable
private fun Check(t: String, ok: Boolean, modifier: Modifier) {
    val c = T.c
    Row(modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(18.dp).clip(CircleShape).background(if (ok) c.good else c.line), contentAlignment = Alignment.Center) { if (ok) Icon24(Res.drawable.ic_check, tint = c.card, modifier = Modifier.size(13.dp)) }
        Spacer(Modifier.width(6.dp))
        Rich(t, style = MaterialTheme.typography.bodySmall, color = if (ok) c.ink else c.ink2)
    }
}

private fun rank(rows: List<IntArray>): Int = Field.R.rankOf(rows.map { r -> r.map { it.q() } })

/** "2x - y" with the ones and zeros a person would drop. */
private fun comboTex(terms: List<Pair<Int, String>>): String {
    val b = StringBuilder()
    for ((k, name) in terms) {
        if (k == 0) continue
        val sign = if (k < 0) "-" else if (b.isEmpty()) "" else "+"
        val m = kotlin.math.abs(k)
        b.append(sign).append(if (m == 1) "" else "$m").append(name)
    }
    return b.ifEmpty { "0" }.toString()
}

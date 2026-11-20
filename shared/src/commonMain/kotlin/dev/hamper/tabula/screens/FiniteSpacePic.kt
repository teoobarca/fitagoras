package dev.hamper.tabula.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import dev.hamper.tabula.la.FiniteSpace
import dev.hamper.tabula.la.Subspace
import dev.hamper.tabula.la.ZVec
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.math.MathEngine
import kotlin.math.atan2
import kotlin.math.min
import dev.hamper.tabula.compat.*

/*
 * Pic.Grid with n = 3, p = 2: Z_2^3 as the eight labelled corners of a cube. Only the corners are vectors; the edges
 * and the shading are a drawing aid. Tap corners: one gives the line {θ, v}, two give the plane {θ, a, b, a+b}
 * (the "slanted" ones too, not only faces). With Goal.Subspaces every subspace of the step's dimension is to be found
 * once; another basis of a subspace already found is named as the same subspace. Everything lit comes from the model
 * (la/FiniteSpace.kt).
 */

/** Distinct colours of found subspaces, from the theme, so both palettes work. */
/** Right and wrong keep their meaning (Palette): found subspaces never take c.good or c.bad. */
fun huntColors(c: Palette): List<Color> = listOf(c.u, c.v, c.mem.main, c.gem.main, c.target, c.paper.main, c.ink3)

/** Where a corner (x, y, z) sits: x to the right, z up, y into the page. */
private fun corner(v: ZVec, o: Offset, s: Float): Offset = Offset(o.x + v[0] * s + v[1] * s * .45f, o.y - v[2] * s - v[1] * s * .32f)

@Composable
internal fun CubePic(p: Pic.Grid, modifier: Modifier, onGoal: () -> Unit) {
    require(p.p == 2) { "the cube shows Z_2^3" }
    val c = T.c
    val hx = rememberHaptics()
    val space = remember { FiniteSpace(2, 3) }
    val hunt = p.goal == Goal.Subspaces
    val dim = p.dim.coerceIn(1, 2)
    val all = remember(dim) { space.subspaceCount(dim).toInt() }
    val picked = remember(p) { mutableStateListOf<ZVec>() }
    val found = remember(p) { mutableStateListOf<Subspace>() }
    var current by remember(p) { mutableStateOf<Subspace?>(null) }
    var repeat by remember(p) { mutableStateOf(false) }
    // generating sets tried so far: the same set again is not "another basis"
    val tried = remember(p) { mutableStateListOf<Set<ZVec>>() }
    var sameBasis by remember(p) { mutableStateOf(false) }
    var taps by remember(p) { mutableIntStateOf(0) }
    var note by remember(p) { mutableStateOf<String?>(null) }
    val goal = if (hunt) found.size == all else taps >= 2
    LaunchedEffect(goal) { if (goal) { if (hunt) hx.right(); onGoal() } }
    val colors = huntColors(c)
    val lit = current?.elements ?: emptySet()

    fun tapCorner(v: ZVec) {
        hx.choose()
        if (v == space.zero) { note = "§\\theta§ je v každom podpriestore, ale do bázy nepatrí: z neho sa nedostaneš nikam."; return }
        taps++
        note = null
        if (v in picked) { picked.remove(v); current = null; return }
        if (picked.size >= dim) picked.clear()
        picked.add(v)
        if (picked.size < dim) { current = null; repeat = false }
        if (picked.size == dim) {
            val s = space.span(picked.toList())
            val set = picked.toSet()
            sameBasis = set in tried
            if (!sameBasis) tried.add(set)
            current = s
            if (s in found) repeat = true else { repeat = false; if (hunt) found.add(s) }
            if (dim == 2) picked.clear()
        }
    }

    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).heightIn(min = 280.dp).card(radius = 24.dp).clip(RoundedCornerShape(24.dp)).padding(12.dp)) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            val d = LocalDensity.current
            val face = LocalMathFace.current; val inks = LocalMathInks.current
            // beside the cube: a corner's label, 22 dp out and as wide as the math font sets "111" (it may use 8 of the 12 dp padding)
            val side = with(d) { 22.dp.toPx() + MathEngine.layout("111", 14.sp.toPx(), face, inks).w - 8.dp.toPx() }
            val s = minOf(w / 1.75f, (w - 2 * side) / 1.45f, h / 1.62f)
            val o = Offset((w - s * 1.45f) / 2f, h - (h - s * 1.32f) / 2f)
            val corners = remember { space.all() }
            val say = if (hunt) "Kocka Z2 na tretiu. Nájdených ${found.size} zo $all." else "Kocka Z2 na tretiu, osem rohov."
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = say }) {
                // the cube's edges: a drawing aid between corners that differ in one component
                for (a in corners) for (b in corners) if (space.index(a) < space.index(b) && (0 until 3).count { a[it] != b[it] } == 1)
                    drawLine(c.axis, corner(a, o, s), corner(b, o, s), 1.5.dp.toPx())
                // the subspace on screen: its corners joined around their centre, shaded (an aid, not more vectors)
                current?.let { sub ->
                    val col = if (hunt) colors[(found.indexOf(sub).takeIf { it >= 0 } ?: found.size) % colors.size] else c.lesson.main
                    val pts = sub.elements.map { corner(it, o, s) }
                    if (pts.size == 2) drawLine(col, pts[0], pts[1], 5.dp.toPx())
                    else {
                        val cx = pts.map { it.x }.average().toFloat(); val cy = pts.map { it.y }.average().toFloat()
                        val ring = pts.sortedBy { atan2(it.y - cy, it.x - cx) }
                        val path = Path().apply { moveTo(ring[0].x, ring[0].y); ring.drop(1).forEach { lineTo(it.x, it.y) }; close() }
                        drawPath(path, col.copy(alpha = .18f))
                        drawPath(path, col, style = Stroke(2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))))
                    }
                }
                for (v in corners) {
                    val q = corner(v, o, s)
                    val on = v in lit
                    if (on) drawCircle(c.lesson.tint, 17.dp.toPx(), q)
                    if (v in picked) drawCircle(c.u, 15.dp.toPx(), q, style = Stroke(3.dp.toPx()))
                    drawCircle(if (on) c.lesson.main else c.ink, if (v == space.zero) 7.dp.toPx() else 6.dp.toPx(), q)
                    val ink = if (on) c.ink else c.ink2
                    // θ's label goes under its corner (the longest one, at the left edge); the others beside theirs
                    // a label may use 8 dp of the card's 12 dp padding, so it stays clear of its corner's ring and of the card's edge
                    if (v == space.zero) drawMath("000=\\theta", 14.sp.toPx(), Offset(q.x - 8.dp.toPx(), q.y + 30.dp.toPx()), ink, face, inks, bleed = 8.dp.toPx())
                    else drawMath(space.label(v), 14.sp.toPx(), Offset(q.x + if (v[0] == 1) 22.dp.toPx() else -40.dp.toPx(), q.y - 10.dp.toPx()), ink, face, inks, bleed = 8.dp.toPx())
                }
            }
            // a 48 dp target on every corner
            val walk = remember(dim, hunt) { if (hunt) emptyList() else listOf(space.vec(1, 0, 0), space.vec(1, 1, 1)) }
            for (v in corners) {
                val q = corner(v, o, s)
                with(d) {
                    Box(
                        Modifier.offset(q.x.toDp() - 24.dp, q.y.toDp() - 24.dp).size(48.dp).clip(CircleShape)
                            .clickable(role = Role.Checkbox) { tapCorner(v) }
                            .semantics { contentDescription = "Roh ${space.label(v)}" + (if (v in walk) dbg("in")?.let { " $it" } ?: "" else ""); selected = v in picked },
                    )
                }
            }
            // debug builds: the lesson walker cannot pick pairs of corners; it uses this switch and logs it
            if (dev.hamper.tabula.Env.DEBUG && hunt) Box(Modifier.align(Alignment.TopEnd).size(4.dp).clickable { found.clear(); found.addAll(space.subspaces(dim)) }.semantics { contentDescription = "dbg:goal" })
        }
        Spacer(Modifier.height(8.dp))
        val what = if (dim == 1) "priamka" else "rovina"
        val text = when {
            note != null -> note!!
            current == null -> if (dim == 1) "Ťukni na nenulový roh §\\mathbf{v}§: priamka §\\langle\\mathbf{v}\\rangle=\\{\\theta,\\mathbf{v}\\}§ má len dva rohy." else "Ťukni na dva rôzne nenulové rohy §\\mathbf{a},\\mathbf{b}§: rozsvieti sa §\\langle\\mathbf{a},\\mathbf{b}\\rangle=\\{\\theta,\\mathbf{a},\\mathbf{b},\\mathbf{a}+\\mathbf{b}\\}§."
            else -> {
                val set = current!!.elements.sortedBy { space.index(it) }.joinToString(", ") { space.label(it) }
                val head = if (repeat) "${if (dim == 1) "Túto priamku" else "Túto rovinu"} už máš: " + (if (sameBasis) "tie isté rohy, ten istý podpriestor." else "iná báza, ten istý podpriestor.") else if (hunt) "Nová $what." else ""
                "$head §\\{\\text{$set}\\}§." + if (hunt) " **${found.size} z $all.**" else ""
            }
        }
        Bubble(text.trim().replace('§', '$'), Modifier.fillMaxWidth())
    }
}

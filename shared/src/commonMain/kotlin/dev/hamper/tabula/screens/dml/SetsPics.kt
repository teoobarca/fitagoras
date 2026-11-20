@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.hamper.tabula.screens.dml

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.data.dml.Arrows
import dev.hamper.tabula.data.dml.Concrete
import dev.hamper.tabula.data.dml.identity
import dev.hamper.tabula.data.dml.setTex
import dev.hamper.tabula.data.dml.holdsOn
import dev.hamper.tabula.data.dml.eval
import dev.hamper.tabula.data.dml.tex
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.ui.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import dev.hamper.tabula.res.*

/*
 * Pictures of Množiny a zobrazenia (Pic.Custom, docs/CONTENT.md). Both decide with the same code the generators and
 * tests use (data/dml/SetsLogic.kt, SetsMaps.kt): what the student builds is judged exactly, never by a picture.
 */

/** What tapping elements into sets should reach: break the identity (a counterexample) or just look around. */
enum class SetGoal { Break, Explore }

/**
 * Tap the elements of a small universe into the sets; both sides of the identity are computed live. [identity] in the
 * syntax of SetsLogic ("(C - B) | (C - A) = C - (A | B)"). Goal Break: the two sides differ. Explore: six taps.
 */
data class SetsBuild(val identity: String, val universe: List<String> = listOf("1", "2", "3"), val goal: SetGoal = SetGoal.Break) : Pic.Custom {
    override val describe: String get() = "Množiny ako riadky prvkov univerza; ťuknutím pridáš alebo uberieš prvok. Pod nimi obe strany vzťahu."

    @Composable
    override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c
        val hx = rememberHaptics()
        val id = remember(identity) { identity(identity) }
        val vars = id.vars
        val sets = remember(identity) { mutableStateMapOf<String, Set<String>>().apply { vars.forEach { put(it, emptySet()) } } }
        var taps by remember(identity) { mutableIntStateOf(0) }
        val conc = Concrete(universe, vars.associateWith { sets[it] ?: emptySet() })
        val holds = id.holdsOn(conc)
        val reached = if (goal == SetGoal.Break) !holds else taps >= 6
        LaunchedEffect(reached) { if (reached) { if (goal == SetGoal.Break) hx.right(); onGoal() } }
        Column(modifier.fillMaxWidth().card(radius = 24.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Rich("$\\mathcal{U}=" + setTex(universe.toSet(), universe) + "$", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            for (v in vars) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Rich("$$v$", Modifier.width(36.dp), style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (e in universe) {
                            val on = e in (sets[v] ?: emptySet())
                            Box(
                                Modifier.size(48.dp).clip(CircleShape)
                                    .background(if (on) T.hue.main else c.card)
                                    .border(2.dp, if (on) T.hue.main else c.edge, CircleShape)
                                    .clickable(role = Role.Checkbox) { hx.choose(); taps++; sets[v] = if (on) sets[v]!! - e else sets[v]!! + e }
                                    .semantics { contentDescription = "$e v množine $v"; selected = on },
                                contentAlignment = Alignment.Center,
                            ) { Text(e, color = if (on) c.card else c.ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            val l = id.lhs.eval(conc).tex(universe); val r = id.rhs.eval(conc).tex(universe)
            Rich("$" + id.lhs.tex() + "=" + l + "$", style = MaterialTheme.typography.bodyLarge)
            Rich("$" + id.rhs.tex() + "=" + r + "$", style = MaterialTheme.typography.bodyLarge)
            Bubble(
                if (holds) "Vzťah §${id.rel.tex}§ tu platí.".replace('§', '$') else "Vzťah §${id.rel.tex}§ tu neplatí: protipríklad.".replace('§', '$'),
                fill = if (holds) c.card else c.goodTint, line = if (holds) c.line else c.good,
            )
        }
    }
}

/** What the arrows should become. Impossible goals (Pozorování 5.2) are explored: done after |A| arrows. */
enum class MapWant(val say: String) {
    Map("zobrazenie"), Total("totálne zobrazenie"), Injective("injektívne"), Surjective("surjektívne"), Bijective("bijekcia"),
    InjNotSurj("injektívne, nie surjektívne"), SurjNotInj("surjektívne, nie injektívne"), Explore("skúšaj"),
}

private fun Arrows.meets(w: MapWant): Boolean = when (w) {
    MapWant.Map -> isMap && arrows.isNotEmpty()
    MapWant.Total -> isTotal
    MapWant.Injective -> injective
    MapWant.Surjective -> surjective
    MapWant.Bijective -> bijective
    MapWant.InjNotSurj -> injective && !surjective
    MapWant.SurjNotInj -> surjective && !injective
    MapWant.Explore -> arrows.size >= a
}

/**
 * Draw arrows from A (left) to B (right): tap an element of A, then one of B; tapping the same pair again removes
 * the arrow. The verdicts (a map? total? injective? surjective?) are computed live from Definice 5.7 and 5.9.
 */
data class ArrowsDraw(val left: List<String>, val right: List<String>, val want: MapWant, val start: Set<Pair<Int, Int>> = emptySet()) : Pic.Custom {
    override val describe: String get() = "Dve množiny, vľavo ${left.joinToString(", ")}, vpravo ${right.joinToString(", ")}. Ťukni na prvok vľavo a potom vpravo, pribudne šípka."

    @Composable
    override fun Draw(modifier: Modifier, onGoal: () -> Unit) {
        val c = T.c
        val hx = rememberHaptics()
        val arrows = remember(this) { mutableStateListOf<Pair<Int, Int>>().apply { addAll(start) } }
        var from by remember(this) { mutableStateOf<Int?>(null) }
        val st = Arrows(left.size, right.size, arrows.toSet())
        val reached = st.meets(want)
        LaunchedEffect(reached) { if (reached) { if (want != MapWant.Explore) hx.right(); onGoal() } }
        val rows = maxOf(left.size, right.size)
        val gap = 64.dp
        Column(modifier.fillMaxWidth().card(radius = 24.dp).padding(16.dp)) {
            Row { Text("A", Modifier.weight(1f), color = c.ink2, style = MaterialTheme.typography.titleMedium); Text("B", color = c.ink2, style = MaterialTheme.typography.titleMedium) }
            BoxWithConstraints(Modifier.fillMaxWidth().height(gap * rows)) {
                val d = LocalDensity.current
                val wPx = constraints.maxWidth.toFloat()
                val gapPx = with(d) { gap.toPx() }
                val r = with(d) { 24.dp.toPx() }
                fun at(side: Int, i: Int, n: Int): Offset {
                    val top = (rows - n) * gapPx / 2
                    return Offset(if (side == 0) r else wPx - r, top + gapPx * i + gapPx / 2)
                }
                val ink = c.ink; val sel = T.hue.main; val bad = c.bad
                Canvas(Modifier.fillMaxSize()) {
                    for ((x, y) in arrows) {
                        val p = at(0, x, left.size); val q = at(1, y, right.size)
                        val dup = arrows.count { it.first == x } > 1
                        val col = if (dup) bad else ink
                        val ang = atan2(q.y - p.y, q.x - p.x)
                        val s = p + Offset(cos(ang) * r, sin(ang) * r); val e = q - Offset(cos(ang) * (r + 4), sin(ang) * (r + 4))
                        drawLine(col, s, e, 3.dp.toPx(), StrokeCap.Round)
                        val h = 11.dp.toPx()
                        drawLine(col, e, e - Offset(cos(ang - .45f) * h, sin(ang - .45f) * h), 3.dp.toPx(), StrokeCap.Round)
                        drawLine(col, e, e - Offset(cos(ang + .45f) * h, sin(ang + .45f) * h), 3.dp.toPx(), StrokeCap.Round)
                    }
                    from?.let { drawCircle(sel, r + 5.dp.toPx(), at(0, it, left.size)) }
                }
                for ((side, names) in listOf(0 to left, 1 to right)) for ((i, n) in names.withIndex()) {
                    val o = at(side, i, names.size)
                    val picked = side == 0 && from == i
                    with(d) {
                        Box(
                            Modifier.offset((o.x - r).toDp(), (o.y - r).toDp()).size(48.dp).clip(CircleShape)
                                .background(if (picked) sel else c.card).border(2.dp, if (picked) sel else c.edge, CircleShape)
                                .clickable(role = Role.Button) {
                                    hx.choose()
                                    if (side == 0) from = if (from == i) null else i
                                    else from?.let { f -> if ((f to i) in arrows) arrows.remove(f to i) else arrows.add(f to i); from = null }
                                }
                                .semantics { contentDescription = (if (side == 0) "Prvok $n z A" else "Prvok $n z B") + (if (side == 0) ", šípky do " + arrows.filter { it.first == i }.joinToString { right[it.second] } else ""); selected = picked },
                            contentAlignment = Alignment.Center,
                        ) { Text(n, color = if (picked) c.card else c.ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag("zobrazenie", st.isMap)
                Tag("totálne", st.isTotal)
                Tag("injektívne", if (st.isTotal) st.injective else null)
                Tag("surjektívne", if (st.isTotal) st.surjective else null)
            }
            if (want != MapWant.Explore) { Spacer(Modifier.height(8.dp)); Text("Cieľ: ${want.say}", color = c.ink2, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

/** A verdict chip: yes, no, or not decided (null, while the arrows are not a total map). */
@Composable
private fun Tag(text: String, on: Boolean?) {
    val c = T.c
    val fill = when (on) { true -> c.goodTint; false -> c.badTint; null -> c.sand }
    val ink = when (on) { true -> c.good; false -> c.bad; null -> c.ink3 }
    Box(Modifier.background(fill, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 6.dp).semantics { contentDescription = text + when (on) { true -> " áno"; false -> " nie"; null -> " zatiaľ nerozhodnuté" } }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon24(when (on) { true -> Res.drawable.ic_check; false -> Res.drawable.ic_close; null -> Res.drawable.ic_dash }, tint = ink, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = ink, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

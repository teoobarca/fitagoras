package dev.hamper.tabula.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.LocalMathFace
import dev.hamper.tabula.math.LocalMathInks
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.Tex
import dev.hamper.tabula.ui.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random
import dev.hamper.tabula.la.Combine
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.la.echelon

/** One row operation of a real elimination: R_target := R_target - k R_src in Z_p. */
data class GemOp(val src: Int, val target: Int, val k: Int)

/** A matrix in Z_p and the row operations GEM does on it. */
data class GemDrill(val p: Int, val start: List<List<Int>>, val ops: List<GemOp>, val source: String?)

/** The first step of 21. 11. 2025, příklad 2. */
val GemReal = GemDrill(5, listOf(listOf(1, 2, 0, 4), listOf(3, 3, 0, 1)), listOf(GemOp(0, 1, 3)), "Test 21. 11. 2025, príklad 2")

/** A fresh 3x4 matrix in Z_5 whose elimination needs no swap; the operations come from the app's own GEM. */
fun gemDrill(r: Random): GemDrill {
    val p = 5
    val f = Field.Zp(p)
    while (true) {
        val rows = List(3) { List(4) { r.nextInt(0, p) } }
        if (rows.any { row -> row.all { it == 0 } } || rows[0][0] == 0) continue
        val e = f.echelon(Mat(rows.map { row -> row.map { Frac.of(it) } }))
        val ops = e.steps.flatMap { it.ops }
        if (ops.any { it !is Combine } || ops.size < 2) continue
        return GemDrill(p, rows, ops.map { val o = it as Combine; GemOp(o.src, o.target, (-o.b.num.toInt()).mod(p)) }, null)
    }
}

/** GEM in Z_p with the subtraction drawn: the real first step, then whole eliminations with new numbers. */
@Composable
fun GemScreen(onClose: () -> Unit, onPaper: () -> Unit, onMistake: (Boolean) -> Unit = {}) {
    val hue = T.c.gem
    CompositionLocalProvider(LocalHue provides hue) {
        val c = T.c
        val hx = rememberHaptics()
        var drill by remember { mutableStateOf(GemReal) }
        val rows = remember(drill) { mutableStateListOf<List<Int>>().apply { addAll(drill.start) } }
        var opIx by remember(drill) { mutableIntStateOf(0) }
        var col by remember(drill, opIx) { mutableIntStateOf(0) }
        val entered = remember(drill, opIx) { mutableStateListOf<Int>() }
        var wrong by remember(drill, opIx) { mutableStateOf<Int?>(null) }
        var shakeN by remember { mutableIntStateOf(0) }
        val shake = remember { Animatable(0f) }
        val p = drill.p
        val n = drill.start[0].size
        val allDone = opIx >= drill.ops.size
        val op = drill.ops.getOrNull(opIx)
        val rowDone = op != null && col >= n
        val still = calm()
        LaunchedEffect(shakeN) { if (shakeN > 0 && !still) shake.animateTo(0f, keyframes { durationMillis = 320; -7f at 60; 6f at 130; -4f at 200; 2f at 260 }) }
        LaunchedEffect(rowDone) { if (rowDone) hx.right() }
        val target = op?.let { o -> (0 until n).map { j -> (rows[o.target][j] - o.k * rows[o.src][j]).mod(p) } }
        // Progress counts entries, not operations: one operation with four numbers is not done after the first.
        val cells = drill.ops.size * n
        // The keypad and its prompt sit right under the matrix, so the cell, its working and the keys are one glance;
        // the footer only carries the verdict and the way on, in the place every step has it.
        StepFrame(minOf(opIx * n + col, cells), cells, onClose, closeLabel = "Zavrieť GEM", scroll = true, footerTint = if (rowDone || allDone) c.goodTint else null, footer = when {
            allDone -> { {
                Verdict("Hotovo.", if (drill.source != null) "Takto začína aj riešenie na papieri." else "Horný stupňovitý tvar: pod každým schodom sú nuly.", true)
                ButtonRow {
                    Second("Nové čísla", { drill = gemDrill(Random.Default) })
                    Cta("Na papier", onPaper)
                }
            } }
            rowDone -> { {
                Verdict("Nový riadok: $(${target!!.joinToString(",")})$", null, true)
                Cta(if (opIx + 1 < drill.ops.size) "Ďalšia úprava" else "Hotovo", { rows[op!!.target] = target; opIx++ })
            } }
            else -> null
        }) {
            Spacer(Modifier.height(2.dp))
            H1(if (allDone) "Horný stupňovitý tvar." else "Uprav ${op!!.target + 1}. riadok.")
            Spacer(Modifier.height(2.dp))
            Sub((drill.source ?: "Nové čísla, celá eliminácia") + ". Počítame v $\\mathbb{Z}_$p$.")
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().panel(22.dp).padding(horizontal = 10.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEachIndexed { ri, row ->
                    val isTarget = op != null && ri == op.target
                    val isSrc = op != null && ri == op.src
                    Column {
                        Row(if (isTarget) Modifier.graphicsLayer { translationX = shake.value * density } else Modifier) {
                            GemRow("\\text{ř}${ri + 1}", row.size) { i ->
                                when {
                                    isTarget && i < entered.size -> Cell("${entered[i]}", Modifier.weight(1f), CellKind.Done, wave = if (rowDone) i else -1)
                                    isTarget && i == col -> Cell(wrong?.toString() ?: "", Modifier.weight(1f), if (wrong != null) CellKind.Wrong else CellKind.Active)
                                    isSrc && i == col -> Cell("${row[i]}", Modifier.weight(1f), CellKind.Hot)
                                    else -> Cell("${row[i]}", Modifier.weight(1f), CellKind.Plain)
                                }
                            }
                        }
                        // the subtraction hangs right under the cell being written; the result is the student's to find,
                        // shown only after a miss. Once the row is done its last working stays, so the panel keeps its height.
                        if (isTarget) GemRow(null, row.size) { i ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                                if (i == col) Work(row[i], op!!.k, rows[op.src][i], reveal = wrong != null)
                                else if (rowDone && i == n - 1) Work(row[i], op!!.k, rows[op.src][i], reveal = true)
                            }
                        }
                    }
                }
                if (op != null) Tex("\\text{(G3)}\\ " + dev.hamper.tabula.la.Combine(op.target, dev.hamper.tabula.la.Frac.ONE, op.src, dev.hamper.tabula.la.Frac.of(-op.k.toLong())).tex(), Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp), size = 18.sp, color = c.ink, say = "riadok ${op.target + 1} mínus ${op.k} krát riadok ${op.src + 1}")
            }
            // a finished row keeps its keys under the rising panel, so nothing empties out before the verdict arrives
            if (op != null) {
                val o = op
                val ci0 = col.coerceAtMost(n - 1)
                val ord = listOf("Prvé", "Druhé", "Tretie", "Štvrté", "Piate")[ci0]
                // two lines are kept for the prompt, so a longer hint after a miss does not push the keys away from the thumb
                val two = with(LocalDensity.current) { (MaterialTheme.typography.bodyMedium.lineHeight * 2).toDp() } + 24.dp
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().heightIn(min = two), contentAlignment = Alignment.BottomStart) {
                    AnimatedContent(Triple(ci0, wrong, opIx), label = "hint", transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(80)) using SizeTransform(clip = false) }) { (ci, w, _) ->
                        val rw = rows[o.target][ci] - o.k * rows[o.src][ci]
                        val t = when {
                            w == null -> "**$ord číslo:** dopočítaj rozdiel pod políčkom a zapíš ho modulo $p."
                            rw in 0 until p -> "$$w$ nie je ono. Rozdiel pod políčkom už je v $\\mathbb{Z}_$p$, stačí ho prepísať."
                            else -> "$$w$ nie je ono. Pod políčkom je rozdiel; pripočítavaj $$p$, kým nie si medzi $0$ a $${p - 1}$."
                        }
                        Box(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) { Bubble(t, Modifier.fillMaxWidth()) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                NumberPad((0 until p).map { "$it" }, { k ->
                    if (rowDone) return@NumberPad
                    val v = k.toInt()
                    if (v == target!![col]) { hx.cell(); entered.add(v); col++; wrong = null; onMistake(false) }
                    else { hx.slip(); wrong = v; shakeN++; onMistake(true) }
                })
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

enum class CellKind { Plain, Hot, Active, Done, Wrong }

@Composable
private fun GemRow(label: String?, n: Int, cell: @Composable RowScope.(Int) -> Unit) {
    val c = T.c
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) Tex(label, Modifier.width(30.dp), size = 16.sp, color = c.ink2) else Spacer(Modifier.width(30.dp))
        for (i in 0 until n) cell(i)
    }
}

@Composable
private fun Cell(t: String, modifier: Modifier, kind: CellKind, wave: Int = -1) {
    val c = T.c; val h = T.hue
    // a right number lands (from 60 %, with a bounce); a finished row answers with a wave, cell after cell
    val still = calm()
    val s = remember { Animatable(if (kind == CellKind.Done && !still) .6f else 1f) }
    LaunchedEffect(Unit) { if (s.value < 1f) s.animateTo(1f, Motion.pop()) }
    LaunchedEffect(wave) { if (wave >= 0 && !still) { delay(wave * 45L); s.animateTo(1.08f, tween(90)); s.animateTo(1f, Motion.pop()) } }
    val fill by animateColorAsState(when (kind) { CellKind.Hot -> h.tint; CellKind.Done -> c.goodTint; CellKind.Wrong -> c.badTint; CellKind.Active -> c.card; else -> c.bg }, tween(160), label = "cell")
    val line = when (kind) { CellKind.Active -> h.main; CellKind.Wrong -> c.bad; CellKind.Done -> c.good; CellKind.Hot -> h.tint; else -> c.line }
    Box(
        modifier.aspectRatio(1.18f).graphicsLayer { scaleX = s.value; scaleY = s.value }.clip(RoundedCornerShape(14.dp)).background(fill).border(if (kind == CellKind.Active || kind == CellKind.Wrong) 2.5.dp else 1.5.dp, line, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (t.isNotEmpty()) Text(t, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium, fontSize = 26.sp), color = when (kind) { CellKind.Hot -> h.text; CellKind.Done -> c.good; CellKind.Wrong -> c.bad; else -> c.ink })
        else Box(Modifier.size(2.5.dp, 26.dp).background(h.main))
    }
}

/** The subtraction written as on paper, under the cell: the old entry, minus k times the entry above, and what is left once [reveal]ed. */
@Composable
private fun Work(a: Int, k: Int, b: Int, reveal: Boolean) {
    val c = T.c; val h = T.hue
    val raw = a - k * b
    Box(
        Modifier.padding(horizontal = 6.dp).fillMaxWidth().clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)).background(h.tint2).padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Odčítanie: $a mínus $k krát $b" + if (reveal) " je $raw." else ", výsledok dopočítaj." },
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.width(IntrinsicSize.Max), horizontalAlignment = Alignment.End) {
            Tex("$a", size = 18.sp, color = c.ink)
            Tex("-$k\\cdot $b", size = 18.sp, color = h.text)
            Box(Modifier.padding(vertical = 3.dp).fillMaxWidth().height(1.5.dp).background(c.ink2))
            Tex(if (reveal) "$raw" else "?", size = 18.sp, color = if (reveal) c.ink else h.text)
        }
    }
}

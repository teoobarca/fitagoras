package dev.hamper.tabula.screens.dml

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.delay

/*
 * Interactive pictures of Matematická indukcia (data/dml/Induction.kt). Text comes in with § as the math delimiter.
 */

private fun s(t: String) = t.replace('§', '$')

/**
 * Dominoes V(1)..V(count): tap the one you check in the base step, switch the induction step on. Only both together
 * make every domino from the base on fall. Goal: base at [start] and the step on. [stepFrom]: the step holds only
 * for n ≥ stepFrom (n² ≤ 2ⁿ: from 4). [falseAt]: statements that are false, a base there cannot be checked.
 */
data class MiDominoes(val start: Int = 1, val count: Int = 8, val stepFrom: Int = 1, val falseAt: Set<Int> = emptySet()) : Pic.Custom {
    override val describe = "Kocky §V(1)§ až §V($count)§. Vyber kocku pre základný krok a zapni indukčný krok.".replace("§", "")
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) = DominoesView(this, modifier, onGoal)
}

@Composable
private fun DominoesView(p: MiDominoes, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    var base by remember(p) { mutableStateOf<Int?>(null) }
    var step by remember(p) { mutableStateOf(false) }
    val b = base
    val lit: List<Int> = when {
        b == null || b in p.falseAt -> emptyList()
        step && b >= p.stepFrom -> (b..p.count).toList()
        else -> listOf(b)
    }
    var shown by remember(p) { mutableIntStateOf(0) }
    LaunchedEffect(b, step) { shown = 0; for (k in 1..lit.size) { delay(150); shown = k } }
    val done = b == p.start && step
    LaunchedEffect(done) { if (done) { hx.right(); onGoal() } }
    val say = when {
        b == null && !step -> "Ťukni na kocku, pre ktorú overíš základný krok."
        b == null -> "Krok platí, ale nespadne nič. Chýba základný krok."
        b in p.falseAt -> "§V($b)§ neplatí, tu základný krok neoveríš."
        !step -> "Platí len §V($b)§. Bez indukčného kroku ďalej nič."
        b < p.stepFrom -> "Krok platí až od §n=${p.stepFrom}§, z §V($b)§ sa ďalej nedostaneš."
        b == p.start -> "Spadnú všetky od §V(${p.start})§ ďalej."
        b > p.start -> "Padajú až od §V($b)§. Tie pred ňou ostali neoverené."
        else -> "Spadnú všetky od §V($b)§ ďalej."
    }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().card(radius = 24.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..p.count).chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { k ->
                        val on = k in lit.take(shown)
                        val fill by animateColorAsState(when { on -> c.goodTint; k in p.falseAt && b == k -> c.badTint; else -> c.card }, tween(160), label = "d")
                        val line = when { k == b -> T.hue.main; on -> c.good; else -> c.line }
                        PressBox({ hx.choose(); base = if (base == k) null else k }, Modifier.weight(1f).heightIn(min = 56.dp), fill, c.edge, line, radius = 14.dp, depth = 3.dp,
                            say = "V($k)" + (if (on) ", platí" else "") + (if (k == b) ", základný krok" else "")) {
                            Rich(s("§V($k)§"), Modifier.padding(vertical = 10.dp), style = MaterialTheme.typography.titleMedium, color = c.ink)
                        }
                    }
                }
            }
            Text("…", Modifier.align(Alignment.CenterHorizontally), color = c.ink2, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        CheckRow(s(if (p.stepFrom == 1) "Indukčný krok: §V(n)\\Rightarrow V(n+1)§ pre každé §n\\geq 1§" else "Indukčný krok: §V(n)\\Rightarrow V(n+1)§ pre každé §n\\geq ${p.stepFrom}§"), step, { step = it })
        Spacer(Modifier.height(10.dp))
        Bubble(s(say), Modifier.fillMaxWidth())
    }
}

/** A proof written out line by line; tap the line with the mistake. Goal: the bad line found. */
data class MiFlaw(val lines: List<String>, val bad: Int) : Pic.Custom {
    override val describe = "Dôkaz po riadkoch. Ťukni na riadok s chybou."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) = FlawView(this, modifier, onGoal)
}

@Composable
private fun FlawView(p: MiFlaw, modifier: Modifier, onGoal: () -> Unit) {
    val tried = remember(p) { mutableStateListOf<Int>() }
    val found = p.bad in tried
    LaunchedEffect(found) { if (found) onGoal() }
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        p.lines.forEachIndexed { i, l ->
            val st = when { i !in tried -> Pick.Idle; i == p.bad -> Pick.Right; else -> Pick.Wrong }
            Choice(s(l), { if (!found && i !in tried) tried += i }, Modifier.fillMaxWidth(), state = st, style = MaterialTheme.typography.bodyLarge, minHeight = 52.dp)
        }
        if (tried.isNotEmpty() && !found) Bubble("Tento riadok je v poriadku. Hľadaj ďalej.", Modifier.fillMaxWidth())
    }
}

/** Amounts 1..[max] paid with coins [a] and [b]: tap an amount to see a way to pay it, or that there is none. */
data class MiCoins(val a: Int = 3, val b: Int = 5, val max: Int = 20) : Pic.Custom {
    override val describe = "Sumy 1 až $max. Ťukni na sumu a uvidíš, ako ju zaplatiť mincami $a a $b."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) = CoinsView(this, modifier, onGoal)

    /** How many of [a] and of [b], fewest coins of [a] first; null when the amount cannot be paid. */
    fun pay(n: Int): Pair<Int, Int>? = (0..n / a).firstNotNullOfOrNull { x -> if ((n - a * x) % b == 0) x to (n - a * x) / b else null }
}

@Composable
private fun CoinsView(p: MiCoins, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val tapped = remember(p) { mutableStateListOf<Int>() }
    LaunchedEffect(tapped.size) { if (tapped.size >= 4) onGoal() }
    val last = tapped.lastOrNull()
    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().card(radius = 24.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..p.max).chunked(5).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { n ->
                        val known = n in tapped
                        val ok = p.pay(n) != null
                        val fill = when { !known -> c.card; ok -> c.goodTint; else -> c.badTint }
                        PressBox({ hx.choose(); if (n !in tapped) tapped += n else { tapped.remove(n); tapped += n } }, Modifier.weight(1f).heightIn(min = 52.dp), fill, c.edge, if (n == last) T.hue.main else c.line,
                            radius = 12.dp, depth = 3.dp, say = "$n korún" + if (known) (if (ok) ", dá sa" else ", nedá sa") else "") {
                            Text("$n", Modifier.padding(vertical = 10.dp), style = MaterialTheme.typography.titleMedium, color = c.ink)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val say = when {
            last == null -> "Ťukni na niekoľko súm, aj malých, aj veľkých."
            else -> p.pay(last)?.let { (x, y) ->
                val parts = List(x) { "${p.a}" } + List(y) { "${p.b}" }
                val back = if (last - p.a >= 8 && p.pay(last - p.a) != null) " Je to suma §${last - p.a}§ plus jedna minca §${p.a}§." else ""
                "§$last=${parts.joinToString("+")}§.$back"
            } ?: "§$last§ sa mincami §${p.a}§ a §${p.b}§ zaplatiť nedá."
        }
        Bubble(s(say), Modifier.fillMaxWidth())
    }
}

/** Powers of a matrix: step n up and down, see Aⁿ computed exactly as Aⁿ⁻¹·A. Goal: n reached 4. */
data class MiPowers(val a: List<List<Long>>, val upTo: Int = 6) : Pic.Custom {
    override val describe = "Mocniny matice A. Tlačidlami plus a mínus meníš n a vidíš A na n."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) = PowersView(this, modifier, onGoal)

    fun power(n: Int): List<List<Long>> {
        var r = a
        repeat(n - 1) { r = r.indices.map { i -> a[0].indices.map { j -> a.indices.fold(0L) { s, k -> s + r[i][k] * a[k][j] } } } }
        return r
    }
}

private fun pm(m: List<List<Long>>) = "\\begin{pmatrix}" + m.joinToString("\\\\") { it.joinToString("&") } + "\\end{pmatrix}"

@Composable
private fun PowersView(p: MiPowers, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    var n by remember(p) { mutableIntStateOf(1) }
    var top by remember(p) { mutableIntStateOf(1) }
    LaunchedEffect(top) { if (top >= 4) onGoal() }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().card(radius = 24.dp).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TexBlock("A^{$n}=" + pm(p.power(n)), size = 22.sp, color = c.ink, say = "A na $n")
            Spacer(Modifier.height(8.dp))
            if (n > 1) TexBlock("=A^{${n - 1}}\\cdot A=" + pm(p.power(n - 1)) + "\\cdot " + pm(p.a), size = 16.sp, color = c.ink2)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PressBox({ hx.tap(); if (n > 1) n-- }, Modifier.weight(1f).heightIn(min = 56.dp), c.card, c.edge, c.line, enabled = n > 1, say = "Menšie n") {
                Text("−", style = MaterialTheme.typography.headlineMedium, color = c.ink)
            }
            Rich(s("§n=$n§"), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), color = c.ink)
            PressBox({ hx.tap(); if (n < p.upTo) { n++; if (n > top) top = n } }, Modifier.weight(1f).heightIn(min = 56.dp), c.card, c.edge, c.line, enabled = n < p.upTo, say = "Väčšie n") {
                Text("+", style = MaterialTheme.typography.headlineMedium, color = c.ink)
            }
        }
        Spacer(Modifier.height(10.dp))
        Bubble(if (top < 4) "Prejdi aspoň po §n=4§ a hľadaj, ktoré prvky sa menia.".let(::s) else "Ktoré prvky sú stále rovnaké a ako rastú ostatné?", Modifier.fillMaxWidth())
    }
}

/** Two sides of an inequality for n = 1, 2, ...: reveal row by row, see where it holds. Goal: all rows revealed. */
data class MiTable(val left: String, val right: String, val rel: String, val rows: List<Triple<Int, String, String>>, val holds: List<Boolean>) : Pic.Custom {
    override val describe = "Tabuľka hodnôt oboch strán nerovnosti pre n od 1."
    @Composable override fun Draw(modifier: Modifier, onGoal: () -> Unit) = TableView(this, modifier, onGoal)
}

@Composable
private fun TableView(p: MiTable, modifier: Modifier, onGoal: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    var shown by remember(p) { mutableIntStateOf(1) }
    LaunchedEffect(shown) { if (shown >= p.rows.size) onGoal() }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().card(radius = 24.dp).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Rich("\$n\$", Modifier.weight(.6f), color = c.ink2)
                Rich(s("§${p.left}§"), Modifier.weight(1f), color = c.ink2)
                Rich(s("§${p.right}§"), Modifier.weight(1f), color = c.ink2)
                Rich(s("§${p.rel}§?"), Modifier.weight(.7f), color = c.ink2)
            }
            p.rows.take(shown).forEachIndexed { i, (n, l, r) ->
                val ok = p.holds[i]
                Row(Modifier.fillMaxWidth().background(if (ok) c.goodTint else c.badTint, RoundedCornerShape(10.dp)).padding(horizontal = 6.dp, vertical = 8.dp)) {
                    Rich(s("§$n§"), Modifier.weight(.6f), color = c.ink)
                    Rich(s("§$l§"), Modifier.weight(1f), color = c.ink)
                    Rich(s("§$r§"), Modifier.weight(1f), color = c.ink)
                    Text(if (ok) "áno" else "nie", Modifier.weight(.7f), color = if (ok) c.good else c.bad, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (shown < p.rows.size) Second("Ďalšie n", { hx.tap(); shown++ })
        else Bubble("Pozri, od ktorého §n§ to platí stále.".let(::s), Modifier.fillMaxWidth())
    }
}

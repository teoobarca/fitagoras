package dev.hamper.tabula

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.*
import dev.hamper.tabula.ui.*

/**
 * Debug only: the widest formulas of all content, in their sentences, inside a card as wide as the mock test's, plus
 * a computed check that no piece of any formula needs shrinking at this width and font size. Screenshots of it are
 * the check for narrow screens and 200 % font; the summary goes to logcat as MATHCHECK.
 */
class MathCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TabulaTheme(Tabula.store(this).theme) { Check() } }
    }
}

private val inline = Regex("""[§$]([^§$]+)[§$]""")

/** Every text the app renders with Rich, and every display formula, from all courses, papers and templates. */
private fun allTexts(): Pair<List<String>, List<String>> {
    val texts = ArrayList<String>(); val display = ArrayList<String>()
    fun line(l: Line) { when (l) { is Para -> texts += l.tex; is Item -> texts += l.tex; is Block -> display += l.tex } }
    fun w(sol: Solution?) { sol?.steps?.forEach { st -> texts += st.say; st.math.forEach { f -> display += f.tex; f.note?.let { texts += it } } } }
    for (c in Courses) {
        for (l in c.lessons) for (s in l.steps) when (s) {
            is Step.Ask -> { texts += s.q; texts += s.options; texts += s.after; s.sub?.let { texts += it }; texts += s.wrong.values }
            is Step.Explore -> { texts += s.title; texts += s.sub; texts += s.doneTitle; s.doneBody?.let { texts += it } }
            is Step.Explain -> { texts += s.title; texts += s.body; s.formal?.forEach(::line); s.more?.let { texts += it } }
            is Step.Worked -> { texts += s.title; texts += s.problem; w(s.solution) }
            is Step.Check -> { texts += s.q; texts += s.options; texts += s.why; s.hint?.let { texts += it }; texts += s.wrong.values; s.sub?.let { texts += it } }
            else -> {}
        }
        for (k in c.concepts) { k.czech.forEach(::line); k.why.forEach { texts += it.part; texts += it.because }; k.judge.forEach { texts += it.tex; texts += it.because; texts += it.wrong }; texts += k.units; texts += k.ask }
        for (t in c.theorems) { t.czech.forEach(::line); texts += t.idea; t.note?.let { texts += it } }
        for (p in c.proofs) { texts += p.task; p.foot?.let { texts += it }; texts += p.given; p.rounds.forEach { r -> texts += r.prompt; r.options.forEach { o -> texts += o.tex; o.why?.let { texts += it } } }; texts += p.end; texts += p.checklist }
        for (paper in c.papers + (1..40).map { c.generatedPaper(it) }) for (p in paper.problems) for (u in p.units) {
            texts += u.text; u.answer?.let { texts += it }; u.checklist.forEach { texts += it.text }; w(u.solution)
        }
    }
    for ((_, g) in Generators) for (seed in 1..12) runCatching {
        val e = g(kotlin.random.Random(seed)); texts += e.prompt; texts += e.hint; e.visual?.let { display += it }; w(e.solution(emptyList()))
    }
    return texts.distinct() to display.distinct()
}

@Composable
private fun Check() {
    val c = T.c
    val d = LocalDensity.current
    val face = LocalMathFace.current; val inks = LocalMathInks.current
    val style = MaterialTheme.typography.bodyLarge
    val px = with(d) { style.fontSize.toPx() }
    val dpx = with(d) { 18.sp.toPx() }
    // the card of the mock test: 20 dp screen margin and 16 dp card padding on each side
    val widthDp = LocalConfiguration.current.screenWidthDp
    val line = with(d) { (widthDp.dp - 72.dp).toPx() }
    val (texts, display) = remember { allTexts() }
    val report = remember(px, line) {
        val formulas = texts.flatMap { t -> inline.findAll(t).map { it.groupValues[1] to t }.toList() }
        val widths = formulas.map { (f, t) -> Triple(f, t, MathEngine.layout(f, px, face, inks, text = true).w) }
        var shrunk = 0; var worst = 1f; var worstTex = ""
        for ((f, _, w) in widths) {
            val ps = if (w < px * 11f && w <= line) listOf(w) else MathEngine.pieces(f, px, face, inks, text = true).map { it.w }
            val k = ps.minOf { fitScale(kotlin.math.ceil(it) + 1f, line - 1f) }
            if (k < 1f) { shrunk++; if (k < worst) { worst = k; worstTex = f } }
        }
        var dShrunk = 0; var dWorst = 1f; var dWorstTex = ""; var dScroll = 0
        val dWidths = display.map { it to MathEngine.layout(it, dpx, face, inks).w }
        val avail = line - with(d) { 24.dp.toPx() }
        for ((tex, _) in dWidths) {
            val ls = displayLines(tex, avail, dpx * 1.5f) { MathEngine.layout(it, dpx, face, inks).w }
            val plan = displayPlan(ls, avail, dpx * 1.5f, { MathEngine.layout(it, dpx, face, inks).w }) { l -> MathEngine.pieces(l, dpx, face, inks).map { it.w } }
            for (r in plan) {
                if (r.scale < 1f) dShrunk++
                if (r.scroll) dScroll++
                if (r.scale < dWorst) { dWorst = r.scale; dWorstTex = tex }
            }
        }
        val msg = "inline ${widths.size} formulas, shrunk $shrunk, smallest ${"%.2f".format(worst)} ($worstTex); display ${display.size}, scaled rows $dShrunk, scrolling rows $dScroll, smallest ${"%.2f".format(dWorst)} ($dWorstTex)"
        Log.i("MATHCHECK", "width $widthDp dp, font ${d.fontScale}: $msg")
        Triple(msg, widths.sortedByDescending { it.third }.distinctBy { it.first }.take(10), dWidths.sortedByDescending { it.second }.take(8))
    }
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("Kontrola vzorcov, ${(d.fontScale * 100).toInt()} %, $widthDp dp", style = MaterialTheme.typography.titleMedium, color = c.ink)
        Text(report.first.take(220), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        Spacer(Modifier.height(10.dp))
        for ((tex, _) in report.third) {
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).panel(16.dp).padding(horizontal = 12.dp, vertical = 10.dp)) { TexBlock(tex, size = 18.sp, color = c.ink) }
        }
        for ((_, t, _) in report.second) {
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).panel(18.dp).padding(horizontal = 16.dp, vertical = 12.dp)) { Rich(t, style = style, color = c.ink) }
        }
        Spacer(Modifier.height(20.dp))
    }
}


package dev.hamper.tabula.screens

import kotlin.math.roundToInt
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import dev.hamper.tabula.compat.SystemClock
import dev.hamper.tabula.compat.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.state.MockResult
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.format
import dev.hamper.tabula.res.*

/** A sheet and a pen: the work happens on the desk, not on the phone. */
@Composable
fun PaperArt(modifier: Modifier) {
    val c = T.c; val h = T.hue
    Canvas(modifier) {
        val w = size.width; val hh = size.height
        val sw = minOf(w * .62f, hh * .74f); val sh = sw * 1.18f
        val tl = Offset((w - sw) / 2 - 12.dp.toPx(), (hh - sh) / 2)
        drawRoundRect(c.bg, tl, Size(sw, sh), CornerRadius(10.dp.toPx()))
        drawRoundRect(c.line, tl, Size(sw, sh), CornerRadius(10.dp.toPx()), style = Stroke(1.5.dp.toPx()))
        val lines = listOf(.74f, .58f, .82f, .46f)
        lines.forEachIndexed { i, f ->
            val y = tl.y + sh * (.2f + i * .17f)
            drawLine(if (i == 3) h.main else c.axis, Offset(tl.x + sw * .12f, y), Offset(tl.x + sw * (.12f + f * .76f), y), 3.dp.toPx(), StrokeCap.Round)
        }
        val tip = Offset(tl.x + sw * (.12f + .46f * .76f) + 8.dp.toPx(), tl.y + sh * .71f)
        drawLine(h.main, tip + Offset(9.dp.toPx(), -11.dp.toPx()), tip + Offset(66.dp.toPx(), -86.dp.toPx()), 11.dp.toPx(), StrokeCap.Round)
        drawLine(c.ink, tip + Offset(1.dp.toPx(), -1.dp.toPx()), tip + Offset(9.dp.toPx(), -11.dp.toPx()), 4.dp.toPx(), StrokeCap.Round)
    }
}

/** A calm ring, nothing that pulses. */
@Composable
fun Clock(seconds: Int, total: Int, modifier: Modifier) {
    val c = T.c; val h = T.hue
    Box(modifier.semantics { contentDescription = "${slovakPlural(seconds / 60, "Zostáva", "Zostávajú", "Zostáva")} ${seconds / 60} ${slovakPlural(seconds / 60, "minúta", "minúty", "minút")}" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(210.dp)) {
            val st = 9.dp.toPx()
            val tl = Offset(st / 2, st / 2); val s = Size(size.width - st, size.height - st)
            drawArc(c.line, 0f, 360f, false, tl, s, style = Stroke(st))
            drawArc(h.main, -90f, 360f * seconds / total, false, tl, s, style = Stroke(st, cap = StrokeCap.Round))
        }
        Text("%d:%02d".format(seconds / 60, seconds % 60), style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 44.sp), color = c.ink)
    }
}

/** The course a paper belongs to: its own papers, or generated ones named "<course>gen-<seed>" (LA1's are "gen-<seed>"). */
fun paperCourse(id: String): Course = Courses.firstOrNull { c -> c.papers.any { it.id == id } || c.generatedById(id) != null } ?: La1

/** The exam a paper is a version of. */
fun paperExam(p: TestPaper): Exam = paperCourse(p.id).let { c -> c.exams.firstOrNull { it.id == p.exam } ?: c.exams.first { it.prepares } }

/** A paper's time: the exam's, and its share of it when problems wait for a lecture. */
fun paperMinutes(id: String): Int {
    val p = paperById(id); val e = paperExam(p); val all = e.minutes ?: 70
    val pts = p.problems.sumOf { it.pts }
    return if (pts >= e.points) all else ((all * pts + e.points / 2) / e.points).coerceAtLeast(5)
}

/** A paper as the app offers it (problems whose topic waits for its lecture left out, see Shown.offered). */
fun paperById(id: String): TestPaper {
    val c = paperCourse(id)
    val full = c.papers.firstOrNull { it.id == id } ?: c.generatedById(id) ?: c.generatedPaper(1)
    return c.shown().offered(full) ?: full
}

/** The note on a paper with problems left out. */
fun missingNote(p: TestPaper): String? {
    p.missing.takeIf { it.isNotEmpty() }?.let { ms ->
        return "Príklad ${ms.joinToString(" a ") { it.n.toString() }} z teórie čísel príde po prednáške o kongruenciách. Body a čas sú za zvyšné príklady."
    }
    val e = paperExam(p); val pts = p.problems.sumOf { it.pts }
    return if (p.problems.isNotEmpty() && pts < e.points) "Len časť: ${pts} z ${e.points} bodov za ${e.acc}, čas podľa bodov." else null
}

/* ---- Parts of a problem ------------------------------------------------------------------------------------- */

/** "2 b". Whether the split is ours says one quiet line under the problem ([estimateNote]), not every part. */
fun partPoints(p: PaperPart) = "${p.pts} b"

/**
 * Where the app split a problem or its points itself, once (Teo, 18. 11.: the sheet has "/12" for the whole problem,
 * no a) to d)): null when every part's points are the paper's own. It never says what the sheet has: a generated
 * paper has none, and some totals are estimates themselves.
 */
fun estimateNote(p: PaperProblem): String? = when {
    p.units.none { it.estimate } -> null
    // the paper's own letters ("a) Definujte …", DML) stay in the text; LA1 sheets ask in one block, the app split it
    p.units.size > 1 && p.units.all { it.text.trimStart().startsWith(it.label) } -> "Body jednotlivých častí sú odhad appky."
    p.units.size > 1 -> "Delenie na časti a ich body sú odhad appky."
    else -> "Body príkladu sú odhad appky."
}

/** What to go through again when a part costs points: the lesson step that teaches it, named by its lesson. */
data class ReviewLink(val route: String, val title: String)

private fun reviewKey(part: PaperPart): String? =
    (part.review ?: part.checklist.firstOrNull { it.fix != null }?.fix)?.let { k -> if (':' !in k && k in Generators) "gen:$k" else k }

fun reviewLink(part: PaperPart, course: Course): ReviewLink? {
    val key = reviewKey(part) ?: return null
    val kind = key.substringBefore(':'); val id = key.substringAfter(':').substringBefore(':')
    if (kind == "lesson") return LessonById[id]?.let { ReviewLink(key, it.name) }
    if (kind == "step") return course.lessons.firstOrNull { l -> l.steps.any { it.id == id } }?.let { ReviewLink(key, it.name) }
    for (l in course.lessons) {
        val step = l.steps.firstOrNull { s ->
            when (kind) {
                "concept" -> s is Step.Def && s.concept == id
                "theorem" -> s is Step.Thm && s.theorem == id
                "proof" -> (s is Step.Prove && s.proof == id) || (s is Step.Thm && TheoremById[s.theorem]?.proof?.id == id)
                "gen" -> s is Step.Practice && s.gen == id
                else -> false
            }
        }
        if (step != null) return ReviewLink("step:${step.id}", l.name)
    }
    val name = when (kind) {
        "concept" -> ConceptById[id]?.name
        "theorem" -> TheoremById[id]?.name
        "proof" -> ProofById[id]?.title
        "gen" -> course.templates.firstOrNull { it.gen == id }?.title ?: Generators[id]?.invoke(kotlin.random.Random(0))?.label
        else -> null
    } ?: return null
    return ReviewLink(key, name)
}

/** The rubric index where each part's lines start in the problem's checklist. */
private fun PaperProblem.offsets(): List<Int> = units.runningFold(0) { a, p -> a + p.checklist.size }

/** One part as it stands on the paper: its letter, its points, the Czech text. */
@Composable
fun PartBlock(part: PaperPart, modifier: Modifier = Modifier) {
    val c = T.c
    Column(modifier.fillMaxWidth().panel(18.dp).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (part.label.isNotEmpty()) Text(part.label, style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.weight(1f))
            Text(partPoints(part), style = MaterialTheme.typography.labelMedium, color = c.ink2)
        }
        Spacer(Modifier.height(4.dp))
        Rich(part.text, style = MaterialTheme.typography.bodyLarge, color = c.ink)
    }
}

/** The whole problem as separate blocks, one per thing it asks, then its own instruction if the paper has one. */
@Composable
fun ProblemBlocks(p: PaperProblem, header: String?) {
    val c = T.c
    for (part in p.units) { PartBlock(part); Spacer(Modifier.height(8.dp)) }
    estimateNote(p)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = c.ink2); Spacer(Modifier.height(6.dp)) }
    if (header != null) { Spacer(Modifier.height(2.dp)); Rich(header, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
}

private data class PartAt(val k: Int, val j: Int)

/** The ticked rubric lines per problem as plain strings, so they survive the screen leaving composition. */
/** Each part's Hodnotenie by its index in the review, as "i:h" (a part with none is not in it). */
private val HodnotenieSaver = androidx.compose.runtime.saveable.listSaver<androidx.compose.runtime.snapshots.SnapshotStateMap<Int, Int>, String>(
    save = { m -> m.map { (k, v) -> "$k:$v" } },
    restore = { l -> mutableStateMapOf<Int, Int>().apply { l.forEach { e -> val (k, v) = e.split(':'); put(k.toInt(), v.toInt()) } } },
)

private val TicksSaver = androidx.compose.runtime.saveable.listSaver<androidx.compose.runtime.snapshots.SnapshotStateList<Set<Int>>, String>(
    save = { l -> l.map { it.joinToString(",") } },
    restore = { l -> mutableStateListOf<Set<Int>>().apply { l.forEach { s -> add(if (s.isEmpty()) emptySet() else s.split(',').map(String::toInt).toSet()) } } },
)

/** The student's own basis per problem (OwnBasis), as "k:n/d,n/d;n/d,..." */
private val OwnedSaver = androidx.compose.runtime.saveable.listSaver<androidx.compose.runtime.snapshots.SnapshotStateMap<Int, List<dev.hamper.tabula.la.Vec>>, String>(
    save = { m -> m.map { (k, vs) -> "$k:" + vs.joinToString(";") { v -> v.joinToString(",") { "${it.num}/${it.den}" } } } },
    restore = { l ->
        mutableStateMapOf<Int, List<dev.hamper.tabula.la.Vec>>().apply {
            for (e in l) {
                val (k, rest) = e.split(":", limit = 2)
                put(k.toInt(), rest.split(";").map { v -> v.split(",").map { x -> val (a, b) = x.split("/"); dev.hamper.tabula.la.Frac.of(a.toLong(), b.toLong()) } })
            }
        }
    },
)

/**
 * Under the model solution of the part with many right answers (OwnBasis): the student may enter their own basis; it
 * is checked (why not, with GEM for a dependent set), and the problem's later parts are then computed in it. Without
 * one the sample basis stays and the box says that another one is right too.
 */
@Composable
private fun OwnBasisBox(own: OwnBasis, current: List<dev.hamper.tabula.la.Vec>?, key: String, onSet: (List<dev.hamper.tabula.la.Vec>?) -> Unit) {
    val c = T.c
    val whole = own.input.quick.firstOrNull()?.first?.startsWith("\\mathbf{e}") == true
    var open by rememberSaveable(key) { mutableStateOf(false) }
    val st = remember(key, own) { VectorsState(own.input) }
    var gem by remember(key) { mutableStateOf<Solution?>(null) }
    Spacer(Modifier.height(18.dp))
    Text(if (current != null) "Tvoja báza" else "Máš inú bázu?", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(4.dp))
    when {
        current != null -> {
            Bubble(m("Je správna. Ďalšie časti počítam v nej: §\\mathcal{B}=${vstex(own.basis(current))}§."), Modifier.fillMaxWidth(), fill = c.goodTint, line = c.good)
            Spacer(Modifier.height(4.dp))
            Quiet("Späť na vzorovú bázu", { onSet(null); st.entries.clear(); st.result = null; gem = null })
        }
        !open -> {
            Sub(if (whole) "Správne je každé doplnenie, s ktorým je súbor LN, nielen to vzorové. Máš iné? Zadaj ho, overím ho a ďalšie časti spočítam v tvojej báze."
                else "Správna je každá báza M, nielen tá vzorová: LN vektory z M, toľko, aká je dimenzia. Máš inú? Zadaj ju, overím ju a ďalšie časti spočítam v nej.")
            Spacer(Modifier.height(8.dp))
            Second("Zadať svoju bázu", { open = true }, Modifier.fillMaxWidth())
        }
        else -> {
            Sub(if (whole) "Napíš vektory, ktoré si pridal k daným." else "Napíš vektory svojej bázy.")
            Spacer(Modifier.height(8.dp))
            VectorsInput(st, enabled = true)
            st.result?.let { r -> Spacer(Modifier.height(8.dp)); Rich(r.head, style = MaterialTheme.typography.bodyLarge, color = c.ink) }
            gem?.let { g -> Spacer(Modifier.height(8.dp)); SolutionView(g) }
            Spacer(Modifier.height(8.dp))
            VectorsPad(st) {
                val list = st.submit()
                val r = own.input.check(list)
                st.result = r; st.stale.clear()
                val vs = list.map { it[0] }
                // GEM only for a set that is wrong as a whole (dependent), not for one bad vector
                gem = if (!r.ok && list.size == own.input.count.last) own.gem(own.basis(vs)) else null
                if (r.ok) { onSet(vs); open = false; gem = null }
            }
        }
    }
}

/**
 * After writing: part by part, the model solution revealed step by step, then the checklist of that part, then its
 * points. [onDone] gets the ticked rubric lines per problem (indices into the problem's checklist) and the lines of the
 * parts nothing was said about (no tap, no sure verdict): those are neither points nor mistakes.
 */
@Composable
private fun PartsReview(
    paper: TestPaper, probs: List<PaperProblem>, onRoute: (String) -> Unit, onExit: () -> Unit, report: ((String) -> Unit)?,
    finishLabel: String, onDone: (List<Set<Int>>, List<Set<Int>>) -> Unit,
) {
    val c = T.c
    val course = paperCourse(paper.id)
    val flat = remember(paper.id, probs) { probs.flatMapIndexed { k, p -> p.units.indices.map { PartAt(k, it) } } }
    // saveable: a "Zopakovať" link opens a lesson step on top, and coming back must find the part and the ticks as they were
    var i by rememberSaveable(paper.id) { mutableIntStateOf(0) }
    val ticks = rememberSaveable(paper.id, saver = TicksSaver) { mutableStateListOf<Set<Int>>().apply { repeat(probs.size) { add(emptySet()) } } }
    // what was said about each part's paper (Hodnotenie), saveable like the ticks: back from "Zopakovať" it is still there
    val hodnotenie = rememberSaveable(paper.id, saver = HodnotenieSaver) { mutableStateMapOf<Int, Int>() }
    // the student's own basis per problem: its later parts are computed in it (the checklists stay the same)
    val owned = rememberSaveable(paper.id, saver = OwnedSaver) { mutableStateMapOf<Int, List<dev.hamper.tabula.la.Vec>>() }
    val at = flat[i]
    val p = owned[at.k]?.let { e -> remember(probs, at.k, e) { probs[at.k].inBasis(e) } } ?: probs[at.k]
    val part = p.units[at.j]; val off = p.offsets()[at.j]
    val sol = part.solution
    // a check after writing (Teo, 18. 11.): the paper is written, so the model solution is there whole at once, steps
    // under each other, and the part opens at its top; step by step stays where the student has not written yet (a
    // worked example in a lesson, "Riešenie" after a wrong answer). Nothing scrolls by itself here: an earlier try
    // scrolled to the end of the solution when the part opened (18. 11.), PapersTest guards it.
    val shown = sol?.steps?.size ?: 0
    val revealed = true
    val scroll = rememberScrollState()
    LaunchedEffect(i) { scroll.scrollTo(0) }
    // where "Tvoj papier" starts in the scrolled column: while it is under the fold, the footer offers a jump to it
    var paperAt by remember(i) { mutableIntStateOf(0) }
    val toPaper = paperAt > 0 && scroll.value + scroll.viewportSize < paperAt + 40
    val jump = rememberCoroutineScope()
    val lastOfProblem = at.j == p.units.lastIndex
    // the system back does what the arrow does: a part back, from the first one out (never the whole screen at once)
    BackHandler { if (i > 0) i-- else onExit() }
    val kurzTutora = course.id
    CompositionLocalProvider(LocalTutorMiesto provides { dev.hamper.tabula.tutor.miesto("${paper.name}, príklad ${p.n}" + (if (part.label.isNotEmpty()) ", časť ${part.label}" else ""), part, kurzTutora, p.text, probs[at.k]) }) {
    StepFrame(i + 1, flat.size, { if (i > 0) i-- else onExit() }, closeLabel = "Späť", scroll = false, unit = "časť",
        trailing = report?.let { r -> { ReportButton { r("paper:${paper.id}:${p.n}") } } },
        footer = {
            // the paper's verdict is never more than a tap away, however long the solution
            if (toPaper) { Second("K hodnoteniu papiera", { jump.launch { scroll.animateScrollTo(paperAt) } }, icon = Res.drawable.ic_chev_down); Spacer(Modifier.height(8.dp)) }
            when {
                i < flat.lastIndex -> Cta(if (lastOfProblem) "Ďalší príklad" else "Ďalšia časť", { i++ })
                else -> Cta(finishLabel, {
                    // the lines of the parts nothing was said about, per problem
                    val nic = probs.map { mutableSetOf<Int>() }
                    flat.forEachIndexed { fi, a -> if (fi !in hodnotenie) { val pp = probs[a.k]; val o = pp.offsets()[a.j]; pp.units[a.j].checklist.indices.forEach { nic[a.k] += o + it } } }
                    onDone(ticks.toList(), nic)
                })
            }
        }) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Spacer(Modifier.height(2.dp))
            Label("Príklad ${p.n}" + (if (part.label.isNotEmpty()) ", časť ${part.label}" else "") + ", " + partPoints(part))
            Spacer(Modifier.height(6.dp))
            SourceCard(null) { Rich(part.text, style = MaterialTheme.typography.bodyLarge, color = c.ink) }
            Spacer(Modifier.height(14.dp))
            if (sol != null) {
                Text("Vzorové riešenie", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(2.dp))
                Sub("Porovnávaj krok po kroku so svojím papierom.")
                Spacer(Modifier.height(10.dp))
                SolutionView(sol, shown = shown)
            } else part.answer?.let { Bubble(it, Modifier.fillMaxWidth(), fill = T.hue.tint2, line = T.hue.tint) }
            p.own?.takeIf { revealed && it.part == at.j }?.let { o -> OwnBasisBox(o, owned[at.k], "${paper.id}:${at.k}") { e -> if (e == null) owned.remove(at.k) else owned[at.k] = e } }
            if (revealed) {
                Spacer(Modifier.height(18.dp))
                Text("Tvoj papier", style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.semantics { heading() }.testTag(PaperVerdictTag).onGloballyPositioned { paperAt = it.positionInParent().y.toInt() })
                Spacer(Modifier.height(10.dp))
                // one way (Teo, 18. 11.: "toho je too much"): the photo and a teacher's verdict, which fills this part's
                // rubric quietly, or one tap for the whole part; the rubric is background, no ticking, no points per line
                val vsetky = part.checklist.indices.map { it + off }.toSet()
                // this part's own index and problem: a verdict that lands after the student moved on still belongs here
                val cast = i; val ak = at.k
                key("papier:${paper.id}:$i") {
                    PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto("${paper.name}, príklad ${p.n}" + (if (part.label.isNotEmpty()) ", časť ${part.label}" else ""), part, course.id, p.text, probs[at.k]) },
                        part.checklist.map { it.text }, hodnotenie[cast], onHodnotenie = { h, ps ->
                        if (h == null) hodnotenie.remove(cast) else hodnotenie[cast] = h
                        // "Mám to": the whole part; a sure verdict: the lines it found; "Nemám" or nothing: none
                        val nove = when { h == Hodnotenie.MAM -> vsetky; ps != null -> ps.splnene.map { it + off }.toSet(); else -> emptySet() }
                        ticks[ak] = ticks[ak].filter { it !in vsetky }.toSet() + nove
                    })
                }
                reviewLink(part, course)?.let { l ->
                    Spacer(Modifier.height(6.dp))
                    Quiet("Zopakovať: ${l.title}", { onRoute(l.route) }, color = c.lesson.text)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    }
}

/** The heading over a part's verdict in the paper review (the screen opens above it, a jump goes to it). */
const val PaperVerdictTag = "paper-verdict"

/** Mistakes for the rubric lines left unticked, so the review comes back to them. */
private fun recordLost(store: Store, paper: TestPaper, probs: List<PaperProblem>, ticks: List<Set<Int>>, nic: List<Set<Int>>) {
    probs.forEachIndexed { k, pp ->
        pp.checklist.forEachIndexed { r, rb ->
            // a part nothing was said about loses no points to review: it was not graded
            if (r !in ticks[k] && r !in nic[k] && rb.fix != null) {
                val key = if (rb.fix in Generators) "gen:${rb.fix}" else rb.fix
                store.mistakes.removeAll { it.key == key }
                store.mistakes.add(Mistake(key, rb.text.take(70), "Nácvik: ${paper.name}, príklad ${pp.n}", key, currentTimeMillis()))
            }
        }
    }
}

/** One test problem on paper: read it part by part, write against the clock, check the paper part by part. */
@Composable
fun PaperProblemScreen(paper: TestPaper, p: PaperProblem, store: Store, onReport: (String) -> Unit, onRoute: (String) -> Unit = {}, onClose: () -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        val c = T.c
        // saveable, like the grading: back from a "Zopakovať" link the problem is where it was, not at its start
        var phase by rememberSaveable(paper.id, p.n) { mutableIntStateOf(0) }
        val total = p.minutes * 60
        var seconds by rememberSaveable(paper.id, p.n) { mutableIntStateOf(total) }
        LaunchedEffect(phase) { while (phase == 1 && seconds > 0) { delay(1000); seconds-- } }
        if (phase == 2) {
            PartsReview(paper, listOf(p), onRoute, { phase = 1 }, onReport, "Hotovo") { ticks, nic ->
                val got = p.checklist.filterIndexed { k, _ -> k in ticks[0] }.sumOf { it.pts }
                // a line lost in a part something was said about is a mistake; nothing said about any part records nothing
                val strateny = p.checklist.indices.any { it !in ticks[0] && it !in nic[0] }
                if (strateny || nic[0].isEmpty()) store.record("paper:${paper.id}:${p.n}", !strateny, false, Mistake("paper:${paper.id}:${p.n}", "${paper.name}, príklad ${p.n}", "Na papieri $got z ${p.pts} b", "paper:${paper.id}:${p.n}", currentTimeMillis()))
                onClose()
            }
            return@CompositionLocalProvider
        }
        val kurzTutora = paperCourse(paper.id).id
        CompositionLocalProvider(LocalTutorMiesto provides { dev.hamper.tabula.tutor.miesto("${paper.name}, príklad ${p.n}", p.units.first(), kurzTutora, p.text, p) }) {
        StepFrame(phase + 1, 3, onClose, closeLabel = "Zavrieť príklad", trailing = { ReportButton { onReport("paper:${paper.id}:${p.n}") } }, footer = {
            when (phase) {
                0 -> Cta("Spustiť ${p.minutes} ${slovakPlural(p.minutes, "minútu", "minúty", "minút")}", { phase = 1 }, icon = Res.drawable.ic_timer)
                else -> Cta("Mám to na papieri", { phase = 2 }, icon = Res.drawable.ic_write)
            }
        }) {
            Spacer(Modifier.height(2.dp))
            Label("${paper.name}, príklad ${p.n}, ${p.pts} b")
            Spacer(Modifier.height(8.dp))
            when (phase) {
                0 -> {
                    ProblemBlocks(p, null)
                    Spacer(Modifier.height(14.dp))
                    Text("Polož telefón a píš.", style = MaterialTheme.typography.headlineSmall, color = c.ink)
                    Spacer(Modifier.height(4.dp))
                    val course = paperCourse(paper.id)
                    Sub(if (course == La1) "Hodnotí sa najmä postup a presné vyjadrovanie. Samotný výsledok plný počet bodov nezaručuje (MARAST, lekce 292)." else "Hodnotí sa postup: každý logický krok a jeho zdôvodnenie, nie len výsledok (hlavička zadania).")
                    Spacer(Modifier.height(10.dp))
                    SourceCard("Hlavička zadania") { Rich(p.note ?: paperCourse(paper.id).paperHeader, style = MaterialTheme.typography.bodyMedium, color = c.ink) }
                    Spacer(Modifier.height(10.dp))
                }
                else -> {
                    Clock(seconds, total, Modifier.fillMaxWidth().height(230.dp))
                    Spacer(Modifier.height(8.dp))
                    ProblemBlocks(p, p.note)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        }
    }
}

/* ---- Mock tests -------------------------------------------------------------------------------------------- */

@Composable
fun PapersScreen(store: Store, onClose: () -> Unit, onPage: (() -> Unit)? = null, onOpen: (String) -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        StepFrame(0, 0, onClose, closeLabel = "Späť") {
            val w = store.course.test.word
            Label("Nácvik ${w.gen}")
            Spacer(Modifier.height(4.dp))
            H1("Vyber ${w.acc}.")
            Spacer(Modifier.height(4.dp))
            val course = store.course
            val shown = course.shown()
            Sub("${shown.mockShape.let { (_, pts, min) -> "$pts bodov, $min minút" }}, na papier.")
            Spacer(Modifier.height(14.dp))
            // the page about the test: what each problem usually asks, where points are lost, one evening's order
            if (onPage != null) testPage(course.test.id)?.let { page -> PaperRow(page.title, pageBlurb(page, course.test.word, types = true), null, onPage) }
            for (p in course.papersFor(course.test).mapNotNull(shown::offered)) PaperRow(p.name, missingNote(p) ?: p.src, store.mocks.lastOrNull { it.paper == p.id }) { onOpen(p.id) }
            val seed = (currentTimeMillis() % 100000).toInt()
            course.generatedPaper(seed).takeIf { it.problems.isNotEmpty() }?.let(shown::offered)?.let { g -> PaperRow("Nové čísla", missingNote(g) ?: course.generatedNote, null) { onOpen(g.id) } }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PaperRow(title: String, sub: String, last: MockResult?, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).card(radius = 18.dp).clip(RoundedCornerShape(18.dp)).clickable(role = Role.Button, onClick = onClick).heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        if (last != null) Text("${last.total} z ${last.max}", style = MaterialTheme.typography.titleSmall, color = c.lesson.text)
        Spacer(Modifier.width(6.dp))
        Icon24(Res.drawable.ic_chevron, tint = c.ink3)
    }
}

@Composable
fun MockIntroScreen(paper: TestPaper, onBack: () -> Unit, onStart: () -> Unit) {
    // the clock lives in a notification and on the lock screen (below): without the notification permission (a fresh
    // install on Android 13+ never had it asked) the service ran with an invisible one. Ask once at the start; the
    // test starts whatever the answer (QA on the emulator, 19. 11.)
    val platform = dev.hamper.tabula.platform.LocalPlatform.current
    var started by remember { mutableStateOf(false) }
    val startOnce = { if (!started) { started = true; onStart() } }
    val ask = platform.rememberReminderPermission { startOnce() }
    val start = { if (platform.remindersAllowed()) startOnce() else ask() }
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        val c = T.c
        StepFrame(0, 0, onBack, closeLabel = "Späť", footer = { Cta("Spustiť ${paperMinutes(paper.id)} minút", start, icon = Res.drawable.ic_timer) }) {
            Spacer(Modifier.height(4.dp))
            val w = paperExam(paper).word
            H1("Ako na ${w.realLoc}.")
            Spacer(Modifier.height(4.dp))
            Sub(if (paper.generated) "Nové čísla v šablónach minulých ${w.genPl}. Priprav si papier a pero." else "Zadania z: ${paper.name}. Priprav si papier a pero.")
            Spacer(Modifier.height(14.dp))
            val n = paper.problems.size
            Text("$n ${slovakPlural(n, "príklad", "príklady", "príkladov")}, ${paper.problems.sumOf { it.pts }} bodov, ${paperMinutes(paper.id)} minút", style = MaterialTheme.typography.titleLarge, color = c.ink)
            missingNote(paper)?.let { Spacer(Modifier.height(4.dp)); Sub(it) }
            Spacer(Modifier.height(8.dp))
            for (p in paper.problems) {
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Rich("**${p.n}.** ${p.title}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.ink2, boldColor = c.ink)
                    Text("${p.pts} b, asi ${p.minutes} min", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                }
            }
            Spacer(Modifier.height(14.dp))
            // the web has no notification: its clock is the wall clock and the test comes back with the page (web/Main.kt)
            Bubble(
                if (platform.isWeb) "Môžeš odísť zo stránky aj zamknúť telefón. Čas beží ďalej a keď sa vrátiš, test tu pokračuje, aj po zatvorení stránky."
                else "Môžeš odísť z aplikácie. Čas beží v oznámení a na zamknutej obrazovke.",
                Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            SourceCard("Hlavička zadania") { Rich(paperCourse(paper.id).paperHeader, style = MaterialTheme.typography.bodyMedium, color = c.ink) }
            Spacer(Modifier.height(12.dp))
        }
    }
}

fun Modifier.keepScreenOn(): Modifier = composed {
    val view = dev.hamper.tabula.compat.LocalView.current
    DisposableEffect(view) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }
    this
}

private fun clockText(secs: Int) = if (secs >= 3600) "%d:%02d:%02d".format(secs / 3600, secs / 60 % 60, secs % 60) else "%d:%02d".format(secs / 60, secs % 60)
private fun minutesWord(m: Int) = "$m ${slovakPlural(m, "minúta", "minúty", "minút")}"

/**
 * The test itself. A small clock, the problems as tabs, each problem as separate blocks. Time is counted per problem
 * while it is open; past its suggested minutes a quiet note says so, nothing blinks.
 */
@Composable
fun MockRunScreen(paper: TestPaper, endAt: Long, onFinish: () -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        val c = T.c; val h = T.hue
        var left by remember { mutableLongStateOf(endAt - SystemClock.elapsedRealtime()) }
        var cur by remember { mutableIntStateOf(0) }
        val spent = remember(paper.id) { mutableStateListOf<Long>().apply { repeat(paper.problems.size) { add(0L) } } }
        LaunchedEffect(endAt) {
            var last = SystemClock.elapsedRealtime()
            while (left > 0) {
                delay(500)
                val now = SystemClock.elapsedRealtime()
                spent[cur] = spent[cur] + (now - last); last = now
                left = endAt - now
            }
        }
        val secs = (left / 1000).toInt().coerceAtLeast(0)
        var confirm by remember { mutableStateOf(false) }
        BackHandler { confirm = true }
        val total = paperMinutes(paper.id) * 60
        Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().keepScreenOn()) {
            Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Label("Nácvik ${paperExam(paper).word.gen}")
                Spacer(Modifier.weight(1f))
                Text(
                    clockText(secs),
                    Modifier.semantics { heading(); contentDescription = "${slovakPlural(secs / 60, "Zostáva", "Zostávajú", "Zostáva")} ${minutesWord(secs / 60)}" },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), color = c.ink2,
                )
            }
            // the whole time as one thin line, so it reads at a glance and never shouts
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.line)) {
                Box(Modifier.fillMaxWidth(((total - secs).toFloat() / total).coerceIn(0f, 1f)).fillMaxHeight().background(h.main))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                paper.problems.forEachIndexed { i, p ->
                    // a no-break space keeps "7 b" together when a narrow screen at a large font wraps the tab
                    Choice("${p.n}.  ${p.pts} b", { cur = i }, Modifier.weight(1f), center = true, state = if (cur == i) Pick.Picked else Pick.Idle, minHeight = 48.dp, style = MaterialTheme.typography.titleSmall)
                }
            }
            val scroll = rememberScrollState()
            LaunchedEffect(cur) { scroll.scrollTo(0) }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll).padding(horizontal = 20.dp).padding(top = 14.dp)) {
                val p = paper.problems[cur]
                Label("Príklad ${p.n}, ${p.pts} b, odporúčaný čas ${p.minutes} min")
                Spacer(Modifier.height(8.dp))
                val on = (spent[cur] / 60000).toInt()
                if (on > p.minutes) {
                    Bubble("Na tomto príklade si už ${minutesWord(on)}, odporúča sa ${p.minutes}. Ak sa zasekneš, prejdi na ďalší a vráť sa k nemu.", Modifier.fillMaxWidth().padding(bottom = 10.dp), fill = T.hue.tint2, line = T.hue.tint)
                }
                ProblemBlocks(p, p.note ?: "Píš na papier, celé vety. Čas beží aj mimo aplikácie.")
                Spacer(Modifier.height(12.dp))
            }
            Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) { Cta("Odovzdať", { confirm = true }) }
        }
        if (confirm) AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Odovzdať nácvik?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Čas sa zastaví. Potom prejdeš riešenie časť po časti a body si dáš podľa papiera.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { TextButton({ confirm = false; onFinish() }) { Text("Odovzdať") } },
            dismissButton = { TextButton({ confirm = false }) { Text("Ešte píšem") } },
            containerColor = c.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = DialogWidth),
        )
    }
}

/** Self-check, part by part: the solution step by step, the checklist, the points. The score is the sum of ticked lines. */
@Composable
fun MockGradeScreen(paper: TestPaper, store: Store, onRoute: (String) -> Unit = {}, onLeave: () -> Unit = {}, onDone: () -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        // back from the first part leaves the grading: asked first, since the paper's points are not saved until the end
        var leave by remember { mutableStateOf(false) }
        if (leave) AlertDialog(
            onDismissRequest = { leave = false },
            title = { Text("Prestať hodnotiť?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Body z tohto nácviku sa neuložia a chyby sa nevrátia v opakovaní.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { TextButton({ leave = false; onLeave() }) { Text("Prestať") } },
            dismissButton = { TextButton({ leave = false }) { Text("Hodnotiť ďalej") } },
            containerColor = T.c.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = DialogWidth),
        )
        PartsReview(paper, paper.problems, onRoute, { leave = true }, null, "Pozrieť výsledok") { ticks, nic ->
            val got = paper.problems.mapIndexed { k, pp -> pp.checklist.mapIndexed { r, rb -> if (r in ticks[k]) rb.pts else 0 } }
            store.mocks += MockResult(paper.id, currentTimeMillis(), got, got.sumOf { it.sum() }, paper.problems.sumOf { it.pts }, nic)
            store.markActive()
            dev.hamper.tabula.system.Telemetry.mockTest(store.course.id, paper.id, got.sumOf { it.sum() }, paper.problems.sumOf { it.pts })
            recordLost(store, paper, paper.problems, ticks, nic)
            store.save()
            onDone()
        }
    }
}

/** Points of one part in a saved result. */
private fun partGot(r: MockResult, k: Int, p: PaperProblem, j: Int): Int {
    val off = p.offsets()[j]
    return p.units[j].checklist.indices.sumOf { r.got.getOrNull(k)?.getOrNull(off + it) ?: 0 }
}

@Composable
fun MockResultScreen(store: Store, onClose: () -> Unit, onFix: (String) -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.paper) {
        val c = T.c
        val r = store.mocks.lastOrNull { paperCourse(it.paper) == store.course }
        if (r == null) { LaunchedEffect(Unit) { onClose() }; return@CompositionLocalProvider }
        val paper = paperById(r.paper)
        val course = paperCourse(paper.id)
        // where the points went: every part that lost some, the biggest loss first
        data class Lost(val k: Int, val p: PaperProblem, val part: PaperPart, val pts: Int, val lines: List<Rubric>)
        val lost = paper.problems.flatMapIndexed { k, p ->
            p.units.mapIndexedNotNull { j, part ->
                val off = p.offsets()[j]
                // lines of a part nothing was said about are not lost: that part was not graded
                val nehodnotene = r.nehodnotene.getOrNull(k).orEmpty()
                val lines = part.checklist.filterIndexed { i, _ -> off + i !in nehodnotene && (r.got.getOrNull(k)?.getOrNull(off + i) ?: 0) == 0 }
                val pts = lines.sumOf { it.pts }
                if (pts > 0) Lost(k, p, part, pts, lines) else null
            }
        }.sortedByDescending { it.pts }
        val worst = lost.firstNotNullOfOrNull { reviewLink(it.part, course) }
        // the score counts up as its points fill in, and lands with one click; an honest result, no confetti
        val still = calm()
        val hx = rememberHaptics()
        val shown = remember(r) { Animatable(if (still) r.total.toFloat() else 0f) }
        LaunchedEffect(r) {
            if (!still) { delay(200); shown.animateTo(r.total.toFloat(), tween(700, easing = FastOutSlowInEasing)) }
            if (r.total > 0) hx.land()
        }
        StepFrame(0, 0, onClose, closeLabel = "Zavrieť výsledok", footer = {
            if (worst != null) Cta("Zopakovať, čo stálo najviac", { onFix(worst.route) }) else Cta("Hotovo", onClose)
        }) {
            Spacer(Modifier.height(2.dp))
            Label("${paper.name}, ${paperMinutes(paper.id)} minút na papieri")
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${shown.value.roundToInt()}", style = MaterialTheme.typography.displayLarge, color = c.ink, modifier = Modifier.semantics { contentDescription = "${r.total}" })
                Spacer(Modifier.width(8.dp))
                Text("z ${r.max} b", style = MaterialTheme.typography.headlineSmall, color = c.ink2, modifier = Modifier.padding(bottom = 9.dp))
            }
            Spacer(Modifier.height(6.dp))
            ScorePips(r, paper, upTo = shown.value)
            Spacer(Modifier.height(16.dp))
            paper.problems.forEachIndexed { k, p ->
                Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).panel(16.dp).padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Rich("**Príklad ${p.n}**  ${p.title}", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.ink2, boldColor = c.ink)
                        Text("${r.got.getOrNull(k)?.sum() ?: 0} z ${p.pts}", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    }
                    if (p.units.size > 1) {
                        Spacer(Modifier.height(4.dp))
                        Text(p.units.indices.joinToString("   ") { j -> "${p.units[j].label} ${partGot(r, k, p, j)}/${p.units[j].pts}" }, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                    }
                }
            }
            if (lost.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Tu ležia tvoje body", style = MaterialTheme.typography.titleLarge, color = c.ink)
                Spacer(Modifier.height(6.dp))
                lost.take(6).forEach { l ->
                    val link = reviewLink(l.part, course)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).then(if (link != null) Modifier.clickable(role = Role.Button) { onFix(link.route) } else Modifier)
                            .heightIn(min = 48.dp).padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("−${l.pts}", style = MaterialTheme.typography.titleMedium, color = c.bad, maxLines = 1, softWrap = false, modifier = Modifier.padding(end = 10.dp).widthIn(min = 28.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Príklad ${l.p.n}" + (if (l.part.label.isNotEmpty()) ", časť ${l.part.label}" else ""), style = MaterialTheme.typography.labelMedium, color = c.ink2)
                            Rich(l.lines.joinToString("; ") { it.text }, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                            if (link != null) Rich("Zopakovať: ${link.title}", style = MaterialTheme.typography.bodySmall, color = c.lesson.text)
                        }
                        if (link != null) { Spacer(Modifier.width(6.dp)); Icon24(Res.drawable.ic_chevron, tint = c.ink3) }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

/** Thirty points as thirty cells: a gap is a lost point. */
@Composable
fun ScorePips(r: MockResult?, paper: TestPaper?, modifier: Modifier = Modifier, upTo: Float? = null) {
    val c = T.c
    val probs = paper?.problems ?: listOf(PaperProblem(1, 7, "", "", 0, emptyList(), ""), PaperProblem(2, 12, "", "", 0, emptyList(), ""), PaperProblem(3, 11, "", "", 0, emptyList(), ""))
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        probs.forEachIndexed { i, p ->
            val earned = r?.got?.getOrNull(i)?.sum() ?: 0
            // while the score counts up, the points fill in problem by problem, as many as the number shows
            val before = (0 until i).sumOf { j -> r?.got?.getOrNull(j)?.sum() ?: 0 }
            val got = if (upTo == null) earned else (upTo - before).roundToInt().coerceIn(0, earned)
            Row(Modifier.weight(p.pts.toFloat()), horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                for (k in 0 until p.pts) Box(Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(3.dp)).background(if (k < got) c.lesson.main else c.line))
            }
        }
    }
}

package dev.hamper.tabula.screens

import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.compat.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.res.*

/** Options in a stable shuffled order per item, so the right one isn't always first. Two-option pairs keep their order. */
fun order(id: String, n: Int): List<Int> = if (n < 3) (0 until n).toList() else (0 until n).shuffled(Random(id.hashCode()))

/**
 * A lesson: one step after another, as one history (state/LessonRun.kt). The system back is handled here and only
 * here: exactly one screen back, a step's own screens included; on the first screen it leaves ([onLeave]), and the
 * run stays, so the lesson opens again where it was. A tap on the step bar lists the steps reached so far.
 * A practice step of a type already mastered is left out.
 */
@Composable
fun LessonFlow(lesson: LessonDef, store: Store, clips: ClipHost, onReport: (String) -> Unit, onClose: () -> Unit, onLeave: () -> Unit, onDone: () -> Unit) {
    // looked up again when a run is replaced (a link that starts the open lesson over); a finished lesson keeps its
    // run while it animates out
    val run = remember(lesson.id, store.restarts) { store.run(lesson) }
    remember(lesson.id) { BarMemory.reset() }
    val total = lesson.steps.size
    var list by remember { mutableStateOf(false) }
    // the end reached with steps jumped over (the step list goes ahead too): how many, offered once to finish
    var gaps by remember { mutableStateOf<List<Int>?>(null) }
    // a series of right answers counts within one lesson
    LaunchedEffect(lesson.id) { Series.n = 0; store.lessonOpened(lesson.id) }
    // statistics: the step on screen (its time, answers and help belong to it), how it was left
    LaunchedEffect(lesson.id, run.at) { Telemetry.stepShown(lesson.id, run.at) }
    val leave = { Telemetry.lessonLeft(); onLeave() }
    // the pieces (data/Pieces.kt) only mark the step bar: their ends ask for nothing, and leaving keeps the very step
    val ps = remember(lesson.id) { pieces(lesson) }
    BackHandler { if (!run.back()) leave() }
    // the lesson finished: a second tap on the last step's button does not finish it again
    var finished by remember(lesson.id, store.restarts) { mutableStateOf(false) }
    // the step on screen has been there StepSettle (frame time, so the desktop tests' clock runs it too): a tap before
    // that is the second half of the double tap that brought it, not a tap on its own button
    val settled = remember(lesson.id) { booleanArrayOf(false) }
    LaunchedEffect(lesson.id, run.at) {
        settled[0] = false
        val start = withFrameMillis { it }
        while (withFrameMillis { it } - start < StepSettle) Unit
        settled[0] = true
    }
    // on from step [from] only while it is the step on screen, and not again right after a move: a quick double tap on
    // "Pokračovať" skipped the next step unseen (emulator, 1.6.2), its second tap landing either on the step going out
    // (still composed during the swap) or on the new step's own button, which is enabled at once (an explanation)
    val next: (Int) -> Unit = { from ->
        if (from == run.at && !finished && settled[0]) {
            Telemetry.stepLeft()
            val more = run.next { k -> (lesson.steps[k] as? Step.Practice)?.let { store.mastered(practiceKey(it)) } == true }
            store.lessonStep(lesson.id, if (more) run.at else total)
            if (!more) {
                val left = run.unseen()
                if (left.isEmpty()) { finished = true; store.finishLesson(lesson.id); onDone() }
                else { store.lessonEndedWithGaps(lesson.id); gaps = left }
            }
        }
    }
    CompositionLocalProvider(LocalStepBarTap provides { list = true }) {
        AnimatedContent(run.at, label = "step", transitionSpec = { StepSwap }) { k ->
            val step = lesson.steps[k]
            CompositionLocalProvider(LocalStepNav provides remember(run, k) { StepNav(run, k) }, LocalTutorMiesto provides remember(lesson.id, k) { { dev.hamper.tabula.tutor.miesto(lesson.id, k, step, lesson.name) } }) {
                // the header's bar is the lesson's, cut into its pieces, without words (Teo, 19. 11.: no panel and no
                // second count at a piece's end); the bar only ever moves on (18. 11.: a bar that jumps back blinks)
                CompositionLocalProvider(LocalStepPieces provides ps) {
                    StepMotionOf(run.forward) { StepView(step, Frame(k + 1, total, onClose, onReport, "Zavrieť lekciu"), store, clips) { next(k) } }
                }
            }
        }
    }
    if (list) StepList(lesson, run, { list = false }) { k -> list = false; run.jump(k) }
    gaps?.let { left ->
        val n = left.size
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { gaps = null },
            title = { Text("Ešte ${slovakPlural(n, "jeden preskočený krok", "$n preskočené kroky", "$n preskočených krokov")}", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Lekcia bude hotová, keď ${if (n == 1) "ho prejdeš" else "ich prejdeš"}, alebo keď povieš, že ju vieš. Inak ju nájdeš na Dnes medzi rozrobenými.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { androidx.compose.material3.TextButton({ gaps = null; run.jump(left.first()) }) { Text(if (n == 1) "Prejsť ho" else "Prejsť ich") } },
            dismissButton = {
                Row {
                    androidx.compose.material3.TextButton({ gaps = null; leave() }) { Text("Neskôr") }
                    androidx.compose.material3.TextButton({ gaps = null; store.knowLesson(lesson.id); leave() }) { Text("Viem to") }
                }
            },
            containerColor = T.c.card, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = DialogWidth),
        )
    }
}

/** A grid to count from (its goal is a tap) whose text asks for no tap: nothing to do on it before going on. */
internal fun asksNoTap(s: Step.Explore): Boolean =
    (s.pic as? Pic.Grid)?.goal == Goal.Count && listOf(s.title, s.sub).none { it.contains("ťuk", ignoreCase = true) }

/** How long after a step changes a tap on the lesson's way on is still the tap that moved it (a double tap). */
private const val StepSettle = 350L

/** The review key of a practice step: its problem type. */
fun practiceKey(s: Step.Practice) = "gen:" + (TemplateOf[s.gen] ?: s.gen)

@Composable
fun StepView(step: Step, frame: Frame, store: Store, clips: ClipHost, onNext: () -> Unit) {
    val report = { frame.onReport(step.id) }
    when (step) {
        is Step.Ask -> AskStep(step, frame, report, onNext)
        is Step.Explore -> ExploreStep(step, frame, report, onNext)
        is Step.Explain -> ExplainStep(step, frame, report, onNext)
        is Step.Worked -> WorkedStep(step, frame, report, onNext)
        is Step.Check -> CheckStep(step, frame, store, report, onNext)
        is Step.Practice -> PracticeStep(step, frame, store, onNext)
        is Step.Clip -> ClipFlow(ClipLibrary.byId(step.clip), store, clips, frame, onNext)
        is Step.Def -> ConceptFlow(ConceptById.getValue(step.concept), store, frame, onNext)
        is Step.Thm -> TheoremFlow(TheoremById.getValue(step.theorem), store, frame, onNext, step.also, step.proof?.let { ProofById[it] })
        is Step.Prove -> ProofFlow(ProofById.getValue(step.proof), store, frame, onNext)
    }
}

/* ---- Ask: a guess first ------------------------------------------------------------------------------------- */

@Composable
private fun AskStep(s: Step.Ask, frame: Frame, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    var pick by keptPick("pick", s.id).fit(null) { it == null || it in s.options.indices }
    var unsure by keptBool("unsure", false, s.id)
    val ord = remember(s.id) { order(s.id, s.options.size) }
    val answered = pick != null || unsure
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
        footerTint = if (answered) c.lesson.tint2 else null,
        footer = {
            if (!answered) {
                val st = MaterialTheme.typography.titleLarge
                ChoiceGroup(ord.map { s.options[it] }, s.options.size == 2 && sideBySide(s.options), st) { i, m, row ->
                    val k = ord[i]
                    Choice(s.options[k], { pick = k }, m.then(if (i == 0) Modifier.firstAnswer() else Modifier), center = row, minHeight = if (row) 64.dp else 60.dp, style = st)
                }
                Quiet("Neviem, ukáž mi to", { unsure = true })
            } else {
                val head = when { unsure -> "Nevadí, ideme na to."; pick == s.correct -> "Odhad sedí."; else -> "Odhad bol iný." }
                // a guess is never right or wrong: a calm head, Pytagoras pleased with a good guess, else thinking it over
                SheetHead(head, if (pick == s.correct && !unsure) Mood.Pleased else Mood.Pondering, c.ink, pick == s.correct, style = MaterialTheme.typography.headlineSmall) {
                    // the answer's first sentence shows; what the wrong guess got wrong waits behind "Prečo?" with the rest
                    val why = listOfNotNull(s.after, pick?.takeIf { !unsure && it != s.correct }?.let { s.wrong[it] }).joinToString(" ")
                    Spacer(Modifier.height(6.dp))
                    Explain(why, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                }
                Spacer(Modifier.height(4.dp))
                Cta("Pokračovať", onNext)
            }
        }) {
        Spacer(Modifier.height(8.dp))
        H1(s.q)
        Spacer(Modifier.height(6.dp))
        s.sub?.let { Sub(it) }
        Spacer(Modifier.height(12.dp))
        s.pic?.let { PicView(it, Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp)) }
        Spacer(Modifier.height(10.dp))
    }
}

/* ---- Explore: the picture is the lesson ------------------------------------------------------------------------ */

@Composable
private fun ExploreStep(s: Step.Explore, frame: Frame, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    var reached by keptBool("reached", false, s.id)
    // a picture that only asks a question about what it shows ("Koľko má bodov?"), no tap: the way on is open from the
    // start (it waited for a tap the text never asked for, and the tap changed what was being counted); the first
    // "Pokračovať" shows the result, the next one goes on
    val asksNoTap = remember(s.id) { asksNoTap(s) }
    val ready = remember(s.id) { booleanArrayOf(false) }
    LaunchedEffect(s.id, reached) {
        ready[0] = false
        val start = withFrameMillis { it }
        while (withFrameMillis { it } - start < StepSettle) Unit
        ready[0] = true
    }
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, scroll = false,
        footerTint = if (reached) c.goodTint else null,
        footer = {
            // the result rises over the bottom of the picture, never moves it; the way on is in the same panel
            // the picture's result is a verdict like every answer: its first sentence heads it, the rest waits behind Prečo?
            if (reached) {
                val (head, more) = remember(s.id) { verdictHead(s.doneTitle, "Hotovo.") }
                Verdict(head, listOfNotNull(more, s.doneBody).joinToString(" ").ifBlank { null }, true)
            }
            // a button that first shows the result and then goes on: each of its two taps only after StepSettle, so the
            // second half of a double tap neither shows the count before the student counted nor skips the shown result
            if (!lekcia() || reached || s.pic !is dev.hamper.tabula.data.Pic.Gem)
                Cta("Pokračovať", { if (ready[0]) { if (reached) onNext() else { reached = true; ready[0] = false } } }, enabled = reached || asksNoTap)
        }) {
        // on a short screen (large font) the picture keeps a usable height and the body scrolls; once the panel
        // is up, the body can scroll out from under it, but nothing scrolls by itself
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val h = maxOf(maxHeight, 520.dp)
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(Modifier.fillMaxWidth().height(h)) {
                    H1(s.title)
                    Spacer(Modifier.height(4.dp))
                    Sub(s.sub)
                    Spacer(Modifier.height(10.dp))
                    PicView(s.pic, Modifier.fillMaxWidth().weight(1f)) { reached = true }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(LocalFooterOverlap.current))
            }
        }
    }
}

/* ---- Explain ---------------------------------------------------------------------------------------------------- */

@Composable
private fun ExplainStep(s: Step.Explain, frame: Frame, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    CompositionLocalProvider(LocalReadingSelf provides wholeCards(s.cards)) {
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = { Cta("Pokračovať", onNext) }) {
        Spacer(Modifier.height(4.dp))
        H1(s.title)
        Spacer(Modifier.height(10.dp))
        Rich(s.body, style = MaterialTheme.typography.bodyLarge, color = c.ink)
        s.pic?.let { Spacer(Modifier.height(12.dp)); PicView(it, Modifier.fillMaxWidth().height(260.dp)) }
        s.formal?.let {
            Spacer(Modifier.height(14.dp))
            SourceCard(s.src ?: "Zo skrípt", statement = true) { SourceLines(it) }
        }
        // the definitions and theorems it rests on, a card each with its own number; a reference to one of them on
        // this screen is not a link (it would open what is already here)
        if (s.cards.isNotEmpty()) { Spacer(Modifier.height(14.dp)); CompositionLocalProvider(LocalReadingSelf provides LocalReadingSelf.current + wholeCards(s.cards)) { ItemCards(s.cards) } }
        // a source from the textbook reads as a source label (Skriptá · veta o hľadaní bázy, 2.59); others say "Zdroj:"
        if (s.formal == null && s.src != null) { Spacer(Modifier.height(8.dp)); if (s.src.startsWith("Skriptá")) CompositionLocalProvider(dev.hamper.tabula.math.LocalRefLabel provides true) { Label(s.src) } else Label("Zdroj: ${s.src}") }
        s.more?.let { Spacer(Modifier.height(10.dp)); Rich(it, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
        Spacer(Modifier.height(12.dp))
    }
    }
}

/* ---- Worked example, line by line --------------------------------------------------------------------------- */

@Composable
private fun WorkedStep(s: Step.Worked, frame: Frame, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    val n = s.solution.steps.size
    var shown by keptInt("shown", 1, s.id)
    val all = shown >= n
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = {
        if (all) Cta("Pokračovať", onNext) else if (lekcia()) MainAndQuiet("Všetko", { shown = n }, "Ďalší krok", { shown++ }) else ButtonRow {
            Second("Všetko", { shown = n })
            Cta("Ďalší krok", { shown++ })
        }
    }) {
        Spacer(Modifier.height(4.dp))
        Kicker("Riešený príklad")
        Spacer(Modifier.height(2.dp))
        H1(s.title)
        Spacer(Modifier.height(10.dp))
        SourceCard(s.src ?: "Zadanie", statement = true) { Rich(s.problem, style = MaterialTheme.typography.bodyLarge, color = c.ink) }
        Spacer(Modifier.height(12.dp))
        // the same goals as a solution in practice: done ones checked, the current one open, the rest ahead
        Column(Modifier.animateContentSize()) { SolutionView(s.solution, shown = shown, follow = true) }
        Spacer(Modifier.height(12.dp))
    }
}

/* ---- Check: your attempt, with feedback ------------------------------------------------------------------------ */

/**
 * The question, the options, your pick and what it means sit together, as on paper. Only the first pick counts.
 * A miss gives a way on without giving the answer away: the hint if the step has one, another try, or the
 * solution on request (the attempt is already counted as a miss).
 */
@Composable
private fun CheckStep(s: Step.Check, frame: Frame, store: Store, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    // what is read back must fit the step as it is now (a new version may have other options)
    var pick by keptPick("pick", s.id).fit(null) { it == null || it in s.options.indices }
    var hint by keptBool("hint", false, s.id)
    var missed by keptBool("missed", false, s.id)
    var solved by keptBool("solved", false, s.id) // the solution was asked for
    var tried by keptInts("tried", s.id).fit(emptyList()) { t -> t.all { it in s.options.indices } }
    val ord = remember(s.id) { order(s.id, s.options.size) }
    val right = pick == s.correct
    val done = right || solved
    val big = MaterialTheme.typography.bodyLarge
    // the verdict rises in the bottom panel, where every answer's verdict is; a miss offers another try or the solution
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, verdictCard = true,
        footerTint = when { right -> c.goodTint; solved -> c.card; pick != null -> c.badTint; else -> null },
        footer = when {
            right -> { { Verdict(if (missed) "Teraz je to správne." else "Správne.", s.why, true, bodyStyle = big, cheer = !missed); Cta("Pokračovať", onNext) } }
            // a shown solution is not a success: a plain panel, no tick, no green
            solved -> { { Shown(s.why, big); Cta("Pokračovať", onNext) } }
            // the step's own words for this pick, else its hint; never the answer on a miss
            pick != null -> { {
                Verdict("Tentoraz nie.", s.wrong[pick ?: -1] ?: s.hint?.takeUnless { hint } ?: tryNext(s.options.size - tried.size), false, bodyStyle = big)
                ButtonRow {
                    // on the page under a verdict card the ghost button has the page's frame; on the mistake's sheet its own
                    Second("Riešenie", { solved = true; pick = null; Telemetry.help("solution", "step:${s.id}") }, line = if (LocalSheetCard.current) null else c.badLine)
                    Cta("Skúsiť znova", { pick = null })
                }
            } }
            else -> null
        }) {
        Spacer(Modifier.height(8.dp))
        H1(s.q)
        s.sub?.let { Spacer(Modifier.height(6.dp)); Sub(it) }
        if (hint && s.hint != null) { Spacer(Modifier.height(10.dp)); Bubble(s.hint, Modifier.fillMaxWidth()) }
        s.pic?.let { Spacer(Modifier.height(12.dp)); PicView(it, Modifier.fillMaxWidth().height(300.dp)) }
        Spacer(Modifier.height(18.dp))
        val st = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
        val row = (s.options.size == 2 && sideBySide(s.options)) || (s.options.size <= 4 && s.options.all { it.length <= 6 })
        @Composable
        fun option(k: Int, m: Modifier, row: Boolean) {
            val state = when {
                k == s.correct && done -> Pick.Right
                k == pick || k in tried -> Pick.Wrong
                else -> Pick.Idle
            }
            // untried options stay fully readable and tappable after a miss; only what was tried and rejected before
            // the current pick steps back, and once it is solved the rest is just context
            val open = !done && k !in tried && pick == null
            val back = (k in tried && k != pick) || (done && state == Pick.Idle)
            Choice(s.options[k], {
                pick = k
                if (k == s.correct) { if (missed) hx.rightAgain() else hx.right() } else { hx.wrong(); tried = tried + k; Telemetry.given("step:${s.id}", s.options[k]) }
                if (!missed) store.record("step:${s.id}", k == s.correct, hint, Mistake("step:${s.id}", "Otázka v lekcii", s.q.take(80), "step:${s.id}", currentTimeMillis()))
                if (k != s.correct) missed = true
            }, m, state = state, dim = back && state != Pick.Idle, enabled = open, quiet = back && state == Pick.Idle, center = row, reveal = k == pick || (done && k == s.correct),
                minHeight = if (row) 64.dp else 60.dp, style = st, tag = if (k == s.correct && open) "correct" else null)
        }
        fun first(i: Int) = if (i == 0) Modifier.firstAnswer() else Modifier
        ChoiceGroup(ord.map { s.options[it] }, row, st) { i, m, inRow -> option(ord[i], m.then(first(i)), inRow) }
        if (pick == null && !done && s.hint != null && !hint) { Spacer(Modifier.height(4.dp)); Quiet("Nápoveda", { hint = true; Telemetry.help("hint", "step:${s.id}") }) }
        Spacer(Modifier.height(12.dp))
    }
}

/** What is left after a miss, said as the next move. */
private fun tryNext(left: Int) = when (left) {
    1 -> "Ostáva už len jedna možnosť."
    2 -> "Skús jednu z dvoch zvyšných."
    3 -> "Skús jednu z troch zvyšných."
    4 -> "Skús jednu zo štyroch zvyšných."
    else -> "Skús inú možnosť."
}

/** A solution the student asked to see: neutral, so it never reads as a right answer. */
@Composable
private fun Shown(body: String, style: androidx.compose.ui.text.TextStyle) {
    val c = T.c
    val titleStyle = MaterialTheme.typography.titleLarge
    val d = androidx.compose.ui.platform.LocalDensity.current
    val line = with(d) { titleStyle.lineHeight.toDp() } + 1.dp
    // the same placement as Verdict's icon: on the first title line, following the letters down past 100 %
    val nudge = ((d.fontScale - 1f).coerceAtLeast(0f) * 2.5f).dp
    Row(Modifier.fillMaxWidth().then(if (LocalSheetCard.current) Modifier.padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 16.dp) else Modifier)) {
        Box(Modifier.height(line), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_hint, tint = c.ink2, modifier = Modifier.offset(y = nudge).size(24.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Riešenie", style = titleStyle, color = c.ink)
            Spacer(Modifier.height(3.dp))
            Rich(body, style = style, color = c.ink)
        }
    }
}

/* ---- Practice: a generated exercise ---------------------------------------------------------------------------- */

@Composable
private fun PracticeStep(s: Step.Practice, frame: Frame, store: Store, onNext: () -> Unit) {
    // the same numbers when the step is gone back to, or the lesson opened again
    val nav = LocalStepNav.current
    val seed = rememberSaveable(nav, s.id) { nav?.get("seed")?.toIntOrNull() ?: Random.nextInt() }
    LaunchedEffect(nav, seed) { nav?.put("seed", seed.toString()) }
    val ex = remember(seed) { Generators.getValue(s.gen)(Random(seed)) }
    LaunchedEffect(ex) { Telemetry.seed("gen:${ex.template}", seed.toString()) }
    val first = remember(ex) { !store.tried("gen:${ex.template}") }
    // graded once: the step gone back to shows its result, "Pokračovať" there does not count it again
    var recorded by keptBool("recorded", false, s.id)
    ExerciseView(ex, s.solo, frame, doneLabel = "Pokračovať", firstTime = first,
        // "Viem to": skipped, never a mistake; review shows the type once to check it
        onKnow = if (recorded || !store.canVouch("gen:${ex.template}")) null else { { store.vouch("gen:${ex.template}"); onNext() } }) { ok, aided ->
        if (!recorded) { recordExercise(store, ex, ok, aided); recorded = true }
        onNext()
    }
}

fun recordExercise(store: Store, ex: Exercise, ok: Boolean, aided: Boolean) {
    store.record("gen:${ex.template}", ok, aided, Mistake("gen:${ex.template}", ex.label, "Chyba v príklade: ${ex.src}", "gen:${ex.template}", currentTimeMillis()))
}

/* ---- Definition in three layers, then from memory ------------------------------------------------------------- */

@Composable
fun ConceptFlow(k: Concept, store: Store, frame: Frame, onDone: () -> Unit) {
    CompositionLocalProvider(LocalHue provides T.c.mem, LocalGlossary provides glossaryFor(k.id), LocalRefs provides refsFor(k.id), LocalReadingSelf provides setOf(k.id)) {
        val c = T.c
        val hx = rememberHaptics()
        // each screen is one in the lesson's history: phase × 100 + the example on screen
        // (phases: 0 text, 1 why, 2 examples, 3 trap, 4 write, 5 compare)
        var screen by keptSub()
        fun go(p: Int, example: Int = 0) { screen = p * 100 + example }
        var trapPick by keptPick("trap", k.id).fit(null) { it == null || it in 0..1 }
        // the paper's one value (Hodnotenie): kept, so a step left and resumed records what was said
        var papier by keptInts("concept.papier", k.id).fit(emptyList()) { it.size <= 1 }
        var recorded by keptBool("recorded", false, k.id)
        val report = { frame.onReport("concept:${k.id}") }
        val nav = LocalStepNav.current
        AnimatedContent(screen, label = "concept", transitionSpec = { StepSwap }) { sc -> StepMotionOf(nav?.forward ?: true) {
            // a screen the definition no longer has (its trap or examples gone in a new version) is the next one it has
            val p = (sc / 100).let { if (it == 3 && k.trap == null) 4 else if (it == 2 && k.judge.isEmpty()) (if (k.trap != null) 3 else 4) else it.coerceIn(0, 5) }
            val j = (sc % 100).coerceIn(0, (k.judge.size - 1).coerceAtLeast(0))
            when (p) {
                0 -> StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = { Cta("Prečo je tam každá časť", { go(1) }) }) {
                    Spacer(Modifier.height(4.dp))
                    Kicker("Definícia, presné znenie")
                    Spacer(Modifier.height(2.dp))
                    H1(k.name)
                    Spacer(Modifier.height(12.dp))
                    SourceCard(k.src, statement = true) { SourceLines(k.czech) }
                    ConceptRemarks(k)
                    SymbolHint(store, k.czech)
                    Spacer(Modifier.height(10.dp))
                    Sub("Na ${courseOfRoute("concept:${k.id}")?.test?.word?.loc ?: "teste"} ju treba napísať celú: so všetkými predpokladmi a kvantifikátormi, ako vetu.")
                    Spacer(Modifier.height(12.dp))
                }
                1 -> StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = { Cta("Príklady a protipríklady", { go(2) }) }) {
                    Spacer(Modifier.height(4.dp))
                    Label(k.name)
                    Spacer(Modifier.height(2.dp))
                    H1("Prečo je tam každá časť.")
                    Spacer(Modifier.height(12.dp))
                    k.why.forEach { w ->
                        Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).panel(18.dp, fill = c.lesson.tint2).padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Rich(w.part, style = MaterialTheme.typography.titleMedium, color = c.lesson.text)
                            Spacer(Modifier.height(4.dp))
                            Rich(w.because, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                        }
                    }
                }
                2 -> {
                    val jd = k.judge[j]
                    var jPick by kept<Boolean?>("judge$j", null, { it?.let { b -> if (b) "1" else "0" } }, { it == "1" }, k.id, j)
                    val right = jPick == jd.yes
                    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
                        footerTint = if (jPick != null) (if (right) c.goodTint else c.badTint) else null,
                        footer = {
                            AnswerBar(listOf("Áno", "Nie"), if (jd.yes) 0 else 1, jPick?.let { if (it) 0 else 1 }, { o ->
                                jPick = o == 0
                                val ok = (o == 0) == jd.yes
                                if (ok) hx.right() else hx.wrong()
                                store.record("judge:${k.id}:$j", ok, false, Mistake("concept:${k.id}", k.name, "Príklad alebo protipríklad", "concept:${k.id}", currentTimeMillis()))
                            })
                            if (jPick != null) {
                                Verdict(if (right) "Správne." else "Nie celkom.", if (right) jd.because else "${jd.wrong} ${jd.because}", right)
                                Cta(if (j + 1 < k.judge.size) "Ďalší" else "Pokračovať", {
                                    if (j + 1 < k.judge.size) go(2, j + 1) else go(if (k.trap != null) 3 else 4)
                                })
                            }
                        }) {
                        Spacer(Modifier.height(4.dp))
                        Label("${k.name} · ${j + 1} z ${k.judge.size}")
                        Spacer(Modifier.height(4.dp))
                        H1(k.question.ifBlank { "Platí to?" })
                        Spacer(Modifier.height(12.dp))
                        Column(Modifier.fillMaxWidth().panel(22.dp).padding(18.dp)) {
                            Rich(jd.tex, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
                        }
                    }
                }
                3 -> {
                    val tr = k.trap!!
                    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
                        footerTint = if (trapPick != null) (if (trapPick == 1) c.goodTint else c.badTint) else null,
                        footer = {
                            AnswerBar(listOf("Áno", "Nie"), 1, trapPick, { o -> trapPick = o; if (o == 1) hx.right() else hx.wrong() })
                            if (trapPick != null) {
                                Verdict(if (trapPick == 1) "Presne, toto je pasca." else "Toto je pasca.", tr.because, trapPick == 1)
                                Cta("Napísať z pamäti", { go(4) })
                            }
                        }) {
                        Spacer(Modifier.height(4.dp))
                        H1("Stačila by táto veta ako definícia?")
                        Spacer(Modifier.height(14.dp))
                        Column(Modifier.fillMaxWidth().panel(24.dp, fill = c.lesson.tint2).padding(20.dp)) {
                            Rich("„${tr.sentence}“", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = c.ink)
                            Spacer(Modifier.height(8.dp))
                            Label(tr.src)
                        }
                    }
                }
                4 -> StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
                    footer = {
                        Cta("Mám napísané", { go(5) }, icon = Res.drawable.ic_write)
                        // no paper at hand: the lesson goes on, review asks for it on paper later (no mistake)
                        // only before it is written: gone back to after "Hotovo", it is already on paper
                        if (!recorded) Quiet("Teraz nie", { store.putOff("concept:${k.id}"); onDone() }, color = T.hue.text)
                    }) {
                    Spacer(Modifier.height(4.dp))
                    Label("Z pamäti, najprv na papier")
                    Spacer(Modifier.height(4.dp))
                    H1(k.ask)
                    Spacer(Modifier.height(6.dp))
                    Sub("Presne, s predpokladmi, ako vetu. Až potom pokračuj.")
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = 200.dp), contentAlignment = Alignment.Center) { PaperArt(Modifier.fillMaxWidth().height(240.dp)) }
                }
                else -> StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = {
                    Cta("Hotovo", {
                        if (!recorded) {
                            // the teacher's verdict, else what the student said; nothing said: back on paper later, no mistake
                            val ok = Hodnotenie.ok(papier.firstOrNull())
                            // nothing said: back on paper later, no mistake, and the step stays open to say it on return
                            if (ok == null) store.putOff("concept:${k.id}")
                            else { store.record("concept:${k.id}", ok, false, Mistake("concept:${k.id}", k.name, "Definícia z pamäti", "concept:${k.id}", currentTimeMillis())); recorded = true }
                        }
                        onDone()
                    })
                }) {
                    Spacer(Modifier.height(4.dp))
                    H1("Porovnaj so skriptami.")
                    Spacer(Modifier.height(10.dp))
                    SourceCard(k.src, statement = true) { SourceLines(k.czech) }
                    ConceptRemarks(k)
                    Spacer(Modifier.height(14.dp))
                    // one way after the sample: the photo and a teacher's verdict, or one tap without it (NaPapier.kt)
                    if (!recorded) key("papier:concept:${k.id}") {
                        PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto(k) }, k.units, papier.firstOrNull(), { h, _ -> papier = listOfNotNull(h) }, k.extra)
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        } }
    }
}

/**
 * Under a statement whose formulas have tappable symbols: one quiet line that says so, with one of them set as in the
 * formula ("Symboly vo vzorci, napríklad ≡, sa dajú ťuknúť: …"). The symbols themselves carry no mark (a patch or
 * dots on | and ≡ read as a cursor or a typesetting error). It is said once: it counts as said only when the line
 * stood whole on the screen, above the step's panel, for a moment (at a large font it starts under the fold, and a
 * student who never scrolled to it never read it; inspector, 18. 11.), and it stays on the screen where it was read.
 * After the first symbol opened, anywhere it is shown, it is gone for good.
 */
@Composable
private fun SymbolHint(store: Store, lines: List<dev.hamper.tabula.data.Line>) {
    val gloss = dev.hamper.tabula.math.LocalGloss.current ?: return
    LaunchedEffect(gloss.open) { if (gloss.open is dev.hamper.tabula.math.Sheet.Sym && !store.symbolTapped) { store.symbolTapped = true; store.save() } }
    val seenBefore = remember { store.symbolHintSeen }
    if (store.symbolTapped || seenBefore) return
    val until = LocalSeenUntil.current
    var inView by remember { mutableStateOf(false) }
    LaunchedEffect(inView) { if (inView && !store.symbolHintSeen) { delay(1500); store.symbolHintSeen = true; store.save() } }
    val tex = remember(lines) { lines.joinToString(" ") { l -> when (l) { is dev.hamper.tabula.data.Para -> l.tex; is dev.hamper.tabula.data.Item -> l.tex; is dev.hamper.tabula.data.Block -> l.tex } } }
    // one example, a bare sign rather than one with its letters ("≡", not "a | b": "a | b a ≡" read as a sentence)
    val example = remember(tex, gloss.symbols) {
        gloss.symbols.mapNotNull { sy -> sy.regex.find(tex)?.let { it.range.first to sy } }.sortedBy { it.first }.map { it.second }
            .sortedBy { sy -> if (sy.tex.replace(Regex("""\\[a-zA-Z]+"""), "").any { it.isLetter() }) 1 else 0 }.firstOrNull()
    } ?: return
    Spacer(Modifier.height(8.dp))
    Sub("Symboly vo vzorci, napríklad §${example.tex}§, sa dajú ťuknúť: ukážu, ako sa čítajú a čo znamenajú.", Modifier.onGloballyPositioned { co ->
        val b = co.boundsInWindow()
        inView = co.size.height > 0 && b.height >= co.size.height - 1f && (until <= 0f || co.positionInRoot().y + co.size.height <= until)
    })
}

/* ---- Theorem and proof ------------------------------------------------------------------------------------------ */

private fun steps(n: Int) = "$n " + slovakPlural(n, "krok", "kroky", "krokov")

@Composable
fun TheoremFlow(t: Theorem, store: Store, frame: Frame, onDone: () -> Unit, also: List<String> = emptyList(), drill: Proof? = null) {
    // screen 0 the theorem, from 1 on its proof (ProofFlow's own screens follow)
    var screen by keptSub()
    val inLesson = LocalStepNav.current != null
    val proof = drill ?: t.proof
    CompositionLocalProvider(LocalGlossary provides glossaryFor(t.id), LocalRefs provides refsFor(t.id), LocalReadingSelf provides setOfNotNull(t.id, proof?.id) + also) {
        if (screen > 0 && proof != null) {
            // in a lesson the lesson's one back handles this (back to the theorem); elsewhere the proof closes here
            BackHandler(!inLesson) { screen = 0 }
            // a drilled proof of a statement on an "also" card (Věta 3.27 under 3.26) shows its own theorem
            ProofFlow(proof, store, frame, onDone, theorem = drill?.let(::theoremOf) ?: t, base = 1)
            return@CompositionLocalProvider
        }
        CompositionLocalProvider(LocalHue provides T.c.mem) {
            val c = T.c
            StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton { frame.onReport("theorem:${t.id}") } }, footer = {
                // one way on: prove it; skipping is possible but quieter
                if (proof != null) {
                    Cta("Dokázať · ${steps(proof.rounds.size)}", { screen = 1 }, icon = Res.drawable.ic_proof)
                    Quiet("Preskočiť dôkaz", onDone, color = quietInk(T.hue.text))
                } else Cta("Pokračovať", onDone)
            }) {
                Spacer(Modifier.height(4.dp))
                Kicker("Veta")
                Spacer(Modifier.height(2.dp))
                H1(t.name)
                Spacer(Modifier.height(12.dp))
                TheoremCard(t, named = false)
                SymbolHint(store, t.czech)
                // the idea right under the theorem it explains, before the further cards of the step: a long card
                // after it would push it below the first screen (the audit, 17. 11.)
                // in a lesson the comment is plain text: room enough that the source line above stays its block's
                Spacer(Modifier.height(if (lekcia()) 18.dp else 12.dp))
                if (lekcia()) Comment(t.idea) else Bubble(t.idea, Modifier.fillMaxWidth(), fill = c.lesson.tint2, line = c.lesson.tint)
                t.note?.let { Spacer(Modifier.height(10.dp)); Rich(it, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
                // a step about several numbered statements: each its own card with its own number and name
                if (also.isNotEmpty()) { Spacer(Modifier.height(12.dp)); ItemCards(also) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * A proof inside its step: the step counter stays, the step's segment splits into the proof's goals. What is being
 * proved stays on top ("Dokazuješ"); the goals are listed from the start, the current one open with its question,
 * the done ones checked with the line that settled them. Then on paper, against the same goals.
 */
@Composable
fun ProofFlow(pr: Proof, store: Store, frame: Frame, onDone: () -> Unit, theorem: Theorem? = null, base: Int = 0) {
    CompositionLocalProvider(
        LocalHue provides T.c.mem, LocalGlossary provides (LocalGlossary.current ?: glossaryFor(pr.id)), LocalRefs provides refsFor(pr.id),
        LocalReadingSelf provides setOfNotNull(pr.id, theorem?.id, theoremOf(pr)?.id),
    ) {
        val c = T.c; val h = T.hue
        val hx = rememberHaptics()
        // kept: a full card opened from the sheet and back again, the lesson gone back to or opened again, all find
        // the proof where it was
        var round by keptInt("proof.round", 0, pr.id).fit(0) { it in 0..pr.rounds.size }
        var wrong by keptPick("proof.wrong", pr.id).fit(null) { w -> w == null || (round < pr.rounds.size && w in pr.rounds[round].options.indices) }
        var anyWrong by keptBool("proof.anyWrong", false, pr.id)
        var paper by keptSub(base) // 0 the proof, 1 write it on paper, 2 compare; screens of the lesson's history
        var papier by keptInts("proof.papier", pr.id).fit(emptyList()) { it.size <= 1 }
        var recorded by keptBool("proof.recorded", false, pr.id)
        val inLesson = LocalStepNav.current != null
        val rounds = pr.rounds
        val n = rounds.size
        val done = round >= n
        val report = { frame.onReport("proof:${pr.id}") }
        val goal = { i: Int -> pr.goals.getOrNull(i) ?: "Krok ${i + 1}" }
        // the theorem it proves even when the proof is opened on its own (a lesson, the review)
        val th = remember(pr.id, theorem?.id) { theorem ?: theoremOf(pr) }
        // named, not numbered: "Prienik a zjednotenie podpriestorov · dôkaz, 2 z 3"
        val head = th?.name ?: pr.title
        // what this proof proves: its theorem's statement or the test's task, drawn from their parts
        // a proof of one part (Frobenius: the necessary condition) has the theorem's wording but not its parts' rounds
        // a test's proof of one point says what it proves itself (p-nula under Pozorování 2.25)
        val what = pr.wording ?: th?.wording?.let { w -> if (th.proof?.id == pr.id) w else w.copy(parts = emptyList()) }
        BackHandler(!inLesson && paper > 0) { paper-- }
        @Composable
        fun pinned() {
            // the part being proved names the bar while the rounds run; on paper it is the whole claim again
            if (what != null) Dokazujes(what, if (round < n && paper == 0) round else -1) else SourceCard(pr.src, statement = true) { Rich(pr.title, style = MaterialTheme.typography.bodyLarge, color = c.ink) }
            pr.foot?.let { Spacer(Modifier.height(4.dp)); Rich(it, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
        }
        // writing and comparing stay inside the same step: the segment stays split, all parts filled
        if (paper == 1) {
            // as with a definition: from memory first, nothing of the proof on screen
            StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, part = StepPart(n, n),
                footer = {
                    Cta("Mám napísané", { paper = 2 }, icon = Res.drawable.ic_write)
                    // no paper at hand: the lesson goes on, review asks for it on paper later (no mistake)
                    if (!recorded) Quiet("Teraz nie", { store.putOff("proof:${pr.id}"); onDone() }, color = quietInk(T.hue.text))
                }) {
                Spacer(Modifier.height(4.dp))
                Kicker("$head · na papier")
                Spacer(Modifier.height(8.dp))
                pinned()
                Spacer(Modifier.height(14.dp))
                H1("Napíš celý dôkaz sám na\u00A0papier")
                Spacer(Modifier.height(6.dp))
                Sub("Bez pozerania, rukou ako na ${courseOfRoute("proof:${pr.id}")?.test?.word?.loc ?: "teste"}.")
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().weight(1f).heightIn(min = 200.dp), contentAlignment = Alignment.Center) { PaperArt(Modifier.fillMaxWidth().height(240.dp)) }
            }
            return@CompositionLocalProvider
        }
        if (paper == 2) {
            // the same goals as the proof, and its conclusion: what the paper must have
            // one list, as on paper: each goal with the line that settles it, neutral until ticked
            val items = remember(pr.id) { dev.hamper.tabula.tutor.polozkyDokazu(pr) }
            StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, part = StepPart(n, n), footer = {
                Cta("Hotovo", {
                    if (!recorded) {
                        val naPapieri = Hodnotenie.ok(papier.firstOrNull())
                        // nothing said about the paper: the rounds alone decide only a mistake made in them
                        if (naPapieri == null && !anyWrong) store.putOff("proof:${pr.id}")
                        else { store.record("proof:${pr.id}", !anyWrong && naPapieri == true, false, Mistake("proof:${pr.id}", pr.title, "Dôkaz: ${pr.src}", "proof:${pr.id}", currentTimeMillis())); recorded = true }
                    }
                    onDone()
                })
            }) {
                Spacer(Modifier.height(4.dp))
                Kicker("$head · na papier")
                Spacer(Modifier.height(8.dp))
                pinned()
                Spacer(Modifier.height(14.dp))
                H1("Porovnaj so svojím papierom")
                Spacer(Modifier.height(14.dp))
                // one way after the proof: the photo and a teacher's verdict, or one tap without it (NaPapier.kt)
                if (!recorded) key("papier:proof:${pr.id}") {
                    PorovnanieSPapierom({ dev.hamper.tabula.tutor.miesto(pr, items) }, items, papier.firstOrNull(), { h, _ -> papier = listOfNotNull(h) })
                }
                Spacer(Modifier.height(12.dp))
            }
            return@CompositionLocalProvider
        }
        StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
            footerTint = if (wrong != null) c.badTint else null, part = StepPart((round + 1).coerceAtMost(n), n), footer = when {
                done -> { { Cta("Ďalej", { paper = 1 }) } }
                wrong != null -> { {
                    Verdict("Prečo nie", rounds[round].options[wrong!!].why, false)
                    Cta("Skúsiť znova", { wrong = null })
                } }
                else -> null
            }) {
            Spacer(Modifier.height(4.dp))
            Kicker(if (done) "$head · dôkaz hotový" else "$head · dôkaz, ${round + 1} z $n")
            Spacer(Modifier.height(8.dp))
            pinned()
            Spacer(Modifier.height(12.dp))
            if (!done) { Rich(pr.given, style = MaterialTheme.typography.bodyMedium, color = c.ink2); Spacer(Modifier.height(10.dp)) }
            Column(Modifier.fillMaxWidth().animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until n) {
                    val r = rounds[i]
                    when {
                        i < round -> GoalDone(goal(i), r.line)
                        i == round -> GoalOpen(i + 1, goal(i), flat = true) {
                            // the question is what the eye lands on: set as a task's statement, the goal's name a label
                            // over it; a prompt that only repeats the goal's title ("Neprázdnosť.") says it once, big
                            val repeats = r.prompt.trimEnd('.', ' ').lowercase() == goal(i).trimEnd('.', ' ').lowercase()
                            Rich(if (repeats) goal(i) else r.prompt, style = MaterialTheme.typography.titleLarge, color = c.ink)
                            Spacer(Modifier.height(14.dp))
                            val ord = remember(pr.id, round) { order("${pr.id}-$round", r.options.size) }
                            ord.forEach { k ->
                                val o = r.options[k]
                                Choice(o.tex, {
                                    if (wrong == null) { if (k == r.correct) { hx.right(); round++ } else { hx.wrong(); wrong = k; anyWrong = true } }
                                }, Modifier.fillMaxWidth().padding(bottom = 8.dp), state = if (wrong == k) Pick.Wrong else Pick.Idle, style = MaterialTheme.typography.bodyMedium, enabled = wrong == null || wrong == k,
                                    // the wrong pick stays above "Prečo nie" (QA on the emulator, 19. 11.: a long option
                                    // low in the part stood under the panel, only its red edge in sight)
                                    tag = if (k == r.correct && wrong == null) "correct" else null, reveal = wrong == k)
                            }
                        }
                        else -> GoalLater(i + 1, goal(i))
                    }
                }
                if (done) {
                    Conclusion(pr.end)
                    pr.warn?.let { w ->
                        Spacer(Modifier.height(4.dp))
                        Column(Modifier.fillMaxWidth().panel(16.dp, fill = h.tint2).padding(horizontal = 14.dp, vertical = 12.dp)) {
                            Rich(w, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = c.ink)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** The proof's conclusion, settled: green, with a check. */
@Composable
private fun Conclusion(end: String) {
    val c = T.c
    Row(Modifier.fillMaxWidth().background(c.goodTint, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Rich(end, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = c.good)
    }
}

@Composable
fun ProofBubble(tex: String, fill: androidx.compose.ui.graphics.Color, line: androidx.compose.ui.graphics.Color, color: androidx.compose.ui.graphics.Color) {
    Bubble(tex, fill = fill, line = line, color = color, style = MaterialTheme.typography.bodyLarge)
}

package dev.hamper.tabula.screens

import dev.hamper.tabula.system.Telemetry
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.*
import dev.hamper.tabula.data.*
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Frame of a step inside a flow: where it is, how to leave, how to report a mistake in it. */
data class Frame(val step: Int, val total: Int, val onClose: () -> Unit, val onReport: (String) -> Unit, val closeLabel: String = "Zavrieť")

/** "−4/3", "5", "−2" → exact number; null while it isn't one yet. */
fun parseFrac(s: String): Frac? {
    val t = s.replace("−", "-").trim()
    if (t.isEmpty() || t == "-") return null
    return runCatching {
        if ('/' in t) { val (a, b) = t.split('/', limit = 2); Frac.of(a.toLong(), b.toLong()) } else Frac.of(t.toLong())
    }.getOrNull()
}

/**
 * One generated exercise, part by part. [solo]: no hint; seeing the solution before the end makes the attempt
 * aided, and aided attempts never count as mastered. [firstTime]: the student never tried this problem type, so its
 * method ("Ako na to?") opens by itself if the answers still fit under it (opening it then is free); otherwise it is one
 * closed line, and opening it counts as help (docs/CONTENT.md, "The answers are on the first screen").
 */
@Composable
fun ExerciseView(
    ex: Exercise, solo: Boolean, frame: Frame, doneLabel: String = "Pokračovať", onNew: ((ok: Boolean, aided: Boolean) -> Unit)? = null, firstTime: Boolean = false,
    onKnow: (() -> Unit)? = null, onDone: (ok: Boolean, aided: Boolean) -> Unit,
) {
    // in a lesson the exercise is kept in its step (gone back to, it shows what was answered); answers that cannot be
    // read back (never expected) leave it unkept rather than half restored
    // kept only for the same problem: a new version of its generator draws other numbers from the same seed, and the
    // old answers must never be applied to them (nor an old "finished" graded as a clean answer)
    val outer = LocalStepNav.current
    val fp = remember(ex) { fingerprint(ex) }
    val nav = remember(outer, ex) {
        outer?.also { n ->
            val same = n.get("ex.fp") == fp && n.get("ex.answers")?.let { decodeAnswers(it) != null } != false
            // another problem than the one saved (or a save that cannot be read): start it fresh, kept from now on
            if (!same) n.forget("ex.")
        }
    }
    LaunchedEffect(nav, fp) { nav?.put("ex.fp", fp) }
    CompositionLocalProvider(LocalStepNav provides nav) { ExerciseBody(ex, solo, frame, doneLabel, onNew, firstTime, onKnow, onDone) }
}

@Composable
private fun ExerciseBody(
    ex: Exercise, solo: Boolean, frame: Frame, doneLabel: String, onNew: ((ok: Boolean, aided: Boolean) -> Unit)?, firstTime: Boolean,
    onKnow: (() -> Unit)?, onDone: (ok: Boolean, aided: Boolean) -> Unit,
) {
    val c = T.c
    val hx = rememberHaptics()
    val nav = LocalStepNav.current
    val answers = remember(nav, ex) { mutableStateListOf<Any>().apply { nav?.get("ex.answers")?.let(::decodeAnswers)?.let { addAll(it) } } }
    var part by keptInt("ex.part", 0, ex).fit(0) { it in 0..minOf(answers.size, ex.parts.size) }
    var wrongEver by keptBool("ex.wrong", false, ex)
    var aided by keptBool("ex.aided", false, ex)
    var hint by keptBool("ex.hint", false, ex)
    // the method, "Ako na to?": one closed line; open on the first encounter of this type (free, also when opened by
    // hand then), unless it would push the answers below the fold; any other opening counts as help
    val firstLook = firstTime && !solo
    var method by keptBool("ex.method", firstLook, ex)
    // open by itself above the answers (a first encounter it fits); opened by the student it sits under them
    var above by keptBool("ex.above", firstLook, ex)
    // decided while the screen settles (the footer is measured a frame later), before the student does anything
    var fitDecided by keptBool("ex.fit", !firstLook, ex)
    var solution by keptBool("ex.solution", false, ex)
    // a right part is kept (its answer is in [answers]); a wrong one comes back as a fresh try, the miss already counted
    var verdict by kept<Boolean?>("ex.verdict", null, { if (it == true) "1" else null }, { if (it == "1") true else null }, ex)
    // this part was answered wrong before: its right answer is "Teraz je to správne", quieter and without the series
    var missedPart by keptBool("ex.missed", false, ex, part)
    val finished = part >= ex.parts.size
    // a problem of several parts done: its own sound once the last part's "right" has played (ui/Sound.kt)
    // only when it gets done here, not when a finished one is shown again (back to the step)
    val doneBefore = remember(ex) { finished }
    LaunchedEffect(finished) { if (finished && !doneBefore && ex.parts.size > 1) { kotlinx.coroutines.delay(450); hx.stepDone() } }
    val current = if (!finished) remember(ex, part) { ex.parts[part](answers.toList()) } else null
    // input state of the current part
    val fields = remember(ex, part) { mutableStateListOf<String>().apply { (current?.answer as? Answer.Numbers)?.labels?.forEach { add("") } } }
    var field by remember(ex, part) { mutableIntStateOf(0) }
    val picks = remember(ex, part) { mutableStateListOf<Int>() }
    var choice by remember(ex, part) { mutableStateOf<Int?>(null) }
    val vecs = remember(ex, part) { (current?.answer as? Answer.Vectors)?.let(::VectorsState) }
    // a part on paper: written first, then the model and one way to say it (NaPapier.kt); the student's word or the tutor's
    var paperOpen by keptBool("ex.paper", false, ex, part)
    var paperSaid by remember(ex, part) { mutableStateOf<Int?>(null) }

    // the tutor (docs/TUTOR.md): on every exercise; after a wrong answer it knows the answer and the app's feedback
    var zla by remember(ex, part) { mutableStateOf<TutorOtvor?>(null) }
    val kurzTutora = remember(ex) { dev.hamper.tabula.data.courseOfRoute("gen:${ex.template}")?.id ?: if (ex.template.startsWith("dml")) "dml" else "la1" }
    val miestoTutora: () -> dev.hamper.tabula.tutor.MiestoAppky = { dev.hamper.tabula.tutor.miesto(ex, answers.toList(), zla?.odpoved, zla?.vazba, kurzTutora) }

    // statistics: what a wrong answer was, as entered
    val key = "gen:${ex.template}"
    fun judge(ok: Boolean, value: Any, entered: String = value.toString()) {
        if (!ok) Telemetry.given(key, entered)
        fitDecided = true
        verdict = ok
        if (ok) { if (missedPart) hx.rightAgain() else hx.right(); answers.add(value); nav?.put("ex.answers", encodeAnswers(answers)) } else { hx.wrong(); wrongEver = true; missedPart = true }
    }

    fun toggleMethod() {
        // free on a first encounter and after the last part is already right; otherwise it is help
        val done = verdict == true && part + 1 >= ex.parts.size
        if (!method && !firstLook && !done) aided = true
        if (!method) Telemetry.help("howto", key)
        if (method) above = false
        method = !method; fitDecided = true
    }
    // the hint of the part on screen; a part of a longer exercise without its own offers none
    val partHint = current?.let { it.hint ?: ex.hint.takeIf { ex.parts.size == 1 } }
    // what a tap on the help opened (the method under the answers, the hint) comes into view above the footer
    val bringHelp = remember { BringIntoViewRequester() }
    val helpScope = rememberCoroutineScope()

    CompositionLocalProvider(LocalHue provides c.gem, LocalTutorMiesto provides miestoTutora) {
        StepFrame(
            frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel,
            trailing = { ReportButton { frame.onReport(ex.template) } },
            footerTint = if (finished) (if (!wrongEver) c.goodTint else c.badTint) else when (verdict) { true -> c.goodTint; false -> c.badTint; null -> null },
            footer = {
                when {
                    finished -> {
                        Verdict(if (!wrongEver && !aided) "Celé správne." else if (!wrongEver) "Správne, s pomocou." else "Hotovo, s chybou.",
                            if (aided) "S nápovedou alebo riešením sa to neráta ako zvládnuté. Príde to znova." else if (wrongEver) "Príde to znova v opakovaní." else "Bez pomoci, takto sa to ráta.", !wrongEver, cheer = !wrongEver && !aided)
                        if (onNew != null) ButtonRow {
                            Second("Nové čísla", { onNew(!wrongEver, aided) })
                            Cta(doneLabel, { onDone(!wrongEver, aided) })
                        } else Cta(doneLabel, { onDone(!wrongEver, aided) })
                    }
                    verdict == true -> {
                        Verdict("Správne.", current?.after ?: (current?.answer as? Answer.Choice)?.why, true, cheer = !wrongEver && !aided)
                        // the next part starts clean: the hint and the method were about the part just answered (Teo, 17. 11.)
                        Cta(if (part + 1 < ex.parts.size) "Ďalšia časť" else "Dokončiť", { verdict = null; hint = false; method = false; above = false; part++ })
                    }
                    verdict == false -> {
                        val a = current!!.answer
                        // the right one first, so it is in the short line; what the wrong answer most likely got wrong is the "Prečo?"
                        val shown = when (a) {
                            is Answer.Numbers -> listOfNotNull("Správne je ${a.shown}.", fields.map(::parseFrac).takeIf { v -> v.all { it != null } }?.let { v -> a.diagnose?.invoke(v.map { it!! }) }).joinToString(" ")
                            is Answer.Choice -> listOfNotNull(choice?.let { a.wrong[it] }, a.why).joinToString(" ")
                            is Answer.Pick -> a.why(picks.toSet())
                            is Answer.Vectors -> vecs?.result?.head
                            is Answer.Paper -> a.miss
                        }
                        Verdict("Tentoraz nie.", shown, false)
                        // the tutor adds to the feedback above, never replaces it: photograph the work, it finds where it went wrong
                        val otvor = TutorOtvor(answerShown(a, fields, choice), shown)
                        SideEffect { zla = otvor }
                        OdfotRiesenie(miestoTutora, quiet = true, label = "Odfoť postup, Pytagoras nájde chybu")
                        ButtonRow {
                            if (!solution) Second("Riešenie", { solution = true; aided = true; Telemetry.help("solution", key) }, line = c.badLine)
                            Cta("Skúsiť znova", { zla = null; verdict = null; choice = null; paperSaid = null; picks.clear(); for (i in fields.indices) fields[i] = ""; field = 0 })
                        }
                    }
                    else -> when (val a = current!!.answer) {
                        is Answer.Choice -> AnswerBar(a.options, a.correct, choice, { k -> choice = k; judge(k == a.correct, k, a.options[k]) })
                        is Answer.Numbers -> {
                            KeyPad(Pad, off = padOff(a.p, a.fractions), onKey = { k ->
                                val s = fields[field]
                                fields[field] = when (k) {
                                    "⌫" -> s.dropLast(1)
                                    "−" -> if (s.startsWith("−")) s.drop(1) else "−$s"
                                    "/" -> if ('/' in s || s.isEmpty()) s else "$s/"
                                    else -> if (a.p != null) k else if (s.length < 6) s + k else s
                                }
                                // in Z_p a cell is one digit: jump to the next one
                                if (a.p != null && k != "⌫" && field < fields.size - 1) field++
                            })
                            val vals = fields.map(::parseFrac)
                            Cta("Skontrolovať", { judge(vals == a.expected, a.expected, fields.joinToString("; ")) }, enabled = vals.all { it != null })
                        }
                        is Answer.Pick -> Cta("Skontrolovať", { judge(a.ok(picks.toSet()), picks.toSet(), picks.sorted().joinToString("; ") { a.options[it] }) }, enabled = picks.size in a.count)
                        // a wrong list stays as it was: the student fixes the entries marked wrong
                        is Answer.Vectors -> VectorsPad(vecs!!) { val list = vecs.submit(); val res = a.check(list); vecs.result = res; vecs.stale.clear(); judge(res.ok, list) }
                        // first the paper, then the model: the button only opens it, the verdict is said under it
                        // the student's word or the tutor's verdict can change until "Pokračovať" (NaPapier.kt: the last word counts)
                        is Answer.Paper -> if (!paperOpen) Cta("Mám napísané, ukáž vzor", { paperOpen = true; fitDecided = true }, icon = Res.drawable.ic_write)
                            else Hodnotenie.ok(paperSaid).let { ok -> Cta("Pokračovať", { if (ok != null) judge(ok, 1, if (ok) "mám" else "nemám") }, enabled = ok != null) }
                    }
                }
            },
        ) {
            // decided once, on the first layout with the footer measured (StepFrame provides that, so it is read in
            // here, inside it); any interaction before that closes the decision as well
            val measured = LocalFooterMeasured.current
            // the last position seen of the first answer: the footer may be measured only after it was placed, and
            // then the answer is not placed again, so the decision waits for both (an open method would push it down)
            var whole by remember(ex, part) { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(measured, whole) {
                val w = whole ?: return@LaunchedEffect
                if (!fitDecided && measured) { fitDecided = true; if (!w) { method = false; above = false } }
            }
            val fit: (Boolean) -> Unit = { w -> whole = w; if (!fitDecided && measured) { fitDecided = true; if (!w) { method = false; above = false } } }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Label(ex.label + if (solo) " · bez pomoci" else "", Modifier.weight(1f))
                // "Viem to": before anything is answered; skipped, not a mistake, checked once in review
                if (onKnow != null && part == 0 && verdict == null && !wrongEver && !aided && !solution) KnowButton(onKnow)
            }
            Spacer(Modifier.height(6.dp))
            val bringTask = remember { BringIntoViewRequester() }
            Column(Modifier.fillMaxWidth().bringIntoViewRequester(bringTask)) {
            SourceCard(ex.src) {
                KeepWhole { m -> Rich(ex.prompt, m, style = MaterialTheme.typography.bodyLarge, color = c.ink) }
                ex.visual?.let { Spacer(Modifier.height(4.dp)) }
            }
            // how it is done, as the goals the solution will check: above the answers only when it opened by itself
            if (!solo && !finished && method && above) {
                Spacer(Modifier.height(10.dp))
                PostupCard(ex.postup, true, ::toggleMethod, now = current?.aim)
            }
            Spacer(Modifier.height(14.dp))
            // parts already done
            for (i in 0 until minOf(part, ex.parts.size)) {
                val p = remember(ex, i) { ex.parts[i](answers.take(i)) }
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.width(24.dp).height(24.dp), contentAlignment = Alignment.CenterStart) { Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(18.dp)) }
                    Column(Modifier.weight(1f)) {
                        Rich(p.q, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                        Rich(answerText(p.answer, answers.getOrNull(i)), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    }
                }
            }
            if (current != null) {
                // after "Ďalšia časť" the new part comes into view (only as far as needed): below a long Ako na to card
                // the keypad stood under a screen with no field in sight. The first part leaves the prompt on top.
                val bring = remember { BringIntoViewRequester() }
                // the window's top then stops on a line edge of the prompt, never through its letters; in a scope of its
                // own, since the scroll itself brings the answer into view and so restarts the effect that started it
                val align = LocalAlignTop.current
                val bringScope = rememberCoroutineScope()
                LaunchedEffect(part) { if (part > 0) { delay(250); bringScope.launch { bring.bringIntoView(); align?.invoke() } } }
                // the first part too, when a long prompt and the keypad leave its first answer under the fold (docs/CONTENT.md,
                // "The answers are on the first screen"): as far the prompt and the first answer fit together; when they do not, the prompt stays whole on top
                // (Teo, 18. 11.: a task opened half under the top bar hid what was asked)
                // once per exercise: `whole` changes as the student scrolls, and a later scroll must not be pulled back
                var brought by remember(ex) { mutableStateOf(false) }
                LaunchedEffect(part, whole, measured) { if (part == 0 && measured && whole == false && !brought) { brought = true; bringTask.bringIntoView() } }
                Column(Modifier.fillMaxWidth().bringIntoViewRequester(bring)) {
                    Spacer(Modifier.height(4.dp))
                    // a choice's options sit in the footer, always in view: there the question itself must fit under the method
                    // "Ako na to?" and "Nápoveda" on the question's own line (QuestionWithHelp): right above the answer at
                    // every width and font, never under the keypad, which covered them at 360 dp when they stood under the
                    // answer (design review 18. 11.), and costing the answer's field no room. Only while the part is unanswered.
                    QuestionWithHelp(
                        part,
                        question = { Rich(current.q, Modifier.then(if (current.answer is Answer.Choice) Modifier.testTag(QuestionTag).onGloballyPositioned { fit(it.wholeOnScreen()) } else Modifier), style = MaterialTheme.typography.titleLarge, color = c.ink) },
                        howTo = if (!solo && verdict == null && !method) ({ toggleMethod(); helpScope.launch { delay(120); bringHelp.bringIntoView() } }) else null,
                        askHint = if (!solo && verdict == null && !hint && partHint != null) ({ hint = true; aided = true; fitDecided = true; Telemetry.help("hint", key); helpScope.launch { delay(120); bringHelp.bringIntoView() } }) else null,
                    )
                    Spacer(Modifier.height(10.dp))
                    when (val a = current.answer) {
                        is Answer.Numbers -> CellRows(a.labels.size, MaterialTheme.typography.titleLarge, 8.dp) { i, m ->
                            val l = a.labels[i]
                            Choice(if (fields.getOrNull(i).isNullOrEmpty()) "$$l$" else fields[i].replace("-", "−"), { field = i }, m.bump(fields.getOrNull(i)).then(if (i == 0) Modifier.firstAnswer(fit) else Modifier), state = if (field == i && verdict == null) Pick.Picked else Pick.Idle, center = true, minHeight = 56.dp, style = MaterialTheme.typography.titleLarge, enabled = verdict == null, tag = "field$i=${a.expected[i]}", hint = fields.getOrNull(i).isNullOrEmpty())
                        }
                        is Answer.Pick -> Column {
                            val good = remember(a) { (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.firstOrNull { it.size in a.count && a.ok(it) } ?: emptySet() }
                            Sub(if (a.count.first == a.count.last) "Vyber ${a.count.first}." else "Vyber, koľko treba.")
                            Spacer(Modifier.height(8.dp))
                            a.options.forEachIndexed { i, o -> CheckRow(o, i in picks, { on -> if (verdict == null) { if (on) picks.add(i) else picks.remove(i) } }, Modifier.padding(bottom = 8.dp).then(if (i == 0) Modifier.firstAnswer(fit) else Modifier), tag = if (i in good) "pick" else null) }
                        }
                        is Answer.Vectors -> VectorsInput(vecs!!, enabled = verdict == null, firstAnswer = Modifier.firstAnswer(fit))
                        is Answer.Paper -> if (!paperOpen) Sub("Napíš to na papier vlastnými slovami. Vzor ukážem, až budeš mať.", Modifier.firstAnswer(fit))
                            else Column {
                                SourceCard("Vzor") { a.sample.forEach { l -> Rich(l, style = MaterialTheme.typography.bodyLarge, color = c.ink); Spacer(Modifier.height(6.dp)) } }
                                // stays after the verdict: the tutor's reasons on a photo are what explains a "neuznal by som"
                                Spacer(Modifier.height(6.dp))
                                PorovnanieSPapierom(miestoTutora, a.look, paperSaid, { h, _ -> if (verdict == null) paperSaid = h })
                            }
                        is Answer.Choice -> {}
                    }
                }
                Column(Modifier.fillMaxWidth().bringIntoViewRequester(bringHelp)) {
                    if (hint && partHint != null) { Spacer(Modifier.height(6.dp)); Bubble(partHint, Modifier.fillMaxWidth(), fill = c.lesson.tint2, line = c.gem.tint) }
                    if (method && !above && !solo) { Spacer(Modifier.height(8.dp)); PostupCard(ex.postup, true, ::toggleMethod, now = current.aim) }
                }
            }
            } // the task, from the prompt to the part being answered
            if (solution || finished) {
                Spacer(Modifier.height(14.dp))
                Text(if (finished) "Riešenie, ako na papier" else "Riešenie", style = MaterialTheme.typography.titleMedium, color = c.ink)
                Spacer(Modifier.height(8.dp))
                SolutionView(remember(ex, answers.size) { ex.solution(answers.toList()) })
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** A wrong answer for the tutor: what the student answered and the feedback the app gave to it. */
private data class TutorOtvor(val odpoved: String?, val vazba: String?)

/** The wrong answer as the student entered it, for the tutor's context. */
private fun answerShown(a: Answer, fields: List<String>, choice: Int?): String? = when (a) {
    is Answer.Numbers -> fields.joinToString(", ").ifBlank { null }
    is Answer.Choice -> choice?.let { a.options.getOrNull(it) }
    else -> null
}

private fun answerText(a: Answer, v: Any?): String = when (a) {
    is Answer.Numbers -> a.shown
    is Answer.Choice -> (v as? Int)?.let { a.options[it] } ?: ""
    is Answer.Pick -> (v as? Set<*>)?.mapNotNull { (it as? Int)?.let { i -> a.options[i] } }?.joinToString(", ") ?: ""
    is Answer.Vectors -> vectorsText(v)
    is Answer.Paper -> "Na papieri."
}

/**
 * [n] answer cells in rows of as many as fit with a readable label in [style] (eight fields of two vectors on a narrow
 * phone at a large font would leave a cell no room at all); every row shares the width evenly, the last one keeps the
 * same cell width. [cell] draws cell i with the modifier that gives it its share.
 */
@Composable
internal fun CellRows(n: Int, style: androidx.compose.ui.text.TextStyle, gap: androidx.compose.ui.unit.Dp, cell: @Composable (Int, Modifier) -> Unit) {
    androidx.compose.foundation.layout.BoxWithConstraints {
        // four components of ℤ₅⁴ fit one row at 360 dp (a fourth alone on a second row hid under the keypad); rows are
        // balanced, never three and a lone one
        val min = 24.dp + with(androidx.compose.ui.platform.LocalDensity.current) { (style.fontSize * 2.2f).toDp() }
        val fits = ((maxWidth + gap) / (min + gap)).toInt().coerceIn(1, maxOf(1, n))
        val rows = (n + fits - 1) / fits
        val per = (n + rows - 1) / rows
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            for (row in (0 until n).chunked(per)) Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                for (i in row) cell(i, Modifier.weight(1f))
                repeat(per - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** "Viem to": a quiet pill beside the problem's label; the problem is skipped without counting as a miss. */
@Composable
private fun KnowButton(onClick: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    Box(
        Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClickLabel = "Preskočiť príklad") { hx.tap(); onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text("Viem to", style = MaterialTheme.typography.labelLarge, color = T.hue.text) }
}

/**
 * The answers of an exercise in the lesson's memory: numbers, a picked option, picked options, vectors. Null for
 * a value of another kind (then the exercise is not kept, never half restored).
 */
fun encodeAnswers(list: List<Any>): String = dev.hamper.tabula.compat.json.JSONArray().apply {
    for (v in list) put(dev.hamper.tabula.compat.json.JSONObject().apply {
        when {
            v is Int -> put("i", v)
            v is Set<*> -> put("s", dev.hamper.tabula.compat.json.JSONArray(v.map { it as Int }))
            v is List<*> && v.isEmpty() -> put("e", 0)
            v is List<*> && v.first() is Frac -> put("f", dev.hamper.tabula.compat.json.JSONArray(v.map { fracText(it as Frac) }))
            v is List<*> && v.first() is List<*> -> put("v", dev.hamper.tabula.compat.json.JSONArray(v.map { g -> dev.hamper.tabula.compat.json.JSONArray((g as List<*>).map { vec -> dev.hamper.tabula.compat.json.JSONArray((vec as List<*>).map { fracText(it as Frac) }) }) }))
            else -> put("x", 0)
        }
    })
}.toString()

/**
 * What makes a saved attempt this problem's: the assignment, the number of parts and the first part as asked (its
 * options and their order, the numbers expected), so a new version of the generator never gets old answers.
 */
private fun fingerprint(ex: Exercise): String {
    val first = runCatching { ex.parts.firstOrNull()?.invoke(emptyList()) }.getOrNull()
    val a = when (val x = first?.answer) {
        is Answer.Choice -> "c" + x.options.joinToString("|") + "#" + x.correct
        is Answer.Numbers -> "n" + x.labels.joinToString("|") + "#" + x.expected.joinToString("|") { fracText(it) }
        is Answer.Pick -> "p" + x.options.joinToString("|")
        is Answer.Vectors -> "v" + x.cells.joinToString("|") + x.count
        is Answer.Paper -> "w" + x.sample.joinToString("|")
        null -> ""
    }
    return "${ex.parts.size}:" + (ex.prompt + "\u0000" + (first?.q ?: "") + "\u0000" + a).hashCode()
}

private fun fracText(f: Frac) = "${f.num}/${f.den}"
private fun fracOf(s: String): Frac = s.split('/').let { (n, d) -> Frac.of(n.toLong(), d.toLong()) }

fun decodeAnswers(s: String): List<Any>? = runCatching {
    val a = dev.hamper.tabula.compat.json.JSONArray(s)
    (0 until a.length()).map { i ->
        val o = a.getJSONObject(i)
        fun arr(k: String) = o.getJSONArray(k)
        when {
            o.has("i") -> o.getInt("i")
            o.has("s") -> arr("s").let { x -> (0 until x.length()).map { x.getInt(it) }.toSet() }
            o.has("e") -> emptyList<Any>()
            o.has("f") -> arr("f").let { x -> (0 until x.length()).map { fracOf(x.getString(it)) } }
            o.has("v") -> arr("v").let { g -> (0 until g.length()).map { gi -> g.getJSONArray(gi).let { vs -> (0 until vs.length()).map { vi -> vs.getJSONArray(vi).let { f -> (0 until f.length()).map { fracOf(f.getString(it)) } } } } } }
            else -> error("unknown answer")
        }
    }
}.getOrNull()

/**
 * The keys of the number pad, laid out as on a phone: 1 2 3 on top, 0 under 8, minus left of 0, delete right of it;
 * the fraction bar in a fourth column next to the digits. In Z_p only the digits there are, in order, delete last.
 */
/**
 * One layout for every number field, as on a calculator, so the hand knows where each key is (Teo, 17. 11.):
 * 1 2 3 / 4 5 6 / 7 8 9 / − 0 / ⌫. A key the field cannot take stays where it is, dimmed ([padOff]).
 */
val Pad = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("−", "0", "/", "⌫"))

/** The keys a field cannot take: in ℤ_p the digits from p up, the sign and the fraction bar; elsewhere the bar without fractions. */
fun padOff(p: Int?, fractions: Boolean): Set<String> = when {
    p != null -> (p..9).map { "$it" }.toSet() + "−" + "/"
    !fractions -> setOf("/")
    else -> emptySet()
}

/**
 * A part's question with its help: "Ako na to?" (the method) and "Nápoveda", each only while it is not open. Both are
 * links that open something under the answer, alike, with the same arrow (inspector, 18. 11.). Where they go:
 * - both at the end of the question's line, when they fit there beside it;
 * - else folded into one "Pomoc" beside the question (a phone of 360 dp); a tap lays the two out under the question;
 * - else both on a line of their own under the question (a long question, a large font).
 * So the help is always beside or right under what is asked, and on a narrow phone it takes no line of its own.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun QuestionWithHelp(part: Int, question: @Composable () -> Unit, howTo: (() -> Unit)?, askHint: (() -> Unit)?) {
    val links = listOfNotNull(howTo?.let { "Ako na to?" to it }, askHint?.let { "Nápoveda" to it })
    // "Pomoc" tapped: the two links under the question, for this part
    var unfolded by remember(part) { mutableStateOf(false) }
    if (links.isEmpty()) { question(); return }
    val style = MaterialTheme.typography.titleSmall
    val tm = androidx.compose.ui.text.rememberTextMeasurer()
    val d = androidx.compose.ui.platform.LocalDensity.current
    // a link: its words, 2 dp, a 20 dp arrow, 8 dp of padding on each side
    fun linkW(t: String) = tm.measure(t, style).size.width + with(d) { (2 + 20 + 16).dp.roundToPx() }
    val gap = with(d) { 8.dp.roundToPx() }
    androidx.compose.ui.layout.SubcomposeLayout(Modifier.fillMaxWidth()) { c ->
        val loose = c.copy(minWidth = 0)
        val q = subcompose("q", question).map { it.measure(loose) }
        val qw = q.maxOfOrNull { it.width } ?: 0
        val qh = q.maxOfOrNull { it.height } ?: 0
        val both = links.sumOf { linkW(it.first) }
        val mode = when {
            unfolded -> 2
            qw + gap + both <= c.maxWidth -> 0
            links.size > 1 && qw + gap + linkW("Pomoc") <= c.maxWidth -> 1
            else -> 2
        }
        val h = subcompose("help$mode") {
            // under the question each keeps its words whole: side by side when they fit, else under each other
            // (320 dp at 200 %: about 377 dp for the two in 280 dp)
            if (mode == 1) HelpLink("Pomoc") { unfolded = true } else FlowRow { links.forEach { (t, f) -> HelpLink(t, f) } }
        }.map { it.measure(loose) }
        val hw = h.maxOfOrNull { it.width } ?: 0
        val hh = h.maxOfOrNull { it.height } ?: 0
        if (mode == 2) layout(c.maxWidth, qh + hh) {
            q.forEach { it.place(0, 0) }
            h.forEach { it.place(0, qh) }
        } else {
            // the line is as tall as the question: the link's 48 dp touch area reaches into the gaps above and below,
            // so the help beside it pushes the answer's field down by nothing
            layout(c.maxWidth, qh) {
                q.forEach { it.place(0, 0) }
                h.forEach { it.place(c.maxWidth - hw, (qh - hh) / 2) }
            }
        }
    }
}

@Composable
private fun HelpLink(text: String, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = c.ink2, softWrap = false)
        Spacer(Modifier.width(2.dp))
        Icon24(Res.drawable.ic_chevron, tint = c.ink2, modifier = Modifier.size(20.dp))
    }
}

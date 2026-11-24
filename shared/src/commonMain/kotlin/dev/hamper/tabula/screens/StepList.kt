package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import dev.hamper.tabula.ui.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.state.LessonRun
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.*
import kotlinx.coroutines.launch

/** What a step is and its name, for the list of steps: "Definícia" · "Lineárny obal". */
fun stepTitle(s: Step): Pair<String, String> = when (s) {
    is Step.Ask -> "Odhad" to s.q
    is Step.Explore -> "Obrázok" to s.title
    is Step.Explain -> "Výklad" to s.title
    is Step.Worked -> "Riešený príklad" to s.title
    is Step.Check -> "Otázka" to s.q
    is Step.Practice -> (if (s.solo) "Príklad bez pomoci" else "Príklad") to (s.title ?: (La1Templates + Courses.flatMap { it.templates }).firstOrNull { it.gen == s.gen }?.title ?: "Príklad")
    is Step.Clip -> "Klip z prednášky" to (ClipLibrary.all.firstOrNull { it.id == s.clip }?.before ?: "Klip")
    is Step.Def -> "Definícia" to (ConceptById[s.concept]?.name ?: s.concept)
    is Step.Thm -> "Veta" to (TheoremById[s.theorem]?.name ?: s.theorem)
    is Step.Prove -> "Dôkaz" to (ProofById[s.proof]?.title ?: s.proof)
}

/**
 * The lesson's contents (Teo, 18. 11.: "mohol by som mať možnosť skipnúť na ďalšie kroky a ich vidieť"; 24. 11.: not
 * found under the bar): its steps in the pieces the bar is cut into, each piece headed by its own segment. A tap goes
 * straight to a step, back or ahead, answers kept. The name leads, its kind under it; the step on screen is tinted, the
 * ones done carry a check (gone on from, or left out because their type is mastered). Skipping asks for nothing; what
 * was skipped is just not done (state/LessonRun.kt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepList(lesson: LessonDef, pieces: List<IntRange>, run: LessonRun, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val c = T.c
    // the lesson stays in sight above it: a tap there closes it, also where there is no back button (the web on an iPhone)
    val tall = with(LocalDensity.current) { (LocalWindowInfo.current.containerSize.height * .8f).toDp() }
    val sheet = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()
    val going = remember { booleanArrayOf(false) }
    ModalBottomSheet(
        onDismiss, sheetState = sheet, containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth,
        // the library's own handle shows an English tooltip under a mouse pointer, over the lesson's name
        dragHandle = { Box(Modifier.padding(top = 14.dp, bottom = 12.dp).size(36.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(c.line)) },
    ) {
        // the sheet slides away first: gone at once, the second half of a double tap answered the step under it
        StepContents(lesson, pieces, run, Modifier.heightIn(max = tall)) { k ->
            if (!going[0]) { going[0] = true; scope.launch { sheet.hide() }.invokeOnCompletion { onPick(k) } }
        }
    }
}

/** What the contents sheet holds: it opens with the step on screen in sight. */
@Composable
fun StepContents(lesson: LessonDef, pieces: List<IntRange>, run: LessonRun, modifier: Modifier = Modifier, onPick: (Int) -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    val heads = pieces.size > 1
    // the row two above the step on screen; near the start the list simply starts at its top
    val from = remember { if (run.at < 4) 0 else 1 + run.at + (if (heads) pieces.indexOfFirst { run.at in it } + 1 else 0) - 2 }
    LazyColumn(modifier.fillMaxWidth().padding(horizontal = 12.dp), rememberLazyListState(from)) {
        item { Text(lesson.name, style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 4.dp).semantics { heading() }) }
        pieces.forEachIndexed { p, steps ->
            if (heads) item {
                // the piece as the bar draws it: its segment, filled by what is done of it
                val done = steps.count { run.done(it) }
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, end = 10.dp, top = 14.dp, bottom = 4.dp)
                        .semantics(mergeDescendants = true) { heading(); stateDescription = "hotové $done z ${steps.count()}" },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Kúsok ${p + 1}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = c.ink2)
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.sand)) {
                        if (done > 0) Box(Modifier.fillMaxWidth(done.toFloat() / steps.count()).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(c.lesson.main))
                    }
                }
            }
            items(steps.toList()) { k ->
                val (kind, name) = remember(lesson.id, k) { stepTitle(lesson.steps[k]) }
                val now = k == run.at
                val done = run.done(k)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 2.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (now) T.hue.tint else c.card)
                        .clickable(role = Role.Button, onClickLabel = "Prejsť na krok") { hx.tap(); onPick(k) }
                        .semantics { selected = now; if (done) stateDescription = "hotové" }
                        .heightIn(min = 56.dp).padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Rich(name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (now) FontWeight.SemiBold else null), color = c.ink)
                        Text(if (now) "$kind · tu si" else kind, style = MaterialTheme.typography.labelMedium, color = if (now) T.hue.text else c.ink2)
                    }
                    if (done) Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.padding(start = 10.dp).size(20.dp))
                }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding().height(8.dp)) }
    }
}

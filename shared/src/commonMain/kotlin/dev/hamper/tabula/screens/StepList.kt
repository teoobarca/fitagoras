package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import dev.hamper.tabula.ui.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.state.LessonRun
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.*

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
 * Every step of the lesson by name (Teo, 18. 11.: "mohol by som mať možnosť skipnúť na ďalšie kroky a ich vidieť"): a
 * tap goes straight there, back or ahead, answers kept. The number is a small figure in the row, not a filled circle
 * ("tie číselká" looked heavy); the name leads, its kind under it; the step on screen is tinted, the ones seen carry a
 * check. Steps jumped over are not seen: the lesson is done once they are, or with "Viem to" (screens/Steps.kt).
 * Steps the lesson left out (a type already mastered) are not listed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepList(lesson: LessonDef, run: LessonRun, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    ModalBottomSheet(onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth) {
        Column(Modifier.padding(horizontal = 12.dp).navigationBarsPadding().verticalScroll(rememberScrollState())) {
            Text(lesson.name, style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(Modifier.height(2.dp))
            Text("Ťukni na krok a si tam, aj dopredu. Lekcia bude hotová, keď prejdeš aj preskočené.", style = MaterialTheme.typography.bodyMedium, color = c.ink2, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(Modifier.height(10.dp))
            for (k in lesson.steps.indices) {
                if (run.skipped(k)) continue
                val (kind, name) = stepTitle(lesson.steps[k])
                val now = k == run.at
                val seen = run.visited(k)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 2.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (now) T.hue.tint2 else c.card)
                        .clickable(role = Role.Button, onClickLabel = "Prejsť na krok") { hx.tap(); onPick(k) }
                        .semantics { selected = now }
                        .heightIn(min = 56.dp).padding(start = 8.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // the number as a figure of the row, set like the kind under the name
                    Text("${k + 1}", Modifier.width(28.dp).padding(top = 3.dp), style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (now) FontWeight.Bold else FontWeight.Normal, fontFeatureSettings = "tnum"), color = if (now) T.hue.text else c.ink3)
                    Column(Modifier.weight(1f)) {
                        Rich(name, style = MaterialTheme.typography.bodyLarge, color = if (seen || now) c.ink else c.ink2)
                        Text(if (now) "$kind · tu si" else kind, style = MaterialTheme.typography.labelMedium, color = if (now) T.hue.text else c.ink3)
                    }
                    if (seen && !now) Icon24(Res.drawable.ic_check, tint = c.ink3, modifier = Modifier.padding(start = 8.dp, top = 2.dp).size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

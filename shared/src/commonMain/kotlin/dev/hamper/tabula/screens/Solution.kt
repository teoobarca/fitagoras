package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.Postup
import dev.hamper.tabula.data.SolStep
import dev.hamper.tabula.data.Solution
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A solution as the goals of its method, the same in practice, in a lesson's worked example and on a paper: each goal
 * of the Postup numbered, the reached ones checked with their steps. [shown] reveals the first steps only (a worked
 * example line by line): the goal being worked on is open, the ones ahead are quiet.
 */
@Composable
fun SolutionView(sol: Solution, modifier: Modifier = Modifier, shown: Int = sol.steps.size, follow: Boolean = false) {
    val visible = sol.steps.take(shown)
    val all = shown >= sol.steps.size
    // revealed step by step: the goal of the newest step comes into view, also when it is already done (one step)
    val newest = visible.lastOrNull()?.aim
    val bring = remember { BringIntoViewRequester() }
    if (follow && shown > 1) LaunchedEffect(shown) { kotlinx.coroutines.delay(250); bring.bringIntoView() }
    val gemSteps = remember(sol) { HashMap<Any, Int>() }
    androidx.compose.runtime.CompositionLocalProvider(LocalGemSteps provides gemSteps) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sol.postup.aims.forEachIndexed { i, aim ->
            val mine = sol.steps.filter { it.aim === aim }
            val seen = visible.filter { it.aim === aim }
            Box(if (follow && aim === newest) Modifier.bringIntoViewRequester(bring) else Modifier) { when {
                mine.isEmpty() && all -> GoalLater(i + 1, aim.title, "Netreba, odpoveď už je jasná.")
                // the goal of the newest step says so to a screen reader; it looks as any goal reached (1.6.2, Teo 19. 11.)
                mine.isNotEmpty() && seen.size == mine.size -> GoalDone(aim.title, if (lekcia() && follow && aim === newest && !all) Modifier.semantics { stateDescription = "Teraz" } else Modifier) { Steps(seen) }
                seen.isNotEmpty() -> GoalOpen(i + 1, aim.title) { Steps(seen); Spacer(Modifier.height(6.dp)) }
                else -> GoalLater(i + 1, aim.title)
            } }
        }
    }
    }
}

/** The steps under one goal: the sentence, then what goes on paper. */
@Composable
private fun ColumnScope.Steps(steps: List<SolStep>) {
    val c = T.c
    steps.forEachIndexed { k, s ->
        if (k > 0) Spacer(Modifier.height(8.dp))
        Rich(s.say, style = MaterialTheme.typography.bodyMedium, color = c.ink)
        for (f in s.math) {
            Spacer(Modifier.height(6.dp))
            when { f.gem != null -> GemWalkView(f.gem); f.table != null -> TableWalkView(f.table); else -> TexBlock(f.tex, size = 17.sp, color = c.ink) }
            f.note?.let { Spacer(Modifier.height(2.dp)); Rich(it, style = MaterialTheme.typography.bodySmall, color = c.ink2) }
        }
    }
}

/**
 * "Ako na to?": the method of a problem type, its goals numbered with how each is done. Closed it is one line
 * ("Ako na to?" with a chevron), so the answers stay on the first screen; a tap opens it ([onToggle]). [now]: the goal
 * the part on screen works on, marked and brought into view.
 */
@Composable
fun PostupCard(p: Postup, open: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier, now: Int? = null) {
    val c = T.c
    Column(modifier.fillMaxWidth().panel(18.dp, fill = c.card)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(18.dp))
                .clickable(role = Role.Button, onClickLabel = if (open) "Zbaliť" else "Ukázať postup", onClick = onToggle)
                .semantics { heading(); stateDescription = if (open) "Rozbalené" else "Zbalené" }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Ako na to?", style = MaterialTheme.typography.titleSmall, color = c.ink2, modifier = Modifier.weight(1f))
            // the app's chevron: pointing on while closed, turned down once open
            val turn by androidx.compose.animation.core.animateFloatAsState(if (open) 90f else 0f, label = "chevron")
            Icon24(Res.drawable.ic_chevron, tint = c.ink2, modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = turn }.clearAndSetSemantics {})
        }
        if (open) Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
            p.aims.forEachIndexed { i, a -> GoalPlan(i + 1, a.title, a.how, now = i == now) }
        }
    }
}

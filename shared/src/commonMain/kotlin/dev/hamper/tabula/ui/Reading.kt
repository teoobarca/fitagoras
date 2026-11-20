package dev.hamper.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.LocalGloss
import dev.hamper.tabula.math.Sheet
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.Tex
import dev.hamper.tabula.res.*

/**
 * The course glossary for the reading screens (a theorem, a definition, a proof, a judge question). A step frame under
 * it marks the first occurrence of each symbol on its screen and opens the symbol sheet on a tap.
 */
val LocalGlossary = compositionLocalOf<List<Symbol>?> { null }

/** The course's references (Definícia 2.7, Veta 2.18): provided app-wide, a reading screen sets its item's course. */
val LocalRefs = compositionLocalOf<RefIndex?> { null }

/** The ids a screen shows itself (its theorem, its definition): references to them are not marked there. */
val LocalReadingSelf = compositionLocalOf<Set<String>> { emptySet() }

/**
 * Draws a full card ("concept:podpriestor", "theorem:t2-18") with a way back: the sheet's way to the whole definition
 * or theorem. It is shown as a layer over the current screen, so the screen under it keeps all its state (answers
 * typed in, a proof's ticks).
 */
val LocalFullCard = compositionLocalOf<(@Composable (route: String, onClose: () -> Unit) -> Unit)?> { null }

/**
 * The bottom sheet of a screen, in its frame's bottom panel where every answer's verdict rises: a tapped symbol (how
 * it reads, one sentence, its definition) or a referenced definition or theorem (the Czech quote, the gist, its card).
 */
@Composable
fun ColumnScope.ReadingSheet(open: Sheet, onSwitch: (Sheet) -> Unit, onCard: (String) -> Unit, onClose: () -> Unit) {
    // nothing inside the sheet is tappable again: one sheet at a time
    CompositionLocalProvider(LocalGloss provides null) {
        Box(Modifier.fillMaxWidth().padding(bottom = 4.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(36.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(T.c.line))
        }
        when (open) {
            is Sheet.Sym -> SymbolBody(open.symbol, onSwitch, onClose)
            is Sheet.Ref -> RefBody(open.target, onCard, onClose)
        }
    }
}

@Composable
private fun ColumnScope.Head(onClose: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { heading(); liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
        content()
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClickLabel = "Zavrieť", onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { Icon24(Res.drawable.ic_close, "Zavrieť", tint = T.c.ink3, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun Link(text: String, onClick: () -> Unit) {
    val c = T.c
    val ink = T.hue.text
    Row(
        Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = ink)
        Spacer(Modifier.width(2.dp))
        Icon24(Res.drawable.ic_chevron, tint = ink, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ColumnScope.SymbolBody(sym: Symbol, onSwitch: (Sheet) -> Unit, onClose: () -> Unit) {
    val c = T.c
    Head(onClose) {
        Tex(sym.tex, size = 22.sp, color = c.ink, weight = 600)
        Spacer(Modifier.width(10.dp))
        Rich(sym.read, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = c.ink)
    }
    Rich(sym.say, style = MaterialTheme.typography.bodyLarge, color = c.ink)
    // its definition opens in the same sheet, as a reference would
    ConceptById[sym.concept]?.let { k ->
        val num = Regex("""\d+\.\d+""").find(k.src)?.value
        Link(if (k.short.isNotBlank()) "Definícia ${k.short}" + (num?.let { " (Def. $it)" } ?: "") else "Definícia: ${k.name}") { onSwitch(Sheet.Ref(RefTarget.Def(k))) }
    }
}

@Composable
private fun ColumnScope.RefBody(target: RefTarget, onCard: (String) -> Unit, onClose: () -> Unit) {
    val c = T.c
    val open = LocalFullCard.current
    val label = when (target) {
        is RefTarget.Def -> definitionLabel(target.k); is RefTarget.Thm -> theoremLabel(target.t)
        is RefTarget.Quote -> "${refNoun(target.n.kind).replaceFirstChar { it.uppercase() }} ${target.n.number}"
    }
    val name = when (target) { is RefTarget.Def -> target.k.name; is RefTarget.Thm -> target.t.name; is RefTarget.Quote -> target.n.name }
    val gist = when (target) { is RefTarget.Def -> conceptGist(target.k); is RefTarget.Thm -> firstSentence(target.t.idea).first; is RefTarget.Quote -> "" }
    Head(onClose) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.ink2)
            Rich(name, style = MaterialTheme.typography.titleLarge, color = c.ink)
        }
    }
    if (gist.isNotBlank()) Rich(gist, style = MaterialTheme.typography.bodyLarge, color = c.ink)
    Spacer(Modifier.height(2.dp))
    // the quote itself, drawn from its parts like every card; its source is already named in the sheet's head
    when (target) {
        is RefTarget.Def -> SourceCard(null, fill = c.bg) { SourceLines(target.k.czech) }
        is RefTarget.Thm -> SourceCard(null, fill = c.bg) { StatementText(target.t.wording); ProofLine(target.t) }
        is RefTarget.Quote -> SourceCard(null, fill = c.bg) { SourceLines(target.n.lines) }
    }
    // the way on only to what the sheet does not already show: a definition's why and examples, a theorem's proof
    if (open != null) when (target) {
        is RefTarget.Def -> Link("Prečo tak a príklady") { onCard(target.route) }
        is RefTarget.Quote -> Unit
        is RefTarget.Thm -> target.t.proof?.let { pr -> Link("Dôkaz · ${pr.rounds.size} ${slovakPlural(pr.rounds.size, "krok", "kroky", "krokov")}") { onCard("proof:${pr.id}") } }
    }
}

/** One Slovak line about a definition: its glossary sentence if a symbol stands for it, else its idea units. */
private fun conceptGist(k: Concept): String =
    Glossaries.values.flatten().firstOrNull { it.concept == k.id }?.say ?: k.units.joinToString(" · ")

/** The proof's place inside its step: [done] parts of [total], the current one counted, as the bar's split segment. */
data class StepPart(val done: Int, val total: Int)

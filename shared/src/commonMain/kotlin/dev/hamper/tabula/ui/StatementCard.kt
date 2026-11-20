package dev.hamper.tabula.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.math.nb
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.*

/*
 * Statements drawn from their parts (data/Model.kt, Wording): every place that shows a theorem, a definition or what
 * a proof proves draws it here, so a card, the reference sheet and the "Dokazuješ" bar always agree.
 */

/** Every fold open from the start: the overflow scan sees the whole text of every card (snap/OverflowScan.kt). */
val LocalUnfoldAll = compositionLocalOf { false }

/**
 * A statement's text: a long one leads with its core and unfolds to the whole text on a tap; a short one is whole.
 * [whole]: always the whole text (the sheet under a proof, where nothing may hide).
 */
@Composable
fun StatementText(s: Wording, whole: Boolean = false) {
    val core = s.core
    if (whole || !s.long || core == null) { SourceLines(s.lines); return }
    // the hypotheses stay with the core: a conclusion alone reads as a different claim (2.55 without dim P = d)
    if (s.assume.isNotEmpty()) { SourceLines(s.assume); Spacer(Modifier.height(8.dp)) }
    TexBlock(core, size = 18.sp, color = T.c.ink, center = true)
    Unfold("Celé znenie", key = s.hashCode().toString()) { SourceLines(s.lines) }
}

/**
 * One numbered statement, one card: its number and name on top, the statement, then how the course proves it when
 * that is not a separate step (a short argument, or why it is not proved). [named]: the name in the card (several
 * cards on one screen); a screen whose title already names it leaves it out.
 */
@Composable
fun TheoremCard(t: Theorem, named: Boolean = true, proofLine: Boolean = true, point: Int? = null) {
    val c = T.c
    // one point of a list the step needs ("Lemma 6.1, bod 3"): the hypotheses and that point, the whole one tap away
    val item = point?.let { n -> t.wording.claim.filterIsInstance<Item>().getOrNull(n - 1) }
    // the source label is short and names this card's own number ("Skriptá · Lema 6.1, bod 3"), never a long name
    SourceCard(cardLabel(t.src, theoremLabel(t)) + (if (item != null) ", bod $point" else ""), plain = true, statement = true) {
        if (named) { Rich(t.name, style = MaterialTheme.typography.titleMedium, color = c.ink); Spacer(Modifier.height(6.dp)) }
        if (item != null) {
            SourceLines(t.wording.assume + item)
            Unfold("Celé znenie", key = "whole:${t.id}") { SourceLines(t.wording.lines) }
        } else StatementText(t.wording)
        if (proofLine) ProofLine(t)
    }
    t.remarks.forEach { q -> Spacer(Modifier.height(if (lekcia()) 18.dp else 10.dp)); if (lekcia()) RemarkBlock(q.src) { SourceLines(q.lines) } else SourceCard(q.src) { SourceLines(q.lines) } }
}

/** "Skriptá · Veta 6.3": where the card's statement is, by its own number; other sources stay as written. */
private fun cardLabel(src: String, own: String) = if (src.startsWith("Skriptá")) "Skriptá · $own" else src

/** How the course proves [t] when it is not proved line by line: the short argument on a tap, or who leaves it out. */
@Composable
fun ProofLine(t: Theorem) {
    val c = T.c
    when (val p = t.proven) {
        is Proven.Lines -> Unit
        is Proven.Short -> { Unfold("Dôkaz", key = "proof:${t.id}") { Rich(p.argument, style = MaterialTheme.typography.bodyMedium, color = c.ink); Spacer(Modifier.height(4.dp)); Label(p.src) } }
        is Proven.Omitted -> if (p.why.isNotBlank()) { Spacer(Modifier.height(10.dp)); Rich("Bez dôkazu: ${p.why}", style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
    }
}

/** A definition as a card of its own: its number, its name, the exact text. */
@Composable
fun ConceptCard(k: Concept, named: Boolean = true) {
    val c = T.c
    SourceCard(cardLabel(k.src, definitionLabel(k)), plain = true, statement = true) {
        if (named) { Rich(k.name, style = MaterialTheme.typography.titleMedium, color = c.ink); Spacer(Modifier.height(6.dp)) }
        SourceLines(k.czech)
    }
}

/**
 * The cards of definitions and theorems by id ("dml-tc-deleni"), one each, in order; "dml-tc-lemma61#3" is point 3 of
 * a theorem's list, the whole statement one tap away. An unknown id draws nothing.
 */
@Composable
fun ItemCards(ids: List<String>) {
    ids.forEachIndexed { i, ref ->
        if (i > 0) Spacer(Modifier.height(10.dp))
        val id = cardId(ref)
        ConceptById[id]?.let { ConceptCard(it) } ?: TheoremById[id]?.let { TheoremCard(it, point = ref.substringAfter('#', "").toIntOrNull()) }
    }
}

/** The item a card reference shows: "dml-tc-lemma61#3" → "dml-tc-lemma61". */
fun cardId(ref: String) = ref.substringBefore('#')

/** Cards a screen shows whole: a reference to them there would open what is already on screen. A point is not whole. */
fun wholeCards(refs: List<String>) = refs.filter { '#' !in it }.toSet()

/**
 * What a proof proves, pinned over it. Closed it is one line: the part being proved now, or the core of the claim
 * ([round] < 0: no part, the whole proof on paper).
 * A tap opens the whole statement: the hypotheses, the claim with its quantifiers, its parts with the current one marked.
 */
@Composable
fun Dokazujes(s: Wording, round: Int) {
    val c = T.c
    val hx = rememberHaptics()
    val all = LocalUnfoldAll.current
    var open by rememberSaveable(s.hashCode()) { mutableStateOf(all) }
    val now = if (round < 0) null else s.parts.lastOrNull { it.from <= round }
    val say = if (open) "Dokazuješ. Zbaliť celé znenie" else "Dokazuješ. Rozbaliť celé znenie"
    // A reference above the work, not a second card competing with the question under it (judges, 18. 11.): flat on
    // the page, the label in the metadata's grey, the claim set from the left edge like a line of the skriptá, one
    // hairline closing it off. No icon: the word says what it is.
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClickLabel = if (open) "Zbaliť" else "Celé znenie") { hx.expand(); open = !open }
            .padding(top = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text((if (now != null) "Dokazuješ · ${now.label}" else "Dokazuješ").nb(), Modifier.weight(1f).clearAndSetSemantics { contentDescription = say }, style = MaterialTheme.typography.labelMedium, color = if (lekcia()) T.hue.text else c.ink2, onTextLayout = { LabelAudit.split(it) })
            Turn(open, Modifier.size(20.dp), if (lekcia()) T.hue.text else c.ink2)
        }
        val shown = now?.tex ?: s.core
        // the claim as context, in the metadata's grey and a size under the question's, so the question under it is
        // what the eye lands on (judges, 18. 11.: the block of equivalences competed with it)
        if (!open && shown != null) { Spacer(Modifier.height(4.dp)); TexBlock(shown, size = 16.sp, color = if (lekcia()) c.ink else c.ink2) }
        if (!open && shown == null) { Spacer(Modifier.height(4.dp)); SourceLines(s.claim) }
        AnimatedVisibility(open, enter = fadeIn() + expandVertically(expandFrom = Alignment.Top), exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)) {
            Column(Modifier.padding(top = 8.dp)) {
                if (s.assume.isNotEmpty()) { Label("Predpoklady"); Spacer(Modifier.height(2.dp)); SourceLines(s.assume); Spacer(Modifier.height(10.dp)) }
                Label("Tvrdenie"); Spacer(Modifier.height(2.dp)); SourceLines(s.claim + s.rest)
                if (s.parts.size > 1) {
                    Spacer(Modifier.height(10.dp)); Label("Časti")
                    s.parts.forEach { p ->
                        val cur = p == now
                        // the label over its formula: the formula gets the card's whole width, as in the closed bar
                        Spacer(Modifier.height(6.dp))
                        Text(p.label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (cur) FontWeight.Bold else FontWeight.Medium), color = if (cur) c.ink else c.ink2)
                        TexBlock(p.tex, size = 18.sp, color = if (cur) c.ink else c.ink2)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
    }
}

/** A chevron that turns: down closed, up open. */
@Composable
private fun Turn(open: Boolean, modifier: Modifier, tint: Color = T.hue.text) {
    val still = calm()
    val turn by animateFloatAsState(if (open) -90f else 90f, if (still) snap() else Motion.settle(), label = "turn")
    Icon24(Res.drawable.ic_chevron, tint = tint, modifier = modifier.rotate(turn))
}

/** A labelled fold that opens in place ("Celé znenie", "Dôkaz"); TalkBack reads it by what it does. */
@Composable
fun Unfold(label: String, key: String, color: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    val hx = rememberHaptics()
    val still = calm()
    val all = LocalUnfoldAll.current
    var open by rememberSaveable(key) { mutableStateOf(all) }
    val toggle = { hx.expand(); open = !open }
    Column {
        AnimatedVisibility(
            open,
            enter = if (still) fadeIn(snap()) else fadeIn(Motion.fade()) + expandVertically(Motion.settle<IntSize>(), expandFrom = Alignment.Top),
            exit = if (still) fadeOut(snap()) else fadeOut(Motion.fade()) + shrinkVertically(Motion.settle<IntSize>(), shrinkTowards = Alignment.Top),
        ) { Column(Modifier.padding(top = 6.dp)) { content() } }
        Box(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = toggle)
                .clearAndSetSemantics { contentDescription = "$label, " + if (open) "zbaliť" else "rozbaliť"; role = Role.Button; stateDescription = if (open) "rozbalené" else "zbalené"; onClick { toggle(); true } }
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // the label gives way, so a long one wraps instead of squeezing the chevron out
                Text(if (open) "Skryť" else label.nb(), Modifier.weight(1f, fill = false), style = MaterialTheme.typography.labelLarge, color = color ?: T.hue.text)
                Spacer(Modifier.width(2.dp))
                Turn(open, Modifier.size(20.dp), color ?: T.hue.text)
            }
        }
    }
}

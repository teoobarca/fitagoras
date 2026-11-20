package dev.hamper.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.nb
import dev.hamper.tabula.res.*

/*
 * The new look of the screens inside a lesson (Teo, 19. 11.: today's "príliš sterilný", "slabá hierarchia"; "go bold,
 * ale nie 100 % playful", the task screens "focused na matiku"). On since Teo's yes (19. 11.); off, 1.6.2's look stays pixel for pixel.
 * It applies only on a step of a lesson ([lekcia]). Nothing a student can reach changes it; the screenshots show
 * 1.6.2's look with -Psnap.look=old (app/src/test/.../snap/Snap.kt).
 *
 * What it changes, through small `if (lekcia())` branches in the shared screens:
 * - a statement (definition, theorem, the skriptá's text, a remark of the skriptá) is paper on the page, its display
 *   formulas centred and a size larger; its source a quiet line under it ("Skriptá · Veta 4.2"), never a header
 *   competing with the statement. No bar, rule or accent on its edge (Teo, 19. 11.: "táto farba sa mi nepáči, preč,
 *   fuj, vibe slop"): the paper, the type and the room around it hold it together;
 * - our comment (the idea of a proof) is no paper: plain text on the page, no fill, frame or source, so the skriptá's
 *   words and ours never look alike (Teo, 19. 11.: "tá komentárová čiara sa mi nepáči");
 * - the kind of screen ("Veta", "Riešený príklad") in the accent above the title;
 * - the goals of a proof and a worked example keep 1.6.2's look (Teo, 19. 11.: "toto asi bolo predtým lepšie"): a goal
 *   reached green with its lines, the one being worked on outlined (in a proof its title in the accent), the
 *   ones ahead dashed;
 * - bottom buttons: one main, the second a quiet word beside it or under it;
 * - a clip's question large, the lecture it comes from in a frame that plays it.
 */

/** The new look of the screens inside a lesson. On; false brings back 1.6.2's look of these screens. */
var LekciaNova by mutableStateOf(true)

/**
 * The new look where it belongs: on a step of a lesson (a definition, theorem, proof, worked example, walk or clip
 * inside a lesson), not on the Test tab, exercises and solutions outside a lesson or the tutor's panel, whose labels
 * are headings rather than sources (review, 19. 11.).
 */
@Composable
@ReadOnlyComposable
fun lekcia(): Boolean = LekciaNova && LocalStepNav.current != null

/** The kind of screen above its title ("Veta", "Riešený príklad", "… · dôkaz, 2 z 4"): the accent, 13/700. */
@Composable
fun Kicker(text: String, modifier: Modifier = Modifier) {
    if (!lekcia()) { Label(text, modifier); return }
    val style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    val t = varLast(text)
    if ('$' in t || '§' in t) Rich(t, modifier, style = style, color = T.hue.text)
    else Text(t.nb(), modifier, style = style, color = T.hue.text, onTextLayout = { LabelAudit.split(it) })
}

/** A statement as a block on the board: paper, its source a quiet line under it, aligned with its text. */
@Composable
fun StatementBlock(label: String?, modifier: Modifier = Modifier, fill: Color = T.c.card, plain: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c
    val shape = RoundedCornerShape(18.dp)
    Column(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().background(fill, shape).border(1.dp, c.line, shape).padding(horizontal = 16.dp, vertical = 16.dp)) { content() }
        if (label != null) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.padding(horizontal = 16.dp)) {
                if (plain) CompositionLocalProvider(LocalRefs provides null) { Label(label) }
                else CompositionLocalProvider(dev.hamper.tabula.math.LocalRefLabel provides true) { Label(label) }
            }
        }
    }
}

/**
 * Our comment on the statement above it (the idea of a proof): plain text on the page, no fill, no frame, no line, no
 * source. The skriptá's words are paper with their source under them; ours are not, so the two never look alike.
 */
@Composable
fun Comment(text: String, modifier: Modifier = Modifier) = Rich(text, modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = T.c.ink)

/** The bottom of a step with two ways: the main one wide, the other a quiet word beside it. */
@Composable
fun MainAndQuiet(quiet: String, onQuiet: () -> Unit, main: String, onMain: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        QuietWord(quiet, onQuiet)
        Spacer(Modifier.width(8.dp))
        Cta(main, onMain, Modifier.weight(1f))
    }
}

/** A second action as a word, not a button: grey, 48 dp tall to tap. */
@Composable
fun QuietWord(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val hx = rememberHaptics()
    Box(
        modifier.heightIn(min = 52.dp).widthIn(min = 88.dp).clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button) { hx.tap(); onClick() }.padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text.nb(), style = MaterialTheme.typography.titleSmall, color = T.c.ink2, onTextLayout = { LabelAudit.check(text, it) }) }
}

/** A remark of the skriptá under a statement ("Poznámka 4.3"): the skriptá's words, so paper as the statement is. */
@Composable
fun RemarkBlock(label: String?, content: @Composable ColumnScope.() -> Unit) = StatementBlock(label, content = content)

/**
 * A table that grows by rows (Euclid, the REA) as a stage of its own: paper over the whole room the walk reserves (rows
 * still to come included, so nothing under it moves), the arrows on its bottom edge. The room reads as the place where
 * the table is written, not as a hole with a navigation to nothing; no frame, the paper is enough.
 */
@Composable
fun Modifier.tableStage(): Modifier = background(T.c.card, RoundedCornerShape(14.dp)).padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 4.dp)

/** The colour of a quiet second action under the main button. */
@Composable
fun quietInk(today: Color): Color = if (lekcia()) T.c.ink2 else today

/**
 * The lecture a clip comes from, as a preview of the screen it plays on: dark (in the dark theme darker than the page,
 * framed by the card line), 16:9, who and which lecture on top, a quiet play mark in the middle (the main button under
 * the screen plays it; this is no second button), the time in the lecture under it. The length is on the button only.
 * A tap still plays it, as the main button does.
 */
@Composable
fun ClipFrame(lecture: String, range: String, onPlay: () -> Unit) {
    val c = T.c
    val ink = Color(0xFFECE8DF)
    // "7. prednáška" never breaks after the ordinal's dot
    val who = remember(lecture) { lecture.replace(Regex("""(\d\.) """), "$1\u00A0").nb() }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = maxWidth * 9f / 16f).clip(RoundedCornerShape(18.dp)).background(if (c.dark) Color(0xFF0F1312) else Color(0xFF26282C))
                .then(if (c.dark) Modifier.border(1.dp, c.line, RoundedCornerShape(18.dp)) else Modifier)
                .clickable(role = Role.Button, onClickLabel = "Pustiť klip", onClick = onPlay).padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(who, style = MaterialTheme.typography.labelMedium, color = ink.copy(alpha = .88f))
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Icon24(Res.drawable.ic_play, tint = ink.copy(alpha = .5f), modifier = Modifier.size(30.dp))
            }
            Text(range, style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"), color = ink.copy(alpha = .88f))
        }
    }
}

/**
 * A clip's question, the screen's one thing, in the title's type. A question that ends in a variable ("… modulo m?")
 * sets it as [varLast] does a title, so it never stands alone on the last line.
 */
@Composable
fun ClipQuestion(text: String) {
    val t = if ('$' in text || '§' in text) text else text.replace(Regex("""(?<=\p{L}) ([a-zA-Z])\?$"""), "\u00A0§$1§?")
    KeepWhole { m -> Rich(varLast(t), m, style = MaterialTheme.typography.headlineSmall, color = T.c.ink) }
}

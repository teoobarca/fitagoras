package dev.hamper.tabula.ui

import dev.hamper.tabula.system.Telemetry
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.res.*

/** Words that end in a period without ending the sentence (compared lower case, without the period). */
private val Abbrev = setOf(
    "napr", "tzv", "def", "resp", "tj", "t", "j", "tzn", "príp", "popr", "popř", "např", "viď", "viz", "vid", "str", "obr",
    "kap", "pozn", "pr", "př", "odd", "čl", "č", "cca", "max", "min", "rov", "tab", "vs", "mj", "ozn", "ap", "pod", "sl",
)

private val Closers = setOf(')', '"', '“', '”', '\'', '»')
private val Openers = setOf('§', '$', '*', '„', '(', '"', '“')

/**
 * The first sentence of [text] and the rest, or null for the rest when there is only one sentence.
 * A sentence ends at . ! ? or … followed by a space and a capital (or a formula, bold, a quote). Never inside
 * math (§…§ or $…$) or bold (**…**), never after an abbreviation such as "napr." and never inside "2.7".
 */
fun firstSentence(text: String): Pair<String, String?> {
    var math = false
    var bold = false
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        when {
            ch == '§' || ch == '$' -> math = !math
            ch == '*' && text.getOrNull(i + 1) == '*' -> { if (!math) bold = !bold; i++ }
            !math && !bold && (ch == '.' || ch == '!' || ch == '?' || ch == '…') -> {
                var j = i + 1
                while (j < text.length && (text[j] == '.' || text[j] in Closers)) j++
                var k = j
                while (k < text.length && text[k].isWhitespace()) k++
                val next = text.getOrNull(k)
                if (k > j && next != null && (next.isUpperCase() || next in Openers) && !(ch == '.' && abbreviation(text, i))) {
                    return text.substring(0, j).trim() to text.substring(k).trim()
                }
            }
        }
        i++
    }
    return text.trim() to null
}

/** The longest head a verdict sheet sets in its large type, in characters a reader sees (a formula counts its glyphs). */
const val VerdictHeadMax = 40

/** About how many characters [text] shows: markup and TeX commands left out, a formula counted by its glyphs. */
fun shownLength(text: String): Int {
    var n = 0
    var math = false
    var i = 0
    while (i < text.length) {
        val ch = text[i]
        when {
            ch == '§' || ch == '$' -> math = !math
            ch == '*' && text.getOrNull(i + 1) == '*' -> i++
            math && ch == '\\' -> { i++; while (i < text.length && text[i].isLetter()) i++; continue }
            math && (ch == '{' || ch == '}' || ch == '^' || ch == '_' || ch == ' ') -> {}
            else -> n++
        }
        i++
    }
    return n
}

/**
 * A verdict's head and what goes under it, from the explanation [text]: its first sentence when that is short
 * (at most [VerdictHeadMax] shown characters), else the clause before its first colon ("Štvrtý stĺpec je vedľajší: …"
 * heads with "Štvrtý stĺpec je vedľajší."), else [fallback] over the whole text. Long text is never set as a head
 * (Teo, 18. 11.: a head of five lines with its formula torn after "=").
 */
fun verdictHead(text: String, fallback: String): Pair<String, String?> {
    val (first, rest) = firstSentence(text)
    if (shownLength(first) <= VerdictHeadMax) return first to rest
    var math = false
    for (i in first.indices) {
        val ch = first[i]
        if (ch == '§' || ch == '$') math = !math
        else if (!math && ch == ':' && first.getOrNull(i + 1) == ' ') {
            val head = first.substring(0, i).trim()
            // an example is not a verdict ("Napr. $X=\\{a\\}$: …")
            if (shownLength(head) > VerdictHeadMax || head.startsWith("Napr")) break
            val after = first.substring(i + 1).trim().let { a -> if (a.first().isLowerCase()) a.replaceFirstChar { it.uppercase() } else a }
            return "$head." to listOfNotNull(after, rest).joinToString(" ")
        }
    }
    return fallback to text.trim()
}

/** Whether the period at [dot] closes an abbreviation: the word right before it is one, or is a lone lower-case letter. */
private fun abbreviation(text: String, dot: Int): Boolean {
    var s = dot
    while (s > 0 && text[s - 1].isLetter()) s--
    val word = text.substring(s, dot)
    if (word.isEmpty()) return false
    return word.lowercase() in Abbrev || (word.length == 1 && word[0].isLowerCase())
}

/**
 * An explanation, short: its first sentence, the rest behind "Prečo?", which opens in place and closes again.
 * One sentence gets no control. TalkBack reads the short line, then the control by what it does.
 */
@Composable
fun Explain(text: String, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.bodyMedium, color: Color = T.c.ink, accent: Color = T.hue.text) {
    val (head, rest) = remember(text) { firstSentence(text) }
    if (rest == null) { Rich(text, modifier, style = style, color = color); return }
    val hx = rememberHaptics()
    val still = calm()
    var open by rememberSaveable(text) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) -90f else 90f, if (still) snap() else Motion.settle(), label = "why")
    Column(modifier) {
        Rich(head, style = style, color = color)
        AnimatedVisibility(
            open,
            enter = if (still) fadeIn(snap()) else fadeIn(Motion.fade()) + expandVertically(Motion.settle<IntSize>(), expandFrom = Alignment.Top),
            exit = if (still) fadeOut(snap()) else fadeOut(Motion.fade()) + shrinkVertically(Motion.settle<IntSize>(), shrinkTowards = Alignment.Top),
        ) { Rich(rest, Modifier.padding(top = 4.dp).semantics { liveRegion = LiveRegionMode.Polite }, style = style, color = color) }
        val say = if (open) "Prečo? Zbaliť vysvetlenie" else "Prečo? Rozbaliť vysvetlenie"
        val toggle = { hx.expand(); if (!open) Telemetry.help("why", null); open = !open }
        // small on screen, a full 48 dp for the finger: the row is tall, the pill inside it is not
        Box(
            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = toggle)
                .clearAndSetSemantics { contentDescription = say; role = Role.Button; stateDescription = if (open) "rozbalené" else "zbalené"; onClick { toggle(); true } }
                .padding(end = 8.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Prečo?", style = MaterialTheme.typography.labelLarge, color = accent)
                Spacer(Modifier.width(2.dp))
                Icon24(Res.drawable.ic_chevron, tint = accent, modifier = Modifier.size(20.dp).rotate(turn))
            }
        }
    }
}

package dev.hamper.tabula.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

/*
 * How a streamed answer is written on screen (Teo, 18. 11.: "streaming s fade-in", 19. 11.: a "native feeling"). The
 * model's deltas come in bursts; the screen shows them word by word at a steady pace that catches up with the stream
 * ([plynulo]), never inside a formula or a tag, so a formula appears whole and set; each new word fades in over a short
 * time ([nabehKonca], drawn over the laid-out line, so the text never moves while it fades). Lines only grow at the end.
 */

/** Inside a paragraph being written: [dev.hamper.tabula.math.Rich] fades in the words added to its end. */
val LocalNabehSlov = staticCompositionLocalOf { false }

/** How long a new word takes to fade in. */
private const val NABEH_NS = 260_000_000L
/** The slowest pace (characters a second) and how fast a burst is caught up with (a backlog shrinks by 1/e in this time). */
private const val MIN_ZNAKOV_ZA_S = 55f
private const val DOBIEHANIE_S = 0.45f

/**
 * What of [cel] (the text received so far) is on screen now: it grows at a steady pace toward [cel], cut at a word's
 * end outside any formula or tag ([rez]). [zivo] false: the turn was not written now (history), it is all there.
 * [koniec]: the stream is over, the last word may show without a space after it. [hned]: show all at once (stopped).
 */
@Composable
fun plynulo(cel: String, zivo: Boolean, koniec: Boolean, hned: Boolean = false): String {
    if (!zivo) return cel
    // a turn starts empty; one composed again (the panel reopened) goes on from what it has, not from the start
    val poz = remember { mutableFloatStateOf(cel.length.toFloat()) }
    val aktualny = rememberUpdatedState(cel)
    val cely = rememberUpdatedState(hned)
    val konci = rememberUpdatedState(koniec)
    LaunchedEffect(Unit) {
        var pred = 0L
        while (true) {
            if (poz.floatValue > aktualny.value.length) poz.floatValue = 0f // the answer was cleared for another try
            if (cely.value || poz.floatValue >= aktualny.value.length) {
                // nothing left to write: wait (no frames) until more arrives
                snapshotFlow { aktualny.value.length to cely.value }.first { (n, c) -> !c && n > poz.floatValue }
                pred = 0L
            }
            val t = withFrameNanos { it }
            val dt = if (pred == 0L) 0.016f else ((t - pred) / 1e9f).coerceAtMost(0.1f)
            pred = t
            val zostava = aktualny.value.length - poz.floatValue
            if (zostava > 0) poz.floatValue = minOf(aktualny.value.length.toFloat(), poz.floatValue + maxOf(MIN_ZNAKOV_ZA_S, zostava / DOBIEHANIE_S) * dt)
        }
    }
    // the caller recomposes only when the shown text changes (a whole word), not on every frame of the pace
    val ukazane = remember { derivedStateOf { if (cely.value) rez(aktualny.value, Int.MAX_VALUE, true) else rez(aktualny.value, poz.floatValue.toInt(), konci.value) } }
    return ukazane.value
}

/**
 * [t] cut to show about [n] characters: at the end of the word [n] falls into (when that word is whole in [t]), never
 * inside a formula ($…$, $$…$$) or a tag (<veta …/>), so a formula is shown only once it is closed. [koniec]: [t] is the
 * whole answer, its end is a word's end too.
 */
fun rez(t: String, n: Int, koniec: Boolean): String {
    if (n >= t.length && koniec) return t
    // the end of the word n falls into
    var hranica = n.coerceIn(0, t.length)
    while (hranica < t.length && !t[hranica].isWhitespace()) hranica++
    var cut = 0
    var i = 0
    var mat = 0 // 0 text, 1 inline formula, 2 display formula
    var tag = false
    while (i < t.length && i <= hranica) {
        val ch = t[i]
        if (!tag && mat == 0 && ch.isWhitespace()) { cut = i }
        when {
            tag -> { if (ch == '>') tag = false }
            ch == '\\' -> { i += 2; continue }
            ch == '$' && t.getOrNull(i + 1) == '$' && mat != 1 -> { if (mat == 2) mat = 0 else if (otvara(t, i, "$$", koniec)) mat = 2; i += 2; continue }
            ch == '$' -> mat = if (mat == 1) 0 else if (mat == 0 && otvara(t, i, "$", koniec)) 1 else mat
            ch == '<' && mat == 0 && t.getOrNull(i + 1)?.isLetter() == true && otvara(t, i, ">", koniec) -> tag = true
        }
        i++
    }
    if (i >= t.length && koniec && mat == 0 && !tag) return t
    return t.substring(0, cut).trimEnd()
}

/**
 * Does the formula or tag opening at [i] close with [zaver]? Until it does, it holds the text back, but never for good:
 * one that is not closed within 300 characters or before a blank line, or not at all when the answer is whole, is text
 * (a lone dollar, "x<y"), so the answer never stops in the middle.
 */
private fun otvara(t: String, i: Int, zaver: String, koniec: Boolean): Boolean {
    val j = t.indexOf(zaver, if (zaver == "$$") i + 2 else i + 1)
    val hranica = minOf(t.length, i + 300)
    val odsek = t.indexOf("\n\n", i).let { if (it < 0) Int.MAX_VALUE else it }
    if (j >= 0) return j < hranica && j < odsek
    return !koniec && t.length < i + 300 && odsek == Int.MAX_VALUE
}

/**
 * The words added to a text's end fade in ([LocalNabehSlov]): each growth of [dlzka] (the laid-out text's length) is a
 * piece that becomes visible over [NABEH_NS], drawn over the finished layout (nothing is laid out again for the fade).
 */
@Composable
fun Modifier.nabehKonca(dlzka: Int, layout: () -> TextLayoutResult?): Modifier {
    val kusy = remember { mutableStateListOf<Pair<Int, Long>>() }
    var cas by remember { mutableLongStateOf(0L) }
    var pred by remember { mutableIntStateOf(0) }
    LaunchedEffect(dlzka) {
        if (dlzka < pred) kusy.clear()
        else if (dlzka > pred) withFrameNanos { t -> kusy += pred to t; cas = t }
        pred = dlzka
        while (kusy.isNotEmpty()) {
            val t = withFrameNanos { it }
            cas = t
            kusy.removeAll { t - it.second >= NABEH_NS }
        }
    }
    // the frame that first draws a longer text comes before the effect stamps its piece: that end is hidden already
    if (kusy.isEmpty() && dlzka <= pred) return this
    return this.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val l = layout() ?: return@drawWithContent
        val n = l.layoutInput.text.length
        val vsetky = if (n > pred) kusy + (pred to Long.MAX_VALUE) else kusy
        vsetky.forEachIndexed { i, (od0, t0) ->
            val a = if (t0 == Long.MAX_VALUE) 0f else ((cas - t0).toFloat() / NABEH_NS).coerceIn(0f, 1f)
            val skry = 1f - FastOutSlowInEasing.transform(a)
            if (skry <= .01f) return@forEachIndexed
            val od = od0.coerceAtMost(n)
            val po = (vsetky.getOrNull(i + 1)?.first ?: n).coerceIn(od, n)
            if (po <= od) return@forEachIndexed
            val prvy = l.getLineForOffset(od); val posledny = l.getLineForOffset(po - 1)
            for (ln in prvy..posledny) {
                val x0 = if (ln == prvy) l.getHorizontalPosition(od, true) else l.getLineLeft(ln)
                val x1 = if (ln == posledny) maxOf(l.getHorizontalPosition(po, true), l.getLineRight(ln).takeIf { po >= n } ?: 0f) else l.getLineRight(ln)
                val top = l.getLineTop(ln)
                drawRect(Color.Black.copy(alpha = skry), Offset(minOf(x0, x1) - 1f, top), Size(kotlin.math.abs(x1 - x0) + 2f, l.getLineBottom(ln) - top), blendMode = BlendMode.DstOut)
            }
        }
    }
}

/**
 * What the tutor is doing, said in words ("Čítam papier", "Overujem výpočet"), with three dots breathing in the
 * accent: a calm sign that it works, no spinner, no time estimate. A new state cross-fades in place.
 */
@Composable
fun Cakanie(stav: String, modifier: Modifier = Modifier) {
    val c = T.c
    // each state stays readable (≥ 0,7 s): a tool that answers at once still says what it did; a pile-up keeps the last
    val rad = remember { kotlinx.coroutines.channels.Channel<String>(kotlinx.coroutines.channels.Channel.UNLIMITED) }
    var ukazany by remember { mutableStateOf(stav) }
    val poslany = remember { mutableStateOf(stav) }
    LaunchedEffect(stav) { if (stav != poslany.value) { poslany.value = stav; rad.trySend(stav) } }
    LaunchedEffect(Unit) {
        val cakajuce = ArrayDeque<String>()
        while (true) {
            if (cakajuce.isEmpty()) cakajuce += rad.receive()
            while (true) cakajuce += rad.tryReceive().getOrNull() ?: break
            while (cakajuce.size > 2) cakajuce.removeFirst()
            ukazany = cakajuce.removeFirst()
            kotlinx.coroutines.delay(700)
        }
    }
    val tr = rememberInfiniteTransition()
    val faza by tr.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart))
    Row(modifier.heightIn(min = 28.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 0 until 3) {
                // a soft wave across the dots: each rises and fades in turn
                val x = ((faza - i * .18f) % 1f + 1f) % 1f
                val v = if (x < .5f) FastOutSlowInEasing.transform(x * 2f) else FastOutSlowInEasing.transform((1f - x) * 2f)
                Box(Modifier.size(7.dp).graphicsLayer { alpha = .28f + .72f * v; scaleX = .8f + .2f * v; scaleY = .8f + .2f * v }.background(c.lesson.main, CircleShape))
            }
        }
        Spacer(Modifier.width(10.dp))
        AnimatedContent(ukazany, transitionSpec = { fadeIn(tween(260, 90)) togetherWith fadeOut(tween(160)) }) { s ->
            Text(s, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        }
    }
}

package dev.hamper.tabula.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.math.Rich
import kotlinx.coroutines.delay
import dev.hamper.tabula.res.*

/*
 * The goals of a proof, a solution and a worked example, one look for all three (the approved proof
 * design): a goal reached is green with a check and the lines that settled it, the goal being worked on is an outlined
 * card with its number, a goal ahead is a quiet dashed row, and the method before an attempt lists them numbered.
 */

/** A goal reached: checked, with the line that settled it. */
@Composable
fun GoalDone(title: String, line: String) = GoalDone(title) { Rich(line, style = MaterialTheme.typography.bodyMedium, color = T.c.ink) }

/** A goal reached: checked, its title, then [content] (the steps that settled it). */
@Composable
fun GoalDone(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c
    Row(modifier.fillMaxWidth().background(c.goodTint, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Box(Modifier.padding(top = 1.dp).size(22.dp).clip(CircleShape).background(c.good), contentAlignment = Alignment.Center) {
            Icon24(Res.drawable.ic_check, tint = c.card, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Rich(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Spacer(Modifier.height(2.dp))
            content()
        }
    }
}

/**
 * The goal being worked on now: its number, title and [content], in a card that says "here". [flat]: the content is
 * a question with its own cards (a proof's options), so no card around them: the number and title over the question,
 * on the page, one level of cards only.
 */
@Composable
fun GoalOpen(k: Int, title: String, flat: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c; val h = T.hue
    val bring = remember { BringIntoViewRequester() }
    LaunchedEffect(k) { delay(250); bring.bringIntoView() }
    if (flat) {
        Column(Modifier.fillMaxWidth().bringIntoViewRequester(bring).padding(top = 6.dp).semantics { stateDescription = "Teraz" }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoalNumber(k, if (c.dark) c.ink else h.main, c.card)
                Spacer(Modifier.width(10.dp))
                Rich(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleSmall, color = if (lekcia()) h.text else c.ink2)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
        return
    }
    Column(
        Modifier.fillMaxWidth().bringIntoViewRequester(bring).background(c.card, RoundedCornerShape(18.dp)).border(2.dp, if (c.dark) c.ink2 else h.main, RoundedCornerShape(18.dp))
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoalNumber(k, if (c.dark) c.ink else h.main, c.card)
            Spacer(Modifier.width(10.dp))
            Rich(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleSmall, color = c.ink)
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/** A goal still ahead: only its title, quiet; [note] says why it is not needed when the solution skips it. */
@Composable
fun GoalLater(k: Int, title: String, note: String? = null) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().dashed(c.axis, 16.dp).padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GoalNumber(k, c.line, c.ink2)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Rich(title, style = MaterialTheme.typography.titleSmall, color = c.ink2)
            note?.let { Rich(it, style = MaterialTheme.typography.bodySmall, color = c.ink2) }
        }
    }
}

/** A goal of a method before any attempt: its number, title and how it is done. */
@Composable
fun GoalPlan(k: Int, title: String, how: String, now: Boolean = false) {
    val c = T.c; val h = T.hue
    // [now]: the goal the part on screen works on, marked as GoalOpen marks it and brought into view
    val bring = remember { BringIntoViewRequester() }
    if (now) LaunchedEffect(k) { delay(250); bring.bringIntoView() }
    Row(
        Modifier.fillMaxWidth().padding(vertical = if (now) 3.dp else 5.dp)
            .then(if (now) Modifier.bringIntoViewRequester(bring).border(2.dp, if (c.dark) c.ink2 else h.main, RoundedCornerShape(14.dp)).padding(horizontal = 8.dp, vertical = 6.dp)
                .semantics { stateDescription = "Teraz" } else Modifier),
    ) {
        Box(Modifier.padding(top = 1.dp)) { if (now) GoalNumber(k, if (c.dark) c.ink else h.main, c.card) else GoalNumber(k, if (c.dark) c.line else h.tint, if (c.dark) c.ink else h.text) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Rich(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Rich(how, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        }
    }
}

@Composable
private fun GoalNumber(k: Int, fill: androidx.compose.ui.graphics.Color, ink: androidx.compose.ui.graphics.Color) {
    // the circle grows with the text, so at a large font the digit never sticks out of it
    val disc = maxOf(22.dp, with(androidx.compose.ui.platform.LocalDensity.current) { MaterialTheme.typography.labelSmall.lineHeight.toDp() } + 6.dp)
    Box(Modifier.size(disc).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
        Text("$k", style = MaterialTheme.typography.labelSmall, color = ink)
    }
}

private fun Modifier.dashed(color: androidx.compose.ui.graphics.Color, radius: androidx.compose.ui.unit.Dp) = drawBehind {
    val w = 1.5.dp.toPx()
    drawRoundRect(
        color, topLeft = androidx.compose.ui.geometry.Offset(w / 2, w / 2), size = androidx.compose.ui.geometry.Size(size.width - w, size.height - w),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(w, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
    )
}

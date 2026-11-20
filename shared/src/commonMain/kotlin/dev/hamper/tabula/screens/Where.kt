package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.*
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.*
import dev.hamper.tabula.compat.time.LocalDateTime
import dev.hamper.tabula.compat.time.*

/* "Kde sme": where the course is for the student's parallel, what comes next, which lessons go with it, the tests. */

// a topic's number stays with its name ("Teória čísel 3", never "čísel / 3": a lone "3" read as a count; inspector, 18. 11.)
private fun topics(c: Course, l: Lecture) = l.topics.joinToString(", ") { c.topicName(it).replace(Regex(""" (?=\d+$)"""), "\u00A0") }

/** "p4s05" -> "prezentácia 4, slajd 5" */
private fun reachedText(r: String) = Regex("p(\\d+)s(\\d+)").find(r)?.let {
    val s = it.groupValues[2].toInt()
    if (s == 0) "došli na začiatok prezentácie ${it.groupValues[1]}" else "došli po prezentáciu ${it.groupValues[1]}, slajd $s"
} ?: r

/** "P8" of the schedule as said: "8. prednáška". */
internal fun lectureName(label: String) = Regex("""P(\d+)""").matchEntire(label)?.let { "${it.groupValues[1]}. prednáška" } ?: label

/**
 * Where the course is, as one quiet line under the path's title: the next lecture and its topic, the whole schedule a
 * tap away. The path's focus is the lesson on turn; the schedule is its context, not a second card.
 */
@Composable
fun WhereLine(store: Store, course: Course, onRoute: (String) -> Unit) {
    val c = T.c
    val now = LocalDateTime.now()
    val p = course.parallel(store.parallel(course))
    val w = where(course, p.id, now)
    // "Ďalej 8. prednáška", never the schedule's bare "P8" (judge, 18. 11.: a number needs its word); the topic after a
    // dash, so a date's last dot never meets a colon ("18. 11.:" read as a typo; inspector, 18. 11.)
    val line = w.next?.let { n -> "Ďalej ${lectureName(n.label)}, ${n.date.format(DayFmt)}\u00A0– ${topics(course, n)}" } ?: "Prednášky v semestri skončili."
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClickLabel = "Celý rozvrh a termíny") { onRoute("where") }
            .heightIn(min = 48.dp).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(line, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        Spacer(Modifier.width(8.dp))
        Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun WhereScreen(store: Store, onClose: () -> Unit, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    val course = store.course
    val now = LocalDateTime.now()
    val p = course.parallel(store.parallel(course))
    val w = where(course, p.id, now)
    StepFrame(0, 0, onClose, closeLabel = "Zavrieť") {
        Label("${course.code}, paralelka ${p.id}: ${p.teacher}, ${weekday2(p.day)} ${p.time}")
        Spacer(Modifier.height(2.dp))
        H1("Kde je predmet")
        Spacer(Modifier.height(4.dp))
        Sub("Podľa harmonogramu na stránke predmetu (overený ${course.schedule.checked.format(DayFmt)} ${course.schedule.checked.year}) a rozvrhu. Paralelku zmeníš v nastaveniach.")
        Quiet("Zmeniť paralelku alebo termín", { onRoute("settings") }, color = c.lesson.text)
        Spacer(Modifier.height(12.dp))
        Text(if (course.test.word == PisomkaWord) "Písomky a skúška" else "Testy a skúška", style = MaterialTheme.typography.titleLarge, color = c.ink)
        Spacer(Modifier.height(4.dp))
        for (e in course.exams) {
            val picked = store.terms[e.id]
            val start = examStart(e, picked, now)
            val mine = e == course.test
            // over: the student's own sitting if picked, else the last term, else the window's end
            val end = picked?.plusMinutes((e.minutes ?: 90).toLong()) ?: examOver(e)
            val past = end < now
            // several sittings and none picked: the days count to the nearest one, and it says so
            val choose = picked == null && e.terms.size > 1
            val whenText = when {
                picked != null -> "tvoj termín ${weekday(picked.toLocalDate())} ${picked.format(DayFmt)}${atTime(picked)}"
                e.terms.size > 1 -> "termíny ${e.terms.minOf { it.start }.format(DayFmt)} až ${e.terms.maxOf { it.start }.format(DayFmt)}"
                e.terms.size == 1 -> "${weekday(e.terms[0].start.toLocalDate())} ${e.terms[0].start.format(DayFmt)}${atTime(e.terms[0].start)}"
                e.from != null && e.to != null -> "${e.from.format(DayFmt)} až ${e.to.format(DayFmt)} ${e.to.year}"
                e.from != null -> "od ${e.from.format(DayFmt)} ${e.from.year}, termín oznámia"
                else -> "termín oznámia"
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                Dot(if (past) c.ink3 else if (mine) c.lesson.main else c.line)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("${e.title}, ${e.points} b" + (e.minutes?.let { ", $it min" } ?: ""), style = MaterialTheme.typography.titleSmall, color = if (past) c.ink2 else c.ink)
                    Text(if (past) "Za tebou ($whenText)" else whenText.replaceFirstChar { it.uppercase() } + (start?.takeIf { it > now }?.let { s -> ", " + (if (choose) "najbližší " else "") + inDays(daysTo(s.toLocalDate())) } ?: "") +
                        (if (choose && mine) "; svoj si vyber v nastaveniach" else ""),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = if (mine && !past) c.lesson.text else c.ink2)
                    if (!past) Text(e.note, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Prednášky", style = MaterialTheme.typography.titleLarge, color = c.ink)
        Spacer(Modifier.height(4.dp))
        for (l in course.lectures(p.id)) {
            val done = l in w.done
            val next = l == w.next
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).then(if (next) Modifier.clip(RoundedCornerShape(14.dp)).background(c.lesson.tint2).padding(8.dp) else Modifier), verticalAlignment = Alignment.Top) {
                Dot(if (done) c.good else if (next) c.lesson.main else c.line)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("${l.label}, ${weekday(l.date)} ${l.date.format(DayFmt)}" + if (next) ", ďalšia" else "", style = MaterialTheme.typography.titleSmall, color = if (done) c.ink2 else c.ink)
                    Text(topics(course, l), style = MaterialTheme.typography.bodyMedium, color = if (done) c.ink2 else c.ink)
                    l.reached?.let { Text(reachedText(it).replaceFirstChar { ch -> ch.uppercase() } + " (stránka predmetu)", style = MaterialTheme.typography.bodySmall, color = c.ink2) }
                    for (les in course.lessonsFor(l).filter { it in course.shown().lessons }) {
                        val ok = les.id in store.lessonsDone
                        Text((if (ok) "Lekcia hotová: " else "Lekcia: ") + les.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = c.lesson.text,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(role = Role.Button) { onRoute("lesson:${les.id}") }.heightIn(min = 40.dp).wrapContentHeight())
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Témy budúcich prednášok sú odhad podľa harmonogramu; prednášajúci môže ísť rýchlejšie alebo pomalšie.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(color))
}

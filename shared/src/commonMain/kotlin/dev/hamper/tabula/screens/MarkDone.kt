package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*

/*
 * "Označiť hotové lekcie" in Nastavenia: tick lessons you already have (from the lectures, or from the old Tabuľa 1.2.x
 * whose progress could not be moved). They count as finished in the path and the plan, nothing else: no answers, no
 * review history, no telemetry. Never shown on its own: new users must not meet it on the first start (Teo, 16. 11.).
 * Everything is in this file; Nastavenia calls [MarkDoneEntry].
 */

/** Lessons ticked by hand count as finished: nothing else is recorded (no attempts, no mistakes, no telemetry). */
fun markLessonsDone(store: Store, ids: Collection<String>) {
    val add = ids.filter { it !in store.lessonsDone }
    if (add.isEmpty()) return
    store.lessonsDone.addAll(add)
    store.save()
}

/** The quiet row in Nastavenia, always there; it opens the list over the screen. */
@Composable
fun MarkDoneEntry(store: Store) {
    var open by remember { mutableStateOf(false) }
    Spacer(Modifier.height(10.dp))
    Second("Označiť hotové lekcie", { open = true })
    if (open) androidx.compose.ui.window.Dialog({ open = false }, androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        AppColumn { MarkDoneScreen(store) { open = false } }
    }
}

@Composable
internal fun MarkDoneScreen(store: Store, onDone: () -> Unit) {
    val c = T.c
    val ticked = remember { mutableStateListOf<String>() }
    StepFrame(0, 0, onDone, closeLabel = "Zavrieť", footer = {
        Cta(if (ticked.isEmpty()) "Zavrieť" else "Hotovo, ${ticked.size} ${slovakPlural(ticked.size, "lekcia", "lekcie", "lekcií")}", {
            markLessonsDone(store, ticked); onDone()
        })
    }) {
        Spacer(Modifier.height(4.dp))
        H1("Označ hotové lekcie.")
        Spacer(Modifier.height(4.dp))
        Sub("Lekcie, ktoré už máš z prednášok, budú v Ceste hotové. Odpovede sa tým nemenia, príklady z nich začnú chodiť v opakovaní.")
        for (course in Courses) {
            val ls = course.shown().lessons
            if (ls.isEmpty()) continue
            Spacer(Modifier.height(14.dp))
            Text("${course.short}, ${course.name}", style = MaterialTheme.typography.titleMedium, color = c.ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(6.dp))
            var part = ""
            for (l in ls) {
                val p = course.partName(l)
                if (p != part) { part = p; Label(p, Modifier.padding(top = 6.dp, bottom = 4.dp)) }
                CheckRow(l.name, l.id in ticked || l.id in store.lessonsDone, { on -> if (l.id !in store.lessonsDone) { if (on) ticked.add(l.id) else ticked.remove(l.id) } }, Modifier.padding(bottom = 6.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

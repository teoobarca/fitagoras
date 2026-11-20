package dev.hamper.tabula.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.VectorsResult
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Vec
import dev.hamper.tabula.la.tex
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.res.*

/*
 * The student's own list of vectors (Answer.Vectors): one vector is typed cell by cell on the same keypad as a numbers
 * answer, "Pridať" puts it in the list, an entry can be removed before checking. After checking, each entry shows its
 * own verdict and the summary says what is missing. Hosted by ExerciseView, so lessons, practice and review share it.
 */

/** The input state of one Answer.Vectors part; [result] is set after checking and cleared by any edit. */
class VectorsState(val a: Answer.Vectors) {
    /** Finished entries: each [Answer.Vectors.group] vectors. */
    val entries = mutableStateListOf<List<Vec>>()
    /** Vectors of the entry being written (a subspace by two generators fills two). */
    val pending = mutableStateListOf<Vec>()
    val cells = mutableStateListOf<String>().apply { repeat(a.cells.size) { add("") } }
    var field by mutableIntStateOf(0)
    var result by mutableStateOf<VectorsResult?>(null)
    /** The entry being rewritten in place (its number stays), or null when a new one is typed. */
    var editing by mutableStateOf<Int?>(null)
    /** Entries rewritten since the last check: their old remark no longer applies, the others keep theirs. */
    val stale = mutableStateListOf<Int>()

    private val values get() = cells.map(::parseFrac)
    val cellsFull get() = values.all { it != null }
    val cellsEmpty get() = cells.all { it.isEmpty() }
    val full get() = editing == null && entries.size >= a.count.last

    fun key(k: String) {
        if (k == "⌫") {
            if (cells[field].isEmpty() && field > 0) field--
            cells[field] = cells[field].dropLast(1)
            return
        }
        val s = cells[field]
        cells[field] = when (k) {
            "−" -> if (s.startsWith("−")) s.drop(1) else "−$s"
            "/" -> if ('/' in s || s.isEmpty()) s else "$s/"
            else -> if (a.p != null) k else if (s.length < 6) s + k else s
        }
        // in Z_p a cell is one digit: jump to the next one
        if (a.p != null && field < cells.size - 1) field++
    }

    /** Puts the typed vector into the entry being written, and the entry into the list when it has its vectors. */
    fun add() {
        if (!cellsFull || full) return
        result = null
        val v = values.map { it!! }
        for (i in cells.indices) cells[i] = ""
        field = 0
        editing?.let { i -> entries[i] = listOf(v); editing = null; stale += i; return }
        pending += v
        if (pending.size == a.group) { entries += pending.toList(); pending.clear() }
    }

    fun remove(i: Int) {
        result = null; stale.clear()
        entries.removeAt(i)
        editing = editing?.let { e -> if (e == i) null else if (e > i) e - 1 else e }
    }

    /** Loads entry [i] back into the cells; "Pridať" then puts it back at the same place (one-vector entries). */
    fun edit(i: Int) {
        // a half-typed new vector is not thrown away by a tap on the list
        if (a.group != 1 || (editing == null && !cellsEmpty)) return
        entries[i][0].forEachIndexed { j, x -> cells[j] = x.toString().replace("-", "−") }
        field = 0
        editing = i
    }

    /** A whole vector from [Answer.Vectors.quick] at one tap, as if typed and added. */
    fun put(v: Vec) {
        if (full || editing != null || a.group != 1) return
        result = null
        entries += listOf(v)
    }

    /** Drops the vectors typed so far of an entry of several vectors. */
    fun dropPending() { pending.clear() }

    /** What is checked: the list, with a completely typed last vector added first. */
    fun submit(): List<List<Vec>> { if (cellsFull && editing == null && pending.size + 1 == a.group) add(); return entries.toList() }
    /** A half-typed vector or entry blocks the check: it would be left out silently. */
    val canCheck get() = editing == null && pending.isEmpty() && (cellsEmpty || (cellsFull && a.group == 1)) && (entries.size + (if (cellsFull && a.group == 1) 1 else 0)) in a.count

    /** Debug walker: fills the right answer (never in a release build). */
    fun fillExample() { entries.clear(); pending.clear(); entries.addAll(a.example); for (i in cells.indices) cells[i] = "" }
}

private fun entryText(e: List<Vec>) = if (e.size == 1) "$${e[0].tex()}$" else "$\\langle " + e.joinToString(",") { it.tex() } + "\\rangle$"

/** The list and the vector being typed: shown above the keypad. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun VectorsInput(st: VectorsState, enabled: Boolean, firstAnswer: Modifier = Modifier) {
    val c = T.c
    val a = st.a
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // with one-tap vectors the line is short: the chips are the first answer and stay on the first screen (snapFold)
        if (a.quick.isNotEmpty()) Rich("Ťukni na vektory, iné napíš na klávesnici." + (if (a.open) "" else " Spolu ${a.count.last}."), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        else Rich("Zložky vektora píš na klávesnici a ťukni na Pridať." +
            (if (a.group > 1) " Jeden podpriestor tvoria ${a.group} vektory za sebou." else " Zápis opravíš ťuknutím naň.") +
            (if (a.open) " Keď máš všetko, skontroluj." else " Spolu ${a.count.last}."), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        st.entries.forEachIndexed { i, e ->
            val res = st.result
            val fb = res?.each?.getOrNull(i)?.takeIf { i !in st.stale }
            // an entry without its own remark is green only when the whole answer is right
            val mark = when { res == null || st.editing == i || i in st.stale -> 0; fb != null -> -1; res.ok -> 1; else -> 0 }
            val fill by animateColorAsState(when (mark) { 1 -> c.goodTint; -1 -> c.badTint; else -> if (st.editing == i) T.hue.tint else c.card }, tween(180), label = "vf")
            val line by animateColorAsState(when (mark) { 1 -> c.good; -1 -> c.bad; else -> if (st.editing == i) T.hue.main else c.line }, tween(180), label = "vl")
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(fill).border(1.5.dp, line, RoundedCornerShape(16.dp)).padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}.", style = MaterialTheme.typography.titleMedium, color = c.ink2, modifier = Modifier.width(30.dp))
                    Rich(entryText(e), Modifier.weight(1f).heightIn(min = 48.dp).then(if (enabled && a.group == 1) Modifier.clickable(role = Role.Button) { st.edit(i) }.semantics { contentDescription = "Opraviť ${i + 1}." } else Modifier).wrapContentHeight(Alignment.CenterVertically), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    if (enabled) Box(
                        Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button) { st.remove(i) }.semantics { contentDescription = "Odstrániť ${i + 1}." },
                        contentAlignment = Alignment.Center,
                    ) { Icon24(Res.drawable.ic_close, tint = c.ink3, modifier = Modifier.size(18.dp)) }
                    else Spacer(Modifier.height(48.dp))
                }
                if (fb != null) Rich(fb, Modifier.padding(end = 8.dp, bottom = 4.dp), style = MaterialTheme.typography.bodyMedium, color = c.ink)
            }
        }
        if (enabled && !st.full) {
            if (st.pending.isNotEmpty()) {
                Rich("Už máš: " + st.pending.joinToString(", ") { "$${it.tex()}$" } + ". Ešte ${a.group - st.pending.size}.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                Quiet("Zrušiť rozpísaný podpriestor", { st.dropPending() })
            }
            // whole vectors at one tap (e₁ … e₄ to complete a basis): only while nothing is half-typed
            if (a.quick.isNotEmpty() && st.cellsEmpty && st.editing == null) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for ((qi, q) in a.quick.withIndex()) Choice("$${q.first}$", { st.put(q.second) }, Modifier.widthIn(min = 64.dp).then(if (qi == 0) firstAnswer else Modifier).semantics { contentDescription = "Pridať " + q.first.replace("\\mathbf{e}_{", "e").replace("}", "") }, center = true, minHeight = 44.dp, style = MaterialTheme.typography.bodyLarge, quiet = true)
            }
            CellRows(a.cells.size, MaterialTheme.typography.titleMedium, 6.dp) { i, m ->
                Choice(if (st.cells[i].isEmpty()) "$${a.cells[i]}$" else st.cells[i], { st.field = i }, m.then(if (i == 0 && a.quick.isEmpty()) firstAnswer else Modifier), state = if (st.field == i) Pick.Picked else Pick.Idle, center = true, minHeight = 52.dp, style = MaterialTheme.typography.titleMedium)
            }
        }
        // the debug walker answers through this one (dbg builds only)
        if (enabled && dbg("pick") != null && st.entries.isEmpty()) Box(
            Modifier.size(48.dp).clickable { st.fillExample() }.semantics { contentDescription = dbg("pick")!! },
        )
    }
}

/** The keypad and "Pridať" under the list; [onCheck] judges the list. */
@Composable
fun VectorsPad(st: VectorsState, onCheck: () -> Unit) {
    val a = st.a
    if (!st.full) KeyPad(Pad, st::key, off = padOff(a.p, true))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!st.full) Second(if (st.editing != null) "Uložiť" else "Pridať", { st.add() }, Modifier.weight(1f), enabled = st.cellsFull)
        Cta("Skontrolovať", onCheck, Modifier.weight(1f), enabled = st.canCheck)
    }
}

/** How a finished Vectors part is shown among the done parts. */
fun vectorsText(v: Any?): String = (v as? List<*>)?.mapNotNull { e -> (e as? List<*>)?.filterIsInstance<List<Frac>>() }?.joinToString(", ", transform = ::entryText) ?: ""

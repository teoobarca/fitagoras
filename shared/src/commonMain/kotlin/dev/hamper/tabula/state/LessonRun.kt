package dev.hamper.tabula.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject

/**
 * A lesson in progress as ONE history: its steps in order, and inside each step the screens it showed in order (a
 * definition's text, why, each example, the trap, writing, comparing; a theorem and its proof; a clip before, during,
 * after). Back is always exactly one screen of that history: the previous screen of this step, else the screen the
 * previous step was left on; on the very first screen back leaves the lesson. Nothing is ever dropped: a step's
 * answers ([mem]) stay, so a step gone back to shows itself answered and is not graded again; leaving and coming back
 * (also after the app was killed) opens the same screen.
 *
 * Steps are named by id (content may gain steps between versions); [at] and [furthest] are indices.
 * Before this model each screen kept its own state and some kept their own back handler, so back from inside a
 * multi-screen step left the whole step (several screens at once) and every step came back unanswered.
 */
class LessonRun(val ids: List<String>, private val changed: () -> Unit = {}) {
    /** The step on screen. */
    var at by mutableIntStateOf(0)
        private set
    /** The furthest step reached (where the lesson resumes after an older install). */
    var furthest by mutableIntStateOf(0)
        private set
    /** Which way the last move went (for the motion: forward slides in from the right). */
    var forward by mutableStateOf(true)
        private set
    /** Per step id, the screens it showed in order; the last one is where it stands. Absent = only its first screen. */
    private val subs = mutableStateMapOf<String, List<Int>>()
    /** Steps the lesson left out (a problem type already mastered): not in the history, back passes over them. */
    private val skipped = mutableStateMapOf<String, Boolean>()
    /**
     * Steps that were on screen. The step list jumps anywhere, also ahead (Teo, 18. 11.: "mohol by som mať možnosť
     * skipnúť na ďalšie kroky"), so the steps passed over in a jump are not seen and the lesson is not done with them.
     */
    private val seen = mutableStateMapOf<String, Boolean>()
    /** Per step id, what the student did there (picks, how far a proof got), as strings. Read once per screen. */
    private val mem = mutableMapOf<String, MutableMap<String, String>>()

    val size get() = ids.size
    val id get() = ids[at]

    /** The screen step [i] stands on. */
    fun sub(i: Int = at): Int = subs[ids[i]]?.lastOrNull() ?: 0
    /** The screens step [i] showed, in order. */
    fun trail(i: Int = at): List<Int> = subs[ids[i]] ?: listOf(0)
    fun skipped(i: Int): Boolean = skipped[ids[i]] == true
    /** A step with anything remembered: it was on screen in this run. */
    fun visited(i: Int): Boolean = seen[ids[i]] == true && !skipped(i)
    /** Steps of the lesson never on screen and not left out: what a jump ahead passed over. */
    fun unseen(): List<Int> = ids.indices.filter { !visited(it) && !skipped(it) }

    private fun show(k: Int) { at = k; if (k > furthest) furthest = k; seen[ids[k]] = true }

    fun get(step: String, key: String): String? = mem[step]?.get(key)
    fun put(step: String, key: String, value: String?) {
        val m = mem.getOrPut(step) { mutableMapOf() }
        if (m[key] == value) return
        if (value == null) m.remove(key) else m[key] = value
        changed()
    }

    /**
     * Forgets what step [step] kept under keys starting with [prefix], without a save of its own (the next change
     * saves it): read back while a screen is being built, so no state is written then.
     */
    fun forget(step: String, prefix: String) { mem[step]?.keys?.removeAll { it.startsWith(prefix) } }

    /** A new screen inside the step on screen. Coming back to an earlier screen and on again keeps what follows. */
    fun open(sub: Int) {
        val t = trail()
        if (t.last() == sub) return
        forward = true
        subs[id] = t + sub
        changed()
    }

    /**
     * On to the next step; steps [skip] says to leave out (only ones never shown in this run) are passed over.
     * Returns false at the end of the lesson.
     */
    fun next(skip: (Int) -> Boolean = { false }): Boolean {
        var k = at + 1
        // a step left out stays out (also when the lesson is walked again after going back); a new one is decided once
        while (k < size && (skipped(k) || (k > furthest && skip(k)))) { skipped[ids[k]] = true; k++ }
        if (k >= size) return false
        forward = true
        show(k)
        changed()
        return true
    }

    /** One screen back. False on the first screen of the lesson: then back leaves it (and this run stays as it is). */
    fun back(): Boolean {
        val t = trail()
        if (t.size > 1) { forward = false; subs[id] = t.dropLast(1); changed(); return true }
        var k = at - 1
        while (k >= 0 && skipped(k)) k--
        if (k < 0) return false
        forward = false
        show(k)
        changed()
        return true
    }

    /** Straight to step [k] (the step list), back or ahead; the steps jumped over stay unseen. */
    fun jump(k: Int): Boolean {
        if (k !in ids.indices || skipped(k) || k == at) return false
        forward = k > at
        show(k)
        changed()
        return true
    }

    fun json(): JSONObject = JSONObject().apply {
        // the ids too: a new version may add or drop steps, the index alone would then point at another step
        put("at", at); put("furthest", furthest); put("atId", ids[at]); put("furthestId", ids[furthest])
        put("subs", JSONObject().apply { subs.forEach { (k, v) -> put(k, JSONArray(v)) } })
        put("skipped", JSONArray(skipped.filterValues { it }.keys.toList()))
        put("seen", JSONArray(seen.filterValues { it }.keys.toList()))
        put("mem", JSONObject().apply { mem.forEach { (k, m) -> if (m.isNotEmpty()) put(k, JSONObject().apply { m.forEach { (a, b) -> put(a, b) } }) } })
    }

    companion object {
        /** A run of a lesson with steps [ids] at step [at] (an older install remembered only that). */
        fun start(ids: List<String>, at: Int = 0, changed: () -> Unit = {}): LessonRun = LessonRun(ids, changed).apply {
            this.at = at.coerceIn(0, (ids.size - 1).coerceAtLeast(0)); furthest = this.at
            for (i in 0..this.at) if (i < ids.size) seen[ids[i]] = true
        }

        /** The saved run; steps no longer in the lesson are forgotten, positions kept within it. */
        fun parse(ids: List<String>, o: JSONObject, changed: () -> Unit = {}): LessonRun = LessonRun(ids, changed).apply {
            val last = (ids.size - 1).coerceAtLeast(0)
            fun place(id: String, index: String) = o.optString(id).let { ids.indexOf(it) }.takeIf { it >= 0 } ?: o.optInt(index, 0).coerceIn(0, last)
            furthest = place("furthestId", "furthest")
            at = place("atId", "at").coerceIn(0, furthest)
            o.optJSONObject("subs")?.let { m -> m.keys().forEach { k -> if (k in ids) { val a = m.getJSONArray(k); subs[k] = (0 until a.length()).map { a.getInt(it) }.ifEmpty { listOf(0) } } } }
            o.optJSONArray("skipped")?.let { a -> for (i in 0 until a.length()) a.getString(i).takeIf { it in ids }?.let { skipped[it] = true } }
            // a run saved before jumps ahead existed went step by step: everything up to the furthest was seen
            val sa = o.optJSONArray("seen")
            if (sa != null) for (i in 0 until sa.length()) sa.getString(i).takeIf { it in ids }?.let { seen[it] = true }
            else for (i in 0..furthest) if (i < ids.size) seen[ids[i]] = true
            if (ids.isNotEmpty()) seen[ids[at]] = true
            o.optJSONObject("mem")?.let { m -> m.keys().forEach { k -> if (k in ids) { val s = m.getJSONObject(k); mem[k] = s.keys().asSequence().associateWith { s.getString(it) }.toMutableMap() } } }
        }
    }
}

package dev.hamper.tabula.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.hamper.tabula.state.LessonRun

/**
 * The step [index] of a lesson [run]: which of its screens is on, and what was done there. Provided by the lesson
 * around each step; null outside a lesson (library, review, a full card), where a screen keeps its state only while
 * it is shown.
 */
class StepNav(val run: LessonRun, val index: Int) {
    val step: String = run.ids[index]
    /** The screen this step stands on (read from the run, so back and the step list move it). */
    val sub: Int get() = run.sub(index)
    /** Which way the lesson last moved, for the motion of a step's own screens. */
    val forward: Boolean get() = run.forward
    fun open(sub: Int) { if (run.at == index) run.open(sub) }
    fun get(key: String): String? = run.get(step, key)
    fun put(key: String, value: String?) = run.put(step, key, value)
    fun forget(prefix: String) = run.forget(step, prefix)
}

val LocalStepNav = compositionLocalOf<StepNav?> { null }

/** A state that writes every new value through [write]. */
private class Through<T>(initial: T, private val write: (T) -> Unit) : MutableState<T> {
    private val s = mutableStateOf(initial)
    override var value: T
        get() = s.value
        set(v) { if (v != s.value) { s.value = v; write(v) } }
    override fun component1(): T = value
    override fun component2(): (T) -> Unit = { value = it }
}

/**
 * A step's state that the lesson keeps: inside a lesson it is saved in the run under [key] (so going back, leaving
 * and coming back find it as it was); elsewhere a plain remembered state. [keys] start it over, as remember's do.
 */
@Composable
fun <T> kept(key: String, init: T, enc: (T) -> String?, dec: (String) -> T?, vararg keys: Any?): MutableState<T> {
    val nav = LocalStepNav.current
    // outside a lesson as before: saveable, so a full card opened over it and closed finds it as it was
    if (nav == null) return rememberSaveable(key, *keys) { mutableStateOf(init) }
    return remember(nav, key, *keys) {
        val saved = nav?.get(key)?.let { runCatching { dec(it) }.getOrNull() }
        Through(saved ?: init) { v -> nav.put(key, enc(v)) }
    }
}

@Composable fun keptInt(key: String, init: Int, vararg keys: Any?) = kept(key, init, { it.toString() }, { it.toIntOrNull() }, *keys)
@Composable fun keptBool(key: String, init: Boolean = false, vararg keys: Any?) = kept(key, init, { if (it) "1" else "0" }, { it == "1" }, *keys)
@Composable fun keptPick(key: String, vararg keys: Any?) = kept<Int?>(key, null, { it?.toString() }, { it.toIntOrNull() }, *keys)
@Composable fun keptInts(key: String, vararg keys: Any?) = kept(key, emptyList(), { l -> l.joinToString(",").ifEmpty { null } }, { s -> s.split(',').map { it.toInt() } }, *keys)

/**
 * Which of a step's own screens is on (a definition's phases, a proof's paper). Inside a lesson it is the run's:
 * a new screen is one more in the lesson's history, so back returns to the one before. [base]: the first screen
 * this flow owns within its step (a proof inside a theorem starts after the theorem's own screen).
 */
@Composable
fun keptSub(base: Int = 0): MutableState<Int> {
    val nav = LocalStepNav.current ?: return rememberSaveable { mutableStateOf(0) }
    return remember(nav, base) {
        object : MutableState<Int> {
            override var value: Int
                get() = (nav.sub - base).coerceAtLeast(0)
                set(v) { nav.open(base + v) }
            override fun component1(): Int = value
            override fun component2(): (Int) -> Unit = { value = it }
        }
    }
}

/**
 * A kept value as it fits the step now: what a newer version of the content no longer has (an option, an example,
 * a round of a proof) reads as [fallback], never as an index out of range.
 */
fun <T> MutableState<T>.fit(fallback: T, ok: (T) -> Boolean): MutableState<T> {
    val inner = this
    return object : MutableState<T> {
        override var value: T
            get() = inner.value.let { if (ok(it)) it else fallback }
            set(v) { inner.value = v }
        override fun component1(): T = value
        override fun component2(): (T) -> Unit = { value = it }
    }
}

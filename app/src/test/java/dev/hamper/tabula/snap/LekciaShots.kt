package dev.hamper.tabula.snap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.compat.media.ExoClipPlayer
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.screens.LocalGemStep
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.store
import org.junit.Test

/**
 * The screens inside a lesson, both courses, for judging their look: `-Psnap.test=LekciaShots [-Psnap.only=def-dml]`.
 * A returning student in the middle of the lesson; a proof one goal in, a worked example two steps in, a GEM walk
 * three operations in, a proof on paper.
 */
class LekciaShots : Shots() {
    private fun idx(id: String, k: Int, p: (Step) -> Boolean) = LessonById.getValue(id).steps.withIndex().filter { p(it.value) }[k].index

    private val cases: List<Triple<String, String, Int>> = listOf(
        Triple("vyklad-dml", "dml-tc-delitelnost", 2),
        Triple("vyklad-la1", "la1-k3-hodnost", 2),
        Triple("def-dml", "dml-tc-kongruencie", 3),
        Triple("def-la1", "la1-k3-inverzia", 3),
        Triple("veta-dml", "dml-tc-kongruencie", 6),
        Triple("veta-la1", "la1-k4-frobenius", 7),
        Triple("dokaz-dml", "dml-tc-kongruencie", 7),
        Triple("dokaz-la1", "la1-k3-inverzia", 12),
        Triple("priklad-dml", "dml-tc-delitelnost", 7),
        Triple("priklad-la1", "la1-k3-regularita", 4),
        Triple("euklid", "dml-tc-rea", -1),
        Triple("rea", "dml-tc-rea", -2),
        Triple("gem", "la1-k3-hodnost", -3),
        Triple("papier-dml", "dml-tc-kongruencie", 7),
        Triple("papier-la1", "la1-k3-inverzia", 12),
        Triple("klip-dml", "dml-tc-kongruencie", 2),
        Triple("klip-la1", "la1-k3-hodnost", 3),
    )

    @Test fun lekcia() {
        val only = Snap.get("only")?.split(',')
        for ((name, id, at) in cases) if (only == null || only.any { name == it }) {
            val step = when (at) {
                -1 -> idx(id, 0) { it is Step.Worked }
                -2 -> idx(id, 1) { it is Step.Worked }
                -3 -> idx(id, 0) { it is Step.Explore && it.pic is Pic.Gem }
                else -> at
            }
            shoot(name) {
                CompositionLocalProvider(LocalGemStep provides (if (name == "gem") 3 else 0)) {
                    Lesson(id, step) { l, s ->
                        val st = l.steps[step]
                        val run = s.run(l)
                        when {
                            name.startsWith("dokaz") -> run.put(st.id, "proof.round", "1")
                            name.startsWith("papier") -> run.open(1)
                            name.startsWith("priklad") || name == "euklid" || name == "rea" -> run.put(st.id, "shown", "2")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Lesson(id: String, step: Int, tweak: (dev.hamper.tabula.data.LessonDef, Store) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { Tabula.store(ctx) }
    val app = remember {
        AppState().also {
            val l = LessonById.getValue(id)
            store.courseId = if (id.startsWith("dml")) "dml" else "la1"
            LessonById.keys.forEach { k -> store.finishLesson(k) }
            store.lessonsDone.clear()
            store.statsNoticed = true
            store.symbolHintSeen = true
            store.restartLesson(l, step)
            tweak(l, store)
            store.save()
            it.push(Nav.Lesson(id))
        }
    }
    val clips = remember { ClipHost(ExoClipPlayer(ctx), onPip = {}) }
    DisposableEffect(clips) { onDispose { clips.release() } }
    App(app, store, clips, {}, {})
}

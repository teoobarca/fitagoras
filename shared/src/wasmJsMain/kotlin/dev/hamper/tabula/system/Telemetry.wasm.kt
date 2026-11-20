package dev.hamper.tabula.system

import dev.hamper.tabula.data.ClipDef

/** The web sends no statistics yet. */
actual object Telemetry {
    actual var enabled: Boolean = false
    actual fun switch(on: Boolean) { enabled = on }
    actual fun opened(courseId: String, parallel: String?) {}
    actual fun lessonStarted(lessonId: String) {}
    actual fun stepShown(lessonId: String, pos: Int) {}
    actual fun skipped(how: String) {}
    actual fun stepLeft() {}
    actual fun lessonLeft() {}
    actual fun lessonFinished(lessonId: String) {}
    actual fun given(key: String, text: String) {}
    actual fun seed(key: String, seed: String) {}
    actual fun answer(key: String, ok: Boolean, aided: Boolean, attempt: Int, courseId: String, fallbackText: String?) {}
    actual fun help(kind: String, key: String?) {}
    actual fun clipPlayed(clip: ClipDef, watched: Double, lecture: String) {}
    actual fun mockTest(courseId: String, paper: String, score: Int, max: Int) {}
    actual fun update(stage: String, version: String?) {}
    actual fun report(item: String, text: String, courseId: String) {}
}

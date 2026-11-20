package dev.hamper.tabula.system

import dev.hamper.tabula.data.ClipDef
import java.io.File
import java.nio.file.Files

/**
 * The desktop JVM (tests, screenshots): the same Tracker as Android, sending nothing until [connect] points it at a
 * server (a test does; the desktop is not a shipped app). [flush] sends at once, on the caller's thread.
 */
actual object Telemetry {
    private var tracker: Tracker? = null
    private var server: Pair<String, String>? = null
    private var ctx: Context? = null
    private var device: String? = null

    /** Sends to [url] with [key] from now on; a fresh queue in [dir] (a temp folder by default). */
    @Synchronized
    fun connect(url: String, key: String, dir: File = Files.createTempDirectory("telemetry").toFile(), prefs: Prefs = MapPrefs(),
                device: String? = null, ctx: Context? = null, now: () -> Long = { System.currentTimeMillis() }) {
        tracker = Tracker(dir, prefs, now = now)
        server = url to key
        this.device = device
        this.ctx = ctx
    }

    @Synchronized
    fun disconnect() { tracker = null; server = null }

    /** What waits to be sent (tests read it). */
    fun waiting(): List<Event> = tracker?.events?.all().orEmpty()

    /** Sends what waits. -> false: the server did not take all of it. */
    @Synchronized
    fun flush(app: String = "desktop", debug: Boolean = false): Boolean {
        val t = tracker ?: return true
        val (url, key) = server ?: return true
        return t.send(app, debug, device, ctx) { path, body -> Tracker.post(url, key, path, body) }
    }

    actual var enabled: Boolean
        get() = tracker?.enabled ?: false
        set(value) { tracker?.switch(value) }
    actual fun switch(on: Boolean) { tracker?.switch(on) }
    actual fun opened(courseId: String, parallel: String?) { tracker?.opened(courseId, parallel) }
    actual fun lessonStarted(lessonId: String) { tracker?.lessonStarted(lessonId) }
    actual fun stepShown(lessonId: String, pos: Int) { tracker?.stepShown(lessonId, pos) }
    actual fun skipped(how: String) { tracker?.skipped(how) }
    actual fun stepLeft() { tracker?.stepLeft() }
    actual fun lessonLeft() { tracker?.lessonLeft() }
    actual fun lessonFinished(lessonId: String) { tracker?.lessonFinished(lessonId) }
    actual fun given(key: String, text: String) { tracker?.given(key, text) }
    actual fun seed(key: String, seed: String) { tracker?.seed(key, seed) }
    actual fun answer(key: String, ok: Boolean, aided: Boolean, attempt: Int, courseId: String, fallbackText: String?) {
        tracker?.answer(key, ok, aided, attempt, courseId, fallbackText)
    }
    actual fun help(kind: String, key: String?) { tracker?.help(kind, key) }
    actual fun clipPlayed(clip: ClipDef, watched: Double, lecture: String) { tracker?.clipPlayed(clip, watched, lecture) }
    actual fun mockTest(courseId: String, paper: String, score: Int, max: Int) { tracker?.mockTest(courseId, paper, score, max) }
    actual fun update(stage: String, version: String?) { tracker?.update(stage, version) }
    actual fun report(item: String, text: String, courseId: String) { tracker?.report(item, text, courseId) }
}

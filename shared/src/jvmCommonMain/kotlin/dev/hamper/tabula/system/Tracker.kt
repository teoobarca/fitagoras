package dev.hamper.tabula.system

import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.data.ClipDef
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.LessonDef
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.data.TheoremById
import dev.hamper.tabula.screens.ClipLibrary
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** The few settings the statistics keep (SharedPreferences on Android, a map in tests). */
interface Prefs {
    fun get(key: String): String?
    fun put(key: String, value: String?)
    fun keys(): Set<String>
}

class MapPrefs : Prefs {
    private val m = HashMap<String, String>()
    @Synchronized override fun get(key: String) = m[key]
    @Synchronized override fun put(key: String, value: String?) { if (value == null) m.remove(key) else m[key] = value }
    @Synchronized override fun keys() = m.keys.toSet()
}

/**
 * Everything the statistics do besides talking to Android: which events a tap makes and what they carry, the queue on
 * disk, and sending it. Telemetry (Android) and the desktop JVM wrap one of these; the JVM tests drive it against a
 * real server. Events go only while [enabled]; nothing here blocks the caller beyond adding a line to a small file (and
 * the Android wrapper does that off the main thread, [enqueue]).
 *
 * What it knows of the moment (the lesson and step on screen, when it appeared, a "Viem to" just tapped) lives only in
 * memory: an answer or a help tap is then placed in its lesson and step without the screens passing it around.
 */
class Tracker(
    dir: File,
    private val prefs: Prefs,
    private val now: () -> Long = { System.currentTimeMillis() },
    private val today: () -> LocalDate = { LocalDate.now() },
    /** Adds an event to the queue file; Android does it on its disk thread, then plans a send. */
    private val enqueue: (() -> Unit) -> Unit = { it() },
    /** After an event is queued ([soon]: a report, send now). */
    private val queued: (soon: Boolean) -> Unit = {},
) {
    val events = JsonQueue(File(dir, "events.jsonl"), MaxQueued, Event::json, Event::from, Event::id)
    val reports = JsonQueue(File(dir, "reports.jsonl"), MaxReports, ErrorReport::stored, ErrorReport::from, ErrorReport::id)

    @Volatile var enabled: Boolean = prefs.get("enabled") != "0"
        private set

    /** Statistics on or off (Nastavenia). Off drops what waits and forgets the install id; on again starts a new id. */
    fun switch(on: Boolean) {
        enabled = on
        prefs.put("enabled", if (on) "1" else "0")
        if (!on) {
            // the student chose to stop: switched on again it is a new history, not tied to this phone's earlier one;
            // the server is told to drop the phone's hash too, so not even a reinstall joins it again
            prefs.put("device", "off")
            prefs.get("install")?.let { prefs.put("forget", it) }
            prefs.put("install", null)
            prefs.keys().filter { it.startsWith("opened:") }.forEach { prefs.put(it, null) }
            enqueue { events.clear(); queued(true) }
        }
    }

    /** Random, made once, kept in the app's own storage: an update keeps it (the app's data stays). */
    fun installId(): String = prefs.get("install") ?: UUID.randomUUID().toString().also { prefs.put("install", it) }

    /* ---- the moment ---------------------------------------------------------------------------------------------- */

    private class Here(val lesson: String, val course: String, val pos: Int, val of: Int, val step: String, val kind: String, val since: Long)

    @Volatile private var here: Here? = null
    /** The course last opened (server code): help and update events belong to it. */
    @Volatile private var current: String? = null
    @Volatile private var skip: String? = null
    private val lessonMs = HashMap<String, Long>()
    /** A wrong answer as entered, and the numbers of an exercise, until its answer is recorded. */
    private val given = HashMap<String, String>()
    private val seeds = HashMap<String, String>()

    private fun spent(since: Long): Long? = (now() - since).takeIf { it in 0..MaxMs }

    /* ---- what happens in the app ------------------------------------------------------------------------------- */

    /** A sitting begins: the app came to the front after [Sitting] or more without an event (or for the first time
     *  since it started), or the parallel changed. With the lecture parallel picked (null: the app's default). */
    fun opened(courseId: String, parallel: String?) {
        val course = serverCourse(courseId) ?: return
        val mark = parallel.orEmpty()
        current = course
        if (now() - lastEvent < Sitting && prefs.get("opened:$course") == mark) return
        if (log(event("open", course, parallel = safeParallel(parallel)))) prefs.put("opened:$course", mark)
    }

    fun lessonStarted(lessonId: String) {
        val lesson = lessons[lessonId] ?: return
        val course = courseOfLesson(lesson.id) ?: return
        log(event("lesson_started", course, lesson = safeId(lesson.id) ?: return, of = lesson.steps.size, text = lesson.name))
    }

    /** The step [pos] of [lesson] is on screen (its clock starts). */
    fun stepShown(lessonId: String, pos: Int) {
        val lesson = lessons[lessonId] ?: return
        val course = courseOfLesson(lesson.id) ?: return
        val step = lesson.steps.getOrNull(pos) ?: return
        here = Here(lesson.id, course, pos, lesson.steps.size, step.id, kindOf(step), now())
        skip = null
    }

    /** "Viem to" (known) or "Teraz nie" (later) on the step on screen: how the next [stepLeft] says it was left. */
    fun skipped(how: String) { skip = how }

    /** The student went on from the step on screen. */
    fun stepLeft() {
        val h = here ?: return
        val ms = spent(h.since)
        if (ms != null) lessonMs[h.lesson] = (lessonMs[h.lesson] ?: 0) + ms
        log(event("step", h.course, lesson = safeId(h.lesson), step = safeId(h.step), pos = h.pos, of = h.of, kind = safeKind(h.kind),
            how = skip ?: "done", ms = ms, text = stepName(h.step)))
        skip = null
    }

    /** The lesson was closed on the step on screen, not finished. */
    fun lessonLeft() {
        val h = here ?: return
        here = null
        log(event("lesson_left", h.course, lesson = safeId(h.lesson), step = safeId(h.step), pos = h.pos, of = h.of, kind = safeKind(h.kind), ms = spent(h.since)))
        lessonMs.remove(h.lesson)
    }

    fun lessonFinished(lessonId: String) {
        val lesson = lessons[lessonId] ?: return
        val course = courseOfLesson(lesson.id) ?: return
        here = null
        log(event("lesson_finished", course, lesson = safeId(lesson.id) ?: return, of = lesson.steps.size, ms = lessonMs.remove(lesson.id), text = lesson.name))
    }

    /** What the student entered for [key] and got wrong; sent with its answer, then forgotten. */
    fun given(key: String, text: String) { synchronized(given) { given[key] = text } }
    fun seed(key: String, seed: String) { synchronized(seeds) { seeds[key] = seed } }

    /** An answer to [key] (an attempt key: "step:<id>", "gen:<template>", "clip:<id>", ...), the [attempt]-th ever. */
    fun answer(key: String, ok: Boolean, aided: Boolean, attempt: Int, courseId: String, fallbackText: String?) {
        val info = item(key, courseId)
        val course = info.course ?: return
        val item = safeId(key) ?: return
        val wrong = synchronized(given) { given.remove(key) }
        val seed = synchronized(seeds) { seeds[key] }
        val h = here?.takeIf { it.lesson == info.lesson || info.lesson == null }
        log(event("answer", course, lesson = safeId(info.lesson ?: h?.lesson), item = item, ok = ok, aided = aided, text = info.text ?: fallbackText,
            source = info.source, attempt = attempt, given = if (ok) null else wrong, seed = safeId(seed),
            step = safeId(h?.step), pos = h?.pos, kind = safeKind(h?.kind ?: key.substringBefore(':')), ms = h?.let { spent(it.since) }))
    }

    /** A help button: hint ("Nápoveda"), howto ("Ako na to?"), why ("Prečo?"), solution ("Riešenie", "Ukáž riešenie"). */
    fun help(kind: String, key: String?) {
        val h = here
        val course = h?.course ?: current ?: return
        log(event("help", course, kind = kind, item = safeId(key), lesson = safeId(h?.lesson), step = safeId(h?.step), pos = h?.pos))
    }

    fun clipPlayed(clip: ClipDef, watched: Double, lecture: String) {
        val course = serverCourse(clip.course) ?: return
        log(event("clip_played", course, clip = safeId(clip.id) ?: return, watched = watched.coerceIn(0.0, 1.0), text = clip.before, source = lecture))
    }

    fun mockTest(courseId: String, paper: String, score: Int, max: Int) {
        val course = serverCourse(courseId) ?: return
        if (max <= 0) return
        log(event("mock_test", course, test = safeId(paper) ?: return, score = score.coerceIn(0, max), max = max))
    }

    /** The update banner shown (once a version), its button tapped, the new version's first start ([version]: the old one). */
    fun update(stage: String, version: String?) {
        val course = current ?: return
        if (stage == "offered") { if (prefs.get("offered") == version) return; prefs.put("offered", version) }
        log(event("update", course, kind = stage, version = version?.takeIf { VersionShape.matches(it) }))
    }

    /** At start: a version other than last time = an update; the first start ever is not one. */
    fun started(version: String, courseId: String) {
        val last = prefs.get("version")
        if (last != version) {
            prefs.put("version", version)
            if (last != null) { current = current ?: serverCourse(courseId); update("installed", last) }
        }
    }

    /** A crash written down when it happened ([crashFile]), sent at the next start. */
    fun crashed(kind: String, message: String?, stack: String, courseId: String) {
        val course = serverCourse(courseId) ?: return
        log(event("crash", course, kind = safeKind(kind) ?: "unknown", message = message, stack = stack))
    }

    /** "Nahlásiť chybu": always sent (with statistics off under a one-off id). [item] as the report button names it. */
    fun report(item: String, text: String, courseId: String) {
        val key = attemptKey(item)
        val info = item(key, courseId)
        val course = info.course ?: return
        val r = ErrorReport(UUID.randomUUID().toString(), now(), course, text.trim(), safeId(key), info.text, safeId(info.lesson ?: here?.lesson),
            safeId(info.clip?.id), safeId(info.clip?.rec), info.clip?.from, info.source, anon = !enabled)
        enqueue { reports.add(r); queued(true) }
    }

    private fun event(
        type: String, course: String, lesson: String? = null, item: String? = null, ok: Boolean? = null, aided: Boolean? = null,
        clip: String? = null, test: String? = null, score: Int? = null, max: Int? = null, text: String? = null, source: String? = null,
        parallel: String? = null, watched: Double? = null, step: String? = null, pos: Int? = null, of: Int? = null, kind: String? = null,
        how: String? = null, ms: Long? = null, given: String? = null, attempt: Int? = null, seed: String? = null, version: String? = null,
        message: String? = null, stack: String? = null,
    ) = Event(UUID.randomUUID().toString(), today().toString(), type, course, lesson, item, ok, aided, clip, test, score, max, text, source,
        parallel, watched, now(), step, pos, of, kind, how, ms, given, attempt, seed, version, message, stack)

    @Volatile private var lastEvent = 0L

    private fun log(e: Event): Boolean {
        if (!enabled) return false
        lastEvent = now()
        enqueue { events.add(e); queued(false) }
        return true
    }

    /* ---- what an id means ------------------------------------------------------------------------------------- */

    private data class Info(val course: String?, val lesson: String? = null, val text: String? = null, val source: String? = null, val clip: ClipDef? = null)

    private val lessons: Map<String, LessonDef> by lazy { Courses.flatMap { it.lessons }.associateBy { it.id } }

    private val steps: Map<String, Pair<LessonDef, Step>> by lazy {
        Courses.flatMap { c -> c.lessons }.flatMap { l -> l.steps.map { it.id to (l to it) } }.toMap()
    }

    private fun courseOfLesson(lessonId: String): String? =
        Courses.firstOrNull { c -> c.lessons.any { it.id == lessonId } }?.let { serverCourse(it.id) }

    /** A report names a step by its bare id and a template by its name; answers are keyed "step:" and "gen:". */
    private fun attemptKey(item: String): String = when {
        ':' in item -> item
        item in steps -> "step:$item"
        item in Generators -> "gen:$item"
        else -> item
    }

    /** A step's line on the admin page: its question or title. */
    private fun stepName(id: String): String? = when (val s = steps[id]?.second) {
        is Step.Ask -> s.q
        is Step.Explore -> s.title
        is Step.Explain -> s.title
        is Step.Worked -> s.title
        is Step.Check -> s.q
        is Step.Practice -> s.title ?: runCatching { Generators[s.gen]?.invoke(kotlin.random.Random(0))?.label }.getOrNull()
        is Step.Clip -> runCatching { ClipLibrary.byId(s.clip).task?.q ?: ClipLibrary.byId(s.clip).before }.getOrNull()
        is Step.Def -> ConceptById[s.concept]?.name
        is Step.Thm -> TheoremById[s.theorem]?.name
        is Step.Prove -> ProofById[s.proof]?.title
        null -> null
    }

    private fun item(key: String, courseId: String): Info {
        val (kind, id) = key.substringBefore(':') to key.substringAfter(':')
        val clip = when (kind) {
            "clip" -> runCatching { ClipLibrary.byId(id) }.getOrNull()
            "step" -> (steps[id]?.second as? Step.Clip)?.let { s -> runCatching { ClipLibrary.byId(s.clip) }.getOrNull() }
            else -> null
        }
        val found = if (kind == "step") steps[id] else null
        val lesson = found?.first
        val course = lesson?.let { courseOfLesson(it.id) } ?: clip?.let { serverCourse(it.course) } ?: serverCourse(courseId)
        val (text, source) = when (val s = found?.second) {
            is Step.Ask -> s.q to null
            is Step.Check -> s.q to null
            is Step.Explain -> s.title to s.src
            is Step.Worked -> s.title to s.src
            is Step.Clip -> (clip?.task?.q) to clip?.lecture
            else -> (clip?.task?.q) to clip?.lecture
        }
        return Info(course, lesson?.id, text, source, clip)
    }

    /* ---- sending ---------------------------------------------------------------------------------------------- */

    /**
     * Sends what waits: reports first, then events in batches. [post] (path, body) -> HTTP status or null (offline).
     * -> false: something must be tried again later. One caller at a time (the wrappers hold a lock).
     */
    fun send(app: String, debug: Boolean, device: String?, ctx: Context?, post: (String, String) -> Int?): Boolean {
        var ok = true
        prefs.get("forget")?.let { old ->
            val body = JSONObject().put("install", old).apply { device?.let { put("device", it) } }.toString()
            // answered at all (an older server says 404): done; offline: next time
            if (post("/api/telemetry/forget", body) != null) prefs.put("forget", null) else ok = false
        }
        while (true) {
            val r = reports.all().firstOrNull() ?: break
            // written or sent with statistics off: a one-off id, so nothing ties the report to a timeline
            val who = if (r.anon || !enabled) UUID.randomUUID().toString() else installId()
            when (outcome(post("/api/telemetry/reports", reportBody(who, app, r, debug)))) {
                Outcome.Retry -> { ok = false; break }
                else -> reports.remove(listOf(r))
            }
        }
        if (!enabled) { events.clear(); return ok }
        val install = installId()
        events.keep { fresh(it, today()) }
        while (true) {
            // switched off (and maybe on) meanwhile: what waits now belongs to the next id, not to this one
            if (!enabled || prefs.get("install") != install) break
            val batch = batch(events.all())
            if (batch.isEmpty()) break
            val phone = device.takeIf { prefs.get("device") != "off" }
            when (outcome(post("/api/telemetry/events", eventsBody(install, app, batch, debug, phone, ctx)))) {
                Outcome.Retry -> { ok = false; break }
                else -> events.remove(batch)
            }
        }
        return ok
    }

    companion object {
        /** Away this long, the next start of the app is a new sitting. */
        const val Sitting = 30L * 60 * 1000
        private val VersionShape = Regex("[A-Za-z0-9._+-]{1,32}")

        /** A step's type as the admin page shows it. */
        fun kindOf(s: Step): String = s::class.simpleName ?: "Step"

        /** POST to the statistics server with the app's key. -> status, null when it could not be reached. */
        fun post(server: String, key: String, path: String, body: String): Int? = runCatching {
            val c = URL(server + path).openConnection() as HttpURLConnection
            try {
                c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 15_000; c.readTimeout = 30_000
                c.setRequestProperty("Authorization", "Bearer $key")
                c.setRequestProperty("Content-Type", "application/json")
                c.outputStream.use { it.write(body.toByteArray()) }
                c.responseCode
            } finally { c.disconnect() }
        }.getOrNull()

        /** Where a crash is written while the app dies, read back at the next start. */
        fun crashFile(dir: File) = File(dir, "crash.txt")

        /** Writes [e] for the next start: class, message, stack (the app's frames are what tells crashes apart). */
        fun writeCrash(dir: File, e: Throwable) = runCatching {
            dir.mkdirs()
            crashFile(dir).writeText(listOf(e::class.java.name, (e.message ?: "").replace('\n', ' ').take(MaxItemText), stackOf(e).take(MaxStack)).joinToString("\n"))
        }

        /** The first frames of the crash and of each cause: what tells crashes apart and where. The causes' messages
         *  could quote anything, so they are left out; a long stack is cut per exception, not at its end. */
        fun stackOf(e: Throwable): String {
            val out = ArrayList<String>()
            var t: Throwable? = e
            var depth = 0
            while (t != null && depth < 5) {
                if (depth > 0) out += "Caused by: ${t::class.java.name}"
                t.stackTrace.take(if (depth == 0) 15 else 8).forEach { out += "\tat $it" }
                t = t.cause.takeIf { it !== t }
                depth++
            }
            return out.joinToString("\n")
        }

        /** -> (class, message, stack) of a crash written before, and forgets it. */
        fun readCrash(dir: File): Triple<String, String?, String>? {
            val f = crashFile(dir)
            if (!f.exists()) return null
            val lines = runCatching { f.readText() }.getOrNull()?.split('\n', limit = 3)
            f.delete()
            if (lines == null || lines.size < 3) return null
            return Triple(lines[0], lines[1].ifBlank { null }, lines[2])
        }
    }
}

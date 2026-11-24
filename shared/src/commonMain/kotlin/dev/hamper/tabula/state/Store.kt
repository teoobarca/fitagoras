package dev.hamper.tabula.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import dev.hamper.tabula.ui.ThemeMode
import kotlinx.coroutines.CoroutineScope
import dev.hamper.tabula.compat.IoDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.LocalDateTime
import dev.hamper.tabula.data.Course
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.DefaultParallel
import dev.hamper.tabula.data.courseById
import dev.hamper.tabula.data.examStart
import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.time.*

/** One answer. [aided]: a hint or the solution was seen first, so it does not count as mastered. */
data class Attempt(val key: String, val at: Long, val ok: Boolean, val aided: Boolean)

/** Something that went wrong and comes back in review; [route] opens the drill for it. */
data class Mistake(val key: String, val title: String, val detail: String, val route: String, val at: Long)

/** A mock test on paper, graded by the student against the checklist. [got] = points per rubric line. */
/** [nehodnotene]: rubric lines per problem of the parts nothing was said about (no points, but not lost either). */
data class MockResult(val paper: String, val at: Long, val got: List<List<Int>>, val total: Int, val max: Int, val nehodnotene: List<Set<Int>> = emptyList())

data class Report(val item: String, val text: String, val at: Long)

/** The 1.2.x test date as an LA1 sitting: the KOS term that day (the earliest), else that day at 18:00. */
fun migrateTestDate(d: LocalDate): LocalDateTime =
    dev.hamper.tabula.data.La1.exams.first { it.id == "la1-test2" }.terms.filter { it.start.toLocalDate() == d }.minByOrNull { it.start }?.start ?: d.atTime(18, 0)

/**
 * Everything the app remembers, in one JSON document in DataStore (survives app updates, never leaves the phone).
 * Reads once at start; every change writes the whole document in the background. [file]: where (StateFile).
 */
class Store(private val file: StateFile) {
    var theme by mutableStateOf(ThemeMode.System)
    /** The course on screen; null until the student picks one: then the one whose test comes first. */
    var courseId by mutableStateOf<String?>(null)
    /** Lecture parallel per course (default 2) and the picked sitting per exam id, both chosen by the student. */
    val parallels = mutableStateMapOf<String, String>()
    val terms = mutableStateMapOf<String, LocalDateTime>()
    var haptics by mutableStateOf(true)
    /**
     * Short sounds (ui/Sound.kt): on by default since Teo chose them (18. 11.). Kept under a new key: the old "sounds"
     * was saved as false by every install while they were off by default, not chosen by anyone.
     */
    var sounds by mutableStateOf(true)
    /** Lecture clip playback speed, kept across clips and restarts. */
    var clipSpeed by mutableFloatStateOf(1f)
    var reminder by mutableStateOf(false)
    /** The update check (every start of the app, every return to it, at most once a minute); the last answer and a "Neskôr" per version. */
    var autoUpdate by mutableStateOf(true)
    /** The one-line note on what the statistics collect was seen (Today, until "Rozumiem" or Nastavenia). */
    var statsNoticed by mutableStateOf(false)
    /** A tappable symbol in a formula was opened once: the line that says they can be tapped is no longer shown. */
    var symbolTapped by mutableStateOf(false)
    /** That line was really on screen, whole, for a moment: from the next statement on it is not shown again. */
    var symbolHintSeen by mutableStateOf(false)
    /** The day the days in a row were last cheered (once a day, at the end of a lesson: [cheerStreak]). */
    var streakCheered = ""
    var updateCheckedAt = 0L
    var release by mutableStateOf<dev.hamper.tabula.system.Release?>(null)
    var snoozedCode by mutableIntStateOf(0)
    var snoozedUntil by mutableLongStateOf(0L)
    /** Until when an install waiting for the "install apps" permission resumes on its own (the grant may restart the app). */
    var resumeInstallUntil = 0L

    /** Furthest step reached per lesson (resume), and lessons finished. */
    val lessonAt = mutableStateMapOf<String, Int>()
    val lessonsDone = mutableStateListOf<String>()
    /** Lessons in progress, each as one history with its answers (LessonRun); kept as read until the lesson opens. */
    private val runs = mutableMapOf<String, LessonRun>()
    private val runsSaved = mutableMapOf<String, JSONObject>()
    /** Problem types the student skipped with "Viem to": shown once in review to check, not counted as a mistake. */
    val vouched = mutableStateListOf<String>()
    /** Proofs and definitions whose paper step was put off with "Teraz nie": asked again in review, never a mistake. */
    val unwritten = mutableStateListOf<String>()
    val attempts = mutableStateListOf<Attempt>()
    val mistakes = mutableStateListOf<Mistake>()
    val mocks = mutableStateListOf<MockResult>()
    val reports = mutableStateListOf<Report>()
    /** Days with any learning (ISO dates, the last 400), for "6 dní po sebe" on Dnes. */
    val activeDays = mutableStateListOf<String>()
    /** When each lesson was last on screen (ms): Dnes puts the one worked on last first (data/Today.kt). */
    val lessonTouched = mutableStateMapOf<String, Long>()
    /** Lessons walked to their end with steps jumped over: offered to finish once there, then a row on Dnes. */
    val lessonGaps = mutableStateListOf<String>()
    /** The day each lesson was finished (ISO date), for "Dnes hotovo". Lessons finished before 1.3.16 have none. */
    val doneOn = mutableStateMapOf<String, String>()
    /**
     * Today's review round (data/Review.kt): the day and what it finished that day. Only the dose for that day reads it;
     * a new day starts empty. Not there in installs before 1.6.3: then nothing was finished yet today.
     */
    private var reviewDay = ""
    private val reviewed = mutableStateListOf<String>()

    /** Bumped on every change, so screens that compute from history recompose. */
    var version by mutableIntStateOf(0)
        private set

    private val scope = CoroutineScope(SupervisorJob() + IoDispatcher)
    private val lock = Mutex()

    init { load(); dev.hamper.tabula.data.Sittings.picked = { terms[it] } }

    private fun load() {
        val raw = runCatching { file.read() }.getOrNull() ?: return
        runCatching {
            val o = JSONObject(raw)
            theme = ThemeMode.entries.firstOrNull { it.name == o.optString("theme") } ?: ThemeMode.System
            o.optString("course").takeIf { it.isNotEmpty() }?.let { courseId = it }
            o.optJSONObject("parallels")?.let { m -> m.keys().forEach { parallels[it] = m.getString(it) } }
            o.optJSONObject("terms")?.let { m -> m.keys().forEach { terms[it] = LocalDateTime.parse(m.getString(it)) } }
            // v1 (1.2.x) kept one date, the LA1 test: it becomes the LA1 sitting on that day
            if (o.optInt("v", 1) < 2) o.optString("testDate").takeIf { it.isNotEmpty() }?.let { terms["la1-test2"] = migrateTestDate(LocalDate.parse(it)) }
            haptics = o.optBoolean("haptics", true)
            sounds = o.optBoolean("sounds2", true)
            clipSpeed = o.optDouble("clipSpeed", 1.0).toFloat().takeIf { it in ClipSpeeds } ?: 1f
            reminder = o.optBoolean("reminder", false)
            autoUpdate = o.optBoolean("autoUpdate", true)
            statsNoticed = o.optBoolean("statsNoticed", false)
            symbolTapped = o.optBoolean("symbolTapped", false)
            symbolHintSeen = o.optBoolean("symbolHintSeen", false)
            streakCheered = o.optString("streakCheered", "")
            updateCheckedAt = o.optLong("updateCheckedAt", 0L)
            release = dev.hamper.tabula.system.UpdateCore.parse(o.optString("release").takeIf { it.isNotEmpty() })
            snoozedCode = o.optInt("snoozedCode", 0)
            snoozedUntil = o.optLong("snoozedUntil", 0L)
            resumeInstallUntil = o.optLong("resumeInstallUntil", 0L)
            o.optJSONObject("lessonAt")?.let { m -> m.keys().forEach { lessonAt[it] = m.getInt(it) } }
            o.optJSONArray("lessonsDone")?.let { a -> for (i in 0 until a.length()) lessonsDone += a.getString(i) }
            o.optJSONArray("attempts")?.let { a -> for (i in 0 until a.length()) a.getJSONObject(i).run { attempts += Attempt(getString("k"), getLong("t"), getBoolean("ok"), getBoolean("aided")) } }
            o.optJSONArray("mistakes")?.let { a -> for (i in 0 until a.length()) a.getJSONObject(i).run { mistakes += Mistake(getString("k"), getString("title"), getString("detail"), getString("route"), getLong("t")) } }
            o.optJSONArray("mocks")?.let { a ->
                for (i in 0 until a.length()) a.getJSONObject(i).run {
                    val g = getJSONArray("got")
                    val nh = optJSONArray("nehodnotene")
                    mocks += MockResult(getString("paper"), getLong("t"), (0 until g.length()).map { k -> g.getJSONArray(k).let { r -> (0 until r.length()).map { r.getInt(it) } } }, getInt("total"), getInt("max"),
                        nh?.let { a -> (0 until a.length()).map { k -> a.getJSONArray(k).let { r -> (0 until r.length()).map { r.getInt(it) }.toSet() } } }.orEmpty())
                }
            }
            o.optJSONArray("reports")?.let { a -> for (i in 0 until a.length()) a.getJSONObject(i).run { reports += Report(getString("item"), getString("text"), getLong("t")) } }
            // last and each on its own: a damaged entry here never costs the record above
            runCatching { o.optJSONObject("runs")?.let { m -> m.keys().forEach { k -> m.optJSONObject(k)?.let { runsSaved[k] = it } } } }
            runCatching { o.optJSONArray("vouched")?.let { a -> for (i in 0 until a.length()) a.optString(i).takeIf { it.isNotEmpty() }?.let { vouched += it } } }
            runCatching { o.optJSONArray("days")?.let { a -> for (i in 0 until a.length()) a.optString(i).takeIf { it.isNotEmpty() }?.let { activeDays += it } } }
            runCatching { o.optJSONObject("doneOn")?.let { m -> m.keys().forEach { k -> doneOn[k] = m.getString(k) } } }
            runCatching { o.optJSONObject("touched")?.let { m -> m.keys().forEach { k -> lessonTouched[k] = m.getLong(k) } } }
            runCatching { o.optJSONArray("gaps")?.let { a -> for (i in 0 until a.length()) a.optString(i).takeIf { it.isNotEmpty() }?.let { lessonGaps += it } } }
            // an install from before the day list: its answers say which days it learned on
            if (!o.has("days")) runCatching { (attempts.map { it.at } + mocks.map { it.at }).map { dayOf(it) }.distinct().sorted().forEach { activeDays += it } }
            runCatching { o.optJSONObject("reviewed")?.let { r -> reviewDay = r.optString("day", ""); r.optJSONArray("keys")?.let { a -> for (i in 0 until a.length()) a.optString(i).takeIf { it.isNotEmpty() }?.let { reviewed += it } } } }
            runCatching { o.optJSONArray("unwritten")?.let { a -> for (i in 0 until a.length()) a.optString(i).takeIf { it.isNotEmpty() }?.let { unwritten += it } } }
        }
    }

    private fun json(): String = JSONObject().apply {
        put("v", 2)
        put("theme", theme.name); put("haptics", haptics); put("sounds2", sounds); put("clipSpeed", clipSpeed.toDouble()); put("reminder", reminder)
        put("autoUpdate", autoUpdate); put("statsNoticed", statsNoticed); put("symbolTapped", symbolTapped); put("symbolHintSeen", symbolHintSeen); put("streakCheered", streakCheered); put("updateCheckedAt", updateCheckedAt); put("snoozedCode", snoozedCode); put("snoozedUntil", snoozedUntil); put("resumeInstallUntil", resumeInstallUntil)
        release?.let { put("release", it.json()) }
        courseId?.let { put("course", it) }
        put("parallels", JSONObject().apply { parallels.forEach { (k, v) -> put(k, v) } })
        put("terms", JSONObject().apply { terms.forEach { (k, v) -> put(k, v.toString()) } })
        put("lessonAt", JSONObject().apply { lessonAt.forEach { (k, v) -> put(k, v) } })
        put("lessonsDone", JSONArray(lessonsDone.toList()))
        put("runs", JSONObject().apply { runsSaved.forEach { (k, v) -> if (k !in runs) put(k, v) }; runs.forEach { (k, v) -> put(k, v.json()) } })
        put("vouched", JSONArray(vouched.toList()))
        put("unwritten", JSONArray(unwritten.toList()))
        put("days", JSONArray(activeDays.toList()))
        put("doneOn", JSONObject().apply { doneOn.forEach { (k, v) -> put(k, v) } })
        put("touched", JSONObject().apply { lessonTouched.forEach { (k, v) -> put(k, v) } })
        put("gaps", JSONArray(lessonGaps.toList()))
        if (reviewDay.isNotEmpty()) put("reviewed", JSONObject().put("day", reviewDay).put("keys", JSONArray(reviewed.toList())))
        put("attempts", JSONArray().apply { attempts.forEach { put(JSONObject().put("k", it.key).put("t", it.at).put("ok", it.ok).put("aided", it.aided)) } })
        put("mistakes", JSONArray().apply { mistakes.forEach { put(JSONObject().put("k", it.key).put("title", it.title).put("detail", it.detail).put("route", it.route).put("t", it.at)) } })
        put("mocks", JSONArray().apply { mocks.forEach { r -> put(JSONObject().put("paper", r.paper).put("t", r.at).put("total", r.total).put("max", r.max).put("got", JSONArray().apply { r.got.forEach { put(JSONArray(it)) } }).apply { if (r.nehodnotene.any { it.isNotEmpty() }) put("nehodnotene", JSONArray().apply { r.nehodnotene.forEach { put(JSONArray(it.sorted())) } }) }) } })
        put("reports", JSONArray().apply { reports.forEach { put(JSONObject().put("item", it.item).put("text", it.text).put("t", it.at)) } })
    }.toString()

    /** Documents handed out and the newest written: writes run in parallel coroutines, an older one must never land last. */
    private var made = 0L
    private var written = 0L

    fun save() {
        version++
        val doc = json()
        val n = ++made
        scope.launch { lock.withLock { if (n > written) { file.write(doc); written = n } } }
    }

    /* ---- course, parallel, test -------------------------------------------------------------------------------- */

    /** The course on screen: the picked one, else the one whose test comes first (then LA1). */
    val course: Course get() = courseId?.let { courseById(it) } ?: Courses.minByOrNull { c ->
        examStart(c.test, terms[c.test.id], LocalDateTime.now())?.takeIf { it.isAfter(LocalDateTime.now()) } ?: LocalDateTime.MAX
    } ?: courseById(null)

    fun parallel(c: Course = course): String = parallels[c.id] ?: DefaultParallel

    /** When this student writes the course's test: the picked sitting, else the next one in KOS. */
    fun testStart(c: Course = course, now: LocalDateTime = LocalDateTime.now()): LocalDateTime =
        examStart(c.test, terms[c.test.id], now) ?: now.plusDays(7)

    fun testDay(c: Course = course): LocalDate = testStart(c).toLocalDate()

    /**
     * The exam period of the course's test while the student has no day for it: no KOS terms yet and none picked (LA1's
     * skúška, 4. 1. to 14. 2.). Then [testStart] is only the window's first day, which no screen may show as the day.
     */
    fun testWindow(c: Course = course): Pair<LocalDate, LocalDate>? =
        c.test.takeIf { it.terms.isEmpty() && terms[it.id] == null }?.from?.let { it to (c.test.to ?: it) }

    /** Is the course's test behind this student: the sitting ended, or for a [testWindow] the whole window. */
    fun testOver(c: Course = course, now: LocalDateTime = LocalDateTime.now()): Boolean =
        if (testWindow(c) != null) dev.hamper.tabula.data.examOver(c.test) < now
        // a day picked without a time ("Iný dátum", stored at midnight) is over only when that day is
        else testStart(c, now).let { t -> if (dev.hamper.tabula.data.hasTime(t)) t.plusMinutes((c.test.minutes ?: 70).toLong()) else t.toLocalDate().atTime(23, 59) } < now

    fun pickCourse(id: String) { courseId = id; save(); Telemetry.opened(course.id, parallels[course.id]) }
    fun pickParallel(c: Course, p: String) { parallels[c.id] = p; save(); Telemetry.opened(c.id, p) }
    /** The sitting of the course's current test (terms are kept per exam, so the next exam starts unpicked). */
    fun pickTerm(c: Course, t: LocalDateTime) { terms[c.test.id] = t; save() }
    /** Forget the sitting picked for [examId] (a mistaken "Áno, 18. 11." in the tab Test): the app asks again. */
    fun unpickTerm(examId: String) { terms.remove(examId); save() }

    /* ---- learning record ---------------------------------------------------------------------------------- */

    fun record(key: String, ok: Boolean, aided: Boolean, mistake: Mistake? = null) {
        Telemetry.answer(key, ok, aided, history(key).size + 1, course.id, mistake?.title)
        attempts += Attempt(key, currentTimeMillis(), ok, aided)
        // written on paper at last (whatever came out): no longer put off
        unwritten.remove(key)
        // a skipped type checked once: right without help, it is known; wrong, it is learnt like any other
        if (key in vouched) { if (ok && !aided) mistakes.removeAll { it.key == key } else vouched.remove(key) }
        if (attempts.size > 3000) attempts.removeRange(0, attempts.size - 3000)
        markActive()
        if (!ok && mistake != null) { mistakes.removeAll { it.key == mistake.key }; mistakes += mistake }
        // two clean answers in a row clear a mistake
        if (ok && !aided && mistakes.any { it.key == key }) {
            val last = attempts.filter { it.key == key }.takeLast(2)
            if (last.size == 2 && last.all { it.ok && !it.aided }) mistakes.removeAll { it.key == key }
        }
        save()
    }

    fun history(key: String) = attempts.filter { it.key == key }

    /**
     * Mastered: the last answer and at least two in all were right without help. Aided ones never count. A type
     * skipped with "Viem to" needs one: the check in review.
     */
    fun mastered(key: String): Boolean {
        val h = history(key)
        return h.isNotEmpty() && h.last().ok && !h.last().aided && h.count { it.ok && !it.aided } >= (if (key in vouched) 1 else 2)
    }

    /**
     * "Viem to" is offered only for a type never tried: then one clean answer in review is a check made after the
     * skip. A type with any history (a mistake, one clean answer) is learnt by the usual rule.
     */
    fun canVouch(key: String) = !tried(key) && mistakes.none { it.key == key }

    /** "Viem to": the problem is skipped, no mistake; review shows it once to check (data/Plan.kt, dueKeys). */
    fun vouch(key: String) { if (key !in vouched && canVouch(key)) { vouched += key; save(); Telemetry.skipped("known") } }

    /**
     * "Teraz nie" on a paper step (a proof or a definition from memory): the lesson goes on with no mistake, and review
     * asks for it once on paper later (data/Plan.kt, dueKeys); writing it, whatever the result, takes it off.
     */
    fun putOff(key: String) { if (key !in unwritten) { unwritten += key; save() }; Telemetry.skipped("later") }

    fun tried(key: String) = attempts.any { it.key == key }

    /** What today's review round finished on [day] (data/Review.kt); empty for any other day. */
    fun reviewedOn(day: LocalDate): List<String> = if (reviewDay == day.toString()) reviewed.toList() else emptyList()

    /** A thing of today's review dose finished (whatever the answer): it counts towards today's dose. */
    fun reviewedNow(key: String) {
        val t = LocalDate.now().toString()
        if (reviewDay != t) { reviewDay = t; reviewed.clear() }
        if (key !in reviewed) reviewed += key
        save()
    }

    /** Nothing finished in today's review yet (a record built from scratch, as the screenshots seed one). */
    fun forgetReviewed() { reviewDay = ""; reviewed.clear(); save() }

    /** A lesson on screen: begun now if it never was. */
    fun lessonOpened(id: String) { if (id !in lessonAt && id !in lessonsDone) Telemetry.lessonStarted(id); lessonTouched[id] = currentTimeMillis(); save() }

    fun lessonStep(id: String, step: Int) {
        val newDay = markActive()
        lessonTouched[id] = currentTimeMillis()
        if ((lessonAt[id] ?: -1) < step) { lessonAt[id] = step; save() } else save()
    }
    fun finishLesson(id: String) {
        if (id !in lessonsDone) { lessonsDone += id; Telemetry.lessonFinished(id) }
        doneOn[id] = LocalDate.now().toString(); markActive()
        lessonGaps.remove(id)
        runs.remove(id); runsSaved.remove(id); save()
    }

    /** "Viem to" on an unfinished lesson: done without the steps left, no celebration, no day in a row. */
    fun knowLesson(id: String) {
        if (id !in lessonsDone) { lessonsDone += id; Telemetry.lessonFinished(id) }
        lessonGaps.remove(id)
        runs.remove(id); runsSaved.remove(id); save()
    }

    /** The end of a lesson reached with steps jumped over: it is not done, Dnes lists it (data/Today.kt). */
    fun lessonEndedWithGaps(id: String) { if (id !in lessonGaps && id !in lessonsDone) { lessonGaps += id; save() } }

    /** Steps of a lesson still to do: all of a lesson never begun, none of a finished one. */
    fun stepsLeft(l: dev.hamper.tabula.data.LessonDef): Int = when {
        l.id in lessonsDone -> 0
        !inProgress(l) -> l.steps.size
        else -> run(l).left().size
    }

    /**
     * Whether the end of this lesson cheers the days in a row: today learnt, yesterday too (the row grew today), and
     * not cheered yet today ([streakCheeredNow] records it once the sound has played).
     */
    fun cheerStreak(): Boolean {
        val t = LocalDate.now()
        return t.toString() in activeDays && t.minusDays(1).toString() in activeDays && streakCheered != t.toString()
    }

    /** The days in a row were cheered today (called when the sound actually plays, not when the screen opens). */
    fun streakCheeredNow() { streakCheered = LocalDate.now().toString(); save() }

    /** Today counts as a day with learning; true when it was not one yet. */
    fun markActive(): Boolean {
        val t = LocalDate.now().toString()
        if (t in activeDays) return false
        activeDays += t
        if (activeDays.size > 400) activeDays.removeRange(0, activeDays.size - 400)
        return true
    }

    /**
     * The lesson's run: the one in progress (also after the app was killed), else a new one. A finished lesson starts
     * over; an older install that remembered only the furthest step starts there.
     */
    fun run(l: dev.hamper.tabula.data.LessonDef): LessonRun = runs.getOrPut(l.id) {
        val ids = l.steps.map { it.id }
        val saved = runsSaved.remove(l.id)
        when {
            saved != null -> runCatching { LessonRun.parse(ids, saved, ::save) }.getOrNull()
            else -> null
        } ?: LessonRun.start(ids, if (l.id in lessonsDone) 0 else (lessonAt[l.id] ?: 0), ::save)
    }

    /** The step a lesson opens on: where its run stands. */
    fun runAt(l: dev.hamper.tabula.data.LessonDef): Int =
        runs[l.id]?.at ?: runsSaved[l.id]?.let { o -> l.steps.indexOfFirst { it.id == o.optString("atId") }.takeIf { it >= 0 } ?: o.optInt("at", 0).coerceIn(0, l.steps.lastIndex) } ?: if (l.id in lessonsDone) 0 else (lessonAt[l.id] ?: 0).coerceAtMost(l.steps.lastIndex)

    /**
     * How far a lesson is, in steps, for what shows its progress: where it stands, but never past what is done (a jump
     * ahead is not progress); one ended with steps skipped stands on the first of them and is as far as it is done.
     */
    fun farAt(l: dev.hamper.tabula.data.LessonDef): Int {
        if (!inProgress(l)) return runAt(l)
        val done = (l.steps.size - stepsLeft(l)).coerceIn(0, l.steps.lastIndex)
        return if (l.id in lessonGaps) done else minOf(runAt(l), done)
    }

    /** A lesson from step [at] with nothing remembered ("Od začiatku", "Zopakovať", a link to a step). */
    fun restartLesson(l: dev.hamper.tabula.data.LessonDef, at: Int = 0) {
        runsSaved.remove(l.id)
        runs[l.id] = LessonRun.start(l.steps.map { it.id }, at, ::save, walked = false)
        lessonGaps.remove(l.id)
        restarts++
        save()
    }

    /** Bumped when a run is replaced: an open lesson then reads the new one (and only then). */
    var restarts by mutableIntStateOf(0)
        private set

    /** A lesson begun and not finished: it goes on where it was, never from the start by itself. */
    fun inProgress(l: dev.hamper.tabula.data.LessonDef) = l.id !in lessonsDone && ((lessonAt[l.id] ?: 0) > 0 || l.id in runs || l.id in runsSaved)

    /** Kept here and sent to the author (Telemetry), with the lesson, the item and the lecture moment. */
    fun report(item: String, text: String) { reports += Report(item, text.trim(), currentTimeMillis()); save(); Telemetry.report(item, text, course.id) }
}

/** The speeds a lecture clip offers. */
val ClipSpeeds = listOf(.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

/** The local day of a moment, as an ISO date. */
fun dayOf(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toString()

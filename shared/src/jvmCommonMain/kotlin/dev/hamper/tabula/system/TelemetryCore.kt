package dev.hamper.tabula.system

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import java.io.File
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.*

/**
 * The plain part of the anonymous statistics (no Android here, so it is unit tested): what one event looks like, the
 * queue on disk, the request bodies, and what to do with each answer of the server. The app's server takes them at
 * POST /api/telemetry/events and /api/telemetry/reports with the clips' guest key.
 */

/** Course id in the app -> the server's course code. Only these two are accepted there. */
fun serverCourse(courseId: String): String? = when (courseId) {
    "la1" -> "BI-LA1.21"
    "dml" -> "BI-DML.21"
    else -> null
}

/** One event. [day] is the phone's day; since 1.4 also [at], the phone's time in ms (the server keeps it to the minute).
 *  [parallel]: the lecture parallel picked in the app (on "open"); [watched]: how far into a clip the student got,
 *  0..1 (on "clip_played"); [text]/[source]: what an item, lesson, step or clip says. A lesson step: [step] (its id),
 *  [pos] (from 0), [of] (the lesson's length), [kind] (its type), [how] (done / known = "Viem to" / later = "Teraz
 *  nie"), [ms] (time on it). An answer: [attempt] (the n-th answer to this item ever), [given] (a wrong answer as
 *  entered; a right one is never sent), [seed] (the numbers of a generated exercise). "help": [kind] = hint / howto /
 *  why / solution. "update": [kind] = offered / accepted / installed, [version] (the new one; the old one on
 *  installed). "crash": [kind] = the exception's class, [message], [stack]. */
data class Event(
    val id: String,
    val day: String,
    val type: String,
    val course: String,
    val lesson: String? = null,
    val item: String? = null,
    val ok: Boolean? = null,
    val aided: Boolean? = null,
    val clip: String? = null,
    val test: String? = null,
    val score: Int? = null,
    val max: Int? = null,
    val text: String? = null,
    val source: String? = null,
    val parallel: String? = null,
    val watched: Double? = null,
    val at: Long? = null,
    val step: String? = null,
    val pos: Int? = null,
    val of: Int? = null,
    val kind: String? = null,
    val how: String? = null,
    val ms: Long? = null,
    val given: String? = null,
    val attempt: Int? = null,
    val seed: String? = null,
    val version: String? = null,
    val message: String? = null,
    val stack: String? = null,
) {
    fun json(): JSONObject = JSONObject().apply {
        put("id", id); put("day", day); put("type", type); put("course", course)
        parallel?.let { put("parallel", it) }; watched?.let { put("watched", it.coerceIn(0.0, 1.0)) }
        lesson?.let { put("lesson", it) }; item?.let { put("item", it) }
        ok?.let { put("ok", it) }; aided?.let { put("aided", it) }
        clip?.let { put("clip", it) }; test?.let { put("test", it) }
        score?.let { put("score", it) }; max?.let { put("max", it) }
        text?.let { put("text", it.take(MaxItemText)) }; source?.let { put("source", it.take(200)) }
        at?.let { put("at", it) }; step?.let { put("step", it) }; pos?.let { put("pos", it) }; of?.let { put("of", it) }
        kind?.let { put("kind", it) }; how?.let { put("how", it) }; ms?.let { put("ms", it.coerceIn(0L, MaxLessonMs)) }
        given?.let { put("given", it.take(MaxGiven)) }; attempt?.let { put("attempt", it) }; seed?.let { put("seed", it) }
        version?.let { put("version", it) }; message?.let { put("message", it.take(MaxItemText)) }; stack?.let { put("stack", it.take(MaxStack)) }
    }

    companion object {
        fun from(o: JSONObject) = Event(
            o.getString("id"), o.getString("day"), o.getString("type"), o.getString("course"),
            o.str("lesson"), o.str("item"), o.bool("ok"), o.bool("aided"), o.str("clip"), o.str("test"),
            if (o.has("score")) o.getInt("score") else null, if (o.has("max")) o.getInt("max") else null, o.str("text"), o.str("source"),
            o.str("parallel"), if (o.has("watched")) o.getDouble("watched") else null,
            o.long("at"), o.str("step"), o.int("pos"), o.int("of"), o.str("kind"), o.str("how"), o.long("ms"), o.str("given"),
            o.int("attempt"), o.str("seed"), o.str("version"), o.str("message"), o.str("stack"),
        )
    }
}

/** "Nahlásiť chybu". [t]: seconds into the recording [recording], when the report is about a lecture clip. */
data class ErrorReport(
    val id: String,
    val at: Long,
    val course: String,
    val text: String,
    val item: String? = null,
    val itemText: String? = null,
    val lesson: String? = null,
    val clip: String? = null,
    val recording: String? = null,
    val t: Double? = null,
    val source: String? = null,
    /** Written with statistics off: goes out under a one-off id, whatever the switch says when it is sent. */
    val anon: Boolean = false,
) {
    /** What the server gets; [anon] stays on the phone ([stored]). */
    fun json(): JSONObject = JSONObject().apply {
        put("id", id); put("at", at); put("course", course); put("text", text.take(2000))
        item?.let { put("item", it) }; itemText?.let { put("itemText", it.take(MaxItemText)) }; lesson?.let { put("lesson", it) }
        clip?.let { put("clip", it) }; recording?.let { put("recording", it) }; t?.let { put("t", it) }
        source?.let { put("source", it.take(200)) }
    }

    fun stored(): JSONObject = json().put("anon", anon)

    companion object {
        fun from(o: JSONObject) = ErrorReport(
            o.getString("id"), o.getLong("at"), o.getString("course"), o.getString("text"), o.str("item"), o.str("itemText"),
            o.str("lesson"), o.str("clip"), o.str("recording"), if (o.has("t")) o.getDouble("t") else null, o.str("source"),
            o.optBoolean("anon", false),
        )
    }
}

private fun JSONObject.str(k: String): String? = if (has(k) && !isNull(k)) getString(k) else null
private fun JSONObject.bool(k: String): Boolean? = if (has(k) && !isNull(k)) getBoolean(k) else null
private fun JSONObject.int(k: String): Int? = if (has(k) && !isNull(k)) getInt(k) else null
private fun JSONObject.long(k: String): Long? = if (has(k) && !isNull(k)) getLong(k) else null

const val MaxItemText = 600
const val MaxGiven = 120
const val MaxStack = 4000
/** A step or lesson time longer than this is not time spent on it (the phone lay there): sent without a time. */
const val MaxMs = 30L * 60 * 1000
/** A whole lesson may take longer (the sum of its steps); the server takes up to six hours. */
const val MaxLessonMs = 6L * 3600 * 1000
const val BatchSize = 200 // the server's limit per request
const val BatchBytes = 56 * 1024 // under the server's 64 KB per request, with room for the envelope
const val MaxQueued = 2000 // events waiting offline; the oldest go first beyond this
const val MaxReports = 100
/** Days an event may wait offline; the server drops older ones anyway. */
const val MaxAgeDays = 30L

/** The server takes ids of this shape only; anything else would cost the whole batch (400). */
private val IdShape = Regex("[A-Za-z0-9._:/+@-]{1,100}")
fun safeId(id: String?): String? = id?.takeIf { IdShape.matches(it) }
private val ParallelShape = Regex("[A-Za-z0-9._-]{1,16}")
private val KindShape = Regex("[A-Za-z0-9._$-]{1,80}")
/** A step type, a help button, an exception class: the server refuses the batch for anything else. */
fun safeKind(k: String?): String? = k?.takeIf { KindShape.matches(it) }
fun safeParallel(p: String?): String? = p?.takeIf { ParallelShape.matches(it) }

/** The head of the queue that fits one request: at most [BatchSize] events and [BatchBytes] of JSON. */
fun batch(events: List<Event>): List<Event> {
    var bytes = 0
    val out = ArrayList<Event>()
    for (e in events) {
        val size = e.json().toString().toByteArray().size + 1
        if (out.size == BatchSize || (out.isNotEmpty() && bytes + size > BatchBytes)) break
        out += e; bytes += size
    }
    return out
}

/** What a sender does with the server's answer. */
enum class Outcome { Sent, Drop, Retry }

/** 2xx sent; 400/413 the batch itself is wrong and never will be right: dropped; 401/403 a key this server no longer
 *  takes: dropped too (the queue must not grow forever on an old build); everything else (5xx, 429, offline) waits. */
fun outcome(status: Int?): Outcome = when {
    status == null -> Outcome.Retry
    status in 200..299 -> Outcome.Sent
    status == 400 || status == 413 || status == 401 || status == 403 -> Outcome.Drop
    else -> Outcome.Retry
}

/** [test]: a debug build (emulator checks): the server keeps it apart and the admin page never shows it. [device]: the
 *  phone's hash ([deviceHash]), so a reinstall continues the same history; [ctx]: the phone's context ([Context]). */
fun eventsBody(install: String, app: String, events: List<Event>, test: Boolean = false, device: String? = null, ctx: Context? = null): String =
    JSONObject().put("install", install).put("app", app).put("events", JSONArray().apply { events.forEach { put(it.json()) } })
        .apply { if (test) put("test", true); device?.let { put("device", it) }; ctx?.let { put("ctx", it.json()) } }.toString()

/** What the numbers can be sliced by on the admin page: the phone, its settings, the network now. Nothing that names a
 *  person or a specific phone (no serial, no account, no IP: the server does not log it). */
data class Context(
    val maker: String? = null, val model: String? = null, val os: String? = null, val sdk: Int? = null,
    /** width x height in dp */
    val screen: String? = null, val font: Float? = null, val dark: Boolean? = null, val lang: String? = null,
    val tz: String? = null,
    /** wifi / mobile / ethernet / none / other */
    val net: String? = null, val emulator: Boolean? = null,
) {
    fun json(): JSONObject = JSONObject().apply {
        maker?.let { put("maker", it.take(40)) }; model?.let { put("model", it.take(60)) }; os?.let { put("os", it.take(20)) }
        sdk?.let { put("sdk", it) }; screen?.let { put("screen", it) }; font?.let { put("font", (it * 100).toInt() / 100.0) }
        dark?.let { put("dark", it) }; lang?.let { put("lang", it) }; tz?.let { put("tz", it) }; net?.let { put("net", it) }
        emulator?.let { put("emulator", it) }
    }
}

/** The phone's id as sent: SHA-256 of the app's ANDROID_ID (already one per app signing key and phone) with this
 *  app's own salt, so it matches nothing another app or service sees. The server keys it once more. */
fun deviceHash(androidId: String): String =
    java.security.MessageDigest.getInstance("SHA-256").digest("fitagoras-device|$androidId".toByteArray())
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

fun reportBody(install: String, app: String, r: ErrorReport, test: Boolean = false): String =
    r.json().put("install", install).put("app", app).apply { if (test) put("test", true) }.toString()

/**
 * Lines of JSON in one file, rewritten whole on every change (a few hundred lines at most). Survives the app being
 * killed; [cap] keeps the newest.
 */
class JsonQueue<T>(private val file: File, private val cap: Int, private val write: (T) -> JSONObject, private val read: (JSONObject) -> T, private val id: (T) -> String) {
    @Synchronized
    fun all(): List<T> = if (!file.exists()) emptyList() else file.readLines().mapNotNull { l -> runCatching { read(JSONObject(l)) }.getOrNull() }

    @Synchronized
    fun add(item: T) = save(all() + item)

    /** Removes the items just sent, by id: whatever was added or trimmed meanwhile stays as it is. */
    @Synchronized
    fun remove(sent: Collection<T>) { val ids = sent.mapTo(HashSet(), id); save(all().filter { id(it) !in ids }) }

    @Synchronized
    fun keep(predicate: (T) -> Boolean) = save(all().filter(predicate))

    @Synchronized
    fun clear() { file.delete() }

    private fun save(items: List<T>) {
        file.parentFile?.mkdirs()
        val tmp = File(file.path + ".tmp")
        tmp.writeText(items.takeLast(cap).joinToString("") { write(it).toString() + "\n" })
        tmp.renameTo(file)
    }
}

/** Events a phone that was offline too long still holds: older than [MaxAgeDays] they are not worth sending. */
fun fresh(e: Event, today: LocalDate): Boolean = runCatching { !LocalDate.parse(e.day).isBefore(today.minusDays(MaxAgeDays)) }.getOrDefault(false)

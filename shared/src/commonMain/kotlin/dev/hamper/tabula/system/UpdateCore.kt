package dev.hamper.tabula.system

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.URI
import dev.hamper.tabula.compat.format

/** What changed in one version: an entry of the server's `history`. */
data class VersionNotes(val versionCode: Int, val versionName: String, val date: String, val notes: String)

/**
 * The newest build as the app's server describes it at GET /api/fitest/latest. [history]: asked
 * with ?since=<this app's versionCode>, the notes of every newer version, newest first (empty from an older server).
 */
data class Release(
    val versionCode: Int, val versionName: String, val date: String, val sha256: String, val size: Long, val url: String, val notes: String,
    val history: List<VersionNotes> = emptyList(),
) {
    fun json(): String = JSONObject().put("versionCode", versionCode).put("versionName", versionName).put("date", date)
        .put("sha256", sha256).put("size", size).put("url", url).put("notes", notes)
        .put("history", JSONArray().also { a -> history.forEach { h -> a.put(JSONObject().put("versionCode", h.versionCode).put("versionName", h.versionName).put("date", h.date).put("notes", h.notes)) } })
        .toString()
}

/** The rules of the update check, apart from Android so they are unit tested. */
object UpdateCore {
    /**
     * Back from the background the automatic check asks when the last answer is older than this; a cold start always
     * asks (Teo, 16. 11.: a release came out within six hours of the last answer, the app did not ask and offered nothing
     * until Settings). A minute, not half an hour (Teo, 19. 11.: 1.6.5 was offered only a while later, the app had asked
     * shortly before the release and stayed in memory): every return to the app asks, only a quick back and forth (the
     * camera, the installer, the sign-in in the browser) rides on the answer just given. The answer is a small JSON.
     */
    const val CheckEvery = 60_000L
    /** "Neskôr" hides an offer of that version this long. */
    const val SnoozeFor = 3 * 24 * 3600_000L

    /** A release from the server's JSON; null when anything the download needs is missing or odd. */
    fun parse(raw: String?): Release? = runCatching {
        val o = JSONObject(raw!!)
        Release(
            o.getInt("versionCode"), o.getString("versionName"), o.optString("date").takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: "", o.getString("sha256").lowercase(),
            o.getLong("size"), o.getString("url"), o.optString("notes").trim(), history(o.optJSONArray("history")),
        ).takeIf { it.versionCode > 0 && it.size > 0 && it.sha256.matches(Regex("[0-9a-f]{64}")) && it.url.isNotBlank() }
    }.getOrNull()

    private fun isoDay(s: String) = s.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: ""

    /** The `history` entries that make sense; an odd entry is left out, never the whole answer. */
    private fun history(a: JSONArray?): List<VersionNotes> = a?.let { arr ->
        (0 until arr.length()).mapNotNull { i ->
            runCatching { arr.getJSONObject(i).let { h -> VersionNotes(h.getInt("versionCode"), h.getString("versionName"), isoDay(h.optString("date")), h.optString("notes").trim()) } }
                .getOrNull()?.takeIf { it.versionCode > 0 }
        }
    }.orEmpty()

    /** The check's address: the server lists the notes of every version after [current] (`?since=`). */
    fun checkUrl(endpoint: String, current: Int): String = endpoint + (if ('?' in endpoint) "&" else "?") + "since=$current"

    /**
     * What the offer lists: every version newer than [current], newest first, each once, with its notes (a version
     * without notes is left out). Without a usable history (an older server, a stored offer) the newest version's notes.
     */
    fun changes(r: Release, current: Int): List<VersionNotes> {
        // the newest version's own entry is the answer itself: its notes there win (a history entry may lack them)
        val newer = r.history.filter { it.versionCode in (current + 1) until r.versionCode }.distinctBy { it.versionCode }.sortedByDescending { it.versionCode }
        val newest = r.history.firstOrNull { it.versionCode == r.versionCode }
        val all = listOf(VersionNotes(r.versionCode, r.versionName, r.date, r.notes.ifBlank { newest?.notes.orEmpty() })) + newer
        return all.filter { it.notes.isNotBlank() }
    }

    /**
     * After a failed check (no answer, a timeout, a server error) the next attempt waits this long: 1, 5, then 15 minutes
     * for every further failure. Only an answered check counts for [CheckEvery]; a success clears the failures.
     */
    val RetryAfter = listOf(60_000L, 5 * 60_000L, 15 * 60_000L)

    /** The pause after [failures] failed checks in a row (none: no pause). */
    fun retryAfter(failures: Int): Long = if (failures <= 0) 0L else RetryAfter[minOf(failures, RetryAfter.size) - 1]

    /**
     * The automatic check: switched on; the first one of a run of the app ([coldStart]), or the last answer ([lastOk])
     * older than [CheckEvery]; and after failed attempts the pause for them over (a clock set back counts as over). The
     * app asks on every start and resume and when the network becomes validated; this decides whether that moment asks.
     */
    fun due(auto: Boolean, lastOk: Long, now: Long, failures: Int = 0, lastFail: Long = 0L, coldStart: Boolean = false): Boolean =
        auto && (coldStart || now - lastOk >= CheckEvery || now < lastOk) && (failures <= 0 || now - lastFail >= retryAfter(failures) || now < lastFail)

    /** What to offer: a newer build than this one, unless "Neskôr" hid exactly that version less than [SnoozeFor] ago. */
    fun offer(r: Release?, current: Int, snoozedCode: Int, snoozedUntil: Long, now: Long): Release? =
        r?.takeIf { it.versionCode > current && !(it.versionCode == snoozedCode && now < snoozedUntil) }

    /** The download address: the server answers with a path, relative to where the check went. */
    fun resolve(endpoint: String, url: String): String = URI(endpoint).resolve(url).toString()

    /** "2026-11-15" -> "15. 11. 2026" */
    fun day(iso: String): String = iso.split('-').mapNotNull { it.toIntOrNull() }.takeIf { it.size == 3 }?.let { (y, m, d) -> "$d. $m. $y" } ?: iso

    /** 7674560 -> "7,3 MB" */
    fun megabytes(bytes: Long): String = String.format(dev.hamper.tabula.compat.Locale.ROOT, "%.1f", bytes / 1048576.0).replace('.', ',') + " MB"
}

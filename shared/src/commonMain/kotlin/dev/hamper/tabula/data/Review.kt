package dev.hamper.tabula.data

import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.state.Attempt
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.state.dayOf

/*
 * Opakovanie na dnes (Teo, 19. 11.: "je toho too much… nikdy neodklikám všetko"; is it clear what comes back, why, and
 * when I'm done?). Review is a dose for today, not the whole queue: at most [DoseCap] things, mistakes first (newest
 * first), at most one thing on paper, and nothing answered today comes back before tomorrow. The queue itself and the
 * rules of mastery stay as they are (dueKeys, Store.mastered, Store.record): the dose only decides what is shown today.
 */

/** At most this many things a day. */
const val DoseCap = 5

/** Why a thing is back, in the order the reasons are checked. */
enum class Back { Wrong, Paper, Aided, Vouched, NotTwice }

/**
 * One thing of today's dose: its [key] (a review route), why it is [back], the day it went wrong ([wrongOn], null when
 * the record has no usable date) and its [mistake] when it is one.
 */
data class DoseItem(val key: String, val back: Back, val wrongOn: LocalDate? = null, val mistake: Mistake? = null)

/**
 * Today's dose: [items] still to do, in order; [done] things finished in today's round; [tomorrow] how many come back
 * tomorrow when nothing else changes.
 */
data class ReviewDose(val items: List<DoseItem>, val done: Int, val tomorrow: Int) {
    /** The dose is worked through today (and there was something in it). */
    val finished get() = items.isEmpty() && done > 0
    /** About two minutes a thing, three for one on paper. */
    val minutes get() = items.sumOf { if (onPaper(it.key)) 3 else 2 }
}

/** A definition or a proof from memory: written on paper (and so at most one a day). */
fun onPaper(key: String) = key.substringBefore(':') == "concept" || key.substringBefore(':') == "proof"

/**
 * The dose from plain parts of the record, nothing else read: [due] is the review queue in its order (dueKeys, only
 * what a review screen shows), [reviewed] what today's round finished already, [today] the day, [day] the local day of
 * a moment. A date that is missing or broken (an install from before the dates, `at == 0`) never hides a thing: it
 * only cannot say when.
 */
fun reviewDose(
    due: List<String>, mistakes: List<Mistake>, attempts: List<Attempt>, unwritten: Collection<String>, vouched: Collection<String>,
    reviewed: Collection<String>, today: LocalDate, day: (Long) -> LocalDate?,
): ReviewDose {
    fun dayOrNull(ms: Long): LocalDate? = if (ms <= 0L) null else runCatching { day(ms) }.getOrNull()
    val done = reviewed.toSet()
    // spacing: anything answered today, right or wrong, in review or in a lesson, waits for tomorrow
    // (a mistake filed today counts too: a lesson's question may record its answer under another key than the mistake)
    val answeredToday = attempts.filter { dayOrNull(it.at) == today }.map { it.key }.toSet() + mistakes.filter { dayOrNull(it.at) == today }.map { it.key }
    val byKey = mistakes.groupBy { it.key }
    val last = attempts.groupBy { it.key }.mapValues { it.value.last() }
    fun item(k: String): DoseItem {
        val m = byKey[k]?.maxByOrNull { it.at }
        if (m != null) {
            // when it went wrong: the mistake's own date, else the last wrong answer's
            val on = dayOrNull(m.at) ?: attempts.lastOrNull { it.key == k && !it.ok }?.let { dayOrNull(it.at) }
            return DoseItem(k, Back.Wrong, on, m)
        }
        return DoseItem(k, when {
            k in unwritten -> Back.Paper
            last[k]?.aided == true -> Back.Aided
            k in vouched -> Back.Vouched
            else -> Back.NotTwice
        })
    }
    /** Up to [room] things from [keys]: mistakes first, newest first, then the rest in the queue's order; one on paper. */
    fun pick(keys: List<String>, room: Int, paperTaken: Boolean): List<String> {
        val wrong = keys.filter { it in byKey }.sortedByDescending { k -> byKey.getValue(k).maxOf { it.at } }
        val rest = keys.filter { it !in byKey }
        var paper = paperTaken
        val out = ArrayList<String>()
        for (k in wrong + rest) {
            if (out.size >= room) break
            if (onPaper(k)) { if (paper) continue; paper = true }
            out += k
        }
        return out
    }
    val keys = due.distinct()
    val doneHere = done.size
    val today0 = pick(keys.filter { it !in answeredToday && it !in done }, (DoseCap - doneHere).coerceAtLeast(0), done.any(::onPaper))
    // tomorrow: the same queue with today's answers no longer in the way
    val tomorrow = pick(keys, DoseCap, false).size
    return ReviewDose(today0.map(::item), doneHere, tomorrow)
}

/** Today's dose of [course] from the [store]: [reviewDose] on its record. */
fun reviewToday(store: Store, today: LocalDate = LocalDate.now(), course: Course = store.course): ReviewDose {
    val due = dueKeys(store, course).filter { it.substringBefore(':') in dev.hamper.tabula.screens.ReviewKinds }
    val reviewed = store.reviewedOn(today).filter { courseOfRoute(it) == course }
    return reviewDose(due, store.mistakes.toList(), store.attempts.toList(), store.unwritten.toList(), store.vouched.toList(), reviewed, today) { LocalDate.parse(dayOf(it)) }
}

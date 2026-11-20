package dev.hamper.tabula.data

/*
 * What Dnes puts first and what it lists as unfinished, from the student's record only (Teo, 18. 11.: "nemám
 * dokončené tie MARAST kvízy, ale išiel som na ďalšiu lekciu, a stále chce na home page, aby som dokončil tú lekciu
 * predtým"). One pure function, tested in TodayTest; screens/Home.kt only draws it.
 */

/**
 * One lesson as Dnes sees it. [touched]: when it was last on screen (ms), null when unknown (an install from before
 * 1.5.5 knows only that it was begun); [left]: steps still to see; [gaps]: walked to its end with steps jumped over;
 * [forTest]: one of the lessons for the course's coming test.
 */
data class LessonState(val id: String, val done: Boolean, val begun: Boolean, val touched: Long?, val left: Int, val gaps: Boolean = false, val forTest: Boolean = false)

/** The hero lesson (null: none left to offer) and the older unfinished ones for "Dnes ešte", the newest first. */
data class TodayChoice(val hero: String?, val unfinished: List<String>)

/** How long an untouched unfinished lesson stays on Dnes; after that only the Cesta shows it. */
const val UnfinishedDays = 7

/**
 * [lessons] in the course's order (the test's lessons first while any is left, as the plan goes), [now] in ms,
 * [daysToTest] to the course's coming test (null: none).
 *
 * 1. The hero is the lesson worked on last while it is not done; when it is done, the next one in order not begun yet.
 * 2. Older unfinished lessons are rows under it, at most two, the newest first; one untouched for [UnfinishedDays]
 *    days leaves Dnes (an unknown time keeps it: it gets one at its next opening).
 * 3. On the day of the test and the day before, the hero is a lesson for the test while one is left: the one of them
 *    worked on last, else the first one not done; an unfinished lesson of other matter becomes a row.
 * 4. A lesson walked to its end with steps jumped over was offered there once; after that it is a row, the hero only
 *    when no other lesson is left.
 *
 * Without a time the lesson later in the course's order counts as the newer one (students go forward).
 */
fun todayChoice(lessons: List<LessonState>, now: Long, daysToTest: Int?): TodayChoice {
    val order = lessons.withIndex().associate { (i, l) -> l.id to i }
    val newest = compareBy<LessonState>({ it.touched ?: 0L }, { order.getValue(it.id) })
    val open = lessons.filter { !it.done && (it.begun || it.gaps) }
    val near = daysToTest != null && daysToTest in 0..1
    val test = lessons.filter { it.forTest && !it.done }
    // a lesson marked done in Nastavenia was not worked on: only a time makes a finished lesson the last one
    val last = lessons.filter { it.touched != null || (!it.done && (it.begun || it.gaps)) }.maxWithOrNull(newest)
    val hero = when {
        // a test lesson begun, else the first one left; one ended with jumped steps only when no other is left (rule 4)
        near && test.isNotEmpty() -> test.filter { it.begun && !it.gaps }.maxWithOrNull(newest)?.id ?: test.firstOrNull { !it.gaps }?.id ?: test.maxWith(newest).id
        last != null && !last.done && !last.gaps -> last.id
        // the next one not begun; else the newest unfinished one, one with gaps only when nothing else is left
        else -> lessons.firstOrNull { !it.done && !it.begun && !it.gaps }?.id ?: open.filter { !it.gaps }.maxWithOrNull(newest)?.id ?: open.maxWithOrNull(newest)?.id
    }
    val week = UnfinishedDays * 24L * 3600 * 1000
    val rows = open.filter { it.id != hero && (it.touched == null || now - it.touched <= week) }.sortedWith(newest.reversed()).take(2).map { it.id }
    return TodayChoice(hero, rows)
}

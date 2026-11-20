package dev.hamper.tabula.data

import dev.hamper.tabula.state.Store
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.DateTimeFormatter
import dev.hamper.tabula.compat.time.ChronoUnit
import dev.hamper.tabula.compat.time.*

fun daysTo(test: LocalDate, today: LocalDate = LocalDate.now()) = ChronoUnit.DAYS.between(today, test).toInt().coerceAtLeast(0)

/** Days to [test], negative when it is over (daysTo stops at 0). */
fun daysUntil(test: LocalDate, today: LocalDate = LocalDate.now()) = ChronoUnit.DAYS.between(today, test).toInt()
fun dayWord(n: Int) = when (n) { 1 -> "deň"; in 2..4 -> "dni"; else -> "dní" }
private val Weekdays = listOf("pondelok", "utorok", "streda", "štvrtok", "piatok", "sobota", "nedeľa")
private val WeekShort = listOf("Po", "Ut", "St", "Št", "Pi", "So", "Ne")
fun weekday(d: LocalDate) = Weekdays[d.dayOfWeek.value - 1]
fun weekday2(d: dev.hamper.tabula.compat.time.DayOfWeek) = Weekdays[d.value - 1]
val DayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("d. M.")

data class PlanTask(val title: String, val minutes: Int, val route: String, val hue: Char)
data class PlanDay(val date: LocalDate, val label: String, val tasks: List<PlanTask>)

/** Where a paper problem is checked in the path: after lesson [after], problem [n] of [paper]. */
data class Checkpoint(val after: String, val paper: String, val n: Int, val title: String, val pts: Int)
val La1Checkpoints = listOf(
    Checkpoint("ln", "slon-2024", 1, "Príklad 1 z testu", 7),
    Checkpoint("obal", "slon-2024", 3, "Príklad 3 z testu", 11),
    Checkpoint("hladanie", "sova-2025", 2, "Príklad 2 z testu", 12),
)

/** The review queue: what went wrong or was solved with help, newest first, then what was not tried twice cleanly. */
fun dueKeys(store: Store, course: Course = store.course): List<String> {
    val shown = course.shown()
    val mine = { k: String -> courseOfRoute(k) == course && shown.offersKey(k) }
    val wrong = store.mistakes.sortedByDescending { it.at }.map { it.key }.filter(mine)
    val aided = store.attempts.groupBy { it.key }.filter { (k, h) -> h.last().aided && k !in wrong && mine(k) }.keys.toList()
    val doneTemplates = shown.lessons.filter { it.id in store.lessonsDone }.flatMap { l -> l.steps.filterIsInstance<Step.Practice>().map { "gen:" + TemplateOf[it.gen] } }.distinct()
    val notYet = doneTemplates.filter { !store.mastered(it) && it !in wrong && it !in aided }
    // skipped with "Viem to": once, to check
    val vouched = store.vouched.filter { mine(it) && !store.mastered(it) }
    // paper steps put off with "Teraz nie": once, on paper
    val unwritten = store.unwritten.filter(mine)
    return (wrong + aided + vouched + unwritten + notYet).distinct()
}

/**
 * Deals what is left over the days until the test: the remaining lessons spread evenly, mistakes the day after,
 * a test problem on paper every second day, full mocks three days and a week before when there is room, the
 * day before only a light review.
 */
fun buildPlan(today: LocalDate, store: Store, course: Course = store.course): List<PlanDay> {
    val start = store.testStart(course)
    val test = start.toLocalDate()
    val n = ChronoUnit.DAYS.between(today, test).toInt().coerceAtLeast(0)
    val mocks = setOfNotNull((n - 2).takeIf { it > 0 }, (n - 5).takeIf { it > 1 })
    // only what this test covers and the app offers: LA1 kapitola 3 waits until the 2nd test is over
    val left = ArrayDeque(course.shown().testLessons.filter { it.id !in store.lessonsDone })
    val lessonDays = (0 until n).count { it !in mocks && it != n - 1 }.coerceAtLeast(1)
    val perDay = ((left.size + lessonDays - 1) / lessonDays).coerceIn(1, 3)
    // what the review screen can show (a paper problem stays in "Vracia sa ti"): a plan row never opens an empty review
    val due = dueKeys(store, course).count { it.substringBefore(':') in dev.hamper.tabula.screens.ReviewKinds }
    var paper = 0
    val paperRoutes = course.shown().planPapers.ifEmpty { listOf("papers") }
    val mins = course.test.minutes ?: 70
    val mockMins = course.shown().mockShape.third
    val out = ArrayList<PlanDay>()
    for (d in 0..n) {
        val date = today.plusDays(d.toLong())
        val label = when (d) { 0 -> "Dnes"; 1 -> "Zajtra"; else -> "${WeekShort[date.dayOfWeek.value - 1]} ${date.format(DayFmt)}" }
        val t = ArrayList<PlanTask>()
        when {
            // the test today, later on: a short review of what is due before it, if anything is
            d == n -> { if (n == 0 && due > 0) t += PlanTask("Ľahké opakovanie", 15, "review", 'm'); t += PlanTask("${course.test.short.replaceFirstChar { it.uppercase() }}${atTime(start)}", mins, "none", 'p') }
            d == n - 1 -> { t += PlanTask("Ľahké opakovanie", 15, "review", 'm'); t += PlanTask("Definície z pamäti", 10, "library", 'm') }
            d in mocks -> { t += PlanTask("Nácvik ${course.test.word.gen} na papieri", mockMins, "mock", 'p'); t += PlanTask("Oprava chýb z nácviku", 15, "review", 'm') }
            else -> {
                repeat(perDay) { left.removeFirstOrNull()?.let { l -> t += PlanTask(l.name, l.minutes, "lesson:${l.id}", 'l') } }
                if (d > 0 || due > 0) t += PlanTask("Zmiešané opakovanie", 10, "review", 'm')
                if (d % 2 == 1 || t.size < 2) t += PlanTask("Príklad ${dev.hamper.tabula.screens.zo(course.test.word.gen)} na papier", 15, paperRoutes[paper++ % paperRoutes.size], 'p')
            }
        }
        out += PlanDay(date, label, t)
    }
    return out
}

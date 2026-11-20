package dev.hamper.tabula.data

/*
 * "Zajtra píšem a toto neviem" (Teo, 19. 11., NAVRH.md 4): two days before the test a student whose lessons for a
 * problem are missing gets a quick way to that problem's most frequent type instead of the whole test on time, and
 * Dnes says the same as the tab Test. One pure function decides for both, tested in BeforeTestTest; screens/TestTab.kt
 * builds its input from the record and the quick way's lesson from the lessons' own steps.
 */

/** How many days before the test the window starts (the day of the test is 0). */
const val BeforeTestDays = 2

/**
 * A problem of the coming test as the decision sees it: [n] its number on the paper; [missing] a lesson for it the
 * course offers is not done; [share] how often its most frequent type that a lesson practises was on past papers
 * (count / of); [quickDone] its quick way was walked to the end; [quick] the quick way has steps at all.
 */
data class ProblemGap(val n: Int, val missing: Boolean, val share: Double, val quickDone: Boolean, val quick: Boolean)

/** What the window before the test puts first. */
sealed interface BeforeTest {
    /** The quick way to problem [n]. */
    data class Quick(val n: Int) : BeforeTest
    /** The whole test on time, once. */
    data object Whole : BeforeTest
}

/**
 * [daysToTest] days to the test (0: today), [recentMock] a whole test written in the last three days.
 *
 * 1. Outside the window ([BeforeTestDays] days before the test up to its day): null, the usual order decides (lessons,
 *    then problem types, then the whole test).
 * 2. In it, of the problems with a missing lesson whose quick way is not done, the one whose type was on the most past
 *    papers (a tie: the lower number) gets its quick way, also after a whole test written (it showed what is missing).
 * 3. None left: the whole test on time, unless one was written in the last three days (then null, the usual order).
 */
fun beforeTest(daysToTest: Int, recentMock: Boolean, problems: List<ProblemGap>): BeforeTest? {
    if (daysToTest !in 0..BeforeTestDays) return null
    problems.filter { it.missing && !it.quickDone && it.quick }
        .maxWithOrNull(compareBy<ProblemGap> { it.share }.thenByDescending { it.n })
        ?.let { return BeforeTest.Quick(it.n) }
    return if (recentMock) null else BeforeTest.Whole
}

/** The id of the quick way to problem [n] of the exam [exam]: kept in the record's finished lessons once walked. */
fun quickId(exam: String, n: Int) = "rychlo-$exam-$n"

/** The exam and the problem of a quick way's id, null for any other id. */
fun quickOf(id: String): Pair<String, Int>? {
    if (!id.startsWith("rychlo-")) return null
    val n = id.substringAfterLast('-').toIntOrNull() ?: return null
    return id.removePrefix("rychlo-").substringBeforeLast('-').takeIf { it.isNotEmpty() }?.let { it to n }
}

/**
 * The quick way to one problem type in [lessons] (the problem's lessons the course offers, in its order), for the
 * type keys in [types] (most frequent first): the first type a lesson practises, its worked example (the last one
 * before the practice step, the solution revealed step by step), the type with help, the same type without help.
 * Nothing new is written: the lesson's own steps. Empty when no lesson practises any of [types].
 */
fun quickSteps(lessons: List<LessonDef>, types: List<String>): List<Step> {
    fun key(s: Step) = (s as? Step.Practice)?.let { "gen:" + (TemplateOf[it.gen] ?: it.gen) }
    for (t in types) for (l in lessons) {
        val i = l.steps.indexOfFirst { key(it) == t }
        if (i < 0) continue
        val worked = l.steps.subList(0, i).lastOrNull { it is Step.Worked }
        val same = l.steps.filterIsInstance<Step.Practice>().filter { key(it) == t }
        return listOfNotNull(worked, same.firstOrNull { !it.solo }, same.firstOrNull { it.solo })
    }
    return emptyList()
}

/** Minutes of a quick way: about three for a worked example, four for an exercise. */
fun quickMinutes(steps: List<Step>) = steps.count { it is Step.Worked } * 3 + steps.count { it !is Step.Worked } * 4

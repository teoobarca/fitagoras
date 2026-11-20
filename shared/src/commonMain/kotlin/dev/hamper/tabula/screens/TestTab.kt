package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.res.*
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.state.dayOf
import dev.hamper.tabula.ui.*

/*
 * The tab "Test" (in every course, Teo, 19. 11.), in place of Nácvik and the plan (Teo, 18. 11.: "nech to je jednoduchšie… nikdy sa
 * toľko nepripravím a neodklikám všetko v appke"). At a glance: what comes (problem 1, 2, 3 as on the paper) and one
 * thing to do now. Everything else one level down: a problem's kinds, lessons and a real assignment on its own screen,
 * the other exercises behind "Všetky cvičenia". At most one number about progress per problem.
 */

/** A problem of the test as the tab shows it, from the page about the test (files/test/<exam>.json). */
class TestProblem(
    val pos: TestPageData.Position,
    /** What it is about: the page's title, else its definition and proof rows, else its most frequent kind. */
    val name: String,
    /** The lessons its kinds link to, in the course's order. */
    val lessons: List<LessonDef>,
    /** Its problem types (generator routes), one per template, those the course offers now. */
    val gens: List<String>,
    /** The same problem on the newest real paper of this exam that the app has, offered now. */
    val sample: Pair<TestPaper, PaperProblem>?,
)

private val PaperDate = Regex("""(\d{1,2})\. (\d{1,2})\. (\d{4})""")
private fun paperDate(p: TestPaper) = PaperDate.find(p.name)?.groupValues?.let { (_, d, m, y) -> y.toInt() * 10000 + m.toInt() * 100 + d.toInt() } ?: 0

/** The real papers of the current exam the app offers, newest first (no generated sets, no sample from another school). */
fun realPapers(course: Course): List<TestPaper> {
    val shown = course.shown()
    return course.papersFor(course.test).filter { !it.generated && "marast" !in it.id }.mapNotNull(shown::offered).sortedByDescending(::paperDate)
}

private fun genKey(route: String) = "gen:" + route.removePrefix("gen:").let { TemplateOf[it] ?: it }

fun testProblems(course: Course): List<TestProblem> {
    val page = testPage(course.test.id) ?: return emptyList()
    val shown = course.shown()
    val real = realPapers(course)
    return page.positions.map { pos ->
        val routes = pos.kinds.flatMap { it.routes }
        val lessonIds = routes.filter { it.startsWith("lesson:") }.map { it.substringAfter(':') }.toSet()
        val lessons = course.lessons.filter { it.id in lessonIds }
        val gens = routes.filter { it.startsWith("gen:") && shown.offersKey(it) }.distinctBy(::genKey)
        val name = pos.title ?: listOfNotNull(pos.definitions?.row, pos.proofs?.row).joinToString(" + ").ifEmpty { null } ?: pos.kinds.firstOrNull()?.label ?: ""
        val sample = real.firstNotNullOfOrNull { pp -> pp.problems.firstOrNull { it.n == pos.n }?.let { pp to it } }
        TestProblem(pos, name, lessons, gens, sample)
    }
}

/** The one thing the tab offers now: [title] with its [why], opened by [action] ([route]). */
data class TestNow(val title: String, val why: String, val action: String, val route: String)

/** The whole test on time: the newest real paper not written yet, else the list of papers. */
fun wholeTestRoute(store: Store, course: Course = store.course): String =
    realPapers(course).firstOrNull { p -> store.mocks.none { it.paper == p.id } }?.let { "mock:${it.id}" } ?: "papers"

fun wholeTestLabel(course: Course) = when (course.test.word) { TestWord -> "Celý test na čas"; PisomkaWord -> "Celá písomka na čas"; else -> "Celá ${course.test.word.nom} na čas" }

/**
 * What to do now for the test: two days before it the whole test on time (once); before that the first lesson for the
 * test not done yet, in the course's order (a beginner starts at the start, not at the lesson a problem links to),
 * then the first problem type not yet mastered, else the whole test. Null once the test is over.
 */
fun testNow(store: Store, problems: List<TestProblem>, today: LocalDate = LocalDate.now(), course: Course = store.course): TestNow? {
    if (store.testOver(course)) return null
    // an exam without a day (LA1's skúška before its KOS terms): no countdown to the window's first day
    val window = store.testWindow(course) != null
    val start = store.testStart(course)
    val days = daysTo(start.toLocalDate(), today)
    val word = course.test.word
    // a whole test written in the last three days counts as done for now
    val recentMock = store.mocks.any { m -> courseOfRoute("paper:${m.paper}") == course && daysTo(today, LocalDate.parse(dayOf(m.at))) <= 2 }
    val w = course.test.word
    val whenText = when (days) { 0 -> "dnes"; 1 -> "zajtra"; else -> "o $days ${slovakPlural(days, "deň", "dni", "dní")}" }
    val whole = TestNow(wholeTestLabel(course), (if (window) "" else "${w.nom.replaceFirstChar { it.uppercase() }} je $whenText. ") + "Raz to napíš celé na papier, ako naostro.", "Spustiť", wholeTestRoute(store, course))
    // two days before the test: the quick way to a problem whose lessons are missing, else the whole test
    // (data/BeforeTest.kt); an exam without a day has no countdown to such a window
    if (!window) when (val b = beforeTest(days, recentMock, problems.map { problemGap(store, course, it) })) {
        is BeforeTest.Quick -> problems.firstOrNull { it.pos.n == b.n }?.let { p -> quickLesson(course, p)?.let { return quickNow(store, p, it) } }
        BeforeTest.Whole -> return whole
        null -> {}
    }
    course.shown().testLessons.firstOrNull { it.id !in store.lessonsDone }?.let { l ->
        val p = problems.firstOrNull { l in it.lessons }
        return TestNow(l.name, (if (p != null) "Lekcia k príkladu ${p.pos.n}" else "Lekcia na ${w.acc}") + ", ${aboutTime(minutesToGo(store, l))}", if (store.inProgress(l)) "Pokračovať" else "Začať lekciu", "lesson:${l.id}")
    }
    for (p in problems) p.gens.firstOrNull { !store.mastered(genKey(it)) }?.let { g ->
        val kind = p.pos.kinds.firstOrNull { g in it.routes }
        return TestNow(kind?.label ?: p.name, "Príklad ${p.pos.n}${kind?.let { ", bol v ${it.count} z ${it.of} ${word.genPl}" } ?: ""}", "Precvičiť", "template:${g.removePrefix("gen:")}")
    }
    return whole
}

/** The problem's types (generator keys) its kinds name, the kinds seen on the most past papers first. */
private fun quickTypes(p: TestProblem): List<String> =
    p.pos.kinds.sortedByDescending { it.count.toDouble() / it.of.coerceAtLeast(1) }.flatMap { k -> k.routes.filter { it.startsWith("gen:") }.map(::genKey) }.distinct()

/** The quick way to [p] as a lesson (data/BeforeTest.kt, quickSteps), null when no lesson of it practises its types. */
fun quickLesson(course: Course, p: TestProblem): LessonDef? {
    val open = course.shown().lessons
    val steps = quickSteps(p.lessons.filter { it in open }, quickTypes(p)).ifEmpty { return null }
    return LessonDef(quickId(course.test.id, p.pos.n), "Rýchlo k príkladu ${p.pos.n}: ${quickKind(p, steps)}", quickMinutes(steps), "", steps, 0)
}

/**
 * What the quick way practises: the label of the problem's kind its exercise is (LA1 problem 2 "Výpočet v Z_p^4" is
 * done through "Súradnice vektora v báze" in R^3: the problem's name promised what the first step did not show;
 * student, 19. 11.), else the problem's name.
 */
private fun quickKind(p: TestProblem, steps: List<Step>): String {
    val key = steps.firstNotNullOfOrNull { (it as? Step.Practice)?.let(::practiceKey) }
    return p.pos.kinds.firstOrNull { k -> k.routes.any { it.startsWith("gen:") && genKey(it) == key } }?.label ?: p.name
}

/** A quick way by its id ("rychlo-<exam>-<n>"), for the course whose test that exam is now. */
fun quickLessonById(id: String): LessonDef? {
    val (exam, n) = quickOf(id) ?: return null
    val course = Courses.firstOrNull { it.test.id == exam } ?: return null
    return testProblems(course).firstOrNull { it.pos.n == n }?.let { quickLesson(course, it) }
}

/** A lesson by id: one of the courses', else a quick way to a problem of a test. */
fun anyLesson(id: String): LessonDef? = LessonById[id] ?: quickLessonById(id)

/** [p] as the window before the test sees it (data/BeforeTest.kt). */
fun problemGap(store: Store, course: Course, p: TestProblem): ProblemGap {
    val open = course.shown().lessons
    val ls = p.lessons.filter { it in open }
    val types = quickTypes(p)
    // how often the type the quick way practises was on past papers
    val q = quickSteps(ls, types)
    val key = q.firstNotNullOfOrNull { (it as? Step.Practice)?.let(::practiceKey) }
    val share = p.pos.kinds.filter { k -> k.routes.any { it.startsWith("gen:") && genKey(it) == key } }.maxOfOrNull { it.count.toDouble() / it.of.coerceAtLeast(1) } ?: 0.0
    return ProblemGap(p.pos.n, ls.any { it.id !in store.lessonsDone }, share, quickId(course.test.id, p.pos.n) in store.lessonsDone, q.isNotEmpty())
}

/** The card of a quick way: what it is made of and how long (no more: its title says why), "Začať" or "Pokračovať". */
private fun quickNow(store: Store, p: TestProblem, q: LessonDef): TestNow {
    val w = q.steps.count { it is Step.Worked }
    val e = q.steps.size - w
    val parts = listOfNotNull(
        if (w > 0) "vzorový príklad" else null,
        if (e > 0) "$e ${slovakPlural(e, "úloha", "úlohy", "úloh")}" else null,
    ).joinToString(" a ")
    return TestNow(
        q.name,
        "${parts.replaceFirstChar { it.uppercase() }} · ${aboutTime(minutesToGo(store, q))}",
        if (store.inProgress(q)) "Pokračovať" else "Začať", "lesson:${q.id}",
    )
}

/**
 * What the window before the test puts first for [course] (the quick way or the whole test), the same card the tab
 * Test shows; null outside it or when the usual order decides. Dnes shows it as its hero.
 */
fun beforeTestNow(store: Store, course: Course = store.course, today: LocalDate = LocalDate.now()): TestNow? {
    // an exam without a day has no window before it; an open "Písal si už…?" goes first on the tab
    if (store.testWindow(course) != null || sittingQuestion(store, course) != null) return null
    val start = store.testStart(course)
    if (daysTo(start.toLocalDate(), today) !in 0..BeforeTestDays) return null
    val now = testNow(store, testProblems(course), today, course) ?: return null
    return now.takeIf { it.route.startsWith("lesson:rychlo-") || it.route.startsWith("mock:") || it.route == "papers" }
}

/**
 * Did the student write the test already? Asked while the course's exam has KOS terms, none is picked, at least one is
 * over and another still ahead (LA1 on 19. 11.: 12. and 18. 11. written, 20. 11. to come): without the answer the tab
 * would show the next term as if the test were still ahead. [past] the terms over, [next] the sittings of the first day
 * still ahead (LA1: 20. 11. at 12:45 and at 14:30; "Ešte nie" picking only the first would end the test for the second).
 */
data class SittingQuestion(val past: List<Term>, val next: List<Term>)

fun sittingQuestion(store: Store, course: Course = store.course, now: LocalDateTime = LocalDateTime.now()): SittingQuestion? {
    val e = course.test
    if (e.terms.isEmpty() || store.terms[e.id] != null) return null
    val (past, ahead) = e.terms.sortedBy { it.start }.partition { it.start.plusMinutes(it.minutes.toLong()) <= now }
    return if (past.isEmpty() || ahead.isEmpty()) null else SittingQuestion(past, ahead.filter { it.start.toLocalDate() == ahead.first().start.toLocalDate() })
}

/** The one number a problem shows: what is missing, or how many of its types are mastered. */
fun problemStatus(store: Store, p: TestProblem, course: Course = store.course): String {
    val open = course.shown().lessons
    val missing = p.lessons.filter { it in open && it.id !in store.lessonsDone }
    if (missing.isNotEmpty()) return if (missing.size == 1) "Chýba lekcia ${missing[0].name}" else "Chýbajú ${missing.size} lekcie"
    if (p.gens.isEmpty()) return if (p.lessons.isNotEmpty() && p.lessons.none { it in open }) "Ešte nebola prednáška" else ""
    val done = p.gens.count { store.mastered(genKey(it)) }
    return if (done == p.gens.size) "Precvičené" else "Precvičené $done z ${p.gens.size} ${slovakPlural(p.gens.size, "typu", "typov", "typov")} úloh"
}

@Composable
fun TestTabScreen(store: Store, bottom: Dp, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    val course = store.course
    val today = rememberCurrentDate()
    // keyed by the exam too: at midnight a course may move on to its next exam (LA1: the test, then the exam)
    val problems = androidx.compose.runtime.remember(course.id, course.test.id, store.version) { testProblems(course) }
    val now = testNow(store, problems, today)
    val start = store.testStart(course)
    val window = store.testWindow(course)
    val ask = sittingQuestion(store, course)
    val platform = dev.hamper.tabula.platform.LocalPlatform.current
    val scroll = rememberScrollState()
    var seen by rememberSaveable { mutableIntStateOf(store.lessonsDone.size) }
    LaunchedEffect(store.lessonsDone.size) { if (store.lessonsDone.size != seen) { seen = store.lessonsDone.size; scroll.scrollTo(0) } }
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(scroll).padding(horizontal = 20.dp).padding(top = 10.dp, bottom = bottom + 40.dp)) {
        Text("${course.test.short} z ${course.short}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = c.lesson.text)
        // the day, or for an exam without one its window ("4. 1. – 14. 2."), never the window's first day as if it were the day
        Text(
            when { now == null -> "Je za tebou"; window != null -> windowText(window); else -> "${weekday(start.toLocalDate()).replaceFirstChar { it.uppercase() }} ${start.format(DayFmt)}${atTime(start)}" },
            Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge, color = c.ink,
        )
        // the exam as its page and the course state it (LA1's skúška: 4 problems, 60 b, 120 min); the mock's shape only
        // without a page (a generated set may cover fewer problems than the exam)
        val (n, pts, min) = testPage(course.test.id)?.let { Triple(it.points.size, course.test.points, course.test.minutes ?: it.minutes) } ?: course.shown().mockShape
        // after the switch to the next exam one clause says why ("2. test máš za sebou"), and where its day comes from
        val lead = listOfNotNull(
            justWritten(store, course)?.let { "${it.short} máš za sebou" },
            window?.let { "termín ${course.test.word.gen} si vyberieš v KOS" },
        ).joinToString(", ").replaceFirstChar { it.uppercase() }.let { if (it.isEmpty()) "" else "$it. " }
        // what the test is, in a sentence (Teo, 18. 11.: "tie 3 príklady, to sú aké? je to niekde vysvetlené?"); each
        // card below says what its problem is
        Text("${lead}Na papier, $min minút: $n ${slovakPlural(n, "príklad", "rôzne príklady", "rôznych príkladov")} za $pts bodov, každý s celým postupom.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        // a mistaken "Áno" in the question below can be taken back while the test's last term is still ahead
        justWritten(store, course)?.takeIf { e -> store.terms[e.id] != null && examOver(e) > LocalDateTime.now() }?.let { e ->
            Quiet("Ešte som ${e.acc} nepísal", { store.unpickTerm(e.id); platform.refreshWidget() }, color = c.lesson.text)
        }
        if (ask != null) {
            Spacer(Modifier.height(16.dp))
            SittingAsk(course, ask) { t -> store.pickTerm(course, t.start); platform.refreshWidget() }
        } else if (now != null) {
            Spacer(Modifier.height(16.dp))
            TestNowBlock("Teraz", now, onRoute)
        }
        Spacer(Modifier.height(22.dp))
        val sure = testPage(course.test.id)?.pointsSure != false
        for (p in problems) {
            val status = problemStatus(store, p)
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp).card(radius = 20.dp).clip(RoundedCornerShape(20.dp))
                    .clickable(role = Role.Button) { onRoute("priklad:${p.pos.n}") }.padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // the number with the name ("1. Parameter a"), what it is under it, the points beside: the bare
                // "Príklad 1" said nothing of the problem
                Column(Modifier.weight(1f)) {
                    Rich("${p.pos.n}. ${p.name}", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    p.pos.about?.let { Rich(it, Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodySmall, color = c.ink2) }
                    if (status.isNotEmpty()) Text(status, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = if (status == "Precvičené") c.good else c.lesson.text)
                }
                if (sure) { Spacer(Modifier.width(8.dp)); Text("${p.pos.points}\u00A0b", style = MaterialTheme.typography.labelLarge, color = c.ink2, softWrap = false) }
                Icon24(Res.drawable.ic_chevron, tint = c.ink3)
            }
        }
        Spacer(Modifier.height(6.dp))
        if (now?.route?.let { it.startsWith("mock:") || it == "papers" } != true) Quiet(wholeTestLabel(course), { onRoute(wholeTestRoute(store)) }, color = c.lesson.text)
        testPage(course.test.id)?.let { page -> Quiet(page.title, { onRoute("temy") }, color = c.lesson.text) }
        Quiet("Všetky cvičenia", { onRoute("cvicenia") }, color = c.lesson.text)
    }
}

/**
 * The tab's one thing ([label] above it): the same green block as the lesson on Dnes (the look of 18. 11., ui/Smer.kt).
 * Dnes shows it as its hero in the window before the test, so both say the same.
 */
@Composable
fun TestNowBlock(label: String, now: TestNow, onRoute: (String) -> Unit) {
    val c = T.c
    val fill = boldFill(c)
    CompositionLocalProvider(LocalPalette provides (if (SmerNow.bold) solidOn(c, fill) else c)) {
        val s = T.c
        Column(Modifier.fillMaxWidth().then(if (SmerNow.bold) Modifier.clip(RoundedCornerShape(28.dp)).background(fill).padding(20.dp) else Modifier.card().padding(18.dp))) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = s.ink2)
            Spacer(Modifier.height(2.dp))
            Rich(now.title, style = MaterialTheme.typography.titleLarge, color = s.ink)
            Text(now.why, style = MaterialTheme.typography.bodyMedium, color = s.ink2)
            Spacer(Modifier.height(14.dp))
            Cta(now.action, { onRoute(now.route) })
        }
    }
}

/**
 * "Písal si už 2. test?" in the tab's green block, in place of its one thing: one button per term over ("Áno, 18. 11.")
 * and "Ešte nie" (the next term; one per sitting when that day has several, "Ešte nie, 14:30"). The answer is the sitting the student picks in Settings, so Dnes, Settings and the
 * tab move on together; a sitting over moves the course on to its next exam at once.
 */
@Composable
private fun SittingAsk(course: Course, q: SittingQuestion, onPick: (Term) -> Unit) {
    val c = T.c
    val e = course.test
    val ps = course.exams.filter { it.prepares }
    val after = ps.getOrNull(ps.indexOf(e) + 1)
    // a day with two sittings over names the time too
    fun label(t: Term) = "Áno, ${t.start.format(DayFmt)}" + if (q.past.count { it.start.toLocalDate() == t.start.toLocalDate() } > 1) " ${t.start.toLocalTime()}" else ""
    val options = q.past.map { label(it) to it } + q.next.map { t -> (if (q.next.size > 1) "Ešte nie, ${t.start.toLocalTime()}" else "Ešte nie") to t }
    val fill = boldFill(c)
    CompositionLocalProvider(LocalPalette provides (if (SmerNow.bold) solidOn(c, fill) else c)) {
        val s = T.c
        Column(Modifier.fillMaxWidth().then(if (SmerNow.bold) Modifier.clip(RoundedCornerShape(28.dp)).background(fill).padding(20.dp) else Modifier.card().padding(18.dp))) {
            Text("Písal si už ${e.acc}?", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = s.ink)
            after?.let { Text("Ak áno, appka prejde na ${it.acc}.", style = MaterialTheme.typography.bodyMedium, color = s.ink2) }
            Spacer(Modifier.height(14.dp))
            options.chunked(2).forEachIndexed { i, row ->
                if (i > 0) Spacer(Modifier.height(10.dp))
                ButtonRow { for ((text, t) in row) Second(text, { onPick(t) }) }
            }
        }
    }
}

/** One problem of the test, one level below the tab: a real assignment to write, what it usually asks, its lessons. */
@Composable
fun TestProblemScreen(store: Store, n: Int, onClose: () -> Unit, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    val course = store.course
    val p = testProblems(course).firstOrNull { it.pos.n == n } ?: run { LaunchedEffect(n) { onClose() }; return }
    val sure = testPage(course.test.id)?.pointsSure != false
    val sample = p.sample
    StepFrame(0, 0, onClose, closeLabel = "Späť", footer = sample?.let { (pp, pr) -> { Cta("Napísať na papier · ${pr.minutes} min", { onRoute("paper:${pp.id}:${pr.n}") }, icon = Res.drawable.ic_write) } }) {
        Label("Príklad ${p.pos.n}" + if (sure) " · ${p.pos.points} b" else "")
        Spacer(Modifier.height(2.dp))
        H1(p.name)
        if (sample != null) {
            Spacer(Modifier.height(12.dp))
            SourceCard(sample.first.name) { ProblemBlocks(sample.second, null) }
        }
        val open = course.shown().lessons
        @Composable
        fun row(title: String, side: String?, done: Boolean, route: String?) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).then(if (route != null) Modifier.clickable(role = Role.Button) { onRoute(route) } else Modifier)
                    .heightIn(min = 48.dp).padding(horizontal = 2.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Rich(title, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = if (route != null) c.ink else c.ink2)
                if (side != null) { Spacer(Modifier.width(8.dp)); Text(side, style = MaterialTheme.typography.bodySmall, color = c.ink2) }
                if (done) Box(Modifier.padding(start = 6.dp).size(22.dp).clip(CircleShape).background(c.goodTint), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(14.dp)) }
                else if (route != null) Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Čo tam býva", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = c.ink)
        p.pos.definitions?.let { row(it.row, "${it.count} z ${it.of} ${course.test.word.genPl}", false, "library") }
        p.pos.proofs?.let { row(it.row, "${it.count} z ${it.of} ${course.test.word.genPl}", false, "library:proofs") }
        for (k in p.pos.kinds) {
            val g = k.routes.firstOrNull { it.startsWith("gen:") && course.shown().offersKey(it) }
            row(k.label, "${k.count} z ${k.of} ${course.test.word.genPl}", g != null && store.mastered(genKey(g)), g?.let { "template:" + it.removePrefix("gen:") })
        }
        if (p.lessons.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Lekcie", Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = c.ink)
            for (l in p.lessons) row(l.name, if (l in open) null else "po prednáške", l.id in store.lessonsDone, if (l in open) "lesson:${l.id}" else null)
        }
        Spacer(Modifier.height(12.dp))
    }
}

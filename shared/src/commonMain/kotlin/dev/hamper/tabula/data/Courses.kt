package dev.hamper.tabula.data

import dev.hamper.tabula.compat.time.DayOfWeek
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.LocalDateTime
import dev.hamper.tabula.compat.time.LocalTime
import kotlin.random.Random
import dev.hamper.tabula.compat.time.*
import kotlin.concurrent.Volatile

/*
 * The courses of the app. Each is its own path (lessons, definitions, test papers, templates), its own schedule and its
 * own tests; ids of steps, definitions, proofs and generators are unique across courses, so the progress record and
 * every route stay one flat namespace and an update keeps what was learned.
 */

/** A lecture parallel: who lectures, on which weekday and when (the faculty timetable). */
data class Parallel(val id: String, val teacher: String, val day: DayOfWeek, val time: LocalTime)

/** One lecture of one parallel: its label in that parallel (P7), the teaching week, its date and the topics it covers. */
data class Lecture(val label: String, val week: Int, val date: LocalDate, val topics: List<String>, val reached: String? = null)

data class CourseSchedule(val checked: LocalDate, val parallels: List<Parallel>, val lectures: Map<String, List<Lecture>>)

/** A sitting of a test as KOS lists it. */
data class Term(val start: LocalDateTime, val minutes: Int, val room: String, val note: String? = null)

/**
 * A graded event of the course. [terms]: the sittings to pick from (KOS); without terms, [from]..[to] is when it happens
 * (a tutorial week, the exam period). [prepares]: the app's papers and plan are made for this one.
 */
data class Exam(
    /** [short]: "2. test", "Skúška" (nominative, starts a sentence); [acc]: "2. písomku", "skúšku" (after "na", "za"). */
    val id: String, val title: String, val short: String, val acc: String, val points: Int, val minutes: Int?,
    val terms: List<Term> = emptyList(), val from: LocalDate? = null, val to: LocalDate? = null,
    val topics: List<String> = emptyList(), val note: String, val src: String, val prepares: Boolean = false,
    /** What the course calls this kind of exam in a sentence: LA1 writes "test", DML "písomka" ("do písomky", "na písomke"). */
    val word: ExamWord = TestWord,
)

/** The noun of an exam kind in the cases the app's sentences need, and "na skutočnom teste" with its adjective. */
data class ExamWord(val nom: String, val gen: String, val acc: String, val loc: String, val realLoc: String, val genPl: String)
val TestWord = ExamWord("test", "testu", "test", "teste", "skutočnom teste", "testov")
val PisomkaWord = ExamWord("písomka", "písomky", "písomku", "písomke", "skutočnej písomke", "písomiek")
val SkuskaWord = ExamWord("skúška", "skúšky", "skúšku", "skúške", "skutočnej skúške", "skúšok")

/** A row of the practice tab beyond the templates (a drill screen of its own). */
data class ExtraDrill(val art: Int, val title: String, val sub: String, val route: String)

data class Course(
    val id: String,
    val code: String,
    val short: String,
    val name: String,
    /** What the path is about, the heading of the Cesta tab: "Základné pojmy". */
    val chapter: String,
    /** The label above it: "BI-LA1, na 2. test". */
    val pathLabel: String,
    val lessons: List<LessonDef>,
    val concepts: List<Concept>,
    val theorems: List<Theorem>,
    val proofs: List<Proof>,
    /** Past papers of every exam the app prepares for; [papersFor] picks the ones of one exam. */
    val papers: List<TestPaper>,
    /** A paper with new numbers for the current [test]; its id says which exam (see [generatedById]). */
    val generatedPaper: (Int) -> TestPaper,
    /** A generated paper by id ("gen-12", "dmlgen-12", "la1gen-12"), null if the id is not this course's. */
    val generatedById: (String) -> TestPaper?,
    val templates: List<TemplateInfo>,
    val checkpoints: List<Checkpoint>,
    /** Paper problems the plan deals out, as routes. */
    val planPapers: List<String>,
    val drills: List<ExtraDrill>,
    val schedule: CourseSchedule,
    val exams: List<Exam>,
    val topicNames: Map<String, String>,
    /** The heading of the library: "Kapitola 2, Základní pojmy lineární algebry". */
    val libraryLabel: String,
    val sources: String,
    /** Clips name their lecture: "Lineárna algebra, 4. prednáška, Klouda". */
    val clipLecturer: String,
    val clipTeacher: String,
    /** The instructions printed on top of the course's test (Czech, word for word). */
    val paperHeader: String,
    /** What "Nové čísla" in the papers list makes. */
    val generatedNote: String,
    /** The topics the content comes from (for what is shown before its lecture); LA1 kapitola 2 is in none. */
    val topics: List<Topic> = emptyList(),
) {
    /**
     * The exam the app prepares for now: the first one marked [Exam.prepares] that is not over (its last term or window
     * end), else the last such. LA1 moves from the 2nd test to the exam after 20. 11.
     */
    val test: Exam get() = testAt(LocalDateTime.now())
    /** The student's own sitting counts: whoever wrote the 2nd test on 12. 11. prepares for the exam from then on. */
    fun testAt(now: LocalDateTime): Exam = exams.filter { it.prepares }.let { ps -> ps.firstOrNull { sittingOver(it) > now } ?: ps.last() }
    private fun sittingOver(e: Exam): LocalDateTime = Sittings.picked(e.id)?.let { it.plusMinutes((e.minutes ?: 90).toLong()) } ?: examOver(e)
    fun papersFor(e: Exam): List<TestPaper> = papers.filter { (it.exam ?: exams.first { x -> x.prepares }.id) == e.id }
    fun lectures(parallel: String): List<Lecture> = schedule.lectures[parallel] ?: schedule.lectures.values.first()
    fun parallel(id: String): Parallel = schedule.parallels.firstOrNull { it.id == id } ?: schedule.parallels.first()
    /** A topic code in words: "TČ2" -> "Teória čísel 2", "S3" -> "Hodnosť a regularita matíc". */
    fun topicName(code: String): String {
        topicNames[code]?.let { return it }
        val m = Regex("^(\\D+)(\\d+)$").find(code) ?: return code
        val base = topicNames[m.groupValues[1]] ?: return code
        return "$base ${m.groupValues[2]}"
    }
    /** Lessons that teach what a lecture covered. */
    fun lessonsFor(l: Lecture): List<LessonDef> = lessons.filter { les -> les.topics.any { t -> l.topics.any { it == t || it.startsWith(t) || t.startsWith(it) } } }
    fun owns(lessonId: String) = lessons.any { it.id == lessonId }
    /** Every generator of the course: the practice tab's and those its lessons use. */
    val gens: Set<String> by lazy { templates.map { it.gen }.toSet() + lessons.flatMap { l -> l.steps.filterIsInstance<Step.Practice>().map { it.gen } } }
    /** Does a lesson teach what this exam covers (by topic codes; a lesson without topics counts for every exam)? */
    fun forExam(l: LessonDef, e: Exam): Boolean = l.topics.isEmpty() || e.topics.isEmpty() || l.topics.any { t -> e.topics.any { t == it || t.startsWith(it) } }
    /** The lessons the current test needs, in path order (also those left out until lectured; see [Shown]). */
    val testLessons: List<LessonDef> get() = test.let { e -> lessons.filter { forExam(it, e) } }
    /** What the app offers: only what has been lectured (Teo's rule, docs/CONTENT.md). */
    fun shown(): Shown = shownBy.getOrPut(id) { Shown(this) }
    /** A heading for the part of the path a lesson belongs to: "Matematická indukcia", "Hodnosť a regularita matíc". */
    fun partName(l: LessonDef): String = l.topics.firstOrNull()?.let { code -> topicNames[code.trimEnd { it.isDigit() }] ?: topicName(code) } ?: chapter
}

const val DefaultParallel = "2"

/* ---- the courses ------------------------------------------------------------------------------------------------- */

private fun at(s: String) = LocalDateTime.parse(s)

/**
 * Past LA1 exams (skúška), problems written by the LA1 topics (`paperProblems` keyed by these ids). Empty until the
 * kapitola 3 author fills it; points must add up to the exam's 60.
 */
val La1ExamSpecs: List<PaperSpec> = emptyList()

/** LA1 content after kapitola 2 (which predates topics and lives in Lessons.kt, Concepts.kt, ...), in schedule order. */
val La1Topics: List<Topic> = listOf(dev.hamper.tabula.data.la1.La1Chapter3, dev.hamper.tabula.data.la1.La1Chapter4)

val La1: Course = Course(
    id = "la1", code = "BI-LA1", short = "LA1", name = "Lineárna algebra 1",
    chapter = "Lineárna algebra 1", pathLabel = "BI-LA1",
    lessons = La1Lessons + La1Topics.flatMap { it.lessons }, concepts = La1Concepts + La1Topics.flatMap { it.concepts },
    theorems = La1Theorems + La1Topics.flatMap { it.theorems }, proofs = La1Proofs + La1Topics.flatMap { it.proofs },
    papers = La1Papers + assemblePapers(La1ExamSpecs, La1Topics),
    generatedPaper = { seed -> if (La1.test.id == "la1-test2") la1GeneratedPaper(seed) else assembleGenerated("la1gen-", seed, La1Topics, "la1-skuska") },
    generatedById = { id ->
        when {
            id.startsWith("la1gen-") -> assembleGenerated("la1gen-", id.removePrefix("la1gen-").toIntOrNull() ?: 1, La1Topics, "la1-skuska")
            id.startsWith("gen-") -> la1GeneratedPaper(id.removePrefix("gen-").toIntOrNull() ?: 1)
            else -> null
        }
    },
    templates = La1Templates + La1Topics.flatMap { it.templates }, checkpoints = La1Checkpoints + La1Topics.flatMap { it.checkpoints },
    planPapers = listOf("paper:slon-2024:1", "paper:sova-2025:2", "paper:medved-2024:2", "paper:slon-2024:3", "paper:sova-2023:1", "paper:slepice-2023:2"),
    drills = listOf(
        ExtraDrill(7, "GEM v §\\mathbb{Z}_5§", "Celá eliminácia po políčkach, nové čísla", "gem"),
        ExtraDrill(3, "Protipríklad", "Polož vektory tak, aby tvrdenie padlo", "counter"),
    ),
    schedule = La1Schedule,
    exams = listOf(
        Exam("la1-test1", "1. zápočtový test", "1. test", "1. test", 10, null, from = LocalDate.of(2026, 10, 19), to = LocalDate.of(2026, 10, 23), topics = listOf("S1"),
            note = "Na tvojom 4. cvičení: sústavy rovníc, GEM, matice, počítanie modulo.", src = "courses.fit.cvut.cz, BI-LA1, harmonogram"),
        Exam("la1-test2", "2. zápočtový test", "2. test", "2. test", 30, 70,
            terms = listOf(
                Term(at("2026-11-12T16:15"), 70, "T9:105"), Term(at("2026-11-18T18:00"), 70, "T9:105"),
                Term(at("2026-11-20T12:45"), 70, "T9:105"), Term(at("2026-11-20T14:30"), 70, "T9:105"),
            ),
            topics = listOf("S2"), prepares = true,
            note = "Základné pojmy: vektorový priestor, podpriestor, lineárna kombinácia, LN a LZ, obal, báza, dimenzia, súradnice. Tri príklady, 7 + 12 + 11 b.",
            src = "KOS, termíny 2. testu (15. 11. 2026); courses.fit.cvut.cz, BI-LA1, Hodnocení"),
        Exam("la1-opravny", "Opravný zápočtový test", "Opravný test", "opravný test", 30, null, from = LocalDate.of(2027, 1, 4), to = LocalDate.of(2027, 1, 9), topics = listOf("S1", "S2"),
            note = "Len pri 18 až 22,5 b a so všetkými štyrmi kvízmi ku kapitole 1 a 2. Treba aspoň polovicu, potom zápočet s 23 b.", src = "courses.fit.cvut.cz, BI-LA1, Hodnocení"),
        Exam("la1-skuska", "Skúška", "Skúška", "skúšku", 60, 120, from = LocalDate.of(2027, 1, 4), to = LocalDate.of(2027, 2, 14), topics = listOf("S3", "S4", "S5", "S6"), prepares = true,
            note = "Písomná, dve hodiny, treba 30 zo 60 b. Hlavne hodnosť a regularita, Frobeniova veta, determinant, vlastné čísla. Termíny v KOS.", src = "courses.fit.cvut.cz, BI-LA1, Hodnocení; MARAST lekce 262 (ukážková skúška: dve hodiny, 4 príklady)", word = SkuskaWord),
    ),
    topicNames = mapOf(
        "S1" to "Sústavy lineárnych rovníc", "S2" to "Základné pojmy lineárnej algebry", "S3" to "Hodnosť a regularita matíc",
        "S4" to "Frobeniova veta a lineárne variety", "S5" to "Determinant", "S6" to "Vlastné čísla a diagonalizovateľnosť",
    ),
    libraryLabel = "Kapitola 2, Základní pojmy lineární algebry",
    sources = "Skriptá BI-LA1 (kapitola 2, číslovanie ako v skriptách), courses.fit.cvut.cz (čo chce 2. test, harmonogram), KOS (termíny), testy z fit-wiki 2022 až 2025, MARAST lekce 292, titulky prednášok 4 až 6 (paralelka 2, Klouda), Discord #la1. Výpočty robí appka presne, zlomkami a modulo p.",
    clipLecturer = "Lineárna algebra", clipTeacher = "Klouda",
    paperHeader = Header,
    topics = La1Topics,
    generatedNote = "Príklad 1 a 2 so šablónami testov, definície a dôkaz z minulých testov",
)

/** Lazy: Dml.kt reads values of this file (PisomkaWord) while it initialises, so an eager list could catch Dml still null. */
val Courses: List<Course> by lazy { listOf(La1, Dml) }

fun courseById(id: String?): Course = Courses.firstOrNull { it.id == id } ?: La1

/** The course a route belongs to (a mistake, a review item), so each course reviews its own. */
fun courseOfRoute(route: String): Course? {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':').substringBefore(':')
    return Courses.firstOrNull { c ->
        when (kind) {
            // a quick way before a test (data/BeforeTest.kt) belongs to the course whose exam it is for
            "lesson" -> c.lessons.any { it.id == id } || quickOf(id)?.first?.let { e -> c.exams.any { it.id == e } } == true
            "step" -> c.lessons.any { l -> l.steps.any { it.id == id } }
            "concept" -> c.concepts.any { it.id == id }
            "theorem" -> c.theorems.any { it.id == id }
            "proof" -> c.proofs.any { it.id == id }
            "gen", "template" -> genOf(id)?.let { g -> g in c.gens } ?: false
            "paper" -> c.papers.any { it.id == id } || c.generatedById(id) != null
            "clip" -> dev.hamper.tabula.screens.ClipLibrary.all.firstOrNull { it.id == id }?.course == c.id
            "gem", "counter" -> c == La1
            else -> false
        }
    }
}

/* ---- only what has been lectured ---------------------------------------------------------------------------------- */

/**
 * Teo's rule: lessons only for topics some parallel has already lectured, built from that lecture. These are built but
 * no lecture has covered them yet (checked against the captions up to [LecturesCheckedAt]), so the app leaves them out,
 * with everything only they lead to. Integrator-owned: remove an id once a lecture covers its topic.
 */
val NotYetLectured: Set<String> = setOf(
    // DML, checked 24. 11. 2026 against the captions. Spěvák (paralelka 2, 8. prednáška, 18. 11.) lectured cancelling
    // with a change of modulus (Věta 6.13, proved), linear congruences (Věta 6.17, 12x ≡ 18 mod 75), lcm of several
    // numbers, the generalised Chinese theorem and the Chinese theorem (stated) with substitution, Euler's function, Euler's
    // theorem with its proof, Fermat as its corollary, the inverse through Euler and the reduction of a power with a
    // coprime base (2^268 mod 9): dml-tc-lkon, dml-tc-cvoz and dml-tc-mocniny are built from that lecture.
    // Dombek (paralelka 1, 9. prednáška, 23. 11.) lectured the binary method, "square and multiply", on 3^644 mod 645
    // (46:45 to 50:46: when Euler cannot be used because the base shares a factor with the modulus, or leaves a big
    // exponent; squaring with a reduction, the repeating remainders, the binary digits of the exponent), then linear
    // congruences, systems and the generalised Chinese theorem, and closed number theory (87:26). The binary method is its
    // own lesson after dml-tc-mocniny, dml-tc-mocnenie, built from that lecture; the step ids of dml-tc-mocniny stayed as
    // released. Since then the power generators also draw a base sharing a factor with the modulus (dml-tc-binary) and a
    // power left after Euler bigger than TcSmallPower, which the solutions compute by the binary method. Not lectured
    // anywhere yet: a power through a factorisation of the modulus and the Chinese theorem (Cvičení 6.25). Spěvák
    // (paralelka 2) announced the binary method for 25. 11. (8. prednáška, 1:35:29); not checked yet.
)
// LA1, checked 19. 11. 2026 11:00 against the captions: Klouda (paralelka 2, 8. prednáška, 18. 11.) finished kapitola 4
// (basis of S_0, two notations of one solution set, varieties, lines over Z_p, Věta 4.18, names, Věta 4.23 with its proof,
// Věta 4.26, Postup 4.30, parametric and non-parametric equations, the plane through three points); next: determinant.
// Kleprlík (1, 10. 11.) stopped before Věta 4.23, Krásenský (3, 13. 11.) after the proof of solvability in Věta 4.2.
// The date below moved to 24. 11. for Dombek's DML lecture; LA1 was not checked again after 19. 11.
val LecturesCheckedAt: LocalDateTime = LocalDateTime.parse("2026-11-24T10:00")

/** The end of the first lecture, in any parallel, that covers [code] (MI2, TČ3, S4; a family like MI matches MI1). */
fun Course.firstLectureEnd(code: String): LocalDateTime? = schedule.parallels.flatMap { p ->
    lectures(p.id).filter { l -> l.topics.any { it == code || it.startsWith(code) && code.none { c -> c.isDigit() } } }.map { lectureEnd(p, it) }
}.minOrNull()

/** Generator id -> the template name its exercises are recorded under ("gen:<template>"), computed once. */
val TemplateOf: Map<String, String> by lazy { Generators.mapValues { (_, g) -> g(Random(0)).template } }

/** A generator id from a progress key's id: the generator itself or its template name. */
fun genOf(id: String): String? = if (id in Generators) id else TemplateOf.entries.firstOrNull { it.value == id }?.key

/** The routes a lesson leads to: its definitions, theorems (and their proofs), proofs, generators and clips. */
fun lessonRefs(l: LessonDef): Set<String> = l.steps.flatMap { s ->
    when (s) {
        is Step.Def -> listOf("concept:${s.concept}")
        is Step.Thm -> listOfNotNull("theorem:${s.theorem}", (s.proof ?: TheoremById[s.theorem]?.proof?.id)?.let { "proof:$it" }) +
            s.also.flatMap { a -> listOfNotNull("theorem:$a", TheoremById[a]?.proof?.let { "proof:${it.id}" }) }
        is Step.Explain -> s.cards.map { dev.hamper.tabula.ui.cardId(it) }.map { if (it in ConceptById) "concept:$it" else "theorem:$it" }
        is Step.Prove -> listOf("proof:${s.proof}")
        is Step.Practice -> listOf("gen:${s.gen}")
        is Step.Clip -> listOf("clip:${s.clip}")
        else -> emptyList()
    }
}.toSet()

/**
 * A course as the app offers it: lessons not in [NotYetLectured] (or the [pending] set a test passes), and only the definitions, theorems, proofs,
 * templates, clips, checkpoints and papers they lead to. An item no lesson uses is offered once its whole topic is.
 * Ids of left-out items still resolve (progress, old routes), they are just not offered.
 */
class Shown(val course: Course, pending: Set<String> = NotYetLectured) {
    val lessons: List<LessonDef> = course.lessons.filter { it.id !in pending }
    private val openRefs = lessons.flatMap(::lessonRefs).toSet()
    private val allRefs = course.lessons.flatMap(::lessonRefs).toSet()
    private val closedTopics = course.topics.filter { t -> t.lessons.any { it !in lessons } }
    private fun inTopic(t: Topic, route: String): Boolean {
        val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
        return when (kind) {
            "concept" -> t.concepts.any { it.id == id }; "theorem" -> t.theorems.any { it.id == id }; "proof" -> t.proofs.any { it.id == id }
            "gen" -> id in t.generators; "clip" -> id.startsWith(t.id + "-"); else -> false
        }
    }
    /** Is this route (concept:x, gen:x, proof:x, theorem:x, clip:x, lesson:x) offered now? */
    fun shows(route: String): Boolean {
        val r = if (':' !in route) "gen:$route" else route
        if (r.startsWith("lesson:")) return lessons.any { it.id == r.substringAfter(':').substringBefore(':') }
        if (r in openRefs) return true
        if (r in allRefs) return false
        return closedTopics.none { inTopic(it, r) }
    }
    val concepts = course.concepts.filter { shows("concept:${it.id}") }
    val theorems = course.theorems.filter { shows("theorem:${it.id}") }
    val proofs = course.proofs.filter { shows("proof:${it.id}") }
    val templates = course.templates.filter { shows("gen:${it.gen}") }
    val clips get() = dev.hamper.tabula.screens.ClipLibrary.all.filter { it.course == course.id && shows("clip:${it.id}") }
    val checkpoints = course.checkpoints.filter { cp -> lessons.any { it.id == cp.after } && problemShown(cp.paper, cp.n) }
    fun problemShown(p: PaperProblem): Boolean = (p.checklist.mapNotNull { it.fix } + listOfNotNull(p.drill)).all { shows(it) }
    fun problemShown(paper: String, n: Int): Boolean = course.papers.firstOrNull { it.id == paper }?.problems?.firstOrNull { it.n == n }?.let(::problemShown) ?: false
    fun paperShown(p: TestPaper): Boolean = p.problems.all(::problemShown)
    /**
     * The paper as offered: whole when every problem is, else without the problems whose topic is still pending a
     * lecture (named in [TestPaper.missing], the screens say they come after it); null when nothing is left.
     */
    fun offered(p: TestPaper): TestPaper? {
        val (ok, out) = p.problems.partition(::problemShown)
        return when { out.isEmpty() -> p; ok.isEmpty() -> null; else -> p.copy(problems = ok, missing = out) }
    }
    /** Plan paper problems of the exam the app prepares for now, and offered. */
    val planPapers get() = course.papersFor(course.test).map { it.id }.toSet().let { ids ->
        course.planPapers.filter { r -> r.split(':').let { (_, id, n) -> id in ids && problemShown(id, n.toInt()) } }
    }
    /** Is a progress key or route offered now ("step:x", "gen:<generator or template>", "concept:x", ...)? */
    fun offersKey(key: String): Boolean {
        val (kind, id) = key.substringBefore(':') to key.substringAfter(':')
        return when (kind) {
            "step" -> lessons.any { l -> l.steps.any { it.id == id } } || course.lessons.none { l -> l.steps.any { it.id == id } }
            "gen", "template" -> genOf(id)?.let { shows("gen:$it") } ?: true
            "paper" -> id.substringBefore(':').let { p -> course.papers.none { it.id == p } || problemShown(p, id.substringAfter(':').toIntOrNull() ?: 1) }
            else -> shows(key)
        }
    }
    val testLessons get() = course.test.let { e -> lessons.filter { course.forExam(it, e) } }
    /** The first offered paper of the current exam, the shape of its mock test. */
    val mockPaper: TestPaper? get() = course.papersFor(course.test).firstNotNullOfOrNull(::offered) ?: course.generatedPaper(1).let(::offered)
    /** What a mock test of the current exam is as offered: problems, points and minutes of its first offered paper. */
    val mockShape: Triple<Int, Int, Int>
        get() {
            val e = course.test
            val p = mockPaper
            if (p == null || p.problems.isEmpty()) return Triple(3, e.points, e.minutes ?: 70)
            val pts = p.problems.sumOf { it.pts }; val all = e.minutes ?: 70
            return Triple(p.problems.size, pts, if (pts >= e.points) all else ((all * pts + e.points / 2) / e.points).coerceAtLeast(5))
        }
}


private val shownBy = dev.hamper.tabula.compat.ConcurrentHashMap<String, Shown>()

/* ---- the path: lessons, their checkpoints and the assessments, in the order a student meets them -------------------- */

sealed interface PathItem {
    data class Part(val name: String) : PathItem
    data class Lesson(val lesson: LessonDef) : PathItem
    data class Check(val checkpoint: Checkpoint) : PathItem
    /** An assessment, right after the last lesson of its scope (the topics it covers), never by list order. */
    data class Milestone(val exam: Exam) : PathItem
}

/**
 * The path of a course: offered lessons under part headings, a checkpoint after its lesson, and each exam the app
 * prepares for placed right after the last lesson it covers (`Course.forExam`). Lessons after a milestone lead to the
 * next one: LA1 kapitola 2, then "2. test", then kapitoly 3 and 4, then "Skúška".
 */
fun pathItems(course: Course, shown: Shown = course.shown()): List<PathItem> {
    val ls = shown.lessons
    val exams = course.exams.filter { it.prepares }
    // the last lesson of each exam's scope; an exam whose scope has no offered lesson goes at the end
    val after = exams.associateWith { e -> ls.indexOfLast { course.forExam(it, e) } }
    val out = ArrayList<PathItem>()
    var part = ""
    ls.forEachIndexed { i, l ->
        val p = course.partName(l)
        if (p != part) { part = p; out += PathItem.Part(p) }
        out += PathItem.Lesson(l)
        shown.checkpoints.firstOrNull { it.after == l.id }?.let { out += PathItem.Check(it) }
        exams.filter { after[it] == i }.forEach { out += PathItem.Milestone(it) }
    }
    exams.filter { after[it] == -1 }.forEach { out += PathItem.Milestone(it) }
    return out
}

/* ---- lookups across courses ------------------------------------------------------------------------------------- */

val AllLessons: List<LessonDef> by lazy { Courses.flatMap { it.lessons } }
val LessonById: Map<String, LessonDef> by lazy { AllLessons.associateBy { it.id } }
val ConceptById: Map<String, Concept> by lazy { (La1Concepts + La1Topics.flatMap { it.concepts } + DmlConcepts).associateBy { it.id } }
val ProofById: Map<String, Proof> by lazy { (La1Proofs + La1Topics.flatMap { it.proofs } + DmlProofs).associateBy { it.id } }
val TheoremById: Map<String, Theorem> by lazy { (La1Theorems + La1Topics.flatMap { it.theorems } + DmlTheorems).associateBy { it.id } }
val Generators: Map<String, (Random) -> Exercise> by lazy { La1Generators + La1Topics.fold(emptyMap()) { a, t -> a + t.generators } + DmlGenerators }

/* ---- where the course is ------------------------------------------------------------------------------------------- */

/** A lecture is over 100 minutes after it starts (90 min and a margin). */
fun lectureEnd(p: Parallel, l: Lecture): LocalDateTime = l.date.atTime(p.time).plusMinutes(100)

data class Where(val done: List<Lecture>, val next: Lecture?, val later: List<Lecture>)

/** The sittings the student picked, per exam id (the Store sets this at start; tests leave it empty). */
object Sittings { @Volatile var picked: (String) -> LocalDateTime? = { null } }

/** A sitting picked by date only (no KOS term, "Iný dátum") is stored at midnight: its time is unknown. */
fun hasTime(t: LocalDateTime) = t.toLocalTime() != LocalTime.MIDNIGHT

/** " o 18:00", or nothing when the time is unknown. */
fun atTime(t: LocalDateTime) = if (hasTime(t)) " o\u00A0${t.toLocalTime()}" else ""

/** "dnes", "zajtra", "o 3 dni". */
fun inDays(d: Int) = when (d) { 0 -> "dnes"; 1 -> "zajtra"; else -> "o $d ${dayWord(d)}" }

/** When an exam is over: the end of its last term, else the end of its window. */
fun examOver(e: Exam): LocalDateTime =
    e.terms.maxOfOrNull { it.start.plusMinutes(it.minutes.toLong()) } ?: e.to?.atTime(23, 59) ?: e.from?.atTime(23, 59) ?: LocalDateTime.MAX

fun where(course: Course, parallel: String, now: LocalDateTime): Where {
    val p = course.parallel(parallel)
    val ls = course.lectures(p.id)
    val done = ls.filter { lectureEnd(p, it) <= now }
    val rest = ls.filter { lectureEnd(p, it) > now }
    return Where(done, rest.firstOrNull(), rest.drop(1))
}

/** When an exam happens for this student: the picked time, else the first term not over, else the last, else its window. */
fun examStart(e: Exam, picked: LocalDateTime?, now: LocalDateTime): LocalDateTime? =
    picked ?: e.terms.firstOrNull { it.start.plusMinutes(it.minutes.toLong()) > now }?.start ?: e.terms.lastOrNull()?.start ?: e.from?.atStartOfDay()

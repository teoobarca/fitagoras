package dev.hamper.tabula.data

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.dml.DmlTest2Topics
import dev.hamper.tabula.data.la1.La1Test2Topics
import dev.hamper.tabula.platform.Assets
import dev.hamper.tabula.state.Store

/**
 * The page about a course's test ("Čo býva na 2. teste / písomke"), drawn from files/test/<exam id>.json: the analysis
 * of the past papers found (test-la1-2 and test-dml-2 in the course sources outside the repository, ROZBOR.md and test2.json), with the
 * lecturers' quotes and their sources. Only what the analysis states with a source; what it marks as derived or
 * unconfirmed is left out of the asset. An exam without an asset has no page.
 */
class TestPageData(
    val title: String, val sub: String, val since: Int,
    val points: List<Int>, val minutes: Int, val tests: Int,
    val facts: List<String>, val sources: List<String>, val notes: List<String>,
    val positions: List<Position>, val definitions: List<Seen>, val proofs: List<Seen>,
    val pointsLost: List<Loss>, val evening: List<Step>, val names: Map<String, String>,
    /** Whether the points of each problem are confirmed for this year; if not, the page gives only the total. */
    val pointsSure: Boolean = true,
) {
    /**
     * A kind of task at a position: how many of the tests in today's format had it, and where the app practises it.
     * [at]: the problems a definition or proof was asked in (empty: not known, shown under every problem that asks one).
     */
    data class Seen(val label: String, val count: Int, val of: Int, val routes: List<String>, val where: List<String> = emptyList(), val at: Set<Int> = emptySet())

    /**
     * One problem of the paper. [title]: what it is about when the papers keep it to one topic (DML), else null.
     * [definitions], [proofs], [other]: the summary rows of a problem that asks definitions or proofs (each kind of its
     * other tasks came once, so they are one row), with how many tests had them; null when the problem has none.
     */
    data class Position(
        val n: Int, val points: Int, val tests: Int, val kinds: List<Seen>, val title: String? = null,
        val definitions: Row? = null, val proofs: Row? = null, val other: Row? = null,
        /** What the problem is, in a sentence, from the kinds the papers had there ("Pre ktoré $a$ je súbor LN, ..."). */
        val about: String? = null,
    )

    /** A summary row: [row] on the folded card, [group] over its list when unfolded ("{n}", "{of}" filled in), [count] tests of [of]. */
    data class Row(val row: String, val group: String, val count: Int, val of: Int) {
        val groupText get() = group.replace("{n}", "$count").replace("{of}", "$of")
    }
    data class Loss(val what: String, val quote: String, val quoteSrc: String, val source: String, val routes: List<String>)
    data class Step(val n: Int, val what: String, val why: String, val routes: List<String>)

    /** A test as the page names it: the analysis's own name, else the LA1 variant animal and year ("sova 2025"). */
    fun testName(id: String): String = names[id] ?: animalName(id)
}

private fun JSONArray?.strings() = if (this == null) emptyList() else (0 until length()).map { getString(it) }
private fun JSONArray?.objects() = if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

private val pages = HashMap<String, TestPageData?>()

/** The page of an exam (files/test/<exam id>.json), or null when the exam has none. */
fun testPage(examId: String): TestPageData? =
    // an exam without a page is remembered as null too (getOrPut would read the missing asset again on every redraw)
    if (examId in pages) pages[examId] else runCatching { parseTestPage(Assets.read("test/$examId.json")) }.getOrNull().also { pages[examId] = it }

/** The analysis writes "\\mathbb R^3"; the engine reads "\\mathbb{R}^3". */
private fun tex(s: String) = s.replace(Regex("""\\mathbb ([A-Z])"""), "\\\\mathbb{$1}")

fun parseTestPage(json: String): TestPageData {
    val o = JSONObject(json)
    val y = o.getJSONObject("this_year")
    val f = o.getJSONObject("format_now")
    val page = o.optJSONObject("page")
    val kinds = o.getJSONObject("kinds")
    val since = f.getInt("since")
    // the tests in today's format (LA1 since 2023: three problems, 7 + 12 + 11; DML since 2023: induction, sets and maps, numbers)
    val tests = o.optJSONArray("tests").objects().filter { it.optString("format").startsWith("$since") }
    // each part with its test and the problem it is in ("pos" in the LA1 analyses, "n" in DML's)
    val placed = tests.flatMap { t -> t.optJSONArray("problems").objects().flatMap { p -> p.optJSONArray("parts").objects().map { Triple(t, it, p.optInt("pos", p.optInt("n"))) } } }
    val parts = placed.map { it.first to it.second }
    // a proof part: asked as a proof, or a question whose answer the app has as a proof ("what does every subspace contain? prove it")
    fun isProof(part: JSONObject) = part.optString("kind") == "proof" || part.optJSONArray("app").strings().firstOrNull()?.startsWith("proof:") == true
    // where the app practises a kind: the links of its first part in the tests
    fun appOf(kind: String): List<String> = parts.firstOrNull { it.second.optString("kind") == kind }?.second?.optJSONArray("app").strings()
    val proofKinds = parts.filter { isProof(it.second) }.map { it.second.optString("kind") }.toSet()
    val positions = o.getJSONArray("positions").objects().map { pos ->
        val seen = pos.getJSONArray("seen").objects()
        val seenKinds = seen.map { it.getJSONArray("kinds").strings() }
        val ks = seenKinds.flatten().distinct().filter { it != "definition" && it !in proofKinds }
        fun row(key: String, count: Int) = pos.optJSONObject(key)?.let { r -> TestPageData.Row(r.getString("row"), r.getString("group"), count, seen.size) }
        val defMin = pos.optJSONObject("definitions")?.optInt("min", 1) ?: 1
        TestPageData.Position(
            pos.getInt("pos"), pos.getInt("points"), seen.size,
            ks.map { k -> TestPageData.Seen(tex(kinds.optString(k, k)), seenKinds.count { k in it }, seen.size, appOf(k)) }.sortedByDescending { it.count },
            title = pos.optString("title").ifBlank { null }?.let(::tex),
            about = pos.optString("about").ifBlank { null }?.let(::tex),
            definitions = row("definitions", seenKinds.count { s -> s.count { it == "definition" } >= defMin }),
            proofs = row("proofs", seenKinds.count { s -> s.any { it == "proof" || it in proofKinds } }),
            other = row("other", seenKinds.count { s -> s.any { it in ks } }),
        )
    }
    val defs = o.getJSONArray("definitions_by_concept_2023_2025").objects().map { d ->
        val route = d.getString("app")
        TestPageData.Seen(route, d.getInt("count"), tests.size, listOf(route), d.optJSONArray("tests").strings(), d.optJSONArray("pos").let { a -> if (a == null) emptySet() else (0 until a.length()).map { a.getInt(it) }.toSet() })
    }
    // the proofs asked, each with the test it was in
    val proofs = placed.filter { isProof(it.second) }.mapNotNull { (t, part, n) ->
        part.optJSONArray("app").strings().firstOrNull { it.startsWith("proof:") }?.let { Triple(it, t.getString("id"), n) }
    }.groupBy { it.first }.map { (r, xs) -> xs.map { it.second }.distinct().let { ts -> TestPageData.Seen(r, ts.size, tests.size, listOf(r), ts, xs.map { it.third }.filter { it > 0 }.toSet()) } }
    val lost = o.getJSONArray("points_lost").objects().map { l ->
        TestPageData.Loss(l.getString("what"), l.optString("quote"), l.optString("quote_src"), l.getString("source"), l.optJSONArray("app").strings())
    }
    val evening = o.getJSONArray("one_evening").objects().map { s -> TestPageData.Step(s.getInt("step"), tex(s.getString("what")), tex(s.getString("why")), s.optJSONArray("app").strings()) }
    val facts = listOfNotNull(y.optString("credit_rule").ifBlank { null }, y.optString("quiz_deadline").ifBlank { null })
    val points = f.optJSONArray("points").let { a -> if (a == null) emptyList() else (0 until a.length()).map { a.getInt(it) } }
    val names = o.optJSONArray("tests").objects().mapNotNull { t -> t.optString("name").ifBlank { null }?.let { t.getString("id") to it } }.toMap()
    return TestPageData(
        page?.optString("title")?.ifBlank { null } ?: "Čo býva na 2. teste",
        page?.optString("sub")?.ifBlank { null } ?: "",
        since, points, y.getInt("minutes"), tests.size, facts, y.optJSONArray("sources").strings(), f.optJSONArray("notes").strings(),
        positions, defs, proofs, lost, evening, names, f.optBoolean("points_confirmed", true),
    )
}

/* ---- The course page's topics of a test ----------------------------------------------------------------------------- */

/**
 * One line of what the course page says a test asks (courses.fit.cvut.cz, classification.adoc): [text] is the course's
 * sentence word for word, [skills] the skills of the coverage register (coverage/skills.json) that practise it; their
 * links say where in the app it is and the student's record says how far they are. [extra]: routes the register has no
 * skill for (a theorem, its proof).
 */
data class TestTopic(val group: String, val text: String, val skills: List<String>, val extra: List<String> = emptyList())

/** The topics of one exam and where on the course page they are listed ([source], shown under the count). */
class TestTopics(val source: String, val topics: List<TestTopic>)

/** The exams whose course page lists what they ask. */
val TestTopicsByExam: Map<String, TestTopics> by lazy {
    mapOf(
        "la1-test2" to TestTopics("Stránka predmetu, Zápočet a zkouška: „Co bude v druhé zápočtové písemce?“, slovo po slove.", La1Test2Topics),
        "dml-pisomka2" to TestTopics("Stránka predmetu, Hodnocení, 2. zápočtovka: „Konkrétně musíte:“, slovo po slove.", DmlTest2Topics),
    )
}

/** How far the student is with a topic: everything it asks done without help, something tried, or nothing yet. */
enum class TopicState(val label: String) { Done("zvládnuté bez pomoci"), Started("rozrobené"), New("nezačaté") }

private val skillById: Map<String, Skill> by lazy { runCatching { CoverageRegister.load().skills.associateBy { it.id } }.getOrDefault(emptyMap()) }

/** The lesson a step is in. */
private fun lessonOfStep(stepId: String): LessonDef? = AllLessons.firstOrNull { l -> l.steps.any { it.id == stepId } }

/** Where a topic is practised and explained: the routes of its skills' links and its own, those the course offers, in order. */
fun topicRoutes(t: TestTopic, course: Course): List<String> {
    val shown = course.shown()
    val refs = t.skills.flatMap { id -> skillById[id]?.links.orEmpty().map { it.ref } } + t.extra
    return refs.filter { !it.startsWith("paper:") }.distinct().filter { shown.offersKey(it) }
}

/** The progress keys behind a topic's routes: what the student answers (a definition, a proof, a problem type, a lesson question). */
private fun gradedKey(route: String): String? {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
    return when (kind) {
        "concept", "proof", "step" -> route
        "gen" -> "gen:" + (TemplateOf[id] ?: id)
        else -> null
    }
}

fun topicState(t: TestTopic, store: Store, course: Course = store.course): TopicState {
    val routes = topicRoutes(t, course)
    val keys = routes.mapNotNull(::gradedKey).distinct()
    // what counts: the definitions, proofs and problem types; a topic with none of those, its lesson questions
    val core = keys.filter { !it.startsWith("step:") }.ifEmpty { keys }
    fun done(k: String) = if (k.startsWith("step:")) store.history(k).lastOrNull()?.let { it.ok && !it.aided } == true else store.mastered(k)
    if (core.isNotEmpty() && core.all(::done)) return TopicState.Done
    val lessons = routes.mapNotNull { r -> if (r.startsWith("step:")) lessonOfStep(r.substringAfter(':'))?.id else null }
    val begun = keys.any { store.tried(it) } || lessons.any { it in store.lessonsDone || (store.lessonAt[it] ?: 0) > 0 }
    return if (begun) TopicState.Started else TopicState.New
}

/** A route of a topic as a tappable label: the lesson of a lesson question, a definition, a theorem, a proof, a problem type. */
fun topicLink(route: String, course: Course): Pair<String, String>? {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
    return when (kind) {
        "step" -> lessonOfStep(id)?.let { "Lekcia: ${it.name}" to "lesson:${it.id}" }
        "concept" -> ConceptById[id]?.let { "Definícia: ${it.name}" to route }
        "theorem" -> TheoremById[id]?.let { "${it.src.removePrefix("Skriptá, ")}: ${it.name}" to route }
        "proof" -> ProofById[id]?.let { "Dôkaz: ${it.title}" to route }
        // a generator without a card of its own (a variant of a template, "count" of "count-all") goes by its template's card
        "gen" -> ((course.templates.firstOrNull { it.gen == id } ?: TemplateOf[id]?.let { t -> course.templates.firstOrNull { TemplateOf[it.gen] == t } })?.title ?: Generators[id]?.let { g -> runCatching { g(kotlin.random.Random(0)).label }.getOrNull() })
            ?.let { "Príklady: $it" to route } ?: ("Príklady" to route)
        else -> null
    }
}

/* ---- Routes of the page ------------------------------------------------------------------------------------------ */

/** How far the student is with one route of the app: null for a theorem or anything not practised by itself. */
fun routeState(route: String, store: Store): TopicState? {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
    return when (kind) {
        "concept", "proof", "gen" -> {
            val key = if (kind == "gen") "gen:" + (TemplateOf[id] ?: id) else route
            when { store.mastered(key) -> TopicState.Done; store.tried(key) -> TopicState.Started; else -> TopicState.New }
        }
        "lesson" -> when { id in store.lessonsDone -> TopicState.Done; (store.lessonAt[id] ?: 0) > 0 -> TopicState.Started; else -> TopicState.New }
        // a written paper says its points instead (mockNote): one sitting is not "done without help"
        "mock" -> if (store.mocks.any { it.paper == id }) null else TopicState.New
        else -> null
    }
}

/** A route as a short label for the page: a definition, a proof, a problem type, a lesson, a test paper. */
fun routeLabel(route: String, course: Course): String? {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
    return when (kind) {
        "paper" -> course.papers.firstOrNull { it.id == id.substringBefore(':') }?.name?.let { n -> id.substringAfter(':', "").toIntOrNull()?.let { "$n, $it.\u00A0príklad" } ?: n }
        "mock" -> course.papers.firstOrNull { it.id == id }?.let { (if (course.test.word == PisomkaWord) "Celá písomka: " else "Celý test: ") + it.name }
        "lesson" -> LessonById[id]?.let { "Lekcia: ${it.name}" }
        else -> topicLink(route, course)?.first
    }
}

/** "sova-2025" → "Sova 2025": an LA1 test by its variant (the animal on its sheets, named on the page's first line) and year. */
private fun animalName(id: String): String {
    val y = id.substringAfterLast('-')
    val a = id.substringBeforeLast('-')
    return "${(TestAnimals[a] ?: a).replaceFirstChar { it.uppercase() }} $y"
}
private val TestAnimals = mapOf("medved" to "medvěd", "mys" to "myš", "vcelka" to "včelka", "kocka" to "kočka")

/** "napísané: 21 z 30 b": the last sitting of a whole paper, or null when it was not written yet. */
fun mockNote(route: String, store: Store): String? =
    if (!route.startsWith("mock:")) null else store.mocks.lastOrNull { it.paper == route.substringAfter(':') }?.let { "napísané: ${it.total} z ${it.max} b" }

package dev.hamper.tabula.data

import dev.hamper.tabula.platform.Assets

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.LocalDateTime
import kotlin.random.Random
import dev.hamper.tabula.compat.time.*

/*
 * Source coverage (docs/CONTENT.md, "Coverage"). Lessons and exercises are outputs of a system; this is its input side.
 *
 * assets/coverage/sources.json: every known source question (tests, exams, rozstřel, MARAST problems, textbook
 * questions and exercises, lecture and proseminar examples), split into sub-questions; each sub-question names the skill it needs,
 * that is what the student must do (define, compute, count, construct, enumerate, prove, decide statements, ...).
 * assets/coverage/skills.json: each skill with its links to app content (concept, explanation, practice, test).
 *
 * The state of a skill is computed from what its links really are in the app, never declared: a link must resolve to
 * existing content of the same course, that content must ask what the link claims ([Link.asks] is found in its text),
 * and only some kinds of content count as independent practice for a capability (a definition is practised by writing
 * it from memory, a count by a generator or a paper problem, statements by a statements answer). Similar names never
 * count. CoverageTest fails on a broken link and on a sub-question in scope that is not practised independently and
 * has no owned, dated exception.
 */

enum class Capability { DEFINE, STATE, COMPUTE, COUNT, DECIDE, CONSTRUCT, ENUMERATE, PROVE, STATEMENTS, MODEL, FIX }

enum class Transcription { VERBATIM, UNCERTAIN, SUMMARY, ID_ONLY }

/**
 * The kinds of sources ([SourceQuestion.kind]) and whether the source is a recording: a lecture, or a proseminar (the
 * course's pre-recorded video on YouTube). A recording's example is found by its time, so its id ends with the second it
 * starts, its [SourceQuestion.ref] names that second (a proseminar's link opens it there, `&t=479s`; a lecture recording,
 * which has no public link, is cited with its id and `t=479 s`) and its anchor names the span (`07:59–12:52`). A misspelt kind would fall out of every count, so an unknown one is an error.
 */
val SourceKinds: Map<String, Boolean> = mapOf(
    "test" to false, "exam" to false, "rozstrel" to false, "quiz" to false, "marast" to false, "textbook" to false, "blog" to false,
    "lecture" to true, "proseminar" to true,
)

/** One graded sub-question of a source. [skill] null: its capability cannot be told from the source. */
data class SourcePart(
    val id: String, val text: String, val skill: String?, val kind: Capability?, val domain: String, val format: String,
    val uncertain: String? = null,
)

data class SourceQuestion(
    val id: String, val course: String, val kind: String, val title: String, val ref: String, val file: String,
    val anchor: String, val transcription: Transcription, val assessment: String, val chapter: String,
    val note: String? = null, val parts: List<SourcePart>,
    /** For a source in scope known only by its id: who collects its text and by when. */
    val exception: CoverageException? = null,
)

/**
 * A link to app content: "gen:<generator>", "paper:<paper>:<n>", "proof:<id>", "concept:<id>", "theorem:<id>",
 * "step:<step id>", "lesson:<id>". [asks]: words the content's text must contain (case and math markup ignored), so a
 * link says what it covers and the test checks the content really asks it. [max]: the most this link may count for,
 * when its content asks only part of the skill (a generator that hands over the matrix the source makes you build).
 */
data class Link(val ref: String, val asks: String, val max: CoverageState? = null)

/**
 * Why a skill in scope is not practised yet: who builds it ([owner], a topic or person), recorded when, due when.
 * [open]: how many of its sub-questions were open when recorded; one more open (a new source, a lost practice) fails,
 * so an exception never hides a later regression.
 */
data class CoverageException(val owner: String, val recorded: String, val due: String, val reason: String, val open: Int? = null)

data class Skill(
    val id: String, val course: String, val lecture: String, val kind: Capability, val domain: String, val title: String,
    val links: List<Link> = emptyList(),
    /** Unit tests ("ClassName#method", class in dev.hamper.tabula...) that check the math of the practice. */
    val checks: List<String> = emptyList(),
    val exception: CoverageException? = null,
    val note: String? = null,
    /**
     * Words any practice of this skill must contain (one of them), fixed once per skill close to the sources' wording,
     * so a link cannot claim it with a phrase of its own choosing ("kolik podprostorů", not "kolik prvků má podprostor").
     */
    val signature: List<String> = emptyList(),
)

/** From nothing to practised; [order] ranks how far a skill got. */
enum class CoverageState(val label: String, val order: Int) {
    NOT_COLLECTED("zadanie nezískané", -3),
    UNCERTAIN("neistý prepis, výkon neznámy", -2),
    OUT_OF_SCOPE("mimo rozsahu", -1),
    PENDING_LECTURE("čaká na prednášku", -1),
    MISSING("chýba", 0),
    OTHER_FIELD("precvičené len nad iným telesom (ℝ a ℤₚ)", 1),
    HIDDEN("v appke skryté, hoci odprednášané", 1),
    EXPLAINED("iba vysvetlené", 2),
    GUIDED("iba vedené precvičenie", 3),
    PRACTISED("samostatne precvičené", 4),
}

class CoverageRegister(val sources: List<SourceQuestion>, val skills: List<Skill>) {
    companion object {
        fun parse(sourcesJson: String, skillsJson: String) = CoverageRegister(parseSources(sourcesJson), parseSkills(skillsJson))
        fun load() = parse(Assets.read("coverage/sources.json"), Assets.read("coverage/skills.json"))

        private fun JSONObject.str(k: String) = optString(k, "")
        private fun JSONObject.strOrNull(k: String) = if (has(k) && !isNull(k)) getString(k).ifBlank { null } else null
        private fun <T> JSONArray?.list(f: (JSONObject) -> T): List<T> = if (this == null) emptyList() else (0 until length()).map { f(getJSONObject(it)) }
        private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else (0 until length()).map { getString(it) }
        private fun capability(s: String?): Capability? = s?.takeIf { it.isNotBlank() }?.let { Capability.valueOf(it.uppercase()) }

        private fun exception(o: JSONObject) = CoverageException(o.str("owner"), o.str("recorded"), o.str("due"), o.str("reason"), if (o.has("open")) o.getInt("open") else null)

        fun parseSources(json: String): List<SourceQuestion> = JSONArray(json).list { o ->
            SourceQuestion(
                o.getString("id"), o.getString("course"), o.getString("kind"), o.str("title"), o.str("ref"), o.str("file"),
                o.str("anchor"), Transcription.valueOf(o.getString("transcription").uppercase().replace('-', '_')),
                o.str("assessment"), o.str("chapter"), o.strOrNull("note"),
                o.optJSONArray("parts").list { p ->
                    SourcePart(p.getString("id"), p.str("text"), p.strOrNull("skill"), capability(p.strOrNull("kind")), p.str("domain"), p.str("format"), p.strOrNull("uncertain"))
                },
                o.optJSONObject("exception")?.let(::exception),
            )
        }

        fun parseSkills(json: String): List<Skill> = JSONArray(json).list { o ->
            Skill(
                o.getString("id"), o.getString("course"), o.getString("lecture"), capability(o.getString("kind"))!!, o.str("domain"), o.str("title"),
                o.optJSONArray("links").list { Link(it.getString("ref"), it.str("asks"), it.strOrNull("max")?.let { m -> CoverageState.valueOf(m.uppercase()) }) },
                o.optJSONArray("checks").strings(),
                o.optJSONObject("exception")?.let(::exception),
                o.strOrNull("note"),
                o.optJSONArray("signature").strings(),
            )
        }
    }
}

/** What a link resolves to in the app. [level]: how far it takes the student for the skill's capability. */
data class Resolved(val course: String, val text: String, val level: CoverageState, val offered: Boolean, val why: String, val fields: Set<String> = emptySet())

/** "Zp" for a finite field, "R" for the reals, in a source's domain ("ℤ₅³", "ℝ⁴") or a content's TeX; null when neither. */
fun fieldsOf(text: String): Set<String> = buildSet {
    if (Regex("ℤ[ₚ₀-₉]|\\\\mathbb\\{Z\\}_|ℤ_|Z_p|Z_[0-9]").containsMatchIn(text)) add("Zp")
    if (Regex("ℝ|\\\\mathbb\\{R\\}|R\\^[0-9n]").containsMatchIn(text)) add("R")
}

data class SkillResult(val skill: Skill, val state: CoverageState, val lectured: Boolean, val inScope: Boolean, val best: List<Pair<Link, Resolved>>)

data class PartResult(val source: SourceQuestion, val part: SourcePart?, val state: CoverageState, val skill: Skill?)

class CoverageReport(val errors: List<String>, val skills: Map<String, SkillResult>, val parts: List<PartResult>, val scope: Map<String, Set<String>>) {
    /** Skills in scope, lectured, with a sub-question not practised independently (also one practised only over another field). */
    val gaps: List<SkillResult> get() = skills.values.filter { r -> r.inScope && r.lectured && partsOf(r.skill.id).any { it.state != CoverageState.PRACTISED } }
    /** The state a gap line shows: the skill's, or that its practice is over the other field. */
    fun gapState(r: SkillResult): CoverageState = if (r.state == CoverageState.PRACTISED) CoverageState.OTHER_FIELD else r.state
    val unexcused: List<SkillResult> get() = gaps.filter { it.skill.exception == null }
    fun partsOf(skill: String) = parts.filter { it.skill?.id == skill }
}

/**
 * Computes the coverage of the register against the app's content. [scope]: per course, the lecture codes the current
 * tests need (a code without digits covers its family: "MI" covers MI1..MI3). [checkedAt]: what has been lectured by then.
 */
class CoverageCheck(
    private val scope: Map<String, Set<String>> = CoverageScope,
    private val checkedAt: LocalDateTime = LecturesCheckedAt,
    /** An exception past its due date is an error: the gap was promised and is still open. */
    private val today: LocalDate = LocalDate.now(),
) {
    private val courses = Courses.associateBy { it.id }
    private val steps: Map<String, Pair<Course, Pair<LessonDef, Step>>> by lazy {
        Courses.flatMap { c -> c.lessons.flatMap { l -> l.steps.map { s -> s.id to (c to (l to s)) } } }.toMap()
    }

    private fun norm(s: String) = s.lowercase().replace("==", "").replace(Regex("[§$*]|\\\\[a-zA-Z]+|[{}\\\\]"), " ").replace(Regex("\\s+"), " ").trim()

    /**
     * One graded part of a generated exercise: the exercise's prompt, the part's question, how many parts the exercise
     * has, the answer kind and the right answer (to tell a question whose answer never changes).
     */
    private class Graded(val prompt: String, val q: String, val parts: Int, val kind: String, val key: String)
    private val genSamples = HashMap<String, List<Graded>>()
    private fun sample(gen: String, make: (Random) -> Exercise): List<Graded> = genSamples.getOrPut(gen) {
        val out = ArrayList<Graded>()
        repeat(60) { seed ->
            val ex = make(Random(seed * 7919 + 13))
            val answers = ArrayList<Any>()
            for (build in ex.parts) {
                val part = build(answers)
                val a = part.answer
                val key = when (a) {
                    is Answer.Choice -> { answers.add(a.correct); a.options[a.correct] }
                    is Answer.Numbers -> { answers.add(a.expected); a.expected.joinToString() }
                    is Answer.Pick -> {
                        val n = a.options.size
                        val pick = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.firstOrNull { it.size in a.count && a.ok(it) } ?: emptySet<Int>()
                        answers.add(pick); pick.joinToString { a.options[it] }
                    }
                    is Answer.Vectors -> { answers.add(a.example); a.example.joinToString() }
                    is Answer.Paper -> { answers.add(1); part.q + seed } // written on paper: what it asks changes with the numbers
                    else -> { answers.add(Unit); part.q + seed } // statements: one key per seed, never "always the same"
                }
                // a list whose length the student decides is an enumeration; a fixed number of vectors is a construction
                val kind = if (a is Answer.Vectors && a.open) "Enumerate" else a::class.simpleName ?: "?"
                out += Graded(ex.prompt, part.q, ex.parts.size, kind, key)
            }
        }
        out
    }

    private fun lines(ls: List<Line>) = ls.joinToString(" ") { when (it) { is Para -> it.tex; is Item -> it.tex; is Block -> it.tex } }

    private fun stepText(s: Step): String = when (s) {
        // the formula under a question (sub) is part of what it asks
        is Step.Ask -> s.q + " " + (s.sub ?: "") + " " + s.options.joinToString(" ") + " " + s.after
        is Step.Explore -> s.title + " " + s.sub + " " + s.doneTitle + " " + (s.doneBody ?: "")
        is Step.Explain -> s.title + " " + s.body + " " + (s.formal?.let(::lines) ?: "") + " " + (s.more ?: "")
        is Step.Worked -> s.title + " " + s.problem + " " + s.solution.steps.joinToString(" ") { st -> st.say + " " + st.math.joinToString(" ") { it.tex + " " + (it.note ?: "") } }
        is Step.Check -> s.q + " " + (s.sub ?: "") + " " + s.options.joinToString(" ") + " " + s.why
        is Step.Practice -> s.title ?: ""
        is Step.Clip -> s.clip
        is Step.Def -> ConceptById[s.concept]?.let { it.name + " " + lines(it.czech) } ?: ""
        is Step.Thm -> TheoremById[s.theorem]?.let { it.name + " " + lines(it.czech) } ?: ""
        is Step.Prove -> ProofById[s.proof]?.let { it.title + " " + it.task } ?: ""
    }

    private fun courseOfGen(gen: String) = Courses.firstOrNull { gen in it.gens }

    /** Is a generator offered as practice without help: a template of the practice tab or a lesson's last problem? */
    private fun soloGen(c: Course, gen: String) = c.templates.any { it.gen == gen } || c.lessons.any { l -> l.steps.any { it is Step.Practice && it.gen == gen && it.solo } }

    /** Resolves a link for a capability, or says why it does not resolve. */
    fun resolve(link: Link, kind: Capability): Result<Resolved> {
        val kindOf = link.ref.substringBefore(':')
        val id = link.ref.substringAfter(':')
        fun fail(msg: String) = Result.failure<Resolved>(IllegalArgumentException(msg))
        return when (kindOf) {
            "gen" -> {
                val make = Generators[id] ?: return fail("generátor '$id' neexistuje")
                val c = courseOfGen(id) ?: return fail("generátor '$id' nepatrí žiadnemu kurzu (nie je v šablónach ani v lekcii)")
                val all = sample(id, make)
                val asks = norm(link.asks)
                // What a part grades: its own question, or the prompt when the exercise is a single question. A
                // sub-question in a multi-part prompt that no part asks is not practised (the prompt copies the task).
                // A part that is only a formula ("$\\dim M=$", "b) $x\\bmod 7$:") is graded against the prompt's question.
                fun bare(q: String) = Regex("\\p{L}{2,}").findAll(q.replace(Regex("\\$[^$]*\\$"), " ")).count() < 3
                val graded = all.filter { asks in norm(it.q) || ((it.parts == 1 || bare(it.q)) && asks in norm(it.prompt)) }
                if (graded.isEmpty()) return fail(
                    if (all.any { asks in norm(it.prompt) }) "generátor '$id' má „${link.asks}“ len v zadaní, žiadna časť to nehodnotí"
                    else "generátor '$id' nepýta „${link.asks}“ (podobný názov nie je pokrytie)")
                val kinds = graded.map { it.kind }.toSet()
                val solo = soloGen(c, id)
                val level = when {
                    !solo -> CoverageState.GUIDED
                    // a reasoning written on paper and compared with its model, as a proof against its checklist
                    kind == Capability.PROVE && "Paper" in kinds -> CoverageState.PRACTISED
                    kind == Capability.DEFINE || kind == Capability.STATE || kind == Capability.PROVE -> CoverageState.GUIDED
                    // Statements and enumerations need the right answer kind in the very part that asks it.
                    kind == Capability.STATEMENTS && "Statements" !in kinds -> CoverageState.GUIDED
                    kind == Capability.ENUMERATE && kinds.none { it in EnumerateAnswers } -> CoverageState.GUIDED
                    // A choice whose right answer is always the same is learned, not practised.
                    kinds == setOf("Choice") && graded.map { it.key }.toSet().size == 1 -> CoverageState.GUIDED
                    else -> CoverageState.PRACTISED
                }
                val text = norm(graded.joinToString(" ") { it.prompt + " " + it.q })
                Result.success(Resolved(c.id, text, level, c.shown().shows("gen:$id"), "generátor, odpovede ${kinds.sorted()}", graded.flatMap { fieldsOf(it.prompt + " " + it.q) }.toSet()))
            }
            "paper" -> {
                val (paper, n) = id.split(':').let { it[0] to (it.getOrNull(1)?.toIntOrNull() ?: return fail("odkaz na písomku bez čísla príkladu: ${link.ref}")) }
                val c = Courses.firstOrNull { cc -> cc.papers.any { it.id == paper } } ?: return fail("písomka '$paper' neexistuje")
                val p = c.papers.first { it.id == paper }.problems.firstOrNull { it.n == n } ?: return fail("písomka '$paper' nemá príklad $n")
                // Written on paper and checked against its rubric; without a rubric nothing checks the attempt.
                val level = if (kind == Capability.STATEMENTS || p.checklist.isEmpty()) CoverageState.GUIDED else CoverageState.PRACTISED
                Result.success(Resolved(c.id, norm(p.title + " " + p.text), level, c.shown().problemShown(paper, n), "príklad písomky, písomne podľa bodovania", fieldsOf(p.text)))
            }
            "proof" -> {
                val p = ProofById[id] ?: return fail("dôkaz '$id' neexistuje")
                val c = Courses.firstOrNull { cc -> cc.proofs.any { it.id == id } } ?: return fail("dôkaz '$id' nepatrí kurzu")
                // A proof is built line by line, then written whole on paper against its checklist.
                val level = if (kind == Capability.PROVE && p.checklist.isNotEmpty()) CoverageState.PRACTISED else CoverageState.GUIDED
                Result.success(Resolved(c.id, norm(p.title + " " + p.task + " " + p.given), level, c.shown().shows("proof:$id"), "dôkaz po riadkoch a celý na papier"))
            }
            "concept" -> {
                val k = ConceptById[id] ?: return fail("definícia '$id' neexistuje")
                val c = Courses.firstOrNull { cc -> cc.concepts.any { it.id == id } } ?: return fail("definícia '$id' nepatrí kurzu")
                // Written from memory and checked unit by unit: practice without help for a definition only.
                val level = if (kind == Capability.DEFINE && k.units.isNotEmpty()) CoverageState.PRACTISED else CoverageState.EXPLAINED
                Result.success(Resolved(c.id, norm(k.name + " " + lines(k.czech) + " " + k.ask), level, c.shown().shows("concept:$id"), "definícia, z pamäti"))
            }
            "theorem" -> {
                val t = TheoremById[id] ?: return fail("veta '$id' neexistuje")
                val c = Courses.firstOrNull { cc -> cc.theorems.any { it.id == id } } ?: return fail("veta '$id' nepatrí kurzu")
                Result.success(Resolved(c.id, norm(t.name + " " + t.src + " " + lines(t.czech) + " " + t.remarks.joinToString(" ") { q -> q.src + " " + lines(q.lines) } + " " + t.idea), CoverageState.EXPLAINED, c.shown().shows("theorem:$id"), "veta"))
            }
            "step" -> {
                val (c, ls) = steps[id] ?: return fail("krok lekcie '$id' neexistuje")
                val (l, s) = ls
                val offered = c.shown().lessons.any { it.id == l.id }
                when (s) {
                    is Step.Practice -> resolve(Link("gen:${s.gen}", link.asks), kind).map { r ->
                        r.copy(level = if (s.solo) r.level else minOf(r.level, CoverageState.GUIDED, compareBy { it.order }), offered = offered)
                    }
                    else -> {
                        val level = when (s) { is Step.Ask, is Step.Check -> CoverageState.GUIDED; else -> CoverageState.EXPLAINED }
                        Result.success(Resolved(c.id, norm(stepText(s)), level, offered, "krok lekcie ${l.id}"))
                    }
                }
            }
            "lesson" -> {
                val c = Courses.firstOrNull { it.owns(id) } ?: return fail("lekcia '$id' neexistuje")
                val l = c.lessons.first { it.id == id }
                Result.success(Resolved(c.id, norm(l.name + " " + l.learn + " " + l.steps.joinToString(" ", transform = ::stepText)), CoverageState.EXPLAINED, c.shown().lessons.any { it.id == id }, "lekcia"))
            }
            else -> fail("neznámy druh odkazu '${link.ref}'")
        }
    }

    fun inScope(course: String, lecture: String): Boolean = scope[course].orEmpty().any { code -> lecture == code || (code.none(Char::isDigit) && lecture.trimEnd(Char::isDigit) == code) }

    fun lectured(course: String, lecture: String): Boolean = courses[course]?.firstLectureEnd(lecture)?.let { it <= checkedAt } ?: false

    /** Does any lecture of the course name this code (S2, MI3, TČ4; a family like MI)? A typo would stay pending forever. */
    fun knownLecture(course: String, lecture: String): Boolean = courses[course]?.firstLectureEnd(lecture) != null

    private fun validate(id: String, e: CoverageException, errors: MutableList<String>) {
        if (e.owner.isBlank() || e.reason.isBlank()) errors += "$id: výnimka bez vlastníka alebo dôvodu"
        val dates = listOf("recorded" to e.recorded, "due" to e.due).map { (k, d) -> k to runCatching { LocalDate.parse(d) }.getOrNull() }
        dates.filter { it.second == null }.forEach { (k, _) -> errors += "$id: výnimka bez dátumu $k (YYYY-MM-DD)" }
        val (rec, due) = dates[0].second to dates[1].second
        if (rec != null && rec > today) errors += "$id: výnimka zapísaná v budúcnosti (${e.recorded})"
        if (rec != null && due != null && due < rec) errors += "$id: termín výnimky ${e.due} je pred jej zápisom"
        if (due != null && due < today) errors += "$id: výnimka po termíne ${e.due} (${e.owner}): postav precvičenie alebo vedome posuň termín"
    }

    fun run(reg: CoverageRegister): CoverageReport {
        val errors = ArrayList<String>()
        val skills = reg.skills.associateBy { it.id }
        reg.skills.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { errors += "skill $it je v registri viackrát" }
        reg.sources.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { errors += "zdroj $it je v registri viackrát" }

        val results = LinkedHashMap<String, SkillResult>()
        for (s in reg.skills) {
            if (s.course !in courses) errors += "${s.id}: neznámy kurz '${s.course}'"
            if (!s.id.startsWith(s.course + ".")) errors += "${s.id}: id musí začínať kurzom '${s.course}.'"
            val resolved = s.links.mapNotNull { l ->
                if (norm(l.asks).length < 6) { errors += "${s.id}: odkaz ${l.ref} nemá 'asks' (aspoň 6 znakov: čo presne pýta)"; return@mapNotNull null }
                resolve(l, s.kind).fold(
                    onSuccess = { r ->
                        when {
                            r.course != s.course -> { errors += "${s.id}: ${l.ref} patrí kurzu ${r.course}, nie ${s.course}"; null }
                            norm(l.asks) !in r.text -> { errors += "${s.id}: ${l.ref} nepýta „${l.asks}“ (podobný názov nie je pokrytie)"; null }
                            r.level == CoverageState.PRACTISED && s.signature.isNotEmpty() && s.signature.none { norm(it) in r.text } ->
                                { errors += "${s.id}: ${l.ref} nepýta výkon skillu (žiadne z ${s.signature})"; null }
                            else -> l to (l.max?.let { m -> if (m.order < r.level.order) r.copy(level = m) else r } ?: r)
                        }
                    },
                    onFailure = { errors += "${s.id}: ${it.message}"; null },
                )
            }
            val lectured = lectured(s.course, s.lecture)
            val inScope = inScope(s.course, s.lecture)
            if (s.course in courses && !knownLecture(s.course, s.lecture)) errors += "${s.id}: prednáška '${s.lecture}' nie je v rozvrhu kurzu"
            val offered = resolved.filter { it.second.offered }
            val bestOffered = offered.maxByOrNull { it.second.level.order }?.second?.level
            val bestAny = resolved.maxByOrNull { it.second.level.order }?.second?.level
            val state = when {
                !inScope -> CoverageState.OUT_OF_SCOPE
                !lectured -> CoverageState.PENDING_LECTURE
                bestOffered == CoverageState.PRACTISED -> CoverageState.PRACTISED
                bestAny == CoverageState.PRACTISED -> CoverageState.HIDDEN
                bestOffered != null -> bestOffered
                bestAny != null -> CoverageState.HIDDEN
                else -> CoverageState.MISSING
            }
            if (resolved.any { it.second.level == CoverageState.PRACTISED } && s.checks.isEmpty()) errors += "${s.id}: precvičenie bez testu (checks: test, ktorý overuje jeho matematiku)"
            s.exception?.let { validate(s.id, it, errors) }
            results[s.id] = SkillResult(s, state, lectured, inScope, resolved.sortedByDescending { it.second.level.order })
        }

        val parts = ArrayList<PartResult>()
        for (src in reg.sources) {
            if (src.course !in courses) errors += "zdroj ${src.id}: neznámy kurz '${src.course}'"
            when (SourceKinds[src.kind]) {
                null -> errors += "zdroj ${src.id}: neznámy druh '${src.kind}' (${SourceKinds.keys.joinToString()})"
                true -> {
                    val at = Regex("(?:[#&]|, )t=(\\d+)").find(src.ref)?.groupValues?.get(1)?.toInt()
                    val span = recordingSpan(src.anchor)
                    if (at == null || !src.id.endsWith("-$at") || span == null || span.first != at || span.last <= span.first)
                        errors += "zdroj ${src.id}: záznam bez platného času (id končí sekundou začiatku, ref s t=<tá sekunda>, anchor od–do od tej sekundy)"
                }
                false -> {}
            }
            src.exception?.let { validate("zdroj ${src.id}", it, errors) }
            if (src.transcription == Transcription.ID_ONLY) {
                if (src.parts.isNotEmpty()) errors += "zdroj ${src.id}: iba ID, a predsa má podotázky"
                // A source we know exists but cannot read hides its questions: in scope someone must own collecting it.
                if (inScope(src.course, src.chapter) && src.exception == null) errors += "zdroj ${src.id}: iba ID v rozsahu, chýba výnimka (kto a dokedy získa zadanie)"
                parts += PartResult(src, null, CoverageState.NOT_COLLECTED, null); continue
            }
            if (src.parts.isEmpty()) errors += "zdroj ${src.id}: má text, ale žiadne podotázky"
            src.parts.groupBy { it.id }.filter { it.value.size > 1 }.keys.forEach { errors += "zdroj ${src.id}: podotázka $it viackrát" }
            for (p in src.parts) {
                val sk = p.skill?.let { skills[it] }
                if (p.skill != null && sk == null) { errors += "${src.id} ${p.id}: neznámy skill '${p.skill}'"; continue }
                if (p.skill == null && p.uncertain == null) errors += "${src.id} ${p.id}: podotázka bez výkonu (skill), a nie je označená ako neistá"
                if (sk != null && sk.course != src.course) errors += "${src.id} ${p.id}: skill ${sk.id} je iného kurzu"
                // In scope a capability is its own skill; out of scope one catch-all skill (logic, determinant) holds all.
                if (sk != null && p.kind != null && p.kind != sk.kind && inScope(sk.course, sk.lecture)) errors += "${src.id} ${p.id}: výkon ${p.kind} ≠ výkon skillu ${sk.id} (${sk.kind})"
                val r = sk?.let { results.getValue(it.id) }
                val f = fieldsOf(p.domain)
                val state = when {
                    r == null -> CoverageState.UNCERTAIN
                    // Practised, but only over the other field than this sub-question asks (ℝ is not ℤₚ).
                    r.state == CoverageState.PRACTISED && f.size == 1 &&
                        r.best.none { (_, x) -> x.level == CoverageState.PRACTISED && x.offered && (x.fields.isEmpty() || f.first() in x.fields) } -> CoverageState.OTHER_FIELD
                    else -> r.state
                }
                parts += PartResult(src, p, state, sk)
            }
        }
        val report = CoverageReport(errors, results, parts, scope)
        val gapIds = report.gaps.map { it.skill.id }.toSet()
        for (r in results.values) {
            val e = r.skill.exception ?: continue
            // A gap may be registered before its lecture; after it, an exception without a gap is stale.
            if (r.skill.id !in gapIds && (r.lectured || !r.inScope)) errors += "${r.skill.id}: nie je medzera (precvičené alebo mimo rozsahu), výnimku zmaž"
            val open = report.partsOf(r.skill.id).count { it.state != CoverageState.PRACTISED }
            if (r.skill.id in gapIds) when {
                e.open == null -> errors += "${r.skill.id}: výnimka bez počtu otvorených podotázok (open)"
                open > e.open -> errors += "${r.skill.id}: otvorených podotázok je $open, výnimka kryje ${e.open} (nový zdroj alebo stratené precvičenie)"
            }
        }
        return report
    }
}

/** The span `mm:ss–mm:ss` (or `h:mm:ss`) in a recording's anchor, in seconds; null when there is none or a second is over 59. */
fun recordingSpan(anchor: String): IntRange? {
    val m = Regex("(?:(\\d+):)?(\\d+):([0-5]\\d)–(?:(\\d+):)?(\\d+):([0-5]\\d)").find(anchor) ?: return null
    val g = m.groupValues.drop(1).map { it.toIntOrNull() ?: 0 }
    return (g[0] * 3600 + g[1] * 60 + g[2])..(g[3] * 3600 + g[4] * 60 + g[5])
}

/** Answer kinds that hold a whole list the student writes and that is judged for completeness: Answer.Vectors with an open count. */
val EnumerateAnswers = setOf("Enumerate")

/** What the current tests need: LA1 chapters 2 to 4 (2nd test and exam), DML induction, sets and maps, number theory. */
val CoverageScope: Map<String, Set<String>> = mapOf("la1" to setOf("S2", "S3", "S4"), "dml" to setOf("MI", "MZ", "TČ"))

/** The report as lines, one per gap first, then the counts per course: tests and exams apart from practice sources. */
fun CoverageReport.lines(): List<String> = buildList {
    val bySkill = parts.filter { it.skill != null }.groupBy { it.skill!!.id }
    fun srcs(id: String) = bySkill[id].orEmpty().filter { it.state != CoverageState.PRACTISED }.map { "${it.source.id}${it.part?.id?.let { p -> " $p" } ?: ""}" }.let { if (it.size > 4) it.take(4) + "+${it.size - 4}" else it }.joinToString(", ")
    val ranked = gaps.sortedWith(compareBy({ it.skill.exception != null }, { gapState(it).order }, { -bySkill[it.skill.id].orEmpty().size }))
    for (g in ranked) {
        val s = g.skill
        val open = bySkill[s.id].orEmpty().count { it.state != CoverageState.PRACTISED }
        val ex = s.exception?.let { "výnimka: ${it.owner}, do ${it.due}" } ?: "BEZ VÝNIMKY"
        add("GAP ${s.id} | ${gapState(g).label} | ${s.kind.name.lowercase()} | ${s.title} | $open podotázok: ${srcs(s.id)} | $ex")
    }
    val check = CoverageCheck(scope)
    for (c in Courses.map { it.id }) for ((label, graded) in listOf("testy, skúšky, kvízy" to true, "cvičenia, skriptá, prednášky, prosemináre" to false)) {
        val ps = parts.filter { it.source.course == c && (it.source.assessment != "practice") == graded }
        val inScope = ps.filter { it.skill?.let { s -> skills[s.id]?.inScope } ?: (it.source.chapter.isNotBlank() && check.inScope(c, it.source.chapter)) }
        fun n(vararg st: CoverageState) = inScope.count { it.state in st }
        add("SUM $c, $label: podotázky v rozsahu ${inScope.size - n(CoverageState.NOT_COLLECTED)}: pokryté ${n(CoverageState.PRACTISED)}, čiastočne ${n(CoverageState.GUIDED, CoverageState.EXPLAINED, CoverageState.OTHER_FIELD)}, chýba ${n(CoverageState.MISSING, CoverageState.HIDDEN)}, neistý výkon ${n(CoverageState.UNCERTAIN)}, čaká na prednášku ${n(CoverageState.PENDING_LECTURE)}; zadaní nezískaných ${n(CoverageState.NOT_COLLECTED)}; mimo rozsahu ${ps.size - inScope.size}")
    }
}

package dev.hamper.tabula.data

import dev.hamper.tabula.data.TemplatesTest.Companion.checkMath
import dev.hamper.tabula.data.TemplatesTest.Companion.checkText
import dev.hamper.tabula.compat.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import dev.hamper.tabula.compat.time.*

/** The whole content: sizes the spec asks for, every reference resolves, every formula is well formed. */
class ContentTest {
    private val clipIds: Set<String> by lazy {
        val f = listOf(File("src/commonMain/composeResources/files/clips.json"), File("shared/src/commonMain/composeResources/files/clips.json")).first { it.exists() }
        val a = JSONArray(f.readText())
        (0 until a.length()).map { a.getJSONObject(it).getString("id") }.toSet()
    }

    private fun lines(ls: List<Line>, where: String) = ls.forEach { l ->
        when (l) { is Para -> checkText(where, l.tex); is Item -> checkText(where, l.tex); is Block -> checkMath(where, l.tex) }
    }

    @Test fun ten_lessons_15_to_25_steps() {
        assertEquals(10, La1Lessons.size)
        for (l in La1Lessons) assertTrue("${l.id}: ${l.steps.size} steps", l.steps.size in 15..25)
        assertEquals("step ids unique", La1Lessons.sumOf { it.steps.size }, La1Lessons.flatMap { it.steps }.map { it.id }.toSet().size)
    }

    @Test fun every_lesson_follows_the_spec_shape() {
        for (l in La1Lessons) {
            assertTrue("${l.id} starts with a question", l.steps.first() is Step.Ask)
            assertTrue("${l.id} has an interactive picture", l.steps.any { it is Step.Explore })
            assertTrue("${l.id} has an explanation", l.steps.any { it is Step.Explain || it is Step.Def })
            assertTrue("${l.id} has a worked example or a walked GEM", l.steps.any { it is Step.Worked || (it is Step.Explore && it.pic is Pic.Gem) })
            assertTrue("${l.id} has an attempt", l.steps.any { it is Step.Check || (it is Step.Practice && !it.solo) })
            assertTrue("${l.id} ends with a problem without help", l.steps.any { it is Step.Practice && it.solo } || l.steps.last() is Step.Check)
        }
    }

    @Test fun references_resolve() {
        val used = HashSet<String>()
        for (s in La1Lessons.flatMap { it.steps }) when (s) {
            is Step.Clip -> { assertTrue("clip ${s.clip}", s.clip in clipIds); used += s.clip }
            is Step.Def -> assertTrue("concept ${s.concept}", s.concept in ConceptById)
            is Step.Thm -> assertTrue("theorem ${s.theorem}", s.theorem in TheoremById)
            is Step.Prove -> assertTrue("proof ${s.proof}", s.proof in ProofById)
            is Step.Practice -> assertTrue("generator ${s.gen}", s.gen in Generators)
            else -> {}
        }
        assertEquals("every clip is placed in a lesson", clipIds, used)
        assertTrue("12 to 20 clips", clipIds.size in 12..20)
        val defsInLessons = La1Lessons.flatMap { it.steps }.filterIsInstance<Step.Def>().map { it.concept }.toSet()
        val explained = La1Lessons.flatMap { it.steps }.filterIsInstance<Step.Explain>().mapNotNull { it.formal }.flatten().toSet()
        val carded = La1Lessons.flatMap { it.steps }.filterIsInstance<Step.Explain>().flatMap { it.cards }.map { dev.hamper.tabula.ui.cardId(it) }.toSet()
        for (k in La1Concepts) assertTrue("definition ${k.id} is taught", k.id in defsInLessons || k.id in carded || k.czech.toSet().all { it in explained })
    }

    @Test fun all_text_is_well_formed() {
        for (s in La1Lessons.flatMap { it.steps }) {
            val w = s.id
            when (s) {
                is Step.Ask -> { checkText(w, s.q); s.options.forEach { checkText(w, it) }; checkText(w, s.after); assertTrue(w, s.correct in s.options.indices) }
                is Step.Explore -> { checkText(w, s.title); checkText(w, s.sub); checkText(w, s.doneTitle); s.doneBody?.let { checkText(w, it) } }
                is Step.Explain -> { checkText(w, s.title); checkText(w, s.body); s.formal?.let { lines(it, w) }; s.more?.let { checkText(w, it) } }
                is Step.Worked -> { checkText(w, s.title); checkText(w, s.problem); TemplatesTest.checkSolution(w, s.solution) }
                is Step.Check -> { checkText(w, s.q); s.options.forEach { checkText(w, it) }; checkText(w, s.why); assertTrue(w, s.correct in s.options.indices); assertEquals("$w options unique", s.options.size, s.options.toSet().size) }
                else -> {}
            }
        }
        for (k in La1Concepts) {
            lines(k.czech, k.id); k.why.forEach { checkText(k.id, it.part); checkText(k.id, it.because) }
            k.judge.forEach { checkText(k.id, it.tex); checkText(k.id, it.because) }; k.units.forEach { checkText(k.id, it) }
            assertTrue("${k.id} has examples and non-examples", k.judge.any { it.yes } && k.judge.any { !it.yes })
            assertTrue("${k.id} source", k.src.startsWith("Skriptá, Defin"))
        }
        for (t in La1Theorems) { lines(t.czech, t.id); checkText(t.id, t.idea); assertTrue(t.src.startsWith("Skriptá")) }
        for (p in La1Proofs) {
            checkText(p.id, p.task); checkText(p.id, p.given); checkText(p.id, p.end); p.checklist.forEach { checkText(p.id, it) }
            p.rounds.forEach { r -> checkText(p.id, r.prompt); r.given?.let { checkText(p.id, it) }; assertTrue(p.id, r.correct in r.options.indices); r.options.forEachIndexed { i, o -> checkText(p.id, o.tex); if (i != r.correct) assertTrue("${p.id}: wrong option without why", o.why != null) } }
        }
    }

    @Test fun clip_texts_are_well_formed() {
        val f = listOf(File("src/commonMain/composeResources/files/clips.json"), File("shared/src/commonMain/composeResources/files/clips.json")).first { it.exists() }
        val a = JSONArray(f.readText())
        for (i in 0 until a.length()) {
            val c = a.getJSONObject(i); val w = "clip " + c.getString("id")
            listOf("why", "before").forEach { k -> c.optString(k).takeIf { it.isNotEmpty() }?.let { checkText(w, it.replace('§', '$')) } }
            val t = c.getJSONObject("task")
            checkText(w, t.getString("q").replace('§', '$')); checkText(w, t.getString("why").replace('§', '$'))
            val o = t.getJSONArray("options"); for (j in 0 until o.length()) checkText(w, o.getString(j).replace('§', '$'))
            t.optJSONObject("wrong")?.let { m -> m.keys().forEach { k -> checkText(w, m.getString(k).replace('§', '$')) } }
        }
    }

    /**
     * The pages about a test (files/test/<exam id>.json, one per exam that has one): each parses, its points add up to
     * the exam's, every position was seen in all tests of today's format, its formulas are well formed, every link
     * opens something in its course.
     */
    @Test fun test_pages_read_and_links_resolve() {
        val dir = listOf(File("src/commonMain/composeResources/files/test"), File("shared/src/commonMain/composeResources/files/test")).first { it.isDirectory }
        val files = dir.listFiles { f -> f.name.endsWith(".json") }!!.sortedBy { it.name }
        assertTrue("the LA1 and DML pages", files.size >= 2)
        for (f in files) {
            val examId = f.name.removeSuffix(".json")
            val course = Courses.first { c -> c.exams.any { it.id == examId } }
            val exam = course.exams.first { it.id == examId }
            val p = parseTestPage(f.readText())
            assertEquals(f.name, exam.points, p.points.sum())
            assertEquals(f.name, p.points.indices.map { it + 1 }, p.positions.map { it.n })
            assertTrue("${f.name}: every position seen in all tests", p.positions.all { it.tests == p.tests })
            val texts = p.positions.flatMap { it.kinds }.map { it.label } + p.pointsLost.map { it.what } + p.evening.flatMap { listOf(it.what, it.why) } + p.notes
            texts.forEach { checkText(f.name, it) }
            val routes = p.positions.flatMap { it.kinds }.flatMap { it.routes } + p.definitions.flatMap { it.routes } + p.proofs.flatMap { it.routes } +
                p.pointsLost.flatMap { it.routes } + p.evening.flatMap { it.routes }
            for (r in routes.distinct()) {
                assertTrue("${f.name} link $r", routeLabel(r, course) != null)
                // a problem type falls back to a bare "Príklady" label, so its generator is checked by itself
                if (r.startsWith("gen:")) assertTrue("${f.name} generator $r", Generators[r.substringAfter(':')] != null)
            }
            // the course page lists what the 2nd tests ask; for LA1's skúška it does not (only "Hodnocení")
            TestTopicsByExam[examId]?.let { assertTrue("${f.name}: the course page's topics", it.topics.isNotEmpty()) }
        }
    }

    @Test fun papers_build_and_add_up() {
        assertEquals(8, La1Papers.size)
        for (paper in La1Papers + (1..30).map { la1GeneratedPaper(it) }) {
            assertEquals(paper.id, listOf(7, 12, 11), paper.problems.map { it.pts })
            for (p in paper.problems) {
                checkText(paper.id, p.text); checkText(paper.id, p.answer)
                assertEquals("${paper.id} problem ${p.n} rubric adds up", p.pts, p.checklist.sumOf { it.pts })
                p.checklist.forEach { checkText(paper.id, it.text) }
                p.solution?.let { TemplatesTest.checkSolution(paper.id, it) }
            }
        }
    }
}

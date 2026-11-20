package dev.hamper.tabula.data

import dev.hamper.tabula.data.TemplatesTest.Companion.checkMath
import dev.hamper.tabula.data.TemplatesTest.Companion.checkText
import dev.hamper.tabula.state.migrateTestDate
import dev.hamper.tabula.compat.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import dev.hamper.tabula.compat.time.DayOfWeek
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.LocalDateTime
import dev.hamper.tabula.compat.time.*

/**
 * Rules every course and every topic keeps (docs/CONTENT.md): ids unique and prefixed, lessons of the approved shape,
 * references resolve, text well formed, every wrong option explained, papers add up, the schedule is consistent.
 * LA1 kapitola 2 predates topics; its own checks are in ContentTest and ExplanationsTest.
 */
class CoursesTest {
    private val topics: List<Pair<Course, Topic>> = DmlTopics.map { Dml to it } + La1Topics.map { La1 to it }

    private fun clipIds(): Map<String, String> {
        val dir = listOf(File("src/commonMain/composeResources/files"), File("shared/src/commonMain/composeResources/files")).first { it.exists() }
        return dir.listFiles().orEmpty().filter { it.name == "clips.json" || (it.name.startsWith("clips-") && it.name.endsWith(".json")) }.flatMap { f ->
            val a = JSONArray(f.readText()); (0 until a.length()).map { a.getJSONObject(it).getString("id") to a.getJSONObject(it).optString("course", "la1") }
        }.toMap()
    }

    private fun text(where: String, s: String?) {
        assertTrue("$where: missing text", !s.isNullOrBlank())
        checkText(where, s!!.replace('§', '$'))
    }

    private fun lines(ls: List<Line>, where: String) = ls.forEach { l ->
        when (l) { is Para -> checkText(where, l.tex); is Item -> checkText(where, l.tex); is Block -> checkMath(where, l.tex) }
    }

    @Test fun ids_are_unique_across_courses_and_prefixed_by_topic() {
        val steps = Courses.flatMap { it.lessons }.flatMap { it.steps }.map { it.id }
        assertEquals("step ids unique", steps.size, steps.toSet().size)
        for (kind in listOf<(Course) -> List<String>>({ c -> c.lessons.map { it.id } }, { c -> c.concepts.map { it.id } }, { c -> c.theorems.map { it.id } }, { c -> c.proofs.map { it.id } })) {
            val all = Courses.flatMap(kind)
            assertEquals("ids unique: ${all.groupBy { it }.filter { it.value.size > 1 }.keys}", all.size, all.toSet().size)
        }
        for ((_, t) in topics) {
            val ids = t.lessons.map { it.id } + t.concepts.map { it.id } + t.theorems.map { it.id } + t.proofs.map { it.id } + t.generators.keys + t.templates.map { it.gen }
            ids.forEach { assertTrue("${t.id}: id $it must start with ${t.id}-", it.startsWith(t.id + "-")) }
            t.templates.forEach { assertTrue("${t.id}: template ${it.gen} has no generator", it.gen in t.generators) }
        }
    }

    @Test fun topic_lessons_follow_the_spec_shape() {
        for ((c, t) in topics) for (l in t.lessons) {
            assertTrue("${l.id}: ${l.steps.size} steps, want 15 to 25", l.steps.size in 15..25)
            assertTrue("${l.id} starts with a question", l.steps.first() is Step.Ask)
            assertTrue("${l.id} has an interactive picture", l.steps.any { it is Step.Explore })
            assertTrue("${l.id} has an explanation", l.steps.any { it is Step.Explain || it is Step.Def })
            assertTrue("${l.id} has a worked example", l.steps.any { it is Step.Worked || (it is Step.Explore && it.pic is Pic.Gem) })
            assertTrue("${l.id} has an attempt", l.steps.any { it is Step.Check || (it is Step.Practice && !it.solo) })
            assertTrue("${l.id} ends with a problem without help", l.steps.any { it is Step.Practice && it.solo } || l.steps.last() is Step.Check)
            assertTrue("${l.id}: topics must name lectures of ${c.code}", l.topics.isNotEmpty() && c.schedule.lectures.values.flatten().any { lec -> c.lessonsFor(lec).contains(l) })
        }
    }

    /**
     * The app has its own formats of exercises and does not copy MARAST's (docs/ZASADY.md, 12a; Teo, 18. 11.): no step
     * of a lesson is named after it. MARAST stays a source ("Zdroj"), never a format or a title.
     */
    @Test fun no_marast_formats_in_lessons() {
        val bad = (La1.lessons + Dml.lessons).flatMap { l ->
            l.steps.map { dev.hamper.tabula.screens.stepTitle(it).second }.filter { "MARAST" in it }.map { "${l.id}: a step titled after MARAST: $it" }
        } + (La1Templates + Courses.flatMap { it.templates }).filter { "MARAST" in it.title }.map { "template ${it.gen}: ${it.title}" }
        assertEquals("MARAST as a format or a title (it may only be a source):\n" + bad.joinToString("\n"), emptyList<String>(), bad)
    }

    @Test fun references_resolve() {
        val clips = clipIds()
        for (c in Courses) for (s in c.lessons.flatMap { it.steps }) when (s) {
            is Step.Clip -> assertEquals("clip ${s.clip} of ${c.id}", c.id, clips[s.clip])
            is Step.Def -> assertTrue("concept ${s.concept}", c.concepts.any { it.id == s.concept })
            is Step.Thm -> assertTrue("theorem ${s.theorem}", c.theorems.any { it.id == s.theorem })
            is Step.Prove -> assertTrue("proof ${s.proof}", c.proofs.any { it.id == s.proof })
            is Step.Practice -> assertTrue("generator ${s.gen}", s.gen in Generators)
            else -> {}
        }
        for ((c, t) in topics) {
            val taught = t.lessons.flatMap { it.steps }.filterIsInstance<Step.Def>().map { it.concept }.toSet()
            for (k in t.concepts) assertTrue("${k.id} is taught in a lesson of its topic", k.id in taught)
            t.checkpoints.forEach { cp -> assertTrue("${cp.after}", c.lessons.any { it.id == cp.after }) }
            t.planPapers.forEach { r -> val (_, paper, n) = r.split(':'); assertTrue(r, c.papers.any { it.id == paper && it.problems.size >= n.toInt() }) }
        }
    }

    @Test fun topic_text_is_well_formed_and_every_wrong_option_explained() {
        for ((_, t) in topics) {
            for (s in t.lessons.flatMap { it.steps }) {
                val w = s.id
                when (s) {
                    is Step.Ask -> { text(w, s.q); s.options.forEach { text(w, it) }; text(w, s.after); assertEquals("$w: explain exactly the wrong options", s.options.indices.toSet() - s.correct, s.wrong.keys); s.wrong.values.forEach { text(w, it) } }
                    is Step.Explore -> { text(w, s.title); text(w, s.sub); text(w, s.doneTitle); s.doneBody?.let { text(w, it) } }
                    is Step.Explain -> { text(w, s.title); text(w, s.body); s.formal?.let { lines(it, w) }; s.more?.let { text(w, it) }; assertNotNull("$w: explanation without source", s.src ?: s.formal ?: s.cards.firstOrNull()) }
                    is Step.Worked -> { text(w, s.title); text(w, s.problem); TemplatesTest.checkSolution(w, s.solution) }
                    is Step.Check -> {
                        text(w, s.q); s.options.forEach { text(w, it) }; text(w, s.why); assertTrue(w, s.correct in s.options.indices)
                        assertEquals("$w options unique", s.options.size, s.options.toSet().size)
                        assertEquals("$w: explain exactly the wrong options", s.options.indices.toSet() - s.correct, s.wrong.keys); s.wrong.values.forEach { text(w, it) }
                    }
                    else -> {}
                }
            }
            for (k in t.concepts) {
                text("${k.id} name", k.name); text("${k.id} ask", k.ask)
                lines(k.czech, k.id); k.why.forEach { text(k.id, it.part); text(k.id, it.because) }
                k.judge.forEach { text(k.id, it.tex); text(k.id, it.because); text(k.id, it.wrong); assertTrue("${k.id}: wrong repeats because", it.wrong != it.because) }
                k.units.forEach { text(k.id, it) }
                assertTrue("${k.id} has examples and non-examples", k.judge.any { it.yes } && k.judge.any { !it.yes })
                assertTrue("${k.id} source", k.src.startsWith("Skriptá, Defin"))
            }
            for (th in t.theorems) { text("${th.id} name", th.name); lines(th.czech, th.id); text(th.id, th.idea); assertTrue(th.src.startsWith("Skriptá")) }
            for (p in t.proofs) {
                text(p.id, p.task); text(p.id, p.given); text(p.id, p.end); p.checklist.forEach { text(p.id, it) }
                p.rounds.forEach { r -> text(p.id, r.prompt); r.given?.let { text(p.id, it) }; assertTrue(p.id, r.correct in r.options.indices); r.options.forEachIndexed { i, o -> text(p.id, o.tex); if (i != r.correct) text("${p.id}: wrong option without why", o.why) } }
            }
        }
    }

    @Test fun papers_add_up_to_the_test() {
        for ((c, t) in topics) for ((paperId, p) in t.paperProblems) {
            val spec = (if (c == Dml) DmlPaperSpecs else La1ExamSpecs).firstOrNull { it.id == paperId }
            assertNotNull("${t.id}: problem for unknown paper $paperId", spec)
            val pts = spec!!.problems.first { it.first == t.id }.second
            assertEquals("$paperId ${t.id}: points as in the spec", pts, p.pts)
        }
        // a topic that has paper problems has them for every paper it appears in
        for ((c, t) in topics) if (t.paperProblems.isNotEmpty()) for (spec in (if (c == Dml) DmlPaperSpecs else La1ExamSpecs)) if (spec.problems.any { it.first == t.id })
            assertTrue("${t.id}: missing its problem of ${spec.id}", spec.id in t.paperProblems)
        for (c in Courses) for (paper in c.papers + (1..20).map { c.generatedPaper(it) }) {
            if (paper.problems.isEmpty()) continue
            val exam = c.exams.firstOrNull { it.id == paper.exam } ?: c.exams.first { it.prepares }
            if (!paper.generated) assertEquals("${paper.id} adds up to ${exam.points}", exam.points, paper.problems.sumOf { it.pts })
            for (p in paper.problems) {
                text(paper.id, p.text); text(paper.id, p.answer)
                assertEquals("${paper.id} problem ${p.n} rubric adds up", p.pts, p.checklist.sumOf { it.pts })
                p.checklist.forEach { text(paper.id, it.text) }
                p.solution?.let { TemplatesTest.checkSolution(paper.id, it) }
            }
        }
    }

    @Test fun schedule_is_consistent() {
        val start = LocalDate.of(2026, 9, 21)
        for (c in Courses) {
            assertEquals("${c.id}: three parallels", 3, c.schedule.parallels.size)
            for (p in c.schedule.parallels) {
                val ls = c.lectures(p.id)
                assertTrue("${c.id}/${p.id}: lectures", ls.size >= 12)
                ls.forEach { l ->
                    assertEquals("${c.id}/${p.id} ${l.label} on the parallel's weekday", p.day, l.date.dayOfWeek)
                    assertEquals("${c.id}/${p.id} ${l.label} in its week", l.week.toLong(), dev.hamper.tabula.compat.time.ChronoUnit.WEEKS.between(start, l.date) + 1)
                    l.topics.forEach { t -> assertTrue("${c.id}: topic $t has a name", c.topicName(t) != t) }
                }
                assertEquals("${c.id}/${p.id}: dates ascend", ls.map { it.date }, ls.map { it.date }.sorted())
            }
            assertTrue("${c.id}: a test the app prepares for", c.exams.any { it.prepares })
            c.exams.forEach { text(it.id, it.note); text(it.id, it.title) }
        }
        // the DML lectures of 15 Nov 2026 as on the course page: Spěvák's P7 on 11. 11. was number theory 2
        val p2 = Dml.lectures("2").first { it.label == "P7" }
        assertEquals(LocalDate.of(2026, 11, 11), p2.date); assertEquals(listOf("TČ2"), p2.topics)
        assertEquals(DayOfWeek.WEDNESDAY, Dml.parallel("2").day)
    }

    @Test fun where_the_course_is() {
        // Wednesday 18 Nov 2026 at 10:00, parallel 2: DML P7 (11. 11.) done, P8 at 11:00 is next
        val w = where(Dml, "2", LocalDateTime.of(2026, 11, 18, 10, 0))
        assertEquals("P7", w.done.last().label); assertEquals("P8", w.next!!.label)
        // during the lecture it is still the next one, after it it is done
        assertEquals("P8", where(Dml, "2", LocalDateTime.of(2026, 11, 18, 12, 0)).next!!.label)
        assertEquals("P8", where(Dml, "2", LocalDateTime.of(2026, 11, 18, 13, 0)).done.last().label)
        // the test sitting: the picked one, else the first not over
        assertEquals(LocalDateTime.of(2026, 12, 2, 18, 0), examStart(Dml.test, null, LocalDateTime.of(2026, 11, 15, 9, 0)))
        assertEquals(LocalDateTime.of(2026, 12, 3, 16, 15), examStart(Dml.test, null, LocalDateTime.of(2026, 12, 2, 19, 30)))
        assertEquals(LocalDateTime.of(2026, 11, 20, 14, 30), examStart(La1.test, LocalDateTime.of(2026, 11, 20, 14, 30), LocalDateTime.of(2026, 11, 15, 9, 0)))
    }

    @Test fun the_old_test_date_becomes_an_la1_sitting() {
        assertEquals(LocalDateTime.of(2026, 11, 18, 18, 0), migrateTestDate(LocalDate.of(2026, 11, 18)))
        assertEquals(LocalDateTime.of(2026, 11, 20, 12, 45), migrateTestDate(LocalDate.of(2026, 11, 20)))
        assertEquals(LocalDateTime.of(2026, 11, 25, 18, 0), migrateTestDate(LocalDate.of(2026, 11, 25)))
    }

    @Test fun routes_find_their_course() {
        assertEquals(La1, courseOfRoute("concept:tn"))
        assertEquals(La1, courseOfRoute("gen:param"))
        assertEquals(La1, courseOfRoute("step:tn-3"))
        assertEquals(La1, courseOfRoute("paper:gen-12"))
        assertEquals(Dml, courseOfRoute("paper:dmlgen-12"))
    }

    @Test fun every_checkpoint_and_plan_entry_opens_its_assembled_paper() {
        assertEquals("all DML past papers assemble", DmlPaperSpecs.map { it.id }, Dml.papers.map { it.id })
        for (c in Courses) {
            // what the app offers: a checkpoint of a lesson pending its lecture is not (LecturedTest)
            for (cp in c.shown().checkpoints) {
                val paper = dev.hamper.tabula.screens.paperById(cp.paper)
                assertEquals("checkpoint ${cp.paper}: the real paper, not a generated stand-in", cp.paper, paper.id)
                val p = paper.problems.firstOrNull { it.n == cp.n }
                assertNotNull("checkpoint ${cp.paper} problem ${cp.n}", p)
                assertEquals("checkpoint ${cp.paper}:${cp.n} points", p!!.pts, cp.pts)
                assertTrue("checkpoint after a lesson of the course", c.lessons.any { it.id == cp.after })
            }
            for (r in c.shown().planPapers) {
                val (_, id, n) = r.split(':')
                val paper = dev.hamper.tabula.screens.paperById(id)
                assertEquals(r, id, paper.id); assertTrue(r, paper.problems.any { it.n == n.toInt() })
            }
        }
        assertEquals("plan papers take turns between topics", listOf("dml-mi", "dml-mz", "dml-tc"), Dml.planPapers.take(3).map { r -> DmlTopics.first { t -> r in t.planPapers }.id })
    }

    @Test fun the_path_keeps_chapter_2_first_and_the_plan_deals_only_the_test() {
        assertEquals("tn", La1.lessons.first().id)
        val t2 = La1.exams.first { it.id == "la1-test2" }
        assertEquals("only kapitola 2 for the 2nd test", La1Lessons.map { it.id }, La1.lessons.filter { La1.forExam(it, t2) }.map { it.id })
        val exam = La1.exams.first { it.id == "la1-skuska" }
        assertTrue("kapitola 3 and on for the exam", La1Topics.flatMap { it.lessons }.all { La1.forExam(it, exam) })
    }

    @Test fun the_students_own_sitting_decides_what_comes_next() {
        try {
            // wrote the 2nd test on 12. 11.: from then on the exam is the target, not the 2nd test until 20. 11.
            Sittings.picked = { if (it == "la1-test2") LocalDateTime.of(2026, 11, 12, 16, 15) else null }
            assertEquals("la1-skuska", La1.testAt(LocalDateTime.of(2026, 11, 15, 9, 0)).id)
            assertEquals("la1-test2", La1.testAt(LocalDateTime.of(2026, 11, 12, 16, 0)).id)
        } finally { Sittings.picked = { null } }
        // a sitting without a time (the exam has no KOS terms yet, a date picked by hand) never reads "o 00:00"
        assertEquals("", atTime(LocalDateTime.of(2027, 1, 4, 0, 0)))
        assertEquals(" o\u00A018:00", atTime(LocalDateTime.of(2026, 11, 18, 18, 0)))
        assertEquals(listOf("dnes", "zajtra", "o 3 dni", "o 5 dní"), listOf(0, 1, 3, 5).map(::inDays))
        // "na 2. písomku", never "na 2. písomka"
        for (c in Courses) for (e in c.exams) assertTrue("${e.id}: accusative", e.acc.isNotBlank() && !e.acc.endsWith("ka") && !e.acc.endsWith("Skúška"))
    }

    @Test fun every_generator_a_lesson_uses_belongs_to_its_course() {
        for (c in Courses) for (l in c.lessons) for (s in l.steps.filterIsInstance<Step.Practice>()) {
            assertEquals("${s.gen} by id", c, courseOfRoute("gen:${s.gen}"))
            assertEquals("${s.gen} by template", c, courseOfRoute("gen:${TemplateOf.getValue(s.gen)}"))
        }
    }

    @Test fun la1_prepares_for_the_exam_after_the_2nd_test() {
        assertEquals("la1-test2", La1.testAt(LocalDateTime.of(2026, 11, 15, 9, 0)).id)
        assertEquals("la1-test2", La1.testAt(LocalDateTime.of(2026, 11, 20, 15, 0)).id)
        assertEquals("la1-skuska", La1.testAt(LocalDateTime.of(2026, 11, 20, 16, 0)).id)
        assertEquals("dml-pisomka2", Dml.testAt(LocalDateTime.of(2026, 12, 4, 15, 0)).id)
    }
}

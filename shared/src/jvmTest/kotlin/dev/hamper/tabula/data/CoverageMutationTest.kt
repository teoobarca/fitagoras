package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import dev.hamper.tabula.compat.time.*

/** Proves CoverageTest bites: break the register on purpose and the check must report it. */
class CoverageMutationTest {
    private val reg = CoverageFiles.register
    private val check = CoverageFiles.check
    private val base = CoverageFiles.report

    private fun with(skill: String, f: (Skill) -> Skill) = CoverageRegister(reg.sources, reg.skills.map { if (it.id == skill) f(it) else it })

    @Test fun removing_the_practice_of_any_practised_skill_fails() {
        val gaps = base.gaps.map { it.skill.id }.toSet()
        val practised = base.skills.values.filter { it.state == CoverageState.PRACTISED && it.skill.id !in gaps && base.partsOf(it.skill.id).isNotEmpty() }
        assertTrue("register has practised skills to mutate", practised.size >= 10)
        for (r in practised) {
            val solo = r.best.filter { it.second.level == CoverageState.PRACTISED }.map { it.first }.toSet()
            val after = check.run(with(r.skill.id) { s -> s.copy(links = s.links - solo) })
            val res = after.skills.getValue(r.skill.id)
            assertTrue("${r.skill.id}: without ${solo.map { it.ref }} it must not stay practised", res.state != CoverageState.PRACTISED)
            assertTrue("${r.skill.id}: without its practice it must be an unexcused gap", after.unexcused.any { it.skill.id == r.skill.id })
        }
    }

    @Test fun removing_one_link_of_marast_13691_fails() {
        // #13691 needs a definition, the homogeneous proof and all subspaces listed; each is its own skill.
        val parts = reg.sources.first { it.id == "la1-marast-13691" }.parts.mapNotNull { it.skill }.toSet()
        // Listing all subspaces counts only as the student's own list (zp-zoznam); the pick from a list alone stays guided.
        assertEquals(CoverageState.PRACTISED, base.skills.getValue("la1.zp-enumerate-subspaces").state)
        val pickOnly = check.run(with("la1.zp-enumerate-subspaces") { s -> s.copy(links = s.links.filter { it.ref == "gen:zp-vypis" }) })
        assertTrue(pickOnly.skills.getValue("la1.zp-enumerate-subspaces").state != CoverageState.PRACTISED)
        for (sk in parts.filter { base.skills[it]?.state == CoverageState.PRACTISED }) {
            val after = check.run(with(sk) { it.copy(links = emptyList()) })
            assertTrue("$sk without links must fail", after.unexcused.any { it.skill.id == sk })
        }
    }

    @Test fun a_link_whose_content_does_not_ask_it_is_an_error() {
        // count-all counts vectors and bases; it never asks how many subspaces there are. Its own practice removed,
        // the skill must not be practised through this link.
        val after = check.run(with("la1.zp-count-subspaces") { it.copy(links = listOf(Link("gen:count-all", "koľko je podpriestorov"))) })
        assertTrue(after.errors.joinToString("\n"), after.errors.any { "la1.zp-count-subspaces" in it && "nepýta" in it })
        assertTrue(after.skills.getValue("la1.zp-count-subspaces").state != CoverageState.PRACTISED)
    }

    @Test fun a_link_that_asks_a_neighbouring_count_is_an_error() {
        // span-basis really asks "Koľko prvkov má M?": a count of elements, not of subspaces. The skill's own
        // signature (fixed once per skill) rejects the link although its phrase is asked and graded.
        val after = check.run(with("la1.zp-count-subspaces") { it.copy(links = listOf(Link("gen:span-basis", "Koľko prvkov má"))) })
        assertTrue(after.errors.joinToString("\n"), after.errors.any { "la1.zp-count-subspaces" in it && "nepýta výkon skillu" in it })
        assertTrue(after.skills.getValue("la1.zp-count-subspaces").state != CoverageState.PRACTISED)
    }

    @Test fun a_phrase_only_in_the_prompt_of_a_multi_part_exercise_is_an_error() {
        // complete's prompt copies the whole test task, including "find a nonzero y"; no part of it grades that.
        val after = check.run(with("la1.lz-add-construct") { it.copy(links = listOf(Link("gen:complete", "Nalezněte nějaký nenulový vektor"))) })
        assertTrue(after.errors.joinToString("\n"), after.errors.any { "la1.lz-add-construct" in it && "len v zadaní" in it })
    }

    @Test fun an_exception_hides_no_new_open_subquestion() {
        val g = base.gaps.first { it.skill.exception != null }
        val after = check.run(with(g.skill.id) { it.copy(exception = it.exception!!.copy(open = 0)) })
        assertTrue(after.errors.any { g.skill.id in it && "výnimka kryje 0" in it })
    }

    @Test fun an_exception_past_its_due_date_is_an_error() {
        val g = base.gaps.first { it.skill.exception != null }
        val late = CoverageCheck(today = dev.hamper.tabula.compat.time.LocalDate.parse(g.skill.exception!!.due).plusDays(1)).run(reg)
        assertTrue(late.errors.any { g.skill.id in it && "po termíne" in it })
    }

    @Test fun practice_over_the_reals_does_not_cover_a_question_over_z_p() {
        // A practised skill whose every practice is over ℝ; a sub-question of it over ℤ₅ stays a gap.
        val r = base.skills.values.first { r -> r.state == CoverageState.PRACTISED && r.skill.course == "la1" &&
            r.best.filter { it.second.level == CoverageState.PRACTISED }.let { ps -> ps.isNotEmpty() && ps.all { it.second.fields == setOf("R") } } }
        val src = SourceQuestion("la1-mutation-zp", "la1", "test", "mutation", "", "", "", Transcription.VERBATIM, "la1-test2", r.skill.lecture,
            parts = listOf(SourcePart("a", "tá istá úloha nad Z_5", r.skill.id, r.skill.kind, "ℤ₅³", "written")))
        val after = check.run(CoverageRegister(reg.sources + src, reg.skills))
        assertEquals(CoverageState.OTHER_FIELD, after.parts.first { it.source.id == "la1-mutation-zp" }.state)
        assertTrue(after.gaps.any { it.skill.id == r.skill.id })
    }

    @Test fun a_link_to_content_that_does_not_exist_is_an_error() {
        val sk = base.skills.values.first { it.state == CoverageState.PRACTISED }.skill.id
        val after = check.run(with(sk) { it.copy(links = it.links + Link("gen:does-not-exist", "čokoľvek tu")) })
        assertTrue(after.errors.any { sk in it && "neexistuje" in it })
    }

    @Test fun a_link_to_the_other_course_is_an_error() {
        val sk = base.skills.values.first { it.skill.course == "la1" && it.state == CoverageState.PRACTISED }.skill.id
        val after = check.run(with(sk) { it.copy(links = it.links + Link("gen:dml-tc-rea", "Bézout")) })
        assertTrue(after.errors.any { sk in it && "patrí kurzu dml" in it })
    }

    @Test fun dropping_an_exception_fails() {
        val excused = base.gaps.filter { it.skill.exception != null }
        assertTrue("register has excused gaps", excused.isNotEmpty())
        for (g in excused) {
            val after = check.run(with(g.skill.id) { it.copy(exception = null) })
            assertTrue("${g.skill.id} without exception must fail", after.unexcused.any { it.skill.id == g.skill.id })
        }
    }

    @Test fun an_exception_without_owner_or_date_is_an_error() {
        val g = base.gaps.first { it.skill.exception != null }
        val after = check.run(with(g.skill.id) { it.copy(exception = it.exception!!.copy(owner = "", due = "čoskoro")) })
        assertTrue(after.errors.any { g.skill.id in it && "vlastníka" in it })
        assertTrue(after.errors.any { g.skill.id in it && "due" in it })
    }

    @Test fun a_subquestion_with_an_unknown_skill_is_an_error() {
        val src = reg.sources.first { it.id == "la1-marast-13691" }
        val broken = src.copy(parts = src.parts.map { if (it.skill == "la1.zp-enumerate-subspaces") it.copy(skill = "la1.zp-podobny-nazov") else it })
        val after = check.run(CoverageRegister(reg.sources.map { if (it.id == src.id) broken else it }, reg.skills))
        assertTrue(after.errors.any { "la1-marast-13691" in it && "neznámy skill" in it })
    }

    @Test fun a_hidden_practice_does_not_count_once_lectured() {
        // Practice left out of the app (NotYetLectured) while its topic has been lectured is a gap, not coverage.
        val hidden = Generators.keys.firstOrNull { g -> Courses.any { c -> g in c.gens && !c.shown().shows("gen:$g") } }
        org.junit.Assume.assumeTrue("nothing is left out until lectured now", hidden != null)
        hidden!!
        val course = Courses.first { hidden in it.gens }.id
        val sk = Skill("$course.mutation-hidden", course, if (course == "la1") "S2" else "MI1", Capability.COMPUTE, "", "mutation",
            links = listOf(Link("gen:$hidden", Generators.getValue(hidden)(kotlin.random.Random(13)).prompt.substringBefore('$').trim().take(20))), checks = listOf("data.TemplatesTest#every_template_many_seeds"))
        val r = check.run(CoverageRegister(reg.sources, reg.skills + sk)).skills.getValue(sk.id)
        assertEquals(CoverageState.HIDDEN, r.state)
    }
}

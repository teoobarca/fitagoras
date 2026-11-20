package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/** Loads the source register (shared/src/commonMain/composeResources/files/coverage) the way the tests see it. */
object CoverageFiles {
    private val dir = listOf(File("src/commonMain/composeResources/files/coverage"), File("shared/src/commonMain/composeResources/files/coverage")).first { it.exists() }
    val register: CoverageRegister by lazy { CoverageRegister.parse(File(dir, "sources.json").readText(), File(dir, "skills.json").readText()) }
    val check = CoverageCheck()
    val report: CoverageReport by lazy { check.run(register) }
}

/**
 * Starts from the sources, not from the app: every known sub-question in the scope of the current tests must be
 * practised without help in the app, or have an exception that names who builds it and by when. A missing question
 * type fails here even though nothing exists for the other tests to check. Report: shared/build/reports/coverage.txt,
 * one line per gap.
 */
class CoverageTest {
    private val report get() = CoverageFiles.report

    @Test fun every_link_resolves_to_content_that_asks_it() {
        if (report.errors.isNotEmpty()) fail("Register coverage/ je chybný (${report.errors.size}):\n" + report.errors.joinToString("\n"))
    }

    @Test fun every_practice_has_an_existing_test() {
        val missing = report.skills.values.flatMap { r ->
            r.skill.checks.filter { c ->
                val (cls, method) = c.split('#').let { it[0] to it.getOrNull(1) }
                val k = runCatching { Class.forName("dev.hamper.tabula.$cls") }.getOrNull()
                k == null || method == null || k.methods.none { it.name == method && it.isAnnotationPresent(Test::class.java) }
            }.map { "${r.skill.id}: test $it neexistuje" }
        }
        assertTrue(missing.joinToString("\n"), missing.isEmpty())
    }

    @Test fun every_subquestion_in_scope_is_practised_or_has_an_owned_exception() {
        val lines = report.lines()
        File("build/reports").mkdirs()
        File("build/reports/coverage.txt").writeText(lines.joinToString("\n") + "\n")
        File("build/reports/coverage-parts.tsv").writeText(report.parts.joinToString("\n") { "${it.source.id}\t${it.part?.id ?: ""}\t${it.skill?.id ?: ""}\t${it.state.name}" } + "\n")
        println(lines.joinToString("\n"))
        val bad = report.unexcused
        if (bad.isNotEmpty()) fail("${bad.size} výkonov zo zdrojov nie je samostatne precvičených a nemá výnimku:\n" +
            lines.filter { it.startsWith("GAP") && it.endsWith("BEZ VÝNIMKY") }.joinToString("\n"))
    }

    @Test fun no_source_part_without_a_capability_unless_marked_uncertain() {
        val bad = report.parts.filter { it.part != null && it.part.skill == null && it.part.uncertain == null && it.source.transcription == Transcription.VERBATIM }
        assertTrue(bad.joinToString("\n") { "${it.source.id} ${it.part!!.id}: výkon chýba" }, bad.isEmpty())
    }

    /** The question type that once got lost (MARAST #13691, #14159, skriptá): its sub-questions stay in the register. */
    @Test fun the_lost_finite_space_questions_stay_registered() {
        val reg = CoverageFiles.register
        fun skillsOf(id: String) = reg.sources.firstOrNull { it.id == id }?.parts?.mapNotNull { it.skill }.orEmpty()
        assertTrue("MARAST #13691: definícia, homogénna sústava, všetky podpriestory", skillsOf("la1-marast-13691").containsAll(listOf("la1.def-subspace", "la1.prove-homog-subspace", "la1.zp-enumerate-subspaces")))
        assertTrue("MARAST #14159: počet podpriestorov", "la1.zp-count-subspaces" in skillsOf("la1-marast-14159"))
        assertTrue("skriptá: počet podpriestorov", reg.sources.any { it.kind == "textbook" && it.parts.any { p -> p.skill == "la1.zp-count-subspaces" } })
    }
}

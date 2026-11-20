package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import dev.hamper.tabula.compat.time.LocalDate
import kotlin.random.Random
import dev.hamper.tabula.compat.time.*

/**
 * Items of graded quizzes that are still open for students never appear in the app (docs/CONTENT.md, "Open graded
 * quizzes"): not in the sources (lessons, generators, assets, comments) and not in anything a generator produces. The
 * same format and skill are practised on generated variants. Add every open quiz item someone brings in here; remove it
 * only by a deliberate decision once its quiz is closed. Past tests and rozstřely published on fit-wiki are not listed.
 */
class OpenQuizTest {

    /** One open quiz item: where it is from, when the quiz closes, and phrases that identify it (any form we write it in). */
    private class OpenItem(val quiz: String, val closes: LocalDate, val phrases: List<String>)

    private val openItems = listOf(
        OpenItem("BI-LA1 MARAST, kvíz ke kapitole 2 (teorie), otázka o Z_2^3", LocalDate.of(2026, 11, 20), listOf(
            "Uvažujme vektorový prostor Z_2^3",
            "Existuje jediný podprostor dimenze dva", "Existuje jediný podprostor dimenze 2",
            "Existuje sedm různých podprostorů dimenze jedna", "Existuje 7 různých podprostorů dimenze 1",
            "Všechny netriviální podprostory obsahují sudý počet vektorů",
            "Prostor obsahuje právě osm vektorů", "Prostor obsahuje právě 8 vektorů",
        )),
    )

    /** Lower case, ℤ and \mathbb{Z} as z, sub/superscript digits as digits, everything but letters and digits dropped. */
    private fun norm(s: String): String = s.lowercase()
        .replace("\\mathbb", "").replace("ℤ", "z")
        .map { c -> "₀₁₂₃₄₅₆₇₈₉".indexOf(c).takeIf { it >= 0 }?.digitToChar() ?: "⁰¹²³⁴⁵⁶⁷⁸⁹".indexOf(c).takeIf { it >= 0 }?.digitToChar() ?: c }
        .filter { it.isLetterOrDigit() }.joinToString("")

    private val needles get() = openItems.flatMap { item -> item.phrases.map { norm(it) to "${item.quiz}: „$it“" } }

    private fun hits(where: String, text: String): List<String> { val t = norm(text); return needles.filter { (n, _) -> n in t }.map { (_, what) -> "$where contains $what" } }

    @Test fun the_check_recognises_an_open_item_in_any_of_our_notations() {
        assertTrue(hits("x", "Uvažujme vektorový prostor §\\mathbb{Z}_{2}^{3}§.").isNotEmpty())
        assertTrue(hits("x", "Uvažujme vektorový prostor ℤ₂³.").isNotEmpty())
        assertTrue(hits("x", "Existuje sedm různých podprostorů\n dimenze jedna.").isNotEmpty())
        assertTrue(hits("x", "Prostor obsahuje právě §8§ vektorů.").isNotEmpty())
        assertTrue(hits("x", "Mějme vektorový prostor §\\mathbb{Z}_{3}^{2}§. Počet vektorů tohoto prostoru je §9§.").isEmpty())
    }

    @Test fun no_open_quiz_item_in_sources_or_generated_content() {
        val bad = ArrayList<String>()
        val root = listOf(File("src/commonMain"), File("shared/src/commonMain")).first { it.exists() }
        root.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "json", "xml", "md", "txt") }.forEach { bad += hits(it.path, it.readText()) }
        // the docs and the course packs (kurz/: chapter extracts quoting lectures and sources) are inputs of content too
        for (dir in listOf("docs", "kurz", "kontroly")) {
            val d = listOf(File("../$dir"), File(dir)).firstOrNull { it.exists() } ?: error("$dir/ not found from ${File(".").absolutePath}")
            d.walkTopDown().filter { it.isFile }.forEach { bad += hits(it.path, it.readText()) }
        }
        for ((name, gen) in Generators) repeat(200) { seed ->
            val ex = gen(Random(seed * 31 + name.hashCode()))
            val answers = ArrayList<Any>()
            val texts = arrayListOf(ex.prompt, ex.label, ex.src, ex.hint)
            for (b in ex.parts) {
                val part = b(answers)
                texts += part.q; texts += part.answer.toString(); part.after?.let { texts += it }
                answers += when (val a = part.answer) {
                    is Answer.Choice -> a.correct
                    is Answer.Numbers -> a.expected
                    is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }
                    is Answer.Vectors -> a.example
                    is Answer.Paper -> 1
                }
            }
            ex.solution(answers).steps.forEach { st -> texts += st.say; st.math.forEach { texts += it.tex; it.note?.let { n -> texts += n } } }
            texts.forEach { bad += hits("generator $name/$seed", it) }
        }
        assertTrue("Open graded quiz items in the app (docs/CONTENT.md, Open graded quizzes):\n" + bad.distinct().joinToString("\n"), bad.isEmpty())
    }
}

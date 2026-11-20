package dev.hamper.tabula.data

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Terms come from the course, never from generic textbooks (data/Terminology.kt). Scans every string in the app's
 * sources and assets (lessons, definitions, theorems, papers, screens) and everything the generators produce.
 */
class TerminologyTest {
    private fun report(hits: List<String>) =
        assertTrue("${hits.size} generic terms the course does not use, write the course's word instead:\n" + hits.distinct().take(40).joinToString("\n"), hits.isEmpty())

    private fun hitsIn(where: String, text: String) = bannedIn(text).map { (w, b) -> "$where: „$w“ → ${b.use} (${b.src})" }

    @Test fun sources_and_assets_use_course_terms() {
        val root = mainSources()
        val hits = ArrayList<String>()
        root.walkTopDown().filter { it.isFile && (it.extension == "kt" || it.extension == "json") && it.name != "Terminology.kt" && it.parentFile.name != "coverage" }.forEach { f ->
            val src = f.readText()
            val texts = when {
                // the source register quotes past tests and lectures word for word; its terms are not ours to change
                f.name == "sources.json" && f.parentFile.name == "coverage" -> emptyList()
                // the skill register: only what people read (titles, notes, the words a link asks), never ids or refs
                f.name == "skills.json" && f.parentFile.name == "coverage" -> Regex("\"(title|note|asks|reason)\":\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(src).map { it.groupValues[2].replace("\\\\", "\\") }.toList()
                // the test page's analysis (files/test): its string values, not the kind ids ("param-span") between them
                f.extension == "json" && f.parentFile.name == "test" -> Regex("\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(src).map { it.groupValues[1].replace("\\\\", "\\") }.toList()
                f.extension == "json" -> listOf(src.replace("\\\\", "\\"))
                else -> KotlinStrings(src).all()
            }
            // ids and keys ("la1-k3-rank-param") are not text a student reads: they have no spaces
            texts.filter { it.contains(' ') }.forEach { hits += hitsIn(f.name, it) }
        }
        report(hits)
    }

    @Test fun generated_text_uses_course_terms() {
        val hits = ArrayList<String>()
        for ((_, texts) in allGeneratedTexts(40)) for (t in texts) hits += hitsIn(t.where, t.text) // formulas too: the R_2→ notation lives there
        java.io.File("build/reports").apply { mkdirs() }.resolve("terminology.txt").writeText(hits.distinct().joinToString("\n"))
        report(hits)
    }

    @Test fun the_string_scanner_skips_code() {
        val q = '"'; val d = '$'
        val src = "val a = ${q}Hlavné stĺpce $d{e.pivots.joinToString($q a $q) { $q$d{it + 1}$q }} sú$q // pivot\nval b = $q$q${q}raw $d{x} text$q$q$q"
        val s = KotlinStrings(src).all()
        assertTrue(s.toString(), s.none { "pivot" in it } && s.any { it.startsWith("Hlavné stĺpce") && it.endsWith(" sú") } && s.any { it.startsWith("raw") })
    }

    @Test fun the_list_catches_what_teo_saw() {
        assertTrue(bannedIn("Vyber pivot v prvom stĺpci.").isNotEmpty())
        assertTrue(bannedIn("Schodovitý tvar matice").isNotEmpty())
        assertTrue(bannedIn("Matica v hornom stupňovitom tvare, hlavné stĺpce 1 a 3.").isEmpty())
    }
}

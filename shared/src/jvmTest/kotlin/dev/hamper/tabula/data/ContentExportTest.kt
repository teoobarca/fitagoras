package dev.hamper.tabula.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

/** The export for the LLM checks (ContentExport.kt) covers every lesson step and every lesson of both courses. */
class ContentExportTest {
    @Test fun every_step_is_exported() {
        val out = Files.createTempDirectory("export").toFile()
        try {
            ContentExporter(seeds = 1).write(out)
            val chapters = ExportChapters
            assertEquals("every lesson in exactly one chapter", (La1.lessons + Dml.lessons).map { it.id }.sorted(), chapters.flatMap { c -> c.lessons.map { it.id } }.sorted())
            for (c in chapters) {
                val text = out.resolve("${c.id}/lekcie.md").readText()
                for (l in c.lessons) l.steps.indices.forEach { assertTrue("${c.id}: lesson:${l.id}:$it missing", "`lesson:${l.id}:$it`" in text) }
                for (f in listOf("lekcie.md", "karty.md", "generatory.md", "pisomky.md")) {
                    val t = out.resolve("${c.id}/$f").readText()
                    assertTrue("${c.id}/$f: a missing card", "CHÝBA" !in t)
                    assertTrue("${c.id}/$f: math left as §", '§' !in t)
                }
                assertTrue("${c.id}: bundle ${c.bundle}", listOf(java.io.File("../${c.bundle}"), java.io.File(c.bundle)).any { it.exists() })
            }
        } finally { out.deleteRecursively() }
    }
}

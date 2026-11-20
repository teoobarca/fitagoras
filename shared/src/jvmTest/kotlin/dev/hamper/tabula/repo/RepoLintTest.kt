package dev.hamper.tabula.repo

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * The package name is written in one place, app/build.gradle.kts (applicationId). Scripts ask scripts/app_id.py (or the
 * built APK); a literal id in a script broke four emulator sessions when the id changed in 1.3.0.
 * Allowed: build.gradle.kts and the manifest; shortcuts.xml must be literal (AppIdTest checks it equals applicationId);
 * Kotlin sources of every source set and tests (`package dev.hamper.tabula...` is the code namespace, not the app id); prose in Markdown.
 */
class RepoLintTest {
    private val id = Regex("""dev\.hamper\.(fitest|tabula)(?![\w.])""")
    private fun allowed(p: String) = p.endsWith("build.gradle.kts") || p.endsWith("AndroidManifest.xml") || p.endsWith("res/xml/shortcuts.xml") ||
        p.startsWith("app/src/main/java/") || p.startsWith("app/src/debug/java/") || p.startsWith("app/src/test/java/") || p.startsWith("shared/src/") || p.endsWith(".md")

    @Test fun no_hard_coded_package_id_outside_the_build() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.firstOrNull { File(it, ".git").exists() }
        assumeTrue("not inside a git checkout", root != null)
        val p = ProcessBuilder("git", "-C", root!!.path, "ls-files").redirectErrorStream(true).start()
        val files = p.inputStream.bufferedReader().readLines()
        assumeTrue("git not available", p.waitFor() == 0)
        val bad = files.filterNot(::allowed).mapNotNull { f ->
            val file = File(root, f)
            if (!file.isFile || file.length() > 2_000_000) return@mapNotNull null
            val text = runCatching { file.readText() }.getOrNull() ?: return@mapNotNull null
            if ('\u0000' in text) return@mapNotNull null // binary
            text.lineSequence().withIndex().firstOrNull { id.containsMatchIn(it.value) }?.let { "$f:${it.index + 1}: ${it.value.trim()}" }
        }
        assertTrue("hard-coded package id, ask scripts/app_id.py instead:\n" + bad.joinToString("\n"), bad.isEmpty())
    }
}

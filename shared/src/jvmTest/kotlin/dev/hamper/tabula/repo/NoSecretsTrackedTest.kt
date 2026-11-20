package dev.hamper.tabula.repo

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * The release key and the clip guest key never go into git, in any form (directory, file or symlink). A tracked
 * symlink named signing once replaced the real key directory on checkout, and git deleted the key for good.
 */
class NoSecretsTrackedTest {
    private val forbidden = Regex("""(^|/)(signing|keystore\.properties|secrets\.properties)$|(^|/)signing/|\.(jks|keystore|p12)$""")

    @Test fun no_key_or_secret_is_tracked() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.firstOrNull { File(it, ".git").exists() }
        assumeTrue("not inside a git checkout", root != null)
        val p = ProcessBuilder("git", "-C", root!!.path, "ls-files").redirectErrorStream(true).start()
        val files = p.inputStream.bufferedReader().readLines()
        assumeTrue("git not available", p.waitFor() == 0)
        val bad = files.filter { forbidden.containsMatchIn(it) }
        assertTrue("never track keys or secrets: $bad (git rm --cached, they are in .gitignore)", bad.isEmpty())
    }

    /** Keys and tokens inside tracked files (the patterns are shared with the pre-push check, CONTRIBUTING.md). */
    @Test fun no_tracked_file_contains_a_secret() {
        val (root, files) = trackedFiles() ?: return
        val bad = scan(root, files, patterns(root, "tajomstva-vzory.txt"))
        assertTrue("secrets in tracked files (move them to secrets.properties, change the key):\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test fun the_ignore_rules_cover_every_form() {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.firstOrNull { File(it, ".gitignore").exists() && File(it, "app").isDirectory }
        assumeTrue(root != null)
        val lines = File(root, ".gitignore").readLines().map { it.trim() }
        for (rule in listOf("signing", "*.jks", "keystore.properties", "secrets.properties")) assertTrue(".gitignore lacks $rule", rule in lines)
    }
}

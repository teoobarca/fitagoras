package dev.hamper.tabula.repo

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * The repository is public; the app's server runs inside the author's private project, whose name, workings and
 * whereabouts stay out of the tree, as do disk paths, internal addresses, the author's machines and tools, and e-mail
 * addresses. The app knows its server only as https://fitagoras.hamper.dev. The patterns are shared with the
 * pre-push check (`scripts/verejne/sukromne-vzory.txt`, CONTRIBUTING.md); the names themselves are in the author's
 * private list outside the repository, read when present. Every tracked file is read, binaries too.
 */
class NoPrivateServerNameTest {
    @Test fun no_tracked_file_names_private_infrastructure() {
        val (root, files) = trackedFiles() ?: return
        // the author's private list (names of his machines, servers, tools) lives outside the repository; read when present
        val own = (System.getenv("FITAGORAS_SUKROMNE_VZORY") ?: File(System.getProperty("user.home"), ".fitagoras/sukromne-vzory.txt").path)
            .let(::File).takeIf { it.isFile }?.let { patternsIn(it) }.orEmpty()
        val bad = scan(root, files, patterns(root, "sukromne-vzory.txt") + own)
        assertTrue("private traces in tracked files (fix them, CONTRIBUTING.md \"Vývoj vo verejnom repozitári\"):\n" + bad.joinToString("\n"), bad.isEmpty())
    }
}

/** The repository root and its tracked files, or null outside a git checkout (the test is then skipped). */
internal fun trackedFiles(): Pair<File, List<String>>? {
    val root = generateSequence(File("").absoluteFile) { it.parentFile }.firstOrNull { File(it, ".git").exists() }
    assumeTrue("not inside a git checkout", root != null)
    val p = ProcessBuilder("git", "-C", root!!.path, "ls-files", "-z").redirectErrorStream(true).start()
    val files = p.inputStream.bufferedReader().readText().split('\u0000').filter { it.isNotEmpty() }
    assumeTrue("git not available", p.waitFor() == 0)
    return root to files
}

/** One regex a line of `scripts/verejne/<name>` (# starts a comment), with MULTILINE like the Python check. */
internal fun patterns(root: File, name: String): List<Regex> {
    val f = File(root, "scripts/verejne/$name")
    assertTrue("$f is missing", f.isFile)
    val list = patternsIn(f)
    assertTrue("$f has no patterns", list.isNotEmpty())
    return list
}

internal fun patternsIn(f: File): List<Regex> =
    f.readLines().filter { it.isNotBlank() && !it.trimStart().startsWith("#") }.map { Regex(it, RegexOption.MULTILINE) }

/** "path:line: match" for every pattern found in a tracked file's path or content (UTF-8, and Latin-1 for binaries). */
internal fun scan(root: File, files: List<String>, rules: List<Regex>): List<String> {
    val own = setOf("scripts/verejne/sukromne-vzory.txt", "scripts/verejne/tajomstva-vzory.txt")
    val out = mutableListOf<String>()
    for (path in files) {
        val f = File(root, path)
        val texts = buildList {
            add(path)
            if (path !in own && f.isFile && !java.nio.file.Files.isSymbolicLink(f.toPath())) {
                val bytes = f.readBytes()
                val utf = String(bytes, Charsets.UTF_8)
                add(utf)
                if (utf.any { it.code > 127 } || bytes.take(8000).any { it == 0.toByte() }) add(String(bytes, Charsets.ISO_8859_1))
            }
        }
        for (text in texts) for (rule in rules) rule.find(text)?.let { m ->
            val line = text.substring(0, m.range.first).count { it == '\n' } + 1
            out += "$path:$line: ${m.value.take(80)} (${rule.pattern.take(40)})"
        }
    }
    return out.distinct()
}

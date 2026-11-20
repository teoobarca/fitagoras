package dev.hamper.tabula.repo

import dev.hamper.tabula.data.KotlinStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every regular expression the app compiles is one Android's ICU engine accepts, not only the desktop JVM's
 * java.util.regex (1.5.1, 18. 11.: a "}" left unescaped in a tutor pattern; the JVM took it, ICU threw while the class
 * loaded, and every exercise with a GEM crashed on the phone while all the JVM tests passed). Reads every pattern
 * written as a literal (Regex("…"), "…".toRegex(), Pattern.compile("…")) in the code that ships (common, Android, app)
 * and fails on what ICU rejects or reads differently:
 * - "{" or "}" outside a character class that is not a quantifier {n}, {n,}, {n,m};
 * - a lookbehind of unbounded length (*, +, {n,});
 * - Java-only classes (\p{javaLowerCase}, \p{IsLatin} spelled the Java way is fine in both, \p{java…} is not);
 * - an embedded flag ICU lacks ((?U) is Java's UNICODE_CHARACTER_CLASS).
 * A pattern built at run time is not seen here; keep patterns literal.
 */
class RegexPortableTest {
    private val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").exists() }
    private val dirs = listOf("shared/src/commonMain", "shared/src/androidMain", "shared/src/jvmCommonMain", "app/src/main")

    /** What ICU rejects in [p], or null. */
    fun problem(p: String): String? {
        var i = 0
        var cls = 0
        while (i < p.length) {
            val c = p[i]
            when {
                c == '\\' -> {
                    if (p.startsWith("\\p{java", i) || p.startsWith("\\P{java", i)) return "a Java-only class ${p.substring(i, minOf(p.length, i + 16))}"
                    if (cls == 0 && i + 1 < p.length && (p[i + 1] == 'p' || p[i + 1] == 'P') && p.getOrNull(i + 2) == '{') { i = p.indexOf('}', i).let { if (it < 0) p.length else it + 1 }; continue }
                    i += 2; continue
                }
                c == '[' -> { cls++; if (p.getOrNull(i + 1) == '^') i++; if (p.getOrNull(i + 1) == ']') i++ }
                c == ']' && cls > 0 -> cls--
                cls == 0 && c == '{' -> {
                    val q = Regex("""\{\d+(,\d*)?}""").matchAt(p, i) ?: return "an unescaped \"{\" at $i that is not a quantifier"
                    i += q.value.length; continue
                }
                cls == 0 && c == '}' -> return "an unescaped \"}\" at $i"
                cls == 0 && (p.startsWith("(?<=", i) || p.startsWith("(?<!", i)) -> {
                    var depth = 0; var j = i
                    while (j < p.length) { if (p[j] == '\\') { j += 2; continue }; if (p[j] == '(') depth++; if (p[j] == ')') { depth--; if (depth == 0) break }; j++ }
                    val body = p.substring(i + 4, minOf(j, p.length))
                    if (Regex("""(?<!\\)[*+]|\{\d+,}""").containsMatchIn(body)) return "a lookbehind of unbounded length at $i"
                }
                cls == 0 && p.startsWith("(?U", i) -> return "the Java-only flag (?U)"
            }
            i++
        }
        return null
    }

    /** The checker itself on the patterns that crashed 1.5.1 and on ones ICU takes. */
    @Test fun the_check_knows_icu() {
        assertTrue(problem("""\\frac\{(-?\d+)}\{(\d+)}""") != null)
        assertTrue(problem("""a}""") != null)
        assertTrue(problem("""(?<=a+)b""") != null)
        assertEquals(null, problem("""\\frac\{(-?\d+)\}\{(\d+)\}"""))
        assertEquals(null, problem("""[{}]x{2,3}\p{L}+(?<=ab)"""))
    }

    @Test fun every_pattern_compiles_on_android() {
        val find = Regex("""(Regex\(\s*|Pattern\.compile\(\s*)(?=")|(?=("{3}|")[^\n]*?\2\s*\.toRegex\()""")
        val bad = ArrayList<String>()
        var n = 0
        for (d in dirs) File(root, d).walkTopDown().filter { it.extension in setOf("kt", "java") }.forEach { f ->
            val src = f.readText()
            for (m in find.findAll(src)) {
                val at = m.range.last + 1
                val lit = KotlinStrings(src.substring(at)).all().firstOrNull() ?: continue
                n++
                problem(lit)?.let { bad += "${f.relativeTo(root)}:${src.substring(0, at).count { it == '\n' } + 1}: $it in /$lit/" }
            }
        }
        assertTrue("found only $n patterns: the scan is broken", n > 50)
        assertEquals("patterns Android (ICU) rejects; escape \"{\" and \"}\" as \\{ \\}:\n" + bad.joinToString("\n"), emptyList<String>(), bad)
    }
}

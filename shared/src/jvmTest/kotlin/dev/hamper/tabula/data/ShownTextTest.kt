package dev.hamper.tabula.data

import dev.hamper.tabula.Env
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.screens.ClipLibrary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.lang.reflect.Modifier
import java.util.IdentityHashMap
import kotlin.random.Random

/**
 * Nothing from the workshop reaches the screen (Teo, 18. 11.: "niekde v testoch ti utiekol môj zdroj … to ti nemôže
 * utiecť"): no path on a disk, no file name, no internal id of a source. A source a student reads names a thing
 * people know ("Test 21. 11. 2025, variant Sova", "Skriptá, Věta 2.18", "stránka predmetu").
 *
 * The corpus is everything the app can show: every string in the content model of both courses (lessons, steps,
 * cards, proofs, papers, plans; walked field by field, so a new field is in it the day it is added), the exercises
 * of every generator over many seeds part by part with right answers (questions, options, feedback, hints, the
 * solution), generated papers, the test pages and their topics, the clips, and the sentences written in the screens'
 * code. It reads more than a student sees (ids, routes): ids and routes never look like paths, so they pass.
 */
class ShownTextTest {
    private val rules = listOf(
        "a path on a disk" to Regex("""(/User[s]/|/Volume[s]/|/home/|~/|[A-Z]:\\)"""),
        "a folder of the repository or the workshop" to Regex("""(?i)\b(gauntle[t]|kurz/|docs/|scripts/|src/|composeResources|kontroly/)"""),
        "a file name" to Regex("""(?i)\b[\p{L}\d_-]+\.(md|json|pdf|adoc|tex|txt|py|kts?|csv|tsv|png|jpe?g|html?|sh|ogg|mp4|xml|ya?ml)\b"""),
        "a path" to Regex("""\p{L}[\p{L}\d_.-]*/[\p{L}\d_.-]+/[\p{L}\d_.-]*\p{L}"""),
        "an address" to Regex("""(?i)(https?://|www\.)"""),
        "an internal id with underscores" to Regex("""(?i)\b[\p{L}\d]+(_[\p{L}\d]+){2,}\b"""),
    )

    /** The text outside formulas: TeX ("\mathbb{Z}_5", "a_1_2", "T:\ ") is the typesetter's business, not a path. */
    private fun prose(s: String) = if ('$' !in s && '§' !in s && Regex("""\\[a-zA-Z]+""").containsMatchIn(s)) "" // a display formula, stored bare
        else s.replace(Regex("""\$\$.*?\$\$|\$[^$]*\$|§[^§]*§""", RegexOption.DOT_MATCHES_ALL), " ")

    private fun findings(where: String, text: String): List<String> {
        val p = prose(text)
        return rules.mapNotNull { (what, re) -> re.find(p)?.let { "$where: $what „${it.value}“ in „${text.take(160)}“" } }
    }

    /** Every String reachable from [root] through fields, lists, maps and arrays (lambdas are skipped). */
    private fun strings(root: Any?): List<String> {
        val out = ArrayList<String>()
        val seen = IdentityHashMap<Any, Unit>()
        val stack = ArrayDeque<Any>()
        root?.let(stack::add)
        while (stack.isNotEmpty()) {
            val o = stack.removeLast()
            if (seen.put(o, Unit) != null) continue
            when (o) {
                is String -> out += o
                is CharSequence -> out += o.toString()
                is Number, is Boolean, is Char, is Enum<*>, is Class<*>, is Function<*>, is Random -> {}
                is Map<*, *> -> o.entries.forEach { (k, v) -> k?.let(stack::add); v?.let(stack::add) }
                is Iterable<*> -> o.forEach { it?.let(stack::add) }
                is Array<*> -> o.forEach { it?.let(stack::add) }
                else -> {
                    val name = o.javaClass.name
                    if (!name.startsWith("dev.hamper.tabula")) continue
                    var c: Class<*>? = o.javaClass
                    while (c != null && c != Any::class.java) {
                        for (f in c.declaredFields) {
                            if (Modifier.isStatic(f.modifiers) || f.type.isPrimitive) continue
                            val v = runCatching { f.isAccessible = true; f.get(o) }.getOrNull() ?: continue
                            // a lazy field holds its value behind kotlin.Lazy
                            stack.add(if (v is Lazy<*>) (runCatching { v.value }.getOrNull() ?: continue) else v)
                        }
                        c = c.superclass
                    }
                }
            }
        }
        return out
    }

    @Test fun nothing_internal_on_screen() {
        Env.install(debug = false, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
        ClipLibrary.load()
        val bad = ArrayList<String>()
        fun scan(where: String, root: Any?) = strings(root).distinct().forEach { bad += findings(where, it) }

        for (c in Courses) scan("course ${c.id}", c)
        // an exercise holds its parts and its solution as functions of the answers before: built here with right
        // answers, as the screen builds them, so every question, option, feedback, hint and solution is read
        for ((id, g) in Generators) for (seed in 0 until 40) {
            val ex = runCatching { g(Random(seed)) }.getOrNull() ?: continue
            scan("gen:$id seed $seed", ex)
            val answers = ArrayList<Any>()
            for (k in ex.parts.indices) {
                val part = runCatching { ex.parts[k](answers.toList()) }.getOrNull() ?: break
                scan("gen:$id seed $seed part $k", part)
                when (val a = part.answer) {
                    is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }
                        .firstOrNull { it.size in a.count && a.ok(it) }?.let { scan("gen:$id seed $seed part $k why", a.why(it)); answers.add(it) } ?: break
                    is Answer.Choice -> answers.add(a.correct)
                    is Answer.Numbers -> answers.add(a.expected)
                    is Answer.Vectors -> { scan("gen:$id seed $seed part $k check", a.check(a.example)); answers.add(a.example) }
                    is Answer.Paper -> answers.add(1)
                }
            }
            runCatching { ex.solution(answers.toList()) }.getOrNull()?.let { scan("gen:$id seed $seed solution", it) }
        }
        for (c in Courses) for (seed in 0 until 10) scan("generated paper ${c.id} $seed", c.generatedPaper(seed))
        for (exam in File("src/commonMain/composeResources/files/test").listFiles()!!.map { it.nameWithoutExtension }) scan("test page $exam", testPage(exam))
        scan("test topics", TestTopicsByExam)
        scan("clips", ClipLibrary.all)

        // the sentences written in the screens' code; a literal without a space is a key or an asset name, never copy
        val code = File("src/commonMain/kotlin/dev/hamper/tabula").walkTopDown()
            .filter { it.extension == "kt" && (it.path.contains("/screens/") || it.path.contains("/ui/")) }
        for (f in code) for (s in KotlinStrings(f.readText()).all()) if (' ' in s.trim()) bad += findings(f.name, s)

        val list = bad.distinct()
        assertEquals("text a student can read that names a file, a path or an internal id (${list.size}); write the source as people know it:\n" +
            list.joinToString("\n"), emptyList<String>(), list)
    }
}

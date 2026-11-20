package dev.hamper.tabula.data

import dev.hamper.tabula.compat.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * After an answer the app shows only the first sentence of the explanation; the rest waits behind "Prečo?". So the
 * first sentence of every explanation (guesses, checks, definition examples, proof rounds, clip tasks, generated
 * exercises and their diagnoses) has to stay short: at most [MAX_WORDS] words, a formula between § or $ counting
 * as one word.
 */
class FirstSentenceTest {
    companion object {
        const val MAX_WORDS = 20
        private val abbreviations = setOf("napr", "tzv", "def", "resp", "tj", "č", "str", "obr", "pozn", "vz", "porov", "príp")

        /** The text up to the first sentence end outside math; "napr.", "Def. 2.7", "20. 10." and "t. j." do not end it. */
        fun firstSentence(s: String): String {
            var math: Char? = null
            var i = 0
            while (i < s.length) {
                val c = s[i]
                if (c == '$' || c == '§') {
                    if (math == null) math = c else if (math == c) math = null
                } else if (math == null && (c == '.' || c == '!' || c == '?')) {
                    if (i + 1 == s.length) return s
                    if (s[i + 1].isWhitespace()) {
                        val next = s.substring(i + 1).trimStart().firstOrNull()
                        val word = s.substring(0, i).takeLastWhile { it.isLetter() }
                        val abbreviation = c == '.' && (word.lowercase() in abbreviations || (word.length == 1 && word[0].isLowerCase()))
                        if (next != null && (next.isUpperCase() || next == '$' || next == '§') && !abbreviation) return s.substring(0, i + 1)
                    }
                }
                i++
            }
            return s
        }

        /** Words of [s], a formula between § or $ counted as one word. */
        fun words(s: String): Int = s.replace(Regex("""§[^§]*§|\$[^$]*\$"""), " M ").split(Regex("""\s+""")).count { w -> w.any { it.isLetterOrDigit() } }
    }

    @Test fun splitter() {
        assertEquals("Áno, napr. §x=1§ patrí do §P§.", firstSentence("Áno, napr. §x=1§ patrí do §P§. Ďalej nič."))
        assertEquals("Podľa Def. 2.7 a tzv. GEM áno.", firstSentence("Podľa Def. 2.7 a tzv. GEM áno. Druhá veta."))
        assertEquals("Hodnota §2.5§ nie je v §\\mathbb{Z}_5§.", firstSentence("Hodnota §2.5§ nie je v §\\mathbb{Z}_5§. Tak."))
        assertEquals("Discord, 20. 10. 2026, t. j. včera.", firstSentence("Discord, 20. 10. 2026, t. j. včera. Áno."))
        assertEquals("Lebo §x. Y§ nie.", firstSentence("Lebo §x. Y§ nie. Ďalej."))
        assertEquals("Jedna veta bez konca", firstSentence("Jedna veta bez konca"))
        assertEquals(4, words("Súčet §(1,2) + (3,4)§ je vektor."))
    }

    private val texts = ArrayList<Pair<String, String>>()
    private fun add(where: String, s: String?) { if (!s.isNullOrBlank()) texts += where to s }

    private fun collect() {
        for (s in Courses.flatMap { it.lessons }.flatMap { it.steps }) when (s) {
            is Step.Ask -> { add("${s.id} after", s.after); s.wrong.forEach { (k, v) -> add("${s.id} option $k", v) } }
            is Step.Check -> { add("${s.id} why", s.why); s.wrong.forEach { (k, v) -> add("${s.id} option $k", v) } }
            else -> {}
        }
        for (k in Courses.flatMap { it.concepts }) {
            k.judge.forEachIndexed { i, j -> add("${k.id} judge $i because", j.because); add("${k.id} judge $i wrong", j.wrong) }
            add("${k.id} trap", k.trap?.because)
        }
        for (p in Courses.flatMap { it.proofs }) p.rounds.forEachIndexed { r, round -> round.options.forEachIndexed { i, o -> add("${p.id} round $r option $i", o.why) } }
        // every course's clips: clips.json and one clips-<topic>.json per topic
        val dir = listOf(File("src/commonMain/composeResources/files"), File("shared/src/commonMain/composeResources/files")).first { it.exists() }
        for (f in dir.listFiles().orEmpty().filter { it.name == "clips.json" || (it.name.startsWith("clips-") && it.name.endsWith(".json")) }) {
        val a = JSONArray(f.readText())
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i); val t = o.getJSONObject("task"); val id = o.getString("id")
            add("$id why", t.getString("why"))
            val w = t.getJSONObject("wrong"); w.keys().forEach { add("$id option $it", w.getString(it)) }
        }
        }
        for ((name, gen) in Generators) repeat(60) { seed ->
            val ex = gen(Random(seed * 104729 + name.hashCode()))
            val answers = ArrayList<Any>()
            for ((pi, build) in ex.parts.withIndex()) {
                val part = build(answers); val where = "$name/$seed part $pi"
                add("$where after", part.after)
                when (val ans = part.answer) {
                    is Answer.Choice -> { add("$where why", ans.why); ans.wrong.forEach { (k, v) -> add("$where option $k", v) }; answers.add(ans.correct) }
                    is Answer.Numbers -> {
                        val r = Random(seed)
                        repeat(6) {
                            val given = ans.expected.map { e -> if (r.nextInt(3) > 0) e else if (ans.p != null) dev.hamper.tabula.la.Frac.of(r.nextInt(ans.p)) else e + dev.hamper.tabula.la.Frac.of(r.nextInt(-3, 4).toLong()) }
                            if (given != ans.expected) add("$where wrong $given", ans.diagnose?.invoke(given))
                        }
                        answers.add(ans.expected)
                    }
                    is Answer.Pick -> {
                        val n = ans.options.size
                        val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in ans.count }
                        sets.forEach { add("$where pick $it", ans.why(it)) }
                        answers.add(sets.first(ans.ok))
                    }
                    is Answer.Vectors -> {
                        add("$where vectors ok", ans.check(ans.example).head)
                        if (ans.open && ans.example.size > 1) ans.check(ans.example.drop(1)).let { add("$where vectors missing", it.head) }
                        if (ans.open) ans.check(ans.example + listOf(ans.example[0])).let { r -> add("$where vectors twice", r.head); r.each.forEachIndexed { i, e -> add("$where vectors entry $i", e) } }
                        answers.add(ans.example)
                    }
                    is Answer.Paper -> { add("$where paper miss", ans.miss); answers.add(1) }
                }
            }
        }
    }

    @Test fun first_sentence_is_short() {
        collect()
        assertTrue("only ${texts.size} explanations found", texts.size > 300)
        val long = texts.map { (where, s) -> Triple(where, firstSentence(s), words(firstSentence(s))) }.filter { it.third > MAX_WORDS }
        assertTrue("first sentence over $MAX_WORDS words:\n" + long.distinctBy { it.second }.joinToString("\n") { "${it.first} (${it.third}): ${it.second}" }, long.isEmpty())
    }
}

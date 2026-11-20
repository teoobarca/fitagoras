package dev.hamper.tabula.data

import dev.hamper.tabula.compat.putIfAbsent

/*
 * References to the textbook in any text ("Definícia 2.7", "(Veta 2.18)", "Pozorovanie 2.8", "Def. 2.7", Czech
 * "Věta 5.5", "Důsledek 6.2") that point to a definition or theorem the app has. A screen marks the first one of each
 * and opens it in the bottom sheet on a tap (ui/Reading.kt). DML numbers each kind separately within a chapter, LA1 has
 * one counter, so a reference is resolved by its kind and number within the course.
 */

/** What a reference opens. */
sealed interface RefTarget {
    val id: String
    /** "concept:podpriestor", the route of its full card. */
    val route: String
    data class Def(val k: Concept) : RefTarget { override val id get() = k.id; override val route get() = "concept:${k.id}" }
    data class Thm(val t: Theorem) : RefTarget { override val id get() = t.id; override val route get() = "theorem:${t.id}" }
    /** A numbered item no lesson teaches, quoted word for word ([RefName.lines]): the sheet is all there is. */
    data class Quote(val n: RefName) : RefTarget { override val id get() = "ref:${n.kind} ${n.number}"; override val route get() = "" }
}

/**
 * A numbered item the app teaches nowhere but a text refers to ("Definice 4.9"): its name, so the number never
 * stands alone, and its [name] and word-for-word [lines], so a tap shows it (every number opens something). A
 * theorem gets a Theorem card instead (with how it is proved); this is for what has none (a definition no lesson asks). [kind] as in [RefIndex] ("def", "veta", "pozorovanie", "tvrdenie", "lema", "dosledok", "postup");
 * [short] as [Concept.short] / [Theorem.short].
 */
data class RefName(val kind: String, val number: String, val short: String, val name: String = "", val lines: List<Line> = emptyList())

/** What a numbered reference is: its name and, if the app has it, what it opens. */
data class RefEntry(val kind: String, val number: String, val short: String, val target: RefTarget?)

/** A reference found in a text: where it is, what it is, the word as written ("Definíciu") and whether it is Slovak. */
data class RefHit(val range: IntRange, val entry: RefEntry?, val word: String, val kind: String, val number: String, val slovak: Boolean, val named: Int = 0) {
    val target: RefTarget? get() = entry?.target
}

/** The kind of a numbered statement, the same for the Czech source and Slovak text. */
fun refKind(word: String): String? {
    val w = word.lowercase()
    return when {
        w.startsWith("def") -> "def"
        w.startsWith("vet") || w.startsWith("vět") -> "veta"
        w.startsWith("pozorov") -> "pozorovanie"
        w.startsWith("tvrzen") || w.startsWith("tvrden") -> "tvrdenie"
        w.startsWith("lem") -> "lema"
        w.startsWith("důsled") || w.startsWith("dôsled") -> "dosledok"
        w.startsWith("postup") || w.startsWith("algoritm") -> "postup"
        w.startsWith("pozn") -> "poznamka"
        else -> null
    }
}

/** Slovak spellings get the name; Czech ones (Věta, Definice, Pozorování, Důsledek, Tvrzení, Lemma) are quotes and stay. */
private fun slovak(word: String): Boolean {
    val w = word.lowercase()
    return w == "def." || w.startsWith("definíc") || w.startsWith("vet") || (w.startsWith("pozorovan") && !w.startsWith("pozorován")) ||
        w.startsWith("tvrden") || (w.startsWith("lem") && !w.startsWith("lemm")) || w.startsWith("dôsled") || w.startsWith("postup")
}

/** How the number is written after the name: "(Def. 2.7)", "(Veta 2.18)". */
fun refAbbr(kind: String) = when (kind) {
    "def" -> "Def."; "veta" -> "Veta"; "pozorovanie" -> "Pozorovanie"; "tvrdenie" -> "Tvrdenie"; "lema" -> "Lema"; "dosledok" -> "Dôsledok"; "poznamka" -> "Poznámka"; else -> "Postup"
}

/** The kind as a word in the nominative, for a source label: "definícia", "veta". */
fun refNoun(kind: String) = when (kind) {
    "def" -> "definícia"; "veta" -> "veta"; "pozorovanie" -> "pozorovanie"; "tvrdenie" -> "tvrdenie"; "lema" -> "lema"; "dosledok" -> "dôsledok"; "poznamka" -> "poznámka"; else -> "postup"
}

/** Kind words of the textbook and of our Slovak, in any case ending, and the number right after. */
private val inText = Regex(
    """(?<![\p{L}])(Def\.|[Dd]efin[ií]c\p{L}*|[Vv][eě]t\p{L}*|[Pp]ozorov[aá]n\p{L}*|[Tt]vrzen\p{L}*|[Tt]vrden\p{L}*|[Ll]emm?\p{L}*|[Dd][ůô]sled\p{L}*|[Pp]ostup\p{L}*|[Aa]lgoritm\p{L}*|[Pp]ozn[aá]mk\p{L}*)[\s ]+(\d+\.\d+)(?![\d])""",
)

/** More numbers after a Czech kind word: "Věty 3.18 a 3.15", "Věty 2.56, 2.57". */
private val listAfter = Regex("""^(?:\s*,\s*|[\s\u00A0]+(?:a|i)[\s\u00A0]+)(\d+\.\d+)(?![\d])""")

/** A name in parentheses right after a reference's number, without digits: "(vektorový priestor Tⁿ)". */
private val nameAfter = Regex("""^[ \u00A0]*\(([^()\d§$]{2,60})\)""")

/** Every kind word in a source line, with all the numbers that follow it ("Věty 2.56, 2.57 a Důsledek 2.58"). */
private val inSource = Regex("""(\p{L}+\.?)|(\d+\.\d+)""")

/** The numbered statements of a course: "veta 2.18" → theorem t2-18, with their names. */
class RefIndex(private val map: Map<String, RefEntry>, private val remarks: Boolean = false) {
    val size get() = map.size
    fun get(kind: String, number: String): RefTarget? = map["$kind $number"]?.target
    fun entry(kind: String, number: String): RefEntry? = map["$kind $number"]

    /** Every numbered reference in [text], known or not, in order. */
    fun scan(text: String): List<RefHit> = inText.findAll(text).flatMap { m ->
        val word = m.groupValues[1]
        val kind = refKind(word) ?: return@flatMap emptySequence()
        // a course whose remarks have no cards yet does not mark them (RemarkTitles)
        if (kind == "poznamka" && !remarks) return@flatMap emptySequence()
        // "Definícia 2.2 (vektorový priestor Tⁿ)": a name the text already gives belongs to the reference, so the
        // registry's name replaces it instead of standing next to it
        val named = nameAfter.find(text.substring(m.range.last + 1))
        val range = if (named != null) m.range.first..(m.range.last + named.value.length) else m.range
        val first = RefHit(range, entry(kind, m.groupValues[2]), word, kind, m.groupValues[2], slovak(word), named?.value?.length ?: 0)
        // the textbook's Czech lists ("Věty 3.18 a 3.15"): every number opens its own card; a Slovak word has one
        // number (RefNamesTest), its name would not fit the others
        val more = ArrayList<RefHit>()
        if (!first.slovak && named == null) {
            var at = m.range.last + 1
            while (true) {
                val n = listAfter.find(text.substring(at)) ?: break
                val num = n.groups[1]!!
                more += RefHit((at + num.range.first)..(at + num.range.last), entry(kind, num.value), word, kind, num.value, false)
                at += n.range.last + 1
            }
        }
        sequenceOf(first) + more.asSequence()
    }.toList()

    /** References in [text] that open something in the app, in order. */
    fun find(text: String): List<RefHit> = scan(text).filter { it.target != null }

    companion object {
        fun of(concepts: List<Concept>, theorems: List<Theorem>, names: List<RefName> = emptyList(), remarks: Boolean = false): RefIndex {
            val map = LinkedHashMap<String, RefEntry>()
            fun scan(src: String, short: String, target: (String) -> RefTarget?) {
                var kind: String? = null
                var from: String? = null // "Tvrzení 6.8 až 6.11": the numbers between belong to it too
                var last: String? = null
                for (m in inSource.findAll(src)) {
                    val word = m.groups[1]?.value
                    if (word == "až") { from = last; continue }
                    if (word != null) { kind = refKind(word) ?: kind.takeIf { word.length <= 2 }; continue } // "a", "i" keep the kind
                    val k = kind ?: continue
                    val numbers = from?.let { a -> val (ch, lo) = a.split('.'); val hi = m.value.substringAfter('.').toInt(); if (m.value.substringBefore('.') == ch) (lo.toInt() + 1..hi).map { "$ch.$it" } else listOf(m.value) } ?: listOf(m.value)
                    from = null; last = m.value
                    for (n in numbers) target(k)?.let { map.putIfAbsent("$k $n", RefEntry(k, n, short, it)) }
                }
            }
            concepts.forEach { c -> scan(c.src, c.short) { k -> if (k == "def") RefTarget.Def(c) else null } }
            theorems.forEach { t -> scan(t.src, t.short) { k -> if (k != "def") RefTarget.Thm(t) else null } }
            names.forEach { n -> map.putIfAbsent("${n.kind} ${n.number}", RefEntry(n.kind, n.number, n.short, if (n.lines.isNotEmpty()) RefTarget.Quote(n) else null)) }
            return RefIndex(map, remarks)
        }
    }
}

/**
 * How a reference reads, so a number never stands alone. In running Slovak text the word keeps its case and the
 * name follows, the number goes behind: "Definíciu 2.23" → "definíciu LZ (Def. 2.23)"; in parentheses
 * "(Veta 2.18)" → "(veta o prieniku podpriestorov, 2.18)". In a source label: "veta o prieniku podpriestorov, 2.18".
 * Czech words in quotes and references without a name stay as written.
 */
fun refText(h: RefHit, text: String, label: Boolean = false, before: String = ""): String {
    val e = h.entry
    val raw = text.substring(h.range)
    if (e == null || e.short.isBlank()) return raw
    val head = before + text.substring(0, h.range.first)
    // a source label names the statement in the nominative where the word stands for itself ("Skriptá, Věta 2.18",
    // "Věty 2.56", a label that starts with it); elsewhere in a label ("pri Vete 2.60") the word keeps its case
    val lead = head.isBlank() || head.trimEnd().endsWith("Skriptá,") || head.trimEnd().endsWith("Skriptá ·")
    if (label && (lead || h.word in czechNominative)) {
        val l = "${refNoun(h.kind)} ${e.short}, ${h.number}"
        return if (head.isBlank()) l.replaceFirstChar { it.uppercase() } else l
    }
    if (!h.slovak) return raw
    // "Def." has no case: the name in the nominative form of the word
    val word0 = if (h.word == "Def.") refNoun(h.kind) else h.word
    val start = head.trimEnd().let { it.isEmpty() || it.last() in ".!?:" }
    val word = if (start) word0.replaceFirstChar { it.uppercase() } else word0.replaceFirstChar { it.lowercase() }
    // inside parentheses already: the number after a comma, never parentheses in parentheses
    val inParens = head.count { it == '(' } > head.count { it == ')' }
    return if (inParens) "$word ${e.short}, ${h.number}" else "$word ${e.short} (${refAbbr(h.kind)} ${h.number})"
}

/** Czech kind words in the nominative (singular or plural), as source labels write them. */
private val czechNominative = setOf("Věta", "Věty", "Definice", "Pozorování", "Tvrzení", "Lemma", "Důsledek", "Důsledky", "Postup", "Postupy", "Poznámka", "Poznámky")

/** The references of each course, by course id. */
val RefIndexes: Map<String, RefIndex> by lazy {
    Courses.associate { c ->
        val titles = RemarkTitles[c.id]
        c.id to RefIndex.of(c.concepts, c.theorems, RefNames[c.id].orEmpty() + (titles?.let { remarkQuotes(c, it) }.orEmpty()), remarks = titles != null)
    }
}

/**
 * Remarks (Poznámka) are references like theorems: a remark the app quotes word for word (a theorem's `remarks`, a
 * lesson's `explain(formal = …, src = "Skriptá, Poznámka 4.31")`) opens that quote. A course lists its remarks' names
 * here ([RemarkTitle]); until it does, its texts' "Poznámka" numbers are not marked (DML for now).
 */
val RemarkTitles: Map<String, Map<String, RemarkTitle>> by lazy { mapOf("la1" to dev.hamper.tabula.data.la1.La1RemarkTitles) }

/** A remark's Slovak short name for running text ("o počte prvkov podpriestoru") and its Czech title from the textbook. */
data class RemarkTitle(val short: String, val name: String)

private val remarkSrc = Regex("""^Skriptá, Poznámka (\d+\.\d+)$""")

/** Every remark the course quotes whole, once, with its title. */
fun remarkQuotes(c: Course, titles: Map<String, RemarkTitle>): List<RefName> {
    val quotes = c.theorems.flatMap { t -> t.remarks.map { it.src to it.lines } } +
        c.lessons.flatMap { l -> l.steps.filterIsInstance<Step.Explain>().mapNotNull { s -> s.formal?.let { f -> s.src.orEmpty() to f } } }
    return quotes.mapNotNull { (src, lines) ->
        val n = remarkSrc.find(src)?.groupValues?.get(1) ?: return@mapNotNull null
        val t = titles[n]
        RefName("poznamka", n, t?.short.orEmpty(), t?.name.orEmpty(), lines)
    }.distinctBy { it.number }
}

/** Named references without a card, by course id (la1/RefNames.kt, dml/RefNames.kt). */
val RefNames: Map<String, List<RefName>> by lazy { mapOf("la1" to dev.hamper.tabula.data.la1.La1RefNames, "dml" to dev.hamper.tabula.data.dml.DmlRefNames) }

/** The course an item belongs to (a concept, theorem or proof id), or null. */
fun courseOf(itemId: String): Course? =
    Courses.firstOrNull { c -> c.concepts.any { it.id == itemId } || c.theorems.any { it.id == itemId } || c.proofs.any { it.id == itemId } }

/** "Skriptá, Věta 2.18" → "Veta 2.18"; the theorem's name when the source has no number. */
fun theoremLabel(t: Theorem): String {
    val m = Regex("""(Věta|Věty|Pozorování|Tvrzení|Lemma|Důsledek|Postup)\s+(\d+\.\d+)""").find(t.src) ?: return t.name
    val kind = when (m.groupValues[1]) { "Pozorování" -> "Pozorovanie"; "Tvrzení" -> "Tvrdenie"; "Lemma" -> "Lema"; "Důsledek" -> "Dôsledok"; "Postup" -> "Postup"; else -> "Veta" }
    return "$kind ${m.groupValues[2]}"
}

/** "Skriptá, Definice 2.7" → "Definícia 2.7"; without a number, the concept's name. */
fun definitionLabel(k: Concept): String =
    Regex("""Defini(?:ce|cia)\s+(\d+\.\d+)""").find(k.src)?.let { "Definícia ${it.groupValues[1]}" } ?: "Definícia: ${k.name}"

/** The references of the course an item belongs to (its own course, whichever is open), or null. */
fun refsFor(itemId: String): RefIndex? = courseOf(itemId)?.let { RefIndexes[it.id] }

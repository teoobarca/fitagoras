package dev.hamper.tabula.tutor

import dev.hamper.tabula.data.Concept
import dev.hamper.tabula.data.PaperPart
import dev.hamper.tabula.data.Proof
import dev.hamper.tabula.data.lineText
import dev.hamper.tabula.data.theoremOf
import dev.hamper.tabula.platform.Assets

/*
 * Case 2 in the app: what one "na papier" checklist is, built from the app's own content (the same lines the student
 * ticks); the general tutor's tool posud_papier judges a photographed paper against it.
 */

/** The course packs shipped with the app (files/kontext, copied at build time from kurz/ and docs/ZASADY.md). */
object KurzKontext {
    fun subor(cesta: String): String? = runCatching { Assets.read("kontext/$cesta") }.getOrNull()

    /** Every pack: "la1/kap3-hodnost-regularita.md", "ZASADY.md"… */
    fun zoznam(): List<String> = subor("index.txt")?.lines()?.filter { it.isNotBlank() }.orEmpty()

    /** A section by its heading's start ("## Zhrnutie"), up to the next heading of the same or a higher level. */
    fun oddiel(text: String, nadpis: String): String? {
        val lines = text.lines()
        val i = lines.indexOfFirst { it.startsWith(nadpis) }.takeIf { it >= 0 } ?: return null
        val level = lines[i].takeWhile { it == '#' }.length
        val end = (i + 1 until lines.size).firstOrNull { j -> lines[j].startsWith("#") && lines[j].takeWhile { it == '#' }.length <= level } ?: lines.size
        return lines.subList(i, end).joinToString("\n").trim()
    }

    /** The course's "what holds in the whole course" (README, "Zhrnutie"), as numbered points. */
    fun zhrnutie(kurz: String): List<String> = subor("$kurz/README.md")?.let { oddiel(it, "## Zhrnutie") }?.lines()
        ?.filter { Regex("^\\d+\\.\\s").containsMatchIn(it) }?.map { it.trim() }.orEmpty()
}

private fun kurzZo(src: String, route: String): String = dev.hamper.tabula.data.courseOfRoute(route)?.id ?: if ("DML" in src || "dml" in route) "dml" else "la1"

private fun bez(s: String) = s.replace("**", "").replace('§', '$')

/** The checklist of a proof on paper ("Porovnaj so svojím papierom"): its start, each goal with its line, its end. */
fun polozkyDokazu(pr: Proof): List<String> =
    listOf("**Začiatok**: ${pr.given}") + pr.rounds.indices.map { "**${pr.goals.getOrNull(it) ?: "Krok ${it + 1}"}**: ${pr.rounds[it].line}" } + "**Záver**: ${pr.end}"

/** A proof written on paper ([ProofFlow] "Porovnaj so svojím papierom"): its goals, as the checklist shows them. */
fun naPapier(pr: Proof, polozky: List<String>): UlohaNaPapier {
    val th = theoremOf(pr)
    val co = pr.what?.lines?.joinToString(" ", transform = ::lineText) ?: pr.title
    val kurz = kurzZo(pr.src, "proof:${pr.id}")
    return UlohaNaPapier(
        "dokaz", th?.name ?: pr.title, "Dokáž: " + bez(co),
        vzor = listOf("Začiatok: " + bez(pr.given)) + pr.rounds.mapIndexed { i, r -> (pr.goals.getOrNull(i)?.let { "$it: " } ?: "") + bez(r.line) } + ("Záver: " + bez(pr.end)),
        body = polozky.map(::bez),
        pravidla = listOfNotNull(pr.foot?.let(::bez), pr.warn?.let { "Pozor: " + bez(it) }) + KurzKontext.zhrnutie(kurz).take(8),
        zdroj = pr.src,
    )
}

/** A definition written from memory: its parts (Concept.units) are the checklist, the textbook's wording the model. */
fun naPapier(k: Concept): UlohaNaPapier {
    val kurz = kurzZo(k.src, "concept:${k.id}")
    return UlohaNaPapier(
        "definicia", k.name, "Napíš definíciu: ${k.name} (${k.src}).",
        vzor = k.czech.map { bez(lineText(it)) },
        body = k.units.map(::bez),
        pravidla = listOfNotNull(k.trap?.let { "Pasca: „${bez(it.sentence)}“ – ${bez(it.because)}" }) + KurzKontext.zhrnutie(kurz).take(8),
        poznamky = k.why.map { "„${bez(it.part)}“: ${bez(it.because)}" },
        zdroj = k.src,
    )
}

/** A part of a test problem: its rubric lines with points, the model solution's sentences and formulas. */
fun naPapier(nazov: String, part: PaperPart, kurz: String, celyPriklad: String? = null): UlohaNaPapier = UlohaNaPapier(
    "uloha", nazov, (celyPriklad?.takeIf { it != part.text }?.let { "Celý príklad: " + bez(it).replace("¦", "") + "\nTáto časť: " } ?: "") + bez(part.text),
    vzor = part.solution?.steps?.flatMap { st -> listOf(bez(st.say)) + st.math.map { "$" + it.tex + "$" } } ?: listOfNotNull(part.answer?.let(::bez)),
    body = part.checklist.map { bez(it.text) },
    pravidla = KurzKontext.zhrnutie(kurz).take(8),
    pts = part.checklist.map { it.pts },
)

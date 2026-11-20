package dev.hamper.tabula.data

import dev.hamper.tabula.data.dml.DmlGlossary
import dev.hamper.tabula.data.la1.La1Glossary

/**
 * A symbol the student can tap in a theorem, a definition, a proof or a judge question: it gets a thin line and opens a
 * sheet with how to read it, one sentence and its definition. Only notation that has a definition in the app.
 *
 * [match]: a regular expression over the TeX source that finds the symbol (the first match on a screen is marked).
 * [tex]: how the sheet shows it. [read]: how it is read, after the symbol ("je podpriestor"). [say]: one Slovak
 * sentence, may hold §math§. [concept]: the id of the Concept that defines it.
 */
data class Symbol(val id: String, val tex: String, val match: String, val read: String, val say: String, val concept: String) {
    val regex: Regex by lazy { Regex(match) }
}

/** One glossary per course, by course id. */
val Glossaries: Map<String, List<Symbol>> by lazy { mapOf("la1" to La1Glossary, "dml" to DmlGlossary) }

/** The glossary of the course a concept, theorem or proof belongs to (empty if none). */
fun glossaryFor(itemId: String): List<Symbol> {
    return courseOf(itemId)?.let { Glossaries[it.id] }.orEmpty()
}

/** The theorem a proof proves, if one of the theorems carries it. */
fun theoremOf(proof: Proof): Theorem? = Courses.asSequence().flatMap { it.theorems }.firstOrNull { t -> t.proofs.any { it.id == proof.id } }

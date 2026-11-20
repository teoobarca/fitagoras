package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.la.*
import kotlin.random.Random

/*
 * BI-LA1, kapitola 4 skrípt "Frobeniova věta a kompletní řešení SLR" (slajdy 4): Frobeniova veta, celá množina riešení,
 * lineárne variety a ich rovnice. Lessons, theory and generators in Chapter4*.kt; the matrix code is shared with kapitola 3
 * (Chapter3Math.kt). Every id starts with "la1-k4-". Rules: docs/CONTENT.md.
 */

/** A problem of a generated exam: the solution set of a system in HST over Z_p as a variety (as 17. 12. 2025, př. 2B). */
internal fun k4GeneratedProblem(r: Random): PaperProblem {
    var h = r.hstSystem()
    while (h.s.particular == null) h = r.hstSystem()
    val f = h.f as Field.Zp
    val n = h.a.n
    val aug = Mat(h.a.rows.indices.map { h.a.rows[it] + listOf(h.b[it]) }, n)
    val s = h.s
    val sol = s.particular!!.tex() + "+\\langle " + s.kernel.joinToString(",") { it.tex() } + "\\rangle"
    return PaperProblem(
        2, 7, "Množina riešení cez Frobeniovu vetu",
        "S využitím Frobeniovy věty popište množinu řešení soustavy nad tělesem $\\mathbb{Z}_{${f.p}}$, jejíž rozšířená matice je \$${aug.tex()}$. Řešení zapište ve tvaru variety.",
        18,
        listOf(
            Rubric("\$h(\\mathbf{A})=h(\\mathbf{A}\\mid\\mathbf{b})$ a odkaz na Frobeniovu vetu", 1, "la1-k4-solve"),
            Rubric("Viazané a voľné premenné, $\\dim S_0=n-h(\\mathbf{A})$", 1, "la1-k4-solve"),
            Rubric("Partikulárne riešenie dosadením a jeho skúška", 2, "la1-k4-solve"),
            Rubric("Báza \$S_0$ z \$(\\mathbf{A}\\mid\\theta)$, nie z \$(\\mathbf{A}\\mid\\mathbf{b})$", 2, "la1-k4-solve"),
            Rubric("Zápis \$S=\\tilde{\\mathbf{x}}+S_0$ ako varieta", 1, "la1-k4-solve"),
        ),
        "\$S=$sol$ (iný bod a iná báza zamerania môžu dať iný, rovnako správny zápis).",
        "la1-k4-solve", solution = frobeniusSolution(f, h.a, s),
    )
}

val La1Chapter4 = Topic(
    id = "la1-k4",
    lessons = K4Lessons,
    concepts = K4Concepts,
    theorems = K4Theorems,
    proofs = K4Proofs,
    generators = K4Generators + K4DrillGenerators,
    templates = K4Templates + K4DrillTemplates,
    generatedProblem = ::k4GeneratedProblem,
)

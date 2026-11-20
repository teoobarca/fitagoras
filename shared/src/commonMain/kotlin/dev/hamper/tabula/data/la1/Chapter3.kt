package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.PMat
import kotlin.random.Random

/*
 * BI-LA1, kapitola 3 skrípt "Hodnost a regularita matice" (slajdy 3): hodnosť, násobenie a kombinácie, inverzná a
 * regulárna matica, veta o regularite, výpočet inverzie. Lessons, definitions, theorems, proofs and generators live in
 * Chapter3*.kt next to this file; kapitola 4 (Frobeniova veta, variety) is Chapter4.kt. Every id starts with "la1-k3-".
 * Rules: docs/CONTENT.md.
 */

/** A problem of a generated exam: rank with a parameter, regularity and the inverse for one value (as 26. 1. 2026, př. 2). */
internal fun k3GeneratedProblem(r: Random): PaperProblem {
    val pc = r.paramCase()
    val sp = pc.res.special.keys.sorted()
    val s = "\\{" + sp.joinToString(",") { it.tex(true) } + "\\}"
    val inv = Field.R.inverse(pc.inv0)!!
    return PaperProblem(
        1, 10, "Hodnosť s parametrom",
        "Mějme matici s parametrem \$a\\in\\mathbb{R}$: $\\mathbf{A}=${PMat(pc.rows).tex()}\\in\\mathbb{R}^{3,3}$. A) (4 b) Spočtěte hodnost \$\\mathbf{A}$ v závislosti na parametru. B) (2 b) Určete, pro které hodnoty parametru \$a$ je matice \$\\mathbf{A}$ regulární. C) (4 b) Spočtěte inverzní matici k \$\\mathbf{A}$ pro hodnotu \$a=0$.",
        24,
        listOf(
            Rubric("A) GEM bez straty prípadov: výrazom s \$a$ delí alebo násobí riadok len vo vetve, kde je nenulový", 2, "la1-k3-rank-param"),
            Rubric("A) Rozbor prípadov: \$h=3$ pre \$a\\notin $s$, \$h=2$ pre \$a\\in $s$", 2, "la1-k3-rank-param"),
            Rubric("B) Regulárna práve pre \$a\\notin $s$, s odkazom na vetu o regularite", 2, "la1-k3-rank-param"),
            Rubric("C) $(\\mathbf{A}\\mid\\mathbf{E})\\sim(\\mathbf{E}\\mid\\mathbf{A}^{-1})$ so sledovateľnými krokmi", 3, "la1-k3-inverse"),
            Rubric("C) Skúška $\\mathbf{A}\\mathbf{A}^{-1}=\\mathbf{E}$", 1, "la1-k3-inverse"),
        ),
        "\$h(\\mathbf{A})=3$ pre \$a\\notin $s$, \$h(\\mathbf{A})=2$ pre \$a\\in $s$; regulárna pre \$a\\notin $s$; pre \$a=0$ je $\\mathbf{A}^{-1}=${inv.tex()}$.",
        "la1-k3-rank-param", solution = rankParamSolution(pc),
    )
}

val La1Chapter3 = Topic(
    id = "la1-k3",
    lessons = K3Lessons,
    concepts = K3Concepts,
    theorems = K3Theorems,
    proofs = K3Proofs,
    generators = K3Generators,
    templates = K3Templates,
    generatedProblem = ::k3GeneratedProblem,
)

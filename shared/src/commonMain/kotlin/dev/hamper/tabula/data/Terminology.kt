package dev.hamper.tabula.data

/*
 * Terminology and notation come from the course (skriptá, lectures, proseminars), never from generic textbooks (Teo,
 * 15. 11.: "pivot" is not what we said in class). CourseTerms lists how each course names the things the app talks
 * about, checked against the skriptá sources (bi-la1, bi-dml); BannedTerms are generic words the course does not use,
 * each with the course's word to write instead. TerminologyTest scans all content and generator output for them.
 * A new term: look it up in the skriptá (and the notation chapter, LA1 chap-12-znaceni) before writing it.
 */

/** A course term: the Czech of the skriptá, our Slovak, where it is defined. */
data class CourseTerm(val czech: String, val slovak: String, val src: String)

val CourseTerms: Map<String, List<CourseTerm>> = mapOf(
    "la1" to listOf(
        CourseTerm("horní stupňovitý tvar (HST)", "horný stupňovitý tvar (HST)", "Skriptá LA1, kap. 1, Definice Horní stupňovitý tvar"),
        CourseTerm("hlavní sloupec, vedlejší sloupec", "hlavný stĺpec, vedľajší stĺpec", "Skriptá LA1, kap. 1, za Definicí Horní stupňovitý tvar"),
        CourseTerm("první nenulový prvek řádku", "prvý nenulový prvok riadku", "Skriptá LA1, kap. 1, Definice Horní stupňovitý tvar"),
        CourseTerm("operace Gaussovy eliminace (G1), (G2), (G3)", "úpravy GEM (G1), (G2), (G3)", "Skriptá LA1, kap. 1, Definice operací GEM"),
        CourseTerm("řádkové úpravy", "riadkové úpravy", "Skriptá LA1, kap. 1 a 3"),
        CourseTerm("zápis úpravy: (G3) ř2 − 2 · ř1, (G1) ř1 ↔ ř3", "(G3) §\\text{ř}2-2\\cdot\\text{ř}1§, rovnako (data/la/Gem.kt RowOp)", "Skriptá LA1, kap. 1 a 3 (příklady GEM); konvencie 15. 11. 2026"),
        CourseTerm("Gaussova eliminace (GEM)", "Gaussova eliminácia (GEM)", "Skriptá LA1, kap. 1, Postup Gaussova eliminace"),
        CourseTerm("lineární obal", "lineárny obal", "Skriptá LA1, kap. 2"),
        CourseTerm("hodnost matice", "hodnosť matice", "Skriptá LA1, kap. 3"),
        CourseTerm("lineárně nezávislý (LN), lineárně závislý (LZ)", "lineárne nezávislý (LN), lineárne závislý (LZ)", "Skriptá LA1, kap. 2"),
    ),
    "dml" to listOf(
        CourseTerm("základní krok (ZK), indukční krok (IK), indukční předpoklad (IP)", "základný krok (ZK), indukčný krok (IK), indukčný predpoklad (IP)", "Skriptá DML, kap. 4"),
        CourseTerm("rozšířený Eukleidův algoritmus (REA)", "rozšírený Euklidov algoritmus (REA)", "Skriptá DML, kap. 6"),
        CourseTerm("Bézoutova rovnost, Bézoutovy koeficienty", "Bézoutova rovnosť, Bézoutove koeficienty", "Skriptá DML, kap. 6"),
        CourseTerm("Čínská věta o zbytcích", "čínska veta o zvyškoch", "Skriptá DML, kap. 6"),
        CourseTerm("Eulerova funkce", "Eulerova funkcia", "Skriptá DML, kap. 6"),
    ),
)

/** A generic word the courses do not use: [pattern] (case-insensitive) and the course's word [use]. */
data class BannedTerm(val pattern: String, val use: String, val src: String, val ignoreCase: Boolean = true) {
    val regex: Regex by lazy { if (ignoreCase) Regex(pattern, RegexOption.IGNORE_CASE) else Regex(pattern) }
}

val BannedTerms: List<BannedTerm> = listOf(
    BannedTerm("""\bNS[DN]\b""", "gcd, lcm (vypísané: najväčší spoločný deliteľ, najmenší spoločný násobok)", "Skriptá DML, Definice 6.3 (značka gcd/lcm, skratky NSD/NSN nepoužíva); Teo 16. 11.", ignoreCase = false),
    BannedTerm("""pivot""", "prvý nenulový prvok riadku (jeho stĺpec je hlavný stĺpec)", "Skriptá LA1, kap. 1, Definice Horní stupňovitý tvar; „pivot“ len v dodatku o kódoch"),
    BannedTerm("""schodovit\p{L}*\s+(tvar|matic)""", "horný stupňovitý tvar (HST)", "Skriptá LA1, kap. 1, Definice Horní stupňovitý tvar"),
    BannedTerm("""redukovan\p{L}*\s+(stupňovit|schodovit|riadkov)""", "horný stupňovitý tvar (HST); redukovaný tvar kurz nepoužíva", "Skriptá LA1, kap. 1"),
    BannedTerm("""Gauss\p{L}*\s*[-–]\s*Jordan""", "GEM (Gaussova eliminácia)", "Skriptá LA1, kap. 1"),
    BannedTerm("""echelon""", "horný stupňovitý tvar (HST)", "Skriptá LA1, kap. 1"),
    BannedTerm("""vedúc\p{L}*\s+(prv|člen|koeficient)""", "prvý nenulový prvok riadku", "Skriptá LA1, kap. 1"),
    BannedTerm("""nulov\p{L}*\s+priestor""", "množina riešení homogénnej sústavy", "Skriptá LA1, kap. 1 a 3"),
    BannedTerm("""R_\{?\d+\}?\s*(\\to|\\leftrightarrow|[-+]\s*\d*\s*R_)""", "(G3) ř2 − 2·ř1, (G1) ř1 ↔ ř3: zápis úprav zo skrípt (RowOp v la/Gem.kt)", "Skriptá LA1, kap. 1 a 3; konvencie GEM 15. 11. 2026", ignoreCase = false),
    BannedTerm("""(?<![\p{L}\\])span(?!\p{L})""", "lineárny obal", "Skriptá LA1, kap. 2"),
    BannedTerm("""(?<![\p{L}\\])rank(?!\p{L})""", "hodnosť", "Skriptá LA1, kap. 3"),
    BannedTerm("""(?<!\p{L})kolm\p{L}*""", "„rieši rovnicu s koeficientmi …“ (riadky matice sú riešenia sústavy, ktorá má bázu podpriestoru v riadkoch)", "Skriptá LA1, Varování 2.5 (súčin dvoch vektorov kurz nedefinuje, kolmosť teda tiež nie); Klouda, 8. prednáška 18. 11. 2026 (Věta 4.23 cez transpozíciu súčinu matíc)"),
    BannedTerm("""báz\p{L}*\s+indukci|bázov\p{L}*\s+krok""", "základný krok (ZK)", "Skriptá DML, kap. 4"),
    BannedTerm("""indukčn\p{L}*\s+hypotéz""", "indukčný predpoklad (IP)", "Skriptá DML, kap. 4"),
)

/** The banned terms in [text], with the course's word for each. */
fun bannedIn(text: String): List<Pair<String, BannedTerm>> =
    BannedTerms.flatMap { b -> b.regex.findAll(text).map { it.value to b }.toList() }

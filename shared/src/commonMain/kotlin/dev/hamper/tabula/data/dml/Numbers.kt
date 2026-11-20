package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*

/*
 * BI-DML, Teória čísel (kapitola 6, cvičenia 6.x): deliteľnosť, gcd a lcm, rozšírený Euklidov algoritmus a Bézout, diofantické rovnice, inverzia modulo m, malá Fermatova a Eulerova veta, lineárne kongruencie a ich sústavy, ČVOZ.
 * Owned by one author: everything of this topic lives in this file (or in files next to it named Numbers*.kt), every id
 * starts with "dml-tc-". Registered in data/Dml.kt (DmlTopics). Rules: docs/CONTENT.md.
 */
val DmlNumbers = Topic(
    id = "dml-tc",
    lessons = tcLessons,
    concepts = tcConcepts,
    theorems = tcTheorems,
    proofs = tcProofs,
    generators = TcGenerators,
    templates = TcTemplateList,
    paperProblems = tcPaperProblems,
    generatedProblem = ::tcGeneratedProblem,
    checkpoints = listOf(
        Checkpoint("dml-tc-kongruencie", "dml-2025-e", 3, "Príklad 3 z písomky 4. 12. 2025", 10),
        Checkpoint("dml-tc-mocniny", "dml-2025-k", 3, "Príklad 3 z písomky 5. 12. 2025", 10),
        Checkpoint("dml-tc-cvoz", "dml-2023-a", 3, "Príklad 3 z písomky 6. 12. 2023", 10),
    ),
    planPapers = listOf("paper:dml-2025-e:3", "paper:dml-2025-k:3", "paper:dml-2024-r:3", "paper:dml-2023-d:2", "paper:dml-2023-a:3"),
)

package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*

/*
 * BI-DML, Množiny a zobrazenia (kapitola 5, cvičenia 5.x): všetky definície, množinové identity cez logiku a protipríklady, injektivita a surjektivita z definície, obraz a vzor, mohutnosť.
 * Owned by one author: everything of this topic lives in this file (or in files next to it named Sets*.kt), every id
 * starts with "dml-mz-". Registered in data/Dml.kt (DmlTopics). Rules: docs/CONTENT.md.
 */
val DmlSets = Topic(
    id = "dml-mz",
    lessons = SetsLessons,
    concepts = SetsConcepts,
    theorems = SetsTheorems,
    proofs = SetsProofs,
    generators = mapOf(
        "dml-mz-rovnost" to ::identityExercise,
        "dml-mz-mnoziny" to ::concreteExercise,
        "dml-mz-sipky" to ::finiteMapExercise,
        "dml-mz-zlozenie" to ::composeExercise,
        "dml-mz-injsurj" to { r -> affineExercise(r) },
        "dml-mz-velkosti" to ::cardinalityExercise,
        "dml-mz-inkluzia" to ::inclusionExercise,
        "dml-mz-obory" to ::domainRangeExercise,
    ),
    templates = listOf(
        TemplateInfo("dml-mz-rovnost", "Množinová identita", "Dôkaz cez formuly alebo protipríklad; písomky 2021 až 2025"),
        TemplateInfo("dml-mz-injsurj", "Injektivita a surjektivita", "\$\\mathbb{Z}^2$, \$\\mathbb{R}^3\\to\\mathbb{R}^2$ z definície; písomky 2024 a 2025"),
        TemplateInfo("dml-mz-velkosti", "Existujú také množiny?", "\$|C\\times D|$, \$|\\mathcal{P}(A)|$; písomky 2023 a 2025"),
        TemplateInfo("dml-mz-zlozenie", "Zloženie a inverzia", "Konečné zobrazenia"),
        TemplateInfo("dml-mz-sipky", "Obraz a vzor", "Injektívne, surjektívne, \$f[M]$, \$f^{-1}[N]$"),
        TemplateInfo("dml-mz-mnoziny", "Výpočet množiny", "Operácie na konkrétnych množinách"),
        TemplateInfo("dml-mz-inkluzia", "Inklúzia a rovnosť", "\$A\\subseteq B\$ práve keď \$A\\cap B=A\$? Cvičení 5.6"),
        TemplateInfo("dml-mz-obory", "Definičný obor a obor hodnôt", "Slovne zadané zobrazenia; Cvičení 5.8"),
    ),
    paperProblems = SetsPaperProblems,
    generatedProblem = ::setsGeneratedProblem,
    checkpoints = listOf(Checkpoint("dml-mz-identity", "dml-2025-e", 2, "Príklad 2 z písomky 4. 12. 2025", 10), Checkpoint("dml-mz-mohutnost", "dml-2025-k", 2, "Príklad 2 z písomky 5. 12. 2025", 10)),
    planPapers = listOf("paper:dml-2025-e:2", "paper:dml-2024-r:2", "paper:dml-2023-a:2", "paper:dml-2023-d:3", "paper:dml-2025-k:2"),
)

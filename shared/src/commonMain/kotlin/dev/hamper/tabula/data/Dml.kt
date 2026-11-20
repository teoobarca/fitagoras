package dev.hamper.tabula.data

import dev.hamper.tabula.data.dml.DmlInduction
import dev.hamper.tabula.data.dml.DmlNumbers
import dev.hamper.tabula.data.dml.DmlSets
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.LocalDateTime
import dev.hamper.tabula.compat.time.*

/*
 * BI-DML, for the 2nd test (písomka): Matematická indukce, Množiny a zobrazení, Teorie čísel. Czech text is from the
 * BI-DML textbook (src/textbook/chap-04..06 and cv-04..06, numbering per kind as the rendered textbook: Definice 5.9,
 * Věta 6.14, Cvičení 6.28), the past tests on fit-wiki and courses.fit.cvut.cz (classification, 2. zápočtovka).
 */

private fun at(s: String) = LocalDateTime.parse(s)

/** The topics of the 2nd test, in the order of the lectures. Adding content = filling one of these files. */
val DmlTopics: List<Topic> = listOf(DmlInduction, DmlSets, DmlNumbers)

/**
 * Past 2nd tests (fit-wiki, transcribed in the course sources outside the repository, zdroje/testy-dml-2.md, check images when unsure):
 * which topic writes which problem and its points. 2023: points of problems 1 and 2 are unreadable or not given,
 * 10 each is our estimate (the paper has 30 in all). 2024 F prints 9 and 4 + 6, so problem 3 has the remaining 11;
 * J and H show no points past problem 1, 9/10/11 as on R. The opravná 2026 (Ř) is the same paper: 75 min, 3 × 10.
 */
val DmlPaperSpecs: List<PaperSpec> = listOf(
    PaperSpec("dml-2025-e", "Písomka 4. 12. 2025 (variant E)", "Zadanie z fit-wiki", listOf("dml-mi" to 10, "dml-mz" to 10, "dml-tc" to 10)),
    PaperSpec("dml-2025-k", "Písomka 5. 12. 2025 (variant K)", "Zadanie z fit-wiki", listOf("dml-mi" to 10, "dml-mz" to 10, "dml-tc" to 10)),
    PaperSpec("dml-2026-r", "Opravná písomka 5. 1. 2026 (variant Ř)", "Zadanie z fit-wiki, overené podľa fotky; opravná zápočtová písomka v tom istom tvare (75 minút, 3 × 10 b)", listOf("dml-mi" to 10, "dml-mz" to 10, "dml-tc" to 10)),
    PaperSpec("dml-2024-r", "Písomka 6. 12. 2024 (variant R)", "Zadanie z fit-wiki", listOf("dml-mi" to 9, "dml-mz" to 10, "dml-tc" to 11)),
    PaperSpec("dml-2024-f", "Písomka 6. 12. 2024 (variant F)", "Zadanie z fit-wiki, overené podľa fotky; body príkladu 3 dopočítané do 30", listOf("dml-mi" to 9, "dml-mz" to 10, "dml-tc" to 11)),
    PaperSpec("dml-2024-j", "Písomka 5. 12. 2024 (variant J)", "Zadanie z fit-wiki, overené podľa fotky; príklady 2 a 3 sú výrezy na hárku príkladu 1, ich body (10 a 11) sú odhad podľa variantu R", listOf("dml-mi" to 9, "dml-mz" to 10, "dml-tc" to 11)),
    PaperSpec("dml-2024-h", "Písomka 5. 12. 2024 (variant H)", "Zadanie z fit-wiki, overené podľa fotky; body sú odhad podľa variantu R, príklad 3 je výrez s rovnakými číslami ako K 2025", listOf("dml-mi" to 9, "dml-mz" to 10, "dml-tc" to 11)),
    PaperSpec("dml-2023-d", "Písomka 8. 12. 2023 (variant D)", "Zadanie z fit-wiki, body príkladov 1 a 2 odhadnuté", listOf("dml-mi" to 10, "dml-tc" to 10, "dml-mz" to 10)),
    PaperSpec("dml-2023-a", "Písomka 6. 12. 2023 (variant A)", "Zadanie z fit-wiki, body odhadnuté", listOf("dml-mi" to 10, "dml-mz" to 10, "dml-tc" to 10)),
)

val DmlLessons: List<LessonDef> get() = DmlTopics.flatMap { it.lessons }
val DmlConcepts: List<Concept> get() = DmlTopics.flatMap { it.concepts }
val DmlTheorems: List<Theorem> get() = DmlTopics.flatMap { it.theorems }
val DmlProofs: List<Proof> get() = DmlTopics.flatMap { it.proofs }
val DmlGenerators: Map<String, (kotlin.random.Random) -> Exercise> get() = DmlTopics.fold(emptyMap()) { a, t -> a + t.generators }
fun dmlGeneratedPaper(seed: Int): TestPaper = assembleGenerated("dmlgen-", seed, DmlTopics)

val Dml: Course = Course(
    id = "dml", code = "BI-DML", short = "DML", name = "Diskrétna matematika a logika",
    chapter = "Indukcia, množiny, čísla", pathLabel = "BI-DML",
    lessons = DmlLessons, concepts = DmlConcepts, theorems = DmlTheorems, proofs = DmlProofs,
    papers = assemblePapers(DmlPaperSpecs, DmlTopics), generatedPaper = ::dmlGeneratedPaper,
    generatedById = { id -> if (id.startsWith("dmlgen-")) dmlGeneratedPaper(id.removePrefix("dmlgen-").toIntOrNull() ?: 1) else null },
    templates = DmlTopics.flatMap { it.templates }, checkpoints = DmlTopics.flatMap { it.checkpoints },
    // one topic after another, so the paper problems in the plan take turns
    planPapers = DmlTopics.map { it.planPapers }.let { ls -> (0 until (ls.maxOfOrNull { it.size } ?: 0)).flatMap { i -> ls.mapNotNull { it.getOrNull(i) } } },
    drills = emptyList(),
    schedule = DmlSchedule,
    exams = listOf(
        Exam("dml-pisomka1", "1. zápočtová písomka", "1. písomka", "1. písomku", 5, 10, from = LocalDate.of(2026, 10, 19), to = LocalDate.of(2026, 10, 23), topics = listOf("VL", "PL"),
            note = "Na tvojom cvičení v 5. týždni, jeden príklad z výrokovej alebo predikátovej logiky.", src = "courses.fit.cvut.cz, BI-DML, Pravidla hodnocení", word = PisomkaWord),
        Exam("dml-pisomka2", "2. zápočtová písomka", "2. písomka", "2. písomku", 30, 75,
            terms = listOf(
                Term(at("2026-12-02T18:00"), 75, "T9:105"), Term(at("2026-12-03T16:15"), 75, "T9:105"), Term(at("2026-12-03T18:00"), 75, "T9:105"),
                Term(at("2026-12-04T12:45"), 75, "T9:105"), Term(at("2026-12-04T14:30"), 75, "T9:105"),
            ),
            topics = listOf("MI", "MZ", "TČ"), prepares = true,
            note = "Tri príklady po 10 b: indukcia (slabá a silná, s komentovaným postupom), množiny a zobrazenia (všetky definície, množinová identita cez logiku, injektivita a surjektivita z definície), teória čísel (gcd a Bézout, diofantická rovnica, inverzia modulo m, veľké mocniny, kongruencie a ich sústavy). Prihlásiť sa v KOS na práve jeden termín, odhlásiť do 1. 12. S modifikáciou ELSA je vyhradený termín št 3. 12. o 18:00 v T9:155.",
            src = "KOS, termíny 2. písomky (15. 11. 2026); courses.fit.cvut.cz, BI-DML, Pravidla hodnocení, 2. zápočtovka", word = PisomkaWord),
        Exam("dml-opravna", "Opravná zápočtová písomka", "Opravná písomka", "opravnú písomku", 30, 75, from = LocalDate.of(2027, 1, 4), topics = listOf("VL", "PL", "MI", "MZ", "TČ"),
            note = "Len pri 15 až 19 b a aspoň 5 zo 6 kvízov v MARASTe. Logika až teória čísel, treba aspoň 50 %, potom zápočet a 20 b. Termín čo najskôr v skúškovom, oznámia ho.", src = "courses.fit.cvut.cz, BI-DML, Pravidla hodnocení", word = PisomkaWord),
        Exam("dml-skuska", "Skúška", "Skúška", "skúšku", 70, null, from = LocalDate.of(2027, 1, 4), to = LocalDate.of(2027, 2, 14),
            note = "Rozstrel v MARASTe 0 až 16 b (treba 10), potom ústna časť 0 až 54 b (treba 20). Celá látka DML, termíny v KOS.", src = "courses.fit.cvut.cz, BI-DML, Pravidla hodnocení", word = SkuskaWord),
    ),
    topicNames = mapOf(
        "VL" to "Výroková logika", "PL" to "Predikátová logika", "MI" to "Matematická indukcia", "MZ" to "Množiny a zobrazenia",
        "TČ" to "Teória čísel", "REL" to "Relácie", "KOM" to "Kombinatorika",
    ),
    libraryLabel = "Kapitoly 4 až 6: Indukce, Množiny a zobrazení, Teorie čísel",
    sources = "Skriptá BI-DML (kapitoly 4 až 6 a cvičenia k nim, číslovanie ako v skriptách), courses.fit.cvut.cz (čo chce 2. písomka, harmonogram), KOS (termíny), písomky z fit-wiki 2021 až 2025, titulky prednášok (paralelka 2, Spěvák). Výpočty robí appka presne, celými číslami a modulo m.",
    clipLecturer = "Diskrétna matematika", clipTeacher = "Spěvák",
    paperHeader = "Postupujte metodicky, neopomeňte žádný logický krok a své řešení vysvětlujte a odpovědi i výsledky řádně označte.",
    generatedNote = "Indukcia, množiny a zobrazenia, teória čísel: šablóny minulých písomiek, nové čísla",
    topics = DmlTopics,
)

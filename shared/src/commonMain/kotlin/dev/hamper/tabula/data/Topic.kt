package dev.hamper.tabula.data

import kotlin.random.Random

/*
 * A topic is the unit content is added in: one file per topic (data/dml/Induction.kt, data/la1/Chapter3.kt), holding its
 * lessons, definitions, theorems, proofs, test templates and its problems of the course's past papers. A course lists
 * its topics in order; nothing else needs to change to add content. Rules: docs/CONTENT.md.
 */
class Topic(
    /** "dml-mi": every id inside starts with it (lessons, steps, concepts, theorems, proofs, generators). */
    val id: String,
    val lessons: List<LessonDef> = emptyList(),
    val concepts: List<Concept> = emptyList(),
    val theorems: List<Theorem> = emptyList(),
    val proofs: List<Proof> = emptyList(),
    /** Generators by id; [templates] lists the ones shown in the practice tab. */
    val generators: Map<String, (Random) -> Exercise> = emptyMap(),
    val templates: List<TemplateInfo> = emptyList(),
    /** This topic's problem of each past paper of the course, by paper id (see the course's [PaperSpec]s). */
    val paperProblems: Map<String, PaperProblem> = emptyMap(),
    /** This topic's problem of a generated paper, new numbers from the seed; null when the topic has no test problem. */
    val generatedProblem: ((Random) -> PaperProblem)? = null,
    /** Paper problems checked in the path after a lesson of this topic. */
    val checkpoints: List<Checkpoint> = emptyList(),
    /** Paper problems the plan deals out ("paper:dml-2025-e:1"). */
    val planPapers: List<String> = emptyList(),
)

/**
 * A past paper of a course, assembled from its topics: [problems] says which topic writes problem n, worth how many
 * points (the order on the paper differs between years).
 */
data class PaperSpec(val id: String, val name: String, val src: String, val problems: List<Pair<String, Int>>, val exam: String? = null)

/** The papers a course has, each problem taken from the topic that owns it; a missing one is left out (tests catch it). */
fun assemblePapers(specs: List<PaperSpec>, topics: List<Topic>): List<TestPaper> = specs.mapNotNull { spec ->
    val ps = spec.problems.mapIndexedNotNull { i, (topic, _) -> topics.firstOrNull { it.id == topic }?.paperProblems?.get(spec.id)?.copy(n = i + 1) }
    if (ps.size == spec.problems.size) TestPaper(spec.id, spec.name, spec.src, ps, exam = spec.exam) else null
}

/** A paper with new numbers: one problem per topic that has a generator, in the course's topic order. */
fun assembleGenerated(prefix: String, seed: Int, topics: List<Topic>, exam: String? = null): TestPaper {
    val r = Random(seed)
    val ps = topics.mapNotNull { it.generatedProblem }.mapIndexed { i, g -> g(r).copy(n = i + 1) }
    return TestPaper("$prefix$seed", "Nové čísla $seed", "Šablóny z minulých písomiek, nové čísla", ps, generated = true, exam = exam)
}

package dev.hamper.tabula.data

/*
 * A lesson in pieces (Teo, 18. 11.: "nikdy sa toľko nepripravím a neodklikám všetko v appke"): one idea and right away
 * a question about it, a few minutes each. The lesson stays one lesson (its run, its end, the path); the piece is what
 * Dnes and the path count ("kúsok 2 z 5") and the step bar is cut into. Its end asks for nothing (Teo,
 * 19. 11.: a panel "Kúsok 3 z 6 máš za sebou" with "Stačí na dnes" at every end would annoy): the lesson closes
 * whenever the student closes it and opens again on that very step.
 */

/** A step that checks something: a question, an exercise, a definition with its examples, a proof. */
private fun Step.checks() = this is Step.Check || this is Step.Practice || this is Step.Def || this is Step.Prove

/**
 * The pieces of [l] as ranges of its steps: a piece closes on a step that checks something once it has at least three
 * steps; a tail of fewer than three joins the piece before it. Every piece but the last ends on such a step.
 */
fun pieces(l: LessonDef): List<IntRange> {
    val out = mutableListOf<IntRange>()
    var from = 0
    l.steps.forEachIndexed { i, s -> if (s.checks() && i - from + 1 >= 3) { out += from..i; from = i + 1 } }
    if (from < l.steps.size) {
        if (out.isNotEmpty() && l.steps.size - from < 3) out[out.lastIndex] = out.last().first until l.steps.size else out += from until l.steps.size
    }
    return out
}

/** The index of the piece step [at] is in (the last piece for the end of the lesson). */
fun pieceOf(l: LessonDef, at: Int): Int = pieces(l).indexOfFirst { at in it }.let { if (it < 0) pieces(l).lastIndex else it }

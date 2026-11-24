package dev.hamper.tabula.data

/*
 * A statement is read once (Teo, 24. 11.: "prečo sa opakujú definície hneď po sebe"): an explanation that rests on a
 * definition or a theorem shows its card, and where the step right beside it IS that definition or theorem, the same
 * text stood there in full twice in a row. The step of the item carries the statement; the explanation keeps its own
 * words. Decided here from the lesson, so every lesson follows it; the steps themselves and their ids do not change.
 */

/** The statements a step shows whole; a card of one point ("id#3") is not the whole statement. */
fun statementsOf(s: Step): Set<String> = when (s) {
    is Step.Def -> setOf(s.concept)
    is Step.Thm -> (listOf(s.theorem) + s.also).filter { '#' !in it }.toSet()
    is Step.Explain -> s.cards.filter { '#' !in it }.toSet()
    else -> emptySet()
}

/** The step as the lesson shows it: an explanation without the cards that a step beside it shows whole. */
fun shownStep(l: LessonDef, i: Int): Step {
    val s = l.steps[i] as? Step.Explain ?: return l.steps[i]
    val items = listOfNotNull(l.steps.getOrNull(i - 1), l.steps.getOrNull(i + 1)).filter { it !is Step.Explain }.flatMap { statementsOf(it) }
    // of two explanations in a row the first one shows it
    val before = (l.steps.getOrNull(i - 1) as? Step.Explain)?.let { statementsOf(shownStep(l, i - 1)) }.orEmpty()
    val left = s.cards.filter { it !in items && it !in before }
    return if (left.size == s.cards.size) s else s.copy(cards = left)
}

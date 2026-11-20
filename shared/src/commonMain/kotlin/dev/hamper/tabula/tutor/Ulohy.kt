package dev.hamper.tabula.tutor

import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.la.GemFrame
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.la.Mat

/*
 * Which GEMs an exercise asks for on paper, read from its own solution: every GEM chain there is a GemWalk, and a walk
 * computed from numbers knows its field. No list of templates to keep in sync: a new generator with a numeric GEM in
 * its solution gets the tutor by itself; one with a parameter (or symbols) in any of its walks does not (yet).
 */

/**
 * The GEMs of [ex] as the student would start them on paper; empty when the exercise has no GEM or has one with a
 * parameter. [answers]: the parts answered so far (the solution of some exercises depends on them).
 */
fun gemUlohy(ex: Exercise, answers: List<Any> = emptyList()): List<GemUloha> {
    val walks = runCatching { ex.solution(answers).steps.flatMap { s -> s.math.mapNotNull { it.gem } } }.getOrElse { return emptyList() }
    if (walks.isEmpty() || walks.any { it.pole == null }) return emptyList()
    val out = ArrayList<GemUloha>()
    for (w in walks) {
        val f = w.pole!!
        val start = cisla(w.start, f) ?: return emptyList()
        // a walk that continues another one (the way back of (A|E) ∼ (E|A⁻¹)) is not a GEM of its own
        if (walks.any { o -> o !== w && o.frames.last() == w.start && o.frames.size > 1 }) continue
        val end = koniec(w, walks)
        val bar = w.start.bar
        val ciel = if (bar != null && bar == w.start.m && end.cells.indices.all { i -> (0 until bar).all { j -> end.cells[i][j].trim() == if (i == j) "1" else "0" } })
            GemUloha.Ciel.JEDNOTKOVA else GemUloha.Ciel.HST
        val u = GemUloha(f, Mat(start, bar), ciel, ex.template)
        if (out.none { it.start == u.start && it.pole == u.pole }) out += u
    }
    return out
}

/** The last matrix of a chain, following walks that continue it. */
private fun koniec(w: GemWalk, all: List<GemWalk>): GemFrame {
    var cur = w
    repeat(4) { cur = all.firstOrNull { it !== cur && it.start == cur.frames.last() && cur.frames.size > 1 } ?: return cur.frames.last() }
    return cur.frames.last()
}

internal fun cislaRamca(fr: GemFrame, f: dev.hamper.tabula.la.Field) = cisla(fr, f)

private fun cisla(fr: GemFrame, f: dev.hamper.tabula.la.Field): List<List<dev.hamper.tabula.la.Frac>>? = fr.cells.map { r -> r.map { bunka(it, f) ?: return null } }

/**
 * What the explanation needs to know about the exercise on screen: the assignment, the part asked, the student's
 * answer, the right one and the feedback the app already gave on it (the recognised mistake; the tutor adds to it).
 */
fun kontext(ex: Exercise, answers: List<Any>, odpovedStudenta: String?, spatnaVazba: String?): Kontext {
    val part = runCatching { ex.parts.getOrNull(answers.size)?.invoke(answers) }.getOrNull()
    val spravna = when (val a = part?.answer) {
        is dev.hamper.tabula.data.Answer.Numbers -> a.shown
        is dev.hamper.tabula.data.Answer.Choice -> a.options[a.correct]
        null -> ""
        else -> "(pozri riešenie)"
    }
    // the solution's sentences after its last GEM: how the course reads the result off the matrix
    // the solution's sentence right after its last GEM (how the course reads the result off the matrix) and where
    // that matrix has its first nonzero elements: the sentence transfers to the student's matrix only when they agree
    var vedenie: List<Int>? = null
    val zaver = runCatching {
        val steps = ex.solution(answers).steps
        val last = steps.indexOfLast { s -> s.math.any { it.gem != null } }
        vedenie = steps.getOrNull(last)?.math?.lastOrNull { it.gem != null }?.gem?.frames?.last()?.leads?.filterNotNull()
        if (last < 0) emptyList() else steps.drop(last + 1).take(1).map { it.say }.filter { it.isNotBlank() && !it.trimEnd().endsWith(":") }
    }.getOrDefault(emptyList())
    // the solution's sentence transfers only to an exercise with one GEM ending in HST (not an inverse, not several parts)
    val ulohy = gemUlohy(ex, answers)
    val jedna = ulohy.size == 1 && ulohy[0].ciel == GemUloha.Ciel.HST
    // which step of the method each GEM belongs to, and which the question is about: a photo of another part says so
    val krokUlohy = ulohy.map { u -> runCatching {
        ex.solution(answers).steps.firstOrNull { st -> st.math.any { fm -> fm.gem?.let { w -> w.pole == u.pole && cislaRamca(w.start, u.pole) == u.start.rows } == true } }
            ?.let { st -> ex.postup.aims.indexOf(st.aim).takeIf { it >= 0 } }
    }.getOrNull() }
    // the answer typed against the right one, component by component (a typing slip shows as one component off)
    val rozdiel = (part?.answer as? dev.hamper.tabula.data.Answer.Numbers)?.let { a ->
        val typed = odpovedStudenta?.split(',')?.map { it.trim() } ?: return@let null
        if (typed.size != a.expected.size) return@let null
        val f = ulohy.firstOrNull()?.pole ?: dev.hamper.tabula.la.Field.R
        val zle = a.expected.indices.filter { i -> bunka(typed[i], f) != a.expected[i] }
        if (zle.isEmpty() || zle.size == a.expected.size) null
        else "Odpoveď v appke sa od správnej líši len v ${zle.joinToString(", ") { "$${a.labels[it]}$" }}: " +
            zle.joinToString("; ") { "zadané ${typed[it]}, správne $" + a.expected[it].tex(true) + "$" } + "." +
            // a number that is no element of ℤp at all: the slip is "not reduced mod p", not a wrong computation
            (f as? dev.hamper.tabula.la.Field.Zp)?.let { z -> zle.mapNotNull { i -> typed[i].toIntOrNull()?.takeIf { (it < 0 || it >= z.p) && bunka((((it % z.p) + z.p) % z.p).toString(), f) == a.expected[i] } }
                .takeIf { it.isNotEmpty() }?.let { bad -> " V $\\mathbb{Z}_{${z.p}}$ sú len čísla 0 až ${z.p - 1}: ${bad.joinToString(", ")} treba zapísať ako zvyšok po delení ${z.p}." } }.orEmpty()
    }
    val num = part?.answer as? dev.hamper.tabula.data.Answer.Numbers
    val zle = num?.let { a ->
        val typed = odpovedStudenta?.split(',')?.map { it.trim() }?.takeIf { it.size == a.expected.size } ?: return@let emptyList()
        val f = ulohy.firstOrNull()?.pole ?: dev.hamper.tabula.la.Field.R
        a.expected.indices.filter { i -> bunka(typed[i], f) != a.expected[i] }
    }.orEmpty()
    return Kontext(ex.prompt, part?.q ?: "", odpovedStudenta, spravna, spatnaVazba, ex.postup.aims.map { it.title + ": " + it.how }, ulohy,
        if (jedna) zaver else emptyList(), if (jedna) vedenie else null, krokUlohy, part?.aim, ex.postup.aims.map { it.title }, rozdiel,
        num?.expected, num?.labels.orEmpty(), zle)
}

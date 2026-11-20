package dev.hamper.tabula.tutor

import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.Concept
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.Exercise
import dev.hamper.tabula.data.Line
import dev.hamper.tabula.data.PaperPart
import dev.hamper.tabula.data.Proof
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.data.Solution
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.data.Theorem
import dev.hamper.tabula.data.TheoremById
import dev.hamper.tabula.data.lineText

/*
 * Where the student is, as text for the tutor (docs/TUTOR.md, "Všeobecný tútor"): any step, card, exercise or test
 * problem of the app, serialized from the app's own model, with what the tutor's tools need to check the student's work
 * there (the GEMs the exercise asks for, the checklist on paper). No kind of task needs its own code: a place the
 * serializer knows nothing special about is still its texts.
 */

/**
 * One place in the app. [opis]: what the screen shows and what is behind it (model solution, why) as markdown;
 * [kurz]: "la1" or "dml"; [gem]: the exercise's GEMs (the tool that reads and checks a photographed GEM needs them);
 * [papier]: the checklist on paper (the tool that judges a written proof, definition or test problem); [odpoved]:
 * what the student entered and what the app said to it.
 */
data class MiestoAppky(
    val nazov: String,
    val kurz: String,
    val opis: String,
    val gem: Kontext? = null,
    val papier: UlohaNaPapier? = null,
    val odpoved: String? = null,
    /** The chain the student photographed, checked by the engine (after the GEM tool), for `<postup krok="n"/>`. */
    val route: String = "",
    /** A paper problem whose part has many right results (OwnBasis): the student's own basis is checked and the later
     *  parts computed by the same functions as the box "Máš inú bázu?" under its solution. */
    val priklad: dev.hamper.tabula.data.PaperProblem? = null,
)

private fun t(s: String?) = s.orEmpty().replace('§', '$').replace(Regex("""\\softbreak(\[\d+\])?"""), " ")
private fun lines(ls: List<Line>) = ls.joinToString("\n") { "> " + t(lineText(it)) }

internal fun riesenie(s: Solution): String = buildString {
    appendLine("Postup kurzu (Ako na to?): " + s.postup.aims.joinToString(" → ") { "${it.title} (${it.how})" })
    for ((aim, steps) in s.groups) {
        appendLine("- **${aim.title}**")
        for (st in steps) {
            appendLine("  - ${t(st.say)}")
            st.math.forEach { f -> appendLine("    $$ ${t(f.tex)} $$" + (f.note?.let { "  (${t(it)})" } ?: "")) }
        }
    }
}

private fun veta(th: Theorem) = "**${th.name}** (${th.src})\n" + lines(th.czech) + "\nMyšlienka: ${t(th.idea)}" + (th.note?.let { "\nPoznámka: ${t(it)}" } ?: "")
private fun definicia(k: Concept) = "**${k.name}** (${k.src})\n" + lines(k.czech) +
    (if (k.why.isNotEmpty()) "\nPrečo každá časť:\n" + k.why.joinToString("\n") { "- „${t(it.part)}“: ${t(it.because)}" } else "")
private fun dokaz(p: Proof) = "**${p.title}** (${p.src})\nDokazuje sa: ${t(p.task)}\nZačiatok: ${t(p.given)}\n" +
    p.rounds.mapIndexed { i, r -> "- ${p.goals.getOrNull(i) ?: "Krok ${i + 1}"}: ${t(r.line)}" }.joinToString("\n") + "\nZáver: ${t(p.end)}" + (p.warn?.let { "\nPozor: ${t(it)}" } ?: "")

private fun kurzKroku(route: String) = dev.hamper.tabula.data.courseOfRoute(route)?.id ?: "la1"

/** A step of a lesson: its texts, and for a check its options and why. */
fun miesto(lekcia: String, i: Int, s: Step, nazovLekcie: String): MiestoAppky {
    val route = "lesson:$lekcia:$i"
    val kurz = kurzKroku(route)
    val opis = buildString {
        appendLine("Lekcia „$nazovLekcie“, krok ${i + 1}.")
        when (s) {
            is Step.Explain -> { appendLine("Výklad: ${t(s.title)}"); appendLine(t(s.body)); s.formal?.let { appendLine("Karta zo skrípt (${s.src.orEmpty()}):"); appendLine(lines(it)) }; s.more?.let { appendLine("Viac: ${t(it)}") } }
            is Step.Worked -> { appendLine("Vzorový príklad: ${t(s.title)}"); appendLine("Zadanie: ${t(s.problem)}"); append(riesenie(s.solution)) }
            is Step.Check -> { appendLine("Kontrolná otázka: ${t(s.q)}"); s.options.forEachIndexed { k, o -> appendLine("  ${'a' + k}) ${t(o)}" + if (k == s.correct) "  (správna)" else "") }; appendLine("Vysvetlenie: ${t(s.why)}"); s.wrong.forEach { (k, v) -> appendLine("Pri ${'a' + k}): ${t(v)}") } }
            is Step.Ask -> { appendLine("Otázka pred výkladom: ${t(s.q)}"); s.options.forEachIndexed { k, o -> appendLine("  ${'a' + k}) ${t(o)}" + if (k == s.correct) "  (správna)" else "") }; appendLine("Potom: ${t(s.after)}") }
            is Step.Explore -> { appendLine("Obrázok na skúšanie: ${t(s.title)}"); appendLine(t(s.sub)); appendLine("Po splnení: ${t(s.doneTitle)} ${t(s.doneBody)}") }
            is Step.Def -> ConceptById[s.concept]?.let { appendLine("Definícia:"); appendLine(definicia(it)); appendLine("Na papier sa píše z pamäti; kontrolný zoznam: " + it.units.joinToString("; ") { u -> t(u) }) }
            is Step.Thm -> TheoremById[s.theorem]?.let { appendLine("Veta:"); appendLine(veta(it)) }
            is Step.Prove -> ProofById[s.proof]?.let { appendLine("Dôkaz (najprv po krokoch v appke, potom celý na papier):"); appendLine(dokaz(it)) }
            is Step.Practice -> appendLine("Príklad z generátora ${s.gen} (otvorí sa ako príklad).")
            is Step.Clip -> appendLine("Klip z prednášky ${s.clip}.")
        }
    }
    val papier = when (s) {
        is Step.Def -> ConceptById[s.concept]?.takeIf { it.units.isNotEmpty() }?.let { naPapier(it) }
        is Step.Prove -> ProofById[s.proof]?.let { naPapier(it, polozkyDokazu(it)) }
        is Step.Thm -> TheoremById[s.theorem]?.proofs?.firstOrNull()?.let { naPapier(it, polozkyDokazu(it)) }
        else -> null
    }
    return MiestoAppky(nazovLekcie, kurz, opis, papier = papier, route = route)
}

/** A card of a definition or theorem opened on its own. */
fun miesto(k: Concept): MiestoAppky = MiestoAppky(k.name, kurzKroku("concept:${k.id}"), "Karta definície:\n" + definicia(k), papier = k.takeIf { it.units.isNotEmpty() }?.let { naPapier(it) }, route = "concept:${k.id}")
fun miesto(th: Theorem): MiestoAppky = MiestoAppky(th.name, kurzKroku("theorem:${th.id}"), "Karta vety:\n" + veta(th), papier = th.proofs.firstOrNull()?.let { naPapier(it, polozkyDokazu(it)) }, route = "theorem:${th.id}")

/** A proof on paper (its checklist on screen). */
fun miesto(pr: Proof, polozky: List<String>): MiestoAppky =
    MiestoAppky(pr.title, kurzKroku("proof:${pr.id}"), "Dôkaz na papier.\n" + dokaz(pr) + "\nKontrolný zoznam na obrazovke:\n" + polozky.joinToString("\n") { "- " + t(it) },
        papier = naPapier(pr, polozky), route = "proof:${pr.id}")

/**
 * An exercise: its prompt, the parts answered, the part on screen and, if the student answered it wrong, their answer
 * and the app's feedback; the model solution (for the tutor, never to be given away unasked).
 */
fun miesto(ex: Exercise, answers: List<Any>, odpoved: String?, vazba: String?, kurz: String): MiestoAppky {
    val sol = runCatching { ex.solution(answers) }.getOrNull()
    val part = runCatching { ex.parts.getOrNull(answers.size)?.invoke(answers) }.getOrNull()
    val opis = buildString {
        appendLine("Príklad: ${ex.label}")
        appendLine("Zadanie: ${t(ex.prompt)}"); ex.visual?.let { appendLine("$$ ${t(it)} $$") }
        for (i in answers.indices) runCatching { ex.parts[i](answers.take(i)) }.getOrNull()?.let { p -> appendLine("Hotová časť: ${t(p.q)} (zodpovedané správne)") }
        part?.let { p ->
            appendLine("Časť na obrazovke: ${t(p.q)}")
            when (val a = p.answer) {
                is Answer.Numbers -> appendLine("Správna odpoveď (appka ju ukáže po odpovedi): ${t(a.shown)}")
                is Answer.Choice -> { a.options.forEachIndexed { k, o -> appendLine("  ${'a' + k}) ${t(o)}" + if (k == a.correct) "  (správna)" else "") }; appendLine("Prečo: ${t(a.why)}") }
                is Answer.Paper -> { appendLine("Píše sa na papier. Vzor: " + a.sample.joinToString(" ") { t(it) }); appendLine("Na čo sa pozerá cvičiaci: " + a.look.joinToString("; ") { t(it) }) }
                else -> {}
            }
            p.hint?.let { appendLine("Nápoveda appky: ${t(it)}") }
        }
        odpoved?.let { appendLine("ŠTUDENT ODPOVEDAL: $it") }
        vazba?.let { appendLine("APPKA MU POVEDALA: ${t(it)}") }
        sol?.let { appendLine("VZOROVÉ RIEŠENIE (neprezrádzaj ho, kým oň študent nepožiada):"); append(riesenie(it)) }
    }
    val gem = kontext(ex, answers, odpoved, vazba).takeIf { it.ulohy.isNotEmpty() }
    // a part written on paper is judged from a photo against its model, as a test problem's part is
    val papier = (part?.answer as? Answer.Paper)?.let { a ->
        UlohaNaPapier("uloha", ex.label, t(ex.prompt) + "\nTáto časť: " + t(part.q), vzor = a.sample.map { t(it) }, body = a.look.map { t(it) },
            pravidla = KurzKontext.zhrnutie(kurz).take(8), zdroj = ex.src)
    }
    return MiestoAppky(ex.label, kurz, opis, gem = gem, odpoved = odpoved, papier = papier, route = "gen:${ex.template}")
}

/** A part of a test problem on paper: its text, rubric and model solution. */
fun miesto(nazov: String, part: PaperPart, kurz: String, celyPriklad: String? = null, priklad: dev.hamper.tabula.data.PaperProblem? = null): MiestoAppky {
    val opis = buildString {
        appendLine("Príklad z písomky na papier: $nazov")
        celyPriklad?.takeIf { it != part.text }?.let { appendLine("Celé zadanie príkladu: ${t(it).replace("¦", "")}") }
        appendLine("Táto časť: ${t(part.text)}")
        appendLine("Rubrika (body): " + part.checklist.joinToString("; ") { "${t(it.text)} (${it.pts} b)" })
        part.solution?.let { appendLine("VZOROVÉ RIEŠENIE:"); append(riesenie(it)) } ?: part.answer?.let { appendLine("Výsledok: ${t(it)}") }
    }
    return MiestoAppky(nazov, kurz, opis, papier = naPapier(nazov, part, kurz, celyPriklad), route = "paper", priklad = priklad?.takeIf { it.own != null })
}

/** A place by its route (the same routes as the app and :app:snap): lesson:<id>:<k>, concept:, theorem:, proof:. */
fun miestoZRouty(route: String): MiestoAppky? {
    val p = route.split(':')
    return when (p[0]) {
        "lesson" -> dev.hamper.tabula.data.Courses.flatMap { it.lessons }.firstOrNull { it.id == p.getOrNull(1) }?.let { l -> p.getOrNull(2)?.toIntOrNull()?.let { i -> l.steps.getOrNull(i)?.let { miesto(l.id, i, it, l.name) } } }
        "step" -> dev.hamper.tabula.data.Courses.flatMap { it.lessons }.firstNotNullOfOrNull { l -> l.steps.indexOfFirst { it.id == p.getOrNull(1) }.takeIf { it >= 0 }?.let { i -> miesto(l.id, i, l.steps[i], l.name) } }
        "concept" -> ConceptById[p.getOrNull(1)]?.let { miesto(it) }
        "theorem" -> TheoremById[p.getOrNull(1)]?.let { miesto(it) }
        "proof" -> ProofById[p.getOrNull(1)]?.let { miesto(it, polozkyDokazu(it)) }
        // paper:<id>:<n>[:<part from 0>]
        "paper" -> dev.hamper.tabula.data.Courses.firstNotNullOfOrNull { c -> c.papers.firstOrNull { it.id == p.getOrNull(1) }?.let { c to it } }?.let { (c, paper) ->
            val pr = paper.problems.firstOrNull { it.n == p.getOrNull(2)?.toIntOrNull() } ?: return null
            val j = p.getOrNull(3)?.toIntOrNull() ?: 0
            val part = pr.units.getOrNull(j) ?: return null
            miesto("${paper.name}, príklad ${pr.n}" + (if (part.label.isNotEmpty()) ", časť ${part.label}" else ""), part, c.id, pr.text, pr)
        }
        else -> null
    }
}

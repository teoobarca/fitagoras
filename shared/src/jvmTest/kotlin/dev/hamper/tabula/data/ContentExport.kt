package dev.hamper.tabula.data

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.dml.DmlInduction
import dev.hamper.tabula.data.dml.DmlNumbers
import dev.hamper.tabula.data.dml.DmlSets
import dev.hamper.tabula.data.la1.La1Chapter3
import dev.hamper.tabula.data.la1.La1Chapter4
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.ui.firstSentence
import java.io.File
import kotlin.random.Random

/*
 * The text of every lesson of LA1 and DML as markdown, one file per chapter, for the LLM checks (the kontroly folder,
 * scripts/llm-kontrola.sh): steps, definitions, theorems, proofs, worked and model solutions, every feedback with what
 * is behind "Prečo?", the references a text makes and what they open, and a few exercises of every generator.
 * Each item carries its route (the same as :app:snap and the app use) and the source line it is written on.
 *
 * ./gradlew :shared:exportContent -Pexport.out=<folder> [-Pexport.seeds=2]
 */

/** One chapter: what its pack in kurz/ covers and what the app has for it. */
class ExportChapter(
    val id: String,
    val course: Course,
    val title: String,
    /** The chapter pack, relative to the repository root. */
    val bundle: String,
    val lessons: List<LessonDef>,
    val concepts: List<Concept>,
    val theorems: List<Theorem>,
    val proofs: List<Proof>,
    val generators: Map<String, (Random) -> Exercise>,
    /** Problems of past papers: route ("paper:dml-2025-e:3") to the problem. */
    val papers: List<Pair<String, PaperProblem>>,
    val generated: ((Random) -> PaperProblem)?,
    val clips: String,
)

private fun topicPapers(course: Course, t: Topic): List<Pair<String, PaperProblem>> = course.papers.flatMap { paper ->
    paper.problems.filter { p -> t.paperProblems[paper.id]?.text == p.text }.map { "paper:${paper.id}:${it.n}" to it }
}

private fun topicChapter(course: Course, t: Topic, title: String, bundle: String) = ExportChapter(
    t.id, course, title, bundle, t.lessons, t.concepts, t.theorems, t.proofs, t.generators, topicPapers(course, t), t.generatedProblem, "clips-${t.id}.json",
)

/** The chapters that have lessons, in course order, each with its pack. */
val ExportChapters: List<ExportChapter> by lazy {
    listOf(
        ExportChapter(
            "la1-k2", La1, "LA1, kapitola 2: Základní pojmy lineární algebry", "kurz/la1/kap2-zakladni-pojmy.md",
            La1Lessons, La1Concepts, La1Theorems, La1Proofs, La1Generators,
            La1Papers.flatMap { p -> p.problems.map { "paper:${p.id}:${it.n}" to it } }, null, "clips.json",
        ),
        topicChapter(La1, La1Chapter3, "LA1, kapitola 3: Hodnost a regularita matic", "kurz/la1/kap3-hodnost-regularita.md"),
        topicChapter(La1, La1Chapter4, "LA1, kapitola 4: Frobeniova věta a lineární variety", "kurz/la1/kap4-frobenius.md"),
        topicChapter(Dml, DmlInduction, "DML, kapitola 4: Důkazy a matematická indukce", "kurz/dml/kap4-dukazy-indukce.md"),
        topicChapter(Dml, DmlSets, "DML, kapitola 5: Množiny a zobrazení", "kurz/dml/kap5-mnoziny-zobrazeni.md"),
        topicChapter(Dml, DmlNumbers, "DML, kapitola 6: Teorie čísel", "kurz/dml/kap6-teorie-cisel.md"),
    )
}

/** Where things are written: the first line that declares an id, and the lines of a lesson's steps. */
private class Sources(root: File) {
    private val base = root.absoluteFile.parentFile.parentFile.parentFile // shared/src/commonMain -> repository root
    private val files = root.walkTopDown().filter { it.isFile && it.extension == "kt" && "/data/" in it.invariantSeparatorsPath }
        .sortedBy { it.path }.map { it to it.readLines() }.toList()
    private val uses = Regex("""\b(def|thm|prove|clip|practice|getValue|ById\[)\(?\s*"$""")
    private fun rel(f: File) = f.absoluteFile.relativeTo(base).invariantSeparatorsPath

    /**
     * Where [id] is declared: the first line matching the declaration patterns of its [kind] (in this order), else the
     * first line that names it and is not a use (def("id"), practice("id"), ...).
     */
    fun of(id: String, kind: String): String? {
        val q = Regex.escape("\"$id\"")
        val decl = Regex("""(Concept|Theorem|Proof)\(\s*$q""")
        val named = Regex("""\bid\s*=\s*$q""")
        val lead = Regex("""^\s*$q\s*,""")
        val patterns = when (kind) {
            "lesson" -> listOf(lessonCall.pattern.replace("([^\"]+)", Regex.escape(id)).let(::Regex))
            "gen" -> listOf(Regex("""$q\s+to\b"""))
            else -> listOf(decl, named, lead)
        }
        for (re in patterns) for ((f, lines) in files) lines.forEachIndexed { i, l -> if (re.containsMatchIn(l)) return "${rel(f)}:${i + 1}" }
        val plain = "\"$id\""
        for ((f, lines) in files) lines.forEachIndexed { i, l ->
            val at = l.indexOf(plain)
            if (at >= 0 && !uses.containsMatchIn(l.substring(0, at + 1))) return "${rel(f)}:${i + 1}"
        }
        return null
    }

    private val stepCall = Regex("""^\s+(ask|explore|explain|worked|check|practice|clip|def|thm|prove)\(""")
    private val lessonCall = Regex("""\blesson\(\s*"([^"]+)"""")

    /**
     * One location per step: the step calls at the indent of the lesson's first one (calls nested deeper, such as a
     * subgoal named check inside a worked example, are not steps). When their count differs from the steps (a helper
     * makes steps), every step gets the lesson's line.
     */
    fun steps(lesson: LessonDef): List<String?> {
        for ((f, lines) in files) {
            val start = lines.indexOfFirst { lessonCall.find(it)?.groupValues?.get(1) == lesson.id }
            if (start < 0) continue
            val end = (start + 1 until lines.size).firstOrNull { lessonCall.containsMatchIn(lines[it]) } ?: lines.size
            val all = (start + 1 until end).filter { stepCall.containsMatchIn(lines[it]) }
            val indent = all.minOfOrNull { lines[it].length - lines[it].trimStart().length }
            val calls = all.filter { lines[it].length - lines[it].trimStart().length == indent }
            return if (calls.size == lesson.steps.size) calls.map { "${rel(f)}:${it + 1}" } else lesson.steps.map { "${rel(f)}:${start + 1} (lekcia)" }
        }
        return lesson.steps.map { null }
    }
}

class ContentExporter(private val seeds: Int = 2) {
    private val src = Sources(mainSources())
    private val sb = StringBuilder()
    private lateinit var refs: RefIndex

    // texts that skip m() (a theorem's idea, clips) still write math as §…§
    // a soft break (\softbreak[k]) is the app's own line breaking, not TeX: the export writes the formula on one line
    private fun ln(s: String = "") { sb.append(s.replace('§', '$').replace(Regex("""\\softbreak(\[\d+\])?"""), " ")).append('\n') }
    private fun at(where: String?) = where?.let { " · `$it`" } ?: ""

    private fun lines(ls: List<Line>, indent: String = "") = ls.forEach { l ->
        when (l) {
            is Para -> ln("$indent${l.tex}")
            is Item -> ln("$indent- ${l.tex}")
            is Block -> ln("$indent$$ ${l.tex} $$")
        }
    }

    /** An explanation shown after an answer: its first sentence at once, the rest behind "Prečo?" (CONTENT.md, first sentence rule). */
    private fun why(label: String, text: String?) {
        if (text.isNullOrBlank()) return
        val (head, rest) = firstSentence(text)
        if (rest == null) ln("  - $label: $head  _(bez „Prečo?“)_")
        else { ln("  - $label: $head"); ln("    - za „Prečo?“: $rest") }
    }

    private val seenRefs = LinkedHashMap<String, String>()
    /** The ids the screen shows itself: the app does not mark references to them there (LocalReadingSelf, Steps.kt). */
    private var self: Set<String> = emptySet()
    private fun theoremOf(p: Proof) = TheoremById.values.firstOrNull { it.proof?.id == p.id }
    /** Collect the numbered references of a text for the step's "Odkazy" line. */
    private fun r(text: String?): String? {
        if (text != null) for (h in refs.scan(text)) {
            if (h.target?.id in self) continue
            val name = text.substring(h.range)
            seenRefs.putIfAbsent(name, h.target?.let { t -> if (t is RefTarget.Quote) "otvorí citát zo skrípt (${t.n.name})" else "otvorí `${t.route}`" } ?: if (h.entry != null) "bez karty, nedá sa ťuknúť" else "neznáme číslo, nedá sa ťuknúť")
        }
        return text
    }
    private fun flushRefs() {
        if (seenRefs.isNotEmpty()) ln("- Odkazy v kroku: " + seenRefs.entries.joinToString("; ") { (k, v) -> "„$k“ → $v" })
        seenRefs.clear()
        self = emptySet()
    }

    /** How the card says the theorem is proved: StatementCard shows a short proof folded under it, an omitted one as "Bez dôkazu". */
    private fun proven(t: Theorem, where: String = ""): String = when (val p = t.proven) {
        is Proven.Lines -> (listOf(p.proof) + p.more).joinToString(", ") { "`proof:${it.id}`" } + where
        is Proven.Short -> "krátky, zbalený pod kartou „Dôkaz“ (${p.src}): ${r(p.argument)}"
        is Proven.Omitted -> "bez dôkazu, karta píše „Bez dôkazu: ${r(p.why)}“ (${p.src})"
    }

    private fun pic(p: Pic?) = p?.let { if (it is Pic.Custom) "obrázok: ${it.describe}" else "obrázok: $it" }

    private fun solution(s: Solution, indent: String = "") {
        ln("${indent}Postup (karta „Ako na to?“, zbalená): " + s.postup.aims.joinToString(" → ") { "${it.title} (${it.how})" })
        for ((aim, steps) in s.groups) {
            ln("$indent- **${aim.title}**")
            for (st in steps) {
                ln("$indent  - ${r(st.say)}")
                st.math.forEach { f -> ln("$indent    $$ ${f.tex} $$" + (f.note?.let { "  ← ${r(it)}" } ?: "")) }
            }
        }
    }

    private fun options(opts: List<String>, correct: Int) = opts.forEachIndexed { i, o -> ln("  ${'a' + i}) ${r(o)}" + if (i == correct) "  ✅" else "") }

    private fun step(i: Int, s: Step, lesson: LessonDef, where: String?, chapter: ExportChapter) {
        val route = "lesson:${lesson.id}:$i"
        when (s) {
            is Step.Ask -> {
                ln("#### $i. Otázka pred výkladom · `$route`${at(where)}")
                ln("- Otázka: ${r(s.q)}"); s.sub?.let { ln("- Pod otázkou: ${r(it)}") }; pic(s.pic)?.let { ln("- $it") }
                options(s.options, s.correct)
                // AskStep (Steps.kt) shows after and, for a wrong guess, its wrong text as one explanation, then splits it
                why("po správnej alebo „Neviem“", r(s.after))
                s.wrong.toSortedMap().forEach { (k, v) -> why("pri zlej ${'a' + k})", r(s.after + " " + r(v))) }
            }
            is Step.Explore -> {
                ln("#### $i. Obrázok na skúšanie · `$route`${at(where)}")
                ln("- ${r(s.title)}"); ln("- ${r(s.sub)}"); pic(s.pic)?.let { ln("- $it") }
                ln("- Po splnení: ${r(s.doneTitle)}"); s.doneBody?.let { ln("- ${r(it)}") }
            }
            is Step.Explain -> {
                ln("#### $i. Výklad: ${r(s.title)} · `$route`${at(where)}")
                ln(r(s.body)!!); pic(s.pic)?.let { ln("- $it") }
                s.formal?.let { ln("- Karta zo skrípt (${s.src ?: "bez zdroja"}):"); it.forEach { l -> r((l as? Para)?.tex ?: (l as? Item)?.tex) }; lines(it, "  > ") }
                if (s.formal == null) s.src?.let { ln("- Zdroj: $it") }
                s.more?.let { ln("- Viac: ${r(it)}") }
            }
            is Step.Worked -> {
                ln("#### $i. Vzorový príklad: ${r(s.title)} · `$route`${at(where)}")
                ln("- Zadanie: ${r(s.problem)}"); s.src?.let { ln("- Zdroj: $it") }
                solution(s.solution, "  ")
            }
            is Step.Check -> {
                ln("#### $i. Kontrolná otázka · `$route`${at(where)}")
                ln("- Otázka: ${r(s.q)}"); s.sub?.let { ln("- Pod otázkou: ${r(it)}") }; pic(s.pic)?.let { ln("- $it") }
                s.hint?.let { ln("- Nápoveda: ${r(it)}") }
                options(s.options, s.correct)
                s.wrong.toSortedMap().forEach { (k, v) -> why("pri zlej ${'a' + k}) najprv", r(v)) }
                why("vysvetlenie", r(s.why))
            }
            is Step.Practice -> ln("#### $i. Príklad z generátora `${s.gen}`${if (s.solo) " (bez pomoci)" else ""} · `$route`${at(where)}" + (s.title?.let { ": $it" } ?: "") + " (ukážky v oddiele Generátory)")
            is Step.Clip -> {
                ln("#### $i. Klip z prednášky `${s.clip}` · `$route`${at(where)}")
                clip(s.clip, chapter)
            }
            is Step.Def -> {
                val k = ConceptById[s.concept]
                ln("#### $i. Definícia `concept:${s.concept}` · `$route`${at(where)}")
                if (k != null) { ln("- ${k.name} (${k.src})"); lines(k.czech, "  > ") } else ln("- CHÝBA karta ${s.concept}")
            }
            is Step.Thm -> {
                val t = TheoremById[s.theorem]
                self = setOfNotNull(t?.id, t?.proof?.id)
                ln("#### $i. Veta `theorem:${s.theorem}` · `$route`${at(where)}")
                if (t != null) {
                    ln("- ${t.name} (${t.src})"); lines(t.czech, "  > "); ln("- Myšlienka: ${r(t.idea)}")
                    ln("- Dôkaz: " + proven(t, " (oddiel Dôkazy)"))
                } else ln("- CHÝBA karta ${s.theorem}")
            }
            is Step.Prove -> {
                val p = ProofById[s.proof]
                self = setOfNotNull(p?.id, p?.let { theoremOf(it) }?.id)
                ln("#### $i. Dôkaz `proof:${s.proof}` · `$route`${at(where)}")
                if (p != null) ln("- ${p.title}: ${r(p.task)} (celý v oddiele Dôkazy)") else ln("- CHÝBA dôkaz ${s.proof}")
            }
        }
        flushRefs()
        ln()
    }

    private val clipCache = HashMap<String, Map<String, JSONObject>>()
    private fun clip(id: String, chapter: ExportChapter) {
        val dir = File(mainSources(), "composeResources/files")
        val all = clipCache.getOrPut(chapter.clips) {
            dir.listFiles().orEmpty().filter { it.name.startsWith("clips") && it.extension == "json" }.flatMap { f ->
                val a = JSONArray(f.readText()); (0 until a.length()).map { a.getJSONObject(it) }
            }.associateBy { it.getString("id") }
        }
        val o = all[id] ?: run { ln("- CHÝBA klip $id"); return }
        ln("- ${o.optString("lecture")}, ${o.optString("teacher", chapter.course.clipTeacher)}, ${o.optString("rec")} od ${o.optDouble("from")} s do ${o.optDouble("to")} s")
        ln("- Pred klipom: ${r(o.optString("before"))}")
        ln("- O čom je: ${r(o.optString("why"))}")
        val cues = o.optJSONArray("cues")
        if (cues != null) {
            val words = (0 until cues.length()).joinToString(" ") { c ->
                val ws = cues.getJSONObject(c).optJSONArray("words"); if (ws == null) "" else (0 until ws.length()).joinToString(" ") { ws.getJSONObject(it).getString("w") }
            }
            ln("- Titulky (čo zaznie): $words")
        }
        val t = o.optJSONObject("task") ?: return
        ln("- Úloha po klipe: ${r(t.getString("q"))}")
        val opts = t.getJSONArray("options")
        options((0 until opts.length()).map { opts.getString(it) }, t.getInt("correct"))
        t.optJSONObject("wrong")?.let { w -> w.keys().asSequence().sorted().forEach { k -> why("pri zlej ${'a' + k.toInt()}) najprv", r(w.getString(k))) } }
        why("vysvetlenie", r(t.optString("why")))
    }

    private fun concept(k: Concept) {
        self = setOf(k.id)
        ln("### ${k.name} · `concept:${k.id}`${at(src.of(k.id, "concept"))}")
        ln("- Zdroj: ${k.src}"); ln("- Znenie (karta):"); k.czech.forEach { r((it as? Para)?.tex ?: (it as? Item)?.tex) }; lines(k.czech, "  > ")
        if (k.why.isNotEmpty()) { ln("- Prečo každá časť:"); k.why.forEach { ln("  - „${r(it.part)}“: ${r(it.because)}") } }
        if (k.judge.isNotEmpty()) { ln("- Príklady (${r(k.question).orEmpty().ifBlank { "Je to ono?" }}):"); k.judge.forEach { j -> ln("  - ${r(j.tex)} → ${if (j.yes) "áno" else "nie"}"); why("pri správnej", r(j.because)); why("pri zlej (appka spojí wrong + because)", r(j.wrong + " " + j.because)) } }
        k.trap?.let { ln("- Pasca: „${r(it.sentence)}“ (${it.src}) → ${r(it.because)}") }
        ln("- Úloha z pamäti: ${r(k.ask)}")
        ln("- Kontrolný zoznam (body, ktoré sa odškrtávajú):"); k.units.forEach { ln("  - ${r(it)}") }
        flushRefs(); ln()
    }

    private fun theorem(t: Theorem) {
        self = setOfNotNull(t.id, t.proof?.id)
        ln("### ${t.name} · `theorem:${t.id}`${at(src.of(t.id, "theorem"))}")
        ln("- Zdroj: ${t.src}"); ln("- Znenie (karta):"); t.czech.forEach { r((it as? Para)?.tex ?: (it as? Item)?.tex) }; lines(t.czech, "  > ")
        ln("- Myšlienka: ${r(t.idea)}")
        t.note?.let { ln("- Poznámka: ${r(it)}") }
        ln("- Dôkaz: " + proven(t))
        flushRefs(); ln()
    }

    private fun proof(p: Proof) {
        self = setOfNotNull(p.id, theoremOf(p)?.id)
        ln("### ${p.title} · `proof:${p.id}`${at(src.of(p.id, "proof"))}")
        ln("- Zdroj: ${p.src}"); ln("- Lišta „Dokazuješ“ / zadanie: ${r(p.task)}"); p.foot?.let { ln("- Pod zadaním: ${r(it)}") }
        ln("- Dané: ${r(p.given)}")
        if (p.goals.isNotEmpty()) ln("- Ciele: " + p.goals.joinToString(" → "))
        p.rounds.forEachIndexed { i, round ->
            ln("- Kolo ${i + 1}: ${r(round.prompt)}")
            round.options.forEachIndexed { j, o -> ln("  ${'a' + j}) ${r(o.tex)}" + if (j == round.correct) "  ✅" else ""); o.why?.let { w -> why("prečo nie", r(w)) } }
        }
        ln("- Záver: ${r(p.end)}")
        p.warn?.let { ln("- Pozor: ${r(it)}") }
        ln("- Kontrolný zoznam na papier:"); p.checklist.forEach { ln("  - ${r(it)}") }
        flushRefs(); ln()
    }

    private fun exercise(name: String, seed: Int, ex: Exercise) {
        ln("#### `gen:$name`, ukážka ${seed + 1} (${ex.label})")
        ln("- Zadanie: ${r(ex.prompt)}"); ex.visual?.let { ln("  $$ $it $$") }; ln("- Zdroj: ${ex.src}")
        ln("- Nápoveda: ${r(ex.hint)}")
        ln("- Ako na to?: " + ex.postup.aims.joinToString(" → ") { "${it.title} (${r(it.how)})" })
        val answers = ArrayList<Any>()
        for ((pi, build) in ex.parts.withIndex()) {
            val part = build(answers)
            ln("- Časť ${pi + 1}: ${r(part.q)}")
            when (val a = part.answer) {
                is Answer.Choice -> {
                    options(a.options, a.correct)
                    a.wrong.toSortedMap().forEach { (k, v) -> why("pri zlej ${'a' + k}) najprv", r(v)) }
                    why("vysvetlenie", r(a.why)); answers.add(a.correct)
                }
                is Answer.Numbers -> {
                    ln("  - Odpoveď: ${a.labels.joinToString(", ") { "$$it$" }} = ${r(a.shown)}")
                    val rnd = Random(seed)
                    a.diagnose?.let { d ->
                        repeat(2) {
                            val g = a.expected.map { e -> if (a.p != null) Frac.of(rnd.nextInt(a.p)) else e + Frac.of(rnd.nextInt(1, 4).toLong()) }
                            if (g != a.expected) why("diagnóza zlej odpovede $g", r(d(g)))
                        }
                    }
                    answers.add(a.expected)
                }
                is Answer.Pick -> {
                    a.options.forEachIndexed { i, o -> ln("  ${'a' + i}) ${r(o)}") }
                    val n = a.options.size
                    val sets = (0 until (1 shl n)).map { m -> (0 until n).filter { m shr it and 1 == 1 }.toSet() }.filter { it.size in a.count }
                    val ok = sets.first(a.ok)
                    ln("  - Správne napr.: ${ok.map { 'a' + it }}"); why("vysvetlenie", r(a.why(ok)))
                    sets.firstOrNull { !a.ok(it) }?.let { bad -> why("pri zlej voľbe ${bad.map { 'a' + it }}", r(a.why(bad))) }
                    answers.add(ok)
                }
                is Answer.Vectors -> {
                    ln("  - Zadáva ${a.entry} (${a.cells.joinToString(", ")}), počet ${a.count}")
                    ln("  - Vzorová odpoveď: ${a.example}")
                    why("verdikt pri správnej", r(a.check(a.example).head))
                    if (a.open && a.example.size > 1) why("verdikt pri neúplnej", r(a.check(a.example.drop(1)).head))
                    answers.add(a.example)
                }
                is Answer.Paper -> {
                    ln("  - Na papier; vzor: ${a.sample.joinToString(" ") { r(it).orEmpty() }}")
                    why("pri „Nemám“", r(a.miss))
                    answers.add(1)
                }
            }
            part.after?.let { ln("  - Potom: ${r(it)}") }
        }
        ln("- Riešenie (ako na papier):"); solution(ex.solution(answers), "  ")
        flushRefs(); ln()
    }

    private fun paper(route: String, p: PaperProblem, course: Course? = null) {
        ln("### ${p.title} · `$route` (${p.pts} b)")
        val paperId = route.split(':').getOrNull(1)
        course?.papers?.firstOrNull { it.id == paperId }?.let { ln("- Písomka: ${it.name}; zdroj: ${it.src}") }
        ln("- Zadanie: ${r(p.text)}"); p.note?.let { ln("- Pokyn: ${r(it)}") }
        for (u in p.units) {
            if (u.label.isNotEmpty()) ln("- Časť ${u.label} (${u.pts} b${if (u.estimate) ", odhad" else ""}): ${r(u.text)}")
            // the screen says the split is ours (Papers.kt, "Čo máš na papieri?"); without it every rubric read as the paper's own
            ln("  - Hodnotenie (appka pri ňom píše: ${if (u.estimate) "body tejto časti sú odhad, nie oficiálne bodovanie" else "rozdelenie bodov v príklade je náš odhad"}): " + u.checklist.joinToString("; ") { "${r(it.text)} (${it.pts} b)" + (it.fix?.let { f -> " → $f" } ?: "") })
            u.answer?.let { ln("  - Výsledok: ${r(it)}") }
            u.solution?.let { ln("  - Vzorové riešenie:"); solution(it, "    ") }
        }
        flushRefs(); ln()
    }

    private fun head(c: ExportChapter, what: String) {
        ln("# ${c.title} (`${c.id}`): $what")
        ln()
        ln("Balík kapitoly: `${c.bundle}`. Lekcií ${c.lessons.size}, krokov ${c.lessons.sumOf { it.steps.size }}, definícií ${c.concepts.size}, viet ${c.theorems.size}, dôkazov ${c.proofs.size}, generátorov ${c.generators.size}, príkladov z písomiek ${c.papers.size}.")
        ln("Matematika je v \$…\$ (TeX). Route (`lesson:<id>:<krok od 0>`, `concept:<id>`, …) je adresa obrazovky v appke a pre `:app:snap`; `súbor:riadok` je miesto v repe.")
        ln("Po odpovedi appka ukáže prvú vetu vysvetlenia, zvyšok je za „Prečo?“; pri každom vysvetlení je to rozdelené tak, ako ho rozdelí appka.")
        ln()
    }

    private fun take(): String = sb.toString().also { sb.setLength(0) }

    /** The chapter's four files: lessons, cards (definitions, theorems, proofs), generators, past papers. */
    fun chapter(c: ExportChapter): Map<String, String> {
        sb.setLength(0)
        refs = RefIndexes.getValue(c.course.id)
        val out = LinkedHashMap<String, String>()
        head(c, "lekcie")
        for (l in c.lessons) {
            ln("## Lekcia „${l.name}“ · `lesson:${l.id}`${at(src.of(l.id, "lesson"))}")
            ln("- Čo sa naučíš: ${l.learn} (${l.minutes} min, témy ${l.topics.joinToString(", ")})")
            ln()
            val wheres = src.steps(l)
            l.steps.forEachIndexed { i, s -> step(i, s, l, wheres[i], c) }
        }
        out["lekcie.md"] = take()
        head(c, "karty (definície, vety, dôkazy)")
        ln("## Definície"); ln(); c.concepts.forEach(::concept)
        ln("## Vety"); ln(); c.theorems.forEach(::theorem)
        val proofs = (c.proofs + c.theorems.mapNotNull { it.proof }).distinctBy { it.id }
        ln("## Dôkazy"); ln(); proofs.forEach(::proof)
        out["karty.md"] = take()
        head(c, "generátory (ukážky, $seeds na generátor)")
        for ((name, gen) in c.generators) {
            ln("### `gen:$name`${at(src.of(name, "gen"))}"); ln()
            for (seed in 0 until seeds) exercise(name, seed, gen(Random(seed * 7919 + name.hashCode())))
        }
        out["generatory.md"] = take()
        head(c, "príklady z písomiek")
        c.papers.forEach { (route, p) -> paper(route, p, c.course) }
        c.generated?.let { g -> paper("nová písomka (generátor kapitoly, semienko 1)", g(Random(1))) }
        out["pisomky.md"] = take()
        // every screen of the chapter, for screenshots (:app:snap -Psnap.route=...)
        out["obrazovky.tsv"] = (c.lessons.flatMap { l -> l.steps.indices.map { "lesson:${l.id}:$it\t${l.id}" } } +
            c.concepts.map { "concept:${it.id}\tkarty" } + c.theorems.map { "theorem:${it.id}\tkarty" }).joinToString("\n", postfix = "\n")
        return out
    }

    /** Writes `<chapter>/{lekcie,karty,generatory,pisomky}.md` and `index.md` into [out]; returns the files. */
    fun write(out: File): List<File> {
        out.mkdirs()
        val files = ExportChapters.flatMap { c ->
            val dir = File(out, c.id).apply { mkdirs() }
            chapter(c).map { (name, text) -> File(dir, name).also { it.writeText(text) } }
        }
        File(out, "index.md").writeText(buildString {
            append("# Export obsahu FITagoras\n\nKaždá kapitola má priečinok: `lekcie.md`, `karty.md` (definície, vety, dôkazy), `generatory.md`, `pisomky.md`, `obrazovky.tsv`.\n\n")
            append("| kapitola | priečinok | balík | lekcie | kroky |\n|---|---|---|---|---|\n")
            ExportChapters.forEach { c -> append("| ${c.title} | `${c.id}/` | `${c.bundle}` | ${c.lessons.size} | ${c.lessons.sumOf { it.steps.size }} |\n") }
        })
        return files
    }
}

/** `exportContent`: args = output folder, seeds per generator. */
fun main(args: Array<String>) {
    val out = File(args.getOrNull(0) ?: error("output folder"))
    val files = ContentExporter(args.getOrNull(1)?.toInt() ?: 2).write(out)
    files.forEach { println("${it.path}\t${it.length() / 1024} kB") }
}

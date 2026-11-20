package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.BannedTerms
import dev.hamper.tabula.data.DmlGenerators
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.math.unknownCommands
import dev.hamper.tabula.tutor.Blok
import dev.hamper.tabula.tutor.Nastavenie
import dev.hamper.tabula.tutor.Prepis
import dev.hamper.tabula.tutor.Strana
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.gemUlohy
import dev.hamper.tabula.tutor.kontext
import dev.hamper.tabula.tutor.nadpis
import dev.hamper.tabula.tutor.siwc.HttpResponses
import dev.hamper.tabula.tutor.vstupVysvetlenia
import dev.hamper.tabula.tutor.vysvetli
import dev.hamper.tabula.tutor.vyhodnot
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.random.Random

/*
 * V: the explanation on text only (docs/TUTOR.md, "Harness"). Verdicts from the student simulator on the app's own
 * exercises (every kind of mistake, a third with none), each with a wrong answer in the app and the app's feedback to
 * it; the phone's explanation call on each. Checked by machine: every number and matrix in the text comes from the
 * input (V1), course words (V4), every formula drawable (V5), no tags (V6), length. Whether the text says the same as
 * the engine (V2) and how it reads go to a judge from another model family: an export for it, the harness itself
 * calls no other API.
 */
object Vysvetlenie {
    fun main(args: List<String>): Int = runBlocking {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        val n = opt("--n")?.toInt() ?: 120
        val model = opt("--model") ?: "gpt-6.2-sol"
        val usilie = opt("--usilie") ?: "low"
        val dir = (opt("--out")?.let(::File) ?: File(tutorData, "runs/vysvetlenie")).apply { mkdirs() }
        val k = HttpResponses(harnessSiwc(), logCli)
        val nast = Nastavenie(model, null, model, usilie)
        // --agent: the general tutor's words on the same verdict (what the student now sees), the verdict given to its GEM tool
        val agent = "--agent" in args
        val jen = opt("--id")?.split(',')?.map { it.trim() }?.toSet()
        val cfg = "$model-$usilie-" + (if (agent) "agent-" + dev.hamper.tabula.tutor.PokynAgenta.hashCode().toUInt().toString(16) else dev.hamper.tabula.tutor.PokynVysvetlenia.hashCode().toUInt().toString(16))
        val cache = File(dir, "beh-$cfg").apply { mkdirs() }
        System.err.println("Pokyn, posledný riadok: " + dev.hamper.tabula.tutor.PokynVysvetlenia.lines().last())
        val r = Random(opt("--seed")?.toLong() ?: 5)
        val st = Student(r)
        val ids = Generators.keys.filter { it !in DmlGenerators.keys }.sorted()
        val kinds = listOf(null, null, null, null, Chyba.E1, Chyba.E1, Chyba.E2, Chyba.E3, Chyba.E4, Chyba.E5, Chyba.E6, Chyba.E9)
        val rows = ArrayList<JSONObject>()
        val judge = StringBuilder()
        var i = 0; var tries = 0
        var stop: String? = null
        while (rows.size < n && tries++ < n * 40 && (jen == null || i < n)) {
            val gid = ids[r.nextInt(ids.size)]
            val ex = Generators.getValue(gid)(Random(r.nextInt(1000)))
            val u = gemUlohy(ex).firstOrNull() ?: continue
            val e = kinds[i % kinds.size]
            val p = st.pripad("v$i", u, e) ?: continue
            if ("druha_polovica" in p.styl) continue
            i++
            // --id v116,v157: only these cases of the set (the same draw, so the same cases as the whole run)
            if (jen != null && p.id !in jen) continue
            // a wrong answer to the first part, and the feedback the app shows on it
            val part = ex.parts.first()(emptyList())
            val (odp, vazba) = when (val a = part.answer) {
                // the same feedback the app shows (screens/Exercise.kt): the right answer first, then the likely mistake
                is Answer.Numbers -> { val v = a.expected.mapIndexed { j, x -> if (j == 0) x + Frac.ONE else x }; v.joinToString(", ") to listOfNotNull("Správne je ${a.shown}.", a.diagnose?.invoke(v)).joinToString(" ") }
                is Answer.Choice -> { val w = (a.options.indices - a.correct).first(); a.options[w] to listOfNotNull(a.wrong[w], a.why).joinToString(" ").ifEmpty { null } }
                else -> null to null
            }
            val kx = kontext(ex, emptyList(), odp, vazba).copy(ulohy = listOf(u))
            val vys = vyhodnot(listOf(u), Prepis(listOf(Strana(1, Strana.Citatelnost.OK, 0, null)), listOf(Blok("gem", 1, null, p.prepis))))
            val vstup = vstupVysvetlenia(kx, vys)
            // the cache knows the input too: a changed verdict text must not reuse an old explanation
            val c = File(cache, "${p.id}-${vstup.hashCode().toUInt().toString(16)}.json")
            // --sucho: only the inputs (the facts as the model would get them), no call
            val raw = if ("--sucho" in args) JSONObject().put("id", p.id).put("text", "") else if (c.exists()) JSONObject(c.readText()) else {
                if (stop != null) break
                val res = if (!agent) runCatching { dev.hamper.tabula.tutor.vysvetliOverene(k, nast, kx, vys, vys.hlavny) } else runCatching {
                    val m = dev.hamper.tabula.tutor.miesto(ex, emptyList(), odp, vazba, "la1").copy(gem = kx)
                    val r = dev.hamper.tabula.tutor.Rozhovor(m, k, nast, usilie)
                    r.art.vopred = vys to kx
                    val a = r.opytaj(null, listOf(ByteArray(1)))
                    dev.hamper.tabula.tutor.Vysvetlenie(a.text, a.pokus, a.spotreba, a.ms, a.casy.firstOrNull { it.first.startsWith("model") }?.second ?: 0, a.volania.joinToString())
                }
                val err = res.exceptionOrNull()
                if (err is TutorChyba && err.druh == TutorChyba.Druh.LIMIT) { stop = err.popis(); break }
                val o = JSONObject().put("id", p.id)
                res.onSuccess { o.put("text", it.text).put("vstup", it.spotreba.vstup).put("vystup", it.spotreba.vystup).put("ms", it.ms).put("prvy_ms", it.prvyMs).put("pokus", it.pokus).put("dovod", it.dovod ?: "") }
                    .onFailure { o.put("chyba", (it as? TutorChyba)?.popis() ?: it.toString()) }
                // a busy server is not a result: the next run asks again
                if ((err as? TutorChyba)?.druh != TutorChyba.Druh.NEDOSTUPNE) c.writeText(o.toString())
                System.err.println("  ${p.id}: ${o.optString("chyba").ifEmpty { "ok ${o.optLong("ms")} ms" }}")
                o
            }
            val text = raw.optString("text")
            val row = JSONObject().put("id", p.id).put("generator", gid).put("chyba", e?.name ?: "-").put("stav", vys.hlavny?.stav?.name)
                .put("nadpis", nadpis(vys.hlavny, vys)).put("text", text).put("ms", raw.optLong("ms")).put("prvy_ms", raw.optLong("prvy_ms"))
                .put("vstup_tokeny", raw.optInt("vstup")).put("vystup_tokeny", raw.optInt("vystup")).put("pokus", raw.optInt("pokus"))
            if (raw.has("chyba")) row.put("zlyhanie", raw.getString("chyba")) else kontroly(text, vstup, row)
            rows += row
            judge.append(JSONObject().put("id", p.id).put("nadpis", row.optString("nadpis")).put("vstup", vstup).put("text", text).toString()).append('\n')
        }
        File(dir, "sudca-$cfg.jsonl").writeText(judge.toString())
        val ok = rows.filter { !it.has("zlyhanie") }
        fun cnt(k: String) = ok.count { it.optBoolean(k) }
        val words = ok.map { it.optInt("slova") }.sorted()
        val rep = JSONObject().put("konfiguracia", cfg).put("texty", rows.size).put("zlyhania", rows.size - ok.size)
            .put("V1_cisla_mimo_vstup", "${ok.count { it.optInt("cisla_mimo") > 0 }}/${ok.size}")
            .put("V1_cisla_spolu", "${ok.sumOf { it.optInt("cisla_mimo") }}/${ok.sumOf { it.optInt("cisla") }}")
            .put("V1_matice_mimo_vstup", "${ok.sumOf { it.optInt("matice_mimo") }}/${ok.sumOf { it.optInt("matice") }}")
            .put("V4_zakazane_slova", "${cnt("zakazane")}/${ok.size}")
            .put("V5_nevysaditelne", "${cnt("nevysaditelne")}/${ok.size}")
            .put("V6_znacky", "${cnt("znacky")}/${ok.size}")
            .put("slova_median", words.getOrElse(words.size / 2) { 0 }).put("slova_max", words.lastOrNull() ?: 0).put("nad_90_slov", ok.count { it.optInt("slova") > 90 })
            .put("ms_median", ok.map { it.optLong("ms") }.sorted().getOrElse(ok.size / 2) { 0 }).put("prvy_text_median", ok.map { it.optLong("prvy_ms") }.sorted().getOrElse(ok.size / 2) { 0 })
            .put("vstup_median", ok.map { it.optInt("vstup_tokeny") }.sorted().getOrElse(ok.size / 2) { 0 })
            .put("opakovane", ok.count { it.optInt("pokus") == 1 }).put("len_fakty", ok.count { it.optInt("pokus") == 2 })
        File(dir, "report-$cfg.json").writeText(JSONObject(rep.toString()).put("riadky", dev.hamper.tabula.compat.json.JSONArray().apply { rows.forEach { put(it) } }).toString())
        println("Vysvetlenie ($cfg): ${rows.size} textov")
        rep.keys().forEach { if (it != "konfiguracia") println("  %-24s %s".format(it, rep.opt(it))) }
        ok.filter { it.optInt("cisla_mimo") > 0 || it.optBoolean("zakazane") || it.optBoolean("nevysaditelne") || it.optBoolean("znacky") }.take(15).forEach {
            println("  ✗ ${it.optString("id")}: mimo ${it.optString("cisla_mimo_zoznam")} zakázané ${it.optString("zakazane_zoznam")} sadzba ${it.optString("sadzba")} | ${it.optString("text").take(160)}")
        }
        stop?.let { println("ZASTAVENÉ limitom plánu: $it") }
        0
    }

    private val num = Regex("(?<![A-Za-z\\\\])-?\\d+(?:/\\d+)?")
    private val frac = Regex("\\\\[dt]?frac\\{(-?\\d+)\\}\\{(\\d+)\\}")

    /** Numbers in a text, normalized: \frac{a}{b} → a/b, −3 → -3; the sign doesn't matter (−3 and 3 come from one cell). */
    private fun cisla(s: String): List<String> {
        val t = frac.replace(s.replace('−', '-')) { "${it.groupValues[1]}/${it.groupValues[2]}" }
        return num.findAll(t).map { it.value.removePrefix("-") }.toList()
    }

    private fun matice(s: String): List<String> = Regex("\\\\begin\\{pmatrix[^\\}]*\\}(.*?)\\\\end\\{pmatrix\\}").findAll(s).map { it.groupValues[1].replace(" ", "").replace('−', '-') }.toList()

    private fun kontroly(text: String, vstup: String, row: JSONObject) {
        val inN = cisla(vstup).toSet()
        val outN = cisla(text)
        val mimo = outN.filter { it !in inN }
        val inM = matice(vstup).toSet()
        val outM = matice(text)
        val banned = BannedTerms.filter { it.regex.containsMatchIn(text) }.map { it.pattern }
        val formulas = Regex("\\$\\$(.+?)\\$\\$|\\$(.+?)\\$", RegexOption.DOT_MATCHES_ALL).findAll(text).map { it.groupValues[1].ifEmpty { it.groupValues[2] } }.toList()
        val bad = formulas.flatMap { f -> unknownCommands(f).map { "$it in $f" } }
        val dollars = text.count { it == '$' } % 2 != 0
        row.put("cisla", outN.size).put("cisla_mimo", mimo.size).put("cisla_mimo_zoznam", mimo.joinToString())
            .put("matice", outM.size).put("matice_mimo", outM.count { it !in inM })
            .put("zakazane", banned.isNotEmpty()).put("zakazane_zoznam", banned.joinToString())
            .put("nevysaditelne", bad.isNotEmpty() || dollars).put("sadzba", (bad + if (dollars) listOf("nepárny $") else emptyList()).joinToString())
            .put("znacky", Regex("<[a-zA-Z/][^>]*>").containsMatchIn(text))
            .put("slova", text.split(Regex("\\s+")).count { it.isNotBlank() })
    }
}

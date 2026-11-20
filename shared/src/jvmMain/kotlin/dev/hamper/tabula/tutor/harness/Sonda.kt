package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.intMat
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.Kontext
import dev.hamper.tabula.tutor.Nastavenie
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.citaj
import dev.hamper.tabula.tutor.siwc.HttpResponses
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * `scripts/tutor.sh sonda [--model M] [--foto F] [--len-usilie]`: what the ChatGPT route accepts, measured with real calls (plan
 * section "Otvorené otázky"): reasoning effort, json_schema with an image in a stream, tokens of one photographed page
 * at three sizes, whether a repeated prefix is cached.
 */
object Sonda {
    fun main(args: List<String>): Int = runBlocking {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        val k = HttpResponses(harnessSiwc(), logCli)
        val model = opt("--model") ?: "gpt-6.2-sol"
        if ("--len-usilie" !in args && opt("--foto") == null) { System.err.println("--foto <fotka strany s GEM> (alebo --len-usilie)"); return@runBlocking 2 }
        val foto by lazy { File(opt("--foto")!!) } // only steps 2./3. read a page
        val runs = File(tutorData, "runs").apply { mkdirs() }
        val out = JSONObject().put("model", model)
        fun chyba(t: Throwable) = (t as? TutorChyba)?.popis() ?: t.toString()
        fun text(effort: String?): JSONObject = JSONObject().put("model", model).put("instructions", "Odpovedaj jedným slovom.")
            .put("input", JSONArray().put(JSONObject().put("role", "user").put("content", "Koľko je 2+3? Napíš len číslo."))).also { t -> effort?.let { t.put("reasoning", JSONObject().put("effort", it)) } }

        // 1. reasoning effort
        val efforts = JSONObject()
        for (e in listOf(null, "none", "minimal", "low", "medium", "high", "xhigh")) {
            val r = runCatching { k.odpoved(text(e)) }
            val line = r.fold({ "ok: '${it.text.trim()}' ${it.spoluMs} ms, výstup ${it.usage.vystup} (úvaha ${it.usage.uvazovanie}), model ${it.model}" }, ::chyba)
            efforts.put(e ?: "(bez)", line)
            println("effort ${e ?: "(bez)"}: $line")
        }
        out.put("reasoning_effort", efforts)
        if ("--len-usilie" in args) return@runBlocking 0 // the model matrix only needs which efforts a model takes

        // 2./3. json_schema + image in a stream; tokens of one page at three sizes
        val kontext = Kontext("Spočtěte hodnost matice A = (2 3 2 2; 1 1 1 0; 3 1 2 -1; 4 2 1 5) nad R.", "h(A) =", "4", "3", null, emptyList(),
            listOf(GemUloha(Field.R, intMat(listOf(2, 3, 2, 2), listOf(1, 1, 1, 0), listOf(3, 1, 2, -1), listOf(4, 2, 1, 5)))))
        val sizes = JSONObject()
        for (px in listOf(1536, 2048, 2560)) {
            val jpg = Rukopis.zoSuboru(foto, px)
            val r = runCatching { citaj(k, Nastavenie(model, "medium", model), kontext, listOf(jpg)) }
            val line = r.fold({ (p, o) -> "ok: vstup ${o.usage.vstup} (cache ${o.usage.vstupCache}), výstup ${o.usage.vystup} (úvaha ${o.usage.uvazovanie}), ${o.spoluMs} ms, prvý text ${o.prvyTextMs} ms, ${jpg.size / 1024} kB, blokov GEM ${p.gemy.size}, matíc ${p.gemy.sumOf { it.gem.matice.size }}" }, ::chyba)
            sizes.put("$px", line); println("strana $px px: $line")
            r.getOrNull()?.let { (_, o) -> File(runs, "sonda-prepis-$px.json").writeText(o.text) }
        }
        out.put("strana", sizes)
        // 4. the same request again: is the prefix (instructions + image) cached?
        val again = runCatching { citaj(k, Nastavenie(model, "medium", model), kontext, listOf(Rukopis.zoSuboru(foto, 2048))).second }
        out.put("cache_opakovane", again.fold({ "vstup ${it.usage.vstup}, z cache ${it.usage.vstupCache}" }, ::chyba))
        println("opakovane: ${out.optString("cache_opakovane")}")
        File(runs, "sonda.json").writeText(out.toString())
        0
    }
}

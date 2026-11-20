package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.Kontext
import dev.hamper.tabula.tutor.Nastavenie
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.Verdikt
import dev.hamper.tabula.tutor.bunka
import dev.hamper.tabula.tutor.citaj
import dev.hamper.tabula.tutor.prepisZJson
import dev.hamper.tabula.tutor.siwc.HttpResponses
import dev.hamper.tabula.tutor.vyhodnot
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/*
 * Č: reading photos with known content (docs/TUTOR.md, "Harness"). A set is a folder with photos and sada.json (the
 * exercise, the truth of every cell, where the mistake is). `data fotky` draws synthetic sets; Teo's real photos have a
 * hand-made sada.json (gold transcription by two models of different families, compared). `citanie` runs the phone's
 * reading call and the engine on each photo and scores the whole chain, photo → verdict.
 */

internal fun ulohaJson(u: GemUloha) = JSONObject().put("pole", (u.pole as? Field.Zp)?.p ?: 0)
    .put("start", JSONArray().apply { u.start.rows.forEach { r -> put(JSONArray().apply { r.forEach { put(it.toString()) } }) } })
    .put("ciara", u.start.bar ?: -1).put("ciel", u.ciel.name)

internal fun ulohaZJson(j: JSONObject): GemUloha {
    val f: Field = j.optInt("pole").let { if (it == 0) Field.R else Field.Zp(it) }
    val a = j.getJSONArray("start")
    val rows = (0 until a.length()).map { i -> a.getJSONArray(i).let { r -> (0 until r.length()).map { bunka(r.getString(it), f)!! } } }
    return GemUloha(f, Mat(rows, j.optInt("ciara", -1).takeIf { it >= 0 }), GemUloha.Ciel.valueOf(j.optString("ciel", "HST")))
}

object Sady {
    /** Draws [n] synthetic sheets into [dir]: half with one mistake (cells: E1, E2, E3, E9; some E4–E6), half right. */
    fun vytvor(dir: File, n: Int, realne: Boolean, seed: Long, otocene: Int, ciste: Boolean = false): Int {
        dir.mkdirs()
        val r = Random(seed)
        val fonts = File(tutorData, "fonts").listFiles { f -> f.extension == "ttf" }?.sortedBy { it.name }.orEmpty()
        val glyfy = if (realne) Glyfy.nacitaj(File(tutorData, "mathwriting/mathwriting-2024/symbols")) else null
        val rk = Rukopis(fonts, glyfy)
        val tasks = ulohyLa1SoZadanim(30, seed0 = 100).filter { (_, u, _) -> u.start.m <= 5 && u.start.n <= 6 }
        val st = Student(r)
        val kinds = listOf(null, null, null, null, null, Chyba.E1, Chyba.E1, Chyba.E2, Chyba.E3, Chyba.E9, Chyba.E1, if (r.nextBoolean()) Chyba.E4 else Chyba.E6)
        val items = JSONArray()
        var i = 0
        var tries = 0
        while (items.length() < n && tries++ < n * 50) {
            val (tid, u, prompt) = tasks[r.nextInt(tasks.size)]
            // [ciste]: right chains only, to measure false alarms on enough right steps
            val e = if (ciste) null else kinds[i % kinds.size]
            if (e == Chyba.E2 && u.pole !is Field.Zp) continue
            val p = st.pripad("${dir.name}-$i", u, e) ?: continue
            // what fits on one sheet: up to 6 matrices, no second half (that needs its own context)
            val kept = p.prepis.matice.count { !it.preskrtnuta }
            if (kept > 6 || kept < (if (ciste) 3 else 2) || "druha_polovica" in p.styl) continue
            val turn = if ((i + 1) % max(1, otocene) == 0 && otocene > 0) listOf(90, 270, 180).random(r) else 0
            val foto = rk.nakresli(p.prepis, r, realne, turn)
            // a chain that did not fit on the sheet would leave the truth with matrices the photo does not show
            if (foto.oblasti.size < p.prepis.matice.size) continue
            i++
            val name = "${p.id}.jpg"
            File(dir, name).writeBytes(foto.jpeg)
            val t = p.pravda
            items.put(JSONObject().put("id", p.id).put("foto", name).put("zdroj", if (realne) "mathwriting" else "pismo").put("uloha_id", tid)
                .put("zadanie", prompt).put("uloha", ulohaJson(u)).put("otocenie", foto.otocenie)
                .put("styl", JSONObject().apply { (p.styl.map { it to "1" } + foto.styl.toList()).forEach { (k, v) -> put(k, v) } })
                .put("gold", JSONArray().apply { p.prepis.matice.filter { !it.preskrtnuta }.forEach { m -> put(JSONArray().apply { m.riadky.forEach { row -> put(JSONArray().apply { row.forEach { put(it.v) } }) } }) } })
                .put("oblasti", JSONArray().apply { foto.oblasti.forEach { b -> put(JSONArray().apply { b.forEach { put(it) } }) } })
                .put("pravda", JSONObject().put("chyba", t.chyba?.name ?: "").put("stav", t.stav.name).put("krok", t.krok ?: -1)
                    .put("riadky", JSONArray().apply { t.riadky.sorted().forEach { put(it) } })
                    .put("bunka", t.bunka?.let { JSONArray().put(it.first).put(it.second) }).put("maloByt", t.maloByt?.toString())))
        }
        File(dir, "sada.json").writeText(JSONObject().put("polozky", items).toString())
        return items.length()
    }
}

object Citanie {
    fun main(args: List<String>): Int = runBlocking {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        val sady = args.filterIndexed { i, a -> i > 0 && args[i - 1] == "--sada" }.map(::File)
        require(sady.isNotEmpty()) { "citanie --sada DIR [--sada DIR2] [--model M] [--usilie E] [--detail high|low] [--px 2048] [--limit N] [--out report.json]" }
        val model = opt("--model") ?: "gpt-6.2-sol"
        val usilie = opt("--usilie") ?: "medium"
        val detail = opt("--detail") ?: "high"
        val px = opt("--px")?.toInt() ?: 2048
        val limit = opt("--limit")?.toInt() ?: Int.MAX_VALUE
        val n = Nastavenie(model, usilie, model, detail = detail)
        val k = HttpResponses(harnessSiwc(), logCli)
        // a repeated run (stability) is its own configuration, so it calls the model again
        // --beh <name>: score an older run's cache (another prompt) against today's truth, without calling the model
        val cfg = opt("--beh") ?: ("$model-$usilie-$detail-$px-" + (opt("--opakovanie")?.let { "r$it-" } ?: "") + (dev.hamper.tabula.tutor.PokynCitania + dev.hamper.tabula.tutor.schemaPrepis().toString()).hashCode().toUInt().toString(16))
        val rows = ArrayList<JSONObject>()
        var stop: String? = null
        for (dir in sady) {
            val items = JSONObject(File(dir, "sada.json").readText()).getJSONArray("polozky")
            val cache = File(dir, "beh-$cfg").apply { mkdirs() }
            for (idx in 0 until minOf(items.length(), limit)) {
                val it = items.getJSONObject(idx)
                val id = it.getString("id")
                val c = File(cache, "$id.json")
                val u = ulohaZJson(it.getJSONObject("uloha"))
                val raw: JSONObject = if (c.exists()) JSONObject(c.readText()) else {
                    if (stop != null || opt("--beh") != null) continue
                    val kontext = Kontext(it.getString("zadanie"), "", null, "", null, emptyList(), listOf(u))
                    // one photo, or several pages ("fotky", from the phone's collection)
                    val names = it.optJSONArray("fotky")?.let { a -> (0 until a.length()).map { i -> a.getString(i) } } ?: listOf(it.getString("foto"))
                    val pages = names.map { nm -> Rukopis.zoSuboru(File(dir, nm), px) }
                    val jpg = pages.first()
                    val res = runCatching { citaj(k, n, kontext, pages) }
                    val e = res.exceptionOrNull()
                    if (e is TutorChyba && e.druh == TutorChyba.Druh.LIMIT) { stop = e.popis(); System.err.println("Limit plánu: $stop"); continue }
                    val o = JSONObject().put("id", id)
                    res.onSuccess { (_, odp) -> o.put("text", odp.text).put("vstup", odp.usage.vstup).put("vystup", odp.usage.vystup).put("uvaha", odp.usage.uvazovanie).put("ms", odp.spoluMs).put("prvy_ms", odp.prvyTextMs).put("kb", jpg.size / 1024) }
                        .onFailure { err -> o.put("chyba", (err as? TutorChyba)?.popis() ?: err.toString()) }
                    if (e == null || (e as? TutorChyba)?.druh != TutorChyba.Druh.NEDOSTUPNE) c.writeText(o.toString())
                    System.err.println("  $id: ${o.optString("chyba").ifEmpty { "ok ${o.optLong("ms")} ms" }}")
                    o
                }
                rows += skore(it, u, raw)
            }
        }
        val rep = suhrn(rows, cfg)
        println(rep.second)
        opt("--out")?.let { File(it).writeText(rep.first.toString()) }
        if (stop != null) println("ZASTAVENÉ limitom plánu, beh pokračuje z cache: $stop")
        0
    }

    /** One photo scored: cells read, the verdict against the truth. */
    private fun skore(item: JSONObject, u: GemUloha, raw: JSONObject): JSONObject {
        val out = JSONObject().put("id", item.getString("id")).put("zdroj", item.optString("zdroj")).put("otocenie", item.optInt("otocenie"))
        if (raw.has("chyba")) return out.put("zlyhanie", raw.getString("chyba"))
        out.put("vstup", raw.optInt("vstup")).put("vystup", raw.optInt("vystup")).put("ms", raw.optLong("ms")).put("prvy_ms", raw.optLong("prvy_ms"))
        val p = prepisZJson(raw.optString("text")) ?: return out.put("zlyhanie", "neplatný JSON")
        out.put("otocenie_precitane", p.strany.firstOrNull()?.otocenie ?: -1)
        val v = vyhodnot(listOf(u), p)
        val h = v.hlavny
        out.put("stav", h?.stav?.name ?: "ZIADNY_GEM").put("krok", h?.krok ?: -1).put("riadky", JSONArray().apply { h?.riadky?.sorted()?.forEach { put(it) } })
            .put("nadpis", dev.hamper.tabula.tutor.nadpis(h, v))
        h?.bunky?.firstOrNull()?.let { out.put("bunka", JSONArray().put(it.miesto.riadok).put(it.miesto.stlpec)) }
        // cells: the model's kept matrices against the gold, matrix by matrix
        // older sets: the generator stopped drawing at the sheet's bottom edge, so the photo shows only the first matrices
        // ("oblasti" counts every drawn matrix, "gold" only those not crossed out; "preskrtnute": "áno" = one crossed out was
        // drawn, checked on the photos); the truth is what the photo shows
        val gold0 = item.optJSONArray("gold")
        val ob = item.optJSONArray("oblasti")
        val preskrtnuta = if (item.optJSONObject("styl")?.optString("preskrtnute") == "áno") 1 else 0
        val orezane = gold0 != null && ob != null && ob.length() < gold0.length() + preskrtnuta
        val gold = if (orezane) JSONArray().apply { for (m in 0 until ob!!.length() - preskrtnuta) put(gold0!!.get(m)) } else gold0
        if (orezane) out.put("orezane", true)
        // a page with several tasks: the block the engine took for this task (its verdict is the main one), not the first
        val hi = v.verdikty.indexOf(h).takeIf { it >= 0 } ?: 0
        val readKept = p.gemy.getOrNull(hi)?.gem?.matice?.filter { !it.preskrtnuta }.orEmpty()
        out.put("blokov", p.gemy.size)
        // the right task found: the chain the verdict is about starts where this task starts (not another task's matrix)
        out.put("spravny_blok", h != null && h.stav != Verdikt.Stav.INE_ZADANIE && readKept.firstOrNull()?.riadky?.map { r -> r.map { bunka(it.v, u.pole) } } == u.start.rows)
        var cells = 0; var ok = 0
        if (gold != null) for (m in 0 until gold.length()) {
            val gm = gold.getJSONArray(m)
            for (i in 0 until gm.length()) { val gr = gm.getJSONArray(i); for (j in 0 until gr.length()) {
                cells++
                val want = bunka(gr.getString(j), u.pole)
                val got = readKept.getOrNull(m)?.riadky?.getOrNull(i)?.getOrNull(j)?.v?.let { bunka(it, u.pole) }
                if (want != null && want == got) ok++
            } }
        }
        out.put("bunky", cells).put("bunky_ok", ok).put("matic_gold", gold?.length() ?: 0).put("matic_precitane", readKept.size)
        // the wrong cell, read as written?
        val t0 = item.getJSONObject("pravda")
        // an error beyond the drawn matrices is not on the photo: the photo shows correct work that is not finished
        val t = if (orezane && t0.optInt("krok") >= gold!!.length()) JSONObject().put("chyba", "").put("stav", "NEDOKONCENE").put("krok", -1).put("riadky", JSONArray()).put("orezane", true)
            else if (orezane && t0.optString("chyba").isEmpty()) JSONObject(t0.toString()).put("orezane", true) else t0
        val tb = t.optJSONArray("bunka")
        if (tb != null && gold != null && t.optInt("krok") >= 0) {
            val km = t.getInt("krok"); val wi = tb.getInt(0); val wj = tb.getInt(1)
            val written = bunka(gold.getJSONArray(km).getJSONArray(wi).getString(wj), u.pole)
            val got = readKept.getOrNull(km)?.riadky?.getOrNull(wi)?.getOrNull(wj)?.v?.let { bunka(it, u.pole) }
            out.put("chybna_bunka_ako_napisana", written != null && written == got)
            out.put("chybna_bunka_opravena", got != null && t.optString("maloByt").isNotEmpty() && got == bunka(t.getString("maloByt"), u.pole))
        }
        // the whole chain against the truth
        val chyba = t.optString("chyba").isNotEmpty()
        val bezCervenej = t.optBoolean("bez_cervenej")
        val stav = h?.stav
        if (bezCervenej) {
            out.put("trieda", if (stav == Verdikt.Stav.CHYBA) "falosny_poplach" else "ok")
        } else if (!chyba) {
            out.put("trieda", when (stav) { Verdikt.Stav.BEZ_CHYBY -> "ok"; Verdikt.Stav.NEDOKONCENE -> if (t.optBoolean("orezane") || t.optString("stav") == "NEDOKONCENE") "ok" else "neiste"; Verdikt.Stav.CHYBA -> "falosny_poplach"; Verdikt.Stav.OTAZKA -> "otazka"; else -> "neiste" })
        } else {
            val tr = t.optJSONArray("riadky")?.let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() } ?: emptySet()
            val tstav = t.optString("stav")
            out.put("trieda", when {
                tstav == "NEDOKONCENE" && stav == Verdikt.Stav.NEDOKONCENE -> "najdene"
                stav == Verdikt.Stav.CHYBA && h.krok == t.optInt("krok") && h.riadky == tr -> "najdene"
                stav == Verdikt.Stav.CHYBA && (h.krok ?: 99) < t.optInt("krok") -> "falosny_poplach"
                stav == Verdikt.Stav.CHYBA -> "zle_miesto"
                stav == Verdikt.Stav.BEZ_CHYBY -> "pochvalene_zle"
                stav == Verdikt.Stav.OTAZKA -> "otazka"
                else -> "neiste"
            })
        }
        // a sentence the student would read that is not true: a mark where there is none or in the wrong place, praise for
        // a wrong chain, "úpravy sedia, posledná matica nie je hotová" for a finished one
        val tstav0 = t.optString("stav")
        val zavadza = out.optString("trieda") in setOf("falosny_poplach", "zle_miesto", "pochvalene_zle") ||
            (stav == Verdikt.Stav.NEDOKONCENE && tstav0 != "NEDOKONCENE" && !t.optBoolean("orezane") && !bezCervenej) || (stav == Verdikt.Stav.BEZ_CHYBY && chyba)
        out.put("zavadzajuce", zavadza)
        out.put("pravda_chyba", chyba).put("prechody_spravne", if (chyba) max(0, t.optInt("krok") - 1) else max(0, (gold?.length() ?: 1) - 1))
        // where the model put the matrices (synthetic sheets know exactly)
        item.optJSONArray("oblasti")?.let { ob ->
            val ious = readKept.mapIndexedNotNull { m, pm -> val b = pm.oblast ?: return@mapIndexedNotNull null; ob.optJSONArray(m)?.let { g -> iou(b, (0 until 4).map { g.getDouble(it) }) } }
            if (ious.isNotEmpty()) out.put("iou", ious.average())
        }
        return out
    }

    private fun iou(a: List<Double>, b: List<Double>): Double {
        val ix = max(0.0, min(a[2], b[2]) - max(a[0], b[0])); val iy = max(0.0, min(a[3], b[3]) - max(a[1], b[1]))
        val inter = ix * iy
        val ua = (a[2] - a[0]) * (a[3] - a[1]) + (b[2] - b[0]) * (b[3] - b[1]) - inter
        return if (ua <= 0) 0.0 else inter / ua
    }

    fun suhrn(rows: List<JSONObject>, cfg: String): Pair<JSONObject, String> {
        val ok = rows.filter { !it.has("zlyhanie") }
        val err = ok.filter { it.optBoolean("pravda_chyba") }
        val cellErr = ok.filter { it.has("chybna_bunka_ako_napisana") }
        fun c(l: List<JSONObject>, t: String) = l.count { it.optString("trieda") == t }
        val transitions = ok.sumOf { it.optInt("prechody_spravne") }
        val rot = ok.filter { it.optInt("otocenie") != 0 }
        val j = JSONObject().put("konfiguracia", cfg).put("fotky", rows.size).put("zlyhania", rows.size - ok.size)
            .put("C1_chybna_bunka_ako_napisana", "${cellErr.count { it.optBoolean("chybna_bunka_ako_napisana") }}/${cellErr.size}")
            .put("C1_opravena_modelom", cellErr.count { it.optBoolean("chybna_bunka_opravena") })
            .put("C2_falosny_poplach", "${c(ok, "falosny_poplach")}/$transitions prechodov")
            .put("C3_bunky", "%.4f".format(ok.sumOf { it.optInt("bunky_ok") }.toDouble() / max(1, ok.sumOf { it.optInt("bunky") })))
            .put("C4_najdene", "${c(err, "najdene")}/${err.size}")
            .put("C5_pochvalene_zle", "${c(err, "pochvalene_zle")}/${err.size}")
            .put("C6_otazky", "${c(ok, "otazka")}/${ok.size}")
            .put("neiste", c(ok, "neiste")).put("zle_miesto", c(err, "zle_miesto"))
            .put("zavadzajuce_vety", "${ok.count { it.optBoolean("zavadzajuce") }}/${ok.size}")
            .put("C7_spravna_uloha_na_strane", ok.filter { it.optInt("blokov") > 1 }.let { m -> "${m.count { it.optBoolean("spravny_blok") }}/${m.size} strán s viacerými GEM-mi; spolu ${ok.count { it.optBoolean("spravny_blok") }}/${ok.size}" })
            .put("otocenie", "${rot.count { it.optInt("otocenie") == it.optInt("otocenie_precitane") }}/${rot.size}")
            .put("iou_priemer", ok.mapNotNull { r -> r.optDouble("iou").takeIf { !it.isNaN() } }.average().let { if (it.isNaN()) -1.0 else it })
            .put("vstup_median", median(ok.map { it.optInt("vstup").toDouble() })).put("vystup_median", median(ok.map { it.optInt("vystup").toDouble() }))
            .put("ms_median", median(ok.map { it.optLong("ms").toDouble() })).put("ms_p90", pct(ok.map { it.optLong("ms").toDouble() }, 0.9))
            .put("riadky", JSONArray().apply { rows.forEach { put(it) } })
        val text = buildString {
            appendLine("Čítanie ($cfg): ${rows.size} fotiek, zlyhaní volania ${rows.size - ok.size}")
            listOf("C1_chybna_bunka_ako_napisana", "C1_opravena_modelom", "C2_falosny_poplach", "C3_bunky", "C4_najdene", "C5_pochvalene_zle", "C6_otazky", "neiste", "zle_miesto", "zavadzajuce_vety", "C7_spravna_uloha_na_strane", "otocenie", "iou_priemer", "vstup_median", "vystup_median", "ms_median", "ms_p90")
                .forEach { appendLine("  %-30s %s".format(it, j.opt(it))) }
            ok.filter { it.optString("trieda") !in setOf("ok", "najdene") }.forEach { appendLine("  ✗ ${it.optString("id")} ${it.optString("trieda")}: ${it.optString("stav")} krok ${it.optInt("krok")} riadky ${it.opt("riadky")} | ${it.optString("nadpis")}") }
            rows.filter { it.has("zlyhanie") }.forEach { appendLine("  ! ${it.optString("id")}: ${it.optString("zlyhanie")}") }
        }
        return j to text
    }

    private fun median(v: List<Double>) = pct(v, 0.5)
    private fun pct(v: List<Double>, q: Double): Double = if (v.isEmpty()) 0.0 else v.sorted()[((v.size - 1) * q).toInt()]
}

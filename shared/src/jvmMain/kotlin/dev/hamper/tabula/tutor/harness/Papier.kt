package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.tutor.Nastavenie
import dev.hamper.tabula.tutor.PokynPosudku
import dev.hamper.tabula.tutor.PrepisTextu
import dev.hamper.tabula.tutor.Strana
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.UlohaNaPapier
import dev.hamper.tabula.tutor.citajText
import dev.hamper.tabula.tutor.naPapier
import dev.hamper.tabula.tutor.polozkyDokazu
import dev.hamper.tabula.tutor.posud
import dev.hamper.tabula.tutor.siwc.HttpResponses
import dev.hamper.tabula.tutor.vstupPosudku
import kotlinx.coroutines.runBlocking
import java.io.File

/*
 * Case 2 in the harness (docs/TUTOR.md, "Na papier"): the judgement on text, against transcripts whose truth is known
 * item by item (a proof or definition written as a student would, complete or with one item left out or wrong), and
 * the real photos as they are. The same functions as the phone.
 *
 *   papier export --out F.jsonl                  every proof and definition with its checklist, for writing cases
 *   papier posud --sada F.jsonl [--out DIR]      the judgement on each case; cache by case × instructions
 *   papier fotky --ciel proof:<id> FOTO…         reading and judgement of real photos, printed
 */
object Papier {
    fun uloha(ciel: String): UlohaNaPapier? = when {
        ciel.startsWith("proof:") -> ProofById[ciel.removePrefix("proof:")]?.let { naPapier(it, polozkyDokazu(it)) }
        ciel.startsWith("concept:") -> ConceptById[ciel.removePrefix("concept:")]?.takeIf { it.units.isNotEmpty() }?.let { naPapier(it) }
        else -> null
    }

    fun main(args: List<String>): Int = runBlocking {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        when (args.firstOrNull()) {
            "export" -> {
                val out = File(opt("--out") ?: error("--out"))
                val sb = StringBuilder()
                (ProofById.keys.map { "proof:$it" } + ConceptById.keys.map { "concept:$it" }).sorted().forEach { c ->
                    val u = uloha(c) ?: return@forEach
                    sb.append(JSONObject().put("ciel", c).put("druh", u.druh).put("nazov", u.nazov).put("zadanie", u.zadanie)
                        .put("vzor", JSONArray().apply { u.vzor.forEach { put(it) } }).put("body", JSONArray().apply { u.body.forEach { put(it) } })
                        .put("pravidla", JSONArray().apply { u.pravidla.forEach { put(it) } }).toString()).append('\n')
                }
                out.writeText(sb.toString()); println("${sb.lines().count { it.isNotBlank() }} úloh → $out"); 0
            }
            "posud" -> posudSadu(File(opt("--sada") ?: error("--sada")), File(opt("--out") ?: "/tmp/papier"), opt("--usilie") ?: "medium", opt("--model"))
            "fotky" -> {
                val u = uloha(opt("--ciel") ?: error("--ciel")) ?: error("neznámy cieľ")
                val fotky = args.filter { it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg") }.map { File(it).readBytes() }
                val k = HttpResponses(harnessSiwc(), logCli)
                val n = Nastavenie.z(k.modely())!!
                val t0 = System.currentTimeMillis()
                val (p, _) = citajText(k, n, u, fotky.map(::jpeg))
                val t1 = System.currentTimeMillis()
                println("PREPIS (${(t1 - t0) / 1000} s):"); p.riadky.forEach { println("  $it") }; if (p.nejasne.isNotEmpty()) println("  nejasné: ${p.nejasne}")
                val (ps, _) = posud(k, n, u, p) { print(it) }
                println(); println("VERDIKT (${(System.currentTimeMillis() - t1) / 1000} s, istota ${ps.istota}): ${ps.verdikt}; splnené body ${ps.splnene.map { it + 1 }}")
                0
            }
            else -> { System.err.println("papier export|posud|fotky"); 2 }
        }
    }

    /** A PNG or JPEG as the phone sends it: JPEG, the long side at most 2048 px. */
    private fun jpeg(b: ByteArray): ByteArray {
        val img = javax.imageio.ImageIO.read(b.inputStream()) ?: return b
        val s = minOf(1.0, 2048.0 / maxOf(img.width, img.height))
        val out = java.awt.image.BufferedImage((img.width * s).toInt(), (img.height * s).toInt(), java.awt.image.BufferedImage.TYPE_INT_RGB)
        out.createGraphics().apply { setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR); drawImage(img, 0, 0, out.width, out.height, null); dispose() }
        return java.io.ByteArrayOutputStream().also { javax.imageio.ImageIO.write(out, "jpg", it) }.toByteArray()
    }

    /**
     * Each case: {"id", "ciel", "riadky": [...], "uznat": "uznal_by"|"s_vyhradou"|"neuznal_by", "poznamka"}: a paper as
     * a student could write it (complete in other words, or with a real flaw). Reported: the verdict against the truth
     * (accepting "s_vyhradou" either way for a minor flaw), wrong refusals of a right paper (the pedantry Teo saw), and
     * wrong acceptances of a flawed one. The texts go to blind judges.
     */
    private suspend fun posudSadu(sada: File, dir: File, usilie: String, model: String?): Int {
        dir.mkdirs()
        val k = HttpResponses(harnessSiwc(), logCli)
        val n = model?.let { Nastavenie(it, null, it, usilie) } ?: Nastavenie.z(k.modely())!!
        val cfg = "${n.modelVysvetlenia}-$usilie-" + PokynPosudku.hashCode().toUInt().toString(16)
        val cache = File(dir, "beh-$cfg").apply { mkdirs() }
        val rows = ArrayList<JSONObject>()
        val judge = StringBuilder()
        for (line in sada.readLines().filter { it.isNotBlank() }) {
            val c = JSONObject(line)
            val u = uloha(c.getString("ciel")) ?: run { System.err.println("  ${c.optString("id")}: neznámy cieľ"); continue }
            val r = c.getJSONArray("riadky"); val p = PrepisTextu(listOf(Strana(1, Strana.Citatelnost.OK, 0, null)), (0 until r.length()).map { r.getString(it) }, emptyList())
            val vstup = vstupPosudku(u, p)
            val f = File(cache, "${c.getString("id")}-${vstup.hashCode().toUInt().toString(16)}.json")
            val o = if (f.exists()) JSONObject(f.readText()) else {
                val t0 = System.currentTimeMillis(); var prvy = -1L
                val res = runCatching { posud(k, n, u, p, usilie) { if (prvy < 0) prvy = System.currentTimeMillis() - t0 } }
                val e = res.exceptionOrNull()
                if (e is TutorChyba && e.druh == TutorChyba.Druh.LIMIT) { System.err.println("LIMIT: ${e.popis()}"); break }
                val o = JSONObject().put("id", c.getString("id")).put("ms", System.currentTimeMillis() - t0).put("prvy_ms", prvy)
                res.onSuccess { (ps, s) -> o.put("verdikt", ps.verdikt.name.lowercase()).put("text", ps.text).put("istota", ps.istota).put("vstup", s.vstup).put("vystup", s.vystup) }
                    .onFailure { o.put("chyba", (it as? TutorChyba)?.popis() ?: it.toString()) }
                if ((e as? TutorChyba)?.druh != TutorChyba.Druh.NEDOSTUPNE) f.writeText(o.toString())
                System.err.println("  ${c.getString("id")}: ${o.optString("chyba").ifEmpty { o.optString("verdikt") + " " + o.optLong("ms") + " ms" }}")
                o
            }
            if (o.has("chyba")) { rows += JSONObject().put("id", c.getString("id")).put("zlyhanie", o.getString("chyba")); continue }
            val pravda = c.optString("uznat"); val v = o.optString("verdikt")
            fun prijate(x: String) = x != "neuznal_by"
            rows += JSONObject().put("id", c.getString("id")).put("pravda", pravda).put("verdikt", v).put("ms", o.optLong("ms")).put("prvy_ms", o.optLong("prvy_ms"))
                .put("zhoda", pravda == v || (pravda == "s_vyhradou" && v != "neuznal_by") || (v == "s_vyhradou" && pravda == "uznal_by"))
                .put("odmietnute_spravne", pravda == "uznal_by" && !prijate(v)).put("prijate_chybne", pravda == "neuznal_by" && prijate(v))
            judge.append(JSONObject().put("id", c.getString("id")).put("podklad_a_prepis", vstup).put("posudok", o.optString("text")).put("verdikt", v).toString()).append('\n')
        }
        File(dir, "sudca-$cfg.jsonl").writeText(judge.toString())
        val ok = rows.filter { !it.has("zlyhanie") }
        val rep = JSONObject().put("konfiguracia", cfg).put("pripady", rows.size).put("zlyhania", rows.size - ok.size)
            .put("verdikt_zhoda", "${ok.count { it.getBoolean("zhoda") }}/${ok.size}")
            .put("spravne_neuznane", "${ok.count { it.getBoolean("odmietnute_spravne") }}/${ok.count { it.getString("pravda") == "uznal_by" }}")
            .put("chybne_uznane", "${ok.count { it.getBoolean("prijate_chybne") }}/${ok.count { it.getString("pravda") == "neuznal_by" }}")
            .put("prvy_text_median_ms", ok.map { it.getLong("prvy_ms") }.sorted().getOrElse(ok.size / 2) { 0 })
            .put("ms_median", ok.map { it.getLong("ms") }.sorted().getOrElse(ok.size / 2) { 0 })
        File(dir, "report-$cfg.json").writeText(JSONObject(rep.toString()).put("riadky", JSONArray().apply { rows.forEach { put(it) } }).toString())
        println("Posudok ($cfg): ${rows.size} prípadov")
        rep.keys().forEach { if (it != "konfiguracia") println("  %-24s %s".format(it, rep.opt(it))) }
        ok.filter { !it.getBoolean("zhoda") }.forEach { println("  ✗ ${it.getString("id")}: pravda ${it.getString("pravda")}, verdikt ${it.getString("verdikt")}") }
        return 0
    }
}

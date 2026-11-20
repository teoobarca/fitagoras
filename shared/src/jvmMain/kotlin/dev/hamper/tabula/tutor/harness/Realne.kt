package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.tutor.Citanie
import dev.hamper.tabula.tutor.GemPrepis
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.PrepisMatica
import dev.hamper.tabula.tutor.bunka
import dev.hamper.tabula.tutor.overPostup
import java.io.File

/*
 * Teo's real pages (data/sady/teo-real): working pages as they are, several tasks on a page, crossings out, at an
 * angle. A transcript per page (another model's, `codex/<page>.json`) is checked by the engine before it becomes
 * truth: a GEM chain the engine finds consistent, or whose one wrong cell is a plausible slip, is kept; anything the
 * transcriber was unsure of is looked at on the photo.
 *
 *   realne over --codex DIR [--out F.json]    every GEM chain of every transcript through the engine, printed
 */
object Realne {
    private val gemDruhy = setOf("gem", "zp_gem", "baza", "suradnice", "ln", "pocet_podpriestorov")

    fun pole(t: String?): Field = t?.trim()?.uppercase()?.removePrefix("Z")?.toIntOrNull()?.let { Field.Zp(it) } ?: Field.R

    fun matice(a: JSONArray): List<PrepisMatica> = (0 until a.length()).map { i ->
        val m = a.getJSONObject(i)
        val r = m.getJSONArray("riadky")
        PrepisMatica((0 until r.length()).map { k -> r.getJSONArray(k).let { row -> (0 until row.length()).map { Citanie(row.getString(it)) } } },
            m.opt("ciara")?.let { (it as? Number)?.toInt() }, m.optBoolean("preskrtnuta"))
    }

    fun main(args: List<String>): Int {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        val dir = File(opt("--codex") ?: error("--codex DIR"))
        val out = JSONArray()
        for (f in dir.listFiles { x -> x.name.endsWith(".json") }!!.sortedBy { it.name }) {
            val j = runCatching { JSONObject(f.readText().trim().removePrefix("```json").removeSuffix("```")) }.getOrNull() ?: run { println("${f.name}: neplatný JSON"); continue }
            val ul = j.getJSONArray("ulohy")
            println("== ${f.nameWithoutExtension} (${j.optString("citatelnost")}, úloh ${ul.length()})")
            for (i in 0 until ul.length()) {
                val u = ul.getJSONObject(i)
                val ms = matice(u.optJSONArray("matice") ?: JSONArray())
                val kept = ms.filter { !it.preskrtnuta }
                val neiste = u.optJSONArray("neiste")?.length() ?: 0
                val row = JSONObject().put("strana", f.nameWithoutExtension).put("uloha", u.optString("id")).put("druh", u.optString("druh")).put("teleso", u.opt("teleso") ?: JSONObject.NULL)
                    .put("matic", kept.size).put("neiste", neiste)
                if (u.optString("druh") in gemDruhy && kept.size >= 2) {
                    val p = pole(u.opt("teleso") as? String)
                    val start = runCatching { Mat(kept[0].riadky.map { r -> r.map { c -> bunka(c.v, p) ?: error("bunka ${c.v}") } }, kept[0].ciara) }.getOrNull()
                    if (start == null) { println("  ${u.optString("id")} ${u.optString("druh")}: neprečítateľná prvá matica"); row.put("stav", "ZLA_PRVA"); out.put(row); continue }
                    val v = overPostup(listOf(GemUloha(p, start)), GemPrepis(ms))
                    val b = v.bunky.firstOrNull()
                    row.put("stav", v.stav.name).put("krok", v.krok ?: -1).put("typ", v.typ?.name ?: JSONObject.NULL)
                        .put("riadky", JSONArray().apply { v.riadky.sorted().forEach { put(it) } })
                    b?.let { row.put("bunka", JSONArray().put(it.miesto.riadok).put(it.miesto.stlpec)).put("napisane", it.napisane.toString()).put("maloByt", it.maloByt.toString()) }
                    println("  ${u.optString("id")} ${u.optString("druh")} $p, matíc ${kept.size}, neisté $neiste: ${v.stav}" + (v.krok?.let { " krok $it" } ?: "") +
                        (b?.let { " bunka ${it.miesto.riadok},${it.miesto.stlpec}: ${it.napisane} → ${it.maloByt}" } ?: "") + (v.typ?.let { " ($it)" } ?: ""))
                } else println("  ${u.optString("id")} ${u.optString("druh")}: matíc ${kept.size}, neisté $neiste — ${u.optJSONArray("riadky_textu")?.let { a -> (0 until minOf(2, a.length())).joinToString(" | ") { a.getString(it) } }?.take(140)}")
                out.put(row)
            }
        }
        opt("--out")?.let { File(it).writeText(out.toString()) }
        return 0
    }
}

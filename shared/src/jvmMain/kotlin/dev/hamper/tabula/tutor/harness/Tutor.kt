package dev.hamper.tabula.tutor.harness

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.data.TheoremById
import dev.hamper.tabula.tutor.MiestoAppky
import dev.hamper.tabula.tutor.Nastavenie
import dev.hamper.tabula.tutor.PokynAgenta
import dev.hamper.tabula.tutor.Rozhovor
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.miesto
import dev.hamper.tabula.tutor.polozkyDokazu
import dev.hamper.tabula.tutor.siwc.HttpResponses
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.random.Random

/*
 * The general tutor in the harness (docs/TUTOR.md, "Všeobecný tútor"): one question at one place of the app, the same
 * loop and tools as the phone.
 *
 *   tutor --miesto lesson:<id>:<krok> | concept:<id> | theorem:<id> | proof:<id> | gen:<id>[:seed] [--otazka TEXT] [FOTKA…]
 *   tutor --sada F.jsonl --out DIR      a set of questions ({"id","miesto","otazka","fotky":[…]}), cached, exported for judges
 */
object Tutor {
    fun miestoZ(route: String): MiestoAppky? {
        val p = route.split(':')
        return when (p[0]) {
            "gen" -> Generators[p[1]]?.let { g ->
                val ex = g(Random(p.getOrNull(2)?.toIntOrNull() ?: 1))
                miesto(ex, emptyList(), p.getOrNull(3), p.getOrNull(4), dev.hamper.tabula.data.courseOfRoute("gen:${p[1]}")?.id ?: if (p[1].startsWith("dml")) "dml" else "la1")
            }
            // foto:<sada>/<id>: a photographed GEM of a reading set, with its own exercise (the truth of the photo)
            "foto" -> {
                val (sada, id) = p[1].split('/')
                val items = JSONObject(File(tutorData, "sady/$sada/sada.json").readText()).getJSONArray("polozky")
                val it = (0 until items.length()).map { items.getJSONObject(it) }.firstOrNull { it.getString("id") == id } ?: return null
                val u = ulohaZJson(it.getJSONObject("uloha"))
                val k = dev.hamper.tabula.tutor.Kontext(it.getString("zadanie"), "", null, "", null, emptyList(), listOf(u))
                MiestoAppky("Príklad", "la1", "Príklad: ${it.getString("zadanie")}\n(Študent chce skontrolovať postup GEM na papieri.)", gem = k)
            }
            // gemmat:<rows "2,3;1,1">:<answer>: a rank exercise with this matrix (Teo's photographed pages)
            "gemmat" -> {
                val rows = p[1].split(';').map { r -> r.split(',').map { dev.hamper.tabula.la.Frac.of(it.trim().toLong()) } }
                val u = dev.hamper.tabula.tutor.GemUloha(dev.hamper.tabula.la.Field.R, dev.hamper.tabula.la.Mat(rows))
                val odp = p.getOrNull(2)
                val k = dev.hamper.tabula.tutor.Kontext("Spočtěte hodnost matice A.", "h(A) =", odp, "", null, listOf("Uprav na HST"), listOf(u))
                MiestoAppky("Hodnosť matice", "la1", "Príklad: Spočtěte hodnost matice A = " + rows.joinToString("; ") { it.joinToString(" ") } + ".\nČasť na obrazovke: h(A) =" + (odp?.let { "\nŠTUDENT ODPOVEDAL: $it" } ?: ""), gem = k, odpoved = odp)
            }
            else -> dev.hamper.tabula.tutor.miestoZRouty(route)
        }
    }

    fun main(args: List<String>): Int = runBlocking {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        if ("--zoznam" in args) {
            for (c in Courses) {
                println("## ${c.id}")
                c.lessons.forEach { l -> println("lesson ${l.id} | ${l.name} | " + l.steps.mapIndexed { i, st -> "$i:" + st::class.simpleName!!.take(4) + ((st as? dev.hamper.tabula.data.Step.Practice)?.let { "(" + it.gen + ")" } ?: "") }.joinToString(" ")) }
                c.papers.forEach { pp -> println("paper ${pp.id} | ${pp.name} | " + pp.problems.joinToString(" ") { "${it.n}:${it.title}(${it.units.size})" }) }
            }
            return@runBlocking 0
        }
        val k = HttpResponses(harnessSiwc(), logCli)
        // the model matrix (docs/TUTOR.md, "Modely"): every step's model and effort, the photo's detail
        val z = Nastavenie.z(k.modely())!!
        fun ef(x: String?) = x?.takeIf { it != "nic" }
        val usilie = opt("--usilie") ?: "low"
        val n = Nastavenie(opt("--model-citania") ?: opt("--model") ?: z.modelCitania, if ("--usilie-citania" in args) ef(opt("--usilie-citania")) else z.usilieCitania,
            opt("--model") ?: z.modelVysvetlenia, ef(usilie), opt("--detail") ?: z.detail, if ("--usilie-posudku" in args) ef(opt("--usilie-posudku")) else z.usiliePosudku)
        opt("--sada")?.let { return@runBlocking sada(File(it), File(opt("--out") ?: "/tmp/tutor-sada"), k, n, usilie, args) }
        // --gem "2,3,2,2;1,1,1,0;…": a rank exercise with this matrix (Teo's photographed pages have no generator seed)
        val m = opt("--gem")?.let { g ->
            val rows = g.split(';').map { r -> r.split(',').map { dev.hamper.tabula.la.Frac.of(it.trim().toLong()) } }
            val u = dev.hamper.tabula.tutor.GemUloha(dev.hamper.tabula.la.Field.R, dev.hamper.tabula.la.Mat(rows))
            val k = dev.hamper.tabula.tutor.Kontext("Spočtěte hodnost matice A.", "h(A) =", opt("--odpoved"), "", null, listOf("Uprav na HST"), listOf(u))
            MiestoAppky("Hodnosť matice", "la1", "Príklad: Spočtěte hodnost matice A = " + rows.joinToString("; ") { it.joinToString(" ") } + ".\nČasť na obrazovke: h(A) =" + (opt("--odpoved")?.let { "\nŠTUDENT ODPOVEDAL: $it" } ?: ""), gem = k, odpoved = opt("--odpoved"))
        } ?: miestoZ(opt("--miesto") ?: error("--miesto")) ?: error("neznáme miesto")
        val fotky = args.filter { it.endsWith(".jpg") || it.endsWith(".png") || it.endsWith(".jpeg") }.map { Fotky.jpeg(File(it).readBytes()) }
        val r = Rozhovor(m, k, n, n.usilieVysvetlenia, "--jedno-volanie" !in args)
        // --zaznam FILE: the stream's events as the screen gets them (text deltas, states, resets, the early verdict), with
        // times, no tokens: test data for the writing on screen (jvmTest StreamZaznamyTest); --stop-po N: "Stop" after
        // N characters, as the student's button does (the call is cancelled)
        val udalosti = JSONArray(); val t0 = System.currentTimeMillis()
        fun ud(e: String, d: String?) { udalosti.put(JSONObject().put("t", System.currentTimeMillis() - t0).put("e", e).apply { d?.let { put("d", it) } }) }
        val stopPo = opt("--stop-po")?.toInt()
        var prijate = 0
        var beh: kotlinx.coroutines.Deferred<dev.hamper.tabula.tutor.OdpovedAgenta>? = null
        beh = async {
            r.opytaj(opt("--otazka"), fotky, onStav = { ud("stav", it); System.err.println("  … $it") }, onReset = { ud("reset", null) }, onVerdikt = { ud("verdikt", it.name) }) { d ->
                ud("text", d); prijate += d.length
                if (stopPo != null && prijate >= stopPo) beh?.cancel()
            }
        }
        val o = try { beh!!.await() } catch (e: kotlinx.coroutines.CancellationException) { null }
        opt("--zaznam")?.let { f ->
            File(f).writeText(JSONObject().put("miesto", opt("--miesto") ?: "gem").put("otazka", opt("--otazka") ?: JSONObject.NULL).put("fotky", fotky.size)
                .put("zastavene", o == null).put("text", o?.text ?: JSONObject.NULL).put("nastroje", JSONArray().apply { o?.volania?.forEach { put(it) } })
                .put("udalosti", udalosti).toString())
        }
        if (o == null) { println("ZASTAVENÉ po $prijate znakoch"); return@runBlocking 0 }
        println("ODPOVEĎ (${o.ms / 1000} s, ${o.kola} kolá, nástroje ${o.volania}, vstup ${o.spotreba.vstup} tok.):")
        println(o.text)
        println("ČAS: " + o.casy.joinToString(" · ") { "${it.first} ${it.second / 100 / 10.0} s" })
        o.vystupyNastrojov.forEach { s -> println("--- nástroj:\n${s.take(1500)}") }
        0
    }

    private suspend fun sada(f: File, dir: File, k: HttpResponses, n: Nastavenie, usilie: String, args: List<String>): Int {
        dir.mkdirs()
        val cfg = "${n.modelVysvetlenia}-$usilie-c${n.modelCitania}-${n.usilieCitania}-${n.detail}-p${n.usiliePosudku}-" + (if ("--jedno-volanie" in args) "jedno-" else "") + (PokynAgenta + dev.hamper.tabula.tutor.PokynPosudku).hashCode().toUInt().toString(16)
        val cache = File(dir, "beh-$cfg").apply { mkdirs() }
        val judge = StringBuilder()
        val rows = ArrayList<JSONObject>()
        for (line in f.readLines().filter { it.isNotBlank() }) {
            val c = JSONObject(line)
            val id = c.getString("id")
            val m = miestoZ(c.getString("miesto")) ?: run { System.err.println("  $id: neznáme miesto ${c.getString("miesto")}"); continue }
            val fotky = c.optJSONArray("fotky")?.let { a -> (0 until a.length()).map { Fotky.jpeg(File(a.getString(it)).readBytes()) } }.orEmpty()
            val otazka = (c.opt("otazka") as? String)?.takeIf { it.isNotBlank() }
            val cf = File(cache, "$id-${(m.opis + otazka + fotky.size).hashCode().toUInt().toString(16)}.json")
            val o = if (cf.exists()) JSONObject(cf.readText()) else {
                val res = runCatching { Rozhovor(m, k, n, n.usilieVysvetlenia, "--jedno-volanie" !in args).opytaj(otazka, fotky) }
                val e = res.exceptionOrNull()
                if (e is TutorChyba && e.druh == TutorChyba.Druh.LIMIT) { System.err.println("LIMIT: ${e.popis()}"); break }
                val o = JSONObject().put("id", id)
                res.onSuccess { a ->
                    o.put("text", a.text).put("casy", JSONArray().apply { a.casy.forEach { (k, v) -> put(JSONObject().put("faza", k).put("ms", v)) } }).put("kola", a.kola).put("prvy_ms", a.prvyTextMs).put("volania", JSONArray().apply { a.volania.forEach { put(it) } }).put("ms", a.ms).put("vstup", a.spotreba.vstup)
                        .put("vystupy", JSONArray().apply { a.vystupyNastrojov.forEach { put(it) } })
                        .put("krokovace", JSONObject().apply { a.art.krokovace.forEach { (r, w) -> put(r, (w as? dev.hamper.tabula.la.GemWalk)?.tex() ?: (w as? dev.hamper.tabula.la.TableWalk)?.rows?.joinToString("\\\\") { it.joinToString(" & ") } ?: "") } })
                }.onFailure { o.put("chyba", (it as? TutorChyba)?.popis() ?: it.toString()) }
                if ((e as? TutorChyba)?.druh != TutorChyba.Druh.NEDOSTUPNE) cf.writeText(o.toString())
                System.err.println("  $id: ${o.optString("chyba").ifEmpty { "ok ${o.optLong("ms")} ms ${o.optJSONArray("volania")}" }}")
                o
            }
            // machine checks: the tool the question needs was called (any of the expected), every tag drawable, no banned word
            val ocak = c.optJSONArray("nastroje")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty()
            val volane = o.optJSONArray("volania")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty()
            o.put("bez_nastroja", ocak.isNotEmpty() && ocak.none { it in volane })
            val text = o.optString("text")
            o.put("zle_znacky", Regex("<(veta|krokovac)[^>]*>").findAll(text).count { m -> dev.hamper.tabula.tutor.kusy(m.value, dev.hamper.tabula.tutor.Artefakty().apply { o.optJSONObject("krokovace")?.keys()?.forEach { krokovace[it] = Unit } }, m0kurz(c)).none { it !is dev.hamper.tabula.tutor.Kus.Text } })
            o.put("zakazane", dev.hamper.tabula.data.BannedTerms.filter { it.regex.containsMatchIn(text) }.joinToString { it.pattern })
            rows += o
            if (!o.has("chyba")) judge.append(JSONObject().put("id", id).put("miesto", m.opis).put("kurz", m.kurz).put("otazka", otazka ?: "(bez otázky: skontroluj / vysvetli)")
                .put("fotky", fotky.size).put("ocakavane_nastroje", c.optJSONArray("nastroje") ?: JSONArray()).put("vystupy_nastrojov", o.optJSONArray("vystupy") ?: JSONArray())
                .put("krokovace", o.optJSONObject("krokovace") ?: JSONObject()).put("odpoved", o.optString("text")).toString()).append('\n')
        }
        File(dir, "sudca-$cfg.jsonl").writeText(judge.toString())
        val ok = rows.filter { !it.has("chyba") }
        println("  bez potrebného nástroja: ${ok.count { it.optBoolean("bez_nastroja") }}/${ok.count { true }}; neplatné značky: ${ok.sumOf { it.optInt("zle_znacky") }}; zakázané slová: ${ok.count { it.optString("zakazane").isNotEmpty() }}")
        ok.filter { it.optBoolean("bez_nastroja") }.forEach { println("    ✗ bez nástroja: ${it.optString("id")} ${it.optJSONArray("volania")}") }
        println("Tútor ($cfg): ${rows.size} otázok, zlyhania ${rows.size - ok.size}, medián ${ok.map { it.optLong("ms") }.sorted().getOrElse(ok.size / 2) { 0 } / 1000} s, prvý text medián ${ok.map { it.optLong("prvy_ms") }.sorted().getOrElse(ok.size / 2) { 0 } / 1000.0} s, nástroje: " +
            ok.flatMap { o -> o.optJSONArray("volania")?.let { a -> (0 until a.length()).map { a.getString(it) } }.orEmpty() }.groupingBy { it }.eachCount())
        return 0
    }
}

private fun m0kurz(c: JSONObject) = if (c.optString("miesto").contains("dml")) "dml" else "la1"

object Fotky {
    /** A PNG or JPEG as the phone sends it: JPEG, the long side at most 2048 px. */
    fun jpeg(b: ByteArray): ByteArray {
        val img = javax.imageio.ImageIO.read(b.inputStream()) ?: return b
        val s = minOf(1.0, 2048.0 / maxOf(img.width, img.height))
        val out = java.awt.image.BufferedImage((img.width * s).toInt(), (img.height * s).toInt(), java.awt.image.BufferedImage.TYPE_INT_RGB)
        out.createGraphics().apply { setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR); drawImage(img, 0, 0, out.width, out.height, null); dispose() }
        return java.io.ByteArrayOutputStream().also { javax.imageio.ImageIO.write(out, "jpg", it) }.toByteArray()
    }
}

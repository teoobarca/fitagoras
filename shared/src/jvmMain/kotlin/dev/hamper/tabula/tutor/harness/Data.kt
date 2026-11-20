package dev.hamper.tabula.tutor.harness

import java.io.File

/** `scripts/tutor.sh data k [--n 12000]`: the engine's gate K on generated cases (no model). */
object Data {
    fun main(args: List<String>): Int {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        return when (args.firstOrNull()) {
            "k" -> {
                val n = opt("--n")?.toInt() ?: 12000
                val t0 = System.currentTimeMillis()
                val v = behK(n, opt("--seed")?.toLong() ?: 1)
                val ms = System.currentTimeMillis() - t0
                println("K: ${v.spravne}/${v.pripady} správne (bez chyby ${v.bezChyby}), ${ms / 1000} s, generátory ${v.generatory.size}: ${v.generatory.sorted().joinToString()}")
                v.poDruhoch.toSortedMap().forEach { (k, c) -> println("  %-10s %5d/%5d".format(k, c.first, c.second)) }
                println("  bunka istá ${v.bunkyNahlasene}/${v.bunkyMozne}, z toho zlá ${v.bunkyChybne}; pravdepodobná ${v.pravdepodobne}, z toho správne ${v.pravdepodobneOk}; druh chyby správne ${v.typOk}/${v.typSpolu}")
                v.zlyhania.forEach { println("  ✗ $it") }
                opt("--out")?.let { File(it).apply { parentFile.mkdirs() }.writeText(buildString {
                    append("{\"pripady\":${v.pripady},\"bez_chyby\":${v.bezChyby},\"spravne\":${v.spravne},\"bunky_nahlasene\":${v.bunkyNahlasene},")
                    append("\"bunky_mozne\":${v.bunkyMozne},\"bunky_chybne\":${v.bunkyChybne},\"typ_ok\":${v.typOk},\"typ_spolu\":${v.typSpolu},\"ms\":$ms,")
                    append("\"po_druhoch\":{" + v.poDruhoch.entries.joinToString(",") { "\"${it.key}\":[${it.value.first},${it.value.second}]" } + "}}")
                }) }
                if (v.spravne == v.pripady) 0 else 1
            }
            "fotky" -> {
                val dir = File(opt("--out") ?: error("--out DIR"))
                val n = Sady.vytvor(dir, opt("--n")?.toInt() ?: 40, "--realne" in args, opt("--seed")?.toLong() ?: 1, opt("--otocene")?.toInt() ?: 0, "--ciste" in args)
                println("$n fotiek v $dir"); 0
            }
            "brana" -> brana(args)
            "import" -> import(args)
            else -> { System.err.println("data k [--n N] [--seed S] [--out súbor.json] | data fotky --out DIR [--n 40] [--realne] [--seed S] [--otocene K]"); 2 }
        }
    }

    /** `data brana --k F --citanie F --vysvetlenie F [--sudca F] --out F`: the gate from the latest reports (docs/TUTOR.md, "Brána"). */
    private fun brana(args: List<String>): Int {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        fun j(k: String) = opt(k)?.let { f -> File(f).takeIf { it.exists() }?.let { dev.hamper.tabula.compat.json.JSONObject(it.readText()) } }
        fun frac(s: String?): Pair<Int, Int>? = s?.substringBefore(' ')?.split('/')?.takeIf { it.size == 2 }?.let { it[0].trim().toInt() to it[1].trim().toInt() }
        val k = j("--k"); val c = j("--citanie"); val v = j("--vysvetlenie"); val sudca = j("--sudca")
        val checks = ArrayList<Triple<String, Boolean, String>>()
        fun add(name: String, ok: Boolean, value: String) { checks += Triple(name, ok, value) }
        if (k == null) add("K", false, "chýba") else {
            val n = k.optInt("pripady"); val ok = k.optInt("spravne"); val clean = k.optInt("bez_chyby")
            add("K1 engine", n >= 10000 && clean >= 3000 && ok == n && k.optInt("bunky_chybne") == 0, "$ok/$n, bez chyby $clean, zlé isté bunky ${k.optInt("bunky_chybne")}")
        }
        if (c == null) add("Č", false, "chýba") else {
            val c1 = frac(c.optString("C1_chybna_bunka_ako_napisana")); add("Č1 zlá bunka ako napísaná", c1 != null && c1.first == c1.second && c1.second > 0, c.optString("C1_chybna_bunka_ako_napisana"))
            val c2 = frac(c.optString("C2_falosny_poplach")); add("Č2 falošný poplach", c2 != null && c2.second >= 300 && c2.first.toDouble() / c2.second <= 1.0 / 300 + 1e-9, c.optString("C2_falosny_poplach"))
            val c4 = frac(c.optString("C4_najdene")); add("Č4 nájdená chyba", c4 != null && c4.second >= 40 && c4.first >= kotlin.math.ceil(c4.second * 0.925), c.optString("C4_najdene"))
            val c5 = frac(c.optString("C5_pochvalene_zle")); add("Č5 pochválené zlé", c5 != null && c5.first == 0, c.optString("C5_pochvalene_zle"))
            val c6 = frac(c.optString("C6_otazky")); add("Č6 otázky", c6 != null && c6.first * 10 <= c6.second, c.optString("C6_otazky"))
            val rows = c.optJSONArray("riadky")
            val real = rows?.let { r -> (0 until r.length()).map { r.getJSONObject(it) }.filter { it.optString("zdroj") == "teo" && !Regex("-otocena\\d+$").containsMatchIn(it.optString("id")) && !it.has("zlyhanie") }.map { it.optString("id") }.distinct().size } ?: 0
            add("skutočný rukopis ≥ 30 strán", real >= 30, "$real")
            // real pages scored on their own: synthetic photos must not dilute a miss on real handwriting
            val teo = rows?.let { r -> (0 until r.length()).map { r.getJSONObject(it) }.filter { it.optString("zdroj") == "teo" && !it.has("zlyhanie") } } ?: emptyList()
            val bad = teo.count { it.optString("trieda") !in setOf("ok", "najdene") }
            add("skutočný rukopis: všetko správne", teo.isNotEmpty() && bad == 0, "${teo.size - bad}/${teo.size} fotiek (vrátane otočených)")
        }
        if (v == null || v.optInt("texty") - v.optInt("zlyhania") < 100) add("V aspoň 100 textov", false, v?.let { "${it.optInt("texty") - it.optInt("zlyhania")}" } ?: "chýba") else {
            val m = frac(v.optString("V1_matice_mimo_vstup")); add("V1 matice zo vstupu", m != null && m.first == 0, v.optString("V1_matice_mimo_vstup"))
            val n = frac(v.optString("V1_cisla_spolu")); add("V1 čísla zo vstupu ≥ 99 %", n != null && (n.second == 0 || n.first.toDouble() / n.second <= 0.01), v.optString("V1_cisla_spolu"))
            add("V4 zakázané slová", frac(v.optString("V4_zakazane_slova"))?.first == 0, v.optString("V4_zakazane_slova"))
            add("V5 sadzba", frac(v.optString("V5_nevysaditelne"))?.first == 0, v.optString("V5_nevysaditelne"))
            add("V6 značky", frac(v.optString("V6_znacky"))?.first == 0, v.optString("V6_znacky"))
        }
        add("V2 rozpor s enginom (sudca)", sudca != null && sudca.optInt("spolu") >= 100 && sudca.optInt("rozpor", -1) == 0, sudca?.let { "${it.optInt("rozpor")}/${it.optInt("spolu")}" } ?: "chýba")
        add("V4 kvalita (sudca) ≥ 4,3, žiadna 1–2", sudca != null && sudca.optDouble("kvalita_priemer", 0.0) >= 4.3 && sudca.optInt("kvalita_min") >= 3,
            sudca?.let { "priemer ${it.optDouble("kvalita_priemer", 0.0)}, najnižšia ${it.optInt("kvalita_min")}" } ?: "chýba")
        // stability: "different/total" photos whose verdict changed over three readings (citanie --opakovanie)
        val st = opt("--stabilita")?.split('/')?.mapNotNull { it.trim().toIntOrNull() }
        add("S stabilita (≥ 30 × 3, ≤ 2 rôzne)", st != null && st.size == 2 && st[1] >= 30 && st[0] <= 2, opt("--stabilita") ?: "nemerané")
        val open = checks.all { it.second }
        val out = dev.hamper.tabula.compat.json.JSONObject().put("otvorena", open).put("datum", java.time.LocalDate.now().toString())
            .put("kontroly", dev.hamper.tabula.compat.json.JSONArray().apply { checks.forEach { (n, ok, value) -> put(dev.hamper.tabula.compat.json.JSONObject().put("nazov", n).put("ok", ok).put("hodnota", value)) } })
        checks.forEach { (n, ok, value) -> println("  ${if (ok) "✓" else "✗"} %-32s %s".format(n, value)) }
        println(if (open) "Brána OTVORENÁ" else "Brána zatvorená")
        opt("--out")?.let { File(it).writeText(out.toString() + "\n") }
        return 0
    }

    /**
     * `data import <zbierka.zip> --out DIR`: the phone's collection as a reading set. The truth of each page is what the
     * student confirmed (the model's transcript with their corrections); where the mistake is follows from it by the
     * engine, which K checks. A page appears as "teo" (real handwriting) in the gate.
     */
    private fun import(args: List<String>): Int {
        fun opt(k: String) = args.getOrNull(args.indexOf(k) + 1)?.takeIf { k in args }
        val zip = File(args.getOrNull(1) ?: error("data import <zip> --out DIR"))
        val out = File(opt("--out") ?: error("--out DIR")).apply { mkdirs() }
        val tmp = java.nio.file.Files.createTempDirectory("zbierka").toFile()
        java.util.zip.ZipFile(zip).use { z -> z.entries().asSequence().filter { !it.isDirectory }.forEach { e ->
            val f = File(tmp, e.name).canonicalFile
            require(f.toPath().startsWith(tmp.canonicalFile.toPath())) { "cesta mimo priečinka: ${e.name}" }
            f.parentFile.mkdirs(); z.getInputStream(e).use { i -> f.outputStream().use { i.copyTo(it) } }
        } }
        val existing = File(out, "sada.json").takeIf { it.exists() }?.let { dev.hamper.tabula.compat.json.JSONObject(it.readText()).getJSONArray("polozky") } ?: dev.hamper.tabula.compat.json.JSONArray()
        val have = (0 until existing.length()).map { existing.getJSONObject(it).getString("id") }.toMutableSet()
        var added = 0
        for (d in tmp.listFiles().orEmpty().filter { File(it, "zaznam.json").exists() }.sortedBy { it.name }) {
            val id = "zbierka-${d.name}"
            if (id in have) continue
            val z = dev.hamper.tabula.compat.json.JSONObject(File(d, "zaznam.json").readText())
            val u = ulohaZJson(z.getJSONObject("uloha"))
            val p = dev.hamper.tabula.tutor.prepisZJson(z.getString("prepis")) ?: continue
            val opravy = z.optJSONArray("opravy")?.let { a -> (0 until a.length()).map { i -> a.getJSONObject(i).let { o -> Oprava(o.getInt("matica"), o.getInt("riadok"), o.getInt("stlpec"), o.getString("napisane")) } } } ?: emptyList()
            // the truth: what the stepper showed when it was confirmed (older records: the transcript with corrections)
            val shown = z.optJSONArray("gold")?.takeIf { it.length() > 0 }?.let { g -> (0 until g.length()).map { mi -> g.getJSONArray(mi).let { m -> (0 until m.length()).map { ri -> m.getJSONArray(ri).let { r -> (0 until r.length()).map { r.getString(it) } } } } } }
            val gold = shown?.let { g -> dev.hamper.tabula.tutor.GemPrepis(g.map { m -> dev.hamper.tabula.tutor.PrepisMatica(m.map { r -> r.map { dev.hamper.tabula.tutor.Citanie(it) } }, u.start.bar) }) }
                ?: opravenyPrepis(p, z.optInt("blok"), opravy) ?: continue
            // every cell is as confirmed: no question about any of them
            val vsetky = gold.matice.filter { !it.preskrtnuta }.flatMapIndexed { mi, m -> m.riadky.flatMapIndexed { ri, r -> r.indices.map { ci -> dev.hamper.tabula.tutor.Miesto(mi, ri, ci) } } }.toSet()
            val v = dev.hamper.tabula.tutor.overPostup(listOf(u), gold, vsetky)
            val n = z.optInt("fotky", 1)
            if (n < 1 || (1..n).any { !File(d, "strana-$it.jpg").exists() }) { System.err.println("  $id: chýbajú fotky, preskakujem"); continue }
            val photos = (1..n).map { i -> "$id-$i.jpg".also { nm -> File(d, "strana-$i.jpg").copyTo(File(out, nm), overwrite = true) } }
            val kept = gold.matice.filter { !it.preskrtnuta }
            val c = v.bunky.firstOrNull()
            existing.put(dev.hamper.tabula.compat.json.JSONObject().put("id", id).put("fotky", dev.hamper.tabula.compat.json.JSONArray().apply { photos.forEach { put(it) } })
                .put("foto", photos.first()).put("zdroj", "teo").put("zadanie", z.optString("zadanie")).put("uloha", z.getJSONObject("uloha")).put("otocenie", p.strany.firstOrNull()?.otocenie ?: 0)
                .put("gold", dev.hamper.tabula.compat.json.JSONArray().apply { kept.forEach { m -> put(dev.hamper.tabula.compat.json.JSONArray().apply { m.riadky.forEach { r -> put(dev.hamper.tabula.compat.json.JSONArray().apply { r.forEach { put(it.v) } }) } }) } })
                .put("pravda", dev.hamper.tabula.compat.json.JSONObject().put("chyba", if (v.stav == dev.hamper.tabula.tutor.Verdikt.Stav.CHYBA || v.stav == dev.hamper.tabula.tutor.Verdikt.Stav.NEDOKONCENE) "zbierka" else "")
                    .put("stav", v.stav.name).put("krok", v.krok ?: -1).put("riadky", dev.hamper.tabula.compat.json.JSONArray().apply { v.riadky.sorted().forEach { put(it) } })
                    .put("bunka", c?.let { dev.hamper.tabula.compat.json.JSONArray().put(it.miesto.riadok).put(it.miesto.stlpec) }).put("maloByt", c?.maloByt?.toString())
                    .put("bez_cervenej", v.stav !in setOf(dev.hamper.tabula.tutor.Verdikt.Stav.CHYBA, dev.hamper.tabula.tutor.Verdikt.Stav.BEZ_CHYBY, dev.hamper.tabula.tutor.Verdikt.Stav.NEDOKONCENE))))
            added++
        }
        tmp.deleteRecursively()
        File(out, "sada.json").writeText(dev.hamper.tabula.compat.json.JSONObject().put("polozky", existing).toString())
        println("Pridané $added strán, v sade ${existing.length()}: $out")
        return 0
    }
}

/** One cell the student corrected in a collected page (the earlier tutor screen): kept matrix, row, column, value written. */
private data class Oprava(val matica: Int, val riadok: Int, val stlpec: Int, val napisane: String)

/** The transcript of [blok] with the corrections applied: the gold of a page collected before it kept its own. */
private fun opravenyPrepis(p: dev.hamper.tabula.tutor.Prepis, blok: Int, opravy: List<Oprava>): dev.hamper.tabula.tutor.GemPrepis? {
    val gem = p.gemy.getOrNull(blok)?.gem ?: return null
    val kept = gem.matice.withIndex().filter { !it.value.preskrtnuta }.map { it.index }
    var mats = gem.matice
    for (o in opravy) {
        val mi = kept.getOrNull(o.matica) ?: continue
        mats = mats.mapIndexed { i, m ->
            if (i != mi) m else m.copy(riadky = m.riadky.mapIndexed { r, row -> if (r != o.riadok) row else row.mapIndexed { c, cell -> if (c == o.stlpec) dev.hamper.tabula.tutor.Citanie(o.napisane) else cell } })
        }
    }
    return gem.copy(matice = mats)
}

package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.data.RefIndexes
import dev.hamper.tabula.data.RefTarget
import dev.hamper.tabula.data.dml.numbers.crt
import dev.hamper.tabula.data.dml.numbers.euclidWalk
import dev.hamper.tabula.data.dml.numbers.gcd
import dev.hamper.tabula.data.dml.numbers.inverse
import dev.hamper.tabula.data.dml.numbers.lkon
import dev.hamper.tabula.data.dml.numbers.md
import dev.hamper.tabula.data.dml.numbers.powMod
import dev.hamper.tabula.data.dml.numbers.powWalk
import dev.hamper.tabula.data.dml.numbers.rea
import dev.hamper.tabula.data.dml.numbers.reaWalk
import dev.hamper.tabula.data.lineText
import dev.hamper.tabula.data.refKind
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.la.Mat
import dev.hamper.tabula.la.TableWalk
import dev.hamper.tabula.la.echelon
import dev.hamper.tabula.la.rank
import dev.hamper.tabula.la.coords
import dev.hamper.tabula.la.completeWithUnitVectors
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/*
 * The tutor (docs/TUTOR.md): one general loop for any place in the app. Input: where the student is (MiestoAppky), what they
 * photographed (optional) and what they ask (optional; without a question "check it"). The model answers from the
 * place and the course packs; whatever can be computed or checked by the app is a tool, and where a tool exists the
 * model must use it and take its verdict. A new tool is one entry in [Nastroje]; no new screen.
 *
 * The earlier pipelines are tools here: the blind GEM reading with the engine's verdict (case 1) and the blind reading
 * of written text judged against the checklist (case 2). Their guards stay theirs.
 */

/** What the tools made, for the screen: walks to draw (`<krokovac ref="k1"/>`), the GEM verdict, the paper's judgement. */
class Artefakty {
    val krokovace = LinkedHashMap<String, Any>() // GemWalk or TableWalk
    var postup: Vysledok? = null
    var postupKontext: Kontext? = null
    var posudok: Posudok? = null
    /** The harness's verdict given ahead: the GEM tool uses it instead of reading photos (the explanation measured alone). */
    var vopred: Pair<Vysledok, Kontext>? = null
    var prepisPapiera: PrepisTextu? = null
    fun pridaj(w: Any): String { val ref = "k${krokovace.size + 1}"; krokovace[ref] = w; return ref }
}

/** One tool: its schema for the model and what the app does. [dostupny]: offered at this place, with these photos. */
class Nastroj(
    val nazov: String,
    val popis: String,
    val parametre: JSONObject,
    val dostupny: (MiestoAppky, Boolean) -> Boolean = { _, _ -> true },
    val spusti: suspend (VolanieKontext, JSONObject) -> String,
)

/** What a tool may use: the place, the photos, the client (a tool may call the model itself, blind), the artefacts. */
class VolanieKontext(val miesto: MiestoAppky, val fotky: List<ByteArray>, val klient: ResponsesKlient, val nastavenie: Nastavenie, val art: Artefakty, val onStav: (String) -> Unit) {
    var spotreba = Spotreba()
    /** Set when a tool's own words are the answer (a paper judged with no question): they stream straight to the student. */
    var naText: ((String) -> Unit)? = null
    var naReset: () -> Unit = {}
    /** The judgement's verdict as soon as its stream names it (before its words): the step shows it in place at once. */
    var naVerdikt: ((Posudok.Verdikt) -> Unit)? = null
}

private fun obj(vararg props: Pair<String, JSONObject>) = JSONObject().put("type", "object").put("additionalProperties", false)
    .put("required", JSONArray().apply { props.forEach { put(it.first) } })
    .put("properties", JSONObject().apply { props.forEach { put(it.first, it.second) } })
private fun typ(t: String, popis: String? = null) = JSONObject().put("type", t).apply { popis?.let { put("description", it) } }
private fun nul(t: String, popis: String? = null) = JSONObject().put("type", JSONArray().put(t).put("null")).apply { popis?.let { put("description", it) } }
private fun pole(items: JSONObject, popis: String? = null) = JSONObject().put("type", "array").put("items", items).apply { popis?.let { put("description", it) } }
private fun vyber(popis: String, vararg v: String) = JSONObject().put("type", "string").put("enum", JSONArray().apply { v.forEach { put(it) } }).put("description", popis)

private fun teleso(s: String): Field? = when {
    s.equals("R", true) || s.equals("Q", true) -> Field.R
    s.startsWith("Z", true) -> s.drop(1).toIntOrNull()?.takeIf { it in 2..97 && (2 until it).none { d -> it % d == 0 } }?.let { Field.Zp(it) }
    else -> null
}

private fun walkText(w: GemWalk): String = buildString {
    append("$" + w.start.tex() + "$")
    w.moves.forEach { m -> append(" ∼ (" + m.kindTex + " " + m.ops.joinToString(", ") { "$" + it.tex() + "$" } + ") $" + m.after.tex() + "$") }
}

private fun tableText(w: TableWalk): String = (listOfNotNull(w.header?.joinToString(" | ")) + w.rows.map { it.joinToString(" | ") }).joinToString("\n")

/** The tools, in one place: adding one is adding an entry here. */
val Nastroje: List<Nastroj> = listOf(
    Nastroj(
        "over_postup_gem",
        "Prečíta študentov postup GEM z priložených fotiek (slepo, bez vzoru) a program appky ho skontroluje: nájde prvý chybný krok, správnu hodnotu a ďalší krok. Volaj VŽDY, keď je na fotke GEM (matice spojené ∼) a študent chce kontrolu. Verdikt programu je záväzný.",
        obj(),
        dostupny = { m, f -> f && m.gem != null },
    ) { vk, _ ->
        val k = vk.art.vopred?.second ?: vk.miesto.gem!!
        val vys = vk.art.vopred?.first ?: run {
            val (prepis, o) = citaj(vk.klient, vk.nastavenie, k, vk.fotky) { n -> vk.onStav("Čítam papier, prečítané matice: $n") }
            vk.spotreba += o.usage
            vk.onStav("Overujem každú úpravu")
            vyhodnot(k.ulohy, prepis)
        }
        vk.art.postup = vys; vk.art.postupKontext = k
        val v = vys.hlavny
        buildString {
            appendLine("NADPIS, KTORÝ APPKA UKÁŽE NAD TVOJÍM TEXTOM: ${nadpis(v, vys)}")
            appendLine("Appka pod ním ukáže študentov krokovač s označenou chybou a správny ďalší krok; neopisuj matice, ale ďalší krok (ktorú úpravu urobiť alebo aké číslo kam prepísať) povedz slovami z faktov a krokovač nespomínaj.")
            if (v != null) { appendLine("ŠTUDENTOV POSTUP A FAKTY PROGRAMU (jediné, čo smieš o postupe tvrdiť):"); append(vstupVysvetlenia(k, vys, v).substringAfter("ŠTUDENTOV POSTUP")) }
        }
    },
    Nastroj(
        "posud_papier",
        "Prečíta študentov napísaný dôkaz, definíciu alebo riešenie z priložených fotiek (slepo) a posúdi, či by to opravujúci uznal (verdikt a prečo).",
        obj(),
        dostupny = { m, f -> f && m.papier != null },
    ) { vk, _ ->
        val u = vk.miesto.papier!!
        val (p, o) = citajText(vk.klient, vk.nastavenie, u, vk.fotky) { n -> vk.onStav("Čítam papier, prečítané riadky: $n") }
        vk.spotreba += o.usage
        vk.art.prepisPapiera = p
        if (p.riadky.isEmpty()) return@Nastroj "Na fotkách nie je čitateľný napísaný text."
        vk.onStav("Posudzujem ako cvičiaci")
        val (ps, s) = posud(vk.klient, vk.nastavenie, u, p, vk.nastavenie.usiliePosudku, onReset = vk.naReset, onVerdikt = { v -> vk.naVerdikt?.invoke(v) }, onText = { d -> vk.naText?.let { vk.onStav(""); it(d) } })
        vk.spotreba += s
        vk.art.posudok = ps
        buildString {
            appendLine("Verdikt: ${ps.verdikt.name.lowercase()} (istota ${ps.istota}).")
            appendLine("Posudok, ktorý študent vidí: ${ps.text}")
            appendLine("PREPIS PAPIERA (prečítaný bez vzoru):"); p.riadky.forEach { appendLine("  $it") }
        }
    },
    Nastroj(
        "gem",
        "Gaussova eliminácia matice programom appky (v ℝ alebo ℤp): kroky v značení kurzu, hodnosť, hlavné stĺpce, riešiteľnosť. Volaj vždy, keď treba upraviť maticu; nikdy nepočítaj GEM sám. Vráti ref krokovača pre značku <krokovac ref=\"…\"/>.",
        obj("riadky" to pole(pole(typ("string")), "riadky matice, čísla ako text: \"-3\", \"1/2\""), "teleso" to typ("string", "\"R\" alebo \"Z5\", \"Z7\"…"), "ciara" to nul("integer", "index stĺpca (od 0), pred ktorým je zvislá čiara rozšírenej matice")),
    ) { vk, a ->
        val f = teleso(a.optString("teleso")) ?: return@Nastroj "Neznáme teleso ${a.optString("teleso")}."
        val r = a.getJSONArray("riadky")
        val rows = (0 until r.length()).map { i -> val row = r.getJSONArray(i); (0 until row.length()).map { j -> bunka(row.getString(j), f) ?: return@Nastroj "Bunku ${row.getString(j)} neviem prečítať." } }
        if (rows.isEmpty() || rows.any { it.size != rows[0].size }) return@Nastroj "Matica nemá rovnako dlhé riadky."
        val bar = (a.opt("ciara") as? Number)?.toInt()
        val e = f.echelon(Mat(rows, bar), bar ?: rows[0].size)
        val w = GemWalk.of(e)
        val ref = vk.art.pridaj(w)
        "ref $ref. Postup: ${walkText(w)}\nHodnosť ${e.pivotsLeft.size}; hlavné stĺpce ${e.pivotsLeft.map { it + 1 }}" +
            (if (bar != null) "; pravá strana ${if (e.consistent) "nie je hlavný stĺpec (sústava má riešenie)" else "je hlavný stĺpec (sústava nemá riešenie)"}" else "") + "."
    },
    Nastroj(
        "la",
        "Overenie vlastností vektorov programom appky (v ℝ alebo ℤp): hodnost, ln (LN alebo LZ), je_baza (je súbor bázou priestoru T^n, ak je obal null, inak bázou obalu daných vektorov), v_obale (patrí vektor do obalu), suradnice (súradnice vektora v DANEJ báze, napr. v študentovej), doplnenie (ktoré jednotkové vektory doplnia LN súbor na bázu T^n). Úlohu s viac správnymi výsledkami (iná báza, iné doplnenie, iný tvar riešenia) posudzuj týmto nástrojom podľa vlastnosti, nikdy zhodou so vzorovým riešením.",
        obj(
            "otazka" to vyber("čo overiť", "hodnost", "ln", "je_baza", "v_obale", "suradnice", "doplnenie"),
            "teleso" to typ("string", "\"R\" alebo \"Z5\", \"Z7\"…"),
            "vektory" to pole(pole(typ("string")), "súbor vektorov (napr. študentova báza), zložky ako text"),
            "obal" to JSONObject().put("type", JSONArray().put("array").put("null")).put("items", pole(typ("string"))).put("description", "pri je_baza: vektory, ktorých obal má súbor generovať; null = celý T^n"),
            "vektor" to JSONObject().put("type", JSONArray().put("array").put("null")).put("items", typ("string")).put("description", "pri v_obale a suradnice: vektor x"),
        ),
    ) { _, a ->
        val f = teleso(a.optString("teleso")) ?: return@Nastroj "Neznáme teleso ${a.optString("teleso")}."
        fun vec(j: JSONArray): List<dev.hamper.tabula.la.Frac>? = (0 until j.length()).map { bunka(j.getString(it), f) ?: return null }
        fun vecs(j: JSONArray?): List<List<dev.hamper.tabula.la.Frac>>? = j?.let { (0 until it.length()).map { i -> vec(it.getJSONArray(i)) ?: return null } }
        val vs = vecs(a.optJSONArray("vektory")) ?: return@Nastroj "Vektory neviem prečítať."
        if (vs.isEmpty() || vs.any { it.size != vs[0].size }) return@Nastroj "Vektory musia mať rovnaký počet zložiek."
        val n = vs[0].size
        val x0 = (a.opt("vektor") as? JSONArray)?.let { vec(it) ?: return@Nastroj "Vektor neviem prečítať." }
        val obal0 = (a.opt("obal") as? JSONArray)?.let { vecs(it) ?: return@Nastroj "Obal neviem prečítať." }
        if ((x0 != null && x0.size != n) || obal0?.any { it.size != n } == true || obal0?.isEmpty() == true) return@Nastroj "Všetky vektory musia mať $n zložiek."
        fun t(v: List<dev.hamper.tabula.la.Frac>) = "(" + v.joinToString(", ") { it.tex(true) } + ")"
        val h = f.rank(vs)
        when (a.optString("otazka")) {
            "hodnost" -> "Hodnosť súboru je $h (vektorov ${vs.size})."
            "ln" -> if (h == vs.size) "Súbor je LN (hodnosť $h = počet vektorov)." else "Súbor je LZ (hodnosť $h < ${vs.size} vektorov)."
            "je_baza" -> {
                // the app's own check of a student's basis (data/Vectors.kt judgeBasis, the same as "Máš inú bázu?"); the
                // count, which the box fixes by its fields, is checked here
                val gens = obal0 ?: List(n) { i -> List(n) { j -> if (i == j) dev.hamper.tabula.la.Frac.ONE else dev.hamper.tabula.la.Frac.ZERO } }
                val dim = f.rank(gens)
                val r = dev.hamper.tabula.data.judgeBasis(f, gens, vs.map { listOf(it) }) { null }
                when {
                    !r.ok -> "Nie: ${r.head} " + r.each.withIndex().filter { it.value != null }.joinToString(" ") { "${it.index + 1}. vektor: ${it.value}" }
                    vs.size != dim -> "Nie: súbor je LN a leží v ${if (obal0 == null) "T^$n" else "obale"}, ale má ${vs.size} vektorov a dimenzia je $dim."
                    else -> "Áno: ${r.head}"
                }
            }
            "v_obale" -> { val x = x0 ?: return@Nastroj "Chýba vektor."; if (f.rank(vs + listOf(x)) == h) "Áno: ${t(x)} patrí do obalu." else "Nie: ${t(x)} do obalu nepatrí (hodnosť stúpne z $h na ${h + 1})." }
            "suradnice" -> {
                val x = x0 ?: return@Nastroj "Chýba vektor."
                if (h < vs.size) return@Nastroj "Súbor je LZ, nie je to báza: súradnice nie sú jednoznačné."
                f.coords(vs, x)?.let { c -> "Súradnice ${t(x)} v báze (" + vs.joinToString(", ") { t(it) } + ") sú ${t(c)}; skúška: " + c.indices.joinToString(" + ") { "${c[it].tex(true)}·${t(vs[it])}" } + " = ${t(x)}." } ?: "Vektor ${t(x)} nie je v obale bázy, súradnice nemá."
            }
            "doplnenie" -> if (h < vs.size) "Súbor je LZ, na bázu sa doplniť nedá." else { val e = f.completeWithUnitVectors(vs); "Doplnenie (stĺpcový postup (A|E), hlavné stĺpce): " + e.joinToString(", ") { "e_${it + 1}" } + ". Iné doplnenie je správne práve vtedy, keď celý súbor je báza (over je_baza)." }
            else -> "Neznáma otázka."
        }
    },
    Nastroj(
        "vlastna_baza",
        "Len pri písomkovom príklade, kde je správnych viac báz (doplnenie na bázu, báza obalu): overí študentovu bázu tou istou kontrolou ako blok appky „Máš inú bázu?“ a ďalšie časti príkladu spočíta v nej (výsledky aj postup). Zadaj vektory tak, ako ich študent zadá do appky: pri doplnení len doplnené vektory, pri báze obalu celú bázu.",
        obj("vektory" to pole(pole(typ("string")), "študentove vektory, zložky ako text")),
        dostupny = { m, _ -> m.priklad?.own != null },
    ) { vk, a ->
        val pr = vk.miesto.priklad ?: return@Nastroj "Tu nie je príklad s vlastnou bázou."
        val own = pr.own ?: return@Nastroj "Tu nie je príklad s vlastnou bázou."
        val f: Field = own.input.p?.let { Field.Zp(it) } ?: Field.R
        val j = a.getJSONArray("vektory")
        val vs = (0 until j.length()).map { i -> j.getJSONArray(i).let { v -> (0 until v.length()).map { bunka(v.getString(it), f) ?: return@Nastroj "Vektory neviem prečítať." } } }
        if (vs.size !in own.input.count) return@Nastroj "Treba ${own.input.count.first}" + (if (own.input.open) "–${own.input.count.last}" else "") + " vektorov, zadaných je ${vs.size}."
        if (vs.any { it.size != own.input.cells.size }) return@Nastroj "Každý vektor má mať ${own.input.cells.size} zložiek."
        val r = own.input.check(vs.map { listOf(it) })
        if (!r.ok) return@Nastroj "Kontrola appky: nie je to správne. ${r.head} " + r.each.withIndex().filter { it.value != null }.joinToString(" ") { "${it.index + 1}. vektor: ${it.value}" }
        val b = own.basis(vs)
        val v = pr.inBasis(vs)
        "Kontrola appky (tá istá ako „Máš inú bázu?“): správne. ${r.head}\nBáza: " + b.joinToString(", ") { "(" + it.joinToString(", ") { x -> x.tex(true) } + ")" } +
            // only the parts the basis changes; the others are the same as in the sample
            v.units.withIndex().drop(own.part + 1).filter { (i, u) -> pr.units.getOrNull(i).let { vz -> vz == null || u.answer != vz.answer || u.solution?.let(::riesenie) != vz.solution?.let(::riesenie) } }.let { zmenene ->
                if (zmenene.isEmpty()) "\nĎalšie časti od bázy nezávisia, platí vzorové riešenie."
                else "\nĎalšie časti v tejto báze (ostatné od bázy nezávisia):\n" + zmenene.joinToString("\n") { (_, u) -> "${u.label} ${u.answer ?: ""}".trim() + (u.solution?.let { s -> "\n" + riesenie(s) } ?: "") }
            } +
            "\nŠtudent môže túto bázu zadať aj v appke pod vzorovým riešením („Máš inú bázu?“); ďalšie časti mu appka ukáže v nej."
    },
    Nastroj(
        "cisla",
        "Teória čísel programom appky: euklid (gcd), rea (rozšírený Euklidov algoritmus, Bézoutove koeficienty), inverzia (a^-1 mod m), mocnina (a^e mod m), lkon (ax ≡ b mod m), crt (sústava x ≡ r_i mod m_i, dvojice r, m), mod (a mod m). Volaj vždy, keď treba také číslo; nikdy ho nepočítaj sám. Tabuľky vrátia ref pre <krokovac ref=\"…\"/>.",
        obj("operacia" to vyber("čo počítať", "euklid", "rea", "inverzia", "mocnina", "lkon", "crt", "mod"), "cisla" to pole(typ("integer"), "euklid/rea: a, b; inverzia: a, m; mocnina: a, e, m; lkon: a, b, m; crt: r1, m1, r2, m2, …; mod: a, m")),
    ) { vk, a ->
        val c = a.getJSONArray("cisla").let { x -> (0 until x.length()).map { (x.get(it) as Number).toLong() } }
        fun need(n: Int) = c.size >= n
        when (a.optString("operacia")) {
            "euklid", "rea" -> {
                if (!need(2) || c[0] <= 0 || c[1] <= 0) return@Nastroj "Treba dve kladné čísla."
                val (x, y) = if (c[0] >= c[1]) c[0] to c[1] else c[1] to c[0]
                val t = rea(x, y)
                val w = if (a.optString("operacia") == "rea") reaWalk(t) else euclidWalk(t)
                val ref = vk.art.pridaj(w)
                "ref $ref. gcd($x, $y) = ${t.gcd}" + (if (a.optString("operacia") == "rea") " = ${t.alpha}·$x + ${t.beta}·$y" else "") + ".\n" + tableText(w)
            }
            "inverzia" -> if (!need(2) || c[1] < 2) "Treba a, m ≥ 2." else inverse(md(c[0], c[1]), c[1])?.let { "${c[0]}^(-1) ≡ $it (mod ${c[1]})." } ?: "Inverzia neexistuje: gcd(${c[0]}, ${c[1]}) = ${gcd(c[0], c[1])} ≠ 1."
            "mocnina" -> if (!need(3) || c[2] < 2 || c[1] < 0) "Treba a, e ≥ 0, m ≥ 2." else if (c[1] == 0L) "${c[0]}^0 ≡ 1 (mod ${c[2]})." else { val w = powWalk(c[0], c[1], c[2]); val ref = vk.art.pridaj(w); "ref $ref. ${c[0]}^${c[1]} ≡ ${powMod(c[0], c[1], c[2])} (mod ${c[2]}).\n" + tableText(w) }
            "lkon" -> if (!need(3) || c[2] < 2) "Treba a, b, m ≥ 2." else lkon(c[0], c[1], c[2])?.let { "${c[0]}x ≡ ${c[1]} (mod ${c[2]}): gcd = ${it.g}, riešenia x ≡ ${it.x0} (mod ${it.n}); v ℤ_${c[2]}: ${it.inZm.joinToString(", ")}." } ?: "Nemá riešenie: gcd(${c[0]}, ${c[2]}) nedelí ${c[1]}."
            "crt" -> if (c.size < 4 || c.size % 2 != 0 || c.chunked(2).any { it[1] < 1 }) "Treba dvojice r, m." else crt(c.chunked(2).map { it[0] to it[1] })?.let { "x ≡ ${it.x} (mod ${it.mod})." } ?: "Sústava nemá riešenie."
            "mod" -> if (!need(2) || c[1] < 1) "Treba a, m ≥ 1." else "${c[0]} mod ${c[1]} = ${md(c[0], c[1])}."
            else -> "Neznáma operácia."
        }
    },
    Nastroj(
        "tvrdenie",
        "Plné znenie vety, definície alebo poznámky zo skrípt kurzu podľa čísla (\"Věta 2.55\", \"Definice 2.49\"). Kartu ukážeš značkou <veta cislo=\"Věta 2.55\"/>.",
        obj("cislo" to typ("string", "druh a číslo, napr. \"Věta 3.7\"")),
    ) { vk, a ->
        val q = a.optString("cislo").trim()
        val m = Regex("""(\p{L}+\.?)\s*(\d+\.\d+)""").find(q) ?: return@Nastroj "Číslo neviem prečítať."
        val kind = refKind(m.groupValues[1]) ?: return@Nastroj "Neznámy druh „${m.groupValues[1]}“."
        val idx = listOfNotNull(RefIndexes[vk.miesto.kurz]) + RefIndexes.values
        val e = idx.firstNotNullOfOrNull { it.entry(kind, m.groupValues[2]) } ?: return@Nastroj "Také číslo v kurze nie je."
        when (val t = e.target) {
            is RefTarget.Thm -> "${t.t.name} (${t.t.src}):\n" + t.t.czech.joinToString("\n") { lineText(it) } + "\nMyšlienka: ${t.t.idea}"
            is RefTarget.Def -> "${t.k.name} (${t.k.src}):\n" + t.k.czech.joinToString("\n") { lineText(it) }
            is RefTarget.Quote -> "${t.n.name}:\n" + t.n.lines.joinToString("\n") { lineText(it) }
            null -> "${e.short}: appka znenie nemá."
        }.replace('§', '$')
    },
    Nastroj(
        "kurz",
        "Čítanie balíkov kurzu (to isté, z čoho píšu autori lekcií): bez súboru zoznam súborov a nadpisov; so súborom a oddielom (začiatok nadpisu, napr. \"## 8. Časté chyby\") text oddielu.",
        obj("subor" to nul("string", "napr. \"la1/kap3-hodnost-regularita.md\", \"dml/README.md\""), "oddiel" to nul("string", "začiatok nadpisu oddielu")),
    ) { vk, a ->
        val s = a.opt("subor") as? String
        if (s == null) {
            val files = KurzKontext.zoznam().filter { it.startsWith(vk.miesto.kurz + "/") || '/' !in it }
            return@Nastroj files.joinToString("\n") { f -> f + ": " + (KurzKontext.subor(f)?.lines()?.filter { it.startsWith("## ") }?.joinToString(" · ") { it.removePrefix("## ") } ?: "") }
        }
        val text = KurzKontext.subor(s) ?: return@Nastroj "Súbor $s nie je."
        val o = (a.opt("oddiel") as? String)?.let { KurzKontext.oddiel(text, it) ?: return@Nastroj "Oddiel „$it“ v $s nie je. Nadpisy: " + text.lines().filter { l -> l.startsWith("#") }.joinToString(" · ") } ?: text
        if (o.length > 20_000) o.lines().filter { it.startsWith("#") }.joinToString("\n", "Oddiel je dlhý, zúž ho na jeden z nadpisov:\n") else o
    },
)

private const val PokynAgentaZaklad = """Si tútor matematiky v appke FITagoras pre študentov 1. ročníka FIT ČVUT (BI-LA1 lineárna algebra, BI-DML diskrétna matematika a logika). Študentovi sa predstavuješ ako Pytagoras. Študent je na mieste v appke, ktoré opisuje MIESTO; môže priložiť fotky svojho papiera a niečo sa pýtať. Bez otázky chce, aby si mu to skontroloval.

Ako pracuješ:
- Čo sa dá vypočítať alebo overiť programom appky, robí NÁSTROJ, nie ty. Ak je na fotke postup GEM a je dostupný over_postup_gem, zavolaj ho. Ak študent odfotil dôkaz, definíciu alebo riešenie a je dostupný posud_papier, zavolaj ho. Úpravu matice počítaj nástrojom gem, gcd, REA, inverzie, mocniny a kongruencie nástrojom cisla. Verdikt nástroja je záväzný: nikdy mu neprotireč a nič k nemu nedopočítavaj.
- Úlohu, ktorá má viac správnych výsledkov (iná báza, iné doplnenie na bázu, iný tvar riešenia sústavy, iný správny postup), nikdy neposudzuj zhodou so vzorovým riešením: over vlastnosť nástrojom la (je to báza? patrí do obalu?) a ďalšie časti úlohy dopočítaj v študentovom výsledku (napr. súradnice v jeho báze).
- Keď na výpočet nástroj nie je, odpovedz z MIESTA a kurzu a povedz jednou vetou, že výpočet appka strojovo neoverila.
- Fakty nástroja len preformuluj: ak hovoria „pravdepodobne“, povedz to tiež; čísla ber z nich, nič nové nepočítaj. O študentovom papieri tvrď len to, čo vieš z nástroja alebo čo na fotke naozaj vidíš. Ak niečo nevieš prečítať, povedz to priamo. Nedomýšľaj, čo si študent myslel.
- Ako študent počítal svoju odpoveď v appke, nevieš, ani keď sa zhoduje s jeho maticou s chybou: nepíš „preto ti vyšlo …“ ani „použil si …“; povedz podmienkovo, čo by z tej matice vyšlo.
- Neprezraď výsledok ani celé riešenie úlohy, kým oň študent výslovne nepožiada („ukáž riešenie“). Inak pomôž ďalším krokom, otázkou alebo nápovedou. Vzorové riešenie v MIESTE je pre teba.
- Drž sa formulácií a značenia kurzu: znenia viet a definícií zo skrípt ber z MIESTA alebo z nástroja tvrdenie, nie z pamäti. Úpravy píš „ř2 − 2·ř1“, hovor „horný stupňovitý tvar (HST)“, „prvý nenulový prvok riadku“, „hlavné a vedľajšie stĺpce“. V DML je $\mathbb{Z}_m$ množina zvyškov $\{0,\dots,m-1\}$ s operáciami modulo m (Definice 6.8), nie zvyškové triedy. Študentov text hodnoť podľa významu, iný správny postup uznaj.
- Odpovedaj ako dobrý cvičiaci, ktorý vidí, kde študent je: po slovensky so všetkou diakritikou, tykaj, ľudsky a k veci, toľko, koľko otázka potrebuje (zvyčajne pár viet). Žiadne nadpisy. Matematiku v $…$ alebo $$…$$ (LaTeX: čísla, písmená, \cdot, \frac, \mathbf, \langle\rangle, \in, \subseteq, \equiv, \pmod, \text{ř}); bez \begin{align}.
- Značky (appka ich vykreslí, inak ich nepíš): <veta cislo="Věta 2.55"/> karta vety alebo definície kurzu; <krokovac ref="k1"/> krokovač z nástroja gem alebo cisla (len ref, ktorý nástroj vrátil). Výsledok over_postup_gem a posud_papier appka ukáže sama, značku na ne nedávaj a neopisuj ich. Nepíš „pozri kartu nižšie“."""

val PokynAgenta: String by lazy {
    fun citatelne(p: String) = p.replace(Regex("""\(\?<?[!=][^)]*\)"""), "").replace("\\b", "").replace("\\p{L}*", "…")
        .replace("\\s+", " ").replace("\\s*", "").replace("[-–]", "-").replace(Regex("""[()|?\\]"""), " ").replace(Regex(" +"), " ").trim()
    PokynAgentaZaklad + "\n- Tieto slová kurz nepoužíva, píš namiesto nich slovo kurzu: " +
        dev.hamper.tabula.data.BannedTerms.filter { "NS" !in it.pattern && "induk" !in it.pattern && "R_" !in it.pattern }.joinToString("; ") { "„${citatelne(it.pattern)}“ → ${it.use}" } + "."
}

/** The message of a question: the course's summary and the place (first question only), tools' results, photos, the question. */
@OptIn(ExperimentalEncodingApi::class)
fun prvaSprava(m: MiestoAppky, otazka: String?, fotky: List<ByteArray>, detail: String = "high", pocetFotiek: Int = fotky.size, vopred: String = "", uvod: Boolean = true): JSONObject {
    val text = buildString {
        if (uvod) {
            val zh = KurzKontext.zhrnutie(m.kurz)
            if (zh.isNotEmpty()) { appendLine("KURZ ${m.kurz.uppercase()}, čo platí v celom predmete:"); zh.forEach { appendLine(it) }; appendLine() }
            appendLine("MIESTO V APPKE: ${m.nazov}")
            appendLine(m.opis.trim())
            m.papier?.let { appendLine(); appendLine("Na tomto mieste je kontrolný zoznam na papier (${it.body.size} bodov).") }
            if (m.gem != null) appendLine("Úloha má postup GEM, ktorý vie appka skontrolovať (nástroj over_postup_gem s fotkou).")
            appendLine()
        }
        if (vopred.isNotEmpty()) append(vopred)
        appendLine(if (pocetFotiek == 0) "FOTKY: žiadne." else "FOTKY: $pocetFotiek (študentov papier)" + if (fotky.isEmpty()) ", prečítané nástrojom vyššie." else ".")
        appendLine("OTÁZKA ŠTUDENTA: " + (otazka?.takeIf { it.isNotBlank() } ?: if (pocetFotiek > 0) "Skontroluj mi to." else "Nerozumiem tomuto miestu, vysvetli mi ho."))
    }
    val content = JSONArray().put(JSONObject().put("type", "input_text").put("text", text))
    for (f in fotky) content.put(JSONObject().put("type", "input_image").put("image_url", "data:image/jpeg;base64," + Base64.Default.encode(f)).put("detail", detail))
    return JSONObject().put("role", "user").put("content", content)
}

/** What one question got: the final text, what the tools made, how many rounds and calls, the usage. */
data class OdpovedAgenta(val text: String, val art: Artefakty, val kola: Int, val volania: List<String>, val spotreba: Spotreba, val ms: Long, val vystupyNastrojov: List<String>,
    /** Where the time went: each model round and each tool, in order. */
    val casy: List<Pair<String, Long>> = emptyList(),
    /** 0 the first text passed the guards, 1 the second, 2 the engine's facts were shown instead. */
    val pokus: Int = 0,
    /** Milliseconds to the first words the student saw (-1: none). */
    val prvyTextMs: Long = -1)

/**
 * One conversation at one place: the questions and answers so far (text only, the photos go with the question they
 * came with). Caps from the plan: 4 rounds and 10 tool calls per question; the last round may not call tools.
 */
class Rozhovor(val miesto: MiestoAppky, private val klient: ResponsesKlient, private val nastavenie: Nastavenie, private val usilie: String? = "low",
    /** false: the photos go straight to the model, without the blind reading tools (measured against them in the harness). */
    private val citatNastrojom: Boolean = true) {
    val historia = ArrayList<JSONObject>()
    val art = Artefakty()

    /**
     * The student stopped an answer: the question and what was written stay in the history, so a follow-up refers to
     * what is on screen. Not for the first question (its message carries the place; the next one opens with it again).
     */
    fun zastavene(otazka: String?, text: String) {
        if (historia.isEmpty()) return
        historia += JSONObject().put("role", "user").put("content", otazka ?: "Skontroluj mi to.")
        historia += JSONObject().put("role", "assistant").put("content", text + "\n(Študent odpoveď tu zastavil.)")
    }

    /**
     * One question. With photos at a place that has a reading tool (a GEM to check, a checklist to judge), the app runs
     * that tool first and the model gets its result as text, not the photos: the photos are read once, blind, and the
     * model needs one round less (docs/TUTOR.md, "Čas"). Elsewhere the photos go to the model as they are.
     */
    suspend fun opytaj(otazka: String?, fotky: List<ByteArray>, onStav: (String) -> Unit = {}, onReset: () -> Unit = {}, onVerdikt: (Posudok.Verdikt) -> Unit = {}, onText0: (String) -> Unit = {}): OdpovedAgenta {
        val t0 = dev.hamper.tabula.compat.currentTimeMillis()
        // when the student saw the first words (the harness measures it)
        var prvy = -1L
        val onText: (String) -> Unit = { d -> if (prvy < 0 && d.isNotBlank()) prvy = dev.hamper.tabula.compat.currentTimeMillis() - t0; onText0(d) }
        val casy = ArrayList<Pair<String, Long>>()
        val vk = VolanieKontext(miesto, fotky, klient, nastavenie, art, onStav)
        val volania = ArrayList<String>(); val vystupy = ArrayList<String>()
        // what the tools of an earlier question found is not this question's (an unreadable new page must not get the old verdict)
        art.postup = null; art.postupKontext = null; art.posudok = null; art.prepisPapiera = null
        /** [povinny]: the photos' own reading; its failure is the turn's (the student gets "Skúsiť znova"), not a note to the model. */
        suspend fun spusti(n: Nastroj, args: JSONObject, povinny: Boolean = false): String {
            val t = dev.hamper.tabula.compat.currentTimeMillis()
            volania += n.nazov
            onStav(StavNastroja[n.nazov] ?: "Overujem")
            val out = try { n.spusti(vk, args) } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: TutorChyba) {
                if (povinny || e.druh == TutorChyba.Druh.LIMIT || e.druh == TutorChyba.Druh.OFFLINE || e.druh == TutorChyba.Druh.ODPOJENE) throw e; "Nástroj zlyhal: ${e.kod}."
            } catch (e: Throwable) { if (povinny) throw TutorChyba(TutorChyba.Druh.INE, "tool_failed", e::class.simpleName ?: "chyba"); "Nástroj zlyhal: ${e::class.simpleName}." }
            casy += "nástroj ${n.nazov}" to dev.hamper.tabula.compat.currentTimeMillis() - t
            vystupy += out
            return out
        }
        // the photos' own tool first, by the app (a GEM to check, else a checklist to judge)
        val citanie = if (fotky.isEmpty() || !citatNastrojom) emptyList() else Nastroje.filter { (it.nazov == "over_postup_gem" || it.nazov == "posud_papier") && it.dostupny(miesto, true) }
        val vopred = StringBuilder()
        // no question: a paper's judgement is the answer and streams as it is written
        if (otazka.isNullOrBlank()) { vk.naText = onText; vk.naReset = onReset; vk.naVerdikt = onVerdikt }
        for (n in citanie) {
            val out = spusti(n, JSONObject(), povinny = true)
            vopred.append("VÝSLEDOK NÁSTROJA ${n.nazov} (appka ho spustila na študentove fotky; fotky nevidíš, toto je všetko, čo je na nich):\n").append(out).append("\n\n")
            // a GEM found: no paper judgement needed; none found: the checklist, if the place has one, judges the paper
            if (n.nazov == "over_postup_gem" && art.postup?.prepis?.gemy?.isNotEmpty() == true) break
        }
        // a paper judged with no question asked: the judgement is the answer (it streamed already); a model round would
        // only say it again, ~8 s later
        vk.naText = null; vk.naVerdikt = null
        val ps = art.posudok
        if (otazka.isNullOrBlank() && "posud_papier" in volania && ps != null && art.postup?.prepis?.gemy.isNullOrEmpty()) {
            val text = ps.text
            // the whole first message stays in the history: a follow-up ("ukáž vzor") needs the place too
            historia += JSONObject().put("role", "user").put("content", textSpravy(prvaSprava(miesto, null, emptyList(), nastavenie.detail, fotky.size, vopred.toString(), uvod = historia.isEmpty())))
            historia += JSONObject().put("role", "assistant").put("content", text)
            return OdpovedAgenta(text, art, 0, volania, vk.spotreba, dev.hamper.tabula.compat.currentTimeMillis() - t0, vystupy, casy, 0, prvy)
        }
        // a reading that found nothing to check (no GEM, no paper to judge) leaves the photos to the model itself
        val nicNecitane = art.postup?.hlavny == null && art.posudok == null
        val fotkyModelu = if (citanie.isEmpty() || nicNecitane) fotky else emptyList()
        val dostupne = Nastroje.filter { it !in citanie && it.dostupny(miesto, fotkyModelu.isNotEmpty()) && (citatNastrojom || (it.nazov != "over_postup_gem" && it.nazov != "posud_papier")) }
        val tools = JSONArray().apply {
            dostupne.forEach { n -> put(JSONObject().put("type", "function").put("name", n.nazov).put("description", n.popis).put("parameters", n.parametre).put("strict", true)) }
        }
        val user = prvaSprava(miesto, otazka, fotkyModelu, nastavenie.detail, fotky.size, vopred.toString(), uvod = historia.isEmpty())
        val input = JSONArray().apply { historia.forEach { put(it) }; put(user) }
        var spotreba = Spotreba()
        var kolo = 0
        var text = ""
        var opravene = false
        var pokus = 0
        val overenyPostup = art.postup?.takeIf { "over_postup_gem" in volania && it.hlavny != null }
        while (true) {
            kolo++
            val posledne = kolo >= 4 || volania.size >= 10
            val telo = JSONObject().put("model", nastavenie.modelVysvetlenia).put("instructions", PokynAgenta).put("input", input)
                .put("include", JSONArray().put("reasoning.encrypted_content"))
            if (tools.length() > 0) { telo.put("tools", tools); if (posledne) telo.put("tool_choice", "none") }
            usilie?.let { telo.put("reasoning", JSONObject().put("effort", it)) }
            onReset()
            onStav(if (kolo == 1 && vopred.isEmpty()) "Premýšľam" else "Píšem odpoveď")
            val tk = dev.hamper.tabula.compat.currentTimeMillis()
            var pise = false
            val o = klient.odpoved(telo) { d -> if (!pise) { pise = true; onStav("") }; onText(d) }
            casy += "model, kolo $kolo (${o.usage.vstup} tok. vstup, ${o.usage.uvazovanie} uvažovanie)" to dev.hamper.tabula.compat.currentTimeMillis() - tk
            spotreba += o.usage
            val calls = o.volania
            if (calls.isEmpty() || posledne) {
                text = o.text
                // after a checking tool the case-1 guards hold: every number from the input or a tool, no step after the
                // wrong one, no guessed reason; once more with the reason, then the engine's facts themselves
                val zdroj = textSpravy(user) + vystupy.joinToString("\n") + PokynAgenta
                val dovod = if (vystupy.isEmpty() || volania.none { it == "over_postup_gem" || it == "posud_papier" }) null
                    else cislaMimo(text, zdroj).takeIf { it.isNotEmpty() }?.let { "Tieto čísla nie sú vo vstupe ani vo výstupe nástrojov: ${it.joinToString(", ")}." }
                        ?: overenyPostup?.takeIf { otazka.isNullOrBlank() }?.let { v -> art.postupKontext?.let { k -> kontrolaVysvetlenia(text, vstupVysvetlenia(k, v), v.hlavny) } }
                if (dovod == null) break
                if (opravene || posledne) {
                    // the facts as the student reads them, when the model's words fail twice
                    overenyPostup?.let { v -> art.postupKontext?.let { k -> text = faktyPreStudenta(k, v); onReset(); onText(text); pokus = 2 } }
                    break
                }
                opravene = true; pokus = 1
                input.put(JSONObject().put("role", "assistant").put("content", text))
                input.put(JSONObject().put("role", "user").put("content", "$dovod Napíš odpoveď znova len z faktov nástroja; nič nepočítaj ani nedomýšľaj."))
                continue
            }
            o.polozky.forEach { input.put(it) }
            for (c in calls) {
                val n = dostupne.firstOrNull { it.nazov == c.nazov }
                val out = if (volania.size >= 10) "Strop 10 volaní nástrojov na otázku je minutý; odpovedz z toho, čo už máš." else if (n == null) "Nástroj ${c.nazov} tu nie je."
                    else spusti(n, runCatching { JSONObject(c.argumenty) }.getOrElse { JSONObject() })
                input.put(JSONObject().put("type", "function_call_output").put("call_id", c.id).put("output", out))
            }
        }
        spotreba += vk.spotreba
        // the history keeps text only: the question with the tools' results (photos never) and the answer
        historia += JSONObject().put("role", "user").put("content", textSpravy(user) + (if (fotky.isNotEmpty() && vopred.isEmpty()) "\n(Priložené fotky: ${fotky.size}. Výstupy nástrojov: " + vystupy.joinToString(" | ").take(4000) + ")" else ""))
        historia += JSONObject().put("role", "assistant").put("content", text)
        return OdpovedAgenta(text, art, kolo, volania, spotreba, dev.hamper.tabula.compat.currentTimeMillis() - t0, vystupy, casy, pokus, prvy)
    }
}

/** What the student sees while a tool runs (the panel shows it instead of an empty spinner). */
val StavNastroja = mapOf(
    "over_postup_gem" to "Čítam papier",
    "posud_papier" to "Čítam papier",
    "gem" to "Počítam GEM",
    "la" to "Overujem výpočet",
    "vlastna_baza" to "Overujem tvoju bázu",
    "cisla" to "Overujem výpočet",
    "tvrdenie" to "Hľadám vetu v skriptách",
    "kurz" to "Hľadám v materiáloch kurzu",
)

/** The text of a message (its input_text parts), without the photos. */
private fun textSpravy(m: JSONObject): String = (m.opt("content") as? String) ?: m.optJSONArray("content")?.let { a ->
    (0 until a.length()).mapNotNull { a.optJSONObject(it)?.takeIf { p -> p.optString("type") == "input_text" }?.optString("text") }.joinToString("\n")
}.orEmpty()

/** Numbers in [text] that [zdroj] doesn't have (\frac{a}{b} as a/b, the sign aside): the guard against computing. */
fun cislaMimo(text: String, zdroj: String): Set<String> {
    val num = Regex("(?<![A-Za-z\\\\_^])-?\\d+(?:/\\d+)?")
    fun cisla(t: String) = num.findAll(Regex("\\\\[dt]?frac\\{(-?\\d+)\\}\\{(\\d+)\\}").replace(t.replace('−', '-')) { "${it.groupValues[1]}/${it.groupValues[2]}" }).map { it.value.removePrefix("-") }.toSet()
    return cisla(text) - cisla(zdroj)
}

/* ---- tags --------------------------------------------------------------------------------------------------------- */

sealed interface Kus {
    data class Text(val t: String) : Kus
    data class Veta(val cislo: String) : Kus
    data class Krokovac(val ref: String) : Kus
}

private val znacka = Regex("""<(veta|krokovac)\s+(cislo|ref)\s*=\s*"([^"]+)"\s*/?>""")

/** The answer split into text and the blocks the app draws; an unknown tag or ref is dropped (and the text keeps no hole). */
fun kusy(text: String, art: Artefakty, kurz: String): List<Kus> {
    val out = ArrayList<Kus>()
    var at = 0
    for (m in znacka.findAll(text)) {
        if (m.range.first > at) out += Kus.Text(text.substring(at, m.range.first))
        at = m.range.last + 1
        when (m.groupValues[1]) {
            "veta" -> if (vetaCiel(m.groupValues[3], kurz) != null) out += Kus.Veta(m.groupValues[3])
            "krokovac" -> if (m.groupValues[3] in art.krokovace) out += Kus.Krokovac(m.groupValues[3])
        }
    }
    if (at < text.length) out += Kus.Text(text.substring(at))
    // a tag still streaming ("<krok") is not shown as text
    return out.map { if (it is Kus.Text) Kus.Text(it.t.replace(Regex("<(?:v|ve|vet|veta|k|kr|kro|krok|kroko|krokov|krokova|krokovac)(\\s[^>]*)?$"), "").replace(Regex("<(?:veta|krokovac)\\b[^>]*>"), "")) else it }.filter { it !is Kus.Text || it.t.isNotBlank() }
}

/** The card a `<veta cislo="…"/>` stands for. */
fun vetaCiel(cislo: String, kurz: String): RefTarget? {
    val m = Regex("""(\p{L}+\.?)\s*(\d+\.\d+)""").find(cislo) ?: return null
    val kind = refKind(m.groupValues[1]) ?: return null
    return (listOfNotNull(RefIndexes[kurz]) + RefIndexes.values).firstNotNullOfOrNull { it.entry(kind, m.groupValues[2])?.target }
}

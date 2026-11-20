package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.la.RowOp
import dev.hamper.tabula.la.opsNote
import dev.hamper.tabula.la.rank
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/*
 * The fixed chain of a photographed GEM after a wrong answer (docs/TUTOR.md): the model reads the photos blind, the
 * engine finds the first wrong step and computes the right one, the model explains in a few sentences. Two calls, no
 * tool loop. The headline and every red mark come from the engine, never from the model. The harness on the Mac runs
 * these same functions.
 */

/** Which model and effort each call uses; the harness measures them, the student never chooses. */
data class Nastavenie(
    val modelCitania: String,
    val usilieCitania: String? = "medium",
    val modelVysvetlenia: String,
    val usilieVysvetlenia: String? = "low",
    val detail: String = "high",
    /** The paper's judgement (a written proof or definition): the one step that weighs an argument, not a cell. */
    val usiliePosudku: String? = "low",
) {
    companion object {
        /**
         * In order of preference; the first one the account lists is taken (GET /v1/models). The model matrix of
         * 18. 11. (docs/TUTOR.md, "Modely"): reading handwriting, gpt-6.1-terra at low effort was as right as gpt-6.2-sol
         * on Teo's real pages (17/17, every wrong cell read as written) and on 20 sheets with errors (9/9 as written,
         * none corrected, 0 false alarms) in 12 s instead of 23–32 s; words and judgements stay with gpt-6.2-sol low
         * (judges 4.88 against terra's 4.38, the paper's verdict 15/16 either way).
         */
        val CitaniePoradie = listOf("gpt-6.1-terra", "gpt-6.2-sol", "gpt-6.1-sol", "gpt-6.2", "gpt-6.1", "gpt-6")
        val VysvetleniePoradie = listOf("gpt-6.2-sol", "gpt-6.1-sol", "gpt-6.2", "gpt-6.1", "gpt-6")

        fun z(modely: List<String>): Nastavenie? {
            fun pick(order: List<String>) = order.firstNotNullOfOrNull { want -> modely.firstOrNull { it == want } } ?: order.firstNotNullOfOrNull { want -> modely.firstOrNull { it.startsWith(want) } }
            val c = pick(CitaniePoradie) ?: return null
            // terra reads at low effort; a sol in its place keeps the effort measured for it in round 3 (low once
            // "corrected" a wrong cell there)
            return Nastavenie(c, usilieCitania = if (c.startsWith("gpt-6.1-terra")) "low" else "medium", modelVysvetlenia = pick(VysvetleniePoradie) ?: c)
        }
    }
}

/** What the student did in the app before photographing: the exercise, their answer and what the app said to it. */
data class Kontext(
    val zadanie: String,
    val otazka: String,
    val odpovedStudenta: String?,
    val spravnaOdpoved: String,
    /** The app's own feedback on the wrong answer (the recognised mistake); the tutor adds to it, never contradicts it. */
    val spatnaVazba: String?,
    /** "Ako na to?": the method's goals, as the course teaches them. */
    val postup: List<String>,
    val ulohy: List<GemUloha>,
    /** How the exercise's own solution goes on after its GEM (its sentences): what to read off the last matrix. */
    val zaver: List<String> = emptyList(),
    /** The columns of the first nonzero elements in the solution's last GEM matrix ([zaver] is about that matrix). */
    val zaverStlpce: List<Int>? = null,
    /** Per GEM of [ulohy]: the step of the method (index into [kroky]) it belongs to; the question's own step. */
    val krokUlohy: List<Int?> = emptyList(),
    val krokOtazky: Int? = null,
    val kroky: List<String> = emptyList(),
    /** The typed answer against the right one, when only some components differ. */
    val rozdiel: String? = null,
    /** The right answer's numbers and their labels ("u_1"…), for a typed answer: is the right vector on the paper? */
    val ocakavane: List<Frac>? = null,
    val menovky: List<String> = emptyList(),
    val zle: List<Int> = emptyList(),
)

/* ---- 1. reading ------------------------------------------------------------------------------------------------- */

@OptIn(ExperimentalEncodingApi::class)
fun teloCitania(n: Nastavenie, k: Kontext, fotky: List<ByteArray>): JSONObject {
    val content = JSONArray().put(JSONObject().put("type", "input_text").put("text",
        "Assignment the student was solving (for context only; do not use it to read digits):\n${k.zadanie}\n\nTranscribe the ${fotky.size} photo(s) below."))
    for (f in fotky) content.put(JSONObject().put("type", "input_image").put("image_url", "data:image/jpeg;base64," + Base64.Default.encode(f)).put("detail", n.detail))
    val telo = JSONObject().put("model", n.modelCitania).put("instructions", PokynCitania)
        .put("input", JSONArray().put(JSONObject().put("role", "user").put("content", content)))
        .put("text", JSONObject().put("format", JSONObject().put("type", "json_schema").put("name", "prepis").put("strict", true).put("schema", schemaPrepis())))
    n.usilieCitania?.let { telo.put("reasoning", JSONObject().put("effort", it)) }
    return telo
}

/** [onMatica]: how many matrices the transcript has so far, as the JSON streams in (for "Čítam 3. maticu…"). */
suspend fun citaj(klient: ResponsesKlient, n: Nastavenie, k: Kontext, fotky: List<ByteArray>, onMatica: (Int) -> Unit = {}): Pair<Prepis, Odpoved> {
    val sb = StringBuilder()
    var seen = 0
    val o = klient.odpoved(teloCitania(n, k, fotky)) { d ->
        sb.append(d)
        val c = Regex("\"riadky\"").findAll(sb).count()
        if (c != seen) { seen = c; onMatica(c) }
    }
    val p = prepisZJson(o.text) ?: throw TutorChyba(TutorChyba.Druh.INE, "bad_transcript", "Prepis nie je platný JSON")
    return p to o
}

/* ---- 2. the engine ---------------------------------------------------------------------------------------------- */

/** The verdict on the photos: each GEM block checked, the one to show first (a mistake before a clean one). */
data class Vysledok(val prepis: Prepis, val verdikty: List<Verdikt>) {
    val hlavny: Verdikt? get() = verdikty.minByOrNull { poradie(it) }
    val necitatelne: List<Strana> get() = prepis.strany.filter { it.citatelnost == Strana.Citatelnost.NECITATELNE }
    val ineBloky: List<Blok> get() = prepis.bloky.filter { it.druh != "gem" }

    private fun poradie(v: Verdikt) = when (v.stav) {
        Verdikt.Stav.CHYBA -> 0
        Verdikt.Stav.OTAZKA -> 1
        Verdikt.Stav.NEDOKONCENE -> 2
        Verdikt.Stav.BEZ_CHYBY -> 3
        Verdikt.Stav.NECITATELNE, Verdikt.Stav.NESUDRZNE -> 4
        Verdikt.Stav.INE_ZADANIE -> 5
    }
}

fun vyhodnot(ulohy: List<GemUloha>, prepis: Prepis): Vysledok = spojPokracovania(ulohy, prepis).let { p -> Vysledok(p, p.gemy.map { overPostup(ulohy, it.gem) }) }

/**
 * One chain the reader split in two (it went on on the next line, Teo's page 18. 11.): a GEM block whose first matrix
 * has the same rows' span as the previous block's last one and is at least as far reduced continues it, so it is
 * joined. Otherwise the first half would be "not finished yet", a sentence that is not true.
 */
internal fun spojPokracovania(ulohy: List<GemUloha>, prepis: Prepis): Prepis {
    val f = ulohy.firstOrNull()?.pole ?: return prepis
    fun mat(m: PrepisMatica): List<List<dev.hamper.tabula.la.Frac>>? = m.riadky.map { r -> r.map { c -> bunka(c.v, f) ?: return null } }
    fun nuly(m: List<List<dev.hamper.tabula.la.Frac>>) = m.map { r -> r.indexOfFirst { !it.isZero }.let { if (it < 0) r.size else it } }.sorted()
    fun pokracuje(a: GemPrepis, b: GemPrepis): Boolean {
        val x = a.matice.lastOrNull { !it.preskrtnuta }?.let(::mat) ?: return false
        val y = b.matice.firstOrNull { !it.preskrtnuta }?.let(::mat) ?: return false
        if (x.isEmpty() || y.isEmpty() || x.size != y.size || x[0].size != y[0].size || x == y) return false
        val h = f.rank(x)
        if (f.rank(y) != h || f.rank(x + y) != h) return false
        return nuly(x).zip(nuly(y)).all { (p, q) -> q >= p }
    }
    val out = ArrayList<Blok>()
    for (b in prepis.bloky) {
        val prev = out.lastOrNull()
        if (prev != null && prev.druh == "gem" && b.druh == "gem" && b.strana == prev.strana && prev.gem.matice.isNotEmpty() && b.gem.matice.isNotEmpty() && pokracuje(prev.gem, b.gem)) {
            // the "~" between the halves has no note of its own
            out[out.lastIndex] = prev.copy(gem = prev.gem.copy(matice = prev.gem.matice + b.gem.matice,
                operacie = prev.gem.operacie.take(prev.gem.matice.size - 1).let { it + List(prev.gem.matice.size - 1 - it.size) { null } } + listOf(null) + b.gem.operacie,
                zaver = b.gem.zaver ?: prev.gem.zaver))
        } else out += b
    }
    return if (out.size == prepis.bloky.size) prepis else prepis.copy(bloky = out)
}

/**
 * The engine's answer after the student said what is in one cell ("Je tu 4 alebo 9?", "Prečítal som zle?"): the chain
 * with that value, checked again with every cell the student vouched for ([potvrdene], kept-matrix indices, this one
 * included) never asked about again. Returns the corrected chain too, so a further answer builds on it.
 */
fun odpovedzOtazku(ulohy: List<GemUloha>, gem: GemPrepis, miesto: Miesto, volba: Frac, potvrdene: Set<Miesto> = emptySet()): Pair<GemPrepis, Verdikt> {
    val kept = gem.matice.withIndex().filter { !it.value.preskrtnuta }.map { it.index }
    val mi = kept.getOrNull(miesto.matica) ?: return gem to overPostup(ulohy, gem, potvrdene)
    val mats = gem.matice.mapIndexed { i, m ->
        if (i != mi) m else m.copy(riadky = m.riadky.mapIndexed { r, row -> if (r != miesto.riadok) row else row.mapIndexed { c, cell -> if (c == miesto.stlpec) Citanie(volba.toString()) else cell } })
    }
    val g = gem.copy(matice = mats)
    return g to overPostup(ulohy, g, potvrdene + miesto)
}

/* ---- the headline, written by the app ------------------------------------------------------------------------- */

private fun Frac.t() = "$" + tex(true) + "$"

/** One sentence that says what the engine found; the model's text comes under it. */
fun nadpis(v: Verdikt?, vysledok: Vysledok? = null): String {
    if (v == null) return when {
        vysledok != null && vysledok.ineBloky.isNotEmpty() -> "Na fotke nevidím postup GEM, ten appka zatiaľ nekontroluje."
        else -> "Na fotke som nenašiel postup GEM."
    }
    val k = v.krok
    return when (v.stav) {
        Verdikt.Stav.BEZ_CHYBY -> if (v.matice.size == 1) "Na papieri je len matica, už v cieľovom tvare. Chyba je až v tom, čo z nej vyvodzuješ."
            else "Na papieri máš postup GEM správne. Chyba je až v tom, čo z neho vyvodzuješ."
        Verdikt.Stav.NEDOKONCENE -> "Úpravy, ktoré vidím, sedia. Posledná matica na fotke ešte nie je " + (if (v.spravneUpravy != null) "hotová" else "v HST") + "; ak máš na papieri ďalšiu, odfoť celý postup."
        Verdikt.Stav.INE_ZADANIE -> "Na fotke je iná matica než v tejto úlohe."
        Verdikt.Stav.NECITATELNE -> "Nepodarilo sa mi prečítať celý postup."
        Verdikt.Stav.NESUDRZNE -> "Neviem spoľahlivo prečítať, ktorá matica je ktorá."
        Verdikt.Stav.OTAZKA -> v.otazka!!.let { q -> "Je v ${q.miesto.riadok + 1}. riadku ${q.moznosti[0].t()} alebo ${q.moznosti[1].t()}?" }
        Verdikt.Stav.CHYBA -> {
            val cell = v.bunky.singleOrNull()
            val riadky = v.riadky.sorted().map { it + 1 }
            val kde = if (riadky.size == 1) "v ${riadky[0]}. riadku" else "v riadkoch ${riadky.joinToString(" a ")}"
            when (v.typ) {
                Verdikt.Typ.ZADANIE -> if (cell != null) "Prvá matica je na papieri inak: ${kde} má byť ${cell.maloByt.t()}, nie ${cell.napisane.t()}." else "Prvá matica nie je celkom tá zo zadania, $kde."
                Verdikt.Typ.NEPOVOLENA -> "$k. úprava nie je ekvivalentná, hodnosť po nej klesla ($kde)."
                Verdikt.Typ.ODPIS -> if (cell != null) "V $k. úprave je $kde zle odpísané číslo: má byť ${cell.maloByt.t()}, nie ${cell.napisane.t()}." else "V $k. úprave je $kde zle odpísané číslo."
                else -> when {
                    cell == null -> "Chyba je v $k. úprave, $kde."
                    cell.iste -> "Chyba je v $k. úprave, $kde: má byť ${cell.maloByt.t()}, nie ${cell.napisane.t()}."
                    else -> "Chyba je v $k. úprave, $kde: najskôr má byť ${cell.maloByt.t()}, nie ${cell.napisane.t()}."
                }
            }
        }
    }
}

/* ---- 3. the explanation ------------------------------------------------------------------------------------- */

private const val PokynZaklad = """Si tútor matematiky v appke FITagoras pre študentov 1. ročníka FIT ČVUT (BI-LA1). Študentovi sa predstavuješ ako Pytagoras. Študent odpovedal v appke zle, odfotil svoj postup na papieri a program ho skontroloval. Tvoja úloha: krátko vysvetliť, čo program našiel.

Pravidlá:
- Píš po slovensky, tykaj, vecne a priateľsky. 2 až 4 vety, najviac 90 slov. Žiadne nadpisy ani odrážky.
- O krokoch rozhodol program. Všetko, čo tvrdíš o študentovom postupe (čo spravil, kde, prečo je to chyba), smie byť len preformulovaný FAKT zo vstupu. Nič k faktom nepridávaj: žiadne domnienky, čo si študent myslel, čo počítal alebo prečo zadal svoju odpoveď. Ak fakt hovorí „pravdepodobne“, povedz to tiež.
- Prečo študent zadal svoju odpoveď, nevieš: vetu „chyba je v závere“ nedopĺňaj o „pretože/lebo …“ a nepíš, čo si s čím zamenil alebo čo si myslel. Povedz, čo z matice vyplýva.
- Ako študent svoju odpoveď v appke počítal, nevieš, ani keď sa zhoduje s maticou s chybou: nepíš „preto ti vyšlo …“ ani „použil si hodnosť …“. Povedz to podmienkovo o matici: „z matice s chybou by vyšla dimenzia 3, správne je …“.
- Ak vo vstupe nie je ODPOVEĎ ŠTUDENTA, netvrď nič o tom, čo zadal alebo vyvodil; len ukáž, čo z matice vyplýva.
- Nepočítaj. Každé číslo, vektor a maticu ber z faktov. Nové čísla nevymýšľaj.
- Neprezraď výsledok úlohy ani ďalšie kroky za tými, ktoré sú vo faktoch. Nikdy nenapíš, čo má študent zadať do appky.
- Tvar ako dobrá spätná väzba v lekcii: prvá veta povie, čo sa na papieri stalo a prečo to nesedí; ďalšia konkrétne, čo urobiť (ktorú úpravu napísať alebo aké číslo kam prepísať, podľa faktov). Ak je vo faktoch odvodenie bunky, ukáž ho.
- Ak FAKTY uvádzajú pravdepodobný dôvod chyby, je to najcennejšia veta: povedz ho hneď v prvej vete ako odhad („Asi si …“, „Vyzerá to, že si …“) aj s výpočtom z faktu a potom, ako to má byť. Iný dôvod si nevymýšľaj; keď ho fakty neuvádzajú, žiadny neuvádzaj.
- Ak fakty porovnávajú poslednú maticu na papieri s tým, čo by dali správne úpravy: keď chyba hlavné stĺpce nezmenila, povedz, že na záver (hodnosť, regulárnosť, LZ…) táto chyba nevplýva; keď ich zmenila, povedz, že rozdiel v poslednej matici (napr. nenulový riadok navyše alebo menej) spôsobila táto chyba. Hodnosť ani iný záver z matice s chybou nikdy neuveď ako platný.
- Ak fakty hovoria, že správna hodnota je na papieri celá, povedz, že na papieri to máš dobre a líši sa len to, čo si zadal do appky (preklep pri prepisovaní). Ak hovoria, že GEM na fotke patrí až k neskoršiemu kroku, ten krok nevysvetľuj.
- Keď sú úpravy správne a chyba je v závere: dotiahni úvahu z údajov o poslednej matici až k SPRÁVNEJ ODPOVEDI zo vstupu (appka ju už ukázala), jedným reťazcom dôsledkov podľa kurzu, napr. „vedľajší stĺpec → voľná neznáma → netriviálne riešenie → LZ“, „pravá strana nie je hlavný stĺpec → sústava má riešenie → vektor do obalu patrí“, „hodnosť h, n neznámych → dimenzia riešení n − h“, „dimenzia 1 → priamka cez počiatok“, „v každom riadku hlavný stĺpec → generuje celý priestor“. Hovor o tom, čo z matice vyplýva, nie o tom, čo si študent myslel. Ak fakty uvádzajú rozdiel odpovede po zložkách, povedz to najprv.
- Keď je na papieri chyba, otázka je o hodnosti, LZ/LN, riešiteľnosti, regulárnosti, dimenzii alebo počte a fakty opisujú aj poslednú maticu na papieri (aj s chybou): ak z nej tým istým reťazcom vyplýva iná odpoveď, než študent zadal, povedz jednou vetou, že chyba je aj v závere (ani z matice na papieri nevyplýva jeho odpoveď). Inak poslednú maticu spomeň len podľa predošlého pravidla; nič nepočítaj.
- Píš správne po slovensky so všetkou diakritikou (napr. „Vymeň“, „riadku“). Matice pomenúvaj ako vo faktoch („matica po 2. úprave“), nie „druhá matica“.
- Príklad dobrej odpovede pri chybe v bunke: „V 3. úprave si od 3. riadku odčítal dvojnásobok 2. riadku, ale v 3. stĺpci vyšlo $1-2\cdot 0=1$, nie $0$. Prepíš tam $0$ na $1$ a ďalšie úpravy sprav z opravenej matice.“
- Príklad s pravdepodobným dôvodom: „Asi si v 2. úprave násobok pripočítal namiesto odčítal: $4+2\cdot 3=10$, ale $\text{ř}2-2\cdot\text{ř}1$ dáva $4-2\cdot 3=-2$. Prepíš v 2. riadku $10$ na $-2$ a pokračuj z opravenej matice. Na hodnosť táto chyba nevplýva, hlavné stĺpce ostanú rovnaké.“
- Spätnú väzbu, ktorú appka už dala k odpovedi, neopakuj; nadväzuj na ňu.
- Názvy a značenie z kurzu: „horný stupňovitý tvar (HST)“, „prvý nenulový prvok riadku“, úpravy „ř2 − 2·ř1“ (nie R2), „hlavné a vedľajšie stĺpce“, „hodnosť“. Matematiku píš v $…$ (LaTeX), napr. $\text{ř}2-2\cdot\text{ř}1$. V $…$ používaj len čísla, $+$, $-$, $\cdot$, $=$, zlomky \frac, \text{ř} a pri $\mathbb{Z}_p$ \equiv a \pmod; žiadne šípky ani iné príkazy (úpravu napíš ako $\text{ř}2-2\cdot\text{ř}1$, nie so šípkou).
- Nepíš vety typu „pozri kartu nižšie“; appka ukáže tvoj krokovač a správny ďalší krok sama."""

private fun mtex(m: List<List<Frac?>>, bar: Int?): String {
    val spec = if (bar == null) "" else "|$bar"
    return "\\begin{pmatrix$spec}" + m.joinToString("\\\\") { r -> r.joinToString("&") { it?.tex(true) ?: "?" } } + "\\end{pmatrix}"
}

private fun ops(o: List<RowOp>?) = o?.takeIf { it.isNotEmpty() }?.let { opsNote(it) } ?: "(úpravy nenapísané)"

private fun inverzia(k: Kontext, v: Verdikt): Boolean {
    val u = k.ulohy.getOrNull(v.uloha ?: 0) ?: return false
    if (u.ciel == GemUloha.Ciel.JEDNOTKOVA) return true
    // (A|E) of a singular A: the solution stops at a zero row, but right of the bar stands E, not a right-hand side
    val bar = u.start.bar ?: return false
    return u.start.n == 2 * bar && u.start.m == bar && u.start.rows.indices.all { i -> (0 until bar).all { j -> u.start.rows[i][bar + j] == if (i == j) Frac.ONE else Frac.ZERO } }
}

private fun pocetRiadkov(n: Int) = if (n == 1) "je 1 nenulový riadok" else if (n in 2..4) "sú $n nenulové riadky" else "je $n nenulových riadkov"

/** Every cell read and the first nonzero elements step to the right, zero rows at the bottom; [bar]: only the left block. */
private fun hst(m: List<List<Frac?>>, bar: Int?): Boolean {
    if (m.any { r -> r.any { it == null } }) return false
    val leads = m.map { r -> r.take(bar ?: r.size).indexOfFirst { !it!!.isZero } }
    return leads.filter { it >= 0 }.zipWithNext().all { (a, b) -> a < b } && leads.dropWhile { it >= 0 }.all { it < 0 }
}

/** "je n nenulových riadkov; hlavné stĺpce sú …; pravá strana (nie) je hlavný stĺpec." of a matrix in its target form. */
private fun zhrnutie(m: List<List<Frac?>>, bar: Int?, inverzia: Boolean): String {
    val lead = m.mapNotNull { r -> r.indexOfFirst { it != null && !it.isZero }.takeIf { it >= 0 } }
    val left = lead.filter { bar == null || it < bar }
    // (A|E): the right block is E's image and never zero, so only the left block's rows say anything (judges, 18. 11.)
    if (inverzia && bar != null) return "v ľavom bloku " + pocetRiadkov(left.size) + "; hlavné stĺpce (stĺpce prvých nenulových prvkov riadkov) sú ${left.map { it + 1 }.joinToString(", ").ifEmpty { "žiadne" }}."
    return pocetRiadkov(lead.size) + "; hlavné stĺpce (stĺpce prvých nenulových prvkov riadkov) sú ${left.map { it + 1 }.joinToString(", ").ifEmpty { "žiadne" }}" +
        (if (bar != null && !inverzia) (if (lead.any { it >= bar }) "; pravá strana je hlavný stĺpec (riadok 0 = c)." else "; pravá strana nie je hlavný stĺpec.") else ".")
}

/**
 * What the engine knows about the verdict, as numbered sentences: the only claims the explanation may make about the
 * student's work (the model rephrases them). Also the explanation's fallback when the model's text fails the check.
 */
fun fakty(k: Kontext, vys: Vysledok, v: Verdikt?): List<String> {
    v ?: return emptyList()
    val f = k.ulohy.getOrNull(v.uloha ?: 0)?.pole ?: Field.R
    val out = ArrayList<String>()
    val kk = v.krok ?: 0
    fun po(i: Int) = if (i <= 0) "prvej matice" else "matice po $i. úprave"
    fun opsFor(row: Int, o: List<RowOp>?) = o?.filter { op -> when (op) { is dev.hamper.tabula.la.Combine -> op.target == row; is dev.hamper.tabula.la.Scale -> op.i == row; is dev.hamper.tabula.la.Swap -> op.i == row || op.j == row; else -> false } }?.takeIf { it.isNotEmpty() }
    // the question about a step before this GEM: the paper can't explain the typed answer, so say both, in any verdict
    val ku0 = k.krokUlohy.getOrNull(v.uloha ?: 0); val ko0 = k.krokOtazky
    val skorsia = ku0 != null && ko0 != null && ko0 < ku0
    if (v.stav == Verdikt.Stav.BEZ_CHYBY || skorsia) k.rozdiel?.let { out += it }
    if (skorsia && v.stav != Verdikt.Stav.BEZ_CHYBY) out += "Otázka v appke je o kroku „${k.kroky.getOrElse(ko0!!) { "" }}“; GEM na fotke patrí až k neskoršiemu kroku „${k.kroky.getOrElse(ku0!!) { "" }}“, takže chyba na papieri nevysvetľuje odpoveď v appke. Povedz najprv, čo nesedí v odpovedi, potom chybu na papieri."
    if (v.preusporiadane) {
        out += "Prvá matica je zadanie, len s riadkami v inom poradí; to je v poriadku."
        // the order of the rows is the order of the coordinates: a column of a unit vector e_t of the assignment looks
        // like another e_s on paper (judges, 18. 11., v157: "doplň tie z pozícií 3 a 4" sent the student to the wrong e_i)
        // (A|E) of an inverse is no completion: there the columns of E pick nothing
        k.ulohy.getOrNull(v.uloha ?: 0)?.takeIf { !v.transponovane && !inverzia(k, v) }?.let { u ->
            fun jednotkovy(col: List<Frac?>) = col.indexOfFirst { it == Frac.ONE }.takeIf { i -> i >= 0 && col.withIndex().all { (r, x) -> r == i || (x != null && x.isZero) } }
            val m0 = v.matice.firstOrNull() ?: return@let
            val ine = (0 until u.start.n).mapNotNull { j ->
                val t = jednotkovy(u.start.rows.map { it[j] }) ?: return@mapNotNull null
                val sp = jednotkovy(m0.map { it.getOrNull(j) }) ?: return@mapNotNull null
                if (sp != t) Triple(j, t, sp) else null
            }
            if (ine.isNotEmpty()) out += "Keďže riadky (súradnice) sú v inom poradí, jednotkové vektory na papieri vyzerajú inak ako v zadaní: " +
                ine.joinToString("; ") { (j, t, sp) -> "${j + 1}. stĺpec je $\\mathbf{e}_{${t + 1}}$ zo zadania (na papieri vyzerá ako $\\mathbf{e}_{${sp + 1}}$)" } +
                ". Ktoré vektory doplniť, určuje poradie stĺpcov v zadaní, nie to, ako stĺpec vyzerá na papieri."
        }
    }
    if (v.transponovane) out += "Študent má maticu zadania transponovanú (riadky a stĺpce vymenené oproti vzorovému riešeniu). Hodnosť je rovnaká, takže hodnosť, LZ/LN aj dimenzia obalu vyjdú rovnako. Ak úloha vyberá z pôvodných vektorov bázu alebo dopĺňa súbor, vektory patria do stĺpcov matice (hlavné stĺpce povedia, ktoré)."
    when (v.stav) {
        Verdikt.Stav.CHYBA -> {
            val riadky = v.riadky.sorted().map { it + 1 }
            when (v.typ) {
                Verdikt.Typ.ZADANIE -> {
                    v.bunky.forEach { b ->
                        out += "Prvá matica sa líši od tej, ktorou má podľa úlohy postup začať: v ${b.miesto.riadok + 1}. riadku, ${b.miesto.stlpec + 1}. stĺpci je ${b.napisane.t()}, má byť ${b.maloByt.t()}." + if (v.matice.size > 1) " Ďalej študent počítal s touto zlou hodnotou." else ""
                        diagnozaOdpisu(b)?.let { out += odhad(it) }
                    }
                    if (v.matice.size > 1) out += "Oprava: v prvej matici prepíš to číslo a všetky ďalšie úpravy sprav znova z opravenej matice."
                }
                Verdikt.Typ.NEPOVOLENA -> {
                    val h0 = runCatching { f.rank(v.matice[kk - 1].map { r -> r.map { it!! } }) }.getOrNull()
                    val h1 = runCatching { f.rank(v.matice[kk].map { r -> r.map { it!! } }) }.getOrNull()
                    out += "V $kk. úprave sú nové riadky ${riadky.joinToString(" a ")} kombináciou starých, ale riadky spolu stratili informáciu" + (if (h0 != null && h1 != null) ": hodnosť klesla z $h0 na $h1." else ".") +
                        " Taká úprava nie je ekvivalentná (napr. násobok nulou, alebo dva riadky upravené naraz jeden z druhého)."
                    v.matice.getOrNull(kk)?.let { m ->
                        val nulove = v.riadky.filter { r -> m.getOrNull(r)?.all { it != null && it.isZero } == true }.map { it + 1 }
                        if (nulove.size == 1) out += "Po $kk. úprave je ${nulove[0]}. riadok nulový, hoci predtým nebol."
                        else if (nulove.size > 1) out += "Po $kk. úprave sú ${nulove.joinToString(". a ")}. riadok nulové, hoci predtým neboli."
                        else if (v.riadky.size >= 2 && runCatching { f.rank(v.riadky.sorted().map { r -> m[r].map { it!! } }) == 1 }.getOrDefault(false))
                            out += "Po $kk. úprave sú riadky ${riadky.joinToString(" a ")} navzájom násobkami, teda jeden z nich stratil svoju informáciu."
                    }
                    v.spravneUpravy?.let { o ->
                        o.filterIsInstance<dev.hamper.tabula.la.Scale>().firstOrNull { it.k.isZero }?.let { z -> out += "${z.i + 1}. riadok sa vynuloval, ako keby sa násobil nulou (${ops(listOf(z))}); riadok sa smie násobiť len nenulovým číslom." }
                        val cs = o.filterIsInstance<dev.hamper.tabula.la.Combine>()
                        // ř_t := a·ř_t + b·ř_s and ř_s := c·ř_s + d·ř_t at once lose information exactly when a·c − b·d = 0
                        cs.firstOrNull { c -> cs.any { d -> d.target == c.src && d.src == c.target && f.sub(f.mul(c.a, d.a), f.mul(c.b, d.b)).isZero } }?.let { c ->
                            out += "V jednom kroku sa mení ${c.target + 1}. aj ${c.src + 1}. riadok a každý sa počíta z toho druhého v pôvodnom tvare; preto po úprave nesú tieto dva riadky spolu menej informácie než predtým. Ekvivalentné je urobiť len jednu z týchto dvoch úprav (druhú až v ďalšom kroku z už nového riadku)."
                        }
                    }
                }
                else -> {
                    if (v.bunky.isEmpty()) out += "V $kk. úprave ${if (riadky.size == 1) "${riadky[0]}. riadok nevyplýva" else "riadky ${riadky.joinToString(" a ")} nevyplývajú"} z ${po(kk - 1)} žiadnou riadkovou úpravou. Úpravy nie sú napísané, preto program nevie, ktoré číslo je zlé."
                    for (b in v.bunky) {
                        val r = b.miesto.riadok
                        val op = opsFor(r, v.spravneUpravy)
                        val ako = if (b.iste) "" else "pravdepodobne "
                        out += if (v.typ == Verdikt.Typ.ODPIS) "V $kk. úprave sa ${r + 1}. riadok mal len odpísať (${ako}sa nemenil, nanajvýš presunul), no v ${b.miesto.stlpec + 1}. stĺpci je ${b.napisane.t()} namiesto ${b.maloByt.t()}."
                        else "V $kk. úprave sa v ${r + 1}. riadku ${ako}robila úprava ${op?.let { ops(it) } ?: "(nenapísaná)"}; v ${b.miesto.stlpec + 1}. stĺpci z nej vychádza ${b.maloByt.t()}" +
                            (b.odvodenie?.let { " ($$it$)" } ?: "") + ", študent napísal ${b.napisane.t()}."
                        val o = v.matice.getOrNull(kk - 1); val n = v.matice.getOrNull(kk)
                        val dovod = when {
                            o == null || n == null -> null
                            v.typ == Verdikt.Typ.ODPIS -> diagnozaOdpisu(b)
                            else -> diagnozaBunky(f, o, n, v.spravneUpravy, b)
                        }
                        dovod?.let { out += odhad(it) }
                        out += "Oprava: v ${r + 1}. riadku prepíš ${b.napisane.t()} na ${b.maloByt.t()} a ďalšie úpravy rob z opravenej matice."
                    }
                }
            }
            v.spravne?.takeIf { v.typ != Verdikt.Typ.ZADANIE }?.let { out += "Správna matica po $kk. úprave" + (v.spravneUpravy?.let { o -> " (úpravy ${ops(o)})" } ?: "") + ": $" + mtex(it, v.ciara) + "$." }
            v.odporucane?.let { m -> out += "Štandardný krok GEM z ${po(kk - 1)}: ${ops(v.odporucaneUpravy)}; dá $" + mtex(m, v.ciara) + "$." +
                v.riadky.sorted().firstOrNull()?.let { r -> " Jeho ${r + 1}. riadok je $" + m.getOrNull(r)?.joinToString(",") { it.tex(true) }?.let { "($it)" } + "$, na papieri je $" + v.matice.getOrNull(kk)?.getOrNull(r)?.joinToString(",") { it?.tex(true) ?: "?" }?.let { "($it)" } + "$ (to je porovnanie so štandardným krokom; študent mohol robiť inú správnu úpravu)." }.orEmpty() }
            if (v.overitCitanie && kk > 0 && v.bunky.isNotEmpty()) out += "Matica po $kk. úprave je posledná na fotke, chybu teda nepotvrdzuje ďalší krok; appka sa študenta opýta, či je číslo prečítané dobre."
            // the student's own last matrix, mistake included: what it says may differ from the answer typed into the app too
            v.matice.lastOrNull()?.takeIf { v.matice.size - 1 > kk && hst(it, if (inverzia(k, v)) v.ciara else null) }?.let { m ->
                out += "Posledná matica na papieri (matica po ${v.matice.size - 1}. úprave) je aj s touto chybou v HST. V nej " + zhrnutie(m, v.ciara, inverzia(k, v)).removeSuffix(".") + " (to platí pre maticu s chybou)."
                spravnaStruktura(f, v, k)?.let { sp ->
                    val moja = strukturaHst(m, v.ciara, inverzia(k, v))
                    val inv = inverzia(k, v)
                    out += if (sp == moja) "Po správnych úpravách by mala posledná matica tiež ${popisStruktury(sp, v.ciara, inv, true)}. Chyba teda nezmenila počet nenulových riadkov ani hlavné stĺpce" +
                        (if (v.ciara != null && !inv) " ani to, či je pravá strana hlavný stĺpec" else "") + ", len čísla v matici: hodnosť, LZ/LN, riešiteľnosť, regulárnosť, dimenzia aj počet riešení vyjdú z matice s chybou rovnako ako po oprave."
                        else "Po správnych úpravách by mala posledná matica ${popisStruktury(sp, v.ciara, inv, true)}; matica na papieri má ${popisStruktury(moja, v.ciara, inv, false)}. Tento rozdiel spôsobila chyba ${if (kk == 0) "v prvej matici (zle odpísané zadanie)" else "v $kk. úprave"}."
                }
                val stlpce = m.mapNotNull { r -> r.indexOfFirst { it != null && !it.isZero }.takeIf { it >= 0 } }
                if (k.zaver.isNotEmpty() && stlpce == k.zaverStlpce) out += "Hlavné stĺpce tejto matice sú rovnaké ako v matici vzorového riešenia, ktoré z nej pokračuje takto: " + k.zaver.joinToString(" ")
            }
        }
        Verdikt.Stav.NEDOKONCENE -> {
            val inverzia = k.ulohy.getOrNull(v.uloha ?: 0)?.ciel == GemUloha.Ciel.JEDNOTKOVA
            if (inverzia) v.matice.lastOrNull()?.let { m ->
                val bar = v.ciara ?: m.size
                val lavy = m.map { r -> r.take(bar) }
                val leads = lavy.map { r -> r.indexOfFirst { it != null && !it.isZero } }
                val hst = leads.filter { it >= 0 }.zipWithNext().all { (a, b) -> a < b } && leads.dropWhile { it >= 0 }.all { it < 0 }
                if (hst) out += "Ľavý blok už je v HST a z ${m.size} riadkov ${pocetRiadkov(leads.count { it >= 0 })}: o regulárnosti sa dá rozhodnúť už teraz (regulárna je práve pri plnej hodnosti)."
            }
            // a row 0 … 0 | c already decides solvability, finished or not
            val bar = v.ciara
            val sporny = if (inverzia || bar == null) null else v.matice.lastOrNull()?.withIndex()?.firstOrNull { (_, r) -> r.take(bar).all { it != null && it.isZero } && r.drop(bar).any { it != null && !it.isZero } }
            out += when {
                inverzia -> "Všetky úpravy sú správne, ale vľavo ešte nie je jednotková matica, takže inverzia ešte nie je hotová."
                sporny != null -> "Všetky úpravy sú správne a posledná matica ešte nie je v HST. Už teraz však má ${sporny.index + 1}. riadok vľavo samé nuly a vpravo nenulové číslo (rovnica $0=${sporny.value.drop(bar!!).first { it != null && !it.isZero }!!.tex(true)}$): sústava nemá riešenie a ďalšie úpravy to nezmenia."
                else -> "Všetky úpravy sú správne, ale posledná matica ešte nie je v HST, takže z nej sa ešte nedá vyvodiť záver."
            }
            v.spravne?.let { out += "Ďalší krok GEM: ${ops(v.spravneUpravy)}, dá $" + mtex(it, v.ciara) + "$." }
        }
        Verdikt.Stav.BEZ_CHYBY -> {
            // the question is about a step before this GEM (the photo is a later part): say so, nothing about that part
            val ku = k.krokUlohy.getOrNull(v.uloha ?: 0); val ko = k.krokOtazky
            val skorsiaOtazka = ku != null && ko != null && ko < ku
            if (skorsiaOtazka) out += "Otázka v appke je o kroku „${k.kroky.getOrElse(ko!!) { "" }}“; GEM na fotke patrí až k neskoršiemu kroku „${k.kroky.getOrElse(ku!!) { "" }}“."
            // a typing slip: the right vector stands whole on the paper, only the app got another number
            val ocak = k.ocakavane
            if (ocak != null && k.zle.isNotEmpty()) {
                val skupiny = k.menovky.indices.groupBy { k.menovky[it].substringBefore('_') }.values.filter { g -> g.size >= 2 && g.any { it in k.zle } }
                for (g in skupiny) {
                    val vec = g.map { ocak[it] }
                    if (vec.count { !it.isZero } < 2) continue
                    val kde = naPapieri(v, vec) ?: continue
                    val meno = k.menovky[g[0]].substringBefore('_').takeIf { it.length == 1 && it[0].isLetter() }?.let { "$\\mathbf{$it}=" } ?: "$"
                    out += "Správne ${meno}(${vec.joinToString(",") { it.tex(true) }})$ je na papieri celé ($kde): na papieri to študent má správne a rozdiel vznikol až pri zadávaní do appky."
                }
            }
            out += (if (v.matice.size == 1) "Na fotke je len prvá matica, bez úprav, a už je v cieľovom tvare." else "Všetky úpravy sú správne a posledná matica je v cieľovom tvare.") +
                (if (k.odpovedStudenta != null) " Chyba je až v tom, čo študent z matice vyvodil alebo zadal do appky."
                else "")
            v.matice.lastOrNull()?.let { m -> out += "V poslednej matici " + zhrnutie(m, v.ciara, inverzia(k, v)) }
            val mojeStlpce = v.matice.lastOrNull()?.mapNotNull { r -> r.indexOfFirst { it != null && !it.isZero }.takeIf { it >= 0 } }
            if (!skorsiaOtazka && k.zaver.isNotEmpty() && mojeStlpce != null && mojeStlpce == k.zaverStlpce)
                out += "Vzorové riešenie z matice s rovnakými hlavnými stĺpcami pokračuje takto: " + k.zaver.joinToString(" ")
            // which part of the exercise the photo is: the method's steps tell it only when the question comes first (the
            // question's step is usually the conclusion drawn from this very GEM), else only the hedge
            if (!skorsiaOtazka) out += "Ak sa matica na fotke nezhoduje s tým, čo pýta otázka v appke, postup môže patriť k inej časti úlohy."
        }
        else -> {}
    }
    return out
}

private fun odhad(s: String) = "Pravdepodobný dôvod chyby (appka ho odhaduje z čísel, nie je to istota): $s."

private fun nenulove(n: Int) = when { n == 1 -> "1 nenulový riadok"; n in 2..4 -> "$n nenulové riadky"; else -> "$n nenulových riadkov" }

/** "3 nenulové riadky a hlavné stĺpce 1, 2, 3; pravá strana by nebola hlavný stĺpec" ([by]: said of the right matrix). */
private fun popisStruktury(s: Struktura, bar: Int?, inverzia: Boolean, by: Boolean): String =
    nenulove(s.riadky) + (if (inverzia) " v ľavom bloku" else "") + " a hlavné stĺpce " + s.hlavne.map { it + 1 }.joinToString(", ").ifEmpty { "žiadne" } +
        (if (bar != null && !inverzia) "; pravá strana " + (if (by) "by " else "") + (if (s.pravaHlavna) (if (by) "bola" else "je") else (if (by) "nebola" else "nie je")) + " hlavný stĺpec" else "")

/** What the right chain's last matrix would show: the echelon form of the last right matrix (the assignment, corrected). */
private fun spravnaStruktura(f: Field, v: Verdikt, k: Kontext): Struktura? {
    val kk = v.krok ?: return null
    val from: List<List<Frac?>> = if (v.typ == Verdikt.Typ.ZADANIE || kk == 0)
        v.matice.firstOrNull()?.mapIndexed { i, row -> row.mapIndexed { j, x -> v.bunky.firstOrNull { it.miesto.riadok == i && it.miesto.stlpec == j }?.maloByt ?: x } } ?: return null
    else v.matice.getOrNull(kk - 1) ?: return null
    if (v.typ == Verdikt.Typ.ZADANIE && v.bunky.isEmpty()) return null
    val m = from.map { r -> r.map { it ?: return null } }
    return runCatching { struktura(f, m, v.ciara, inverzia(k, v)) }.getOrNull()
}

/** Where on the paper the right vector stands whole: "v 1. riadku prvej matice" (a typing slip in the app, not on paper). */
private fun naPapieri(v: Verdikt, vec: List<Frac>): String? {
    for ((i, m) in v.matice.withIndex()) {
        // the first matrix is the assignment: a right answer that is an input vector stands there whatever was typed
        if (i == 0) continue
        val name = "matice po $i. úprave"
        m.indexOfFirst { row -> row.size >= vec.size && row.take(vec.size) == vec }.takeIf { it >= 0 }?.let { return "v ${it + 1}. riadku $name" }
        // a column is the answer only as a solution read off it: right of the bar, the identity left of it (judges, 18. 11.,
        // v72: the second components of two right-hand sides matched x by chance)
        val bar = v.ciara
        val jednotkova = bar != null && bar >= vec.size && m.size >= vec.size && m.withIndex().all { (r, row) -> (0 until bar).all { c -> row.getOrNull(c) == if (r == c && r < vec.size) Frac.ONE else Frac.ZERO } }
        if (jednotkova) (bar!! until (m.firstOrNull()?.size ?: 0)).firstOrNull { j -> m.take(vec.size).map { it.getOrNull(j) } == vec }?.let { return "v ${it + 1}. stĺpci $name" }
    }
    return null
}

/** The input of the explanation: the exercise, the student's answer and the engine's facts, as text. */
fun vstupVysvetlenia(k: Kontext, vys: Vysledok, v: Verdikt? = vys.hlavny): String = buildString {
    appendLine("ÚLOHA: ${k.zadanie}")
    appendLine("OTÁZKA V APPKE: ${k.otazka}")
    k.odpovedStudenta?.let { appendLine("ODPOVEĎ ŠTUDENTA V APPKE: $it") }
    appendLine("SPRÁVNA ODPOVEĎ: ${k.spravnaOdpoved}")
    k.spatnaVazba?.let { appendLine("SPÄTNÁ VÄZBA, KTORÚ APPKA UŽ UKÁZALA: $it") }
    if (k.postup.isNotEmpty()) appendLine("POSTUP KURZU (Ako na to?): " + k.postup.joinToString(" → "))
    appendLine()
    appendLine("NADPIS, KTORÝ ŠTUDENT VIDÍ: ${nadpis(v, vys)}")
    if (v == null) return@buildString
    val f = k.ulohy.getOrNull(v.uloha ?: 0)?.pole
    if (f is Field.Zp) appendLine("Počíta sa v $\\mathbb{Z}_{${f.p}}$.")
    appendLine("ŠTUDENTOV POSTUP (ako ho program prečítal):")
    v.matice.forEachIndexed { i, m ->
        if (i > 0) appendLine("  ∼ ${i}. úprava: ${ops(v.upravy.getOrNull(i - 1))}")
        appendLine("  " + (if (i == 0) "prvá matica" else "matica po $i. úprave") + ": $" + mtex(m, v.ciara) + "$")
    }
    appendLine("FAKTY (jediné, čo smieš o postupe tvrdiť):")
    fakty(k, vys, v).forEachIndexed { i, t -> appendLine("  ${i + 1}. $t") }
}

/**
 * Why a generated explanation can't be shown, or null: a number that isn't in its input, a step after the one the
 * engine stopped at, a formula the typesetter can't draw after cleaning. The app then asks once more, then shows
 * the facts themselves.
 */
fun kontrolaVysvetlenia(text: String, vstup: String, v: Verdikt?): String? {
    if (text.isBlank()) return "prázdne"
    val num = Regex("(?<![A-Za-z\\\\])-?\\d+(?:/\\d+)?")
    fun cisla(t: String) = num.findAll(Regex("\\\\[dt]?frac\\{(-?\\d+)\\}\\{(\\d+)\\}").replace(t.replace('−', '-')) { "${it.groupValues[1]}/${it.groupValues[2]}" }).map { it.value.removePrefix("-") }.toSet()
    val mimo = cisla(text) - cisla(vstup)
    if (mimo.isNotEmpty()) return "čísla mimo vstupu: ${mimo.joinToString()}"
    // a guessed reason for the student's answer ("chyba je v závere, lebo…", "zamenil si…"): the judges' one recurring
    // contradiction; the input never shows why the student typed what they did
    val odhad = "Pravdepodobný dôvod chyby" in vstup
    Regex("(?i)(závere?|odpove(ď|di|ďou))[^.:;]{0,20}?\\b(pretože|lebo)\\b|myslel\\s+si|si\\s+myslel|považoval\\s+si|si\\s+pokladal" + (if (odhad) "" else "|zamenil\\s+si\\s+(?!\\$?\\d)|si\\s+zamenil|pomýlil\\s+si|si\\s+sa\\s+pomýlil")).find(text)?.let { return "dôvod odpovede: ${it.value}" }
    val k = v?.krok
    // steps after the wrong one are computed from wrong numbers: not to be judged. The last matrix may be named
    // ("matica po n. úprave") when the facts describe it (what it says, mistake included).
    val posledna = v?.matice?.size?.minus(1)?.takeIf { "Posledná matica na papieri" in vstup }
    if (k != null && v.stav == Verdikt.Stav.CHYBA) Regex("(po\\s+)?(\\d+)\\.\\s*úprav").findAll(text)
        .firstOrNull { val n = it.groupValues[2].toInt(); n > k && !(n == posledna && it.groupValues[1].isNotEmpty()) }
        ?.let { return "úprava ${it.groupValues[2]} po chybnej $k" }
    return null
}

/** The instructions with the words the course doesn't use (data/Terminology.kt, the one list the content checks use too). */
val PokynVysvetlenia: String by lazy {
    fun citatelne(p: String) = p.replace(Regex("""\(\?<?[!=][^)]*\)"""), "").replace("\\b", "").replace("\\p{L}*", "…")
        .replace("\\s+", " ").replace("\\s*", "").replace("[-–]", "-").replace(Regex("""[()|?\\]"""), " ").replace(Regex(" +"), " ").trim()
    PokynZaklad + "\n- Tieto slová kurz nepoužíva, píš namiesto nich slovo kurzu: " +
        dev.hamper.tabula.data.BannedTerms.filter { "NS" !in it.pattern && "induk" !in it.pattern && "R_" !in it.pattern }.joinToString("; ") { "„${citatelne(it.pattern)}“ → ${it.use}" } + "."
}

/** [predosle]: the explanations already shown; each is followed by the student's "Nerozumiem". */
fun teloVysvetlenia(n: Nastavenie, vstup: String, predosle: List<String> = emptyList()): JSONObject {
    val input = JSONArray().put(JSONObject().put("role", "user").put("content", vstup))
    for (a in predosle) {
        input.put(JSONObject().put("role", "assistant").put("content", a))
        input.put(JSONObject().put("role", "user").put("content", NerozumiemOtazka))
    }
    val telo = JSONObject().put("model", n.modelVysvetlenia).put("instructions", PokynVysvetlenia).put("input", input)
    n.usilieVysvetlenia?.let { telo.put("reasoning", JSONObject().put("effort", it)) }
    return telo
}

/** "Nerozumiem": the same verdict explained again, slower; asked at most a few times per result. */
const val NerozumiemOtazka = "Nerozumiem. Vysvetli to podrobnejšie a jednoduchšie, krok za krokom na tomto mieste postupu (najviac 120 slov). Stále nepočítaj nič nové a neprezraď výsledok úlohy."

/**
 * The model's text as the app can set it: a command the typesetter doesn't know (an arrow, say) is dropped from its
 * formula rather than shown raw (V5 in the harness counts how often the instruction alone isn't enough).
 */
fun vysaditelne(text: String): String = Regex("\\$\\$(.+?)\\$\\$|\\$(.+?)\\$", RegexOption.DOT_MATCHES_ALL).replace(text) { m ->
    val f = m.groupValues[1].ifEmpty { m.groupValues[2] }
    val bad = dev.hamper.tabula.math.unknownCommands(f)
    if (bad.isEmpty()) m.value else {
        var g = f.replace("\\leftarrow", ":=").replace("\\gets", ":=").replace("\\Leftarrow", ":=")
        for (c in dev.hamper.tabula.math.unknownCommands(g).distinct().sortedByDescending { it.length }) g = g.replace(if (c.startsWith("\\")) c else "\\$c", " ")
        m.value.replace(f, g)
    }
}

suspend fun vysvetli(klient: ResponsesKlient, n: Nastavenie, vstup: String, predosle: List<String> = emptyList(), onText: (String) -> Unit): Odpoved =
    klient.odpoved(teloVysvetlenia(n, vstup, predosle), onText)

/** The engine's facts as the student reads them: the explanation's last fallback. */
fun faktyPreStudenta(k: Kontext, vys: Vysledok): String = priamo(fakty(k, vys, vys.hlavny).joinToString(" "))

/** The facts said to the student (they are written about "študent" for the model). */
private fun priamo(t: String) = t.replace("Ďalej študent počítal", "Ďalej si počítal").replace("Študent má", "Máš").replace("študent má", "máš").replace("študent mohol", "mohol si").replace("študent napísal", "napísal si").replace("študent počítal", "počítal si")
    .replace("študenta opýta", "ťa opýta").replace("študent z matice vyvodil alebo zadal", "si z matice vyvodil alebo zadal").replace("študent", "ty")

/** What the student gets: the text, which try it was (0, 1; 2 = the engine's facts shown instead), and the calls' usage. */
data class Vysvetlenie(val text: String, val pokus: Int, val spotreba: Spotreba, val ms: Long, val prvyMs: Long, val dovod: String?)

/**
 * The explanation the app shows: the model's text when it passes [kontrolaVysvetlenia]; otherwise once more, and if
 * that fails too, the engine's facts as plain sentences. [onReset] clears what was streamed before a retry.
 */
suspend fun vysvetliOverene(klient: ResponsesKlient, n: Nastavenie, k: Kontext, vys: Vysledok, v: Verdikt?, predosle: List<String> = emptyList(),
                            onReset: () -> Unit = {}, onText: (String) -> Unit = {}): Vysvetlenie {
    val vstup = vstupVysvetlenia(k, vys, v)
    var spotreba = Spotreba()
    var dovod: String? = null
    var ms = 0L; var prvy = -1L
    for (pokus in 0..1) {
        if (pokus > 0) onReset()
        val o = vysvetli(klient, n, vstup, predosle, onText)
        spotreba += o.usage; ms += o.spoluMs; if (prvy < 0) prvy = o.prvyTextMs
        val t = vysaditelne(o.text)
        dovod = kontrolaVysvetlenia(t, vstup, v)
        if (dovod == null) return Vysvetlenie(t, pokus, spotreba, ms, prvy, null)
    }
    onReset()
    val f = priamo(fakty(k, vys, v).joinToString(" "))
    onText(f)
    return Vysvetlenie(f, 2, spotreba, ms, prvy, dovod)
}

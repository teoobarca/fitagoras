package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.data.dml.numbers.gcd
import kotlin.math.abs
import kotlin.random.Random

/*
 * What the induction proseminar practises and the app did not (docs/COVERAGE.md, "Prosemináre: skutočné medzery"):
 * an inductively defined set of integers, which numbers it holds and both inclusions (Cvičení 4.12; proseminár 25/26
 * č. 4, 40:35–46:35; Dombek, 5. prednáška, 26:09), a step by k with k base cases and why that covers every n
 * (proseminár 25/26 č. 4, 15:59–21:55; Spěvák, 4. prednáška, 73:19), and a flawed induction to judge and correct on
 * paper (Cvičení 4.11; proseminár 25/26 č. 4, 36:50; Dombek, 5. prednáška, 52:35; Legerský, 5. prednáška, 78:04).
 * InductionTest builds the sets by applying the rules and checks the answers against them.
 */

private fun mm(s: String) = m(s)

private fun nums(labels: List<String>, expected: List<Long>, shown: String, diag: (List<Long>) -> String) =
    Answer.Numbers(labels, null, expected.map { Frac.of(it) }, mm(shown), diagnose = { g -> mm(diag(g.map { if (it.isInt) it.num else Long.MIN_VALUE })) })

/* ---- 1. An inductively defined set of integers: which numbers, and both inclusions ------------------------------ */

val MiInductiveSetPostup = postup(
    "Prvé prvky a odhad" to "Použi pravidlá niekoľkokrát a odhadni, aké čísla vznikajú.",
    "§S\\subseteq M§ štrukturálnou indukciou" to "ZK: počiatočné prvky majú vlastnosť. IK: každé pravidlo ju zachová.",
    "§M\\subseteq S§: každý prvok vyrobíš" to "Najprv vyrob najmenší kladný prvok, potom indukciou ďalšie a nakoniec záporné.",
    "Záver" to "Obe inklúzie spolu dávajú §S=M§.",
)

/**
 * kind 0: a, b ∈ S; m, n ∈ S ⇒ m + n, m − n ∈ S (Cvičení 4.12); kind 1: the same with m − n only; kind 2: s ∈ S;
 * x ∈ S ⇒ x + k, x − k ∈ S (Dombek). [a], [b] are the seeds of kinds 0 and 1, [a] and [b] the seed s and the step k of kind 2.
 */
data class MiSetTask(val kind: Int, val a: Long, val b: Long)

/** The subtractions of Euclid's algorithm from a, b down to gcd(a, b): each (larger, smaller, difference). */
fun miSubtractions(a: Long, b: Long): List<Triple<Long, Long, Long>> {
    val out = ArrayList<Triple<Long, Long, Long>>()
    var x = maxOf(a, b); var y = minOf(a, b)
    val g = gcd(a, b)
    // stop as soon as the gcd itself has been made
    while (x != y) { out += Triple(x, y, x - y); val d = x - y; if (d == g) break; if (d >= y) x = d else { x = y; y = d } }
    return out
}

fun miSetTask(r: Random): MiSetTask = when (r.nextInt(3)) {
    2 -> MiSetTask(2, r.pick(-9, 20).toLong(), r.pick(2, 7).toLong())
    else -> {
        val kind = if (r.nextInt(3) == 0) 1 else 0
        var t: MiSetTask? = null
        while (t == null) {
            val g = r.pick(2, 6).toLong()
            val a = g * r.pick(2, 9); val b = g * r.pick(2, 9)
            if (a == b || a % b == 0L || b % a == 0L || gcd(a, b) != g || miSubtractions(a, b).size > 5) continue
            t = MiSetTask(kind, minOf(a, b), maxOf(a, b))
        }
        t
    }
}

private fun rulesTex(t: MiSetTask) = when (t.kind) {
    0 -> "§${t.a},${t.b}\\in S§; jsou-li §m,n\\in S§, pak také §m+n\\in S§, §m-n\\in S§"
    1 -> "§${t.a},${t.b}\\in S§; jsou-li §m,n\\in S§, pak také §m-n\\in S§"
    else -> "§${t.a}\\in S§; je-li §x\\in S§, pak také §x+${t.b}\\in S§ a §x-${t.b}\\in S§"
}

fun miSet(t: MiSetTask): Exercise {
    val (intro, sub, sup, end) = MiInductiveSetPostup
    val prompt = mm("Určete, jaká čísla obsahuje množina celých čísel §S§, která je definovaná induktivně: ${rulesTex(t)}; a žádná jiná čísla než ta získaná konečným použitím předchozích pravidel množina §S§ neobsahuje. Svou odpověď dokažte.")
    if (t.kind == 2) {
        val s = t.a; val k = t.b; val rr = s.mod(k)
        fun el(j: String) = Terms.sum("$s", Terms.times(k, j))
        val first = listOf(s - 2 * k, s - k, s, s + k, s + 2 * k)
        val sample = listOf(
            "Odhad: §S=M=\\{${el("j")}\\mid j\\in\\mathbb{Z}\\}§, teda čísla so zvyškom §$rr§ po delení §$k§.",
            "§S\\subseteq M§ štrukturálnou indukciou. ZK: §$s\\in M§ (pre §j=0§). IK: ak §x=${el("j")}§, tak §x\\pm $k=${el("(j\\pm 1)")}\\in M§.",
            "§M\\subseteq S§: §V(j)§: §${el("j")}\\in S§ pre §j\\in\\mathbb{N}_0§ slabou indukciou: ZK §$s\\in S§, IK §${el("(j+1)")}=(${el("j")})+$k\\in S§ podľa pravidla. Pre záporné §j§ rovnako pravidlom §x-$k§.",
            "Spolu §S=M§.",
        )
        return Exercise(
            "dml-mi-induktivna", "Induktívne definovaná množina", "Šablóna Cvičení 4.12; Dombek, 5. prednáška",
            prompt,
            parts = listOf(
                { _ -> Part(mm("§S§ sú práve čísla so zvyškom §r§ po delení §$k§. §r=§"), nums(listOf("r"), listOf(rr), "§r=$rr§") { g ->
                    val v = g[0]
                    when {
                        v == s && s != rr -> "§$s§ je počiatočný prvok, nie zvyšok. Zvyšok po delení §$k§ je z §\\{0,\\dots,${k - 1}\\}§: §$s=" + s.floorDiv(k) + "\\cdot $k+$rr§."
                        v < 0 || v >= k -> "Zvyšok po delení §$k§ je z §\\{0,\\dots,${k - 1}\\}§. Pravidlá menia číslo o §$k§, zvyšok čísla §$s§ ostane: §$rr§."
                        else -> "Pravidlá pripočítavajú a odčítavajú §$k§, a to zvyšok nezmení. Zvyšok čísla §$s§ je §$rr§."
                    }
                }, aim = 0, hint = mm("Vypíš prvky §$s§, §$s+$k§, §$s-$k§, … Čo majú spoločné po delení §$k§?")) },
                { _ -> Part(mm("Na papier: dôkaz oboch inklúzií, §S\\subseteq M§ štrukturálnou indukciou a §M\\subseteq S§ indukciou."), Answer.Paper(sample.map(::mm),
                    listOf("Odhad §M§ zapísaný ako množina", "§S\\subseteq M§: ZK pre počiatočný prvok, IK pre obe pravidlá", "§M\\subseteq S§: indukcia pre nezáporné §j§ a pravidlo §x-$k§ pre záporné", "Záver §S=M§").map(::mm),
                    mm("Dôkaz potrebuje obe inklúzie. Štrukturálna indukcia ukáže len, že §S§ má iba také čísla; že ich má všetky, treba indukciou podľa §j§.")),
                    aim = 1, hint = mm("Najprv vlastnosť zachovaná pravidlami, potom indukcia §V(j)§: §${el("j")}\\in S§.")) },
            ),
            postup = MiInductiveSetPostup,
            solution = { _ ->
                MiInductiveSetPostup.solve {
                    intro(mm("Pravidlá od §$s§ krokujú po §$k§ oboma smermi:"), first.joinToString(",\\ ") + ",\\ \\dots")
                    intro(mm("Odhad: §S§ je množina §M§ čísel so zvyškom §$rr§ po delení §$k§."), "M=\\{${el("j")}\\mid j\\in\\mathbb{Z}\\}")
                    sub(mm("ZK: §$s\\in M§ (pre §j=0§). IK: ak §x=${el("j")}\\in M§, tak §x+$k§ aj §x-$k§ sú tvaru §${el("(j\\pm 1)")}§, teda v §M§. Podľa princípu štrukturálnej indukcie §S\\subseteq M§."))
                    sup(mm("Slabou indukciou podľa §j\\in\\mathbb{N}_0§ dokážem §V(j)§: §${el("j")}\\in S§. ZK §j=0§: §$s\\in S§. IK: §${el("(j+1)")}=(${el("j")})+$k§ je v §S§ podľa pravidla §x+$k§."))
                    sup(mm("Pre záporné §j§ rovnako, pravidlom §x-$k§ od §$s§ nadol. Teda §M\\subseteq S§."))
                    end(mm("Obe inklúzie platia, preto §S=M§."), "S=\\{${el("j")}\\mid j\\in\\mathbb{Z}\\}")
                }
            },
            hint = mm("Odhadni z prvých prvkov. §S\\subseteq M§: pravidlá zachovajú vlastnosť. §M\\subseteq S§: každý prvok vyrob pravidlami."),
        )
    }
    val a = t.a; val b = t.b; val g = gcd(a, b)
    val chain = miSubtractions(a, b)
    val plusRule = if (t.kind == 0) "pravidla §m+n§" else "pravidla §m-n§ s §m=$g k§, §n=-$g§"
    val sample = buildList {
        add("Odhad: §S=M=\\{${g}k\\mid k\\in\\mathbb{Z}\\}§.")
        add("§S\\subseteq M§ štrukturálnou indukciou. ZK: §$a§ a §$b§ sú násobky §$g§. IK: ak §m=${g}k§, §n=$g\\ell§, tak " + (if (t.kind == 0) "§m+n=$g(k+\\ell)§ aj " else "") + "§m-n=$g(k-\\ell)§.")
        add("§$g\\in S§: " + chain.joinToString(", ") { "§${it.first}-${it.second}=${it.third}§" } + ", všetko pravidlom §m-n§.")
        add("§0=$g-$g\\in S§; indukciou §${g}k\\in S§ pre §k\\in\\mathbb{N}§: §$g(k+1)§ vznikne z §${g}k§ a §$g§ " + (if (t.kind == 0) "pravidlom §m+n§" else "ako §${g}k-(-$g)§, kde §-$g=0-$g\\in S§") + "; záporné ako §0-${g}k§.")
        add("Spolu §S=M§.")
    }
    return Exercise(
        "dml-mi-induktivna", "Induktívne definovaná množina", "Šablóna Cvičení 4.12; proseminár 25/26 č. 4",
        prompt,
        parts = listOf(
            { _ -> Part(mm("§S§ je množina všetkých celočíselných násobkov čísla §d\\geq 1§. §d=§"), nums(listOf("d"), listOf(g), "§d=\\gcd($a,$b)=$g§") { gv ->
                val v = gv[0]
                when {
                    v == a -> "§$a§ je v §S§, ale aj §$b-$a=${b - a}§; menší kladný prvok je §$g§."
                    v > g && v % g == 0L -> "Všetky prvky sú násobky §$v§? Nie: §$g§ vznikne odčítaním, " + chain.joinToString(", ") { "§${it.first}-${it.second}=${it.third}§" } + "."
                    v in 1 until g -> "§$v§ nevyrobíš: §$a§ aj §$b§ sú násobky §$g§ a súčet ani rozdiel to nezmení."
                    else -> "Odčítaním ako v Euklidovom algoritme dostaneš §\\gcd($a,$b)=$g§ a z neho všetky jeho násobky."
                }
            }, aim = 0, hint = mm("Odčítaj menšie od väčšieho, ako v Euklidovom algoritme. Čo je najmenší kladný prvok?")) },
            { _ -> Part(mm("Na papier: dôkaz oboch inklúzií, §S\\subseteq M§ štrukturálnou indukciou a §M\\subseteq S§ indukciou."), Answer.Paper(sample.map(::mm),
                listOf("Odhad §S=\\{${g}k\\mid k\\in\\mathbb{Z}\\}§", "§S\\subseteq M§: ZK pre §$a,$b§, IK pre " + (if (t.kind == 0) "obe pravidlá" else "pravidlo §m-n§"), "§$g\\in S§ vyrobené pravidlami", "§M\\subseteq S§: indukcia pre kladné násobky, potom §0§ a záporné", "Záver §S=M§").map(::mm),
                mm("Dôkaz potrebuje obe inklúzie. Štrukturálna indukcia ukáže, že v §S§ sú len násobky §$g§; že tam sú všetky, treba vyrobiť §$g§ a potom indukciu.")),
                aim = 1, hint = mm("§S\\subseteq M§: pravidlá zachovajú deliteľnosť §$g§. §M\\subseteq S§: najprv §$g\\in S§, potom indukcia.")) },
        ),
        postup = MiInductiveSetPostup,
        solution = { _ ->
            MiInductiveSetPostup.solve {
                intro(mm("Pravidlo §m-n§ použijem ako v Euklidovom algoritme, menšie od väčšieho:"), chain.joinToString(",\\ ") { "${it.first}-${it.second}=${it.third}" })
                intro(mm("Odhad: §S§ je množina §M§ všetkých násobkov §$g§."), "M=\\{${g}k\\mid k\\in\\mathbb{Z}\\}")
                sub(mm("ZK: §$a=$g\\cdot ${a / g}§ a §$b=$g\\cdot ${b / g}§ sú v §M§. IK: ak §m=${g}k§ a §n=$g\\ell§, tak " + (if (t.kind == 0) "§m+n=$g(k+\\ell)§ aj " else "") + "§m-n=$g(k-\\ell)§ " + (if (t.kind == 0) "sú" else "je") + " v §M§. Podľa princípu štrukturálnej indukcie §S\\subseteq M§."))
                sup(mm("Odčítania vyššie sú použitia pravidla §m-n§ na prvky §S§, preto §$g\\in S§ a §0=$g-$g\\in S§."))
                sup(mm("Slabou indukciou §${g}k\\in S§ pre §k\\in\\mathbb{N}§: ZK §$g\\in S§; IK: §$g(k+1)§ dostanem z §${g}k§ a §$g§ " + (if (t.kind == 0) "pravidlom §m+n§." else "ako §${g}k-(-$g)§, lebo §-$g=0-$g\\in S§.") + " Záporné násobky sú §0-${g}k\\in S§."))
                end(mm("Obe inklúzie platia, preto §S=M§, všetky celočíselné násobky §$g§."), "S=\\{${g}k\\mid k\\in\\mathbb{Z}\\}")
            }
        },
        hint = mm("Odčítaj ako v Euklidovom algoritme. §S\\subseteq M§: pravidlá zachovajú deliteľnosť. §M\\subseteq S§: vyrob najmenší kladný prvok."),
    )
}

/* ---- 2. A step by k: which base cases it needs (proseminár 25/26 č. 4, 15:59; Spěvák, 4. prednáška, 73:19) ------- */

val MiStepKPostup = postup(
    "Kam krok dosiahne" to "Krok §V(n)\\Rightarrow V(n+k)§ ide od každého základného kroku po §k§ hore, nikdy dole.",
    "Pokryté zvyšky" to "Každé §n\\geq n_0§ je §n_0+i+jk§ so §0\\leq i<k§; treba ZK pre každé §i§.",
    "Záver" to "Všetky §i§ pokryté: slabý princíp pre každé zvlášť. Inak najmenšie nepokryté §n§.",
)

/** Base cases [bases] proved, the step V(n) ⇒ V(n + k) for every n ≥ n0; V(n) is claimed for every n ≥ n0. */
data class MiStepTask(val n0: Int, val k: Int, val bases: List<Int>, val asked: Int)

/** The numbers ≥ n0 the proof reaches: a base case and steps up by k. */
fun miStepReached(t: MiStepTask, upTo: Int): Set<Int> = (t.n0..upTo).filter { n -> t.bases.any { b -> n >= b && (n - b) % t.k == 0 } }.toSet()

/** The smallest n ≥ n0 the proof does not reach, or null when it reaches all. */
fun miStepMissing(t: MiStepTask): Int? = (t.n0..t.n0 + 3 * t.k).firstOrNull { it !in miStepReached(t, t.n0 + 3 * t.k) }

fun miStepTask(r: Random): MiStepTask {
    val n0 = r.pick(0, 2); val k = r.pick(2, 3)
    val full = (n0 until n0 + k).toList()
    val bases = when (r.nextInt(4)) {
        0, 1 -> full
        2 -> full - full.random(r)
        else -> full.map { it + 1 }
    }
    return MiStepTask(n0, k, bases, n0 + r.pick(10, 60))
}

fun miStep(t: MiStepTask): Exercise {
    val (n0, k, bases, asked) = t
    val miss = miStepMissing(t)
    val basesTex = bases.joinToString(",\\ ") { "V($it)" }
    val dom = if (n0 == 0) "n\\in\\mathbb{N}_0" else if (n0 == 1) "n\\in\\mathbb{N}" else "n\\geq $n0"
    val (reach, cover, end) = MiStepKPostup
    // n0 + i + kj and n − n0 as written by hand: no "0+i", no "n-0"
    val nij = Terms.sum("$n0", "i", "${k}j"); val n0i = Terms.sum("$n0", "i"); val nm = Terms.sum("n", "${-n0}")
    val from = n0 + (asked - n0).mod(k)
    val chains = bases.sorted().joinToString(";\\ ") { b -> "V($b)\\Rightarrow V(${b + k})\\Rightarrow V(${b + 2 * k})\\Rightarrow\\cdots" }
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(mm("Plynie z toho §V(n)§ pre všetky §$dom§?"), Answer.Choice(listOf("Áno", "Nie"), if (miss == null) 0 else 1,
        mm(if (miss == null) "Áno: ZK pokrývajú všetky zvyšky po delení §$k§ a krok ide od každého ďalej." else "Nie: §V($miss)§ z nich neplynie, krok ide len hore a ZK pre §$miss§ chýba."),
        mapOf((if (miss == null) 1 else 0) to mm(if (miss == null) "Plynie: každé §n§ je ZK plus niekoľko krokov po §$k§, lebo ZK sú pre všetky zvyšky po delení §$k§."
            else "Neplynie: §V($miss)§ nie je ZK a krok po §$k§ k nemu nevedie zo žiadneho ZK."))),
        aim = 1, hint = mm("Z každého ZK §V(b)§ krok dá §V(b+$k)§, §V(b+${2 * k})§, … Ktoré §n§ takto nedosiahneš?")) }
    val second: (List<Any>) -> Part = if (miss == null) { _ -> Part(mm("Z ktorého základného kroku plynie §V($asked)§? Zadaj jeho §n§."), nums(listOf("n"), listOf(from.toLong()), "§$asked=$from+${(asked - from) / k}\\cdot $k§") { gv ->
        val v = gv[0]
        when {
            v in bases.map { it.toLong() } -> "Z §V($v)§ dosiahneš §$v+$k j§, ale §$asked-$v=${asked - v}§ nie je násobok §$k§."
            else -> "Hľadaj ZK §b§ s §$asked-b§ deliteľným §$k§: §$asked=$from+${(asked - from) / k}\\cdot $k§."
        }
    }, aim = 1, hint = mm("§$asked-b§ musí byť násobok §$k§. Ktoré §b§ z ZK to splní?")) }
    else { _ -> Part(mm("Najmenšie §n\\geq $n0§, pre ktoré §V(n)§ z toho neplynie:"), nums(listOf("n"), listOf(miss.toLong()), "§n=$miss§") { gv ->
        val v = gv[0]
        when {
            v in bases.map { it.toLong() } -> "§V($v)§ je overený základný krok, ten plynie."
            v < n0 -> "Tvrdenie je len pre §n\\geq $n0§."
            else -> "§V($miss)§ nie je ZK a krok ide len hore po §$k§, preto k nemu nevedie. Je to najmenšie také §n§."
        }
    }, aim = 2, hint = mm("Vypíš, kam sa dostaneš z každého ZK krokmi po §$k§, a nájdi prvé chýbajúce §n\\geq $n0§.")) }
    parts += second
    val sample = if (miss == null) listOf(
        "Každé §n\\geq $n0§ zapíšem ako §n=$nij§ so §0\\leq i<$k§, §j\\in\\mathbb{N}_0§ (delenie §$nm§ číslom §$k§ so zvyškom).",
        "Pre pevné §i§ dokážem §W(j)§: §V($nij)§ slabou indukciou podľa §j§: ZK §W(0)=V($n0i)§ je overený, IK §W(j)\\Rightarrow W(j+1)§ je daný krok pre §n=$nij\\geq $n0§.",
        "Teda §V(n)§ platí pre všetky §$dom§. Ide to aj silným princípom so ZK §$basesTex§: pre §n\\geq ${n0 + k - 1}§ je §n+1-$k\\geq $n0§, takže §V(n+1)§ plynie krokom z už dokázaného §V(n+1-$k)§.")
    else listOf(
        "Z overených ZK krok dá len §$chains§.",
        "§V($miss)§ medzi nimi nie je: nie je to ZK a krok vedie len hore, z menších §n§ po §$k§ k nemu nevedie.",
        "Dôkaz je neúplný, chýba ZK §V($miss)§.")
    parts += { _ -> Part(mm("Na papier: zdôvodnenie, prečo základné kroky a krok o §$k§ pokryjú všetky §n§, alebo ktoré nepokryjú."), Answer.Paper(sample.map(::mm),
        (if (miss == null) listOf("Zápis §n=$nij§, §0\\leq i<$k§", "Slabá indukcia podľa §j§ pre každé §i§ zvlášť, alebo silný princíp", "Záver pre všetky §$dom§")
            else listOf("Kam dosiahne krok z každého ZK", "Konkrétne §n=$miss§, ku ktorému nevedie", "Záver: chýba ZK §V($miss)§")).map(::mm),
        mm(if (miss == null) "Zdôvodnenie musí povedať, že každé §n§ je ZK plus niekoľko krokov po §$k§; pre každý zvyšok jedna slabá indukcia."
            else "Zdôvodnenie musí ukázať §n=$miss§, ku ktorému krok zo žiadneho ZK nevedie, lebo ide len hore.")),
        aim = 2, hint = mm("Krok ide po §$k§ len hore. Pre každý zvyšok §n§ po delení §$k§ treba jeden ZK.")) }
    return Exercise(
        "dml-mi-krok", "Indukcia s krokom o k", "Šablóna podľa proseminára 25/26 č. 4 a Spěvákovej 4. prednášky",
        mm("Pro §n\\in\\mathbb{Z}§: tvrzení §V(n)§ dokazujeme pro všechna §$dom§. Ověřili jsme §$basesTex§ a pro všechna §$dom§ implikaci §V(n)\\Rightarrow V(n+$k)§. Plyne z toho §V(n)§ pro všechna §$dom§? Zdůvodněte."),
        parts = parts,
        postup = MiStepKPostup,
        solution = { _ ->
            MiStepKPostup.solve {
                reach(mm("Krok posúva o §$k§ hore, takže z každého ZK dostanem jednu postupnosť:"), chains)
                if (miss == null) {
                    cover(mm("ZK §$basesTex§ pokrývajú každý zvyšok §$nm§ po delení §$k§. Každé §n\\geq $n0§ je §$nij§ so §0\\leq i<$k§."), "$asked=$from+${(asked - from) / k}\\cdot $k")
                    end(mm("Pre každé §i§ dá slabá indukcia podľa §j§ všetky §V($nij)§, preto §V(n)§ platí pre všetky §$dom§."))
                } else {
                    cover(mm("§V($miss)§ v žiadnej z postupností nie je: nie je to ZK a krok ide len hore."))
                    end(mm("Dôkaz je neúplný: §V($miss)§ z overených krokov neplynie, chýba ZK §V($miss)§."))
                }
            }
        },
        hint = mm("Krok ide po §$k§ len hore. Každé §n§ musí byť ZK plus niekoľko krokov."),
    )
}

/* ---- 3. A flawed induction: judge it and correct it on paper (Cvičení 4.11) ------------------------------------- */

val MiFlawPostup = postup(
    "Základný krok" to "Over ZK sám: dosaď najmenšie §n§ do oboch strán.",
    "Indukčný krok" to "Prečítaj krok: pre ktoré §n§ ho treba a či používa len IP.",
    "Záver" to "Chýba alebo neplatí časť? Dôkaz neplatí; nepravdivé tvrdenie vyvráť protipríkladom.",
)

/** A sum 1..n of f(i) = p·i + q and its closed form (a2 n² + a1 n)/den. */
private class SumFamily(val left: String, val p: Long, val q: Long, val a2: Long, val a1: Long, val den: Long)

private val SumFamilies = listOf(
    SumFamily("""1+2+\dots+n""", 1, 0, 1, 1, 2),
    SumFamily("""2+4+\dots+2n""", 2, 0, 1, 1, 1),
    SumFamily("""1+3+\dots+(2n-1)""", 2, -1, 1, 0, 1),
    SumFamily("""1+4+\dots+(3n-2)""", 3, -2, 3, -1, 2),
)

/** kind 0: a correct sum proof; 1: a sum off by c, only the step (Cvičení 4.11 b, c); 2: a^n = 1 (Cvičení 4.11 a); 3: every n irrational (Dombek); 4: the step only from n = 2 on. */
data class MiFlawTask(val kind: Int, val fam: Int = 0, val c: Long = 0)

fun miFlawTask(r: Random): MiFlawTask = when (r.nextInt(6)) {
    0 -> MiFlawTask(0, r.nextInt(SumFamilies.size))
    1, 2 -> { var c: Long; do { c = r.pick(-15, 15).toLong() } while (c == 0L); MiFlawTask(1, r.nextInt(SumFamilies.size), c) }
    3 -> MiFlawTask(2)
    4 -> MiFlawTask(3)
    else -> MiFlawTask(4, r.nextInt(SumFamilies.size))
}

/** The right side (a2 n² + a1 n + den·c)/den in TeX. */
private fun rightTex(f: SumFamily, c: Long): String {
    val num = Terms.sum(Terms.times(f.a2, "n^2"), Terms.times(f.a1, "n"), if (f.den * c != 0L) "${f.den * c}" else "0")
    return if (f.den == 1L) num else "\\frac{$num}{${f.den}}"
}

/** The right side at n, exactly (the closed form is an integer at every n). */
fun miFlawRight(fam: Int, c: Long, n: Long): Long { val f = SumFamilies[fam]; return (f.a2 * n * n + f.a1 * n) / f.den + c }
/** The left side at n by adding up. */
fun miFlawLeft(fam: Int, n: Long): Long { val f = SumFamilies[fam]; return (1..n).sumOf { f.p * it + f.q } }

private val FlawOptions = listOf("Áno, dôkaz je správny", "Nie, základný krok chýba alebo neplatí", "Nie, indukčný krok chýba alebo neplatí pre niektoré §n§")

fun miFlaw(t: MiFlawTask): Exercise {
    val (zkAim, ikAim, end) = MiFlawPostup
    val f = SumFamilies[t.fam]
    val nextTerm = Terms.sum(Terms.times(f.p, "n+1"), "${f.q}")
    val claim = "${f.left}=" + rightTex(f, t.c)
    val prompt: String; val correct: Int; val why: String; val sample: List<String>; val look: List<String>; val miss: String
    when (t.kind) {
        0, 1, 4 -> {
            val l1 = miFlawLeft(t.fam, 1); val r1 = miFlawRight(t.fam, t.c, 1)
            prompt = when (t.kind) {
                0 -> "Uvažujme vlastnost §V(n)§: §$claim§. Ověřili jsme §V(1)§ a implikaci §V(n)\\Rightarrow V(n+1)§ pro všechna §n\\in\\mathbb{N}§. Je pravda, že jsme tím ukázali, že §V(n)§ platí pro všechna §n\\in\\mathbb{N}§?"
                1 -> "Uvažujme vlastnost §V(n)§: §$claim§. Potom platí implikace §V(n)\\Rightarrow V(n+1)§ (ověřte!). Je pravda, že jsme tím ukázali, že §V(n)§ platí pro všechna §n\\in\\mathbb{N}§?"
                else -> "Uvažujme vlastnost §V(n)§: §$claim§. Ověřili jsme §V(1)§ a implikaci §V(n)\\Rightarrow V(n+1)§ pro všechna §n\\geq 2§. Je pravda, že jsme tím ukázali, že §V(n)§ platí pro všechna §n\\in\\mathbb{N}§?"
            }
            val ik = "IK: §${f.left}+($nextTerm)\\stackrel{IP}{=}" + rightTex(f, t.c) + "+($nextTerm)§, a to sa po úprave rovná pravej strane pre §n+1§."
            when (t.kind) {
                0 -> { correct = 0; why = "Áno: ZK §$l1=$r1§ platí a krok je pre všetky §n\\in\\mathbb{N}§."
                    sample = listOf("ZK: §n=1§: ľavá strana §$l1§, pravá §$r1§, rovnajú sa.", ik, "Dôkaz je správny, podľa slabého princípu MI §V(n)§ platí pre všetky §n\\in\\mathbb{N}§.")
                    look = listOf("ZK prepočítaný: obe strany pre §n=1§", "IK prepočítaný: pribudne člen §$nextTerm§", "Záver: dôkaz je správny")
                    miss = "Tu je dôkaz v poriadku: ZK §$l1=$r1§ platí a krok ide od §n=1§." }
                1 -> { correct = 1; why = "Nie: ZK neplatí, pre §n=1§ je ľavá strana §$l1§ a pravá §$r1§."
                    sample = listOf("ZK chýba a neplatí: pre §n=1§ je ľavá strana §$l1§, pravá §$r1§.", ik + " Bez ZK však nič nedokazuje.",
                        "Rozdiel pravej a ľavej strany je pre každé §n§ rovnaký, §${t.c}§, preto §V(n)§ neplatí pre žiadne §n§; protipríklad §n=1§.")
                    look = listOf("ZK prepočítaný a vyvrátený: §$l1\\neq $r1§", "IK síce platí, ale bez ZK nestačí", "Protipríklad §n=1§")
                    miss = "Chyba je v ZK: pre §n=1§ vyjde §$l1\\neq $r1§. Krok platí, ale bez ZK nestačí." }
                else -> { correct = 2; why = "Nie: krok je len pre §n\\geq 2§, takže chýba prechod §V(1)\\Rightarrow V(2)§."
                    sample = listOf("ZK §V(1)§ platí: §$l1=$r1§.", "IK je overený len pre §n\\geq 2§, chýba §V(1)\\Rightarrow V(2)§; §V(2)§ preto z ničoho neplynie, a bez neho ani ďalšie.",
                        "Dôkaz je neúplný, hoci tvrdenie platí: stačí overiť aj §V(2)§ alebo krok pre §n\\geq 1§.")
                    look = listOf("ZK v poriadku", "Chýba krok §V(1)\\Rightarrow V(2)§", "Oprava: ZK aj pre §n=2§, alebo krok od §n=1§")
                    miss = "Chyba je v kroku: začína až pri §n=2§, takže §V(2)§ z §V(1)§ neplynie." }
            }
        }
        2 -> {
            prompt = "Je následující úvaha správná? Pro všechna nenulová celá §a§ a všechna §n\\in\\mathbb{N}_0§ platí §a^n=1§, neboť pro §n=0§ je §a^0=1§; a nechť pro §n\\geq 0§ je §a^n=1§, pak §a^{n+1}=a^n\\cdot\\frac{a^n}{a^{n-1}}=\\frac{1\\cdot 1}{1}=1§."
            correct = 2; why = "Nie: krok používa §a^{n-1}=1§, teda §V(n-1)§, a na to sa IP nevzťahuje."
            sample = listOf("ZK §a^0=1§ platí.", "IK používa §a^{n-1}=1§, teda §V(n-1)§, ale IP je len §V(n)§. Pre §n=0§ by to bolo §V(-1)§, a §-1\\notin\\mathbb{N}_0§: krok z §V(0)§ na §V(1)§ zlyhá.",
                "Tvrdenie je nepravdivé: pre §a=2§ a §n=1§ je §a^n=2\\neq 1§.")
            look = listOf("ZK v poriadku", "Chyba: krok potrebuje okrem §V(n)§ aj §V(n-1)§, ale IP je len §V(n)§; pre §n=0§ zlyhá", "Protipríklad §a=2§, §n=1§")
            miss = "Chyba je v kroku: §a^{n-1}§ potrebuje §V(n-1)§, IP ho nedáva a pre §n=0§ nemá zmysel."
        }
        else -> {
            prompt = "Je následující úvaha správná? Každé §n\\in\\mathbb{N}§ je iracionální: je-li §n§ iracionální, je i §n+1§ iracionální, neboť kdyby §n+1§ bylo racionální, bylo by racionální i §n=(n+1)-1§."
            correct = 1; why = "Nie: ZK chýba a neplatí, §1=\\tfrac{1}{1}§ je racionálne."
            sample = listOf("IK obmenou platí: z racionálneho §n+1§ je racionálne aj §n=(n+1)-1§.", "ZK chýba a neplatí: §1=\\tfrac{1}{1}§ je racionálne.",
                "Tvrdenie je nepravdivé, protipríklad §n=1§. Krok bez ZK nič nedokazuje.")
            look = listOf("Krok obmenou je v poriadku", "Chyba: chýba ZK, a §V(1)§ neplatí", "Protipríklad §n=1§")
            miss = "Chyba je v ZK: chýba, a pre §n=1§ neplatí, lebo §1=\\tfrac{1}{1}§ je racionálne."
        }
    }
    val wrong = (0 until 3).filter { it != correct }.associateWith { i ->
        when (i) {
            0 -> "Dôkaz nie je správny. $miss"
            1 -> if (t.kind == 0) "ZK je overený: pre §n=1§ sa obe strany rovnajú." else if (t.kind == 2) "ZK §a^0=1§ platí. Problém je v kroku." else "ZK §V(1)§ platí. $miss"
            else -> if (t.kind == 1 || t.kind == 3) "Krok platí (overené). $miss" else "Krok je overený pre všetky §n\\in\\mathbb{N}§, teda od §n=1§."
        }
    }
    return Exercise(
        "dml-mi-chyba", "Nájdi chybu v indukcii", "Šablóna Cvičení 4.11; proseminár 25/26 č. 4; Dombek a Legerský, 5. prednáška",
        mm(prompt),
        parts = listOf(
            { _ -> Part(mm("Je dôkaz správny? Ak nie, kde je chyba?"), Answer.Choice(FlawOptions.map(::mm), correct, mm(why), wrong.mapValues { mm(it.value) }), aim = 0,
                hint = mm("Over ZK sám dosadením. V kroku sleduj, pre ktoré §n§ je a či používa len IP.")) },
            { _ -> Part(mm("Na papier: posúď dôkaz krok po kroku; chybu ukáž a oprav, nepravdivé tvrdenie vyvráť protipríkladom."), Answer.Paper(sample.map(::mm), look.map(::mm), mm(miss)),
                aim = 2, hint = mm("ZK prepočítaj, krok prečítaj: pre ktoré §n§ a z akého predpokladu.")) },
        ),
        postup = MiFlawPostup,
        solution = { _ ->
            MiFlawPostup.solve {
                when (t.kind) {
                    0, 1, 4 -> {
                        val l1 = miFlawLeft(t.fam, 1); val r1 = miFlawRight(t.fam, t.c, 1)
                        zkAim(mm(if (l1 == r1) "Pre §n=1§ sa obe strany rovnajú, ZK platí." else "Pre §n=1§ sa strany nerovnajú, ZK neplatí" + (if (t.kind == 1) " a v úvahe ani nie je." else ".")), "$l1" + (if (l1 == r1) "=" else "\\neq ") + "$r1")
                        ikAim(mm("V kroku pribudne člen §$nextTerm§ a pravá strana sa zmení presne o neho, preto krok platí" + (if (t.kind == 4) ", ale len pre §n\\geq 2§." else ".")))
                        end(mm(when (t.kind) { 0 -> "Dôkaz je správny: ZK a krok pre všetky §n\\in\\mathbb{N}§."; 1 -> "Dôkaz neplatí, chýba ZK; tvrdenie neplatí pre žiadne §n§, rozdiel strán je vždy §${t.c}§."; else -> "Dôkaz je neúplný, chýba §V(1)\\Rightarrow V(2)§; oprava: overiť aj §V(2)§." }))
                    }
                    2 -> {
                        zkAim(mm("ZK §a^0=1§ platí."))
                        ikAim(mm("Krok používa §a^{n-1}=1§, teda §V(n-1)§, ale IP je len §V(n)§. Pre §n=0§ by to bolo §V(-1)§, preto krok z §V(0)§ na §V(1)§ zlyhá."))
                        end(mm("Úvaha nie je správna a tvrdenie je nepravdivé: pre §a=2§ a §n=1§ je §a^n=2\\neq 1§."))
                    }
                    else -> {
                        zkAim(mm("ZK chýba a neplatí: §1=\\tfrac{1}{1}§ je racionálne."))
                        ikAim(mm("Krok obmenou platí: z racionálneho §n+1§ je racionálne aj §n=(n+1)-1§."))
                        end(mm("Úvaha nie je správna: bez ZK krok nič nedokazuje, a tvrdenie neplatí už pre §n=1§."))
                    }
                }
            }
        },
        hint = mm("Over ZK dosadením a v kroku sleduj, pre ktoré §n§ je a čo predpokladá."),
    )
}

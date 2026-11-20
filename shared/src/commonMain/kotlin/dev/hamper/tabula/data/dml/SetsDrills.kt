package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import kotlin.random.Random

/*
 * What the proseminar of chapter 5 practises and the app did not (docs/COVERAGE.md, "Prosemináre: skutočné medzery"):
 * an inclusion equivalent to an identity, A ⊆ B ⇔ A ∩ B = A, both implications (Cvičení 5.6; proseminár 25/26 č. 5,
 * 36:59–42:04), and the domain and the range of a map given in words (Cvičení 5.8; proseminár 25/26 č. 5, 42:04; Spěvák,
 * 5. prednáška, 53:36). SetsTest checks the claims and the counterexamples on every subset of a small universe and the
 * ranges by computing the maps.
 */

private fun mm(s: String) = m(s)

/* ---- 1. A ⊆ B and an equivalent identity: both implications, or a counterexample ---------------------------------- */

val InclusionEquivPostup = postup(
    "Platí, alebo nie?" to "Skús malé množiny, napríklad §A\\subsetneq B§. Ak niečo zlyhá, máš protipríklad.",
    "§(\\Rightarrow)§" to "Predpokladaj §A\\subseteq B§ a dokáž pravú stranu; rovnosť ako dve inklúzie.",
    "§(\\Leftarrow)§" to "Predpokladaj pravú stranu a pre ľubovoľné §x\\in A§ odvoď §x\\in B§.",
    "Záver" to "Platia obe implikácie; pri protipríklade over obe strany ekvivalencie.",
)

/**
 * A ⊆ B ⇔ [right] for all A, B ⊆ U. A true one carries its proof as (subgoal, sentence) pairs, a false one fails on
 * A ⊊ B ([inclusionCounter]); SetsTest decides each on every pair of subsets of a small universe.
 */
class InclusionClaim(val id: String, val right: String, val src: String,
                     val proof: List<Pair<Int, String>> = emptyList(), val look: List<String> = emptyList()) {
    val holds get() = proof.isNotEmpty()
}

private fun sTex(s: Set<Int>) = if (s.isEmpty()) "\\emptyset" else "\\{" + s.sorted().joinToString(",") + "\\}"

val InclusionClaims: List<InclusionClaim> = listOf(
    InclusionClaim("prienik", """A\cap B=A""", "Cvičení 5.6 b); proseminár 25/26 č. 5",
        proof = listOf(
            1 to """Nech §A\subseteq B§. Vždy §A\cap B\subseteq A§, lebo z §x\in A\wedge x\in B§ plynie §x\in A§.""",
            1 to """Naopak pre §x\in A§ je podľa predpokladu aj §x\in B§, teda §x\in A\cap B§; preto §A\subseteq A\cap B§ a spolu §A\cap B=A§.""",
            2 to """Nech §A\cap B=A§. Pre §x\in A§ je §x\in A\cap B§, teda aj §x\in B§; preto §A\subseteq B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: rovnosť ako dve inklúzie, §A\cap B\subseteq A§ vždy""", """§(\Leftarrow)§: §A=A\cap B\subseteq B§""")),
    InclusionClaim("zjednotenie", """A\cup B=B""", "Cvičení 5.6 c)",
        proof = listOf(
            1 to """Nech §A\subseteq B§. Vždy §B\subseteq A\cup B§, lebo z §x\in B§ plynie §x\in A\vee x\in B§.""",
            1 to """Naopak z §x\in A\cup B§ je §x\in A§ alebo §x\in B§; v prvom prípade podľa predpokladu tiež §x\in B§. Preto §A\cup B\subseteq B§ a spolu §A\cup B=B§.""",
            2 to """Nech §A\cup B=B§. Pre §x\in A§ je §x\in A\cup B=B§; preto §A\subseteq B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: §B\subseteq A\cup B§ vždy, opačne z predpokladu""", """§(\Leftarrow)§: §A\subseteq A\cup B=B§""")),
    InclusionClaim("rozdiel", """A\setminus B=\emptyset""", "Cvičení 5.6 (obmena); Definice 5.3",
        proof = listOf(
            1 to """Nech §A\subseteq B§. Prvok §x\in A\setminus B§ by spĺňal §x\in A§ aj §x\notin B§, a z §x\in A§ plynie §x\in B§, spor. Teda §A\setminus B=\emptyset§.""",
            2 to """Nech §A\setminus B=\emptyset§ a §x\in A§. Keby §x\notin B§, bolo by §x\in A\setminus B§, spor; teda §x\in B§ a §A\subseteq B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: prvok rozdielu by dal spor s §A\subseteq B§""", """§(\Leftarrow)§: §x\in A§, §x\notin B§ by ležalo v prázdnom rozdiele""")),
    InclusionClaim("doplnky", """\overline{B}\subseteq\overline{A}""", "Cvičení 5.6 (obmena); Definice 5.3",
        proof = listOf(
            1 to """Nech §A\subseteq B§ a §x\in\overline{B}§, teda §x\notin B§. Keby §x\in A§, bolo by §x\in B§; preto §x\notin A§, čiže §x\in\overline{A}§.""",
            2 to """Nech §\overline{B}\subseteq\overline{A}§ a §x\in A§. Keby §x\notin B§, bolo by §x\in\overline{B}\subseteq\overline{A}§, teda §x\notin A§, spor; preto §x\in B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: obmena implikácie §x\in A\Rightarrow x\in B§""", """§(\Leftarrow)§: spor s §x\in A§""")),
    InclusionClaim("prienik-doplnok", """A\cap\overline{B}=\emptyset""", "Cvičení 5.6 (obmena); Definice 5.3",
        proof = listOf(
            1 to """Nech §A\subseteq B§. Prvok §x\in A\cap\overline{B}§ by spĺňal §x\in A§ aj §x\notin B§, a z §x\in A§ plynie §x\in B§, spor. Teda §A\cap\overline{B}=\emptyset§.""",
            2 to """Nech §A\cap\overline{B}=\emptyset§ a §x\in A§. Keby §x\notin B§, bolo by §x\in A\cap\overline{B}§, spor; teda §x\in B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: prvok prieniku by dal spor s §A\subseteq B§""", """§(\Leftarrow)§: §x\in A§, §x\notin B§ by ležalo v prázdnom prieniku""")),
    InclusionClaim("doplnok-zjednotenie", """\overline{A}\cup B=\mathcal{U}""", "Cvičení 5.6 (obmena); Definice 5.3",
        proof = listOf(
            1 to """Nech §A\subseteq B§ a §x\in\mathcal{U}§. Ak §x\in A§, tak §x\in B§; inak §x\in\overline{A}§. V oboch prípadoch §x\in\overline{A}\cup B§, a opačná inklúzia platí vždy.""",
            2 to """Nech §\overline{A}\cup B=\mathcal{U}§ a §x\in A§. Potom §x\notin\overline{A}§, a keďže §x\in\overline{A}\cup B§, je §x\in B§."""),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: rozbor prípadov §x\in A§, §x\notin A§""", """§(\Leftarrow)§: §x\in A§ vylúči §\overline{A}§, ostane §B§""")),
    InclusionClaim("opacna", """B\subseteq A""", "Cvičení 5.6 a)"),
    InclusionClaim("prienik-b", """A\cap B=B""", "Cvičení 5.6 b) s prehodenými množinami"),
    InclusionClaim("zjednotenie-a", """A\cup B=A""", "Cvičení 5.6 c) s prehodenými množinami"),
    InclusionClaim("doplnky-zle", """\overline{A}\subseteq\overline{B}""", "Cvičení 5.6, obmena naopak"),
    InclusionClaim("rozdiel-zle", """B\setminus A=\emptyset""", "Cvičení 5.6, rozdiel naopak"),
)

/** The value of each side of a claim on concrete sets, for a counterexample: "A∩B={1}", "B={1,2}". */
private fun sides(c: InclusionClaim, u: Set<Int>, a: Set<Int>, b: Set<Int>): String = when (c.id) {
    "opacna" -> "B=${sTex(b)}\\not\\subseteq ${sTex(a)}=A"
    "prienik-b" -> "A\\cap B=${sTex(a.intersect(b))}\\neq ${sTex(b)}=B"
    "zjednotenie-a" -> "A\\cup B=${sTex(a.union(b))}\\neq ${sTex(a)}=A"
    "doplnky-zle" -> "\\overline{A}=${sTex(u - a)}\\not\\subseteq ${sTex(u - b)}=\\overline{B}"
    else -> "B\\setminus A=${sTex(b - a)}\\neq\\emptyset"
}

fun inclusionExercise(r: Random): Exercise {
    val pool = if (r.nextInt(10) < 4) InclusionClaims.filter { !it.holds } else InclusionClaims.filter { it.holds }
    return inclusionClaim(pool.random(r), r)
}

/** A counterexample to a false claim: U, A ⊊ B ⊊ U with A nonempty; then A ⊆ B holds and the right side fails. */
fun inclusionCounter(r: Random): Triple<Set<Int>, Set<Int>, Set<Int>> {
    val u = (1..r.pick(3, 4)).toSet()
    val a = u.shuffled(r).take(r.pick(1, u.size - 2)).toSet()
    val b = a + (u - a).shuffled(r).take(r.pick(1, u.size - a.size - 1))
    return Triple(u, a, b)
}

fun inclusionClaim(c: InclusionClaim, r: Random): Exercise {
    val (u, a, b) = inclusionCounter(r)
    val (decide, there, back, end) = InclusionEquivPostup
    val ce = if (c.holds) null else "\\mathcal{U}=${sTex(u)},\\ A=${sTex(a)},\\ B=${sTex(b)}"
    val sample = if (ce == null) c.proof.map { (k, s) -> (if (k == 1) "§(\\Rightarrow)§: " else "§(\\Leftarrow)§: ") + s } + "Platia obe implikácie, teda ekvivalencia platí vždy."
        else listOf("Neplatí. Protipríklad: §$ce§.", "§A\\subseteq B§ platí, ale §${sides(c, u, a, b)}§; implikácia §(\\Rightarrow)§ zlyhá.")
    val look = if (ce == null) c.look else listOf("Konkrétne množiny v univerze", "Overené, že §A\\subseteq B§ platí", "Overené, že pravá strana neplatí")
    val yes = "Áno, platí vždy"; val no = "Nie, existuje protipríklad"
    val choice = if (ce == null) Answer.Choice(listOf(yes, no), 0, mm("Platí: obe implikácie sa dajú dokázať pre ľubovoľné §A,B§."),
        mapOf(1 to mm("Protipríklad neexistuje: z §A\\subseteq B§ plynie §${c.right}§ a naopak, pre ľubovoľné §A,B§.")))
    else Answer.Choice(listOf(yes, no), 1, mm("Neplatí: pre §$ce§ je §A\\subseteq B§, ale §${sides(c, u, a, b)}§."),
        mapOf(0 to mm("Neplatí vždy: pre §$ce§ je §A\\subseteq B§, ale §${sides(c, u, a, b)}§.")))
    return Exercise(
        "dml-mz-inkluzia", "Inklúzia a rovnosť", "Šablóna Cvičení 5.6; ${c.src}",
        mm("""Nechť §A,B§ jsou libovolné podmnožiny společného univerza §\mathcal{U}§. Rozhodněte, zda vždy platí: §A\subseteq B§ právě tehdy, když §${c.right}§. Platnost dokažte odvozením, pro důkaz neplatnosti stačí uvést protipříklad."""),
        parts = listOf(
            { _ -> Part(mm("Platí ekvivalencia pre všetky §A,B\\subseteq\\mathcal{U}§?"), choice, aim = 0,
                hint = mm("""Skús §A\subsetneq B§, napríklad §A=\{1\}§, §B=\{1,2\}§. Ak všetko sedí, dokazuj oba smery.""")) },
            { _ -> Part(mm("Na papier: dôkaz oboch implikácií, alebo protipríklad s overením."), Answer.Paper(sample.map(::mm), look.map(::mm),
                mm(if (ce == null) "Dôkaz musí mať oba smery: z §A\\subseteq B§ odvodiť §${c.right}§ a naopak. Rovnosť množín sú dve inklúzie."
                    else "Protipríklad musí mať konkrétne množiny a overenie: §A\\subseteq B§ platí, ale §${sides(c, u, a, b)}§.")),
                aim = if (ce == null) 1 else 3, hint = mm(if (ce == null) "Najprv §(\\Rightarrow)§, potom §(\\Leftarrow)§; pri rovnosti obe inklúzie." else "Zvoľ §A\\subsetneq B§ a obe strany spočítaj.")) },
        ),
        postup = InclusionEquivPostup,
        solution = { _ ->
            InclusionEquivPostup.solve {
                if (ce == null) {
                    decide(mm("Ekvivalencia platí, preto dokážem obe implikácie zvlášť."))
                    for ((k, s) in c.proof) (if (k == 1) there else back)(mm(s))
                    end(mm("Platia obe implikácie, teda §A\\subseteq B§ práve vtedy, keď §${c.right}§."))
                } else {
                    decide(mm("Ekvivalencia neplatí, stačí uviesť množiny, pre ktoré jedna strana platí a druhá nie."), ce)
                    end(mm("§A\\subseteq B§ platí, ale pravá strana nie, preto ekvivalencia neplatí pre všetky §A,B§."), Formula(sides(c, u, a, b)))
                }
            }
        },
        hint = mm("Ekvivalencia sú dve implikácie. Pri podozrení skús §A\\subsetneq B§."),
    )
}

/* ---- 2. The domain and the range of a map given in words (Cvičení 5.8; Spěvák, 5. prednáška) --------------------- */

/** One option of a choice: the set (TeX) and why it is wrong (null for the right one). */
internal class Opt(val tex: String, val wrong: String?)

/**
 * A map in words (Czech, as the textbook gives it) with its domain and range and the wrong sets students give, each with
 * why. [values]: the map computed on a sample of its domain, [claimed]: the right range as a test, both compared by
 * SetsTest on [window].
 */
internal class WordMap(val text: String, val src: String, val domain: List<Opt>, val range: List<Opt>, val reason: Pair<String, String>,
              val values: (() -> Set<Long>)? = null, val claimed: ((Long) -> Boolean)? = null, val window: LongRange = 0L..0L)

private fun ok(tex: String) = Opt(tex, null)
private fun no(tex: String, why: String) = Opt(tex, why)

private val N = """\mathbb{N}"""
private val N0 = """\mathbb{N}_0"""
private val Z = """\mathbb{Z}"""
private val R = """\mathbb{R}"""
private val BIN = """\{0,1\}^*"""

internal fun wordMap(r: Random): WordMap = when (r.nextInt(11)) {
    0 -> {
        val b = listOf(10, 10, 2, 3, 5, 8, 16).random(r)
        val where = if (b == 10) "v desítkovém zápisu" else "v zápisu o základu §$b§"
        WordMap("přiřazuje každému přirozenému číslu jeho poslední číslici $where", "Cvičení 5.8 a); proseminár 25/26 č. 5",
            listOf(ok(N), no(N0, "§0§ nie je prirodzené číslo (Definice 4.1), vstupy sú §1,2,3,\\dots§."), no("""\{0,1,\dots,${b - 1}\}""", "To je obor hodnôt, nie definičný obor.")),
            listOf(ok("""\{0,1,\dots,${b - 1}\}"""), no("""\{1,2,\dots,${b - 1}\}""", "Aj §0§ je posledná číslica, napríklad čísla §$b§."), no("""\{0,1,\dots,$b\}""", "Číslica §$b§ v zápise o základe §$b§ nie je, najväčšia je §${b - 1}§."), no(N, "Obor hodnôt sú len hodnoty, ktoré naozaj vyjdú, nie celé §\\mathbb{N}§.")),
            "Poslednú číslicu má každé prirodzené číslo, teda §D_f=$N§." to "Vyjde každá číslica §0§ až §${b - 1}§: číslica §c\\neq 0§ ako posledná číslica čísla §c§, nula z čísla §$b§. Iné číslice nie sú.",
            { (1..500).map { (it % b).toLong() }.toSet() }, { v -> v in 0L until b }, -5L..20L)
    }
    1 -> {
        val c = r.pick(1, 4)
        WordMap(if (c == 1) "přiřazuje každému přirozenému číslu následující číslo" else "přiřazuje každému přirozenému číslu §n§ číslo §n+$c§", "Cvičení 5.8 b)",
            listOf(ok(N), no(N0, "§0§ nie je prirodzené číslo (Definice 4.1)."), no("""\{${c + 1},${c + 2},\dots\}""", "To je obor hodnôt, nie definičný obor.")),
            listOf(ok("""\{${c + 1},${c + 2},\dots\}"""), no(N, "Najmenšia hodnota je §1+$c=${c + 1}§, menšie čísla nevyjdú."), no("""\{$c,${c + 1},\dots\}""", "§$c§ by bolo §f(0)§, ale §0\\notin\\mathbb{N}§."), no(Z, "Hodnoty sú kladné čísla aspoň §${c + 1}§.")),
            "Predpis má zmysel pre každé prirodzené číslo, teda §D_f=$N§." to "Vyjde každé §k\\geq ${c + 1}§, ako §f(k-$c)§; menšie nie, lebo §n\\geq 1§.",
            { (1..300).map { (it + c).toLong() }.toSet() }, { v -> v >= c + 1 }, -5L..150L)
    }
    2 -> WordMap("přiřazuje každému binárnímu řetězci jeho délku", "Cvičení 5.8 c)",
        listOf(ok(BIN), no(N0, "To je obor hodnôt. Vstupy sú reťazce."), no("""\{0,1\}""", "To sú dva symboly, vstup je celý reťazec, aj prázdny.")),
        listOf(ok(N0), no(N, "Prázdny reťazec §\\varepsilon§ má dĺžku §0§."), no("""\{0,1\}""", "Dĺžka môže byť ľubovoľná, nielen §0§ alebo §1§."), no(Z, "Dĺžka nie je záporná.")),
        "Dĺžku má každý binárny reťazec, aj prázdny, teda §D_f=$BIN§." to "Vyjde každé §n\\in$N0§: reťazec §n§ núl má dĺžku §n§, prázdny §0§.",
        { bins(10).map { it.length.toLong() }.toSet() }, { v -> v >= 0 }, -5L..10L)
    3 -> WordMap("přiřazuje každému binárnímu řetězci počet skupin (faktorů) 01 v tomto řetězci", "Cvičení 5.8 d)",
        listOf(ok(BIN), no(N0, "To je obor hodnôt. Vstupy sú reťazce."), no("""\{01\}^*""", "Vstupom je každý binárny reťazec, nielen poskladaný z §01§.")),
        listOf(ok(N0), no(N, "Reťazec bez §01§, napríklad §1§, dá §0§."), no("""\{0,1\}""", "§0101§ má dve skupiny §01§; vyjde aj viac."), no(Z, "Počet nie je záporný.")),
        "Skupiny §01§ sa dajú spočítať v každom reťazci, teda §D_f=$BIN§." to "Vyjde každé §n\\in$N0§: §n§-krát zopakované §01§ ich má §n§, prázdny reťazec §0§.",
        { bins(10).map { s -> s.windowed(2).count { it == "01" }.toLong() }.toSet() }, { v -> v >= 0 }, -5L..5L)
    4 -> WordMap("přiřazuje každému binárnímu řetězci počet jedniček minus počet nul v tomto řetězci", "Cvičení 5.8 e)",
        listOf(ok(BIN), no(Z, "To je obor hodnôt. Vstupy sú reťazce."), no("""\{0,1\}""", "Vstup je celý reťazec, nie jeden symbol.")),
        listOf(ok(Z), no(N0, "Reťazec §0§ dá §0-1=-1§, záporné hodnoty vyjdú."), no(N, "Vyjde §0§ aj záporné čísla, napríklad z §0§ a §00§."), no("""\{-1,0,1\}""", "Reťazec §111§ dá §3§; vyjde každé celé číslo.")),
        "Počet jednotiek aj núl má každý reťazec, teda §D_f=$BIN§." to "Vyjde každé §k\\in$Z§: §k§ jednotiek pre §k\\geq 0§, §-k§ núl pre §k<0§.",
        { bins(10).map { s -> (s.count { it == '1' } - s.count { it == '0' }).toLong() }.toSet() }, { true }, -10L..10L)
    5 -> WordMap("přiřazuje každému celému číslu §k§ nejmenší možné číslo, které je čtvercem (tj. číslem typu §\\ell^2§ pro §\\ell\\in\\mathbb{Z}§) a zároveň není menší než §k§", "Cvičení 5.8 f)",
        listOf(ok(Z), no(N0, "Vstupy sú všetky celé čísla, aj záporné."), no("""\{\ell^2\mid\ell\in$N0\}""", "To je obor hodnôt, nie definičný obor.")),
        listOf(ok("""\{\ell^2\mid\ell\in$N0\}"""), no(N0, "Vychádzajú len štvorce: §2§ ani §3§ nevyjdú."), no("""\{\ell^2\mid\ell\in$N\}""", "Aj §0=0^2§ vyjde, napríklad pre §k=0§ alebo záporné §k§."), no(Z, "Štvorec nie je záporný.")),
        "Ku každému celému §k§ existuje najmenší štvorec, ktorý nie je menší než §k§, teda §D_f=$Z§." to "Vyjde každý štvorec §\\ell^2§, lebo §f(\\ell^2)=\\ell^2§; a vyjde vždy štvorec, iné čísla nie.",
        { (-30..400).map { k -> var l = 0; while (l * l < k) l++; (l * l).toLong() }.toSet() }, { v -> v >= 0 && (0..20).any { it * it == v.toInt() } }, -10L..400L)
    6 -> WordMap("přiřazuje dvojici reálných čísel jejich maximum", "Cvičení 5.8 g)",
        listOf(ok("""$R\times$R"""), no(R, "Vstupom je dvojica čísel, nie jedno číslo."), no("""\{(x,y)\in$R^2\mid x\neq y\}""", "Maximum má aj dvojica rovnakých čísel: §\\max(2,2)=2§.")),
        listOf(ok(R), no("""$R\times$R""", "Výsledok je jedno číslo, nie dvojica."), no("""\{x\in$R\mid x\geq 0\}""", "§\\max(-3,-5)=-3§; vyjdú aj záporné čísla."), no(N0, "Vyjde každé reálne číslo, napríklad §\\max(\\tfrac12,0)=\\tfrac12§.")),
        "Maximum má každá dvojica reálnych čísel, teda §D_f=$R\\times$R§." to "Vyjde každé §x\\in$R§, napríklad ako §\\max(x,x)=x§.", null)
    7 -> WordMap("přiřazuje dvojici reálných čísel §(x,y)§ jejich součet §x+y§", "Spěvák, 5. prednáška (sčítanie ako zobrazenie)",
        listOf(ok("""$R\times$R"""), no(R, "Vstupom je dvojica čísel."), no("""\{(x,y)\in$R^2\mid y\neq 0\}""", "Sčítať sa dá vždy; s nulou je problém až pri delení.")),
        listOf(ok(R), no("""$R\times$R""", "Súčet je jedno číslo."), no("""\{x\in$R\mid x\geq 0\}""", "§(-1)+(-3)=-4§; vyjdú aj záporné."), no(Z, "§\\tfrac12+0=\\tfrac12§ nie je celé.")),
        "Sčítať sa dá každá dvojica reálnych čísel, teda §D_f=$R\\times$R§." to "Vyjde každé §x\\in$R§, lebo §f(x,0)=x§.", null)
    8 -> WordMap("přiřazuje dvojici reálných čísel §(x,y)§ jejich podíl §\\tfrac{x}{y}§, pokud existuje", "Spěvák, 5. prednáška (aký je definičný obor delenia)",
        listOf(ok("""$R\times($R\setminus\{0\})"""), no("""$R\times$R""", "Nulou deliť nemožno: §\\tfrac{1}{0}§ nie je definované."), no("""($R\setminus\{0\})\times$R""", "Nula smie byť v čitateli: §\\tfrac{0}{5}=0§. Nulový nesmie byť menovateľ.")),
        listOf(ok(R), no("""$R\setminus\{0\}""", "Aj §0§ vyjde, ako §\\tfrac{0}{1}§."), no("""$R\times$R""", "Podiel je jedno číslo."), no("""\{x\in$R\mid x\geq 0\}""", "§\\tfrac{-1}{2}§ je záporné.")),
        "Podiel existuje práve pri nenulovom menovateli, teda §D_f=$R\\times($R\\setminus\\{0\\})§." to "Vyjde každé §x\\in$R§, ako §\\tfrac{x}{1}§.", null)
    9 -> {
        val mod = r.pick(2, 12)
        WordMap("přiřazuje každému celému číslu §k§ zbytek §k\\bmod $mod§", "Definice 6.2; Cvičení 5.8 a) (zvyšok namiesto číslice)",
            listOf(ok(Z), no(N, "Zvyšok majú aj nula a záporné čísla, §-1\\bmod $mod=${mod - 1}§."), no("""\{0,1,\dots,${mod - 1}\}""", "To je obor hodnôt.")),
            listOf(ok("""\{0,1,\dots,${mod - 1}\}"""), no("""\{1,2,\dots,${mod - 1}\}""", "Aj §0§ vyjde, napríklad §$mod\\bmod $mod=0§."), no("""\{0,1,\dots,$mod\}""", "Zvyšok je menší než §$mod§."), no("""\{-${mod - 1},\dots,${mod - 1}\}""", "Zvyšok nie je záporný ani pre záporné §k§ (Definice 6.2).")),
            "Delenie so zvyškom existuje pre každé celé číslo, preto §D_f=$Z§ (Věta 6.2)." to "Vyjde každý zvyšok §0§ až §${mod - 1}§, napríklad ako zvyšok seba samého.",
            { (-100..100).map { ((it % mod + mod) % mod).toLong() }.toSet() }, { v -> v in 0L until mod }, -20L..20L)
    }
    else -> WordMap("přiřazuje každému celému číslu jeho absolutní hodnotu", "Spěvák, 5. prednáška",
        listOf(ok(Z), no(N0, "Absolútnu hodnotu má aj záporné číslo."), no(N, "Aj §0§ a záporné čísla majú absolútnu hodnotu.")),
        listOf(ok(N0), no(N, "§|0|=0§ vyjde."), no(Z, "Absolútna hodnota nie je záporná."), no("""\{1,2,\dots\}\cup\{-1,-2,\dots\}""", "Hodnoty sú nezáporné a vyjde aj §0§.")),
        "Absolútnu hodnotu má každé celé číslo, teda §D_f=$Z§." to "Vyjde každé §n\\in$N0§, ako §|n|=n§; záporné nie.",
        { (-50..50).map { kotlin.math.abs(it).toLong() }.toSet() }, { v -> v >= 0 }, -50L..50L)
}

/** Every binary string up to length [n]. */
fun bins(n: Int): List<String> = (0..n).flatMap { len -> (0 until (1 shl len)).map { v -> (0 until len).joinToString("") { b -> if (v shr (len - 1 - b) and 1 == 1) "1" else "0" } } }

private fun choiceOf(opts: List<Opt>, r: Random): Answer.Choice {
    val os = opts.shuffled(r)
    val right = os.indexOfFirst { it.wrong == null }
    return Answer.Choice(os.map { "§${it.tex}§" }.map(::mm), right, "",
        os.withIndex().filter { it.value.wrong != null }.associate { (i, o) -> i to mm(o.wrong!!) })
}

fun domainRangeExercise(r: Random): Exercise {
    val w = wordMap(r)
    val d = choiceOf(w.domain, r).let { it.copy(why = mm(w.reason.first)) }
    val h = choiceOf(w.range, r).let { it.copy(why = mm(w.reason.second)) }
    val dTex = w.domain.first().tex; val hTex = w.range.first().tex
    val (domain, range) = DomainRangePostup
    return Exercise(
        "dml-mz-obory", "Definičný obor a obor hodnôt", "Šablóna Cvičení 5.8; ${w.src}",
        mm("Pro zobrazení §f§, které ${w.text}, určete jeho definiční obor §D_f§ a obor hodnot §H_f§."),
        parts = listOf(
            { _ -> Part(mm("§D_f=§"), d, aim = 0, hint = mm("Pre ktoré vstupy má predpis zmysel? Zobrazenia v BI-DML sú totálne na svojom definičnom obore.")) },
            { _ -> Part(mm("§H_f=§"), h, aim = 1, hint = mm("Ktoré hodnoty naozaj vyjdú? Ku každej nájdi vstup a vylúč tie, ktoré nevyjdú.")) },
        ),
        postup = DomainRangePostup,
        solution = { _ ->
            DomainRangePostup.solve {
                domain(mm(w.reason.first), "D_f=$dTex")
                range(mm(w.reason.second), "H_f=$hTex")
            }
        },
        hint = mm("§D_f§: vstupy, pre ktoré má predpis zmysel. §H_f§: hodnoty, ktoré naozaj vyjdú, nie celý cieľ."),
    )
}

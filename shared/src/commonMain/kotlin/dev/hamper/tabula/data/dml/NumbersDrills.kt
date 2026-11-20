package dev.hamper.tabula.data.dml

import dev.hamper.tabula.la.Frac
import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dml.numbers.*
import kotlin.math.abs
import kotlin.random.Random

/*
 * What the proseminars of chapter 6 practise and the app did not (docs/COVERAGE.md, "Prosemináre: skutočné medzery",
 * rows for the 2nd test): properties of divisibility proved from the definition or refuted (Cvičení 6.1, 6.2; proseminár
 * 25/26 č. 6 00:07–06:45, 21/22 k 6. cvičeniu 01:49–16:54), divisibility for every n without induction (Cvičení 6.3, 6.4,
 * 6.19; 21/22 16:54–43:09, 25/26 06:45–12:50), gcd and lcm from the factorisations (Pozorování 6.1; 21/22 43:09, 25/26
 * 12:51), a remainder of an expression with and without reducing on the way (Příklad 6.10, Cvičení 6.13, 6.15; 21/22 k 7.
 * cvičeniu 04:14, 24:46), and word problems on a Diophantine equation (Cvičení 6.9 to 6.11; 21/22 1:23:06). Every number
 * is drawn and everything asked is computed here; NumbersTest re-derives the answers by brute force.
 */

private fun tx(s: String) = s.replace('§', '$')
private fun Random.r(lo: Int, hi: Int) = nextInt(lo, hi + 1)
private fun Random.rl(lo: Long, hi: Long) = nextLong(lo, hi + 1)
private fun p(n: Long) = if (n < 0) "($n)" else "$n"
/** A count with the Slovak noun agreeing: "1 minúta", "2 minúty", "5 minút" (the number as math). */
private fun sk(n: Long, one: String, few: String, many: String) = "§$n§ " + when { n == 1L -> one; n in 2..4 -> few; else -> many }

private fun nums(labels: List<String>, expected: List<Long>, shown: String, diag: (List<Long>) -> String) =
    Answer.Numbers(labels, null, expected.map { Frac.of(it) }, tx(shown), diagnose = { g -> tx(diag(g.map { if (it.isInt) it.num else Long.MIN_VALUE })) })

/* ---- 1. A property of divisibility: proof from the definition, or a counterexample -------------------------------- */

/** Proving a claim about divisibility from Definice 6.1, or refuting it (Cvičení 6.1, 6.2; Tvrzení 6.1; both proseminars). */
val TcDivProofPostup = postup(
    "Platí, alebo nie?" to "Skús malé čísla, aj nulu a záporné. Keď niečo zlyhá, máš protipríklad.",
    "Prepíš definíciou" to "Každé §x\\mid y§ prepíš na §y=kx§ s celým §k§, pri každom predpoklade iné písmeno.",
    "Výraz ako násobok" to "Z rovností vyjadri to, čo má byť deliteľné, ako celý násobok deliteľa.",
    "Záver" to "Povedz, prečo je ten násobok celý; pri protipríklade over predpoklad aj záver.",
)

/** A counterexample: its numbers (TeX and [nums] by name), why the hypothesis holds, why the conclusion fails (TeX). */
class TcCounter(val values: String, val hyp: String, val fails: String, val nums: Map<String, Long>)

/**
 * One claim about divisibility. [vars]: its variables and their domain (TeX); [why]: one sentence why it holds, for the
 * choice. True claims carry their proof, a step per (subgoal of [TcDivProofPostup], sentence, formulas), and what a
 * grader looks for; false ones draw a counterexample ([counter]).
 */
class TcDivClaim(
    val id: String, val vars: String, val claim: String, val src: String, val why: String = "",
    val proof: List<Triple<Int, String, List<String>>> = emptyList(), val look: List<String> = emptyList(),
    val counter: ((Random) -> TcCounter)? = null,
) {
    val holds get() = counter == null
}

private fun st(aim: Int, say: String, vararg tex: String) = Triple(aim, say, tex.toList())

val TcDivClaims: List<TcDivClaim> = listOf(
    TcDivClaim("refl", """a\in\mathbb{Z}""", """a\mid a""", "Cvičení 6.1 a); proseminár 21/22 k 6. cvičeniu",
        why = """§a\cdot 1=a§ a §1§ je celé, aj pri §a=0§.""",
        proof = listOf(
            st(1, """Podľa definície hľadám celé §k§ s §a\cdot k=a§."""),
            st(2, """Vyhovuje §k=1§ pre každé §a§, aj pre §a=0§.""", """a\cdot 1=a"""),
            st(3, """§k=1§ je celé, preto §a\mid a§ pre každé §a\in\mathbb{Z}§.""")),
        look = listOf("""Definícia: celé §k§ s §a\cdot k=a§""", """§k=1§, funguje aj pre §a=0§""")),
    TcDivClaim("jedna-nula", """z\in\mathbb{Z}""", """1\mid z\ \wedge\ z\mid 0""", "Spěvák, 6. prednáška; proseminár 21/22 k 6. cvičeniu",
        why = """§1\cdot z=z§ a §z\cdot 0=0§, oba činitele sú celé.""",
        proof = listOf(
            st(1, """Treba celé §k§, pre ktoré jednotka krát §k§ dá §z§, a celé §\ell§ so §z\cdot\ell=0§."""),
            st(2, """Vyhovuje §k=z§ a §\ell=0§.""", """1\cdot z=z,\qquad z\cdot 0=0"""),
            st(3, """Obe sú celé, preto §1\mid z§ aj §z\mid 0§, aj pre §z=0§.""")),
        look = listOf("""§1\mid z§: §k=z§""", """§z\mid 0§: §\ell=0§, aj pre §z=0§""")),
    TcDivClaim("tranzitivita", """a,b,c\in\mathbb{Z}""", """a\mid b\ \wedge\ b\mid c\ \Rightarrow\ a\mid c""", "Cvičení 6.1 c); proseminár 21/22 k 6. cvičeniu",
        why = """z §b=ka§ a §c=\ell b§ je §c=(\ell k)a§.""",
        proof = listOf(
            st(1, """Z predpokladov podľa definície: §b=ka§ a §c=\ell b§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Dosadím §b§ do druhej rovnosti.""", """c=\ell b=\ell(ka)=(\ell k)a"""),
            st(3, """§\ell k§ je celé ako súčin celých čísel, preto §a\mid c§.""")),
        look = listOf("""Dve rôzne písmená: §b=ka§, §c=\ell b§""", """Dosadenie: §c=(\ell k)a§""", """§\ell k\in\mathbb{Z}§, teda §a\mid c§""")),
    TcDivClaim("antisymetria", """a,b\in\mathbb{N}_0""", """a\mid b\ \wedge\ b\mid a\ \Rightarrow\ a=b""", "Cvičení 6.1 b); proseminár 21/22 k 6. cvičeniu",
        why = """z §b=k\ell b§ vyjde §a=b=0§ alebo §k=\ell=1§.""",
        proof = listOf(
            st(1, """Podľa definície §b=ka§ a §a=\ell b§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Dosadím §a§ do prvej rovnosti.""", """b=k(\ell b)=(k\ell)b"""),
            st(2, """Ak §b=0§, tak §a=\ell\cdot 0=0=b§."""),
            st(2, """Ak §b\neq 0§, vykrátim §b§: §k\ell=1§. Aj §a\neq 0§, inak by bolo §b=k\cdot 0=0§; teda §a,b>0§ a §k=\tfrac{b}{a}>0§, preto §k=\ell=1§."""),
            st(3, """V oboch prípadoch §a=b§.""")),
        look = listOf("""Prepis: §b=ka§, §a=\ell b§""", """Prípad §b=0§ zvlášť""", """Pri §b\neq 0§: §k\ell=1§ a kladnosť dá §k=1§""", """Záver §a=b§""")),
    TcDivClaim("antisymetria-z", """a,b\in\mathbb{Z}""", """a\mid b\ \wedge\ b\mid a\ \Rightarrow\ a=b""", "Spěvák, 6. prednáška; Cvičení 6.1 (na §\\mathbb{N}§ platí)",
        counter = { r -> val n = r.r(2, 12).toLong(); TcCounter("""a=$n,\ b=-$n""", """$n\cdot(-1)=-$n,\ \ (-$n)\cdot(-1)=$n""", """$n\neq -$n""", mapOf("a" to n, "b" to -n)) }),
    TcDivClaim("absolutne", """x,y\in\mathbb{Z}""", """x\mid y\ \wedge\ y\mid x\ \Rightarrow\ |x|=|y|""", "Spěvák, 6. prednáška",
        why = """z §y=k\ell y§ vyjde §x=y=0§ alebo §k\ell=1§, teda §|k|=1§.""",
        proof = listOf(
            st(1, """Podľa definície §y=kx§ a §x=\ell y§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Dosadím: §y=(k\ell)y§. Ak §y=0§, tak §x=\ell\cdot 0=0§ a §|x|=|y|§."""),
            st(2, """Ak §y\neq 0§, vykrátim §y§: §k\ell=1§. Súčin dvoch celých čísel je §1§ len pre §k=\ell=1§ alebo §k=\ell=-1§, teda §|k|=1§."""),
            st(3, """Potom §|y|=|k|\cdot|x|=|x|§.""")),
        look = listOf("""Prepis: §y=kx§, §x=\ell y§""", """Prípad §y=0§ zvlášť""", """§k\ell=1§, preto §|k|=1§""", """Záver §|x|=|y|§""")),
    TcDivClaim("zvysok", """a,b\in\mathbb{Z},\ a>0""", """a\mid b\ \Leftrightarrow\ b\bmod a=0""", "Cvičení 6.2 a); proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        why = """§b=ka§ je delenie so zvyškom §0§, a zvyšok je jednoznačný.""",
        proof = listOf(
            st(1, """§a\mid b§ znamená §b=ka§ pre celé §k§. §b\bmod a§ je zvyšok §r§ z §b=qa+r§, §0\leq r<a§ (Definice 6.2)."""),
            st(2, """§(\Rightarrow)§: §b=k\cdot a+r§, kde §r=0§ spĺňa §0\leq r<a§. Zvyšok je jednoznačný, preto §b\bmod a=0§ (Věta 6.2)."""),
            st(2, """§(\Leftarrow)§: pri §b\bmod a=0§ je §b=qa+r§ so §r=0§, teda §b=qa§ a §q\in\mathbb{Z}§."""),
            st(3, """Obe implikácie platia, preto §a\mid b\Leftrightarrow b\bmod a=0§.""")),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: §b=k\cdot a+r§ so §r=0§ a jednoznačnosť zvyšku (Věta 6.2)""", """§(\Leftarrow)§: §b=qa§, teda §a\mid b§""")),
    TcDivClaim("sucin", """a,b,c,d\in\mathbb{Z}""", """a\mid b\ \wedge\ c\mid d\ \Rightarrow\ ac\mid bd""", "Cvičení 6.2 b); proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        why = """z §b=ka§ a §d=\ell c§ je §bd=(k\ell)\cdot ac§.""",
        proof = listOf(
            st(1, """Podľa definície §b=ka§ a §d=\ell c§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Vynásobím obe rovnosti; násobenie je komutatívne.""", """bd=(ka)(\ell c)=(k\ell)\cdot ac"""),
            st(3, """§k\ell§ je celé, preto §ac\mid bd§.""")),
        look = listOf("""Dve rôzne písmená: §b=ka§, §d=\ell c§""", """§bd=(k\ell)\cdot ac§""", """§k\ell\in\mathbb{Z}§, teda §ac\mid bd§""")),
    TcDivClaim("kratenie", """a,b,c\in\mathbb{Z}""", """ac\mid bc\ \wedge\ c\neq 0\ \Rightarrow\ a\mid b""", "Cvičení 6.2 c); proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        why = """z §bc=k\cdot ac§ smieš krátiť nenulové §c§: §b=ka§.""",
        proof = listOf(
            st(1, """Podľa definície §bc=k\cdot ac§ pre nejaké §k\in\mathbb{Z}§."""),
            st(2, """§c\neq 0§, preto rovnosť smiem vydeliť §c§.""", """b=ka"""),
            st(3, """§k§ je celé, preto §a\mid b§.""")),
        look = listOf("""Prepis: §bc=k\cdot ac§""", """Krátenie §c§ s odvolaním na §c\neq 0§""", """§b=ka§, teda §a\mid b§""")),
    TcDivClaim("kratenie-nula", """a,b,c\in\mathbb{Z}""", """ac\mid bc\ \Rightarrow\ a\mid b""", "Cvičení 6.2 c) bez predpokladu §c\\neq 0§",
        counter = { r ->
            val a = r.r(2, 9).toLong(); var b: Long; do { b = r.r(1, 20).toLong() } while (b % a == 0L)
            TcCounter("""a=$a,\ b=$b,\ c=0""", """ac=0\mid 0=bc,\ \text{lebo}\ 0\cdot 1=0""", """$a\nmid $b""", mapOf("a" to a, "b" to b, "c" to 0L))
        }),
    TcDivClaim("euklid", """a,b,c\in\mathbb{Z}""", """a\mid bc\ \Rightarrow\ a\mid b\ \vee\ a\mid c""", "Cvičení 6.2 d); proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        counter = { r ->
            val u = r.r(2, 9).toLong(); val v = r.r(2, 9).toLong(); val a = u * v
            TcCounter("""a=$a,\ b=$u,\ c=$v""", """bc=$u\cdot $v=$a=a,\ \ a\mid a""", """$a\nmid $u\ \wedge\ $a\nmid $v""", mapOf("a" to a, "b" to u, "c" to v))
        }),
    TcDivClaim("abs-hodnoty", """a,b\in\mathbb{Z}""", """a\mid b\ \Leftrightarrow\ |a|\mid|b|""", "Legerský, 7. prednáška",
        why = """absolútne hodnoty zmenia len znamienko, a to sa prenesie do §k§.""",
        proof = listOf(
            st(1, """§(\Rightarrow)§: z §a\mid b§ je §b=ka§ pre nejaké §k\in\mathbb{Z}§."""),
            st(1, """§(\Leftarrow)§: z §|a|\mid|b|§ je §|b|=\ell|a|§ pre nejaké §\ell\in\mathbb{Z}§."""),
            st(2, """§(\Rightarrow)§: §|b|=|k|\cdot|a|§ a §|k|§ je celé."""),
            st(2, """§(\Leftarrow)§: §|a|=\varepsilon a§ a §b=\delta|b|§ pre nejaké §\varepsilon,\delta\in\{1,-1\}§, preto §b=\delta\ell\varepsilon\cdot a§."""),
            st(3, """§|k|§ aj §\delta\ell\varepsilon§ sú celé, preto platia obe implikácie.""")),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: §|b|=|k||a|§""", """§(\Leftarrow)§: znamienka §\pm 1§ sa prenesú do celého násobku""")),
    TcDivClaim("odhad", """a,b\in\mathbb{Z}""", """a\mid b\ \wedge\ b\neq 0\ \Rightarrow\ |a|\leq|b|""", "Spěvák, 6. prednáška; Legerský, 7. prednáška",
        why = """§b=ka§ a §b\neq 0§ dáva §k\neq 0§, teda §|k|\geq 1§.""",
        proof = listOf(
            st(1, """Podľa definície §b=ka§ pre nejaké §k\in\mathbb{Z}§."""),
            st(2, """§b\neq 0§, preto §k\neq 0§, a nenulové celé číslo má §|k|\geq 1§.""", """|b|=|k|\cdot|a|\geq 1\cdot|a|=|a|"""),
            st(3, """Teda §|a|\leq|b|§.""")),
        look = listOf("""Prepis §b=ka§""", """§k\neq 0§ z §b\neq 0§, preto §|k|\geq 1§""", """§|b|=|k||a|\geq|a|§""")),
    TcDivClaim("odhad-nula", """a,b\in\mathbb{Z}""", """a\mid b\ \Rightarrow\ |a|\leq|b|""", "Legerský, 7. prednáška (§7\\mid 0§)",
        counter = { r ->
            val a = r.r(2, 9).toLong() * (if (r.nextBoolean()) 1 else -1)
            TcCounter("""a=$a,\ b=0""", """${p(a)}\cdot 0=0""", """|$a|=${abs(a)}>0=|b|""", mapOf("a" to a, "b" to 0L))
        }),
    TcDivClaim("sucet", """a,b,c\in\mathbb{Z}""", """a\mid b\ \wedge\ a\mid c\ \Rightarrow\ a\mid(b+c)""", "Legerský, 7. prednáška; Tvrzení 6.1",
        why = """z §b=ka§ a §c=\ell a§ je §b+c=(k+\ell)a§.""",
        proof = listOf(
            st(1, """Podľa definície §b=ka§ a §c=\ell a§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Sčítam rovnosti a vyberiem §a§.""", """b+c=ka+\ell a=(k+\ell)a"""),
            st(3, """§k+\ell§ je celé, preto §a\mid(b+c)§.""")),
        look = listOf("""Dve rôzne písmená: §b=ka§, §c=\ell a§""", """§b+c=(k+\ell)a§""", """§k+\ell\in\mathbb{Z}§""")),
    TcDivClaim("nasobok", """a,b,n\in\mathbb{Z}""", """a\mid b\ \Rightarrow\ a\mid nb""", "Legerský, 7. prednáška; Tvrzení 6.1",
        why = """z §b=ka§ je §nb=(nk)a§.""",
        proof = listOf(
            st(1, """Podľa definície §b=ka§ pre nejaké §k\in\mathbb{Z}§."""),
            st(2, """Vynásobím rovnosť číslom §n§.""", """nb=n(ka)=(nk)a"""),
            st(3, """§nk§ je celé, preto §a\mid nb§ pre každé §n\in\mathbb{Z}§.""")),
        look = listOf("""Prepis §b=ka§""", """§nb=(nk)a§ a §nk\in\mathbb{Z}§""")),
    TcDivClaim("rozdiel", """a,b,c\in\mathbb{Z}""", """a\mid(b+c)\ \wedge\ a\mid b\ \Rightarrow\ a\mid c""", "Legerský, 7. prednáška; Spěvák, 6. prednáška",
        why = """z §b+c=ka§ a §b=\ell a§ je §c=(k-\ell)a§.""",
        proof = listOf(
            st(1, """Podľa definície §b+c=ka§ a §b=\ell a§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """Odčítam druhú rovnosť od prvej.""", """c=(b+c)-b=ka-\ell a=(k-\ell)a"""),
            st(3, """§k-\ell§ je celé, preto §a\mid c§.""")),
        look = listOf("""Dve rôzne písmená: §b+c=ka§, §b=\ell a§""", """§c=(k-\ell)a§""", """§k-\ell\in\mathbb{Z}§""")),
    TcDivClaim("scitanec", """a,b,c\in\mathbb{Z}""", """a\mid(b+c)\ \Rightarrow\ a\mid b""", "Legerský, 7. prednáška (opačný smer platí len s predpokladom §a\\mid c§)",
        counter = { r ->
            val a = r.r(2, 9).toLong(); var b: Long; do { b = r.r(1, 20).toLong() } while (b % a == 0L)
            val c = a * ((b + a - 1) / a) - b + a * r.r(0, 2)
            TcCounter("""a=$a,\ b=$b,\ c=$c""", """b+c=${b + c}=$a\cdot ${(b + c) / a}""", """$a\nmid $b""", mapOf("a" to a, "b" to b, "c" to c))
        }),
    TcDivClaim("kombinacie", """a,b,c\in\mathbb{Z}""", """(a\mid b\wedge a\mid c)\ \Leftrightarrow\ (\forall m,n\in\mathbb{Z})(a\mid mb+nc)""", "Spěvák, 6. prednáška; Legerský, 7. prednáška; Tvrzení 6.1",
        why = """§mb+nc=(mk+n\ell)a§, a naopak stačí voľby §(1,0)§ a §(0,1)§.""",
        proof = listOf(
            st(1, """§(\Rightarrow)§: podľa definície §b=ka§ a §c=\ell a§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
            st(2, """§(\Rightarrow)§: pre ľubovoľné §m,n\in\mathbb{Z}§ je §mb+nc=(mk+n\ell)a§ a §mk+n\ell§ je celé."""),
            st(2, """§(\Leftarrow)§: predpoklad platí pre každé §m,n§; voľba §m=1§, §n=0§ dá §a\mid b§ a voľba §m=0§, §n=1§ dá §a\mid c§."""),
            st(3, """Platia obe implikácie.""")),
        look = listOf("""Oba smery zvlášť""", """§(\Rightarrow)§: §mb+nc=(mk+n\ell)a§ pre ľubovoľné §m,n§""", """§(\Leftarrow)§: voľby §(1,0)§ a §(0,1)§""")),
)

fun tcDivProof(c: TcDivClaim, r: Random): Exercise {
    val ce = c.counter?.invoke(r)
    val (decide, _, _, end) = TcDivProofPostup
    val sample = if (ce == null) c.proof.map { (_, s, f) -> s + f.joinToString("") { " §$it§" } }
        else listOf("""Neplatí. Protipríklad: §${ce.values}§.""", """Predpoklad platí: §${ce.hyp}§.""", """Záver neplatí: §${ce.fails}§.""")
    val look = if (ce == null) c.look else listOf("Konkrétne čísla, nie všeobecné písmená", """Overené, že predpoklad platí""", """Overené, že záver neplatí""")
    val yes = "Áno, platí vždy"; val no = "Nie, existuje protipríklad"
    val choice = if (ce == null) Answer.Choice(listOf(yes, no), 0, tx("Platí: ${c.why}"),
        mapOf(1 to tx("Protipríklad neexistuje, tvrdenie platí. Dôvod: ${c.why}")))
    else Answer.Choice(listOf(yes, no), 1, tx("""Neplatí: §${ce.values}§ splní predpoklad, ale záver nie."""),
        mapOf(0 to tx("""Neplatí vždy: pre §${ce.values}§ je §${ce.hyp}§, ale §${ce.fails}§.""")))
    return Exercise(
        "dml-tc-delitelnost", "Vlastnosť deliteľnosti", "Šablóna Cvičení 6.1 a 6.2; ${c.src}",
        tx("""Rozhodněte, zda pro všechna §${c.vars}§ platí §${c.claim}§. Platnost dokažte z definice dělitelnosti, pro důkaz neplatnosti stačí uvést protipříklad."""),
        parts = listOf(
            { _ -> Part(tx("Platí tvrdenie pre všetky čísla zo zadania?"), choice, aim = 0,
                hint = tx("""Skús malé čísla, nulu aj záporné. Ak všetko vychádza, prepíš §x\mid y§ na §y=kx§.""")) },
            { _ -> Part(tx("Na papier: dôkaz z definície deliteľnosti, alebo protipríklad s overením."), Answer.Paper(sample.map(::tx), look.map(::tx),
                tx(if (ce == null) "Dôkaz musí každé §x\\mid y§ prepísať na §y=kx§ s celým §k§ a ukázať celý násobok. ${c.why.replaceFirstChar { it.uppercase() }}"
                    else "Protipríklad musí mať konkrétne čísla a overenie: §${ce.values}§ splní predpoklad, ale záver nie.")),
                aim = if (ce == null) 2 else 3, hint = tx(if (ce == null) "Prepíš predpoklady na rovnosti s celými §k§, §\\ell§ a dosaď." else "Hľadaj malé čísla, pri ktorých predpoklad platí a záver nie; pomôže nula alebo súčin dvoch čísel.")) },
        ),
        postup = TcDivProofPostup,
        solution = { _ ->
            TcDivProofPostup.solve {
                if (ce == null) {
                    decide(tx("Tvrdenie platí, preto ho dokážem priamo z definície deliteľnosti (Definice 6.1)."))
                    for ((aim, say, f) in c.proof) TcDivProofPostup[aim](tx(say), f.map { Formula(it) })
                } else {
                    decide(tx("Tvrdenie neplatí, na vyvrátenie stačí jeden protipríklad."), ce.values)
                    end(tx("Predpoklad platí, záver nie, teda tvrdenie vyvrátené."), Formula(ce.hyp), Formula(ce.fails))
                }
            }
        },
        hint = tx("""§x\mid y§ prepíš na §y=kx§ s celým §k§; pri podozrivom tvrdení skús malé čísla, nulu aj záporné."""),
    )
}

fun tcDivProofExercise(r: Random): Exercise {
    val pool = if (r.nextInt(10) < 3) TcDivClaims.filter { !it.holds } else TcDivClaims.filter { it.holds }
    return tcDivProof(pool.random(r), r)
}

/* ---- 2. Divisibility for every n without induction (Cvičení 6.3, 6.4, 6.19) ------------------------------------- */

/** A polynomial or a product of consecutive numbers, case by case over the remainders (both proseminars). */
val TcAllNPostup = postup(
    "Rozdeľ deliteľa" to "Zložené §d§ rozdeľ na mocniny rôznych prvočísel (§6=2\\cdot 3§) a každú over zvlášť.",
    "Rozbor zvyškov" to "Rozober všetky zvyšky §n§; za §n§ smieš dosadiť jeho zvyšok, kongruencia sa zachová (Tvrzení 6.6).",
    "Záver" to "Vo všetkých prípadoch vyšiel násobok; pri delení na časti delí aj ich súčin.",
)

/** A sum of two powers of n, through congruent bases (Cvičení 6.4 f, 6.19; proseminár 21/22 k 6. cvičeniu 33:58). */
val TcPowSumPostup = postup(
    "Oddeľ mocninu s §n§" to "Každý člen prepíš ako číslo krát mocninu so spoločným exponentom, §g^{kn+s}=g^s\\cdot(g^k)^n§.",
    "Zredukuj základy" to "Základy §g^k§ sú kongruentné, preto aj ich mocniny (Tvrzení 6.6).",
    "Záver" to "Po vytknutí mocniny ostane číslo deliteľné §d§.",
)

/** kind 0: d | a polynomial; 1: d | a product of k consecutive numbers; 2: d | c_n, a sum of two powers. */
data class TcAllNTask(val kind: Int, val d: Long, val poly: List<Pair<Long, Int>> = emptyList(), val k: Int = 0, val pw: TcPowSum? = null)

/** c_n = g1^(k1 n + s1) + c2 · g2^(k2 n + s2) for n ≥ n0, divisible by d: g1^k1 ≡ g2^k2 and g1^u1 + c2 g2^u2 ≡ 0 (mod d). */
data class TcPowSum(val g1: Long, val k1: Int, val s1: Int, val c2: Long, val g2: Long, val k2: Int, val s2: Int, val n0: Int) {
    val u1 get() = k1 * n0 + s1
    val u2 get() = k2 * n0 + s2
    val b1 get() = pow(g1, k1)
    val b2 get() = pow(g2, k2)
    val const get() = pow(g1, u1) + c2 * pow(g2, u2)
}

/** P(x) for the polynomial as (coefficient, exponent) pairs. */
fun tcPolyAt(poly: List<Pair<Long, Int>>, x: Long): Long = poly.sumOf { (c, e) -> c * pow(x, e) }

private fun polyTex(poly: List<Pair<Long, Int>>): String =
    Terms.sum(poly.sortedByDescending { it.second }.map { (c, e) -> Terms.times(c, Terms.pow("n", e)) })

private fun primePowers(d: Long): List<Long> = factor(d).map { (q, e) -> pow(q, e) }

fun tcAllNTask(r: Random): TcAllNTask = when (r.nextInt(3)) {
    0 -> {
        var t: TcAllNTask? = null
        while (t == null) {
            val d = listOf(2L, 3L, 3L, 5L, 6L, 6L).random(r)
            val e = when (d) { 2L -> r.r(2, 3); 5L -> 5; else -> 3 }
            val f = r.r(1, e - 1)
            val terms = HashMap<Int, Long>()
            terms[e] = 1L
            terms[f] = (terms[f] ?: 0L) + r.r(-7, 7)
            terms[1] = (terms[1] ?: 0L) + r.r(-9, 9)
            val poly = terms.filter { it.value != 0L }.map { it.value to it.key }
            if (poly.size < 2 || poly.all { md(it.first, d) == 0L }) continue
            if ((0L until d).any { md(tcPolyAt(poly, it), d) != 0L }) continue
            // the remainder whose value the exercise asks stays small enough to compute by hand
            if ((0L until d).any { abs(tcPolyAt(poly, it)) > 2000 }) continue
            t = TcAllNTask(0, d, poly)
        }
        t
    }
    1 -> when (r.r(2, 4)) { 2 -> TcAllNTask(1, 2, k = 2); 3 -> TcAllNTask(1, 6, k = 3); else -> TcAllNTask(1, if (r.nextBoolean()) 24 else 8, k = 4) }
    else -> {
        var t: TcAllNTask? = null
        while (t == null) {
            val d = r.rl(5, 23)
            val g1 = r.rl(2, 9); val k1 = r.r(1, 3); val g2 = r.rl(2, 9); val k2 = r.r(1, 2)
            if (g1 == g2) continue
            val b1 = pow(g1, k1); val b2 = pow(g2, k2)
            if (b1 == b2 || b1 > 200 || b2 > 200 || md(b1 - b2, d) != 0L || md(b1, d) <= 1L) continue
            val n0 = if (r.nextInt(3) == 0) 1 else 0
            val s1 = r.r(-n0, 2); val s2 = r.r(-n0, 2)
            if (n0 == 1 && s1 >= 0 && s2 >= 0) continue
            val c2 = listOf(1L, 1L, 1L, 2L, 3L).random(r)
            val pw = TcPowSum(g1, k1, s1, c2, g2, k2, s2, n0)
            if (pw.u1 < 0 || pw.u2 < 0 || pw.const % d != 0L || pw.const > 3000) continue
            t = TcAllNTask(2, d, pw = pw)
        }
        t
    }
}

private val CzCount = mapOf(2 to "dvou", 3 to "tří", 4 to "čtyř")

private fun consecutiveTex(k: Int) = (0 until k).joinToString("") { if (it == 0) "n" else "(n+$it)" }

/** For n ≡ r (mod q), the factor n + j of the k consecutive numbers that q divides. */
private fun factorFor(q: Long, r: Long) = md(q - r, q)

/** The exponent kn + s in TeX ("3n+1", "2n-1", "n"). */
private fun expTex(k: Int, s: Int, v: String = "n") = Terms.times(k, v) + Terms.plus(s.toLong())

fun tcPowSumTex(pw: TcPowSum): String = Terms.pow(pw.g1, expTex(pw.k1, pw.s1)) + "+" + Terms.times(pw.c2, Terms.pow(pw.g2, expTex(pw.k2, pw.s2)))

fun tcAllN(t: TcAllNTask, r: Random = Random(0)): Exercise = when (t.kind) {
    0 -> tcAllNPoly(t, r)
    1 -> tcAllNConsecutive(t, r)
    else -> tcAllNPowers(t)
}

private fun tcAllNPoly(t: TcAllNTask, rnd: Random): Exercise {
    val d = t.d; val poly = t.poly
    val pt = polyTex(poly)
    val qs = primePowers(d)
    val q = qs.random(rnd)
    val asked = rnd.rl(1, q - 1)
    val value = tcPolyAt(poly, asked)
    val (split, cases, end) = TcAllNPostup
    fun table(q: Long) = Formula("\\begin{aligned}" + (0L until q).joinToString("\\\\") { x -> "n\\equiv $x&:\\ P($x)=${tcPolyAt(poly, x)}\\equiv 0" } + "\\end{aligned}\\pmod{$q}",
        tx("Každý riadok je jeden zvyšok §n§ po delení §$q§."))
    val sample = buildList {
        add("§P(n)=$pt§." + if (qs.size > 1) " §$d=" + qs.joinToString("\\cdot ") + "§, každé prvočíslo zvlášť." else "")
        for (x in qs) add("Ak §n\\equiv r\\pmod{$x}§, je §P(n)\\equiv P(r)\\pmod{$x}§ (Tvrzení 6.6): " + (0L until x).joinToString(", ") { "§P($it)=${tcPolyAt(poly, it)}§" } + ", všetko násobky §$x§.")
        add(if (qs.size > 1) "§P(n)=2y§ pre celé §y§; §3\\mid 2y§ a §\\gcd(3,2)=1§, preto §3\\mid y§ (Lemma 6.2) a §6\\mid P(n)§ pre každé §n\\in\\mathbb{N}_0§." else "Vo všetkých prípadoch §$d\\mid P(n)§, teda pre každé §n\\in\\mathbb{N}_0§.")
    }
    return Exercise(
        "dml-tc-vsetky-n", "Deliteľnosť pre každé n", "Šablóna Cvičení 6.4; proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        tx("Dokažte bez použití matematické indukce, že pro každé §n\\in\\mathbb{N}_0§ platí §$d\\mid $pt§."),
        parts = listOf(
            { _ -> Part(tx("Rozbor zvyškov, prípad §n\\equiv $asked\\pmod{$q}§: koľko je hodnota §P($asked)§ v §\\mathbb{Z}§ pre §P(n)=$pt§?"), nums(listOf("P($asked)"), listOf(value), "§P($asked)=$value§") { g ->
                val v = g[0]
                when {
                    v == md(value, q) && value != v -> "To je zvyšok §P($asked)§ po delení §$q§. Zadaj hodnotu v §\\mathbb{Z}§: §P($asked)=$value§."
                    else -> "Dosaď §n=$asked§ do §$pt§: vyjde §$value§, a to je násobok §$q§."
                }
            }, aim = 1, hint = tx("Za §n§ dosaď §$asked§, lebo §n\\equiv $asked§ a kongruencia sa pri súčtoch, súčinoch a mocninách zachová.")) },
            { _ -> Part(tx("Na papier: celý dôkaz bez indukcie, rozborom zvyškov."), Answer.Paper(sample.map(::tx),
                listOf(if (qs.size > 1) "Rozdelenie §6=2\\cdot 3§, každé prvočíslo zvlášť" else "Zvyšky po delení prvočíslom §$d§", "Všetky zvyšky §n§, žiadny nevynechaný", "Dosadenie zvyšku zdôvodnené kongruenciou (Tvrzení 6.6)", "Záver pre každé §n\\in\\mathbb{N}_0§").map(::tx),
                tx("Dôkaz musí prejsť všetky zvyšky §n§ po delení " + qs.joinToString(" a ") { "§$it§" } + ". V každom prípade §P(r)§ vyjde násobok" + (if (qs.size > 1) "; potom §2\\cdot 3=6§." else "."))),
                aim = 1, hint = tx(if (qs.size > 1) "Zvlášť zvyšky po delení §2§ a po delení §3§." else "Zvyšky §0,\\dots,${d - 1}§ a v každom §P(r)§.")) },
        ),
        postup = TcAllNPostup,
        solution = { _ ->
            TcAllNPostup.solve {
                if (qs.size > 1) split(tx("§$d=" + qs.joinToString("\\cdot ") + "§ a sú to rôzne prvočísla, preto dokážem zvlášť deliteľnosť každým z nich."), "P(n)=$pt")
                else split(tx("§$d§ je prvočíslo, stačí jeden rozbor zvyškov po delení §$d§."), "P(n)=$pt")
                for (x in qs) cases(tx("Ak §n\\bmod $x=r§, je §n\\equiv r\\pmod{$x}§. Kongruencia sa zachová pri súčtoch, súčinoch aj mocninách, preto §P(n)\\equiv P(r)§ (Tvrzení 6.6)."), table(x))
                if (qs.size > 1) end(tx("Vo všetkých prípadoch delí §P(n)§ aj §2§ aj §3§. Teda §P(n)=2y§; §3\\mid 2y§ a §\\gcd(3,2)=1§, preto §3\\mid y§ (Lemma 6.2), a tak §6\\mid P(n)§ pre každé §n\\in\\mathbb{N}_0§."))
                else end(tx("Vo všetkých prípadoch §P(n)\\equiv 0\\pmod{$d}§, teda §$d\\mid P(n)§ pre každé §n\\in\\mathbb{N}_0§."))
            }
        },
        hint = tx("Rozober zvyšky §n§ po delení " + qs.joinToString(" a ") { "§$it§" } + "; v každom prípade dosaď zvyšok."),
    )
}

private fun tcAllNConsecutive(t: TcAllNTask, rnd: Random): Exercise {
    val d = t.d; val k = t.k
    val prod = consecutiveTex(k)
    val qs = primePowers(d)
    // what each part of d asks of the k numbers: a multiple of a prime q, or for 8 a multiple of 4 and another even one
    val q = qs.random(rnd)
    val mod = if (q == 8L) 4L else q
    val asked = rnd.rl(1, mod - 1)
    val j = factorFor(mod, asked)
    val (split, cases, end) = TcAllNPostup
    fun caseRows(x: Long): Formula = if (x == 8L)
        Formula("\\begin{aligned}" + (0L until 4).joinToString("\\\\") { rr -> val a = factorFor(4, rr); val b = (a + 2) % 4; "n\\equiv $rr&:\\ 4\\mid " + (if (a == 0L) "n" else "n+$a") + ",\\ 2\\mid " + (if (b == 0L) "n" else "n+$b") } + "\\end{aligned}\\pmod 4",
            tx("Dve párne čísla zo štyroch idú po §2§, jedno z nich je násobok §4§."))
    else Formula("\\begin{aligned}" + (0L until x).joinToString("\\\\") { rr -> val a = factorFor(x, rr); "n\\equiv $rr&:\\ $x\\mid " + (if (a == 0L) "n" else "n+$a") } + "\\end{aligned}\\pmod{$x}",
        tx("Pri zvyšku §r§ je §n+(${x}-r)\\equiv 0§."))
    val parts = qs.joinToString("\\cdot ")
    val sample = buildList {
        add("Súčin §$prod§." + if (qs.size > 1) " §$d=$parts§, každú časť zvlášť." else "")
        for (x in qs) add(if (x == 8L) "Podľa §n\\bmod 4§: jedno zo štyroch čísel je násobok §4§ a ďalšie, o §2§ vzdialené, je párne; súčin má činiteľ §4\\cdot 2=8§."
            else "Podľa §n\\bmod $x§: pri zvyšku §r=0§ je násobkom §$x§ samo §n§, inak §n+($x-r)§, a to je medzi činiteľmi.")
        add(if (qs.size > 1) "Súčin je §${qs[0]}y§; §${qs[1]}\\mid ${qs[0]}y§ a §\\gcd(${qs[1]},${qs[0]})=1§, preto §${qs[1]}\\mid y§ (Lemma 6.2) a §$d\\mid§ súčin pre každé §n\\in\\mathbb{Z}§." else "Teda §$d§ delí súčin pre každé §n\\in\\mathbb{Z}§.")
    }
    return Exercise(
        "dml-tc-vsetky-n", "Súčin po sebe idúcich čísel", "Šablóna Cvičení 6.3; proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        tx("Dokažte, že součin libovolných ${CzCount[k]} po sobě následujících celých čísel je vždy dělitelný §$d§."),
        parts = listOf(
            { _ -> Part(tx("Čísla §$prod§, prípad §n\\equiv $asked\\pmod{$mod}§: ktoré §n+j§ je deliteľné §$mod§? §j=§"), nums(listOf("j"), listOf(j), "§n+$j\\equiv $asked+$j\\equiv 0\\pmod{$mod}§") { g ->
                val v = g[0]
                when {
                    v == asked -> "§n+$asked\\equiv ${asked * 2}§, zvyšok sa zdvojnásobil. Doplň do §$mod§: §j=$mod-$asked=$j§."
                    v in 0 until k && v != j -> "§" + (if (v == 0L) "n" else "n+$v") + "\\equiv ${asked + v}\\not\\equiv 0\\pmod{$mod}§. Potrebuješ §$asked+j\\equiv 0§, teda §j=$j§."
                    else -> "§j§ je medzi §0§ a §${k - 1}§ a doplní zvyšok §$asked§ do §$mod§: §j=$j§."
                }
            }, aim = 1, hint = tx("Ak §n\\equiv $asked§, tak §n+j\\equiv $asked+j§. Kedy je to §0§ modulo §$mod§?")) },
            { _ -> Part(tx("Na papier: celý dôkaz bez indukcie, rozborom zvyškov."), Answer.Paper(sample.map(::tx),
                listOf(if (qs.size > 1) "Rozdelenie §$d=$parts§ na nesúdeliteľné časti" else "Zvyšky po delení §${if (q == 8L) 4 else d}§", "Všetky zvyšky, v každom konkrétny činiteľ", "Záver: súčin je deliteľný §$d§ pre každé §n\\in\\mathbb{Z}§").map(::tx),
                tx("Dôkaz musí v každom zvyšku ukázať činiteľa deliteľného " + qs.joinToString(" a ") { if (it == 8L) "§4§ (a ďalšieho párneho)" else "§$it§" } + ".")),
                aim = 1, hint = tx("Rozbor zvyškov po delení " + qs.joinToString(" a ") { if (it == 8L) "§4§" else "§$it§" } + "; v každom nájdi činiteľa.")) },
        ),
        postup = TcAllNPostup,
        solution = { _ ->
            TcAllNPostup.solve {
                if (qs.size > 1) split(tx("§$d=$parts§ a tieto čísla sú nesúdeliteľné, preto ukážem deliteľnosť každým zvlášť."), prod)
                else if (q == 8L) split(tx("§8=2^3§: ukážem, že medzi činiteľmi je násobok §4§ a ďalšie párne číslo."), prod)
                else split(tx("§$d§ je prvočíslo, stačí jeden rozbor zvyškov."), prod)
                for (x in qs) cases(tx(if (x == 8L) "Rozoberiem §n\\bmod 4§; párne čísla sú vždy dve, ob jedno." else "Rozoberiem §n\\bmod $x§: pri §r=0§ je násobkom §$x§ samo §n§, inak §n+($x-r)\\equiv r+$x-r\\equiv 0§, a to je medzi činiteľmi."), caseRows(x))
                end(tx(if (qs.size > 1) "Súčin je deliteľný §${qs[0]}§ aj §${qs[1]}§. Teda je to §${qs[0]}y§; §${qs[1]}\\mid ${qs[0]}y§ a §\\gcd(${qs[1]},${qs[0]})=1§, preto §${qs[1]}\\mid y§ (Lemma 6.2), a tak §$d§ delí súčin pre každé §n\\in\\mathbb{Z}§."
                    else if (q == 8L) "V každom prípade je jeden činiteľ násobok §4§ a ďalší párny, preto §8§ delí súčin pre každé §n\\in\\mathbb{Z}§."
                    else "V každom prípade je jeden činiteľ násobok §$d§, preto §$d§ delí súčin pre každé §n\\in\\mathbb{Z}§."))
            }
        },
        hint = tx("Rozbor zvyškov §n§; v každom prípade nájdi činiteľa deliteľného prvočíslom z rozkladu §$d§."),
    )
}

private fun tcAllNPowers(t: TcAllNTask): Exercise {
    val d = t.d; val pw = t.pw!!
    val v = if (pw.n0 == 0) "n" else "m"
    val dom = if (pw.n0 == 0) "\\mathbb{N}_0" else "\\mathbb{N}"
    val rho = md(pw.b1, d)
    // b1 ≡ b2 ≡ rho, each written once (64 ≡ 16 ≡ 4, not 9 ≡ 4 ≡ 4)
    val chainTex = listOf(pw.b1, pw.b2, rho).distinct().joinToString("\\equiv ") + "\\pmod{$d}"
    val powChain = listOf(pw.b1, pw.b2, rho).distinct().joinToString("\\equiv ") { Terms.pow(it, v) }
    val cn = tcPowSumTex(pw)
    val (split, reduce, end) = TcPowSumPostup
    val term1 = Terms.pow(pw.g1, expTex(pw.k1, pw.s1)) + "=" + Terms.times(pow(pw.g1, pw.u1), Terms.pow(pw.b1, v))
    val term2 = Terms.pow(pw.g2, expTex(pw.k2, pw.s2)) + "=" + Terms.times(pow(pw.g2, pw.u2), Terms.pow(pw.b2, v))
    val sum = "c_n&\\equiv " + Terms.times(pow(pw.g1, pw.u1), Terms.pow(rho, v)) + "+" + Terms.times(pw.c2 * pow(pw.g2, pw.u2), Terms.pow(rho, v)) + "\\\\&=${pw.const}\\cdot " + Terms.pow(rho, v) + "\\equiv 0\\pmod{$d}"
    val sample = listOf(
        (if (pw.n0 == 1) "Pre §n\\geq 1§ označím §m=n-1\\geq 0§. " else "") + "§$term1§, §$term2§.",
        "§$chainTex§, preto aj §$powChain§ (Tvrzení 6.6).",
        "§c_n\\equiv(" + pow(pw.g1, pw.u1) + "+" + (pw.c2 * pow(pw.g2, pw.u2)) + ")\\cdot " + Terms.pow(rho, v) + "=${pw.const}\\cdot " + Terms.pow(rho, v) + "\\equiv 0\\pmod{$d}§, lebo §$d\\mid ${pw.const}§.",
    )
    return Exercise(
        "dml-tc-vsetky-n", "Deliteľnosť súčtu mocnín", "Šablóna Cvičení 6.19 a 6.4 f); proseminár 21/22 k 6. cvičeniu",
        tx("Pomocí kongruencí dokažte, že číslo §c_n=$cn§ je dělitelné §$d§ pro každé §n\\in $dom§."),
        parts = listOf(
            { _ -> Part(tx((if (pw.n0 == 1) "S §m=n-1§: č" else "S §n\\in\\mathbb{N}_0§: č") + "len §" + Terms.pow(pw.g1, expTex(pw.k1, pw.s1)) + "§ prepíšeš ako §" + term1.substringAfter('=') + "§, člen §" + Terms.pow(pw.g2, expTex(pw.k2, pw.s2)) + "§ ako §" + term2.substringAfter('=') + "§. Základy §${pw.b1}§ a §${pw.b2}§ dajú po delení §$d§ ten istý zvyšok. Koľko je §${pw.b1}\\bmod $d§?"), nums(listOf("${pw.b1}\\bmod $d"), listOf(rho), "§$chainTex§") { g ->
                val x = g[0]
                when {
                    x == pw.b1 -> "§${pw.b1}§ je ešte väčšie ako §$d§. Zvyšok: §${pw.b1}=${pw.b1 / d}\\cdot $d+$rho§."
                    x == rho - d -> "§${rho - d}§ je kongruentné, ale zvyšok je z §\\{0,\\dots,${d - 1}\\}§: §$rho§."
                    else -> "§${pw.b1}=${pw.b1 / d}\\cdot $d+$rho§, teda zvyšok je §$rho§; rovnaký dá aj §${pw.b2}§."
                }
            }, aim = 1, hint = tx("Prepíš §" + Terms.pow(pw.g1, expTex(pw.k1, pw.s1)) + "§ ako číslo krát §" + Terms.pow(pw.b1, v) + "§ a vydeľ §${pw.b1}§ číslom §$d§.")) },
            { _ -> Part(tx("Na papier: celý dôkaz bez indukcie, pomocou kongruencií."), Answer.Paper(sample.map(::tx),
                listOf("Mocniny prepísané so spoločným exponentom §$v§" + if (pw.n0 == 1) " (§m=n-1§)" else "", "Základy kongruentné, mocniny tiež (Tvrzení 6.6)", "Vytknutá mocnina a číslo §${pw.const}§ deliteľné §$d§", "Záver pre každé §n\\in $dom§").map(::tx),
                tx("Dôkaz musí ukázať §${pw.b1}\\equiv ${pw.b2}\\pmod{$d}§ a po vytknutí §" + Terms.pow(rho, v) + "§ číslo §${pw.const}§, ktoré je násobok §$d§.")),
                aim = 2, hint = tx("§${pw.b1}\\equiv ${pw.b2}\\pmod{$d}§; potom vytkni spoločnú mocninu.")) },
        ),
        postup = TcPowSumPostup,
        solution = { _ ->
            TcPowSumPostup.solve {
                split(tx((if (pw.n0 == 1) "Pre §n\\geq 1§ označím §m=n-1\\geq 0§; exponent §m§ potom začína od nuly. " else "") + "Každý člen prepíšem ako číslo krát mocninu so spoločným exponentom §$v§."),
                    Formula(term1), Formula(term2))
                reduce(tx("§$d\\mid ${pw.b1}-${pw.b2}§, preto §$chainTex§. Kongruencia sa zachová pri mocnine aj súčine, preto smiem základy nahradiť (Tvrzení 6.6)."),
                    Formula("\\begin{aligned}$sum\\end{aligned}"))
                end(tx((if (pw.const == d) "§${pw.const}§ je násobok §$d§" else "§${pw.const}=${pw.const / d}\\cdot $d§") + ", preto §c_n\\equiv 0\\pmod{$d}§, teda §$d\\mid c_n§ pre každé §n\\in $dom§."))
            }
        },
        hint = tx("Prepíš oba členy s tou istou mocninou §$v§ a porovnaj základy modulo §$d§."),
    )
}

/* ---- 3. gcd and lcm from the prime factorisations (Pozorování 6.1) ----------------------------------------------- */

val TcFactorPostup = postup(
    "Rozklad na prvočísla" to "Rozlož §|a|§ a §|b|§; znamienka §\\gcd§ ani §\\lcm§ nemenia.",
    "Tie isté prvočísla" to "Chýbajúce prvočísla doplň s exponentom §0§, aby obe čísla mali tú istú §k§-ticu.",
    "§\\gcd§: menší exponent" to "Pri každom prvočísle vezmi menší z dvoch exponentov.",
    "§\\lcm§: väčší exponent" to "Pri každom prvočísle vezmi väčší z dvoch exponentov.",
)

data class TcFactorTask(val a: Long, val b: Long)

fun tcFactorTask(r: Random): TcFactorTask {
    val primes = listOf(2L, 3L, 5L, 7L, 11L, 13L, 17L)
    while (true) {
        val ps = primes.shuffled(r).take(r.r(3, 4)).sorted()
        val ea = ps.map { r.r(0, 3) }; val eb = ps.map { r.r(0, 3) }
        if (ps.indices.any { ea[it] == 0 && eb[it] == 0 }) continue
        val a = ps.indices.fold(1L) { acc, i -> acc * pow(ps[i], ea[i]) }
        val b = ps.indices.fold(1L) { acc, i -> acc * pow(ps[i], eb[i]) }
        if (a !in 12..3000 || b !in 12..3000 || a == b || gcd(a, b) == 1L || a % b == 0L || b % a == 0L) continue
        // a prime both have with different exponents, so the minimum and the maximum differ somewhere
        if (ps.indices.none { ea[it] > 0 && eb[it] > 0 && ea[it] != eb[it] } && r.nextInt(3) > 0) continue
        val sa = if (r.nextInt(4) == 0) -1L else 1L; val sb = if (r.nextInt(4) == 0) -1L else 1L
        return TcFactorTask(sa * a, sb * b)
    }
}

/** 420 = 2·210 = 2^2·105 = 2^2·3·35 = 2^2·3·5·7: one division by the smallest prime per step. */
private fun divChain(n: Long): String {
    val steps = ArrayList<String>(); var rest = n; val got = ArrayList<Long>()
    steps += "$n"
    while (rest > 1) {
        val q = (2L..rest).first { rest % it == 0L }
        got += q; rest /= q
        val head = got.groupBy { it }.entries.joinToString("\\cdot ") { (pp, l) -> Terms.pow(pp, l.size) }
        steps += if (rest > 1) "$head\\cdot $rest" else head
    }
    return steps.joinToString("=")
}

/** The exponents of [n] at [ps], zero for a prime that does not divide it. */
private fun exps(n: Long, ps: List<Long>): List<Int> { val f = factor(abs(n)).toMap(); return ps.map { f[it] ?: 0 } }

/** The factorisation over [ps] with every exponent written, zero too (Pozorování 6.1), the first power as the prime alone. */
private fun alignedTex(ps: List<Long>, es: List<Int>) = ps.indices.joinToString("\\cdot ") { if (es[it] == 1) "${ps[it]}" else "${ps[it]}^{${es[it]}}" }

fun tcFactor(t: TcFactorTask): Exercise {
    val (a, b) = t
    val ps = (factor(abs(a)).map { it.first } + factor(abs(b)).map { it.first }).distinct().sorted()
    val ea = exps(a, ps); val eb = exps(b, ps)
    val mins = ps.indices.map { minOf(ea[it], eb[it]) }; val maxs = ps.indices.map { maxOf(ea[it], eb[it]) }
    val g = gcd(a, b); val l = lcm(a, b)
    val wrongMax = ps.indices.filter { ea[it] > 0 && eb[it] > 0 }.fold(1L) { acc, i -> acc * pow(ps[i], maxs[i]) }
    val common1 = ps.indices.filter { ea[it] > 0 && eb[it] > 0 }.fold(1L) { acc, i -> acc * ps[i] }
    val (fact, same, gAim, lAim) = TcFactorPostup
    fun signed(n: Long) = (if (n < 0) "-" else "") + tcFactorTex(abs(n))
    return Exercise(
        "dml-tc-rozklad", "gcd a lcm z rozkladov", "Šablóna Cvičení 6.5; Pozorování 6.1; proseminár 25/26 č. 6 a 21/22 k 6. cvičeniu",
        tx("Pomocí prvočíselných rozkladů určete §\\gcd(a,b)§ a §\\lcm(a,b)§ pro §a=$a§ a §b=$b§."),
        parts = listOf(
            { _ -> Part(tx("§\\gcd($a,$b)§:"), nums(listOf("\\gcd"), listOf(g), "§\\gcd(a,b)=" + alignedTex(ps, mins) + "=$g§") { gv ->
                val v = gv[0]
                when {
                    v == l -> "To je §\\lcm§. Pri §\\gcd§ ber menší z exponentov, nie väčší (Pozorování 6.1)."
                    v == wrongMax && v != g -> "Spoločné prvočísla sedia, ale exponent ber menší, nie väčší: §\\gcd=" + alignedTex(ps, mins) + "=$g§."
                    v == -g -> "§\\gcd§ nie je záporný. Definice 6.3 ho berie z §\\mathbb{N}_0§; znamienka nehrajú rolu."
                    v == common1 && v != g -> "Exponenty: prvočíslo, ktoré je v oboch číslach v mocnine, ber s menším z exponentov, nie len raz."
                    v > 0 && g % v == 0L -> "§$v§ je spoločný deliteľ, ale nie najväčší. Pri každom prvočísle ber menší exponent: §$g§."
                    else -> "Porovnaj exponenty: §|a|=" + alignedTex(ps, ea) + "§, §|b|=" + alignedTex(ps, eb) + "§; menšie dajú §$g§."
                }
            }, aim = 2, hint = tx("Rozlož obe čísla, chýbajúce prvočísla s exponentom §0§. Pri §\\gcd§ ber menší exponent.")) },
            { _ -> Part(tx("§\\lcm($a,$b)§:"), nums(listOf("\\lcm"), listOf(l), "§\\lcm(a,b)=" + alignedTex(ps, maxs) + "=$l§") { gv ->
                val v = gv[0]
                when {
                    v == abs(a * b) -> "To je súčin §|a|\\cdot|b|§. Pri spoločnom prvočísle ber väčší exponent, nie súčet exponentov."
                    v == g -> "To je §\\gcd§. Pri §\\lcm§ ber väčší z exponentov."
                    v == -l -> "§\\lcm§ nie je záporný. Definice 6.3 ho berie z §\\mathbb{N}_0§."
                    v > 0 && v % l == 0L -> "§$v§ je spoločný násobok, ale nie najmenší. Pri každom prvočísle ber väčší exponent: §$l§."
                    else -> "Pri každom prvočísle ber väčší exponent: §\\lcm=" + alignedTex(ps, maxs) + "=$l§."
                }
            }, aim = 3, hint = tx("Tá istá tabuľka exponentov; pri §\\lcm§ ber väčší.")) },
        ),
        postup = TcFactorPostup,
        solution = { _ ->
            TcFactorPostup.solve {
                fact(tx("Obe čísla rozložím na prvočísla: vždy oddelím najmenšie prvočíslo, ktoré ešte delí zvyšok."), Formula(divChain(abs(a))), Formula(divChain(abs(b))))
                if (a < 0 || b < 0) fact(tx("Znamienka nehrajú rolu, lebo §a§ a §|a|§ majú tých istých deliteľov aj násobky (Lemma 6.1)."))
                same(tx("Chýbajúce prvočísla doplním s exponentom §0§, lebo Pozorování 6.1 chce v oboch číslach tú istú §k§-ticu prvočísel."),
                    Formula("\\begin{aligned}|a|&=" + alignedTex(ps, ea) + "\\\\|b|&=" + alignedTex(ps, eb) + "\\end{aligned}"))
                gAim(tx("Pri každom prvočísle vezmem menší exponent, lebo spoločný deliteľ ho nesmie prekročiť ani v jednom čísle (Pozorování 6.1)."),
                    Formula("\\begin{aligned}\\gcd(a,b)&=" + alignedTex(ps, mins) + "\\\\&=$g\\end{aligned}"))
                lAim(tx("Pri každom prvočísle vezmem väčší exponent, lebo spoločný násobok musí obsiahnuť obe čísla (Pozorování 6.1)."),
                    Formula("\\begin{aligned}\\lcm(a,b)&=" + alignedTex(ps, maxs) + "\\\\&=$l\\end{aligned}"))
                lAim(tx("Skúška: §$g\\cdot $l=${g * l}=|a|\\cdot|b|§ sedí, lebo súčin §\\gcd§ a §\\lcm§ je vždy §|a|\\cdot|b|§ (Tvrzení 6.3)."))
            }
        },
        hint = tx("Rozlož obe čísla na prvočísla, chýbajúce doplň s exponentom §0§. §\\gcd§: menšie exponenty, §\\lcm§: väčšie."),
    )
}

/* ---- 4. Practical reduction modulo m and the opposite element (Příklad 6.10, Cvičení 6.13 b, 6.15) -------------- */

/** shape 0: ab + cd, 1: ab − cd, 2: ab + c^e, 3: (a + b)c. */
data class TcReduceTask(val m: Long, val shape: Int, val xs: List<Long>, val e: Int = 0)

fun tcReduceTask(r: Random): TcReduceTask {
    val m = if (r.nextInt(4) == 0) listOf(120L, 243L, 666L, 101L, 100L).random(r) else r.rl(5, 37)
    val shape = r.nextInt(4)
    val n = if (shape == 3) 3 else 4
    // half of the numbers just below or above a multiple of m: a small negative representative saves work (Spěvák, Dombek)
    val xs = (0 until n).map { if (r.nextBoolean()) m * r.rl(2, 3000 / m + 40) + r.rl(-3, 3) else r.rl(m + 1, 99999) }
    val e = if (shape == 2) r.r(2, 5) else 0
    val t = TcReduceTask(m, shape, if (shape == 2) xs.take(2) + (xs[2] % 1000 + m + 1) else xs, e)
    // a representative 0 or 1 makes a factor vanish or stand alone; draw again
    return if (t.xs.any { tcRep(it, m) in 0L..1L }) tcReduceTask(r) else t
}

/** The representative the solution writes: the remainder, or a small negative number when that is closer (241 ≡ −2 mod 243). */
fun tcRep(x: Long, m: Long): Long { val q = md(x, m); return if (m - q <= 3 && m - q < q) q - m else q }

fun tcReduceTex(t: TcReduceTask, v: List<String>): String = when (t.shape) {
    0 -> "${v[0]}\\cdot ${v[1]}+${v[2]}\\cdot ${v[3]}"
    1 -> "${v[0]}\\cdot ${v[1]}-${v[2]}\\cdot ${v[3]}"
    2 -> "${v[0]}\\cdot ${v[1]}+" + Terms.pow(v[2], t.e)
    else -> "(${v[0]}+${v[1]})\\cdot ${v[2]}"
}

/** The value of the expression on [xs] (the representatives), exact. */
fun tcReduceValue(t: TcReduceTask, xs: List<Long>): Long = when (t.shape) {
    0 -> xs[0] * xs[1] + xs[2] * xs[3]
    1 -> xs[0] * xs[1] - xs[2] * xs[3]
    2 -> xs[0] * xs[1] + pow(xs[2], t.e)
    else -> (xs[0] + xs[1]) * xs[2]
}

/** The computation on the representatives as on paper, reducing after every operation (Tvrzení 6.6): rows of an aligned block. */
private fun reduceRows(t: TcReduceTask, r: List<Long>, m: Long): List<String> {
    fun red(v: Long) = if (md(v, m) == v) "" else "\\equiv ${md(v, m)}"
    fun prod(a: Long, b: Long) = "${p(a)}\\cdot ${p(b)}&=${a * b}" + red(a * b)
    return when (t.shape) {
        0, 1 -> {
            val pp = md(r[0] * r[1], m); val qq = md(r[2] * r[3], m); val s = if (t.shape == 0) pp + qq else pp - qq
            listOf(prod(r[0], r[1]), prod(r[2], r[3]), "x&\\equiv $pp" + (if (t.shape == 0) "+" else "-") + "$qq=$s" + red(s))
        }
        2 -> {
            val pp = md(r[0] * r[1], m)
            val rows = arrayListOf(prod(r[0], r[1]))
            var cur = r[2]
            for (k in 2..t.e) { val pr = cur * r[2]; rows += Terms.pow(r[2], k) + "&\\equiv ${p(cur)}\\cdot ${p(r[2])}=$pr" + red(pr); cur = md(pr, m) }
            rows += "x&\\equiv $pp+$cur=${pp + cur}" + red(pp + cur)
            rows
        }
        else -> {
            val sum = r[0] + r[1]; val s1 = md(sum, m)
            listOf("${p(r[0])}+${p(r[1])}&=$sum" + red(sum), "x&\\equiv $s1\\cdot ${p(r[2])}=${s1 * r[2]}" + red(s1 * r[2]))
        }
    }
}

fun tcReduce(t: TcReduceTask): Exercise {
    val m = t.m
    val reps = t.xs.map { tcRep(it, m) }
    val e = tcReduceTex(t, t.xs.map { "$it" })
    val onReps = tcReduceTex(t, reps.map(::p))
    val value = tcReduceValue(t, reps)
    val res = md(value, m)
    val opp = md(-res, m)
    val (each, compute) = TcReducePostup
    val repsTex = "\\begin{aligned}" + t.xs.zip(reps).distinct().joinToString("\\\\") { (x, q) -> "$x&\\equiv $q" } + "\\end{aligned}\\pmod{$m}"
    return Exercise(
        "dml-tc-redukcia", "Redukcia výrazu modulo m", "Šablóna Příkladu 6.10 a Cvičení 6.13 b), 6.15; proseminár 21/22 k 7. cvičeniu",
        tx("Spočtěte §x=($e)\\bmod $m§ tak, že čísla průběžně nahradíte jejich zbytky. Potom určete opačný prvek §-x§ v §\\mathbb{Z}_{$m}§."),
        parts = listOf(
            { _ -> Part(tx("§x§:"), nums(listOf("x"), listOf(res), "§x=$res§") { g ->
                val v = g[0]
                when {
                    v == value && value != res -> "To je hodnota na zvyškoch, ešte ju zredukuj: §$value\\equiv $res\\pmod{$m}§."
                    v < 0 && md(v, m) == res -> "§$v§ je kongruentné, ale zvyšok je z §\\{0,\\dots,${m - 1}\\}§: pripočítaj §$m§."
                    v == opp && opp != res -> "To je opačný prvok. Výraz sám dá §$res§."
                    else -> "Nahraď čísla zvyškami: §$onReps=$value\\equiv $res\\pmod{$m}§."
                }
            }, aim = 1, hint = tx("Každé číslo nahraď jeho zvyškom po delení §$m§; číslo tesne pod násobkom §$m§ ako malé záporné.")) },
            { _ -> Part(tx("Opačný prvok §-x§ k §x=$res§ v §\\mathbb{Z}_{$m}§:"), nums(listOf("-x"), listOf(opp), "§-$res\\equiv $opp\\pmod{$m}§") { g ->
                val v = g[0]
                when {
                    v == -res && res != 0L -> "§-$res§ nie je prvok §\\mathbb{Z}_{$m}§. Opačný prvok je §$m-$res=$opp§."
                    v == res && res != opp -> "Opačný prvok dá so §$res§ súčet §0§ v §\\mathbb{Z}_{$m}§: §$res+$opp=$m\\equiv 0§."
                    else -> if (res == 0L) "Opačný prvok k §0§ je §0§, lebo §0+0\\equiv 0\\pmod{$m}§." else "Opačný prvok k §$res§ je §$m-$res=$opp§, lebo §$res+$opp=$m\\equiv 0\\pmod{$m}§."
                }
            }, aim = 1, hint = tx("§-x§ je číslo z §\\{0,\\dots,${m - 1}\\}§, ktoré so §x§ dá súčet deliteľný §$m§.")) },
        ),
        postup = TcReducePostup,
        solution = { _ ->
            TcReducePostup.solve {
                each(tx("Každé číslo nahradím kongruentným malým číslom, lebo kongruencia sa pri sčítaní aj násobení zachová (Tvrzení 6.6)."), Formula(repsTex,
                    tx("Číslo tesne pod násobkom §$m§ píšem ako malé záporné.")))
                compute(tx("So zvyškami počítam ďalej a po každej operácii hneď redukujem, takže čísla ostanú malé."), Formula("\\begin{aligned}" + reduceRows(t, reps, m).joinToString("\\\\") + "\\end{aligned}\\pmod{$m}"))
                compute(tx(if (res == 0L) "Opačný prvok k §0§ je §0§, lebo §0+0\\equiv 0§." else "Opačný prvok k §$res§ je §$m-$res§, lebo ich súčet je §$m\\equiv 0§."), "-x\\equiv $opp\\pmod{$m}")
            }
        },
        hint = tx("Každé číslo nahraď zvyškom (aj záporným, ak je bližšie), počítaj so zvyškami a výsledok zredukuj."),
    )
}

/* ---- 5. Quotient and remainder, also of a negative number and of an expression (Věta 6.2, Cvičení 6.13 a) ------- */

/** kind 0: a negative number, 1: a positive one, 2: an expression computed in Z first (Cvičení 6.13 a). */
data class TcDivTask(val kind: Int, val d: Long, val a: Long, val expr: String = "")

fun tcDivTask(r: Random): TcDivTask = when (r.nextInt(3)) {
    0 -> { val d = r.rl(2, 30); var a: Long; do { a = -r.rl(10, 999) } while (a % d == 0L && r.nextInt(3) > 0); TcDivTask(0, d, a) }
    1 -> { val d = r.rl(2, 30); TcDivTask(1, d, r.rl(d + 1, 9999)) }
    else -> {
        val d = listOf(7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L).random(r)
        val x = List(8) { r.rl(2, 25) }
        val neg = r.nextInt(3) == 0
        val inner = x[0] * x[1] * x[2] + x[3] * x[4] + (if (neg) -1 else 1) * x[5] * x[6]
        val a = inner * x[7]
        val tex = "(${x[0]}\\cdot ${x[1]}\\cdot ${x[2]}+${x[3]}\\cdot ${x[4]}" + (if (neg) "-" else "+") + "${x[5]}\\cdot ${x[6]})\\cdot ${x[7]}"
        TcDivTask(2, d, a, tex)
    }
}

fun tcDiv(t: TcDivTask): Exercise {
    val (kind, d, a) = t
    val q = a.floorDiv(d); val r = md(a, d)
    val aa = abs(a); val qq = aa / d; val rr = aa % d
    val (absAim, fix, check) = TcDivisionPostup
    return Exercise(
        "dml-tc-deleni", "Podiel a zvyšok", "Šablóna Vety 6.2 a Cvičení 6.13 a); proseminár 21/22 k 7. cvičeniu",
        tx(if (kind == 2) "Výraz §a=${t.expr}§ spočtěte v §\\mathbb{Z}§ a určete celočíselný podíl §q§ a zbytek §r§ po dělení čísla §a§ číslem §d=$d§, tj. §a=q\\cdot $d+r§, §0\\leq r<$d§."
            else "Určete celočíselný podíl §q§ a zbytek §r§ po dělení čísla §a=$a§ číslem §d=$d§, tj. §a=q\\cdot $d+r§, §0\\leq r<$d§."),
        parts = listOf({ _ -> Part(tx("Podiel §q§ a zvyšok §r§:"), nums(listOf("q", "r"), listOf(q, r), "§$a=${p(q)}\\cdot $d+$r§") { g ->
            val (gq, gr) = g
            when {
                gr < 0 -> "Zvyšok nesmie byť záporný (Definice 6.2). Podiel zmenši o §1§ a zvyšok zväčši o §$d§: §q=$q§, §r=$r§."
                gr >= d -> "Zvyšok musí byť menší než §$d§. §$gr§ ešte obsahuje celé §$d§."
                a < 0 && gq == -qq && gr == rr && rr != 0L -> "To je zvyšok §|a|§. Pri zápornom §a§ je §a=(-$qq)\\cdot $d-$rr§, a §-$rr§ treba opraviť: §q=$q§, §r=$r§."
                gq * d + gr != a -> "Skúška nesedí: §${p(gq)}\\cdot $d+$gr=${gq * d + gr}\\neq $a§."
                else -> "Skúška: §${p(q)}\\cdot $d+$r=$a§."
            }
        }, aim = 1, hint = tx(if (a < 0) "Vydeľ §|a|=$aa§, vráť znamienko a záporný zvyšok oprav: podiel o §1§ menší, zvyšok o §$d§ väčší." else "Koľkokrát sa §$d§ zmestí do §a§? Zvyšok je to, čo ostane.")) }),
        postup = TcDivisionPostup,
        solution = { _ ->
            TcDivisionPostup.solve {
                if (kind == 2) absAim(tx("Najprv výraz spočítam v §\\mathbb{Z}§."), "a=${t.expr}=$a")
                if (a < 0) {
                    absAim(tx("Najprv delím §|a|=$aa§ a potom vrátim znamienko, lebo delenie kladného čísla poznám (dôkaz Vety 6.2)."), "$aa=$qq\\cdot $d+$rr\\quad\\Rightarrow\\quad $a=(-$qq)\\cdot $d-$rr")
                    if (rr != 0L) fix(tx("§-$rr§ zvyšok nie je, zvyšok musí byť v §\\{0,\\dots,${d - 1}\\}§. Podiel zmenším o §1§ a zvyšok zväčším o §$d§."), "$a=${p(q)}\\cdot $d+($d-$rr)=${p(q)}\\cdot $d+$r")
                    else fix(tx("Zvyšok je §0§, opravovať netreba."))
                } else {
                    absAim(tx("§a=$a§ je nezáporné, delím priamo."), "$a=$q\\cdot $d+$r")
                    fix(tx("§0\\leq $r<$d§, zvyšok opravovať netreba."))
                }
                check(tx("Teda §q=$q§ a §r=$a\\bmod $d=$r§. Skúška: §${p(q)}\\cdot $d+$r=$a§."))
            }
        },
        hint = tx("Zvyšok je vždy z §\\{0,\\dots,${d - 1}\\}§, aj pri zápornom čísle; podiel môže byť záporný."),
    )
}

/* ---- 6. Word problems on a Diophantine equation (Cvičení 6.9 to 6.11; proseminár 21/22 k 6. cvičeniu 1:23:06) ---- */

/** Measuring with two hourglasses: the fewest turns and how long it takes (proseminár 21/22 k 6. cvičeniu, 1:23:06–1:30:26). */
val TcHourglassPostup = postup(
    "Rovnica" to "Behy jedných hodín ráta §x§, druhých §y§; kladné na dlhšej strane, záporné na kratšej: §ax+by=t§.",
    "Všetky riešenia" to "Jedno riešenie z REA, ostatné §(x_0+ks,\\ y_0-k\\ell)§, §s=b/\\gcd§, §\\ell=a/\\gcd§ (Věta 6.8).",
    "Najmenej otočení" to "Hľadaj §k§ s najmenším §|x|+|y|§; stačí skúsiť §k§ okolo zmeny znamienka §x§ a §y§.",
    "Postup merania" to "Opačné znamienka: spusti obe naraz, interval je medzi koncom kratšej a dlhšej strany. Inak spusti behy za sebou.",
)

/** kind 0: buying two goods for exactly c, each count at least [least]; 1: coins without change; 2: hourglasses. */
data class TcWordLdrTask(val kind: Int, val a: Long, val b: Long, val c: Long, val least: Long = 0)

/** The nonnegative (or ≥ least) solutions of ax + by = c, by brute force over x. */
fun tcWordSolutions(a: Long, b: Long, c: Long, least: Long): List<Pair<Long, Long>> =
    (least..c / a).mapNotNull { x -> val rest = c - a * x; if (rest >= b * least && rest % b == 0L) x to rest / b else null }

/** The solution of ax + by = t with the smallest |x| + |y| (null when there are none or two tie), by brute force. */
fun tcFewestTurns(a: Long, b: Long, t: Long): Pair<Long, Long>? {
    val all = (-60L..60L).mapNotNull { x -> val rest = t - a * x; if (rest % b == 0L) x to rest / b else null }
    if (all.isEmpty()) return null
    val best = all.minOf { abs(it.first) + abs(it.second) }
    return all.filter { abs(it.first) + abs(it.second) == best }.singleOrNull()
}

fun tcWordLdrTask(r: Random): TcWordLdrTask {
    while (true) {
        when (r.nextInt(3)) {
            2 -> {
                val a = r.rl(5, 17); val b = r.rl(3, a - 1); val t = r.rl(2, a + 4)
                if (t == a || t == b || gcd(a, b) != 1L && r.nextInt(2) == 0) continue
                if (t % gcd(a, b) != 0L && r.nextInt(4) > 0) continue
                if (t % gcd(a, b) == 0L && tcFewestTurns(a, b, t) == null) continue
                return TcWordLdrTask(2, a, b, t)
            }
            else -> {
                // 0: buying, 1: coins, 3: a camp where every child gets one of each (Cvičení 6.10 b)
                val kind = listOf(0, 1, 3).random(r)
                val g = if (r.nextInt(3) == 0) r.rl(2, 4) else 1L
                val a = g * r.rl(3, 16); val b = g * r.rl(3, 16)
                if (a == b || gcd(a / g, b / g) != 1L) continue
                val least = when { kind == 3 -> r.rl(3, 10); kind == 0 && r.nextBoolean() -> 1L; else -> 0L }
                val c = g * r.rl(if (kind == 3) 60 else 25, if (kind == 3) 300 else 160)
                val sols = tcWordSolutions(a, b, c, least)
                if (kind == 3 && (sols.size < 2 || sols.size > 8)) continue
                if (sols.isEmpty() && r.nextInt(5) > 0 || sols.size > 9) continue
                return TcWordLdrTask(kind, a, b, c, least)
            }
        }
    }
}

fun tcWordLdr(t: TcWordLdrTask): Exercise = if (t.kind == 2) tcHourglass(t) else tcBuy(t)

private fun tcBuy(t: TcWordLdrTask): Exercise = if (t.kind == 3) tcCamp(t) else tcBuyCount(t)

/** The most children when each gets at least one of each: the largest min(x, y) over the solutions with x, y ≥ least. */
fun tcCampMax(a: Long, b: Long, c: Long, least: Long): Long = tcWordSolutions(a, b, c, least).maxOf { minOf(it.first, it.second) }

private fun tcCamp(t: TcWordLdrTask): Exercise {
    val (_, a, b, c, least) = t
    val sols = tcWordSolutions(a, b, c, least)
    val best = tcCampMax(a, b, c, least)
    val l = ldr(a, b, c)!!
    val g = gcd(a, b)
    val (allAim, cond, pick) = TcLdrNonnegPostup
    return Exercise(
        "dml-tc-slovna-ldr", "Koľko detí na tábore", "Šablóna Cvičení 6.10 b) a 6.11 b)",
        tx("Jedna klobása stojí §$a§ Kč a jeden buřt §$b§ Kč. Vedoucí tábora utratí přesně §$c§ Kč a koupí každému dítěti alespoň jeden buřt a alespoň jednu klobásu (někteří mohou dostat víc). Určete všechny možné počty dětí na táboře, je-li jich alespoň §$least§. Kolik jich může být nejvýše?"),
        parts = listOf({ _ -> Part(tx("Najväčší možný počet detí:"), nums(listOf("n"), listOf(best), "§n=$best§") { gv ->
            val v = gv[0]
            val maxX = sols.maxOf { it.first }
            when {
                v == maxX && maxX != best -> "Pri §$maxX§ klobásach je buřtov len §${sols.first { it.first == maxX }.second}§; každé dieťa potrebuje oboje, rozhoduje menší z počtov."
                v > best -> "Toľko detí sa nedá: pri každej možnosti nákupu je klobás alebo buřtov menej než §$v§."
                v < best && v >= least -> "Toľko detí byť môže, ale aj viac: možnosť " + sols.first { minOf(it.first, it.second) == best }.let { "§(${it.first},${it.second})§" } + " stačí pre §$best§."
                else -> "Pre každú možnosť §(x,y)§ s §x,y\\geq $least§ vezmi menší z počtov; najväčší z nich je §$best§."
            }
        }, aim = 2, hint = tx("Počet detí je najviac menší z počtov §x§, §y§. Vypíš všetky nákupy s §x,y\\geq $least§.")) }),
        postup = TcLdrNonnegPostup,
        solution = { _ ->
            TcLdrNonnegPostup.solve {
                val tt = if (a >= b) rea(a, b) else rea(b, a)
                allAim(tx("Nech §x§ je počet klobás a §y§ počet buřtov; celá suma dáva rovnicu §${a}x+${b}y=$c§."), "${a}x+${b}y=$c")
                allAim(tx("REA dá §\\gcd($a,$b)=$g§ a jedno riešenie. Ostatné sa líšia o riešenie homogénnej rovnice, preto sú to (Věta 6.8):"),
                    Formula(reaWalk(tt, swap = a < b), tx("Stĺpec §\\alpha§ patrí k §x§, §\\beta§ k §y§.")), Formula(bezoutTimes(a, b, c)), Formula("(x,y)=(${l.x0},${l.y0})+k\\cdot(${l.sx},${l.sy}),\\quad k\\in\\mathbb{Z}"))
                val kLo = (least - l.x0 + l.sx - 1).floorDiv(l.sx); val kHi = (l.y0 - least).floorDiv(-l.sy)
                cond(tx("Každé dieťa dostane oboje a detí je aspoň §$least§, preto §x,y\\geq $least§; to dá hranice pre §k§."),
                    Formula("\\begin{aligned}${l.x0}+${l.sx}k\\geq $least&\\ \\Leftrightarrow\\ k\\geq $kLo\\\\${l.y0}-${-l.sy}k\\geq $least&\\ \\Leftrightarrow\\ k\\leq $kHi\\end{aligned}"))
                pick(tx("Vyhovujúce nákupy a pri každom menší z počtov:"), sols.joinToString(",\\ ") { "(${it.first},${it.second})" })
                pick(tx("Detí môže byť najviac §$best§; každý počet od §$least§ po §$best§ ide, niektoré deti dostanú viac. Možné počty sú §\\{$least,\\dots,$best\\}§."))
            }
        },
        hint = tx("Zostav §${a}x+${b}y=$c§, nájdi nákupy s §x,y\\geq $least§ a pri každom menší z počtov."),
    )
}

private fun tcBuyCount(t: TcWordLdrTask): Exercise {
    val (kind, a, b, c, least) = t
    val g = gcd(a, b)
    val sols = tcWordSolutions(a, b, c, least)
    val l = ldr(a, b, c)
    val fewest = sols.minByOrNull { it.first + it.second }
    val (allAim, cond, pick) = TcLdrNonnegPostup
    val (xName, yName) = if (kind == 0) "klobás" to "buřtov" else "mincí po §$a§" to "mincí po §$b§"
    val story = if (kind == 0) "Jedna klobása stojí §$a§ Kč a jeden buřt §$b§ Kč. Kolika způsoby lze za přesně §$c§ Kč koupit klobásy a buřty" + (if (least > 0) ", aby byl na nákupu aspoň jeden kus od každého" else "") + "? Při kterém z nich se koupí nejméně kusů?"
        else "Zaplaťte přesně §$c§ piastrů mincemi v hodnotě §$a§ a §$b§ piastrů, bez vracení. Kolika způsoby to jde? Při kterém se použije nejméně mincí?"
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("Počet možností:"), nums(listOf("\\text{počet}"), listOf(sols.size.toLong()), "§${sols.size}§") { gv ->
        val v = gv[0]
        when {
            g > 1 && l != null && v == tcWordSolutionsStep(a, b, c, least) -> "Krok riešení je §(${b / g},-${a / g})§, nie §($b,-$a)§: koeficienty majú §\\gcd=$g§, treba ho vykrátiť (Věta 6.8)."
            least > 0 && v == tcWordSolutions(a, b, c, 0).size.toLong() && v != sols.size.toLong() -> "Zarátal si aj možnosti s nulou kusov. Zadanie chce aspoň jeden kus od každého."
            l == null -> "§\\gcd($a,$b)=$g§ nedelí §$c§, preto rovnica nemá ani celočíselné riešenie (Věta 6.7)."
            else -> "Všetky riešenia sú §x=${l.x0}+${l.sx}k§, §y=${l.y0}-${-l.sy}k§; podmienky §x,y\\geq $least§ pripustia §${sols.size}§ hodnôt §k§."
        }
    }, aim = 1, hint = tx("Rovnica §${a}x+${b}y=$c§; všetky celé riešenia, potom nerovnosti §x,y\\geq $least§ pre §k§.")) }
    if (fewest != null) parts += { _ -> Part(tx("Možnosť s najmenším počtom kusov spolu: §x§ $xName, §y§ $yName."), nums(listOf("x", "y"), listOf(fewest.first, fewest.second), "§(x,y)=(${fewest.first},${fewest.second})§") { gv ->
        val pair = gv[0] to gv[1]
        when {
            pair in sols -> "Je to možnosť, ale nie s najmenším súčtom §x+y§: §${pair.first + pair.second}>${fewest.first + fewest.second}§."
            gv[0] * a + gv[1] * b != c -> "Nezaplatí sa presne: §$a\\cdot ${p(gv[0])}+$b\\cdot ${p(gv[1])}=${gv[0] * a + gv[1] * b}\\neq $c§."
            else -> "Počty musia byť aspoň §$least§. Pri §x+y§ rozhodne, ktoré z dvoch je lacnejšie na kus: viac drahších znamená menej kusov."
        }
    }, aim = 2, hint = tx("Súčet §x+y§ sa s §k§ mení stále rovnakým smerom; najmenší je na kraji intervalu pre §k§.")) }
    return Exercise(
        "dml-tc-slovna-ldr", if (kind == 0) "Nákup za presnú sumu" else "Mince bez vracania", "Šablóna Cvičení 6.10 a 6.11; Příklad 6.6",
        tx(story),
        parts = parts,
        postup = TcLdrNonnegPostup,
        solution = { _ ->
            TcLdrNonnegPostup.solve {
                allAim(tx("Nech §x§ je počet " + (if (kind == 0) "klobás" else "mincí po §$a§") + " a §y§ počet " + (if (kind == 0) "buřtov" else "mincí po §$b§") + "; zaplatiť presne §$c§ znamená:"), "${a}x+${b}y=$c,\\qquad x,y\\geq $least")
                if (l == null) {
                    allAim(tx("§\\gcd($a,$b)=$g§ nedelí §$c§, preto rovnica nemá celočíselné riešenie (Věta 6.7), teda ani jedno z obchodu."))
                    pick(tx("Nedá sa to žiadnym spôsobom: §0§ možností."))
                } else {
                    val tt = if (a >= b) rea(a, b) else rea(b, a)
                    allAim(tx("REA dá §\\gcd($a,$b)=$g§ a jedno riešenie. Ostatné sa líšia o riešenie homogénnej rovnice, preto sú to §(x_0+k\\cdot ${l.sx},\\ y_0-k\\cdot ${-l.sy})§ (Věta 6.8)."),
                        Formula(reaWalk(tt, swap = a < b), tx("Stĺpec §\\alpha§ patrí k §x§, §\\beta§ k §y§.")), Formula(bezoutTimes(a, b, c)), Formula("(x,y)=(${l.x0},${l.y0})+k\\cdot(${l.sx},${l.sy}),\\quad k\\in\\mathbb{Z}"))
                    val kLo = (least - l.x0 + l.sx - 1).floorDiv(l.sx); val kHi = (l.y0 - least).floorDiv(-l.sy)
                    cond(tx("Oba počty musia byť aspoň §$least§; každá podmienka dá hranicu pre §k§."),
                        Formula("\\begin{aligned}${l.x0}+${l.sx}k\\geq $least&\\ \\Leftrightarrow\\ k\\geq $kLo\\\\${l.y0}-${-l.sy}k\\geq $least&\\ \\Leftrightarrow\\ k\\leq $kHi\\end{aligned}"))
                    if (sols.isEmpty()) pick(tx("Interval pre §k§ je prázdny, preto sa to nedá: §0§ možností."))
                    else {
                        pick(tx((if (sols.size == 1) "Vyhovuje " else "Vyhovujú ") + sk(sols.size.toLong(), "hodnota", "hodnoty", "hodnôt") + " §k§, teda " + sk(sols.size.toLong(), "možnosť", "možnosti", "možností") + ":"), sols.joinToString(",\\ ") { "(${it.first},${it.second})" })
                        pick(tx("Najmenej kusov spolu, §${fewest!!.first + fewest.second}§, má §(x,y)=(${fewest.first},${fewest.second})§."))
                    }
                }
            }
        },
        hint = tx("Zostav §${a}x+${b}y=$c§, nájdi všetky celé riešenia a z podmienok §x,y\\geq $least§ interval pre §k§."),
    )
}

/** gcd(a,b) = αa + βb from the REA, multiplied by c/gcd: the particular solution as the textbook gets it (Důsledek 6.1). */
private fun bezoutTimes(a: Long, b: Long, c: Long): String {
    val t = if (a >= b) rea(a, b) else rea(b, a)
    val (al, be) = if (a >= b) t.alpha to t.beta else t.beta to t.alpha
    val k = c / t.gcd
    return "${t.gcd}=${p(al)}\\cdot $a+${p(be)}\\cdot $b\\ \\Big|\\cdot $k\\ \\Rightarrow\\ (x_0,y_0)=(${al * k},${be * k})"
}

/** The count a student gets with the step (b, −a) not divided by the gcd: every g-th solution only. */
fun tcWordSolutionsStep(a: Long, b: Long, c: Long, least: Long): Long {
    val all = tcWordSolutions(a, b, c, least)
    if (all.isEmpty()) return -1
    return all.filterIndexed { i, _ -> i % gcd(a, b) == 0L }.size.toLong()
}

private fun tcHourglass(t: TcWordLdrTask): Exercise {
    val (_, a, b, tt) = t
    val g = gcd(a, b)
    val l = ldr(a, b, tt)
    val best = if (l != null) tcFewestTurns(a, b, tt) else null
    val (eq, all, fewest, how) = TcHourglassPostup
    val parts = ArrayList<(List<Any>) -> Part>()
    parts += { _ -> Part(tx("Dá sa namerať interval §$tt§ min?"), Answer.Choice(listOf("Áno", "Nie"), if (l != null) 0 else 1,
        tx(if (l != null) "Áno: §\\gcd($a,$b)=$g§ delí §$tt§, preto má §${a}x+${b}y=$tt§ celé riešenie (Věta 6.7)." else "Nie: §\\gcd($a,$b)=$g§ nedelí §$tt§. Každý nameraný čas je kombinácia §$a§ a §$b§, teda násobok §$g§."),
        mapOf((if (l != null) 1 else 0) to tx(if (l != null) "Dá sa: §\\gcd($a,$b)=$g§ delí §$tt§, rovnica §${a}x+${b}y=$tt§ má celé riešenie (Věta 6.7)." else "Nedá sa: každý nameraný čas je §${a}x+${b}y§, a to je násobok §$g§, kým §$tt§ nie je."))),
        aim = 0, hint = tx("Každý nameraný interval je §${a}x+${b}y§ s celými §x,y§. Kedy taký výraz môže dať §$tt§?")) }
    if (l != null && best != null) {
        val (bx, by) = best
        val time = if (bx >= 0 && by >= 0) tt else maxOf(abs(bx) * a, abs(by) * b)
        parts += { _ -> Part(tx("Riešenie §${a}x+${b}y=$tt§ s najmenším počtom otočení §|x|+|y|§:"), nums(listOf("x", "y"), listOf(bx, by), "§(x,y)=(${bx},${by})§") { gv ->
            val (x, y) = gv
            when {
                a * x + b * y != tt -> "Nie je to riešenie: §$a\\cdot ${p(x)}+$b\\cdot ${p(y)}=${a * x + b * y}\\neq $tt§."
                abs(x) + abs(y) > abs(bx) + abs(by) -> "Riešenie to je, ale s §${abs(x) + abs(y)}§ otočeniami; ide to s §${abs(bx) + abs(by)}§. Posuň §k§ v §(x_0+${b / g}k,\\ y_0-${a / g}k)§."
                else -> "Najmenej otočení dáva §(${bx},${by})§."
            }
        }, aim = 2, hint = tx("Všetky riešenia sú §(x_0+${b / g}k,\\ y_0-${a / g}k)§; skús §k§, pri ktorých je §x§ alebo §y§ blízko nuly.")) }
        parts += { _ -> Part(tx("Koľko minút trvá celé meranie týmto spôsobom?"), nums(listOf("\\text{min}"), listOf(time), "${sk(time, "minúta", "minúty", "minút")}") { gv ->
            val v = gv[0]
            when {
                v == tt && time != tt -> sk(tt, "minúta", "minúty", "minút") + " je nameraný interval, ale čakať treba, kým dobehne dlhšia strana: " + sk(time, "minúta", "minúty", "minút") + "."
                v == abs(bx) * a + abs(by) * b && v != time -> "Hodiny bežia naraz, nie za sebou. Celé meranie trvá ako dlhšia strana: " + sk(time, "minúta", "minúty", "minút") + "."
                else -> if (bx >= 0 && by >= 0) "Obe hodiny idú za sebou, spolu " + sk(tt, "minúta", "minúty", "minút") + "." else "Strany bežia naraz: §${abs(bx)}\\cdot $a=${abs(bx) * a}§ a §${abs(by)}\\cdot $b=${abs(by) * b}§; meranie trvá ako dlhšia, " + sk(time, "minúta", "minúty", "minút") + "."
            }
        }, aim = 3, hint = tx("Ak má §x§ alebo §y§ záporné znamienko, obe strany bežia naraz a interval je ich rozdiel.")) }
    }
    return Exercise(
        "dml-tc-slovna-ldr", "Presýpacie hodiny", "Šablóna Cvičení 6.9; proseminár 21/22 k 6. cvičeniu",
        tx("Máme dvoje přesýpací hodiny: na §$a§ minut a na §$b§ " + (if (b <= 4) "minuty" else "minut") + ". Hodiny vždy necháme přesypat celé a hned je otočíme znovu, nebo je odložíme; každé spuštění počítáme jako jedno otočení. Lze pomocí nich naměřit interval §$tt§ " + (if (tt in 2..4) "minuty" else "minut") + "? Pokud ano, popište všechny způsoby a vyberte ten, při kterém hodiny celkem otočíme nejméněkrát."),
        parts = parts,
        postup = TcHourglassPostup,
        solution = { _ ->
            TcHourglassPostup.solve {
                eq(tx("§x§ je počet behov §$a§-minútových hodín, §y§ počet behov §$b§-minútových. Kladné číslo: beh na dlhšej strane, záporné: na kratšej, od ktorej konca sa interval meria."), "${a}x+${b}y=$tt")
                if (l == null) {
                    eq(tx("§\\gcd($a,$b)=$g§ nedelí §$tt§, preto rovnica nemá celé riešenie (Věta 6.7)."))
                    how(tx(sk(tt, "minúta", "minúty", "minút") + " sa nedá namerať: každý nameraný čas je násobok §$g§."))
                } else {
                    val tw = if (a >= b) rea(a, b) else rea(b, a)
                    all(tx("REA dá §\\gcd($a,$b)=$g§ a jedno riešenie. Ostatné sa líšia o riešenie homogénnej rovnice, preto sú to (Věta 6.8):"),
                        Formula(reaWalk(tw, swap = a < b), tx("Stĺpec §\\alpha§ patrí k §x§, §\\beta§ k §y§.")), Formula(bezoutTimes(a, b, tt)), Formula("(x,y)=(${l.x0},${l.y0})+k\\cdot(${l.sx},${l.sy}),\\quad k\\in\\mathbb{Z}"))
                    val k0 = (-l.x0).floorDiv(l.sx)
                    val near = (k0 - 1..k0 + 2).map { k -> val x = l.x0 + k * l.sx; val y = l.y0 + k * l.sy; Triple(k, x, y) }
                    fewest(tx("Počet otočení je §|x|+|y|§. Najmenší je pri §k§, kde §x§ mení znamienko, preto skúsim susedné §k§:"),
                        Formula("\\begin{aligned}" + near.joinToString("\\\\") { (k, x, y) -> "k=$k&:\\ (${x},${y}),\\ |x|+|y|=${abs(x) + abs(y)}" } + "\\end{aligned}"))
                    val (bx, by) = best!!
                    if (bx >= 0 && by >= 0) how(tx("Najmenej otočení, §${bx + by}§, dáva §(${bx},${by})§: spusti §$a§-minútové §${bx}§-krát a potom §$b§-minútové §${by}§-krát za sebou, spolu " + sk(tt, "minúta", "minúty", "minút") + "."))
                    else {
                        val (longN, longM, shortN, shortM) = if (bx > 0) listOf(bx, a, -by, b) else listOf(by, b, -bx, a)
                        how(tx("Najmenej otočení, §${abs(bx) + abs(by)}§, dáva §(${bx},${by})§. Obe hodiny spustím naraz: §$longM§-minútové bežia §${longN}§-krát, §$shortM§-minútové §${shortN}§-krát. Interval začne, keď dobehnú kratšie (§${shortN * shortM}§ min), a skončí s dlhšími (§${longN * longM}§ min)."))
                    }
                }
            }
        },
        hint = tx("Rovnica §${a}x+${b}y=$tt§, všetky jej celé riešenia, a z nich to s najmenším §|x|+|y|§."),
    )
}

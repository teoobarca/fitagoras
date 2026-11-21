package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.screens.dml.MiCoins
import dev.hamper.tabula.screens.dml.MiDominoes
import dev.hamper.tabula.screens.dml.MiFlaw
import dev.hamper.tabula.screens.dml.MiPowers
import dev.hamper.tabula.screens.dml.MiTable

/*
 * BI-DML, Matematická indukcia (kapitola 4 skrípt, cvičenia 4.x): slabá a silná indukcia, sumy, nerovnosti, deliteľnosť, mocniny matice, rekurentné postupnosti.
 * Owned by one author: everything of this topic lives in this file (or in files next to it named Induction*.kt), every id
 * starts with "dml-mi-". Registered in data/Dml.kt (DmlTopics). Rules: docs/CONTENT.md.
 *
 * Numbering of the textbook per kind within chapter 4 (chap-04 + cv-04): Definice 4.8 rekurentní posloupnost, Věta 4.1
 * slabý princip, Věta 4.2 silný princip, Příklad 4.9 součet, Příklad 4.15 mince, Poznámka 4.9 formule, Poznámka 4.12
 * "nejen indukce", Varování 4.3 úskalí, Varování 4.4 indukce naruby, Cvičení 4.1 až 4.18. Lecturer remarks are from the
 * captions of BI-DML 4. přednáška, paralelka 2 (Spěvák, 14. 10. 2026) and paralelka 1 (Dombek, 19. 10. 2026).
 */

private const val Dombek = "Dombek, prednáška 19. 10. 2026"

/* ---- Definitions and theorems ---------------------------------------------------------------------------------- */

private val rekurentna = Concept(
    id = "dml-mi-rekurentna", name = "Rekurentne zadaná postupnosť", src = "Skriptá, Definice 4.8",
    czech = listOf(
        p("""Řekneme, že nekonečná posloupnost čísel §(a_1,a_2,a_3,a_4,\dots)§ je **definována rekurentně** (případně **rekurzivně**, či **induktivně**), pokud je zadána následovně:"""),
        i("""Několik prvních hodnot (nezáleží zda pouze §a_1§, nebo více) je explicitně zadáno, je známa jejich hodnota."""),
        i("""Pro každý další člen posloupnosti §a_n§ existuje tzv. rekurentní předpis, tj. pravidlo, které nám umožňuje §a_n§ určit ze znalosti předchozích členů."""),
        p("Snadno nahlédneme, že pro úlohu napočítávat členy rekurentní posloupnosti se přirozeně nabízí právě rekurzivní algoritmy."),
    ),
    why = listOf(
        w("Několik prvních hodnot je explicitně zadáno", "Bez nich predpis nemá z čoho začať. Koľko ich je, toľko hodnôt overíš v základnom kroku silnej indukcie."),
        w("rekurentní předpis", "Pravidlo, ktoré z predošlých členov vypočíta ďalší. Na koľko členov späť siaha, toľko predošlých hodnôt potrebuje indukčný predpoklad."),
        w("ze znalosti předchozích členů", "Len predošlých, nie budúcich. Preto sa členy dajú počítať postupne a indukcia o nich vie niečo dokázať."),
    ),
    judge = listOf(
        j("""§f_0=0,\ f_1=1,\ f_n=f_{n-1}+f_{n-2}§ pre §n\geq 2§""", true, "Áno, dve prvé hodnoty sú dané a ďalší člen je súčet dvoch predošlých. To je Fibonacciho postupnosť (Definice 4.9).",
            "Je to rekurentná postupnosť: dané §f_0,f_1§ a predpis z predošlých členov, presne ako v Definícii 4.8."),
        j("""§A_{n+2}=A_{n+1}+6A_n§ pre §n\in\mathbb{N}§, nič viac""", false, "Nie, chýbajú prvé hodnoty §A_1,A_2§. Samotný predpis postupnosť neurčí.",
            "Predpis tu je, ale spĺňa ho nekonečne veľa postupností. Definícia chce aj prvé hodnoty."),
        j("""§0!=1,\ n!=n\cdot(n-1)!§ pre §n\geq 1§""", true, "Áno, §0!§ je dané a každý ďalší člen sa počíta z predošlého (Definice 4.9).",
            "Faktoriál skriptá takto definujú rekurzívne: hodnota §0!=1§ a predpis z predošlého člena."),
        j("""§a_n=2^n§ pre každé §n\in\mathbb{N}§""", false, "Nie, to je priamy vzorec. Člen sa nepočíta z predošlých.",
            """Tú istú postupnosť možno zadať aj rekurentne, §a_1=2,\ a_{n+1}=2a_n§. Takto zapísaná je však zadaná priamo."""),
    ),
    units = listOf("Prvé hodnoty sú dané", "Predpis pre každý ďalší člen", "Predpis používa len predošlé členy", "Ide o nekonečnú postupnosť čísel"),
    ask = "Definujte rekurentně zadanou posloupnost.",
    question = "Je to rekurentne zadaná postupnosť?",
    short = "rekurentnej postupnosti",
)

private val slabyCz = listOf(
    p("""Nechť §n_0\in\mathbb{Z}§ a budiž §V(n)§ vlastnost celých čísel, která má smysl pro všechna §n\geq n_0§. Předpokládejme, že následující předpoklady jsou splněny:"""),
    i("""**Základní krok (ZK)**: §V(n_0)§ platí."""),
    i("""**Indukční krok (IK)**: Pro každé §n\in\mathbb{Z},\ n\geq n_0§ je pravdivá implikace: „Jestliže platí §V(n)§, pak platí i §V(n+1)§.“"""),
    p("""Potom dle **slabého principu matematické indukce** (nebo jen **slabé indukce**) platí §V(n)§ pro všechna §n\in\mathbb{Z},\ n\geq n_0§."""),
    p("""Výrok §V(n)§ v implikaci z indukčního kroku (§V(n)\Rightarrow V(n+1)§) nazýváme **indukční předpoklad**."""),
)

private val silnyCz = listOf(
    p("""Nechť §n_0\in\mathbb{Z}§ a budiž §V(n)§ vlastnost celých čísel, která má smysl pro všechna §n\geq n_0§. Nechť §m\in\mathbb{N}_0§. Předpokládejme, že následující předpoklady jsou splněny:"""),
    i("""**Základní krok (ZK)**: §V(n_0),V(n_0+1),V(n_0+2),\ldots,V(n_0+m)§ platí."""),
    i("""**Indukční krok (IK)**: Pro každé §n\in\mathbb{Z},\ n\geq n_0+m§ je pravdivá implikace: „Jestliže platí §V(k)§ ==pro všechna== §k\in\{n_0,n_0+1,\ldots,n\}§, pak platí i §V(n+1)§.“"""),
    p("""Potom dle **silného principu matematické indukce** (nebo jen **silné indukce**) platí §V(n)§ pro všechna §n\in\mathbb{Z},\ n\geq n_0§."""),
    p("""Výrok §V(n_0)\wedge V(n_0+1)\wedge\cdots\wedge V(n)§ na levé straně implikace z indukčního kroku nazýváme **indukční předpoklad**."""),
)

private val slaby = Theorem(
    "dml-mi-slaby", "Slabý princíp matematickej indukcie", "Skriptá, Věta 4.1",
    wording(slabyCz.take(1), slabyCz.subList(1, 4), core = """\begin{aligned}&\text{ZK: }V(n_0),\\&\text{IK: }(\forall n\geq n_0)\\&\qquad(V(n)\Rightarrow V(n+1)),\\&\text{potom }(\forall n\geq n_0)V(n)\end{aligned}""", rest = listOf(slabyCz[4])),
    idea = m("""Ak platí §V(n_0)§ a z každého §V(n)§ plynie §V(n+1)§, platí §V(n)§ pre všetky §n\geq n_0§."""),
    note = m("Pri princípe napíš aj záver, nielen dva kroky. Dombek: bez záveru je to „seznam úkolů, ale ne tu odměnu“."),
    proven = noProof("Skriptá tento princíp uvádzajú bez dôkazu a na prednáške sa nedokazuje. Dombek: „to se nedokazujeme, to je tak nějak jasně vidět, že to jde použít“.", "Skriptá, Věta 4.1; $Dombek"),
    short = "o slabej indukcii",
)

private val silny = Theorem(
    "dml-mi-silny", "Silný princíp matematickej indukcie", "Skriptá, Věta 4.2",
    wording(silnyCz.take(1), silnyCz.subList(1, 4), core = """\begin{aligned}&\text{ZK: }V(n_0),\dots,V(n_0+m),\\&\text{IK: }(\forall n\geq n_0+m)\\&\quad V(n_0)\wedge\dots\wedge V(n)\\&\qquad\Rightarrow V(n+1),\\&\text{potom }(\forall n\geq n_0)V(n)\end{aligned}""", rest = listOf(silnyCz[4])),
    idea = m("""Ak platia prvé hodnoty a z §V(n_0),\dots,V(n)§ plynie §V(n+1)§, platí §V(n)§ pre všetky §n\geq n_0§."""),
    note = m("Na písomke: rekurencie, kde člen závisí od dvoch predošlých (2022, 2023), a postupnosť z písomky 5. 12. 2025 (variant K)."),
    proven = noProof("Skriptá tento princíp uvádzajú bez dôkazu.", "Skriptá, Věta 4.2"),
    short = "o silnej indukcii",
)

/* ---- Proofs, line by line ----------------------------------------------------------------------------------------- */

private const val ForAll = "To je celé tvrdenie, ktoré máš dokázať. Dombek: „nikdy neříkejte věci, jakože předpokládáme, že tvrzení §V(n)§ platí pro každé §n§“."
private const val Circle = "To máš dokázať, nie predpokladať. Predpokladať cieľ je dôkaz kruhom."

private fun checklist(vararg s: String) = s.map(::m)

private val pSuma = Proof(
    "dml-mi-p-suma", "Súčet nepárnych čísel", "Skriptá, Cvičení 4.4 a)",
    wording(emptyList(), listOf(p("""Matematickou indukcí dokažte platnost následujících vztahů pro všechna §n\in\mathbb{N}§. a) §§1+3+5+\cdots+(2n-1)=n^2§§.""")), core = """\forall n\in\mathbb{N}:\ \softbreak\textstyle\sum_{k=1}^n(2k-1)=n^2"""),
    null,
    m("""Dokazujem §V(n)§: §\sum_{k=1}^{n}(2k-1)=n^2§ pre všetky §n\in\mathbb{N}§, slabou indukciou."""),
    listOf(
        r("Prvý riadok: základný krok.", 0,
            c("""ZK, §n=1§: ľavá strana §1§, pravá §1^2=1§, rovnajú sa."""),
            c("ZK: pre §n=1§ tvrdenie zrejme platí.", "Treba ukázať výpočet oboch strán. Slovo „zrejme“ nie je overenie."),
            c("""ZK, §n=0§: §0=0§.""", "Tvrdenie je pre §n\\in\\mathbb{N}§, začína sa pri §n=1§.")),
        r("Indukčný krok: čo predpokladáš?", 1,
            c("""Predpokladajme, že §\sum_{k=1}^{n}(2k-1)=n^2§ pre všetky §n\in\mathbb{N}§.""", ForAll),
            c("""Nech §n\geq 1§ je pevné ľubovoľné a §\sum_{k=1}^{n}(2k-1)=n^2§ (IP)."""),
            c("""Predpokladajme, že §\sum_{k=1}^{n+1}(2k-1)=(n+1)^2§.""", Circle)),
        r("Ako začneš počítať?", 2,
            c("""§\sum_{k=1}^{n+1}(2k-1)=(n+1)^2§, upravím obe strany až na §0=0§.""", "To je indukcia naruby. Z dokazovaného odvodíš pravdu, a to nič nedokazuje (Varování 4.4)."),
            c("""§\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n-1)\stackrel{IP}{=}n^2+2n-1§""", "Pribudne člen pre §k=n+1§, teda §2(n+1)-1=2n+1§, nie §2n-1§."),
            c("""§\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n+1)\stackrel{IP}{=}n^2+2n+1§""")),
        r("Dokonči krok.", 0,
            c("""§n^2+2n+1=(n+1)^2§, a to je pravá strana §V(n+1)§."""),
            c("""Vyšlo §n^2+2n+1§, krok je hotový.""", "Chýba posledný krok: ukázať, že to je pravá strana §V(n+1)§, teda §(n+1)^2§.")),
    ),
    m("""Záver: podľa slabého princípu MI platí §1+3+\dots+(2n-1)=n^2§ pre všetky §n\in\mathbb{N}§."""),
    checklist("ZK s výpočtom oboch strán", "IP pre pevné ľubovoľné §n§, nie pre všetky", "Súčet do §n+1§ rozdelený, rovnosť s IP označená", "Úprava až na pravú stranu §V(n+1)§", "Záver s odkazom na princíp MI"),
    minutes = 8,
    goals = listOf("Základný krok", "Indukčný predpoklad", "Rozklad súčtu a IP", "Úprava na §(n+1)^2§"),
)

private val pDel = Proof(
    "dml-mi-p-del", "Deliteľnosť piatimi", "Skriptá, Cvičení 4.1 b)",
    wording(emptyList(), listOf(p("""Pomocí matematické indukce dokažte platnost následujících vztahů: b) §11^n-6§ je dělitelné §5§ pro §\forall n\in\mathbb{N}§.""")), core = """(\forall n\in\mathbb{N})\;5\mid(11^n-6)"""),
    null,
    m("""ZK, §n=1§: §11^1-6=5§, deliteľné §5§."""),
    listOf(
        r("Indukčný predpoklad.", 2,
            c("""Nech §11^n-6=5k§ pre nejaké §k\in\mathbb{Z}§ a všetky §n\in\mathbb{N}§.""", "To je celé tvrdenie, a navyše s jedným §k§ pre všetky §n§. IP je pre jedno pevné §n§."),
            c("""Nech §5\mid 11^{n+1}-6§.""", Circle),
            c("""Nech §n\geq 1§ je pevné a §11^n-6=5k§ pre nejaké §k\in\mathbb{Z}§ (IP).""")),
        r("Rozlož výraz pre §n+1§.", 0,
            c("""§11^{n+1}-6=11\cdot 11^n-6=(11^n-6)+10\cdot 11^n§"""),
            c("""§11^{n+1}-6=11\cdot(11^n-6)§""", "Nesedí, lebo §11(11^n-6)=11^{n+1}-66§, nie §11^{n+1}-6§."),
            c("""§11^{n+1}-6=(11^n-6)+11§""", "§11^{n+1}§ nie je §11^n+11§. Exponent o jedna vyšší znamená krát §11§.")),
        r("Použi IP.", 1,
            c("""§10\cdot 11^n§ je deliteľné §5§, teda celý výraz tiež.""", "Chýba IP. Že §11^n-6§ je deliteľné §5§, vieš len z IP, a treba to napísať."),
            c("""§\stackrel{IP}{=}5k+10\cdot 11^n=5(k+2\cdot 11^n)§, a to je násobok §5§."""),
            c("""§11^n-6§ je deliteľné §5§, lebo to tvrdíme.""", "„Lebo to tvrdíme“ je kruh. Deliteľnosť §11^n-6§ vieš z IP, len pre toto pevné §n§.")),
    ),
    m("""Záver: podľa slabého princípu MI je §11^n-6§ deliteľné §5§ pre všetky §n\in\mathbb{N}§."""),
    checklist("ZK s hodnotou pre §n=1§", "IP cez §11^n-6=5k§ pre pevné §n§", "Rozklad §11^{n+1}-6=(11^n-6)+10\\cdot 11^n§", "IP vyznačený, výsledok §5(\\dots)§", "Záver"),
    minutes = 8,
    goals = listOf("Indukčný predpoklad", "Rozklad §11^{n+1}-6§", "Použitie IP"),
)

private val pNerov = Proof(
    "dml-mi-p-nerov", "Nerovnosť od štvrtého člena", "Skriptá, Cvičení 4.3 d)",
    wording(emptyList(), listOf(p("""Matematickou indukcí dokažte platnost následujících vztahů pro všechna §n\in\mathbb{N}§. d) §§n^2\leq 2^n§§, pro §n\geq 4§.""")), core = """(\forall n\geq4)\;n^2\leq2^n"""),
    null,
    m("""Pre §n=3§ nerovnosť neplatí, §9>8§. Dokazujeme ju preto od §n=4§."""),
    listOf(
        r("Základný krok.", 1,
            c("""ZK, §n=1§: §1\leq 2§.""", "Tvrdenie je od §n=4§. Krok od §n=1§ by musel prejsť cez §n=3§, kde neplatí."),
            c("""ZK, §n=4§: §4^2=16\leq 16=2^4§."""),
            c("""ZK, §n=5§: §25\leq 32§.""", "Začni tam, kde tvrdenie začína, pri §n=4§. Inak §V(4)§ ostane neoverené.")),
        r("Indukčný predpoklad.", 0,
            c("""Nech §n\geq 4§ je pevné a §n^2\leq 2^n§ (IP)."""),
            c("""Nech §n\geq 1§ je pevné a §n^2\leq 2^n§.""", "Krok potrebuje §n\\geq 4§, lebo využije §2n+1\\leq n^2§. Pre §n=1,2§ to neplatí."),
            c("""Nech §(n+1)^2\leq 2^{n+1}§.""", Circle)),
        r("Reťaz odhadov.", 2,
            c("""§(n+1)^2=n^2+2n+1\stackrel{IP}{=}2^n+2n+1§""", "IP je nerovnosť, nie rovnosť. Za §n^2§ sa §2^n§ dosadiť nedá (Varování 4.4)."),
            c("""§(n+1)^2\leq 2^{n+1}§, lebo §n^2\leq 2^n§.""", "To je tvrdenie bez odvodenia. Chýba výpočet s §2n+1\\leq n^2§."),
            c("""§(n+1)^2=n^2+2n+1\leq n^2+n^2=2n^2\stackrel{IP}{\leq}2\cdot 2^n=2^{n+1}§""")),
        r("Prečo §2n+1\\leq n^2§?", 0,
            c("""Pre §n\geq 4§ je §n^2-2n-1=(n-1)^2-2\geq 9-2>0§."""),
            c("Lebo §2n+1§ je nepárne.", "Parita s nerovnosťou nesúvisí."),
            c("""Pre §n=4§ platí §9\leq 16§, teda vždy.""", "Jedna hodnota nestačí. Treba to pre každé §n\\geq 4§.")),
    ),
    m("""Záver: podľa slabého princípu MI platí §n^2\leq 2^n§ pre všetky §n\geq 4§."""),
    checklist("ZK pri §n=4§", "IP pre pevné §n\\geq 4§", "Reťaz od §(n+1)^2§ jedným smerom, IP ako §\\stackrel{IP}{\\leq}§", "Zdôvodnený odhad §2n+1\\leq n^2§", "Záver od §n=4§"),
    minutes = 10,
    goals = listOf("Základný krok §n=4§", "Indukčný predpoklad", "Reťaz odhadov", """Odhad §2n+1\leq n^2§"""),
)

private val pRek = Proof(
    "dml-mi-p-rek", "Rekurencia silnou indukciou", "Písomka 6. 12. 2023 (variant A), príklad 1",
    wording(listOf(p("""Mějme nekonečnou posloupnost §(A_1,A_2,A_3,\dots)§, jejíž členy pro každé §n\in\mathbb{N}§ splňují vztah §A_{n+2}=A_{n+1}+6A_n§ a dále platí §A_1=-1,A_2=7§.""")), listOf(p("""Dokažte matematickou indukcí, že pro každé §n\in\mathbb{N}§ platí §§A_n=3^{n-1}+(-2)^n§§.""")), core = """\forall n\in\mathbb{N}:\quad\softbreak A_n=3^{n-1}+(-2)^n"""),
    null,
    m("""Silná indukcia, lebo §A_{n+1}§ závisí od dvoch predošlých členov."""),
    listOf(
        r("Základný krok.", 1,
            c("""§n=1§: §§3^0+(-2)^1=-1=A_1§§ To stačí.""", "Krok pre §A_2§ by potreboval §A_0§, ktoré nie je. Treba overiť aj §n=2§."),
            c("""§n=1§ aj §n=2§: §§\begin{aligned}3^0+(-2)^1&=-1=A_1,\\3^1+(-2)^2&=7=A_2\end{aligned}§§"""),
            c("""§n=1,2,3§, lebo bez §A_3§ krok nezačne.""", "Netreba, §A_3§ dá krok pre §n=2§ z §A_1§ a §A_2§. Overiť ho navyše nie je chyba, ale krok ho nepotrebuje.")),
        r("Indukčný predpoklad.", 2,
            c("""Nech §n\geq 1§ je pevné a §A_n=3^{n-1}+(-2)^n§.""", "Slabý IP hovorí len o §A_n§. Krok potrebuje aj §A_{n-1}§."),
            c("""Nech §A_k=3^{k-1}+(-2)^k§ pre všetky §k\in\mathbb{N}§.""", ForAll),
            c("""Nech §n\geq 2§ je pevné a §A_k=3^{k-1}+(-2)^k§ pre všetky §k\in\{1,\dots,n\}§ (IP).""")),
        r("Rozpíš §A_{n+1}§.", 0,
            c("""§A_{n+1}=A_n+6A_{n-1}\stackrel{IP}{=}3^{n-1}+(-2)^n+6\cdot 3^{n-2}+6\cdot(-2)^{n-1}§"""),
            c("""§A_{n+1}=A_n+6A_{n-1}\stackrel{IP}{=}3^n+(-2)^{n+1}§""", "To je cieľ, nie výpočet. Z IP dosadíš za §A_n§ a §A_{n-1}§."),
            c("""§A_{n+1}=A_{n+2}-6A_n§""", "Ide to dopredu na §A_{n+2}§, o ktorom IP nič nehovorí.")),
        r("Uprav.", 1,
            c("""§=3^{n-2}\cdot 9+(-2)^{n-1}\cdot 4=3^n-2^{n+1}§""", "Znamienko sa stratilo. §4=(-2)^2§, teda §(-2)^{n-1}\\cdot 4=(-2)^{n+1}§."),
            c("""§=3^{n-2}(3+6)+(-2)^{n-1}(-2+6)=3^n+(-2)^{n+1}§""")),
    ),
    m("""Záver: podľa silného princípu MI platí §A_n=3^{n-1}+(-2)^n§ pre všetky §n\in\mathbb{N}§."""),
    checklist("ZK pre §n=1§ aj §n=2§", "IP pre všetky §k\\in\\{1,\\dots,n\\}§, §n\\geq 2§", "Rekurencia a IP pre §A_n§ aj §A_{n-1}§", "Korektná úprava na §3^n+(-2)^{n+1}§", "Záver so silným princípom"),
    minutes = 12,
    goals = listOf("Základný krok §n=1,2§", "Silný indukčný predpoklad", "Rozpis §A_{n+1}§", "Úprava na §3^n+(-2)^{n+1}§"),
)

private val pMatice = Proof(
    "dml-mi-p-matice", "Mocnina matice z písomky 2025", "Písomka 4. 12. 2025 (variant E), príklad 1",
    wording(listOf(p("""Uvažujme matici §\mathbf{A}=\begin{pmatrix}-1&3\\0&-1\end{pmatrix}\in\mathbb{R}^{2,2}§.""")), listOf(p("""Výpočtem pro několik prvních hodnot §n\in\mathbb{N}§ odhadněte, čemu se rovná §\mathbf{A}^n§ pro obecné §n\in\mathbb{N}§ a tento svůj odhad dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad.""")), core = """\forall n\in\mathbb{N}:\quad\softbreak\mathbf{A}^n=\softbreak[1]\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}"""),
    null,
    m("""§A^2=\begin{pmatrix}1&-6\\0&1\end{pmatrix}§, §A^3=\begin{pmatrix}-1&9\\0&-1\end{pmatrix}§, §A^4=\begin{pmatrix}1&-12\\0&1\end{pmatrix}§."""),
    listOf(
        r("Odhad.", 0,
            c("""§A^n=\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}§"""),
            c("""§A^n=\begin{pmatrix}(-1)^n&3n\\0&(-1)^n\end{pmatrix}§""", "Pre §n=2§ nesedí: vpravo hore je §-6§, nie §6§."),
            c("""§A^n=\begin{pmatrix}(-1)^n&3^n\\0&(-1)^n\end{pmatrix}§""", "Matica sa neumocňuje po prvkoch. §A^2§ má vpravo hore §-6§, nie §9§.")),
        r("Základný krok.", 2,
            c("ZK netreba, §A^2§ a §A^3§ sme spočítali.", "Chýba overenie pre §n=1§, kde indukcia začína. §A^2§ a §A^3§ overia len §n=2,3§ (Varování 4.3)."),
            c("""§n=0§: §A^0=E§, jednotková matica.""", "Odhad je pre §n\\in\\mathbb{N}§, začína sa pri §n=1§."),
            c("""§n=1§: §\begin{pmatrix}-1&3\cdot 1\cdot(-1)^0\\0&-1\end{pmatrix}=A§.""")),
        r("Indukčný predpoklad.", 1,
            c("Nech odhad platí pre všetky §n\\in\\mathbb{N}§.", ForAll),
            c("""Nech §n\in\mathbb{N}§ je pevné a §A^n=\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}§ (IP)."""),
            c("""Nech §A^{n+1}=\begin{pmatrix}(-1)^{n+1}&3(n+1)(-1)^n\\0&(-1)^{n+1}\end{pmatrix}§.""", Circle)),
        r("Indukčný krok.", 0,
            c("""§A^{n+1}=A^n\cdot A\stackrel{IP}{=}\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}\begin{pmatrix}-1&3\\0&-1\end{pmatrix}=\begin{pmatrix}(-1)^{n+1}&3(-1)^n+3n(-1)^n\\0&(-1)^{n+1}\end{pmatrix}§"""),
            c("""§A^{n+1}=A^n+A\stackrel{IP}{=}\begin{pmatrix}(-1)^n-1&3n(-1)^{n-1}+3\\0&(-1)^n-1\end{pmatrix}§""", "Mocnina je súčin, nie súčet. §A^{n+1}=A^n\\cdot A§."),
            c("""§A^{n+1}=A^n\cdot A\stackrel{IP}{=}\begin{pmatrix}(-1)^{n+1}&3(-1)^n-3n(-1)^n\\0&(-1)^{n+1}\end{pmatrix}§""", "Znamienko vpravo hore: §3n(-1)^{n-1}\\cdot(-1)=3n(-1)^n§, mínus sa pohltí v mocnine.")),
        r("Dokonči.", 1,
            c("""§3(-1)^n+3n(-1)^n=3n(-1)^n§""", "Sčítanec §3(-1)^n§ sa stratil."),
            c("""§3(-1)^n+3n(-1)^n=3(n+1)(-1)^n§, a to je odhad pre §n+1§.""")),
    ),
    m("""Záver: podľa slabého princípu MI platí §A^n=\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}§ pre všetky §n\in\mathbb{N}§."""),
    checklist("Niekoľko mocnín a odhad", "ZK pre §n=1§", "IP pre pevné §n§ a cieľ §V(n+1)§", "§A^{n+1}=A^n\\cdot A§ s vyznačeným IP", "Úprava na odhad pre §n+1§ a záver"),
    minutes = 15,
    goals = listOf("Odhad §A^n§", "Základný krok", "Indukčný predpoklad", """Súčin §A^n\cdot A§""", "Úprava na odhad pre §n+1§"),
)

private val pPostupnost = Proof(
    "dml-mi-p-postupnost", "Kedy je postupnosť pod štyrmi", "Písomka 5. 12. 2025 (variant K), príklad 1",
    wording(listOf(p("""Mějme posloupnost reálných čísel definovanou předpisem §x_1=5§, §x_2=5§ a §x_{n+2}=\sqrt{1+2x_n+x_{n+1}}§ pro všechna §n\in\mathbb{N}§.""")), listOf(p("""Zjistěte, pro která §n\in\mathbb{N}§ platí §x_n<4§. Své tvrzení dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad.""")), core = """x_n<4\ \Leftrightarrow\ n\geq4"""),
    null,
    m("""Prvé členy: §§\begin{aligned}x_3&=\sqrt{1+10+5}=4,\\x_4&=\sqrt{1+10+4}=\sqrt{15}<4\end{aligned}§§ Odhad: §x_n<4§ práve pre §n\geq 4§."""),
    listOf(
        r("Ktoré hodnoty overíš v ZK?", 0,
            c("""§§\begin{aligned}x_4&=\sqrt{15}<4,\\x_5&=\sqrt{1+8+\sqrt{15}}\\&<\sqrt{13}<4\end{aligned}§§"""),
            c("""Len §x_4<4§.""", "Krok siaha na dva predošlé členy. Pre §x_5§ by potreboval §x_3<4§, a §x_3=4§."),
            c("""§x_1§, §x_2§ a §x_3§.""", "Tie sú §\\geq 4§ a tvrdenie je pre §n\\geq 4§. ZK začína pri §n=4§.")),
        r("Indukčný predpoklad.", 1,
            c("""Nech §n\geq 4§ je pevné a §x_k<4§ pre všetky §k\in\{4,\dots,n\}§.""", "Pre §n=4§ by krok potreboval §x_3<4§, a §x_3=4§. Krok začína pri §n=5§."),
            c("""Nech §n\geq 5§ je pevné a §x_k<4§ pre všetky §k\in\{4,\dots,n\}§ (IP)."""),
            c("""Nech §x_n<4§ pre všetky §n\geq 4§.""", ForAll)),
        r("Indukčný krok.", 2,
            c("""§x_{n+1}=\sqrt{1+2x_{n-1}+x_n}\stackrel{IP}{=}\sqrt{1+8+4}=\sqrt{13}§""", "IP je nerovnosť §x_k<4§, nie rovnosť. Dosadiť sa nedá."),
            c("""§x_{n+1}<4§, lebo postupnosť klesá.""", "Pokles si nedokázal. Spolu s IP by stačil, ale bez dôkazu nič neznamená."),
            c("""§x_{n+1}=\sqrt{1+2x_{n-1}+x_n}\stackrel{IP}{<}\sqrt{1+2\cdot 4+4}=\sqrt{13}<4§""")),
        r("Prečo smieš odhadovať pod odmocninou?", 0,
            c("Odmocnina je rastúca a výraz pod ňou je kladný."),
            c("""Lebo §\sqrt{a+b}=\sqrt{a}+\sqrt{b}§.""", "Táto rovnosť neplatí, napr. §\\sqrt{9+16}=5\\neq 7§."),
            c("Netreba to zdôvodňovať.", "Opravujúci chce každý logický krok. Bez rastúcej odmocniny z §a<b§ neplynie §\\sqrt{a}<\\sqrt{b}§.")),
    ),
    m("""Záver: podľa silného princípu MI je §x_n<4§ pre všetky §n\geq 4§. Pre §n=1,2,3§ je §x_n\geq 4§ výpočtom. Odpoveď: práve pre §n\geq 4§."""),
    checklist("Prvé členy a odpoveď §n\\geq 4§", "ZK pre §n=4§ a §n=5§", "IP pre §k\\in\\{4,\\dots,n\\}§, §n\\geq 5§", "Odhad §\\sqrt{13}<4§ s vyznačeným IP", "Rastúca odmocnina a záver aj pre §n=1,2,3§"),
    minutes = 15,
    goals = listOf("Základný krok §n=4,5§", "Indukčný predpoklad od §n=5§", "Odhad §x_{n+1}<4§", "Rastúca odmocnina"),
)

/* ---- Lessons ------------------------------------------------------------------------------------------------------ */

/** §1+2+\dots+n§ of Příklad 4.9 as a member of the generator's family of sums. */
private val sumOfK = miLinSumOf(1, 0)!!

private val slaba = lesson("dml-mi-slaba", "Slabá indukcia", 14, "Dva kroky, ktoré dokážu tvrdenie pre nekonečne veľa §n§ naraz.", art = 2, topics = listOf("MI1", "MI2")) {
    ask("""Overíš §V(1)§ a pre každé §n\geq 1§ dokážeš §V(n)\Rightarrow V(n+1)§. Platí §V(1000)§?""", "Áno", "Nie, overili sme len §V(1)§", "Len ak overíme aj §V(1000)§", correct = 0,
        after = """Áno, reťaz implikácií dovedie z §V(1)§ až k §V(1000)§. Každý jej krok je tá istá dokázaná implikácia.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Overili sme aj každý krok o jedna ďalej. Z §V(1)§ a §V(1)\Rightarrow V(2)§ plynie §V(2)§, a tak ďalej až po §V(1000)§.""",
            2 to """Netreba, dovedie ťa tam reťaz krokov. Práve na to je indukčný krok."""))
    explore("Zhoď všetky kocky.", "Ťukni na kocku pre základný krok a zapni indukčný krok.", MiDominoes(start = 1),
        "Základný krok a indukčný krok spolu zhodia všetky kocky.", "Jeden bez druhého nestačí: bez základného kroku nespadne nič, bez indukčného len prvá kocka.")
    explain("Slabý princíp: dva kroky, jeden záver.",
        """**ZK:** overíš §V(n_0)§. **IK:** pre každé §n\geq n_0§ dokážeš implikáciu §V(n)\Rightarrow V(n+1)§. Potom platí §V(n)§ pre všetky §n\geq n_0§. Predpoklad §V(n)§ v kroku je indukčný predpoklad (IP).""",
        cards = listOf("dml-mi-slaby"))
    clip("dml-mi-schody")
    check("Čo dokazuješ v indukčnom kroku?", """Implikáciu §V(n)\Rightarrow V(n+1)§ pre každé §n\geq n_0§""", """Výrok §V(n)§ pre každé §n\geq n_0§""", """Výrok §V(n+1)§""", correct = 0,
        why = """Implikáciu, nie samotné §V(n)§. Skriptá: „Nedokazujeme samotné §V(n)§, ale jen §V(n_0)§ a poté implikaci §V(n)\Rightarrow V(n+1)§.“""",
        wrong = mapOf(1 to "To je záver, ktorý plynie z oboch krokov. Keby si ho vedel dokázať priamo, indukciu nepotrebuješ.",
            2 to """Samo §V(n+1)§ nevieš, vieš ho len za predpokladu §V(n)§. Krok je implikácia."""))
    clip("dml-mi-implikacia")
    // the generator's own solution for a = 1, b = 0, so the worked example and practice cannot drift apart (Zásada 13)
    worked("Súčet §1+2+\\dots+n§", """Dokážte, že §${miSumClaim(sumOfK)}§ pre všetky §n\in\mathbb{N}§.""", "Skriptá, Příklad 4.9", miSumSolution(sumOfK))
    check("""Ktorý člen pribudne v §\sum_{k=1}^{n+1}k§ oproti §\sum_{k=1}^{n}k§?""", "§n+1§", "§n§", """§\frac{(n+1)(n+2)}{2}§""", correct = 0,
        why = """Jediný nový člen je pre §k=n+1§. Ostatné sú už v súčte do §n§.""",
        wrong = mapOf(1 to """§n§ je posledný člen súčtu do §n§, ten tam už je.""", 2 to """To je pravá strana pre §n+1§, cieľ kroku. Pribudne len jeden člen."""))
    thm("dml-mi-slaby")
    check("Kde v kroku použiješ IP?", """Keď súčet do §n§ nahradíš §\frac{n(n+1)}{2}§""", "Keď overíš §n=1§", """Na konci, keď vyjde §\frac{(n+1)(n+2)}{2}§""", correct = 0,
        why = """Tam, kde súčet do §n§ nahradíš vzorcom. Tú rovnosť označ §\stackrel{IP}{=}§, ako v skriptách.""",
        wrong = mapOf(1 to "To je základný krok, ten IP nepotrebuje. IP je predpoklad v kroku.", 2 to "Na konci už len upravuješ. IP si použil skôr, pri súčte do §n§."))
    practice("dml-mi-suma", solo = false)
    explain("Indukcia nemusí začať pri §n=1§.", """§n_0§ môže byť §0§, §4§ aj záporné. Základný krok je pri najmenšom §n§, pre ktoré tvrdenie platiť má, a krok ide od toho istého §n_0§.""",
        formal = cz("""Princip matematické indukce se typicky používá pro §n_0=0§ nebo §n_0=1§, není to ale vůbec nutné."""), src = "Skriptá, Poznámka 4.9")
    check("Tvrdenie má platiť pre §n\\geq 0§. Kde je základný krok?", "§n=0§", "§n=1§", "§n=0§ aj §n=1§, inak krok nezačne", correct = 0,
        why = """Pri najmenšom §n§, teda §n=0§. Krok potom ide pre každé §n\geq 0§.""",
        wrong = mapOf(1 to """Potom by §V(0)§ ostalo neoverené. Kocky padajú len dopredu.""", 2 to """Krok začne aj zo samotného §V(0)§, §V(1)§ dá krok. Overiť ho navyše nie je chyba, ale netreba."""))
    explain("Princíp formulou: záver je odmena.",
        """Záver §(\forall n\geq n_0)V(n)§ plynie z oboch krokov spolu. Dombek: kto pri princípe napíše len dva kroky, napísal „seznam úkolů, ale ne tu odměnu“.""",
        formal = listOf(p("""Prostředky predikátové logiky (kde §V(n)§ je predikátem pro §n§ a universum je §\mathbb{Z}§) pak lze slabou indukci zapsat následující pravdivou formulí:"""),
            // too wide for 320 dp in one row: ZK, IK and the conclusion under each other, the quantifier's scope indented
            // under it as in the core of Věta 4.1 (Zásada 11)
            b("""\big(V(n_0)\softbreak[1]\wedge(\forall n\geq n_0)\softbreak[0](V(n)\Rightarrow V(n+1))\big)\softbreak[1]\Rightarrow(\forall n\geq n_0)V(n)""")),
        src = "Skriptá, Poznámka 4.9; $Dombek")
    check("Ktorý zápis je indukčný krok?", """§(\forall n\geq 1)(V(n)\Rightarrow V(n+1))§""", """§(\forall n\geq 1)V(n)\Rightarrow V(n+1)§""", """§V(n)\Rightarrow(\forall n\geq 1)V(n+1)§""", correct = 0,
        why = """Kvantifikátor viaže celú implikáciu, preto zátvorka okolo nej. Dombek: chýbajúce zátvorky sú v písomkách častá chyba.""",
        wrong = mapOf(1 to """Bez zátvorky sa §\forall§ vzťahuje len na §V(n)§. Vznikne iná formula s voľným §n§ za šípkou.""",
            2 to """Tu je §n§ v §V(n)§ voľné a kvantifikátor stojí až za šípkou. Krok hovorí o každom §n§, celá implikácia patrí do zátvorky."""))
    practice("dml-mi-suma", solo = true)
}

private val zapis = lesson("dml-mi-zapis", "Zápis ako na písomke", 15, "Čo musí byť v riešení, aby opravujúci dal body, a chyby, za ktoré ich strhne.", art = 3, topics = listOf("MI2")) {
    ask("""Študent dokazuje indukciou, že §V(n)§ platí pre všetky §n\in\mathbb{N}§. V indukčnom kroku napíše: „Predpokladajme, že §V(n)§ platí pre všetky §n\in\mathbb{N}§.“ Je to v poriadku?""", "Áno, to je IP", "Nie", correct = 1,
        after = """Nie, to je celé tvrdenie, ktoré má dokázať. IP je §V(n)§ len pre jedno pevné, ľubovoľne zvolené §n§: z neho dokážeš §V(n+1)§, a keďže §n§ bolo ľubovoľné, krok platí pre všetky §n§. Predpokladať §V(n)§ pre všetky §n§ je predpokladať cieľ; Dombek: opravujúci sa zarazí, „to jsme měli dokázat“.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """IP hovorí o jednom pevnom §n§. Pre všetky §n§ je to záver, ktorý ešte nemáš."""))
    explore("Nájdi chybu v dôkaze.", """Dôkaz tvrdí, že §a^n=1§ pre každé nenulové celé §a§ a §n\in\mathbb{N}_0§. To neplatí, §2^1=2§. Ťukni na zlý riadok.""",
        MiFlaw(listOf(
            "ZK: pre §n=0§ je §a^0=1§.",
            "IK: nech pre §n\\geq 0§ platí §a^n=1§.",
            "Potom §a^{n+1}=a^n\\cdot\\frac{a^n}{a^{n-1}}=1\\cdot\\frac{1}{1}=1§.",
            "Podľa slabého princípu MI je §a^n=1§ pre všetky §n\\in\\mathbb{N}_0§.",
        ).map(::m), bad = 2),
        """Chyba je v §a^{n-1}=1§: o ňom IP nič nehovorí.""", """IP je len pre §n§. Pre §n=0§ navyše §a^{-1}§ nie je ani v tvrdení (Cvičení 4.11 a).""")
    explain("Štyri časti, ktoré opravujúci hľadá.",
        """**ZK** s výpočtom oboch strán. **IK**: „nech §n\geq n_0§ je pevné ľubovoľné a platí §V(n)§“, potom dôkaz §V(n+1)§. Rovnosť, kde použiješ IP, označ §\stackrel{IP}{=}§. **Záver**: podľa princípu MI platí §V(n)§ pre všetky §n\geq n_0§.""",
        formal = cz("Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."), src = "Písomka 4. 12. 2025 (variant E), príklad 1")
    clip("dml-mi-pisomka")
    check("Ktorá veta je správny začiatok indukčného kroku?", """Nech §n\geq 1§ je pevné ľubovoľné a platí §V(n)§.""", """Predpokladajme, že §V(n)§ platí pre každé §n\geq 1§.""", """Predpokladajme, že platí §V(n+1)§.""", correct = 0,
        why = """Pevné, ľubovoľné §n§ a predpoklad len preň. Ľubovoľné preto, aby dôkaz kroku platil pre každé §n\geq 1§ naraz; pevné preto, aby si nepredpokladal §V(n)§ pre všetky §n§, teda to, čo dokazuješ. Dombek: zvolíš ľubovoľné §n§ a zafixuješ ho.""",
        wrong = mapOf(1 to ForAll, 2 to """§V(n+1)§ je cieľ kroku. Predpokladať ho je dôkaz kruhom."""))
    explain("Bez základného kroku dokážeš aj nezmysel.",
        """Implikácia „§n§ je iracionálne §\Rightarrow n+1§ je iracionálne“ platí pre každé celé §n§. Bez ZK by z nej vyšlo, že každé celé číslo je iracionálne.""",
        formal = cz("""Nepřímý důkaz je tedy hotov a my tak můžeme podlehnout dojmu, že každé celé číslo je iracionální. To je ale samozřejmě nesmysl, na vině je vynechaný základní krok."""),
        src = "Skriptá, Varování 4.3")
    check("""§V(n)§: §1+2+\dots+n=\frac{(n-1)(n+2)}{2}§. Implikácia §V(n)\Rightarrow V(n+1)§ platí. Platí §V(n)§ pre všetky §n\in\mathbb{N}§?""", "Áno", "Nie, §V(1)§ neplatí", "Nedá sa rozhodnúť", correct = 1,
        why = """Nie, základný krok zlyhá: pre §n=1§ je vľavo §1§, vpravo §0§. Krok sám nič nedokazuje (Cvičení 4.11 b).""",
        wrong = mapOf(0 to """Krok platí, ale bez ZK nič nespadne. Pre §n=1§ je §1\neq\frac{0\cdot 3}{2}=0§.""", 2 to """Rozhodnúť sa dá dosadením §n=1§. §V(1)§ neplatí, tvrdenie teda neplatí pre všetky §n§."""))
    clip("dml-mi-naruby")
    explain("Nerob indukciu naruby.",
        """Nezačínaj tým, čo máš dokázať, a neupravuj to na §0=0§. Upravuj jednu stranu §V(n+1)§, v strede použi IP a dôjdi k druhej strane.""",
        formal = cz("""Při ověřování indukčního kroku používáme postupných úprav výrazu na jedné straně ověřované (ne)rovnosti a postupně (někde uprostřed použijeme indukční předpoklad) se propracujeme k výrazu na straně druhé.""",
            """Studenti se velmi často dopouští toho, že si neznámou (ne)rovnici (tu, kterou mají dokázat) napíší a pouze ji přepisují a upravují, a to tak dlouho, dokud nedostanou něco známého."""),
        src = "Skriptá, Varování 4.4")
    check("""Ktorý zápis indukčného kroku je správny?""",
        """§\sum_{k=1}^{n+1}(2k-1)=\sum_{k=1}^{n}(2k-1)+(2n+1)\stackrel{IP}{=}n^2+2n+1=(n+1)^2§""",
        """§\sum_{k=1}^{n+1}(2k-1)=(n+1)^2§, teda §n^2+2n+1=n^2+2n+1§, teda §0=0§""",
        """§n^2+(2n+1)=(n+1)^2§, a to platí""", correct = 0, sub = """Tvrdenie §§1+3+\dots+(2n-1)=n^2§§""",
        why = """Prvý ide od ľavej strany k pravej a vyznačí IP. Že z dokazovanej rovnosti vyjde pravdivé §0=0§, nič nedokazuje: pravdu odvodíš aj z nepravdy. Dôkaz musí ísť od známeho k cieľu, a to hovorí Varování 4.4.""",
        wrong = mapOf(1 to "To je indukcia naruby. Z dokazovaného odvodíš pravdu §0=0§, a to nič nedokazuje (Varování 4.4).",
            2 to """Nevidno, odkiaľ je §n^2§ a kde je IP. Treba napísať, že súčet do §n§ je §n^2§ podľa IP."""))
    worked("Deliteľnosť §7^n-1§ šiestimi", """§7^n-1§ je dělitelné §6§ pro §\forall n\in\mathbb{N}§.""", "Skriptá, Cvičení 4.1 a)", MiWeakPostup) {
        val (claim, zk, ik, end) = MiWeakPostup
        claim("""Dokazujem slabou indukciou výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""6\mid 7^n-1"""))
        zk("""Pre §n=1§ je §7-1=6§, a to je deliteľné §6§.""")
        ik("""Nech §n\geq 1§ je pevné a §7^n-1=6k§ pre nejaké §k\in\mathbb{Z}§ (IP); deliteľnosť prepíšem ako existenciu celého §k§. Výraz pre §n+1§ rozložím tak, aby v ňom bol výraz z IP.""",
            eq("""7^{n+1}-1=7\cdot 7^n-1=(6+1)7^n-1=(7^n-1)+6\cdot 7^n"""))
        ik("""Za §7^n-1§ dosadím z IP a vytknem §6§; §\ell=k+7^n§ je celé číslo.""", eq("""\stackrel{IP}{=}6k+6\cdot 7^n=6(k+7^n)"""))
        end("""Podľa slabého princípu MI je §7^n-1§ deliteľné §6§ pre všetky §n\in\mathbb{N}§.""")
    }
    check("""§8\mid 3^n+7^n-2§. V kroku: §3^{n+1}+7^{n+1}-2=3(3^n+7^n-2)+\square§. Čo patrí do §\square§?""", """§4\cdot 7^n+4§""", """§4\cdot 7^n§""", """§7^n+4§""", correct = 0,
        why = """§7\cdot 7^n-3\cdot 7^n=4\cdot 7^n§ a §-2+6=4§. A §4(7^n+1)§ je deliteľné §8§, lebo §7^n+1§ je párne (Cvičení 4.1 d).""",
        wrong = mapOf(1 to """Chýba konštanta: §-2-3\cdot(-2)=4§.""", 2 to """§7\cdot 7^n-3\cdot 7^n=4\cdot 7^n§, nie §7^n§."""))
    prove("dml-mi-p-del")
    practice("dml-mi-del", solo = false)
    check("Na písomke máš tvrdenie dokázať indukciou. Vieš aj kratší priamy dôkaz. Čo urobíš?", "Dokážem ho indukciou, ako chce zadanie", "Dokážem ho priamo, je krajší", "Napíšem „princíp MI platí“ a priamy dôkaz", correct = 0,
        why = "Indukciou, ako chce zadanie. Skriptá: za iný, aj lepší dôkaz zbytočne stratíte body (Poznámka 4.12).",
        wrong = mapOf(1 to "Skriptá varujú: za sebelepší, ale iný dôkaz zbytočne stratíte body (Poznámka 4.12).",
            2 to "Veta „princíp MI platí“ nič nedokazuje. Dombek: princíp platí, máte ukázať, že platí vaše tvrdenie."))
    practice("dml-mi-del", solo = true)
}

private val nerovnosti = lesson("dml-mi-nerovnosti", "Nerovnosti a začiatok inde", 15, "Ako viesť krok pri nerovnosti a od ktorého §n§ začať.", art = 4, topics = listOf("MI2")) {
    ask("Pre ktoré §n\\in\\mathbb{N}§ platí §n^2\\leq 2^n§?", "Pre všetky", "Pre §n=1,2§ a pre §n\\geq 4§", "Len pre §n\\geq 4§", correct = 1,
        after = """§n=3§ vypadne, lebo §9>8§. Od §n=4§ platí stále, a to dokáže indukcia so ZK pri §n=4§.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """Pre §n=3§ je §9>8§. Jedna hodnota stačí na vyvrátenie.""", 2 to """Aj §1\leq 2§ a §4\leq 4§ platia. Indukcia od §n=4§ ich nepokryje, overíš ich zvlášť."""))
    explore("Porovnaj obe strany.", "Odkrývaj riadky a sleduj, kde nerovnosť platí.",
        MiTable("n^2", "2^n", "\\leq", (1..8).map { Triple(it, "${it * it}", "${1 shl it}") }, (1..8).map { it * it <= 1 shl it }),
        "Od §n=4§ platí stále, §n=3§ je výnimka.")
    explore("Kde začne základný krok?", "Krok §V(n)\\Rightarrow V(n+1)§ platí už pre §n\\geq 3§, ale §V(3)§ neplatí (§9>8§). Zhoď kocky od §V(4)§.", MiDominoes(start = 4, stepFrom = 4, falseAt = setOf(3)),
        "ZK pri §n=4§ a krok pre §n\\geq 4§ zhodia všetko od §4§.", "Kocky §1§ a §2§ sú pravdivé, ale reťaz z nich cez §3§ neprejde.")
    explain("Krok pri nerovnosti: reťaz od jednej strany.",
        """Začni stranou §V(n+1)§, napr. §(n+1)^2§, a odhaduj jedným smerom. Kde použiješ IP, napíš §\stackrel{IP}{\leq}§. IP je nerovnosť, nedosádzaj ho ako rovnosť.""",
        formal = cz("""U důkazů nerovnic se navíc studenti dopouští toho, že i indukční předpoklad ve tvaru nerovnosti používají jako by to byla rovnost – prostě podle něj „dosadí“ – takovýchto hříchů se prosím nedopouštějte!"""),
        src = "Skriptá, Varování 4.4")
    worked("Nerovnosť od §n=4§", """Matematickou indukcí dokažte §n^2\leq 2^n§, pro §n\geq 4§.""", "Skriptá, Cvičení 4.3 d)", MiWeakPostup) {
        val (claim, zk, ik, end) = MiWeakPostup
        claim("""Dokazujem slabou indukciou výrok §V(n)§ pre všetky §n\in\mathbb{N}§, §n\geq 4§; indukcia začína od §4§.""", eq("""n^2\leq 2^n"""))
        zk("""Začínam pri §n=4§, kde tvrdenie má platiť prvý raz.""", eq("""4^2=16\leq 16=2^4"""))
        ik("""Nech §n\geq 4§ je pevné a §n^2\leq 2^n§ (IP). Najprv odhadnem §(n+1)^2§ zhora.""",
            eq("""(n+1)^2=n^2+2n+1\leq n^2+n^2=2n^2""", "Pre §n\\geq 4§ je §2n+1\\leq n^2§, lebo §n^2-2n-1=(n-1)^2-2\\geq 7§."))
        ik("""Potom IP vynásobím kladným číslom §2§.""", eq("""2n^2\stackrel{IP}{\leq}2\cdot 2^n=2^{n+1}"""))
        end("""Podľa slabého princípu MI platí §n^2\leq 2^n§ pre všetky §n\geq 4§.""")
    }
    check("Prečo krok potrebuje §n\\geq 4§, nie §n\\geq 1§?", """Využíva §2n+1\leq n^2§, a to pre §n=1,2§ neplatí""", "Lebo tak je to v zadaní", "Lebo pre §n=4§ nastane rovnosť", correct = 0,
        why = """Odhad §2n+1\leq n^2§ platí až od §n=3§. ZK pri §4§ a krok od §4§ do seba zapadnú.""",
        wrong = mapOf(1 to """Je to aj matematická nutnosť. Krok z §n=2§ na §3§ by musel dať §9\leq 8§.""", 2 to """Rovnosť pri §n=4§ nevadí, §\leq§ ju pripúšťa. Dôvod je odhad §2n+1\leq n^2§."""))
    check("Ktorý riadok kroku pre §n^2\\leq 2^n§ je chybný?", """§(n+1)^2=n^2+2n+1§""", """§n^2+2n+1\stackrel{IP}{=}2^n+2n+1§""", """§2\cdot 2^n=2^{n+1}§""", correct = 1,
        why = """IP je nerovnosť, za §n^2§ sa §2^n§ nedosádza. Správne je §\stackrel{IP}{\leq}§ (Varování 4.4).""",
        wrong = mapOf(0 to "Tento riadok je správny, je to roznásobenie platné pre každé §n§.", 2 to "Tento riadok je správny, §2\\cdot 2^n=2^{n+1}§ platí vždy."))
    prove("dml-mi-p-nerov")
    explain("Rozhodni, pre ktoré §n§ to platí.",
        """Najprv dosaď niekoľko hodnôt a odhadni hranicu §n_0§. Potom ZK pri §n_0§ a krok pre §n\geq n_0§. Malé §n§ pod hranicou vyrieš výpočtom.""",
        formal = cz("""Rozhodněte, pro která čísla z §\mathbb{N}§ platí §§\frac{1}{\sqrt{1}}+\frac{1}{\sqrt{2}}+\dots+\frac{1}{\sqrt{n}}\geq\sqrt{n}.§§"""),
        src = "Písomka 29. 11. 2021, príklad 2")
    check("""Po IP máš odhad zdola. Čo ešte treba ukázať, aby vyšlo §V(n+1)§?""",
        """§\sqrt{n}+\frac{1}{\sqrt{n+1}}\geq\sqrt{n+1}§""", """§\sqrt{n}+\frac{1}{\sqrt{n+1}}\leq\sqrt{n+1}§""", """§\sqrt{n}\geq\sqrt{n+1}§""", correct = 0, sub = """§§\sum_{k=1}^{n+1}\frac{1}{\sqrt{k}}\stackrel{IP}{\geq}\sqrt{n}+\frac{1}{\sqrt{n+1}}§§""",
        why = """Reťaz pokračuje rovnakým smerom §\geq§ až k pravej strane §V(n+1)§. Potom z oboch nerovností plynie výsledok.""",
        wrong = mapOf(1 to """Opačný smer reťaz preruší. Z §a\geq b§ a §b\leq c§ neplynie nič.""", 2 to """To neplatí, §\sqrt{n}<\sqrt{n+1}§. Treba využiť aj člen §\frac{1}{\sqrt{n+1}}§."""))
    worked("Súčet prevrátených odmocnín", """Dokážte §\frac{1}{\sqrt{1}}+\dots+\frac{1}{\sqrt{n}}\geq\sqrt{n}§ pre všetky §n\in\mathbb{N}§.""", "Písomka 29. 11. 2021, príklad 2", MiWeakPostup) {
        val (claim, zk, ik, end) = MiWeakPostup
        claim("""Dokazujem slabou indukciou výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""\sum_{k=1}^{n}\frac{1}{\sqrt{k}}\geq\sqrt{n}"""))
        zk("""Pre §n=1§ je ľavá strana §\frac{1}{\sqrt{1}}=1§ a pravá §\sqrt{1}=1§, nerovnosť platí.""")
        ik("""Nech §n\geq 1§ je pevné a platí §V(n)§ (IP). Odtrhnem posledný člen súčtu a na zvyšok použijem IP.""",
            eq("""\sum_{k=1}^{n+1}\frac{1}{\sqrt{k}}=\sum_{k=1}^{n}\frac{1}{\sqrt{k}}+\frac{1}{\sqrt{n+1}}\stackrel{IP}{\geq}\sqrt{n}+\frac{1}{\sqrt{n+1}}"""))
        ik("""Zlomky dám na spoločného menovateľa a odhadnem čitateľ zdola.""",
            eq("""=\frac{\sqrt{n(n+1)}+1}{\sqrt{n+1}}\geq\frac{\sqrt{n\cdot n}+1}{\sqrt{n+1}}=\frac{n+1}{\sqrt{n+1}}=\sqrt{n+1}""", "§n(n+1)\\geq n^2§ a odmocnina je rastúca."))
        end("""Podľa slabého princípu MI nerovnosť platí pre všetky §n\in\mathbb{N}§.""")
    }
    check("§2^n>n^2§: od ktorého §n§ to platí stále?", "§n\\geq 5§", "§n\\geq 4§", "§n\\geq 3§", correct = 0,
        why = """§2^4=16=4^2§ ešte nie je ostro väčšie, §2^5=32>25§ už áno. Odtiaľ to platí stále: krok §2^{n+1}=2\cdot 2^n\stackrel{IP}{>}2n^2\geq(n+1)^2§ prejde, lebo §2n^2-(n+1)^2=(n-1)^2-2\geq 0§ pre §n\geq 3§. ZK je teda pri §n=5§ a krok pre každé §n\geq 5§.""",
        wrong = mapOf(1 to """Pri §n=4§ je rovnosť §16=16§, ostrá nerovnosť neplatí.""", 2 to """Pri §n=3§ je §8<9§, nerovnosť neplatí."""))
    check("""Zadanie: „Zjistěte, pro která §n\in\mathbb{N}§ platí §x_n<4§.“ Čo musí byť v odpovedi?""", "Všetky také §n§, dôkaz pre ne a výpočet pre ostatné", "Len dôkaz indukciou pre §n\\geq n_0§", "Len tabuľka prvých hodnôt", correct = 0,
        why = """Odpoveď je množina §n§, nie len dôkaz. Malé §n§ mimo nej ukáž výpočtom, zvyšok indukciou.""",
        wrong = mapOf(1 to """Chýba, čo s §n<n_0§. Treba povedať, že tam nerovnosť neplatí, a ukázať to.""", 2 to """Tabuľka je odhad, nie dôkaz. Pre nekonečne veľa §n§ treba indukciu."""))
    check("Ktorý krok pre §n!\\leq n^n§ je správny?",
        """§(n+1)!=(n+1)\cdot n!\stackrel{IP}{\leq}(n+1)n^n\leq(n+1)(n+1)^n§""",
        """§(n+1)!=(n+1)\cdot n!\stackrel{IP}{=}(n+1)n^n=(n+1)^{n+1}§""",
        """§(n+1)!\leq(n+1)^{n+1}§, lebo §n!\leq n^n§""", correct = 0,
        why = """Reťaz ide jedným smerom a IP je vyznačený. Posledný krok platí, lebo §n^n\leq(n+1)^n§ (Cvičení 4.3 c).""",
        wrong = mapOf(1 to """IP je nerovnosť, nie rovnosť. A §(n+1)n^n\neq(n+1)^{n+1}§.""", 2 to "To je len tvrdenie, nie odvodenie. Chýba výpočet s IP."))
    check("""Prečo platí posledná nerovnosť v kroku pre §2^{n-1}\leq n!§?""",
        "Lebo §2\\leq n+1§ pre §n\\geq 1§ a §n!>0§", "Lebo §n!\\geq 1§", "Lebo §2^n\\leq(n+1)!§", correct = 0, sub = """§§2^n=2\cdot 2^{n-1}\stackrel{IP}{\leq}2\cdot n!\leq(n+1)\cdot n!§§""",
        why = """Pre §n\geq 1§ je §2\leq n+1§ a obe strany násobíš kladným §n!§. Násobenie kladným číslom smer nerovnosti zachová, záporným by ho otočilo. Prvá nerovnosť reťaze je IP vynásobený kladnou §2§ (Cvičení 4.3 b).""",
        wrong = mapOf(1 to """§n!\geq 1§ je pravda, ale porovnávaš §2§ a §n+1§.""", 2 to "To je cieľ kroku, nemôže byť jeho zdôvodnením."))
    // the claim and the bound after the IP under the question, not two fractions inside the title (Zásada 11)
    check("""Krok nerovnosti: čo ešte treba ukázať?""",
        """§\frac{1}{n}-\frac{1}{(n+1)^2}\geq\frac{1}{n+1}§""", """§\frac{1}{n}-\frac{1}{(n+1)^2}\leq\frac{1}{n+1}§""", "Nič, krok je hotový", correct = 0,
        sub = """Tvrdenie §§\sum_{k=1}^{n}\frac{1}{k^2}\leq 2-\frac{1}{n}§§ Po IP máš §§2-\frac{1}{n}+\frac{1}{(n+1)^2}§§""",
        why = """Od §2§ musíš odčítať aspoň §\frac{1}{n+1}§. Len tak vyjde §\leq 2-\frac{1}{n+1}§ (Cvičení 4.4 f).""",
        wrong = mapOf(1 to "Opačne. Menší odčítanec dá väčší výsledok, a ten by nemusel byť pod hranicou.", 2 to """Výraz §2-\frac{1}{n}+\frac{1}{(n+1)^2}§ ešte nie je §2-\frac{1}{n+1}§. Treba ich porovnať."""))
}

/** The recurrence of the paper 25. 11. 2022: §C_{n+2}=-C_{n+1}+12C_n§, §C_n=3^n+(-4)^n§. */
private val rec2022 = MiRec(3, -4, 0, 0).also { check(it.p == -1L && it.q == 12L && it.closed(1) == -1L && it.closed(2) == 25L) }

private val silna = lesson("dml-mi-silna", "Silná indukcia a rekurencie", 16, "Kedy nestačí predpoklad pre §n§ a ako vyzerá krok so ZK pre viac hodnôt.", art = 5, topics = listOf("MI2", "MI3")) {
    ask("""Dokazuješ indukciou, že každé celé §n\geq 2§ je súčin prvočísel. V indukčnom kroku nie je §n+1§ prvočíslo, teda §n+1=a\cdot b§, kde §2\leq a,b\leq n§. Stačí na to IP slabej indukcie, teda tvrdenie len pre §n§?""", "Áno", "Nie, treba vedieť niečo o §a§ a §b§", correct = 1,
        after = """Nie, §a§ ani §b§ nemusí byť §n§. Treba predpoklad pre všetky čísla do §n§, a to je silná indukcia.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """IP hovorí len o §n§, nie o §a§ a §b§. Napr. §12=3\cdot 4§ pri §n=11§."""))
    clip("dml-mi-slaba-nestaci")
    explain("Silný princíp: predpoklad pre všetky predošlé.",
        """**ZK:** overíš §V(n_0),\dots,V(n_0+m)§. **IK:** pre každé §n\geq n_0+m§ z §V(n_0)\wedge\dots\wedge V(n)§ dokážeš §V(n+1)§. Potom §V(n)§ platí pre všetky §n\geq n_0§.""",
        cards = listOf("dml-mi-silny"))
    clip("dml-mi-nohy")
    thm("dml-mi-silny")
    explore("Zaplať mincami 3 a 5 korún.", "Ťukni na niekoľko súm. Ktoré sa nedajú?", MiCoins(3, 5, 20),
        "Od §8§ ide každá suma: §n+1§ je suma §n-2§ plus trojkoruna.", "Preto ZK pre §8,9,10§ a krok siaha tri sumy späť (Příklad 4.15).")
    worked("Platba mincami", """Dokážte, že tri- a päťkorunami sa dá bez vracania vyplatiť každá suma aspoň §8§ korún.""", "Skriptá, Příklad 4.15", MiStrongPostup) {
        val (claim, zk, ik, end) = MiStrongPostup
        claim("""§V(n)§: sumu §n§ korún vyplatíš len trojkorunami a päťkorunami. Dokazujem ho pre všetky §n\geq 8§.""")
        zk("""Krok siaha tri sumy späť, preto overím tri hodnoty.""", eq("""8=3+5,\quad 9=3+3+3,\quad 10=5+5"""))
        ik("""Nech §n\geq 10§ je pevné a platí §V(8),\dots,V(n)§ (IP). Potom §n-2\geq 8§ a sumu §n-2§ vyplatíš podľa IP.""")
        ik("""Pridáš trojkorunu a máš §n+1§ korún, teda platí §V(n+1)§.""")
        end("""Podľa silného princípu MI platí §V(n)§ pre všetky §n\geq 8§.""")
    }
    check("Prečo v kroku mincí treba §n\\geq 10§?", """Aby §n-2\geq 8§ a IP sa naň vzťahoval""", "Lebo §10§ je deliteľné §5§", "Aby bol ZK kratší", correct = 0,
        why = """IP pokrýva len §8,\dots,n§. Pre §n=9§ by §n-2=7§, a §7§ sa vyplatiť nedá (Spěvák, 4. prednáška).""",
        wrong = mapOf(1 to "Deliteľnosť §5§ s krokom nesúvisí. Krok pridáva trojkorunu k sume §n-2§.", 2 to """ZK je naopak dlhší, tri hodnoty. Hranica §10§ zaručí, že §n-2\geq 8§."""))
    def("dml-mi-rekurentna")
    explain("Koľko členov späť, toľko hodnôt v ZK.",
        """Ak §A_{n+2}§ závisí od §A_{n+1}§ a §A_n§, krok pre §A_{n+1}§ siaha na §A_n§ aj §A_{n-1}§: silná indukcia a ZK pre §n=1,2§. V Cvičení 4.6 siaha rekurencia tri členy späť, preto tam tri hodnoty.""",
        formal = cz("""Stejně jako ve Cvičení 4.5 musíme použít silnou indukci, jelikož dokazujeme rovnost týkající se přímo členů §A_n§ a ty (kromě prvních členů) vždy závisí na trojici předchozích členů."""),
        src = "Skriptá, Cvičení 4.6, řešení")
    // C_n = 3^n + (-4)^n is the generator's family with roots 3 and -4: its solution, the letter of the paper (Zásada 13)
    worked("Rekurencia z písomky 2022", """§C_{n+2}=12C_n-C_{n+1}§, §C_1=${rec2022.closed(1)}§, §C_2=${rec2022.closed(2)}§. Dokážte, že §C_n=${rec2022.closedTex()}§ pre všetky §n\in\mathbb{N}§.""", "Písomka 25. 11. 2022, príklad 4", miRecSolution(rec2022, "C"))
    check("""Koľko hodnôt treba v ZK pri tejto rekurencii?""", "§3§", "§2§", "§1§", correct = 0, sub = """§§A_{n+3}=A_{n+2}+5A_{n+1}+3A_n§§""",
        why = """Krok siaha tri členy späť, preto §A_0,A_1,A_2§ (Cvičení 4.6).""",
        wrong = mapOf(1 to """Pre §A_3§ treba §A_2,A_1,A_0§. S dvoma hodnotami by prvý krok nemal z čoho počítať.""", 2 to "Jedna hodnota stačí len pri rekurencii o jeden člen späť."))
    prove("dml-mi-p-rek")
    practice("dml-mi-rek", solo = false)
    check("""Pre ktoré §n§ je §x_n<4§?""", "Pre §n\\geq 4§", "Pre §n\\geq 3§", "Pre žiadne", correct = 0, sub = """§§x_1=x_2=5,\quad x_{n+2}=\sqrt{1+2x_n+x_{n+1}}§§""",
        why = """§x_3=\sqrt{16}=4§ ešte nie, §x_4=\sqrt{15}<4§ už áno. Ďalej silnou indukciou: §x_{n+1}<\sqrt{1+8+4}<4§.""",
        wrong = mapOf(1 to """§x_3=\sqrt{1+10+5}=4§, a §4<4§ neplatí.""", 2 to """§x_4=\sqrt{1+10+4}=\sqrt{15}<4§."""))
    prove("dml-mi-p-postupnost")
    practice("dml-mi-rek", solo = true)
}

private val maticaE = listOf(listOf(-1L, 3L), listOf(0L, -1L))

/** [maticaE] in TeX, from its numbers. */
private val maticaETex = dev.hamper.tabula.la.Mat(maticaE.map { r -> r.map { dev.hamper.tabula.la.Frac.of(it) } }).tex()

/** A, A^2, ..., A^k of [maticaE], multiplied out (the worked example shows them, nothing typed by hand). */
private fun maticaEPowers(k: Int): List<List<List<Long>>> = generateSequence(maticaE) { p ->
    List(2) { i -> List(2) { j -> (0 until 2).sumOf { t -> p[i][t] * maticaE[t][j] } } }
}.take(k).toList()

/** The generator's family (dml-mi-matica-jordan) for this matrix: the guess and the multiplied-out step (Zásada 13). */
private val maticaEFamily = miJordan(-1, 3, low = false).also { check(it.a == maticaE) }

private val maticaESolution: Solution = MiMatrixPostup.let { pp ->
    val (guess, zk, ik, end) = pp
    pp.solve {
        guess("""Spočítam prvé mocniny: diagonála strieda znamienko, vpravo hore je §3n§ so znamienkom.""",
            eq(maticaEPowers(4).withIndex().drop(1).joinToString(",\\ ") { (i, p) -> "A^{${i + 1}}=" + dev.hamper.tabula.la.Mat(p.map { r -> r.map { dev.hamper.tabula.la.Frac.of(it) } }).tex() }))
        guess("""Odhad zapíšem ako výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""A^n=${maticaEFamily.right.tex("n")}"""))
        zk("""Pre §n=1§ dá odhad na diagonále §-1§ a vpravo hore §3§, teda presne maticu §A§.""")
        ik("""Nech §n\in\mathbb{N}§ je pevné a platí §V(n)§ (IP). §A^{n+1}§ rozložím na §A^n\cdot A§ a za §A^n§ dosadím odhad.""",
            listOf(Formula("""A^{n+1}=A^n\cdot A\stackrel{IP}{=}${maticaEFamily.right.tex("n")}${maticaETex}""", "Tu je IP.")) + maticaEFamily.middle.map { Formula("=$it") })
        ik("""Vyšiel odhad s §n+1§ namiesto §n§, teda §V(n+1)§.""")
        end("""Podľa slabého princípu MI platí §V(n)§ pre všetky §n\in\mathbb{N}§.""")
    }
}

private val matice = lesson("dml-mi-matice", "Mocnina matice", 16, "Najčastejší príklad písomky: odhadnúť §A^n§ a dokázať odhad indukciou.", art = 6, topics = listOf("MI2", "MI3")) {
    ask("""Nad §\mathbb{R}§: §A=\begin{pmatrix}1&1\\0&1\end{pmatrix}§. Aké je §A^5§?""", """§\begin{pmatrix}1&5\\0&1\end{pmatrix}§""", """§\begin{pmatrix}1&1\\0&1\end{pmatrix}§""", """§\begin{pmatrix}5&5\\0&5\end{pmatrix}§""", correct = 0,
        after = """Každé násobenie §A§ pridá vpravo hore §1§. Odtiaľ odhad §A^n=\begin{pmatrix}1&n\\0&1\end{pmatrix}§ (Cvičení 4.9 a).""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Mocnina sa mení: už §A^2=\begin{pmatrix}1&2\\0&1\end{pmatrix}\neq A§.""", 2 to """Mocnina nie je §5A§. §A^5=A\cdot A\cdot A\cdot A\cdot A§."""))
    explore("Nad §\\mathbb{R}§: Mocniny matice z písomky 2025.", "Zvyšuj §n§ a sleduj, čo sa deje s prvkami.", MiPowers(maticaE),
        """Diagonála strieda §-1§ a §1§, vpravo hore je §3n§ so striedavým znamienkom.""", """Odhad: §A^n=\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}§.""")
    explain("Postup v troch krokoch.",
        """Spočítaj §A^2§, §A^3§, prípadne §A^4§. Odhadni vzorec a over ho na nich. Potom indukcia: ZK pre §n=1§ a krok §A^{n+1}=A^n\cdot A§ s IP.""",
        formal = listOf(Para(pMatice.task)), src = "Písomka 4. 12. 2025 (variant E), príklad 1")
    check("Ako rozložíš §A^{n+1}§ v kroku?", """§A^n\cdot A§ alebo §A\cdot A^n§""", """Len §A\cdot A^n§, súčin matíc nie je komutatívny""", """§A^n+A§""", correct = 0,
        why = """Oba rozklady sú správne, lebo násobenie matíc je asociatívne. §A^{n+1}§ je súčin §n+1§ matíc §A§ a zátvorky smieš dať okolo prvých §n§ aj okolo posledných §n§, výsledok je ten istý. Skriptá: „můžeme si vybrat“ (Cvičení 4.9).""",
        wrong = mapOf(1 to """Komutativitu tu netreba. §A^n\cdot A§ aj §A\cdot A^n§ sú §A^{n+1}§ vďaka asociativite.""", 2 to "Mocnina je súčin, nie súčet."))
    worked("Písomka 4. 12. 2025 (variant E)", pMatice.task.replace('$', '§'), "Písomka 4. 12. 2025 (variant E), príklad 1", maticaESolution)
    // the matrices under the question at body size: inside the title they broke over four lines (Zásada 11)
    check("""Nad §\mathbb{R}§: Matica z písomky 2023: aký je odhad §A^n§?""", "§A^n=3^{n-1}A§", "§A^n=3^nA§", "§A^n=3nA§", correct = 0,
        sub = """§§\begin{aligned}A&=\begin{pmatrix}1&-2\\-1&2\end{pmatrix},\\A^2&=\begin{pmatrix}3&-6\\-3&6\end{pmatrix}=3A.\end{aligned}§§""",
        why = """§A^2=3A§, teda každé ďalšie násobenie pridá činiteľ §3§. Pre §n=1§ musí vyjsť §A§, preto §3^{n-1}§.""",
        wrong = mapOf(1 to """Pre §n=1§ by vyšlo §3A\neq A§. Exponent je o jedna menší.""", 2 to """Pre §n=2§ by vyšlo §6A§, ale §A^2=3A§."""))
    prove("dml-mi-p-matice")
    explain("Na čo si dať pozor v zápise.",
        """Odhad bez výpočtu mocnín, chýbajúci ZK, IP „pre všetky §n§“ a krok bez vyznačeného IP. Zadanie to pripomína výslovne.""",
        formal = cz("Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."), src = "Písomky 2024 a 2025, príklad 1; $Dombek")
    check("""Nad §\mathbb{R}§: §A=\begin{pmatrix}1&0&1\\0&3&0\\0&0&1\end{pmatrix}§ (písomka 2024). Aký je odhad §A^n§?""",
        """§\begin{pmatrix}1&0&n\\0&3^n&0\\0&0&1\end{pmatrix}§""", """§\begin{pmatrix}1&0&1\\0&3^n&0\\0&0&1\end{pmatrix}§""", """§\begin{pmatrix}1&0&n\\0&3n&0\\0&0&1\end{pmatrix}§""", correct = 0,
        why = """§A^2§ má vpravo hore §2§ a v strede §9§. Rohový prvok rastie o §1§, stred sa násobí §3§.""",
        wrong = mapOf(1 to """§A^2§ má vpravo hore §1+1=2§, nie §1§.""", 2 to """Stred sa pri každom násobení násobí §3§: §3,9,27§, nie §3,6,9§."""))
    practice("dml-mi-matica-jordan", solo = false)
    check("Spočítal si §A^2,A^3§ a odhad sedí. Treba ešte ZK?", "Áno, jeden riadok: §n=1§ v odhade dá §A§", "Nie, výpočet mocnín ho nahrádza", "Nie, pri maticiach sa ZK nerobí", correct = 0,
        why = "Áno, ZK patrí do dôkazu a stačí jeden riadok. Výpočty pred odhadom sú len hľadanie vzorca.",
        wrong = mapOf(1 to "Nenahrádza, chýba overenie pre §n=1§, kde indukcia začína. Výpočty overia len §n=2,3§ (Varování 4.3).", 2 to "ZK patrí ku každej indukcii, aj pri maticiach (Cvičení 4.9)."))
    check("""Nad §\mathbb{R}§: §A=\begin{pmatrix}2&1\\0&1\end{pmatrix}§. Vpravo hore v §A^2,A^3,A^4§ je §3,7,15§. Odhad?""", "§2^n-1§", "§2n-1§", "§n§", correct = 0,
        why = """§3=2^2-1§, §7=2^3-1§, §15=2^4-1§. Pre §n=1§ dá §2^1-1=1§, sedí s §A§.""",
        wrong = mapOf(1 to """§2n-1§ dá §3,5,7§, nie §3,7,15§.""", 2 to """Prvok rastie rýchlejšie než §n§: §3,7,15§."""))
    check("""Nad §\mathbb{R}§: Krok pre §A=\begin{pmatrix}2&1\\0&1\end{pmatrix}§: v súčine §\begin{pmatrix}2^n&2^n-1\\0&1\end{pmatrix}\begin{pmatrix}2&1\\0&1\end{pmatrix}§ je vpravo hore""",
        """§2^n+(2^n-1)=2^{n+1}-1§""", """§2(2^n-1)=2^{n+1}-2§""", """§2^n\cdot 1=2^n§""", correct = 0,
        why = """Prvý riadok krát druhý stĺpec dá §2^n\cdot 1+(2^n-1)\cdot 1=2^{n+1}-1§, teda odhad pre §n+1§.""",
        wrong = mapOf(1 to """Riadok krát stĺpec je §2^n\cdot 1+(2^n-1)\cdot 1§, nie prvok krát §2§.""", 2 to """Chýba druhý sčítanec §(2^n-1)\cdot 1§. Riadok krát stĺpec sčíta dva súčiny."""))
    check("Kde v kroku uvedieš IP?", """Pri §A^n\cdot A§, keď §A^n§ nahradíš odhadom""", "Pri ZK", "Na konci, keď vyjde odhad pre §n+1§", correct = 0,
        why = """Keď §A^n§ nahradíš odhadom, rovnosť označ §\stackrel{IP}{=}§. Zadanie to chce: „kde využíváte indukční předpoklad“.""",
        wrong = mapOf(1 to "ZK IP nepotrebuje, je to výpočet pre §n=1§.", 2 to "Na konci už len upravuješ. IP si použil pri dosadení za §A^n§."))
    practice("dml-mi-matica", solo = true)
}

/* ---- Past papers: this topic's problem 1 ------------------------------------------------------------------------- */

private fun matrixRubric(pts: Int, drill: String) = listOf(
    Rubric("Niekoľko mocnín spočítaných a odhad §A^n§", 3, drill),
    Rubric("ZK pre §n=1§", 1, "proof:dml-mi-p-matice"),
    Rubric("IK: IP pre pevné ľubovoľné §n§ a cieľ §V(n+1)§", 2, "proof:dml-mi-p-matice"),
    Rubric("§A^{n+1}=A^n\\cdot A§ s vyznačeným IP a úprava na odhad", pts - 7, drill),
    Rubric("Záver podľa princípu MI", 1, "proof:dml-mi-p-matice"),
).map { it.copy(text = m(it.text)) }


private val paper2025e = PaperProblem(1, 10, "Mocnina matice", pMatice.task, 25, matrixRubric(10, "dml-mi-matica-jordan"),
    m("""§A^n=\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}§ pre všetky §n\in\mathbb{N}§."""), "dml-mi-matica-jordan", maticaESolution)

private val paper2025k = PaperProblem(1, 10, "Postupnosť a nerovnosť", pPostupnost.task, 25,
    listOf(
        Rubric("Prvé členy a odpoveď: práve pre §n\\geq 4§", 2, "proof:dml-mi-p-postupnost"),
        Rubric("ZK pre §n=4§ a §n=5§", 2, "proof:dml-mi-p-postupnost"),
        Rubric("IK silnej indukcie, IP pre §k\\in\\{4,\\dots,n\\}§", 2, "proof:dml-mi-p-postupnost"),
        Rubric("Odhad §x_{n+1}<\\sqrt{13}<4§ s vyznačeným IP a rastúcou odmocninou", 3, "proof:dml-mi-p-postupnost"),
        Rubric("Záver aj pre §n=1,2,3§", 1, "proof:dml-mi-p-postupnost"),
    ).map { it.copy(text = m(it.text)) },
    m("""§x_n<4§ práve pre §n\geq 4§ (§x_1=x_2=5§, §x_3=4§)."""), null, proofSolution(pPostupnost))

/**
 * "A^2=…,\\ A^3=…" of a past paper's matrix, multiplied out by the engine (Zásada 13), never typed: [also] adds what the
 * power equals besides ("=3A").
 */
private fun powersTex(a: MiMat, upto: Int, also: (Int, MiMat) -> String = { _, _ -> "" }): String =
    (2..upto).joinToString(",\\ ") { k -> miMatPow(a, k).let { p -> "A^{$k}=" + miPmat(p.map { r -> r.map { "$it" } }) + also(k, p) } }

private val matica2024r: MiMat = listOf(listOf(1L, 0L, 1L), listOf(0L, 3L, 0L), listOf(0L, 0L, 1L))
private val matica2023d: MiMat = listOf(listOf(1L, -2L), listOf(-1L, 2L))

/** "=3A" when the power is a multiple of A, checked on the numbers. */
private fun multipleOfA(a: MiMat): (Int, MiMat) -> String = { k, p ->
    val c = miIpow(3L, k - 1)
    check(p == a.map { r -> r.map { it * c } }) { "A^$k is not ${c}A" }
    "=${c}A"
}

private val paper2024r = PaperProblem(1, 9, "Mocnina matice 3 krát 3",
    m("""Uvažujme matici §\mathbf{A}=\begin{pmatrix}1&0&1\\0&3&0\\0&0&1\end{pmatrix}\in\mathbb{R}^{3,3}§. Výpočtem pro několik prvních hodnot §n§ odhadněte §\mathbf{A}^n§ a odhad dokažte matematickou indukcí (uveďte indukční krok a využití IP)."""),
    22, matrixRubric(9, "dml-mi-matica"),
    m("""§A^n=\begin{pmatrix}1&0&n\\0&3^n&0\\0&0&1\end{pmatrix}§ pre všetky §n\in\mathbb{N}§."""), "dml-mi-matica",
    MiMatrixPostup.let { pp ->
        val (guess, zk, ik, end) = pp
        pp.solve {
            guess("""Spočítam prvé mocniny: roh rastie o §1§, stred sa násobí §3§.""",
                eq(powersTex(matica2024r, 3)))
            guess("""Odhad zapíšem ako výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""A^n=\begin{pmatrix}1&0&n\\0&3^n&0\\0&0&1\end{pmatrix}"""))
            zk("""Pre §n=1§ dá odhad §\begin{pmatrix}1&0&1\\0&3&0\\0&0&1\end{pmatrix}=A§.""")
            ik("""Nech §n\in\mathbb{N}§ je pevné a platí §V(n)§ (IP). §A^{n+1}=A^n\cdot A§, za §A^n§ dosadím odhad a vynásobím.""",
                eq("""A^{n+1}=A^n\cdot A\stackrel{IP}{=}\begin{pmatrix}1&0&n\\0&3^n&0\\0&0&1\end{pmatrix}\begin{pmatrix}1&0&1\\0&3&0\\0&0&1\end{pmatrix}=\begin{pmatrix}1&0&1+n\\0&3^{n+1}&0\\0&0&1\end{pmatrix}""", "Tu je IP."))
            ik("""Vyšiel odhad s §n+1§ namiesto §n§, teda §V(n+1)§.""")
            end("""Podľa slabého princípu MI platí §V(n)§ pre všetky §n\in\mathbb{N}§.""")
        }
    })

private val paper2023d = PaperProblem(1, 10, "Mocnina matice",
    m("""Uvažujme matici §\mathbf{A}=\begin{pmatrix}1&-2\\-1&2\end{pmatrix}\in\mathbb{R}^{2,2}§. Výpočtem pro několik prvních hodnot §n\in\mathbb{N}§ odhadněte, čemu se rovná §\mathbf{A}^n§ pro obecné §n§ a tento odhad dokažte matematickou indukcí."""),
    25, matrixRubric(10, "dml-mi-matica"),
    m("""§A^n=3^{n-1}A=\begin{pmatrix}3^{n-1}&-2\cdot 3^{n-1}\\-3^{n-1}&2\cdot 3^{n-1}\end{pmatrix}§ pre všetky §n\in\mathbb{N}§."""), "dml-mi-matica",
    MiMatrixPostup.let { pp ->
        val (guess, zk, ik, end) = pp
        pp.solve {
            guess("""Spočítam prvé mocniny: každé násobenie pridá činiteľ §3§.""", eq(powersTex(matica2023d, 3, multipleOfA(matica2023d))))
            guess("""Odhad zapíšem ako výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""A^n=3^{n-1}A"""))
            zk("""Pre §n=1§ je §3^0=1§, odhad dá §A§.""")
            ik("""Nech §n\in\mathbb{N}§ je pevné a §A^n=3^{n-1}A§ (IP). §A^{n+1}=A^n\cdot A§, za §A^n§ dosadím odhad.""",
                eq("""A^{n+1}=A^n\cdot A\stackrel{IP}{=}3^{n-1}A\cdot A=3^{n-1}A^2=3^{n-1}\cdot 3A=3^nA""", "§A^2=3A§ zo začiatku."))
            ik("""Vyšiel odhad s §n+1§ namiesto §n§, teda §V(n+1)§.""")
            end("""Podľa slabého princípu MI platí §A^n=3^{n-1}A§ pre všetky §n\in\mathbb{N}§.""")
        }
    })

private val paper2023a = PaperProblem(1, 10, "Rekurencia", pRek.task, 25,
    listOf(
        Rubric("ZK pre §n=1§ aj §n=2§", 2, "dml-mi-rek"),
        Rubric("Silná indukcia: IP pre §k\\in\\{1,\\dots,n\\}§, §n\\geq 2§", 2, "proof:dml-mi-p-rek"),
        Rubric("Rekurencia a IP pre §A_n§ aj §A_{n-1}§, vyznačené", 3, "dml-mi-rek"),
        Rubric("Úprava na §3^n+(-2)^{n+1}§", 2, "dml-mi-rek"),
        Rubric("Záver podľa silného princípu MI", 1, "proof:dml-mi-p-rek"),
    ).map { it.copy(text = m(it.text)) },
    m("""Rovnosť platí pre všetky §n\in\mathbb{N}§, dôkaz silnou indukciou so ZK pre §n=1,2§."""), "dml-mi-rek", proofSolution(pRek))

/* 2024 F, J, H and the opravná 2026 Ř (fit-wiki photos, transcribed in zdroje/testy-dml-2.md). */

private val maticaJ: MiMat = listOf(listOf(-1L, 0L), listOf(5L, -1L))

/** The generator's family (dml-mi-matica-jordan) for the 2024 J matrix: the guess and the multiplied-out step (Zásada 13). */
private val maticaJFamily = miJordan(-1, 5, low = true).also { check(it.a == maticaJ) }

private val paper2024j = PaperProblem(1, 9, "Mocnina matice",
    m("""Uvažujme matici §\mathbf{A}=\begin{pmatrix}-1&0\\5&-1\end{pmatrix}\in\mathbb{R}^{2,2}§. Výpočtem pro několik prvních hodnot §n\in\mathbb{N}§ odhadněte, čemu se rovná §\mathbf{A}^n§ pro obecné §n\in\mathbb{N}§ a tento svůj odhad dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."""),
    25, matrixRubric(9, "dml-mi-matica-jordan"),
    m("""§A^n=${maticaJFamily.right.tex("n")}§ pre všetky §n\in\mathbb{N}§."""), "dml-mi-matica-jordan",
    MiMatrixPostup.let { pp ->
        val (guess, zk, ik, end) = pp
        pp.solve {
            guess("""Spočítam prvé mocniny: diagonála strieda znamienko, vľavo dole je §5n§ so znamienkom.""", eq(powersTex(maticaJ, 4)))
            guess("""Odhad zapíšem ako výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""A^n=${maticaJFamily.right.tex("n")}"""))
            zk("""Pre §n=1§ dá odhad na diagonále §-1§ a vľavo dole §5\cdot 1\cdot(-1)^0=5§, teda presne maticu §A§.""")
            ik("""Nech §n\in\mathbb{N}§ je pevné a platí §V(n)§ (IP). §A^{n+1}§ rozložím na §A^n\cdot A§ a za §A^n§ dosadím odhad.""",
                listOf(Formula("""A^{n+1}=A^n\cdot A\stackrel{IP}{=}${maticaJFamily.right.tex("n")}${miPmat(maticaJ.map { r -> r.map { "$it" } })}""", "Tu je IP.")) + maticaJFamily.middle.map { Formula("=$it") })
            ik("""Vyšiel odhad s §n+1§ namiesto §n§, teda §V(n+1)§.""")
            end("""Podľa slabého princípu MI platí §V(n)§ pre všetky §n\in\mathbb{N}§.""")
        }
    })

/** F_n of the 2024 F recurrence, computed from F_1, F_2; the closed form 2^n(2n-1) is checked on it. */
private val rekF: List<Long> = generateSequence(2L to 12L) { (a, b) -> b to 4 * b - 4 * a }.take(20).map { it.first }.toList()
    .also { f -> f.forEachIndexed { i, v -> val n = i + 1; check(v == miIpow(2, n) * (2 * n - 1)) { "F_$n" } } }

private val paper2024f = PaperProblem(1, 9, "Rekurencia",
    m("""Mějme nekonečnou posloupnost §(F_1,F_2,F_3,\dots)§, jejíž členy pro každé §n\in\mathbb{N}§ splňují vztah §F_{n+2}=4F_{n+1}-4F_n§ a dále platí §F_1=2§ a §F_2=12§. Dokažte matematickou indukcí, že pro každé §n\in\mathbb{N}§ platí §F_n=2^n(2n-1)§."""),
    25, listOf(
        Rubric("ZK pre §n=1§ aj §n=2§", 2, "dml-mi-rek"),
        Rubric("Silná indukcia: IP pre §k\\in\\{1,\\dots,n\\}§, §n\\geq 2§", 2, "proof:dml-mi-p-rek"),
        Rubric("Rekurencia a IP pre §F_n§ aj §F_{n-1}§, vyznačené", 2, "dml-mi-rek"),
        Rubric("Úprava na §2^{n+1}(2n+1)§", 2, "dml-mi-rek"),
        Rubric("Záver podľa silného princípu MI", 1, "proof:dml-mi-p-rek"),
    ).map { it.copy(text = m(it.text)) },
    m("""Rovnosť platí pre všetky §n\in\mathbb{N}§, dôkaz silnou indukciou so ZK pre §n=1,2§."""), "dml-mi-rek",
    MiStrongPostup.let { pp ->
        val (claim, zk, ik, end) = pp
        pp.solve {
            claim("""Dokazujem silnou indukciou výrok §V(n)§ pre všetky §n\in\mathbb{N}§.""", eq("""F_n=2^n(2n-1)"""))
            zk("""Rekurencia siaha dva členy späť, preto overím §n=1§ aj §n=2§ dosadením do vzorca.""",
                eq("""2^1\cdot(2\cdot 1-1)=${rekF[0]}=F_1,\qquad 2^2\cdot(2\cdot 2-1)=${rekF[1]}=F_2"""))
            ik("""Nech §n\geq 2§ je pevné ľubovoľné a §V(k)§ platí pre všetky §k\in\{1,\dots,n\}§ (IP). §F_{n+1}§ rozpíšem rekurenciou a na oba členy použijem IP.""",
                eq("""F_{n+1}=4F_n-4F_{n-1}""", "Rekurencia platí, lebo §n-1\\geq 1§."),
                eq("""\stackrel{IP}{=}4\cdot 2^n(2n-1)-4\cdot 2^{n-1}(2n-3)""", "IP pre §k=n§ aj §k=n-1§, oba sú v §\\{1,\\dots,n\\}§."))
            ik("""Vytknem §2^{n+1}§ a dopočítam zátvorku.""",
                eq("""=2^{n+1}\big(2(2n-1)-(2n-3)\big)=2^{n+1}(2n+1)=2^{n+1}\big(2(n+1)-1\big)""", "To je pravá strana §V(n+1)§."))
            end("""Podľa silného princípu MI platí §F_n=2^n(2n-1)§ pre všetky §n\in\mathbb{N}§.""")
        }
    })

/** r_0, r_1, ... of the 2026 Ř recurrence; the answer "exactly from n = 2" is checked on the first terms. */
private val rekR: List<Long> = generateSequence(2L to 1L) { (a, b) -> b to b + 6 * a }.take(30).map { it.first }.toList()
    .also { r -> r.forEachIndexed { n, v -> check((v > miIpow(2, n + 1)) == (n >= 2)) { "r_$n" } } }

private val paper2026r = PaperProblem(1, 10, "Rekurencia a nerovnosť",
    m("""Mějme nekonečnou posloupnost §(r_0,r_1,r_2,\dots)§, jejíž členy pro každé §n\in\mathbb{N}_0§ splňují vztah §r_{n+2}=r_{n+1}+6r_n§ a dále platí §r_0=2§ a §r_1=1§. Určete pro která všechna čísla §n\in\mathbb{N}_0§ platí následující nerovnost. Vaše tvrzení pak dokažte matematickou indukcí. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad. §r_n>2^{n+1}§"""),
    25, listOf(
        Rubric("Prvé členy a odpoveď: práve pre §n\\geq 2§", 2, "dml-mi-rek"),
        Rubric("ZK pre §n=2§ a §n=3§", 2, "proof:dml-mi-p-rek"),
        Rubric("IK silnej indukcie, IP pre §k\\in\\{2,\\dots,n\\}§, §n\\geq 3§", 2, "proof:dml-mi-p-rek"),
        Rubric("Odhad §r_{n+1}>2^{n+1}+6\\cdot 2^n=2^{n+3}>2^{n+2}§ s vyznačeným IP", 3, "dml-mi-rek"),
        Rubric("Záver aj pre §n=0,1§", 1, "proof:dml-mi-p-rek"),
    ).map { it.copy(text = m(it.text)) },
    m("""§r_n>2^{n+1}§ práve pre §n\geq 2§ (§r_0=2§, §r_1=1§, §r_2=${rekR[2]}§)."""), "dml-mi-rek",
    MiStrongPostup.let { pp ->
        val (claim, zk, ik, end) = pp
        pp.solve {
            claim("""Spočítam prvé členy z rekurencie a porovnám ich s §2^{n+1}§.""",
                eq("""r_0=2\not>2,\quad r_1=1\not>4,\quad r_2=${rekR[2]}>8,\quad r_3=${rekR[3]}>16,\quad r_4=${rekR[4]}>32"""))
            claim("""Odhad: nerovnosť platí práve pre §n\geq 2§. Pre §n=0,1§ neplatí, to je výpočet vyššie; zvyšok dokážem silnou indukciou ako výrok §V(n)§ pre §n\geq 2§.""", eq("""r_n>2^{n+1}"""))
            zk("""Krok siaha dva členy späť, preto overím §n=2§ aj §n=3§.""", eq("""r_2=${rekR[2]}>2^3=8,\qquad r_3=${rekR[3]}>2^4=16"""))
            ik("""Nech §n\geq 3§ je pevné ľubovoľné a §r_k>2^{k+1}§ pre všetky §k\in\{2,\dots,n\}§ (IP). §r_{n+1}§ rozpíšem rekurenciou; IP použijem pre §k=n§ aj §k=n-1\geq 2§.""",
                eq("""r_{n+1}=r_n+6r_{n-1}\stackrel{IP}{>}2^{n+1}+6\cdot 2^n=2^n(2+6)=2^{n+3}>2^{n+2}""", "Sčítam dve ostré nerovnosti, druhú vynásobenú kladným §6§."))
            ik("""Vyšlo §r_{n+1}>2^{(n+1)+1}§, teda §V(n+1)§.""")
            end("""Podľa silného princípu MI platí §r_n>2^{n+1}§ pre všetky §n\geq 2§; pre §n=0,1§ neplatí. Odpoveď: práve pre §n\geq 2§.""")
        }
    })

private val paper2024h = PaperProblem(1, 9, "Nerovnosť so súčtom",
    m("""Určete pro která všechna přirozená čísla §n§ platí následující nerovnost. Vaše tvrzení pak dokažte matematickou indukcí. §\sum_{i=2}^{n}\frac{1}{i^2}<\frac{n-1}{n+1}§. Nezapomeňte uvést, jak vypadá Váš indukční krok, a kde využíváte indukční předpoklad."""),
    25, listOf(
        Rubric("Odpoveď §n\\geq 2§; pre §n=1§ je súčet prázdny a §0<0§ neplatí", 2, "dml-mi-suma"),
        Rubric("ZK pre §n=2§: §\\frac14<\\frac13§", 1, "dml-mi-suma"),
        Rubric("IP pre pevné ľubovoľné §n\\geq 2§ a cieľ §V(n+1)§", 2, "dml-mi-suma"),
        Rubric("Krok: §S_{n+1}=S_n+\\frac{1}{(n+1)^2}§, IP vyznačené a zdôvodnený odhad §\\frac{n-1}{n+1}+\\frac{1}{(n+1)^2}\\leq\\frac{n}{n+2}§", 3, "dml-mi-suma"),
        Rubric("Záver podľa princípu MI", 1, "dml-mi-suma"),
    ).map { it.copy(text = m(it.text)) },
    m("""Nerovnosť platí práve pre §n\geq 2§; pre §n=1§ je vľavo prázdny súčet §0§ a §0<0§ neplatí."""), "dml-mi-suma",
    MiWeakPostup.let { pp ->
        val (claim, zk, ik, end) = pp
        pp.solve {
            claim("""Pre §n=1§ je súčet od §i=2§ prázdny, teda §0§, a vpravo je §0§: §0<0§ neplatí. Pre §n=2,3§ platí, preto dokazujem výrok §V(n)§ pre všetky §n\geq 2§.""",
                eq("""\sum_{i=2}^{n}\frac{1}{i^2}<\frac{n-1}{n+1}"""))
            zk("""Pre §n=2§ je vľavo jediný člen.""", eq("""\frac{1}{4}<\frac{1}{3}=\frac{2-1}{2+1}"""))
            ik("""Nech §n\geq 2§ je pevné ľubovoľné a platí §V(n)§ (IP). Súčet do §n+1§ je súčet do §n§ a jeden nový člen.""",
                eq("""\sum_{i=2}^{n+1}\frac{1}{i^2}=\sum_{i=2}^{n}\frac{1}{i^2}+\frac{1}{(n+1)^2}\stackrel{IP}{<}\frac{n-1}{n+1}+\frac{1}{(n+1)^2}""", "Tu je IP."))
            ik("""Ešte ukážem, že to nie je viac než §\frac{n}{n+2}§, pravá strana §V(n+1)§. Vynásobím kladným §(n+1)^2(n+2)§, nerovnosť sa nezmení.""",
                eq("""\frac{n-1}{n+1}+\frac{1}{(n+1)^2}\leq\frac{n}{n+2}\ \Leftrightarrow\ (n-1)(n+1)(n+2)+(n+2)\leq n(n+1)^2"""),
                eq("""\Leftrightarrow\ (n+2)n^2\leq n^3+2n^2+n\ \Leftrightarrow\ 0\leq n""", "Platí, lebo §n\\geq 2§."))
            ik("""Spolu §\sum_{i=2}^{n+1}\frac{1}{i^2}<\frac{n}{n+2}=\frac{(n+1)-1}{(n+1)+1}§, teda §V(n+1)§.""")
            end("""Podľa slabého princípu MI platí nerovnosť pre všetky §n\geq 2§; pre §n=1§ neplatí. Odpoveď: práve pre §n\geq 2§.""")
        }
    })

/* ---- The topic ---------------------------------------------------------------------------------------------------- */

val DmlInduction = Topic(
    id = "dml-mi",
    lessons = listOf(slaba, zapis, nerovnosti, silna, matice),
    concepts = listOf(rekurentna),
    theorems = listOf(slaby, silny),
    proofs = listOf(pSuma, pDel, pNerov, pRek, pMatice, pPostupnost),
    generators = MiGenerators,
    templates = listOf(
        TemplateInfo("dml-mi-matica", "Mocnina matice", m("Odhad §A^n§ a dôkaz; príklad 1 písomiek 2023 až 2025, 9 až 10 b")),
        TemplateInfo("dml-mi-rek", "Rekurencia silnou indukciou", "Dva predošlé členy; písomky 2022 a 2023"),
        TemplateInfo("dml-mi-postupnost", "Postupnosť s odmocninou", m("Pre ktoré §n§ je §x_n<K§, silná indukcia; písomka 2025 (K)")),
        TemplateInfo("dml-mi-suma", "Súčet indukciou", "Cvičení 4.4, písomka 2021"),
        TemplateInfo("dml-mi-del", "Deliteľnosť indukciou", "Cvičení 4.1 a 4.2"),
        TemplateInfo("dml-mi-chyba", "Nájdi chybu v indukcii", "Posúď dôkaz a oprav ho; Cvičení 4.11"),
        TemplateInfo("dml-mi-krok", "Indukcia s krokom o k", "Koľko základných krokov treba a prečo"),
        TemplateInfo("dml-mi-induktivna", "Induktívne definovaná množina", "Ktoré čísla obsahuje, obe inklúzie; Cvičení 4.12"),
    ),
    paperProblems = mapOf("dml-2025-e" to paper2025e, "dml-2025-k" to paper2025k, "dml-2024-r" to paper2024r, "dml-2023-d" to paper2023d, "dml-2023-a" to paper2023a,
        "dml-2026-r" to paper2026r, "dml-2024-f" to paper2024f, "dml-2024-j" to paper2024j, "dml-2024-h" to paper2024h),
    generatedProblem = ::miGeneratedProblem,
    checkpoints = listOf(Checkpoint("dml-mi-silna", "dml-2023-a", 1, "Príklad 1 z písomky 6. 12. 2023", 10), Checkpoint("dml-mi-matice", "dml-2025-e", 1, "Príklad 1 z písomky 4. 12. 2025", 10)),
    planPapers = listOf("paper:dml-2025-e:1", "paper:dml-2023-a:1", "paper:dml-2025-k:1", "paper:dml-2024-r:1"),
)

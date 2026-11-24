package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dml.numbers.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.screens.dml.ClockMode
import dev.hamper.tabula.screens.dml.ClockPic
import dev.hamper.tabula.screens.dml.CoinsPic
import dev.hamper.tabula.screens.dml.CrtPic
import dev.hamper.tabula.screens.dml.DivisionPic
import dev.hamper.tabula.screens.dml.EuclidPic

/*
 * The seven lessons of Teorie čísel. Worked examples are the textbook's (Příklady 6.1 to 6.20, Cvičení 6.5 to 6.28)
 * or the past tests; numbers that come out of a computation are computed here by NumbersMath and checked in NumbersTest.
 */

private val rea254 = rea(254, 158)
private val rea431 = rea(431, 420)
private val rea137 = rea(13, 7)
private val ldr125 = ldr(7, 13, 125)!!
private fun signed(v: Long) = if (v < 0) "($v)" else "$v"
private fun plusMinus(v: Long) = if (v < 0) "$v" else "+$v"

internal val tcLessonDelitelnost = lesson("dml-tc-delitelnost", "Deliteľnosť a zvyšok", 14, "Kedy §a§ delí §b§, zvyšok aj pri zápornom čísle a čo presne je §\\gcd§.", art = 0, topics = listOf("TČ1")) {
    ask("""Koľko je §-17\bmod 5§?""", "§3§", "§-2§", "§2§", correct = 0,
        after = """Zvyšok je vždy z §\{0,\dots,4\}§: §-17=(-4)\cdot 5+3§.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Zvyšok nesmie byť záporný. §-17=(-3)\cdot 5-2§ síce platí, ale §r§ musí byť z §\{0,\dots,4\}§.""",
            2 to """To je zvyšok §17§, nie §-17§. Znamienko zvyšok mení: §-17=(-4)\cdot 5+3§."""))
    explore("""Skáč z nuly k §-17§ po §5§.""", """Hľadaj počet skokov §q§, aby zvyšok §r=-17-5q§ bol z §\{0,\dots,4\}§.""", DivisionPic(-17, 5),
        """§-17=(-4)\cdot 5+3§: posledný skok je až za §-17§ a zvyšok sa meria doprava.""")
    explain("Deliteľnosť: existuje celé §k§.", """§a|b§ znamená §a\cdot k=b§ pre nejaké §k\in\mathbb{Z}§. Nedelí sa, len násobí: preto §7|0§ aj §0|0§, ale §0\nmid 5§.""",
        cards = listOf("dml-tc-delitelnost"))
    clip("dml-tc-c-delitelnost")
    check("""Ktoré platí?""", """§-3\mid 12§""", """§0\mid 3§""", """§4\mid 6§""", correct = 0, why = """Áno, §k=-4§ je celé: §(-3)\cdot(-4)=12§.""",
        wrong = mapOf(1 to """Nula delí len nulu. §0\cdot k=0\neq 3§ pre každé §k§.""", 2 to """§k=\tfrac{3}{2}§ nie je celé. Definícia chce §k\in\mathbb{Z}§."""))
    thm("dml-tc-deleni")
    explain("Zvyšok je vždy nezáporný.", """Pre §d\in\mathbb{N}§ je §a\bmod d§ z §\{0,\dots,d-1\}§, aj pre záporné §a§. Podiel §q§ môže byť záporný, zvyšok nie.""",
        cards = listOf("dml-tc-zbytek"))
    worked("Zvyšok záporného čísla", """Urči podiel a zvyšok po delení §-23§ číslom §4§.""", "Skriptá, Věta 6.2 (dôkaz), Definice 6.2", TcDivisionPostup) {
        val (absAim, fix, check) = TcDivisionPostup
        absAim("""Najprv delím §|a|=23§ a potom vrátim znamienko, lebo delenie kladného čísla poznám (dôkaz Vety 6.2).""", eq("""23=5\cdot 4+3\quad\Rightarrow\quad -23=(-5)\cdot 4-3"""))
        fix("""§-3§ zvyšok nie je, zvyšok musí byť v §\{0,\dots,3\}§. Podiel zmenším o §1§ a zvyšok zväčším o §4§, podľa úpravy §(-q-1)d+(d-r)§ z dôkazu.""", eq("""-23=(-6)\cdot 4+(4-3)=(-6)\cdot 4+1"""))
        check("""Teda §q=-6§ a §r=-23\bmod 4=1§. Skúška: §-24+1=-23§.""")
    }
    check("""§-30\bmod 7§ je""", "§5§", "§-2§", "§2§", correct = 0, why = """§-30=(-5)\cdot 7+5§ a §0\leq 5<7§.""",
        wrong = mapOf(1 to """Zvyšok nesmie byť záporný. Pripočítaj §7§: §-2+7=5§.""", 2 to """To je §30\bmod 7§. Pre §-30§ platí §-30=(-5)\cdot 7+5§."""))
    prove("dml-tc-p-deleni")
    explain("§\\gcd§: delí ho každý spoločný deliteľ.", """Školská definícia „najväčší zo spoločných deliteľov“ má dieru: nulu delí každé číslo, takže §0§ a §0§ najväčší spoločný deliteľ nemajú. Kurz preto berie najväčší podľa deliteľnosti: §\gcd(a,b)§ je spoločný deliteľ z §\mathbb{N}_0§, ktorý je násobkom každého spoločného deliteľa. Tak vyjde aj §\gcd(0,0)=0§ a §\gcd(a,0)=|a|§ (Dombek).""",
        cards = listOf("dml-tc-gcd"), more = """Poznámka 6.1: §\gcd(0,0)=0§, §\lcm(a,0)=\lcm(0,0)=0§.""")
    clip("dml-tc-c-gcd")
    check("""§\gcd(-12,0)§ je""", "§12§", "§0§", "§-12§", correct = 0, why = """Nulu delí každé číslo, takže rozhodujú delitele §12§.""",
        wrong = mapOf(1 to """§0§ nedelí §-12§, teda nie je spoločný deliteľ. Spoločné delitele §a§ a §0§ sú delitele §a§, preto §\gcd(a,0)=|a|§.""", 2 to """§\gcd§ je z §\mathbb{N}_0§, nie záporný. §\gcd(-12,0)=\gcd(12,0)=12§."""))
    def("dml-tc-delitelnost")
    def("dml-tc-zbytek")
    def("dml-tc-gcd")
    check("""Sú §21§ a §50§ nesúdeliteľné?""", "Áno", "Nie", correct = 0, why = """Áno, §21=3\cdot 7§ a §50=2\cdot 5^2§ nemajú spoločné prvočíslo.""",
        wrong = mapOf(1 to """Sú nesúdeliteľné, lebo §\gcd(21,50)=1§. Nesúdeliteľnosť nechce prvočísla."""))
}

internal val tcLessonRea = lesson("dml-tc-rea", "Euklidov algoritmus a Bézout", 16, "§\\gcd§ bez rozkladu, tabuľka REA a Bézoutove koeficienty.", art = 1, topics = listOf("TČ1")) {
    ask("""Aký je §\gcd(254,158)§?""", "§2§", "§1§", "§4§", correct = 0,
        after = """§254=2\cdot 127§ a §158=2\cdot 79§. Čísla §127§ a §79§ sú prvočísla, preto majú spoločný len činiteľ §2§. Pri veľkých číslach sa rozklad hľadá ťažko; Euklidov algoritmus ho nepotrebuje.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Obe čísla sú párne, teda §2§ je spoločný deliteľ a §\gcd\geq 2§.""", 2 to """§4\nmid 254§: §254=4\cdot 63+2§."""))
    explore("""Odrezávaj štvorce z obdĺžnika §42\times 30§.""", "Každé ťuknutie je jedno delenie so zvyškom.", EuclidPic(42, 30),
        """Posledné štvorce majú stranu §6=\gcd(42,30)§.""", """Štvorec so stranou §d§ vyplní obdĺžnik presne vtedy, keď §d§ delí obe strany.""")
    explain("Odčítanie násobku §\\gcd§ nemení.", """§\gcd(a+cb,b)=\gcd(a,b)§ (Lemma 6.1), teda §\gcd(a,b)=\gcd(b,a\bmod b)§. Zvyšky klesajú, kým nepríde §0§; posledný nenulový je §\gcd§.""",
        cards = listOf("dml-tc-lemma61#3", "dml-tc-ea"))
    worked("Euklidov algoritmus", """§\gcd(254,158)§""", "Skriptá, Příklad 6.1", TcEuclidPostup) {
        val (divide, repeat, last) = TcEuclidPostup
        val ea = euclidWalk(rea254)
        divide("""Väčšie číslo vydelím menším so zvyškom: §${ea.rows[0].joinToString("")}§.""")
        repeat("""Ďalej delím vždy predošlého deliteľa predošlým zvyškom, kým nevyjde zvyšok §0§. Deliteľ a zvyšok sa posúvajú o riadok nižšie.""", Formula(ea))
        last("""Posledný nenulový zvyšok je §\gcd§: §\gcd(254,158)=${rea254.gcd}§.""")
    }
    check("Kde v Euklidovom algoritme nájdeš §\\gcd§?", "Posledný nenulový zvyšok", "Posledný zvyšok, teda §0§", "Prvý podiel §q§", correct = 0,
        why = "Posledný nenulový zvyšok; nula je len značka konca (Věta 6.3).",
        wrong = mapOf(1 to """Nula je značka konca, nie výsledok. Každé delenie zachová §\gcd§ dvojice (Lemma 6.1), takže §\gcd(a,b)=\gcd(r_k,0)=r_k§, posledný nenulový zvyšok.""", 2 to """Podiel všeobecne neurčuje §\gcd§. Slúži len na výpočet ďalšieho zvyšku."""))
    thm("dml-tc-bezout", "dml-tc-rea-veta")
    clip("dml-tc-c-bezout")
    explain("Tabuľka REA: každý riadok je rovnosť.", """Riadok §(r_i,\alpha_i,\beta_i)§ znamená §r_i=\alpha_i a+\beta_i b§. Prvé dva sú §(a,1,0)§ a §(b,0,1)§; každý ďalší je predpredošlý mínus §q§-násobok predošlého, ako krok GEM.""",
        formal = listOf(
            p("""Pro §a, b \in \mathbb{Z}§, §a \geq b > 0§ nalezněte §\gcd(a,b)§ a Bézoutovy koeficienty §\alpha,\beta\in\mathbb{Z}§ (splňující §\gcd(a,b)=\alpha\cdot a + \beta\cdot b§)."""),
            i("""Položíme §r_1 := a, r_2 := b§. Připravíme si tabulku o čtyřech sloupcích, do záhlaví napíšeme postupně §r_i, \alpha_i, \beta_i, q_i§."""),
            i("""Do prvního řádku zapíšeme za §r_1,\alpha_1,\beta_1,q_1§ postupně hodnoty §a,1,0,-§ (kódujeme rovnost §r_1=1\cdot a+0\cdot b§)."""),
            i("""Do druhého řádku zapíšeme za §r_2,\alpha_2,\beta_2,q_2§ postupně §b,0,1,\lfloor\tfrac{a}{b}\rfloor§ (kódujeme rovnost §r_2=0\cdot a+1\cdot b§ a navíc si ukládáme §q_2§, výsledek celočíselného podílu §a/b§)."""),
            i("""Pro řádek §i\geq 3§ položíme nejprve"""),
            b("""r_i := r_{i-2}-q_{i-1}\cdot r_{i-1}\,."""),
            i("""Pokud §r_i=0§, položíme §k=i-1§ a skončíme."""),
            i("""Pokud §r_i\neq 0§, vypočítáme dále se znalostí předchozích řádků:"""),
            b("""\begin{aligned}\alpha_i&:=\alpha_{i-2}-q_{i-1}\cdot\alpha_{i-1},\\\beta_i&:=\beta_{i-2}-q_{i-1}\cdot\beta_{i-1},\\q_i&:=\lfloor r_{i-1}/r_i\rfloor.\end{aligned}"""),
            i("""Opakujeme krok 4. pro další řádek (§i := i+1§)."""),
            p("""Výsledek je uložen v předposledním §k§-tém řádku tabulky (první tři sloupce) takto:"""),
            b("""\begin{aligned}\gcd(a,b)&=r_k,\\\alpha&=\alpha_k,\\\beta&=\beta_k,\end{aligned}"""),
            p("""kde §\alpha,\beta§ jsou koeficienty lineární kombinace §\gcd(a,b) = \alpha\cdot a+\beta\cdot b§. Pozor na pořadí §a,b§!"""),
            p("""Schematicky vypadá zápis algoritmu následovně:"""),
            b("""\begin{matrix|1} r_i & \alpha_i & \beta_i & q_i \\ a & 1 & 0 & - \\ b & 0 & 1 & q_2=\lfloor\tfrac{a}{b}\rfloor \\ r_3=a-q_2\cdot b & 1-q_2\cdot 0 & 0-q_2\cdot 1 & q_3 \\ \dots & \dots & \dots & \dots \\ r_k=\gcd(a,b) & \alpha & \beta & q_k \\ r_{k+1}=0 & - & - & - \end{matrix}"""),
        ), src = "Skriptá, Poznámka 6.2")
    worked("REA tabuľkou", """§\gcd(254,158)§ a Bézoutove koeficienty""", "Skriptá, Příklad 6.3", TcBezoutPostup) {
        val (table, bez, check) = TcBezoutPostup
        table("""Nový riadok je predpredošlý mínus §q§-násobok predošlého, vo všetkých troch stĺpcoch.""", Formula(reaWalk(rea254)))
        bez("""Riadok nad nulou dáva §\gcd§ aj koeficienty.""", eq("""2=${rea254.alpha}\cdot 254+(${rea254.beta})\cdot 158"""))
        check("""Skúška dosadením: §${rea254.alpha * 254}-${-rea254.beta * 158}=2§.""")
    }
    check("""Riadok tabuľky REA pre §a=254§, §b=158§ je §(34,\ 2,\ -3)§. Čo znamená?""", """§34=2\cdot 254-3\cdot 158§""", """§34=2\cdot 158-3\cdot 254§""", """§\gcd(254,158)=34§""", correct = 0,
        why = """§2\cdot 254-3\cdot 158=508-474=34§: §\alpha§ patrí k §a§, §\beta§ k §b§.""",
        wrong = mapOf(1 to """Prehodil si §a§ a §b§. §\alpha§ patrí k §a=254§.""", 2 to """§34§ je len medzivýsledok. §\gcd§ je v poslednom riadku pred nulou."""))
    practice("dml-tc-rea", solo = false)
    check("""Tabuľka REA počíta s §60§ a dá pri ňom koeficient §-5§. Aký je koeficient pri §-60§?""", "§5§", "§-5§", "§60§", correct = 0,
        why = """§(-5)\cdot 60=5\cdot(-60)§: pri zápornom čísle sa koeficient otočí (Cvičení 6.5).""",
        wrong = mapOf(1 to """§(-5)\cdot(-60)=300§, nie §-300§. Pri zápornom čísle sa znamienko koeficientu otočí.""", 2 to """Koeficient nie je samotné číslo. §(-5)\cdot 60=5\cdot(-60)§, teda §5§."""))
    explain("Najmenší spoločný násobok z §\\gcd§.", """Pre §(a,b)\neq(0,0)§ je §\lcm(a,b)=\tfrac{|a|\cdot|b|}{\gcd(a,b)}§, a §\lcm(0,0)=0§. Napr. §\lcm(420,231)=\tfrac{420\cdot 231}{21}=4620§ (Cvičení 6.5).""",
        cards = listOf("dml-tc-gcd-lcm"))
    check("""§\lcm(12,18)§ je""", "§36§", "§216§", "§6§", correct = 0, why = """§\tfrac{12\cdot 18}{6}=36§.""",
        wrong = mapOf(1 to """To je súčin §12\cdot 18§. Treba ho vydeliť §\gcd=6§.""", 2 to """To je §\gcd(12,18)§, nie §\lcm§."""))
    practice("dml-tc-rea", solo = true)
}

internal val tcLessonLdr = lesson("dml-tc-diofant", "Diofantické rovnice", 15, "Kedy má §ax+by=c§ celočíselné riešenie a ako nájsť všetky.", art = 2, topics = listOf("TČ2")) {
    ask("""Dá sa mincami po §6§ a §9§ zaplatiť presne §20§, aj s vydávaním?""", "Nie", "Áno", correct = 0,
        after = """Každá suma §6x+9y§ je násobok §3§, a §20§ nie je.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Riešenie neexistuje: §6x+9y=3(2x+3y)§ je násobok §3§, ale §20§ nie. Vydávanie na tom nič nemení."""))
    explore("""Zaplať §4§ mincami po §7§ a §13§.""", "Záporný počet: tie mince dostaneš späť.", CoinsPic(7, 13, 4),
        """§7\cdot(-5)+13\cdot 3=4§: dáš tri po §13§ a dostaneš päť po §7§ späť.""")
    explain("Riešenie je práve pri §\\gcd(a,b)\\mid c§.", """§\gcd(a,b)§ delí ľavú stranu, preto musí deliť aj §c§. Naopak pre §(a,b)\neq(0,0)§ dá Bézout §\gcd=\alpha a+\beta b§; vynásob §\tfrac{c}{\gcd}§.""",
        cards = listOf("dml-tc-ldr-res", "dml-tc-ldr-navod"))
    clip("dml-tc-c-ldr")
    check("""Má §4x+6y=9§ celočíselné riešenie?""", "Áno", "Nie", correct = 1, why = """Nie: §\gcd(4,6)=2§ nedelí §9§.""",
        wrong = mapOf(0 to """Riešenie neexistuje: §4x+6y§ je vždy párne, §9§ nie. Všeobecne §\gcd(4,6)=2§ delí každú kombináciu §4x+6y§, preto by musel deliť aj pravú stranu (Věta 6.7)."""))
    check("""Dá sa presýpacími hodinami na §11§ a §5§ minút odmerať §7§ minút?""", """Áno, §\gcd(11,5)§ je §1§ a delí §7§""", """Nie, §7§ nie je násobok §5§ ani §11§""", correct = 0,
        why = """Áno, napr. §2\cdot 11-3\cdot 5=7§ (Cvičení 6.9).""",
        wrong = mapOf(1 to """Stačí kombinácia s celými koeficientmi: §2\cdot 11-3\cdot 5=7§ (Věta 6.7)."""))
    worked("Jedno riešenie", """Pre §x,y\in\mathbb{Z}§: §7x+13y=125§""", "Skriptá, Příklad 6.5", TcLdrOnePostup) {
        val (gcdAim, exists, one) = TcLdrOnePostup
        gcdAim("""REA zapíšem v poradí rovnice: §\alpha§ pri §7§, §\beta§ pri §13§.""",
            Formula(reaWalk(rea137, swap = true)))
        val (x0, y0) = ldr125.x0 to ldr125.y0
        exists("""§\gcd(7,13)=${ldr125.g}§ delí §125§, preto riešenie existuje: Bézoutovu rovnosť pre §1§ stačí vynásobiť §125§ (Věta 6.7).""")
        one("""Bézoutovu rovnosť vynásobím §125§, lebo tak sa ľavá strana §1§ zmení na §125§ (Důsledek 6.1).""", eq("""125=${signed(x0)}\cdot 7+${signed(y0)}\cdot 13\quad\Rightarrow\quad(x_0,y_0)=($x0,$y0)"""))
        one("""Skúška: §${7 * x0}${plusMinus(13 * y0)}=${7 * x0 + 13 * y0}§.""")
    }
    thm("dml-tc-ldr-res")
    explain("Všetky riešenia: krok §(b/\\gcd,-a/\\gcd)§.", """Pre §a,b\neq 0§ (Věta 6.8): k jednému riešeniu pripočítaj ľubovoľný celý násobok riešenia §ax+by=0§. Najmenší krok je §(\tfrac{b}{\gcd},-\tfrac{a}{\gcd})§; bez krátenia by si riešenia stratil.""",
        formal = listOf(
            p("""Jaká má přidružená homogenní LDR (§ax + by = 0§) všechna řešení? Jistě například §(x,y) = (b,-a)§, nebo dokonce každý celočíselný násobek §(x,y) = k \cdot (b, -a)§. Vypadají tak všechna řešení?"""),
            p("""Ne – například §6x+4y=0§ má jistě mezi řešeními i dvojici §(2,-3)§, ačkoli ta nelze zapsat jako „celé číslo krát §(4,-6)§“. V případě, že koeficienty §a,b§ nejsou nesoudělné, musíme rovnici zkrátit. A jak nám říká Lemma 6.2, zkrátíme-li dvojici celých čísel jejich největším společným dělitelem, dostaneme dvojici nesoudělných čísel."""),
            p("""Rovnice"""),
            b("""\left(\frac{a}{\gcd(a,b)}\right)x + \left(\frac{b}{\gcd(a,b)}\right)y = 0"""),
            p("""má tak koeficienty nejen celočíselné, ale navíc nesoudělné. Dá se ukázat, že pokud teď budeme brát všechny dvojice ve tvaru §k\cdot\Big(\frac{b}{\gcd(a,b)},\frac{-a}{\gcd(a,b)}\Big)§, kde §k\in\mathbb{Z}§, již dostaneme všechna řešení přidružené homogenní LDR."""),
            p("""Co nám tedy Věta 6.8 vlastně říká o všech řešeních lineární diofantické rovnice, která alespoň jedno řešení má, použijeme-li nové pojmy? Říká, že §(x,y)\in\mathbb{Z}^2§ je řešením, právě tehdy, pokud se rovná"""),
            // a display formula as in the textbook (chap-06-tc.woo:1351, its underbraces left out), not inside the paragraph
            b("""(x_0,y_0) \softbreak[1]+ k \cdot \left(\frac{b}{\gcd(a,b)}, \frac{-a}{\gcd(a,b)}\right),"""),
            p("""tedy „pokud je součtem nějakého partikulárního řešení a nějakého řešení přidružené homogenní rovnice“."""),
        ), src = "Skriptá, Poznámka 6.5", cards = listOf("dml-tc-ldr-vsetky"))
    check("""Pre §x,y,k\in\mathbb{Z}§: Všetky riešenia §6x+4y=0§ sú""", """§k\cdot(2,-3)§""", """§k\cdot(4,-6)§""", """§k\cdot(6,-4)§""", correct = 0,
        why = """Najprv krátiš §\gcd=2§: §3x+2y=0§, krok §(2,-3)§.""",
        wrong = mapOf(1 to """Bez krátenia stratíš riešenia: §(2,-3)§ nie je celý násobok §(4,-6)§ (Poznámka 6.5).""", 2 to """§6\cdot 6+4\cdot(-4)=20\neq 0§. Krok musí riešiť homogénnu rovnicu."""))
    worked("Riešenie bez vydávania", """Pre §x,y\in\mathbb{Z}§: §7x+13y=125§ s §x,y\geq 0§""", "Skriptá, Příklad 6.6", TcLdrNonnegPostup) {
        val (all, cond, pick) = TcLdrNonnegPostup
        val l = ldr125
        val step = "(${l.sx},${l.sy})"
        val ks = (-l.x0.floorDiv(l.sx)..l.y0.floorDiv(-l.sy)).reversed()
        all("""Všetky riešenia sú jedno riešenie plus násobky §$step§, lebo rozdiel dvoch riešení rieši §7x+13y=0§. Z §7x=-13y§ a nesúdeliteľnosti §7§ a §13§ je §x§ násobok §13§, preto §(x,y)=k\cdot$step§ (Věta 6.8).""", eq("""(x,y)=(${l.x0},${l.y0})+k\cdot$step,\quad k\in\mathbb{Z}"""))
        cond("""Obe zložky majú byť nezáporné, z toho dve nerovnosti pre §k§.""", eq("""${l.x0}+${l.sx}k\geq 0\ \wedge\ ${l.y0}${l.sy}k\geq 0"""), eq("""\Rightarrow\quad k\in\{${ks.sorted().joinToString(",")}\}"""))
        pick("""Dosadím ${if (ks.count() == 2) "obe hodnoty" else "všetky hodnoty"} §k§.""", ks.map { k -> eq("""(${l.x0},${l.y0})${plusMinus(k)}\cdot$step=(${l.x0 + k * l.sx},${l.y0 + k * l.sy})""", if (l.x0 + k * l.sx == 16L) "Riešenie zo skrípt." else null) })
        pick("""${if (ks.count() == 2) "Obe riešenia sú" else "Všetky sú"} nezáporné.""")
    }
    practice("dml-tc-ldr", solo = false)
    check("""Pre §x,y,k\in\mathbb{Z}§: Všetky riešenia §4x+6y=10§ sú §(1,1)+k\cdot(s,-t)§. Koľko je §(s,t)§?""", "§(3,2)§", "§(6,4)§", "§(2,3)§", correct = 0,
        why = """§\gcd(4,6)=2§, krok §(\tfrac{6}{2},-\tfrac{4}{2})=(3,-2)§.""",
        wrong = mapOf(1 to """Nevydelil si §\gcd=2§. Vynecháš napr. riešenie §(4,-1)§.""", 2 to """Prehodil si: pri §x§ je §\tfrac{b}{\gcd}=3§, pri §y§ je §\tfrac{a}{\gcd}=2§."""))
    def("dml-tc-ldr")
    practice("dml-tc-ldr", solo = true)
}

internal val tcLessonKongruencie = lesson("dml-tc-kongruencie", "Kongruencie a inverzia", 17, "Kongruencia, počítanie so zvyškami a inverzia v §\\mathbb{Z}_m§ cez REA.", art = 3, topics = listOf("TČ2", "TČ3")) {
    ask("""Aký zvyšok dá §241\cdot 242§ po delení §243§?""", "§2§", "§1§", "§241§", correct = 0,
        after = """§241\equiv -2§ a §242\equiv -1\pmod{243}§, takže súčin je §\equiv(-2)\cdot(-1)=2§. Činitele smieš nahradiť kongruentnými číslami, lebo kongruencia sa pri násobení zachová (Tvrzení 6.6). Tak to rátal Spěvák na 7. prednáške.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """§1§ by vyšlo z §(-1)\cdot(-1)§. Ale §241=243-2§, teda §241\equiv -2§, a súčin je §\equiv 2§.""",
            2 to """§241§ by vyšlo pri §242\equiv 1§. Ale §242\equiv -1§, teda súčin je §\equiv -241\equiv 2§."""))
    explain("Kongruencia: §m§ delí rozdiel.", """Keď nás zaujíma len zvyšok, rovnosť nahradíme slabším vzťahom: §a\equiv b\pmod{m}§, keď §m\mid(a-b)§, teda keď sa čísla líšia o celý násobok §m§. Dombek pripomína, že definícia berie §m\in\mathbb{N}§ a povoľuje aj §m=1§, hoci je to zbytočné.""",
        src = "Dombek, 8. prednáška", cards = listOf(tcKongruence.id))
    clip("dml-tc-c-kongruence")
    def("dml-tc-kongruence")
    clip("dml-tc-c-zapis")
    check("""Ako súvisia §a\equiv b\pmod{m}§ a §b=a\bmod m§?""", "Z druhého plynie prvé, opačne nie", "Sú ekvivalentné", "Z prvého plynie druhé, opačne nie", correct = 0,
        why = """Zápis §b=a\bmod m§ tvrdí viac: §b§ je ten jediný zvyšok z §\{0,\dots,m-1\}§ (Dombek).""",
        wrong = mapOf(1 to """Nie sú: §12\equiv 7\pmod{5}§ platí, ale §7=12\bmod 5§ nie, lebo §12\bmod 5=2§.""",
            2 to """Opačne. §12\equiv 7\pmod 5§ platí, a predsa §7\neq 12\bmod 5=2§."""))
    thm("dml-tc-kongruence-ekv")
    prove("dml-tc-p-kongruence-ekv")
    thm("dml-tc-operace")
    worked("Redukcia modulo §m§", """§§(124567\cdot 21546)+312^5\bmod 5§§""", "Skriptá, Příklad 6.10; Dombek, 8. prednáška", TcReducePostup) {
        val (each, compute) = TcReducePostup
        each("""Každé číslo nahradím jeho zvyškom po delení §5§, lebo kongruencia sa pri sčítaní aj násobení zachová (Tvrzení 6.6).""", eq("""124567\equiv 2,\quad 21546\equiv 1,\quad 312\equiv 2\pmod 5"""))
        compute("""So zvyškami počítam ďalej a výsledok znova zredukujem.""", eq("""(124567\cdot 21546)+312^5"""), eq("""\equiv(2\cdot 1)+2^5=34\equiv 4\pmod 5"""))
        compute("""§=§ píšem len pri skutočnej rovnosti, inak §\equiv§ s modulom v zátvorke. V jednom príklade sa totiž modul môže zmeniť (Varování 6.2, Dombek).""")
    }
    check("""Koľko je §665\cdot 7\bmod 666§?""", "§659§", "§7§", "§-7§", correct = 0,
        why = """§665\equiv -1\pmod{666}§, teda súčin je §\equiv -7\equiv 659§.""",
        wrong = mapOf(1 to """To by platilo pre §665\equiv 1§. Ale §665=666-1§, teda §665\equiv -1§ a súčin §\equiv -7§.""",
            2 to """§-7§ je kongruentné, ale zvyšok musí byť z §\{0,\dots,665\}§: §-7+666=659§."""))
    explain("Čo sa stane v §\\mathbb{Z}_m§, zostane v §\\mathbb{Z}_m§.", """Tak to Dombek nazval: v §\mathbb{Z}_m§ sčítaš alebo vynásobíš ako v §\mathbb{Z}§ a vezmeš zvyšok. Redukovať smieš aj medzivýsledky, výsledok sa nezmení (Pozorování 6.2). Takto ste počítali v LA1 nad §\mathbb{Z}_p§.""",
        src = "Dombek, 8. prednáška", cards = listOf(tcZm.id))
    def("dml-tc-zm")
    explain("Odčítať vieme vždy, deliť nie.", """Opačný prvok k §a\neq 0§ je §m-a§, k §0§ je to §0§. Delenie znamená hľadať §x§ s §a\cdot_m x=1§, multiplikatívnu inverziu §a^{-1}§. Dombek: „inverzia“ bez prívlastku je obvykle multiplikatívna.""",
        src = "Dombek, 8. prednáška", cards = listOf(tcInverze.id))
    def("dml-tc-inverze")
    explore("""Hľadaj §3^{-1}§ v §\mathbb{Z}_{10}§.""", """Skúšaj násobky §3k\bmod 10§, kým nepríde §1§.""", ClockPic(10, 3, ClockMode.Multiples, 1),
        """§3\cdot 7=21\equiv 1§: §3^{-1}=7§ v §\mathbb{Z}_{10}§.""")
    explore("""Teraz §3§ v §\mathbb{Z}_{12}§.""", """Choď po násobkoch, kým sa nevrátiš do §0§.""", ClockPic(12, 3, ClockMode.Multiples, 0),
        """Len §0,3,6,9§: §1§ nikdy, lebo §\gcd(3,12)=3§. Inverzia neexistuje (Příklad 6.8).""")
    thm("dml-tc-inverze")
    prove("dml-tc-p-inverze")
    worked("Inverzia cez REA", """§420^{-1}§ v §\mathbb{Z}_{431}§""", "Písomka 4. 12. 2025 (variant E), príklad 3", TcInvPostup) {
        val (exists, reaAim, result) = TcInvPostup
        val inv = md(rea431.beta, 431)
        exists("""§420=2^2\cdot 3\cdot 5\cdot 7§ a žiadne z týchto prvočísel nedelí §431§, preto §\gcd(420,431)=1§ a inverzia existuje (Věta 6.12). Potvrdí to aj REA nižšie.""")
        reaAim("""Hľadám §x§ so §420x\equiv 1\pmod{431}§, teda riešenie §420x+431y=1§, lebo kongruencia znamená §431\mid 420x-1§. REA začnem väčším číslom §431§:""",
            Formula(reaWalk(rea431)))
        result("""REA dala §x_0=${rea431.beta}§. Všetky riešenia sú §x_0+k\cdot 431§, lebo §\gcd=1§ (Věta 6.8), a do §\{0,\dots,430\}§ padne práve jedno.""",
            eq("""420^{-1}=${rea431.beta}+431=$inv"""), eq("""420\cdot $inv=${420L * inv}=${420L * inv / 431}\cdot 431+1""", "Skúška: zvyšok §1§."))
    }
    thm("dml-tc-inverze-vsetky")
    clip("dml-tc-c-z23")
    check("""V ktorom §\mathbb{Z}_m§ má inverziu každé nenulové číslo?""", """§\mathbb{Z}_{23}§""", """§\mathbb{Z}_{21}§""", """§\mathbb{Z}_{25}§""", correct = 0,
        why = """§23§ je prvočíslo, takže každé §a\in\{1,\dots,22\}§ je s ním nesúdeliteľné (Důsledek 6.2).""",
        wrong = mapOf(1 to """§21=3\cdot 7§: napríklad §3§ je s §21§ súdeliteľné, inverziu nemá.""",
            2 to """§5\mid 25§: §\gcd(5,25)=5§, preto §5§ inverziu nemá."""))
    practice("dml-tc-inv", solo = false)
    practice("dml-tc-inv-min", solo = true)
}

/*
 * The last three lessons follow Spěvák's 8. prednáška (paralelka 2, 18. 11. 2026, captions checked 19. 11.): cancelling
 * with a change of modulus, linear congruences, systems through the generalised Chinese theorem, then Euler's function,
 * Euler's theorem with its proof and Fermat as its corollary. Times [h:mm:ss] are in that recording.
 */
internal val tcLessonLkon = lesson("dml-tc-lkon", "Lineárne kongruencie", 16, "Krátenie so zmenou modulu a riešenie §ax\\equiv b§ ako rovnice: vydeliť znamená vynásobiť inverziou.", art = 5, topics = listOf("TČ3")) {
    ask("""§3x\equiv 9\pmod{15}§. Je §x=8§ riešenie?""", "Áno", "Nie", correct = 0,
        after = """Áno, §3\cdot 8=24\equiv 9\pmod{15}§. Krátením §3§ bez zmeny modulu by si ho stratil.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Dosaď: §24-9=15§ je násobok §15§. Je to riešenie."""))
    explore("""Násobky §3§ v §\mathbb{Z}_{15}§.""", """Hľadaj §k§ s §3k\equiv 9§.""", ClockPic(15, 3, ClockMode.Multiples, 9),
        """§k=3§, ale aj §8§ a §13§: tri riešenia s krokom §15/3=5§.""")
    explain("Krátiť smieš, ale modul sa delí.", """§ac\equiv bc\pmod m§ je to isté ako §a\equiv b\pmod{\tfrac{m}{\gcd(m,c)}}§. Spěvák: §1\cdot 3\equiv 5\cdot 3\pmod{12}§, ale §1\not\equiv 5\pmod{12}§. Krátim §3§ a modul delím §\gcd(3,12)=3§: §1\equiv 5\pmod 4§. Pri §\gcd(m,c)=1§ ostane modul §m§.""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-kraceni"))
    clip("dml-tc-c-kraceni-modul")
    thm("dml-tc-lemma62")
    prove("dml-tc-p-kraceni")
    check("""§9x\equiv 12\pmod{15}§ po krátení §3§ je""", """§3x\equiv 4\pmod 5§""", """§3x\equiv 4\pmod{15}§""", """§x\equiv 4\pmod 5§""", correct = 0,
        why = """Krátim §3§ a modul delím §\gcd(15,3)=3§ (Věta 6.13).""",
        wrong = mapOf(1 to """Modul sa delí §\gcd(15,3)=3§. So starým modulom by nebolo ani riešenie.""", 2 to """Vydelil si §9§ deviatimi, §12§ len tromi. Kráti sa to isté číslo na oboch stranách."""))
    def("dml-tc-lkon")
    thm("dml-tc-lkon-res", "dml-tc-lkon-pocet")
    explain("Vydeliť znamená vynásobiť inverziou.", """Kongruenciu riešiš ako lineárnu rovnicu: najprv overíš, či §d=\gcd(a,m)§ delí §b§. Ak nie, riešenie neexistuje; inak krátiš §d§ aj s modulom. Potom sú koeficient a nový modul nesúdeliteľné. Ak nový modul vyjde §1§, vyhovuje každé celé §x§; inak má koeficient inverziu a obe strany ňou vynásobíš. Takto Spěvák dokázal Větu 6.17. Inverziu nájdeš REA, pri malom module aj z hlavy.""",
        formal = listOf(
            p("""Úlohy nalezení inverze a řešení lineární kongruence spolu těsně souvisí, každou z nich lze vyřešit pomocí druhé."""),
            i("""(řešení kongruencí §\to§ hledání inverze): Jak známo, nalézt multiplikativní inverzi k zadanému §a \in \mathbb{Z}_m§ znamená nalézt jediné §x \in \mathbb{Z}_m§ (v §\mathbb{Z}§ jich, pokud existuje nějaké, existuje nekonečně mnoho) splňující kongruenci §a \cdot x \equiv 1 \pmod{m}§. Je to tedy speciální případ řešení lineární kongruence! Jednak pro pravou stranu §b=1§, jednak nás zajímá pouze řešení v §\mathbb{Z}_m§."""),
            i("""(znalost inverzí §\to§ vyřešení kongruence): Mějme zadánu kongruenci §a \cdot x \equiv b \pmod{m}§. Přičtení celočíselného násobku modula k libovolné straně kongruence (například §mx, 2mx, -mx, \ldots§) je ekvivalentní úprava. Proto můžeme kongruenci převést na ekvivalentní §a' \cdot x \equiv b \pmod{m}§, kde pro stručnost značíme §a':=a \bmod{m}§. Je-li koeficient §a'§ nesoudělný s modulem §m§, má inverzi. Pokud tuto inverzi známe (nebo ji dokážeme snadno nalézt), zadanou kongruenci vyřešíme snadno rozšířením:"""),
            b("""\begin{aligned}&a \cdot x \equiv b \pmod{m}\\\Leftrightarrow\ &a' \cdot x \equiv b \pmod{m}\\\Leftrightarrow\ &(a')^{-1} \cdot a' \cdot x\\&\quad\equiv (a')^{-1} \cdot b \pmod{m}\\\Leftrightarrow\ &x \equiv (a')^{-1} \cdot b \pmod{m}.\end{aligned}"""),
        ), src = "Skriptá, Poznámka 6.9; Spěvák, 8. prednáška")
    worked("Lineárna kongruencia zo Spěvákovej prednášky", """§12x\equiv 18\pmod{75}§""", "Spěvák, 8. prednáška", tcLkon(TcLkonTask(12, 18, 75)).solution(emptyList()))
    clip("dml-tc-c-lkon-deleni")
    check("""Spěvák krátil §12x\equiv 18\pmod{75}§ aj šiestimi. Čo dostal?""", """§2x\equiv 3\pmod{25}§""", """§2x\equiv 3\pmod{75}§""", "Šiestimi krátiť nesmie", correct = 0,
        why = """Modul sa delí §\gcd(75,6)=3§, nie šiestimi (Věta 6.13).""",
        wrong = mapOf(1 to """§\gcd(75,6)=3\neq 1§, preto sa modul zmení: §75/3=25§. Modulo §75§ by si stratil riešenia §14§ a §64§: napríklad §2\cdot 14=28\not\equiv 3\pmod{75}§.""",
            2 to """Krátiť smieš ľubovoľným §c§, len modul vydelíš §\gcd(m,c)=3§. Spěvák: §2\cdot 13=26\equiv 1§, teda §x\equiv 39\equiv 14\pmod{25}§."""))
    check("""Koľko riešení má §6x\equiv 4\pmod{10}§ v §\mathbb{Z}_{10}§?""", "§2§", "§1§", "§0§", correct = 0,
        why = """§\gcd(6,10)=2§ delí §4§, teda dve riešenia: §4§ a §9§.""",
        wrong = mapOf(1 to """Jedno je len pri §\gcd(a,m)=1§. Tu §\gcd=2§: §4§ aj §9§.""", 2 to """§\gcd(6,10)=2§ delí §4§, riešenie existuje: §6\cdot 4=24\equiv 4§."""))
    practice("dml-tc-lkon", solo = false)
    check("""Všetky riešenia §4x\equiv 4\pmod{12}§ sú""", """§x\equiv 1\pmod 3§""", """§x\equiv 1\pmod{12}§""", """§x=1§""", correct = 0,
        why = """§\gcd(4,12)=4§ delí §4§: krátim a modul je §12/4=3§ (Cvičení 6.23 e).""",
        wrong = mapOf(1 to """Modul sa delí §\gcd(4,12)=4§. Chýbajú riešenia §4§ a §7§.""", 2 to """Riešení je nekonečne veľa: §1,4,7,10,\dots§ aj záporné."""))
    check("""Riešenie §14x\equiv 7\pmod{35}§ je""", """§x\equiv 3\pmod 5§""", "Nemá riešenie", """§x\equiv 3\pmod{35}§""", correct = 0,
        why = """§\gcd(14,35)=7§ delí §7§: §2x\equiv 1\pmod 5§, teda §x\equiv 3§.""",
        wrong = mapOf(1 to """§\gcd(14,35)=7§ delí §7§, riešenie existuje.""", 2 to """Po krátení §7§ je modul §35/7=5§. Vyhovujú aj §8§ či §13§."""))
    practice("dml-tc-lkon", solo = true)
}

internal val tcLessonCvoz = lesson("dml-tc-cvoz", "Sústavy kongruencií", 18, "Zovšeobecnená čínska veta, §\\lcm§ viac čísel a dosadzovanie ako na prednáške.", art = 6, topics = listOf("TČ3")) {
    ask("""Číslo dáva zvyšok §1§ po delení §2§, §2§ po delení §3§ a §3§ po delení §5§. Koľko takých je medzi §0§ a §29§?""", "Práve jedno", "Žiadne", "Viac", correct = 0,
        after = """Je to §23§, jediné v §\mathbb{Z}_{30}§ (Příklad 6.19).""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Moduly sú nesúdeliteľné, riešenie existuje vždy: §23§.""", 2 to """Medzi §0§ a §29§ je jediné, ďalšie je až §53§."""))
    explore("""Nájdi §x§ so zvyškom §2§ modulo §3§ a §3§ modulo §5§.""", """Riadok je §x\bmod 3§, stĺpec §x\bmod 5§. Skoky §+3§ držia riadok.""", CrtPic(3, 5, 2, 3),
        """§x=8§. Každá bunka má práve jedno číslo z §\mathbb{Z}_{15}§: to je čínska veta.""")
    explain("Jedna neznáma, viac modulov.", """Sústava kongruencií nie je sústava rovníc: hľadáme jedno neznáme celé §x§, ktoré spĺňa všetky kongruencie naraz, každú vo vlastnom module. Riešenie sa opakuje s krokom §\lcm§ všetkých modulov. To je spoločný násobok, ktorý delí každý iný spoločný násobok. Počítaš ho postupne, §\lcm(\lcm(m_1,m_2),m_3)§, alebo z rozkladu na prvočísla.""",
        formal = cz("""Číslo §n\in\mathbb{N}_0§ je nejmenší společný násobek čísel §m_1, m_2, \ldots m_N§, pokud je jejich společný násobek, tedy §(\forall i\in\{1,\dots,N\})(m_i | n)§, a navíc pokud dělí každý další jejich společný násobek. Značíme jej stejně, §n = \lcm(m_1, m_2, \ldots, m_N)§."""),
        src = "Skriptá, Varování 6.4; Spěvák, 8. prednáška")
    check("""§\lcm(6,10,15)§ je""", "§30§", "§900§", "§60§", correct = 0,
        why = """§\lcm(6,10)=30§ a §\lcm(30,15)=30§. Z rozkladov §6=2\cdot 3§, §10=2\cdot 5§, §15=3\cdot 5§ vezmeš každé prvočíslo v najvyššej mocnine: §2\cdot 3\cdot 5=30§.""",
        wrong = mapOf(1 to """To je súčin. Moduly sú súdeliteľné, §\lcm§ je menší: §\lcm(\lcm(6,10),15)=\lcm(30,15)=30§.""",
            2 to """§60§ je spoločný násobok, ale nie najmenší: §30§ je deliteľné §6§, §10§ aj §15§."""))
    explain("Zovšeobecnená čínska veta", """Spěvák začal všeobecnou vetou. Riešenie existuje práve vtedy, keď pre každú dvojicu kongruencií §\gcd(m_i,m_j)§ delí §a_i-a_j§. Potom je jediné modulo §\lcm§ modulov. Dôkaz preskočil, skriptá ho nepožadujú.""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-zcvoz"))
    clip("dml-tc-c-zcvoz")
    check("""Má §x\equiv 1\pmod 6§, §x\equiv 2\pmod 4§ riešenie?""", "Nie", "Áno", correct = 0,
        why = """Nie, §\gcd(6,4)=2§ nedelí §1-2§: §x§ by bolo nepárne aj párne.""",
        wrong = mapOf(1 to """Prvá chce nepárne §x§, druhá párne. §\gcd(6,4)=2§ nedelí §-1§ (Věta 6.20)."""))
    explain("Čínska veta je špeciálny prípad.", """Ak sú moduly navzájom nesúdeliteľné, každé §\gcd(m_i,m_j)=1§ delí rozdiel a podmienka platí sama. §\lcm§ je vtedy súčin modulov. Spěvák ju len vyslovil, bez dôkazu; skriptá ho majú (vzorec s §M_i§) a nájdeš ho pri vete.""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-cvoz"))
    check("""Sústava §x\equiv 3\pmod 4§, §x\equiv 1\pmod 6§: modulo čoho je riešenie jediné?""", "§12§", "§24§", "§2§", correct = 0,
        why = """Modulo §\lcm(4,6)=12§; §\gcd=2§ delí §3-1§, riešenie je §x\equiv 7§.""",
        wrong = mapOf(1 to """Súčin platí len pre nesúdeliteľné moduly. Tu §\lcm(4,6)=12§ (Věta 6.20).""", 2 to """§2=\gcd(4,6)§. Riešenie je jediné modulo §\lcm§, nie §\gcd§."""))
    worked("Dosadzovanie", """§x\equiv 5\pmod 6§, §x\equiv 3\pmod{10}§, §x\equiv 8\pmod{15}§""", "Skriptá, Příklad 6.20; Spěvák, 8. prednáška", tcCrt(TcCrtTask(listOf(5L to 6L, 3L to 10L, 8L to 15L))).solution(emptyList()))
    clip("dml-tc-c-modul-chyba")
    worked("Druhý príklad z prednášky", """§x\equiv 2\pmod 4§, §x\equiv 4\pmod 6§, §x\equiv 4\pmod{21}§""", "Spěvák, 8. prednáška; sústava po tom, čo každú kongruenciu upravil na tvar §x\\equiv a\\pmod m§", tcCrt(TcCrtTask(listOf(2L to 4L, 4L to 6L, 4L to 21L))).solution(emptyList()))
    practice("dml-tc-crt", solo = false)
    worked("Písomka 2023", """§6x\equiv 36\pmod{195}§, §5x\equiv 4\pmod{91}§; najväčšie záporné riešenie""", "Písomka 6. 12. 2023 (variant A), príklad 3", tcCombo(TcComboTask(6, 36, 195, 5, 4, 91)).solution(emptyList()))
    check("""Riešenie sústavy je §x\equiv 201\pmod{455}§. Najväčšie záporné riešenie?""", "§-254§", "§-201§", "§-709§", correct = 0,
        why = """§201-455=-254§.""",
        wrong = mapOf(1 to """§-201§ riešenie nie je. Od §201§ treba odčítať §455§.""", 2 to """§-709§ je riešenie, ale menšie. Najväčšie záporné je §201-455=-254§."""))
    practice("dml-tc-crt", solo = true)
}

internal val tcLessonMocniny = lesson("dml-tc-mocniny", "Veľké mocniny", 17, "Eulerova funkcia, Eulerova a malá Fermatova veta a dva zvyšky z písomky.", art = 4, topics = listOf("TČ3")) {
    ask("""Smie sa exponent redukovať modulo §m§, napr. §2^7\equiv 2^2\pmod 5§?""", "Nie", "Áno", correct = 0,
        after = """§2^7=128\equiv 3§, ale §2^2=4§. Exponent sa redukuje modulo iného čísla: pri §5§ modulo §4§, ako ukáže Eulerova veta. §7\equiv 3\pmod 4§ a naozaj §2^3=8\equiv 3\pmod 5§.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Základ áno, exponent nie. Základ je činiteľ, preto ho smieš nahradiť kongruentným číslom; exponent hovorí, koľko činiteľov násobíš, a ten sa modulom nezachová. Dombek (1. paralelka) práve na §2^7§ a §2^2§ ukazuje, že študenti to v písomkách robia a je to chyba."""))
    explore("""Mocniny §2§ v §\mathbb{Z}_9§.""", """Umocňuj, kým sa nevrátiš do §1§.""", ClockPic(9, 2, ClockMode.Powers, 1),
        """§2^6\equiv 1\pmod 9§: po §6§ krokoch si späť. A §6§ je počet čísel z §1,\dots,9§ nesúdeliteľných s §9§.""")
    explain("Eulerova funkcia", """§\varphi(n)§ počíta čísla z §1,\dots,n§ nesúdeliteľné s §n§. Spěvák: §\varphi(2)=1§, §\varphi(3)=2§, §\varphi(4)=2§, §\varphi(5)=4§, §\varphi(6)=2§. Funkcia teda nemusí rásť.""",
        src = "Spěvák, 8. prednáška", cards = listOf(tcEuler.id))
    def("dml-tc-euler")
    explain("Eulerova veta", """Pri §\gcd(a,m)=1§ je §a^{\varphi(m)}\equiv 1\pmod m§, exponent preto stačí poznať modulo §\varphi(m)§. Spěvák ju dokázal: zvyšky nesúdeliteľné s §m§ vynásobené §a§ sú tie isté zvyšky v inom poradí. Súčin oboch zoznamov je teda rovnaký, len vľavo navyše §a^{\varphi(m)}§, a súčin zvyškov smieš vykrátiť bez zmeny modulu (Věta 6.13).""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-ev"))
    clip("dml-tc-c-ev-22")
    explain("Výpočet §\\varphi§ z rozkladu", """Pre prvočíslo §p§ je §\varphi(p^\alpha)=p^\alpha-p^{\alpha-1}§: odpočítaš násobky §p§, napríklad §\varphi(27)=27-9=18§. Pre nesúdeliteľné činitele sa hodnoty násobia, preto stačí rozklad: §§\begin{aligned}\varphi(100)&=\varphi(4)\varphi(25)\\&=2\cdot 20=40.\end{aligned}§§""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-phi-mocnina", "dml-tc-phi-soucin", "dml-tc-phi-rozklad"))
    clip("dml-tc-c-phi-pozor")
    check("""§\varphi(28)§ je""", "§12§", "§27§", "§14§", correct = 0, why = """§\varphi(28)=\varphi(4)\cdot\varphi(7)=2\cdot 6=12§.""",
        wrong = mapOf(1 to """§28§ nie je prvočíslo. §\varphi(28)=\varphi(4)\cdot\varphi(7)=2\cdot 6§.""", 2 to """Odrátal si len párne čísla. Treba aj násobky §7§: §\varphi(28)=12§."""))
    worked("Eulerova veta", """§2^{268}\bmod 9§""", "Skriptá, Příklad 6.14; Spěvák, 8. prednáška", tcPow(TcPowTask(2, 268, 9)).solution(emptyList()))
    explain("Malá Fermatova veta", """Pre prvočíslo §p§ je §\varphi(p)=p-1§, takže Eulerova veta dáva §a^{p-1}\equiv 1\pmod p§ pre §p\nmid a§. Vynásobením §a§ vyjde §a^p\equiv a\pmod p§, a to platí pre každé celé §a§. Ak §p\nmid a§, exponent sa redukuje modulo §p-1§, napríklad (Příklad 6.11): §§\begin{aligned}5^{203}&=(5^{100})^2\cdot 5^3\\&\equiv 125\equiv 24\pmod{101}\end{aligned}§§""",
        src = "Spěvák, 8. prednáška; Skriptá, Příklad 6.11", cards = listOf("dml-tc-mfv", "dml-tc-phi-prvocislo"))
    check("""§3^{100}\bmod 7§ je""", "§4§", "§2§", "§1§", correct = 0, why = """§100\equiv 4\pmod 6§, teda §3^{100}\equiv 3^4=81\equiv 4§.""",
        wrong = mapOf(1 to """Exponent si redukoval modulo §7§. Správne je modulo §p-1=6§: §100\equiv 4§.""", 2 to """§1§ by vyšlo pre exponent deliteľný §6§. §100\equiv 4\pmod 6§."""))
    explain("Inverzia z Eulerovej vety", """§a\cdot a^{\varphi(m)-1}=a^{\varphi(m)}\equiv 1§, preto inverzia §a§ je zvyšok §a^{\varphi(m)-1}§. Spěvák: §7^{-1}§ v §\mathbb{Z}_9§ je zvyšok §7^5§. Sám ho nedopočítal, vyjde §4§, lebo §7\cdot 4=28\equiv 1§. Prakticky je REA rýchlejší, toto je podľa neho skôr zaujímavosť.""",
        src = "Spěvák, 8. prednáška", cards = listOf("dml-tc-ev-inverze", "dml-tc-mfv-inverze"))
    check("""Smieš pri §3^{644}\bmod 645§ redukovať exponent Eulerovou vetou?""", """Nie, §3\mid 645§""", """Áno, §\varphi(645)§ existuje""", correct = 0,
        why = """Nie, §645=3\cdot 5\cdot 43§ a Eulerova veta chce §\gcd(a,m)=1§. Ani žiadna iná mocnina tu nedá §1§: každé §3^k§ je násobok §3§, a §3^k-1§ teda nie, takže §645\nmid 3^k-1§. Taký prípad rieši binárne mocnenie, ďalšia lekcia.""",
        wrong = mapOf(1 to """§\varphi(645)§ existuje, ale veta platí len pre základ nesúdeliteľný s modulom. §\gcd(3,645)=3§, takže redukcia exponentu nie je zdôvodnená."""))
    practice("dml-tc-pow", solo = false)
    worked("Dva zvyšky z písomky", """§\begin{aligned}&\text{a) }667^{325}\bmod 60\\&\text{b) }917^{(667^{325})}\bmod 61\end{aligned}§""", "Písomka 5. 12. 2025 (variant K), príklad 3",
        tcTower(TcTowerTask(667, 325, 917, 61)).solution(emptyList()))
    check("""Prečo je v časti a) modul práve §60§?""", """§61§ je prvočíslo a MFV redukuje exponent modulo §61-1§""", """Exponent sa redukuje modulo §61§""", """Je to náhoda zadania""", correct = 0,
        why = """MFV dá §2^{60}\equiv 1\pmod{61}§, takže exponent treba poznať modulo §60§.""",
        wrong = mapOf(1 to """Modulo §61§ sa redukuje základ. Exponent podľa MFV modulo §p-1=60§.""", 2 to """Nie je: §60=61-1§ je presne modul, ktorý potrebuje MFV v časti b)."""))
    practice("dml-tc-tower", solo = true)
    check("""Prvá kongruencia z Cvičení 6.28 je §3^{1338}x\equiv 1\pmod{28}§. Na čo sa zjednoduší?""", """§x\equiv 1\pmod{28}§""", """§x\equiv 3\pmod{28}§""", "Nedá sa, exponent je priveľký", correct = 0,
        why = """§\varphi(28)=12§, §1338\equiv 6§ a §3^6=729\equiv 1\pmod{28}§.""",
        wrong = mapOf(1 to """Koeficient §3^{1338}\equiv 1§, takže §x\equiv 1§, nie §3§.""", 2 to """Exponent redukuj modulo §\varphi(28)=12§ (Eulerova veta)."""))
    practice("dml-tc-combo", solo = true)
}

/*
 * Built from Dombek's lecture (paralelka 1, 9. prednáška, 23. 11. 2026, 46:45 to 51:40): when Euler does not help, square
 * and multiply on 3^644 mod 645, the repeating remainders, the binary digits of the exponent, and his summary of modular
 * arithmetic. Numbers of the example as in Příklad 6.16 (the captions hear the exponent as 664 once; the digits he reads
 * out, 2^9 + 2^7 + 2^2, are 644).
 */
internal val tcLessonMocnenie = lesson("dml-tc-mocnenie", "Binárne mocnenie", 15, "Veľká mocnina, keď Eulerova veta nepomôže: umocňovanie na druhú a dvojkový zápis exponentu.", art = 7, topics = listOf("TČ4")) {
    ask("""Máš §3^4=81§. Ako dostaneš §3^8§ najrýchlejšie?""", """Umocním §3^4§ na druhú""", """Vynásobím §3^4§ ešte štyrikrát trojkou""", """Vynásobím §3^4§ dvoma""", correct = 0,
        after = """Jedným násobením: §(3^4)^2=3^{4\cdot 2}=3^8§. Opakovaním dostaneš §3^{16}§, §3^{32}§ a ďalšie, vždy jedným násobením.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Dá to §3^8§, ale štyrmi násobeniami. Stačí jedno: §(3^4)^2=3^{4\cdot 2}=3^8§.""",
            2 to """§3^4\cdot 2=162§ nie je mocnina trojky. Exponent zdvojnásobí umocnenie na druhú: §(3^4)^2=3^8§."""))
    explore("""Mocniny §2§ v §\mathbb{Z}_{12}§.""", """Bodky sú zvyšky §0,\dots,11§, začínaš v §2^0=1§. Každý krok násobí §2§ a redukuje modulo §12§; sleduj, či sa vrátiš do §1§.""", ClockPic(12, 2, ClockMode.Powers, 8),
        """§2^3\equiv 8§ a §2^4=16\equiv 4\equiv 2^2§, odtiaľ sa §4§ a §8§ striedajú. Do §1§ sa nevrátiš: §2^k§ pre §k\geq 1§ je párne aj §12§ je párne, takže §2^k-1§ nie je násobok §12§. Eulerova veta to nesľubuje, lebo chce §\gcd(a,m)=1§ a tu je §\gcd(2,12)=2§.""")
    explain("Keď Eulerova veta nepomôže", """Eulerova veta zmenší exponent len pri nesúdeliteľnom základe: pre §m\in\mathbb{N}§, §m\geq 2§ a §a\in\mathbb{N}§ s §\gcd(a,m)=1§ je §a^{\varphi(m)}\equiv 1\pmod m§ (Věta 6.15). Dombek (1. paralelka) uvádza dva prípady, keď pomôže metóda square and multiply, v skriptách binárne mocnenie. Prvý: základ a modul sú súdeliteľné, napríklad §3^{644}\bmod 645§, kde §645=3\cdot 5\cdot 43§. Druhý: Eulerovu vetu si použil, ale zvyšný exponent je stále napríklad trojciferný.""",
        formal = cz("""Podkapitolu zakončíme vysvětlením, jak efektivně redukovat mocniny modulo §m§ v případě, že z jakéhokoli důvodu nechceme nebo nemůžeme využít Malou Fermatovu či Eulerovu větu (typicky v případě, kdy základ exponenciály je soudělný s modulem, nebo když mocnina je moc malá na to, aby MFV/EV pomohla, ale moc velká na ruční přímočarý výpočet). Je to tzv. binární mocnění modulo §m§."""),
        src = "Dombek, 9. prednáška; Skriptá, text pred Příkladom 6.16", cards = listOf("dml-tc-ev"))
    clip("dml-tc-c-kedy-binarne")
    explain("Umocni na druhú a zredukuj", """Dombek: „Umocni na druhou, pokud jsi přelezl přes modulo, udělej redukci modulo M.“ Z §3§ tak dostaneš §3^2,3^4,3^8,3^{16},\dots§, každú mocninu z predošlej jedným násobením, lebo §\big(a^{2^j}\big)^2=a^{2^{j+1}}§. Redukovať po každom kroku smieš: pre §a,r\in\mathbb{Z}§, §m\in\mathbb{N}§, §m\geq 2§ a §j\in\mathbb{N}_0§ z §a^{2^j}\equiv r\pmod m§ plynie §a^{2^{j+1}}\equiv r^2\pmod m§, lebo kongruencia sa zachová pri súčine (Tvrzení 6.6). Podmienku §\gcd(a,m)=1§ to nepotrebuje. Skončíš pri najväčšej mocnine dvojky, ktorá je nanajvýš exponent; pri §644§ je to §512=2^9§.""",
        src = "Dombek, 9. prednáška; Skriptá, Příklad 6.16", cards = listOf("dml-tc-operace"))
    clip("dml-tc-c-umocni")
    check("""§3^{16}\equiv 66\pmod{645}§. Zvyšok §3^{32}§ modulo §645§ je""", "§486§", "§132§", "§4356§", correct = 0,
        why = """§3^{32}=(3^{16})^2§ a §66^2=4356=6\cdot 645+486§.""",
        wrong = mapOf(1 to """§132=2\cdot 66§: zdvojnásobil si zvyšok. Exponent sa zdvojnásobí umocnením zvyšku na druhú: §66^2=4356\equiv 486§.""",
            2 to """§4356=66^2§ ešte nie je zredukované. Zvyšok musí byť z §\{0,\dots,644\}§: §4356-6\cdot 645=486§."""))
    explain("Dvojkový zápis exponentu", """Každé §N\in\mathbb{N}§ je súčet rôznych mocnín dvojky, to je jeho dvojkový zápis: §644=512+128+4=(1010000100)_2§. Pre §s,t\in\mathbb{N}§ je §a^{s+t}=a^s\cdot a^t§, preto §3^{644}=3^{512}\cdot 3^{128}\cdot 3^{4}§. Vynásobíš len zvyšky z tabuľky pri jednotkách zápisu a po každom súčine redukuješ modulo §645§; ostatné riadky sú len medzikroky.""",
        src = "Dombek, 9. prednáška; Skriptá, Příklad 6.16")
    clip("dml-tc-c-dvojkovy")
    worked("Príklad z prednášky", """§3^{644}\bmod 645§""", "Dombek, 9. prednáška; Skriptá, Příklad 6.16", tcBinary(TcBinaryTask(3, 644, 645)).solution(emptyList()))
    check("""V tabuľke vyšlo §3^{256}\equiv 81§, rovnako ako §3^4§. Aký zvyšok má §3^{1024}§ modulo §645§?""", "§66§", "§111§", "§81§", correct = 0,
        why = """Od §81§ sa riadky opakujú, preto §3^{1024}\equiv 66§ ako §3^{16}§. Každý riadok vznikne z predošlého tým istým umocnením: §3^{512}\equiv 111§ ako §3^8§. Dombek ďalší zvyšok §66§ napísal bez počítania.""",
        wrong = mapOf(1 to """§111§ je zvyšok §3^{512}§. §3^{1024}=(3^{512})^2§ a §111^2=12321\equiv 66§.""",
            2 to """§81§ je zvyšok §3^{256}§. Riadky sa opakujú od §3^4\equiv 81§: po §81§ ide §111§, potom §66§."""))
    check("""Čo potrebuje binárne mocnenie §a^N\bmod m§ pre §a\in\mathbb{Z}§, §N,m\in\mathbb{N}§, §m\geq 2§?""", "Nič ďalšie, stačí Tvrzení 6.6", """§\gcd(a,m)=1§""", "Prvočíselný modul", correct = 0,
        why = """Redukcia po každom súčine stojí len na Tvrzení 6.6, a to platí pre všetky §a,b,c,d,m\in\mathbb{Z}§, §m\geq 2§. Preto ide aj §3^{644}\bmod 645§, kde §\gcd(3,645)=3§.""",
        wrong = mapOf(1 to """§\gcd(a,m)=1§ chce Eulerova veta (Věta 6.15), nie binárne mocnenie. Dombek ho ukazuje práve na §3^{644}\bmod 645§, kde §\gcd(3,645)=3§.""",
            2 to """Prvočíselný modul chce malá Fermatova veta. Binárne mocnenie funguje pre každý modul §m\geq 2§, aj pre §645=3\cdot 5\cdot 43§."""))
    practice("dml-tc-binary", solo = false)
    check("""§\gcd(17,35)=1§ a §\varphi(35)=24§, preto podľa Eulerovej vety §17^{110}\equiv 17^{14}\pmod{35}§. Ktoré riadky tabuľky vynásobíš pre §17^{14}§?""",
        """§17^{8}§, §17^{4}§ a §17^{2}§""", """§17^{8}§, §17^{4}§, §17^{2}§ a §17§""", """§17^{8}§ a §17^{6}§""", correct = 0,
        why = """§14=8+4+2=(1110)_2§, jednotky sú pri §8§, §4§ a §2§.""",
        wrong = mapOf(1 to """To je §8+4+2+1=15§. §14§ je párne, riadok §17§ do súčinu nepatrí.""",
            2 to """§6§ nie je mocnina dvojky, riadok §17^6§ tabuľka nemá. §14=8+4+2§."""))
    worked("Euler, potom binárne mocnenie", """§17^{110}\bmod 35§""", "Rozstřel 11. 1. 2023, otázka 5", tcPow(TcPowTask(17, 110, 35)).solution(emptyList()))
    practice("dml-tc-pow-binary", solo = false)
    explain("Čo z toho máme", """Dombek takto uzavrel modulárnu aritmetiku. Krátiť smieš, len si strážiš modul (Věta 6.13). Exponent modulo §m§ všeobecne redukovať nemožno; modulo §\varphi(m)§ smieš, ak §\gcd(a,m)=1§ (Věta 6.15). Keď to nejde alebo exponent ostal veľký, binárne mocnenie dôjde k výsledku vždy: pri exponente s §L§ dvojkovými číslicami je to §L-1§ umocnení na druhú a o jedno menej násobení, než má zápis jednotiek. Pri §644§ teda §9§ a §2§ namiesto §643§ násobení.""",
        src = "Dombek, 9. prednáška")
    practice("dml-tc-binary", solo = true)
}

internal val tcLessons = listOf(tcLessonDelitelnost, tcLessonRea, tcLessonLdr, tcLessonKongruencie, tcLessonLkon, tcLessonCvoz, tcLessonMocniny, tcLessonMocnenie)

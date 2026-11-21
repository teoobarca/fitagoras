package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.*

/*
 * The five lessons of kapitola 3 (slajdy 3): hodnosť, hodnosť v úlohách, násobenie a kombinácie, inverzná matica,
 * regularita. Every step says where it rests: textbook items by number, the exam by date, clips by lecture. Numbers in
 * worked examples are the textbook's or are checked in Chapter3ContentTest; the exam solution is computed.
 */

private val S3 = listOf("S3")
private fun row(vararg xs: Int) = xs.toList()

/** Skúška 26. 1. 2026 (variant Hilbert), príklad 2: the matrix with the parameter. */
internal val HilbertRows: List<PVec> = listOf(
    listOf(Poly.of(-1), Poly.lin(1, 1), Poly.lin(-2, 1)),
    listOf(Poly.of(1), Poly.of(-2), Poly.of(1)),
    listOf(Poly.of(0), Poly.lin(-1, -1), Poly.of(2)),
)

internal fun hilbertCase(): ParamCase = ParamCase(HilbertRows, paramRank(HilbertRows)!!, Mat(HilbertRows.map { r -> r.map { it.eval(Frac.ZERO) } }))

internal val k3lHodnost = lesson("la1-k3-hodnost", "Hodnosť matice", 16, "Čo je hodnosť (dimenzia obalu riadkov), prečo ju GEM nemení a ako ju odčítať z HST.", 3, S3) {
    ask("""Riadky matice §\begin{pmatrix}1&2\\2&4\\3&6\end{pmatrix}§ ber ako vektory z §\mathbb{R}^2§. Akú dimenziu má ich lineárny obal?""", "§1§", "§2§", "§3§", correct = 0,
        after = """Dimenzia §1§: každý riadok je násobkom §(1,2)§, obal je priamka §\langle(1,2)\rangle§.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Dva rôzne riadky ešte nedávajú dimenziu §2§. Všetky tri sú násobky §(1,2)§, ich obal je priamka §\langle(1,2)\rangle§.""",
            2 to """Tri riadky nedávajú dimenziu §3§: ležia v §\mathbb{R}^2§ a to má dimenziu §2§. Tu sú navyše všetky na priamke §\langle(1,2)\rangle§."""))
    explore("""Riadky matice §\begin{pmatrix}1&2\\2&4\end{pmatrix}§ nad §\mathbb{Z}_5§""", "Svetlé body sú obal riadkov. Spočítaj ich, potom ťukni na iný vektor a porovnaj.", Pic.Grid(5, listOf(1 to 2, 2 to 4), Goal.Count, 5),
        """Päť bodov, teda §5^1§: obal riadkov je priamka dimenzie §1§. Hodnosť tej matice je §1§.""")
    explain("Hodnosť je dimenzia obalu riadkov.", """Riadky ber ako vektory z §T^n§ a pýtaj sa, koľko smerov dávajú spolu. Nie koľko je riadkov, ani koľko je nenulových.""",
        cards = listOf("la1-k3-hodnost"))
    clip("k3-hodnost-def-riadky")
    check("""Určte hodnosť matice §\begin{pmatrix}1&3&1\\2&1&-1\end{pmatrix}\in\mathbb{R}^{2,3}§.""", "§1§", "§2§", "§3§", correct = 1, why = """Riadky nie sú násobky, sú LN: obal má dimenziu §2§ (Příklad 3.3).""",
        wrong = mapOf(0 to """Hodnosť §1§ by chcela oba riadky na jednej priamke. §(2,1,-1)§ však nie je násobkom §(1,3,1)§.""",
            2 to """Dva riadky dajú najviac dva smery. Tri stĺpce na tom nič nezmenia (Důsledek 3.13)."""))
    explain("GEM obal riadkov nemení.", """Každý nový riadok je kombináciou starých a každý krok sa dá vrátiť. Obal riadkov ostane ten istý, a s ním aj jeho dimenzia, hodnosť.""",
        cards = listOf("la1-k3-t-gem", "la1-k3-t-gem-hodnost"))
    clip("k3-hodnost-kroky-gem")
    explain("V HST sa hodnosť dá odčítať.", """Nenulové riadky matice v HST sú LN, nulové obal nemenia. Hodnosť je počet nenulových riadkov, teda aj počet hlavných stĺpcov.""",
        cards = listOf("la1-k3-t-hst"))
    explore("Krokuj GEM", "Matica z Príkladu 3.8 nad §\\mathbb{R}§. Sleduj, koľko nenulových riadkov ostane.", Pic.Gem(listOf(row(2, 1, 2, 1, 0), row(-1, 0, 1, 0, 1), row(3, 1, 0, 1, 1), row(6, 2, 1, 2, 0)), 0, caption = "Skriptá, Příklad 3.8"),
        """Tri nenulové riadky v HST: §h(\mathbf{A})=3§. GEM hodnosť nezmenil, takže to platí aj pre pôvodnú maticu.""")
    clip("k3-hodnost-hst")
    worked("Hodnosť nad §\\mathbb{Z}_5§", """Spočtěte hodnost matice §\mathbf{A}=\begin{pmatrix}1&2&0&3\\2&1&1&0\\3&3&1&3\end{pmatrix}\in\mathbb{Z}_5^{3,4}§.""", "Postup podľa Vět 3.6 a 3.7", K3RankPostup) {
        val (gem, count, rows) = K3RankPostup
        gem("""Z definície by hodnosť chcela dimenziu obalu riadkov; rýchlejšie je upraviť maticu na HST, lebo GEM hodnosť nemení (Věta 3.6).""",
            Formula(Field.Zp(5).walk(intMat(listOf(1, 2, 0, 3), listOf(2, 1, 1, 0), listOf(3, 3, 1, 3)), listOf(rowAdd(2, 3, 1), rowAdd(3, 2, 1)), listOf(rowAdd(3, 4, 2))), "v §\\mathbb{Z}_5§ je §-2=3§ a §-3=2§"))
        count("""V HST sú dva nenulové riadky, teda §h(\mathbf{A})=2§ (Věta 3.7).""")
        rows("""Riadky nie sú LN: tretí bol súčtom prvých dvoch, preto po GEM vyšiel nulový.""")
    }
    check("""Po GEM nad §\mathbb{R}§ vyšlo §\begin{pmatrix}1&2&0\\0&0&3\\0&0&0\end{pmatrix}§. Hodnosť je""", "§1§", "§2§", "§3§", correct = 1, why = """Dva nenulové riadky v HST, hodnosť §2§ (Věta 3.7).""",
        wrong = mapOf(0 to """Nenulové riadky sú dva. Druhý má schod až v treťom stĺpci, ale nenulový je.""",
            2 to """Nulový riadok do obalu nič nepridá. V HST sa počítajú len nenulové riadky."""))
    practice("la1-k3-rank", solo = false)
    def("la1-k3-hodnost")
    check("""Na skúške: „Definujte hodnost matice.“ Stačí „maximální počet LN řádků“?""", "Áno", "Nie", correct = 1, why = """Nie: LN je vlastnosť súboru, nie jedného riadku (Varování 3.11).""",
        wrong = mapOf(0 to """Skriptá to výslovne volajú nezmyslom. O jednom riadku sa nedá povedať, či je LN; definícia hovorí o dimenzii obalu."""))
    clip("k3-hodnost-def-vs-vypocet")
    explain("Čo je a čo nie je krok GEM", """§\mathbf{A}\sim\mathbf{B}§ píšeš len pre matice rovnakého typu. Nulový riadok pri sústave vynechať smieš, ale krok GEM to nie je.""",
        cards = listOf("la1-k3-sim"))
    def("la1-k3-sim")
    check("Ktorý zápis pre matice nad §\\mathbb{R}§ je správny?", """§\begin{pmatrix}1&2\\2&4\end{pmatrix}\sim\begin{pmatrix}1&2\\0&0\end{pmatrix}§""", """§\begin{pmatrix}1&2\\2&4\end{pmatrix}\sim\begin{pmatrix}1&2\end{pmatrix}§""", """§\begin{pmatrix}1&2\\2&4\end{pmatrix}=\begin{pmatrix}1&2\\0&0\end{pmatrix}§""", correct = 0,
        why = """Zápis platí: je to jeden dovolený krok (G3) medzi maticami rovnakého typu.""",
        wrong = mapOf(1 to """Rôzne typy: vyškrtnutie riadku nie je krok GEM (Definice 3.4).""",
            2 to """Matice sa nerovnajú, len sa jedna dá previesť na druhú. Rovnosť matíc znamená rovnaké všetky prvky a tu sa líši druhý riadok; §\sim§ hovorí len, že druhá vznikla z prvej úpravami GEM (Definice 3.4)."""))
    practice("la1-k3-rank", solo = true)
}

internal val k3lAplikacie = lesson("la1-k3-aplikacie", "Hodnosť v úlohách", 15, "Riadky a stĺpce dávajú rovnakú hodnosť; dimenzia obalu, patrí vektor do obalu, rovnosť obalov.", 4, S3) {
    ask("""Matica §\mathbf{A}\in\mathbb{R}^{3,5}§ má hodnosť §2§. Akú dimenziu má obal jej stĺpcov?""", "§2§", "§3§", "§5§", correct = 0,
        after = """Tiež §2§: transpozícia hodnosť nemení (Věta 3.12).""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """§3§ je počet riadkov, nie dimenzia. Obal stĺpcov má dimenziu §h(\mathbf{A}^T)=h(\mathbf{A})=2§.""",
            2 to """Päť stĺpcov nedá päť smerov. Ležia v §\mathbb{R}^3§ a ich obal má dimenziu §h(\mathbf{A})=2§."""))
    explore("""Riadky §(1,2)§ a §(3,1)§ v §\mathbb{Z}_5§""", "Obal riadkov matice §\\begin{pmatrix}1&2\\\\3&1\\end{pmatrix}§. Koľko má bodov?", Pic.Grid(5, listOf(1 to 2, 3 to 1), Goal.Count, 5),
        """Len päť: §(3,1)=3\cdot(1,2)§ v §\mathbb{Z}_5§, hodnosť je §1§.""", """Stĺpce §(1,3)§ a §(2,1)§ sú tiež závislé: §(2,1)=2\cdot(1,3)§. Obe dimenzie vyšli §1§, ako hovorí Věta 3.12.""")
    explain("Riadky a stĺpce dajú rovnako veľa smerov.", """Dimenzia obalu riadkov sa rovná dimenzii obalu stĺpcov. Preto je hodnosť najviac počet riadkov aj najviac počet stĺpcov.""",
        cards = listOf("la1-k3-t-transp", "la1-k3-t-odhad-rozmermi"))
    clip("k3-hodnost-transpozicia")
    check("""§\mathbf{A}\in T^{4,2}§. Najväčšia možná §h(\mathbf{A})§ je""", "§2§", "§4§", "§8§", correct = 0, why = """§h(\mathbf{A})\leq\min\{4,2\}=2§ (Důsledek 3.13).""",
        wrong = mapOf(1 to """Riadky sú štyri, ale ležia v §T^2§. Ich obal má dimenziu najviac §2§.""",
            2 to """Hodnosť je dimenzia, nie počet prvkov matice. Najviac §\min\{4,2\}=2§."""))
    clip("k3-hodnost-min-nie-rovnost")
    explain("Tri úlohy cez hodnosť", """Dimenzia obalu: vektory do riadkov a hodnosť. Patrí §\mathbf{y}§ do obalu: pridanie §\mathbf{y}§ hodnosť nezmení. Rovnosť obalov: obe dimenzie sa rovnajú dimenzii obalu všetkých vektorov.""",
        formal = listOf(
            p("""**Výpočet dimenze lineárního obalu vektorů:** Potřebujeme-li pro zadané vektory §\mathbf{x}_{1},\dots, \mathbf{x}_{m}\in T^{n}§ spočítat"""),
            b("""\dim\langle \mathbf{x}_1,\dots,\mathbf{x}_m\rangle,"""),
            p("""stačí vektory napsat do řádků matice"""),
            b("""\begin{pmatrix}\mathbf{x}_1^T\\\mathbf{x}_2^T\\\vdots\\\mathbf{x}_m^T\end{pmatrix}"""),
            p("""a úpravami GEM převést tuto matici do horního stupňovitého tvaru. Počet nenulových řádků je pak hledaná dimenze lineárního obalu a nenulové řádky (po transponování; tedy místo toho, abychom je chápali jako prvky §T^{1,n}§, je bereme jako prvky §T^{n,1}=T^n§) jsou bází tohoto lineárního obalu."""),
            p("""**Ověření, zda vektor patří do lineárního obalu:** Jsou dány §\mathbf{y},\mathbf{x}_{1},\dots,\mathbf{x}_{m}\in T^{n}§. Potřebujeme-li rozhodnout, zda"""),
            b("""\mathbf{y}\in\langle \mathbf{x}_1,\dots,\mathbf{x}_m\rangle,"""),
            p("""stačí využít Důsledek 2.64, který nám říká, že daný vztah platí, právě když"""),
            b("""\dim\langle \mathbf{x}_1,\dots,\mathbf{x}_m\rangle\softbreak[1]=\dim\langle \mathbf{x}_{1},\dots,\mathbf{x}_{m},\mathbf{y}\rangle."""),
            p("""Tedy stačí ověřit, zda hodnost matice, jejíž řádky jsou vektory §\mathbf{x}_{1},\dots,\mathbf{x}_{m}§, je stejná jako hodnost matice, ve které je navíc přidán řádek §\mathbf{y}§."""),
            p("""**Ověření rovnosti lineárních obalů:** Jsou dány §\mathbf{x}_{1},\dots,\mathbf{x}_{r},\mathbf{y}_{1},\dots,\mathbf{y}_{s}\in T^{n}§. Potřebujeme-li ověřit, zda"""),
            b("""\langle \mathbf{x}_1,\dots,\mathbf{x}_r\rangle=\langle \mathbf{y}_1,\dots,\mathbf{y}_s\rangle,"""),
            p("""vzpomeneme si na Pozorování 2.65, podle kterého jsou tyto dva lineární obaly rovny právě tehdy, když"""),
            b("""\dim\langle \mathbf{x}_1,\dots,\mathbf{x}_r\rangle\softbreak[1]=\dim\langle \mathbf{y}_1,\dots,\mathbf{y}_s\rangle\softbreak[1]=\dim\langle \mathbf{x}_{1},\dots,\mathbf{x}_{r},\softbreak[2]\mathbf{y}_{1},\dots,\mathbf{y}_{s}\rangle."""),
            p("""Stačí tedy ověřit rovnost hodností příslušných matic."""),
        ), src = "Skriptá, Poznámka 3.9")
    clip("k3-hodnost-dim-obalu")
    worked("Dimenzia obalu a patrí doň vektor?", """V §\mathbb{R}^4§ mějme §\mathbf{x}_1=(1,2,0,1)§, §\mathbf{x}_2=(2,3,1,0)§, §\mathbf{x}_3=(0,1,-1,2)§. Určete §\dim\langle\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3\rangle§ a rozhodněte, zda §\mathbf{y}=(1,1,1,-1)§ do tohoto obalu patří.""", "Postup podľa Poznámky 3.9", K3SpanRankPostup) {
        val (dim, add, end) = K3SpanRankPostup
        dim("""Vektory píšem do riadkov a upravím GEM; nič neriešim, len počítam nenulové riadky.""",
            Formula(Field.R.walk(intMat(listOf(1, 2, 0, 1), listOf(2, 3, 1, 0), listOf(0, 1, -1, 2)), listOf(rowSub(2, 2, 1)), listOf(rowAdd(3, 1, 2)))))
        dim("""Hodnosť je §2§, teda dimenzia obalu je §2§. Nenulové riadky §(1,2,0,1),(0,-1,1,-2)§ sú jeho báza.""")
        add("""K báze obalu pridám §\mathbf{y}=(1,1,1,-1)§ ako tretí riadok a hodnosť spočítam znova.""",
            Formula(Field.R.walk(intMat(listOf(1, 2, 0, 1), listOf(0, -1, 1, -2), listOf(1, 1, 1, -1)), listOf(rowSub(3, 1, 1)), listOf(rowSub(3, 1, 2)))))
        end("""Hodnosť ostala §2§, obaly majú rovnakú dimenziu, preto §(1,1,1,-1)§ do obalu patrí (Důsledek 2.64). Skúška:""", eq("""-(1,2,0,1)+(2,3,1,0)=(1,1,1,-1)"""))
    }
    check("""Hodnosť matice s riadkami §\mathbf{x}_1,\mathbf{x}_2§ je §2§, s pridaným §\mathbf{y}§ je §3§. Potom""", """§\mathbf{y}\notin\langle\mathbf{x}_1,\mathbf{x}_2\rangle§""", """§\mathbf{y}\in\langle\mathbf{x}_1,\mathbf{x}_2\rangle§""", """§\mathbf{y}=\theta§""", correct = 0,
        why = """Obal sa zväčšil, takže §\mathbf{y}§ v ňom nebol (Důsledek 2.64).""",
        wrong = mapOf(1 to """Dimenzia stúpla, obal sa zväčšil. Keby §\mathbf{y}§ v obale bol, ostala by hodnosť §2§.""",
            2 to """Nulový vektor hodnosť nezvýši. V každom obale je §\theta§ už od začiatku."""))
    clip("k3-obal-patri-vektor")
    explain("Riadky, alebo stĺpce? Dva rôzne postupy.", """Cez hodnosť: vektory do riadkov, nič neriešiš, porovnáš počty nenulových riadkov. Z definície obalu: vektory do stĺpcov, §\mathbf{y}§ ako pravá strana, pýtaš sa, či má sústava riešenie. Na papieri napíš, ktorý postup robíš.""",
        formal = listOf(
            p("""U všech bodů z předchozího přehledu je postup přes hodnosti matic jen jednou z možností. Například u 2. bodu můžeme fakt"""),
            b("""\mathbf{y}\in\langle \mathbf{x}_1,\dots,\mathbf{x}_m\rangle\Leftrightarrow\softbreak[1](\exists\alpha_1,\dots,\alpha_m\in T)\softbreak\left(\mathbf{y}=\sum_{i=1}^m \alpha_i \mathbf{x}_i\right)"""),
            p("""ověřit i přímo z definice, přes řešitelnost soustavy lineárních rovnic s neznámými §\alpha_1,\dots,\alpha_m\in T§ (analogicky k Algoritmu 2.69). Tyto dva různé postupy důrazně rozlišujme!"""),
            i("""V postupu dle tohoto vztahu řešíme SLR s maticí, která má všechny dané vektory zapsané **ve sloupcích** (navíc §\mathbf{y}§ určuje pravou stranu soustavy) a zajímá nás řešitelnost soustavy s neznámými §\alpha_1,\dots,\alpha_n\in T§. (Preklep v skriptách: neznámych je §m§, správne §\alpha_1,\dots,\alpha_m§.)"""),
            i("""V postupu s využitím hodnosti pracujeme s maticí bez pravé strany, která má všechny dané vektory zapsané **v řádcích** a nic jako její řešení nás nezajímá! Pouze zkoumáme, kolik nenulových řádků zůstane po úpravě dvou matic na horní stupňovitý tvar – matice pouze s vektory §\mathbf{x}_i§ a matice s přidaným §\mathbf{y}§."""),
        ), src = "Skriptá, Poznámka 3.10")
    check("""Overuješ cez hodnosť, či §\mathbf{y}\in\langle\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3\rangle§. Kam dáš vektory?""", "Do riadkov, raz bez §\\mathbf{y}§ a raz s ním", "Do stĺpcov, §\\mathbf{y}§ ako pravú stranu", "Do riadkov, §\\mathbf{y}§ ako pravú stranu", correct = 0,
        why = """Cez hodnosť porovnáš dve matice s vektormi v riadkoch (Poznámka 3.10).""",
        wrong = mapOf(1 to """To je druhý postup, z definície obalu. Správny je tiež, ale otázka chce postup cez hodnosť.""",
            2 to """Pri hodnosti žiadna pravá strana nie je. Porovnávaš hodnosti dvoch matíc a nič neriešiš."""))
    clip("k3-obal-rovnost-dimenzie")
    check("""§\dim\langle\mathbf{x}_1,\mathbf{x}_2\rangle=\dim\langle\mathbf{y}_1,\mathbf{y}_2\rangle=2§. Rovnajú sa tie obaly?""", "Nie nutne", "Áno, vždy", correct = 0,
        why = """Rovnaká dimenzia nestačí: §\langle\mathbf{e}_1,\mathbf{e}_2\rangle\neq\langle\mathbf{e}_2,\mathbf{e}_3\rangle§ v §\mathbb{R}^3§.""",
        wrong = mapOf(1 to """Dve rôzne roviny v §\mathbb{R}^3§ majú obe dimenziu §2§. Treba aj dimenziu obalu všetkých štyroch (Pozorování 2.65)."""))
    practice("la1-k3-span-rank", solo = false)
    explain("Hodnosť malej matice v §\\mathbb{Z}_p§", """Matica §(c)\in\mathbb{Z}_p^{1,1}§ má hodnosť §1§ pre §c\neq 0§ a §0§ pre §c=0§. Na skúške 17. 12. 2025 sa pýtali, koľko matíc v §\mathbb{Z}_7^{1,1}§ má hodnosť §1§: šesť.""",
        src = "Skúška 17. 12. 2025 (variant Frobenius), príklad 3A")
    check("""Koľko matíc v §\mathbb{Z}_3^{1,2}§ má hodnosť §1§?""", "§8§", "§9§", "§2§", correct = 0, why = """Každý nenulový riadok: §3^2-1=8§.""",
        wrong = mapOf(1 to """Nulová matica má hodnosť §0§. Z §3^2=9§ matíc ju treba odčítať.""",
            2 to """Hodnosť §1§ má každý nenulový riadok, nie dva. Nenulových dvojíc v §\mathbb{Z}_3^2§ je §8§."""))
    practice("la1-k3-span-rank", solo = true)
}

internal val k3lNasobenie = lesson("la1-k3-nasobenie", "Násobenie a kombinácie", 15, "Matica krát stĺpec kombinuje stĺpce, riadok krát matica kombinuje riadky; hodnosť súčinu.", 2, S3) {
    ask("""Čo je nad §\mathbb{R}§ §\begin{pmatrix}1&2\\3&4\end{pmatrix}\begin{pmatrix}1\\1\end{pmatrix}§?""", "§(3,7)^T§", "§(4,6)^T§", "§(1,3)^T§", correct = 0,
        after = """Súčet stĺpcov: §1\cdot(1,3)^T+1\cdot(2,4)^T=(3,7)^T§.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Sčítal si riadky. Stĺpec sprava kombinuje stĺpce: §(1,3)^T+(2,4)^T§.""",
            2 to """Matica stĺpec nevyberie, kombinuje všetky stĺpce. Oba tu majú koeficient §1§."""))
    explore("""Vyrieš nad §\mathbb{R}§ §\mathbf{A}\mathbf{x}=\mathbf{b}§ posúvaním.""", """§\mathbf{A}=\begin{pmatrix}2&1\\1&2\end{pmatrix}§, §\mathbf{b}=(4,5)^T§. Posúvaj §x_1§ pri prvom stĺpci a §x_2§ pri druhom.""", Pic.Plane(2 to 1, 1 to 2, 4 to 5, range = 3),
        """§x_1=1§, §x_2=2§: §1\cdot(2,1)^T+2\cdot(1,2)^T=(4,5)^T§.""", """Riešenie sústavy sú koeficienty kombinácie stĺpcov, ktorá dá pravú stranu (Pozorování 3.16).""")
    explain("Riadok zľava kombinuje riadky, stĺpec sprava stĺpce.", """§\mathbf{A}\mathbf{b}§ je kombinácia stĺpcov §\mathbf{A}§ s koeficientmi z §\mathbf{b}§. §\mathbf{a}\mathbf{A}§ je kombinácia riadkov s koeficientmi z §\mathbf{a}§. Sústava §\mathbf{A}\mathbf{x}=\mathbf{b}§ hľadá koeficienty stĺpcov.""",
        cards = listOf("la1-k3-t-kombinacie", "la1-k3-t-slr-kombinacie"))
    clip("k3-nasobenie-ax-stlpce")
    check("""Nad §\mathbb{R}§: §\begin{pmatrix}1&-1\end{pmatrix}\begin{pmatrix}2&0&5\\1&3&1\end{pmatrix}=§""", """§\begin{pmatrix}1&-3&4\end{pmatrix}§""", """§\begin{pmatrix}3&3&6\end{pmatrix}§""", """§\begin{pmatrix}2&-3\end{pmatrix}§""", correct = 0,
        why = """Prvý riadok mínus druhý: §(2,0,5)-(1,3,1)=(1,-3,4)§.""",
        wrong = mapOf(1 to """Sčítal si riadky. Koeficient §-1§ pri druhom riadku znamená odčítanie.""",
            2 to """Výsledok má toľko prvkov ako riadok matice, tri. Riadok zľava kombinuje celé riadky."""))
    explain("Súčin po riadkoch a po stĺpcoch", """§i§-tý riadok §\mathbf{A}\mathbf{B}§ je §\mathbf{A}_{i:}\mathbf{B}§, kombinácia riadkov §\mathbf{B}§. §j§-tý stĺpec je §\mathbf{A}\mathbf{B}_{:j}§, kombinácia stĺpcov §\mathbf{A}§.""",
        cards = listOf("la1-k3-t-po-riadkoch"))
    clip("k3-nasobenie-stlpce-ab")
    clip("k3-nasobenie-riadky-zlava")
    worked("Matica, ktorá kombinuje riadky", """Mějme matici §\mathbf{A}\in\mathbb{R}^{2,3}§. Napište matici §\mathbf{B}§ takovou, že po součinu těchto dvou matic vznikne matice, která má v prvním řádku součet řádků matice §\mathbf{A}§, v druhém řádku nulový řádek, v třetím řádku dvojnásobek prvního řádku matice §\mathbf{A}§.""", "Skriptá, Otázka za Větou 3.18", K3RowsPostup) {
        val (which, coefs) = K3RowsPostup
        which("""Kombinovať riadky §\mathbf{A}§ vie len súčin s §\mathbf{A}§ vpravo. Riadok súčinu je totiž riadok ľavého činiteľa krát pravý: §(\mathbf{B}\mathbf{A})_{i:}=\mathbf{B}_{i:}\mathbf{A}§ (Věta 3.18).""")
        which("""Výsledok má §3§ riadky a §\mathbf{A}§ má §2§ riadky, preto §\mathbf{B}\in\mathbb{R}^{3,2}§. Súčin §\mathbf{A}\mathbf{B}§ by kombinoval stĺpce, ten nechceme.""")
        coefs("""Riadok §\mathbf{B}§ sú koeficienty pri §\mathbf{A}_{1:}§ a §\mathbf{A}_{2:}§: súčet je §(1,1)§, nulový §(0,0)§, dvojnásobok prvého §(2,0)§.""", Formula("""\mathbf{B}=""" + intMat(listOf(1, 1), listOf(0, 0), listOf(2, 0)).tex()))
    }
    practice("la1-k3-product", solo = false)
    explain("Násobením hodnosť nevzrastie.", """Riadky §\mathbf{A}\mathbf{B}§ ležia v obale riadkov §\mathbf{B}§, stĺpce v obale stĺpcov §\mathbf{A}§. Nový smer súčin nevyrobí.""",
        cards = listOf("la1-k3-t-hodnost-sucinu"))
    clip("k3-hodnost-sucinu")
    thm("la1-k3-t-hodnost-sucinu")
    check("""§\mathbf{A}\in\mathbb{R}^{3,2}§, §\mathbf{B}\in\mathbb{R}^{2,3}§. Môže byť §h(\mathbf{A}\mathbf{B})=3§?""", "Nie", "Áno", correct = 0,
        why = """Nie: §h(\mathbf{A}\mathbf{B})\leq h(\mathbf{A})\leq 2§ (Věta 3.19).""",
        wrong = mapOf(1 to """Súčin je síce typu §3\times 3§, ale §h(\mathbf{A}\mathbf{B})\leq h(\mathbf{A})\leq\min\{3,2\}=2§."""))
    check("""Platí vždy pre matice nad telesom §T§ §h(\mathbf{A}\mathbf{B})=\min\{h(\mathbf{A}),h(\mathbf{B})\}§?""", "Nie, len §\\leq§", "Áno", correct = 0,
        why = """Len nerovnosť: §\begin{pmatrix}1&0\\0&0\end{pmatrix}\begin{pmatrix}0&0\\0&1\end{pmatrix}=\Theta§, hoci obe majú hodnosť §1§.""",
        wrong = mapOf(1 to """Rovnosť neplatí: súčin dvoch matíc hodnosti §1§ môže byť nulová matica. Napríklad §\begin{pmatrix}1&0\\0&0\end{pmatrix}\begin{pmatrix}0&0\\0&1\end{pmatrix}=\Theta§."""))
    prove("la1-k3-p-obraz-lz")
    practice("la1-k3-product", solo = true)
}

internal val k3lInverzia = lesson("la1-k3-inverzia", "Inverzná matica", 18, "Jednotková matica, definícia inverznej a regulárnej matice, jednoznačnosť inverzie a jej pravidlá.", 5, S3) {
    ask("""Existuje matica §\mathbf{B}\in\mathbb{R}^{2,2}§, pre ktorú §\begin{pmatrix}0&0\\0&0\end{pmatrix}\mathbf{B}=\mathbf{E}§ (jednotková matica §2\times 2§)?""", "Áno", "Nie", correct = 1,
        after = """Nie: súčin s nulovou maticou je vždy nulová matica.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """Každý prvok súčinu je súčet súčinov, v ktorých je vždy prvok nulovej matice, teda §0§. Na diagonále §\mathbf{E}§ sú však jednotky (Otázka za Definicí 3.23)."""))
    explore("""Skús nad §\mathbb{R}§ invertovať §\begin{pmatrix}1&2\\2&4\end{pmatrix}§""", "GEM na §(\\mathbf{A}\\mid\\mathbf{E})§. Čo vznikne v ľavom bloku?", Pic.Gem(listOf(row(1, 2, 1, 0), row(2, 4, 0, 1)), 0, bar = 2),
        """Vľavo nulový riadok: §h(\mathbf{A})=1§ a inverzia neexistuje.""", """Táto matica je singulárna. Prečo nulový riadok inverziu vylučuje, ukáže Věta 3.35 v ďalšej lekcii.""")
    explain("Jednotková matica je jednotka násobenia.", """§\mathbf{A}\mathbf{E}=\mathbf{A}=\mathbf{E}\mathbf{A}§ (Pozorování 3.22). K tejto jednotke hľadáme inverzné prvky, ako §2\cdot\tfrac12=1§ pri číslach.""",
        cards = listOf("la1-k3-jednotkova"))
    def("la1-k3-jednotkova")
    explain("Inverzná: súčin v oboch poradiach je §\\mathbf{E}§.", """Regulárna je matica, ku ktorej inverzná **existuje**; ostatné sú singulárne. Len štvorcové matice.""",
        cards = listOf("la1-k3-inverzna"))
    clip("k3-inverzia-definicia")
    check("""Je nad §\mathbb{R}§ §\begin{pmatrix}0&-1\\1&1\end{pmatrix}§ inverzná k §\begin{pmatrix}1&1\\-1&0\end{pmatrix}§?""", "Áno", "Nie", correct = 0,
        why = """Áno: súčin v oboch poradiach je §\mathbf{E}§ (Otázka za Definicí 3.23).""",
        wrong = mapOf(1 to """Sú inverzné, lebo ich súčin v oboch poradiach je §\mathbf{E}§. Over: §\begin{pmatrix}1&1\\-1&0\end{pmatrix}\begin{pmatrix}0&-1\\1&1\end{pmatrix}=\mathbf{E}§."""))
    clip("k3-regularna-len-stvorcova")
    clip("k3-inverzia-nedefinicia")
    def("la1-k3-inverzna")
    explain("Inverzia je jediná, preto smieme písať §\\mathbf{A}^{-1}§.", """Dve inverzné sa musia rovnať. Dôkaz chcela skúška 17. 12. 2025 za 4 body.""",
        cards = listOf("la1-k3-t-jednoznacnost"))
    clip("k3-inverzia-jednoznacnost")
    prove("la1-k3-p-jednoznacnost")
    thm("la1-k3-t-vlastnosti", "la1-k3-t-inv-sucin", "la1-k3-t-inv-transp", proof = "la1-k3-p-inv-sucin")
    clip("k3-inverzia-sucinu-pisomka")
    check("""§\mathbf{A},\mathbf{B}\in T^{n,n}§ sú regulárne. §(\mathbf{A}\mathbf{B})^{-1}=§""", """§\mathbf{B}^{-1}\mathbf{A}^{-1}§""", """§\mathbf{A}^{-1}\mathbf{B}^{-1}§""", """§\mathbf{A}\mathbf{B}^{-1}§""", correct = 0,
        why = """Poradie sa obráti (Věta 3.27).""",
        wrong = mapOf(1 to """Poradie sa obráti. §(\mathbf{A}\mathbf{B})(\mathbf{A}^{-1}\mathbf{B}^{-1})§ vo všeobecnosti nie je §\mathbf{E}§.""",
            2 to """Invertuje sa každý činiteľ, aj §\mathbf{A}§, a poradie sa obráti."""))
    prove("la1-k3-p-inv-transp")
    worked("Skúška 26. 1. 2026, príklad 3B", """Uvažujme regulární matici §\mathbf{B}§ nad §\mathbb{R}§. Upravte výraz §(3\mathbf{B}\mathbf{B}^T)^{-1}§.""", "Skúška 26. 1. 2026 (variant Hilbert), príklad 3B, 4 b", K3AlgebraPostup) {
        val (rules, apply, check) = K3AlgebraPostup
        rules("""Potrebujem dve pravidlá a skalár. Inverzia súčinu obráti poradie, lebo vnútorné činitele sa zrušia (Věta 3.27):""", eq("""(\mathbf{X}\mathbf{Y})(\mathbf{Y}^{-1}\mathbf{X}^{-1})=\mathbf{E}"""))
        rules("""Transpozícia a inverzia sa prehodia, lebo transpozícia súčinu obráti poradie (Věta 3.28):""", eq("""\mathbf{X}^T(\mathbf{X}^{-1})^T=(\mathbf{X}^{-1}\mathbf{X})^T=\mathbf{E}"""))
        apply("""Najprv skalár: §(3\mathbf{X})(\tfrac13\mathbf{X}^{-1})=\mathbf{E}§, teda §(3\mathbf{X})^{-1}=\tfrac13\mathbf{X}^{-1}§.""", eq("""(3\mathbf{B}\mathbf{B}^T)^{-1}=\tfrac13(\mathbf{B}\mathbf{B}^T)^{-1}"""))
        apply("""Potom inverzia súčinu s obráteným poradím; §\mathbf{B}^T§ je regulárna, lebo jej inverzia je §(\mathbf{B}^{-1})^T§ (Věta 3.28).""", eq("""=\tfrac13(\mathbf{B}^T)^{-1}\mathbf{B}^{-1}=\tfrac13(\mathbf{B}^{-1})^T\mathbf{B}^{-1}"""))
        check("""Na papieri pri každom kroku napíš vetu, o ktorú sa opiera. Skúška:""", eq("""(3\mathbf{B}\mathbf{B}^T)\cdot\tfrac13(\mathbf{B}^{-1})^T\mathbf{B}^{-1}=\mathbf{B}\mathbf{B}^T(\mathbf{B}^T)^{-1}\mathbf{B}^{-1}=\mathbf{E}"""))
    }
    practice("la1-k3-algebra", solo = false)
    prove("la1-k3-p-proste")
    practice("la1-k3-algebra", solo = true)
}

/** Příklad 3.38 in Z_7: (A|E) to (E|A^-1), the textbook's own matrices, computed from the operations. */
private val ex338a = Field.Zp(7).walk(intMat(listOf(1, 3, 2, 1, 0, 0), listOf(2, 3, 1, 0, 1, 0), listOf(3, 4, 5, 0, 0, 1), bar = 3),
    listOf(rowSub(2, 2, 1), rowSub(3, 3, 1)), listOf(rowSub(3, 4, 2)))
private val ex338b = Field.Zp(7).walk(intMat(listOf(1, 3, 2, 1, 0, 0), listOf(0, 4, 4, 5, 1, 0), listOf(0, 0, 4, 5, 3, 1), bar = 3),
    listOf(rowSub(1, 4, 3), rowSub(2, 1, 3)), listOf(rowSub(1, 6, 2)), listOf(rowTimes(2, 2.q()), rowTimes(3, 2.q())))

internal val k3lRegularita = lesson("la1-k3-regularita", "Regularita a výpočet inverzie", 20, "GEM ako násobenie regulárnou maticou, veta o regularite s dôkazom, inverzia z §(\\mathbf{A}\\mid\\mathbf{E})§ a úloha zo skúšky.", 6, S3) {
    ask("""Štvorcová §\mathbf{A}\in\mathbb{R}^{3,3}§ má po GEM v HST nulový riadok. Je regulárna?""", "Áno", "Nie", correct = 1,
        after = """Nie: §h(\mathbf{A})<3§, a regulárne sú práve matice s hodnosťou §n§ (Věta 3.35).""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """Nulový riadok znamená §h(\mathbf{A})<3§. Pri regulárnej by §\mathbf{E}=\mathbf{A}\mathbf{A}^{-1}§ mala hodnosť menšiu než §3§ (Věta 3.19)."""))
    explore("Krokuj §(\\mathbf{A}\\mid\\mathbf{E})§ v §\\mathbb{Z}_7§", "Prvá fáza: HST v ľavom bloku. Vznikne v ňom nulový riadok?", Pic.Gem(listOf(row(1, 3, 2, 1, 0, 0), row(2, 3, 1, 0, 1, 0), row(3, 4, 5, 0, 0, 1)), 7, bar = 3, caption = "Skriptá, Příklad 3.38"),
        """Tri nenulové riadky vľavo: §\mathbf{A}§ je regulárna.""", """Druhú fázu, delenie a nulovanie nahor, ukazuje celé riešenie nižšie.""")
    explain("GEM je násobenie regulárnou maticou.", """Výmena riadkov, násobenie riadku a pripočítanie násobku sú násobenia zľava maticami §\mathbf{P}(i,j)§, §\mathbf{P}_i(\alpha)§ a §\mathbf{Q}_{i,j}(\alpha)§. Všetky sú regulárne, a tak aj ich súčin.""",
        cards = listOf("la1-k3-t-gem-matica", "la1-k3-t-regularna-gem", "la1-k3-t-regularna-hodnost"))
    clip("k3-regularita-gem-matice")
    worked("Matica, ktorá robí GEM", """Úpravy nad §\mathbb{R}§ §\begin{pmatrix}6&15&0\\2&4&1\end{pmatrix}\sim\begin{pmatrix}2&4&1\\6&15&0\end{pmatrix}\sim\begin{pmatrix}2&4&1\\0&3&-3\end{pmatrix}\sim\begin{pmatrix}2&4&1\\0&1&-1\end{pmatrix}§: aká matica §\mathbf{P}§ im zodpovedá?""", "Skriptá, Příklad 3.33", K3OpsMatrixPostup) {
        val (each, product, check) = K3OpsMatrixPostup
        // each operation done on E gives its matrix (Poznámky 3.29 až 3.31); the product is computed, not typed
        val ops = listOf(rowSwap(1, 2), rowSub(2, 3, 1), rowTimes(2, Frac.of(1, 3)))
        val e2 = intMat(listOf(1, 0), listOf(0, 1))
        val ps = ops.map { Field.R.applyOp(e2, it) }
        each("""Každej úprave zodpovedá matica, ktorá vznikne tou istou úpravou z §\mathbf{E}§.""",
            Formula(ps.joinToString(",\\quad ") { it.tex() }, "(G1) ${'$'}${ops[0].tex()}${'$'}, (G3) ${'$'}${ops[1].tex()}${'$'}, (G2) ${'$'}${ops[2].tex()}${'$'}"))
        product("""Matice úprav vynásobím tak, že posledná úprava je najviac vľavo.""",
            Formula("""\mathbf{P}=""" + ps.reversed().joinToString("") { it.tex() } + "=" + ps.reversed().reduce { a, b -> Field.R.times(a, b) }.tex()))
        check("""Tú istú §\mathbf{P}§ dostaneš, keď tie isté úpravy urobíš na §\mathbf{E}§. Na tom stojí výpočet inverzie.""")
    }
    check("""Ktorá matica pri násobení zľava vymení 1. a 2. riadok matice z §T^{2,n}§?""", """§\begin{pmatrix}0&1\\1&0\end{pmatrix}§""", """§\begin{pmatrix}1&0\\0&1\end{pmatrix}§""", """§\begin{pmatrix}1&1\\0&1\end{pmatrix}§""", correct = 0,
        why = """§\mathbf{E}§ s vymenenými riadkami vymení riadky (Poznámka 3.29).""",
        wrong = mapOf(1 to """§\mathbf{E}§ nič nemení: §\mathbf{E}\mathbf{A}=\mathbf{A}§ (Pozorování 3.22).""",
            2 to """Táto matica k prvému riadku pripočíta druhý. Výmenu robí §\mathbf{E}§ s vymenenými riadkami."""))
    explain("Regulárna, hodnosť §n§, §\\mathbf{A}\\sim\\mathbf{E}§: to isté.", """Najdôležitejšia veta kapitoly, na skúške ju treba vysloviť aj dokázať. Riadky aj stĺpce regulárnej matice sú LN.""",
        cards = listOf("la1-k3-t-regularita"))
    clip("k3-regularita-kruh-implikacii")
    thm("la1-k3-t-regularita")
    clip("k3-regularita-hodnost-chyba")
    check("""V dôkaze 1. §\Rightarrow§ 2. máš §n=h(\mathbf{E})=h(\mathbf{A}\mathbf{A}^{-1})\leq h(\mathbf{A})§. Čo ešte chýba?""", "§h(\\mathbf{A})\\leq n§", "§h(\\mathbf{A}^{-1})=n§", "Nič", correct = 0,
        why = """Horná hranica: matica typu §n\times n§ má hodnosť najviac §n§ (Důsledek 3.13).""",
        wrong = mapOf(1 to """Hodnosť inverzie nepotrebuješ. Chýba len horná hranica §h(\mathbf{A})\leq n§.""",
            2 to """Z §n\leq h(\mathbf{A})§ rovnosť sama neplynie. Treba aj §h(\mathbf{A})\leq n§."""))
    explain("Inverzia z §(\\mathbf{A}\\mid\\mathbf{E})§", """Tie isté úpravy robíš naraz na §\mathbf{A}§ aj na §\mathbf{E}§. Nulový riadok vľavo: singulárna, koniec. Inak vľavo vyrobíš §\mathbf{E}§ a vpravo je §\mathbf{A}^{-1}§.""",
        cards = listOf("la1-k3-t-postup"))
    clip("k3-regularita-inverzia-ae")
    worked("Inverzia v §\\mathbb{Z}_7§", """Spočítejte inverzní matici k matici §\mathbf{A}=\begin{pmatrix}1&3&2\\2&3&1\\3&4&5\end{pmatrix}\in \mathbb{Z}_7^{3,3}§.""", "Skriptá, Příklad 3.38", K3InversePostup) {
        val (hst, regular, back, result) = K3InversePostup
        hst("""Vedľa §\mathbf{A}§ napíšem §\mathbf{E}§ a ľavý blok upravím na HST, modulo §7§.""", Formula(ex338a, "modulo §7§"))
        regular("""Ľavý blok má tri nenulové riadky, §h(\mathbf{A})=3§. Hodnosť je plná, preto GEM dostane vľavo §\mathbf{E}§ a tie isté úpravy urobia vpravo z §\mathbf{E}§ inverziu. Matica je teda regulárna (Věta 3.35).""")
        back("""Nad diagonálou vynulujem a každý riadok vynásobím inverziou jeho prvého nenulového prvku.""", Formula(ex338b, "Napríklad §4^{-1}=2§ v §\\mathbb{Z}_7§"))
        result("""Vľavo je §\mathbf{E}§, vpravo inverzia. Skúška: §\mathbf{A}\mathbf{A}^{-1}=\mathbf{E}§ modulo §7§, stačí jeden súčin, lebo pri štvorcových maticiach z §\mathbf{A}\mathbf{X}=\mathbf{E}§ plynie aj §\mathbf{X}\mathbf{A}=\mathbf{E}§ (Věta 3.42).""", Formula("""\mathbf{A}^{-1}=""" + Field.Zp(7).jordan(intMat(listOf(1, 3, 2, 1, 0, 0), listOf(2, 3, 1, 0, 1, 0), listOf(3, 4, 5, 0, 0, 1), bar = 3), 3).result.let { r -> Mat(r.rows.map { it.drop(3) }) }.tex()))
    }
    practice("la1-k3-inverse", solo = false)
    explain("Stačí jeden súčin.", """Pre štvorcové matice z §\mathbf{A}\mathbf{B}=\mathbf{E}§ plynie aj §\mathbf{B}\mathbf{A}=\mathbf{E}§. Skúšku inverzie preto robíš jedným násobením.""",
        cards = listOf("la1-k3-t-jedna-strana"))
    clip("k3-regularita-jedna-strana")
    worked("Skúška 26. 1. 2026, príklad 2", """Mějme matici s parametrem §a\in\mathbb{R}§: §\mathbf{A}=\begin{pmatrix}-1&1+a&-2+a\\1&-2&1\\0&-1-a&2\end{pmatrix}\in\mathbb{R}^{3,3}§. A) (4 b) Spočtěte hodnost §\mathbf{A}§ v závislosti na parametru. B) (2 b) Určete, pro které hodnoty parametru §a§ je matice §\mathbf{A}§ regulární. C) (4 b) Spočtěte inverzní matici k §\mathbf{A}§ pro hodnotu §a=0§.""", "Skúška 26. 1. 2026 (variant Hilbert), príklad 2, 10 b",
        rankParamSolution(hilbertCase()))
    practice("la1-k3-rank-param", solo = false)
    explain("Koľko je regulárnych matíc v §\\mathbb{Z}_p^{2,2}§", """Regulárna má LN riadky: prvý nenulový (§p^2-1§ možností), druhý mimo obalu prvého (§p^2-p§ možností).""",
        formal = k3tRegularita.remarks.single().lines, src = "Skriptá, Poznámka 3.36")
    practice("la1-k3-count", solo = false)
    practice("la1-k3-inverse", solo = true)
    practice("la1-k3-rank-param", solo = true)
}

internal val K3Lessons = listOf(k3lHodnost, k3lAplikacie, k3lNasobenie, k3lInverzia, k3lRegularita)

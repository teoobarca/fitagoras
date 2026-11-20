package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.la.*

/*
 * The three lessons of kapitola 4 (slajdy 4): Frobeniova veta, celá množina riešení, lineárne variety. Exam problems are
 * solved by the same code as the exercises; hand-written numbers are checked in Chapter4ContentTest.
 */

private val S4 = listOf("S4")
private fun row(vararg xs: Int) = xs.toList()
private val Z5 = Field.Zp(5)
private val Z3 = Field.Zp(3)

/** Skúška 17. 12. 2025 (variant Frobenius), príklad 2B: the augmented matrix over Z_5. */
internal val FrobeniusA = Z5.mat(listOf(row(1, 0, 2, 1, 1, 0, 1), row(0, 0, 1, 1, 0, 1, 1), row(0, 0, 0, 0, 2, 4, 1), row(0, 0, 0, 0, 0, 0, 3)))
internal val FrobeniusB = Z5.vec(listOf(1, 2, 0, 2))

/** Skúška 26. 1. 2026 (variant Hilbert), príklad 3D: x+y+u+v=2, x+z+2u=1 in Z_3^5. */
internal val HilbertVA = Z3.mat(listOf(row(1, 1, 0, 1, 1), row(1, 0, 1, 2, 0)))
internal val HilbertVB = Z3.vec(listOf(2, 1))

/** MARAST 14158: the plane through a, b, c in Z_5^4. */
internal val MarastPts = listOf(Z5.vec(listOf(1, 1, 1, 1)), Z5.vec(listOf(1, 1, 2, 2)), Z5.vec(listOf(2, 2, 2, 1)))

private fun marastSolution(): Solution {
    val (a, b, c) = MarastPts
    val us = listOf(Z5.plus(b, Z5.scale(-Frac.ONE, a)), Z5.plus(c, Z5.scale(-Frac.ONE, a)))
    val (am, bv) = Z5.equationsOf(a, us)
    val names = listOf("x", "y", "z", "u")
    val (dirs, homog, rows, eqs) = PlanePostup
    return PlanePostup.solve {
        dirs("""Posunutie je §\mathbf{a}=${a.tex()}§, smerové vektory sú rozdiely bodov modulo §5§.""", eq("""\mathbf{b}-\mathbf{a}=${us[0].tex()},\quad \mathbf{c}-\mathbf{a}=${us[1].tex()}"""))
        dirs("""Nie sú násobkami, sú LN, takže je to rovina §\mathbf{a}+\langle\mathbf{b}-\mathbf{a},\mathbf{c}-\mathbf{a}\rangle§. Parametrické rovnice dostanem rozpisom bodu roviny po zložkách, lebo tak ich definuje Poznámka 4.31:""",
            listOf(eq("""(x,y,z,u)=${a.tex()}+s${us[0].tex()}+t${us[1].tex()}"""), Formula(parametricTex(Z5, names, a, us, listOf("s", "t")))))
        homog("""Pre neparametrické rovnice riešim homogénnu sústavu so smerovými vektormi v riadkoch (Postup 4.30).""", gemLines(Z5.echelon(Mat(us))))
        val hs = Z5.solveAll(Mat(us), List(2) { Frac.ZERO })
        homog("""Voľné premenné sú §${hs.free.joinToString(",") { names[it] }}§: postupne jednu položím §1§, ostatné §0§, a viazané dopočítam modulo §5§. Tak dostanem bázu riešení.""", hs.kernel.map { Formula(it.tex()) })
        rows("""Báza riešení ide do riadkov §\mathbf{A}§, pravá strana je §\mathbf{A}\mathbf{a}§.""", eq("""\mathbf{A}=${am.tex()},\quad \mathbf{A}\mathbf{a}=${bv.tex()}^T"""))
        eqs("""Riadky prepíšem na rovnice.""", eq(am.rows.indices.joinToString(",\\quad ") { equationTex(am.rows[it], bv[it], names) }))
        eqs("""MARAST má inú, rovnako správnu sústavu §x+4y=0§, §4y+z+4u=4§. Obe majú za riešenie tú istú rovinu: tri body ich spĺňajú a hodnosť je §2§.""")
    }
}

private fun hilbertVarietySolution(): Solution {
    val s = Z3.solveAll(HilbertVA, HilbertVB)
    val names = listOf("x", "y", "z", "u", "v")
    val (solve, param, member) = K4ParamPostup
    return K4ParamPostup.solve {
        solve("""Rovnice sú sústava §(\mathbf{A}\mid\mathbf{b})§ nad §\mathbb{Z}_3§; parametrický tvar je jej množina riešení (Postup 4.10).""")
        frobenius(Z3, HilbertVA, s, names, FrobeniusAims(solve, solve, solve, solve, solve))
        val ts = s.kernel.indices.map { "t_{${it + 1}}" }
        param("""Parametrický tvar je bod plus kombinácie bázy §S_0§; dimenzia je počet parametrov, §5-2=3§. Parametrické rovnice z nej dostanem rozpisom po zložkách, lebo tak ich definuje Poznámka 4.31.""",
            listOf(eq("(x,y,z,u,v)=${s.particular!!.tex()}" + s.kernel.indices.joinToString("") { "+${ts[it]}${s.kernel[it].tex()}" }), Formula(parametricTex(Z3, names, s.particular!!, s.kernel, ts))))
        member("""Príslušnosť vektora sa overí dosadením do oboch rovníc. Vektor zo zadania tu chýba: v prepise z fit-wiki má 4 zložky v §\mathbb{Z}_3^5§, ten kus je nečitateľný.""")
    }
}

internal val k4lFrobenius = lesson("la1-k4-frobenius", "Frobeniova veta", 16, "Kedy má sústava riešenie, ako vyzerajú všetky riešenia a akú dimenziu má §S_0§.", 7, S4) {
    ask("""Matica §\mathbf{A}\in\mathbb{R}^{2,2}§ má stĺpce §(1,2)^T§ a §(2,4)^T§. Má sústava §\mathbf{A}\mathbf{x}=(3,1)^T§ riešenie?""", "Áno", "Nie", correct = 1,
        after = """Nie: oba stĺpce ležia na priamke §\langle(1,2)\rangle§ a §(3,1)§ na nej neleží.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """Kombinácie §(1,2)^T§ a §(2,4)^T§ sú len násobky §(1,2)^T§. Vektor §(3,1)^T§ medzi nimi nie je."""))
    explore("""Skús §x_1(1,2)^T+x_2(2,4)^T=(3,1)^T§.""", "Posúvaj koeficienty pri stĺpcoch.", Pic.Plane(1 to 2, 2 to 4, 3 to 1, range = 2, goal = Goal.Explore),
        """Z priamky §y=2x§ neujdeš: §\mathbf{b}§ nie je v obale stĺpcov.""", """§h(\mathbf{A})=1§, ale s pravou stranou §h(\mathbf{A}\mid\mathbf{b})=2§. Presne toto rozhoduje Frobeniova veta.""")
    explain("Riešenie existuje, keď §\\mathbf{b}§ nepridá hodnosť.", """Sústava hľadá kombináciu stĺpcov rovnú §\mathbf{b}§. Tá existuje práve vtedy, keď §\mathbf{b}§ leží v obale stĺpcov, teda §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§.""",
        cards = listOf("la1-k4-t-frobenius"))
    clip("k4-frobenius-pisomka")
    clip("k4-frobenius-deklaruj")
    check("""§h(\mathbf{A})=2§, §h(\mathbf{A}\mid\mathbf{b})=3§. Sústava §\mathbf{A}\mathbf{x}=\mathbf{b}§""", "nemá riešenie", "má práve jedno riešenie", "má nekonečne veľa riešení", correct = 0,
        why = """Pravá strana zvýšila hodnosť, riešenie neexistuje (bod 1).""",
        wrong = mapOf(1 to """Riešenie neexistuje vôbec: §h(\mathbf{A})\neq h(\mathbf{A}\mid\mathbf{b})§ (Frobeniova veta, bod 1).""",
            2 to """O počte riešení má zmysel hovoriť len pri riešiteľnej sústave. Tu sa hodnosti líšia, riešenie nie je žiadne."""))
    clip("k4-frobenius-dokaz-hodnosti")
    thm("la1-k4-t-frobenius")
    prove("la1-k4-p-frob-nutna")
    explain("Všetky riešenia: jedno plus homogénne.", """§S=\tilde{\mathbf{x}}+S_0§, kde §S_0§ je podpriestor riešení §\mathbf{A}\mathbf{x}=\theta§ dimenzie §n-h(\mathbf{A})§.""",
        cards = listOf("la1-k4-t-homogenna"))
    clip("k4-frobenius-dimenzia-s0")
    check("""§\mathbf{A}\in\mathbb{R}^{3,5}§ a §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})=3§. §\dim S_0=§""", "§2§", "§3§", "§5§", correct = 0,
        why = """§\dim S_0=n-h(\mathbf{A})=5-3=2§ (bod 3).""",
        wrong = mapOf(1 to """§3§ je hodnosť. Dimenzia §S_0§ je §n-h(\mathbf{A})=5-3§.""",
            2 to """§5§ je počet neznámych. Každá nezávislá rovnica zníži dimenziu o jeden."""))
    clip("k4-frobenius-vedlajsie-stlpce")
    explain("Regulárna matica: práve jedno riešenie", """Pri regulárnej §\mathbf{A}§ je §h(\mathbf{A})=n=h(\mathbf{A}\mid\mathbf{b})§ a §S_0=\{\theta\}§, teda §\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}§.""",
        cards = listOf("la1-k4-t-regularna"))
    check("""§\mathbf{A}\in\mathbb{R}^{4,4}§ je regulárna. Sústava §\mathbf{A}\mathbf{x}=\mathbf{b}§ má""", "práve jedno riešenie pre každé §\\mathbf{b}§", "riešenie len pre niektoré §\\mathbf{b}§", "nekonečne veľa riešení", correct = 0,
        why = """Plná hodnosť: riešenie vždy existuje a je jediné, §\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}§.""",
        wrong = mapOf(1 to """Pri regulárnej je §h(\mathbf{A})=4§ a pravá strana hodnosť nezvýši. Matica §(\mathbf{A}\mid\mathbf{b})§ má tiež len štyri riadky, hodnosť nad §4§ mať nemôže; preto §h(\mathbf{A}\mid\mathbf{b})=h(\mathbf{A})§ pre každé §\mathbf{b}§ a podľa Frobeniovej vety riešenie vždy existuje.""",
            2 to """§\dim S_0=4-4=0§, teda §S_0=\{\theta\}§. Všetky riešenia sú §\tilde{\mathbf{x}}+S_0=\{\tilde{\mathbf{x}}\}§, teda jediné, a existuje, lebo pravá strana hodnosť §4§ nezvýši."""))
    worked("Frobeniova veta na malej sústave", """Rozhodněte o řešitelnosti a popište množinu řešení soustavy §\begin{pmatrix|3}1&2&-1&2\\2&4&1&7\\1&2&2&5\end{pmatrix}§ nad §\mathbb{R}§.""", "Postup podľa Věty 4.2", FrobeniusPostup) {
        val a = intMat(listOf(1, 2, -1), listOf(2, 4, 1), listOf(1, 2, 2))
        frobenius(Field.R, a, Field.R.solveAll(a, vec(2, 7, 5)))
        val (_, _, _, _, set) = FrobeniusPostup
        set("""S pravou stranou §(2,7,6)^T§ by vyšiel riadok §(0\ 0\ 0\mid 1)§ a §S=\emptyset§.""")
    }
    practice("la1-k4-solve", solo = false)
    check("Na skúške píšeš kompletné znenie Frobeniovej vety. Čo napíšeš ako prvé?", "§\\mathbf{A}\\in T^{m,n}§, §\\mathbf{b}\\in T^{m,1}§ a sústava §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§", "Sústava je riešiteľná práve vtedy, keď §h(\\mathbf{A})=h(\\mathbf{A}\\mid\\mathbf{b})§", "§S=\\tilde{\\mathbf{x}}+S_0§", correct = 0,
        why = """Bez deklarácie nedávajú §n§, §\mathbf{b}§ ani §S_0§ zmysel.""",
        wrong = mapOf(1 to """Je to bod 1, ale bez deklarácie §\mathbf{A}§ a §\mathbf{b}§ nevieš, čo je §n§ v bode 3.""",
            2 to """Toto je bod 2 a predpokladá riešiteľnosť aj partikulárne riešenie. Najprv deklarácia, potom tri body."""))
    practice("la1-k4-solve", solo = true)
}

internal val k4lRiesenie = lesson("la1-k4-riesenie", "Celá množina riešení", 18, "Viazané a voľné premenné, partikulárne riešenie, báza §S_0§ a zápis riešení ako varieta, ako na skúške.", 1, S4) {
    ask("""Homogénna sústava §\mathbf{A}\mathbf{x}=\theta§ má §5§ neznámych a §h(\mathbf{A})=3§. Koľko vektorov má báza podpriestoru jej riešení §S_0§?""", "§2§", "§3§", "§5§", correct = 0,
        after = """§5-3=2§: jeden vektor za každú voľnú premennú.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(1 to """Tri sú viazané premenné, pri hlavných stĺpcoch. Báza §S_0§ má vektor za každú voľnú: §5-3=2§.""",
            2 to """Päť je neznámych. Vektory bázy §S_0§ sú len pre voľné premenné."""))
    explore("Krokuj sústavu z Príkladu 4.12", "Ktoré stĺpce budú hlavné a ktoré premenné voľné?", Pic.Gem(listOf(row(1, 3, 2, 0, 3, 3), row(1, 1, 1, -1, 5, -2), row(2, 8, 5, 3, 7, 13), row(3, 9, 6, 2, 12, 11)), 0, bar = 5, caption = "Skriptá, Příklad 4.12"),
        """Hlavné stĺpce 1, 2, 4: viazané §x_1,x_2,x_4§, voľné §x_3,x_5§.""", """Pravá strana nie je hlavný stĺpec, riešenie existuje. §\dim S_0=5-3=2§.""")
    explain("Viazané a voľné premenné", """Do voľných dosadíš čokoľvek, viazané dopočítaš. Partikulárne: voľné §0§, s pravou stranou. Báza §S_0§: voľné postupne §\mathbf{e}_1,\mathbf{e}_2,\dots§, bez pravej strany.""",
        cards = listOf("la1-k4-t-postup", "la1-k4-t-postup-nehomogenna"))
    clip("k4-riesenie-posunutie")
    clip("k4-riesenie-volne-premenne")
    worked("Príklad zo skrípt", """Nalezněte řešení soustavy §\begin{pmatrix|5}1&3&2&0&3&3\\1&1&1&-1&5&-2\\2&8&5&3&7&13\\3&9&6&2&12&11\end{pmatrix}§ nad §\mathbb{R}§.""", "Skriptá, Příklady 4.6 a 4.12", FrobeniusPostup) {
        val a = intMat(listOf(1, 3, 2, 0, 3), listOf(1, 1, 1, -1, 5), listOf(2, 8, 5, 3, 7), listOf(3, 9, 6, 2, 12))
        frobenius(Field.R, a, Field.R.solveAll(a, vec(3, -2, 13, 11)))
        val (_, _, _, _, set) = FrobeniusPostup
        set("""Skriptá vektory bázy vynásobia, aby zmizli zlomky: §\langle(-1,-1,2,0,0),(-33,7,0,-6,4)\rangle§ je ten istý obal.""")
    }
    check("Bázu §S_0§ počítaš z", "§(\\mathbf{A}\\mid\\theta)§", "§(\\mathbf{A}\\mid\\mathbf{b})§", correct = 0,
        why = """§S_0§ sú riešenia homogénnej sústavy, pravá strana nulová (Varování 4.11).""",
        wrong = mapOf(1 to """S pravou stranou vyjdú riešenia pôvodnej sústavy, nie vektory z §S_0§. Vektor §\mathbf{z}\in S_0§ musí spĺňať §\mathbf{A}\mathbf{z}=\theta§, preto pravú stranu zakry (Varování 4.11)."""))
    clip("k4-riesenie-pekna-baza")
    check("""Smieš do §(x_3,x_5)§ dosadiť §(2,0)§ a §(1,2)§ namiesto §(1,0)§ a §(0,1)§?""", "Áno", "Nie", correct = 0,
        why = """Áno: stačí ľubovoľná báza priestoru voľných premenných (Postup 4.5).""",
        wrong = mapOf(1 to """Postup 4.5 dovoľuje ľubovoľnú bázu §T^{n-h}§. Skriptá tak v Príklade 4.6 obchádzajú zlomky."""))
    clip("k4-riesenie-iny-zapis")
    check("""Tebe vyšlo §S=(1,0)+\langle(1,1)\rangle§, spolužiakovi §S=(2,1)+\langle(2,2)\rangle§. Opisujete tú istú množinu?""", "Áno", "Nie, líšia sa bodom aj smerom", "Nie, zápis je jednoznačný", correct = 0,
        why = """Áno: rozdiel §(2,1)-(1,0)=(1,1)§ leží v zameraní a §\langle(2,2)\rangle=\langle(1,1)\rangle§ (Věta 4.18, bod 4). Správnosť voči sústave to neoverí: Kleprlík hovorí, že potom máte obaja rovnako dobre, alebo rovnako zle.""",
        wrong = mapOf(1 to """Iný bod a iný smerový vektor môžu opísať tú istú priamku. Bod §(2,1)§ na nej leží a §(2,2)§ je násobok §(1,1)§.""",
            2 to """Zápis množiny riešení jednoznačný nie je. Bod aj bázu zamerania môžeš zvoliť inak."""))
    clip("k4-riesenie-prehodenie-stlpcov")
    worked("Skúška 17. 12. 2025, príklad 2B", """S využitím Frobeniovy věty popište množinu řešení soustavy nad tělesem §\mathbb{Z}_5§, jejíž rozšířená matice je §\begin{pmatrix|7}1&0&2&1&1&0&1&1\\0&0&1&1&0&1&1&2\\0&0&0&0&2&4&1&0\\0&0&0&0&0&0&3&2\end{pmatrix}§. Řešení zapište ve tvaru variety.""", "Skúška 17. 12. 2025 (variant Frobenius), príklad 2B, 7 b",
        frobeniusSolution(Z5, FrobeniusA, Z5.solveAll(FrobeniusA, FrobeniusB)))
    practice("la1-k4-solve", solo = false)
    check("""Skúška 2. 2. 2026: §\mathbf{A}\in\mathbb{Z}_5^{4,7}§ v HST má štyri nenulové riadky. Pre koľko pravých strán s nulovou poslednou zložkou má sústava riešenie?""", "§125§", "§0§", "§625§", correct = 0,
        why = """Pre všetky: §h(\mathbf{A})=4§ a pravá strana hodnosť nezvýši; s §b_4=0§ je ich §5^3=125§.""",
        wrong = mapOf(1 to """Prvý nenulový prvok každého riadku vľavo od čiary znamená, že §h(\mathbf{A}\mid\mathbf{b})=4=h(\mathbf{A})§. Pridaný stĺpec nemôže dať hodnosť nad §4§, lebo riadky sú len štyri; podľa Frobeniovej vety je teda riešiteľná sústava s každou pravou stranou.""",
            2 to """§625=5^4§ sú všetky pravé strany. Zadanie chce len tie s nulovou poslednou zložkou: §5^3=125§."""))
    practice("la1-k4-solve", solo = true)
}

internal val k4lVariety = lesson("la1-k4-variety", "Lineárne variety", 20, "Varieta ako posunutý podpriestor, jej zameranie a dimenzia, prečo je každá varieta množinou riešení a prechod medzi rovnicami a parametrami.", 0, S4) {
    ask("""Je množina §\{(x,y)\in\mathbb{R}^2 : y=2x+1\}§, priamka v rovine, podpriestor §\mathbb{R}^2§?""", "Áno", "Nie", correct = 1,
        after = """Nie, neobsahuje §\theta§. Je to však posunutý podpriestor, varieta.""", sub = "Ešte bez výkladu. Stačí odhad.",
        wrong = mapOf(0 to """§(0,0)§ na nej neleží, a každý podpriestor obsahuje §\theta§ (Pozorování 2.8)."""))
    explore("""Body variety §(2,1)+\langle(1,0)\rangle§""", """Prvý koeficient nechaj §1§ (posunutie §(2,1)§), druhým sa posúvaj po smere §(1,0)§. Dostaň sa do §(4,1)§.""", Pic.Plane(2 to 1, 1 to 0, 4 to 1, range = 3),
        """§1\cdot(2,1)+2\cdot(1,0)=(4,1)§: posunutie plus vektor zo zamerania.""", """Všetky body §(2,1)+t(1,0)§ tvoria priamku §y=1§, rovnobežnú s osou §x§ (Obrázok v skriptách pri Poznámke 4.17).""")
    explain("Varieta je posunutý podpriestor.", """Množina riešení riešiteľnej sústavy je presne taká: §S=\tilde{\mathbf{x}}+S_0§. Podpriestor je varieta s posunutím §\theta§.""",
        formal = listOf(p("""Máme-li §W=\mathbf{a}+P§ varietu z předchozí definice, potom"""), i("""vektor §\mathbf{a}§ nazýváme **vektorem posunutí variety** §W§,"""), i("""podprostor §P§ nazýváme **zaměřením** variety §W§ a značíme ho §Z(W)§,"""), i("""každý nenulový vektor ze §Z(W)§ nazýváme **směrovým vektorem variety** §W§,"""), i("""číslo §\dim Z(W)§ nazýváme **dimenzí variety** §W§.""")), src = "Skriptá, Poznámka 4.17", cards = listOf("la1-k4-varieta"))
    clip("k4-varieta-definicia")
    clip("k4-varieta-zamerenie")
    check("""Dimenzia variety §(1,2,3)+\langle(1,0,0),(0,1,0)\rangle\subseteq\mathbb{R}^3§ je""", "§2§", "§3§", "§1§", correct = 0,
        why = """Dimenzia variety je dimenzia zamerania: rovina, §2§.""",
        wrong = mapOf(1 to """Tri zložky má vektor posunutia, nie zameranie. Dimenzia je §\dim Z(W)=2§.""",
            2 to """Zameranie má dva LN vektory, je to rovina. Tie dva vektory zameranie generujú a sú LN, teda tvoria jeho bázu, a dimenzia variety je §2§."""))
    def("la1-k4-varieta")
    explain("Posunúť môžeš o ktorýkoľvek bod.", """Vektor posunutia nie je jednoznačný, zameranie áno. Variety dimenzie §0,1,2,n-1§ sú bod, priamka, rovina, nadrovina.""",
        cards = listOf("la1-k4-t-varieta"))
    clip("k4-varieta-lubovolny-bod")
    clip("k4-varieta-dva-zapisy")
    check("""§\mathbf{b}\in W=\mathbf{a}+P§. Čo platí?""", "§W=\\mathbf{b}+P§", "§W=\\mathbf{b}+\\langle\\mathbf{b}\\rangle§", "§\\mathbf{b}=\\mathbf{a}§", correct = 0,
        why = """Každý bod variety je jej vektorom posunutia (Věta 4.18, bod 2).""",
        wrong = mapOf(1 to """Zameranie sa nemení, je jednoznačné (Věta 4.18, bod 3). Mení sa len vektor posunutia.""",
            2 to """Vektor posunutia jednoznačný nie je. Ktorýkoľvek bod variety poslúži."""))
    explain("Každý podpriestor je množina riešení.", """Výpočet má tri kroky: bázu §P§ napíš do riadkov, vyrieš homogénnu sústavu a bázu jej riešení napíš do riadkov §\mathbf{A}§. Klouda chce, aby ste to vedeli spočítať aj dokázať; na skúške 26. 1. 2026 bol dôkaz za 6 b.""",
        cards = listOf("la1-k4-t-podpriestor-slr"))
    clip("k4-sustava-vypocet")
    prove("la1-k4-p-podpriestor-slr")
    thm("la1-k4-t-varieta-slr", "la1-k4-t-nadrovina")
    explain("Parametricky a neparametricky", """Parametrický tvar: bod plus kombinácie smerových vektorov. Neparametrický: sústava rovníc. Z rovníc k parametrom sústavu vyriešiš, opačne ideš Postupom 4.30.""",
        cards = listOf("la1-k4-t-prevod"))
    worked("Rovina cez tri body v §\\mathbb{R}^4§", """Nalezneme neparametrické rovnice roviny v §\mathbb{R}^{4}§, která obsahuje vektory §(1,2,1,0)§, §(2,3,2,1)§ a §(3,2,4,0)§.""", "Skriptá, Příklad 4.33; Klouda, 8. prednáška (parametrický tvar)", planeSolution(Field.R, listOf(vec(1, 2, 1, 0), vec(2, 3, 2, 1), vec(3, 2, 4, 0))))
    practice("la1-k4-sustava", solo = false)
    worked("Rovina v §\\mathbb{Z}_5^4§ dvoma spôsobmi", """Najděte parametrické a neparametrické rovnice roviny v §\mathbb{Z}_5^4§, která obsahuje vektory §\mathbf{a}=(1,1,1,1)§, §\mathbf{b}=(1,1,2,2)§ a §\mathbf{c}=(2,2,2,1)§.""", "MARAST, ukážková zkoušková písemka (lekce 262), príklad 14158",
        marastSolution())
    practice("la1-k4-param", solo = false)
    worked("Skúška 26. 1. 2026, príklad 3D", """Uvažujte varietu v §\mathbb{Z}_3^5§ zadanou neparametricky: §x+y+u+v=2§, §x+z+2u=1§. Napište její parametrické vyjádření. Jakou má varieta dimenzi?""", "Skúška 26. 1. 2026 (variant Hilbert), príklad 3D, 6 b (zadanie podľa prepisu z fit-wiki)",
        hilbertVarietySolution())
    practice("la1-k4-equations", solo = true)
    practice("la1-k4-param", solo = true)
}

internal val K4Lessons = listOf(k4lFrobenius, k4lRiesenie, k4lVariety)

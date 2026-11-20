package dev.hamper.tabula.data

import dev.hamper.tabula.la.*

/*
 * The nine lessons of kapitola 2. Each step says where it rests: textbook items by number, past tests by date,
 * clips by lecture. Numbers in worked examples are the textbook's or are checked in CoreTest.
 */

private class L(val id: String) {
    val steps = ArrayList<Step>()
    private var n = 0
    private fun next() = "$id-${n++}"
    fun ask(q: String, vararg o: String, correct: Int, after: String, pic: Pic? = null, sub: String? = null, wrong: Map<Int, String> = emptyMap()) { steps += Step.Ask(next(), m(q), o.map(::m), correct, m(after), pic, sub?.let(::m), wrong.mapValues { m(it.value) }) }
    fun explore(title: String, sub: String, pic: Pic, done: String, body: String? = null) { steps += Step.Explore(next(), m(title), m(sub), pic, m(done), body?.let(::m)) }
    fun explain(title: String, body: String, pic: Pic? = null, formal: List<Line>? = null, src: String? = null, more: String? = null, cards: List<String> = emptyList()) { steps += Step.Explain(next(), m(title), m(body), pic, formal, src, more?.let(::m), cards) }
    fun worked(title: String, problem: String, src: String?, postup: Postup, solve: SolutionScope.() -> Unit) { steps += Step.Worked(next(), m(title), m(problem), postup.solve(solve), src) }
    fun worked(title: String, problem: String, src: String?, solution: Solution) { steps += Step.Worked(next(), m(title), m(problem), solution, src) }
    fun check(q: String, vararg o: String, correct: Int, why: String, hint: String? = null, pic: Pic? = null, sub: String? = null, wrong: Map<Int, String> = emptyMap()) { steps += Step.Check(next(), m(q), o.map(::m), correct, m(why), hint?.let(::m), pic, wrong.mapValues { m(it.value) }, sub?.let(::m)) }
    fun practice(gen: String, solo: Boolean, title: String? = null) { steps += Step.Practice(next(), gen, solo, title) }
    fun clip(c: String) { steps += Step.Clip(next(), c) }
    fun def(c: String) { steps += Step.Def(next(), c) }
    fun thm(t: String, vararg also: String, proof: String? = null) { steps += Step.Thm(next(), t, also.toList(), proof) }
    fun prove(p: String) { steps += Step.Prove(next(), p) }
}

private fun lesson(id: String, name: String, minutes: Int, learn: String, art: Int, body: L.() -> Unit) = LessonDef(id, name, minutes, m(learn), L(id).apply(body).steps, art, listOf("S2"))
private fun cz(vararg s: String) = s.map { Para(m(it)) }

val La1Lessons: List<LessonDef> = listOf(

    lesson("tn", "Vektorový priestor", 12, "Čo je §T^n§, ako sa v ňom počíta a čo v ňom nie je definované.", 0) {
        ask("""Koľko je §(1,2,0,4)+(3,3,0,1)§ v §\mathbb{Z}_5^4§?""", "§(4,5,0,5)§", "§(4,0,0,0)§", "§(4,0,0,5)§", correct = 1,
            after = """V §\mathbb{Z}_5§ sú len čísla §0,\dots,4§. Každú zložku zvlášť: §2+3=5\equiv 0\pmod{5}§ a §4+1=5\equiv 0\pmod{5}§.""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """Takto sa sčíta v §\mathbb{Z}^4§, nie v §\mathbb{Z}_5^4§. Číslo §5§ nie je prvok §\mathbb{Z}_5=\{0,\dots,4\}§, každú zložku treba vziať modulo §5§.""", 2 to """Modulo §5§ si vzal len v druhej zložke. Platí v každej zložke zvlášť, aj vo štvrtej: §5§ nie je prvok §\mathbb{Z}_5§."""))
        explore("""Sčítaj šípky: dostaň sa do §(3,4)§.""", """Posúvaj koeficienty pri §\mathbf{u}=(2,1)§ a §\mathbf{v}=(1,3)§.""", Pic.Plane(2 to 1, 1 to 3, 3 to 4, range = 3),
            """§1\mathbf{u}+1\mathbf{v}=(2+1,1+3)=(3,4)§: sčítanie po zložkách je skladanie šípok.""", "Skriptá, Pozorovanie 2.1: v §\\mathbb{R}^2§ zodpovedá sčítanie po zložkách skladaniu šípok, súčet je uhlopriečkou rovnobežníka.")
        explain("Vektor je len §n§-tica čísel.", """Šípka je obrázok pre §n=2§. Pre §n=4§ alebo v §\mathbb{Z}_5§ obrázok nemáme, ale počíta sa rovnako: po zložkách, a v §\mathbb{Z}_p§ modulo §p§.""",
            cards = listOf("tn"))
        explore("""Toto je celé §\mathbb{Z}_5^2§.""", """Ťukni na vektor a pozri jeho násobky §\alpha\mathbf{v}§ pre §\alpha=0,\dots,4§.""", Pic.Grid(5, goal = Goal.Explore),
            """Násobky sa v §\mathbb{Z}_5§ zacyklia: §5\mathbf{v}=\theta§. Celý priestor má §5^2=25§ vektorov.""")
        clip("l1-tn")
        check("""§3\cdot(2,4)§ v §\mathbb{Z}_5^2§ je""", "§(6,12)§", "§(1,2)§", "§(6,2)§", correct = 1, why = """§3\cdot 2=6\equiv 1\pmod{5}§ a §3\cdot 4=12\equiv 2\pmod{5}§.""",
            wrong = mapOf(0 to """To je výsledok v §\mathbb{Z}^2§. Čísla §6§ a §12§ nie sú prvky §\mathbb{Z}_5=\{0,\dots,4\}§: v §\mathbb{Z}_5^2§ sa každá zložka berie modulo §5§.""", 2 to """Druhú zložku si zredukoval, prvú nie. §6§ nie je prvok §\mathbb{Z}_5§, modulo §5§ sa berie v každej zložke."""))
        worked("Počítanie v §\\mathbb{Z}_5^3§", """Spočítaj §2\cdot(1,4,3)+(4,4,2)§ v §\mathbb{Z}_5^3§.""", "Definícia 2.2", ArithZpPostup) {
            val z5 = Field.Zp(5)
            arithSteps(z5, Frac.of(2), z5.vec(listOf(1, 4, 3)), Frac.ONE, z5.vec(listOf(4, 4, 2)))
            val (_, add) = ArithZpPostup
            add("""Záporné čísla: pripočítavaj §5§, kým nie si medzi §0§ a §4§. Napríklad §-1=4§, §-3=2§.""")
        }
        check("""Koľko je §-11§ v §\mathbb{Z}_5§?""", "§1§", "§4§", "§11§", correct = 1, why = """§-11+15=4§. Číslo §1§ by vyšlo pre §+11§.""", hint = "Pripočítavaj 5, kým nie si medzi 0 a 4.",
            wrong = mapOf(0 to """Zahodil si znamienko: §1§ je zvyšok čísla §+11§. Aj pre záporné číslo musí výsledok ležať v §\{0,\dots,4\}§ a líšiť sa od neho o násobok §5§.""", 2 to """§11§ nie je prvok §\mathbb{Z}_5=\{0,\dots,4\}§, a navyše si stratil znamienko. Výsledok treba zredukovať modulo §5§."""))
        explain("Čo v §T^n§ nie je.", """Skalár plus vektor ani vektor krát vektor definované nie sú. Skalár píš vľavo: §\alpha\mathbf{x}§. A „násobenie vektora skalárom“ nie je „skalárny súčin“, to je niečo iné (vyučujúci, Discord #la1, 20. 10. 2026).""",
            formal = cz("""Mezi oblíbené studentské hříchy totiž patří takové činy jako je „sčítání“ skaláru s vektorem nebo „násobení“ vektoru vektorem – nic takového ale (zatím) nemáme definováno, jde tedy o naprosté nesmysly. Podobně se vyvarujte mluvit o součinu „vektor krát skalár“."""),
            src = "Skriptá, Varování 2.5")
        check("""Ktorý zápis má zmysel v §\mathbb{R}^3§?""", """§2\cdot(1,0,3)§""", """§(1,0,3)\cdot(2,1,1)§""", """§2+(1,0,3)§""", correct = 0, why = "Definované je len sčítanie vektorov a násobenie vektora skalárom.",
            wrong = mapOf(1 to """Násobenie vektora vektorom v §T^n§ definované nie je. Definícia 2.2 dáva len §\mathbf{x}+\mathbf{y}§ a §\alpha\mathbf{x}§ so skalárom §\alpha\in T§ (Varovanie 2.5).""", 2 to """Sčítanie skalára s vektorom definované nie je. §+§ v §T^n§ sčítava dva vektory z §T^n§ (Varovanie 2.5)."""))
        practice("arith-zp", solo = false)
        def("tn")
        explain("Pravidlá počítania", """Sčítanie je komutatívne a asociatívne, násobenie distributívne, §1\cdot\mathbf{x}=\mathbf{x}§ a §0\cdot\mathbf{x}=\theta§. Platí to preto, lebo po zložkách platí to isté pre čísla v telese.""",
            formal = listOf(Item(m("""Sčítání vektorů je komutativní: §(\forall\mathbf{x},\mathbf{y}\in T^n)(\mathbf{x}+\mathbf{y}=\mathbf{y}+\mathbf{x})§.""")), Item(m("""Neutrální prvek §1\in T§ je neutrální i vůči násobení vektoru skalárem: §(\forall\mathbf{x}\in T^n)(1\cdot\mathbf{x}=\mathbf{x})§.""")), Item(m("""Existuje nulový vektor v §T^n§ a nulový násobek libovolného vektoru je nulový vektor: §(\exists\theta\in T^n)(\forall\mathbf{x}\in T^n)(0\cdot\mathbf{x}=\theta)§."""))),
            src = "Skriptá, Poznámka 2.4")
        check("""Koľko prvkov má §\mathbb{Z}_3^2§?""", "§6§", "§9§", "§8§", correct = 1, why = """Každá z dvoch zložiek má 3 možnosti: §3\cdot 3=3^2=9§.""",
            wrong = mapOf(0 to """§6=2\cdot 3§ je počet zložiek krát počet možností. Voľby zložiek sa však násobia: každá z §3§ hodnôt prvej zložky ide s každou z §3§ hodnôt druhej.""", 2 to """§8=2^3§: zamenil si základ a exponent. Základ je počet prvkov telesa §\mathbb{Z}_3§, exponent počet zložiek."""))
        check("""Čo je nulový vektor §\theta§ v §\mathbb{Z}_5^4§?""", "§(0,0,0,0)§", "§0§", "§(5,5,5,5)§", correct = 0, why = """§\theta\in T^n§ je §n§-tica núl. Číslo §0§ je skalár, nie vektor, a §5§ v §\mathbb{Z}_5§ nie je.""",
            wrong = mapOf(1 to """§0§ je skalár, prvok telesa §\mathbb{Z}_5§. Nulový vektor §\theta§ je prvok §\mathbb{Z}_5^4§, teda štvorica.""", 2 to """§5§ nie je prvok §\mathbb{Z}_5=\{0,\dots,4\}§. Zložky vektora z §\mathbb{Z}_5^4§ musia byť z §\mathbb{Z}_5§."""))
        practice("arith", solo = true)
    },

    lesson("podpriestor", "Podpriestor", 17, "Tri podmienky podpriestoru, prečo v ňom je nulový vektor a ako ukázať, že niečo podpriestor nie je.", 1) {
        ask("""Je priamka §y=x+1§ podpriestor §\mathbb{R}^2§?""", "Áno", "Nie", correct = 1, after = """Neobsahuje §(0,0)§. Podpriestor s každým vektorom §\mathbf{x}§ obsahuje aj §0\cdot\mathbf{x}=(0,0)§, takže priamka bez počiatku podpriestorom byť nemôže (Pozorovanie 2.8).""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """„Rovná“ nestačí: podpriestor musí byť uzavretý na sčítanie, a táto priamka nie je. §(0,1)§ na nej leží, §(0,1)+(0,1)=(0,2)§ už nie."""))
        explore("""Vezmi dva body priamky §y=2x§.""", "Ťukni na dva body. Uvidíš ich súčet a dvojnásobok.", Pic.Closure(SetKind.LineThroughZero),
            "Súčet aj násobok ostali na priamke. Z nej sa operáciami nedá ujsť.")
        explore("""Teraz priamka §y=x+1§.""", "Zase dva body a ich súčet.", Pic.Closure(SetKind.LineOffZero),
            """Súčet vyskočil z priamky. Množina nie je uzavretá na sčítanie, podpriestor to nie je.""")
        explain("Podpriestor sa nedá opustiť.", """Tri podmienky: neprázdny, uzavretý na sčítanie a na násobenie skalárom. Kvantifikátor §\forall§ znamená všetky dvojice: jeden príklad nič nedokazuje, jeden protipríklad vyvráti. Zápis §P\ssub T^n§ čítaj „§P§ je podpriestor §T^n§“.""",
            cards = listOf("podpriestor"))
        clip("l2-def")
        explain("Nulový vektor je dôsledok, nie podmienka.", """Vezmi hocijaké §\mathbf{x}\in P§: §0\cdot\mathbf{x}=\theta§ musí byť v §P§. Naopak sa to hodí: §\theta\notin P§ hneď znamená, že §P§ podpriestor nie je. Opačne nie: §\theta\in P§ nestačí.""",
            formal = cz("""§\theta\notin P\Rightarrow P\text{ není podprostorem }T^n§."""), src = "Skriptá, Poznámka 2.10", cards = listOf("t2-8"))
        explore("""Množina §|x|=|y|§ nulu obsahuje.""", "Je podpriestor? Skús dva body.", Pic.Closure(SetKind.Abs),
            """§(1,1)+(1,-1)=(2,0)§ vypadne. Nulový vektor nestačí (Príklad 2.12).""")
        check("""Zjednotenie osí §x§ a §y§ v §\mathbb{R}^2§ je podpriestor?""", "Áno", "Nie", correct = 1, why = """Zjednotenie osí nie je uzavreté na sčítanie. §(1,0)§ aj §(0,1)§ v ňom sú, ale ich súčet §(1,1)§ nie (Príklad 2.17).""", pic = Pic.Closure(SetKind.Cross),
            wrong = mapOf(0 to """Zjednotenie podpriestorov podpriestorom byť nemusí, hoci každá os je podpriestor. Zjednotenie síce obsahuje §\theta§, ale uzavretosť na sčítanie tu zlyhá (Veta 2.18)."""))
        worked("Dôkaz, že niečo podpriestor je", """Ukáž, že §P=\{(x,y,z)\in\mathbb{R}^3\mid x+2y=0\}§ je podpriestor §\mathbb{R}^3§.""", "Skriptá, Príklad 2.14", SubspacePostup) {
            val (theta, sum, mult, end) = SubspacePostup
            theta("""Dosadím §\theta=(0,0,0)§ do podmienky: §0+2\cdot 0=0§ platí, teda §\theta\in P§ a §P§ je neprázdna.""")
            sum("""Nech §(x_1,y_1,z_1),(x_2,y_2,z_2)\in P§, teda §x_1+2y_1=0§ a §x_2+2y_2=0§. Pre súčet podmienka platí znova:""",
                Formula("""(x_1+x_2)+2(y_1+y_2)=(x_1+2y_1)+(x_2+2y_2)=0+0=0"""))
            mult("""Nech §\alpha\in\mathbb{R}§; pre §\alpha(x_1,y_1,z_1)§ vyberiem §\alpha§ pred zátvorku.""", Formula("""\alpha x_1+2\alpha y_1=\alpha(x_1+2y_1)=\alpha\cdot 0=0"""))
            end("""Všetky tri podmienky platia pre **všetky** prvky a skaláry, preto §P\ssub\mathbb{R}^3§.""")
        }
        check("""Je prvý kvadrant §\{(x,y)\in\mathbb{R}^2\mid x\geq 0,\ y\geq 0\}§ podpriestor §\mathbb{R}^2§?""", "Áno", "Nie", correct = 1, why = """Nie je uzavretá na násobenie záporným skalárom. §(1,1)§ v nej je, ale §(-1)\cdot(1,1)=(-1,-1)§ nie.""", pic = Pic.Closure(SetKind.Quadrant),
            wrong = mapOf(0 to """Na sčítanie a násobenie nezáporným číslom je uzavretá, záporným nie. Definícia 2.7 však chce §(\forall\alpha\in T)§, teda aj záporné §\alpha§."""))
        clip("l2-prienik")
        thm("t2-18")
        check("""Ktoré sú jediné netriviálne podpriestory §\mathbb{R}^2§?""", "Priamky cez počiatok", "Všetky priamky", "Kruhy so stredom v počiatku", correct = 0, why = """Priamka mimo počiatku neobsahuje §\theta§. Okrem nich sú už len §\{\theta\}§ a §\mathbb{R}^2§ (Príklad 2.13).""",
            wrong = mapOf(1 to """Priamka mimo počiatku neobsahuje §\theta§, a každý podpriestor ho obsahuje (Pozorovanie 2.8).""", 2 to """Kruh nie je uzavretý na násobenie skalárom. Pre §\mathbf{x}\neq\theta§ z neho §t\mathbf{x}§ pri dosť veľkom §t§ vyjde von."""))
        check("""Je §\mathbb{R}^1§ podpriestor §\mathbb{R}^2§?""", "Áno", "Nie", correct = 1, why = """§\mathbb{R}^1§ nie je podmnožina §\mathbb{R}^2§. Jeho prvky sú §1§-tice, nie dvojice (Varovanie 2.16).""",
            wrong = mapOf(0 to """§\mathbb{R}^1§ nie je podmnožina §\mathbb{R}^2§: jeho prvky sú §1§-tice. Priamka v §\mathbb{R}^2§ podpriestor byť môže, ale §\mathbb{R}^1§ to nie je. Definícia 2.7 začína „Nechť §P§ je podmnožina §T^n§“."""))
        def("podpriestor")
        explain("Triviálny a vlastný", """§\{\theta\}§ a §T^n§ má každý priestor. Vlastný znamená menší než celý. §\{\theta\}§ je preto vlastný a pritom triviálny.""",
            cards = listOf("trivialny"))
        check("""Je množina riešení §x+y=1§ v §\mathbb{R}^2§ podpriestor?""", "Áno", "Nie", correct = 1, why = """§(0,0)§ rovnicu nespĺňa. Riešenia nehomogénnej sústavy podpriestor nie sú (Klouda, 4. prednáška).""",
            wrong = mapOf(0 to """Podpriestor tvoria riešenia len homogénnej sústavy (pravá strana §0§). Tu je pravá strana §1§: §(1,0)§ aj §(0,1)§ sú riešenia, ich súčet §(1,1)§ dáva §1+1=2§."""))
        practice("subspace", solo = false)
        prove("p-homog")
        practice("subspace", solo = true)
        prove("p-theta")
    },

    lesson("lk", "Lineárna kombinácia", 11, "Súčet násobkov s koeficientmi z telesa, triviálna a netriviálna kombinácia.", 2) {
        ask("""Existujú čísla §\alpha,\beta\in\mathbb{R}§, pre ktoré §\alpha(1,1)+\beta(1,2)=(3,4)§?""", "Áno", "Nie", correct = 0, after = """Áno: §2(1,1)+1(1,2)=(3,4)§.""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(1 to """Koeficienty nemusia byť §0§ ani §1§, smú byť ľubovoľné reálne čísla. Stačí, že nejaké §\alpha,\beta§ s §\alpha(1,1)+\beta(1,2)=(3,4)§ existujú."""))
        explore("""Trafíš §(3,4)§?""", """Natiahni §\mathbf{u}=(1,1)§ a §\mathbf{v}=(1,2)§ a sčítaj.""", Pic.Plane(1 to 1, 1 to 2, 3 to 4, range = 3),
            """§2\mathbf{u}+1\mathbf{v}=(3,4)§. Čísla §2§ a §1§ sú koeficienty lineárnej kombinácie.""")
        explain("Natiahni, potom sčítaj.", """Lineárna kombinácia je súčet násobkov. Stačí, že nejaké koeficienty z telesa **existujú**. Triviálna má všetky koeficienty nulové a dá vždy §\theta§.""",
            cards = listOf("lk"))
        clip("l3-trivialna")
        check("""Ktorá kombinácia súboru §(\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3)§ je triviálna?""", """§0\mathbf{x}_1+0\mathbf{x}_2+0\mathbf{x}_3§""", """§0\mathbf{x}_1+0\mathbf{x}_2+1\mathbf{x}_3§""", """§1\mathbf{x}_1+1\mathbf{x}_2+1\mathbf{x}_3§""", correct = 0, why = "Triviálna: všetky koeficienty nulové.",
            wrong = mapOf(1 to """Koeficient pri §\mathbf{x}_3§ je §1\neq 0§. Triviálna kombinácia má **všetky** koeficienty nulové; jeden nenulový ju robí netriviálnou.""", 2 to """Triviálna neznamená „najjednoduchšia“. Koeficienty §1§ dajú súčet vektorov, triviálna kombinácia má všetky koeficienty nulové."""))
        check("""§0\mathbf{x}_1+5\mathbf{x}_2+0\mathbf{x}_3§ nad §\mathbb{R}§ je""", "triviálna", "netriviálna", correct = 1, why = "Stačí jeden nenulový koeficient, aby bola netriviálna.",
            wrong = mapOf(0 to """Na netriviálnosť stačí jediný nenulový koeficient, tu §5\neq 0§. Triviálna kombinácia má všetky koeficienty nulové."""))
        worked("Príklad zo skrípt", """Over rovnosť §\left(\tfrac{7}{2},-1\right)=2(1,1)+\left(-\tfrac{3}{2}\right)(-1,2)§.""", "Skriptá, Príklad 2.22", ArithPostup) {
            arithSteps(Field.R, Frac.of(2), Field.R.vec(listOf(1, 1)), Frac.of(-3, 2), Field.R.vec(listOf(-1, 2)))
            val (_, add) = ArithPostup
            add("""Vektor §(\tfrac{7}{2},-1)§ je teda lineárnou kombináciou súboru §((1,1),(-1,2))§ s koeficientmi §2§ a §-\tfrac{3}{2}§.""")
        }
        explore("""Skús §(3,1)§ z §(1,2)§ a §(2,4)§.""", "Posúvaj, ako chceš.", Pic.Plane(1 to 2, 2 to 4, 3 to 1, range = 2, goal = Goal.Explore),
            """Neujdeš z priamky §y=2x§: §(3,1)§ nie je lineárnou kombináciou tohto súboru.""")
        check("""Je §(0,0)§ lineárnou kombináciou súboru §((1,2),(3,1))§?""", "Áno", "Nie", correct = 0, why = """Triviálna kombinácia dá §\theta§ pre každý súbor.""",
            wrong = mapOf(1 to """Nulové koeficienty sú dovolené: §0(1,2)+0(3,1)=(0,0)§. Definícia 2.21 chce len existenciu koeficientov. Netriviálna kombinácia rovná §\theta§ tu síce neexistuje, ale na tú sa otázka nepýta."""))
        worked("Je to kombinácia?", """Je §(1,5)§ lineárnou kombináciou súboru §((1,1),(1,3))§?""", null, MemberPostup) {
            val (system, gem, side, end) = MemberPostup
            system("""Hľadám koeficienty §\alpha,\beta\in\mathbb{R}§ s §\alpha(1,1)+\beta(1,3)=(1,5)§. Po zložkách §\alpha+\beta=1§ a §\alpha+3\beta=5§; stĺpce sú dané vektory.""")
            gem("""Od druhého riadku odčítam prvý a mám horný stupňovitý tvar.""", Formula(Field.R.walk(intMat(listOf(1, 1, 1), listOf(1, 3, 5), bar = 2), listOf(rowSub(2, 1, 1)))))
            side("""Pravá strana nie je hlavný stĺpec, riešenie existuje. Zdola §\beta=2§, potom §\alpha=1-2=-1§.""")
            end("""Áno, §(1,5)§ je kombináciou súboru.""", Formula("""(1,5)=-(1,1)+2(1,3)"""))
        }
        practice("arith", solo = false)
        explain("Čokoľvek zo sčítania a násobenia je kombinácia.", """Výraz ako §2(\mathbf{x}+3\mathbf{y})-\mathbf{x}§ sa upraví na §1\mathbf{x}+6\mathbf{y}§. Preto stačí skúmať tvar §\sum\alpha_i\mathbf{x}_i§: nič tým nestrácaš (Klouda, 4. prednáška).""")
        check("""Uprav §2(\mathbf{x}+3\mathbf{y})-\mathbf{x}§.""", """§\mathbf{x}+6\mathbf{y}§""", """§2\mathbf{x}+6\mathbf{y}§""", """§\mathbf{x}+5\mathbf{y}§""", correct = 0, why = """§2\mathbf{x}+6\mathbf{y}-\mathbf{x}=\mathbf{x}+6\mathbf{y}§.""",
            wrong = mapOf(1 to """Zabudol si odčítať §\mathbf{x}§: podľa distributivity §2\mathbf{x}-\mathbf{x}=(2-1)\mathbf{x}§.""", 2 to """Odčítal si od koeficientu pri §\mathbf{y}§. Distributivita dá §2(\mathbf{x}+3\mathbf{y})=2\mathbf{x}+6\mathbf{y}§ a §-\mathbf{x}§ mení len koeficient pri §\mathbf{x}§."""))
        explain("Súbor alebo vektor?", """Súbor je §m§-tica vektorov, píš ich tučne: §(\mathbf{x}_1,\dots,\mathbf{x}_m)§. Netučné §(x_1,\dots,x_m)§ je jeden vektor so zložkami.""",
            formal = cz("""Napíšeme-li na tabuli bez kontextu pouze §(x_1,x_2,\dots,x_m)§, může to znamenat minimálně tyto dvě různé věci: Soubor vektorů, pokud §\mathbf{x}_1,\dots,\mathbf{x}_m§ jsou postupně očíslované vektory z §T^n§. Jeden jediný vektor §\mathbf{x}=(x_1,\dots,x_m)§ z vektorového prostoru §T^m§."""),
            src = "Skriptá, Poznámka 2.20")
        def("lk")
        practice("arith", solo = true)
    },

    lesson("ln", "LN a LZ", 28, "Kedy je súbor lineárne nezávislý, ako to overiť z definície a cez GEM.", 3) {
        ask("""Existujú čísla §\alpha,\beta\in\mathbb{R}§, nie obe nulové, pre ktoré §\alpha(1,2)+\beta(2,4)=(0,0)§?""", "Áno", "Nie", correct = 0, after = """Áno: §2(1,2)-(2,4)=(0,0)§ a koeficienty §2§, §-1§ nie sú nulové. Súbor, z ktorého sa dá takto netriviálne nakombinovať §\theta§, sa volá lineárne závislý; v tejto lekcii to zavedieme presne.""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(1 to """Existujú, lebo §(2,4)§ je dvojnásobok §(1,2)§. Koeficienty smú byť aj záporné, takže jeden vektor sa dá druhým „vynulovať“."""))
        explore("""Nájdi §\alpha,\beta§, nie obe nulové, s §\alpha\mathbf{u}+\beta\mathbf{v}=\theta§.""", """§\mathbf{u}=(1,2)§, §\mathbf{v}=(2,4)§.""", Pic.Plane(1 to 2, 2 to 4, null, range = 3, goal = Goal.Zero),
            """Netriviálna kombinácia rovná §\theta§: súbor §(\mathbf{u},\mathbf{v})§ je LZ.""")
        explore("""Teraz §\mathbf{v}=(2,1)§. Nájdeš netriviálnu?""", "Skúšaj.", Pic.Plane(1 to 2, 2 to 1, null, range = 3, goal = Goal.Zero),
            """Nenájdeš: §\alpha(1,2)+\beta(2,1)=\theta§ len pre §\alpha=\beta=0§. Súbor je LN.""")
        explain("Len triviálna dá nulu.", """LN: jediná kombinácia rovná §\theta§ je tá s nulami. LZ: existuje aj iná. Pozor na kvantifikátory: „pre všetky §\alpha_i§: ak súčet je §\theta§, potom sú všetky nulové“.""",
            cards = listOf("ln"))
        clip("l4-subor")
        check("""Súbor §((1,0),(0,1),(1,1))§ je""", "LN", "LZ", correct = 1, why = """§1(1,0)+1(0,1)-1(1,1)=\theta§, netriviálna.""",
            wrong = mapOf(0 to """Tretí vektor je súčtom prvých dvoch, takže súbor je LZ. Každé dva z nich sú síce LN, ale LN je vlastnosť celého súboru."""))
        thm("t2-25", proof = "p-nula")
        check("""Súbor §((1,2,3),(0,0,0))§ je""", "LN", "LZ", correct = 1, why = """Obsahuje §\theta§: §0(1,2,3)+1\cdot\theta=\theta§ je netriviálna.""",
            wrong = mapOf(0 to """Každý súbor s §\theta§ je LZ. Nulový vektor nie je „nič“: koeficient pri ňom môže byť nenulový a súčet ostane §\theta§ (Pozorovanie 2.25)."""))
        check("""Súbor §((2,5))§ s jedným vektorom je""", "LN", "LZ", correct = 0, why = """Súbor s jediným nenulovým vektorom je LN. Súbor dĺžky §1§ je LZ, len keď je §(\theta)§ (Pozorovanie 2.25, bod 4).""",
            wrong = mapOf(1 to """LZ by znamenalo §\alpha(2,5)=\theta§ pre nejaké §\alpha\neq 0§. Z §2\alpha=0§ však plynie §\alpha=0§: jediná kombinácia rovná §\theta§ je triviálna."""))
        worked("Overenie LN v §\\mathbb{Z}_5^3§", """Je §((1,2,3),(4,2,3),(3,4,2))§ v §\mathbb{Z}_5^3§ lineárne nezávislý?""", "Skriptá, Príklad 2.28", LnPostup) {
            val (system, gem, side, end) = LnPostup
            system("""Z definície LN hľadám §\alpha,\beta,\gamma\in\mathbb{Z}_5§ s §\alpha(1,2,3)+\beta(4,2,3)+\gamma(3,4,2)=\theta§. Po zložkách je to homogénna sústava, vektory sú stĺpce.""",
                Formula("""(\alpha+4\beta+3\gamma,\ 2\alpha+2\beta+4\gamma,\ 3\alpha+3\beta+2\gamma)=(0,0,0)"""))
            gem("""Úpravami GEM v §\mathbb{Z}_5§ dostanem horný stupňovitý tvar.""",
                Formula(Field.Zp(5).walk(intMat(listOf(1, 4, 3, 0), listOf(2, 2, 4, 0), listOf(3, 3, 2, 0), bar = 3), listOf(rowSub(2, 2, 1), rowSub(3, 3, 1)), listOf(rowSwap(2, 3)), listOf(rowSub(3, 4, 2))), "v §\\mathbb{Z}_5§"))
            side("""Všetky tri stĺpce vľavo sú hlavné, vedľajší je len stĺpec pravej strany. Jediné riešenie je §(0,0,0)§.""")
            end("""Len triviálna kombinácia dá §\theta§, súbor je LN.""")
        }
        explore("Krokuj GEM", """Súbor §((1,0,1,0),(2,2,0,0),(2,0,1,1),(1,1,2,-2))§ v §\mathbb{R}^4§, vektory v stĺpcoch.""", Pic.Gem(listOf(listOf(1, 2, 2, 1), listOf(0, 2, 0, 1), listOf(1, 0, 1, 2), listOf(0, 0, 1, -2)), 0, caption = "Skriptá, Príklad 2.29"),
            """Súbor je LZ: štvrtý stĺpec je vedľajší, takže riešení je viac, aj netriviálne, napr. §(\alpha,\beta,\gamma,\delta)=(-8,-1,4,2)§.""",
            "Pozor: poznámka pod čiarou v skriptách uvádza §\\alpha=12,\\beta=1,\\gamma=4,\\delta=2§, ale tá kombinácia dá §(24,4,20,0)§, nie §\\theta§.")
        clip("l4-pisomka")
        thm("t2-27", "t2-30")
        practice("ln", solo = false)
        clip("l4-bez-pocitania")
        check("""Vidíš §\mathbf{x}_3=\mathbf{x}_1+3\mathbf{x}_2§. Súbor §(\mathbf{x}_1,\mathbf{x}_2,\mathbf{x}_3)§ je""", "LN", "LZ", correct = 1, why = """§\mathbf{x}_1+3\mathbf{x}_2-\mathbf{x}_3=\theta§ je netriviálna kombinácia, lebo koeficient §-1§ nie je nulový. Na LZ to stačí: Definícia 2.23 chce len existenciu jednej netriviálnej kombinácie rovnej §\theta§, takže GEM netreba.""",
            wrong = mapOf(0 to """Táto rovnosť sa dá prepísať na netriviálnu kombináciu rovnú §\theta§, súbor je LZ. Po presunutí na jednu stranu je koeficient pri §\mathbf{x}_3§ rovný §-1\neq 0§."""))
        thm("t2-32")
        check("""Súbor štyroch vektorov z §\mathbb{R}^3§ je""", "vždy LZ", "vždy LN", "záleží na vektoroch", correct = 0, why = """Štyri vektory v §\mathbb{R}^3§ sú vždy LZ, lebo §m=4>n=3§. Hlavných stĺpcov je najviac 3, jeden ostane vedľajší (Pozorovanie 2.32).""",
            wrong = mapOf(1 to """Štyri vektory v §\mathbb{R}^3§ nie sú LN nikdy. Homogénna sústava so §4§ neznámymi a §3§ rovnicami má vždy netriviálne riešenie.""", 2 to """Štyri vektory v §\mathbb{R}^3§ sú LZ vždy, nech sú akékoľvek. Pri troch by to od vektorov záviselo. Matica §3\times 4§ má najviac §3§ hlavné stĺpce a jeden ostane vedľajší."""))
        def("ln")
        practice("ln", solo = true)
        prove("p-dva")
        worked("Sústava so zadanými riešeniami", """Najděte matici §\mathbf{A}\in\mathbb{Z}_3^{2,4}§ a vektor §\mathbf{b}\in\mathbb{Z}_3^2§ takové, že soustava §\mathbf{A}\mathbf{x}=\mathbf{b}§ má v množině řešení vektor §(1,2,2,0)§ i vektor §(2,1,1,1)§. Řádky matice §\mathbf{A}§ chápané jako soubor vektorů ze §\mathbb{Z}_3^4§ musí být navíc lineárně nezávislé.""", "Test listopad 2022, príklad 5 (8 b)", SystemPointsPostup) {
            val z3 = Field.Zp(3)
            systemPointsSteps(z3, z3.vec(listOf(1, 2, 2, 0)), z3.vec(listOf(2, 1, 1, 1)), listOf(listOf(1, 1, 0, 0), listOf(1, 0, 0, 2)).map(z3::vec))
        }
        practice("soustava-body", solo = true)
    },

    lesson("obal", "Lineárny obal", 22, "Všetko, kam sa z daných vektorov dostaneš; prečo je to podpriestor a ako zistiť, či doň vektor patrí.", 4) {
        ask("""Existujú čísla §\alpha,\beta\in\mathbb{R}§, pre ktoré §\alpha\mathbf{u}+\beta\mathbf{v}=(3,1)§, kde §\mathbf{u}=(1,2)§ a §\mathbf{v}=(2,4)§?""", "Áno", "Nie", correct = 1, after = """Nie. §\mathbf{u}§ aj §\mathbf{v}§ ležia na priamke §y=2x§, a tak aj každá ich kombinácia; §(3,1)§ na nej neleží.""", pic = Pic.Still(listOf(1 to 2, 2 to 4), null, 3 to 1, listOf("\\U=(1,2)", "\\V=(2,4)")), sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """Tieto dva vektory celú rovinu nedajú, lebo §(2,4)=2(1,2)§. Každá ich kombinácia je násobok §(1,2)§ a leží na priamke §y=2x§."""))
        explore("Posúvaj, kam sa dostaneš.", """Koeficienty pri §\mathbf{u}§ a §\mathbf{v}§.""", Pic.Plane(1 to 2, 2 to 4, 3 to 1, range = 2, goal = Goal.Explore),
            """Posúvaj, koľko chceš, z priamky §y=2x§ neujdeš. To je lineárny obal §\langle\mathbf{u},\mathbf{v}\rangle§.""")
        clip("l5-vlastne")
        explain("Všetko, kam sa dostaneš, je lineárny obal.", """Obal je množina **všetkých** kombinácií. Je v ňom §\theta§ aj vektory súboru samy. Obal je nástroj, ktorý zo súboru vyrobí množinu.""",
            cards = listOf("obal"))
        explore("""Ťukni na všetky body obalu §\langle(1,2)\rangle§.""", "Pozor, môže ich byť viac.", Pic.TapSpan(1 to 2, null, listOf(2 to 4, 3 to 1, -1 to -2, 0 to 0, -2 to 1, 3 to 6)),
            """Druhá zložka je vždy dvojnásobok prvej. Aj §(0,0)§: stačí koeficient §0§.""")
        // the picture names its vectors u and v: the new second vector is v too, no longer a multiple of u
        explore("""Teraz §\mathbf{v}=(1,1)§, už nie násobok §\mathbf{u}§. Trafíš §(3,1)§?""", """Kombinuj §\mathbf{u}=(1,2)§ a §\mathbf{v}=(1,1)§.""", Pic.Plane(1 to 2, 1 to 1, 3 to 1, range = 5),
            """§-2\mathbf{u}+5\mathbf{v}=(3,1)§. Dva vektory mimo jednej priamky: obal je celá rovina.""")
        clip("l5-pridanie")
        thm("t2-34", "t2-35", proof = "p-obal")
        explore("""Obal §\langle(1,2)\rangle§ v §\mathbb{Z}_5^2§""", "Svetlé body sú obal vektora §\\mathbf{v}=(1,2)§: §\\theta,\\mathbf{v},2\\mathbf{v},\\dots§. Spočítaj ich a ťukni na iný vektor, nech vidíš jeho obal.", Pic.Grid(5, listOf(1 to 2), Goal.Count, 5),
            """Päť bodov: §\alpha(1,2)§ pre §\alpha=0,\dots,4§. Nad konečným telesom je obal konečný.""")
        worked("Obal nad §\\mathbb{Z}_2§", """Vypíš §L=\langle(1,0,1,0),(0,1,1,0)\rangle\ssub\mathbb{Z}_2^4§.""", "Skriptá, Príklad 2.37", SpanListPostup) {
            val z2 = Field.Zp(2)
            spanListSteps(z2, listOf(z2.vec(listOf(1, 0, 1, 0)), z2.vec(listOf(0, 1, 1, 0))))
        }
        check("""Koľko prvkov má §\langle(1,1,0,0),(0,1,1,0)\rangle\ssub\mathbb{Z}_2^4§?""", "§2§", "§4§", "§16§", correct = 1, why = """Štyri voľby §\alpha,\beta\in\{0,1\}§ dajú štyri rôzne vektory. Sú to §\theta,(1,1,0,0),(0,1,1,0),(1,0,1,0)§. Úloha z testu 22. 11. 2023 (variant Myš).""",
            wrong = mapOf(0 to """Obal nie sú len dva dané vektory, ale všetky ich kombinácie. Kombinácie §\alpha\mathbf{x}+\beta\mathbf{y}§ dajú aj §\theta§ a súčet.""", 2 to """§16=2^4§ je počet prvkov celého §\mathbb{Z}_2^4§. Dva LN vektory generujú podpriestor dimenzie §2§, nie celý priestor dimenzie §4§."""))
        thm("t2-38")
        worked("Patrí vektor do obalu?", """Patrí §(1,0,3)§ do §\langle(1,1,1),(0,1,-1)\rangle§ v §\mathbb{R}^3§?""", "Postup podľa Definície 2.33", MemberPostup) {
            val (system, gem, side, end) = MemberPostup
            system("""Hľadám §\alpha,\beta\in\mathbb{R}§ s §\alpha(1,1,1)+\beta(0,1,-1)=(1,0,3)§. Stĺpce sú dané vektory, pravá strana §(1,0,3)§.""")
            gem("""Úpravami GEM dostanem horný stupňovitý tvar rozšírenej matice.""",
                Formula(Field.R.walk(intMat(listOf(1, 0, 1), listOf(1, 1, 0), listOf(1, -1, 3), bar = 2), listOf(rowSub(2, 1, 1), rowSub(3, 1, 1)), listOf(rowAdd(3, 1, 2)))))
            side("""Posledný riadok hovorí §0=1§: pravá strana je hlavný stĺpec, sústava nemá riešenie.""")
            side("""Pozor: samotný nulový riadok po GEM nič neznamená. Rozhoduje, či je pravá strana hlavný stĺpec (vyučujúci LA1, Discord #la1, 23. 10. 2026).""")
            end("""Žiadna kombinácia nedá §(1,0,3)§, vektor do obalu nepatrí.""")
        }
        practice("member", solo = false)
        thm("t2-39", proof = "p-lzlk")
        clip("l5-predlzenie")
        thm("t2-42")
        def("obal")
        check("""Aký je rozdiel medzi lineárnym obalom a podpriestorom?""", "Obal je nástroj, ako zo súboru vyrobiť množinu; podpriestor je vlastnosť množiny", "Sú to dve mená pre to isté", "Obal je vždy celé §T^n§", correct = 0,
            why = """Obal je operácia na súbore vektorov, podpriestor je vlastnosť množiny. Obal tú vlastnosť má vždy (Veta 2.38). Tak to rozlíšil vyučujúci LA1 na Discorde #la1 (25. 10. 2026).""",
            wrong = mapOf(1 to """Pojmy sú rôzne, hoci každý obal je podpriestor. §\langle\cdot\rangle§ je operácia na súbore vektorov, „byť podpriestorom“ je vlastnosť množiny. Že ju obal má vždy, hovorí Veta 2.38.""", 2 to """Obal je celé §T^n§ len vtedy, keď súbor §T^n§ generuje. Napríklad §\langle(1,2)\rangle§ je len priamka v §\mathbb{R}^2§."""))
        practice("param-span", solo = true)
        practice("obal-vypis", solo = true)
    },

    lesson("baza", "Báza", 29, "Súbor, ktorý je LN a zároveň generuje podpriestor; štandardná báza.", 5) {
        ask("""Je súbor §((1,0),(0,1),(1,1))§ báza §\mathbb{R}^2§?""", "Áno", "Nie", correct = 1, after = "Generuje, ale je LZ: tretí je súčet prvých dvoch.", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """Generovať celé §\mathbb{R}^2§ nestačí, báza musí byť aj LN. Tri vektory v §\mathbb{R}^2§ LN nie sú nikdy (Definícia 2.44, Pozorovanie 2.32)."""))
        explore("""Vyber bázu §\mathbb{Z}_5^2§.""", "Ťukaj na vektory: obal sa ukáže hneď. Celých 25 bodov, a ani jeden vektor navyše.", Pic.Grid(5, goal = Goal.Basis, target = 25),
            """Dva LN vektory dajú všetkých 25 bodov. To je báza: generuje a je LN.""")
        explain("Generovať znamená dať celé §P§.", """Obal súboru je **presne** §P§: každý vektor z §P§ sa dá nakombinovať a nič z obalu nie je mimo §P§.""",
            cards = listOf("generuje"))
        explain("Báza: dosť vektorov, ale ani jeden navyše.", """Generovanie dá každý vektor. LN zaručí, že nič nie je navyše, a preto sú súradnice jednoznačné. Jedna podmienka nestačí.""",
            cards = listOf("baza"))
        clip("l6-baza")
        check("""§((1,2),(2,4))§ je báza §\langle(1,2)\rangle§?""", "Áno", "Nie", correct = 1, why = "Generuje tú priamku, ale je LZ.",
            wrong = mapOf(0 to """Generovanie sedí, ale súbor je LZ: §2(1,2)-(2,4)=\theta§. Báza musí byť aj LN. Bázou §\langle(1,2)\rangle§ je napríklad §((1,2))§."""))
        check("""§((1,1),(1,-1))§ je báza §\mathbb{R}^2§?""", "Áno", "Nie", correct = 0, why = """Je LN aj generuje, teda je to báza. Jeden nie je násobkom druhého a §(a,b)=\tfrac{a+b}{2}(1,1)+\tfrac{a-b}{2}(1,-1)§.""",
            wrong = mapOf(1 to """Je to báza: súbor je LN, lebo ani jeden vektor nie je násobkom druhého. Dva LN vektory v priestore dimenzie §2§ sú báza (Veta 2.55)."""))
        clip("l6-generuje")
        worked("Generuje §\\mathbb{Z}_5^3§?", """Generuje §((1,2,3),(4,2,3),(3,4,2))§ priestor §\mathbb{Z}_5^3§?""", "Skriptá, Príklad 2.46", GeneratesPostup) {
            val (system, gem, every, end) = GeneratesPostup
            system("""Pre každé §\mathbf{v}=(v_1,v_2,v_3)\in\mathbb{Z}_5^3§ hľadám §\alpha,\beta,\gamma\in\mathbb{Z}_5§ s §\alpha(1,2,3)+\beta(4,2,3)+\gamma(3,4,2)=\mathbf{v}§. Na pravej strane sú premenné.""")
            gem("""Úpravami GEM v §\mathbb{Z}_5§ dostanem horný stupňovitý tvar; pravú stranu upravujem spolu s riadkami.""",
                Formula(GemWalk.symbolic(listOf(listOf("1", "4", "3", "v_1"), listOf("2", "2", "4", "v_2"), listOf("3", "3", "2", "v_3")), 3,
                    listOf(rowSub(2, 2, 1), rowSub(3, 3, 1)) to listOf(listOf("1", "4", "3", "v_1"), listOf("0", "4", "3", "3v_1+v_2"), listOf("0", "1", "3", "2v_1+v_3")),
                    listOf(rowSwap(2, 3)) to listOf(listOf("1", "4", "3", "v_1"), listOf("0", "1", "3", "2v_1+v_3"), listOf("0", "4", "3", "3v_1+v_2")),
                    listOf(rowSub(3, 4, 2)) to listOf(listOf("1", "4", "3", "v_1"), listOf("0", "1", "3", "2v_1+v_3"), listOf("0", "0", "1", "v_2+v_3"))), "v §\\mathbb{Z}_5§"))
            every("""Hlavný stĺpec je v každom riadku vľavo od čiary, takže pravá strana hlavná byť nemôže. Riešenie existuje pre každé §\mathbf{v}§.""")
            end("""Súbor generuje §\mathbb{Z}_5^3§. Riešenie je navyše jediné, aj pre §\mathbf{v}=\theta§, teda je aj LN a je to báza.""")
        }
        explain("Štandardná báza", """§\mathbf{e}_i§ má jednotku na §i§-tom mieste. Súradnice vektora v nej sú jeho zložky.""", cards = listOf("standardna"))
        check("""Ktorý súbor je štandardná báza §\mathbb{R}^3§?""", """§((1,0,0),(0,1,0),(0,0,1))§""", """§((1,1,1),(0,1,1),(0,0,1))§""", """§((1,0,0),(0,1,0))§""", correct = 0, why = """V štandardnej báze má §\mathbf{e}_i§ jednotku len na §i§-tom mieste. Druhý súbor je tiež báza, ale nie štandardná. Tretí negeneruje §\mathbb{R}^3§.""",
            wrong = mapOf(1 to """Je to báza §\mathbb{R}^3§, ale nie štandardná. V štandardnej báze má §\mathbf{e}_i§ jednotku len na §i§-tom mieste a inde nuly (Definícia 2.48).""", 2 to """Dva vektory §\mathbb{R}^3§ negenerujú. Tretia zložka každej ich kombinácie je §0§, takže napr. §(0,0,1)§ z nich nevyrobíš."""))
        practice("param-basis", solo = false)
        explain("Báza nie je jediná.", """Netriviálny podpriestor nad §\mathbb{R}§ má báz nekonečne mnoho a ani nad §\mathbb{Z}_p§ nebýva báza jediná. Všetky majú rovnako prvkov (Dôsledok 2.58). A báza je súbor: iné poradie je iná báza.""")
        check("""Koľko báz má §\mathbb{R}^2§?""", "Jednu", "Nekonečne mnoho", "Dve", correct = 1, why = """Nekonečne mnoho, napr. §((1,0),(0,t))§ pre každé §t\neq 0§.""",
            wrong = mapOf(0 to """Jedna je len štandardná báza. Napríklad §((1,0),(0,2))§ je tiež LN a generuje §\mathbb{R}^2§, teda je to iná báza.""", 2 to """§2§ je počet vektorov v každej báze, teda dimenzia, nie počet báz. Báz samotných je nekonečne veľa."""))
        def("baza")
        check("""Má §\{\theta\}§ bázu?""", "Áno", "Nie", correct = 1, why = """V §\{\theta\}§ nie je žiadny LN súbor, takže bázu nemá. Jediný jednoprvkový §(\theta)§ je LZ (Veta 2.53).""",
            wrong = mapOf(0 to """Každý súbor z §\{\theta\}§ obsahuje len §\theta§, a preto je LZ. Báza musí byť LN, takže §\{\theta\}§ žiadnu nemá (Pozorovanie 2.25)."""))
        practice("param-gen", solo = true)
        worked("Parameter nad §\\mathbb{Z}_5§", """Zjistěte, pro které hodnoty §\alpha\in\mathbb{Z}_5§ je soubor vektorů §((2,1,1),(2,2,\alpha),(3,3,2))§ bází prostoru §\mathbb{Z}_5^3§.""", "Test 15. 11. 2022 (variant A), príklad 1", ParamZpPostup) {
            val (matrix, read, each, end) = ParamZpPostup
            matrix(paramZpSystem(ParamKind.BASIS, 3, 5),
                Formula(paramZpWalk(5, listOf(vec(2, 1, 1), vec(3, 3, 2)), vec(2, 2, 0), at = 2, shift = 0), "v §\\mathbb{Z}_5§ je §-3=2§, §-2=3§ a §\\alpha+3-6=\\alpha+2§"))
            read("""Prvé dva stĺpce sú hlavné; tretí je hlavný práve vtedy, keď §\alpha+2\neq 0§ v §\mathbb{Z}_5§. """ + paramZpRead(ParamKind.BASIS))
            each("""Hodnôt §\alpha§ je len päť, dosadím každú.""",
                Formula("""\alpha\in\{0,1,2,4\}:\ \alpha+2\in\{2,3,4,1\}""", "nenulové, tri LN vektory sú báza (Veta 2.55)"), Formula("""\alpha=3:\ 3+2=5=0""", "súbor je LZ"))
            end("""Súbor je bázou práve pre §\alpha\in\{0,1,2,4\}§.""")
        }
        practice("param-zp", solo = true)
        worked("Súbor so zadanými bázami", """Najděte soubor čtyř vektorů §(\mathbf{z}_1,\mathbf{z}_2,\mathbf{z}_3,\mathbf{z}_4)§ z prostoru §\mathbb{Z}_3^3§ takový, že §(\mathbf{z}_1,\mathbf{z}_2,\mathbf{z}_4)§ a §(\mathbf{z}_1,\mathbf{z}_2,\mathbf{z}_3)§ jsou báze, §(\mathbf{z}_1,\mathbf{z}_3,\mathbf{z}_4)§ ani §(\mathbf{z}_2,\mathbf{z}_3,\mathbf{z}_4)§ nejsou. Kolika různými způsoby lze vytvořit vektor §(1,1,1)§ jako LK souboru §(\mathbf{z}_1,\mathbf{z}_2,\mathbf{z}_3,\mathbf{z}_4)§?""", "Test 15. 11. 2022 (variant 9A), príklad 4; teleso je v prepise nečitateľné, tu §\\mathbb{Z}_3§", SetBasesPostup) {
            val z3 = Field.Zp(3)
            val e = (0 until 3).map { unit(3, it) }
            setBasesSteps(z3, e + listOf(e[2]), 3, e + listOf(e[2]), z3.vec(listOf(1, 1, 1)))
        }
        practice("soubor-baze", solo = true)
        practice("lk-pocet", solo = true)
    },

    lesson("dimenzia", "Dimenzia", 14, "Definícia dimenzie cez LN súbory, nie iba počet prvkov bázy.", 6) {
        ask("""Koľko najviac vektorov môže mať LN súbor vektorov z §\mathbb{R}^2§?""", "§1§", "§2§", "§3§", "ľubovoľne veľa", correct = 1, after = "Dva. Tri vektory v §\\mathbb{R}^2§ sú vždy LZ (Pozorovanie 2.32).", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """Zmestia sa aj dva, napríklad §(1,0)§ a §(0,1)§. Sú LN, lebo ani jeden nie je násobkom druhého.""", 2 to """Homogénna sústava s tromi neznámymi a dvoma rovnicami má netriviálne riešenie.""", 3 to """LN súbor v §\mathbb{R}^2§ má najviac dva vektory, hoci vektorov je nekonečne veľa. Dva LN už generujú celé §\mathbb{R}^2§, a tak je každý ďalší ich kombináciou a súbor sa stane LZ."""))
        explore("""Pridávaj vektory do súboru v §\mathbb{Z}_5^2§.""", "Kedy sa LN pokazí? Ťukni na tri vektory.", Pic.Grid(5, goal = Goal.Free),
            """Tretí vektor v §\mathbb{Z}_5^2§ súbor vždy pokazí: LN súbor má najviac 2 vektory. Dimenzia je 2.""")
        explain("Dimenzia: najdlhší LN súbor.", """Existuje LN súbor dĺžky §d§ a každý dlhší je LZ; dimenzia §0§ je, keď LN súbor neexistuje vôbec, teda §P=\{\theta\}§. Definícia potrebuje len LN, preto ju kurz volí (skriptá pod Pozorovaním 2.52).""",
            cards = listOf("dimenzia"))
        clip("l7-def")
        explain("Prečo nie „počet prvkov bázy“", """Je to pravda (Veta 2.57), ale je to veta, nie definícia: potrebuje vedieť, že báza existuje a že všetky majú rovnako prvkov. Na otázku „Napište definici dimenze podprostoru.“ ju neuznajú.""", src = "MARAST, ukážková písomka (lekce 292)")
        check("""Stačí na teste ako definícia dimenzie veta „Dimenze je rovna počtu prvků báze.“?""", "Áno", "Nie", correct = 1, why = "Pravdivé tvrdenie, nie definícia. Ukážková písomka v MARASTe ju výslovne neuznáva.",
            wrong = mapOf(0 to """Tvrdenie je pravdivé, ale definícia to nie je. Je to Veta 2.57: používa bázu a predpokladá, že existuje a že všetky bázy majú rovnako prvkov. Definícia 2.49 hovorí o LN súboroch."""))
        thm("t2-52")
        check("""§\dim\langle(1,2),(2,4)\rangle=§""", "§0§", "§1§", "§2§", correct = 1, why = """§((1,2))§ je LN, a každé dva vektory z tej priamky sú LZ.""",
            wrong = mapOf(0 to """Dimenziu §0§ má len §\{\theta\}§ (Veta 2.53). Tu je §((1,2))§ LN súbor dĺžky §1§, teda dimenzia je aspoň §1§.""", 2 to """Vektory sú dva, ale LZ: §(2,4)=2(1,2)§. Dimenzia nie je počet daných vektorov, ale dĺžka najdlhšieho LN súboru v obale."""))
        check("""§\dim\{\theta\}=§""", "§0§", "§1§", correct = 0, why = """Neexistuje LN súbor dĺžky 1: §(\theta)§ je LZ.""",
            wrong = mapOf(1 to """Dimenzia §1§ by chcela LN súbor dĺžky §1§, ale jediný kandidát §(\theta)§ je LZ. Jeden prvok množiny neznamená dimenziu §1§."""))
        thm("t2-53")
        clip("l7-jedna")
        thm("t2-55", proof = "p-dim3")
        worked("Stačí jedna vlastnosť", """Je §((1,1,0),(0,1,1),(1,0,1))§ báza §\mathbb{R}^3§?""", "Veta 2.55", BasisPostup) {
            val (count, ln, end) = BasisPostup
            count("""§\dim\mathbb{R}^3=3§ a súbor má §3§ vektory, preto podľa Vety 2.55 stačí overiť LN.""")
            ln("""Vektory dám do stĺpcov homogénnej sústavy a upravím GEM.""",
                Formula(Field.R.walk(intMat(listOf(1, 0, 1), listOf(1, 1, 0), listOf(0, 1, 1)), listOf(rowSub(2, 1, 1)), listOf(rowSub(3, 1, 2)))))
            ln("""Všetky tri stĺpce sú hlavné, sústava má len triviálne riešenie a súbor je LN.""")
            end("""Tri LN vektory v priestore dimenzie §3§ tvoria bázu §\mathbb{R}^3§.""")
        }
        thm("t2-56", "t2-57", "t2-58")
        practice("dim", solo = false)
        def("dimenzia")
        check("""V §P§ existuje LN súbor dĺžky 3. Potom""", """§\dim P\geq 3§""", """§\dim P=3§""", """§\dim P\leq 3§""", correct = 0, why = """Dimenzia je aspoň §3§, môže byť aj väčšia. Dlhší LN súbor môže existovať tiež (Veta 2.53, bod 3).""",
            wrong = mapOf(1 to """Rovnosť by platila, keby bol navyše každý súbor dĺžky §4§ LZ. Napríklad v §\mathbb{R}^4§ existuje LN súbor dĺžky §3§, a §\dim\mathbb{R}^4=4§.""", 2 to """Nerovnosť ide opačne: dimenzia je **aspoň** §3§, nie najviac. LN súbor dĺžky §3§ dáva len dolný odhad (Veta 2.53, bod 3)."""))
        practice("dim", solo = true)
    },

    lesson("hladanie", "Doplnenie na bázu", 24, "Výber bázy z generujúceho súboru a doplnenie LN súboru na bázu, ako na teste.", 7) {
        ask("""Súbor §((1,2,0,4),(3,3,0,1))§ je LN. Koľko vektorov treba doplniť na bázu §\mathbb{Z}_5^4§?""", "§1§", "§2§", "§3§", correct = 1, after = """§\dim\mathbb{Z}_5^4=4§ a dva už máš.""", sub = "Úloha z testu 21. 11. 2025. Zatiaľ len odhad.",
            wrong = mapOf(0 to """Báza §\mathbb{Z}_5^4§ má §4§ vektory, lebo §\dim\mathbb{Z}_5^4=4§. S jedným doplneným by mal súbor len tri a §\mathbb{Z}_5^4§ by negeneroval (Pozorovanie 2.52).""", 2 to """Päť vektorov v priestore dimenzie §4§ je vždy LZ. Toľko by ich mal súbor s tromi doplnenými (Pozorovanie 2.32)."""))
        explore("""Doplň §(1,2)§ na bázu §\mathbb{Z}_5^2§.""", "Ťukni na vektor, ktorý ju doplní.", Pic.Grid(5, listOf(1 to 2), Goal.Basis, 25),
            """Hocijaký vektor mimo §\langle(1,2)\rangle§ (Veta 2.42). Nikdy nie nulový vektor.""")
        clip("l8-istota")
        thm("t2-59", "t2-60")
        worked("Test 21. 11. 2025, príklad 2", m("""Doplňte soubor vektorů §((1,2,0,4),(3,3,0,1))§ na bázi §\mathbb{Z}_5^4§. Ve vámi nalezené bázi najděte souřadnice vektoru §\mathbf{x}=(1,0,1,0)§. Nalezněte nějaký nenulový vektor §\mathbf{y}§ tak, aby soubor §((1,2,0,4),(3,3,0,1),\mathbf{y})§ byl LZ. Kolik takových vektorů existuje?"""), "Test 21. 11. 2025 (variant Sova), príklad 2", CompletePostup) {
            val (basis, coords, lz, count) = CompletePostup
            val z5 = Field.Zp(5)
            val start = listOf(z5.vec(listOf(1, 2, 0, 4)), z5.vec(listOf(3, 3, 0, 1)))
            val ech = z5.echelon(completionMatrix(start))
            val extra = z5.completeWithUnitVectors(start)
            val b = start + extra.map { unit(4, it) }
            val ex = extra.joinToString(",") { "\\mathbf{e}_${it + 1}" }
            basis("Označím dané vektory §\\mathbf{u}=(1,2,0,4)§, §\\mathbf{v}=(3,3,0,1)§. " + Reasons.completeColumns, gemLines(ech))
            basis("""Hlavné stĺpce sú §\mathbf{u},\mathbf{v},$ex§. Vedľajšie stĺpce sú kombináciou predošlých, tie vynechám. """ + Reasons.pivotColumnsLN + " " + Reasons.lnOfDim + """ Báza je §\mathcal{B}=${vstex(b)}§.""")
            val x = z5.vec(listOf(1, 0, 1, 0))
            val ex2 = z5.echelon(Mat.columns(b, listOf(x)))
            coords("""Súradnice sú koeficienty: hľadám §\alpha_1,\dots,\alpha_4\in\mathbb{Z}_5§ s §\alpha_1\mathbf{b}_1+\dots+\alpha_4\mathbf{b}_4=\mathbf{x}§, kde §\mathbf{b}_1,\dots,\mathbf{b}_4§ sú vektory bázy §\mathcal{B}§. Vektory bázy do stĺpcov, §\mathbf{x}§ na pravú stranu, GEM do HST.""", gemLines(ex2))
            coords("""Riešenie je jediné, lebo báza je LN. Dosadzujem zdola:""", backSubst(z5, ex2, (1..4).map { "\\alpha_$it" }).first + Formula("(\\mathbf{x})_{\\mathcal{B}}=${vtex(z5.coords(b, x)!!)}"))
            lz("""Stačí §\mathbf{y}§ z obalu daných vektorov, napríklad ich súčet.""", Formula("""\mathbf{y}=(1,2,0,4)+(3,3,0,1)=(4,0,0,0)"""))
            count("""Súbor je LZ práve vtedy, keď §\mathbf{y}\in\langle\mathbf{u},\mathbf{v}\rangle§. Vektory §\mathbf{u},\mathbf{v}§ sú totiž LN: vektor v obale je ich kombináciou a vektor mimo obalu súbor LN nechá (Veta 2.39 a Veta 2.42). Obal má §5^2=25§ prvkov, nenulových je §24§.""")
        }
        clip("l8-tip")
        explore("Pozri, ako GEM vyberie bázu", """§M=\langle(1,1,1,0),(1,-1,0,1),(2,0,1,1),(4,6,5,-1)\rangle§, vektory v stĺpcoch. Krokuj a sleduj, ktoré stĺpce budú hlavné.""", Pic.Gem(listOf(listOf(1, 1, 2, 4), listOf(1, -1, 0, 6), listOf(1, 0, 1, 5), listOf(0, 1, 1, -1)), 0, caption = "MARAST, lekce 292, príklad 2"),
            """Hlavné stĺpce sú 1. a 2.: báza §M§ je §((1,1,1,0),(1,-1,0,1))§ a §\dim M=2§ (Veta 2.59).""")
        check("Dopĺňaš súbor §(\\mathbf{u},\\mathbf{v})§ na bázu §\\mathbb{Z}_5^4§ a tipneš si §\\mathbf{e}_3,\\mathbf{e}_4§. Smieš?", "Áno, ak GEM ukáže, že je to báza", "Nie, tipovať sa nesmie", "Áno, aj bez overenia", correct = 0, why = "Tip je legitímny, ak ho overíš (Klouda, 6. prednáška).",
            wrong = mapOf(1 to """Tipovať smieš, len tip musíš overiť. Veta 2.60 hovorí len, že doplnenie existuje, nie ako ho nájdeš. Body sú za to, že ukážeš, že doplnený súbor je báza.""", 2 to """Neoverený tip nie je dôkaz: nie každé dva jednotkové vektory súbor doplnia. Ak niektorý leží v obale daných vektorov, vyjde súbor LZ."""))
        practice("complete", solo = false)
        check("Smieš doplniť nulový vektor?", "Áno", "Nie", correct = 1, why = """Súbor s §\theta§ je LZ (Pozorovanie 2.25), bázou byť nemôže.""",
            wrong = mapOf(0 to """Súbor s §\theta§ je vždy LZ, a báza musí byť LN. Nulový vektor nepridá žiadny smer: §1\cdot\theta=\theta§ je netriviálna kombinácia."""))
        practice("span-basis", solo = false)
        explain("Iná báza toho istého §M§", """Testy chcú „ještě nějakou jinou bázi“. Napr. vynásob jeden vektor bázy číslom, ktoré je v telese rôzne od §0§ aj §1§: §(2\mathbf{b}_1,\mathbf{b}_2)§ nad §\mathbb{R}§ či §\mathbb{Z}_5§. V §\mathbb{Z}_2§ také číslo nie je (§2=0§), tam pripočítaj k jednému vektoru iný: §(\mathbf{b}_1+\mathbf{b}_2,\mathbf{b}_2)§. Vysvetli prečo: je to súbor z §M§ dĺžky §\dim M§ a je LN, teda báza (Veta 2.55).""", src = "Testy 22. 11. 2023 (varianty Myš, Sova) a 14. 11. 2024 (variant Medvěd), príklad 2")
        explain("Stĺpce, alebo riadky?", """**Vybrať bázu** z daných vektorov (Veta 2.59): vektory do **stĺpcov**. Neznáme sú koeficienty, hlavné stĺpce povedia, ktoré pôvodné vektory sú LN a ostatné sú ich kombináciou. **Doplniť na bázu** (Veta 2.60): tiež do **stĺpcov**. Na istotu: dané vektory a za ne celú štandardnú bázu, GEM, hlavné stĺpce sú báza. Alebo si tipni, ktoré §\mathbf{e}_j§ doplníš, a GEM-om over, že sú všetky stĺpce hlavné. Do **riadkov** ide hodnosť (dimenzia obalu).""", src = "Skriptá, Věty 2.59 a 2.60; Klouda, 6. prednáška (42:30)")
        practice("complete", solo = true)
        practice("span-basis", solo = true)
        explain("Báza, ktorá obsahuje daný vektor", """Ak §\mathbf{z}=c_1\mathbf{b}_1+\dots+c_d\mathbf{b}_d§ a §c_k\neq 0§, vymeň §\mathbf{b}_k§ za §\mathbf{z}§. Z rovnosti sa dá §\mathbf{b}_k§ vyjadriť (delíš §c_k§), takže nový súbor generuje celé §M§ a má §\dim M§ prvkov: je to báza (Veta 2.55). Vektor s nulovým koeficientom vymeniť nesmieš, súbor by bol LZ.""", src = "Testy 22. 11. 2023 (varianty Myš, Sova, Včelka) a 14. 11. 2024 (variant Medvěd), príklad 2")
        practice("baza-ina", solo = false)
        practice("baza-ina", solo = true)
    },

    lesson("suradnice", "Súradnice", 28, "Súradnice vzhľadom na bázu, prečo sú jednoznačné, výpočet oboma smermi a počet prvkov podpriestoru v §\\mathbb{Z}_p^n§.", 8) {
        ask("""§\mathcal{B}=((1,0),(1,1))§ je báza §\mathbb{R}^2§. Aké sú súradnice vektora §(3,2)§ vzhľadom na bázu §\mathcal{B}§?""", "§(3,2)§", "§(1,2)§", "§(2,1)§", correct = 1, after = """§1(1,0)+2(1,1)=(3,2)§.""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """To sú zložky vektora, teda súradnice v štandardnej báze. V báze §\mathcal{B}§ sú súradnice koeficienty §\alpha,\beta§ s §\alpha(1,0)+\beta(1,1)=(3,2)§.""", 2 to """Poradie je obrátené: §2(1,0)+1(1,1)=(3,1)\neq(3,2)§. Súradnice idú v poradí vektorov bázy."""))
        explore("""Dostaň sa do §\mathbf{x}=(3,2)§ po mriežke bázy.""", """Posúvaj počet krokov po §\mathbf{u}=(1,0)§ a po §\mathbf{v}=(1,1)§.""", Pic.Coords(1 to 0, 1 to 1, 3 to 2),
            """§(\mathbf{x})_{\mathcal{B}}=(1,2)§: koľko z ktorého vektora bázy. Mriežka sa mení s bázou, vektor ostáva ten istý.""")
        explain("Súradnice: koľko z ktorého vektora bázy.", """Súradníc je toľko, koľko prvkov má báza, a záleží na poradí. Vektor mimo §P§ súradnice vzhľadom na bázu podpriestoru §P§ nemá.""",
            cards = listOf("suradnice"))
        clip("l9-standardna")
        clip("l9-jednoznacnost")
        explore("""Iná báza: §\mathbf{u}=(1,2)§, §\mathbf{v}=(-1,1)§.""", """Nájdi súradnice §\mathbf{x}=(1,5)§.""", Pic.Coords(1 to 2, -1 to 1, 1 to 5),
            """§2\mathbf{u}+1\mathbf{v}=(2-1,4+1)=(1,5)§, teda §(\mathbf{x})_{\mathcal{B}}=(2,1)§.""")
        clip("l9-dokaz")
        thm("t2-66")
        worked("Súradnice z vektora", """Báza §\mathcal{B}=((1,1,1),(1,1,2),(1,2,3))§ priestoru §\mathbb{R}^3§. Nájdi §(\mathbf{w})_{\mathcal{B}}§ pre §\mathbf{w}=(4,2,3)§.""", "Skriptá, Príklad 2.71", VecToCoordsPostup) {
            val (system, gem, back, check) = VecToCoordsPostup
            system("""Súradnice sú koeficienty: hľadám §\alpha,\beta,\gamma§ s §\alpha(1,1,1)+\beta(1,1,2)+\gamma(1,2,3)=(4,2,3)§. Stĺpce sú vektory bázy, pravá strana §\mathbf{w}§.""")
            gem("""Úpravami GEM dostanem horný stupňovitý tvar.""",
                Formula(Field.R.walk(intMat(listOf(1, 1, 1, 4), listOf(1, 1, 2, 2), listOf(1, 2, 3, 3), bar = 3), listOf(rowSub(2, 1, 1), rowSub(3, 1, 1)), listOf(rowSwap(2, 3)))))
            back("""Spätne dosadím zdola: §\gamma=-2§, §\beta=-1-2\gamma=3§, §\alpha=4-\beta-\gamma=3§.""", Formula("""(\mathbf{w})_{\mathcal{B}}=(3,3,-2)"""))
            check("""Skúška: kombinácia bázy s týmito koeficientmi dá §\mathbf{w}§.""", Formula("""3(1,1,1)+3(1,1,2)-2(1,2,3)=(4,2,3)"""))
        }
        check("""Ktorý vektor má súradnice §(1,-1,2)§ v báze §((1,0,0),(1,1,0),(0,0,1))§?""", "§(1,-1,2)§", "§(0,-1,2)§", "§(2,-1,2)§", correct = 1, why = """§1(1,0,0)-1(1,1,0)+2(0,0,1)=(0,-1,2)§. Zo súradníc sa vektor dostane bez sústavy: je to kombinácia vektorov bázy s týmito koeficientmi (otázka zo skrípt pri Definícii 2.68).""",
            wrong = mapOf(0 to """Zamenil si súradnice a vektor. Vektor je §1\mathbf{b}_1-1\mathbf{b}_2+2\mathbf{b}_3§ a treba ho vypočítať. So svojimi súradnicami sa každý vektor zhoduje len v štandardnej báze, v tejto nie.""", 2 to """Znamienko: koeficient pri §(1,1,0)§ je §-1§, teda tento vektor sa odčíta. Prvá zložka je §1-1§, nie §1+1§."""))
        check("""§\mathcal{B}§ je báza §\mathbb{Z}_5^4§. Ktorá rovnosť je zmysluplná?""", """§(\mathbf{x})_{\mathcal{B}}=(1,2)§""", """§(\mathbf{x})_{\mathcal{B}}=(1,2,1)§""", """§(\mathbf{x})_{\mathcal{B}}=(1,3,0,0)§""", correct = 2, why = "Zmysluplná je len štvorica, lebo báza §\\mathbb{Z}_5^4§ má 4 prvky. Súradnice sú koeficienty lineárnej kombinácie vektorov bázy, jeden pri každom z nich (Definícia 2.68); dvojica a trojica by patrili k báze s 2 alebo 3 prvkami. Úloha z testu 20. 11. 2024 (variant Slon).",
            wrong = mapOf(0 to """Dvojica by sedela pre bázu s dvoma prvkami. Báza §\mathbb{Z}_5^4§ má §4§ prvky (§\dim\mathbb{Z}_5^4=4§) a súradníc je toľko, koľko prvkov má báza.""", 1 to """Trojica by sedela pre bázu s tromi prvkami, napríklad podpriestoru dimenzie §3§. Báza celého §\mathbb{Z}_5^4§ má §4§ prvky."""))
        explain("Pozor na smer", """Vektor → súradnice: rieš sústavu so stĺpcami z bázy. Súradnice → vektor: dosaď do §\sum\alpha_i\mathbf{b}_i§. Zámena dáva čísla, ktoré „vychádzajú“, a podľa zadania stojí 2 až 4 body (vyučujúci LA1, Discord #la1, 10. 12. 2024).""")
        practice("coords", solo = false)
        explain("Koľko prvkov má podpriestor v §\\mathbb{Z}_p^n§", """Vektory z §P§ a §d§-tice súradníc si zodpovedajú jeden k jednému (Veta 2.66). §d§-tíc z §\mathbb{Z}_p§ je §p^d§. Toto číslo počíta **vektory** jedného §P§; koľko je rôznych podpriestorov, je iná otázka a má vlastnú lekciu.""",
            formal = cz("""Důsledkem existence takové bijekce je, že podprostor §P\ssub T^n§ dimenze §d§ bude mít stejnou mohutnost jako §T^d§. Speciálně: Je-li §P\ssub\mathbb{Z}_p^n§ dimenze §d§, potom počet prvků §P§ je §p^d§."""), src = "Skriptá, Poznámka 2.67")
        check("""§P\ssub\mathbb{Z}_7^5§, §\dim P=2§. Koľko prvkov má §P§?""", "§49§", "§10§", "§14§", correct = 0, why = """§7^2=49§. Každý prvok §P§ zodpovedá jedinej dvojici súradníc a každá z nich má §7§ možností zo §\mathbb{Z}_7§ (Veta 2.66, Poznámka 2.67). Úloha z testu 24. 11. 2023 (variant Včelka), tam aj s dôkazom.""",
            wrong = mapOf(1 to """§10=2\cdot 5§ je §\dim P\cdot n§. Počet prvkov určujú súradnice: každý prvok §P§ zodpovedá jedinej dvojici zo §\mathbb{Z}_7§ (Veta 2.66).""", 2 to """§14=2\cdot 7§: voľby dvoch súradníc sa násobia, nie sčítajú. Každá zo §7§ hodnôt prvej súradnice ide s každou zo §7§ hodnôt druhej."""))
        practice("count", solo = true)
        def("suradnice")
        check("""Súradnice §\mathbf{x}+\mathbf{y}§ v báze §(\mathbf{x},\mathbf{y},\mathbf{z})§ sú""", "§(1,1,0)§", "§(1,1,1)§", "§(2,0,0)§", correct = 0, why = """§\mathbf{x}+\mathbf{y}=1\mathbf{x}+1\mathbf{y}+0\mathbf{z}§. Súradnice sú koeficienty pri všetkých vektoroch bázy v ich poradí, aj pri §\mathbf{z}§, ktorý vo výraze chýba (otázka zo skrípt pri Definícii 2.68).""",
            wrong = mapOf(1 to """§\mathbf{z}§ v súčte nie je, jeho koeficient je §0§. Súradnice sú koeficienty pri všetkých vektoroch bázy, aj pri tých, ktoré vo výraze chýbajú.""", 2 to """§(2,0,0)§ sú súradnice §2\mathbf{x}§. Každá súradnica patrí k jednému vektoru bázy: pri §\mathbf{x}§ aj pri §\mathbf{y}§ je koeficient §1§."""))
        practice("coords", solo = true)
        prove("p-pd")
        worked("Báza zo súradníc", """Mějme soubor §(\mathbf{x},\mathbf{y})§ vektorů z §\mathbb{Z}_5^2§, o kterém víme, že tvoří bázi §\mathbb{Z}_5^2§. Souřadnice vektoru §(2,1)§ vzhledem k této bázi jsou §(2,1)§ a souřadnice vektoru §(3,3)§ jsou §(4,4)§. Určete, jak vypadají vektory §\mathbf{x}§ a §\mathbf{y}§.""", "Test 9. 11. 2022 (variant 6A), príklad 4", BasisFromCoordsPostup) {
            val z5 = Field.Zp(5)
            basisFromCoordsSteps(fromCoordsCase(5, z5.vec(listOf(2, 1)), z5.vec(listOf(2, 1)), z5.vec(listOf(4, 4)), z5.vec(listOf(3, 3))))
        }
        practice("baza-zo-suradnic", solo = true)
        practice("suradnice-lk", solo = true)
    },

    // Counting and finding subspaces of Z_p^n: textbook questions at Poznámka 2.67, MARAST #13691 and #14159. Every
    // count names what it counts; the app's own steps and templates practise it (no MARAST format, ZASADY 12a).
    lesson("podpriestory-zp", "Koľko je podpriestorov", 30, "Koľko je vektorov, báz a podpriestorov v §\\mathbb{Z}_p^n§, ako nájsť všetky podpriestory.", 4) {
        ask("""Koľko je rôznych podpriestorov dimenzie §1§ v §\mathbb{Z}_3^2§?""", "§8§", "§4§", "§3§", "§1§", correct = 1,
            after = """Štyri: §8§ nenulových vektorov, ale §\mathbf{v}§ a §2\mathbf{v}§ ležia na tej istej priamke, teda §8/2=4§. Priamka tu znamená podpriestor dimenzie §1§, rovina dimenzie §2§; v §\mathbb{Z}_p^n§ sú to konečné množiny bodov, mená sú len požičané z §\mathbb{R}^n§.""", sub = "Ešte bez výkladu. Stačí odhad.",
            wrong = mapOf(0 to """§8§ je počet nenulových vektorov, nie priamok. §\mathbf{v}§ a §2\mathbf{v}§ generujú ten istý podpriestor, takže každá priamka je v ôsmich zarátaná dvakrát.""",
                2 to """§3§ je počet vektorov na jednej priamke §\{\theta,\mathbf{v},2\mathbf{v}\}§, nie počet priamok.""",
                3 to """Priamok cez §\theta§ je viac: §\langle(1,0)\rangle§ a §\langle(0,1)\rangle§ sú dve rôzne."""))
        explore("""Nájdi všetky priamky v §\mathbb{Z}_3^2§.""", """Ťukni na vektor §\mathbf{v}§: rozsvietia sa jeho násobky a čiarkovaná cesta §\theta\to\mathbf{v}\to 2\mathbf{v}§. Keď pretečie cez okraj, počíta sa modulo §3§.""", Pic.Grid(3, goal = Goal.Subspaces),
            """Štyri priamky, každá s tromi bodmi. Spoločný majú len §\theta§.""",
            """Ťuknutie na §2\mathbf{v}§ dalo tú istú priamku ako §\mathbf{v}§: iný generátor, ten istý podpriestor. Ostatných §8§ bodov sa rozdelí po dvoch: §8/2=4§.""")
        explain("Vektor, báza, podpriestor: tri rôzne veci", """**Vektor** je jeden bod, napríklad §(1,2)§. **Báza** je usporiadaný súbor vektorov, napríklad §((1,2))§ alebo §((2,1))§. **Podpriestor** je celá množina bodov: v §\mathbb{Z}_3^2§ je §\langle(1,2)\rangle=\langle(2,1)\rangle=\{\theta,(1,2),(2,1)\}§. Keď sa pýtajú, koľko je podpriestorov, rátaš množiny, a dve bázy tej istej množiny sú jeden podpriestor.""",
            cards = listOf("podpriestor", "baza"))
        check("""Sú §\langle(1,1)\rangle§ a §\langle(2,2)\rangle§ v §\mathbb{Z}_3^2§ dva rôzne podpriestory?""", "Áno, majú rôzne generátory", "Nie, je to tá istá množina", correct = 1,
            why = """Oba sú množina §\{\theta,(1,1),(2,2)\}§. Platí §2\cdot(1,1)=(2,2)§ a §2\cdot(2,2)=(4,4)=(1,1)§, teda každý generuje druhý.""",
            wrong = mapOf(0 to """Rôzne generátory ešte neznamenajú rôzne množiny. Porovnaj množiny: obe sú §\{\theta,(1,1),(2,2)\}§."""))
        explore("""§\mathbb{Z}_2^3§ ako rohy kocky""", """Vektor §(x,y,z)§ s nulami a jednotkami je roh kocky. Násobiť sa dá len §0§ a §1§, takže priamka je §\{\theta,\mathbf{v}\}§. Roh §110§ je vektor §(1,1,0)§. Ťukni postupne na dva nenulové rohy, každý ukáže svoju priamku.""", Pic.Grid(2, goal = Goal.Explore, n = 3, dim = 1),
            """Priamka v §\mathbb{Z}_2^3§ má dva rohy: §\theta§ a jeden ďalší.""",
            """Preto je priamok toľko, koľko nenulových rohov: §2^3-1=7§. Hrany kocky sú len kresba, medzi rohmi žiadne ďalšie vektory nie sú.""")
        explain("Koľko je priamok", """Každý nenulový vektor leží v práve jednej priamke a každá priamka má §p-1§ nenulových vektorov §\mathbf{v},2\mathbf{v},\dots,(p-1)\mathbf{v}§. Dve rôzne priamky majú spoločné len §\theta§. Preto je priamok v §\mathbb{Z}_p^n§ práve §(p^n-1)/(p-1)§: v §\mathbb{Z}_3^2§ je to §8/2=4§, v §\mathbb{Z}_2^3§ §7/1=7§.""",
            src = "Skriptá, otázky pri Poznámke 2.67; MARAST #13691")
        check("""Koľko priamok (podpriestorov dimenzie §1§) má §\mathbb{Z}_5^2§?""", "§24§", "§6§", "§5§", "§25§", correct = 1,
            why = """§24§ nenulových vektorov, na každej priamke štyri: §24/4=6§.""",
            wrong = mapOf(0 to """§24§ je počet nenulových vektorov, nie priamok. Každá priamka ich obsahuje štyri, §\mathbf{v},2\mathbf{v},3\mathbf{v},4\mathbf{v}§, teda je zarátaná štyrikrát.""",
                2 to """§5§ je počet vektorov na jednej priamke vrátane §\theta§, nie počet priamok.""",
                3 to """§25§ je počet všetkých vektorov §\mathbb{Z}_5^2§. Podpriestor je množina vektorov, nie jeden vektor."""))
        check("""Koľko podpriestorov dimenzie §2§ (rovín) má §\mathbb{Z}_2^3§? Tipni.""", "§1§", "§3§, steny kocky cez §\\theta§", "§7§", correct = 2,
            why = """§7§: rovina je každá množina §\{\theta,\mathbf{a},\mathbf{b},\mathbf{a}+\mathbf{b}\}§ s rôznymi nenulovými §\mathbf{a},\mathbf{b}§, nielen stena. Dvojíc rôznych nenulových rohov je §7\cdot 6/2=21§, každá leží v práve jednej rovine a rovina ich obsahuje tri, teda rovín je §21/3=7§.""",
            wrong = mapOf(0 to """Po jednom je len §\{\theta\}§ a celý priestor. Rovina je každá množina §\{\theta,\mathbf{a},\mathbf{b},\mathbf{a}+\mathbf{b}\}§ s nenulovými §\mathbf{a}\neq\mathbf{b}§.""",
                1 to """Steny cez §\theta§ sú len tri z nich. Rovina tu je množina štyroch rohov uzavretá na súčet, nemusí to byť stena."""))
        explore("""Nájdi všetky roviny §\mathbb{Z}_2^3§.""", """Ťukni na dva rôzne nenulové rohy §\mathbf{a},\mathbf{b}§: generujú §\{\theta,\mathbf{a},\mathbf{b},\mathbf{a}+\mathbf{b}\}§, súčet po zložkách modulo §2§. Koeficienty sú len §0§ a §1§, preto má rovina §2\cdot 2=4§ rohy. „Rovina“ je podpriestor dimenzie §2§, nemusí to byť stena kocky.""", Pic.Grid(2, goal = Goal.Subspaces, n = 3, dim = 2),
            """Sedem rovín, každá so štyrmi rohmi.""",
            """Každú rovinu generujú tri neusporiadané dvojice jej nenulových rohov, teda šesť usporiadaných báz, preto ti niektoré ťuknutia dali rovinu, ktorú si už mal. Tri sú steny, tri sú uhlopriečne obdĺžniky a jedna, §\{000,110,011,101\}§, v žiadnej obyčajnej rovine neleží. Aj tak je uzavretá na súčet: §110+011=101§.""")
        explain("Koľko je podpriestorov dimenzie §d§", """Rátaj bázy a vydeľ. Usporiadaných LN §d§-tíc v §\mathbb{Z}_p^n§ je §(p^n-1)(p^n-p)\cdots(p^n-p^{d-1})§ a každá je bázou nejakého podpriestoru dimenzie §d§. Každý podpriestor je v tom čísle toľkokrát, koľko má báz: §(p^d-1)(p^d-p)\cdots(p^d-p^{d-1})§. Roviny §\mathbb{Z}_2^3§: §(8-1)(8-2)=42§ dvojíc, jedna rovina má §(4-1)(4-2)=6§ báz, teda §42/6=7§.""",
            formal = listOf(
                Para(m("""Kolik existuje podprostorů dimenze §2§ v prostoru §\mathbb{Z}_5^3§?""")),
                Para("Je to"), Block("""\frac{(5^3-1)(5^3-5)}{(5^2-1)(5^2-5)}"""),
                Para(m("""Pomůžeme si bází podprostoru. Libovolný LN soubor délky dva je bází nějakého podprostoru dimenze 2. Takových možností je §(5^3-1)(5^3-5)§ (…). V tomto čísle je ale zastoupen každý podprostor tolikrát, kolik existuje jeho bází. To jest §(5^2-1)(5^2-5)§krát. Podělením získáme výsledek.""")),
            ), src = "Skriptá, otázka pri Poznámke 2.67 a jej riešenie")
        worked("Roviny v §\\mathbb{Z}_5^3§", """Kolik existuje podprostorů dimenze §2§ v prostoru §\mathbb{Z}_5^3§?""", "Skriptá, otázka pri Poznámke 2.67", zpChainSolution(5, 3, 2))
        practice("zp-pocet", solo = false)
        explain("Triviálne, netriviálne a párnosť", """Triviálne podpriestory sú §\{\theta\}§ a celý §T^n§ (Definícia 2.9), netriviálne sú ostatné: v §\mathbb{Z}_p^n§ tie s dimenziou §0<d<n§. Podpriestor dimenzie §d§ má §p^d§ vektorov (Poznámka 2.67). V §\mathbb{Z}_2^n§ sú to čísla §2,4,8,\dots§, teda netriviálne majú párny počet (česky sudý; nepárny je lichý), ale §\{\theta\}§ má §1§. V §\mathbb{Z}_3^n§ sú všetky počty nepárne.""",
            formal = cz("""Speciálně: Je-li §P\ssub\mathbb{Z}_p^n§ dimenze §d§, potom počet prvků §P§ je §p^d§."""), src = "Skriptá, Poznámka 2.67", cards = listOf("trivialny"))
        check("""Ktoré podpriestory §\mathbb{Z}_2^3§ majú nepárny počet vektorov?""", "Žiadne", "Len §\\{\\theta\\}§", "Priamky", correct = 1,
            why = """§\{\theta\}§ má §2^0=1§ vektor, ostatné §2§, §4§ alebo §8§.""",
            wrong = mapOf(0 to """Zabudol si na §\{\theta\}§: je to podpriestor s jedným vektorom, len triviálny.""",
                2 to """Priamka §\{\theta,\mathbf{v}\}§ má §2§ vektory, to je párne číslo."""))
        practice("zp-vypis", solo = false)
        check("""Koľko podpriestorov má §\mathbb{Z}_2^3§ spolu, aj s triviálnymi?""", "§14§", "§16§", "§8§", correct = 1,
            why = """§1+7+7+1=16§: §\{\theta\}§, §7§ priamok, §7§ rovín a celý priestor.""",
            wrong = mapOf(0 to """Chýbajú triviálne §\{\theta\}§ a celý §\mathbb{Z}_2^3§. Aj to sú podpriestory, dimenzie §0§ a §3§ (Definícia 2.9), a otázka sa pýta na všetky.""",
                2 to """§8§ je počet vektorov, nie podpriestorov. Podpriestor je množina vektorov."""))
        practice("zp-pocet", solo = true)
        practice("zp-zoznam", solo = true)
    },
)


/** A lecture clip: real captions from /api/captions, cut times, question before, task after. Loaded from assets/clips.json. */
data class ClipDef(val id: String, val lesson: Int, val rec: String, val lecture: String, val from: Double, val to: Double, val why: String, val before: String, val warning: Boolean, val task: ClipTask, val cues: List<ClipCue>, val course: String = "la1", val teacher: String? = null)
/** [wrong]: per wrong option, the misconception behind it (clips.json "wrong", keyed by option index). */
data class ClipTask(val q: String, val options: List<String>, val correct: Int, val why: String, val wrong: Map<Int, String> = emptyMap())
data class ClipCue(val t0: Double, val t1: Double, val words: List<ClipWord>) { val text get() = words.joinToString(" ") { it.w } }
data class ClipWord(val t0: Double, val t1: Double, val w: String)

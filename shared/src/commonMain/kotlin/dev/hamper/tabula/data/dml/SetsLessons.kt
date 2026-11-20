package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*
import dev.hamper.tabula.screens.dml.ArrowsDraw
import dev.hamper.tabula.screens.dml.MapWant
import dev.hamper.tabula.screens.dml.SetGoal
import dev.hamper.tabula.screens.dml.SetsBuild

/*
 * The six lessons of Množiny a zobrazenia. Each step says where it rests: textbook items by number (per kind, as the
 * rendered BI-DML textbook), past tests by date, clips by lecture (Spěvák, parallel 2).
 */

private const val Guess = "Ešte bez výkladu. Stačí odhad."
private val YesNo = arrayOf("Áno", "Nie")

val lMnoziny = lesson("dml-mz-mnoziny", "Množiny a operácie", 14, "Čo je množina, inklúzia, potenčná množina, operácie a karteziánsky súčin.", art = 0, topics = listOf("MZ1")) {
    ask("""Platí §\{1,2\}=\{2,1,1\}§?""", *YesNo, correct = 0, after = """Platí, poradie ani opakovanie v množine nehrajú rolu.""", sub = Guess,
        wrong = mapOf(1 to """Množina nie je zoznam, prvok v nej buď je, alebo nie je. Koľkokrát ho napíšeš, je jedno (Varovanie 5.1)."""))
    explore("""Ťukaj prvky do §A§ a §B§.""", """Sleduj §A\setminus B§ a §A\cap\overline{B}§.""", SetsBuild("A - B = A & ~B", goal = SetGoal.Explore),
        """Obe strany sú vždy rovnaké: §x\notin B§ je to isté ako §x\in\overline{B}§.""")
    explain("Množina je len to, čo v nej leží.", """Zadáš ju výčtom alebo vlastnosťou nad univerzom §\mathcal{U}§. Poradie ani opakovanie nerozhodujú, len či prvok v nej je.""",
        cards = listOf("dml-mz-mnozina"))
    def("dml-mz-mnozina")
    check("""Koľko prvkov má §\{\emptyset,\{\emptyset\}\}§?""", "§0§", "§1§", "§2§", correct = 2, why = """Dva, §\emptyset§ a §\{\emptyset\}§ sú rôzne prvky.""",
        wrong = mapOf(0 to """Prázdna množina ako prvok sa ráta, je to objekt. Vonkajšia množina obsahuje dve rôzne veci.""", 1 to """§\emptyset§ a §\{\emptyset\}§ sa líšia: druhá má jeden prvok, prvá žiadny."""))
    explain("Inklúzia je implikácia, rovnosť ekvivalencia.", """§A\subseteq B§: každé §x§ z §A§ je aj v §B§. Rovnosť sú dve inklúzie. Potenčná množina §\mathcal{P}(A)§ má za prvky podmnožiny §A§.""",
        cards = listOf("dml-mz-inkluze"))
    def("dml-mz-inkluze")
    check("""Čo platí pre §X=\{1,2\}§?""", """§\{1\}\in\mathcal{P}(X)§""", """§1\in\mathcal{P}(X)§""", """§\{1\}\subseteq\mathcal{P}(X)§""", correct = 0, why = """§\{1\}\subseteq X§, a prvky §\mathcal{P}(X)§ sú práve podmnožiny §X§.""",
        wrong = mapOf(1 to """Prvky §\mathcal{P}(X)§ sú množiny, §1§ je číslo. Podľa Definície 5.2 je §Y\in\mathcal{P}(X)§ práve vtedy, keď §Y\subseteq X§; podmnožinou §X§ je §\{1\}§, nie samotné číslo §1§.""", 2 to """Zámena §\in§ a §\subseteq§ (Varovanie 5.3). Inklúzia by chcela §1\in\mathcal{P}(X)§."""))
    worked("Potenčná množina", """Vypíš §\mathcal{P}(A)§ pre §A=\{1,2,3\}§ a z nej §\mathcal{P}(B)§ pre §B=\{1,2,3,4\}§.""", "Skriptá, Příklad 5.4", PowerSetListPostup) {
        val (listA, extend) = PowerSetListPostup
        listA("""Vypíšem všetky podmnožiny podľa počtu prvkov, aj §\emptyset§ a celú §A§; spolu §2^3=8§.""", eq("""\begin{aligned}\mathcal{P}(A)=\{&\emptyset,\\&\{1\},\{2\},\{3\},\\&\{1,2\},\{1,3\},\{2,3\},\\&\{1,2,3\}\}\end{aligned}""", "Po riadkoch podľa počtu prvkov: §0,1,2,3§."))
        extend("""Každá podmnožina §B§ buď §4§ obsahuje, alebo nie. §\mathcal{P}(B)§ sú teda všetky prvky §\mathcal{P}(A)§ a k tomu každý z nich s pridanou §4§:""",
            eq("""\begin{aligned}&\{4\},\\&\{1,4\},\{2,4\},\{3,4\},\\&\{1,2,4\},\{1,3,4\},\{2,3,4\},\\&\{1,2,3,4\}\end{aligned}""", "Spolu §16=2^4§ prvkov."))
    }
    thm("dml-mz-t-inkluze")
    explain("Operácie sú logické spojky.", """Prienik je §\wedge§, zjednotenie §\vee§, doplnok §\neg§, rozdiel §x\in A\wedge x\notin B§. Na tom stoja všetky dôkazy identít.""",
        cards = listOf("dml-mz-operace"))
    def("dml-mz-operace")
    practice("dml-mz-mnoziny", solo = false)
    explain("Súčin páruje prvky.", """§A\times B§ sú usporiadané dvojice §(a,b)§ s §a\in A§, §b\in B§. Leží v univerze §\mathcal{U}\times\mathcal{U}§, nie v §\mathcal{U}§.""",
        cards = listOf("dml-mz-kart"))
    def("dml-mz-kart")
    check("""Koľko prvkov má §\{1,2,3\}\times\{a,b\}§?""", "§5§", "§6§", "§9§", correct = 1, why = """Šesť, §3\cdot 2=6§ dvojíc (Tvrzení 5.2).""",
        wrong = mapOf(0 to """Sčítal si počty, súčin ich násobí. Každý z §3§ prvkov ide s každým z §2§.""", 2 to """§9=3^2§ by mal §\{1,2,3\}^2§. Druhá množina má len §2§ prvky."""))
    def("dml-mz-vice")
    def("dml-mz-disjunktni")
    check("""§\{1,2\}§, §\{2,3\}§, §\{1,3\}§ majú prázdny prienik všetkých troch. Sú po dvoch disjunktné?""", *YesNo, correct = 1, why = """Nie sú, §\{1,2\}\cap\{2,3\}=\{2\}§.""",
        wrong = mapOf(0 to """Prázdny prienik všetkých troch nestačí. Po dvoch znamená prázdny prienik každej dvojice (Otázka 5.1)."""))
    practice("dml-mz-mnoziny", solo = true)
}

val lIdentity = lesson("dml-mz-identity", "Množinové identity cez logiku", 18, "Rovnosť a inklúziu množín dokážeš prepisom na formuly; neplatnú vyvrátiš malým protipríkladom.", art = 1, topics = listOf("MZ1")) {
    ask("""Platí pre všetky množiny §A,B,C\subseteq\mathcal{U}§ rovnosť §§(C\setminus B)\cup(C\setminus A)=C\setminus(A\cup B)?§§""", *YesNo, correct = 1, after = """Neplatí, prvok z §C§ a §A§, ktorý nie je v §B§, je len vľavo.""", sub = Guess,
        wrong = mapOf(0 to """Vyzerá to ako De Morgan, ale so zlou operáciou. Vľavo vyjde §C\setminus(A\cap B)§, vpravo §C\setminus(A\cup B)§."""))
    explore("Nájdi protipríklad.", """Ťukaj prvky do §A§, §B§, §C§, kým sa strany nebudú líšiť.""", SetsBuild("(C - B) | (C - A) = C - (A | B)"),
        """Stačí jeden prvok v §C§ a §A§, ktorý nie je v §B§: vľavo je, vpravo nie.""", "Písomka 4. 12. 2025 (variant E), príklad 2 c2.")
    explain("Príslušnosť je formula.", """§x\in A\cap B§ je §x\in A\wedge x\in B§; §\cup§ je §\vee§, §\setminus§ je §\wedge\neg§, doplnok §\neg§. Rovnosť množín je ekvivalencia formúl pre každé §x§, inklúzia implikácia.""",
        formal = cz("""Výrok „§x§ leží v množině §M§“, kde §M§ vznikne nějakou aplikací množinových operací §\overline{(\ )},\cap,\cup§ na množiny §A,B,C,\dots§ je ekvivalentní formuli, která vznikne analogickou aplikací logických spojek §\neg,\wedge,\vee§ na prvotní formule §(x\in A),(x\in B),(x\in C),\dots§."""),
        src = "Skriptá, Poznámka 5.3")
    clip("dml-mz-demorgan")
    worked("Dôkaz rovnosti", """Dokáž §A\setminus(B\cup C)=(A\setminus B)\cap(A\setminus C)§.""", "Skriptá, Cvičení 5.3 h a jeho riešenie", SetIdentityPostup) {
        val (defined, unfoldAim, compare, end) = SetIdentityPostup
        defined("""Všetky množiny sú podmnožiny §\mathcal{U}§, vzťah je definovaný.""")
        unfoldAim("""Nech §x\in\mathcal{U}§ je ľubovoľné. Príslušnosť k ľavej strane prepisujem zvonku dovnútra, až kým nevyjde pravá strana.""",
            eq("""x\in A\setminus(B\cup C)\Leftrightarrow(x\in A\wedge\neg(x\in B\cup C))""", "Definícia rozdielu"),
            eq("""\Leftrightarrow(x\in A\wedge\neg(x\in B\vee x\in C))""", "Definícia zjednotenia"),
            eq("""\Leftrightarrow(x\in A\wedge(x\notin B\wedge x\notin C))""", "De Morgan"),
            eq("""\Leftrightarrow((x\in A\wedge x\notin B)\wedge(x\in A\wedge x\notin C))""", "Idempotencia: §x\\in A§ dvakrát"),
            eq("""\Leftrightarrow(x\in A\setminus B\wedge x\in A\setminus C)\Leftrightarrow x\in(A\setminus B)\cap(A\setminus C)""", "Definície rozdielu a prieniku"))
        compare("""Reťazec ekvivalencií spojil formulu ľavej strany s formulou pravej, sú teda ekvivalentné.""")
        end("""Ekvivalencia platí pre každé §x§, teda množiny sa rovnajú.""")
    }
    check("""Čo je §x\in A\setminus(B\cap C)§ po rozpísaní?""", """§x\in A\wedge\neg(x\in B\wedge x\in C)§""", """§x\in A\wedge x\notin B\wedge x\notin C§""", """§x\in A\vee\neg(x\in B\wedge x\in C)§""", correct = 0,
        why = """Rozdiel je §\wedge\neg§ a prienik §\wedge§, nič viac.""",
        wrong = mapOf(1 to """Negácia konjunkcie nie je konjunkcia negácií. De Morgan dáva §\neg(p\wedge q)\Leftrightarrow(\neg p\vee\neg q)§.""", 2 to """Rozdiel je konjunkcia: §x\in A§ a zároveň nie v §B\cap C§."""))
    thm("dml-mz-t-algebra")
    thm("dml-mz-t-doplnek")
    explain("Neplatí? Stačí jeden protipríklad.", """Keď sa formuly pre nejaké §x§ líšia, zvoľ malé množiny a vypočítaj obe strany. Tak ukážeš, že je to naozaj protipríklad.""",
        formal = cz("""Vidíme, že výroky na levé a pravé straně zřejmě nejsou ekvivalentní (vhodný protipříklad je např. §X=\{1\},Y=\{2\},Z=\{3\}§ v universu §\mathcal{U}=\{1,2,3\}§)."""),
        src = "Skriptá, riešenie Cvičení 5.4 b")
    clip("dml-mz-pisomka")
    check("""Je §X=\{1\}§, §Y=\{2\}§, §Z=\{3\}§ v §\mathcal{U}=\{1,2,3\}§ protipríklad na §X\setminus(Y\cup Z)=(X\setminus Y)\cup Z§?""", *YesNo, correct = 0,
        why = """Je, vľavo vyjde §\{1\}§, vpravo §\{1,3\}§.""",
        wrong = mapOf(1 to """Je, strany sa líšia. §X\setminus(Y\cup Z)=\{1\}§, ale §(X\setminus Y)\cup Z=\{1,3\}§."""))
    explain("Pri súčine rozpíš dvojicu.", """Pre §\times§ ber §(u,v)\in\mathcal{U}\times\mathcal{U}§ a prepisuj §§(u,v)\in X\times Y\Leftrightarrow u\in X\wedge v\in Y.§§ Výraz ako §X\setminus(Y\times Z)§ nie je definovaný.""",
        formal = cz("""Tvrzení §§X\setminus(Y\times Z)=(X\setminus Y)\times(X\setminus Z)§§ neplatí, množina na levé straně rovnosti §X\setminus(Y\times Z)§ není definovaná, jde o rozdíl dvou množin z jiných univers (§\mathcal{U}§ oproti §\mathcal{U}\times\mathcal{U}§)."""),
        src = "Skriptá, riešenie Cvičení 5.5 c")
    explore("Doplnok súčinu", """Nájdi §X,Y§, pre ktoré sa strany líšia.""", SetsBuild("~(X * Y) = ~X * ~Y", listOf("a", "b")),
        """Napr. §X=\{a\}§, §Y=\{a,b\}§: vpravo §\{b\}\times\emptyset=\emptyset§, vľavo §\{(b,a),(b,b)\}§.""", "Skriptá, riešenie Cvičení 5.5 f.")
    worked("Inklúzia so súčinom", """Platí vždy §\overline{X}\times\overline{Y}\subseteq\overline{X\times Y}§?""", "Písomka 2024 (variant N), príklad 2c", SetIdentityPostup) {
        val (defined, unfoldAim, compare, end) = SetIdentityPostup
        defined("""Obe strany sú podmnožiny §\mathcal{U}\times\mathcal{U}§, vzťah je definovaný.""")
        unfoldAim("""Nech §(u,v)\in\mathcal{U}\times\mathcal{U}§ je ľubovoľná dvojica. Ľavú stranu prepíšem podľa definície súčinu a doplnku.""",
            eq("""(u,v)\in\overline{X}\times\overline{Y}\Leftrightarrow(u\in\overline{X}\wedge v\in\overline{Y})\Leftrightarrow(u\notin X\wedge v\notin Y)"""))
        compare("""Pri inklúzii stačí implikácia: z konjunkcie plynie disjunkcia, a tá je formulou pravej strany.""",
            eq("""\Rightarrow(u\notin X\vee v\notin Y)""", "Z konjunkcie plynie disjunkcia"),
            eq("""\Leftrightarrow\neg(u\in X\wedge v\in Y)\Leftrightarrow\neg((u,v)\in X\times Y)\Leftrightarrow(u,v)\in\overline{X\times Y}""", "De Morgan, súčin, doplnok"))
        end("""Implikácia platí pre každú dvojicu, inklúzia teda platí vždy. Opačná nie (protipríklad vyššie).""")
    }
    check("""Platí táto rovnosť vždy?""", *YesNo, correct = 1, sub = """§§(X\setminus Y)\times(X\setminus Z)\softbreak[1]=(X\setminus Z)\times(X\setminus Y)§§""",
        why = """Neplatí, dvojice sú usporiadané: pre §X=\{1,2\}§, §Y=\{1\}§, §Z=\{2\}§ je vľavo §\{(2,1)\}§, vpravo §\{(1,2)\}§.""",
        wrong = mapOf(0 to """Súčin nie je komutatívny, §(2,1)\neq(1,2)§. Rovnosť platí, len keď sú §X\setminus Y§ a §X\setminus Z§ rovnaké alebo jedna prázdna."""))
    prove("dml-mz-p-identita")
    practice("dml-mz-rovnost", solo = false)
    practice("dml-mz-rovnost", solo = true)
}

val lZobrazenia = lesson("dml-mz-zobrazenia", "Zobrazenie, obraz a vzor", 13, "Zobrazenie ako množina dvojíc, totálnosť, definičný obor, obor hodnôt, obraz a vzor množiny.", art = 2, topics = listOf("MZ1")) {
    ask("""Je §\{(1,a),(1,b),(2,a)\}§ zobrazenie z §\{1,2\}§ do §\{a,b\}§?""", *YesNo, correct = 1, after = """Nie je, prvok §1§ má dva obrazy.""", sub = Guess,
        wrong = mapOf(0 to """Zobrazenie dovolí každému prvku najviac jeden obraz. Tu má §1§ obrazy §a§ aj §b§."""))
    explore("Nakresli totálne zobrazenie.", "Ťukni na prvok vľavo, potom vpravo. Každý prvok vľavo potrebuje práve jednu šípku.", ArrowsDraw(listOf("1", "2", "3"), listOf("a", "b"), MapWant.Total),
        """Každý prvok §A§ má práve jednu šípku. Do jedného prvku §B§ ich smie viesť viac.""")
    explain("Zobrazenie je množina dvojíc.", """§f\subseteq A\times B§, kde každé §a§ má najviac jeden obraz. Totálne: práve jeden. V BI-DML je každé zobrazenie totálne.""",
        cards = listOf("dml-mz-zobrazeni"))
    clip("dml-mz-totalne")
    def("dml-mz-zobrazeni")
    check("""Je §f(n)=n-1§, §f:\mathbb{N}\to\mathbb{N}§ zobrazenie v zmysle BI-DML?""", *YesNo, correct = 1, why = """Nie je, §f(1)=0\notin\mathbb{N}§, a zobrazenia sú totálne.""",
        wrong = mapOf(0 to """Pre §n=1§ obraz v §\mathbb{N}=\{1,2,\dots\}§ neexistuje. Totálne zobrazenie ho chce pre každé §n§."""))
    worked("Definičný obor a obor hodnôt", """Urči §D_f§ a §H_f§: a) poslednej číslice prirodzeného čísla, b) nasledovníka prirodzeného čísla.""", "Skriptá, Cvičení 5.8 a, b", DomainRangePostup) {
        val (domain, range) = DomainRangePostup
        domain("""a) Každé prirodzené číslo má poslednú číslicu, teda §D_f=\mathbb{N}§. b) §f(n)=n+1§ je definované pre každé §n§, teda tiež §D_f=\mathbb{N}§.""")
        range("""a) Objavia sa všetky číslice §0§ až §9§ (napríklad z §10,1,\dots,9§), teda §H_f=\{0,1,\dots,9\}§.""")
        range("""b) Hodnota §1§ nevyjde nikdy, lebo §n\geq 1§; každé §k\geq 2§ je §f(k-1)§. Teda §H_f=\mathbb{N}\setminus\{1\}§.""")
    }
    check("""Aký je §H_f§ pre §f(n)=n^2§, §f:\mathbb{Z}\to\mathbb{Z}§?""", """§\mathbb{Z}§""", """§\{k^2\mid k\in\mathbb{N}_0\}§""", """§\mathbb{N}§""", correct = 1, why = """Obor hodnôt sú druhé mocniny: §0,1,4,9,\dots§.""",
        wrong = mapOf(0 to """Obor hodnôt sú len zasiahnuté prvky. Záporné čísla ani §2§ nikto nezasiahne.""", 2 to """§\mathbb{N}§ obsahuje aj §2§ či §3§, a tie štvorcami nie sú. A §0=0^2§ v §\mathbb{N}§ chýba."""))
    explain("Obraz a vzor množiny", """§f[M]§: kam sa dostanú prvky §M§. §f^{-1}[N]§: kto sa dostane do §N§. Vzor množiny existuje vždy, aj bez inverzie.""",
        cards = listOf("dml-mz-obraz"))
    def("dml-mz-obraz")
    check("""Pre §f(n)=n^2§ na §\mathbb{Z}§ je §f^{-1}[\{1,2\}]=§""", """§\{-1,1\}§""", """§\{1,4\}§""", """§\{1\}§""", correct = 0, why = """§(-1)^2=1^2=1§ a štvorec §2§ v §\mathbb{Z}§ nie je.""",
        wrong = mapOf(1 to """To je obraz §f[\{1,2\}]§, nie vzor. Vzor sú §n§ s §n^2\in\{1,2\}§.""", 2 to """Chýba §-1§: aj §(-1)^2=1\in\{1,2\}§."""))
    // the image and preimage computed from the map (Zásada 13), as the generator dml-mz-sipky computes them
    val exA = listOf("1", "2", "3", "4"); val exB = listOf("a", "b", "c"); val exF = intArrayOf(0, 2, 0, 2)
    val exImg = image(exF, setOf(0, 2)).map { exB[it] }.toSet(); val exPre = preimage(exF, setOf(0, 1)).map { exA[it] }.toSet()
    worked("Obraz a vzor pri konečnom zobrazení", """§f:\{1,2,3,4\}\to\{a,b,c\}§ s hodnotami §§${tableRows(Triple(exF, exA to exB, "f"))}§§ Urči §f[\{1,3\}]§ a §f^{-1}[\{a,b\}]§.""", "Definice 5.8", ImagePreimagePostup) {
        val (img, pre) = ImagePreimagePostup
        img("""Obraz tvoria obrazy prvkov §1§ a §3§; je to množina, preto §a§ v nej je len raz.""", eq("""f[\{1,3\}]=\{f(1),f(3)\}=${setTex(exImg, exB)}"""))
        pre("""Vzor tvoria prvky s obrazom §a§ alebo §b§: §f(1)=f(3)=a§ a na §b§ nevedie nič.""", eq("""f^{-1}[\{a,b\}]=${setTex(exPre, exA)}"""))
    }
    practice("dml-mz-sipky", solo = false)
    check("""Čo je §f[A]§ pre §f:A\to B§?""", """§H_f§""", """§B§""", """§D_f§""", correct = 0, why = """Obraz celej §A§ je obor hodnôt, §H_f=f[A]§.""",
        wrong = mapOf(1 to """§f[A]§ sú len zasiahnuté prvky §B§. Celé §B§ to je, len keď je §f§ surjektívne.""", 2 to """§D_f§ je podmnožina §A§, obraz leží v §B§. §f[A]=\{f(a)\mid a\in A\}§ zbiera všetky hodnoty, ktoré §f§ dosiahne, a presne tak je definovaný obor hodnôt §H_f§; §D_f§ naopak zbiera vstupy."""))
    check("""Zúženie §f|_{\{1,2\}}§ zobrazenia §f(n)=2n§ na §\mathbb{Z}§ má obor hodnôt""", """§\{2,4\}§""", """§\{1,2\}§""", """§\{2k\mid k\in\mathbb{Z}\}§""", correct = 0, why = """§f(1)=2§ a §f(2)=4§.""",
        wrong = mapOf(1 to """To je definičný obor zúženia. Hodnoty sú §f(1)=2§ a §f(2)=4§.""", 2 to """To sú hodnoty celého §f§. Zúžené berie len §1§ a §2§."""))
    practice("dml-mz-sipky", solo = true)
}

val lInjSurj = lesson("dml-mz-injsurj", "Injektívne a surjektívne", 20, "Injektivitu a surjektivitu dokážeš z definície a vyvrátiš protipríkladom, na §\\mathbb{Z}^n§ aj §\\mathbb{R}^n§.", art = 3, topics = listOf("MZ1")) {
    ask("""Je §f(n)=13n§, §f:\mathbb{Z}\to\mathbb{Z}§ surjektívne?""", *YesNo, correct = 1, after = """Nie je, §1§ nemá vzor: §n=\tfrac{1}{13}\notin\mathbb{Z}§.""", sub = Guess,
        wrong = mapOf(0 to """Každé §n§ sa síce zobrazí, ale surjektivita chce vzor pre každý cieľ. Na §1§ nevedie žiadne celé §n§."""))
    explore("Prosté, ale nie na.", "Žiadne dve šípky do toho istého prvku a vpravo niečo ostane bez šípky.", ArrowsDraw(listOf("1", "2", "3"), listOf("a", "b", "c", "d"), MapWant.InjNotSurj),
        """Pri §|A|<|B|§ ostane vždy niečo bez vzoru (Pozorovanie 5.2).""")
    explain("Prosté: rôzne vstupy, rôzne výstupy. Na: každý cieľ zasiahnutý.", """Injektivitu dokazuješ tak, že z §f(x)=f(y)§ odvodíš §x=y§. Surjektivitu tak, že ku každému §y§ nájdeš vzor vzorcom. Vyvrátiť stačí jeden protipríklad.""",
        cards = listOf("dml-mz-injsurj"))
    clip("dml-mz-injektivne")
    def("dml-mz-injsurj")
    explore("Na, ale nie prosté.", "Každý prvok vpravo musí dostať šípku.", ArrowsDraw(listOf("1", "2", "3"), listOf("a", "b"), MapWant.SurjNotInj),
        """Pri §|A|>|B|§ sa dva prvky musia stretnúť (Pozorovanie 5.2).""")
    worked("Dôkaz injektivity", """Dokáž, že §f(n)=n^3+n§, §f:\mathbb{Z}\to\mathbb{Z}§ je prosté.""", "Skriptá, Příklad 5.5", InjPostup) {
        val (expand, solveAim, end) = InjPostup
        expand("""Pre ľubovoľné §m,n\in\mathbb{Z}§ rozpíšem rovnosť obrazov a všetko prevediem na jednu stranu.""", eq("""f(m)=f(n)\Leftrightarrow m^3+m=n^3+n\Leftrightarrow m^3-n^3+m-n=0"""))
        solveAim("""Vytknem §m-n§ podľa rozkladu rozdielu tretích mocnín; druhý činiteľ nulový byť nemôže.""",
            eq("""\Leftrightarrow(m-n)(m^2+mn+n^2+1)=0"""),
            eq("""\Leftrightarrow((m-n=0)\vee(m^2+mn+n^2+1=0))\Leftrightarrow m=n""", "Druhá rovnica nemá riešenie: diskriminant §n^2-4(n^2+1)<0§"))
        end("""Z rovnosti obrazov plynie §m=n§, zobrazenie je prosté. Nestačí povedať „§n^3§ aj §n§ sú prosté“: §g(n)=n^3-n§ prosté nie je.""")
    }
    worked("Vyvrátenie injektivity", """Je §f(n)=n^2+n§, §f:\mathbb{Z}\to\mathbb{Z}§ prosté?""", "Skriptá, Příklad 5.6", InjPostup) {
        val (expand, solveAim, end) = InjPostup
        expand("""Rovnosť obrazov rozpíšem a upravím na súčin; pokus o dôkaz ukáže, kde hľadať protipríklad.""", eq("""f(m)=f(n)\Leftrightarrow m^2+m=n^2+n\Leftrightarrow(m-n)(m+n+1)=0"""))
        solveAim("""Súčin je nulový aj pre §m=-n-1§, nielen pre §m=n§.""", eq("""\Leftrightarrow((m=n)\vee(m=-n-1))"""))
        end("""Protipríklad: §f(2)=6=f(-3)§, hoci §2\neq -3§. Zobrazenie nie je prosté.""")
    }
    check("""Ako dokážeš, že §f§ je injektívne?""", """Z §f(x)=f(y)§ pre ľubovoľné §x,y§ odvodím §x=y§""", """Nájdem §x\neq y§ s §f(x)\neq f(y)§""", """Ukážem, že každé §x§ má obraz""", correct = 0,
        why = """Definícia hovorí o všetkých dvojiciach, tak treba ľubovoľné §x,y§.""",
        wrong = mapOf(1 to """To je dôkaz príkladom (Varovanie 5.4). Jedna dvojica nič nehovorí o ostatných.""", 2 to """To je totálnosť, nie injektivita. Injektivita porovnáva obrazy dvoch prvkov."""))
    worked("Dôkaz surjektivity", """Dokáž, že §f(m,n)=(m,m+2n)§, §f:\mathbb{R}^2\to\mathbb{R}^2§ je surjektívne.""", "Skriptá, Příklad 5.7", SurjPostup) {
        val (expand, express, check) = SurjPostup
        expand("""Ku ľubovoľnému §(x,y)\in\mathbb{R}^2§ hľadám vzor §(m,n)§; §x,y§ sú parametre.""", eq("""f(m,n)=(x,y)\Leftrightarrow(x=m\wedge y=m+2n)"""))
        express("""Z rovníc vyjadrím §m§ a §n§.""", eq("""\Leftrightarrow\Big(m=x\wedge n=\frac{y-x}{2}\Big)"""))
        check("""Vzor §(x,\tfrac{y-x}{2})§ leží v §\mathbb{R}^2§ a dosadením §f(x,\tfrac{y-x}{2})=(x,y)§. Zobrazenie je surjektívne.""")
    }
    check("""Ten istý predpis ako §f:\mathbb{Z}^2\to\mathbb{Z}^2§: je surjektívny?""", *YesNo, correct = 1, sub = """§§f(m,n)=(m,m+2n)§§""", why = """Nie je, pre §(0,1)§ vyjde §n=\tfrac{1}{2}\notin\mathbb{Z}§ (Příklad 5.8).""",
        wrong = mapOf(0 to """Vzorec §n=\tfrac{y-x}{2}§ nie je vždy celé číslo. Pre nepárne §y-x§ vzor v §\mathbb{Z}^2§ nie je."""))
    check("""Je §f(n)=(n^2,n^2+n)§, §f:\mathbb{Z}\to\mathbb{Z}^2§ injektívne?""", *YesNo, correct = 0, why = """Je, druhá zložka mínus prvá je §n§ (Cvičení 5.10 b).""",
        wrong = mapOf(1 to """„Vidím §n^2§, teda nie je prosté“ je chybná úvaha. Z oboch zložiek naraz plynie §n=n'§."""))
    prove("dml-mz-p-z2")
    practice("dml-mz-injsurj", solo = false)
    check("""Čo platí pre §f(m,n)=2m-n§, §f:\mathbb{Z}^2\to\mathbb{Z}§?""", "Injektívne, nie surjektívne", "Surjektívne, nie injektívne", "Bijekcia", correct = 1,
        why = """Surjektívne, nie injektívne: §f(0,-x)=x§, ale §f(0,0)=f(1,2)§.""",
        wrong = mapOf(0 to """Opačne: §f(0,0)=f(1,2)=0§, teda nie je prosté, a §f(0,-x)=x§, teda je na.""", 2 to """Bijekcia musí byť prostá, a §f(0,0)=f(1,2)§ (Cvičení 5.10 e)."""))
    practice("dml-mz-injsurj", solo = true)
}

val lSkladanie = lesson("dml-mz-skladanie", "Zloženie, identita, inverzia", 18, "Ako sa skladajú zobrazenia, mocnina, kedy existuje inverzia a otázky „existujú také §f§ a §g§?“", art = 4, topics = listOf("MZ1")) {
    ask("""Zobrazenia §f,g:\mathbb{Z}\to\mathbb{Z}§ sú §g(n)=n+1§ a §f(n)=2n§. Koľko je §(f\circ g)(3)§?""", "§8§", "§7§", "§6§", correct = 0, after = """§8§, najprv §g(3)=4§, potom §f(4)=8§.""", sub = Guess,
        wrong = mapOf(1 to """To je §g(f(3))=g(6)=7§, opačné poradie. §f\circ g§ znamená najprv §g§.""", 2 to """§6=f(3)§, §g§ si vynechal. Zloženie použije obe zobrazenia."""))
    explore("Nakresli bijekciu.", "Každý prvok vpravo musí dostať práve jednu šípku. Obrátené šípky potom tvoria inverziu.", ArrowsDraw(listOf("1", "2", "3"), listOf("a", "b", "c"), MapWant.Bijective),
        """Obrátené šípky bijekcie sú zase zobrazenie: inverzia (Veta 5.4).""")
    explain("Zloženie: najprv §g§, potom §f§.", """§(f\circ g)(a)=f(g(a))§ a cieľ §g§ musí byť definičný obor §f§. Zápis je opačne než šípky na obrázku.""",
        cards = listOf("dml-mz-slozeni"))
    clip("dml-mz-poradie")
    def("dml-mz-slozeni")
    def("dml-mz-idzobr")
    def("dml-mz-mocnina")
    check("""§f(n)=2n§ na §\mathbb{Z}§: koľko je §f^3(1)§?""", "§8§", "§6§", "§1§", correct = 0, why = """§8§, lebo §f(f(f(1)))=f(f(2))=f(4)=8§.""",
        wrong = mapOf(1 to """§6=3\cdot f(1)§. Mocnina skladá: §f(f(f(1)))=8§.""", 2 to """§1=f^0(1)§, nie §f^3(1)§. Tri razy zdvojnásob."""))
    explain("Inverzia vracia obrazy na vzory.", """§f^{-1}§ ide opačným smerom a musia platiť obe zloženia: §§\begin{aligned}f\circ f^{-1}&=\mathrm{id}_B,\\f^{-1}\circ f&=\mathrm{id}_A.\end{aligned}§§ Existuje práve pre bijekcie.""",
        cards = listOf("dml-mz-inverze"))
    def("dml-mz-inverze")
    thm("dml-mz-t-invbij")
    worked("Inverzia na §\\mathbb{R}§", """Nájdi inverziu k §f(x)=2x+1§, §f:\mathbb{R}\to\mathbb{R}§.""", "Definice 5.13", InversePostup) {
        val (express, both, end) = InversePostup
        express("""Vymením úlohy vzoru a obrazu: z §y=f(x)§ vyjadrím §x§.""", eq("""y=2x+1\Leftrightarrow x=\frac{y-1}{2}"""))
        both("""Kandidát §g(y)=\tfrac{y-1}{2}§ overím v oboch zloženiach; obe musia byť identity.""", eq("""(f\circ g)(y)=2\cdot\frac{y-1}{2}+1=y,\quad(g\circ f)(x)=\frac{(2x+1)-1}{2}=x"""))
        end("""Teda §f^{-1}(y)=\tfrac{y-1}{2}§. Na §\mathbb{Z}§ by inverzia nebola, §\tfrac{y-1}{2}§ nie je vždy celé.""")
    }
    thm("dml-mz-t-sklad")
    prove("dml-mz-p-sklad-g")
    prove("dml-mz-p-id")
    thm("dml-mz-t-invert")
    check("""Pre bijekcie §f:B\to C§, §g:A\to B§ je §(f\circ g)^{-1}=§""", """§g^{-1}\circ f^{-1}§""", """§f^{-1}\circ g^{-1}§""", """§f\circ g§""", correct = 0,
        why = """Opačné poradie: najprv sa vráti §f§, potom §g§ (Veta 5.3).""",
        wrong = mapOf(1 to """Poradie sa obráti: posledné použité §f§ sa vracia prvé. §f^{-1}\circ g^{-1}§ ani nemusí byť definované.""", 2 to """§f\circ g§ ide z §A§ do §C§, inverzia musí ísť z §C§ do §A§."""))
    practice("dml-mz-zlozenie", solo = false)
    practice("dml-mz-zlozenie", solo = true)
}

val lMohutnost = lesson("dml-mz-mohutnost", "Mohutnosť", 14, "Rovnako veľké množiny cez bijekciu, konečné a spočetné množiny, počty prvkov súčinu a potenčnej množiny.", art = 5, topics = listOf("MZ2")) {
    ask("""Je prirodzených čísel menej než celých, teda platí §|\mathbb{N}|<|\mathbb{Z}|§?""", """Áno, §\mathbb{N}\subsetneq\mathbb{Z}§""", "Nie, je ich rovnako", correct = 1,
        after = """Rovnako, §0,1,-1,2,-2,\dots§ očísluje celé čísla prirodzenými (Důsledek 5.1).""", sub = Guess,
        wrong = mapOf(0 to """Pri nekonečných množinách vlastná podmnožina neznamená menšiu mohutnosť. Rozhoduje, či existuje bijekcia."""))
    explore("Prosté zobrazenie zo štyroch do troch?", "Skús nakresliť šípku z každého prvku vľavo tak, aby žiadne dve nešli do toho istého.", ArrowsDraw(listOf("1", "2", "3", "4"), listOf("a", "b", "c"), MapWant.Explore),
        """Nedá sa: štvrtá šípka musí trafiť obsadený prvok (Pozorovanie 5.2).""")
    clip("dml-mz-mohutnost")
    explain("Rovnako veľké = dajú sa spárovať.", """§|A|=|B|§, keď existuje bijekcia §A\to B§; §|A|\leq|B|§, keď existuje prosté zobrazenie. Pri konečných množinách je to obyčajný počet prvkov.""",
        cards = listOf("dml-mz-mohutnost"))
    def("dml-mz-mohutnost")
    thm("dml-mz-t-konecne")
    check("""Existuje prosté zobrazenie §\{1,\dots,5\}\to\{1,\dots,4\}§?""", *YesNo, correct = 1, why = """Neexistuje, väčšia množina sa do menšej bez zlepenia nezmestí (Pozorovanie 5.2).""",
        wrong = mapOf(0 to """Päť šípok do štyroch cieľov: dve skončia v tom istom, a to prosté zobrazenie zakazuje."""))
    explain("Konečné, spočetné, nespočetné", """Konečná sa dá spočítať do nejakého §m§. Spočetná sa dá očíslovať celým §\mathbb{N}§. Nespočetná je väčšia, napr. §\mathbb{R}§.""",
        cards = listOf("dml-mz-konecne"))
    def("dml-mz-konecne")
    thm("dml-mz-t-pocty")
    worked("Existujú také množiny?", """Existují množiny §C§ a §D§ takové, že §|C|+|D|=7§ a současně platí §|C\times D|=16§?""", "Písomka 5. 12. 2025 (variant K), príklad 2b", CardSumPostup) {
        val (numbers, factor, end) = CardSumPostup
        numbers(Reasons.productSize + """ Hľadám teda čísla so súčtom §7§ a súčinom §16§.""", eq("""c+d=7,\qquad c\cdot d=16"""))
        factor("""Rozložím §16§ na súčin všetkými spôsobmi a pozriem sa na súčty.""", factorPairLines(16))
        kotlin.check(factorPairs(16).none { it.first + it.second == 7 }) { "the worked example says no pair sums to 7" }
        end("""Žiadny rozklad nemá súčet §7§ a nekonečná množina by mala nekonečný súčin. Také množiny **neexistujú**.""")
    }
    check("""§C\subseteq D§ a §|C\times D|=36§. Môže byť §|D\setminus C|=5§?""", *YesNo, correct = 0, why = """Môže, pre §|C|=4§, §|D|=9§: §4\cdot 9=36§ a §9-4=5§.""",
        wrong = mapOf(1 to """Hodnota §5§ sa dá dosiahnuť. Pri §|C|=4§, §|D|=9§ a §C\subseteq D§ je §|D\setminus C|=9-4=5§."""))
    check("""Môže mať §\mathcal{P}(A)§ práve §7§ prvkov?""", *YesNo, correct = 1, why = """Nemôže, §|\mathcal{P}(A)|=2^{|A|}§ a §7§ nie je mocnina dvojky.""",
        wrong = mapOf(0 to """Počet podmnožín je vždy §2^n§: §1,2,4,8,\dots§, a §7§ medzi nimi nie je. Každý ďalší prvok počet podmnožín zdvojnásobí, lebo každá podmnožina ho buď obsahuje, alebo nie."""))
    thm("dml-mz-t-srovn")
    check("""Platí §|\mathbb{N}\times\mathbb{N}|=|\mathbb{N}|§?""", *YesNo, correct = 0, why = """Platí, dvojice sa dajú očíslovať (Důsledek 5.1).""",
        wrong = mapOf(1 to """„Nekonečno krát nekonečno“ nie je väčšia mohutnosť. Prosté §(k,\ell)\mapsto p_k^{\ell}§, kde §p_k§ je §k§-te prvočíslo, dá §|\mathbb{N}\times\mathbb{N}|\leq|\mathbb{N}|§; prosté §n\mapsto(n,1)§ dá opačnú nerovnosť a z oboch je rovnosť (Věta 5.7, bod 5)."""))
    check("""Je §\mathbb{R}§ spočetná?""", *YesNo, correct = 1, why = """Nie je, §\mathbb{R}§ je nespočetná (Tvrzení 5.6).""",
        wrong = mapOf(0 to """Na rozdiel od §\mathbb{Q}§ sa reálne čísla očíslovať nedajú. Ku každému zoznamu §r_1,r_2,\dots§ čísel z §(0,1)§ zostrojí diagonála číslo, ktoré sa od §r_k§ líši na §k§-tej cifre, takže v zozname chýba (Tvrzení 5.6)."""))
    practice("dml-mz-velkosti", solo = false)
    practice("dml-mz-velkosti", solo = true)
}

val SetsLessons = listOf(lMnoziny, lIdentity, lZobrazenia, lInjSurj, lSkladanie, lMohutnost)

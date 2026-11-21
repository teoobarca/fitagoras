package dev.hamper.tabula.data

/** Content is written with § for the math delimiter, so raw strings need no escaping. */
fun m(t: String) = t.trimIndent().replace('§', '$').replace("\n", " ")

private fun p(t: String) = Para(m(t))
private fun i(t: String) = Item(m(t))
private fun b(t: String) = Block(t.trimIndent())
private fun w(part: String, because: String) = Why(m(part), m(because))
private fun j(tex: String, yes: Boolean, because: String, wrong: String) = Judge(m(tex), yes, m(because), m(wrong))

/*
 * Every definition of kapitola 2 "Základní pojmy lineární algebry" (chap-03.woo), Czech word for word, the
 * numbering as in the rendered textbook (one counter per chapter, Otázky are not numbered).
 */
val La1Concepts: List<Concept> = listOf(
    Concept(
        id = "tn", name = "Vektorový priestor §T^n§", src = "Skriptá, Definice 2.2",
        czech = listOf(
            p("""Nechť §T§ je libovolné těleso a §n\in\mathbb{N}§. Množinu §n§-tic"""),
            b("""T^n=\big\{(x_1,\dots,x_n)\mid\softbreak[1] x_i\in T\ \softbreak[2]\text{pro každé}\ i\in\hat{n}\big\}"""),
            p("""spolu s operacemi **sčítání vektorů** §+§, **násobení vektoru skalárem** §\cdot§ definovaných po složkách takto: pro každé §\alpha\in T§ a pro každé §\mathbf{x},\mathbf{y}\in T^n§ položíme"""),
            b("""\mathbf{x}+\mathbf{y}:=\softbreak[2](x_1+y_1,\dots,x_n+y_n),\quad\softbreak \alpha\cdot\mathbf{x}:=(\alpha x_1,\dots,\alpha x_n)"""),
            p("""nazýváme **vektorovým prostorem §T^n§**."""),
        ),
        why = listOf(
            w("""libovolné těleso §T§""", """Čísla musia vedieť sčítať, násobiť a deliť nenulovým. Preto §\mathbb{Q},\mathbb{R},\mathbb{C}§ alebo §\mathbb{Z}_p§ pre prvočíslo §p§. V §\mathbb{Z}_p§ sa počíta modulo §p§."""),
            w("""množina §n§-tic""", """Vektor je §n§-tica čísel z §T§, nič viac. Šípka v rovine je len obrázok pre §n=2§."""),
            w("""definovaných po složkách""", """Sčítaš prvú s prvou, druhú s druhou. Skalár násobí každú zložku. Iné operácie (vektor krát vektor, skalár plus vektor) tu definované nie sú."""),
        ),
        judge = listOf(
            j("""§(1,2,0,4)+(3,3,0,1)=(4,0,0,0)§ v §\mathbb{Z}_5^4§""", true, """V §\mathbb{Z}_5§ je §2+3=0§ a §4+1=0§, lebo §5\equiv 0\pmod{5}§.""", """Nuly nie sú chyba, v §\mathbb{Z}_5§ je §2+3=0§. Číslo §5§ nie je prvok §\mathbb{Z}_5=\{0,\dots,4\}§, jeho zvyšok po delení §5§ je §0§. Presne tak počíta Definícia 2.2 nad §T=\mathbb{Z}_5§."""),
            j("""§2\cdot(3,4)=(6,8)§ v §\mathbb{Z}_5^2§""", false, """V §\mathbb{Z}_5§ nie sú čísla §6,8§. Správne §(1,3)§.""", """§(6,8)§ je výsledok v §\mathbb{Z}^2§, nie v §\mathbb{Z}_5^2§. Zložky vektora z §\mathbb{Z}_5^2§ musia ležať v §\{0,\dots,4\}§."""),
            j("""V §T^2§: §3+(1,2)§ je vektor §(4,5)§""", false, """Skalár s vektorom sčítať nevieme, to nie je definované (Varovanie 2.5).""", """Pripočítať §3§ ku každej zložke vyzerá prirodzene, ale taká operácia nie je definovaná. §+§ v Definícii 2.2 sčítava len dva vektory z §T^n§."""),
            j("""V §T^2§: §(1,2)\cdot(3,4)§ je §(3,8)§""", false, """Násobenie vektora vektorom nie je definované.""", """Násobenie po zložkách vyzerá prirodzene, ale súčin dvoch vektorov nie je definovaný. Definícia 2.2 pozná len §\alpha\cdot\mathbf{x}§ so skalárom §\alpha\in T§."""),
        ),
        units = listOf("Teleso §T§ a §n\\in\\mathbb{N}§", "Množina §n§-tíc čísel z §T§", "Sčítanie po zložkách", "Násobenie skalárom po zložkách"),
        ask = "Napište definici vektorového prostoru §T^n§.",
        question = "Je to správny výpočet?",
        short = "vektorového priestoru",
    ),
    Concept(
        id = "podpriestor", name = "Podpriestor", src = "Skriptá, Definice 2.7",
        czech = listOf(
            p("""Nechť §P§ je podmnožina §T^n§. Řekneme, že §P§ je **podprostor** vektorového prostoru §T^n§, pokud platí:"""),
            i("""množina §P§ je neprázdná, tzn. §P\neq\emptyset§,"""),
            i("""množina §P§ je uzavřená vůči sčítání vektorů v ní, tzn. §(\forall\mathbf{x},\mathbf{y}\in P)(\mathbf{x}+\mathbf{y}\in P)§,"""),
            i("""množina §P§ je uzavřená vůči násobení vektorů v ní ==libovolným== skalárem, tzn. §(\forall\alpha\in T)(\forall\mathbf{x}\in P)(\alpha\mathbf{x}\in P)§."""),
            p("""Vztah „být podprostorem“ pak značíme §P\ssub T^n§."""),
        ),
        why = listOf(
            w("""§P§ je podmnožina §T^n§""", """Podpriestor je vždy kus nejakého §T^n§, nie samostatný priestor. Preto §\mathbb{R}^1§ nie je podpriestor §\mathbb{R}^2§ (Varovanie 2.16)."""),
            w("""neprázdná""", """Bez tejto podmienky by prázdna množina splnila obe uzavretosti „zadarmo“, lebo v nej nie je čo sčítať."""),
            w("""uzavřená vůči sčítání: §(\forall\mathbf{x},\mathbf{y}\in P)§""", """Pre **všetky** dvojice z §P§. Jeden príklad nestačí, na vyvrátenie stačí jedna dvojica, ktorá vyskočí von."""),
            w("""uzavřená vůči násobení libovolným skalárem""", """Aj záporným a aj nulou. Z toho plynie, že §0\cdot\mathbf{x}=\theta\in P§: nulový vektor v definícii netreba, je to dôsledok (Pozorovanie 2.8)."""),
        ),
        judge = listOf(
            j("""Priamka §\{(x,y)\in\mathbb{R}^2\mid x+2y=0\}§""", true, """Prechádza počiatkom, súčet aj násobok bodov priamky na nej ostanú (Príklad 2.13).""", """Priamka cez počiatok je podpriestor, spĺňa všetky tri podmienky. Z §x_1+2y_1=0§ a §x_2+2y_2=0§ plynie §(x_1+x_2)+2(y_1+y_2)=0§ aj §\alpha x_1+2\alpha y_1=0§ (Definícia 2.7)."""),
            j("""Priamka §\{(x,y)\in\mathbb{R}^2\mid x+2y=2\}§""", false, """Neobsahuje §(0,0)§, a každý podpriestor obsahuje §\theta§ (Pozorovanie 2.8).""", """Je to priamka, ale neprechádza počiatkom: §0+2\cdot 0=0\neq 2§. Podpriestorom je len priamka cez §\theta§, nie hocijaká priamka."""),
            j("""§\{(x,y)\in\mathbb{R}^2\mid |x|=|y|\}§""", false, """Obsahuje §\theta§, ale §(1,1)+(1,-1)=(2,0)§ v nej nie je (Príklad 2.12).""", """Obsahovať §\theta§ je nutné, nie dostatočné. Uzavretosť na sčítanie musí platiť pre všetky dvojice, a tu jedna dvojica vypadne."""),
            j("""V §T^2§: Zjednotenie osí §x§ a §y§""", false, """§(1,0)+(0,1)=(1,1)§ vyskočí von (Príklad 2.17).""", """Každá os je podpriestor, ale ich zjednotenie nie. Súčet nenulových bodov z rôznych osí neleží na žiadnej osi; zjednotenie podpriestorov podpriestorom byť nemusí (Veta 2.18)."""),
            j("""§\{\theta\}§""", true, """Neprázdna a §\theta+\theta=\theta§, §\alpha\theta=\theta§. Triviálny podpriestor.""", """§\{\theta\}§ je neprázdna a uzavretá na obe operácie, nič viac Definícia 2.7 nechce. Je to triviálny podpriestor (Pozorovanie 2.8)."""),
            j("""Množina riešení homogénnej sústavy""", true, """Obsahuje nulu, súčet aj násobok riešení je riešenie (MARAST, ukážková písomka 292).""", """Podpriestor nedáva len nehomogénna sústava. Pri homogénnej je pravá strana nulová, a tak §\theta§ je riešenie a súčet aj násobok riešení sú zase riešenia."""),
            j("""§\{(x,y)\in\mathbb{R}^2\mid x\geq 0,\ y\geq 0\}§""", false, """§(1,1)§ v nej je, ale §(-1)\cdot(1,1)§ nie.""", """Na sčítanie je uzavretá, aj na násobenie nezáporným číslom. Definícia 2.7 však chce §(\forall\alpha\in T)§, teda aj §\alpha=-1§."""),
        ),
        units = listOf("§P§ je podmnožina §T^n§", "Neprázdna", "Uzavretá na sčítanie, pre všetky §\\mathbf{x},\\mathbf{y}\\in P§", "Uzavretá na násobenie každým skalárom"),
        extra = listOf("Značenie §P\\ssub T^n§"),
        trap = Trap("Podpriestor je množina, ktorá obsahuje nulový vektor.", "Je to nutná podmienka, nie definícia: §\\{(x,y)\\mid |x|=|y|\\}§ nulu obsahuje a podpriestor nie je. Klouda ju v definícii nechce (4. prednáška).", "Pozorovanie 2.8, Poznámka 2.10"),
        ask = "Napište definici podprostoru.",
        question = "Je to podpriestor?",
        short = "podpriestoru",
    ),
    Concept(
        id = "trivialny", name = "Triviálny a vlastný podpriestor", src = "Skriptá, Definice 2.9",
        czech = listOf(
            p("""Podprostory §\{\theta\}§ a §T^n§ vektorového prostoru §T^n§ nazýváme **triviálními podprostory**. Každý podprostor §P\ssub T^n§, pro který současně platí §P\neq T^n§, nazýváme **vlastním podprostorem**."""),
        ),
        why = listOf(
            w("""§\{\theta\}§ a §T^n§""", """Dva podpriestory, ktoré má každé §T^n§. Netriviálne sú tie medzi nimi, v §\mathbb{R}^3§ napríklad priamky a roviny cez počiatok."""),
            w("""§P\neq T^n§""", """Vlastný znamená menší než celý priestor. Pozor: §\{\theta\}§ je vlastný, a pritom triviálny."""),
        ),
        judge = listOf(
            j("""§\{\theta\}\ssub\mathbb{R}^3§ je vlastný""", true, """Nie je celé §\mathbb{R}^3§.""", """„Vlastný“ znamená len, že to nie je celý priestor (Definícia 2.9). §\{\theta\}\neq\mathbb{R}^3§, teda vlastný je, hoci je zároveň triviálny."""),
            j("""§\{\theta\}\ssub\mathbb{R}^3§ je netriviálny""", false, """Je triviálny, hoci vlastný.""", """Triviálne podpriestory sú podľa Definície 2.9 dva, §\{\theta\}§ a §T^n§. To, že je §\{\theta\}§ zároveň vlastný, na tom nič nemení."""),
            j("""Rovina §2x+y-z=0§ v §\mathbb{R}^3§ je netriviálny vlastný podpriestor""", true, """Prechádza počiatkom, nie je §\{\theta\}§ ani §\mathbb{R}^3§.""", """Homogénna rovnica dá podpriestor; obsahuje §(1,0,2)\neq\theta§ a neobsahuje §(1,0,0)§. Nie je teda ani §\{\theta\}§, ani §\mathbb{R}^3§."""),
        ),
        units = listOf("Triviálne: §\\{\\theta\\}§ a §T^n§", "Vlastný: §P\\neq T^n§"),
        ask = "Napište, co je triviální a co vlastní podprostor.",
        question = "Je ten podpriestor pomenovaný správne?",
        short = "triviálneho a vlastného podpriestoru",
    ),
    Concept(
        id = "lk", name = "Lineárna kombinácia", src = "Skriptá, Definice 2.21",
        czech = listOf(
            p("""Nechť §\mathbf{x}\in T^n§ a §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ je soubor vektorů z §T^n§. Říkáme, že vektor §\mathbf{x}§ je **lineární kombinací** souboru §(\mathbf{x}_1,\dots,\mathbf{x}_m)§, právě když existují čísla §\alpha_1,\dots,\alpha_m\in T§ taková, že"""),
            b("""\mathbf{x}=\sum_{i=1}^m\alpha_i\mathbf{x}_i."""),
            p("""Čísla §\alpha_i§, §i\in\hat{m}§, nazýváme **koeficienty lineární kombinace**. Jestliže jsou ==všechny== koeficienty nulové, tj. §(\forall i\in\hat{m})(\alpha_i=0)§, nazýváme takovou lineární kombinaci **triviální**. V opačném případě jde o **lineární kombinaci netriviální**."""),
        ),
        why = listOf(
            w("""soubor §(\mathbf{x}_1,\dots,\mathbf{x}_m)§""", """Súbor je §m§-tica vektorov: záleží na poradí a vektor sa môže opakovať. Preto „súbor“, nie množina."""),
            w("""existují čísla §\alpha_1,\dots,\alpha_m\in T§""", """Stačí jedna vhodná voľba koeficientov. Koeficienty sú z telesa §T§, v §\mathbb{Z}_5§ teda len §0,\dots,4§."""),
            w("""triviální: všechny koeficienty nulové""", """Tá dá vždy §\theta§. Netriviálna znamená, že **aspoň jeden** koeficient je nenulový, nie všetky."""),
        ),
        judge = listOf(
            j("""V §\mathbb{R}^2§: §(3,4)§ je LK súboru §((1,1),(1,2))§""", true, """§2(1,1)+1(1,2)=(3,4)§.""", """Koeficienty nemusia byť len §0§ a §1§: hľadáš ľubovoľné §\alpha,\beta\in\mathbb{R}§, a §\alpha=2§, §\beta=1§ vyhovujú."""),
            j("""§(3,1)\in\mathbb{R}^2§ je LK súboru §((1,2),(2,4))§""", false, """Všetky kombinácie ležia na priamke §y=2x§, §(3,1)§ nie.""", """§(2,4)=2(1,2)§, takže každá kombinácia je násobok §(1,2)§: druhá zložka je dvojnásobok prvej. §(3,1)§ taký nie je."""),
            j("""Kombinácia §0\mathbf{x}_1+5\mathbf{x}_2§ nad §\mathbb{R}§ je netriviálna""", true, """Stačí jeden nenulový koeficient.""", """Netriviálna znamená, že **aspoň jeden** koeficient je nenulový, nie všetky. Tu §5\neq 0§."""),
            j("""§\theta§ je LK každého súboru""", true, """Triviálna kombinácia, všetky koeficienty nula.""", """Koeficienty smú byť nulové: §0\mathbf{x}_1+\dots+0\mathbf{x}_m=\theta§ je lineárna kombinácia pre každý súbor, aj LN."""),
        ),
        units = listOf("Vektor §\\mathbf{x}§ a súbor §(\\mathbf{x}_1,\\dots,\\mathbf{x}_m)§ z §T^n§", "Existujú §\\alpha_1,\\dots,\\alpha_m\\in T§", "§\\mathbf{x}=\\sum\\alpha_i\\mathbf{x}_i§", "Triviálna: všetky koeficienty nulové"),
        ask = "Napište definici lineární kombinace a triviální lineární kombinace.",
        question = "Platí to o lineárnej kombinácii?",
        short = "lineárnej kombinácie",
    ),
    Concept(
        id = "ln", name = "Lineárna nezávislosť a závislosť", src = "Skriptá, Definice 2.23",
        czech = listOf(
            p("""Nechť §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ je soubor vektorů z §T^n§. Řekneme, že soubor §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ je **lineárně nezávislý (LN)**, právě když ==pouze== triviální lineární kombinace tohoto souboru je rovna nulovému vektoru §\theta§. V opačném případě nazýváme soubor **lineárně závislým (LZ)**."""),
            p("""Jinými slovy:"""),
            b("""\text{LN}\Leftrightarrow\softbreak[1](\forall\alpha_1,\dots,\alpha_m\in T)\softbreak\Big(\textstyle\sum_{i=1}^m\alpha_i\mathbf{x}_i=\theta\softbreak[1]\Rightarrow(\forall i\in\hat{m})\softbreak[2](\alpha_i=0)\Big)"""),
            b("""\text{LZ}\Leftrightarrow\softbreak[1](\exists\alpha_1,\dots,\alpha_m\in T)\softbreak(\exists k\in\hat{m})\softbreak\Big(\alpha_k\neq 0\softbreak[1]\wedge\textstyle\sum_{i=1}^m\alpha_i\mathbf{x}_i=\theta\Big)"""),
        ),
        why = listOf(
            w("""**pouze** triviální lineární kombinace""", """Triviálna dá §\theta§ vždy, to nič nehovorí. LN je, keď **žiadna iná** §\theta§ nedá."""),
            w("""§(\forall\alpha_1,\dots,\alpha_m)(\dots\Rightarrow\dots)§""", """Kvantifikátor je pred implikáciou: pre každú voľbu koeficientov, ktorá dá §\theta§, musia byť všetky nulové. Koeficienty nie sú vopred dané, vyberáš ich v kvantifikátore (Discord #la1, 6. 2. 2024)."""),
            w("""LZ: existuje §k§ s §\alpha_k\neq 0§""", """Stačí jeden nenulový koeficient, nie všetky. Presná negácia LN."""),
            w("""soubor vektorů""", """LN je vlastnosť **súboru**. Veta „vektor §\mathbf{u}§ je nezávislý na §\mathbf{v}§“ je zle (Klouda, 4. prednáška)."""),
        ),
        judge = listOf(
            j("""V §\mathbb{R}^2§: §((1,2),(2,4))§ je LZ""", true, """§2(1,2)-1(2,4)=\theta§ je netriviálna.""", """Na LZ stačí jedna netriviálna kombinácia rovná §\theta§, a tá tu je, lebo §(2,4)=2(1,2)§."""),
            j("""V §\mathbb{R}^2§: §((1,0),(0,1))§ je LZ""", false, """§\alpha(1,0)+\beta(0,1)=(\alpha,\beta)=\theta§ len pre §\alpha=\beta=0§.""", """Kombinácia §\alpha(1,0)+\beta(0,1)§ je vektor §(\alpha,\beta)§, a ten je §\theta§ len pre §\alpha=\beta=0§. Netriviálna kombinácia rovná §\theta§ neexistuje."""),
            j("""Súbor s nulovým vektorom je LZ""", true, """§1\cdot\theta+0\cdot\dots=\theta§ je netriviálna (Pozorovanie 2.25).""", """Koeficient pri §\theta§ môže byť nenulový a súčet ostane §\theta§. Preto je každý súbor s §\theta§ LZ, nech sú ostatné vektory akékoľvek."""),
            j("""§((1,2,3),(4,2,3),(3,4,2))§ v §\mathbb{Z}_5^3§ je LN""", true, """GEM dá jediné riešenie §(0,0,0)§ (Príklad 2.28).""", """Súbor je LN: GEM nad §\mathbb{Z}_5§ dá tri hlavné stĺpce, teda len triviálne riešenie. Netriviálna kombinácia rovná §\theta§ tu neexistuje (Príklad 2.28)."""),
            j("""V §\mathbb{R}^2§: Súbor dvoch rôznych nenulových vektorov je vždy LN""", false, """§((1,2),(2,4))§: druhý je násobkom prvého.""", """Rôzne a nenulové ešte neznamená LN. Súbor dvoch vektorov je LZ práve vtedy, keď je jeden násobkom druhého (Pozorovanie 2.25)."""),
        ),
        units = listOf("Súbor §(\\mathbf{x}_1,\\dots,\\mathbf{x}_m)§ z §T^n§", "LN: **len** triviálna kombinácia dá §\\theta§", "Kvantifikátor: pre všetky §\\alpha_i§ s §\\sum\\alpha_i\\mathbf{x}_i=\\theta§", "LZ: v opačnom prípade, existuje netriviálna rovná §\\theta§"),
        trap = Trap("Súbor je LZ, keď je jeden vektor lineárnou kombináciou ostatných.", "To je Veta 2.39 a platí len pre §m\\geq 2§; pre súbor jedného vektora nedáva zmysel. Kurz chce Definíciu 2.23.", "Veta 2.39, text pod Definíciou 2.23"),
        ask = "Napište definici lineárně nezávislého a lineárně závislého souboru.",
        question = "Platí to o lineárnej závislosti?",
        short = "LN a LZ",
    ),
    Concept(
        id = "obal", name = "Lineárny obal", src = "Skriptá, Definice 2.33",
        czech = listOf(
            p("""Buď §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ soubor vektorů z §T^n§. Množinu všech lineárních kombinací tohoto souboru nazveme **lineárním obalem souboru** §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ a značíme ji §\langle\mathbf{x}_1,\dots,\mathbf{x}_m\rangle§. Neboli"""),
            b("""\langle\mathbf{x}_1,\dots,\mathbf{x}_m\rangle=\softbreak[1]\Big\{\textstyle\sum_{i=1}^m\alpha_i\mathbf{x}_i\ \Big|\softbreak[1]\ \alpha_i\in T\ \softbreak[2]\text{pro každé}\ i\in\hat{m}\Big\}."""),
        ),
        why = listOf(
            w("""množinu **všech** lineárních kombinací""", """Obal je množina, nie jeden vektor. Patrí doň každá kombinácia, aj triviálna (teda §\theta§) aj samotné §\mathbf{x}_i§."""),
            w("""§\alpha_i\in T§""", """Nad §\mathbb{R}§ je obal nekonečný (ak súbor obsahuje nenulový vektor), nad §\mathbb{Z}_p§ konečný: obal jedného nenulového vektora v §\mathbb{Z}_5^2§ má 5 prvkov (Príklad 2.37)."""),
            w("""soubor vektorů""", """Obal je **nástroj, ako zo súboru vyrobiť množinu**; podpriestor je **vlastnosť množiny** (vyučujúci LA1, Discord #la1, 25. 10. 2026)."""),
        ),
        judge = listOf(
            j("""§\langle(1,2)\rangle§ v §\mathbb{R}^2§ je priamka §y=2x§""", true, """Všetky násobky §\alpha(1,2)=(\alpha,2\alpha)§.""", """Obal §(1,2)§ sú všetky jeho násobky §(\alpha,2\alpha)§, a to je presne priamka §y=2x§."""),
            j("""§\langle(1,0),(0,1)\rangle=\mathbb{R}^2§""", true, """§(a,b)=a(1,0)+b(0,1)§.""", """Obal nie sú len dve osi: každý §(a,b)\in\mathbb{R}^2§ je kombinácia §a(1,0)+b(0,1)§."""),
            j("""§\langle(1,1,0,0),(0,1,1,0)\rangle\ssub\mathbb{Z}_2^4§ má 4 prvky""", true, """§\alpha,\beta\in\{0,1\}§: §\theta,(1,1,0,0),(0,1,1,0),(1,0,1,0)§ (test 22. 11. 2023, myš).""", """Nad §\mathbb{Z}_2§ je obal konečný, každý koeficient má len §2§ možnosti. Kombinácií je §2\cdot 2=4§ a pre LN súbor sú všetky rôzne."""),
            j("""V §\mathbb{R}^2§: §(1,2)\notin\langle(1,2),(2,4)\rangle§""", false, """Každý vektor súboru leží vo svojom obale (Pozorovanie 2.34).""", """Že je súbor LZ, nič nemení: §(1,2)=1(1,2)+0(2,4)§ je kombinácia súboru, teda v obale je."""),
        ),
        units = listOf("Súbor §(\\mathbf{x}_1,\\dots,\\mathbf{x}_m)§ z §T^n§", "Množina **všetkých** lineárnych kombinácií", "Koeficienty §\\alpha_i\\in T§"),
        extra = listOf("Značenie §\\langle\\mathbf{x}_1,\\dots,\\mathbf{x}_m\\rangle§"),
        ask = "Napište definici lineárního obalu souboru vektorů.",
        question = "Platí to o lineárnom obale?",
        short = "lineárneho obalu",
    ),
    Concept(
        id = "generuje", name = "Generovanie podpriestoru", src = "Skriptá, Definice 2.43",
        czech = listOf(
            p("""O souboru vektorů §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ z §T^n§ řekneme, že **generuje** podprostor §P\ssub T^n§, právě když platí: §§\langle\mathbf{x}_1,\dots,\mathbf{x}_m\rangle=P§§. V případě, že §P=T^n§, můžeme zjednodušeně říkat §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ generuje (vektorový) prostor §T^n§."""),
        ),
        why = listOf(
            w("""§\langle\dots\rangle=P§, rovnosť""", """Nestačí, že obal je v §P§. Musí to byť **celé** §P§: každý vektor z §P§ je kombináciou súboru."""),
            w("""z toho §\mathbf{x}_i\in P§""", """Keď súbor generuje §P§, jeho vektory v §P§ ležia (Pozorovanie 2.34, bod 2)."""),
        ),
        judge = listOf(
            j("""§((1,0),(0,1),(1,1))§ generuje §\mathbb{R}^2§""", true, """Už prvé dva generujú, tretí ničomu nevadí.""", """Generovanie LN nevyžaduje: vektor navyše obal nezmenší. Už §(1,0)§ a §(0,1)§ dajú každé §(a,b)§."""),
            j("""§((1,2),(2,4))§ generuje §\mathbb{R}^2§""", false, """Obal je len priamka §y=2x§.""", """Dva vektory nestačia automaticky: §(2,4)=2(1,2)§, a tak je obal len priamka. Napríklad §(1,0)§ z nich nevyrobíš."""),
            j("""Súbor generuje §T^n§, keď sústava s jeho vektormi v stĺpcoch má riešenie pre každú pravú stranu""", true, """Presne to overuje Postup 2.45: na pravú stranu dáš premenné.""", """Je to presne definícia generovania pre §P=T^n§. Každý vektor §T^n§ je kombináciou súboru, teda sústava má riešenie pre každú pravú stranu (Definícia 2.43)."""),
        ),
        units = listOf("Súbor z §T^n§ a podpriestor §P§", "Rovnosť §\\langle\\mathbf{x}_1,\\dots,\\mathbf{x}_m\\rangle=P§"),
        ask = "Napište, co znamená, že soubor generuje podprostor §P§.",
        question = "Platí to o generovaní?",
        short = "generujúceho súboru",
    ),
    Concept(
        id = "baza", name = "Báza", src = "Skriptá, Definice 2.44",
        czech = listOf(
            p("""Mějme §P\ssub T^n§. Existuje-li v §T^n§ soubor vektorů §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ takový, že"""),
            i("""§(\mathbf{x}_1,\dots,\mathbf{x}_m)§ je LN,"""),
            i("""§(\mathbf{x}_1,\dots,\mathbf{x}_m)§ generuje §P§,"""),
            p("""nazýváme soubor vektorů §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ **bází** podprostoru §P§. Jako dříve: v případě, že §P=T^n§, můžeme zjednodušeně říkat, že §(\mathbf{x}_1,\dots,\mathbf{x}_m)§ je bází (vektorového) prostoru §T^n§."""),
        ),
        why = listOf(
            w("""je LN""", """Báza nie je zbytočne veľká: nič sa z nej nedá vynechať bez zmenšenia obalu, a súradnice sú preto jednoznačné (Veta 2.66)."""),
            w("""generuje §P§""", """Báza je dosť veľká: každý vektor z §P§ sa z nej dá nakombinovať."""),
            w("""V §T^2§: soubor""", """Báza je súbor, záleží na poradí: §((1,0),(0,1))§ a §((0,1),(1,0))§ sú dve rôzne bázy a dávajú iné súradnice."""),
        ),
        judge = listOf(
            j("""§((1,0),(0,1),(1,1))§ je báza §\mathbb{R}^2§""", false, """Generuje, ale je LZ: tretí je súčet prvých dvoch.""", """Generovanie je len polovica, báza musí byť aj LN. Tri vektory v §\mathbb{R}^2§ LN nie sú (Definícia 2.44, Pozorovanie 2.32)."""),
            j("""V §\mathbb{R}^2§: §((1,2),(2,4))§ je báza §\langle(1,2)\rangle§""", false, """Generuje priamku, ale je LZ.""", """Generovanie nestačí, báza musí byť aj LN. Tu je §2(1,2)-(2,4)=\theta§ netriviálna kombinácia (Definícia 2.44)."""),
            j("""§((1,1),(1,-1))§ je báza §\mathbb{R}^2§""", true, """Dva LN vektory v priestore dimenzie 2 (Veta 2.55).""", """Je to báza, lebo ani jeden vektor nie je násobkom druhého. Súbor je teda LN a dva LN vektory v priestore dimenzie §2§ sú báza (Veta 2.55)."""),
            j("""§\{\theta\}§ má bázu""", false, """V §\{\theta\}§ nie je žiadny LN súbor (Veta 2.53).""", """Každý súbor z §\{\theta\}§ obsahuje len §\theta§, a súbor s §\theta§ je LZ. Báza musí byť LN, preto §\{\theta\}§ bázu nemá."""),
        ),
        units = listOf("Podpriestor §P\\ssub T^n§", "Súbor je LN", "Súbor generuje §P§", "Obe podmienky naraz"),
        ask = "Napište definici báze podprostoru.",
        question = "Platí to o báze?",
        short = "bázy",
    ),
    Concept(
        id = "standardna", name = "Štandardná báza", src = "Skriptá, Definice 2.48",
        czech = listOf(
            p("""Ve vektorovém prostoru §T^n§ označme"""),
            b("""\begin{aligned}\mathbf{e}_1&:=(1,0,\dots,0),\\\mathbf{e}_2&:=(0,1,0,\dots,0),\\&\ \ \vdots\\\mathbf{e}_n&:=(0,\dots,0,1).\end{aligned}"""),
            p("""Potom soubor §\mathcal{E}_n=(\mathbf{e}_1,\mathbf{e}_2,\dots,\mathbf{e}_n)§ je báze §T^n§. Soubor §\mathcal{E}_n§ nazýváme **standardní bází** (známý taky jako kanonická báze). U jejích prvků se můžeme často setkat s označením jednotkové vektory."""),
        ),
        why = listOf(
            w("""§\mathbf{e}_i§ má jednotku na §i§-tom mieste""", """Súradnice vektora v štandardnej báze sú jeho zložky: §(a,b,c)=a\mathbf{e}_1+b\mathbf{e}_2+c\mathbf{e}_3§ (Príklad 2.70)."""),
            w("""je báze""", """LN: kombinácia §\sum\alpha_i\mathbf{e}_i=(\alpha_1,\dots,\alpha_n)§ je §\theta§ len pre samé nuly. Generuje: každý vektor sa tak dá zapísať."""),
        ),
        judge = listOf(
            j("""§(\mathbf{x})_{\mathcal{E}_3}=\mathbf{x}§ pre každé §\mathbf{x}\in\mathbb{R}^3§""", true, """V štandardnej báze sú súradnice vektor sám (Klouda, 5. prednáška).""", """V štandardnej báze je §\mathbf{x}=x_1\mathbf{e}_1+x_2\mathbf{e}_2+x_3\mathbf{e}_3§, takže súradnice sú zložky. Odlišné sú až v inej báze."""),
            j("""§((1,1),(0,1))§ je štandardná báza §\mathbb{R}^2§""", false, """Je to báza, ale štandardná je §((1,0),(0,1))§.""", """Báza to je, ale štandardná nie, lebo §(1,1)§ nie je žiadne §\mathbf{e}_i§. Štandardná báza je len jedna: §\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly (Definícia 2.48)."""),
        ),
        units = listOf("§\\mathbf{e}_i§: jednotka na §i§-tom mieste, inde nuly", "§\\mathcal{E}_n=(\\mathbf{e}_1,\\dots,\\mathbf{e}_n)§ je báza §T^n§"),
        ask = "Napište, co je standardní báze §T^n§.",
        question = "Platí to o štandardnej báze?",
        short = "štandardnej bázy",
    ),
    Concept(
        id = "dimenzia", name = "Dimenzia podpriestoru", src = "Skriptá, Definice 2.49",
        czech = listOf(
            p("""Buď §P§ podprostor §T^n§. Řekneme, že **dimenze podprostoru** §P§ je rovna"""),
            i("""§0§, pokud neexistuje LN soubor vektorů z §P§ délky §1§."""),
            i("""§d\in\mathbb{N}§, pokud ==existuje== LN soubor vektorů z §P§ délky §d§, ale ==každý== soubor vektorů z §P§ délky §d+1§ už je LZ."""),
            p("""Dimenzi podprostoru §P§ označujeme symbolem §\dim P§."""),
        ),
        why = listOf(
            w("""neexistuje LN soubor délky §1§""", """Jednoprvkový súbor §(\mathbf{x})§ je LZ len pre §\mathbf{x}=\theta§. Dimenziu §0§ má teda práve §\{\theta\}§ (Veta 2.53)."""),
            w("""**existuje** LN soubor délky §d§""", """Do §P§ sa zmestí §d§ nezávislých smerov."""),
            w("""ale **každý** soubor délky §d+1§ je LZ""", """A viac sa nezmestí. Obe časti spolu hovoria: §d§ je najväčšia dĺžka LN súboru v §P§."""),
            w("""prečo nie „počet prvkov bázy“""", """Táto definícia potrebuje len LN. Veta 2.57 je jej dôsledok, nie definícia, a na teste ju ako definíciu neuznajú (MARAST 292)."""),
        ),
        judge = listOf(
            j("""Nad §\mathbb{R}§: §\dim\langle(1,2),(2,4)\rangle=1§""", true, """§((1,2))§ je LN, každé dva vektory z priamky sú LZ.""", """Obal má síce dva generátory, ale sú LZ: §(2,4)=2(1,2)§. Dimenzia je dĺžka najdlhšieho LN súboru, nie počet daných vektorov."""),
            j("""§\dim\mathbb{Z}_5^3=3§""", true, """§\mathcal{E}_3§ je LN a každý súbor 4 vektorov v §\mathbb{Z}_5^3§ je LZ (Pozorovanie 2.32 a Pozorovanie 2.52).""", """Počet prvkov §5^3=125§ dimenzia nie je. Aj konečný priestor má dimenziu podľa Definície 2.49, teda podľa dĺžky LN súborov."""),
            j("""§\dim\{\theta\}=1§""", false, """Jediný jednoprvkový súbor je §(\theta)§ a je LZ. Dimenzia je §0§.""", """Dimenzia nie je počet prvkov, ale dĺžka najdlhšieho LN súboru. §\{\theta\}§ má síce jeden prvok, no LN súbor v ňom nie je žiadny."""),
            j("""Ak v §P§ existuje LN súbor dĺžky 3, tak §\dim P=3§""", false, """Len §\dim P\geq 3§. Treba ešte, že každý súbor dĺžky 4 je LZ.""", """Jeden LN súbor dĺžky §3§ dáva len dolnú hranicu (Veta 2.53). Napríklad v §\mathbb{R}^4§ taký súbor existuje, a §\dim\mathbb{R}^4=4§."""),
        ),
        units = listOf("§P§ je podpriestor §T^n§", "§0§: neexistuje LN súbor vektorov z §P§ dĺžky §1§", "§d\\in\\mathbb{N}§: existuje LN súbor vektorov z §P§ dĺžky §d§", "a každý súbor vektorov z §P§ dĺžky §d+1§ je LZ"),
        extra = listOf("Značenie §\\dim P§"),
        trap = Trap("Dimenze je rovna počtu prvků báze.", "Pravdivé tvrdenie (Veta 2.57), nie definícia. Ukážková písomka v MARASTe ju výslovne neuznáva.", "MARAST, lekce 292, príklad 3"),
        ask = "Napište definici dimenze podprostoru.",
        question = "Platí to o dimenzii?",
        short = "dimenzie",
    ),
    Concept(
        id = "suradnice", name = "Súradnice vzhľadom na bázu", src = "Skriptá, Definice 2.68",
        czech = listOf(
            p("""Nechť §\mathcal{B}=(\mathbf{x}_1,\dots,\mathbf{x}_d)§ je báze §P\ssub T^n§ a vektor §\mathbf{v}\in P§ splňuje"""),
            b("""\mathbf{v}=\sum_{i=1}^d\alpha_i\mathbf{x}_i."""),
            p("""**Souřadnicemi vektoru §\mathbf{v}\in P§ vzhledem k bázi §\mathcal{B}§** nazveme uspořádanou §d§-tici"""),
            b("""(\mathbf{v})_{\mathcal{B}}:=(\alpha_1,\dots,\alpha_d)\in T^d."""),
        ),
        why = listOf(
            w("""§\mathcal{B}§ je báze""", """Generuje, takže súradnice existujú. Je LN, takže sú jediné (Veta 2.66). Bez oboch by „súradnice“ neboli dobre definované."""),
            w("""§\mathbf{v}\in P§""", """Vektor mimo §P§ súradnice vzhľadom na bázu podpriestoru §P§ nemá: sústava nemá riešenie (test 14. 11. 2024, medvěd)."""),
            w("""uspořádaná §d§-tice §\in T^d§""", """Súradníc je toľko, koľko prvkov má báza, nie koľko zložiek má vektor. V podpriestore dimenzie 2 v §\mathbb{Z}_5^4§ sú súradnice dvojica."""),
        ),
        judge = listOf(
            j("""V §\mathbb{R}^3§: V báze §((1,0,0),(1,1,0),(0,0,1))§ je §((1,2,3))_{\mathcal{B}}=(-1,2,3)§""", true, """§-1(1,0,0)+2(1,1,0)+3(0,0,1)=(1,2,3)§ (Otázka v skriptách).""", """Súradnice sa nemusia rovnať zložkám ani byť kladné. Rozhoduje dosadenie: §-1(1,0,0)+2(1,1,0)+3(0,0,1)§ naozaj dá §(1,2,3)§."""),
            j("""Pre bázu §\mathcal{B}§ priestoru §\mathbb{Z}_5^4§ môže byť §(\mathbf{x})_{\mathcal{B}}=(1,2,1)§""", false, """Báza má 4 prvky, súradnice sú štvorica (test 20. 11. 2024, slon).""", """Trojica by sedela pre bázu s tromi prvkami. Báza §\mathbb{Z}_5^4§ má §4§ prvky, a súradníc je toľko, koľko prvkov má báza."""),
            j("""V §T^3§: Súradnice §\mathbf{x}+\mathbf{y}§ v báze §(\mathbf{x},\mathbf{y},\mathbf{z})§ sú §(1,1,0)§""", true, """§1\mathbf{x}+1\mathbf{y}+0\mathbf{z}§ (Otázka v skriptách).""", """Súradnice sú koeficienty pri **všetkých** vektoroch bázy v jej poradí. §\mathbf{z}§ v súčte nie je, preto má koeficient §0§."""),
        ),
        units = listOf("§\\mathcal{B}=(\\mathbf{x}_1,\\dots,\\mathbf{x}_d)§ je báza §P§", "§\\mathbf{v}\\in P§ a §\\mathbf{v}=\\sum\\alpha_i\\mathbf{x}_i§", "§(\\mathbf{v})_{\\mathcal{B}}:=(\\alpha_1,\\dots,\\alpha_d)\\in T^d§, usporiadaná"),
        ask = "Napište definici souřadnic vzhledem k bázi.",
        question = "Platí to o súradniciach?",
        short = "súradníc vzhľadom na bázu",
    ),
)

internal val La1ConceptById = La1Concepts.associateBy { it.id }

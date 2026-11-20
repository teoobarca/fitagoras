package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Problem "Množiny a zobrazení" of each past 2nd test (fit-wiki, transcribed in zdroje/testy-dml-2.md outside the repository),
 * Czech word for word, with a rubric adding up to the points of DmlPaperSpecs and a solution. Rubric points per part are
 * given on the paper only for 8. 12. 2023 (3 + 3 + 4); elsewhere they are our split of the 10 points.
 */

private fun rb(text: String, pts: Int, fix: String? = null) = Rubric(m(text), pts, fix?.let(::m))

private val e2 = PaperProblem(
    n = 2, pts = 10, title = "Identita, zloženie, množinové vzťahy", minutes = 25,
    text = m("""a) Definujte (korektně, i se správnou volbou dotčených množin) pojmy identické zobrazení a složení zobrazení. b) Existují zobrazení §f:B\to C§, §g:A\to B§ taková, že obě §f,g§ jsou injektivní, ale §f\circ g§ není? Svou odpověď zdůvodněte (tj. nalezněte konkrétní příklad, či dokažte, že taková zobrazení neexistují). c) Nechť §A,B,C§ jsou libovolné podmnožiny společného universa §U§. Rozhodněte (zvlášť), zda níže uvedené vztahy vždy platí. Tvrzení buď podrobně dokažte odvozením (bez Vennova diagramu), nebo nalezněte protipříklad a ukažte, že se o protipříklad skutečně jedná. c1) §(C\setminus B)\cap(C\setminus A)=C\setminus(A\cup B)§. c2) §(C\setminus B)\cup(C\setminus A)=C\setminus(A\cup B)§."""),
    checklist = listOf(
        rb("""a) §\mathrm{id}_A:A\to A§ s §(\forall x\in A)(\mathrm{id}_A(x)=x)§; §g:A\to B§, §f:B\to C§, §(f\circ g)(a)=f(g(a))§ pre všetky §a\in A§""", 3, "Bez množín (§A\\to A§, cieľ §g§ = definičný obor §f§) definícia nie je korektná."),
        rb("""b) Neexistujú: z §f(g(x))=f(g(y))§ injektivita §f§ dá §g(x)=g(y)§, injektivita §g§ dá §x=y§""", 3, "Príklad prostých §f,g§ nič nedokazuje; treba ľubovoľné §x,y§."),
        rb("""c1) Platí: rozpísanie na formuly a De Morgan, pre ľubovoľné §x§""", 2),
        rb("""c2) Neplatí: konkrétny protipríklad s vypočítanými stranami""", 2, "Protipríklad treba overiť, nielen napísať množiny."),
    ),
    answer = m("""b) Neexistujú (Věta 5.5). c1) platí; c2) neplatí, napr. §U=C=A=\{1\}§, §B=\emptyset§: vľavo §\{1\}§, vpravo §\emptyset§."""),
    drill = "dml-mz-rovnost",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Zloženie prostých" to "Z rovnosti obrazov zloženia odvoď rovnosť argumentov.",
        "c1) Platí vzťah?" to "Rozpíš obe strany na formuly pre ľubovoľné §x§ a porovnaj.",
        "c2) Platí vzťah?" to "Rozpíš, a keď sa formuly líšia, nájdi a over protipríklad.",
    ).let { pp -> pp.solve {
        pp[0]("""**Identické zobrazenie** na §A§: §\mathrm{id}_A:A\to A§, §(\forall x\in A)(\mathrm{id}_A(x)=x)§. **Zloženie** §f:B\to C§ a §g:A\to B§: §(f\circ g):A\to C§, §(f\circ g)(a):=f(g(a))§ pre všetky §a\in A§.""")
        pp[1]("""Neexistujú. Nech §x,y\in A§ a §(f\circ g)(x)=(f\circ g)(y)§, teda §f(g(x))=f(g(y))§.""")
        pp[1]("""§f§ je prosté, preto §g(x)=g(y)§; §g§ je prosté, preto §x=y§. Teda §f\circ g§ je prosté.""")
        pp[2]("""Pre ľubovoľné §x\in U§ rozpíšem ľavú stranu podľa definícií a upravím na pravú.""",
            eq("""x\in(C\setminus B)\cap(C\setminus A)\Leftrightarrow(x\in C\wedge x\notin B\wedge x\in C\wedge x\notin A)\Leftrightarrow(x\in C\wedge\neg(x\in A\vee x\in B))\Leftrightarrow x\in C\setminus(A\cup B)""", "De Morgan, idempotencia"))
        pp[2]("""Ekvivalencia platí pre každé §x§, vzťah platí vždy.""")
        pp[3]("""Ľavá strana je §(x\in C\wedge x\notin B)\vee(x\in C\wedge x\notin A)\Leftrightarrow(x\in C\wedge\neg(x\in A\wedge x\in B))§, teda §C\setminus(A\cap B)§. Líši sa, hľadám protipríklad.""")
        pp[3]("""Protipríklad: §U=\{1\}§, §A=C=\{1\}§, §B=\emptyset§. Vľavo §(C\setminus B)\cup(C\setminus A)=\{1\}\cup\emptyset=\{1\}§, vpravo §C\setminus(A\cup B)=\emptyset§. Neplatí.""")
    } },
)

private val k2 = PaperProblem(
    n = 2, pts = 10, title = "Mocnina zobrazenia, mohutnosť súčinu, injektivita", minutes = 25,
    text = m("""a) Pro množinu §A§ definujte pojem zobrazení §f§ z §A§ do §A§. Dále, jsou-li §f§ a §g§ zobrazení z §A§ do §A§, definujte jejich složení §(f\circ g)§ a pro každé §n\in\mathbb{N}_0§ definujte mocninu §f^n§. b) Existují množiny §C§ a §D§ takové, že §|C|+|D|=7§ a současně platí §|C\times D|=16§? Svou odpověď zdůvodněte (tj. nalezněte konkrétní příklad či dokažte, že takové množiny neexistují). c) Vyšetřete injektivitu a surjektivitu zobrazení §f:(\mathbb{R}^+)^3\to(\mathbb{R}^+)^3§ zadaného vztahem §f(x,y,z)=(\sqrt{y\cdot z},\sqrt{x\cdot z},\sqrt{y\cdot z})§ pro všechna §(x,y,z)\in(\mathbb{R}^+)^3§. Zkoumanou vlastnost vždy definujte. Platnost dokažte formálně z definice, neplatnost ukažte vymyšlením protipříkladu."""),
    checklist = listOf(
        rb("""a) §f\subseteq A\times A§, ku každému §a§ práve jedno §b§; §(f\circ g)(a)=f(g(a))§; §f^0=\mathrm{id}_A§, §f^1=f§, §f^n=f\circ\dots\circ f§""", 3),
        rb("""b) §|C\times D|=|C|\cdot|D|§, rozklady §16§ majú súčty §17,10,8§: neexistujú""", 3),
        rb("""c) Definície injektivity a surjektivity a protipríklad na injektivitu""", 2),
        rb("""c) Protipríklad na surjektivitu: bod bez vzoru""", 2),
    ),
    answer = m("""b) Neexistujú. c) Ani injektívne, ani surjektívne, pre predpis, ako je prepísaný: jeho 1. a 3. zložka sú rovnaké a prepis z obrázka na fit-wiki to nepotvrdzuje."""),
    drill = "dml-mz-velkosti",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Existujú také množiny?" to "§|C\\times D|=|C|\\cdot|D|§: hľadáš čísla s daným súčtom a súčinom.",
        "c) Injektivita a surjektivita" to "Injektivita z definície alebo protipríklad, surjektivita z definície alebo bod bez vzoru.",
    ).let { pp -> pp.solve {
        pp[0]("""§f:A\to A§ je §f\subseteq A\times A§, kde ku každému §a\in A§ existuje práve jedno §b\in A§ s §(a,b)\in f§. §(f\circ g)(a)=f(g(a))§ pre všetky §a\in A§. §f^0=\mathrm{id}_A§, §f^1=f§, §f^n=f\circ f\circ\dots\circ f§ (§n§-krát) pre §n\geq 2§.""")
        pp[1]("""§|C\times D|=|C|\cdot|D|=16§ (Tvrzení 5.2). Rozklady §(1,16),(2,8),(4,4)§ majú súčty §17,10,8§, žiadny §7§. Neexistujú.""")
        pp[2]("""Riešim predpis tak, ako je prepísaný z obrázka zadania na fit-wiki; že 1. a 3. zložka sú rovnaké, prepis nepotvrdzuje. Definície pre §f:D\to C§: injektívne je, ak §(\forall u,v\in D)(f(u)=f(v)\Rightarrow u=v)§; surjektívne, ak §(\forall y\in C)(\exists x\in D)(f(x)=y)§.""")
        pp[2]("""Injektivita: dva rôzne body majú rovnaký obraz, §f(1,2,2)=(2,\sqrt{2},2)=f(2,4,1)§. Nie je injektívne.""")
        pp[2]("""Surjektivita: prvá a tretia zložka obrazu sú vždy rovnaké, preto §(1,1,2)§ vzor nemá. Nie je surjektívne.""")
    } },
)

private val r2 = PaperProblem(
    n = 2, pts = 10, title = "Zloženie, identita, injektivita a surjektivita", minutes = 25,
    text = m("""a) Definujte (korektně, i se správnou volbou dotčených množin) pojmy složení zobrazení a identické zobrazení. b) Existují zobrazení §f§ a §g§ taková, že §f\circ g§ i §g\circ f§ jsou definované a §f\circ g§ je identické zobrazení zatímco §g\circ f§ není identické zobrazení? Svou odpověď zdůvodněte. c) Vyšetřete injektivitu a surjektivitu §f:\mathbb{R}^3\to\mathbb{R}^2§, §f(x,y,z)=(-x-y+z,\,2x+2y-2z)§. Zkoumanou vlastnost vždy definujte; platnost dokažte z definice, neplatnost protipříkladem."""),
    checklist = listOf(
        rb("""a) Zloženie s §g:A\to B§, §f:B\to C§ a identita §\mathrm{id}_A:A\to A§ s kvantifikátorom""", 3),
        rb("""b) Existujú: §g:\{1\}\to\{1,2\}§, §f:\{1,2\}\to\{1\}§ a overenie oboch zložení""", 3),
        rb("""c) Nie je injektívne: §f(0,0,0)=f(1,-1,0)§""", 2),
        rb("""c) Nie je surjektívne: druhá zložka je vždy §-2§-násobok prvej, §(0,1)§ vzor nemá""", 2),
    ),
    answer = m("""b) Existujú. c) Ani injektívne, ani surjektívne."""),
    drill = "dml-mz-injsurj",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Príklad zobrazení" to "Nájdi konkrétne malé zobrazenia a over obe zloženia.",
        "c) Injektivita a surjektivita" to "Protipríklad na injektivitu a bod bez vzoru, alebo dôkaz z definície.",
    ).let { pp -> pp.solve {
        pp[0]("""Pre §f:B\to C§ a §g:A\to B§ je §(f\circ g):A\to C§, §(f\circ g)(a):=f(g(a))§ pre všetky §a\in A§. §\mathrm{id}_A:A\to A§, §(\forall x\in A)(\mathrm{id}_A(x)=x)§.""")
        pp[1]("""Stačí §g:\{1\}\to\{1,2\}§, §g(1)=1§ a §f:\{1,2\}\to\{1\}§, §f(1)=f(2)=1§. Over obe zloženia:""",
            eq("""(f\circ g)(1)=1\ \Rightarrow\ f\circ g=\mathrm{id}_{\{1\}},\qquad (g\circ f)(2)=g(1)=1\neq 2\ \Rightarrow\ g\circ f\neq\mathrm{id}_{\{1,2\}}"""))
        pp[2]("""Definície pre §f:D\to C§: injektívne je, ak §(\forall u,v\in D)(f(u)=f(v)\Rightarrow u=v)§; surjektívne, ak §(\forall y\in C)(\exists x\in D)(f(x)=y)§.""")
        pp[2]("""§f(1,-1,0)=(0,0)=f(0,0,0)§, hoci §(1,-1,0)\neq(0,0,0)§: nie je injektívne.""")
        pp[2]("""Pre každý obraz §(a,b)§ je §b=-2a§, preto §(0,1)§ vzor nemá: nie je surjektívne.""")
    } },
)

private val d3 = PaperProblem(
    n = 3, pts = 10, title = "Inklúzia, rozdiel, potenčná množina", minutes = 25,
    text = m("""a) (3 b) Pro množiny §X,Y\subseteq\mathcal{U}§ definujte pojmy: „§X\subseteq Y§“, „rozdíl množin §X,Y§“ a „potenční množina množiny §X§“. b) (3 b) Nalezněte příklad množiny §A§ (v libovolném universu), pro kterou platí, že §\mathcal{P}(A)§ má právě §7§ prvků, nebo dokažte, že taková množina v žádném universu neexistuje. c) (4 b) Uvažujme libovolné univerzum §\mathcal{U}§ a množiny §A,B,C\subseteq\mathcal{U}§. S využitím definic z bodu a) přepište do ekvivalentní formule vztah §(A\cap B\cap\overline{C})\subseteq(A\cup B)\setminus C§. Rozhodněte, zda uvedený vztah vždy platí a svou odpověď dokažte (pokud obecně neplatí, vymyslete protipříklad)."""),
    checklist = listOf(
        rb("""a) §X\subseteq Y\Leftrightarrow(\forall x\in\mathcal{U})(x\in X\Rightarrow x\in Y)§, §X\setminus Y§, §\mathcal{P}(X)=\{Z\subseteq\mathcal{U}\mid Z\subseteq X\}§""", 3),
        rb("""b) §|\mathcal{P}(A)|=2^{|A|}§ a §7§ nie je mocnina dvojky: neexistuje (aj pre nekonečné §A§ je §\mathcal{P}(A)§ nekonečná)""", 3),
        rb("""c) Formula §(\forall x)((x\in A\wedge x\in B\wedge x\notin C)\Rightarrow((x\in A\vee x\in B)\wedge x\notin C))§""", 2),
        rb("""c) Dôkaz, že implikácia platí pre každé §x§""", 2),
    ),
    answer = m("""b) Neexistuje. c) Vzťah vždy platí."""),
    drill = "dml-mz-rovnost",
    solution = postup(
        "a) Definície" to "Presné znenie s kvantifikátorom a univerzom.",
        "b) Potenčná množina" to "Množina s §n§ prvkami má §2^n§ podmnožín.",
        "c) Platí vzťah?" to "Prepíš inklúziu na implikáciu formúl pre ľubovoľné §x§.",
    ).let { pp -> pp.solve {
        pp[0]("""§X\subseteq Y\Leftrightarrow(\forall x\in\mathcal{U})(x\in X\Rightarrow x\in Y)§; §X\setminus Y:=\{x\in\mathcal{U}\mid x\in X\wedge x\notin Y\}§; §\mathcal{P}(X):=\{Z\subseteq\mathcal{U}\mid Z\subseteq X\}§.""")
        pp[1]("""Konečná §A§ s §n§ prvkami má §2^n§ podmnožín, nekonečná nekonečne veľa. §7§ nie je mocnina dvojky, taká §A§ neexistuje.""")
        pp[2]("""Inklúziu prepíšem podľa definícií z a) na implikáciu pre každé §x§.""", eq("""(\forall x\in\mathcal{U})\big((x\in A\wedge x\in B\wedge x\notin C)\Rightarrow((x\in A\vee x\in B)\wedge x\notin C)\big)"""))
        pp[2]("""Ak platí ľavá strana, je §x\in A§, teda §x\in A\vee x\in B§, a §x\notin C§ máme priamo. Implikácia platí pre každé §x§, vzťah vždy platí.""")
    } },
)

private val a2 = PaperProblem(
    n = 2, pts = 10, title = "Rovnosť množín, súčin, rozdiel", minutes = 25,
    text = m("""a) Pro množiny §A,B\subseteq U§ definujte pojmy: „§A=B§“, rozdíl množin §A,B§ a kartézský součin §A,B§. b) Mějme 2 množiny §C\subseteq D§, a platí že §|C\times D|=36§; vysvětlete, jakých všech hodnot může nabývat §|D\setminus C|§? c) Dokažte nebo ukažte protipříklad: §(X\setminus Y)\times(X\setminus Z)=(X\setminus Z)\times(X\setminus Y)§."""),
    checklist = listOf(
        rb("""a) §A=B\Leftrightarrow(\forall x\in U)(x\in A\Leftrightarrow x\in B)§, rozdiel a súčin podľa definícií""", 3),
        rb("""b) §|C|\cdot|D|=36§, §|C|\leq|D|§, §|D\setminus C|=|D|-|C|§: hodnoty §35,16,9,5,0§""", 3),
        rb("""c) Konkrétne množiny §X,Y,Z§ ako protipríklad""", 2),
        rb("""c) Obe strany vypočítané a ukázané, že sa líšia""", 2, "Protipríklad treba overiť, nielen napísať množiny."),
    ),
    answer = m("""b) §|D\setminus C|\in\{0,5,9,16,35\}§. c) Neplatí, napr. §X=\{1,2\}§, §Y=\{1\}§, §Z=\{2\}§."""),
    drill = "dml-mz-velkosti",
    solution = postup(
        "a) Definície" to "Presné znenie s kvantifikátorom a univerzom.",
        "b) Možné veľkosti rozdielu" to "§|C|\\cdot|D|§ je daný súčin a pri §C\\subseteq D§ je §|D\\setminus C|=|D|-|C|§.",
        "c) Platí vzťah?" to "Rozpíš na formuly; ak sa líšia, nájdi a over protipríklad.",
    ).let { pp -> pp.solve {
        pp[0]("""§A=B\Leftrightarrow(\forall x\in U)(x\in A\Leftrightarrow x\in B)§; §A\setminus B:=\{x\in U\mid x\in A\wedge x\notin B\}§; §A\times B:=\{(a,b)\mid a\in A\wedge b\in B\}§.""")
        pp[1]("""§|C\times D|=|C|\cdot|D|=36§ a §C\neq\emptyset§. Z §C\subseteq D§ je §|C|\leq|D|§ a §|D\setminus C|=|D|-|C|§.""")
        pp[1]("""Dvojice §(1,36),(2,18),(3,12),(4,9),(6,6)§ dávajú §35,16,9,5,0§; všetky sa dajú dosiahnuť, napríklad §C=\{1,\dots,|C|\}§, §D=\{1,\dots,|D|\}§.""")
        pp[2]("""Ľavú stranu rozpíšem podľa definícií; vpravo sú §Y§ a §Z§ vymenené, preto hľadám protipríklad.""", eq("""(u,v)\in(X\setminus Y)\times(X\setminus Z)\Leftrightarrow(u\in X\wedge u\notin Y\wedge v\in X\wedge v\notin Z)"""))
        pp[2]("""Protipríklad: §X=\{1,2\}§, §Y=\{1\}§, §Z=\{2\}§. Vľavo §\{2\}\times\{1\}=\{(2,1)\}§, vpravo §\{1\}\times\{2\}=\{(1,2)\}§. Neplatí.""")
    } },
)

/* 2024 F, J, H and the opravná 2026 Ř (fit-wiki photos; on J the problem is a cut-out pasted on the sheet of J-1). */

private val f2 = PaperProblem(
    n = 2, pts = 10, title = "Obor hodnôt, definičný obor, prosté a na", minutes = 25,
    text = m("""a) (4b) Pro množiny §A,B\subseteq\mathcal{U}§ a zobrazení §f:A\to B§ definujte pojmy: „obor hodnot §f§“, „definiční obor §f§“. Dále definujte, kdy je §f§ „prosté“ a kdy je „na“. b) (6b) Definujme zobrazení §f:\mathbb{N}\times\mathbb{N}\to\mathbb{N}§ předpisem §f(a,b)=2^a4^b§ pro všechna §(a,b)\in\mathbb{N}\times\mathbb{N}§. Zjistěte a zdůvodněte, zda je §f§ prosté a zda na."""),
    checklist = listOf(
        rb("""a) §D_f§ a §H_f§ ako podmnožiny §A§ a §B§ s kvantifikátorom""", 2),
        rb("""a) Prosté: §(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)§; na: §(\forall y\in B)(\exists x\in A)(f(x)=y)§""", 2),
        rb("""b) §f(a,b)=2^{a+2b}§; nie je prosté: §f(3,1)=2^5=f(1,2)§, hoci §(3,1)\neq(1,2)§""", 3, "Na vyvrátenie treba dva konkrétne rôzne prvky s rovnakým obrazom, overené výpočtom."),
        rb("""b) Nie je na: obraz je vždy párny, §3\in\mathbb{N}§ vzor nemá""", 3, "Na vyvrátenie treba konkrétny prvok §\\mathbb{N}§ a zdôvodnenie, prečo ho žiadna dvojica nedá."),
    ),
    answer = m("""b) Nie je prosté ani na: §f(3,1)=f(1,2)=32§ a §3§ nemá vzor."""),
    drill = "dml-mz-injsurj",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Prosté?" to "Hľadaj dva rôzne prvky s rovnakým obrazom, alebo z rovnosti obrazov odvoď rovnosť prvkov.",
        "b) Na?" to "Hľadaj prvok bez vzoru, alebo ku každému prvku nájdi vzor.",
    ).let { pp -> pp.solve {
        pp[0]("""**Definičný obor** §D_f:=\{a\in A\mid(\exists b\in B)(f(a)=b)\}§, **obor hodnôt** §H_f:=\{b\in B\mid(\exists a\in A)(f(a)=b)\}§.""")
        pp[0]("""§f§ je **prosté**, ak §(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)§, a **na**, ak §(\forall y\in B)(\exists x\in A)(f(x)=y)§.""")
        pp[1]("""Prepíšem predpis na jednu mocninu: §f(a,b)=2^a\cdot(2^2)^b=2^{a+2b}§. Obraz závisí len od §a+2b§, preto stačia dve dvojice s rovnakým §a+2b§.""",
            eq("""f(3,1)=2^3\cdot 4^1=32=2^1\cdot 4^2=f(1,2)"""))
        pp[1]("""§(3,1)\neq(1,2)§ a obrazy sa rovnajú, preto §f§ nie je prosté.""")
        pp[2]("""Každý obraz §2^{a+2b}§ s §a,b\geq 1§ je párny, preto nepárne §3\in\mathbb{N}§ vzor nemá. §f§ nie je na.""")
    } },
)

private val j2 = PaperProblem(
    n = 2, pts = 10, title = "Definičný obor, veľkosti, množinové vzťahy", minutes = 25,
    text = m("""a) Uvažujme zobrazení §f:A\to B§. Definujte pro toto zobrazení definiční obor a obor hodnot a vysvětlete, co to znamená, když řekneme, že je totální. b) Existují množiny §C§ a §D§ takové, že §|C|+|D|=14§ a zároveň platí §|C\times D|=13§? Svou odpověď zdůvodněte (tj. nalezněte konkrétní příklad či dokažte, že takové množiny neexistují). c) Nechť §X,Y,Z§ jsou libovolné podmnožiny společného universa §U§. Rozhodněte (zvlášť), zda níže uvedené vztahy vždy platí. Tvrzení buď podrobně dokažte odvozením (bez Vennova diagramu), nebo nalezněte protipříklad a ukažte, že se o protipříklad skutečně jedná. c1) §(X\cup Y)\cap Z\subseteq(Y\cap Z)\cup X§ c2) §(X\cup Y)\setminus Z=(X\setminus Z)\cup Y§"""),
    checklist = listOf(
        rb("""a) §D_f§ a §H_f§ s kvantifikátorom; totálne: §D_f=A§, teda každé §a\in A§ má práve jeden obraz""", 3),
        rb("""b) Existujú: §|C\times D|=|C|\cdot|D|=13§ a §13=1\cdot 13§, napr. §C=\{1\}§, §D=\{1,\dots,13\}§, oba počty overené""", 3, "Odpoveď treba doložiť konkrétnymi množinami a overiť obe podmienky."),
        rb("""c1) Platí: pre ľubovoľné §x§ rozbor §x\in X§ / §x\notin X§ alebo úprava formúl""", 2),
        rb("""c2) Neplatí: konkrétny protipríklad s vypočítanými stranami""", 2, "Protipríklad treba overiť, nielen napísať množiny."),
    ),
    answer = m("""b) Existujú, napr. §C=\{1\}§, §D=\{1,\dots,13\}§. c1) platí; c2) neplatí, napr. §U=Y=Z=\{1\}§, §X=\emptyset§."""),
    drill = "dml-mz-velkosti",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Existujú také množiny?" to "§|C\\times D|=|C|\\cdot|D|§: hľadáš čísla s daným súčtom a súčinom.",
        "c1) Platí vzťah?" to "Pre ľubovoľné §x§ z ľavej strany ukáž, že je v pravej.",
        "c2) Platí vzťah?" to "Rozpíš, a keď sa formuly líšia, nájdi a over protipríklad.",
    ).let { pp -> pp.solve {
        pp[0]("""§D_f:=\{a\in A\mid(\exists b\in B)(f(a)=b)\}§ a §H_f:=\{b\in B\mid(\exists a\in A)(f(a)=b)\}§. §f§ je **totálne**, ak §D_f=A§: každé §a\in A§ má práve jeden obraz, nielen nanajvýš jeden.""")
        pp[1]("""Pre konečné množiny je §|C\times D|=|C|\cdot|D|§ (Tvrzení 5.2). §13§ je prvočíslo, rozklad je len §1\cdot 13§, a §1+13=14§.""")
        pp[1]("""Existujú: §C=\{1\}§, §D=\{1,\dots,13\}§. Overenie: §|C|+|D|=1+13=14§ a §C\times D=\{(1,1),\dots,(1,13)\}§ má §13§ prvkov.""")
        pp[2]("""Nech §x\in(X\cup Y)\cap Z§, teda §(x\in X\vee x\in Y)\wedge x\in Z§. Ak §x\in X§, je §x§ v pravej strane, lebo tá obsahuje §X§.""")
        pp[2]("""Ak §x\notin X§, musí byť §x\in Y§ a §x\in Z§, teda §x\in Y\cap Z§, a opäť je v pravej strane. Inklúzia platí vždy.""")
        pp[3]("""Vľavo je §(x\in X\vee x\in Y)\wedge x\notin Z§, vpravo §(x\in X\wedge x\notin Z)\vee x\in Y§. Líšia sa pre §x\in Y\cap Z§, hľadám protipríklad.""")
        pp[3]("""Protipríklad: §U=\{1\}§, §X=\emptyset§, §Y=Z=\{1\}§. Vľavo §(X\cup Y)\setminus Z=\{1\}\setminus\{1\}=\emptyset§, vpravo §(X\setminus Z)\cup Y=\emptyset\cup\{1\}=\{1\}§. Neplatí.""")
    } },
)

private val h2 = PaperProblem(
    n = 2, pts = 10, title = "Súčin, mohutnosť, injektivita a surjektivita", minutes = 25,
    text = m("""a) Definujte pojmy kartézský součin §A\times B§ a to, že množina §A§ má stejnou mohutnost jako množina §B§. b) Existují neprázdné množiny §A§, §B§ takové, že §|A|=|B|=|A\times B|§? Svou odpověď zdůvodněte (tj. nalezněte konkrétní příklad či dokažte, že taková dvojice množin neexistuje). c) Vyšetřete injektivitu a surjektivitu zobrazení §f:\mathbb{Z}^2\to\mathbb{Z}^2§ zadaného vztahem §f(m,n)=(2m-3n+3,5m-7n+2)§ pro všechna §(m,n)\in\mathbb{Z}^2§. Zkoumanou vlastnost vždy definujte. Platnost dokažte formálně z definice, neplatnost ukažte vymyšlením protipříkladu."""),
    checklist = listOf(
        rb("""a) §A\times B:=\{(a,b)\mid a\in A\wedge b\in B\}§; §|A|=|B|§, ak existuje bijekcia §A\to B§""", 3),
        rb("""b) Existujú, napr. §A=B=\{1\}§: §A\times B=\{(1,1)\}§ a bijekcie medzi nimi""", 3, "Odpoveď treba doložiť konkrétnymi množinami a bijekciami."),
        rb("""c) Injektívne: z §f(m,n)=f(m',n')§ dve rovnice pre rozdiely a z nich §m=m'§, §n=n'§""", 2),
        rb("""c) Surjektívne: ku každému §(u,v)\in\mathbb{Z}^2§ vzor s celými zložkami a skúška""", 2, "Vzor musí mať celé zložky; dosaď ho a over, že dá §(u,v)§."),
    ),
    answer = m("""b) Existujú, napr. §A=B=\{1\}§. c) Injektívne aj surjektívne, teda bijekcia; vzor §(u,v)§ je §(-7u+3v+15,\,-5u+2v+11)§."""),
    drill = "dml-mz-injsurj",
    solution = postup(
        "a) Definície" to "Presné znenie so všetkými množinami, na ktorých pojem žije.",
        "b) Existujú také množiny?" to "Skús najmenšie neprázdne množiny a over bijekcie.",
        "c) Injektivita a surjektivita" to "Injektivita z rovnosti obrazov, surjektivita vyriešením §f(m,n)=(u,v)§ v celých číslach.",
    ).let { pp -> pp.solve {
        pp[0]("""§A\times B:=\{(a,b)\mid a\in A\wedge b\in B\}§. §A§ a §B§ majú **rovnakú mohutnosť**, §|A|=|B|§, práve keď existuje bijekcia §f:A\to B§.""")
        pp[1]("""Existujú: §A=B=\{1\}§. Potom §A\times B=\{(1,1)\}§ a zobrazenie §1\mapsto(1,1)§ je bijekcia §A\to A\times B§, rovnako §1\mapsto 1§ medzi §A§ a §B§. Teda §|A|=|B|=|A\times B|§.""")
        pp[2]("""Definície pre §f:\mathbb{Z}^2\to\mathbb{Z}^2§: injektívne, ak §(\forall x,y\in\mathbb{Z}^2)(f(x)=f(y)\Rightarrow x=y)§; surjektívne, ak §(\forall y\in\mathbb{Z}^2)(\exists x\in\mathbb{Z}^2)(f(x)=y)§.""")
        pp[2]("""Injektivita: nech §f(m,n)=f(m',n')§. Odčítaním zložiek dostanem pre §d=m-m'§, §e=n-n'§ sústavu, z ktorej §e=0§ a potom §d=0§.""",
            eq("""2d-3e=0,\quad 5d-7e=0\ \Rightarrow\ 5(2d-3e)-2(5d-7e)=-e=0\ \Rightarrow\ e=0,\ d=0"""))
        pp[2]("""Surjektivita: k §(u,v)\in\mathbb{Z}^2§ hľadám §(m,n)§ s §2m-3n=u-3§ a §5m-7n=v-2§. Vyriešim sústavu a dosadením overím.""",
            eq("""m=-7u+3v+15,\quad n=-5u+2v+11"""),
            eq("""f(m,n)=\big(2m-3n+3,\ 5m-7n+2\big)=(u,v)""", "Skúška: §2m-3n+3=-14u+6v+30+15u-6v-33+3=u§, podobne druhá zložka."))
        pp[2]("""§m,n§ sú celé, lebo §u,v§ sú celé, teda vzor leží v §\mathbb{Z}^2§. §f§ je injektívne aj surjektívne.""")
    } },
)

/** Bézout for 484 and 294 by the REA, as the number theory template computes it (gcd 2). */
private val bezout2026r = tcBezout(484, 294).also { (g, x, y) -> check(g == 2L && 484 * x + 294 * y == 2L) }

private val r2026 = PaperProblem(
    n = 2, pts = 10, title = "Prosté a na formulou, zobrazenie do párnych čísel", minutes = 25,
    text = m("""(a) Mějme zobrazení §h:X\to Y§. Užitím formule s omezenými kvantifikátory definujte, že je zobrazení §g§ prosté. Obdobně definujte, že je zobrazení §g§ na. Obě formule znegujte do tvaru, kde negace stojí jen před atomickými formulemi. (b) Označme §S§ množinu všech sudých celých čísel. Zjistěte, zda je zobrazení §h:\mathbb{Z}\times\mathbb{Z}\to S§ dané předpisem §h(a,b)=484a+294b§ prosté a zda je na. Platnost dokažte formálně z definice, neplatnost ukažte vymyšlením protipříkladu."""),
    checklist = listOf(
        rb("""(a) Prosté: §(\forall x,y\in X)(h(x)=h(y)\Rightarrow x=y)§; na: §(\forall y\in Y)(\exists x\in X)(h(x)=y)§""", 2),
        rb("""(a) Negácie: §(\exists x,y\in X)(h(x)=h(y)\wedge x\neq y)§ a §(\exists y\in Y)(\forall x\in X)(h(x)\neq y)§""", 3, "Kvantifikátory sa otočia a implikácia sa neguje na konjunkciu, nie na implikáciu."),
        rb("""(b) Nie je prosté: §h(147,-242)=0=h(0,0)§""", 2, "Na vyvrátenie treba dve konkrétne rôzne dvojice s rovnakým obrazom, overené výpočtom."),
        rb("""(b) Je na: §\gcd(484,294)=2§, Bézout §2=484\cdot ${bezout2026r.second}+294\cdot(${bezout2026r.third})§ a vzor §(k\cdot ${bezout2026r.second},k\cdot(${bezout2026r.third}))§ pre §2k§""", 3, "Na dôkaz surjektivity treba ku každému párnemu číslu vzor a skúšku, nielen príklad."),
    ),
    answer = m("""(b) Nie je prosté (§h(147,-242)=h(0,0)=0§), je na (Bézout: §2=484\cdot ${bezout2026r.second}+294\cdot(${bezout2026r.third})§)."""),
    drill = "dml-mz-injsurj",
    solution = postup(
        "(a) Definície formulou" to "Prosté a na s kvantifikátormi obmedzenými na §X§ a §Y§.",
        "(a) Negácie" to "Otoč kvantifikátory a negáciu posuň až k atomickým formulám.",
        "(b) Prosté?" to "Hľadaj dve rôzne dvojice s rovnakým obrazom.",
        "(b) Na?" to "Ku každému párnemu číslu nájdi vzor cez Bézoutovu rovnosť.",
    ).let { pp -> pp.solve {
        pp[0]("""Zadanie píše raz §h§ a raz §g§; ide o to isté zobrazenie §h:X\to Y§. Prvá formula hovorí, že je prosté, druhá, že je na.""",
            eq("""(\forall x\in X)(\forall y\in X)\big(h(x)=h(y)\Rightarrow x=y\big)"""),
            eq("""(\forall y\in Y)(\exists x\in X)\big(h(x)=y\big)"""))
        pp[1]("""Negácia otočí kvantifikátory; negácia implikácie §A\Rightarrow B§ je §A\wedge\neg B§ a §\neg(x=y)§ je atomická formula s negáciou, zapíšem ju §x\neq y§.""",
            eq("""(\exists x\in X)(\exists y\in X)\big(h(x)=h(y)\wedge x\neq y\big)"""),
            eq("""(\exists y\in Y)(\forall x\in X)\big(h(x)\neq y\big)"""))
        pp[2]("""§h§ je dobre definované: §484§ aj §294§ sú párne, teda aj §484a+294b§. Prosté nie je, lebo §484\cdot 147=71148=294\cdot 242§:""",
            eq("""h(147,-242)=484\cdot 147-294\cdot 242=0=h(0,0)"""))
        pp[2]("""§(147,-242)\neq(0,0)§ a obrazy sa rovnajú, §h§ nie je prosté.""")
        pp[3]("""REA dá §\gcd(484,294)=2§ a Bézoutovu rovnosť. Pre ľubovoľné §s=2k\in S§ ju vynásobím §k§:""",
            eq("""2=484\cdot ${bezout2026r.second}+294\cdot(${bezout2026r.third})\ \Rightarrow\ h\big(${bezout2026r.second}k,\ ${bezout2026r.third}k\big)=k\big(484\cdot ${bezout2026r.second}+294\cdot(${bezout2026r.third})\big)=2k=s"""))
        pp[3]("""Každé párne §s§ má vzor v §\mathbb{Z}\times\mathbb{Z}§, §h§ je na.""")
    } },
)

val SetsPaperProblems: Map<String, PaperProblem> = mapOf("dml-2025-e" to e2, "dml-2025-k" to k2, "dml-2024-r" to r2, "dml-2023-d" to d3, "dml-2023-a" to a2,
    "dml-2026-r" to r2026, "dml-2024-f" to f2, "dml-2024-j" to j2, "dml-2024-h" to h2)

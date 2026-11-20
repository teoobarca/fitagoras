package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Every definition of kapitola 5 "Množiny a zobrazení" (chap-05-mnoziny-zobrazeni.woo), Czech word for word; \UU is
 * written \mathcal{U}, \PP \mathcal{P}, \ceq :=. Numbering per kind as in the rendered textbook (Definice 5.1 to 5.15).
 */

val mzMnozina = Concept(
    id = "dml-mz-mnozina", name = "Množina a prvok", src = "Skriptá, Definice 5.1",
    czech = listOf(
        p("""**Množina** je ==neuspořádaný== souhrn přesně specifikovaných objektů, které nazýváme prvky množiny. Může být zadána"""),
        i("""výčtem svých prvků (všech, nebo zkráceně s využitím §\ldots§)"""),
        i("""charakterizující vlastností: Množinu všech prvků §x§ universa §\mathcal{U}§, které splňují vlastnost §v(x)§, zapisujeme"""),
        b("""M=\{x\mid x\in\mathcal{U}\wedge v(x)\}\softbreak[1]=\{x\in\mathcal{U}\mid v(x)\}."""),
        i("""výrazem s charakterizující vlastností: Množinu všech výrazů §t(x_1,\dots x_n)§ závisejících na proměnných §x_1,\dots x_n§ z universa §\mathcal{U}§, pro všechny volby §x_1,\dots x_n§ které splňují vlastnost §v(x)§, zapisujeme"""),
        b("""M=\{t(x_1,\dots x_n)\mid\softbreak[1] v(x_1,\dots x_n)\}."""),
        p("""To, že prvek §x§ **leží v množině** §M§, značíme §x\in M§, v opačném případě píšeme §x\not\in M§. Množina, která neobsahuje žádné prvky, se nazývá **prázdná množina** a značí se §\emptyset§, případně §\{\}§."""),
    ),
    why = listOf(
        w("""neuspořádaný souhrn""", """Poradie ani opakovanie nehrajú rolu: §\{1,2\}=\{2,1\}=\{1,1,2\}§. Prvok v množine buď je, alebo nie je (Varování 5.1)."""),
        w("""přesně specifikovaných objektů""", """O každom objekte sa musí dať rozhodnúť, či do množiny patrí."""),
        w("""universa §\mathcal{U}§""", """Vlastnosť vyberá prvky z vopred zvoleného univerza; bez neho hrozia paradoxy (Poznámka 5.2, Russell)."""),
        w("""výrazem s charakterizující vlastností""", """Zápis §\{2k\mid k\in\mathbb{Z}\}§: prvky sú hodnoty výrazu §2k§, nie samotné §k§."""),
    ),
    judge = listOf(
        j("""§\{1,2,2,3\}=\{3,2,1\}§""", true, """Platí, poradie ani opakovanie v množine nehrajú rolu.""", """Množiny sa rovnajú, hoci zápis je iný. Prvok buď v množine je, alebo nie je; koľkokrát ho napíšeš, je jedno (Varování 5.1)."""),
        j("""§\{3k\mid k\in\{-1,0,1,\dots,11\}\}=\{-3,0,3,\dots,33\}§""", true, """Platí, prvky sú hodnoty §3k§ pre §k§ od §-1§ po §11§ (Příklad 5.2).""", """Zápis výrazom vypisuje hodnoty §3k§, nie §k§. Pre §k=-1§ je to §-3§, pre §k=11§ je to §33§."""),
        j("""§\emptyset\in\emptyset§""", false, """Neplatí, prázdna množina nemá žiadny prvok, ani §\emptyset§.""", """Prázdna množina neobsahuje nič, ani sama seba. Pravdivé je §\emptyset\subseteq\emptyset§, to je iný vzťah."""),
        j("""§\{\emptyset\}=\emptyset§""", false, """Neplatí, §\{\emptyset\}§ má jeden prvok, §\emptyset§ žiadny.""", """Krabica s prázdnou krabicou nie je prázdna. §\{\emptyset\}§ má práve jeden prvok."""),
    ),
    units = listOf("Neusporiadaný súhrn presne špecifikovaných objektov, prvky", "Zadanie výčtom", "Zadanie charakterizujúcou vlastnosťou nad univerzom §\\mathcal{U}§", "Zadanie výrazom s vlastnosťou"),
    extra = listOf("§x\\in M§, §x\\notin M§, prázdna množina §\\emptyset§"),
    ask = "Definujte pojem množina a uveďte, jak může být zadána.",
    question = "Platí to o množinách?",
    short = "množiny",
)

val mzInkluze = Concept(
    id = "dml-mz-inkluze", name = "Inklúzia, rovnosť, potenčná množina", src = "Skriptá, Definice 5.2",
    czech = listOf(
        p("""Pro libovolné množiny §A§ a §B§ prvků z universa §\mathcal{U}§ definujeme vztahy:"""),
        i("""§A§ je **podmnožinou** §B§ (a tedy §B§ je **nadmnožinou** §A§):"""),
        b("""A\subseteq B\Leftrightarrow\softbreak[1](\forall x\in\mathcal{U})\softbreak(x\in A\Rightarrow x\in B)"""),
        p("""Vztah §\subseteq§ se také nazývá **množinová inkluze**."""),
        i("""§A§ je **rovna** §B§:"""),
        b("""A=B\Leftrightarrow\softbreak[1](\forall x\in\mathcal{U})\softbreak(x\in A\Leftrightarrow x\in B)"""),
        i("""§A§ je **vlastní podmnožinou** §B§:"""),
        b("""A\subsetneq B\Leftrightarrow\softbreak[1]((A\subseteq B)\softbreak[2]\wedge(A\neq B))"""),
        p("""Pro množinu §A§ pak definujeme její **potenční množinu** jako množinu obsahující všechny podmnožiny §A§, značíme §\mathcal{P}(A)§:"""),
        b("""\mathcal{P}(A):=\{X\subseteq\mathcal{U}\mid X\subseteq A\}"""),
        p("""Pozor, je to podmnožina jiného universa, než množina §A§ samotná."""),
    ),
    why = listOf(
        w("""§(\forall x\in\mathcal{U})(x\in A\Rightarrow x\in B)§""", """Inklúzia je implikácia pre každý prvok. Dôkaz: vezmi ľubovoľné §x§; vyvrátenie: jedno §x\in A§, ktoré nie je v §B§."""),
        w("""§x\in A\Leftrightarrow x\in B§""", """Rovnosť je ekvivalencia, teda dve inklúzie: §A\subseteq B§ a §B\subseteq A§ (Tvrzení 5.1)."""),
        w("""§A\neq B§""", """Vlastná podmnožina je menšia: §A\subsetneq A§ neplatí nikdy."""),
        w("""všechny podmnožiny §A§""", """Prvky §\mathcal{P}(A)§ sú množiny, aj §\emptyset§ a §A§. Preto §Y\in\mathcal{P}(X)\Leftrightarrow Y\subseteq X§ (Varování 5.3)."""),
    ),
    judge = listOf(
        j("""§\emptyset\subseteq A§ pre každú §A§""", true, """Platí, implikácia s nepravdivým predpokladom §x\in\emptyset§ je vždy pravdivá.""", """Prázdna množina je podmnožinou každej (Tvrzení 5.1). Implikácia §x\in\emptyset\Rightarrow x\in A§ platí, lebo §x\in\emptyset§ je vždy nepravda."""),
        j("""§\{1\}\in\mathcal{P}(\{1,2\})§""", true, """Platí, §\{1\}\subseteq\{1,2\}§, a prvky §\mathcal{P}§ sú práve podmnožiny.""", """Prvky potenčnej množiny sú množiny. §\{1\}§ je podmnožina §\{1,2\}§, teda prvok §\mathcal{P}(\{1,2\})§."""),
        j("""§\{1\}\subseteq\mathcal{P}(\{1,2\})§""", false, """Neplatí, prvok §1§ nie je podmnožinou §\{1,2\}§.""", """Zámena §\in§ a §\subseteq§ (Varování 5.3). §\{1\}\subseteq\mathcal{P}(\dots)§ by znamenalo §1\in\mathcal{P}(\dots)§, a §1§ nie je podmnožinou §\{1,2\}§."""),
        j("""§\mathcal{P}(\{1,2,3\})§ má §8§ prvkov""", true, """Platí, §2^3=8§ podmnožín (Příklad 5.4).""", """Má ich osem, aj §\emptyset§ a celú množinu. Sú to §\emptyset§, tri jednoprvkové, tri dvojprvkové a §\{1,2,3\}§."""),
        j("""§A\subsetneq A§ pre nejakú §A§""", false, """Neplatí, vlastná podmnožina sa musí od §A§ líšiť.""", """Definícia chce §A\subseteq B\wedge A\neq B§. Pre §B=A§ druhá časť neplatí nikdy."""),
    ),
    units = listOf("§A\\subseteq B§: pre všetky §x\\in\\mathcal{U}§ implikácia §x\\in A\\Rightarrow x\\in B§", "§\\mathcal{P}(A)§: množina všetkých podmnožín §A§"),
    extra = listOf("§A=B§: pre všetky §x§ ekvivalencia", "Vlastná podmnožina: §A\\subseteq B\\wedge A\\neq B§", "§\\mathcal{P}(A)§ je v inom univerze než §A§"),
    ask = "Pro množiny §X,Y\\subseteq\\mathcal{U}§ definujte pojmy „§X\\subseteq Y§“ a „potenční množina množiny §X§“.",
    question = "Platí to o podmnožinách?",
    short = "inklúzie, rovnosti a potenčnej množiny",
)

val mzOperace = Concept(
    id = "dml-mz-operace", name = "Množinové operácie", src = "Skriptá, Definice 5.3",
    czech = listOf(
        p("""Buďte §A,B\subseteq\mathcal{U}§. Definujeme"""),
        i("""**doplněk** §A§ (v universu §\mathcal{U}§): §\overline{A}:=\{x\in\mathcal{U}\mid x\notin A\}§"""),
        i("""**průnik** §A§ a §B§: §A\cap B:=\{x\in\mathcal{U}\mid x\in A\wedge x\in B\}§"""),
        i("""**sjednocení** §A§ a §B§: §A\cup B:=\{x\in\mathcal{U}\mid x\in A\vee x\in B\}§"""),
        i("""**rozdíl** §A§ a §B§: §A\setminus B:=\{x\in\mathcal{U}\mid x\in A\wedge x\notin B\}§"""),
    ),
    why = listOf(
        w("""§\{x\in\mathcal{U}\mid\dots\}§""", """Každá operácia je množina prvkov univerza s vlastnosťou. Preto sa §x\in A\cap B§ dá prepísať na formulu §x\in A\wedge x\in B§ (Poznámka 5.3)."""),
        w("""doplněk ... v universu""", """Doplnok závisí od univerza: v §\mathbb{N}§ je §\overline{\{1\}}=\{2,3,\dots\}§, v §\mathbb{Z}§ obsahuje aj záporné čísla."""),
        w("""§x\in A\wedge x\notin B§""", """Rozdiel je §A\cap\overline{B}§, nie „§A§ mínus počet“: §\{1,2\}\setminus\{2,3\}=\{1\}§."""),
    ),
    judge = listOf(
        j("""§\{1,2\}\setminus\{2,3\}=\{1\}§""", true, """Platí, §1\in A§ a §1\notin B§; §2§ v §B§ je.""", """Rozdiel nechá prvky §A§, ktoré nie sú v §B§. Prvok §3§ v §A§ nie je, preto výsledok nezmení."""),
        j("""§A\setminus B=B\setminus A§ pre všetky §A,B§""", false, """Neplatí, pre §A=\{1\}§, §B=\emptyset§ je ľavá strana §\{1\}§, pravá §\emptyset§.""", """Rozdiel nie je komutatívny. Stačí jeden protipríklad: §\{1\}\setminus\emptyset=\{1\}§, ale §\emptyset\setminus\{1\}=\emptyset§."""),
        j("""V §\mathcal{U}=\{1,2,3\}§ je §\overline{\{1\}}=\{2,3\}§""", true, """Platí, doplnok berie zvyšok univerza.""", """Doplnok sú všetky prvky univerza, ktoré v §A§ nie sú. Tu §2§ a §3§."""),
        j("""§x\in A\cup B§ práve keď §x\in A\wedge x\in B§""", false, """Neplatí, zjednotenie je disjunkcia §\vee§, konjunkcia patrí prieniku.""", """Zámena §\cup§ a §\cap§. Zjednotenie chce, aby §x§ bolo aspoň v jednej z množín."""),
    ),
    units = listOf("Doplnok §\\overline{A}§: §x\\in\\mathcal{U}§ a §x\\notin A§", "Prienik: §x\\in A\\wedge x\\in B§", "Zjednotenie: §x\\in A\\vee x\\in B§", "Rozdiel: §x\\in A\\wedge x\\notin B§", "Všetko ako §\\{x\\in\\mathcal{U}\\mid\\dots\\}§"),
    ask = "Buďte §A,B\\subseteq\\mathcal{U}§. Definujte doplněk množiny §A§, průnik množin §A,B§, sjednocení množin §A,B§ a rozdíl množin §A,B§.",
    question = "Platí to o množinových operáciách?",
    short = "množinových operácií",
)

val mzKart = Concept(
    id = "dml-mz-kart", name = "Karteziánsky súčin", src = "Skriptá, Definice 5.4",
    czech = listOf(
        p("""Buďte §A,B\subseteq\mathcal{U}§. ==Uspořádanou== dvojici prvků §a\in A,b\in B§ značíme §(a,b)§, ta již ale obecně neleží v universu §\mathcal{U}§, ale v universu"""),
        b("""\mathcal{U}\times\mathcal{U}:=\{(a,b)\mid\softbreak[1] a\in\mathcal{U}\wedge b\in\mathcal{U}\}."""),
        p("""**Kartézský součin** množin §A§ a §B§ definujeme jako"""),
        b("""A\times B:=\{(a,b)\mid\softbreak[1] a\in A\wedge b\in B\}"""),
    ),
    why = listOf(
        w("""uspořádanou dvojici""", """Na poradí záleží: §(1,2)\neq(2,1)§. Preto §A\times B\neq B\times A§ vo všeobecnosti."""),
        w("""v universu §\mathcal{U}\times\mathcal{U}§""", """Súčin je v inom univerze než §A§ a §B§. Výraz ako §X\setminus(Y\times Z)§ preto nie je definovaný (Řešení Cvičení 5.5 c)."""),
        w("""§a\in A\wedge b\in B§""", """Dvojica §(u,v)§ je v §A\times B§ práve keď §u\in A§ a §v\in B§. Tak sa súčiny prepisujú v dôkazoch."""),
    ),
    judge = listOf(
        j("""§\{1,2\}\times\{a\}=\{(1,a),(2,a)\}§""", true, """Platí, prvá zložka z §\{1,2\}§, druhá z §\{a\}§.""", """Súčin páruje každý prvok prvej množiny s každým prvkom druhej. Tu sú dve dvojice."""),
        j("""§A\times\emptyset=\emptyset§""", true, """Platí, druhú zložku nie je odkiaľ vziať (Řešení Cvičení 5.5 e).""", """Dvojica potrebuje §b\in\emptyset§, a také §b§ neexistuje. Súčin s prázdnou množinou je prázdny."""),
        j("""§\{1,2\}\times\{3\}=\{3\}\times\{1,2\}§""", false, """Neplatí, §(1,3)§ je vľavo, ale vpravo sú dvojice §(3,\cdot)§.""", """Dvojice sú usporiadané. §(1,3)\neq(3,1)§, preto poradie činiteľov rozhoduje."""),
        j("""§|A\times B|=|A|+|B|§ pre konečné §A,B§""", false, """Neplatí, súčin má §|A|\cdot|B|§ prvkov (Tvrzení 5.2).""", """Každý z §|A|§ prvkov ide s každým z §|B|§ prvkov, voľby sa násobia."""),
    ),
    units = listOf("Usporiadaná dvojica §(a,b)§ leží v §\\mathcal{U}\\times\\mathcal{U}§", "§A\\times B=\\{(a,b)\\mid a\\in A\\wedge b\\in B\\}§"),
    ask = "Pro neprázdné množiny §A,B§ definujte kartézský součin §A\\times B§.",
    question = "Platí to o karteziánskom súčine?",
    short = "karteziánskeho súčinu",
)

val mzVice = Concept(
    id = "dml-mz-vice", name = "Zjednotenie, prienik a súčin viacerých množín", src = "Skriptá, Definice 5.5",
    czech = listOf(
        p("""Uvažujme množiny §A_1,A_2,\ldots,A_n\subseteq\mathcal{U}§. Zobecnění operací §\cup,\cap§ a §\times§ §n§ množin definujeme (a značíme analogicky k sumám a produktům pro sčítání, resp. násobení) jako:"""),
        // each formula is wider than a card at 320 dp: displayed, soft breaks after the bar of the set-builder and the quantifier (Teo, 18. 11.)
        b("""\bigcup_{k=1}^n A_k=\{x\in\mathcal{U}\mid\softbreak[1](\exists k\in\{1,2,\ldots,n\})\softbreak[0](x\in A_k)\}"""),
        b("""\bigcap_{k=1}^n A_k=\{x\in\mathcal{U}\mid\softbreak[1](\forall k\in\{1,2,\ldots,n\})\softbreak[0](x\in A_k)\}"""),
        b("""A_1\times A_2\times\ldots\times A_n\softbreak[1]=\{(a_1,a_2,\ldots,a_n)\mid\softbreak[1]a_1\in A_1\wedge a_2\in A_2\softbreak[2]\wedge\ldots\wedge a_n\in A_n\}"""),
        i("""Pokud §A_i=A§ pro všechna §i\in\{1,2,\ldots,n\}§, pak značíme §A\times A\times\ldots\times A=A^n§ (§n§-tá (kartézská) **mocnina množiny** §A§)."""),
    ),
    why = listOf(
        w("""§(\exists k)(x\in A_k)§""", """Zjednotenie: §x§ stačí v jednej z množín. Bez slova „zjednotenie“ v definícii, len kvantifikátor (písomka 25. 11. 2022)."""),
        w("""§(\forall k)(x\in A_k)§""", """Prienik: §x§ musí byť vo všetkých."""),
        w("""§A^n§""", """§n§-tice prvkov §A§, napr. §\mathbb{R}^2=\mathbb{R}\times\mathbb{R}§ z LA1."""),
    ),
    judge = listOf(
        j("""§\bigcap_{k=1}^3\{1,\dots,k\}=\{1\}§""", true, """Platí, len §1§ je vo všetkých troch (Řešení Cvičení 5.7 b).""", """Prienik chce všetky množiny naraz. §\{1\}§ je najmenšia z nich a ostatné ju obsahujú."""),
        j("""§\bigcup_{k=1}^3\{1,\dots,k\}=\{1\}§""", false, """Neplatí, zjednotenie je §\{1,2,3\}§.""", """Zjednotenie chce aspoň jednu množinu, nie všetky. Prvok §3§ je v §\{1,2,3\}§."""),
        j("""§(1,2,3)\in\{1\}\times\{2\}\times\{3\}§""", true, """Platí, každá zložka je z príslušnej množiny.""", """Trojica patrí do súčinu, keď §a_1\in A_1§, §a_2\in A_2§ a §a_3\in A_3§. Tu áno."""),
    ),
    units = listOf("Obe ako množiny §\\{x\\in\\mathcal{U}\\mid\\dots\\}§", "§\\bigcup§: existuje §k\\in\\{1,\\dots,n\\}§ s §x\\in A_k§", "§\\bigcap§: pre všetky §k\\in\\{1,\\dots,n\\}§ je §x\\in A_k§"),
    extra = listOf("Súčin §n§ množín: §n§-tice", "§A^n§"),
    ask = "Buďte §A_1,\\dots,A_n\\subseteq\\mathcal{U}§. Definujte množiny §\\bigcup_{k=1}^nA_k§, §\\bigcap_{k=1}^nA_k§ a to bez zmínění jakékoli další množinové operace.",
    question = "Platí to pre viac množín?",
    short = "operácií s viacerými množinami",
)

val mzDisjunktni = Concept(
    id = "dml-mz-disjunktni", name = "Disjunktné množiny", src = "Skriptá, Definice 5.6",
    czech = listOf(
        p("""O dvojici množin §A,B§ řekneme, že jsou **disjunktní**, pokud mají tzv. prázdný průnik, §A\cap B=\emptyset§."""),
        p("""O množinách §A_1,A_2,\dots,A_n§ §(n\in\mathbb{N})§ řekneme, že jsou **všechny navzájem disjunktní** (případně **po dvou disjunktní**), pokud"""),
        b("""(\forall k,\ell\in\{1,2,\dots,n\})\softbreak(k\neq\ell\Rightarrow A_k\cap A_{\ell}=\emptyset)."""),
    ),
    why = listOf(
        w("""§k\neq\ell\Rightarrow§""", """Porovnávaš každú dvojicu rôznych množín. Bez §k\neq\ell§ by si chcel §A_k\cap A_k=\emptyset§, teda všetky prázdne."""),
        w("""po dvou""", """Nestačí prázdny prienik všetkých: §\{1,2\},\{2,3\},\{1,3\}§ majú §\bigcap=\emptyset§, ale po dvoch disjunktné nie sú (Otázka 5.1)."""),
    ),
    judge = listOf(
        j("""§\{1,2\},\{3\},\{4,5\}§ sú po dvoch disjunktné""", true, """Platí, každé dve majú prázdny prienik.""", """Sú, žiadne číslo nie je v dvoch množinách naraz. Skontroluj všetky tri dvojice."""),
        j("""§\{1,2\},\{2,3\},\{1,3\}§ sú po dvoch disjunktné""", false, """Neplatí, §\{1,2\}\cap\{2,3\}=\{2\}§ (Otázka 5.1).""", """Prázdny prienik všetkých troch nestačí. Definícia chce prázdny prienik každej dvojice."""),
        j("""§A§ a §\overline{A}§ sú disjunktné""", true, """Platí, §A\cap\overline{A}=\emptyset§ (Věta 5.1).""", """Prvok nemôže byť v §A§ aj mimo §A§ naraz: §x\in A\wedge x\notin A§ je kontradikcia."""),
    ),
    units = listOf("Po dvoch: pre všetky §k\\neq\\ell§ je §A_k\\cap A_\\ell=\\emptyset§"),
    extra = listOf("Dve množiny: §A\\cap B=\\emptyset§"),
    ask = "Definujte, kdy jsou množiny §A_1,\\dots,A_n§ všechny navzájem disjunktní.",
    question = "Platí to o disjunktnosti?",
    short = "disjunktných množín",
)

val mzZobrazeni = Concept(
    id = "dml-mz-zobrazeni", name = "Zobrazenie, definičný obor, obor hodnôt", src = "Skriptá, Definice 5.7",
    czech = listOf(
        p("""Nechť §A§ a §B§ jsou libovolné neprázdné množiny. Množinu §f\subseteq A\times B§ takovou, že pro každé §a\in A§ existuje **nejvýše jedno** §b\in B§ tak, že §(a,b)\in f§, nazveme **zobrazením z §A§ do §B§**. Vztah §(a,b)\in f§ zapisujeme jako §f(a)=b§. Podobně, §f\subseteq A\times B§ zapisujeme §f:A\to B§."""),
        p("""Pro §f:A\to B§ definujeme **definiční obor** §D_f\subseteq A§ a **obor hodnot** §H_f\subseteq B§ jako:"""),
        b("""D_f:=\{a\in A\mid\softbreak[1](\exists b\in B)(f(a)=b)\},\quad\softbreak H_f:=\{b\in B\mid\softbreak[1](\exists a\in A)(f(a)=b)\}."""),
        p("""Jak se dočteme v budoucí Kapitole 7, pojem zobrazení §f:A\to B§ je speciálním případem binární relace §(f,A,B)§."""),
        p("""Řekneme, že zobrazení §f:A\to B§ je **totální**, pokud platí §D_f=A§. Totálním zobrazením (z) §A§ do §B§ je tedy každá podmnožina §f\subseteq A\times B§ taková, že pro každé §a\in A§ existuje **právě jedno** §b\in B§ splňující §(a,b)\in f§. Nebude-li řečeno jinak, v kurzu BI-DML.21 budeme uvažovat nadále pouze totální zobrazení."""),
    ),
    why = listOf(
        w("""množinu §f\subseteq A\times B§""", """Zobrazenie je množina dvojíc (graf), nie postup výpočtu."""),
        w("""nejvýše jedno""", """Žiadny prvok §A§ nesmie mať dva obrazy. Prvok nemusí mať žiadny obraz, potom §a\notin D_f§."""),
        w("""totální, §D_f=A§""", """Každý prvok §A§ má práve jeden obraz. V BI-DML je každé zobrazenie totálne, aj pri počítaní zobrazení v testoch (Spěvák, 5. prednáška)."""),
        w("""§H_f\subseteq B§""", """Obor hodnôt môže byť menší než §B§. Sú to len zasiahnuté prvky: §n\mapsto n+1§ na §\mathbb{N}§ má §H_f=\mathbb{N}\setminus\{1\}§ (Cvičení 5.8 b)."""),
    ),
    judge = listOf(
        j("""§\{(1,a),(2,a)\}\subseteq\{1,2\}\times\{a,b\}§ je totálne zobrazenie""", true, """Je, každý prvok §\{1,2\}§ má práve jeden obraz.""", """Dva prvky smú mať ten istý obraz, to definícia nezakazuje. Zakazuje len dva obrazy jedného prvku."""),
        j("""§\{(1,a),(1,b),(2,a)\}§ je zobrazenie z §\{1,2\}§ do §\{a,b\}§""", false, """Nie je, prvok §1§ má dva obrazy.""", """Definícia chce pre každé §a§ najviac jedno §b§. Tu má §1§ obrazy §a§ aj §b§."""),
        j("""§\{(1,a)\}§ je totálne zobrazenie z §\{1,2\}§ do §\{a,b\}§""", false, """Nie je totálne, §2§ nemá obraz, §D_f=\{1\}\neq\{1,2\}§.""", """Zobrazením (netotálnym) je, ale totálne chce obraz pre každý prvok §A§. Prvok §2§ ho nemá."""),
        j("""§f(n)=\sqrt{n}§ je zobrazenie §\mathbb{Z}\to\mathbb{Z}§ v zmysle BI-DML""", false, """Nie je, pre §n=-1§ či §n=2§ obraz v §\mathbb{Z}§ neexistuje, a v BI-DML sú zobrazenia totálne.""", """Predpis musí dať obraz v §B§ pre každé §a\in A§. Záporné čísla a čísla, ktoré nie sú druhými mocninami celých čísel, celočíselnú odmocninu nemajú."""),
    ),
    // the test asks for a map from A to A (B = A in Definice 5.7), so the check says A (Zásada 4)
    units = listOf("§A§ neprázdna, §f\\subseteq A\\times A§", "Pre každé §a\\in A§ najviac jedno §b\\in A§ s §(a,b)\\in f§", "Totálne: §D_f=A§, práve jedno §b\\in A§; v BI-DML vždy"),
    extra = listOf("Všeobecne §f:A\\to B§ pre dve neprázdne množiny", "Zápis §f(a)=b§, §f:A\\to A§", "§D_f§ a §H_f§ cez existenčný kvantifikátor"),
    ask = "Pro množinu §A§ definujte pojem zobrazení §f§ z §A§ do §A§.",
    question = "Platí to o zobrazení?",
    short = "zobrazenia",
)

val mzObraz = Concept(
    id = "dml-mz-obraz", name = "Obraz a vzor množiny, zúženie", src = "Skriptá, Definice 5.8",
    czech = listOf(
        p("""Nechť §A§ a §B§ jsou libovolné neprázdné množiny a §f:A\rightarrow B§. Definujeme"""),
        i("""**obraz množiny** §M\subseteq A§ při §f:A\rightarrow B§: §f[M]:=\{f(a)\mid a\in M\}§,"""),
        i("""**vzor množiny** §N\subseteq B§ při §f:A\rightarrow B§: §f^{-1}[N]:=\{a\in A\mid f(a)\in N\}§,"""),
        i("""**zúžení (restrikci) zobrazení** §f:A\rightarrow B§ na množinu §M\subseteq A§: §f|_M:M\rightarrow B,\ f|_M(a):=f(a)§ pro každé §a\in M§."""),
    ),
    why = listOf(
        w("""§f[M]=\{f(a)\mid a\in M\}§""", """Obraz sú hodnoty na prvkoch §M§. Obor hodnôt je §H_f=f[A]§."""),
        w("""§f^{-1}[N]=\{a\in A\mid f(a)\in N\}§""", """Vzor množiny existuje vždy, aj keď §f§ nemá inverziu. Je to len zápis, nie inverzné zobrazenie."""),
        w("""§f|_M§""", """Ten istý predpis, menší definičný obor. Zúžením sa z neprostého zobrazenia môže stať prosté."""),
    ),
    judge = listOf(
        j("""Pre §f(n)=n^2§ na §\mathbb{Z}§ je §f^{-1}[\{4\}]=\{-2,2\}§""", true, """Platí, §f(-2)=f(2)=4§ a nič iné na §4§ nevedie.""", """Vzor množiny zbiera všetky prvky s obrazom v §N§, nie jeden. Aj §-2§ má obraz §4§."""),
        j("""Pre §f(n)=n^2§ na §\mathbb{Z}§ vzor §f^{-1}[\{3\}]§ nie je definovaný""", false, """Je definovaný a je prázdny: §\emptyset§.""", """§f^{-1}[N]§ nie je inverzné zobrazenie, je to množina. Ak sa na §3§ nič nezobrazí, je prázdna."""),
        j("""§f[\{1,2\}]=\{3\}§ pre §f(1)=f(2)=3§""", true, """Platí, obraz je množina hodnôt, §3§ len raz.""", """Množina neopakuje prvky. Obe hodnoty sú §3§, preto §f[\{1,2\}]=\{3\}§."""),
    ),
    units = listOf("Obraz §f[M]=\\{f(a)\\mid a\\in M\\}§ pre §M\\subseteq A§", "Vzor §f^{-1}[N]=\\{a\\in A\\mid f(a)\\in N\\}§ pre §N\\subseteq B§"),
    extra = listOf("Zúženie §f|_M:M\\to B§, §f|_M(a)=f(a)§"),
    ask = "Pro §f:A\\to B§ definujte obraz množiny §M\\subseteq A§ a vzor množiny §N\\subseteq B§.",
    question = "Platí to o obraze a vzore?",
    short = "obrazu a vzoru množiny",
)

val mzInjSurj = Concept(
    id = "dml-mz-injsurj", name = "Injektívne, surjektívne, bijektívne", src = "Skriptá, Definice 5.9",
    czech = listOf(
        p("""Nechť §A§ a §B§ jsou libovolné neprázdné množiny. Řekneme, že §f:A\rightarrow B§ je"""),
        i("""**injektivní** **(prosté)**, pokud platí §(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)§"""),
        i("""**surjektivní** **(na)**, pokud platí §(\forall y\in B)(\exists x\in A)(f(x)=y)§"""),
        i("""**bijektivní** **(vzájemně jednoznačné)**, nebo také **bijekce**, pokud je současně injektivní a surjektivní."""),
    ),
    why = listOf(
        w("""§(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)§""", """Rovnaký obraz dovolí len rovnaký prvok. Dôkaz: vezmi ľubovoľné §x,y§ s §f(x)=f(y)§ a odvoď §x=y§. Vyvrátenie: dva rôzne prvky s rovnakým obrazom. Ekvivalentne §x\neq y\Rightarrow f(x)\neq f(y)§ (Poznámka 5.5)."""),
        w("""§(\forall y\in B)(\exists x\in A)§""", """Každý prvok cieľa má vzor. Dôkaz: vzorec pre vzor ľubovoľného §y§, ktorý leží v §A§. Vyvrátenie: jedno §y§ bez vzoru."""),
        w("""současně injektivní a surjektivní""", """Bijekcia páruje prvky §A§ a §B§ jedna k jednej. Práve vtedy existuje inverzia (Věta 5.4)."""),
    ),
    judge = listOf(
        j("""§f(n)=n+1§, §f:\mathbb{N}_0\to\mathbb{N}_0§ je injektívne""", true, """Je, z §n+1=n'+1§ plynie §n=n'§ (Řešení Cvičení 5.9 b).""", """Z rovnosti obrazov sa dá odvodiť rovnosť argumentov, nič viac definícia nechce."""),
        j("""§f(n)=n+1§, §f:\mathbb{N}_0\to\mathbb{N}_0§ je surjektívne""", false, """Nie je, §0§ nemá vzor: §n=-1\notin\mathbb{N}_0§.""", """Na §\mathbb{Z}§ by bolo, na §\mathbb{N}_0§ nie. Rozhoduje aj množina, na ktorej je zobrazenie dané."""),
        j("""§f(x)=x^3§, §f:\mathbb{R}\to\mathbb{R}§ je bijekcia""", true, """Je, tretia odmocnina reálneho čísla existuje a je jediná (Řešení Cvičení 5.9 e).""", """Na §\mathbb{Z}§ by surjektívne nebolo, na §\mathbb{R}§ je: vzor §y§ je §\sqrt[3]{y}§."""),
        j("""§f(n)=(n^2,n^2+n)§, §f:\mathbb{Z}\to\mathbb{Z}^2§ nie je injektívne, lebo obsahuje §n^2§""", false, """Je injektívne: odčítaním rovníc vyjde §n=n'§ (Řešení Cvičení 5.10 b).""", """„Vidím §n^2§, teda nie je prosté“ je chybná úvaha. Druhá zložka mínus prvá dáva §n§ samotné."""),
        j("""§f(1)\neq f(2)§, teda §f:\mathbb{Z}\to\mathbb{Z}§ je injektívne""", false, """Nie, jeden príklad nič nedokazuje (Varování 5.4).""", """Toto je „dôkaz príkladom“. Injektivita je tvrdenie pre všetky dvojice, treba ho dokázať všeobecne."""),
    ),
    units = listOf("§A,B§ neprázdne, §f:A\\to B§", "Injektívne: §(\\forall x,y\\in A)(f(x)=f(y)\\Rightarrow x=y)§", "Surjektívne: §(\\forall y\\in B)(\\exists x\\in A)(f(x)=y)§", "Bijektívne: injektívne aj surjektívne"),
    trap = Trap("Injektívne znamená, že každý prvok má práve jeden obraz.", "To je definícia totálneho zobrazenia, nie injektivity. Injektivita hovorí, že rôzne prvky majú rôzne obrazy.", "Definice 5.7 a 5.9"),
    ask = "Pro zobrazení §f:A\\to B§ definujte pojmy injektivní, surjektivní a bijektivní.",
    question = "Platí to o injektivite a surjektivite?",
    short = "injekcie, surjekcie a bijekcie",
)

val mzSlozeni = Concept(
    id = "dml-mz-slozeni", name = "Rovnosť a zloženie zobrazení", src = "Skriptá, Definice 5.10",
    czech = listOf(
        p("""Zobrazení §f:A\rightarrow B§ a §g:C\rightarrow D§ nazveme **shodná**, právě když platí"""),
        b("""(A=C)\wedge(B=D)\softbreak[1]\wedge(\forall a\in A)(f(a)=g(a))."""),
        p("""Pro zobrazení §f:B\rightarrow C§ a §g:A\rightarrow B§ definujeme jejich **složení (složené zobrazení)**"""),
        b("""(f\circ g):A\to C,\ \softbreak(f\circ g)(a):=f(g(a))\softbreak[1]\text{ pro všechna }a\in A."""),
    ),
    why = listOf(
        w("""§(A=C)\wedge(B=D)§""", """Rovnaký predpis nestačí, rovnať sa musia aj množiny. §n\mapsto n^2§ na §\mathbb{Z}§ a na §\mathbb{N}§ sú rôzne zobrazenia."""),
        w("""§f:B\rightarrow C§ a §g:A\rightarrow B§""", """Cieľ §g§ je definičný obor §f§. Inak §f(g(a))§ nemusí dávať zmysel (písomka 25. 11. 2022)."""),
        w("""§(f\circ g)(a):=f(g(a))§""", """Najprv §g§, potom §f§: zápis je opačne než šípky na obrázku (Spěvák, 5. prednáška)."""),
    ),
    judge = listOf(
        j("""Pre §g(n)=n+1§, §f(n)=2n§ na §\mathbb{Z}§ je §(f\circ g)(3)=8§""", true, """Platí, §g(3)=4§, potom §f(4)=8§.""", """Výsledok je §8§. Zloženie čítaj sprava: najprv §g§, potom §f§; opačne by vyšlo §g(f(3))=7§."""),
        j("""§f\circ g=g\circ f§ pre všetky §f,g:\mathbb{Z}\to\mathbb{Z}§""", false, """Neplatí, §f(n)=2n§, §g(n)=n+1§: §(f\circ g)(0)=2§, §(g\circ f)(0)=1§.""", """Skladanie nie je komutatívne. Stačí jeden protipríklad v jednom bode."""),
        j("""§f:\{1\}\to\{a\}§ a §g:\{1\}\to\{a,b\}§ s §f(1)=g(1)=a§ sú shodné""", false, """Nie sú, majú rôzne cieľové množiny §\{a\}\neq\{a,b\}§.""", """Definícia chce aj §B=D§. Rovnaké hodnoty nestačia."""),
    ),
    units = listOf("Zloženie §f\\circ g:A\\to C§ pre §g:A\\to B§, §f:B\\to C§", "§(f\\circ g)(a)=f(g(a))§ pre všetky §a\\in A§"),
    extra = listOf("Shodné: rovnaké množiny a §f(a)=g(a)§ pre všetky §a§"),
    ask = "Definujte (korektně, i se správnou volbou dotčených množin) pojem složení zobrazení.",
    question = "Platí to o zložení a rovnosti zobrazení?",
    short = "rovnosti a zloženia zobrazení",
)

val mzIdZobr = Concept(
    id = "dml-mz-idzobr", name = "Identické zobrazenie", src = "Skriptá, Definice 5.11",
    czech = listOf(
        p("""**Identické zobrazení** **(identita)** na množině §A§ je zobrazení §\mathrm{id}_A:A\rightarrow A§ splňující"""),
        b("""(\forall x\in A)(\mathrm{id}_A(x)=x)."""),
    ),
    why = listOf(
        w("""na množině §A§, §\mathrm{id}_A:A\rightarrow A§""", """Identita patrí k jednej množine. §\mathrm{id}_A§ a §\mathrm{id}_B§ pre §A\neq B§ sú rôzne zobrazenia."""),
        w("""§(\forall x\in A)§""", """Každý prvok sa zobrazí sám na seba, nielen niektoré."""),
    ),
    judge = listOf(
        j("""§f(n)=|n|§ na §\mathbb{N}§ je §\mathrm{id}_{\mathbb{N}}§""", true, """Je, pre §n\in\mathbb{N}§ je §|n|=n§, a množiny sedia.""", """Iný predpis neznamená iné zobrazenie. Na §\mathbb{N}§ dáva §|n|§ vždy §n§."""),
        j("""§f(n)=|n|§ na §\mathbb{Z}§ je §\mathrm{id}_{\mathbb{Z}}§""", false, """Nie je, §f(-1)=1\neq -1§.""", """Identita musí vrátiť každý prvok nezmenený. Záporné čísla sa zmenia."""),
        j("""§f\circ\mathrm{id}_A=f§ pre §f:A\to B§""", true, """Platí, §f(\mathrm{id}_A(a))=f(a)§ (Pozorování 5.1).""", """Identita nič nezmení, je neutrálna pri skladaní. Pozor na stranu: vpravo musí byť §\mathrm{id}_A§, vľavo §\mathrm{id}_B§."""),
    ),
    units = listOf("§\\mathrm{id}_A:A\\to A§", "Pre všetky §x\\in A§ je §\\mathrm{id}_A(x)=x§"),
    ask = "Definujte (korektně, i se správnou volbou dotčených množin) pojem identické zobrazení.",
    question = "Platí to o identite?",
    short = "identického zobrazenia",
)

val mzMocnina = Concept(
    id = "dml-mz-mocnina", name = "Mocnina zobrazenia", src = "Skriptá, Definice 5.12",
    czech = listOf(
        p("""Buďte §f:A\rightarrow A§ zobrazení a §n\in\mathbb{N}_0§. **Mocninu (§n§tou) zobrazení** §f§ definujeme pomocí skládání a identity jako:"""),
        b("""f^0:=\mathrm{id}_A,\quad\softbreak f^1:=f,\quad\softbreak f^n:=f\circ f\circ\dots\circ f\softbreak[1]\ (n\text{krát})\text{ pro }n\geq 2."""),
    ),
    why = listOf(
        w("""§f:A\rightarrow A§""", """Skladať so sebou sa dá len zobrazenie z množiny do tej istej množiny."""),
        w("""§f^0:=\mathrm{id}_A§""", """Nulté opakovanie neurobí nič, ako §x^0=1§ pri číslach."""),
        w("""§f\circ f\circ\dots\circ f§""", """Mocnina je opakované skladanie, nie umocnenie hodnoty: §f^2(x)=f(f(x))§, nie §f(x)^2§."""),
    ),
    judge = listOf(
        j("""Pre §f(n)=n+3§ na §\mathbb{Z}§ je §f^2(1)=7§""", true, """Platí, §f(f(1))=f(4)=7§.""", """§f^2§ je dvakrát §f§ za sebou. §f(1)^2=16§ by bolo umocnenie hodnoty, to mocnina zobrazenia nie je."""),
        j("""Pre §f(n)=2n§ na §\mathbb{Z}§ je §f^2(3)=36§""", false, """Neplatí, §f^2(3)=f(f(3))=f(6)=12§.""", """§36=f(3)^2§ je umocnená hodnota. Mocnina zobrazenia skladá: §f(f(3))§."""),
        j("""§f^0(x)=x§ pre každé §x\in A§""", true, """Platí, §f^0=\mathrm{id}_A§.""", """Nulté opakovanie je identita, vráti prvok nezmenený."""),
    ),
    units = listOf("§f:A\\to A§, §n\\in\\mathbb{N}_0§", "§f^0=\\mathrm{id}_A§", "§f^1=f§", "§f^n=f\\circ\\dots\\circ f§ (§n§-krát) pre §n\\geq 2§"),
    ask = "Jsou-li §f§ zobrazení z §A§ do §A§, pro každé §n\\in\\mathbb{N}_0§ definujte mocninu §f^n§.",
    question = "Platí to o mocnine zobrazenia?",
    short = "mocniny zobrazenia",
)

val mzInverze = Concept(
    id = "dml-mz-inverze", name = "Inverzné zobrazenie", src = "Skriptá, Definice 5.13",
    czech = listOf(
        p("""Buď §f:A\rightarrow B§ zobrazení. Jestliže k němu existuje zobrazení §g:B\rightarrow A§ splňující"""),
        b("""f\circ g=\mathrm{id}_B\ \wedge\ g\circ f=\mathrm{id}_A"""),
        p("""(tedy takové, jehož složením s §f§ ==v libovolném pořadí== dostaneme identitu), pak řekneme, že §f§ je **invertibilní** a §g§ nazveme **inverzním zobrazením** k §f§ (nebo krátce jen **inverzí** (k) §f§) a značíme jej §f^{-1}§."""),
    ),
    why = listOf(
        w("""§g:B\rightarrow A§""", """Inverzia ide opačným smerom, z cieľa späť."""),
        w("""§f\circ g=\mathrm{id}_B\wedge g\circ f=\mathrm{id}_A§""", """Obe rovnosti. Len jedna nestačí: §f\circ g§ môže byť identita a §g\circ f§ nie (písomka 6. 12. 2024)."""),
        w("""invertibilní""", """Invertibilné je práve bijektívne zobrazenie (Věta 5.4)."""),
    ),
    judge = listOf(
        j("""§f(x)=2x+1§ na §\mathbb{R}§ má inverziu §f^{-1}(y)=\tfrac{y-1}{2}§""", true, """Má, obe zloženia dajú identitu.""", """Uvedené zobrazenie je inverziou. Platí §f(f^{-1}(y))=y§ aj §f^{-1}(f(x))=x§."""),
        j("""§f(n)=2n+1§ na §\mathbb{Z}§ je invertibilné""", false, """Nie je, §2§ nemá vzor v §\mathbb{Z}§, teda nie je surjektívne.""", """Vzorec §\tfrac{y-1}{2}§ nedáva celé číslo pre párne §y§. Invertibilné je len bijektívne zobrazenie (Věta 5.4)."""),
        j("""Ak §f\circ g=\mathrm{id}_B§, potom §g=f^{-1}§""", false, """Neplatí, treba aj §g\circ f=\mathrm{id}_A§.""", """Príklad: §g:\{1\}\to\{1,2\}§, §g(1)=1§ a §f:\{1,2\}\to\{1\}§. Potom §f\circ g=\mathrm{id}§, ale §g\circ f§ pošle §2§ na §1§."""),
    ),
    units = listOf("§g:B\\to A§", "§f\\circ g=\\mathrm{id}_B§", "§g\\circ f=\\mathrm{id}_A§"),
    extra = listOf("Značenie §f^{-1}§, §f§ je invertibilné"),
    ask = "Pro zobrazení §f:A\\to B§ definujte inverzní zobrazení.",
    question = "Platí to o inverzii?",
    short = "inverzného zobrazenia",
)

val mzMohutnost = Concept(
    id = "dml-mz-mohutnost", name = "Mohutnosť", src = "Skriptá, Definice 5.14",
    czech = listOf(
        p("""Buďte §A,B§ neprázdné množiny. Řekneme, že"""),
        i("""množiny §A,B§ mají **stejnou mohutnost** (značeno §|A|=|B|§), právě když ==existuje== bijekce §f:A\to B§."""),
        i("""**mohutnost** množiny §A§ **je menší nebo rovna** mohutnosti množiny §B§ (značeno §|A|\leq|B|§), právě když existuje prosté zobrazení §f:A\to B§."""),
        i("""**mohutnost** §A§ **je ostře (striktně) menší** než mohutnost §B§ (značeno §|A|<|B|§), právě když §|A|\leq|B|§, ale neplatí §|A|=|B|§."""),
        i("""Dále definujeme: §|\emptyset|\leq|A|§, §|\emptyset|<|A|§, §|\emptyset|\leq|\emptyset|§ a §|\emptyset|=|\emptyset|§."""),
        p("""Značením §|A|\neq|B|§ vyjádříme případ, kdy neplatí §|A|=|B|§."""),
    ),
    why = listOf(
        w("""existuje bijekce""", """Rovnako veľké = dajú sa spárovať jedna k jednej. Tak počíta dieťa na prstoch (Poznámka 5.9)."""),
        w("""existuje prosté zobrazení""", """Menšia alebo rovná = vojde sa do druhej bez zlepenia dvoch prvkov."""),
        w("""ale neplatí §|A|=|B|§""", """Ostro menšia: prosté zobrazenie existuje, bijekcia žiadna. Nestačí, že jedno konkrétne prosté nie je na."""),
    ),
    judge = listOf(
        j("""§|\mathbb{N}|=|\mathbb{Z}|§""", true, """Platí, existuje bijekcia §\mathbb{N}\to\mathbb{Z}§ (Důsledek 5.1).""", """Že je §\mathbb{N}\subsetneq\mathbb{Z}§, nič nehovorí: pre nekonečné množiny môže mať vlastná podmnožina rovnakú mohutnosť (Tvrzení 5.5)."""),
        j("""§|\mathbb{N}|<|\mathbb{Z}|§, lebo §n\mapsto n§ je prosté a nie je na""", false, """Neplatí, ostro menšia chce, aby žiadna bijekcia neexistovala.""", """Jedno neprosté či nesurjektívne zobrazenie o mohutnosti nerozhoduje. Bijekcia §\mathbb{N}\to\mathbb{Z}§ existuje."""),
        j("""§|\{1,2,3\}|\leq|\{a,b,c,d\}|§""", true, """Platí, napr. §1\mapsto a§, §2\mapsto b§, §3\mapsto c§ je prosté.""", """Na §|A|\leq|B|§ stačí jedno prosté zobrazenie. Tu ho vieš napísať priamo."""),
        j("""§|\mathbb{Q}|<|\mathbb{R}|§""", true, """Platí, §\mathbb{Q}§ je spočetná a §\mathbb{R}§ nespočetná (Tvrzení 5.4 a Tvrzení 5.6).""", """Racionálne čísla sú husté, ale spočetné. Reálnych je viac: bijekcia §\mathbb{Q}\to\mathbb{R}§ neexistuje."""),
    ),
    units = listOf("§|A|=|B|§: existuje bijekcia §A\\to B§", "§|A|\\leq|B|§: existuje prosté §A\\to B§", "§|A|<|B|§: §|A|\\leq|B|§ a neplatí §|A|=|B|§"),
    extra = listOf("Prázdna množina zvlášť"),
    ask = "Pro neprázdné množiny §A,B§ definujte vztahy §|A|=|B|§, §|A|\\leq|B|§ a §|A|<|B|§.",
    question = "Platí to o mohutnosti?",
    short = "mohutnosti",
)

val mzKonecne = Concept(
    id = "dml-mz-konecne", name = "Konečné a nekonečné množiny", src = "Skriptá, Definice 5.15",
    czech = listOf(
        p("""Množina §A§ se nazývá"""),
        i("""**konečná**, jestliže §A=\emptyset§ (pak píšeme §|A|=0§), nebo existuje takové §m\in\mathbb{N}§, že §|A|=|\{1,2,\ldots,m\}|§ (pak píšeme §|A|=m§)."""),
        i("""**nekonečná**, když není konečná. Nekonečnou množinu §A§ dále nazveme **spočetnou (spočetně nekonečnou)**, jestliže §|A|=|\mathbb{N}|§, **nespočetnou (nespočetně nekonečnou)**, jestliže je nekonečná, ale není spočetná (tedy když §|A|>|\mathbb{N}|§)."""),
    ),
    why = listOf(
        w("""§|A|=|\{1,2,\ldots,m\}|§""", """Konečná = dá sa spočítať do nejakého §m§, teda bijekcia na §\{1,\dots,m\}§. Mohutnosť je potom obyčajný počet prvkov."""),
        w("""§|A|=|\mathbb{N}|§""", """Spočetná = prvky sa dajú očíslovať všetkými prirodzenými číslami."""),
        w("""nekonečná, ale není spočetná""", """Viac než §\mathbb{N}§: napr. §\mathbb{R}§ alebo interval §(0,1)§ (Tvrzení 5.6)."""),
    ),
    judge = listOf(
        j("""§\mathbb{Z}\times\mathbb{Z}§ je spočetná""", true, """Platí, Tvrzení 5.4.""", """Súčin dvoch spočetných množín je spočetný, dvojice sa dajú očíslovať (Hilbertov hotel)."""),
        j("""§\{n^2\mid n\in\mathbb{N}\}§ je konečná, lebo štvorce „rednú“""", false, """Neplatí, štvorcov je nekonečne veľa, §n\mapsto n^2§ je bijekcia z §\mathbb{N}§.""", """Riedkosť neznamená konečnosť. Množina je spočetne nekonečná."""),
        j("""§(0,1)§ je nespočetná""", true, """Platí, Tvrzení 5.6.""", """Interval je ohraničený, ale nespočetný: prvky sa nedajú očíslovať prirodzenými číslami."""),
        j("""§\emptyset§ je nekonečná""", false, """Neplatí, §\emptyset§ je konečná s §|\emptyset|=0§.""", """Definícia ju menuje výslovne ako konečnú."""),
    ),
    units = listOf("Konečná: §\\emptyset§ alebo §|A|=|\\{1,\\dots,m\\}|§ pre §m\\in\\mathbb{N}§", "Nekonečná: nie konečná", "Spočetná: §|A|=|\\mathbb{N}|§", "Nespočetná: nekonečná a nie spočetná"),
    ask = "Definujte konečnou, nekonečnou, spočetnou a nespočetnou množinu.",
    question = "Platí to o konečnosti a spočetnosti?",
    short = "konečnej a nekonečnej množiny",
)

val SetsConcepts = listOf(mzMnozina, mzInkluze, mzOperace, mzKart, mzVice, mzDisjunktni, mzZobrazeni, mzObraz, mzInjSurj, mzSlozeni, mzIdZobr, mzMocnina, mzInverze, mzMohutnost, mzKonecne)

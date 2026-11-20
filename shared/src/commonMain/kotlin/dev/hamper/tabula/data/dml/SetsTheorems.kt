package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Theorems of kapitola 5 the 2nd test leans on, Czech word for word, and the proofs past tests asked for, line by line.
 * Tasks of proofs are the Czech wording of the tests (fit-wiki, transcribed in testy-dml-2.md) or of the textbook.
 */

private const val Venn = "Zadanie chce odvodenie bez Vennovho diagramu. Obrázok nie je dôkaz, treba formuly pre ľubovoľné §x§."
private const val ByExample = "Príklad nič nedokazuje, tvrdenie je pre všetky prvky (Varovanie 5.4). Treba vziať ľubovoľné prvky."
private const val Itself = "Toto je presne tvrdenie, ktoré máš zdôvodniť. Odvolanie na samotné tvrdenie nie je dôkaz, treba ísť z definície."

/* ---- Proofs ------------------------------------------------------------------------------------------------------ */

val pIdentita = Proof(
    "dml-mz-p-identita", "Rovnosť množín odvodením", "Písomka 4. 12. 2025 (variant E), príklad 2 c1",
    wording(listOf(p("""Nechť §A,B,C§ jsou libovolné podmnožiny společného universa §\mathcal{U}§.""")), listOf(p("""Rozhodněte, zda vztah §§(C\setminus B)\cap(C\setminus A)=C\setminus(A\cup B)§§ vždy platí. Tvrzení buď podrobně dokažte odvozením (bez Vennova diagramu), nebo nalezněte protipříklad.""")), core = """(C\setminus B)\cap(C\setminus A)\softbreak[1]=C\setminus(A\cup B)"""),
    "Bez Vennova diagramu.",
    m("""Rovnosť množín: pre každé §x\in\mathcal{U}§ dokážem ekvivalenciu §§\begin{aligned}&x\in(C\setminus B)\cap(C\setminus A)\\\Leftrightarrow\ &x\in C\setminus(A\cup B).\end{aligned}§§ Nech §x\in\mathcal{U}§ je ľubovoľné."""),
    listOf(
        r("Rozpíš ľavú stranu podľa definície prieniku a rozdielu.", 1,
            c("""Z Vennovho diagramu vidno, že ľavá strana je §C§ bez §A§ a §B§.""", Venn),
            c("""§x\in(C\setminus B)\cap(C\setminus A)\Leftrightarrow(x\in C\wedge x\notin B)\wedge(x\in C\wedge x\notin A)§"""),
            c("""§x\in(C\setminus B)\cap(C\setminus A)\Leftrightarrow(x\in C\wedge x\notin B)\vee(x\in C\wedge x\notin A)§""", "Prienik je konjunkcia §\\wedge§, nie disjunkcia. Disjunkciu dáva zjednotenie.")),
        r("Uprav formulu.", 0,
            c("""§\Leftrightarrow(x\in C\wedge x\notin A\wedge x\notin B)§ (komutativita, asociativita a idempotencia §\wedge§)"""),
            c("""§\Leftrightarrow(x\in C\wedge x\notin A)§""", "Stratila sa podmienka §x\\notin B§. Úprava musí byť ekvivalentná, nič nesmie vypadnúť."),
            c("""§\Leftrightarrow x\in C§""", "Podmienky §x\\notin A§ a §x\\notin B§ sa nedajú vynechať, nie sú vždy pravdivé.")),
        r("Ako dostať §A\\cup B§?", 2,
            c("""§\Leftrightarrow(x\in C\wedge\neg(x\in A\wedge x\in B))§""", "De Morgan dáva §\\neg p\\wedge\\neg q\\Leftrightarrow\\neg(p\\vee q)§, so zjednotením, nie s prienikom."),
            c("""§\Leftrightarrow x\in C\setminus(A\cap B)§""", "Prienik by zodpovedal §\\neg(x\\in A\\wedge x\\in B)§, a to je slabšie než §x\\notin A\\wedge x\\notin B§."),
            c("""§\Leftrightarrow(x\in C\wedge\neg(x\in A\vee x\in B))\Leftrightarrow(x\in C\wedge x\notin A\cup B)\Leftrightarrow x\in C\setminus(A\cup B)§ (De Morgan, definícia §\cup§ a §\setminus§)""")),
    ),
    m("""Pre každé §x\in\mathcal{U}§ sú obe príslušnosti ekvivalentné, teda vzťah vždy platí."""),
    listOf("Ľubovoľné §x\\in\\mathcal{U}§ a ekvivalencia príslušností", "Rozpísanie podľa definícií, zvonku dovnútra", "Každý krok je logický zákon (De Morgan), pomenovaný", "Záver: rovnosť platí pre každé §x§", "Žiadny Vennov diagram"),
    goals = listOf("Rozpis ľavej strany", "Spojenie podmienok", "De Morganov zákon"),
)

val pSkladInj = Proof(
    "dml-mz-p-sklad-inj", "Skladanie injekcií, surjekcií a bijekcií", "Skriptá, dôkaz Věty 5.5; Písomka 4. 12. 2025 (variant E), príklad 2b",
    null,
    null,
    m("""Najprv dokážeme injektivitu z definície, potom surjektivitu hľadaním vzoru. Bijektivita vyplynie z týchto dvoch častí."""),
    listOf(
        r("Začni z definície.", 1,
            c("""Pre §A=\{1,2\}§ a konkrétne §\ca{f},\cb{g}§ overím §(\ca{f}\circ \cb{g})(1)\neq(\ca{f}\circ \cb{g})(2)§.""", ByExample),
            c("""Nech §x,y\in A§ sú ľubovoľné a §(\ca{f}\circ \cb{g})(x)=(\ca{f}\circ \cb{g})(y)§."""),
            c("""Podľa Vety 5.5 je §\ca{f}\circ \cb{g}§ prosté.""", Itself)),
        r("Rozpíš zloženie.", 0,
            c("""Podľa definície zloženia je §\ca{f}(\cb{g}(x))=\ca{f}(\cb{g}(y))§."""),
            c("""Podľa definície zloženia je §\cb{g}(\ca{f}(x))=\cb{g}(\ca{f}(y))§.""", "Poradie je naopak: §(\\ca{f}\\circ \\cb{g})(x)=\\ca{f}(\\cb{g}(x))§, najprv §\\cb{g}§ (Definícia 5.10)."),
            c("""Teda §\ca{f}(x)=\ca{f}(y)§.""", "§\\ca{f}§ sa aplikuje na §\\cb{g}(x)§ a §\\cb{g}(y)§, nie na §x,y\\in A§; §\\ca{f}§ je definované na §B§.")),
        r("Použi injektivitu §\\ca{f}§.", 2,
            c("""§\ca{f}§ je prosté, teda §x=y§.""", "Injektivita §\\ca{f}§ dá rovnosť argumentov §\\ca{f}§, a tie sú §\\cb{g}(x)§ a §\\cb{g}(y)§, nie §x§ a §y§."),
            c("""§\cb{g}§ je prosté, teda §\ca{f}(\cb{g}(x))=\ca{f}(\cb{g}(y))§.""", "To je predpoklad, nie nový krok. Injektivita sa používa opačným smerom: z rovnosti obrazov rovnosť argumentov."),
            c("""§\cb{g}(x),\cb{g}(y)\in B§ a §\ca{f}§ je prosté, teda §\cb{g}(x)=\cb{g}(y)§.""")),
        r("Dokonči.", 0,
            c("""§\cb{g}§ je prosté, teda §x=y§. Tým je §\ca{f}\circ \cb{g}§ prosté."""),
            c("""Teda §\ca{f}\circ \cb{g}=\mathrm{id}_A§.""", "Injektivita nehovorí, že sa prvky zobrazia samy na seba. Stačí §x=y§."),
            c("""§\ca{f}§ je prosté, teda §x=y§.""", "Z §\\cb{g}(x)=\\cb{g}(y)§ vyvodí §x=y§ až injektivita §\\cb{g}§; §\\ca{f}§ si už použil.")),
        r("Surjektivita. Ako nájdeš vzor ľubovoľného §z\\in C§?", 0,
            c("""Zo surjektivity §\ca{f}§ existuje §y\in B§ s §\ca{f}(y)=z§. Zo surjektivity §\cb{g}§ existuje §x\in A§ s §\cb{g}(x)=y§. Potom §(\ca{f}\circ \cb{g})(x)=\ca{f}(\cb{g}(x))=\ca{f}(y)=z§, teda §\ca{f}\circ \cb{g}§ je na."""),
            c("""Z injektivity §\ca{f}\circ \cb{g}§ plynie surjektivita.""", "Injektivita o surjektivite nič nehovorí: zobrazenie §\\{1\\}\\to\\{1,2\\}§ je prosté, ale nie je na. Treba nájsť vzor každého §z\\in C§."),
            given = """Surjektivita: nech §\ca{f}§ aj §\cb{g}§ sú na a §z\in C§ je ľubovoľné."""),
        r("Bijektivita. Čo už vieme?", 0,
            c("""Bijekcie sú prosté aj na, podľa prvých dvoch častí je teda §\ca{f}\circ \cb{g}§ prosté aj na, čiže bijekcia."""),
            c("""Stačí, že §\ca{f}\circ \cb{g}§ je prosté.""", "Bijekcia musí byť prostá aj na."),
            given = """Bijektivita: nech §\ca{f}§ aj §\cb{g}§ sú bijekcie."""),
    ),
    m("""Zloženie dvoch prostých zobrazení je prosté, zloženie dvoch zobrazení na je na. Preto je zloženie dvoch bijekcií bijekcia."""),
    listOf("Ľubovoľné §x,y\\in A§ s rovnakým obrazom pri §\\ca{f}\\circ \\cb{g}§", "§\\ca{f}(\\cb{g}(x))=\\ca{f}(\\cb{g}(y))§ podľa definície zloženia", "Injektivita §\\ca{f}§ dá §\\cb{g}(x)=\\cb{g}(y)§, injektivita §\\cb{g}§ dá §x=y§", "Vzor každého prvku cieľovej množiny cez surjektivitu f aj g", "Bijektivita z prvých dvoch bodov"),
    goals = listOf("""Rovnaký obraz pri §\ca{f}\circ \cb{g}§""", "Rozpis zloženia", "Injektivita §\\ca{f}§", "Injektivita §\\cb{g}§", "Surjektivita", "Bijektivita"),
)

val pSkladG = Proof(
    "dml-mz-p-sklad-g", "Z prostého zloženia plynie prosté §g§", "Písomka 2024 (variant N), príklad 2b; Cvičení 5.11 b, d",
    wording(emptyList(), listOf(p("""Existují zobrazení §\ca{f}:B\to C§, §\cb{g}:A\to B§ taková, že §\ca{f}\circ \cb{g}§ je injektivní, ale jedno z §\ca{f},\cb{g}§ není? (Najděte příklad, nebo dokažte, že z injektivity §\ca{f}\circ \cb{g}§ vždy plyne injektivita §\ca{f}§ i §\cb{g}§.)""")), core = """f\circ g\text{ prosté}\ \Rightarrow\ g\text{ prosté},\ \softbreak f\text{ nemusí byť prosté}""", parts = listOf(part("Injektivita g", """f\circ g\text{ prosté}\ \Rightarrow\ g\text{ prosté}""", 0), part("Protipríklad pre f", """\exists f,g:\ f\circ g\text{ prosté},\ \softbreak f\text{ nie je prosté}""", 2))),
    null,
    m("""Existujú: §\cb{g}§ musí byť prosté, ale §\ca{f}§ nemusí. Najprv dokážem, že §\cb{g}§ prosté je."""),
    listOf(
        r("Ako začať dôkaz injektivity §\\cb{g}§?", 0,
            c("""Nech §x,y\in A§ sú ľubovoľné a §\cb{g}(x)=\cb{g}(y)§."""),
            c("""Nech §x,y\in A§ a §(\ca{f}\circ \cb{g})(x)=(\ca{f}\circ \cb{g})(y)§.""", "Dokazuješ injektivitu §\\cb{g}§, preto vychádzaš z rovnosti obrazov pri §\\cb{g}§."),
            c("""§\ca{f}\circ \cb{g}§ je prosté, teda aj §\cb{g}§ je prosté.""", Itself)),
        r("Čo z toho dostaneš?", 1,
            c("""Teda §x=y§, lebo §\cb{g}§ je zobrazenie.""", "Že §\\cb{g}§ je zobrazenie, dáva najviac jeden obraz, nie rovnosť vzorov. To je injektivita, a tú dokazuješ."),
            c("""Aplikujem §\ca{f}§: §\ca{f}(\cb{g}(x))=\ca{f}(\cb{g}(y))§, teda §(\ca{f}\circ \cb{g})(x)=(\ca{f}\circ \cb{g})(y)§, a z injektivity §\ca{f}\circ \cb{g}§ je §x=y§."""),
            c("""Teda §\ca{f}§ je prosté.""", "O §\\ca{f}§ sa tu nič nedozvieš, a §\\ca{f}§ prosté byť nemusí.")),
        r("A §\\ca{f}§? Nájdi príklad, kde §\\ca{f}§ prosté nie je.", 2,
            c("""Neexistuje, aj §\ca{f}§ musí byť prosté.""", "§\\ca{f}§ môže zlepiť prvky §B§, na ktoré §\\cb{g}§ nič nepošle. Príklad je v ďalšej možnosti."),
            c("""§\cb{g}(n)=n§, §\cb{g}:\mathbb{N}\to\mathbb{N}§ a §\ca{f}(n)=n§, §\ca{f}:\mathbb{N}\to\mathbb{N}§.""", "Tu sú obe prosté. Treba príklad, kde §\\ca{f}§ prosté nie je."),
            c("""§\cb{g}(n)=n§, §\cb{g}:\mathbb{N}_0\to\mathbb{Z}§ a §\ca{f}(n)=|n|§, §\ca{f}:\mathbb{Z}\to\mathbb{N}_0§: §\ca{f}(-1)=\ca{f}(1)§, no §(\ca{f}\circ \cb{g})(n)=n§ je prosté.""")),
    ),
    m("""Také zobrazenia existujú: §\cb{g}§ prosté byť musí (dokázané), §\ca{f}§ nie. Príklad je ten z Řešení Cvičení 5.11 c, d, len s §\mathbb{N}_0§ namiesto §\mathbb{N}§, lebo §|0|=0\notin\mathbb{N}§."""),
    // the task asks only for an example when one exists (Zásada 4); that g must be injective is the proof's lesson, not a line of the answer
    listOf("Odpoveď: existujú", "Konkrétny príklad s neprostým §\\ca{f}§ a overenie", "Overené, že §\\ca{f}\\circ \\cb{g}§ je prosté"),
    goals = listOf("Predpoklad §\\cb{g}(x)=\\cb{g}(y)§", "Odvodenie §x=y§", "Príklad s neprostým §\\ca{f}§"),
)

val pIdNeid = Proof(
    "dml-mz-p-id", "§f\\circ g§ identita, §g\\circ f§ nie", "Písomka 6. 12. 2024 (variant R), príklad 2b",
    wording(emptyList(), listOf(p("""Existují zobrazení §\ca{f}§ a §\cb{g}§ taková, že §\ca{f}\circ \cb{g}§ i §\cb{g}\circ \ca{f}§ jsou definované a §\ca{f}\circ \cb{g}§ je identické zobrazení zatímco §\cb{g}\circ \ca{f}§ není identické zobrazení? Svou odpověď zdůvodněte.""")), core = """\begin{aligned}\exists f,g:\ f\circ g&=\mathrm{id}_A,\\g\circ f&\ne\mathrm{id}_B\end{aligned}"""),
    null,
    m("""Existujú. Aby boli definované obe zloženia, beriem §\cb{g}:A\to B§ a §\ca{f}:B\to A§."""),
    listOf(
        r("Zvoľ množiny.", 1,
            c("""§A=B=\{1,2\}§""", "Tieto množiny príklad nedajú. Pri rovnako veľkých konečných množinách z §\\ca{f}\\circ \\cb{g}=\\mathrm{id}_A§ plynie, že §\\cb{g}§ je bijekcia a §\\ca{f}=\\cb{g}^{-1}§; potom aj §\\cb{g}\\circ \\ca{f}=\\mathrm{id}§."),
            c("""§A=\{1\}§, §B=\{1,2\}§"""),
            c("""Také §\ca{f},\cb{g}§ neexistujú, lebo §\ca{f}\circ \cb{g}=\mathrm{id}§ znamená, že §\ca{f}=\cb{g}^{-1}§.""", "Inverzia chce obe rovnosti naraz (Definícia 5.13). Jedna z nich nestačí.")),
        r("Zvoľ zobrazenia.", 0,
            c("""§\cb{g}(1)=1§ a §\ca{f}(1)=\ca{f}(2)=1§."""),
            c("""§\cb{g}(1)=1§ a §\ca{f}(1)=1§, §\ca{f}(2)=2§.""", "§\\ca{f}§ ide do §A=\\{1\\}§, hodnota §2§ tam nie je."),
            c("""§\cb{g}(1)=2§ a §\ca{f}(2)=2§.""", "§\\ca{f}§ musí byť totálne na §B§ a hodnoty brať z §A=\\{1\\}§.")),
        r("Over.", 2,
            c("""§(\ca{f}\circ \cb{g})(1)=1§, teda obe sú identity.""", "§\\cb{g}\\circ \\ca{f}§ nie je identita. §(\\cb{g}\\circ \\ca{f})(2)=\\cb{g}(1)=1\\neq 2§."),
            c("""§(\cb{g}\circ \ca{f})(1)=1§ a §(\cb{g}\circ \ca{f})(2)=2§.""", "§\\ca{f}(2)=1§, teda §(\\cb{g}\\circ \\ca{f})(2)=\\cb{g}(1)=1§, nie §2§."),
            c("""§(\ca{f}\circ \cb{g})(1)=\ca{f}(1)=1§, teda §\ca{f}\circ \cb{g}=\mathrm{id}_A§; §(\cb{g}\circ \ca{f})(2)=\cb{g}(1)=1\neq 2§, teda §\cb{g}\circ \ca{f}\neq\mathrm{id}_B§.""")),
    ),
    m("""Také zobrazenia existujú, napr. §\cb{g}:\{1\}\to\{1,2\}§, §\cb{g}(1)=1§ a §\ca{f}:\{1,2\}\to\{1\}§."""),
    listOf("Množiny zvolené tak, aby obe zloženia boli definované", "Konkrétne §\\ca{f}§ a §\\cb{g}§ s definičnými obormi", "Overenie §\\ca{f}\\circ \\cb{g}=\\mathrm{id}_A§", "Bod, v ktorom §\\cb{g}\\circ \\ca{f}§ nie je identita"),
    goals = listOf("Voľba množín", "Voľba §\\ca{f}§ a §\\cb{g}§", "Overenie oboch zložení"),
)

val pInjSurjZ2 = Proof(
    "dml-mz-p-z2", "Injektivita a surjektivita na §\\mathbb{Z}^2§", "Písomka 4. 12. 2025, vzorová stránka, príklad 2b",
    wording(emptyList(), listOf(p("""Vyšetřete injektivitu a surjektivitu zobrazení §f:\mathbb{Z}^2\to\mathbb{Z}^2§, §f(m,n)=(2m-3n+2,\,5m-7n+3)§ (z definice nebo protipříkladem).""")), core = """f:\mathbb{Z}^2\to\mathbb{Z}^2\text{ je bijekcia}""", parts = listOf(part("Injektivita", """f(u)=f(v)\ \Rightarrow\ u=v""", 0), part("Surjektivita", """\forall z\in\mathbb{Z}^2:\ \exists u\in\mathbb{Z}^2:\ f(u)=z""", 1))),
    null,
    m("""Myšlienka: rovnosť obrazov aj hľadanie vzoru sú sústavy dvoch lineárnych rovníc; prvá musí mať len riešenie §m=m',\ n=n'§, druhá celočíselné riešenie pre každý cieľ. Injektivita: nech §(m,n),(m',n')\in\mathbb{Z}^2§ a §f(m,n)=f(m',n')§. Po zložkách §2m-3n+2=2m'-3n'+2§ a §5m-7n+3=5m'-7n'+3§."""),
    listOf(
        r("Uprav.", 0,
            c("""§2(m-m')-3(n-n')=0§ a §5(m-m')-7(n-n')=0§; z prvej §\cdot 5§ mínus druhej §\cdot 2§: §-(n-n')=0§, teda §n=n'§, a potom §m=m'§."""),
            c("""§f(0,0)=(2,3)\neq f(1,0)=(4,8)§, teda §f§ je prosté.""", ByExample),
            c("""Odčítam rovnice: §3(m-m')-4(n-n')=0§, teda §m=m'§ a §n=n'§.""", "Jedna rovnica s dvoma neznámymi má nekonečne veľa riešení. Z nej samej §m=m'§ neplynie.")),
        r("Surjektivita: ku §(x,y)\\in\\mathbb{Z}^2§ hľadám §(m,n)§.", 1,
            c("""§f(0,0)=(2,3)§, teda bod §(2,3)§ vzor má a §f§ je surjektívne.""", ByExample),
            c("""§2m-3n=x-2§, §5m-7n=y-3§; odtiaľ §m=-7(x-2)+3(y-3)=-7x+3y+5§ a §n=-5(x-2)+2(y-3)=-5x+2y+4§."""),
            c("""§m=\tfrac{x-2}{2}§ a §n=\tfrac{y-3}{-7}§.""", "Rovnice sa riešia spolu, každá obsahuje §m§ aj §n§. Delenie jednotlivo dá zlé výsledky."),
            given = """Surjektivita: nech §(x,y)\in\mathbb{Z}^2§ je ľubovoľný prvok; hľadám §(m,n)\in\mathbb{Z}^2§ s §f(m,n)=(x,y)§."""),
        r("Záver o surjektivite?", 2,
            c("""Vzor existuje len pre niektoré §(x,y)§.""", "Vzorce §m=-7x+3y+5§, §n=-5x+2y+4§ dávajú celé čísla pre každé celé §x,y§."),
            c("""Je surjektívne, lebo je injektívne.""", "Pre nekonečné množiny z injektivity surjektivita neplynie, napr. §n\\mapsto 2n§ na §\\mathbb{Z}§."),
            c("""§m,n\in\mathbb{Z}§ pre všetky §x,y\in\mathbb{Z}§ a dosadením §f(-7x+3y+5,\,-5x+2y+4)=(x,y)§, teda §f§ je surjektívne.""")),
    ),
    m("""§f§ je injektívne aj surjektívne, teda bijekcia."""),
    listOf("Definícia injektivity a ľubovoľné dva body", "Riešenie sústavy pre rozdiely: §m=m'§, §n=n'§", "Definícia surjektivity a vzor pre ľubovoľné §(x,y)§", "Vzor je celočíselný a overený dosadením"),
    goals = listOf("Injektivita", "Vzor bodu §(x,y)§", "Surjektivita"),
)

val SetsProofs = listOf(pIdentita, pSkladInj, pSkladG, pIdNeid, pInjSurjZ2)

/* ---- Theorems ---------------------------------------------------------------------------------------------------- */

val tInkluze = Theorem(
    "dml-mz-t-inkluze",
    "Vlastnosti inklúzie",
    "Skriptá, Tvrzení 5.1",
    wording(
        emptyList(),
        listOf(p("""Pro libovolné §A,B,C\subseteq\mathcal{U}§ platí:"""), i("""§\emptyset\subseteq A§ a §A\subseteq A§ (množiny §\emptyset§ a §A§ jsou triviální podmnožiny množiny §A§)"""), i("""§((A\subseteq B)\wedge(B\subseteq C))\Rightarrow(A\subseteq C)§"""), i("""§((A\subseteq B)\wedge(B\subseteq A))\Leftrightarrow(A=B)§"""), i("""§(A\subseteq B)\Rightarrow(\mathcal{P}(A)\subseteq\mathcal{P}(B))§""")),
    ),
    m("""Rovnosť množín sú dve inklúzie; prázdna množina je podmnožinou každej."""),
    shortProof("""Důkaz plyne přímočaře z Definice 5.2 a logických zákonů.""", "Skriptá, dôkaz Tvrzení 5.1"),
    note = m("""Načo: keď sa rovnosť nedá dokázať jedným reťazcom ekvivalencií, dokážeš prvkovo §L\subseteq P§ aj §P\subseteq L§ a bod 3 z nich spraví §L=P§. Tak dokážeš rovnosť aj vtedy, keď niektorý krok reťazca platí len jedným smerom."""),
    short = "o vlastnostiach inklúzie"
)

val tDoplnek = Theorem(
    "dml-mz-t-doplnek",
    "Doplnok, §\\mathcal{U}§ a §\\emptyset§",
    "Skriptá, Věta 5.1",
    wording(
        listOf(p("""Buď §A\subseteq\mathcal{U}§.""")),
        listOf(p("""Pak platí:"""), i("""Zákony doplňku: §A\cap\overline{A}=\emptyset§, §A\cup\overline{A}=\mathcal{U}§"""), i("""Zákon involuce: §\overline{(\overline{A})}=A§"""), i("""Zákony identity: §A\cap\mathcal{U}=A§, §A\cup\emptyset=A§"""), i("""Zákony anihilace: §A\cap\emptyset=\emptyset§, §A\cup\mathcal{U}=\mathcal{U}§""")),
    ),
    m("""Sú to logické zákony prepísané do množín: §p\wedge\neg p\Leftrightarrow\bot§, §p\vee\neg p\Leftrightarrow\top§, §\neg\neg p\Leftrightarrow p§."""),
    noProof("Skriptá pri tejto vete samostatný dôkaz neuvádzajú; dôkazy častí sú v cvičeniach, ostatné nechávajú čitateľovi.", "Skriptá, Věta 5.1"),
    short = "o doplnku a prázdnej množine"
)

val tAlgebra = Theorem(
    "dml-mz-t-algebra",
    "Algebraické vlastnosti operácií",
    "Skriptá, Věta 5.2",
    wording(
        listOf(p("""Buďte §A,B,C\subseteq\mathcal{U}§.""")),
        listOf(p("""Pak platí:"""), i("""Zákony idempotence: §A\cap A=A§, §A\cup A=A§"""), i("""Komutativní zákony: §A\cap B=B\cap A§, §A\cup B=B\cup A§"""), i("""Asociativní zákony: §(A\cap B)\cap C=A\cap(B\cap C)§, §(A\cup B)\cup C=A\cup(B\cup C)§"""), i("""Distributivní zákony: §A\cap(B\cup C)=(A\cap B)\cup(A\cap C)§, §A\cup(B\cap C)=(A\cup B)\cap(A\cup C)§"""), i("""Zákony absorpce: §A\cap(A\cup B)=A§, §A\cup(A\cap B)=A§"""), i("""De Morganovy zákony: §A\setminus(B\cap C)=(A\setminus B)\cup(A\setminus C)§, §A\setminus(B\cup C)=(A\setminus B)\cap(A\setminus C)§""")),
    ),
    m("""Každý zákon je logický zákon pre formuly §x\in A§, §x\in B§, §x\in C§ (Poznámka 5.3)."""),
    noProof("Skriptá pri tejto vete samostatný dôkaz neuvádzajú; dôkazy častí sú v cvičeniach, ostatné nechávajú čitateľovi.", "Skriptá, Věta 5.2"),
    note = m("""Na teste: identitu dokazuješ prepisom na formuly a týmito zákonmi, nie Vennovým diagramom."""),
    short = "o zákonoch množinových operácií"
)

val tInvertibilni = Theorem(
    "dml-mz-t-invert",
    "Vlastnosti invertibilných zobrazení",
    "Skriptá, Věta 5.3",
    wording(
        listOf(p("""Je-li §f:A\rightarrow B§ invertibilní,""")),
        listOf(p("""pak"""), i("""§(\forall a\in A)(\forall b\in B)(f^{-1}(b)=a\Leftrightarrow f(a)=b)§ (tedy inverze zobrazení, pokud existuje, je určena jednoznačně),"""), i("""§f^{-1}§ je také invertibilní a platí §(f^{-1})^{-1}=f§."""), p("""Jsou-li §f§: §B\rightarrow C§ a §g§: §A\rightarrow B§ invertibilní, pak i jejich složení §(f\circ g)§ je invertibilní a platí"""), b("""(f\circ g)^{-1}=g^{-1}\circ f^{-1}.""")),
        core = """\begin{aligned}f^{-1}(b)=a&\ \Leftrightarrow\ f(a)=b,\\(f^{-1})^{-1}&=f,\\(f\circ g)^{-1}&=g^{-1}\circ f^{-1}\end{aligned}""",
    ),
    m("""Inverzia vymení vzor a obraz; inverzia zloženia ide v opačnom poradí: najprv sa vráti §f§, potom §g§."""),
    shortProof("""Ze vztahu §f^{-1}\circ f=\mathrm{id}_A§ plyne, že pro každé §a\in A§ platí §(f^{-1}\circ f)(a)=f^{-1}(f(a))=a§. Druhou implikaci (kdykoli §f^{-1}(b)=a§, pak také §f(a)=b§) dostaneme obdobným způsobem ze vztahu §f\circ f^{-1}=\mathrm{id}_B§ (vyzkoušejte sami!). Invertibilita §f^{-1}§ a vztah §(f^{-1})^{-1}=f§ plyne z jednoznačnosti inverze a přímo z definice. Díky jednoznačnosti inverze stačí ověřit, že dvojice zobrazení §f\circ g:A\to C§ a §g^{-1}\circ f^{-1}:C\to A§ vyhovuje Definici 5.13, přitom využijeme asociativitu (Pozorování 5.1): §(f\circ g)\circ(g^{-1}\circ f^{-1})=\mathrm{id}_C§, §(g^{-1}\circ f^{-1})\circ(f\circ g)=\mathrm{id}_A§.""", "Skriptá, dôkaz Věty 5.3"),
    short = "o vlastnostiach inverzného zobrazenia"
)

val tInvBij = Theorem(
    "dml-mz-t-invbij",
    "Inverzia existuje práve pre bijekciu",
    "Skriptá, Věta 5.4",
    wording(
        listOf(p("""Buď §f:A\rightarrow B§ zobrazení.""")),
        listOf(p("""§f§ je invertibilní právě tehdy, když §f§ je bijekce.""")),
    ),
    m("""Prehodené dvojice §\{(b,a)\mid(a,b)\in f\}§ tvoria zobrazenie práve vtedy, keď každé §b§ má práve jeden vzor: aspoň jeden (surjektivita) a najviac jeden (injektivita)."""),
    shortProof("""Definujme tedy §g=\{(b,a)\in B\times A\mid(a,b)\in f\}§, to je možné vždy. Platí, že §f§ je invertibilní tehdy a jen tehdy, když je §g§ zobrazením (bijektivním). Aby §g§ bylo (totálním) zobrazením, potřebujeme, aby pro každé §b\in B§ existovala v §g§ právě jedna dvojice §(b,a)§. Tedy aby v §f§ pro každé §b\in B§ existovala právě jedna dvojice §(a,b)§. A to je pravda, díky injektivitě §f§ taková dvojice vždy existuje nejvýše jedna, díky surjektivitě existuje alespoň jedna.""", "Skriptá, dôkaz Věty 5.4"),
    short = "o existencii inverzného zobrazenia"
)

val tSkladani = Theorem(
    "dml-mz-t-sklad",
    "Skladanie injekcií, surjekcií a bijekcií",
    "Skriptá, Věta 5.5",
    wording(
        listOf(p("""Buďte §\ca{f}:B\rightarrow C§ a §\cb{g}:A\rightarrow B§ zobrazení.""")),
        listOf(p("""Potom pro jejich složení platí:"""), i("""jsou-li §\ca{f}§ a §\cb{g}§ prostá zobrazení, tak je §\ca{f}\circ \cb{g}§ prosté,"""), i("""jsou-li §\ca{f}§ a §\cb{g}§ zobrazení na, tak je §\ca{f}\circ \cb{g}§ na,"""), i("""jsou-li §\ca{f}§ a §\cb{g}§ bijekce, tak je §\ca{f}\circ \cb{g}§ bijekce.""")),
        core = """f,g\text{ prosté}\Rightarrow f\circ g\text{ prosté},\ \softbreak\text{rovnako pre „na“ a bijekce}""",
        parts = listOf(part("Injektivita", """f,g\text{ prosté}\ \Rightarrow\ f\circ g\text{ prosté}""", 0), part("Surjektivita", """f,g\text{ na}\ \Rightarrow\ f\circ g\text{ na}""", 4), part("Bijektivita", """f,g\text{ bijekcie}\softbreak[1]\ \Rightarrow f\circ g\text{ bijekcia}""", 5)),
    ),
    m("""Vlastnosti sa skladaním zachovajú. Opačne to neplatí celé: z prostého §\ca{f}\circ \cb{g}§ plynie len prosté §\cb{g}§, zo surjektívneho len surjektívne §\ca{f}§ (Cvičení 5.11)."""),
    proved(pSkladInj),
    short = "o skladaní injekcií a surjekcií"
)

val tKonecne = Theorem(
    "dml-mz-t-konecne",
    "Injektivita a surjektivita na konečných množinách",
    "Skriptá, Pozorování 5.2",
    wording(
        listOf(p("""Nechť §f:A\rightarrow B§ je zobrazení mezi neprázdnými ==konečnými== množinami §A,B§.""")),
        listOf(p("""Potom platí:"""), i("""má-li §B§ více prvků než §A§, pak §f§ nemůže být na,"""), i("""má-li §A§ více prvků než §B§, pak §f§ nemůže být prosté,"""), i("""pokud §A§ a §B§ nemají stejně prvků, pak §f§ nemůže být bijekce,"""), i("""je-li §f§ bijekce, tak mají §A§ a §B§ stejný počet prvků,"""), i("""mají-li §A§ a §B§ stejný počet prvků, pak je §f§ prosté právě tehdy, když je §f§ na.""")),
    ),
    m("""Do menšej množiny sa všetko nezmestí bez zlepenia; menšia nepokryje väčšiu. Pre nekonečné množiny to neplatí: §n\mapsto 2n§ na §\mathbb{Z}§ je prosté, nie na."""),
    shortProof("""Důkaz si laskavý čtenář jistě provede snadno sám, postačí jednoduchá úvaha ve stylu „Kdybych konstruoval zobrazení §f:A\rightarrow B§ s požadovanými vlastnostmi (postupným přiřazování obrazů vpravo prvkům vlevo), v jakém vztahu bych potřeboval velikosti §A§ a §B§ tak, aby to šlo, abych nějaké prvky nevynechal a/nebo nevybral vícekrát?“""", "Skriptá, dôkaz Pozorování 5.2"),
    short = "o zobrazeniach konečných množín"
)

val tSrovnavani = Theorem(
    "dml-mz-t-srovn",
    "Porovnávanie mohutností",
    "Skriptá, Věta 5.7",
    wording(
        listOf(p("""Buďte §A,B,C§ libovolné množiny.""")),
        listOf(p("""Pak pro jejich mohutnosti platí:"""), i("""§|A|=|A|§ a §|A|\leq|A|§,"""), i("""jestliže §B\subseteq A§, pak §|B|\leq|A|§,"""), i("""jestliže §|A|\leq|B|§ a §|B|\leq|C|§, pak §|A|\leq|C|§,"""), i("""jestliže §|A|=|B|§ a §|B|=|C|§, pak §|A|=|C|§,"""), i("""(Cantorova-Schröderova-Bernsteinova věta) jestliže §|A|\leq|B|§ a §|B|\leq|A|§, pak §|A|=|B|§.""")),
    ),
    m("""Identita a zloženie prostých či bijektívnych zobrazení dávajú prvé štyri body; posledný je hlbší a jeho dôkaz kurz nevyžaduje."""),
    shortProof("""Je-li některá z množin prázdná, tvrzení platí triviálně. Jelikož identita §\mathrm{id}_A:A\to A§ je zřejmě bijektivní (a tedy i injektivní), platí §|A|=|A|§ (a tedy i §|A|\leq|A|§). Nahradíme-li pravou množinu §B§ nadmnožinou §A\supseteq B§ a předpis nezměníme, získáme zobrazení §f:B\to A§, které již nemusí být surjektivní (žádný prvek z §(A\setminus B)§ nebude mít vzor), ale injektivní stále bude. Platí-li §|A|\leq|B|§ a §|B|\leq|C|§, pak existují injektivní zobrazení §f:A\to B§ a §g:B\to C§. Jak známo, složené zobrazení §(g\circ f):A\to C§ je také injektivní, tedy platí §|A|\leq|C|§. Platí-li §|A|=|B|§ a §|B|=|C|§, pak existují bijekce §f:A\to B§ a §g:B\to C§. Jak známo, složené zobrazení §(g\circ f):A\to C§ je také bijekce, tedy platí §|A|=|C|§. Bod 5: Ukázat, že pokud existují prostá zobrazení z §A§ do §B§ i z §B§ do §A§, pak existuje i bijekce z §A§ do §B§, není úplně triviální. Důkaz tohoto tvrzení proto vynecháváme a po studentech nevyžadujeme.""", "Skriptá, dôkaz Věty 5.7"),
    note = m("""Načo: bijekciu medzi nekonečnými množinami často priamo nenájdeš. Bodu 5 stačia dve prosté zobrazenia opačným smerom; tak sa ukáže aj §|\mathbb{N}\times\mathbb{N}|=|\mathbb{N}|§ (Důsledek 5.1)."""),
    short = "o porovnávaní mohutností"
)

val tPocty = Theorem(
    "dml-mz-t-pocty",
    "Mohutnosti konečných množín",
    "Skriptá, Tvrzení 5.2",
    wording(
        listOf(p("""Buď §A§ konečná množina.""")),
        listOf(i("""Každá vlastní podmnožina §B\subsetneq A§ splňuje §|B|<|A|§."""), i("""Je-li i §B§ konečná množina, pak je i §A\cup B§ konečná a platí §|A\cup B|\leq|A|+|B|§."""), i("""Je-li §B§ konečná a ==disjunktní== s §A§, pak navíc platí §|A\cup B|=|A|+|B|§."""), i("""Je-li §B§ konečná, pak je §A\times B§ konečná a platí §|A\times B|=|A|\cdot|B|§.""")),
    ),
    m("""Pri konečných množinách je mohutnosť počet prvkov: zjednotenie najviac súčet, súčin presne súčin."""),
    shortProof("""Pro každou podmnožinu platí §|B|\leq|A|§, dle Věty 5.7. Z §B§ do §A§ lze jistě sestrojit injektivní zobrazení, ale už ne surjektivní (tedy ani bijekci). Platí tedy §|B|<|A|§. (pro body 2.,3.,4.) U konečných množin mohutnosti odpovídá počet prvků, vše lze tedy zdůvodnit úvahou (jak by šlo či nešlo zkonstruovat injektivní/bijektivní zobrazení mezi konečnými množinami daných velikostí), opět podobně jako v Pozorování 5.2.""", "Skriptá, dôkaz Tvrzení 5.2"),
    short = "o mohutnosti konečných množín"
)


internal val tSkladIdentita = Theorem(
    "dml-mz-t-sklad-id", "Skladanie zobrazení a identita", "Skriptá, Pozorování 5.1",
    wording(
        listOf(p("""Buďte §f:C\rightarrow D§, §g:B\rightarrow C§, §h:A\rightarrow B§ zobrazení.""")),
        listOf(p("""Platí vztahy:"""), i("""asociativní zákon: §(f\circ g)\circ h=f\circ(g\circ h)§"""), i("""zákon o složení s identitou: §(f\circ\mathrm{id}_C)=f§ a §(\mathrm{id}_D\circ f)=f§""")),
        core = """\begin{aligned}(f\circ g)\circ h&=f\circ(g\circ h),\\f\circ\mathrm{id}_C&=f,\\\mathrm{id}_D\circ f&=f\end{aligned}""",
    ),
    m("""Zátvorky pri skladaní možno presúvať. Identita na správnej množine zobrazenie nemení."""),
    noProof("Skriptá ho uvádzajú bez dôkazu.", "Skriptá, Pozorování 5.1"),
    short = "o skladaní zobrazení a identite",
)

internal val tHilbert = Theorem(
    "dml-mz-t-hilbert",
    "Dôsledky Hilbertovho hotela",
    "Skriptá, Důsledek 5.1",
    wording(
        emptyList(),
        listOf(p("""Zobrazení (prostá či bijektivní) zkonstruovaná v Poznámce 5.12 přímo implikují následující vztahy mezi mohutnostmi:"""), i("""§|\mathbb{N}_0|=|\mathbb{N}|§,"""), i("""§(\forall k\in\mathbb{N})\big(|\{-k+1,\ldots,-1,0\}\cup\mathbb{N}|=|\mathbb{N}|\big)§,"""), i("""§|\mathbb{Z}|=|\mathbb{N}|§,"""), i("""§|\mathbb{N}\times\mathbb{N}|=|\mathbb{N}|§. Přímo z konstruovaného zobrazení plyne pouze vztah §|\mathbb{N}\times\mathbb{N}|\leq|\mathbb{N}|§. Jelikož však zřejmě platí i §|\mathbb{N}|\leq|\mathbb{N}\times\mathbb{N}|§, z Věty 5.7, bodu 5. pak dostáváme rovnost §|\mathbb{N}\times\mathbb{N}|=|\mathbb{N}|§.""")),
        core = """|\mathbb{N}_0|=|\mathbb{Z}|=|\mathbb{N}^2|=|\mathbb{N}|,\ \softbreak\forall k\in\mathbb{N}:\ \softbreak[1]|\{-k+1,\ldots,0\}\cup\mathbb{N}|\softbreak[2]=|\mathbb{N}|""",
    ),
    m("""Pridanie konečne či spočetne veľa hostí veľkosť hotela nezmení. Celé čísla aj dvojice prirodzených čísel možno očíslovať."""),
    shortProof("""Zobrazení (prostá či bijektivní) zkonstruovaná v Poznámce 5.12 přímo implikují následující vztahy mezi mohutnostmi. Přímo z konstruovaného zobrazení plyne pouze vztah §|\mathbb{N}\times\mathbb{N}|\leq|\mathbb{N}|§. Jelikož však zřejmě platí i §|\mathbb{N}|\leq|\mathbb{N}\times\mathbb{N}|§, z Věty 5.7, bodu 5. pak dostáváme rovnost §|\mathbb{N}\times\mathbb{N}|=|\mathbb{N}|§.""", "Skriptá, Důsledek 5.1"),
    short = "o Hilbertovom hoteli"
)

internal val tDalsiSpocetne = Theorem(
    "dml-mz-t-dalsi-spocetne", "Ďalšie spočetné množiny", "Skriptá, Tvrzení 5.4",
    wording(
        emptyList(),
        listOf(i("""Množiny §\mathbb{Z}\times\mathbb{Z}§, §\mathbb{Z}\times\mathbb{N}§ jsou spočetné."""), i("""Množina §\mathbb{Q}§ racionálních čísel je spočetná.""")),
    ),
    m("""Dvojice celých čísel aj racionálne čísla možno očíslovať prirodzenými číslami."""),
    shortProof("""Kombinováním myšlenek z řešení Poznámky 5.12 (vyzkoušejte!) postupnou konstrukcí bijekcí z §\mathbb{Z}\times\mathbb{Z}§ do §\mathbb{Z}\times\mathbb{N}§ (trik se sudými a lichými čísly v druhé složce) a dále do §\mathbb{N}\times\mathbb{N}§ (tentýž trik, ale v první složce). Bijekci z §\mathbb{N}\times\mathbb{N}§ do §\mathbb{N}§ už známe. Existuje tedy prosté zobrazení §f:\mathbb{Q}\to(\mathbb{Z}\times\mathbb{N})§, které každému racionálnímu číslu přiřadí dvojici §(p,q)\in(\mathbb{Z}\times\mathbb{N})§ takovou, že §\frac pq§ je ono racionální číslo po zkrácení, tj. v základním tvaru. Tedy §|\mathbb{Q}|\leq|\mathbb{Z}\times\mathbb{N}|=|\mathbb{N}|§. Na druhou stranu, vztah §\mathbb{N}\subseteq\mathbb{Q}§ implikuje §|\mathbb{N}|\leq|\mathbb{Q}|§ a díky bodu 5. Věty 5.7 dostáváme rovnost §|\mathbb{N}|=|\mathbb{Q}|§.""", "Skriptá, dôkaz Tvrzení 5.4"),
    short = "o ďalších spočetných množinách",
)

internal val tNekonecne = Theorem(
    "dml-mz-t-nekonecne",
    "Mohutnosti nekonečných množín",
    "Skriptá, Tvrzení 5.5",
    wording(
        emptyList(),
        listOf(p("""Platí následující:"""), i("""Je-li §A§ nekonečná množina, tak má vlastní podmnožinu se stejnou mohutností."""), i("""Nechť §A,B§ jsou množiny, §B§ je nekonečná a §|A|\leq|B|§. Pak §|A\cup B|=|B|§."""), i("""Nechť §A,B§ jsou neprázdné množiny, §B§ je nekonečná a §|A|\leq|B|§, pak §|A\times B|=|B|§.""")),
    ),
    m("""Nekonečná množina môže mať rovnako veľkú vlastnú podmnožinu. Pri uvedených podmienkach veľkosť zjednotenia aj súčinu určuje väčšia množina."""),
    noProof("Skriptá dôkazy vynechávajú, pretože sú netriviálne a potrebujú axiómu výberu.", "Skriptá, Tvrzení 5.5"),
    short = "o mohutnosti nekonečných množín"
)

internal val tNespocetne = Theorem(
    "dml-mz-t-nespocetne", "Nespočetné množiny", "Skriptá, Tvrzení 5.6",
    wording(
        emptyList(),
        listOf(p("""Platí následující:"""), i("""Množina všech reálných čísel z otevřeného intervalu §(0,1)=\{x\in\mathbb{R}\mid0<x<1\}§ je nespočetná."""), i("""Množina všech reálných čísel §\mathbb{R}§ je nespočetná."""), i("""Dokonce platí §|(0,1)|=|\mathbb{R}^+|=|\mathbb{R}|§.""")),
        core = """|\mathbb{N}|<|(0,1)|=|\mathbb{R}^+|=|\mathbb{R}|""",
    ),
    m("""Diagonálny argument ukáže, že žiadny zoznam neobsiahne všetky reálne čísla intervalu. Interval, kladné reálne čísla a všetky reálne čísla majú rovnakú mohutnosť."""),
    shortProof("""Pro spor předpokládejme, že §|(0,1)|=|\mathbb{N}|§, tedy že lze sestavit nekonečný (ale spočetný) seznam obsahující všechna reálná čísla ležící mezi §0§ a §1§, očíslovaná přirozenými čísly. Sestavíme nový řetězec, např. tak, že za desetinnou čárkou bude obsahovat jen cifry §5§ a §6§ a pro §\forall k\in\mathbb{N}§ platí, že se od rozvoje §r_k§ liší na §k§té pozici (pokud má §r_k§ na dané pozici něco jiného než §5§ nebo §6§, můžeme si vybrat). Tím jsme ovšem vytvořili řetezec, který v celém seznamu jistě není (od každého čísla v něm se na nějaké pozici liší). Ten současně reprezentuje nějaké reálné číslo z §(0,1)§. V seznamu přitom měla být všechna taková čísla a tedy dostáváme spor! Čtenář ze střední školy jistě zná logaritmické a exponenciální funkce, §\log_a(x)§ a §a^x§ (pro základ §a>0,a\neq1§). Tyto jsou krásným příkladem bijekcí mezi §\mathbb{R}§ a §\mathbb{R}^+§. Jistě ale známe funkci arkus tangens, §\mathrm{arctg}(x):\mathbb{R}\to(-\frac\pi2,\frac\pi2)§, která je bijekcí. Když si s funkčním předpisem §\mathrm{arctg}(x)§ malinko pohrajete, jistě obdržíte funkci, která §\mathbb{R}§ zobrazí bijektivně na interval §(0,1)§ namísto původního §(-\frac\pi2,\frac\pi2)§.""", "Skriptá, dôkaz Tvrzení 5.6"),
    short = "o nespočetných množinách",
)

val SetsTheorems = listOf(tInkluze, tDoplnek, tAlgebra, tInvertibilni, tInvBij, tSkladani, tKonecne, tSrovnavani, tPocty, tSkladIdentita, tHilbert, tDalsiSpocetne, tNekonecne, tNespocetne)

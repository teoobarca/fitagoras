package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Kapitola 4 "Frobeniova věta a kompletní řešení SLR" (chap-05.woo): the definition of a variety, the theorems the course
 * lists ("Frobeniova věta a lineární variety") word for word, proofs line by line where the course or an exam wants them.
 * Numbering: one counter per chapter (Pozorování 4.1, Věta 4.2, ...). \vx of the source is written \mathbf{x}.
 */

internal val k4Varieta = Concept(
    id = "la1-k4-varieta", name = "Lineárna varieta", src = "Skriptá, Definice 4.16",
    czech = listOf(
        p("""Množinu §W\subseteq T^n§ nazveme **lineární varietou** (případně pouze **varietou**), pokud existují §\mathbf{a}\in T^n§ a §P\ssub T^n§ takové, že"""),
        b("""W = \mathbf{a} + P."""),
    ),
    why = listOf(
        w("""existují §\mathbf{a}\in T^n§ a §P\ssub T^n§""", """Stačí jeden zápis. Vektor posunutia jednoznačný nie je, zameranie áno (Věta 4.18)."""),
        w("""§P\ssub T^n§""", """Posúvame **podpriestor**, nie hocijakú množinu. Priamka mimo počiatku je varieta, kruh nie."""),
        w("""§W = \mathbf{a} + P§""", """Znamená §\{\mathbf{a}+\mathbf{p}\mid\mathbf{p}\in P\}§: každý bod podpriestoru posunutý o §\mathbf{a}§."""),
        w("""dimenzí variety §\dim Z(W)§""", """Dimenzia sa berie zo zamerania: priamka má dimenziu §1§, aj keď neprechádza počiatkom."""),
    ),
    judge = listOf(
        j("""§(2,1)+\langle(1,0)\rangle\subseteq\mathbb{R}^2§""", true, """Priamka cez §(2,1)§ rovnobežná s osou §x§: posunutý podpriestor.""", """Je to varieta: §\langle(1,0)\rangle§ je podpriestor a §(2,1)§ vektor posunutia. Počiatkom prechádzať nemusí."""),
        j("""Každý podpriestor §P\ssub T^n§""", true, """§P=\theta+P§: posunutie o nulový vektor.""", """Podpriestor varietou je: stačí vektor posunutia §\theta§. Varieta je zovšeobecnenie podpriestoru."""),
        j("""Množina riešení sústavy §x+y=1,\ x+y=2§""", false, """Je prázdna, a varieta prázdna nie je (obsahuje aspoň §\mathbf{a}§).""", """Sústava nemá riešenie, množina je §\emptyset§. Každá varieta obsahuje svoj vektor posunutia (Věta 4.18), teda je neprázdna."""),
        j("""Kružnica §x^2+y^2=1§""", false, """Nie je: obsahuje §(1,0)§ a §(-1,0)§, ale nie bod §(0,0)§ medzi nimi. Varieta nad §\mathbb{R}§ s dvoma bodmi obsahuje celú priamku cez ne.""", """Kružnica nie je posunutý podpriestor. Posunutím priamky či roviny vznikne opäť priamka či rovina, nie krivka."""),
        j("""§(1,2,3)+\langle(1,0,0),(0,1,0)\rangle§ má dimenziu §3§""", false, """Dimenzia je §\dim Z(W)=2§: je to rovina.""", """Dimenzia variety je dimenzia zamerania, nie počet zložiek vektora posunutia. Zameranie má bázu z dvoch vektorov."""),
    ),
    units = listOf("Podmnožina §W\\subseteq T^n§", "Existuje §\\mathbf{a}\\in T^n§ a podpriestor §P§", "§W=\\mathbf{a}+P§", "Zameranie §Z(W)=P§, dimenzia §\\dim Z(W)§"),
    trap = Trap("Varieta je podprostor, který neprochází počátkem.", "Podpriestor je tiež varieta (§P=\\theta+P§), a varieta je posunutý podpriestor, nie hocijaká množina mimo počiatku.", "Definice 4.16"),
    ask = "Napište definici lineární variety a její dimenze.",
    question = "Sedí to s definíciou variety?",
    short = "lineárnej variety",
)

internal val K4Concepts = listOf(k4Varieta)

/* ---- proofs ---------------------------------------------------------------------------------------------------- */

internal val k4pPostacujuca = Proof(
    "la1-k4-p-frob-postacujuca",
    "Frobenius: rovnaké hodnosti, potom riešenie existuje",
    "Skriptá, Věta 4.2, dôkaz 1. bodu (§\\Leftarrow§); MARAST, ukážková zkoušková písemka (lekce 262), príklad 14159",
    null,
    "Tu dokazuješ len smer §\\Leftarrow§: ak sa hodnosti rovnajú, sústava má riešenie.",
    m("""Myšlienka: obal stĺpcov §\mathbf{A}§ leží v obale stĺpcov §(\mathbf{A}\mid\mathbf{b})§ a má rovnakú dimenziu, takže sa rovnajú; §\mathbf{b}§ je teda kombináciou stĺpcov a jej koeficienty sú riešenie. Nech §\mathbf{A}\in T^{m,n}§, §\mathbf{b}\in T^{m,1}§ a §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§. Ukážem, že existuje §\mathbf{x}§ s §\mathbf{A}\mathbf{x}=\mathbf{b}§."""),
    listOf(
        r("Prepíš rovnosť hodností na obaly.", 1,
            c("""Obaly riadkov §\mathbf{A}§ a §(\mathbf{A}\mid\mathbf{b})§ sa rovnajú.""", """Riadky majú rôzne dĺžky, ležia v rôznych priestoroch. Obaly sa porovnať nedajú."""),
            c("""Hodnosť je aj dimenzia obalu stĺpcov (Věta 3.12): §\dim\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n}\rangle=\dim\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n},\mathbf{b}\rangle§."""),
            c("""Teda §\mathbf{b}=\theta§.""", """Rovnosť hodností to nehovorí. Pravá strana môže byť ľubovoľný vektor z obalu stĺpcov.""")),
        r("Čo z rovnosti dimenzií?", 0,
            c("""§\mathbf{b}\in\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n}\rangle§ (Důsledek 2.64)."""),
            c("""§\mathbf{b}§ je jeden zo stĺpcov §\mathbf{A}§.""", """Dimenzia o tom nehovorí. Plynie len, že §\mathbf{b}§ je kombináciou stĺpcov."""),
            c("""Stĺpce §\mathbf{A}§ sú LN.""", """To z rovnosti hodností neplynie a ani to netreba. Ide o to, kde leží §\mathbf{b}§.""")),
        r("Dokonči.", 2,
            c("""Riešenie je jediné.""", """Dokazuješ len existenciu. Jediné je len pri §h(\mathbf{A})=n§ (3. bod vety)."""),
            c("""§\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}§ je riešenie.""", """§\mathbf{A}§ nemusí byť štvorcová ani regulárna. Inverziu tu nemáš."""),
            c("""Existujú §x_1,\dots,x_n\in T§ s §\mathbf{b}=\sum_{j=1}^n x_j\mathbf{A}_{:j}=\mathbf{A}\mathbf{x}§ (Věta 3.15), teda §\mathbf{x}=(x_1,\dots,x_n)^T§ je riešenie.""")),
    ),
    m("""Ak §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§, sústava §\mathbf{A}\mathbf{x}=\mathbf{b}§ má riešenie."""),
    listOf("Predpoklad a čo dokazujem", "Rovnosť hodností ako rovnosť dimenzií obalov stĺpcov (transpozícia)", "§\\mathbf{b}§ v obale stĺpcov (Důsledek 2.64)", "Koeficienty kombinácie sú riešenie (Věta 3.15)"),
    goals = listOf("""Hodnosti ako dimenzie""", """§\mathbf{b}§ v obale stĺpcov""", """Riešenie z koeficientov"""),
    minutes = 8,
)

internal val k4pNutna = Proof(
    "la1-k4-p-frob-nutna",
    "Frobenius: ak riešenie existuje, hodnosti sú rovnaké",
    "Skriptá, Věta 4.2, dôkaz 1. bodu (§\\Rightarrow§)",
    null,
    "Tu dokazuješ len smer §\\Rightarrow§: ak sústava má riešenie, hodnosti sa rovnajú.",
    m("""Myšlienka: riešenie sú koeficienty, ktorými sa §\mathbf{b}§ poskladá zo stĺpcov §\mathbf{A}§, a vektor poskladaný zo stĺpcov ich obal nezväčší. Nech §\mathbf{x}=(x_1,\dots,x_n)^T§ je riešenie sústavy §\mathbf{A}\mathbf{x}=\mathbf{b}§."""),
    listOf(
        r("Prepíš §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§.", 0,
            c("""§\sum_{j=1}^n x_j\mathbf{A}_{:j}=\mathbf{b}§ (Věta 3.15), teda §\mathbf{b}\in\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n}\rangle§."""),
            c("""§\sum_{i=1}^m x_i\mathbf{A}_{i:}=\mathbf{b}§""", """Stĺpec sprava kombinuje stĺpce, nie riadky. Riadky by kombinoval riadok zľava."""),
            c("""§\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}§""", """§\mathbf{A}§ nemusí byť regulárna. A nepotrebuješ §\mathbf{x}§ vyjadriť, len využiť.""")),
        r("Čo z toho pre dimenzie?", 1,
            c("""Obaly sa líšia o §\mathbf{b}§, dimenzia stúpne o §1§.""", """Naopak: vektor z obalu obal nezmení (Pozorování 2.34)."""),
            c("""§\dim\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n}\rangle=\dim\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n},\mathbf{b}\rangle§ (Důsledek 2.64)."""),
            c("""Stĺpce §\mathbf{A}§ sú LN.""", """To neplynie: riešení môže byť aj viac. Dôležité je len, kde leží §\mathbf{b}§.""")),
        r("Záver.", 0,
            c("""Teda §h(\mathbf{A}^T)=h((\mathbf{A}\mid\mathbf{b})^T)§ a podľa Věty 3.12 §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§."""),
            c("""Teda §h(\mathbf{A})=n§.""", """Rovnosť dimenzií hovorí o §\mathbf{A}§ a §(\mathbf{A}\mid\mathbf{b})§, nie o §n§."""),
            c("""Teda riešenie je jediné.""", """O počte riešení táto časť nehovorí. Jediné je len pri §h(\mathbf{A})=n§.""")),
    ),
    m("""Ak sústava má riešenie, tak §h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§."""),
    listOf("Riešenie ako koeficienty kombinácie stĺpcov", "§\\mathbf{b}§ v obale stĺpcov, dimenzie sa rovnajú", "Dimenzie obalov stĺpcov sú hodnosti transponovaných, Věta 3.12"),
    goals = listOf("""§\mathbf{b}§ v obale stĺpcov""", """Rovnosť dimenzií""", """Rovnosť hodností"""),
    minutes = 6,
)

internal val k4pPodpriestorSlr = Proof(
    "la1-k4-p-podpriestor-slr",
    "K podpriestoru existuje sústava",
    "Skúška 26. 1. 2026 (variant Hilbert), príklad 3C, 6 b; Skriptá, Věta 4.23 a jej dôkaz",
    null,
    "Dokazuješ smer §\\Rightarrow§ pre netriviálny §P§, ako ho chcela skúška 26. 1. 2026. Ostatné je krátke: §P=\\{\\theta\\}§ rieši §(\\mathbf{E}\\mid\\theta)§, §P=T^n§ nulová rovnica a smer §\\Leftarrow§ je Pozorování 4.1. Pozor na Varování 4.24: ukážka na konkrétnom príklade nie je dôkaz.",
    m("""Myšlienka: nájdem rovnice, ktoré spĺňa každý vektor bázy §P§, a dimenziou vylúčim ďalšie riešenia. Nech §P\ssub T^n§ je netriviálny, §\dim P=k\in\{1,\dots,n-1\}§, a §(\mathbf{x}_1,\dots,\mathbf{x}_k)§ je jeho báza. Hľadám §\mathbf{A}§ tak, aby množina riešení §S_0§ sústavy §(\mathbf{A}\mid\theta)§ bola §P§."""),
    listOf(
        r("Odkiaľ vezmeš riadky §\\mathbf{A}§?", 0,
            c("""Vyriešim homogénnu sústavu §(\mathbf{X}\mid\theta)§, kde §\mathbf{X}§ má v riadkoch §\mathbf{x}_1^T,\dots,\mathbf{x}_k^T§. Jej bázu §(\mathbf{a}_1,\dots,\mathbf{a}_{n-k})§ dám do riadkov §\mathbf{A}§."""),
            c("""Riadky §\mathbf{A}§ budú §\mathbf{x}_1^T,\dots,\mathbf{x}_k^T§.""", """Sústavu s bázou §P§ v riadkoch riešiš, nie je to hľadaná §\mathbf{A}§. Jej riešenia tvoria podpriestor dimenzie §n-k§, ktorý sa s §P§ vo všeobecnosti nerovná; riadky §\mathbf{A}§ sú až jej riešenia."""),
            c("""§\mathbf{A}=\mathbf{E}§""", """§\mathbf{E}\mathbf{x}=\theta§ má jediné riešenie §\theta§. To sedí len pre §P=\{\theta\}§, a ten je triviálny.""")),
        r("Prečo každé §\\mathbf{x}_i§ rieši §\\mathbf{A}\\mathbf{x}=\\theta§?", 1,
            c("""Lebo §\mathbf{x}_i\in P§.""", Circular4),
            c("""Každé §\mathbf{a}_j§ rieši rovnicu s koeficientmi §\mathbf{x}_i^T§, teda §\sum_\ell (\mathbf{x}_i)_\ell(\mathbf{a}_j)_\ell=0§. Tá istá rovnosť hovorí, že §\mathbf{x}_i§ rieši rovnicu s koeficientmi §\mathbf{a}_j^T§ (Poznámka 4.22)."""),
            c("""Lebo §\mathbf{A}§ je regulárna.""", """§\mathbf{A}§ má §n-k<n§ riadkov, nie je ani štvorcová.""")),
        r("Akú dimenziu má §S_0§?", 2,
            c("""§\dim S_0=n-k§""", """§n-k§ je počet riadkov §\mathbf{A}§, teda §h(\mathbf{A})§. Dimenzia riešení je §n-h(\mathbf{A})§."""),
            c("""§\dim S_0=n§""", """Celé §T^n§ by riešila len nulová matica. §\mathbf{A}§ má §n-k§ LN riadkov."""),
            c("""Riešenia §(\mathbf{X}\mid\theta)§ majú dimenziu §n-h(\mathbf{X})=n-k§, preto §\mathbf{A}§ má §n-k§ LN riadkov, §h(\mathbf{A})=n-k§ a §\dim S_0=n-(n-k)=k§ (Frobeniova veta).""")),
        r("Záver.", 0,
            c("""§(\mathbf{x}_1,\dots,\mathbf{x}_k)§ je LN súbor dĺžky §k§ v §S_0§ dimenzie §k§, teda báza §S_0§ (Věta 2.55). Preto §S_0=\langle\mathbf{x}_1,\dots,\mathbf{x}_k\rangle=P§."""),
            c("""§\dim S_0=\dim P§, a to stačí.""", """Rovnaká dimenzia sama nestačí. Treba aj, že §\mathbf{x}_i§ v §S_0§ ležia."""),
            c("""§P\subseteq S_0§, teda §P=S_0§.""", """Inklúzia sama rovnosť nedá. Treba ešte rovnaké dimenzie.""")),
    ),
    m("""Homogénna sústava §(\mathbf{A}\mid\theta)§ má za množinu riešení práve §P§, a navyše §h(\mathbf{A})=n-\dim P§."""),
    listOf("Báza §P§ do riadkov §\\mathbf{X}§, báza riešení §(\\mathbf{X}\\mid\\theta)§ do riadkov §\\mathbf{A}§", "Každé §\\mathbf{x}_i§ rieši §(\\mathbf{A}\\mid\\theta)§ (symetria rovnice)", "§h(\\mathbf{A})=n-k§ a §\\dim S_0=k§ z Frobeniovej vety", "LN súbor dĺžky §k§ v priestore dimenzie §k§ je báza, §S_0=P§"),
    goals = listOf("""Riadky matice §\mathbf{A}§""", """§\mathbf{x}_i§ riešia sústavu""", """Dimenzia §S_0§""", """Rovnosť §S_0=P§"""),
    minutes = 14,
)

private const val Circular4 = "Toto je kruh: že §P§ je množina riešení, práve dokazuješ."

internal val k4pVarietaSlr = Proof(
    "la1-k4-p-varieta-slr",
    "K variete existuje sústava",
    "Skriptá, Věta 4.26 a jej dôkaz",
    null,
    null,
    m("""§(\Leftarrow)§: množina riešení riešiteľnej sústavy je §\tilde{\mathbf{x}}+S_0§ a §S_0§ je podpriestor (Pozorování 4.1), teda varieta. Ostáva §(\Rightarrow)§: nech §M=\mathbf{a}+P§."""),
    listOf(
        r("Matica sústavy.", 1,
            c("""§\mathbf{A}§ má v riadkoch bázu §P§.""", """Báza §P§ v riadkoch je sústava, ktorú riešiš. Jej riešenia sú iný podpriestor; maticu §\mathbf{A}§ dáva až Věta 4.23."""),
            c("""Podľa Věty 4.23 existuje §\mathbf{A}§, pre ktorú je §P§ množinou riešení §(\mathbf{A}\mid\theta)§."""),
            c("""§\mathbf{A}=\mathbf{E}§""", """§\mathbf{E}\mathbf{x}=\theta§ má len riešenie §\theta§. Funguje to len pre §P=\{\theta\}§.""")),
        r("Pravá strana.", 0,
            c("""§\mathbf{b}:=\mathbf{A}\mathbf{a}§. Potom §\mathbf{a}§ rieši §\mathbf{A}\mathbf{x}=\mathbf{b}§."""),
            c("""§\mathbf{b}:=\mathbf{a}§""", """§\mathbf{a}§ je z §T^n§, pravá strana má toľko zložiek, koľko je rovníc. A §\mathbf{A}\mathbf{a}=\mathbf{a}§ platiť nemusí."""),
            c("""§\mathbf{b}:=\theta§""", """S nulovou pravou stranou je množinou riešení §P§, a to sa rovná §\mathbf{a}+P§ len pri §\mathbf{a}\in P§. Vo všeobecnosti by sa posunutie stratilo.""")),
        r("Záver.", 0,
            c("""§S_0=P§ a §\mathbf{a}§ je partikulárne riešenie, teda podľa Frobeniovej vety §S=\mathbf{a}+S_0=\mathbf{a}+P=M§."""),
            c("""§S=P§""", """Vo všeobecnosti nie: §P§ je množina riešení pridruženej homogénnej sústavy a všetky riešenia sú §\mathbf{a}+P§. To je §P§ len pri §\mathbf{a}\in P§ (Věta 4.18, bod 4)."""),
            c("""§S=\{\mathbf{a}\}§""", """Riešenie nie je jediné, keď §P\neq\{\theta\}§. K §\mathbf{a}§ sa pripočíta celé §S_0=P§.""")),
    ),
    m("""Každá varieta je množinou riešení nejakej sústavy a množina riešení každej riešiteľnej sústavy je varieta; navyše §h(\mathbf{A})=n-\dim M§."""),
    listOf("§(\\Leftarrow)§ zo štruktúry riešení", "Matica z Věty 4.23", "Pravá strana §\\mathbf{b}=\\mathbf{A}\\mathbf{a}§", "Frobeniova veta: §S=\\mathbf{a}+P§"),
    goals = listOf("""Matica sústavy""", """Pravá strana""", """Množina riešení"""),
    minutes = 8,
)

internal val K4Proofs = listOf(k4pPostacujuca, k4pNutna, k4pPodpriestorSlr, k4pVarietaSlr)

/* ---- theorems -------------------------------------------------------------------------------------------------- */

internal val k4tHomogenna = Theorem(
    "la1-k4-t-homogenna",
    "Riešenia homogénnej sústavy",
    "Skriptá, Pozorování 4.1",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{m,n}§.""")),
        listOf(p("""Množina §S_0§ všech řešení homogenní soustavy §\mathbf{A}\mathbf{x}=\theta§ je podprostor §T^{n,1}§.""")),
    ),
    "§\\theta§ je riešenie a súčet aj násobok riešení sú riešenia. Preto má §S_0§ dimenziu a, ak §S_0\\neq\\{\\theta\\}§, aj bázu.",
    shortProof("""My jsme si o řešení homogenních soustav vlastně již ukázali (Ač jsme tomu tehdy ještě tak neříkali.), že je podprostor §T^n§.""", "Skriptá, odvodenie Pozorování 4.1"),
    short = "o riešeniach homogénnej sústavy",
)

internal val k4tFrobenius = Theorem("la1-k4-t-frobenius",
    "Frobeniova veta",
    "Skriptá, Věta 4.2",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{m,n}§ a §\mathbf{b}\in T^{m,1}§. Uvažujme soustavu §m§ lineárních rovnic pro §n§ neznámých §\mathbf{A}\mathbf{x}=\mathbf{b}§.""")),
        listOf(p("""Potom:"""), i("""Tato soustava je řešitelná ==právě tehdy, když== §§h(\mathbf{A})=h(\mathbf{A}\mid\mathbf{b})§§."""), i("""Je-li §h(\mathbf{A}\mid\mathbf{b})=h(\mathbf{A})§ a §\tilde{\mathbf{x}}\in T^n§ splňuje §\mathbf{A}\tilde{\mathbf{x}}=\mathbf{b}§, pak §§S=\tilde{\mathbf{x}}+S_{0}§§."""), i("""Množina řešení homogenní soustavy §\mathbf{A}\mathbf{x}=\theta§ je podprostor dimenze §n-h(\mathbf{A})§.""")),
        core = """\begin{aligned}&S\ne\emptyset\Leftrightarrow h(\mathbf{A})=h(\mathbf{A}|\mathbf{b}),\\&S=\tilde{\mathbf{x}}+S_0\ \text{pro }\tilde{\mathbf{x}}\in S,\\&\dim S_0=n-h(\mathbf{A})\end{aligned}""",
    ),
    "Riešenie existuje, keď pravá strana nepridá hodnosť. Všetky riešenia sú jedno riešenie plus riešenia homogénnej sústavy, ktoré tvoria podpriestor dimenzie §n-h(\\mathbf{A})§.",
    proved(k4pPostacujuca, k4pNutna),
    note = "Na skúške „Napište kompletní znění Frobeniovy věty“ za 4 b (17. 12. 2025, 26. 1. 2026). Začni deklaráciou §\\mathbf{A}\\in T^{m,n}§, §\\mathbf{b}\\in T^{m,1}§. Dôkaz 1. bodu je tu po riadkoch (oba smery). Pri 2. bode skriptá len odkážu na tvar riešenia známy z kapitoly o sústavách. Dôkaz 3. bodu (dimenzia §S_0§) skriptá majú, Kleprlík ho však nevyžaduje: „ten je jako dobrovolně ve skriptech, ale jako nikdo po vás ho chtít nebude“ (8. prednáška).",
    short = "o riešiteľnosti sústavy",
    remarks = listOf(quote("Skriptá, Poznámka 4.3", p("""Vektor §\tilde{\mathbf{x}}§ z druhého bodu předchozí věty se nazývá **partikulární řešení**."""), p("""Třetí bod předchozí věty můžeme také zapsat v následující formě"""), i("""Pokud je §h(\mathbf{A})=n§, pak §S_0=\{\theta\}§."""), i("""Pokud §h:=h(\mathbf{A})<n§, pak existuje LN soubor §(\mathbf{z}_1,\dots,\mathbf{z}_{n-h})§ vektorů z §T^n§ tak, že"""), b("""S_0=\langle\mathbf{z}_1,\dots,\mathbf{z}_{n-h}\rangle."""))),
)

internal val k4tRegularna = Theorem("la1-k4-t-regularna",
    "Sústava s regulárnou maticou",
    "Skriptá, Poznámka 4.4",
    wording(
        listOf(),
        listOf(p("""Jak plyne z Frobeniovy věty, je-li matice soustavy §\mathbf{A}§ čtvercová a regulární, existuje pro jakýkoli vektor pravých stran §\mathbf{b}§ ==právě jedno== řešení soustavy §\mathbf{A}\mathbf{x}=\mathbf{b}§. Jak vypadá toto řešení? Stačí obě strany rovnice vynásobit zleva inverzní maticí §\mathbf{A}^{-1}§ a dostaneme"""), b("""\mathbf{x}=\mathbf{E}\mathbf{x}=\mathbf{A}^{-1}\mathbf{A} \mathbf{x}\softbreak[1]=\mathbf{A}^{-1}\mathbf{b}.""")),
        core = """\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}""",
    ),
    "Plná hodnosť: §h(\\mathbf{A})=n=h(\\mathbf{A}\\mid\\mathbf{b})§, riešenie existuje a §S_0=\\{\\theta\\}§, takže je jediné.",
    shortProof("""Stačí obě strany rovnice vynásobit zleva inverzní maticí §\mathbf{A}^{-1}§ a dostaneme §\mathbf{x}=\mathbf{E}\mathbf{x}=\mathbf{A}^{-1}\mathbf{A}\mathbf{x}=\mathbf{A}^{-1}\mathbf{b}§.""", "Skriptá, Poznámka 4.4"),
    short = "o sústave s regulárnou maticou",
)

internal val k4tPostup = Theorem("la1-k4-t-postup",
    "Riešenie homogénnej sústavy",
    "Skriptá, Postup 4.5",
    wording(
        listOf(p("""Řešíme soustavu §\mathbf{A}\mathbf{x}=\theta§ s rozšířenou maticí §(\mathbf{A}\mid\theta)§, kde §\mathbf{A}\in T^{m,n}§.""")),
        listOf(i("""Nemá-li zadaná soustava matici v horním stupňovitém tvaru, převedeme ji na něj pomocí GEM (množina řešení se nemění)."""), i("""Za vázané proměnné označíme proměnné příslušející hlavním sloupcům matice. Zbývající volné proměnné označme např. §(t_1,\dots,t_{n-h})§."""), i("""Zvolíme libovolnou bázi prostoru §T^{n-h}§. Potom je jakákoliv volba volných proměnných v lineárním obalu této báze."""), i("""Pro každý zvolený bazický vektor §(t_1,\dots,t_{n-h})\in T^{n-h}§ reprezentující volbu volných proměnných dopočítáme ze soustavy vázané proměnné."""), i("""Dostáváme LN soubor §n-h§ vektorů, který generuje §S_0§.""")),
        core = """S_0=\langle\mathbf{z}_1,\dots,\mathbf{z}_{n-h}\rangle\ \softbreak[1]\text{pro}\ h(\mathbf{A})<n""",
    ),
    """Zvolíš bázu voľných premenných a pri každom jej vektore dopočítaš viazané premenné. Dostaneš bázu §S_0§. Keď voľné premenné nie sú, teda §h(\mathbf{A})=n§, je §S_0=\{\theta\}§ a bázu nehľadáš (Poznámka 4.3).""",
    shortProof("""Zvolíme-li jakoukoli bázi podprostoru §T^{n-h}§ a pro každý bazický vektor reprezentující nějakou volbu volných proměnných dopočítáme vázané proměnné, zůstane tento soubor LN. Takto získáme LN soubor §n-h§ vektorů v podprostoru §S_0§ dimenze §n-h§. Tedy se nutně jedná o bázi §S_0§.""", "Skriptá, zdôvodnenie Postupu 4.5"),
    short = "riešenia sústavy",
)

internal val k4tVarieta = Theorem(
    "la1-k4-t-varieta",
    "Vlastnosti variety",
    "Skriptá, Věta 4.18",
    wording(
        listOf(p("""Buď §W\subseteq T^n§ lineární varieta.""")),
        listOf(i("""Vektor posunutí je součástí variety, tj. je-li §\mathbf{a}\in T^n§, §P\ssub T^n§, pak §\mathbf{a}\in \mathbf{a}+P§."""), i("""==Každý== její vektor je současně vektorem posunutí, tj. pro každý vektor §\mathbf{b}\in W§ platí §W=\mathbf{b}+Z(W)§."""), i("""Zaměření variety §Z(W)§ je určeno jednoznačně, tj. pokud pro nějaké podprostory §P,Q \ssub T^n§ platí §W=\mathbf{a}+P=\mathbf{a}+Q§, pak §P=Q§."""), i("""Mějme §\mathbf{a},\mathbf{b} \in T^n§ a §P,Q\ssub T^n§. Potom pro dvě variety platí §\mathbf{a}+P=\mathbf{b}+Q§ právě tehdy, když §P=Q§ a zároveň §\mathbf{b}-\mathbf{a}\in P§.""")),
        core = """\begin{aligned}&\mathbf{a}\in\mathbf{a}+P\\&\mathbf{b}\in W\Rightarrow W=\mathbf{b}+Z(W)\\&\mathbf{a}+P=\mathbf{a}+Q\Rightarrow P=Q\\&\mathbf{a}+P=\mathbf{b}+Q\\&\quad\Leftrightarrow P=Q\wedge\mathbf{b}-\mathbf{a}\in P\end{aligned}""",
    ),
    "Posunúť môžeš o ktorýkoľvek bod variety, zameranie je vždy to isté. Dva zápisy opisujú tú istú varietu, keď majú rovnaké zameranie a rozdiel posunutí v ňom leží.",
    shortProof("""1. Jelikož §\theta§ vždy patří do §P§, dostaneme §\mathbf{a}=\mathbf{a}+\theta\in\mathbf{a}+P§. 2. Mějme varietu §W=\mathbf{a}+Z(W)§. Máme-li vektor §\mathbf{b}§ z §W§, pak existuje §\mathbf{v}\in Z(W)§ tak, že §\mathbf{b}=\mathbf{a}+\mathbf{v}§. Odkud dostáváme, že §\mathbf{b}-\mathbf{a}=\mathbf{v}\in Z(W)§. §\mathbf{b}+Z(W)=(\mathbf{a}+\mathbf{b}-\mathbf{a})+Z(W)=\mathbf{a}+(\mathbf{b}-\mathbf{a}+Z(W))=\mathbf{a}+Z(W)§. 3. Existují-li dva podprostory §P,Q\ssub T^n§ takové, že §W=\mathbf{a}+P=\mathbf{a}+Q§, dostaneme přičtením vektoru §-\mathbf{a}§ k oběma stranám rovnosti ihned, že §P=Q§. 4. §\Rightarrow§: Z rovnosti variet speciálně platí §\mathbf{b}\in\mathbf{a}+P§. Proto existuje §\mathbf{v}\in P§ tak, že §\mathbf{b}=\mathbf{a}+\mathbf{v}§, z čehož plyne §\mathbf{b}-\mathbf{a}=\mathbf{v}\in P§. Z předpokladu §\mathbf{a}+P=\mathbf{b}+Q§ plyne, že prvek §\mathbf{a}=\mathbf{a}+\theta§ z variety §\mathbf{a}+P§ náleží také do variety §\mathbf{b}+Q§. Díky předpokladu a 1. bodu získáme, že §\mathbf{a}+P=\mathbf{b}+Q=\mathbf{a}+Q§. Rovnost §P=Q§ pak plyne přímo z 3. bodu o jednoznačnosti zaměření variety. §\Leftarrow§: §\mathbf{a}+P=\mathbf{b}+(\mathbf{a}-\mathbf{b})+P=\mathbf{b}+P=\mathbf{b}+Q§.""", "Skriptá, dôkaz Věty 4.18"),
    short = "o vlastnostiach variety",
    remarks = listOf(quote("Skriptá, Poznámka 4.19", p("""Varietu o dimenzi §0§ nazýváme **bod**. Varietu o dimenzi §1§ nazýváme **přímka**. Varietu o dimenzi §2§ nazýváme **rovina**. Varietu o dimenzi §n-1§ nazýváme **nadrovina**."""))),
)

internal val k4tPodpriestorSlr = Theorem("la1-k4-t-podpriestor-slr",
    "Podpriestor je množina riešení",
    "Skriptá, Věta 4.23",
    wording(
        listOf(p("""Nechť §P§ je podmnožina §T^n§.""")),
        listOf(p("""Pak §P§ je podprostorem právě tehdy, když existuje homogenní soustava lineárních rovnic §\mathbf{A}\mathbf{x}=\theta§, jejíž množinou řešení je §P§.""")),
        core = """\begin{aligned}P\ssub T^n&\ \Leftrightarrow\ (\exists\mathbf{A})\ P=S_0\\h(\mathbf{A})&=n-\dim P\end{aligned}""",
        rest = listOf(p("""Navíc je pro tuto soustavu splněno §§h(\mathbf{A})=n-\dim P§§.""")),
    ),
    "Bázu §P§ napíš do riadkov a vyrieš homogénnu sústavu; báza jej riešení sú riadky §\\mathbf{A}§. Frobeniova veta potom dá §\\dim S_0=n-h(\\mathbf{A})=\\dim P§.",
    proved(k4pPodpriestorSlr),
    note = "Stránka predmetu: „Umět dokázat, že každá varieta je řešením nějaké soustavy lin. rovnic.“ Na skúške 26. 1. 2026 za 6 b.",
    short = "o podpriestore ako množine riešení",
)

internal val k4tVarietaSlr = Theorem("la1-k4-t-varieta-slr",
    "Varieta je množina riešení",
    "Skriptá, Věta 4.26",
    wording(
        listOf(p("""Nechť §M\subseteq T^n§ je ==neprázdná==.""")),
        listOf(p("""Pak §M§ je lineární varietou právě tehdy, když existuje soustava lineárních rovnic §\mathbf{A}\mathbf{x}=\mathbf{b}§, jejíž množinou řešení je §M§.""")),
        core = """\begin{aligned}&M\text{ je varieta}\\\Leftrightarrow\ &(\exists\mathbf{A},\mathbf{b})\ M=S\\&h(\mathbf{A})=n-\dim M\end{aligned}""",
        rest = listOf(p("""Navíc je pro tuto soustavu splněno"""), b("""h(\mathbf{A})=n-\dim M.""")),
    ),
    "Maticu vezmeš z Věty 4.23 pre zameranie, pravú stranu dopočítaš: §\\mathbf{b}=\\mathbf{A}\\mathbf{a}§.",
    proved(k4pVarietaSlr),
    short = "o variete ako množine riešení",
)

internal val k4tPrevod = Theorem("la1-k4-t-prevod",
    "Neparametrické rovnice variety",
    "Skriptá, Postup 4.30",
    wording(
        listOf(p("""Máme zadanou lineární varietu §W=\mathbf{a}+P§ o dimenzi §\dim W=k§.""")),
        listOf(p("""Hledáme soustavu lineárních rovnic §\mathbf{A}\mathbf{x}=\mathbf{b}§ s maticí §(\mathbf{A}\mid\mathbf{b})§, jejíž množinou všech řešení je právě §W§."""), i("""Je-li §k=0§, pak hledanou soustavou je §(\mathbf{E}\mid\mathbf{a})§."""), i("""Je-li §k=n§, hledanou soustavou je §(\Theta\mid\theta)§."""), i("""Pro §k\in\hat{n-1}§ označme bázi §P§ jako §(\mathbf{u}_1,\dots,\mathbf{u}_k)§ a vyřešme homogenní soustavu s maticí §\mathbf{Y}§, která má v řádcích vektory §\mathbf{u}_1,\dots,\mathbf{u}_k§:"""), b("""\begin{pmatrix}\mathbf{u}_1^T&0\\\vdots&\vdots\\\mathbf{u}_k^T&0\end{pmatrix}\,."""), i("""Hledanou matici §\mathbf{A}§ po řádcích sestavíme z vektorů libovolné báze řešení §(\mathbf{Y}\mid\theta)§."""), i("""Hledaný vektor §\mathbf{b}§ získáme vynásobením §\mathbf{A}\mathbf{a}=\mathbf{b}§.""")),
        core = """\begin{aligned}Z(W)&=S_0\\\mathbf{b}&=\mathbf{A}\mathbf{a}\\W&=\{\mathbf{x}\mid\mathbf{A}\mathbf{x}=\mathbf{b}\}\end{aligned}""",
    ),
    "Parametrické rovnice dávajú body variety ako posunutie plus kombinácie smerových vektorov. Neparametrické sú sústava rovníc. Z rovníc k parametrom vyriešiš sústavu; z parametrov k rovniciam ideš cez Postup 4.30.",
    shortProof("""Druhý směr převodu se v podstatě řídí postupem konstruktivního důkazu Věty 4.26, heslovitě si jej tady ještě zopakujeme.""", "Skriptá, zdôvodnenie Postupu 4.30"),
    note = "Stránka predmetu: „Umět přejít z parametrických rovnic na neparametrické a zpět.“",
    short = "prechodu na neparametrické rovnice",
    remarks = listOf(quote("Skriptá, Poznámka 4.31", p("""Nechť §W\subseteq T^n§ je lineární varieta, označme bázi §Z(W)§ jako §(\mathbf{u}_1,\dots,\mathbf{u}_k)§."""), p("""Vztah §W=\mathbf{a}+\langle\mathbf{u}_1,\dots,\mathbf{u}_k\rangle§ lze vyjádřit jako"""), b("""\mathbf{x}\in W\softbreak[1]\Leftrightarrow\exists\alpha_1,\dots,\alpha_k\in T:\ \softbreak[2]\mathbf{x}=\mathbf{a}+\sum_{i=1}^k\alpha_i\mathbf{u}_i\,."""), p("""**Parametrickými rovnicemi variety** §W§ rozumíme rovnici"""), b("""\mathbf{x}=\mathbf{a}+\sum_{i=1}^k\alpha_i\mathbf{u}_i"""), p("""rozepsanou po složkách. (Neboli máme §n§ rovnic s §k§ parametry §\alpha_1,\dots,\alpha_k§, jedna rovnice pro každou složku §\mathbf{x}§.)"""), p("""**Neparametrickými rovnicemi variety** §W§ rozumíme rozepsanou soustavu lineárních rovnic jako v"""), b("""\begin{aligned}a_{11}x_1+\cdots+a_{1n}x_n&=b_1\\a_{21}x_1+\cdots+a_{2n}x_n&=b_2\\&\ \ \vdots\\a_{m1}x_1+\cdots+a_{mn}x_n&=b_m\end{aligned}"""), p("""odpovídající soustavě"""), b("""\mathbf{A}\mathbf{x}=\mathbf{b}"""), p("""určující varietu §W§. (Neboli máme tolik rovnic, kolik je řádků matice §\mathbf{A}§, s §n§ neznámými.)"""))),
)

internal val k4tPostupNehomogenna = Theorem("la1-k4-t-postup-nehomogenna",
    "Riešenie nehomogénnej sústavy",
    "Skriptá, Postup 4.10",
    wording(
        listOf(p("""Řešíme soustavu §\mathbf{A}\mathbf{x}=\mathbf{b}§ s rozšířenou maticí §(\mathbf{A}\mid\mathbf{b})§, kde §\mathbf{A}\in T^{m,n}§ a §\mathbf{b}\in T^{m,1}§.""")),
        listOf(i("""Nemá-li zadaná soustava matici v horním stupňovitém tvaru, převedeme ji na něj pomocí GEM (množina řešení se nemění)."""), i("""Pokud §h(\mathbf{A}) < h(\mathbf{A}\mid\mathbf{b})§, řešení neexistuje. V opačném případě postupujeme dále."""), i("""Za vázané proměnné označíme proměnné příslušející hlavním sloupcům matice. Zbývající volné proměnné označíme §(t_1,\dots,t_{n-h})§."""), i("""Pro nalezení §\tilde{\mathbf{x}}§ zvolme za §(t_1,\dots,t_{n-h})§ libovolně a ze soustavy §(\mathbf{A}\mid\mathbf{b})§ dopočítejme vázané proměnné."""), i("""Jako dříve v Algoritmu 4.5 dopočítáme ze soustavy §(\mathbf{A}\mid\theta)§ bázi §S_0§."""), i("""Řešením je §S = \tilde{\mathbf{x}} + S_0§.""")),
        core = """S=\tilde{\mathbf{x}}+S_0""",
    ),
    """Jedno partikulárne riešenie spočítaš s pravou stranou a bázu homogénnych riešení s nulovou pravou stranou. Pri §h(\mathbf{A})=n§ je §S_0=\{\theta\}§, a tak §S=\{\tilde{\mathbf{x}}\}§ (Poznámka 4.3).""",
    shortProof("""Z Frobeniovy věty i dříve odvozených výsledků víme, že množina všech řešení soustavy lineárních rovnic §\mathbf{A}\mathbf{x}=\mathbf{b}§ je buď prázdná nebo lze popsat ve tvaru: §S=\tilde{\mathbf{x}}+S_0§, kde §\tilde{\mathbf{x}}§ je jakékoli řešení soustavy §\mathbf{A}\mathbf{x}=\mathbf{b}§.""", "Skriptá, zdôvodnenie Postupu 4.10"),
    short = "riešenia nehomogénnej sústavy",
    remarks = listOf(quote("Skriptá, Varování 4.11", p("""Nezapomeňte počítat bázi §S_0§ pomocí §(\mathbf{A}\mid\theta)§, ==nikoliv== §(\mathbf{A}\mid\mathbf{b})§! Lehko se v tom opomenutím udělá chyba."""))),
)

internal val k4tNadrovina = Theorem("la1-k4-t-nadrovina",
    "Nadrovina ako jedna rovnica",
    "Skriptá, Důsledek 4.28",
    wording(
        listOf(),
        listOf(p("""Množina §M\subseteq T^n§ je nadrovinou, právě když je množinou řešení jedné lineární rovnice"""), b("""\alpha_1x_1+\dots+\alpha_nx_n=\beta\,,"""), p("""kde alespoň jeden z koeficientů §\alpha_1,\dots,\alpha_n§ je nenulový.""")),
        core = """M\text{ je nadrovina}\softbreak[1]\ \Leftrightarrow\ M=\{\mathbf{x}\mid\textstyle\sum_{i=1}^n\alpha_ix_i=\beta\},\ \softbreak[2](\alpha_1,\dots,\alpha_n)\ne\theta""",
    ),
    """Nadrovinu opisuje jedna lineárna rovnica s aspoň jedným nenulovým koeficientom.""",
    shortProof("""§(\Rightarrow)§: Je-li §M§ nadrovinou, tak dle Věty 4.26 existuje matice §\mathbf{A}§ hodnosti §1§ taková, že množinou řešení §(\mathbf{A}\mid\mathbf{b})§ je právě §M§; soustava je řešitelná, proto i §h(\mathbf{A}\mid\mathbf{b})=1§. Po GEM tedy obdržíme právě jeden nenulový řádek, tj. právě jednu rovnici §\alpha_1x_1+\dots+\alpha_nx_n=\beta§, kde alespoň jeden z koeficientů je nenulový (jinak by soustava nebyla řešitelná). §(\Leftarrow)§: Je to přímý důsledek Frobeniovy věty: rovnice odpovídá soustavě §\mathbf{A}\mathbf{x}=\mathbf{b}§, kde §\mathbf{A}§ má jeden nenulový řádek, tedy §h(\mathbf{A})=1§ a §\dim M=\dim S_0=n-h(\mathbf{A})=n-1§.""", "Skriptá, dôkaz Důsledku 4.28"),
    short = "o nadrovine ako riešení rovnice",
)

internal val k4tRovnostMnozin = Theorem("la1-k4-t-rovnost-mnozin",
    "Rovnosť dvoch množín riešení",
    "Skriptá, Pozorování 4.14",
    wording(
        listOf(p("""Mějme vektory §\mathbf{u},\mathbf{v},\mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}§ z §T^{n}§ a nechť je soubor §(\mathbf{y}_1,\dots,\mathbf{y}_k)§ i soubor §(\mathbf{z}_1,\dots,\mathbf{z}_k)§ lineárně nezávislý.""")),
        listOf(p("""Potom rovnost"""), b("""\mathbf{u}+\langle \mathbf{y}_1,\dots,\mathbf{y}_k\rangle\softbreak[1]=\mathbf{v}+\langle \mathbf{z}_1,\dots,\mathbf{z}_k\rangle"""), p("""platí ==právě tehdy, když== matice, jejíž řádky jsou vektory §\mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}, \mathbf{u}-\mathbf{v}§, má hodnost rovnu §k§.""")),
    ),
    "Obe množiny sú rovnaké, keď majú rovnaké zameranie a rozdiel posunutí v ňom leží. Obe podmienky naraz overí hodnosť jednej matice.",
    shortProof("""§(\Rightarrow)§: Platí-li rovnost množin řešení, pak §\mathbf{u}-\mathbf{v}+\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle§. Z rovnosti rovnou plyne, že §\mathbf{u}-\mathbf{v}=\mathbf{u}-\mathbf{v}+\theta\in\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle§. Navíc díky předchozímu lemmatu a první rovnosti v důkazu platí §\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=(\mathbf{v}-\mathbf{u})+\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle§, neboli oba lineární obaly se rovnají. Celkově máme §\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}, \mathbf{u}-\mathbf{v}\rangle§ a dimenze obou těchto obalů je rovna §k§. §(\Leftarrow)§: Nechť matice, jejíž řádky jsou vektory §\mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}, \mathbf{u}-\mathbf{v}§, má hodnost rovnu §k§. Protože soubor §(\mathbf{y}_{1},\dots,\mathbf{y}_{k})§ je LN, získáme §\mathbf{u}-\mathbf{v}\in\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle§, tedy s pomocí předchozího lemmatu vyplyne §\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\mathbf{u}-\mathbf{v}+\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle§. Dále protože §\dim\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\dim\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=\dim\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k},\mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=k§, dostáváme z Pozorování 2.65 o rovnosti lineárních obalů rovnost §\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle§. Celkem máme §\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle=\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\mathbf{u}-\mathbf{v}+\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle§. Nakonec si stačí opět uvědomit, že předchozí rovnost platí právě tehdy, když platí §\mathbf{u}+\langle \mathbf{y}_{1},\dots,\mathbf{y}_{k}\rangle=\mathbf{v}+\langle \mathbf{z}_{1},\dots,\mathbf{z}_{k}\rangle§.""", "Skriptá, dôkaz Pozorování 4.14"),
    short = "o rovnosti dvoch množín riešení",
)

internal val K4Theorems = listOf(k4tHomogenna, k4tFrobenius, k4tRegularna, k4tPostup, k4tPostupNehomogenna, k4tVarieta, k4tPodpriestorSlr, k4tVarietaSlr, k4tNadrovina, k4tPrevod, k4tRovnostMnozin)

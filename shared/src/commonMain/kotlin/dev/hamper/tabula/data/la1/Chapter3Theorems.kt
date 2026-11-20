package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Theorems of kapitola 3 the course lists for the exam (classification.adoc: "Hodnost a regularita matice"), statements
 * word for word from chap-04.woo; proofs line by line where the course or a past exam wants them.
 */

private const val Circular = "Toto je kruh: opieraš sa o tvrdenie, ktoré sa práve dokazuje."

/* ---- proofs ---------------------------------------------------------------------------------------------------- */

internal val k3pJednoznacnost = Proof(
    "la1-k3-p-jednoznacnost",
    "Inverzná matica je najviac jedna",
    "Skúška 17. 12. 2025 (variant Frobenius), príklad 3C; Skriptá, Věta 3.25",
    null,
    null,
    m("""Nech §\ca{\mathbf{B}_1},\cb{\mathbf{B}_2}\in T^{n,n}§ sú obe inverzné k §\mathbf{A}§: §§\begin{aligned}\mathbf{A}\ca{\mathbf{B}_1}=\ca{\mathbf{B}_1}\mathbf{A}&=\mathbf{E},\\\mathbf{A}\cb{\mathbf{B}_2}=\cb{\mathbf{B}_2}\mathbf{A}&=\mathbf{E}.\end{aligned}§§ Ukážem §\ca{\mathbf{B}_1}=\cb{\mathbf{B}_2}§."""),
    listOf(
        r("Začni od §\\ca{\\mathbf{B}_1}§.", 1,
            c("""§\ca{\mathbf{B}_1}=\mathbf{A}^{-1}=\cb{\mathbf{B}_2}§""", """Zápis §\mathbf{A}^{-1}§ už predpokladá jedinú inverziu. Práve to dokazuješ."""),
            c("""§\ca{\mathbf{B}_1}=\ca{\mathbf{B}_1}\mathbf{E}§"""),
            c("""§\ca{\mathbf{B}_1}=\mathbf{E}§, lebo §\mathbf{A}\ca{\mathbf{B}_1}=\mathbf{E}§.""", """Z §\mathbf{A}\ca{\mathbf{B}_1}=\mathbf{E}§ neplynie §\ca{\mathbf{B}_1}=\mathbf{E}§. Platilo by to len pre §\mathbf{A}=\mathbf{E}§.""")),
        r("Nahraď §\\mathbf{E}§ tak, aby sa §\\ca{\\mathbf{B}_1}§ a §\\mathbf{A}§ stretli.", 0,
            c("""§\ca{\mathbf{B}_1}\mathbf{E}=\ca{\mathbf{B}_1}(\mathbf{A}\cb{\mathbf{B}_2})§, lebo §\mathbf{A}\cb{\mathbf{B}_2}=\mathbf{E}§."""),
            c("""§\ca{\mathbf{B}_1}\mathbf{E}=\mathbf{E}\ca{\mathbf{B}_1}=\cb{\mathbf{B}_2}§""", """Prvá rovnosť platí, druhá je tvrdenie samo. Z ničoho zatiaľ neplynie."""),
            c("""§\ca{\mathbf{B}_1}\mathbf{E}=\ca{\mathbf{B}_1}(\cb{\mathbf{B}_2}\mathbf{A})§""", """Rovnosť platí, ale §\ca{\mathbf{B}_1}§ a §\mathbf{A}§ sa v nej nestretnú. Tu stojí §\ca{\mathbf{B}_1}\cb{\mathbf{B}_2}§, o ktorom nič nevieš.""")),
        r("Dokonči.", 2,
            c("""§\ca{\mathbf{B}_1}(\mathbf{A}\cb{\mathbf{B}_2})=(\mathbf{A}\cb{\mathbf{B}_2})\ca{\mathbf{B}_1}=\ca{\mathbf{B}_1}§""", """Rovnosť platí, ale dáva len §\ca{\mathbf{B}_1}=\ca{\mathbf{B}_1}§. Cieľ je dostať na koniec §\cb{\mathbf{B}_2}§."""),
            c("""§\ca{\mathbf{B}_1}(\mathbf{A}\cb{\mathbf{B}_2})=\ca{\mathbf{B}_1}\mathbf{A}+\ca{\mathbf{B}_1}\cb{\mathbf{B}_2}§""", """Toto nie je distributivita: v zátvorke je súčin, nie súčet. Rozniesť sa dá len cez sčítanie."""),
            c("""Asociativita: §\ca{\mathbf{B}_1}(\mathbf{A}\cb{\mathbf{B}_2})=(\ca{\mathbf{B}_1}\mathbf{A})\cb{\mathbf{B}_2}=\mathbf{E}\cb{\mathbf{B}_2}=\cb{\mathbf{B}_2}§.""")),
    ),
    m("""§\ca{\mathbf{B}_1}=\cb{\mathbf{B}_2}§, inverzná matica k §\mathbf{A}§ existuje najviac jedna."""),
    listOf("Dve inverzné s rôznymi menami, obe rovnosti z definície", "§\\ca{\\mathbf{B}_1}=\\ca{\\mathbf{B}_1}\\mathbf{E}=\\ca{\\mathbf{B}_1}(\\mathbf{A}\\cb{\\mathbf{B}_2})§", "Asociativita, nikde komutativita", "Záver §\\ca{\\mathbf{B}_1}=\\cb{\\mathbf{B}_2}§"),
    goals = listOf("""Vloženie §\mathbf{E}§""", """Náhrada §\mathbf{E}=\mathbf{A}\cb{\mathbf{B}_2}§""", """Asociativita a záver"""),
    minutes = 6,
)

internal val k3pSucin = Proof(
    "la1-k3-p-inv-sucin",
    "Inverzia súčinu",
    "Skriptá, Věta 3.27 a jej dôkaz",
    null,
    "Len definícia inverznej matice a asociativita.",
    m("""Ukážem, že §\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}§ spĺňa definíciu inverznej matice k §\ca{\mathbf{A}}\cb{\mathbf{B}}§."""),
    listOf(
        r("Prvé poradie.", 1,
            c("""§(\ca{\mathbf{A}}\cb{\mathbf{B}})(\ca{\mathbf{A}}^{-1}\cb{\mathbf{B}}^{-1})=\mathbf{E}§""", """V tomto poradí sa §\cb{\mathbf{B}}§ s §\cb{\mathbf{B}}^{-1}§ nestretnú. Medzi nimi stojí §\ca{\mathbf{A}}^{-1}§."""),
            c("""§(\ca{\mathbf{A}}\cb{\mathbf{B}})(\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1})=\ca{\mathbf{A}}(\cb{\mathbf{B}}\cb{\mathbf{B}}^{-1})\ca{\mathbf{A}}^{-1}=\ca{\mathbf{A}}\ca{\mathbf{A}}^{-1}=\mathbf{E}§"""),
            c("""§(\ca{\mathbf{A}}\cb{\mathbf{B}})^{-1}=\ca{\mathbf{A}}^{-1}\cb{\mathbf{B}}^{-1}§ ako pri číslach.""", """Pravidlo z čísel tu neplatí, matice nekomutujú. Poradie sa pri inverzii obráti.""")),
        r("Stačí jedna rovnosť?", 0,
            c("""Nie, aj druhé poradie: §(\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1})(\ca{\mathbf{A}}\cb{\mathbf{B}})=\cb{\mathbf{B}}^{-1}(\ca{\mathbf{A}}^{-1}\ca{\mathbf{A}})\cb{\mathbf{B}}=\cb{\mathbf{B}}^{-1}\cb{\mathbf{B}}=\mathbf{E}§."""),
            c("""Áno, súčin matíc s §\mathbf{E}§ je vždy komutatívny.""", """Definícia 3.23 chce obe rovnosti. Že jedna stačí, hovorí až Věta 3.42, a tú tu nepoužívame."""),
            c("""Áno, lebo §\ca{\mathbf{A}}§ a §\cb{\mathbf{B}}§ sú regulárne.""", """Regularita §\ca{\mathbf{A}},\cb{\mathbf{B}}§ druhú rovnosť nedá zadarmo. Treba ju vypočítať.""")),
        r("Záver.", 1,
            c("""Teda §\ca{\mathbf{A}}\cb{\mathbf{B}}=\mathbf{E}§.""", """To nevyšlo a ani neplatí. Vyšlo, že súčin s §\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}§ je §\mathbf{E}§."""),
            c("""Podľa definície je §\ca{\mathbf{A}}\cb{\mathbf{B}}§ regulárna a §(\ca{\mathbf{A}}\cb{\mathbf{B}})^{-1}=\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}§."""),
            c("""Teda §\ca{\mathbf{A}}^{-1}\cb{\mathbf{B}}^{-1}=\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}§.""", """Toto vo všeobecnosti neplatí. Matice nekomutujú, ani ich inverzie.""")),
    ),
    m("""§(\ca{\mathbf{A}}\cb{\mathbf{B}})^{-1}=\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}§: poradie sa obráti."""),
    listOf("Kandidát §\\cb{\\mathbf{B}}^{-1}\\ca{\\mathbf{A}}^{-1}§ v obrátenom poradí", "Súčin v jednom poradí cez asociativitu", "Súčin v druhom poradí", "Záver z definície: regulárna a tvar inverzie"),
    goals = listOf("""Prvé poradie""", """Druhé poradie""", """Záver z definície"""),
    minutes = 6,
)

internal val k3pTranspozicia = Proof(
    "la1-k3-p-inv-transp",
    "Inverzia transponovanej",
    "Skriptá, Věta 3.28 a jej dôkaz",
    null,
    "Len definícia inverznej matice a vlastnosti transpozície.",
    m("""Ukážem, že §(\mathbf{A}^{-1})^T§ je inverzná k §\mathbf{A}^T§. Použijem §(\mathbf{X}\mathbf{Y})^T=\mathbf{Y}^T\mathbf{X}^T§ a §\mathbf{E}^T=\mathbf{E}§."""),
    listOf(
        r("Prvé poradie.", 2,
            c("""§\mathbf{A}^T(\mathbf{A}^{-1})^T=(\mathbf{A}^T\mathbf{A}^{-1})^T§""", """Transpozícia sa nedá len tak vytiahnuť pred zátvorku. Vpravo je §(\mathbf{A}^{-1})^T\mathbf{A}§, a to vo všeobecnosti nie je §\mathbf{E}§."""),
            c("""§\mathbf{A}^T(\mathbf{A}^{-1})^T=\mathbf{A}^{-1}\mathbf{A}^T§""", """Transpozícia sa nedá presunúť z jedného činiteľa na druhý. §\mathbf{A}^{-1}\mathbf{A}^T§ vo všeobecnosti nie je §\mathbf{E}§."""),
            c("""§\mathbf{A}^T(\mathbf{A}^{-1})^T=(\mathbf{A}^{-1}\mathbf{A})^T=\mathbf{E}^T=\mathbf{E}§""")),
        r("Druhé poradie.", 0,
            c("""§(\mathbf{A}^{-1})^T\mathbf{A}^T=(\mathbf{A}\mathbf{A}^{-1})^T=\mathbf{E}^T=\mathbf{E}§"""),
            c("""Netreba, transpozícia poradie nemení.""", """Definícia 3.23 chce obe rovnosti. A transpozícia poradie v súčine naopak mení."""),
            c("""§(\mathbf{A}^{-1})^T\mathbf{A}^T=(\mathbf{A}^{-1})^T\mathbf{A}=\mathbf{E}§""", """Transpozícia pri §\mathbf{A}§ sa stratila. Pravidlo dáva §(\mathbf{A}^{-1})^T\mathbf{A}^T=(\mathbf{A}\mathbf{A}^{-1})^T§.""")),
    ),
    m("""§\mathbf{A}^T§ je regulárna a §(\mathbf{A}^T)^{-1}=(\mathbf{A}^{-1})^T§."""),
    listOf("Kandidát §(\\mathbf{A}^{-1})^T§", "Transpozícia súčinu s obráteným poradím", "Obe poradia, §\\mathbf{E}^T=\\mathbf{E}§", "Záver z definície"),
    goals = listOf("""Prvé poradie""", """Druhé poradie"""),
    minutes = 5,
)

internal val k3pHodnostSucinu = Proof(
    "la1-k3-p-hodnost-sucinu",
    "Hodnosť súčinu",
    "Skriptá, Věta 3.19 a jej dôkaz",
    null,
    null,
    m("""Najprv §h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq h(\cb{\mathbf{B}})§ cez riadky, potom transpozíciou §h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq h(\ca{\mathbf{A}})§. Transpozície pri riadkoch pre prehľadnosť vynechávam."""),
    listOf(
        r("Čo sú riadky §\\ca{\\mathbf{A}}\\cb{\\mathbf{B}}§?", 0,
            c("""§(\ca{\mathbf{A}}\cb{\mathbf{B}})_{i:}=\ca{\mathbf{A}}_{i:}\cb{\mathbf{B}}§ je lineárna kombinácia riadkov §\cb{\mathbf{B}}§ (Věty 3.18 a 3.15)."""),
            c("""Lineárne kombinácie riadkov §\ca{\mathbf{A}}§.""", """Z riadku §\ca{\mathbf{A}}§ sú len koeficienty. Kombinujú sa riadky §\cb{\mathbf{B}}§."""),
            c("""Lineárne kombinácie stĺpcov §\cb{\mathbf{B}}§.""", """Riadky súčinu kombinujú riadky §\cb{\mathbf{B}}§, nie stĺpce. Kombináciami stĺpcov §\ca{\mathbf{A}}§ sú stĺpce súčinu.""")),
        r("Čo z toho pre obaly a hodnosť?", 1,
            c("""Obaly riadkov §\ca{\mathbf{A}}\cb{\mathbf{B}}§ a §\cb{\mathbf{B}}§ sa rovnajú.""", """Rovnosť neplatí vždy. Pre §\ca{\mathbf{A}}=\Theta§ je obal riadkov súčinu len §\{\theta\}§."""),
            c("""§\langle(\ca{\mathbf{A}}\cb{\mathbf{B}})_{1:},\dots,(\ca{\mathbf{A}}\cb{\mathbf{B}})_{m:}\rangle\subseteq\langle\cb{\mathbf{B}}_{1:},\dots,\cb{\mathbf{B}}_{n:}\rangle§ (Pozorování 2.34), preto §h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq h(\cb{\mathbf{B}})§ (Věta 2.62)."""),
            c("""§h(\ca{\mathbf{A}}\cb{\mathbf{B}})=h(\ca{\mathbf{A}})\cdot h(\cb{\mathbf{B}})§""", """Hodnosti sa nenásobia. Napríklad §h(\mathbf{E}_2\mathbf{E}_2)=2\neq 4§.""")),
        r("Druhá nerovnosť.", 2,
            c("""Rovnako: riadky §\ca{\mathbf{A}}\cb{\mathbf{B}}§ sú kombinácie riadkov §\ca{\mathbf{A}}§.""", """Nie sú, to by bolo opačné poradie. Treba prejsť cez transpozíciu."""),
            c("""§h(\ca{\mathbf{A}}\cb{\mathbf{B}})=h(\ca{\mathbf{A}}^T\cb{\mathbf{B}}^T)\leq h(\ca{\mathbf{A}})§""", """§(\ca{\mathbf{A}}\cb{\mathbf{B}})^T§ je §\cb{\mathbf{B}}^T\ca{\mathbf{A}}^T§. Poradie sa obráti, a súčin §\ca{\mathbf{A}}^T\cb{\mathbf{B}}^T§ ani nemusí byť definovaný."""),
            c("""§h(\ca{\mathbf{A}}\cb{\mathbf{B}})=h((\ca{\mathbf{A}}\cb{\mathbf{B}})^T)=h(\cb{\mathbf{B}}^T\ca{\mathbf{A}}^T)\leq h(\ca{\mathbf{A}}^T)=h(\ca{\mathbf{A}})§ (Věta 3.12 a prvá časť).""")),
    ),
    m("""§h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq h(\cb{\mathbf{B}})§ aj §h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq h(\ca{\mathbf{A}})§, teda §h(\ca{\mathbf{A}}\cb{\mathbf{B}})\leq\min\{h(\ca{\mathbf{A}}),h(\cb{\mathbf{B}})\}§."""),
    listOf("Riadky súčinu sú kombinácie riadkov §\\cb{\\mathbf{B}}§", "Inklúzia obalov, preto nerovnosť dimenzií", "Druhá nerovnosť transpozíciou, poradie sa obráti", "Záver s minimom"),
    goals = listOf("""Riadky súčinu""", """Odhad cez §h(\cb{\mathbf{B}})§""", """Odhad cez §h(\ca{\mathbf{A}})§"""),
    minutes = 8,
)

internal val k3pRegularita = Proof(
    "la1-k3-p-regularita",
    "Regulárna práve keď hodnosť §n§",
    "Skriptá, Věta 3.35 a jej dôkaz; skúška 17. 12. 2025 (variant Frobenius), príklad 3D a 3E",
    null,
    "Na skúške 17. 12. 2025: „Formulujte korektně tvrzení, které říká, jakou hodnost mají regulární matice.“ (2 b) a „Dokažte tvrzení z předchozího bodu.“ (5 b)",
    m("""Dokážem reťazec 1. §\Rightarrow§ 2. §\Rightarrow§ 3. §\Rightarrow§ 1. Potom je každé tvrdenie ekvivalentné s každým."""),
    listOf(
        r("1. §\\Rightarrow§ 2.: §\\mathbf{A}§ je regulárna.", 2,
            c("""§h(\mathbf{A})=h(\mathbf{A}^{-1})=n§""", """Hodnosť §\mathbf{A}^{-1}§ nepoznáš. Nerovnosť z Věty 3.19 treba použiť na §\mathbf{E}=\mathbf{A}\mathbf{A}^{-1}§."""),
            c("""Regulárna matica nemá nulový riadok, preto §h(\mathbf{A})=n§.""", """Nenulové riadky nestačia: §\begin{pmatrix}1&2\\2&4\end{pmatrix}§ ich má, a hodnosť je §1§."""),
            c("""Existuje §\mathbf{A}^{-1}§ a §n=h(\mathbf{E})=h(\mathbf{A}\mathbf{A}^{-1})\leq h(\mathbf{A})\leq n§ (Věta 3.19), teda §h(\mathbf{A})=n§.""")),
        r("2. §\\Rightarrow§ 3.: §h(\\mathbf{A})=n§.", 0,
            c("""GEM dá HST s §n§ nenulovými riadkami (Věta 3.7), teda §n§ hlavných stĺpcov a nenulové §b_{11},\dots,b_{nn}§ na diagonále."""),
            c("""Teda §\mathbf{A}=\mathbf{E}§.""", """Hodnosť §n§ má veľa matíc, napr. §2\mathbf{E}§ nad §\mathbb{R}§. Tvrdenie je §\mathbf{A}\sim\mathbf{E}§, nie rovnosť."""),
            c("""Riadky sú LN, a tak §\mathbf{A}\sim\mathbf{E}§ hneď.""", """Že GEM dôjde až k §\mathbf{E}§, treba ukázať krokmi. LN riadkov to samo nehovorí.""")),
        r("Ako ďalej k §\\mathbf{E}§?", 1,
            c("""Nulové riadky vyškrtnem a ostane §\mathbf{E}§.""", """Žiadne nulové riadky tu nie sú. A vyškrtnutie riadku nie je krok GEM."""),
            c("""Každý riadok vydelím jeho prvkom na diagonále (G2), potom násobkami nižších riadkov vynulujem prvky nad diagonálou (G3), od posledného stĺpca: §\mathbf{A}\sim\mathbf{B}\sim\mathbf{C}\sim\mathbf{E}§."""),
            c("""Stĺpcovými úpravami vynulujem prvky nad diagonálou.""", """§\sim§ znamená riadkové úpravy (Definice 3.4). Stĺpcové úpravy tu nemáš.""")),
        r("3. §\\Rightarrow§ 1.: §\\mathbf{A}\\sim\\mathbf{E}§.", 0,
            c("""Existuje regulárna §\mathbf{P}§ s §\mathbf{P}\mathbf{A}=\mathbf{E}§ (Tvrzení 3.32). Teda §\mathbf{A}=\mathbf{P}^{-1}\mathbf{P}\mathbf{A}=\mathbf{P}^{-1}\mathbf{E}=\mathbf{P}^{-1}§, a to je regulárna matica."""),
            c("""Z §\mathbf{P}\mathbf{A}=\mathbf{E}§ je §\mathbf{P}=\mathbf{A}^{-1}§ priamo z definície.""", """Definícia chce aj §\mathbf{A}\mathbf{P}=\mathbf{E}§. Věta 3.42 by stačila, ale jej dôkaz stojí na tejto vete."""),
            c("""GEM nemení regularitu, a §\mathbf{E}§ je regulárna.""", Circular)),
    ),
    m("""1. §\Rightarrow§ 2. §\Rightarrow§ 3. §\Rightarrow§ 1.: všetky tri tvrdenia sú ekvivalentné. Regulárne sú práve matice s hodnosťou §n§."""),
    listOf("Reťazec implikácií, ktorý sa uzavrie", "1 ⇒ 2: §n=h(\\mathbf{E})\\leq h(\\mathbf{A})§ (Věta 3.19) aj horná hranica §h(\\mathbf{A})\\leq n§ (Důsledek 3.13)", "2 ⇒ 3: HST s §n§ nenulovými riadkami, delenie a nulovanie nahor", "3 ⇒ 1: regulárna §\\mathbf{P}§ a §\\mathbf{A}=\\mathbf{P}^{-1}§"),
    goals = listOf("""Implikácia 1 ⇒ 2""", """Implikácia 2 ⇒ 3: HST""", """Úpravy až na §\mathbf{E}§""", """Implikácia 3 ⇒ 1"""),
    minutes = 12,
)

internal val k3pJednaStrana = Proof(
    "la1-k3-p-jedna-strana",
    "Stačí inverzia z jednej strany",
    "Skriptá, Věta 3.42 a jej dôkaz",
    null,
    null,
    m("""Prípad §\mathbf{A}\mathbf{B}=\mathbf{E}§; druhý je analogický."""),
    listOf(
        r("Prečo je §\\mathbf{A}§ regulárna?", 1,
            c("""Z definície, lebo §\mathbf{A}\mathbf{B}=\mathbf{E}§.""", """Definícia chce obe poradia. Druhé zatiaľ nemáš."""),
            c("""§n=h(\mathbf{E})=h(\mathbf{A}\mathbf{B})\leq h(\mathbf{A})\leq n§ (Věta 3.19), teda §h(\mathbf{A})=n§ a §\mathbf{A}§ je regulárna (Věta 3.35)."""),
            c("""§h(\mathbf{B})=n§, teda §\mathbf{A}§ je regulárna.""", """To hovorí o §\mathbf{B}§, nie o §\mathbf{A}§. Odhad treba pre §h(\mathbf{A})§.""")),
        r("Prečo §\\mathbf{B}=\\mathbf{A}^{-1}§?", 0,
            c("""§\mathbf{A}^{-1}=\mathbf{A}^{-1}\mathbf{E}=\mathbf{A}^{-1}(\mathbf{A}\mathbf{B})=(\mathbf{A}^{-1}\mathbf{A})\mathbf{B}=\mathbf{B}§"""),
            c("""Matice s výsledkom §\mathbf{E}§ komutujú, teda aj §\mathbf{B}\mathbf{A}=\mathbf{E}§.""", """To je obsah vety, nie argument. Zdôvodnenie dá až výpočet cez §\mathbf{A}^{-1}§."""),
            c("""§\mathbf{B}=\mathbf{A}\mathbf{B}=\mathbf{E}§""", """Rovnosť §\mathbf{B}=\mathbf{A}\mathbf{B}§ neplatí, násobenie §\mathbf{A}§ maticu mení. Platí len §\mathbf{A}\mathbf{B}=\mathbf{E}§.""")),
    ),
    m("""§\mathbf{A}§ je regulárna a §\mathbf{A}^{-1}=\mathbf{B}§. Pri overovaní inverzie stačí jeden súčin."""),
    listOf("Odhad hodnosti cez §h(\\mathbf{E})=n§", "Regularita z Věty 3.35", "§\\mathbf{A}^{-1}=\\mathbf{A}^{-1}\\mathbf{A}\\mathbf{B}=\\mathbf{B}§"),
    goals = listOf("""Regularita §\mathbf{A}§""", """§\mathbf{B}=\mathbf{A}^{-1}§"""),
    minutes = 6,
)

internal val k3pProste = Proof(
    "la1-k3-p-proste",
    "Regulárna matica dáva prosté zobrazenie",
    "Skúška 26. 1. 2026 (variant Hilbert), príklad 4E, bonus 5 b (zadanie podľa prepisu z fit-wiki)",
    wording(
        listOf(p("""Mějme §\mathbf{A}\in\mathbb{R}^{n,n}§ a zobrazení §\mathbf{A}:\mathbb{R}^n\to\mathbb{R}^n§, §\mathbf{A}(\mathbf{x}):=\mathbf{A}\mathbf{x}§.""")),
        listOf(p("""Dokažte, že pokud je §\mathbf{A}§ regulární matice, pak je toto zobrazení prosté.""")),
        core = """\mathbf{A}\text{ je regulárna}\softbreak[1]\ \Rightarrow\forall\mathbf{x},\mathbf{y}\in\mathbb{R}^n:\ \softbreak[2]\mathbf{A}\mathbf{x}=\mathbf{A}\mathbf{y}\ \Rightarrow\ \mathbf{x}=\mathbf{y}""",
    ),
    null,
    m("""Prosté znamená: z §\mathbf{A}\mathbf{x}=\mathbf{A}\mathbf{y}§ plynie §\mathbf{x}=\mathbf{y}§. Nech §\mathbf{A}\mathbf{x}=\mathbf{A}\mathbf{y}§."""),
    listOf(
        r("Čo s rovnosťou?", 2,
            c("""Vydelím obe strany §\mathbf{A}§.""", """Maticou sa nedelí. Delenie nahrádza násobenie inverznou maticou."""),
            c("""Vynásobím obe strany §\mathbf{A}^{-1}§ sprava.""", """§\mathbf{x}\mathbf{A}^{-1}§ nie je definované: §\mathbf{x}§ je typu §n\times 1§. Násobí sa zľava."""),
            c("""§\mathbf{A}§ je regulárna, existuje §\mathbf{A}^{-1}§; vynásobím zľava: §\mathbf{A}^{-1}(\mathbf{A}\mathbf{x})=\mathbf{A}^{-1}(\mathbf{A}\mathbf{y})§.""")),
        r("Uprav.", 0,
            c("""Asociativita: §(\mathbf{A}^{-1}\mathbf{A})\mathbf{x}=(\mathbf{A}^{-1}\mathbf{A})\mathbf{y}§, teda §\mathbf{E}\mathbf{x}=\mathbf{E}\mathbf{y}§ a §\mathbf{x}=\mathbf{y}§."""),
            c("""Teda §\mathbf{A}=\mathbf{E}§.""", """O §\mathbf{A}§ rovnosť nič také nehovorí. Ide o vektory §\mathbf{x},\mathbf{y}§."""),
            c("""Teda §\mathbf{A}(\mathbf{x}-\mathbf{y})=\theta§, a preto §\mathbf{x}-\mathbf{y}=\theta§ hneď.""", """Druhý krok chce zdôvodnenie. Bez §\mathbf{A}^{-1}§ alebo hodnosti z neho nič neplynie.""")),
    ),
    m("""Z §\mathbf{A}\mathbf{x}=\mathbf{A}\mathbf{y}§ plynie §\mathbf{x}=\mathbf{y}§, zobrazenie je prosté."""),
    listOf("Čo znamená prosté", "Násobenie zľava §\\mathbf{A}^{-1}§, ktorá existuje z regularity", "Asociativita a §\\mathbf{E}\\mathbf{x}=\\mathbf{x}§"),
    goals = listOf("""Násobenie §\mathbf{A}^{-1}§ zľava""", """Asociativita a záver"""),
    minutes = 5,
)

internal val k3pObrazLz = Proof(
    "la1-k3-p-obraz-lz",
    "Obraz LZ súboru je LZ",
    "Skúška 17. 12. 2025 (variant Frobenius), príklad 4E, bonus 4 b (zadanie podľa prepisu z fit-wiki)",
    wording(
        listOf(p("""Mějme §\mathbf{A}\in\mathbb{C}^{n,n}§ a LZ soubor §(\mathbf{x},\mathbf{y},\mathbf{z})§ vektorů z §\mathbb{C}^n§.""")),
        listOf(p("""Ukažte, že soubor §(\mathbf{A}\mathbf{x},\mathbf{A}\mathbf{y},\mathbf{A}\mathbf{z})§ je LZ.""")),
        core = """(\mathbf{A}\mathbf{x},\mathbf{A}\mathbf{y},\mathbf{A}\mathbf{z})\text{ je LZ}""",
    ),
    null,
    m("""Myšlienka: netriviálnu kombináciu rovnú §\theta§ vynásobím maticou §\mathbf{A}§; vyjde kombinácia obrazov s tými istými koeficientmi, stále netriviálna a rovná §\theta§. Začnem definíciou LZ."""),
    listOf(
        r("Čo vieš o §(\\mathbf{x},\\mathbf{y},\\mathbf{z})§?", 0,
            c("""Existujú §\alpha,\beta,\gamma\in\mathbb{C}§, nie všetky nulové, s §\alpha\mathbf{x}+\beta\mathbf{y}+\gamma\mathbf{z}=\theta§."""),
            c("""Niektorý z nich je nulový.""", """LZ súbor nulový vektor obsahovať nemusí. Definícia dáva netriviálnu kombináciu."""),
            c("""§\mathbf{z}=\mathbf{x}+\mathbf{y}§""", """Takúto konkrétnu rovnosť nevieš. Vieš len, že nejaká netriviálna kombinácia je §\theta§.""")),
        r("Vynásob maticou.", 1,
            c("""§\mathbf{A}(\alpha\mathbf{x}+\beta\mathbf{y}+\gamma\mathbf{z})=\alpha\beta\gamma\,\mathbf{A}\mathbf{x}\mathbf{y}\mathbf{z}§""", """Súčin vektorov definovaný nie je. Matica sa roznesie na každý sčítanec."""),
            c("""§\theta=\mathbf{A}\theta=\mathbf{A}(\alpha\mathbf{x}+\beta\mathbf{y}+\gamma\mathbf{z})=\alpha\mathbf{A}\mathbf{x}+\beta\mathbf{A}\mathbf{y}+\gamma\mathbf{A}\mathbf{z}§"""),
            c("""Treba §\mathbf{A}^{-1}§.""", """§\mathbf{A}§ nemusí byť regulárna. Stačí distributivita násobenia.""")),
        r("Záver.", 0,
            c("""Koeficienty §\alpha,\beta,\gamma§ nie sú všetky nulové: netriviálna kombinácia rovná §\theta§, súbor §(\mathbf{A}\mathbf{x},\mathbf{A}\mathbf{y},\mathbf{A}\mathbf{z})§ je LZ."""),
            c("""Vektory §\mathbf{A}\mathbf{x},\mathbf{A}\mathbf{y},\mathbf{A}\mathbf{z}§ sú nulové.""", """Nulové obrazy z toho neplynú, len ich netriviálna kombinácia rovná §\theta§. To stačí na LZ."""),
            c("""Súbor je LN, lebo §\mathbf{A}§ mení vektory.""", """Vyšla netriviálna kombinácia rovná §\theta§. To je presne definícia LZ.""")),
    ),
    m("""§(\mathbf{A}\mathbf{x},\mathbf{A}\mathbf{y},\mathbf{A}\mathbf{z})§ je LZ, s tými istými koeficientmi."""),
    listOf("Netriviálna kombinácia z definície LZ", "§\\mathbf{A}\\theta=\\theta§ a distributivita", "Tie isté koeficienty, stále netriviálne"),
    goals = listOf("""Definícia LZ""", """Násobenie maticou""", """Netriviálnosť"""),
    minutes = 6,
)

internal val K3Proofs = listOf(k3pJednoznacnost, k3pSucin, k3pTranspozicia, k3pHodnostSucinu, k3pRegularita, k3pJednaStrana, k3pProste, k3pObrazLz)

/* ---- theorems -------------------------------------------------------------------------------------------------- */

internal val k3tGem = Theorem(
    "la1-k3-t-gem",
    "GEM nemení obal riadkov",
    "Skriptá, Pozorování 3.5",
    wording(
        listOf(p("""Nechť §\mathbf{A},\mathbf{B}\in T^{m,n}§ a §\mathbf{A}\sim\mathbf{B}§,""")),
        listOf(p("""potom"""), b("""\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle\softbreak[1]=\langle(\mathbf{B}_{1:})^T,\dots,(\mathbf{B}_{m:})^T\rangle.""")),
        core = """\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle\softbreak[1]=\langle(\mathbf{B}_{1:})^T,\dots,(\mathbf{B}_{m:})^T\rangle""",
        rest = listOf(p("""Tedy aplikace elementárních kroků **GEM nijak nemění lineární obal souboru řádků** matice.""")),
    ),
    "Obaly riadkov sú rovnaké, lebo každý krok GEM je vratný. Nový riadok je kombináciou starých, a krok sa dá vrátiť; rovnaký obal má rovnakú dimenziu.",
    shortProof("""Rozmyslete si, že nám tvrzení stačí ukázat pouze pro případ, kdy použijeme GEM operaci jednou. Ukážeme pouze pro případ, kdy jsme použili operaci (G2) s §0\neq\alpha\in T§, ostatní případy se udělají analogicky. Mějme tedy matici §\mathbf{B}§ takovou, že řádky §\mathbf{B}§ jsou rovny řádkům §\mathbf{A}§ až na §j§-tý řádek §\mathbf{B}_{j:}§, který je §\alpha§ násobek odpovídajícího §j§-tého řádku §\mathbf{A}_{j:}§. Potom zřejmě je každý řádek matice §\mathbf{B}§ v lineárním obalu řádků matice §\mathbf{A}§: §(\mathbf{B}_{1:})^T,\dots,(\mathbf{B}_{m:})^T\in\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle§. Z Pozorování 2.34 obdržíme, že lineární obal řádků §\mathbf{B}§ je podmožinou lineárního obalu řádků §\mathbf{A}§: §\langle(\mathbf{B}_{1:})^T,\dots,(\mathbf{B}_{m:})^T\rangle\subseteq\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle§. Jelikož je §(G2)§ vratná operace opět pomocí §(G2)§, dostaneme obdobně i §\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle\subseteq\langle(\mathbf{B}_{1:})^T,\dots,(\mathbf{B}_{m:})^T\rangle§. Tedy oba lineární obaly jsou totožné.""", "Skriptá, dôkaz Pozorování 3.5"),
    note = "Stránka predmetu: „Porozumění vztahu GEM a hodnosti matice.“",
    short = "o GEM a obale riadkov",
)

internal val k3tHst = Theorem(
    "la1-k3-t-hst",
    "Hodnosť matice v HST",
    "Skriptá, Věta 3.7",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{m,n}§ je matice v horním stupňovitém tvaru s právě §k \in \mathbb{N}\cup \{0\}§ nenulovými řádky.""")),
        listOf(p("""Pak §h(\mathbf{A})=k§.""")),
    ),
    "Nenulové riadky v HST sú LN. Zo stĺpcov so schodmi postupne vyjde, že všetky koeficienty sú nulové; nulové riadky obal nemenia.",
    shortProof("""Je-li §\mathbf{A}§ nulová matice (tj. §\mathbf{A}§ neobsahuje nenulový řádek), potom řádky této matice generují podprostor §\{\theta\}§ s dimenzí §0§ a tvrzení platí. Víme, že lineární obal se odebráním nulových vektorů nezmění (1. a 3. bod z Pozorování 2.34), proto můžeme beze ztráty ignorovat nulové řádky matice §\mathbf{A}§, tedy §\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{m:})^T\rangle=\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{k:})^T\rangle§. Ukážeme-li, že soubor nenulových řádků matice §\mathbf{A}§ je LN, bude platit rovnost §h(\mathbf{A})=k§. Pojďme na to, mějme tedy nějakou lineární kombinaci nenulových řádků matice §\mathbf{A}§ rovnou nulovému vektoru: §\alpha_1(\mathbf{A}_{1:})^T+\dots+\alpha_k(\mathbf{A}_{k:})^T=\theta§, kde §\alpha_i\in T§ pro každé §i\in\hat{k}§. Mějme pomocí §1\leq j_1<j_2<\dots<j_k§ označeny indexy hlavních sloupců v horní stupňovité matici §\mathbf{A}§. Pokud se omezíme na rovnice v §j_1§-tém, §j_2§-tém až §j_k§-tém řádku, dostaneme soustavu §§\begin{aligned}\alpha_1a_{1j_1}&=0\\\alpha_1a_{1j_2}+\alpha_2a_{2j_2}&=0\\&\ \,\vdots\\\alpha_1a_{1j_k}+\dots+\alpha_ka_{kj_k}&=0.\end{aligned}§§ Tato soustava má právě jedno řešení. Jelikož v prvním řádku je jediné nenulové číslo §a_{1j_1}§, odpovídá tento řádek rovnici §\alpha_1a_{1j_1}=0§. Z nenulovosti §a_{1j_1}§ plyne §\alpha_1=0§. Pro každý další index §\ell\in\{2,\dots,k\}§ postupně provedeme stejnou úvahu. Víme-li, že §\alpha_1,\dots,\alpha_{\ell-1}=0§, potom nám zbyde rovnice §\alpha_\ell a_{\ell j_\ell}=0§. Z nenulovosti §a_{\ell j_\ell}§ plyne §\alpha_\ell=0§. Takto postupně dostaneme, že §\alpha_1,\alpha_2,\dots,\alpha_k§ jsou nutně nulové. Tedy pouze trivální kombinace je rovna nulovému vektoru neboli soubor prvních §k§ řádků matice §\mathbf{A}§ je LN. Celkově tedy máme, že opravdu platí §h(\mathbf{A})=k§.""", "Skriptá, dôkaz Věty 3.7"),
    short = "o hodnosti matice v HST",
)

internal val k3tTransp = Theorem(
    "la1-k3-t-transp",
    "Hodnosť transponovanej",
    "Skriptá, Věta 3.12",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{m,n}§.""")),
        listOf(p("""Potom"""), b("""h(\mathbf{A})=h(\mathbf{A}^{T}).""")),
    ),
    "Dimenzia obalu riadkov sa rovná dimenzii obalu stĺpcov. Preto je hodnosť najviac počet riadkov aj najviac počet stĺpcov.",
    shortProof("""Je-li §h(\mathbf{A})=0§, tak matice §\mathbf{A}§ musí být nulová. Potom i její transpozice je nulová. Proto i §h(\mathbf{A}^T)=0§. Předpokládejme tedy §k:=h(\mathbf{A})>0§. Z definice hodnosti a transpozice platí vztah §h(\mathbf{A}^T)=\dim\langle\mathbf{A}_{:1},\dots,\mathbf{A}_{:n}\rangle§. Za pomoci Věty 3.7 obdržíme, že §\mathbf{A}§ bude mít po GEM právě §k§ nenulových řádků. Jsou-li indexy §j_1,\ldots,j_k§ hlavních sloupců, snadno ukážeme, že soubor §(\mathbf{A}_{:j_1},\ldots,\mathbf{A}_{:j_k})§ sloupců původní matice odpovídajících hlavním sloupcům výše je lineárně nezávislý. Uvažujme rovnici §\alpha_{j_1}\mathbf{A}_{:j_1}+\ldots+\alpha_{j_k}\mathbf{A}_{:j_k}=\theta§, potom po maticovém zápisu této soustavy a aplikací stejných GEM operací jako dříve obdržíme soustavu, která obsahuje právě hlavní sloupce matice vpravo a nulovou pravou stranu. Proto existuje právě jedno řešení, a to §\alpha_{j_1}=\ldots=\alpha_{j_k}=0§. Právě jsme ukázali, že existuje lineárně nezávislý soubor sloupců matice §\mathbf{A}§ délky §k§. Nutně tedy pro libovolnou matici §\mathbf{A}§ platí §h(\mathbf{A})=k\leq h(\mathbf{A}^T)§. Aplikujeme již dokázaný vztah na matici §\mathbf{A}^T§ a konečně obdržíme: §h(\mathbf{A})\leq h(\mathbf{A}^T)\leq h((\mathbf{A}^T)^T)=h(\mathbf{A})§.""", "Skriptá, dôkaz Věty 3.12"),
    short = "o hodnosti transponovanej matice",
)

internal val k3tKombinacie = Theorem("la1-k3-t-kombinacie",
    "Násobenie a lineárne kombinácie",
    "Skriptá, Věta 3.15",
    wording(
        listOf(p("""Mějme matici §\mathbf{A} \in T^{m,n}§.""")),
        listOf(i("""Je-li §\mathbf{a}=\begin{pmatrix} \alpha_1 & \alpha_2 & \cdots& \alpha_m\end{pmatrix}\in T^{1,m}§, potom §\mathbf{a}\cdot\mathbf{A}§ je lineární kombinace řádků §\mathbf{A}§ s koeficienty z §\mathbf{a}§, tedy §§\mathbf{a}\cdot\mathbf{A}=\sum_{i=1}^m \alpha_i\mathbf{A}_{i:}§§."""), i("""Je-li §\mathbf{b}=\begin{pmatrix} \beta_1& \beta_2& \cdots& \beta_n\end{pmatrix}^T\in T^{n,1}§, potom §\mathbf{A} \cdot \mathbf{b}§ je lineární kombinace sloupců §\mathbf{A}§ s koeficienty z §\mathbf{b}§, tedy §§\mathbf{A}\cdot\mathbf{b}=\sum_{j=1}^n \beta_j\mathbf{A}_{:j}§§.""")),
        core = """\begin{aligned}\mathbf{a}\mathbf{A}&=\sum_{i=1}^m\alpha_i\mathbf{A}_{i:}\\\mathbf{A}\mathbf{b}&=\sum_{j=1}^n\beta_j\mathbf{A}_{:j}\end{aligned}""",
    ),
    "Riadok zľava kombinuje riadky, stĺpec sprava kombinuje stĺpce. Sústava §\\mathbf{A}\\mathbf{x}=\\mathbf{b}§ hľadá koeficienty, ktoré zo stĺpcov poskladajú §\\mathbf{b}§.",
    shortProof("""Pro každé §i \in \hat m§ dle definice maticového násobení platí: §(\mathbf{A}\mathbf{b})_{i1}=\sum_{\ell=1}^n a_{i\ell}\mathbf{b}_{\ell1}=\sum_{\ell=1}^n(\mathbf{A}_{:\ell})_{i1}\beta_\ell=(\sum_{\ell=1}^n\beta_\ell\mathbf{A}_{:\ell})_{i1}§. Tedy opravdu §\mathbf{A}\cdot\mathbf{b}§ a §\sum_{j=1}^n\beta_j\mathbf{A}_{:j}§ mají stejné prvky. Bod 1. o řádcích se ukáže naprosto analogicky.""", "Skriptá, dôkaz Věty 3.15"),
    note = "Stránka predmetu: „Rozumět tomu, jak funguje maticové násobení matice krát vektor, vektor krát matice.“",
    short = "o súčine a lineárnych kombináciách",
)

internal val k3tPoRiadkoch = Theorem("la1-k3-t-po-riadkoch",
    "Súčin po riadkoch a po stĺpcoch",
    "Skriptá, Věta 3.18",
    wording(
        listOf(p("""Mějme matici §\mathbf{A} \in T^{m,n}§ a §\mathbf{B} \in T^{n,k}§.""")),
        listOf(i("""Je-li §i\in \hat m§, potom §i§-tý řádek matice §\mathbf{A}\mathbf{B}§ získáme jako součin §i§-tého řádku první matice a celé druhé matice. Přesněji: platí §(\mathbf{A}\mathbf{B})_{i:}=\mathbf{A}_{i:}\cdot \mathbf{B}§ neboli"""), b("""\mathbf{A}\mathbf{B}=\begin{pmatrix}\mathbf{A}_{1:}\cdot\mathbf{B}\\\mathbf{A}_{2:}\cdot\mathbf{B}\\\vdots\\\mathbf{A}_{m:}\cdot\mathbf{B}\end{pmatrix}\,."""), i("""Je-li §j\in \hat k§, potom §j§-tý sloupec matice §\mathbf{A}\mathbf{B}§ získáme jako součin první matice a §j§-tého sloupce druhé matice. Přesněji: platí §(\mathbf{A}\mathbf{B})_{:j}=\mathbf{A}\cdot \mathbf{B}_{:j}§ neboli"""), b("""\mathbf{A}\mathbf{B}=\softbreak[1]\begin{pmatrix}\mathbf{A}\mathbf{B}_{:1}&\cdots&\mathbf{A}\mathbf{B}_{:k}\end{pmatrix}.""")),
        core = """\begin{aligned}(\mathbf{A}\mathbf{B})_{i:}&=\mathbf{A}_{i:}\mathbf{B}\\(\mathbf{A}\mathbf{B})_{:j}&=\mathbf{A}\mathbf{B}_{:j}\end{aligned}""",
    ),
    "Riadky súčinu sú kombinácie riadkov pravej matice s koeficientmi z ľavej. Stĺpce súčinu sú kombinácie stĺpcov ľavej matice.",
    shortProof("""Dle definice je §((\mathbf{A}\mathbf{B})_{i:})_{1j}=(\mathbf{A}\mathbf{B})_{ij}=\sum_{\ell=1}^n a_{i\ell}b_{\ell j}§. Na druhou stranu §(\mathbf{A}_{i:}\cdot\mathbf{B})_{1j}=\sum_{\ell=1}^n(\mathbf{A}_{i:})_{1\ell}b_{\ell j}§. Jelikož je §(\mathbf{A}_{i:})_{1\ell}=a_{i\ell}§, získáváme totožný výraz jako v předcházející rovnici.""", "Skriptá, dôkaz Věty 3.18"),
    short = "o riadkoch a stĺpcoch súčinu",
)

internal val k3tHodnostSucinu = Theorem("la1-k3-t-hodnost-sucinu",
    "Odhad hodnosti súčinu",
    "Skriptá, Věta 3.19",
    wording(
        listOf(p("""Je-li §\ca{\mathbf{A}}\in T^{m,n}§ a §\cb{\mathbf{B}}\in T^{n,p}§,""")),
        listOf(p("""potom"""), b("""h(\ca{\mathbf{A}}\cb{\mathbf{B}})\softbreak[1]\leq \min\left\{h(\ca{\mathbf{A}}),h(\cb{\mathbf{B}})\right\}.""")),
        core = """h(\mathbf{A}\mathbf{B})\leq\min\{h(\mathbf{A}),h(\mathbf{B})\}""",
        parts = listOf(part("Odhad cez B", """h(\mathbf{A}\mathbf{B})\leq h(\mathbf{B})""", 0), part("Odhad cez A", """h(\mathbf{A}\mathbf{B})\leq h(\mathbf{A})""", 2)),
    ),
    "Hodnosť súčinu neprevýši hodnosť žiadneho činiteľa. Riadky súčinu ležia v obale riadkov §\\cb{\\mathbf{B}}§, stĺpce v obale stĺpcov §\\ca{\\mathbf{A}}§.",
    proved(k3pHodnostSucinu),
    note = "Stránka predmetu: „Rozumět tomu, jak se mění hodnost při násobení matic.“",
    short = "o hodnosti súčinu",
)

internal val k3tJednoznacnost = Theorem("la1-k3-t-jednoznacnost",
    "Jednoznačnosť inverzie",
    "Skriptá, Věta 3.25",
    wording(
        listOf(p("""Je-li §\mathbf{A}\in T^{n,n}§ regulární,""")),
        listOf(p("""potom je inverzní matice k §\mathbf{A}§ určena jednoznačně.""")),
        core = """\text{nejvýše jedna }\mathbf{B}:\ \softbreak\mathbf{A}\mathbf{B}=\mathbf{B}\mathbf{A}=\mathbf{E}""",
    ),
    "Dve inverzné sa rovnajú: §\\ca{\\mathbf{B}_1}=\\ca{\\mathbf{B}_1}(\\mathbf{A}\\cb{\\mathbf{B}_2})=(\\ca{\\mathbf{B}_1}\\mathbf{A})\\cb{\\mathbf{B}_2}=\\cb{\\mathbf{B}_2}§. Až preto smieme písať §\\mathbf{A}^{-1}§.",
    proved(k3pJednoznacnost),
    note = "Na skúške 17. 12. 2025 za 4 b.",
    short = "o jednoznačnosti inverzie",
)

internal val k3tVlastnosti = Theorem(
    "la1-k3-t-vlastnosti",
    "Regularita inverznej matice",
    "Skriptá, Pozorování 3.26",
    wording(
        listOf(p("""Je-li §\mathbf{B}§ inverzní matice k §\mathbf{A}§,""")),
        listOf(p("""potom je §\mathbf{A}§ inverzní maticí k §\mathbf{B}§.""")),
        core = """(\mathbf{A}^{-1})^{-1}=\mathbf{A}""",
        rest = listOf(p("""Neboli: Pokud je §\mathbf{A}§ regulární matice, pak je i §\mathbf{A}^{-1}§ regulární matice a platí"""), b("""(\mathbf{A}^{-1})^{-1}=\mathbf{A}.""")),
    ),
    """Inverzná matica je regulárna a jej inverziou je pôvodná matica.""",
    shortProof("""Přímo z definice plyne následující pozorování.""", "Skriptá, odvodenie Pozorování 3.26"),
    short = "o vlastnostiach regulárnych matíc",
)

internal val k3tGemMatica = Theorem(
    "la1-k3-t-gem-matica",
    "GEM ako násobenie regulárnou maticou",
    "Skriptá, Tvrzení 3.32",
    wording(
        listOf(p("""Nechť §\mathbf{A},\mathbf{B}\in T^{m,n}§ a §\mathbf{A}\sim\mathbf{B}§.""")),
        listOf(p("""Potom existuje regulární matice §\mathbf{P}\in T^{m,m}§ taková, že §\mathbf{B}=\mathbf{P}\mathbf{A}§.""")),
    ),
    "Každý krok GEM je násobenie zľava jednoduchou regulárnou maticou (výmena, násobok, pripočítanie). Preto GEM ani násobenie regulárnou maticou hodnosť nemení.",
    shortProof("""Matice §\mathbf{B}§ vznikla z §\mathbf{A}§ konečnou posloupností elementárních kroků (G1)–(G3) GEM, které lze realizovat vynásobením matice §\mathbf{A}§ zleva popořadě regulárními maticemi §\mathbf{P}_1,\dots,\mathbf{P}_n§ z §T^{m,m}§ (každá z nich je nějakého z výše uvedených tří typů). Tedy §\mathbf{B}=\mathbf{P}_n\dots\mathbf{P}_1\mathbf{A}§. Protože součin regulárních matic je regulární matice, položíme §\mathbf{P}:=\mathbf{P}_n\dots\mathbf{P}_1§ a dostaneme tvrzení věty.""", "Skriptá, dôkaz Tvrzení 3.32"),
    note = "Na skúške 2. 2. 2026: „Formulujte korektně tvrzení o odhadu hodnosti součinu matic, pokud je druhá z nich regulární.“ (3 b), teda Důsledek 3.40.",
    short = "o GEM a regulárnej matici",
    remarks = listOf(quote("Skriptá, Poznámka 3.29", p("""Povšimněme si, že §\mathbf{P}(i,j)§ je matice vzniklá z jednotkové matice §\mathbf{E}\in T^{m,m}§ prohozením §i§-tého a §j§-tého řádku."""), i("""Matice §\mathbf{P}(i,j)\cdot\mathbf{A}§ je matice §\mathbf{A}§ s prohozeným §i§-tým a §j§-tým řádkem. (Rozmyslete si!)"""), i("""Matice §\mathbf{P}(i,j)§ je regulární a platí"""), b("""\big(\mathbf{P}(i,j)\big)^{-1}=\mathbf{P}(i,j).""")), quote("Skriptá, Poznámka 3.30", p("""Matice §\mathbf{P}_i(\alpha)§ je (diagonální) matice vzniklá z jednotkové matice §\mathbf{E}\in T^{m,m}§ nahrazením čísla §1§ na §i§-té pozici na diagonále číslem §\alpha§."""), i("""Matice §\mathbf{P}_i(\alpha)\mathbf{A}§ je matice §\mathbf{A}§ s §i§-tým řádkem vynásobeným číslem §\alpha§."""), i("""Matice §\mathbf{P}_i(\alpha)§ je regulární právě pro §\alpha\neq0§ a platí"""), b("""\big(\mathbf{P}_i(\alpha)\big)^{-1}=\mathbf{P}_i(\alpha^{-1}).""")), quote("Skriptá, Poznámka 3.31", p("""Opět §\mathbf{Q}_{i,j}(\alpha)§ je matice vzniklá z jednotkové matice §\mathbf{E}\in T^{m,m}§ přidáním čísla §\alpha\in T§ na §(j,i)§-tou pozici."""), i("""Matice §\mathbf{Q}_{i,j}(\alpha)\mathbf{A}§ je matice §\mathbf{A}§ po přičtení §\alpha§násobku §i§-tého řádku k §j§-tému. Tedy"""), b("""\big(\mathbf{Q}_{i,j}(\alpha)\mathbf{A}\big)_{j:}=\mathbf{A}_{j:}+\alpha\mathbf{A}_{i:}."""), i("""Matice §\mathbf{Q}_{i,j}(\alpha)§ je regulární a platí"""), b("""\big(\mathbf{Q}_{i,j}(\alpha)\big)^{-1}=\mathbf{Q}_{i,j}(-\alpha)."""))),
)

internal val k3tRegularita = Theorem("la1-k3-t-regularita",
    "Regularita a hodnosť",
    "Skriptá, Věta 3.35",
    wording(
        listOf(p("""Buď §\mathbf{A}\in T^{n,n}§.""")),
        listOf(p("""Následující tvrzení jsou ekvivalentní."""), i("""§\mathbf{A}§ je regulární."""), i("""§h(\mathbf{A})=n§."""), i("""§\mathbf{A}\sim\mathbf{E}§.""")),
        core = """\mathbf{A}\text{ je regulární}\softbreak[1]\Leftrightarrow h(\mathbf{A})=n\softbreak[1]\Leftrightarrow\mathbf{A}\sim\mathbf{E}""",
        parts = listOf(part("1 ⇒ 2", """\mathbf{A}\text{ je regulárna}\Rightarrow h(\mathbf{A})=n""", 0), part("2 ⇒ 3", """h(\mathbf{A})=n\Rightarrow\mathbf{A}\sim\mathbf{E}""", 1), part("3 ⇒ 1", """\mathbf{A}\sim\mathbf{E}\Rightarrow\mathbf{A}\text{ je regulárna}""", 3)),
    ),
    "Štvorcová matica je regulárna práve vtedy, keď má plnú hodnosť. Vtedy GEM v HST nevyrobí nulový riadok a dôjde až k §\\mathbf{E}§; na tom stojí výpočet inverzie.",
    proved(k3pRegularita),
    note = "Stránka predmetu: „Umět dokázat větu o ekvivalenci regularity, plné hodnosti a zgemování na jednotkovou matici.“",
    short = "o regularite a hodnosti",
    remarks = listOf(quote("Skriptá, Poznámka 3.36", p("""U matice §\mathbf{A}\in T^{n,n}§ je vztah §h(\mathbf{A})=n§ ekvivalentní tomu, že §T^n=\langle(\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{n:})^T\rangle§. Díky Větě 2.55 víme, že u souborů vektorů z §T^n§ délky §n§ stačí ověřit jednu vlastnost báze a druhou dostaneme zdarma. Proto platí"""), p("""§\mathbf{A}§ je regulární §\Leftrightarrow§ soubor §\left((\mathbf{A}_{1:})^T,\dots,(\mathbf{A}_{n:})^T\right)§ řádků matice §\mathbf{A}§ je LN."""), p("""Navíc díky Větě 3.12 víme, že §h(\mathbf{A})=h(\mathbf{A}^T)§, tedy platí také ekvivalence:"""), p("""§\mathbf{A}§ je regulární §\Leftrightarrow§ soubor §\left((\mathbf{A}_{:1}),\dots,(\mathbf{A}_{:n})\right)§ sloupců matice §\mathbf{A}§ je LN."""))),
)

internal val k3tPostup = Theorem("la1-k3-t-postup",
    "Výpočet inverzie z §(\\mathbf{A}\\mid\\mathbf{E})§",
    "Skriptá, Postup 3.37",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{n,n}§.""")),
        listOf(p("""Ověřte, zda je matice regulární a pokud je, nalezněte k ní matici inverzní §\mathbf{A}^{-1}§."""), i("""Hledáme matici §\mathbf{A}^{-1}§ s vlastností §\mathbf{A}^{-1}\mathbf{A}=\mathbf{A}\mathbf{A}^{-1}=\mathbf{E}§."""), i("""Doplněním zadané matice o jednotkovou matici stejného rozměru sestavme dvoublokovou rozšířenou matici §(\mathbf{A}\mid\mathbf{E})\in T^{n,2n}§."""), i("""Na celou §(\mathbf{A}\mid\mathbf{E})§ používáme řádkové úpravy GEM, pro libovolnou posloupnost řádkových úprav realizovaných regulární maticí §\mathbf{P}§ pak platí §(\mathbf{A}\mid\mathbf{E})\sim(\mathbf{P}\mathbf{A}\mid\mathbf{P}\mathbf{E})=(\mathbf{P}\mathbf{A}\mid\mathbf{P})\,§. Díky Větě 3.35 platí, že levý blok §\mathbf{A}§ je možné převést na jednotkovou matici právě tehdy, když je §\mathbf{A}§ regulární. Vznikne-li při úpravách §\mathbf{A}§ na horní stupňovitý tvar nulový řádek, pak §\mathbf{A}§ je singulární a inverze neexistuje."""), i("""Je-li §\mathbf{A}§ regulární, pak pro úpravy §\mathbf{P}§ vedoucí k převedení levého bloku matice §(\mathbf{A}\mid\mathbf{E})§ na jednotkovou matici platí §\mathbf{P}=\mathbf{A}^{-1}§, tedy §§(\mathbf{A}\mid\mathbf{E})\sim(\mathbf{E}\mid\mathbf{A}^{-1})§§ a pravý blok výsledné matice obsahuje hledanou §\mathbf{A}^{-1}§.""")),
        core = """(\mathbf{A}\mid\mathbf{E})\sim(\mathbf{E}\mid\mathbf{A}^{-1})""",
    ),
    "Tie isté kroky robíš naraz na §\\mathbf{A}§ aj na §\\mathbf{E}§. Keď vľavo vznikne §\\mathbf{E}§, vpravo je §\\mathbf{P}=\\mathbf{A}^{-1}§.",
    shortProof("""Je-li §\mathbf{A}§ regulární, můžeme dostat §\mathbf{P}\mathbf{A}=\mathbf{E}§ a z definice inverzní matice pak plyne §\mathbf{A}^{-1}=\mathbf{P}§.""", "Skriptá, zdôvodnenie Postupu 3.37"),
    note = "Stránka predmetu: „Výpočet hodnosti a nalezení inverzní matice.“",
    short = "výpočtu inverznej matice",
)

internal val k3tJednaStrana = Theorem("la1-k3-t-jedna-strana",
    "Inverzia z jednej strany stačí",
    "Skriptá, Věta 3.42",
    wording(
        listOf(p("""Buď §\mathbf{A}\in T^{n,n}§. Existuje-li §\mathbf{B}\in T^{n,n}§ taková, že platí §\mathbf{A}\mathbf{B}=\mathbf{E}§ ==nebo== §\mathbf{B}\mathbf{A}=\mathbf{E}§,""")),
        listOf(p("""potom je §\mathbf{A}§ regulární a §\mathbf{B}=\mathbf{A}^{-1}§.""")),
        core = """\mathbf{A}\text{ je regulárna},\ \softbreak\mathbf{B}=\mathbf{A}^{-1}""",
        parts = listOf(part("Regularita", """\mathbf{A}\text{ je regulárna}""", 0), part("Inverzia", """\mathbf{B}=\mathbf{A}^{-1}""", 1)),
    ),
    "Pri overovaní inverzie stačí jeden súčin. Pre štvorcové matice; dôkaz stojí na hodnosti.",
    proved(k3pJednaStrana),
    short = "o inverzii z jednej strany",
)

internal val k3tGemHodnost = Theorem("la1-k3-t-gem-hodnost",
    "GEM nemení hodnosť",
    "Skriptá, Věta 3.6",
    wording(
        listOf(p("""Buďte §\mathbf{A},\mathbf{B}\in T^{m,n}§. Je-li §\mathbf{A}\sim\mathbf{B}§,""")),
        listOf(p("""potom"""), b("""h(\mathbf{A})=h(\mathbf{B}).""")),
    ),
    """GEM zachováva obal riadkov, a preto aj jeho dimenziu, čiže hodnosť.""",
    shortProof("""Protože operace GEM nemění lineární obal, nemůže změnit ani jeho dimenzi.""", "Skriptá, odvodenie Věty 3.6"),
    short = "o GEM a hodnosti",
)

internal val k3tOdhadRozmermi = Theorem("la1-k3-t-odhad-rozmermi",
    "Odhad hodnosti rozmermi",
    "Skriptá, Důsledek 3.13",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{m,n}§.""")),
        listOf(p("""Potom §h(\mathbf{A})\leq\min\{m,n\}§.""")),
    ),
    """Hodnosť neprevýši počet riadkov ani počet stĺpcov.""",
    shortProof("""Právě dokázaná věta vlastně říká, že jsme klidně mohli definovat hodnost jinak (ale ekvivalentně!), a to jako dimenzi lineárního obalu souboru **sloupců** matice §\mathbf{A}§. Z toho také plyne následující tvrzení, které je triviální aplikací Tvrzení 2.54.""", "Skriptá, odvodenie Důsledku 3.13"),
    short = "o odhade hodnosti rozmermi",
)

internal val k3tSlrKombinacie = Theorem("la1-k3-t-slr-kombinacie",
    "Sústava ako kombinácia stĺpcov",
    "Skriptá, Pozorování 3.16",
    wording(
        listOf(),
        listOf(p("""Vztah §\mathbf{A}\mathbf{x}=\mathbf{b}§ lze s pomocí Věty 3.15 přepsat jako"""), b("""\sum_{j=1}^{n}x_j\mathbf{A}_{:j}=\mathbf{b}\,,"""), p("""tedy platí, že hledání řešení soustavy lineárních rovnic není nic jiného než hledání koeficientů lineární kombinace **sloupců** matice §\mathbf{A}§, která je rovna právě vektoru pravé strany §\mathbf{b}§!""")),
        core = """\mathbf{A}\mathbf{x}=\mathbf{b}\ \Leftrightarrow\ \sum_{j=1}^{n}x_j\mathbf{A}_{:j}=\mathbf{b}""",
    ),
    """Riešenie sústavy dáva koeficienty kombinácie stĺpcov, ktorá vytvorí pravú stranu.""",
    shortProof("""Vztah §\mathbf{A}\mathbf{x}=\mathbf{b}§ lze s pomocí Věty 3.15 přepsat jako §\sum_{j=1}^{n}x_j\mathbf{A}_{:j}=\mathbf{b}§.""", "Skriptá, Pozorování 3.16"),
    short = "o sústave a kombinácii stĺpcov",
)

internal val k3tNeutralna = Theorem("la1-k3-t-jednotkova",
    "Násobenie jednotkovou maticou",
    "Skriptá, Pozorování 3.22",
    wording(
        listOf(p("""Pokud §\mathbf{A}\in T^{m,n}§, §\mathbf{E}_n\in T^{n,n}§ a §\mathbf{E}_m\in T^{m,m}§,""")),
        listOf(p("""pak"""), b("""\mathbf{A}\mathbf{E}_n=\mathbf{A}=\mathbf{E}_m\mathbf{A}.""")),
        core = """\mathbf{A}\mathbf{E}_n=\mathbf{A}=\mathbf{E}_m\mathbf{A}""",
        rest = listOf(p("""Speciálně pro §\mathbf{A}\in T^{n,n}§ máme §\mathbf{A}\mathbf{E}_n=\mathbf{A}=\mathbf{E}_n\mathbf{A}§.""")),
    ),
    """Jednotková matica pri vhodných rozmeroch nemení maticu pri násobení zľava ani sprava.""",
    shortProof("""Ukažme si třeba §\mathbf{E}_m\mathbf{A}=\mathbf{A}§. Podle Věty 3.18 je §i§-tý řádek součinu §\mathbf{E}_m\mathbf{A}§ roven §\mathbf{E}_{i:}\cdot\mathbf{A}§ a pro každé §i\in\hat m§ platí §\mathbf{E}_{i:}\cdot\mathbf{A}=\begin{pmatrix}0&\cdots&0&1&0&\cdots&0\end{pmatrix}\cdot\mathbf{A}=\mathbf{A}_{i:}§. Řádky §\mathbf{E}_m\mathbf{A}§ jsou tedy řádky §\mathbf{A}§, tj. §\mathbf{E}_m\mathbf{A}=\mathbf{A}§. Rovnost §\mathbf{A}\mathbf{E}_n=\mathbf{A}§ se ukáže obdobně po sloupcích.""", "Skriptá, dôkaz Pozorování 3.22"),
    short = "o násobení jednotkovou maticou",
)

internal val k3tInvSucin = Theorem(
    "la1-k3-t-inv-sucin",
    "Inverzia súčinu",
    "Skriptá, Věta 3.27",
    wording(
        listOf(p("""Nechť §\ca{\mathbf{A}},\cb{\mathbf{B}}\in T^{n,n}§ jsou regulární,""")),
        listOf(p("""potom §\ca{\mathbf{A}}\cb{\mathbf{B}}§ je také regulární a platí"""), b("""(\ca{\mathbf{A}}\cb{\mathbf{B}})^{-1}=\cb{\mathbf{B}}^{-1}\ca{\mathbf{A}}^{-1}.""")),
        core = """\mathbf{A}\mathbf{B}\text{ je regulárna},\ \softbreak(\mathbf{A}\mathbf{B})^{-1}=\mathbf{B}^{-1}\mathbf{A}^{-1}""",
        parts = listOf(part("Prvé poradie", """(\mathbf{A}\mathbf{B})(\mathbf{B}^{-1}\mathbf{A}^{-1})=\mathbf{E}""", 0), part("Druhé poradie", """(\mathbf{B}^{-1}\mathbf{A}^{-1})(\mathbf{A}\mathbf{B})=\mathbf{E}""", 1)),
    ),
    """Poradie činiteľov sa pri inverzii súčinu obráti. Obe poradia násobenia s kandidátom dajú jednotkovú maticu.""",
    proved(k3pSucin),
    note = "Stránka predmetu: „Umět dokázat základní vlastnosti regulárních matic: inverze k součinu matic, inverze k transponované matici.“",
    short = "o inverzii súčinu",
)

internal val k3tInvTransp = Theorem("la1-k3-t-inv-transp",
    "Inverzia transponovanej matice",
    "Skriptá, Věta 3.28",
    wording(
        listOf(p("""Nechť §\mathbf{A}\in T^{n,n}§ je regulární,""")),
        listOf(p("""potom §\mathbf{A}^T§ je také regulární a platí"""), b("""(\mathbf{A}^T)^{-1}=(\mathbf{A}^{-1})^T.""")),
        core = """\mathbf{A}^T\text{ je regulárna},\ \softbreak(\mathbf{A}^T)^{-1}=(\mathbf{A}^{-1})^T""",
        parts = listOf(part("Prvé poradie", """\mathbf{A}^T(\mathbf{A}^{-1})^T=\mathbf{E}""", 0), part("Druhé poradie", """(\mathbf{A}^{-1})^T\mathbf{A}^T=\mathbf{E}""", 1)),
    ),
    """Transpozíciu a inverziu možno vykonať v ľubovoľnom poradí.""",
    proved(k3pTranspozicia),
    short = "o inverzii transponovanej matice",
)

internal val k3tRegGem = Theorem("la1-k3-t-regularna-gem",
    "Násobenie regulárnou maticou je GEM",
    "Skriptá, Věta 3.39",
    wording(
        listOf(p("""Nechť §\mathbf{A},\mathbf{B}\in T^{m,n}§. Existuje-li §\mathbf{P}\in T^{m,m}§ regulární taková, že §\mathbf{B}=\mathbf{P}\mathbf{A}§,""")),
        listOf(p("""potom §\mathbf{A}\sim\mathbf{B}§.""")),
    ),
    """Násobenie regulárnou maticou zľava sa dá vykonať riadkovými úpravami GEM.""",
    shortProof("""Matice §\mathbf{P}§ je regulární, proto §\mathbf{P}\sim\mathbf{E}§. Nechť tuto eliminaci realizuje regulární matice §\mathbf{Q}§, tedy §\mathbf{Q}\mathbf{P}=\mathbf{E}§. Aplikujeme-li stejné kroky GEM jako při eliminaci §\mathbf{P}\sim\mathbf{E}§ na matici §\mathbf{B}§, dostaneme matici §\mathbf{A}§, protože §\mathbf{Q}\mathbf{B}=\mathbf{Q}\mathbf{P}\mathbf{A}=\mathbf{E}\mathbf{A}=\mathbf{A}§. Tedy §\mathbf{B}\sim\mathbf{A}§ a z vratnosti všech elementárních kroků GEM platí i §\mathbf{A}\sim\mathbf{B}§.""", "Skriptá, dôkaz Věty 3.39"),
    short = "o regulárnom násobení a GEM",
)

internal val k3tRegHodnost = Theorem("la1-k3-t-regularna-hodnost",
    "Regulárne násobenie nemení hodnosť",
    "Skriptá, Důsledek 3.40",
    wording(
        listOf(),
        listOf(p("""Násobením regulární maticí se hodnost nezmění."""), p("""Přesněji: máme-li libovolnou matici §\mathbf{A}§ z §T^{m,n}§ a regulární matici §\mathbf{P}\in T^{m,m}§, potom platí"""), b("""h(\mathbf{A})=h(\mathbf{P}\mathbf{A})."""), p("""A je-li §\mathbf{Q}\in T^{n,n}§ regulární, pak"""), b("""h(\mathbf{A})=h(\mathbf{A}\mathbf{Q}).""")),
        core = """\mathbf{P},\mathbf{Q}\text{ regulární}\softbreak[1]\Rightarrow h(\mathbf{P}\mathbf{A})=h(\mathbf{A})\softbreak[2]=h(\mathbf{A}\mathbf{Q})""",
    ),
    """Regulárny činiteľ zachová hodnosť pri násobení zľava aj sprava.""",
    shortProof("""První část plyne jednoduše z toho, že víme §\mathbf{A}\sim\mathbf{P}\mathbf{A}§ a také, že GEM nemění hodnost matice. Pro druhou část postupně využijeme toho, že transpozice nemění hodnost, poté jak vypadá transpozice součinu matic, dále regularitu matice §\mathbf{Q}^T§ a vztah §h(\mathbf{A})=h(\mathbf{P}\mathbf{A})§: §h(\mathbf{A}\mathbf{Q})=h((\mathbf{A}\mathbf{Q})^T)=h(\mathbf{Q}^T\mathbf{A}^T)=h(\mathbf{A}^T)=h(\mathbf{A})§.""", "Skriptá, dôkaz Důsledku 3.40"),
    short = "o hodnosti pri regulárnom násobení",
)

internal val K3Theorems = listOf(k3tGem, k3tGemHodnost, k3tHst, k3tTransp, k3tOdhadRozmermi, k3tKombinacie, k3tSlrKombinacie, k3tPoRiadkoch, k3tHodnostSucinu, k3tNeutralna, k3tJednoznacnost, k3tVlastnosti, k3tInvSucin, k3tInvTransp, k3tGemMatica, k3tRegularita, k3tPostup, k3tRegGem, k3tRegHodnost, k3tJednaStrana)

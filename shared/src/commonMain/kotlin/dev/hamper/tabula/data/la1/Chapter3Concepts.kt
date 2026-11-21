package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * The definitions of kapitola 3 "Hodnost a regularita matice" (chap-04.woo), Czech word for word, numbered as in the
 * rendered textbook (one counter per chapter: Definice 3.1, Poznámka 3.2, Příklad 3.3, ...). \mA of the source is
 * written \mathbf{A}.
 */

internal val k3Hodnost = Concept(
    id = "la1-k3-hodnost", name = "Hodnosť matice", src = "Skriptá, Definice 3.1",
    czech = listOf(
        p("""Nechť §\mathbf{A}\in T^{m,n}§. **Hodností matice** §\mathbf{A}§ nazýváme **dimenzi lineárního obalu souboru řádků matice** §\mathbf{A}§ (kde tyto řádky z §T^{1,n}§ chápeme jako vektory z §T^{n}§) a značíme ji §h(\mathbf{A})§. Neboli hodnost matice §\mathbf{A}§ je rovna"""),
        b("""h(\mathbf{A}) := \dim \big\langle (\mathbf{A}_{1:})^T,\dots,\softbreak[2](\mathbf{A}_{m:})^T \big\rangle."""),
    ),
    why = listOf(
        w("""§\mathbf{A}\in T^{m,n}§""", """Hodnosť má každá matica, aj obdĺžniková. Regularita je naopak len pre štvorcové."""),
        w("""dimenzi lineárního obalu souboru řádků""", """Hodnosť je číslo: koľko smerov naozaj dávajú riadky spolu. Dva rovnaké nenulové riadky dajú jeden smer."""),
        w("""řádky z §T^{1,n}§ chápeme jako vektory z §T^{n}§""", """Riadok je matica typu §1\times n§, nie vektor. Lineárny obal a dimenziu poznáme len pre vektory z §T^n§, preto transpozícia (Poznámka 3.2)."""),
        w("""značíme ji §h(\mathbf{A})§""", """§h(\mathbf{A})§ je zápis hodnosti, nie spôsob jej výpočtu. Výpočet (GEM, počet nenulových riadkov) je veta, nie táto definícia."""),
    ),
    judge = listOf(
        j("""Nad §\mathbb{R}§: §h\begin{pmatrix}1&3&1\\2&1&-1\end{pmatrix}=2§""", true, """Riadky §(1,3,1)§ a §(2,1,-1)§ sú LN, obal má dimenziu §2§ (Příklad 3.3).""", """Hodnosť sedí: ani jeden riadok nie je násobkom druhého. Dva LN vektory generujú podpriestor dimenzie §2§."""),
        j("""Nad §\mathbb{R}§: §h\begin{pmatrix}1&2\\2&4\end{pmatrix}=2§""", false, """Druhý riadok je dvojnásobok prvého, obal je priamka. Hodnosť je §1§.""", """Dva nenulové riadky ešte neznamenajú hodnosť §2§. Riadok §(2,4)§ leží v §\langle(1,2)\rangle§, obal má dimenziu §1§."""),
        j("""§h(\Theta)=0§ pre nulovú maticu""", true, """Obal nulových riadkov je §\{\theta\}§ a ten má dimenziu §0§.""", """Nulová matica má hodnosť §0§. Jej riadky generujú len §\{\theta\}§, a §\dim\{\theta\}=0§ (Veta 2.53)."""),
        j("""Pre §\mathbf{A}\in T^{3,5}§ môže byť §h(\mathbf{A})=4§""", false, """Matica má len tri riadky: obal troch vektorov má dimenziu najviac §3§.""", """Štyri nejde: obal troch riadkov má dimenziu najviac §3§ (Tvrzení 2.54). Navyše §h(\mathbf{A})\leq\min\{3,5\}§ (Důsledek 3.13)."""),
        j("""Hodnosť matice §\mathbf{A}\in T^{m,n}§ je počet jej nenulových riadkov""", false, """Platí to len v hornom stupňovitom tvare (Veta 3.7), nie pre každú maticu.""", """To je veta o matici v HST, nie definícia. Dva rovnaké nenulové riadky počíta dvakrát, hoci ich obal má dimenziu §1§."""),
    ),
    units = listOf("Matica §\\mathbf{A}\\in T^{m,n}§", "Riadky chápané ako vektory z §T^n§ (transpozícia)", "Lineárny obal riadkov", "Dimenzia tohto obalu"),
    extra = listOf("Značenie §h(\\mathbf{A})§"),
    trap = Trap("Hodnost je počet nenulových řádků po tom, co udělám GEM.", "To je spôsob výpočtu, nie definícia. Skriptá výslovne varujú, že taká odpoveď sa neuznáva; nesprávne je aj „maximální počet LN řádků“, lebo LN je vlastnosť súboru, nie riadku.", "Varování 3.11"),
    ask = "Definujte hodnost matice.",
    question = "Platí to o hodnosti?",
    short = "hodnosti matice",
)

internal val k3Sim = Concept(
    id = "la1-k3-sim", name = "Prevoditeľnosť GEM-om §\\mathbf{A}\\sim\\mathbf{B}§", src = "Skriptá, Definice 3.4",
    czech = listOf(
        p("""Nechť §\mathbf{A}, \mathbf{B}\in T^{m,n}§. Je-li možné matici §\mathbf{A}§ převést konečně mnoha řádkovými úpravami (G1)–(G3) Gaussovy eliminační metody na matici §\mathbf{B}§, budeme tuto skutečnost zkráceně zapisovat"""),
        b("""\mathbf{A}\sim\mathbf{B}\,."""),
    ),
    why = listOf(
        w("""§\mathbf{A}, \mathbf{B}\in T^{m,n}§""", """Obe matice majú rovnaké rozmery. Vyškrtnutie nulového riadku nie je krok GEM, takže pri ňom §\sim§ nepíšeš."""),
        w("""konečně mnoha řádkovými úpravami""", """GEM používa iba riadkové úpravy. Sú to výmena riadkov (G1), násobenie riadku nenulovým číslom (G2), pripočítanie násobku iného riadku (G3)."""),
        w("""(G2): nenulovým číslom""", """Násobiť nulou nesmieš: tú úpravu by si nevedel vrátiť. Každý krok GEM je vratný, preto z §\mathbf{A}\sim\mathbf{B}§ plynie §\mathbf{B}\sim\mathbf{A}§."""),
    ),
    judge = listOf(
        j("""Nad §\mathbb{R}§: §\begin{pmatrix}1&2\\2&4\end{pmatrix}\sim\begin{pmatrix}1&2\\0&0\end{pmatrix}§""", true, """Jeden krok (G3): od druhého riadku odpočítaš dvojnásobok prvého.""", """Je to dovolený krok (G3): druhý riadok mínus dvojnásobok prvého. Nulový riadok, ktorý vznikne, v matici ostáva."""),
        j("""Nad §\mathbb{R}§: §\begin{pmatrix}1&2\\2&4\end{pmatrix}\sim\begin{pmatrix}1&2\end{pmatrix}§""", false, """Matice majú rôzne rozmery. Vyškrtnúť nulový riadok nie je krok GEM.""", """Vzťah §\sim§ je len pre matice rovnakého typu. Vyškrtnutie nulového riadku pri riešení sústavy nevadí, ale krokom GEM nie je."""),
        j("""Nad §\mathbb{R}§: §\begin{pmatrix}1&2\\3&4\end{pmatrix}\sim\begin{pmatrix}1&2\\0&0\end{pmatrix}§""", false, """Druhý riadok by si musel vynásobiť nulou, a to (G2) nedovoľuje.""", """Žiadna povolená úprava nulový riadok nevyrobí. Riadky §(1,2)§ a §(3,4)§ sú LN a GEM obal riadkov nemení (Pozorování 3.5)."""),
        j("""Z §\mathbf{A}\sim\mathbf{B}§ plynie §\mathbf{B}\sim\mathbf{A}§""", true, """Každý krok GEM sa dá vrátiť krokom toho istého typu.""", """Platí to: výmenu vráti tá istá výmena, násobenie §\alpha§ násobenie §\alpha^{-1}§, pripočítanie odpočítanie."""),
    ),
    units = listOf("Matice rovnakého typu §T^{m,n}§", "Konečne veľa riadkových úprav (G1) až (G3)", "Zápis §\\mathbf{A}\\sim\\mathbf{B}§"),
    ask = "Co znamená zápis §\\mathbf{A}\\sim\\mathbf{B}§?",
    question = "Platí to o úpravách GEM?",
    short = "prevoditeľnosti matíc GEM-om",
)

internal val k3Jednotkova = Concept(
    id = "la1-k3-jednotkova", name = "Jednotková a diagonálna matica", src = "Skriptá, Definice 3.20",
    czech = listOf(
        p("""**Jednotkovou maticí** §n§-tého řádu rozumíme čtvercovou matici §\mathbf{E}\in T^{n,n}§ splňující"""),
        b("""e_{ij}=\left\lbrace\begin{matrix}1 & \text{ pro } i=j,\\0 & \text{ jinak.}\end{matrix}\right."""),
        p("""Pokud chceme zdůraznit rozměry této matice, píšeme §\mathbf{E}_n§ namísto §\mathbf{E}§."""),
        p("""**Diagonální maticí** §n§-tého řádu nazveme libovolnou čtvercovou matici §\mathbf{A}\in T^{n,n}§ splňující"""),
        b("""i\neq j\Rightarrow a_{ij}=0\,."""),
        p("""**Diagonálou** čtvercové matice §\mathbf{A}\in T^{n,n}§ nazveme vektor §(a_{11},a_{22},\dots,a_{nn}) \in T^n§."""),
    ),
    why = listOf(
        w("""čtvercovou matici""", """Diagonála a jednotková matica majú zmysel len pre štvorcové matice."""),
        w("""§e_{ij}=1§ pro §i=j§, jinak §0§""", """Jednotková matica má na diagonále jednotky a mimo nej nuly. §\mathbf{E}§ je neutrálna pri násobení: §\mathbf{A}\mathbf{E}=\mathbf{A}=\mathbf{E}\mathbf{A}§ (Pozorování 3.22)."""),
        w("""§i\neq j\Rightarrow a_{ij}=0§""", """Diagonálna matica smie mať na diagonále čokoľvek, aj nuly. Podmienka hovorí len o prvkoch mimo diagonály."""),
    ),
    judge = listOf(
        j("""Matica §\begin{pmatrix}2&0\\0&-3\end{pmatrix}\in T^{2,2}§ je diagonálna""", true, """Mimo diagonály sú nuly, na diagonále smie byť čokoľvek.""", """Je diagonálna: podmienka §i\neq j\Rightarrow a_{ij}=0§ platí. Jednotky na diagonále treba len pri jednotkovej matici."""),
        j("""Nulová matica §\Theta\in T^{2,2}§ je diagonálna""", true, """Mimo diagonály má nuly, a viac definícia nechce.""", """Je: implikácia §i\neq j\Rightarrow a_{ij}=0§ platí. Nuly na diagonále definícia nezakazuje."""),
        j("""Matica §\begin{pmatrix}0&1\\1&0\end{pmatrix}\in T^{2,2}§ je jednotková""", false, """Jednotky má mimo diagonály, nie na nej.""", """Jednotková matica má §e_{11}=e_{22}=1§ a §e_{12}=e_{21}=0§. Táto matica vymení riadky (§\mathbf{P}(1,2)§), jednotková nie je."""),
        j("""Matica §\begin{pmatrix}1&0&0\\0&1&0\end{pmatrix}\in T^{2,3}§ je jednotková""", false, """Nie je štvorcová, a jednotková matica je štvorcová.""", """Definícia hovorí o štvorcovej matici §\mathbf{E}\in T^{n,n}§. Matica typu §2\times 3§ jednotková byť nemôže."""),
    ),
    units = listOf("Štvorcová matica §T^{n,n}§", "§\\mathbf{E}§: jednotky na diagonále, inde nuly", "Diagonálna: mimo diagonály nuly"),
    extra = listOf("Diagonála je vektor §(a_{11},\\dots,a_{nn})§"),
    ask = "Napište definici jednotkové a diagonální matice.",
    question = "Je tá matica pomenovaná správne?",
    short = "jednotkovej a diagonálnej matice",
)

internal val k3Inverzna = Concept(
    id = "la1-k3-inverzna", name = "Inverzná a regulárna matica", src = "Skriptá, Definice 3.23",
    czech = listOf(
        p("""Buď §\mathbf{A}\in T^{n,n}§. Matici §\mathbf{B}\in T^{n,n}§ nazveme **inverzní maticí** k matici §\mathbf{A}§, pokud platí"""),
        b("""\mathbf{A}\mathbf{B} = \mathbf{B}\mathbf{A} = \mathbf{E}."""),
        p("""Inverzní matice obvykle značíme §\mathbf{B}=\mathbf{A}^{-1}§."""),
        p("""Matici §\mathbf{A}§ nazveme **regulární**, pokud ==existuje== matice, která je k ní inverzní."""),
        p("""Pokud §\mathbf{A}§ není regulární, nazýváme matici §\mathbf{A}§ **singulární**."""),
    ),
    why = listOf(
        w("""§\mathbf{A},\mathbf{B}\in T^{n,n}§""", """Inverzia je definovaná len pre štvorcové matice. Pri neštvorcovej regularita ani inverzia definované nie sú."""),
        w("""§\mathbf{A}\mathbf{B} = \mathbf{B}\mathbf{A} = \mathbf{E}§""", """Obe poradia, lebo násobenie matíc nie je komutatívne. Že stačí jedno, je až veta (Věta 3.42)."""),
        w("""regulární, pokud existuje matice""", """Regularita je vlastnosť samotnej §\mathbf{A}§: existuje k nej inverzná. Bez „existuje“ to nie je definícia (Varování 3.24)."""),
        w("""singulární""", """Singulárna matica nemá inverznú maticu. Napríklad nulová matica: §\Theta\mathbf{B}=\Theta\neq\mathbf{E}§ pre každé §\mathbf{B}§."""),
    ),
    judge = listOf(
        j("""Nad §\mathbb{R}§: §\begin{pmatrix}0&-1\\1&1\end{pmatrix}§ je inverzná k §\begin{pmatrix}1&1\\-1&0\end{pmatrix}§""", true, """Súčin v oboch poradiach je §\mathbf{E}§ (Otázka za Definicí 3.23).""", """Je inverzná: vynásob v oboch poradiach a vyjde §\mathbf{E}§. Overiť treba §\mathbf{A}\mathbf{C}§ aj §\mathbf{C}\mathbf{A}§."""),
        j("""Nad §\mathbb{R}§: §\begin{pmatrix}1&1\\2&0\end{pmatrix}§ je inverzná k §\begin{pmatrix}1&1\\-1&0\end{pmatrix}§""", false, """Súčin je §\begin{pmatrix}3&1\\-1&-1\end{pmatrix}§, nie §\mathbf{E}§.""", """Nie je: §\mathbf{A}\mathbf{B}=\begin{pmatrix}3&1\\-1&-1\end{pmatrix}\neq\mathbf{E}§. Jedna nesplnená rovnosť stačí."""),
        j("""Nulová matica §\Theta\in\mathbb{R}^{2,2}§ je regulárna""", false, """§\Theta\mathbf{B}=\Theta\neq\mathbf{E}§ pre každé §\mathbf{B}§, inverzia neexistuje.""", """Je singulárna: akýkoľvek súčin s nulovou maticou je nulová matica, nikdy §\mathbf{E}§."""),
        j("""§\mathbf{E}§ je regulárna""", true, """§\mathbf{E}\mathbf{E}=\mathbf{E}§, takže §\mathbf{E}^{-1}=\mathbf{E}§.""", """Je regulárna: sama sebe je inverziou, §\mathbf{E}\mathbf{E}=\mathbf{E}§ v oboch poradiach."""),
        j("""Matica typu §2\times 3§ je singulárna""", false, """Regularita ani singularita pre neštvorcové matice definované nie sú.""", """Nie je singulárna ani regulárna: Definice 3.23 hovorí len o maticiach z §T^{n,n}§."""),
    ),
    units = listOf("Štvorcové §\\mathbf{A},\\mathbf{B}\\in T^{n,n}§ rovnakého rádu", "Inverzná: §\\mathbf{A}\\mathbf{B}=\\mathbf{B}\\mathbf{A}=\\mathbf{E}§, obe poradia", "Regulárna: inverzná existuje"),
    extra = listOf("Značenie §\\mathbf{A}^{-1}§", "Singulárna: nie je regulárna"),
    trap = Trap("Mějme matice §\\mathbf{A},\\mathbf{B}\\in T^{n,n}§ takové, že §\\mathbf{A}\\mathbf{B}=\\mathbf{B}\\mathbf{A}=\\mathbf{E}§, potom nazýváme matici §\\mathbf{A}§ regulární a §\\mathbf{B}§ inverzní maticí k matici §\\mathbf{A}§.", "Toto je „nedefinícia“ zo skrípt: nehovorí, čo je regulárna §\\mathbf{A}§ sama o sebe. Chýba „existuje matica §\\mathbf{B}§“.", "Varování 3.24"),
    ask = "Definujte inverzní matici a regulární matici.",
    question = "Je tá matica pomenovaná správne?",
    short = "inverznej a regulárnej matice",
)

internal val K3Concepts = listOf(k3Hodnost, k3Sim, k3Jednotkova, k3Inverzna)

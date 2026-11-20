package dev.hamper.tabula.data.la1

import dev.hamper.tabula.data.Symbol

/**
 * Tappable notation of LA1 (see [Symbol]). Order matters where two could overlap: the coordinates (\mathcal{B} or
 * \mathcal{E}_3 as a subscript) come before the standard basis \mathcal{E}_n.
 */
val La1Glossary: List<Symbol> = listOf(
    Symbol("ssub", """\ssub""", """\\ssub""", "je podpriestor",
        """§P\ssub T^n§: §P§ je neprázdna podmnožina uzavretá na sčítanie aj násobenie skalárom; obyčajné §\subseteq§ hovorí len o podmnožine.""", "podpriestor"),
    Symbol("tn", """T^n""", """(?<![A-Za-z\\])T\^(?:n|\{n\})""", "vektorový priestor",
        """Všetky §n§-tice čísel z telesa §T§ so sčítaním a násobením skalárom po zložkách.""", "tn"),
    Symbol("obal", """\langle\mathbf{x}_1,\dots,\mathbf{x}_m\rangle""", """(?<!\\left)\\langle.*?\\rangle""", "lineárny obal",
        """Množina všetkých lineárnych kombinácií daných vektorov, teda podpriestor, nie súbor ani jeden vektor.""", "obal"),
    Symbol("dim", """\dim P""", """\\dim(?![A-Za-z])""", "dimenzia",
        """Dĺžka najdlhšieho LN súboru v §P§, nie počet prvkov §P§; preto §\dim\{\theta\}=0§.""", "dimenzia"),
    Symbol("hodnost", """h(\mathbf{A})""", """(?<![A-Za-z\\])h(?=\()""", "hodnosť matice",
        """Dimenzia obalu riadkov matice; počet nenulových riadkov to je len v hornom stupňovitom tvare.""", "la1-k3-hodnost"),
    Symbol("sim", """\mathbf{A}\sim\mathbf{B}""", """\\sim(?![A-Za-z])""", "sa dá previesť GEM-om na",
        """Z §\mathbf{A}§ vznikne §\mathbf{B}§ riadkovými úpravami GEM; matice sa nemusia rovnať, zhodujú sa obaly ich riadkov.""", "la1-k3-sim"),
    Symbol("inverzna", """\mathbf{A}^{-1}""", """\\mathbf\{[A-Z]\}\^\{-1\}""", "inverzná matica",
        """Matica, ktorej súčin s §\mathbf{A}§ v oboch poradiach je §\mathbf{E}§; nie sú to prevrátené hodnoty prvkov.""", "la1-k3-inverzna"),
    Symbol("suradnice", """(\mathbf{v})_{\mathcal{B}}""", """\(\\mathbf\{[a-z]\}\)_\{\\mathcal\{[A-Z]\}(?:_\d)?\}""", "súradnice vzhľadom na bázu",
        """Koeficienty, ktorými sa §\mathbf{v}§ zloží z bázy §\mathcal{B}§ v jej poradí; je ich toľko, koľko prvkov má báza.""", "suradnice"),
    Symbol("standardna", """\mathcal{E}_n""", """\\mathcal\{E\}(?:_(?:n|\d))?""", "štandardná báza",
        """Súbor §(\mathbf{e}_1,\dots,\mathbf{e}_n)§, kde §\mathbf{e}_i§ má jednotku na §i§-tom mieste a inde nuly.""", "standardna"),
    Symbol("jednotkova", """\mathbf{E}""", """\\mathbf\{E\}(?:_n)?""", "jednotková matica",
        """Štvorcová matica s jednotkami na diagonále a nulami inde; násobenie ňou maticu nemení.""", "la1-k3-jednotkova"),
    Symbol("zameranie", """Z(W)""", """Z\(W\)""", "zameranie variety",
        """Podpriestor §P§ zo zápisu §W=\mathbf{a}+P§; na rozdiel od vektora posunutia je určený jednoznačne.""", "la1-k4-varieta"),
)

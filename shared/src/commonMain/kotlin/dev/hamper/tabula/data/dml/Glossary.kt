package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.Symbol

/** Tappable notation of DML (see [Symbol]). */
val DmlGlossary: List<Symbol> = listOf(
    // a|b, gcd(a, b) | c and 5\mid 12; not the | of |A|, |a|\cdot|b| or of a set-builder {k^2\mid ...}
    Symbol("dml-deli", """a\mid b""", """(?<![\^_]\d)(?<=\d)\\mid|(?:(?<=[a-z0-9)])|(?<=[a-z0-9)] ))(?<![A-Za-z\\][a-z])(?<![A-Za-z\\][a-z] )\|(?= ?[a-z0-9(])""", "delí",
        """§a\mid b§ je výrok „existuje celé §k§ s §ak=b§“, nie číslo ani zlomok §\tfrac{b}{a}§.""", "dml-tc-delitelnost"),
    Symbol("dml-equiv", """\equiv""", """\\equiv""", "je kongruentné s",
        """§a\equiv b\pmod{m}§ znamená §m\mid(a-b)§: vzťah dvoch čísel, nie výpočet zvyšku.""", "dml-tc-kongruence"),
    Symbol("dml-bmod", """\bmod""", """\\bmod""", "zvyšok po delení",
        """§a\bmod m§ je jedno číslo z §\{0,\dots,m-1\}§, kým kongruencia §\equiv§ je vzťah.""", "dml-tc-zbytek"),
    Symbol("dml-gcd", """\gcd""", """\\gcd""", "najväčší spoločný deliteľ",
        """§\gcd(a,b)§ je spoločný deliteľ deliteľný každým spoločným deliteľom; nikdy nie je záporný.""", "dml-tc-gcd"),
    Symbol("dml-zm", """\mathbb{Z}_m""", """\\mathbb\{Z\}_(?:[A-Za-z0-9]|\{[A-Za-z0-9]+\})""", "zvyšky modulo m",
        """§\mathbb{Z}_m=\{0,1,\dots,m-1\}§ a počíta sa v nej modulo §m§; samo §m§ v nej nie je.""", "dml-tc-zm"),
    Symbol("dml-varphi", """\varphi""", """\\varphi""", "Eulerova funkcia",
        """§\varphi(n)§ je počet čísel §1,\dots,n§ nesúdeliteľných s §n§; §n-1§ to je len pre prvočíslo.""", "dml-tc-euler"),
    Symbol("dml-subseteq", """\subseteq""", """\\subseteq""", "je podmnožinou",
        """§A\subseteq B§: každý prvok §A§ je aj v §B§. Znak §\in§ hovorí o prvku, nie o podmnožine.""", "dml-mz-inkluze"),
    Symbol("dml-potencna", """\mathcal{P}""", """\\mathcal\{P\}""", "potenčná množina",
        """§\mathcal{P}(A)§ je množina všetkých podmnožín §A§, preto §X\in\mathcal{P}(A)§ práve keď §X\subseteq A§.""", "dml-mz-inkluze"),
    Symbol("dml-hf", """H_f""", """(?<![A-Za-z\\])H_(?:f|\{f\})""", "obor hodnôt",
        """§H_f§ sú len hodnoty, ktoré §f§ naozaj nadobudne, a môže byť menší než cieľová množina §B§.""", "dml-mz-zobrazeni"),
    Symbol("dml-vzor", """f^{-1}[N]""", """[a-z]\^\{-1\}\[""", "vzor množiny",
        """§f^{-1}[N]§ sú prvky, ktoré §f§ pošle do §N§; existuje vždy, aj keď §f§ inverziu nemá.""", "dml-mz-obraz"),
    Symbol("dml-circ", """f\circ g""", """(?:\\c[abc]\{[a-z]\}|[a-z])\s*\\circ\s*(?:\\c[abc]\{[a-z]\}|[a-z])""", "zložené s",
        """§(f\circ g)(a)=f(g(a))§: najprv sa použije §g§, potom §f§, teda sprava doľava.""", "dml-mz-slozeni"),
    Symbol("dml-id", """\mathrm{id}_A""", """\\mathrm\{id\}""", "identita",
        """§\mathrm{id}_A§ vráti každý prvok §A§ nezmenený; identity na rôznych množinách sú rôzne zobrazenia.""", "dml-mz-idzobr"),
    Symbol("dml-mohutnost", """|A|""", """\|(?:[A-Z]|\\mathbb\{[A-Z]\})\|""", "mohutnosť",
        """§|A|=|B|§ znamená, že existuje bijekcia §A\to B§; pri konečnej množine je §|A|§ počet prvkov.""", "dml-mz-mohutnost"),
)

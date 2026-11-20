package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Definitions of Teorie čísel (Skriptá DML, chap-06-tc.woo, Definice 6.1 to 6.11), Czech word for word; the
 * textbook's macros written out (\Z = \mathbb{Z}, \ceq = :=), its "not congruent" symbol as ≢.
 */

internal val tcDelitelnost = Concept(
    id = "dml-tc-delitelnost", name = "Deliteľnosť", src = "Skriptá, Definice 6.1",
    czech = listOf(
        p("""Nechť §a, b \in \mathbb{Z}§. Řekneme, že **§a§ dělí §b§**, značíme §a|b§, jestliže existuje §k \in \mathbb{Z}§ takové, že"""),
        b("""a\cdot k = b."""),
        p("""V takovém případě říkáme, že §a§ je (celočíselný) **dělitel** §b§ a §b§ je (celočíselný) **násobek** §a§, případně také, že §b§ je **dělitelné** §a§."""),
        p("""Pokud §a§ nedělí §b§, píšeme §a \nmid b§. Samotné §|§ nazýváme **relací dělitelnosti**."""),
    ),
    why = listOf(
        w("""§a, b \in \mathbb{Z}§""", """Aj záporné čísla a nula. Preto §-3|6§ aj §5|0§."""),
        w("""existuje §k \in \mathbb{Z}§""", """Celé §k§, nie zlomok. §2|7§ neplatí, lebo §k=\tfrac{7}{2}§ nie je celé číslo."""),
        w("""§a\cdot k = b§""", """Definícia nedelí, len násobí. Preto §0|0§ platí (§0\cdot 1=0§) a o delení nulou netreba hovoriť."""),
    ),
    judge = listOf(
        j("""§-4\mid 12§""", true, """Áno, §k=-3§ je celé: §(-4)\cdot(-3)=12§.""", """Záporný deliteľ je v poriadku. Definícia chce len celé §k§ s §a\cdot k=b§, a §k=-3§ vyhovuje."""),
        j("""§7\mid 0§""", true, """Áno, §7\cdot 0=0§ a §k=0§ je celé.""", """Nula je násobok každého čísla: §k=0§ je celé a §7\cdot 0=0§ (Tvrzení 6.1)."""),
        j("""§0\mid 5§""", false, """Nie, §0\cdot k=0\neq 5§ pre každé celé §k§.""", """Žiadne §k§ nedá §0\cdot k=5§. Nula delí len nulu."""),
        j("""§3\mid 10§""", false, """Nie, §k=\tfrac{10}{3}§ nie je celé číslo.""", """Definícia chce §k\in\mathbb{Z}§. Zlomok §\tfrac{10}{3}§ nestačí."""),
    ),
    units = listOf("§a,b\\in\\mathbb{Z}§", "existuje §k\\in\\mathbb{Z}§", "§a\\cdot k=b§"),
    extra = listOf("dělitel, násobek, dělitelné", "§a\\nmid b§ a relace dělitelnosti"),
    trap = Trap("§a|b§ znamená, že §\\tfrac{b}{a}§ je celé číslo.", "Pre §a\\neq 0§ je to to isté, ale zlomok definícia nepoužíva: §0|0§ platí, hoci §\\tfrac{0}{0}§ nemá zmysel.", "Skriptá, Definice 6.1, Tvrzení 6.1"),
    ask = "Napište definici relace dělitelnosti.",
    question = "Platí táto deliteľnosť?",
    short = "deliteľnosti",
)

internal val tcZbytek = Concept(
    id = "dml-tc-zbytek", name = "Podiel a zvyšok", src = "Skriptá, Definice 6.2",
    czech = listOf(
        p("""Číslo §q§ z předchozí Věty 6.2 nazveme **celočíselný podíl (po dělení §a§ číslem §d§)**. Číslo §r\in\{0,1,\dots,d-1\}§ nazveme **zbytkem po (celočíselném) dělení §a§ číslem §d§** a značíme jej"""),
        b("""r = a \bmod d."""),
    ),
    why = listOf(
        w("""§a \in \mathbb{Z}, d \in \mathbb{N}§""", """Delenec môže byť záporný, deliteľ je kladný. Preto má §-17\bmod 5§ zmysel."""),
        w("""§0 \leq r < d§""", """Práve táto podmienka robí §q§ a §r§ jednoznačnými. Zvyšok nikdy nie je záporný, ani pri zápornom §a§."""),
        w("""§r = a \bmod d§""", """§\bmod§ je funkcia: číslu priradí jeho zvyšok. Kongruencia §a\equiv b\pmod{m}§ je naopak vzťah dvoch čísel."""),
    ),
    judge = listOf(
        j("""§-17\bmod 5=3§""", true, """Áno: §-17=(-4)\cdot 5+3§ a §0\leq 3<5§.""", """Zvyšok nie je §-2§. §-17=(-3)\cdot 5-2§ síce platí, ale zvyšok musí byť z §\{0,\dots,4\}§, preto §q=-4§, §r=3§."""),
        j("""§-17\bmod 5=-2§""", false, """Nie, zvyšok musí byť z §\{0,\dots,4\}§; správne §3§.""", """Rovnosť §-17=(-3)\cdot 5-2§ platí, ale §r=-2§ porušuje §0\leq r<d§. Jednoznačný zvyšok je §3§."""),
        j("""§23\bmod 7=2§""", true, """Áno: §23=3\cdot 7+2§.""", """§3\cdot 7=21§ a §23-21=2§, zvyšok je z §\{0,\dots,6\}§."""),
        j("""§23=2\cdot 7+9§ dáva §23\bmod 7=9§""", false, """Nie, §9\geq 7§; zvyšok musí byť menší než §d§.""", """Rovnosť platí, ale §r<d§ nie. Podiel treba zväčšiť: §23=3\cdot 7+2§."""),
    ),
    units = listOf("§a\\in\\mathbb{Z}§, §d\\in\\mathbb{N}§", "jednoznačné §q,r§ s §a=qd+r§", "§0\\leq r<d§", "§q§ celočíselný podiel, §r§ zvyšok", "§r=a\\bmod d§"),
    ask = "Napište definici celočíselného podílu a zbytku po dělení.",
    question = "Je to správny zvyšok?",
    short = "podielu a zvyšku",
)

internal val tcGcd = Concept(
    id = "dml-tc-gcd", name = "gcd, lcm a nesúdeliteľnosť", src = "Skriptá, Definice 6.3",
    czech = listOf(
        p("""Buďte §a,b \in \mathbb{Z}§."""),
        i("""Číslo §n \in \mathbb{N}_0§ je **společný dělitel** čísel §a, b§, jestliže §n|a \wedge n|b§."""),
        i("""Číslo §n \in \mathbb{N}_0§ je **největší společný dělitel** čísel §a, b§, značíme §n=\gcd(a,b)§, pokud je jejich společný dělitel a současně je (celočíselným) násobkem ==každého== jejich dalšího společného dělitele, tedy pokud"""),
        b("""\Big( n|a \wedge n|b \wedge (\forall d\in\mathbb{N}_0)\softbreak[1] \big( ( d|a \wedge d|b ) \Rightarrow d|n \big) \Big)."""),
        i("""Čísla §a,b§ nazýváme **nesoudělná**, jestliže §\gcd(a,b)=1§. Mluvíme o vlastnosti **nesoudělnosti**, také říkáme „číslo §a§ je nesoudělné s §b§“. Čísla §a,b§, pro která platí §\gcd(a,b)>1§, nazveme **soudělná**."""),
        i("""Číslo §n \in \mathbb{N}_0§ je **společný násobek** čísel §a, b§, jestliže §a|n \wedge b|n§."""),
        i("""Číslo §n \in \mathbb{N}_0§ je **nejmenší společný násobek** čísel §a, b§, značíme §n=\lcm(a,b)§, pokud je jejich společný násobek a navíc dělí každý další jejich společný násobek, tedy pokud"""),
        b("""\Big( a|n \wedge b|n \wedge (\forall m\in\mathbb{N}_0)\softbreak[1] \big( ( a|m \wedge b|m ) \Rightarrow n|m \big) \Big)."""),
    ),
    why = listOf(
        w("""§n \in \mathbb{N}_0§""", """§\gcd§ ani §\lcm§ nie sú záporné. §\gcd(-4,6)=2§, nie §-2§."""),
        w("""je (celočíselným) násobkem každého jejich dalšího společného dělitele""", """„Najväčší“ tu neznamená podľa §\leq§, ale „deliteľný každým spoločným deliteľom“. Vďaka tomu má zmysel aj §\gcd(0,0)=0§ (Poznámka 6.1)."""),
        w("""§(\forall d\in\mathbb{N}_0)\big((d|a \wedge d|b) \Rightarrow d|n\big)§""", """Kvantifikátor cez všetky spoločné delitele. Jeden spoločný deliteľ, ktorý delí §n§, nestačí."""),
        w("""dělí každý další jejich společný násobek""", """Pri §\lcm§ je to zrkadlovo: spoločný násobok, ktorý delí všetky ostatné spoločné násobky."""),
    ),
    judge = listOf(
        j("""§\gcd(12,18)=6§""", true, """Áno: §6§ delí obe a každý spoločný deliteľ §1,2,3,6§ delí §6§.""", """§6§ splní obe časti definície: je spoločný deliteľ a §1,2,3§ aj §6§ ho delia."""),
        j("""§\gcd(12,18)=3§""", false, """Nie, spoločný deliteľ §2§ nedelí §3§.""", """§3§ je spoločný deliteľ, ale definícia chce aj §d|n§ pre každý spoločný deliteľ §d§; §2\nmid 3§."""),
        j("""§\gcd(0,7)=7§""", true, """Áno, §7|0§ aj §7|7§ a každý deliteľ §7§ delí §7§.""", """Nula je násobok každého čísla, takže spoločné delitele §0§ a §7§ sú delitele §7§ (Poznámka 6.1)."""),
        j("""§\gcd(-8,12)=-4§""", false, """Nie, §\gcd§ je z §\mathbb{N}_0§; správne §4§.""", """§-4§ delí obe čísla, ale definícia chce §n\in\mathbb{N}_0§. Platí §\gcd(-8,12)=\gcd(8,12)=4§ (Lemma 6.1)."""),
        j("""§9§ a §28§ sú nesúdeliteľné""", true, """Áno, §\gcd(9,28)=1§, hoci ani jedno nie je prvočíslo.""", """Sú nesúdeliteľné, lebo §\gcd(9,28)=1§. Nesúdeliteľnosť nechce prvočísla: §9=3^2§ a §28=2^2\cdot 7§ len nemajú spoločné prvočíslo."""),
    ),
    units = listOf("§a,b\\in\\mathbb{Z}§", "§n\\in\\mathbb{N}_0§", "spoločný deliteľ: §n|a\\wedge n|b§", "§\\gcd§: každý spoločný deliteľ delí §n§", "§\\lcm§: spoločný násobok, ktorý delí každý spoločný násobok"),
    extra = listOf("nesúdeliteľné: §\\gcd(a,b)=1§"),
    trap = Trap("Největší společný dělitel je největší číslo, které dělí obě čísla.", "Školská definícia zlyhá pre §\\gcd(0,0)§: nulu delí každé číslo, najväčšie neexistuje. Spěvák ju akceptuje len s doplnením (6. prednáška).", "Poznámka 6.1; Spěvák, 6. prednáška"),
    ask = "Napište definici největšího společného dělitele a nejmenšího společného násobku.",
    question = "Platí to o gcd?",
    short = "gcd, lcm a nesúdeliteľnosti",
)

internal val tcLdrDef = Concept(
    id = "dml-tc-ldr", name = "Lineárna diofantická rovnica", src = "Skriptá, Definice 6.4",
    czech = listOf(
        p("""**Lineární diofantická rovnice (pro dvě neznámé)** je libovolná rovnice typu"""),
        b("""ax + by = c,"""),
        p("""kde §a, b, c \in \mathbb{Z}§ pro dvě neznámé §x, y \in \mathbb{Z}§. Nehrozí-li zmatení, můžeme vynechat přívlastek lineární, případně lze zkrátit na LDR."""),
    ),
    why = listOf(
        w("""§a, b, c \in \mathbb{Z}§""", """Celočíselné koeficienty. Rovnicu s §\tfrac{1}{2}x§ najprv vynásob, aby boli celé."""),
        w("""dvě neznámé §x, y \in \mathbb{Z}§""", """Riešenie hľadáme len v celých číslach. To robí z jednoduchej rovnice úlohu: §2x+4y=3§ nad §\mathbb{R}§ riešenie má, nad §\mathbb{Z}§ nie."""),
    ),
    judge = listOf(
        j("""§7x+13y=125§, §x,y\in\mathbb{Z}§""", true, """Áno, celé koeficienty a celé neznáme.""", """Presne tvar §ax+by=c§ s §a=7§, §b=13§, §c=125§ (Příklad 6.4)."""),
        j("""§x^2+y^2=25§, §x,y\in\mathbb{Z}§""", false, """Nie, nie je lineárna.""", """Je diofantická, ale kvadratická. Definice 6.4 berie len §ax+by=c§."""),
        j("""§2x+4y=3§, §x,y\in\mathbb{R}§""", false, """Nie, neznáme musia byť celé.""", """Tvar sedí, ale nad §\mathbb{R}§ to nie je diofantická rovnica. Nad §\mathbb{Z}§ by bola, a nemala by riešenie."""),
    ),
    units = listOf("§ax+by=c§", "§a,b,c\\in\\mathbb{Z}§", "neznáme §x,y\\in\\mathbb{Z}§"),
    ask = "Definujte lineární diofantickou rovnici.",
    question = "Je to lineárna diofantická rovnica?",
    short = "lineárnej diofantickej rovnice",
)

internal val tcKongruence = Concept(
    id = "dml-tc-kongruence", name = "Kongruencia modulo §m§", src = "Skriptá, Definice 6.7",
    czech = listOf(
        p("""Nechť §a,b \in \mathbb{Z}, m \in \mathbb{N}§. Pokud §m|(a-b)§, říkáme, že §a§ je **kongruentní s §b§ modulo §m§** a píšeme"""),
        b("""a \equiv b \pmod{m}."""),
        p("""V opačném případě §a§ **není kongruentní s §b§ modulo §m§** a píšeme §a ≢ b \pmod{m}§."""),
        p("""Samotné §\equiv \pmod{m}§ nazýváme **relací kongruence modulo §m§**. Číslo §m§ nazýváme **modulo** nebo **modul**. Obvykle uvažujeme pouze §m \geq 2§."""),
    ),
    why = listOf(
        w("""§m|(a-b)§""", """Kongruencia stojí na deliteľnosti rozdielu. Ekvivalentne: §a§ a §b§ majú rovnaký zvyšok po delení §m§ (Tvrzení 6.4)."""),
        w("""§m \in \mathbb{N}§, obvykle §m \geq 2§""", """Modulo §1§ sú kongruentné všetky čísla, nič to nehovorí. Preto §m\geq 2§."""),
        w("""relace""", """Kongruencia je vzťah dvoch čísel, nie výpočet. §a\bmod m§ je naopak funkcia, ktorá vráti zvyšok (Spěvák, 7. prednáška)."""),
    ),
    judge = listOf(
        j("""§-3\equiv 11\pmod{7}§""", true, """Áno, §-3-11=-14§ je násobok §7§.""", """Záporné číslo nevadí: §7|(-14)§, teda aj obe čísla majú zvyšok §4§."""),
        j("""§17\equiv 2\pmod{4}§""", false, """Nie, §17-2=15§ nie je násobok §4§.""", """§17\bmod 4=1§ a §2\bmod 4=2§: zvyšky sa líšia, §4\nmid 15§."""),
        j("""§a\equiv a+5m\pmod{m}§ pre každé §a§""", true, """Áno, rozdiel §-5m§ je násobok §m§.""", """Pripočítať násobok modulu kongruenciu nezmení (Tvrzení 6.4, bod 3)."""),
        j("""§12\equiv 7\pmod{5}§ znamená §12=7\bmod 5§""", false, """Nie, kongruencia platí, ale §7\bmod 5=2\neq 12§. Kongruencia je vzťah dvoch čísel, nie rovnosť so zvyškom.""", """Kongruencia neprikazuje, že ľavá strana je zvyšok. Rovnosť so zvyškom z nej plynie, len keď ľavá strana leží v §\{0,\dots,4\}§."""),
    ),
    units = listOf("§a,b\\in\\mathbb{Z}§, §m\\in\\mathbb{N}§", "§m|(a-b)§", "zápis §a\\equiv b\\pmod{m}§"),
    extra = listOf("inak §a ≢ b\\pmod{m}§", "§m§ je modul, obvykle §m\\geq 2§"),
    trap = Trap("§a\\equiv b\\pmod{m}§ znamená §a=b\\bmod m§.", "Platí to len vtedy, keď §a\\in\\{0,\\dots,m-1\\}§. Kongruencia je vzťah: §12\\equiv 7\\pmod{5}§, hoci §12\\neq 2§.", "Definice 6.7; Spěvák, 7. prednáška"),
    ask = "Napište definici kongruence modulo §m§.",
    question = "Platí to o kongruencii?",
    short = "kongruencie",
)

internal val tcZm = Concept(
    id = "dml-tc-zm", name = "Množina zvyškov §\\mathbb{Z}_m§", src = "Skriptá, Definice 6.8",
    czech = listOf(
        p("""Nechť §m\geq 2§. Jako §\mathbb{Z}_m§ označíme množinu všech zbytků modulo §m§,"""),
        b("""\mathbb{Z}_m=\{0,1,2,\dots,m-1\}."""),
        p("""Na §\mathbb{Z}§ zavádíme operace **sčítání** a **násobení** modulo §m§:"""),
        b("""+_{m}: \mathbb{Z}\times\mathbb{Z}\to\mathbb{Z}_m,\quad\softbreak a +_{m} b := (a + b) \bmod m,"""),
        b("""\cdot_{m}: \mathbb{Z}\times\mathbb{Z}\to\mathbb{Z}_m,\quad\softbreak a \cdot_{m} b := (a\cdot b) \bmod m,"""),
        p("""kde operace §+, \cdot§ jsou standardní operace sčítání a násobení celých čísel."""),
    ),
    why = listOf(
        w("""§\mathbb{Z}_m=\{0,1,2,\dots,m-1\}§""", """Presne §m§ prvkov, všetky možné zvyšky. Číslo §m§ samo v §\mathbb{Z}_m§ nie je."""),
        w("""§a +_{m} b := (a + b) \bmod m§""", """Sčítaš ako v §\mathbb{Z}§ a vezmeš zvyšok. Z §\mathbb{Z}_m§ sa tak nikdy nedostaneš."""),
        w("""§\mathbb{Z}\times\mathbb{Z}\to\mathbb{Z}_m§""", """Vstupy smú byť ľubovoľné celé čísla, napr. §-1§ namiesto §m-1§. Výsledok je vždy zvyšok."""),
    ),
    judge = listOf(
        j("""§5 +_{7} 4 = 2§""", true, """Áno, §9\bmod 7=2§.""", """§5+4=9§ nie je prvok §\mathbb{Z}_7§; operácia vráti zvyšok §2§."""),
        j("""§4\cdot_{6} 3 = 12§""", false, """Nie, §12\bmod 6=0§; výsledok musí byť z §\mathbb{Z}_6§.""", """§12§ je súčin v §\mathbb{Z}§. Operácia §\cdot_{6}§ ho ešte zredukuje na §0§."""),
        j("""§\mathbb{Z}_5§ má §5§ prvkov""", true, """Áno, §\{0,1,2,3,4\}§.""", """Zvyšky po delení §5§ sú §0§ až §4§, teda päť čísel; §5§ medzi nimi nie je."""),
    ),
    units = listOf("§m\\geq 2§", "§\\mathbb{Z}_m=\\{0,\\dots,m-1\\}§", "§+_m,\\cdot_m:\\mathbb{Z}\\times\\mathbb{Z}\\to\\mathbb{Z}_m§", "§a+_m b:=(a+b)\\bmod m§", "§a\\cdot_m b:=(a\\cdot b)\\bmod m§"),
    ask = "Definujte množinu §\\mathbb{Z}_m§ a operace sčítání a násobení modulo §m§.",
    question = "Platí to o počítaní so zvyškami?",
    short = "množiny zvyškov modulo m",
)

internal val tcInverze = Concept(
    id = "dml-tc-inverze", name = "Inverzia v §\\mathbb{Z}_m§", src = "Skriptá, Definice 6.9",
    czech = listOf(
        p("""Buďte §m\geq 2§ a §a\in\mathbb{Z}_m§. Číslo §x\in\mathbb{Z}_m§ nazveme"""),
        i("""**aditivní inverzí** k §a§ (značíme §-a§), právě když platí"""),
        b("""x +_{m} a = a +_{m} x = 0."""),
        i("""**multiplikativní inverzí** k §a§ (značíme §a^{-1}§), právě když platí"""),
        b("""x \cdot_{m} a = a \cdot_{m} x = 1."""),
        p("""Nehrozí-li nedorozumění, obvykle namísto aditivní inverze mluvíme o **opačném prvku**. V tom případě pak pojem multiplikativní inverze může být zkrácen pouze na **inverzní prvek**, příp. **inverzi prvku**."""),
    ),
    why = listOf(
        w("""§x\in\mathbb{Z}_m§""", """Inverzia je prvok §\mathbb{Z}_m§, teda z §\{0,\dots,m-1\}§. Záporný Bézoutov koeficient treba zredukovať."""),
        w("""§x \cdot_{m} a = a \cdot_{m} x = 1§""", """Inverzia dá súčin §1§ modulo §m§. Existuje práve pri §\gcd(a,m)=1§ (Věta 6.12)."""),
        w("""§x +_{m} a = 0§""", """Opačný prvok existuje vždy: §m-a§ (pre §a\neq 0§). Nezamieňaj ho s inverziou."""),
    ),
    judge = listOf(
        j("""§3^{-1}=5§ v §\mathbb{Z}_7§""", true, """Áno, §3\cdot 5=15\equiv 1\pmod{7}§.""", """Súčin §15§ má zvyšok §1§ po delení §7§, presne to definícia chce."""),
        j("""§3^{-1}=4§ v §\mathbb{Z}_7§""", false, """Nie, §4§ je opačný prvok: §3+4\equiv 0§.""", """§3\cdot 4=12\equiv 5\pmod{7}§, nie §1§. §4§ je §-3§, aditívna inverzia."""),
        j("""§3^{-1}§ v §\mathbb{Z}_{12}§ neexistuje""", true, """Áno, §\gcd(3,12)=3\neq 1§.""", """Každý násobok §3§ má po delení §12§ zvyšok §0,3,6§ alebo §9§, nikdy §1§ (Příklad 6.8)."""),
        j("""§2^{-1}=4§ v §\mathbb{Z}_9§""", false, """Nie, §2\cdot 4=8§, nie §1§; inverzia je §5§.""", """§2\cdot 4=8 ≢ 1\pmod 9§. Inverzia je §5§, lebo §2\cdot 5=10\equiv 1§."""),
    ),
    units = listOf("§m\\geq 2§, §a\\in\\mathbb{Z}_m§", "aditívna: §x+_m a=a+_m x=0§, značí sa §-a§", "multiplikatívna: §x\\cdot_m a=a\\cdot_m x=1§, značí sa §a^{-1}§", "§x\\in\\mathbb{Z}_m§"),
    ask = "Definujte aditivní a multiplikativní inverzi v §\\mathbb{Z}_m§.",
    question = "Platí to o inverzii?",
    short = "multiplikatívnej inverzie",
)

internal val tcEuler = Concept(
    id = "dml-tc-euler", name = "Eulerova funkcia φ", src = "Skriptá, Definice 6.10",
    czech = listOf(
        p("""Eulerova funkce §\varphi(n): \mathbb{N} \to \mathbb{N}§ je zobrazení, které každému §n\in\mathbb{N}§ přiřadí počet přirozených čísel menších nebo rovných §n§, která jsou s §n§ nesoudělná, tj."""),
        b("""(\forall n \in \mathbb{N})\softbreak\big(\varphi(n) := \big| \{k \in \mathbb{N} \mid\softbreak[1] k \leq n \wedge \gcd(k,n) = 1 \} \big|\big)."""),
    ),
    why = listOf(
        w("""počet přirozených čísel menších nebo rovných §n§""", """Rátajú sa §1,\dots,n§. Pre §n\geq 2§ samo §n§ nikdy nevyhovie, lebo §\gcd(n,n)=n§."""),
        w("""nesoudělná s §n§""", """Pre §n\geq 2§ sú to presne tie, ktoré majú v §\mathbb{Z}_n§ inverziu (Věta 6.12). Preto §\varphi(n)§ je exponent v Eulerovej vete."""),
    ),
    judge = listOf(
        j("""§\varphi(10)=4§""", true, """Áno: §1,3,7,9§.""", """Párne čísla a §5§ sú súdeliteľné s §10§; ostanú štyri (Příklad 6.9)."""),
        j("""§\varphi(9)=8§""", false, """Nie, §9§ nie je prvočíslo: §\varphi(9)=9-3=6§.""", """§n-1§ platí len pre prvočísla (Tvrzení 6.8). §3§ a §6§ sú súdeliteľné s §9§."""),
        j("""§\varphi(13)=12§""", true, """Áno, §13§ je prvočíslo.""", """Všetky §1,\dots,12§ sú s prvočíslom §13§ nesúdeliteľné (Tvrzení 6.8)."""),
        j("""§\varphi(12)=\varphi(3)\cdot\varphi(4)=2\cdot 2=4§""", true, """Áno, §3§ a §4§ sú nesúdeliteľné.""", """Multiplikativita (Tvrzení 6.10) chce nesúdeliteľné činitele, a §\gcd(3,4)=1§. Kontrola: §1,5,7,11§."""),
        j("""§\varphi(12)=\varphi(2)\cdot\varphi(6)=1\cdot 2=2§""", false, """Nie, §2§ a §6§ sú súdeliteľné; správne §4§.""", """Multiplikativita platí len pre nesúdeliteľné činitele (Tvrzení 6.10), a §\gcd(2,6)=2§."""),
    ),
    units = listOf("§\\varphi:\\mathbb{N}\\to\\mathbb{N}§", "počet §k\\in\\mathbb{N}§, §k\\leq n§", "s §\\gcd(k,n)=1§"),
    ask = "Definujte Eulerovu funkci φ.",
    question = "Platí to o Eulerovej funkcii?",
    short = "Eulerovej funkcie",
)

internal val tcLkonDef = Concept(
    id = "dml-tc-lkon", name = "Lineárna kongruencia", src = "Skriptá, Definice 6.11",
    czech = listOf(
        p("""Řešením **lineární kongruence** rozumíme nalezení ==všech== celých čísel §x§ splňujících kongruenci"""),
        b("""ax \equiv b \pmod m,"""),
        p("""kde §a, b, m \in \mathbb{Z}§ a §m \geq 2§."""),
    ),
    why = listOf(
        w("""nalezení všech celých čísel §x§""", """Všetky, nie jedno. Výsledok je trieda §x\equiv x_0\pmod{n}§, prípadne „nemá riešenie“."""),
        w("""§ax \equiv b \pmod m§""", """Je to §m|(ax-b)§, teda LDR §ax+my=b§ s neznámou §y§, ktorá nás nezaujíma (Věta 6.17)."""),
    ),
    judge = listOf(
        j("""Riešenia §9x\equiv 12\pmod{15}§ sú §x\equiv 3\pmod{5}§""", true, """Áno, v §\mathbb{Z}_{15}§ sú to §3,8,13§ (Příklad 6.17).""", """Kráti sa §3§ a modul sa zmení na §15/3=5§ (Věta 6.13). Skúška: §9\cdot 8=72\equiv 12\pmod{15}§."""),
        j("""Riešenia §9x\equiv 12\pmod{15}§ sú §x\equiv 3\pmod{15}§""", false, """Nie, chýbajú §8§ a §13§: modul sa krátením zmenšil na §5§.""", """§x=8§ kongruenciu spĺňa, a §8 ≢ 3\pmod{15}§. Pri krátení §3§ sa delí aj modul (Věta 6.13)."""),
        j("""§2x\equiv 3\pmod{4}§ nemá riešenie""", true, """Áno, §\gcd(2,4)=2§ nedelí §3§.""", """§2x§ je párne, §2x-3§ nepárne, nikdy násobok §4§ (Věta 6.17)."""),
    ),
    units = listOf("nájsť všetky §x\\in\\mathbb{Z}§", "§ax\\equiv b\\pmod{m}§", "§a,b,m\\in\\mathbb{Z}§, §m\\geq 2§"),
    ask = "Co rozumíme řešením lineární kongruence?",
    question = "Platí to o riešeniach kongruencie?",
    short = "lineárnej kongruencie",
)

internal val tcConcepts = listOf(tcDelitelnost, tcZbytek, tcGcd, tcLdrDef, tcKongruence, tcZm, tcInverze, tcEuler, tcLkonDef)

package dev.hamper.tabula.data.dml

import dev.hamper.tabula.data.*
import dev.hamper.tabula.data.dsl.*

/*
 * Theorems of Teorie čísel (Skriptá DML, chap-06-tc.woo), Czech word for word, and the proofs the oral exam asks
 * (classification.adoc: věty Fermatova, Eulerova, ČVOZ), built line by line from the textbook's own proofs.
 */

private const val Circle = "Kruh: odvolávaš sa na to, čo práve dokazuješ."

internal val tcProofDeleni = Proof(
    "dml-tc-p-deleni", "Delenie so zvyškom", "Skriptá, Věta 6.2 a jej dôkaz",
    null, null,
    m("""Existenciu pre §a\geq 0§ ukážeme indukciou podľa §a§, potom pre §a<0§, nakoniec jednoznačnosť."""),
    listOf(
        r("Základný krok, §a=0§:", 0,
            c("""§q=r=0§, lebo §0=0\cdot d+0§ a §0\leq 0<d§."""),
            c("""§q=0§, §r=d§.""", """Zvyšok musí spĺňať §r<d§, a §d<d§ neplatí."""),
            c("""Pre §a=0§ podľa Věty 6.2 existujú §q,r§.""", Circle)),
        r("""Indukčný krok: nech §a=qd+r§, §0\leq r<d§. Čo pre §a+1§?""", 1,
            c("""§a+1=qd+(r+1)§ a §r+1§ je zvyšok.""", """Môže byť §r+1=d§, a to zvyškom nie je. Ten prípad treba zvlášť."""),
            c("""§a+1=qd+(r+1)§: ak §r+1<d§, hotovo; ak §r+1=d§, tak §a+1=(q+1)d+0§."""),
            c("""§a+1=(q+1)d+r§.""", """Pridal si celé §d§ namiesto §1§; rovnosť neplatí."""),
            given = """Indukčný krok: nech §a=qd+r§, §0\leq r<d§."""),
        r("""Pre §a<0§: z §|a|=-a=qd+r§ máme §a=(-q)d+(-r)§. Ďalej?""", 0,
            c("""Ak §r=0§, hotovo; inak §a=(-q-1)d+(d-r)§ a §0<d-r<d§."""),
            c("""§-r§ je zvyšok a §-q§ podiel.""", """Pre §r>0§ je §-r<0§, porušuje §0\leq r§."""),
            c("""Zvyšok §a§ je ten istý ako zvyšok §|a|§.""", """Neplatí: §17\bmod 5=2§, ale §-17\bmod 5=3§."""),
            given = """Pre §a<0§: z §|a|=-a=qd+r§ máme §a=(-q)d+(-r)§."""),
        r("""Jednoznačnosť: nech §a=q_1d+r_1=q_2d+r_2§, §0\leq r_1,r_2<d§.""", 2,
            c("""Obe sú zvyšky, preto §r_1=r_2§.""", """To je presne tvrdenie. Treba ho odvodiť z rovností."""),
            c("""§d|(r_1-r_2)§, preto §r_1-r_2=d§.""", """Platí §|r_1-r_2|<d§; jediný násobok §d§ v tom rozsahu je §0§."""),
            c("""§(q_2-q_1)d=r_1-r_2§, teda §d|(r_1-r_2)§; zároveň §|r_1-r_2|<d§, preto §r_1=r_2§ aj §q_1=q_2§."""),
            given = """Jednoznačnosť: nech §a=q_1d+r_1=q_2d+r_2§, §0\leq r_1,r_2<d§."""),
    ),
    m("""Existujú a sú jednoznačné: §a=qd+r§, §0\leq r<d§."""),
    listOf("Indukcia pre §a\\geq 0§: ZK §a=0§, IK s prípadom §r+1=d§", "§a<0§ cez §|a|§ a úprava §(-q-1)d+(d-r)§", "Jednoznačnosť: §d|(r_1-r_2)§ a §|r_1-r_2|<d§"),
    minutes = 12,
    goals = listOf("Základný krok §a=0§", "Indukčný krok pre §a+1§", "Záporné §a§", "Jednoznačnosť"),
)

internal val tcProofLdr = Proof(
    "dml-tc-p-ldr", "Kedy má LDR riešenie", "Skriptá, Věta 6.7 a jej dôkaz",
    null,
    null,
    m("""Ekvivalencia: dokážeme každú implikáciu zvlášť. Prípad §a=b=0§ je zrejmý: rovnica je §0=c§, rieši ju každá dvojica práve pri §c=0=\gcd(0,0)§."""),
    listOf(
        r("""§(\Rightarrow)§: nech §ax+by=c§ pre nejaké §x,y\in\mathbb{Z}§.""", 1,
            c("""Potom §a|c§ aj §b|c§.""", """Neplatí: §2\cdot 1+3\cdot 1=5§, a §2\nmid 5§."""),
            c("""§\gcd(a,b)§ delí §a§ aj §b§, preto delí aj §ax+by=c§ (Tvrzení 6.1)."""),
            c("""Podľa Bézoutovej rovnosti je §c=\gcd(a,b)§.""", """Bézout hovorí len, že §\gcd§ je nejaká kombinácia; §c§ môže byť jeho násobok."""),
            given = """§(\Rightarrow)§: nech §ax+by=c§ pre nejaké §x,y\in\mathbb{Z}§."""),
        r("""§(\Leftarrow)§: nech §c=k\cdot\gcd(a,b)§, §k\in\mathbb{Z}§.""", 0,
            c("""REA dá §\gcd(a,b)=\alpha a+\beta b§; potom §c=(k\alpha)a+(k\beta)b§, teda §x=k\alpha§, §y=k\beta§."""),
            c("""Stačí §x=\tfrac{c}{a}§, §y=0§.""", """§\tfrac{c}{a}§ nemusí byť celé: §2x+3y=1§ má riešenie, §\tfrac{1}{2}§ nie je celé."""),
            c("""Riešenie existuje podľa Věty 6.8.""", """Věta 6.8 predpokladá, že jedno riešenie už máme. Na existenciu ju použiť nemôžeš."""),
            given = """§(\Leftarrow)§: nech §c=k\cdot\gcd(a,b)§, §k\in\mathbb{Z}§."""),
    ),
    m("""Obe implikácie platia: §ax+by=c§ má riešenie práve vtedy, keď §\gcd(a,b)|c§."""),
    listOf("§(\\Rightarrow)§: §\\gcd§ delí každú celočíselnú kombináciu §a,b§", "§(\\Leftarrow)§: Bézoutova rovnosť vynásobená §k=c/\\gcd(a,b)§", "Riešenie vypísané: §x=k\\alpha§, §y=k\\beta§"),
    minutes = 6,
    goals = listOf("""§\gcd(a,b)§ delí §c§""", "Riešenie z Bézouta"),
)

internal val tcProofInverze = Proof(
    "dml-tc-p-inverze", "Kedy existuje inverzia", "Skriptá, Věta 6.12 a jej dôkaz",
    null,
    null,
    m("""Dôkaz je konštruktívny: preložíme ho na diofantickú rovnicu."""),
    listOf(
        r("Čomu je ekvivalentná existencia §a^{-1}§?", 0,
            c("""Existuje §x\in\mathbb{Z}_m§ s §ax\equiv 1\pmod{m}§, teda riešenie LDR §ax+my=1§."""),
            c("""Existuje §x§ s §a\cdot x=1§ v §\mathbb{Z}§.""", """V §\mathbb{Z}§ má inverziu len §\pm 1§. Hľadáme súčin §\equiv 1\pmod{m}§."""),
            c("""Platí §\gcd(a,m)=1§ podľa Věty 6.12.""", Circle)),
        r("Kedy má §ax+my=1§ riešenie?", 1,
            c("""Vždy, lebo §m\geq 2§.""", """§2x+4y=1§ riešenie nemá: ľavá strana je párna."""),
            c("""Práve keď §\gcd(a,m)|1§, teda §\gcd(a,m)=1§ (Věta 6.7)."""),
            c("""Len keď §m§ je prvočíslo.""", """Prvočíselný modul stačí pre nenulové §a§, ale netreba ho: §3^{-1}=3§ v §\mathbb{Z}_8§.""")),
        r("Jednoznačnosť: aké sú všetky riešenia §x§?", 2,
            c("""§x=x_0+k\cdot m§ a v §\mathbb{Z}_m§ ich je §m§.""", """§x_0+km§ pre rôzne §k§ dávajú v §\mathbb{Z}_m§ ten istý zvyšok, nie §m§ rôznych."""),
            c("""Jediné §x_0§, lebo LDR má jediné riešenie.""", """LDR má nekonečne veľa riešení (Věta 6.8). Jednoznačná je až redukcia do §\mathbb{Z}_m§."""),
            c("""Podľa Věty 6.8 §x=x_0+k\tfrac{m}{\gcd(a,m)}=x_0+km§; do §\mathbb{Z}_m§ padne práve jedno.""")),
    ),
    m("""Inverzia existuje práve pri §\gcd(a,m)=1§ a v §\mathbb{Z}_m§ je jediná; nájde ju REA na §ax+my=1§."""),
    listOf("§a^{-1}§ existuje §\\Leftrightarrow§ §ax+my=1§ má riešenie", "Věta 6.7: §\\gcd(a,m)|1§", "Všetky riešenia §x_0+km§, v §\\mathbb{Z}_m§ jedno"),
    minutes = 6,
    goals = listOf("Preklad na §ax+my=1§", """Podmienka §\gcd(a,m)=1§""", """Jednoznačnosť v §\mathbb{Z}_m§"""),
    warn = m("""A pri zloženom §m§? Niektoré nenulové prvky inverziu nemajú. V §\mathbb{Z}_{12}§ je §\gcd(3,12)=3§, preto §3x§ dáva len zvyšky §0,3,6,9§, nikdy §1§."""),
)

internal val tcProofKongruenceEkv = Proof(
    "dml-tc-p-kongruence-ekv", "Kongruencia a rovnaké zvyšky", "Skriptá, Tvrzení 6.4 a jeho dôkaz",
    null,
    null,
    m("""Body (1) až (3) rozpíšeme z definície, bod (4) pomocou delenia so zvyškom (Věta 6.2)."""),
    listOf(
        r("""Prečo sú (1), (2) a (3) ekvivalentné?""", 0,
            c("""Podľa definície §a\equiv b\pmod m§ znamená §m\mid(a-b)§, teda §(\exists k\in\mathbb{Z})(a-b=km)§. To je (3) aj (2), lebo §a-b=(a-b)-0§."""),
            c("""Lebo §m\mid(a-b)§ znamená §|a-b|<m§.""", """Deliteľnosť nehovorí o veľkosti: §5\mid 15§, hoci §15>5§. Znamená §a-b=km§ pre celé §k§."""),
            c("""Bod (3) plynie z vety o delení so zvyškom.""", """Netreba ju: §a=b+km§ je len prepísaná definícia deliteľnosti §m\mid(a-b)§.""")),
        r("""K bodu (4): čo urobíš s §a§ a §b§?""", 2,
            c("""Predpokladám §0\leq a,b<m§.""", """Tvrdenie je pre všetky celé §a,b§, zúžiť si ich nesmieš."""),
            c("""Použijem §(a-b)\bmod m=a\bmod m-b\bmod m§.""", """Neplatí: §(1-4)\bmod 5=2§, ale §1\bmod 5-4\bmod 5=-3§."""),
            c("""Obe vydelím §m§ so zvyškom: §a=q_a m+(a\bmod m)§ a §b=q_b m+(b\bmod m)§. Odčítam: §a-b=(q_a-q_b)m+r§, kde §r=a\bmod m-b\bmod m§.""")),
        r("""§(1)\Rightarrow(4)§: nech §m\mid(a-b)§.""", 1,
            c("""Potom §a=b§, preto sa rovnajú aj zvyšky.""", """Z kongruencie rovnosť neplynie: §12\equiv 7\pmod 5§, hoci §12\neq 7§."""),
            c("""Potom §m§ delí aj §r=a\bmod m-b\bmod m§. Ten rozdiel je medzi §-(m-1)§ a §m-1§ a jediný násobok §m§ tam je §0§."""),
            c("""Potom §a\bmod m-b\bmod m=m§.""", """Rozdiel dvoch zvyškov z §\{0,\dots,m-1\}§ je v absolútnej hodnote menší než §m§, takže §m§ nevyjde."""),
            given = """§(1)\Rightarrow(4)§: nech §m\mid(a-b)§."""),
        r("""§(4)\Rightarrow(1)§: nech §a\bmod m=b\bmod m§.""", 0,
            c("""Zvyšky sa odčítajú a ostane §a-b=(q_a-q_b)m§, teda §m\mid(a-b)§."""),
            c("""Potom §a=b§, a teda aj §a\equiv b\pmod m§.""", """Z rovnakých zvyškov rovnosť neplynie: §3\bmod 5=8\bmod 5§, hoci §3\neq 8§ (Dombek)."""),
            c("""Plynie to z Tvrzení 6.4.""", Circle),
            given = """§(4)\Rightarrow(1)§: nech §a\bmod m=b\bmod m§."""),
    ),
    m("""Všetky štyri výroky sú ekvivalentné: kongruentné čísla majú rovnaký zvyšok po delení §m§ a naopak."""),
    listOf("(1) až (3): §m\\mid(a-b)\\Leftrightarrow(\\exists k\\in\\mathbb{Z})(a-b=km)§", "Delenie so zvyškom: §a-b=(q_a-q_b)m+r§, §r=a\\bmod m-b\\bmod m§", "§(1)\\Rightarrow(4)§: rozdiel zvyškov je násobok §m§ medzi §-(m-1)§ a §m-1§, teda §0§", "§(4)\\Rightarrow(1)§: §a-b=(q_a-q_b)m§"),
    minutes = 8,
    goals = listOf("(1) až (3) z definície", "Delenie so zvyškom", "§(1)\\Rightarrow(4)§", "§(4)\\Rightarrow(1)§"),
    warn = m("""Rovnaký zvyšok neznamená rovnaké čísla: §3\bmod 5=8\bmod 5§, hoci §3\neq 8§ (Dombek, 8. prednáška)."""),
)

internal val tcProofKongruenceVlastnosti = Proof(
    "dml-tc-p-kongruence-vlastnosti", "Kongruencia je reflexívna, symetrická a tranzitívna", "Skriptá, Tvrzení 6.5 a jeho dôkaz z definície; Spěvák, 7. prednáška; Dombek, 8. prednáška",
    null,
    null,
    m("""Každý bod rozpíšem z definície: §a\equiv b\pmod m§ znamená §m\mid(a-b)§, teda §a-b=km§ pre nejaké §k\in\mathbb{Z}§."""),
    listOf(
        r("""Reflexivita: prečo §a\equiv a\pmod m§?""", 0,
            c("""§a-a=0=0\cdot m§ a §0\in\mathbb{Z}§, teda §m\mid(a-a)§."""),
            c("""§m\mid a§, preto §a\equiv a§.""", """§m§ nemusí deliť §a§: §5\equiv 5\pmod 3§, hoci §3\nmid 5§. Delí rozdiel §a-a=0§."""),
            c("""Lebo §a=a§ a rovnosť je zvláštny prípad kongruencie.""", """To je tvrdenie, nie dôkaz. Odvoď ho z definície: §m\mid(a-a)§.""")),
        r("""Symetria: nech §a-b=km§. Čo §b-a§?""", 2,
            c("""§b-a=km§, teda §b\equiv a§.""", """Znamienko: §b-a=-(a-b)=-km§. Násobok je §-k§."""),
            c("""Symetria plynie z toho, že rovnosť je symetrická.""", """Kongruencia nie je rovnosť; odvoď ju z §a-b=km§."""),
            c("""§b-a=(-k)m§ a §-k\in\mathbb{Z}§, teda §b\equiv a\pmod m§."""),
            given = """Symetria: nech §a\equiv b\pmod m§, teda §a-b=km§, §k\in\mathbb{Z}§."""),
        r("""Tranzitivita: nech §a-b=km§ a §b-c=\ell m§. Čo §a-c§?""", 1,
            c("""§a-c=(a-b)(b-c)=k\ell m^2§.""", """Rozdiely sa sčítajú, nenásobia: §(a-b)+(b-c)=a-c§."""),
            c("""§a-c=(a-b)+(b-c)=(k+\ell)m§ a §k+\ell\in\mathbb{Z}§, teda §a\equiv c\pmod m§."""),
            c("""§a-b=km§ a §b-c=km§, preto §a-c=2km§.""", """Druhý predpoklad potrebuje iné písmeno, §\ell§. S tým istým §k§ by si tvrdil, že oba rozdiely sú rovnaké."""),
            given = """Tranzitivita: nech §a\equiv b§ a §b\equiv c\pmod m§, teda §a-b=km§ a §b-c=\ell m§ pre nejaké §k,\ell\in\mathbb{Z}§."""),
    ),
    m("""Kongruencia modulo §m§ je reflexívna, symetrická aj tranzitívna."""),
    listOf("Reflexivita: §a-a=0\\cdot m§", "Symetria: §b-a=(-k)m§, §-k§ celé", "Tranzitivita: druhé písmeno §\\ell§, §a-c=(k+\\ell)m§"),
    minutes = 5,
    goals = listOf("Reflexivita", "Symetria", "Tranzitivita"),
)

internal val tcProofOperace = Proof(
    "dml-tc-p-operace", "Kongruencia zachováva operácie", "Skriptá, Tvrzení 6.6 a jeho dôkaz; proseminár 21/22 k 7. cvičeniu",
    null,
    null,
    m("""Predpoklady prepíšem z definície: §a=b+km§ a §c=d+\ell m§ pre nejaké §k,\ell\in\mathbb{Z}§, pre každý predpoklad iné písmeno."""),
    listOf(
        r("""Súčet §a+c§:""", 0,
            c("""§a+c=(b+d)+(k+\ell)m§, teda §a+c\equiv b+d\pmod m§."""),
            c("""§a+c=(b+d)+km§.""", """Pripočítali sa oba násobky: §km+\ell m=(k+\ell)m§."""),
            c("""§a+c=b+d§, lebo §a=b§ a §c=d§.""", """Z kongruencie rovnosť neplynie: §12\equiv 7\pmod 5§, hoci §12\neq 7§. Pracuj s §a=b+km§, §c=d+\ell m§.""")),
        r("""Rozdiel §a-c§:""", 1,
            c("""§a-c=(b-d)+(k+\ell)m§.""", """Odčítajú sa aj násobky: §km-\ell m=(k-\ell)m§."""),
            c("""§a-c=(b-d)+(k-\ell)m§, teda §a-c\equiv b-d\pmod m§."""),
            c("""Rozdiel sa nezachová, lebo §b-d§ môže byť záporné.""", """Kongruencia záporné čísla pripúšťa; §k-\ell§ je celé, a to stačí.""")),
        r("""Súčin §ac§:""", 2,
            c("""§ac=bd+k\ell m§.""", """Roznásob všetky štyri členy §(b+km)(d+\ell m)§; chýbajú §b\ell m§ a §dkm§."""),
            c("""§ac=bd+(k+\ell)m§.""", """Pri súčine sa násobky nesčítajú; roznásob §(b+km)(d+\ell m)§."""),
            c("""§ac=(b+km)(d+\ell m)=bd+(b\ell+dk+k\ell m)m§, teda §ac\equiv bd\pmod m§.""")),
        r("""Mocnina indukciou podľa §n§: ZK §a^1\equiv b^1§ je predpoklad. Indukčný krok?""", 0,
            c("""§a^{n+1}=a\cdot a^n\stackrel{ZK}{\equiv}b\cdot a^n\stackrel{IP}{\equiv}b\cdot b^n=b^{n+1}§, oba kroky podľa bodu 3."""),
            c("""§a^{n+1}=a^n\cdot a\equiv b^n\cdot b§ podľa bodu 4.""", """Bod 4 práve dokazuješ. V kroku smieš použiť IP pre §n§ a bod 3, nie celý bod 4."""),
            c("""§a^{n+1}-b^{n+1}=(a-b)^{n+1}=(km)^{n+1}§.""", """Rozdiel mocnín nie je mocnina rozdielu: §a^2-b^2=(a-b)(a+b)§."""),
            given = """Mocnina indukciou podľa §n§: ZK §a^1\equiv b^1\pmod m§ je predpoklad; IP: §a^n\equiv b^n\pmod m§."""),
    ),
    m("""Kongruencia sa zachová pri súčte, rozdiele, súčine aj mocnine."""),
    listOf("Prepis §a=b+km§, §c=d+\\ell m§, dve rôzne písmená", "Súčet a rozdiel: §(k\\pm\\ell)m§", "Súčin: roznásobenie, z ďalších členov vytknuté §m§", "Mocnina indukciou: ZK, IP a bod 3"),
    minutes = 8,
    goals = listOf("Súčet", "Rozdiel", "Súčin", "Mocnina indukciou"),
)

internal val tcProofKraceni = Proof(
    "dml-tc-p-kraceni", "Krátenie v kongruencii", "Skriptá, Věta 6.13 a jej dôkaz",
    null,
    null,
    m("""Myšlienka: všetko vydelíme §d=\gcd(m,c)§; potom sú §\tfrac{m}{d}§ a §\tfrac{c}{d}§ nesúdeliteľné a činiteľ §\tfrac{c}{d}§ smieme vynechať (Lemma 6.2). Ľavá strana je §m|(ac-bc)§, teda §(\exists k\in\mathbb{Z})(c(a-b)=km)§; označme to §(*)§."""),
    listOf(
        r("""§(\Rightarrow)§: z §(*)§ treba §\tfrac{m}{d}|(a-b)§.""", 2,
            c("""§m|c(a-b)§, teda §m|(a-b)§.""", """Bez nesúdeliteľnosti neplatí: §15|3\cdot 5§, ale §15\nmid 5§."""),
            c("""§m|c(a-b)§, teda §m|c§ alebo §m|(a-b)§.""", """Tak funguje len prvočíslo (Euklidovo lemma): §12|3\cdot 4§, a §12§ nedelí ani §3§, ani §4§."""),
            c("""Vydelím §d§: §\tfrac{c}{d}(a-b)=k\tfrac{m}{d}§. §\gcd(\tfrac{m}{d},\tfrac{c}{d})=1§ (Lemma 6.2), preto §\tfrac{m}{d}|(a-b)§."""),
            given = """§(\Rightarrow)§:"""),
        r("""§(\Leftarrow)§: nech §a-b=k\tfrac{m}{d}§.""", 0,
            c("""Vynásobím §c§: §ac-bc=m\cdot\tfrac{kc}{d}§ a §\tfrac{c}{d}§ je celé, teda §m|(ac-bc)§."""),
            c("""Kongruenciu smiem vynásobiť, a modul ostane §\tfrac{m}{d}§.""", """Treba dôjsť k modulu §m§, nie §\tfrac{m}{d}§. Bez výpočtu je to len tvrdenie."""),
            c("""Platí to, lebo krátenie je ekvivalentná úprava.""", Circle),
            given = """§(\Leftarrow)§: nech §a-b=k\tfrac{m}{d}§."""),
        r("""Špeciálny prípad §\gcd(m,c)=1§?""", 1,
            c("""Krátiť §c§ smiem vždy, keď §c\neq 0§.""", """Nestačí: §3\cdot 8\equiv 3\cdot 3\pmod{15}§, ale §8 ≢ 3\pmod{15}§."""),
            c("""Ak §d=1§, je §\tfrac{m}{d}=m§ a krátenie aj rozširovanie §c§ je ekvivalentná úprava. Ak §d>1§, nie je: pre §a=\tfrac{m}{d}§, §b=0§ platí §ac=m\tfrac{c}{d}\equiv 0=bc§, ale §0<a<m§, teda §a ≢ b\pmod m§."""),
            c("""Vtedy sa modul zmení na §\tfrac{m}{c}§.""", """Modul sa delí §d=\gcd(m,c)=1§, teda ostane §m§.""")),
    ),
    m("""§ac\equiv bc\pmod{m}\Leftrightarrow a\equiv b\pmod{\tfrac{m}{d}}§; pri §\gcd(m,c)=1§ sa modul nemení."""),
    listOf("Prepis na §c(a-b)=km§", "§(\\Rightarrow)§ delenie §d§ a Lemma 6.2 (nesúdeliteľné §\\tfrac{m}{d},\\tfrac{c}{d}§)", "§(\\Leftarrow)§ vynásobenie §c§, §\\tfrac{c}{d}§ celé", "Pri §d=1§ modul ostáva; pri §d>1§ protipríklad §a=\\tfrac{m}{d}§, §b=0§"),
    minutes = 10,
    goals = listOf("""Krátenie: modul §\tfrac{m}{d}§""", "Rozšírenie: späť na §m§", """Prípad §\gcd(m,c)=1§"""),
)

internal val tcProofMfv = Proof(
    "dml-tc-p-mfv", "Malá Fermatova veta", "Skriptá, Věta 6.14 a jej dôkaz",
    null,
    null,
    m("""Myšlienka: čísla §a, 2a,\dots,(p-1)a§ dajú modulo §p§ tie isté zvyšky §1,\dots,p-1§, len v inom poradí; porovnáme súčiny oboch zoznamov a vykrátime §(p-1)!§. Uvažujme teda týchto §p-1§ čísel."""),
    listOf(
        r("Prečo žiadne z nich nie je deliteľné §p§?", 0,
            c("""§p§ je prvočíslo, §p\nmid a§ a §p\nmid j§ pre §1\leq j<p§, preto §p\nmid ja§."""),
            c("""Lebo §a<p§.""", """§a§ nemusí byť menšie než §p§; stačí, že nie je jeho násobok."""),
            c("""Podľa Eulerovej vety.""", """Eulerova veta o deliteľnosti čísel §ja§ nič nehovorí. Stačí, že prvočíslo §p§ nedelí ani §a§, ani §j§.""")),
        r("Prečo žiadne dve nie sú kongruentné modulo §p§?", 1,
            c("""Lebo §ja\neq ka§ v §\mathbb{Z}§.""", """Rôzne čísla môžu byť kongruentné: §3\neq 10§, ale §3\equiv 10\pmod 7§."""),
            c("""Sporom: z §ja\equiv ka\pmod p§ krátením §a§ (§\gcd(a,p)=1§, Věta 6.13) plynie §j\equiv k\pmod p§, nemožné pre §1\leq j<k\leq p-1§."""),
            c("""Krátim §a§, lebo §a\neq 0§.""", """Krátiť smieš len číslom nesúdeliteľným s modulom (Věta 6.13), nenulovosť nestačí.""")),
        r("Čo z toho plynie?", 2,
            c("""Ich súčin je §\equiv 0\pmod p§.""", """Ani jedno nie je násobok §p§ a §p§ je prvočíslo: súčin násobkom §p§ nie je."""),
            c("""§a^{p-1}=1§.""", """Tu platí len kongruencia, nie rovnosť (Varování 6.2)."""),
            c("""Ich zvyšky sú permutácia §1,\dots,p-1§, preto §a\cdot 2a\cdots(p-1)a\equiv 1\cdot 2\cdots(p-1)§, teda §a^{p-1}(p-1)!\equiv(p-1)!\pmod p§.""")),
        r("Posledný krok?", 0,
            c("""§\gcd((p-1)!,p)=1§, krátim §(p-1)!§: §a^{p-1}\equiv 1\pmod p§."""),
            c("""Vydelím obe strany §(p-1)!§.""", """Krátenie treba odôvodniť podmienkou §\gcd((p-1)!,p)=1§. V kongruencii sa nedelí, kráti sa (Věta 6.13)."""),
            c("""Krátim §(p-1)!§, lebo je nenulové.""", """Nenulovosť nestačí, treba §\gcd((p-1)!,p)=1§ (Věta 6.13). Platí, lebo §p§ je prvočíslo väčšie než všetky činitele.""")),
    ),
    m("""Pre prvočíslo §p§ a §p\nmid a§ platí §a^{p-1}\equiv 1\pmod{p}§."""),
    listOf("§p-1§ čísel §ja§, žiadne nie je násobok §p§", "Žiadne dve nie sú kongruentné: krátenie §a§ (Věta 6.13)", "Zvyšky sú permutácia §1,\\dots,p-1§: súčin", "Krátenie §(p-1)!§, lebo §\\gcd((p-1)!,p)=1§"),
    minutes = 12,
    goals = listOf("Žiadne nie je násobok §p§", "Rôzne zvyšky", "Súčin všetkých čísel", "Krátenie §(p-1)!§"),
)

internal val tcProofCvoz = Proof(
    "dml-tc-p-cvoz", "Čínska veta o zvyškoch", "Skriptá, Věta 6.19 a jej dôkaz",
    null,
    null,
    m("""Myšlienka: pre každú kongruenciu zostrojíme člen, ktorý je §\equiv 1§ v jej module a §\equiv 0§ vo všetkých ostatných; súčet týchto členov vynásobených §a_i§ potom spĺňa všetky kongruencie naraz. Pre každé §i§ položme §M_i:=\tfrac{M}{m_i}§, súčin všetkých modulov okrem §i§-tého."""),
    listOf(
        r("""Prečo má §M_iX_i\equiv 1\pmod{m_i}§ riešenie §X_i§?""", 1,
            c("""Lebo §M_i>m_i§.""", """Veľkosť nerozhoduje; §6x\equiv 1\pmod 4§ riešenie nemá."""),
            c("""Moduly sú navzájom nesúdeliteľné, preto §\gcd(M_i,m_i)=1§ a riešenie existuje (Věta 6.18)."""),
            c("""Inverzia existuje vždy, keď §m_i\geq 2§.""", """Len pri §\gcd(M_i,m_i)=1§ (Věta 6.12). Tu ho zaručí nesúdeliteľnosť modulov.""")),
        r("""Prečo §x=a_1M_1X_1+\dots+a_NM_NX_N§ spĺňa §j§-tu kongruenciu?""", 0,
            c("""Pre §i\neq j§ je §m_j|M_i§, teda §M_iX_i\equiv 0\pmod{m_j}§; a §M_jX_j\equiv 1\pmod{m_j}§. Ostane §x\equiv a_j§."""),
            c("""Lebo §M_iX_i\equiv 1\pmod{m_j}§ pre každé §i§.""", """Pre §i\neq j§ je §M_iX_i§ násobok §m_j§, teda §M_iX_i\equiv 0\pmod{m_j}§, nie §1§."""),
            c("""Podľa zovšeobecnenej ČVOZ (Věta 6.20).""", """Dokazuješ ČVOZ; odvolať sa na všeobecnejšiu vetu nesmieš, a skriptá ju ani nedokazujú.""")),
        r("Jednoznačnosť modulo §M§?", 2,
            c("""§x-y§ je násobok každého §m_j§, preto §x=y§.""", """Plynie len §x\equiv y\pmod M§: §23§ aj §53§ riešia Příklad 6.19."""),
            c("""Násobok každého modulu je vždy násobok ich súčinu.""", """Bez nesúdeliteľnosti neplatí: §12§ je násobok §4§ aj §6§, ale nie §24§."""),
            c("""Ak §x,y§ riešia sústavu, §m_j|(x-y)§ pre každé §j§; moduly sú navzájom nesúdeliteľné, preto §M|(x-y)§, teda §x\equiv y\pmod M§.""")),
    ),
    m("""Riešenie existuje, §x\equiv\sum a_iM_iX_i\pmod M§, a v §\mathbb{Z}_M§ je jediné."""),
    listOf("§M_i=M/m_i§ a §\\gcd(M_i,m_i)=1§", "§X_i§ z §M_iX_i\\equiv 1\\pmod{m_i}§", "Overenie §j§-tej kongruencie: §M_iX_i\\equiv 0§ pre §i\\neq j§", "Jednoznačnosť: §M|(x-y)§ vďaka nesúdeliteľnosti"),
    minutes = 12,
    goals = listOf("Existencia §X_i§", "Overenie §j§-tej kongruencie", "Jednoznačnosť modulo §M§"),
)

internal val tcProofs = listOf(tcProofDeleni, tcProofLdr, tcProofKongruenceEkv, tcProofKongruenceVlastnosti, tcProofOperace, tcProofInverze, tcProofKraceni, tcProofMfv, tcProofCvoz)

internal val tcThmDeleni = Theorem("dml-tc-deleni", "Delenie so zvyškom", "Skriptá, Věta 6.2",
    wording(
        listOf(p("""Nechť §a \in \mathbb{Z}, d \in \mathbb{N}§.""")),
        listOf(p("""Pak existují **jednoznačně určená** čísla §q, r \in \mathbb{Z}§ taková, že"""), b("""a = qd + r \quad\softbreak[1]\wedge\quad 0 \leq r < d.""")),
        core = """a = qd + r \;\wedge\; 0 \leq r < d""",
        parts = listOf(
            part("Existencia", """(\exists q,r\in\mathbb{Z})\softbreak(a=qd+r\wedge 0\leq r<d)""", 0),
            part("Jednoznačnosť", """\begin{aligned}a&=q_1d+r_1=q_2d+r_2,\\ 0&\leq r_1,r_2<d\\ &\Rightarrow\ q_1=q_2\wedge r_1=r_2\end{aligned}""", 3),
        ),
    ),
    "Každé celé číslo sa dá rozdeliť na násobok §d§ a zvyšok z §\\{0,\\dots,d-1\\}§, a to jediným spôsobom. Aj záporné: §-17=(-4)\\cdot 5+3§.",
    proved(tcProofDeleni), note = "Dôkaz zo skrípt je zároveň cvičenie na slabú indukciu.", short = "o delení so zvyškom")

internal val tcThmLemma61 = Theorem("dml-tc-lemma61", "Príprava pre Euklidov algoritmus", "Skriptá, Lemma 6.1",
    wording(
        listOf(p("""Buďte §a,b \in \mathbb{Z}§.""")),
        listOf(
            i("""Platí §\gcd(a,b)=\gcd(b,a)§ a §\lcm(a,b)=\lcm(b,a)§."""),
            i("""Platí §\gcd(a,b)=\gcd(|a|,|b|)§ a §\lcm(a,b)=\lcm(|a|,|b|)§."""),
            i("""Pro libovolné §c \in \mathbb{Z}§ platí §§\gcd{(a+cb, b)}\softbreak[1]=\gcd{(a,b)}.§§"""),
        ),
    ),
    "Poradie ani znamienka §\\gcd§ nemenia, a ani pripočítanie násobku jedného čísla k druhému. Bod 3 je celý Euklidov algoritmus: §\\gcd(a,b)=\\gcd(b,a\\bmod b)§.",
    shortProof("""1. Je zřejmé z definice. 2. Plyne z faktu, že čísla §a,b§ a čísla §|a|,|b|§ mají stejné množiny společných dělitelů i společných násobků. 3. Dělí-li §e§ obě čísla §a§ a §b§, dělí i §a+cb§ (Tvrzení 6.1). Naopak dělí-li §e§ čísla §a+cb§ a §b§, dělí i jejich rozdíl §(a+cb)+(-c)b=a§. Společní dělitelé §a+cb,b§ jsou tedy stejná čísla jako společní dělitelé §a,b§, proto §\gcd(a+cb,b)=\gcd(a,b)§.""", "Skriptá, dôkaz Lemmatu 6.1"),
    note = "Bod 3 je krok Euklidovho algoritmu: §a\\bmod b=a-qb§ je §a+cb§ pre §c=-q§.", short = "o príprave pre Euklidov algoritmus")

internal val tcThmEa = Theorem("dml-tc-ea", "Euklidov algoritmus", "Skriptá, Věta 6.3",
    wording(
        listOf(p("""Nechť §a, b\in\mathbb{Z}§ splňují §a \geq b > 0§.""")),
        listOf(
            p("""**Eukleidův algoritmus** má na vstupu §(a,b)§, jeho výstupem bude §r_k = \gcd{(a,b)}§."""),
            p("""Definujme §(r_n)_{n\geq 1}§, posloupnost zbytků po dělení, a to rekurentně:"""),
            b("""\begin{aligned}r_1&=a,\\ r_2&=b,\\ \forall n\geq 3:\softbreak\ r_{n}&=r_{n-2} \bmod r_{n-1}.\end{aligned}"""),
            p("""Tato §(r_n)_{n\geq 1}§ je klesající a existuje §k\in\mathbb{N}§ takové, že §r_{k+1}=0§."""),
            p("""Označíme-li jako §k§ nejmenší takové, že §r_{k+1}=0§, pak platí §r_k = \gcd{(a,b)}§. Tedy poslední nenulový člen §(r_n)_{n\geq 1}§ je hledaným §\gcd(a,b)§."""),
        ),
        core = """\gcd(a,b)=r_k,\quad r_{k+1}=0""",
        rest = listOf(
            p("""Je-li §b=0§, rovnou platí §\gcd(a,0)=a§."""),
            p("""Je-li §a§ nebo §b§ záporné nebo neplatí-li §a \geq b§, situaci převedeme na předchozí případ vztahem §\gcd(a,b) = \gcd(|a|,|b|) = \gcd(|b|,|a|)§."""),
        ),
    ),
    "Zvyšky klesajú, kým nepríde §0§; posledný nenulový je §\\gcd§. Každý krok §\\gcd§ nemení (Lemma 6.1, bod 3).",
    shortProof("""Zbytky jsou nezáporné a ostře klesají (Věta 6.2), proto po konečně mnoha krocích dojdeme k §r_{k+1}=0§. V každém kroku je §r_{n-2}=q_{n-1}r_{n-1}+r_n§, takže podle Lemmatu 6.1 platí §\gcd(r_{n-2},r_{n-1})=\gcd(r_{n-1},r_n)§. Řetězem těchto rovností §\gcd(a,b)=\gcd(r_1,r_2)=\dots=\gcd(r_k,0)=r_k§.""", "Skriptá, dôkaz Věty 6.3"),
    note = "Pozor na vetu „Je-li §b=0§, rovnou platí §\\gcd(a,0)=a§“: skriptá tu mlčky berú §a\\geq 0§. Pre záporné §a§ je §\\gcd(a,0)=|a|§, napr. §\\gcd(-12,0)=12§. Spěvák (6. prednáška): „GCDčko Ačka a nuly [je] rovný absolutní hodnotě A.“", short = "o Euklidovom algoritme")

internal val tcThmBezout = Theorem("dml-tc-bezout", "Bézoutova rovnosť", "Skriptá, Věta 6.4",
    wording(
        listOf(p("""Buďte §a,b \in \mathbb{Z}§.""")),
        listOf(p("""Pak"""), b("""(\exists m,n\in \mathbb{Z})\softbreak(\gcd(a,b) = m\cdot a + n\cdot b).""")),
        rest = listOf(p("""Každou takovou dvojici §m,n§ nazýváme **Bézoutovy koeficienty** a celý vztah výše nazýváme **Bézoutova rovnost (identita)**.""")),
    ),
    "Euklidov algoritmus dá len §\\gcd§. Dombek chce viac: zapísať §\\gcd§ ako celočíselnú kombináciu §a§ a §b§, lebo tieto koeficienty potom využijeme pri diofantických rovniciach. Bézout hovorí, že sa to dá vždy, a jednu dvojicu nájde REA (Veta 6.5). Koeficienty nie sú jednoznačné.",
    shortProof("""Existence vyplyne z Věty 6.5: pro §a\geq b>0§ je výstupem REA §\gcd(a,b)§ a dvojice koeficientů. Pro obecná §a,b\in\mathbb{Z}\setminus\{0\}§ aplikujeme REA na §|a|,|b|§ (seřazené). V případě §a=0§ nebo §b=0§ platí triviálně.""", "Skriptá, dôkaz Věty 6.4"),
    note = "Na písomke: §\\gcd§ a Bézoutove koeficienty (stránka predmetu, 2. zápočtovka). Pozor na poradie §a,b§ v tabuľke.", short = "o Bézoutovej rovnosti")

internal val tcThmRea = Theorem("dml-tc-rea-veta", "Rozšírený Euklidov algoritmus", "Skriptá, Věta 6.5",
    wording(
        listOf(p("""Nechť §a, b\in\mathbb{Z}§ splňují §a \geq b > 0§.""")),
        listOf(
            p("""**Rozšířený Eukleidův algoritmus** (zkráceně REA nebo EEA) má na vstupu §(a,b)§, jeho výstupem bude §r_k = \gcd{(a,b)}§ a dvojice Bézoutových koeficientů §(\alpha_k, \beta_k)§."""),
            p("""Definujme rekurentně tři posloupnosti, §(r_n)_{n\geq 1}§ stejně jako v EA a dále posloupnosti koeficientů §(\alpha_n)_{n\geq 1}§, §(\beta_n)_{n\geq 1}§ následovně:"""),
            b("""\begin{aligned}r_1&=a, & \alpha_1&=1, & \beta_1&=0,\\ r_2&=b, & \alpha_2&=0, & \beta_2&=1,\end{aligned}"""),
            p("""§\forall n\geq 3§: §r_{n} = r_{n-2} \bmod r_{n-1}§ (podíl §\lfloor\tfrac{r_{n-2}}{r_{n-1}}\rfloor§ ozn. §q_{n-1}§),"""),
            b("""\begin{aligned}r_{n}&=r_{n-2}-q_{n-1}r_{n-1},\\ \alpha_{n}&=\alpha_{n-2}-q_{n-1}\alpha_{n-1},\\ \beta_{n}&=\beta_{n-2}-q_{n-1}\beta_{n-1}.\end{aligned}"""),
            p("""Pak pro každé §n\leq k+1§ platí §r_n = \alpha_n\cdot a + \beta_n\cdot b§ a speciálně pro §n=k§ máme"""),
            b("""\gcd(a,b) = \alpha_k\cdot a + \beta_k\cdot b."""),
        ),
        core = """\text{každý riadok:}\ \softbreak r_n=\alpha_n\cdot a+\beta_n\cdot b""",
    ),
    "Každý riadok tabuľky REA je rovnosť §r_n=\\alpha_n a+\\beta_n b§; nový riadok je predpredošlý mínus §q§-násobok predošlého, ako krok GEM. Riadok nad nulou dáva §\\gcd§ aj koeficienty.",
    shortProof("""Silnou indukcí podle §n§. Pro §n\in\{1,2\}§ vztah platí: §a=1\cdot a+0\cdot b§ a §b=0\cdot a+1\cdot b§. V indukčním kroku předpokládejme vztah pro §r_{n-2}§ a §r_{n-1}§; odečtením §q_{n-1}§-násobku druhé rovnosti od první dostaneme §r_n=r_{n-2}-q_{n-1}r_{n-1}=(\alpha_{n-2}-q_{n-1}\alpha_{n-1})a+(\beta_{n-2}-q_{n-1}\beta_{n-1})b=\alpha_n a+\beta_n b§.""", "Skriptá, dôkaz Věty 6.5"),
    note = "Pozor na poradie §a,b§: §\\alpha§ patrí k §a§, §\\beta§ k §b§. Spěvák (6. prednáška): „Setkáte se s ním na cvičení, setkáte se s ním nejspíš i ve zkouškový písemce.“", short = "o rozšírenom Euklidovom algoritme")

internal val tcThmGcdLcm = Theorem("dml-tc-gcd-lcm", "Vzťah gcd a lcm", "Skriptá, Tvrzení 6.3",
    wording(listOf(p("""Nechť §a,b\in\mathbb{Z}§.""")), listOf(p("""Pak platí"""), b("""\gcd(a,b)\cdot\lcm(a,b)\softbreak[1]=|a|\cdot|b|."""))),
    "Súčin §\\gcd§ a §\\lcm§ je súčin absolútnych hodnôt, takže §\\lcm§ spočítaš z §\\gcd§ bez rozkladu na prvočísla.",
    shortProof("""Je-li §a§ nebo §b§ rovno nule nebo jedné, stačí dosadit. Znaménka na hodnoty výrazů nemají vliv, stačí tedy kladná §a,b§. Z rozkladů §a=\prod_{i=1}^k p_i^{\alpha_i}§, §b=\prod_{i=1}^k p_i^{\beta_i}§ je §\gcd(a,b)\cdot\lcm(a,b)=\prod_{i=1}^k p_i^{\min(\alpha_i,\beta_i)+\max(\alpha_i,\beta_i)}=\prod_{i=1}^k p_i^{\alpha_i+\beta_i}=a\cdot b§.""", "Skriptá, dôkaz Tvrzení 6.3"),
    short = "o vzťahu gcd a lcm")

internal val tcThmLdr = Theorem(
    "dml-tc-ldr-res",
    "Riešiteľnosť LDR",
    "Skriptá, Věta 6.7",
    wording(
        emptyList(),
        listOf(p("""Lineární diofantická rovnice §ax + by = c§ má alespoň jedno řešení právě tehdy, když §\gcd(a, b) | c§.""")),
        core = """\exists x,y\in\mathbb{Z}:\ ax+by=c\softbreak[1]\Leftrightarrow\quad\gcd(a,b)\mid c""",
        parts = listOf(part("(⇒)", """ax+by=c\softbreak[1]\Rightarrow\ \gcd(a,b)\mid c""", 0), part("(⇐)", """\gcd(a,b)\mid c\softbreak[1]\Rightarrow\ \exists x,y\in\mathbb{Z}:\ ax+by=c""", 1)),
    ),
    "Riešenie je práve vtedy, keď §\\gcd§ delí pravú stranu. Pre §(a,b)\\neq(0,0)§ ho nájdeš z Bézouta vynásobeného §c/\\gcd§. Všetky ostatné dostaneš pripočítaním násobkov §(b/\\gcd,-a/\\gcd)§.",
    proved(tcProofLdr),
    note = "Na písomke: riešenie lineárnej diofantickej rovnice (stránka predmetu).",
    short = "o riešeniach lineárnej diofantickej rovnice"
)

internal val tcThmKongruenceEkv = Theorem(
    "dml-tc-kongruence-ekv",
    "Kongruencia a rovnaké zvyšky",
    "Skriptá, Tvrzení 6.4",
    wording(
        listOf(p("""Nechť §a,b,m\in \mathbb{Z}§ a §m \geq 2§.""")),
        listOf(p("""Následující výroky jsou ekvivalentní:"""), i("""§a \equiv b \pmod m§,"""), i("""§a-b \equiv 0 \pmod m§,"""), i("""§(\exists k\in\mathbb{Z})(a = b + km)§,"""), i("""§a \bmod m = b \bmod m§, tj. §a§ a §b§ mají ==stejné== zbytky po dělení §m§.""")),
        // each line keeps its number from the skriptá's list, (1) to (4): the proof and its questions name them
        core = """\begin{aligned}&a\equiv b\pmod m&&\text{(1)}\\\Leftrightarrow\ &a-b\equiv0\pmod m&&\text{(2)}\\\Leftrightarrow\ &\exists k\in\mathbb{Z}:\ a=b+km&&\text{(3)}\\\Leftrightarrow\ &a\bmod m=b\bmod m&&\text{(4)}\end{aligned}""",
        parts = listOf(part("Body (1) až (3)", """\begin{aligned}&a\equiv b\pmod m&&\text{(1)}\\\Leftrightarrow\ &a-b\equiv0\pmod m&&\text{(2)}\\\Leftrightarrow\ &\exists k\in\mathbb{Z}:\ a=b+km&&\text{(3)}\end{aligned}""", 0), part("Bod (4)", """\begin{aligned}&a\equiv b\pmod m&&\text{(1)}\\\Leftrightarrow\ &a\bmod m=b\bmod m&&\text{(4)}\end{aligned}""", 1)),
    ),
    "Byť kongruentné modulo §m§ znamená líšiť sa o celý násobok §m§, a to je presne to isté ako mať rovnaký zvyšok po delení §m§. Myšlienka dôkazu: obe čísla vydeľ §m§ so zvyškom; rozdiel zvyškov je násobok §m§ medzi §-(m-1)§ a §m-1§, teda §0§.",
    proved(tcProofKongruenceEkv),
    note = "Dokázali ho Dombek (8. prednáška) aj Spěvák (7. prednáška), každý inou cestou.",
    short = "o kongruencii a zvyškoch"
)

internal val tcThmOperace = Theorem(
    "dml-tc-operace",
    "Kongruencia zachováva operácie",
    "Skriptá, Tvrzení 6.6",
    wording(
        listOf(p("""Nechť §a,b,c,d,m\in \mathbb{Z}§ a §m \geq 2§. Splňují-li současně"""), b("""a\equiv b \pmod m"""), b("""\wedge\ c\equiv d \pmod m,""")),
        listOf(p("""pak platí následující:"""), i("""§a+c \equiv b+d \pmod m§,"""), i("""§a-c \equiv b-d \pmod m§,"""), i("""§ac \equiv bd \pmod m§,"""), i("""§(\forall k\in\mathbb{N})\big(a^k \equiv b^k \pmod m\big)§.""")),
        core = """\begin{aligned}&a\equiv b\pmod m,\\&c\equiv d\pmod m\ \Rightarrow\\&a\pm c\equiv b\pm d,\ \ ac\equiv bd,\\&a^k\equiv b^k\ (k\in\mathbb{N})\end{aligned}""",
        parts = listOf(part("Súčet", """a+c\equiv b+d\pmod m""", 0), part("Rozdiel", """a-c\equiv b-d\pmod m""", 1), part("Súčin", """ac\equiv bd\pmod m""", 2), part("Mocnina", """(\forall k\in\mathbb{N})\softbreak(a^k\equiv b^k\pmod m)""", 3)),
    ),
    "Sčítanec, menšiteľ, činiteľ aj základ mocniny smieš nahradiť kongruentným číslom a výsledok ostane kongruentný. Myšlienka dôkazu: §a=b+km§, §c=d+\\ell m§ a po roznásobení má každý ďalší člen činiteľ §m§; mocninu dostaneš indukciou z násobenia. Deliť ani redukovať exponent zatiaľ nesmieš.",
    proved(tcProofOperace),
    note = "Dombek (8. prednáška): veľké čísla nahraď malými kongruentnými, ušetríš prácu aj chyby. Exponent nie: §2^7=128\\equiv 3\\not\\equiv 4=2^2\\pmod 5§. Krátiť tiež nie: §3\\cdot 8\\equiv 3\\cdot 3\\pmod{15}§, ale §8\\not\\equiv 3\\pmod{15}§.",
    short = "o počítaní s kongruenciami"
)

internal val tcThmInverze = Theorem(
    "dml-tc-inverze",
    "Existencia inverzie",
    "Skriptá, Věta 6.12",
    wording(
        listOf(p("""Nechť §m\geq 2§ a §a \in \mathbb{Z}_m§.""")),
        listOf(p("""V §\mathbb{Z}_m§ existuje multiplikativní inverze k §a§ právě tehdy, když §\gcd(a,m)=1§.""")),
        core = """\exists x\in\mathbb{Z}_m:\ a\cdot_{m} x=1\softbreak[1]\Leftrightarrow\ \gcd(a,m)=1,\ \softbreak[2]x\text{ je jediné}""",
        parts = listOf(part("Existencia", """\exists x\in\mathbb{Z}_m:\ a\cdot_{m} x=1\softbreak[1]\Leftrightarrow\ \gcd(a,m)=1""", 0), part("Jednoznačnosť", """a\cdot_{m} x=a\cdot_{m} y=1\softbreak[1]\Rightarrow x=y""", 2)),
        rest = listOf(p("""Pokud tato inverze existuje, pak je jediná.""")),
    ),
    "§a^{-1}§ existuje práve vtedy, keď má §ax\\equiv 1\\pmod m§ riešenie, teda keď má lineárna diofantická rovnica §ax+my=1§ riešenie, a to je podľa Vety 6.7 práve pri §\\gcd(a,m)=1§. Všetky riešenia sú §x_0+km§ a do §\\{0,\\dots,m-1\\}§ padne práve jedno.",
    proved(tcProofInverze),
    note = "Na písomke: „nalezení multiplikativní inverze modulo m“; v rokoch 2024 a 2025 s hľadaním najmenšieho modulu.",
    short = "o existencii multiplikatívnej inverzie"
)

internal val tcThmInverzeVsetky = Theorem(
    "dml-tc-inverze-vsetky",
    "Kedy majú inverziu všetky nenulové prvky",
    "Skriptá, Důsledek 6.2",
    wording(
        emptyList(),
        listOf(p("""Je zřejmé, že §0§ multiplikativní inverzi mít nikdy nemůže, navíc právě všechny prvky §\mathbb{Z}_m§ soudělné s §m§ multiplikativní inverzi nemají. Obecně platí:"""), i("""Pokud §m§ není prvočíslo (tedy je složené), existuje jeho menší dělitel různý od §0§ i od §1§. Tedy v §\mathbb{Z}_m§ existuje nenulový prvek soudělný s §m§, ten nemá inverzi."""), i("""Je-li §m§ prvočíslo, pak každé z čísel §1, 2, \dots, m-1§ je s §m§ nesoudělné (opak by znamenal existenci netriviálního dělitele §m§). Tedy ==každé== nenulové §a \in \mathbb{Z}_m§ má multiplikativní inverzi §\bmod{m}§."""), i("""V 1. i 2. víme (díky důkazu Věty 6.12), jak ji nalézt! Pomocí REA aplikovaného na diofantickou rovnici §ax+my=1§ (pokud nutno, k partikulárnímu řešení §x_0§ přičteme násobek modula, abychom dostali prvek §\mathbb{Z}_m§).""")),
        core = """m\text{ prvočíslo}\softbreak[1]\ \Leftrightarrow\ \forall a\in\mathbb{Z}_m\setminus\{0\}:\ \softbreak[2]\exists a^{-1}\in\mathbb{Z}_m""",
    ),
    "Pri prvočíselnom module má inverziu každé nenulové číslo, preto LA1 počíta v §\\mathbb{Z}_p§. Pri zloženom §m§ ju nemá napríklad žiadny jeho deliteľ §d§ s §1<d<m§.",
    shortProof("""Pokud §m§ není prvočíslo (tedy je složené), existuje jeho menší dělitel různý od §0§ i od §1§. Tedy v §\mathbb{Z}_m§ existuje nenulový prvek soudělný s §m§, ten nemá inverzi. Je-li §m§ prvočíslo, pak každé z čísel §1,2,\dots,m-1§ je s §m§ nesoudělné (opak by znamenal existenci netriviálního dělitele §m§). Tedy každé nenulové §a\in\mathbb{Z}_m§ má multiplikativní inverzi §\bmod m§.""", "Skriptá, Důsledek 6.2"),
    note = "Dombek (8. prednáška): v §\\mathbb{Z}_2§, §\\mathbb{Z}_3§, §\\mathbb{Z}_5§ či §\\mathbb{Z}_7§ hľadáš inverziu skúšaním; keď dá LA1 na písomke §\\mathbb{Z}_{23}§, zachráni ťa Euklidov algoritmus.",
    short = "o inverziách pri prvočíselnom module"
)

internal val tcThmKraceni = Theorem(
    "dml-tc-kraceni",
    "Krátenie v kongruencii",
    "Skriptá, Věta 6.13",
    wording(
        listOf(p("""Nechť §a,b,c \in \mathbb{Z}§ a §m \in \mathbb{N}, m \geq 2§, označme §d=\gcd(m,c)§.""")),
        listOf(p("""Pak platí ekvivalence:"""), b("""ac \equiv bc \pmod{m} \quad\softbreak[1]\Leftrightarrow\quad a \equiv b \pmod{\tfrac{m}{d}}.""")),
        core = """ac\equiv bc\pmod m\softbreak[1]\Leftrightarrow a\equiv b\softbreak[2]\pmod{\tfrac{m}{\gcd(m,c)}}""",
        parts = listOf(part("(⇒)", """ac\equiv bc\pmod m\softbreak[1]\Rightarrow a\equiv b\pmod{\tfrac{m}{\gcd(m,c)}}""", 0), part("(⇐)", """a\equiv b\pmod{\tfrac{m}{\gcd(m,c)}}\softbreak[1]\Rightarrow ac\equiv bc\pmod m""", 1), part("Bez zmeny modulu", """\gcd(m,c)=1\softbreak[1]\Leftrightarrow\text{krátiť }c\text{ smiem}\softbreak[2]\text{bez zmeny modulu}""", 2)),
        rest = listOf(p("""Speciálně, pro §c\in\mathbb{Z}§ platí §\gcd(m,c)=1§ právě tehdy, když v kongruenci §(\bmod{m})§ je krácení i rozšiřování číslem §c§ ekvivalentní úprava (beze změny modula).""")),
    ),
    "Krátiť číslom §c§ smieš, ale modul sa vydelí §\\gcd(m,c)§. Bez toho stratíš riešenia: §3x\\equiv 9\\pmod{15}§ má aj §x=8§.",
    proved(tcProofKraceni),
    note = "Ukážková otázka ústnej skúšky: „Vyslovte větu o krácení v modulu a s využitím bodu výše ji dokažte.“ Bod výše je Lemma 6.2 (stránka predmetu).",
    short = "o krátení v kongruencii"
)

internal val tcThmMfv = Theorem(
    "dml-tc-mfv",
    "Malá Fermatova veta",
    "Skriptá, Věta 6.14",
    wording(
        listOf(p("""Buď §p§ prvočíslo a §a \in \mathbb{N}§ takové přirozené číslo, které není násobkem §p§ (tedy §\gcd(a,p)=1§).""")),
        listOf(p("""Potom platí kongruence"""), b("""a^{p-1} \equiv 1 \pmod{p}.""")),
        core = """p\text{ prvočíslo},\ p\nmid a\softbreak[1]\Rightarrow a^{p-1}\equiv1\pmod p""",
    ),
    "Modulo prvočíslo §p§ sa exponent smie redukovať modulo §p-1§, ak základ nie je násobok §p§.",
    proved(tcProofMfv),
    note = "Ústna skúška chce vedieť jej znenie (stránka predmetu: „věty Fermatovu, Eulerovu, ČVOZ“). Spěvák ju na 8. prednáške odvodil z Eulerovej vety (§\\varphi(p)=p-1§) a dodal tvar §a^p\\equiv a\\pmod p§ pre každé celé §a§; skriptá ju dokazujú priamo, tento dôkaz je tu.",
    short = "o mocninách modulo prvočíslo"
)

internal val tcThmEv = Theorem(
    "dml-tc-ev",
    "Eulerova veta",
    "Skriptá, Věta 6.15",
    wording(
        listOf(p("""Nechť §m \in \mathbb{N}§, §m\geq 2§ a §a \in \mathbb{N}§ je číslo nesoudělné s §m§.""")),
        listOf(p("""Potom platí kongruence §§a^{\varphi(m)} \equiv 1 \pmod{m}§§, kde §\varphi(m)§ je Eulerova funkce.""")),
        core = """a^{\varphi(m)}\equiv1\pmod m""",
    ),
    "Pri §\\gcd(a,m)=1§ sa exponent redukuje modulo §\\varphi(m)§. Pre prvočíslo je §\\varphi(p)=p-1§ a je to MFV. Pri súdeliteľnom základe veta neplatí.",
    shortProof("""Nech §z_1,\dots,z_{\varphi(m)}§ sú zvyšky po delení §m§ nesúdeliteľné s §m§. Čísla §az_1,\dots,az_{\varphi(m)}§ sú tiež nesúdeliteľné s §m§ a navzájom nekongruentné, lebo z §az_i\equiv az_j§ smieme krátiť §a§ bez zmeny modulu (§\gcd(a,m)=1§, Věta 6.13). Ich zvyšky sú preto tie isté §z_i§ v inom poradí, a teda §a^{\varphi(m)}z_1\cdots z_{\varphi(m)}\equiv z_1\cdots z_{\varphi(m)}\pmod m§. Súčin §z_1\cdots z_{\varphi(m)}§ je nesúdeliteľný s §m§, vykrátime ho a ostane §a^{\varphi(m)}\equiv 1\pmod m§.""", "Spěvák, 8. prednáška; skriptá dôkaz vynechávajú a nevyžadujú"),
    note = "Skriptá dôkaz vynechávajú a nevyžadujú; Spěvák ho na 8. prednáške ukázal a malú Fermatovu vetu z neho odvodil ako dôsledok. Znenie treba vedieť (ústna skúška).",
    short = "o mocninách a Eulerovej funkcii"
)

internal val tcThmLkon = Theorem(
    "dml-tc-lkon-res",
    "Riešenie lineárnej kongruencie",
    "Skriptá, Věta 6.17",
    wording(
        listOf(p("""Buďte §a, b, m \in\mathbb{Z}§ a §m \geq 2§.""")),
        listOf(p("""Lineární kongruence §a\cdot x \equiv b \pmod{m}§ má řešení právě tehdy, když §\gcd(a, m) | b§. §x_0\in\mathbb{Z}§ ji řeší právě tehdy, když existuje §y_0 \in \mathbb{Z}§ takové, že dvojice §(x_0, y_0)§ řeší diofantickou rovnici §ax_0 + my_0 = b§. Všechna řešení lineární kongruence v §\mathbb{Z}§ jsou pak ve tvaru"""), b("""x \equiv x_0 + k \cdot \frac{m}{\gcd(a,m)}\softbreak[1] \pmod{m}, \quad\softbreak[1] k\in\mathbb{Z}.""")),
        core = """\begin{aligned}&d=\gcd(a,m),\\&\exists x:\ ax\equiv b\pmod m\\\Leftrightarrow\ &d\mid b,\\&x=x_0+k\frac md,\quad k\in\mathbb{Z}\end{aligned}""",
    ),
    "Riešenie je práve pri §\\gcd(a,m)|b§; riešenia sa opakujú s krokom §m/\\gcd(a,m)§, v §\\mathbb{Z}_m§ ich je §\\gcd(a,m)§.",
    shortProof("""Víme, že §ax\equiv b\pmod m§ je ekvivalentní §m\mid(ax-b)§, tedy že pro nějaké §j\in\mathbb{Z}§ platí §ax-b=jm§. To bude ovšem platit právě tehdy, když rovnice §a\cdot x+m\cdot y=b§ má nějaké celočíselné řešení §(x,y)§. K tomu je díky Větě 6.7 nutné a stačí, aby §\gcd(a,m)\mid b§. Nás ovšem zajímá pouze složka §x§, takže všechna řešení lineární kongruence §ax\equiv b\pmod m§ mají tvar §x=x_0+k\cdot\frac m{\gcd(a,m)}§.""", "Skriptá, dôkaz Věty 6.17"),
    note = "Na písomke: „řešení (bez použití hrubé síly) lineárních kongruencí“ (stránka predmetu).",
    short = "o riešení lineárnej kongruencie"
)

internal val tcThmCvoz = Theorem(
    "dml-tc-cvoz",
    "Čínska veta o zvyškoch",
    "Skriptá, Věta 6.19",
    wording(
        listOf(p("""Uvažujme soustavu lineárních kongruencí"""), b("""\begin{aligned}x &\equiv a_1 \pmod{m_1},\\x &\equiv a_2 \pmod{m_2},\\&\ \ \vdots\\x &\equiv a_N \pmod{m_N},\end{aligned}"""), p("""kde §m_1,m_2,\dots,m_N \geq 2§ jsou ==navzájem nesoudělná==, tedy §\gcd(m_i,m_j) = 1§ pro každá §i\neq j§.""")),
        listOf(p("""Řešení této soustavy vždy existuje a všechna řešení jsou kongruentní modulo §M§ (tedy v §\mathbb{Z}_M§ je řešení určeno jednoznačně), kde §M = \prod_{i=1}^N m_i§.""")),
        core = """\begin{aligned}&\gcd(m_i,m_j)=1\ (i\ne j)\Rightarrow\\&\exists x\ \forall i:\ x\equiv a_i\pmod{m_i},\\&x\text{ jediné v }\mathbb{Z}_M,\\&M=m_1\cdots m_N\end{aligned}""",
        parts = listOf(part("Existencia", """\gcd(m_i,m_j)=1\ (i\ne j)\softbreak[1]\ \Rightarrow\exists x\in\mathbb{Z}\ \forall i:\ \softbreak[2]x\equiv a_i\pmod{m_i}""", 0), part("Jednoznačnosť", """x,y\text{ riešia sústavu}\softbreak[1]\Rightarrow\ x\equiv y\pmod M""", 2)),
    ),
    "Pri navzájom nesúdeliteľných moduloch má sústava vždy riešenie, jediné modulo súčin modulov.",
    proved(tcProofCvoz),
    note = "Ústna skúška chce vedieť jej znenie (stránka predmetu). Spěvák ju na 8. prednáške vyslovil ako špeciálny prípad zovšeobecnenej vety, dôkaz vynechal a vzorec s §M_i§ ukázal len ako poznámku; dôkaz zo skrípt je zároveň návod, ako sústavu riešiť.",
    short = "o sústave kongruencií"
)

internal val tcThmZcvoz = Theorem(
    "dml-tc-zcvoz",
    "Zovšeobecnená ČVOZ",
    "Skriptá, Věta 6.20",
    wording(
        listOf(p("""Uvažujme soustavu lineárních kongruencí"""), b("""\begin{aligned}x &\equiv a_1 \pmod{m_1},\\x &\equiv a_2 \pmod{m_2},\\&\ \ \vdots\\x &\equiv a_N \pmod{m_N},\end{aligned}"""), p("""kde §m_1,m_2,\dots,m_N \geq 2§.""")),
        listOf(p("""Tato soustava má řešení právě tehdy, když §\gcd(m_i,m_j)§ dělí §a_i-a_j§ pro všechna §i\neq j§. Pokud řešení existuje, je určeno jednoznačně modulo §\lcm(m_1,m_2,\ldots,m_N)§.""")),
    ),
    "Pri súdeliteľných moduloch musia dve kongruencie súhlasiť modulo §\\gcd§ svojich modulov. Riešenie je potom jediné modulo §\\lcm§, nie modulo súčin.",
    noProof("Skriptá dôkaz vynechávajú a nepožadujú; Spěvák ho na 8. prednáške tiež preskočil a ukázal len postup dosadzovaním.", "Skriptá, dôkaz Věty 6.20; Spěvák, 8. prednáška"),
    note = "Test 6. 12. 2023: moduly §65§ a §91§ majú §\\gcd=13§.",
    short = "o sústave so súdeliteľnými modulmi"
)

internal val tcThmLemma62 = Theorem(
    "dml-tc-lemma62",
    "Krátenie gcd a deliteľnosť súčinu",
    "Skriptá, Lemma 6.2",
    wording(
        listOf(p("""Buďte §a,b,c\in\mathbb{Z}§ libovolné.""")),
        listOf(i("""Pokud neplatí §a = b = 0§, pak §§\gcd\Big(\frac{a}{\gcd{(a,b)}}, \softbreak[1]\frac{b}{\gcd{(a,b)}}\Big)=1.§§"""), i("""Jestliže §a|bc§ a současně §\gcd(a,b)=1§, pak nutně §a|c§.""")),
    ),
    "Po vydelení §\\gcd§ sú čísla nesúdeliteľné. A číslo nesúdeliteľné s §b§, ktoré delí §bc§, musí deliť §c§: na tom stojí dôkaz krátenia.",
    shortProof("""Nechť §d=\gcd(a,b)§, ukážeme, že §\tfrac ad§ a §\tfrac bd§ nemají jiný společný kladný dělitel než 1. Buď §e\in\mathbb{N}§ takové, že §e\mid(\tfrac ad)§ a §e\mid(\tfrac bd)§. Tedy §a=dek§ a §b=de\ell§, takže §de§ je společným dělitelem §a§ a §b§. Jenže §d§ má být největší společný dělitel §a§ a §b§, takže nutně §de\leq d§, z čehož plyne §e=1§. Předpokládáme §\gcd(a,b)=1§. Z Bézoutovy rovnosti víme, že existují §x,y\in\mathbb{Z}§ taková, že §1=ax+by§. Po vynásobení §c§ dostaneme §c=cax+cby§. Podle předpokladu §a\mid(bc)§ a samozřejmě i §a\mid(cax)§, takže díky Tvrzení 6.1 je výraz na pravé straně také dělitelný číslem §a§, proto musí platit i §a\mid c§.""", "Skriptá, dôkaz Lemmatu 6.2"),
    note = "Ukážková otázka ústnej skúšky chce obe tvrdenia sformulovať a potom s nimi dokázať vetu o krátení (stránka predmetu).",
    short = "o gcd a deliteľnosti súčinu"
)


internal val tcThmLdrVsetky = Theorem(
    "dml-tc-ldr-vsetky",
    "Všetky riešenia LDR",
    "Skriptá, Věta 6.8",
    wording(
        listOf(p("""Nechť §a,b\in\mathbb{Z}\setminus\{0\}§ a dvojice §(x_0,y_0)\in\mathbb{Z}^2§ je řešením rovnice §ax + by = c§.""")),
        listOf(p("""Potom množina všech celočíselných řešení této rovnice je"""), b("""\Big\{\Big(x_0 + \frac{b}{\gcd(a,b)}\cdot k,\ \softbreak[1]y_0 - \frac{a}{\gcd(a,b)}\cdot k\Big) \ \Big|\softbreak[1]\ k\in \mathbb{Z}\Big\}.""")),
        core = """\begin{aligned}&d=\gcd(a,b),\quad k\in\mathbb{Z}:\\&x=x_0+\frac bd k,\\&y=y_0-\frac ad k\end{aligned}""",
        rest = listOf(p("""což lze ekvivalentně zapsat také jako"""), b("""\Big\{(x_0,y_0) \softbreak[1]+ k \cdot \Big(\frac{b}{\gcd(a,b)}, \softbreak[2]\frac{-a}{\gcd(a,b)}\Big) \ \Big|\softbreak[1]\ k\in \mathbb{Z}\Big\}.""")),
    ),
    m("""K jednému riešeniu pripočítaš ľubovoľný celý násobok najmenšieho kroku. Tak získaš všetky riešenia."""),
    shortProof("""Předpokládáme existenci řešení §(x_0,y_0)§, uvažujme (a chtějme popsat) obecné řešení §(x,y)§. Zřejmě platí §ax+by = ax_0 + by_0§, tedy §ax-ax_0=by_0-by§. Vydělením obou stran rovnice číslem §d=\gcd(a,b)§ dostaneme §\frac{a}{d}(x-x_0)=\frac{b}{d}(y_0-y)§. Díky 2. bodu Lemmatu 6.2 dále platí §\tfrac{a}{d}\mid(y_0-y)§, §\tfrac{b}{d}\mid(x-x_0)§. Proto existují §u,v\in\mathbb{Z}§ taková, že §(x-x_0)=u\cdot\frac{b}{d}§ a §(y_0-y)=v\cdot\frac{a}{d}§. Po dosazení do vztahu §\frac{a}{d}(x-x_0)=\frac{b}{d}(y_0-y)§ a následném zkrácení nenulovými zlomky §\frac{a}{d},\frac{b}{d}§ dostaneme §u=v§. §k\in\mathbb{Z}§ mohlo být libovolné, pro různá §k§ zjevně dostaneme různá řešení a snadno ověříme (zkouškou), že pro libovolné §k\in\mathbb{Z}§ jsou §x,y§ výše opravdu řešeními.""", "Skriptá, dôkaz Věty 6.8"),
    short = "o všetkých riešeniach lineárnej diofantickej rovnice"
)

internal val tcThmLdrNavod = Theorem(
    "dml-tc-ldr-navod",
    "Ako nájsť riešenie LDR",
    "Skriptá, Důsledek 6.1",
    wording(
        listOf(p("""Potřebujeme-li nalézt nějaké řešení lineární diofantické rovnice §ax+by=c§,""")),
        listOf(p("""stačí postupovat v souladu s důkazem Věty 6.7:"""), i("""Určíme §\gcd(a,b)§. Vzhledem ke krokům níže je pro to vhodnější použít rovnou REA namísto základního EA."""), i("""Není-li §c§ násobkem §\gcd(a,b)§, řešení LDR neexistuje."""), i("""Je-li §c§ násobkem §\gcd(a,b)§, řešení existují. Pomocí rozšířeného Eukleidova algoritmu nalezneme nějakou dvojici Bézoutových koeficientů §\alpha,\beta\in\mathbb{Z}§, splňujících §\gcd(a,b)=\alpha\cdot a+\beta\cdot b§."""), i("""Obě strany Bézoutovy identity můžeme vynásobit celým číslem §\frac{c}{\gcd(a,b)}§, dostaneme tak §c=\tfrac{c}{\gcd(a,b)}\cdot\gcd(a,b)=\tfrac{c}{\gcd(a,b)}\cdot\alpha\cdot a+\tfrac{c}{\gcd(a,b)}\cdot\beta\cdot b§. Z toho plyne řešení zadané LDR: §x=\alpha\cdot\frac{c}{\gcd(a,b)}§, §y=\beta\cdot\frac{c}{\gcd(a,b)}§.""")),
        core = """\begin{aligned}d&=\gcd(a,b),\\d\nmid c&\ \Rightarrow\ \text{bez riešenia},\\d\mid c:&\quad x=\alpha\frac cd,\quad y=\beta\frac cd\end{aligned}""",
    ),
    m("""Bézoutove koeficienty vynásobíš podielom pravej strany a §\gcd§. Pre §a=b=0§ sa deliť nedá: §0x+0y=c§ rieši každá dvojica pri §c=0§, inak žiadna."""),
    shortProof("""Po dosazení dostáváme §c=k\cdot\gcd(a,b)=k\alpha a+k\beta b§, tedy celá čísla §x=k\alpha,y=k\beta§ (kde §k=\frac{c}{\gcd(a,b)}§) řeší rovnici §ax+by=c§.""", "Skriptá, dôkaz Věty 6.7"),
    short = "o nájdení riešenia lineárnej diofantickej rovnice"
)

internal val tcThmMfvInverze = Theorem(
    "dml-tc-mfv-inverze", "Inverzia pomocou MFV", "Skriptá, Tvrzení 6.7",
    wording(
        listOf(p("""Pokud je §p§ prvočíslo a §a\in \mathbb{Z}_p\setminus\{0\}§,""")),
        listOf(p("""pak §a^{p-2}§ je multiplikativní inverzí čísla §a \bmod p§.""")),
    ),
    m("""Inverziou je zvyšok §a^{p-2}\bmod p§, nie samotná mocnina: v §\mathbb{Z}_7§ je §3^5=243§, ale §3^{-1}=243\bmod 7=5§."""),
    shortProof("""Když §a§ není násobkem §p§, pak z Malé Fermatovy věty plyne §a\cdot a^{p-2}=a^{p-1}\equiv1\pmod p§. To ovšem znamená, že §a^{p-2}§ je multiplikativní inverzí čísla §a§ modulo §p§.""", "Skriptá, dôkaz Tvrzení 6.7"),
    short = "o inverzii pomocou malej Fermatovej vety",
)

internal val tcThmPhiPrvocislo = Theorem(
    "dml-tc-phi-prvocislo", "Eulerova funkcia na prvočíslach", "Skriptá, Tvrzení 6.8",
    wording(
        emptyList(),
        listOf(p("""Přirozené číslo §p§ je prvočíslem, právě když platí §\varphi(p)=p-1§.""")),
    ),
    m("""Rovnosť §φ(p)=p-1§ charakterizuje prvočísla."""),
    shortProof("""Je-li §p§ prvočíslo, pak jsou z přirozených čísel mezi §1§ a §p§ nesoudělná s §p§ právě ta, která jsou menší než §p§, a takových čísel je právě §p-1§, takže §\varphi(p)=p-1§. Kdyby bylo §p§ složené číslo, pak by muselo mít nějakého dělitele §d§ takového, že §1<d<p§. Pak by ovšem bylo §\varphi(p)\leq p-2§, což je spor, takže §p§ musí být prvočíslo.""", "Skriptá, dôkaz Tvrzení 6.8"),
    short = "o Eulerovej funkcii na prvočíslach",
)

internal val tcThmPhiMocnina = Theorem(
    "dml-tc-phi-mocnina", "Eulerova funkcia na mocninách prvočísel", "Skriptá, Tvrzení 6.9",
    wording(
        listOf(p("""Nechť §p§ je prvočíslo, §\alpha \in \mathbb{N}§ a §\varphi(m)§ značí Eulerovu funkci.""")),
        listOf(p("""Pak §\varphi(p^{\alpha})=p^{\alpha} - p^{\alpha-1}§.""")),
    ),
    m("""Od všetkých čísel odpočítaš násobky prvočísla."""),
    shortProof("""Navíc, čísla soudělná s prvočíslem jsou pouze jeho násobky. Všechna přirozená čísla menší nebo rovná §p^\alpha§ a soudělná s ním, můžeme tedy vyjádřit ve tvaru §kp§, kde §1\leq k\leq p^{\alpha-1}§. Z toho vyplývá, že existuje přesně §p^{\alpha-1}§ takových čísel, a tak je právě §p^\alpha-p^{\alpha-1}§ celých čísel menších nebo rovných §p^\alpha§, která s ním jsou nesoudělná.""", "Skriptá, dôkaz Tvrzení 6.9"),
    short = "o Eulerovej funkcii na mocninách prvočísel",
)

internal val tcThmPhiSoucin = Theorem(
    "dml-tc-phi-soucin", "Multiplikativita Eulerovej funkcie", "Skriptá, Tvrzení 6.10",
    wording(
        listOf(p("""Nechť §m,n \in \mathbb{N}§, §\gcd(m,n)=1§ a §\varphi(m)§ značí Eulerovu funkci.""")),
        listOf(p("""Potom §\varphi(m\cdot n)=\varphi(m)\cdot \varphi(n)§.""")),
        rest = listOf(p("""Funkce s touto vlastností, „obraz součinu nesoudělných argumentů je roven součinu jejich obrazů“ (které navíc zobrazují §1§ na §1§) nazýváme **multiplikativní**.""")),
    ),
    m("""Pre nesúdeliteľné argumenty sa hodnoty Eulerovej funkcie násobia."""),
    noProof("Skriptá dôkaz preskakujú a nevyžadujú; Spěvák ho na 8. prednáške tiež vynechal („je technická“).", "Skriptá, dôkaz Tvrzení 6.10; Spěvák, 8. prednáška"),
    short = "o multiplikativite Eulerovej funkcie",
)

internal val tcThmPhiRozklad = Theorem(
    "dml-tc-phi-rozklad", "Eulerova funkcia z faktorizácie", "Skriptá, Věta 6.16",
    wording(
        listOf(p("""Nechť §m = p_1^{\alpha_1}\cdot p_2^{\alpha_2}\cdots p_k^{\alpha_k}§ je kanonický rozklad složeného čísla §m \in \mathbb{N}§ a §\varphi(m)§ značí Eulerovu funkci.""")),
        listOf(p("""Potom"""), b("""\varphi(m)= m\left(1 - \frac{1}{p_1}\right)\softbreak[1]\left(1 - \frac{1}{p_2}\right)\cdots\softbreak[1]\left(1 - \frac{1}{p_k}\right).""")),
        core = """\varphi(m)=m\prod_{j=1}^k\left(1-\frac1{p_j}\right)""",
    ),
    m("""Každé rôzne prvočíslo v rozklade prispieva faktorom §1-1/p§."""),
    shortProof("""Vzhledem k tomu, že funkce §\varphi§ je multiplikativní, platí §\varphi(m)=\varphi(p_1^{\alpha_1})\varphi(p_2^{\alpha_2})\cdots\varphi(p_k^{\alpha_k})§. Z vlastností Eulerovy funkce dále pro každé §j\in\{1,2,\dots,k\}§ platí §\varphi(p_j^{\alpha_j})=p_j^{\alpha_j}-p_j^{\alpha_j-1}=p_j^{\alpha_j}(1-\frac1{p_j})§.""", "Skriptá, dôkaz Věty 6.16"),
    short = "o výpočte Eulerovej funkcie z rozkladu",
)

internal val tcThmEvInverze = Theorem(
    "dml-tc-ev-inverze", "Inverzia pomocou Eulerovej vety", "Skriptá, Tvrzení 6.11",
    wording(
        listOf(p("""Pokud §m \in \mathbb{N}, m\geq 2§ a číslo §a \in \mathbb{Z}_m§ je nesoudělné s §m§,""")),
        listOf(p("""pak §a^{\varphi(m)-1}§ je multiplikativní inverzí čísla §a \bmod m§.""")),
    ),
    m("""Inverziou je zvyšok §a^{\varphi(m)-1}\bmod m§, nie samotná mocnina: v §\mathbb{Z}_9§ je §2^5=32§, ale §2^{-1}=32\bmod 9=5§."""),
    shortProof("""Pokud §a§ je nesoudělné s §m§, pak z Eulerovy věty plyne §a\cdot a^{\varphi(m)-1}=a^{\varphi(m)}\equiv1\pmod m§. To ovšem znamená, že §a^{\varphi(m)-1}§ je multiplikativní inverzí čísla §a§ modulo §m§.""", "Skriptá, dôkaz Tvrzení 6.11"),
    short = "o inverzii pomocou Eulerovej vety",
)

internal val tcThmLkonPocet = Theorem(
    "dml-tc-lkon-pocet",
    "Počet riešení kongruencie modulo m",
    "Skriptá, Věta 6.18",
    wording(
        listOf(p("""Jestliže §\gcd(a,m) | b§,""")),
        listOf(p("""potom lineární kongruence §ax \equiv b \pmod m§ má v §\mathbb{Z}_m§ (tzv. v modulu nebo modulo §m§) **přesně** §\gcd(a,m)§ řešení.""")),
        core = """\begin{aligned}&d=\gcd(a,m),\\&S=\{x\in\mathbb{Z}_m:\\&ax\equiv b\pmod m\},\\&|S|=d\end{aligned}""",
        rest = listOf(p("""Množinu řešení v §\mathbb{Z}_m§ získáme například jako"""), b("""\Big\{\Big(x_0 + k \cdot \frac{m}{\gcd(a,m)}\Big) \softbreak[1]\bmod m \ \Big|\softbreak[1]\ k\in\{0,1,\dots,\softbreak[2]\gcd(a,m)-1 \}\Big\},"""), p("""kde §x_0§ je z dvojice §(x_0, y_0)§, která je nějakým řešením §a\cdot x + m\cdot y = b§."""), p("""Za §k§ lze případně volit libovolných §\gcd(a,m)§ po sobě jdoucích celých čísel.""")),
    ),
    m("""Riešenia sa líšia o násobky §\tfrac{m}{\gcd(a,m)}§, takže v §\mathbb{Z}_m§ ich je presne §\gcd(a,m)§."""),
    shortProof("""Plyne ze znalosti množiny řešení lineárních diofantických rovnic (Věta 6.17) a vlastnosti kongruence modulo §m§. Budeme-li do výrazu §(x_0+k\cdot\frac m{\gcd(a,m)})§ za §k§ postupně dosazovat po sobě jdoucí celá čísla, budeme dostávat celá čísla lišící se od sousedních vždy o §\frac m{\gcd(a,m)}§. Pro libovolné §\ell\in\mathbb{Z}§ navíc platí, že pokud dosadíme §k=\ell§ a §k=\ell+\gcd(a,m)§, díky redukci §\bmod m§ dostaneme stejné řešení.""", "Skriptá, dôkaz Věty 6.18"),
    short = "o počte riešení lineárnej kongruencie"
)

internal val tcThmDelitelnost = Theorem(
    "dml-tc-delitelnost-vlastnosti",
    "Vlastnosti deliteľnosti",
    "Skriptá, Tvrzení 6.1",
    wording(
        emptyList(),
        listOf(p("""Pro relaci dělitelnosti §|§ platí:"""), b("""(\forall n\in\mathbb{Z})\big(1\mid n\wedge n\mid0\big)."""), p("""Dále, buďte §a,b,c\in\mathbb{Z}§. Pak:"""), i("""§a|b§ právě tehdy, když §|a|\mid|b|§."""), i("""Jestliže §a|b§ a §b\neq0§, tak §|a|\leq|b|§."""), i("""Jestliže §a|b§ a §a|c§, pak §a|(b+c)§."""), i("""Jestliže §a|b§, pak §a|(nb)§ pro všechna §n\in\mathbb{Z}§."""), i("""Jestliže §a|(b+c)§ a současně §a|b§, pak §a|c§."""), i("""Platí, že §a|b§ a §a|c§ právě tehdy, když pro všechna §m,n\in\mathbb{Z}§ platí §a|(mb+nc)§.""")),
    ),
    m("""Deliteľnosť sa zachová pri celočíselných kombináciách. To zdôvodňuje kroky Euklidovho algoritmu aj LDR."""),
    shortProof("""Vztahy §1|n§ a §n|0§ plynou z toho, že §1\cdot n=n§ a §n\cdot 0=0§. 1. Z §a|b§, tj. §b=k\cdot a§ pro nějaké §k\in\mathbb{Z}§, plyne §|b|=|k|\cdot|a|§, tedy §|a|\,|\,|b|§; opačným směrem obdobně, §|b|=k\cdot|a|§ implikuje §b=k\cdot a§ nebo §b=(-k)\cdot a§. 2. Z §b=k\cdot a§ a §b\neq 0§ plyne §k\neq 0§, tedy §|k|\geq 1§ a §|b|=|k|\cdot|a|\geq|a|§. 3. Z §b=k\cdot a§ a §c=\ell\cdot a§ plyne §b+c=(k+\ell)\cdot a§, tedy §a|(b+c)§. 4. Z §b=k\cdot a§ plyne §n\cdot b=n\cdot k\cdot a§, tedy §a|(nb)§. 5. Z §b+c=k\cdot a§ a §b=\ell\cdot a§ plyne §c=a\cdot(k-\ell)§, tedy §a|c§. 6. §(\Rightarrow)§: z §b=ka§ a §c=\ell a§ je §mb+nc=(mk+n\ell)a§. §(\Leftarrow)§: platí-li §a|(mb+nc)§ pro libovolné §m,n§, platí i pro §m=1,n=0§, resp. §m=0,n=1§.""", "Skriptá, dôkaz Tvrzení 6.1"),
    short = "o vlastnostiach deliteľnosti"
)

internal val tcThmModuloOperace = Theorem(
    "dml-tc-modulo-operace",
    "Operácie modulo m a kongruencia",
    "Skriptá, Pozorování 6.2",
    wording(
        listOf(p("""Nechť §m\in\mathbb{N},m\geq2§.""")),
        listOf(i("""Každé celé číslo je kongruentní svému zbytku po dělení, tj. §(\forall n\in\mathbb{N})(n\bmod m\equiv n\pmod m)§."""), i("""Pro libovolné §a,b\in\mathbb{Z}§ (tedy i pro libovolné §a,b\in\mathbb{Z}_m§) platí:"""), b("""a=b\ \softbreak[1]\underset{\nLeftarrow}{\Rightarrow}\ a\bmod m=b\bmod m\ \softbreak[1]\Leftrightarrow\ a\equiv b\pmod m"""), i("""Operace modulo splňují pro libovolné §a,b\in\mathbb{Z}§ (tedy i pro libovolné §a,b\in\mathbb{Z}_m§):"""), b("""a+_m b=(a\bmod m)+_m b\softbreak[1]=a+_m(b\bmod m)\softbreak[1]=(a\bmod m)\softbreak[2]+_m(b\bmod m)\softbreak[1]\equiv a+b\pmod m,"""), b("""a\cdot_m b=(a\bmod m)\cdot_m b\softbreak[1]=a\cdot_m(b\bmod m)\softbreak[1]=(a\bmod m)\softbreak[2]\cdot_m(b\bmod m)\softbreak[1]\equiv a\cdot b\pmod m""")),
    ),
    m("""Vstupy smieš nahradiť zvyškami už počas výpočtu. Výsledok operácie modulo m sa nezmení. Bod 1 platí pre každé celé §n§, ako hovorí jeho slovné znenie; vzorec v skriptách má §n\in\mathbb{N}§."""),
    noProof("Skriptá ho uvádzajú bez dôkazu.", "Skriptá, Pozorování 6.2"),
    short = "o operáciách modulo m a kongruencii"
)

internal val tcThmModuloAlgebra = Theorem(
    "dml-tc-modulo-algebra",
    "Algebraické vlastnosti operácií modulo m",
    "Skriptá, Věta 6.11",
    wording(
        emptyList(),
        listOf(p("""Trojice §(\mathbb{Z}_m,+_m,\cdot_m)§, kde §m\geq2§, splňuje následující vlastnosti §(\forall x,y,z\in\mathbb{Z}_m)§:"""), i("""Uzavřenost na operace: §(\forall x,y\in\mathbb{Z}_m)(x+_m y\in\mathbb{Z}_m)§ §(\forall x,y\in\mathbb{Z}_m)(x\cdot_m y\in\mathbb{Z}_m)§"""), i("""Komutativní zákony: §(\forall x,y\in\mathbb{Z}_m)(x+_m y=y+_m x)§ §(\forall x,y\in\mathbb{Z}_m)(x\cdot_m y=y\cdot_m x)§"""), i("""Asociativní zákony: §(\forall x,y,z\in\mathbb{Z}_m)(x+_m(y+_m z)=(x+_m y)+_m z)§ §(\forall x,y,z\in\mathbb{Z}_m)(x\cdot_m(y\cdot_m z)=(x\cdot_m y)\cdot_m z)§"""), i("""Distributivní zákony: §(\forall x,y,z\in\mathbb{Z}_m)(x\cdot_m(y+_m z)=(x\cdot_m y)+_m(x\cdot_m z))§ §(\forall x,y,z\in\mathbb{Z}_m)((x+_m y)\cdot_m z=(x\cdot_m z)+_m(y\cdot_m z))§"""), i("""Existence neutrálního prvku: §(\exists0\in\mathbb{Z}_m)(\forall x\in\mathbb{Z}_m)(x+_m0=0+_m x=x)§ §(\exists1\in\mathbb{Z}_m)(\forall x\in\mathbb{Z}_m)(x\cdot_m1=1\cdot_m x=x)§"""), i("""Existence inverzního prvku: §(\forall x\in\mathbb{Z}_m)(\exists y\in\mathbb{Z}_m)(x+_m y=y+_m x=0)§""")),
    ),
    m("""Sčítanie a násobenie zvyškov zachovávajú algebraické zákony celých čísel. Opačný prvok existuje ku každému zvyšku."""),
    shortProof("""Uzavřenost na operace plyne rovnou z definice, přechodem od §\mathbb{Z}§ k §\mathbb{Z}_m§ totiž provádíme zúžení obou zobrazení §+_m,\cdot_m:\mathbb{Z}\times\mathbb{Z}\to\mathbb{Z}_m§ na §\mathbb{Z}_m\times\mathbb{Z}_m\to\mathbb{Z}_m§. Komutativní, asociativní a distributivní zákony plynou z jejich protějšků platných pro klasické §+,\cdot§. Stačí použít Pozorování 6.2 a uvědomit si, že rovnají-li se dva výrazy obsahující §+,\cdot§, rovnají se i jejich zbytky po dělení §m§. To, že čísla §0§, resp. §1§ jsou skutečně neutrálními prvky vůči sčítání, resp. násobení modulo, je zřejmé. Pro libovolné §m\geq2§ a §a\in\mathbb{Z}_m§ existuje §x\in\mathbb{Z}_m§ takové, že §x+_m a=a+_m x=0§ a snadnou úvahou dojdeme k tomu, že musí být pouze jedno.""", "Skriptá, dôkaz Věty 6.11"),
    short = "o algebraických vlastnostiach operácií modulo m"
)

internal val tcThmKongruenceVlastnosti = Theorem(
    "dml-tc-kongruence-vlastnosti",
    "Vlastnosti kongruencie",
    "Skriptá, Tvrzení 6.5",
    wording(
        listOf(p("""Nechť §m\geq 2§. Relace kongruence §\equiv\pmod m§ splňuje následující vlastnosti:""")),
        listOf(i("""§(\forall a\in\mathbb{Z})\big(a\equiv a\pmod m\big)§."""),
            i("""§(\forall a,b\in\mathbb{Z})\softbreak\big(a\equiv b\pmod m\softbreak[1]\Rightarrow b\equiv a\pmod m\big)§."""),
            i("""§(\forall a,b,c\in\mathbb{Z})\softbreak\Big(\big(a\equiv b\pmod m\softbreak[2]\wedge b\equiv c\pmod m\big)\softbreak[1]\Rightarrow a\equiv c\pmod m\Big)§.""")),
        core = """\begin{aligned}&a\equiv a\\&a\equiv b\ \Rightarrow\ b\equiv a\\&a\equiv b\wedge b\equiv c\ \Rightarrow\ a\equiv c\end{aligned}""",
        parts = listOf(part("Reflexivita", """a\equiv a\pmod m""", 0), part("Symetria", """a\equiv b\softbreak[1]\Rightarrow b\equiv a\pmod m""", 1),
            part("Tranzitivita", """a\equiv b\wedge b\equiv c\softbreak[1]\Rightarrow a\equiv c\pmod m""", 2)),
    ),
    "Kongruencia sa správa ako rovnosť: číslo je kongruentné so sebou, vzťah sa dá otočiť a reťaziť. Myšlienka dôkazu: rozdiely §a-b=km§ sa otáčajú zmenou znamienka a sčítajú.",
    proved(tcProofKongruenceVlastnosti),
    note = "Spěvák (7. prednáška) aj Dombek (8. prednáška) ich nechali dokázať z definície; skriptá: „vyzkoušejte!“.",
    short = "o vlastnostiach kongruencie"
)

internal val tcThmGcdRozklad = Theorem(
    "dml-tc-gcd-rozklad",
    "gcd a lcm pomocou rozkladu",
    "Skriptá, Pozorování 6.1",
    wording(
        listOf(p("""Buďte §a,b\in\mathbb{N}§, §a,b\geq 2§, jejichž prvočíselné faktorizace doplníme tak, aby §a§ i §b§ byly vyjádřeny pomocí mocnin stejné §k§-tice prvočísel (chybějící doplníme s nulovým exponentem),"""),
            b("""\begin{aligned}a&=p_1^{\alpha_1}\cdot p_2^{\alpha_2}\cdots p_k^{\alpha_k},\\ b&=p_1^{\beta_1}\cdot p_2^{\beta_2}\cdots p_k^{\beta_k}.\end{aligned}""")),
        listOf(p("""Potom pro jejich §\gcd(a,b)§, §\lcm(a,b)§ platí:"""),
            b("""\gcd(a,b)=\softbreak[1]p_1^{\min(\alpha_1,\beta_1)}\cdot p_2^{\min(\alpha_2,\beta_2)}\softbreak[1]\cdots p_k^{\min(\alpha_k,\beta_k)},"""),
            b("""\lcm(a,b)=\softbreak[1]p_1^{\max(\alpha_1,\beta_1)}\cdot p_2^{\max(\alpha_2,\beta_2)}\softbreak[1]\cdots p_k^{\max(\alpha_k,\beta_k)},""")),
        rest = listOf(p("""kde výrazy §\min(\alpha_k,\beta_k)§ a §\max(\alpha_k,\beta_k)§ značí menší, resp. větší z prvků §\alpha_k,\beta_k§.""")),
    ),
    "§\\gcd§ má pri každom prvočísle menší z dvoch exponentov, §\\lcm§ väčší. Prvočíslo, ktoré v čísle nie je, má exponent §0§ (Spěvák, 7. prednáška).",
    noProof("Skriptá ho uvádzajú bez dôkazu: „ten je zřejmý z definice“.", "Skriptá, Pozorování 6.1"),
    note = "Proseminár (25/26, č. 6): pre veľké čísla je rozklad pomalý, §\\gcd§ vtedy počítaj Euklidovým algoritmom.",
    short = "o gcd a lcm z rozkladu"
)

internal val tcTheorems = listOf(tcThmDeleni, tcThmLemma61, tcThmEa, tcThmBezout, tcThmRea, tcThmGcdLcm, tcThmGcdRozklad, tcThmLdr, tcThmKongruenceEkv, tcThmKongruenceVlastnosti, tcThmOperace, tcThmInverze, tcThmInverzeVsetky, tcThmLemma62, tcThmKraceni, tcThmMfv, tcThmEv, tcThmLkon, tcThmCvoz, tcThmZcvoz, tcThmLdrVsetky, tcThmLdrNavod, tcThmMfvInverze, tcThmPhiPrvocislo, tcThmPhiMocnina, tcThmPhiSoucin, tcThmPhiRozklad, tcThmEvInverze, tcThmLkonPocet, tcThmDelitelnost, tcThmModuloOperace, tcThmModuloAlgebra)

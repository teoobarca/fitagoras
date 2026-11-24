# BI-DML kap. 6: Základy teorie čísel

Balík kapitoly: skriptá `chap-06-tc.woo` + `cv-06`, slajdy `DML_P05_tc`, kódy rozvrhu TČ1 až TČ4; v appke téma `dml-tc` (`data/dml/Numbers*.kt`).

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## Časť 1: deliteľnosť, Euklidov algoritmus, diofantické rovnice, prvočísla (chap-06-tc.woo:1–1676)

### 1. Rozsah a zdroje

Stav overenia: 17. 11. 2026, výlučne lokálne zdroje a časované titulky; obraz ani zvuk nahrávok neboli overené.
Rozsah: `chap-06-tc.woo:1–1676`, sekcie „O teorii čísel“, „Dělitelnost“, „Eukleidův algoritmus“, „Lineární diofantické rovnice“, „Prvočísla“.
Začína Větou 6.1 (Velká Fermatova věta, motivácia), končí Tvrzením 6.3 (vzťah $\gcd$ a $\lcm$); posledná definícia je Definice 6.6, posledná veta Věta 6.10, posledná lemma Lemma 6.3.
Čísla sú prevzaté výhradne z `cisla/dml-kap6.tsv`; rôzne druhy majú samostatné počítadlá.
Modulárna aritmetika od riadka 1677 sem nepatrí; operácia zvyšku $a\bmod d$ sem patrí už ako Definice 6.2.
RSA je iba bonus: „Toto téma nepatří do osnov BI-DML a rozhodně nebude nijak zkoušeno.“ [chap-06-tc.woo:145]

- Dombek: `dml-P6-par1-dombek` 1:11:49–1:37:35, motivácia, deliteľnosť, dôkaz delenia so zvyškom.
- Dombek: `dml-P7-par1-dombek` 06:24–1:38:47, zvyšok, $\gcd/\lcm$, EA, REA, LDR až po odvodenie všetkých riešení.
- Spěvák: `dml-P6-par2-spevak` 35:18–1:33:36, motivácia až REA; LDR ohlasuje na ďalšiu prednášku.
- Spěvák: `dml-P7-par2-spevak` 05:29–1:08:23, opakovanie Bézouta, LDR, prvočísla, faktorizácia, $\gcd\cdot\lcm$; od 1:09:00 modulárna aritmetika.
- Legerský: `dml-P7-par3-legersky` 1:10:07–1:38:10, motivácia, deliteľnosť, jednoznačnosť delenia so zvyškom; existenciu necháva na ďalšiu prednášku.
- Legerský: `dml-P8-par3-legersky` zachovaný obsah 09:07–1:38:40, existencia delenia so zvyškom, $\gcd/\lcm$, EA, REA, LDR.
- Proseminár: `dml-prosem2526-06` 00:00–43:15, dôkazy deliteľnosti, faktorizácia, REA, $\lcm$, tri LDR. Meno sa v titulkoch nepredstaví.

INDEX označuje Dombekovu P6 ako `failed`, ale lokálny súbor obsahuje použiteľné titulky vrátane celej zadanej časti; označenie neznamená, že titulky chýbajú.
Legerského P8 je `partial`: pred 09:07 sú prevažne neobsahové úseky; neodvodzovať z toho presný skutočný začiatok výkladu.
Prvočísla z tohto rozsahu sú odprednášané u Spěváka. V zadanej dvojici Dombekových nahrávok ešte nie; Legerský ich na konci P8 výslovne necháva na nabudúce. [dml-P8-par3-legersky 1:38:40]
V prvej časti kapitoly preto nie je celý tematický blok, ktorý by nebol odprednášaný v žiadnej zo zadaných paraleliek; nie všetky paralelky sú však rovnako ďaleko.

### 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **Jednoduché otázky, ťažké dôkazy.** Motivácia cez staré číselné problémy, Veľkú Fermatovu vetu a kryptografiu; školská zrozumiteľnosť znenia nezaručuje ľahký dôkaz. [dml-P6-par2-spevak 35:28–38:49]

2. **Začať od definície.** $a\mid b$ znamená celočíselný násobok, nie výpočet podielu; postupne sa z definície získajú pravidlá pre súčty a lineárne kombinácie (Definice 6.1, Tvrzení 6.1). [dml-P6-par1-dombek 1:17:29–1:24:34]

3. **Zvyšok musí byť jednoznačný.** Věta 6.2 rozlišuje zadané $a\in\mathbb Z,d\in\mathbb N$ od existujúcich $q,r$ a vyžaduje $0\le r<d$; Dombek ju dokazuje existenciou pre nezáporné, záporné a potom jednoznačnosťou. [dml-P6-par1-dombek 1:25:02–1:37:35]

4. **Najväčší a najmenší podľa deliteľnosti.** Definice 6.3 nepoužíva iba školské maximum/minimum; charakterizácia cez ďalších deliteľov a násobky pokrýva aj nuly. [dml-P7-par1-dombek 09:33–17:01]

5. **Zmenšiť čísla a zachovať spoločných deliteľov.** Lemma 6.1 je most k EA: pripočítanie celočíselného násobku druhého argumentu nemení $\gcd$; klesajúce zvyšky zaručia koniec a posledný nenulový zvyšok je výsledok (Věta 6.3). [dml-P7-par1-dombek 18:21–21:18; 29:01–36:27]

6. **Z výsledku spraviť lineárnu kombináciu.** Bézoutova rovnosť (Věta 6.4) dostane konštruktívny dôkaz cez REA (Věta 6.5): k zvyškom pribudnú dva koeficientové stĺpce s rovnakými operáciami. [dml-P7-par1-dombek 37:08–49:29]

7. **Prečo práve $\gcd$.** Věta 6.6 hovorí, že je najmenšou kladnou celočíselnou lineárnou kombináciou; Legerský z toho ihneď odvodzuje, že kombinácia rovná 1 preukazuje nesúdeliteľnosť. [dml-P8-par3-legersky 1:08:18–1:11:47]

8. **Zaplatiť mincami.** $7x+13y=125$ motivuje LDR; riešiteľnosť je presne $\gcd(a,b)\mid c$ a násobenie Bézoutovej rovnosti dá jedno riešenie (Věta 6.7, Důsledek 6.1). [dml-P7-par2-spevak 07:22–18:38]

9. **Jedno riešenie ešte nie sú všetky.** Záporné počty mincí znamenajú vracanie; treba homogénnu rovnicu a celočíselný parameter, pričom krok je $(b/d,-a/d)$ pre $d=\gcd(a,b)$ (Lemma 6.2, Věta 6.8). [dml-P7-par2-spevak 18:45–35:30]

10. **Prvočísla sú stavebné kamene.** Spěvák pripomenie existenciu rozkladu zo silnej indukcie, konštruuje ďalšie prvočíslo, dokáže Eukleidovo lemma a jednoznačnosť faktorizácie; uzavrie výpočtom $\gcd,\lcm$ cez exponenty a ich súčin. [dml-P7-par2-spevak 41:52–1:07:51]

Spěvák pri LDR najprv samostatne vyrieši homogénnu rovnicu a až potom pridá partikulárne riešenie; skriptá a Dombek dokazujú všeobecný tvar priamo rozdielom dvoch riešení. [dml-P7-par2-spevak 24:34–34:00; dml-P7-par1-dombek 1:33:00–1:38:33; chap-06-tc.woo:1266]

### 3. Pojmy a tvrdenia

„Test“ nižšie znamená doložený výskyt alebo potrebný nástroj; pri samotnej teórii bez testového dokladu to výslovne uvádzam.
Odkazy `dml-cv-*` a `dml-rozstrel-*` sú identifikátory v `sources.json`, nie čísla viet.

| Číslo | Druh | Názov | Jadro | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 6.1 | Definice | Dělitelnost | $a\mid b\iff(\exists k\in\mathbb Z)(ak=b)$. | Definícia, nedokazuje sa. [chap-06-tc.woo:240] | Základ dôkazov, `dml-cv-06-1`, `-2`. |
| 6.1 | Tvrzení | Vlastnosti dělitelnosti | Spoločný deliteľ delí každú celočíselnú lineárnu kombináciu. | Áno; Dombek bod 6 oboma smermi. [chap-06-tc.woo:269; dml-P6-par1-dombek 1:20:15–1:23:38] | Priamo `dml-cv-06-2`, `-4`; zdôvodnenie algoritmov. |
| 6.2 | Věta | O dělení se zbytkem | Existuje jediná dvojica $q,r\in\mathbb Z$ s $a=qd+r$, $0\le r<d$. | Áno; Dombek celý dôkaz, Legerský rozdelene medzi P7/P8. [chap-06-tc.woo:377; dml-P6-par1-dombek 1:27:38–1:37:35; dml-P7-par3-legersky 1:38:10; dml-P8-par3-legersky 09:07] | Dombek ju výslovne uvádza ako skúškovú otázku. [dml-P6-par1-dombek 1:25:13] |
| 6.2 | Definice | Celočíselný podíl a zbytek po dělení | $r=a\bmod d\in\{0,\dots,d-1\}$. | Definícia. [chap-06-tc.woo:444] | EA, dôkazy deliteľnosti, `dml-cv-06-2/a`. |
| 6.3 | Definice | Společný dělitel/násobek; gcd/lcm; nesoudělnost | $\gcd$ je spoločný deliteľ deliteľný každým ďalším; $\lcm$ spoločný násobok deliaci každý ďalší. | Definícia; Dombek vyžaduje práve túto formuláciu. [chap-06-tc.woo:458; dml-P7-par1-dombek 13:30] | EA/REA a inverzie v 2. teste. [testy-dml-2.md:9] |
| 6.1 | Lemma | Příprava pro Eukleidův algoritmus | Symetria, absolútne hodnoty a $\gcd(a+cb,b)=\gcd(a,b)$. | Áno, všetky body; Dombek 17:01–21:18, Legerský 30:03–34:40; Spěvák dokazuje ekvivalentný krok so zvyškom. [chap-06-tc.woo:563; dml-P7-par1-dombek 17:01–21:18; dml-P8-par3-legersky 30:03–34:40; dml-P6-par2-spevak 1:09:46–1:12:42] | Zdôvodňuje EA; `dml-cv-06-6`. |
| 6.3 | Věta | Eukleidův algoritmus (EA) | Posledný nenulový zvyšok je $\gcd(a,b)$. | Áno; Dombek 29:01–36:27, Legerský 41:46–46:34. [chap-06-tc.woo:612; dml-P7-par1-dombek 29:01–36:27; dml-P8-par3-legersky 41:46–46:34] | Výpočtový základ, `dml-cv-06-5`; Legerského miernejšia požiadavka na dôkaz je v §6. |
| 6.4 | Věta | Bézoutova rovnost | $\gcd(a,b)=ma+nb$ pre nejaké celé $m,n$. | Áno, cez REA; Dombek ohlási dôkaz REA. [chap-06-tc.woo:739; dml-P7-par1-dombek 37:08–38:33] | Priamo rozstrel `dml-rozstrel-2023-01-11-8`. |
| 6.5 | Věta | Rozšířený Eukleidův algoritmus | S každým zvyškom sa počítajú $\alpha_i,\beta_i$ tak, aby $r_i=\alpha_i a+\beta_i b$. | Áno, silná indukcia; Dombek celý dôkaz, Legerský tiež; Spěvák vysvetlí invariant bez úplného rozpisu. [chap-06-tc.woo:771; dml-P7-par1-dombek 43:21–49:29; dml-P8-par3-legersky 1:00:13–1:08:18; dml-P6-par2-spevak 1:26:10–1:27:07] | Priamo rozstrel, ďalej inverzie. [testy-rozstrel.md:14; testy-dml-2.md:9] |
| 6.6 | Věta | gcd je nejmenší kladná lineární kombinace | Každá kladná celočíselná kombinácia $a,b\ne0$ je aspoň $\gcd(a,b)$. | Áno; čistý argument Legerský 1:09:18–1:10:56. [chap-06-tc.woo:1009; dml-P8-par3-legersky 1:09:18–1:10:56] | Podpora dôkazov, `dml-cv-06-7`; samostatný testový výskyt zdroj nemá. |
| 6.4 | Definice | Lineární diofantická rovnice | $ax+by=c$ s celočíselnými koeficientmi aj neznámymi. | Definícia. [chap-06-tc.woo:1071] | Päť základných a dvanásť ďalších LDR, `dml-cv-06-8`, `-29`. |
| 6.7 | Věta | O řešitelnosti diofantické rovnice | Riešenie existuje práve keď $\gcd(a,b)\mid c$. | Áno; Dombek oba smery. [chap-06-tc.woo:1097; dml-P7-par1-dombek 1:09:51–1:14:23] | Rozlíšenie riešiteľných a neriešiteľných úloh, `dml-cv-06-8`. |
| 6.1 | Důsledek | Jak najít řešení LDR? | Bézoutove koeficienty násobíme $c/\gcd(a,b)$. | Plynie z dôkazu Věty 6.7; Legerský zopakuje návod. [chap-06-tc.woo:1127; dml-P8-par3-legersky 1:18:50–1:19:50] | `dml-cv-06-8`, `-9`, `-10`, `-11`, `-29`. |
| 6.2 | Lemma | Příprava pro kompletní řešení LDR | Po vydelení gcd sú koeficienty nesúdeliteľné; z $a\mid bc$, $\gcd(a,b)=1$ plynie $a\mid c$. | Áno; Spěvák oba body. [chap-06-tc.woo:1206; dml-P7-par2-spevak 19:50–24:23] | Nutný krok úplnosti riešení, `dml-cv-06-11`. |
| 6.8 | Věta | Všechna řešení LDR | Pre $a,b\ne0$ sú všetky $(x_0,y_0)+k(b/d,-a/d)$, $k\in\mathbb Z$, $d=\gcd(a,b)$. | Áno; Dombek a Legerský rozdielom riešení, Spěvák cez homogénnu rovnicu. [chap-06-tc.woo:1248; dml-P7-par1-dombek 1:33:00–1:38:33; dml-P8-par3-legersky 1:30:56–1:36:40; dml-P7-par2-spevak 24:34–34:00] | Priamo LDR a minimalizácia Bézoutovho koeficientu. [testy-rozstrel.md:14; dml-cv-06-8] |
| 6.5 | Definice | Homogenní rovnice a partikulární řešení LDR | Partikulárne je ľubovoľné jedno riešenie; pridružená homogénna má pravú stranu 0. | Definícia. [chap-06-tc.woo:1304] | Terminológia postupov `dml-cv-06-8`, `-10`. |
| 6.6 | Definice | Prvočísla a čísla složená | V $\mathbb N$ je 1 osobitne, prvočísla majú dva kladné delitele, ostatné sú zložené. | Definícia; Spěvák 42:11–43:13. [chap-06-tc.woo:1389; dml-P7-par2-spevak 42:11–43:13] | Podklad faktorizácie; samostatný testový výskyt zdroj nemá. |
| 6.9 | Věta | Kolik je prvočísel | Prvočísel je nekonečne veľa. | Áno, spor; Spěvák konštruktívne indukciou. [chap-06-tc.woo:1417; dml-P7-par2-spevak 44:31–48:53] | Teoretický obsah; v zadaných testoch samostatne nenájdené. |
| 6.3 | Lemma | Eukleidovo lemma | Prvočíslo deliace súčin delí niektorý činiteľ. | Áno, Lemma 6.2 a indukcia; Spěvák oba body. [chap-06-tc.woo:1459; dml-P7-par2-spevak 48:58–53:45] | Úplnosť faktorizácie, nie všeobecné pravidlo pre ľubovoľný deliteľ. [dml-cv-06-2/d] |
| 6.2 | Tvrzení | Rozklad na součin prvočísel | Každé zložené číslo je súčinom prvočísel. | Áno odkazom na skorší dôkaz silnou indukciou; Spěvák ho pripomína. [chap-06-tc.woo:1507; dml-P7-par2-spevak 43:23–44:24] | `dml-cv-06-12`; samostatný testový výskyt zdroj nemá. |
| 6.10 | Věta | Základní věta aritmetiky | Pre $n\ge2$ existuje jediný rozklad na rastúce prvočísla s kladnými exponentmi. | Áno, najmenší protipríklad; Spěvák silnou indukciou podľa počtu činiteľov. [chap-06-tc.woo:1519; dml-P7-par2-spevak 57:19–1:01:46] | Spěvák výslovne žiada poznať názov a znenie na skúšku. [dml-P7-par2-spevak 54:15] |
| 6.1 | Pozorování | gcd a lcm pomocí faktorizace | Na spoločnom zozname prvočísel berieme minimá/maximá exponentov. | Skriptá bez dôkazu, z definície; Spěvák vysvetľuje. [chap-06-tc.woo:1585; dml-P7-par2-spevak 1:02:44–1:04:39] | Priamo `dml-cv-06-5`. |
| 6.3 | Tvrzení | Vztah gcd a lcm | $\gcd(a,b)\operatorname{lcm}(a,b)=|a||b|$. | Áno; Spěvák pomocou min/max exponentov. [chap-06-tc.woo:1613; dml-P7-par2-spevak 1:05:12–1:07:42] | Výpočet lcm v `dml-cv-06-5`; proseminár 25:16. |

Věta 6.1 o Fermatovi je motivácia; jej dôkaz v skriptách ani zadaných prednáškach nie je. [chap-06-tc.woo:62; dml-P6-par2-spevak 37:06]

### 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Dokázať deliteľnosť alebo nájsť protipríklad.**

1. Rozpísať predpoklad $a\mid b$ ako $b=ka$, $k\in\mathbb Z$.
2. Algebraicky zostaviť cieľ ako celočíselný násobok požadovaného deliteľa; pri dvoch predpokladoch použiť nezávislé celé $k,\ell$.
3. Pri ekvivalencii urobiť oba smery; pri tvrdení o všetkých lineárnych kombináciách vybrať späť $(m,n)=(1,0),(0,1)$.
4. Nepravdivý výrok vyvrátiť konkrétnymi číslami a overiť predpoklad aj neplatnosť záveru.

Dombek tak dokazuje Tvrzení 6.1/6; proseminár vyvracia všeobecné $a\mid bc\Rightarrow a\mid b\lor a\mid c$ číslami $a=7\cdot17,b=7,c=17$. [dml-P6-par1-dombek 1:20:15–1:23:38; dml-prosem2526-06 05:13–06:30]
Podmienku nenulovosti pri krátení ani kladnosti modulu nevynechať. [dml-prosem2526-06 00:21–00:49; 04:16–05:09]

**B. EA: čo písať a prečo končí.**

1. Cez Lemmu 6.1 nahradiť vstupy absolútnymi hodnotami a usporiadať $a\ge b>0$; nuly vybaviť osobitne.
2. Písať postupné delenia $r_{i-2}=q_{i-1}r_{i-1}+r_i$, $0\le r_i<r_{i-1}$; začať $r_1=a,r_2=b$.
3. Ďalšie delenie vždy používa predchádzajúci deliteľ a práve získaný zvyšok.
4. Zastaviť pri nulovom zvyšku; odpoveď je posledný nenulový, nie nula.
5. Korektnosť: kladné zvyšky ostro klesajú; dvojice po sebe idúcich členov majú rovnaký $\gcd$ podľa Lemmy 6.1; koniec je $\gcd(r_k,0)=r_k$.

V skriptách a u Dombeka/Legerského vstup $(254,158)$ dá $254,158,96,62,34,28,6,4,2,0$.
Spěvák najprv názorne počíta $(258,34)$ a až potom dokazuje algoritmus; zvyšky sú $258,34,20,14,6,2,0$.
[Věta 6.3, chap-06-tc.woo:612; chap-06-tc.woo:654; dml-P7-par1-dombek 26:07–28:53; dml-P6-par2-spevak 1:05:44–1:07:45; dml-P8-par3-legersky 39:14–41:46]

**C. REA priebežnou tabuľkou: hlavný zápis kurzu.**

Záhlavie skrípt a slajdov je v poradí $r_i,\alpha_i,\beta_i,q_i$; každý riadok kóduje $r_i=\alpha_i a+\beta_i b$.
Začať riadkami $(a,1,0,-)$ a $(b,0,1,q_2)$, kde $q_2=\lfloor a/b\rfloor$.
Pre ďalší riadok od predpredchádzajúceho odčítať $q_{i-1}$-násobok predchádzajúceho vo všetkých troch hlavných stĺpcoch:
$r_i=r_{i-2}-q_{i-1}r_{i-1}$, $\alpha_i=\alpha_{i-2}-q_{i-1}\alpha_{i-1}$, $\beta_i=\beta_{i-2}-q_{i-1}\beta_{i-1}$.
Podiel zapísaný pri novom nenulovom riadku je $q_i=\lfloor r_{i-1}/r_i\rfloor$, používa sa až na vytvorenie ďalšieho riadka.
Keď vznikne $r_i=0$, koeficienty tohto nulového riadka už netreba dopĺňať; výsledok sa číta o riadok vyššie.
[chap-06-tc.woo:862–927; P05_tc.tex:538–590]

| $r_i$ | $\alpha_i$ | $\beta_i$ | $q_i$ |
|---:|---:|---:|---:|
| 254 | 1 | 0 | |
| 158 | 0 | 1 | 1 |
| 96 | 1 | −1 | 1 |
| 62 | −1 | 2 | 1 |
| 34 | 2 | −3 | 1 |
| 28 | −3 | 5 | 1 |
| 6 | 5 | −8 | 4 |
| 4 | −23 | 37 | 1 |
| 2 | 28 | −45 | 2 |
| 0 | | | |

Tabuľka je zo skrípt: $2=28\cdot254-45\cdot158$. [chap-06-tc.woo:966–994]
Dombek ju prakticky píše, podielový stĺpec má rád, ale označuje ho za nepovinný; preferuje tabuľku pred opakovaným písaním matíc. [dml-P7-par1-dombek 49:47–50:05; 54:28–55:41]
Spěvák používa rovnaký princíp na $(258,34)$, dostane $2=-5\cdot258+38\cdot34$; kvocienty netreba osobitne vypisovať. [dml-P6-par2-spevak 1:28:29–1:28:34; 1:31:01–1:32:01]
Legerský výslovne pomenuje stĺpce $r_i,\alpha_i,\beta_i$ a celočíselné podiely; celé rovnice ukáže pre pochopenie a potom ich zhustí. [dml-P8-par3-legersky 55:59–59:43]
Proseminár: $(420,231)$, invariant vysvetlí pred počítaním, výsledok $21=5\cdot420-9\cdot231$. [dml-prosem2526-06 17:01–24:56]

**D. Rovnice, matice, spätné dosadzovanie.**

Rovnicový REA stále priebežne počíta $r_i=\alpha_i a+\beta_i b$; nejde o návrat od gcd späť cez hotový EA. [chap-06-tc.woo:833–859]
Maticový REA začína $\begin{pmatrix}a&1&0\\b&0&1\end{pmatrix}$; nový prvý riadok je starý druhý, nový druhý je starý prvý mínus $q$-násobok starého druhého.
Opakuje sa po nulu vľavo dole; prvý riadok poslednej matice je $(\gcd(a,b),\alpha,\beta)$.
Skriptá majú výslovne všetky tri formy; Spěvák maticový zápis predvedie, Dombek ho porovná s tabuľkou, Legerský odkáže na skriptá. [chap-06-tc.woo:930–1000; dml-P6-par2-spevak 1:32:03–1:33:06; dml-P7-par1-dombek 54:45–55:41; dml-P8-par3-legersky 59:02–59:17]
Samostatný postup „najprv EA, potom spätné dosadzovanie“ som v zadanej časti skrípt a časovaných výkladoch nenašiel; nepodsúvať ho ako prednáškový štandard ani tvrdiť, že je zakázaný.

**E. Záporné vstupy a poradie koeficientov.**

Počítať $\gcd(|a|,|b|)$; pri návrate k pôvodným záporným číslam zmeniť príslušné znamienka Bézoutových koeficientov.
Proseminár: $12=2\cdot156-5\cdot60=(-2)(-156)+5(-60)$.
Ak tabuľka začína väčším číslom, poradie koeficientov vo výslednej rovnici musí stále zodpovedať jej argumentom.
[chap-06-tc.woo:760–768; dml-prosem2526-06 26:03–27:23; 29:24–30:10]
Skriptá pri $7x+13y=125$ zvolia stĺpce podľa rovnice: riadok 13 má koeficienty $(0,1)$ a riadok 7 má $(1,0)$, hoci zvyšky idú zostupne. [chap-06-tc.woo:1162–1185]

**F. LDR: jedno riešenie a všetky riešenia.**

1. Vypočítať $d=\gcd(a,b)$, prakticky rovno REA.
2. Ak $d\nmid c$, odpoveď je prázdna množina s dôvodom podľa Věty 6.7.
3. Získať $d=\alpha a+\beta b$ a vynásobiť $c/d$: $(x_0,y_0)=(\alpha c/d,\beta c/d)$.
4. Pridružená homogénna rovnica je $ax+by=0$; po vydelení $d$ má nesúdeliteľné koeficienty a riešenia $k(b/d,-a/d)$.
5. Zapísať množinu všetkých riešení $\{(x_0+kb/d,y_0-ka/d)\mid k\in\mathbb Z\}$, alebo vektorovo $\{(x_0,y_0)+k(b/d,-a/d)\mid k\in\mathbb Z\}$.
6. Pri obmedzení na kladné/nezáporné počty vyriešiť nerovnosti pre ten istý celý parameter $k$.

Zdôvodnenie: Věta 6.7, Důsledek 6.1, Lemma 6.2 a Věta 6.8; kontrola dosadením a argument úplnosti, nie iba výroba niekoľkých riešení. [chap-06-tc.woo:1097–1159; 1206–1301; dml-P7-par2-spevak 24:34–34:00]
Jedno partikulárne riešenie možno aj uhádnuť, netreba povinne REA; proseminár pri $2x+3y=4$ uzná $(2,0)$ aj vypočítané $(-4,4)$. [dml-prosem2526-06 31:25–33:54]
Pri $2x+4y=6$ je výsledok $(1+2k,1-k)$, $k\in\mathbb Z$; pri $2x+4y=3$ riešenie nie je. [dml-prosem2526-06 37:35–38:01; 42:32–43:07]
Pri $7x+13y=125$ vyjde $(250,-125)+k(13,-7)$; $k=-18$ dá $(16,1)$ bez vracania mincí. [chap-06-tc.woo:1356–1374; dml-P8-par3-legersky 1:38:10–1:38:40]

Věta 6.8 má v skriptách predpoklad $a,b\ne0$; nevkladať do jej vzorca slepo prípad $(a,b)=(0,0)$.
Pri degenerovaných rovniciach treba priamo vyšetriť zostávajúcu jednu rovnicu; pre $a=b=0$ je výsledok $\mathbb Z^2$ pri $c=0$, inak $\varnothing$ (priame dosadenie do Definice 6.4).

**G. Faktorizácia, lcm a dôkaz o prvočíslach.**

Pri známych faktorizáciách zjednotiť zoznam prvočísel, chýbajúce doplniť exponentom 0; pre gcd vziať minimá, pre lcm maximá.
Pre veľké čísla je účelnejší EA a potom $\operatorname{lcm}(a,b)=|a||b|/\gcd(a,b)$, ak $\gcd(a,b)\ne0$; pri oboch nulách je lcm priamo 0.
[Pozorování 6.1, chap-06-tc.woo:1588; Tvrzení 6.3, chap-06-tc.woo:1613; dml-prosem2526-06 25:16–25:33]
Skriptá dokazujú nekonečnosť sporem cez $P=p_1\cdots p_k+1$ a jeho prvočíselného deliteľa; nepredpokladajú, že $P$ musí byť prvočíslo.
Spěvák namiesto konečného zoznamu všetkých prvočísel indukčne rozširuje ľubovoľný zoznam a berie najmenší deliteľ $P$ väčší než 1. [chap-06-tc.woo:1426–1451; dml-P7-par2-spevak 45:11–48:53]

### 5. Značenie a terminológia

- $a\mid b$: „$a$ dělí $b$“, $a$ je „dělitel“, $b$ „násobek“, resp. „$b$ je dělitelné $a$“; orientácia nie je poradie zlomku $b/a$. [Definice 6.1, chap-06-tc.woo:240; dml-P7-par3-legersky 1:16:51–1:17:51]
- $a\nmid b$ či grafické $a∤b$: negácia deliteľnosti, nie nový druh operácie. Skriptá používajú TeX `\nmid`; Legerský hovorí o preškrtnutí čiarky. [chap-06-tc.woo:256; dml-P7-par3-legersky 1:18:02]
- $a\bmod d$ je konkrétny zvyšok, teda hodnota funkcie; nie relácia kongruencie. Dombek upozorňuje na odlišný zápis modulo v zátvorke pri neskoršej kongruencii. [Definice 6.2, chap-06-tc.woo:444; dml-P7-par1-dombek 07:37–08:57]
- $\gcd(a,b)$ a $\operatorname{lcm}(a,b)$: „největší společný dělitel“, „nejmenší společný násobek“; „nesoudělná“ znamená $\gcd(a,b)=1$. [Definice 6.3, chap-06-tc.woo:458]
- V celom preverovanom `chap-06-tc.woo`, `P05_tc.tex` a `anki-dml.txt` sa reťazce NSD/NSN nevyskytujú. Písomný štandard je vypísaný názov a gcd/lcm; neprítomnosť reťazca nie je dôkaz zákazu jeho vyslovenia.
- Dombek 09:36: „Značení GCD z angličtiny vy možná znáte NCD z češtiny. Tak GCD od teďka.“ (nejasné: „NCD“ je presný prepis, pravdepodobne NSD). [dml-P7-par1-dombek 09:36]
- Dombek zároveň povie „to, co znáte jako NSN“, potom zavádza least common multiple. České skratky teda spomenie ako školské východisko, ale ďalej používa gcd/lcm. Netvrdiť, že ich nikdy nevysloví. [dml-P7-par1-dombek 12:16–12:48]
- Spěvák pomenúva GCD ako greatest common divisor a LCM ako least common multiple. [dml-P6-par2-spevak 59:34; 1:01:13]
- Legerský používa GCD a nesúdeliteľnosť nazve aj „co-prime“; samostatný zákaz NSD/NSN v jeho titulkoch zdroj nemá. [dml-P8-par3-legersky 20:20; 25:41]
- EA, REA aj EEA sú kurzové skratky; REA/EEA sú rovnocenné české/anglické označenia rozšíreného algoritmu. [Věta 6.5, chap-06-tc.woo:771; dml-P7-par1-dombek 38:04–38:09]
- Bézout: „Bézoutova rovnost (identita)“, „Bézoutovy koeficienty“; čítať približne „bezu“, koeficienty nie sú jednoznačné. [chap-06-tc.woo:749–756; dml-P7-par1-dombek 37:35–37:42; dml-P7-par2-spevak 16:52–17:22]
- LDR, „partikulární řešení“, „přidružená homogenní rovnice“, „kanonický/prvočíselný rozklad“, „faktorizace“ sú terminológia skrípt. [Definice 6.4, chap-06-tc.woo:1071; Definice 6.5, chap-06-tc.woo:1304; Věta 6.10, chap-06-tc.woo:1519]

### 6. Čo kto zdôrazňuje, chce a zakazuje

- Dombek | „Věta o dělení se zbytkem je jedna ze zkouškových otázek.“ Nestačí $a=dq+r$ bez množín, kvantifikátorov, existencie/jednoznačnosti a rozsahu zvyšku. [dml-P6-par1-dombek 1:25:13–1:26:51]
- Dombek | „tady ta definice, berte tuhle“: gcd/lcm formulovať cez deliteľnosť ďalších spoločných deliteľov/násobkov. [dml-P7-par1-dombek 13:30]
- Dombek | „Tak GCD od teďka.“ Doložená preferencia označenia, nie doložená bodová sankcia za NSD. [dml-P7-par1-dombek 09:36]
- Dombek | „Ale to je řekněme nepovinné.“ Hovorí o samostatnom zapisovaní kvocientu, nie o vypustení koeficientov REA. [dml-P7-par1-dombek 49:55–50:05]
- Dombek | „A občas odečítáte zápodné číslo. Tam se často chybuje.“ (nejasné: „zápodné“ v prepise znamená záporné). [dml-P7-par1-dombek 54:22]
- Spěvák | „Setkáte se s ním nejspíš i ve zkouškový písemce“: REA treba ovládať, bude na cvičeniach. Ide o očakávanie prednášajúceho, nie záruku konkrétneho testu. [dml-P6-par2-spevak 1:31:31]
- Spěvák | „tak ani nemusíte provádět ten rozšířený“: keď riešenie/identitu vidno, možno použiť uhádnutú dvojicu. [dml-P7-par2-spevak 17:22]
- Spěvák | „formulujete základní větu aritmetiky“: poznať názov a presné znenie vrátane jednoznačnosti. [dml-P7-par2-spevak 54:15–55:38]
- Legerský | „No určitě musíte vědět, jak běží ten algoritmus.“ O základnom EA; pri D by dôkaz neskúšal, pri ambícii na A sa naň môže niekto spýtať. [dml-P8-par3-legersky 46:59–47:34]
- Legerský | „Ale určitě ne bez změní.“ (nejasné: „změní“ znamená znenia). Nezovšeobecniť jeho miernejšie vyjadrenie o dôkaze EA na zákaz učiť dôkazy alebo na všetky paralelky. [dml-P8-par3-legersky 47:34]
- Legerský | „Nemá smysl pustit jenom ten základní a pak znovu pouštět rozšířený.“ Pri riešení LDR rovno zbierať Bézoutove koeficienty. [dml-P8-par3-legersky 1:18:50]
- Legerský | „tak nedostanete všechno ta řešení“: pri neskrátenom homogénnom smere $(b,-a)$ pre súdeliteľné $a,b$ chýbajú riešenia. [dml-P8-par3-legersky 1:37:40]
- Proseminár | „Tady opravdu budeme připokládat, že A je kladné.“ Podmienka pre $b\bmod a$; chybne/neúplne zadaný obor sa musí opraviť. [dml-prosem2526-06 00:46]
- Skriptá | „Pozor na pořadí $a,b$!“ Pri tabuľke aj matici je poradie koeficientov súčasť významu výsledku. [chap-06-tc.woo:914; 963]

Zdroje nedokladajú všeobecný zákaz maticového REA, tabuľku ako jediný povolený formát ani zákaz uhádnuť partikulárne riešenie.

### 7. Typické úlohy

Frekvencie sú počty v dodanom inventári a cvičeniach, nie odhad pravdepodobnosti na budúcej skúške; prednáškové otázky nepočítam ako nezávislé testové výskyty.

| Typ | Krátky príklad zadania po česky | Odkaz | Ako často v dostupnom súbore |
|---|---|---|---|
| Dôkaz vlastností deliteľnosti | „Dokažte … $a\mid a$“, tranzitivitu a antisymetriu na $\mathbb N$. | `dml-cv-06-1`, cv-06-tc.woo:31 | 3 podúlohy. |
| Dôkaz/vyvrátenie, krátenie | „Jestliže $ac\mid bc$ a $c\ne0$, potom $a\mid b$.“ | `dml-cv-06-2`, cv-06-tc.woo:79 | 4 podúlohy; všetky proseminár. |
| Deliteľnosť výrazu pre všetky celé čísla | „Dokažte … součin tří po sobě jdoucích celých čísel je dělitelný šesti.“ | `dml-cv-06-3`, cv-06-tc.woo:133; `dml-cv-06-4`, :201 | 1 + 6 podúloh; proseminár 07:06–12:39. |
| gcd, Bézout, lcm | „Najděte $\gcd(a,b)$ … nalezněte Bézoutovy koeficienty.“ | `dml-cv-06-5`, cv-06-tc.woo:321 | 3 dvojice vrátane záporných vstupov; 2 rieši proseminár. |
| Dôkazy o gcd a nejednoznačnosti koeficientov | „Dokažte…“ | `dml-cv-06-6`, `dml-cv-06-7`, cv-06-tc.woo:510; :596 | 4 + 2 podúlohy. |
| LDR s/bez riešenia | „Řešte diofantickou rovnici“ $2x+4y=3$, $2x+4y=6$. | `dml-cv-06-8`, cv-06-tc.woo:649 | 5 základných; ďalších 12 v `dml-cv-06-29`, :2925. |
| Slovná LDR | „Lze … naměřit interval 7 minut?“ Hodiny 11 a 5 minút. | `dml-cv-06-9`, cv-06-tc.woo:780 | 2 evidované časti: konštrukcia a všetky spôsoby. |
| Nezáporné riešenia a dodatočné podmienky | „kolik buřtů a klobás … utratit celou částku“ | `dml-cv-06-10`, cv-06-tc.woo:892 | 2 časti; 2 ďalšie v násobenej verzii `dml-cv-06-11`, :1023. |
| Odhad počtu prvočiniteľov | „obsahuje nejvýše $\lceil\log_2 n\rceil$ činitelů“ | `dml-cv-06-12`, cv-06-tc.woo:1032 | 1 dôkaz; $n\ge2^k$ je kľúč. |
| Najmenší kladný Bézoutov koeficient | „pro něž má $B$ minimální kladnou hodnotu“ pre $(512,320)$. | `dml-rozstrel-2023-01-11-8`, testy-rozstrel.md:14 | 1 priamy rozstrelový výskyt. |
| REA ako súčasť inverzie | Najmenší modul pre inverziu čísla 420, potom vypočítať inverziu. | testy-dml-2.md:9; `dml-t2-2024-n-3`; vzor `dml-t2-2025-vzor-3` | Text má reálnu E-3/2025 a N-3/2024, plus vzor s 630; samotná inverzia patrí 6b. |

Rozstrelovú minimalizáciu riešiť cez všetky riešenia $512A+320B=64$: napr. $A=2+5k$, $B=-3-8k$, takže minimálne kladné $B=5$ dá $A=-3$.
Toto je kontrolný výpočet výťahu, nie citované vzorové riešenie; priamym dosadením $-3\cdot512+5\cdot320=64$ a $B\equiv5\pmod8$ sa overí aj minimalita. [zadanie testy-rozstrel.md:14; Věta 6.8, chap-06-tc.woo:1248]
Záznamy `TČ2` obsahujú aj modulárne úlohy za hranicou 1676, napr. `dml-cv-06-17` či `dml-skripta-tc-2357`; samotná hodnota `chapter` teda nestačí na zaradenie do 6a.
Samostatná LDR ani dôkaz prvočíselnej vety sa v dodanom prepise 2. testov ako jednoznačne doložená priama úloha nenašli; nevydávať ich za pravidelný príklad č. 3.
Otvorené kvízy MARAST: pre túto časť zdroj nemá samostatný obsah; rozstrel je archív skúškových otázok, nie otvorený tréningový kvíz. Žiadne otvorené znenie sa nepreberá.

### 8. Časté chyby a otázky (Discord, prednášky)

- **$n\mid0$ nie je delenie nulou.** Dombek spomína, že to musel so študentmi vyjasňovať aj pri skúške: relácia nič nevypočítava. Z definície platí aj $0\mid0$. [dml-P6-par1-dombek 1:19:20–1:20:08; Definice 6.1, chap-06-tc.woo:240]
- **Holé $a=dq+r$ nie je formulácia vety.** Chýba, čo je dané, čo existuje, z ktorých množín, jednoznačnosť a interval zvyšku. [dml-P6-par1-dombek 1:25:17–1:26:51]
- **Záporný zvyšok nie je zvyšok kurzu.** Pri zápornom $a$ treba upraviť podiel a dostať $0\le r<d$. [dml-P6-par1-dombek 1:33:26–1:34:50]
- **Znamienka v REA.** Odčítanie záporného čísla je opakovaná numerická chyba; používať tú istú riadkovú operáciu pre $r,\alpha,\beta$. [dml-P7-par1-dombek 52:39–54:22]
- **Gcd kladných vstupov vs. koeficienty pôvodných vstupov.** Absolútne hodnoty zachovajú gcd, ale nie bez opravy znamienok Bézoutovu identitu. [dml-prosem2526-06 26:03–27:23]
- **Všetky riešenia LDR nie sú $k(b,-a)$ bez krátenia.** Pre súdeliteľné koeficienty sa vynechá časť riešení; parameter musí byť celý a spoločný pre obe súradnice. [dml-P8-par3-legersky 1:37:40; dml-prosem2526-06 40:39–42:14]
- **Prvočíselná podmienka nie je ozdoba.** $12\mid3\cdot4$, ale $12\nmid3$ a $12\nmid4$; Eukleidovo lemma sa takto na všeobecný deliteľ použiť nedá. [dml-P7-par2-spevak 49:09–49:39]
- **Súčin prvočísel plus 1 nemusí byť prvočíslo.** Spěvák to výslovne povie; stačí jeho nový prvočíselný deliteľ. [dml-P7-par2-spevak 45:58–46:51]
- Discord k týmto konkrétnym chybám v dodanom súbore zdroj nemá; `discord-dml.md` je neúplný výskum siedmich zistení, nie kompletný archív diskusie. [discord-dml.md:6]
- Teove otázky: dôkaz Lemmy 6.1 **existuje** v skriptách aj na slajde a prednáškach; NSD/NSN nie sú písomný štandard; symbol $\nmid$ patrí do Definice 6.1, ale hodnotenie úzkej otázky nemá automaticky vyžadovať celý sprievodný slovník. Posledné je požiadavka na appku, nie citovaný pokyn vyučujúceho. [chap-06-tc.woo:240; 563; P05_tc.tex:319–344]

### 9. Rozdiely medzi paralelkami

- **Dombek:** $(254,158)$, dôkaz REA silnou indukciou rozpisuje na tabuli, tabuľku preferuje, podiel je voliteľný. Pri definíciách silno trvá na kvantifikátoroch a kurzovej definícii gcd/lcm. [dml-P7-par1-dombek 43:21–49:29; 49:47–55:41; dml-P6-par1-dombek 1:25:17–1:26:51]
- **Spěvák:** iný úvodný príklad $(258,34)$, EA najprv ukáže a až potom dokazuje; REA vysvetlí invariantom, úplný algebraický krok ponechá na premyslenie. LDR vyskladá ako partikulárne plus homogénne riešenia. [dml-P6-par2-spevak 1:05:44–1:12:47; 1:26:10–1:27:07; dml-P7-par2-spevak 24:34–34:00]
- **Legerský:** pri REA prejde od plných rovníc k tabuľke, maticu odkáže do skrípt; existenciu delenia so zvyškom robí až v P8. Pri Lemme 6.2/1 použije vydelenie Bézoutovej identity gcd, nie argument maximálnosti spoločného deliteľa zo skrípt. [dml-P7-par3-legersky 1:38:10; dml-P8-par3-legersky 55:59–59:17; 1:24:05–1:25:24]
- **Prvočísla:** v zadanom balíku ich dokončí len Spěvák; jeho dôkaz nekonečnosti je konštruktívny, skriptový je sporem; jednoznačnosť faktorizácie robí indukciou podľa počtu prvočiniteľov, skriptá cez najmenší protipríklad. [dml-P7-par2-spevak 45:11–48:53; 57:19–1:01:46; chap-06-tc.woo:1426; 1544]
- **Proseminár:** $(420,231)$ a záporné vstupy, dôraz na význam koeficientov; pri LDR názorne porovná uhádnuté a vypočítané partikulárne riešenia. [dml-prosem2526-06 17:01–30:10; 31:25–33:54]

### 10. Nejasné a chýbajúce

1. **Obraz tabule/slajdu a zvuk:** zdroj nemá v tomto čítaní; pri formuláciách „tento stĺpec“ pomáhajú skriptá a TeX, nie nezávislé vizuálne overenie.
2. **Dombek NSD:** prepis v 09:36 má „NCD“; významová identifikácia s NSD je pravdepodobná, fonetické overenie chýba. NSN v 12:16 je zachytené priamo.
3. **Spěvák $42x+70y=266$:** 38:35–39:17 správne odvodí $14=-70+2\cdot42$ a $266=-19\cdot70+38\cdot42$, ale 40:13–40:43 nadiktuje súradnice v opačnom poradí. Pre rovnicu v poradí $42x+70y$ je správne $(38,-19)+k(5,-3)$; hovorené $(-19,38)$ nedáva pravú stranu 266. Nejasné: chyba reči, prepisu alebo orientácie na tabuli; obraz chýba. [dml-P7-par2-spevak 38:35–40:43]
4. **Skriptový nulový prípad:** Poznámka 6.1 píše pri $a\ne0$ $\gcd(a,0)=a$ bez absolútnej hodnoty. Pre záporné $a$ to odporuje Definici 6.3 a Lemme 6.1; všeobecne je správne $|a|$, čo Spěvák vysloví. Nepreberať nedopovedaný kladný predpoklad do lekcie. [chap-06-tc.woo:525; dml-P6-par2-spevak 1:08:00]
5. **Anki export:** znenia sú užitočné, ale pri Věte 6.2 končia pri $0\le r$ bez hornej hranice a pri Věte 6.10 sú odrezané podmienky za $p_1$. Plné znenie brať z Woo/TeX. [anki-dml.txt:126; 144; chap-06-tc.woo:377; 1519]
6. **Inventár nie je presný časový rozcestník:** napr. id `dml-lecture-dombek-p8-1555` opisuje obsah z overenej P7; názov/číslo v id nepoužiť ako autoritatívnu nahrávku. Priamy odkaz je tu časovaný `dml-P7-par1-dombek`. [sources.json: id dml-lecture-dombek-p8-1555; dml-P7-par1-dombek 26:07]
7. **Prednášky o prvočíslach u Dombeka/Legerského:** v zadanej dvojici nahrávok chýbajú; nie je to dôkaz, že ich neprebrali neskôr. Neskoršie nahrávky mimo zadanej sady sa tu nesyntetizujú.
8. **Pravidlá skúšania:** doložené sú osobné výroky prednášajúcich, nie jednotná bodovacia rubrika pre Lemmu 6.1, formát tabuľky alebo skratku NSD. Taká rubrika zdroj nemá.
9. **Discord a otvorené MARAST kvízy:** lokálny podklad nedáva úplné pokrytie kapitoly; frekvenciu otázok z celej histórie nemožno určiť.

Overenie citácií: pred odovzdaním znovu skontrolovaných najmenej 10 časových/textových odkazov a čísla tvrdení proti TSV; výpočty Bézoutových identít a uvedené parametrizácie skontrolované dosadením.

### 11. Surové zdroje

Cesty sú lokálne; skrátené názvy v citáciách sa rozvíjajú podľa tohto zoznamu.

- `kontext/BRIEF.md`, pravidlá a osnova; `INDEX.md`, stav titulkov a priradenie paraleliek.
- `kontext/cisla/dml-kap6.tsv`, číslovanie po Tvrzení 6.3 pred riadkom 1677.
- `zdroje/src/bi-dml-master/src/textbook/chap-06-tc.woo:1–1676`.
- `zdroje/src/bi-dml-master/src/textbook/cv-06-tc.woo:27–1066`, navyše `2925–2981` (ďalšie LDR, hoci sú neskôr v súbore).
- `kontext/skripta-dml/bi-dml-master/src/slides/DML_P05_tc.tex:1–10`, iba obal s inputom `P05_tc`.
- `kontext/skripta-dml/bi-dml-master/src/slides/P05_tc.tex:18–1220`, skutočný obsah po prvočísla.
- `kontext/anki-dml.txt:123–145`, definície/vety prvej časti; číslovanie autoritatívne z TSV, nie odhad z exportu.
- `kontext/txt/dml-P6-par1-dombek.txt`, 1:11:49–1:37:35.
- `kontext/txt/dml-P7-par1-dombek.txt`, 06:24–1:38:47.
- `kontext/txt/dml-P6-par2-spevak.txt`, 35:18–1:33:36.
- `kontext/txt/dml-P7-par2-spevak.txt`, 05:29–1:08:23; hranica ďalšej témy 1:09:00.
- `kontext/txt/dml-P7-par3-legersky.txt`, 1:10:07–1:38:10.
- `kontext/txt/dml-P8-par3-legersky.txt`, 09:07–1:38:40.
- `kontext/txt/dml-prosem2526-06.txt`, 00:00–43:15.
- `zdroje/testy-dml-2.md`, celý prepis skontrolovaný; relevantné najmä :9, :17, :27.
- `zdroje/testy-rozstrel.md`, najmä :14, identifikátor `dml-rozstrel-2023-01-11-8`.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`, DML záznamy `chapter=TČ*`, najmä `dml-cv-06-1` až `-12`, `-29`, rozstrel a prednáškové záznamy; modulárne podúlohy oddelené podľa skutočného obsahu.
- `zdroje/discord-dml.md`, preverený výskyt tém a obmedzený rozsah archívu, najmä :6.

## Časť 2: kongruencie, Z_m, inverzia, Fermat a Euler, lineárne kongruencie (chap-06-tc.woo:1677–)

### 1. Rozsah a zdroje

Stav podľa lokálnych podkladov k 17. 11. 2026, doplnený 19. 11. o Spěvákovu 8. prednášku (nižšie). Rozsah prednášok určujú titulky, nie úplnosť slajdov ani prítomnosť úlohy v testoch.
Istota o uvedených časoch a obsahu je vysoká; neisté sú obraz tabule, neúplný prepis Legerského a rozdiely v označení nahrávok.

- **Skriptá:** „Modulární aritmetika“ od riadka 1677: Definice 6.7 až 6.9, Tvrzení 6.4 až 6.6, Pozorování 6.2, Věta 6.11 a 6.12, Důsledek 6.2.
- „Modulární aritmetika: krácení a redukce exponentů“: Věta 6.13 až 6.16, Definice 6.10, Tvrzení 6.7 až 6.11; končí príkladom binárneho mocnenia od riadka 2860.
- „Řešení lineárních kongruencí“ od riadka 2929: Definice 6.11, Věta 6.17 až 6.20, vrátane sústav a ČVOZ/ZČVOZ.
- Čísla sú prevzaté z `cisla/dml-kap6.tsv`; druhy majú vlastné počítadlá. Čísla príkladov tabuľka označuje ako neoverené, preto ich tu používam cez label alebo riadok.
- **Dombek, paralelka 1:** `dml-P8-par1-dombek` od 44:19, súvislý výklad od 44:53 do 1:34:45. Kongruencia, operácie, $\mathbb Z_m$, inverzie, dôkaz existencie a jednoznačnosti cez LDR/REA, potom znenie a príklad krátenia.
- O 1:35:49 výslovne odkladá dôkaz krátenia na nabudúce; vyučovací záver 1:35:55, titulky pokračujú nevecným dozvukom do 1:39:48. [dml-P8-par1-dombek 1:34:45–1:35:55]
- `dml-P7-par1-dombek` túto druhú časť súvislo neprednáša: rozlišuje zápisy mod už 07:24–08:51, inverziu len predznamenáva pri LDR o 1:17:08. Koniec patrí dôkazu všetkých riešení LDR, nie kongruenciám.
- **Spěvák, paralelka 2:** `dml-P7-par2-spevak` 1:09:00–1:32:56, rozlúčka 1:33:00. Kongruencia, ekvivalentné podmienky, vlastnosti, operácie, $\mathbb Z_m$, redukcia výrazov.
- O 1:32:17 vysvetľuje aditívnu inverziu; o 1:32:47 iba upozorňuje, že multiplikatívna nemusí existovať, a o 1:32:56 sľubuje slajd znovu. Celý výklad multiplikatívnej inverzie ani jej algoritmu tým doložený nie je.
- **Spěvák, paralelka 2, 8. prednáška (18. 11.):** `3675538843@sirius.fit.cvut.cz`, titulky od 07:29 (prvých 7 minút bez zvuku) do 1:35:48. Poradie: krátenie so zmenou modulu s dôkazom (07:29–15:32, príklad $1\cdot3\equiv5\cdot3\pmod{12}\Rightarrow1\equiv5\pmod4$), opačný prvok a inverzia v $\mathbb Z_4,\mathbb Z_5,\mathbb Z_{12}$, Věta 6.12 s dôkazom cez Bézouta a jednoznačnosť asociativitou, $\mathbb Z_{10}$, $\mathbb Z_p$ je teleso práve pre prvočíslo (15:32–27:45).
- Ďalej Definice 6.11 a Věta 6.17 v tvare „jediné $x_0\in\{0,\dots,m'-1\}$, $m'=m/\gcd(a,m)$, všetky $x_0+km'$“, dokázaná krátením a inverziou (27:45–35:20). Príklad $12x\equiv18\pmod{75}$: krátenie 3, $4^{-1}\equiv19$ v $\mathbb Z_{25}$ REA tabuľkou $(25,4)$, $x\equiv14\pmod{25}$, v $\mathbb Z_{75}$ tri riešenia (35:20–40:47); znova krátením 6 na $2x\equiv3\pmod{25}$, $2^{-1}=13$ (40:47–43:41).
- $\lcm$ viac čísel (Varování 6.4, 44:51–46:38), „zobecněná čínská věta o zbytcích“ najprv, bez dôkazu (46:38–49:39), ČVOZ ako jej špeciálny prípad, bez dôkazu, vzorec s $M_i$ len ako poznámka (49:39–52:24), paralelné počítanie (Poznámka 6.10, 52:24–54:34).
- Príklady dosadzovaním: Příklad 6.20, $x=23+30u$, posledná kongruencia $0u\equiv0$ nič nepridá, živá chyba „neskrátil jsem ten modul“ (54:34–1:00:42); sústava upravená na $x\equiv2\pmod4$, $x\equiv4\pmod6$, $x\equiv4\pmod{21}$, $\lcm=84$, $x=46+84n$ so skúškou (1:00:42–1:10:22). Pôvodné kongruencie s koeficientmi titulky presne nezachytili.
- Eulerova funkcia, $\varphi(2..6)$ (1:12:55–1:14:55), Eulerova veta a **jej dôkaz** permutáciou zvyškov nesúdeliteľných s $m$ (1:14:55–1:22:39), príklad $3^{10q+1}\bmod22$ s opravou „dvojka je soudělná“. $\varphi(p^\alpha)$ s $\varphi(8),\varphi(27),\varphi(81)$, multiplikativita bez dôkazu s protipríkladom $\varphi(12)\neq\varphi(2)\varphi(6)$, Věta 6.16 a $\varphi(100)=40$ (1:22:39–1:30:42).
- $2^{268}\bmod9=7$ (Příklad 6.14, 1:30:42–1:32:12), MFV ako dôsledok EV aj v tvare $a^p\equiv a\pmod p$ (1:32:12–1:33:37), inverzia ako $a^{\varphi(m)-1}$, $7^{-1}$ v $\mathbb Z_9$ (1:33:43–1:35:18). Na záver $3^{644}\bmod645$: „algoritmus, který si ukážeme příště“ (1:35:29); binárne mocnenie teda ešte nebolo.
- **Dombek, paralelka 1, 9. prednáška (23. 11.):** `3674210640@sirius.fit.cvut.cz`. Eulerova funkcia a veta (~25–46 min), inverzia cez EV „pro ruční počítání úplně nepropagoval“ (46:19). Potom binárne mocnenie: „co dělat, když nejde nic?“, buď základ a modul súdeliteľné, alebo po EV „furt je třeba trojciferný“ exponent, „pomůže metoda square and multiply“ (46:45–47:14); $3^{644}\bmod645$: „Umocni na druhou, pokud jsi přelezl přes modulo, udělej redukci modulo M“, mocniny $3^1,3^2,3^4,\ldots$ po $3^{512}$ (47:49–48:56); zvyšky sa opakujú, ďalší „bez počítání napíšu, že je 66“ (48:56–49:40); dvojkový zápis $644=2^9+2^7+2^2$, súčet v exponente je súčin mocnín (titulky tu skomolené, 49:42–50:44); zhrnutie modulárnej aritmetiky (50:46–51:40). Titulky raz počujú exponent ako 664, prečítané dvojkové číslice dávajú 644. Ďalej lineárne kongruencie a inverzia (~52–69), sústavy a zovšeobecnená čínska veta (~70–87), „tím končí téma teorie čísel“ (87:26).
- **Legerský, paralelka 3:** v dodanom `dml-P8-par3-legersky` nie je súvislý úsek tejto druhej časti. O 1:38:10–1:38:40 dokončuje LDR a piastry; o 1:38:40 oznamuje prvočísla na nabudúce.
- INDEX ho označuje `partial`; titulky majú dozvuk do 1:44:52. Nemožno z toho tvrdiť úplnosť prepisu, no dostupný koniec výučby je stále pred prvočíslami.
- **Proseminár:** `dml-prosem2526-07` 00:00–41:38. MFV a veľké mocniny približne 00:28–07:55, úprava kongruencií a REA 08:55–16:04, ZČVOZ a dosadzovanie 16:12–26:29, mocninové koeficienty a ČVOZ 26:55–41:28.
- Meno prosemináristu zdroj nemá; úvod ho neuvádza. `dml-prosem2526-06` končí o 43:15 pri LDR; nie je pokračovaním výkladu kongruencií.

**Hranica tvorby lekcií (19. 11.):** po Spěvákovej 8. prednáške je odprednášané všetko až po redukciu mocniny s nesúdeliteľným základom: krátenie, lineárna kongruencia, ZČVOZ/ČVOZ s dosadzovaním, $\varphi$, Eulerova veta, MFV a inverzia cez EV. Lekcie `dml-tc-lkon`, `dml-tc-cvoz` a `dml-tc-mocniny` sú podľa nej prerobené a odomknuté.
**Binárne mocnenie (24. 11.):** odprednášal Dombek na 9. prednáške (23. 11.), lekcia `dml-tc-mocnenie` je postavená z nej; Spěvák ho ohlásil na 25. 11. Generátory odvtedy dávajú aj základ súdeliteľný s modulom (`dml-tc-binary`) a zvyšnú mocninu po EV väčšiu než 7, počítanú binárnym mocnením.
**Ešte neodprednášané:** rozklad modulu a ČVOZ pri mocnine (Cvičení 6.25).
Proseminár precvičuje aj neodprednášané; **nenahrádza dôkaz, že boli odprednášané**.

### 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. Rovnosť je niekedy zbytočne presná: stačí vedieť, aký zvyšok dá výsledok. Dombek spája tému s počítačovým počítaním a s $\mathbb Z_5,\mathbb Z_7$ v LA1. [dml-P8-par1-dombek 44:53–45:57]
2. Definice 6.7 nahrádza rovnosť vzťahom $m\mid(a-b)$. Spěvák motivuje modulo 2 paritou a modulo 3 tromi možnými zvyškami. [dml-P7-par2-spevak 1:09:15–1:10:33]
3. Tvrzení 6.4 spája deliteľnosť rozdielu, rovnosť s pridaným $km$ a rovnosť zvyškov. Dombek dôkaz opiera o rozdiel dvoch zvyškov, Spěvák dokazuje cyklus implikácií. [dml-P8-par1-dombek 49:01–55:11; dml-P7-par2-spevak 1:12:22–1:18:04]
4. Tvrzení 6.5 a 6.6 umožnia kongruencie reťaziť a nahrádzať členy pri sčítaní, odčítaní a násobení. Mocnenie rovnakým prirodzeným exponentom sa odvodí indukciou; zatiaľ sa nemení exponent. [dml-P8-par1-dombek 55:41–1:03:38]
5. Definice 6.8 zavádza operácie vracajúce výsledok do $\mathbb Z_m$. Dombek vysvetľuje zákony Věty 6.11 prenosom z celých čísel cez zvyšky; Spěvák viac času venuje rozdielu funkcie mod a relácie kongruencie. [dml-P8-par1-dombek 1:04:08–1:15:33; dml-P7-par2-spevak 1:24:15–1:28:50]
6. Definice 6.9 oddeľuje opačný a inverzný prvok. Dombek skúša trojku v $\mathbb Z_5,\mathbb Z_4,\mathbb Z_{12}$: pri zloženom module niektoré inverzie existujú a iné nie. [dml-P8-par1-dombek 1:15:47–1:19:20]
7. Věta 6.12 premieňa otázku inverzie na LDR $ax+my=1$. Z riešiteľnosti dostaneme $\gcd(a,m)=1$, z REA kandidáta a z kroku $m$ jediné riešenie v $\mathbb Z_m$. [dml-P8-par1-dombek 1:19:26–1:26:51]
8. Počítanie v celých číslach s $\equiv$ dovoľuje aj záporné pohodlné reprezentanty. Dombek zdôrazňuje, že výsledok nazvaný redukcia už má patriť do $\{0,\ldots,m-1\}$. [dml-P8-par1-dombek 1:28:20–1:31:21]
9. Dve hranice voľného nahrádzania: nemožno bez podmienok krátiť a nemožno redukovať exponent rovnakým modulom ako základ. Věta 6.13 opravuje krátenie zmenou modula; dôkaz ostáva na nabudúce. [dml-P8-par1-dombek 1:31:51–1:35:49]
10. **Spěvák, 8. prednáška:** krátenie so zmenou modulu je kľúč ku všetkému ďalšiemu („tuto větu budeme hodněkrát využívat“). Lineárna kongruencia sa rieši ako lineárna rovnica, len „vydeliť“ znamená vynásobiť inverziou. Sústava má jednu neznámu a rôzne moduly; všeobecná veta (ZČVOZ) ide pred ČVOZ, rieši sa dosadzovaním. Eulerova veta (s dôkazom) zmenší veľkú mocninu, MFV je jej dôsledok; súdeliteľný základ až nabudúce. [Spěvák P8 07:29–15:32; 40:24–41:17; 46:38–51:25; 1:14:55–1:35:48]
11. **Proseminár** precvičuje MFV/EV, REA pri koeficiente a ZČVOZ s dosadzovaním až k mocninovým koeficientom. [dml-prosem2526-07 00:37–02:03; 11:39–16:32; 26:55–41:28]

### 3. Pojmy a tvrdenia

V stĺpci dôkaz „nedoložené“ znamená nedoložené v dodaných prednáškach, nie zákaz dôkazu na skúške.
Pri definíciách sa dôkaz neuplatňuje; pri budúcom učive sa rozlišuje vyslovenie v proseminári a prednáška.
Odkazy E, K, A a rozstrel sú konkrétne lokálne doklady testového významu, nie odhad budúcich testov.

| Číslo | Druh | Názov | Jadro jednou vetou | Dôkaz v skriptách; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 6.7 | Definice | Kongruence modulo $m$ | $a\equiv b\pmod m$ znamená $m\mid(a-b)$. | Definícia; Dombek 46:08–47:01. | Základ všetkých úloh, napr. [testy-dml-2.md:36]. |
| 6.4 | Tvrzení | Vztahy ekvivalentní kongruenci | Kongruencia je ekvivalentná rovnosti zvyškov aj $a=b+km$. | Áno, :1751; Dombek 50:33–55:11, Spěvák 1:12:22–1:18:04. | Prevod na parameter pri sústavách [testy-dml-2.md:36]. |
| 6.5 | Tvrzení | Vlastnosti kongruence | Relácia je reflexívna, symetrická a tranzitívna. | Áno, :1805; Spěvák 1:18:25–1:20:37 vysvetľuje jednotlivé vlastnosti. | Podklad správnych úprav [testy-dml-2.md:14]. |
| 6.6 | Tvrzení | Zachování operací | Súčet, rozdiel, súčin a prirodzené mocniny kongruentných základov ostanú kongruentné. | Áno, :1885; Dombek 59:19–1:00:58 súčin, 1:03:38 indukciu necháva študentom. | Redukcie v K-3 [testy-dml-2.md:14]. |
| 6.8 | Definice | Množina zbytků a operace | $\mathbb Z_m=\{0,\ldots,m-1\}$ a operácie končia redukciou. | Definícia, :1965; Spěvák 1:26:13–1:28:13. | Inverzia v $\mathbb Z_m$ [testy-dml-2.md:9]. |
| 6.2 | Pozorování | Operace na $\mathbb Z$ a $\mathbb Z_m$ | Zvyšky možno redukovať už počas výpočtu bez zmeny konečného zvyšku. | Samostatný blok dôkazu nie, :1993; Dombek vysvetľuje 1:05:08–1:09:31. | Medzivýpočty mocnín [testy-dml-2.md:14]. |
| 6.11 | Věta | Algebraické vlastnosti operací | Platia uzavretosť, komutativita, asociativita, distributivita, neutrálne a opačné prvky. | Áno, :2115; Dombek 1:10:04–1:15:33, ukážka asociativity a opačných prvkov. | Podklad, nie samostatná úloha v čítanej vzorke. |
| 6.9 | Definice | Inverze v $\mathbb Z_m$ | Opačný prvok dáva súčet 0, multiplikatívna inverzia súčin 1. | Definícia, :2137; Dombek 1:15:47–1:16:47. | Priamo E-3 [testy-dml-2.md:9]. |
| 6.12 | Věta | Existence multiplikativní inverze | Inverzia existuje práve pri $\gcd(a,m)=1$ a v $\mathbb Z_m$ je jediná. | Áno, :2233; Dombek 1:19:26–1:26:51 cez LDR. | Priamo E-3, výber najmenšieho modula. |
| 6.2 | Důsledek | Všechny nenulové inverze | Všetky nenulové prvky sú invertibilné práve pre prvočíselný modul. | Odôvodnenie :2264; Dombek 1:22:12–1:23:12 najmä smer pre prvočíslo. | E-3 nevyžaduje prvočíselný modul, iba nesúdeliteľnosť. |
| 6.13 | Věta | O krácení v modulu | $ac\equiv bc\pmod m\Leftrightarrow a\equiv b\pmod{m/\gcd(m,c)}$. | Áno, :2433; Spěvák P8 dôkaz 09:09–14:52 (aj $c=0$); Dombek znenie/príklad 1:34:09–1:34:45, dôkaz odložený 1:35:49. | Úprava sústavy A-3 [testy-dml-2.md:36]. |
| 6.14 | Věta | Malá Fermatova věta | Pre prvočíslo $p$ a $p\nmid a$ platí $a^{p-1}\equiv1\pmod p$. | Áno, :2483; na prednáške nedoložené, proseminár znenie 00:37–01:05. | Mocninová veža K-3b; [testy-dml-2.md:14]. |
| 6.7 | Tvrzení | Inverze pomocí MFV | Inverzia nenulového $a\in\mathbb Z_p$ je zvyšok $a^{p-2}$. | Áno, :2529; prednáška nedoložené. | Cvičný variant [dml-skripta-tc-2545]; priamy test touto metódou zdroj nemá. |
| 6.10 | Definice | Eulerova funkce | $\varphi(n)$ počíta $k\in\mathbb N$, $k\leq n$, nesúdeliteľné s $n$. | Definícia, :2566; prednáška nedoložené, proseminár výpočet 29:16–30:09. | Podmienky viet [testy-rozstrel.md:17]. |
| 6.15 | Věta | Eulerova věta | Pri $\gcd(a,m)=1$ platí $a^{\varphi(m)}\equiv1\pmod m$. | Nie, :2663 „Vynecháváme a nevyžadujeme.“; Spěvák P8 ju dokázal 1:17:57–1:22:39. | K-3a a rozstrel [testy-dml-2.md:14; testy-rozstrel.md:17]. |
| 6.8 | Tvrzení | $\varphi$ na prvočíslech | $p$ je prvočíslo práve vtedy, keď $\varphi(p)=p-1$. | Áno, :2680; prednáška nedoložené. | Teória [testy-rozstrel.md:17]. |
| 6.9 | Tvrzení | $\varphi$ na mocninách prvočísel | $\varphi(p^\alpha)=p^\alpha-p^{\alpha-1}$. | Áno, :2716; prednáška nedoložené, proseminár používa 29:23–29:38. | Pomocný výpočet veľkých mocnín [testy-dml-2.md:14]. |
| 6.10 | Tvrzení | Multiplikativita $\varphi$ | $\varphi(mn)=\varphi(m)\varphi(n)$ pri $\gcd(m,n)=1$. | Nie, :2750 dôkaz nevyžaduje; prednáška nedoložené, proseminár 33:04. | Výpočet modula exponentu [testy-dml-2.md:14]. |
| 6.16 | Věta | Výpočet $\varphi$ faktorizací | Pre rozklad na rôzne prvočísla $\varphi(m)=m\prod_{p\mid m}(1-1/p)$. | Áno, :2768; prednáška nedoložené. | K-3a, R-3a [testy-dml-2.md:14; :25]. |
| 6.11 | Tvrzení | Inverze pomocí EV | Inverzia je zvyšok $a^{\varphi(m)-1}$ pri nesúdeliteľnosti. | Áno, :2837; prednáška nedoložené. | Cvičný variant [dml-skripta-tc-2846]. |
| 6.11 | Definice | Úloha lineární kongruence | Nájsť všetky celé $x$ s $ax\equiv b\pmod m$. | Definícia, :2933; úplný prednáškový výklad nedoložený. | A-3 [testy-dml-2.md:36]. |
| 6.17 | Věta | Řešení lineární kongruence | Riešiteľnosť je $\gcd(a,m)\mid b$, krok všetkých riešení je $m/\gcd(a,m)$. | Áno, :2966; prednáška nedoložené, proseminár rieši príklady 09:08–15:29. | A-3, príprava jednotlivých kongruencií. |
| 6.18 | Věta | Řešení v $\mathbb Z_m$ | Ak je riešiteľná, má presne $\gcd(a,m)$ rôznych riešení v $\mathbb Z_m$. | Áno, :3064; prednáška nedoložené. | Počet riešení [testy-rozstrel.md:31]. |
| 6.19 | Věta | Čínská věta o zbytcích | Pre po dvoch nesúdeliteľné moduly je jedno riešenie modulo ich súčin. | Áno, :3206 konštrukcia; prednáška nedoložené, proseminár použitie 35:43–36:13. | [dml-skripta-tc-3271], sústavy v testoch. |
| 6.20 | Věta | Zobecněná Čínská věta o zbytcích | Nutné a postačujúce je $\gcd(m_i,m_j)\mid(a_i-a_j)$ pre všetky dvojice; periódou je lcm. | Nie, :3362 „Vynecháváme a nepožadujeme.“; Spěvák P8 znenie 46:38–49:39, dôkaz „přeskakujeme“; proseminár 16:12–18:14. | A-3 po úpravách, rozstrel [testy-dml-2.md:36; testy-rozstrel.md:31]. |

Riadky označené iba `:číslo` v stĺpci dôkaz odkazujú na `chap-06-tc.woo`.
Časy bez názvu prednášky v tabuľke patria menovanému Dombekovi (`dml-P8-par1-dombek`) alebo Spěvákovi (`dml-P7-par2-spevak`).
Anki potvrdzuje znenia a označenia definícií, tvrdení a viet v tomto rozsahu; nie je dôkazom, že sa už vyučovali. [anki-dml.txt:146–169]

### 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Výraz modulo $m$: odprednášané u Dombeka aj Spěváka.**

1. Pri každom člene nájsť pohodlné kongruentné číslo, pokojne záporné.
2. Zachovať $\equiv$ a platný modul; násobiť, sčítať a priebežne znovu redukovať.
3. Ak otázka žiada zvyšok, posledný reprezentant musí byť v $\{0,\ldots,m-1\}$.
Spěvák používa $241\cdot242\equiv(-2)(-1)\equiv2\pmod{243}$ a výraz $59\cdot302+45\cdot243$ modulo 30.
Ten dáva $(-1)2+15\cdot3\equiv13\pmod{30}$. [dml-P7-par2-spevak 1:29:25–1:31:49]
Opora: Tvrzení 6.6 a Pozorování 6.2. [chap-06-tc.woo:1854; :1993]

**B. Inverzia cez REA: odprednášané u Dombeka, konkrétna tabuľka v proseminári.**

1. Zadať aj prostredie: hľadám $a^{-1}$ v $\mathbb Z_m$, teda $ax\equiv1\pmod m$.
2. Previesť na $ax+my=1$. Otestovať $\gcd(a,m)=1$; ak neplatí, inverzia neexistuje.
3. REA spočíta Bézoutove koeficienty. Vybrať koeficient pri **$a$**, nie automaticky prvý či druhý stĺpec.
4. Tento koeficient $x_0$ redukovať modulo $m$; konečný zápis je $a^{-1}=x_0\bmod m$ v uvedenom $\mathbb Z_m$.
5. Overiť, že súčin s $a$ dáva zvyšok 1. Všetky celé riešenia pomocnej kongruencie sú $x=x_0+km$.
Takto Dombek odvodzuje existenciu aj jednoznačnosť, bez samostatného numerického veľkého príkladu. [dml-P8-par1-dombek 1:19:26–1:26:51; Věta 6.12, chap-06-tc.woo:2221]

Proseminár pre $7^{-1}$ modulo 60 dáva modul ako prvý vstup, teda dvojicu $(60,7)$.
Začiatočné riadky možno zapísať $(r,\alpha,\beta)=(60,1,0),(7,0,1)$, s invariantom $r=60\alpha+7\beta$.
Ďalšie sú $(4,1,-8),(3,-1,9),(1,2,-17)$; ide o odčítanie príslušného násobku predchádzajúceho riadka od predpredchádzajúceho.
Z posledného nenulového riadka $1=2\cdot60-17\cdot7$ plynie $7^{-1}=43$ v $\mathbb Z_{60}$.
Slúpec koeficientu pri 60 netreba pre samotnú inverziu dopočítavať; pri 7 áno. [dml-prosem2526-07 11:47–13:50]
Tabuľka tu je rekonštrukcia z vyslovených krokov a invariantov REA, nie prepis obrazu tabule. Opora: Věta 6.5 a dôkaz Věty 6.12. [chap-06-tc.woo:771; :2233]

Dombek pri malých moduloch pripúšťa skúšanie/pamäť, pri väčších ukazuje význam REA. [dml-P8-par1-dombek 1:27:25–1:27:49]
Skriptá ručné skúšanie malých inverzií považujú za korektné; pri väčších ho dôrazne neodporúčajú. [cv-06-tc.woo:1213–1220]

**C. Najmenší prípustný modul a inverzia: doložený testový typ.**

Pre $a=420$, $m\geq422$ hľadať najmenšie $m$ s $\gcd(420,m)=1$, nie najbližšie prvočíslo.
Postupné kandidáty vyradia spoločné faktory s $420=2^2\cdot3\cdot5\cdot7$; $m=431$ je prvý prípustný.
REA dá $1=-196\cdot420+191\cdot431$, teda $420^{-1}=235$ v $\mathbb Z_{431}$.
Toto je kontrolný výpočet k zadaniu, nie tvrdenie, že ho Dombek na nahrávke riešil. [testy-dml-2.md:9; dml-t2-2024-n-3; Věta 6.12, chap-06-tc.woo:2221]

**D. Krátenie: Dombek zatiaľ vyslovil pravidlo a príklad, dôkaz nie.**

Pri $ac\equiv bc\pmod m$ vypočítať $d=\gcd(m,c)$ a písať $a\equiv b\pmod{m/d}$.
Ak $d=1$, modul sa nemení. Ak je spoločný faktor väčší, nemožno ho vynechať z úpravy modula.
Dombek: $3x\equiv9\pmod{15}\Leftrightarrow x\equiv3\pmod5$; $x=8$ ukazuje, prečo by modulo 15 po krátení bolo zlé.
[dml-P8-par1-dombek 1:32:21–1:34:45; Věta 6.13, chap-06-tc.woo:2420]
Proseminár: $10k\equiv8\pmod{36}\Leftrightarrow5k\equiv4\pmod{18}$. [dml-prosem2526-07 19:29–20:39]

**Postupy E, F, H, I a dosadzovanie z J odprednášal Spěvák na 8. prednáške (18. 11.); G (binárne mocnenie, súdeliteľný základ) Dombek na 9. prednáške (23. 11.); rozklad modulu ešte nikto.** Spěvák pri E počíta zvyšnú malú mocninu priamo ($2^4=16\equiv7$); pri H kráti $\gcd(a,m)$ alebo aj väčším spoločným deliteľom oboch strán, vždy s modulom $m/\gcd(m,c)$, a inverziu nájde REA alebo z hlavy; pri I a J najprv overí podmienku pre každú dvojicu, potom dosadzuje a robí skúšku.

**E. Veľká mocnina pomocou MFV alebo EV.**

1. Najprv overiť prvočíselnosť modula pre MFV a nesúdeliteľnosť základu s modulom pre obe vety.
2. Zvoliť $t=p-1$ pri MFV alebo $t=\varphi(m)$ pri EV; exponent deliť týmto $t$, nie modulom $m$.
3. Napísať $n=qt+r$ a $a^n=(a^t)^q a^r\equiv a^r\pmod m$.
4. Malú mocninu dorátať s priebežnou redukciou, prípadne metódou štvorcov.
Proseminár $3^{33}\bmod11$: $33=3\cdot10+3$, zvyšok je $3^3\bmod11=5$. [dml-prosem2526-07 00:37–02:21; cv-06-tc.woo:1626]
Pri $6^{66}\bmod19$ ostane exponent $12$ a výsledok $7$; medzivýpočty používajú mocniny $2,4,8$. [dml-prosem2526-07 03:25–07:23]
Skriptá ukazujú EV na $2^{268}\bmod9$: $\varphi(9)=6$, $268=44\cdot6+4$, výsledok $7$. [chap-06-tc.woo:2807–2827]
Opora: Věta 6.14 a 6.15, nie samotné Tvrzení 6.6 o mocnení rovnakým exponentom.

**F. Výpočet Eulerovej funkcie a alternatívnej inverzie.**

Rozložiť modul na mocniny rôznych prvočísel, použiť $\varphi(p^\alpha)=p^{\alpha-1}(p-1)$ a súčin pre nesúdeliteľné faktory.
V proseminári $\varphi(28)=\varphi(4)\varphi(7)=(4-2)(7-1)=12$; $\varphi(9)=9-3=6$ sa kontroluje aj spočítaním vhodných čísel.
[dml-prosem2526-07 29:16–30:09; 32:46–33:36; Tvrzení 6.9 a 6.10, chap-06-tc.woo:2705; :2731]
Inverziu možno namiesto REA získať redukciou $a^{p-2}$ alebo $a^{\varphi(m)-1}$, ale len pri splnených podmienkach.
Skriptá napríklad $2^5\equiv5\pmod9$, teda $2^{-1}=5$ v $\mathbb Z_9$. [Tvrzení 6.7 a 6.11, chap-06-tc.woo:2529; :2830–2853]

**G. Mocnina v exponente a prípad nesúdeliteľnosti, ktorá neplatí.**

Pri $A^E\bmod m$ najprv odôvodniť, že stačí $E\bmod\varphi(m)$, potom samostatne vypočítať tento zvyšok.
Vnútorný výpočet má vlastný modul a vlastnú kontrolu podmienok; výsledok z neho je exponentom vonkajšieho výpočtu.
Tak sú zostavené dvojice úloh K-3, R-3 a D-2. [testy-dml-2.md:14; :25; :39; cv-06-tc.woo:1974–1984]
Ak $\gcd(a,m)>1$, EV sa priamo použiť nesmie. Univerzálna alternatíva je binárne mocnenie: vypočítať $a,a^2,a^4,\ldots$ modulo $m$ a vynásobiť faktory podľa binárneho rozvoja exponentu.
[chap-06-tc.woo:2856–2860; cv-06-tc.woo:1679–1696]
Dombek: tabuľka $3^{2^j}\bmod645$ (opakované umocnenie na druhú s redukciou) po najväčšiu mocninu dvojky nanajvýš exponent, potom súčin riadkov pri jednotkách dvojkového zápisu, $111\cdot396\cdot81\equiv36$. Nesúdeliteľnosť netreba, stačí Tvrzení 6.6. [Dombek P9 46:45–50:46; Příklad 6.16]
Iná alternatíva v cvičeniach rozkladá modul na po dvoch nesúdeliteľné faktory a výsledky skladá ČVOZ; nevyhlasovať ju za už odprednášanú. [cv-06-tc.woo:2684–2705]

**H. Jedna lineárna kongruencia.**

Pre $ax\equiv b\pmod m$ určiť $d=\gcd(a,m)$; ak $d\nmid b$, množina riešení je prázdna.
Ak $d\mid b$, cez REA vyriešiť $ax+my=b$ a zobrať $x=x_0+k\,m/d$, $k\in\mathbb Z$.
V $\mathbb Z_m$ vypísať $d$ rôznych zvyškov, napríklad pre $k=0,\ldots,d-1$.
Alternatívne zredukovať koeficienty, skrátiť s príslušnou zmenou modula a násobiť inverziou zostávajúceho koeficientu.
[Věta 6.17 a 6.18, chap-06-tc.woo:2949; :3046; cv-06-tc.woo:2129–2148]
Skriptá ukazujú $9x\equiv12\pmod{15}$ cez LDR aj cez úpravy: $x\equiv3\pmod5$, v $\mathbb Z_{15}$ teda $\{3,8,13\}$.
[chap-06-tc.woo:3084; :3123]
Proseminár vynásobí $3x\equiv1\pmod{10}$ sedmičkou; pri $5x\equiv3\pmod{36}$ využije $35\equiv-1$ a dostane $x\equiv15$.
[dml-prosem2526-07 09:08–11:28]

**I. Sústavám najprv vyriešiť jednotlivé kongruencie.**

ČVOZ sa aplikuje na tvar $x\equiv a_i\pmod{m_i}$, nie bez úprav na ľubovoľné $c_ix\equiv b_i\pmod{m_i}$.
Po krátení pracovať s novými modulmi. Pre po dvoch nesúdeliteľné moduly je výsledný modul ich súčin.
Pri všeobecných moduloch preveriť všetky dvojice $\gcd(m_i,m_j)\mid(a_i-a_j)$, výsledný modul je lcm.
[Věta 6.19 a 6.20, chap-06-tc.woo:3184; :3338; cv-06-tc.woo:2647–2656]

**J. Dva kurzové spôsoby skladania sústavy.**

Konštrukcia zo skrípt: $M=\prod m_i$, $M_i=M/m_i$, nájsť $X_i$ s $M_iX_i\equiv1\pmod{m_i}$.
Potom $x\equiv\sum_i a_iM_iX_i\pmod M$. Každý pomocný člen je 1 vo vlastnom module a 0 v ostatných.
Tento postup je doložený dôkazom ČVOZ v skriptách a slajdoch, nie dodanou prednáškou ani ako hlavný postup proseminára.
[chap-06-tc.woo:3206–3261; P05_tc.tex:2234–2288]

Dosadzovanie z proseminára: z prvej kongruencie napísať $x=a_1+m_1k$, dosadiť do druhej, vyriešiť $k$, dosadiť do ďalšej.
Pri $3x\equiv1\pmod{10}$, $5x\equiv3\pmod{36}$, $7x\equiv9\pmod{60}$ vznikne $x\equiv7,15,27$ v moduloch $10,36,60$.
Kontroly sú $2\mid8$, $10\mid20$, $12\mid12$ a $\operatorname{lcm}(10,36,60)=180$.
Z $x=7+10k$ dostaneme $5k\equiv4\pmod{18}$, teda $k\equiv8\pmod{18}$ a $x=87+180l$.
Tretia podmienka sa zmení na $0l\equiv0\pmod{60}$; neobmedzuje parameter, nie je to dôvod sústavu zahodiť.
[dml-prosem2526-07 16:12–26:23; cv-06-tc.woo:3003–3009]
Dosadzovanie platí aj pri moduloch spĺňajúcich prísnejšiu ČVOZ; kontrolu predpokladov a výsledného modula treba uviesť pri oboch postupoch.
[chap-06-tc.woo:3370–3384]

### 5. Značenie a terminológia

- $a\bmod m$ je číslo, funkčný výstup v $\{0,\ldots,m-1\}$; $a\equiv b\pmod m$ je vzťah dvoch čísel. České názvy: „zbytek“, „kongruence“, „modulo“ aj „modul“. [Definice 6.7, chap-06-tc.woo:1703; dml-P7-par2-spevak 1:24:15–1:25:43]
- Definícia kongruencie dovoľuje $m=1$; bežne sa ďalej uvažuje $m\geq2$. Neprenášať tento neskorší predpoklad naspäť do základnej definície. [chap-06-tc.woo:1707–1719; dml-P8-par1-dombek 46:08–46:28]
- $\mathbb Z_m$, $+_{\!m}$, $\cdot_{\!m}$; skriptá definujú operácie na $\mathbb Z\times\mathbb Z$ s výsledkom v $\mathbb Z_m$, pri vlastnostiach ich obmedzujú na $\mathbb Z_m$. [Definice 6.8, chap-06-tc.woo:1965–1986; :2117]
- $-a$ je „aditivní inverze“, „opačný prvek“; $a^{-1}$ je „multiplikativní inverze“, skrátene „inverze“ či „inverzní prvek“. Bez prívlastku Dombek myslí druhý význam. [chap-06-tc.woo:2137–2164; dml-P8-par1-dombek 1:16:17]
- $\gcd$, $\operatorname{lcm}$; REA aj EEA sú vo Věte 6.5 názvy toho istého rozšíreného algoritmu. Slúpec inverzie určuje koeficient pri invertovanom čísle. [chap-06-tc.woo:771; dml-prosem2526-07 11:47–13:50]
- MFV, EV, $\varphi$, ČVOZ a ZČVOZ sú kurzové názvy; CRT je výslovne uvedená medzinárodná skratka ČVOZ. Netvrdiť, že kurz CRT nepoužíva. [chap-06-tc.woo:2468–2469; :2566; :3184; :3264; P05_tc.tex:2226]
- Binárne mocnenie sa označuje aj „square and multiply“; v cvičeniach „metoda čtverců“. [chap-06-tc.woo:2860; cv-06-tc.woo:2705; dml-prosem2526-07 07:09]
- Doložený zásadný rozdiel v značení medzi Dombekom a Spěvákom zdroj nemá. Obaja odlišujú mod ako funkciu od mod v zátvorke pri kongruencii.
- Celkové tvrdenie, ktoré alternatívne symboly kurz nikdy nepoužíva, zdroj nemá; nemožno ho odvodiť z neprítomnosti v niekoľkých nahrávkach.

### 6. Čo kto zdôrazňuje, chce a zakazuje

- Dombek | „Prosím vás, tady tu syntax musíme dodržovat.“ | Pri zavádzaní $\equiv$ a mod v zátvorke. [dml-P8-par1-dombek 46:40]
- Dombek | „Není to nutné.“ | Odmieta študentské tvrdenie, že už definícia kongruencie musí mať $m\geq2$. [dml-P8-par1-dombek 46:16–46:25]
- Dombek | „Neučte se ty vzorečky naspomnět.“ | V kontexte rôznych rolí písmen v LDR a pri inverzii. [dml-P8-par1-dombek 1:25:29]
- Dombek | „Prostě rovnost a konkurence nejsou to samé.“ | Doslovný automatický prepis má „konkurence“; význam je kongruencia, overený predchádzajúcim výkladom. [dml-P8-par1-dombek 1:29:50]
- Dombek | „zkrácení jen tak v kongruenci není ekvivalentní úprava.“ | Treba sledovať spoločný faktor s modulom. [dml-P8-par1-dombek 1:32:59]
- Dombek | „To je taky blbě.“ | K redukcii exponentu tým istým modulom bez príslušnej vety; následne dáva protipríklad. [dml-P8-par1-dombek 1:33:19–1:33:49]
- Dombek | „No tak jo, tak si to dokážeme příště.“ | Dôkaz krátenia zatiaľ nepovažovať za prebraný. [dml-P8-par1-dombek 1:35:49]
- Spěvák | „Zkuste si to dokázat sami, přímo z definice je to jednoduchý“ | Súčin kongruencií necháva ako cvičenie. [dml-P7-par2-spevak 1:23:31]
- Skriptá | „pro malá modula je i poměrně rychlý“ | Ručné hľadanie inverzie je pri malých moduloch korektné. [cv-06-tc.woo:1216]
- Skriptá | „v testech ji u studentů samozřejmě vidět nechceme“ | Pri riešení kongruencie nechcú skúšanie všetkých prvkov; nie je to všeobecný zákaz všimnúť si inverziu. [cv-06-tc.woo:2135]
- Skriptá | „Jen vždy musíte ověřit předpoklady“ | K ČVOZ/ZČVOZ a uvedeniu modula jednoznačnosti. [chap-06-tc.woo:3384]
- Skriptá | „Vynecháváme a nevyžadujeme.“ | Dôkaz EV; nevyžadovanie sa týka dôkazu, nie znalosti a použitia vety. [chap-06-tc.woo:2663]
- Skriptá | „Vynecháváme a nepožadujeme.“ | Dôkaz ZČVOZ. [chap-06-tc.woo:3362]
- Proseminár | „ale když ji násobíte číslem, který je nesoudělný s tím modulem, tak je to ekvalentní úprava“ | Doslovný prepis; význam „ekvivalentní“. [dml-prosem2526-07 15:52]
- Proseminár | „tak už jsem hotovej, nemusím nic dalšího řešit“ | Len keď tretia podmienka už platí pre každý parameter. [dml-prosem2526-07 25:27]
- Test E-3 | „Své kroky poctivě zdůvodňujte.“ | Pri najmenšom module aj výpočte inverzie. [testy-dml-2.md:9]
- Spěvák | „můžete zkrátit, ale museli byste zkrátit i modul“ | Krátenie kongruencie; „tuto větu budeme hodněkrát využívat“. [Spěvák P8 14:57; 15:32]
- Spěvák | „teďka vidíte chybu v přímém přenosu“ | Pri sústave sám zabudol zmenšiť modul po krátení. [Spěvák P8 58:18–58:26]
- Spěvák | „vždycky potřeba … ověřit“, že základ a modul sú nesúdeliteľné, „bez toho to nefunguje“ | Pred Eulerovou vetou. [Spěvák P8 1:30:42]
- Spěvák | „pozor na to používání multiplicativity“ | $\varphi(12)\neq\varphi(2)\varphi(6)$. [Spěvák P8 1:27:12]
- Spěvák | inverzia cez EV „pro praktické výpočty není moc vhodné“ | Prakticky REA. [Spěvák P8 1:34:47]
- Legerského špecifický zákaz alebo požiadavku k modulárnej aritmetike zdroj nemá, pretože sem ešte nedošiel.

### 7. Typické úlohy

Frekvencia znamená výskyt v dodanom inventári, nie percento reálnych testov. Zhodné zadanie 2024 N/2025 E je jedna inventárna položka, vzor 2025 nie je ďalší uskutočnený test.

| Typ | Príklad zadania po česky | Odkaz | Ako často v čítanej vzorke |
|---|---|---|---|
| Najmenší modul a inverzia | „Nejmenší $m\geq422$, aby mělo $420$ inverzi; vypočítejte ji.“ | [testy-dml-2.md:9; :27; dml-t2-2024-n-3] | 1 inventárna položka pre 2 doložené varianty; navyše 1 vzor s $630$, :17. |
| Dve nadväzujúce mocniny | „Určete $667^{325}\bmod60$ a $917^{667^{325}}\bmod61$.“ | [testy-dml-2.md:14; dml-t2-2025-k-3] | 3 dvojice v testoch: K 2025, R 2024, D 2023; pri D neisté priradenie obrázkov k číslu príkladu. |
| Sústavová úloha a záporný reprezentant | „Všechna řešení $6x\equiv36\ (195)$, $5x\equiv4\ (91)$; největší záporné.“ | [testy-dml-2.md:36; dml-t2-2023-a-3] | 1 testová položka s 2 podotázkami. |
| Samostatná mocnina v rozstrele | „Spočítejte $17^{110}\bmod35$.“ | [dml-rozstrel-2023-01-11-5] | 1 inventárna položka. |
| Pravdivosť formulácií MFV/EV | „Která tvrzení jsou vždy pravdivá?“ | [testy-rozstrel.md:17; dml-rozstrel-2023-01-15-1] | 1 položka s viacerými tvrdeniami; kontrolovať chýbajúcu nesúdeliteľnosť. |
| Počet riešení v intervale | „Vyberte soustavy s právě jedním řešením v $\langle0,120\rangle$.“ | [testy-rozstrel.md:31; dml-rozstrel-2026-01-29-5] | 1 položka so 4 sústavami; čísla v 2. a 4. sú neisté. |
| Inverzie a opačné prvky | „Najděte $a^{-1}$, pokud existuje.“ | [cv-06-tc.woo:1123; :1223; dml-cv-06-14; dml-cv-06-15] | 3 inverzie v prvom bloku a 4 dvojice opačný/inverzný v druhom. |
| Tabuľky operácií | „Tabulky v $\mathbb Z_4,\mathbb Z_5$; řešitelnost rovnic.“ | [dml-cv-06-16] | 1 inventárny blok, tabuľky a 2 teoretické podotázky. |
| Mocnenie vrátane veže | „Vypočtěte $3^{33}\bmod11$, $6^{66}\bmod19$…“ | [cv-06-tc.woo:1616; dml-cv-06-22] | 10 podúloh, z toho 1 veža. |
| Jedna kongruencia | „Řešte $39x\equiv27\pmod{123}$.“ | [cv-06-tc.woo:2087; dml-cv-06-23] | 7 podúloh vrátane neriešiteľnej. |
| ČVOZ a ZČVOZ | „Řešte soustavu kongruencí.“ | [cv-06-tc.woo:2416; dml-cv-06-24] | 5 podúloh, aj spoločné faktory a koeficienty pri $x$. |
| Veľká mocnina rozdelením modula | „Mocnina se soudělným základem a modulem.“ | [cv-06-tc.woo:2684; dml-cv-06-25] | 1 blok. |
| Slovné sústavy | „Výrobky ve skupinách“; „počet vajec“. | [cv-06-tc.woo:2744; :2790; dml-cv-06-26; dml-cv-06-27] | 2 inventárne bloky s 3 podotázkami. |
| Sústavové mocninové koeficienty | „Nalezněte bez hrubé síly všechna řešení.“ | [cv-06-tc.woo:2849; :2992; dml-cv-06-28; dml-cv-06-31] | 2 bloky s odlišnými pravými stranami; nezamieňať ich. |
| Úplnosť riešenia | „Vysvětlete, proč žádná jiná řešení neexistují.“ | [cv-06-tc.woo:3003; dml-cv-06-32] | 1 blok, výpočet a teoretické zdôvodnenie. |

Samostatné presné zadania ústnej skúšky k tejto časti zdroj nemá. Rozstrel je doložený osobitne vyššie.
Overený súbor otvorených MARAST úloh k tejto časti zdroj nemá; ich znenia ani parafrázy sa tu nepoužívajú.

### 8. Časté chyby a otázky (Discord, prednášky)

- Zamieňať $a\bmod m=b$ a $a\equiv b\pmod m$: prvé určuje konkrétny zvyšok, druhé celú triedu čísel. Dombek to spája aj s hodnotením písomiek. [dml-P8-par1-dombek 47:05–47:44; 1:29:50]
- Tvrdiť, že definícia kongruencie vyžaduje $m\geq2$. Dombek uvádza túto odpoveď zo skúšok a opravuje ju. [dml-P8-par1-dombek 46:13–46:28]
- Ponechať záporný Bézoutov koeficient ako prvok $\mathbb Z_m$ bez redukcie. Je to vhodný celočíselný reprezentant, no do stanovenej množiny ešte nepatrí. [dml-P8-par1-dombek 1:24:26–1:24:42]
- Vybrať nesprávny koeficient z REA, lebo sa zmenilo poradie $a,m$. Proseminár výslovne sleduje slúpec pri 7 pri vstupoch $(60,7)$. [dml-prosem2526-07 11:47–12:18]
- Krátiť kongruenciu bez úpravy modula: pri $3x\equiv9\pmod{15}$ by sa stratilo riešenie $x=8$. [dml-P8-par1-dombek 1:32:21–1:32:59]
- Redukovať exponent modulom základu: $2^7\not\equiv2^2\pmod5$, hoci $7\equiv2\pmod5$. V prepisových slovách je exponent miestami skomolený; hodnoty 128 a 4 význam potvrdzujú. [dml-P8-par1-dombek 1:33:19–1:34:01]
- Použiť EV na $5^{1032}$ modulo 45 bez kontroly gcd. Proseminár najprv kráti kongruenciu piatimi na modul 9. [dml-prosem2526-07 27:36–28:56]
- Považovať $0l\equiv0$ za spor alebo jediné $l=0$. V skutočnosti podmienka platí pre všetky celé $l$. [dml-prosem2526-07 24:42–25:27]
- Zameniť jedno riešenie v základnom module s jediným celým riešením alebo jediným v uzavretom intervale. Sústava má triedu $r+Mk$, interval treba skontrolovať vrátane oboch koncov. [Věta 6.19 a 6.20, chap-06-tc.woo:3184; :3338; testy-rozstrel.md:31]
- **Discord k tejto téme:** konkrétne otázky o kongruenciách a Eulerovej vete zdroj nemá. Samotný záznam priznáva, že tieto dotazy neboli prejdené; nejde o dôkaz, že sa študenti nepýtali. [discord-dml.md:18–27; :85; :93]

### 9. Rozdiely medzi paralelkami

Dombek je z dostupných nahrávok najďalej: vysvetľuje inverziu cez LDR/REA aj jednoznačnosť, vracia sa k prostrediu LA1 a končí pred dôkazom krátenia.
Príklady trojky v troch moduloch ukazujú, prečo zložený modul neznamená neexistenciu každej inverzie. [dml-P8-par1-dombek 1:16:47–1:26:51]
Spěvák začína túto časť už na P7, rozoberá paritu a dôkaz ekvivalencie podmienok cyklom; pri úpravách výrazov ukazuje konkrétne redukcie aj záporných reprezentantov.
Na konci P7 ešte len otvára inverzie; na P8 (18. 11.) prebral všetko od krátenia po Eulerovu vetu s dôkazom a predbehol tým ostatné paralelky. [dml-P7-par2-spevak 1:09:00–1:18:04; 1:29:25–1:32:56; Spěvák P8 07:29–1:35:48]
Legerského posledný dodaný vecný koniec je LDR s piastrami a ohlásenie prvočísel. Rozdiel v metóde vysvetlenia kongruencií preto zdroj nemá. [dml-P8-par3-legersky 1:38:10–1:38:40]
Proseminár je spoločný a ide výrazne ďalej: preferuje postupné dosadzovanie, drobné inverzie si všimne, pre väčšie spraví REA.
Konštrukciu cez $M_i,X_i$ ako jeho hlavný spôsob zdroj nedokladá. [dml-prosem2526-07 09:08–13:50; 19:03–26:23]

### 10. Nejasné a chýbajúce

- **Názov a dátum nahrávky:** INDEX a titulky označujú Dombeka ako P8; inventár aj Teove poznámky k appke hovoria pri rovnakom ID `3674210639@sirius.fit.cvut.cz` P9 a spájajú ho so 16. 11. Samotné titulky kalendárny dátum nepotvrdzujú. Citácie tu používajú lokálny názov P8 podľa zadania. [INDEX.md; sources.json, dml-lecture-dombek-p9-5511]
- **Chýbajúci pokračujúci výklad:** P8 paralelky 2 ani ďalšie nahrávky s kompletným výkladom MFV/EV/ČVOZ v INDEX-e nie sú. Presný stav mimo dodaných nahrávok nemožno určiť.
- **Legerský:** stav `partial` je obmedzenie zdroja. Dostupný koniec však výslovne odkladá ešte len prvočísla; nevymýšľať skrytý výklad kongruencií. [dml-P8-par3-legersky 1:38:40]
- **Slajdy:** `DML_P05_tc.tex` je len 10-riadkový obal a na riadku 10 vkladá `P05_tc.tex`. Vlastné citácie obsahu preto patria do tohto vloženého súboru, nie do neexistujúcich vysokých riadkov obalu.
- **Proseminár 07, posledná sústava:** titulky prvú rovnicu zreteľne čítajú s pravou stranou 35 modulo 45 a napokon dávajú $x\equiv8\pmod9$, $x\equiv1\pmod{28}$, teda $x=197+252l$. [dml-prosem2526-07 26:59; 31:51–32:05; 35:25; 41:08]
- Rovnaké exponenty sú v skriptách dvakrát: riešený blok `exe_tc_cvoz_mocniny` má pravé strany 25 a 1, s výsledkom $169\pmod{252}$; neriešený `exe_tc_cvoz_bezres1` má 35 a 4. [cv-06-tc.woo:2853–2854; :2911; :2996–2997]
- Pre znenie 35 a 4 by po redukcii bolo $x\equiv8\pmod9$, $x\equiv4\pmod{28}$ a výsledok $116\pmod{252}$. Overené dosadením; $197$ tomuto druhému zneniu nevyhovuje.
- (nejasné: či proseminár na tabuli používal pravú stranu 1 namiesto 4, či ide o chybu výkladu alebo zdrojového zadania). Bez obrazu tabule nemožno rozhodnúť; varianty sa nesmú zlúčiť do jednej vzorovej úlohy.
- **Šum titulkov:** $3^{33}\bmod11$ je v úvode skomolené, no o 01:09–01:44 sa vyjasní; úsek 02:21–04:41 obsahuje medzery a nesúvisiace frázy. Rekonštruovaný výsledok 5 overujú skriptá aj priamy výpočet, nie doslovný prepis celej medzery.
- **Preklep v dôkaze inverzie:** veta skrípt na riadku 2253 o „být s ní nesoudělný“ nie je správnym zdôvodnením $d\mid1\Leftrightarrow d=1$ pre kladné $d$; každý integer je s 1 nesúdeliteľný. Znenie Věty 6.12 aj prednáškový argument sú jednoznačné. [chap-06-tc.woo:2252–2253; dml-P8-par1-dombek 1:21:53–1:22:12]
- **Anki a skriptá:** výraz $a^{\varphi(m)-1}$ chápať ako reprezentanta modulo $m$, pri výsledku v $\mathbb Z_m$ treba redukovať. Samotná veľká celočíselná mocnina nemusí ležať v $\mathbb Z_m$. [Tvrzení 6.11, chap-06-tc.woo:2834; anki-dml.txt:164]
- Discord je neúplný výskumný záznam; presné otázky študentov k tejto časti ani detailné požiadavky ústnych skúšajúcich nedodáva.
- Doklad o celoštátnej či celokurzovej frekvencii úloh zdroj nemá; uvedené počty sú iba inventár lokálnej vzorky.

### 11. Surové zdroje

Cesty nižšie sú vstupy na čítanie; odkazy `súbor:riadok` vyššie sa vzťahujú na tieto konkrétne lokálne kópie.

- `kontext/BRIEF.md`: pravidlá, citácie a osnova.
- `kontext/INDEX.md`: inventár a stavy nahrávok, najmä P7/P8 DML a prosemináre 06/07.
- `kontext/cisla/dml-kap6.tsv`: označenia od Definice 6.7 po Větu 6.20.
- `zdroje/src/bi-dml-master/src/textbook/chap-06-tc.woo`: 1677–koniec kapitoly; pomocný REA od 771, najmä 1703–2264, 2420–2860, 2933–3387 a ďalej príklad ZČVOZ.
- `zdroje/src/bi-dml-master/src/textbook/cv-06-tc.woo`: 1067–1317 (redukcia, inverzie), 1612–1984 (mocniny), 2037–2148 (obmedzenia a metódy), 2416–2911 (sústavy), 2989–3009 (proseminárové zadania).
- `kontext/skripta-dml/bi-dml-master/src/slides/DML_P05_tc.tex`: obal :10; `P05_tc.tex`: 1225–1626 modulárna aritmetika, 1627–2021 krátenie a mocniny, 2022–2432 lineárne kongruencie a sústavy.
- `kontext/anki-dml.txt`: 146–169, znenia definícií a tvrdení druhej časti.
- `kontext/txt/dml-P7-par1-dombek.txt`: 07:24–08:51 a 1:17:08, kontrola záveru do 1:41:26.
- `kontext/txt/dml-P8-par1-dombek.txt`: 44:19–1:35:55, najmä 46:08–55:11, 59:19–1:15:33, 1:19:26–1:26:51, 1:29:20–1:35:49.
- `kontext/txt/dml-P7-par2-spevak.txt`: 1:09:00–1:33:02.
- `kontext/txt/dml-P8-par3-legersky.txt`: kontrola tém a záver 1:38:10–1:38:40; dozvuk do 1:44:52.
- `kontext/txt/dml-prosem2526-06.txt`: kontrola tém, záver LDR 39:58–43:15.
- `kontext/txt/dml-prosem2526-07.txt`: celé 00:00–41:38, presné tematické úseky v oddiele 1.
- `zdroje/testy-dml-2.md`: 9, 14, 17, 25, 27, 36, 39.
- `zdroje/testy-rozstrel.md`: otázky TČ3/TČ4, najmä 17 a 31; číselné nejasnosti rešpektované.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`: DML TČ3/TČ4, ID citované v oddieloch 3 a 7; zvlášť poznámka pri `dml-lecture-dombek-p9-5511` o odloženom dôkaze.
- `zdroje/discord-dml.md`: stav výskumu a neprejdené dotazy 18–27, 85, 93; žiadne doložené špecifické otázky o kongruenciách.

Kontrola pred odovzdaním: náhodná vzorka najmenej 10 časových a riadkových citácií znovu porovnaná s lokálnymi zdrojmi; numerické príklady overené výpočtom. Žiadne zdrojové súbory sa nemenili.

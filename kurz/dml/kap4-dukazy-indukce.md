# BI-DML kap. 4: Matematický důkaz, matematická indukce

Balík kapitoly: skriptá `chap-04-dukazy-indukce.woo` + `cv-04`, slajdy `DML_P03`, kódy rozvrhu MI1 až MI3; v appke téma `dml-mi` (`data/dml/Induction*.kt`).

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav lokálnych podkladov overený 17. 11. 2026. Výťah pokrýva celú kapitolu, vrátane štrukturálnej indukcie.
Čísla sú výhradne z `cisla/dml-kap4.tsv`, potvrdené zneniami Anki; nezamieňať s číslami kapitoly 6 v appke spomenutými v komentári TSV.

- Značenie a zjednodušenia: Definície 4.1 až 4.3, prirodzené čísla, sumy, produkty, obmedzené kvantifikátory; potom dohoda o šípkach mimo čistej logiky. [chap-04-dukazy-indukce.woo:14–294]
- Typy dôkazov: Definície 4.4 až 4.7, príklady priameho/nepriameho dôkazu, sporu, rozboru prípadov a ekvivalencie. [chap-04-dukazy-indukce.woo:307–527]
- Indukcia a rekurzia: Definície 4.8 a 4.9; slabý princíp Věta 4.1; silný princíp Věta 4.2; induktívne množiny Definícia 4.10 a štrukturálny princíp Věta 4.3. [chap-04-dukazy-indukce.woo:537–1585]
- Cvičenia: `cv-04-indukce.woo`, vrátane výslovného varovania pred indukciou naruby a úloh na chybné dôkazy. [cv-04-indukce.woo:845–873; cv-04-indukce.woo:1765–1819]

Časové rozsahy sú hranice tematických úsekov, nie tvrdenie, že každá sekunda obsahuje súvislý výklad:

| Paralelka | Nahrávka a úsek | Obsah |
|---|---|---|
| 1, Dombek | dml-P3-par1-dombek 59:11–1:38:10 | Prechod z logiky, značenie, priamy/nepriamy dôkaz, spor. |
| 1, Dombek | dml-P4-par1-dombek 06:32–1:35:26 | Dokončenie typov dôkazov, rekurzia, slabá, silná aj štrukturálna indukcia. |
| 2, Spěvák | dml-P3-par2-spevak 1:16:50–1:35:20 | Značenie a dôležité zjednodušenia. |
| 2, Spěvák | dml-P4-par2-spevak 05:29–1:30:25 | Typy dôkazov a všetky tri princípy indukcie. |
| 3, Legerský | dml-P4-par3-legersky 1:10:05–1:36:22 | Značenie a prechod z čistej logiky; INDEX uvádza partial. |
| 3, Legerský | dml-P5-par3-legersky 06:10–1:39:37 | Typy dôkazov, rekurencia, slabá a silná indukcia; výklad prerušuje evakuácia. |
| 3, záskok | dml-P6-par3-legersky 07:26–16:30 | Induktívne množiny, štrukturálna indukcia a vlastnosť zápisu predikátových formúl. |

P6 tretej paralelky neprednáša Legerský: P5 oznamuje záskok kolegu Spěváka; P6 predstavuje záskok za Legerského, ale meno automatický prepis skomolí na „Jan Skrivák“.
Pripísanie Spěvákovi má oporu v ohlásení P5; samotný názov súboru by bol zavádzajúci. [dml-P5-par3-legersky 06:37; dml-P6-par3-legersky 06:13]

Proseminár `dml-prosem2526-04`, 00:00–46:27: titulky sú prítomné, ale viaceré pasáže sú poškodené a obsahujú vložené znelky; prednášajúci sa v úvode nepredstavuje menom. [dml-prosem2526-04 00:00; dml-prosem2526-04 46:27]
Žiadna hlavná sekcia kapitoly nezostáva neodprednášaná vo všetkých paralelkách: štrukturálny princíp výslovne zaznie v P4 prvej aj druhej a P6 tretej. [dml-P4-par1-dombek 1:31:13–1:34:56; dml-P4-par2-spevak 1:22:01–1:29:55; dml-P6-par3-legersky 11:27–16:30]
Istota: vysoká pri formálnych zneniach skrípt/Anki a explicitných pokynoch; obmedzená pri tom, čo bolo iba nakreslené na tabuli a titulky to nepopisujú.

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. Doteraz sme skúmali syntax logiky, teraz ju použijeme v matematike. Najprv treba zjednotiť význam $\mathbb N$, súčtov a skrátených kvantifikátorov, aby dôkazy boli čitateľné. [dml-P3-par1-dombek 59:11–1:07:49; dml-P3-par2-spevak 1:16:50–1:24:46]
2. Dôkaz implikácie je sled korektných dôsledkov, nie manipulácia smerujúca od želaného výsledku k niečomu pravdivému. Priamy dôkaz, Definícia 4.4, ukazuje súčin dvoch nepárnych čísel. [dml-P4-par2-spevak 07:52–10:08]
3. Niekedy sa oplatí zmeniť východisko: Definícia 4.5 používa kontrapozíciu, Definícia 4.6 spor. Na opačnej implikácii o nepárnosti súčinu a iracionalite odmocniny vidno rozdiel týchto postupov. [dml-P4-par2-spevak 10:43–19:18]
4. Rozbor prípadov musí vyčerpať možnosti; ekvivalencia potrebuje oba smery alebo reťazec ekvivalentných úprav. Dombek pridáva úsporný uzavretý cyklus implikácií medzi viacerými tvrdeniami. [dml-P4-par2-spevak 19:54–25:56; dml-P4-par1-dombek 15:29–16:44]
5. Rekurencia vytvára ďalšie členy zo známych, indukcia dokazuje, že sa pri vytváraní zachováva vlastnosť. Definície 4.8 a 4.9 konkretizujú faktoriál a Fibonacciho postupnosť. [dml-P4-par1-dombek 17:02–31:45; dml-P4-par2-spevak 30:35–33:21]
6. Nekonečne veľa dosadení nahradíme dvoma konečnými dôkazmi: prvá hodnota a všeobecná implikácia k nasledovníkovi. Súčet $1+\cdots+n$ motivuje Větu 4.1; Spěvák kreslí schody, proseminár domino. [dml-P4-par1-dombek 31:51–39:39; dml-P4-par2-spevak 34:46–41:14; dml-prosem2526-04 00:17–02:43]
7. Slabá indukcia nepatrí iba k algebraickým vzorcom. Dláždenie šachovnice používa rozdelenie na štyri menšie úlohy; Dombek navyše rozoberá dosiahnuteľnosť polí koňom pomocou dvoch indukcií. [dml-P4-par2-spevak 46:42–49:49; dml-P4-par1-dombek 56:26–1:00:56]
8. Rozklad čísla na prvočísla zlyhá pri pokuse použiť IP iba pre bezprostredne predchádzajúce číslo. Věta 4.2 povoľuje celý konečný úsek predchádzajúcich hodnôt; mince navyše ukážu nutnosť viacerých počiatočných hodnôt. [dml-P4-par2-spevak 50:29–1:02:35; dml-P4-par1-dombek 1:19:24–1:22:54]
9. Věta 4.3 prenesie rovnakú myšlienku na prvky vytvárané pravidlami, napríklad formule alebo vektory. Záskok v tretej paralelke volí vlastnosť rozostupov atomických formúl; proseminár množinu generovanú číslami 6 a 8. [dml-P4-par2-spevak 1:22:01–1:28:24; dml-P6-par3-legersky 13:01–16:30; dml-prosem2526-04 40:35–45:57]

## 3. Pojmy a tvrdenia

Číslovanie sa ráta osobitne podľa druhu: Definícia 4.1 a Věta 4.1 sú rôzne objekty. Dôležitosť nižšie opisuje doložený výskyt, nie predpoveď budúceho testu.

| Číslo | Druh | Názov | Jadro jednou vetou | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 4.1 | Definícia | Přirozená a celá čísla | $0\notin\mathbb N$, ale $0\in\mathbb N_0$. | Definícia, nedokazuje sa; Spěvák vysvetľuje dôsledok pre odpovede [dml-P3-par2-spevak 1:17:24–1:18:40]. | Domény a hranice každej indukcie; upozornenie na MARAST v tom istom úseku. |
| 4.2 | Definícia | Suma a produkt | Sčítavame/násobíme celé indexy medzi medzami; prázdne hodnoty sú 0 a 1. | Definícia; Dombek vysvetľuje neutrálny prvok [dml-P3-par1-dombek 1:04:03–1:05:04]. | Súčet vo Fibonacciho úlohe [testy-dml-2.md:61]. |
| 4.3 | Definícia | Omezené kvantifikátory a zkrácené značení | Univerzálne obmedzenie skracuje implikáciu, existenčné konjunkciu. | Definícia; Legerský rozpisuje oba smery prekladu [dml-P4-par3-legersky 1:21:39–1:23:41]. | Potrebné pre presné IK; v čistej predikátovej logike zakázané [dml-P3-par2-spevak 1:29:16–1:29:46]. |
| 4.4 | Definícia | Přímý důkaz | Z predpokladu vedie konečný reťazec korektných úsudkov do záveru. | Definícia bez dôkazu; príklad áno [chap-04-dukazy-indukce.woo:340]; Spěvák [dml-P4-par2-spevak 08:59–10:08]. | Základ dokazovania IK; cvičný inventár [sources.json: dml-skripta-mi-330]. |
| 4.5 | Definícia | Nepřímý důkaz | Dokážeme $\neg B\Rightarrow\neg A$ namiesto $A\Rightarrow B$. | Definícia; príklad áno [chap-04-dukazy-indukce.woo:370]; Spěvák [dml-P4-par2-spevak 11:58–13:21]. | Rozlíšiť metódy [sources.json: dml-skripta-mi-360]; samostatný testový výskyt zdroj nemá. |
| 4.6 | Definícia | Důkaz sporem | Z $A\wedge\neg B$ odvodíme kontradikciu. | Definícia; príklad áno [chap-04-dukazy-indukce.woo:422]; Spěvák [dml-P4-par2-spevak 16:16–18:46]. | Cvičný dôkaz iracionality [sources.json: dml-skripta-mi-415]; samostatný testový výskyt zdroj nemá. |
| 4.7 | Definícia | Důkaz rozborem případů | Každá z vyčerpávajúcich alternatív musí viesť k záveru. | Definícia; ilustračný rozbor áno [chap-04-dukazy-indukce.woo:450]; Spěvák [dml-P4-par2-spevak 19:54–22:28]. | Vstupuje do prvočíselného rozkladu [sources.json: dml-skripta-mi-1155]. |
| 4.8 | Definícia | Rekurentně (rekurzivně) definovaná posloupnost | Počiatočné členy a predpis z predchádzajúcich určujú ďalšie členy. | Definícia, nedokazuje sa; Spěvák [dml-P4-par2-spevak 30:35–31:42]. | Rekurencie v štyroch inventárnych testových úlohách MI2 [sources.json: dml-t2-2025-k-1; dml-t2-2023-a-1; dml-t2-2022-v1-4; dml-t2-2022-v2-4]. |
| 4.9 | Definícia | Faktoriál a Fibonacciho posloupnost | $0!=1$, $n!=n(n-1)!$; $f_0=0$, $f_1=1$, $f_n=f_{n-1}+f_{n-2}$ pre $n\geq2$. | Definícia, nedokazuje sa; Spěvák [dml-P4-par2-spevak 29:15–32:51]. | Fibonacci v teste [testy-dml-2.md:61], faktoriály v cvičeniach [cv-04-indukce.woo:442–446]. |
| 4.1 | Věta | Slabý princip matematické indukce | ZK a všeobecný prechod $V(n)\Rightarrow V(n+1)$ dávajú všetky $n\geq n_0$. | Samostatný dôkaz vety skriptá nemajú; Dombek hovorí, že princíp nedokazuje [dml-P4-par1-dombek 37:28–38:39]. | Hlavný rámec matíc a súčtov [testy-dml-2.md:7; testy-dml-2.md:61]. |
| 4.2 | Věta | Silný princip matematické indukce | IP zahŕňa všetky hodnoty od $n_0$ po $n$, ZK môže obsahovať $m+1$ hodnôt. | Samostatný dôkaz vety skriptá nemajú; výklad a príklady Spěvák [dml-P4-par2-spevak 55:36–1:02:35]. | Rekurencie dvoch/troch predchádzajúcich členov [testy-dml-2.md:34; cv-04-indukce.woo:1047–1064]. |
| 4.10 | Definícia | Induktivně definovaná množina | Obsahuje práve prvky získané konečnou aplikáciou základných a induktívnych pravidiel. | Definícia, nedokazuje sa; Spěvák [dml-P4-par2-spevak 1:15:29–1:16:31]. | Predpoklad štrukturálnej indukcie [sources.json: dml-cv-04-12; dml-cv-04-16]. |
| 4.3 | Věta | Princip strukturální indukce | Overíme základy a zachovanie vlastnosti každým generujúcim pravidlom. | Samostatný dôkaz vety skriptá nemajú; aplikácia na vektory áno [chap-04-dukazy-indukce.woo:1529]; Spěvák [dml-P4-par2-spevak 1:25:24–1:28:24]. | Cvičenia a výslovný dôraz na skúšku [dml-P6-par3-legersky 16:30]; priamu testovú úlohu MI3 inventár nemá. |

Presné základné odkazy: [Definice 4.1, chap-04-dukazy-indukce.woo:28], [Definice 4.2, chap-04-dukazy-indukce.woo:56], [Definice 4.3, chap-04-dukazy-indukce.woo:111].
Ďalej [Definice 4.4, chap-04-dukazy-indukce.woo:316], [Definice 4.5, chap-04-dukazy-indukce.woo:351], [Definice 4.6, chap-04-dukazy-indukce.woo:399], [Definice 4.7, chap-04-dukazy-indukce.woo:439].
Rekurencie a princípy: [Definice 4.8, chap-04-dukazy-indukce.woo:594], [Definice 4.9, chap-04-dukazy-indukce.woo:617], [Věta 4.1, chap-04-dukazy-indukce.woo:833], [Věta 4.2, chap-04-dukazy-indukce.woo:1187], [Definice 4.10, chap-04-dukazy-indukce.woo:1355], [Věta 4.3, chap-04-dukazy-indukce.woo:1498].
Anki má rovnaké znenia a čísla; hlavné tri princípy sú na riadkoch 84, 85 a 88. [anki-dml.txt:84–88]

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

### Úplný zápis slabej indukcie

1. Definuj konkrétny výrok $V(n)$, doménu $n\in\mathbb Z$ a dolnú hranicu $n_0$; cieľom je $(\forall n\in\mathbb Z,\ n\geq n_0)V(n)$.
2. **ZK:** dosaď $n=n_0$ do celého tvrdenia a over ho, pri rovnosti vypočítaj obe strany.
3. **IK:** zvoľ ľubovoľné, ale počas dôkazu pevné $n\geq n_0$. Napíš, že za predpokladu $V(n)$ dokazuješ $V(n+1)$.
4. **IP:** rozpíš obsah $V(n)$, nie iba názov. V cieli dosaď $n+1$ do všetkých výskytov $n$.
5. Od známeho výrazu/platného tvrdenia odvoď cieľ; miesto použitia IP vyznač nad príslušným vzťahom alebo výslovne slovami.
6. **Záver:** zo ZK a IK podľa slabého princípu MI platí konkrétne tvrdenie pre všetky celé $n\geq n_0$.

Zdôvodnenie: Věta 4.1 a jej formula. [chap-04-dukazy-indukce.woo:833–873]
Pevné ľubovoľné $n$ zdôrazňuje Dombek; rozpis konkrétnej implikácie a konkrétneho záveru Legerský. [dml-P4-par1-dombek 41:13–41:42; dml-P5-par3-legersky 1:12:08–1:12:47; dml-P5-par3-legersky 1:16:22–1:17:24]
V písomke sú IK a použitie IP výslovne požadované; názvy ZK/IK odporúča Spěvák kvôli čitateľnosti. [testy-dml-2.md:7; testy-dml-2.md:12; dml-P4-par2-spevak 42:14]

Formálne, pri univerze $\mathbb Z$:

$\big(V(n_0)\wedge(\forall n\geq n_0)(V(n)\Rightarrow V(n+1))\big)\Rightarrow(\forall n\geq n_0)V(n)$.

Toto je celý princíp, nie iba ľavá strana. IP je iba $V(n)$ vnútri dokazovanej implikácie, nie $(\forall n\geq n_0)V(n)$.
Prvý kvantifikátor v IK musí mať v dosahu celú implikáciu; nemožno predpokladať platnosť pre všetky $n$ a následne ju „dokázať“. [dml-P4-par1-dombek 39:57–41:42; dml-P4-par1-dombek 1:11:33–1:12:03]

### Vzor: súčet a miesto použitia IP

Dokazujeme $V(n):\ \sum_{k=1}^{n}k=\frac{n(n+1)}2$ pre každé $n\in\mathbb N$.
ZK: pri $n=1$ je súčet $1$ a pravá strana $\frac{1\cdot2}{2}=1$.
IK: nech $n\geq1$ a predpokladajme $\sum_{k=1}^{n}k=\frac{n(n+1)}2$; chceme $\sum_{k=1}^{n+1}k=\frac{(n+1)(n+2)}2$.

$\sum_{k=1}^{n+1}k=\sum_{k=1}^{n}k+(n+1)\stackrel{IP}{=}\frac{n(n+1)}2+(n+1)=\frac{(n+1)(n+2)}2$.

Záver: podľa slabého princípu MI rovnosť platí pre všetky $n\in\mathbb N$.
Skriptá aj prednášky oddeľujú posledný sčítanec; Legerský dôrazne radí najprv rozpísať dokazovanú implikáciu. [chap-04-dukazy-indukce.woo:787–830; dml-P5-par3-legersky 1:12:08–1:16:09]
Proseminár analogicky rieši $\sum_{i=1}^{n}(2i-1)=n^2$, s posledným členom $2n+1$. [dml-prosem2526-04 23:26–25:43]

### Dôkaz deliteľnosti

Výrok $6\mid(7^n-1)$ možno v IP rozpísať $(\exists k\in\mathbb Z)(7^n-1=6k)$.
Cieľ má vlastného svedka $(\exists\ell\in\mathbb Z)(7^{n+1}-1=6\ell)$; nejde o požiadavku rovnakého $k$.
Po ZK $7^1-1=6$ rozložíme $7^{n+1}-1=(7^n-1)+6\cdot7^n$ a použijeme IP na prvý člen; výsledok je násobok 6.
Takto je postup presne rozpísaný aj s kvantifikátormi v cvičeniach. [cv-04-indukce.woo:56–82]
Proseminár ukazuje aj prevod na násobok; jeho obmedzenie svedka na prirodzené čísla nepreberať ako všeobecnú definíciu deliteľnosti, skriptá tu používajú $\mathbb Z$. [dml-prosem2526-04 03:19–03:39; cv-04-indukce.woo:66]

### Nerovnosť: správny smer a správny vzťah

Pre $S_n=\sum_{i=1}^{n}\frac1{i^2}$ a IP $S_n\leq2-\frac1n$ postupujeme:

$S_{n+1}=S_n+\frac1{(n+1)^2}\stackrel{IP}{\leq}2-\frac1n+\frac1{(n+1)^2}\leq2-\frac1{n+1}$.

IP tu dáva $\leq$, nesmie sa zmeniť na $=$. Posledná pomocná nerovnosť sa v proseminári overuje vratnými úpravami cez násobenie kladným $n(n+1)^2$ až na $-1\leq0$.
Dôkaz funguje vďaka možnosti ísť od známeho $-1\leq0$ späť k cieľu; samotné odvodzovanie pravdy z cieľa by nestačilo. [dml-prosem2526-04 28:56–29:43; dml-prosem2526-04 31:04–36:10]
Skriptá odporúčajú upravovať jednu stranu na druhú a výslovne varujú pred mechanickým prepisovaním celej neoverenej rovnice na $0=0$. [cv-04-indukce.woo:845–873]

### Silná indukcia a počet základných hodnôt

Věta 4.2: zvoľ $n_0\in\mathbb Z$, $m\in\mathbb N_0$; $V$ musí mať zmysel pre všetky $n\geq n_0$.
ZK overí $V(n_0),\ldots,V(n_0+m)$, teda **$m+1$ hodnôt**.
IK pre ľubovoľné pevné $n\geq n_0+m$ predpokladá $(\forall k\in\{n_0,\ldots,n\})V(k)$ a odvodzuje $V(n+1)$.
Záver sa vracia k všetkým $n\geq n_0$, nie iba k tým nad posledným základným prípadom. [chap-04-dukazy-indukce.woo:1187–1279; anki-dml.txt:85]

- Prvočíselný rozklad: ZK je iba $n=2$, teda $m=0$. Ak je $n+1$ prvočíslo, hotovo; inak $n+1=ab$, $2\leq a,b\leq n$, takže IP možno použiť na oba činitele. [chap-04-dukazy-indukce.woo:1219–1258; dml-P4-par2-spevak 1:00:26–1:02:05]
- Mince 3 a 5: $n_0=8$, $m=2$. Overíme $8=3+5$, $9=3+3+3$, $10=5+5$; pre $n\geq10$ máme $n-2\geq8$, z IP vyplatíme $n-2$ a pridáme 3, čím dostaneme $n+1$. [chap-04-dukazy-indukce.woo:1283–1315; dml-P4-par1-dombek 1:21:00–1:22:54]
- Rekurencia troch členov: pri $A_{n+3}=A_{n+2}+5A_{n+1}+3A_n$ overiť $n=0,1,2$ a až potom dosadzovať vzorce z IP. [cv-04-indukce.woo:1047–1069]

Počet základných prípadov teda odvodíme z najmenšieho indexu, na ktorý sa v IK odvolávame; viac počiatočných hodnôt samo osebe nevysvetľuje obsah IP. [dml-P5-par3-legersky 1:34:17–1:37:47]
Pri alternatívnom zápise cieľa $V(n)$ namiesto $V(n+1)$ musí byť horný index IP $n-1$ a IK začína pri $n\geq n_0+m+1$; nesmie vzniknúť medzera. [chap-04-dukazy-indukce.woo:1261–1279; dml-P4-par1-dombek 1:12:59–1:13:41]

### Iné kroky a slabá verzia mincí

Proseminár pre $8\mid(3^n+7^n-2)$ overuje $V(1),V(2)$ a dokazuje $V(n)\Rightarrow V(n+2)$.
Prvý základ pokryje nepárne indexy, druhý párne; bez druhého základu by neboli pokryté všetky prirodzené čísla. [dml-prosem2526-04 16:22–21:43]
Spěvák analogicky vysvetľuje mince ako tri rady hodnôt s krokom 3 a základmi 8, 9, 10; netreba používať všetky časti silného IP. [dml-P4-par2-spevak 1:12:25–1:13:25]
Legerský ukazuje aj priamy prechod o jednu korunu: ak je vo vyplatení pätikoruna, nahradíme ju dvoma trojkorunami; ak sú iba trojkoruny, pri sume aspoň 8 sú aspoň tri, tie nahradíme dvoma pätikorunami. [dml-P5-par3-legersky 1:38:21–1:39:20]
Skriptá ponúkajú slabý dôkaz cez zosilnený výrok o dostupných minciach. Silná teda neznamená, že výsledok je principiálne nedokázateľný slabou indukciou pri vhodnej úprave tvrdenia. [chap-04-dukazy-indukce.woo:1318; dml-P5-par3-legersky 1:05:05–1:05:24]

### Mocnina matice v teste

Najprv podľa zadania spočítaj prvé mocniny a vyslov odhad $A^n=B(n)$. Potom over základ a pre všeobecné $n$ použi $A^{n+1}=A^nA\stackrel{IP}{=}B(n)A$, vynásob a porovnaj s $B(n+1)$.
Toto je syntéza aplikácie Věty 4.1 na testový typ, nie tvrdenie o tabuľovom postupe konkrétneho prednášajúceho. [testy-dml-2.md:7; testy-dml-2.md:23; cv-04-indukce.woo:1431–1450]
V testoch ide o dve činnosti, odhad aj dôkaz; niekoľko úspešných mocnín nie je náhradou všeobecného IK. [sources.json: dml-t2-2025-e-1/a,b]

### Štrukturálna indukcia

Věta 4.3: jasne pomenuj vlastnosť $V(x)$ prvku z induktívne zadanej množiny.
ZK overí všetky prvky vložené základnými pravidlami; IK overí každé generujúce pravidlo za predpokladu vlastnosti všetkých jeho vstupov.
Záver: každý prvok množiny má danú vlastnosť, pretože vzniká konečným použitím pravidiel. [chap-04-dukazy-indukce.woo:1355–1375; chap-04-dukazy-indukce.woo:1498–1525]

- Vektory: pri základoch $(1,1),(1,-1)$ over $\lvert x_2\rvert\leq x_1$; pre súčet použi $\lvert a_2+b_2\rvert\leq\lvert a_2\rvert+\lvert b_2\rvert\stackrel{IP}{\leq}a_1+b_1$ a pre $\alpha>0$ vzťah $\lvert\alpha a_2\rvert=\alpha\lvert a_2\rvert\stackrel{IP}{\leq}\alpha a_1$. [chap-04-dukazy-indukce.woo:1529–1568]
- Formule: základy nemajú závorky, negácia ich počet nemení, každá binárna spojka pridá jednu ľavú a jednu pravú; treba pokryť všetky konštruktory. [cv-04-indukce.woo:1884–1938; dml-P4-par2-spevak 1:28:24–1:29:25]
- Čísla generované zo 6 a 8 súčtom a rozdielom: ZK overí párnosť oboch základov; pri IK súčet aj rozdiel párnych ostávajú párne. To dokazuje iba $S\subseteq2\mathbb Z$. [cv-04-indukce.woo:1822–1879; dml-prosem2526-04 41:18–42:53]
- Na $S=2\mathbb Z$ treba druhú inklúziu: vytvor $2=8-6$, $0=6-6$, indukciou všetky kladné párne a pravidlom rozdielu aj $0-2n$. Proseminár túto druhú časť dopĺňa, skriptá ju nechávajú čitateľovi. [dml-prosem2526-04 42:57–45:57; cv-04-indukce.woo:1877–1881]

### Ostatné dôkazové metódy

Priamy dôkaz: z $m=2k+1$, $n=2\ell+1$ odvoď $mn=2(2k\ell+k+\ell)+1$; uveď, že $k,\ell\in\mathbb Z$.
Nepriamy: pre cieľ „nepárny súčin implikuje oba činitele nepárne“ predpokladaj aspoň jeden činiteľ párny; BÚNO $m=2k$, teda $mn=2kn$ je párny.
Spor: pri iracionalite $\sqrt3$ predpokladaj $\sqrt3=m/n$ s nesúdeliteľnými $m,n\in\mathbb N$; odvoď $3\mid m$ aj $3\mid n$, teda spor s nesúdeliteľnosťou.
Rozbor: najprv zdôvodni, prečo prípady pokrývajú všetky možnosti, a potom dokáž záver v každom; pri $n^2-n$ stačia párne a nepárne $n$.
Ekvivalencia: oba smery dokáž osobitne, prípadne používaj iba skutočne vratné ekvivalencie; pri viacerých tvrdeniach možno použiť uzavretý cyklus implikácií.
[chap-04-dukazy-indukce.woo:316–527; dml-P4-par2-spevak 08:59–25:56; dml-P4-par1-dombek 16:14]

## 5. Značenie a terminológia

- České názvy: „základní krok“ ZK, „indukční krok“ IK, „indukční předpoklad“ IP, „slabý/silný princip matematické indukce“, „princip strukturální indukce“. [chap-04-dukazy-indukce.woo:833–859; chap-04-dukazy-indukce.woo:1187–1215; chap-04-dukazy-indukce.woo:1498–1525]
- MI je dovolená skratka matematickej indukcie; slajdy ju výslovne uvádzajú. [P03_dukaz_indukce.tex:530]
- $V(n)$ je vlastnosť/predikát s argumentom $n$; pri pevnom $n$ príslušný výrok. Premenná sa môže volať $n,k,i,j$, ale musí byť jasné, čo je pevné a čo viazané. [chap-04-dukazy-indukce.woo:837–873; dml-P4-par1-dombek 1:12:38–1:12:55]
- Použitie IP sa značí $\stackrel{IP}{=}$, pri odhade $\stackrel{IP}{\leq}$; nejde o nový druh rovnosti. [chap-04-dukazy-indukce.woo:811–830; chap-04-dukazy-indukce.woo:1560–1568; dml-prosem2526-04 12:32; dml-prosem2526-04 28:56–29:10]
- $\mathbb N=\{1,2,\ldots\}$, $\mathbb N_0=\{0,1,2,\ldots\}$, $\mathbb Z$ celé čísla; konvenciu $\mathbb N$ s nulou a $\mathbb N^+$ pre kladné kurz nepoužíva. [Definice 4.1, chap-04-dukazy-indukce.woo:28; dml-P3-par2-spevak 1:18:40]
- $n_0$ nemusí byť 0 ani 1, môže byť záporné celé číslo. $m$ v silnom princípe môže byť 0; počet základných hodnôt je $m+1$. [chap-04-dukazy-indukce.woo:868; chap-04-dukazy-indukce.woo:1191–1202]
- $\sum_{k=d}^{h}a(k)$ a $\prod_{k=d}^{h}a(k)$ postupujú po celých indexoch; pre $d>h$ dávajú 0 a 1. [Definice 4.2, chap-04-dukazy-indukce.woo:56]
- $(\forall x\in\mathbb Z)F$ skracuje $(\forall x)(x\in\mathbb Z\Rightarrow F)$; $(\exists x>0)F$ skracuje $(\exists x)(x>0\wedge F)$. Negácia mení kvantifikátor, zachová obmedzenie a neguje až $F$. [chap-04-dukazy-indukce.woo:111–156]
- Mimo čistej logiky sa pre platné dôsledky/ekvivalencie používajú $\Rightarrow,\Leftrightarrow$; v čistej logike sa spojka a vzťah medzi formulami naďalej musia rozlišovať. [chap-04-dukazy-indukce.woo:249–294; dml-P3-par2-spevak 1:33:24–1:35:20]
- BÚNO znamená „bez újmy na obecnosti“ a vyžaduje symetriu/oprávnené premenovanie, nie ľubovoľné vypustenie prípadu. [chap-04-dukazy-indukce.woo:379; dml-P4-par2-spevak 12:36–12:47]
- Štrukturálna indukcia pracuje s prvkami $m\in M$, nie nevyhnutne s číselným $n$. Induktívna definícia povoľuje len konečný počet aplikácií pravidiel. [Definice 4.10, chap-04-dukazy-indukce.woo:1355; Věta 4.3, chap-04-dukazy-indukce.woo:1498]
- Zásadný rozdiel skratky „SI“ medzi slabou a silnou pomenúva aj Legerský; pri závere je bezpečnejšie vypísať princíp celým názvom, ako to sám následne robí. [dml-P5-par3-legersky 1:16:29–1:16:45]

## 6. Čo kto zdôrazňuje, chce a zakazuje

Nasledujú krátke doslovné úryvky automatických titulkov; matematický význam je rozpracovaný vyššie podľa skrípt.

| Kto | Čo povedal | Citácia |
|---|---|---|
| Dombek | „Ani jeden nesmíte vynechat“: týka sa ZK a IK. | [dml-P4-par1-dombek 39:09] |
| Dombek | „Vy máte ukázat, že nějaké tvrzení V platí“: záverom nemá byť vyhlásenie, že platí princíp indukcie. | [dml-P4-par1-dombek 39:39] |
| Dombek | „tak co řešíš, to jsme měli dokázat přece“: odmieta predpokladať už od začiatku všetky $V(n)$. | [dml-P4-par1-dombek 40:54] |
| Dombek | „libovolné Nko aspoň něco, zafixujete ho“: správny začiatok IK. | [dml-P4-par1-dombek 41:34] |
| Dombek | „Tohle jsou úkoly, to je checklist.“ Samotný výpočet ZK/IK bez všeobecného záveru nie je celé znenie princípu. | [dml-P4-par1-dombek 1:11:33] |
| Dombek | „Důkaz kruhem je FOPA, které je zakázáno“: nezamieňať s korektným cyklom dokazovaných implikácií. | [dml-P4-par1-dombek 16:14] |
| Dombek | „v první zápočtové písemce tohle pro vás neexistuje“: obmedzené kvantifikátory v čistej predikátovej logike. | [dml-P3-par1-dombek 1:07:24] |
| Dombek | „ale pak ty bodíky nebudou“: následok porušenia predchádzajúceho obmedzenia. | [dml-P3-par1-dombek 1:14:19] |
| Spěvák | „do písemek určitě pište, aby se v tom ten opravující orientoval“: označenie ZK. | [dml-P4-par2-spevak 42:14] |
| Spěvák | „celou tuto implikaci“: IK nie je dôkaz izolovaného výroku. | [dml-P4-par2-spevak 43:25] |
| Spěvák | „Vy jste z něj akorát odvodili něco pravdivýho.“ Odvodenie $0=0$ z cieľa bez cesty späť nestačí. | [dml-P4-par2-spevak 23:55] |
| Legerský | „nepište tam VN, potom VN plus jedna, napište si tam přímo to, co chcete ukázat“: praktická rada rozpisovať obsah. | [dml-P5-par3-legersky 1:12:08] |
| Legerský | „Nechceme slyšet, že to je domino.“ Pri otázke na princíp treba matematické znenie. | [dml-P5-par3-legersky 1:09:09] |
| Legerský | „tak ověřuju tři základní kroky“: pri $m=2$ sú to hodnoty 8, 9, 10. | [dml-P5-par3-legersky 1:36:51] |
| Spěvák, záskok v par. 3 | „ten princip je dobrý znát hlavně ke zkoušce“: štrukturálna indukcia. | [dml-P6-par3-legersky 16:30] |
| Proseminár | „Napíšu na to rovno zase IP“: označuje miesto použitia predpokladu. | [dml-prosem2526-04 12:32] |
| Proseminár | „tak vám to uzná“: v konkrétnom príklade stačí slovne zdôvodniť párnosť $7^n+1$, netreba osobitne rozpísať triviálnu pomocnú indukciu. | [dml-prosem2526-04 13:47–13:57] |
| Proseminár | „Je potřeba jít obráceně“: dôkaz musí ísť od známej pravdy k cieľu. | [dml-prosem2526-04 36:05] |
| Skriptá | „Nedělejme indukci naruby!“: explicitné varovanie vrátane zámeny nerovnosti za rovnosť. | [cv-04-indukce.woo:845–873] |

Presnú sadzbu strhnutých bodov za vynechané ZK/IK/IP alebo záver zdroj nemá. Historické zadania však IK a miesto IP priamo vyžadujú. [testy-dml-2.md:7; testy-dml-2.md:12; testy-dml-2.md:23]
Pokyn Legerského k rozpisu $V(n)$ je výslovne odôvodnená rada, nie doložený zákaz každého korektného dôkazu používajúceho definovanú skratku $V$. [dml-P5-par3-legersky 1:12:17]

## 7. Typické úlohy

Frekvencie sú počty v lokálnom inventári a prepisoch, nie štatistika všetkých testov. `sources.json` má 13 testových záznamov MI: 9 MI1, 4 MI2, žiadny MI3; vzor a neisté/studentské prepisy sú medzi nimi.

| Typ | Príklad zadania po česky | Odkaz | Ako často v podkladoch |
|---|---|---|---|
| Odhad mocniny matice a dôkaz | „Odhadněte $A^n$ a odhad dokažte matematickou indukcí.“ | [testy-dml-2.md:7; testy-dml-2.md:23; testy-dml-2.md:27; testy-dml-2.md:38] | 4 inventárne záznamy vrátane vzoru; R/N 2024 je tá istá matica, v inventári zlúčená. |
| Rekurencia dvoch členov, explicitný vzorec | „Dokažte, že $A_n=3^{n-1}+(-2)^n$.“ | [testy-dml-2.md:34; testy-dml-2.md:48; testy-dml-2.md:55] | 3 testové záznamy, roky 2022 a 2023. |
| Nelineárna postupnosť a hranica nerovnosti | „Zjistěte, pro která $n$ platí $x_n<4$; dokažte indukcí.“ | [testy-dml-2.md:12; sources.json: dml-t2-2025-k-1] | 1 test 2025, 10 bodov. |
| Fibonacciho súčet | „Dokažte $f_1+f_3+\cdots+f_{2n-1}=f_{2n}$.“ | [testy-dml-2.md:61] | 1 doložený test 2021; ďalšie 3 vlastnosti v [sources.json: dml-cv-04-5]. |
| Súčet a nerovnosť | „Dokažte $\sum_{i=1}^{n}(2i-1)=n^2$“; „$\sum_{i=1}^{n}1/i^2\leq2-1/n$“. | [cv-04-indukce.woo:567; cv-04-indukce.woo:587; dml-prosem2526-04 23:26–36:35] | 7 častí súčtového cvičenia; 3 harmonické vlastnosti [sources.json: dml-cv-04-4; dml-cv-04-10]. |
| Deliteľnosť mocnín/polynómov | „Dokažte, že $7^n-1$ je dělitelné 6.“ | [cv-04-indukce.woo:37; sources.json: dml-cv-04-1; sources.json: dml-cv-04-2] | 4 exponenciálne a 6 ďalších častí; testová zmienka 2021 je študentská spomienka [testy-dml-2.md:68]. |
| Faktoriálové a exponenciálne odhady | „Dokažte $n^2\leq2^n$ pro $n\geq4$.“ | [cv-04-indukce.woo:428–450] | 4 základné nerovnosti, ďalšie v súčtovom cvičení. |
| Rekurencia troch členov | „Dokažte $A_n=(-1)^n(n+1)$.“ | [cv-04-indukce.woo:1047–1064] | 1 cvičenie, explicitná potreba 3 základných hodnôt. |
| Rozklad, mince, kombinatorická konštrukcia | „Každé $n\geq2$ je součinem prvočísel“; „Vyplaťte každou částku alespoň 8.“ | [sources.json: dml-skripta-mi-1155; dml-skripta-mi-1283; dml-cv-04-7] | Opakované v paralelkách; skriptá aj lámanie čokolády. |
| Obrázková indukcia | „Vydlážděte šachovnici bez jednoho pole“; „Doskáčete koněm na libovolné pole?“ | [sources.json: dml-skripta-mi-896; dml-skripta-mi-1004; dml-cv-04-8] | 2 hlavné príklady; cvičenie hyperkocky má 3 časti. |
| Odhalenie chyby dôkazu | „Jsou následující úvahy správné?“ | [cv-04-indukce.woo:1765–1819; sources.json: dml-cv-04-11] | 3 cvičné chybné argumenty; 1 rozobratý aj v proseminári. |
| Štrukturálna indukcia na číslach/formulách/slovách | „Každá formule má stejně otevíracích a zavíracích závorek.“ | [cv-04-indukce.woo:1906; sources.json: dml-cv-04-12; dml-cv-04-13; dml-cv-04-16] | 3 cvičné bloky po 2 častiach; nulový priamy testový záznam MI3. |

Nerovnosť H 2024 má výslovne neistú pravú stranu; nepoužiť ju ako overený vzor. [testy-dml-2.md:29]
Úloha 29. 11. 2021 na súčet prevrátených odmocnín v prepise explicitne nepredpisuje indukciu; inventár ju dopĺňa ako spôsob dôkazu. [testy-dml-2.md:66; sources.json: dml-t2-2021-1129-2]
V `testy-rozstrel.md` sa priamy typ na indukčný dôkaz nenašiel; v inventári nie je MI test s týmto zdrojovým súborom. Nevyvodzovať z toho zákaz indukcie v rozstrele.
Otvorené kvízy MARAST: prednášky dokladajú ich existenciu a citlivosť na konvenciu prirodzených čísel; znenia ani parafrázy otvorených otázok tu nie sú. [dml-P3-par2-spevak 1:17:24–1:18:08]

## 8. Časté chyby a otázky (Discord, prednášky)

- **Predpoklad všetkého namiesto IP:** „predpokladajme, že tvrdenie platí pre všetky $n$“ predpokladá cieľ; správne fixovať ľubovoľné $n$ a predpokladať iba hodnoty dovolené zvoleným princípom. [dml-P4-par1-dombek 40:28–41:42]
- **Chýbajúci ZK:** samotná implikácia môže platiť aj s nepravdivým predpokladom; chybný „dôkaz“, že každé celé číslo je iracionálne, to demonštruje. [P03_dukaz_indukce.tex:676–697; dml-P5-par3-legersky 1:17:58–1:20:19]
- **Chýbajúci záver princípu:** pri skúškovej otázke nestačí opísať iba ZK a IK; treba povedať, čo z nich plynie a pre aké indexy. [dml-P4-par1-dombek 1:11:33–1:12:03]
- **Neoprávnené použitie $V(n-1)$:** chybný argument $a^n=1$ používa ďalšiu predchádzajúcu hodnotu mimo slabého IP; pri prvom prechode potrebuje dokonca index $-1$, ktorý základ nepokrýva. [dml-prosem2526-04 38:58–40:27; cv-04-indukce.woo:1775–1809]
- **Zvýšenie začiatku IK bez doplnenia ZK:** mince potrebujú prechod až od $n\geq10$, teda samostatne 8, 9 a 10; inak by sa odvolávali na nevyplatiteľnú 7. [dml-P4-par2-spevak 1:10:22–1:11:47]
- **Zámena odhadu za rovnosť:** IP v tvare nerovnosti neumožňuje „dosadenie“ so znakom $=$. [cv-04-indukce.woo:873; dml-prosem2526-04 28:56–29:10]
- **Dôkaz odzadu bez vratnosti:** z cieľa odvodíme $0=0$, ale to cieľ nepotvrdzuje. Vratnosť každej úpravy musí byť zrejmá a zdôvodnená. [dml-P4-par2-spevak 23:19–24:12; dml-prosem2526-04 35:12–36:10]
- **Jedna inklúzia vydávaná za rovnosť množín:** zachovanie párnosti dokazuje len, že iné než párne prvky nevzniknú; osobitne treba dokázať, že vzniknú všetky párne. [dml-prosem2526-04 42:53–43:04; cv-04-indukce.woo:1877–1880]
- **Obmedzené kvantifikátory v úlohe čistej logiky:** v indukcii povolené, pri syntaxi predikátových formúl nie. [dml-P4-par3-legersky 1:23:10–1:23:41]

Discord: lokálny výťah nemá konkrétnu overenú študentskú otázku o indukcii ani bodovaciu prax pre ňu; výslovne priznáva neprejdené hľadanie „indukce“. [discord-dml.md:18–26; discord-dml.md:85; discord-dml.md:93]
Má príbuznú odpoveď učiteľa k dôkazu ekvivalencie logických formúl: každý krok má mať oporu vo vete skrípt. Neprezentovať ju ako osobitné bodovacie pravidlo indukcie. [discord-dml.md:30–35]

## 9. Rozdiely medzi paralelkami

- **Dombek:** výrazne komentuje rozsah kvantifikátorov, fixovanie $n$, celý záver princípu a typické písomkové formulácie. Okrem dláždenia rozoberá aj koňa na nekonečnej šachovnici. [dml-P4-par1-dombek 39:57–41:42; dml-P4-par1-dombek 56:26–1:00:56; dml-P4-par1-dombek 1:11:33–1:12:03]
- **Spěvák:** obraz schodov používa naprieč slabou, silnou indukciou a skokmi po viacerých schodoch. Výslovne vysvetľuje, že tri princípy sú kurzové rozdelenie a nie vyčerpávajúci zoznam možností. [dml-P4-par2-spevak 33:21–40:34; dml-P4-par2-spevak 1:02:35–1:03:32]
- **Spěvák:** na vektoroch oddeľuje dokázanie vlastnosti od dokázania rovnosti s celou výsečou; má vlastné tabletové slajdy, ktoré podľa jeho slov nemusia byť rovnaké ako spoločné slajdy. [dml-P4-par2-spevak 1:13:39–1:14:14; dml-P4-par2-spevak 1:28:09–1:28:24]
- **Legerský:** dôrazne odporúča vypísať konkrétny IP aj cieľ; mince prejde najprv cez výber dostatočného ZK a potom aj slabým postupom s výmenou mincí. Dláždenie pre čas po evakuácii výslovne preskakuje. [dml-P5-par3-legersky 1:12:08–1:12:47; dml-P5-par3-legersky 1:17:30; dml-P5-par3-legersky 1:34:17–1:39:20]
- **Tretia paralelka, záskok Spěvák:** namiesto vektorového hlavného príkladu ukazuje štrukturálny dôkaz, že medzi dvoma atomickými formulami musí byť binárna spojka; pri unárnych konštruktoroch nové atomické výskyty nepribudnú. [dml-P6-par3-legersky 14:26–16:30]
- **Proseminár:** viac priestoru venuje dekompozícii algebraických výrazov, označovaniu IP, oprave chybných argumentov a dokončeniu druhej inklúzie pri párnych číslach. [dml-prosem2526-04 10:01–15:29; dml-prosem2526-04 35:12–40:27; dml-prosem2526-04 42:57–45:57]

## 10. Nejasné a chýbajúce

- Presné bodové zrážky za jednotlivé indukčné chyby: zdroj nemá. Doložené sú požiadavky zadania a výslovné upozornenia, nie univerzálny sadzobník.
- Konkrétne Discord otázky študentov k indukcii: zdroj nemá; lokálny zber je neúplný, takže nemožno tvrdiť, že na Discorde neexistujú. [discord-dml.md:18–26]
- P4 tretej paralelky má v INDEX stav partial; úplnosť celej nahrávky nemožno potvrdiť. Relevantný záverečný úsek však titulky obsahujú.
- Titulky proseminára majú výpadky a skomolené vzorce, napríklad „38“ namiesto 48 pri rozklade $49\cdot7^n$; pokračovanie explicitne používa 48. Vzorce preto overovať proti cvičeniam, nie kopírovať fonetický prepis. [dml-prosem2526-04 20:14; dml-prosem2526-04 21:09; cv-04-indukce.woo:49]
- P6 tretej paralelky je v metadátach Legerský, ale ide o ohlásený záskok Spěváka; meno v úvodnom prepise je nejasné. [dml-P5-par3-legersky 06:37; dml-P6-par3-legersky 06:13]
- Spěvák pri ústnom opise vektorov najprv povie „nezáporný“, neskôr opraví na kladný skalár; skriptá majú $\alpha>0$. Pri opisovaní celej množiny tým nemožno bez vysvetlenia pridať nulový vektor. [dml-P4-par2-spevak 1:19:56–1:20:26; dml-P4-par2-spevak 1:26:11–1:26:18; chap-04-dukazy-indukce.woo:1459–1470]
- Pri rozklade na prvočísla Spěvák pre jeden základný prípad ústne spomenie $m=1$; podľa formálnej verzie so ZK od $n_0$ po $n_0+m$ je správne $m=0$, čo neskôr jeho „$m+1$ schodov“ potvrdzuje. Uprednostniť skriptá/Anki. [dml-P4-par2-spevak 56:35; dml-P4-par2-spevak 58:56; anki-dml.txt:85]
- Samostatné dôkazy troch princípov ako viet: skriptá nemajú; obsahujú formulácie, motiváciu a aplikácie. Ich ekvivalenciu Spěvák vyslovuje, úplný formálny dôkaz v zadaných titulkoch nepredvádza. [dml-P4-par2-spevak 33:51; dml-P4-par2-spevak 1:24:02–1:24:20]
- Priamy testový výskyt štrukturálnej indukcie a spoľahlivé údaje o jej frekvencii v rozstrele: zdroj nemá. Dôraz na znalosť princípu ku skúške je doložený. [dml-P6-par3-legersky 16:30]
- `sources.json` nie je autorita pre názov nahrávky ani náročnosť princípu: Dombekove záznamy majú staršie P4/P5 oproti zadaným P3/P4; dláždenie u Spěváka je označené MI2, hoci používa slabý prechod. Citácie prednášok vyššie vychádzajú z názvov a časov lokálnych TXT. [sources.json: dml-lecture-dombek-p5-1911; dml-lecture-spevak-p4-2789; dml-P4-par2-spevak 47:35–49:37]
- Bonusové video o indukcii je odporučené, ale samostatný prepis tohto bonusu nie je medzi zadanými súbormi výťahu; nepreberať z neho domnelé ďalšie pravidlá. [dml-P4-par1-dombek 06:59–07:25; dml-P4-par2-spevak 1:03:32]

Náhodne skontrolovaných 12 časových citácií proti textu zdroja; navyše overené všetky uvedené časové značky a doslovné úryvky v tabuľke pokynov.

## 11. Surové zdroje

Cesty sú lokálne; skrátené citácie vyššie odkazujú na tieto súbory.

- `kontext/BRIEF.md`, osnova, pravidlá a formát citácií.
- `kontext/INDEX.md`, inventár nahrávok a stav titulkov.
- `kontext/cisla/dml-kap4.tsv`, jediný zdroj čísel objektov kapitoly.
- `zdroje/src/bi-dml-master/src/textbook/chap-04-dukazy-indukce.woo`, riadky 14–1585, najmä 316–527, 787–873, 1155–1318 a 1355–1585.
- `zdroje/src/bi-dml-master/src/textbook/cv-04-indukce.woo`, zadania a riešenia; osobitne 56–82, 428–591, 845–873, 1047–1069, 1765–1938 a 2153–2236.
- `kontext/skripta-dml/bi-dml-master/src/slides/DML_P03_dukaz_indukce.tex:10`, obal vkladá `P03_dukaz_indukce.tex`.
- `kontext/skripta-dml/bi-dml-master/src/slides/P03_dukaz_indukce.tex`, samotný obsah; princípy 586–614, 837–867, 1047–1104; úskalia 676–697.
- `kontext/anki-dml.txt:75–90`, znenia kapitoly; princípy 84, 85 a 88.
- `kontext/txt/dml-P3-par1-dombek.txt`, 59:11–1:38:10.
- `kontext/txt/dml-P4-par1-dombek.txt`, 06:32–1:35:26.
- `kontext/txt/dml-P3-par2-spevak.txt`, 1:16:50–1:35:20.
- `kontext/txt/dml-P4-par2-spevak.txt`, 05:29–1:30:25.
- `kontext/txt/dml-P4-par3-legersky.txt`, 1:10:05–1:36:22.
- `kontext/txt/dml-P5-par3-legersky.txt`, 06:10–1:39:37; záskok ohlásený 06:37.
- `kontext/txt/dml-P6-par3-legersky.txt`, záskok 06:13, výklad MI3 07:26–16:30.
- `kontext/txt/dml-prosem2526-04.txt`, 00:00–46:27; dôležité zápisy 04:28–08:06, 23:26–25:43, 28:56–36:10, 38:58–45:57.
- `zdroje/testy-dml-2.md`, najmä 7, 12, 17, 23, 27, 29, 34, 38, 48, 55, 61, 66, 68 a 70; zachovať označenia neistoty a vzorového zadania.
- `zdroje/testy-rozstrel.md`, kontrola výskytu tém; žiadny priamy inventárny MI test z tohto súboru.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`, `course=dml`, `chapter=MI1/MI2/MI3`; citované ID sú dohľadateľné priamo v poli `id`.
- `zdroje/discord-dml.md`, 18–35, 85 a 93: limity zberu a príbuzné pravidlo k dôkazu ekvivalencie.

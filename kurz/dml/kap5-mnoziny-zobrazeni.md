# BI-DML kap. 5: Množiny a zobrazení

Balík kapitoly: skriptá `chap-05-mnoziny-zobrazeni.woo` + `cv-05`, slajdy `DML_P04`, kódy rozvrhu MZ1, MZ2; v appke téma `dml-mz` (`data/dml/Sets*.kt`).

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Výťah vychádza z lokálnych podkladov dostupných 17. 11. 2026. Čísla sú výhradne z `cisla/dml-kap5.tsv`; každý druh má vlastné počítadlo, preto napr. Definice 5.1 a Věta 5.1 označujú iné objekty.
Skratka `chap-05-mnoziny-zobrazeni.woo` nižšie znamená súbor z adresára `2026-11-06-dml-la1/src/bi-dml-master/src/textbook/`; analogicky `cv-05-mnoziny-zobrazeni.woo`.

- Naivní teorie množin: Definice 5.1, od riadka 86; univerzum a Russellov paradox sú motivačné poznámky.
- Množinové vztahy a operace: Definice 5.2 až 5.6, Tvrzení 5.1, Věty 5.1 až 5.2, riadky 288 až 779.
- Ověřování množinových vztahů: metodický výklad od riadka 785, bez novej číslovanej definície alebo vety.
- Zobrazení: Definice 5.7 až 5.13, Pozorování 5.1 až 5.2, Věty 5.3 až 5.5, riadky 901 až 1404.
- Ověřování injektivity a surjektivity: postupy a príklady od riadka 1405 do 1560, bez novej vety.
- Mohutnost množin: Definice 5.14 až 5.15, Věty 5.6 až 5.7, Tvrzení 5.2 až 5.6 a Důsledek 5.1, riadky 1583 až 2316.
- Cvičenia: množiny od riadka 27, zobrazenia od 1006, ďalšie príklady bez riešenia od 1651. [cv-05-mnoziny-zobrazeni.woo:27]

Časové pokrytie podľa skutočne čitateľných titulkov:

- Paralelka 1, Dombek: `dml-P5-par1-dombek` 09:37–1:36:02, množiny až definícia inverzie; `dml-P6-par1-dombek` 07:07–1:09:38, vlastnosti inverzie až diagonála, otázky navyše 1:09:48–1:11:25.
- Paralelka 2, Spěvák: `dml-P5-par2-spevak` 04:59–1:35:36, množiny, zobrazenia aj definícia porovnávania mohutností; `dml-P6-par2-spevak` 05:29–35:12, mohutnosti, hotel a diagonála.
- Paralelka 3: `dml-P6-par3-legersky` 17:00–koniec výkladu približne 1:35, množiny a zobrazenia; vyučuje záskok, nie automaticky osoba v názve súboru. V 06:13 sa predstavuje prepisom „Jan Skrivák“ a hovorí, že zastupuje Legerského; pravdepodobne ide o Jana Spěváka, meno je však skomolené.
- Legerský osobne nadväzuje v `dml-P7-par3-legersky` 10:49–1:09:54: konečné množiny, mohutnosť, hotel a diagonála; 1:10:07 začína teóriu čísel.
- Proseminár `dml-prosem2526-05` 00:00–54:55: rovnosti, inklúzie, protipríklady, kartézske súčiny, definičný obor a obor hodnôt, injektivita a surjektivita. Prednášajúci sa v titulkoch menom nepredstavuje.

Jadro celej kapitoly je v uvedených nahrávkach pokryté; nemožno označiť celú podkapitolu za ešte neodprednášanú. Všetky jednotlivé zákony sa však na prednáškach osobitne nedokazujú a príklady z cvičení tým nie sú automaticky prebrané.
INDEX označuje Dombekovu P6 ako `failed`, ale textový súbor existuje a má súvislý výklad; skorý začiatok P5 zachytáva iba znelku, preto nemožno rekonštruovať pôvodnú úvodnú motiváciu. [INDEX.md:15] [dml-P5-par1-dombek 09:37] [dml-P6-par1-dombek 07:07]

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. Potrebujeme jazyk na presný opis objektov. Univerzum ohraničí prostredie, v ktorom tvoríme množiny, a Russellov paradox vysvetlí opatrnosť pri „množine všetkého“. Spěvák začína touto motiváciou ešte pred Definíciou 5.1. [dml-P5-par2-spevak 05:11–09:52]
2. Množina je určená prvkami, nie poradím alebo počtom ich opakovaní. Výčet, vlastnosť a výraz s podmienkou sú rôzne zápisy toho istého typu objektu; Spěvák to spája s rozdielom množiny a súboru vektorov z LA1. [dml-P5-par2-spevak 13:43–17:30]
3. Inklúzia a rovnosť sa cez Definíciu 5.2 preložia na implikáciu a ekvivalenciu o ľubovoľnom prvku. Potenčná množina mení typ prvkov: jej prvkami už sú množiny. [dml-P5-par1-dombek 28:10–33:24]
4. Operácie z Definícií 5.3 až 5.6 dávajú množinovú podobu logiky. Prienik patrí ku konjunkcii, zjednotenie k disjunkcii a doplnok k negácii; pozor na rozdiel spoločného prieniku a disjunktnosti po dvojiciach. [dml-P5-par1-dombek 38:12–47:10]
5. Rovnosť sa dokazuje prekladom členstva cez definície, použitím logických zákonov a prekladom naspäť. Dombek chce tento postup; Vennove diagramy uznáva ako princíp, ale nechce nimi nahradiť písomné odvodzovanie. [dml-P5-par1-dombek 48:51–58:03]
6. Zobrazenie z Definície 5.7 nie je iba vzorec: je množinou usporiadaných dvojíc. Totalita zabezpečí obraz každého vstupu; Definícia 5.8 potom odlíši obraz a vzor množiny. [dml-P5-par1-dombek 1:01:02–1:12:00]
7. Injektivita zakazuje zlepenie rôznych vstupov, surjektivita vyžaduje pokrytie celej cieľovej množiny. Dôkaz a vyvrátenie sa riadia kvantifikátormi Definície 5.9, nie skúšaním niekoľkých úspešných hodnôt. [dml-P6-par1-dombek 22:45–25:39]
8. Skladanie, identita a inverzia opisujú postupné vykonanie a vrátenie operácie. Věta 5.4 prepája existenciu inverzie s bijektivitou; Spěvák dôsledne rozdelí ekvivalenciu na oba smery. [dml-P5-par2-spevak 1:20:04–1:23:42]
9. Počítanie konečnej množiny je párovanie jej prvkov s číslami. Definícia 5.14 preto prenesie „rovnako veľké“ na bijekciu a „najviac také veľké“ na injekciu, aj keď sa obyčajné počítanie skončiť nedá. [dml-P6-par2-spevak 05:53–06:45]
10. Hilbertov hotel a Věta 5.7 ukazujú, prečo vlastná podmnožina môže mať rovnakú mohutnosť a prečo sú aj $\mathbb Z$, $\mathbb N^2$ a $\mathbb Q$ spočetné. Legerský navyše ukazuje indukciu pri odhade konečného zjednotenia. [dml-P7-par3-legersky 34:17–37:48] [dml-P7-par3-legersky 40:22–57:07]
11. Hustota racionálnych čísel ešte neznamená rovnakú mohutnosť ako pri reálnych. Cantorova diagonála skonštruuje číslo chýbajúce v každom údajne úplnom zozname a dokáže nespočetnosť už $(0,1)$, Tvrzení 5.6. [dml-P7-par3-legersky 1:01:37–1:09:00]

## 3. Pojmy a tvrdenia

„Def.“ v stĺpci dôkazu znamená zavedenie pojmu, nie chýbajúci dôkaz. „Test“ znamená doložený výskyt typu úlohy v lokálnom výbere, nie predpoveď ďalšieho testu.

| Číslo | Druh | Názov | Jadro | Dôkaz v skriptách; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 5.1 | Definice | Množina, prvek množiny | Neusporiadaný súhrn presne určených objektov. | Def.; Spěvák 09:52. [chap-05-mnoziny-zobrazeni.woo:86] [dml-P5-par2-spevak 09:52] | Základ všetkých operácií; [testy-dml-2.md:35]. |
| 5.2 | Definice | Množinová inkluze a rovnost, potenční množina | $A\subseteq B$ je univerzálna implikácia, rovnosť univerzálna ekvivalencia; $\mathcal P(A)$ obsahuje podmnožiny $A$. | Def.; Dombek 28:10–32:13. [chap-05-mnoziny-zobrazeni.woo:288] [dml-P5-par1-dombek 28:10–32:13] | Priamo definovať a použiť; [testy-dml-2.md:40]. |
| 5.1 | Tvrzení | Vlastnosti množinové inkluze | Reflexivita, tranzitivita, dve inklúzie dávajú rovnosť; monotónnosť $\mathcal P$. | Áno; Spěvák vysvetľuje začiatok vlastností 22:34–23:10. [chap-05-mnoziny-zobrazeni.woo:354] [dml-P5-par2-spevak 22:34–23:10] | Dve inklúzie v dôkazoch; [cv-05-mnoziny-zobrazeni.woo:831]. |
| 5.3 | Definice | Množinové operace | Doplnok, prienik, zjednotenie a rozdiel sa definujú členstvom. | Def.; záskok v 3. paralelke 38:25–40:29. [chap-05-mnoziny-zobrazeni.woo:451] [dml-P6-par3-legersky 38:25–40:29] | Priame definície; [testy-dml-2.md:52]. |
| 5.4 | Definice | Kartézský součin | $A\times B=\{(a,b)\mid a\in A\wedge b\in B\}$. | Def.; záskok 40:29–41:29. [chap-05-mnoziny-zobrazeni.woo:497] [dml-P6-par3-legersky 40:29–41:29] | Definícia a identity; [testy-dml-2.md:35]. |
| 5.1–5.2 | Věta | Vlastnosti množinových operací | Zákony doplnku, identity, anihilácie, idempotencie, komutativity, asociativity, distribúcie, absorpcie a De Morganove zákony. | Dôkazy častí v cvičeniach, ostatné na doplnenie; Dombek ukazuje De Morgana 49:07–51:08. [chap-05-mnoziny-zobrazeni.woo:568] [chap-05-mnoziny-zobrazeni.woo:621] [dml-P5-par1-dombek 49:07–51:08] | Odvodzovanie, nie iba názov zákona; [testy-dml-2.md:8]. |
| 5.5 | Definice | Sjednocení, průnik a součin více argumentů, uspořádané $n$-tice | Zjednotenie používa $\exists k$, prienik $\forall k$, súčin usporiadanú $n$-ticu. | Def.; záskok 41:59–42:59. [chap-05-mnoziny-zobrazeni.woo:718] [dml-P6-par3-legersky 41:59–42:59] | Priama formulová definícia; [testy-dml-2.md:45]. |
| 5.6 | Definice | Disjunktní množiny | Dve množiny majú prázdny prienik; rodina je navzájom disjunktná po dvojiciach. | Def.; Dombek rozlišuje 45:43–47:10. [chap-05-mnoziny-zobrazeni.woo:750] [dml-P5-par1-dombek 45:43–47:10] | Podmienka sčítania mohutností; [dml-skripta-mz-769]. |
| 5.7 | Definice | Zobrazení, definiční obor a obor hodnot | $f\subseteq A\times B$ priradí vstupu najviac jeden výstup; totalita dá práve jeden. | Def.; Dombek 1:01:02–1:01:32. [chap-05-mnoziny-zobrazeni.woo:901] [dml-P5-par1-dombek 1:01:02–1:01:32] | Definícia zobrazenia; [testy-dml-2.md:13]. |
| 5.8 | Definice | Obraz a vzor při zobrazení, zúžení | $f[M]$ zbiera obrazy; $f^{-1}[N]$ všetky vstupy smerujúce do $N$; restrikcia obmedzí vstupy. | Def.; Dombek 1:10:06–1:12:40. [chap-05-mnoziny-zobrazeni.woo:941] [dml-P5-par1-dombek 1:10:06–1:12:40] | Cvičenia s obormi; [dml-cv-05-8]. Priamy test vzoru v dodanom výbere nedoložený. |
| 5.9 | Definice | Zobrazení injektivní, surjektivní a bijektivní | Rovnosť obrazov núti rovnosť vstupov; každý cieľ má vzor; bijekcia spĺňa oboje. | Def.; Dombek metodicky 23:06–25:39. [chap-05-mnoziny-zobrazeni.woo:979] [dml-P6-par1-dombek 23:06–25:39] | Opakovaná explicitná požiadavka; [testy-dml-2.md:24] [testy-dml-2.md:53]. |
| 5.10–5.11 | Definice | Rovnost a složení zobrazení; Identické zobrazení | Zhoda zahŕňa obe množiny aj hodnoty; $(f\circ g)(a)=f(g(a))$; $\mathrm{id}_A(a)=a$. | Def.; Dombek trvá na oboch množinách 1:28:22. [chap-05-mnoziny-zobrazeni.woo:1067] [chap-05-mnoziny-zobrazeni.woo:1087] [dml-P5-par1-dombek 1:28:22] | Priamo skúšané; [testy-dml-2.md:8] [testy-dml-2.md:46]. |
| 5.1 | Pozorování | Vlastnosti složení zobrazení a identity | Skladanie je asociatívne, vhodné identity sú neutrálne. | Bez dôkazu, na čitateľa; Spěvák používa pri jednoznačnosti 1:14:29–1:16:26. [chap-05-mnoziny-zobrazeni.woo:1103] [dml-P5-par2-spevak 1:14:29–1:16:26] | Podklad pre inverziu a skladanie; [testy-dml-2.md:24]. |
| 5.12 | Definice | Mocnina zobrazení | Pre $f:A\to A$ je $f^0=\mathrm{id}_A$, vyššie mocniny opakované skladanie. | Def.; Dombek motivuje iterácie 1:34:08–1:34:11. [chap-05-mnoziny-zobrazeni.woo:1138] [dml-P5-par1-dombek 1:34:08–1:34:11] | Priama definícia; [testy-dml-2.md:13]. |
| 5.13 | Definice | Inverzní zobrazení | Obe zloženia s inverziou musia byť príslušné identity. | Def.; Dombek 1:34:40–1:35:49. [chap-05-mnoziny-zobrazeni.woo:1158] [dml-P5-par1-dombek 1:34:40–1:35:49] | Jednostranná identita nestačí; [testy-dml-2.md:24]. |
| 5.3 | Věta | Vlastnosti invertibilních zobrazení | Inverzia je jediná, obojstranná a $(f\circ g)^{-1}=g^{-1}\circ f^{-1}$. | Áno; Spěvák alternatívne jednoznačnosť asociativitou 1:14:29–1:16:26. [chap-05-mnoziny-zobrazeni.woo:1176] [dml-P5-par2-spevak 1:14:29–1:16:26] | Použitie v existencii inverzie; priamy samostatný testový dôkaz vo výbere nie je. |
| 5.4 | Věta | O existenci inverze zobrazení | Invertibilita je ekvivalentná bijektivite. | Áno; Spěvák oba smery 1:20:04–1:25:56. [chap-05-mnoziny-zobrazeni.woo:1237] [dml-P5-par2-spevak 1:20:04–1:25:56] | Prepája skúšané definície; [testy-dml-2.md:24]. |
| 5.5 | Věta | Skládání injekcí, surjekcí a bijekcí | Každá z troch vlastností sa zachová skladaním dvoch zobrazení s touto vlastnosťou. | Áno; Dombek vysvetlí spôsob 22:02–22:37. [chap-05-mnoziny-zobrazeni.woo:1300] [dml-P6-par1-dombek 22:02–22:37] | Priamo dôkaz alebo neexistencia; [testy-dml-2.md:8]. |
| 5.2 | Pozorování | Injektivita a surjektivita na konečných množinách | Pri rovnako veľkých konečných množinách injektivita práve vtedy, keď surjektivita. | Náčrt úvahou, nie rozvinutý formálny dôkaz; Legerský 11:31–13:44. [chap-05-mnoziny-zobrazeni.woo:1367] [dml-P7-par3-legersky 11:31–13:44] | Počty injekcií; [testy-rozstrel.md:24] [testy-rozstrel.md:34]. |
| 5.14 | Definice | Mohutnost množin | Rovnosť cez existenciu bijekcie, $\leq$ cez existenciu injekcie. | Def.; Legerský 14:47–18:35. [chap-05-mnoziny-zobrazeni.woo:1583] [dml-P7-par3-legersky 14:47–18:35] | Vysvetlenie vzťahu súčinu a množín; [testy-dml-2.md:17]. |
| 5.15 | Definice | Konečné a nekonečné množiny | Spočetne nekonečná má mohutnosť $\mathbb N$; nespočetná je nekonečná a nie spočetná. | Def.; Dombek upozorní na užší význam 36:00. [chap-05-mnoziny-zobrazeni.woo:1636] [dml-P6-par1-dombek 36:00] | Pre konštrukcie mohutností; [dml-skripta-mz-2086]. |
| 5.6 | Věta | Spočetné nekonečno je nejmenší nekonečno | Každá nekonečná $A$ spĺňa $|\mathbb N|\leq|A|$. | Nedokazuje sa; Legerský 22:53. [chap-05-mnoziny-zobrazeni.woo:1679] [dml-P7-par3-legersky 22:53] | Použitie, nie doložená samostatná testová otázka. |
| 5.7 | Věta | Srovnávání mohutností | Inklúzia dá nerovnosť, vzťahy sú tranzitívne a dve opačné injekcie dajú rovnosť. | Body 1–4 áno, bod 5 bez dôkazu a nevyžaduje sa; Legerský 25:22–31:07. [chap-05-mnoziny-zobrazeni.woo:1716] [dml-P7-par3-legersky 25:22–31:07] | Hotel a spočetnosť; [dml-skripta-mz-2053]. |
| 5.2–5.3 | Tvrzení | Vlastnosti mohutností konečných množin; Mohutnost sjednocení a součinu konečných množin | Súčin dá súčin počtov; zjednotenie najviac ich súčet, pri disjunktnosti rovnosť. | Áno, stručnou úvahou; Legerský navyše indukcia 34:17–37:48. [chap-05-mnoziny-zobrazeni.woo:1779] [chap-05-mnoziny-zobrazeni.woo:1820] [dml-P7-par3-legersky 34:17–37:48] | Číselné podmienky; [testy-dml-2.md:13] [testy-dml-2.md:35]. |
| 5.1 | Důsledek | Co plyne z Hilbertova nekonečného hotelu | $|\mathbb N_0|=|\mathbb Z|=|\mathbb N^2|=|\mathbb N|$. | Konštrukcie pred dôsledkom, pri $\mathbb N^2$ aj Věta 5.7; Legerský 44:41–54:02. [chap-05-mnoziny-zobrazeni.woo:2053] [dml-P7-par3-legersky 44:41–54:02] | Konštrukčné cvičenia; [dml-skripta-mz-1880]. |
| 5.4 | Tvrzení | Další spočetné množiny | $\mathbb Z^2$, $\mathbb Z\times\mathbb N$ a $\mathbb Q$ sú spočetné. | Áno; Legerský špirála a racionálne čísla 55:07–57:07. [chap-05-mnoziny-zobrazeni.woo:2086] [dml-P7-par3-legersky 55:07–57:07] | Dôkazové cvičenia; [dml-skripta-mz-2086]. |
| 5.5 | Tvrzení | Vlastnosti mohutností nekonečných množin | Vlastná podmnožina môže mať rovnakú mohutnosť; väčšia nekonečná mohutnosť určuje uvedené zjednotenie aj neprázdny súčin. | Nedokazuje sa, skriptá odkazujú na axiómu výberu; Spěvák 24:48–26:00. [chap-05-mnoziny-zobrazeni.woo:2188] [dml-P6-par2-spevak 24:48–26:00] | Nekonečný prípad úlohy o súčine; [testy-dml-2.md:17]. |
| 5.6 | Tvrzení | Nespočetné množiny | $(0,1)$ a $\mathbb R$ sú nespočetné, $|(0,1)|=|\mathbb R^+|=|\mathbb R|$. | Áno, diagonála a známe bijekcie; Legerský 1:03:17–1:09:00. [chap-05-mnoziny-zobrazeni.woo:2221] [dml-P7-par3-legersky 1:03:17–1:09:00] | Dôkazové cvičenie; [dml-skripta-mz-2221]. |

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**Množinová inklúzia, prvkovo.**
1. Napíš, z akého univerza berieš ľubovoľné $x$ a čo máš dokázať: $x\in L\Rightarrow x\in P$.
2. Predpokladaj $x\in L$. Operácie rozbaľuj zvonka dovnútra: členstvo v prieniku na konjunkciu, v zjednotení na disjunkciu, v rozdiele na členstvo a nečlenstvo.
3. Logickými dôsledkami odvoď $x\in P$. Ekvivalencie sú dovolené, ale na inklúziu stačí implikácia.
4. Uzavri, že z ľubovoľnosti $x$ plynie $L\subseteq P$, Definícia 5.2.
Vzor z proseminára: $x\in A\setminus B\Leftrightarrow(x\in A\wedge x\notin B)\Rightarrow x\in A$.
[chap-05-mnoziny-zobrazeni.woo:795] [dml-prosem2526-05 01:59–04:42]

**Množinová rovnosť, reťazec ekvivalencií.**
1. Pre ľubovoľné $x\in\mathcal U$ potrebuješ $x\in L\Leftrightarrow x\in P$, nie iba jednu implikáciu.
2. Rozbaľ definície od poslednej vykonanej operácie. Negáciu zloženého výroku najprv ponechaj so zátvorkami.
3. Pomenuj použitý logický zákon; potom zlož členstvo v operáciách na pravej strane.
4. Napíš záver $L=P$. Ak si použil iba jednostranné dôsledky, doplň opačnú inklúziu podľa Tvrdenia 5.1.
Dombekov vzor: $x\in\overline{A\cap B}\Leftrightarrow\neg(x\in A\wedge x\in B)\Leftrightarrow(x\notin A\vee x\notin B)\Leftrightarrow x\in\overline A\cup\overline B$.
[Definice 5.2, chap-05-mnoziny-zobrazeni.woo:288] [dml-P5-par1-dombek 49:07–51:08]

**Ekvivalencia vzťahov medzi množinami.**
Proseminár pri $A\subseteq B\Leftrightarrow A\cap B=A$ dokazuje dva smery, nemusí všetko znovu rozpisovať až na atómy.
Pri $A\subseteq B$ použije $A=A\cap A\subseteq A\cap B\subseteq A$, teda rovnosť z dvoch inklúzií.
Pri $A\cap B=A$ použije $A=A\cap B\subseteq B$.
Toto je doložený alternatívny postup s už známymi vzťahmi; nemení explicitnú požiadavku konkrétneho testu na odvodzovanie z definícií.
[Tvrzení 5.1, chap-05-mnoziny-zobrazeni.woo:354] [cv-05-mnoziny-zobrazeni.woo:863] [dml-prosem2526-05 37:17–41:57]

**Vyvrátenie množinovej rovnosti alebo inklúzie.**
Zvoľ konkrétne univerzum a všetky množiny tak, aby spĺňali zadanie. Vypočítaj obe strany a ukáž rozdielový prvok; pri inklúzii musí byť vľavo a nebyť vpravo.
Proseminár vyvracia $X\setminus(Y\cup Z)=(X\setminus Y)\cup Z$ voľbou $X=Y=\emptyset$, $Z=\{1\}$ v univerze $\{1,2\}$: vľavo $\emptyset$, vpravo $\{1\}$.
Treba ukázať, že zvolená konfigurácia naozaj vyvracia vzťah, nestačí iba zoznam množín. [dml-prosem2526-05 19:20–21:27] [testy-dml-2.md:8]

**Kartézsky súčin a doplnok.**
Použi ľubovoľnú dvojicu $(a,b)\in\mathcal U\times\mathcal U$; $(a,b)\in X\times Y$ rozbaľ ako $a\in X\wedge b\in Y$.
Napr. distribúcia $X\times(Y\cup Z)$ sa prevedie na distribúciu konjunkcie cez disjunkciu.
Doplnok $X\times Y$ ber v $\mathcal U^2$, doplnky $X,Y$ v $\mathcal U$; univerzá nesplynú.
Proseminár pri $\mathcal U=\{1,2,3\}$, $X=\{1\}$, $Y=\{2\}$ porovnáva 8 prvkov doplnku súčinu so 4 prvkami $\overline X\times\overline Y$.
Prepis v 35:21 povie „devět dalších“, ale 36:21 opisuje správne deväť dvojíc mínus jednu; nepreberať chybný počet 9.
[Definice 5.4, chap-05-mnoziny-zobrazeni.woo:497] [dml-prosem2526-05 25:23–28:57] [dml-prosem2526-05 34:56–36:34]

**Dokázanie injektivity.**
1. Uveď definíciu: $(\forall x,y\in A)(f(x)=f(y)\Rightarrow x=y)$.
2. Zober ľubovoľné dva vstupy, predpokladaj rovnosť ich obrazov a dosaď predpis. Pri dvojiciach alebo trojiciach rozlož rovnosť obrazov na rovnosti súradníc.
3. Odvodzuj dôsledky až k rovnosti celých vstupov. Ekvivalentné úpravy sú vítané, ale nie nevyhnutné.
4. Kontroluj oprávnenosť delenia a predpoklady o číslach; uveď záver „je injektívne“.
Vzor skrípt $f(n)=n^3+n$ na $\mathbb Z$: rovnosť obrazov vedie na $(m-n)(m^2+mn+n^2+1)=0$. Druhý faktor nemôže byť nulový, preto $m=n$; skriptá to zdôvodnia záporným diskriminantom.
[Definice 5.9, chap-05-mnoziny-zobrazeni.woo:979] [chap-05-mnoziny-zobrazeni.woo:1433] [dml-P6-par1-dombek 23:06–23:51]

**Vyvrátenie injektivity.**
Negácia je $(\exists x,y\in A)(x\neq y\wedge f(x)=f(y))$. Napíš oba konkrétne vstupy, ich príslušnosť do $A$, ich rôznosť a vypočítaj rovnaké obrazy.
Pri $f(n)=n^2+n$ na $\mathbb Z$ stačí $2\neq-3$ a $f(2)=6=f(-3)$.
Neúspešný pokus o dôkaz môže odhaliť vetvu $m=-n-1$ a pomôcť protipríklad nájsť; samotné „nedokázal som“ nie je vyvrátenie.
Pri $f(m,n)=(m^2,mn)$ proseminár volí $(0,0)$ a $(0,1)$, oba obrazy sú $(0,0)$.
[chap-05-mnoziny-zobrazeni.woo:1458] [dml-prosem2526-05 49:16–50:38]

**Dokázanie surjektivity.**
1. Uveď $(\forall b\in B)(\exists a\in A)(f(a)=b)$ a vyber ľubovoľné cieľové $b$.
2. Rieš $f(a)=b$ pre neznámy vstup $a$; $b$ je parameter. Pri viacerých súradniciach rieš príslušnú sústavu.
3. Napíš explicitnú voľbu $a=a(b)$ a over $a\in A$. Nestačí nájsť reálne riešenie, ak má byť celočíselné alebo kladné.
4. Dosaď a over $f(a(b))=b$ pre každé $b\in B$, potom uzavri surjektivitu.
Skriptá: $f:\mathbb R^2\to\mathbb R^2$, $f(m,n)=(m,m+2n)$; pre $(x,y)$ je vzor $(x,(y-x)/2)\in\mathbb R^2$.
Proseminár: pri $f(m,n)=m+n+13$, $\mathbb Z^2\to\mathbb Z$, pre ľubovoľné $c\in\mathbb Z$ zvolí $(0,c-13)$.
[chap-05-mnoziny-zobrazeni.woo:1502] [dml-P6-par1-dombek 24:40–25:39] [dml-prosem2526-05 54:12–54:48]

**Vyvrátenie surjektivity.**
Negácia je $(\exists b\in B)(\forall a\in A)(f(a)\neq b)$. Zvoľ konkrétny cieľ a dokáž, že nemá žiadny prípustný vzor.
Pri rovnakom predpise $(m,m+2n)$ na $\mathbb Z^2$ musí byť $y-x$ párne; napr. cieľ $(0,1)$ by nútil $n=1/2\notin\mathbb Z$.
Pri $n\mapsto n+1$ na $\mathbb N_0$ proseminár volí cieľ $0$ a ukáže, že jediný možný vzor je $-1$, mimo domény.
Nenájdenie vzoru alebo neprípustnosť jedného z viacerých možných vzorov samo osebe nestačí; musíš vylúčiť všetky.
[chap-05-mnoziny-zobrazeni.woo:1532] [dml-prosem2526-05 46:49–47:44]

**Skladanie a zachovanie vlastností.**
Najprv zakresli alebo vypíš typy $g:A\to B$, $f:B\to C$; potom $(f\circ g):A\to C$. Aplikuje sa najprv $g$.
Injektivita: $f(g(x))=f(g(y))\Rightarrow g(x)=g(y)\Rightarrow x=y$; pri šípkach pomenuj použitú injektivitu $f$, potom $g$.
Surjektivita: pre ľubovoľné $z\in C$ najprv nájdi $y\in B$ pomocou surjektivity $f$, potom $x\in A$ pomocou surjektivity $g$; over $f(g(x))=z$.
Bijektivita vyplynie z oboch častí. Obrátené závery nezovšeobecňuj: zo surjektivity zloženia plynie surjektivita $f$, z injektivity zloženia injektivita $g$, opačné dve tvrdenia všeobecne nie.
[Věta 5.5, chap-05-mnoziny-zobrazeni.woo:1300] [cv-05-mnoziny-zobrazeni.woo:1540] [dml-P6-par1-dombek 22:02–22:37]

**Inverzia a jej existencia.**
Kandidáta $g:B\to A$ over oboma výpočtami $f\circ g=\mathrm{id}_B$ a $g\circ f=\mathrm{id}_A$.
Věta 5.4: po otočení dvojíc musí mať každé $b$ práve jeden obraz. Surjektivita pôvodného $f$ zabezpečí existenciu, injektivita jednoznačnosť.
Spěvák dokazuje smer „invertibilné $\Rightarrow$ bijekcia“ aplikovaním inverzie na rovné obrazy a voľbou $a=f^{-1}(b)$ pre surjektivitu; spätný smer konštruuje z jediného vzoru.
Jednoznačnosť inverzie možno ukázať aj asociativitou: ak $g,h$ sú inverzie, potom $g=\mathrm{id}_A\circ g=(h\circ f)\circ g=h\circ(f\circ g)=h$.
[Věty 5.3–5.4, chap-05-mnoziny-zobrazeni.woo:1176] [chap-05-mnoziny-zobrazeni.woo:1237] [dml-P5-par2-spevak 1:14:29–1:16:26] [dml-P5-par2-spevak 1:20:04–1:25:56]

**Porovnávanie mohutností a spočetnosť.**
Pre $|A|\leq|B|$ skonštruuj injekciu $A\to B$; pre rovnosť bijekciu alebo dve opačné injekcie a odvolanie na bod 5 Věty 5.7, Cantorovu-Schröderovu-Bernsteinovu vetu.
Jedna injekcia, ktorá nie je na, nevylučuje inú bijekciu a nepreukazuje ostrú nerovnosť mohutností.
Hotel: $n\mapsto n+1$ z $\mathbb N_0$ do $\mathbb N$; pre $\mathbb Z\to\mathbb N$ predpis $2n$ pri $n\geq1$ a $-2n+1$ pri $n\leq0$.
Pre $\mathbb N^2\to\mathbb N$ hotel používa $(k,\ell)\mapsto p_k^\ell$, kde $p_k$ je $k$-té prvočíslo. Je to injekcia, nie surjekcia; opačnú injekciu možno vziať $n\mapsto(n,1)$ a použiť Větu 5.7.
Pre $\mathbb Q$ priraď zlomku v základnom tvare jednoznačnú dvojicu $(p,q)\in\mathbb Z\times\mathbb N$; opačný odhad dá $\mathbb N\subseteq\mathbb Q$. Samotný výpis nezredukovaných zlomkov by obsahoval opakovania.
[Definice 5.14, chap-05-mnoziny-zobrazeni.woo:1583] [Důsledek 5.1, chap-05-mnoziny-zobrazeni.woo:2053] [Tvrzení 5.4, chap-05-mnoziny-zobrazeni.woo:2086] [dml-P7-par3-legersky 50:41–57:07]

**Nespočetnosť diagonálou.**
Predpokladaj sporom úplný zoznam $r_1,r_2,\ldots$ všetkých čísel z $(0,1)$. Použi jednoznačné desatinné rozvoje bez nekonečného chvosta deviatok.
Zostroj číslo $s=0,s_1s_2\ldots$, kde každé $s_k$ je 5 alebo 6 a líši sa od $k$-tej cifry $r_k$.
Potom $s\in(0,1)$, ale $s\neq r_k$ pre každé $k$; spor s úplnosťou zoznamu. Nehovor, že stačí pridať číslo na koniec nekonečného zoznamu.
Rovnosť mohutností $\mathbb R$ a $(0,1)$ možno doložiť $x\mapsto\frac1\pi\arctan(x)+\frac12$, medzi $\mathbb R$ a $\mathbb R^+$ exponenciálou.
[Tvrzení 5.6, chap-05-mnoziny-zobrazeni.woo:2221] [chap-05-mnoziny-zobrazeni.woo:2311] [dml-P7-par3-legersky 1:03:17–1:09:00]

## 5. Značenie a terminológia

- Univerzum $\mathcal U$; skriptá majú zdrojové makro `\UU`. $\mathbb N=\{1,2,\ldots\}$, $\mathbb N_0=\{0,1,\ldots\}$; nulu nepridávať do $\mathbb N$ bez upozornenia. [chap-05-mnoziny-zobrazeni.woo:39] [cv-05-mnoziny-zobrazeni.woo:907] [dml-P6-par2-spevak 19:29]
- $x\in A$ znamená „je prvkem“, $A\subseteq B$ „je podmnožinou“. Vlastná podmnožina je $A\subsetneq B$, teda inklúzia a nerovnosť, nie iba $\subseteq$. [Definice 5.2, chap-05-mnoziny-zobrazeni.woo:288]
- Samotné $\subset$ nie je bezpečná náhrada: Dombek upozorňuje na protichodné konvencie a volí jednoznačné symboly. Nevyvodzovať z prepisu jeho gest presné znenie technickej normy. [dml-P5-par1-dombek 29:36–31:20] [P04_mnoz_zobr.tex:218] [P04_mnoz_zobr.tex:231]
- $\mathcal P(A)$, zdrojové `\PP(A)`, je „potenční množina“. $Y\subseteq X\Leftrightarrow Y\in\mathcal P(X)$; $Y\in X$ a $Y\subseteq X$ nie sú zameniteľné. [chap-05-mnoziny-zobrazeni.woo:439]
- $\overline A$, $A\cap B$, $A\cup B$, $A\setminus B$: „doplněk“, „průnik“, „sjednocení“, „rozdíl“. Používajú sa na množiny; $\neg,\wedge,\vee$ na výroky. [Definice 5.3, chap-05-mnoziny-zobrazeni.woo:451] [dml-P5-par1-dombek 51:36–52:36]
- Zápis $\{x\in\mathcal U\mid v(x)\}$ je kurzový základ. Dvojbodka, bodkočiarka alebo čiarky namiesto konjunkcií existujú; Spěvák priznáva, že sám niekedy píše dvojbodku. Nie je doložený všeobecný zákaz týchto variantov. [chap-05-mnoziny-zobrazeni.woo:240] [dml-P5-par2-spevak 18:00–20:30]
- $D_f$ je „definiční obor“, $H_f$ „obor hodnot“, pričom $H_f=f[A]\subseteq B$. Cieľová množina $B$ nemusí byť skutočný obor hodnôt; pri surjekcii sa rovnajú. [Definice 5.7–5.9, chap-05-mnoziny-zobrazeni.woo:901]
- $f(a)$ je obraz prvku; $f[M]$ obraz množiny. Presne $f[\{a\}]=\{f(a)\}$, nie totožnosť prvku a jednoprvkovej množiny. [chap-05-mnoziny-zobrazeni.woo:941] [dml-P5-par1-dombek 1:10:06]
- $f^{-1}[N]$ je vzor množiny, definovaný aj pre nebijektívne $f$; $f^{-1}(b)$ je hodnota inverzného zobrazenia, ktoré existuje až pri bijekcii. Hranaté a okrúhle zátvorky nesú odlišný typ objektu. [chap-05-mnoziny-zobrazeni.woo:941] [chap-05-mnoziny-zobrazeni.woo:1158] [dml-P5-par1-dombek 1:10:44–1:12:00]
- $f|_M$ je „zúžení (restrikce)“: mení vstupnú množinu a zachová predpis. [Definice 5.8, chap-05-mnoziny-zobrazeni.woo:941]
- „Injektivní“ = „prosté“, „surjektivní“ = „na“, „bijektivní“ = „vzájemně jednoznačné“. Injektivita má aj ekvivalentný zápis $x\neq y\Rightarrow f(x)\neq f(y)$, ale skriptá odporúčajú dokazovať z rovnosti obrazov. [chap-05-mnoziny-zobrazeni.woo:979] [chap-05-mnoziny-zobrazeni.woo:1031]
- $f^n$ pri zobrazení znamená iteráciu, nie bodové umocnenie $f(x)^n$; $f^0=\mathrm{id}_A$ potrebuje $f:A\to A$. [Definice 5.12, chap-05-mnoziny-zobrazeni.woo:1138]
- „Spočetná“ v skriptách a slajdoch znamená spočetne nekonečná; Spěvák na P6 používa aj širší význam zahŕňajúci konečné množiny. Do lekcií nepreberať oba významy bez vysvetlenia. [chap-05-mnoziny-zobrazeni.woo:1636] [P04_mnoz_zobr.tex:914] [dml-P6-par2-spevak 11:01–12:01]
- $|A|=|B|$ je tu predovšetkým vzťah definovaný existenciou bijekcie. Kurz nezavádza kardinálne čísla ako samostatné objekty; pri konečných množinách je $|A|$ počet prvkov. [chap-05-mnoziny-zobrazeni.woo:1618] [dml-P7-par3-legersky 18:35]

## 6. Čo kto zdôrazňuje, chce a zakazuje

Citáty sú doslovné krátke úryvky titulkov alebo písomných podkladov; nejasný prepis nie je opravený potichu.

- Dombek, množinové dôkazy: „tohleto po vás budeme chtít dokazovat z definice.“ [dml-P5-par1-dombek 48:51]
- Dombek, diagramy v hodnotenej odpovedi: „nechceme vám dávat body za to, že vybarvujete kolečka.“ [dml-P5-par1-dombek 55:48]
- Dombek, miešanie typov: „Množinová operace aplikovaná na formuly“ a „To nedělejte, takové věci.“ [dml-P5-par1-dombek 52:06]
- Dombek, vzor verzus inverzia: „To, že tam je na mínus prvou, neznamená inverzní zobrazení.“ [dml-P5-par1-dombek 1:10:50]
- Dombek, pozitívny dôkaz: „Ale abyste dokázali takovou věc, nestačí najít nějaký konkrétní případ.“ [dml-P6-par1-dombek 24:26]
- Dombek, surjektivita: „Ať už je Bčko jaké chce, takhle vypadá Ačko.“ Cieľ je ľubovoľný, vstup sa konštruuje podľa neho. [dml-P6-par1-dombek 25:35]
- Spěvák, využitie množinových dôkazov: „I to bývá občas náplní nějakých zkouškových nebo zápočtových písemek.“ [dml-P5-par2-spevak 38:49]
- Proseminár, kontrola domény pri surjektivite: „Ačko upravdu je z našeho definičního oboru, náleží Z.“ Pravopis je podľa titulkov. [dml-prosem2526-05 45:53]
- Proseminár, už známe jednoduché inklúzie: „To tu beru jako, že je jasný.“ Kontext je monotónnosť prieniku, nie povolenie vynechať celé odôvodnenie. [dml-prosem2526-05 40:45]
- Skriptá, nesprávna argumentácia: „$n^3$ je prosté, $n$ je prosté, takže jejich součet je také prostý“ je výslovne uvedená ako nepovolená skratka. [chap-05-mnoziny-zobrazeni.woo:1438]
- Skriptá, Cantorova-Schröderova-Bernsteinova veta: „Důkaz tohoto tvrzení proto vynecháváme a po studentech nevyžadujeme.“ [chap-05-mnoziny-zobrazeni.woo:1776]
- Test, konkrétna povinnosť: „Zkoumanou vlastnost vždy definujte; platnost dokažte z definice, neplatnost protipříkladem.“ [testy-dml-2.md:24]

## 7. Typické úlohy

Frekvencia nižšie je počet rôznych záznamov príkladov v `sources.json`, nie počet termínov, študentov alebo pravdepodobnosť výskytu. Jeden záznam môže mať viac podotázok aj viac zručností; vzorový test a študentské spomienky sú súčasťou inventára.
Filtrovanie DML a kapitol MZ* aj na úrovni podotázok dáva 104 záznamov: 15 testových, 7 rozstrelových, 26 zo skrípt/cvičení a 56 prednáškových.

| Typ | Krátky príklad zadania po česky | Zdroj | Ako často v dodanom inventári |
|---|---|---|---|
| Definície množín | „Definujte inkluzi, rozdíl a potenční množinu.“ | [testy-dml-2.md:40] [dml-t2-2023-d-3] | 5 testových záznamov so zručnosťou `dml.mz-def-sets`. |
| Definície zobrazení | „Definujte složení a identické zobrazení.“ | [testy-dml-2.md:8] [dml-t2-2025-e-2] | 6 testových záznamov s `dml.mz-def-map`. |
| Mocnina | „Definujte $f^n$ pro $n\in\mathbb N_0$.“ | [testy-dml-2.md:13] [dml-t2-2025-k-2] | 1 testový záznam s `dml.mz-def-power`. |
| Rovnosť alebo inklúzia | „Dokažte odvozením, nebo nalezněte protipříklad.“ | [testy-dml-2.md:8] [testy-dml-2.md:40] [testy-dml-2.md:66] | 3 testové záznamy s `dml.mz-identity`; v cvičeniach rozsiahle série. |
| Súčinové identity | „Platí vždy $\overline X\times\overline Y\subseteq\overline{X\times Y}$?“ | [testy-dml-2.md:27] [dml-t2-2024-n-2] | 3 testové záznamy s `dml.mz-cartesian-identity`. |
| Injektivita a surjektivita | „Vyšetřete $f(m,n)=(2m-3n+2,5m-7n+3)$ na $\mathbb Z^2$.“ | [testy-dml-2.md:17] [dml-t2-2025-vzor-2] | 3 testové záznamy s `dml.mz-injsurj`, ďalšie 2 s nelineárnym typom. |
| Existencia pri skladaní | „Mohou být obě zobrazení injektivní a jejich složení ne?“ | [testy-dml-2.md:8] [testy-dml-2.md:24] [testy-dml-2.md:27] | 3 testové záznamy s `dml.mz-compose-exist`. |
| Konečné mohutnosti | „$C\subseteq D$, $|C\times D|=36$: jaké je $|D\setminus C|$?“ | [testy-dml-2.md:35] [dml-t2-2023-a-2] | 4 testové záznamy s `dml.mz-sizes`, vrátane otázky na 7 prvkov potenčnej množiny. |
| Výber pravdivých tvrdení | „Které množinové vztahy platí vždy?“ | [testy-rozstrel.md:10] [dml-rozstrel-2023-01-15-7] | 3 rozstrelové záznamy s `dml.mz-statements-sets`. |
| Zobrazenie na potenčnej množine | „Určete injektivitu a surjektivitu $f(X)=(X\oplus A)\oplus B$.“ | [testy-rozstrel.md:8] [testy-rozstrel.md:21] | 2 rozstrelové záznamy; symetrická diferencia je v zadaní definovaná. |
| Počet injekcií | „Kolik je injektivních zobrazení $X\times X$ do $Y\times Y$?“ | [testy-rozstrel.md:24] [testy-rozstrel.md:34] | 2 rozstrelové záznamy; kombinatorický presah. |
| Indexované operácie | „Najděte sjednocení a průnik $M_i=\{1,\ldots,i\}$, $N_i=\{i,i+1,\ldots\}$.“ | [cv-05-mnoziny-zobrazeni.woo:907] [dml-cv-05-7] | Jedna séria, 4 podotázky s konečnou i nekonečnou verziou. |
| Spočetnosť a nespočetnosť | „Dokažte spočetnost $\mathbb Q$ a nespočetnost $(0,1)$.“ | [dml-skripta-mz-2086] [dml-skripta-mz-2221] | Doložené v skriptách a prednáškach; medzi 15 testovými záznamami nie ako samostatný typ. |

MARAST: dodaný výber neobsahuje samostatný záznam MZ* s druhom MARAST ani overené znenia otvorených kvízov k tejto kapitole; zdroj nemá podklad na ich frekvenciu. Znenia otvorených kvízov sa sem nepreberajú.
Samostatná úplná sada otázok ústnej skúšky k tejto kapitole: zdroj nemá. Rozstrelové záznamy sú doloženou časťou skúškových úloh, nie úplnou osnovou ústnej skúšky.

## 8. Časté chyby a otázky (Discord, prednášky)

- Zamieňanie prvku a podmnožiny, najmä v $\mathcal P(A)$: skriptá to výslovne označujú za častú študentskú chybu. [chap-05-mnoziny-zobrazeni.woo:439]
- Pripisovanie poradia alebo opakovaní množine: množina vektorov nie je súbor vektorov. [dml-P5-par2-spevak 16:24–17:30]
- Miešanie množinových operácií s logickými spojkami; zbytočné skracovanie $x\in A$ na samotné $A$ zatemní typ objektu. [chap-05-mnoziny-zobrazeni.woo:795] [dml-P5-par1-dombek 51:36–52:36]
- Dôkaz „propříkladem“: dva vstupy s rôznymi obrazmi nedokazujú injektivitu a jeden nájdený vzor nedokazuje surjektivitu. [chap-05-mnoziny-zobrazeni.woo:1556] [dml-P6-par1-dombek 24:26–24:33]
- Zabudnutie domény pri riešení rovnice pre vzor: rovnaký predpis môže byť na reálnych číslach na, na celých nie. [chap-05-mnoziny-zobrazeni.woo:1532] [dml-prosem2526-05 45:53–47:44]
- Čítanie $f^{-1}[N]$ ako automatickej existencie inverzie: vzor môže byť prázdny alebo viacprvkový a existuje aj bez inverzného zobrazenia. [dml-P5-par1-dombek 1:10:44–1:12:00]
- Prehodenie poradia skladania alebo identity na nesprávnej množine: Spěvák pri dôkaze inverzie zloženia opravuje index identity až na $C$. [dml-P5-par2-spevak 1:18:56–1:19:34]
- Z prázdneho prieniku všetkých množín neplynie ich disjunktnosť po dvojiciach. [dml-P5-par1-dombek 47:10] [dml-skripta-mz-769]
- Prenos pravidiel konečných množín na nekonečné: vlastná podmnožina nemusí mať menšiu mohutnosť. [dml-P6-par2-spevak 24:57–25:06]
- Otázka, prečo diagonála neukáže nespočetnosť $\mathbb N$: Legerský odlišuje konečný zápis prirodzeného čísla od nekonečných desatinných rozvojov. [dml-P7-par3-legersky 1:09:20–1:09:54]
- Discord k množinám a zobrazeniam konkrétne otázky v dodanom súbore nemá. To je hranica neúplného výskumu, nie dôkaz, že sa na Discorde téma neriešila. [discord-dml.md:17]
- Súvisiaci všeobecný pokyn učiteľa z Discordu požaduje reťazec ekvivalencií, kde má každý krok oporu vo vete; týka sa formúl, nemožno ho vydávať za samostatný nový výrok o hodnotení tejto kapitoly. [discord-dml.md:32]

## 9. Rozdiely medzi paralelkami

- Dombek najvýslovnejšie odmieta odovzdávanie vyfarbených diagramov namiesto odvodzovania a rozoberá hodnotiteľnosť postupu. Zároveň zdôrazňuje vzor verzus inverziu a nejednoznačnosť samotného $\subset$. [dml-P5-par1-dombek 55:48–56:06] [dml-P5-par1-dombek 1:10:44–1:12:00] [dml-P5-par1-dombek 29:36–31:20]
- Spěvák využíva asociativitu a identitu na elegantný dôkaz jednoznačnosti inverzie; Dombek odvodzuje bodový vzťah $f^{-1}(b)=a\Leftrightarrow f(a)=b$. [dml-P5-par2-spevak 1:14:29–1:16:26] [dml-P6-par1-dombek 08:18–09:24]
- Spěvák v druhej paralelke dôjde už na P5 k definícii porovnávania mohutností; Dombek končí P5 definíciou inverzie. [dml-P5-par2-spevak 1:32:38–1:34:41] [dml-P5-par1-dombek 1:35:54–1:35:58]
- Tretia paralelka P6 je záskok. Vennov prístup ukazuje ako rozbor všetkých prípadov súvisiaci s pravdivostnou tabuľkou, s upozornením na nepresný obrázok; nemožno tieto výroky pripísať Legerskému podľa mena súboru. [dml-P6-par3-legersky 06:13] [dml-P6-par3-legersky 52:08–52:57]
- Legerský na P7 rozpisuje indukčný dôkaz odhadu zjednotenia konečne mnohých konečných množín; pri $\mathbb Z^2$ vysvetľuje špirálové číslovanie. [dml-P7-par3-legersky 34:17–37:48] [dml-P7-par3-legersky 55:07]
- Rozpor v slove „spočetná“: Dombek uvádza užšie kurzové chápanie, Spěvák na P6 zahŕňa konečné množiny. Definícia 5.15 a aktuálny text slajdov podporujú užší význam. [dml-P6-par1-dombek 36:00] [dml-P6-par2-spevak 11:01–12:01] [P04_mnoz_zobr.tex:914]

## 10. Nejasné a chýbajúce

- Chýba overenie zvuku a rukou písanej tabule; titulky neidentifikujú spoľahlivo každý symbol. Výťah ich matematický význam kontroluje proti skriptám, nie domýšľaním kresieb.
- Prvý zachytený vecný riadok Dombekovej P5 je až 09:37. Úplný slovný úvod tejto nahrávky zdroj nemá; označenie P6 ako `failed` v INDEX nezodpovedá samotnej existencii použiteľného textu. [INDEX.md:15] [dml-P5-par1-dombek 09:37]
- Záskok v tretej paralelke je explicitný, presný prepis jeho mena je nejasný; nejde o istý podklad na atribúciu „Legerský povedal“. [dml-P6-par3-legersky 06:13]
- Proseminár má titulky, ale zdroj nemá meno prednášajúceho. Má aj chybný počet prvkov v 35:21; matematicky je doplnok jednoprvkovej množiny v deväťprvkovom univerze osemprvkový. [dml-prosem2526-05 35:21–36:34]
- Spěvákova širšia definícia spočetnosti sa líši od skrípt a slajdov; obe konvencie sú matematicky používané, ale v lekcii musí byť zvolená a vysvetlená kurzová. [dml-P6-par2-spevak 11:01–12:01] [chap-05-mnoziny-zobrazeni.woo:1636]
- Riešenie skladania v cvičeniach má problém: uvádza $f:\mathbb Z\to\mathbb N$, $f(n)=|n|$, ale $f(0)=0\notin\mathbb N$. Príklad v tejto podobe nie je správne typované zobrazenie. Matematická oprava, nie citácia zdroja: nahradiť prostrednú množinu $\mathbb Z$ množinou $\mathbb Z\setminus\{0\}$; inklúzia z $\mathbb N$ a absolútna hodnota potom dávajú požadovaný protipríklad. [cv-05-mnoziny-zobrazeni.woo:1603]
- Predpis nelineárneho zobrazenia v teste K 2025 je neistý už v prepise aj inventári; nepreberať ho ako spoľahlivé kanonické zadanie. [testy-dml-2.md:13] [dml-t2-2025-k-2]
- Spomienka na test 2021 uvádza $(n^2,(n+1)^2):\mathbb Z\to\mathbb N^2$, čo pre $n=0,-1$ nezodpovedá kurzovému $\mathbb N$ bez nuly. Neopravovať zdroj potichu. [testy-dml-2.md:70] [dml-t2-2021-1203rano-3]
- Dombekov improvizovaný posun tangensu v titulkoch nie je dostatočne presný; vzorec bijekcie bol prevzatý z konca skrípt a potvrdený Legerského výkladom. [dml-P6-par1-dombek 1:05:35–1:06:08] [chap-05-mnoziny-zobrazeni.woo:2311] [dml-P7-par3-legersky 1:04:18–1:04:47]
- Dôkaz Věty 5.6, bodu 5 Věty 5.7 a všeobecného Tvrdenia 5.5 zdroj zámerne nedodáva. Nejde o dieru, ktorú treba autorovi lekcie zakryť vymysleným kurzovým dôkazom. [chap-05-mnoziny-zobrazeni.woo:1676] [chap-05-mnoziny-zobrazeni.woo:1776] [chap-05-mnoziny-zobrazeni.woo:2183]
- Záznamy `sources.json` s prefixom `dml-lecture-dombek-p6-*` zahŕňajú obsah, ktorý tu zodpovedá súboru P5; na citovanie nahrávok preto používam skutočné názvy a časové pečiatky textov, nie odhad podľa ID inventára.
- Úplné pokrytie MARAST, presné znenia ústnych otázok a konkrétne kapitole venované Discord otázky zdroj nemá. Frekvencie v oddiele 7 platia len pre dodaný inventár.

## 11. Surové zdroje

Spoločný koreň kontextových podkladov: `kontext/`.

- `BRIEF.md`, celý: pravidlá, citácie, osnova.
- `cisla/dml-kap5.tsv`, celý: jediné použité číslovanie definícií, viet a tvrdení.
- `INDEX.md`, najmä riadky 12–20 a 33: metadáta nahrávok a stav titulkov.
- `zdroje/src/bi-dml-master/src/textbook/chap-05-mnoziny-zobrazeni.woo`, kapitola 1–2316; hlavné použité úseky 86–779, 785–877, 901–1560, 1583–1851, 1880–2316.
- `zdroje/src/bi-dml-master/src/textbook/cv-05-mnoziny-zobrazeni.woo`, množinové úlohy 52–985, zobrazenia 1015–1640, inventár neriešených sérií od 1651.
- `skripta-dml/bi-dml-master/src/slides/DML_P04_mnoz_zobr.tex`, riadky 1–11: obal prezentácie; posledný príkaz načítava skutočný obsah.
- `skripta-dml/bi-dml-master/src/slides/P04_mnoz_zobr.tex`, najmä 216–291, 399–430, 452–493, 620–755, 845–927, 1180–1296: značenie, definície, dôkazy, spočetnosť.
- `txt/dml-P5-par1-dombek.txt`, 09:37–1:36:02; najmä 28:10–37:15, 48:51–58:03, 1:10:06–1:12:40, 1:28:22, 1:34:08–1:35:58.
- `txt/dml-P6-par1-dombek.txt`, 07:07–1:11:25; najmä 22:02–25:39, 36:00, 43:49–44:05, 1:03:22–1:09:38.
- `txt/dml-P5-par2-spevak.txt`, 04:59–1:35:36; najmä 13:43–23:10, 37:25–39:42, 1:14:29–1:25:56, 1:32:38–1:35:36.
- `txt/dml-P6-par2-spevak.txt`, 05:29–35:12; najmä 09:17–12:49, 15:49–26:00, 27:00–34:54.
- `txt/dml-P6-par3-legersky.txt`, predstavenie záskoku 06:13, kapitola 17:00 až približne 1:35; najmä 32:09–42:59, 46:29–53:57, 1:24:05–1:31:33.
- `txt/dml-P7-par3-legersky.txt`, 10:49–1:09:54; najmä 11:31–18:35, 25:22–31:07, 34:17–37:48, 40:22–57:07, 1:03:17–1:09:54.
- `txt/dml-prosem2526-05.txt`, 00:00–54:55; najmä 01:59–04:42, 08:28–13:54, 19:20–21:27, 25:23–36:34, 37:17–41:57, 43:30–54:48.
- `zdroje/testy-dml-2.md`, riadky 8, 13, 17, 24, 27, 35, 40, 45–53, 64–70.
- `zdroje/testy-rozstrel.md`, riadky 8–10, 18–24 a 34.
- `zdroje/discord-dml.md`, celý: hranice výskumu, najmä riadky 17–25 a 32–38; kapitole venované konkrétne otázky chýbajú.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`, všetky DML záznamy s kapitolou MZ* vrátane prepisov kapitoly v podotázkach; 104 záznamov, ID uvedené pri konkrétnych úlohách.

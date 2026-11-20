# BI-DML kap. 2: Výroková logika

Balík kapitoly: skriptá `chap-02-vyrokova-logika.woo` + `cv-02`, slajdy `DML_P01`, kódy rozvrhu VL1, VL2; v appke zatiaľ bez lekcií.

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav overenia: 17. 11. 2026. Výťah rozlišuje obsah skrípt od skutočne zachovaného výkladu; neprítomnosť v titulkoch neznamená, že sa téma neučila.
Čísla definícií, viet a ostatných objektov sú výhradne z `cisla/dml-kap2.tsv`; rôzne druhy majú samostatné počítadlá.

Rozsah `chap-02-vyrokova-logika.woo`:

- § 2.1 Úvod k matematické logice: úsudky a motivácia, Příklad 2.1 na riadku 46.
- § 2.2 Od přirozeného jazyka k výrokové logice: Definice 2.1 až 2.7, prvotné formule, ohodnotenie a päť spojok, riadky 159 až 565.
- § 2.3 Formální definice: Definice 2.8 až 2.11, jazyk, syntax, podformule, pravdivostná tabuľka, riadky 566 až 930.
- § 2.4 Tautologie, kontradikce, splnitelná formule: Definice 2.12 až 2.14 a Tvrzení 2.1, riadky 931 až 1212.
- § 2.5 Logická ekvivalence a logický důsledek: Definice 2.15, Tvrzení 2.2 a 2.3, Důsledek 2.1, Věta 2.1, riadky 1213 až 1532.
- § 2.6 Základní zákony výrokové logiky: Věty 2.2 až 2.8, Definice 2.16, riadky 1533 až 2246.
- § 2.7 Universální systém logických spojek: Definice 2.17 a 2.18, Věty 2.9 a 2.10, riadky 2247 až 2473.
- § 2.8 Konjunktivní a disjunktivní normální tvar formulí: Definice 2.19 až 2.22, Věty 2.11 a 2.12, riadky 2474 až 2950.
- § 2.9 Využití DNT a KNT: Pozorování 2.1 a 2.2, Důsledek 2.2, Věty 2.13 a 2.14, riadky 2951 až 3342.
- § 2.10 Zajímavosti: Věty 2.15 a 2.16, minimalizácia, od riadku 3343. [cisla/dml-kap2.tsv; chap-02-vyrokova-logika.woo:13]

Pokrytie nahrávkami:

- **Paralelka 1, Dombek:** `dml-P2-par1-dombek` od 07:04 rovno prechádza k predikátovej logike. Úsek 07:04 až 15:31 pripomína limity VL, nie dokončovanie KNT/DNT. Súvislý výklad kapitoly 2 v tejto P2 zdroj nemá. [dml-P2-par1-dombek 07:04–15:31]
- **Paralelka 2, Spěvák:** `dml-P2-par2-spevak` 03:33 až 1:19:44; rekapitulácia ekvivalencie, USS od 04:06, normálne tvary od 21:03, tabuľková konštrukcia, dôsledok a minimalizácia. Nová téma začína v 1:20:03. [dml-P2-par2-spevak 03:33–04:45; dml-P2-par2-spevak 1:19:14–1:20:03]
- **Paralelka 3, Legerský:** `dml-P2-par3-legersky` 06:01 až 1:36:24; zákony, USS, DNT/KNT, úplné tvary a korespondencia ÚDNT s tabuľkou. Na konci avizuje ÚKNT a dôsledok nabudúce. [dml-P2-par3-legersky 06:01–06:30; dml-P2-par3-legersky 1:36:03–1:36:24]
- **Doplnenie nad explicitne uvedenú P2:** `dml-P3-par3-legersky` má napriek téme „Predikátová logika 1“ v INDEX-e ešte VL. Čitateľný úsek 12:20 až 25:57 obsahuje dôkaz kritéria dôsledku, ekvivalenciu, SAT, klasické úsudky a obvody; od 26:05 nasleduje motivácia PL. Úsek pred 12:20 je prevažne nezrozumiteľný a nemožno z neho rekonštruovať výklad ÚKNT. [INDEX.md:8; dml-P3-par3-legersky 12:20–12:59; dml-P3-par3-legersky 25:48–26:05]
- **Prosemináre:** `dml-prosem2526-01` má tému VL, zákony a úplné systémy spojok; `dml-prosem2526-02` KNT a DNT. Oba majú v INDEX-e 0 cues: **titulky chýbajú**. Označenie „(proseminář)“ v cvičeniach nedokladá konkrétny hovorený postup vo videu. [INDEX.md:29; INDEX.md:30; cv-02-vyrokova-logika.woo:894]

**Úplne chýba nahrávka VL1 vo všetkých troch paralelkách.** Chýba teda súvislé prvé zavedenie prvotných výrokov, piatich spojok a ich tabuliek, syntaxe, podformulí, ohodnotenia, tautológie/splniteľnosti/kontradikcie, nutnej a postačujúcej podmienky a základnej ekvivalencie/dôsledku (§ 2.1 až 2.5).
Niektoré z týchto vecí sa v P2 pripomínajú, nejde preto o úplnú neprítomnosť každého pojmu v celom korpuse. Zákony § 2.6 zachraňuje Legerského P2, hoci Spěvák ich už považuje za minulé učivo. [BRIEF.md, Zdroje; dml-P2-par2-spevak 03:33–04:06; dml-P2-par2-spevak 06:45–06:59; dml-P2-par3-legersky 06:07–08:00]
Z titulkov nemožno tvrdiť, že základ kapitoly „ešte nebol odprednášaný“; prednášajúci výslovne odkazujú na minulú hodinu.

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **Tabuľky fungujú, ale rastú.** Legerský nadväzuje na ekvivalenciu ako zhodu všetkých ohodnotení: pri piatich prvotných formulách je už 32 riadkov. Zákony majú túto prácu nahradiť, nie spochybniť tabuľkovú metódu. [dml-P2-par3-legersky 06:19–07:30]

2. **Vybudovať zásobu dovolených prepisov.** Věty 2.2 až 2.5 dávajú jednoduché dôsledky, prácu s $\top,\bot$, asociativitu, distributivitu, absorpciu a De Morganove zákony. Z asociativity vzniká praktické vynechávanie zátvoriek pri opakovanej rovnakej spojke. [dml-P2-par3-legersky 08:30–16:26]

3. **Implikáciu a ekvivalenciu rozobrať na jednoduchšie spojky.** Věty 2.6 až 2.8 umožnia odstrániť $\Rightarrow,\Leftrightarrow$ a správne negovať; manželia na ostrove ukazujú riešenie úsudku úpravami namiesto tabuľky. Spěvák túto časť už len pripomína. [dml-P2-par3-legersky 20:50–23:37; dml-P2-par3-legersky 26:41–38:58; dml-P2-par2-spevak 06:45–07:25]

4. **Koľko spojok naozaj potrebujeme?** XOR a prepis ekvivalencie motivujú Definici 2.17: USS zachová vyjadriteľnosť všetkých pravdivostných funkcií cez ekvivalentné formule. Ušetrená spojka môže znamenať výrazne dlhší zápis. [dml-P2-par2-spevak 05:25–08:27; dml-P2-par3-legersky 39:20–43:23]

5. **Stačia dve, potom aj jedna.** Věta 2.9 dáva $\{\neg,\vee\}$, $\{\neg,\wedge\}$, $\{\neg,\Rightarrow\}$; Věta 2.10 NAND a NOR. Legerský podrobne vysvetlí štrukturálnu indukciu pre prvú dvojicu, Spěvák ukáže náhrady a potom konštrukciu negácie a konjunkcie NAND-om. [dml-P2-par3-legersky 45:03–57:23; dml-P2-par2-spevak 12:04–18:14]

6. **Namiesto ľubovoľnej syntaxe chceme čitateľný tvar.** Literál, implikant a klausula vedú cez Definice 2.19 až 2.21 k DNT a KNT. Samotný literál či jediný implikant/klausula sú dovolené hraničné prípady. [dml-P2-par2-spevak 21:03–24:21; dml-P2-par2-spevak 31:47–32:43]

7. **Zákony už vedia vyrábať normálne tvary.** Dvojica $A\Rightarrow(B\Rightarrow C)$ a $(A\Rightarrow B)\Rightarrow C$ ukazuje odstránenie implikácií a distributivitu; zátvorky menia výsledok. Věta 2.11 zaručí existenciu oboch tvarov, ale obyčajné tvary nie sú jednoznačné. [dml-P2-par2-spevak 33:41–39:15; dml-P2-par3-legersky 1:13:38–1:21:21]

8. **Doplnením všetkých premenných dostaneme úplné tvary.** Definice 2.22 vynúti každú prvotnú formulu práve raz v každom termíne a zakáže ekvivalentné duplicity. Doplnenie chýbajúceho $B$ cez $B\vee\neg B$ dáva myšlienku Věty 2.12. [dml-P2-par3-legersky 1:22:42–1:28:29; dml-P2-par3-legersky 1:31:45–1:33:36]

9. **Úplný tvar je tabuľka prepísaná do formule.** Minterm zodpovedá jednej jednotke, maxterm jednej nule. Spěvák ukáže oba smery a odvodí počítanie ohodnotení a jednoznačnosť až na poradie, Důsledek 2.2; Legerský v P2 dôjde k mintermom. [dml-P2-par2-spevak 52:32–57:13; dml-P2-par2-spevak 1:04:22–1:11:14; dml-P2-par3-legersky 1:33:50–1:36:11]

10. **Z tvarov čítame ekvivalenciu aj dôsledok.** Věty 2.13 a 2.14 menia porovnávanie tabuliek na porovnanie termínov; obvody ukazujú zmysel kratšej ekvivalentnej formule. Legerský dôsledok dokončuje až v P3. [dml-P2-par2-spevak 1:11:43–1:19:14; dml-P3-par3-legersky 12:20–20:35; dml-P3-par3-legersky 25:02–25:43]

## 3. Pojmy a tvrdenia

V stĺpci test: **T** = písomné prevody [testy-dml-2.md:49; testy-dml-2.md:56; testy-dml-2.md:66], **R1** = výber dôsledkov [testy-rozstrel.md:12], **R2** = maxtermy [testy-rozstrel.md:30]. „Cvičenia“ znamená doklad výcviku, nie doloženú frekvenciu skúšania.
Pri definíciách sa dôkaz nepožaduje. „P1 chýba“ označuje chýbajúci prvý výklad, nie tvrdenie o tom, či sa tam dokazovalo.

| Číslo | Druh | Názov | Jadro | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 2.1, 2.2 | Definice | Prvotní výrok a formule; Pravdivostní ohodnocení | Výroky zastupujú $A,B,\ldots$ a $v$ im priraďuje 0 alebo 1. [chap-02-vyrokova-logika.woo:184] | Definície; P1 chýba. | Základ T, R1, R2. |
| 2.3 až 2.7 | Definice | Negace; Konjunkce; Disjunkce; Implikace; Ekvivalence | Päť operácií má pevné pravdivostné tabuľky. [chap-02-vyrokova-logika.woo:259; chap-02-vyrokova-logika.woo:735] | Definície; P1 chýba. | Základ T a R1. |
| 2.8 až 2.10 | Definice | Jazyk; Formule; Podformule | Formule vznikajú konečne mnohými pravidlami, podformule je sama korektná formule. [chap-02-vyrokova-logika.woo:586] | Definície; pripomenutie syntaxe [dml-P2-par3-legersky 46:00–46:30]. | Základ úprav T, cvičenia [cv-02-vyrokova-logika.woo:295]. |
| 2.11 | Definice | Pravdivostní ohodnocení výrokové formule | $v(F)$ sa počíta od prvotných formulí cez podformule. [chap-02-vyrokova-logika.woo:735] | Definícia; P1 chýba. | T, R1, R2. |
| 2.12 | Definice | Tautologie, splnitelnost a kontradikce | Všetky jednotky; aspoň jedna jednotka; všetky nuly. [chap-02-vyrokova-logika.woo:938] | Definícia; P1 chýba. | [cv-02-vyrokova-logika.woo:298], základ R1. |
| 2.1 | Tvrzení | Základní vztahy mezi tautologií a kontradikcí VL | Negácia prehodí tautológiu a kontradikciu; tautológia je splniteľná. [chap-02-vyrokova-logika.woo:976] | Áno, priamo z definície na 1005; prednáškový dôkaz zdroj nemá. | Cvičenia [cv-02-vyrokova-logika.woo:494]. |
| 2.13, 2.14 | Definice | Nutná/postačující podmínka; Symboly pro kontradikci a tautologii | V pravdivej implikácii je predpoklad postačujúci, záver nutný; $\top,\bot$ sú konštantné formule. [chap-02-vyrokova-logika.woo:1136; chap-02-vyrokova-logika.woo:1183] | Definície; P1 chýba. | [cv-02-vyrokova-logika.woo:225], okrajové prípady T. |
| 2.15 | Definice | Logická ekvivalence a důsledek ve výrokové logice | Ekvivalencia je zhoda hodnôt pre každé $v$; dôsledok zachováva pravdivosť predpokladu. [chap-02-vyrokova-logika.woo:1223] | Definícia; rekapitulácia [dml-P2-par3-legersky 06:19–06:30]. | R1, všetky ekvivalentné úpravy T. |
| 2.2 | Tvrzení | Základní vztahy mezi logickým důsledkem a ekvivalencí ve VL | Dôsledok je reflexívny a tranzitívny, ekvivalencia je obojstranný dôsledok. [chap-02-vyrokova-logika.woo:1327] | Áno, z definície na 1360; použitie [dml-P3-par3-legersky 18:25–18:41]. | R1; cvičenia [cv-02-vyrokova-logika.woo:1303]. |
| 2.3 | Tvrzení | Náhrada podformule logicky ekvivalentní formulí | Ekvivalentná náhrada podformule zachová ekvivalenciu celku. [chap-02-vyrokova-logika.woo:1368] | Áno, slovný argument; pripomenutie dôkazu stromom [dml-P2-par3-legersky 38:15–38:22]. | Opora každého kroku T. |
| 2.1 | Věta | Logická ekvivalence a důsledek ve VL pomocí tautologie a kontradikce | $E\models F$ práve vtedy, keď $E\Rightarrow F$ je tautológia alebo $E\wedge\neg F$ kontradikcia. [chap-02-vyrokova-logika.woo:1421] | Áno na 1443; P1 chýba, pripomenutie ekvivalenčnej časti [dml-P2-par3-legersky 10:38–10:52]. | R1; [cv-02-vyrokova-logika.woo:1340]. |
| 2.2 | Věta | Logické zákony, základní logické důsledky | Napr. $A\wedge B\models A$ a $A\models A\vee B$. [chap-02-vyrokova-logika.woo:1593] | Skriptá bez dôkazov podľa 1573; Legerský slovne [dml-P2-par3-legersky 08:30–09:51]. | R1, dôsledky úplných tvarov. |
| 2.3, 2.4 | Věta | Základní principy; Vlastnosti $\top$ a $\bot$ | Spor, vylúčené tretie, dvojitá negácia, identita a anihilácia. [chap-02-vyrokova-logika.woo:1646; chap-02-vyrokova-logika.woo:1703] | Skriptá bez dôkazov; Legerský vysvetľuje [dml-P2-par3-legersky 10:00–12:02]. | Úpravy a zúplňovanie T. |
| 2.5 | Věta | Algebraické vlastnosti spojek | Idempotencia, komutativita, asociativita, obe distributivity, absorpcia, De Morgan. [chap-02-vyrokova-logika.woo:1740] | Skriptá bez dôkazov; slovné argumenty a tabuľka ako overenie [dml-P2-par3-legersky 12:10–16:26]. | Priamo T. |
| 2.6 | Věta | Významy implikace | Obmena, $A\Rightarrow B$ ako $\neg A\vee B$, negácia implikácie ako $A\wedge\neg B$. [chap-02-vyrokova-logika.woo:1922] | Skriptá bez dôkazov; Legerský tabuľkou/slovne a odvodením [dml-P2-par3-legersky 20:50–23:37]. | T a R1. |
| 2.16 | Definice | Obměněná implikace, obměna | K $E\Rightarrow F$ patrí $\neg F\Rightarrow\neg E$. [chap-02-vyrokova-logika.woo:2011] | Definícia [dml-P2-par3-legersky 21:38–22:08]. | R1; neskoršie dôkazy. |
| 2.7, 2.8 | Věta | Významy ekvivalence; Algebraické vlastnosti ekvivalence | Ekvivalencia má DNT aj KNT prepis; jej negácia zneguje jednu stranu. [chap-02-vyrokova-logika.woo:2041; chap-02-vyrokova-logika.woo:2114] | Skriptá bez dôkazov; časť odvodená zákonmi [dml-P2-par3-legersky 26:57–31:25], negácia [dml-P2-par3-legersky 34:19–34:34]. | Úpravy T; cvičenia [cv-02-vyrokova-logika.woo:717]. |
| 2.17 | Definice | Universální systém logických spojek | Každá formule má ekvivalent používajúci iba spojky systému. [chap-02-vyrokova-logika.woo:2274] | Definícia [dml-P2-par3-legersky 42:39–43:11]. | Výcvik [cv-02-vyrokova-logika.woo:894], priamy test v dodanom výbere zdroj nemá. |
| 2.9 | Věta | Dvouprvkové universální systémy | Univerzálne sú $\{\neg,\vee\}$, $\{\neg,\wedge\}$, $\{\neg,\Rightarrow\}$. [chap-02-vyrokova-logika.woo:2309] | Skriptá áno pre prvú dvojicu, zvyšok cvičenie; Legerský štrukturálnou indukciou [dml-P2-par3-legersky 45:53–57:23]; Spěvák náčrt náhrad [dml-P2-par2-spevak 12:04–14:32]. | [cv-02-vyrokova-logika.woo:894; cv-02-vyrokova-logika.woo:1025]. |
| 2.18 | Definice | Spojky NAND a NOR | $A\uparrow B$ je $\neg(A\wedge B)$, $A\downarrow B$ je $\neg(A\vee B)$. [chap-02-vyrokova-logika.woo:2370] | Definícia [dml-P2-par2-spevak 14:38–15:23]. | [cv-02-vyrokova-logika.woo:917]. |
| 2.10 | Věta | Jednoprvkové universální systémy | Každá z $\{\uparrow\}$ a $\{\downarrow\}$ samostatne tvorí USS. [chap-02-vyrokova-logika.woo:2398] | Skriptá áno pre NAND, „pro zájemce“; Spěvák konštrukcia [dml-P2-par2-spevak 15:46–18:14]; Legerský iba princíp a negácia [dml-P2-par3-legersky 1:00:00–1:00:54]. | Cvičenia, nie doložený výskyt v teste. |
| 2.19 až 2.21 | Definice | Literál; Disjunktivní normální tvar; Konjunktivní normální tvar | DNT je disjunkcia implikantov, KNT konjunkcia klausúl, s dovoleným jediným členom. [chap-02-vyrokova-logika.woo:2485; chap-02-vyrokova-logika.woo:2494; chap-02-vyrokova-logika.woo:2545] | Definície [dml-P2-par2-spevak 22:28–24:21; dml-P2-par2-spevak 31:47–32:43]. | Priamo T a R2. |
| 2.11 | Věta | Každou formuli lze převést do DNT i KNT | Oba ekvivalentné tvary existujú. [chap-02-vyrokova-logika.woo:2776] | Skriptá iba odkaz na štrukturálnu indukciu/cvičenie 4. kapitoly; obaja v P2 vynechajú [dml-P2-par2-spevak 38:45–39:15; dml-P2-par3-legersky 1:20:02–1:20:33]. | Záruka riešiteľnosti T. |
| 2.22 | Definice | Úplný DNT a KNT | V každom minterme/maxterme sú všetky pôvodné premenné práve raz; členy sú navzájom neekvivalentné. [chap-02-vyrokova-logika.woo:2806] | Definícia [dml-P2-par3-legersky 1:23:19–1:28:29]. | Priamo T a R2. |
| 2.12 | Věta | Každou formuli lze převést do ÚDNT i ÚKNT | Po doplnení konvencií pre $\bot,\top$ existujú aj oba úplné tvary. [chap-02-vyrokova-logika.woo:2885] | Skriptá myšlienka dôkazu na 2893; Legerský dopĺňaním [dml-P2-par3-legersky 1:31:45–1:33:36]; Spěvák aj tabuľkou [dml-P2-par2-spevak 1:06:49–1:08:49]. | Priamo T. |
| 2.1, 2.2 | Pozorování | ÚDNT/ÚKNT vs pravdivostní tabulka | Minterm označuje jeden pravdivý, maxterm jeden nepravdivý riadok. [chap-02-vyrokova-logika.woo:2996; chap-02-vyrokova-logika.woo:3048] | Skriptá vysvetlené predchádzajúcimi príkladmi; Spěvák [dml-P2-par2-spevak 56:35–57:13; dml-P2-par2-spevak 1:06:03–1:06:19]. | Priamo T a R2. |
| 2.2 | Důsledek | Souvislost mezi úplnými normálními tvary a pravdivostní tabulkou | Úplné tvary sú jednoznačné až na poradie, počty členov spolu dávajú $2^n$. [chap-02-vyrokova-logika.woo:3077] | Skriptá odvodia z predchádzajúcich výsledkov; Spěvák [dml-P2-par2-spevak 1:09:19–1:11:14]. | Počítanie a čítanie ohodnotení T. |
| 2.13 | Věta | Logická ekvivalence podle úplných normálních tvarů | Pri rovnakých prvotných formulách stačia rovnaké mintermy alebo rovnaké maxtermy. [chap-02-vyrokova-logika.woo:3126] | Skriptá áno z jednoznačnosti; Spěvák [dml-P2-par2-spevak 1:14:44]; Legerský [dml-P3-par3-legersky 18:25–18:41]. | [cv-02-vyrokova-logika.woo:1624]. |
| 2.14 | Věta | Logický důsledek podle úplných normálních tvarů | Pre $A\models B$ idú mintermy $A_d$ do $B_d$, maxtermy opačne. [chap-02-vyrokova-logika.woo:3173] | Skriptá áno; Spěvák [dml-P2-par2-spevak 1:12:13–1:14:14]; Legerský oba smery pre mintermy [dml-P3-par3-legersky 12:44–18:19]. | R1; [cv-02-vyrokova-logika.woo:1303]. |

Doplnky: Věta 2.15 (klasické dôsledky, riadok 3364) je súhrn na výcvik úsudkov, Legerský ju slovne komentuje v P3 22:12 až 24:31; Věta 2.16 (jediné jednoprvkové USS, riadok 3552) má v skriptách dôkaz pre záujemcov, Spěvák ho nedokazuje [dml-P2-par2-spevak 19:18–20:33]. Priame testovanie týchto dvoch viet dodaný výber testov nedokladá.

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Pravdivostná tabuľka, tautológia a splniteľnosť.**

1. Vypísať všetky rôzne prvotné formule a všetkých $2^n$ kombinácií ich hodnôt, každú raz.
2. Vytvárať stĺpce podformulí od jednoduchších k celej formule, počítať podľa piatich tabuliek spojok.
3. Posledný stĺpec: samé 1 znamenajú tautológiu; aspoň jedna 1 splniteľnosť; samé 0 kontradikciu. Tautológia je zároveň splniteľná.
4. Pri otázke na pravdivé ohodnotenia uviesť konkrétne kombinácie, nie iba počet.

Opora: Definice 2.11 a 2.12, otázka za definíciou má riešenie na riadku 968. Skriptá používajú pre dvojicu poradie $11,10,01,00$; nie je doložené, že iné úplné poradie zakazujú. Prvé živé zostavenie tabuľky z VL1 zdroj nemá. [chap-02-vyrokova-logika.woo:735; chap-02-vyrokova-logika.woo:768; chap-02-vyrokova-logika.woo:968; dml-P2-par3-legersky 06:30–07:30]

**B. Rozhodnutie ekvivalencie a dôsledku.**

1. Pre ekvivalenciu porovnať hodnoty $E,F$ na všetkých spoločných ohodnoteniach; stačí aj ukázať, že $E\Leftrightarrow F$ je tautológia.
2. Pre $E\models F$ skontrolovať iba riadky, kde $E=1$: nikde nesmie byť $F=0$. Alternatíva je tautológia $E\Rightarrow F$ alebo kontradikcia $E\wedge\neg F$.
3. Na vyvrátenie stačí jediný svedok: rôzne hodnoty pri ekvivalencii, dvojica $E=1,F=0$ pri dôsledku.
4. Pri dôkaze úpravami písať reťaz ekvivalentných formulí, nie iba výsledok. Náhrady vnútri povoľuje Tvrzení 2.3, záver Věta 2.1.

Legerský takto vyvracia komutativitu implikácie pri $v(A)=1,v(B)=0$ a asociativitu pri $v(A)=v(C)=0$; celá tabuľka na protipríklad netreba. Cvičenie na riadku 1303 výslovne žiada viac spôsobov **bez tabuliek**, ide o miestne obmedzenie úlohy. [dml-P2-par3-legersky 24:58–26:18; Věta 2.1, chap-02-vyrokova-logika.woo:1421; cv-02-vyrokova-logika.woo:1303]

**C. DNT a KNT ekvivalentnými úpravami, obaja prednášajúci.**

1. Odstrániť implikácie pomocou $E\Rightarrow F\mathrel{\vert=\vert}\neg E\vee F$; ekvivalencie prepísať podľa Věty 2.7.
2. Negácie presúvať dovnútra De Morganovými zákonmi; odstrániť dvojité negácie, aby zostali iba pri prvotných formulách.
3. Používať asociativitu a správnu distributivitu: pre DNT rozvinúť konjunkcie disjunkcií, pre KNT disjunkcie konjunkcií.
4. Zjednodušiť: kontradiktorický implikant v DNT zahodiť, tautologickú klausulu v KNT zahodiť; použiť idempotenciu a absorpciu.
5. Zastaviť, keď vonkajšia štruktúra zodpovedá žiadanému tvaru. Ďalšia distributivita môže začať prechod do druhého tvaru.

Opora: Věty 2.3 až 2.7, Tvrzení 2.3 a Poznámka 2.16. Legerský výslovne dopĺňa chýbajúci predkrok odstránenia implikácií/ekvivalencií; Spěvák ho ukazuje na príkladoch pred zoznamom rád. [chap-02-vyrokova-logika.woo:2712; dml-P2-par3-legersky 1:17:10–1:19:50; dml-P2-par2-spevak 33:41–38:45]
Spěvákovo vyjadrenie o neexistencii univerzálneho návodu **pomocou zákonov** neopisovať ako matematickú nemožnosť algoritmu: kontextom je praktický výber úprav, následne ponúka mechanickú tabuľkovú cestu; Věta 2.11 existenciu garantuje. [dml-P2-par2-spevak 38:15–39:15; chap-02-vyrokova-logika.woo:2776]

Najpoužívanejšie zákony pri týchto prevodoch:

- $\neg\neg A\mathrel{\vert=\vert}A$, $A\wedge\neg A\mathrel{\vert=\vert}\bot$, $A\vee\neg A\mathrel{\vert=\vert}\top$. [Věta 2.3, chap-02-vyrokova-logika.woo:1646]
- $A\wedge\top\mathrel{\vert=\vert}A$, $A\vee\bot\mathrel{\vert=\vert}A$; $A\wedge\bot\mathrel{\vert=\vert}\bot$, $A\vee\top\mathrel{\vert=\vert}\top$. [Věta 2.4, chap-02-vyrokova-logika.woo:1703]
- $A\wedge(B\vee C)\mathrel{\vert=\vert}(A\wedge B)\vee(A\wedge C)$ a duálne $A\vee(B\wedge C)\mathrel{\vert=\vert}(A\vee B)\wedge(A\vee C)$. [Věta 2.5, chap-02-vyrokova-logika.woo:1740]
- $\neg(A\wedge B)\mathrel{\vert=\vert}\neg A\vee\neg B$, $\neg(A\vee B)\mathrel{\vert=\vert}\neg A\wedge\neg B$. [Věta 2.5, chap-02-vyrokova-logika.woo:1740]
- $A\vee(A\wedge B)\mathrel{\vert=\vert}A$, $A\wedge(A\vee B)\mathrel{\vert=\vert}A$; oproti tomu $A\vee A\mathrel{\vert=\vert}A$ a $A\wedge A\mathrel{\vert=\vert}A$ sú zákony idempotencie. [Věta 2.5, chap-02-vyrokova-logika.woo:1740]
- $A\Leftrightarrow B\mathrel{\vert=\vert}(A\wedge B)\vee(\neg A\wedge\neg B)$ je vhodný DNT prepis; $(\neg A\vee B)\wedge(A\vee\neg B)$ je KNT prepis. [Věta 2.7, chap-02-vyrokova-logika.woo:2041]

**D. Spoločný prednáškový príklad, pozor na zátvorky.**

- $A\Rightarrow(B\Rightarrow C)\mathrel{\vert=\vert}\neg A\vee\neg B\vee C$: jeden klausulový člen je KNT, disjunkcia jednotlivých literálov zároveň DNT.
- $(A\Rightarrow B)\Rightarrow C\mathrel{\vert=\vert}(A\wedge\neg B)\vee C$: DNT.
- Distribúciou druhého výsledku dostať $(A\vee C)\wedge(\neg B\vee C)$: KNT.

Prvý krok Věta 2.6, posledný Věta 2.5, náhrada Tvrzení 2.3. Rovnaké príklady v slajdoch aj u oboch prednášajúcich. [P01_log_vyrokova.tex:1535; P01_log_vyrokova.tex:1541; dml-P2-par2-spevak 34:16–36:44; dml-P2-par3-legersky 1:13:38–1:15:47]

**E. Zúplnenie úpravami.**

1. Fixovať všetky prvotné formule pôvodného zadania; zjednodušením sa ich zoznam pre účel úplného tvaru nesmie potichu zmeniť.
2. Vyčistiť DNT/KNT od opakovaných literálov, rozporov a triviálnych členov; skontrolovať špeciálne $\bot,\top$.
3. V implikante $I$ doplniť chýbajúce $B$ cez $I\mathrel{\vert=\vert}I\wedge(B\vee\neg B)\mathrel{\vert=\vert}(I\wedge B)\vee(I\wedge\neg B)$.
4. V klausule $K$ použiť duálne $K\mathrel{\vert=\vert}K\vee(B\wedge\neg B)\mathrel{\vert=\vert}(K\vee B)\wedge(K\vee\neg B)$.
5. Opakovať pre každú chýbajúcu premennú, potom odstrániť ekvivalentné duplicity členov. Spěvák odporúča rovnaké abecedné poradie literálov na ich ľahšie rozpoznanie.

Opora: Definice 2.22, Věta 2.12 a zákony Vět 2.3 až 2.5. Obaja ukazujú doplnenie ÚDNT; Legerský ÚKNT v P2 komentuje analogicky. [chap-02-vyrokova-logika.woo:2893; dml-P2-par2-spevak 48:10–52:32; dml-P2-par3-legersky 1:31:45–1:33:36]

**F. ÚDNT a ÚKNT priamo z tabuľky.**

1. Zostaviť tabuľku zadanej formule na všetkých pôvodných premenných.
2. **ÚDNT:** pre každý riadok s výsledkom 1 napísať konjunkciu všetkých premenných; hodnota premennej 1 dá kladný literál, 0 negovaný. Mintermy spojiť disjunkciou.
3. **ÚKNT:** pre každý riadok s výsledkom 0 napísať disjunkciu všetkých premenných; hodnota premennej 0 dá kladný literál, 1 negovaný. Maxtermy spojiť konjunkciou.
4. Napríklad riadok $101$ dá do ÚDNT $(A\wedge\neg B\wedge C)$, ak je výsledok 1; do ÚKNT $(\neg A\vee B\vee\neg C)$, ak je výsledok 0. Ide o dve rôzne podmienky výberu riadka.
5. Bez jednotiek je ÚDNT $\bot$, bez núl je ÚKNT $\top$; naopak ÚDNT tautológie má všetkých $2^n$ mintermov a ÚKNT kontradikcie všetkých $2^n$ maxtermov.

Opora: Pozorování 2.1, 2.2, Důsledek 2.2, konvencia v Poznámke 2.18. **Spěvák výslovne ukazuje oba tabuľkové prevody**, vrátane rekonštrukcie tvaru z hodnôt. Legerského P2 doloží ÚDNT; jeho nasledujúci začiatok P3 nie je čitateľný na potvrdenie samostatnej ukážky ÚKNT. [chap-02-vyrokova-logika.woo:2860; chap-02-vyrokova-logika.woo:2996; chap-02-vyrokova-logika.woo:3048; dml-P2-par2-spevak 54:17–55:44; dml-P2-par2-spevak 1:04:52–1:08:19]

**G. Čítanie výsledku a porovnanie úplných tvarov.**

- Počet pravdivých ohodnotení je počet mintermov, nepravdivých počet maxtermov; ich súčet musí byť $2^n$.
- Pre ekvivalenciu porovnať rovnakosť množín mintermov alebo maxtermov pri rovnakom zozname premenných.
- Pre $E\models F$: každý minterm $E_d$ musí byť vo $F_d$; každý maxterm $F_k$ musí byť v $E_k$. Pri maxtermoch je smer opačný.
- Pamäťová opora z dôkazu: pridaním disjunktu dostávame dôsledok, odobratím konjunktu tiež. Toto zdôvodnenie je lepšie ako nezdôvodnená šípka medzi počtami členov.

Opora: Důsledek 2.2, Věty 2.13 a 2.14. Spěvák tvrdenia preberie v P2, Legerský v P3 vysvetlí oba smery dôkazu pre mintermy. [chap-02-vyrokova-logika.woo:3077; chap-02-vyrokova-logika.woo:3173; dml-P2-par2-spevak 1:12:13–1:14:44; dml-P3-par3-legersky 12:44–18:41]

**H. Ako dokazovať USS.**

1. Nestačí prepísať jedinú konkrétnu formulu: cieľ je ekvivalent pre každú formulu.
2. Možno vyjsť zo známeho USS a vyjadriť všetky jeho spojky novým systémom; netreba zakaždým nanovo všetkých päť pôvodných spojok.
3. Pre $\{\neg,\vee\}$ ide $E\wedge F\mathrel{\vert=\vert}\neg(\neg E\vee\neg F)$, $E\Rightarrow F\mathrel{\vert=\vert}\neg E\vee F$; ekvivalenciu rozložiť na dve implikácie a tiež odstrániť vzniknutú konjunkciu.
4. Legerský ukazuje základný krok pre prvotnú formulu a indukčný krok pre spojky; Spěvák formálnu štrukturálnu indukciu odkladá.
5. NAND: $\neg E\mathrel{\vert=\vert}E\uparrow E$, $E\wedge F\mathrel{\vert=\vert}(E\uparrow F)\uparrow(E\uparrow F)$, čím sa opiera o známy $\{\neg,\wedge\}$.

Opora: Věty 2.9 a 2.10. Pri dôkaze ne-USS riešenia cvičenia 891 obsahujú aj záporné prípady; samotný neúspech nájsť prepis nie je dôkaz. [chap-02-vyrokova-logika.woo:2309; chap-02-vyrokova-logika.woo:2398; cv-02-vyrokova-logika.woo:891; dml-P2-par2-spevak 09:32–10:38; dml-P2-par2-spevak 15:46–18:14; dml-P2-par3-legersky 1:00:09–1:00:28]

## 5. Značenie a terminológia

- **Výrok verzus formule:** výrok je oznamovacia veta, prvotná formule jej symbol; $A,B,C,\ldots$ aj $A_i$, všeobecné formule často $E,F,G$. $v(A)\in\{0,1\}$ je hodnota pri ohodnotení $v$. [Definice 2.1 a 2.2, chap-02-vyrokova-logika.woo:184; chap-02-vyrokova-logika.woo:200]
- Spojky: $\neg$ negace, $\wedge$ konjunkce, $\vee$ disjunkce, $\Rightarrow$ implikace, $\Leftrightarrow$ ekvivalence. Disjunkcia nie je XOR: pravdivá je aj pri dvoch jednotkách; implikácia je nepravdivá iba pri $1\Rightarrow0$. [Definice 2.11, chap-02-vyrokova-logika.woo:745]
- **Väzba medzi formulami sa nesmie zameniť za spojku.** Skriptové makro `\logi` vykresľuje vzťah $E\mathrel{\vert=\vert}F$, ASCII `|=|`; `\thus` vzťah $E\models F$, ASCII `|=`. V slajdoch sú ich presné definície v `preamble.tex:261` a `:262`, Anki používa rovnaký vzhľad. [chap-02-vyrokova-logika.woo:1233; preamble.tex:261; anki-dml.txt:19; dml-P2-par3-legersky 26:41–26:57]
- $E\Leftrightarrow F$ je formule, ktorá môže byť pri niektorom ohodnotení nepravdivá; $E\mathrel{\vert=\vert}F$ je tvrdenie o všetkých ohodnoteniach. Podobne $E\models F$ vyžaduje tautologickosť $E\Rightarrow F$, nie jedinú pravdivú hodnotu tejto implikácie. [Věta 2.1, chap-02-vyrokova-logika.woo:1421]
- $\top$ označuje tautologickú formulu a $\bot$ kontradiktorickú; nie sú to názvy ľubovoľnej pravdivej/nepravdivej hodnoty v konkrétnom riadku. $F\mathrel{\vert=\vert}\top$ vyjadruje tautologickosť, $F\mathrel{\vert=\vert}\bot$ kontradiktorickosť. [chap-02-vyrokova-logika.woo:1183; P01_log_vyrokova.tex:805]
- Kurz píše **DNT, KNT, ÚDNT, ÚKNT**, české „disjunktivní/konjunktivní normální tvar“. Slajdy uvádzajú aj anglické DNF/CNF; historické testy používajú DNF/KNF. Tieto varianty vysvetliť ako tú istú vec, nezavádzať dva rôzne matematické pojmy. Spěvák anglickú skratku v prednáške nepoužíva. [P01_log_vyrokova.tex:1460; P01_log_vyrokova.tex:1487; testy-dml-2.md:49; dml-P2-par2-spevak 23:28–23:40]
- Česká terminológia: **literál**, **implikant**, **klausule**, **minterm**, **maxterm**. Literál je iba prvotná formule alebo jej negácia, nie negácia ľubovoľnej zloženej formule. V prepise zaznieva aj „klauzule“, skriptá píšu „klausule“. [chap-02-vyrokova-logika.woo:2485; chap-02-vyrokova-logika.woo:2545; dml-P2-par2-spevak 32:28]
- USS = „universální systém spojek“; nepliesť s úplnosťou normálneho tvaru. NAND je **Shefferův symbol** $\uparrow$, NOR **Peirceova šipka** $\downarrow$. [chap-02-vyrokova-logika.woo:2274; chap-02-vyrokova-logika.woo:2370]
- Vonkajšie zátvorky sa vynechávajú; vnútorné všeobecne nie. Homogénne reťazce $\wedge$ alebo $\vee$ možno zjednodušiť asociativitou, pri zmiešaných spojkách nemožno zaviesť nevyslovenú prioritu. Kurz tu nepredpisuje programátorské `&&`, `||`, `!` ako svoj formálny jazyk. [chap-02-vyrokova-logika.woo:602; chap-02-vyrokova-logika.woo:633; dml-P2-par3-legersky 18:31–18:59]
- $A_d,A_k$ označujú úplný DNT/KNT formule $A$; $v(ABC)$ v riešení cvičenia znamená trojicu hodnôt v tomto poradí. [chap-02-vyrokova-logika.woo:3091; cv-02-vyrokova-logika.woo:1592]
- Významný doložený rozdiel symbolov medzi paralelkami zdroj nemá; automatické „donut“, „konato“, „denet“ sú skomolenia DNT/KNT, nie samostatná terminológia.

## 6. Čo kto zdôrazňuje, chce a zakazuje

| Kto | Krátky doslovný citát | Význam a citácia |
|---|---|---|
| Legerský | „ty logické zákony uvedeme bez důkazů“ | Každý sa dá overiť tabuľkou; neznamená to zákaz dokazovania. [dml-P2-par3-legersky 07:30] |
| Legerský | „rozhodně netvrdím, že to všechno je potřeba mít na paměť“ | Výber zákonov je redundantný. [dml-P2-par3-legersky 08:00] |
| Legerský | „tak musím pečlivě závorkovat“ | Pri miešaní spojok nemožno preniesť voľnosť homogénnych reťazcov. [dml-P2-par3-legersky 18:55] |
| Legerský | „stačí jedno jediné pravdivostní ohodnocení a mám hotovo“ | Na vyvrátenie ekvivalencie stačí svedok. [dml-P2-par3-legersky 25:48] |
| Legerský | „tohle není znegování“ | Negovanie oboch strán ekvivalencie zachová pôvodnú ekvivalenciu. [dml-P2-par3-legersky 33:37] |
| Legerský | „když napíšu takhle formuly B a zároveň C“ | Zoznam premenných nie je automaticky $A,B,C$; minterm je relatívny k zadaniu. [dml-P2-par3-legersky 1:24:12–1:24:27] |
| Legerský | „musím tenhle nebo jeden z nich musím škrtnout“ | Úplný tvar nesmie obsahovať duplicitné ekvivalentné mintermy. [dml-P2-par3-legersky 1:26:59] |
| Spěvák | „Ale když to budete mít srovnalé podle ABCD“ | Odporúča jednotné poradie na rozpoznanie duplicít, nevyhlasuje ho za podmienku správnosti. [dml-P2-par2-spevak 51:25–51:46] |
| Spěvák | „Může to být náplní.“ | Úplné tvary spája s prvou zápočtovou písomkou; nie je to záruka konkrétneho zadania. [dml-P2-par2-spevak 1:11:30–1:11:42] |
| Skriptá | „Neznamená to prosím, že se je studenti musí učit takto nazpaměť.“ | Ide o názvy zákonov; bežné pojmy komutativita/asociativita/distributivita majú poznať. [chap-02-vyrokova-logika.woo:1582] |
| Vyučujúci DML, Discord | parafráza: každú ekvivalenciu v reťazci treba vedieť zdôvodniť odkazom na niektorú vetu zo skrípt | Odporúča reťaz zdôvodnených ekvivalencií; konkrétne zadanie na obrázku nebolo prečítané. [discord-dml.md:30; discord-dml.md:33] |

Požiadavku či zákaz z proseminára zdroj nemá, titulky chýbajú. V testoch, ktoré výslovne žiadajú „užitím logických zákonů“, samotná tabuľka nenahrádza požadovaný postup. [testy-dml-2.md:49; testy-dml-2.md:56]

## 7. Typické úlohy

Frekvencie nižšie sú počty v dodaných lokálnych prepisoch, nie štatistika všetkých testov. Historické roky nepredstavujú aktuálny harmonogram predmetu.
`sources.json` nemá samostatný kód kapitoly VL: relevantné normálne tvary zaraďuje do `chapter: PL`, `skill: dml.logic`; preto sa vyberali aj podľa textu, nie iba poľa `chapter`.

| Typ | Krátky príklad zadania po česky | Zdroj | Ako často v dodanom výbere |
|---|---|---|---|
| Previesť zákonmi do DNT aj KNT, jeden tvar zúplniť, určiť nepravdivosť | „Formuli $(A\vee B)\Rightarrow(C\wedge\neg A)$ převeďte užitím logických zákonů…“ | [testy-dml-2.md:49], `dml-t2-2022-v1-5`, časti a až c | Dve varianty 25. 11. 2022, každá 7 bodov; druhá formula na riadku 56, `dml-t2-2022-v2-5`. |
| Rovnaký reťazec úloh na inej implikácii | „Najdi KNT, DNT a jedno z úplných DNT či KNT…“ | [testy-dml-2.md:66], `dml-t2-2021-1129-1` | Jeden ďalší test z 29. 11. 2021; prepis otázok bez bodov. |
| Úplné tvary tabuľkou | „úplná DNF a KNF tabulkou“ | [testy-dml-2.md:70], `dml-t2-2021-1203rano-1` | Jedna študentská spomienka; formula je výslovne neistá, nepoužiť ako spoľahlivé zadanie. |
| Vybrať logické dôsledky prirodzenej vety | „Které výroky jsou logickými důsledky výroku…“ | [testy-rozstrel.md:12], 11. 1. 2023, otázka 6 | Jeden doložený rozstrel; veta o nevyspatí a únave. |
| Rozpoznať maxtermy úplného KNT | „Které z formulí jsou maxtermy v úplné KNF…“ | [testy-rozstrel.md:30], `dml-rozstrel-2026-01-29-4` | Jeden rozstrel, 29. 1. 2026. |
| Tabuľka a klasifikácia formule | „Sestrojte pravdivostní tabulky…“ | [cv-02-vyrokova-logika.woo:298] | Jedna sada, časti a až h. |
| Dokázať/rozpoznať USS | „Rozhodněte a zdůvodněte…“ | [cv-02-vyrokova-logika.woo:894] | Päť ponúknutých systémov; oficiálne riešenie na 923. Priamy výskyt v dodaných testoch zdroj nemá. |
| Obmedziť dovolené spojky | „pouze negace a disjunkce“ | [cv-02-vyrokova-logika.woo:1032] | Štyri úlohy v sade od 1022. |
| Prevody a počet pravdivých ohodnotení | „nalezněte její DNT a KNT…“ | [cv-02-vyrokova-logika.woo:1146] | Sedem častí, posledná s hviezdičkou. |
| Dôsledok viacerými postupmi bez tabuliek | „Více způsoby (ale bez pravdivostních tabulek)…“ | [cv-02-vyrokova-logika.woo:1303] | Osem kandidátov, všeobecný postup na 1345. |
| Celý skúškový postup vrátane ohodnotení | „Z nalezeného úplného tvaru odvoďte…“ | [cv-02-vyrokova-logika.woo:1557] | Jedna komplexná úloha zhodná s formulou testu 2022 v1. |

Novšie druhé písomky v súbore majú aj množiny, relácie a čísla; samotný názov `testy-dml-2.md` nie je dôvod preniesť všetky tamojšie úlohy do VL.
Kvantifikované rozstrelové otázky patria PL, aj keď používajú výrokové zákony; nie sú ďalšími priamymi výskytmi VL. [testy-rozstrel.md:7; testy-rozstrel.md:20; testy-rozstrel.md:22]
Samostatný korpus otvorených DML kvízov MARAST k VL tu zdroj nemá; uvedené sú iba historické rozstrely z dovoleného prepisu. Žiadne znenie otvoreného kvízu MARAST sa nepreberá.

## 8. Časté chyby a otázky (Discord, prednášky)

- **„Ako sa dokazuje ekvivalencia?“** Discord zachytáva otázku študentky a radu vyučujúceho ku každej ekvivalencii uviesť oporu vo vete; konkrétna formula nie je známa. Nemožno z toho urobiť presný príbeh chyby vo VL. [discord-dml.md:30–35]
- **Obrátená implikácia verzus obmena.** $B\Rightarrow A$ nie je všeobecne ekvivalent $A\Rightarrow B$; obmena je $\neg B\Rightarrow\neg A$. Legerský vyžiada konkrétne ohodnotenie proti komutativite. [dml-P2-par3-legersky 21:08–22:14; dml-P2-par3-legersky 24:58–25:48]
- **Ignorovanie zátvoriek.** Implikácia nie je asociatívna; dve usporiadania zátvoriek majú odlišné normálne tvary. [dml-P2-par3-legersky 25:55–26:18; P01_log_vyrokova.tex:1535; P01_log_vyrokova.tex:1541]
- **Negácia oboch strán ekvivalencie.** $\neg A\Leftrightarrow\neg B$ je ekvivalent pôvodnej formule, jej negáciu získame negáciou jednej strany. [dml-P2-par3-legersky 33:09–34:34]
- **Minterm bez všetkých premenných alebo s duplicitou.** Bežný implikant môže obsahovať $A\wedge\neg A$, minterm nie; minterm je relatívny k celej zadanej formule. Dva syntakticky rôzne, ale ekvivalentné mintermy sa nesmú opakovať. [dml-P2-par3-legersky 1:23:19–1:28:29]
- **Zámena polarity pri maxtermoch.** Spěvák počas čítania tabuľky sám opraví negácie; pri ÚKNT sa hľadá klausula nepravdivá v nulovom riadku. Nepreberať pôvodný nesprávny zápis ako metódu. [dml-P2-par2-spevak 1:04:52–1:05:52]
- **Obrátený smer dôsledku.** Pri porovnávaní ekvivalencie a implikácie publikum navrhne opačný smer; správny je $(A\Leftrightarrow B)\models(A\Rightarrow B)$. [dml-P2-par2-spevak 1:14:44–1:16:14]
- **Zamenenie DNT/KNT s najkratším zápisom.** Obyčajné tvary nie sú jednoznačné, úplné sú jednoznačné iba až na poradie a pri fixovaných premenných. [dml-P2-par3-legersky 1:20:39–1:22:29; Důsledek 2.2, chap-02-vyrokova-logika.woo:3077]

Ďalšie konkrétne študentské omyly vo VL lokálny Discordový výskum zdroj nemá; súbor sám uvádza neúplné prehľadanie a neprečítané obrázky. [discord-dml.md:5; discord-dml.md:18]

## 9. Rozdiely medzi paralelkami

- **Dombek:** z P2 nie je dostupný výklad výpočtových postupov VL; už motivuje jemnejší jazyk cez výroky o Karlovi a konečnom/nekonečnom univerze. Nesmie sa mu pripísať Spěvákov alebo Legerského postup. [dml-P2-par1-dombek 07:04–15:31]
- **Spěvák:** zákony už prebral v nenahratej P1; v P2 dokončí USS, oba normálne tvary, oba tabuľkové prevody, kritérium dôsledku aj obvody. Pri NAND-e explicitne skladá negáciu a konjunkciu, formálnu štrukturálnu indukciu odkladá. [dml-P2-par2-spevak 06:45–06:59; dml-P2-par2-spevak 09:32–10:38; dml-P2-par2-spevak 15:46–18:14; dml-P2-par2-spevak 1:19:14]
- **Legerský:** v P2 začína zákonmi, venuje veľa priestoru zátvorkám, negáciám a štrukturálnej indukcii pre $\{\neg,\vee\}$. NAND/NOR nevypisuje celé; do konca P2 stihne korespondenciu mintermov s tabuľkou. Čitateľné dokončenie dôsledku a doplnkov má P3. [dml-P2-par3-legersky 06:07; dml-P2-par3-legersky 45:53–57:23; dml-P2-par3-legersky 1:00:00–1:00:54; dml-P2-par3-legersky 1:36:11–1:36:24; dml-P3-par3-legersky 12:20–25:57]
- **Spoločné:** príklady s dvoma rôzne uzátvorkovanými implikáciami aj doplnenie mintermu cez tautológiu. Rozdiel je v tempe a podrobnosti dôkazov, nie v definícii DNT/KNT. [dml-P2-par2-spevak 34:16–36:44; dml-P2-par3-legersky 1:13:38–1:15:47; dml-P2-par3-legersky 1:32:42–1:33:36]

## 10. Nejasné a chýbajúce

- **VL1 všetkých paraleliek nemá nahrávku.** Pre § 2.1 až 2.5 je opora vo formalizovaných skriptách, slajdoch a Anki; presný prvý hovorený výklad, výber príkladov a požiadavky jednotlivých učiteľov nemožno rekonštruovať. Neskoršie rekapitulácie túto medzeru neodstraňujú.
- **Prosemináre 25/26-01 a 02 nemajú titulky**, hoci INDEX má stav „done“; rozhodujúci je počet 0 cues. Nemožno tvrdiť, kto tam čo zakazuje alebo vyžaduje. [INDEX.md:29; INDEX.md:30]
- **Legerského P3 pred 12:20 je poškodený prepis.** Opakované nezrozumiteľné titulky nepreukazujú, čo zaznelo o ÚKNT a počítaní; od 12:20 je doložené dokončenie VL. INDEX označuje nahrávku iba PL, čo je neúplný tematický popis. [dml-P3-par3-legersky 05:28–12:20; INDEX.md:8]
- `DML_P01_log_vyrokova.tex` je iba vstupný súbor s 10 riadkami a dvoma vstupmi. Vlastné citácie slajdov preto odkazujú na skutočný obsah `P01_log_vyrokova.tex`, nie na vymyslené riadky vstupu. [DML_P01_log_vyrokova.tex:8; DML_P01_log_vyrokova.tex:10]
- `sources.json` zlučuje relevantné úlohy VL pod `PL`; jeho štítok nie je dôkaz, že úloha s $A,B,C$ obsahuje predikáty. Zároveň sa nesmú počítať testové podotázky a ich rovnaké cvičebné predlohy ako nezávislé testové výskyty.
- V historickej študentskej spomienke z 3. 12. 2021 je samotná formula neistá; výťah používa iba doložený typ úlohy, nie jej znenie ako spoľahlivý vzor. [testy-dml-2.md:70]
- **Názvy klasických pravidiel sú v zdrojoch sporné:** Věta 2.15 označuje $(A\vee B)\wedge\neg A\models B$ ako „Modus tollens“ a $(A\Rightarrow B)\wedge\neg B\models\neg A$ ako „Reduction ad absurdum“. Rovnako to preberá Anki; vzorce sú použiteľné, názvoslovie bez ďalšieho overenia neprezentovať ako všeobecnú terminologickú normu. [chap-02-vyrokova-logika.woo:3384; chap-02-vyrokova-logika.woo:3390; chap-02-vyrokova-logika.woo:3396; anki-dml.txt:49]
- Spěvák v 1:16:44 slovne obráti obsahovanie maxtermov pri príklade, hoci predtým kritérium vysvetlí správne. Použiť presnú Větu 2.14: maxtermy záveru sú obsiahnuté medzi maxtermami predpokladu. [dml-P2-par2-spevak 1:12:13–1:12:43; dml-P2-par2-spevak 1:16:44; chap-02-vyrokova-logika.woo:3188]
- Spěvákovo váhanie v názvoch idempotencie („absorpce nebo tak nějak“) nepreberať do terminológie; $A\wedge A\mathrel{\vert=\vert}A$ je v skriptách idempotencia. [dml-P2-par2-spevak 16:47–16:59; Věta 2.5, chap-02-vyrokova-logika.woo:1740]
- Legerského 1:30:00 má v prepise zmätenú formuláciu výnimky pre úplný tvar kontradikcie. Rozhodujúce sú jasné skriptá: $\bot$ ako prázdny ÚDNT, $\top$ ako prázdny ÚKNT. [dml-P2-par3-legersky 1:29:48–1:30:00; chap-02-vyrokova-logika.woo:2872]

## 11. Surové zdroje

Základ `kontext/`:

- `BRIEF.md`, pravidlá, rozsah chýbajúcich P1 a citačná osnova; `INDEX.md:3–8,29–30`, inventár nahrávok.
- `cisla/dml-kap2.tsv`, celé, jediný zdroj čísel pomenovaných objektov.
- `txt/dml-P2-par1-dombek.txt`, 07:04 až 15:31, iba overenie prechodu do PL a limitov VL.
- `txt/dml-P2-par2-spevak.txt`, 03:33 až 1:19:44, hlavný zdroj USS, normálnych tvarov, oboch tabuľkových konštrukcií a dôsledku.
- `txt/dml-P2-par3-legersky.txt`, 06:01 až 1:36:24, hlavný zdroj zákonov, štrukturálnej indukcie a úplných tvarov.
- `txt/dml-P3-par3-legersky.txt`, kontrola začiatku do 12:20, čitateľné dokončenie VL 12:20 až 25:57; PL od 26:05.
- `skripta-dml/bi-dml-master/src/slides/DML_P01_log_vyrokova.tex:8–10`, vstup; `P01_log_vyrokova.tex:23–2128`, vlastný obsah; `preamble.tex:260–262`, značky vzťahov.
- `anki-dml.txt:2–51`, sekcia Výroková logika, znenia definícií a viet, príklady a otázky; riadok 52 už začína PL.

Základ `zdroje/`:

- `src/bi-dml-master/src/textbook/chap-02-vyrokova-logika.woo`, najmä 184 až 215, 586 až 800, 938 až 1008, 1136 až 1462, 1554 až 2175, 2274 až 2448, 2485 až 3211 a 3364 až 3588.
- `src/bi-dml-master/src/textbook/cv-02-vyrokova-logika.woo`, inventár celej kapitoly; najmä 295 až 494, 891 až 1083, 1143 až 1649.
- `testy-dml-2.md:49,56,66,70`, staré prevody VL; `testy-rozstrel.md:12,30`, priame otázky VL.
- `discord-dml.md:5–35`, hranice výskumu a doložená otázka k dôkazom ekvivalencie.

Ďalší inventár: `shared/src/commonMain/composeResources/files/coverage/sources.json`, DML položky `dml-t2-2022-v1-5`, `dml-t2-2022-v2-5`, `dml-t2-2021-1129-1`, `dml-t2-2021-1203rano-1`, `dml-rozstrel-2026-01-29-4`.

Kontrola pred odovzdaním: náhodne vybraných 12 prednáškových citácií porovnaných s časom a obsahom zdroja; osobitne overené značenie v Anki, vstup slajdov a počty častí cvičení.

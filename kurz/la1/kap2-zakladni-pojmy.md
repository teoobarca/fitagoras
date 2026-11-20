# BI-LA1 kap. 2: Základní pojmy lineární algebry

Balík kapitoly: skriptá `chap-03.woo`, slajdy 2, kód rozvrhu S2; v appke staré súbory LA1 kapitoly 2 (`Lessons.kt`, `Concepts.kt`, `Theorems.kt`, `Templates.kt`, `Papers.kt`).

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## Časť 1: vektorový priestor, podpriestor, LN a LZ, lineárny obal (chap-03.woo:1–1537)

### 1. Rozsah a zdroje

Rozsah: `chap-03.woo:1–1537`, od „Prostor šipek v rovině“ cez vektorový priestor, podpriestor, LN/LZ a lineárny obal po „Lineární obal a lineární (ne)závislost“.
Prvý číslovaný výsledok je Pozorování 2.1, posledný Věta 2.42. Čísla sú výhradne z `cisla/la1-kap2.tsv`.
Definícia generovania 2.43, báza, dimenzia a súradnice patria do druhej časti a tu sa nevykladajú.

Časové hranice sú podľa titulkov, nie podľa zmeny slajdu na obraze:

- Kleprlík: `la1-P3-par1-kleprlik 1:23:14–1:33:26`, motivácia šípkami a prechod k $T^n$; `la1-P4-par1-kleprlik 09:39–1:35:59`, priestor, podpriestor a LN/LZ; `la1-P5-par1-kleprlik 13:13–1:02:25`, opakovanie, GEM, obal a výsledky po 2.42. O 1:02:59 už začína generovanie.
- Klouda: koniec `la1-P3-par2-klouda` ešte dokončuje predchádzajúcu kapitolu; o 1:36:12 výslovne oznamuje dokončenie prvej prezentácie. Predbežná motivácia riešeniami homogénnej sústavy je už 43:06–43:17. Vlastný výklad tejto časti: `la1-P4-par2-klouda 08:04–1:37:01` a `la1-P5-par2-klouda 07:37–1:02:44`; o 1:04:10 už zavádza bázu.
- Krásenský: `la1-P3-par3-krasensky` ešte rieši sústavy; zmienka o $S_0$ ako podpriestore je 46:21, koniec 1:36:02. Vlastný výklad: `la1-P4-par3-krasensky 07:40–1:37:49` a `la1-P5-par3-krasensky 06:59–1:30:01`; o 1:30:55 už prichádza generovanie.
- Proseminár `la1-prosem2223-02`: titulky chýbajú, podľa `INDEX.md` má 0 cues. Nepripisujem mu časované výroky ani postupy, ktoré poznáme iba z písanej cvičebnice.

Prvá časť je v uvedených prednáškach prebraná; nenašiel som jej tému, ktorá by nebola vysvetlená v žiadnej paralelke.
To neznamená, že každý dôkaz zaznel všade: napríklad Krásenský dôkaz 2.42 vynecháva pre čas [la1-P5-par3-krasensky 1:29:17–1:30:01].
Slajdy zodpovedajú rozsahu `bi-la1-prezentace-02.tex:27–543`; od riadka 544 sa začína druhá časť.

### 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. Šípky zo strednej školy majú súčet podľa rovnobežníka a násobenie číslom. Kleprlík nimi motivuje operácie, ktoré potom budú platiť aj bez geometrickej predstavy [la1-P3-par1-kleprlik 1:23:14–1:26:30; Pozorování 2.1, chap-03.woo:133].

2. Dvojice nahradíme $n$-ticami nad jedným zvoleným telesom: nemusíme budovať osobitnú teóriu pre každý počet zložiek. Klouda za pracovný model berie priamo $T^n$, Krásenský vysvetľuje ekvivalenciu stĺpcového zápisu a n-tice s čiarkami [la1-P4-par2-klouda 17:47–18:17; la1-P4-par3-krasensky 07:40–08:10; Definice 2.2, chap-03.woo:188].

3. Množina riešení homogénnej sústavy vedie k otázke, v ktorých množinách zostávajú súčet a násobok. Odpoveďou je podpriestor, Definice 2.7, s tromi podmienkami a predpokladom $P\subseteq T^n$ [la1-P4-par1-kleprlik 13:21–20:31; chap-03.woo:361].

4. Uzavretosť nie je len slovíčko: štvorček alebo kvadrant sa pri dovolených operáciách rozbijú. Klouda postupným pridávaním násobkov a súčtov motivuje najmenší podpriestor obsahujúci zadané vektory [la1-P4-par2-klouda 30:36–38:18; Poznámka 2.15, chap-03.woo:627].

5. Nula musí v podpriestore byť, ale jej prítomnosť sama nestačí. Krásenský odlišuje $\emptyset$, $\theta$ a $\{\theta\}$ a geometricky vysvetľuje, prečo v $\mathbb R^2$ zostávajú iba počiatok, priamky cez počiatok a celý priestor [la1-P4-par3-krasensky 36:09–43:00; Pozorování 2.8, chap-03.woo:405].

6. Prienik podpriestorov zachová obidve uzavretosti a obsahuje spoločný nulový vektor. Zjednotenie dvoch rôznych priamok cez počiatok môže zlyhať na súčte [la1-P4-par1-kleprlik 42:07–47:56; Věta 2.18, chap-03.woo:718].

7. Podpriestor chceme popisovať pomocou vektorov bez nadbytočných prvkov. Zavedieme súbor, lineárnu kombináciu a otázku, či možno netriviálne nakombinovať nulový vektor, teda LN/LZ podľa Definice 2.23 [la1-P4-par1-kleprlik 50:48–51:18, 1:05:39–1:10:29; chap-03.woo:820,881].

8. Definícia LN/LZ sa stane výpočtom: neznáme sú koeficienty kombinácie, z rovnosti zložiek vznikne homogénna sústava. Stĺpce obsahujú zadané vektory; GEM rozhodne o jedinom alebo viacerých riešeniach [la1-P4-par2-klouda 1:11:21–1:15:41; Postup 2.27, chap-03.woo:1052].

9. Všetky kombinácie naraz vytvoria lineárny obal, Definice 2.33. Klouda vysvetľuje, že jeho neprázdnosť a obe uzavretosti presne dávajú podpriestor; Krásenský ich počíta rozpisom koeficientov [la1-P5-par2-klouda 19:41–20:45; la1-P5-par3-krasensky 40:14–47:14; Věta 2.38, chap-03.woo:1405].

10. Nadbytočný vektor možno vyhodiť bez zmeny obalu; vektor mimo obalu možno pridať k LN súboru. Klouda dokazuje obe hlavné vety, Krásenský ukazuje praktické rozpoznanie závislosti a druhý dôkaz odkazuje do skrípt [la1-P5-par2-klouda 42:26–1:02:34; la1-P5-par3-krasensky 1:27:13–1:30:01; Věty 2.39 a 2.42, chap-03.woo:1426,1495].

### 3. Pojmy a tvrdenia

„Dôkaz v skriptách“ rozlišuje rozpísaný dôkaz od odkazu na definíciu alebo úlohy pre čitateľa. Pri definícii sa dôkaz nepožaduje.
Testová dôležitosť znamená výskyt v lokálnych zadaniach, nie odhad budúceho testu.

| Číslo | Druh | Názov | Jadro | Dôkaz v skriptách; na prednáške | Dôležitosť pre test |
|---|---|---|---|---|---|
| 2.2 | Definice | Vektorový prostor $T^n$ | $n$-tice nad telesom so súčtom a násobením skalárom po zložkách. | Definícia, nedokazuje sa; [la1-P4-par2-klouda 17:47–19:57]. | Predpoklad všetkých výpočtov [testy-la1-2.md:10,27]. |
| 2.7 | Definice | Podprostor | Neprázdna podmnožina $T^n$ uzavretá na súčet a všetky skalárne násobky. | Definícia; [la1-P4-par1-kleprlik 19:04–20:31]. | Priama definícia aj rozhodovanie [testy-la1-2.md:6,40,50]. |
| 2.8 | Pozorování | O jednoduchých vlastnostech podprostorů | Podpriestor obsahuje $\theta$; $\{\theta\}$ aj $T^n$ sú podpriestory. | Skriptá: na čitateľa; [la1-P4-par3-krasensky 36:09–40:26] odôvodňuje. | Dôkaz nuly a protipríklady [testy-la1-2.md:81]. |
| 2.9 | Definice | Triviální/vlastní podprostor | Triviálne sú $\{\theta\},T^n$; vlastný znamená $P\ne T^n$. | Definícia; [la1-P4-par3-krasensky 47:48] uvádza triviálne podpriestory. | Výpis netriviálnych podpriestorov [la1-marast-13691]. |
| 2.10 | Poznámka | O užitečnosti obměny implikace | Ak $\theta\notin P$, potom $P$ nie je podpriestor; obrátenie neplatí. | Skriptá: vysvetlenie; [la1-P4-par3-krasensky 36:09–36:28]. | Rýchle zamietnutie [testy-la1-2.md:40]. |
| 2.18 | Věta | O operacích s podprostory | Prienik je podpriestor; zjednotenie ním byť nemusí. | Skriptá: áno, prienik a protipríklad; [la1-P4-par1-kleprlik 42:07–47:56]. | Nácvik dôkazov z definície; prednášajúci pripúšťa písemku P4 42:56–43:10. |
| 2.21 | Definice | Lineární kombinace | $\sum_i\alpha_i\mathbf x_i$, $\alpha_i\in T$; triviálna má všetky koeficienty nulové. | Definícia; [la1-P5-par2-klouda 09:01] rekapituluje. | Nevyhnutná pre LN/LZ a obal [testy-la1-2.md:12,23]. |
| 2.23 | Definice | Lineární (ne)závislost | LN: nulový vektor vznikne iba triviálnou kombináciou; LZ: existuje netriviálna nulová kombinácia. | Definícia; [la1-P4-par1-kleprlik 1:05:39–1:10:29]. | Opakované definície [testy-la1-2.md:12,23,29,35,50,63,69,74]. |
| 2.25 | Pozorování | O základních vlastnostech lineární (ne)závislosti | Nula či opakovanie spôsobí LZ; pridanie zachová LZ, odobratie zachová LN. | Skriptá: dôkaz bodov 2 a 6, zvyšok na čitateľa; [la1-P4-par2-klouda 1:25:16–1:31:38]. | Priame dôkazy [testy-la1-2.md:50,63,74]. |
| 2.27 | Postup | Ověření LN/LZ souboru vektorů | Riešiť homogénnu sústavu pre koeficienty, potom rozhodnúť jediné/viaceré riešenia. | Skriptá: odvodenie a príklady; [la1-P5-par3-krasensky 27:12–29:27]. | Početná LN/LZ [testy-la1-2.md:10,38; la1-marast-13613]. |
| 2.28 | Příklad | LN nad $\mathbb Z_5$ | Konkrétna trojica v $\mathbb Z_5^3$ má iba triviálne riešenie. | Skriptá: úplný výpočet; [la1-P5-par1-kleprlik 19:32–20:53]. | Modulárny GEM [la1-marast-13613/a]. |
| 2.30 | Pozorování | O vyšetřování LN/LZ a matici příslušné SLR | Vektory sú v stĺpcoch matice s nulovou pravou stranou. | Skriptá: vysvetlenie; [la1-P4-par2-klouda 1:13:33], 1:15:30. | Povinné prepojenie definície a GEM [testy-la1-2.md:3]. |
| 2.31 | Varování | Ne slepému GEM! | Matica bez vysvetlenia, čo a prečo riešime, nestačí. | Nie je veta; [la1-P5-par1-kleprlik 22:04–22:58]. | Výslovná požiadavka na zdôvodnenie [testy-la1-2.md:3]. |
| 2.32 | Pozorování | O délce souboru garantující LZ v $T^n$ | Ak $m>n$, súbor je LZ. | Skriptá: áno cez počet hlavných stĺpcov; [la1-P5-par3-krasensky 29:27–31:10]. | Rýchle rozhodovanie [la1-marast-13430]. |
| 2.33 | Definice | Lineární obal | Množina všetkých lineárnych kombinácií daného súboru. | Definícia; [la1-P5-par2-klouda 10:49–12:01]. | Priama definícia aj príslušnosť [testy-la1-2.md:23,27,35]. |
| 2.34 | Pozorování | O základních vlastnostech lineárního obalu | Obal obsahuje nulu a pôvodné vektory, je uzavretý; pridanie kombinácie ho nezmení. | Skriptá: rozpísaný súčet, ostatné stručne/na čitateľa; [la1-P5-par2-klouda 19:41–23:44]. | Dôkaz rovnosti obalov [testy-la1-2.md:23]. |
| 2.35 | Důsledek | O shodnosti obalů lišících se o vektor | $\mathbf z\in\langle X\rangle\Leftrightarrow\langle X\rangle=\langle X,\mathbf z\rangle$. | Skriptá: odvodenie z 2.34; [la1-P5-par3-krasensky 49:59–1:01:46] rozoberá pridanie. | Presne tento typ dokazovania [testy-la1-2.md:23]. |
| 2.37 | Příklad | Obal nad rôznymi telesami | Obal dvoch konkrétnych vektorov je $\{(\alpha,\beta,\alpha+\beta,0)\}$. | Skriptá: výpočet a výpis; [la1-P5-par3-krasensky 1:11:35–1:16:06]. | Výpis nad $\mathbb Z_2$ [testy-la1-2.md:35]. |
| 2.38 | Věta | O lineárním obalu a podprostoru | Každý lineárny obal je podpriestor. | Skriptá: plynie z 2.34; [la1-P5-par2-klouda 20:03–20:37]. | Vysvetľuje legitimitu označenia podpriestor v úlohách [testy-la1-2.md:27]. |
| 2.39 | Věta | O vztahu LZ a lineárního obalu | Pre $m\ge2$ je súbor LZ práve vtedy, keď niektorý vektor leží v obale ostatných. | Skriptá: áno, obe implikácie; [la1-P5-par2-klouda 42:26–51:50]. | Dôkaz pre trojicu priamo z definície [testy-la1-2.md:12]. |
| 2.41 | Důsledek | O lineárním obalu LZ souboru | Z LZ súboru dĺžky aspoň 2 možno niektorý vektor odstrániť bez zmeny obalu. | Skriptá: odvodenie z 2.39 a 2.34; [la1-P5-par3-krasensky 1:27:43–1:29:12]. | Odstraňovanie nadbytočných vektorov [la1-marast-13462]. |
| 2.42 | Věta | O prodlužování LN souboru | Pridaním vektora mimo obalu k LN súboru vznikne LN súbor. | Skriptá: áno; [la1-P5-par2-klouda 55:10–1:02:34]; [la1-P5-par3-krasensky 1:30:01] dôkaz vynecháva. | Podklad konštrukcií; samotné dopĺňanie bázy až druhá časť [testy-la1-2.md:11]. |

Časové odkazy v tabuľke smerujú na rovnaké súbory titulkov ako odkazy v ostatných častiach.
Nezamieňať čísla výsledkov s poradím odrážok na slajdoch: obsah 2.34 je na slajdoch rozdelený a preusporiadaný [bi-la1-prezentace-02.tex:440–479].

### 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**Overenie podpriestoru podľa definície.**

1. Uviesť ambientný priestor a overiť $P\subseteq T^n$. Dvojice nie sú trojice: samotné $\mathbb R^2$ nie je podpriestor $\mathbb R^3$ [Varování 2.16, chap-03.woo:649].
2. Neprázdnosť doložiť konkrétnym prvkom, typicky overením $\theta\in P$.
3. Zvoliť **ľubovoľné** $\mathbf x,\mathbf y\in P$. Z ich definičných podmienok odvodiť, že $\mathbf x+\mathbf y$ spĺňa tie isté podmienky.
4. Zvoliť **ľubovoľné** $\alpha\in T$ a $\mathbf x\in P$. Odvodiť podmienky pre $\alpha\mathbf x$.
5. Uzavrieť, že platia všetky tri body Definice 2.7 [chap-03.woo:361; la1-P4-par3-krasensky 31:19–33:05].

Pre $P=\{(x,y)\in\mathbb R^2\mid x+2y=0\}$ sa na papier píše:
$\theta\in P$; pre $\mathbf u,\mathbf v\in P$ platí $(u_1+v_1)+2(u_2+v_2)=(u_1+2u_2)+(v_1+2v_2)=0$;
a pre $\alpha\in\mathbb R$ platí $\alpha u_1+2\alpha u_2=\alpha(u_1+2u_2)=0$.
Ide o rozpísanie definície pre príklad zo skrípt, nie o doslovný prepis tabule [Příklad 2.13, chap-03.woo:526; bi-la1-prezentace-02.tex:253].

Kleprlík nechce iba vetu „je uzavřená“: treba vysvetliť, čo uzavretosť znamená. Pripúšťa presný slovný zápis, teda nie je nutné naspamäť kopírovať symboly [la1-P4-par1-kleprlik 24:48–25:46].
Konkrétne dva nakreslené vektory ilustrujú univerzálnu podmienku, ale nedokazujú ju [la1-P4-par1-kleprlik 22:02].

**Ako podpriestor zamietnuť.**

Stačí jediná nesplnená podmienka: prázdna množina; neprítomnosť nuly; dva jej prvky so súčtom mimo; alebo skalárny násobok mimo.
Pri protipríklade výslovne overiť, že vstupy patria do množiny a výsledok nepatrí.
Skriptá: $x\ge1$ zamieta nulový vektor; $|x|=|y|$ obsahuje nulu, ale $(1,1)+(1,-1)=(2,0)$ už nevyhovuje [Poznámka 2.10, Příklady 2.11–2.12, chap-03.woo:442,454,481].

V konečnom teste netreba mechanicky overovať všetky dvojice, keď už máme protipríklad: zadanie výslovne povoľuje nesplnenie odôvodniť [testy-la1-2.md:6].

**Riešenia homogénnej sústavy sú podpriestor.**

Označiť $S_0=\{\mathbf x\in T^n\mid A\mathbf x=\theta\}$.
Potom $A\theta=\theta$, $A(\mathbf x+\mathbf y)=A\mathbf x+A\mathbf y=\theta$ a $A(\alpha\mathbf x)=\alpha A\mathbf x=\theta$.
Tým sa overia tri podmienky 2.7; skriptá na túto väzbu upozorňujú hneď za definíciou [chap-03.woo:396; la1-P4-par1-kleprlik 13:21–18:42; testy-la1-2.md:6,81; la1-marast-13691/b].

**Prienik a zjednotenie.**

Pre $P\cap Q$ najprv uviesť spoločný prvok $\theta$. Ak sú $\mathbf x,\mathbf y$ v prieniku, sú v oboch podpriestoroch, takže ich súčet aj skalárny násobok sú tiež v oboch.
Na zjednotenie stačí protipríklad dvoch súradnicových osí: $(1,0)+(0,1)=(1,1)$ nie je na žiadnej osi [Věta 2.18, chap-03.woo:718; la1-P4-par1-kleprlik 42:07–47:56].

**LN/LZ univerzálne cez GEM, vektory do stĺpcov.**

1. Deklarovať neznáme koeficienty $\alpha_1,\ldots,\alpha_m\in T$ a napísať $\alpha_1\mathbf x_1+\cdots+\alpha_m\mathbf x_m=\theta$.
2. Operácie rozpísať po zložkách, použiť rovnosť zodpovedajúcich zložiek.
3. Vzniká homogénna sústava $n$ rovníc o $m$ neznámych: $[\mathbf x_1\ \cdots\ \mathbf x_m\mid\theta]$.
4. GEM previesť do HST. Pravá nulová strana je vedľajší stĺpec; ak sú všetky koeficientové stĺpce hlavné, existuje iba nulové riešenie a súbor je LN.
5. Ak je vedľajší aj niektorý koeficientový stĺpec, existuje netriviálne riešenie a súbor je LZ. Pri čistej otázke LN/LZ netreba dopočítať všetky riešenia.

Zdôvodnenie: Definice 2.23, Postup 2.27 a Pozorování 2.30 [chap-03.woo:881,1052,1142].
Kleprlík: [la1-P5-par1-kleprlik 19:32–22:58]. Klouda: [la1-P4-par2-klouda 1:11:21–1:17:27]. Krásenský: [la1-P5-par3-krasensky 27:12–29:27].
Všetci traja v týchto úsekoch používajú **stĺpce** a odvodzujú ich z kombinácie; nenachádzam tu konkurenčný postup s pôvodnými vektormi v riadkoch.

**Kedy sa objavujú riadky a prečo to nie je rozpor.**

Cvičebnica pri #13486 dáva vektory do riadkov, pretože hľadá opis rovnakého obalu a využíva neskoršie tvrdenie o zachovaní riadkového obalu pri GEM.
Sama upozorňuje na znalosti kapitoly 4. Tento postup nie je v tejto prvej časti odvodený a nemožno ním nahradiť vysvetlenie sústavy pre koeficienty [marast-problems/lecture-256.md:189–220].
Rozdiel medzi zápisom jednej n-tice „do riadka s čiarkami“ a uložením viacerých vektorov do riadkov matice musí zostať explicitný [la1-P4-par3-krasensky 07:40–08:10].

**Rýchle LN/LZ bez GEM.**

Pred počítaním skontrolovať nulu, dva rovnaké vektory, pri dvojici násobok a počet vektorov $m>n$ [Pozorování 2.25 a 2.32, chap-03.woo:949,1166].
LZ možno doložiť jednou konkrétnou netriviálnou kombináciou rovnou $\theta$.
Pri symbolických vektoroch napríklad $\mathbf x-\mathbf y-(\mathbf x-\mathbf y)=\theta$ s koeficientmi $(1,-1,-1)$ rieši závislosť súboru $(\mathbf x,\mathbf y,\mathbf x-\mathbf y)$ [testy-la1-2.md:29; Definice 2.23].
Ak zadanie žiada dôkaz z definície, uviesť túto kombináciu aj nenulový koeficient, nie iba odkaz na vetu [testy-la1-2.md:12,29].

**Príslušnosť vektora do obalu.**

Riešiť $\alpha_1\mathbf x_1+\cdots+\alpha_m\mathbf x_m=\mathbf v$, teraz s pravou stranou $\mathbf v$, nie automaticky $\theta$.
Vektory sú opäť v stĺpcoch, sústava je $[\mathbf x_1\ \cdots\ \mathbf x_m\mid\mathbf v]$.
Aspoň jedno riešenie znamená $\mathbf v\in\langle\mathbf x_1,\ldots,\mathbf x_m\rangle$; sporový riadok znamená nepríslušnosť.
Viacero riešení neprekáža: otázka je existencia kombinácie, nie jej jednoznačnosť [Definice 2.33, chap-03.woo:1194; marast-problems/lecture-256.md:147–175, #13465].

V #13465 sa nad $\mathbb R$ nájdu koeficienty $(2,1,\tfrac12)$, nad $\mathbb Z_2$ sa iný zadaný vektor do obalu nedostane a nad $\mathbb Z_5$ má ďalší príklad viacero vyjadrení.
Je to písaný postup cvičebnice, nie overený prepis proseminára [marast-problems/lecture-256.md:153–175].

**Parameter a výrok $a\in\mathbb R$.**

V testoch ide o dve odlišné otázky: pre ktoré $a\in\mathbb R$ je súbor LN; alebo pre ktoré $a\in\mathbb R$ je zadaný vektor v obale [testy-la1-2.md:10,27,33,38,53,62,72].
V prvej úlohe je pravá strana nulová a chceme iba triviálne riešenie; v druhej je pravá strana zadaný vektor a chceme aspoň jedno riešenie.
Neznámymi sú koeficienty kombinácie. Parameter $a$ určuje, ktorú sústavu z rodiny skúmame [Definice 2.23 a 2.33; marast-problems/lecture-256.md:137–175].

Pri vlastnom spracovaní parametra treba pred delením výrazom $f(a)$ oddeliť prípad $f(a)=0$. Je to matematický dôsledok podmienky nenulového násobenia pri GEM, nie citát konkrétneho vyučujúceho z týchto úsekov.
Záver písať ako množinu prípustných hodnôt v deklarovanom telese, napríklad $a\in\mathbb R\setminus\{a_0\}$, až keď je konkrétna výnimka vypočítaná.
Zdroj má konkrétne zadania s $a\in\mathbb R$, ale v čítaných úsekoch P3–P5 nemá overený súvislý výklad vetvenia GEM podľa parametra; nevymýšľam mu čas [testy-la1-2.md:10,38; la1-marast-13433].

**Výpočty nad $\mathbb Z_p$.**

Teleso je súčasť zadania: aj koeficienty kombinácie patria doň. Súčet, súčin a inverzie počítať modulo $p$, nie ako bežné reálne zlomky.
V príklade 2.28 nad $\mathbb Z_5$ dávajú vektory $(1,2,3),(4,2,3),(3,4,2)$ tri hlavné koeficientové stĺpce, teda LN [chap-03.woo:1087; la1-P5-par1-kleprlik 19:32–20:53].
„Viac riešení“ nad konečným telesom neznamená „nekonečne veľa“ [la1-P3-par2-klouda 1:34:50–1:34:58].
GEM potrebuje inverziu nenulového prvku; Krásenský výslovne upozorňuje, že bez telesa sa môže rozbiť [la1-P3-par3-krasensky 1:35:00–1:35:04].

Pri výpise obalu dosadiť všetky kombinácie koeficientov a odstrániť opakovania výsledkov.
Príklad 2.37 má $\langle(1,0,1,0),(0,1,1,0)\rangle=\{(\alpha,\beta,\alpha+\beta,0)\mid\alpha,\beta\in T\}$.
Nad $\mathbb Z_2$ sú to $(0,0,0,0),(0,1,1,0),(1,0,1,0),(1,1,0,0)$; nad $\mathbb Z_3$ deväť vektorov [chap-03.woo:1352; la1-P5-par3-krasensky 1:11:35–1:16:06].
Počet dvojíc koeficientov nie je všeobecne počet rôznych výsledkov: v tomto príklade rôznosť vidno z prvých dvoch zložiek. Všeobecnú teóriu počtu prvkov podpriestoru ponechávam druhej časti.

**Dôkazy o obale a závislosti.**

Pri $\mathbf z\in\langle X\rangle$ napísať jeho kombináciu. V ľubovoľnej kombinácii z $X,\mathbf z$ dosadiť za $\mathbf z$ a zoskupiť koeficienty pôvodných vektorov; opačná inklúzia má koeficient pri $\mathbf z$ nulový.
To je dôkaz rovnosti obalov z definície, vhodný aj keď test zakazuje odvolanie na všeobecnejšiu vetu [testy-la1-2.md:23; la1-P5-par3-krasensky 53:43–1:00:36].

V 2.39 zo závislosti zvoliť nenulový koeficient $\alpha_k$, preniesť ostatné členy a násobiť jeho inverziou. Opačne z vyjadrenia $\mathbf x_k$ odčítať $\mathbf x_k$: koeficient $-1\ne0$ dá netriviálnu nulovú kombináciu [chap-03.woo:1426; la1-P5-par2-klouda 42:26–51:50].
V 2.42 predpokladať nulovú kombináciu rozšíreného súboru. Nenulový nový koeficient by vyjadril nový vektor zo starých, spor; nulový koeficient vráti pôvodný LN súbor [chap-03.woo:1495; la1-P5-par2-klouda 55:10–1:02:34].

### 5. Značenie a terminológia

- České názvy: „vektorový prostor“, „podprostor“, „soubor vektorů“, „lineární kombinace“, „lineárně nezávislý/závislý“, „lineární obal“ [Definice 2.2, 2.7, 2.21, 2.23, 2.33].
- $T$ je teleso, $n,m\in\mathbb N$; príklady $\mathbb Q,\mathbb R,\mathbb C,\mathbb Z_p$. Teleso ani počet zložiek nie sú voľne zameniteľné uprostred výpočtu [Poznámka 2.3, chap-03.woo:228].
- $\mathbf x,\mathbf y$ sú vektory, $\alpha,\beta$ skaláry, $x_i,y_i$ zložky; nulový vektor je $\theta$, skalárna nula $0$ [Poznámka 2.6, chap-03.woo:311; la1-P4-par1-kleprlik 12:42–12:52,45:22–45:33].
- $P\ssubset T^n$ znamená „je podpriestorom“, $P\subseteq T^n$ iba „je podmnožinou“. Makro $\ssubset$ preberám zo zdroja; v cieľovom rendereri treba jeho podporu [Definice 2.7, chap-03.woo:361; la1-P4-par3-krasensky 37:09–37:29].
- $(\mathbf x_1,\ldots,\mathbf x_m)$ je súbor, nie množina: poradie a opakovania sú súčasťou zápisu; LN/LZ sa pri permutácii nemení [Poznámka 2.20, chap-03.woo:800; Pozorování 2.25, chap-03.woo:949].
- $\langle\mathbf x_1,\ldots,\mathbf x_m\rangle$ je množina, $\mathbf v\in\langle X\rangle$ je výrok o jej prvku [Definice 2.33, chap-03.woo:1194].
- LN, LZ, GEM, HST sú používané skratky [chap-03.woo:881,1052]. Krásenský navyše výslovne zavádza LO a skrátené slovo „obal“ [la1-P5-par3-krasensky 36:11].
- $\hat m=\{1,\ldots,m\}$; Kleprlík to pri kvantifikátore pripomína [la1-P4-par1-kleprlik 1:07:10–1:07:14].
- $\oplus,\odot$ sú dočasné symboly motivačnej časti, ďalej sa používajú $+,\cdot$ [chap-03.woo:48,188].
- Stĺpcový vektor a n-tica s čiarkami vyjadrujú ten istý vektor. Krásenský preferuje stĺpce pre prehľadnosť, skriptá často šetria miesto n-ticami [la1-P4-par3-krasensky 07:40–08:10].
- Kurz tu nedefinuje súčet skalára s vektorom ani súčin dvoch vektorov a varuje aj pred zápisom „vektor krát skalár“ [Varování 2.5, chap-03.woo:302]. Násobenie skalárom sa nesmie nazývať „skalární součin“ [discord-la1.md:420–426].
- V týchto definíciách sa obal značí špicatými zátvorkami; všeobecný zákaz označenia `span` zdroj nemá. Netvrdiť, že sa žiadne iné označenie nikde v kurze nevyskytuje.

### 6. Čo kto zdôrazňuje, chce a zakazuje

- Kleprlík, definícia podpriestoru: „Význam pište do testu.“ Samotný názov uzavretosti treba doplniť jeho významom pre ľubovoľné vstupy [la1-P4-par1-kleprlik 25:04–25:43].
- Kleprlík, forma definícií: „my to chceme, aby to mělo stejný význam, jako v těch skriptech.“ Netreba doslovné memorovanie jednej formulácie [la1-P4-par1-kleprlik 1:10:15].
- Kleprlík, GEM: „Ale mějte na paměti, proč. Napište to do toho testu, až to budete zdůvodňovat.“ Hneď ďalej spomína bod za vysvetlenie [la1-P5-par1-kleprlik 22:49–22:58].
- Klouda, prechod od definície k matici: „Tak byste tam měli vysvětlit, jak jste tu matici dostali.“ Výnimkou je, keď výslovne povedia, že netreba [la1-P4-par2-klouda 1:16:49–1:17:27].
- Krásenský, GEM: „To, že vezmu vektory, napíšu je do sloupců matice a gemuju, tak to není vůbec dostatečné zdůvodnění.“ [la1-P5-par3-krasensky 28:30].
- Krásenský, rozsah dopočítania: „tak skutečně stačí dojít do toho horního stupňovitého tvaru“. Platí pri otázke iba LN/LZ, potom sa rozhoduje podľa počtu riešení [la1-P5-par3-krasensky 27:49–28:00].
- Krásenský, rôzne objekty: „Nepleťte si, prosím, prázdnou množinu.“ Následne odlišuje nulový vektor a množinu obsahujúcu nulový vektor [la1-P4-par3-krasensky 38:08–39:19].
- Kleprlík, dôkaz verzus ilustrácia: „Důkaz s příkladem není dobrý, ale pro vás, pro vysvětlení to může být OK.“ Ide o univerzálne tvrdenie; protipríklad pri vyvrátení je naopak legitímny [la1-P5-par1-kleprlik 50:54; la1-P4-par1-kleprlik 42:27–42:45].
- Kleprlík, alternatívna charakterizácia LZ: „Není to definice, rozhodně ne.“ Ďalej ju neodporúča ako prvú cestu pri dokazovaní, lebo treba riešiť, ktorý vektor vyjadrujeme [la1-P5-par1-kleprlik 47:37–47:51,55:09–56:09].
- Klouda, tá istá charakterizácia: „nemusíte nic počítat, napíšete jenom,“ a ukáže viditeľnú kombináciu jedného vektora z ostatných. Je to dovolená skratka po dokázaní vety, pokiaľ zadanie nežiada definíciu [la1-P5-par2-klouda 53:45–54:06].
- Skriptá: „Metoda“ s výrokom „dám vektory do matice a zgemuji“ bez pochopenia je varovanie 2.31 [chap-03.woo:1158].
- Testový prepis má všeobecnú požiadavku vysvetľovať premenné a pôvod koeficientov, ale uvádza aj osobitnú výnimku pri Př. 1. Preniesť konkrétne pokyny zadania, nie bezvýhradne tvrdiť, že každá písemka vyžaduje rovnakú dĺžku komentára [testy-la1-2.md:3].

### 7. Typické úlohy

Početnosť je iba v lokálnom inventári `sources.json`, nie štatistika všetkých testov kurzu. Počítam podúlohy; pri uvedenom počte nezapočítavam prednáškové príklady ani otvorené kvízy.

| Typ | Krátky príklad zadania po česky | Odkaz | Ako často v podkladoch |
|---|---|---|---|
| Parameter a LN | „Zjistěte, pro které hodnoty $a\in\mathbb R$ je soubor ... lineárně nezávislý.“ | testy-la1-2.md:10,38; la1-marast-13433 | Dve explicitné testové LN zadania; inventár má 12 testových `la1.param-r` vrátane obalu a tém druhej časti. |
| Parameter a príslušnosť | „Pro které hodnoty $a\in\mathbb R$ patří vektor ... do podprostoru ...?“ | testy-la1-2.md:27,33,53,62,72 | Päť konkrétnych testových zadaní v prepise. |
| Definícia LN/LZ | „Napište definici lineárně nezávislého souboru.“ | testy-la1-2.md:12,23,29,35,50,63,69,74,81 | Deväť testových podúloh pod `la1.def-ln`; táto značka v inventári zahŕňa aj LZ. |
| Definícia a rozhodnutie o podpriestore | „Ověřte splnění podmínek z definice podprostoru.“ | testy-la1-2.md:6,40,50,81; la1-t2-2025-sova-3/d | Tri definície, jeden explicitný rozhodovací príklad a jeden vlastný protipríklad v testovom inventári. |
| Jednoduchý dôkaz LZ | „Jakmile soubor obsahuje nulový vektor, potom je lineárně závislý.“ | testy-la1-2.md:63; tiež :50,69,74 | Opakované varianty: nula, duplicita, pridanie vektora, symbolická kombinácia. |
| LZ a kombinácia ostatných | „Ukažte z definice, že ... je jeden vektor ... lineární kombinací ostatních.“ | testy-la1-2.md:12 | Jedna explicitná testová ekvivalencia pre trojicu. |
| Rovnosť obalov z definície | „$\mathbf z\in\langle\mathbf x,\mathbf y\rangle$ ... $\langle\mathbf x,\mathbf y\rangle=\langle\mathbf x,\mathbf y,\mathbf z\rangle$.“ | testy-la1-2.md:23 | Jedna explicitná testová dôkazová úloha; dve testové definície obalu. |
| Výpis obalu | „Napište všechny prvky z ... $\mathbb Z_2^4$.“ | testy-la1-2.md:35; Příklad 2.37, chap-03.woo:1352 | Jedna testová úloha, dva konečné prípady v skriptovom príklade. |
| LN/LZ číselne | „Rozhodněte, zda ... lineárně závislé či nezávislé.“ | la1-marast-13613/a,b; la1-marast-13430; la1-marast-13438 | #13613 má dva prípady; ďalšie cvičebnicové príklady. |
| LN/LZ kombinácií abstraktných LN vektorov | „Ověřte lineární závislost nebo nezávislost souboru ...“ | marast-problems/lecture-256.md:101–135, #13431, #13432 | Dva doložené typy: trojica kombinácií a cyklické súčty. |
| Príslušnosť a koeficienty kombinácie | „Pokud $\mathbf x\in\langle M\rangle$, najděte koeficienty ...“ | marast-problems/lecture-256.md:147–175, #13465 | Tri prípady nad $\mathbb R,\mathbb Z_2,\mathbb Z_5$. |
| Rovnica popisujúca obal | „Popište nějakým způsobem vektory, které do lineárního obalu patří.“ | la1-marast-13614; marast-problems/lecture-256.md:19–30 | Jeden hlavný proseminárny príklad; ďalší rozbor v #13489. |
| Homogénne riešenia sú podpriestor | „Dokažte, že množina řešení homogenní soustavy ... je podprostorem.“ | testy-la1-2.md:6,81; la1-marast-13691/b | Dve testové podúlohy v inventári a ukážková písemka MARAST. |
| Výpis netriviálnych podpriestorov | „Najděte všechny netriviální podprostory prostoru $\mathbb Z_3^2$.“ | marast.md:257–265, #13691 | Jedna ukážková úloha; blog 45 rozvíja všeobecné počítanie až s pojmami druhej časti. |

Skúšky používajú tieto zručnosti vložené do ďalších tém: napríklad dôkaz LN pomocou vlastných vektorov alebo prevod podpriestoru na sústavu [testy-la1-zkouska.md:10,15]. Neoznačujem ich za izolované úlohy tejto časti.
Pre rozstřel v `testy-rozstrel.md` som nenašiel samostatné relevantné zadanie s týmito výrazmi; nevyrábam mu početnosť.
Otvorené kvízy ku kapitole 2 existujú v teoretickej a početnej forme; ich znenia ani parafrázy tu nie sú [marast.md:11–45].

### 8. Časté chyby a otázky (Discord, prednášky)

- Obal verzus podpriestor: študent sa na rozdiel priamo pýta. Vyučujúci LA1 na Discorde predmetu odpovedá, že lineárny obal je nástroj, ako zo súboru vyrobiť množinu, kým podpriestor je vlastnosť množiny [discord-la1.md:408–418].
- Násobenie skalárom verzus skalárny súčin: vyučujúci na Discorde predmetu upozorňuje na ustálený odlišný význam druhého názvu [discord-la1.md:420–426].
- GEM s parametrom nad konečným telesom: študent rozumie číselnému GEM, ale nevie, kam postup smeruje pri parametri. Dostupný kontext neobsahuje odpoveď s riešením [discord-la1.md:543–549].
- $\theta\in P$ je iba nutná podmienka. Kríž dvoch priamok cez počiatok vyvracia domnelú postačujúcnosť [Příklad 2.12, chap-03.woo:481].
- $\emptyset$, $\theta$ a $\{\theta\}$ nie sú to isté; neprázdnosť nemožno vynechať [la1-P4-par3-krasensky 38:08–39:19].
- Triviálna kombinácia znamená nulové **koeficienty**, nie ľubovoľnú kombináciu s nulovým výsledkom; práve druhá možnosť odlišuje LZ [Definice 2.21 a 2.23, chap-03.woo:820,881].
- LZ neznamená, že každý vektor je násobkom iného; pri viac než dvoch vektoroch ide o kombináciu. Ani 2.39 nehovorí, že každý alebo konkrétne prvý vektor leží v obale ostatných [Pozorování 2.25, chap-03.woo:949; Varování 2.40, chap-03.woo:1472; la1-P5-par1-kleprlik 55:09–56:09].
- Dve rôzne kombinácie môžu dať ten istý vektor: počet voľieb koeficientov nemožno automaticky vydávať za počet prvkov obalu [Definice 2.23, 2.33; marast-problems/lecture-256.md:171–175].
- Zdrojový Discord má aj diskusiu o tom, čo GEM zachováva, vrátane tvrdenia z druhej ruky o štátnici. Nie je to dôkaz matematiky; pre túto časť používať odvodenie cez sústavu [discord-la1.md:60–72; Pozorování 2.30, chap-03.woo:1142].

### 9. Rozdiely medzi paralelkami

- Kleprlík začína šípkami už na konci P3. V P4 vysvetľuje podpriestor cez geometrické príklady a dôkaz prieniku; v P5 detailne zdôrazňuje komentár ku GEM a rozdiel medzi názvom uzavretosti a jej významom [la1-P3-par1-kleprlik 1:23:14–1:26:30; la1-P4-par1-kleprlik 24:48–25:46,42:07–47:56; la1-P5-par1-kleprlik 22:04–22:58].
- Klouda P3 druhú prezentáciu ešte nezačne. V P4 buduje intuíciu pridávaním bodov ku štvorčeku/kvadrantu, v P5 podrobne dokazuje 2.39 aj 2.42 a odporúča využiť rozpoznanú kombináciu ako skratku [la1-P3-par2-klouda 1:36:12; la1-P4-par2-klouda 30:36–38:18; la1-P5-par2-klouda 42:26–1:02:34].
- Krásenský preferuje stĺpcový zápis a v P4 dôkladne rozlišuje typy objektov. V P5 rozpisuje uzavretosť obalu a príklady nad konečnými telesami; 2.42 vysloví a dôkaz necháva do skrípt [la1-P4-par3-krasensky 07:40–08:10,38:08–39:19; la1-P5-par3-krasensky 40:14–47:14,1:11:35–1:16:06,1:29:17–1:30:01].
- Rozdiel v odporúčaní použiť 2.39 nie je rozdiel v matematike: Kleprlík preferuje pri dokazovaní pôvodnú definíciu, Klouda dovolí krátke rozpoznanie kombinácie. Obaja musia ustúpiť konkrétnemu zadaniu „priamo z definície“ [la1-P5-par1-kleprlik 47:37–47:51; la1-P5-par2-klouda 53:45–54:06; testy-la1-2.md:12].
- Pri LN/LZ cez sústavu používajú všetci traja stĺpce. Riadková cesta v neskoršej cvičebnicovej úlohe nie je odlišná požiadavka jednej paralelky [odkazy v časti 4; marast-problems/lecture-256.md:197–199].

### 10. Nejasné a chýbajúce

- Titulky proseminára 22/23-02 chýbajú. Písané postupy MARAST sú dostupné, ale nemožno z nich odvodiť, kto a v akom čase to povedal vo videu.
- Konce P3 u Kloudu a Krásenského neobsahujú samostatný výklad druhej prezentácie; `INDEX.md` ich témy označuje širšie. Uprednostnil som skutočný obsah titulkov pred odhadom z indexu [la1-P3-par2-klouda 1:36:12; la1-P3-par3-krasensky 1:33:45–1:36:02].
- `INDEX.md` označuje P5 Krásenského ako `partial`. Dodané titulky napriek tomu obsahujú celú tu požadovanú postupnosť až po 2.42 a prechádzajú do generovania/bázy; úplnosť pôvodného videa som neoveroval.
- Automatický prepis má chyby. Napríklad „nenulové“ v kontexte neprázdnej množiny je zjavne problematické slovo; význam kontrolujem Definíciou 2.7, nie prepisom samotným [la1-P4-par3-krasensky 31:45; chap-03.woo:361].
- V Příklade 2.29 je chybná poznámka s údajnými koeficientmi $\alpha=12,\beta=1,\gamma=4,\delta=2$: druhá rovnica dá $2\beta+\delta=4\ne0$. Nulový posledný riadok a záver LZ sú správne; konkrétny svedok je napríklad $(-8,-1,4,2)$, overený dosadením do všetkých štyroch zložiek [chap-03.woo:1113–1139]. Nepreberať chybný svedok do lekcie.
- Pri testoch uprednostniť opravy na riadku 6 pred starými poznámkami: medveď 2024 má oba dôkazy na tom istom variante a sova 2023 bodovanie 7/7/7. Starší prepis má neistotu ponechanú nižšie [testy-la1-2.md:6,29,38–40].
- Niektoré úlohy majú odpoveď, ale nemajú viditeľný postup, napríklad #13433; pri #13613/#13614 sa nemožno tváriť, že máme samostatný časovaný rozbor [marast-problems/lecture-256.md:16,30,144].
- Súvislý prednáškový postup vetvenia podľa parametra v požadovaných úsekoch zdroj nemá overený; sú dostupné konkrétne testové zadania a študentská otázka. Pravidlo nedelenia nulou je tu označené ako matematické odvodenie, nie požiadavka pripísaná učiteľovi.
- Inventár nie je bezchybná terminologická autorita: napríklad `la1.def-ln` zahŕňa aj znenia LZ. Početnosti vyššie sú početnosti záznamov, nie pravdepodobnosť témy na skúške.
- Rozstřel nemá v prečítanom súbore doložený samostatný typ pre túto časť. Generalizované počítanie podpriestorov z blogu 45 potrebuje bázu a dimenziu, preto ostáva mimo výkladu prvej časti.

### 11. Surové zdroje

Základ `K = kontext/`, `L = zdroje/`.

- `K/BRIEF.md`: pravidlá, citačný formát a osnova.
- `L/src/bi-la1-master/src/textbook/chap-03.woo:1–1537`: celá určená prvá časť; nosné bloky 188–324, 361–449, 454–718, 718–780, 800–1009, 1052–1183, 1194–1531.
- `K/cisla/la1-kap2.tsv`: položky 2.1–2.42; ďalšie položky len na určenie hranice.
- `K/skripta/bi-la1-master/src/slides/bi-la1-prezentace-02.tex:27–543`: motivácia až zväčšovanie a zmenšovanie súborov.
- `K/INDEX.md`: identifikácia paraleliek, chýbajúce titulky proseminára, stav P5 par3.
- `K/txt/la1-P3-par1-kleprlik.txt`: 1:23:14–1:33:26, skoršie motivácie homogénnymi sústavami oddelené od vlastnej kapitoly.
- `K/txt/la1-P3-par2-klouda.txt`: 43:06–43:17, 1:34:50–1:36:29; koniec predchádzajúcej kapitoly.
- `K/txt/la1-P3-par3-krasensky.txt`: 46:21, 1:33:45–1:36:02; koniec predchádzajúcej kapitoly a podmienka telesa pri GEM.
- `K/txt/la1-P4-par1-kleprlik.txt`: 09:39–1:35:59; zvlášť 19:04–25:46, 42:07–47:56, 1:05:39–1:10:58.
- `K/txt/la1-P4-par2-klouda.txt`: 08:04–1:37:01; zvlášť 17:47–19:57, 29:20–38:18, 1:11:21–1:18:47.
- `K/txt/la1-P4-par3-krasensky.txt`: 07:40–1:37:49; zvlášť 31:19–43:00, 47:48 a záver LN/LZ.
- `K/txt/la1-P5-par1-kleprlik.txt`: 13:13–1:02:25; zvlášť 19:32–22:58, 31:31–33:39, 45:29–1:02:25.
- `K/txt/la1-P5-par2-klouda.txt`: 07:37–1:02:44; zvlášť 10:49–24:14, 42:26–1:02:34.
- `K/txt/la1-P5-par3-krasensky.txt`: 06:59–1:30:01; zvlášť 27:12–31:10, 36:11–1:16:06, 1:17:52–1:30:01.
- `L/testy-la1-2.md:3–82`: hlavička, opravy prepisu, početné a dôkazové úlohy.
- `L/testy-la1-zkouska.md:10,15`: iba posúdenie nadväzností na skúšku.
- `L/testy-rozstrel.md`: kontrola, bez samostatného relevantného nálezu pre túto časť.
- `L/marast-cvicebnice.md:53–96`: identifikácia #13613/#13614 a dostupnosti riešení; dobové pokyny k ukážkovým písemkám.
- `L/marast-problems/lecture-256.md:5–175,189–220,238–268`: LN/LZ, obal, parameter, postupy; riadkový GEM iba ako upozornenie na neskoršiu kapitolu.
- `L/marast-problems/lecture-292.md`: #13691; porovnané s `L/marast.md:257–265`.
- `L/marast.md:11–45,257–265`: existencia kvízov a verejný ukážkový príklad; bez prenosu znení otvorených kvízov.
- `L/marast-blog-45.md:14–65`: vzťah výberu LN vektorov a ich obalov; obsah vyžadujúci dimenziu ponechaný druhej časti.
- `L/discord-la1.md:60–72,408–426,543–549`: GEM a význam zachovania, obal/podpriestor, terminológia, parameter.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`: iba LA1, uvedené ID a zručnosti; otvorené kvízy vynechané z obsahu aj početností testov.

Kontrola citácií: po zápise náhodne skontrolované časové odkazy a odkazy do skrípt proti lokálnym súborom; audio/video nebolo osobitne prehrávané.

## Časť 2: báza, dimenzia, súradnice (chap-03.woo:1538–2636)

### 1. Rozsah a zdroje

Stav overenia: 17. 11. 2026, výlučne dodané lokálne zdroje. Číslovanie výhradne z `cisla/la1-kap2.tsv`.
Skriptá: `chap-03.woo:1538–2634`, od „Báze podprostoru“ po koniec „Souřadnice vzhledem k bázi“.
Sekcie: Báze podprostoru, Dimenze podprostoru, Souvislost báze a dimenze, Hledání báze podprostoru, Vlastnosti dimenze, Souřadnice vzhledem k bázi.
Prvá položka je Definice 2.43 Generování podprostoru; posledná Příklad 2.72, mapovanie obrázka cez súradnice.
Definice 2.73 Obecný vektorový prostor už do rozsahu nepatrí. Predpoklady 2.35, 2.39, 2.41 a 2.42 pripomínam len tam, kde zdôvodňujú postup.
Slajdy: `bi-la1-prezentace-02.tex:544–900`, rovnaké jadro, bez podrobného rozpisu výpočtov a dôkazov zo skrípt.

- Kleprlík: `la1-P5-par1-kleprlik 1:03:01–1:36:39`, generovanie, báza, štandardná báza, dimenzia a formulácia 2.55; `la1-P6-par1-kleprlik 14:53–1:00:33`, pokračovanie až po súradnice.
- Klouda: `la1-P5-par2-klouda 1:03:04–1:36:38`, generovanie, báza, výpočet nad $\mathbb Z_5$, dimenzia, predbežné súradnice; `la1-P6-par2-klouda 13:13–55:01`, dôkazy, vyberanie a doplnenie bázy, jednoznačnosť súradníc.
- Krásenský: `la1-P5-par3-krasensky 1:30:55–1:36:04`, generovanie a báza; `la1-P6-par3-krasensky 06:09–1:35:36`, najprv súradnice, potom návrat k dimenzii a konštrukcii bázy.
- P5 Krásenského má v INDEXe stav `partial`; uvedené časy sú doložené, úplnosť celej nahrávky nemožno predpokladať.
- Proseminár `la1-prosem2223-02`: titulky chýbajú, INDEX uvádza 0 cues. Jeho presný úsek, hovoriaceho ani doslovné pokyny nemožno z tohto zdroja doložiť.

Jadro bázy, dimenzie a súradníc sa do konca P6 objaví aspoň v jednej paralelke.
V dodaných P5/P6 som nenašiel odvodenie všeobecného vzorca počtu báz a podpriestorov nad $\mathbb Z_p$; preto ho čerpám zo skrípt a blogu, nie z úst prednášajúcich.
Počet prvkov konečného podpriestoru však Krásenský vysvetľuje explicitne [la1-P6-par3-krasensky 11:22–13:06].

### 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. Obal vyrába zo súboru podpriestor; teraz chceme opačne podpriestor opísať vhodným súborom. Báza má generovať všetko a neobsahovať nadbytočnosť, formálne Definície 2.43 a 2.44 [la1-P6-par1-kleprlik 14:53–16:23].

2. Dve vlastnosti bázy sa dajú kontrolovať tou istou sústavou: všeobecná pravá strana overí generovanie, nulová LN. Klouda to počíta nad $\mathbb Z_5$ a z hlavných stĺpcov vyčíta oboje, čo zodpovedá Postupu 2.45 a Příkladu 2.46 [la1-P5-par2-klouda 1:08:01–1:18:47].

3. Štandardná báza $\mathcal E_n$ umožňuje zvoliť koeficienty priamo zo zložiek vektora. Krásenský na konci P5 overuje jej LN aj generovanie priamo, teda ešte bez neskoršej vety o dimenzii [la1-P5-par3-krasensky 1:34:42–1:36:04; Definice 2.48, chap-03.woo:1723].

4. Dimenzia má formalizovať predstavu priamky a roviny, ale kurz ju definuje cez LN súbory, nie cez počet prvkov bázy. Krásenský vysvetľuje, že alternatíva by vyžadovala najprv existenciu báz a rovnosť ich dĺžok [la1-P6-par3-krasensky 40:12–44:51; Definice 2.49, chap-03.woo:1758].

5. LN súbor dá dolný odhad dimenzie, generujúci horný. Klouda dokazuje horný odhad 2.54 pomocou GEM a sporu s existenciou $m+1$ LN vektorov [la1-P6-par2-klouda 15:54–25:01].

6. Pri vopred známej dimenzii $d$ stačí pre $d$ vektorov z $P$ overiť jednu vlastnosť bázy. Věta 2.55 šetrí kontrolu generovania, ale jej predpoklady nemožno zamlčať [la1-P6-par1-kleprlik 18:17–27:32; la1-P6-par3-krasensky 1:10:52–1:12:26].

7. Z definície dimenzie a 2.55 dostávame existenciu bázy; z dvoch odhadov rovnosť dimenzie a dĺžky bázy. Preto majú všetky bázy rovnakú dĺžku, Věty 2.56, 2.57 a Důsledek 2.58 [la1-P6-par1-kleprlik 28:02–32:40].

8. Bázu získame buď odoberaním redundantných generátorov, alebo pridávaním vektorov mimo doterajšieho obalu. Klouda k 2.60 pridáva dve početné stratégie: pripojiť celú bázu a vybrať hlavné stĺpce, alebo doplnenie tipnúť a GEM ho overiť [la1-P6-par2-klouda 38:34–44:57].

9. Inklúzia podpriestorov obmedzuje dimenziu; pri rovnakej dimenzii a inklúzii dostaneme rovnosť. Samotná rovnosť dimenzií dvoch obalov nestačí, treba aj dimenziu spoločného obalu, 2.62 až 2.65 [la1-P6-par1-kleprlik 36:06–43:11].

10. Báza dá každému vektoru jediné koeficienty: generovanie zaručí existenciu, LN jednoznačnosť. To sú súradnice podľa 2.66 a 2.68, užitočné na orientáciu v podpriestore aj mapovanie textúry [la1-P6-par1-kleprlik 44:20–47:40, 50:56–58:06].

Krásenský tento posledný krok zámerne presúva na začiatok P6, aby najprv ukázal účel báz; až potom vysvetľuje dimenziu [la1-P6-par3-krasensky 10:01–10:38, 28:47–29:19].

### 3. Pojmy a tvrdenia

V stĺpci dôkaz rozlišujem dôkaz, overenie definície a iba formuláciu; „neoverené“ neznamená, že sa nikde nedokazuje.
Dôležitosť sa týka doloženého korpusu úloh, nie odhadu budúceho testu.

| Číslo | Druh | Názov | Jadro | Dôkaz alebo výklad | Dôležitosť pre test |
|---|---|---|---|---|---|
| 2.43 | Definice | Generování podprostoru | Súbor generuje $P$, ak sa jeho obal rovná $P$. | Definícia, samostatný dôkaz sa nežiada; Klouda výklad [la1-P5-par2-klouda 1:03:09–1:03:40]. Skriptá :1549. | Predpoklad všetkých úloh na bázu [testy-la1-2.md:28]. |
| 2.44 | Definice | Báze | Báza $P$ je LN súbor generujúci $P$. | Definícia; Krásenský obe vlastnosti [la1-P5-par3-krasensky 1:31:57–1:33:08]. Skriptá :1566. | Priamo definovať [testy-la1-2.md:29,45]. |
| 2.45 | Postup | Ověření zda soubor generuje podprostor | Pre ľubovoľný $v\in P$ musí byť sústava koeficientov riešiteľná. | Skriptá kroky áno :1599; Klouda realizácia [la1-P5-par2-klouda 1:11:07–1:16:55]. | MARAST #13489; parameter v teste [testy-la1-2.md:78]. |
| 2.48 | Definice | Standardní/kanonická báze $T^n$ | $\mathcal E_n=(e_1,\ldots,e_n)$ s jednou jednotkou v každom vektore. | Skriptá definícia :1723; overenie oboch vlastností Krásenský [la1-P5-par3-krasensky 1:35:23–1:36:04]. | Pomôcka pre doplnenie [testy-la1-2.md:11]. |
| 2.49 | Definice | Dimenze podprostoru | $d$ je dĺžka existujúceho LN súboru, pričom každý súbor dĺžky $d+1$ je LZ; osobitne $d=0$. | Skriptá :1758; korektnosť rieši 2.50; Krásenský [la1-P6-par3-krasensky 44:44–54:33]. | Definícia výslovne [testy-la1-2.md:40,55]. |
| 2.50 | Poznámka | O korektnosti definice dimenze | Také číslo existuje a je jediné. | Skriptá dôkaz áno :1781, s pomocou Lemmy 2.51; Krásenský vysvetľuje existenciu a jednoznačnosť [la1-P6-par3-krasensky 50:06–54:33]. | Podklad správnej definície [testy-la1-2.md:40]. |
| 2.52 | Pozorování | Dimenze $T^n$ | $\dim T^n=n$. | Skriptá zdôvodnenie áno :1838; Krásenský LN štandardnej bázy a LZ dlhších súborov [la1-P6-par3-krasensky 56:40–1:02:31]. | Umožňuje 2.55 v celom priestore [testy-la1-2.md:16]. |
| 2.53 | Věta | O jednoduchých vlastnostech dimenze | Nulová dimenzia charakterizuje $\{\theta\}$; LN súbor dá dolný a univerzálna LZ horný odhad. | Skriptá dôkaz áno :1865; Kleprlík 0 a dolný odhad [la1-P5-par1-kleprlik 1:25:18–1:31:38]. | Dôkazy dimenzie a bázy [testy-la1-2.md:40,63]. |
| 2.54 | Tvrzení | O dimenzi a délce generujícího souboru | $m$ generátorov dá $\dim P\leq m$. | Skriptá dôkaz áno :1934; Klouda GEM a spor [la1-P6-par2-klouda 15:54–25:01]. | Pomocný výsledok pri dôkaze 2.55 [testy-la1-2.md:29,63]. |
| 2.55 | Věta | O souboru délky odpovídající dimenzi | Pre $d$ vektorov z $P$, $\dim P=d>0$, sú ekvivalentné báza, LN a generovanie. | Skriptá dôkaz áno :2001; Kleprlík [la1-P6-par1-kleprlik 19:30–27:25], Klouda [la1-P6-par2-klouda 26:17–36:06], Krásenský [la1-P6-par3-krasensky 1:12:26–1:22:00]. | Výpočet bázy aj samostatný dôkaz špeciálneho prípadu [testy-la1-2.md:40,63,74]. |
| 2.56 | Věta | O existenci báze | Každý podpriestor kladnej dimenzie $d$ má $d$-člennú bázu. | Skriptá dôkaz áno :2051; Kleprlík [la1-P6-par1-kleprlik 28:02–29:27], Krásenský [la1-P6-par3-krasensky 1:24:24–1:25:39]. | Základ konštrukcií [testy-la1-2.md:28]. |
| 2.57 | Věta | O vztahu dimenze a délky báze | Existencia $d$-člennej bázy implikuje $\dim P=d$. | Skriptá dôkaz áno :2068; Kleprlík dva odhady [la1-P6-par1-kleprlik 29:37–31:29]. | Záver úlohy „báze a dimenze“ [testy-la1-2.md:34,39]. |
| 2.58 | Důsledek | O délce bází | Všetky bázy toho istého $P$ majú dĺžku $\dim P$. | Skriptá dôkaz áno :2087; Kleprlík [la1-P6-par1-kleprlik 31:37–32:40]. | Druhá báza toho istého priestoru [testy-la1-2.md:28,34]. |
| 2.59 | Věta | O vybrání báze | Z generátorov nenulového podpriestoru možno vybrať bázu. | Skriptá dôkaz áno :2113; Kleprlík iterované odoberanie [la1-P6-par1-kleprlik 32:40–34:27]. | Nosný typ úlohy [testy-la1-2.md:28,34,39; la1-marast-13616]. |
| 2.60 | Věta | O doplnění na bázi | LN súbor v $P$ možno doplniť vektormi z $P$ na jeho bázu. | Skriptá dôkaz áno :2134; Krásenský iterácia a konečnosť [la1-P6-par3-krasensky 1:32:05–1:33:34]; Klouda praktické varianty [la1-P6-par2-klouda 42:29–44:57]. | 7 podotázok doplnenia v inventári druhých testov; napr. [la1-t2-2025-sova-2/a]. |
| 2.62 | Věta | O vztahu dimenzí zanořených podprostorů | $P\subseteq Q$ dá $\dim P\leq\dim Q$, pri vlastnej inklúzii ostro. | Skriptá dôkaz áno :2180; Kleprlík geometrický výklad, nie celý dôkaz [la1-P6-par1-kleprlik 36:06–38:07]. | Podklad porovnávania obalov [la1-marast-13487]. |
| 2.63 | Důsledek | O podprostoru stejné dimenze | Inklúzia a rovnaká dimenzia implikujú rovnosť. | Skriptá samostatný dôkaz nie :2227, priamy dôsledok 2.62; Kleprlík [la1-P6-par1-kleprlik 38:18–39:08]. | Zdôvodnenie druhej bázy [testy-la1-2.md:28]. |
| 2.64 | Důsledek | O vektoru v lineárním obalu a dimenzích lineárních obalů | $z\in\langle X\rangle$ práve vtedy, keď pridanie $z$ nezvýši dimenziu. | Skriptá dôkaz áno :2241; Kleprlík aplikácia po zavedení hodnosti [la1-P6-par1-kleprlik 1:26:29–1:26:49]. | Alternatíva overenia členstva [testy-la1-2.md:27]. |
| 2.65 | Pozorování | O rovnosti lineárních obalů | Rovnosť dvoch obalov je ekvivalentná rovnosti ich dimenzií aj dimenzie spoločného obalu. | Skriptá dôkaz áno :2262; Kleprlík význam podmienok [la1-P6-par1-kleprlik 41:06–43:11]. | MARAST #13487 a úlohy na inú bázu [testy-la1-2.md:34]. |
| 2.66 | Věta | O jednoznačnosti koeficientů | Každý vektor podpriestoru má vzhľadom na pevnú bázu práve jednu kombináciu. | Skriptá dôkaz áno :2320; všetci: Kleprlík [la1-P6-par1-kleprlik 51:40–56:35], Klouda [la1-P6-par2-klouda 48:00–54:09], Krásenský [la1-P6-par3-krasensky 13:06–17:46]. | Priamo dôkaz [testy-la1-2.md:45,78]. |
| 2.67 | Poznámka | O počtu prvků podprostoru | Podpriestor dimenzie $d$ nad $\mathbb Z_p$ má $p^d$ prvkov. | Skriptá :2356, zdôvodnenie bijekciou pred poznámkou; Krásenský [la1-P6-par3-krasensky 11:22–13:06]. | Počet prvkov aj dôkaz [testy-la1-2.md:34,45,68]. |
| 2.68 | Definice | Souřadnice vzhledem k bázi | $(v)_{\mathcal B}$ je usporiadaná $d$-tica koeficientov v poradí bázy. | Definícia, korektnosť cez 2.66; skriptá :2373; Krásenský [la1-P6-par3-krasensky 18:11–23:42]. | Definícia, dĺžka a oba smery prevodu [testy-la1-2.md:12,22,45]. |
| 2.69 | Postup | Nalezení souřadnic vektoru v bázi | Riešime sústavu $\sum\alpha_i x_i=v$ pomocou GEM. | Skriptá kroky áno :2464; Kleprlík [la1-P6-par1-kleprlik 58:01–58:33]. | Súradnice sú opakovaná súčasť druhého testu [testy-la1-2.md:11,22,49]. |

Odkazy `Skriptá :riadok` v tabuľke znamenajú `chap-03.woo:riadok`. Definície nemajú dôkaz vlastného znenia.
Konvencia kurzu je výslovná: $\{\theta\}$ nemá bázu, pretože sa tu nepripúšťa prázdny súbor; dimenziu má 0 [Věta 2.53, chap-03.woo:1865; la1-P5-par1-kleprlik 1:28:58].

### 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

#### Overenie, či súbor generuje $P$, prípadne tvorí bázu

1. Overiť, že zadané vektory sú v $P$; pravá strana je všeobecný vektor $v\in P$.
2. Napísať rovnicu $\alpha_1x_1+\cdots+\alpha_mx_m=v$; neznáme sú koeficienty, nie zložky bázových vektorov.
3. Zostaviť rozšírenú maticu $(x_1\ \cdots\ x_m\mid v)$, vektory teda do stĺpcov.
4. GEM do HST; generovanie znamená riešiteľnosť pre každú dovolenú pravú stranu.
5. Na LN použiť tú istú ľavú maticu s nulovou pravou stranou; jediným riešením musia byť nulové koeficienty.

Takto je to v Postupe 2.45 a v nadväzujúcom komentári, Klouda robí obe kontroly naraz [chap-03.woo:1599–1648; la1-P5-par2-klouda 1:09:04–1:18:47; bi-la1-prezentace-02.tex:574].
Pri známej $\dim P=d$ a presne $d$ vektoroch z $P$ stačí LN, Věta 2.55; nevyrábať dimenziu z ešte neoverenej bázy [la1-P6-par3-krasensky 1:10:52–1:11:52].

#### Báza obalu: pôvodné vektory do stĺpcov, vybrať hlavné stĺpce

1. Označiť $P=\langle y_1,\ldots,y_m\rangle$ a písať rovnicu pre kombináciu, aby bolo jasné, čo matica opisuje.
2. Zostaviť $A=(y_1\ \cdots\ y_m)$, urobiť riadkový GEM do HST a zaznamenať indexy hlavných stĺpcov.
3. Ako bázu vypísať príslušné **pôvodné** vektory $y_{i_1},\ldots,y_{i_d}$, nie stĺpce zmenenej matice po GEM.
4. Zdôvodniť LN vybraného súboru a zachovanie obalu: vedľajšie stĺpce sú kombináciami predchádzajúcich hlavných, a preto ich možno odobrať.
5. Napísať obidve vlastnosti bázy a až potom $\dim P=d$ podľa Věty 2.57.

Opora: proseminár 3 pripomína tento postup z 2. proseminára (vyhodiť vektor LZ súboru bez zmeny obalu) [la1-prosem2223-03 00:52–01:21]; Klouda vyberá hlavné stĺpce pri doplnení na bázu [la1-P6-par2-klouda 43:29–43:50]; vysvetlenie odoberania [Věta 2.59, chap-03.woo:2113; Důsledek 2.41, chap-03.woo:1482].
Prečo brať pôvodné vektory: GEM tu zachováva riešenia sústav koeficientov, teda vzťahy medzi označenými stĺpcami; všeobecne nezachováva ich konkrétny obal. Toto je matematické vysvetlenie postupu, nie doslovný citát prednášky.
MARAST #13489 explicitne vyjadrí $(1,0,4)=2(1,2,3)+3(3,2,1)$ nad $\mathbb Z_5$, odoberie tretí vektor a pre zvyšné dva overí LN [marast-problems/lecture-256.md, #13489].
Pri samých nulových generátoroch je výsledkom $P=\{\theta\}$ a $\dim P=0$, podľa miestnej konvencie bez bázy [la1-P6-par3-krasensky 37:42–38:01; Věta 2.53, chap-03.woo:1865].

#### Doplnenie na bázu, spôsob 1: pripojiť celú známu bázu

1. Vstupný súbor $(x_1,\ldots,x_m)$ musí byť LN a musí ležať v cieľovom $P$.
2. Zaň do stĺpcov pripojiť známu bázu $(b_1,\ldots,b_d)$ cieľového $P$; pre $P=T^n$ štandardnú $\mathcal E_n$.
3. Urobiť GEM matice $(x_1\ \cdots\ x_m\mid b_1\ \cdots\ b_d)$ do HST.
4. Vybrať pôvodné vektory na pozíciách hlavných stĺpcov; prvých $m$ ostáva, lebo vstup bol LN.
5. Výsledok generuje $P$ a je LN; pridaných bude $d-m$ vektorov. Uviesť argument, nie iba zoznam výsledných vektorov.

Tento postup učí explicitne **Klouda** [la1-P6-par2-klouda 43:02–43:50]. Čiara tu oddeľuje dva súbory generátorov, nie pravú stranu jednej sústavy.
Existenčná opora: Věta 2.60, výber z rozšírených generátorov 2.59; koniec pri dĺžke $d$ z 2.55 [chap-03.woo:2001,2113,2134].
Pre vlastný podpriestor nemožno automaticky pripojiť ľubovoľné štandardné vektory okolitého $T^n$: museli by patriť do $P$ [Věta 2.60, chap-03.woo:2134].

#### Doplnenie na bázu, spôsob 2: tipnúť a overiť

1. Doplniť pôvodný súbor o $d-m$ kandidátov z $P$.
2. Všetky vektory dať do stĺpcov a GEM overiť LN; pri dĺžke $d=\dim P$ potom 2.55 zaručí bázu.
3. Ak kandidáti LN nezachovali, zvoliť iné; samotný tip bez overenia nie je riešenie.

Aj tento spôsob učí **Klouda**, vrátane výslovného uznania tipu následne dokázaného GEM [la1-P6-par2-klouda 43:59–44:57].
Jeho poznámku o pravdepodobnosti nad $\mathbb R$ nezovšeobecňovať na konečné telesá; výpočet musí LN skutočne preukázať.
Kleprlík a Krásenský v P6 vysvetľujú princíp pridávať vektor mimo obalu, ale oba Kloudove praktické varianty v týchto úsekoch explicitne nepredvádzajú [la1-P6-par1-kleprlik 34:34–35:57; la1-P6-par3-krasensky 1:32:05–1:33:34].
V skriptách je iteratívny dôkaz, nie plný maticový recept; Poznámka 2.61 odkazuje zrýchlenie jedným výpočtom na proseminár a cvičenie [chap-03.woo:2134–2160].

#### Súradnice vektora: GEM rozšírenej matice

1. Zachovať presné poradie $\mathcal B=(x_1,\ldots,x_d)$ a napísať $\sum_i\alpha_ix_i=v$.
2. Sústava má rozšírenú maticu $(x_1\ \cdots\ x_d\mid v)$, teda bázové vektory do stĺpcov, daný vektor doprava.
3. GEM do HST; dopočítať jediné riešenie pre koeficienty a zapísať $(v)_{\mathcal B}=(\alpha_1,\ldots,\alpha_d)$.
4. Ak sa tvrdí, že $v\in P$ a $\mathcal B$ je báza, súradnice existujú a sú jediné podľa 2.66.
5. Ak pri platnej báze vlastného podpriestoru vznikne spor, $v\notin P$; viac riešení signalizuje, že súbor nie je LN, alebo chybu výpočtu.

Takto Postup 2.69, slajdy a Kleprlík [chap-03.woo:2464; bi-la1-prezentace-02.tex:857; la1-P6-par1-kleprlik 58:01–1:00:25].
Krásenský osobitne upozorňuje na rozpor s existenciou či jednoznačnosťou [la1-P6-par3-krasensky 26:32–27:26].
**GEM áno; zákaz dosadzovania zdroj nemá.** Skriptá výslovne povoľujú skončiť v HST, príklad potom pokračuje až na jednotkovú ľavú maticu, lebo je to ľahké [Příklad 2.71, chap-03.woo:2510–2546].
Teda odporúčaný výklad má ukázať rozšírenú maticu a GEM; nemožno tvrdiť, že spätné dopočítanie z HST je zakázané.
Overený vzor: pre $\mathcal B=((1,1,1),(1,1,2),(1,2,3))$ je $(a,b,c)_{\mathcal B}=(a+b-c,a-2b+c,b-a)$ [Příklad 2.71, chap-03.woo:2510].

#### Opačný smer: zo súradníc vektor

Ak sú dané súradnice $(c_1,\ldots,c_d)$, počítať priamo $v=c_1x_1+\cdots+c_dx_d$; nejde o hľadanie neznámych koeficientov.
Počet súradníc musí byť $d$, aj keď pôvodný vektor má $n$ zložiek; preto časť ponúknutých rovností nemusí dávať zmysel [Definice 2.68, chap-03.woo:2373; la1-P6-par3-krasensky 18:11–18:41; testy-la1-2.md:22,39,49].

#### Zdôvodnenie dimenzie a inej bázy

Najprv preukázať, že nájdený súbor je báza $P$, potom spočítať jeho prvky a použiť 2.57.
Teoreticky dôkaz 2.57 spojí LN, teda $\dim P\geq d$ podľa 2.53, a generovanie, teda $\dim P\leq d$ podľa 2.54 [la1-P6-par1-kleprlik 30:26–31:15; chap-03.woo:2068].
Pri požiadavke inej bázy doložiť jej príslušnosť do $P$ a LN správneho počtu vektorov; alebo priamo LN a rovnosť obalov.
Rovnosť dimenzií sama o sebe nestačí; použiť inklúziu s 2.63 alebo tri dimenzie z 2.65 [la1-P6-par1-kleprlik 41:56–42:41; testy-la1-2.md:28,34].

#### Riadkový postup: prípustná alternatíva s iným zdôvodnením

Vektory možno vložiť do riadkov, GEM zachová ich obal a nenulové **výsledné riadky** HST tvoria bázu; to je odlišné od výberu pôvodných hlavných stĺpcov.
Tento variant Kleprlík vysvetľuje až pri hodnosti a explicitne pripúšťa v písomke pri správnom komentári [la1-P6-par1-kleprlik 1:25:10–1:27:59].
MARAST #13486 ho tiež používa a upozorňuje na potrebu neskoršej teórie; jeho historické označenie „kapitola 4“ nepreberať ako číslo tejto kapitoly [marast-problems/lecture-256.md, #13486].
Pri dopĺňaní musí výsledná báza zachovať vstupné vektory; obyčajné nahradenie celej bázy zmenenými riadkami túto požiadavku samo nezaručuje [Věta 2.60, chap-03.woo:2134].

#### Počítanie nad $\mathbb Z_p$: prvky, bázy, podpriestory

- Podpriestor $P$ dimenzie $d$ má $p^d$ prvkov: každá z $p^d$ usporiadaných $d$-tíc koeficientov dá iný vektor a všetky vektory tak vzniknú [Věta 2.66 a Poznámka 2.67, chap-03.woo:2320,2356].
- Počet LN súborov dĺžky $k\leq n$ v $\mathbb Z_p^n$ je $\prod_{i=0}^{k-1}(p^n-p^i)$. Pri $i$ už vybraných LN vektoroch sa vylučuje celý ich $p^i$-prvkový obal [marast-blog-45.md:41–50].
- Počet báz pevného $d$-rozmerného podpriestoru je $\prod_{i=0}^{d-1}(p^d-p^i)$; okolité $n$ ho nemení [marast-blog-45.md:52–56].
- Báza je usporiadaný súbor: pri tomto počte sa **nedelí $d!$**. Poradie je súčasťou bázy a súradníc [Definice 2.44 a 2.68, chap-03.woo:1566,2373; la1-P6-par3-krasensky 38:24–39:56].
- Počet $k$-rozmerných podpriestorov v $\mathbb Z_p^n$ je $\dfrac{\prod_{i=0}^{k-1}(p^n-p^i)}{\prod_{i=0}^{k-1}(p^k-p^i)}$: každý podpriestor bol započítaný toľkokrát, koľko má vlastných báz [marast-blog-45.md:34–60; chap-03.woo:2563].
- Krajné dimenzie riešiť osobitne: práve jeden nulový a práve jeden celý podpriestor; nevkladať sem bez komentára konvenciu prázdnej bázy [marast-blog-45.md:18; Věta 2.53, chap-03.woo:1865].

Kontrolné hodnoty zo zdrojových príkladov: bázy $\mathbb Z_5^3$: $(125-1)(125-5)(125-25)=1\,488\,000$; bázy pevného dvojrozmerného $P$ nad $\mathbb Z_5$: $(25-1)(25-5)=480$.
Dvojrozmerné podpriestory $\mathbb Z_5^3$: $\frac{(125-1)(125-5)}{(25-1)(25-5)}=31$ [chap-03.woo:2163,2563; testy-la1-2.md:69].
V $\mathbb Z_3^2$ sú štyri jednorozmerné podpriestory, spolu s nulovým a celým šesť; každý nenulový vektor sa delí o priamku so svojím dvojnásobkom [marast-problems/lecture-262.md, #14159].
Pri rozšírení LN dvojice o nenulový vektor, ktoré má byť LZ, vyberať z jej obalu bez nuly: $p^2-1$ možností [Věta 2.42, chap-03.woo:1495; Poznámka 2.67, chap-03.woo:2356; testy-la1-2.md:11].

### 5. Značenie a terminológia

- České názvy: „generování podprostoru“, „báze“, „standardní/kanonická báze“, „dimenze podprostoru“, „souřadnice vzhledem k bázi“ [Definície 2.43, 2.44, 2.48, 2.49, 2.68].
- $\mathcal B=(x_1,\ldots,x_d)$ je usporiadaný súbor; $P=\langle x_1,\ldots,x_d\rangle$ je množina, podpriestor. Obal nezávisí od poradia, súradnice áno [la1-P6-par3-krasensky 38:24–39:56].
- Skriptá píšu vektory tučne pomocou makier `\vx`, `\vv`, štandardnú bázu `\EE_n`, všeobecnú `\BB`; vo výťahu ich zapisujem ako $x,v,\mathcal E_n,\mathcal B$ [chap-03.woo:1723,2373].
- $\theta$ je nulový vektor, $0$ skalár alebo hodnota dimenzie; $\dim P$ je celé nezáporné číslo [chap-03.woo:1758,1865].
- $(v)_{\mathcal B}\in T^d$ sú súradnice, zatiaľ čo $v\in P\subseteq T^n$; skriptá pripúšťajú aj stĺpcový zápis súradníc [Definice 2.68, chap-03.woo:2373].
- LN, LZ; GEM, HST; „hlavní/vedlejší sloupec“. Názov „pivot“ v tomto výťahu nenahrádza kurzový názov [la1-P5-par2-klouda 1:10:30–1:10:37, 1:16:16–1:17:51].
- Medzi upravenými maticami Klouda píše $\sim$, nie $=$, lebo nejde o rovnaké matice [la1-P5-par2-klouda 1:13:46–1:13:59].
- Krásenský výslovne prijíma vektor ako riadkovú n-ticu s čiarkami aj ako stĺpec [la1-P6-par3-krasensky 22:42–23:12]. To neznamená ľubovoľne meniť orientáciu vo výpočte sústavy.
- Kleprlík si bázu v P6 označí kaligrafickým $X$, Klouda a Krásenský hovoria o kaligrafickom $B$; konkrétne písmeno nie je povinné, musí byť konzistentné [la1-P6-par1-kleprlik 49:10; la1-P5-par2-klouda 1:35:30–1:35:37; la1-P6-par3-krasensky 22:12].
- Kurz nepoužíva počet prvkov bázy ako **definíciu** dimenzie a v tomto modeli nepoužíva prázdnu bázu nulového podpriestoru [chap-03.woo:1838–1865; la1-P5-par1-kleprlik 1:28:58].
- Všeobecný zákaz iných písmen či hranatých zátvoriek pre súradnice zdroj nemá; pozorované značenie neprezentovať ako univerzálny zákaz. Slovo „pivot“ kurz nepoužíva a appka ho nepíše (Teo 15. 11., `data/Terminology.kt`, ZASADY zásada 6).

### 6. Čo kto zdôrazňuje, chce a zakazuje

| Kto | Krátky doslovný citát | Význam pre autora lekcie a citácia |
|---|---|---|
| Klouda | „Řeší se to pomocí gemu samozřejmě, jako všechno, ale existují dva způsoby.“ | Doplnenie učiť oboma doloženými variantmi [la1-P6-par2-klouda 42:49]. |
| Klouda | „Pak uděláte gem a vyberete jenom ty hlavní sloupce.“ | Pri doplnení za pôvodné vektory najprv pripojí bázu [la1-P6-par2-klouda 43:40]. |
| Klouda | „když si náhodně typnete a pak uděláte gem, který vám dokáže, že jste se trefili“ | Tip musí byť následne overený, nie odmietnutý iba preto, že bol tipom [la1-P6-par2-klouda 44:42]. |
| Krásenský | „Pro nás je dimenze definovaná takhle a ne nějak jinak.“ | Odpoveď na definíciu má vychádzať z LN/LZ súborov [la1-P6-par3-krasensky 56:07]. |
| Krásenský | „tak to by byla argumentace kruhem“ | Pri použití 2.55 nemožno dimenziu opierať o zatiaľ neoverenú bázu [la1-P6-par3-krasensky 1:10:52]. |
| Krásenský | „ale je to soubor v tom podprostoru“ | Pri báze vlastného $P$ nezabudnúť na príslušnosť vektorov do $P$ [la1-P6-par3-krasensky 1:11:22]. |
| Krásenský | „Tohle jsou důležitý příklady, které se vyskytují v zápočtový písemce.“ | Vzťahuje na výber bázy aj doplnenie; pokračuje požiadavkou vysvetliť výpočet [la1-P6-par3-krasensky 1:33:34]. |
| Kleprlík | „A vykopávej ty zbytečné vektory, než vykopneme random vektory.“ | Odoberať možno doložené redundantné generátory, nie náhodne [la1-P6-par1-kleprlik 34:13]. |
| Kleprlík | „Uděláte půlku řádkovýho, půlku sloupcovýho a vytvoříte nějaký nesmysly.“ | Nemiešať odlišné invarianty postupov [la1-P6-par1-kleprlik 1:27:25]. |
| Kleprlík | „Řešíte soustavu linárních rovnic.“ | V kontexte výslovne odmieta formuláciu „vyřešit matici“; titulky majú „linárních“ [la1-P6-par1-kleprlik 1:28:15]. |
| Klouda | „tohle je zase důkaz, který po vás eventuelně budeme chtít“ | Nasleduje existencia a jednoznačnosť koeficientov bázy [la1-P6-par2-klouda 45:26]. |
| MARAST, pokyny | „Dimenze je rovna počtu prvků báze.“ | Práve túto vetu označuje zdroj ako nesprávnu odpoveď na požiadavku definície dimenzie [marast.md:267]. |

Pri dokazovacej úlohe môže zadanie zakázať odvolanie na všeobecnejšiu vetu a jej dôsledky; vtedy treba dokazovať špeciálny prípad z povolených základov [testy-la1-2.md:12,40,45; discord-la1.md:438–442].
Výroky o tom, čo bude v písomke, sú výrokmi v historických nahrávkach, nie potvrdením obsahu konkrétneho budúceho testu.
Proseminár: zdroj nemá titulky, preto mu nepripisujem doslovné príkazy ani citáty.

### 7. Typické úlohy

Frekvencia: vlastný súčet podotázok `parts` s daným `skill` pre LA1 a `file=testy-la1-2.md` v `sources.json`; nejde o počet študentov ani pravdepodobnosť výskytu.
Úlohy s dvoma podotázkami rovnakej zručnosti sa počítajú dvakrát; inventár zahŕňa aj neúplné či zmiešané prepisy.

| Typ | Krátky český príklad alebo skrátený opis zadania | Odkaz | Ako často v tomto inventári |
|---|---|---|---|
| Doplnenie bázy | „Doplňte soubor … na bázi $\mathbb Z_5^4$.“ | [testy-la1-2.md:11; la1-t2-2025-sova-2/a; la1-marast-13494] | 7 podotázok druhých testov, ďalšie v MARASTe. |
| Báza a dimenzia obalu | „Najděte bázi a dimenzi podprostoru $M$.“ | [testy-la1-2.md:28,34,39; la1-marast-13616] | 8 podotázok `span-basis-dim` v 7 úlohách. |
| Iná báza toho istého $M$ | „Vymyslete ještě nějakou jinou bázi … a vysvětlete, proč je to taky báze.“ | [testy-la1-2.md:34; la1-t2-2024-medved-2/d] | 5 podotázok; medveď navyše vyžaduje vektor mimo pôvodného súboru. |
| Súradnice, oba smery | „Ve vámi nalezené bázi najděte souřadnice … Který vektor má souřadnice …?“ | [testy-la1-2.md:49; la1-marast-13617] | 16 podotázok `coords` v 12 úlohách, oba smery spolu. |
| Zmysluplnosť súradníc | „Rozhodněte, která z následujících rovností je smysluplná.“ | [testy-la1-2.md:22,39] | 4 podotázky `coords-meaningful`. |
| Neznáma báza z daných súradníc | „Určete, jak vypadají vektory $\mathbf x$ a $\mathbf y$.“ | [testy-la1-2.md:64,75,82] | 3 podotázky `basis-from-coords`. |
| Parameter a báza | „Pro které hodnoty $a$ je soubor … bází $\mathbb R^3$?“ | [testy-la1-2.md:16,21,67] | Opakovane, tieto 3 čitateľné výskyty; neuvádzam globálny súčet. |
| Definície | „Napište definici báze … dimenze … souřadnic vzhledem k bázi.“ | [testy-la1-2.md:40,45,55] | `def-basis` 7, `def-dim` 2, `def-coords` 3. |
| Dôkaz vzťahu dimenzie a bázy | „Je-li tento soubor lineárně nezávislý, tak je bází podprostoru $P$.“ | [testy-la1-2.md:40,63,74] | 4 podotázky `prove-dim-basis`, oba smery špeciálnych prípadov. |
| Dôkaz jednoznačnosti | „… existují právě jedny souřadnice.“ | [testy-la1-2.md:45,78] | 2 podotázky `prove-coords-unique`. |
| Počet prvkov | „Kolik prvků má podprostor $M$ a proč?“ | [testy-la1-2.md:34,45,68] | 3 podotázky `span-count`. |
| Počet báz pevného podpriestoru | „Mějme $P\subset\subset\mathbb Z_5^3$, který má dimenzi 2. Kolik jeho různých bází existuje?“ | [testy-la1-2.md:69; la1-t2-2022-a-3/d] | 1 podotázka `zp-count-bases`; navyše skriptá a blog. |
| Počet dovolených LZ rozšírení | „Nalezněte nějaký nenulový vektor … Kolik takových vektorů existuje?“ | [testy-la1-2.md:11] | 1 podotázka `zp-count-lz-additions`. |
| Počet alebo výpis podpriestorov | „Kolik různých podprostorů existuje v $\mathbb Z_3^2$? Vysvětlete svou odpověď.“ | [marast-problems/lecture-262.md, #14159; lecture-292.md, #13691] | Doložená ukážková skúška a ukážkový druhý test; tiež otázka skrípt :2563. |
| Výmena prvku bázy | „Který vektor z $X$ je možné nahradit … má-li soubor zůstat bází?“ | [marast-problems/lecture-256.md, #13498] | 1 položka `basis-exchange` v cvičebnici. |

Skriptové cvičenia: počet báz $\mathbb Z_5^3$ (:2163), jednoduché súradnice a opačný prevod (:2404–2420), všeobecné súradnice (:2493,2510), počet podpriestorov (:2563), mapovanie obrázka (:2593).
Skúškové testy majú využitie báz najmä v neskorších témach: bázy vlastných podpriestorov a LN spojenia vlastných vektorov; nie je to dôkaz, že sa samostatná téma 2b skúša rovnako často [testy-la1-zkouska.md:10,13,15].
`testy-rozstrel.md` obsahuje BI-DML, nie doložený LA1 rozstřel; pre tento rozsah zdroj nemá zodpovedajúci LA1 rozstřel.
Otvorené kvízy MARAST: existujú teoretický a početný kvíz ku kapitole 2; ich otázky ani parafrázy sem nepreberám [marast.md:20–36].

### 8. Časté chyby a otázky (Discord, prednášky)

- **Zamenený smer prevodu.** Študent opisuje, že použil vektor ako súradnice a súradnice ako vektor; početne všetko vychádzalo. Vyučujúci LA1 na Discorde predmetu k tomu odpovedá, že je to podľa zadania strata dvoch až štyroch bodov. Neprenášať naňho záver, že práve táto jediná chyba spôsobila neúspech [discord-la1.md:487–500].
- **Nesprávna dĺžka súradníc.** Rozlišovať okolitý rozmer $n$ a dĺžku bázy $d$; testy to skúšajú explicitnými rovnosťami s rôznou dĺžkou [testy-la1-2.md:22,39; la1-P6-par3-krasensky 18:11–18:41].
- **Dimenzia ako definícia cez bázu.** Pravdivá veta nie je požadovaná definícia; Krásenský to odlišuje od praktického výpočtu dimenzie [marast.md:267; la1-P6-par3-krasensky 44:14–44:40, 55:33–56:07].
- **Dokazovanie pomocou dokazovanej vety.** Discord pripúšťa pri počítaní širšie nástroje, pri dôkaze zakazuje samotnú vetu a jej dôsledky [discord-la1.md:438–442].
- **LN neznamená automaticky bázu.** Treba aj generovanie alebo vopred známu dimenziu zodpovedajúcu dĺžke súboru; navyše vektory musia ležať v $P$ [la1-P6-par3-krasensky 1:10:52–1:11:52].
- **Odoberanie ľubovoľných generátorov.** Nie každý vektor LZ súboru možno odobrať bez zmeny obalu [la1-P6-par1-kleprlik 25:09–26:10, 34:13].
- **Dva obaly rovnakej dimenzie nemusia byť rovnaké.** Kontrolovať aj spoločný obal alebo inklúziu [la1-P6-par1-kleprlik 41:56–42:41].
- **Slepé matice bez vysvetlenia.** Krásenský výslovne odmieta ako dostatočné zdôvodnenie samotné vloženie vektorov do stĺpcov a GEM [la1-P5-par3-krasensky 28:30].
- **Miešanie riadkov a stĺpcov.** Pri stĺpcoch vyberať pôvodné vektory; pri riadkovom postupe sa opierať o zachovanie obalu riadkov [la1-P6-par1-kleprlik 1:25:51–1:27:59].
- **Obal verzus podpriestor.** Na Discorde učiteľ vysvetľuje obal ako nástroj vyrábajúci množinu zo súboru, podpriestor ako vlastnosť množiny [discord-la1.md:407–418].
- **Počet báz verzus podpriestorov.** Pri podpriestoroch deliť počtom báz jedného podpriestoru, nie počtom permutácií samotných vektorov [marast-blog-45.md:34–60].

### 9. Rozdiely medzi paralelkami

- **Kleprlík:** motivuje obal „kouzelnou skřínkou“, dôkaz 2.55 rozkladá na krátke implikácie a 2.57 na dva odhady; pri súradniciach ide od geometrie a textúry k definícii a dôkazu [la1-P6-par1-kleprlik 14:53–16:23, 19:30–31:29, 44:20–56:35].
- **Klouda:** v P5 podrobne počíta generovanie a LN nad $\mathbb Z_5$ so všeobecnou pravou stranou. V P6 podrobne dokazuje 2.54 a explicitne menuje oba praktické spôsoby doplnenia [la1-P5-par2-klouda 1:08:01–1:18:47; la1-P6-par2-klouda 15:54–25:01, 42:49–44:57].
- **Krásenský:** najprv ukáže účel bázy cez jediné koeficienty, bijekciu a počet prvkov, potom rovinu $z=2y$ a vektor $(-7,4,8)$ so súradnicami $(-7,4)$; až potom dimenziu [la1-P6-par3-krasensky 10:01–23:42, 40:01–44:51].
- **Krásenský:** venuje viac slov rozdielu medzi definíciou dimenzie a praktickým výpočtom; zdôrazňuje kruhové zdôvodnenie a príslušnosť do podpriestoru [la1-P6-par3-krasensky 43:38–44:40, 48:00–49:59, 1:10:52–1:11:52].
- **Všetci traja:** pri jednoznačnosti koeficientov využijú rozdiel dvoch reprezentácií a LN; rozdiel je v poradí a rozprávaní, nie v matematickom jadre [la1-P6-par1-kleprlik 53:09–56:35; la1-P6-par2-klouda 48:00–54:09; la1-P6-par3-krasensky 15:12–17:46].
- Iba u Kloudu v týchto zdrojoch viem priamo pripísať oba početné varianty doplnenia. Proseminár nemá titulky, preto jeho osobu ani odlišný variant nepotvrdzujem.

### 10. Nejasné a chýbajúce

- Titulky proseminára 22/23-02 chýbajú; chýba presný časový úsek, doslovný výklad a overenie identity hovoriaceho.
- P5 Krásenského je označená `partial`; nemožno z nej vyvodzovať neprítomnosť neprepisovaného výkladu.
- Titulky neobsahujú obraz tabule. Preto presné písmená a čísla tam, kde nie sú vyslovené, rekonštruujem iba zo skrípt/slajdov alebo ich neuvádzam.
- Kleprlíkov prepis geometrického príkladu má rozporné čísla („3,5“, „minus jedna polovina“); správny príklad podľa skrípt je $v=(0,7/2)=\tfrac32(2,3)-(3,1)$ [la1-P6-par1-kleprlik 44:41–45:10; chap-03.woo:2459].
- Krásenský pri motivácii počtu prvkov pre štyri koeficienty vyslovuje v titulkoch len tri násobenia sedmičky, potom $7^4$; určujúce je vysvetlenie štyroch koeficientov, nie doslovný počet faktorov v prepise [la1-P6-par3-krasensky 11:36–12:36].
- Veta z titulkov „nezávislý soubor … horní odhad“ je lokálny preklep alebo prerieknutie: LN dáva dolný odhad podľa 2.53 [la1-P6-par3-krasensky 1:05:38; chap-03.woo:1865].
- Zákaz dosadzovania pri súradniciach zdroj nemá. Teove poznámky požadujú v appke ukázať GEM, ale samy nedokazujú, že vyučujúci zakazujú všetky iné správne riešenia.
- Kloudovu vetu o vysokej šanci náhodného doplnenia nevydávať za vyčíslenú pravdepodobnosť; konkrétny distribučný model ani odhad zdroj nemá [la1-P6-par2-klouda 43:59–44:57].
- Univerzálne vzorce počtov sú doložené blogom a skriptovými otázkami, nie súvislým odvodením v týchto P5/P6. Enumerácia cez rHST z blogu už využíva Frobeniovu vetu, teda presahuje túto časť [marast-blog-45.md:251–317].
- MARAST #13616, #13617, #13494 a #13498 majú v lokálnom prepise výsledok, ale písomný postup „neviditeľný“; netvrdiť, že zdroj ukazuje konkrétnu elimináciu.
- MARAST #13486 má v zadaní posledný vektor $(0,-2,-1,-5)$, no v matici jeho opačný $(0,2,1,5)$. Obal to nemení, ale ide o nevysvetlenú zmenu v prepise; nekopírovať výpočet bez komentára [marast-problems/lecture-256.md, #13486].
- Testový korpus má nečitateľné a zmiešané listy; počty z inventára sú počty evidovaných podotázok, nie nezávislých úplných variantov. Poznámka pri medveďovi o príslušnosti dôkazového listu zostáva v samotnom prepise neistá [testy-la1-2.md:29,78–82].
- Skúškové testy sú len tri prepísané varianty; LA1 rozstřel zdroj nemá [testy-la1-zkouska.md:3; testy-rozstrel.md:1].
- Discord je výber nálezov, nie úplný archív; samotný zdroj priznáva nevykonané tematické dotazy [discord-la1.md:733].

### 11. Surové zdroje

Nasledujúce cesty sú voči koreňom podkladov kurzu (kurz/README.md, mimo repozitára).

- `kontext/BRIEF.md`, pravidlá, osnova a citácie.
- `kontext/INDEX.md`, riadky P5/P6 LA1 a `la1-prosem2223-02`.
- `kontext/cisla/la1-kap2.tsv`, položky 2.43 až 2.72; iba tento súbor určuje čísla.
- `zdroje/src/bi-la1-master/src/textbook/chap-03.woo:1538–2634`; pomocné 2.35 :1290, 2.39 :1426, 2.41 :1482, 2.42 :1495.
- `kontext/skripta/bi-la1-master/src/slides/bi-la1-prezentace-02.tex:544–900`.
- `kontext/txt/la1-P5-par1-kleprlik.txt`, 1:03:01–1:36:39.
- `kontext/txt/la1-P5-par2-klouda.txt`, 1:03:04–1:36:38; pomocný dôkaz predĺženia LN 58:45–1:02:34.
- `kontext/txt/la1-P5-par3-krasensky.txt`, 1:30:55–1:36:04; upozornenie na zdôvodnenie 28:30.
- `kontext/txt/la1-P6-par1-kleprlik.txt`, 14:53–1:00:33; riadková alternatíva a upozornenia 1:25:10–1:28:45.
- `kontext/txt/la1-P6-par2-klouda.txt`, 13:13–55:01; kľúčové doplnenie 42:29–44:57.
- `kontext/txt/la1-P6-par3-krasensky.txt`, 06:09–1:35:36.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`, LA1, identifikátory uvedené vyššie; otvorený `kind=quiz` sa nepoužíva ako znenie ani parafráza úloh.
- `zdroje/testy-la1-2.md:9–82`, najmä :11,22,28,34,39,40,45,49,64,68,69,75.
- `zdroje/testy-la1-zkouska.md:3–23`; `testy-rozstrel.md:1`, iba overenie predmetu.
- `zdroje/marast-problems/lecture-256.md`, #13615, #13616, #13617, #13462, #13486, #13487, #13489, #13494, #13498, #13504.
- `zdroje/marast-problems/lecture-292.md`, #13488 a #13691; `lecture-262.md`, #14159.
- `zdroje/marast-blog-45.md:18–68`, počty; :251–317, hranica presahu cez Frobeniovu vetu.
- `zdroje/marast.md:20–36,267`, existencia otvorených kvízov a pokyny k definíciám; `marast-cvicebnice.md:77–85,119–123`, dostupnosť príkladov a rozsah.
- `zdroje/discord-la1.md:407–442,487–500,733`.

Kontrola citácií: pred odovzdaním opätovne náhodne overené najmenej 10 odkazov priamo v lokálnych zdrojoch; presné znenia citátov sa vzťahujú na automatické titulky, nie na neoverený obraz tabule.

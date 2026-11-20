# BI-DML kap. 3: Predikátová logika

Balík kapitoly: skriptá `chap-03-predikatova-logika.woo` + `cv-03`, slajdy `DML_P02`, kódy rozvrhu PL1, PL2; v appke zatiaľ bez lekcií.

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav lokálnych podkladov: 17. 11. 2026. Výťah zachytáva kapitolu 3 a výslovne označený presah k obmedzeným kvantifikátorom z úvodu kapitoly 4.
Čísla definícií a viet sú výhradne z `cisla/dml-kap3.tsv`; zhodujú sa s Anki sekciou Predikátová logika, riadky 52–74.
Skratka PL znamená predikátovú logiku, VL výrokovú logiku.

Sekcie skrípt:

- Motivace, riadky 12–106: hranice VL, príklady tvrdení o ľuďoch a prirodzených číslach.
- Od přirozeného jazyka přes výrokovou logiku k predikátové logice, 107–310: Definice 3.1, kvantifikátory.
- Formální definice, 311–984: Definice 3.2–3.5, jazyk, termy, formule, výskyty premenných a formalizácia.
- Interpretace a pravdivost, 985–1409: Definice 3.6–3.9, Věta 3.1.
- Logická ekvivalence a logický důsledek, 1410–1532: Definice 3.10, Tvrzení 3.1–3.2.
- Pravidla pro práci s predikátovými formulemi, 1533–2171: Věty 3.2–3.6 a Důsledek 3.1.
- Zajímavosti, 2172–2359: Věty 3.7–3.8. Slajdy túto časť označujú ako nepovinnú. [P02_log_predikatova.tex:1273]

Časové rozsahy sú hranice tematického výkladu podľa titulkov, nie záruka súvislej reči bez prestávok:

- Dombek: `dml-P2-par1-dombek` 07:04–1:37:54, od motivácie po pravdivosť; `dml-P3-par1-dombek` 06:32–59:41, zákony a úpravy; presah k obmedzeným kvantifikátorom 1:05:34–1:11:51.
- Spěvák: `dml-P2-par2-spevak` 1:20:03–1:32:28, úvod PL; `dml-P3-par2-spevak` 07:18–1:16:31, formálne pojmy až výmena kvantifikátorov; presah k obmedzenému a skrátenému zápisu 1:21:27–1:35:20.
- Legerský: `dml-P3-par3-legersky` 25:57–1:34:49, motivácia až pravdivosť; `dml-P4-par3-legersky` 06:32–1:11:10, zopakovanie a pravidlá; presah k obmedzeným kvantifikátorom 1:20:32–1:29:49.
- Proseminár `dml-prosem2526-03`, video `youtube-bjuF9lGBWag`: titulky chýbajú, INDEX uvádza 0 cues. Označenie „proseminář“ v cvičeniach nie je dokladom toho, čo vo videu zaznelo. [INDEX.md; cv-03-predikatova-logika.woo:33]

V určených titulkoch sa preberá jadro až po Větu 3.6; samostatný výklad Věty 3.8 o distribúcii s implikáciou som nenašiel.
Věta 3.7 nie je samostatne doložená ako odprednášaná veta, hoci súvislosť spojky a logického vzťahu Spěvák slovne vysvetľuje na záver.
Neprednášané v týchto nahrávkach neznamená nevyžadované v celom predmete. [dml-P3-par2-spevak 1:33:24–1:35:20; chap-03-predikatova-logika.woo:2179,2294]

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **VL nevidí dovnútra tvrdenia.** „Každý má někoho rád“ a „Karel má někoho rád“ súvisia tým, že Karel patrí medzi všetkých; samostatné písmená VL túto väzbu nezachytia. [dml-P2-par1-dombek 07:58–09:55; dml-P2-par2-spevak 1:21:21–1:22:37]

2. **Dlhý konečný zoznam nie je vhodná náhrada kvantifikácie.** Niekto z konečného ľudstva môže znamenať veľkú disjunkciu, každý veľkú konjunkciu; pre všetky prirodzené čísla už nekonečný výrokový reťazec nepomôže. [dml-P2-par1-dombek 12:30–15:16; dml-P3-par3-legersky 29:20–30:16]

3. **Rozlíšime objekt a tvrdenie o objekte.** Term je to, o čom hovoríme; predikát z termov vytvorí atomickú formulu. Nasledujú Definice 3.2–3.4, nie iba pridanie dvoch symbolov k VL. [dml-P2-par1-dombek 37:40–45:47; dml-P3-par3-legersky 30:27–32:03]

4. **Kvantifikátor musí mať premennú a dosah.** Definice 3.1 a 3.4 umožňujú zapísať každý/niekto; poradie argumentov v $r(x,y)$ aj poradie kvantifikátorov mení význam. [dml-P2-par1-dombek 23:20–23:39,26:06–26:20; dml-P3-par3-legersky 36:03]

5. **Najprv správne prečítame štruktúru.** Definice 3.5 oddeľuje voľné a viazané výskyty; formačný strom je pomôcka na pochopenie, kam patria spojky a kvantifikátory. Dombek osobitne varuje pred domýšľaním zátvoriek. [dml-P2-par1-dombek 48:00–55:26]

6. **Význam príde až interpretáciou.** Definice 3.6 zvolí neprázdné univerzum a význam mimologických symbolov; kontext priradí hodnoty premenným, vo formule sa dosadzuje len za voľné výskyty. [dml-P2-par1-dombek 1:17:30–1:21:08; dml-P3-par2-spevak 26:03–29:27]

7. **Pravdivosť má tri úrovne.** Definice 3.7–3.9 odlišujú pravdivosť pri pevnej interpretácii a kontexte, vo všetkých kontextoch jednej interpretácie a vo všetkých interpretáciách. Uzavretá formula už nepotrebuje kontext, stále však potrebuje interpretáciu. [dml-P2-par1-dombek 1:29:07–1:37:43; dml-P3-par3-legersky 1:29:37–1:34:19]

8. **VL sa znovu používa ako nástroj.** Věty 3.1–3.2 a Tvrzení 3.2 umožňujú rozpoznať opakujúce sa podformuly, použiť výrokový zákon a upravovať aj vnútro PL formuly. [dml-P3-par1-dombek 07:54–14:31,20:09–22:22; dml-P3-par2-spevak 53:43–58:20]

9. **Negácia sa postupne presúva dovnútra.** Věta 3.4 preklopí kvantifikátor a nechá pokračovať negáciu; pri spojke sa použije príslušný zákon VL. Cieľom sú negácie iba pred atomickými formulami. [dml-P3-par1-dombek 33:55–36:17; dml-P3-par2-spevak 53:04–53:28]

10. **Preklad vybranej skupiny vyžaduje správnu spojku.** Všetky kočky sú šelmy sa píše s $\forall$ a implikáciou, niektorá kočka so $\exists$ a konjunkciou; negácie spájajú diagonály Aristotelovho štvorca. [dml-P3-par1-dombek 40:20–46:39; dml-P4-par3-legersky 53:18–58:03]

11. **Distribúcia a zmena poradia majú hranice.** Věty 3.5–3.6 rozlišujú ekvivalencie od jednosmerných dôsledkov; príklady s jedlom a olympiádami ukážu, prečo opačný smer zlyhá. [dml-P3-par1-dombek 47:49–59:11; dml-P3-par2-spevak 1:08:02–1:16:22]

12. **Až potom príde pohodlnejší matematický zápis.** Obmedzené kvantifikátory sú vedomé skrátenie, ktoré sa v ďalších témach používa, ale pri otázke z čistej PL sa musí rozbaliť. [dml-P3-par1-dombek 1:05:34–1:09:28; dml-P4-par3-legersky 1:21:39–1:23:41]

Poradie paraleliek sa líši hlavne tempom: Spěvák prvý raz motivuje PL až na konci P2, Legerský až po dokončení VL v P3; obsahové príklady sa vo veľkej miere zhodujú so slajdmi.

## 3. Pojmy a tvrdenia

V tabuľke „definícia“ znamená, že sa nedokazuje. Pri vetách rozlišujem dôkaz, náčrt a ilustráciu.
Dôležitosť nie je odhad výskytu na budúcej skúške: uvádzam iba doložené použitie v zdrojoch.
Symbol $\models$ označuje logický dôsledok, $\mathrel{\vert\mkern-3mu=\mkern-3mu\vert}$ logickú ekvivalenciu.

| Číslo | Druh | Názov | Jadro | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 3.1 | Definice | Kvantifikátory | $(\forall x)F$ platí pre každý prvok, $(\exists x)F$ pre aspoň jeden. [chap-03-predikatova-logika.woo:159] | Definícia, nedokazuje sa; Dombek 23:11–24:14 v P2. | Základ formalizácie a negácie [testy-dml-2.md:63]. |
| 3.2 | Definice | Jazyk predikátové logiky | Logické symboly oddeľuje od konštánt, funkcií a predikátov s určenou aritou. [chap-03-predikatova-logika.woo:319] | Definícia; Spěvák P3 10:37–12:53. | Podmienka korektného zápisu [cv-03-predikatova-logika.woo:148]. |
| 3.3 | Definice | Termy | Premenné a konštanty sú termy, aplikácia funkcie na príslušný počet termov vytvorí ďalší term. [chap-03-predikatova-logika.woo:424] | Definícia; Dombek P2 37:40–42:09. | Výpis termov sa žiada v cvičeniach [cv-03-predikatova-logika.woo:158]. |
| 3.4 | Definice | Formule predikátové logiky | Atómy vzniknú predikátom na termoch; formule ďalej spojkami a kvantifikáciou. [chap-03-predikatova-logika.woo:471] | Definícia; Dombek P2 43:05–45:47. | Formalizácia na teste [testy-dml-2.md:63]. |
| 3.5 | Definice | Volné a vázané proměnné, otevřená a uzavřená formule | Otvorená má iba voľné výskyty, uzavretá iba viazané; nie každá formula patrí do jednej z týchto tried. [chap-03-predikatova-logika.woo:513] | Definícia; Dombek P2 49:28–51:22. | Nutné pre kontext; samostatnú testovú otázku inventár nedokladá. |
| 3.6 | Definice | Interpretace a kontext | Interpretácia dá univerzum a význam symbolov; kontext priradí prvky premenným. [chap-03-predikatova-logika.woo:1037] | Definícia; Spěvák P3 26:03–29:27. | Prednášaný základ pravdivosti; priamy test v lokálnom výbere zdroj nemá. |
| 3.7 | Definice | Pravdivost v interpretaci a kontextu | Atómy sa vyhodnotia vo vzťahoch, spojky podľa VL a kvantifikátory cez voľné výskyty v tele. [chap-03-predikatova-logika.woo:1241] | Definícia; Spěvák P3 36:27–39:49. | Základ zdôvodňovania a protipríkladov [cv-03-predikatova-logika.woo:803]. |
| 3.8 | Definice | Pravdivost v interpretaci | Vyžaduje pravdivosť v každom kontexte danej interpretácie. [chap-03-predikatova-logika.woo:1275] | Definícia; Spěvák P3 40:19–41:51. | Súčasť rozlišovania úrovní pravdivosti; frekvenciu testov zdroj nemá. |
| 3.9 | Definice | Tautologie, splnitelnost a kontradikce v predikátové logice | Tautológia platí všade; splniteľnosť potrebuje jednu interpretáciu a kontext; kontradikcia nijaké nepripúšťa. [chap-03-predikatova-logika.woo:1284] | Definícia; Dombek P2 1:36:20–1:37:43. | Výslovný cieľ kurzu [P02_log_predikatova.tex:998]. |
| 3.1 | Věta | Pravdivost predikátové formule vzniklé z formule výrokové | Systematická náhrada rovnakých prvotných formúl rovnakými PL formulami zachová tautológiu a kontradikciu. [chap-03-predikatova-logika.woo:1328] | Skriptá áno, 1347; Dombek P3 09:59–11:30 slovne. | Rozpoznávanie vzoru v kvízoch [dml-P3-par1-dombek 11:30–14:31]. |
| 3.10 | Definice | Logická ekvivalence a důsledek v predikátové logice | Porovnáva pravdivosť oboch formúl pri každej rovnakej interpretácii a kontexte. [chap-03-predikatova-logika.woo:1417] | Definícia; Dombek P3 14:51–16:50. | Priamo rozstrel [testy-rozstrel.md:7,20,22,32]. |
| 3.1 | Tvrzení | Základní vztahy mezi logickým důsledkem a ekvivalencí v PL | Dôsledok je tranzitívny; ekvivalencia znamená dôsledok v oboch smeroch a je reflexívna, symetrická, tranzitívna. [chap-03-predikatova-logika.woo:1460] | Skriptá áno, krátko 1493; Dombek P3 18:50–19:50 vysvetlenie. | Spájanie krokov dôkazu [cv-03-predikatova-logika.woo:803]. |
| 3.2 | Tvrzení | Náhrada podformule logicky ekvivalentní formulí v PL | Ekvivalentnú podformulu možno vložiť aj do väčšej formuly. [chap-03-predikatova-logika.woo:1500] | Skriptá náčrt 1511; Dombek P3 20:09–22:22 stromová intuícia. | Úpravy a dôkazy [discord-dml.md:33]. |
| 3.2 | Věta | Logická ekvivalence a důsledek v PL odvozené z VL | Systematické dosadenie PL formúl zachová výrokovú ekvivalenciu aj dôsledok. [chap-03-predikatova-logika.woo:1576] | Skriptá áno, odkaz na Věty 3.1 a 3.7, 1595; Dombek P3 24:40–25:48 vysvetlenie. | Použitie zákonov VL vo všetkých úpravách [cv-03-predikatova-logika.woo:803]. |
| 3.1 | Důsledek | Predikátová logika dědí logické zákony po logice výrokové | Zákony VL ostávajú nástrojom aj vo vnútri PL formúl. [chap-03-predikatova-logika.woo:1620] | Samostatný dôkaz nie, dôsledok Věty 3.2; Dombek P3 26:32–28:29. | Negácia a ekvivalencie [testy-dml-2.md:63,69]. |
| 3.3 | Věta | Výměna kvantifikátorů | $(\forall x)F(x)\models F(x)\models(\exists x)F(x)$, aj cez dosadenú konštantu. [chap-03-predikatova-logika.woo:1673] | Skriptá áno, 1693; Spěvák P3 59:24–1:01:18 z definície. | Oslabovanie tvrdenia [testy-rozstrel.md:7,20]. |
| 3.4 | Věta | Negace kvantifikátorů | $\neg(\exists x)F$ je ekvivalentná $(\forall x)\neg F$, obdobne opačný typ. [chap-03-predikatova-logika.woo:1740] | Skriptá iba intuícia konečného univerza, 1758; Dombek P3 34:55–35:59 rovnako; Spěvák 1:02:23–1:02:54 odkaz na definíciu. | Priamo test a rozstrel [testy-dml-2.md:63; testy-rozstrel.md:7]. |
| 3.5 | Věta | Distribuce kvantifikátorů - s konjunkcí a disjunkcí | $\forall$ distribuuje ekvivalentne cez $\wedge$, $\exists$ cez $\vee$; krížové kombinácie iba jednosmerne. [chap-03-predikatova-logika.woo:2022] | Skriptá nie, ilustračné príklady; Spěvák P3 1:08:12 výslovne bez dôkazu. | Priamo dve otázky [testy-rozstrel.md:22,32]. |
| 3.6 | Věta | Výměna dvou kvantifikátorů | Rovnaké typy možno zameniť; $(\exists x)(\forall y)F\models(\forall y)(\exists x)F$, všeobecne nie opačne. [chap-03-predikatova-logika.woo:2121] | Skriptá nie, ilustrácie; Dombek P3 56:09–59:11 príklady. | Priamo [testy-rozstrel.md:32]. |
| 3.7 | Věta | Logická ekvivalence a důsledek v PL pomocí tautologie a kontradikce | Ekvivalencia zodpovedá tautológii $E\Leftrightarrow F$; dôsledok tautológii $E\Rightarrow F$ alebo kontradikcii $E\wedge\neg F$. [chap-03-predikatova-logika.woo:2179] | Skriptá náčrt 2201; samostatný prednáškový dôkaz zdroj nemá. | Pomocná väzba pre úpravy; samostatné testové zadanie zdroj nemá. |

Větu 3.8 nerozpisujem ako jadro: je v Zajímavostiach, Anki ju obsahuje, ale priame použitie v určených testoch a titulkoch nie je doložené.
Presné znenie ostáva v [chap-03-predikatova-logika.woo:2294; anki-dml.txt:74].

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Preklad prirodzeného jazyka do PL a späť**

1. Urči univerzum, povolené predikáty, ich aritu, význam a poradie argumentov; konštanty označ osobitne.
2. Rozlož vetu na to, o kom hovorí a čo o ňom tvrdí; zvoľ kvantifikátory v poradí závislostí.
3. Obmedzenie skupiny zapíš v tele formuly: všetci $p$ sú $q$ ako $(\forall x)(p(x)\Rightarrow q(x))$; niektorý $p$ je $q$ ako $(\exists x)(p(x)\wedge q(x))$.
4. Zátvorkami vyznač celé telo kvantifikácie; prečítaj výsledok späť a skontroluj, či je to pôvodná veta.
5. Pri zložitom aritmetickom tvrdení možno dočasne zaviesť pomocný predikát, ale výsledok prepíš do pôvodného povoleného jazyka, ak to zadanie žiada.

Opora: Definice 3.1–3.4, Aristotelove súdy a varovanie o spojkách.
Dombek ukazuje kočky a šelmy; skriptá aj postupné budovanie deliteľnosti a najväčšieho spoločného deliteľa. [dml-P3-par1-dombek 42:15–46:39; chap-03-predikatova-logika.woo:884–984,1852–1912; cv-03-predikatova-logika.woo:148–158]

Konkrétne „Všichni studenti jsou zde“ znamená $(\forall x)(s(x)\Rightarrow z(x))$.
„Jsou zde jen studenti“ obracia podmienku: $(\forall x)(z(x)\Rightarrow s(x))$.
Tieto vety netvrdia to isté. [cv-03-predikatova-logika.woo:21–99]

**B. Negácia formuly až pred atómy**

1. Napíš $\neg F$ so zachovanou štruktúrou celého $F$.
2. Pri kvantifikátore použi Větu 3.4: preklop $\forall/\exists$, pošli negáciu do tela, poradie premenných zachovaj.
3. Pri spojke použi zákon VL: najmä De Morganove zákony a $\neg(A\Rightarrow B)$ ekvivalentné $A\wedge\neg B$.
4. Opakuj, odstráň dvojité negácie; skonči, keď sú negácie iba pred atómami.
5. Výsledok prelož do zrozumiteľnej češtiny, ak sa to žiada; dvojitý gramatický zápor nepočítaj ako dve logické negácie.

Opora: Věta 3.4, Důsledek 3.1 a Tvrzení 3.2. [dml-P3-par1-dombek 33:55–39:50; dml-P3-par2-spevak 53:04–53:28; cv-03-predikatova-logika.woo:21–25]

Vzor z prednášky: $\neg(\forall x)(\exists y)r(x,y)$ sa zmení na $(\exists x)(\forall y)\neg r(x,y)$.
Význam: existuje človek, ktorý nemá nikoho rád. [dml-P3-par1-dombek 38:02–38:58]
Legerský odporúča pri negovaní slovnej vety trasu veta → formula → negácia → veta, nie samotný jazykový odhad. [dml-P4-par3-legersky 28:44–29:14,50:10]

**C. Interpretácia a vyhodnotenie pravdivosti**

1. Zvoľ neprázdne $\mathcal U$; konštantám priraď prvky, funkciám funkcie na univerze, predikátom vzťahy.
2. Rozlíš každý voľný a viazaný výskyt; kontext síce určuje všetky premenné jazyka, ale vo formule nahrádzaj iba voľné výskyty.
3. Vyhodnoť termy a atómy, potom spojky podľa VL; pri $\exists$ hľadaj aspoň jeden vhodný prvok, pri $\forall$ vyžaduj všetky.
4. Pri každom kvantifikátore meň iba voľné výskyty jeho premennej v jeho tele, nie výskyty viazané vnútorným kvantifikátorom.
5. Na pravdivosť v interpretácii nestačí jeden kontext; na tautológiu nestačí jedna interpretácia.

Opora: Definice 3.5–3.9. Spěvák ukazuje postup od atómov smerom nahor; Legerský pripúšťa aj rozklad od vonkajšieho kvantifikátora dovnútra. [dml-P3-par2-spevak 32:19–39:49; dml-P3-par3-legersky 1:31:46–1:33:05]

Na $\mathbb R$ s $r(x,y)$ vo význame $x<y$ platí $(\forall x)(\exists y)r(x,y)$, ale neplatí $(\exists x)(\forall y)r(x,y)$.
To je porovnanie v konkrétnej interpretácii, nie vyhlásenie prvej formuly za tautológiu. [P02_log_predikatova.tex:711–735; dml-P2-par1-dombek 1:25:04–1:28:28]

**D. Rozpoznanie výrokového vzoru**

1. Nájdite opakujúce sa rovnaké PL podformuly.
2. Dočasne ich označte $A,B,\dots$ a rozpoznajte známu VL tautológiu, kontradikciu alebo zákon.
3. Uveďte Větu 3.1 alebo 3.2; úpravu vnútri väčšej formuly oprieť aj o Tvrzení 3.2.

Dombek tento postup ukazuje na kontrapozícii a vzore $A\wedge\neg A$; rovnaká prvotná formula musí byť všade nahradená tou istou PL formulou. [dml-P3-par1-dombek 08:28–14:31]
Neúspech pri rozpoznávaní výrokovej tautológie sám osebe nie je dôkazom, že PL formula nie je tautológia; Věta 3.1 tvrdí uvedený smer prenosu. [chap-03-predikatova-logika.woo:1328]

**E. Dôkaz ekvivalencie alebo dôsledku úpravami**

1. Uprav negácie a spojky, prípadne bezpečne premenuj viazané premenné.
2. Zapíš reťazec logických ekvivalencií; každý krok odôvodni konkrétnym pravidlom.
3. Distribúciu urob iba v dovolenom smere; ak použiješ jednosmerný dôsledok, označ ho ako dôsledok.
4. Tranzitivitou uzavri výsledok. Ak máš dokázať ekvivalenciu a používaš jednosmerné kroky, potrebuješ aj opačný smer.
5. Neplatný smer vyvráť konkrétnou interpretáciou a prípadne kontextom, kde predpoklad platí a záver neplatí.

Opora: Definice 3.10, Tvrzení 3.1–3.2, Věty 3.2–3.6.
Požiadavka na zdôvodnenie každého kroku je aj v učiteľovej odpovedi na Discorde. [discord-dml.md:30–35; dml-P3-par1-dombek 58:41–59:11; cv-03-predikatova-logika.woo:803–873]

Používané pravidlá distribúcie, s predpokladmi Věty 3.5 o voľnej $x$ v $E,F$:

- $(\forall x)(E\wedge F)$ je ekvivalentná $(\forall x)E\wedge(\forall x)F$.
- $(\exists x)(E\vee F)$ je ekvivalentná $(\exists x)E\vee(\exists x)F$.
- $(\forall x)E\vee(\forall x)F\models(\forall x)(E\vee F)$, všeobecne nie späť.
- $(\exists x)(E\wedge F)\models(\exists x)E\wedge(\exists x)F$, všeobecne nie späť.

Pri neplatnom spätnom smere sa môžu rôzni ľudia rozhodnúť pre rôzne jedlá alebo rôzni spolužiaci absolvovať rôzne olympiády. [Věta 3.5, chap-03-predikatova-logika.woo:2022–2111; dml-P3-par1-dombek 51:46–53:49]
Dombek ukazuje aj dvojkrok: premenuj viazanú premennú v jednej podformule, až potom spoj rovnaké existenčné kvantifikátory nad disjunkciou. [dml-P3-par1-dombek 55:03–55:39]

**F. Obmedzené kvantifikátory: presah do kapitoly 4**

1. $(\forall x\in M)F$ rozbaľ ako $(\forall x)(x\in M\Rightarrow F)$.
2. $(\exists x\in M)F$ rozbaľ ako $(\exists x)(x\in M\wedge F)$.
3. Pri negácii zmeň typ kvantifikátora, ponechaj obmedzenie a neguj $F$.
4. Napríklad $\neg(\forall x>0)F$ zodpovedá $(\exists x>0)\neg F$, nie kvantifikácii nad nekladnými číslami.

Opora: rozbalenie, Věta 3.4 a zákony VL; znenie v kapitole 4 nečíslujem, lebo číselná tabuľka zadania je iba pre kapitolu 3.
[chap-04-dukazy-indukce.woo:115–158; dml-P4-par3-legersky 1:24:53–1:26:40]
Pri úlohe z čistej PL používaj plnú podobu a povolený jazyk; samotné množinové obmedzenie ešte nemusí byť povoleným symbolom zadania. [dml-P3-par1-dombek 1:07:24–1:07:49; cv-03-predikatova-logika.woo:148]

**G. Počet objektov: existencia, rôznosť a úplnosť**

Pri „aspoň dve knihy“ treba dva existenčné kvantifikátory, obe vlastnosti a $x\ne y$.
Pri „práve dve“ pridaj, že každá kniha na stole sa rovná jednému z týchto dvoch svedkov.
Pri „najviac jedna“ univerzálne tvrď, že ak dve veci sú knihami na stole, musia byť rovnaké; existencia sa nevyžaduje.
[cv-03-predikatova-logika.woo:592–676]
Skriptá nepoužívajú v týchto riešeniach samostatný kvantifikátor jednoznačnej existencie, ale rozpis pomocou $\exists,\forall$ a rovnosti. [cv-03-predikatova-logika.woo:632–676]

## 5. Značenie a terminológia

- **„Obecný / universální / velký kvantifikátor“:** $\forall$; **„existenční / malý“:** $\exists$. Vo formule písať $(\forall x)$, $(\exists x)$ spolu s premennou. [Definice 3.1, chap-03-predikatova-logika.woo:159; dml-P2-par1-dombek 23:20–23:39]
- **Dosah:** kvantifikátor sa vzťahuje na bezprostredne nasledujúcu najkratšiu formulu; širší dosah treba vyznačiť zátvorkami. [dml-P2-par1-dombek 53:25]
- **Vonkajšie zátvorky:** kurz pripúšťa ich vynechanie; to nie je povolenie vynechať vnútorné zátvorky určujúce štruktúru. [dml-P3-par2-spevak 20:36,21:53; dml-P3-par3-legersky 53:45–53:56]
- **„Arita / četnost“:** počet argumentov; unárny jeden, binárny dva, ternárny tri. Funkcia dá objekt, predikát dá tvrdenie o objektoch. [Definice 3.2–3.4, chap-03-predikatova-logika.woo:319,424,471]
- **„Term“ verzus „atomická formule“:** $f(x,y)$ je pri funkčnom $f$ term, $p(f(x,y))$ pri unárnom predikáte $p$ atóm; funkčný výraz sám nie je pravdivý/nepravdivý. [dml-P2-par1-dombek 43:49–44:29]
- **„Volný/vázaný výskyt“ verzus „volná/vázaná proměnná“:** jedna premenná môže mať v tej istej formule oba druhy výskytov. [Definice 3.5, chap-03-predikatova-logika.woo:513; dml-P3-par2-spevak 24:53]
- **„Otevřená / uzavřená formule“:** v tomto kurze iba voľné / iba viazané výskyty. Formula $(\forall x)r(x,y)$ nie je ani otvorená, ani uzavretá. [dml-P2-par1-dombek 50:26–51:22; anki-dml.txt:56]
- **„Universum“:** $\mathcal U$, neprázdne. Prázdna skupina vybraná predikátom sa nesmie zameniť s prázdnym univerzom. [Definice 3.6, chap-03-predikatova-logika.woo:1037]
- **Logický dôsledok:** v zdroji makro `\thus`, vykreslené $\models$; ekvivalencia makro `\logi`, vykreslené $\mathrel{\vert\mkern-3mu=\mkern-3mu\vert}$. Ide o vzťahy medzi formulami. [Definice 3.10, chap-03-predikatova-logika.woo:1417; anki-dml.txt:63]
- **Spojky:** $\Rightarrow$, $\Leftrightarrow$ tvoria ďalšiu formulu; nie sú v čistej logike automaticky zameniteľné za predchádzajúce vzťahy. Mimo logiky kurz tieto šípky používa aj pri tvrdeniach a úpravách s významom platnosti. [dml-P3-par2-spevak 1:33:24–1:35:20]
- **Poradie kvantifikátorov:** rovnaké typy komutujú podľa Věty 3.6; rôzne typy všeobecne nie. Poradie argumentov predikátu sa pritom svojvoľne nemení. [chap-03-predikatova-logika.woo:2121; dml-P3-par3-legersky 36:03]
- **Premenovanie:** viazanú premennú zmeň pri kvantifikátore aj v jeho tele a nepouži symbol inej premennej s výskytom v tejto podformule. [chap-03-predikatova-logika.woo:1444; dml-P3-par1-dombek 17:50–18:20]
- **Obmedzený zápis:** $(\forall x\in M)$ či $(\exists x>0)$ je ďalšia skratka, nie formálna konštrukcia Definice 3.4. Zoskupenie viacerých premenných pod jeden kvantifikátor sa zavádza až medzi zjednodušeniami. [dml-P3-par2-spevak 1:30:46–1:32:24]
- **Číselné univerzá v príkladoch:** $\mathbb N=\{1,2,\dots\}$, nula patrí do $\mathbb N_0$; ide o konvenciu kurzu, nie celosvetovo jednotné značenie. [dml-P3-par1-dombek 1:00:39–1:01:35]
- Pevný zákaz alternatívnych písmen pre premenné zdroj nemá; Dombek žiada, aby bola ich rola jasná z kontextu. Overený rozdiel symbolov medzi paralelkami zdroj nemá. [dml-P2-par1-dombek 16:32–16:40]

## 6. Čo kto zdôrazňuje, chce a zakazuje

Nasledujú doslovné krátke úryvky titulkov; skomolené slová v citáte neopravujem.

| Kto | Čo povedal | Citácia |
|---|---|---|
| Dombek | „Ve formuli bude vždycky kvantifikátor s proměnou a se závorkou.“ | [dml-P2-par1-dombek 23:36] |
| Dombek | „Prosím, nepředstavujme si závorky.“ | [dml-P2-par1-dombek 52:41] |
| Dombek | „Jasně, že po vás budem chtít exaktní definice“ | [dml-P3-par1-dombek 22:47] |
| Dombek | „vlastně tady ty důkazy po vás“; „vlastně nebudem chtít, jo.“ | [dml-P3-par1-dombek 22:28–22:32] |
| Dombek | „Jo, nesmíte zapomenout na to, že se posílá dovnitř.“ | [dml-P3-par1-dombek 36:14] |
| Dombek | „Ať vás ani nenapadne v první zápočtové písemce používat nějakou zkratku, co znáte ze střední školy, to vám neuznáme.“ | [dml-P3-par1-dombek 43:58] |
| Dombek | „Prosím vás, v první zápočtové písemce tohle pro vás neexistuje.“ | [dml-P3-par1-dombek 1:07:24] |
| Dombek | „Když se vás u zkoušky zeptáme na nějaké definice a základní věci z predikátové logiky,“; „tohle pro vás neexistuje.“ | [dml-P3-par1-dombek 1:07:29–1:07:34] |
| Spěvák | „aby stála ta negace jen před atomickýma formulema.“ | [dml-P3-par2-spevak 53:28] |
| Spěvák | „V žádném případě to nemůžu číst jako každá kočka není černá.“ | [dml-P3-par2-spevak 1:03:54] |
| Legerský | „Nedělejte to intuitivně negace.“ | [dml-P4-par3-legersky 50:10] |
| Legerský | „Pozor, omezené kvantifikátory nejsou predikátová logika.“ | [dml-P4-par3-legersky 57:33] |
| Legerský | „tak klidně zjednodušujte a použijte omezené kvantifikátory.“ | [dml-P4-par3-legersky 1:23:38] |
| Vyučujúci DML, Discord | parafráza: každú ekvivalenciu v reťazci treba vedieť zdôvodniť odkazom na niektorú vetu zo skrípt | [discord-dml.md:33] |

Dombekova poznámka o nevyžadovaní dôkazov sa týka práve vysvetľovaných všeobecných tvrdení a nedostatočne formalizovaného aparátu; nie je zákazom zadávať dôkaz ekvivalencie konkrétnych formúl.
Dôkazy konkrétnych úprav sú doložené cvičeniami aj Discordom. [dml-P3-par1-dombek 22:24–22:55; cv-03-predikatova-logika.woo:803; discord-dml.md:33]
Poučka o párovaní $\forall$ s implikáciou a $\exists$ s konjunkciou sa týka vyjadrenia vybranej skupiny, nie zákazu všetkých ostatných kombinácií; Věta 3.5 ich výslovne skúma.
[chap-03-predikatova-logika.woo:1884–1912,2022]
Proseminár: zdroj nemá titulky, preto mu nepripisujem žiadny výrok ani zákaz.

## 7. Typické úlohy

Početnosti nižšie znamenajú výskyty v lokálnom výbere, nie pravdepodobnosť budúceho testu.
Inventár `sources.json` má pre DML a `chapter=PL` 62 položiek a 129 podotázok: 49 prednáškových, 6 testových, 6 rozstrelových a 1 skriptovú položku.
Kontrola proti zneniu odhalila chybné zaradenia: štyri zo šiestich testových položiek sú VL, rovnako dve zo šiestich rozstrelových.
Do obsahovej početnosti PL ich nezapočítavam; inventár nie je úplným zoznamom cvičení kapitoly 3.

| Typ | Krátky príklad zadania po česky | Zdroj | Ako často v tomto výbere |
|---|---|---|---|
| Preklad, negácia, spätný preklad | „Každý absolvent PA1, který současně zvládl DML nebo LA1, zvládl jistě i TZP.“ | [testy-dml-2.md:63; sources.json: dml-t2-2021-1203odp-3] | Jeden priamo doložený testový príklad, 7 bodov, 2 podotázky v inventári. |
| Vzťahy medzi viacerými formulami | „dokázat, že v dvojicích je jedna logickým důsledkem druhé“ | [testy-dml-2.md:69; sources.json: dml-t2-2021-1201-1] | Jedna študentská spomienka; presné úplné znenie chýba. |
| Dôsledky negácie kvantifikovanej formuly | „Které formule jsou logickým důsledkem NEGACE“ | [testy-rozstrel.md:7; sources.json: dml-rozstrel-2023-01-11-1] | 1 otázka z 11. 1. 2023. |
| Dôsledky striedania kvantifikátorov | „Které formule jsou logickým důsledkem“ $(\forall x)(\exists y)\neg r(x,y)$ | [testy-rozstrel.md:20; sources.json: dml-rozstrel-2023-01-15-4] | 1 otázka z 15. 1. 2023. |
| Distribúcia cez spojky | „Které logické důsledky platí“ | [testy-rozstrel.md:22; sources.json: dml-rozstrel-2023-01-15-6] | 1 otázka so 4 tvrdeniami. |
| Ekvivalencie, negácia a poradie kvantifikátorov | „Které z dvojic formulí jsou logicky ekvivalentní“ | [testy-rozstrel.md:32; sources.json: dml-rozstrel-2026-01-29-6] | 1 otázka; tretia dvojica je v prepise neistá. |
| Základné slovné preklady | „Jsou zde jen studenti.“ | [cv-03-predikatova-logika.woo:21] | 9 viet, každá s negáciou a spätným prekladom. |
| Aritmetická formalizácia | „Součet každých dvou lichých čísel je sudý.“ | [cv-03-predikatova-logika.woo:148] | 6 viet; navyše termy a atómy. |
| Formalizácia tvrdení o funkcii | „Na každém uzavřeném intervalu funkce $f$ nabývá maxima.“ | [cv-03-predikatova-logika.woo:466] | 2 zadania; druhé o koreňoch kvadratickej rovnice. |
| Presný počet objektov | „Na stole leží právě dvě knihy.“ | [cv-03-predikatova-logika.woo:592] | 6 viet o aspoň/práve/najviac jednej či dvoch knihách. |
| Preklady a vzťahy všetkých dvojíc | „Každý býložravec před někým utíká nebo se před někým schovává.“ | [cv-03-predikatova-logika.woo:682] | 5 viet, úpravy a následné porovnanie dvojíc. |
| Dôsledky medzi $A,B,C$ | „pro každou dvojici ... vyšetřete ... logický důsledek“ | [cv-03-predikatova-logika.woo:803] | 3 formule, skúmajú sa smery dôsledkov. |

Súhrn overeného rozstrelového výberu: jadro PL sa objavuje v 4 otázkach z 24 otázok troch uvedených rozstrelov; rozloženie je 1 + 2 + 1.
Otázka 2026 o antisymetrii používa PL v kontexte relácií, ale nepočítam ju ako samostatnú úlohu jadra tejto kapitoly. [testy-rozstrel.md:7,20,22,27,32]
Položky `dml-t2-2022-v1-5`, `dml-t2-2022-v2-5`, `dml-t2-2021-1129-1`, `dml-t2-2021-1203rano-1` patria znením k VL, hoci inventár uvádza PL.
Podobne `dml-rozstrel-2023-01-11-6` a `dml-rozstrel-2026-01-29-4` skúšajú VL. [testy-dml-2.md:49,56,66,70; testy-rozstrel.md:12,30]
Samostatný úplný korpus ústnych skúšok PL zdroj nemá.
MARAST: prednášky dokladajú existenciu tréningových úloh na tautológie, dôsledky a ekvivalencie; ich otvorené kvízové znenia ani parafrázy tu nie sú. [dml-P3-par1-dombek 11:30–12:52,25:48–26:15]

## 8. Časté chyby a otázky (Discord, prednášky)

- **Ako dokázať ekvivalenciu?** Na Discorde sa študent pýta na postup; učiteľ žiada reťazec ekvivalencií a zdôvodnenie každej vety. Zadanie bolo v neprečítanom obrázku, konkrétnu formulu zdroj nemá. [discord-dml.md:30–35]
- **Domyslené zátvorky zmenia dosah.** Dombek to výslovne spája s ťažkosťami pri písomkách; čítať skutočný zápis, nie širšiu formulu, ktorú si človek predstaví. [dml-P2-par1-dombek 51:55–53:25]
- **Negácia zostane vonku alebo sa stratí.** Zmeniť kvantifikátor nestačí: negácia musí pokračovať do tela a nakoniec k atómom. [dml-P3-par1-dombek 36:14–36:17; dml-P3-par2-spevak 53:04–53:28]
- **Každý nie je to isté ako nikto.** „Žádná kočka není černá“ nemá byť nahradené nejednoznačnou vetou o tom, že všetky kočky nie sú čierne. [dml-P3-par2-spevak 1:03:24–1:04:54]
- **Dva existenčné svedky nemusia byť ten istý.** Z existencie účastníka MO a účastníka FO neplynie existencia účastníka oboch. [dml-P3-par1-dombek 53:06–53:49]
- **Rovnaké písmeno v nezávislých podformulách nie je spoločný svedok.** Viazané $x$ v dvoch kvantifikovaných častiach možno samostatne premenovať. [dml-P3-par1-dombek 53:55–55:39]
- **Poradie zmiešaných kvantifikátorov nie je kozmetika.** Každého môže poznať niekto iný; nemusí existovať jeden človek, ktorý pozná všetkých. [dml-P3-par2-spevak 1:15:37–1:16:22]
- **Dosadzovanie kontextu aj do viazaných výskytov je nesprávne.** Dombek ukazuje, že sa nemá mechanicky nahradiť aj meno premennej pod kvantifikátorom. [dml-P2-par1-dombek 1:19:14–1:20:50]
- **Jedna vhodná interpretácia nedokazuje všeobecnú platnosť.** Konkrétny slovný príklad je pomôcka na intuíciu, nie nepriestrelná metóda dôkazu. [dml-P3-par1-dombek 32:55–33:25]
- **„Len“ obracia smer podmienky.** Riešenia viet o študentoch rozlišujú všetci študenti sú tu a sú tu iba študenti. [cv-03-predikatova-logika.woo:21–99]
- **Práve dve vyžaduje rôznosť a vylúčenie ďalších.** Samotné dve premenné ani dva kvantifikátory tento význam nezaručia. [cv-03-predikatova-logika.woo:632–676]
- Študent na Discorde predmetu odporúča kontrolovať okrajové prípady a ovládať prehadzovanie kvantifikátorov; je to osobná skúsenosť, nie hodnotiaca smernica učiteľa. [discord-dml.md:60–65]

## 9. Rozdiely medzi paralelkami

- **Dombek:** viac rozpráva o oddelení syntaxe a príbehu, explicitne rozoberá zátvorky a kontext; formačný strom vysvetľuje ako pomôcku. Tvrdí, že si nevybavuje požiadavku kresliť ho na hodnotenej úlohe, čo nie je univerzálny zákaz takej úlohy. [dml-P2-par1-dombek 18:19–20:28,51:55–55:26]
- **Spěvák:** systematicky prechádza vyhodnocovanie od atómov a štyri základné zručnosti; vysvetľuje, čo presne znamená pokyn „znegujte“. Na konci výslovne odlišuje spojky od vzťahov medzi formulami. [dml-P3-par2-spevak 32:19–39:49,51:41–53:36,1:33:24–1:35:20]
- **Legerský:** P3 najprv dokončuje VL, preto PL rozkladá cez P3 a P4; zdôrazňuje metódu formalizovať, negovať a znovu preložiť. Pri pravdivosti pripúšťa pohľad od vonkajšej konštrukcie aj od atómov. [dml-P3-par3-legersky 25:57,1:31:46–1:32:52; dml-P4-par3-legersky 28:44–29:14,50:10]
- Prázdniny/Vianoce, čaj/káva, guláš/sviečková a MO/FO sú spoločné ilustrácie zo slajdov, nie nezávislé postupy jednotlivých paraleliek. [P02_log_predikatova.tex:1305–1350; dml-P3-par1-dombek 50:22–53:49; dml-P3-par2-spevak 1:09:55–1:13:15; dml-P4-par3-legersky 1:01:41–1:06:10]
- Všetci rozlišujú čistú PL od neskoršej skratky s obmedzenými kvantifikátormi; rozdiel v dovolenosti medzi paralelkami sa nepotvrdil. [dml-P3-par1-dombek 1:07:24–1:07:49; dml-P3-par2-spevak 1:32:54; dml-P4-par3-legersky 1:23:26–1:23:41]

## 10. Nejasné a chýbajúce

- **Chýbajú titulky proseminára 25/26-03.** Metódu ani výroky jeho vyučujúceho nemožno overiť.
- **Titulky nezachytávajú tabuľu.** Deiktické „tohle“ a ukazovanie zátvoriek nepostačujú na presný prepis formuly; tam vychádzam z doloženého skriptového/slajdového tvaru, nie z rekonštrukcie obrazu.
- **Spěvák 1:06:41–1:06:54:** (nejasné: titulky hovoria „Někdo nemá někoho rád“, hoci predchádzajúci výklad zodpovedá $\exists x\forall y\neg r(x,y)$). Správny slovný význam overený u Dombeka je niekto nemá nikoho rád. [dml-P3-par2-spevak 1:06:18–1:06:54; dml-P3-par1-dombek 38:42–38:58]
- **Cvičenie o „pouze píšící studenti“ pripúšťa dve čítania.** Riešenie ich samo rozlišuje; nevynucovať jedinú formalizáciu bez vysvetlenia významu. Druhý zápis má v zdrojovom Woo podozrivé zátvorky, jeho význam je však sprevádzaný slovným opisom. [cv-03-predikatova-logika.woo:126–145]
- **Inventár má nesprávne kapitoly VL/PL a posun názvu Dombekovej nahrávky.** Id `dml-lecture-dombek-p4-*` odkazujú na záznam `3674210634`, ktorého lokálny súbor je P3; citácie výťahu sa riadia názvom lokálnych titulkov a časom.
- **Číslovanie:** zoznam definícií končí 3.10; zmienka o rozsahu 6.1–6.11 v komentári TSV nie je použiteľné číslovanie tejto kapitoly. Príklady a poznámky má TSV označené ako neoverené; preto im nepriraďujem autoritatívne čísla.
- **Slajdový súbor zadania je obal.** `DML_P02_log_predikatova.tex:10` vkladá `P02_log_predikatova`; vecné citácie smerujú na tento vložený `.tex`, nie na neexistujúci obsah obalu.
- **Věty 3.7–3.8:** Anki ich má, no samostatný prednáškový dôkaz v určených úsekoch zdroj nemá; nepovinnosť Zajímavostí je doložená slajdom, nie domyslená podľa názvu. [anki-dml.txt:73–74; P02_log_predikatova.tex:1273]
- **Testové dáta sú výber.** Znenie 2026 má neisté miesta, chýbajú fotky; test z 1. 12. 2021 je spomienka. Nemožno z nich určiť úplnú frekvenciu alebo dnešnú hodnotiacu rubriku. [testy-rozstrel.md:26–32; testy-dml-2.md:68–69]
- **Discord je neúplný výskum.** Neobsahuje načítaný obrázok formuly pri otázke na dôkaz ani priame odkazy na jednotlivé správy; tvrdenia o neprítomnosti ďalších otázok by neboli podložené. [discord-dml.md:5–26,34–35]
- Istota je vysoká pri definíciách, zákonoch a priamych krátkych citátoch; obmedzená pri úplnosti odprednášaného obsahu a pri rekonštruovaných historických testoch. Obraz a zvuk nahrávok sa tu samostatne nekontrolovali.

## 11. Surové zdroje

Cesty nižšie sú voči koreňom podkladov kurzu (kurz/README.md, mimo repozitára).

- `kontext/BRIEF.md`: pravidlá, citácie a osnova.
- `kontext/INDEX.md`: riadky nahrávok DML P2–P4 a proseminára 2526-03.
- `kontext/cisla/dml-kap3.tsv`: celé, autorita pre čísla podľa druhu.
- `zdroje/src/bi-dml-master/src/textbook/chap-03-predikatova-logika.woo`: sekcie a rozsahy v časti 1; dôraz na 159–561,749–984,1037–1327,1328–1532,1576–2171,2179–2359.
- `zdroje/src/bi-dml-master/src/textbook/cv-03-predikatova-logika.woo`: 21–145,148–189,466–489,592–676,682–873.
- `zdroje/src/bi-dml-master/src/textbook/chap-04-dukazy-indukce.woo`: 115–180, iba presah k obmedzeným kvantifikátorom.
- `kontext/skripta-dml/bi-dml-master/src/slides/DML_P02_log_predikatova.tex`: obal, vkladanie obsahu na riadku 10.
- `kontext/skripta-dml/bi-dml-master/src/slides/P02_log_predikatova.tex`: 126–253,628–814,899–1009,1087–1388.
- `kontext/anki-dml.txt`: 52–74, celá sekcia Predikátová logika.
- `kontext/txt/dml-P2-par1-dombek.txt`: 07:04–1:37:54.
- `kontext/txt/dml-P3-par1-dombek.txt`: 06:32–59:41 a 1:05:34–1:11:51.
- `kontext/txt/dml-P2-par2-spevak.txt`: 1:20:03–1:32:28.
- `kontext/txt/dml-P3-par2-spevak.txt`: 07:18–1:16:31 a 1:21:27–1:35:20.
- `kontext/txt/dml-P3-par3-legersky.txt`: 25:57–1:34:49.
- `kontext/txt/dml-P4-par3-legersky.txt`: 06:32–1:11:10 a 1:20:32–1:29:49; INDEX označuje celý prepis partial.
- `zdroje/testy-rozstrel.md`: 7,20,22,32, s kontrolou okolitých otázok a neistôt.
- `zdroje/testy-dml-2.md`: 44–70, kontrola formálnej PL aj chybne zaradených položiek.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`: filter `course=dml`, `chapter=PL`; identifikátory konkrétnych úloh v časti 7.
- `zdroje/discord-dml.md`: 5–35 a 60–65.

Kontrola pred odovzdaním: náhodná vzorka najmenej 10 citácií opätovne porovnaná s lokálnymi riadkami a časmi; nejasné prepisy sú vyššie označené.

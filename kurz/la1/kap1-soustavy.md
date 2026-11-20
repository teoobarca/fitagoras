# BI-LA1 kap. 1: Soustavy lineárních rovnic

Balík kapitoly: skriptá `chap-02.woo`, slajdy 1, kód rozvrhu S1.

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav lokálnych podkladov overený 17. 11. 2026; nejde o overenie aktuálneho webu predmetu.
Číslovanie výhradne podľa `cisla/la1-kap1.tsv`, text podľa `chap-02.woo`.

- Najjednoduchšia rovnica a grupa: Věta 1.1 až Pozorování 1.6, `chap-02.woo:86–423`.
- Modulárna aritmetika: Lemma 1.7 až Věta 1.11, `chap-02.woo:424–633`.
- Teleso a geometrická motivácia: Definice 1.12 až Poznámka 1.16, `chap-02.woo:634–951`.
- Definícia SLR a ekvivalentné úpravy: Definice 1.17 až Věta 1.19, `chap-02.woo:952–1199`.
- Matice, operácie a súčin: Definice 1.20 až Věta 1.36, `chap-02.woo:1200–1881`.
- Maticový zápis a štruktúra riešenia: Definice 1.37 až Věta 1.44, `chap-02.woo:1882–2248`.
- Ľahko riešiteľné sústavy, HST, riešiteľnosť: Pozorování 1.45 až Věta 1.53, `chap-02.woo:2249–2830`.
- GEM: Definice 1.54 až Příklad 1.57, `chap-02.woo:2831–3136`.
- Dodatok „Zajímavosti“, ďalšie telesá a strojové čísla: od `chap-02.woo:3137`; bez ďalšieho číslovaného tvrdenia.

Použiteľné obsahové úseky nahrávok; úvodné slučky titulkov a záverečné rozhovory nepočítam ako výklad:

| Paralelka | P2 | P3 a hranica kapitoly |
|---|---|---|
| Kleprlík | `la1-P2-par1-kleprlik` 13:15–1:35:35 | `la1-P3-par1-kleprlik` 07:05–1:20:55, potom záverečná rada k štúdiu do 1:22:56; od 1:23:14 motivácia vektorových priestorov |
| Klouda | `la1-P2-par2-klouda` 06:48–1:34:56 | `la1-P3-par2-klouda` 08:31–1:36:12; prvú prezentáciu dokončí až na konci, kapitolu 2 nezačne |
| Krásenský | `la1-P2-par3-krasensky` 06:29–1:35:47 | `la1-P3-par3-krasensky` 05:42–1:36:02; kapitolu 2 odloží na ďalšiu hodinu |

Hranice vyplývajú z výrokov o dokončení prezentácie a ďalšej téme, nie iba z orientačného INDEXu.
[la1-P3-par1-kleprlik 1:20:55; 1:23:14] [la1-P3-par2-klouda 1:36:12] [la1-P3-par3-krasensky 1:02:25; 1:35:32]

Proseminár `la1-prosem2223-01`: **titulky chýbajú**. INDEX uvádza tému „matice, soustavy rovnic, konečná tělesa“, ale `cues = 0`; stav `done` neznamená dostupný prepis. Neprisudzujem mu preto výroky ani časy. [INDEX.md, riadok la1-prosem2223-01]
P1 nie sú nahrané podľa zadania. P2 už grupu, teleso a modulo opakujú ako prebrané; pôvodný výklad a dôkazy z P1 nemožno rekonštruovať. [BRIEF.md, Zdroje] [la1-P2-par2-klouda 06:54–07:43]
Jadro kapitoly je do konca P3 pokryté; podrobný beh celého GEM príkladu 1.57 sa v týchto prednáškach neukazuje porovnateľne podrobne ako v skriptách.
Samostatný súvislý výklad dodatku o konštrukcii ďalších konečných telies zdroj nemá; skriptá ho označujú za nevyžadovanú zaujímavosť. [chap-02.woo:3137–3164]

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **Potrebujeme prostredie, v ktorom sa dá deliť nenulovým prvkom.** Grupa vysvetlila jednu rovnicu; teleso zjednotí sčítanie a násobenie, aby rovnaký postup fungoval nad $\mathbb Q,\mathbb R,\mathbb C,\mathbb Z_p$ (Definice 1.12). [la1-P2-par1-kleprlik 13:35–15:53]
2. **Sústava znamená spoločné splnenie všetkých rovníc.** V $\mathbb R^3$ ide o prienik rovín, ktorý môže byť prázdny, bod, priamka či rovina; geometria motivuje tri možnosti počtu riešení. [la1-P2-par2-klouda 14:18–16:41; 24:27–29:34]
3. **Zafixujeme počet rovníc, neznámych a teleso.** Definice 1.17 odlišuje jednotlivé riešenie, usporiadanú $n$-ticu, od množiny $S$; homogénna sústava má nulovú pravú stranu a vždy obsahuje nulové riešenie. [la1-P2-par3-krasensky 31:06–38:38]
4. **Množinu riešení chceme zachovať.** Tri úpravy rovníc U1 až U3 podľa Věty 1.19 dovolia meniť zápis sústavy bez straty alebo pridania riešení; Klouda zdôvodňuje tretiu úpravu oboma inklúziami. [la1-P2-par2-klouda 51:42–1:00:47]
5. **Matica odstráni opakované písanie premenných.** Pozícia koeficientu nesie jeho význam, preto treba písať aj nuly a dodržať poradie stĺpcov; Definice 1.20 potom zavádza maticu aj nezávisle od sústavy. [la1-P2-par1-kleprlik 31:02–34:26] [la1-P2-par3-krasensky 50:23–55:56]
6. **Operácie s maticami pripravia krátky zápis $\mathbf A\mathbf x=\mathbf b$.** Súčin je riadok krát stĺpec, nie násobenie po zložkách; práve tak vytvorí ľavé strany rovníc (Definice 1.31 a 1.39). [la1-P2-par1-kleprlik 1:12:14–1:14:22; 1:30:20–1:34:33]
7. **Maticový zápis skráti dôkazy.** Věta 1.40 opisuje súčet a násobky homogénnych riešení aj rozdiel dvoch nehomogénnych riešení; nejde len o názorné obrázky, ale o výpočty využívajúce distributivitu. [la1-P3-par1-kleprlik 18:35–21:37; 24:25–29:57]
8. **Celé riešenie je posunuté homogénne riešenie.** Věta 1.44 dáva $S=\tilde{\mathbf x}+S_0$, ak už máme jedno riešenie; úplný opis $S_0$ bude motivovať ďalšiu kapitolu. [la1-P3-par3-krasensky 1:00:19–1:02:25]
9. **Najprv sa naučíme rozpoznať výsledok eliminácie.** Horný trojuholníkový tvar vedie na spätné dosadzovanie; všeobecnejší HST (Definice 1.47) pripúšťa vedľajšie stĺpce a nulové riadky. [la1-P3-par1-kleprlik 43:45–46:18; 55:06–1:00:51]
10. **Hlavné stĺpce rozhodnú a GEM nás k nim dostane.** Věta 1.53 rozlíši žiadne, jediné a viaceré riešenia; Definice 1.54, Věta 1.55 a Postup 1.56 zabezpečia prechod ľubovoľnej rozšírenej matice do HST. [la1-P3-par1-kleprlik 1:03:43–1:20:55] [la1-P3-par2-klouda 1:32:38–1:35:59]

Poradie jadra je spoločné. Kleprlík je v P2 približne o tri slajdy ďalej; Klouda v P3 ešte začína súčinom, Krásenský opakovaním operácií a potom vektormi. [la1-P3-par2-klouda 20:41–21:30] [la1-P3-par3-krasensky 06:07–09:38]

## 3. Pojmy a tvrdenia

„Definícia, bez dôkazu“ neznamená, že sa má dokazovať. Dôležitosť je doložená konkrétnou úlohou alebo požiadavkou, nie odhadom frekvencie ostrých testov.

| Číslo | Druh | Názov | Jadro jednou vetou | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 1.1 | Věta | O jedné rovnici jedné neznámé v $\mathbb R$ | Pre $a\ne0$ má $ax=b$ jediné riešenie $a^{-1}b$. | Áno, od :142; použitie pri dosadzovaní Klouda P2 19:45–24:27, pôvodný dôkaz v P1 chýba. | Základ GEM a dosadzovania, [la1-marast-13690]. |
| 1.2 | Definice | Grupa | Asociatívna binárna operácia na neprázdnej množine má neutrálny prvok a inverziu každého prvku. | Definícia, bez dôkazu; P2 len opakovanie, [la1-P2-par3-krasensky 06:29–07:38]. | Výslovne [classification.adoc:71]; protipríklad negrupy v uvedenom úseku. |
| 1.4 | Věta | O jedné rovnici jedné neznámé v grupě | $a\circ x=b$ má jediné riešenie $a^{-1}\circ b$. | Áno, :328; pôvodný dôkaz na prednáške zdroj nemá. | Zdôvodňuje inverzné úpravy; [la1-P2-par2-klouda 20:02–22:27]. |
| 1.7 | Lemma | O asociativitě operací modulo | Súčet a súčin modulo sú asociatívne a komutatívne a spĺňajú distributivitu. | Nie, výslovne preskočené v skriptách :484; pôvodný prednáškový dôkaz zdroj nemá. | Pravidlá počítania v [la1-marast-13229] a modulo GEM. |
| 1.9 | Věta | O sčítání modulo $n$ a grupě | $(\mathbb Z_n,+_n)$ je grupa. | Nie samostatný dôkaz, záver predchádzajúceho výkladu :532; P2 opakuje výsledok. | Modulo požadované v [classification.adoc:70], [la1-marast-13229]. |
| 1.10 | Lemma | O existenci inverzí v $\mathbb Z_n$ | Nenulové $m$ je invertibilné práve pri $\gcd(m,n)=1$. | Nie, výslovne odložené do DML, :592; dôkaz v zadaných prednáškach zdroj nemá. | Rozlíšenie telesa a netelesa, [la1-marast-13229], [la1-marast-13384]. |
| 1.11 | Věta | O násobení modulo $p$ a grupě | Pre prvočíslo $p$ je $(\mathbb Z_p\setminus\{0\},\cdot_p)$ grupa. | Nie samostatný dôkaz, dôsledok výkladu a lemmy; P2 rekapitulácia, [la1-P2-par3-krasensky 11:31]. | Podmienka na delenie v modulo GEM, [la1-marast-13234]. |
| 1.12 | Definice | Těleso | Dve abelovské grupy pre sčítanie a nenulové násobenie spájajú distributívne zákony. | Definícia, bez dôkazu; rozbalenie axióm Krásenský P2 07:40–09:48. | [classification.adoc:70–72], [la1-marast-13229]. |
| 1.17 | Definice | Soustava lineárních rovnic (SLR) | $m$ lineárnych rovníc pre $n$ neznámych nad jedným telesom má riešenia v $T^n$. | Definícia, bez dôkazu; [la1-P2-par2-klouda 37:20–43:44]. | Prepis sústavy oboma smermi, [classification.adoc:65]. |
| 1.19 | Věta | O základních operacích se SLR | U1 až U3 zachovávajú celú množinu riešení. | Áno, :1156; Klouda P2 53:33–1:00:47 rozoberá najmä U3. | Zdôvodnenie metódy v [la1-marast-13690]. |
| 1.20 | Definice | Matice | Matica typu $m\times n$ je tabuľka prvkov $T$ s $m$ riadkami a $n$ stĺpcami. | Definícia, bez dôkazu; [la1-P2-par3-krasensky 44:44–48:57]. | [classification.adoc:63;71], [la1-marast-16358]. |
| 1.25 | Definice | Řádek/sloupec matice | $\mathbf A_{i:}$ je celý riadok, $\mathbf A_{:j}$ celý stĺpec. | Definícia, bez dôkazu; [la1-P2-par1-kleprlik 42:09–44:06]. | Potrebné na zrozumiteľný zápis GEM a súčinu, [la1-marast-13224]. |
| 1.26 | Definice | Násobek matice a sčítání matic | Rovnaké rozmery a teleso umožnia sčítať po zložkách; skalár násobí každú zložku. | Definícia, bez dôkazu; Kleprlík P2 46:18–53:24 píše formálny predpis. | Priamo definovať v [la1-marast-16359:a]. |
| 1.28 | Poznámka | O zakladních vlastnostech násobku/součtu matic | Súčet je komutatívny a asociatívny, skalár a súčet spĺňajú uvedené distributívne vlastnosti. | Čiastočne áno, :1540, prvá časť bodu 3; ostatné cvičenie. Krásenský P2 1:07:05–1:11:52 formuluje predpoklady. | Priamo komutativita v [la1-marast-16359:b]. |
| 1.29 | Definice | Transpozice matice | $\mathbf A^T\in T^{n,m}$ má $(\mathbf A^T)_{ji}=a_{ij}$. | Definícia, bez dôkazu; [la1-P2-par3-krasensky 1:17:03–1:18:50]. | [la1-marast-13231:c], [la1-marast-16358]. |
| 1.31 | Definice | Součin matic | Pri $\mathbf A\in T^{m,n}$, $\mathbf B\in T^{n,p}$ je $(\mathbf A\mathbf B)_{ij}=\sum_{k=1}^n a_{ik}b_{kj}$. | Definícia, bez dôkazu; [la1-P2-par2-klouda 1:27:12–1:34:38]. | Samostatný početný typ, [la1-marast-16358], [la1-marast-13196]. |
| 1.35 | Věta | O asociativitě maticového násobení | $\mathbf A(\mathbf B\mathbf C)=(\mathbf A\mathbf B)\mathbf C$ pri nadväzujúcich rozmeroch. | Áno, :1815; na prednáškach sa nerozpisuje, Kleprlík P2 1:19:58–1:21:40, Klouda P3 16:44–17:45, Krásenský P2 1:34:48–1:35:10. | Používa sa pri maticových výrazoch, [la1-marast-13196:g]. |
| 1.36 | Věta | O vlastnostech maticového násobení | Platí ľavá i pravá distributivita, presun skalára a $(\mathbf A\mathbf B)^T=\mathbf B^T\mathbf A^T$. | Nie, skriptá :1878 nechávajú dôkazy čitateľovi; Klouda P3 18:05–20:41 len výklad. | Formulovanie s predpokladmi, [la1-P3-par3-krasensky 08:14–09:16]; [la1-marast-16358]. |
| 1.37 | Definice | Vektory a nulová matice | Vektor je jednostĺpcová matica, $\theta$ nulový vektor, $\Theta$ nulová matica. | Definícia, bez dôkazu; [la1-P3-par2-klouda 21:30–23:42]. | Správny typ výsledku a zápis $S$, [la1-marast-13233]. |
| 1.39 | Definice | Maticový zápis SLR | $\mathbf A\mathbf x=\mathbf b$ a $(\mathbf A\mid\mathbf b)$ reprezentujú tú istú sústavu; pridružená má pravú stranu $\theta$. | Definícia a rozpis súčinu; [la1-P3-par3-krasensky 18:28–26:42]. | [classification.adoc:65], [la1-marast-13690]. |
| 1.40 | Věta | O vlastnostech SLR | $S_0$ je uzavretá na súčet a skalár, rozdiel riešení z $S$ je v $S_0$ a pripočítanie $S_0$ zachová $S$. | Áno, :2101; Kleprlík P3 13:17–33:18; Klouda P3 37:20–45:37 a 54:11–55:06; Krásenský P3 37:12–50:00. | Jednoduché dôkazy výslovne možné v písomke, [la1-P3-par3-krasensky 37:12]. |
| 1.41 | Důsledek | O počtu řešení soustavy nad $\mathbb R$ | Nad $\mathbb Q,\mathbb R,\mathbb C$ dve rôzne riešenia znamenajú nekonečne veľa riešení. | Áno, :2157; Kleprlík P3 33:19–33:49 vysvetľuje konštrukciu. | Správna interpretácia HST, [la1-marast-13387]. |
| 1.42 | Definice | Součet vektoru a množiny | $\mathbf x+A=\{\mathbf x+\mathbf a\mid\mathbf a\in A\}$. | Definícia, bez dôkazu; [la1-P3-par1-kleprlik 34:00–36:47]. | Zápis riešení v [la1-marast-13235], [la1-marast-13203]. |
| 1.44 | Věta | O struktuře řešení SLR | Ak $\tilde{\mathbf x}\in S$, potom $S=\tilde{\mathbf x}+S_0$. | Áno, :2242 z dvoch inklúzií; Krásenský P3 1:01:55; Kleprlík P3 42:50 len odkazuje na 1.40. | Vzorový postup v [la1-marast-13203]. |
| 1.45 | Pozorování | O SLR s horní trojúhelníkovou maticí | Štvorcová horná trojuholníková matica bez núl na diagonále dáva jediné riešenie. | Skriptá zdôvodňujú spätným dosadzovaním; Krásenský P3 1:12:21–1:13:21 náčrt, nie plná indukcia. | [la1-marast-13233], podklad Věty 1.53. |
| 1.47 | Definice | Horní stupňovitý tvar | Nulové riadky sú dole a prvé nenulové prvky sa v ďalších riadkoch posúvajú striktne doprava. | Definícia, bez dôkazu; Kleprlík P3 55:06–1:01:14, Krásenský P3 1:20:40–1:27:49. | Rozpoznanie a hlavné stĺpce, [la1-lecture-krasensky-p3-4879]. |
| 1.53 | Věta | O řešitelnosti soustavy s maticí v HST | Posledný stĺpec hlavný znamená spor; jediný vedľajší jediné riešenie; ďalší vedľajší viac riešení. | Áno, :2754; Kleprlík P3 1:09:18 dôkaz vynechá, Krásenský P3 1:30:25–1:33:38 zdôvodní prípady len náčrtom. | Kľúčový záver po GEM, [la1-marast-13690], [la1-marast-13234]. |
| 1.54 | Definice | Operace GEM | G1 mení poradie riadkov, G2 nenulovo škáluje riadok a G3 pripočíta násobok iného riadka. | Definícia, bez dôkazu; [la1-P3-par1-kleprlik 1:10:22–1:12:14]. | Samotný výpočet, [la1-marast-13224]. |
| 1.55 | Věta | O vlivu GEM na řešení SLR | G1 až G3 na rozšírenej matici nemenia množinu riešení. | Áno odkazom na 1.19, :2890; Kleprlík P3 1:12:24–1:13:22 slovne, Krásenský P3 1:34:09–1:34:37 rovnako. | Zdôvodnenie celého postupu, [la1-marast-13690]. |
| 1.56 | Postup | Gaussova eliminace (GEM) | Zľava vyberieme nenulový začiatok a eliminujeme pod ním, potom pokračujeme v menšom bloku. | Skriptá odvodzujú elimináciu :2900–2928 a dávajú algoritmus; Kleprlík P3 1:14:20–1:20:50 ho vysvetľuje, nie formálny dôkaz korektnosti. | [classification.adoc:69], [la1-marast-13224], [la1-marast-13690]. |

Odkazy `chap-02.woo:…` v ďalších sekciách používajú tú istú overenú tabuľku čísel.

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Počítanie v $\mathbb Z_p$ a riešenie jednej rovnice**

1. Najprv uveď teleso: $\mathbb Z_p=\{0,1,\ldots,p-1\}$, $p$ prvočíslo.
2. Súčet a súčin redukuj modulo $p$; aj záporné číslo nahraď zvyškom v tomto rozsahu.
3. Odčítanie je pripočítanie opačného prvku; delenie nenulovým $a$ je násobenie $a^{-1}$, ktoré spĺňa $a\cdot_p a^{-1}=1$.
4. Inverziu pri malom module možno nájsť overením súčinov; netvrdiť, že každé nenulové číslo má inverziu aj pri zloženom module.
[chap-02.woo:435–462] [Lemma 1.10, chap-02.woo:577] [Věta 1.11, chap-02.woo:617]

Kurzový príklad: v $\mathbb Z_5$ je $4^{-1}=4$, preto $3\cdot2+(4+4)\cdot4+4^{-1}\cdot3=0$.
V $\mathbb Z_6$ ten istý výraz nemožno vyhodnotiť, lebo $4^{-1}$ neexistuje. [la1-marast-13229]
Pri $3x+4=2+4x$ v $\mathbb Z_5$ vyjde $x=2$; operácie sú v tom istom telese aj na pravej strane. [la1-marast-13230]
Prednášky P2 modulo hlavne pripomínajú; podrobný pôvodný postup z P1 a postup proseminára zdroj nemá. [la1-P2-par2-klouda 07:38–07:59]

**B. Prepis sústavy do rozšírenej matice**

1. Vymenuj neznáme v poradí $x_1,\ldots,x_n$ (pri troch zvyčajne $x,y,z$) a uveď ich teleso.
2. Rovnice píš po riadkoch; $j$-tý stĺpec patrí $x_j$, chýbajúci člen zapíš koeficientom $0$.
3. Pravé strany tvoria posledný stĺpec, oddelený zvislou čiarou: $(\mathbf A\mid\mathbf b)$.
4. Rozlišuj $\mathbf A\in T^{m,n}$, $\mathbf b\in T^m$, $\mathbf x\in T^n$ a rozšírenú maticu v $T^{m,n+1}$.
5. Súčin $\mathbf A\mathbf x$ po riadkoch zrekonštruuje pôvodné ľavé strany; toto je zdôvodnenie zápisu $\mathbf A\mathbf x=\mathbf b$.
[Definice 1.39, chap-02.woo:1980] [la1-P2-par1-kleprlik 31:02–34:26; 1:30:33–1:34:33]

**C. GEM a zápis jednotlivých úprav na papier**

- (G1): $\text{ř}i\leftrightarrow\text{ř}j$.
- (G2): $\text{ř}i\leftarrow\alpha\text{ř}i$, $\alpha\in T\setminus\{0\}$.
- (G3): $\text{ř}i\leftarrow\text{ř}i+\alpha\text{ř}j$, $i\ne j$, $\alpha\in T$; tu môže byť $\alpha=0$.
[Definice 1.54, chap-02.woo:2839–2887]

Skriptá píšu medzi celé matice šípku, nad ňu označenie (G1), (G2) alebo (G3), pod ňu konkrétny zásah, napríklad $\text{ř}2-\frac13\text{ř}1$.
Je to doložený vzor zápisu, nie doložená povinnosť zakaždým vypisovať číslo pravidla: Krásenský pri U-úpravách výslovne hovorí, že opis zásahu stačí na pochopenie. [Příklad 1.57, chap-02.woo:3011;3040;3065] [la1-P2-par3-krasensky 42:28–42:33]
Pri G3 sa mení cieľový riadok, zdrojový ostáva; zásah sa vykoná na **celom riadku vrátane pravej strany**. [la1-P2-par2-klouda 54:50–55:35]

1. Pracuj s $\mathbf B=(\mathbf A\mid\mathbf b)$, začni hore vľavo.
2. Ak je skúmaný stĺpec v zostávajúcom dolnom bloku nulový, presuň sa na ďalší stĺpec, nie na ďalší riadok.
3. Ak máš hore nulu a nižšie nenulu, pomocou G1 priveď nenulu na začiatok bloku.
4. Prvok $b_{j\ell}$ pod nenulovým $b_{k\ell}$ odstráň G3 s $\alpha=-b_{j\ell}(b_{k\ell})^{-1}$.
5. Hotový riadok a stĺpec nechaj bokom; pokračuj o riadok a stĺpec ďalej, kým sa riadky alebo stĺpce neminú.
6. G2 môže zjednodušiť čísla, ale algoritmus ho na dosiahnutie HST nepotrebuje; nenulový začiatok nemusí byť jednotka.
[Postup 1.56, chap-02.woo:2936–2982] [la1-P3-par1-kleprlik 1:14:20–1:20:50]

Správnosť zachovania $S$ odôvodni Větou 1.55; existenciu eliminačného násobku zaručuje inverzia nenulového prvku telesa.
V $\mathbb Z_p$ je rovnaký algoritmus, len všetky operácie vrátane $\alpha$ rátame modulo $p$. [Věta 1.55, chap-02.woo:2893] [chap-02.woo:2920–2928] [la1-P3-par3-krasensky 1:35:00–1:35:32]

Alternatívy v zdrojoch: Příklad 1.57 ukáže najprv doslovný algoritmus so zlomkami a potom výhodnejšie poradie s G1 a G2.
MARAST #13224 tiež povoľuje rozumný výber úprav, aby nevznikali zbytočne veľké koeficienty a zlomky. [chap-02.woo:3002–3094] [marast-problems/lecture-255.md:578–579]

**D. Rozpoznanie HST a rozhodnutie o riešeniach**

1. Nulová matica je v HST.
2. Pri nenulovej matici over, že všetky nulové riadky sú dole.
3. V každom nenulovom riadku označ iba **prvý** nenulový prvok; jeho stĺpcové indexy musia striktne rásť.
4. Stĺpce týchto prvkov sú hlavné, zvyšné vedľajšie; stĺpec s neskoršou nenulou sa tým nestáva hlavným.
[Definice 1.47, chap-02.woo:2529–2568] [la1-P3-par3-krasensky 1:21:12–1:25:48]

Na sústavu aplikuj tento test na **rozšírenú** maticu, nie iba na $\mathbf A$:

- Posledný stĺpec hlavný: riadok $0= c\ne0$, teda $S=\varnothing$.
- Posledný stĺpec jediný vedľajší: každá neznáma sa jednoznačne dopočíta, teda práve jedno riešenie.
- Posledný stĺpec vedľajší a aspoň jeden ďalší vedľajší: viac než jedno riešenie; nad $\mathbb Q,\mathbb R,\mathbb C$ nekonečne veľa.
[Věta 1.53, chap-02.woo:2727–2751] [Důsledek 1.41, chap-02.woo:2150] [la1-P3-par2-klouda 1:32:38–1:34:58]

Počet rovníc ani prítomnosť nulového riadka samy osebe nie sú argument. Napíš konkrétny prípad Věty 1.53, ktorý tvoja HST matica spĺňa.
[la1-P3-par1-kleprlik 1:08:17–1:09:17] [la1-P3-par3-krasensky 1:28:32–1:28:47] [discord-la1.md:396]

**E. Nájsť riešenie a správne ho zapísať**

1. Ak je riešenie jediné, postupuj od poslednej nenulovej rovnice nahor a dosadzuj už vypočítané hodnoty.
2. Pri viacerých riešeniach zvoľ hodnoty neznámych vedľajších stĺpcov $\mathbf A$, napríklad všetky $0$, a hlavné dopočítaj odzadu.
3. Druhé riešenie dostaneš inou voľbou aspoň jednej voľnej neznámej. Každé vypíš ako celú $n$-ticu v pôvodnom poradí.
4. Ak zadanie chce všetky riešenia, pomenuj voľné parametre, uveď $t,s,\ldots\in T$ a vyjadri každú zložku; samotné dve ukážky už nestačia.
[Pozorování 1.45, chap-02.woo:2368] [Věta 1.53, chap-02.woo:2771–2790] [la1-P3-par3-krasensky 1:16:34–1:19:31] [la1-marast-13200]

Rozlišuj riešenie $\mathbf x=(1,1,1)$ a množinu riešení $S=\{(1,1,1)\}$; bez riešenia píš $S=\varnothing$, nie $S=\{\theta\}$.
[la1-P2-par2-klouda 24:27–25:28] [la1-P3-par3-krasensky 1:30:14–1:30:20]

Kleprlík ilustruje parametrický zápis $S=\{(3+2t,1-t)\mid t\in\mathbb R\}$ a jeho tvar $(3,1)+\{t(2,-1)\mid t\in\mathbb R\}$.
Budúci zápis pomocou lineárneho obalu označuje ako predbehnutie ďalšej teórie. [la1-P3-par1-kleprlik 40:03–41:29]
Klouda parametre pripúšťa ako známy postup, no odkladá vhodnejší opis väčších sústav; nejde o všeobecný zákaz parametrov. [la1-P3-par2-klouda 1:22:53–1:23:48]
Krásenský pri prvej písomke trvá na **dvoch konkrétnych riešeniach**, keď ich zadanie žiada; nenahradiť ich len parametrizáciou. [la1-P3-par3-krasensky 1:16:34–1:16:46]

**F. Kurzový príklad GEM nad $\mathbb Z_5$ a úplnejšieho zápisu**

MARAST #13203 začína $\left(\begin{array}{rrrr|r}2&3&4&3&1\\3&2&0&1&1\end{array}\right)$.
G3, $\text{ř}2\leftarrow\text{ř}2+\text{ř}1$, dá $\left(\begin{array}{rrrr|r}2&3&4&3&1\\0&0&4&4&2\end{array}\right)$.
Hlavné sú stĺpce 1 a 3; vedľajšie 2, 4 a pravá strana 5, preto viac riešení podľa Věty 1.53.
Voľba $y=t=0$ dá partikulárne $(2,0,3,0)$; homogénne voľby $(y,t)=(1,0)$ a $(0,1)$ dajú $(1,1,0,0)$ a $(3,0,4,1)$.
Zdroj zapisuje $S=\{(2,0,3,0)+\alpha(1,1,0,0)+\beta(3,0,4,1)\mid\alpha,\beta\in\mathbb Z_5\}$ a uvádza 25 riešení.
Odlišnosť od reálneho prípadu je obor parametrov, nie riadkové operácie. Zdroj sám upozorňuje, že tvrdenie „viac ich nie je“ sa objasní neskôr.
[Věta 1.44, chap-02.woo:2233] [marast-problems/lecture-255.md:537–562]

**G. Jednoduché dôkazy o riešeniach**

Začni prekladom predpokladov: $\mathbf x\in S_0$ znamená $\mathbf A\mathbf x=\theta$, nie $\mathbf A\mathbf x=\mathbf b$.
Potom vypočítaj požadovanú ľavú stranu a uveď použitú vlastnosť:

- $\mathbf A(\alpha\mathbf x)=\alpha\mathbf A\mathbf x=\alpha\theta=\theta$.
- $\mathbf A(\mathbf x+\mathbf y)=\mathbf A\mathbf x+\mathbf A\mathbf y=\theta$ pre $\mathbf x,\mathbf y\in S_0$.
- $\mathbf A(\mathbf x-\mathbf y)=\mathbf b-\mathbf b=\theta$ pre $\mathbf x,\mathbf y\in S$.
- $\mathbf A(\tilde{\mathbf x}+\mathbf z)=\mathbf b+\theta=\mathbf b$ pre $\tilde{\mathbf x}\in S$, $\mathbf z\in S_0$.

Na rovnosť $S=\tilde{\mathbf x}+S_0$ treba oba smery: každý súčet je riešenie a každé riešenie sa tak dá zapísať.
[Věta 1.40, chap-02.woo:2113–2144] [Věta 1.44, chap-02.woo:2242] [la1-P3-par1-kleprlik 20:02–21:07; 28:19–29:57] [la1-P3-par3-krasensky 1:01:55]

## 5. Značenie a terminológia

- České názvy: „soustava lineárních rovnic“ (SLR), „přidružená homogenní soustava“, „rozšířená matice soustavy“, „horní stupňovitý tvar“ (HST), „hlavní/vedlejší sloupce“, „Gaussova eliminace“ (GEM). [Definice 1.17, 1.39, 1.47, 1.54]
- $T$ je teleso, $T^n$ usporiadané $n$-tice; $\mathbb Z_7$ má sedem prvkov, $\mathbb Z_5^3$ obsahuje trojice prvkov z $\mathbb Z_5$. Nezamieňať dolný index s horným exponentom. [la1-P2-par3-krasensky 15:00–16:48]
- Matice sú tučné veľké písmená $\mathbf A$, prvky malé $a_{ij}$ alebo $(\mathbf A)_{ij}$; prvý index je riadok, druhý stĺpec. Ručne sa tučnosť naznačuje zdvojenou časťou písmena. [la1-P2-par1-kleprlik 37:53–42:02]
- Píš $\mathbf A\in T^{m,n}$ alebo slovne „matice typu $m\times n$“; **nepíš $\mathbf A^{m,n}$** ako označenie rozmeru. [Varování 1.24, chap-02.woo:1316] [la1-P2-par3-krasensky 47:33–48:43]
- $\mathbf A_{i:}$ má typ $1\times n$, $\mathbf A_{:j}$ typ $m\times1$; dvojbodka znamená prebehni všetky hodnoty v danej pozícii. [Definice 1.25, chap-02.woo:1326] [la1-P2-par2-klouda 1:16:36–1:17:14]
- Vektor $\mathbf x\in T^n$ sa stotožňuje so stĺpcom v $T^{n,1}$. Riadok bez čiarok je riadková matica, nie tento stĺpec; $\mathbf x=(x_1\ \cdots\ x_n)^T$ šetrí miesto na slajde. [Poznámka 1.38, chap-02.woo:1958] [la1-P3-par3-krasensky 13:41–15:02; 23:36–24:00]
- $S$ je množina riešení pôvodnej sústavy, $S_0$ pridruženej homogénnej; kurz používa $S$, Kleprlík výslovne odlišuje možné iné označenia v iných textoch. [la1-P2-par1-kleprlik 24:24–24:49]
- $\theta$ je nulový vektor a $\Theta$ nulová matica; rozmery určuj z kontextu, pri dôkaze ich možno rozlíšiť indexom. [la1-P3-par2-klouda 23:12–23:42] [la1-P3-par1-kleprlik 16:03–16:33]
- $\hat n=\{1,2,\ldots,n\}$ je kurzová skratka pre množinu indexov. [Definice 1.46, chap-02.woo:2519] [la1-P3-par2-klouda 1:27:54–1:28:38]
- U1 až U3 pomenúvajú úpravy rovníc, G1 až G3 ich maticové verzie; GEM nie je požiadavka dostať jednotky a nuly všade. [chap-02.woo:2834–2897;2941] [bi-la1-prezentace-01.tex:873–877]
- Slovo „pivoty“ Kleprlík spomenie len ako to, čo „někdo tomu říká“; kurz ho nepoužíva a appka tiež nie (Teo 15. 11., `data/Terminology.kt`, ZASADY zásada 6). Píš prvý nenulový prvok riadku a hlavný stĺpec. [la1-P3-par1-kleprlik 59:16–1:00:37]
- Žiadny všeobecný zákaz alternatívnej skratky, napríklad GE, zdroj nemá; doložené označenie je GEM. Kloudovo vyslovené „HEST“ je výslovnosť HST v prepise. [la1-P3-par2-klouda 1:26:20]

## 6. Čo kto zdôrazňuje, chce a zakazuje

Citáty sú krátke doslovné výňatky z automatických titulkov, nie jazykovo opravený prepis.

| Kto | Čo povedal | Citácia a praktický význam |
|---|---|---|
| Klouda | „Takže je dobré si ty úpravy nějak zapisovat.“ | [la1-P2-par2-klouda 54:44]; uviesť, ktorý riadok a násobok mením. |
| Krásenský | „tak už je jasné, co dělám.“ | [la1-P2-par3-krasensky 42:33]; po opise odčítania rovníc, číslo úpravy si práve nepamätá. |
| Kleprlík | „Včetně rozměru.“ | [la1-P2-par1-kleprlik 45:18]; pri definícii maticového sčítania na prvú písomku. |
| Krásenský | „Tohleto není kompletní matematická věta, protože neříkám, za jakých předpokladů tohleto platí.“ | [la1-P2-par3-krasensky 1:07:32]; samotné $\mathbf A+\mathbf B=\mathbf B+\mathbf A$ nestačí. |
| Krásenský | „Tak tam nebude stačit, abyste napsali, pokud má aspoň jedna strana smysl“ | [la1-P2-par3-krasensky 1:11:38]; pri formulovaní vety na test treba konkrétne predpoklady a rozmery. |
| Krásenský | „Nevymýšlejte nějaké věci, že se to doplní nulama, tak aby ty rozměry seděly.“ | [la1-P2-par3-krasensky 1:24:45]; nedefinovaný súčin sa neopravuje zmenou zadania. |
| Kleprlík | „ten důkaz po vás jít nebudeme.“ | [la1-P2-par1-kleprlik 1:21:40]; prepis je neobratný, kontext výslovne vynecháva dôkaz asociativity súčinu. |
| Krásenský | „tohle můžete opravdu dostat jako důkaz klidně v písemce“ | [la1-P3-par3-krasensky 37:12]; pri násobku homogénneho riešenia, neplatí to automaticky o každom dôkaze kapitoly. |
| Kleprlík | „ale nemusíte se ji naučit formulovat.“ | [la1-P3-par1-kleprlik 57:55]; o formálnej definícii HST, treba vedieť rozpoznať tvar. |
| Krásenský | „Bude stačit, když budete umět s jistotou poznat u zadané matice, jestli je nebo není v tom horním stupňovitém tvaru.“ | [la1-P3-par3-krasensky 1:20:50]; rovnaká výnimka z memorovania definície. |
| Kleprlík | „Potřebuju, abyste s ním uměli počítat.“ | [la1-P3-par1-kleprlik 1:18:07]; o algoritme GEM, nežiada formuláciu ani implementáciu. |
| Krásenský | „Chceme dvě konkrétní řešení“ | [la1-P3-par3-krasensky 1:16:46]; ak ich žiada zadanie, výstupom majú byť dve celé rôzne $n$-tice. |
| Krásenský | „Nikoliv množina obsahující nulový vektor, ale prázdná množina.“ | [la1-P3-par3-krasensky 1:30:20]; rozlíšenie žiadneho a jediného nulového riešenia. |
| Skriptá | „Při počítání se nemusíme striktně držet algoritmu.“ | [chap-02.woo:2980]; výber poradia G1 až G3 je voľný pri zachovaní platných úprav. |
| Vzorová písomka | „Vysvětlete také, jak z výpočtu plynou závěry, které uděláte.“ | [marast-problems/lecture-286.md:27]; výsledná matica potrebuje interpretáciu počtu riešení. |

Výnimky o HST a algoritme nemožno preniesť na všetky definície: stránka predmetu žiada korektne formulovať základné pojmy aj pracovať s nimi v jednoduchom dôkaze. [classification.adoc:71–72]
Požiadavky a citáty sa vzťahujú na zachytený kurz a zdrojovú verziu; presné bodové zrážky za vynechaný komentár zdroj nemá.

## 7. Typické úlohy

Frekvencia nižšie znamená **počet doložených položiek v lokálnom súbore**, nie pravdepodobnosť na skúške.
Inventár `sources.json` má pre LA1/S1 60 záznamov: 32 MARAST, 24 prednáškových, 3 kvízové metadáta a 1 študentský opis prípravy na test.
Z 32 MARAST položiek je 29 z `lecture-255.md` a 3 z ukážkovej prvej písomky `lecture-286.md`.
Označenie `skill=la1.systems` je miestami zberná kategória aj pre telesá alebo mocniny matíc; nemožno z nej odvodzovať presný počet sústav.

| Typ | Krátky príklad zadania po česky | Odkaz | Ako často v podkladoch |
|---|---|---|---|
| Modulo a neexistujúca inverzia | „Spočítejte nejprve v $\mathbb Z_5$ a následně v $\mathbb Z_6$.“ | [la1-marast-13229] | 1 položka, dva obory výpočtu. |
| Jedna rovnica nad telesom | „V tělese $\mathbb Z_5$ vyřešte rovnici $3x+4=2+4x$.“ | [la1-marast-13230] | 1 položka. |
| Rovnica pri zloženom module | „Pro jaká $b$ ze $\mathbb Z_9$ mají následující rovnice alespoň jedno řešení?“ | [la1-marast-13384] | 1 položka, dva lineárne prípady; $\mathbb Z_9$ nie je teleso. |
| Počet koreňov mimo lineárneho prípadu | „Pro jaká $b\in\mathbb Z_5$ má rovnice $x^2+2x+b=0$ kolik řešení?“ | [la1-marast-13385] | 1 položka; tréning modulo, nie SLR. |
| Definovanosť maticových výrazov | „Rozhodněte, zda jsou následující matice $C$ a $D$ definovány.“ | [la1-marast-16358], [la1-marast-13231] | Ukážková písomka: 2 body; navyše cvičebné položky. |
| Maticové súčiny a transpozície | „Vypočtěte následující součiny a součty (jsou-li definovány).“ | [la1-marast-13196], [la1-marast-13198], [la1-marast-13190] | 3 položky, spolu 26 podúloh nad $\mathbb R$ a $\mathbb Z_5$. |
| Mocniny matíc | „Odvoďte předpis pro obecnou mocninu $\mathbf A^n$. Jeho správnost dokažte.“ | [la1-marast-13197], [la1-marast-13195], [la1-marast-13232] | 3 položky; v #13195 päť matíc, nie päť samostatných testov. |
| Sústava s jediným riešením | „Nalezněte všechna $x,y,z\in\mathbb R$ splňující soustavu rovnic.“ | [la1-marast-13233] | 1 proseminárna položka a ďalšie reálne sústavy. |
| GEM a tri možnosti počtu riešení | „Rozhodněte, zda má soustava řešení… Vysvětlete také, jak z výpočtu plynou závěry.“ | [la1-marast-13690] | Ukážková prvá písomka: 6 z 10 bodov, nie štatistika ostrých testov. |
| Súčet matíc a formulácia vety | „Napište definici součtu dvou matic včetně potřebných předpokladů.“ | [la1-marast-16359] | Ukážková písomka: 2 body, definícia aj komutativita. |
| Bez riešenia alebo viac riešení nad $\mathbb Z_3$ | „Najděte všechna řešení“ / „najděte co nejvíce řešení“. | [la1-marast-13234], [la1-marast-13235] | 2 proseminárne položky; výsledky sú prázdna množina a 3 riešenia. |
| Malé sústavy nad konečnými telesami | „Vyřešte následující soustavy rovnic.“ | [la1-marast-13201], [la1-marast-13221] | 6 prípadov v #13201 a 1 ďalšia sústava v #13221. |
| GEM s úplnejším opisom $S$ | „Vyřešte soustavu s rozšířenou maticí“ nad $\mathbb Z_5$. | [la1-marast-13203] | 1 detailné riešenie s partikulárnym a homogénnymi vektormi. |
| Reálne sústavy s ukázaným postupom | „Vyřešte v $\mathbb R$ soustavu.“ | [la1-marast-13224] až [la1-marast-13228] | 5 položiek; rôzne počty riešení a poradia úprav. |
| Homogénne a nehomogénne sústavy | „Vyřešte v $\mathbb R$ následující homogenní/nehomogenní soustavy.“ | [la1-marast-13209], [la1-marast-13223] | 2 položky, spolu 6 podúloh. |
| Geometrický význam sústavy | „Nalezněte průnik následujících dvou rovin v $\mathbb R^3$.“ | [la1-marast-13199], [la1-marast-13200] | 4 prípady v #13199 a 1 podrobný parametrický príklad #13200. |
| Nenulová štruktúra ešte neurčuje počet riešení | „Kolik řešení taková soustava může mít? Svá tvrzení zdůvodněte.“ | [la1-marast-13387], [la1-marast-13388] | 2 položky; obe pripúšťajú 0, 1 aj nekonečne veľa. |
| Parameter nad konečným telesom | „Zjistěte, pro která $a\in\mathbb Z_5$ má … soustava rovnic řešení a kolik řešení má.“ | [la1-marast-13693], [la1-discord-2021-10-27-param-zp-system] | 1 úplná cvičebná položka a 1 neúplný študentský opis. |
| Jednoduchý dôkaz o $S_0$ | „Dokázat: je-li $\mathbf x\in S_0$, pak $\alpha\mathbf x\in S_0$.“ | [la1-lecture-klouda-p3-2240], [la1-lecture-klouda-p3-2449] | Opakovane vo všetkých paralelkách, možnosť písomky doložená Krásenským P3 37:12. |
| Rozpoznať HST bez eliminácie | „Které … soustavy … mají žádné/právě jedno/více řešení?“ | [chap-02.woo:2793–2826], [la1-lecture-krasensky-p3-4879] | 6 sústav v otázke skrípt plus prednáškové príklady. |

**Hranice testových dôkazov:** v LA1/S1 filtrovanom inventári nie je žiadne zadanie z `testy-la1-2.md` ani `testy-la1-zkouska.md`; sústavy sa v ďalších kapitolách používajú ako nástroj, čo nie je ďalší samostatný test kapitoly 1.
Skúška Frobenius má neskoršiu nadväzujúcu úlohu: z HST nad $\mathbb Z_5$ opísať riešenie vo forme variety pomocou Frobeniovej vety. Je to využitie základov kapitoly 1 v ďalšej teórii, nie požiadavka vedieť variety už po P3. [testy-la1-zkouska.md:8]
`testy-rozstrel.md` je podľa hlavičky BI-DML, preto jeho otázky nepriraďujem k LA1. Konkrétne zadanie rozstřelu LA1 k tejto kapitole zdroj nemá. [testy-rozstrel.md:1–3]
Hlavička druhého LA1 testu podporuje potrebu vysvetliť vznik premenných a koeficientov, ale neprepisujem jej požiadavky automaticky na bodovanie prvého testu. [testy-la1-2.md:3]
Tri kvízy S1 sú doložené len metadátami: teória, početný kvíz kapitoly 1 a početné opakovanie pred prvým testom; lokálny záznam ich k 6. 11. označuje ako uzavreté, znenia otázok nemá. [marast.md:11–18;47–63]
Otvorené kvízy sa do výťahu ani príkladov nepreberajú; ukážková písomka lecture-286 je samostatná cvičebná lekcia, na ktorú odkazuje stránka predmetu. [classification.adoc:74]

## 8. Časté chyby a otázky (Discord, prednášky)

- **Nulový riadok ako dôkaz počtu riešení.** Vyučujúci LA1 na Discorde predmetu výslovne upozorňuje, že nulový riadok po GEM sám osebe neznamená vôbec nič. Treba preskúmať celú rozšírenú maticu a stĺpce. [discord-la1.md:394–400]
- **„Je veľa rovníc, preto sústava nemá riešenie.“** Krásenský odmieta odpoveď publika; aj pri štyroch rovniciach a troch neznámych môže po zmene konštánt riešenie existovať. [la1-P3-par3-krasensky 1:14:18–1:14:46]
- **Nejasný smer GEM s parametrom.** Študent opisuje problém práve pri $\mathbb Z_5$; lokálny Discord neobsahuje nadväzujúce riešenie. Nie je to potvrdené ostré testové zadanie. [discord-la1.md:543–549]
- **„Homogénna“ znamená iba nulové riešenie.** Krásenský upozorňuje na častý omyl: nulové riešenie je zaručené, ďalšie nie sú vylúčené. [la1-P2-par3-krasensky 37:04–38:10]
- **Presuniem konštantu doľava a sústava je homogénna.** Musia byť nulové pravé strany v štandardnom lineárnom zápise, nestačí mechanicky písať výraz rovný nule. [la1-P2-par3-krasensky 35:04–35:34]
- **Rovnica bez $x$ má o jednu neznámu menej.** V sústave pre $(x,y,z)$ aj $2y+z=3$ opisuje rovinu v $\mathbb R^3$, $x$ je voľné; nejde automaticky o priamku v $\mathbb R^2$. [la1-P2-par2-klouda 27:13–27:54]
- **Dopĺňanie núl pri nekompatibilných maticiach.** Kurz žiada oznámiť, že operácia nie je definovaná. [la1-P2-par1-kleprlik 58:25–59:40] [la1-P2-par3-krasensky 1:24:37–1:24:45]
- **G3 musí zachovať riešenie každej rovnice zvlášť.** Nemusí; zachováva spoločnú množinu riešení sústavy, geometricky prienik. [la1-P2-par2-klouda 56:00–57:12]
- **HST má dva prvé nenulové prvky pod sebou alebo nulový riadok uprostred.** Kontrolovať polohy prvých nenúl a poradie nulových riadkov. [la1-P3-par3-krasensky 1:21:12–1:23:43]
- **Neexistujúce riešenie zapísané ako $\{\theta\}$.** Prázdna množina a jednoprvková množina sú odlišné odpovede. [la1-P3-par3-krasensky 1:30:14–1:30:20]
- **Maticový súčin sa môže prehodiť ako súčin čísel.** Ani pri štvorcových maticiach všeobecne nie; „nie je komutatívny“ neznamená, že nijaké dve matice nekomutujú. [la1-P2-par3-krasensky 1:31:30–1:33:58]

Discord má k týmto témam konkrétne nálezy, ale nie reprezentatívnu frekvenciu chýb. Záver o formáte skúšky sa nesmie opierať o príbeh o štátniciach uvedený v tom istom súbore. [discord-la1.md:70–74;732–737]

## 9. Rozdiely medzi paralelkami

- **Kleprlík:** v P2 už stihne $\mathbf A\mathbf x=\mathbf b$, v P3 podrobne rozpisuje jednotlivé body Věty 1.40. HST kreslí schodmi s výškou 1 a ľubovoľnou dĺžkou, ukazuje výpočet eliminačného násobku a v P3 začne motivovať kapitolu 2. [la1-P2-par1-kleprlik 1:34:21] [la1-P3-par1-kleprlik 13:17–33:18;56:35–57:31;1:14:33–1:17:51;1:23:14]
- **Klouda:** v P2 viac času venuje geometrii, významu ekvivalencie a dôkazu U3 oboma inklúziami; v P3 opakuje súčin a na príklade s piatimi neznámymi rozoberá voľné $x_3,x_5$. Parametrizáciu porovnáva s budúcim vhodnejším opisom, celú prvú prezentáciu uzatvorí až na konci. [la1-P2-par2-klouda 53:33–1:00:47] [la1-P3-par2-klouda 08:31–12:09;1:14:07–1:23:48;1:36:12]
- **Krásenský:** v P2 dôsledne rozoberá predpoklady definícií a zakázaný zápis rozmeru, v P3 požiadavky písomky a rozdiel medzi vektorom a riadkovou maticou. HST rozpoznáva krúžkovaním prvých nenúl; GEM sám len rýchlo zhrnie a odkáže na cvičenie/proseminár. [la1-P2-par3-krasensky 47:33–48:43;1:01:15–1:11:52] [la1-P3-par3-krasensky 13:41–15:02;1:16:34–1:16:46;1:22:13–1:25:48;1:33:45–1:35:32]
- **Spoločné:** zachovať $S$, počítať nad telesom, po GEM interpretovať HST; pri prvej písomke nájsť jediné alebo aspoň dve konkrétne riešenia podľa situácie. Požiadavku potvrdzuje aj stránka predmetu a vzorová písomka. [classification.adoc:64–72] [marast-problems/lecture-286.md:27]

## 10. Nejasné a chýbajúce

- Titulky `la1-prosem2223-01` chýbajú; identitu rečníka, jeho formulácie, zákazy a časy zdroj nemá. MARAST „Příklady z prosemináře“ nahrádza zadania a niekedy riešenia, nie výklad videa.
- P1 nie sú nahrané; úvodné dôkazy o grupách, modulo a telesách vieme overiť zo skrípt, nie z pôvodného výkladu jednotlivých prednášajúcich.
- INDEX zhrňuje všetky P3 ako „slajdy 1 a 2“, ale Klouda a Krásenský podľa titulkov len dokončia prezentáciu 1; nezavádzať im fiktívny začiatok kapitoly 2. [la1-P3-par2-klouda 1:36:12] [la1-P3-par3-krasensky 1:02:25;1:35:32]
- Automatické titulky nedokladajú všetko na tabuli. Nejasné konkrétne koeficienty alebo priebežné opravy neprepisovať ako spoľahlivé vzorové výsledky; napríklad Kleprlík P3 pri eliminácii opravuje zamenené poloviny a tretiny. [la1-P3-par1-kleprlik 1:16:39–1:17:09]
- **Nejasné ASR alebo prerieknutie:** Kleprlík P3 1:12:42 obsahuje „Vynásobení řádku 0 nezmění řešení.“ Kontext 1:10:57 a definícia hovoria o nenulovom násobku; chybnú vetu nepoužiť ako pravidlo. [Definice 1.54, chap-02.woo:2858–2870]
- **Chyba presného algoritmického zápisu:** skriptá :2945 aj slajd :856 píšu pri pokračovaní „$k\le m$ nebo $\ell\le n$“, ale slovné vysvetlenie a zmysel algoritmu končia, keď sa minú riadky alebo stĺpce; pri implementácii by pokračovanie vyžadovalo obe medze súčasne. Ide o logické posúdenie zápisu, nie citovaný zákaz vyučujúceho. [chap-02.woo:2945;2977] [bi-la1-prezentace-01.tex:856]
- **Ďalšie nepresnosti slajdu:** množina $\{k+1,m\}$ v :859 nemá vnútorné indexy, kým skriptá :2955 majú $\{k+1,k+2,\ldots,m\}$; používať úplný interval. Příklad 1.57 navyše na :3047 oznamuje zastavenie skôr, než by vyplývalo z doslovného algoritmu. Neprenášať to do krokovača. [bi-la1-prezentace-01.tex:859] [chap-02.woo:2955;3047]
- **Rozpor popisu zásahu a matíc MARAST:** #13224 na :609 označuje G1 ako $\text{ř}3\leftrightarrow\text{ř}2$, ale susedné matice v skutočnosti vymenia tretí a štvrtý riadok. Toto je overiteľný nesúlad lokálneho prepisu, bez živého originálu nemožno určiť, kde vznikol. [marast-problems/lecture-255.md:604–614]
- Slajdy sú osnova, nie kompletný študijný text; Krásenský sám upozorňuje, že niektoré vlastnosti chýbajú a treba skriptá. [la1-P2-par3-krasensky 58:21–59:15]
- Dôkazy Věty 1.53 sú v skriptách úplnejšie ako ústne náčrty; Kleprlík ich výslovne nežiada. Nezamieňať to so zákazom učiť zdôvodnenie troch prípadov. [chap-02.woo:2754–2790] [la1-P3-par1-kleprlik 1:09:18]
- Kompletná história ostrých testov kapitoly 1, presné opakovanie úloh medzi rokmi, rubrika bodových zrážok a samostatný LA1 rozstřel v podkladoch chýbajú. Vzorová písomka je doklad typu a dôrazu, nie frekvencie.
- Lokálne metadáta kvízov neobsahujú ich otázky; stav k 6. 11. nemožno prezentovať ako živý stav k 17. 11. Žiadne znenie otvoreného kvízu nie je použité.
- Teove poznámky o stĺpcoch pri doplnení bázy sa týkajú neskoršej kapitoly; tu používame stĺpcový vektor neznámych a riadkové úpravy rozšírenej matice. Tieto dve veci si neprotirečia. [Definice 1.39 a 1.54]

## 11. Surové zdroje

Základné korene pre otvorenie súborov:

- `K = kontext`
- `S = zdroje`

Použité súbory a úseky:

- `K/BRIEF.md`, pravidlá, citácie a osnova; `K/INDEX.md`, LA1 P2/P3 a `la1-prosem2223-01`.
- `K/cisla/la1-kap1.tsv`, všetky čísla 1.1 až 1.57.
- `S/src/bi-la1-master/src/textbook/chap-02.woo`, sekcie :86–3136 a dodatok :3137–3180; jadro GEM :2529–3130.
- `K/skripta/bi-la1-master/src/slides/bi-la1-prezentace-01.tex`, najmä :202–242, :324–543, :544–669, :673–887.
- `K/skripta/bi-la1-master/classification.adoc:57–74`, témy prvej písomky; `study-materials/index.adoc:78–80`, priradenie cvičení a ukážkovej písomky.
- `K/txt/la1-P2-par1-kleprlik.txt`, 13:15–1:35:35; `K/txt/la1-P3-par1-kleprlik.txt`, 07:05–1:20:55, záver a prechod 1:20:55–1:23:14.
- `K/txt/la1-P2-par2-klouda.txt`, 06:48–1:34:56; `K/txt/la1-P3-par2-klouda.txt`, 08:31–1:36:29.
- `K/txt/la1-P2-par3-krasensky.txt`, 06:29–1:35:47; `K/txt/la1-P3-par3-krasensky.txt`, 05:42–1:36:02.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`, filter `course=la1`, `chapter=S1`; identifikátory použité v sekciách 3 a 7.
- `S/marast-problems/lecture-255.md:4–985`, najmä #13229 až #13235, #13203, #13224 a #13693; ide o cvičebnú lekciu.
- `S/marast-problems/lecture-286.md:1–44`, ukážková prvá písomka #16358, #13690, #16359.
- `S/marast.md:5–63`, iba prehľadové metadáta kvízov, nie otázky.
- `S/testy-la1-2.md:3`, metodická hlavička; `S/testy-la1-zkouska.md`, kontrola proti priradeniu v inventári; `S/testy-rozstrel.md:1–3`, identifikácia predmetu DML.
- `S/discord-la1.md:70–74;394–400;543–549;732–737`, chyby, parameter, limity výpovednej hodnoty.

Kontrola pred odovzdaním: najmenej 10 vybraných citácií bolo spätne porovnaných s časom a textom v lokálnych titulkoch a s riadkami skrípt/MARASTu; kontrola neznamená overenie zvuku alebo obrazu videí.

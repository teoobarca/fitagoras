# BI-LA1 kap. 4: Frobeniova věta a kompletní řešení SLR

Balík kapitoly: skriptá `chap-05.woo`, slajdy 4, kód rozvrhu S4; v appke téma `la1-k4` (`data/la1/Chapter4*.kt`).

Stav k 17. 11. 2026, rozsah a zdroje doplnené 19. 11. o 8. prednášku Kloudu (18. 11.). Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav zdrojov k 17. 11. 2026. Obsah pokrýva celú kapitolu zo skrípt, nie tvrdenie, že ju už všetky paralelky dokončili.
Čísla nižšie sú výhradne z `cisla/la1-kap4.tsv`; nepreberám historické čísla vyslovené na nahrávke.

- „Frobeniova věta“: Pozorování 4.1 až Poznámka 4.3, `chap-05.woo:40–385`.
- „Řešení SLR“, „Řešení homogenní soustavy“, „Řešení nehomogenní soustavy“: Poznámka 4.4 až Příklad 4.12, `chap-05.woo:386–812`.
- „Ověření shodností řešení SLR“: Lemma 4.13 až Příklad 4.15, `chap-05.woo:813–960`.
- „Lineární variety“, „Příklady lineárních variet“: Definice 4.16 až Příklad 4.21, `chap-05.woo:961–1325`.
- „Lineární varieta jako řešení SLR“: Poznámka 4.22 až Pozorování 4.29, `chap-05.woo:1326–1895`.
- „Parametrické a neparametrické rovnice variety“: Postup 4.30 až Příklad 4.33 a záverečná otázka, `chap-05.woo:1896–2168`.

**Paralelka 1, Kleprlík:** `la1-P8-par1-kleprlik`, **10:19–1:37:18**, teda podstatne skôr než orientačných 58 minút zo zadania.
Frobeniova veta 16:33–31:22; úplné riešenie 32:14–1:01:39; rovnosť množín 1:01:45–1:09:48; variety a vlastnosti 1:09:54–1:34:03; názvy a avízo ďalšej látky 1:34:05–1:37:18.
Pri Vete 4.18 dokazuje prvé dva body, ďalšie dva iba formuluje. [la1-P8-par1-kleprlik 1:31:22–1:34:03]

**Paralelka 2, Klouda:** `la1-P7-par2-klouda`, **1:20:16–1:35:05**.
Začiatok slajdov 4, dôkaz kritéria riešiteľnosti 1:22:08–1:30:13, potom štruktúra a intuitívne zdôvodnenie dimenzie pomocou vedľajších stĺpcov.
INDEX označuje tento prepis `partial`; posledný obsahový riadok 1:35:05 ešte obsahuje záverečné poďakovanie. Z označenia preto nevyvodzujem konkrétnu chýbajúcu vetu.
`la1-P8-par2-klouda` (18. 11., uid 3675636332, titulky overené 19. 11.), **8:41–1:31:50**, dokončil celé slajdy 4 („tím jsem skončil tady tu prezentaci“ [1:31:02]):
Frobeniova veta zopakovaná 8:41–12:13; báza $S_0$ na príklade v HST (voľné premenné, štandardná báza, „jakoukoliv bázi“, aby vyšli krajšie čísla) 12:13–24:33; rHST len spomenie ako domácu úlohu 24:33–26:05; viac zápisov jedného riešenia a kontrola rovnosti hodnosťou matice s $2k+1$ riadkami (4.14) 26:05–30:48;
varieta, vektor posunutia, zaměření, dimenzia, smerové vektory 30:48–37:50; priamka nad $\mathbb Z_5^2$ má 5 bodov („toroid“) 39:59–42:45; Věta 4.18 „si dokážte jako cvičení“ 42:45–44:59; bod, priamka, rovina, nadrovina 44:59–46:30;
**Věta 4.23 s dôkazom** cez príklad v $\mathbb Z_5^4$ 46:30–1:16:59 (krajné prípady $k=0$: $E$, $k=n$: nulová matica; riadky $A$ riešia sústavu s bázou v riadkoch, transpozícia súčinu, Frobenius dá dimenziu); Věta 4.26 a $b=Aa$ 1:17:01–1:21:00; parametrické a neparametrické rovnice 1:21:02–1:24:17; rovina cez tri body = Příklad 4.33 (len parametrický tvar) 1:25:27–1:31:02.
Dôraz: „Co je z téhle podkapitoly důležité, je umět z řešení najít soustavu… budeme to cpat do písemek, budeme to chtít, abyste to uměli dokázat.“ [1:31:02–1:31:50]; „v té zápočtový to nebude, ale ve zkouškový se to objevit může“ [1:14:28]; výpočet „jenom tohle… napíšu do řádku tu bázi… vyřeším soustavu… ty napíšu do řádků. A mám matici A.“ [1:14:57–1:15:25].
Čísla jeho príkladu v $\mathbb Z_5^4$ z titulkov spoľahlivo nečítam (ASR, tabuľa), preto ho appka neprepisuje. Ďalšia prednáška (25. 11.): determinant. [la1-P8-par2-klouda 1:31:02–1:32:18]

**Paralelka 3, Krásenský:** `la1-P8-par3-krasensky`, **1:24:17–1:36:53**.
Pred 1:24:17 ešte dokončuje regularitu. Potom pripomenie $S$, $S_0$, posun o partikulárne riešenie a podpriestor; končí dôkazom $S\ne\emptyset\Leftrightarrow h(A)=h(A\mid b)$.
Postup zostavovania bázy $S_0$ ani definíciu variety v tomto úseku ešte nevyloží. [la1-P8-par3-krasensky 1:24:17–1:36:53]

**Proseminár:** `la1-prosem2223-04`, **00:30–59:27**; od 59:57 začína determinantová časť, ktorú tento výťah vynecháva.
Dve úlohy: podmienka na pravú stranu a úplné riešenie 01:05–36:56; konštrukcia SLR k variete nad $\mathbb Z_5$ 37:14–59:27.
Meno nie je v metadátach ani v prečítanom úvode výslovne predstavené; uvádzam „proseminár“, nie neoverené autorstvo.

**Stav 19. 11.:** konštrukciu SLR k podpriestoru a variete (Vety 4.23 a 4.26 s dôkazom, Postup 4.30, Poznámka 4.31, Příklad 4.33) odprednášal Klouda 18. 11.; Varování 4.24, príklady 4.25/4.27, Důsledok 4.28 (nadrovina ako jedna rovnica spomenutá slovom [45:33–46:30]) a 4.29 len zo skrípt.
Kleprlík túto konštrukciu výslovne odkladá na nabudúce; známe stredoškolské normálové vektory použije iba ako avízo. Proseminár ju však už ukazuje. [la1-P8-par1-kleprlik 1:35:20–1:36:48; la1-prosem2223-04 42:37–59:27]
Krásenský (P8, 13. 11.) skončil pri dôkaze riešiteľnosti; jeho P9 je 20. 11. Kleprlík (P8, 10. 11.) dokončí 4.23+ na P9 24. 11. (17. 11. sviatok).

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **Už vieme nájsť riešenie, potrebujeme všetky.** Základ je $S=\tilde x+S_0$ a fakt, že $S_0$ je podpriestor (4.1); preto stačí nájsť jeho bázu. [la1-P8-par1-kleprlik 12:51–14:46; la1-P8-par3-krasensky 1:27:48–1:30:01]
2. **Rovnica je skladanie stĺpcov.** $Ax=b$ hľadá koeficienty lineárnej kombinácie stĺpcov $A$, ktorá dá $b$; otázka existencie sa zmení na členstvo v obale. [la1-P7-par2-klouda 1:21:28–1:23:58]
3. **Existenciu stlačíme na dve čísla.** Pridanie $b$ nemení obal práve vtedy, keď nezmení jeho dimenziu; pri vnorených podpriestoroch to stačí na rovnosť, transpozícia prepojí dimenziu obalu stĺpcov s hodnosťou. Tak vzniká prvá časť Vety 4.2. [la1-P8-par3-krasensky 1:33:01–1:36:49]
4. **Hodnosť určí aj počet voľných smerov.** $\dim S_0=n-h(A)$, teda presne toľko vektorov má mať báza; plná stĺpcová hodnosť znamená nulovú dimenziu $S_0$. Klouda to vysvetľuje cez počet vedľajších stĺpcov. [la1-P7-par2-klouda 1:31:33–1:35:05; la1-P8-par1-kleprlik 32:14–34:48]
5. **Bázu reálne dopočítame.** Do voľných súradníc dosadíme bázu $T^{n-h(A)}$, ostatné dopočítame; vznikne správny počet LN riešení, čiže celá báza $S_0$ (4.5). [la1-P8-par1-kleprlik 47:00–51:31]
6. **Pridáme jeden bod a máme celú množinu.** Partikulárne riešenie dostaneme ľubovoľnou voľbou voľných premenných pre nenulovú pravú stranu, potom ho spojíme s homogénnym obalom (4.10). [la1-P8-par1-kleprlik 59:24–1:01:16]
7. **Odlišný zápis nemusí byť chyba.** Iná báza aj iné partikulárne riešenie môžu opisovať tú istú množinu; Pozorování 4.14 prevedie porovnanie na hodnosť jednej matice. [la1-P8-par1-kleprlik 1:01:45–1:06:16]
8. **Tento tvar dostane meno.** Vektor plus podpriestor je lineárna varieta (4.16), jej dimenzia je dimenzia zaměření; existencia a jednoznačnosť zaměření vysvetľujú korektnosť tohto pojmu (4.18). [la1-P8-par1-kleprlik 1:10:05–1:18:57; 1:31:36–1:32:24]
9. **Obrátime smer úlohy.** „Uděláme takový zpětný chod řešení soustavy“: k zadanej množine hľadáme sústavu. Bázu zaměření do riadkov pomocnej sústavy, jej riešenia do riadkov $A$, nakoniec $b=Aa$ (4.23, 4.26, 4.30); Klouda to nazýva hlavným výsledkom kapitoly. [la1-P8-par2-klouda 37:50–39:00; 1:14:43–1:15:48; 1:19:30–1:20:50; la1-prosem2223-04 37:14–43:45; 46:48–57:01]
10. **Dva jazyky tej istej množiny.** Parametre opisujú, ako body vytvoriť, neparametrické rovnice určujú, ktoré podmienky musia splniť; prevod je riešenie SLR alebo jej spätná konštrukcia. Klouda: z neparametrických k parametrickým je „přesně úkol, který už znáte“, vyriešiť sústavu; opačne Postup 4.30. [la1-P8-par2-klouda 1:21:02–1:25:27; la1-prosem2223-04 40:40–41:53; la1-P8-par1-kleprlik 1:35:30–1:36:48]

## 3. Pojmy a tvrdenia

„Dôležitosť“ znamená doloženie v dostupnom korpuse, nie predpoveď ďalšej písomky. Postupy majú zdôvodnenie, nie samostatný formálny dôkaz.

| Číslo | Druh | Názov | Jadro | Dôkaz v skriptách; na prednáške | Dôležitosť pre test |
|---|---|---|---|---|---|
| 4.1 | Pozorování | O struktuře řešení homogenní SLR | $S_0$ je podpriestor $T^{n,1}$. | Odkaz na už dokázanú uzavretosť; Kleprlík rekapituluje [la1-P8-par1-kleprlik 13:21–13:40]. | Aj 2. test: dôkaz podpriestoru [testy-la1-2.md:6; :81]. |
| 4.2 | Věta | Frobeniova | Riešiteľnosť je rovnosť hodností, pri riešiteľnosti $S=\tilde x+S_0$, vždy $\dim S_0=n-h(A)$. | Áno, riešiteľnosť a dimenzia; posun odkazuje na skorší dôkaz. Kleprlík 21:32–31:22, Klouda 1:22:08–1:35:05, Krásenský 1:32:01–1:36:49; poslední dvaja sa líšia rozsahom. | Celé znenie 2/3 prepísaných skúšok [testy-la1-zkouska.md:8; :15]; oficiálna požiadavka [classification.adoc:184]. |
| 4.3 | Poznámka | O terminologii a reformulaci popisu $S_0$ | Partikulárne riešenie je jeden bod; $S_0$ je buď $\{\theta\}$, alebo obal LN $n-h(A)$ vektorov. | Dôsledok 4.2; Kleprlík vysvetlí [la1-P8-par1-kleprlik 32:14–34:48]. | Potrebné pre tvar odpovede [testy-la1-zkouska.md:8; :20]. |
| 4.4 | Poznámka | O řešení SLR s regulární maticí | Pri regulárnej $A$ má každá pravá strana práve jedno riešenie $A^{-1}b$. | Áno, výpočet; Kleprlík dva spôsoby vrátane skúšky dosadením [la1-P8-par1-kleprlik 35:14–43:40]. | Hraničný prípad 4.2, nie samostatne doložená úloha tejto kapitoly. |
| 4.5 | Postup | Řešení homogenní SLR | Po GEM volíme bázu voľných súradníc a dopočítame vázané. | Zdôvodnenie pred postupom; Kleprlík [la1-P8-par1-kleprlik 47:00–51:31]. | Súčasť úplného riešenia [la1-exam-2025-12-17-frobenius-2/b]. |
| 4.7 | Věta | O řešení homogenní SLR se speciální maticí | Z blokov $(E_k\ B)$ získame bázu ako riadky $(-B^T\ E_{n-k})$. | Áno; Kleprlík iba použitie, formuláciu ani dôkaz nechce [la1-P8-par1-kleprlik 53:26–57:04]. | Doplnková metóda, nie doložená povinná formulácia; cvičenie [la1-skripta-chap05-668/a]. |
| 4.10 | Postup | Řešení SLR | GEM, kontrola existencie, partikulárny bod, homogénna báza, ich súčet. | Zdôvodnenie 4.2 a 4.5; Kleprlík [la1-P8-par1-kleprlik 59:24–1:01:16]. | Priamo 2/3 prepísaných skúšok [testy-la1-zkouska.md:8; :20]. |
| 4.13 | Lemma | O přičtení vektoru z podprostoru | Ak $x\in P$, potom $x+P=P$. | Áno, obe inklúzie [Lemma 4.13, chap-05.woo:829]; samostatný dôkaz v určených prednáškach zdroj nemá. | Nástroj k 4.14 a 4.18, bez samostatného výskytu v prepísaných skúškach. |
| 4.14 | Pozorování | O rovnosti dvou množin řešení SLR | Pri dvoch LN $k$-členných súboroch testujeme spoločnú hodnosť vrátane rozdielu posunov. | Áno; Kleprlík dôkaz vynechá, ukáže aplikáciu [la1-P8-par1-kleprlik 1:04:59–1:09:23]. | Skriptový príklad a prednášková kontrola [la1-skripta-chap05-925/a]; nie samostatná úloha v troch skúškach. |
| 4.16 | Definice | Lineární varieta | $W\subseteq T^n$ má tvar $a+P$ pre $a\in T^n$ a podpriestor $P$. | Definícia sa nedokazuje; Kleprlík [la1-P8-par1-kleprlik 1:11:03–1:12:04]. | Výslovná požiadavka [classification.adoc:187]. |
| 4.18 | Věta | O základních vlastnostech variety | Bod posunu leží vo variete, každý jej bod je možný posun, zaměření je jednoznačné a rovnosť má dve podmienky. | Áno všetky body; Kleprlík prvé dva dokazuje, zvyšok len vysloví [la1-P8-par1-kleprlik 1:24:30–1:34:03]. | Zdôvodnenie dimenzie a rovnosti; priame samostatné skúškové zadanie zdroj nemá. |
| 4.23 | Věta | O vztahu SLR a podprostorů | Podpriestory sú presne množiny riešení homogénnych SLR a $h(A)=n-\dim P$. | Áno; Klouda dokazuje na príklade v $\mathbb Z_5^4$ vrátane krajných prípadov [la1-P8-par2-klouda 46:30–1:16:59], proseminár ukáže konštrukciu [la1-prosem2223-04 42:37–51:07]. | Dôkaz za 6 bodov Hilbert [la1-exam-2026-01-26-hilbert-3/c]; požaduje [classification.adoc:189]. |
| 4.26 | Věta | O vztahu SLR a variet | Neprázdna množina je varietou práve vtedy, keď je množinou riešení nejakej SLR. | Áno, z 4.23 a $b=Aa$; Klouda [la1-P8-par2-klouda 1:17:01–1:21:00], proseminár výpočet [la1-prosem2223-04 54:23–57:01]. | Dôkaz výslovne požaduje [classification.adoc:189]; aplikácie [la1-marast-13875/a]. |
| 4.28 | Důsledek | O vztahu SLR a nadrovin | Nadrovina je riešenie jednej rovnice s aspoň jedným nenulovým koeficientom. | Áno; v určených prednáškach ešte nie [chap-05.woo:1841]. | Geometrické porozumenie; samostatný výskyt v troch skúškach zdroj nemá. |
| 4.29 | Pozorování | O řešení SLR jako průniku nadrovin | Pri riešiteľnej sústave s $A\ne\Theta$ je riešenie prienik nadrovin jednotlivých nenulových rovníc. | Zdôvodnené slovne; na určených prednáškach ešte nie [chap-05.woo:1873]. | Kontext ku geometrickým úlohám, priame skúškové zadanie zdroj nemá. |
| 4.30 | Postup | Vyjádření variety pomocí SLR | Bázu zaměření dáme do riadkov pomocnej sústavy, jej homogénnu bázu do riadkov $A$, potom $b=Aa$. | Konštruktívne dôkazy 4.23/4.26; Klouda „tady je ten algoritmus“ [la1-P8-par2-klouda 1:21:02–1:21:40], proseminár [la1-prosem2223-04 46:48–57:01]. | Hlavný prevod [la1-marast-13875/a; la1-marast-14158/b]. |
| 4.31 | Poznámka | O (ne)parametrickém vyjádření variety | Parametrické rovnice rozpisujú $a+\sum\alpha_i u_i$ po súradniciach; neparametrické rozpisujú $Ax=b$. | Terminológia; Klouda [la1-P8-par2-klouda 1:21:40–1:24:17], proseminár vysvetlí [la1-prosem2223-04 40:40–41:53]. | Priamo skúška Hilbert [la1-exam-2026-01-26-hilbert-3/d1; /d2]. |

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

**A. Kompletné znenie Frobeniovej vety na papieri.**

1. Najprv deklaruj teleso a rozmerové prostredie: $A\in T^{m,n}$, $b\in T^{m,1}$, sústava $Ax=b$ pre $n$ neznámych.
2. Napíš $Ax=b$ je riešiteľná **práve vtedy**, keď $h(A)=h(A\mid b)$.
3. Ak je riešiteľná a $A\tilde x=b$, potom $S=\tilde x+S_0$.
4. $S_0$ je podpriestor riešení $Ax=\theta$ a $\dim S_0=n-h(A)$.

Toto je celé znenie Vety 4.2 v dodaných skriptách aj slajdoch, vrátane všetkých predpokladov. [Věta 4.2, chap-05.woo:106; bi-la1-prezentace-04.tex:72]
Kleprlík na nahrávke hovorí o dvoch nových bodoch a starom vzťahu posunu; je to rozdiel v usporiadaní prezentovanej verzie, nie dôvod vypustiť časť požadovanú skúškou. [la1-P8-par1-kleprlik 18:37–20:38; testy-la1-zkouska.md:8]

**B. Dôkaz kritéria riešiteľnosti.**

- Polož $U=\langle A_{:1},\ldots,A_{:n}\rangle$, $V=\langle A_{:1},\ldots,A_{:n},b\rangle$; vždy platí $U\subseteq V$.
- $Ax=b$ je riešiteľná práve vtedy, keď $b\in U$, práve vtedy, keď $U=V$.
- Rovnosť týchto obalov je ekvivalentná rovnosti ich dimenzií **preto, že sú vnorené**.
- Dimenzie obalov stĺpcov sú $h(A^T)$ a $h((A\mid b)^T)$; transpozícia nemení hodnosť.
- Výsledok je $h(A)=h(A\mid b)$. Reťazec ekvivalencií musí mať zdôvodnený aj spätný smer.

Krásenský a Klouda idú reťazcom, Kleprlík radšej rozdelí dve implikácie; oba postupy sú v súlade so skriptami. [Věta 4.2, chap-05.woo:106; la1-P8-par3-krasensky 1:32:01–1:36:49; la1-P7-par2-klouda 1:22:08–1:29:27; la1-P8-par1-kleprlik 21:32–30:58]
Pri úlohe „dokážte postačujúcu podmienku“ začni rovnosťou hodností a skonči existenciou $x$; samotná nutná podmienka úlohu nesplní. [la1-marast-14159/3]

**C. Úplné riešenie $Ax=b$: čo sa píše a v akom poradí.**

1. Zapíš $(A\mid b)$ a pomocou GEM prejdi na HST. Upravuj aj pravú stranu.
2. Uveď $h(A)$ a $h(A\mid b)$; nerovnaké hodnosti znamenajú $S=\emptyset$ a koniec.
3. Pri rovnosti označ hlavné a vedľajšie stĺpce matice $A$: prvé určujú vázané, druhé voľné premenné. Pravá strana nie je ďalšia neznáma.
4. Zvoľ voľné premenné pre partikulárne riešenie, bežne všetky nulové, a spätne dopočítaj vázané; získaj $\tilde x$.
5. Teraz **vynuluj pravú stranu**. Pre $d=n-h(A)$ voľných súradníc zvoľ bázu $T^d$, štandardne $(1,0,\ldots,0)$ až $(0,\ldots,0,1)$.
6. Pre každú túto voľbu dopočítaj vázané súradnice; dostaneš $z_1,\ldots,z_d\in S_0$.
7. Zapíš $S_0=\langle z_1,\ldots,z_d\rangle$ a výsledok **$S=\tilde x+\langle z_1,\ldots,z_d\rangle$**.
8. Zdôvodni úplnosť: $d$ LN vektorov z podpriestoru $S_0$ dimenzie $d$ je jeho báza; použiješ 4.2, nie len kontrolu jednotlivých vektorov.

Poradie 4–5 zodpovedá skriptám a prosemináru; Kleprlík najprv ukáže homogénnu bázu a potom nájde partikulárny bod. Poradie výpočtu sa teda môže obrátiť. [Postup 4.10, chap-05.woo:734; la1-prosem2223-04 19:09–24:34; la1-P8-par1-kleprlik 59:24–1:01:16]
Do odpovede píš súradnice stále v pôvodnom poradí $x_1,\ldots,x_n$; pri prehodení stĺpcov vráť premenné späť. Pevné poradie vektorov bázy zdroj nepredpisuje: povoľuje ľubovoľnú bázu. [Poznámka 4.8, chap-05.woo:661; Postup 4.5, chap-05.woo:464]
Pri $d=0$ napíš $S=\{\tilde x\}$, pri homogénnej sústave $S_0=\{\theta\}$. Toto je jednoprvková množina, nie prázdna množina. [Poznámka 4.3, chap-05.woo:132; la1-P8-par1-kleprlik 1:01:16]

**Parametre a prípustné alternatívy zápisu.**

Tvar s explicitnými parametrami je $S=\{\tilde x+\alpha_1z_1+\cdots+\alpha_dz_d\mid\alpha_1,\ldots,\alpha_d\in T\}$.
Pri zápise obalom netreba parametre jednotlivo vypisovať, pretože ich kvantifikáciu už obsahuje význam obalu; pri parametrických rovniciach uvedieš, že sú ľubovoľné a z ktorého telesa.
To je matematické rozvinutie Poznámky 4.31, nie doložený pokyn na syntax konkrétneho MARAST vstupu. [Poznámka 4.31, chap-05.woo:1977]
Pre $\mathbb Z_5$ sú parametre z $\mathbb Z_5$, nie z $\mathbb R$; súradnice i spätné dosadzovanie počítaš v tom istom telese. [la1-prosem2223-04 40:12–41:03; 47:54–48:10]
Štandardná báza voľných súradníc nie je povinná: iná báza alebo následné nenulové prenásobenie bazických vektorov môže odstrániť zlomky. [la1-P8-par1-kleprlik 52:42–53:26; la1-prosem2223-04 33:59–36:56]

**Overený vzor zo skrípt.**

Pre maticu v Příklade 4.12 a pravú stranu $(3,-2,13,11)^T$ vychádza
$S=(-3,2,0,1,0)+\langle(-1,-1,2,0,0),(-33,7,0,-6,4)\rangle$.
Má päť súradníc, dva nezávislé homogénne smery a jeden partikulárny bod; explicitne teda dva parametre z $\mathbb R$.
Pri kontrole sa testuje $A\tilde x=b$, $Az_i=\theta$, LN a $\dim S_0=5-h(A)=2$. [Příklad 4.12, chap-05.woo:775–810]

**D. Pre ktoré pravé strany je sústava riešiteľná.**

GEM rob na $(A\mid(b_1,\ldots,b_m)^T)$; každú úpravu prenášaj na výrazy s $b_i$.
V HST musia byť všetky pravé strany pri nulových riadkoch $A$ nulové. Vysvetli to rovnosťou hodností podľa 4.2.
Proseminár dospeje k podmienke $-4b_1+b_2-b_3+b_4=0$; potom pre konkrétne $b$ len dosadí do hotového HST, neopakuje GEM. [la1-prosem2223-04 03:07–04:10; 11:11–12:42; 16:23–18:39; la1-marast-13874/a]
Pozor na ASR v 12:42, kde vypadne mínus pred $4b_1$; správne znamienko potvrdzuje neskoršie dosadenie a verejný prepis MARAST. [la1-prosem2223-04 18:03–18:09; marast-problems/lecture-260.md:14]

**E. Rovnosť dvoch množín riešení.**

Nech $S_1=u+\langle y_1,\ldots,y_k\rangle$, $S_2=v+\langle z_1,\ldots,z_k\rangle$ a oba súbory sú LN.

1. Over predpoklad LN a rovnaký počet $k$; pri redundantných generátoroch najprv nájdi bázy ich obalov.
2. Zostav maticu s **riadkami** $y_1^T,\ldots,y_k^T,z_1^T,\ldots,z_k^T,(u-v)^T$.
3. Spočítaj hodnosť. $S_1=S_2$ práve vtedy, keď je hodnosť $k$ (4.14).
4. Ak sa dimenzie zaměření líšia, množiny sa už podľa 4.18 rovnať nemôžu.
5. Toto overuje rovnosť množín, nie správnosť voči pôvodnej sústave. Tú over zvlášť postupom C.

Body 1 pri redundantných súboroch a 4 sú dôsledky uvedených viet; priamo formulovaný kurzový hodnostný test má predpoklad dvoch LN $k$-členných súborov. [Pozorování 4.14, chap-05.woo:862; Věta 4.18, chap-05.woo:1075]
Rovnako možno overiť dve podmienky $P=Q$ a $v-u\in P$ podľa 4.18; rovnaká dimenzia samotná nestačí.
Kleprlík demonštruje hodnosť 2, po zámernej chybe 3; neprepisujem jeho ASR čísla, spoľahlivý číselný príklad je 4.15. [la1-P8-par1-kleprlik 1:08:51–1:09:48; Příklad 4.15, chap-05.woo:925]

**F. Nájsť SLR k $W=a+P$.**

1. Zisti dimenziu $k$ a bázu $(u_1,\ldots,u_k)$ podpriestoru $P$; ak máš len generátory, najprv vyber bázu.
2. Daj $u_i^T$ do **riadkov** pomocnej matice $Y\in T^{k,n}$ a vyrieš $Yc=\theta$.
3. Nájdi bázu $(c_1,\ldots,c_{n-k})$ tejto pomocnej množiny riešení, opäť postupom 4.5.
4. Z jej vektorov zostav **riadky** hľadanej $A$, teda $A_{i:}=c_i^T$; $A$ má $n$ stĺpcov a pri tejto konštrukcii $n-k$ LN riadkov.
5. Vypočítaj $b=Aa$ a odovzdaj $(A\mid b)$; pri požiadavke na neparametrické rovnice ju rozpíš ako jednotlivé rovnice.
6. Zdôvodni $Au_i=\theta$, teda $P\subseteq S_0$, a zároveň $\dim S_0=n-h(A)=k=\dim P$; preto $P=S_0$.
7. Keďže $Aa=b$, vektor $a$ je partikulárne riešenie, takže 4.2 dáva presne $a+P$.

To je obsah konštruktívnych dôkazov 4.23/4.26 a Postupu 4.30, nie iba trik na uhádnutie rovníc. Klouda zdôvodňuje krok 6 slovami „ono opravdu stačí, aby tyhle dva byly řešením, ta matice měla správnou hodnost“: LN riadky dajú $h(A)=n-k$, Frobenius $\dim S_0=k$, a $k$ LN riešení je báza [la1-P8-par2-klouda 1:09:55–1:12:22]. O „kolmých“ vektoroch nehovorí (kurz súčin vektorov nedefinuje, Varování 2.5): symetriu vysvetľuje transpozíciou súčinu matíc [1:01:31–1:03:48]. [Věta 4.23, chap-05.woo:1419; Věta 4.26, chap-05.woo:1690; Postup 4.30, chap-05.woo:1938]
Triviálne prípady: $k=0$ dá $(E\mid a)$; $k=n$ dá nulovú sústavu $(\Theta\mid\theta)$, lebo $a+T^n=T^n$. [Postup 4.30, chap-05.woo:1938]
V proseminári: $a=(2,1,0,0,1)$, $P=\langle(1,2,2,0,1),(1,1,2,0,0)\rangle\subseteq\mathbb Z_5^5$; vyjde
$A=\begin{pmatrix}3&0&1&0&0\\0&0&0&1&0\\1&4&0&0&1\end{pmatrix}$, $b=(1,0,2)^T$.
Teda neparametricky $3x_1+x_3=1$, $x_4=0$, $x_1+4x_2+x_5=2$ v $\mathbb Z_5$. [la1-prosem2223-04 53:04–57:01; la1-marast-13875/a]

**G. Z bodov na rovinu a z rovníc na parametre.**

Pri zadanej rovine cez $a,b,c$ zvoľ bod posunu $a$ a smery $b-a,c-a$; over ich LN, pretože dva rozdiely nemusia vždy tvoriť bázu roviny.
Potom $W=a+\langle b-a,c-a\rangle$ a pokračuj F; nedávaj automaticky pôvodné body do bázy zaměření. [Příklad 4.33, chap-05.woo:2089; marast-problems/lecture-260.md:66]
Opačne vyrieš zadanú SLR podľa C, rozviň $x=a+\sum\alpha_i u_i$ a rozpíš jeho $n$ súradníc; počet nezávislých parametrov je $k=\dim W$.
Príklad 4.32 má $x+y-z+u=1$, $2x+u=2$ v $\mathbb R^4$, príklad 4.33 ide opačným smerom od troch bodov. [Poznámka 4.31, chap-05.woo:1977; Příklad 4.32, chap-05.woo:2050; Příklad 4.33, chap-05.woo:2089]
Členstvo bodu $q$ over buď dosadením do všetkých neparametrických rovníc, alebo riešiteľnosťou $q-a=\sum\alpha_i u_i$; pri druhom postupe idú smery do **stĺpcov** matice pre neznáme koeficienty. [chap-05.woo:1061; :2163; la1-prosem2223-04 03:59–04:16]

**H. Alternatíva s jednotkovým blokom.**

GEM možno dotiahnuť do rHST, po prípadnej permutácii stĺpcov dostať $\begin{pmatrix}E_k&B\\\Theta&\Theta\end{pmatrix}$ a čítať bázu $S_0$ z riadkov $(-B^T\ E_{n-k})$.
Následne musíš vrátiť súradnice do pôvodného poradia. Ide o Vetu 4.7, nie všeobecný príkaz zameniť akékoľvek bloky v ľubovoľnom HST. [Věta 4.7, chap-05.woo:560; Poznámka 4.8, chap-05.woo:661]
Kleprlík ukáže tento spôsob ako doplnkový, aj pre súvislosť so samoopravnými kódmi; bežný postup C nevyžaduje rHST. [la1-P8-par1-kleprlik 53:26–59:18]

## 5. Značenie a terminológia

- $T$ je teleso, $T^{m,n}$ matice typu $m\times n$; $T^{n,1}$ stĺpcové vektory, pri identifikácii aj $T^n$. [chap-05.woo:48–75]
- $h(A)$ je hodnosť; kurz v sledovaných formuláciách píše $h$, nie `rank` ako hlavný symbol. `Matrix rank` zaznie pri Wolfram Alpha, nie ako zákaz alternatívnej značky. [bi-la1-prezentace-04.tex:72; la1-P8-par1-kleprlik 1:08:53–1:08:56]
- $A_{:j}$ je $j$-ty stĺpec, $A_{i:}$ riadok. Riešenie $x$ sa násobí sprava ako stĺpec; „vektory do riadkov“ pri F znamenajú transponované vektory. [chap-05.woo:174–180; :1419–1534]
- $S$ znamená všetky riešenia, $S_0$ riešenia „přidružené homogenní soustavy“, $\tilde x$ jedno „partikulární řešení“. [Poznámka 4.3, chap-05.woo:132]
- Krásenský výslovne uvádza, že v tomto kurze sa používa $S_0$, inde aj $\ker A$ ako „jádro matice“. Nie je doložený zákaz $\ker A$, ale lekcia má nadviazať na $S_0$. [la1-P8-par3-krasensky 1:26:19–1:26:35]
- $\langle z_1,\ldots,z_d\rangle$ je „lineární obal“; LN a LZ znamenajú lineárne nezávislý a závislý súbor. [Postup 4.5, chap-05.woo:464; Pozorování 4.14, chap-05.woo:862]
- $\theta$ je nulový vektor, $\Theta$ nulová matica, $E$ jednotková matica; vždy rozhodujú rozmery. [bi-la1-prezentace-04.tex:32; Věta 4.7, chap-05.woo:560]
- HST je „horní stupňovitý tvar“, rHST „redukovaný horní stupňovitý tvar“, GEM „Gaussova eliminační metoda“. Úplná Gaussova eliminácia sa v skriptách označuje aj ako Gaussova–Jordanova. [chap-05.woo:9; :646–659]
- „Vázané proměnné“ patria hlavným stĺpcom, „volné proměnné“ vedľajším; parametre skriptá označia $t_i$ alebo $\alpha_i$. [Postup 4.5, chap-05.woo:464; Poznámka 4.31, chap-05.woo:1977]
- $W=a+P$: „vektor posunutí“ $a$, „zaměření“ $Z(W)=P$, „směrový vektor“ každý **nenulový** vektor zo $Z(W)$, $\dim W=\dim Z(W)$. [Poznámka 4.17, chap-05.woo:984]
- Český hlavný názov je „lineární varieta“; „afinní podprostor“ skriptá uvedú ako názov v iných kurzoch. Teda nie je pravda, že by tento pojem úplne zakazovali. [Definice 4.16, chap-05.woo:971; úvod :962–968]
- Bod, přímka, rovina majú dimenziu 0, 1, 2; nadrovina v $T^n$ má dimenziu $n-1$, nie vždy 2. [Poznámka 4.19, chap-05.woo:1157; la1-P8-par1-kleprlik 1:34:14–1:35:07]
- Písmená $M$, $W$ či $V$ pre varietu nie sú fixné: Kleprlík zmenu výslovne pripúšťa. Pevné sú význam a deklarácia. [la1-P8-par1-kleprlik 1:16:02–1:16:09]

## 6. Čo kto zdôrazňuje, chce a zakazuje

Krátke citáty z titulkov ponechávam doslovné aj s hovorovou češtinou; zmysel kontrolujú uvedené skriptá.

- **Kleprlík, znenie vety:** „Nejdřív musíte deklarovat prostředí.“ Chýbajúce rozmery nechajú $n$ bez významu. [la1-P8-par1-kleprlik 17:28; 20:40–20:58]
- **Kleprlík, dôkaz dimenzie:** „A ten je jako dobrovolně ve skriptech, ale jako nikdo po vás ho chtít nebude.“ Týka sa práve dôkazu dimenzionálnej časti, nie jej znenia či aplikácie. [la1-P8-par1-kleprlik 31:13–31:18]
- **Oficiálne požiadavky:** žiadajú formuláciu 4.2, dôkaz prvej časti, úplné riešenie, prevody a dôkazy 4.23/4.26; názov nahrávky ani neodprednášanie povinnosť nerušia. [classification.adoc:183–189]
- **Kleprlík, poradie súradníc:** „akorát potom nezapomeňte zpátky prohodit pořadí těch spočítaných neznámých.“ Prehadzovanie skôr neodporúča, nezakazuje. [la1-P8-par1-kleprlik 51:20–51:27]
- **Kleprlík, bloková metóda:** „Zase po vás nebudu chtít důkaz tenhle věty.“ Hneď dodá, že nechce ani formuláciu; ide konkrétne o 4.7. [la1-P8-par1-kleprlik 56:57–57:02]
- **Skriptá, homogénna báza:** „Nezapomínejte si zakrýt pravou stranu SLR!“ Báza sa počíta pre nulu, nie pôvodné $b$. [Varování 4.11, chap-05.woo:768]
- **Proseminár, voľba bodu:** „Tady ta volba těch dvou 0 než vždycky jako je nejlepší, ale vždycky funguje.“ Automatický prepis je gramaticky chybný; kontext znamená, že nulová voľba funguje, hoci môže vytvoriť zlomky. [la1-prosem2223-04 22:04]
- **Proseminár, pravá strana:** „My už teď řešíme jen homogenitou stavu.“ Skomolené ASR znamená homogénnu sústavu; zdôrazní nahradenie pravých strán nulami. [la1-prosem2223-04 29:04; Varování 4.11, chap-05.woo:768]
- **Kleprlík, porovnanie výsledkov:** „tak to buď máte oba dva stejně dobře, nebo stejně špatně.“ Rovnosť zápisov nenahrádza skúšku správnosti. [la1-P8-par1-kleprlik 1:09:45]
- **Skriptá, dôkaz:** „Je to jen ilustrační“ je krátky doslovný úryvok z varovania: konkrétny „propříklad“ nenahrádza všeobecný dôkaz. [Varování 4.24, chap-05.woo:1537]
- **Proseminár, rôzne výsledky:** „Ale rozhodně to není určeno jednoznačně.“ Týka sa matice a pravej strany nájdenej k variete; iná báza pomocného riešenia dá inú platnú sústavu. [la1-prosem2223-04 59:27; 57:42–58:59]
- **Vyučujúci LA1 na Discorde predmetu:** stačí splniť úlohu a definovať varietu. Pri samotnej definícii netreba povinne pridávať názvy zaměření a dimenzie. [discord-la1.md:108]
- **Vyučujúci LA1 na Discorde predmetu:** v tvrdení treba vedieť ekvivalenciu jasne rozlíšiť. Pri Frobeniovej vete nepíš len jednostranné „pokud“. [discord-la1.md:120]

## 7. Typické úlohy

Frekvencie nižšie sú v **lokálne prepísanom korpuse**: tri skúšky, verejné úlohy MARAST a inventár `sources.json`; nie percentá všetkých historických skúšok.
Pri počítaní podotázok rešpektujem vlastné `chapter` časti: v Hilbertovej úlohe 3 nepočítam inverznú maticu ani determinant ako kapitolu 4.

| Typ | Krátke zadanie po česky | Odkaz | Ako často v dostupnom korpuse |
|---|---|---|---|
| Znenie 4.2 | „Napište kompletní znění Frobeniovy věty…“ | [la1-exam-2025-12-17-frobenius-2/a; la1-exam-2026-01-26-hilbert-3/a; la1-marast-14159/2] | 2 z 3 skúšok + 1 verejná vzorová podotázka MARAST. |
| Dôkaz riešiteľnosti | „Dokažte část… postačující podmínkou…“ | [la1-marast-14159/3; classification.adoc:184] | 1 verejná vzorová podotázka; v troch skúškach priamo 0. |
| Kompletné riešenie ako varieta | „Řešení zapište ve tvaru variety.“ | [testy-la1-zkouska.md:8; :20; la1-marast-13874/b] | 2 z 3 skúšok, 1 MARAST; 3 podotázky v skriptovom inventári pre homogénnu/nehomogénnu sústavu a blokovú metódu. |
| Podmienka na $b$ | „Pro jaká $b$… má tato soustava řešení?“ | [la1-marast-13874/a; la1-prosem2223-04 01:41–01:45] | 1 verejná úloha MARAST; zvlášť 1 skúškový bonus na počet pravých strán [la1-exam-2026-02-02-bolzano-2/b]. |
| Dôkaz existencie SLR | „Dokažte, že pro netriviální podprostory… existuje… soustava…“ | [la1-exam-2026-01-26-hilbert-3/c] | 1 z 3 skúšok, 6 bodov; aj oficiálna požiadavka. |
| Z rovníc na parametre a dimenziu | „Napište její parametrické vyjádření. Jakou má… dimenzi?“ | [la1-exam-2026-01-26-hilbert-3/d1; /d2; la1-marast-13892/a; -13893/a; -13894/a; -13895/a] | 1 skúška s 2 podotázkami; 4 verejné MARAST podotázky, 3 skriptové. |
| Parametricky na neparametricky | „Najděte neparametrické rovnice… variety…“ | [la1-marast-13875/a; la1-marast-13901/a] | 2 verejné MARAST podotázky s týmto skill; ďalšie prevody sú medzi úlohami z bodov. |
| Varieta cez body | „Najděte parametrické a neparametrické rovnice roviny…“ | [la1-marast-14158/a; /b; la1-marast-13904/a; /b; la1-marast-13898/a; /b; la1-marast-13899/a] | 7 verejných MARAST podotázok so skill `points-to-equations`; 3 ďalšie v skriptovom inventári pod týmto skill. |
| Rovnosť riešení | „Ověřte, že se tyto množiny rovnají.“ | [Příklad 4.15, chap-05.woo:925; la1-skripta-chap05-925/a] | 1 skriptový príklad, 1 Kleprlíkov prednáškový príklad; v troch skúškach 0. |
| Členstvo vo variete | „Patří vektory… varietě…?“ | [chap-05.woo:1061; :2163; la1-exam-2026-01-26-hilbert-3/d3] | 4 skriptové podotázky; 1 skúšková s neistým rozmerom zadaného vektora. |
| Vypísanie bodov | „Vypište všechny body…“ | [la1-skripta-chap05-1246/a; /b; /c] | 3 skriptové podotázky; 0 v troch prepísaných skúškach. |

**Druhý zápočtový test:** v `sources.json` nie je podotázka druhu `test` zaradená do S4; to neznamená neprítomnosť predpokladov kapitoly.
`testy-la1-2.md:6` a `:81` dokladajú dôkaz, že homogénne riešenia tvoria podpriestor; `:82` obsahuje samostatné fotografie úlohy na konštrukciu $A,b$ s dvoma predpísanými riešeniami a LN riadkami.
Pri tejto fotografii nie je isté priradenie ku konkrétnej variante druhého testu; nevydávam ju za štandardný doklad výskytu celej kapitoly 4 v druhom teste.

**Rozstřel:** `testy-rozstrel.md` obsahuje BI-DML, nie LA1; zdroj nemá relevantné zadanie pre túto kapitolu. [testy-rozstrel.md:1]
**Otvorené kvízy:** existuje teoretický kvíz ku kapitolám 3 a 4 a početný ku kapitolám 4 a 5; zaznamenaná je len existencia a typ. Ich otázky ani parafrázy sem nepatria. [marast.md:73–90; la1-quiz-kap4-5-pocetni]

## 8. Časté chyby a otázky (Discord, prednášky)

- **Len implikácia namiesto ekvivalencie.** Študent hlási stratu bodu za „pokud“, učiteľ potvrdí potrebu rozlíšiť ekvivalenciu. [discord-la1.md:112–122]
- **Nezavedené $n$.** Vzorec $n-h(A)$ bez deklarácie rozmerov nedáva čitateľovi určený význam $n$. [la1-P8-par1-kleprlik 20:40–20:58]
- **Výpočet bázy s nenulovým $b$.** Tak dostávame body nehomogénnej množiny, nie homogénne smery; výslovné varovanie 4.11. [chap-05.woo:768; la1-prosem2223-04 24:03–24:34]
- **Rovnaká dimenzia znamená rovnaký podpriestor.** Chýba inklúzia; rôzne roviny v $\mathbb R^3$ môžu mať rovnakú dimenziu. [la1-P8-par1-kleprlik 27:37–29:15]
- **Zabudnutý návrat súradníc po prehodení stĺpcov.** Zmení sa význam riešenia. [la1-P8-par1-kleprlik 56:21–56:51; Poznámka 4.8, chap-05.woo:661]
- **Iný výsledok než MARAST musí byť zlý.** Nie: výber bázy aj bodu nie je jednoznačný, treba overiť množinu. [la1-prosem2223-04 57:14–59:27]
- **Podarilo sa dosadiť generátory, teda už je dôkaz hotový.** Pri konštrukcii SLR to ukazuje iba zahrnutie; rovnosť uzatvára hodnosť a dimenzia. [Věta 4.23, chap-05.woo:1419–1534; la1-prosem2223-04 50:17–51:07]
- **Číselný príklad namiesto všeobecného dôkazu.** Pomocné texty „Co se děje v důkazu…“ sú ilustrácia, nie odpoveď na „dokažte“. [Varování 4.24, chap-05.woo:1537]
- **Čo je dimenzia variety?** Študentka opisuje uznanú odpoveď ako dimenziu zaměření s vysvetlením zaměření; je to skúsenosť študentky, nie samostatný pokyn učiteľa. [discord-la1.md:631–637]
- **Ako vôbec vymyslieť SLR k variete?** Študent odporúča proseminár s takýmto príkladom; konkrétny prečítaný proseminár presne tento postup ukazuje. [discord-la1.md:703–709; la1-prosem2223-04 37:14–59:27]
- **„Matice má řešení.“** Krásenský sa pri tejto formulácii sám opravuje; riešenie má sústava $Ax=b$. [la1-P8-par3-krasensky 1:31:31–1:32:01]

## 9. Rozdiely medzi paralelkami

- **Kleprlík:** najďalej v dodanom materiáli; dôkaz riešiteľnosti po dvoch implikáciách, podrobný algoritmus bázy, doplnkový blokový spôsob, kontrola rovnosti cez Wolfram Alpha a geometrické príklady variet. [la1-P8-par1-kleprlik 21:32–31:22; 47:00–59:18; 1:07:05–1:24:03]
- **Klouda:** na konci P7 najprv intuitívne vysvetlí skladanie $b$ zo stĺpcov, potom obaly, hodnosti a dimenziu cez vedľajšie stĺpce; v dostupnom úseku ešte nerobí úplný číselný algoritmus 4.10. [la1-P7-par2-klouda 1:21:28–1:35:05]
- **Krásenský:** väčšinu P8 dokončuje kapitolu o regularite; novú kapitolu začne až 1:24:17. Dôkaz riešiteľnosti dá ako reťaz ekvivalencií s explicitným vnorením obalov, dokončenie odloží. [la1-P8-par3-krasensky 1:24:17–1:36:53]
- **Proseminár:** počíta jeden väčší reálny príklad s premennou pravou stranou a konkrétnym bodom, potom opačný prevod v $\mathbb Z_5^5$; pre autorov lekcií najúplnejšie zachytáva „čo písať pri počítaní“. [la1-prosem2223-04 01:05–36:56; 37:14–59:27]
- **Poradie operácií:** skriptá a proseminár nájdu najprv partikulárne riešenie, Kleprlík najprv vysvetlí homogénnu bázu; zhodujú sa na výsledku bod plus obal. [Postup 4.10, chap-05.woo:734; la1-P8-par1-kleprlik 1:01:05]
- **Riadky verzus stĺpce nie sú spor:** stĺpce skladajú pravú stranu pri hľadaní koeficientov; riadky sa používajú v pomocnej sústave pri konštrukcii rovníc a pri teste rovnosti. [la1-P7-par2-klouda 1:21:41–1:23:58; Pozorování 4.14, chap-05.woo:862; Postup 4.30, chap-05.woo:1938]

## 10. Nejasné a chýbajúce

- **P8 paralelky 2 doplnená 19. 11.:** Klouda 18. 11. odprednášal 4.23–4.33 (oddiel 1). Čísla jeho príkladu v $\mathbb Z_5^4$ z titulkov spoľahlivo nečítam (tabuľa), appka ho preto neprepisuje. Varování 4.24, príklady 4.25/4.27 a 4.28/4.29 ostávajú len zo skrípt.
- **Prepis nie je obraz tabule:** pri Kleprlíkových číselných príkladoch a rovnosti množín sú čísla poškodené; presné matice beriem zo skrípt/MARAST. [la1-P8-par1-kleprlik 1:02:39; 1:08:01–1:08:30]
- **ASR zamieňa „podprostor“ a „varieta“:** Kleprlík v 1:12:29 povie podpriestor, v 1:12:59–1:13:29 to opraví na varietu. Správne platí: neprázdne riešenie je varieta, homogénne riešenie podpriestor. [la1-P8-par1-kleprlik 1:12:17–1:13:41; Věta 4.26, chap-05.woo:1690]
- **Číslovanie/bodovanie Frobenia:** nahrávka Kleprlíka hovorí o dvoch bodoch a starom posune; lokálne skriptá aj slajdy už obsahujú tri položky. Na papieri používaj ich celé znenie, nie skrátenú historickú štruktúru. [la1-P8-par1-kleprlik 18:37–20:38; bi-la1-prezentace-04.tex:72–82]
- **Dôkaz 4.7 v skriptách má skratku:** po predpoklade $A\sim\begin{pmatrix}E&B\\0&0\end{pmatrix}$ následne počíta $A$ ako túto upravenú maticu. Doplniť treba vyslovenie, že GEM zachová množinu riešení, takže dôkaz členstva môžeme robiť na upravenej matici. [Věta 4.7, chap-05.woo:560–640]
- **Počet bodov nad konečným telesom:** v 1:22:53 ASR zachytí „6 bodíků“, hoci vymenúva parametre 0 až 4; nejde o spoľahlivý údaj. Správny výpočet rob z piatich hodnôt parametra, nie z tejto vety. [la1-P8-par1-kleprlik 1:22:53; Příklad 4.21, chap-05.woo:1246]
- **Počet báz v proseminári:** vyslovených 125 sám prednášajúci okamžite odvolá; údaj nepreberať. [la1-prosem2223-04 57:59–58:22]
- **Chyba alebo neistota v skúškovom prepise:** Hilbertova varieta je v $\mathbb Z_3^5$, ale kontrolovaný vektor má iba štyri súradnice. Chýbajúcu súradnicu zdroj nemá; úlohu neopravovať domyslením. [testy-la1-zkouska.md:15; la1-exam-2026-01-26-hilbert-3/d3]
- **Obmedzené frekvencie:** prepísané sú len tri skúšky; „2 z 3“ je údaj o tomto výbere. Kleprlíkova vlastná poznámka o približne dvoch tretinách písomiek nie je nezávislý štatistický výskum. [testy-la1-zkouska.md:3; la1-P8-par1-kleprlik 16:42]
- **Otvorené MARAST kvízy:** otázky nie sú k dispozícii; iba názov a typ. Verejnú cvičebnicu od nich odlišujem. [marast.md:73–90; marast-cvicebnice.md:25]
- **Autorské meno proseminára:** presný podpis v prečítanom úvode ani v metadátach zdroj nemá; Discord predmetu spája podobný príklad s jedným z prednášajúcich, ale to samo nie je identifikácia konkrétneho videa. [la1-prosem2223-04 00:30–01:05; discord-la1.md:707]
- **Skriptá majú záverečné TODO:** samostatne dopísať výpočet prieniku variet a určovanie rovnosti variet. Rovnosť už vysvetľujú 4.14 a 4.18; osobitný nový algoritmus prieniku v tejto kapitole zdroj nemá. [chap-05.woo:2172–2178]

## 11. Surové zdroje

Cesty sú voči koreňom podkladov kurzu (kurz/README.md, mimo repozitára); riadky `.woo` sa vzťahujú na prvú, nie slajdovú kópiu repozitára.

- `kontext/BRIEF.md`: pravidlá, citácie a osnova.
- `kontext/cisla/la1-kap4.tsv`: celý súbor, jediné použité číslovanie 4.1–4.33.
- `kontext/INDEX.md:55–59,68`: inventár prednášok a proseminára, statusy titulkov.
- `kontext/txt/la1-P8-par1-kleprlik.txt`: 10:19–1:37:18; konkrétne úseky sú rozdelené v sekcii 1.
- `kontext/txt/la1-P7-par2-klouda.txt`: 1:20:16–1:35:05.
- `kontext/txt/la1-P8-par3-krasensky.txt`: 1:24:17–1:36:53.
- `kontext/txt/la1-prosem2223-04.txt`: 00:30–59:27; od 59:57 už determinant, vynechaný.
- `zdroje/src/bi-la1-master/src/textbook/chap-05.woo`: 5–2178, hlavné časti podľa sekcie 1.
- `kontext/skripta/bi-la1-master/src/slides/bi-la1-prezentace-04.tex`: 14–567, najmä 72–92, 215–275, 283–300, 391–395, 449–516.
- `kontext/skripta/bi-la1-master/classification.adoc:183–189`: oficiálny rozsah skúškových zručností a dôkazov; v citáciách skrátené na `classification.adoc`.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`: LA1 položky s efektívnym `chapter=S4`, ich `parts`, identifikátory a zručnosti; osobitne kvíz len s `id-only`.
- `zdroje/testy-la1-zkouska.md:3–20`: tri prepísané skúšky a neistota Hilbertovej otázky.
- `zdroje/testy-la1-2.md:6,81–82`: dôkaz podpriestoru a fotografia konštrukcie sústavy.
- `zdroje/testy-rozstrel.md:1`: ide o DML, pre LA1 sa nepoužil ako doklad úloh.
- `zdroje/marast-problems/lecture-260.md`: verejné úlohy 13874, 13875, 13904, 13892–13895, 13898, 13899, 13901; determinantové úlohy vynechané.
- `zdroje/marast.md:73–90,361–389`: iba existencia kvízov a verejné vzorové úlohy 14158/14159.
- `zdroje/marast-cvicebnice.md:25`: inventár verejnej prednášky 260.
- `zdroje/discord-la1.md:104–122,631–637,703–709`: rozsah definície, ekvivalencia, dimenzia variety a spätná konštrukcia SLR.

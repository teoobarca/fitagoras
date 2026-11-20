# BI-LA1: predmet

Úroveň predmetu pod [docs/ZASADY.md](../../docs/ZASADY.md). Najprv zhrnutie a kapitoly, potom podrobný výťah
(prednášajúci, testy, čo kto chce a zakazuje, značenie, terminológia) s citáciami. Stav k 17. 11. 2026; výťah overený:
časy všetkých citácií existujú v titulkoch, citáty kontrolované ručne.
Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## Zhrnutie: čo platí v celom predmete

1. **Vysvetľuj postup.** Hlavička 2. testu: „Řádně vysvětlete svůj postup tak, aby jej pochopil čtenář, který pouze
   zná definice a tvrzení z přednášky“ [testy-la1-2.md:3]. Všetci traja to hovoria aj o GEM: „To, že vezmu vektory,
   napíšu je do sloupců matice a gemuju, tak to není vůbec dostatečné zdůvodnění.“ [la1-P5-par3-krasensky 28:30];
   Kleprlík [la1-P5-par1-kleprlik 22:10–22:49], Klouda [la1-P4-par2-klouda 1:16:38–1:17:27]. Do vzorového riešenia
   patrí: čo sú neznáme, odkiaľ je matica, čo z výpočtu plynie a prečo.
2. **Definícia je jedna.** Ekvivalentná veta nie je odpoveď na „definujte“ (Klouda pri regularite
   [la1-P7-par2-klouda 47:46–49:09], Kleprlík pri LN [la1-P4-par1-kleprlik 1:10:35–1:11:12], MARAST: „Dimenze je rovna
   počtu prvků báze.“ nie je definícia [marast.md:267]). Vlastnými slovami smieš, ak význam ostane
   [la1-P6-par3-krasensky 55:28–56:07].
3. **Pri dôkaze špeciálneho prípadu nepoužívaj vetu samotnú ani jej dôsledky** (vyučujúci LA1 na Discorde predmetu
   [discord-la1.md:438–442]); pri počítaní smieš všetko zo skrípt po danú kapitolu.
4. **Slová kurzu:** „vyřešit matici“ nie (rieši sa sústava, matica sa upravuje GEM) [la1-P6-par1-kleprlik 1:28:15;
   LA1 desatero, chap-12-znaceni.woo:954]; HST, hlavný/vedľajší stĺpec, (G1) až (G3), ∼ medzi maticami
   [la1-P5-par2-klouda 1:13:46]; hodnosť $h(\mathbf A)$, obal $\langle\cdot\rangle$, podpriestor $P\subset\subset T^n$,
   nulový vektor $\theta$, jednotková matica $\mathbf E$, typ matice $\mathbf A\in T^{m,n}$ (nie $\mathbf A^{m,n}$).
   Celé značenie: oddiel 4 nižšie a `chap-12-znaceni.woo`.
5. **Konvencie kurzu, ktoré sa líšia od učebníc:** dimenzia je definovaná cez LN súbory, nie počtom prvkov bázy
   (Definice 2.49); nulový podpriestor bázu nemá, prázdny súbor kurz nepripúšťa [la1-P5-par1-kleprlik 1:28:58];
   $\mathbb N=\{1,2,\dots\}$ bez nuly [chap-12-znaceni.woo:182].
6. **LA1 desatero** (skriptá, `chap-12-znaceni.woo:936–982`) je zhrnutie toho, čo kurz od študenta chce; je v oddiele 3.

## Kapitoly

| kap. | balík | skriptá | slajdy, kód rozvrhu | v appke | odprednášané k 17. 11. 2026 |
|---|---|---|---|---|---|
| 1 | [kap1-soustavy.md](kap1-soustavy.md) | `chap-02.woo` | 1, S1 | bez lekcií | všetky paralelky (P2, začiatok P3) |
| 2 | [kap2-zakladni-pojmy.md](kap2-zakladni-pojmy.md) | `chap-03.woo` | 2, S2 | staré súbory kapitoly 2 (`Lessons.kt` …) | všetky paralelky (P3 až P6) |
| 3 | [kap3-hodnost-regularita.md](kap3-hodnost-regularita.md) | `chap-04.woo` | 3, S3 | `la1-k3` | všetky paralelky (P6 až P8) |
| 4 | [kap4-frobenius.md](kap4-frobenius.md) | `chap-05.woo` | 4, S4 | `la1-k4` | Kleprlík takmer celá (P8, bez sústavy k variete); Krásenský a Klouda len Frobeniova veta, časť o riešiteľnosti; Klouda P8 je 18. 11. |
| 5, 6 | (determinant, vlastné čísla) | `chap-06.woo`, `chap-07.woo` | 5, 6 | | nie |

## Paralelky

Časy podľa rozvrhu fakulty (`data/Schedule.kt`) a začiatkov nahrávok; `index.adoc` stránky predmetu má staršie časy
(18:00, 11:00), neplatia.

| paralelka | prednášajúci | kedy |
|---|---|---|
| 1 | Luděk Kleprlík | utorok 16:15, T9:155 |
| 2 | Karel Klouda (garant) | streda 9:15, T9:105 |
| 3 | Jakub Krásenský | piatok 9:15, T9:155 |

Proseminár: spoločné video z roku 22/23 (staršie 21/22); autor sa v titulkoch nepredstavuje.

<details><summary>Nahrávky a titulky (súbor titulkov = meno v citáciách; číslo P je číslo prednášky v nahrávke)</summary>

| súbor titulkov | id nahrávky | kto | dátum | téma | titulky | stav nahrávky |
|---|---|---|---|---|---|---|
| la1-P2-par1-kleprlik | 3675636158@sirius.fit.cvut.cz | Kleprlík L. | 29. 9. 2026 | Sústavy lineárnych rovníc (slajdy 1) | áno | done |
| la1-P2-par2-klouda | 3675636326@sirius.fit.cvut.cz | Klouda K. | 30. 9. 2026 | Sústavy lineárnych rovníc (slajdy 1) | áno | done |
| la1-P2-par3-krasensky | 3675636459@sirius.fit.cvut.cz | Krásenský J. | 2. 10. 2026 | Sústavy lineárnych rovníc (slajdy 1) | áno | done |
| la1-P3-par1-kleprlik | 3675636159@sirius.fit.cvut.cz | Kleprlík L. | 6. 10. 2026 | Sústavy lineárnych rovníc, základné pojmy (slajdy 1 a 2) | áno | done |
| la1-P3-par2-klouda | 3675636327@sirius.fit.cvut.cz | Klouda K. | 7. 10. 2026 | Sústavy lineárnych rovníc, základné pojmy (slajdy 1 a 2) | áno | done |
| la1-P3-par3-krasensky | 3675636460@sirius.fit.cvut.cz | Krásenský J. | 9. 10. 2026 | Sústavy lineárnych rovníc, základné pojmy (slajdy 1 a 2) | áno | done |
| la1-P4-par1-kleprlik | 3675636160@sirius.fit.cvut.cz | Kleprlík L. | 13. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | done |
| la1-P4-par2-klouda | 3675636328@sirius.fit.cvut.cz | Klouda K. | 14. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | done |
| la1-P4-par3-krasensky | 3675636461@sirius.fit.cvut.cz | Krásenský J. | 16. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | done |
| la1-P5-par1-kleprlik | 3675636161@sirius.fit.cvut.cz | Kleprlík L. | 20. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | done |
| la1-P5-par2-klouda | 3675636329@sirius.fit.cvut.cz | Klouda K. | 21. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | done |
| la1-P5-par3-krasensky | 3675636462@sirius.fit.cvut.cz | Krásenský J. | 23. 10. 2026 | Základné pojmy lineárnej algebry (slajdy 2) | áno | partial |
| la1-P6-par1-kleprlik | 3675636162@sirius.fit.cvut.cz | Kleprlík L. | 27. 10. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | done |
| la1-P6-par2-klouda | 3675636330@sirius.fit.cvut.cz | Klouda K. | 4. 11. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | done |
| la1-P6-par3-krasensky | 3675636463@sirius.fit.cvut.cz | Krásenský J. | 30. 10. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | done |
| la1-P7-par1-kleprlik | 3675636163@sirius.fit.cvut.cz | Kleprlík L. | 3. 11. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | done |
| la1-P7-par2-klouda | 3675636331@sirius.fit.cvut.cz | Klouda K. | 11. 11. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | partial |
| la1-P7-par3-krasensky | 3675636464@sirius.fit.cvut.cz | Krásenský J. | 6. 11. 2026 | Hodnosť a regularita matíc (slajdy 3) | áno | done |
| la1-P8-par1-kleprlik | 3675636164@sirius.fit.cvut.cz | Kleprlík L. | 10. 11. 2026 | Frobeniova veta a lineárne variety (slajdy 4) | áno | done |
| la1-P8-par3-krasensky | 3675636465@sirius.fit.cvut.cz | Krásenský J. | 13. 11. 2026 | Frobeniova veta a lineárne variety (slajdy 4) | áno | done |
| la1-prosem2122-01 | youtube-quA56qwOdQk | proseminár | YouTube | Proseminář 1 (21/22) | **chýbajú** | done |
| la1-prosem2122-02 | youtube-AuL9bk5mOFA | proseminár | YouTube | Proseminář 2 (21/22) | **chýbajú** | done |
| la1-prosem2122-03 | youtube-bcFzhk0Vfbc | proseminár | YouTube | Proseminář 3 (21/22) | **chýbajú** | done |
| la1-prosem2122-04 | youtube-mGSrI3QKV6Q | proseminár | YouTube | Proseminář 4 (21/22) | **chýbajú** | done |
| la1-prosem2122-05 | youtube-ahXKuXnB500 | proseminár | YouTube | Proseminář 5 (21/22) | áno (od 20. 11. 2026) | done |
| la1-prosem2223-01 | youtube-jV9MIEnWAM0 | proseminár | YouTube | matice, soustavy rovnic, konečná tělesa | áno (od 20. 11. 2026) | done |
| la1-prosem2223-02 | youtube-sLZQteej65M | proseminár | YouTube | vektorové prostory, LN/LZ, obal, báze, dimenze, souřadnice | áno (od 20. 11. 2026) | done |
| la1-prosem2223-03 | youtube-6RGobFd8w7o | proseminár | YouTube | hodnost, regulární matice, inverze | áno | done |
| la1-prosem2223-04 | youtube-nhpO0gDcN1Q | proseminár | YouTube | Frobeniova věta, variety, determinant | áno | done |
| la1-prosem2223-05 | youtube-Y2fdE9mbSeg | proseminár | YouTube | vlastní čísla, diagonalizace | áno | done |

</details>

Riešené príklady a metódy proseminárov s titulkami sú od 20. 11. 2026 v registri pokrytia (`coverage/sources.json`, druh `proseminar`, id `<proseminár>-<sekunda>`, kotva do záznamu); výťah nižšie ich ešte necituje.

## Podrobne

Výťah z dodaného lokálneho korpusu, stav spracovania 17. 11. 2026. Presné termíny v KOS-e, pravidlá pomôcok a totožnosť všetkých autorov proseminárov zdroj nemá spoľahlivo doložené.
Pravidlá hodnotenia nižšie pochádzajú z lokálnej stránky predmetu; termíny a trvanie zo starších ukážok sú označené osobitne. Živý web ani KOS sa pri tomto výťahu neotvárali.
Účel kurzu: nástroje pre ďalšiu teóriu a aplikácie, logické a algoritmické myslenie a presné vyjadrovanie. Hlavným materiálom majú byť priebežne opravované skriptá. [chap-01.woo:20–41]

### 1. Prednášajúci a paralelky (kto, kedy, ktoré nahrávky sú, čo chýba; proseminár: kto ho vedie, ak sa dá zistiť)

- Paralelka 1: **Luděk Kleprlík, utorok** 16:15 (rozvrh fakulty; `index.adoc` uvádza zastaraných 18:00). K dispozícii sú titulky P2 až P8, súbory `la1-PN-par1-kleprlik.txt`. [INDEX.md:40–59; index.adoc:24,55–60]
- Paralelka 2: **Karel Klouda, streda**, garant; 9:15 (rozvrh fakulty; `index.adoc` uvádza zastaraných 11:00). Titulky P2 až P7, `la1-PN-par2-klouda.txt`; P8 tejto paralelky v inventári nie je. [teachers/index.adoc:10–18; INDEX.md:41–59; index.adoc:55–60]
- Paralelka 3: **Jakub Krásenský, piatok**, v harmonograme o 9:15. Titulky P2 až P8, `la1-PN-par3-krasensky.txt`. [teachers/index.adoc:23–27; INDEX.md:42–59; index.adoc:55–60]
- Časy overené podľa začiatkov nahrávok (Kleprlík ut 16:15, Klouda st 9:15, Krásenský pi 9:15); organizačné slajdy ich majú správne, `index.adoc` nie. [bi-la1-prezentace-00.tex:44–52]
- Brief výslovne uvádza, že prvé prednášky P1 nahrané nie sú. V korpuse naozaj nie sú titulky P1; stránka však odkazuje na starší záznam P1 z roku 2022/23. Ide o rozdiel medzi dostupnosťou titulkov a existenciou staršieho videa. [BRIEF.md, Zdroje; INDEX.md:40–69; index.adoc:81–84]
- Stav `partial` majú P5 Krásenský a P7 Klouda. Znamená to neúplný prepis podľa inventára, nie dôkaz, že ostatný obsah nebol odprednášaný. [INDEX.md:51,56]
- Z neskorších prednášok P9 a ďalej korpus titulky nemá; nahrávky na stránke pritom siahajú po P12. [INDEX.md:40–69; index.adoc:187–228]
- Organizácia 2026: podľa e-mailu reprodukovaného na Discorde sa nové prednášky nenahrávajú; staršie videá majú byť kompatibilné, tempo sa môže líšiť. [discord-la1.md:693–701]
- Proseminár je spoločný záznam pre všetkých. Má pripraviť základy pred 45-minútovým cvičením; treba ho pozrieť vopred a samostatne počítať. [index.adoc:26–29]
- Inventár uvádza päť proseminárov 2021/22 a päť 2022/23. Reálne textové titulky sú iba k `la1-prosem2223-03`, `-04`, `-05`; položky s `cues = 0` nemožno považovať za použiteľný prepis. [INDEX.md:60–69; kontrola súborov txt/]
- Proseminár 3: hodnosť, regularita, inverzia; 4: Frobeniova veta, variety, determinant; 5: vlastné čísla a diagonalizácia. [INDEX.md:67–69]
- **Kto vedie proseminár:** dostupné začiatky troch prepisov meno neobsahujú. Študentské správy na Discorde predmetu nepriamo spájajú proseminárny príklad konštrukcie sústavy k variete a vysvetľovanie vedľajších stĺpcov s jedným z prednášajúcich. Autorstvo videí to neurčuje. [la1-prosem2223-03 00:30; la1-prosem2223-04 00:30–00:58; la1-prosem2223-05 00:00–00:53; discord-la1.md:703–717]
- Aktuálny zoznam cvičiacich: Kleprlík, Krásenský, Marta Nollová, Hanka Řada, Irena Šindelářová a Magdaléna Tinková. Osobné stránky v dodanej kópii sú dostupné iba pre Kleprlíka a Kloudu; odkaz na osobnú stránku nie je dôkaz, že jej súbor je v korpuse. [teachers/index.adoc:16–46; teachers/kleprlud/index.adoc; teachers/kloudkar/index.adoc]
- Kleprlík ponúka konzultácie po dohode e-mailom alebo MS Teams, Klouda po dohode e-mailom. [teachers/kleprlud/index.adoc:8–14; teachers/kloudkar/index.adoc:3–8]

### 2. Hodnotenie a testy (body, termíny, formát, pomôcky, štandardná hlavička zadania doslova, čo sa hodnotí, s odkazmi)

**Body a podmienky podľa stránky predmetu**

- Prvý zápočtový test: 0 až 10 bodov, na vlastnom štvrtom cvičení, teda približne v piatom týždni; početná aj teoretická otázka. [classification.adoc:49–54]
- Druhý zápočtový test: 0 až 30 bodov, mimo vlastného cvičenia, približne v ôsmom a deviatom týždni; zápis v KOS-e, početné aj teoretické otázky. [classification.adoc:77–80]
- Zápočet: najmenej 23 bodov zo 40 z oboch testov spolu. Bonusy za aktivitu tento prah nenahrádzajú. [classification.adoc:23–33,118]
- Opravná zápočtová písomka: iba pri 18 až 22,5 bodu a splnení všetkých štyroch určených teoretických a početných kvízov ku kapitolám 1 a 2. Aspoň polovica bodov z opravnej nastaví semestrálny zisk na 23 a prinesie zápočet. [classification.adoc:125–129,142–153]
- Opravná je plánovaná na prvý týždeň skúškového, 4. až 9. januára; v kontexte harmonogramu ide o rok 2027. Presný dátum má byť oznámený. [classification.adoc:146–147; marast-cvicebnice.md:114]
- Skúška je písomná, 60 základných a 10 bonusových bodov; treba najmenej 30 zo základných 60. Bonusy sa započítajú až po úspešnej skúške. [classification.adoc:163–167]
- Aktivita: najviac 4 bonusové body. Bežne v semestri; výslovná výnimka povoľuje hlásenie chýb skrípt, prezentácií a MARASTu aj počas skúškového. Nie sú nárokovateľné. [classification.adoc:132–138]
- Maximum po úspešnej skúške: 114 bodov. Hodnotenia môžu byť celé alebo polbodové, **nezaokrúhľujú sa**. [classification.adoc:10,16,217]
- Hranice známok: A od 90, B od 80, C od 70, D od 60, E od 50; F pod 50. Podmienku úspešnej skúšky treba splniť samostatne. Slajdy výslovne rozpisujú horné hranice na 89,5/79,5/69,5/59,5, zatiaľ čo tabuľka stránky ich skracuje na celé čísla. [classification.adoc:165–167,217–229; bi-la1-prezentace-00.tex:99–106]
- Naprosté nepravdy môžu byť hodnotené zápornými bodmi. Nie je doložené univerzálne pravidlo odpočtu za každú chybu. [classification.adoc:41]
- Neúčasť alebo neprihlásenie na druhý test znamená 0 bez nároku na náhradný termín; závažné zdravotné výnimky sa riešia individuálne. Pri prvom teste s cvičiacim, pri druhom s Kleprlíkom. [classification.adoc:54,80]
- Absencia na výučbe sama osebe penalizovaná nie je, môže však znamenať stratu príležitosti na aktivitu. [classification.adoc:158–160]
- Uznanie minuloročného zápočtu: žiadosť Kloudovi do 9. októbra 23:59, prenesie sa minuloročný zisk zo zápočtových testov. Nejde o automatické uznanie ľubovoľne starého zápočtu. [classification.adoc:120; bi-la1-prezentace-00.tex:74]

**Termíny a trvanie: čo je skutočne doložené**

- Harmonogram 2026/27 kladie prvé testy podľa dňa cvičenia do týždňov 19. až 23. októbra a 26. až 30. októbra; pondelkové cvičenia majú posun. Druhý test je v tabuľke pri ôsmom a deviatom týždni. [index.adoc:121–184]
- Discordový oznam z 31. 10. 2026 mieša dni a dátumy; autor 1. 11. priznal chybu. Preto dátumy „čtvrtek 18.11.“ a „pátek 19.11.“ nevydávam za overený rozpis. Otvorenie termínov je oznámené 3. 11. o 11:41. [discord-la1.md:11–31]
- Prvý test v októbri 2026: približne 30 minút, pri vyššej náročnosti môžu cvičiaci pridať napríklad päť. [discord-la1.md:384–390]
- Ukážkový druhý test v lekcii 292: 70 minút, tri až štyri príklady, bodové časti 7 + 12 + 11. Je to doložený **formát ukážky**, nie nový overený pokyn k ostrému termínu 2026. [marast-cvicebnice.md:91]
- Archív 2023 až 2025 často používa tri časti 7/12/11, no samotný prepis zaznamenáva výnimky a opravy, napríklad sova 2023 7/7/7. Nevyvodzovať univerzálne bodovanie zo starých fotografií. [testy-la1-2.md:3,6,32–40]
- Ukážková skúška pre prezenčné štúdium: štyri príklady, dve hodiny. Kombinovaná ukážka má osobitne hodinu a 45 minút; tieto údaje sa nesmú zameniť. [marast-cvicebnice.md:93–95]
- Archív skúšok obsahuje iba tri prepísané varianty: Frobenius 17. 12. 2025, Hilbert 26. 1. 2026, Bolzano 2. 2. 2026. Všetky majú štyri hlavné príklady, ale rôzne rozdelenie bodov. [testy-la1-zkouska.md:3–22]
- Harmonogram predpokladá predtermín na konci semestra a skúškový termín každý týždeň 4. januára až 5. februára; v poslednom týždni 8. až 12. februára termín nie je. Ďalší termín v druhom týždni letného semestra je označený ako predbežný. [index.adoc:221–256]
- **Pomôcky:** zdroj nemá pravidlo povoľujúce ani zakazujúce kalkulačku, ťahák, skriptá, mobil či vlastný papier pri písomke. Výrok o tom, ktoré vety možno použiť, nie je povolením priniesť si skriptá. [prehľadané classification.adoc, archívy testov, discord-la1.md a všetky dostupné titulky LA1]

**Štandardná hlavička doslova podľa miestneho prepisu**

Nasledujúce je presné znenie dostupného prepisu, nie nová kontrola pôvodnej fotografie. Dlhší citát je tu zámerne pre výslovnú požiadavku na celú hlavičku.

> Řádně vysvětlete svůj postup tak, aby jej pochopil čtenář, který pouze zná definice a tvrzení z přednášky: Pokud budete například řešit soustavu rovnic, napište, co jsou proměnné a odkud se vzaly koeficienty. Vysvětlete také, jak přesně z výpočtu plynou závěry, které uděláte. V tomto příkladě byste měli demonstrovat své nabyté vědomosti z BI-LA1.

[testy-la1-2.md:3]

Výnimka pre príklad 1 v tom istom prepise:

> V tomto příkladu nemusíte své výpočty komentovat. Avšak znalý čtenář ve vašich výpočtech musí být schopen poznat, jak jste se k výsledku dobrali. Můžete předpokládat, že opravující zná standardní postupy z 2. prosemináře a cvičení k němu.

[testy-la1-2.md:3]

Súbor `testy-la1-zkouska.md` spoločnú štandardnú hlavičku neprepisuje. Nemožno potvrdiť, že vyššie uvedená hlavička platí doslova pre všetky skúškové varianty. Obsahuje konkrétne pokyny pri podotázkach, napríklad formulovať tvrdenie korektne, dokázať ho alebo zapísať riešenie ako varietu. [testy-la1-zkouska.md:8–10,15–16,20–22]

**Čo sa hodnotí**

- Prvý test: matice a operácie, prepis sústavy, GEM a interpretácia počtu riešení, modulo prvočíslo, základné definície a jednoduché dôkazy. [classification.adoc:59–72]
- Druhý test: priestor $T^n$, podpriestor, kombinácie, LN/LZ, obal, báza, dimenzia a súradnice; definície, zdôvodnenia a vybrané dôkazy sú súčasťou požiadaviek. [classification.adoc:85–111]
- Skúška: hodnosť a regularita, Frobeniova veta a variety, determinant, vlastné čísla a diagonalizácia vrátane uvedených definícií a dôkazov. [classification.adoc:174–209]
- Archív potvrdzuje spojenie formulácie definície, formulácie vety, dôkazu a výpočtu v jednom teste; samotné napočítanie nestačí. [testy-la1-zkouska.md:9–10,15–16,21–22]
- Pri dôkaze špeciálneho prípadu sa nesmie odvolať na dokazovanú všeobecnú vetu ani jej dôsledky, ak to zadanie vylučuje. Nejde o všeobecný zákaz používania už dokázaných viet pri výpočtoch. [testy-la1-2.md:12,23,29; discord-la1.md:438–440]

### 3. Čo prednášajúci chcú na teste vidieť a čo zakazujú (celokurzové, citácie s časom; kto čo)

- **Klouda, prepojenie teórie a výpočtu:** „To bude platit obecně“; postup zadanie → matica → GEM → áno/nie bez vysvetlenia nestačí. Vysvetliť pôvod matice a súvis s definíciou, pokiaľ zadanie výslovne nepovolí komentár vynechať. [la1-P4-par2-klouda 1:16:38–1:17:27]
- **Krásenský, to isté konkrétne:** „To, že vezmu vektory, napíšu je do sloupců matice a gemuju, tak to není vůbec dostatečné zdůvodnění.“ Treba povedať, že neznáme sú koeficienty kombinácie a odkiaľ sústava vznikla. [la1-P5-par3-krasensky 28:30–28:57]
- **Kleprlík, rovnaký dôraz:** „Ale mějte na paměti, proč. Napište to do toho testu, až to budete zdůvodňovat.“ Ide o prevod lineárnej nezávislosti na sústavu s vektormi v stĺpcoch. [la1-P5-par1-kleprlik 22:10–22:49]
- **Kleprlík, význam namiesto doslovného učenia:** „my to chceme, aby to mělo stejný význam, jako v těch skriptech.“ Pripúšťa slovnú aj symbolickú formuláciu tej istej definície. [la1-P4-par1-kleprlik 1:08:12–1:10:15]
- **Krásenský, rovnaký princíp:** „Můžou tam být prohozeny nějaké slova, ale matematicky to musí znamenat tady tyhle věci.“ Dodáva požiadavku používať kurzové značenie a terminológiu. [la1-P6-par3-krasensky 55:28–56:07]
- **Klouda, definícia nie je ekvivalentná veta:** „Definice je jenom jedna.“ Pri regularite požaduje existenciu inverzie; nenulový determinant ani plná hodnosť nemajú nahradiť požadovanú definíciu. [la1-P7-par2-klouda 47:46–49:09]
- **Kleprlík, rovnaké rozlíšenie pri LN/LZ:** „ale není to definice.“ Vyjadriteľnosť jedného vektora kombináciou ostatných je v tomto výklade dokazovaná veta, nie východisková definícia. [la1-P4-par1-kleprlik 1:10:35–1:11:12]
- **Krásenský, predpoklady viet:** „co teda vlastně ty tři matice musí splňovat“. Pri požiadavke vysloviť asociativitu nestačí neurčité „pokud má aspoň jedna strana smysl“; treba uviesť príslušné rozmery a prostredie. [la1-P2-par3-krasensky 1:11:26–1:12:00]
- **Kleprlík, konkrétny príklad nie je všeobecný dôkaz:** „Důkaz s příkladem není dobrý“. Jeho číselný náznak na vysvetlenie nemajú študenti prebrať ako dôkaz do písomky. [la1-P5-par1-kleprlik 50:38–51:13]
- **Krásenský, čísla tvrdení netreba memorovať:** „Nemusíte si samozřejmě pamatovat“ presné číslo pozorovania; má byť zrejmé, akú vlastnosť študent používa a prečo záver platí. [la1-P6-par3-krasensky 09:31]
- **Krásenský, vlastná kontrola:** „tohle nemůže být správně, ale už nemám čas to opravit“. Ak výsledok odporuje teórii, aspoň tento rozpor výslovne pomenovať; najlepšie je výpočet opraviť. Nie je sľúbený konkrétny počet bodov. [la1-P6-par3-krasensky 26:56–27:26]
- **Klouda, vyberať priamy dôkaz:** „součin je regulární, protože jeho inverze vypadá takhle“. Pri regularite súčinu odporúča konštrukciu inverzie namiesto zbytočne zložitej cesty cez hodnosť. Ide o radu k tomuto typu, nie zákaz iných korektných dôkazov. [la1-P7-par2-klouda 26:19–27:17]
- **Kleprlík, terminológia:** „Takže matici nevyřešíte.“ Rieši sa sústava alebo príslušná rovnica, matica sa napríklad upravuje. [la1-P6-par1-kleprlik 1:28:15]
- **Krásenský, čitateľnosť:** „ta písme napsat nějakým způsobem odlišně“. Rozlišovať rukou písané $m$ a $n$, aby bolo jasné, o ktorý rozmer ide. [la1-P2-par3-krasensky 1:03:05–1:03:33]
- **Proseminár 3, autor v prepise neidentifikovaný:** odporúča výpočet inverzie súbežným GEM na matici a jednotkovej matici; „Když pochopíte, proč funguje, tak je to skvělý.“ Zdôrazňuje vzťah k definícii inverzie a riešeniu viacerých sústav. [la1-prosem2223-03 1:11:58–1:13:28]
- **Proseminár 4, autor neidentifikovaný:** farebné rozlišovanie hlavných a vedľajších stĺpcov je pedagogická pomôcka; z poznámky o nákupe farbičiek nevyplýva povinná farebná úprava písomky. [la1-prosem2223-04 25:34–26:04]

**LA1 desatero ako celokurzový súhrn**

1. Vopred pozrieť príslušný proseminár. [chap-12-znaceni.woo:946]
2. „Vždy vím, co a proč počítám.“ [chap-12-znaceni.woo:950]
3. „vyřeším matici“ nedáva zmysel. [chap-12-znaceni.woo:954]
4. „Dám vektory nějak do matice a GEMuji“ nie je postup. [chap-12-znaceni.woo:958]
5. Dimenzia $m$ neznamená, že podpriestor musí byť $T^m$. [chap-12-znaceni.woo:962]
6. Sarrusovo/krížové pravidlo neplatí pre $4\times4$ a väčšie matice. [chap-12-znaceni.woo:966]
7. Vlastný vektor musí byť nenulový. [chap-12-znaceni.woo:970]
8. Nepliesť definíciu a tvrdenie. [chap-12-znaceni.woo:974]
9. Každé tvrdenie má predpoklady a záver. [chap-12-znaceni.woo:978]
10. „Důkazy nevynechávám, jsou to lehké body.“ Je to motivačná formulácia skrípt, nie záruka ľahkosti každého dôkazu. [chap-12-znaceni.woo:982]

### 4. Značenie kurzu (tabuľka symbol | význam | odkaz do chap-12-znaceni.woo; plus čo sa nepoužíva)

V tabuľke sú makrá WooWoo prepísané do bežného TeX-u: napríklad `\vx` ako $\mathbf{x}$, `\mA` ako $\mathbf{A}$, `\BB` ako $\mathcal B$. Rozlíšenie vektor/matica/skalár sa zachováva. Odkazy vedú na presný zdrojový riadok.

| Symbol | Význam | Odkaz do chap-12-znaceni.woo |
|---|---|---|
| $:=$ (makro `\ceq`) | Definičná rovnosť, ľavá strana je definovaná pravou | chap-12-znaceni.woo:38 |
| $\wedge$, $\vee$ | Konjunkcia, disjunkcia | chap-12-znaceni.woo:46 |
| $\Rightarrow$, $\Leftrightarrow$ | Implikácia, ekvivalencia | chap-12-znaceni.woo:62 |
| $\forall$, $\exists$ | Všeobecný a existenčný kvantifikátor | chap-12-znaceni.woo:78 |
| $\{a,b,c\}$ | Množina s uvedenými prvkami | chap-12-znaceni.woo:94 |
| $\{x\in M\mid P(x)\}$ | Prvky množiny spĺňajúce podmienku | chap-12-znaceni.woo:102 |
| $x\in M$, $x\notin M$ | Príslušnosť a nepríslušnosť prvku | chap-12-znaceni.woo:110 |
| $A,B,C,\dots$ | Veľké netučné písmená obvykle označujú množiny | chap-12-znaceni.woo:118 |
| $A\subseteq B$ | Podmnožina, pripúšťa sa rovnosť | chap-12-znaceni.woo:126 |
| $\emptyset$, $A\cup B$, $A\cap B$ | Prázdna množina, zjednotenie, prienik | chap-12-znaceni.woo:134 |
| $A\setminus B$, $A\times B$ | Rozdiel množín, karteziánsky súčin | chap-12-znaceni.woo:158 |
| $\min A$ | Najmenší prvok množiny | chap-12-znaceni.woo:174 |
| $\mathbb N=\{1,2,3,\ldots\}$ | Prirodzené čísla, v tejto konvencii bez nuly | chap-12-znaceni.woo:182 |
| $\mathbb Z$, $\mathbb Q$, $\mathbb R$, $\mathbb C$ | Celé, racionálne, reálne a komplexné čísla | chap-12-znaceni.woo:190 |
| $\mathbb Z_n$ | Zvyšky 0 až n mínus 1 s operáciami modulo n | chap-12-znaceni.woo:198 |
| $\mathbb R^2$, $\mathbb R^3$ | Dvojice a trojice reálnych čísel | chap-12-znaceni.woo:222 |
| $n!$, $\sum_{i=1}^n x_i$, $\prod_{i=1}^n x_i$ | Faktoriál, súčet, súčin | chap-12-znaceni.woo:238 |
| $\circ$ | Zástupný symbol grupovej operácie | chap-12-znaceni.woo:288 |
| $+_p$, $\cdot_p$ | Sčítanie a násobenie modulo p | chap-12-znaceni.woo:296 |
| $\alpha,\beta,\gamma,\dots$ | Skaláry | chap-12-znaceni.woo:312 |
| $\lambda$ | Obvykle vlastné číslo | chap-12-znaceni.woo:320 |
| $\pi,\sigma,\tau$ | Permutácie v kapitole o determinante | chap-12-znaceni.woo:328 |
| $\theta$ | Nulový vektor | chap-12-znaceni.woo:336 |
| $\Theta$ | Nulová matica | chap-12-znaceni.woo:344 |
| $\tau_{ij}$ | Transpozícia ako permutácia, nie transponovanie matice | chap-12-znaceni.woo:352 |
| $\operatorname{sgn}\pi$ | Znamienko permutácie | chap-12-znaceni.woo:360 |
| $\nu_a(\lambda)$, $\nu_g(\lambda)$ | Algebraická a geometrická násobnosť vlastného čísla | chap-12-znaceni.woo:368 |
| $\mathcal B$ | Súbor vektorov, často báza | chap-12-znaceni.woo:384 |
| $\mathcal E$, $\mathcal E_n$ | Štandardná báza priestoru T na n-tú | chap-12-znaceni.woo:392 |
| $\mathcal X$, $\mathcal Y$, $\mathcal Z$ | Súbory vektorov | chap-12-znaceni.woo:400 |
| $m,n$ | Prirodzené čísla | chap-12-znaceni.woo:408 |
| $p$ | Obvykle prvočíslo, najmä v označení konečného telesa | chap-12-znaceni.woo:416 |
| $x,y,z,u,v$; $x_1,x_2,\dots$ | Zložky vektora alebo neznáme | chap-12-znaceni.woo:424 |
| $a_{ij}$, $b_{ij}$ | Prvky matice alebo koeficienty sústavy | chap-12-znaceni.woo:440 |
| $\hat n$ | Množina indexov 1 až n | chap-12-znaceni.woo:448 |
| $(x,y)$, $(x,y,z)$, $(x,y,z,u,v)$ | Zložky vektorov v priestoroch príslušnej dĺžky | chap-12-znaceni.woo:456 |
| $(x_1,\dots,x_n)$ | Vektor z priestoru T na n-tú | chap-12-znaceni.woo:480 |
| $P,Q$ | Často podpriestory | chap-12-znaceni.woo:488 |
| $S$ | Množina riešení sústavy | chap-12-znaceni.woo:496 |
| $T$ | Komutatívne teleso, typicky racionálne, reálne, komplexné alebo zvyškové modulo prvočíslo | chap-12-znaceni.woo:504 |
| $W$ | Často varieta | chap-12-znaceni.woo:512 |
| $S_0$ | Množina riešení pridruženej homogénnej sústavy | chap-12-znaceni.woo:520 |
| $T^{m,n}$ | Matice s m riadkami a n stĺpcami nad T | chap-12-znaceni.woo:528 |
| $T^m=T^{m,1}$ | Vektory ako stĺpcové matice | chap-12-znaceni.woo:536 |
| $P\subset\subset T^m$ | Kurzový symbol pre podpriestor | chap-12-znaceni.woo:544 |
| $\dim P$ | Dimenzia podpriestoru alebo variety | chap-12-znaceni.woo:552 |
| $Z(W)$ | Zameranie variety | chap-12-znaceni.woo:560 |
| $\mathbf x,\mathbf y,\mathbf z,\mathbf u$ | Tučné malé písmená pre vektory | chap-12-znaceni.woo:568 |
| $\mathbf x$, $\mathbf b$ | Často vektor neznámych a vektor pravých strán | chap-12-znaceni.woo:576 |
| $\mathbf e_1,\mathbf e_2,\dots$ | Vektory štandardnej bázy | chap-12-znaceni.woo:592 |
| $\mathbf x+\mathbf y$, $\alpha\mathbf x$ | Súčet vektorov, násobenie vektora skalárom zľava | chap-12-znaceni.woo:600 |
| $\mathbf x+P$ | Súčet vektora a množiny, varieta pri podpriestore P | chap-12-znaceni.woo:616 |
| $(\mathbf x,\mathbf y,\mathbf z)$; $(\mathbf x_1,\dots,\mathbf x_n)$ | Súbor vektorov | chap-12-znaceni.woo:624 |
| $\langle\mathbf x,\mathbf y,\mathbf z\rangle$; $\langle\mathbf x_1,\dots,\mathbf x_n\rangle$ | Lineárny obal súboru | chap-12-znaceni.woo:640 |
| $(\mathbf x)_{\mathcal B}$ | Súradnice vektora vzhľadom na bázu | chap-12-znaceni.woo:656 |
| $\mathbf A,\mathbf B,\mathbf C$ | Tučné veľké písmená pre matice | chap-12-znaceni.woo:664 |
| $\mathbf E$, $\mathbf E_n$ | Jednotková matica n krát n | chap-12-znaceni.woo:672 |
| $\alpha\mathbf A$ | Násobok matice prvkom telesa | chap-12-znaceni.woo:680 |
| $\mathbf A^T$ | Transponovaná matica | chap-12-znaceni.woo:688 |
| $\mathbf A^k$ | K-tá mocnina matice | chap-12-znaceni.woo:696 |
| $\mathbf A^{-1}$ | Inverzná matica | chap-12-znaceni.woo:704 |
| $h(\mathbf A)$ | Hodnosť matice | chap-12-znaceni.woo:712 |
| $\det\mathbf A$, $\|\mathbf A\|$ | Determinant matice | chap-12-znaceni.woo:720 |
| $\sigma(\mathbf A)$ | Spektrum matice | chap-12-znaceni.woo:728 |
| $p_{\mathbf A}$ | Charakteristický polynóm | chap-12-znaceni.woo:736 |
| $\mathbf A_{i:}$ | I-ty riadok matice | chap-12-znaceni.woo:744 |
| $\mathbf A_{:j}$ | J-ty stĺpec matice | chap-12-znaceni.woo:752 |
| $(\mathbf A)_{ij}$, $a_{ij}$ | Prvok na pozícii ij | chap-12-znaceni.woo:760 |
| $\mathbf A(k,l)$ | Matica po vynechaní k-teho riadka a l-teho stĺpca | chap-12-znaceni.woo:768 |
| $\mathbf A+\mathbf B$ | Súčet matíc | chap-12-znaceni.woo:776 |
| $\mathbf A\cdot\mathbf B$, $\mathbf A\mathbf B$ | Súčin matíc | chap-12-znaceni.woo:784 |
| $\mathbf A\sim\mathbf B$ | Prevod z A na B pomocou GEM, nie symbol maticovej podobnosti | chap-12-znaceni.woo:792 |
| $(\mathbf A\mid\mathbf b)$ | Rozšírená matica sústavy | chap-12-znaceni.woo:800 |
| $\mathbf A\mathbf x=\mathbf b$ | Maticový zápis sústavy | chap-12-znaceni.woo:808 |
| $\mathbb Z_p$ | Špeciálne konečné teleso | chap-12-znaceni.woo:816 |
| $\mathbb Z_p^{m,n}$, $\mathbb R^{m,n}$, $\mathbb C^{m,n}$ | Matice nad príslušnými telesami | chap-12-znaceni.woo:824 |
| $\mathrm{HST}$, $\mathrm{rHST}$ | Horný stupňovitý a redukovaný horný stupňovitý tvar | chap-12-znaceni.woo:870 |
| $\mathrm{GEM}$; $(G1),(G2),(G3)$ | Gaussova eliminácia a jej operácie | chap-12-znaceni.woo:886 |
| $\mathrm{LN}$, $\mathrm{LZ}$ | Lineárne nezávislý a lineárne závislý súbor | chap-12-znaceni.woo:902 |
| $\mathrm{SLR}$, $\mathrm{VP}$ | Sústava lineárnych rovníc, vektorový priestor | chap-12-znaceni.woo:918 |

**Čo nepoužívať a čo z neprítomnosti nevyvodzovať**

- Typ matice písať $\mathbf A\in T^{m,n}$, nie $\mathbf A^{m,n}$. Zákaz je výslovný v skriptách aj u Krásenského. [chap-02.woo:1313–1320; la1-P2-par3-krasensky 47:33–48:43]
- Pre jednotlivý prvok preferovať $(\mathbf A)_{ij}$ alebo $a_{ij}$; výskyt $\mathbf A_{ij}$ či $A_{ij}$ v kurzových materiáloch majú študenti hlásiť ako chybu. [chap-02.woo:1310; chap-12-znaceni.woo:760–764]
- Súčty skalár + vektor a násobenie vektor × vektor tu zavedené nie sú; skriptá varujú aj pred zápisom „vektor krát skalár“. Používať zavedené násobenie skalár × vektor. [chap-03.woo:302–308]
- Značenie $\operatorname{rank}$, $I$ pre jednotkovú maticu alebo $\operatorname{span}$ nepatrí do tejto tabuľky; z toho samotného nevyplýva výslovný zákaz každého z nich. Pre autora lekcií sú doložené kurzové voľby $h$, $\mathbf E$ a uhlové zátvorky. [chap-12-znaceni.woo:640–674,712–716]
- Pomocné regulárne matice realizujúce GEM nemajú pevné univerzálne mená: Krásenský výslovne hovorí, že ich netreba označovať rovnako ako on. [la1-P8-par3-krasensky 32:30–33:46,39:34]

### 5. Terminológia (česky | slovensky | kde je definované), vrátane zakázaných všeobecných slov, ak ich kurz výslovne odmieta

Čísla definícií sú prevzaté z `cisla/la1-kap1.tsv` až `la1-kap4.tsv`, nie dopočítané z poradia v zdroji. Pri neskorších kapitolách sa uvádza iba názov a riadok, pretože ich číselné tabuľky v tomto adresári nie sú.

| Česky | Slovensky | Kde je definované |
|---|---|---|
| grupa | grupa | Definice 1.2, chap-02.woo:244 |
| těleso | teleso | Definice 1.12, chap-02.woo:668 |
| matice, typ/rozměr matice | matica, typ/rozmer matice | Definice 1.20, chap-02.woo:1222 |
| řádek/sloupec matice | riadok/stĺpec matice | Definice 1.25, chap-02.woo:1326 |
| vektor, nulová matice | vektor, nulová matica | Definice 1.37, chap-02.woo:1938 |
| soustava lineárních rovnic, maticový zápis | sústava lineárnych rovníc, maticový zápis | Definice 1.39, chap-02.woo:1980 |
| horní stupňovitý tvar | horný stupňovitý tvar | Definice 1.47, chap-02.woo:2529 |
| operace Gaussovy eliminace | operácie Gaussovej eliminácie | Definice 1.54, chap-02.woo:2839 |
| vektorový prostor | vektorový priestor | Definice 2.2, chap-03.woo:188 |
| násobení vektoru skalárem | násobenie vektora skalárom | Definice 2.2, chap-03.woo:197 |
| podprostor | podpriestor | Definice 2.7, chap-03.woo:361 |
| soubor vektorů | súbor vektorov, usporiadaná n-tica vektorov | chap-03.woo:795–797 |
| lineární kombinace | lineárna kombinácia | Definice 2.21, chap-03.woo:820 |
| lineárně nezávislý/závislý soubor | lineárne nezávislý/závislý súbor | Definice 2.23, chap-03.woo:881 |
| lineární obal souboru | lineárny obal súboru | Definice 2.33, chap-03.woo:1194 |
| báze | báza | Definice 2.44, chap-03.woo:1566 |
| dimenze podprostoru | dimenzia podpriestoru | Definice 2.49, chap-03.woo:1758 |
| souřadnice vzhledem k bázi | súradnice vzhľadom na bázu | Definice 2.68, chap-03.woo:2373 |
| hodnost matice | hodnosť matice | Definice 3.1, chap-04.woo:53 |
| inverzní, regulární matice | inverzná, regulárna matica | Definice 3.23, chap-04.woo:914 |
| lineární varieta | lineárna varieta | Definice 4.16, chap-05.woo:971 |
| determinant | determinant | Definícia s labelom dfn-determinant, chap-06.woo:471 |
| vlastní číslo, vlastní vektor | vlastné číslo, vlastný vektor | Definícia dfn-vlastni-cislo-matice, chap-07.woo:282 |
| charakteristický polynom | charakteristický polynóm | Definícia def-char-poly, chap-07.woo:412 |
| algebraická/geometrická násobnost | algebraická/geometrická násobnosť | Definícia dfn-geometricka-asobnost-matice, chap-07.woo:503 |

**Kurzové rozlíšenia, ktoré musí autor lekcie zachovať**

- Súbor je usporiadaná n-tica vektorov, nie automaticky množina ani matica. Množina nemá poradie; kurz pre súbor preferuje slovnú deklaráciu „soubor vektorů z…“. [chap-03.woo:795–817; la1-P4-par2-klouda 1:16:27–1:16:35]
- Lineárny obal vytvára zo súboru množinu, podpriestor je vlastnosť množiny. Tak odpovedal vyučujúci na Discorde predmetu na otázku študenta z októbra 2026. [discord-la1.md:408–418]
- LN/LZ je vlastnosť **súboru**. Fráza „maximální počet LN řádků“ je v skriptách výslovne odmietnutá; jednotlivý riadok sa takto nenazýva LN. [chap-04.woo:435–436]
- Hodnosť je definovaná ako dimenzia lineárneho obalu súboru riadkov, chápaných ako vektory po transpozícii. Nie ako počet nenulových riadkov po GEM; to je výpočet opretý o tvrdenia. [chap-04.woo:58–75,430–431]
- Dimenzia je v tomto kurze definovaná pomocou existencie LN súboru dĺžky $d$ a závislosti všetkých súborov dĺžky $d+1$; nulový podpriestor má osobitný prípad $0$. Počet vektorov bázy je následný vzťah, nie požadované východiskové znenie definície. [chap-03.woo:1763–1778; la1-P6-par3-krasensky 55:28–56:07]
- Kurz nepripúšťa bázu nulového podpriestoru cez prázdny súbor: Kleprlík to výslovne vysvetľuje, Krásenský nulový podpriestor uvádza ako výnimku existencie bázy. Nepreberať bez upozornenia všeobecnejšiu konvenciu z inej učebnice. [la1-P5-par1-kleprlik 1:28:58; la1-P6-par3-krasensky 06:50]
- Skalárny súčin nie je násobenie vektora skalárom. Vyučujúci na Discorde predmetu tento názov pre škálovanie výslovne odmieta; skriptá skalárny a vektorový súčin vyčleňujú mimo náplne kurzu. [discord-la1.md:420–426; chap-03.woo:307]
- Výslovne odmietnuté frázy: „vyřeším matici“, „řeším soubor“. Pomenovať riešenú otázku, napríklad sústavu alebo hľadané koeficienty. [chap-04.woo:440–441]
- „Dám vektory nějak do matice a GEMuji“ nie je vysvetlený postup. Nejde o zákaz GEM, ale o chýbajúci cieľ a matematický dôvod. [chap-12-znaceni.woo:958; la1-P4-par2-klouda 1:16:38–1:17:27]
- Všeobecný zákaz slov ako „zrejmé“, „triviálne“, „nejako“ alebo slovného vysvetľovania zdroj nemá; nepridávať vlastný zoznam takých slov. Generické slová, ktoré kurz nepoužíva (pivot, schodovitý tvar, rank, span …), sú v `data/Terminology.kt` a v appke platia (ZASADY, zásada 6).

### 6. Rozdiely medzi paralelkami (tempo, štýl, dôraz)

Ide o pozorovania z dostupných prepisov, nie rebríček kvality alebo prísnosti. Stránka sľubuje približne totožný obsah všetkých paraleliek. [index.adoc:24]

- **Tempo nie je totožné:** Kleprlík v P8 už formuluje Frobeniovu vetu a prechádza k varietám, Krásenský v P8 ešte prepája regularitu, GEM a hľadanie inverzie. Klouda už na konci P7 hovorí o dimenzii homogénneho riešenia; P8 chýba. [la1-P8-par1-kleprlik 10:19,16:42; la1-P8-par3-krasensky 10:03,43:10–45:40; la1-P7-par2-klouda 1:35:05]
- **Kleprlík:** v sledovaných miestach používa programátorskú motiváciu a číselný náznak pred všeobecným zápisom; výslovne odlišuje pedagogický náznak od dôkazu do testu. Hovorí aj o praktických stratégiách a častých termínoch. [la1-P4-par1-kleprlik 1:08:33–1:08:44; la1-P5-par1-kleprlik 50:38–51:13; la1-P8-par1-kleprlik 09:52]
- **Klouda:** v citovaných pasážach zdôrazňuje, ktorá formulácia je definícia, ako výpočet súvisí s definíciou a ako vybrať jednoduchý dôkaz. Práca s maticami zahŕňa aj vysvetlenie dvojbodkovej notácie cez programátorskú analógiu. [la1-P7-par2-klouda 26:19–27:17,47:46–49:09; la1-P4-par2-klouda 1:16:38–1:17:27; la1-P2-par2-klouda 1:16:47–1:17:14]
- **Krásenský:** v sledovaných úsekoch explicitne rozoberá typy objektov a rozmerové predpoklady, čitateľnosť zápisu a kontrolu výsledku proti teórii. Pripúšťa vlastné slová pri zachovaní významu. [la1-P2-par3-krasensky 47:33–48:43,1:03:05–1:03:33,1:11:26–1:12:00; la1-P6-par3-krasensky 27:26,55:28–56:07]
- **Konkrétny rozdiel v podrobnosti odpovede:** Klouda pri podpriestore pripúšťa slovné uvedenie neprázdnosti a uzavretosti; Kleprlík odmieta skončiť iba pri pomenovaní uzavretosti bez vysvetlenia jej významu. Bez opravenej rovnakej odpovede od oboch nemožno potvrdiť rozdielne bodovanie. Pre lekciu je bezpečne doložená explicitná formulácia podmienok z definície. [la1-P4-par2-klouda 42:11–42:26; la1-P4-par1-kleprlik 24:48–25:29]
- **Poradie výkladu:** Krásenský P6 výslovne mení poradie oproti slajdom a ide skôr k súradniciam; tematický inventár sám tento jemný rozdiel nezachytí. [la1-P6-par3-krasensky 10:01–10:17,18:03]
- Spoločný základ je podstatnejší než štýl: definície s presným významom, odôvodnený prechod na sústavu a interpretácia výsledku. Rozdielnu prísnosť opravovania medzi troma prednášajúcimi zdroj nemá doloženú. [citácie v oddiele 3; discord-la1.md:735]

### 7. MARAST a iné nástroje (ako sa používajú; bez znení otvorených kvízov)

- MARAST oddeľuje **Kvízy**, **Cvičebnicu** a **Lekcie**. Lekcie obsahujú výklad, ciele, príklady a ukážkové písomky, nie iba hodnotené pokusy. [marast-cvicebnice.md:6–12]
- Odporúčaný sled podľa organizácie výučby: prednáška/skriptá → príslušný proseminár pred cvičením → samostatné počítanie → otázky a príklady na cvičení. Kvízy priebežne kontrolujú pripravenosť a môžu podmieňovať opravnú písomku. [index.adoc:26–29; teachers/kleprlud/index.adoc:19–31; classification.adoc:125–129]
- Lekcie k témam: 255 telesá a sústavy, 256 základné pojmy, 258 hodnosť a regularita, 260 Frobeniova veta/variety/determinant, 261 vlastné čísla a diagonalizácia. [marast-cvicebnice.md:20–26]
- Ukážkové testy: lekcia 286 prvý zápočet, 292 druhý zápočet, 262 skúška. Materiály 257 a 293 sú osobitne označené pre kombinované štúdium. [marast-cvicebnice.md:21–29]
- Dôležitý nesúlad: Kleprlíkova stránka pri hodnosti odkazuje na 257, ale prečítaný obsah MARASTu označuje 257 ako kombinovanú zápočtovú ukážku a hodnosť ako 258. Navigovať podľa názvu a obsahu, nepreberať slepo ID. [teachers/kleprlud/index.adoc:105–108; marast-cvicebnice.md:24,28]
- Cvičebnica: hľadať podľa ID, témy, tagu či obťažnosti. Rozlišovať príklady s postupom a bez neho; textová odpoveď automaticky neznamená kompletný vysvetlený postup. Filter môže obmedziť počet zobrazených úloh. [marast-cvicebnice.md:9–16]
- Ukážková druhá písomka upozorňuje, že videá nie sú vzorom postačujúcim na plný bodový zisk. Pre autorov lekcií je rozhodujúce aj slovné zdôvodnenie a prepojenie s teóriou. [marast-cvicebnice.md:91]
- Kvízy sú nebodované, ale splnenie štyroch určených má význam pre opravu zápočtu. Označenie „nepovinné“ preto neznamená nulový dôsledok ich vynechania. [classification.adoc:28–30,125–129]
- Lokálne čítanie MARASTu zo 6. 11. 2026 zachytáva teoretické aj početné kvízy a opakovania. Pri kapitole 2 boli termíny 20. 11. 2026 22:01 a 22:02; nejde o znovu overený stav k 17. 11. [marast.md:1–3,20–35]
- Prehľadové pomery ani percentá profilu sa nesmú vydávať za klasifikačné body kurzu alebo automaticky za počet pokusov. Samotný zdroj ich presný význam nepotvrdzuje. [marast.md:9]
- Znenia ani parafrázy otázok otvorených kvízov sa do tohto výťahu nepreberajú. Vystupuje iba ich existencia, typ, účel a termín z prehľadu. [BRIEF.md, Testy a úlohy]
- Grades/Klasifikace eviduje body; Assignments sprístupňuje opravené písomky. Chýbajúce zverejnenie v Assignments nemusí znamenať nenapísaný test. [classification.adoc:10–13; discord-la1.md:33–43]
- V Assignments môže byť opravujúci komentár dostupný cez osobitný náhľad opravy. Toto je historická používateľská rada z januára 2026, nie nová kontrola rozhrania. [discord-la1.md:452–458]
- KOS je miesto prihlásenia na druhý zápočtový test. Presný platný termín treba odlíšiť od orientačného harmonogramu a pomiešaného oznamu. [classification.adoc:77; discord-la1.md:11–31]
- Skriptá s vlastnými poznámkami sú hlavný materiál; MARAST a videá dopĺňajú výklad. Iné zdroje môžu mať iné značenie, rozsah a poradie. Slajdy uvádzajú LLM s výhradou overovania odpovedí a 3Blue1Brown ako intuíciu, nie náhradu učiva. [bi-la1-prezentace-00.tex:165–185]
- Hlásenie chýb ide cez issues a môže priniesť bonus za aktivitu; nie je to nárok na bod za každé hlásenie. [classification.adoc:137–138]

### 8. Nejasné a chýbajúce

- **Presné termíny druhého testu 2026:** zdroj nemá spoľahlivo opravený rozpis z KOS-u. Discordový autor uznal pomiešanie dátumov; údaj nemožno potichu opraviť podľa kalendára. [discord-la1.md:11–31]
- **Pomôcky a technická organizácia:** pravidlá kalkulačky, ťahákov, papiera, dokladov a mobilov sa v prehľadaných zdrojoch nenašli. Trvanie druhého testu a skúšky je doložené ukážkami, nie osobitným potvrdením každého termínu 2026. [marast-cvicebnice.md:91–98]
- **Hlavička skúšky:** zdroj nemá jej úplný spoločný prepis. Hlavička druhého zápočtu pochádza zo súhrnného prepisu fotografií; pôvodné obrazové zadania sa pri tejto úlohe neoverovali. [testy-la1-2.md:3; testy-la1-zkouska.md:1–3]
- **Chýbajúce titulky:** P1 všetkých paraleliek, Klouda P8, P9 a ďalej; prosemináre 2021/22 a prvé dva 2022/23. P5 Krásenský a P7 Klouda sú označené ako čiastočné. [INDEX.md:40–69]
- **Proseminárny autor:** priama sebaprezentácia v dostupných začiatkoch chýba. Discord predmetu spája vybrané proseminárne výklady s jedným z prednášajúcich, nie spoľahlivo s každým videom. [discord-la1.md:703–717]
- **Časová vrstva:** rozvrh stránky, staré slajdy a archívne testy nie sú ten istý beh. Záver, že stránka je aktualizovaná pre 2026/27, má oporu aj v lokálnom čítaní Courses zo 6. 11. 2026. [marast-cvicebnice.md:100–130]
- **Výnimka v bonusoch:** stránka najprv obmedzuje aktivitu na semester a následne výslovne povoľuje hlásenie chýb aj vo skúškovom. Zachované ako výnimka, nie zamlčaná kolízia. [classification.adoc:136–137]
- **Vnútorné opravy archívu:** úvodná oprava z 10. 11. 2026 má pri variante medvěd prednosť pred staršou hranatou poznámkou, ktorá ešte pochybuje o spoločnej variante; pri sove 2023 zostáva rozpor prepísaných bodov. [testy-la1-2.md:6,29,37–40]
- **Historické pravidlá z Discordu:** údaj o piatich zo šiestich kvízov nie je pravidlom 2026; aktuálna lokálna klasifikácia vyžaduje štyri konkrétne kvízy. [discord-la1.md:376–382; classification.adoc:129]
- **Ústna skúška/rozstřel:** pre BI-LA1 zdroj nemá spoľahlivý opis takého formátu. Pravidlá hovoria o písomnej skúške, nájdený podrobný príbeh na Discorde bol o štátniciach. [classification.adoc:165; discord-la1.md:74,734]
- **Prísnosť paraleliek:** nie sú dostupné porovnateľné opravy rovnakých odpovedí ani dôkaz rozdielnej bodovej politiky. Uvedené rozdiely sú vo výklade a formulácii odporúčaní. [discord-la1.md:735; oddiel 6]
- **Automatický prepis:** nevizuálne „tohle“ bez identifikovateľného objektu a skomolené matematické výrazy nie sú podkladom na vymyslený presný zákaz. Napríklad Kloudovo „Nepište nám tohle“ je významovo overené samostatným výslovným varovaním skrípt. [la1-P2-par2-klouda 1:18:10–1:18:38; chap-02.woo:1320]

### 9. Surové zdroje

Korene ciest: `K = kontext`, `S = K/skripta/bi-la1-master`, `D = zdroje`. V citáciách krátke meno označuje súbor v týchto koreňoch.

- BRIEF.md: pravidlá práce, citačný formát, zákaz otvorených kvízov.
- INDEX.md: inventár LA1 riadky 40 až 69, mená, témy, stav prepisov, ID nahrávok.
- index.adoc: rozvrh, organizácia, harmonogram, odkazy na staršie videá.
- classification.adoc: celé pravidlá, hlavne 10 až 41, 49 až 167, 174 až 229.
- teachers/index.adoc, Kleprlík, Klouda: kontakty, forma výučby, odkazy na lekcie; prečítaný aj teachers/ex-teachers/index.adoc.
- Úvod, chap-01.woo: 20 až 41 a podpis 73. Jeho kópia v S/src/textbook je obsahovo totožná.
- Značenie a desatero: celá kapitola, desatero 936 až 982. Kópia v S/src/textbook je obsahovo totožná; tabuľka vyššie odkazuje do nej.
- Organizačné slajdy 00: 14 až 117, 160 až 185, motivácia 192 až 223.
- testy-la1-2.md: hlavička, výnimka, bodovanie a revízia 3 až 6; archív 9 až 85.
- testy-la1-zkouska.md: rozsah archívu a tri varianty 3 až 22.
- marast.md: organizačný prehľad LA1, najmä 1 až 35; bez preberania znení kvízov.
- marast-cvicebnice.md: navigácia 6 až 29, formáty testov 89 až 98, Courses 100 až 130, obmedzenia 158 až 168.
- discord-la1.md: termíny a zverejňovanie 11 až 56; definície 76 až 122; aktuálne testy a terminológia 384 až 440; nástroje 452 až 458; historické rady 559 až 589; organizácia 693 až 717; obmedzenia 732 až 736.
- cisla/la1-kap1.tsv, kap2, kap3, kap4: overené čísla definícií.
- Dodatočné presné definície a zákazy overené v chap-02.woo, chap-03.woo, chap-04.woo, chap-05.woo, chap-06.woo, chap-07.woo.

**Prehľadané titulky:** všetkých 20 súborov P2 až P8 a tri dostupné prosemináre v K/txt. Vyhľadávanie zahŕňalo test, skúšku, body, „na testu“, „do testu“, „chceme“, „nepište“, „musíte zdůvodnit“, značenie a postupy; kandidáti na celokurzové pravidlá sa čítali s okolitým kontextom. Výskyty vzťahujúce sa iba na konkrétnu matematickú úlohu nie sú automaticky celokurzové pravidlá.

- Kleprlík P2 až P8: hlavné citované úseky P4 16:20 až 17:00, 24:48 až 25:29, 1:08:12 až 1:11:12; P5 22:10 až 22:49, 50:38 až 51:13, 1:15:51 až 1:16:35, 1:28:58; P6 1:28:15; P8 09:52 až 16:42. txt/la1-P4-par1-kleprlik.txt
- Klouda P2 až P7: P2 1:16:41 až 1:18:38; P4 41:51 až 43:38, 1:15:30 až 1:17:27; P7 26:19 až 27:17, 46:00 až 49:09, 1:35:05. txt/la1-P7-par2-klouda.txt
- Krásenský P2 až P8: P2 46:34 až 48:43, 1:03:05 až 1:03:33, 1:11:26 až 1:12:00; P5 28:30 až 28:57; P6 06:50, 09:31 až 10:17, 26:56 až 27:26, 55:28 až 56:07; P8 10:03, 32:30 až 33:46, 39:34, 43:10 až 45:40. txt/la1-P6-par3-krasensky.txt
- Proseminár 3: 00:30 až 01:30, 1:11:58 až 1:13:28. txt/la1-prosem2223-03.txt
- Proseminár 4: 00:30 až 00:58, 25:34 až 26:04. txt/la1-prosem2223-04.txt
- Proseminár 5: 00:00 až 00:53, kontrola relevantných výskytov v celom prepise. txt/la1-prosem2223-05.txt

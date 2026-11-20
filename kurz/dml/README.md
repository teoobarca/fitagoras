# BI-DML: predmet

Úroveň predmetu pod [docs/ZASADY.md](../../docs/ZASADY.md). Najprv zhrnutie a kapitoly, potom podrobný výťah
(prednášajúci, písomky, čo kto chce a zakazuje, značenie, terminológia) s citáciami. Stav k 17. 11. 2026; výťah overený:
časy všetkých citácií existujú v titulkoch, citáty kontrolované ručne.
Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## Zhrnutie: čo platí v celom predmete

1. **Číslovanie skrípt DML je po druhoch** (Definice 6.3, Věta 6.3, Lemma 6.1 sú rôzne veci); čísla ber z
   [cisla.tsv](cisla.tsv).
2. **Značenie z kurzu:** $\gcd$, $\lcm$ s menom vypísaným slovom, nie NSD/NSN. Dombek: „Značení GCD z angličtiny vy
   možná znáte NCD z češtiny. Tak GCD od teďka.“ [dml-P7-par1-dombek 09:36]; skriptá ani Anki NSD/NSN nemajú.
   Indukcia: ZK, IK, IP (nie „báza indukcie“, „indukčná hypotéza“). Ďalšie: oddiel 4 nižšie.
3. **Dôkaz nie je konštatovanie.** Dombek: „Proč? No, proto.“ a hneď „No, taky do zkoušky nepište, jo.“
   [dml-P7-par1-dombek 1:10:07–1:10:13]. Definície s kvantifikátormi a predpokladmi presne podľa skrípt.
4. **Postupy z prednášok:** REA tabuľkou (Dombek: „preferuju tabulku“ [dml-P7-par1-dombek 55:41]); ostatné postupy
   a kto ich ako robí sú v balíkoch kapitol, oddiel 4.

## Kapitoly

| kap. | balík | skriptá | slajdy, kód rozvrhu | v appke | odprednášané k 17. 11. 2026 |
|---|---|---|---|---|---|
| 2 | [kap2-vyrokova-logika.md](kap2-vyrokova-logika.md) | `chap-02-vyrokova-logika.woo` | `DML_P01`, VL1, VL2 | bez lekcií | áno; VL1 nikde nenahraná |
| 3 | [kap3-predikatova-logika.md](kap3-predikatova-logika.md) | `chap-03-predikatova-logika.woo` | `DML_P02`, PL1, PL2 | bez lekcií | áno |
| 4 | [kap4-dukazy-indukce.md](kap4-dukazy-indukce.md) | `chap-04-dukazy-indukce.woo` | `DML_P03`, MI1 až MI3 | `dml-mi` | áno |
| 5 | [kap5-mnoziny-zobrazeni.md](kap5-mnoziny-zobrazeni.md) | `chap-05-mnoziny-zobrazeni.woo` | `DML_P04`, MZ1, MZ2 | `dml-mz` | áno |
| 6 | [kap6-teorie-cisel.md](kap6-teorie-cisel.md) | `chap-06-tc.woo` | `DML_P05_tc`, TČ1 až TČ4 | `dml-tc` | deliteľnosť až LDR všade; kongruencie a inverzia len Dombek (16. 11.); Fermat, Euler, lineárne kongruencie a CRT ešte nie (balík, časť 2, oddiel 1) |
| 7, 8 | (relácie, kombinatorika) | `chap-07`, `chap-08` | | | nie |

## Paralelky

| paralelka | prednášajúci | kedy |
|---|---|---|
| 1 | Daniel Dombek (organizátor predmetu) | pondelok 14:30, T9:105 |
| 2 | Jan Spěvák (garant) | streda 11:00, TK:BS |
| 3 | Jan Legerský | štvrtok 14:30, T9:155 |

Pozor na čísla prednášok: súbory titulkov majú číslo prednášky z nahrávky. U Dombeka je o jedna menšie než v rozvrhu
appky (28. 9. bol sviatok), preto pri citácii rozhoduje dátum v tabuľke nižšie. 6. prednášku 3. paralelky (29. 10.)
odučil za Legerského záskok, prepis meno skomolí („Jan Skrivák“) [dml-P6-par3-legersky 06:13]. Proseminár: spoločné
video 25/26 (staršie 21/22 bez titulkov); autor sa nepredstavuje.

<details><summary>Nahrávky a titulky (súbor titulkov = meno v citáciách)</summary>

| súbor titulkov | id nahrávky | kto | dátum | téma | titulky | stav nahrávky |
|---|---|---|---|---|---|---|
| dml-P2-par1-dombek | 3674210633@sirius.fit.cvut.cz | Dombek D. | 5. 10. 2026 | Predikátová logika 1 | áno | done |
| dml-P2-par2-spevak | 3675538837@sirius.fit.cvut.cz | Spěvák J. | 30. 9. 2026 | Výroková logika 2, Predikátová logika 1 | áno | done |
| dml-P2-par3-legersky | 3675538849@sirius.fit.cvut.cz | Legerský J. | 1. 10. 2026 | Výroková logika 2 | áno | done |
| dml-P3-par1-dombek | 3674210634@sirius.fit.cvut.cz | Dombek D. | 12. 10. 2026 | Predikátová logika 2, Matematická indukce 1 | áno | done |
| dml-P3-par2-spevak | 3675538838@sirius.fit.cvut.cz | Spěvák J. | 7. 10. 2026 | Predikátová logika 2, Matematická indukce 1 | áno | done |
| dml-P3-par3-legersky | 3675538850@sirius.fit.cvut.cz | Legerský J. | 8. 10. 2026 | Predikátová logika 1 | áno | done |
| dml-P4-par1-dombek | 3674210635@sirius.fit.cvut.cz | Dombek D. | 19. 10. 2026 | Matematická indukce 2 | áno | done |
| dml-P4-par2-spevak | 3675538839@sirius.fit.cvut.cz | Spěvák J. | 14. 10. 2026 | Matematická indukce 2 | áno | done |
| dml-P4-par3-legersky | 3675538851@sirius.fit.cvut.cz | Legerský J. | 15. 10. 2026 | Predikátová logika 2, Matematická indukce 1 | áno | partial |
| dml-P5-par1-dombek | 3674210636@sirius.fit.cvut.cz | Dombek D. | 26. 10. 2026 | Množiny a zobrazení 1 | áno | done |
| dml-P5-par2-spevak | 3675538840@sirius.fit.cvut.cz | Spěvák J. | 21. 10. 2026 | Množiny a zobrazení 1 | áno | done |
| dml-P5-par3-legersky | 3675538852@sirius.fit.cvut.cz | Legerský J. | 22. 10. 2026 | Matematická indukce 2 | áno | done |
| dml-P6-par1-dombek | 3674210637@sirius.fit.cvut.cz | Dombek D. | 2. 11. 2026 | Množiny a zobrazení 2, Teorie čísel 1 | áno | failed |
| dml-P6-par2-spevak | 3675538841@sirius.fit.cvut.cz | Spěvák J. | 4. 11. 2026 | Množiny a zobrazení 2, Teorie čísel 1 | áno | done |
| dml-P6-par3-legersky | 3675538853@sirius.fit.cvut.cz | Legerský J. | 29. 10. 2026 | Matematická indukce 3, Množiny a zobrazení 1 | áno | done |
| dml-P7-par1-dombek | 3674210638@sirius.fit.cvut.cz | Dombek D. | 9. 11. 2026 | Teorie čísel 2 | áno | done |
| dml-P7-par2-spevak | 3675538842@sirius.fit.cvut.cz | Spěvák J. | 11. 11. 2026 | Teorie čísel 2 | áno | done |
| dml-P7-par3-legersky | 3675538854@sirius.fit.cvut.cz | Legerský J. | 5. 11. 2026 | Množiny a zobrazení 2, Teorie čísel 1 | áno | done |
| dml-P8-par1-dombek | 3674210639@sirius.fit.cvut.cz | Dombek D. | 16. 11. 2026 | Teorie čísel 3 | áno | done |
| dml-P8-par3-legersky | 3675538855@sirius.fit.cvut.cz | Legerský J. | 12. 11. 2026 | Teorie čísel 2 | áno | partial |
| dml-prosem2122-02 | youtube-yQs_OO6Bhf8 | proseminár | YouTube | Proseminář ke 2. cvičení (21/22) | **chýbajú** | done |
| dml-prosem2122-03 | youtube-eS7nNfsAlks | proseminár | YouTube | Proseminář ke 3. cvičení (21/22) | **chýbajú** | done |
| dml-prosem2122-04 | youtube-yttDqOC7SV0 | proseminár | YouTube | Proseminář ke 4. cvičení (21/22) | **chýbajú** | done |
| dml-prosem2122-05 | youtube-aL6BmuDYnb4 | proseminár | YouTube | Proseminář k 5. cvičení (21/22) | áno (od 20. 11. 2026) | done |
| dml-prosem2122-06 | youtube-J9wlP18xv40 | proseminár | YouTube | Proseminář k 6. cvičení (21/22) | áno (od 20. 11. 2026) | done |
| dml-prosem2122-07 | youtube-gpLXsAW6Jao | proseminár | YouTube | Proseminář k 7. cvičení (21/22) | áno (od 20. 11. 2026) | done |
| dml-prosem2526-01 | youtube-MiHCbSvDtm0 | proseminár | YouTube | výroková logika, logické zákony, úplné systémy spojek | áno (od 20. 11. 2026) | done |
| dml-prosem2526-02 | youtube-kLi-aFzpMos | proseminár | YouTube | KNT a DNT | áno (od 20. 11. 2026) | done |
| dml-prosem2526-03 | youtube-bjuF9lGBWag | proseminár | YouTube | predikátová logika | áno (od 20. 11. 2026) | done |
| dml-prosem2526-04 | youtube-GWNignQg7OI | proseminár | YouTube | matematická indukce | áno | done |
| dml-prosem2526-05 | youtube--p-Mw3KqEFQ | proseminár | YouTube | množiny a zobrazení | áno | done |
| dml-prosem2526-06 | youtube-eMg1HCiRpyc | proseminár | YouTube | teorie čísel I | áno | done |
| dml-prosem2526-07 | youtube-72ZR0mwZj3E | proseminár | YouTube | teorie čísel II | áno | done |
| dml-prosem2526-08 | youtube-ZaW-UmXLBso | proseminár | YouTube | relace | áno | done |
| dml-prosem2526-09 | youtube-zNNTu-Rl7BY | proseminár | YouTube | ekvivalence a uspořádání | áno | done |
| dml-prosem2526-10 | youtube-Uwsh7Oylcwg | proseminár | YouTube | kombinatorika I | áno | done |
| dml-prosem2526-11 | youtube-yHHitlo7CGI | proseminár | YouTube | kombinatorika II | áno | done |

</details>

Riešené príklady a metódy proseminárov s titulkami sú od 20. 11. 2026 v registri pokrytia (`coverage/sources.json`, druh `proseminar`, id `<proseminár>-<sekunda>`, kotva do záznamu); výťah nižšie ich ešte necituje.

## Podrobne

Stav k 17. 11. 2026 podľa lokálneho balíka. Presné termíny jednotlivých skúšok, prihlasovanie na druhú písomku a úplnú pôvodnú hlavičku zadania zdroj nemá; nižšie sú doložené pravidlá a dostupné doslovné úryvky.
Pravidlá 2026/27 odlišujem od starších testov a videí. Verejné odkazy iba preberám zo súborov, ich dostupnosť online som neoveroval.
Skratky ciest a úplný inventár sú v časti 8. Čísla definícií v kapitolách 2 až 6 sú overené v `cisla/dml-kapN.tsv`; pri ostatných uvádzam názov a miesto bez domysleného čísla.

### 1. Prednášajúci a paralelky

- **Paralelka 1: Daniel Dombek**, pondelok 14:30, T9:105. Organizátor predmetu, rieši hromadnú komunikáciu, písomky, kvízy a skúškové termíny. [P00_intro.tex:102–104; teachers/index.adoc:10]
- **Paralelka 2: Jan Spěvák**, streda 11:00, TK:BS. Garant predmetu, rieši predmet ako celok a výnimky. [P00_intro.tex:107–109; teachers/index.adoc:14]
- **Paralelka 3: Jan Legerský**, štvrtok 14:30, T9:155. [P00_intro.tex:112–114; index.adoc:31]
- Konzultácie po dohode, kontakty sú na osobných stránkach vyučujúcich; Spěvák uvádza osobne alebo MS Teams. [teachers/dombedan/index.adoc:8–16; teachers/spevajan/index.adoc:8–26; teachers/legerjan/index.adoc:8–11]
- Cvičiaci v aktuálnom zozname: Dombek, Spěvák, Jitka Rybníčková, Jan Starý, Irena Šindelářová, Magdaléna Tinková. Legerský je v aktuálnom zozname uvedený ako prednášajúci; jeho staré cvičenia sú na osobnej stránke zakomentované. [teachers/index.adoc:9–30; teachers/legerjan/index.adoc:13–137]

**Ako je predmet usporiadaný:** prednáška 90 min týždenne, predtočený proseminár približne 45 až 60 min týždenne, cvičenie 45 min týždenne.
Poradie prípravy je prednáška → proseminár → cvičenie; proseminár ukazuje základné príklady, cvičenie ťažšie a priestor na otázky. Dochádzka sa nekontroluje, odporúča sa. [index.adoc:239–243; P00_intro.tex:65–88]
Oficiálny študijný základ sú elektronické skriptá, spoločné pre všetky tri časti výučby. HTML obsahuje aj riešenia cvičení, PDF ich nemá; slajdy obsahujú menej než skriptá a celý výklad. [study-materials/index.adoc:10–22]

**Lokálne dostupné prednášky podľa názvov v INDEX.md:**

| Lokálne označenie | Dombek, par. 1 | Spěvák, par. 2 | Legerský, par. 3 |
|---|---|---|---|
| P2 | Predikátová logika 1 | Výroková 2, predikátová 1 | Výroková 2 |
| P3 | Predikátová 2, indukcia 1 | Predikátová 2, indukcia 1 | Predikátová 1 |
| P4 | Indukcia 2 | Indukcia 2 | Predikátová 2, indukcia 1; partial |
| P5 | Množiny a zobrazenia 1 | Množiny a zobrazenia 1 | Indukcia 2 |
| P6 | Množiny a zobrazenia 2, teória čísel 1; INDEX: failed | Množiny a zobrazenia 2, teória čísel 1 | Indukcia 3, množiny a zobrazenia 1 |
| P7 | Teória čísel 2 | Teória čísel 2 | Množiny a zobrazenia 2, teória čísel 1 |
| P8 | Teória čísel 3 | chýba v INDEX aj v txt/ | Teória čísel 2; partial |

Zdroj tabuľky: [INDEX.md:3–22]. Písmená P tu označujú názov lokálneho súboru, nie spoľahlivo číslo týždňa v aktuálnom harmonograme.
Fyzicky je prítomných **20 prednáškových TXT**, vrátane `dml-P6-par1-dombek.txt`, hoci INDEX ho označuje failed. Má časované riadky 04:59 až 1:37:51; samotná existencia nepreukazuje úplnosť alebo bezchybnosť prepisu.
P1 v lokálnom balíku titulkov chýbajú. Stránka 2026/27 však na P1 všetkých troch paralelek odkazuje, preto nemožno tvrdiť, že záznam P1 vôbec neexistuje. [BRIEF.md, Zdroje; index.adoc:39–41]
P0 je uvedená na stránke ako organizačné video z 15. 9. 2026; lokálny časovaný prepis P0 zdroj nemá. Obsah je v `P00_intro.tex`, ktorý načítava `DML_P00_intro.tex`. [index.adoc:34; DML_P00_intro.tex:10]

**Prosemináre:** spoločné video, nie samostatná paralelka. Fyzicky sú dostupné len TXT `dml-prosem2526-04` až `-11`: indukcia, množiny/zobrazenia, teória čísel I/II, relácie, ekvivalencie/usporiadanie, kombinatorika I/II. [INDEX.md:32–39; uvedené súbory, hlavičky]
INDEX uvádza aj `prosem2526-01` až `-03` a `prosem2122-02` až `-07`, ale s 0 cues a bez TXT v adresári. Stav done pri týchto položkách teda neznamená dostupné titulky. [INDEX.md:23–31; kontrola txt/]
Autor série 25/26 sa v prečítaných úvodoch nepredstavuje; meno v metadátach chýba. Spěvákova stránka sériu odkazuje a Dombek mu výslovne pripisuje **bonusové video o indukcii**, čo nestačí na potvrdenie autorstva každého proseminára. [teachers/spevajan/index.adoc:18; dml-P4-par1-dombek 06:52–07:25]
Pri citovaní proseminárov preto uvádzam „proseminár, autor nepotvrdený“.

### 2. Hodnotenie, písomky, rozstřel, skúška

**Platnosť:** nasledujúce pravidlá sú z časti „Podrobný popis hodnocení v ZS 2026/27“. Body sa evidujú v [Klasifikácii](https://grades.fit.cvut.cz), vždy celočíselne; zápočty z minulých rokov sa neuznávajú. [classification.adoc:8–11,29]

| Časť | Maximum | Minimum/podmienka | Formát a trvanie |
|---|---:|---|---|
| 1. zápočtová písomka | 5 | spoločné minimum zo semestra | 1 príklad, 10 min, vlastné cvičenie v 5. týždni |
| 2. zápočtová písomka | 30 | zo semestra spolu aspoň 20/35 | 3 príklady, 75 min, hromadne mimo cvičenia, prihlásenie v KOS |
| 6 priebežných kvízov MARAST | 0 | 5/6 je podmienka prípadnej opravy | dobrovoľné, bez bodov |
| Opravná zápočtová písomka | nastaví semester na 20 | vstup: 15 až 19 zo semestra + aspoň 5/6 kvízov; úspech aspoň 50 % | 75 min, logika až teória čísel, KOS |
| Skúškový rozstrel | 16 | aspoň 10 | 8 úloh, 40 min, PC učebňa/MARAST |
| Ústna časť | 54 | aspoň 20 spolu, aspoň 5 z každej otázky, bez zásadnej neznalosti | 2 otázky po 27; príprava cca 45 min, rozhovor 15 až 20 min |

Zdroje tabuľky: [classification.adoc:18–26,41–48,57,74,83,103–112,125–144].

**Prvá písomka:** v 5. výučbovom týždni, podľa harmonogramu 19. až 25. 10. 2026. Každý musí na svoje cvičenie podľa KOS, píše sa hneď na začiatku. [classification.adoc:48–51; index.adoc:86–98]
Obsahuje výrokovú **alebo** predikátovú logiku: logické zákony a predpísané normálne tvary, dôsledok/ekvivalencia vo výrokovej logike z úplných tvarov, formalizácia, negácia a spätný preklad do prirodzeného jazyka. Rozhodovanie o dôsledku či ekvivalencii dvojíc predikátových formúl sa v nej nevyžaduje. [classification.adoc:57–67]

**Druhá písomka:** v 11. týždni, harmonogram uvádza 30. 11. až 6. 12. 2026. Presné dni, hodiny, miestnosti a otvorenie prihlasovania sú v lokálnych podkladoch nevyplnené, časť pravidiel stále uvádza TBA. Nie sú to termíny starých testov z decembra 2025. [index.adoc:180–189; classification.adoc:74–78]
Po jednom príklade z indukcie, množín/zobrazení a teórie čísel; celkom 30 bodov. Rozdelenie presne 10+10+10 je doložené v testoch 2025, aktuálny opis zaručuje tri príklady a súčet, nie podrobné delenie. [classification.adoc:83; testy-dml-2.md:7–14]
Slabú a silnú indukciu treba korektne aplikovať a komentovať; množinové identity dokazovať logikou; injektivitu/surjektivitu dokazovať z definície alebo vyvrátiť konkrétnym protipríkladom. [classification.adoc:85–88]
Teória čísel: gcd/lcm, Bézoutove koeficienty, lineárne diofantické rovnice, multiplikatívna inverzia, redukcia mocnín a lineárne kongruencie bez hrubej sily; témy sa môžu kombinovať. [classification.adoc:89–95]
Bez prihlásenia alebo pri neospravedlnenej neúčasti je 0 bodov, bez automatického náhradného termínu. Vážne dôvody sa riešia individuálne, ideálne vopred. [classification.adoc:51,75]

**Oprava:** presný termín zdroj nemá, uvedené TBA. Prihlásenie v KOS medzi jednorazovými akciami po vyhodnotení druhej písomky; nesplnenie vstupných podmienok znamená odhlásenie. [classification.adoc:108–112]

**Rozstrel:** celá látka DML, s dôrazom na relácie a kombinatoriku; prevažujú výberové úlohy preverujúce elementárne porozumenie. [classification.adoc:128–129]
Za úplne správnu úlohu 2 body, za výberovú úlohu s jednou chybou zo štyroch 1 bod, inak 0. Zdroj neurčuje všeobecný čiastkový bodovací vzorec pre iné formáty. [classification.adoc:130]
Všetkých osem úloh sa zobrazuje súčasne. Priebežné ukladanie je možné, celý kvíz sa odosiela naraz; výsledok vidno po odoslaní, po odhlásení už nie. [classification.adoc:127]
Príchod k T9:349, školské počítače, MARAST a dodané heslo. Povolené sú iba dodané prázdne papiere a písacie potreby; v kvíze je jednoduchá klikacia kalkulačka. [classification.adoc:125–126]
Úspešný rozstrel sa po neúspešnej ústnej časti **neprenáša** na ďalší pokus; opakujú sa obe časti. [classification.adoc:131]

**Ústna časť:** losuje sa po jednej otázke z dvoch tematických skupín. Skúšajúci sa môže pýtať aj na súvisiace pojmy mimo explicitného znenia otázky. Čas je v Grades, miestnosť ešte TBA. [classification.adoc:137–139]
Právo veta znamená 0 bodov/F za zásadnú neznalosť základov aj pri inak dobrej odpovedi. Nestačí teda iba súčet bodov. [classification.adoc:140–144]
Pre minimum treba definície, základné tvrdenia, intuitívne porozumenie aj exaktný zápis; nemiešať množiny s formulami a rovnosť s ekvivalenciou. Vyššie hodnotenie zahŕňa dôkazy, súvislosti a protipríklady. [classification.adoc:149–172]

**Termíny skúšky:** harmonogram uvádza skúškové obdobie 4. 1. až 14. 2. po jesennom semestri 2026, teda v roku 2027; konkrétne vypísané dátumy zdroj nemá. Oprava má byť čo najskôr, skúšky takmer určite každý týždeň. [index.adoc:229–232]
Na prvý pokus majú slúžiť len prvé termíny; posledné dva sú podľa pravidiel 2026/27 vyhradené na opravy. Vyučujúci DML na Discorde predmetu 29. 1. 2026 tento zákaz vtedy zrušil, to je však starší beh a nie dôkaz zmeny pravidiel 2026/27. [classification.adoc:119–120; discord-dml.md:55–58]
Slajd uvádza 2 až 3 pokusy, presné individuálne oprávnenie na tretí pokus tento balík nevysvetľuje. [P00_intro.tex:175]
Modifikácie ELSA treba mať zaznamenané v stĺpci elsa v Grades; pri druhej písomke sú podrobnosti TBA. [classification.adoc:52,76,110,118]

**Známka:** súčet semester + rozstrel + ústna, maximum 105; všetky jednotlivé minimá musia byť splnené. A od 90, B 80 až 89, C 70 až 79, D 60 až 69, E 50 až 59; neúspech v ktorejkoľvek fáze znamená F. [classification.adoc:229–251]
Známku nemožno dodatočne zlepšiť ďalším skúšaním; text osobitne spomína preskúšanie pri nedodržaní pokynov počas testu. [classification.adoc:230–232]

**Pomôcky a dostupná hlavička doslova:**

- Lokálny prepis testov 2024/25 uvádza „jediné pomůcky psací potřeby“. Aktuálne pravidlá majú explicitný zoznam pomôcok pri rozstrele, nie rovnako úplný zoznam pri oboch zápočtovkách. [testy-dml-2.md:3; classification.adoc:126]
- Jediný doslovný všeobecný pokyn z hlavičky zachovaný v prepise: „Postupujte metodicky, neopomeňte žádný logický krok a své řešení vysvětlujte a odpovědi i výsledky řádně označte.“ [testy-dml-2.md:3]
- Úplnú pôvodnú štandardnú hlavičku vrátane kolónok, zalomenia, dátumu a celého zoznamu zákazov **zdroj nemá**. Citovaný pokyn nenahrádzam rekonštrukciou.
- `testy-rozstrel.md` je prepis úloh z wiki, nie úplný export úvodnej obrazovky MARASTu; doslovnú spoločnú hlavičku rozstrelu nemá. [testy-rozstrel.md:1–6,36–37]
- Archívne pokyny 2022/23: „Papíry použijete pouze naše.“; „vysvětlujte, komentujte, argumentujte, pište prosím přehledně.“ Nezamieňať s aktualizovanými pravidlami 2026/27. [test_info.adoc:4–6,16]
- Rovnaký archív žiada ISIC/kartičku, podpis a meno cvičiaceho, pred začiatkom nemanipulovať s papiermi, po limite prestať písať a odovzdať preložené papiere. [test_info.adoc:6–17]

**Čo pridáva Discord, s obmedzenou istotou:**

- Vyučujúci 30. 10. 2026 na Discorde predmetu uvádza opravenie zápočtovky a odoslanie správy 127 študentom; export neobsahuje ich výsledky ani termín druhej písomky. [discord-dml.md:37–42]
- V šiestich vypísaných januárových/februárových termínoch 2026 sú doložené počty prítomných, úspešných v rozstrele a na ústnej. Sú to pokusy v jednotlivých termínoch, nie unikátni študenti. [discord-dml.md:45–53]
- Študentská správa z 15. 1. 2026 tvrdí, že po limite sa kvíz sám neodošle. Učiteľova nadväzujúca reakcia a širší kontext nie sú úplne overené; nevyhlasovať za garantovanú vlastnosť aktuálneho MARASTu. [discord-dml.md:67–71]
- Študentské rady kontrolovať okrajové prípady, kvantifikátory a kombinatoriku sú osobná skúsenosť, nie pravidlá vyučujúcich ani prísľub rovnakých úloh. [discord-dml.md:60–65]

### 3. Čo prednášajúci chcú na písomke vidieť a čo zakazujú

Celokurzové jadro: čitateľný postup, exaktné objekty, definície s doménami a zdôvodnenie krokov. Tematicky obmedzené zákazy sú nižšie označené; nemožno ich preniesť na všetky úlohy.

| Kto | Požiadavka a krátky doslovný citát | Zdroj a hranica platnosti |
|---|---|---|
| Dombek | Čítať zátvorky, neignorovať rozsah operátorov: „Důležité číst závorky víc než kdy jindy.“ | [dml-P2-par1-dombek 51:55], pri predikátových formulách a písomkách |
| Dombek | V čistej predikátovej logike nepoužívať neskoršie skratky: „Prosím vás, v první zápočtové písemce tohle pro vás neexistuje.“ | [dml-P3-par1-dombek 1:07:24–1:07:46], obmedzené kvantifikátory; platí aj pri skúškových definíciách logiky |
| Spěvák | Rovnaké rozlíšenie: „omezené kvantifikátory jsou jenom prostředek, který zjednodušuje zápis“ | [dml-P3-par2-spevak 1:32:54], v čistých logických úlohách nie, ďalej áno |
| Spěvák | „Znegujte“ znamená dostať negáciu až pred atomické formule, nie iba pridať jednu negáciu zvonku. „aby stála ta negace jen před atomickýma formulema.“ | [dml-P3-par2-spevak 53:28]; súlad [classification.adoc:65] |
| Dombek | V indukcii oba kroky: „Ani jeden nesmíte vynechat“ | [dml-P4-par1-dombek 39:09], základný a indukčný krok |
| Dombek | Správne zátvorky a implikácia v indukčnom kroku, nie predpoklad platnosti dokazovaného celku: „mít tam správně závorky.“ | [dml-P4-par1-dombek 39:57–40:40] |
| Spěvák | Označiť základný krok, aby sa opravujúci orientoval: „do písemek určitě pište, aby se v tom ten opravující orientoval.“ | [dml-P4-par2-spevak 42:14]; pri indukcii |
| Legerský | Rozpísať konkrétnu dokazovanú implikáciu: „nepište tam VN, potom VN plus jedna, napište si tam přímo to, co chcete ukázat.“ | [dml-P5-par3-legersky 1:12:08]; rada k zápisu indukcie, nie všeobecný zákaz symbolu $V(n)$ |
| Dombek | Nemiešať definíciu množiny a formulu o členstve: „Hlavně to nepleťte mezi sebou.“ | [dml-P5-par1-dombek 40:12–41:14]; pri skúške opisuje syntakticky nezmyselnú kombináciu |
| Dombek | Vennov diagram nenahrádza požadované odvodenie: „nechceme vám dávat body za to, že vybarvujete kolečka.“ | [dml-P5-par1-dombek 55:48]; súlad [classification.adoc:87] |
| Spěvák | Pri počítaní zobrazení sa bez inej dohody myslia totálne: „Takže uvažujeme pouze totální zobrazení“ | [dml-P5-par2-spevak 47:06]; výslovne spája s odpoveďou v MARASTe |
| Legerský | Rovnaké pravidlo totálnosti: „my budeme dál uvažovat jenom totální zobrazení“ | [dml-P6-par3-legersky 58:53]; netotálna interpretácia môže dať chybný počet |
| Spěvák | Definície sa žiadajú v podobe kurzu: „tak chci tuto definici“ | [dml-P6-par2-spevak 58:41], gcd; školskú verziu prijme iba s dovysvetlením |
| Dombek | Samotné konštatovanie nie je dôkaz: „No, taky do zkoušky nepište, jo.“ | [dml-P7-par1-dombek 1:10:13], reakcia na vlastné „Proč? No, proto.“ z 1:10:07 |
| Dombek | Nezamieňať rovnosť a kongruenciu a uvádzať modul; v jednej úlohe sa môže meniť. | [dml-P8-par1-dombek 1:29:50]; prepis „konkurence“ je nejasný, význam určuje kontext kongruencií |
| Legerský | Vedieť znenie a použitie Euklidovho algoritmu; jeho dôkaz nie je podmienkou každého úspešného výkonu. „No určitě musíte vědět, jak běží ten algoritmus.“ | [dml-P8-par3-legersky 46:59–47:34]; nie všeobecné odpustenie dôkazov |
| Proseminár, autor nepotvrdený | Označiť použitie indukčného predpokladu: „Napíšu na to rovno zase IP“ | [dml-prosem2526-04 12:32]; automatický prepis, presný grafický zápis z TXT nevidno |
| Proseminár, autor nepotvrdený | Pri očividnej parite stačí stručná poznámka: „že zřejmě tohle je sudé číslo“ | [dml-prosem2526-04 13:47–14:04]; týka sa konkrétneho výrazu s mocninou nepárneho čísla |

**Písomné požiadavky potvrdené mimo titulkov:**

- Definícia musí určovať správne množiny, pri platnosti treba dôkaz, pri neplatnosti konkrétny protipríklad a overenie, že tvrdenie skutočne vyvracia. [testy-dml-2.md:8,13,24]
- Pri indukcii uviesť podobu indukčného kroku a miesto použitia indukčného predpokladu. [testy-dml-2.md:7,12]
- Vyučujúci DML na Discorde predmetu žiada reťazec logických ekvivalencií s oporou každého kroku vo vete zo skrípt: každú ekvivalenciu treba vedieť zdôvodniť odkazom na niektorú vetu zo skrípt. Nie je to doložený príkaz písať ku každému kroku číslo vety. [discord-dml.md:31–34]
- Odvodená redakčná zásada pre lekcie: výsledok aj odpoveď označiť, pomenovať používanú definíciu/vetu a jej predpoklady. Ide o syntézu pokynov, nie nový doslovný výrok jedného učiteľa. [testy-dml-2.md:3; classification.adoc:85–95]

### 4. Značenie kurzu

Značenie je súčasť porozumenia: formula, množina, zobrazenie a číslo nie sú zameniteľné typy objektov. [classification.adoc:153–154; chap-01-uvod.woo:201–205]

| Symbol | Význam | Odkaz |
|---|---|---|
| $\mathbb N=\{1,2,3,\dots\}$ | prirodzené čísla **bez nuly** | [Definice 4.1, chap-04-dukazy-indukce.woo:28–45; dml-P3-par2-spevak 1:17:24–1:18:40] |
| $\mathbb N_0=\{0,1,2,\dots\}$ | prirodzené čísla s pridanou nulou | [Definice 4.1, chap-04-dukazy-indukce.woo:43–45] |
| $\mathbb Z$ | celé čísla | [Definice 4.1, chap-04-dukazy-indukce.woo:45] |
| $A,B,C,\dots$, $v(A)\in\{0,1\}$ | prvotné formule a pravdivostné ohodnotenie | [Definice 2.1–2.2, chap-02-vyrokova-logika.woo:184–200; anki-dml.txt:2–3] |
| $\neg,\wedge,\vee,\Rightarrow,\Leftrightarrow$ | negácia, konjunkcia, disjunkcia, implikácia, ekvivalencia ako spojky | [anki-dml.txt:4–8; cisla/dml-kap2.tsv] |
| $\top,\bot$ | tautológia, kontradikcia | [anki-dml.txt:18] |
| $\models$, $\mathrel{\vert\mkern-3mu{=}\mkern-3mu\vert}$ | logický dôsledok, logická ekvivalencia; druhý symbol textovo `\|=\|` | [Definice 2.15, chap-02-vyrokova-logika.woo:1223; anki-dml.txt:19; preamble.tex:261–262] |
| $\forall x$, $\exists x$ | všeobecný a existenčný kvantifikátor | [anki-dml.txt:52; dml-P3-par3-legersky 33:33] |
| $(\forall x\in M)F$, $(\exists x>0)F$ | obmedzené kvantifikátory, skratky mimo čistej logiky | [Definice 4.3, chap-04-dukazy-indukce.woo:111–133] |
| $\sum_{k=d}^{h}a(k)$, $\prod_{k=d}^{h}a(k)$ | suma a produkt; pri $d>h$ hodnoty 0 a 1 | [Definice 4.2, chap-04-dukazy-indukce.woo:56–87] |
| $:=$ | zavedenie definíciou; zdrojové makro `\ceq` | [preamble.tex:209; chap-05-mnoziny-zobrazeni.woo:958,968] |
| $\square$, Q.E.D. | koniec dôkazu, Halmosov symbol alebo latinská skratka | [chap-01-uvod.woo:251–256]; slajdy uvádzajú aj c.b.d. [P00_intro.tex:287] |
| $x\in A$, $x\notin A$, $\emptyset$ | členstvo, nečlenstvo, prázdna množina | [Definice 5.1, chap-05-mnoziny-zobrazeni.woo:86; anki-dml.txt:91] |
| $A\subseteq B$, $A\subsetneq B$, $\mathcal P(A)$ | inklúzia, vlastná podmnožina, potenčná množina | [Definice 5.2, chap-05-mnoziny-zobrazeni.woo:288; anki-dml.txt:92] |
| $\overline A$, $\cap$, $\cup$, $\setminus$, $\times$ | doplnok v univerze, prienik, zjednotenie, rozdiel, kartézsky súčin | [Definice 5.3–5.4, chap-05-mnoziny-zobrazeni.woo:451–497; anki-dml.txt:94–95] |
| $f:A\to B$, $D_f$, $H_f$ | zobrazenie, definičný obor a skutočný obor hodnôt; $H_f\subseteq B$, nie automaticky $H_f=B$ | [Definice 5.7, chap-05-mnoziny-zobrazeni.woo:901–938] |
| $f[M]$, $f^{-1}[N]$ | obraz množiny, vzor množiny; hranaté zátvorky | [Definice 5.8, chap-05-mnoziny-zobrazeni.woo:941–968] |
| $f^{-1}$ | inverzné zobrazenie, odlišné od operácie vzoru | [Definice 5.13, chap-05-mnoziny-zobrazeni.woo:1158; dml-P5-par1-dombek 1:10:44–1:12:05] |
| $f\circ g$ | najprv $g$, potom $f$ | [Definice 5.10, chap-05-mnoziny-zobrazeni.woo:1067; testy-dml-2.md:8] |
| $\gcd(a,b)$, $\operatorname{lcm}(a,b)$ | najväčší spoločný deliteľ a najmenší spoločný násobok; nezáporné čísla, definícia cez deliteľnosť | [Definice 6.3, chap-06-tc.woo:458–504; dml-P6-par2-spevak 59:34,1:01:13] |
| $a\mid b$ | $a$ delí $b$, nie zlomok $a/b$ | [Definice 6.1, chap-06-tc.woo:240; dml-P7-par3-legersky 1:16:51] |
| $a\bmod m$ | číslo: zvyšok v $\{0,\dots,m-1\}$ | [Definice 6.2, chap-06-tc.woo:444; dml-P7-par2-spevak 1:24:15–1:25:43] |
| $a\equiv b\pmod m$ | vzťah kongruencie dvoch čísel | [Definice 6.7, chap-06-tc.woo:1703; dml-P7-par2-spevak 1:09:35] |
| $\mathbb Z_m$, $+_m$, $\cdot_m$ | množina zvyškov a modulárne operácie | [Definice 6.8, chap-06-tc.woo:1965; dml-P7-par2-spevak 1:26:13–1:27:13] |
| $\varphi(n)$ | Eulerova funkcia | [Definice 6.10, chap-06-tc.woo:2566–2573] |
| $\Delta_X$, $R^{-1}$, $R\circ S$, $R^+$, $R^*$ | diagonálna relácia, inverzia, skladanie, tranzitívny a reflexívne tranzitívny uzáver | [anki-dml.txt:177,179,182,185; testy-rozstrel.md:13] |
| $\preceq$, $\prec$ | čiastočné usporiadanie a z neho odvodené ostré usporiadanie | [anki-dml.txt:198,200] |
| $\binom nk$, $n!$ | kombinačné číslo a faktoriál | [anki-dml.txt:222,83] |

**Čo nepoužívať alebo nezamieňať:**

- Nulu nevkladať do $\mathbb N$. Dvojica konvencií $\mathbb N^+$ a $\mathbb N$ s nulou nie je konvencia DML. [chap-04-dukazy-indukce.woo:32–45; dml-P3-par2-spevak 1:18:40]
- DML zapisuje **gcd/lcm**, nie NSD/NSN. Našiel sa pozitívny doklad používaného značenia, **nie explicitný všeobecný zákaz** českých/slovenských skratiek NSD/NSN. [chap-06-tc.woo:476,500]
- Kurz používa DNT/KNT a ÚDNT/ÚKNT. Spěvák pri DNT výslovne odmieta používanie anglického názvu/skratky; prepis začína skomoleným „Donato“, preto z neho neodvodzujem nový symbol. [anki-dml.txt:40–43; dml-P2-par2-spevak 23:33]
- „KNF“ sa vyskytuje v sekundárnom prepise rozstrelu 2026, ale nie je dôvodom premenovať kurzové KNT na KNF. [testy-rozstrel.md:30; anki-dml.txt:41]
- V čistej logike rozlišovať spojku $\Leftrightarrow$ od vzťahu logickej ekvivalencie a $\Rightarrow$ od dôsledku. Od kapitoly o dôkazoch kurz povoľuje obvyklý matematický zápis spojkami v univerzálne platnom význame. [chap-04-dukazy-indukce.woo:252–283; dml-P3-par2-spevak 1:33:24–1:34:43]
- Vzor $f^{-1}[N]$ má zmysel aj bez existencie inverzného zobrazenia $f^{-1}$. Nevynechávať hranaté zátvorky, ak by sa tým zmenil význam. [dml-P5-par1-dombek 1:10:44–1:12:05]
- Zobrazenie je od príslušnej dohody totálne, ak nie je povedané inak. Úvodná všeobecnejšia definícia pripúšťa aj netotálnosť, to nie je rozpor. [chap-05-mnoziny-zobrazeni.woo:905–938]
- Zápis dvojíc riešení po riadkoch Legerský pripúšťa; nie je doložené pravidlo DML, že dvojice musia byť stĺpcové vektory ako v LA1. [dml-P8-par3-legersky 1:28:57]

### 5. Terminológia

Slovenské ekvivalenty sú redakčný preklad; česká terminológia ostáva v citátoch a definíciách. Pri termínoch z Anki je uvedené miesto v exporte, ktoré obsahuje aj odkaz na presnú definíciu v HTML skriptách.

| Česky | Slovensky | Kde je definované alebo vysvetlené |
|---|---|---|
| definice | definícia | [chap-01-uvod.woo:201–205] |
| věta, tvrzení | veta, tvrdenie | [chap-01-uvod.woo:211–225] |
| lemma | lema, pomocné tvrdenie | [chap-01-uvod.woo:229–234] |
| pozorování, důsledek | pozorovanie, dôsledok | [chap-01-uvod.woo:238–243] |
| důkaz | dôkaz | [chap-01-uvod.woo:246–256] |
| prvotní výrok, prvotní formule | prvotný výrok, prvotná formula | [Definice 2.1, chap-02-vyrokova-logika.woo:184; anki-dml.txt:2] |
| pravdivostní ohodnocení | pravdivostné ohodnotenie | [Definice 2.2, chap-02-vyrokova-logika.woo:200; anki-dml.txt:3] |
| tautologie, splnitelnost, kontradikce | tautológia, splniteľnosť, kontradikcia | [anki-dml.txt:14] |
| logická ekvivalence, logický důsledek | logická ekvivalencia, logický dôsledok | [Definice 2.15, chap-02-vyrokova-logika.woo:1223; Definice 3.10, chap-03-predikatova-logika.woo:1417] |
| obměněná implikace, obměna | obmenená implikácia, obmena | [anki-dml.txt:29] |
| universální systém spojek | univerzálny systém spojok | [anki-dml.txt:35] |
| literál, implikant, klausule | literál, implikant, klauzula | [anki-dml.txt:39–41]; variant „klauzule“ v reči [dml-P2-par3-legersky 1:12:38] |
| disjunktivní/konjunktivní normální tvar | disjunktívny/konjunktívny normálny tvar | [Definice 2.20–2.21, chap-02-vyrokova-logika.woo:2494–2545] |
| minterm, maxterm | minterm, maxterm | [anki-dml.txt:43] |
| obecný/existenční kvantifikátor | všeobecný/existenčný kvantifikátor | [anki-dml.txt:52] |
| term, predikátová formule | term, predikátová formula | [anki-dml.txt:54–55] |
| volná/vázaná proměnná | voľná/viazaná premenná | [anki-dml.txt:56] |
| interpretace, kontext | interpretácia, kontext | [anki-dml.txt:57] |
| přímý/nepřímý důkaz, důkaz sporem | priamy/nepriamy dôkaz, dôkaz sporom | [Definice 4.4–4.6, chap-04-dukazy-indukce.woo:316–399] |
| základní krok, indukční krok, indukční předpoklad | základný krok, indukčný krok, indukčný predpoklad | [anki-dml.txt:84–85]; postup [dml-P4-par1-dombek 39:09] |
| množina, podmnožina, potenční množina | množina, podmnožina, potenčná množina | [Definice 5.1–5.2, chap-05-mnoziny-zobrazeni.woo:86–288] |
| průnik, sjednocení, doplněk, rozdíl | prienik, zjednotenie, doplnok, rozdiel | [Definice 5.3, chap-05-mnoziny-zobrazeni.woo:451] |
| zobrazení, definiční obor, obor hodnot | zobrazenie, definičný obor, obor hodnôt | [Definice 5.7, chap-05-mnoziny-zobrazeni.woo:901–938] |
| obraz, vzor, zúžení | obraz, vzor, zúženie | [Definice 5.8, chap-05-mnoziny-zobrazeni.woo:941–976] |
| prosté/injektivní, na/surjektivní, bijektivní | prosté/injektívne, na/surjektívne, bijektívne | [Definice 5.9, chap-05-mnoziny-zobrazeni.woo:979] |
| největší společný dělitel, nejmenší společný násobek | najväčší spoločný deliteľ, najmenší spoločný násobok | [Definice 6.3, chap-06-tc.woo:458–504] |
| nesoudělná čísla | nesúdeliteľné čísla | [Definice 6.3, chap-06-tc.woo:484–486] |
| lineární diofantická rovnice, partikulární řešení | lineárna diofantická rovnica, partikulárne riešenie | [Definice 6.4–6.5, chap-06-tc.woo:1071,1304] |
| relace, ekvivalence, rozklad množiny | relácia, ekvivalencia, rozklad množiny | [anki-dml.txt:170,189,193] |
| částečné/úplné uspořádání | čiastočné/úplné usporiadanie | [anki-dml.txt:198,213] |
| nejmenší prvek, minimální prvek | najmenší prvok, minimálny prvok; odlišné pojmy | [anki-dml.txt:210] |
| kombinační číslo, permutace, pravděpodobnostní prostor | kombinačné číslo, permutácia, pravdepodobnostný priestor | [anki-dml.txt:222,237,218] |
| zápočet, rozstřel | zápočet, vstupný skúškový test | organizačné názvy, nie matematické definície [classification.adoc:98–130] |

### 6. Rozdiely medzi paralelkami

**Tempo podľa dostupného indexu:** Dombek a Spěvák majú od lokálneho P3 podobné tematické míľniky; Legerský je v P3 ešte pri prvej predikátovej logike, v P5 pri indukcii a v P8 pri druhej časti teórie čísel. Je to porovnanie tém nahrávok, nie meranie rýchlosti reči alebo kvality. [INDEX.md:3–22]
Číslovanie Dombekových lokálnych nahrávok sa od aktuálneho harmonogramu líši: napríklad id 3674210633 je lokálne P2, ale stránka ho uvádza ako P3 zo 5. 10. 2026. Pri výbere videa sa riadiť aj id a témou. [INDEX.md:3; index.adoc:63]

- **Dombek:** v overených úsekoch vysvetľuje, prečo je nesprávny zápis problém, cez konkrétne chyby pri opravovaní. Rozoberá miešanie množín/formúl, nevýhody bodovania Vennových diagramov a zmenu modulu v jednej úlohe. [dml-P5-par1-dombek 40:20–41:14,55:48–56:06; dml-P8-par1-dombek 1:29:50]
- **Spěvák:** v overených úsekoch systematicky pomenúva konvencie použiteľné v MARASTe a štandardný zápis riešenia: nula mimo $\mathbb N$, totálne zobrazenia, označenie základného kroku. [dml-P3-par2-spevak 1:17:24–1:18:40; dml-P5-par2-spevak 47:06; dml-P4-par2-spevak 42:14]
- **Legerský:** výslovne experimentuje so skorším zaradením štrukturálnej indukcie už pri univerzálnych systémoch spojok. „Tak tady zkouším na vás takhle vychylit strukturální indukci o čtyři týdny dřív než v loňském roce.“ Automatický prepis „vychylit“ ponechaný. [dml-P2-par3-legersky 56:08; širší kontext 45:23–46:30]
- **Legerský:** trvá na konkrétne rozpísanej implikácii indukčného kroku a otvorene rozlišuje požiadavku vedieť algoritmus od úrovne dôkazu pre vyššiu známku. [dml-P5-par3-legersky 1:12:08–1:13:03; dml-P8-par3-legersky 46:59–47:34]
- Zhoda naprieč paralelkami: obmedzené kvantifikátory až mimo čistých logických úloh, rozlišovanie typov objektov a totálnosť zobrazení. Nie sú doložené samostatné klasifikačné pravidlá podľa prednášajúceho. [dml-P3-par1-dombek 1:07:24–1:07:46; dml-P3-par2-spevak 1:32:54; dml-P6-par3-legersky 58:53; classification.adoc:29]
- Študentské hodnotenie prísnosti, zrozumiteľnosti alebo náročnosti jednotlivých skúšajúcich **zdroj nemá**. Discord export výslovne uvádza, že tieto témy neboli prejdené. [discord-dml.md:19–24]

### 7. Nejasné a chýbajúce

- Lokálne chýbajú P1 všetkých paralelek, P8 Spěváka a neskoršie prednášky; nemožno z toho usudzovať, že predmet končí teóriou čísel. Skriptá a dostupné prosemináre pokrývajú aj relácie a kombinatoriku. [INDEX.md:3–39; chap-01-uvod.woo:25–28]
- INDEX označuje P4/P8 Legerského partial a P6 Dombeka failed. TXT pre P6 fyzicky existuje; neoverená je úplnosť a dôvod stavu failed. [INDEX.md:11,15,22; txt/dml-P6-par1-dombek.txt]
- Prednášky a aktuálny harmonogram majú odlišné číslovanie/témy; dátum nahrávky neodvodzovať iba z názvu TXT. [INDEX.md:3; index.adoc:50,63]
- Prosemináre 21/22 a prvé tri 25/26 sú v INDEX bez cues, lokálne TXT chýbajú. Autor celého súboru proseminárov nie je spoľahlivo potvrdený; odkaz zo Spěvákovej stránky nie je podpis videa. [INDEX.md:23–31; teachers/spevajan/index.adoc:18]
- Presné termíny a registrácie druhej písomky, opravy, skúšok a miestnosť ústnej sú v pravidlách čiastočne TBA. Harmonogram určuje týždne, nie kompletné akcie v KOS. [classification.adoc:76–77,108,124,137]
- `test_info.adoc` je výslovne pre rok 2022/23; Legerského rozsiahly zoznam cvičení a staré bodovanie prvej písomky sú v komentári `////`, teda nepoužiť ako aktívne pravidlá 2026/27. [test_info.adoc:4; teachers/legerjan/index.adoc:13–137]
- `study-materials/index.adoc` obsahuje aj vetu o roku 2025/26 vedľa harmonogramu B261. Údaj o ročníku videí preto treba čítať z konkrétneho odkazu a INDEX, nie mechanicky z tejto vety. [study-materials/index.adoc:28,84]
- Prvá písomka má v slajdoch približne 5 až 10 min, v podrobných pravidlách štandardne 10 min. Pre aktuálny presný čas používam druhý údaj. [P00_intro.tex:133; classification.adoc:48]
- Staré druhé testy majú 2024 trvanie 60 min a 2022 šesť príkladov; dnešný formát je 75 min/tri príklady. Historické formáty nezamieňať. [testy-dml-2.md:3,21,44; classification.adoc:74,83]
- Úplná štandardná hlavička písomky ani úvodné deklarácie skutočného rozstrelu nie sú zachované. Hlavičku nebolo možné overiť proti originálnemu obrázku; k dispozícii je iba sekundárny prepis. [testy-dml-2.md:1–3; testy-rozstrel.md:3,37]
- Explicitný zákaz NSD/NSN sa nenašiel. Doložené je používanie gcd/lcm; rozdiel medzi konvenciou a zákazom je zámerný. [chap-06-tc.woo:476,500]
- Pri titulkoch sú chybné rozpoznania a miestami chýba diakritika. Žiadny symbol z tabule nerekonštruujem len zo slov „tohle“; tabuľku značenia kontrolujem aj v skriptách/Anki.
- Discord je neúplný výskumný záznam k 6. 11. 2026, nie úplný export kanála. Chýbajú priame odkazy na správy, obrázky a širší kontext viacerých reakcií. [discord-dml.md:3–26]
- Anki je užitočné na presné názvy a znenie, nie samostatná záruka úplného výkladu alebo aktuálnych organizačných pravidiel. Má 241 textových riadkov vo formáte téma/otázka, odpoveď, URL oddelených `||`. [anki-dml.txt:1–241]

### 8. Surové zdroje

**Korene ciest:**

- `K = kontext`
- `S = zdroje`
- `WEB = K/skripta-dml/bi-dml-master`
- `WOO = S/src/bi-dml-master/src/textbook`
- `index.adoc`, `classification.adoc`, `test_info.adoc`, `teachers/...`, `study-materials/...` v citáciách patria pod WEB.
- `P00_intro.tex` a `preamble.tex` patria pod WEB/src/slides; `DML_P00_intro.tex` je obal s inputom skutočného obsahu.
- `chap-*.woo` patria pod WOO, `testy-*.md` a `discord-dml.md` pod S, `anki-dml.txt`, `INDEX.md`, `BRIEF.md`, `cisla/...` pod K.
- Citácia názvu prednášky/proseminára bez prípony smeruje na K/txt/názov.txt; čas označuje začiatok riadku v prehrávači.

**Čítané organizačné zdroje:**

- K/BRIEF.md: pravidlá, citácie, dostupnosť P1 a opis automatického prepisu.
- K/INDEX.md:3–39: DML prednášky a prosemináre, témy, id, stavy.
- WEB/index.adoc:19–253: rozvrh, harmonogram B261, študijné poradie.
- WEB/classification.adoc:1–258: celé pravidlá 2026/27, minimá, formáty a ukážky ústnych otázok.
- WEB/test_info.adoc:1–19: archívny organizačný pokyn 2022/23.
- WEB/study-materials/index.adoc:1–108: elektronické skriptá, rozdiel PDF/HTML/slajdy, videá a ich časové označenie; zakomentované časti nepoužité ako aktívne pravidlá.
- WEB/teachers/index.adoc a teachers/{dombedan,spevajan,legerjan,rybnijit,staryja2,sindeire,tinkomag,ex-teachers}/index.adoc: aktuálne mená, kontakty a odlíšenie archívu.
- WOO/chap-01-uvod.woo:1–324: cieľ kurzu, rozsah, štruktúra matematického textu.
- WEB/src/slides/DML_P00_intro.tex:1–10 a P00_intro.tex:23–340: organizácia, učitelia, hodnotenie, ciele a formalizmus; preamble.tex:209,261–262: presné makrá symbolov.
- S/testy-dml-2.md:1–45: hlavička, trvanie, staré formáty a požadovaný postup; S/testy-rozstrel.md:1–37: formát a hranice historického prepisu.
- S/discord-dml.md:1–94: celý neúplný výskumný záznam; použité organizačné zistenia a ich výslovné obmedzenia.
- K/anki-dml.txt:1–241: formát exportu a cielené definície zo všetkých tém.
- K/cisla/dml-kap2.tsv až dml-kap6.tsv: kontrola čísiel použitých definícií; čísla kap. 1, 7, 8 nie sú odvodené z počítania Woo prostredí.
- Doplnková kontrola značenia: WOO/chap-02-vyrokova-logika.woo, chap-03-predikatova-logika.woo, chap-04-dukazy-indukce.woo:28–289, chap-05-mnoziny-zobrazeni.woo:86–1158, chap-06-tc.woo:240–504,1703,1965,2566–2573; konkrétne definície podľa tabuliek.

**Prehľadané všetky fyzicky prítomné DML TXT:**

- dml-P2-par1-dombek, dml-P3-par1-dombek, dml-P4-par1-dombek, dml-P5-par1-dombek, dml-P6-par1-dombek, dml-P7-par1-dombek, dml-P8-par1-dombek.
- dml-P2-par2-spevak, dml-P3-par2-spevak, dml-P4-par2-spevak, dml-P5-par2-spevak, dml-P6-par2-spevak, dml-P7-par2-spevak.
- dml-P2-par3-legersky, dml-P3-par3-legersky, dml-P4-par3-legersky, dml-P5-par3-legersky, dml-P6-par3-legersky, dml-P7-par3-legersky, dml-P8-par3-legersky.
- dml-prosem2526-04, -05, -06, -07, -08, -09, -10, -11.
- Hľadané korene: písem/pisem, zkouš/zkous, rozstř/rozst, bodov, na test/do testu, chceme, nepište/nepiste, zdůvod/zduvod, značen/značit, gcd/lcm, nsd/nsn, nepoužív. Zásahy typu „zkoušíte“ alebo názvy lokálnych premenných nie sú automaticky kurzové pravidlo.
- Úseky použité na všeobecné pokyny: Dombek P2 51:55; P3 43:58 a 1:00:39–1:16:04; P4 06:32–07:53 a 37:30–40:40; P5 40:12–41:14,55:48–56:06,1:10:44–1:12:05; P7 1:07:51–1:10:41; P8 1:29:50.
- Spěvák P2 23:33; P3 52:55–53:36 a 1:17:06–1:34:47; P4 40:34–43:08; P5 47:06; P6 58:41–1:01:13; P7 1:09:35 a 1:24:15–1:27:13.
- Legerský P2 45:23–56:08; P3 33:33; P5 1:11:21–1:13:58; P6 58:53; P7 1:16:51; P8 46:59–47:34 a 1:28:57.
- Proseminár 04 12:32–14:18; úvody 04 až 11 od 00:00 pri hľadaní identity. Ostatné zásahy boli tematické výpočty, nie doložené nové organizačné pravidlá.

**Kontrola citácií pred odovzdaním:** 12 náhodne vybraných doslovných citácií znova porovnaných s doslovným riadkom a časom v TXT; kontrola neznamená overenie automatického prepisu proti zvuku.

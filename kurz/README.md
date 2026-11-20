# Kontext kurzov (balíky pre autorov obsahu)

Vetva dokumentov pod koreňom [docs/ZASADY.md](../docs/ZASADY.md):

| úroveň | súbor | obsah |
|---|---|---|
| koreň | [docs/ZASADY.md](../docs/ZASADY.md) | zásady obsahu, postup tvorby lekcie |
| predmet | [la1/README.md](la1/README.md), [dml/README.md](dml/README.md) | prednášajúci, testy, značenie, čo kto chce a zakazuje, zoznam kapitol |
| kapitola | `<predmet>/kap<N>-*.md` | balík kapitoly: príbeh, postupy, značenie, úlohy, chyby, citácie s časom |
| lekcia | `<predmet>/plany/<id lekcie>.md` | plán lekcie (píše autor lekcie pred písaním) |

`<predmet>/cisla.tsv`: čísla všetkých definícií, viet a ďalších tvrdení skrípt (číslo, druh, label, názov, riadok
vo `.woo`), vypočítané zo zdroja skrípt a overené (LA1 proti číslam v appke, DML proti Anki balíčku vyučujúcich).
Číslo do `src` ber odtiaľto, nie z pamäti.

## Citácie v balíkoch

- Prednáška a proseminár: `[la1-P6-par2-klouda 42:49]` = súbor titulkov a čas v nahrávke (ten istý, aký vidí študent
  v prehrávači a aký používajú klipy). Mená súborov: `<predmet>-P<číslo prednášky>-par<paralelka>-<prednášajúci>`,
  proseminár `<predmet>-prosem<rok>-<číslo>`. Id nahrávky ku každému súboru je v tabuľke „Nahrávky“ v README
  predmetu.
- Skriptá: `[Věta 2.59, chap-03.woo:2113]`. Slajdy: `[bi-la1-prezentace-02.tex:123]`. Testy, MARAST, Discord:
  `[testy-la1-2.md:40]`, alebo id zo `shared/src/commonMain/composeResources/files/coverage/sources.json`.

## Surové zdroje (mimo repozitára)

Balíky sú výťahy. Surové zdroje, z ktorých vznikli, v repozitári nie sú: skriptá vo formáte WooWoo, slajdy, text
titulkov nahrávok prednášok, prepisy starých testov, záznamy z MARASTu a poznámky z Discordu predmetu. Správca ich
drží lokálne v dvoch koreňoch podkladov, `kontext/` a `zdroje/`; všetky cesty v balíkoch a v tabuľke nižšie sú
relatívne voči nim.

Kde ich nájde niekto iný: skriptá, slajdy a stránky predmetov sú na courses.fit.cvut.cz (BI-LA1, BI-DML), záznamy
prednášok v systéme nahrávok FIT (id nahrávky ku každému súboru titulkov je v README predmetu), staré testy na
fit-wiki.cz a úlohy v MARASTe, všetko pre študentov FIT. Citáciu, ktorú nevieš overiť (Discord, interné poznámky
`INDEX.md`, `BRIEF.md`), ber ako podklad, nie dôkaz, a v pochybnosti ju over v skriptách alebo na prednáške.

| čo | kde |
|---|---|
| titulky nahrávok prednášok (text s časmi; nástroj na ich stiahnutie nie je súčasťou repozitára), prehľad nahrávok | `kontext/txt/` (riadky `[mm:ss] veta`), `INDEX.md` (stav k 17. 11. 2026) |
| skriptá LA1 (WooWoo), slajdy, stránka predmetu | `kontext/skripta/bi-la1-master/` |
| skriptá DML, slajdy, stránka predmetu | `zdroje/src/bi-dml-master/`, `kontext/skripta-dml/bi-dml-master/` |
| testy (fit-wiki), MARAST, Discord | `zdroje/` (`testy-*.md`, `marast*.md`, `marast-problems/`, `discord-*.md`) |
| Anki vyučujúcich DML | `zdroje/anki/`, textom `kontext/anki-dml.txt` |
| inventár podotázok so zručnosťami | `shared/src/commonMain/composeResources/files/coverage/sources.json` (v repe) |

## Ako balík obnoviť

Po každej novej prednáške alebo proseminári k odprednášanej kapitole (alebo keď autor lekcie nájde v balíku chybu):

1. Získaj titulky novej nahrávky a preveď ich na riadky `[mm:ss] text` do `kontext/txt/`.
2. Z nových úsekov sprav výťah podľa osnovy balíka (nadpisy 1 až 11 v každom `kap*.md`), s citáciami ku každému
   tvrdeniu; zdroje sa pritom len čítajú.
3. Výťah over: náhodné citácie proti titulkom a skriptám (aspoň 5 na kapitolu), čísla proti `cisla.tsv`, nič
   nevymyslené. Potom ho zapracuj do balíka a doplň „Stav k“ v hlavičke.
4. Čo zdroje nemajú (napr. titulky proseminára), ostáva v oddiele „Nejasné a chýbajúce“, kým to niekto nedoplní.

# Zásady obsahu FITagoras

Koreň dokumentov pre každého, kto píše alebo prerába obsah (lekcie, definície, vety, dôkazy, vzorové riešenia,
úlohy). Platí pre LA1 aj DML. Zásady schválil Teo (autor appky) 15. až 17. 11. 2026; pri každej je dátum jeho nálezu.
Technické pravidlá (DSL, id, testy, GEM krokovač, „Ako na to?“) sú v [CONTENT.md](CONTENT.md), overovanie
v [SYSTEM.md](SYSTEM.md). Ak sa niečo z CONTENT.md bije so zásadou tu, platí táto a CONTENT.md treba opraviť.

## Čo čítaš pred písaním (povinne, v tomto poradí)

1. **Tento súbor** celý.
2. **Predmet:** [kurz/la1/README.md](../kurz/la1/README.md) alebo [kurz/dml/README.md](../kurz/dml/README.md):
   prednášajúci, testy, značenie a čo kto chce či zakazuje v celom predmete.
3. **Kapitola:** `kurz/<predmet>/kap<N>-*.md`, balík kapitoly: príbeh, ako ho rozprávajú prednášky, postupy,
   značenie, typické úlohy, chyby študentov, citácie s časom v nahrávke.
4. **Lekcia:** plán lekcie (krok 3 postupu nižšie), ak už existuje.

Balík kapitoly je výťah, nie pravda sama osebe: pred tvrdením, ktoré ide do appky, otvor citovaný zdroj
(skriptá, titulky na danom čase) a over ho. Keď nájdeš v balíku chybu alebo novú prednášku, oprav balík v tej istej
zmene.

## Zásady

Každá zásada: pravidlo, prečo, a príklad z Teových nálezov „takto nie“ / „takto áno“.

### 1. Opravuj systém, nie príklad

Chyba v jednej lekcii je testovací prípad. Nájdi vrstvu, ktorá ju pustila (chýbajúce pravidlo, komponent,
generátor mimo formátovača, chýbajúci kontext v balíku), oprav ju, preveď cez ňu všetko a rozšír širokú kontrolu,
ktorá ju mala chytiť, aby našla každý výskyt v LA1 aj DML. Nie nový test ku každej oprave: testy, ktoré zrkadlia kód,
pripínajú text alebo overujú, že odstránená vec je preč, nepíšeme (Teo 17. 11.; [SYSTEM.md](SYSTEM.md), „Verification“).
Podrobne: [CONTENT.md](CONTENT.md), prvý odstavec.

- ❌ Opraviť jednu kartu, kde sú dve vety, a nechať ostatné. Alebo opraviť ju a pridať test, že práve táto karta má
  jednu vetu.
- ✅ Pravidlo „jedna karta = jedno tvrdenie“ v modeli a kontrola, ktorá nájde všetky takéto karty v LA1 aj DML.
- ❌ V 1.3.13 lišta „Dokazuješ“ vypísala doslova „textstyle“ a všetky testy prešli: kontrola vzorcov nevidela vzorce,
  ktoré skladá model viet (jadro a časti).
- ✅ Kontrola „každý vzorec sa vykreslí len príkazmi, ktoré sadzba pozná“ (`MathSourceTest`) prechádza aj jadrá a časti
  viet; a autor pred odovzdaním pozrel záber kroku s lištou, kde by „textstyle“ videl hneď.

### 2. Postupy tak, ako sa učia na prednáške, proseminári a cvičení

Každý postup (GEM, doplnenie na bázu, súradnice, Euklid, REA, indukcia, dôkaz injektivity…) robíme krokmi
kurzu, aj keď je to o krok dlhšie. Každý krok zdôvodníme: najprv dôvod slovami, potom veta zo skrípt s číslom
(CONTENT.md, „The reason in words, the theorem after it“). Skratky nevykladáme, ale správny výsledok inou cestou
appka vždy uzná. Poradie zdrojov postupu: **skriptá > titulky proseminárov a prednášok > MARAST, riešenia starých
testov, Discord.** Postup ber z balíka kapitoly, oddiel „Postupy“; ak tam nie je, najprv ho doplň zo zdrojov.
(Teo 16. 11.)

- ❌ Doplnenie na bázu vektormi do **riadkov** s citáciou „Klouda, 6. prednáška“. Klouda nič také nepovedal
  a kurz píše vektory do stĺpcov.
- ✅ Vektory do **stĺpcov**: $(\mathbf{u}\ \mathbf{v}\mid \mathbf{e}_1\ \mathbf{e}_2\ \mathbf{e}_3\ \mathbf{e}_4)$,
  GEM do HST, za bázu sa vezmú **pôvodné** vektory na pozíciách hlavných stĺpcov: vedľajšie stĺpce sú kombináciou
  predošlých (Důsledek 2.41), vybraných je $\dim$ LN vektorov, teda báza (Věta 2.55); existenciu dávajú Věty 2.59
  a 2.60. Klouda učí dva spôsoby, oba stĺpcami: „na istotu“ s celou štandardnou bázou a „tipnúť a overiť GEM“
  [la1-P6-par2-klouda 42:49–44:57]. Úloha v appke zatiaľ uzná ľubovoľnú dvojicu štandardných vektorov, ktorá dá
  bázu; voľne zadaná iná báza je otvorená úloha.
- ❌ Výklad súradníc vzhľadom k báze riešením rovníc dosadzovaním (jednu neznámu vyjadriť a dosadiť do ďalšej)
  namiesto GEM.
- ✅ Výklad: GEM rozšírenej matice $(\mathbf x_1 \cdots \mathbf x_d \mid \mathbf v)$ do HST a súradnice z nej, ako
  Postup 2.69 a Kleprlík [la1-P6-par1-kleprlik 58:01–1:00:25]. Spätné dopočítanie z HST (alebo dotiahnutie na
  jednotkovú maticu ako Příklad 2.71) je súčasť GEM, nie „dosadzovanie“. Správny výsledok inou cestou appka uzná: kurz
  ju nezakazuje, len ju neučí (balík kapitoly 2, oddiel 4).

### 3. Formálne a presne podľa skrípt všetko, čo sa píše na papier

Doslova a so všetkými predpokladmi: znenia viet a definícií, lišta „Dokazuješ“, vzorové riešenia a kroky dôkazov,
odpovede „definuj“, kvízové možnosti, ktoré parafrázujú definíciu. Ľudsky smie ostať vysvetlenie, nápoveda,
spätná väzba a nadpisy. Každá veta a definícia je v appke aspoň raz v plnom znení; skrátená verzia sa nikdy
netvári ako tvrdenie (je označená ako jadro, plné znenie je o ťuk ďalej). Doslova je **vzorové** znenie; študentovu
odpoveď („definuj“, „vyslov“) hodnotíme podľa významu a toho, či má všetky podmienky a kvantifikátory, nie podľa
zhody slov. Kleprlík: „my to chceme, aby to mělo stejný význam, jako v těch skriptech“ [la1-P4-par1-kleprlik 1:10:15];
Krásenský: „Můžou tam být prohozeny nějaké slova, ale matematicky to musí znamenat tady tyhle věci.“
[la1-P6-par3-krasensky 55:28]. Ekvivalentná veta ale nie je definícia (README LA1, zhrnutie bod 2).
(Teo 16. 11..)

- ❌ Lišta „Dokazuješ“ pri Vete 6.2 ukazuje len $a = qd + r \wedge 0 \le r < d$: chýbajú predpoklady aj kvantifikátor.
- ✅ Celé tvrdenie ako v skriptách: „Nechť $a \in \mathbb{Z}$, $d \in \mathbb{N}$. Pak existují jednoznačně určená
  čísla $q, r \in \mathbb{Z}$ taková, že $a = qd + r \wedge 0 \le r < d$.“ a viditeľné dve časti dôkazu:
  existencia a jednoznačnosť. Zbalená lišta smie byť krátka, ťuknutím sa rozbalí celé znenie.

### 4. Kontrolný zoznam „z pamäti“ obsahuje len to, čo zadanie žiada

Pri úlohe „napíš definíciu / vetu“ sa počíta len to, čo zadanie naozaj chce. Ostatné, čo skriptá k pojmu dodávajú,
môže byť v zozname ako „navyše zo skrípt“, ale nepočíta sa a nevracia úlohu do opakovania. (Teo 16. 11..)

- ❌ „Napište definici relace dělitelnosti“: zoznam vyžaduje aj „dělitel, násobek, dělitelné“ a „$a \nmid b$
  a relace dělitelnosti“, kým to nie je odškrtnuté, definícia sa vracia do opakovania.
- ✅ Povinné: $a, b \in \mathbb{Z}$; existuje $k \in \mathbb{Z}$; $a \cdot k = b$. Navyše (nepočíta sa): názvy
  dělitel a násobek, značka $a \nmid b$.

### 5. Všetci prednášajúci a proseminár, vždy s menom

Obsah aj klipy vyberáme zo všetkých paraleliek a proseminárov, nie len z Teovej (test je spoločný). Pri každej
poznámke a klipe uvedieme, kto to povedal. Keď sa prednášajúci líšia (čo smieš, značenie, postup), riadime sa
skriptami alebo prísnejším a rozdiel študentovi povieme. Načasovanie „po prednáške“ sa riadi paralelkou, ktorú si
študent zvolil. (Teo 16. 11..)

- ❌ Lekcie LA1 citujú takmer len Kloudu; Kleprlík a Krásenský sú len pri 4. prednáške.
- ✅ Balík kapitoly má pri každom postupe a dôraze všetky tri paralelky aj proseminár s časom v nahrávke; lekcia
  cituje toho, kto to povedal najjasnejšie, a rozdiel, ak nejaký je.

### 6. Názvy a značenie z kurzu, nie z učebníc

Pojmy, skratky a symboly berieme zo skrípt a z prednášok, nikdy zo všeobecných učebníc. Zákazy a zvyklosti
prednášajúcich sú v README predmetu a v balíku kapitoly (s citáciou); technický zoznam zakázaných slov je
`data/Terminology.kt` (`TerminologyTest`). Pred novým pojmom alebo skratkou ho over v skriptách (LA1:
`chap-12-znaceni`). Platí aj vtedy, keď niekto z prednášajúcich iné slovo mimochodom vysloví (Kleprlík: „někdo tomu říká pivoty“
[la1-P3-par1-kleprlik 59:16]): appka píše slovo skrípt. (Teo 15. a 16. 11.)

- ❌ DML lekcia „NSD, NSN a nesúdeliteľnosť“ so vzorcami gcd/lcm; skriptá skratky NSD/NSN nepoužívajú.
- ✅ „gcd, lcm a nesúdeliteľnosť“: meno vypísané slovom a značka $\gcd$, $\lcm$ ako v Definici 6.3.
- ❌ „pivot“, „schodovitý tvar“, $R_2 \to R_2 - 2R_1$.
- ✅ „prvý nenulový prvok riadku“, „horný stupňovitý tvar (HST)“, „(G3) ř2 − 2·ř1“.

### 7. „Prečo?“ musí pridať informáciu

Po odpovedi appka ukáže prvú vetu vysvetlenia, zvyšok je za „Prečo?“ (CONTENT.md, „First sentence rule“). Čo je za
„Prečo?“, musí niečo vysvetliť: prečo je správna možnosť správna a prečo sú ostatné zlé. Ak je vysvetlenie jedna
veta, „Prečo?“ sa neukazuje. (Teo 16. 11..)

- ❌ „Ktorá rovnosť je zmysluplná?“ → po rozbalení „Prečo?“ len „Len c).“
- ✅ „Len c): báza má 4 prvky, súradnice sú preto štvorica. Súradnice sú koeficienty lineárnej kombinácie vektorov
  bázy, jeden pri každom z nich (Definice 2.68). Dvojica a trojica nemajú pri štyroch vektoroch bázy zmysel.“

### 8. Jedna karta = jedno tvrdenie

Karta zo skrípt nesie jednu definíciu, vetu, lemmu či pozorovanie. Krok, ktorý potrebuje len časť (napr. bod 3
lemmy), ukáže tú časť a celé znenie je o ťuk ďalej. Nadpis karty patrí jej tvrdeniu. Popis zdroja je krátky
(„Lemma 6.1, bod 3“), nie dlhý text malým písmom. (Teo 16. a 17. 11..)

- ❌ Euklid, krok „Odčítanie násobku gcd nemení“: Lemma 6.1 a Věta 6.3 v jednej karte, veta sa na konci odreže.
  Věta 6.4 a 6.5 (Bézout, REA) a poznámka v jednej karte. LA1: 2.59 + 2.60 a 2.56 až 2.58 v jednej karte s nadpisom
  podľa prvej.
- ✅ Krok ukáže Lemma 6.1, bod 3: $\gcd(a + cb, b) = \gcd(a, b)$; Věta 6.3 je vlastná karta v kroku, kde sa
  používa.

### 9. Odkaz a vyskakovacie okno len vtedy, keď ukážu niečo nové

Odkaz na tvrdenie (a jeho okno) má zmysel, len ak pridá: celé znenie, dôkaz, kde sa tvrdenie ešte používa. Všetky
citované tvrdenia sú ťukateľné rovnako. (Teo 16. a 17. 11..)

- ❌ Odkaz „Lemma 6.1“ otvorí okno s presne tým istým textom, aký je na karte, a „celá veta“ dole znova to isté.
  „Tvrzení 6.3“ v riešení lcm sa naopak ťuknúť nedá.
- ✅ Odkaz na kartu, ktorá je celá na obrazovke, nie je; odkaz na skrátené jadro otvorí plné znenie a dôkaz.

### 10. Každá veta má výslovný stav dôkazu

Pri každej vete je jedno z: dôkaz (ako v skriptách alebo na prednáške), krátky argument, „nepreberá sa“ so
zdrojom, kto to tak povedal. Nikdy ticho chýbajúci dôkaz. Stav pre každú vetu kapitoly je v balíku, tabuľka
„Pojmy a tvrdenia“. (Teo 16. 11.)

- ❌ Lemma 6.1 v appke bez dôkazu, hoci skriptá ho majú (bod 3: rovnaké množiny spoločných deliteľov,
  `chap-06-tc.woo:586`).
- ✅ Bod 3 s krátkym dôkazom zo skrípt; bod 1 „Je zřejmé z definice“, bod 2 jednou vetou zo skrípt (čísla $a, b$
  a $|a|, |b|$ majú rovnaké množiny spoločných deliteľov aj násobkov), `chap-06-tc.woo:586–598`.

### 11. Štruktúra zo skrípt, sadzba pre mobil

Znenia preberáme so štruktúrou zo skrípt (zarovnania, tabuľky, odrážky), nie prepisované ručne do riadkov textu.
Cieľ nie je kópia rozloženia skrípt, ale čitateľnosť na mobile: vzorec sa na šírke 320 dp nesmie lámať; ak sa
nezmestí, preštruktúrujeme ho (nie zmenšíme); súbežné veci dáme pod seba; dlhé formálne znenie najprv jadrom,
celé znenie zo skrípt rozbaliteľné; žiadne dlhé vzorce vnútri odseku. (Teo 16. 11..)
Výrok sa láme čo najmenej a len na logických miestach (Teo 18. 11.): jeden logický riadok s mäkkými zlommi
(`\softbreak`, docs/CONTENT.md), zlomy podľa šírky telefónu, nie pevné riadky; kvantifikátory s „LN ⇔“ na jednom
riadku, telo na druhom, zlom pred ⇒ alebo ∧ až keď sa nezmestí, s odsadením.

Sadzbu existujúcich lekcií hromadne neprerábaj bez dohody v issue: najprv ukážka na jednej lekcii.

- ❌ REA (Věta 6.5) sploštená do riadkov textu, ktoré sa lámu.
- ✅ Úzka tabuľka $n \mid r \mid \alpha \mid \beta$ (počiatočné riadky, potom rekurentný riadok), alebo každá
  postupnosť zvlášť rovnakým vzorom; jadro „každý riadok: $r = \alpha a + \beta b$“, celé znenie po ťuknutí.

### 12. Lekcia má dych: problém → nápad → tvrdenie → dôkaz → čo z toho máme

Skriptá sa čítajú ako príbeh, appka nesmie byť kôpka izolovaných kúskov. Každá veta príde s motiváciou (aký problém
rieši) a s tým, čo nám dáva; dôkaz začne myšlienkou jednou vetou; medzi krokmi sú prechody; začiatok lekcie
nadväzuje na minulú. Príbeh kapitoly, ako ho rozprávajú prednášky, je v balíku kapitoly, oddiel 2. Motivácie a prechody
existujúcich lekcií hromadne neprerábaj bez dohody v issue: najprv ukážka štýlu na jednej lekcii. V novej alebo prerábanej lekcii sa drž príbehu z balíka
a nevymýšľaj vlastný. (Teo 16. 11..)

- ❌ Lekcia začne kartou „Věta 6.3 (Eukleidův algoritmus)“ bez otázky, prečo ho potrebujeme.
- ✅ Problém (gcd veľkých čísel bez rozkladu na prvočísla; skriptá najprv počítajú $\gcd(254, 158)$ „středoškolsky“
  rozkladom, `chap-06-tc.woo:654`) → nápad (odčítanie násobku gcd nemení, Lemma 6.1, bod 3)
  → algoritmus (Věta 6.3) → čo z toho máme (gcd v pár krokoch, REA neskôr dá aj Bézoutove koeficienty).

### 12a. Appka má vlastné formáty úloh; formáty MARASTu nenapodobňuje

Lekcia učí výkladom a krokmi, ktoré sú rovnaké v každej lekcii (otázka pred výkladom, výklad, obrázok, kontrolná
otázka, vzorový príklad, príklad z generátora, klip, definícia, veta, dôkaz) a končí ako ostatné. Záložka Test má vlastné
šablóny a písomky na papier. Nič v appke nenapodobňuje rozhranie ani typ otázky MARASTu (výroky ✓ / – / ✗ a podobne):
tie patria do MARASTu. Čo by také otázky skúšali, precvičia vlastné kroky a šablóny appky. MARAST smie byť zdrojom
faktu alebo úlohy (riadok „Zdroj“, stránka o teste), nikdy formátom ani nadpisom kroku. (Teo, 18. 11.: „ani nechcem
otázky vo formáte MARASTu, to tam nemajú čo robiť, tie majú byť v MARASTe; my máme vlastné príklady a formáty“.)

- ❌ Na konci lekcie výklad „Ako sa to pýta MARAST“ a úlohy s výrokmi ✓ / – / ✗; v Nácviku šablóna „Otázky ako v MARASTe“.
- ✅ Počty podpriestorov precvičí šablóna „Koľko podpriestorov v Z_p^n“ a kontrolné otázky lekcie; MARAST ostane ako
  zdroj („Hodnotí sa najmä postup… (MARAST, lekce 292)“).

### 13. Vzorové riešenia počíta engine

Vzorový príklad zručnosti, ktorá má generátor, sa počíta enginom (GEM ako `GemWalk`, Euklid a mocniny cez spoločné
komponenty), nikdy ručne napísané matice a výpočty vedľa generátora. (CONTENT.md, „GEM krok
po kroku“.)

- ❌ Ručne napísaná `pmatrix` reťaz GEM riadkami v lekcii, hoci generátor tej istej zručnosti počíta stĺpcami.
- ✅ `GemWalk.of(...)` z tých istých dát ako generátor, takže výklad a príklad sa nemôžu rozísť.

### 14. Poradie práce, len odprednášané, nič vymyslené, nič z otvorených kvízov

- **Poradie (Teo 16. 11.):** najprv systém (štruktúra viet a definícií, stav dôkazov, vzorové príklady
  enginom, navigácia, kontroly), potom kontroly vygenerujú zoznam chýb v celom LA1 a DML, potom opravy, **až potom
  nové kapitoly**. Kapitola, ktorá má balík, ale nemá lekcie (LA1 kap. 1, DML kap. 2 a 3), nie je pozvánka písať
  lekcie teraz; nové kapitoly začni až po dohode v issue.

- Nové lekcie len k témam odprednášaným aspoň v jednej paralelke (CONTENT.md, „Only what has been lectured“). Čo je
  odprednášané a kde, je v balíku kapitoly, oddiel „Rozsah a zdroje“.
- Žiadny vymyslený fakt o kurze: čo prednášajúci povedal, musí byť overiteľné (titulky s časom, stránka predmetu).
- Nič z otvoreného kvízu MARAST, ani parafráza (CONTENT.md, „Open graded quizzes“).

## Postup tvorby lekcie

Pred každou novou lekciou alebo prerábkou (Teo 16. 11.):

1. **Zdroje.** Načítaj balík kapitoly a over, že je aktuálny: pribudla odvtedy prednáška alebo proseminár? Ak áno, doplň balík (postup: [kurz/README.md](../kurz/README.md)). Over, že zdroje sú naozaj
   dostupné (titulky niektorých proseminárov chýbajú, balík to uvádza).
2. **Výťah.** Z balíka a zo zdrojov vypíš pre lekciu: čo prednášajúci chcú a nechcú, postup a jeho kroky, značenie,
   typické úlohy, časté chyby. Každé tvrdenie s citáciou. Čo v balíku chýba, dopíš doň.
3. **Plán lekcie** v `kurz/<predmet>/plany/<id lekcie>.md`: príbeh (problém → nápad → tvrdenie → dôkaz → čo z toho
   máme), kroky lekcie, ktoré tvrdenia a v akom znení, ktoré klipy (kto, čas), postup a jeho zdôvodnenia, úloha bez
   pomoci na konci, nadväznosť na predošlú lekciu.
4. **Písanie** podľa plánu, zásad tu a technických pravidiel v [CONTENT.md](CONTENT.md).
5. **Kontrola** pred odovzdaním:
   - prejdi zásady 2 až 14 po jednej proti svojej zmene (každý príklad „takto nie“ je vzor chyby, ktorú hľadáš);
   - citácie: náhodne otvor aspoň 5 citovaných miest (skriptá, titulky na danom čase);
   - testy podľa úrovní v [SYSTEM.md](SYSTEM.md), „Verification“: počas práce len dotknuté triedy
     (`./gradlew :shared:jvmTest --tests '*.WordingTest'`), raz pred odovzdaním celé
     `./gradlew :shared:jvmTest :app:assembleDebug`, zábery a Robolectric pred vydaním alebo keď
     meníš ich oblasť;
   - nezávislá revízia (iný človek, alebo model inej rodiny než ten, ktorý text písal): vernosť kurzu (proti balíku a zdrojom), matematika, slovenčina;
   - záber každého zmeneného kroku pozri očami študenta po prednáške (`:app:snap`, svetlá aj tmavá, 320 dp): surový
     príkaz sadzby („textstyle“, `\mathbb`, `§`), rozsypaný alebo orezaný vzorec je chyba, aj keď testy prešli.

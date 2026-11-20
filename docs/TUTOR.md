# Tútor cez ChatGPT

Pre používateľa sa pomocník volá **Pytagoras** (v appke, v README aj na stránke; Teo, 20. 11.). `tutor` a „tútor“
ostávajú technický názov v kóde a v tomto dokumente.

**Všeobecný tútor (Teo, 18. 11.).** Tútor nie je funkcia pre GEM, ale jedna slučka pre ľubovoľné miesto appky:
vstup = kde študent je (krok lekcie, karta vety či definície, príklad, písomkový príklad; `tutor/Miesto.kt` ich
serializuje z modelu appky), voliteľné fotky papiera a voliteľná otázka (bez nej „skontroluj mi to“). Model odpovedá
z miesta a z balíkov kurzu; čo vie appka vypočítať alebo overiť, je **nástroj** (`tutor/Agent.kt`, zoznam `Nastroje`)
a kde nástroj existuje, model ho musí použiť a jeho verdikt je záväzný. Nový nástroj = jedna položka v zozname, žiadna
nová obrazovka. Pôvodné reťazce sú nástroje: `over_postup_gem` (slepé čítanie GEM + `OverPostup` + fakty), `posud_papier`
(slepé čítanie textu + posúdenie bodov kontrolného zoznamu s citátom), ďalej `gem`, `la` (báza, LN, obal, súradnice
v ľubovoľnej, aj študentovej báze), `cisla` (Euklid, REA, inverzia, mocnina, kongruencie, ČVZ), `tvrdenie` (znenie zo
skrípt), `kurz` (balíky `kurz/**/*.md` pribalené pri builde do `files/kontext`). Úlohy s viac správnymi výsledkami sa
neposudzujú zhodou so vzorom, ale overením vlastnosti (`la`). Strop: 4 kolá, 10 volaní nástrojov na otázku.

V UI je jeden vstup: „Opýtať sa“ v hlavičke každého kroku, príkladu a písomkového príkladu (`LocalTutorMiesto`) a
„Odfoť riešenie“ pri každom kontrolnom zozname „na papier“ a v spätnej väzbe po zlej odpovedi; oboje otvorí ten istý
panel (`screens/TutorPanel.kt`), ktorý odpoveď kreslí komponentmi appky (verdikt GEM so študentovým krokovačom,
posúdené body zoznamu, karta vety `<veta cislo="…"/>`, krokovač nástroja `<krokovac ref="k1"/>`). Tútor vidí každý,
nie je za vývojárskym režimom ani za bránou; prvé použitie ukáže jednu obrazovku, čo sa posiela (fotka a text úlohy do
ChatGPT cez vlastný účet, tokeny len v telefóne), a prihlásenie. Čísla harnessu sa merajú a hlásia ďalej, podmienkou
viditeľnosti nie sú.

Pôvodný opis prípadu 1 (fotka GEM po zlej odpovedi) nižšie platí pre nástroj `over_postup_gem`.

## Pohyb a vstup (19. 11.)

Teo: „tútora chcem fakt veľmi poriadne zakomponovať“, „streaming a natívny feeling“. Odpoveď nie je prilepený chat:

- **Písanie** (`ui/Pisanie.kt`): text zo streamu ide na obrazovku po slovách stálym tempom, ktoré dobieha nárazy siete
  (aspoň 55 znakov/s, zvyšok sa zmenší na 1/e za 0,45 s); nikdy nie uprostred vzorca ani značky (`rez`), vzorec sa
  objaví celý vysádzaný, karta vety a krokovač celé na svojom mieste. Každé nové slovo nabehne za 260 ms (`Rich` kreslí
  cez hotový riadok, nič sa znova nerozkladá), riadky rastú len na konci. Posun drží koniec v zábere, kým študent sám
  neroluje hore; dole alebo novou otázkou sa znova pripojí. „Stop“ (tlačidlo odoslania počas písania) ukončí odpoveď
  tam, kde je, a ukáže všetko prijaté.
- **Čakanie** (`Cakanie`): tri bodky v zelenej appky a čo tútor robí („Čítam papier“, „Overujem výpočet“, „Hľadám
  vetu v skriptách“, `StavNastroja`); bez odhadu času. Prvý stav sa objaví v tom istom snímku ako ťuk.
- **Posudok v kroku** (`PosudokKarta`, Teo 20. 11.): poradie ako pri opravenej písomke. Navrchu „Tvoj papier“, čo
  Pytagoras prečítal, bežným písmom (hneď po čítaní, ešte počas posudzovania; nad 10 riadkov prvé riadky a „Zobraziť
  celé“), potom verdikt, prvá veta ako dôvod a zvyšok pod obyčajným nadpisom „Čo opraviť“ (bez čiary a farby). Verdikt
  ide v schéme prvý, appka ho ukáže hneď, ako ho stream pomenuje (`verdiktVPrude`, len zobrazenie; platí posudok
  rozobraný na konci), na mieste čakania; karta sa zafarbí (zelená uznal, terakota neuznal), farbu má len verdikt.
  Žiadny riadok, ktorý len hovorí, kto hovorí: v paneli to povie hlavička, v kroku verdikt („Pytagoras by to
  neuznal“). Kontrola GEM má to isté poradie; že chybu našiel výpočet appky, hovorí pod verdiktom „Každú úpravu
  prepočítala appka.“
- **Vstup**: fotky z foťáka čakajú pod poľom (odobrať, pridať), Enter alebo kláves odoslania pošle, 2–3 tiché čipy
  podľa miesta (`navrhy`). Druhá otázka počas písania čaká na prvú (jedna história).
- **Skutočné streamy**: `scripts/tutor.sh tutor … --zaznam FILE` zapíše udalosti streamu tak, ako ich dostane
  obrazovka (kúsky textu, stavy, resety, skorý verdikt, s časom, bez tokenov), `--stop-po N` zastaví po N znakoch ako
  „Stop“. 11 záznamov z 19. 11. (otázky s nástrojmi `la`, `tvrdenie`, `cisla`, `gem`, značky `<veta/>` a
  `<krokovac/>`, `$$…$$`, posudok papiera a GEM z Teových fotiek, dve zastavenia) je v
  `shared/src/jvmTest/resources/tutor/stream/`; `StreamZaznamyTest` ich prehrá cez písanie na virtuálnych hodinách:
  na obrazovke je vždy začiatok prijatého textu, nikdy otvorený vzorec ani značka, na konci celá odpoveď.
- **Prihlásenie** sa dá pri „Čakám na prihlásenie“ zrušiť (`zrusPrihlasenie`: telefón zavrie poslucháča na
  127.0.0.1, web zabudne rozbehnutý pokus).
- **Overenie pohybu**: `TutorPohybTest` (desktop, virtuálny čas, model s nárazmi tokenov) zapíše snímky každých 40 ms
  s `-Ppohyb.out=DIR`; bez neho sa preskočí.

## Kde je čo

| čo | kde |
|---|---|
| prihlásenie (PKCE, poslucháč na 127.0.0.1, vloženie callback URL, overenie ID tokenu, obnova s rotáciou, revokácia) | `shared/src/jvmCommonMain/.../tutor/siwc/Siwc.kt` |
| klient Responses (`stream:true`, `store:false`, SSE, chyby → `TutorChyba.Druh`) | `.../tutor/siwc/HttpResponses.kt` |
| trezor: telefón Keystore AES-GCM v `noBackupFilesDir`, harness súbor 0600 v `~/.fitagoras` | `androidMain/.../tutor/KeystoreTrezor.kt`, `jvmMain/.../tutor/harness/SuborovyTrezor.kt` |
| čítanie (pokyn, schéma `Prepis`, slepé: len zadanie, nie riešenie) | `commonMain/.../tutor/Prepis.kt`, `Retazec.kt` |
| kontrola enginom | `commonMain/.../tutor/OverPostup.kt`, zápis buniek a úprav `Zapis.kt` |
| ktoré GEM úloha chce (z jej vlastného riešenia, `GemWalk.pole`) | `commonMain/.../tutor/Ulohy.kt` |
| nadpis (píše appka), vstup a pokyn vysvetlenia | `commonMain/.../tutor/Retazec.kt` |
| krokovače verdiktu (študentov postup, správny krok) | `commonMain/.../screens/Tutor.kt`; karta v `screens/Settings.kt` |
| foťák (CameraX v appke), služba v popredí, pripojenie | `androidMain/.../tutor/TutorFotak.kt`, `AndroidTutor.kt` |
| web: prihlásenie vložením adresy, klient cez `fetch`, výber fotky | `wasmJsMain/.../tutor/WebSiwc.kt`, `WebResponses.kt`, `WebTutor.kt`, `resources/web.js` |
| harness | `jvmMain/.../tutor/harness/`, spúšťa `scripts/tutor.sh` (`tutor --miesto <route> [--otazka …] [FOTKA…]`, `papier posud`) |
| všeobecná slučka, nástroje, značky | `commonMain/.../tutor/Agent.kt`; miesto `Miesto.kt`; papier `Posudok.kt`, `NaPapier.kt` |
| panel a vstup | `commonMain/.../screens/TutorPanel.kt`, prvé prihlásenie a bloky `screens/NaPapier.kt` |

## Web (18. 11.)

Teo: tútor aj „z webu pre iPhone“, hoci webovú cestu dokumentácia OpenAI nepopisuje. Na webe beží tá istá slučka,
inak len prihlásenie, sieť a foťák (`shared/src/wasmJsMain/.../tutor/`):

- **Prihlásenie** (`WebSiwc.kt`): ten istý verejný klient, PKCE a presmerovanie na `http://127.0.0.1:1455/auth/callback`
  ako na telefóne. Na 127.0.0.1 v prehliadači nič nepočúva, prihlásenie skončí na stránke „nedá sa pripojiť“ a študent
  jej adresu skopíruje späť do appky (záchranná cesta telefónu; tlačidlo „Vložiť skopírovanú adresu“ číta schránku, pole
  pod ním je pre prehliadač, ktorý schránku nedá). Nové okno sa otvára ešte v ťuknutí a adresu dostane po výpočte PKCE.
- **Tokeny** sú v `localStorage` tohto prehliadača (kľúč `fitagoras.tutor`), nešifrované; idú len na `auth.openai.com`
  a `api.openai.com` priamo zo stránky (obe odpovedajú CORS `*`, overené 18. 11. aj na skutočný POST). Náš server ich
  nevidí. Dokumentácia pre plánové prihlásenie odporúča tokeny mimo úložiska prehliadača: to je vedomá odchýlka.
- **ID token** sa neoveruje podpisom: JWKS (`/.well-known/jwks.json`) nemá CORS, stránka ho neprečíta. Prišiel priamo
  z token endpointu cez TLS, čo OpenID Connect Core 3.1.3.7 (6) pripúšťa namiesto podpisu; iss, aud, exp, nonce a sub
  sa kontrolujú.
- **Foťák**: výber súboru prehliadača (iOS ponúkne fotoaparát aj knižnicu), JPEG najviac 2048 px bez EXIF (canvas).
- **Riziko**: OpenAI môže túto cestu kedykoľvek zatvoriť (CORS, presmerovanie, kontrola klienta). Každé zlyhanie je
  `TutorChyba` a obrazovky padnú späť na cestu bez AI („Mám to / Nemám“), ako pri vyčerpanom limite.

## Ako kontroluje engine

Krok $M_k \sim M_{k+1}$ je správny práve vtedy, keď majú obe matice rovnaký riadkový priestor (riadkové úpravy sú
invertovateľné). Preto prejde ľubovoľné poradie a mix úprav aj nenapísané úpravy. Zlý riadok je riadok mimo
predošlého riadkového priestoru. Bunku engine menuje:

- **isto**, keď ju určujú študentove napísané úpravy, alebo keď každá kombinácia jedného či dvoch starých riadkov,
  ktorá sedí so zvyškom riadku, ukazuje na tú istú bunku s tou istou hodnotou;
- **pravdepodobne** („najskôr má byť…“), keď úpravy napísané nie sú a bunku určí úprava, na ktorú ukazujú ostatné
  riadky toho istého ∼ (riadok sám ostáva, odčíta sa ten istý riadok ako v ostatných). Takto to číta človek, ale
  nie je to dôkaz;
- inak len riadok.

Či je zlá hodnota chyba študenta, alebo zlé prečítanie, rozhodne ďalší krok: chyba sa šíri, zlé prečítanie nie.
Keď ďalší krok sedí s opravenou hodnotou, appka sa opýta („Neviem rozlíšiť jedno číslo“) a odpoveď prepočíta engine bez modelu. Chyba
v poslednej matici nemá ďalší krok, preto je značka sivá a vedľa nej „Prečítal som zle, je tam …“.

**Súdržnosť.** Krok, ktorý zachová riadkový priestor, no posunie prvý nenulový prvok niektorého riadku doľava (po
zoradení riadkov), nie je krok GEM: takmer vždy ide o riadky susedných matíc prečítané do seba. Engine vtedy vráti
`NESUDRZNE` a appka povie „Neviem spoľahlivo prečítať, ktorá matica je ktorá“ a nič neoznačí. Radšej „neviem
prečítať“ než nepravdivá veta. Nedokončený postup je podmienka, nie tvrdenie: „Posledná matica na fotke ešte nie je
hotová; ak máš na papieri ďalšiu, odfoť celý postup.“

**Foťák** radí len „Odfoť svoje riešenie, celý papier ostro“; orezanie ani „jedna úloha na fotku“ nie je. Strana môže
mať viac úloh (Teove pracovné strany z 18. 11.): čítač celý prepíše len reťazec zadanej úlohy, z ostatných dá prvú
maticu a engine ich podľa nej odloží (`INE_ZADANIE`). Reťazec, ktorý pokračuje na ďalšom riadku a čítač ho rozdelil,
engine spojí (`spojPokracovania`). Na 17 skutočných fotkách (12 strán s viacerými úlohami) bola vybraná správna
úloha 17/17.

## Harness

Beží tým istým kódom ako telefón, na počítači cez ChatGPT prihlásenie (nie platené API). Zostavenie raz:
`./gradlew :shared:tutorHarness`, potom bez Gradle:

| príkaz | čo |
|---|---|
| `scripts/tutor.sh login` | vypíše URL a čaká na callback na 127.0.0.1:1455 (z iného počítača cez `ssh -N -L 1455:127.0.0.1:1455 <stroj>`); `login --url '<adresa>'` dokončí s vloženou adresou |
| `stav`, `modely`, `obnov`, `odhlas` | pripojenie, modely účtu, vynútená obnova s rotáciou, revokácia |
| `data k --n 12000` | **K**: engine bez modelu na vygenerovaných postupoch (simulátor študenta `Student.kt`, chyby E1–E6, E9) |
| `data fotky --out DIR [--realne]` | syntetický „rukopis“ (`Rukopis.kt`): písma OFL alebo skutočné ťahy ľudí (MathWriting `symbols`), papier, tieň, perspektíva, otočenie, JPEG |
| `citanie --sada DIR …` | **Č**: fotky so známym obsahom cez čítanie a engine; cache v `DIR/beh-<konfigurácia>`; `--beh <konfigurácia>` preráta starší beh bez volania modelu; `zavadzajuce_vety` = vety, ktoré by študent čítal a nie sú pravdivé |
| `vysvetlenie --n 120` | **V**: vysvetlenia vygenerovaných verdiktov; strojové kontroly a export pre sudcu z inej rodiny |
| `sonda` | čo route prijme (úsilie, `json_schema` s obrázkom, tokeny strany) |
| `data brana …` | z výsledkov zapíše `composeResources/files/tutor/brana.json` |

Pravda fotky je to, čo je na nej naozaj nakreslené. Staršie sady mali listy, na ktorých generátor pri spodnom okraji
prestal kresliť a pravda obsahovala aj nenakreslené matice (10 zo 177 fotiek); harness ich porovnáva len s
nakreslenými a generátor taký list už zahodí. Práve tieto listy boli „zavádzajúce vety na hustých listoch“ z 2. kola.

Dáta harnessu (písma, MathWriting, sady, behy) sú mimo repozitára: `$FITAGORAS_TUTOR_DATA`, inak
`~/.fitagoras/tutor-data` (`harness/DataDir.kt`). Malá vzorka
K (1 500 prípadov) je aj v `jvmTest` (`OverPostupTest`), ťuky obrazovky v `TutorFlowUiTest`.

## Brána

Od kola 4 je tútor viditeľný pre každého (Teo, 18. 11.: „zatiaľ tú appku nikto nemá“); `files/tutor/brana.json`
a vývojársky režim ho už neskrývajú. Hranice nižšie ostávajú meradlom: čísla sa uvádzajú, nie sú podmienkou.

## Skutočný rukopis

Sada `data/sady/teo-real` (mimo repozitára, súkromná; fotky rukopisu do repa nepatria): skutočné pracovné strany
z 18. 11. (22 fotiek, viac úloh na strane, škrty, pod uhlom) a 4 staré. Prepis každej strany spravil iný model, `scripts/tutor.sh realne over` ho prejde enginom; do tvrdých metrík ide len reťazec GEM, ktorý engine
uzná ako súdržný (4 z nich som overil aj okom na fotke), plus 5 starých fotiek s Teovou skutočnou chybou. Spolu 17
fotiek, 13 strán; strana s prepísanou chybnou bunkou a reťazce s parametrom ostali mimo. Definície, dôkazy a DML na
ostatných stranách sú mäkká sada `matica-realne-foto.jsonl` (6 otázok s fotkou, pravdu som overil na fotke).

## Stav po kole 4 (18. 11.)

| čo | na čom | hranica | výsledok |
|---|---|---|---|
| falošný poplach pri čítaní | skutočný rukopis, 29 správnych prechodov | ≤ 1 na ~300 | 0/29 (terra low) |
| chybná bunka prečítaná tak, ako je napísaná | skutočný 3, syntetické listy 9 | vždy | 3/3, 9/9; 0 „opravených“ |
| nájdená chyba | skutočný 3, syntetický 10 | ≥ 92,5 % | 3/3, 9/10 (1 „nečitateľné“, nie zlé miesto) |
| správna úloha na strane s viacerými | 12 strán s viacerými GEM | — | 17/17 |
| zavádzajúce vety | 17 skutočných fotiek | 0 | 0/17 (po spojení rozdeleného reťazca) |
| kvalita vysvetlení (1–5) | sada 2468, 160 textov, nikdy neladená | ≥ 4,3 | sudca 1: 4,49 (polovica, 80), sudca 2: 4,56 (160) |
| rozpor so slovami enginu | sada 2468 | 0 | sudca 1: 0/80, sudca 2: 2/160 (v116 domnienka, v157 súradnice pri prehodených riadkoch; obe opravené v pokyne a faktoch) |
| prezradenie | sada 2468 | 0 | sudca 1: 0/80, sudca 2: 1/160 (v157) |
| posudok papiera | 16 prepisov (8 inak formulovaných správnych, 8 chybných) | — | 15/16 (kongruencia cez zvyšky uznaná) |
| otázky textom s nástrojmi | 8 otázok, sudca 2 | 0 omylov | 0 omylov, 4,88 |
| čas | čítanie skutočnej strany / otázka textom / posudok | ~15 s / ~5 s | 12 s / 8 s / 5 s |

Neoverené: prihlásený tútor v telefóne (emulátor bez prihlásenia ukáže len prihlásenie), foťák živo; nové modely len
cez harness (ten istý kód). Skutočného rukopisu so skutočnými chybami je stále málo (3 chyby).

## Vysvetlenie z faktov

Model nedostáva verdikt na interpretáciu, ale očíslované FAKTY enginu (čo sa stalo, kde, akou úpravou, odvodenie bunky,
správna matica alebo odporúčaný krok; pri správnom postupe hlavné stĺpce a nenulové riadky poslednej matice a pri
úlohe s jednou GEM aj veta vzorového riešenia, ak má rovnaké hlavné stĺpce) a smie ich len preformulovať.
Pri chybe na papieri, keď je posledná matica (aj s chybou) v HST, fakty opíšu aj ju (nenulové riadky, hlavné stĺpce,
pravá strana); model z nej dotiahne reťazec dôsledkov podľa kurzu a povie, či je chyba aj v závere (sudcovia v 2. kole:
v 18 zo 75 textov študentova vlastná matica dávala inú odpoveď, než zadal, a text to zamlčal). Pri správnom postupe
model dotiahne dôsledky z poslednej matice až k správnej odpovedi, ktorú appka už ukázala („vedľajší stĺpec → voľná
neznáma → … → LZ“). Fotka len s maticou zadania nie je „všetky úpravy správne“.
`kontrolaVysvetlenia` po vygenerovaní odmietne text s číslom mimo vstupu alebo s úpravou po chybnej (poslednú maticu
smie pomenovať „matica po n. úprave“, keď ju fakty opisujú); potom raz znova, inak sa ukážu fakty samé (v 2. osobe).

## Výsledky harnessu (18. 11., kolo 3)

| meranie | pred kolom 3 | po kole 3 |
|---|---|---|
| zavádzajúce vety na fotkách (177, z toho 70 hustých listov) | 12/177 (pravda s nenakreslenými maticami) | 0/177 (pôvodný pokyn čítania; s počítaním ∼ 1/177) |
| Č2 falošný poplach / Č4 nájdená chyba / Č5 pochválené zlé | 0/381 · 59/59 · 0/59 | 0/365 · 59/59 · 0/59 |
| K engine | 12 000/12 000 | 12 000/12 000 |
| V2 rozpor s enginom (sudcovia z inej rodiny modelov, čerstvá sada) | 1/120 | 0/150 (začiatok kola), 0/160 (záverečná, nikdy neladená) |
| V4 kvalita vysvetlení (1–5) | 3,96, najnižšia 3 | 4,02, najnižšia 3 (23× 5, 117× 4, 20× 3) |
| vysvetlenie: medián slov, medián času | 41 slov, 5,6 s | 45 slov, 5,0 s |

Kvalita ≥ 4,3 nie je splnená. Čo sudcovia ešte hodnotia trojkou: pri chybe na papieri text uvedie hodnosť chybnej
matice bez „po oprave sa zmení“; pri preklepe v appke (papier správne) hovorí o inej časti úlohy; vektory zapísané
do riadkov pri dopĺňaní na bázu; (A|E) pri inverzii dostane vetu o pravej strane; nedokončený postup, z ktorého už
riadok 0 = c rozhoduje. Rozpor s enginom 0 platí aj na sade, na ktorej sa nič neladilo.

## Modely (kolo 4, 18. 11.)

Účet ponúka gpt-6.2-sol, 6.2-astra, 6.1-terra, 6.1-sol, 6-astra, 6-sol, 6-luna a rad 5.6 (ten sa nemeral). Úsilie:
sol a astra berú low–max, terra a luna aj none; minimal nikto. Volania ide z ChatGPT plánu, nie z platených API.

| krok | sada | nastavenie | výsledok | čas (medián) |
|---|---|---|---|---|
| čítanie GEM | Teove skutočné strany, 17 fotiek (12 strán s viacerými úlohami, 3 skutočné chyby) | sol medium high (predtým) | 0/29 falošný poplach, 3/3 nájdené, bunky 95,1 %, úloha 17/17 | 31,7 s |
| | | sol low high | rovnako, bunky 95,4 % | 22,5 s |
| | | sol low auto | rovnako, bunky 100 % | 22,9 s |
| | | **terra low high + cielené čítanie** | 0/29, 3/3 ako napísané, bunky 100 %, úloha 17/17, 0 zavádzajúcich | **12,0 s** (p90 14,6) |
| | syntetické listy s chybami, 20 | terra low high | 9/9 chybných buniek ako napísané, 0 „opravených“, 0/39 poplach, 9/10 nájdené (1 „nečitateľné“) | 9,0 s |
| posudok papiera | 16 prepisov (8 správnych inak formulovaných, 8 chybných) | sol medium / sol low / astra low / 6.1-sol low / terra low | 15/16 každý (jediná nezhoda: kongruencia cez zvyšky) | 6,1 / 5,4 / 6,7 / 5,2 / 4,3 s |
| | | luna low | 12/16, štyri chybné uznal s výhradou | 3,5 s |
| otázky textom | 8 otázok s nástrojmi (iná báza, REA, inverzia, definícia) | sol low / sol medium | sudca 2: 0 omylov, skóre 4,88 | 8 s (prvý text 6,2 s) |
| | | astra low / 6.1-sol low | 0 omylov, 4,71 / 4,75 | 8 / 10 s |
| | | terra low / terra none | 0 omylov, 4,38 / 4,50 | 7 / 10 s |
| | | luna low | 1 omyl, 4,50 | 5 s |
| celý tútor na skutočných stranách | 6 otázok s fotkou (REA na DML strane, definícia LK, 3 GEM na stranách s viacerými úlohami) | sol low / terra low + čítanie sol low | 12/12 vecne správne (pri REA 84/50 obe odhalili Bézoutove koeficienty zo zlého riadku) | 26 / 21 s |

Zvolené (`Nastavenie.z`): čítanie **gpt-6.1-terra low** (bez terry gpt-6.2-sol medium, overený v kole 3), detail high;
vysvetlenie, rozhovor a posudok **gpt-6.2-sol low**. Dve úrovne teda vyšli naopak, než sa čakalo: rukopis zvláda
rýchly model, slová a úsudok ostávajú na silnejšom (rozdiel v skóre 4,88 a 4,38). Strop času: odpoveď na fotku je
čítanie (~12 s) plus posudok alebo vysvetlenie (~5 s); odpoveď na text s nástrojom sú dve kolá modelu (~6–8 s), cieľ 5 s
nesplnený, menej sa dá len bez nástroja.

## Odchýlky od plánu

- Čítanie po maticiach (druhé čítanie výrezov) nie je postavené: s pravdou podľa nakreslených matíc mali husté listy
  0 zavádzajúcich viet, nebolo čo zlepšiť. Počítanie znakov ∼ v pokyne čítania bolo skúšané a vrátené (1/177 horšie).

- Kontext vysvetlenia sú zatiaľ údaje úlohy z modelu appky („Ako na to?“, správna odpoveď, spätná väzba appky),
  nie pribalené `kurz/*.md`: na vysvetlenie jedného kroku GEM stačia a nič sa nekopíruje. Pribalenie príde s chatom.
- Prepis má bunky ako reťazce a iné čítania v zozname `nejasne` (nie objekt na bunku): výstup je 3× kratší
  a čítanie 2,5× rýchlejšie, presnosť rovnaká.
- Pravdepodobná bunka (vyššie) je navyše: bez nej by sa pri nenapísaných úpravách nedala ukázať správna hodnota ani
  v Teovom vlastnom postupe.
- „Nerozumiem“ je zatiaľ druhé, pomalšie vysvetlenie toho istého verdiktu (najviac dvakrát), nie chat.
- Model čítania podľa porovnania na 35 a potom 177 fotkách: `gpt-6.2-sol` `medium` (59/59 chýb, 0 zlých miest,
  medián 13,9 s); `gpt-6.2-astra` `low` je o 1 s rýchlejší, ale raz označil zlé miesto; `gpt-6.2-sol` `low` raz
  „opravil“ zlú bunku; `gpt-6.1-sol` `low` našiel 14/17; `gpt-6-luna` mal falošné poplachy. Priebeh čítania
  („Prečítané matice: 2“) ide zo streamu prepisu.
- Konfigurácia podľa merania kola 3 (v kole 4 zmenená, pozri Modely): čítanie `gpt-6.2-sol`, úsilie `medium`, `detail: high`, 2048 px; vysvetlenie
  `gpt-6.2-sol`, úsilie `low`. Route prijme úsilie `low`–`max` (nie `none`, `minimal`); `json_schema` s fotkou v streame
  funguje; strana ≈ 4 000 tokenov vstupu a ≈ 500 výstupu, čítanie ≈ 15 s (p90 20 s), vysvetlenie ≈ 4,5 s; cache
  prefixu sa pri opakovaní neprejavila.
- Kde engine nevie, čo presne je zlé (len riadok, alebo správny postup a zlá odpoveď v appke), dostane model
  výslovne „nehádaj, čo študent spravil“: bez toho si sudcovia všimli vymyslené dôvody.
- Výrez fotky pri otázke je podľa obdĺžnika matice, ktorý vráti model, a mriežky buniek.

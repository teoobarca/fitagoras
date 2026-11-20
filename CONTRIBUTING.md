# Ako prispieť do FITagoras

## Chyba v obsahu alebo v appke

Najužitočnejší príspevok je **nahlásená chyba v obsahu** (zlé číslo, nepresné znenie, iné značenie než na prednáške):

- v appke pre Android pri každom kroku a úlohe vpravo hore ikona vlajky (Nahlásiť chybu); netreba účet;
- alebo [issue na GitHube](https://github.com/teoobarca/fitagoras/issues/new/choose) (šablóny „Chyba v obsahu“ a „Chyba v appke“): kde (predmet,
  lekcia, krok), čo je zle a podľa čoho (skriptá s číslom vety, prednáška s časom, zadanie testu).

Bezpečnostný problém nehlás verejne, postup je v [SECURITY.md](SECURITY.md).

## Kód a obsah

1. Zostav a otestuj (JDK 21, Android SDK, compileSdk 37):

   ```sh
   ./gradlew :shared:jvmTest :app:assembleDebug
   ```

   Bez `secrets.properties` (šablóna `secrets.properties.example`) sa appka zostaví, len sa neprehrajú klipy
   z prednášok a neposielajú sa štatistiky ani hlásenia chýb. Kľúč ku klipom má len autor appky.
2. Pozri si obrazovky, ktoré meníš, bez emulátora:
   `./gradlew :app:snap -Psnap.route=lesson:<id lekcie>:<krok> -Psnap.out=/tmp/zabery` (ďalšie trasy v
   [docs/SYSTEM.md](docs/SYSTEM.md), „Screenshots without the emulator“).
3. Počas práce púšťaj len testy toho, čo meníš (`./gradlew :shared:jvmTest --tests '*.PapersTest'`), celú sadu raz
   pred odovzdaním.
4. Pull request proti vetve `main` tohto repozitára (zo svojho forku). Do popisu napíš, čo sa mení a ako si to overil;
   pri zmene obrazovky pridaj záber.

**Pridať lekciu alebo tému:** obsah sú dáta v kóde. Predmet sa skladá z tém, nová látka je nový súbor témy
v `shared/src/commonMain/kotlin/dev/hamper/tabula/data/` (`dml/`, `la1/`) a jeden riadok v zozname tém predmetu.
Pred zmenou obsahu (lekcie, definície, vety, dôkazy, úlohy) si prečítaj, v tomto poradí:
[docs/ZASADY.md](docs/ZASADY.md) (zásady a postup tvorby lekcie), `kurz/<predmet>/README.md` (predmet a značenie),
balík kapitoly `kurz/<predmet>/kap<N>-*.md` a technické pravidlá [docs/CONTENT.md](docs/CONTENT.md). Nový predmet je
väčšia zmena: najprv issue. Kód: [docs/KMP.md](docs/KMP.md), štruktúra appky: [docs/VYVOJ.md](docs/VYVOJ.md),
ako sa appka overuje: [docs/SYSTEM.md](docs/SYSTEM.md).

Zásady, ktoré sa najľahšie porušia:

- Každé číslo, ktoré appka ukáže alebo skontroluje, počíta kód (`la/`, generátory), nie ručne napísaný text.
- Učí sa len to, čo sa naozaj prednášalo, v značení a názvoch kurzu.
- Keď niečo nesedí, oprav vrstvu, ktorá to pustila (pravidlo, komponent, generátor), nie jeden príklad, a rozšír
  celoplošnú kontrolu, ktorá to mala chytiť. Nie test na jednu opravu.

Commity sú malé a výstižné, správa hovorí, čo sa zmenilo a prečo.

## Vývoj vo verejnom repozitári

Repozitár je verejný a autor v ňom vyvíja priamo. Platí pre každého, aj pre AI agentov:

- **Na GitHub sa pushuje len `main`.** Pracovné vetvy ostávajú lokálne (autor ich zlučuje do `main` sám), cudzie
  príspevky prichádzajú ako pull request z forku.
- **Nič súkromné:** správy commitov, názvy vetiev, kód, dokumenty ani dáta nemenujú súkromné stroje, servery a ich
  rozhrania, cesty na disku, účty ani interné nástroje; žiadne mená ani citácie zo súkromných
  kanálov (Discord) a žiadne fotky cudzieho rukopisu. Autor a e-mail commitu sú GitHub noreply adresa.
- **Tajomstvá len mimo gitu:** kľúče ku klipom v `secrets.properties`, podpisový kľúč v `~/.fitest/` (alebo
  `-PfitestSigning=<súbor>`), nikdy v checkoute (`.gitignore` a `NoSecretsTrackedTest`).
- **Kontrola pred pushom** prejde správy, autorov a obsah každého commitu, ktorý ide von, a odmietne inú vetvu než
  `main`. Zapojenie raz v každom klone: `git config core.hooksPath scripts/verejne`. Ručne:
  `python3 scripts/verejne/kontrola.py rozsah origin/main..HEAD` (commity) alebo
  `python3 scripts/verejne/kontrola.py strom` (celý strom). Vzory sú v `scripts/verejne/*-vzory.txt`; tie isté
  kontrolujú testy `NoPrivateServerNameTest` a `NoSecretsTrackedTest`.

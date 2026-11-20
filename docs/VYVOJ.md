# Vývoj a vydanie FITagoras

Poznámky pre toho, kto appku stavia a vydáva. Pre používateľov je [README](../README.md).

## Ako je appka postavená

Kotlin a Compose Multiplatform: appka pre Android a webová verzia z jedného kódu ([docs/KMP.md](KMP.md)), funguje
offline okrem klipov a tútora. Cesty nižšie sú v `shared/src/commonMain/kotlin/dev/hamper/tabula/`, ak nie je
napísané inak.

- **Obsah ako dáta** v `data/`: `Lessons.kt` (lekcie), `Concepts.kt` (definície: české znenie, slovenské „prečo“,
  príklady a protipríklady, časti na zápis z pamäti), `Theorems.kt` (vety a dôkazy po riadkoch), `Papers.kt`
  (staré testy z fit-wiki overené proti fotkám a generované testy), `Templates.kt` (šablóny testových príkladov
  s novými číslami). Matematika v texte je medzi `§`.
- **Predmety a témy**: `data/Courses.kt` (LA1) a `data/Dml.kt` (DML) skladajú predmet z tém, nový obsah = jeden
  súbor témy (`data/dml/Induction.kt`, `data/la1/Chapter3.kt`), pravidlá v [docs/CONTENT.md](CONTENT.md).
  Rozvrh prednášok po paralelkách (`data/Schedule.kt`) generuje `scripts/schedule.py` z plánov predmetov mimo repa,
  termíny testov sú z KOS. Balík (`applicationId`) je `dev.hamper.fitest`, trieda aplikácie `FitestApp`.
- **Výpočty** v `la/`: presné zlomky a ℤₚ, GEM so zaznamenanými krokmi, úlohy s parametrom. Každé číslo, ktoré
  appka ukáže alebo skontroluje, sa počíta tam.
- **Klipy z prednášok**: `composeResources/files/clips.json` (úseky so skutočnými titulkami, generuje `scripts/clips.py` z
  `/api/captions` servera appky) sa streamujú z `fitagoras.hamper.dev/api/clips/…` s hosťovským kľúčom, ktorý sa pri
  builde vloží zo `secrets.properties` (mimo gitu, `clipsGuestToken=…`). Server má len hash kľúča a pustí s ním len
  krátke klipy. Výmena: nový kľúč do súboru, jeho hash na server, nové APK.
- **Čo appka posiela** (štatistiky, hlásenia, aktualizácie, Pytagoras): [docs/SUKROMIE.md](SUKROMIE.md).
- **Postup** v `state/Store.kt`, na Androide v DataStore (prežije aktualizácie), na webe v `localStorage`.
- Témy Hrejivý a Tmavý (`ui/Theme.kt`), vlastná sadzba vzorcov s TeX písmom MathJax (`math/`); widget, Live Update
  časovač nácviku a obraz v obraze sú v `app/` (len Android).

Build (JDK 21 v `JAVA_HOME`): `./gradlew :shared:jvmTest :app:assembleDebug`, webová verzia `scripts/web.sh`
(do `build/web/`, [docs/KMP.md](KMP.md), „The web“).
Release: pozri **Kľúče a vydanie** nižšie.

Zábery v README (`docs/zabery/tutor-*.png`, `klip-*.png`, svetlá a tmavá) sú dve karty po 410 px, na GitHube vedľa
seba, na mobile pod sebou. Každá je jedna obrazovka v rámiku telefónu na karte so štvorčekovým papierom, orezaná na
výšku 1925 px záberu (pod panelom tútora, medzi dvoma riadkami titulkov): `scripts/readme-karta.py <záber> <light|dark>
1925 <výstup>`. Tútor je `tutor-panel-gem` z `./gradlew :app:snap -Psnap.test=TutorShots.panelGem
-Psnap.theme=light,dark -Psnap.width=pixel -Psnap.out=<priečinok>`. Klip Robolectric neprehrá, ten je záber
z emulátora, ten istý ako na stránke appky (`klip-krok`), s piatym
argumentom `1412-1500` (vyblednutý riadok staršieho titulku pod prehrávačom).

Testovanie na emulátore: `scripts/walk.py lesson <id>` prejde celú lekciu skutočnými ťuknutiami, `scripts/ui.py`
ťuká podľa textu. Meno balíka sa nikde nepíše: skripty sa pýtajú `scripts/app_id.py` (z `build.gradle.kts`,
alebo `--apk <súbor>` z hotového APK), v shelli `PKG=$(python3 scripts/app_id.py)` a appka sa spúšťa cez
`am start -n $(adb shell cmd package resolve-activity --brief -c android.intent.category.LAUNCHER $PKG | tail -1)` (bez mena triedy v skripte). `RepoLintTest` zlyhá na doslovnom id.


## Kľúče a vydanie

- **Kľúč:** FITagoras sa podpisuje kľúčom `fitest-release.jks` (RSA 4096, CN=FITest, platný do 2059), heslá sú
  v `keystore.properties` vedľa neho. Certifikát SHA-256: `1c59fa01894806b0a5620a32aaf2f6ab685ea9a896701904ca2ad10c64f7bfea`.
  Každá aktualizácia musí byť podpísaná ním, inak sa nedá nainštalovať cez predošlú verziu.
- **Kde je:** u autora v `~/.fitest/` mimo akéhokoľvek repa, so zálohou mimo počítača; súbory majú práva 600.
  `keystore.properties` ukazuje na `~/.fitest/fitest-release.jks`: pri obnove zo zálohy skopíruj oba súbory do
  `~/.fitest/`. Bez kľúča zostavíš ladiace APK (`:app:assembleDebug`), nie release.
- **Nikdy v checkoute:** kľúč ani odkaz naň nepatrí do priečinka s repom. Git zmaže ignorovaný súbor pri prepnutí
  vetvy bez koša. `.gitignore` aj test `NoSecretsTrackedTest` to strážia.
- **Podpísať:** `./gradlew :app:assembleRelease` číta
  `~/.fitest/keystore.properties` (iný súbor: `-PfitestSigning=<cesta>`); bez neho release zlyhá. Overenie:
  `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`.
- **Vydať** (len autor): nahrať APK na server, ktorý ho podáva na [fitagoras.hamper.dev](https://fitagoras.hamper.dev),
  a porovnať SHA-256 verejného súboru s lokálnym.

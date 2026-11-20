# FITagoras: pokyny pre AI agentov a prispievateľov

Appka (Kotlin, Compose Multiplatform: Android a web), v ktorej sa študenti 1. ročníka FIT ČVUT učia BI-LA1
a BI-DML: lekcie, definície a vety zo skrípt, klipy z prednášok, príklady z testov s novými číslami, nácvik testu
na papieri a Pytagoras (AI pomocník cez účet ChatGPT používateľa). Čo appka je a ako sa zostaví: [README.md](README.md).

**Pred písaním alebo prerábkou obsahu** (lekcie, definície, vety, dôkazy, vzorové riešenia, úlohy) načítaj vetvu
dokumentov od koreňa po lekciu, v tomto poradí:

1. [docs/ZASADY.md](docs/ZASADY.md): zásady obsahu a postup tvorby lekcie (koreň);
2. `kurz/<predmet>/README.md`: predmet, prednášajúci, značenie, čo kto chce a zakazuje;
3. `kurz/<predmet>/kap<N>-*.md`: balík kapitoly (príbeh, postupy, typické úlohy, citácie s časom).

Technické pravidlá obsahu: [docs/CONTENT.md](docs/CONTENT.md). Overovanie, build a vydanie:
[docs/SYSTEM.md](docs/SYSTEM.md), [docs/VYVOJ.md](docs/VYVOJ.md). Kód: [docs/KMP.md](docs/KMP.md).

## Overovanie

Podrobnosti a presné príkazy: [docs/SYSTEM.md](docs/SYSTEM.md), „Verification“.

- Za každú opravu nepribúda test. Keď sa objaví nový druh chyby, rozšír jednu z mála celoplošných kontrol.
- Žiadne testy, ktoré zrkadlia kód, pripínajú text alebo overujú, že niečo odstránené už nie je.
- Pred odovzdaním si pozri vykreslené obrazovky toho, čo si zmenil (`./gradlew :app:snap -Psnap.route=…`).
- Počas práce púšťaj len dotknuté triedy testov (`./gradlew :shared:jvmTest --tests '*.YourTest'`), celú sadu raz
  pred odovzdaním (`./gradlew :shared:jvmTest :app:assembleDebug`), zábery a Robolectric pred vydaním alebo keď
  meníš ich oblasť.

## Verejný repozitár

Repozitár je verejný a vyvíja sa priamo v ňom. Pravidlá sú v [CONTRIBUTING.md](CONTRIBUTING.md), „Vývoj vo verejnom
repozitári“; najmä:

- Správy commitov, názvy vetiev, kód, dokumenty ani dáta nemenujú súkromné stroje, servery, cesty na disku, účty
  ani interné nástroje. Pred pushom to stráži `scripts/verejne/kontrola.py` (hook `pre-push`), v testoch
  `NoPrivateServerNameTest` a `NoSecretsTrackedTest`.
- Tajomstvá len v `secrets.properties` (mimo gitu), podpisový kľúč mimo checkoutu.
- Nepoužívaj kľúče platených API z iných projektov.

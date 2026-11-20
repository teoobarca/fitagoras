# LLM kontroly obsahu

Kontroly obsahu appky ako testy v markdowne: každý súbor tu je špecifikácia jednej kontroly odvodená zo zásad
v [docs/ZASADY.md](../docs/ZASADY.md). Codex CLI (prihlásené účtom ChatGPT, nie platené API) nimi občas prejde celú appku:
`scripts/llm-kontrola.sh` vyexportuje text všetkých lekcií (`:shared:exportContent`), voliteľne zábery krokov
(`:app:snap`), a pre každú kapitolu × skupinu kontrol pustí jeden beh. Výsledok je jeden report so zoznamom nálezov.
Report je **zoznam práce pre audit**, nie verdikt: nález pred opravou over v kóde a zdrojoch (ZASADY, postup tvorby,
krok 5). Ako a kedy to pustiť: [docs/SYSTEM.md](../docs/SYSTEM.md), „LLM content checks“.

## Hlavička kontroly

Každý súbor začína riadkami, ktoré číta skript:

```
Skupina: <postup | znenie | vysvetlenia | matematika | sadzba>
Vstupy: <súbory exportu kapitoly: lekcie, karty, generatory, pisomky; zabery = PNG krokov>
```

Kontroly jednej skupiny idú v jednom behu (rovnaké vstupy, jedno čítanie exportu). Nová kontrola = nový súbor
s hlavičkou; skript ju pridá sám.

## Formát nálezu (pre všetky kontroly)

Beh vracia JSON podľa schémy v skripte; každý nález má:

| pole | čo |
|---|---|
| `kontrola` | meno súboru kontroly bez `.md` (`preco`, `postup-kurzu`, …) |
| `miesto` | route z exportu (`lesson:dml-tc-rea:2`, `concept:dml-tc-gcd`, `gen:la1-coords ukážka 2`, `paper:slon-2024:1`) |
| `subor` | `súbor:riadok` z exportu, ak ho miesto má, inak prázdne |
| `citat` | doslovný úryvok z exportu (alebo opis zo záberu), ktorý chybu ukazuje; najviac ~300 znakov |
| `preco` | prečo je to chyba: ktorá zásada a čo hovorí balík kapitoly alebo skriptá (s citáciou balíka, napr. `[la1-P6-par2-klouda 42:49]`), a ako by to malo byť |
| `zavaznost` | `P1`, `P2` alebo `P3` (nižšie) |
| `istota` | `vysoka`, `stredna`, `nizka`: ako veľmi si nález overil v balíku a exporte |

## Závažnosť

- **P1:** študent sa naučí niečo nesprávne alebo stratí body na teste: zlá matematika, nesprávna „správna“ odpoveď,
  postup v rozpore s kurzom, chýbajúci predpoklad či kvantifikátor v znení, ktoré ide na papier, značenie, za ktoré
  prednášajúci strhávajú.
- **P2:** porušenie zásady, ktoré zhorší učenie: „Prečo?“ bez informácie, dve tvrdenia v jednej karte, odkaz bez
  novej informácie, veta bez motivácie, chýbajúci stav dôkazu, vzorec, ktorý sa na mobile láme.
- **P3:** drobnosť: štylistika, mierne nepresný popis zdroja, kozmetika.

## Spoločné pravidlá behu

- Len čítaj. Nič nemeň, nič neinštaluj, nespúšťaj build ani testy.
- Každý nález musí mať citát z exportu. Bez citátu nie je nález.
- Jeden nález = jedno miesto. Rovnaký vzor na viacerých miestach: každé miesto zvlášť (najviac 10 na vzor, potom
  jeden súhrnný nález s počtom a zoznamom ďalších miest v `preco`).
- Zásady 11 (sadzba) a 12 (dych): nálezy hlás, ale v `preco` nenavrhuj hromadnú prerábku (tá sa najprv dohodne).
- Čo balík kapitoly výslovne uvádza ako otvorenú úlohu alebo známu výnimku, nie je nález.

# Sadzba na mobile (zo záberov)

Skupina: sadzba
Vstupy: lekcie, zabery

Zásada 11. Vzorec sa na šírke 320 dp nesmie lámať; ak sa nezmestí, preštruktúrujeme ho (nie zmenšíme); súbežné veci
pod seba; dlhé formálne znenie najprv jadrom, celé znenie rozbaliteľné; žiadne dlhé vzorce vnútri odseku. Zábery sú
krokov lekcie na šírke 320 dp, svetlá téma, stav pred odpoveďou (spätná väzba a ďalšie časti vzorového riešenia nie
sú vidieť). Meno záberu je route kroku: `lesson_<id>_<krok>-light-320-1.0.png` = `lesson:<id>:<krok>`.

Nálezy hlás ako zoznam pre audit; hromadná prerábka sadzby sa najprv dohodne.

## Čo hľadať na záberoch

- Vzorec zlomený na konci riadku uprostred (napr. „$m\cdot$“ na jednom riadku a „$a+n\cdot b$“ na ďalšom, rovnosť
  rozdelená za „=“ v odseku).
- Dlhý vzorec vnútri odseku, ktorý by mal byť samostatne.
- Súbežné veci (tri rekurencie, viac rovností) v jednom riadku, ktorý sa láme.
- Vzorec alebo tabuľka zmenšená tak, že je ťažko čitateľná, alebo orezaná, prekrytá, idúca cez okraj karty.
- Text, ktorý je vidieť ako surový TeX (`\mathbb`, `§`, `$`).
- Na obrazovke vidno surový príkaz sadzby alebo rozsypaný vzorec: názov príkazu ako slovo (napr. „textstyle“,
  „frac“, „mathbf“, aj bez spätnej lomky), zátvorky `{` `}` alebo `^` `_` vypísané ako znaky, vzorec rozpadnutý na
  kúsky textu. Pozri aj lištu „Dokazuješ“ nad dôkazom a karty viet (1.3.13: lišta vypísala „textstyle“).
- Karta zo skrípt tak dlhá, že výklad kroku je pod ňou mimo prvej obrazovky (krok je vysoký; prvá obrazovka telefónu
  je ~ prvých 700 dp záberu).

## Príklad chyby

- ❌ REA (Věta 6.5) sploštená do riadkov textu, ktoré sa lámu; Věta 6.4: „$(\exists m,n\in\mathbb Z)(\gcd(a,b)=m\cdot$“
  na jednom riadku a „$a+n\cdot b)$“ na ďalšom.
- ✅ Úzka tabuľka $n\mid r\mid\alpha\mid\beta$ alebo každá postupnosť zvlášť rovnakým vzorom.

## Čo nie je chyba

- Zalomenie bežného textu medzi slovami a krátkymi vzorcami (vzorec ostal celý).
- Zalomenie zobrazeného vzorca na „=“ alebo „,“, ak je zarovnané a čitateľné (sadzba appky to robí zámerne).
- Prázdne miesto pod obsahom (záber je vysoký schválne).

## Výstup

`miesto` = route kroku, `citat` = opis, čo na zábere vidíš (ktorý vzorec, kde sa zlomil).

# Odkaz a vyskakovacie okno bez novej informácie

Skupina: vysvetlenia
Vstupy: lekcie, karty

Zásada 9. Odkaz na tvrdenie (a jeho okno) má zmysel, len ak pridá: celé znenie, dôkaz, kde sa tvrdenie ešte používa.
Všetky citované tvrdenia sú ťukateľné rovnako. V exporte je pri každom kroku riadok „Odkazy v kroku: „Lemma 6.1“ →
otvorí `theorem:…`“ alebo „→ bez karty, nedá sa ťuknúť“. Čo okno ukáže, nájdeš v `karty.md` pod tou route (aj
v inej kapitole).

## Čo hľadať

- Odkaz v kroku, ktorý otvorí kartu s presne tým istým textom, aký je v kroku už celý vidieť (napr. krok „Veta“
  s kartou a v jej texte odkaz na seba samu, alebo výklad s „Kartou zo skrípt“, ktorá je tá istá ako cieľ odkazu).
- Odkaz, ktorý otvorí **inú** kartu, než na akú text odkazuje (napr. „Lemma 6.1“ otvorí kartu „Věta 6.3“ alebo
  spoločnú kartu viacerých tvrdení).
- Tvrdenie citované číslom, ktoré sa nedá ťuknúť („bez karty“), hoci v inom kroku alebo inej kapitole rovnaký druh
  odkazu ťuknúť ide (nejednotnosť), alebo ide o tvrdenie, ktoré sa v kroku naozaj používa.
- Odkaz s neznámym číslom („neznáme číslo“): pravdepodobne zlé číslo tvrdenia.

## Príklady chyby

- ❌ Odkaz „Lemma 6.1“ otvorí okno s tým istým textom, aký je na karte, a „celá veta“ dole znova to isté.
- ❌ „Tvrzení 6.3“ v riešení lcm sa nedá ťuknúť.

## Čo nie je chyba

- Odkaz zo skráteného jadra na plné znenie s dôkazom.
- Odkaz karty na seba samu: appka ho na vlastnej obrazovke karty neoznačí a export ho preto nevypisuje.
- Odkaz na pojem, ktorý v kroku nie je vypísaný.

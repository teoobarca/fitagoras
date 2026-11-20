# Postup v rozpore s kurzom

Skupina: postup
Vstupy: lekcie, generatory, pisomky

Zásady 2 a 13. Každý postup (GEM, doplnenie na bázu, súradnice, hodnosť, inverzia, Frobenius, Euklid, REA, diofantické
rovnice, kongruencie, indukcia, dôkaz injektivity…) robíme krokmi kurzu, ako ich opisuje balík kapitoly, oddiel 4
„Postupy“. Každý krok je zdôvodnený: najprv dôvod slovami, potom veta zo skrípt s číslom.

## Čo hľadať

- Vzorové riešenie, výklad alebo generátor robí postup inak, než ho učí kurz (iné poradie, iná metóda, iná orientácia
  matice, riadky namiesto stĺpcov, dosadzovanie namiesto GEM, iný tvar tabuľky REA).
- Krok bez zdôvodnenia, alebo zdôvodnený zlou vetou či číslom (porovnaj s balíkom, oddiel 3 „Pojmy a tvrdenia“,
  a `kurz/<predmet>/cisla.tsv`, ak ho máš).
- Výklad učí skratku, ktorú kurz nevykladá, ako hlavnú cestu.
- Ručne napísaný výpočet (matica, reťaz GEM, Euklid) vo výklade, hoci tá istá zručnosť má generátor (zásada 13): v
  exporte je to vzorový príklad v lekcii, ktorého čísla nie sú z generátora tej istej kapitoly.
- Citácia prednášajúceho pri postupe, ktorú balík nepotvrdzuje (napr. „Klouda, 6. prednáška“ pri niečom, čo balík
  pripisuje inak alebo vôbec).

## Príklady chyby (Teove nálezy)

- ❌ Doplnenie na bázu vektormi do **riadkov** s citáciou „Klouda, 6. prednáška“. Kurz píše vektory do **stĺpcov**
  $(\mathbf u\ \mathbf v\mid \mathbf e_1\ \dots\ \mathbf e_4)$, GEM do HST, báza = pôvodné vektory na pozíciách hlavných
  stĺpcov [la1-P6-par2-klouda 42:49–44:57].
- ❌ Súradnice vzhľadom k báze riešené dosadzovaním (vyjadriť neznámu a dosadiť) namiesto GEM rozšírenej matice
  $(\mathbf x_1\cdots\mathbf x_d\mid\mathbf v)$ do HST (Postup 2.69).
- ❌ Ručne napísaná `pmatrix` reťaz GEM v lekcii vedľa generátora, ktorý tú istú úlohu počíta stĺpcami.

## Čo nie je chyba

- Spätné dopočítanie z HST alebo dotiahnutie na jednotkovú maticu (Příklad 2.71): je to súčasť GEM.
- Appka uzná správny výsledok inou cestou (grader); chyba je len to, čo appka **učí** alebo ukazuje ako vzor.
- Doplnenie na bázu, ktoré zatiaľ uzná len dvojice štandardných vektorov (otvorená úloha).
- Postup, ktorý balík neopisuje vôbec: to nie je nález proti kurzu, ale môžeš ho nahlásiť ako P3 „postup chýba
  v balíku“.

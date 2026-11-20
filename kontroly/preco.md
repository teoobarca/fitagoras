# „Prečo?“ bez informácie

Skupina: vysvetlenia
Vstupy: lekcie, generatory

Zásada 7. Po odpovedi appka ukáže prvú vetu vysvetlenia, zvyšok je za „Prečo?“. Čo je za „Prečo?“, musí niečo
vysvetliť: prečo je správna možnosť správna a prečo sú ostatné zlé. Ak je vysvetlenie jedna veta, „Prečo?“ sa
neukazuje. V exporte je pri každom vysvetlení „za „Prečo?“: …“ alebo „(bez „Prečo?“)“.

## Čo hľadať

- Text „za „Prečo?““ len opakuje prvú vetu, verdikt alebo otázku inými slovami, alebo je to len „Len c).“, „Áno.“,
  „Toto platí.“.
- Text „za „Prečo?““ nevysvetľuje, prečo sú ostatné možnosti zlé, hoci ich je viac a nie sú vysvetlené inde (pri
  zlej voľbe appka ukáže text „pri zlej x) najprv“, takže tie sa počítajú).
- Prvá veta je taká dlhá alebo obsahuje celé vysvetlenie, že „Prečo?“ už nič nepridá (a mal by byť bez „Prečo?“).
- Vysvetlenie pri zlej možnosti, ktoré nehovorí, čo konkrétne je na nej zlé (len „To nie je správne.“).
- Prvá veta, ktorá sa zlomí na skratke a zvyšok je preto nezmyselný fragment.

## Príklad chyby

- ❌ „Ktorá rovnosť je zmysluplná?“ → za „Prečo?“ len „Len c).“
- ✅ „Len c): báza má 4 prvky, súradnice sú preto štvorica. Súradnice sú koeficienty lineárnej kombinácie vektorov
  bázy, jeden pri každom z nich (Definice 2.68). Dvojica a trojica nemajú pri štyroch vektoroch bázy zmysel.“

## Čo nie je chyba

- Krátke vysvetlenie bez „Prečo?“ pri jednoduchej otázke.
- „Prečo?“, ktoré pridá výpočet, protipríklad, odkaz na vetu so zdôvodnením alebo citát prednášajúceho, ktorý niečo
  vysvetľuje.

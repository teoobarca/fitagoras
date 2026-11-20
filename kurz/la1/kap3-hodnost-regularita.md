# BI-LA1 kap. 3: Hodnost a regularita matice

Balík kapitoly: skriptá `chap-04.woo`, slajdy 3, kód rozvrhu S3; v appke téma `la1-k3` (`data/la1/Chapter3*.kt`).

Stav k 17. 11. 2026. Výťah zo skrípt, slajdov, titulkov všetkých paraleliek a proseminárov, testov, MARAST a Discordu; overené: všetky časy citácií existujú v titulkoch, citáty a tvrdenia k Teovým nálezom kontrolované ručne. Pred použitím tvrdenia v appke otvor citovaný zdroj.

Nad týmto súborom: [ZASADY](../../docs/ZASADY.md) → [predmet](README.md). Citácie a surové zdroje: [kurz/README.md](../README.md). Čísla tvrdení: [cisla.tsv](cisla.tsv).

## 1. Rozsah a zdroje

Stav lokálnych podkladov k 17. 11. 2026. Výroky o vyučujúcich sa vzťahujú na dodané nahrávky BI-LA1.21, nie automaticky na aktuálne pravidlá každého termínu.
Číslovanie je výhradne z `cisla/la1-kap3.tsv`; číslo súboru `chap-04.woo` neznamená kapitolu 4.

Skriptá pokrývajú tieto tematické celky:

- Hodnosť a jej výpočet: Definícia 3.1 až Príklad 3.8, `chap-04.woo:47–324`.
- Aplikácie hodnosti: Poznámky 3.9–3.10 a Varovanie 3.11, `chap-04.woo:325–443`.
- Transpozícia: Veta 3.12 až Poznámka 3.14, `chap-04.woo:444–556`.
- Násobenie a lineárne kombinácie: Veta 3.15 až Veta 3.19, `chap-04.woo:557–820`.
- Jednotková, inverzná a regulárna matica: Definícia 3.20 až Veta 3.28, `chap-04.woo:821–1066`.
- Maticová interpretácia GEM: Poznámky 3.29–3.31, Tvrdenie 3.32 až Poznámka 3.34, `chap-04.woo:1067–1379`.
- Kritériá regularity a algoritmus inverzie: Veta 3.35 až Príklad 3.38, `chap-04.woo:1380–1581`.
- Ďalšie vlastnosti: Veta 3.39 až Veta 3.42, `chap-04.woo:1582–1678`.
- Zaujímavosti: Tvrdenie 3.43 o blokovom násobení a nasledujúce algoritmy, `chap-04.woo:1679` a ďalej.

Rozsahy nahrávok relevantné pre kapitolu:

| Paralelka | Nahrávka a úsek | Čo obsahuje |
|---|---|---|
| 1, Kleprlík | la1-P6-par1-kleprlik 1:03:20–1:34:26 | Prechod z kap. 2, ekvivalencia GEM, hodnosť, transpozícia; záver iba avizuje inverziu. |
| 1, Kleprlík | la1-P7-par1-kleprlik 11:39–1:37:23 | Opakovanie hodnosti, násobenie, regularita, dôkazy, inverzia, hra; dodatok k zdôvodňovaniu 1:39:19–1:40:05. |
| 1, Kleprlík | la1-P8-par1-kleprlik, začiatok skontrolovaný po 27:04 | O 10:19 už začína Frobeniovu vetu; kap. 3 samostatne nedokončuje. |
| 2, Klouda | la1-P6-par2-klouda 59:25–1:36:48 | Motivácia invariantom obalu, hodnosť, GEM, transpozícia a hodnosť súčinu. |
| 2, Klouda | la1-P7-par2-klouda 07:43–1:19:13 | Regularita, inverzie, elementárne matice, kritériá, algoritmus; od 1:20:19 prechádza ku kap. 4. |
| 3, Krásenský | la1-P6-par3-krasensky | Dokončuje kap. 2; hodnosť ešte samostatne nezavádza. Záver kap. 2 je 1:33:34–1:36:06. |
| 3, Krásenský | la1-P7-par3-krasensky 10:36–1:31:19 | GEM, hodnosť, príklady, transpozícia, násobenie a definícia inverzie; úvod 08:20–10:36 rieši hranice druhého testu. |
| 3, Krásenský | la1-P8-par3-krasensky 05:19–1:24:09 | Podstatné dokončenie kapitoly vrátane dôkazov a inverzie; od 1:24:17 ďalšia prezentácia. |
| Proseminár | la1-prosem2223-03 00:30–1:21:40 | Báza cez riadky, hodnosť, parameter v $\mathbb Z_5$, tri pohľady na inverziu, maticová rovnica. |

INDEX označuje Kloudovu P7 ako `partial`; dodaný text napriek tomu siaha po 1:35:05 a obsahuje aj prechod ku kap. 4. Dôvod označenia zdroj nemá.
Proseminár má titulky, meno prednášajúceho v metadátach ani v prečítanom úvode uvedené nie je.
Jadro po Vetu 3.42 je v uvedených paralelkách pokryté; neprebratie žiadnej jeho časti vo všetkých paralelkách sa nepotvrdilo.
Výklad Tvrdenia 3.43 a algoritmov násobenia zo sekcie Zajímavosti sa v určených úsekoch nepotvrdil; slajdy namiesto nich končia hrou. [bi-la1-prezentace-03.tex:460]

## 2. Príbeh kapitoly, ako ho rozprávajú prednášky

1. **GEM zachováva viac než riešenia sústavy.** Klouda najskôr mení generátory obalu a necháva študentov rozhodovať, či sa obal zmenil; tri dovolené zmeny sú práve kroky GEM. Výsledkom je Pozorovanie 3.5, zachovanie obalu riadkov. [la1-P6-par2-klouda 1:00:25–1:07:38]

2. **Z obalu urobíme číselnú charakteristiku matice.** Definícia 3.1 berie dimenziu obalu riadkov; transpozície iba zabezpečujú, aby riadky boli vektormi z $T^n$. Kleprlík zavádza pojem priamo, Krásenský ho ilustruje konkrétnou maticou $G$. [la1-P6-par1-kleprlik 1:07:10–1:12:27; la1-P7-par3-krasensky 18:25–22:32]

3. **Definícia ešte nie je algoritmus.** Vety 3.6 a 3.7 spoločne dovolia GEM do HST a spočítanie nenulových riadkov. Krásenský tú istú hodnosť najskôr určí z definície a potom druhýkrát novou teóriou. [la1-P7-par3-krasensky 29:25–48:33]

4. **Hodnosť je nástroj na dimenziu, členstvo a rovnosť obalov.** Vektory pri tomto postupe dávame do riadkov a porovnávame hodnosti vhodných matíc. Prednášky zároveň odlišujú tento postup od riešenia sústavy s vektormi v stĺpcoch. [la1-P6-par1-kleprlik 1:24:21–1:26:51; la1-P7-par3-krasensky 49:44–52:14]

5. **Riadky a stĺpce majú rovnakú dimenziu obalu.** Veta 3.12 hovorí $h(A)=h(A^T)$, nie rovnosť samotných obalov. Preto $h(A)\leq\min\{m,n\}$ a pri samotnej hodnosti možno použiť aj stĺpcové úpravy. [la1-P6-par1-kleprlik 1:29:51–1:33:14; la1-P7-par3-krasensky 55:10–1:02:14]

6. **Maticové násobenie vyrába lineárne kombinácie.** Riadok zľava kombinuje riadky, stĺpec sprava stĺpce; Vety 3.15 a 3.18 vysvetlia aj $h(AB)\leq\min\{h(A),h(B)\}$. Klouda ukazuje obe orientácie, Kleprlík a Krásenský druhý odhad získajú transpozíciou. [la1-P6-par2-klouda 1:28:19–1:36:48; la1-P7-par1-kleprlik 28:08–29:56]

7. **Jednotková matica otvára otázku inverzie.** Definícia 3.23 požaduje obojstranné násobenie na $E$; regularita znamená existenciu takej matice. Až potom má zmysel dokazovať jednoznačnosť a pravidlá pre inverziu súčinu a transpozície. [la1-P7-par1-kleprlik 36:34–50:55]

8. **Každý krok GEM možno zapísať ako násobenie zľava.** Elementárne matice vzniknú rovnakou operáciou na $E$ a sú regulárne, lebo operácia má spätný krok. Tvrdenie 3.32 spojí všetky operácie do jediného súčinu. [la1-P7-par2-klouda 34:19–44:30; la1-P8-par3-krasensky 40:25–43:10]

9. **Regularitu konečne vieme testovať bez hádania inverzie.** Veta 3.35 spojí existenciu inverzie, plnú hodnosť a $A\sim E$; z nej vznikne Postup 3.37, spoločná eliminácia $(A\mid E)$. [la1-P7-par2-klouda 49:28–1:05:59]

10. **Teória sa uzatvorí oboma smermi.** Násobenie regulárnou maticou realizuje GEM a zachováva hodnosť; pri štvorcových maticiach už na overenie inverzie stačí jedna strana. Krásenský poslednú vetu presunie pred opačný smer maticovej interpretácie, aby ňou dovysvetlil algoritmus. [la1-P7-par2-klouda 1:13:16–1:18:43; la1-P8-par3-krasensky 1:04:20–1:22:09]

## 3. Pojmy a tvrdenia

Dôležitosť nižšie znamená doložené použitie, nie odhad percenta výskytu na budúcom teste.
„Skriptá: áno“ znamená prítomnosť dôkazu alebo výslovného odvodenia; definície dôkaz nemajú.

| Číslo | Druh | Názov | Jadro jednou vetou | Dôkaz: skriptá; prednáška | Dôležitosť pre test |
|---|---|---|---|---|---|
| 3.1 | Definice | Hodnost matice | $h(A)=\dim\langle(A_{1:})^T,\ldots,(A_{m:})^T\rangle$. [chap-04.woo:53] | Nie, definícia; Kleprlík 1:07:10–1:10:40 v P6. | Priamo definovať: [testy-la1-zkouska.md:9]. |
| 3.4 | Definice | Značení převoditelnosti GEMem | $A\sim B$ znamená konečnú postupnosť riadkových G1–G3 pri rovnakom rozmere. [chap-04.woo:104] | Nie, definícia; [la1-P7-par3-krasensky 10:36–18:25]. | Nevyhnutný jazyk dôkazov a algoritmu 3.37. |
| 3.5 | Pozorování | O GEM a lineárním obalu řádků | GEM zachováva celý obal riadkov. [chap-04.woo:126] | Áno, G2 podrobne, ostatné analogicky; [la1-P7-par3-krasensky 29:54–35:35]. | Zdôvodnenie bázy cez riadky: [marast-problems/lecture-258.md:86]. |
| 3.6 | Věta | O vlivu GEM na hodnost matice | $A\sim B\Rightarrow h(A)=h(B)$. [chap-04.woo:167] | Áno, odvodenie pred vetou; [la1-P6-par1-kleprlik 1:13:33–1:18:21]. | Základ každého výpočtu hodnosti. [testy-la1-zkouska.md:14] |
| 3.7 | Věta | O hodnosti matice v HST | Hodnosť matice v HST je počet jej nenulových riadkov. [chap-04.woo:183] | Áno; Krásenský vysvetľuje LN nenulových riadkov [la1-P7-par3-krasensky 40:53–46:22]. | Výpočet i zdôvodnenie: [la1-marast-13741]. |
| 3.12 | Věta | O hodnosti transpozice matice | Pre každú $A$ platí $h(A)=h(A^T)$. [chap-04.woo:450] | Áno; Krásenský iba komentuje princíp, dôkaz nechce [la1-P7-par3-krasensky 57:51–1:00:51]. | Používať pri odhadoch a zmene orientácie. [testy-la1-zkouska.md:21] |
| 3.13 | Důsledek | O odhadu hodnosti matice $m\times n$ | $h(A)\leq\min\{m,n\}$. [chap-04.woo:539] | Áno, odvodenie pred dôsledkom; [la1-P7-par3-krasensky 1:01:28–1:02:14]. | Horné zovretie v dôkaze regularity. [testy-la1-zkouska.md:9] |
| 3.15 | Věta | O vztahu lineární kombinace a násobení vektoru s maticí | $aA$ kombinuje riadky, $Ax$ stĺpce. [chap-04.woo:624] | Áno; [la1-P7-par1-kleprlik 14:27–17:22]. | Konštrukcia násobiacej matice: [la1-skripta-chap04-778]. |
| 3.16 | Pozorování | O řešení SLR a lineární kombinaci sloupců | $Ax=b$ hľadá koeficienty kombinácie stĺpcov $A$ rovnej $b$. [chap-04.woo:672] | Áno, dosadenie z 3.15; [la1-P6-par2-klouda 1:30:21–1:30:51]. | Maticové rovnice a inverzia po stĺpcoch. [la1-marast-13386] |
| 3.18 | Věta | O násobení matic a lineárních kombinacích | $(AB)_{i:}=A_{i:}B$, $(AB)_{:j}=AB_{:j}$. [chap-04.woo:723] | Áno; [la1-P6-par2-klouda 1:28:19–1:30:51]. | Vysvetlenie GEM a inverzie; [la1-skripta-chap04-1270]. |
| 3.19 | Věta | O odhadu hodnosti součinu matic | $h(AB)\leq\min\{h(A),h(B)\}$. [chap-04.woo:791] | Áno; [la1-P7-par1-kleprlik 23:09–29:56]. | Použiť pri dôkaze regulárna $\Rightarrow h(A)=n$. [testy-la1-zkouska.md:9] |
| 3.20, 3.22 | Definice; Pozorování | Jednotková/diagonální matice; O vlivu jednotkové matice při maticovém násobení | $E$ má diagonálu 1 a inde 0; pri vhodných rozmeroch $EA=A=AE$. [chap-04.woo:827; chap-04.woo:875] | Definícia nie, neutralita áno; [la1-P7-par1-kleprlik 33:36–36:34]. | Nutné v definícii inverzie. [testy-la1-zkouska.md:9] |
| 3.23 | Definice | Inverzní a regulární matice | Pre $A,B\in T^{n,n}$ je $B=A^{-1}$, ak $AB=BA=E$; regulárna $A$ má inverziu. [chap-04.woo:914] | Nie, definícia; [la1-P7-par2-klouda 20:27–21:27]. | Priamo definovať. [testy-la1-zkouska.md:9; testy-la1-zkouska.md:21] |
| 3.25 | Věta | O jednoznačnosti maticové inverze | Ak inverzia existuje, je jediná. [chap-04.woo:977] | Áno; [la1-P7-par1-kleprlik 44:19–47:20]. | Priamo dokázať. [testy-la1-zkouska.md:9] |
| 3.26 | Pozorování | O regularitě inverzní matice | Inverzia je regulárna a $(A^{-1})^{-1}=A$. [chap-04.woo:1001] | Áno, priamo z definície; [la1-P7-par2-klouda 33:04–33:28]. | Potrebné v dôkaze 3.35, algebra inverzií. |
| 3.27 | Věta | O regularitě součinu matic | Pre regulárne $A,B\in T^{n,n}$ je $(AB)^{-1}=B^{-1}A^{-1}$. [chap-04.woo:1016] | Áno; [la1-P7-par1-kleprlik 47:27–50:16]. | Výslovne skúškový dôkaz; úprava výrazu [testy-la1-zkouska.md:15]. |
| 3.28 | Věta | O regularitě transponované matice | $(A^T)^{-1}=(A^{-1})^T$. [chap-04.woo:1043] | Áno; [la1-P7-par2-klouda 31:20–32:12]. | Úprava výrazu s transpozíciou. [testy-la1-zkouska.md:15] |
| 3.32 | Tvrzení | O reprezentaci GEM regulární maticí | $A\sim B\Rightarrow B=PA$ pre regulárnu $P\in T^{m,m}$. [chap-04.woo:1254] | Áno; [la1-P8-par3-krasensky 40:25–43:10]. | Zdôvodňuje algoritmus; konštrukcia [la1-skripta-chap04-1270]. |
| 3.35 | Věta | O vztahu regularity a hodnosti matice | Pre $A\in T^{n,n}$: regularita $\Leftrightarrow h(A)=n\Leftrightarrow A\sim E$. [chap-04.woo:1386] | Áno; [la1-P7-par2-klouda 49:28–57:57]. | Formulácia aj dôkaz priamo na skúške. [testy-la1-zkouska.md:9] |
| 3.36 | Poznámka | O dalších ekvivalentních podmínkách pro regularitu | Regularite je ekvivalentná LN súboru riadkov aj súboru stĺpcov. [chap-04.woo:1469] | Áno, odvodenie; [la1-P7-par2-klouda 59:07–59:46]. | Prepojenie s bázou, konceptuálne zdôvodnenie 3.35. |
| 3.37 | Postup | Ověření regularity a nalezení inverzní matice | Riadkové úpravy $(A\mid E)\sim(E\mid A^{-1})$. [chap-04.woo:1494] | Áno, zdôvodnenie v texte; [la1-P8-par3-krasensky 1:00:01–1:03:24]. | Priamy výpočet. [testy-la1-zkouska.md:14; la1-marast-13743] |
| 3.39 | Věta | O vztahu maticového násobení reg. maticí a GEM | $B=PA$, $P$ regulárna, implikuje $A\sim B$. [chap-04.woo:1590] | Áno; [la1-P8-par3-krasensky 1:10:10–1:18:31]. | Teoretické vysvetlenie násobenia; samostatné ostré zadanie v prepísaných skúškach zdroj nemá. |
| 3.40 | Důsledek | O vlivu násobení regulární maticí na hodnost | $h(PA)=h(A)=h(AQ)$ pre regulárne $P,Q$ vhodných rozmerov. [chap-04.woo:1609] | Áno; [la1-P8-par3-krasensky 1:19:13–1:22:03]. | Priamo formulovať pre druhý regulárny činiteľ. [testy-la1-zkouska.md:21] |
| 3.42 | Věta | O postačitelnosti inverze z jedné strany | Pre štvorcové $A,B$ už $AB=E$ alebo $BA=E$ dáva $B=A^{-1}$. [chap-04.woo:1656] | Áno; [la1-P7-par2-klouda 1:14:20–1:18:43]. | Krátky skúškový dôkaz podľa Kleprlíka [la1-P7-par1-kleprlik 1:33:51–1:34:18]. |

## 4. Postupy (ako sa to počíta na prednáške, proseminári a v skriptách)

### Hodnosť pomocou GEM

1. Napísať maticu $A$, bez pridávania pravej strany, ak zadanie žiadnu neobsahuje.
2. Riadkovými G1–G3 dostať $B$ do HST; pri násobení riadku vyžadovať nenulový koeficient.
3. Spočítať $k$ nenulových riadkov, ekvivalentne hlavných stĺpcov HST.
4. Uzavrieť $h(A)=h(B)=k$: prvá rovnosť je Veta 3.6, druhá Veta 3.7.

Toto je algoritmus, nie Definícia 3.1. [Věta 3.6, chap-04.woo:167; Věta 3.7, chap-04.woo:183; la1-P6-par2-klouda 1:17:10–1:17:16]
Model záveru zo skrípt: HST má tri nenulové riadky, teda hodnosť 3; GEM hodnosť nezmenil, preto aj pôvodná matica má hodnosť 3. [Příklad 3.8, chap-04.woo:308–321]
Proseminár vysvetľuje obe časti cez zachovanie obalu a bázu tvorenú nenulovými riadkami. [la1-prosem2223-03 18:00–21:35]

### Parameter: nestratiť prípady delením

1. Eliminovať najskôr bez delenia výrazom obsahujúcim parameter.
2. Ak treba deliť $f(a)$, oddeliť $f(a)=0$ a $f(a)\neq0$.
3. V každej vetve znovu skontrolovať HST a počet nenulových riadkov.
4. Nad $\mathbb Z_p$ počítať v danom telese, nie v reálnych číslach.

Proseminár vedome nedelí $a+1$ bez odlíšenia $a=4$ v $\mathbb Z_5$; výsledok príkladu je hodnosť 2 pre $a=4$ a 3 inak. [la1-prosem2223-03 27:18–36:09; marast-problems/lecture-258.md:29–40]
Reálny parameter môže zmeniť aj polohu hlavných stĺpcov: v #13759 je pri $\alpha=0$ ešte nutné odčítať zhodné riadky; konečný výsledok je 2, inak 4. [marast-problems/lecture-258.md:238–272]

### Riadky alebo stĺpce: čo presne hľadáme

- **Dimenzia a ľubovoľná báza obalu:** vektory dať do riadkov, GEM; nenulové výsledné riadky transponovať na vektory. Dôvod: Pozorovanie 3.5 zachováva obal a Veta 3.7 dáva LN v HST. [la1-prosem2223-03 03:00–04:54; marast-problems/lecture-258.md:86–101]
- **Báza vybraná z pôvodného súboru:** vektory dať do stĺpcov a po GEM vybrať pôvodné vektory na pozíciách hlavných stĺpcov. Nevyberať upravené stĺpce ako bázu pôvodného obalu. [la1-prosem2223-03 00:52–01:50; 14:56–15:28]
- **Členstvo $y\in\langle x_1,\ldots,x_m\rangle$ cez koeficienty:** riešiť $\sum\alpha_i x_i=y$, teda stĺpce $x_i$ a pravá strana $y$. [Poznámka 3.10, chap-04.woo:401–412]
- **To isté členstvo cez hodnosť:** porovnať hodnosť matice s riadkami $x_i$ a matice s navyše pridaným riadkom $y$; netreba riešiť neexistujúcu pravú stranu. [Poznámka 3.10, chap-04.woo:414–417]
- **Rovnosť obalov:** overiť $h(X)=h(Y)=h\binom{X}{Y}$ pre matice s generátormi v riadkoch. Samotné $h(X)=h(Y)$ nestačí. [bi-la1-prezentace-03.tex:69; marast-problems/lecture-258.md:141–176]

Krásenský varuje pred miešaním týchto argumentácií, nie pred riadkami všeobecne. [la1-P7-par3-krasensky 51:14–52:14]

### Transpozícia a stĺpcové úpravy

Na samotnú hodnosť možno použiť aj stĺpcové analógie G1–G3: transponovať, vykonať riadkovú úpravu a transponovať späť.
Zdôvodnenie je Veta 3.12 spolu s Vetou 3.6. [Poznámka 3.14, chap-04.woo:547–554; la1-P6-par1-kleprlik 1:30:57–1:31:43]
Toto oprávnenie neprenášať do algoritmu $(A\mid E)$: tam Postup 3.37 výslovne používa rovnaké **riadkové** úpravy na oboch blokoch. [chap-04.woo:1494–1526]

### Inverzia cez $(A\mid E)$

1. Overiť, že $A\in T^{n,n}$; vedľa napísať jednotkovú maticu rovnakého rádu.
2. Zapísať $(A\mid E)$ a každú riadkovú operáciu vykonať na celom riadku oboch blokov.
3. Ľavý blok previesť do HST. Ak má menej než $n$ nenulových riadkov, napísať $h(A)<n$, teda $A$ je singulárna a inverzia neexistuje.
4. Inak pokračovať: normalizovať prvé nenulové prvky riadkov na 1 a eliminovať nad nimi, až je ľavý blok presne $E$.
5. Z výsledku $(E\mid B)$ odčítať $A^{-1}=B$.

Dôvod: všetky operácie reprezentuje regulárna $P$, takže $(A\mid E)\sim(PA\mid PE)=(PA\mid P)$; pri $PA=E$ je $P=A^{-1}$. [Tvrzení 3.32, chap-04.woo:1254; Věta 3.35, chap-04.woo:1386; Postup 3.37, chap-04.woo:1494]
Nulový riadok sa posudzuje v **ľavom bloku**, nie až v celej rozšírenej matici. [chap-04.woo:1518; la1-P8-par3-krasensky 1:00:14–1:00:43]
Proseminár tento tretí, spoločný postup odporúča do písomky. Kleprlík umožňuje samotný štandardný výpočet bez ďalšieho komentára; to nenahrádza dôkaz, ak zadanie žiada vysvetliť, prečo algoritmus funguje. [la1-prosem2223-03 1:11:58–1:12:28; la1-P7-par1-kleprlik 1:27:55–1:28:06]

Kontrolný príklad z proseminára: $A=\begin{pmatrix}1&0&1\\0&1&0\\1&1&2\end{pmatrix}$, $A^{-1}=\begin{pmatrix}2&1&-1\\0&1&0\\-1&-1&1\end{pmatrix}$. [marast-problems/lecture-258.md:43–57; la1-prosem2223-03 1:06:52–1:11:58]
Kontrolný príklad zo skrípt nad $\mathbb Z_7$: $A=\begin{pmatrix}1&3&2\\2&3&1\\3&4&5\end{pmatrix}$, $A^{-1}=\begin{pmatrix}2&0&2\\0&3&5\\3&6&2\end{pmatrix}$. [Příklad 3.38, chap-04.woo:1529–1579]

### Ďalšie dva pohľady na tú istú inverziu

- **Po stĺpcoch:** z $AB=E$ vznikne $Ab_j=e_j$; všetky sústavy majú rovnakú ľavú maticu, preto ich eliminovať spolu. Vektory riešení zapísať do stĺpcov $B$, nie do riadkov. [la1-prosem2223-03 40:50–50:09; la1-P8-par3-krasensky 12:09–16:39]
- **Súčinom elementárnych matíc:** ak $P_k\cdots P_1A=E$, potom $A^{-1}=P_k\cdots P_1$; najskôr aplikovaná matica je najbližšie k $A$. Proseminár takto algoritmus odvodzuje, až potom zavedie skratku $(A\mid E)$. [la1-prosem2223-03 56:43–1:04:23]

Podmienku jednej strany treba zdôvodniť Vetou 3.42 alebo už známou regularitou; samotná Definícia 3.23 má dve rovnosti. [chap-04.woo:919–928; chap-04.woo:1656–1674]

### GEM ako násobenie maticami

G1: rovnaká výmena riadkov na $E$ dá $P(i,j)$, ktorá je sama sebe inverzná.
G2: na diagonále $E$ nahradiť $i$-tu jednotku číslom $\alpha\neq0$; spätná matica má $\alpha^{-1}$.
G3: pre (G3) ř$j$ + $\alpha\cdot$ř$i$ pridať do $E$ prvok $\alpha$ na pozíciu $(j,i)$; spätná matica má $-\alpha$.
Všetky pôsobia **zľava**. [Poznámky 3.29–3.31, chap-04.woo:1127–1223; bi-la1-prezentace-03.tex:349–388]
Pre viac krokov písať $B=P_k\cdots P_1A$; súčin je regulárny podľa Vety 3.27. [Tvrzení 3.32, chap-04.woo:1254–1266]
Stĺpcové operácie zodpovedajú násobeniu sprava, ale s týmto postupom ich nemiešať. [Poznámka 3.34, chap-04.woo:1365; la1-P7-par2-klouda 43:30]

### Krátke dôkazy, ktoré treba vedieť napísať

- **Jednoznačnosť:** pre dve inverzie $B,C$ napísať $B=BE=B(AC)=(BA)C=EC=C$. Použité sú neutralita, asociativita a rovnosti z definície. [Věta 3.25, chap-04.woo:984–995]
- **Inverzia súčinu:** kandidát je $B^{-1}A^{-1}$; vypočítať $(AB)(B^{-1}A^{-1})=E$ aj opačný súčin. Poradie činiteľov neprehadzovať. [Věta 3.27, chap-04.woo:1025–1037]
- **Regulárna $\Rightarrow$ plná hodnosť:** $n=h(E)=h(AA^{-1})\leq h(A)\leq n$, preto $h(A)=n$. Prvá nerovnosť je 3.19, druhá 3.13. [Věta 3.35, chap-04.woo:1413–1420]
- **Plná hodnosť $\Rightarrow A\sim E$:** HST štvorcovej matice má $n$ hlavných stĺpcov, prvé nenulové prvky na diagonále; normalizáciou a elimináciou nahor vznikne $E$. [chap-04.woo:1423–1451]
- **$A\sim E\Rightarrow$ regularita:** podľa 3.32 existuje regulárna $P$ s $PA=E$; potom $A=P^{-1}$ a je regulárna. Nepoužiť bez zdôvodnenia ešte nedokázané pravidlo o jednostrannej inverzii. [chap-04.woo:1454–1462]
- **Krásenského alternatíva:** obrátiť úpravy na $E\sim A$; z 3.32 dostať $A=PE=P$ s regulárnou $P$. [la1-P8-par3-krasensky 53:38–55:09]
- **Jedna strana stačí:** z $AB=E$ najprv zovrieť $h(A)$ na $n$ a použiť 3.35; až teraz použiť existujúcu $A^{-1}$ a odvodiť $B=A^{-1}AB=A^{-1}$. [Věta 3.42, chap-04.woo:1663–1674]

## 5. Značenie a terminológia

- $T^{m,n}$ je typ matice, $T^n$ priestor vektorov; prechod z riadku na vektor z $T^n$ sa v definícii zapisuje transpozíciou. [Definice 3.1; Poznámka 3.2, chap-04.woo:53–81]
- $A_{i:}$ je $i$-ty riadok, $A_{:j}$ je $j$-ty stĺpec; $(AB)_{i:}=A_{i:}B$ a $(AB)_{:j}=AB_{:j}$. [Věta 3.18, chap-04.woo:723–752]
- Kurz píše $h(A)$, český pojem je „hodnost matice“; Klouda dopĺňa anglické „rank“. Označenie $\operatorname{rank}(A)$ nie je v citovanej definícii zavedené. [chap-04.woo:58–62; la1-P6-par2-klouda 59:55]
- $A\sim B$ je „převoditelnost GEMem“, nie podobnosť matíc zo spektrálnej teórie. Formálne obe matice majú rovnaký rozmer; vymazanie nulového riadku nie je G1–G3. [Definice 3.4, chap-04.woo:104–119]
- GEM je „Gaussova eliminační metoda“, HST „horní stupňovitý tvar“; pokračovanie s jednotkovými prvými nenulovými prvkami a nulami nad nimi je „Gaussova–Jordanova eliminace“. [chap-04.woo:101–109; chap-04.woo:1466]
- $E$ je „jednotková matice“, $A^{-1}$ „inverzní matice“, „regulární“ znamená existenciu inverzie, „singulární“ jej neexistenciu u štvorcovej matice. [Definice 3.20, chap-04.woo:827; Definice 3.23, chap-04.woo:914–938]
- LN/LZ sa vzťahuje na **súbor** vektorov; formuláciu „maximální počet LN řádků“ skriptá ako definíciu odmietajú. [Varování 3.11, chap-04.woo:435–436]
- V citovaných definíciách prednášajúci používajú rovnaké základné symboly; odlišný systém značenia sa nepotvrdil. Klouda pri slovnom výklade občas transpozície skracuje, formálny vzorec treba prebrať zo skrípt. [la1-P6-par2-klouda 1:09:28–1:10:52]
- Iné všeobecné zákazy symbolov zdroj nemá; nepripisovať kurzu zákaz skratky len preto, že sa v týchto úsekoch nevyskytla.

## 6. Čo kto zdôrazňuje, chce a zakazuje

Citáty sú krátke doslovné úseky dodaných titulkov alebo skrípt; chyby prepisu sa neopravujú vo vnútri citátu.

| Kto | Čo povedal | Citácia a význam |
|---|---|---|
| Skriptá | „Nikdy se nepokoušejte“ | Varovanie pred definovaním hodnosti algoritmom. [Varování 3.11, chap-04.woo:430] |
| Skriptá | „vyřeším matici“ | Výslovne odmietnuté slovné spojenie; riešime sústavu, maticu upravujeme. [Varování 3.11, chap-04.woo:440] |
| Klouda | „Definice je jenom jedna.“ | V otázke na definíciu regularity uviesť existenciu inverzie, nie iba plnú hodnosť. [la1-P7-par2-klouda 48:06–49:09] |
| Klouda | „Musíte ještě využít toho, že hodnost matice A nemůže být větší než N“ | Pri dôkaze $h(A)=n$ nestačí odvodiť $h(A)\geq n$. [la1-P7-par2-klouda 52:22] |
| Klouda | „tak začnou vymyslet složitý věci“ | Pri regularite súčinu je priamočiary kandidát na inverziu; cesta cez hodnosť býva zbytočne komplikovaná. [la1-P7-par2-klouda 26:41–26:48] |
| Kleprlík | „Druhý nejoblíbenější důkaz ze zkouškový písemky“ | Hovorí o dôkaze regularity súčinu; jeho odhad opakovania nie je štatistika korpusu. [la1-P7-par1-kleprlik 47:27] |
| Kleprlík | „bez jakéhokoli komentáře“ | Štandardný mechanický výpočet inverzie cez blokovú maticu možno použiť priamo. [la1-P7-par1-kleprlik 1:27:55] |
| Proseminár | „Řekněme doporučen do písemky.“ | Odporúča spoločný GEM $(A\mid E)$ po predvedení troch spôsobov. [la1-prosem2223-03 1:12:28] |
| Krásenský | „Matici můžu gemovat, ale nemůžu ji řešit.“ | Trvá na pomenovaní skutočného matematického problému. [la1-P7-par3-krasensky 53:40] |
| Krásenský | „my po vás ten důkaz nebudeme chtít“ | V kontexte Vety 3.12 o transpozícii; jej tvrdenie treba poznať a používať. [la1-P7-par3-krasensky 57:51–58:21] |
| Krásenský | „Na písemku ji nepotřebujete.“ | O teórii hodnosti pre práve chystaný druhý zápočtový test, nie o záverečnej skúške. [la1-P7-par3-krasensky 53:18–53:24] |
| Kleprlík | „teoreticky dá použít už i v té druhé zápočtové písemce“ | O technike cez zachovanie obalu a hodnosť; rozdiel oproti Krásenského odporúčaniu zachovať. [la1-P7-par1-kleprlik 13:49–13:53] |

Na papieri preto rozlišovať tri požiadavky: **definovať**, **vypočítať**, **dokázať**. Skúška Frobenius ich v jednej úlohe žiada oddelene. [testy-la1-zkouska.md:9]
Čísla viet sú orientácia pre autora lekcie; všeobecný príkaz písať ich na teste zdroj nemá. Kleprlík pri explicitnej nerovnosti $h(AB)\leq h(A)$ hovorí, že je zrejmé, o ktorú vetu ide. [la1-P7-par1-kleprlik 1:39:19–1:39:34]

## 7. Typické úlohy

Počty označujú konkrétne položky dostupného inventára, nie frekvenciu v celej histórii skúšok.
`sources.json` obsahuje aj prednáškové úlohy, opakované výskyty a záznamy iba s názvom; tieto kategórie nemožno sčítať ako nezávislé ostré testy.

| Typ | Krátky príklad zadania po česky | Odkaz | Ako často v dostupných podkladoch |
|---|---|---|---|
| Definície hodnosti a regularity | „Definujte hodnost matice.“ | [testy-la1-zkouska.md:9; la1-marast-14159] | Ostrá skúška Frobenius aj ukážková skúška MARAST. |
| Definícia inverzie | „Napište definici inverzní matice“ | [testy-la1-zkouska.md:9; testy-la1-zkouska.md:21] | Dve z troch prepísaných skúškových variantov. |
| Jednoznačnosť inverzie | „Dokažte, že pro každou matici existuje nejvýše jedna matice k ní inverzní.“ | [la1-exam-2025-12-17-frobenius-3/c] | Jeden priamy ostrý doklad; opakovane predvádzaný dôkaz. |
| Regularita a plná hodnosť | „Formulujte korektně tvrzení, které říká, jakou hodnost mají regulární matice.“ | [la1-exam-2025-12-17-frobenius-3/d,e] | Jeden ostrý doklad formulácie aj dôkazu. |
| Hodnosť konkrétnej matice | „Najděte hodnost matice“ | [la1-marast-13741; la1-marast-13758; Příklad 3.8, chap-04.woo:293] | Dve samostatné úlohy tematickej cvičebnice, príklad skrípt; aj Bolzano, príklad 1 D. |
| Hodnosť s parametrom a regularita | „Spočtěte hodnost A v závislosti na parametru.“ | [testy-la1-zkouska.md:14; la1-marast-13759; la1-marast-13742] | Jedna ostrá skúška, dve tematické úlohy, z toho jedna nad $\mathbb Z_5$. |
| Výpočet inverzie | „Ověřte, zda je matice ... regulární. Pokud je, najděte její inverzi.“ | [la1-marast-13743; la1-marast-13794; la1-marast-13811; testy-la1-zkouska.md:14] | Tri tematické položky cvičebnice a Hilbert C; príklad skrípt 3.38 nad $\mathbb Z_7$. |
| Overenie kandidáta na inverziu | „Je k matici A ... inverzní matice B?“ | [la1-skripta-chap04-955] | Jedna otázka skrípt s dvoma kandidátmi. |
| Algebra inverzie | „Upravte výraz $(3BB^T)^{-1}$.“ | [testy-la1-zkouska.md:15] | Jeden ostrý doklad; používa 3.27 a 3.28. |
| Hodnosť po násobení regulárnou | „Formulujte korektně tvrzení ... pokud je druhá z nich regulární.“ | [testy-la1-zkouska.md:21] | Jeden ostrý doklad pre Dôsledok 3.40. |
| Konštrukcia násobiacej matice | „Napište matici B takovou, že po součinu ...“ | [la1-skripta-chap04-778] | Jedna otázka skrípt, viac prednáškových ilustrácií. |
| GEM ako súčin | „Jaká matice P odpovídá GEM $A\sim B$?“ | [la1-skripta-chap04-1270] | Jeden rozpracovaný príklad skrípt; všetky tri paralelky. |
| Maticová rovnica | „Najděte všechny matice X ... $BX+3B=E$“ | [la1-marast-13744; la1-marast-13783] | Dve tematické položky; prvá na konci proseminára. |
| Počítanie regulárnych matíc | „Kolik existuje různých regulárních matic ...“ | [testy-la1-zkouska.md:22; la1-exam-2025-12-17-frobenius-3/a2] | Bonus Bolzano; Frobenius má jednoduché počítanie hodnosti 1 v $\mathbb Z_7^{1,1}$. |
| Báza a rovnosť obalov cez hodnosť | „Rozhodněte, zda ... tvoří bázi stejného podprostoru“ | [marast-problems/lecture-258.md:104–176] | Dva nadväzujúce príklady #13486 a #13487; tematicky presahujú kap. 2. |

Druhý zápočtový test: v `testy-la1-2.md` sa priamy typ hodnosť/regularita/inverzia nenašiel; úlohy o bázach, LN a obaloch patria do kap. 2.
Nevyvodzovať z toho zákaz každého použitia hodnosti: prednášky sa v odporúčaní líšia, pozri sekcie 6 a 9.
`testy-rozstrel.md` obsahuje BI-DML, nie LA1; pre rozstřel LA1 k tejto kapitole **zdroj nemá**. [testy-rozstrel.md:1]
MARAST uvádza početný kvíz kap. 3 a teoretický kvíz kap. 3 a 4, iba s metadátami; znenie ani parafrázy ich otázok sa tu nepoužívajú. [marast.md:65–81]

## 8. Časté chyby a otázky (Discord, prednášky)

- **Hodnosť zamieňaná za algoritmus:** na otázku po definícii odpoveď „po GEM spočítam riadky“ nestačí. [Varování 3.11, chap-04.woo:430–431; la1-P7-par3-krasensky 53:40–54:40]
- **Plná hodnosť zamieňaná za definíciu regularity:** ide o ekvivalentnú vlastnosť, nie o definíciu požadovanú kurzom. [la1-P7-par2-klouda 48:06–49:09]
- **Riadkový a stĺpcový obal:** GEM zachováva riadkový obal, stĺpcový všeobecne mení; dimenzia stĺpcového obalu sa zachováva. [la1-prosem2223-03 14:56–15:28]
- **Rovnaká dimenzia dvoch obalov:** sama osebe nedokazuje ich rovnosť, treba aj kontrolu spoločného obalu. [bi-la1-prezentace-03.tex:69; marast-problems/lecture-258.md:141–176]
- **Parameter bez vetvenia:** delenie $a+1$ môže stratiť prípad $a=4$ v $\mathbb Z_5$. [la1-prosem2223-03 27:18]
- **Discord k parametrom:** študent nevie, kam smerovať GEM s parametrom nad konečným telesom; v zachytenom kontexte chýba odpoveď s riešením. To je potreba výučby, nie doložený ostrý príklad. [discord-la1.md:543–549]
- **Jednostrannosť bez podmienok:** Veta 3.42 má $A,B\in T^{n,n}$; neprepisovať ju pre ľubovoľné obdĺžnikové matice. [chap-04.woo:1660; la1-P8-par3-krasensky 10:47–11:14]
- **Prehodenie činiteľov:** pri inverzii súčinu sa poradie obracia; v maticovej rovnici násobiť inverziou zo správnej strany. [chap-04.woo:1022; la1-prosem2223-03 1:18:23–1:18:48]
- **Chyba vo vzore:** proseminár pri treťom výpočte prizná chybu v prvom postupe; výslednú inverziu preberať z kontrolovaného záveru a MARAST, nie z prvého diktovania. [la1-prosem2223-03 1:11:58; marast-problems/lecture-258.md:53–57]
- **Falošný doklad pravidiel skúšky:** Discordový spor o zachovanie obalu pri GEM je príbeh zo štátnic, nie z ústnej LA1. [discord-la1.md:68–74]

Samostatnú overenú discordovú otázku o výpočte inverzie alebo definícii regularity dodaný výber neposkytol; širší Discord nebol týmto výťahom prehľadaný.

## 9. Rozdiely medzi paralelkami

- **Kleprlík:** hodnosť už na konci P6, celú regularitu dokončí na P7; opakovane rozlišuje ľahké skúškové dôkazy a technické dôkazy. Pri inverzii dáva mechanickému postupu výslovné povolenie bez komentára. [la1-P6-par1-kleprlik 1:03:20–1:34:26; la1-P7-par1-kleprlik 47:20–47:27; 1:27:55]
- **Klouda:** motivuje najprv invariantom obalu, potom hodnosťou; na P6 odvodí odhad hodnosti súčinu a na P7 najskôr skúma, prečo nemožno inverziu očakávať pre všetky matice. Zdôrazní chybu chýbajúceho horného odhadu v dôkaze regularity. [la1-P6-par2-klouda 1:00:25–1:08:26; la1-P7-par2-klouda 16:53–20:27; 52:22]
- **Krásenský:** P6 zostáva v kap. 2; P7 hodnosť počíta dvakrát, z definície a pomocou viet. Väčšinu regularity a inverzie dokončuje až na P8; táto časť nie je iba krátky úvod P8. [la1-P7-par3-krasensky 29:25–48:33; la1-P8-par3-krasensky 05:19–1:24:09]
- **Krásenského dôkaz 3.39:** pridáva pomocné tvrdenie o rozklade regulárnej matice na elementárne matice, ktoré potom aplikuje na $A$. Skriptá majú stručnejší dôkaz cez elimináciu $P\sim E$. [la1-P8-par3-krasensky 1:11:31–1:18:31; chap-04.woo:1598–1606]
- **Druhý test:** Kleprlík povie, že techniku cez hodnosť možno teoreticky použiť; Krásenský chce najskôr ovládať zdôvodnenia predchádzajúcej kapitoly a hodnosť na tento test neodporúča. Toto nie je rozpor v matematike, ale rozdiel v didaktickom odporúčaní. [la1-P7-par1-kleprlik 13:49–13:53; la1-P7-par3-krasensky 09:06–10:36; 50:00–50:16]
- **Proseminár:** na jednej matici prejde od riešenia sústav cez súčin elementárnych matíc k $(A\mid E)$; je najkonkrétnejším lokálnym zdrojom na počítanie. [la1-prosem2223-03 40:50–1:12:28]

## 10. Nejasné a chýbajúce

- **Kloudova P7 má stav partial.** Dodaný prepis pokrýva všetky hlavné témy až po 3.42 a ďalej Frobeniovu vetu, no INDEX nevysvetľuje, čo je neúplné. Nepredpokladať stopercentné zachytenie audia.
- **Skúškový korpus je neúplný.** Prepísané sú iba varianty Frobenius, Hilbert a Bolzano; Jarník, Kurzweil, Eukleides, Tarski, Riemann a ďalšie sú v zdroji bez prepisu. Frekvencie v sekcii 7 preto nie sú odhadom všetkých termínov. [testy-la1-zkouska.md:3]
- **Proseminár nemá meno prednášajúceho.** Titulky nechýbajú, ale autorstvo z nich ani metadát nebolo možné bezpečne určiť.
- **Zaujímavosti skrípt nie sú doložené prednáškovým výkladom.** Tvrdenie 3.43 je v skriptách, ale blokové a rýchle násobenie sa v určených úsekoch nepotvrdilo; skúškovú dôležitosť zdroj nemá.
- **Ostré skúšky sú neúplný korpus.** `testy-la1-zkouska.md:3` uvádza neprepísané ďalšie varianty; záznamy `id-only` v inventári nie sú dôkazom konkrétneho znenia ani počtu výskytov úloh.
- **Rozstřel LA1 zdroj nemá.** Dodaný súbor s týmto názvom je o DML; jeho úlohy nemožno vydávať za testovanie LA1.
- **Kvízy kap. 3 a 3+4 majú len metadáta.** Znenia sa nezískavali a nepoužili. Stav „nový“ je stav lokálneho odpisu, nie živá kontrola k 17. 11. 2026. [marast.md:65–81]
- **Automatické titulky obsahujú chyby.** Kloudovo „vynásobení nulovým číslem“ pri vratnosti GEM o 42:00 je v rozpore so správnou podmienkou $\alpha\neq0$ na slajde a v skriptách; ide o nejasné miesto prepisu, nie dovolenú operáciu. [la1-P7-par2-klouda 42:00; bi-la1-prezentace-03.tex:374; chap-04.woo:1180]
- **Nesprávny smer v ústnom diktovaní algoritmu:** Kleprlík o 1:28:06 hovorí „Nadspěte ji za tu jednotkovou matici“, no zvyšok vety aj formálny zdroj jednoznačne pracujú s $A$ vľavo a $E$ vpravo. (Nejasné: slovosled/prepis.) [la1-P7-par1-kleprlik 1:28:06; chap-04.woo:1524]
- **Poznámka MARAST #13486 má číselnú odchýlku:** zadanie na riadku 84 obsahuje posledný vektor $(0,-2,-1,-5)$, postup na riadkoch 90 a 101 jeho opačný vektor. Obal to nemení, ale nie je to doslovne identický súbor. [marast-problems/lecture-258.md:84–101]
- **Aktuálne hodnotiace rubriky zdroj nemá.** Výťah dokladá konkrétne požiadavky z nahrávok a prepísaných skúšok, nesľubuje presné bodovanie každej budúcej odpovede.

## 11. Surové zdroje

Základné adresáre: `K=kontext`, `S=zdroje`.

- `K/BRIEF.md`: pravidlá, citačný formát a osnova.
- `K/INDEX.md`: nahrávky, paralelky, stav titulkov; záznam P7-par2 je partial.
- `K/cisla/la1-kap3.tsv`: jediné použité číslovanie 3.1–3.43.
- `S/src/bi-la1-master/src/textbook/chap-04.woo`: jadro 47–1674; sekcia Zajímavosti od 1679.
- `K/skripta/bi-la1-master/src/slides/bi-la1-prezentace-03.tex`: 16–100 hodnosť; 160–249 násobenie; 252–325 inverzia; 328–455 GEM a kritériá; od 460 hra.
- `K/txt/la1-P6-par1-kleprlik.txt`: 1:03:20–1:34:26.
- `K/txt/la1-P7-par1-kleprlik.txt`: 11:39–1:37:23, dodatok 1:39:19–1:40:05.
- `K/txt/la1-P8-par1-kleprlik.txt`: skontrolovaný začiatok; od 10:19 už kap. 4.
- `K/txt/la1-P6-par2-klouda.txt`: 59:25–1:36:48.
- `K/txt/la1-P7-par2-klouda.txt`: 07:43–1:19:13; od 1:20:19 prechod do kap. 4.
- `K/txt/la1-P6-par3-krasensky.txt`: skontrolované dokončenie kap. 2, najmä 1:33:34–1:36:06.
- `K/txt/la1-P7-par3-krasensky.txt`: 08:20–1:31:19.
- `K/txt/la1-P8-par3-krasensky.txt`: 05:19–1:24:09, prechod 1:24:17.
- `K/txt/la1-prosem2223-03.txt`: 00:30–1:21:40; hodnosť 16:12–36:09, inverzia 36:34–1:12:28, rovnica do záveru.
- `shared/src/commonMain/composeResources/files/coverage/sources.json`: položky LA1, kapitola S3 a presahy podľa `parts[].chapter`; v texte uvedené identifikátory.
- `S/testy-la1-zkouska.md`: 3, 9, 14–15, 19, 21–22.
- `S/testy-la1-2.md`: kontrola priamych typov kapitoly 3; bez nálezu.
- `S/testy-rozstrel.md`: hlavička potvrdzuje BI-DML, nie zdroj úloh LA1.
- `S/marast-problems/lecture-258.md`: cvičebnica; najmä #13741–13744, #13486–13487, #13758–13759; ďalšie typy podľa inventára #13794, #13811, #13783.
- `S/marast.md:65–81`, `S/marast-cvicebnice.md:24`: iba metadáta kvízov a prehľad cvičebnice.
- `S/marast-blog-45.md:257–321`: skontrolovaný presah do Frobeniovej vety a počítania podpriestorov; nepoužitý ako zdroj jadra kap. 3.
- `S/discord-la1.md`: 68–74, 535–549; lokálny výber diskusií, nie úplný archív Discordu.

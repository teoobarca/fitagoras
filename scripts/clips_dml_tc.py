#!/usr/bin/env python3
"""Builds shared/src/commonMain/composeResources/files/clips-dml-tc.json: the number theory clips of BI-DML, Spěvák
(paralelka 2, 6. to 8. prednáška, 4., 11. and 18. 11. 2026) and Dombek (paralelka 1, 8. and 9. prednáška, 16. and
23. 11. 2026).

The moments were picked from the captions and checked against the caption text; the words and their times are
Whisper's, cut to the clip and shifted so that 0 is the clip's start.
CAPTIONS_API (required) is the captions endpoint of the app's server, the URL that ends in /api/captions/; the
script appends the recording's id to it.

    CAPTIONS_API=<server>/api/captions/ python3 scripts/clips_dml_tc.py
"""

import json
import os
import sys
import urllib.parse
import urllib.request
from pathlib import Path

API = os.environ.get("CAPTIONS_API", "").rstrip("/") + "/"
if API == "/":
    sys.exit("Set CAPTIONS_API to the captions endpoint of the app's server (the URL that ends in /api/captions/).")
P6 = ("3675538841@sirius.fit.cvut.cz", "6. prednáška")
P7 = ("3675538842@sirius.fit.cvut.cz", "7. prednáška")
P8 = ("3675538843@sirius.fit.cvut.cz", "8. prednáška")
D8 = ("3674210639@sirius.fit.cvut.cz", "8. prednáška", "Dombek")
D9 = ("3674210640@sirius.fit.cvut.cz", "9. prednáška", "Dombek")

CLIPS = [
    dict(id="dml-tc-c-delitelnost", lesson=1, src=P6, t0=2364.0, t1=2397.2,
         why="a delí b, keď existuje celé k s a·k = b; a je deliteľ, b násobok, b je deliteľné a.",
         before="Ako je definované „§a§ delí §b§“?",
         task=dict(q="Platí podľa tejto definície §0|0§?",
                   options=["Áno, napr. §k=1§ dáva §0\\cdot 1=0§", "Nie, nulou sa deliť nedá", "Nedá sa to rozhodnúť"], correct=0,
                   why="Áno, stačí jedno celé §k§ s §0\\cdot k=0§. Definícia nedelí, len násobí.",
                   wrong={1: "Definícia nedelí, iba násobí. §0\\cdot 1=0§, teda §k=1§ vyhovuje.",
                          2: "Dá sa: stačí nájsť jedno celé §k§, a §k=1§ dáva §0\\cdot 1=0§."})),
    dict(id="dml-tc-c-gcd", lesson=1, src=P6, t0=3521.1, t1=3600.8,
         why="Na skúške chce Spěvák definíciu gcd ako spoločného deliteľa, ktorý je deliteľný každým spoločným deliteľom.",
         before="Akú definíciu najväčšieho spoločného deliteľa chce Spěvák na skúške?",
         task=dict(q="Čo musí splniť §d=\\gcd(a,b)§ okrem toho, že delí §a§ aj §b§?",
                   options=["Každý spoločný deliteľ §a,b§ delí §d§", "§d§ je najväčšie číslo, ktoré delí §a§ aj §b§", "§d§ je prvočíslo"], correct=0,
                   why="Každý spoločný deliteľ musí deliť §d§. Tak je §d§ najväčší podľa deliteľnosti, nie podľa veľkosti, a definícia dá aj §\\gcd(0,0)=0§ (Definice 6.3).",
                   wrong={1: "Táto definícia zlyhá pre §\\gcd(0,0)§: najväčší deliteľ nuly neexistuje. Nulu delí každé prirodzené číslo, takže spoločné delitele §0§ a §0§ nemajú najväčší podľa veľkosti; Spěvák preto školskú definíciu berie len s takýmto doplnením.",
                          2: "§\\gcd(4,8)=4§ nie je prvočíslo, a predsa je to §\\gcd§."})),
    dict(id="dml-tc-c-bezout", lesson=2, src=P6, t0=4675.4, t1=4722.75,
         why="gcd(a, b) je celočíselná lineárna kombinácia a a b; koeficienty nie sú určené jednoznačne.",
         before="Čo hovorí Bézoutova rovnosť a sú jej koeficienty jednoznačné?",
         task=dict(q="§\\gcd(4,6)=2=(-1)\\cdot 4+1\\cdot 6§. Je to jediná dvojica koeficientov?",
                   options=["Nie, napr. aj §2\\cdot 4+(-1)\\cdot 6=2§", "Áno, Bézoutove koeficienty sú jednoznačné", "Áno, ak ich nájde REA"], correct=0,
                   why="Nie: §(-1)\\cdot 4+1\\cdot 6=2§ aj §2\\cdot 4+(-1)\\cdot 6=2§. K dvojici smieš pripočítať ľubovoľný celý násobok §(3,-2)§, lebo §4\\cdot 3+6\\cdot(-2)=0§; dvojíc koeficientov je preto nekonečne veľa.",
                   wrong={1: "Nie sú jednoznačné, platí aj §2\\cdot 4+(-1)\\cdot 6=2§.",
                          2: "REA dá len jednu dvojicu, ale platí aj §2\\cdot 4+(-1)\\cdot 6=2§. Ďalšie dostaneš pripočítaním násobkov §(3,-2)§, lebo §4\\cdot 3+6\\cdot(-2)=0§."})),
    dict(id="dml-tc-c-ldr", lesson=3, src=P7, t0=765.9, t1=802.93,
         why="Diofantická rovnica ax + by = c má riešenie práve vtedy, keď gcd(a, b) delí c; dôkaz stojí na Bézoutovej rovnosti.",
         before="Kedy má §ax+by=c§ celočíselné riešenie?",
         task=dict(q="Má §6x+9y=20§ celočíselné riešenie?",
                   options=["Nie, §\\gcd(6,9)=3§ nedelí §20§", "Áno, lebo §6§ aj §9§ sú menšie než §20§", "Áno, ak smú byť §x,y§ záporné"], correct=0,
                   why="Nie: ľavá strana je vždy násobok §3§, §20§ nie je. To je Věta 6.7.",
                   wrong={1: "Riešenie neexistuje: §6x+9y§ je násobok §3§, ale §20§ nie. Veľkosť nerozhoduje.",
                          2: "Riešenie neexistuje ani so zápornými §x,y§: §6x+9y§ je vždy násobok §3§, §20§ nie."})),
    dict(id="dml-tc-c-kongruence", lesson=4, src=P7, t0=4155.5, t1=4218.4,
         why="a ≡ b (mod m), keď m delí rozdiel a − b; modulo 2 sú kongruentné čísla s rovnakou paritou.",
         before="Ako je definovaná kongruencia modulo §m§?",
         task=dict(q="Platí §-5\\equiv 9\\pmod{7}§?",
                   options=["Áno, §7§ delí §-5-9=-14§", "Nie, záporné číslo nemôže byť kongruentné s kladným", "Nie, lebo §-5\\bmod 7§ nie je §9§"], correct=0,
                   why="Áno, §7§ delí rozdiel §-14§. Obe čísla majú zvyšok §2§.",
                   wrong={1: "Môže, rozhoduje len deliteľnosť rozdielu. §-14=(-2)\\cdot 7§.",
                          2: "Kongruencia neporovnáva zvyšok s číslom, ale dve čísla. Obe majú zvyšok §2§."})),
    dict(id="dml-tc-c-mod", lesson=4, src=P7, t0=5055.2, t1=5121.3,
         why="a mod m je funkcia, ktorá vráti zvyšok; a ≡ b (mod m) je vzťah dvoch čísel s rovnakým zvyškom.",
         before="Aký je rozdiel medzi §a\\bmod m§ a §a\\equiv b\\pmod{m}§?",
         task=dict(q="Ktoré zápisy sú správne?",
                   options=["§17\\bmod 5=2§ aj §17\\equiv 7\\pmod{5}§", "§17\\bmod 5=7§", "§17\\equiv 2\\pmod{5}§, teda §17=2§"], correct=0,
                   why="Oba platia: funkcia vráti zvyšok §2§, kongruencia spája čísla s rovnakým zvyškom.",
                   wrong={1: "§\\bmod§ vracia zvyšok z §\\{0,\\dots,4\\}§, a §7§ medzi nimi nie je.",
                          2: "Kongruencia nie je rovnosť. §17\\neq 2§, len majú rovnaký zvyšok."})),
    dict(id="dml-tc-c-zapis", lesson=4, src=D8, t0=2825.0, t1=2894.5,
         why="a ≡ b (mod m) je vzťah: čísla sa líšia o násobok m. a = b mod m tvrdí viac: a je ten jediný zvyšok b; z druhého plynie prvé, opačne nie.",
         before="Prečo Dombek trvá na zátvorke v §a\\equiv b\\pmod{m}§?",
         task=dict(q="Platí §12\\equiv 7\\pmod{5}§. Platí aj §12=7\\bmod 5§?",
                   options=["Nie, §7\\bmod 5=2\\neq 12§", "Áno, je to iný zápis toho istého", "Áno, lebo §5\\mid 12-7§"], correct=0,
                   why="Nie, §12=7\\bmod 5§ tvrdí, že §12§ je zvyšok po delení §5§, a to nie je.",
                   wrong={1: "Nie je to to isté: rovnica so zvyškom tvrdí viac. §7\\bmod 5=2§, nie §12§.",
                          2: "§5\\mid 12-7§ dáva len kongruenciu. Rovnica §12=7\\bmod 5§ chce navyše, aby §12§ bol zvyšok, a §7\\bmod 5=2§."})),
    dict(id="dml-tc-c-z23", lesson=4, src=D8, t0=5245.8, t1=5298.2,
         why="V malom ℤ_p hľadáš inverziu hrubou silou, no v ℤ_23 na písomke z LA1 ťa zachráni Euklidov algoritmus.",
         before="Ako hľadať inverziu v §\\mathbb{Z}_{23}§, keď skúšanie je zdĺhavé?",
         task=dict(q="Prečo má v §\\mathbb{Z}_{23}§ inverziu každé nenulové číslo?",
                   options=["§23§ je prvočíslo, preto §\\gcd(a,23)=1§ pre §a=1,\\dots,22§", "Lebo §23§ je nepárne", "Lebo §\\mathbb{Z}_{23}§ má viac než §20§ prvkov"], correct=0,
                   why="§23§ je prvočíslo, takže každé §a\\in\\{1,\\dots,22\\}§ je s ním nesúdeliteľné (Důsledek 6.2).",
                   wrong={1: "Nepárnosť nestačí: §9§ je nepárne a §3§ v ňom inverziu nemá.",
                          2: "Veľkosť nerozhoduje: v §\\mathbb{Z}_{24}§ nemá inverziu ani §2§. Rozhoduje, že §23§ je prvočíslo."})),
    # Spěvák, 8. prednáška (18. 11.): lineárne kongruencie, sústavy, Eulerova funkcia a veta (lekcie 5 až 7)
    dict(id="dml-tc-c-kraceni-modul", lesson=5, src=P8, t0=897.88, t1=948.48,
         why="Kongruenciu smieš krátiť, ale modul vydelíš gcd modulu a krátiaceho čísla: z 1·3 ≡ 5·3 (mod 12) je 1 ≡ 5 (mod 4).",
         before="Čo sa stane s modulom, keď kongruenciu krátiš?",
         task=dict(q="Spěvák krátí §1\\cdot 3\\equiv 5\\cdot 3\\pmod{12}§ trojkou. Aký je nový modul?",
                   options=["§4§", "§12§", "§36§"], correct=0,
                   why="§12/\\gcd(3,12)=4§; §1\\equiv 5\\pmod 4§ platí, modulo §12§ nie.",
                   wrong={1: "Modulo §12§ je §1\\not\\equiv 5§. Modul sa delí §\\gcd(3,12)=3§.",
                          2: "Modul sa delí, nie násobí: §12/\\gcd(3,12)=4§."})),
    dict(id="dml-tc-c-lkon-deleni", lesson=5, src=P8, t0=2424.10, t1=2473.02,
         why="Lineárnu kongruenciu riešiš ako lineárnu rovnicu; rozdiel je len v tom, že deliť znamená vynásobiť inverziou a pri krátení sa môže zmeniť modul.",
         before="Čím sa riešenie §4x\\equiv 6\\pmod{25}§ líši od riešenia rovnice §4x=6§?",
         task=dict(q="Ako Spěvák „vydelí“ §4x\\equiv 6\\pmod{25}§ štvorkou?",
                   options=["Vynásobí obe strany §4^{-1}\\equiv 19§", "Vydelí §6§ štyrmi", "Vydelí štyrmi aj modul"], correct=0,
                   why="§4\\cdot 19=76\\equiv 1\\pmod{25}§, preto §x\\equiv 6\\cdot 19=114\\equiv 14§.",
                   wrong={1: "§6/4§ nie je celé číslo. V §\\mathbb{Z}_{25}§ sa delí násobením inverziou: §4^{-1}\\equiv 19§.",
                          2: "§\\gcd(4,25)=1§, modul ostáva §25§; stačí vynásobiť inverziou §19§."})),
    dict(id="dml-tc-c-zcvoz", lesson=6, src=P8, t0=2919.60, t1=2986.66,
         why="Sústava má riešenie práve vtedy, keď gcd každej dvojice modulov delí rozdiel pravých strán; riešenie je jediné modulo lcm modulov.",
         before="Kedy má sústava §x\\equiv a_i\\pmod{m_i}§ riešenie a koľko ich je?",
         task=dict(q="Sústava §x\\equiv 5\\pmod 6§, §x\\equiv 3\\pmod{10}§. Platí podmienka vety?",
                   options=["Áno, §\\gcd(6,10)=2§ delí §5-3§", "Nie, moduly sú súdeliteľné", "Áno, lebo §5>3§"], correct=0,
                   why="§2\\mid 2§, riešenie existuje a je jediné modulo §\\lcm(6,10)=30§.",
                   wrong={1: "Súdeliteľné moduly nevadia, ak §\\gcd§ delí rozdiel pravých strán: §2\\mid 5-3§.",
                          2: "Veľkosť pravých strán nerozhoduje. Rozhoduje §\\gcd(6,10)=2\\mid 5-3§."})),
    dict(id="dml-tc-c-modul-chyba", lesson=6, src=P8, t0=3467.42, t1=3512.30,
         why="Pri dosadzovaní Spěvák krátil 6t ≡ 8 (mod 10) dvoma a zabudol zmenšiť modul; opravil sa na modulo 5: pri krátení sa modul delí tiež.",
         before="Na čo si dať pozor, keď pri dosadzovaní krátiš kongruenciu?",
         task=dict(q="§6t\\equiv 8\\pmod{10}§ krátim dvoma. Čo dostanem?",
                   options=["§3t\\equiv 4\\pmod 5§", "§3t\\equiv 4\\pmod{10}§", "§3t\\equiv 8\\pmod 5§"], correct=0,
                   why="Modul sa delí §\\gcd(10,2)=2§ (Věta 6.13): §3t\\equiv 4\\pmod 5§, teda §t\\equiv 3\\pmod 5§.",
                   wrong={1: "To je presne chyba, ktorú Spěvák opravil: modul treba vydeliť §\\gcd(10,2)=2§, teda modulo §5§.",
                          2: "Kráti sa celá kongruencia: aj pravá strana §8/2=4§, aj modul §10/2=5§."})),
    dict(id="dml-tc-c-ev-22", lesson=7, src=P8, t0=4598.28, t1=4670.54,
         why="φ(22) = 10 a 3 je s 22 nesúdeliteľné, preto 3^10 ≡ 1 a pri exponente 10q + 1 zostane 3; s dvojkou to nejde, lebo je s 22 súdeliteľná.",
         before="Ako Spěvák spočíta mocninu s obrovským exponentom modulo §22§ rýchlejšie než Wolfram Alpha?",
         task=dict(q="Prečo Spěvák zmenil základ z §2§ na §3§?",
                   options=["§\\gcd(2,22)=2§, Eulerova veta by neplatila", "§3§ je prvočíslo", "Z §2§ by vyšla nula"], correct=0,
                   why="Eulerova veta chce základ nesúdeliteľný s modulom; §\\gcd(3,22)=1§, ale §\\gcd(2,22)=2§.",
                   wrong={1: "Aj §2§ je prvočíslo. Vadí, že §2§ delí §22§: §\\gcd(2,22)=2§.",
                          2: "§2^k§ nikdy nie je násobok §22§, nula nevyjde. Vadí, že §\\gcd(2,22)=2\\neq 1§."})),
    dict(id="dml-tc-c-phi-pozor", lesson=7, src=P8, t0=5190.50, t1=5243.36,
         why="φ(12) = φ(3)·φ(4) = 4, lebo 3 a 4 sú nesúdeliteľné; φ(2)·φ(6) = 2 ≠ 4: multiplikativita platí len pre nesúdeliteľné čísla.",
         before="Smieš počítať §\\varphi(12)§ ako §\\varphi(2)\\cdot\\varphi(6)§?",
         task=dict(q="Ktorý rozpis dá správne §\\varphi(12)§?",
                   options=["§\\varphi(3)\\cdot\\varphi(4)=2\\cdot 2§", "§\\varphi(2)\\cdot\\varphi(6)=1\\cdot 2§", "§\\varphi(12)=11§"], correct=0,
                   why="§\\gcd(3,4)=1§, preto §\\varphi(12)=\\varphi(3)\\varphi(4)=4§: s §12§ sú nesúdeliteľné §1,5,7,11§.",
                   wrong={1: "§\\gcd(2,6)=2§, multiplikativita tu neplatí: vyšlo by §2§, ale §\\varphi(12)=4§.",
                          2: "§11§ by platilo pre prvočíslo. §12§ nie je prvočíslo; nesúdeliteľné s ním sú len §1,5,7,11§."})),
    # Dombek, 9. prednáška (23. 11.): binary powering, 3^644 mod 645 (lekcia 8). Whisper hears the exponent as "664"
    # where he says it first; these clips start after that, the binary digits he reads out (2^9 + 2^7 + 2^2) are 644.
    dict(id="dml-tc-c-kedy-binarne", lesson=8, src=D9, t0=2805.4, t1=2835.0,
         why="Square and multiply pomôže, keď sú základ a modul súdeliteľné, alebo keď po Eulerovej vete ostane veľký exponent.",
         before="V akých dvoch prípadoch Dombek siahne po metóde square and multiply?",
         task=dict(q="Pri ktorej úlohe Eulerova veta exponent zmenšiť nedovolí?",
                   options=["§3^{644}\\bmod 645§", "§2^{268}\\bmod 9§", "§17^{110}\\bmod 35§"], correct=0,
                   why="§\\gcd(3,645)=3§ a Eulerova veta chce základ nesúdeliteľný s modulom. Pri §2^{268}\\bmod 9§ aj §17^{110}\\bmod 35§ je §\\gcd=1§.",
                   wrong={1: "§\\gcd(2,9)=1§, Eulerova veta platí: §\\varphi(9)=6§ a §2^{268}\\equiv 2^4\\pmod 9§.",
                          2: "§\\gcd(17,35)=1§, Eulerova veta platí: §\\varphi(35)=24§ a §17^{110}\\equiv 17^{14}\\pmod{35}§."})),
    dict(id="dml-tc-c-umocni", lesson=8, src=D9, t0=2869.8, t1=2896.6,
         why="Počítame 3, 3², 3⁴, 3⁸, …: každú ďalšiu dostaneme umocnením predošlej na druhú a zvyšok hneď zredukujeme modulo 645.",
         before="Čo robí Dombek v každom kroku s mocninou trojky?",
         task=dict(q="Máš §3^8\\equiv 111\\pmod{645}§. Ako dostaneš zvyšok §3^{16}§?",
                   options=["§111^2=12321\\equiv 66§", "§111\\cdot 2=222§", "§111\\cdot 3=333§"], correct=0,
                   why="§3^{16}=(3^8)^2§, teda §111^2=12321=19\\cdot 645+66§.",
                   wrong={1: "Zdvojnásobiť treba exponent, nie zvyšok: §3^{16}=(3^8)^2\\equiv 111^2\\equiv 66§.",
                          2: "To je zvyšok §3^9§, exponent o jedna väčší. §3^{16}=(3^8)^2\\equiv 111^2\\equiv 66§."})),
    dict(id="dml-tc-c-dvojkovy", lesson=8, src=D9, t0=2983.4, t1=3013.2,
         why="644 = 2^9 + 2^7 + 2^2: súčet v exponente je súčin mocnín, preto sa vynásobia tri pripravené zvyšky.",
         before="Ako Dombek poskladá §3^{644}§ z pripravených mocnín?",
         task=dict(q="§644=2^9+2^7+2^2§. Ktoré zvyšky z tabuľky vynásobíš?",
                   options=["§3^{512}§, §3^{128}§ a §3^{4}§", "§3^{9}§, §3^{7}§ a §3^{2}§", "Všetky od §3§ po §3^{512}§"], correct=0,
                   why="§3^{644}=3^{512}\\cdot 3^{128}\\cdot 3^4§, lebo §a^{s+t}=a^s\\cdot a^t§. Vyjde §111\\cdot 396\\cdot 81\\equiv 36\\pmod{645}§.",
                   wrong={1: "§9§, §7§ a §2§ sú exponenty dvojky. Riadky sú §3^{2^9}=3^{512}§, §3^{2^7}=3^{128}§ a §3^{2^2}=3^4§.",
                          2: "Súčin všetkých riadkov je §3^{1023}§, lebo §1+2+\\dots+512=1023§. Len riadky pri jednotkách dajú §3^{644}§."})),
]


def main() -> None:
    cache: dict = {}
    out = []
    for c in CLIPS:
        rec, lecture, *who = c["src"]
        teacher = who[0] if who else "Spěvák"
        if rec not in cache:
            with urllib.request.urlopen(API + urllib.parse.quote(rec), timeout=60) as r:
                cache[rec] = json.load(r)["cues"]
        t0, t1 = c["t0"], c["t1"]
        cues = []
        for cue in cache[rec]:
            words = cue[3] or []
            if isinstance(words, str):
                words = json.loads(words)
            inside = [w for w in words if t0 - 0.05 <= w[0] < t1 and w[1] <= t1 + 0.6]
            if not inside:
                continue
            ws = [{"t0": round(max(0.0, w[0] - t0), 2), "t1": round(w[1] - t0, 2), "w": w[2]} for w in inside]
            cues.append({"t0": ws[0]["t0"], "t1": ws[-1]["t1"], "words": ws})
        task = dict(c["task"])
        task["wrong"] = {str(k): v for k, v in task["wrong"].items()}
        out.append({"id": c["id"], "course": "dml", "lesson": c["lesson"], "rec": rec, "lecture": lecture, "teacher": teacher, "from": t0, "to": t1,
                    "why": c["why"], "before": c["before"], "warning": False, "task": task, "cues": cues})
    dest = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files/clips-dml-tc.json"
    dest.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{len(out)} clips -> {dest}")
    for o in out:
        text = " ".join(w["w"] for cue in o["cues"] for w in cue["words"])
        print(o["id"], round(o["to"] - o["from"], 1), "s:", text[:90], "...", text[-60:])


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Builds shared/src/commonMain/composeResources/files/clips-dml-mi.json: the lecture clips of Matematická indukcia (BI-DML).

All clips are from BI-DML 4. přednáška, paralelka 2 (Spěvák, 14. 10. 2026). Bounds were picked on the word times of the
captions so that each clip starts and ends with a sentence. The words and their times are Whisper's, from
the captions endpoint of the app's server (/api/captions/<id>); nothing in the captions is typed by hand.
CAPTIONS_API (required) is the captions endpoint of the app's server, the URL that ends in /api/captions/; the
script appends the recording's id to it.

    CAPTIONS_API=<server>/api/captions/ python3 scripts/clips_dml_mi.py
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
REC = "3675538839@sirius.fit.cvut.cz"
LECTURE = "4. přednáška"

CLIPS = [
    {
        "id": "dml-mi-schody", "lesson": 1, "from": 2126.4, "to": 2174.2,
        "before": "Prečo stačia dva kroky na nekonečne veľa tvrdení?",
        "why": "Spěvák: ak sa panáčik dostane na prvý schod a z každého schodu na ďalší, vylezie ľubovoľne vysoko. To je celý princíp indukcie.",
        "task": {
            "q": "Čo v obraze so schodmi zodpovedá základnému kroku?",
            "options": ["Dostať sa na prvý schod", "Z každého schodu vyliezť na ďalší", "Vyliezť na všetky schody"],
            "correct": 0,
            "why": "Základný krok je prvý schod. Krok z každého schodu na ďalší je indukčný krok.",
            "wrong": {"1": "To je indukčný krok. Bez prvého schodu by sa nepoužil ani raz.",
                      "2": "To je záver, plynie až z oboch krokov spolu."},
        },
    },
    {
        "id": "dml-mi-implikacia", "lesson": 1, "from": 2441.3, "to": 2479.0,
        "before": "Dokazuje indukčný krok tvrdenie §V(n)§?",
        "why": "Spěvák: indukčný krok dokazuje celú implikáciu §V(n)\\Rightarrow V(n+1)§ pre každé §n\\geq n_0§, nie samotné §V(n)§ ani §V(n+1)§.",
        "task": {
            "q": "Čo presne dokazuje indukčný krok?",
            "options": ["Implikáciu §V(n)\\Rightarrow V(n+1)§ pre každé §n\\geq n_0§", "Samotné §V(n+1)§", "§V(n)§ pre všetky §n§"],
            "correct": 0,
            "why": "Implikáciu, a to pre každé §n\\geq n_0§. Spěvák: dokazuje sa celá implikácia, nie tvrdenie §V(n)§ ani §V(n+1)§.",
            "wrong": {"1": "Samo §V(n+1)§ nevieš, len za predpokladu §V(n)§.",
                      "2": "To je záver z oboch krokov, nie obsah kroku."},
        },
    },
    {
        "id": "dml-mi-pisomka", "lesson": 2, "from": 2534.6, "to": 2588.1, "warning": True,
        "before": "Ako má vyzerať základný krok na písomke?",
        "why": "Spěvák: označenie ZK a IK píšte do písomiek, aby sa v tom opravujúci vyznal. Základný krok overí obe strany pre §n=1§.",
        "task": {
            "q": "Prečo písať ZK a IK výslovne?",
            "options": ["Aby sa v riešení vyznal opravujúci", "Bez skratiek je dôkaz nesprávny", "Netreba, stačí výpočet"],
            "correct": 0,
            "why": "Spěvák: „aby se v tom ten opravující orientoval“. Riešenie musí ukázať, kde je ktorá časť.",
            "wrong": {"1": "Nesprávny by bol bez obsahu krokov, nie bez skratiek. Označenie pomáha čítať.",
                      "2": "Označenie pomáha opravujúcemu rozlíšiť kroky. Spěvák ho preto do písomiek výslovne odporúča."},
        },
    },
    {
        "id": "dml-mi-naruby", "lesson": 2, "from": 1399.9, "to": 1469.5, "warning": True,
        "before": "Stačí z dokazovanej rovnosti odvodiť §0=0§?",
        "why": "Spěvák: z toho, čo chcete dokázať, ste odvodili niečo pravdivé, ale dokázané to nie je. Treba ísť od §0=0§ späť, teda ekvivalentné úpravy.",
        "task": {
            "q": "Úpravou dokazovanej rovnosti vyšlo §0=0§. Je dokázaná?",
            "options": ["Nie, z nej len plynie pravda", "Áno, §0=0§ platí", "Áno, ak sme nič nedelili"],
            "correct": 0,
            "why": "Nie, implikácia ide opačným smerom. Aj z nepravdy sa dá odvodiť pravda.",
            "wrong": {"1": "§0=0§ platí vždy, aj keby pôvodná rovnosť neplatila. Z nepravdy sa dá odvodiť pravda.",
                      "2": "Aj bez delenia môže byť úprava len jedným smerom, napr. umocnenie. Treba ukázať opačnú implikáciu."},
        },
    },
    {
        "id": "dml-mi-slaba-nestaci", "lesson": 4, "from": 3117.6, "to": 3207.0,
        "before": "Prečo slabá indukcia nestačí na rozklad na prvočísla?",
        "why": "Spěvák: §n+1=a\\cdot b§, ale o §a§ a §b§ predpoklad pre §n§ nič nehovorí. Slabý princíp je tu nedostatočný.",
        "task": {
            "q": "Prečo IP o §n§ nepomôže pri §n+1=a\\cdot b§?",
            "options": ["§a§ ani §b§ nemusí byť §n§", "Lebo §n+1§ je prvočíslo", "Lebo §a\\cdot b>n§"],
            "correct": 0,
            "why": "IP hovorí len o §n§. O menších číslach §a,b§ nehovorí nič.",
            "wrong": {"1": "Ak je §n+1§ prvočíslo, je hotovo aj bez IP. Problém je zložené §n+1§.",
                      "2": "To platí, ale nevadí to. Chýba predpoklad o §a§ a §b§."},
        },
    },
    {
        "id": "dml-mi-nohy", "lesson": 4, "from": 3255.7, "to": 3335.4,
        "before": "Čo smieš predpokladať v silnej indukcii?",
        "why": "Spěvák: kým lezieš po schodoch, máš obsadené všetky pod sebou. Smieš predpokladať všetky tvrdenia od §V(n_0)§ po §V(n)§.",
        "task": {
            "q": "Čo je IP v silnej indukcii?",
            "options": ["§V(k)§ pre všetky §k\\in\\{n_0,\\dots,n\\}§", "Len §V(n)§", "§V(k)§ pre všetky §k\\geq n_0§"],
            "correct": 0,
            "why": "Všetky tvrdenia od §n_0§ po §n§. Spěvák: silnejší predpoklad.",
            "wrong": {"1": "To je IP slabej indukcie.",
                      "2": "To je celé tvrdenie. IP siaha len po §n§."},
        },
    },
]


def main() -> None:
    with urllib.request.urlopen(API + urllib.parse.quote(REC), timeout=60) as r:
        cues_all = json.load(r)["cues"]
    out = []
    for c in CLIPS:
        t0, t1 = c["from"], c["to"]
        cues = []
        for cue in cues_all:
            words = cue[3] or []
            if isinstance(words, str):
                words = json.loads(words)
            # strict bounds: a word that runs past the end belongs to the next sentence
            inside = [w for w in words if w[0] >= t0 - 0.05 and w[1] <= t1 + 0.05]
            if not inside:
                continue
            ws = [{"t0": round(max(0.0, w[0] - t0), 2), "t1": round(w[1] - t0, 2), "w": w[2]} for w in inside]
            cues.append({"t0": ws[0]["t0"], "t1": ws[-1]["t1"], "words": ws})
        out.append({
            "id": c["id"], "lesson": c["lesson"], "rec": REC, "lecture": LECTURE, "from": t0, "to": t1,
            "why": c["why"], "before": c["before"], "warning": bool(c.get("warning")),
            "task": c["task"], "cues": cues, "course": "dml",
        })
    dest = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files/clips-dml-mi.json"
    dest.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{len(out)} clips -> {dest}")
    for o in out:
        text = " ".join(w["w"] for c in o["cues"] for w in c["words"])
        print(o["id"], round(o["to"] - o["from"], 1), "s:", text[:60], "...", text[-40:])


if __name__ == "__main__":
    main()

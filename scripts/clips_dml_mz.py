#!/usr/bin/env python3
"""Builds shared/src/commonMain/composeResources/files/clips-dml-mz.json: the lecture moments of Množiny a zobrazenia (BI-DML, parallel 2, Spěvák).

The moments were found in the captions of P5 and P6 (2026-11-15) and cut on sentence boundaries by the word times. Captions come
from the captions endpoint of the app's server (/api/captions/<id>), cut to the clip and shifted so that 0 is the clip's start.
Nothing in the captions is typed by hand: the words and their times are Whisper's.
CAPTIONS_API (required) is the captions endpoint of the app's server, the URL that ends in /api/captions/; the
script appends the recording's id to it.

    CAPTIONS_API=<server>/api/captions/ python3 scripts/clips_dml_mz.py
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
P5 = "3675538840@sirius.fit.cvut.cz"
P6 = "3675538841@sirius.fit.cvut.cz"

CLIPS = [
    {
        "id": "dml-mz-demorgan", "lesson": 2, "rec": P5, "lecture": "5. přednáška", "from": 2269.5, "to": 2345.0, "warning": False,
        "why": "Spěvák dokazuje De Morganov zákon pre množiny reťazcom ekvivalencií: logický De Morgan, potom definícia doplnku a zjednotenia.",
        "before": "Ako dokázať §\\overline{A\\cap B}=\\overline{A}\\cup\\overline{B}§ bez obrázka?",
        "task": {
            "q": "Ktorý krok spojí §\\neg(x\\in A\\wedge x\\in B)§ s §(x\\notin A\\vee x\\notin B)§?",
            "options": ["Logický De Morganov zákon", "Definícia zjednotenia", "Vennov diagram"], "correct": 0,
            "why": "Logický De Morganov zákon: §\\neg(p\\wedge q)\\Leftrightarrow(\\neg p\\vee\\neg q)§.",
            "wrong": {"1": "Definícia zjednotenia príde až v poslednom kroku. Tu sa negácia konjunkcie mení na disjunkciu negácií.",
                      "2": "Obrázok nie je krok dôkazu. Spěvák ide čisto cez ekvivalencie formúl."},
        },
    },
    {
        "id": "dml-mz-pisomka", "lesson": 2, "rec": P5, "lecture": "5. přednáška", "from": 2347.0, "to": 2398.1, "warning": True,
        "why": "Rovnosť a inklúziu množín dokazuješ definíciami a reťazcom logických ekvivalencií, také úlohy bývajú na písomkách. Vennov diagram sa ťažko berie ako dôkaz.",
        "before": "Ako sa na písomke dokazuje rovnosť a inklúzia množín?",
        "task": {
            "q": "Čo dokazuješ pri §X\\subseteq Y§?",
            "options": ["Pre každé §x\\in\\mathcal{U}§: §x\\in X\\Rightarrow x\\in Y§", "Pre každé §x\\in\\mathcal{U}§: §x\\in X\\Leftrightarrow x\\in Y§", "Že vo Vennovom diagrame leží §X§ vnútri §Y§"], "correct": 0,
            "why": "Inklúzia je implikácia pre každý prvok univerza.",
            "wrong": {"1": "Ekvivalencia dokazuje rovnosť, inklúzii stačí implikácia. Rovnosť sú dve inklúzie.",
                      "2": "Diagram sa ťažko berie ako dôkaz, musel by byť nakreslený úplne presne. Dôkaz je cez formuly."},
        },
    },
    {
        "id": "dml-mz-totalne", "lesson": 3, "rec": P5, "lecture": "5. přednáška", "from": 2801.2, "to": 2855.7, "warning": True,
        "why": "Totálne zobrazenie dá každému prvku §A§ práve jeden obraz. V BI-DML sú všetky zobrazenia totálne, aj v otázkach, koľko zobrazení existuje.",
        "before": "Môže zobrazenie v BI-DML niektorému prvku §A§ nedať obraz?",
        "task": {
            "q": "Koľko obrazov má v BI-DML každý prvok §A§ pri §f:A\\to B§?",
            "options": ["Práve jeden", "Najviac jeden", "Aspoň jeden"], "correct": 0,
            "why": "Práve jeden, v BI-DML sú zobrazenia totálne.",
            "wrong": {"1": "Najviac jeden pripúšťa netotálne zobrazenia. Tie kurz bez upozornenia neuvažuje.",
                      "2": "Dva obrazy jedného prvku zobrazenie nedovolí vôbec (Definícia 5.7)."},
        },
    },
    {
        "id": "dml-mz-injektivne", "lesson": 4, "rec": P5, "lecture": "5. přednáška", "from": 3526.7, "to": 3600.4, "warning": False,
        "why": "Prosté zobrazenie nepošle dva rôzne prvky na ten istý. Formulácie s rovnosťou a s nerovnosťou sú ekvivalentné obmenou.",
        "before": "Ako sa dá injektivita sformulovať dvoma spôsobmi?",
        "task": {
            "q": "Ktorá formula je ekvivalentná s §f(x)=f(y)\\Rightarrow x=y§?",
            "options": ["§x\\neq y\\Rightarrow f(x)\\neq f(y)§", "§x=y\\Rightarrow f(x)=f(y)§", "§f(x)\\neq f(y)\\Rightarrow x\\neq y§"], "correct": 0,
            "why": "Obmena implikácie: z §p\\Rightarrow q§ je §\\neg q\\Rightarrow\\neg p§ (Poznámka 5.5).",
            "wrong": {"1": "To je obrátená implikácia, platí pre každé zobrazenie. Injektivitu nevyjadruje.",
                      "2": "Aj to platí pre každé zobrazenie. Je to obmena obrátenej implikácie, nie injektivita."},
        },
    },
    {
        "id": "dml-mz-poradie", "lesson": 5, "rec": P5, "lecture": "5. přednáška", "from": 3813.4, "to": 3872.9, "warning": True,
        "why": "V §(f\\circ g)(a)=f(g(a))§ ide prvé §g§, hoci je napísané vpravo. Zápis sleduje poradie v §f(g(a))§, nie šípky na obrázku.",
        "before": "Prečo sa zloženie, v ktorom ide prvé §g§, píše §f\\circ g§?",
        "task": {
            "q": "Pre §g:A\\to B§ a §f:B\\to C§ je §(f\\circ g)(a)=§",
            "options": ["§f(g(a))§", "§g(f(a))§", "§f(a)\\circ g(a)§"], "correct": 0,
            "why": "Najprv §g§, potom §f§: §f(g(a))§.",
            "wrong": {"1": "Opačné poradie, to nie je §f\\circ g§. §f(a)§ pre §a\\in A§ navyše nemusí byť ani definované.",
                      "2": "Skladajú sa zobrazenia, nie hodnoty. §f(a)§ pre §a\\in A§ nemusí existovať."},
        },
    },
    {
        "id": "dml-mz-mohutnost", "lesson": 6, "rec": P6, "lecture": "6. přednáška", "from": 353.3, "to": 413.8, "warning": False,
        "why": "Počítanie je bijekcia, prsty na študentov. Rovnaká mohutnosť znamená bijekciu, menšia alebo rovná prosté zobrazenie.",
        "before": "Čo znamená, že dve množiny sú rovnako veľké?",
        "task": {
            "q": "Päť prstov a šesť študentov. Čo existuje?",
            "options": ["Prosté zobrazenie z prstov do študentov, nie bijekcia", "Bijekcia medzi prstami a študentmi", "Prosté zobrazenie zo študentov do prstov"], "correct": 0,
            "why": "Prosté áno, bijekcia nie, lebo §5<6§ (Pozorovanie 5.2).",
            "wrong": {"1": "Bijekcia chce rovnaký počet prvkov. Jeden študent ostane bez prsta.",
                      "2": "Šesť študentov na päť prstov: dvaja dostanú ten istý prst."},
        },
    },
]


def captions(uid: str, cache: dict) -> list:
    if uid not in cache:
        with urllib.request.urlopen(API + urllib.parse.quote(uid), timeout=60) as r:
            cache[uid] = json.load(r)["cues"]
    return cache[uid]


def main() -> None:
    cache: dict = {}
    out = []
    for c in CLIPS:
        t0, t1 = c["from"], c["to"]
        cues = []
        for cue in captions(c["rec"], cache):
            words = cue[3] or []  # [t0, t1, text, [[t0, t1, word], ...]]
            if isinstance(words, str):
                words = json.loads(words)
            inside = [w for w in words if w[0] >= t0 - 0.05 and w[1] <= t1 + 0.4]
            if not inside:
                continue
            ws = [{"t0": round(max(0.0, w[0] - t0), 2), "t1": round(w[1] - t0, 2), "w": w[2]} for w in inside]
            cues.append({"t0": ws[0]["t0"], "t1": ws[-1]["t1"], "words": ws})
        out.append({**{k: v for k, v in c.items()}, "course": "dml", "cues": cues})
    dest = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files/clips-dml-mz.json"
    dest.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{len(out)} clips -> {dest}")
    for o in out:
        print(o["id"], round(o["to"] - o["from"], 1), "s,", sum(len(c["words"]) for c in o["cues"]), "words,", " ".join(w["w"] for c in o["cues"][:1] for w in c["words"])[:80])


if __name__ == "__main__":
    main()

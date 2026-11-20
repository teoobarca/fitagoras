#!/usr/bin/env python3
"""Builds shared/src/commonMain/composeResources/files/clips-la1-k3.json and clips-la1-k4.json (LA1 kapitola 3 and 4) from the picked lecture moments.

Input: the candidate list from the caption search (la1-kap3/clips-candidates.json, outside the repository). Captions come
from the captions endpoint of the app's server (/api/captions/<id>), cut to the clip and shifted so that 0 is the clip's
start. Nothing is typed by hand: the words and their times are Whisper's. Each quote of a candidate must be inside its cut.
CAPTIONS_API (required) is the captions endpoint of the app's server, the URL that ends in /api/captions/; the
script appends the recording's id to it.

    CAPTIONS_API=<server>/api/captions/ python3 scripts/clips_la1_k3.py <candidates.json>
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
TEACHER = {
    "3675636330@sirius.fit.cvut.cz": "Klouda", "3675636331@sirius.fit.cvut.cz": "Klouda",
    "3675636465@sirius.fit.cvut.cz": "Krásenský", "3675636164@sirius.fit.cvut.cz": "Kleprlík",
}
# candidate id -> (topic, lesson number in the app's path order); a candidate not listed is not used
K3 = {
    "k3-hodnost-def-riadky": 1, "k3-hodnost-kroky-gem": 1, "k3-hodnost-hst": 1, "k3-hodnost-def-vs-vypocet": 1,
    "k3-hodnost-transpozicia": 2, "k3-hodnost-min-nie-rovnost": 2, "k3-hodnost-dim-obalu": 2, "k3-obal-patri-vektor": 2, "k3-obal-rovnost-dimenzie": 2,
    "k3-nasobenie-ax-stlpce": 3, "k3-nasobenie-stlpce-ab": 3, "k3-nasobenie-riadky-zlava": 3, "k3-hodnost-sucinu": 3,
    "k3-inverzia-definicia": 4, "k3-regularna-len-stvorcova": 4, "k3-inverzia-nedefinicia": 4, "k3-inverzia-jednoznacnost": 4, "k3-inverzia-sucinu-pisomka": 4,
    "k3-regularita-gem-matice": 5, "k3-regularita-kruh-implikacii": 5, "k3-regularita-hodnost-chyba": 5, "k3-regularita-inverzia-ae": 5, "k3-regularita-jedna-strana": 5,
}
K4 = {
    "k3-frobenius-pisomka": 6, "k3-frobenius-deklaruj": 6, "k3-frobenius-dokaz-hodnosti": 6, "k3-frobenius-dimenzia-s0": 6, "k3-frobenius-vedlajsie-stlpce": 6,
    "k3-riesenie-posunutie": 7, "k3-riesenie-volne-premenne": 7, "k3-riesenie-prehodenie-stlpcov": 7, "k3-riesenie-pekna-baza": 7, "k3-riesenie-iny-zapis": 7,
    "k3-varieta-definicia": 8, "k3-varieta-zamerenie": 8, "k3-varieta-lubovolny-bod": 8, "k3-varieta-dva-zapisy": 8, "k3-varieta-ako-slr": 8,
}


def captions(uid: str, cache: dict) -> list:
    if uid not in cache:
        with urllib.request.urlopen(API + urllib.parse.quote(uid), timeout=60) as r:
            cache[uid] = json.load(r)["cues"]
    return cache[uid]


def norm(s: str) -> str:
    return " ".join("".join(ch for ch in s.lower() if ch.isalnum() or ch.isspace()).split())


def build(cands: dict, pick: dict, cache: dict, rename: str | None) -> list:
    out = []
    for cid, lesson in pick.items():
        c = cands[cid]
        t0, t1 = round(c["t0"], 1), round(c["t1"], 1)  # the server cuts at 0.1 s
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
        said = norm(" ".join(w["w"] for q in cues for w in q["words"]))
        assert norm(c["quote"]) in said, f"{cid}: quote not inside the cut"
        assert 40 <= t1 - t0 <= 125, f"{cid}: {t1 - t0} s"
        task = c["taskAfter"]
        out.append({
            "id": cid.replace("k3-", rename, 1) if rename else cid, "course": "la1", "lesson": lesson, "rec": c["rec"],
            "lecture": c["lecture"], "teacher": TEACHER[c["rec"]], "from": t0, "to": t1,
            "why": c["why"], "before": c["questionBefore"], "warning": bool(c.get("warning")),
            "task": {"q": task["q"], "options": task["options"], "correct": task["correct"], "why": task["why"],
                     "wrong": {str(k): v for k, v in task["wrong"].items()}},
            "cues": cues,
        })
    return out


def main() -> None:
    cands = {c["id"]: c for c in json.loads(Path(sys.argv[1]).read_text())}
    cache: dict = {}
    assets = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files"
    for name, pick, rename in (("clips-la1-k3.json", K3, None), ("clips-la1-k4.json", K4, "k4-")):
        out = build(cands, pick, cache, rename)
        (assets / name).write_text(json.dumps(out, ensure_ascii=False, indent=1))
        print(f"{len(out)} clips -> {name}")
        for o in out:
            print(" ", o["id"], o["teacher"], round(o["to"] - o["from"]), "s,", sum(len(c["words"]) for c in o["cues"]), "words")


if __name__ == "__main__":
    main()

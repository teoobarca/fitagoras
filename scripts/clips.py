#!/usr/bin/env python3
"""Builds shared/src/commonMain/composeResources/files/clips.json from the picked lecture moments and the real captions.

Input: the candidate list from the caption search (clips-candidates.json, outside the repository) and the picked
indexes below. Captions come from the captions endpoint of the app's server (/api/captions/<id>), cut to the clip and shifted so that
0 is the clip's start. Nothing is typed by hand: the words and their times are Whisper's.
CAPTIONS_API (required) is the captions endpoint of the app's server, the URL that ends in /api/captions/; the
script appends the recording's id to it.

    CAPTIONS_API=<server>/api/captions/ python3 scripts/clips.py <candidates.json>
"""

import json
import os
import re
import sys
import urllib.parse
import urllib.request
from pathlib import Path

API = os.environ.get("CAPTIONS_API", "").rstrip("/") + "/"
if API == "/":
    sys.exit("Set CAPTIONS_API to the captions endpoint of the app's server (the URL that ends in /api/captions/).")
# index in the candidate list -> stable clip id used by the lessons
PICK = {
    20: "l1-tn", 0: "l2-def", 18: "l2-prienik", 19: "l3-trivialna", 1: "l4-pisomka", 9: "l4-subor", 2: "l4-bez-pocitania",
    8: "l5-vlastne", 16: "l5-pridanie", 21: "l5-predlzenie", 6: "l6-baza", 15: "l6-generuje", 12: "l7-def", 7: "l7-jedna",
    13: "l8-istota", 5: "l8-tip", 3: "l9-jednoznacnost", 4: "l9-dokaz", 17: "l9-standardna",
}


# What each wrong option of a task gets wrong (checked against the textbook; shown after a wrong pick, before "why").
WRONG = {
    "l1-tn": {1: "To by bol jeden skalár, nie vektor z §T^n§. Súčet dvoch §n§-tíc je podľa Definície 2.2 opäť §n§-tica.",
              2: "Definícia 2.2 sčítava každú zložku: §i§-tá zložka súčtu je §x_i+y_i§ pre každé §i§, nielen pre §i=1§."},
    "l2-def": {1: "Uzavretosť sama nestačí: spĺňa ju aj prázdna množina, a tá §\\theta§ neobsahuje (Pozorovanie 2.8).",
               2: "Nulový vektor je nutná podmienka, nie dostatočná: §\\{(x,y)\\mid |x|=|y|\\}§ ho obsahuje, a predsa nie je uzavretá na sčítanie (Príklad 2.12)."},
    "l2-prienik": {1: "Zjednotenie nie je uzavreté na sčítanie: súčet nenulového vektora z jednej priamky a nenulového z druhej neleží na žiadnej z nich.",
                   2: "Prienik podpriestorov je vždy podpriestor (Veta 2.18). Tu je to §\\{\\theta\\}§, triviálny podpriestor."},
    "l3-trivialna": {1: "To je naopak netriviálna kombinácia: stačí jeden nenulový koeficient, aby triviálna nebola.",
                     2: "Triviálna neznamená „najjednoduchšia“. Koeficienty §1§ dajú súčet všetkých vektorov súboru."},
    "l4-pisomka": {0: "Matica bez vysvetlenia nehovorí, prečo z GEM plynie LN. Treba napísať, že hľadáš koeficienty s §\\alpha_1\\mathbf{x}_1+\\dots+\\alpha_m\\mathbf{x}_m=\\theta§ a že to vedie na túto sústavu.",
                   2: "Samotná odpoveď nič nedokazuje. Body sú za postup: z definície LN zostaviť sústavu, vyriešiť ju a z riešení vyvodiť záver."},
    "l4-subor": {1: "„Nezávislý na“ je vzťah dvoch vektorov. Definícia 2.23 však hovorí o súbore §(\\mathbf{u},\\mathbf{v})§ ako celku.",
                 2: "LN nemá stupne: súbor buď je LN, alebo nie je. A je to vlastnosť súboru, nie jedného vektora."},
    "l4-bez-pocitania": {1: "Rovnosť §\\mathbf{x}_3=\\mathbf{x}_1+3\\mathbf{x}_2§ už je po presunutí na jednu stranu netriviálna kombinácia rovná §\\theta§. GEM by našiel len to, čo už vidíš.",
                         2: "Opak: §\\mathbf{x}_1+3\\mathbf{x}_2-\\mathbf{x}_3=\\theta§ je kombinácia s koeficientom §-1\\neq 0§, teda netriviálna."},
    "l5-vlastne": {1: "Obal obsahuje všetky kombinácie, aj tie s nulovými koeficientmi pri ostatných vektoroch, takže každý vektor súboru v ňom leží (Pozorovanie 2.34).",
                   2: "Koeficienty §1§ pri všetkých dajú §\\mathbf{x}_1+\\dots+\\mathbf{x}_m§, a to je vo všeobecnosti iný vektor než §\\mathbf{x}_1§."},
    "l5-pridanie": {1: "Nový smer nepribudne: §\\mathbf{z}§ je kombináciou §\\mathbf{x}_1,\\dots,\\mathbf{x}_m§, takže každá kombinácia so §\\mathbf{z}§ sa dá prepísať bez neho (Pozorovanie 2.34).",
                    2: "Obal sa pridaním §\\mathbf{z}§ nezmení, celý priestor teda dostaneš, len ak ho generoval už pôvodný súbor."},
    "l5-predlzenie": {1: "LZ by vyšlo, keby §\\mathbf{y}§ v obale ležal. Mimo obalu sa netriviálna kombinácia rovná §\\theta§ zostaviť nedá (Veta 2.42).",
                      2: "Určiť sa to dá: LN súbor rozšírený o vektor mimo svojho obalu je LN (Veta 2.42)."},
    "l6-baza": {1: "Toto je úloha druhej podmienky bázy, generovania. Otázka sa pýta na LN.",
                2: "Báza nulový vektor neobsahuje nikdy: súbor s §\\theta§ je LZ, a báza musí byť LN."},
    "l6-generuje": {1: "Nejaký vektor nakombinuje každý súbor, aspoň §\\theta§. Definícia 2.43 chce všetky vektory priestoru.",
                    2: "Nulový vektor sa dá nakombinovať z každého súboru, triviálnou kombináciou. O generovaní to nehovorí nič."},
    "l7-def": {1: "V §\\mathbb{R}^2§ (dimenzia §2§) je §((1,2),(2,4))§ súbor dĺžky §2§, a je LZ. Definícia 2.49 chce existenciu jedného LN súboru dĺžky §d§ a LZ každého dlhšieho.",
               2: "Podpriestor nad §\\mathbb{R}§ okrem §\\{\\theta\\}§ obsahuje nekonečne veľa vektorov. Dimenzia hovorí o dĺžke LN súborov, nie o počte prvkov."},
    "l7-jedna": {1: "Overiť to navyše nie je chyba, ale je to zbytočné: pri súbore dĺžky §\\dim P§ to z LN vyplýva (Veta 2.55).",
                 2: "Štandardná báza s tým nesúvisí a v podpriestore ani nemusí ležať. Stačí LN a správny počet vektorov (Veta 2.55)."},
    "l8-istota": {1: "Nulové vektory nepridajú žiadny smer: obal sa nezmení a súbor s §\\theta§ je LZ.",
                  2: "Daný súbor je už LN, škrtaním sa nič nezlepší. Na bázu vektory chýbajú, treba pridávať, nie uberať."},
    "l8-tip": {1: "Overenie GEMom Klouda naopak chce: tip je legitímny práve vtedy, keď ho overíš.",
               2: "Pridať celú štandardnú bázu a vybrať hlavné stĺpce je istý postup, len pracnejší. Zakázaný nie je."},
    "l9-jednoznacnost": {0: "Generujúci súbor môže vektor vyjadriť viacerými spôsobmi: v §((1,0),(0,1),(1,1))§ je §(1,1)=1(1,1)=1(1,0)+1(0,1)§.",
                         2: "Báza štandardné vektory obsahovať nemusí: §((1,1),(1,-1))§ je báza §\\mathbb{R}^2§ bez nich."},
    "l9-dokaz": {1: "Nulový vektor sa dá nakombinovať vždy, triviálne. Rozdiel dvoch vyjadrení dá navyše netriviálnu kombináciu rovnú §\\theta§.",
                 2: "Súvisí: odčítaním dvoch vyjadrení vznikne kombinácia rovná §\\theta§ s koeficientmi §\\alpha_i-\\beta_i§, ktoré nie sú všetky nulové."},
    "l9-standardna": {1: "Súradnice idú v poradí vektorov bázy: §4\\mathbf{e}_1-3\\mathbf{e}_2=(4,-3)§, kým §-3\\mathbf{e}_1+4\\mathbf{e}_2=(-3,4)§.",
                      2: "Báza je LN, preto sú súradnice vždy jednoznačné (Veta 2.66). V štandardnej báze sú to rovno zložky vektora."},
}


def captions(uid: str, cache: dict) -> list:
    if uid not in cache:
        with urllib.request.urlopen(API + urllib.parse.quote(uid), timeout=60) as r:
            cache[uid] = json.load(r)["cues"]
    return cache[uid]


def math(t: str) -> str:
    """Question and task text only (never captions): x1, xm, R2, Z5 written as formulas."""
    t = re.sub(r"\b([xyzuvw])(\d|m)\b", lambda m: f"§\\mathbf{{{m.group(1)}}}_{m.group(2)}§", t)
    t = re.sub(r"\bR(\d)\b", r"§\\mathbb{R}^\1§", t)
    t = re.sub(r"\bZ(\d)\b", r"§\\mathbb{Z}_\1§", t)
    t = t.replace("§, ..., §", ",\\dots,").replace("⟨§", "§\\langle ").replace("§⟩", "\\rangle§")
    return t


def main() -> None:
    cands = json.loads(Path(sys.argv[1]).read_text())
    cache: dict = {}
    out = []
    for idx, cid in PICK.items():
        c = cands[idx]
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
        task = c["taskAfter"]
        out.append({
            "id": cid, "lesson": c["lesson"], "rec": c["rec"], "lecture": c["lecture"], "from": round(t0, 1), "to": round(t1, 1),
            "why": c["why"], "before": math(c["questionBefore"]), "warning": bool(c.get("warning")),
            "task": {"q": math(task["q"]), "options": [math(o) for o in task["options"]], "correct": task["correct"], "why": math(task["why"])},
            "cues": cues,
        })
    for o in out:  # two strings the regex can't get right: z is a vector in one clip and scalars in another
        if o["id"] == "l5-pridanie":
            o["task"]["q"] = "Vektor §\\mathbf{z}§ patrí do §\\langle\\mathbf{x}_1,\\dots,\\mathbf{x}_m\\rangle§. Aký je §\\langle\\mathbf{x}_1,\\dots,\\mathbf{x}_m,\\mathbf{z}\\rangle§?"
        if o["id"] == "l6-generuje":
            o["task"]["options"] = [x.replace("§\\mathbf{z}_1§, §\\mathbf{z}_2§, §\\mathbf{z}_3§", "§z_1,z_2,z_3§") for x in o["task"]["options"]]
        # corrections from the independent content check (10. 11.)
        if o["id"] == "l2-def":
            o["task"]["options"][1] = "Iba uzavretosť na sčítanie aj násobenie číslom"
            o["task"]["why"] = "Treba aj neprázdnosť: prázdna množina je uzavretá „zadarmo“. Nulový vektor je potom dôsledok, do definície ho netreba písať."
        if o["id"] == "l6-generuje":
            o["task"]["options"][1] = "Nájdem jeden vektor, ktorý sa z neho dá nakombinovať"
        if o["id"] == "l7-def":
            o["task"]["options"][0] = "Pre §d\\geq 1§: v P existuje LN súbor dĺžky d a každý dlhší je LZ"
        if o["id"] == "l8-tip":
            o["task"]["options"][1] = "Neoveruj tip GEMom"
    for o in out:
        o["task"]["wrong"] = {str(k): v for k, v in WRONG[o["id"]].items()}
    dest = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files/clips.json"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(out, ensure_ascii=False, indent=1))
    print(f"{len(out)} clips -> {dest}")
    for o in out:
        print(o["id"], o["to"] - o["from"], "s,", sum(len(c["words"]) for c in o["cues"]), "words")


if __name__ == "__main__":
    main()

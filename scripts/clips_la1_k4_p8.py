#!/usr/bin/env python3
"""Adds or replaces clips of LA1 kapitola 4 in clips-la1-k4.json from picked lecture moments (Klouda, 8. prednáška, 18. 11. 2026).

Input: a candidates file in the format of scripts/clips_la1_k3.py (id, rec, lecture, t0, t1, quote, why, questionBefore,
taskAfter) with "lesson" (the lesson number in the app's path order), and the captions of each recording as saved from
/api/captions/<uid> (one JSON file per uid, named <uid>.json, in the captions folder). Nothing is typed by hand: the words
and their times are Whisper's, cut to the clip and shifted so that 0 is its start. Each quote must be inside its cut.

    python3 scripts/clips_la1_k4_p8.py <candidates.json> <captions folder>
"""

import json
import sys
from pathlib import Path

TEACHER = {"3675636332@sirius.fit.cvut.cz": "Klouda", "3675636331@sirius.fit.cvut.cz": "Klouda",
           "3675636465@sirius.fit.cvut.cz": "Krásenský", "3675636164@sirius.fit.cvut.cz": "Kleprlík"}


def norm(s: str) -> str:
    return " ".join("".join(ch for ch in s.lower() if ch.isalnum() or ch.isspace()).split())


def clip(c: dict, cues_all: list) -> dict:
    t0, t1 = round(c["t0"], 1), round(c["t1"], 1)
    cues = []
    for cue in cues_all:
        words = cue[3] or []
        if isinstance(words, str):
            words = json.loads(words)
        inside = [w for w in words if w[0] >= t0 - 0.05 and w[1] <= t1 + 0.4]
        if not inside:
            continue
        ws = [{"t0": round(max(0.0, w[0] - t0), 2), "t1": round(w[1] - t0, 2), "w": w[2]} for w in inside]
        cues.append({"t0": ws[0]["t0"], "t1": ws[-1]["t1"], "words": ws})
    said = norm(" ".join(w["w"] for q in cues for w in q["words"]))
    assert norm(c["quote"]) in said, f"{c['id']}: quote not inside the cut"
    assert 40 <= t1 - t0 <= 125, f"{c['id']}: {t1 - t0} s"
    task = c["taskAfter"]
    return {
        "id": c["id"], "course": "la1", "lesson": c["lesson"], "rec": c["rec"], "lecture": c["lecture"],
        "teacher": TEACHER[c["rec"]], "from": t0, "to": t1, "why": c["why"], "before": c["questionBefore"],
        "warning": bool(c.get("warning")),
        "task": {"q": task["q"], "options": task["options"], "correct": task["correct"], "why": task["why"],
                 "wrong": {str(k): v for k, v in task["wrong"].items()}},
        "cues": cues,
    }


def main() -> None:
    cands = json.loads(Path(sys.argv[1]).read_text())
    caps = Path(sys.argv[2])
    target = Path(__file__).resolve().parent.parent / "shared/src/commonMain/composeResources/files/clips-la1-k4.json"
    clips = json.loads(target.read_text())
    for c in cands:
        new = clip(c, json.loads((caps / f"{c['rec']}.json").read_text())["cues"])
        at = next((i for i, o in enumerate(clips) if o["id"] == new["id"]), None)
        if at is None:
            clips.append(new)
        else:
            clips[at] = new
        print(new["id"], new["teacher"], round(new["to"] - new["from"]), "s,", sum(len(q["words"]) for q in new["cues"]), "words")
    target.write_text(json.dumps(clips, ensure_ascii=False, indent=1))


if __name__ == "__main__":
    main()

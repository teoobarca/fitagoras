"""Report LLM kontrol: zlúči behy/*/vysledok.json z scripts/llm-kontrola.sh do report.md, nalezy.json a nalezy.tsv.

python3 -I scripts/llm-kontrola-report.py <OUT>

Ak v OUT je presnost.md (overenie vzorky nálezov človekom alebo iným modelom), vloží sa do reportu za súhrn.
"""
import json
import os
import sys
from collections import Counter, defaultdict
from datetime import date

out = sys.argv[1]
runs = os.path.join(out, "behy")
exp = os.path.join(out, "export")


def read(p, default=""):
    try:
        with open(p, encoding="utf-8") as f:
            return f.read().strip()
    except OSError:
        return default


SEV = {"P1": 0, "P2": 1, "P3": 2}
findings, failed, done, secs = [], [], [], 0
for name in sorted(os.listdir(runs)) if os.path.isdir(runs) else []:
    d = os.path.join(runs, name)
    if not os.path.isdir(d):
        continue
    s = read(os.path.join(d, "sekundy"))
    secs += int(s) if s.isdigit() else 0
    raw = read(os.path.join(d, "vysledok.json"))
    if not raw:
        failed.append(name)
        continue
    try:
        r = json.loads(raw)
    except json.JSONDecodeError:
        failed.append(name + " (neplatný JSON)")
        continue
    done.append((name, r.get("prezrete", "")))
    chapter = "-".join(name.split("-")[:2])
    for f in r.get("nalezy", []):
        f["kapitola"] = chapter
        f["beh"] = name
        findings.append(f)

findings.sort(key=lambda f: (f["kapitola"], SEV.get(f["zavaznost"], 3), f["kontrola"], f["miesto"]))
per_chapter = defaultdict(int)
for f in findings:
    per_chapter[f["kapitola"]] += 1
    f["id"] = f"{f['kapitola']}-{per_chapter[f['kapitola']]}"

with open(os.path.join(out, "nalezy.json"), "w", encoding="utf-8") as fh:
    json.dump(findings, fh, ensure_ascii=False, indent=1)
with open(os.path.join(out, "nalezy.tsv"), "w", encoding="utf-8") as fh:
    cols = ["id", "zavaznost", "istota", "kontrola", "kapitola", "miesto", "subor", "citat", "preco"]
    fh.write("\t".join(cols) + "\n")
    for f in findings:
        fh.write("\t".join(str(f.get(c, "")).replace("\t", " ").replace("\n", " ") for c in cols) + "\n")

chapters = sorted({f["kapitola"] for f in findings} | {"-".join(n.split("-")[:2]) for n, _ in done})
checks = sorted({f["kontrola"] for f in findings})
by = Counter((f["kontrola"], f["kapitola"]) for f in findings)
sev = Counter(f["zavaznost"] for f in findings)
wall = read(os.path.join(out, "trvanie")).splitlines()

L = []
L.append(f"# LLM kontrola obsahu FITagoras, {date.today().isoformat()}")
L.append("")
L.append(f"Verzia {read(os.path.join(exp, 'verzia'), '?')}, commit `{read(os.path.join(exp, 'commit'), '?')}`. "
         f"Behov {len(done)} hotových, {len(failed)} zlyhaných; codex spolu {secs // 60} min, "
         f"celý beh skriptu {int(wall[-1]) // 60 if wall and wall[-1].isdigit() else '?'} min.")
L.append(f"Nálezov **{len(findings)}**: P1 {sev['P1']}, P2 {sev['P2']}, P3 {sev['P3']}. "
         "Je to zoznam práce pre audit, nie verdikt: každý nález pred opravou over (docs/ZASADY.md, postup tvorby, krok 5).")
L.append("")
pres = read(os.path.join(out, "presnost.md"))
if pres:
    L.append(pres)
    L.append("")
L.append("## Počty podľa kontroly a kapitoly")
L.append("")
L.append("| kontrola | " + " | ".join(chapters) + " | spolu |")
L.append("|---|" + "---|" * (len(chapters) + 1))
for c in checks:
    L.append(f"| {c} | " + " | ".join(str(by[(c, k)] or "") for k in chapters) + f" | {sum(by[(c, k)] for k in chapters)} |")
L.append("| **spolu** | " + " | ".join(str(per_chapter[k]) for k in chapters) + f" | {len(findings)} |")
L.append("")
if failed:
    L.append("## Zlyhané behy")
    L.append("")
    L.extend(f"- `{n}`: log v `behy/{n.split(' ')[0]}/log.txt`" for n in failed)
    L.append("")


def one(f):
    where = f"`{f['miesto']}`" + (f" · `{f['subor']}`" if f.get("subor") else "")
    cit = f["citat"].replace("\n", " ")
    return [f"- **{f['id']}** [{f['zavaznost']}, istota {f['istota']}] **{f['kontrola']}** · {where}",
            f"  - „{cit}“", f"  - {f['preco']}"]


for k in chapters:
    fs = [f for f in findings if f["kapitola"] == k]
    L.append(f"## {k} ({len(fs)})")
    L.append("")
    for f in fs:
        L.extend(one(f))
    L.append("")
L.append("## Čo behy prešli")
L.append("")
L.extend(f"- `{n}`: {p}" for n, p in done)
with open(os.path.join(out, "report.md"), "w", encoding="utf-8") as fh:
    fh.write("\n".join(L) + "\n")
print(f"{len(findings)} nálezov, {len(done)} behov, {len(failed)} zlyhaných")

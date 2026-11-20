#!/usr/bin/env python3
"""Kontrola pred zverejnením: nič súkromné ani tajné nesmie odísť na GitHub.

Vzory sú v sukromne-vzory.txt a tajomstva-vzory.txt vedľa tohto súboru (tie isté číta NoPrivateServerNameTest
a NoSecretsTrackedTest), navyše súkromný zoznam autora mimo repozitára ($FITAGORAS_SUKROMNE_VZORY, predvolene
~/.fitagoras/sukromne-vzory.txt), ak existuje. Návod: CONTRIBUTING.md, „Vývoj vo verejnom repozitári“.

  python3 scripts/verejne/kontrola.py strom [PRIEČINOK]   sledované súbory checkoutu (alebo všetky súbory priečinka,
                                                          ak nie je v gite)
  python3 scripts/verejne/kontrola.py rozsah OD..DO        commity v rozsahu: správy, autori, mená a obsah zmenených súborov
  python3 scripts/verejne/kontrola.py pre-push REMOTE URL  ako git pre-push hook (riadky refov na stdin): pushuje sa len
                                                          main a každý nový commit prejde ako pri `rozsah`

Návratový kód 0 = čisté, 1 = nález (vypíše súbor, riadok a vzor), 2 = zlé použitie.
"""
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
PATTERN_FILES = ("sukromne-vzory.txt", "tajomstva-vzory.txt")
# the pattern lists themselves are the only files allowed to hold the patterns
SELF = {f"scripts/verejne/{f}" for f in PATTERN_FILES}
# file names that must never be tracked (the same rule as NoSecretsTrackedTest)
FORBIDDEN_PATH = re.compile(r"(^|/)(signing|keystore\.properties|secrets\.properties)$|(^|/)signing/|\.(jks|keystore|p12)$")
ZERO = "0" * 40
ALLOWED_REFS = {"refs/heads/main"}
AUTHOR_EMAIL = re.compile(r"@users\.noreply\.github\.com$")


# the author's own list of private names (machines, servers, tools): outside the repository, since naming them here
# would publish them; read in addition when present
PRIVATE_FILE = os.environ.get("FITAGORAS_SUKROMNE_VZORY") or os.path.expanduser("~/.fitagoras/sukromne-vzory.txt")


def load_patterns():
    out = []
    files = [os.path.join(HERE, name) for name in PATTERN_FILES]
    if os.path.isfile(PRIVATE_FILE):
        files.append(PRIVATE_FILE)
    for path in files:
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.rstrip("\n")
                if line.strip() and not line.lstrip().startswith("#"):
                    out.append((os.path.basename(path), line, re.compile(line, re.MULTILINE)))
    if not out:
        sys.exit("kontrola: žiadne vzory")
    return out


PATTERNS = load_patterns()


def scan_bytes(label, data, findings):
    # text as UTF-8, and the same bytes as Latin-1 so strings inside binaries (PNG text chunks, MP4 tags) count too
    texts = [data.decode("utf-8", "replace")]
    if b"\0" in data[:8000] or not texts[0].isascii():
        texts.append(data.decode("latin-1"))
    seen = set()
    for text in texts:
        for src, raw, rx in PATTERNS:
            for m in rx.finditer(text):
                line = text.count("\n", 0, m.start()) + 1
                key = (raw, line)
                if key in seen:
                    continue
                seen.add(key)
                snippet = m.group(0)
                findings.append(f"{label}:{line}: {snippet[:80]!r} ({src}: {raw})")


def scan_path(path, findings, label=None):
    label = label or path
    if FORBIDDEN_PATH.search(path):
        findings.append(f"{label}: súbor s kľúčom alebo tajomstvom sa nesmie sledovať")


def git(*args, cwd=None, binary=False):
    r = subprocess.run(["git", *args], cwd=cwd, capture_output=True)
    if r.returncode != 0:
        raise RuntimeError(f"git {' '.join(args)}: {r.stderr.decode(errors='replace').strip()}")
    return r.stdout if binary else r.stdout.decode("utf-8", "replace")


def cmd_tree(root):
    root = os.path.abspath(root)
    findings = []
    try:
        top = git("rev-parse", "--show-toplevel", cwd=root).strip()
        files = [f for f in git("ls-files", "-z", cwd=top).split("\0") if f]
        base = top
    except (RuntimeError, FileNotFoundError):
        base = root
        files = []
        for d, dirs, names in os.walk(root):
            dirs[:] = [x for x in dirs if x != ".git"]
            files += [os.path.relpath(os.path.join(d, n), root) for n in names]
    for rel in sorted(files):
        scan_path(rel, findings)
        scan_bytes(rel, rel.encode(), findings)  # the path itself
        full = os.path.join(base, rel)
        if rel in SELF or not os.path.isfile(full) or os.path.islink(full):
            continue
        with open(full, "rb") as f:
            scan_bytes(rel, f.read(), findings)
    return report(findings, f"{len(files)} súborov v {base}")


def commits_in(rng):
    return [c for c in git("rev-list", "--reverse", rng).split() if c]


def check_commits(commits, findings):
    for c in commits:
        short = c[:10]
        meta = git("show", "-s", "--format=%an%n%ae%n%cn%n%ce", c).splitlines()
        an, ae, cn, ce = (meta + ["", "", "", ""])[:4]
        for email in sorted({ae, ce}):
            if not AUTHOR_EMAIL.search(email):
                findings.append(f"commit {short}: e-mail autora/commitera {email!r} nie je GitHub noreply adresa")
        scan_bytes(f"commit {short} (správa a autor)", git("show", "-s", "--format=%an <%ae>%n%cn <%ce>%n%B", c).encode(), findings)
        out = git("diff-tree", "-r", "--root", "--no-commit-id", "--no-renames", "-z", c, binary=True).split(b"\0")
        # -z output: ":<modes> <shas> <status>\0<path>\0" pairs
        i = 0
        while i + 1 < len(out):
            head, path = out[i].decode(), out[i + 1].decode("utf-8", "replace")
            i += 2
            if not head.startswith(":"):
                continue
            parts = head[1:].split()
            new_mode, new_sha, status = parts[1], parts[3], parts[4]
            label = f"commit {short} {path}"
            scan_path(path, findings, label)
            scan_bytes(label, path.encode(), findings)
            if status.startswith("D") or new_sha == ZERO or path in SELF or new_mode == "160000":
                continue
            scan_bytes(label, git("cat-file", "blob", new_sha, binary=True), findings)


def cmd_range(rng):
    findings = []
    commits = commits_in(rng)
    check_commits(commits, findings)
    return report(findings, f"{len(commits)} commitov v {rng}")


def cmd_pre_push(remote):
    findings = []
    n = 0
    for line in sys.stdin:
        parts = line.split()
        if len(parts) != 4:
            continue
        local_ref, local_sha, remote_ref, remote_sha = parts
        if local_sha == ZERO:
            continue  # deleting a remote ref sends nothing
        if remote_ref not in ALLOWED_REFS:
            findings.append(f"{remote_ref}: na GitHub sa pushuje len main (pracovné vetvy ostávajú lokálne)")
            continue
        if remote_sha != ZERO and _exists(remote_sha):
            commits = commits_in(f"{remote_sha}..{local_sha}")
        else:
            # a new remote ref (or one we don't have): every commit not yet on the remote
            commits = [c for c in git("rev-list", "--reverse", local_sha, "--not", f"--remotes={remote}").split() if c]
        n += len(commits)
        check_commits(commits, findings)
    return report(findings, f"{n} nových commitov pre {remote}")


def _exists(sha):
    return subprocess.run(["git", "cat-file", "-e", sha], capture_output=True).returncode == 0


def report(findings, what):
    if findings:
        print(f"kontrola zverejnenia: {len(findings)} nálezov ({what})", file=sys.stderr)
        for f in findings[:200]:
            print("  " + f, file=sys.stderr)
        if len(findings) > 200:
            print(f"  … a ďalších {len(findings) - 200}", file=sys.stderr)
        print("Oprav to (alebo vzor v scripts/verejne/, ak je nález neškodný) a skús znova.", file=sys.stderr)
        return 1
    extra = "" if os.path.isfile(PRIVATE_FILE) else "; bez súkromného zoznamu vzorov"
    print(f"kontrola zverejnenia: čisté ({what}{extra})")
    return 0


def main(argv):
    if len(argv) >= 2 and argv[1] == "strom":
        return cmd_tree(argv[2] if len(argv) > 2 else ".")
    if len(argv) == 3 and argv[1] == "rozsah":
        return cmd_range(argv[2])
    if len(argv) >= 3 and argv[1] == "pre-push":
        return cmd_pre_push(argv[2])
    print(__doc__, file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv))

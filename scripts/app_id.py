#!/usr/bin/env python3
"""The app's package name, from one place: the built APK (aapt2 dump badging) when given, else applicationId in
app/build.gradle.kts. Every emulator script asks this instead of writing the id (it changed in 1.3.0 and four
sessions failed on a hard-coded one; RepoLintTest fails on a literal id in scripts).

    python3 scripts/app_id.py                 # from build.gradle.kts
    python3 scripts/app_id.py --apk app.apk   # from an APK (debug, release, or one downloaded)
    PKG=$(python3 scripts/app_id.py)          # in shell scripts
"""
import glob
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def from_apk(apk: str) -> str:
    tools = sorted(glob.glob(os.path.expanduser("~/Library/Android/sdk/build-tools/*/aapt2")))
    out = subprocess.run([tools[-1], "dump", "badging", apk], capture_output=True, text=True, check=True).stdout
    return re.search(r"package: name='([^']+)'", out).group(1)


def from_gradle() -> str:
    return re.search(r'applicationId = "([^"]+)"', (ROOT / "app/build.gradle.kts").read_text()).group(1)


def package(apk: str | None = None) -> str:
    return from_apk(apk) if apk else from_gradle()


if __name__ == "__main__":
    args = sys.argv[1:]
    print(package(args[1] if args[:1] == ["--apk"] and len(args) > 1 else None))

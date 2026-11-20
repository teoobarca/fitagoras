#!/usr/bin/env python3
"""The app's sounds from their originals: one format, the silence before them cut, loudness levelled by role.

usage: scripts/zvuky.py <folder with the originals> [--out app/src/main/assets/zvuky]

Every role (tap.wav, button.wav, correct.wav, ...) is read from <role>.<any extension> in the folder, cut where the
sound starts (-45 dBFS; a lead-in in LEAD too) and after it ends, faded out over 15 ms, mono 44.1 kHz, levelled so that its loudest 100 ms
reach the role's target (TARGET, dBFS RMS) with the peak at most -1 dBFS, and written as 16-bit WAV (nothing to
decode: SoundPool has it ready at once). The app plays
the files by role (shared/.../ui/Sound.kt), so a sound is replaced by replacing its file and running this again.
Prints the table that goes into the folder's POVOD.md. Needs ffmpeg.
"""
import array, math, os, subprocess, sys, tempfile

# loudest 100 ms, dBFS RMS: the verdict loudest, nothing above "correct", a tap far below it
TARGET = {
    "correct": -15, "combo_1": -15, "combo_2": -15, "combo_3": -15, "combo_4": -15, "combo_5": -15,
    "lesson_done": -15, "streak": -16, "lesson_done_alt": -17, "correct_alt": -19, "wrong": -18, "wrong_alt": -22,
    "step_done": -20, "button": -25, "tap": -29,
}
RATE = 44100
# seconds of a sound's lead-in left out, so its accent lands when it starts: Duolingo's node_complete opens with a
# blip and 0.35 s of near silence, its chord came 0.54 s after the end screen appeared (Teo, 18. 11.: "nie je sync")
LEAD = {"lesson_done": .45}


def pcm(path):
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", path, "-ac", "1", "-ar", str(RATE), "-f", "s16le", "-"], capture_output=True, check=True).stdout
    a = array.array("h"); a.frombytes(raw); return [x / 32768 for x in a]


def db(x): return 20 * math.log10(max(x, 1e-9))


def window_rms(s, ms=100):
    n = int(RATE * ms / 1000); best = 0.0; acc = 0.0
    sq = [x * x for x in s]
    for i, v in enumerate(sq):
        acc += v
        if i >= n: acc -= sq[i - n]
        best = max(best, acc / min(i + 1, n))
    return math.sqrt(best)


def main():
    src = sys.argv[1]
    out = sys.argv[sys.argv.index("--out") + 1] if "--out" in sys.argv else os.path.join(os.path.dirname(__file__), "..", "app/src/main/assets/zvuky")
    files = {os.path.splitext(f)[0]: os.path.join(src, f) for f in os.listdir(src) if os.path.splitext(f)[0] in TARGET}
    missing = sorted(set(TARGET) - set(files))
    if missing: sys.exit("missing roles: " + ", ".join(missing))
    print("| rola | dĺžka | RMS 100 ms | špička |\n|---|---|---|---|")
    for role in TARGET:
        s = pcm(files[role])
        on = [i for i, x in enumerate(s) if abs(x) > 10 ** (-45 / 20)]
        a, b = on[0], on[-1] + 1
        s = s[a:b]
        if role in LEAD:
            s = s[int(RATE * LEAD[role]):]
            rise = int(RATE * .005)
            s = [x * min(1.0, i / rise) for i, x in enumerate(s)]
        gain = 10 ** ((TARGET[role] - db(window_rms(s))) / 20)
        peak = max(abs(x) for x in s) * gain
        if peak > 10 ** (-1 / 20): gain *= 10 ** (-1 / 20) / peak
        fade = int(RATE * .015)
        s = [x * gain * (min(1.0, (len(s) - i) / fade)) for i, x in enumerate(s)]
        with tempfile.NamedTemporaryFile(suffix=".raw", delete=False) as t:
            t.write(array.array("h", [max(-32767, min(32767, int(x * 32767))) for x in s]).tobytes()); tmp = t.name
        dst = os.path.join(out, role + ".wav")
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-f", "s16le", "-ar", str(RATE), "-ac", "1", "-i", tmp, "-c:a", "pcm_s16le", dst], check=True)
        os.unlink(tmp)
        print(f"| {role} | {len(s) / RATE * 1000:.0f} ms | {db(window_rms(s)):.1f} dB | {db(max(abs(x) for x in s)):.1f} dB |")


if __name__ == "__main__":
    main()

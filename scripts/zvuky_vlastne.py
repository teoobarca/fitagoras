#!/usr/bin/env python3
"""The app's own sound effects, synthesized: a replacement for the 15 sounds copied from other apps.

usage: scripts/zvuky_vlastne.py [--out build/zvuky-vlastne]

Each role is built from oscillators (additive partials, a triangle, a pitch glide, a chorused pad), envelopes and
band-limited noise; nothing is sampled from a recording. The parameters below come from measuring the sound Teo
picked for the role (the processed files in app/src/main/assets/zvuky, see POVOD.md there): length, attack, where
the peak is, the notes and their order, decay to -20/-40 dB, spectral centroid and noise share. The
notes are moved into one key (F major: F G A Bb C D E), so the set is one family and differs from the originals.

The raw sounds are then levelled by scripts/zvuky.py (same trim, fade, per-role loudness target, 16-bit 44.1 kHz WAV),
into --out. Swap into the app: --out app/src/main/assets/zvuky. Needs numpy and ffmpeg. Deterministic (fixed seeds).
"""
import importlib.util, math, os, sys, tempfile, wave
import numpy as np

SR = 44100

# ----------------------------------------------------------------------------------------------------------------
# Per-role design. "ref" is what was measured on the picked sound (dur and decay to -30/-20/-40 dB from the peak,
# att = time to 90 % of the loudest 10 ms frame, cen = spectral centroid at 22.05 kHz, notes = strongest partial at
# each onset). Every event: (onset ms, voice, pitch, level dB, voice parameters). Pitches are note names or Hz.
# ----------------------------------------------------------------------------------------------------------------
ROLES = {
    # ref Elevate click: 18 ms, att 0, cen 378, a dull knock with its energy 400-560 Hz, no pitch to speak of
    "tap": dict(length=22, events=[
        (0, "knock", 430, 0, dict(drop=1.12, tau=7.5, noise=-16, lp=1000, extra=[(250, -8)])),
    ]),
    # ref Elevate button_press: 49 ms, two clicks 23 ms apart (press, release); the 2nd carries G5 777 Hz and a
    # 190 Hz thump; the 1st 500 + 1400 Hz and noise 2.5-5.5 kHz; cen 1557
    "button": dict(length=52, events=[
        (0, "knock", 1420, -6, dict(drop=1.05, tau=5, noise=-4, band=(2500, 5500), extra=[(500, -4)])),
        (23, "knock", 780, 0, dict(drop=1.03, tau=8, noise=-12, band=(1800, 4500), extra=[(190, -9), (2050, -14)])),
    ]),
    # ref Elevate correct_answer: 338 ms, peak at 5 ms, C4 (+E4 -16 dB, 2nd harmonic -17 dB) then G4 at 80 ms over
    # the ringing C4; decay -20 dB at 195 ms, -40 dB at 330 ms; cen 513; tonal (flatness 0)
    "correct": dict(length=360, events=[
        (0, "mallet", "C4", 0, dict(tau=80)),
        (0, "mallet", "E4", -15, dict(tau=80)),
        (82, "mallet", "G4", -2, dict(tau=70)),
    ]),
    # ref Duolingo match_correct 1-4: 105-111 ms each, att 0, a triangle-like tone (odd harmonics -19/-30/-37 dB),
    # -20 dB at 70-75 ms; the pitch climbs C#4 F#4 A#4 C#5 (cen 370 -> 664); here a semitone lower: C4 F4 A4 C5
    "combo_1": dict(length=115, events=[(0, "tri", "C4", 0, dict(tau=32))]),
    "combo_2": dict(length=118, events=[(0, "tri", "F4", 0, dict(tau=33))]),
    "combo_3": dict(length=118, events=[(0, "tri", "A4", 0, dict(tau=33))]),
    "combo_4": dict(length=118, events=[(0, "tri", "C5", 0, dict(tau=33))]),
    # ref Duolingo match_correct_05: 732 ms, peak at 5 ms, F#4 + F#5 (-5 dB, wavering) ringing, brief sparkles up
    # to C#7 in the first 130 ms; -20 dB at 445 ms, -40 dB at 725 ms; cen 914 (the brightest of the series)
    "combo_5": dict(length=760, events=[
        (0, "tri", "F4", -2, dict(tau=180, attack=1.5)),
        (0, "bell", "F5", 0, dict(tau=240, trem=(6.5, .45))),
        (0, "ping", "C7", -16, dict(tau=30)),
        (70, "ping", "F6", -17, dict(tau=40)),
        (115, "ping", "A6", -18, dict(tau=60)),
    ]),
    # ref Drops checkpoint_success: 212 ms, att 10 ms, a near-pure C5; -20 dB at 120 ms, -40 dB at 205 ms; cen 587
    "correct_alt": dict(length=225, events=[(0, "sine", "C5", 0, dict(tau=50, attack=8))]),
    # ref Busuu wrong: 277 ms, C6 (pure) for 100 ms then C5 louder (peak at 105 ms), -40 dB 170 ms after it; cen 724
    "wrong": dict(length=285, events=[
        (0, "sine", "C6", 0, dict(tau=40, attack=4)),
        (95, "sine", "C5", 0, dict(tau=48, attack=6)),
    ]),
    # ref Duolingo wrong_answer_quiet: 223 ms, two wooden knocks, F#5 then C5 at 130 ms (louder); partials at
    # 1 : 2.05 : 5.7 : 8.5 of the pitch (wood, not a string); cen 2396, the noisiest of the set (flatness 0.019)
    "wrong_alt": dict(length=230, events=[
        (0, "wood", "F5", -4, dict(tau=40)),
        (128, "wood", "C5", 0, dict(tau=42)),
    ]),
    # ref Memrise reviewing: 149 ms, one tone gliding up ~300 -> 900 Hz with its 2nd harmonic, peak at 40-60 ms,
    # -12 dB by 100 ms; crest 2.5 (a held tone, not a hit); cen 621
    "step_done": dict(length=150, events=[(0, "glide", 300, 0, dict(to=900, attack=35, hold=55, tau=40))]),
    # ref Duolingo node_complete (lead-in cut): 876 ms; a quick F# major run with a soft thump (0-150 ms, peak 80 ms),
    # a quiet shimmering chord ~20 dB lower, a second hit at 655-720 ms (F#3 thump, F#5 C#6 A#5) ringing out to
    # A#6; cen 1069, a little noise (hi 0.04); here in F major
    "lesson_done": dict(length=900, events=[
        (0, "thump", "F3", -6, dict(tau=60)),
        (0, "bell", "F4", -2, dict(tau=70)),
        (45, "bell", "A4", -1, dict(tau=70)),
        (90, "bell", "C5", 0, dict(tau=75)),
        (135, "bell", "F5", -3, dict(tau=90)),
        (60, "shimmer", ["F4", "C5", "F5", "A5", "C6"], -20, dict(until=700, trem=(7, .5))),
        (0, "swish", None, -22, dict(band=(3000, 9000), tau=35)),
        (665, "thump", "F3", -6, dict(tau=70)),
        (665, "bell", "F5", -6, dict(tau=90)),
        (690, "bell", "C6", -4, dict(tau=80)),
        (700, "bell", "A5", -8, dict(tau=110)),
        (720, "ping", "A6", -14, dict(tau=140)),
        (660, "swish", None, -24, dict(band=(3000, 9000), tau=25)),
    ]),
    # ref Quizlet study_complete: 1519 ms, a soft synth chord rolled up C5+G5 (0), E5 (~100-200 ms), C6 (300 ms),
    # harmonic-rich up to 5 kHz with a slow vibrato, peak at 235 ms, -20 dB at 605 ms, -40 dB at 1285 ms; cen 1239
    "lesson_done_alt": dict(length=1550, events=[
        (0, "pad", "C5", -2, dict(tau=280, attack=200)),
        (0, "pad", "G5", -4, dict(tau=300, attack=200)),
        (110, "pad", "E5", -3, dict(tau=360, attack=120)),
        (290, "pad", "C6", -3, dict(tau=450, attack=60)),
    ]),
    # ref Quizlet streak_activated: 617 ms, four pure sine pings climbing A#4 A#5 D6 A#6 at 0/85/145/225 ms, all
    # ringing, the last loudest (peak at 230 ms); -20 dB at 230 ms, -40 dB at 385 ms after it; cen 1593;
    # here Bb4 F5 D6 Bb6 a touch faster
    "streak": dict(length=640, events=[
        (0, "ping", "Bb4", -6, dict(tau=200)),
        (75, "ping", "F5", -5, dict(tau=180)),
        (140, "ping", "D6", -3, dict(tau=150)),
        (210, "ping", "Bb6", 0, dict(tau=110)),
    ]),
}

# ----------------------------------------------------------------------------------------------------------------

NOTES = {"C": 0, "C#": 1, "Db": 1, "D": 2, "D#": 3, "Eb": 3, "E": 4, "F": 5, "F#": 6, "Gb": 6, "G": 7, "G#": 8,
         "Ab": 8, "A": 9, "A#": 10, "Bb": 10, "B": 11, "H": 11}


def hz(p):
    if not isinstance(p, str): return float(p)
    name, octave = p[:-1], int(p[-1])
    return 440.0 * 2 ** ((NOTES[name] + 12 * (octave + 1) - 69) / 12)


def amp(db): return 10 ** (db / 20)


def t_of(ms): return np.arange(int(SR * ms / 1000)) / SR


def ramp(x, ms):
    """raised-cosine attack, so no onset ever clicks unless a click is meant"""
    n = min(len(x), max(1, int(SR * ms / 1000)))
    x[:n] *= 0.5 - 0.5 * np.cos(np.pi * np.arange(n) / n)
    return x


def partials(f, ms, parts, tau, attack=2.0):
    """sum of sines at f * ratio, each (ratio, dB, decay scale) decaying exp(-t / (tau * scale))"""
    t = t_of(ms); y = np.zeros_like(t)
    for ratio, db, scale in parts:
        if f * ratio < SR / 2.2:
            y += amp(db) * np.sin(2 * np.pi * f * ratio * t) * np.exp(-t / (tau * scale / 1000))
    return ramp(y, attack)


def band_noise(ms, lo, hi, tau, rng):
    """white noise kept between lo and hi Hz (smooth FFT mask), decaying with tau ms"""
    n = int(SR * ms / 1000); x = rng.standard_normal(n)
    X = np.fft.rfft(x); f = np.fft.rfftfreq(n, 1 / SR)
    mask = 1 / (1 + (lo / np.maximum(f, 1)) ** 4) / (1 + (f / hi) ** 4)
    y = np.fft.irfft(X * mask, n)
    y /= np.max(np.abs(y)) or 1
    return ramp(y * np.exp(-np.arange(n) / SR / (tau / 1000)), 0.5)


def v_sine(f, ms, tau, attack=5):
    # near-pure tone: the measured refs carry only a faint 2nd harmonic (-25..-30 dB)
    return partials(f, ms, [(1, 0, 1), (2, -28, .5)], tau, attack)


def v_mallet(f, ms, tau, rng):
    # soft mallet on a tuned bar: fundamental, a quickly fading octave (ref: 2nd harmonic -17 dB) and the bar's
    # 4th partial, plus 4 ms of felt noise under 2 kHz
    y = partials(f, ms, [(1, 0, 1), (2, -17, .35), (3.98, -30, .15)], tau, 1.5)
    nz = band_noise(6, 200, 2000, 1.5, rng) * amp(-30)
    y[:len(nz)] += nz
    return y


def v_tri(f, ms, tau, attack=1.0):
    # triangle: odd harmonics at 1/n^2 (ref -19/-30/-37 dB), the upper ones fading faster
    return partials(f, ms, [(1, 0, 1), (3, -19, .6), (5, -28, .4), (7, -34, .3)], tau, attack)


def v_bell(f, ms, tau, trem=None):
    # sine-ish bell: harmonic 2 and 4 faint (the ref's 1480/2960 lines) and one inharmonic 2.76 for a metal edge
    y = partials(f, ms, [(1, 0, 1), (2, -22, .6), (2.76, -30, .25), (4, -32, .4)], tau, 1.5)
    if trem:
        rate, depth = trem; t = t_of(ms)
        y *= 1 - depth / 2 + depth / 2 * np.cos(2 * np.pi * rate * t)
    return y


def v_ping(f, ms, tau):
    # glassy ping: a pure sine with a hint of a 4.2x partial that dies within ~20 ms (the strike)
    return partials(f, ms, [(1, 0, 1), (4.2, -24, .08), (2, -32, .5)], tau, 1.0)


def v_knock(f, ms, tau, drop, rng, noise=-12, lp=None, band=None, extra=()):
    # a short knock: damped sine whose pitch falls from f * drop to f in 10 ms, noise for the contact, extra
    # damped sines for body resonances
    t = t_of(ms)
    fr = f * (1 + (drop - 1) * np.exp(-t / .01))
    y = np.sin(2 * np.pi * np.cumsum(fr) / SR) * np.exp(-t / (tau / 1000))
    for fx, db in extra:
        y += amp(db) * np.sin(2 * np.pi * fx * t) * np.exp(-t / (tau * 1.2 / 1000))
    lo, hi = band if band else (60, lp or 2000)
    y += band_noise(ms, lo, hi, tau * .5, rng) * amp(noise)
    return ramp(y, .4)


def v_wood(f, ms, tau, rng):
    # woodblock: inharmonic modes 1 : 2.05 : 5.7 : 8.5 (measured on the ref), the high ones short, plus a tick
    y = partials(f, ms, [(1, 0, 1), (2.05, -5, .45), (5.7, -7, .2), (8.5, -12, .12)], tau, .8)
    y += band_noise(ms, 1500, 8000, 4, rng) * amp(-3)
    return y


def v_glide(f, ms, to, attack, hold, tau):
    # one tone sliding up exponentially f -> to over the whole sound, 2nd harmonic -10 dB, 3rd -22 dB
    t = t_of(ms)
    fr = f * (to / f) ** (t / t[-1])
    ph = 2 * np.pi * np.cumsum(fr) / SR
    y = np.sin(ph) + amp(-10) * np.sin(2 * ph) + amp(-22) * np.sin(3 * ph)
    e = np.where(t < hold / 1000, 1.0, np.exp(-(t - hold / 1000) / (tau / 1000)))
    return ramp(y * e, attack)


def v_thump(f, ms, tau, rng):
    # low body under a hit: a sine dropping a little in pitch plus low noise
    return v_knock(f, ms, tau, 1.25, rng, noise=-18, lp=600)


def v_shimmer(notes, ms, trem):
    # a quiet held chord that wavers (tremolo), fading in over 40 ms and out over the last 120 ms
    t = t_of(ms); y = np.zeros_like(t)
    for i, p in enumerate(notes):
        rate, depth = trem
        r = rate * (1 + .13 * i)
        y += np.sin(2 * np.pi * hz(p) * t) * (1 - depth / 2 + depth / 2 * np.cos(2 * np.pi * r * t + i))
    y /= len(notes) ** .5
    n = int(SR * .12); y[-n:] *= np.linspace(1, 0, n)
    return ramp(y, 40)


def v_swish(ms, band, tau, rng):
    return band_noise(ms, band[0], band[1], tau, rng)


def v_pad(f, ms, tau, attack):
    # soft synth voice: harmonics 1/n^1.9 up to ~4.5 kHz, two copies detuned +-4 cents (chorus), slow vibrato
    t = t_of(ms); y = np.zeros_like(t)
    for cents, phase in ((-4, 0), (4, 1.3)):
        fr = f * 2 ** (cents / 1200) * (1 + .0025 * np.sin(2 * np.pi * 5.2 * t + phase))
        ph = 2 * np.pi * np.cumsum(fr) / SR
        n = 1
        while f * n < 4500:
            # upper harmonics fade sooner, so the tail mellows like the ref's
            y += n ** -1.9 * np.sin(n * ph) * np.exp(-t / (tau / 1000 / (1 + .12 * (n - 1))))
            n += 1
    return ramp(y, attack)


def render(role, spec):
    rng = np.random.default_rng(sum(map(ord, role)))  # fixed seed per role
    total = spec["length"]; out = np.zeros(int(SR * total / 1000))
    for onset, voice, p, db, kw in spec["events"]:
        ms = total - onset
        kw = dict(kw)
        if voice == "sine": y = v_sine(hz(p), ms, **kw)
        elif voice == "mallet": y = v_mallet(hz(p), ms, rng=rng, **kw)
        elif voice == "tri": y = v_tri(hz(p), ms, **kw)
        elif voice == "bell": y = v_bell(hz(p), ms, **kw)
        elif voice == "ping": y = v_ping(hz(p), ms, **kw)
        elif voice == "knock": y = v_knock(hz(p), ms, rng=rng, **kw)
        elif voice == "wood": y = v_wood(hz(p), ms, rng=rng, **kw)
        elif voice == "glide": y = v_glide(hz(p), ms, **kw)
        elif voice == "thump": y = v_thump(hz(p), ms, rng=rng, **kw)
        elif voice == "swish": y = v_swish(ms, rng=rng, **kw)
        elif voice == "pad": y = v_pad(hz(p), ms, **kw)
        elif voice == "shimmer": y = v_shimmer(p, kw.pop("until") - onset, **kw)
        else: raise ValueError(voice)
        y = y / (np.max(np.abs(y)) or 1) * amp(db)
        i = int(SR * onset / 1000); out[i:i + len(y)] += y[:len(out) - i]
    n = int(SR * .012); out[-n:] *= np.linspace(1, 0, n)  # end at zero
    return out / np.max(np.abs(out)) * amp(-1)


def write_wav(path, x):
    with wave.open(path, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((np.clip(x, -1, 1) * 32767).astype("<i2").tobytes())


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    out = sys.argv[sys.argv.index("--out") + 1] if "--out" in sys.argv else os.path.join(here, "..", "build", "zvuky-vlastne")
    os.makedirs(out, exist_ok=True)
    spec = importlib.util.spec_from_file_location("zvuky", os.path.join(here, "zvuky.py"))
    zvuky = importlib.util.module_from_spec(spec); spec.loader.exec_module(zvuky)
    assert set(ROLES) == set(zvuky.TARGET), "roles differ from scripts/zvuky.py"
    zvuky.LEAD.clear()  # the lead-in cut is for Duolingo's node_complete; ours hits at once
    with tempfile.TemporaryDirectory() as raw:
        for role, s in ROLES.items():
            write_wav(os.path.join(raw, role + ".wav"), render(role, s))
        sys.argv = [sys.argv[0], raw, "--out", out]
        zvuky.main()


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Plays lessons on the emulator through real taps, using the debug-only "dbg:" marks for the right answers.
Fails loudly on a dead end (nothing to tap for several rounds) or a crash. Debug builds only.

usage: walk.py lesson <id> [shots_dir]    walk.py route <route>    walk.py screen
"""
import os, re, subprocess, sys, time
import xml.etree.ElementTree as ET

A = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from app_id import package  # noqa: E402

PKG = os.environ.get('FITEST_PKG') or package(os.environ.get('FITEST_APK'))


def sh(*a):
    return subprocess.run([A, *a], capture_output=True)


def nodes():
    for _ in range(6):
        r = sh('exec-out', 'uiautomator', 'dump', '/dev/tty')
        x = r.stdout.decode('utf8', 'ignore')
        i, j = x.find('<?xml'), x.rfind('</hierarchy>')
        if i >= 0 and j > 0:
            return list(ET.fromstring(x[i:j + 12]).iter('node'))
        time.sleep(.6)
    raise SystemExit('no ui dump')


def b(n):
    return list(map(int, re.findall(r'\d+', n.get('bounds'))))


def label(n):
    return (n.get('text') or '') + '|' + (n.get('content-desc') or '')


def tap_xy(x, y, wait=.5):
    sh('shell', 'input', 'tap', str(int(x)), str(int(y)))
    time.sleep(wait)


def tap(n, wait=.9):
    q = b(n)
    tap_xy((q[0] + q[2]) / 2, (q[1] + q[3]) / 2, wait)


def clickable_for(ns, n):
    """The clickable node that holds a text node (Compose puts the click on a parent)."""
    q = b(n)
    best = None
    for m in ns:
        if m.get('clickable') == 'true' and m.get('enabled') != 'false':
            r = b(m)
            if r[0] <= q[0] and r[1] <= q[1] and r[2] >= q[2] and r[3] >= q[3]:
                if best is None or (r[2] - r[0]) * (r[3] - r[1]) < (b(best)[2] - b(best)[0]) * (b(best)[3] - b(best)[1]):
                    best = m
    return best


def find_text(ns, text, exact=True):
    for n in ns:
        t = n.get('text') or ''
        d = n.get('content-desc') or ''
        if (t == text or d == text) if exact else (text in t or text in d):
            c = n if n.get('clickable') == 'true' else clickable_for(ns, n)
            if c is not None and c.get('enabled') != 'false':
                return c
    return None


def disabled_button(ns, text):
    for n in ns:
        if (n.get('text') or '') == text:
            c = None
            q = b(n)
            for m in ns:
                r = b(m)
                if m.get('clickable') == 'true' and r[0] <= q[0] and r[1] <= q[1] and r[2] >= q[2] and r[3] >= q[3]:
                    c = m
            if c is not None and c.get('enabled') == 'false':
                return True
    return False


def set_slider(n, value):
    """CoefSlider jumps to a tapped position; thumb is 48 dp (SliderThumb in ui/Components.kt), density 3."""
    d = n.get('content-desc')
    lo = float(re.search(r'min=(-?[\d.]+)', d).group(1)); hi = float(re.search(r'max=(-?[\d.]+)', d).group(1))
    q = b(n)
    thumb = 144
    x = q[0] + thumb / 2 + (value - lo) / (hi - lo) * (q[2] - q[0] - thumb)
    tap_xy(x, (q[1] + q[3]) / 2, .35)


def type_value(ns, v):
    keys = {}
    for n in ns:
        d = n.get('content-desc') or ''
        if d and len(d) <= 2 and b(n)[1] > 1500:
            keys[d] = n
    for ch in v.replace('-', '−'):
        k = keys.get(ch)
        if k is None:
            raise SystemExit(f'no key {ch!r} on the pad, keys={list(keys)}')
        tap(k, .15)


PRIORITY = ['Wait', 'Dokončiť', 'Ďalšia časť', 'Pokračovať', 'Späť do lekcie', 'Ďalší', 'Prečo je tam každá časť', 'Príklady a protipríklady',
            'Napísať z pamäti', 'Mám napísané', 'Hotovo', 'Dôkaz po riadkoch', 'Dôkaz', 'Dokázať · ', 'Na záver to napíš na papier', 'Ďalej', 'Všetko', 'Ďalší krok',
            'Prečítať, čo povedal', 'Radšej len prečítať titulky', 'Ďalšia úprava', 'Skúsiť znova', 'Neviem, ukáž mi to']


def step_once(ns):
    """Does one thing on the current screen. Returns a short description, or None if stuck."""
    texts = {n.get('text') for n in ns}
    # sliders with targets
    sliders = [n for n in ns if 'dbg:' in (n.get('content-desc') or '') and 'min=' in (n.get('content-desc') or '')]
    if sliders and disabled_button(ns, 'Pokračovať'):
        for s in sliders:
            d = s.get('content-desc')
            m = re.search(r'dbg:val=(-?[\d.]+)', d)
            if m:
                set_slider(s, float(m.group(1)))
            else:  # wiggle: a few different positions
                for v in (-2, 1, -1, 2, 0.5, -0.5):
                    set_slider(s, v)
        return 'sliders'
    # number fields: tap each, type the value
    fields = sorted([n for n in ns if re.search(r'dbg:field\d+=', n.get('content-desc') or '')], key=lambda n: b(n)[0])
    if fields and disabled_button(ns, 'Skontrolovať'):
        for f in fields:
            v = re.search(r'dbg:field\d+=(\S+)', f.get('content-desc')).group(1)
            tap(f, .25)
            # clear what is there
            ns2 = nodes()
            z = find_text(ns2, 'Zmazať')
            for _ in range(7):
                if z is not None:
                    tap(z, .05)
            type_value(nodes(), v)
        c = find_text(nodes(), 'Skontrolovať')
        if c is not None:
            tap(c, 1.0)
        return 'numbers'
    def open_picks(ns_):
        # the mark sits on a child; whether it is ticked shows on the checkable node with the same bounds
        on = {n.get('bounds') for n in ns_ if n.get('checked') == 'true' or n.get('selected') == 'true'}
        return [n for n in ns_ if 'dbg:pick' in (n.get('content-desc') or '') and n.get('bounds') not in on]
    picks = open_picks(ns)
    if picks and 'Skontrolovať' in texts:
        # the options may sit below the fold (a "Ako na to" card above them): tap what is on screen, scroll, repeat
        for _ in range(8):
            on_screen = [p for p in open_picks(nodes()) if 250 < b(p)[1] and b(p)[3] < 2600]
            for p in on_screen:
                tap(p, .3)
            if not open_picks(nodes()):
                break
            # slow drag, no fling: the rows must stand still before the next dump and tap
            sh('shell', 'input', 'swipe', '640', '2000', '640', '1400', '1200')
            time.sleep(1.5)
        c = find_text(nodes(), 'Skontrolovať')
        if c is not None:
            tap(c, 1.0)
        return 'picks'
    ins = [n for n in ns if 'dbg:in' in (n.get('content-desc') or '') and n.get('selected') != 'true']
    if ins and (disabled_button(ns, 'Pokračovať') or find_text(ns, 'Skontrolovať') is not None):
        for p in ins:
            tap(p, .3)
        c = find_text(nodes(), 'Skontrolovať')
        if c is not None:
            tap(c, 1.0)
        return 'points'
    correct = [n for n in ns if (n.get('content-desc') or '') == 'dbg:correct' and n.get('enabled') != 'false']
    if correct:
        tap(correct[0], 1.0)
        return 'correct'
    # a topic's own picture (Pic.Custom) without dbg marks: play it with real taps first, then the debug goal switch
    goal = [n for n in ns if (n.get('content-desc') or '') == 'dbg:goal']
    if goal and disabled_button(ns, 'Pokračovať'):
        global FUZZ
        FUZZ = FUZZ + 1 if 'FUZZ' in globals() else 1
        if FUZZ <= 6:
            cands = [n for n in ns if n.get('clickable') == 'true' and n.get('enabled') != 'false' and 300 < b(n)[1] < 2300
                     and (n.get('content-desc') or '') not in ('dbg:goal', 'Nahlásiť chybu') and not (n.get('content-desc') or '').startswith('Zavrieť')]
            for n in cands[(FUZZ - 1) % max(1, len(cands)):][:4]:
                tap(n, .4)
            return 'picture taps'
        FUZZ = 0
        tap(goal[0], .8)
        print('  PICTURE GOAL SKIPPED (debug switch)', flush=True)
        return 'goal switch'
    for t in PRIORITY:
        c = find_text(ns, t, exact=not t.endswith(' · '))  # 'Dokázať · 3 kroky': the count varies
        if c is not None:
            tap(c, 1.0)
            return t
    # an Ask step: tap the first option (any guess is fine); elsewhere the right answer may be below the fold
    if 'Neviem, ukáž mi to' not in texts and 'Skúsiť znova' not in texts:
        return None
    for n in ns:
        if n.get('clickable') == 'true' and n.get('enabled') != 'false' and not (n.get('content-desc') or '') and b(n)[1] > 1500:
            kids = [k for k in n.iter('node') if k is not n and (k.get('text') or '')]
            if kids:
                tap(n, 1.0)
                return 'guess'
    return None


def crashed():
    out = sh('logcat', '-d', '-b', 'crash').stdout.decode('utf8', 'ignore')
    return 'FATAL EXCEPTION' in out and PKG in out


def walk(done_text, shots=None, limit=400):
    stuck = 0
    steps = 0
    last = None
    while steps < limit:
        ns = nodes()
        if any((n.get('text') or '') in done_text for n in ns):
            return steps
        if crashed():
            raise SystemExit('CRASH, see adb logcat -b crash')
        cur = next((n.get('content-desc') for n in ns if (n.get('content-desc') or '').startswith('Krok ')), None)
        if shots and cur != last and cur:
            sh('exec-out', 'screencap', '-p')
            open(os.path.join(shots, cur.replace(' ', '_') + '.png'), 'wb').write(sh('exec-out', 'screencap', '-p').stdout)
        last = cur
        did = step_once(ns)
        steps += 1
        if did is None:
            sh('shell', 'input', 'swipe', '640', '1600', '640', '700', '300')
            time.sleep(.6)
            stuck += 1
            if stuck >= 8:
                open('/tmp/la1shots/deadend.png', 'wb').write(sh('exec-out', 'screencap', '-p').stdout)
                raise SystemExit(f'DEAD END at {cur}: nothing to tap, screenshot /tmp/la1shots/deadend.png')
        else:
            stuck = 0
            print(f'  {cur}: {did}', flush=True)
    raise SystemExit('too many actions')


if __name__ == '__main__':
    cmd = sys.argv[1]
    if cmd == 'lesson':
        lid = sys.argv[2]
        shots = sys.argv[3] if len(sys.argv) > 3 else None
        if shots:
            os.makedirs(shots, exist_ok=True)
        sh('logcat', '-c', '-b', 'crash')
        sh('shell', 'am', 'start', '-a', 'android.intent.action.VIEW', '-d', f'tabula://lesson/{lid}/0', PKG)
        time.sleep(2.5)
        n = walk({'Lekciu máš.'}, shots)
        print(f'OK {lid}: {n} actions')
    elif cmd == 'route':
        sh('shell', 'am', 'start', '-a', 'android.intent.action.VIEW', '-d', 'tabula://' + sys.argv[2].replace(':', '/'), PKG)
    elif cmd == 'screen':
        for n in nodes():
            t = n.get('text') or n.get('content-desc')
            if t or n.get('clickable') == 'true':
                print(b(n), n.get('clickable'), n.get('enabled'), repr(t)[:100])

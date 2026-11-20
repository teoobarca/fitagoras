#!/usr/bin/env python3
"""Tiny adb driver: tap by visible text or content description, like a person would.
usage: ui.py tap "Začať lekciu" | ui.py shot out.png | ui.py has "text" | ui.py dump | ui.py swipe x1 y1 x2 y2 ms"""
import os, re, subprocess, sys, time
import xml.etree.ElementTree as ET
A = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
def sh(*a, **k): return subprocess.run([A, *a], capture_output=True, **k)
def nodes():
    for _ in range(4):
        r = sh('exec-out', 'uiautomator', 'dump', '/dev/tty')
        x = r.stdout.decode('utf8', 'ignore')
        i = x.find('<?xml')
        j = x.rfind('</hierarchy>')
        if i >= 0 and j > 0:
            return ET.fromstring(x[i:j + 12]).iter('node')
        time.sleep(.5)
    raise SystemExit('no dump')
def bounds(n):
    a = list(map(int, re.findall(r'\d+', n.get('bounds'))))
    return a
def find(q):
    best = None
    for n in nodes():
        t = (n.get('text') or '') + '|' + (n.get('content-desc') or '')
        if q in t:
            best = n
            if n.get('clickable') == 'true': return n
    return best
c = sys.argv[1]
if c == 'tap':
    for _ in range(12):
        n = find(sys.argv[2])
        if n is not None:
            b = bounds(n)
            sh('shell', 'input', 'tap', str((b[0] + b[2]) // 2), str((b[1] + b[3]) // 2))
            print('tapped', sys.argv[2], b); break
        time.sleep(.5)
    else:
        print('NOT FOUND', sys.argv[2]); sys.exit(1)
    time.sleep(float(sys.argv[3]) if len(sys.argv) > 3 else .9)
elif c == 'has':
    n = find(sys.argv[2]); print('YES' if n is not None else 'NO', sys.argv[2])
elif c == 'dump':
    for n in nodes():
        t = n.get('text') or n.get('content-desc')
        if t or n.get('clickable') == 'true':
            b = bounds(n); print(b, n.get('clickable'), repr(t)[:90], f"{(b[2]-b[0])/3:.0f}x{(b[3]-b[1])/3:.0f}dp")
elif c == 'shot':
    open(sys.argv[2], 'wb').write(sh('exec-out', 'screencap', '-p').stdout)
elif c == 'swipe':
    sh('shell', 'input', 'swipe', *sys.argv[2:])

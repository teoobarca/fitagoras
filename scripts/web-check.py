# The web app's check before an upload (docs/KMP.md, "The web"): serves build/web/ from this machine and walks the
# basic path a student takes, in WebKit as an iPhone and in Chromium as an Android phone. Exit code 0 only when all of
# it holds:
#   - the splash shows while loading and goes once the first screen is drawn;
#   - Dnes offers the next lesson; a lesson opens, a question answers ("Neviem, ukáž mi to") and the step moves on;
#   - the address keeps the open lesson, a reload comes back into it, and the browser's back leaves it for Dnes;
#   - the progress survives the reload (localStorage);
#   - offline, a second visit opens from the service worker;
#   - the console has no errors (beyond the known ones below) and nothing is fetched from another host;
#   - the whole test on time starts at its full time and counts down, keeps the time across a switch to another tab, a
#     reload and a closed tab (on a fake clock moved forward meanwhile), and is gone once handed in;
#   - a lecture clip plays in the lesson's frame: the <video> really plays (time runs, a frame decoded), lies exactly
#     on its frame in the canvas also after the captions scroll and the window changes size, the time under it runs
#     with it, fullscreen and back work, leaving the step hides and pauses it, and offline the captions stay with a
#     sentence over the frame instead of the picture (light and dark, and a wide window, where it sits in the column and
#     its fullscreen still fills the window);
#   - in a computer's window (1440 x 900, 1024 x 768) the app is a column at most COLUMN px wide in the middle
#     (ui/AppColumn.kt): the tabs, Dnes's button and a lesson's controls stay in it, and the wheel beside it scrolls it.
#
#   scripts/web.sh && uvx --with playwright==1.64.0 python scripts/web-check.py [--shots DIR] [--app URL]
#
# The clips come from a stand-in server here (one test video for every clip, with byte ranges, only with the key the
# build carries), so the check needs no network. --app URL walks a running server instead (its /app/ and /api/clips).
#
# Playwright 1.64.0 matches browsers already in ~/Library/Caches/ms-playwright (otherwise: `playwright install webkit
# chromium` once). Nothing is installed globally; the server is started and stopped here.
import asyncio
import os
import re
import socket
import subprocess
import sys
import time

from playwright.async_api import async_playwright

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WEB = os.path.join(ROOT, "build", "web")
SHOTS = sys.argv[sys.argv.index("--shots") + 1] if "--shots" in sys.argv else None
APP = sys.argv[sys.argv.index("--app") + 1] if "--app" in sys.argv else None
CLIP = "k3-hodnost-sucinu"  # an LA1 clip (files/clips-la1-k3.json); its route opens the clip's step on its own
# messages of the libraries themselves, not of the app: Kotlin/Wasm's notice and Skia asking WebGL for a debug extension
# (and WebKit's own media controls missing their icons in Playwright's build: "Button failed to load")
KNOWN = ("Accessing `memory` via `wasmExports` is deprecated", "WEBGL_debug_renderer_info", "WEBGL_polygon_mode", "Button failed to load")

CSP = ("default-src 'self'; script-src 'self' 'unsafe-inline' 'wasm-unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; "
       "font-src 'self'; media-src 'self' blob:; worker-src 'self'; manifest-src 'self'; "
       "connect-src 'self' https://auth.openai.com https://api.openai.com; frame-ancestors 'none'; base-uri 'none'; form-action 'none'")
SERVER = f"""
import http.server, os, re, sys, urllib.parse
KEY, CLIP = sys.argv[3], sys.argv[4]
class H(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        if not self.path.startswith("/api/"):
            self.send_header("Content-Security-Policy", {CSP!r}); self.send_header("Cache-Control", "no-cache")
        super().end_headers()
    def log_message(self, *a): pass
    def do_HEAD(self):
        return self.clip(False) if self.path.startswith("/api/clips/") else super().do_HEAD()
    def do_GET(self):
        return self.clip(True) if self.path.startswith("/api/clips/") else super().do_GET()
    def clip(self, body):
        # the app's clip address: /api/clips/<recording>.mp4?from=&to=&key= (the key in the address: a <video> sends no headers)
        q = urllib.parse.parse_qs(urllib.parse.urlsplit(self.path).query)
        if not KEY or q.get("key", [""])[0] != KEY or "from" not in q or "to" not in q:
            self.send_response(401); self.send_header("Content-Length", "0"); self.end_headers(); return
        data = open(CLIP, "rb").read()
        start, end = 0, len(data) - 1
        m = re.match(r"bytes=(\\d*)-(\\d*)", self.headers.get("Range", ""))
        if m:
            start = int(m.group(1)) if m.group(1) else len(data) - int(m.group(2))
            end = min(int(m.group(2)), len(data) - 1) if m.group(1) and m.group(2) else len(data) - 1
            self.send_response(206); self.send_header("Content-Range", f"bytes {{start}}-{{end}}/{{len(data)}}")
        else:
            self.send_response(200)
        self.send_header("Content-Type", "video/mp4"); self.send_header("Accept-Ranges", "bytes")
        self.send_header("Cache-Control", "private, no-cache"); self.send_header("Content-Length", str(end - start + 1)); self.end_headers()
        if body:
            try: self.wfile.write(data[start:end + 1])
            except (BrokenPipeError, ConnectionResetError): pass
H.extensions_map[".wasm"] = "application/wasm"
http.server.ThreadingHTTPServer(("127.0.0.1", int(sys.argv[1])), lambda *a, **k: H(*a, directory=sys.argv[2], **k)).serve_forever()
"""

# the app's widest (ui/AppColumn.kt, AppWidth): one dp is one CSS pixel in the browser
COLUMN = 480

problems: list[str] = []


def check(ok: bool, what: str) -> None:
    print(("  ok    " if ok else "  CHYBA ") + what)
    if not ok:
        problems.append(what)


def free_port() -> int:
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


async def tap(page, name: str, wait: int = 900, exact: bool = False) -> None:
    """A tap at the middle of the button the app exposes to accessibility (Compose draws on a canvas)."""
    el = page.get_by_role("button", name=name, exact=exact).first
    await el.wait_for(state="attached", timeout=15000)
    b = await el.bounding_box()
    await page.mouse.click(b["x"] + b["width"] / 2, b["y"] + b["height"] / 2)
    await page.wait_for_timeout(wait)


async def has(page, name: str, wait: int = 0) -> bool:
    """Whether the screen offers that button, waiting up to [wait] ms for it (animations, a slow software renderer)."""
    end = time.time() + wait / 1000
    while True:
        if await page.get_by_role("button", name=name).count() > 0:
            return True
        if time.time() >= end:
            return False
        await page.wait_for_timeout(200)


async def gone(page, name: str, wait: int = 4000) -> bool:
    end = time.time() + wait / 1000
    while await page.get_by_role("button", name=name).count() > 0:
        if time.time() >= end:
            return False
        await page.wait_for_timeout(200)
    return True


async def ready(page) -> float:
    t = time.time()
    await page.get_by_role("button").first.wait_for(state="attached", timeout=60000)
    await page.locator("#splash").wait_for(state="detached", timeout=10000)
    return time.time() - t


async def shot(page, name: str) -> None:
    if SHOTS:
        await page.screenshot(path=os.path.join(SHOTS, name + ".png"))


async def walk(p, engine: str, device: str, url: str) -> None:
    print(f"{engine} ({device})")
    browser = await getattr(p, engine).launch()
    try:
        ctx = await browser.new_context(**p.devices[device])
        page = await ctx.new_page()
        log: list[str] = []
        page.on("console", lambda m: log.append(m.text) if m.type == "error" and not any(k in m.text for k in KNOWN) else None)
        page.on("pageerror", lambda e: log.append(f"pageerror: {e}"))
        page.on("request", lambda r: log.append(f"request to another host: {r.url}") if not r.url.startswith(url.split("/app/")[0]) and not r.url.startswith(("data:", "blob:")) else None)

        await page.goto(url, wait_until="commit")
        await page.locator("#splash").wait_for(state="attached", timeout=10000)
        check(True, "úvodná obrazovka sa ukáže hneď")
        took = await ready(page)
        check(True, f"prvá obrazovka nakreslená, úvodná zmizla ({took:.1f} s)")
        await shot(page, f"{engine}-1-dnes")
        check(await has(page, "Začať lekciu", 4000), "Dnes ponúka lekciu")

        await tap(page, "Začať lekciu", 1500)
        check(await has(page, "Zavrieť lekciu", 4000), "lekcia sa otvorí")
        lesson_url = page.url
        check("#lesson:" in page.url, f"adresa drží lekciu ({page.url.split('/app/')[-1]})")
        if await has(page, "Neviem, ukáž mi to", 3000):
            await tap(page, "Neviem, ukáž mi to", 1200)
            check(await has(page, "Pokračovať", 4000), "po „Neviem“ ide krok ďalej")
            # back inside the lesson is the lesson's own step back: the lesson stays, and so does its address
            await tap(page, "Pokračovať", 1500)
            await page.go_back()
            await page.wait_for_timeout(1200)
            check(await has(page, "Zavrieť lekciu") and "#lesson:" in page.url, "späť v lekcii ostane v lekcii aj s adresou")
        await shot(page, f"{engine}-2-lekcia")
        saved = await page.evaluate("() => (localStorage.getItem('fitest.state') || '').length")
        check(saved > 0, f"postup uložený ({saved} znakov)")

        await page.reload(wait_until="commit")
        await ready(page)
        check(await has(page, "Zavrieť lekciu", 4000), "po obnovení stránky je študent späť v lekcii")
        await shot(page, f"{engine}-3-po-obnoveni")
        await page.go_back()
        check(await gone(page, "Zavrieť lekciu") and await has(page, "Dnes", 4000), "späť (gesto, tlačidlo) vráti na Dnes")
        check("#" not in page.url, "adresa je zas bez lekcie")

        # a link straight into the lesson (a new tab): back leaves the lesson, and a second back doesn't open it again
        link = await ctx.new_page()
        await link.goto(lesson_url, wait_until="commit")
        await ready(link)
        check(await has(link, "Zavrieť lekciu", 4000), "odkaz #lesson otvorí lekciu")
        await link.go_back()
        ok = await gone(link, "Zavrieť lekciu")
        await link.go_back()
        await link.wait_for_timeout(1500)
        check(ok and not await has(link, "Zavrieť lekciu") and await has(link, "Dnes"), "z lekcie z odkazu späť na Dnes a tam ostane")
        await link.close()

        # offline: the service worker has the whole build once it is active
        await page.wait_for_timeout(1500)
        controlled = await page.evaluate("() => navigator.serviceWorker.ready.then(() => !!navigator.serviceWorker.controller)")
        check(controlled, "service worker beží")
        await ctx.set_offline(True)
        await page.reload(wait_until="commit")
        try:
            await ready(page)
            check(await has(page, "Dnes", 4000), "bez siete sa appka otvorí")
        except Exception as e:  # noqa: BLE001 - the failure is the finding
            check(False, f"bez siete sa appka otvorí ({str(e).splitlines()[0]})")
        await shot(page, f"{engine}-4-offline")
        await ctx.set_offline(False)

        check(not log, "konzola bez chýb, nič z cudzích adries" + ("" if not log else ": " + " | ".join(log[:5])))
    finally:
        await browser.close()


async def minutes_left(page, wait: int = 8000) -> int | None:
    """The whole minutes the timed test's clock reads ("1:09:57", its heading "Zostáva 69 minút", screens/Papers.kt)."""
    el = page.get_by_role("heading", name=re.compile(r"^Zostáva")).first
    try:
        await el.wait_for(state="attached", timeout=wait)
    except Exception:  # noqa: BLE001 - no clock on the screen is the finding
        return None
    secs = 0
    for part in (await el.inner_text()).strip().split(":"):
        secs = secs * 60 + int(part)
    return secs // 60


async def timed_test(p, engine: str, device: str, url: str) -> None:
    """The whole test on time (the web showed 0:00 at once, 19. 11.), on Playwright's fake clock so time away can pass."""
    print(f"{engine} ({device}): test na čas")
    browser = await getattr(p, engine).launch()
    try:
        ctx = await browser.new_context(**p.devices[device])
        page = await ctx.new_page()
        await page.clock.install()
        # the intro of one paper by its route (the tab's "Celý test na čas" sits below the fold on a phone). A new
        # student starts in the course whose test comes first, and a paper of the other course doesn't open there:
        # so one paper of each course, and the one that opens is used.
        opened = False
        for paper in ("slon-2024", "dml-2025-k"):
            await page.goto(url + f"#mock:{paper}", wait_until="commit")
            await page.reload(wait_until="commit")
            await ready(page)
            opened = await has(page, re.compile(r"^Spustiť \d+ minút"), 4000)
            if opened:
                break
        check(opened, "úvod testu na čas sa otvorí")
        intro = await page.locator("body").aria_snapshot()
        check("oznámení" not in intro and "pokračuje" in intro, "úvod na webe nesľubuje oznámenie, sľubuje pokračovanie")
        full = int(re.search(r"Spustiť (\d+) minút", await page.locator("body").aria_snapshot()).group(1))
        await shot(page, f"{engine}-5-test-uvod")
        await tap(page, re.compile(r"^Spustiť \d+ minút"), 1500)
        left = await minutes_left(page)
        check(left in (full - 1, full), f"po spustení ukazuje celý čas ({left} z {full} min)")
        await shot(page, f"{engine}-6-test-zaciatok")
        await page.clock.fast_forward("03:00")
        await page.wait_for_timeout(1500)
        left = await minutes_left(page)
        check(left in (full - 4, full - 3), f"odpočítava (po 3 min: {left} min)")

        # another tab in front while 10 minutes pass
        other = await ctx.new_page()
        await other.bring_to_front()
        await page.clock.fast_forward("10:00")
        await page.bring_to_front()
        await page.wait_for_timeout(1500)
        left = await minutes_left(page)
        check(left in (full - 14, full - 13), f"po návrate na kartu čas sedí (o 10 min neskôr: {left} min)")
        await other.close()

        await page.reload(wait_until="commit")
        await ready(page)
        left = await minutes_left(page)
        check(left in (full - 14, full - 13), f"po obnovení stránky test pokračuje ({left} min)")

        # the tab closed, the app opened again from its plain address 20 minutes later (the new page's clock starts there)
        saved = await page.evaluate("() => localStorage.getItem('fitest.mock')")
        then = await page.evaluate("() => Date.now()")
        await page.close()
        page = await ctx.new_page()
        await page.clock.install(time=then / 1000 + 20 * 60)
        await page.goto(url, wait_until="commit")
        await ready(page)
        left = await minutes_left(page)
        check(saved is not None and left in (full - 34, full - 33), f"po zatvorení karty a novom otvorení o 20 min test pokračuje ({left} min)")
        await shot(page, f"{engine}-7-test-po-navrate")

        await tap(page, "Odovzdať", 1200)
        dialog = page.get_by_role("button", name="Odovzdať").last  # the dialog's, over the screen's
        b = await dialog.bounding_box()
        await page.mouse.click(b["x"] + b["width"] / 2, b["y"] + b["height"] / 2)
        await page.wait_for_timeout(1500)
        await page.reload(wait_until="commit")
        await ready(page)
        check(await minutes_left(page, 3000) is None and await page.evaluate("() => localStorage.getItem('fitest.mock')") is None, "po odovzdaní sa test po obnovení nevráti")
    finally:
        await browser.close()


VIDEO = """() => { const v = document.querySelector('#clip video'); if (!v) return null; const r = v.getBoundingClientRect();
  const l = document.getElementById('clip');
  return {t: v.currentTime, ready: v.readyState, paused: v.paused, w: v.videoWidth, h: v.videoHeight, shown: !!l && l.style.display !== 'none',
          x: r.left, y: r.top, width: r.width, height: r.height, error: v.error && v.error.code, full: !!document.fullscreenElement,
          src: (v.getAttribute('src') || '').replace(/key=[^&]*/, 'key=…')}; }"""
FRAME = "Video, dvojité ťuknutie na celú obrazovku"  # the picture's label in the app (screens/Clip.kt, ClipLayer)


async def frame_box(page):
    el = page.get_by_label(FRAME).first
    try:
        await el.wait_for(state="attached", timeout=8000)
    except Exception:  # noqa: BLE001 - no frame is the finding
        return None
    return await el.bounding_box()


def on_frame(v, b, tol=2.5) -> bool:
    """The video on its frame. The frame's box is what the step's body leaves visible of it: a frame taller than the
    room left (a wide window) is cut there, and the canvas covers the video beyond, so then it only has to lie inside."""
    if not (v and b):
        return False
    if all(abs(v[k] - b[k]) <= tol for k in ("x", "y", "width", "height")):
        return True
    return abs(v["x"] - b["x"]) <= tol and abs(v["width"] - b["width"]) <= tol and v["y"] <= b["y"] + tol and v["y"] + v["height"] >= b["y"] + b["height"] - tol


async def until(page, test, wait=15000):
    end = time.time() + wait / 1000
    while True:
        v = await page.evaluate(VIDEO)
        if test(v) or time.time() >= end:
            return v
        await page.wait_for_timeout(250)


async def label_text(page, pattern) -> str | None:
    el = page.get_by_text(pattern).first
    try:
        await el.wait_for(state="attached", timeout=3000)
        return (await el.inner_text()).strip()
    except Exception:  # noqa: BLE001
        return None


async def clips(p, engine: str, device: str, url: str, dark: bool = False, wide: bool = False) -> None:
    """A lecture clip in its step: plays, sits on its frame, follows the window, fullscreen and back, hides on leaving."""
    name = f"{engine}-klip" + ("-tmavy" if dark else "") + ("-siroke" if wide else "")
    print(f"{engine} ({'široké okno' if wide else device}{', tmavá' if dark else ''}): klip z prednášky")
    browser = await getattr(p, engine).launch()
    try:
        opts = {"viewport": {"width": 1440, "height": 900}} if wide else dict(p.devices[device])
        ctx = await browser.new_context(**opts, color_scheme="dark" if dark else "light")
        page = await ctx.new_page()
        log: list[str] = []
        page.on("console", lambda m: log.append(m.text) if m.type == "error" and not any(k in m.text for k in KNOWN) else None)
        page.on("pageerror", lambda e: log.append(f"pageerror: {e}"))
        await page.goto(url + "#clip:" + CLIP, wait_until="commit")
        await ready(page)
        check(await has(page, re.compile(r"^Pustiť klip"), 6000), "krok klipu ponúka „Pustiť klip“")
        await tap(page, re.compile(r"^Pustiť klip"), 300)
        v = await until(page, lambda v: v and v["ready"] >= 2 and v["t"] > 1.0 and not v["paused"], 30000)
        check(bool(v) and v["ready"] >= 2 and v["w"] > 0 and not v["paused"], f"video hrá (readyState {v and v['ready']}, {v and v['w']}×{v and v['h']}, {v and round(v['t'], 1)} s)")
        t0 = v["t"] if v else 0
        await page.wait_for_timeout(1500)
        v = await page.evaluate(VIDEO)
        check(bool(v) and v["t"] > t0 + 0.8, f"čas videa beží ({round(t0, 1)} → {v and round(v['t'], 1)} s)")
        check(bool(v) and "key=" in v["src"] and v["src"].startswith("/api/clips/"), f"adresa klipu z tohto servera s kľúčom ({v and v['src'][:60]})")
        b = await frame_box(page)
        check(on_frame(v, b), f"video leží presne na svojom mieste v kroku ({v and (round(v['x']), round(v['y']), round(v['width']), round(v['height']))} ~ {b and (round(b['x']), round(b['y']), round(b['width']), round(b['height']))})")
        stamp1 = await label_text(page, re.compile(r"^\d+:\d\d$"))
        await page.wait_for_timeout(2200)
        stamp2 = await label_text(page, re.compile(r"^\d+:\d\d$"))
        check(bool(stamp1 and stamp2 and stamp1 != stamp2), f"čas pod videom ide s ním, titulky idú za ním ({stamp1} → {stamp2})")
        await shot(page, f"{name}-1-hra")

        # the captions scroll under it: the video stays on its frame
        list_box = await page.get_by_role("button", name=re.compile("Prejsť sem")).first.bounding_box() if await page.get_by_role("button", name=re.compile("Prejsť sem")).count() else None
        if list_box:
            await page.mouse.move(list_box["x"] + list_box["width"] / 2, list_box["y"] + list_box["height"] / 2)
            await page.mouse.wheel(0, 300)
            await page.wait_for_timeout(800)
        v, b = await page.evaluate(VIDEO), await frame_box(page)
        check(on_frame(v, b), "po posune titulkov video ostane na mieste")
        # the window changes (a rotated phone, a resized window)
        size = page.viewport_size
        await page.set_viewport_size({"width": int(size["width"] * (0.8 if wide else 1.15)), "height": size["height"]})
        await page.wait_for_timeout(1200)
        v, b = await page.evaluate(VIDEO), await frame_box(page)
        check(on_frame(v, b), f"po zmene veľkosti okna video sedí na mieste ({v and round(v['x'])},{v and round(v['y'])} ~ {b and round(b['x'])},{b and round(b['y'])})")
        await page.set_viewport_size(size)
        await page.wait_for_timeout(1200)

        # fullscreen and back (an iPhone gets Safari's own player there, which a desktop WebKit doesn't have)
        fb = await page.get_by_role("button", name="Na celú obrazovku").first.bounding_box()
        # a wide window keeps the app in its phone-wide column (ui/AppColumn.kt), so the controls stay in the window there too
        reach = bool(fb and b) and fb["y"] + fb["height"] <= page.viewport_size["height"]
        check(reach, f"ovládanie klipu je v okne ({fb and round(fb['y'] + fb['height'])} z {page.viewport_size['height']} px)")
        if not reach:
            return
        await tap(page, "Na celú obrazovku", 900)
        v = await page.evaluate(VIDEO)
        vw = page.viewport_size["width"]
        check(bool(v) and (v["width"] >= vw * 0.95 or v["height"] >= page.viewport_size["height"] * 0.95), f"celá obrazovka: video cez celú šírku ({v and round(v['width'])} z {vw}, prehliadač na celej obrazovke: {v and v['full']})")
        # the lesson under the picture is out of reach, the fullscreen layer has its own controls
        check(await gone(page, "Na celú obrazovku", 2000), "na celej obrazovke je ovládanie appky nad videom")
        await shot(page, f"{name}-2-cela-obrazovka")
        # the controls hide after 3 s of playing and a tap on the picture brings them back, as on Android. Exact name:
        # the fullscreen layer's own button ("Zobraziť ovládanie") is named after all the text in it
        for _ in range(4):
            try:
                eb = await page.get_by_role("button", name="Zavrieť celú obrazovku", exact=True).first.bounding_box(timeout=800)
            except Exception:  # noqa: BLE001 - hidden controls
                eb = None
            if eb:
                await page.mouse.click(eb["x"] + eb["width"] / 2, eb["y"] + eb["height"] / 2)
            else:
                await page.mouse.click(vw / 2, page.viewport_size["height"] / 3)
            if await has(page, "Na celú obrazovku", 1200):
                break
        await page.wait_for_timeout(800)
        v, b = await page.evaluate(VIDEO), await frame_box(page)
        check(await has(page, "Na celú obrazovku") and on_frame(v, b) and not v["full"], "z celej obrazovky späť do kroku, video zas na svojom mieste")
        check(not log, "konzola bez chýb" + ("" if not log else ": " + " | ".join(log[:5])))

        # leaving the step: the picture goes and the clip stops
        await tap(page, "Pokračovať", 1200)
        v = await page.evaluate(VIDEO)
        check(bool(v) and not v["shown"] and v["paused"], "po odchode z kroku video zmizne a stojí")

        # offline: the frame says so, the captions stay
        if not wide and not dark:
            await ctx.set_offline(True)
            off = await ctx.new_page()
            await off.goto(url + "#clip:" + CLIP, wait_until="commit")
            try:
                await ready(off)
                await tap(off, re.compile(r"^Pustiť klip"), 2500)
                said = await label_text(off, re.compile("Server nedostupný"))
                check(said is not None, f"bez siete veta nad rámikom ({said})")
                check(await off.get_by_role("button", name=re.compile("Prejsť sem")).count() > 0 or await has(off, "Titulky pokračujú nižšie"), "bez siete titulky ostanú")
                await shot(off, f"{name}-3-bez-siete")
            except Exception as e:  # noqa: BLE001 - the failure is the finding
                check(False, f"bez siete ({str(e).splitlines()[0]})")
            await ctx.set_offline(False)
    finally:
        await browser.close()


async def spread(page) -> tuple[float, float]:
    """The leftmost and rightmost edge of every button and tab the screen exposes (Compose draws on a canvas)."""
    els = await page.get_by_role("button").all() + await page.get_by_role("tab").all()
    boxes = [b for b in [await el.bounding_box() for el in els] if b and b["width"] > 0]
    return min(b["x"] for b in boxes), max(b["x"] + b["width"] for b in boxes)


async def wide(p, engine: str, url: str) -> None:
    """A computer's window: the app as a phone-wide column in the middle, nothing out at the window's edges."""
    print(f"{engine} (široké okno)")
    browser = await getattr(p, engine).launch()
    try:
        for w, h in ((1440, 900), (1024, 768)):
            ctx = await browser.new_context(viewport={"width": w, "height": h})
            page = await ctx.new_page()
            await page.goto(url, wait_until="commit")
            await ready(page)
            await has(page, "Začať lekciu", 4000)
            left, right = await spread(page)
            check(right - left <= COLUMN and abs((left + right) / 2 - w / 2) <= 2,
                  f"{w}x{h}: Dnes v strede, nie širšia než {COLUMN} px ({left:.0f} až {right:.0f})")
            start = await page.get_by_role("button", name="Začať lekciu").first.bounding_box()
            check(start is not None and start["width"] <= COLUMN and abs(start["x"] + start["width"] / 2 - w / 2) <= 2,
                  f"{w}x{h}: tlačidlo lekcie v stĺpci ({start and round(start['width'])} px)")
            await shot(page, f"{engine}-{w}-dnes")

            # the wheel beside the column scrolls the column (web.js, "the column")
            # a new load (a changed #route alone stays in the page, mid-transition), then the same controls by name
            await page.goto(url + "#cesta", wait_until="commit")
            await page.reload(wait_until="commit")
            await ready(page)
            await page.wait_for_timeout(800)
            names = [n for n in [await el.get_attribute("aria-label") or await el.inner_text() for el in await page.get_by_role("button").all()] if n][:12]

            async def tops() -> list:
                out = []
                for n in names:
                    try:
                        b = await page.get_by_role("button", name=n, exact=True).first.bounding_box(timeout=1000)
                    except Exception:  # noqa: BLE001 - gone from the screen: no position to compare
                        b = None
                    out.append(b and b["y"])
                return out
            before = await tops()
            await page.mouse.move(60, h / 2)
            await page.mouse.wheel(0, 300)
            await page.wait_for_timeout(1200)
            after = await tops()
            moved = any(a is not None and b is not None and a < b - 50 for a, b in zip(after, before))
            check(moved, f"{w}x{h}: koliesko vedľa stĺpca posúva Cestu")

            await page.goto(url, wait_until="commit")
            await ready(page)
            await tap(page, "Začať lekciu", 1500)
            check(await has(page, "Zavrieť lekciu", 4000), f"{w}x{h}: lekcia sa otvorí")
            left, right = await spread(page)
            check(right - left <= COLUMN and abs((left + right) / 2 - w / 2) <= 2,
                  f"{w}x{h}: lekcia v strede, nie širšia než {COLUMN} px ({left:.0f} až {right:.0f})")
            await shot(page, f"{engine}-{w}-lekcia")
            await ctx.close()
    finally:
        await browser.close()


async def serving(port: int, wait: float = 20) -> bool:
    """Whether the local server answers within [wait] s (a loaded machine starts Python slowly)."""
    end = time.time() + wait
    while time.time() < end:
        try:
            with socket.create_connection(("127.0.0.1", port), timeout=1):
                return True
        except OSError:
            await asyncio.sleep(0.2)
    return False


async def main() -> int:
    if not os.path.isfile(os.path.join(WEB, "index.html")):
        print("Chýba build/web: najprv scripts/web.sh", file=sys.stderr)
        return 2
    if SHOTS:
        os.makedirs(SHOTS, exist_ok=True)
    port = free_port()
    # the server serves build/web under /app/ like the real one (the service worker's scope is the folder)
    root = os.path.join(ROOT, "build", "web-check")
    os.makedirs(root, exist_ok=True)
    link = os.path.join(root, "app")
    if not os.path.islink(link):
        os.symlink(WEB, link)
    # the stand-in clip: one minute of a test picture with a tone, made once
    clip = os.path.join(root, "clip.mp4")
    if not os.path.isfile(clip):
        subprocess.run(["ffmpeg", "-v", "error", "-y", "-f", "lavfi", "-i", "testsrc=size=1280x720:rate=25", "-f", "lavfi", "-i", "sine=frequency=330:sample_rate=48000",
                        "-t", "60", "-c:v", "libx264", "-preset", "veryfast", "-g", "50", "-pix_fmt", "yuv420p", "-c:a", "aac", "-movflags", "+faststart", clip], check=True)  # fmt: skip
    key = (re.search(r'name="fitagoras-clips" content="([^"]*)"', open(os.path.join(WEB, "index.html")).read()) or [None, ""])[1]
    if not key:
        print("build/web nemá kľúč klipov (clipsWebToken v secrets.properties): klipy sa skontrolujú len ako titulky.", file=sys.stderr)
    # with the CSP the app's server sends with /app/, so what it would block shows up here as well
    server = None if APP else subprocess.Popen(["/usr/bin/python3", "-c", SERVER, str(port), root, key, clip], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    try:
        if not APP and not await serving(port):
            print("Lokálny server sa nespustil.", file=sys.stderr)
            return 2
        url = APP or f"http://127.0.0.1:{port}/app/"
        async with async_playwright() as p:
            if "--len-klipy" not in sys.argv:
                await walk(p, "webkit", "iPhone 13", url)
                await walk(p, "chromium", "Pixel 7", url)
                await timed_test(p, "webkit", "iPhone 13", url)
                await timed_test(p, "chromium", "Pixel 7", url)
                await wide(p, "webkit", url)
                await wide(p, "chromium", url)
            if key or APP:
                await clips(p, "webkit", "iPhone 13", url)
                await clips(p, "webkit", "iPhone 13", url, dark=True)
                await clips(p, "chromium", "Pixel 7", url)
                await clips(p, "chromium", "Pixel 7", url, wide=True)
    finally:
        if server:
            server.terminate()
            server.wait(5)
    print("\nVšetko v poriadku." if not problems else f"\n{len(problems)} problém(y): " + "; ".join(problems))
    return 0 if not problems else 1


sys.exit(asyncio.run(main()))

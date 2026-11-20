// What the Kotlin/Wasm app asks of the browser beyond drawing (shared/src/wasmJsMain, docs/KMP.md, "The web"):
// networking with a readable stream, crypto, the clipboard, photos, sounds, files and the back gesture. Plain functions
// that take and return strings, so the Kotlin side stays a thin list of `js(...)` calls.
"use strict";
window.fitWeb = (() => {
  const b64url = (bytes) => btoa(String.fromCharCode(...bytes)).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");

  // ---- the splash ------------------------------------------------------------------------------------------------
  // index.html's #splash stays until the app has drawn its first frame (web/Main.kt calls ready()). Its bar counts the
  // bytes the app's files have brought so far, out of the total scripts/web.sh writes into the meta "fitagoras-bytes"
  // (without it, a development build, the bar just runs). A long first download says why it takes long; a download
  // that stops, or a failure, gets a sentence and "Skúsiť znova".
  const TEXT = {
    first: "Prvýkrát sa sťahuje celá appka, asi 5 MB. Potom sa otvára hneď, aj bez internetu.",
    stalled: "Sťahovanie stojí. Skontroluj pripojenie na internet.",
    offline: "Appku sa nepodarilo stiahnuť. Skontroluj pripojenie na internet a skús to znova.",
    old: "Tento prehliadač appku nespustí. Treba Safari z iOS 18.2 alebo novšieho, prípadne aktuálny Chrome či Firefox.",
    broken: "Appku sa nepodarilo spustiť. Skús to znova; ak to nepomôže, napíš autorovi.",
  };
  const total = () => Number((document.querySelector('meta[name="fitagoras-bytes"]') || {}).content) || 0;
  let splashOn = true, got = 0, lastByte = Date.now(), failedOnce = false;
  const started = Date.now();
  const el = (sel) => document.querySelector("#splash" + (sel ? " " + sel : ""));
  function say(text, cls) {
    const s = el(); if (!s) return;
    if (cls) s.classList.add(cls);
    el("p").textContent = text;
  }
  function fail(kind) {
    if (!splashOn || failedOnce) return;
    failedOnce = true;
    say(TEXT[kind], "failed");
  }
  function failure(reason) {
    const m = String(reason && (reason.message || reason.name) || reason || "");
    if (typeof WebAssembly !== "object" || /Wasm GC|CompileError|exception handling/i.test(m) || (reason && reason.name === "CompileError")) fail("old");
    else if (!navigator.onLine || /Failed to fetch|NetworkError|Load failed|network connection|offline/i.test(m)) fail("offline");
    else fail("broken");
  }
  const origFetch = window.fetch.bind(window);
  window.fetch = function (...args) {
    const p = origFetch(...args);
    if (!splashOn) return p;
    // a copy of each answer is read alongside only to count its bytes; the app gets the answer itself untouched
    return p.then((r) => {
      if (splashOn && r.ok && r.body) {
        const rd = r.clone().body.getReader();
        const pump = () => rd.read().then(({ done, value }) => {
          if (done || !splashOn) { if (!done) rd.cancel().catch(() => {}); return; }
          got += value.byteLength; lastByte = Date.now(); pump();
        }).catch(() => {});
        pump();
      }
      return r;
    });
  };
  const tick = setInterval(() => {
    const s = el(); if (!s || !splashOn) return;
    const t = total();
    if (t) { s.classList.remove("wait"); el(".bar i").style.width = Math.min(100, Math.round(100 * got / t)) + "%"; }
    if (failedOnce) return;
    const now = Date.now();
    if (now - lastByte > 15000 && now - started > 15000 && !document.hidden && !(t && got >= t)) { say(TEXT.stalled, "slow"); return; }
    s.classList.remove("slow");
    say(now - started > 2500 && !(navigator.serviceWorker && navigator.serviceWorker.controller) ? TEXT.first : "Načítava sa…");
  }, 250);
  addEventListener("error", (e) => { if (splashOn) failure(e.error || e.message); });
  addEventListener("unhandledrejection", (e) => { if (splashOn) failure(e.reason); });
  addEventListener("offline", () => { if (splashOn && got < total()) fail("offline"); });
  addEventListener("DOMContentLoaded", () => {
    el("button").addEventListener("click", () => location.reload());
    if (typeof WebAssembly !== "object") fail("old");
  });
  // the app's first frame is on screen: the splash fades out over it, and counting stops
  function ready() {
    if (!splashOn) return;
    splashOn = false; clearInterval(tick); window.fetch = origFetch;
    const s = el(); if (!s) return;
    s.classList.add("gone");
    setTimeout(() => s.remove(), 250);
    watchCanvas();
  }

  // ---- when drawing stops ------------------------------------------------------------------------------------------
  // The app draws with WebGL. Safari may take the context of a page left in the background, and the app doesn't get it
  // back: the screen would stay empty. The page reloads instead (at once, or when it is looked at again), which returns
  // to the same screen (route() below) with the saved progress. An error after the start may have stopped the app the
  // same way, so it gets a quiet bar with "Načítať znova" rather than an empty screen without a way out.
  function reloadWhenSeen() {
    if (!document.hidden) { location.reload(); return; }
    document.addEventListener("visibilitychange", () => { if (!document.hidden) location.reload(); });
  }
  function watchCanvas() {
    const find = (n) => { for (const c of n.children || []) { if (c.tagName === "CANVAS") return c; const f = (c.shadowRoot && find(c.shadowRoot)) || find(c); if (f) return f; } return null; };
    const canvas = find(document.getElementById("app") || document.body);
    if (canvas) canvas.addEventListener("webglcontextlost", reloadWhenSeen);
  }
  let crashShown = false;
  function crashed() {
    if (splashOn || crashShown) return;
    crashShown = true;
    const bar = document.createElement("div");
    bar.id = "crash";
    bar.setAttribute("role", "alert");
    bar.innerHTML = '<span>Niečo sa pokazilo.</span><button type="button">Načítať znova</button><button type="button" class="x" aria-label="Zavrieť">✕</button>';
    bar.querySelector("button").addEventListener("click", () => location.reload());
    // the app may well go on: then the bar just goes away
    bar.querySelector(".x").addEventListener("click", () => bar.remove());
    document.body.appendChild(bar);
  }
  addEventListener("error", () => crashed());
  addEventListener("unhandledrejection", () => crashed());

  // ---- network -------------------------------------------------------------------------------------------------
  // One request; resolves to JSON {status, rid, text} or {status: 0, error} when there is no connection at all.
  async function request(method, url, headersJson, body) {
    try {
      const r = await fetch(url, { method, headers: JSON.parse(headersJson || "{}"), body: body == null ? undefined : body, redirect: "error", cache: "no-store" });
      return JSON.stringify({ status: r.status, rid: r.headers.get("x-request-id"), text: await r.text() });
    } catch (e) { return JSON.stringify({ status: 0, error: String(e && e.name || e) }); }
  }

  // Server-sent events: every event's data goes to onEvent as it arrives; resolves to {status, rid, text (an error's
  // body), interrupted}. A controller per call, so the Kotlin side can abort it when its coroutine is cancelled.
  const running = new Map();
  let nextId = 1;
  function stream(url, headersJson, body, onEvent) {
    const id = nextId++;
    const ctl = new AbortController();
    running.set(id, ctl);
    const done = (async () => {
      let r;
      try { r = await fetch(url, { method: "POST", headers: JSON.parse(headersJson), body, signal: ctl.signal, cache: "no-store" }); }
      catch (e) { return JSON.stringify({ status: 0, error: String(e && e.name || e) }); }
      const rid = r.headers.get("x-request-id");
      if (r.status !== 200) return JSON.stringify({ status: r.status, rid, text: await r.text().catch(() => "") });
      const reader = r.body.getReader();
      const dec = new TextDecoder();
      let buf = "", data = "";
      try {
        for (;;) {
          const { value, done } = await reader.read();
          if (done) break;
          buf += dec.decode(value, { stream: true });
          let nl;
          while ((nl = buf.indexOf("\n")) >= 0) {
            const line = buf.slice(0, nl).replace(/\r$/, "");
            buf = buf.slice(nl + 1);
            if (line.startsWith("data:")) data += line.slice(5).replace(/^ /, "") + "\n";
            else if (line === "" && data) { const d = data.trim(); data = ""; if (onEvent(d)) { ctl.abort(); return JSON.stringify({ status: 200, rid }); } }
          }
        }
        if (data.trim()) onEvent(data.trim());
        return JSON.stringify({ status: 200, rid });
      } catch (e) { return JSON.stringify({ status: 200, rid, interrupted: String(e && e.name || e) }); }
    })();
    done.finally(() => running.delete(id));
    return { id, done };
  }

  // ---- crypto ------------------------------------------------------------------------------------------------------
  const random = (n) => b64url(crypto.getRandomValues(new Uint8Array(n)));
  async function sha256(text) { return b64url(new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text)))); }
  const uuid = () => crypto.randomUUID();
  const decode = (s) => { try { return decodeURIComponent(s.replace(/\+/g, " ")); } catch (e) { return s; } };
  function base64urlDecode(s) {
    const b = atob(s.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((s.length + 3) % 4));
    return new TextDecoder().decode(Uint8Array.from(b, (c) => c.charCodeAt(0)));
  }

  // ---- clipboard, windows, files ------------------------------------------------------------------------------
  async function readClipboard() { try { return await navigator.clipboard.readText(); } catch (e) { return ""; } }
  // A link in a new tab, the app's own tab stays (window.open with "noopener" returns null even when it opened, so the
  // window is opened blank, cut from this page and then sent on). Blocked: this tab goes there itself.
  function open(url) {
    const w = window.open("about:blank", "_blank");
    if (w) { try { w.opener = null; w.location.href = url; return; } catch (e) {} }
    location.href = url;
  }
  // A window opened now, inside the tap (a browser blocks a later one), and sent to its address when it is known
  // (the sign-in's address needs an async hash first). Blocked anyway: this tab goes there itself.
  let pendingWindow = null;
  function openSoon() { try { pendingWindow = window.open("about:blank", "_blank"); } catch (e) { pendingWindow = null; } }
  function openNow(url) {
    const w = pendingWindow; pendingWindow = null;
    if (w && !w.closed) { try { w.opener = null; w.location.href = url; return; } catch (e) {} }
    location.href = url;
  }

  function download(name, text) {
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob([text], { type: "application/json" }));
    a.download = name;
    document.body.appendChild(a); a.click(); a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 10000);
  }

  // One file chooser; resolves when it closes (with nothing when cancelled: the "cancel" event, or focus returning
  // without a file in browsers that don't send it).
  function choose(accept, multiple, capture) {
    return new Promise((resolve) => {
      const input = document.createElement("input");
      input.type = "file"; input.accept = accept; input.multiple = multiple;
      if (capture) input.setAttribute("capture", capture);
      input.style.display = "none";
      let settled = false;
      const finish = (files) => { if (settled) return; settled = true; input.remove(); resolve(files); };
      input.addEventListener("change", () => finish(Array.from(input.files || [])));
      input.addEventListener("cancel", () => finish([]));
      document.body.appendChild(input);
      input.click();
    });
  }

  async function pickText() { const [f] = await choose("application/json,.json", false); return f ? await f.text() : ""; }

  // Photos of paper: the camera or the library (iOS offers both), each as a JPEG of at most 2048 px on the longer
  // side, upright, without EXIF (a canvas drops it); resolves to the base64 strings joined by ",".
  async function pickPhotos(max) {
    const files = (await choose("image/*", max > 1)).slice(0, max);
    const out = [];
    for (const f of files) {
      try {
        const bmp = await createImageBitmap(f, { imageOrientation: "from-image" });
        const k = Math.min(1, 2048 / Math.max(bmp.width, bmp.height));
        const c = document.createElement("canvas");
        c.width = Math.round(bmp.width * k); c.height = Math.round(bmp.height * k);
        c.getContext("2d").drawImage(bmp, 0, 0, c.width, c.height);
        bmp.close && bmp.close();
        const url = c.toDataURL("image/jpeg", 0.88);
        out.push(url.slice(url.indexOf(",") + 1));
      } catch (e) { /* not an image the browser can decode (HEIC on a desktop): skipped */ }
    }
    return out.join(",");
  }

  // ---- sounds --------------------------------------------------------------------------------------------------
  // Web Audio: the files are fetched and decoded after the first touch (a browser starts audio only after one);
  // on iOS 17+ the "ambient" session keeps to the silent switch like the app does.
  let ctx = null; const buffers = new Map(); let base = "zvuky/";
  function soundsAt(path) { base = path; }
  function unlock() {
    if (ctx) { if (ctx.state === "suspended") ctx.resume(); return; }
    try { if (navigator.audioSession) navigator.audioSession.type = "ambient"; } catch (e) {}
    const C = window.AudioContext || window.webkitAudioContext;
    if (!C) return;
    ctx = new C();
    ["tap", "button", "correct", "combo_1", "combo_2", "combo_3", "combo_4", "combo_5", "correct_alt", "wrong", "wrong_alt", "step_done", "lesson_done", "lesson_done_alt", "streak"]
      .forEach((n) => fetch(base + n + ".wav").then((r) => r.arrayBuffer()).then((b) => ctx.decodeAudioData(b)).then((d) => buffers.set(n, d)).catch(() => {}));
  }
  function play(name, gain) {
    if (!ctx || ctx.state !== "running") return;
    const b = buffers.get(name); if (!b) return;
    const s = ctx.createBufferSource(); s.buffer = b;
    const g = ctx.createGain(); g.gain.value = gain;
    s.connect(g).connect(ctx.destination); s.start();
  }
  ["pointerdown", "touchend", "keydown"].forEach((e) => window.addEventListener(e, unlock, { capture: true, passive: true }));

  // ---- lecture clips ----------------------------------------------------------------------------------------------
  // One <video> for the whole app (screens/Clip.kt's player, web/WebClipPlayer.kt). It lies UNDER the app's canvas:
  // the app clears its pixels where the picture is (a hole the shape of the frame), so whatever the app draws over it
  // (the spinner, captions and controls in fullscreen, a dialog, the speed menu) stays on top, and the canvas masks the
  // rest: the rounded corners, the panels, the edges of a scrolled step. place() gets the frame in canvas pixels and
  // turns it into page pixels from where the canvas really is (a centred column on a wide window, the safe areas).
  // Taps go to the canvas (pointer-events: none). Fullscreen: the iPhone gets its native player (webkitEnterFullscreen,
  // the captions as a text track); other browsers the whole page in fullscreen with the app's own controls over the video.
  // The browser's back button and Esc end it too. A <video> sends no headers: the clip key rides in the address.
  let video = null, layer = null, onClip = null, frame = null, track = null, nativeFull = false;
  const canvasOf = () => { const find = (n) => { for (const c of n.children || []) { if (c.tagName === "CANVAS") return c; const f = (c.shadowRoot && find(c.shadowRoot)) || find(c); if (f) return f; } return null; }; return find(document.getElementById("app") || document.body); };
  function clipVideo() {
    if (video) return video;
    layer = document.createElement("div");
    layer.id = "clip";
    layer.style.cssText = "position:fixed;left:0;top:0;width:0;height:0;overflow:hidden;pointer-events:none;display:none";
    video = document.createElement("video");
    video.setAttribute("playsinline", ""); video.setAttribute("webkit-playsinline", "");
    video.playsInline = true; video.preload = "auto"; video.controls = false; video.disableRemotePlayback = true;
    video.style.cssText = "position:absolute;left:0;top:0;width:100%;height:100%;object-fit:contain;background:#000";
    layer.appendChild(video);
    const app = document.getElementById("app");
    document.body.insertBefore(layer, app || null);
    ["loadstart", "loadedmetadata", "loadeddata", "canplay", "canplaythrough", "playing", "waiting", "play", "pause", "seeking", "seeked",
      "ended", "ratechange", "emptied", "stalled"].forEach((e) => video.addEventListener(e, () => tell(e)));
    video.addEventListener("error", () => { if (video.getAttribute("src")) probe(video.getAttribute("src"), loads); });
    video.addEventListener("webkitbeginfullscreen", () => { nativeFull = true; captionsShown(true); });
    video.addEventListener("webkitendfullscreen", () => { nativeFull = false; captionsShown(false); tell("fullexit"); });
    document.addEventListener("fullscreenchange", () => { if (!document.fullscreenElement) tell("fullexit"); setTimeout(reposition, 50); });
    addEventListener("resize", reposition);
    if (window.visualViewport) window.visualViewport.addEventListener("resize", reposition);
    return video;
  }
  const tell = (e) => { if (onClip) onClip(e); };
  // A video's error says nothing about why: one byte asked for again tells the app what the server said (0: no answer).
  // Only for the load it belongs to: an answer that comes after another clip (or a retry) started is dropped.
  let loads = 0, probing = null;
  async function probe(src, load) {
    if (probing) probing.abort();
    const ctl = probing = new AbortController();
    let status = 0;
    try { const r = await fetch(src, { headers: { Range: "bytes=0-0" }, cache: "no-store", redirect: "error", signal: ctl.signal }); status = r.ok ? -1 : r.status; r.body && r.body.cancel && r.body.cancel().catch(() => {}); } catch (e) { status = 0; }
    if (load === loads && !ctl.signal.aborted) tell("error:" + status);
  }
  const newLoad = () => { loads++; if (probing) { probing.abort(); probing = null; } };
  // iOS lets a page start a video with sound only from a tap; a load() inside the first tap frees this element for good
  // (the app starts the clip a frame after "Pustiť klip", outside the tap)
  let blessed = false;
  function bless() {
    if (blessed) return; blessed = true;
    try { const v = clipVideo(); if (!v.getAttribute("src")) v.load(); } catch (e) {}
  }
  ["touchend", "click", "keydown", "mouseup"].forEach((e) => window.addEventListener(e, bless, { capture: true, passive: true }));
  function reposition() {
    if (!layer || !frame || layer.style.display === "none") return;
    const c = canvasOf(); if (!c || !c.width) return;
    const r = c.getBoundingClientRect(), k = r.width / c.width;
    // the box is the canvas: a frame partly outside it (a step scrolled) is cut by it, never drawn over the page around
    layer.style.left = r.left + "px"; layer.style.top = r.top + "px"; layer.style.width = r.width + "px"; layer.style.height = r.height + "px";
    const [x, y, w, h] = frame;
    Object.assign(video.style, { left: x * k + "px", top: y * k + "px", width: w * k + "px", height: h * k + "px" });
  }
  function captionsShown(on) { if (track) track.track.mode = on && track.dataset.want === "1" ? "showing" : "hidden"; }
  const clipApi = {
    on(cb) { onClip = cb; clipVideo(); },
    load(src) { const v = clipVideo(); newLoad(); v.setAttribute("src", src); v.load(); },
    unload() { if (!video) return; newLoad(); video.pause(); video.removeAttribute("src"); video.load(); },
    play() { const v = clipVideo(); const p = v.play(); if (p && p.catch) p.catch((e) => { if (e && e.name === "NotAllowedError") tell("blocked"); }); },
    pause() { if (video) video.pause(); },
    seek(s) { if (video) { try { video.currentTime = s; } catch (e) {} } },
    rate(r) { if (video) { video.playbackRate = r; video.defaultPlaybackRate = r; try { video.preservesPitch = true; video.webkitPreservesPitch = true; } catch (e) {} } },
    // "paused ready ended seeking": what the player's state is made of (web/WebClipPlayer.kt)
    status() { const v = video; if (!v) return "1 0 0 0"; return (v.paused ? 1 : 0) + " " + v.readyState + " " + (v.ended ? 1 : 0) + " " + (v.seeking ? 1 : 0); },
    time() { return video ? video.currentTime : 0; },
    duration() { return video && isFinite(video.duration) ? video.duration : -1; },
    place(x, y, w, h) { clipVideo(); frame = [x, y, w, h]; layer.style.display = "block"; reposition(); },
    hide() {
      frame = null; if (!layer) return; layer.style.display = "none";
      if (document.pictureInPictureElement === video) document.exitPictureInPicture().catch(() => {});
      try { if (video.webkitPresentationMode === "picture-in-picture") video.webkitSetPresentationMode("inline"); } catch (e) {}
    },
    // the captions of the playing clip as WebVTT, for the iPhone's native fullscreen player
    captions(vtt, on) {
      const v = clipVideo();
      if (track) { URL.revokeObjectURL(track.src); track.remove(); track = null; }
      if (!vtt) return;
      track = document.createElement("track");
      track.kind = "captions"; track.srclang = "sk"; track.label = "Slovensky, automatický prepis"; track.dataset.want = on ? "1" : "0";
      track.src = URL.createObjectURL(new Blob([vtt], { type: "text/vtt" }));
      v.appendChild(track);
      track.track.mode = nativeFull && on ? "showing" : "hidden";
    },
    phone() { return matchMedia("(pointer: coarse)").matches && Math.min(screen.width, screen.height) < 600; },
    canPip() { const v = clipVideo(); return !!(document.pictureInPictureEnabled || (v.webkitSupportsPresentationMode && v.webkitSupportsPresentationMode("picture-in-picture"))); },
    pip() {
      const v = clipVideo();
      try {
        if (document.pictureInPictureEnabled) { (document.pictureInPictureElement === v ? document.exitPictureInPicture() : v.requestPictureInPicture()).catch(() => {}); return; }
        if (v.webkitSetPresentationMode) v.webkitSetPresentationMode(v.webkitPresentationMode === "picture-in-picture" ? "inline" : "picture-in-picture");
      } catch (e) {}
    },
    // true when it went fullscreen (or is on its way), false when the browser refused: the app then fills the window itself
    full(on) {
      const v = clipVideo();
      if (!on) {
        if (nativeFull && v.webkitExitFullscreen) { try { v.webkitExitFullscreen(); } catch (e) {} }
        if (document.fullscreenElement) document.exitFullscreen().catch(() => {});
        return true;
      }
      const d = document.documentElement;
      if (document.fullscreenEnabled && d.requestFullscreen) {
        d.requestFullscreen({ navigationUI: "hide" }).then(() => { try { screen.orientation.lock("landscape").catch(() => {}); } catch (e) {} }).catch(() => {});
        return true;
      }
      if (v.webkitEnterFullscreen && v.readyState >= 1) { try { v.webkitEnterFullscreen(); return true; } catch (e) { return false; } }
      return false;
    },
  };

  // ---- the keyboard ----------------------------------------------------------------------------------------------
  // iOS Safari lays the keyboard over the page instead of shrinking it (Compose's insets see nothing): the app's box
  // follows the visible part of the window, so a text field at the bottom (the tutor's question) stays above the keys.
  if (window.visualViewport) {
    const vv = window.visualViewport;
    const fit = () => {
      const a = document.getElementById("app"); if (!a) return;
      const covered = window.innerHeight - vv.height > 80; // a keyboard, not the toolbar sliding away
      a.style.height = covered ? vv.height + "px" : "";
      a.style.top = covered ? vv.offsetTop + "px" : "";
    };
    vv.addEventListener("resize", fit); vv.addEventListener("scroll", fit);
  }

  // ---- back gesture, window, storage ------------------------------------------------------------------------------
  // The browser's back (the iOS edge swipe, Android's back, the back button) goes to the app's own back: one history
  // entry above the page is kept armed and re-armed after each use.
  // The entry below never carries a #route: going back to it must not look like a link to the screen the app was
  // opened with (route() below keeps the screen in the address of the entry above).
  let popped = 0;
  function onBack(cb) {
    if (!history.state || !history.state.fit) {
      const here = location.href;
      history.replaceState({ fitBase: true }, "", location.pathname + location.search);
      history.pushState({ fit: true }, "", here);
    }
    window.addEventListener("popstate", (e) => {
      // a #route typed or opened in this tab makes a new entry without state: that is a link (hashchange opens it), not back
      if (!e.state) { history.replaceState({ fit: true }, ""); return; }
      popped = Date.now(); history.pushState({ fit: true }, "", routeUrl(lastRoute)); cb();
    });
  }

  // The screen on top is kept in the address (#lesson:<id>, the same routes a link opens), replaced rather than added,
  // so reloading the page comes back to it (a home-screen app starts at its start_url, Dnes); a link changed in the address bar
  // (or another #route opened in this tab) opens it.
  let lastRoute = "";
  const routeUrl = (r) => r ? "#" + r : location.pathname + location.search;
  function route(r) {
    lastRoute = r;
    if (("#" + r) !== location.hash && (r || location.hash)) history.replaceState(history.state, "", routeUrl(r));
  }
  function onRoute(cb) {
    addEventListener("hashchange", () => {
      const r = location.hash.replace(/^#/, "");
      // a hashchange that comes with the back gesture is the back, not a link
      if (r && r !== lastRoute && Date.now() - popped > 300) cb(r);
    });
  }

  // Two tabs of the app share one progress document in localStorage, and each writes its whole copy: a tab left behind
  // would overwrite with its older copy what the other one saved. Once the other tab has saved, this one reloads when it
  // comes forward again (back on its screen, by the address above), so it continues from the newer progress.
  let staleState = false;
  addEventListener("storage", (e) => { if (e.key === "fitest.state") staleState = true; });
  document.addEventListener("visibilitychange", () => { if (!document.hidden && staleState) location.reload(); });

  // The page's own colours under the app (light or dark, as the app's theme): the page, the tone around the app's
  // column in a wide window and the hairline between them (index.html's --bg, --around, --edge); the browser's bars
  // take the page's.
  function chrome(bg, around, edge) {
    const s = document.documentElement.style;
    s.setProperty("--bg", bg); s.setProperty("--around", around); s.setProperty("--edge", edge);
    document.querySelectorAll('meta[name="theme-color"]').forEach((m) => m.setAttribute("content", bg));
  }

  // ---- the column ----------------------------------------------------------------------------------------------
  // A window wider than the app's column (ui/AppColumn.kt) has a tone beside it where nothing scrolls. The wheel or the
  // trackpad there scrolls the column: the event goes on to the app at the column's nearer edge, at the same height
  // (screens keep 20 px of plain page along their edges, so it lands on the page's own scroll, not on a control).
  let columnWidth = 0;
  function column(w) { columnWidth = w; }
  // Compose puts its canvas in the shadow root of a box inside #app, and listens for the wheel on the canvas
  function appCanvas(app) {
    for (const el of [app, ...app.querySelectorAll("*")]) {
      const c = el.shadowRoot && el.shadowRoot.querySelector("canvas");
      if (c) return c;
    }
    return app.querySelector("canvas");
  }
  addEventListener("wheel", (e) => {
    if (!columnWidth || e.fitColumn || e.ctrlKey) return;  // ctrl: the trackpad's pinch, the browser's zoom
    const app = document.getElementById("app");
    if (!app || !(e.target === app || app.contains(e.target))) return;
    const canvas = appCanvas(app);
    if (!canvas) return;
    const r = canvas.getBoundingClientRect();
    const w = Math.min(r.width, columnWidth), left = r.left + (r.width - w) / 2, right = left + w;
    if (e.clientX >= left && e.clientX < right) return;
    e.preventDefault(); e.stopImmediatePropagation();
    const x = e.clientX < left ? left + 10 : right - 10;
    const moved = new WheelEvent("wheel", { bubbles: true, cancelable: true, composed: true, view: window,
      clientX: x, clientY: e.clientY, screenX: e.screenX + (x - e.clientX), screenY: e.screenY,
      deltaX: e.deltaX, deltaY: e.deltaY, deltaZ: e.deltaZ, deltaMode: e.deltaMode,
      shiftKey: e.shiftKey, altKey: e.altKey, metaKey: e.metaKey, buttons: e.buttons });
    moved.fitColumn = true;
    canvas.dispatchEvent(moved);
  }, { capture: true, passive: false });

  async function persist() { try { return navigator.storage && navigator.storage.persist ? String(await navigator.storage.persist()) : "unsupported"; } catch (e) { return "error"; } }
  const standalone = () => window.matchMedia("(display-mode: standalone)").matches || navigator.standalone === true;
  const ios = () => /iPhone|iPad|iPod/.test(navigator.userAgent) || (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);

  return { request, stream, abort: (id) => { const c = running.get(id); if (c) c.abort(); }, random, sha256, uuid, base64urlDecode, decode, encode: encodeURIComponent,
    ready, failed: (m) => failure(m), clip: clipApi, readClipboard, open, openSoon, openNow, download, pickText, pickPhotos, soundsAt, play, onBack, route, onRoute, chrome, column, persist, standalone, ios };
})();

# FITagoras on Compose Multiplatform

One codebase for Android, the desktop JVM (unit tests, screenshots without an emulator) and the browser (wasmJs,
https://fitagoras.hamper.dev/app/, "The web" below). The Android app is the same app: `dev.hamper.fitest`, the same key, the same DataStore file,
the same androidx versions at runtime.

## Where things are

| Path | What |
|---|---|
| `shared/src/commonMain/kotlin/dev/hamper/tabula/` | content (`data/`), exact maths (`la/`), the formula engine (`math/`), progress (`state/`), every screen (`screens/`, `ui/`, `App.kt`) |
| `shared/src/commonMain/composeResources/` | drawables the UI draws, the two fonts, `files/` (clips, coverage register) |
| `shared/src/commonMain/kotlin/.../compat/` | the Android and JVM calls common code makes, under their Android names (below) |
| `shared/src/commonMain/kotlin/.../platform/` | what screens ask of the device: widget, reminder (`Platform`), shipped files (`Assets`) |
| `shared/src/androidMain/` | the Android side of `compat` (each is the framework call itself), ExoPlayer (`ExoClipPlayer`), Telemetry |
| `shared/src/skikoMain/` | the desktop and web side: Skia drawing for the formula engine, fonts from bytes, no-op shims |
| `shared/src/jvmCommonMain/` | Android + JVM only (java.io): TelemetryCore |
| `shared/src/jvmMain/`, `wasmJsMain/` | desktop screenshots (`desktop/Snap.kt`); the web: entry point `web/Main.kt`, `platform/Web.kt` (storage, backup, `WebPlatform`), `tutor/Web*.kt`, `resources/` (index.html, web.js, manifest, icons) |
| `shared/src/jvmTest/` | every unit test, plain JUnit 4 on the JVM |
| `app/` | MainActivity, FitestApp (the Application class), AndroidPlatform (DataStore, widget, reminder), notifications, widget, shortcuts, manifest, Android res, the Robolectric screenshots (`app/src/test/.../snap`) |

## Rules for new code

- Common code imports no `android.*`, `java.*` or Android-only `androidx.*` (activity, core, media3, work, glance,
  datastore). Use the compat names: `dev.hamper.tabula.compat.time.*` for java.time, `compat.json.JSONObject` for
  org.json, `compat.SystemClock`, `compat.BackHandler`, `compat.LocalView` (haptics, keep screen on),
  `compat.media.Player`, `compat.graphics.*` (formula engine), `compat.JMath.multiplyExact`, `currentTimeMillis()`.
  On Android each of these is the same call as before; that is why the app behaves identically.
- Something the device does (a permission, a notification, the widget): a method on `platform.Platform`, implemented in
  `app/.../AndroidPlatform.kt`, default no-op for the desktop and web.
- Icons: `Res.drawable.ic_x` (put the vector XML into `composeResources/drawable/`); files: `Assets.read("x.json")`.
- Fonts: `fontFile(...)` for Compose text, `loadTypeface(...)` for the formula engine; both read `composeResources/font`.
- Plain text: `dev.hamper.tabula.ui.Text`, not Material's (import it instead of `androidx.compose.material3.Text`). It
  sets Slovak typography (`nb()`: no one-letter word at a line's end, a number keeps its unit, a date or a range stays
  whole) and lets `ButtonLabelTest` see every break; text with math goes through `Rich`, which does the same.

## Checks

- Content and logic: `./gradlew :shared:jvmTest --tests '*.YourTest'` during work, the whole `:shared:jvmTest` once
  before handing over (plain JVM, no Android compile; levels in docs/SYSTEM.md, "Verification"). Tests that need
  Android (Robolectric: answer flows, GEM layout, the screenshot tasks) stay in `:app` and run before a release or when
  their area changes.
- Screenshots without an emulator:
  - `./gradlew :app:snap …` (Robolectric, Android's own text stack; docs/SYSTEM.md) is the reference for how a screen
    looks: it renders the same pixels before and after the move to Compose Multiplatform.
  - `./gradlew :shared:snapDesktop -Psnap.route=… -Psnap.out=…` (same options) renders with Skia on the desktop JVM in
    a few seconds, a preview and the closest thing to the web: same line breaks, fonts and formulas, but line heights
    differ by a few dp, so it does not replace `:app:snap` for checking the look.
- The browser: `scripts/web.sh` (the production build into `build/web/`, below), then serve that folder (any static
  server, e.g. `python3 -m http.server`); `#lesson:<id>` in the URL opens a lesson. For quick work the development build
  (`:shared:wasmJsBrowserDevelopmentExecutableDistribution`, `shared/build/dist/wasmJs/developmentExecutable/`) skips
  Binaryen.
- Android: `:app:assembleDebug` as before.

## The web

Production build and the folder to upload: `scripts/web.sh` → `build/web/` (Binaryen-optimized wasm, the main font
pinned to the axes the app uses, `sw.js` for offline, `.br`/`.gz` copies). Served by the app's server at
https://fitagoras.hamper.dev/app/ (only the maintainer uploads). Measured
18. 11. 2026 (Chrome, throttled): the first load is 4.7 MB with brotli (two wasm files 1.9 + 2.6 MB, fonts 0.6 MB,
content 0.1 MB) and shows the app after 1.3 s on Wi-Fi, 4.8 s on 4G, 25 s on 3G; a later start comes from the service worker:
0.3 s without network limits, nothing downloaded; online it still asks for each file (a 304 when unchanged, the wasm
files not at all), offline it opens from the cache.

While it downloads, `index.html` shows a splash (the icon, the name, a bar): `web.js` counts the bytes the app's files
bring against the total `scripts/web.sh` writes into the meta `fitagoras-bytes`, says on a first visit why it takes long,
and turns a failed or stalled download (or a browser without Wasm GC) into a sentence with "Skúsiť znova". `web/Main.kt`
fades it out after the app's second frame, so nothing blinks between them. The screen on top is kept in the address
(`#lesson:<id>`, the routes of `AppState.open`), replaced rather than added, so a reload comes back into the lesson.

Before an upload: `uvx --with playwright==1.64.0 python scripts/web-check.py` serves `build/web/` and walks splash →
Dnes → lesson → reload → back → offline in WebKit (iPhone) and Chromium (Android); exit code 0 only when all holds and
the console stays clean. Upload the hashed wasm and the resources before `index.html`, `fitest.js` and `sw.js`, so
nobody gets a page that asks for wasm files not yet on the server.

What the web leaves out (`Platform.isWeb`, the rows and buttons are not shown), and why:

| Android | web | why |
|---|---|---|
| in-app update | none | the page is always the newest upload |
| reminder notification, widget, shortcuts | none | no background work or home-screen widget in a browser |
| haptics | none | iOS Safari has no vibration; the sounds still play (Web Audio, `web.js`) |
| statistics, error reports (the flag) | none | they go with the same key; a report that only stays in the browser would mislead |
| progress | localStorage of this browser, "Stiahnuť zálohu / Obnoviť zo zálohy" in Settings | Safari deletes a site's storage after 7 days without a visit unless the app is on the home screen |
| tutor | the same, sign-in finished by pasting the browser's last address | docs/TUTOR.md, "Web" |

Lecture clips play as on Android, through the same `Player` (web/WebClipPlayer.kt, `fitWeb.clip` in web.js): one
`<video>` lies under the canvas, and the clip's frame clears the canvas there, so the app's own drawing (the spinner, the
fullscreen captions and controls, dialogs, the speed menu) stays on top and the canvas hides the rest; the video is
placed from where the canvas really is, so a centred column or the safe areas move it along. The web has its own clip
key (`clipsWebToken` in `secrets.properties`, put into `index.html` by `scripts/web.sh`; public by design like the
APK's, the server opens only clips with it and can swap either key on its own); a `<video>` sends no headers, so the key
goes in the clip's address (`&key=`). Differences from Android: fullscreen on an iPhone is Safari's own player (the
captions as a text track); elsewhere the page goes fullscreen with the app's controls, and turning the phone sideways
fills the window without the browser's fullscreen (a browser allows that only from a tap). Picture-in-picture where the
browser has it (the button is hidden otherwise). A clip starts only after a tap (a reload straight into it waits paused).
Without a key in the build, or offline, the captions are there as on Android ("Radšej len prečítať titulky", and the
list under the player).

A window wider than a phone (the web on a computer, a tablet, a phone on its side) shows the app as a column 480 dp
wide in its middle, top to bottom, with a tone one step darker around it (`ui/AppColumn.kt`, Teo 20. 11.). It is one
place: the root of `App` and the dialogs that are screens of their own (a full card, the tutor, marking lessons done)
draw through `AppColumn`; an `AlertDialog` takes `Modifier.widthIn(max = DialogWidth)` and a `ModalBottomSheet`
`sheetMaxWidth = AppWidth`, since those are windows of their own. Only the clip in fullscreen and a tutor photo opened
large fill the whole window. On the web the splash stands in the same column (index.html) and the wheel beside the
column scrolls it (web.js, "the column").

Browser back (the iOS edge swipe, the back button) is the app's back (`web.js` keeps one history entry armed). Safe
areas come from CSS (`viewport-fit=cover`, `env(safe-area-inset-*)` on `#app`), not from Compose insets.

# How FITagoras is built and checked

Content rules are in [CONTENT.md](CONTENT.md), the coverage register in [COVERAGE.md](COVERAGE.md). This file is how
work is verified. Checks are as small as the change allows: one Gradle build at a time on an ordinary laptop is the
budget.

## Verification (Teo, 15. and 17. 11. 2026)

How the app is checked: a few broad checks that catch real bugs everywhere, not a pile of tests that each guard one past fix.
Why (17. 11.): in 1.3.13 every test passed while the "Dokazuješ" bar printed the word "textstyle" (the typesetter did not know
`\textstyle`), and work slowed down because every fix got its own test and every step ran the whole suite, screenshots
included.

**What a check is**

- **A few broad checks over the whole content**, each run over every course, lesson, card, generator (many seeds) and
  paper: every formula is drawn only with commands the typesetter knows, including the ones the statement model
  assembles (the core and parts of the "Dokazuješ" bar), and every display formula goes through the line breaking
  without failing: `MathSourceTest`; every generator over many seeds, its math against the engine: `TemplatesTest`,
  `ExplanationsTest`, `GemWalkTest`, `ConstructTemplatesTest`; how math and solutions are written: `MathStyleTest`;
  terminology: `TerminologyTest`; statements and reading: `WordingTest`, `ReadingTest`; coverage: `CoverageTest`; the
  layout on a phone (Android, below): every formula fits its card at 320 dp and 200 % (`OverflowScan`), the first answer
  is on the first screen at 411 dp (`snapFold`), no button label is cut at 360 dp and 200 % (`ButtonLabelTest`).
- **Do not add a test for each fix.** A new class of bug: extend the broad check that should have caught it (more
  content in its corpus, one more rule in its scan) so it finds every instance in both courses; a new broad check only
  when none fits.
- **No tests that mirror the code** (compute the same thing the same way again), **pin a text** (`assertEquals` on a
  sentence or a label) **or assert that a removed thing is gone.** Concrete numbers are fine where they come from an
  independent source (a textbook example, brute force).
- **Look at the rendered screens of what you changed** before handing over (`:app:snap`, below: light and dark, 320 dp,
  and 200 % font for text or layout changes). A raw TeX command ("textstyle", `\mathbb`, `§`), a formula falling apart
  into pieces, a cut or overlapping line is a bug the tests may not see.

**When to run what.**

| When | What | Command |
|---|---|---|
| During work | only the test classes you touched or whose area you changed; screenshots of the changed routes | `./gradlew :shared:jvmTest --tests '*.PapersTest'` (repeat `--tests` for more classes; one method: `--tests '*.PapersTest.pointsAddUpPartByPart'`). An Android-only class: `./gradlew :app:testDebugUnitTest --tests '*.GemStageLayoutTest'`. Screens: `./gradlew :app:snap -Psnap.route=lesson:<id>:<step> -Psnap.out=/tmp/x` |
| Once before handing over | the whole JVM suite and the Android compile | `./gradlew :shared:jvmTest :app:assembleDebug` |
| Before a release, or when you change their area | Robolectric, screenshot checks, button labels, the catalogue | `./gradlew :app:testDebugUnitTest`, `:app:snapGolden`, `:app:snapFold`, `:app:snap -Psnap.test=OverflowScan*`, `:app:snap -Psnap.test=ButtonLabelTest`, `:app:snapCatalogue -Psnap.out=/tmp/cat`; the LLM checks (below) |

"Their area": `snapFold` after a change to a question screen or what pushes its answers down; `OverflowScan` after a
change to the typesetter or to statements, cards and display formulas; `ButtonLabelTest` after a change to a button or
its label; `snapGolden` after a change to the shared look (theme, cards, fonts); the Robolectric class of a component
(`GemStageLayoutTest`, `TableStageLayoutTest`, `AnswerFlowTest`, `PartInViewTest`, `MarkupInLabelsTest`) after a change
to that component. Then that one task or class, not the whole release set.

**Speed.** Keep the Gradle daemon up between runs (no `--no-daemon`). The configuration cache and the build cache are on (`gradle.properties`):
an unchanged task is up to date or comes from the cache, so a second run of the same command takes seconds; `--rerun`
forces a task to run again. JVM and Robolectric tests run in 2 parallel forks of 2 GB (`-Ptest.forks=1` when memory
is short). Measured on 17. 11. (16 GB machine under load, warm daemon): one class 5 to 17 s, the whole
`:shared:jvmTest` about 2.5 min, `:app:testDebugUnitTest` about 4 min, an unchanged rerun 2 s. Most of the JVM suite is
`LessonFlowUiTest` (~80 s: the desktop formula drawing shapes every glyph on every frame) and `CoverageMutationTest`
(~40 s).

**Other rules**

1. **Taps on the desktop JVM, the emulator only for what needs Android** (Teo, 17. 11.): taps, navigation, back,
   resuming and what an answer does are a headless Compose UI test in `shared/src/jvmTest` (`runComposeUiTest` +
   `performClick`, the real common screens; back through the shared handler, `compat.SystemBack.press()`; which event
   plays which haptic pattern through `ui.HapticTrace`), no emulator. Example:
   `screens/LessonFlowUiTest.kt`. The emulator only for what needs a real Android: the system back gesture, clip
   playback, install and update, notifications, the widget, how a haptic feels (Teo on his phone). Then walk only the
   lessons you changed (`scripts/walk.py lesson <id>`), never the whole app. The full app walk happens once per release.
2. **Reviews** need no build: they read the diff.

## Before saying ready

1. The levels above: your classes during work, then once `./gradlew :shared:jvmTest :app:assembleDebug` with a
   real exit code 0 (never hidden behind `| tail`), and the release-level tasks of the area you
   changed.
2. Screenshots of every screen you changed, looked at (light and dark, 320 dp; 200 % for text and layout).
3. Merge `main` into your branch, then open a pull request against `main` ([CONTRIBUTING.md](../CONTRIBUTING.md)).

## Where the code is (Compose Multiplatform, since 1.3.6)

One codebase for Android, the desktop JVM and later the browser; details and the rules for new code in
[KMP.md](KMP.md). Content, maths, the formula engine, progress and every screen are in
`shared/src/commonMain/kotlin/dev/hamper/tabula/` (the paths below are relative to it); fonts, drawables and the
clip files in `shared/src/commonMain/composeResources/`; the Android side of a device call in `shared/src/androidMain/`
(the in-app update `system/Updater.kt`, ExoPlayer, Telemetry); `app/` keeps MainActivity, the widget, notifications,
the manifest and the Robolectric tests and screenshots. Common code imports no `android.*` or `java.*`: it uses the
`compat` names or a `platform.Platform` method.

## Releases

One release per finished outcome, as soon as its gate is green: the changed parts get the full gate (every changed
lesson walked with a screenshot per step, its pictures played, light, dark and 200 %), everything else a short smoke
walk (one lesson per part), then the update from the public APK with progress kept. Signing, upload and the hash and
certificate check: [VYVOJ.md](VYVOJ.md), "Kľúče a vydanie". Package name in scripts: `scripts/app_id.py`, never a literal.

## Screenshots without the emulator

Static screens render on the JVM (Robolectric native graphics + Roborazzi: the real Android text and Canvas, our fonts) in seconds and need no emulator:

1. `./gradlew :app:snap -Psnap.route=lesson:dml-mz-mnoziny:3,concept:<id> -Psnap.out=/tmp/x`: PNGs like `lesson_dml-mz-mnoziny_3-light-427-1.0.png` (route, theme, width in dp, font); a wrong id fails instead of showing another screen. Routes: `home`, `cesta`, `nacvik` (the tab Test), `priklad:<n>` (a problem of the test), `cvicenia` (every exercise), `lesson:<id>:<step from 0>`, `concept|theorem|proof|clip:<id>`, `gen:<id>`, `paper:<id>:<n>`, `settings`, `where`, `a>b` (b on top of a); lessons open before their lecture too.
2. Options: `-Psnap.theme=light,dark`, `-Psnap.width=pixel,360` (pixel = the emulator's Pixel, 1281×2856), `-Psnap.font=1.0,1.3,2.0`, `-Psnap.height=<dp>` (taller window, to see a whole step), `-Psnap.course=dml`. Every combination is rendered.
3. Your own composable: a class in `app/src/test/java/dev/hamper/tabula/snap/` that extends `Shots` with `@Test fun x() = shoot("name") { MyPic(...) }`, run with `-Psnap.test=YourShots`. Don't commit scratch shots.
4. `./gradlew :app:snapCatalogue -Psnap.out=/tmp/cat [-Psnap.only=dml-tc] [-Psnap.steps=3]`: every step type (`types/`) and the first steps of every lesson of both courses, light and dark (164 PNGs, ~35 s).
5. `./gradlew :app:snapFold [-Psnap.only=<lesson or generator id part>] [-Psnap.seeds=5]`: every question step and generator at 411 dp / 100 %; fails when the first answer is below the fold (docs/CONTENT.md, "The answers are on the first screen"). Run it after changing a question screen or its content.
6. `./gradlew :app:snapGolden`: a few stable screens against `app/src/test/snapshots` (off in the ordinary `test`); a failure leaves `*_compare.png` in `app/build/outputs/roborazzi`, `-Psnap.record=true` accepts an intended change.

Same as the emulator (SSIM 0.998 on the same lesson steps): layout, fonts, math, Canvas pictures, colours. Different: the status and gesture bars are empty (no clock, icons or handle), the image is 1 px wider, the state is a fresh install (no progress; the course is `-Psnap.course` or the default), a clip step shows its locked card unless the worktree has `secrets.properties`. Still not here: taps (a desktop Compose UI test, "Verification", other rules 1); the emulator for clips playing, install and update, notifications, the widget and the system back gesture.

## How content is produced

**Principle (Teo).** Lessons and exercises are outputs of a system: the content model, shared components,
generators, the rules in docs and the tests. Never fix one example. Fix the system so the
problem cannot happen anywhere, regenerate or convert everything through it, and extend the broad check that should
have caught it so it fails if the problem comes back anywhere ("Verification" above), not a test for the one item.

## The pipeline

0. **Context.** [ZASADY.md](ZASADY.md) and the course and chapter packs in `kurz/` (what the lectures say, procedures,
   notation, typical problems, with timestamps) are the first input of every content change.
1. **Lectures and skriptá.** What was lectured (captions, slides, the textbook sources) decides what exists and how it
   is named (`docs/CONTENT.md`, "Only what has been lectured", "Terminology from the course").
2. **Content model** (`data/Model.kt`, `data/Postup.kt`): lessons and their steps, concepts, theorems, proofs, papers,
   exercises. A method is a `Postup`; a solution is steps under its subgoals.
3. **Generators** (`data/*Templates.kt`, `data/dml/*Gen.kt`): numbers drawn, everything computed by code, text written
   through the shared formatter (`data/Terms.kt`), solutions through the Postup.
4. **Shared components** render it the same everywhere: `screens/Solution.kt` (`SolutionView`, `PostupCard`),
   `ui/Goals.kt` (goal cards shared with proofs), `screens/GemWalk.kt` (every GEM chain, the matrix in place),
   `Rich` for text with math, the reading sheet for symbols and
   references (`ui/Reading.kt`).
5. **Broad checks** encode the rules over all content: `MathSourceTest` (every formula drawable), `MathStyleTest`
   (formatting, Postup, sentence length), `TerminologyTest`, `TemplatesTest`, `GemWalkTest`, `ExplanationsTest`,
   `FirstSentenceTest`, `WordingTest`, `ReadingTest`, `CoursesTest`, `LecturedTest`, `CoverageTest`.
6. **Look and review**: screenshots of every changed screen (`:app:snap`), a code review and an independent content
   review of math and Slovak.

## LLM content checks (kontroly/)

The principles in [ZASADY.md](ZASADY.md) as checks in markdown (`kontroly/*.md`, one per check, grouped by the
`Skupina:` line), run by the Codex CLI over the whole app instead of more UI tests (Teo, 16. 11.). Codex runs read-only and signed in
with a ChatGPT account (`codex login`); the script removes API keys from its environment, so nothing is billed per
token.

- **When:** before a release, or overnight after a batch of content changes; not on every change. The report is a
  work list for an audit, never a gate: verify each finding before fixing it (ZASADY, „Postup tvorby lekcie“, step 5),
  and fix the layer that let it through (above).
- **Run:** `scripts/llm-kontrola.sh [--zabery] [--kapitoly la1-k2,dml-tc] [--skupiny postup,matematika] [--out DIR]`.
  It exports the text (`./gradlew :shared:exportContent -Pexport.out=<dir>`: every lesson step, card, proof, past paper
  and two exercises of each generator, with routes and `file:line`, the „Prečo?“ split and what each reference opens),
  with `--zabery` also renders every lesson step at 320 dp (`:app:snap`) for the `sadzba` check, then runs one Codex per
  chapter × group (with `--zabery` one per lesson for `sadzba`), 4 at a time (`--paralelne`), and writes
  `report.md`, `nalezy.tsv` and `nalezy.json` to the output folder (default
  `~/.fitagoras/llm-kontrola-<date>`, outside the checkout, so reruns from another checkout reuse it). A rerun with the same `--out` skips finished runs; `--len-report`
  only rebuilds the report.
- **How long** (first run, 17. 11., 1.3.11, both courses): about 50 min with `--zabery` (export 1 min, 663 screenshots
  ~12 min, 60 Codex runs of 1 to 3 min each, 4 at a time), about 15 min without. That run found 453 findings; on a random sample of 20, 16 were right, 2 partly (80 to 90 %).
  Severity is the model's guess: P1 is sometimes inflated, so rank by the verified ones.
- **A new check:** a new `kontroly/<name>.md` with the `Skupina:` and `Vstupy:` lines, what to look for, an example of
  the error from Teo's findings and what is not an error; the script picks it up.

## When something is wrong

Find which layer let it through (a missing rule, a component that allows it, a generator outside the formatter), fix
that layer, convert everything through it, extend the broad check that should have caught it (so it finds every
instance in both courses), and write the rule into `docs/CONTENT.md`. Not a new test for the one item ("Verification").

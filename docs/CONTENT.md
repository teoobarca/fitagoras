# Adding content (courses, topics, lessons)

**Read first:** [ZASADY.md](ZASADY.md) (the content rules Teo approved, with examples, and how a lesson is made), then
the course and chapter packs under `kurz/` it points to. This file is the technical layer under them.

**Fix the system, not the example.** Lessons and exercises are outputs of a system: the content model, shared
components, generators, these rules and the tests. We never fix one example: we fix the
system so the problem cannot recur anywhere, regenerate everything through it, and extend the broad check that should
have caught it (the formula scan, the generator sweeps, terminology, wording, coverage) so it finds every instance in
both courses. A fix that patches a single item is sent back or generalised before it is merged. **No test per fix**,
no test that mirrors the code, pins a text or asserts that a removed thing is gone ([SYSTEM.md](SYSTEM.md),
"Verification").

The app has courses (`data/Courses.kt`: `La1`, `Dml`). A course's content comes from **topics**. A topic is one Kotlin
file (or a few files next to it named `<Topic>*.kt`) with everything about one theme: lessons, definitions, theorems,
proofs, test templates and its problems of the past papers. Several authors may work in parallel, so:

**Add content by adding or filling your own topic file only.** Do not edit other topics, `Courses.kt`, `Dml.kt`,
`Model.kt` or shared screens in the same change; if you need a change there, make it a separate change (or ask the
maintainer in an issue).

| course | topic file | topic id |
|---|---|---|
| DML | `data/dml/Induction.kt` (`DmlInduction`) | `dml-mi` |
| DML | `data/dml/Sets.kt` (`DmlSets`) | `dml-mz` |
| DML | `data/dml/Numbers.kt` (`DmlNumbers`) | `dml-tc` |
| LA1 | `data/la1/Chapter3.kt` (`La1Chapter3`), later chapters next to it | `la1-k3` (`la1-k4`, ...) |

LA1 kapitola 2 (`Lessons.kt`, `Concepts.kt`, `Theorems.kt`, `Papers.kt`, `Templates.kt`) predates topics; leave it alone.
A new LA1 chapter file gets one line in `La1Topics` (Courses.kt); that one line is the only shared edit allowed.

## Domain before the answer

Every domain-dependent question, picture task and generated exercise declares its field, space, modulus or number
set in the visible question, subtitle or caption before answering ([ZASADY.md](ZASADY.md), “Teleso a obor sú súčasť
zadania”). A lesson-opening convention is insufficient for a step reviewed alone. Use the course notation, e.g.
`nad §\mathbb{R}§`, `v §\mathbb{Z}_5^4§`, `x,y,k\in\mathbb{Z}`; declare index bounds separately. A defined task
name may provide its mathematical domain contract (a Diophantine equation has integer unknowns).

`DomainTest` walks both complete course registries, standalone concept judges and formal cards, all clip files, papers and
every generator over many seeds and valid prior-answer branches. It uses typed pictures and answers and numerical
task/feedback evidence to find domain-sensitive tasks, then checks **only the pre-answer premise**. Feedback, hints,
solutions and hidden `p`/`Field` metadata cannot supply it. Generic field facts use their visible `T` hypotheses;
undecidable cases have explicit reasoned exceptions, never a blanket skip for a course or task type. Numerical
examples need their own field even when an earlier formal definition quantifies over a generic `T`.

## Coverage: start from the sources (`assets/coverage/`, `CoverageTest`)

A topic is done when every question its sources ask can be practised without help, not when it has lessons.

- **Inventory first.** Before building a topic, every sub-question of its sources goes into
  `assets/coverage/sources.json` (tests, exams, rozstřel, MARAST problems, textbook questions and exercises, lecture
  and proseminar examples), each with the skill it needs. A recording (a lecture, a proseminar video) is entered one
  worked example or method at a time, anchored to the second it starts (`SourceKinds` in `data/Coverage.kt`: the id
  ends with that second, the ref names that second (a proseminar's link opens it there, a lecture recording is cited
  by its id and `t=<second> s`), the anchor gives the span from that second), with what only the board shows
  in its note; never copied captions. A source known only by its id or title is `id-only`, never "covered";
  an unsure transcription stays `uncertain` and never becomes a generator's oracle before the original is checked.
- **Capabilities are different skills.** Defining, stating, computing, counting, deciding, constructing, enumerating
  all objects, proving and judging several statements (✓/–/✗) are separate skills, each in its own domain
  (ℝ is not ℤₚ). Counting bases does not cover counting subspaces; one correct example does not cover "all of them";
  a single-choice question does not cover a statements question.
- **Coverage is a linked independent practice, not a citation.** Each skill in `assets/coverage/skills.json` links
  concept → explanation → practice without help → a unit test of its math. Without help means: a generator offered on
  its own (practice tab or a lesson's last problem) whose graded part asks it and whose answer is not always the same,
  a past-paper problem with a rubric, a definition with idea units (from memory), a proof with a checklist. A check, an
  ask, a worked example or a proof's next line is at most guided. Each link names the words its graded question asks
  (`asks`); a skill may fix its own wording (`signature`) so no link can claim a neighbouring count; a link whose
  content does only part of the skill is capped (`max: guided`). A question over ℤₚ is not covered by practice over ℝ.
- **Done = `CoverageTest` green**: every sub-question in scope (`CoverageScope`) is practised independently, or its
  skill has an exception naming who builds it, when recorded, when due and how many sub-questions it leaves open
  (`open`; one more open fails, so an exception never hides a regression). When the gap is in the model or a shared
  screen (a new answer type), the owner is the maintainer. A past due date fails; remove the exception in the change
  that adds the practice (a stale one fails too). Ranked gaps: `docs/COVERAGE.md`; the current report:
  `shared/build/reports/coverage.txt` after `:shared:jvmTest --tests '*.CoverageTest'`, one line per gap.

## Ids

Every id in a topic starts with the topic id and a dash: lessons `dml-mi-slaba`, steps are made from the lesson id
automatically (`dml-mi-slaba-3`), concepts `dml-mi-princip`, theorems `dml-tc-mfv`, proofs `dml-mi-p-matice`,
generators `dml-tc-inverse`. Ids are stored in the students' progress: **never rename an id once released.**
`CoursesTest` fails on a duplicate or unprefixed id.

## The DSL

`import dev.hamper.tabula.data.*` and `import dev.hamper.tabula.data.dsl.*` (`data/dsl/Dsl.kt`). Text uses `§...§` for
math (it becomes `$...$`); raw strings `"""..."""` need no escaping. Any `^` or `\` outside `§...§` fails the tests
(a formula shown raw). Display formulas in `x()` and `b()` are raw TeX without `§`.
A tall formula inside a line of text (a matrix, a fraction) opens the line up around it (`inlineSlot` in
math/InlineBreaks.kt), and a sum, union or product in running text keeps its limits to the side, as TeX's textstyle;
still, a matrix bigger than 2×2 reads better as a display formula (`x()`, `b()`) of its own. A TeX command the engine
cannot draw fails `MathSourceTest` with the list of formulas; teach the engine the command (`math/`) in a separate change. Display math shrinks to 75 % at most and
then scrolls inside its own box; `./gradlew :app:snap -Psnap.test=OverflowScan*` renders every lesson step at 320 dp and
200 % and fails on any formula wider than its card. The debug route
`tabula://tex` shows the samples.

```kotlin
val DmlInduction = Topic(
    id = "dml-mi",
    lessons = listOf(slaba, silna),
    concepts = listOf(princip), theorems = listOf(slabyPrincip), proofs = listOf(maticeProof),
    generators = mapOf("dml-mi-sum" to ::sumExercise), templates = listOf(TemplateInfo("dml-mi-sum", "Súčet indukciou", "...")),
    paperProblems = mapOf("dml-2025-e" to e1, ...),       // one per paper in DmlPaperSpecs where this topic appears
    generatedProblem = { r -> newProblem(r) },            // problem of a generated paper, new numbers
    checkpoints = listOf(Checkpoint("dml-mi-silna", "dml-2025-k", 1, "Príklad 1 z písomky", 10)),
    planPapers = listOf("paper:dml-2025-e:1"),
)

private val slaba = lesson("dml-mi-slaba", "Slabá indukcia", 14, "Čo sa naučíš, jedna veta.", art = 2, topics = listOf("MI1", "MI2")) {
    ask("...", "A", "B", correct = 0, after = "...", wrong = mapOf(1 to "..."))
    explore(...); explain(...); worked(...); check(...); def("dml-mi-princip"); practice("dml-mi-sum", solo = true)
}
```

Inside a topic, refer to your own definitions by the `val` (e.g. `formal = princip.czech`), **not** through
`ConceptById` or other global maps: those are built from all topics and would be read while your file initialises.

`topics` of a lesson are schedule codes: DML `MI1 MI2 MI3`, `MZ1 MZ2`, `TČ1 .. TČ4`; LA1 slide sets `S3 .. S6`. They tie the
lesson to the lectures in "Kde sme"; a code that no lecture of the course has fails the tests.

## Only what has been lectured (Teo's rule)

**New lessons only for topics already lectured in at least one parallel; build them from that lecture.**

- Any parallel counts (Kleprlík's or Krásenský's lecture on Frobenius backs a lesson even if Klouda is behind). Use its
  captions, clips and what the lecturer stresses.
- `topics` of a lesson name what it really needs (a lesson on inverses modulo m is `TČ3`, not `TČ2`): `LecturedTest`
  checks that a lecture on each of them has taken place by `LecturesCheckedAt` (data/Courses.kt).
- A lesson built before its lecture stays in the code but out of the app: it is listed in `NotYetLectured`
  (data/Courses.kt) with the evidence, and removes it once a lecture covers the topic. What only such a lesson leads to
  (definitions, templates, clips, checkpoints, past paper problems) is left out with it.

## A lesson (the quality bar of LA1 1.2.x)

- 15 to 25 steps, in the order: a question before any explanation (`ask`), an interactive picture (`explore`), the
  explanation (`explain`, with `formal` = the Czech text it rests on and `src`), a worked example (`worked`, line by
  line with a short Slovak note why), your attempt (`check`, `practice(solo = false)`), and at the end a problem
  without help (`practice(solo = true)` or a last `check`). `CoursesTest` checks the shape.
- Definitions in three layers (`Concept`: exact Czech text, `why` per condition, `judge` examples and non-examples with
  `because` and `wrong`, `units` for the check from memory (only what `ask` asks, the rest in `extra`), `ask` as the
  test asks it); every concept of a topic is taught by a `def(...)` step in one of its lessons.
- Theorems with proofs line by line (`Proof` with rounds; each wrong line says why) when the course wants the proof,
  otherwise a short proof or the reason there is none ("Statements and proofs").
- **Every wrong option says why it is wrong** (`wrong = mapOf(index to "...")` on `ask` and `check`, `wrong` on every
  `judge`, `why` on every wrong proof line, `diagnose` on every `Answer.Numbers`, `wrong` on every `Answer.Choice`).
- **First sentence rule.** After an answer the app shows only the first sentence of each explanation (`after`, `why`,
  every wrong-option text, `because`, `wrong`); the rest is behind "Prečo?". So every explanation starts with one short
  sentence (at most about 15 words) that stands alone as the verdict and the key reason; details follow in later
  sentences. In that first sentence avoid abbreviations ending with a period, except "napr.", "tzv.", "Def." and
  numbers like "Veta 2.7".
- Generators: numbers drawn, then everything asked and checked is computed by code (exact integers, fractions, modulo
  m), never typed by hand. A generator returns an `Exercise`; `ExplanationsTest` runs every generator 120 times and
  checks that every wrong answer gets an explanation.

## The answers are on the first screen

On a question step (Ask, Check, a generated exercise) the first answer option is whole on the first screen of Teo's
phone (411 × 914 dp, 100 % font) without scrolling. Nothing optional may push it down: closed, "Ako na to?" is one
line under the answers, beside "Nápoveda" ("Ako na to? ▸"; a tap opens the method there and counts as help, except on a
first encounter or after the last part is right). It opens by itself above the answers only on the first encounter of
a problem type, and only when the answers still fit under it (`ExerciseView` measures that once the footer is
measured). When the options sit in the footer (a choice), the question above them must be whole on screen too. A new input marks its first option with
`Modifier.firstAnswer()`; `./gradlew :app:snapFold` renders every question step and every generator (several seeds,
as a first encounter) and fails on an answer below the fold.

## Postup and solutions (exercises and worked examples)

A student learns the method, not the numbers. Every generator and every worked example follows a **Postup**
(`data/Postup.kt`): 2 to 5 named subgoals in the order a student does them, each with one sentence how
(`"Súčet ostane v množine?" to "Vezmi dva prvky a over podmienku pre ich súčet."`). The app shows it as "Ako na to?"
before the first attempt of a problem type, and every solution as the same numbered goals (`ui/Goals.kt`, as in a
proof). Subgoal-labelled worked examples: Catrambone 1998, Margulieux and Catrambone 2016.

- **Postup required.** `Exercise(postup = ...)` and `worked(..., postup) { ... }`; a named `val ...Postup` next to the
  generator; one per problem type (variants with another method get their own). The last subgoal gives the answer
  ("Záver"). Lesson worked examples reuse the Postup of the generator they prepare for.
- **A solution is steps under subgoals:** `postup.solve { theta("Sentence.", "formula") }`. A step without a subgoal
  cannot be written. Steps go in the Postup's order from the first subgoal to the last; a subgoal may be skipped only
  when an earlier one already decides (a counterexample), and the app marks it "netreba".
- **Step sentence pattern:** what is checked or done, and why it follows, in one short Slovak sentence (first sentence
  at most 20 words): "Dosadím §\theta§: obe strany sú nulové, takže §\theta§ v množine je." Then the formulas
  (`Formula(tex, note)`, a GEM chain as a walk, see below). Never a bare formula without a sentence.
- **The reason in words, the theorem after it (Teo, 15. 11.).** "If I don't know the theorems by heart, how do I
  justify it?" Every "why" says the reason in words a student can write on the test; a theorem, observation or remark
  may be cited only after it, never instead of it. ❌ "Dimenzia je počet vektorov bázy (Veta 2.57)." ✅ "GEM nemení
  vzťahy medzi stĺpcami; v HST sú hlavné stĺpce LN a ostatné ich kombináciou, preto pôvodné vektory pri hlavných
  stĺpcoch tvoria bázu obalu (Veta 2.59). Dimenzia je počet vektorov ľubovoľnej bázy, preto §\dim=2§ (Veta 2.57)."
  Reasons that recur are written once in `data/Reasons.kt` (columns after GEM, rank, coordinates, θ, a LN set as long as
  the dimension); use them instead of rewording. Definitions may be cited as they are ("podmienky Definície 2.7"):
  a step checks a definition, it does not lean on it. `MathStyleTest.a_reason_before_every_theorem` fails on a step
  sentence whose citation has no causal clause (lebo, preto, teda, takže, totiž, …) before it.
- **Formatter required.** Coefficients, signs, linear combinations and powers in generated TeX only through
  `data/Terms.kt` (`Terms.lin`, `times`, `plus`, `sum`, `pow`, `paren`). Never "1x", "-1x", "0x", "+ -", "- -",
  "x^1", "1(…)", "·1". `MathStyleTest` runs every generator over many seeds and fails on these, on math outside `§…§`,
  on a step that breaks its Postup and on a first sentence longer than 20 words.

## GEM krok po kroku (one component, the matrix never moves)

Every GEM chain the student sees, in a lesson picture, a worked example, a solution or a paper, is a **walk**
(`la/GemWalk.kt`) shown by one component (`screens/GemWalk.kt`: `GemStage`, `GemWalkView`). Teo's report (16. 11.):
when the matrix was centred and the "∼" and the operation appeared above it, it jumped between steps and he could not
see what changed.

- **Build a walk, never type a chain.** From the engine: `GemWalk.of(echelon)`, `GemWalk.of(start, steps)`,
  `gemLines(e)`; from given operations: `Field.R.walk(intMat(...), listOf(rowSub(2, 2, 1), rowSub(3, 1, 1)),
  listOf(rowSwap(2, 3)))`, rows from 1, the matrices computed in the field. `GemWalk.symbolic` only when the entries
  are not numbers (v_1). A formula with `pmatrix` and `\sim` typed as TeX fails `GemWalkTest`.
- **One kind per ∼**, (G1), (G2) or (G3) above it and the operations below it, as in the skriptá (konvencie 15. 11.,
  chap-02.woo:3011-3083, chap-04.woo:1282-1296). Engine steps that mix kinds are split into one ∼ per kind.
- **Stable layout is a property of the walk:** every cell as wide as the widest value it ever takes, every row as tall as
  its tallest, the place for the kind, the ∼ and the operations reserved from the first step, the matrix anchored at the
  top. Changed rows are tinted, the first nonzero element a (G3) step subtracts is framed, H and V stand under the
  columns of the last matrix (Klouda). Swipe moves a step, press and hold shows the matrix before.
- **A step change is calm (Teo, 17. 11.: "the numbers blink"):** it starts in the frame of the tap (`GemFade` is made
  while composing, at progress 0, never in an effect that runs after a frame of the new matrix), only changed entries
  move, like a counter inside their cell, old out and new in with opacities adding up to one, 280 ms ease; tint, frame,
  (Gk) and operation fade with them, the ∼ stays; back is the reverse, also halfway through; under "Remove animations" a
  120 ms cross-fade on its own clock (the system scale 0 would make it a cut).
  `GemFadeTest` holds the rules, `GemFadeFramesTest` renders the frames.
  `GemStageLayoutTest` (Robolectric, real text) measures every step of every walk and fails if the matrix moves by a
  pixel.

## Tables step by step: Euclid, REA, binary powers (one component)

Teo (16. 11.): Euclid's equations wrapped and the REA was a flattened block. A procedure that grows a table
row by row is a **table walk** (`la/TableWalk.kt`) shown by one component (`screens/TableWalk.kt`, the same step change as
GEM): fixed columns aligned, every row reserved, the rule of the step above the table (`r_8=r_6-4\cdot r_7`,
`\lfloor 158/96\rfloor=1`), the rows it uses tinted, numbers that move to the next row sliding there (Euclid's divisor and
remainder), the result framed at the end. Builders compute everything: `euclidWalk(rea(a, b))`, `reaWalk(rea(a, b),
swap)` (narrow i | r | α | β, q in the rule, not a column), `powWalk(b, e, m)`. On 320 dp nothing wraps and nothing
scrolls (`TableStageLayoutTest`); a table never goes into a formula as TeX.

## Vzorový príklad počíta engine (Teo, 16. 11., príčina C)

A worked example of a skill the app computes shows the engine's matrices and tables, never ones typed by hand: GEM through
`Field.walk`/`GemWalk.of`, Euclid and the REA through their walks, any other matrix as `Mat.tex()` of a computed matrix
(`Field.times`, `jordan`, `applyOp`). The problem text may show its matrix. `WorkedFromEngineTest` scans every
`worked(...)` body and fails on a typed `\begin{pmatrix}`, `matrix` or `array`.

## Terminology from the course

Terms and notation come from the course (skriptá, lectures, proseminars), never from generic textbooks: GEM gives
"horný stupňovitý tvar (HST)", "hlavný / vedľajší stĺpec", "prvý nenulový prvok riadku", úpravy (G1) až (G3), never
"pivot" or "schodovitý tvar"; a row operation is written as in the skriptá, the kind and then the rows, "(G3)
§\text{ř}2-2\cdot\text{ř}1§", "(G1) §\text{ř}1\leftrightarrow\text{ř}3§" (generated through `RowOp` in `la/Gem.kt`), never
"§R_2\to R_2-2R_1§". `data/Terminology.kt` lists the course terms and the banned generic ones with the word
to use; `TerminologyTest` scans every string and every generator's output. Before a new term, look it up in the
skriptá (LA1 notation: chap-12-znaceni) and add it there.

## Counting objects and multi-statement questions

- **Vectors, bases, subspaces and varieties are different objects.** A vector is a point, a basis an ordered tuple of
  vectors, a subspace a set of vectors that many bases generate, a variety a shifted subspace. Two bases of one
  subspace are one subspace. In Z_p^n use the model `la/FiniteSpace.kt` (canonical identity of a subspace, exact
  counts, enumeration) instead of new formulas in a generator; `FiniteSpaceTest` checks it against brute force.
- **Every count names what it counts** and what counts as the same object ("usporiadané LN dvojice", "bázy jednej
  roviny", "roviny ako množiny"). A count of vectors never stands for a count of subspaces, nor bases for subspaces.
- **The app has its own formats and never copies MARAST's** (Teo, 18. 11.: "tie majú byť v MARASTe; my máme vlastné
  príklady a formáty"): no ✓ / – / ✗ statements, nothing that imitates MARAST's screens or question types. What such
  questions ask (counts in Z_p^n, parity, what counts as the same subspace) is practised with the app's own steps and
  templates (`zp-pocet`, `zp-vypis`, `zp-zoznam`, checks in the lesson). MARAST may be a source of a fact or a problem.
- Czech text in generators follows Czech numeral agreement (2 vektory, 5 vektorů); `FiniteSpaceTemplatesTest` checks the generators.
- **What the student makes or lists is judged by its property**, never against one sample answer: `Answer.Vectors`
  (`data/Vectors.kt`) for every element of a span, every subspace (each by a basis), another basis, the rows of
  §[A|\mathbf{b}]§. Each wrong entry says why (outside the span, a repeat, the same subspace under another basis, LZ),
  an enumeration also says how many are missing. A list whose length the student decides (open `count`) is an
  enumeration; a pick from a prepared list stays guided. `ConstructTemplatesTest` checks every judge by brute force.

## Open graded quizzes

- **No item of a graded quiz that is still open for students goes into the app**, verbatim or recognisably close (the
  app is shared with classmates; a MARAST kvíz item with its key would be a published answer). Practise the same
  format and skill on generated variants: another space, other numbers, reworded statements of the same kinds.
- Nothing is labelled as a quiz item ("Kvíz …"). Past tests and rozstřely published on fit-wiki stay fine.
- Every open quiz item that reaches us is listed in `OpenQuizTest` (with the date its quiz closes); the test fails if
  any of its phrases appears in the sources, the assets, the docs or anything a generator produces. An item leaves the
  list only by a deliberate decision after its quiz is closed.

## Sources and language

- Every item cites its source in `src`: "Skriptá, Definice 5.9", "Písomka 4. 12. 2025 (E)", "Spěvák, 7. prednáška".
  Numbering as in the rendered textbook, which differs by course: **DML** counts each kind separately within a chapter
  (Definice, Věta, Tvrzení, Lemma, Důsledek, Příklad, Poznámka, Cvičení; chapters 4 to 6 = `chap-04..06` + `cv-04..06`;
  checked against the teachers' Anki deck), **LA1** has one shared counter per chapter (Definice 3.1, Poznámka 3.2,
  Příklad 3.3), chapter n = `chap-0(n+1).woo` (Úvod is unnumbered).
- Czech definitions and test tasks word for word from the source. Explanations in Slovak with correct diacritics.
- No em dashes. No invented course facts: what the lecturer or the course said must be checkable (captions, course page).

## Past papers

A course lists its past papers once (`DmlPaperSpecs` in `data/Dml.kt`, `La1ExamSpecs` in `data/Courses.kt` with
`exam = "la1-skuska"`: id, name, which topic writes problem n and its points). LA1 prepares for the 2nd test until
20. 11. and for the exam after it (`Course.test`); papers belong to their exam. Each topic supplies its problem for every paper it appears in (`paperProblems`, keyed by paper id): the
Czech task word for word, a checklist of rubric lines adding up to the points, the answer and a solution. Points of a
paper add up to the test (DML 30).

### Past papers: problems and parts

A problem that asks several things is split into **parts** (`PaperPart` in `data/Model.kt`), shown on the mock test
as separate blocks a), b), c) and graded one by one after "Odovzdať": the model solution step by step, the checklist
of that part, its points. Build it with `paperProblem(n, title, minutes, parts, drill, note)`; it labels the parts,
joins their text into the problem's text and their rubric lines into its checklist.

```kotlin
paperProblem(2, "Inverzia modulo m", 20, listOf(
    PaperPart("", "Spočítejte gcd(84, 30) Eukleidovým algoritmem.", 3, listOf(Rubric("Tabuľka a gcd", 3, "dml-tc-euklid")), euclidLines),
    PaperPart("", "Najděte inverzi 30 modulo 84, pokud existuje.", 4, listOf(Rubric("Bézout", 2, "dml-tc-inverse"), Rubric("Záver", 2)), inverseLines, answer = "Neexistuje."),
), drill = "dml-tc-inverse")
```

- `text`: the Czech task of that part word for word; cut only between sentences or where the paper itself moves to
  the next thing. The parts joined by spaces must be the whole task (`PapersTest` checks it).
- `pts` and `checklist`: rubric lines adding up to the part's points. `estimate = true` (the default) says the split
  of the problem's points is ours; set it to false only when the paper or the graders give points per part.
- `solution`: the steps revealed one by one, computed by code like everything else; `answer`: the result in a line.
- `review`: what "Zopakovať" opens when the part costs points ("concept:…", "proof:…", "gen:…", "lesson:…");
  without it the first rubric line's `fix` decides. It opens the lesson step that teaches it.
- `note` on the problem: the paper's own instruction for that problem (LA1 problem 1: "V tomto příkladu nemusíte…").

A problem without parts still works as one part (`PaperProblem.units`). New or changed solution text goes past a
second reviewer before it ships.

## Clips

One asset file per topic: `shared/src/commonMain/composeResources/files/clips-<topic id>.json` (LA1 kapitola 2 keeps `clips.json`), entries
as in `clips.json` plus `"course": "dml"`. Captions are the real ones from `/api/captions/<id>`; build the file with a
script like `scripts/clips.py`. A lesson uses a clip with `clip("<clip id>")`; a clip of another course fails the tests.

## Pictures

An interactive picture of a topic implements `Pic.Custom` (data/Model.kt) in its own file under
`screens/` (e.g. `screens/dml/Venn.kt`): it draws itself with the shared components and theme (`T.c`, `card()`,
`Rich`, `Choice`; Hrejivý and Tmavý, 48 dp targets, works at 200 % font) and calls `onGoal()` when its goal is met.

## Statements and proofs (`Wording`, `Proven`; `WordingTest`)

Teo, 16. 11.: the "Dokazuješ" bar showed only the conclusion of Věta 6.2 without its hypotheses, a card titled
"Lemma 6.1" also held Věta 6.3, Lemma 6.1 had no proof and no reason, the REA recursion was one line breaking anywhere,
and the check from memory of Definice 6.1 counted words the task never asked. The cause was one: a theorem was a list
of lines and every screen guessed what it says. Now a statement is stored in parts and every screen draws from them
(`ui/StatementCard.kt`: `TheoremCard`, `ConceptCard`, `ItemCards`, `Dokazujes`, the reference sheet).

- **One number, one card.** A `Theorem` (or `Concept`) is exactly one numbered item of the textbook; its `src` names
  that one number. A step about several shows a card each: `thm("t2-59", "t2-60")`, `explain(..., cards = listOf(...))`.
  A numbered remark that belongs to a theorem is `remarks = listOf(quote("Skriptá, Varování 2.40", p(...)))`, a card of
  its own. Splitting an old combined item: the old id keeps one statement (ids are stored progress), the others get new
  ids; lesson steps are never added for it (step ids are positions).
- **Wording, word for word:** `wording(assume, claim, core, parts, rest)`. `assume` the hypotheses ("Nechť …"), `claim`
  the claim with its quantifiers, `rest` what follows in the same numbered item; joined they are the Czech text.
- **Core:** the claim as one display formula (raw TeX) that fits a 320 dp card at full size, five rows at most.
  Required when the theorem is proved line by line (the closed "Dokazuješ" bar shows it) and for a test's proof task;
  a long wording (`Wording.long`) with a core leads with it, the whole text one tap away. A list of laws has no core.
- **Parts:** what a proof proves separately (existencia a jednoznačnosť, ⇒ a ⇐, body): `part("Jednoznačnosť", tex,
  from = 3)`, `from` the proof round it starts at. The bar names the part being proved and shows its formula; open,
  it shows the hypotheses, the claim and all parts.
- **How it is proved, always:** `proven = proved(proof)` (line by line), `shortProof(argument, src)` (the textbook's
  few sentences, Czech, condensed only by leaving sentences out) or `noProof(why, src)` (who leaves it out and how we
  know). A proof of a theorem has no wording of its own (`null`); a test's proof task has `wording(...)`.
- **A reference shows something new or nothing.** Every number in any text opens a card (`RefNamesTest`): a statement
  texts refer to gets its card even without a lesson step. A reference to a card already on the screen is plain text
  (`LocalReadingSelf`). The sheet shows the statement and how it is proved; its link goes only to what the sheet does
  not show (a definition's why and examples, a theorem's proof).
- **A quote is not a copy.** `explain(formal = …)` only for text that is not a card of the app (a remark, a test's
  wording), with a `src` of one number at most; a definition or theorem the app has is shown by `cards`.
- **Adjacent remarks:** a definition may carry `Concept.remarks`, each with its own number and source card. Use this
  for terminology or dimension supplied by a numbered remark, not by the definition itself. Lessons and reading
  sheets use the same stored quote.
- **Definitions:** `units` are only what `ask` asks (Definice 6.1: §a,b\in\mathbb{Z}§, §\exists k\in\mathbb{Z}§,
  §a\cdot k=b§); the rest the textbook says goes to `extra` ("Navyše zo skrípt, nepočíta sa"). "Z pamäti" and the
  review count `units` only.
- **Mobile typesetting:** a statement breaks as little as it can and only where its logic does (Teo, 18. 11.). Write
  it as one logical line with soft breaks: `\softbreak` after the quantifiers (the body goes under, flush), `\softbreak[1]`
  before ⇒, ⇔, ∧, = inside it (indented a quad), `\softbreak[2]` inside a list or set (two quads). The card takes the
  fewest lines that fit its width, and of those the shallowest breaks; nothing breaks while the line fits (360 dp at
  100 % mostly whole lines, 320 dp at 200 % more breaks). Brackets around a break have a fixed size (`\Big(`, `\big\langle`),
  never `\left…\right`; a sum in a broken statement may take `\textstyle` (limits beside it). A hard row (`\\` in
  `aligned`) only where the textbook has rows or for parallel things (e₁, e₂, …; a system); a `\softbreak` inside an
  aligned row starts a new row only when the table does not fit (Euklid's `∀n ≥ 3: rₙ = …`). The words and symbols
  stay the textbook's. `./gradlew :app:snap -Psnap.test=OverflowScan*` (`statementsFitUnbroken`) renders every card,
  every remark and every "Dokazuješ" bar (core and each part) at 320 dp and 100 % with all folds open and fails on
  any formula whose lines do not fit at full size.
- **gcd and lcm, never NSD/NSN** (Definice 6.3; Dombek): `TerminologyTest` and `WordingTest` fail on the abbreviations.

`WordingTest` lists every item that breaks a rule, so one run is the whole to-do list.

## Reading markup (theorems, definitions, proofs)

Teo's rule for all of it: **don't spam it; the line and the marker must not disturb.** Most items get none.

- **Display formula** `§§...§§`: the key formula of a statement on its own centered line. Double the delimiters of
  that formula inside the Czech quote; no word changes. At most one per item (two for two statements). The pinned
  "Dokazuješ" card shows the wording's `core` and parts, never a formula picked out of the text.
- **Key word** `==vždy==`: a word that decides true or false on a test (vždy, nemusí, každý, existuje, právě když,
  nejvýše, alespoň). Only in the Czech quotes of theorems and definitions, at most 2 per card.
- **Named objects** `\ca{P}`, `\cb{Q}`, `\cc{R}` in TeX: one muted colour per object, only where a statement has 2 or 3
  named objects; the same object keeps its colour in the theorem, its idea and its whole proof.
- **Glossary**: one file per course (`data/la1/Glossary.kt`, `data/dml/Glossary.kt`). A `Symbol` with a regex over TeX
  (`match`), how it reads, one sentence and the id of the Concept that defines it; only notation with a definition in
  the app. The app marks the first occurrence per screen with a thin line; a tap opens the sheet.
- **Every Concept** has `question`, the question its judge items ask ("Je to podpriestor?"), never "Platí to?".
- **Every Proof** has `goals`, one short title per round ("Neprázdnosť", "Indukčný krok"), and `warn` where its
  theorem says what does not follow (the "nemusí" part).

`ReadingTest` checks all of this.

## Before saying ready

[SYSTEM.md](SYSTEM.md), "Verification" and "Before saying ready": during work only the test classes of what you changed
(`./gradlew :shared:jvmTest --tests '*.WordingTest'`), once before handing over
`./gradlew :shared:jvmTest :app:assembleDebug` with a real exit code 0, and screenshots of every step you changed
looked at (`:app:snap`, light and dark, 320 dp). The screenshot checks (`snapFold`, `OverflowScan`) run before a release
or when you change their area (a question screen, statements and display formulas).

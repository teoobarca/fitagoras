package dev.hamper.tabula.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.semantics.SemanticsProperties
import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.SystemBack
import dev.hamper.tabula.compat.media.SilentPlayer
import dev.hamper.tabula.data.Dml
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.LessonDef
import dev.hamper.tabula.data.dueKeys
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.BarTrace
import dev.hamper.tabula.ui.Buzz
import dev.hamper.tabula.ui.Sfx
import dev.hamper.tabula.ui.Sounds
import dev.hamper.tabula.ui.HapticTrace
import dev.hamper.tabula.ui.LocalHapticsOn
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * A lesson tapped through headless on the desktop JVM (Compose Multiplatform, the same screens as on the phone), back
 * pressed through the shared back handler: seconds, no emulator (docs/SYSTEM.md, "Verification", other rules 1). What it holds: back
 * is one screen and a step gone back to stays answered and ungraded; leaving and opening again (a new store from the
 * saved document, as after the app was killed) finds the same screen; "Viem to"; the keypad; which event plays which
 * haptic pattern (how it feels is checked on a phone).
 */
@OptIn(ExperimentalTestApi::class)
class LessonFlowUiTest {
    private val buzz = mutableListOf<Buzz>()

    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
        ClipLibrary.load()
        HapticTrace.sink = { buzz += it }
    }

    @After fun tearDown() { HapticTrace.sink = null; BarTrace.sink = null }

    private class Outcome { var left = false; var done = false }

    private fun ComposeUiTest.lesson(l: LessonDef, store: Store, out: Outcome = Outcome()): Outcome {
        setContent {
            TabulaTheme(ThemeMode.Light) {
                CompositionLocalProvider(LocalHapticsOn provides { true }) {
                    LessonFlow(l, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}, { out.left = true }) { out.done = true }
                }
            }
        }
        waitForIdle()
        return out
    }

    private fun ComposeUiTest.tap(text: String) { onAllNodes(hasText(text, substring = true) and hasClickAction()).onFirst().performClick(); waitForIdle() }
    private fun ComposeUiTest.right() { onNode(hasContentDescription("dbg:correct")).performClick(); waitForIdle() }
    private fun ComposeUiTest.back(): Boolean = SystemBack.press().also { waitForIdle() }
    private fun ComposeUiTest.shows(text: String) = onAllNodes(hasText(text, substring = true) or hasContentDescription(text, substring = true)).fetchSemanticsNodes().isNotEmpty()

    private val delitelnost get() = LessonById.getValue("dml-tc-delitelnost")

    @Test fun backIsOneScreenAndAStepComesBackAnswered() = runComposeUiTest {
        val l = delitelnost
        val store = Store(MemoryStateFile())
        store.restartLesson(l, 12)                     // a check, then the definition of divisibility
        val out = lesson(l, store)
        val check = "step:${l.steps[12].id}"
        right()
        assertEquals(1, store.history(check).size)
        tap("Pokračovať")
        tap("Prečo je tam každá časť")
        tap("Príklady a protipríklady")
        right()                                          // the first example, answered
        val run = store.run(l)
        assertEquals(13 to 200, run.at to run.sub())

        // back: the definition's own screens one by one, then the check, answered, its options closed
        assertTrue(back()); assertEquals(13 to 100, run.at to run.sub()); assertTrue(shows("Príklady a protipríklady"))
        assertTrue(back()); assertEquals(13 to 0, run.at to run.sub()); assertTrue(shows("Prečo je tam každá časť"))
        assertTrue(back()); assertEquals(12, run.at)
        assertTrue("the check shows its verdict", shows("Správne."))
        assertTrue("no option is open again", onAllNodes(hasContentDescription("dbg:correct")).fetchSemanticsNodes().isEmpty())

        // forward again: the example still answered, nothing graded twice
        tap("Pokračovať"); tap("Prečo je tam každá časť"); tap("Príklady a protipríklady")
        assertTrue(shows("Správne."))
        assertEquals(1, store.history(check).size)
        assertEquals(1, store.history("judge:dml-tc-delitelnost:0").size)

        // all the way back: one press per screen, and on the first one back leaves the lesson
        var presses = 0
        while (!out.left && presses < 60) { back(); presses++ }
        assertTrue(out.left)
        assertEquals("the definition's 3 screens, 12 steps back to the first, then one more to leave", 3 + 12 + 1, presses)
    }

    /**
     * A quick double tap on "Pokračovať" is one step on (emulator, 1.6.2: the second tap skipped a step unseen). Both
     * ways it went: the second tap in the same frame, on the step going out, and 100 ms later, on the new step's own
     * "Pokračovať" (an explanation's is enabled at once).
     */
    @Test fun aDoubleTapIsOneStep() = runComposeUiTest {
        val l = dev.hamper.tabula.data.AllLessons.first { l -> (0 until l.steps.size - 2).any { l.steps[it] is dev.hamper.tabula.data.Step.Explain && l.steps[it + 1] is dev.hamper.tabula.data.Step.Explain } }
        val k = (0 until l.steps.size - 2).first { l.steps[it] is dev.hamper.tabula.data.Step.Explain && l.steps[it + 1] is dev.hamper.tabula.data.Step.Explain }
        val store = Store(MemoryStateFile())
        store.restartLesson(l, k)
        lesson(l, store)
        mainClock.advanceTimeBy(1_000)
        mainClock.autoAdvance = false
        val on = onAllNodes(hasText("Pokračovať") and hasClickAction()).onFirst()
        on.performClick(); on.performClick()
        assertEquals("${l.id}: two taps in one frame are one step", k + 1, store.run(l).at)
        mainClock.advanceTimeBy(100)
        val buttons = onAllNodes(hasText("Pokračovať") and hasClickAction())
        buttons[buttons.fetchSemanticsNodes().size - 1].performClick()
        assertEquals("${l.id}: a tap 100 ms later on the new step's button is the same double tap", k + 1, store.run(l).at)
        mainClock.autoAdvance = true
        waitForIdle()
        tap("Pokračovať")
        assertEquals("${l.id}: a tap once the step has settled goes on", k + 2, store.run(l).at)
    }

    /**
     * Every Z_p^2 grid to explore or count from, in both courses, is open the way its text says: one tap when it asks for
     * a tap ("Ťukni na vektor"), at once when it only asks about what it shows ("Koľko má bodov?"). Emulator, 1.6.2:
     * "Pokračovať" stayed grey after the one tap asked for, and on a grid that asked for none.
     */
    @Test fun aGridOpensTheWayItsTextSays() {
        val grids = dev.hamper.tabula.data.AllLessons.flatMap { l -> l.steps.withIndex().mapNotNull { (i, s) -> (s as? dev.hamper.tabula.data.Step.Explore)?.let { Triple(l, i, it) } } }
            .filter { (_, _, s) -> (s.pic as? dev.hamper.tabula.data.Pic.Grid)?.let { it.n == 2 && it.goal in setOf(dev.hamper.tabula.data.Goal.Explore, dev.hamper.tabula.data.Goal.Count) } == true }
        assertTrue("the grids are found", grids.size >= 3)
        for ((l, i, s) in grids) {
            val asksTap = listOf(s.title, s.sub).any { it.contains("ťuk", ignoreCase = true) }
            if (asksTap) runComposeUiTest {
                var opened = false
                setContent { TabulaTheme(ThemeMode.Light) { PicView(s.pic) { opened = true } } }
                waitForIdle()
                onAllNodes(hasContentDescription("Vektor 2, 1", substring = true) and hasClickAction()).onFirst().performClick()
                waitForIdle()
                assertTrue("${l.id} ${s.id}: one tap opens the way on", opened)
            } else runComposeUiTest {
                val store = Store(MemoryStateFile())
                store.restartLesson(l, i)
                lesson(l, store)
                assertTrue("${l.id} ${s.id}: asks for no tap, so the way on is open", onAllNodes(hasText("Pokračovať") and hasClickAction() and androidx.compose.ui.test.isEnabled()).fetchSemanticsNodes().isNotEmpty())
                // a double tap on it shows the result and stays: the second half neither skips the result nor the step
                mainClock.advanceTimeBy(1_000)
                mainClock.autoAdvance = false
                val way = onAllNodes(hasText("Pokračovať") and hasClickAction())[0]
                way.performClick(); way.performClick()
                mainClock.autoAdvance = true; waitForIdle()
                assertEquals("${l.id} ${s.id}: a double tap stays on the step", i, store.run(l).at)
            }
        }
    }

    @Test fun leavingAndComingBackAfterTheAppWasKilled() {
        val file = MemoryStateFile()
        val l = delitelnost
        runComposeUiTest {
            val store = Store(file)
            store.restartLesson(l, 12)
            lesson(l, store)
            right(); tap("Pokračovať"); tap("Prečo je tam každá časť")
            // the save runs in the background: wait for the document that has the definition's second screen
            waitUntil(timeoutMillis = 5_000) { file.read()?.contains("\"${l.steps[13].id}\":[0,100]") == true }
        }
        runComposeUiTest {
            val store = Store(file)                       // a new process: only the saved document
            lesson(l, store)
            assertEquals(13 to 100, store.run(l).at to store.run(l).sub())
            assertTrue(shows("Príklady a protipríklady"))
            back()
            back()
            assertTrue("the check before it is still answered", shows("Správne."))
            // closed in the middle of a piece (the check ended the one before; data/Pieces.kt) and walked again: the
            // check counted once
            tap("Pokračovať"); tap("Prečo je tam každá časť")
            assertEquals(13, store.run(l).at)
            assertEquals(1, store.history("step:${l.steps[12].id}").size)
        }
    }

    @Test fun keypadAndKnowIt() = runComposeUiTest {
        val l = LessonById.getValue("dml-tc-rea")
        val store = Store(MemoryStateFile())
        store.restartLesson(l, 10)                        // a practice: gcd by the extended Euclid
        lesson(l, store)
        // the field tells the debug build what is right ("dbg:field0=7"); type it on the phone-layout pad
        val want = onAllNodes(SemanticsMatcher("field") { n -> n.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }.any { it.startsWith("dbg:field0=") } })
            .fetchSemanticsNodes().first().config[SemanticsProperties.ContentDescription].first { it.startsWith("dbg:field0=") }.substringAfter('=')
        fun type(v: String) { for (ch in v) { onAllNodes(hasContentDescription(if (ch == '-') "Mínus" else "$ch") and hasClickAction()).onFirst().performClick(); waitForIdle() } }
        // a wrong number first: the key clicks, then the wrong answer's pattern
        val miss = (want.toLong() + 1).toString()
        buzz.clear()
        type(miss)
        assertEquals("one key click per key", List(miss.length) { Buzz.Key }, buzz.filter { it == Buzz.Key })
        tap("Skontrolovať")
        assertTrue(shows("Tentoraz nie."))
        assertEquals(Buzz.Wrong, buzz.last())
        tap("Skúsiť znova")
        buzz.clear()
        type(want)
        tap("Skontrolovať")
        assertTrue(shows("Správne."))
        assertEquals("right after a miss: quieter, no series", Buzz.RightAgain, buzz.last())

        // the solo exercise at the end: "Viem to" skips it, no mistake, review shows it once
        store.restartLesson(l, 14)
        val gen = "gen:dml-tc-rea"
        // the lesson on screen reads the new run
        waitForIdle()
        assertEquals(14, store.run(l).at)
        tap("Viem to")
        assertTrue(gen in store.vouched)
        assertFalse(store.tried(gen))
        assertTrue(store.mistakes.none { it.key == gen })
        assertTrue(gen in dueKeys(store, Dml))
        // one clean answer in review settles it
        store.record(gen, ok = true, aided = false)
        assertTrue(store.mastered(gen))
        assertFalse(gen in dueKeys(store, Dml))
        // a type with any history is not offered to skip: a miss is learnt by the usual rule
        store.record("gen:dml-tc-ldr", ok = false, aided = false)
        assertFalse(store.canVouch("gen:dml-tc-ldr"))
    }

    @Test fun whichEventPlaysWhichPattern() = runComposeUiTest {
        val l = delitelnost
        val store = Store(MemoryStateFile())
        store.restartLesson(l, 12)
        lesson(l, store)
        buzz.clear()
        val sounds = mutableListOf<Sfx>()
        Sounds.trace = { sounds += it }
        right()                                            // 1st right in a row
        tap("Pokračovať"); tap("Prečo je tam každá časť"); tap("Príklady a protipríklady")
        repeat(4) { k -> right(); tap(if (k < 3) "Ďalší" else "Pokračovať") }   // 2nd to 5th
        right()                                            // the trap, 6th
        assertEquals(listOf(Buzz.Right, Buzz.Right, Buzz.Right, Buzz.Right, Buzz.Series, Buzz.Right), buzz.filter { it == Buzz.Right || it == Buzz.Series })
        assertTrue("every button and option answers the thumb", buzz.count { it == Buzz.Tap || it == Buzz.Button || it == Buzz.Choose } >= 10)
        // the same moments as sounds: the series climbs from "correct" through the combos; links that move around are silent
        val verdicts = setOf(Sfx.Correct, Sfx.Combo1, Sfx.Combo2, Sfx.Combo3, Sfx.Combo4, Sfx.Combo5, Sfx.CorrectAgain, Sfx.Wrong)
        assertEquals(listOf(Sfx.Correct, Sfx.Combo1, Sfx.Combo2, Sfx.Combo3, Sfx.Combo4, Sfx.Combo5), sounds.filter { it in verdicts })
        assertEquals("one sound per moment that has one", buzz.mapNotNull { Sounds.of(it) }.size, sounds.size)
        Sounds.trace = null

    }


    /**
     * The step bar moves one way per move (Teo, 18. 11.: it jumped ahead and back and blinked between steps): every
     * value it draws, frame by frame, from a tap forward to the screen at rest never falls, after back never rises.
     * Through a check, a definition's own screens and back again.
     */
    @Test fun theStepBarMovesOneWay() = runComposeUiTest {
        val l = delitelnost
        val store = Store(MemoryStateFile())
        store.restartLesson(l, 12)
        val drawn = mutableListOf<Float>()
        BarTrace.sink = { drawn += it }
        lesson(l, store)
        var moves = 0
        fun move(forward: Boolean, what: String, act: () -> Unit) {
            drawn.clear()
            act()
            mainClock.advanceTimeBy(3_000)
            waitForIdle()
            val bad = drawn.zipWithNext().firstOrNull { (a, b) -> if (forward) b < a - 1e-4f else b > a + 1e-4f }
            assertTrue("$what: the bar went ${if (forward) "back" else "ahead"} at $bad: $drawn", bad == null)
            moves += drawn.size
        }
        move(true, "the check") { right() }
        for (t in listOf("Pokračovať", "Prečo je tam každá časť", "Príklady a protipríklady")) move(true, t) { tap(t) }
        repeat(4) { k -> move(true, "example $k") { right() }; val t = if (k < 3) "Ďalší" else "Pokračovať"; move(true, "$t $k") { tap(t) } }
        repeat(8) { i -> move(false, "back $i") { back() } }
        assertTrue("the bar was drawn as the steps changed", moves > 20)
    }

    /**
     * "Teraz nie" on the paper step of a definition (Teo, 17. 11.): the lesson goes on, nothing counts as a mistake, and
     * review asks for the definition later; writing it then takes it off the list.
     */
    @Test fun paperPutOffGoesToReviewWithoutAMistake() = runComposeUiTest {
        val l = delitelnost
        val store = Store(MemoryStateFile())
        store.lessonsDone += l.id // a finished lesson, so the review offers what it holds
        store.restartLesson(l, 13)                      // the definition of divisibility
        lesson(l, store)
        tap("Prečo je tam každá časť")
        tap("Príklady a protipríklady")
        for (i in 0 until 12) {
            if (shows("Napísať z pamäti")) break
            if (onAllNodes(hasContentDescription("dbg:correct")).fetchSemanticsNodes().isNotEmpty()) right()
            else if (shows("Áno") && shows("Nie") && shows("Stačila by")) tap("Nie")
            else if (shows("Ďalší")) tap("Ďalší") else tap("Pokračovať")
        }
        tap("Napísať z pamäti")
        assertTrue(shows("Mám napísané"))
        val run = store.run(l)
        tap("Teraz nie")
        assertEquals("the lesson moved on", 14, run.at)
        val key = "concept:dml-tc-delitelnost"
        assertTrue(key in store.unwritten)
        assertTrue("no mistake", store.mistakes.none { it.key == key })
        assertTrue("asked in review", key in dueKeys(store, Dml))
        store.record(key, true, false)
        assertFalse(key in store.unwritten)
    }
}

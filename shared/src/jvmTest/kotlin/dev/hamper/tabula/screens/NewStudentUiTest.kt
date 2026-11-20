package dev.hamper.tabula.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Env
import dev.hamper.tabula.Nav
import dev.hamper.tabula.compat.media.SilentPlayer
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.data.TemplateOf
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * A new student's way through the app where it once went wrong: review before any lesson, and grading a mock test on
 * paper (back from its first part, a "Zopakovať" link in the middle of it).
 */
@OptIn(ExperimentalTestApi::class)
class NewStudentUiTest {
    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
        ClipLibrary.load()
    }

    /** Review brings back only what was learnt: nothing before the first lesson, then only that lesson's problems and definitions. */
    @Test fun reviewOnlyWhatWasLearnt() {
        for (course in Courses) {
            val store = Store(MemoryStateFile())
            store.pickCourse(course.id)
            assertTrue("${course.id}: nothing to review before the first lesson", reviewRoutes(store, Random(1)).isEmpty())
            val first = course.shown().lessons.first { l -> l.steps.any { it is Step.Practice || it is Step.Def } }
            store.finishLesson(first.id)
            // a problem written on paper and short of points: it waits in "Vracia sa ti", never as a review screen
            val paper = course.papers.first().id // any past paper: course.test moves with the date
            store.record("paper:$paper:1", false, false, dev.hamper.tabula.state.Mistake("paper:$paper:1", "Príklad 1", "Na papieri 0 b", "paper:$paper:1", 0L))
            val taught = first.steps.flatMap { s ->
                when (s) {
                    is Step.Practice -> listOf("gen:${s.gen}", "gen:${TemplateOf[s.gen]}")
                    is Step.Def -> listOf("concept:${s.concept}")
                    else -> emptyList()
                }
            }.toSet()
            repeat(20) { seed ->
                val r = reviewRoutes(store, Random(seed))
                assertTrue("${course.id}: review after ${first.id} is not empty", r.isNotEmpty())
                assertTrue("${course.id}: review after ${first.id} brings only its own items: $r", r.all { it in taught })
            }
        }
    }

    /** From Dnes through the first lesson of each course to its end and back: the lesson counts as done everywhere. */
    @Test fun firstLessonToTheEnd() {
        for (cid in listOf("la1", "dml")) runDesktopComposeUiTest(width = 400, height = 1600) {
            val store = Store(MemoryStateFile())
            store.pickCourse(cid)
            store.statsNoticed = true
            // the test weeks away: two days before it Dnes offers the quick way to a problem instead (data/BeforeTest.kt)
            store.terms[store.course.test.id] = LocalDate.now().plusDays(30).atStartOfDay()
            val app = AppState()
            setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
            waitForIdle()
            val first = nextLesson(store)!!
            onAllNodes(hasText("Začať lekciu"))[0].performClick(); waitForIdle()
            val forward = listOf("Pokračovať", "Ďalej", "Skontrolovať", "Mám napísané", "Ďalší krok", "Hotovo", "Teraz nie", "Viem to", "Celé riešenie", "Ukáž riešenie", "Neviem, ukáž mi to", "Prečítať, čo povedal", "Späť do lekcie", "Ďalší", "Prečo je tam každá časť", "Príklady a protipríklady", "Napísať z pamäti", "Na papier", "Dokázať")
            var guard = 0
            val log = ArrayList<String>()
            while (app.stack.last() !is Nav.LessonEnd && guard++ < 400) {
                val right = onAllNodes(hasContentDescription("dbg:correct"))
                if (right.fetchSemanticsNodes().isNotEmpty()) { log += "${store.runAt(first)}:correct"; right[0].performClick(); waitForIdle(); continue }
                val label = forward.firstOrNull { onAllNodes(hasText(it) and hasClickAction() and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
                // a picture to play with (the way on opens when its goal is reached): the walk steps over it
                if (label == null && onAllNodes(hasText("Pokračovať") and hasClickAction()).fetchSemanticsNodes().isNotEmpty()) {
                    log += "${store.runAt(first)}:picture"; runOnIdle { store.run(first).next() }; waitForIdle(); continue
                }
                if (label == null) {
                    val texts = onAllNodes(hasClickAction()).fetchSemanticsNodes().map { n -> (n.config.getOrNull(SemanticsProperties.Text)?.joinToString() ?: "") + "|" + (n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString() ?: "") }
                    val all = onAllNodes(SemanticsMatcher("any") { true }).fetchSemanticsNodes().mapNotNull { n -> n.config.getOrNull(SemanticsProperties.Text)?.joinToString() }
                    throw AssertionError("$cid ${first.id}: stuck at ${store.runAt(first)} (${first.steps[store.runAt(first)].id}); clickable: $texts; texts: ${all.take(40)}")
                }
                log += "${store.runAt(first)}:$label"
                onAllNodes(hasText(label) and hasClickAction() and isEnabled())[0].performClick(); waitForIdle()
            }
            assertTrue("$cid: the end of ${first.id}, last: ${log.takeLast(12)}; on screen: ${onAllNodes(SemanticsMatcher("any") { true }).fetchSemanticsNodes().mapNotNull { n -> n.config.getOrNull(SemanticsProperties.Text)?.joinToString() }.take(30)}", app.stack.last() is Nav.LessonEnd)
            assertTrue("$cid: ${first.id} is done", first.id in store.lessonsDone)
            shot("first-lesson-end-$cid")
            onAllNodes(hasText("Hotovo") and hasClickAction())[0].performClick(); waitForIdle()
            shot("first-lesson-dnes-$cid")
            assertEquals(1, app.stack.size)
            assertTrue("$cid: the next lesson is another one", nextLesson(store) != first)
        }
    }

    /** The end of a lesson of the other course (opened from a link) counts and goes on in the lesson's course, not the
     *  one picked on Dnes (web walk, 19. 11.: a DML lesson with LA1 picked ended with LA1's numbers and next lesson). */
    @Test fun aLessonEndsInItsOwnCourse() = runDesktopComposeUiTest(width = 400, height = 1200) {
        val store = Store(MemoryStateFile())
        store.pickCourse("la1")
        store.statsNoticed = true
        val app = AppState()
        val dml = Courses.first { it.id == "dml" }
        val la1 = Courses.first { it.id == "la1" }
        val lesson = nextLesson(store, dml)!!
        app.push(Nav.LessonEnd(lesson.id))
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        waitForIdle()
        val texts = onAllNodes(androidx.compose.ui.test.SemanticsMatcher("any") { true }).fetchSemanticsNodes()
            .mapNotNull { n -> n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.joinToString() }
        val la1Next = nextLesson(store, la1)!!.name
        assertTrue("LA1's next lesson offered after a DML lesson: $texts", texts.none { la1Next in it })
        assertTrue("the course's own test word: $texts", texts.any { dml.test.acc in it })
    }

    /** System back on Cesta and Test goes to Dnes; only on Dnes it leaves the app (emulator, 1.6.2: back on Cesta closed it). */
    @Test fun backFromATabGoesToDnes() = runDesktopComposeUiTest(width = 400, height = 900) {
        val store = Store(MemoryStateFile())
        store.statsNoticed = true
        val app = AppState()
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        waitForIdle()
        for (tab in listOf("Cesta", "Test")) {
            onAllNodes(hasContentDescription(tab) and hasClickAction())[0].performClick(); waitForIdle()
            assertTrue("back on $tab is taken", dev.hamper.tabula.compat.SystemBack.press()); waitForIdle()
            assertEquals(dev.hamper.tabula.Tab.Dnes, app.tab)
        }
        assertTrue("on Dnes back leaves the app", !dev.hamper.tabula.compat.SystemBack.press())
    }

    /**
     * A switch of course on Dnes is a cross-fade: in its first frames the screen is still the old page, whole, with
     * nothing of the new one showing through (emulator, 1.6.2: between the old page's cards the new page showed at full
     * strength, "DMLML" on the tab); at the end it is the other course.
     */
    @Test fun aCourseSwitchStartsAsTheOldPage() = runDesktopComposeUiTest(width = 400, height = 900) {
        val store = Store(MemoryStateFile())
        store.pickCourse("la1")
        store.statsNoticed = true
        val app = AppState()
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        waitForIdle()
        mainClock.autoAdvance = false
        fun frame() = onAllNodes(androidx.compose.ui.test.isRoot()).let { it[it.fetchSemanticsNodes().size - 1] }.captureToImage().toAwtImage()
        val before = frame()
        dev.hamper.tabula.CourseSwitch.ask("dml")
        var n = 0
        while (store.course.id != "dml" && n++ < 20) mainClock.advanceTimeByFrame()
        assertEquals("the course changed under the picture", "dml", store.course.id)
        mainClock.advanceTimeByFrame()
        val first = frame()
        // the page above the bar (the bar is not in the picture, its colour turns on the curve)
        val h = before.height * 3 / 4
        var differ = 0
        for (y in 0 until h) for (x in 0 until before.width) {
            val a = before.getRGB(x, y); val b = first.getRGB(x, y)
            val d = maxOf(kotlin.math.abs((a shr 16 and 255) - (b shr 16 and 255)), kotlin.math.abs((a shr 8 and 255) - (b shr 8 and 255)), kotlin.math.abs((a and 255) - (b and 255)))
            if (d > 24) differ++
        }
        assertTrue("the first frame of the switch is the old page: ${differ} of ${h * before.width} pixels differ", differ < h * before.width / 200)
        mainClock.autoAdvance = true
        mainClock.advanceTimeBy(2_000); waitForIdle()
        assertTrue("at the end it shows the other course", onAllNodes(hasText("písomka", substring = true, ignoreCase = true)).fetchSemanticsNodes().isNotEmpty())
    }

    /** A whole DML mock from the tab "Test": start, hand in, "Mám to" at every part, the result. */
    @Test fun wholeMockFromTestTab() = runDesktopComposeUiTest(width = 400, height = 1600) {
        val store = Store(MemoryStateFile())
        store.pickCourse("dml")
        store.statsNoticed = true
        val app = AppState()
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        waitForIdle()
        // the screens set Slovak text with no-break spaces ("4.\u00A012.\u00A02025", ui/Text.kt): matched as plain spaces
        fun loose(t: String) = SemanticsMatcher("text with \"$t\"") { n -> n.config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text.replace('\u00A0', ' ').contains(t) } }
        fun tap(t: String) { onAllNodes(loose(t) and hasClickAction() and isEnabled())[0].performClick(); waitForIdle() }
        // the tab "Test" (Nácvik before 18. 11., "Písomka" in DML before 19. 11.): the whole paper on time, straight to its intro
        onAllNodes(hasContentDescription("Test"))[0].performClick(); waitForIdle()
        tap("Celá písomka na čas")
        assertTrue("the intro of a whole paper", app.stack.last() is Nav.MockIntro)
        tap("Spustiť")
        assertTrue("the mock runs", app.stack.last() is Nav.MockRun)
        // the clock starts at the paper's whole time with no platform behind it (the web and the desktop start nothing
        // of their own; the web showed 0:00 at once, 19. 11.)
        val full = paperMinutes((app.stack.last() as Nav.MockRun).paper) * 60
        val clock = onAllNodes(SemanticsMatcher("a clock (h:)m:ss") { n -> n.config.getOrNull(SemanticsProperties.Text).orEmpty().any { Regex("""\d+(:\d\d){1,2}""").matches(it.text) } }).fetchSemanticsNodes()
            .single().config[SemanticsProperties.Text].single().text.split(':').fold(0) { a, p -> a * 60 + p.toInt() }
        assertTrue("the clock starts at ${full / 60}:00, shows ${clock / 60}:${(clock % 60).toString().padStart(2, '0')}", clock in full - 10..full)
        tap("Odovzdať"); onAllNodes(hasText("Odovzdať") and hasClickAction()).let { it[it.fetchSemanticsNodes().size - 1].performClick() }; waitForIdle()
        assertTrue("grading", app.stack.last() is Nav.MockGrade)
        var guard = 0
        while (app.stack.last() is Nav.MockGrade && guard++ < 200) {
            if (onAllNodes(hasText("Ukázať celé riešenie")).fetchSemanticsNodes().isNotEmpty()) { tap("Ukázať celé riešenie"); continue }
            // one tap for the whole part (Teo, 18. 11.: no ticking line by line)
            val vybrate = onAllNodes(androidx.compose.ui.test.hasAnyDescendant(hasText("Mám to")) and androidx.compose.ui.test.isSelected()).fetchSemanticsNodes().isNotEmpty()
            onAllNodes(hasText("Mám to") and hasClickAction()).let { m -> if (m.fetchSemanticsNodes().isNotEmpty() && !vybrate) { m[0].performScrollTo().performClick(); waitForIdle() } }
            val next = listOf("Ďalšia časť", "Ďalší príklad", "Pozrieť výsledok").first { onAllNodes(hasText(it) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
            tap(next)
        }
        assertTrue("the result", app.stack.last() is Nav.MockResult)
        val r = store.mocks.single()
        assertEquals("every part had: full points (${r.paper}: ${r.got})", r.max, r.total)
        shot("mock-result-dml")
        tap("Hotovo")
        assertTrue("back on the tab", app.stack.size == 1 && app.tab == dev.hamper.tabula.Tab.Nacvik)
        shot("mock-nacvik-dml")
    }

    /** The screen as it is now (a dialog: the dialog), left in build/reports for a look. */
    private fun androidx.compose.ui.test.ComposeUiTest.shot(name: String) {
        java.io.File("build/reports").mkdirs()
        javax.imageio.ImageIO.write(onAllNodes(androidx.compose.ui.test.isRoot()).let { it[it.fetchSemanticsNodes().size - 1] }.captureToImage().toAwtImage(), "png", java.io.File("build/reports/$name.png"))
    }

    @Test fun mockGradingKeepsItsPlaceAndAsksBeforeLeaving() = runDesktopComposeUiTest(width = 400, height = 1600) {
        val store = Store(MemoryStateFile())
        store.pickCourse("la1")
        val app = AppState()
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        runOnIdle { app.push(Nav.MockGrade("slon-2024")) }
        waitForIdle()
        fun click(t: String) = onAllNodes(hasText(t, substring = true))[0].performClick()
        // the first part: the whole solution, "Mám to", then its "Zopakovať" link and back
        if (onAllNodes(hasText("Ukázať celé riešenie")).fetchSemanticsNodes().isNotEmpty()) { click("Ukázať celé riešenie"); waitForIdle() }
        onAllNodes(hasText("Mám to") and hasClickAction())[0].performScrollTo().performClick(); waitForIdle()
        click("Zopakovať:"); waitForIdle()
        assertTrue("the link opened something on top of the grading", app.stack.last() !is Nav.MockGrade)
        runOnIdle { app.pop() }
        waitForIdle()
        assertTrue("back on the grading", app.stack.last() is Nav.MockGrade)
        onAllNodes(androidx.compose.ui.test.hasAnyDescendant(hasText("Mám to")) and androidx.compose.ui.test.isSelected()).assertCountEquals(1)
        // back from the first part asks; leaving saves nothing and never shows a result
        onAllNodes(hasContentDescription("Späť"))[0].performClick(); waitForIdle()
        assertTrue("still grading until the student confirms", app.stack.last() is Nav.MockGrade)
        shot("grade-leave-la1")
        onAllNodes(hasText("Prestať"))[0].performClick(); waitForIdle()
        assertEquals(1, app.stack.size)
        assertTrue("nothing saved", store.mocks.isEmpty())
    }

    /** Review 18. 11.: a mock graded without a word about any part loses nothing to review (it was not graded). */
    @Test fun mockWithNothingSaidRecordsNoMistakes() = runDesktopComposeUiTest(width = 400, height = 1600) {
        val store = Store(MemoryStateFile())
        store.pickCourse("la1")
        val app = AppState()
        setContent { TabulaTheme(ThemeMode.Light) { App(app, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}) } }
        runOnIdle { app.push(Nav.MockGrade("slon-2024")) }
        waitForIdle()
        fun loose(t: String) = SemanticsMatcher("text with \"$t\"") { n -> n.config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text.replace('\u00A0', ' ').contains(t) } }
        var guard = 0
        while (app.stack.last() is Nav.MockGrade && guard++ < 200) {
            if (onAllNodes(hasText("Ukázať celé riešenie")).fetchSemanticsNodes().isNotEmpty()) { onAllNodes(hasText("Ukázať celé riešenie"))[0].performClick(); waitForIdle(); continue }
            val next = listOf("Ďalšia časť", "Ďalší príklad", "Pozrieť výsledok").first { onAllNodes(loose(it) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
            onAllNodes(loose(next) and hasClickAction() and isEnabled())[0].performClick(); waitForIdle()
        }
        assertTrue("the result", app.stack.last() is Nav.MockResult)
        assertTrue("no mistakes from parts nothing was said about: ${store.mistakes.map { it.key }}", store.mistakes.isEmpty())
        // the result knows they were not graded: nothing lost to review, so no "Zopakovať" link
        assertTrue(store.mocks.single().nehodnotene.all { it.isNotEmpty() })
        assertTrue(onAllNodes(hasText("Zopakovať, čo stálo najviac")).fetchSemanticsNodes().isEmpty())
    }
}

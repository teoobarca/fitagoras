package dev.hamper.tabula

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import dev.hamper.tabula.ui.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.hamper.tabula.data.*
import dev.hamper.tabula.screens.*
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import kotlin.random.Random
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.synchronized
import dev.hamper.tabula.res.*
import dev.hamper.tabula.res.Res
import kotlin.concurrent.Volatile
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.state.StateFile

/** The one store of the process: the activity, the widget and notifications read the same state. */
object Tabula {
    @Volatile private var s: Store? = null
    /** The store, created on first use from [file] (Android: Tabula.store(context), the DataStore file). */
    fun store(file: () -> StateFile): Store = s ?: synchronized(this) { s ?: Store(file()).also { s = it } }
    /** The paper of a running mock, for the timer notification (its length and pacing segments). */
    @Volatile var mockPaper: String? = null
}

sealed interface Nav {
    data object Home : Nav
    /** A lesson, on the screen its run stands on (state/LessonRun.kt). */
    data class Lesson(val id: String) : Nav
    data class LessonEnd(val id: String) : Nav
    data class Item(val route: String) : Nav
    data class Template(val gen: String) : Nav
    data object Gem : Nav
    data object Counter : Nav
    data class Library(val section: String) : Nav
    data object Papers : Nav
    data class Paper(val paper: String, val n: Int) : Nav
    data class MockIntro(val paper: String) : Nav
    data class MockRun(val paper: String) : Nav
    data class MockGrade(val paper: String) : Nav
    data object MockResult : Nav
    /** Today's review dose (data/Review.kt), from Dnes and the plan. */
    data object Review : Nav
    /** "Zmiešané opakovanie" (Test → Všetky cvičenia): more than today's dose for whoever wants it. */
    data object ReviewMix : Nav
    data class ReviewEnd(val n: Int, val today: Boolean = false) : Nav
    /** One problem of the test, below the tab "Test" (screens/TestTab.kt). */
    data class TestProblem(val n: Int) : Nav
    /** Every exercise of the course, below the tab "Test" (what Nácvik listed). */
    data object Practice : Nav
    /** The page about the course's test, when its exam has one (screens/TestTopics.kt). */
    data object Topics : Nav
    data object Where : Nav
    /** Debug builds only: formulas rendered for a look (tabula://tex). */
    data object Tex : Nav
    data object Settings : Nav
}

enum class Tab { Dnes, Cesta, Nacvik }

class AppState {
    val stack = mutableStateListOf<Nav>(Nav.Home)
    var tab by mutableStateOf(Tab.Dnes)
    var mockEnd by mutableLongStateOf(0L)
    var mockPaper by mutableStateOf<String?>(null)
    var report by mutableStateOf<String?>(null)

    fun push(n: Nav) { if (stack.lastOrNull() != n) stack.add(n) }
    fun pop() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun home() { while (stack.size > 1) stack.removeAt(stack.lastIndex) }
    fun swap(n: Nav) { stack[stack.lastIndex] = n }

    /**
     * The whole test on time starts: its clock runs for the paper's minutes from now, by [SystemClock], so it holds
     * while the app is away. Here and not in the platform, so a platform with nothing of its own (the web, the desktop)
     * gets a running clock too (the web showed 0:00 at once, 19. 11.); Android adds the notification on top.
     */
    fun startMock(paper: String) {
        mockPaper = paper
        mockEnd = dev.hamper.tabula.compat.SystemClock.elapsedRealtime() + dev.hamper.tabula.screens.paperMinutes(paper) * 60_000L
    }

    /** Plan rows, mistakes, shortcuts, the widget and notifications all land here. */
    fun open(route: String?, store: Store) {
        if (route == null || route == "none") return
        val parts = route.split(':')
        when (parts[0]) {
            "home", "dnes" -> { home(); tab = Tab.Dnes }
            "cesta" -> { home(); tab = Tab.Cesta }
            "nacvik" -> { home(); tab = Tab.Nacvik }
            "lesson" -> {
                // a lesson whose lecture has not happened yet is not offered, even from an old link
                val l = parts.getOrNull(1)?.let { anyLesson(it) }?.takeIf { l -> Courses.none { c -> c.owns(l.id) && !c.shown().lessons.contains(l) } } ?: nextLesson(store) ?: return
                // a step named in the route ("Od začiatku", "Zopakovať", a link) starts the lesson over from there;
                // without one it goes on where it was left
                parts.getOrNull(2)?.toIntOrNull()?.let { store.restartLesson(l, it.coerceIn(0, l.steps.lastIndex)) }
                push(Nav.Lesson(l.id))
            }
            "gen", "template" -> parts.getOrNull(1)?.let { g ->
                // a template whose lesson waits for its lecture is not offered, even from an old link
                if (Courses.any { c -> c.templates.any { it.gen == g } && !c.shown().shows("gen:$g") }) return
                push(Nav.Template(if (g in Generators) g else "param"))
            }
            "concept", "theorem", "proof", "clip", "step" -> if (Courses.all { c -> courseOfRoute(route) != c || c.shown().offersKey(route) }) push(Nav.Item(route))
            "gem" -> push(Nav.Gem)
            "counter" -> push(Nav.Counter)
            "library" -> push(Nav.Library(parts.getOrNull(1) ?: "defs"))
            "papers", "mock", "mockrun" -> when {
                mockEnd > 0L && mockPaper != null -> push(Nav.MockRun(mockPaper!!))
                // "mock:<paper>": the intro of that whole paper (the test page's "one whole paper in 70 minutes")
                parts.getOrNull(1)?.let { id -> store.course.papers.any { it.id == id } } == true -> push(Nav.MockIntro(parts[1]))
                else -> push(Nav.Papers)
            }
            "grade" -> mockPaper?.let { home(); stack.add(Nav.MockGrade(it)) }
            "result" -> push(Nav.MockResult)
            "paper" -> {
                val p = parts.getOrNull(1) ?: return
                val n = parts.getOrNull(2)?.toIntOrNull() ?: 1
                if (paperById(p).problems.none { it.n == n }) return  // a problem waiting for its lecture
                push(Nav.Paper(p, n))
            }
            "review" -> push(Nav.Review)
            "mix" -> push(Nav.ReviewMix)
            // the plan was folded into the tab "Test" (18. 11.); an old link lands there
            "plan" -> { home(); tab = Tab.Nacvik }
            "priklad" -> parts.getOrNull(1)?.toIntOrNull()?.let { push(Nav.TestProblem(it)) }
            "cvicenia" -> push(Nav.Practice)
            "temy", "vediet" -> push(Nav.Topics)
            "where", "kde" -> push(Nav.Where)
            "tex" -> if (Env.DEBUG) push(Nav.Tex)
            "course" -> parts.getOrNull(1)?.let { id -> if (Courses.any { it.id == id }) { store.pickCourse(id); home() } }
            "settings", "profile" -> push(Nav.Settings)
        }
    }
}

@Composable
fun App(app: AppState, store: Store, clips: ClipHost, onMockStart: () -> Unit, onMockStop: () -> Unit) {
    val c = T.c
    val closeConfirm = remember { mutableStateOf(false) }
    // references to definitions and theorems are tappable everywhere, in the course that is open; the sheet's link to a
    // full card opens it on top
    // the "Zvuky" switch, read when a sound plays (ui/Sound.kt)
    Sounds.on = { store.sounds }
    // the colour of the course open on Dnes (ui/Smer.kt): a switch on the tabs blends it (Home), anything else sets it at once
    val accent = store.course.id
    SideEffect { if (CourseAccentNow != accent) { if (AccentT >= 1f) AccentFrom = accent; CourseAccentNow = accent } }
    CompositionLocalProvider(
        LocalHapticsOn provides { store.haptics }, LocalRefs provides RefIndexes[store.course.id],
        LocalFullCard provides { r: String, close: () -> Unit -> RouteItem(r, store, clips, Frame(0, 0, close, { app.report = it }, "Zavrieť"), close) },
    ) {
        Box(Modifier.fillMaxSize()) {
            // every screen in a column a phone wide, in the middle of a wider window (ui/AppColumn.kt)
            // fullscreen or picture-in-picture: the lesson under the black picture is out of TalkBack's reach
            AppColumn(if (clips.full || clips.pip) Modifier.clearAndSetSemantics {} else Modifier) {
            NavDisplay(
                backStack = app.stack,
                onBack = { app.pop() },
                transitionSpec = { (slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 8 } + fadeIn(tween(220))) togetherWith fadeOut(tween(160)) },
                popTransitionSpec = { fadeIn(tween(220)) togetherWith (slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 8 } + fadeOut(tween(200))) },
                predictivePopTransitionSpec = { fadeIn(tween(220)) togetherWith (slideOutHorizontally(tween(280)) { it / 8 } + fadeOut(tween(200))) },
                entryProvider = { key -> NavEntry(key) { Screen(key, app, store, clips, closeConfirm, onMockStart, onMockStop) } },
            )
            }
            // the playing clip's picture, above every screen, so fullscreen grows it instead of mounting a new one; over
            // the whole window, not the column: fullscreen on a phone on its side or a tablet fills the screen
            ClipLayer(clips, store)
        }
        if (closeConfirm.value) AlertDialog(
            onDismissRequest = { closeConfirm.value = false },
            title = { Text("Prerušiť lekciu?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Vrátiš sa na ten istý krok z Dnes alebo z Cesty.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { TextButton({ closeConfirm.value = false; dev.hamper.tabula.system.Telemetry.lessonLeft(); app.home() }) { Text("Prerušiť") } },
            dismissButton = { TextButton({ closeConfirm.value = false }) { Text("Pokračovať") } },
            containerColor = c.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = DialogWidth),
        )
        app.report?.let { item -> ReportDialog(item, { app.report = null }) { text -> store.report(item, text); app.report = null } }
        // a newer build: offered on the home screens only, never in a lesson or a running mock test; Settings can ask for it
        val now = currentTimeMillis()
        val onHome = app.stack.size == 1 && app.mockEnd == 0L
        val offer = when {
            // asked for in Settings: shown there or at home, never over a lesson or a running mock test
            dev.hamper.tabula.system.Updater.asked && app.mockEnd == 0L && app.stack.last().let { it == Nav.Settings || it == Nav.Home } -> store.release
            onHome -> dev.hamper.tabula.system.UpdateCore.offer(store.release, dev.hamper.tabula.system.Updater.current(), store.snoozedCode, store.snoozedUntil, now)
            else -> null
        }
        offer?.let { r -> UpdateSheet(store, r) { dev.hamper.tabula.system.Updater.asked = false } }
    }
}

/** The report goes to the author with the id of the step or item (Telemetry, also with statistics off); a copy stays on the phone. */
@Composable
private fun ReportDialog(item: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val c = T.c
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nahlásiť chybu", style = MaterialTheme.typography.headlineSmall) },
        text = {
            // the dialog keeps its buttons on screen and gives this the rest: with the keyboard open on a small phone at a
            // large font it scrolls instead of pushing "Odoslať" away
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Čo je zle? Pošle sa autorovi s označením „$item“.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                Spacer(Modifier.height(10.dp))
                // five lines from the start (Teo, 19. 11.: "môže byť jemne väčší"; 120 dp held three), growing with the text up to eight
                OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), minLines = 5, maxLines = 8, placeholder = { Text("Napr. v druhom riadku má byť mínus") })
            }
        },
        confirmButton = { TextButton({ onSave(text) }) { Text("Odoslať") } },
        dismissButton = { TextButton(onDismiss) { Text("Zrušiť") } },
        containerColor = c.card, shape = RoundedCornerShape(24.dp), modifier = Modifier.widthIn(max = DialogWidth),
    )
}

@Composable
private fun Screen(key: Nav, app: AppState, store: Store, clips: ClipHost, closeConfirm: MutableState<Boolean>, onMockStart: () -> Unit, onMockStop: () -> Unit) {
    val c = T.c
    val report: (String) -> Unit = { app.report = it }
    val route: (String) -> Unit = { app.open(it, store) }
    Box(Modifier.fillMaxSize().background(c.bg)) {
        when (key) {
            Nav.Home -> Home(app, store)
            // a quick way before the test (rychlo-…) is not a lesson of the path: its end goes back to where it was opened
            is Nav.Lesson -> remember(key.id) { anyLesson(key.id) }?.let { l ->
                LessonFlow(l, store, clips, report, { closeConfirm.value = true }, { app.pop() }) { if (quickOf(l.id) != null) app.pop() else app.swap(Nav.LessonEnd(l.id)) }
            }
            is Nav.LessonEnd -> {
                val l = LessonById[key.id]
                // the lesson's own course, not the one picked on Dnes: a DML lesson opened from a link while LA1 is picked
                // counted LA1's lessons and offered LA1's next one (web walk, 19. 11.)
                val course = l?.let { lesson -> Courses.firstOrNull { it.owns(lesson.id) } } ?: store.course
                val next = nextLesson(store, course)
                val checks = l?.steps?.filterIsInstance<Step.Check>().orEmpty()
                val clean = checks.count { s -> store.history("step:${s.id}").lastOrNull()?.let { it.ok && !it.aided } == true }
                // counted and named as the path counts it: toward the test when the lesson is for it, else all lessons of the course
                val sh = course.shown()
                val forTest = l != null && l in sh.testLessons
                val of = if (forTest) sh.testLessons else sh.lessons
                val doneN = of.count { it.id in store.lessonsDone }
                // the review line only when there is something to come back (a lesson all right at the first try has nothing)
                RoundEndScreen(
                    "Lekciu máš.", if (checks.isNotEmpty() && clean == checks.size) "" else "Chyby a to, čo išlo s pomocou, sa vrátia v opakovaní.",
                    next?.let { "Ďalšia lekcia" }, next?.let { n -> { app.swap(Nav.Lesson(n.id)) } },
                    nextName = next?.let { "${it.name} · ${it.minutes}\u00A0min" },
                    onDone = { app.home(); app.tab = Tab.Dnes }, kicker = l?.name, learn = l?.learn,
                    facts = listOfNotNull(
                        checks.size.takeIf { it > 0 }?.let { n -> "$clean\u00A0z\u00A0$n" to "${if (n == 1) "otázky" else "otázok"} na prvý pokus bez nápovedy" },
                        "$doneN\u00A0z\u00A0${of.size}" to "${if (of.size == 1) "lekcie" else "lekcií"} ${if (forTest) "na ${course.test.acc}" else "v predmete"}",
                    ),
                    art = l?.let { motifFor("lesson:${it.id}") },
                    // confetti for a lesson that went well (half the questions or more on the first try), never as irony
                    confetti = checks.isEmpty() || clean * 2 >= checks.size,
                    streak = if (remember(key) { store.cheerStreak() }) ({ store.streakCheeredNow() }) else null,
                    formula = l?.let { dev.hamper.tabula.ui.keyFormula(it) },
                    test = l?.let { remember(it.id) { dev.hamper.tabula.screens.testTeaser(it, course) } },
                )
            }
            // the place is described only when the tutor opens (a failure there stays in the tutor's panel)
            is Nav.Item -> CompositionLocalProvider(dev.hamper.tabula.screens.LocalTutorMiesto provides remember(key.route) { { dev.hamper.tabula.tutor.miestoZRouty(key.route) ?: dev.hamper.tabula.tutor.MiestoAppky(key.route, dev.hamper.tabula.data.courseOfRoute(key.route)?.id ?: if ("dml" in key.route) "dml" else "la1", "Miesto v appke: ${key.route}.") } }) {
                RouteItem(key.route, store, clips, Frame(0, 0, { app.pop() }, report, "Zavrieť")) { app.pop() }
            }
            is Nav.Template -> {
                var seed by remember(key) { mutableIntStateOf(Random.nextInt()) }
                val ex = remember(seed) { Generators.getValue(key.gen)(Random(seed)) }
                val first = remember(ex) { !store.tried("gen:${ex.template}") }
                ExerciseView(ex, solo = false, frame = Frame(0, 0, { app.pop() }, report, "Zavrieť príklad"), doneLabel = "Hotovo", onNew = { ok, aided -> recordExercise(store, ex, ok, aided); seed = Random.nextInt() }, firstTime = first) { ok, aided ->
                    recordExercise(store, ex, ok, aided); app.pop()
                }
            }
            Nav.Gem -> GemScreen({ app.pop() }, { app.swap(Nav.Paper("sova-2025", 2)) })
            Nav.Counter -> CounterScreen { app.pop() }
            is Nav.Library -> LibraryScreen(store, key.section, { app.pop() }, route)
            Nav.Papers -> PapersScreen(store, { app.pop() }, onPage = { route("temy") }) { app.push(Nav.MockIntro(it)) }
            is Nav.Paper -> { val p = paperById(key.paper); PaperProblemScreen(p, p.problems.firstOrNull { it.n == key.n } ?: p.problems.first(), store, report, route) { app.pop() } }
            is Nav.MockIntro -> MockIntroScreen(paperById(key.paper), { app.pop() }) { app.startMock(key.paper); onMockStart(); app.swap(Nav.MockRun(key.paper)) }
            is Nav.MockRun -> MockRunScreen(paperById(key.paper), app.mockEnd) { app.mockEnd = 0L; onMockStop(); app.swap(Nav.MockGrade(key.paper)) }
            is Nav.MockGrade -> MockGradeScreen(paperById(key.paper), store, route, onLeave = { app.pop() }) { app.swap(Nav.MockResult) }
            Nav.MockResult -> MockResultScreen(store, { app.pop() }) { app.pop(); app.open(it, store) }
            Nav.Review -> ReviewScreen(store, clips, report, { app.pop() }, today = true) { app.swap(Nav.ReviewEnd(it, today = true)) }
            Nav.ReviewMix -> ReviewScreen(store, clips, report, { app.pop() }) { app.swap(Nav.ReviewEnd(it)) }
            // today's dose done: said as done for today, with what comes back tomorrow (data/Review.kt)
            is Nav.ReviewEnd -> if (key.today) {
                val tomorrow = remember { reviewToday(store).tomorrow }
                RoundEndScreen("Opakovanie na dnes máš.", if (tomorrow > 0) "Zajtra sa ${slovakPlural(tomorrow, "vráti", "vrátia", "vráti")} $tomorrow ${slovakPlural(tomorrow, "vec", "veci", "vecí")}." else "Zajtra sa nevráti nič.", null, null, onDone = { app.pop() })
            }
            // nothing due: a round of none said "Kolo opakovania máš. 0 položiek"; it says what comes here instead
            else if (key.n == 0) RoundEndScreen("Teraz nie je čo opakovať.", "Chyby a úlohy, ktoré išli s pomocou, sa sem vrátia, keď nejaké budú.", null, null, onDone = { app.pop() })
                else RoundEndScreen("Kolo opakovania máš.", "${key.n} ${slovakPlural(key.n, "položka", "položky", "položiek")}. Čo išlo bez pomoci dvakrát za sebou, z opakovania zmizne.", null, null, onDone = { app.pop() })
            is Nav.TestProblem -> TestProblemScreen(store, key.n, { app.pop() }, route)
            Nav.Practice -> PracticeScreen(store, { app.pop() }, route)
            Nav.Topics -> TestTopicsScreen(store, { app.pop() }, route)
            Nav.Where -> WhereScreen(store, { app.pop() }, route)
            Nav.Tex -> StepFrame(0, 0, { app.pop() }) {
                for (t in TexSamples) { dev.hamper.tabula.math.Rich(t, style = MaterialTheme.typography.titleLarge, color = T.c.ink); Spacer(Modifier.height(10.dp)) }
            }
            Nav.Settings -> SettingsScreen(store, { app.pop() }, report)
        }
    }
}

private val BarHeight = 72.dp

/**
 * A switch of course asked on the tabs of Dnes: Home takes a picture of the page, changes the course under it and fades
 * the picture with the accent (one curve). Without Home on screen the course changes at once.
 */
object CourseSwitch {
    /** One request: the course and a number, so the same course asked twice is two requests; never cleared (clearing it
     *  restarted the effect that handles it and cancelled the picture before it was taken). */
    class Ask(val id: String, val n: Int)
    var asked by mutableStateOf<Ask?>(null)
    /** The last request handled: a Home composed again later never takes an old one for a new one. */
    var handled = 0
    fun ask(id: String) { asked = Ask(id, (asked?.n ?: 0) + 1) }
}

@Composable
private fun Home(app: AppState, store: Store) {
    val inset = BarHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val platform = LocalPlatform.current
    LaunchedEffect(store.lessonsDone.size, store.version) { platform.refreshWidget() }
    // system back on Cesta or Test goes to Dnes first, only Dnes leaves the app (emulator, 1.6.2: back on Cesta closed it);
    // only while Home is the top screen: during a push it is still composed and must not take a back meant for the new screen
    dev.hamper.tabula.compat.BackHandler(enabled = app.tab != Tab.Dnes && app.stack.size == 1) { app.tab = Tab.Dnes }
    Box(Modifier.fillMaxSize()) {
        // a switch of course (Teo, 18. 11.: "viac smooth prepínanie"): the old page stays as a picture (a GPU layer of its
        // last frame) over the new one, which composes under it; once the new page has drawn, the picture fades and the
        // accent turns on one 280 ms curve, so content, block, bar and buttons change together and only one page is
        // ever composed. The header and the tabs are the same pixels in both, so they stand; the bar is not in it.
        val course = store.course.id
        // a GPU layer is a recording of the live children, not a picture: an "old" layer showed the new page too (recorded
        // on the emulator), so the old page is taken as a bitmap at the tap, before the course changes under it
        val layer = rememberGraphicsLayer()
        var snap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        var veil by remember { mutableFloatStateOf(0f) }
        val pageBg = rememberUpdatedState(T.c.bg)
        val asked = CourseSwitch.asked
        LaunchedEffect(asked) {
            val ask = asked ?: return@LaunchedEffect
            if (ask.n <= CourseSwitch.handled) return@LaunchedEffect
            CourseSwitch.handled = ask.n
            val id = ask.id
            if (id == store.course.id) return@LaunchedEffect
            try {
                snap = try { layer.toImageBitmap() } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Throwable) { null }
                veil = 1f
                AccentFrom = store.course.id; AccentT = 0f
                store.pickCourse(id)
                // the new page's first frames are drawn under the picture: nothing moves before the curve can run smoothly
                withFrameNanos { }; withFrameNanos { }
                animate(0f, 1f, animationSpec = tween(280, easing = FastOutSlowInEasing)) { v, _ -> veil = 1f - v; AccentT = v }
            } finally {
                // also when Home leaves mid-switch: the course is chosen and the accent settles on it, never half-way
                if (store.course.id != id) store.pickCourse(id)
                veil = 0f; snap = null; AccentT = 1f; AccentFrom = id
            }
        }
        Box(Modifier.fillMaxSize().drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            drawLayer(layer)
            val picture = snap; val v = veil
            // the picture holds only what the old page drew (cards, text), clear between them: alone it let the new page
            // show through at full strength while it faded ("DMLML" on the tab, the DML card under the LA1 one; emulator,
            // 1.6.2). On the page's background, in one layer faded as a whole, the switch is a cross-fade.
            if (picture != null && v > 0f) drawIntoCanvas { cv ->
                cv.saveLayer(androidx.compose.ui.geometry.Rect(androidx.compose.ui.geometry.Offset.Zero, size), androidx.compose.ui.graphics.Paint().apply { alpha = v })
                drawRect(pageBg.value)
                drawImage(picture)
                cv.restore()
            }
        }) {
            AnimatedContent(app.tab, label = "tabs", modifier = Modifier.fillMaxSize(), transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(100)) }) { t ->
                // each course its own composition and scroll: a switch never shows one course at the other's scroll
                key(course) {
                    when (t) {
                        Tab.Dnes -> TodayScreen(store, inset) { app.open(it, store) }
                        Tab.Cesta -> PathScreen(store, inset) { app.open(it, store) }
                        Tab.Nacvik -> TestTabScreen(store, inset) { app.open(it, store) }
                    }
                }
            }
        }
        // lists end in a soft edge above the bar, so what goes under it reads as more to scroll, not as cut off; a short
        // one (16 dp): at 40 the last readable line already faded out above the bar (judges, 18. 11.). Not on Dnes:
        // its last row's reason sat right in the edge and paled out of reading (judges, 18. 11., round 7); there the
        // bar's own hairline ends the page and a line it cuts reads as more to scroll
        val c = T.c
        if (app.tab != Tab.Dnes) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = inset).fillMaxWidth().height(16.dp).background(Brush.verticalGradient(listOf(c.bg.copy(alpha = 0f), c.bg))))
        Bar(app.tab, { app.tab = it }, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Bar(tab: Tab, onTab: (Tab) -> Unit, modifier: Modifier) {
    val c = T.c
    val hx = rememberHaptics()
    Column(modifier.fillMaxWidth().background(c.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(BarHeight - 1.dp).padding(horizontal = 8.dp)) {
            val items = listOf(
                Triple(Tab.Dnes, "Dnes", Res.drawable.ic_today to Res.drawable.ic_today_fill),
                Triple(Tab.Cesta, "Cesta", Res.drawable.ic_route to Res.drawable.ic_route_fill),
                // "Test" in every course (Teo, 19. 11.: "prečo sa to v rôznych predmetoch volá inak?"); what the exam
                // itself is called ("2. písomka z DML") stays in the sentences about it
                Triple(Tab.Nacvik, "Test", Res.drawable.ic_timer to Res.drawable.ic_timer_fill),
            )
            for ((t, l, ic) in items) {
                val on = tab == t
                Column(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).clickable(role = Role.Tab) { hx.tick(); onTab(t) }.semantics { selected = on; contentDescription = l },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    // the active tab is a pill of the accent with its icon in the accent's ink (the style notes, 4)
                    // the pill's own fade is between tabs only; the one green in every course
                    val onP by animateFloatAsState(if (on) 1f else 0f, tween(180), label = "pill")
                    val fill = androidx.compose.ui.graphics.lerp(c.card, c.lesson.main, onP)
                    val ink = androidx.compose.ui.graphics.lerp(c.ink2, c.onAcc, onP)
                    Box(Modifier.size(60.dp, 32.dp).clip(RoundedCornerShape(16.dp)).background(fill), contentAlignment = Alignment.Center) {
                        Icon24(if (on) ic.second else ic.first, tint = ink)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(l, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (on) FontWeight(750) else FontWeight.SemiBold), color = if (on) c.ink else c.ink2)
                }
            }
        }
    }
}

/** Notation the courses use, for a look on the device (debug route tabula://tex). */
private val TexSamples = listOf(
    "\$f(x,y,z)=(\\sqrt{y\\cdot z},\\sqrt{x\\cdot z},\\sqrt[3]{y\\cdot z})\$",
    "\$\\overline{A\\cup B}=\\overline{A}\\cap\\overline{B}\$, \$g\\circ f\$, \$A\\supseteq B\\subsetneq C\$, \$A\\nsubseteq B\$",
    "\$\\bigcup_{k=1}^{n}A_k\$, \$\\bigcap_{k=1}^{n}A_k\$, \$\\prod_{k=1}^{n}k\$, \$\\sum_{i=1}^{n}\\frac{1}{i^2}\$",
    "\$\\gcd(420,422)=2\$, \$\\lcm(4,6)=12\$, \$a\\bmod m\$, \$3\\nmid 10\$, \$\\varphi(60)=16\$",
    "\$x\\equiv 4\\pmod{91}\$, \$\\lfloor x\\rfloor\$, \$V(n)\\nRightarrow V(n+1)\$, \$\\top\$, \$\\bot\$, \$A\\sim B\$, \$\\Theta\$",
)

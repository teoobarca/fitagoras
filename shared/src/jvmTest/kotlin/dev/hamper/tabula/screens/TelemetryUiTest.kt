package dev.hamper.tabula.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.sun.net.httpserver.HttpServer
import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.SystemBack
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.media.SilentPlayer
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Context
import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.system.deviceHash
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress

/**
 * The statistics of a lesson tapped through on the desktop JVM, sent over HTTP (docs/SYSTEM.md, Verification 3): the
 * real screens make the events, the real sender posts them. To a stand-in server here; with FITAGORAS_TELEMETRY_URL
 * (and _KEY) to a real one, e.g. a local copy of the app's server, which must take every batch.
 */
@OptIn(ExperimentalTestApi::class)
class TelemetryUiTest {
    private val bodies = mutableListOf<String>()
    private var server: HttpServer? = null
    private val realUrl = System.getenv("FITAGORAS_TELEMETRY_URL")

    @Before fun setUp() {
        Env.install(debug = true, clipsGuestToken = "", versionName = "1.4.0")  // debug: the fields say what is right; flush() sends as a release
        installJvmAssets()
        ClipLibrary.load()
        val url = realUrl ?: HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).also { s ->
            s.createContext("/") { x -> bodies += x.requestBody.readBytes().decodeToString(); x.sendResponseHeaders(204, -1); x.close() }
            s.start(); server = s
        }.let { "http://127.0.0.1:${it.address.port}" }
        Telemetry.connect(url, System.getenv("FITAGORAS_TELEMETRY_KEY") ?: "test", device = deviceHash("desktop-ui-test"),
            ctx = Context(maker = "JetBrains", model = "Desktop UI test", os = "17", sdk = 34, screen = "411x914", font = 1f, dark = false,
                lang = "sk-SK", tz = "Europe/Prague", net = "wifi", emulator = false))
    }

    @After fun tearDown() { Telemetry.disconnect(); server?.stop(0) }

    private fun ComposeUiTest.tap(text: String) { onAllNodes(hasText(text, substring = true) and hasClickAction()).onFirst().performClick(); waitForIdle() }
    private fun ComposeUiTest.shows(text: String) = onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()

    private fun events(): List<JSONObject> = bodies.flatMap { b -> JSONObject(b).getJSONArray("events").let { a -> (0 until a.length()).map { a.getJSONObject(it) } } }

    @Test fun aWrongAnswerAStepAndLeavingAreSent() = runComposeUiTest {
        // a check whose options are plain words (a formula renders as math, not as its source text)
        val (l, at) = dev.hamper.tabula.data.Dml.lessons.firstNotNullOf { les ->
            les.steps.indices.firstOrNull { k -> k > 0 && k < les.steps.size - 1 && (les.steps[k] as? Step.Check)?.options?.none { '$' in it || '\\' in it } == true }?.let { les to it }
        }
        val check = l.steps[at] as Step.Check
        val store = Store(MemoryStateFile())
        var left = false
        store.restartLesson(l, at)
        Telemetry.opened("dml", "2")
        setContent { TabulaTheme(ThemeMode.Light) { LessonFlow(l, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}, { left = true }) {} } }
        waitForIdle()
        val wrong = check.options.indices.first { it != check.correct }
        onAllNodes(hasText(check.options[wrong]) and hasClickAction()).onFirst().performClick(); waitForIdle()
        tap("Skúsiť znova")
        onNode(hasContentDescription("dbg:correct")).performClick(); waitForIdle()
        tap("Pokračovať")                                    // on to the next step
        repeat(60) { if (!left) { SystemBack.press(); waitForIdle() } }   // back through the check (its own screens), then out
        assertTrue(left)

        assertTrue("the server took every batch", Telemetry.flush(app = "1.4.0"))
        if (realUrl != null) return@runComposeUiTest
        val body = JSONObject(bodies.single())
        assertEquals(deviceHash("desktop-ui-test"), body.getString("device"))
        assertEquals("Desktop UI test", body.getJSONObject("ctx").getString("model"))
        val ev = events()
        assertEquals(listOf("open", "lesson_started", "answer", "step", "lesson_left"), ev.map { it.getString("type") })
        with(ev[2]) { assertEquals(false, getBoolean("ok")); assertEquals(check.options[wrong], getString("given")); assertEquals(1, getInt("attempt")); assertEquals(at, getInt("pos")) }
        with(ev[3]) { assertEquals(at, getInt("pos")); assertEquals("done", getString("how")); assertEquals("Check", getString("kind")); assertTrue(has("ms")) }
        assertTrue("left on the step it was on when closed (back walks the history down)", ev[4].getInt("pos") <= at)
        assertTrue(ev.all { it.has("at") && it.getString("course") == "BI-DML.21" })
    }

    @Test fun knowItIsASkipAndTheLastStepFinishes() = runComposeUiTest {
        val l = LessonById.getValue("dml-tc-rea")
        val store = Store(MemoryStateFile())
        store.restartLesson(l); repeat(14) { store.run(l).next() }   // walked to the solo exercise at the end
        Telemetry.opened("dml", null)
        setContent { TabulaTheme(ThemeMode.Light) { LessonFlow(l, store, ClipHost(SilentPlayer(), onPip = {}), {}, {}, {}) {} } }
        waitForIdle()
        tap("Viem to")
        assertTrue(Telemetry.flush(app = "1.4.0"))
        if (realUrl != null) return@runComposeUiTest
        val ev = events()
        val step = ev.single { it.getString("type") == "step" }
        assertEquals(14, step.getInt("pos")); assertEquals("known", step.getString("how"))
        assertEquals("lesson_finished", ev.last().getString("type"))
    }
}

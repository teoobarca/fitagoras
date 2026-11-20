@file:OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)

package dev.hamper.tabula.snap

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeActivityScenarioOption
import com.github.takahirom.roborazzi.RoborazziComposeCaptureOption
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.fontScale
import com.github.takahirom.roborazzi.activityTheme
import com.github.takahirom.roborazzi.size
import com.github.takahirom.roborazzi.uiMode
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.data.AllLessons
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.data.TheoremById
import dev.hamper.tabula.screens.paperById
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.compat.media.ExoClipPlayer
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import android.os.Looper
import org.robolectric.Shadows.shadowOf
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Duration
import java.lang.reflect.Proxy
import kotlin.math.roundToInt
import dev.hamper.tabula.store

/**
 * JVM screenshots without the emulator: Robolectric with native graphics (the real Android text and Canvas stack,
 * our fonts from res/font), captured by Roborazzi. The status bar and the gesture bar take their room (insets, as on
 * the phone) but are drawn empty: no clock or icons.
 *
 * Agents: `./gradlew :app:snap -Psnap.route=lesson:dml-mz-mnoziny:3 -Psnap.out=/tmp/x` (docs/SYSTEM.md), or a class
 * of your own in this package that extends [Shots], calls [shoot] and runs with `-Psnap.test=YourShots`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SnapSdk], qualifiers = "xxhdpi")
abstract class Shots

/** The emulator runs API 37; Robolectric's image of the same level renders text and Canvas with its native stack. */
const val SnapSdk = 37

data class Look(val dark: Boolean, val widthDp: Int, val heightDp: Int, val fontScale: Float) {
    /** File name suffix: light-427-1.0 */
    val tag get() = "${if (dark) "dark" else "light"}-$widthDp-$fontScale"
    /** The Pixel's status bar (camera cutout) is 52 dp; a plain one 24 dp. */
    val statusDp get() = if (widthDp == Pixel.first) 52 else 24
}

/** The phone the emulator copies (Pixel 9 Pro, 1280×2856 px at 480 dpi; 427 dp is 1 px wider) and a narrow phone. */
val Pixel = 427 to 952
val Narrow = 360 to 800

object Snap {
    private fun prop(name: String) = System.getProperty("snap.$name")?.takeIf { it.isNotBlank() }

    /** Folder for the PNGs: -Psnap.out (default build/snap in the module). */
    val out: File get() = File(prop("out") ?: "build/snap").absoluteFile

    /** Every combination of -Psnap.theme (light,dark), -Psnap.width (pixel,360 or a dp number), -Psnap.font (1.0,1.3). */
    fun looks(theme: String = "light", width: String = "pixel", font: String = "1.0"): List<Look> {
        val themes = (prop("theme") ?: theme).split(',').map { it.trim() == "dark" }
        val widths = (prop("width") ?: width).split(',').map { w ->
            when (val t = w.trim()) { "pixel" -> Pixel; "360", "narrow" -> Narrow; else -> t.toInt() to Pixel.second }
        }.map { (w, h) -> w to (prop("height")?.toInt() ?: h) }
        val fonts = (prop("font") ?: font).split(',').map { it.trim().toFloat() }
        return themes.flatMap { d -> widths.flatMap { (w, h) -> fonts.map { f -> Look(d, w, h, f) } } }
    }

    fun get(name: String): String? = prop(name)
}

/**
 * A status bar [statusDp] high and gesture navigation (24 dp), as insets only. Robolectric's SystemUi is not public
 * yet, hence reflection and a proxy for the status bar size.
 */
private fun phoneBars(statusDp: Int) {
    val ui = Class.forName("org.robolectric.shadows.SystemUi")
    val bars = ui.getDeclaredMethod("systemUiForDefaultDisplay").apply { isAccessible = true }.invoke(null)
    val behavior = Class.forName("org.robolectric.shadows.SystemUi\$StatusBarBehavior")
    val top = Class.forName("org.robolectric.shadows.SystemUi\$SystemBar\$Side").enumConstants.first { (it as Enum<*>).name == "TOP" }
    val status = Proxy.newProxyInstance(behavior.classLoader, arrayOf(behavior)) { _, m, _ ->
        when (m.name) {
            "calculateSide" -> top
            "calculateSize" -> (statusDp * ApplicationProvider.getApplicationContext<Context>().resources.displayMetrics.density).roundToInt()
            else -> null
        }
    }
    val nav = ui.getDeclaredField("GESTURAL_NAVIGATION").apply { isAccessible = true }.get(null)
    ui.declaredMethods.first { it.name == "setBehavior" }.apply { isAccessible = true }.invoke(bars, status, nav)
}

/** Transparent bars over the app, as MainActivity sets them; without it the window paints a dark scrim under navigation. */
private object EdgeToEdge : RoborazziComposeActivityScenarioOption {
    override fun configureWithActivityScenario(scenario: ActivityScenario<out Activity>) {
        scenario.onActivity { a ->
            val clear = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
            (a as ComponentActivity).enableEdgeToEdge(clear, clear)
            a.window.isNavigationBarContrastEnforced = false // gesture navigation on the phone has no scrim either
        }
    }
}

/**
 * Lets entrance animations finish before the capture (a screen pushed on the stack fades in from alpha 0): one second
 * of the main looper's virtual time, so frames run without waiting.
 */
private object Settle : RoborazziComposeCaptureOption {
    override fun beforeCapture() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1))
        // the render thread runs in real time: give it a moment to draw the settled frame before the copy
        Thread.sleep(50)
        shadowOf(Looper.getMainLooper()).idle()
    }
    override fun afterCapture() {}
}

/** Golden mode compares with the committed image instead of writing; everything else just writes. */
internal var task: RoborazziTaskType = RoborazziTaskType.Record

/** Renders [content] in the app theme at [look] into [file]. */
@OptIn(ExperimentalRoborazziApi::class)
fun shoot(file: File, look: Look, content: @Composable () -> Unit) {
    for (attempt in 1..Attempts) {
        if (task == RoborazziTaskType.Record) {
            capture(file, look, content)
            if (!blank(file)) return
        } else try {
            return capture(file, look, content) // a comparison cannot see the empty frame: a difference is rendered again
        } catch (e: AssertionError) {
            if (attempt == Attempts) throw e
        }
        System.err.println("snap: ${file.name} came out empty or different (attempt $attempt), rendering again")
        // the hardware renderer draws on its own thread in real time, not on the looper's virtual clock: on a busy
        // machine the copy came before the frame, and an immediate retry came early again. Wait for it, longer each time.
        Thread.sleep(400L * attempt)
    }
    throw AssertionError("${file.name}: the screen stayed empty in $Attempts attempts")
}

/** How often a capture is taken again when it came out empty (with a growing real-time wait in between). */
private const val Attempts = 5

/**
 * One colour over the whole image: the window was captured before Compose drew into it (the window's own grey, not the
 * app's background). Happens to about one capture in 150 on an idle machine and far more often under load, when the
 * render thread is late; [shoot] waits and takes it again.
 * Every pixel is looked at: a flat screen of thin type on the page (the "Dokazuješ" bar alone, no card under it) has
 * strokes a sparse grid of samples falls between.
 */
private fun blank(file: File): Boolean {
    val img = javax.imageio.ImageIO.read(file) ?: return true
    val first = img.getRGB(0, 0)
    val row = IntArray(img.width)
    for (y in 0 until img.height) { img.getRGB(0, y, img.width, 1, row, 0, img.width); if (row.any { it != first }) return false }
    return true
}

private fun capture(file: File, look: Look, content: @Composable () -> Unit) {
    ClipLibrary.load() // as MainActivity
    // a direction of the look (ui/Smer.kt): -Psnap.smer=Zosit|Sadzba|Odvazna
    Snap.get("smer")?.let { dev.hamper.tabula.ui.SmerNow = dev.hamper.tabula.ui.Smer.valueOf(it) }
    // the look of the screens inside a lesson (ui/LekciaVzhlad.kt): -Psnap.look=old for 1.6.2's, for comparison
    Snap.get("look")?.let { dev.hamper.tabula.ui.LekciaNova = it != "old" }
    phoneBars(look.statusDp)
    file.parentFile?.mkdirs()
    captureRoboImage(
        file = file,
        roborazziOptions = RoborazziOptions(
            taskType = task,
            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0f), // one wrong glyph must fail; renders repeat exactly
            recordOptions = RoborazziOptions.RecordOptions(resizeScale = 1.0),
        ),
        roborazziComposeOptions = RoborazziComposeOptions {
            size(look.widthDp, look.heightDp)
            fontScale(look.fontScale)
            activityTheme(android.R.style.Theme_Material_NoActionBar) // not fullscreen: the bars keep their insets
            addOption(EdgeToEdge)
            addOption(Settle)
            uiMode(if (look.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO) // values-night too
        },
    ) {
        TabulaTheme(if (look.dark) ThemeMode.Dark else ThemeMode.Light) {
            Box(Modifier.fillMaxSize().background(T.c.bg)) { content() }
        }
    }
}

/** Shorthand: one PNG per look, named `<name>-<look.tag>.png` under [dir]. */
fun shoot(name: String, looks: List<Look> = Snap.looks(), dir: File = Snap.out, content: @Composable () -> Unit) {
    for (l in looks) shoot(File(dir, "$name-${l.tag}.png"), l, content)
}

/**
 * A real screen of the app inside the real App (navigation, dialogs, the bottom bar): `home`, `cesta`, `nacvik`,
 * `lesson:<id>:<step>` (0-based), `concept:<id>`, `theorem:<id>`, `proof:<id>`, `clip:<id>`, `gen:<id>`, `paper:<id>:<n>`,
 * `library:defs`, `plan`, `where`, `review`, `papers`, `settings`; `a>b` opens a, then b on top. [course]: la1 or dml,
 * the course Dnes and Cesta show. Lessons, generators and items open even before their lecture (unlike a link on the
 * phone), so content waiting for its lecture can be checked too.
 */
@Composable
fun AppAt(route: String, course: String? = null) {
    val ctx = LocalContext.current
    val store = remember { Tabula.store(ctx) }
    val app = remember {
        store.courseId = course // the singleton outlives a capture: no course left over from the previous one
        // the student's sittings, -Psnap.sitting=la1-test2=2026-11-18T18:00[;exam=time]: the tab "Test" after a test
        // written; without it none is picked (a fresh install). Set every time: the singleton outlives a capture
        store.terms.clear()
        Snap.get("sitting")?.split(';')?.forEach { kv -> kv.split('=', limit = 2).let { (k, v) -> store.terms[k.trim()] = dev.hamper.tabula.compat.time.LocalDateTime.parse(v.trim()) } }
        AppState().also { a -> for (r in route.split('>').map { it.trim() }) navOf(r, a, store)?.let(a::push) }
    }
    val clips = remember { ClipHost(ExoClipPlayer(ctx), onPip = {}) }
    DisposableEffect(clips) { onDispose { clips.release() } }
    App(app, store, clips, {}, {})
}

private val Plain = setOf("home", "dnes", "cesta", "nacvik", "gem", "counter", "library", "papers", "review", "mix", "plan", "cvicenia", "temy", "where", "kde", "settings", "profile", "result")

/**
 * The screen [r] names (null: the app's router opened it), or an error: the app quietly falls back (an unknown concept closes, a missing paper problem
 * shows the first one), which would put a plausible wrong screen in the PNG after an id was renamed.
 */
private fun navOf(r: String, a: AppState, store: Store): Nav? {
    val p = r.split(':')
    val id = p.getOrNull(1).orEmpty()
    fun need(ok: Boolean, what: String) { if (!ok) throw IllegalArgumentException("snap route '$r': $what") }
    return when (p[0]) {
        "lesson" -> {
            val l = LessonById[id]; need(l != null, "no lesson '$id'")
            val n = p.getOrNull(2)?.toIntOrNull() ?: 0; need(n in l!!.steps.indices, "the lesson has steps 0..${l.steps.lastIndex}")
            store.restartLesson(l, n)
            Nav.Lesson(id)
        }
        "gen", "template" -> { need(id in Generators, "no generator '$id'"); Nav.Template(id) }
        "concept" -> { need(id in ConceptById, "no concept '$id'"); Nav.Item(r) }
        "theorem" -> { need(id in TheoremById, "no theorem '$id'"); Nav.Item(r) }
        "proof" -> { need(id in ProofById, "no proof '$id'"); Nav.Item(r) }
        "clip" -> { need(ClipLibrary.all.any { it.id == id }, "no clip '$id'"); Nav.Item(r) }
        "step" -> { need(AllLessons.any { l -> l.steps.any { it.id == id } }, "no step '$id'"); Nav.Item(r) }
        "priklad" -> { need(id.toIntOrNull() != null, "priklad:<n>, a problem of the test"); Nav.TestProblem(id.toInt()) }
        "paper" -> {
            val n = p.getOrNull(2)?.toIntOrNull() ?: 1
            need(paperById(id).let { it.id == id && it.problems.any { q -> q.n == n } }, "no problem $n in paper '$id' (or it waits for its lecture)")
            Nav.Paper(id, n)
        }
        else -> {
            need(p[0] in Plain, "unknown route (docs/SYSTEM.md lists them)")
            a.open(r, store) // the app's own router: tabs, lists and settings have no ids to get wrong
            null
        }
    }
}

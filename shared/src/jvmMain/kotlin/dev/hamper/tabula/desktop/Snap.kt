package dev.hamper.tabula.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Env
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.compat.media.SilentPlayer
import dev.hamper.tabula.data.AllLessons
import dev.hamper.tabula.data.ConceptById
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.ProofById
import dev.hamper.tabula.data.TheoremById
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.screens.ClipLibrary
import dev.hamper.tabula.screens.paperById
import dev.hamper.tabula.state.MemoryStateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.system.exitProcess

/*
 * Screenshots on the desktop JVM, no emulator and no Android: Compose Multiplatform draws the real screens with Skia
 * into an off-screen image. ./gradlew :shared:snapDesktop -Psnap.route=lesson:<id>:<step>,concept:<id> -Psnap.out=<dir>
 * [-Psnap.theme=light,dark] [-Psnap.width=pixel,360] [-Psnap.font=1.0,1.3] [-Psnap.height=<dp>] [-Psnap.density=3]
 * [-Psnap.course=la1|dml]. Routes as the Android snap task (docs/SYSTEM.md); a wrong id fails.
 */

data class Look(val dark: Boolean, val widthDp: Int, val heightDp: Int, val fontScale: Float) {
    val tag get() = "${if (dark) "dark" else "light"}-$widthDp-$fontScale"
    val statusDp get() = if (widthDp == Pixel.first) 52 else 24
}

/** The emulator's Pixel (1281×2856 px at density 3) and a narrow phone. */
private val Pixel = 427 to 952
private val Narrow = 360 to 800

private fun prop(name: String) = System.getProperty("snap.$name")?.takeIf { it.isNotBlank() }

fun looks(): List<Look> {
    val themes = (prop("theme") ?: "light").split(',').map { it.trim() == "dark" }
    val widths = (prop("width") ?: "pixel").split(',').map { w ->
        when (val t = w.trim()) { "pixel" -> Pixel; "360", "narrow" -> Narrow; else -> t.toInt() to Pixel.second }
    }.map { (w, h) -> w to (prop("height")?.toInt() ?: h) }
    val fonts = (prop("font") ?: "1.0").split(',').map { it.trim().toFloat() }
    return themes.flatMap { d -> widths.flatMap { (w, h) -> fonts.map { f -> Look(d, w, h, f) } } }
}

/** Process-wide setup, as MainActivity does on the phone; the progress lives in memory. */
fun prepare(): Store {
    Env.install(debug = false, clipsGuestToken = "", versionName = "desktop")
    installJvmAssets()
    ClipLibrary.load()
    return Tabula.store { MemoryStateFile() }
}

/** Renders [content] in the app theme at [look] (density [density] px per dp) into a PNG. */
fun shoot(file: File, look: Look, density: Float = prop("density")?.toFloat() ?: 3f, content: @Composable () -> Unit) {
    val w = (look.widthDp * density).toInt()
    val h = (look.heightDp * density).toInt()
    val scene = ImageComposeScene(w, h, Density(density, look.fontScale)) {
        TabulaTheme(if (look.dark) ThemeMode.Dark else ThemeMode.Light) {
            // the phone's bars as space (no clock or icons): a 52 dp status bar on the Pixel, 24 dp elsewhere, 24 dp gesture bar
            Box(Modifier.fillMaxSize().background(T.c.bg).padding(top = look.statusDp.dp, bottom = 24.dp)) { content() }
        }
    }
    try {
        // one second of frames: entrance animations (a pushed screen fades in) finish before the capture
        var t = 0L
        repeat(61) { scene.render(t); t += 16_666_667L }
        val img = scene.render(t)
        file.parentFile?.mkdirs()
        file.writeBytes(img.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    } finally {
        scene.close()
    }
}

/** A real screen inside the real App, by route (`home`, `lesson:<id>:<step>`, `concept:<id>`, ... `a>b` stacks b on a). */
@Composable
fun AppAt(store: Store, route: String, course: String?) {
    val app = remember {
        store.courseId = course
        AppState().also { a -> for (r in route.split('>').map { it.trim() }) navOf(r, a, store)?.let(a::push) }
    }
    val clips = remember { ClipHost(SilentPlayer(), onPip = {}) }
    DisposableEffect(clips) { onDispose { clips.release() } }
    App(app, store, clips, {}, {})
}

private val Plain = setOf("home", "dnes", "cesta", "nacvik", "gem", "counter", "library", "papers", "review", "mix", "plan", "cvicenia", "where", "kde", "settings", "profile", "result")

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
            need(paperById(id).let { it.id == id && it.problems.any { q -> q.n == n } }, "no problem $n in paper '$id'")
            Nav.Paper(id, n)
        }
        else -> {
            need(p[0] in Plain, "unknown route")
            a.open(r, store)
            null
        }
    }
}

/** Compose's lifecycle wants the UI thread: everything runs on the AWT event thread (headless). */
fun main() = javax.swing.SwingUtilities.invokeAndWait { run() }

private fun run() {
    val routes = prop("route")?.split(',')?.map { it.trim() } ?: run { System.err.println("-Psnap.route=<route>[,<route>…]"); exitProcess(2) }
    val out = File(prop("out") ?: "build/snap-desktop").absoluteFile
    val store = prepare()
    val started = System.nanoTime()
    var n = 0
    for (r in routes) for (l in looks()) {
        val f = File(out, "${r.replace(':', '_').replace('>', '+')}-${l.tag}.png")
        shoot(f, l) { AppAt(store, r, prop("course")) }
        println(f.path); n++
    }
    System.err.println("snapDesktop: $n PNGs in ${(System.nanoTime() - started) / 1_000_000} ms")
    exitProcess(0)
}

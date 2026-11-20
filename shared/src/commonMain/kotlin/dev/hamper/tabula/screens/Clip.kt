package dev.hamper.tabula.screens

import dev.hamper.tabula.compat.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.positionInRoot
import dev.hamper.tabula.compat.LocalConfiguration
import dev.hamper.tabula.compat.Configuration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import dev.hamper.tabula.compat.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.compat.media.PlaybackException
import dev.hamper.tabula.compat.media.PlaybackParameters
import dev.hamper.tabula.compat.media.Player
import dev.hamper.tabula.compat.media.HttpDataSource
import dev.hamper.tabula.compat.graphics.Rect
import dev.hamper.tabula.system.ClipFocus
import dev.hamper.tabula.platform.Assets
import dev.hamper.tabula.platform.ResourceIndex
import dev.hamper.tabula.compat.media.PlayerSurface
import dev.hamper.tabula.compat.media.SURFACE_TYPE_TEXTURE_VIEW
import dev.hamper.tabula.Env
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.ClipSpeeds
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.res.*
import dev.hamper.tabula.compat.graphics.*
import org.jetbrains.compose.resources.DrawableResource

/** The app's server: lecture clips, statistics and error reports (the update check: Env.UPDATE_URL, the same host). */
const val Server = "https://fitagoras.hamper.dev"

/** The guest key for lecture clips, baked in at build time from secrets.properties (never in git); the server opens
 *  only BI-LA1 clips with it. null in a build without the file: then the clips are read as captions. */
val ClipKey: String? get() = Env.CLIPS_GUEST_TOKEN.ifEmpty { null } // read each time: Env is set in FitestApp.onCreate

/** The 19 clips with their real captions (assets/clips.json, built by scripts/clips.py from /api/captions). */
object ClipLibrary {
    var all: List<ClipDef> = emptyList()
        private set

    fun load() {
        if (all.isNotEmpty()) return
        // clips.json (LA1 kapitola 2) and one clips-<topic id>.json per topic, e.g. clips-dml-mi.json
        val files = listOf("clips.json") + (ResourceIndex.files.filter { it.startsWith("clips-") && it.endsWith(".json") }.sorted())
        val objs = files.flatMap { f -> dev.hamper.tabula.compat.json.JSONArray(Assets.read(f)).let { arr -> (0 until arr.length()).map { arr.getJSONObject(it) } } }
        all = objs.map { o ->
            val t = o.getJSONObject("task")
            val cues = o.getJSONArray("cues")
            ClipDef(
                o.getString("id"), o.getInt("lesson"), o.getString("rec"), o.getString("lecture"), o.getDouble("from"), o.getDouble("to"),
                o.getString("why"), o.getString("before"), o.optBoolean("warning"),
                ClipTask(t.getString("q"), t.getJSONArray("options").let { a -> (0 until a.length()).map { a.getString(it) } }, t.getInt("correct"), t.getString("why"),
                    t.optJSONObject("wrong")?.let { w -> w.keys().asSequence().associate { it.toInt() to w.getString(it) } } ?: emptyMap()),
                (0 until cues.length()).map { k ->
                    val cu = cues.getJSONObject(k); val ws = cu.getJSONArray("words")
                    ClipCue(cu.getDouble("t0"), cu.getDouble("t1"), (0 until ws.length()).map { w -> ws.getJSONObject(w).let { ClipWord(it.getDouble("t0"), it.getDouble("t1"), it.getString("w")) } })
                },
                o.optString("course", "la1"),
                o.optString("teacher").ifEmpty { null },
            )
        }
    }

    fun byId(id: String) = all.first { it.id == id }
}

/**
 * Owned by the activity so the same player survives into picture-in-picture. [player]: ExoPlayer on Android
 * (ExoClipPlayer), the page's <video> on the web (web/WebClipPlayer). [canPip]: the browser may have no picture-in-picture.
 * [fullOnTurn]: turning the device sideways goes fullscreen (a phone; not a desktop browser, whose window is wide anyway).
 */
class ClipHost(val player: Player, val onPip: () -> Unit, private val onFull: (Boolean) -> Unit = {}, val canPip: Boolean = true, val fullOnTurn: Boolean = true) {
    // spoken media; the player does not touch audio focus itself (it would take it for good), ClipFocus does
    private val focusPort = player.focusPort(onPause = { player.pause() }, onResume = { player.play() })
    /** Music pauses while a clip plays and resumes after it. */
    val focus = ClipFocus(focusPort).also { focusPort.attach(it) }
    var open by mutableStateOf(false)
    /** The player waits for data or a decoded frame: a spinner over the picture, the play button keeps saying what the user wants. */
    var loading by mutableStateOf(false)
        private set
    /** Playing, or about to play once loaded: what the play button shows. */
    var wants by mutableStateOf(false)
        private set

    init {
        player.addListener(object : Player.Listener {
            // an error leaves the player idle with play still requested: that reads as stopped, and a tap retries
            override fun onPlaybackStateChanged(state: Int) { loading = state == Player.STATE_BUFFERING; wants = player.playWhenReady && state != Player.STATE_ENDED && state != Player.STATE_IDLE; audible() }
            override fun onPlayWhenReadyChanged(play: Boolean, reason: Int) { wants = play && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE; audible() }
        })
    }

    private fun audible() = focus.playing(player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE)

    /** The clip is gone for now: paused, and the music gets its focus back. */
    fun leave() { player.pause(); focus.stop() }

    /** Every seek keeps the play state: playing goes on playing from there, paused stays paused on that frame. */
    fun seek(ms: Long) {
        // after the end the button says play: a seek there only moves, it does not start the clip again
        if (player.playbackState == Player.STATE_ENDED) player.pause()
        player.seekTo(ms.coerceIn(0, maxOf(0, player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE)))
    }

    fun toggle() {
        val p = player
        if (p.playWhenReady && p.playbackState != Player.STATE_ENDED && p.playbackState != Player.STATE_IDLE) p.pause()
        else { if (p.playbackState == Player.STATE_ENDED) p.seekTo(0); if (p.playbackState == Player.STATE_IDLE) p.prepare(); p.play() }
    }
    /** Picture-in-picture: the app stays composed underneath and the one picture fills the small window. */
    var pip by mutableStateOf(false)
    /** The clip screen is leaving (a step or screen change animates it out): the picture goes at once, not after the animation. */
    var leaving by mutableStateOf(false)
    /** The part of the screen the inline picture may use (the step's body): it never paints over the footer panel. */
    var area by mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
    var bounds: Rect? = null
    /** Where the lesson's video frame is, in root pixels: the one player picture ([ClipLayer]) sits on it. */
    var inline by mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
    /** What plays, for the captions over the fullscreen picture. */
    var clip by mutableStateOf<ClipDef?>(null)
    /** A line over the picture: the server cutting the clip, or why it does not play. */
    var note by mutableStateOf<String?>(null)
    var full by mutableStateOf(false)
        private set
    var captions by mutableStateOf(true)

    /** Fullscreen in landscape with the bars hidden, or back to the lesson; the same player and picture throughout. */
    fun goFull(on: Boolean) { if (full != on) { full = on; onFull(on) } }
    private var loaded: String? = null

    /** Streams the clip from the server; the token rides in the header, a first cut can take a while. */
    fun load(clip: ClipDef, token: String) {
        val key = "${clip.id}|$token"
        if (loaded == key && player.playerError == null) return
        loaded = key
        val url = "$Server/api/clips/${Uri.encode(clip.rec)}.mp4?from=${clip.from}&to=${clip.to}"
        player.load(url, mapOf("Authorization" to "Bearer $token"))
    }

    fun release() = player.release()
}

/**
 * The clip's start and end in whole seconds of the lecture, rounded alike, so the range it shows and its length come
 * from the same two numbers. Cut down each on its own, 4155.5–4218.4 read "1:09:15–1:10:18 · 62 s", a second short of
 * its own range (design review 18. 11.; 10 of the 19 LA1 clips the same way).
 */
private fun ClipDef.ends(): Pair<Int, Int> = kotlin.math.round(from).toInt() to kotlin.math.round(to).toInt()

/** The clip's length in whole seconds, the difference of the times it shows. */
fun ClipDef.seconds(): Int = ends().let { (a, b) -> b - a }

/** The lecture's time [sec] into the clip, rounded as [ends] are: at 0 the clip's start, at its length its end. */
internal fun stamp(clip: ClipDef, sec: Double): String {
    val t = kotlin.math.round(clip.from + sec).toInt()
    return if (t >= 3600) "${t / 3600}:${(t / 60 % 60).toString().padStart(2, '0')}:${(t % 60).toString().padStart(2, '0')}" else "${t / 60}:${(t % 60).toString().padStart(2, '0')}"
}

fun lectureName(clip: ClipDef) = courseById(clip.course).let { c -> "${c.clipLecturer}, ${clip.lecture.replace("přednáška", "prednáška")}, ${clip.teacher ?: c.clipTeacher}" }

/** Question before, the clip with its captions, a task after, and back to the lesson. */
@Composable
fun ClipFlow(clip: ClipDef, store: Store, host: ClipHost, frame: Frame, onDone: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    var phase by keptSub() // 0 before, 1 watch, 2 task: screens of the lesson's history
    var readOnly by keptBool("readOnly", false, clip.id)
    var pick by keptPick("pick", clip.id).fit(null) { it == null || it in clip.task.options.indices }
    // a wrong pick before: the right one after it is not celebrated (Pytagoras only at the first try)
    var missed by keptBool("missed", false, clip.id)
    val report = { frame.onReport("clip:${clip.id}") }
    val token = ClipKey
    AnimatedContent(phase, label = "clip", transitionSpec = { (fadeIn(tween(240)) + slideInHorizontally(tween(280)) { it / 10 }) togetherWith fadeOut(tween(120)) }) { p ->
        when (p) {
            0 -> StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) }, footer = {
                if (token != null) {
                    Cta("Pustiť klip, ${clip.seconds()} s", { readOnly = false; phase = 1 }, icon = Res.drawable.ic_play)
                    Quiet("Radšej len prečítať titulky", { readOnly = true; phase = 1 })
                } else {
                    Cta("Prečítať, čo povedal", { readOnly = true; phase = 1 })
                }
            }) {
                Spacer(Modifier.height(4.dp))
                if (lekcia()) { ClipIntro(clip, token != null) { readOnly = false; phase = 1 }; return@StepFrame }
                if (token != null) { Label(lectureName(clip) + ", " + stamp(clip, 0.0)); Spacer(Modifier.height(6.dp)) }
                if (clip.warning) {
                    Box(Modifier.background(c.badTint, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("Typická chyba", style = MaterialTheme.typography.labelMedium, color = c.bad) }
                    Spacer(Modifier.height(8.dp))
                }
                H1(clip.before)
                Spacer(Modifier.height(16.dp))
                if (token == null) {
                    ClipStill(clip)
                    Spacer(Modifier.height(10.dp))
                    Sub("Táto verzia appky video zo servera nepúšťa. Titulky sú v appke, takže si to prečítaš aj teraz.")
                }
            }
            1 -> ClipWatch(clip, host, store, token.takeUnless { readOnly }, frame, report) { phase = 2 }
            else -> {
                val t = clip.task
                val ord = remember(clip.id) { order(clip.id, t.options.size) }
                val right = pick == t.correct
                StepFrame(frame.step, frame.total, frame.onClose, closeLabel = frame.closeLabel, trailing = { ReportButton(report) },
                    footerTint = if (pick != null) (if (right) c.goodTint else c.badTint) else null,
                    footer = {
                        AnswerBar(ord.map { t.options[it] }, ord.indexOf(t.correct), pick?.let { ord.indexOf(it) }, { oi ->
                            val k = ord[oi]; pick = k
                            if (k == t.correct) { if (missed) hx.rightAgain() else hx.right() } else { hx.wrong(); missed = true }
                            store.record("clip:${clip.id}", k == t.correct, false, Mistake("clip:${clip.id}", "Klip: ${clip.before.take(60)}", lectureName(clip), "clip:${clip.id}", currentTimeMillis()))
                        })
                        if (pick != null) {
                            Verdict(if (right) "Správne." else "Ešte raz.", if (right) t.why else listOfNotNull(pick?.let { t.wrong[it] }, t.why).joinToString(" "), right, cheer = !missed)
                            if (right) Cta("Späť do lekcie", onDone) else ButtonRow {
                                Second("Pozrieť znova", { pick = null; phase = 1 }, line = T.c.badLine)
                                Cta("Skúsiť znova", { pick = null })
                            }
                        }
                    }) {
                    Spacer(Modifier.height(4.dp))
                    Label("Po klipe")
                    Spacer(Modifier.height(4.dp))
                    H1(t.q)
                }
            }
        }
    }
}

@Composable
private fun ClipWatch(clip: ClipDef, host: ClipHost, store: Store, token: String?, frame: Frame, report: () -> Unit, onNext: () -> Unit) {
    val c = T.c
    val p = host.player
    var playing by remember { mutableStateOf(false) }
    var pos by remember { mutableDoubleStateOf(0.0) }
    var buffering by remember { mutableStateOf(token != null) }
    var error by remember { mutableStateOf<String?>(null) }
    val web = dev.hamper.tabula.platform.LocalPlatform.current.isWeb
    val dur = clip.to - clip.from
    // the furthest point reached, s into the clip: the statistics tell clips left early from ones watched through
    val reach = remember(clip.id) { doubleArrayOf(0.0) }
    if (token != null) {
        DisposableEffect(clip.id) {
            host.open = true
            host.load(clip, token)
            p.seekTo(0); p.playbackParameters = PlaybackParameters(store.clipSpeed); p.play()
            val l = object : Player.Listener {
                override fun onPlayerError(e: PlaybackException) {
                    val code = (e.cause as? HttpDataSource.InvalidResponseCodeException)?.responseCode
                    error = when (code) {
                        401, 403 -> if (web) "Server túto verziu stránky už nepustí. Načítaj stránku znova." else "Server túto verziu appky už nepustí. Stiahni si novú z fitagoras.hamper.dev."
                        429 -> "Priveľa klipov naraz. Skús to o chvíľu."
                        503 -> "Klipy sú na serveri teraz vypnuté."
                        404 -> "Táto nahrávka na serveri nie je."
                        null -> "Server nedostupný alebo slabý signál. Titulky nižšie fungujú aj bez siete."
                        else -> "Server vrátil chybu $code."
                    }
                    buffering = false
                }
                override fun onPlaybackStateChanged(state: Int) { buffering = state == Player.STATE_BUFFERING }
            }
            p.addListener(l)
            host.clip = clip
            onDispose {
                // only a clip that really played and was left, not one still being cut on the server or gone to PiP
                val furthest = maxOf(reach[0], p.currentPosition / 1000.0)
                if (error == null && !host.pip && furthest > 0 && dur > 0) dev.hamper.tabula.system.Telemetry.clipPlayed(clip, furthest / dur, lectureName(clip))
                p.removeListener(l); host.goFull(false); host.open = false; host.inline = null; host.note = null; host.leave()
            }
        }
        LaunchedEffect(clip.id) {
            while (true) { playing = host.wants; pos = p.currentPosition / 1000.0; if (pos > reach[0]) reach[0] = pos; delay(50) }
        }
    }
    // turning the phone sideways while the clip plays is the same as the fullscreen button
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(landscape) { if (landscape && token != null && !host.pip && host.fullOnTurn) host.goFull(true) }
    // leaving the step or the screen: the picture disappears with the start of the animation, not 300 ms after it
    val navScope = androidx.navigation3.ui.LocalNavAnimatedContentScope.current
    var leaving = navScope.transition.targetState != androidx.compose.animation.EnterExitState.Visible
    var m = LocalStepMotion.current
    while (m != null) { if (m.scope.transition.targetState != androidx.compose.animation.EnterExitState.Visible) leaving = true; m = m.outer }
    var gone by remember(clip.id) { mutableStateOf(false) }
    SideEffect { host.leaving = leaving || gone }
    // a tapped line lights up at once and stays lit until playback is there, even while the seek still loads
    var pin by remember(clip.id) { mutableStateOf<Int?>(null) }
    val said = clip.cues.indexOfLast { pos >= it.t0 - .1 }.coerceAtLeast(0)
    LaunchedEffect(pin) { if (pin != null) { delay(4000); pin = null } }
    if (pin != null && said == pin) SideEffect { pin = null }
    val current = pin ?: said
    val list = rememberLazyListState()
    // the line being said glides to the top of the list, just under its edge, so the fade never covers it
    LaunchedEffect(current) {
        if (token == null) return@LaunchedEffect
        val seen = list.layoutInfo.visibleItemsInfo.firstOrNull { it.index == current }
        if (seen != null) list.animateScrollBy(seen.offset.toFloat(), Motion.settle()) else list.animateScrollToItem(current)
    }
    StepFrame(frame.step, frame.total, frame.onClose, closeLabel = "Zavrieť prednášku", trailing = { ReportButton(report) }, scroll = false, footer = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ďalej:", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            Spacer(Modifier.width(6.dp))
            Text("krátka úloha", style = MaterialTheme.typography.titleSmall, color = c.ink)
        }
        Spacer(Modifier.height(4.dp))
        Cta("Pokračovať", { gone = true; onNext() })
    }) {
        // the question from before the clip is kept on this screen: it is what to listen for. With the video it stays
        // above the player; when reading, it heads the captions and scrolls with them, so large text still leaves room to read
        val head: @Composable () -> Unit = {
            Rich(clip.before, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Spacer(Modifier.height(2.dp))
            Label(lectureName(clip))
            Spacer(Modifier.height(8.dp))
        }
        if (token != null) {
            head()
            // the frame of the video; the picture itself is drawn over it by ClipLayer, so going fullscreen moves it, never remounts it
            val note = error ?: if (buffering && pos < .1) "Server strihá klip, prvýkrát to chvíľu trvá…" else null
            SideEffect { host.note = note }
            Box(
                Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(18.dp)).background(Color.Black)
                    .onGloballyPositioned {
                        val r = it.boundsInWindow(); host.bounds = Rect(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
                        val o = it.positionInRoot(); host.inline = androidx.compose.ui.geometry.Rect(o.x, o.y, o.x + it.size.width, o.y + it.size.height)
                        host.area = it.parentLayoutCoordinates?.boundsInRoot()
                    },
            )
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.line)) {
                Box(Modifier.fillMaxWidth((pos / dur).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(if (c.dark) c.ink else c.lesson.main))
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stamp(clip, pos), style = MaterialTheme.typography.labelMedium, color = c.ink, modifier = Modifier.widthIn(min = 48.dp))
                Spacer(Modifier.weight(1f))
                SmallText("−10 s", "O 10 sekúnd späť") { host.seek(p.currentPosition - 10_000) }
                RoundIcon(if (playing) Res.drawable.ic_pause else Res.drawable.ic_play, if (playing) "Pozastaviť" else "Prehrať", big = true) {
                    if (p.playbackState == Player.STATE_IDLE) error = null
                    host.toggle()
                }
                SpeedChoice(store, p, c.ink2)
                Spacer(Modifier.weight(1f))
                if (host.canPip) RoundIcon(Res.drawable.ic_pip, "Obraz v obraze", onClick = host.onPip)
                RoundIcon(Res.drawable.ic_fullscreen, "Na celú obrazovku") { host.goFull(true) }
            }
            Spacer(Modifier.height(4.dp))
        }
        // every timestamp takes the width of the longest one, in tabular digits, so the text starts at one x
        val stampStyle = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum")
        val widest = remember(clip.id) { clip.cues.maxOf { stamp(clip, it.t0).length }.let { "0".repeat(it - 3) + ":00" } }
        Box(Modifier.weight(1f).fillMaxWidth()) { LazyColumn(Modifier.fillMaxSize(), state = list, contentPadding = PaddingValues(top = if (token != null) 28.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (token == null) item {
                Column {
                    head()
                    Sub("Titulky z prednášky (automatický prepis, slová môžu byť skomolené).")
                    Spacer(Modifier.height(4.dp))
                }
            }
            itemsIndexed(clip.cues) { i, cue ->
                val on = token != null && i == current
                val past = token != null && i < current
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (on) c.lesson.tint2 else Color.Transparent)
                        .then(if (token != null) Modifier.clickable(role = Role.Button, onClickLabel = "Prejsť sem") { pin = i; pos = cue.t0; host.seek((cue.t0 * 1000).toLong()) } else Modifier)
                        .heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Box(Modifier.alignByBaseline().padding(end = 10.dp)) {
                        Text(widest, style = stampStyle, color = Color.Transparent, maxLines = 1, softWrap = false, modifier = Modifier.clearAndSetSemantics {})
                        Text(stamp(clip, cue.t0), style = stampStyle, color = c.ink2, maxLines = 1, softWrap = false, modifier = Modifier.align(Alignment.CenterEnd))
                    }
                    Text(
                        buildAnnotatedString {
                            if (!on) append(cue.text)
                            // the line being said reads in full; what is already said only takes the lesson's ink
                            else cue.words.forEachIndexed { wi, w ->
                                if (wi > 0) append(" ")
                                withStyle(SpanStyle(color = if (pos >= w.t0) c.lesson.text else c.ink)) { append(w.w) }
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge, color = if (past) c.ink2 else c.ink, modifier = Modifier.weight(1f).alignByBaseline(),
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
        // the list runs on under the footer and the header: it fades out instead of ending in a cut line
        if (list.canScrollForward) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(40.dp).background(Brush.verticalGradient(listOf(c.bg.copy(alpha = 0f), c.bg))))
        if (list.canScrollBackward) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(24.dp).background(Brush.verticalGradient(listOf(c.bg, c.bg.copy(alpha = 0f)))))
        }
        // on first view the cut can fall between two lines and look like the end: say that the captions go on,
        // under the list, not over a line of it
        val untouched = list.firstVisibleItemIndex == 0 && list.firstVisibleItemScrollOffset == 0
        if (list.canScrollForward && untouched) {
            val scope = rememberCoroutineScope()
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button) { scope.launch { list.animateScrollBy(list.layoutInfo.viewportSize.height * .7f) } }
                    .heightIn(min = 48.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Titulky pokračujú nižšie", style = MaterialTheme.typography.titleSmall, color = c.lesson.text)
                Spacer(Modifier.width(4.dp))
                Icon24(Res.drawable.ic_chevron, tint = c.lesson.text, modifier = Modifier.size(20.dp).rotate(90f))
            }
        }
    }
}

/** New look (ui/LekciaVzhlad.kt): the question large, the lecture as the frame that plays it, its time a quiet line. */
@Composable
private fun ClipIntro(clip: ClipDef, playable: Boolean, onPlay: () -> Unit) {
    val c = T.c
    if (clip.warning) {
        Box(Modifier.background(c.badTint, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("Typická chyba", style = MaterialTheme.typography.labelMedium, color = c.bad) }
        Spacer(Modifier.height(10.dp))
    }
    ClipQuestion(clip.before)
    Spacer(Modifier.height(20.dp))
    if (playable) ClipFrame(lectureName(clip), "${stamp(clip, 0.0)}–${stamp(clip, clip.to - clip.from)}", onPlay)
    else {
        ClipStill(clip)
        Spacer(Modifier.height(10.dp))
        Sub("Táto verzia appky video zo servera nepúšťa. Titulky sú v appke, takže si to prečítaš aj teraz.")
    }
}

/** Where the video would play in a build without the clip key: the lecture, the moment and the length, nothing that pretends to play. */
@Composable
private fun ClipStill(clip: ClipDef) {
    val c = T.c
    val ink = Color(0xFFECE8DF)
    // 16:9 like the player it stands for; with large text it grows instead of stacking text on the lock
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Column(
            // in the dark theme the page itself is this grey: the frame goes a step darker and gets an edge, so it still reads as a screen
            Modifier.fillMaxWidth().heightIn(min = maxWidth * 9f / 16f).clip(RoundedCornerShape(18.dp)).background(if (c.dark) c.edge else Color(0xFF26282C))
                .then(if (c.dark) Modifier.border(1.5.dp, c.line, RoundedCornerShape(18.dp)) else Modifier).padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(lectureName(clip), style = MaterialTheme.typography.labelMedium, color = ink.copy(alpha = .8f))
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(52.dp).clip(CircleShape).background(ink.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_lock, tint = ink) }
                Spacer(Modifier.height(8.dp))
                Text("Video tu nejde", style = MaterialTheme.typography.titleSmall, color = ink)
            }
            Row(Modifier.fillMaxWidth()) {
                Text("${stamp(clip, 0.0)}–${stamp(clip, clip.to - clip.from)}", style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"), color = ink.copy(alpha = .8f), modifier = Modifier.weight(1f))
                Text("${clip.seconds()} s", style = MaterialTheme.typography.labelMedium, color = ink.copy(alpha = .8f))
            }
        }
    }
}

@Composable
private fun RoundIcon(id: DrawableResource, say: String, big: Boolean = false, onClick: () -> Unit) {
    val c = T.c
    Box(
        Modifier.size(if (big) 56.dp else 48.dp).clip(CircleShape).then(if (big) Modifier.background(c.lesson.tint) else Modifier)
            .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = say },
        contentAlignment = Alignment.Center,
    ) { Icon24(id, tint = if (big) c.lesson.text else c.ink2) }
}

@Composable
private fun SmallText(t: String, say: String, onClick: () -> Unit) {
    val c = T.c
    Box(
        Modifier.heightIn(min = 48.dp).widthIn(min = 56.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = "$say, $t" }.padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) { Text(t, style = MaterialTheme.typography.titleSmall, color = c.ink2, maxLines = 1, softWrap = false) }
}

/** 1,25× as people write it. */
internal fun speedLabel(v: Float) = (if (v == v.toInt().toFloat()) "${v.toInt()}" else "$v".replace('.', ',')) + "×"

/** Speed of the clip: a tap opens the six speeds with the current one ticked; the pick is kept and the voice keeps its pitch. */
@Composable
private fun SpeedChoice(store: Store, p: Player, ink: Color, onOpen: (Boolean) -> Unit = {}) {
    val c = T.c
    val hx = rememberHaptics()
    var open by remember { mutableStateOf(false) }
    // the fullscreen controls hold still while the menu is open
    LaunchedEffect(open) { onOpen(open) }
    Box {
        Box(
            Modifier.heightIn(min = 48.dp).widthIn(min = 56.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button) { open = true }
                .semantics { contentDescription = "Rýchlosť, zmeniť, ${speedLabel(store.clipSpeed)}" }.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) { Text(speedLabel(store.clipSpeed), style = MaterialTheme.typography.titleSmall, color = ink, maxLines = 1, softWrap = false) }
        DropdownMenu(open, { open = false }, containerColor = c.card, shape = RoundedCornerShape(16.dp)) {
            for (v in ClipSpeeds) {
                val on = v == store.clipSpeed
                DropdownMenuItem(
                    text = { Text(speedLabel(v), style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), color = if (on) c.lesson.text else c.ink) },
                    onClick = { hx.tap(); store.clipSpeed = v; store.save(); p.playbackParameters = PlaybackParameters(v); open = false },
                    trailingIcon = { if (on) Icon24(Res.drawable.ic_check, tint = c.lesson.text, modifier = Modifier.size(20.dp)) else Spacer(Modifier.size(20.dp)) },
                    modifier = Modifier.semantics { selected = on },
                )
            }
        }
    }
}

/**
 * The picture of the playing clip, one for the whole app, drawn above everything. In the lesson it sits on the
 * video frame ([ClipHost.inline]); fullscreen it grows from there to the whole screen on black, with captions over
 * the bottom and controls that hide after 3 s. The same view all the way, so nothing goes black or buffers again.
 */
@Composable
fun ClipLayer(host: ClipHost, store: Store) {
    val inl = host.inline
    if (!host.open || inl == null || (host.leaving && !host.pip)) return
    val p = host.player
    val still = calm()
    val d = LocalDensity.current
    val f = remember { Animatable(if (host.full) 1f else 0f) }
    LaunchedEffect(host.full) { f.animateTo(if (host.full) 1f else 0f, if (still) snap() else Motion.settle()) }
    BackHandler(host.full) { host.goFull(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
        val fw = minOf(w, h * 16f / 9f); val fh = fw * 9f / 16f
        val whole = androidx.compose.ui.geometry.Rect((w - fw) / 2, (h - fh) / 2, (w + fw) / 2, (h + fh) / 2)
        // picture-in-picture: the window is the picture
        val k = if (host.pip) 1f else f.value
        val all = androidx.compose.ui.geometry.Rect(0f, 0f, w, h)
        val a = host.area ?: all
        val cr = androidx.compose.ui.geometry.Rect(a.left * (1 - k), a.top * (1 - k), a.right + (w - a.right) * k, a.bottom + (h - a.bottom) * k)
        val r = androidx.compose.ui.geometry.Rect(
            inl.left + (whole.left - inl.left) * k, inl.top + (whole.top - inl.top) * k,
            inl.right + (whole.right - inl.right) * k, inl.bottom + (whole.bottom - inl.bottom) * k,
        )
        if (k > 0f) Box(Modifier.fillMaxSize().graphicsLayer { alpha = k.coerceIn(0f, 1f) }.background(Color.Black))
        // inline, the picture is cut to the step's body, so a tall question never pushes it over the panel below
        Box(Modifier.offset { IntOffset(cr.left.roundToInt(), cr.top.roundToInt()) }.size(with(d) { cr.width.toDp() }, with(d) { cr.height.toDp() }).clipToBounds()) {
        Box(
            Modifier.offset { IntOffset((r.left - cr.left).roundToInt(), (r.top - cr.top).roundToInt()) }.size(with(d) { r.width.toDp() }, with(d) { r.height.toDp() })
                .clip(RoundedCornerShape(18.dp * (1f - k).coerceIn(0f, 1f))).background(Color.Black)
                .pointerInput(Unit) { detectTapGestures(onDoubleTap = { if (!host.full) host.goFull(true) }) }
                .semantics { contentDescription = "Video, dvojité ťuknutie na celú obrazovku" },
            contentAlignment = Alignment.Center,
        ) {
            PlayerSurface(player = p, modifier = Modifier.fillMaxSize(), surfaceType = SURFACE_TYPE_TEXTURE_VIEW)
            val note = host.note
            if (note != null) Text(note, style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(16.dp))
            // loading after a seek: a small spinner on the frame, so a still picture never reads as paused
            // on a dark disc, so it reads on a pale slide too
            else if (host.loading) Box(Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = .55f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(32.dp), color = Color.White, strokeWidth = 3.dp, trackColor = Color.White.copy(alpha = .25f))
            }
        }
        }
        if (host.full && !host.pip) FullControls(host, store, Modifier.graphicsLayer { alpha = k.coerceIn(0f, 1f) })
    }
}

@Composable
private fun FullControls(host: ClipHost, store: Store, modifier: Modifier) {
    val p = host.player
    val clip = host.clip ?: return
    val hx = rememberHaptics()
    val dur = clip.to - clip.from
    var pos by remember { mutableDoubleStateOf(p.currentPosition / 1000.0) }
    val playing = host.wants
    var drag by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(Unit) { while (true) { pos = p.currentPosition / 1000.0; delay(100) } }
    var shown by remember { mutableStateOf(true) }
    var poke by remember { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    // with TalkBack on the controls stay: a hidden control cannot be found by touch exploration
    val talk = dev.hamper.tabula.compat.rememberTouchExploration()
    LaunchedEffect(shown, poke, playing, drag, menu) { if (shown && playing && drag == null && !menu && !talk) { delay(3000); shown = false } }
    val a by animateFloatAsState(if (shown) 1f else 0f, tween(200), label = "controls")
    val act: (() -> Unit) -> Unit = { block -> hx.tap(); poke++; block() }
    Box(modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { shown = !shown; poke++ } }.semantics { onClick("Zobraziť ovládanie") { shown = true; poke++; true } }) {
        if (host.captions) {
            val cue = clip.cues.lastOrNull { pos >= it.t0 - .1 && pos <= it.t1 + 1.5 }
            val lift by animateDpAsState(if (shown) 112.dp else 28.dp, Motion.settle(), label = "captionLift")
            if (cue != null) Text(
                cue.text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = Color.White, textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = lift).widthIn(max = 720.dp).padding(horizontal = 48.dp)
                    .background(Color.Black.copy(alpha = .62f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
        if (a > 0f) Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().graphicsLayer { alpha = a }
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .75f))))
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Slider(
                value = drag ?: (pos / dur).toFloat().coerceIn(0f, 1f),
                onValueChange = { drag = it; poke++ },
                onValueChangeFinished = { drag?.let { v -> pos = v * dur; host.seek((v * dur * 1000).toLong()) }; drag = null },
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = .3f)),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Posun v klipe" },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FullIcon(if (playing) Res.drawable.ic_pause else Res.drawable.ic_play, if (playing) "Pozastaviť" else "Prehrať") {
                    act { host.toggle() }
                }
                Box(
                    Modifier.heightIn(min = 48.dp).widthIn(min = 56.dp).clip(RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { act { host.seek(p.currentPosition - 10_000) } }
                        .semantics { contentDescription = "O 10 sekúnd späť" }, contentAlignment = Alignment.Center,
                ) { Text("−10 s", style = MaterialTheme.typography.titleSmall, color = Color.White) }
                Spacer(Modifier.width(8.dp))
                Text("${stamp(clip, pos)} / ${stamp(clip, dur)}", style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"), color = Color.White.copy(alpha = .85f))
                Spacer(Modifier.weight(1f))
                SpeedChoice(store, p, Color.White) { menu = it; poke++ }
                FullIcon(Res.drawable.ic_cc, if (host.captions) "Skryť titulky" else "Zobraziť titulky", dim = !host.captions) { act { host.captions = !host.captions } }
                FullIcon(Res.drawable.ic_fullscreen_exit, "Zavrieť celú obrazovku") { act { host.goFull(false) } }
            }
        }
    }
}

@Composable
private fun FullIcon(id: DrawableResource, say: String, dim: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = say },
        contentAlignment = Alignment.Center,
    ) { Icon24(id, tint = Color.White.copy(alpha = if (dim) .45f else 1f)) }
}

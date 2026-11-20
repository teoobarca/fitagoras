package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.RefTarget
import dev.hamper.tabula.la.GemWalk
import dev.hamper.tabula.la.TableWalk
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_camera
import dev.hamper.tabula.res.ic_ask
import dev.hamper.tabula.res.ic_close
import dev.hamper.tabula.res.ic_send
import dev.hamper.tabula.tutor.*
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.hamper.tabula.res.ic_check
import dev.hamper.tabula.res.ic_refresh
import dev.hamper.tabula.res.ic_x
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import org.jetbrains.compose.resources.decodeToImageBitmap

/*
 * The tutor's one entry and its panel (docs/TUTOR.md, "Všeobecný tútor"): wherever the student is, "Opýtať sa" opens
 * a conversation about that place; "Odfoť riešenie" adds photos of the paper. The answer is drawn with the app's own
 * components: the engine's verdict on a photographed GEM with the student's stepper, the paper's checklist judged item
 * by item, theorem cards and steppers from the tools.
 */

/** The place on screen, for the tutor's entry in the step's header; screens set it (a lesson step, an exercise…). */
val LocalTutorMiesto = staticCompositionLocalOf<(() -> MiestoAppky)?> { null }

/** The entry in a header: a small pill next to "Nahlásiť chybu". */
@Composable
fun TutorVstup() {
    val m = LocalTutorMiesto.current ?: return
    LocalTutor.current ?: return
    val c = T.c
    var otvor by remember { mutableStateOf(false) }
    when (TutorVstupNow) {
        // the header's own stroke beside the flag (Teo, 18. 11.: the "Opýtať sa" pill pressed into the header and fought
        // the bar); found on every step, the same 48 dp target as the flag
        TutorVstupTvar.Ikona -> Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = "Opýtať sa Pytagora") { otvor = true }.semantics { contentDescription = "Opýtať sa Pytagora" }, contentAlignment = Alignment.Center) {
            Icon24(Res.drawable.ic_ask, tint = c.ink2)
        }
        TutorVstupTvar.Pilulka -> Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(18.dp)).clickable(role = Role.Button) { otvor = true }.semantics { contentDescription = "Opýtať sa Pytagora" }, contentAlignment = Alignment.Center) {
            Text("Opýtať sa", Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, c.edge, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium, color = c.lesson.text)
        }
        TutorVstupTvar.Riadok -> {}
    }
    if (otvor) TutorPanelDialog(m, onZavri = { otvor = false })
}

/** Where the tutor is entered from on a step (the comparison of 18. 11.; the app uses [TutorVstupTvar.Ikona]). */
enum class TutorVstupTvar { Ikona, Pilulka, Riadok }
var TutorVstupNow = TutorVstupTvar.Ikona

/** The variant [TutorVstupTvar.Riadok]: a quiet line above the step's buttons (screenshots only). */
@Composable
fun TutorRiadok() {
    if (TutorVstupNow != TutorVstupTvar.Riadok) return
    val m = LocalTutorMiesto.current ?: return
    LocalTutor.current ?: return
    var otvor by remember { mutableStateOf(false) }
    Quiet("Opýtať sa Pytagora", { otvor = true }, color = T.c.lesson.text, icon = Res.drawable.ic_ask)
    if (otvor) TutorPanelDialog(m, onZavri = { otvor = false })
}

/** "Odfoť riešenie" where the paper is the point (a checklist, a wrong answer): the panel opens on the camera. */
@Composable
fun OdfotRiesenie(miesto: () -> MiestoAppky, onPosudok: (Posudok, PrepisTextu?) -> Unit = { _, _ -> }, modifier: Modifier = Modifier, label: String = "Odfoť riešenie, Pytagoras ho skontroluje", quiet: Boolean = false) {
    LocalTutor.current ?: return
    var otvor by remember { mutableStateOf(false) }
    if (quiet) Quiet(label, { otvor = true }, modifier, icon = Res.drawable.ic_camera) else Second(label, { otvor = true }, modifier, icon = Res.drawable.ic_camera)
    if (otvor) TutorPanelDialog(miesto, fotak = true, onPosudok = onPosudok, onZavri = { otvor = false })
}

/**
 * The tutor is an addition: when its code cannot start (1.5.1 on Android: a pattern ICU rejects failed the class's
 * initialisation and took the exercise down with it), the step stays and the tutor says it cannot help now.
 */
fun <T> tutorSafe(f: () -> T): T? = try { f() } catch (e: Throwable) { runCatching { dev.hamper.tabula.system.Telemetry.help("tutor-failed", e::class.simpleName) }; null }

/**
 * The tutor's screens cover the whole phone, the system bars too (their pages pad for them): a dialog that fits the
 * bars left them grey under its dim over a beige page (emulator, 1.6.2).
 */
private val TutorOkno get() = dev.hamper.tabula.compat.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

@Composable
fun TutorPanelDialog(miesto: () -> MiestoAppky, fotak: Boolean = false, onPosudok: (Posudok, PrepisTextu?) -> Unit = { _, _ -> }, stav: TutorStav? = null, zaostrit: Boolean = false, onZavri: () -> Unit) {
    androidx.compose.ui.window.Dialog(onZavri, TutorOkno) {
        dev.hamper.tabula.compat.DialogBars(light = !T.c.dark)
        // the tutor never takes the lesson down: a place it can't describe is said in its own frame (and counted)
        val m = remember { tutorSafe(miesto) }
        // a screen of its own, in the app's column (ui/AppColumn.kt)
        AppColumn {
            m?.let { if (stav != null) TutorPanel(it, fotak, onPosudok, onZavri, stav, zaostrit) else TutorPanel(it, fotak, onPosudok, onZavri, zaostrit = zaostrit) } ?: TutorRam("Pytagoras", onZavri) {
                Spacer(Modifier.height(24.dp))
                Text("Pytagoras je teraz nedostupný. Príklad ide ďalej bez neho.", style = MaterialTheme.typography.bodyLarge, color = T.c.ink)
            }
        }
    }
}


/**
 * The paper's check in the step itself (Teo, 18. 11.: "celé to je v tom chat window a nevidím to zadanie"): the camera
 * opens alone, and the judgement streams in place, under the task and the sample, above the checklist it ticks. The
 * conversation opens only when asked ("Opýtať sa ďalej"), with the same history. The checklist by hand stays the
 * way without the tutor.
 */
@Composable
fun PapierKontrola(miesto: () -> MiestoAppky, otvorFotak: Boolean = false, onPosudok: (Posudok, PrepisTextu?) -> Unit) {
    val sluzba = LocalTutor.current ?: return
    val c = T.c
    val scope = rememberCoroutineScope()
    val stav = remember { TutorStav() }.also { it.scope = scope }
    // [otvorFotak]: the camera at once (screenshots of the step with its judgement)
    var fotit by remember { mutableStateOf(otvorFotak && tutorSafe(miesto) != null) }
    var panel by remember { mutableStateOf(false) }
    // the place is described only when the tutor is used (a failure there stays the tutor's, never the step's)
    var m by remember { mutableStateOf<MiestoAppky?>(if (otvorFotak) tutorSafe(miesto) else null) }
    var nejde by remember { mutableStateOf(false) }
    fun miestoTeraz(): MiestoAppky? = m ?: tutorSafe(miesto).also { m = it; nejde = it == null }
    val t = stav.tahy.lastOrNull { it.fotky.isNotEmpty() }

    if (t != null) {
        key(t) { PosudokKarta(t, onZnova = { miestoTeraz()?.let { stav.znova(sluzba, scope, it, t, onPosudok) } }, sluzba = sluzba) }
        Spacer(Modifier.height(10.dp))
    }
    if (nejde) { Text("Pytagoras je teraz nedostupný. Odškrtni si to sám podľa zoznamu.", style = MaterialTheme.typography.bodyMedium, color = c.ink2); Spacer(Modifier.height(10.dp)) }
    when {
        t == null -> Second("Odfoť riešenie, Pytagoras ho skontroluje", { if (miestoTeraz() != null) fotit = true }, icon = Res.drawable.ic_camera)
        t.hotovo -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Quiet("Opýtať sa ďalej", { if (miestoTeraz() != null) panel = true }, Modifier.weight(1f), color = c.lesson.text, icon = Res.drawable.ic_ask)
            Quiet("Odfotiť znova", { if (miestoTeraz() != null) fotit = true }, Modifier.weight(1f), icon = Res.drawable.ic_camera)
        }
        else -> {}
    }
    if (fotit) androidx.compose.ui.window.Dialog({ fotit = false }, TutorOkno) {
        val p by sluzba.pripojenie.collectAsState()
        // the sign-in page is the app's own colour, the camera is dark
        dev.hamper.tabula.compat.DialogBars(light = !T.c.dark && (p as? Pripojenie.Pripojeny)?.planPovoleny != true)
        // the first use signs in on its one screen, then the camera opens by itself
        AppColumn {
            if ((p as? Pripojenie.Pripojeny)?.planPovoleny != true) PrvePripojenie(sluzba, p) { fotit = false }
            else sluzba.Fotak(maxStran = 6, onHotovo = { f -> fotit = false; m?.let { stav.opytaj(sluzba, scope, it, null, f, onPosudok) } }, onZavri = { fotit = false })
        }
    }
    // the follow-up opens on the field, the judgement above it (the model has it in its history)
    if (panel) TutorPanelDialog({ m!! }, onPosudok = onPosudok, stav = stav, zaostrit = true, onZavri = { panel = false })
}

/** The verdict's colours and word: right is green, wrong terracotta (as everywhere in the app), a reservation neutral. */
@Composable
private fun verdiktVzhlad(v: Posudok.Verdikt?): Triple<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color, String?> {
    val c = T.c
    return when (v) {
        Posudok.Verdikt.UZNAL_BY -> Triple(c.goodTint, c.good, "Uznal by som")
        Posudok.Verdikt.S_VYHRADOU -> Triple(c.sand, c.ink, "Uznal by som s výhradou")
        Posudok.Verdikt.NEUZNAL_BY -> Triple(c.badTint, c.badInk, "Neuznal by som")
        null -> Triple(c.card, c.ink2, null)
    }
}

/**
 * The teacher's verdict on a paper, read at a glance: the verdict word in its colour (it comes first in the stream, so
 * it lands at once, in the place the waiting state held), the reason (the first sentence), then what to fix under a
 * rule. Nothing moves when a part lands: the verdict takes the waiting line's place, the words only grow at the end.
 */
@Composable
private fun PosudokKarta(t: Tah, onZnova: () -> Unit, sluzba: TutorSluzba, nahlady: Boolean = true) {
    val c = T.c
    val v = t.posudok?.first?.verdikt ?: t.verdikt
    val (tint, ink, slovo) = verdiktVzhlad(v)
    val fill by androidx.compose.animation.animateColorAsState(tint, androidx.compose.animation.core.tween(420))
    val ram by androidx.compose.animation.animateColorAsState(if (v == Posudok.Verdikt.NEUZNAL_BY) c.badLine else c.line, androidx.compose.animation.core.tween(420))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(fill).border(1.dp, ram, RoundedCornerShape(24.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pytagoras k tvojmu papieru", style = MaterialTheme.typography.labelMedium, color = c.ink2, modifier = Modifier.weight(1f))
            if (nahlady) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { t.fotky.take(3).forEachIndexed { i, f -> Nahlad(f, i, 40) } }
        }
        Spacer(Modifier.height(8.dp))
        val chyba = t.chyba
        val zivo = remember(t) { !t.hotovo }
        val zobraz = plynulo(t.text, zivo, t.hotovo, hned = t.zastavene || chyba != null)
        // the headline: what the tutor does now, then the verdict in the same place
        val hlavicka: Any = when { slovo != null -> v!!; !t.hotovo -> "cakam"; else -> "nic" }
        androidx.compose.animation.AnimatedContent(hlavicka, transitionSpec = { fadeIn(androidx.compose.animation.core.tween(320, 80)) togetherWith fadeOut(androidx.compose.animation.core.tween(160)) }) { h ->
            when (h) {
                is Posudok.Verdikt -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon24(if (h == Posudok.Verdikt.NEUZNAL_BY) Res.drawable.ic_x else Res.drawable.ic_check, tint = ink, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(slovo ?: "", style = MaterialTheme.typography.titleMedium, color = ink)
                }
                "cakam" -> Cakanie(t.stav ?: if (t.text.isEmpty()) "Čítam papier" else "Posudzujem ako cvičiaci")
                else -> Spacer(Modifier.height(0.dp))
            }
        }
        if (zobraz.isNotEmpty()) {
            val (dovod, oprav) = remember(zobraz) { prvaVeta(vysaditelne(zobraz)) }
            Spacer(Modifier.height(8.dp))
            CompositionLocalProvider(dev.hamper.tabula.ui.LocalNabehSlov provides zivo) {
                Rich(dovod, style = MaterialTheme.typography.bodyLarge, color = c.ink)
                if (oprav.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    // what to fix, under a rule in the verdict's colour; with a full verdict it is only a remark
                    // the rule is drawn beside the text (an intrinsic height would lay the typeset text out twice)
                    val linka = if (v == Posudok.Verdikt.UZNAL_BY) c.line else ink.copy(alpha = .55f)
                    Box(Modifier.fillMaxWidth().drawBehind {
                        drawRoundRect(linka, size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                    }.padding(start = 15.dp)) {
                        Column {
                            if (v != null && v != Posudok.Verdikt.UZNAL_BY) { Text("Čo opraviť", style = MaterialTheme.typography.labelMedium, color = ink); Spacer(Modifier.height(2.dp)) }
                            oprav.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }.forEachIndexed { i, o -> key(i) { if (i > 0) Spacer(Modifier.height(6.dp)); Rich(o.trim(), style = MaterialTheme.typography.bodyLarge, color = c.ink) } }
                        }
                    }
                }
            }
        }
        if (chyba != null) {
            if (zobraz.isNotEmpty()) Spacer(Modifier.height(8.dp))
            ChybaRiadok(chyba, onZnova, sluzba)
        } else if (t.zastavene) {
            Text("Zastavené.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        } else if (t.hotovo) {
            if (t.posudok == null && t.text.isBlank()) Text("Na fotke som nenašiel napísaný text. Skús ju odfotiť ostrejšie a celú.", style = MaterialTheme.typography.bodyLarge, color = c.ink)
            CoSomPrecital(t.posudok?.second)
        }
    }
}

/**
 * The judgement's first sentence (the verdict and why, as the judge is asked to write it) and the rest (what to fix).
 * A sentence ends at ".", "!" or "?" outside a formula, followed by a space; the first one whole is the reason.
 */
internal fun prvaVeta(t: String): Pair<String, String> {
    var mat = false
    for (i in t.indices) {
        val ch = t[i]
        if (ch == '$') mat = !mat
        if (!mat && (ch == '.' || ch == '!' || ch == '?') && i + 1 < t.length && t[i + 1].isWhitespace() && i > 12) return t.substring(0, i + 1) to t.substring(i + 1).trim()
    }
    return t to ""
}

/** What went wrong, in words, with no internal code (the panel and the step say the same). */
internal fun chybaSlovami(e: TutorChyba): String = when (e.druh) {
    TutorChyba.Druh.LIMIT -> "Tvoj ChatGPT plán teraz ďalšie otázky nepustí (dosiahol si limit). Skús to neskôr."
    TutorChyba.Druh.OFFLINE -> "Nie si pripojený na internet. Keď budeš, skús to znova."
    TutorChyba.Druh.NEDOSTUPNE -> "ChatGPT je práve preťažený a neodpovedal. Skús to o chvíľu znova."
    TutorChyba.Druh.PRERUSENE -> "Spojenie sa prerušilo uprostred odpovede. Skús to znova."
    TutorChyba.Druh.ODPOJENE -> "Prihlásenie do ChatGPT vypršalo. Prihlás sa znova a skús to ešte raz."
    TutorChyba.Druh.NEPOVOLENE -> "Tvoj ChatGPT účet nedovolil Pytagorovi použiť plán. Pozri Nastavenia."
    else -> "Tu sa mi niečo pokazilo a odpoveď nedokončím. Skús to znova."
}

/** An error in words and the one way on: try again, sign in again, or the plan's usage. */
@Composable
private fun ChybaRiadok(e: TutorChyba, onZnova: () -> Unit, sluzba: TutorSluzba) {
    val c = T.c
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.card).border(1.dp, c.line, RoundedCornerShape(16.dp)).padding(start = 14.dp, end = 14.dp, top = 12.dp)) {
        Text(chybaSlovami(e), style = MaterialTheme.typography.bodyMedium, color = c.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when (e.druh) {
                TutorChyba.Druh.ODPOJENE -> Quiet("Prihlásiť sa znova", { sluzba.pripoj() }, Modifier.weight(1f), color = c.lesson.text)
                TutorChyba.Druh.NEPOVOLENE -> {}
                TutorChyba.Druh.LIMIT -> {
                    Quiet("Skúsiť znova", onZnova, Modifier.weight(1f), color = c.ink, icon = Res.drawable.ic_refresh)
                    Quiet("Využitie plánu", { sluzba.spravovatVyuzitie() }, Modifier.weight(1f), color = c.lesson.text)
                }
                else -> Quiet("Skúsiť znova", onZnova, Modifier.weight(1f), color = c.ink, icon = Res.drawable.ic_refresh)
            }
        }
        if (e.druh == TutorChyba.Druh.NEPOVOLENE) Spacer(Modifier.height(12.dp))
    }
}

/** One turn of the conversation as the screen keeps it. */
class Tah internal constructor(val otazka: String?, val fotky: List<ByteArray>) {
    var text by mutableStateOf("")
    var stav by mutableStateOf<String?>(null)
    var hotovo by mutableStateOf(false)
    var chyba by mutableStateOf<TutorChyba?>(null)
    var postup by mutableStateOf<Pair<Vysledok, Kontext>?>(null)
    var posudok by mutableStateOf<Pair<Posudok, PrepisTextu?>?>(null)
    /** The judgement's verdict as soon as the stream names it (the parsed [posudok] replaces it at the end). */
    var verdikt by mutableStateOf<Posudok.Verdikt?>(null)
    /** The student stopped it: what was written stays, readable, with no error. */
    var zastavene by mutableStateOf(false)
    internal var job: kotlinx.coroutines.Job? = null
}

/**
 * One conversation about one place, kept by whoever shows it: the panel alone, or a step that shows the paper's
 * judgement in place and opens the panel only for a follow-up (the same history, the model knows what it said).
 * Questions run one after another: a second one sent while the first is written waits for it (the history is one).
 */
class TutorStav {
    /** The owner's scope when the state outlives the panel (a step): closing the panel must not cancel its answer. */
    internal var scope: kotlinx.coroutines.CoroutineScope? = null
    internal var rozhovor: Rozhovor? = null
    internal val tahy = mutableStateListOf<Tah>()
    private val rad = kotlinx.coroutines.sync.Mutex()
    val bezi: Boolean get() = tahy.lastOrNull()?.let { !it.hotovo } == true

    fun opytaj(sluzba: TutorSluzba, scope: kotlinx.coroutines.CoroutineScope, m: MiestoAppky, q: String?, fotky: List<ByteArray>, onPosudok: (Posudok, PrepisTextu?) -> Unit) {
        val t = Tah(q?.takeIf { it.isNotBlank() }, fotky)
        // the first sign, in the same frame as the tap: what the tutor starts with (or that it waits for the answer before)
        t.stav = if (bezi) "Odpoviem hneď po predošlej otázke" else if (fotky.isNotEmpty()) "Čítam papier" else "Premýšľam"
        tahy += t
        t.job = scope.launch {
            var zacal = false
            try {
                rad.lock()
                zacal = true
                try {
                    t.stav = if (fotky.isNotEmpty()) "Čítam papier" else "Premýšľam"
                    val k = sluzba.klient() ?: throw TutorChyba(TutorChyba.Druh.ODPOJENE, "not_signed_in", "Nie si pripojený k ChatGPT")
                    val r = rozhovor ?: Rozhovor(m, k, Nastavenie.z(sluzba.modely()) ?: throw TutorChyba(TutorChyba.Druh.NEPODPOROVANE, "no_model", "Účet neponúka žiadny model, ktorý Pytagoras pozná")).also { rozhovor = it }
                    sluzba.drz("Pytagoras odpovedá…")
                    val pred = r.art.postup; val predP = r.art.posudok
                    try {
                        val o = r.opytaj(t.otazka, fotky, onStav = { s -> t.stav = s.ifEmpty { null } }, onReset = { t.text = ""; t.verdikt = null }, onVerdikt = { v -> t.verdikt = v }) { d -> t.stav = null; t.text += d }
                        t.text = o.text
                    } finally {
                        sluzba.drz(null)
                        // what the tools found stays on screen even when the words after them fail (a limit, a dropped stream)
                        r.art.postup?.takeIf { it !== pred }?.let { t.postup = it to r.art.postupKontext!! }
                        r.art.posudok?.takeIf { it !== predP }?.let { ps -> t.posudok = ps to r.art.prepisPapiera; onPosudok(ps, r.art.prepisPapiera) }
                    }
                } finally { rad.unlock() }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // stopped by the student: what was written stays. Cut off otherwise (the screen went away): it never
                // looks finished, if the state outlives the screen it says so and offers again
                if (!t.zastavene) t.chyba = TutorChyba(TutorChyba.Druh.PRERUSENE, "cancelled", "Prerušené")
                // what the student saw stays in the history: a follow-up refers to the stopped answer
                else if (zacal) rozhovor?.zastavene(t.otazka, t.text)
                throw e
            } catch (e: TutorChyba) { t.chyba = e } catch (e: Throwable) {
                // the cause stays in the log for a bug report, the student gets words and a way to try again
                t.chyba = TutorChyba(TutorChyba.Druh.INE, "internal", e::class.simpleName ?: "chyba")
            } finally {
                t.stav = null; t.hotovo = true
                // a verdict named early but never confirmed (the judgement failed or was cut off) is not shown as one
                if (t.posudok == null) t.verdikt = null
            }
        }
    }

    /** "Stop": the answer being written ends where it is; a question waiting behind it still runs. */
    fun zastav() {
        val t = tahy.firstOrNull { !it.hotovo } ?: return
        t.zastavene = true
        t.job?.cancel()
    }

    fun znova(sluzba: TutorSluzba, scope: kotlinx.coroutines.CoroutineScope, m: MiestoAppky, t: Tah, onPosudok: (Posudok, PrepisTextu?) -> Unit) {
        if (bezi) return
        tahy.remove(t); opytaj(sluzba, scope, m, t.otazka, t.fotky, onPosudok)
    }
}

/** 2–3 quiet suggestions for where the student is; null question: photograph the paper. */
internal fun navrhy(m: MiestoAppky, poOdpovedi: Boolean): List<Pair<String, String?>> {
    val papier = m.papier != null || m.gem != null
    val r = m.route
    val otazky = when {
        poOdpovedi -> listOf("Vysvetli to inak", "Ukáž ďalší krok")
        r.startsWith("concept:") -> listOf("Vysvetli mi to inak", "Ukáž príklad")
        r.startsWith("theorem:") || r.startsWith("proof:") -> listOf("Prečo to platí?", "Ukáž ďalší krok")
        else -> listOf("Prečo tento krok?", "Ukáž ďalší krok")
    }.map { it to it as String? }
    return otazky + if (papier && !poOdpovedi) listOf("Skontroluj mi to" to null) else emptyList()
}

@Composable
fun TutorPanel(m: MiestoAppky, fotakNaZaciatku: Boolean, onPosudok: (Posudok, PrepisTextu?) -> Unit, onZavri: () -> Unit, stav: TutorStav = remember { TutorStav() }, zaostrit: Boolean = false) {
    val sluzba = LocalTutor.current ?: return
    val p by sluzba.pripojenie.collectAsState()
    if ((p as? Pripojenie.Pripojeny)?.planPovoleny != true) { PrvePripojenie(sluzba, p, onZavri); return }
    val vlastny = rememberCoroutineScope()
    val scope = stav.scope ?: vlastny
    val tahy = stav.tahy
    // the camera: 1 the photos wait under the field (they can be removed, more added), 2 they go at once ("Skontroluj mi to")
    var fotak by remember { mutableStateOf(if (fotakNaZaciatku) 2 else 0) }
    var otazka by remember { mutableStateOf("") }
    val cakajuce = remember { mutableStateListOf<ByteArray>() }
    val bezi = stav.bezi
    val scroll = rememberScrollState()
    // the end of the answer stays in view while it is written, until the student scrolls up himself; back at the
    // bottom (or a new question) it follows again
    var sledovat by remember { mutableStateOf(true) }
    val nested = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput && available.y > 0f) sledovat = false
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    LaunchedEffect(scroll) {
        snapshotFlow { scroll.value >= scroll.maxValue - 2 }.collect { if (it && !sledovat && scroll.isScrollInProgress) sledovat = true }
    }
    LaunchedEffect(scroll) {
        snapshotFlow { scroll.maxValue }.collect {
            // a drag interrupts the follow (its exception must not end this effect, or it never follows again)
            if (sledovat && it != Int.MAX_VALUE && scroll.value < it) try { scroll.animateScrollTo(it, androidx.compose.animation.core.tween(220)) } catch (e: kotlinx.coroutines.CancellationException) { if (!isActive) throw e }
        }
    }
    val pole = remember { androidx.compose.ui.focus.FocusRequester() }
    if (zaostrit) LaunchedEffect(Unit) { runCatching { pole.requestFocus() } }

    fun opytaj(q: String?, fotky: List<ByteArray>) { sledovat = true; stav.opytaj(sluzba, scope, m, q, fotky, onPosudok) }
    fun posli() {
        if (otazka.isBlank() && cakajuce.isEmpty()) return
        opytaj(otazka.takeIf { it.isNotBlank() }, cakajuce.toList()); otazka = ""; cakajuce.clear()
    }

    if (fotak != 0) {
        val rezim = fotak
        sluzba.Fotak(maxStran = 6 - (if (rezim == 1) cakajuce.size else 0), onHotovo = { f ->
            fotak = 0
            if (rezim == 2) opytaj(otazka.takeIf { it.isNotBlank() }, f).also { otazka = "" } else cakajuce += f
        }, onZavri = { fotak = 0; if (tahy.isEmpty() && fotakNaZaciatku) onZavri() }); return
    }

    val c = T.c
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 6.dp, end = 12.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onZavri).semantics { contentDescription = "Zavrieť" }, contentAlignment = Alignment.Center) {
                Icon24(Res.drawable.ic_close, tint = c.ink)
            }
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text("Pytagoras", style = MaterialTheme.typography.labelMedium, color = c.ink2)
                Rich(m.nazov.replace('§', '$'), style = MaterialTheme.typography.titleSmall, color = c.ink)
            }
        }
        Column(Modifier.weight(1f).nestedScroll(nested).verticalScroll(scroll).padding(horizontal = 20.dp)) {
            if (tahy.isEmpty()) {
                Spacer(Modifier.height(24.dp))
                H1("Na čo sa chceš opýtať?")
                Spacer(Modifier.height(6.dp))
                Sub("Pytagoras vidí, kde si v appke. Opýtaj sa vlastnými slovami alebo odfoť svoje riešenie.")
            }
            for (t in tahy) key(t) { TahView(t, m, stav.rozhovor?.art, sluzba, onZnova = { stav.znova(sluzba, scope, m, t, onPosudok) }) }
            Spacer(Modifier.height(16.dp))
        }
        // suggestions: quiet chips by the field, only when nothing is being written or typed
        val cipy = remember(m, tahy.isNotEmpty()) { navrhy(m, tahy.isNotEmpty()) }
        AnimatedVisibility(!bezi && otazka.isEmpty() && cakajuce.isEmpty(), enter = fadeIn() + androidx.compose.animation.expandVertically(), exit = fadeOut() + androidx.compose.animation.shrinkVertically()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((label, q) in cipy) Cip(label, if (q == null) Res.drawable.ic_camera else null) { if (q == null) fotak = 2 else opytaj(q, emptyList()) }
            }
        }
        // photos waiting to be sent: a tap on the cross removes one, the last tile adds more
        if (cakajuce.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            cakajuce.forEachIndexed { i, f -> key(f) {
                Box {
                    Nahlad(f, i, 60)
                    Box(Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp).size(28.dp).clip(CircleShape).background(c.ink).clickable(role = Role.Button) { cakajuce.remove(f) }
                        .semantics { contentDescription = "Odobrať fotku ${i + 1}" }, contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_close, tint = c.bg, modifier = Modifier.size(16.dp)) }
                }
            } }
            if (cakajuce.size < 6) Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, c.edge, RoundedCornerShape(12.dp)).clickable(role = Role.Button) { fotak = 1 }
                .semantics { contentDescription = "Pridať fotku" }, contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_camera, tint = c.ink2) }
        }
        // the question field: a photo, a text, send (Enter or the keyboard's send); while an answer is written the
        // button stops it
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, enabled = cakajuce.size < 6) { fotak = 1 }.semantics { contentDescription = "Odfotiť riešenie" }, contentAlignment = Alignment.Center) {
                Icon24(Res.drawable.ic_camera, tint = if (cakajuce.size < 6) c.ink else c.ink3)
            }
            Box(Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp)).background(c.card).border(1.dp, c.line, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp), contentAlignment = Alignment.CenterStart) {
                if (otazka.isEmpty()) Text(if (cakajuce.isEmpty()) "Opýtaj sa vlastnými slovami…" else "Pridaj otázku alebo pošli", style = MaterialTheme.typography.bodyLarge, color = c.ink3)
                BasicTextField(otazka, { otazka = it.take(600) }, Modifier.fillMaxWidth().focusRequester(pole).onPreviewKeyEvent { e ->
                    // a hardware Enter sends (web, a tablet keyboard), Shift+Enter is a new line
                    if (e.key == androidx.compose.ui.input.key.Key.Enter && !e.isShiftPressed && e.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) { posli(); true } else false
                }, textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.ink), cursorBrush = SolidColor(c.ink), maxLines = 5,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences, imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSend = { posli() }))
            }
            val moze = otazka.isNotBlank() || cakajuce.isNotEmpty()
            val stop = bezi && !moze
            val fill by androidx.compose.animation.animateColorAsState(if (stop) c.ink else if (moze) c.lesson.main else c.sand, androidx.compose.animation.core.tween(180))
            Box(Modifier.padding(start = 6.dp).size(48.dp).clip(CircleShape).background(fill).clickable(role = Role.Button, enabled = moze || stop) { if (stop) stav.zastav() else posli() }
                .semantics { contentDescription = if (stop) "Zastaviť odpoveď" else "Poslať" }, contentAlignment = Alignment.Center) {
                if (stop) Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(c.bg))
                else Icon24(Res.drawable.ic_send, tint = if (moze) c.onAcc else c.ink3)
            }
        }
        Text("Používa tvoj ChatGPT plán · Spravovať využitie", Modifier.fillMaxWidth().clickable { sluzba.spravovatVyuzitie() }.padding(horizontal = 20.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall, color = c.ink3)
    }
}

/** A quiet suggestion: a thin outline, the text in ink2, never louder than the field. */
@Composable
private fun Cip(text: String, icon: org.jetbrains.compose.resources.DrawableResource?, onClick: () -> Unit) {
    val c = T.c
    Row(Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(20.dp)).border(1.dp, c.edge, RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { Icon24(icon, tint = c.ink2, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
    }
}

/** Display formulas ($$…$$) stand as their own pieces: each comes in whole, fading, like a card. */
private fun odseky(s: String): List<String> = s.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }.flatMap { p ->
    if ("$$" !in p) listOf(p) else Regex("\\$\\$.+?\\$\\$", RegexOption.DOT_MATCHES_ALL).let { r ->
        val out = ArrayList<String>(); var at = 0
        for (mm in r.findAll(p)) { p.substring(at, mm.range.first).takeIf { it.isNotBlank() }?.let(out::add); out += mm.value; at = mm.range.last + 1 }
        p.substring(at).takeIf { it.isNotBlank() }?.let(out::add); out
    }
}

@Composable
private fun TahView(t: Tah, m: MiestoAppky, art: Artefakty?, sluzba: TutorSluzba, onZnova: () -> Unit) {
    val c = T.c
    Spacer(Modifier.height(16.dp))
    // the student's turn, right-aligned: the photos as thumbnails (a tap enlarges), the question in the accent
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        if (t.fotky.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { t.fotky.forEachIndexed { i, f -> Nahlad(f, i) } }
            Spacer(Modifier.height(6.dp))
        }
        Text(t.otazka ?: if (t.fotky.isNotEmpty()) "Skontroluj mi to." else "Vysvetli mi to.",
            Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(16.dp)).background(c.lesson.main).padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium, color = c.onAcc)
    }
    Spacer(Modifier.height(12.dp))
    // a paper judged with no question: the same card as in the step
    if (t.otazka == null && t.fotky.isNotEmpty() && t.postup == null && (t.posudok != null || t.verdikt != null || (m.papier != null && m.gem == null))) {
        PosudokKarta(t, onZnova, sluzba, nahlady = false)
        return
    }
    t.postup?.let { (vys, k) -> if (remember(vys) { runCatching { nadpis(vys.hlavny, vys); vys.hlavny?.let { studentovPostup(it); spravnyKrok(it) } }.isSuccess }) { Nabeh { PostupBlok(vys, k) }; Spacer(Modifier.height(12.dp)) } }
    val zivo = remember(t) { !t.hotovo }
    val zobraz = plynulo(t.text, zivo, t.hotovo, hned = t.zastavene || t.chyba != null)
    // what the tutor is doing now (reading the paper, checking a computation), in the place the first words will take
    val caka = !t.hotovo && zobraz.isEmpty()
    if (caka) Cakanie(t.stav ?: "Píšem odpoveď", Modifier.padding(vertical = 2.dp))
    // the answer, written word by word (ui/Pisanie.kt): a formula appears whole, a card or a stepper whole in its place
    val ks = remember(zobraz) { runCatching { kusy(vysaditelne(zobraz), art ?: Artefakty(), m.kurz) }.getOrElse { listOf(Kus.Text(zobraz)) }
        .flatMap { k -> if (k is Kus.Text) odseky(k.t).map { Kus.Text(it) } else listOf(k) } }
    CompositionLocalProvider(dev.hamper.tabula.ui.LocalNabehSlov provides zivo) {
        Column(Modifier.fillMaxWidth().animateContentSize(androidx.compose.animation.core.tween(180))) {
            ks.forEachIndexed { i, k -> key(i, k::class) {
                when (k) {
                    is Kus.Text -> { if (i > 0) Spacer(Modifier.height(8.dp)); if (zivo && k.t.trimStart().startsWith("$$")) Nabeh { Rich(k.t.trim(), style = MaterialTheme.typography.bodyLarge, color = c.ink) } else Rich(k.t.trim(), style = MaterialTheme.typography.bodyLarge, color = c.ink) }
                    is Kus.Veta -> Nabeh { Column { Spacer(Modifier.height(8.dp)); VetaKarta(k.cislo, m.kurz); Spacer(Modifier.height(8.dp)) } }
                    is Kus.Krokovac -> Nabeh { Column { Spacer(Modifier.height(8.dp)); when (val w = art?.krokovace?.get(k.ref)) { is GemWalk -> SourceCard(null) { GemWalkView(w) }; is TableWalk -> SourceCard(null) { TableWalkView(w) }; else -> {} }; Spacer(Modifier.height(8.dp)) } }
                }
            } }
        }
    }
    // a tool called in the middle of an answer: its state under the words written so far
    AnimatedVisibility(!t.hotovo && !caka && t.stav != null, enter = fadeIn() + androidx.compose.animation.expandVertically(), exit = fadeOut() + androidx.compose.animation.shrinkVertically()) {
        Cakanie(t.stav ?: "", Modifier.padding(top = 8.dp))
    }
    t.chyba?.let { e -> if (zobraz.isNotEmpty()) Spacer(Modifier.height(10.dp)); ChybaRiadok(e, onZnova, sluzba) }
    if (t.zastavene && t.chyba == null) { Spacer(Modifier.height(6.dp)); Text("Zastavené.", style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
    t.posudok?.let { (ps, pr) -> Spacer(Modifier.height(10.dp)); Nabeh { Column {
        VerdiktCip(ps)
        // with a question, the words above are the answer to it: the judgement's own reasons go with the verdict
        if (t.otazka != null && ps.text != t.text) { Spacer(Modifier.height(6.dp)); Rich(ps.text, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
        CoSomPrecital(pr)
    } } }
}

/** A block that fades in once, when it first appears. */
@Composable
private fun Nabeh(content: @Composable () -> Unit) {
    val a = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, androidx.compose.animation.core.tween(380)) }
    Box(Modifier.graphicsLayer { alpha = a.value }) { content() }
}


/** [b] scaled so its longer side is [px]. */
private fun zmensi(b: androidx.compose.ui.graphics.ImageBitmap, px: Int): androidx.compose.ui.graphics.ImageBitmap {
    val s = px.toFloat() / maxOf(b.width, b.height)
    if (s >= 1f) return b
    val w = maxOf(1, (b.width * s).toInt()); val h = maxOf(1, (b.height * s).toInt())
    val out = androidx.compose.ui.graphics.ImageBitmap(w, h)
    androidx.compose.ui.graphics.Canvas(out).drawImageRect(b, dstSize = androidx.compose.ui.unit.IntSize(w, h), paint = androidx.compose.ui.graphics.Paint())
    return out
}

/** A photo the student sent, small; a tap opens it whole. */
@Composable
private fun Nahlad(jpg: ByteArray, i: Int, dp: Int = 72) {
    val c = T.c
    // a small copy is kept, the full page decoded only while it is open (six pages of a turn would hold ~70 MB)
    val img = remember(jpg) { runCatching { zmensi(jpg.decodeToImageBitmap(), 160) }.getOrNull() } ?: return
    var velky by remember { mutableStateOf(false) }
    androidx.compose.foundation.Image(img, "Fotka ${i + 1}, ťukni a zväčší sa", Modifier.size(dp.dp).clip(RoundedCornerShape(if (dp < 60) 8.dp else 12.dp)).border(1.dp, c.line, RoundedCornerShape(12.dp))
        .clickable(role = Role.Button) { velky = true }, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
    if (velky) androidx.compose.ui.window.Dialog({ velky = false }, TutorOkno) {
        // the photo lies on black over the whole window, like a clip in fullscreen: the one view wider than the app's column
        dev.hamper.tabula.compat.DialogBars(light = false)
        val cela = remember(jpg) { runCatching { jpg.decodeToImageBitmap() }.getOrNull() }
        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black).clickable { velky = false }.semantics { contentDescription = "Zavrieť fotku" }, contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(cela ?: img, "Fotka ${i + 1}", Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
        }
    }
}

@Composable
private fun VetaKarta(cislo: String, kurz: String) {
    when (val t = vetaCiel(cislo, kurz)) {
        is RefTarget.Thm -> TheoremCard(t.t)
        is RefTarget.Def -> ConceptCard(t.k)
        is RefTarget.Quote -> SourceCard(t.n.name.ifEmpty { cislo }) { SourceLines(t.n.lines) }
        null -> {}
    }
}

/** The engine's verdict on a photographed GEM: its headline, the student's stepper with the mark, the right step. */
@Composable
private fun PostupBlok(vys0: Vysledok, k: Kontext) {
    val c = T.c
    val vys = vys0
    var v by remember(vys0) { mutableStateOf(vys0.hlavny) }
    var gem by remember(vys0) { mutableStateOf(vys0.prepis.gemy.getOrNull(vys0.verdikty.indexOf(vys0.hlavny))?.gem) }
    var potvrdene by remember(vys0) { mutableStateOf(emptySet<dev.hamper.tabula.tutor.Miesto>()) }
    val bad = v?.stav == Verdikt.Stav.CHYBA
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(if (bad) c.badTint else c.card).border(1.dp, if (bad) c.badLine else c.line, RoundedCornerShape(24.dp)).padding(16.dp)) {
        Text("Appka skontrolovala tvoj postup GEM", style = MaterialTheme.typography.labelMedium, color = c.ink2)
        Spacer(Modifier.height(4.dp))
        Rich(nadpis(v, vys), style = MaterialTheme.typography.titleMedium, color = if (bad) c.badInk else c.ink)
        val vv = v ?: return@Column
        studentovPostup(vv)?.let { walk ->
            Spacer(Modifier.height(10.dp))
            val kk = vv.krok ?: vv.otazka?.miesto?.matica
            val zn = when {
                vv.stav == Verdikt.Stav.CHYBA && kk != null -> GemZnacky(kk, vv.riadky, vv.bunky.map { it.miesto.riadok to it.miesto.stlpec }.toSet(), neiste = vv.overitCitanie)
                vv.stav == Verdikt.Stav.OTAZKA && vv.otazka != null -> vv.otazka.miesto.let { GemZnacky(it.matica, emptySet(), setOf(it.riadok to it.stlpec), neiste = true) }
                else -> null
            }
            SourceCard("Na papieri", fill = c.card) { GemWalkView(walk, znacky = zn, otvor = kk ?: walk.size) }
        }
        // the engine's own question when a reading is in doubt: the answer is checked again, without the model
        vv.otazka?.takeIf { vv.stav == Verdikt.Stav.OTAZKA }?.let { q ->
            Spacer(Modifier.height(10.dp))
            Text("Neviem rozlíšiť jedno číslo. Čo tam máš?", style = MaterialTheme.typography.titleSmall, color = c.ink)
            Spacer(Modifier.height(6.dp))
            ButtonRow { for (o in q.moznosti) Choice("$" + o.tex(true) + "$", {
                gem?.let { g -> val (ng, nv) = odpovedzOtazku(k.ulohy, g, q.miesto, o, potvrdene); gem = ng; potvrdene = potvrdene + q.miesto; v = nv }
            }, center = true) }
        }
        if (vv.overitCitanie && vv.bunky.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Rich("Je to posledná matica, žiadny ďalší krok to nepotvrdí. Ak tam máš $${vv.bunky[0].maloByt.tex(true)}$, prečítal som to zle.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        spravnyKrok(vv)?.let { right ->
            Spacer(Modifier.height(10.dp))
            SourceCard(if (vv.stav == Verdikt.Stav.NEDOKONCENE) "Ďalší krok" else "Správne by bolo", fill = c.card) { GemWalkView(right, otvor = right.size) }
        }
    }
}

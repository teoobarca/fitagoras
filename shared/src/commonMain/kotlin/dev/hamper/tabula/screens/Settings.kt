package dev.hamper.tabula.screens

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.Env
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.state.ClipSpeeds
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.system.Telemetry
import dev.hamper.tabula.system.Updater
import dev.hamper.tabula.system.UpdatePhase
import dev.hamper.tabula.platform.LocalPlatform
import dev.hamper.tabula.math.nb
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.compat.time.Instant
import dev.hamper.tabula.compat.time.ZoneOffset
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.res.*
import org.jetbrains.compose.resources.DrawableResource

const val FitestPage = "https://fitagoras.hamper.dev"

/*
 * Settings in groups: what the student's courses are (parallel and test sitting per course, which drive "Kde sme",
 * the plan and the countdown), how the app looks and feels, clips, privacy, updates, and about. Each row says in one
 * line what it changes.
 */
@Composable
fun SettingsScreen(store: Store, onBack: () -> Unit, onReport: (String) -> Unit) {
    val c = T.c
    val platform = LocalPlatform.current
    // the browser has none of the device rows below (Platform.isWeb)
    val web = platform.isWeb
    val hx = rememberHaptics()
    @Suppress("UNUSED_VARIABLE") val v = store.version
    var course by remember { mutableStateOf<Course?>(null) }
    var markDone by remember { mutableStateOf(false) }
    val ask = platform.rememberReminderPermission { ok -> store.reminder = ok; store.save(); if (ok) platform.scheduleReminder() }
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding()) {
        Box(Modifier.padding(start = 6.dp).size(48.dp).clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onBack).semantics { contentDescription = "Späť" }, contentAlignment = Alignment.Center) {
            Icon24(Res.drawable.ic_back, tint = c.ink)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding()) {
            Text("Nastavenia", style = MaterialTheme.typography.headlineLarge, color = c.ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(4.dp))

            // less text (Teo, 18. 11.: "Nastavenia kus lepšie"): the rows say what they hold, a line under them only
            // where it decides something
            Group("Predmety") {
                Courses.forEachIndexed { i, k ->
                    if (i > 0) Divider(full = LocalDensity.current.fontScale > 1.3f)
                    val p = k.schedule.parallels.firstOrNull { it.id == store.parallel(k) }
                    // the course's code where the others have an icon, as the tabs on Dnes name it: one cap icon on
                    // every course told them apart by nothing (judge, 18. 11.)
                    NavRow(
                        null, k.name, code = k.short,
                        sub = listOfNotNull(
                            p?.let { "Paralelka ${it.id}, ${it.teacher}, ${weekday2(it.day)}\u00A0${it.time}" },
                            "${k.test.short.replaceFirstChar { it.uppercase() }}: ${testWhen(store, k)}",
                        ).joinToString("\n"),
                    ) { course = k }
                }
                Divider()
                NavRow(Res.drawable.ic_check, "Označiť hotové lekcie", "Čo už máš z prednášok") { markDone = true }
            }

            // where progress lives, honest and short: a line under the courses, no card and no button that does nothing
            // yet (judges, 18. 11.: a long paragraph without an action pushed the theme under the fold)
            if (!web) Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 12.dp), verticalAlignment = Alignment.Top) {
                Icon24(Res.drawable.ic_phone, tint = c.ink2, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                // what keeps it and what loses it (the old card's warning, Teo's meaning of the footer): an update keeps it, a new
                // phone or uninstalling loses it
                Text("Postup je len v tomto telefóne. Aktualizácia ho nezmaže, nový telefón ani odinštalovanie ho neprenesú.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            }
            platform.SettingsRows(store)

            Group("Vzhľad a zvuk") {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon24(Res.drawable.ic_palette, tint = c.ink2)
                        Spacer(Modifier.width(14.dp))
                        Text("Téma", style = MaterialTheme.typography.titleMedium, color = c.ink)
                    }
                    Spacer(Modifier.height(10.dp))
                    Segmented(listOf(ThemeMode.System to "Systémová", ThemeMode.Light to "Svetlá", ThemeMode.Dark to "Tmavá"), store.theme) { store.theme = it; store.save() }
                }
                Divider()
                if (!web) ToggleRow(Res.drawable.ic_vibration, "Haptika", "Pri ťuknutí a odpovedi", store.haptics) { store.haptics = it; store.save(); if (it) hx.right() }
                ToggleRow(Res.drawable.ic_volume, "Zvuky", if (store.sounds && Sounds.muted()) "Hlasitosť médií je na nule: pridaj ju tlačidlom hlasitosti." else "Pri odpovedi a na konci lekcie, podľa hlasitosti médií", store.sounds) { store.sounds = it; store.save(); if (it) Sounds.play(Sfx.Correct) }
            }

            if (!web) Group("Klipy z prednášok") {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon24(Res.drawable.ic_speed, tint = c.ink2)
                        Spacer(Modifier.width(14.dp))
                        Text("Rýchlosť prehrávania", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = c.ink)
                    }
                    Spacer(Modifier.height(10.dp))
                    for (row in ClipSpeeds.chunked(3)) {
                        Row(Modifier.padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (s in row) Choice(speedLabel(s), { store.clipSpeed = s; store.save() }, Modifier.weight(1f), center = true,
                                state = if (store.clipSpeed == s) Pick.Picked else Pick.Idle, minHeight = 48.dp, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            if (!web) Group("Pripomienka a widget") {
                ToggleRow(Res.drawable.ic_bell, "Pripomienka", "Oznámenie s dnešným plánom", store.reminder) { on ->
                    if (!on) { store.reminder = false; store.save() }
                    else if (platform.remindersAllowed()) { store.reminder = true; store.save(); platform.scheduleReminder() }
                    else ask()
                }
                Divider()
                NavRow(Res.drawable.ic_widgets, "Pridať widget na plochu", "Dni do testu a čo je dnes na rade") {
                    platform.pinWidget()
                }
            }

            val tutor = dev.hamper.tabula.tutor.LocalTutor.current
            // the developer mode as state, so the debug build's long press shows and hides its tools at once
            var vyvojar by remember { mutableStateOf(tutor?.vyvojar == true) }
            if (tutor != null && dev.hamper.tabula.tutor.Brana.karta(tutor)) TutorKarta(tutor, vyvojar)

            if (!web) Group("Súkromie") {
                ToggleRow(Res.drawable.ic_stat, "Posielať štatistiky", "Bez mena, e-mailu a IP adresy, pod náhodným číslom. Keď sú vypnuté, appka nepošle nič, ani pády, okrem chyby, ktorú nahlásiš sám.", Telemetry.enabled) { Telemetry.switch(it) }
                // the whole list, word for word as before, one tap away: what is sent is not shortened, only folded
                var co by remember { mutableStateOf(false) }
                Quiet(if (co) "Skryť" else "Čo presne sa posiela", { co = !co }, Modifier.padding(start = 52.dp), color = c.lesson.text)
                AnimatedVisibility(co) {
                    Text("Pod náhodným číslom (história prežije aj preinštalovanie): kroky lekcií, kedy a ako dlho, odpovede (nesprávne aj ako si ich zadal), nápovedy, preskočenia, klipy, testy, verzia appky, pády, model telefónu, Android, obrazovka, písmo, tmavý režim, jazyk, časové pásmo a sieť. Vypnutím sa nič z toho neposiela, telefón sa od histórie odpojí a po zapnutí začne nová. Chybu, ktorú nahlásiš sám (vlajkou alebo Nahlásiť problém), pošle appka aj vtedy, bez čísla.",
                        Modifier.padding(start = 52.dp, end = 14.dp, bottom = 12.dp), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                }
            }

            if (!web) Group("Aktualizácie") {
                ToggleRow(Res.drawable.ic_refresh, "Kontrolovať automaticky", "Pri otvorení appky, nič o tebe nepošle", store.autoUpdate) { store.autoUpdate = it; store.save() }
                Divider()
                val phase = Updater.phase
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Icon24(Res.drawable.ic_download, tint = c.ink2, modifier = Modifier.padding(top = 2.dp).size(24.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        // a long press on the version switches the tutor's developer mode (docs/TUTOR.md, "Brána")
                        Text("Verzia ${Env.VERSION_NAME}", style = MaterialTheme.typography.titleMedium, color = c.ink,
                            // the tutor's developer mode only in a debug build, never behind a gesture a student can find
                            modifier = if (!Env.DEBUG) Modifier else Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { tutor?.let { t -> t.vyvojar = !t.vyvojar; vyvojar = t.vyvojar } }) })
                        val (line, bad) = when {
                            phase == UpdatePhase.Checking -> "Pýtam sa servera…" to false
                            phase == UpdatePhase.UpToDate -> "Máš najnovšiu verziu." to false
                            phase is UpdatePhase.Failed -> phase.message to true
                            store.release != null -> "Je nová verzia ${store.release!!.versionName}." to false
                            store.autoUpdate -> "Nové verzie ponúkne appka sama na úvodnej obrazovke." to false
                            else -> "Automatická kontrola je vypnutá." to false
                        }
                        Text(line, style = MaterialTheme.typography.bodySmall, color = if (bad) c.bad else c.ink2)
                        Spacer(Modifier.height(10.dp))
                        val newer = store.release
                        SmallButton(if (newer != null && phase !is UpdatePhase.Failed) "Zobraziť novú verziu" else if (phase is UpdatePhase.Failed) "Skúsiť znova" else "Skontrolovať teraz", enabled = phase != UpdatePhase.Checking) {
                            if (newer != null && phase !is UpdatePhase.Failed) Updater.asked = true else platform.checkUpdate(store, manual = true)
                        }
                    }
                }
            }

            Group("O appke") {
                NavRow(Res.drawable.ic_open, "Stránka FITagoras", "Video a odkaz na stiahnutie pre kamarátov", trailing = Res.drawable.ic_open) {
                    platform.openUrl(FitestPage)
                }
                // the web sends no reports (Telemetry is off there): no row that would only keep a copy
                if (!web) Divider()
                if (!web) NavRow(Res.drawable.ic_report, "Nahlásiť problém", "Pošle sa autorovi") { onReport("app") }
                if (store.reports.isNotEmpty()) {
                    Divider()
                    var open by remember { mutableStateOf(false) }
                    NavRow(Res.drawable.ic_info, "Tvoje hlásenia (${store.reports.size})", "Kópia toho, čo si poslal", expanded = open) { open = !open }
                    AnimatedVisibility(open) {
                        Column(Modifier.padding(start = 52.dp, end = 14.dp, bottom = 10.dp)) {
                            store.reports.asReversed().forEach { r ->
                                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Text(if (r.item == "app") "appka" else r.item, style = MaterialTheme.typography.labelMedium, color = c.ink2)
                                    Text(r.text.ifEmpty { "(bez textu)" }, style = MaterialTheme.typography.bodyMedium, color = c.ink)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(if (web) "FITagoras ${Env.VERSION_NAME}." else "FITagoras ${Env.VERSION_NAME}. Chybu v úlohe nahlásiš vlajkou hore pri každom kroku.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            Spacer(Modifier.height(10.dp))
            Text("Zdroje", style = MaterialTheme.typography.titleSmall, color = c.ink2)
            for (k in Courses) Text("${k.short}: ${k.sources}", style = MaterialTheme.typography.bodySmall, color = c.ink2, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(48.dp))
        }
    }
    course?.let { k -> CourseSettingsSheet(store, k) { course = null } }
    if (markDone) androidx.compose.ui.window.Dialog({ markDone = false }, androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        AppColumn { MarkDoneScreen(store) { markDone = false } }
    }
}

/** One course: the parallel of its lectures and the sitting of its test, each with what it changes. */
@Composable
internal fun CourseSettingsSheet(store: Store, k: Course, onClose: () -> Unit) {
    val c = T.c
    val platform = LocalPlatform.current
    var picker by remember { mutableStateOf(false) }
    ModalBottomSheet(onClose, sheetState = rememberModalBottomSheetState(true), containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 16.dp).navigationBarsPadding()) {
            Label(k.code)
            Text(k.name, style = MaterialTheme.typography.headlineMedium, color = c.ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(14.dp))
            Text("Paralelka prednášok", style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text("Podľa nej appka ukáže, čo už na prednáške bolo a čo príde (Kde sme).", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            Spacer(Modifier.height(8.dp))
            for (p in k.schedule.parallels) {
                Choice("${p.id}. ${p.teacher}, ${weekday2(p.day)} ${p.time}", { store.pickParallel(k, p.id) }, Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    state = if (store.parallel(k) == p.id) Pick.Picked else Pick.Idle, minHeight = 52.dp, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(12.dp))
            Text("Termín: ${k.test.title}", style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text("Ten, na ktorý si prihlásený v KOS. Podľa neho je plán a odpočet.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            Spacer(Modifier.height(8.dp))
            val picked = store.testStart(k)
            val known = k.test.terms.any { it.start == picked }
            for (t in k.test.terms) {
                Choice("${weekday(t.start.toLocalDate())} ${t.start.format(DayFmt)} o ${t.start.toLocalTime()}, ${t.room}" + (t.note?.let { ", $it" } ?: ""),
                    { store.pickTerm(k, t.start); platform.refreshWidget() }, Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    state = if (picked == t.start) Pick.Picked else Pick.Idle, minHeight = 52.dp, style = MaterialTheme.typography.titleSmall)
            }
            // an exam without a day yet (no KOS terms, none picked) has no "vlastný dátum": its window's start is not one
            if (!known && store.testWindow(k) == null) Choice("${weekday(picked.toLocalDate())} ${picked.format(DayFmt)}${atTime(picked)}, vlastný dátum", {}, Modifier.fillMaxWidth().padding(bottom = 6.dp),
                state = Pick.Picked, minHeight = 52.dp, style = MaterialTheme.typography.titleSmall)
            Second("Iný dátum", { picker = true }, icon = Res.drawable.ic_calendar)
            Spacer(Modifier.height(8.dp))
            Text("${k.test.short.replaceFirstChar { it.uppercase() }}: ${k.test.points} bodov, ${k.test.minutes ?: 70} minút. " + (if (k.test.terms.isEmpty()) "Termíny v KOS ešte nie sú." else "Termíny z KOS k 15. 11. 2026."), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
    }
    if (picker) {
        val st = rememberDatePickerState(initialSelectedDateMillis = store.testDay(k).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { picker = false },
            confirmButton = { TextButton({ st.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().let { d ->
                // a KOS term that day keeps its time; any other day is stored without one (midnight = time unknown)
                store.pickTerm(k, k.test.terms.filter { t -> t.start.toLocalDate() == d }.minByOrNull { t -> t.start }?.start ?: d.atStartOfDay())
            }; platform.refreshWidget() }; picker = false }) { Text("Uložiť") } },
            dismissButton = { TextButton({ picker = false }) { Text("Zrušiť") } },
        ) { DatePicker(st) }
    }
}

/* ---- the tutor ---------------------------------------------------------------------------------------------------- */

/**
 * "Pytagoras" (the tutor, through the student's ChatGPT): what it is, the account, the one-time notice that it uses
 * the student's plan (OpenAI's UI guidelines), "Spravovať využitie", "Odpojiť". The first sign-in is where the tutor is used (PrvePripojenie).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TutorKarta(t: dev.hamper.tabula.tutor.TutorSluzba, vyvojar: Boolean) {
    val c = T.c
    val p by t.pripojenie.collectAsState()
    var poucenie by remember { mutableStateOf(false) }
    LaunchedEffect(p) { if (p is dev.hamper.tabula.tutor.Pripojenie.Pripojeny && !t.poucenyOPlane) poucenie = true }
    // one card in the student's words (Teo, 18. 11.: nothing about harness, models or settings of ChatGPT): signed in, the
    // account and its two actions; not signed in, what it does and the sign-in, the same button as where it is used
    Group("Pytagoras") {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            // what it is, in every state: an AI, through the student's own ChatGPT, and it can be wrong (Teo, 20. 11.)
            Text("Pytagoras je AI pomocník: skontroluje tvoj papier a poradí s krokom. Beží cez tvoj účet ChatGPT a môže sa pomýliť.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
            Spacer(Modifier.height(12.dp))
            when (val s = p) {
                dev.hamper.tabula.tutor.Pripojenie.Nepripojeny -> {
                    ChatGptTlacidlo("Prihlásiť sa cez ChatGPT") { t.pripoj() }
                }
                dev.hamper.tabula.tutor.Pripojenie.Pripaja -> {
                    Text("Čakám na prihlásenie v prehliadači…", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallButton("Otvoriť prihlásenie znova") { t.pripoj() }
                        SmallButton("Zrušiť") { t.zrusPrihlasenie() }
                    }
                    t.CakamNaPrihlasenie()
                }
                is dev.hamper.tabula.tutor.Pripojenie.Pripojeny -> {
                    Text(s.email ?: "Prihlásený cez ChatGPT", style = MaterialTheme.typography.titleMedium, color = c.ink, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    if (!s.planPovoleny) {
                        Text("Ešte treba povoliť používanie tvojho plánu.", style = MaterialTheme.typography.bodySmall, color = c.badInk)
                        Spacer(Modifier.height(10.dp))
                        ChatGptTlacidlo("Povoliť v ChatGPT") { t.pripoj(suhlas = true) }
                    }
                    Spacer(Modifier.height(10.dp))
                    // a narrow phone with a large font puts the second button under the first instead of cutting it
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallButton("Spravovať využitie") { t.spravovatVyuzitie() }
                        SmallButton("Odpojiť") { t.odpoj() }
                    }
                }
            }
            if (Env.DEBUG && vyvojar) {
                // a debug build's developer mode only: the handwriting collected for the harness, shared as one file
                Spacer(Modifier.height(10.dp))
                val n = t.zbierkaPocet()
                Text("Zbierka pre harness: $n ${if (n == 1) "strana" else if (n in 2..4) "strany" else "strán"} v tomto telefóne.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                if (n > 0) {
                    Spacer(Modifier.height(8.dp))
                    var zmazane by remember { mutableStateOf(false) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallButton("Exportovať zbierku") { t.exportujZbierku() }
                        SmallButton(if (zmazane) "Vymazané" else "Vymazať zbierku", enabled = !zmazane) { t.vymazZbierku(); zmazane = true }
                    }
                }
            }
        }
    }
    if (poucenie) androidx.compose.material3.AlertDialog(
        onDismissRequest = {},
        title = { Text("Používaš svoj ChatGPT plán") },
        text = { Text("Každá kontrola ide cez tvoj ChatGPT účet a míňa jeho limit.") },
        confirmButton = { androidx.compose.material3.TextButton({ t.poucenyOPlane = true; poucenie = false }) { Text("Rozumiem") } },
        modifier = Modifier.widthIn(max = DialogWidth),
    )
}

/* ---- pieces ------------------------------------------------------------------------------------------------------ */

/**
 * One of a few options in a single row (the theme): a sunk track, the picked one raised on paper (in the dark a lighter
 * surface than the track, so it reads lifted, not pressed in). All labels share one size, the one at which the longest
 * fits its third (judges, 18. 11.: "Systémová" alone came out smaller).
 */
@Composable
private fun <T> Segmented(options: List<Pair<T, String>>, picked: T, onPick: (T) -> Unit) {
    val c = dev.hamper.tabula.ui.T.c
    val hx = rememberHaptics()
    // at a large font the options stand one under another, so no word is split across lines
    val stacked = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
    val track = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.sand).padding(4.dp).selectableGroup()
    val base = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val d = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val slot = with(d) { ((maxWidth - 8.dp - 4.dp * (options.size - 1)) / options.size - 12.dp).toPx() }
        val widest = options.maxOf { measurer.measure(it.second, base, maxLines = 1, softWrap = false).size.width }.toFloat()
        val size = if (stacked || widest <= slot) 15.sp else (15f * slot / widest).coerceAtLeast(11f).sp
        val each: @Composable (Modifier) -> Unit = { m ->
            for ((v, label) in options) {
                val on = v == picked
                Box(
                    m.heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                        .then(if (on) Modifier.background(if (c.dark) c.lesson.tint else c.card).then(if (c.dark) Modifier.border(1.5.dp, c.lesson.main, RoundedCornerShape(12.dp)) else Modifier.border(1.dp, c.line, RoundedCornerShape(12.dp))) else Modifier)
                        .selectable(on, role = Role.RadioButton) { hx.tap(); onPick(v) }.padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, style = MaterialTheme.typography.titleSmall.copy(fontSize = size, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium), color = if (on) c.ink else c.ink2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, softWrap = false)
                }
            }
        }
        if (stacked) Column(track, verticalArrangement = Arrangement.spacedBy(4.dp)) { each(Modifier.fillMaxWidth()) }
        else Row(track, horizontalArrangement = Arrangement.spacedBy(4.dp)) { each(Modifier.weight(1f)) }
    }
}

/** A titled group of rows on one quiet panel. */
@Composable
internal fun Group(title: String, sub: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleLarge, color = c.ink, modifier = Modifier.semantics { heading() })
    if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
    Spacer(Modifier.height(8.dp))
    Column(Modifier.fillMaxWidth().card().clip(RoundedCornerShape(24.dp)), content = content)
}

@Composable
// [full]: between rows whose text starts at the card's edge (the courses at a large font), from that edge
internal fun Divider(full: Boolean = false) = Box(Modifier.padding(start = if (full) 14.dp else 52.dp).fillMaxWidth().height(1.dp).background(T.c.line))

@Composable
private fun ToggleRow(icon: DrawableResource, title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().toggleable(on, role = Role.Switch, onValueChange = onChange).heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon24(icon, tint = c.ink2)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Spacer(Modifier.width(8.dp))
        Switch(on, null, colors = SwitchDefaults.colors(checkedTrackColor = c.lesson.main, checkedThumbColor = c.card, uncheckedTrackColor = c.sand, uncheckedBorderColor = c.axis, uncheckedThumbColor = c.ink2))
    }
}

@Composable
internal fun NavRow(icon: DrawableResource?, title: String, sub: String, trailing: DrawableResource = Res.drawable.ic_chevron, expanded: Boolean? = null, code: String? = null, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { if (expanded != null) stateDescription = if (expanded) "rozbalené" else "zbalené" }
            .heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // the code in the icon's column (24 dp and its gap), the title where every row's title starts; at a large font
        // the code would take the title's room ("matematik / a"), so it heads the title and everything under it starts
        // at the card's edge (an empty column beside them left a narrow strip: "Klouda, streda / 09:15"; inspector, 18. 11.)
        val bigCode = code != null && LocalDensity.current.fontScale > 1.3f
        if (code != null && !bigCode) Box(Modifier.width(38.dp)) { Text(code, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight(750)), color = c.ink, maxLines = 1, softWrap = false) }
        else if (code == null && icon != null) { Icon24(icon, tint = c.ink2); Spacer(Modifier.width(14.dp)) }
        Column(Modifier.weight(1f)) {
            if (bigCode) Text(code!!, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight(750)), color = c.ink)
            // "2. test: štvrtok 19. 11. o 18:00": the date and the time never break inside
            Text(title.nb(), style = MaterialTheme.typography.titleMedium, color = c.ink, onTextLayout = { LabelAudit.split(it) })
            Text(sub.nb(), style = MaterialTheme.typography.bodySmall, color = c.ink2, onTextLayout = { LabelAudit.split(it) })
        }
        Spacer(Modifier.width(8.dp))
        Icon24(trailing, tint = c.ink3, modifier = Modifier.size(if (trailing == Res.drawable.ic_open) 20.dp else 24.dp).graphicsLayer { rotationZ = when (expanded) { true -> -90f; false -> 90f; null -> 0f } })
    }
}

/** A compact action inside a row. */
@Composable
private fun SmallButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = T.c
    val hx = rememberHaptics()
    Box(
        Modifier.clip(RoundedCornerShape(16.dp)).background(if (enabled) c.lesson.tint else c.sand).clickable(enabled = enabled, role = Role.Button) { hx.tap(); onClick() }
            .heightIn(min = 48.dp).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = MaterialTheme.typography.titleSmall, color = if (enabled) c.lesson.text else c.ink3, maxLines = 1) }
}

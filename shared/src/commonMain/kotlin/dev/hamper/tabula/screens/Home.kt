@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.hamper.tabula.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.em
import dev.hamper.tabula.state.dayOf
import androidx.compose.material3.*
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.math.nb
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.platform.LocalPlatform
import org.jetbrains.compose.resources.DrawableResource
import dev.hamper.tabula.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.hamper.tabula.compat.time.Instant
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.ZoneOffset
import dev.hamper.tabula.compat.nanoTime
import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.time.*
import dev.hamper.tabula.res.*

/** Where the student is (data/Today.kt): the lesson worked on last while unfinished, else the next one. */
fun nextLesson(store: Store, course: Course = store.course): LessonDef? = todayOf(store, course).first

/**
 * Dnes's choice for [course] from the record (data/Today.kt, todayChoice): the hero lesson and the older unfinished
 * ones. The order is the plan's: the test's lessons first while any is left, then the rest of the course.
 */
fun todayOf(store: Store, course: Course = store.course, today: LocalDate = LocalDate.now()): Pair<LessonDef?, List<LessonDef>> {
    val sh = course.shown()
    val order = if (sh.testLessons.any { it.id !in store.lessonsDone }) sh.testLessons + sh.lessons.filter { it !in sh.testLessons } else sh.lessons
    val states = order.map { l ->
        LessonState(l.id, l.id in store.lessonsDone, store.inProgress(l), store.lessonTouched[l.id], if (store.inProgress(l)) store.stepsLeft(l) else l.steps.size, l.id in store.lessonGaps, l in sh.testLessons)
    }
    val days = runCatching { daysTo(store.testStart(course).toLocalDate(), today) }.getOrNull()
    val ch = todayChoice(states, currentTimeMillis(), days)
    val byId = order.associateBy { it.id }
    return ch.hero?.let { byId[it] } to ch.unfinished.mapNotNull { byId[it] }
}

/** "2. test z LA1, streda 18. 11. o 18:00"; "Skúška z LA1, skúškové 4. 1. – 14. 2." while it has no day. */
fun testLine(store: Store, course: Course = store.course): String = "${course.test.short} z ${course.short}, ${testWhen(store, course)}"

/** "streda 18. 11. o 18:00", or "skúškové 4. 1. – 14. 2." for an exam without a day yet ([Store.testWindow]). */
fun testWhen(store: Store, course: Course = store.course): String {
    store.testWindow(course)?.let { return "skúškové ${windowText(it)}" }
    val t = store.testStart(course)
    return "${weekday(t.toLocalDate())} ${t.format(DayFmt)}${atTime(t)}"
}

/** "z testov", "zo skúšok": the preposition before a word that starts with s, z, š, ž and a consonant takes an "o". */
fun zo(w: String) = (if (w.length > 1 && w[0].lowercaseChar() in "szšž" && w[1].lowercaseChar() !in "aáäeéiíoóôuúyý") "zo " else "z ") + w

/** "4. 1. – 14. 2." */
fun windowText(w: Pair<LocalDate, LocalDate>) = "${w.first.format(DayFmt)} – ${w.second.format(DayFmt)}"

/**
 * The exam the student wrote last, for a week after its sitting ("2. test máš za sebou"): the one before the course's
 * current exam among those the app prepares for, by the picked sitting, else its last term. Null otherwise.
 */
fun justWritten(store: Store, course: Course = store.course, now: dev.hamper.tabula.compat.time.LocalDateTime = dev.hamper.tabula.compat.time.LocalDateTime.now()): Exam? {
    val ps = course.exams.filter { it.prepares }
    val e = ps.getOrNull(ps.indexOf(course.test) - 1) ?: return null
    val end = store.terms[e.id]?.plusMinutes((e.minutes ?: 70).toLong()) ?: examOver(e)
    return e.takeIf { end <= now && end.plusDays(7) > now }
}

fun resumeAt(store: Store, l: LessonDef) = store.runAt(l)

/* ---- Dnes ------------------------------------------------------------------------------------------------------- */

/*
 * Dnes (design review 17. 11., the style notes; Teo's order of the hero card is locked): the date and the days in a row, the
 * courses as tabs, one hero card with the lesson to go on with, "Dnes ešte" with two or three things and why each is
 * there today, one block about the test, and a quiet line about where the progress is kept. Everything from the
 * student's own record; what the app does not know it does not show.
 */

@Composable
fun TodayScreen(store: Store, bottom: Dp, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    val course = store.course
    val today = rememberCurrentDate()
    val (next, unfinished) = todayOf(store, course, today)
    // a finished lesson changes the hero: come back to the top so it is the first thing seen, not an old scroll position
    val scroll = rememberScrollState()
    var seen by rememberSaveable { mutableIntStateOf(store.lessonsDone.size) }
    LaunchedEffect(store.lessonsDone.size) { if (store.lessonsDone.size != seen) { seen = store.lessonsDone.size; scroll.scrollTo(0) } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val pad = if (maxWidth < 340.dp) 16.dp else 20.dp
    CompositionLocalProvider(LocalWindowWidth provides maxWidth) {
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(scroll).padding(horizontal = pad).padding(top = 2.dp, bottom = bottom + 32.dp)) {
        TopLine(store, today, onRoute)
        SubjectTabs(store, today)
        Spacer(Modifier.height(14.dp))
        val fresh = store.attempts.isEmpty() && store.lessonsDone.isEmpty() && store.lessonAt.isEmpty()
        val doneToday = store.doneOn.filterValues { it == today.toString() }.keys.mapNotNull { id -> course.shown().lessons.firstOrNull { it.id == id } }
        // two days before the test the hero is what the tab Test offers (the quick way to a problem whose lessons are
        // missing, else the whole test): the two never disagree; the lesson waits on the Cesta
        val pre = remember(course.id, course.test.id, store.version, today) { beforeTestNow(store, course, today) }
        // the card on top in the course's own colour, the one place besides its tab that has it (ui/Smer.kt)
        CompositionLocalProvider(LocalPalette provides withCourse(c)) {
            when {
                pre != null -> TestNowBlock(preLabel(store, today), pre, onRoute)
                next == null -> AllDoneCard(store, onRoute)
                doneToday.isNotEmpty() && !store.inProgress(next) -> DoneTodayCard(store, doneToday.last(), next, onRoute)
                else -> HeroCard(store, next, fresh, onRoute)
            }
        }
        // said once, under the hero: the way into the first lesson stays on the first screen (design review 18. 11.)
        if (!store.statsNoticed && dev.hamper.tabula.system.Telemetry.enabled) {
            Spacer(Modifier.height(8.dp))
            StatsNotice({ store.statsNoticed = true; store.save() }) { store.statsNoticed = true; store.save(); onRoute("settings") }
        }
        // today's review: the first row while there is something in it; done, it is gone until tomorrow
        val dose = remember(store.version, today, course.id) { reviewToday(store, today, course) }
        val review = reviewRow(dose)
        // before the test the lesson the test's card displaced waits on the Cesta: one thing now, not a second choice
        // under it (student, 19. 11.: "mám spraviť aj Vektorový priestor?")
        val more = remember(store.version, today, next?.id, unfinished, review) { listOfNotNull(review) + unfinishedRows(store, unfinished) + todayMore(store, today, next).take((if (unfinished.isEmpty()) 3 else 2) - (if (review != null) 1 else 0)) }
        // a new student gets a welcome, not a list: nothing of theirs to bring back yet (before the test the card above
        // is their one thing, and the welcome's "the first lesson above" would point at nothing)
        if (fresh) { if (pre == null) EmptyMore(course.test.word) }
        else if (more.isNotEmpty()) {
            // each thing says its own minutes; no sum by the heading (it wrapped at a large font and counted only some)
            Text("Dnes ešte", Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 2.dp, start = 2.dp).semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = c.ink)
            more.forEachIndexed { i, t -> MoreRow(t, first = i == 0, onKnow = t.know?.let { id -> { store.knowLesson(id) } }) { onRoute(t.route) } }
        }
        // the test's card above already says when the test is: no second card with the same date
        if (pre == null) { Spacer(Modifier.height(28.dp)); TestRow(store, today, onRoute) }
        Spacer(Modifier.height(20.dp))
        BackupLine(LocalPlatform.current.isWeb) { onRoute("settings") }
    }
    }
    }
}

/**
 * Streda 18. 11., the days in a row under it, settings beside. Two quiet lines instead of one crowded row (judges,
 * 18. 11., round 3: the date, the marks, the words and the icon competed in one row before the hero).
 */
@Composable
private fun TopLine(store: Store, today: LocalDate, onRoute: (String) -> Unit) {
    val c = T.c
    val n = streak(store, today)
    // at a large font the days in a row take the whole width under the row, so "6 dní po sebe" stays one line
    val big = LocalDensity.current.fontScale >= 1.5f
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${weekday(today).replaceFirstChar { it.uppercase() }} ${today.format(DayFmt)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(650)), color = c.ink)
            if (n >= 2 && !big) Streak(store, today, n, Modifier.padding(top = 2.dp))
        }
        Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button) { onRoute("settings") }.semantics { contentDescription = "Nastavenia" }, contentAlignment = Alignment.Center) {
            Icon24(Res.drawable.ic_person, tint = c.ink2)
        }
    }
    if (n >= 2 && big) Streak(store, today, n, Modifier.padding(bottom = 6.dp))
}

/** Days in a row with learning, up to today (today still open counts from yesterday). */
fun streak(store: Store, today: LocalDate): Int {
    val days = store.activeDays.toSet()
    var d = if (today.toString() in days) today else today.minusDays(1)
    var n = 0
    while (d.toString() in days) { n++; d = d.minusDays(1) }
    return n
}

/**
 * Seven pencil marks for the last seven days, all of one shape (today half-inked until something is learnt) and "6 dní po sebe".
 * In ink, not the accent: the green belongs to the hero card alone (judges, 18. 11., round 5: the old Dnes with one
 * green surface read calmer than marks, underline, icons and the button all in green).
 */
@Composable
private fun Streak(store: Store, today: LocalDate, n: Int, modifier: Modifier = Modifier) {
    val c = T.c
    val days = store.activeDays.toSet()
    val st = MaterialTheme.typography.bodySmall
    val h = with(LocalDensity.current) { st.fontSize.toDp() } * .9f
    Row(modifier.semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(h * 44f / 14f, h)) {
            val k = size.height / 14f
            for (i in 0 until 7) {
                val day = today.minusDays((6 - i).toLong())
                val x = (1.6f + i * 6.4f) * k
                if (day.toString() in days) drawRoundRect(c.ink2, Offset(x, 1f * k), Size(3.4f * k, 12f * k), CornerRadius(1.7f * k))
                // today, nothing learnt yet: the same mark, half-inked (Teo, 18. 11.: the outlined, empty last one was
                // too fussy); a missed day is the faint line colour
                else if (i == 6) drawRoundRect(c.ink2.copy(alpha = .38f), Offset(x, 1f * k), Size(3.4f * k, 12f * k), CornerRadius(1.7f * k))
                else drawRoundRect(c.line, Offset(x, 1f * k), Size(3.4f * k, 12f * k), CornerRadius(1.7f * k))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text("$n ${dayWord(n)} po sebe".replace(' ', '\u00A0'), style = st, color = c.ink2)
    }
}

/** Said once, on the first start (Teo, 17. 11.): what the statistics collect, and where they are switched off. */
const val StatsNoticeText = "Appka posiela štatistiky o učení bez mena. Vypneš ich v Nastaveniach."

/**
 * Said once, quietly: one short sentence and no frame, like the backup line; what is sent and why is in Settings
 * (Teo, 18. 11.: "k tomu súkromiu nepíš tam tak dlho exaktne"). The sentence opens Settings, "Rozumiem" puts it away.
 */
@Composable
fun StatsNotice(onOk: () -> Unit, onSettings: () -> Unit) {
    val c = T.c
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            StatsNoticeText,
            Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClickLabel = "Štatistiky v nastaveniach", onClick = onSettings)
                .heightIn(min = 48.dp).padding(horizontal = 2.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall, color = c.ink2,
        )
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier.clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onOk).heightIn(min = 48.dp).padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) { Text("Rozumiem", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = c.lesson.text) }
    }
}

/** "test zajtra o 18:00" for a course whose test is today or tomorrow, else null. */
fun testSoon(store: Store, k: Course, today: LocalDate): String? {
    // an exam without a day yet has no "zajtra": its window's first day is not the student's day
    if (store.testWindow(k) != null || store.testOver(k)) return null
    val t = store.testStart(k)
    return when (daysTo(t.toLocalDate(), today)) { 0 -> "${k.test.word.nom} dnes${atTime(t)}"; 1 -> "${k.test.word.nom} zajtra${atTime(t)}"; else -> null }
}

/**
 * The courses, "important in front" (Teo, 15. 11., option 3): the open course first, beside it only a course that
 * wants something now, with why ("test zajtra o 18:00"), all the others under "+N", which opens the list. Drawn as
 * light tabs on one rule (the style notes 4), the open one underlined in the accent: lighter than the hero card under them,
 * in the dark as in the light (design review 18. 11.: the dark tile of the open course pulled the eye before the hero). The
 * row looks the same with two courses or six.
 */
@Composable
private fun SubjectTabs(store: Store, today: LocalDate) {
    var sheet by remember { mutableStateOf(false) }
    val open = store.course
    val wants = Courses.filter { it != open }.mapNotNull { k -> testSoon(store, k, today)?.let { k to it } }.take(1)
    // the course that wants something first, then the others in their order
    val others = wants.map { it.first } + Courses.filter { it != open && wants.none { w -> w.first == it } }
    CompositionLocalProvider(LocalPalette provides withCourse(T.c)) { CourseRow(open, wants, others, { sheet = true }) { dev.hamper.tabula.CourseSwitch.ask(it.id) } }
    if (sheet) CourseSheet(store, today, { sheet = false }) { id -> sheet = false; if (id != open.id) dev.hamper.tabula.CourseSwitch.ask(id) }
}

/**
 * The row of [SubjectTabs]: [open], then the [others] by name as long as they fit on the line ("LA1  DML"), and only
 * those that do not under "+N", which opens the list (Teo, 18. 11.: "prečo tam je +1, keď tam môžeš dať DML"). The
 * course that [wants] something comes first. Why a course is in front goes on its own line under the row, "Test z LA1
 * zajtra o 18:00": beside the tab it read as a note of its own, not of LA1 (judges, 18. 11., round 5).
 */
@Composable
fun CourseRow(open: Course, wants: List<Pair<Course, String>>, others: List<Course>, onMore: () -> Unit, onPick: (Course) -> Unit) {
    val c = T.c
    val d = LocalDensity.current
    val gap = if (LocalWindowWidth.current < 340.dp) 16.dp else 24.dp
    Box(Modifier.fillMaxWidth()) {
        // the rule the tabs stand on
        Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(1.dp).background(c.line))
        androidx.compose.ui.layout.SubcomposeLayout(Modifier.fillMaxWidth()) { cs ->
            val loose = cs.copy(minWidth = 0, minHeight = 0)
            val g = gap.roundToPx()
            val first = subcompose("open") { CourseTab(open.short, true, "Predmet ${open.short}, ${open.name}. Zmeniť predmet", "Zmeniť predmet", chevron = true, onClick = onMore) }.map { it.measure(loose) }
            val tabs = others.map { k ->
                val why = wants.firstOrNull { it.first == k }?.second
                subcompose(k.id) { CourseTab(k.short, false, if (why != null) "${k.short}, $why" else "${k.short}, ${k.name}", "Otvoriť ${k.short}") { onPick(k) } }.map { it.measure(loose) }
            }
            fun w(l: List<androidx.compose.ui.layout.Placeable>) = l.maxOfOrNull { it.width } ?: 0
            // as many named as fit; with any left over, room for "+N" too
            var n = others.size
            // a slot is composed once per pass: each "+N" measured once and kept
            val pluses = HashMap<Int, List<androidx.compose.ui.layout.Placeable>>()
            fun plusOf(rest: Int) = pluses.getOrPut(rest) { subcompose("plus$rest") { CourseTab("+$rest", false, "Ďalšie predmety: $rest", "Ďalšie predmety", onClick = onMore) }.map { it.measure(loose) } }
            fun plusW(rest: Int) = if (rest == 0) 0 else g + w(plusOf(rest))
            while (n > 0 && w(first) + tabs.take(n).sumOf { g + w(it) } + plusW(others.size - n) > cs.maxWidth) n--
            val rest = others.size - n
            val plus = if (rest > 0) plusOf(rest) else emptyList()
            // in the courses' own order (LA1, DML), the open one marked where it stands: a switch never swaps their places
            // (the open one first made the two labels cross over in the switch's crossfade; recorded 18. 11.)
            val placed = (listOf(open to first) + others.take(n).zip(tabs.take(n))).sortedBy { (k, _) -> Courses.indexOf(k) }.map { it.second }
            val row = placed + listOfNotNull(plus.ifEmpty { null })
            val h = row.maxOf { l -> l.maxOfOrNull { it.height } ?: 0 }
            layout(cs.maxWidth, h) {
                var x = 0
                for (l in row) { l.forEach { it.place(x, h - it.height) }; x += w(l) + g }
            }
        }
    }
    for ((k, why) in wants) {
        val small = MaterialTheme.typography.bodySmall
        Row(
            Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClickLabel = "Otvoriť ${k.short}") { onPick(k) }.padding(start = 2.dp, top = 6.dp, bottom = 6.dp)
                .semantics(mergeDescendants = true) { contentDescription = "${k.short}, $why" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon24(Res.drawable.ic_alarm, tint = c.ink2, modifier = Modifier.size(with(d) { small.fontSize.toDp() } * 1.2f))
            Spacer(Modifier.width(8.dp))
            // one phrase that names its course, "Test z LA1 zajtra o 18:00", the day and hour set as the fact they are;
            // at a large font it breaks before the day, "Test z LA1" / "zajtra o 18:00", never inside the fact
            Text(
                androidx.compose.ui.text.buildAnnotatedString {
                    append("${k.test.word.nom.replaceFirstChar { it.uppercase() }} z ${k.short} ".nb())
                    pushStyle(androidx.compose.ui.text.SpanStyle(color = c.ink, fontWeight = FontWeight.SemiBold)); append(why.substringAfter(' ').replace(' ', '\u00A0')); pop()
                },
                Modifier.weight(1f), style = small, color = c.ink2,
            )
            // the chevron at the right edge, where every other row that opens something has it, not after the text
            Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.padding(start = 2.dp).size(20.dp))
        }
    }
}

/** One tab of the course row: the code, the open one in ink and underlined. */
@Composable
private fun CourseTab(code: String, active: Boolean, desc: String, label: String, chevron: Boolean = false, onClick: () -> Unit) {
    val c = T.c
    val d = LocalDensity.current
    val st = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(if (active) 750 else 600))
    val small = MaterialTheme.typography.labelMedium
    Column(
        Modifier.width(IntrinsicSize.Max).heightIn(min = 48.dp).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics(mergeDescendants = true) { if (active) selected = true; contentDescription = desc },
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(Modifier.padding(start = 2.dp, end = 2.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(code, style = st, color = if (active) c.lesson.text else c.ink2, softWrap = false)
            if (chevron) Icon24(Res.drawable.ic_chev_down, tint = c.ink2, modifier = Modifier.padding(start = 2.dp).size(with(d) { st.fontSize.toDp() } * 1.1f))
        }
        Spacer(Modifier.height(9.dp))
        // the open course underlined in its own colour (green LA1, indigo DML): which course is open, at a glance
        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.5.dp)).background(if (active) c.lesson.main else Color.Transparent))
    }
}

/** Every course with what waits in it: tap to open it. */
@Composable
private fun CourseSheet(store: Store, today: LocalDate, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val c = T.c
    ModalBottomSheet(onDismiss, sheetState = rememberModalBottomSheetState(true), containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 16.dp).navigationBarsPadding()) {
            Text("Predmety", style = MaterialTheme.typography.headlineSmall, color = c.ink)
            Spacer(Modifier.height(12.dp))
            for (k in Courses) {
                val on = k == store.course
                val t = store.testStart(k)
                val d = daysTo(t.toLocalDate(), today)
                // the lessons for the test the line starts with, counted as Cesta counts them
                val done = k.shown().testLessons.count { it.id in store.lessonsDone }
                val past = store.testOver(k)
                val window = store.testWindow(k)
                val why = testSoon(store, k, today)
                    ?: ((if (past) "${k.test.short} je za tebou" else if (window != null) "${k.test.short} v skúškovom ${windowText(window)}" else "${k.test.short} ${inDays(d)}, ${t.format(DayFmt)}") + " · $done z ${k.shown().testLessons.size} lekcií")
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(16.dp)).background(if (on) c.sand else c.card)
                        .border(1.dp, c.line, RoundedCornerShape(16.dp)).clickable(role = Role.Button) { onPick(k.id) }
                        .heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp).semantics { selected = on },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(k.short, Modifier.width(52.dp), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(750)), color = c.ink)
                    Column(Modifier.weight(1f)) {
                        Text(k.name, style = MaterialTheme.typography.titleMedium, color = c.ink)
                        Text(why, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                    if (on) Icon24(Res.drawable.ic_check, tint = c.lesson.text)
                }
            }
        }
    }
}

/**
 * "Po 8. prednáške" and "Dombek, pondelok 16. 11.": the lectures of the student's parallel the lesson follows that
 * were already held, and the day of the last of them. A lesson opened before its lecture: "K 8. prednáške" and that
 * lecture's day. (Inspector, 18. 11.: "Po 7. a 8. prednáške · Spěvák, streda 11. 11." stood above "8. prednáška …
 * streda 18. 11." on the same screen: the 8th was still to come and the day was the 7th's.)
 */
fun lectureOf(store: Store, course: Course, l: LessonDef, now: dev.hamper.tabula.compat.time.LocalDateTime = dev.hamper.tabula.compat.time.LocalDateTime.now()): Pair<String, String>? = runCatching {
    val p = course.parallel(store.parallel(course))
    // every lecture of the parallel that teaches the lesson's topics: one is "Po 8.", two "Po 7. a 8.", more "Po 6.–8."
    val all = course.lectures(p.id).filter { lec -> course.lessonsFor(lec).any { it.id == l.id } && lec.label.removePrefix("P").toIntOrNull() != null }
    if (all.isEmpty()) return@runCatching null
    val held = all.filter { lectureEnd(p, it) <= now }
    val ls = held.ifEmpty { listOf(all.first()) }
    val ns = ls.map { it.label.removePrefix("P").toInt() }
    val which = when (ns.size) { 1 -> "${ns[0]}."; 2 -> "${ns[0]}. a ${ns[1]}."; else -> "${ns.first()}.–${ns.last()}." }
    val lec = ls.last()
    (if (held.isEmpty()) "K $which prednáške" else "Po $which prednáške") to "${p.teacher}, ${weekday(lec.date)} ${lec.date.format(DayFmt)}"
}.getOrNull()

/**
 * "asi 14 min", from an hour and a half on in hours ("asi 4 h", "asi 1,5 h"): 235 minutes is a sum to work out. One
 * unbreakable phrase: at a large font "asi" ended a line and "15 min" began the next (inspector, 18. 11.).
 */
fun aboutTime(min: Int): String {
    if (min < 90) return "asi\u00A0$min\u00A0min"
    val halves = (min + 15) / 30
    return "asi\u00A0${halves / 2}${if (halves % 2 == 1) ",5" else ""}\u00A0h"
}

/** Minutes still to go in [l]: the rest of it when it is open, else all of it. */
fun minutesToGo(store: Store, l: LessonDef): Int = if (store.inProgress(l)) minutesLeft(l, resumeAt(store, l)) else l.minutes

/** Minutes left in a lesson at step [at] of its steps, at least one. */
fun minutesLeft(l: LessonDef, at: Int): Int = (l.minutes * (l.steps.size - at) + l.steps.size - 1) / l.steps.size.coerceAtLeast(1)

/**
 * How far and how long: "kúsok 2 z 5" and the minutes to the end of the lesson (its pieces never stop it, so a piece's
 * own minutes promised a break that does not come; student, 19. 11.). Not begun: "5 kúskov". A lesson of one piece
 * counts its steps as before.
 */
fun pieceFar(l: LessonDef, at: Int, started: Boolean): Pair<String, String> {
    val ps = pieces(l)
    val time = aboutTime(minutesLeft(l, if (started) at else 0))
    if (ps.size < 2) return (if (started) "krok ${at + 1} z ${l.steps.size}" else "${l.steps.size} ${slovakPlural(l.steps.size, "krok", "kroky", "krokov")}") to time
    return (if (started) "kúsok ${pieceOf(l, at) + 1} z ${ps.size}" else "${ps.size} ${slovakPlural(ps.size, "kúsok", "kúsky", "kúskov")}") to time
}

/**
 * The pieces of a lesson as segments as long as their steps (as the step bar in the lesson draws them): those done
 * full, the one on as far as it got, the rest empty.
 */
@Composable
fun PieceBar(l: LessonDef, at: Int, modifier: Modifier = Modifier) {
    val c = T.c
    val ps = pieces(l)
    val i = pieceOf(l, at)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        ps.forEachIndexed { k, r ->
            val f = when { k < i -> 1f; k > i -> 0f; else -> ((at - r.first).toFloat() / r.count()).coerceIn(0f, 1f) }
            Box(Modifier.weight(r.count().toFloat()).height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.sand)) {
                if (f > 0f) Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(c.lesson.main))
            }
        }
    }
}

/**
 * The hero (locked order): the picture, "Po N. prednáške · lecturer, day", the lesson, what it teaches, "krok X z Y" and
 * "asi N min", the bar, Pokračovať. A lesson not started yet says how many steps and starts.
 */
@Composable
private fun HeroCard(store: Store, next: LessonDef, fresh: Boolean, onRoute: (String) -> Unit) {
    when (SmerNow) {
        Smer.Zosit -> HeroZosit(store, next, fresh, onRoute)
        Smer.Sadzba -> HeroSadzba(store, next, fresh, onRoute)
        Smer.Odvazna, Smer.CPlus -> HeroOdvazna(store, next, fresh, onRoute)
        Smer.Dnes -> HeroToday(store, next, fresh, onRoute)
    }
}

/** What every hero says: the kicker (lecture, whose, when), how far and how long, the button's words. */
private class HeroWords(store: Store, next: LessonDef, fresh: Boolean, big: Boolean) {
    val started = store.inProgress(next)
    val at = resumeAt(store, next)
    val kick = lectureOf(store, store.course, next).let { lec -> listOfNotNull(lec?.first ?: if (fresh) "Začni tu" else null, lec?.second?.takeIf { !big }) }
    val far = pieceFar(next, at, started).first
    val time = pieceFar(next, at, started).second
    val go = if (started) "Pokračovať" else "Začať lekciu"
    val progress = at.toFloat() / next.steps.size
}

/**
 * A: a slip of squared paper with a red margin, the lesson's sketch large and Pytagoras beside the kicker, saying
 * what is on today; blue ink.
 */
@Composable
private fun HeroZosit(store: Store, next: LessonDef, fresh: Boolean, onRoute: (String) -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    val w = HeroWords(store, next, fresh, big)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.art.paper).squaredPaper(c.art.grid, margin = c.art.margin)
            .border(1.dp, c.line, RoundedCornerShape(10.dp)).padding(start = 44.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pytagoras(Mood.Pleased, size = 40.dp)
            Spacer(Modifier.width(10.dp))
            Text(if (w.started) "Pokračujeme tam, kde si skončil" else "Dnes na rade", style = MaterialTheme.typography.labelLarge, color = c.lesson.text)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                if (w.kick.isNotEmpty()) { Parts(w.kick, style = MaterialTheme.typography.labelMedium, color = c.ink2); Spacer(Modifier.height(6.dp)) }
                // a narrow phone or a larger font a size down, so no word of the name breaks ("Kongru-encie" at 320 dp, 130 %)
                WholeWordsText(next.name.nb(), listOfNotNull(MaterialTheme.typography.displaySmall.takeIf { LocalWindowWidth.current >= 380.dp && LocalDensity.current.fontScale <= 1.15f }, MaterialTheme.typography.headlineSmall, MaterialTheme.typography.titleLarge), Modifier.semantics { heading() }, color = c.ink)
            }
            // beside the name only where the name keeps its words whole (at 320 dp and 130 % it broke "Kongrue-ncie")
            if (!big && !(LocalWindowWidth.current < 380.dp && LocalDensity.current.fontScale > 1.15f)) { Spacer(Modifier.width(8.dp)); LessonSketch(motifFor("lesson:${next.id}"), size = if (LocalWindowWidth.current < 340.dp) 96.dp else 116.dp) }
        }
        Spacer(Modifier.height(12.dp))
        Parts(listOf(w.far, w.time), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        if (w.started) { Spacer(Modifier.height(8.dp)); ProgressBar(w.progress) }
        Spacer(Modifier.height(16.dp))
        Cta(w.go, { onRoute("lesson:${next.id}") })
    }
}

/**
 * B: no card, the page is the card. A rule, the lesson's name set large in the skriptá's type, and its key statement
 * as the picture; the progress as a hairline; one ultramarine.
 */
@Composable
private fun HeroSadzba(store: Store, next: LessonDef, fresh: Boolean, onRoute: (String) -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    val w = HeroWords(store, next, fresh, big)
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(c.ink))
        Spacer(Modifier.height(10.dp))
        Parts(listOf(if (w.started) "Rozrobená" else "Na rade") + w.kick, style = MaterialTheme.typography.labelMedium, color = c.lesson.text)
        Spacer(Modifier.height(8.dp))
        WholeWordsText(next.name.nb(), listOf(MaterialTheme.typography.displaySmall, MaterialTheme.typography.headlineSmall, MaterialTheme.typography.titleLarge), Modifier.semantics { heading() }, color = c.ink)
        keyFormula(next)?.let { f ->
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().background(c.card).border(1.dp, c.line).padding(horizontal = 12.dp, vertical = 14.dp)) {
                Rich("$$$f$$", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = c.ink)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Parts(listOf(w.far, w.time), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(c.line)) { Box(Modifier.fillMaxWidth(w.progress.coerceIn(0f, 1f)).fillMaxHeight().background(c.lesson.main)) }
        Spacer(Modifier.height(16.dp))
        Cta(w.go, { onRoute("lesson:${next.id}") })
    }
}

/**
 * C: one block in full green, the one thing on Dnes: the name large in white, the sketch on a light tile, a white
 * button; everything under it stays quiet.
 */
@Composable
private fun HeroOdvazna(store: Store, next: LessonDef, fresh: Boolean, onRoute: (String) -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    val w = HeroWords(store, next, fresh, big)
    val fill = boldFill(c)
    val warm = SmerNow == Smer.CPlus
    CompositionLocalProvider(LocalPalette provides solidOn(c, fill)) {
        val s = T.c
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(fill).padding(start = 22.dp, end = 22.dp, top = if (warm) 18.dp else 22.dp, bottom = if (warm) 18.dp else 22.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    // C+: Pytagoras in measure beside the label, the one character on Dnes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (w.started) "Rozrobená" else "Na rade", style = MaterialTheme.typography.labelLarge, color = s.ink2)
                    }
                    Spacer(Modifier.height(6.dp))
                    // the largest size at which the name's longest word keeps one line beside the sketch: measured, not
                    // guessed from the width ("Kongruen/cie" at 360 dp beside the 124 dp sketch, 19. 11.)
                    val sizes = if (warm || LocalWindowWidth.current < 380.dp || LocalDensity.current.fontScale > 1.15f) listOf(MaterialTheme.typography.headlineSmall, MaterialTheme.typography.headlineMedium, MaterialTheme.typography.titleLarge)
                        else listOf(MaterialTheme.typography.headlineMedium, MaterialTheme.typography.titleLarge)
                    WholeWordsText(next.name.nb(), sizes, Modifier.semantics { heading() }, color = s.ink)
                }
                if (!big && !(LocalWindowWidth.current < 380.dp && LocalDensity.current.fontScale > 1.15f)) {
                    Spacer(Modifier.width(10.dp))
                    // C+: the sketch large on a scrap of squared paper (the grid carries the drawing, nowhere else)
                    Box {
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(c.art.paper).then(if (warm) Modifier.squaredPaper(c.art.grid, 12.dp) else Modifier).padding(4.dp)) { CompositionLocalProvider(LocalPalette provides c) { LessonSketch(motifFor("lesson:${next.id}"), size = if (warm) 124.dp else 88.dp) } }
                    }
                }
            }
            if (w.kick.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Parts(w.kick, style = MaterialTheme.typography.bodySmall, color = s.ink2) }
            Spacer(Modifier.height(16.dp))
            Parts(listOf(w.far.replaceFirstChar { it.uppercase() }, w.time), style = MaterialTheme.typography.titleMedium, color = s.ink)
            if (w.started) { Spacer(Modifier.height(8.dp)); PieceBar(next, w.at) }
            Spacer(Modifier.height(18.dp))
            Cta(w.go, { onRoute("lesson:${next.id}") })
        }
    }
}

/** Today's hero (1.5.5). */
@Composable
private fun HeroToday(store: Store, next: LessonDef, fresh: Boolean, onRoute: (String) -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    val at = resumeAt(store, next)
    val started = store.inProgress(next)
    Column(Modifier.fillMaxWidth().card().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp)) {
        // after which lecture, whose and when (at a large font only the lecture), then the lesson's name; the sketch of
        // its topic beside them, so the card stays low and Pokračovať high (judges, 18. 11.: the band above the card
        // cost the height the button needs)
        HeroHead(motifFor("lesson:${next.id}"), done = false) {
            val lec = lectureOf(store, store.course, next)
            val kick = listOfNotNull(lec?.first ?: if (fresh) "Začni tu" else null, lec?.second?.takeIf { !big })
            if (kick.isNotEmpty()) { Parts(kick, style = MaterialTheme.typography.labelMedium, color = c.ink2); Spacer(Modifier.height(4.dp)) }
            WholeWordsText(next.name.nb(), listOf(MaterialTheme.typography.headlineMedium, MaterialTheme.typography.titleLarge), Modifier.semantics { heading() }, color = c.ink)
        }
        Spacer(Modifier.height(12.dp))
        // how far and how long; at a break the dot between them goes, never "krok 6 z 25 ·" / "asi 14 min"
        val (far, time) = pieceFar(next, at, started)
        Parts(listOf(far, time), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        if (started) { Spacer(Modifier.height(8.dp)); PieceBar(next, at) }
        Spacer(Modifier.height(16.dp))
        Cta(if (started) "Pokračovať" else "Začať lekciu", { onRoute("lesson:${next.id}") })
    }
}

/** The hero's head: [text] on the left, the sketch of [motif] on the right (none from 150 %, the style notes 2). */
@Composable
private fun HeroHead(motif: Motif, done: Boolean, text: @Composable ColumnScope.() -> Unit) {
    if (LocalDensity.current.fontScale >= 1.5f) { Column(content = text); return }
    // narrower screens a smaller scrap, so the name keeps room for "Kongruencie" on one line
    val sketch = if (LocalWindowWidth.current < 340.dp) 84.dp else 96.dp
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(top = 2.dp), content = text)
        Spacer(Modifier.width(14.dp))
        LessonSketch(motif, size = sketch, done = done)
    }
}

/** One continuous bar, 6 dp: the sunk track and the accent. */
@Composable
fun ProgressBar(f: Float, modifier: Modifier = Modifier) {
    val c = T.c
    Box(modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.sand)) {
        if (f > 0f) Box(Modifier.fillMaxWidth(f.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(c.lesson.main))
    }
}

/** A lesson finished today and nothing open: its topic's picture, the next lesson offered quietly. */
@Composable
private fun DoneTodayCard(store: Store, done: LessonDef, next: LessonDef, onRoute: (String) -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    Column(Modifier.fillMaxWidth().card().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp)) {
        // the same sketch, now finished: the walk reaches its goal
        HeroHead(motifFor("lesson:${done.id}"), done = true) {
            Text("Dnes hotovo", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = c.lesson.text)
            Spacer(Modifier.height(4.dp))
            // every lesson name's words whole beside the sketch ("Kongruenc/ie" at 360 dp, 19. 11.)
            WholeWordsText("${done.name} máš za\u00A0sebou.".nb(), listOf(MaterialTheme.typography.headlineMedium, MaterialTheme.typography.titleLarge), Modifier.semantics { heading() }, color = c.ink)
        }
        Spacer(Modifier.height(8.dp))
        // the offer of the next lesson in words, the button only names it: at a large font a button holding the name
        // and the minutes grew to three lines and outweighed the heading (inspector, 18. 11.)
        Parts(listOfNotNull("Ďalšia lekcia počká na zajtra. Ak chceš, môžeš ísť ďalej aj dnes.".takeIf { !big }, "${next.name}: ${aboutTime(minutesToGo(store, next))}".takeIf { big }), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        Spacer(Modifier.height(16.dp))
        Second(if (big) "Ďalšia lekcia" else "${next.name}, ${aboutTime(minutesToGo(store, next))}", { onRoute("lesson:${next.id}") })
    }
}

/** Every lesson of the course done: the test on paper is next. */
@Composable
private fun AllDoneCard(store: Store, onRoute: (String) -> Unit) {
    val c = T.c
    Column(Modifier.fillMaxWidth().card().padding(20.dp)) {
        HeroHead(Motif.Paper, done = false) {
            Text("Všetkých ${store.course.shown().lessons.size} lekcií máš", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium, color = c.ink)
        }
        Spacer(Modifier.height(8.dp))
        Text("Teraz celé ${if (store.course.test.word == PisomkaWord) "písomky" else "testy"} na papier a opakovanie chýb.".nb(), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
        Spacer(Modifier.height(16.dp))
        Cta("Nácvik ${store.course.test.word.gen}", { onRoute("papers") })
    }
}

/** "Test zajtra o 18:00": above the hero in the window before the test. */
fun preLabel(store: Store, today: LocalDate): String {
    val t = store.testStart()
    val day = when (daysTo(t.toLocalDate(), today)) { 0 -> "dnes"; 1 -> "zajtra"; else -> "pozajtra" }
    return "${store.course.test.word.nom.replaceFirstChar { it.uppercase() }} $day${atTime(t)}"
}

/** A thing for today besides the hero, with the reason it is here today; [know]: a lesson "Viem to" marks done. */
data class MoreTask(val title: String, val why: String, val route: String, val motif: Motif, val minutes: Int?, val know: String? = null)

/**
 * The older unfinished lessons (todayOf): one quiet row each, "Rozrobená, ostáva 5 krokov", opened where it was left,
 * or marked done with "Viem to". A lesson walked to its end with steps jumped over says how many it jumped.
 */
fun unfinishedRows(store: Store, ls: List<LessonDef>): List<MoreTask> = ls.map { l ->
    val n = store.stepsLeft(l)
    val steps = "$n\u00A0${slovakPlural(n, "krok", "kroky", "krokov")}"
    val why = if (l.id in store.lessonGaps) "Preskočené ${steps}" else "Rozrobená, ${slovakPlural(n, "ostáva", "ostávajú", "ostáva")} $steps"
    MoreTask(l.name, why, "lesson:${l.id}", motifFor("lesson:${l.id}"), null, know = l.id)
}

/**
 * Two or three things for today besides today's review (reviewRow), each with its reason from the record: a
 * problem of a paper that went wrong and what the plan deals for today after the hero lesson.
 */
fun todayMore(store: Store, today: LocalDate, next: LessonDef?): List<MoreTask> {
    val course = store.course
    val out = ArrayList<MoreTask>()
    val mine = store.mistakes.filter { courseOfRoute(it.key) == course }
    // a problem of a paper that went wrong: it needs paper and a clock, so the review round leaves it here
    val paperMiss = mine.filter { it.key.substringBefore(':') == "paper" }.maxByOrNull { it.at }
    if (paperMiss != null) out += MoreTask(mathSafe(paperMiss.title), "${dayPhrase(LocalDate.parse(dayOf(paperMiss.at)), today)} ${paperMiss.detail.replaceFirstChar { it.lowercase() }}", paperMiss.route, Motif.Paper, null)
    // what the plan deals for today after the hero lesson: a second lesson, a problem from a past paper on paper
    val plan = buildPlan(today, store).firstOrNull()?.tasks.orEmpty().filter { it.route != "lesson:${next?.id}" && it.route != "review" && it.route != "none" }
    val left = course.shown().testLessons.count { it.id !in store.lessonsDone }
    val days = daysTo(store.testStart(course).toLocalDate(), today)
    for (t in plan) {
        if (out.size >= 3) break
        when {
            // no "na 46 dní" for an exam without a day: the count of days would be to the window's start
            t.route.startsWith("lesson:") -> out += MoreTask(t.title, "Do ${genitive(course.test)} ${slovakPlural(left, "ostáva", "ostávajú", "ostáva")} $left ${slovakPlural(left, "lekcia", "lekcie", "lekcií")}" + (if (store.testWindow(course) == null) " na $days ${dayWord(days)}" else ""), t.route, motifFor(t.route), t.minutes)
            t.route.startsWith("paper:") || t.route == "papers" -> {
                val paper = t.route.split(':').getOrNull(1)?.let { id -> course.papers.firstOrNull { it.id == id } }
                out += MoreTask(t.title, paper?.let { "${it.name}, s časom ako na ${course.test.word.loc}" } ?: "S časom ako na ${course.test.word.loc}", t.route, Motif.Paper, t.minutes)
            }
        }
    }
    return out.take(3)
}

/** "§1 ... §" cut in the middle of a formula would show raw TeX: cut before it. */
private fun mathSafe(s: String): String = if (s.count { it == '§' } % 2 == 1) s.substringBeforeLast('§').trimEnd() + "…" else s

/** "Dnes", "Včera", "V piatok" (the last week), else "12. 11." */
fun dayPhrase(d: LocalDate, today: LocalDate): String = when (val n = daysTo(today, d)) {
    0 -> "Dnes"; 1 -> "Včera"
    in 2..6 -> when (d.dayOfWeek.value) { 1 -> "V pondelok"; 2 -> "V utorok"; 3 -> "V stredu"; 4 -> "Vo štvrtok"; 5 -> "V piatok"; 6 -> "V sobotu"; else -> "V nedeľu" }
    else -> d.format(DayFmt)
}

/** "2. test" → "2. testu", "2. písomka" → "2. písomky", "Skúška" → "skúšky" (after "do"). */
fun genitive(e: Exam): String {
    val s = e.short
    val g = when {
        s.endsWith("písomka") -> s.dropLast(1) + "y"
        s.endsWith("Skúška") || s.endsWith("skúška") -> s.dropLast(1) + "y"
        s.startsWith("Opravný test") -> "opravného testu"
        s.endsWith("test") -> s + "u"
        else -> s
    }
    return g.replaceFirstChar { if (it.isUpperCase()) it.lowercaseChar() else it }
}

/**
 * "Opakovanie na dnes" (Teo, 19. 11.: "je toho too much"): a row of "Dnes ešte" like the others, how many things and
 * how many of them mistakes, its minutes; why each thing is back is said in the round, on the thing itself. Null when
 * nothing is left for today.
 */
fun reviewRow(d: ReviewDose): MoreTask? {
    val n = d.items.size
    if (n == 0) return null
    val wrong = d.items.count { it.back == Back.Wrong }
    val why = "$n\u00A0${slovakPlural(n, "vec", "veci", "vecí")}" + (if (wrong in 1 until n) ", z\u00A0toho $wrong\u00A0${slovakPlural(wrong, "chyba", "chyby", "chýb")}" else if (wrong == n) ", ${if (n == 1) "chyba" else "všetko chyby"}" else "")
    return MoreTask("Opakovanie na dnes", why, "review", Motif.Review, d.minutes)
}

/** "včera zle", "v piatok zle", "zle" when the record has no date. */
private fun wrongWhen(d: DoseItem, today: LocalDate): String =
    d.wrongOn?.let { on -> "${dayPhrase(on, today).replaceFirstChar { ch -> ch.lowercase() }} zle" } ?: "zle"

/**
 * Why a thing of today's dose is back, one short line above it in the round: only a mistake and when it was made; the
 * rest needs no reason (Teo, 19. 11.: less to read).
 */
fun reviewReason(d: DoseItem, today: LocalDate): StepReason? =
    if (d.back != Back.Wrong) null else StepReason("Vracia sa: **${if (d.wrongOn != null) wrongWhen(d, today) else "predtým zle"}**", true)

@Composable
private fun MoreRow(t: MoreTask, first: Boolean, onKnow: (() -> Unit)? = null, onClick: () -> Unit) {
    val c = T.c
    // rows on the page, a hairline between them: a short list, not three more cards under the hero. No sketch per row
    // (judges, 18. 11., round 5: the small ones were weaker than the hero's and added green): the hero's picture is
    // the one picture on Dnes, the rows are words and why
    if (!first) Box(Modifier.fillMaxWidth().padding(start = 2.dp).height(1.dp).background(c.line))
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClick = onClick).heightIn(min = 64.dp).padding(start = 2.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val big = LocalDensity.current.fontScale >= 1.5f
        Column(Modifier.weight(1f)) {
            Rich(t.title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Spacer(Modifier.height(2.dp))
            // a reason with math through Rich, a plain one through Text, whose Slovak setting keeps "9 úloh" together
            if ('§' in t.why || '$' in t.why) Rich(t.why, style = MaterialTheme.typography.bodySmall, color = c.ink2)
            else Text(t.why, style = MaterialTheme.typography.bodySmall, color = c.ink2)
            // at a large font a column beside it would squeeze the reason to a word a line: the minutes go under it
            if (big) t.minutes?.let { Text("$it\u00A0min", Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = c.ink2) }
        }
        // the minutes are a value of the row, beside where it opens, not the end of its reason ("… bez pomoci / 10 min")
        Spacer(Modifier.width(12.dp))
        if (!big) t.minutes?.let { Text("$it\u00A0min", style = MaterialTheme.typography.bodySmall, color = c.ink2, softWrap = false) }
        // the second way out of an unfinished lesson: a quiet word beside it, not a button that competes with the row
        if (onKnow != null) Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClickLabel = "Označiť lekciu ako hotovú", onClick = onKnow).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text("Viem to", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = c.lesson.text, softWrap = false)
        }
        Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.padding(start = 2.dp).size(20.dp))
    }
}

/**
 * A new student: nothing to review yet; what will come here, in words like the rows it stands for (the hero's sketch is
 * the one picture on Dnes)
 */
@Composable
private fun EmptyMore(word: ExamWord) {
    val c = T.c
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, start = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Začni prvou lekciou hore. Potom sa sem budú vracať tvoje chyby a príklady ${zo(word.genPl)} na papier.".nb(), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink2)
    }
}

/**
 * The test, one block: when it is, what is still missing for it and since when the next part opens, the mock on
 * paper and the whole plan.
 */
/**
 * What is left of the test card (18. 11.): one row, the test and when, opening the tab "Test" where its problems and
 * the one thing to do now are. The hero above stays as todayChoice picks it.
 */
@Composable
private fun TestRow(store: Store, today: LocalDate, onRoute: (String) -> Unit) {
    val c = T.c
    val course = store.course
    val start = store.testStart(course)
    val over = store.testOver(course)
    val window = store.testWindow(course) != null
    val d = daysTo(start.toLocalDate(), today)
    // some terms over, none picked: the tab asks whether it was written, so the row does not count down to the next term
    val asked = sittingQuestion(store, course) != null
    val at = when { asked -> "už si ${if (course.test.word == TestWord) "ho" else "ju"} písal?"; over -> "je za tebou"; window -> testWhen(store, course); d == 0 -> "dnes${atTime(start)}"; d == 1 -> "zajtra${atTime(start)}"; else -> testWhen(store, course) }
    // two days before, the tab's one thing is the whole test: said here too, so Dnes and the tab do not disagree
    val now = if (!asked && !over && !window && d <= 2) remember(course.id, course.test.id, store.version, today) { testNow(store, testProblems(course), today) } else null
    val whenText = if (now != null && (now.route.startsWith("mock:") || now.route == "papers")) "$at · ${now.title.replaceFirstChar { it.lowercase() }}" else at
    Row(
        Modifier.fillMaxWidth().card().clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button) { onRoute("nacvik") }.heightIn(min = 56.dp).padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("${course.test.short} z ${course.short}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = c.ink)
            Text(whenText, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Icon24(Res.drawable.ic_chevron, tint = c.ink2, modifier = Modifier.size(20.dp))
    }
}

/** Where the progress is: a quiet line, no frame, no logo; Settings say more. */
@Composable
private fun BackupLine(web: Boolean, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClickLabel = "Záloha postupu v nastaveniach", onClick = onClick).heightIn(min = 48.dp).padding(horizontal = 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon24(Res.drawable.ic_phone, tint = c.ink2, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(if (web) "Postup je len v tomto prehliadači." else "Postup je len v tomto telefóne.", Modifier.weight(1f, fill = false), style = MaterialTheme.typography.bodySmall, color = c.ink2)
        Spacer(Modifier.width(6.dp))
        Text("Záloha", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = c.ink)
        Icon24(Res.drawable.ic_chevron, tint = c.ink2, modifier = Modifier.size(18.dp))
    }
}

/** The window's width (the Dnes margins and tabs tighten under 340 dp). */
val LocalWindowWidth: androidx.compose.runtime.ProvidableCompositionLocal<Dp> = androidx.compose.runtime.compositionLocalOf { 400.dp }


/* ---- Cesta -------------------------------------------------------------------------------------------------------- */

@Composable
fun PathScreen(store: Store, bottom: Dp, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    var sheet by remember { mutableStateOf<LessonDef?>(null) }
    val course = store.course
    val shown = course.shown()
    val Lessons = shown.lessons
    val current = nextLesson(store)
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = bottom + 40.dp)) {
        Label("${course.pathLabel}, na ${course.test.acc}")
        Spacer(Modifier.height(2.dp))
        Text(course.chapter, style = MaterialTheme.typography.headlineLarge, color = c.ink)
        WhereLine(store, course, onRoute)
        Spacer(Modifier.height(10.dp))
        val testDone = shown.testLessons.count { it.id in store.lessonsDone }
        val testN = shown.testLessons.size
        Text("Na ${course.test.acc}: $testDone z $testN ${if (testN == 1) "lekcie" else "lekcií"} hotových", style = MaterialTheme.typography.labelMedium, color = c.ink2)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (l in shown.testLessons) Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(if (l.id in store.lessonsDone) c.lesson.main else c.sand))
        }
        Spacer(Modifier.height(16.dp))
        val now = dev.hamper.tabula.compat.time.LocalDateTime.now()
        // One focus, as on a plan you study by: a part all done folds into one line, the part with the lesson on turn
        // stays open with that lesson as its one card, the rest of it quiet rows (judges, 18. 11.: four done lessons
        // took the screen and the next step sat cut off at the bar). A tap opens a folded part again.
        var opened by rememberSaveable { mutableStateOf(listOf<String>()) }
        val items = pathItems(course, shown)
        val partOf = HashMap<Int, String>()
        run { var p = ""; items.forEachIndexed { i, it -> if (it is PathItem.Part) p = it.name; partOf[i] = p } }
        val lessonsIn = items.indices.filter { items[it] is PathItem.Lesson }.groupBy({ partOf.getValue(it) }) { (items[it] as PathItem.Lesson).lesson }
        fun folded(part: String) = part !in opened && lessonsIn[part].orEmpty().let { ls -> ls.isNotEmpty() && ls.all { it.id in store.lessonsDone } && current !in ls }
        items.forEachIndexed { i, item ->
            val part = partOf.getValue(i)
            if (item !is PathItem.Part && item !is PathItem.Milestone && folded(part)) return@forEachIndexed
            when (item) {
                is PathItem.Part -> if (folded(item.name)) DonePart(item.name, lessonsIn[item.name].orEmpty().size) { opened = opened + item.name }
                    else Text(item.name, style = MaterialTheme.typography.titleMedium, color = c.ink2, modifier = Modifier.padding(top = if (i == 0) 2.dp else 10.dp, bottom = 4.dp).semantics { heading() })
                is PathItem.Lesson -> { val l = item.lesson
                    when {
                        l == current -> NowCard(store, l, { sheet = l }) { onRoute("lesson:${l.id}") }
                        else -> LessonRow(l, l.id in store.lessonsDone, if (store.inProgress(l)) store.runAt(l) else 0) { sheet = l }
                    }
                }
                is PathItem.Check -> { val cp = item.checkpoint
                    Row(
                        Modifier.fillMaxWidth().padding(start = 68.dp, bottom = 8.dp).clip(RoundedCornerShape(16.dp)).background(c.sand).clickable(role = Role.Button) { onRoute("paper:${cp.paper}:${cp.n}") }.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon24(Res.drawable.ic_write, tint = c.ink2, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("${cp.title}, ${cp.pts}\u00A0b", style = MaterialTheme.typography.titleSmall, color = c.ink2)
                    }
                }
                is PathItem.Milestone -> {
                    val e = item.exam
                    val current = e == course.test
                    val start = examStart(e, store.terms[e.id], now)
                    val over = (store.terms[e.id]?.plusMinutes((e.minutes ?: 90).toLong()) ?: examOver(e)) < now
                    val sub = when {
                        current -> shown.mockShape.let { (n, pts, min) -> "Nácvik: $n ${slovakPlural(n, "príklad", "príklady", "príkladov")}, $pts bodov, $min minút" }
                        over -> "Za tebou"
                        else -> "Ďalej, ${e.points} bodov" + (start?.let { s -> ", od ${s.format(DayFmt)} ${s.year}" } ?: "")
                    }
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button) { onRoute(if (current) "papers" else "where") }.heightIn(min = 64.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                            val ring = if (current) c.lesson.main else c.line
                            Box(Modifier.size(28.dp).clip(CircleShape).border(3.dp, ring, CircleShape), contentAlignment = Alignment.Center) { Box(Modifier.size(8.dp).clip(CircleShape).background(ring)) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(e.short.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, color = if (over) c.ink2 else c.ink)
                            Text(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Second("Definície, vety a klipy", { onRoute("library") })
    }
    sheet?.let { l ->
        ModalBottomSheet({ sheet = null }, sheetState = rememberModalBottomSheetState(true), containerColor = c.card, contentColor = c.ink, sheetMaxWidth = AppWidth) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp).navigationBarsPadding()) {
                val done = l.id in store.lessonsDone
                val at = store.runAt(l)
                val started = store.inProgress(l)
                Label(when { done -> "Hotová lekcia"; started -> "Rozrobená, ${pieceFar(l, at, true).first}"; l == current -> "Na rade"; else -> "Čaká ťa" })
                Spacer(Modifier.height(2.dp))
                Text(l.name, style = MaterialTheme.typography.headlineMedium, color = c.ink)
                Spacer(Modifier.height(8.dp))
                Rich(l.learn, style = MaterialTheme.typography.bodyLarge, color = c.ink2)
                Spacer(Modifier.height(4.dp))
                val clips = l.steps.count { it is Step.Clip }
                Text("${l.steps.size} ${slovakPlural(l.steps.size, "krok", "kroky", "krokov")}, $clips ${slovakPlural(clips, "klip", "klipy", "klipov")} z prednášky, asi ${l.minutes} min", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                Spacer(Modifier.height(16.dp))
                if (!done && started) ButtonRow {
                    Second("Od začiatku", { sheet = null; onRoute("lesson:${l.id}:0") })
                    Cta("Pokračovať", { sheet = null; onRoute("lesson:${l.id}") }, hue = c.lesson)
                } else Cta(if (done) "Zopakovať" else "Začať lekciu", { sheet = null; onRoute("lesson:${l.id}:0") }, hue = c.lesson)
            }
        }
    }
}

@Composable
private fun LessonRow(l: LessonDef, done: Boolean, at: Int, onClick: () -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClickLabel = "Otvoriť lekciu", onClick = onClick)
            .heightIn(min = if (done) 48.dp else 72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // every lesson keeps its picture at full strength (Teo, 18. 11.: "nemôžem vidieť obrázky aj na completed? ... takú
        // malú opacity, to je dobré?"); a done one says so with a check on the picture's corner and its quieter name, one
        // in progress with its step in the lesson's colour
        when {
            big && done -> Box(Modifier.width(28.dp), contentAlignment = Alignment.Center) { DoneMark() }
            big -> Spacer(Modifier.width(4.dp))
            else -> Box {
                LessonThumb(l)
                if (done) Box(Modifier.align(Alignment.BottomEnd).offset(x = 4.dp).clip(CircleShape).background(c.bg).padding(2.dp)) { DoneMark() }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(l.name, style = MaterialTheme.typography.titleMedium, color = if (done) c.ink2 else c.ink)
            if (!done) Text(if (at > 0) "Rozrobená, ${pieceFar(l, at, true).first}" else "Asi ${l.minutes} min", style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (at > 0) FontWeight.SemiBold else null), color = if (at > 0) c.lesson.text else c.ink2)
        }
        if (!done) Icon24(Res.drawable.ic_chevron, tint = c.ink3)
        Spacer(Modifier.width(4.dp))
    }
}

/** A part whose every lesson is done, folded into one line; a tap opens it. */
@Composable
private fun DonePart(name: String, n: Int, onOpen: () -> Unit) {
    val c = T.c
    val big = LocalDensity.current.fontScale >= 1.5f
    Row(
        Modifier.fillMaxWidth().padding(bottom = 4.dp).clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClickLabel = "Rozbaliť lekcie", onClick = onOpen)
            .semantics { stateDescription = "zbalené" }.heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(if (big) 28.dp else 56.dp), contentAlignment = Alignment.Center) { DoneMark() }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).semantics { heading() }) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text("$n ${slovakPlural(n, "lekcia hotová", "lekcie hotové", "lekcií hotových")}", style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.graphicsLayer { rotationZ = 90f })
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun DoneMark() {
    val c = T.c
    // on the light page the pale disc alone all but vanished (inspector, 18. 11.): it gets the tick's green as a ring
    val ring = if (c.dark) Modifier else Modifier.border(1.5.dp, c.good, CircleShape)
    Box(Modifier.size(24.dp).clip(CircleShape).background(c.goodTint).then(ring), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(16.dp)) }
}

/**
 * The lesson on turn, the path's one card: where it stands, its name, how far, and the way in. The rest of the card
 * opens the lesson's sheet (start over, what it covers).
 */
@Composable
private fun NowCard(store: Store, l: LessonDef, onInfo: () -> Unit, onGo: () -> Unit) {
    val c = T.c
    val started = store.inProgress(l)
    val at = store.runAt(l)
    Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 8.dp).card().clip(RoundedCornerShape(24.dp))) {
        val (far, time) = pieceFar(l, at, started)
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "O lekcii", onClick = onInfo).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // the words lead; the lesson's sketch beside them at the size Dnes shows it (a 40 dp tile said nothing of the
            // lesson, judges 18. 11.; none at all hid which lesson it is at a glance, Teo 18. 11.)
            Column(Modifier.weight(1f)) {
                Text(if (started) "Rozrobená" else "Na rade", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = c.lesson.text)
                Text(l.name, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = c.ink)
                Text("${far.replace(' ', '\u00A0')}\u00A0· $time", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            }
            if (LocalDensity.current.fontScale < 1.5f) { Spacer(Modifier.width(12.dp)); LessonSketch(motifFor("lesson:${l.id}"), size = 72.dp) }
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
            if (started) { PieceBar(l, at); Spacer(Modifier.height(14.dp)) }
            Cta(if (started) "Pokračovať" else "Začať lekciu", onGo)
        }
    }
}

/* ---- Nácvik ------------------------------------------------------------------------------------------------------- */

/**
 * Every exercise of the course, one level below the tab "Test" (what the tab Nácvik listed until 18. 11.): the whole
 * past papers, the page about the test, the problem types with new numbers, the drills, definitions, proofs, review.
 */
@Composable
fun PracticeScreen(store: Store, onClose: () -> Unit, onRoute: (String) -> Unit) {
    val c = T.c
    @Suppress("UNUSED_VARIABLE") val v = store.version
    val last = store.mocks.lastOrNull { courseOfRoute("paper:${it.paper}") == store.course }
    StepFrame(0, 0, onClose, closeLabel = "Späť") {
        H1("Všetky cvičenia")
        Spacer(Modifier.height(8.dp))
        val w = store.course.test.word
        Drill(Motif.Paper, if (w == PisomkaWord) "Celé písomky na čas" else "Celé testy na čas", if (last != null) "Naposledy ${last.total} z ${last.max} b" else "Skutočné zadania z minulých rokov aj nové čísla", false) { onRoute("papers") }
        if (last != null) Drill(Motif.Review, "Posledný nácvik ${w.gen}", "${last.total} z ${last.max} b, po častiach", false) { onRoute("result") }
        testPage(store.course.test.id)?.let { page -> Drill(Motif.Card, page.title, pageBlurb(page, store.course.test.word, types = false), false) { onRoute("temy") } }
        Spacer(Modifier.height(16.dp))
        Text("Príklady ako na ${store.course.test.word.loc}", style = MaterialTheme.typography.titleLarge, color = c.ink)
        Text("Vždy nové čísla, výsledok spočítaný presne.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
        Spacer(Modifier.height(6.dp))
        val course = store.course
        val shown = course.shown()
        shown.templates.forEachIndexed { i, t ->
            val key = "gen:" + TemplateOf[t.gen]
            Drill(motifFor("gen:${t.gen}"), t.title, t.sub, store.mastered(key)) { onRoute("template:${t.gen}") }
        }
        Spacer(Modifier.height(16.dp))
        Text("Cvičenia", style = MaterialTheme.typography.titleLarge, color = c.ink)
        Spacer(Modifier.height(6.dp))
        course.drills.forEach { d -> Drill(motifFor(d.route), m(d.title), d.sub, false) { onRoute(d.route) } }
        val Concepts = shown.concepts; val Proofs = shown.proofs
        Drill(Motif.Card, "Definície z pamäti", "${Concepts.size} ${slovakPlural(Concepts.size, "definícia", "definície", "definícií")}, tri vrstvy a písanie z pamäti", false) { onRoute("library") }
        Drill(Motif.Proof, "Dôkazy po riadkoch", "${Proofs.size} ${slovakPlural(Proofs.size, "dôkaz", "dôkazy", "dôkazov")}, ${slovakPlural(Proofs.size, "aj ten", "aj tie", "aj tie")} z minulých ${course.test.word.genPl}", false) { onRoute("library:proofs") }
        Drill(Motif.Review, "Zmiešané opakovanie", "Chyby a to, čo išlo s pomocou", false) { onRoute("mix") }
        val clips = shown.clips
        if (clips.isNotEmpty()) {
            val lects = clips.map { it.lecture.substringBefore('.').toIntOrNull() ?: 0 }.filter { it > 0 }
            Drill(Motif.Clip, "Klipy z prednášok", "${clips.size} ${slovakPlural(clips.size, "krátke miesto", "krátke miesta", "krátkych miest")} z ${lects.min()}. až ${lects.max()}. prednášky", false) { onRoute("library:clips") }
        }
    }
}

@Composable
private fun Drill(art: Motif, title: String, sub: String, done: Boolean, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button, onClick = onClick).heightIn(min = 72.dp).padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (LocalDensity.current.fontScale < 1.5f) MotifTile(art)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Rich(title, style = MaterialTheme.typography.titleMedium, color = c.ink)
            Rich(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        if (done) Box(Modifier.size(24.dp).clip(CircleShape).background(c.goodTint), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(16.dp)) }
        else Icon24(Res.drawable.ic_chevron, tint = c.ink3)
    }
}

/* ---- Library ------------------------------------------------------------------------------------------------------ */

@Composable
fun LibraryScreen(store: Store, section: String, onClose: () -> Unit, onRoute: (String) -> Unit) {
    val c = T.c
    StepFrame(0, 0, onClose, closeLabel = "Späť") {
        val course = store.course
        val shown = course.shown()
        val Concepts = shown.concepts; val Theorems = shown.theorems; val Proofs = shown.proofs
        Label(course.libraryLabel)
        Spacer(Modifier.height(4.dp))
        H1("Definície, vety a klipy")
        Spacer(Modifier.height(4.dp))
        Sub("Všetko so zdrojom: číslo v skriptách, dátum ${course.test.word.gen} alebo prednáška.")
        Spacer(Modifier.height(10.dp))
        val order = when (section) { "proofs" -> listOf("proofs", "defs", "thms", "clips"); "clips" -> listOf("clips", "defs", "thms", "proofs"); else -> listOf("defs", "thms", "proofs", "clips") }
        val has = mapOf("defs" to Concepts.isNotEmpty(), "thms" to Theorems.isNotEmpty(), "proofs" to Proofs.isNotEmpty(), "clips" to shown.clips.isNotEmpty())
        for (s in order.filter { has[it] == true }) {
            Spacer(Modifier.height(8.dp))
            when (s) {
                "defs" -> {
                    Text("Definície", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Concepts.forEach { k -> LibRow(k.name, k.src, store.mastered("concept:${k.id}")) { onRoute("concept:${k.id}") } }
                }
                "thms" -> {
                    Text("Vety, ktoré chce kurz", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Theorems.forEach { t -> LibRow(t.name, t.src + if (t.proof != null) ", s dôkazom" else "", false) { onRoute("theorem:${t.id}") } }
                }
                "proofs" -> {
                    Text("Dôkazy po riadkoch", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    Proofs.forEach { p -> LibRow(p.title, p.src, store.mastered("proof:${p.id}")) { onRoute("proof:${p.id}") } }
                }
                "clips" -> {
                    Text("Klipy z prednášok", style = MaterialTheme.typography.titleLarge, color = c.ink)
                    shown.clips.forEach { k -> LibRow(k.before, "${lectureName(k)}, ${k.seconds()} s", store.history("clip:${k.id}").lastOrNull()?.ok == true) { onRoute("clip:${k.id}") } }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LibRow(title: String, sub: String, done: Boolean, onClick: () -> Unit) {
    val c = T.c
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick).heightIn(min = 56.dp).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Rich(title, style = MaterialTheme.typography.titleSmall, color = c.ink)
            Rich(sub, style = MaterialTheme.typography.bodySmall, color = c.ink2)
        }
        if (done) Box(Modifier.size(24.dp).clip(CircleShape).background(c.goodTint), contentAlignment = Alignment.Center) { Icon24(Res.drawable.ic_check, tint = c.good, modifier = Modifier.size(16.dp)) }
        else Icon24(Res.drawable.ic_chevron, tint = c.ink3)
    }
}

/* ---- Review: what went wrong comes back -------------------------------------------------------------------------- */

/** A review round from the queue; when nothing is due, unmastered templates and random definitions. */
fun reviewRoutes(store: Store, r: kotlin.random.Random): List<String> {
    val course = store.course
    val due = dueKeys(store, course)
    val shown = course.shown()
    // besides what is due, two problems and a definition from finished lessons only: never something not yet learnt
    val done = shown.lessons.filter { it.id in store.lessonsDone }
    val gens = done.flatMap { l -> l.steps.filterIsInstance<Step.Practice>().map { "gen:" + (TemplateOf[it.gen] ?: it.gen) } }.distinct()
    val defs = done.flatMap { l -> l.steps.filterIsInstance<Step.Def>().map { "concept:${it.concept}" } }.distinct()
    // only what a review screen can show (RouteItem): a paper problem stays in "Vracia sa ti", it needs paper and a clock
    val pool = (due.filter { it.substringBefore(':') in ReviewKinds } + gens.shuffled(r).take(2) + defs.shuffled(r).take(1)).distinct()
    return pool.take(6)
}

/**
 * A review round. [today]: exactly today's dose (data/Review.kt), each thing with why it is back, every finished one
 * counted towards today; else "Zmiešané opakovanie", which may draw beyond the dose (reviewRoutes).
 */
@Composable
fun ReviewScreen(store: Store, clips: ClipHost, onReport: (String) -> Unit, onClose: () -> Unit, today: Boolean = false, onDone: (Int) -> Unit) {
    val day = remember { LocalDate.now() }
    val roundStart = remember { currentTimeMillis() }
    val dose = remember { if (today) reviewToday(store, day) else null }
    val routes = remember { dose?.items?.map { it.key } ?: reviewRoutes(store, kotlin.random.Random(nanoTime())) }
    var i by remember { mutableIntStateOf(0) }
    val total = routes.size
    // today's dose already done, or all of it answered today elsewhere: the end says so and what comes tomorrow
    if (total == 0 && dose != null && (dose.done > 0 || dose.tomorrow > 0)) { LaunchedEffect(Unit) { onDone(0) }; return }
    // before the first lesson there is nothing to go back to: say so instead of a finished round of nothing
    if (total == 0) {
        StepFrame(0, 0, onClose, closeLabel = "Zavrieť opakovanie", footer = { Cta("Späť", onClose) }) {
            H1("Zatiaľ nie je čo opakovať.")
            Spacer(Modifier.height(6.dp))
            Sub("Opakovanie skladá chyby a príklady z lekcií, ktoré máš za sebou. Po prvej lekcii tu niečo bude.")
        }
        return
    }
    if (i >= total) { LaunchedEffect(Unit) { onDone(total) }; return }
    val route = routes[i]
    // left after an answer and before its "Ďalej" (the close button or the system back, which pops the screen without
    // it): that answer still counts towards today's dose, so the round never offers more than the dose
    if (dose != null) DisposableEffect(route) {
        onDispose { if (store.attempts.any { it.key == route && it.at >= roundStart } || store.mistakes.any { it.key == route && it.at >= roundStart }) store.reviewedNow(route) }
    }
    val frame = Frame(i + 1, total, onClose, onReport, "Zavrieť opakovanie")
    val next: () -> Unit = { if (dose != null) store.reviewedNow(route); i++ }
    val reason = dose?.items?.getOrNull(i)?.let { reviewReason(it, day) }
    CompositionLocalProvider(LocalStepReason provides reason) { key(i) { RouteItem(route, store, clips, frame, next) } }
}

/** The kinds of route [RouteItem] shows. */
internal val ReviewKinds = setOf("gen", "concept", "proof", "theorem", "clip", "step")

/** One item by its route, the same views the lessons use. */
@Composable
fun RouteItem(route: String, store: Store, clips: ClipHost, frame: Frame, onDone: () -> Unit) {
    val (kind, id) = route.substringBefore(':') to route.substringAfter(':')
    when (kind) {
        "gen" -> {
            val gen = genOf(id) ?: Generators.keys.first()
            var seed by remember(route) { mutableIntStateOf(kotlin.random.Random.nextInt()) }
            val ex = remember(seed) { Generators.getValue(gen)(kotlin.random.Random(seed)) }
            ExerciseView(ex, solo = true, frame = frame) { ok, aided -> recordExercise(store, ex, ok, aided); onDone() }
        }
        "concept" -> ConceptById[id]?.let { ConceptFlow(it, store, frame, onDone) } ?: LaunchedEffect(Unit) { onDone() }
        "proof" -> ProofById[id]?.let { ProofFlow(it, store, frame, onDone) } ?: LaunchedEffect(Unit) { onDone() }
        "theorem" -> TheoremById[id]?.let { TheoremFlow(it, store, frame, onDone) } ?: LaunchedEffect(Unit) { onDone() }
        "clip" -> ClipLibrary.all.firstOrNull { it.id == id }?.let { ClipFlow(it, store, clips, frame, onDone) } ?: LaunchedEffect(Unit) { onDone() }
        "step" -> AllLessons.flatMap { it.steps }.firstOrNull { it.id == id }?.let { StepView(it, frame, store, clips, onDone) } ?: LaunchedEffect(Unit) { onDone() }
        else -> LaunchedEffect(Unit) { onDone() }
    }
}

@Composable
fun RoundEndScreen(
    title: String, body: String, next: String?, onNext: (() -> Unit)?, onDone: () -> Unit,
    kicker: String? = null, learn: String? = null, facts: List<Pair<String, String>> = emptyList(), art: Motif? = null, confetti: Boolean = false,
    streak: (() -> Unit)? = null, nextName: String? = null, formula: String? = null, test: TestTeaser? = null,
) {
    val c = T.c
    val hx = rememberHaptics()
    if (SmerNow != Smer.Dnes) {
        LaunchedEffect(Unit) { delay(60); hx.finish(); if (streak != null) { delay(1100); hx.streak(); streak() } }
        EndInSmer(title, body, next, onNext, onDone, kicker, learn, facts, art, confetti, nextName, formula, test)
        return
    }
    // One moment (Teo, 18. 11.: the end "je dosť mid": half the screen empty, three grey lines of one weight, no sense
    // of done): the picture lands, the sound's chord, the phone's click and the title come together. lesson_done peaks
    // 90 ms after it starts (its lead-in is cut, POVOD.md), Buzz.Finish clicks about 120 ms after it starts; both
    // start 60 ms after the screen, the picture's pop and the title rise from 60 and 100 ms, so all four meet around
    // 150 to 200 ms. [streak]: the days in a row grew today, said after the lesson's own sound has ended, never over it.
    LaunchedEffect(Unit) { delay(60); hx.finish(); if (streak != null) { delay(1100); hx.streak(); streak() } }
    var origin by remember { mutableStateOf<Offset?>(null) }
    var box by remember { mutableStateOf(Offset.Zero) }
    val big = LocalDensity.current.fontScale >= 1.5f
    BoxWithConstraints(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding().onGloballyPositioned { box = it.positionInRoot() }) {
        val short = maxHeight < 640.dp || big
        val pic = if (short) 88.dp else if (maxWidth < 340.dp) 112.dp else 128.dp
        // the whole height: the moment in the middle of what is left, the way on at the bottom, where the thumb is
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f).heightIn(min = 8.dp))
            val center = Modifier.onGloballyPositioned { val p = it.positionInRoot(); origin = Offset(p.x + it.size.width / 2f, p.y + it.size.height / 2f) }
            // the reward: the lesson's own sketch at its goal, else Pytagoras (the review's end)
            Box(center.landing()) {
                if (art != null) LessonSketch(art, size = pic, done = true)
                else Pytagoras(if (confetti) Mood.Celebrating else Mood.Pleased, size = pic)
            }
            Spacer(Modifier.height(if (short) 14.dp else 22.dp))
            if (kicker != null) { Text(kicker, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = c.lesson.text, textAlign = TextAlign.Center, modifier = Modifier.arrive(0, start = 60)); Spacer(Modifier.height(4.dp)) }
            Text(title, style = MaterialTheme.typography.displaySmall, color = c.ink, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() }.arrive(0, start = 100))
            // what the lesson taught, in the words of its plan; from 150 % it steps aside so the buttons stay in view
            if (learn != null && !big) { Spacer(Modifier.height(10.dp)); Rich(learn, Modifier.arrive(1, start = 100), style = MaterialTheme.typography.bodyLarge, color = c.ink2, textAlign = TextAlign.Center) }
            if (facts.isNotEmpty()) {
                Spacer(Modifier.height(if (short) 18.dp else 26.dp))
                // the result as figures, each with what it counts: read at a glance, not three grey lines of one weight
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).arrive(2, start = 100), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for ((value, label) in facts) Column(
                        Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(c.card).padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = c.ink, softWrap = false)
                        Spacer(Modifier.height(2.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                }
            }
            if (body.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Rich(body, Modifier.arrive(2, start = 100), style = MaterialTheme.typography.bodySmall, color = c.ink2, textAlign = TextAlign.Center) }
            Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
            Column(Modifier.fillMaxWidth().arrive(3, start = 100), horizontalAlignment = Alignment.CenterHorizontally) {
                if (next != null && onNext != null) {
                    Cta(next, onNext, hue = c.lesson)
                    // what comes next under the button, not in it (Teo: a long label read as a sentence, not a button)
                    if (nextName != null) { Spacer(Modifier.height(6.dp)); Rich(nextName, style = MaterialTheme.typography.bodyMedium, color = c.ink2, textAlign = TextAlign.Center) }
                    Spacer(Modifier.height(12.dp))
                    Second("Hotovo", onDone)
                } else Cta("Hotovo", onDone, hue = c.lesson)
            }
        }
        if (confetti) Burst(origin?.let { it - box }, listOf(c.lesson.main, c.art.glow, c.art.r4, c.art.diskHi, c.edge), Modifier.matchParentSize())
    }
}

/** "Na teste ťa čaká": a problem of a past test the lesson prepared for, its first sentence, where it opens. */
data class TestTeaser(val label: String, val text: String, val route: String)

/** A problem of a past test this lesson prepares for: a part whose review leads into the lesson, else one of a type it practises. */
fun testTeaser(l: LessonDef, course: Course): TestTeaser? {
    val gens = l.steps.filterIsInstance<Step.Practice>().map { TemplateOf[it.gen] ?: it.gen }.toSet()
    fun of(paper: TestPaper, p: PaperProblem, u: PaperPart) = TestTeaser("${paper.name}, príklad ${p.n}" + (if (u.label.isNotEmpty()) ", časť ${u.label}" else "") + ", ${u.pts}\u00A0b", u.text.substringBefore(". ") + if ('.' in u.text) "." else "", "paper:${paper.id}:${p.n}")
    for (paper in course.papers) for (p in paper.problems) for (u in p.units) {
        val r = reviewLink(u, course)?.route ?: continue
        if (r.startsWith("lesson:${l.id}") || (r.startsWith("step:") && l.steps.any { it.id == r.removePrefix("step:") })) return of(paper, p, u)
    }
    for (paper in course.papers) for (p in paper.problems) if (p.drill != null && p.drill in gens) return of(paper, p, p.units.first())
    return null
}

/**
 * The end of a lesson in directions A, B and C (ui/Smer.kt). The same parts as today's: the moment, what the lesson
 * taught, the result in figures, the way on; each direction sets them its own way. C shows the problem of a past test
 * the lesson prepared for ([test]): the place the proposed structure gives it (NAVRH.md); A and B leave it out.
 */
@Composable
private fun EndInSmer(
    title: String, body: String, next: String?, onNext: (() -> Unit)?, onDone: () -> Unit, kicker: String?, learn: String?,
    facts: List<Pair<String, String>>, art: Motif?, confetti: Boolean, nextName: String?, formula: String?, test: TestTeaser?,
) {
    val base = T.c
    val smer = SmerNow
    val fill = boldFill(base)
    var origin by remember { mutableStateOf<Offset?>(null) }
    var box by remember { mutableStateOf(Offset.Zero) }
    val big = LocalDensity.current.fontScale >= 1.5f
    CompositionLocalProvider(LocalPalette provides (if (smer.bold) solidOn(base, fill) else base)) {
        val c = T.c
        val page = when (smer) {
            Smer.Zosit -> Modifier.background(c.art.paper).squaredPaper(c.art.grid, margin = c.art.margin, marginAt = 28.dp)
            Smer.Odvazna, Smer.CPlus -> Modifier.background(fill)
            else -> Modifier.background(c.bg)
        }
        BoxWithConstraints(Modifier.fillMaxSize().then(page).statusBarsPadding().navigationBarsPadding().onGloballyPositioned { box = it.positionInRoot() }) {
            val short = maxHeight < 640.dp || big
            val center = Modifier.onGloballyPositioned { val p = it.positionInRoot(); origin = Offset(p.x + it.size.width / 2f, p.y + it.size.height / 2f) }
            val align = if (smer == Smer.Sadzba) Alignment.Start else Alignment.CenterHorizontally
            val ta = if (smer == Smer.Sadzba) TextAlign.Start else TextAlign.Center
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(start = if (smer == Smer.Zosit) 40.dp else 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                horizontalAlignment = align,
            ) {
                Spacer(Modifier.weight(1f).heightIn(min = 8.dp))
                when (smer) {
                    // A: Pytagoras and the lesson's sketch, a stamp across them
                    Smer.Zosit -> Box(center.landing(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Pytagoras(Mood.Celebrating, size = if (short) 84.dp else 112.dp)
                            if (art != null) { Spacer(Modifier.width(10.dp)); LessonSketch(art, size = if (short) 72.dp else 96.dp, done = true) }
                        }
                        Text("HOTOVO", Modifier.align(Alignment.TopEnd).offset(x = 18.dp, y = (-12).dp).graphicsLayer { rotationZ = -9f }
                            .border(2.dp, c.bad, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = c.bad)
                    }
                    // B: the rule and the lesson's statement are the picture
                    Smer.Sadzba -> Box(Modifier.fillMaxWidth().height(2.dp).background(c.ink).then(center))
                    // C+: Pytagoras cheering beside the lesson's sketch on its scrap of squared paper
                    // C+: the lesson's sketch alone on the main axis, on its scrap of squared paper (Teo, 18. 11.: Pytagoras
                    // beside it "kus zavadza")
                    Smer.CPlus -> if (art != null) Box(center.landing().clip(RoundedCornerShape(24.dp)).background(base.art.paper).squaredPaper(base.art.grid, 12.dp).padding(6.dp)) {
                        CompositionLocalProvider(LocalPalette provides base) { LessonSketch(art, size = if (short) 88.dp else 128.dp, done = true) }
                    }
                    // no lesson (the review's end): Pytagoras on his own, not on a scrap of paper meant for a drawing
                    else Box(center.landing()) { CompositionLocalProvider(LocalPalette provides base) { Pytagoras(if (confetti) Mood.Celebrating else Mood.Pleased, size = 104.dp, bg = Color.White.copy(alpha = .92f)) } }
                    // C: the sketch on a light tile
                    else -> Box(center.landing().clip(RoundedCornerShape(28.dp)).background(base.art.paper).padding(8.dp)) {
                        CompositionLocalProvider(LocalPalette provides base) { if (art != null) LessonSketch(art, size = if (short) 80.dp else 104.dp, done = true) else Pytagoras(Mood.Celebrating, size = 96.dp) }
                    }
                }
                Spacer(Modifier.height(if (short) 14.dp else 22.dp))
                if (kicker != null) { Text(kicker, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = if (smer.bold) c.ink2 else c.lesson.text, textAlign = ta, modifier = Modifier.arrive(0, start = 60)); Spacer(Modifier.height(4.dp)) }
                Text(title, style = if (smer == Smer.Sadzba) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displaySmall, color = c.ink, textAlign = ta, modifier = Modifier.semantics { heading() }.arrive(0, start = 100))
                if (learn != null && !big) { Spacer(Modifier.height(10.dp)); Rich(learn, Modifier.arrive(1, start = 100), style = MaterialTheme.typography.bodyLarge, color = c.ink2, textAlign = ta) }
                if (smer == Smer.Sadzba && formula != null && !big) {
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().arrive(1, start = 100).background(c.card).border(1.dp, c.line).padding(horizontal = 12.dp, vertical = 14.dp)) {
                        Rich("$$$formula$$", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), color = c.ink)
                    }
                }
                if (facts.isNotEmpty()) {
                    Spacer(Modifier.height(if (short) 18.dp else 24.dp))
                    if (smer == Smer.Sadzba) Column(Modifier.fillMaxWidth().arrive(2, start = 100)) {
                        // B: a ruled table, the figure in the formulas' type on the right
                        for ((value, label) in facts) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
                            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(label.replaceFirstChar { it.uppercase() }, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                                Text(value, style = MaterialTheme.typography.headlineSmall, color = c.ink, softWrap = false)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
                    } else Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).arrive(2, start = 100), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for ((i, f) in facts.withIndex()) Box(Modifier.weight(1f).fillMaxHeight()) {
                        val (value, label) = f
                        Column(
                            Modifier.fillMaxSize().clip(RoundedCornerShape(if (smer == Smer.Zosit) 6.dp else 18.dp)).background(when (smer) { Smer.Zosit -> c.card.copy(alpha = .9f); Smer.Odvazna, Smer.CPlus -> Color.Black.copy(alpha = .16f); else -> c.card })
                                .then(if (smer == Smer.Zosit) Modifier.border(1.dp, c.line, RoundedCornerShape(6.dp)) else Modifier).padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = c.ink, softWrap = false)
                            Spacer(Modifier.height(2.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        }
                        // C+, variant 1: Pytagoras small on the result's corner, off the main axis, only for a lesson
                        // that went well (cheering "0 z 3" would read as false praise)
                        if (smer == Smer.CPlus && i == 0 && (EndMascot == 2 || EndMascot == 1 && confetti)) CompositionLocalProvider(LocalPalette provides base) {
                            Pytagoras(Mood.Celebrating, Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-14).dp), bg = Color.White, size = 44.dp)
                        }
                        }
                    }
                }
                // C: the problem of a past test the lesson prepared for, a white card on the green
                if (smer == Smer.Odvazna && test != null && !short) {
                    Spacer(Modifier.height(14.dp))
                    val quiet = smer == Smer.CPlus
                    CompositionLocalProvider(LocalPalette provides (if (quiet) c else base)) {
                        val t = T.c
                        Column(Modifier.fillMaxWidth().arrive(2, start = 140).clip(RoundedCornerShape(20.dp)).background(if (quiet) Color.Black.copy(alpha = .16f) else base.card).padding(16.dp)) {
                            Text("Na teste ťa čaká", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (quiet) t.ink2 else base.lesson.text)
                            Spacer(Modifier.height(2.dp))
                            Text(test.label, style = MaterialTheme.typography.titleMedium, color = t.ink)
                            Spacer(Modifier.height(6.dp))
                            Rich(test.text, style = MaterialTheme.typography.bodyMedium, color = t.ink2)
                        }
                    }
                }
                if (body.isNotEmpty() && smer != Smer.Odvazna) { Spacer(Modifier.height(12.dp)); Rich(body, Modifier.arrive(2, start = 100), style = MaterialTheme.typography.bodySmall, color = c.ink2, textAlign = ta) }
                Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
                Column(Modifier.fillMaxWidth().arrive(3, start = 100), horizontalAlignment = align) {
                    if (next != null && onNext != null) {
                        Cta(next, onNext, hue = c.lesson)
                        if (nextName != null) { Spacer(Modifier.height(6.dp)); Rich(nextName, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = c.ink2, textAlign = TextAlign.Center) }
                        Spacer(Modifier.height(12.dp))
                        Second("Hotovo", onDone)
                    } else Cta("Hotovo", onDone, hue = c.lesson)
                }
            }
            if (confetti && smer != Smer.Sadzba) Burst(origin?.let { it - box }, listOf(base.lesson.main, base.art.glow, base.bad, base.art.diskHi, base.edge, Color.White), Modifier.matchParentSize())
        }
    }
}

/**
 * Pytagoras at the end of a lesson in C+: 0 none, 1 on the result when the lesson went well, 2 always (screenshots).
 * None (18. 11.): on the result's corner he read as a sticker that competed with the drawing, the green is the reward.
 */
var EndMascot = 0

/** The end screen's picture: from nothing to a little too big and back, 60 ms after the screen appears. */
@Composable
private fun Modifier.landing(): Modifier {
    val still = calm()
    val a = remember { Animatable(if (still) 1f else 0f) }
    LaunchedEffect(Unit) { if (!still) { delay(60); a.animateTo(1f, Motion.pop()) } }
    return graphicsLayer { scaleX = a.value; scaleY = a.value }
}

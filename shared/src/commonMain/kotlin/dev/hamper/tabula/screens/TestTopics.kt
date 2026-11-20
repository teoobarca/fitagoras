package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import dev.hamper.tabula.ui.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.platform.LocalDensity
import dev.hamper.tabula.data.*
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_chevron
import dev.hamper.tabula.res.ic_info
import androidx.compose.ui.draw.rotate
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.*
import dev.hamper.tabula.math.nb
import androidx.compose.foundation.border

/**
 * The page about the course's test (LA1: 2nd test, DML: 2nd písomka): the facts (problems, points, minutes, when and
 * where), what each problem usually asks over the papers in today's format, where points are lost (the lecturers'
 * words, with their source), the course page's topics and an order for one evening. Each item links to where the app
 * practises it, with how far the student is. Drawn from files/test/<exam id>.json (data/TestPage.kt) and the exam's
 * topics (TestTopicsByExam). Opened from the plan and from Nácvik when the exam has a page.
 */
@Composable
fun TestTopicsScreen(store: Store, onClose: () -> Unit, onRoute: (String) -> Unit, examId: String = store.course.test.id) {
    val c = T.c
    val course = store.course
    // [examId]: the course's next exam by default; tests name one, so the page does not depend on today's date
    val page = testPage(examId)
    val topics = TestTopicsByExam[examId]
    val states = remember(store.version, course.id) { topics?.topics.orEmpty().map { topicState(it, store, course) } }
    // what a route shows here: offered by the course, with a label and how far the student is
    fun links(routes: List<String>, max: Int = 3, note: String? = null, count: String? = null): List<Link> = routes.distinct()
        .filter { course.shown().offersKey(it) }
        .mapNotNull { r -> routeLabel(r, course)?.let { Link(it, r, routeState(r, store), note ?: mockNote(r, store), count) } }.take(max)
    StepFrame(0, 0, onClose, closeLabel = "Zavrieť") {
        Label("${course.test.short} z ${course.short}")
        Spacer(Modifier.height(4.dp))
        // the page's own title, a step larger than a lesson's: it is a page to come back to, not a step
        Rich(page?.title ?: "Čo býva na ${course.test.short}", style = MaterialTheme.typography.headlineLarge, color = c.ink)
        page?.sub?.takeIf { it.isNotBlank() }?.let { Spacer(Modifier.height(4.dp)); Sub(it) }

        // Teo, 18. 11.: layered for the evening before the test: what to do is on top, the rest one level lower
        if (page != null) {
            // (1) the facts: three lines, the long rule and the sources under "i"
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().panel(18.dp, fill = c.card).padding(horizontal = 16.dp, vertical = 14.dp)) {
                // when it is, the one thing that matters most, first and largest; in ink, as green is only for what
                // opens something (judges, 18. 11.: the date weighed the same as the count of problems; a green one
                // read as a link)
                Text(whenWhere(store, course), style = MaterialTheme.typography.titleLarge, color = c.ink)
                Spacer(Modifier.height(6.dp))
                // each part whole ("7 + 12 + 11 b" never split at a large font); at a break the dot goes
                Parts(listOf("${page.points.size} príklady", "${if (page.pointsSure) page.points.joinToString(" + ") else "spolu " + page.points.sum()} b".replace(' ', '\u00A0'), "${page.minutes} min"), style = MaterialTheme.typography.bodyLarge, color = c.ink)
                page.facts.firstOrNull()?.let { rule ->
                    Spacer(Modifier.height(2.dp))
                    Text(unbroken(rule.substringBefore(" (").substringBefore(";")).nb(), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                }
                InfoNote("Podrobnosti a zdroje", "facts") {
                    page.facts.forEach { Text(it.nb(), style = MaterialTheme.typography.bodySmall, color = c.ink2); Spacer(Modifier.height(4.dp)) }
                    Source(page.sources.joinToString("; "))
                }
            }

            // (5) one evening: what to do tonight, each step with the place to start; the rest of its links folded
            // a page with nothing for an evening (LA1's skúška, weeks ahead) has no such section
            if (page.evening.isNotEmpty()) Section("Ak máš len jeden večer")
            // each step: what to do and why, and right under it, in the same column, where in the app to start; the
            // other places folded behind a green "Ďalšie 3 miesta v appke" (judges, 18. 11.: a hairline and a link from
            // the card's edge cut the step from the place to practise it; a grey fold read as one more heading)
            val d = LocalDensity.current
            // the step's number grows with the text, so its digit never sticks out of the circle at a large font
            val disc = max(28.dp, with(d) { MaterialTheme.typography.titleSmall.lineHeight.toDp() } + 8.dp)
            for (s in page.evening) {
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().panel(18.dp, fill = c.card).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp)) {
                    Row {
                        // on the dark card a lighter disc, so the number's place shows there too
                        Box(Modifier.size(disc).clip(CircleShape).background(if (c.dark) c.edge else c.sand), contentAlignment = Alignment.Center) {
                            Text("${s.n}", style = MaterialTheme.typography.titleSmall, color = c.ink)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Rich(unbroken(s.what), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = c.ink)
                            Spacer(Modifier.height(2.dp))
                            Rich(unbroken(s.why), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                        }
                    }
                    val all = links(s.routes, max = 9)
                    // in the column of the text above
                    if (all.isNotEmpty()) Column(Modifier.padding(start = disc + 12.dp)) {
                        Spacer(Modifier.height(4.dp))
                        LinkRows(all.take(1), onRoute)
                        if (all.size > 1) Unfold(morePlaces(all.size - 1), "evening-${s.n}") { LinkRows(all.drop(1), onRoute) }
                    }
                    if (all.isEmpty()) Spacer(Modifier.height(6.dp))
                }
            }

            // (2) what each problem usually asks: one card a problem, its most frequent kinds; all of it folded
            Section("Čo tam býva")
            for (pos in page.positions) {
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().panel(18.dp, fill = c.card).padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // a title may hold math ("Výpočet v $\mathbb{Z}_p^4$ s vysvetlením")
                    Rich("${pos.n}. ${pos.title ?: "príklad"}" + (if (page.pointsSure) " · ${pos.points} b" else ""), Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = c.ink)
                    Spacer(Modifier.height(4.dp))
                    // a problem asking definitions or proofs: those rows first; its other tasks as one row when each came once
                    val rows = listOfNotNull(pos.definitions, pos.proofs, pos.other)
                    val top = rows.map { it.row to "${it.count}× z ${it.of}" } + (if (pos.other == null) pos.kinds.map { it.label to "${it.count}× z ${it.of}" } else emptyList())
                    top.take(3).forEach { (label, count) -> CountRow(label, count) }
                    Unfold("Zobraziť všetko", "problem-${pos.n}") {
                        pos.definitions?.let { d ->
                            // only those asked in this problem, when the page says where (LA1's skúška asks them in two)
                            GroupCard(d.groupText, page.definitions.filter { it.at.isEmpty() || pos.n in it.at }.flatMap { links(it.routes, count = "${it.count}× z ${it.of}") }, onRoute)
                            Spacer(Modifier.height(6.dp))
                        }
                        pos.proofs?.let { p -> GroupCard(p.groupText, page.proofs.filter { it.at.isEmpty() || pos.n in it.at }.flatMap { links(it.routes, note = it.where.joinToString(", ") { t -> page.testName(t) }) }, onRoute) }
                        pos.other?.takeIf { pos.kinds.isNotEmpty() }?.let { o ->
                            Spacer(Modifier.height(10.dp))
                            Text(o.groupText.nb(), style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                        }
                        for (k in pos.kinds) { Spacer(Modifier.height(6.dp)); KindCard(k.label, k.count, k.of, links(k.routes), onRoute) }
                    }
                }
            }
            InfoNote("Odkiaľ sú počty, ako vyzerajú hárky a pokyny na nich", "sheets") {
                Text("Koľkokrát bol ktorý typ úlohy v ${page.tests} ${when (course.test.word) { PisomkaWord -> "písomkách"; SkuskaWord -> "skúškach"; else -> "testoch" }} od roku ${page.since} (fit-wiki).".nb(), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                page.notes.forEach { n ->
                    Row(Modifier.padding(top = 4.dp)) {
                        Text("•", style = MaterialTheme.typography.bodySmall, color = c.ink3, modifier = Modifier.width(12.dp))
                        Rich(n, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                }
            }

            // (3) where points are lost: five, one sentence each; the quote, its source and the links on a tap
            val lost = page.pointsLost
            if (lost.isNotEmpty()) Section("Na čom sa strácajú body")
            lost.take(5).forEachIndexed { i, l -> Spacer(Modifier.height(6.dp)); LossRow(l, "loss-$i", links(l.routes), onRoute) }
            if (lost.size > 5) Unfold(moreMistakes(lost.size - 5), "loss-more") {
                lost.drop(5).forEachIndexed { i, l -> Spacer(Modifier.height(6.dp)); LossRow(l, "loss-${i + 5}", links(l.routes), onRoute) }
            }
        }

        // (4) the course page's topics: folded, with how many are done
        if (topics != null) {
        Section("Témy zo stránky predmetu")
        Column(Modifier.fillMaxWidth().panel(18.dp, fill = c.card).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("${states.count { it == TopicState.Done }} z ${states.size} zvládnutých bez pomoci".nb(), style = MaterialTheme.typography.titleMedium, color = c.ink)
            Text(topics.source.nb(), style = MaterialTheme.typography.bodySmall, color = c.ink2)
            Unfold("Rozbaliť témy", "topics") {
                var group = ""
                topics.topics.forEachIndexed { i, t ->
                    if (t.group != group) {
                        group = t.group
                        Spacer(Modifier.height(10.dp))
                        Text(group, style = MaterialTheme.typography.titleSmall, color = c.ink, modifier = Modifier.semantics { heading() })
                    }
                    Spacer(Modifier.height(6.dp))
                    TopicCard(t.text, states[i], topicRoutes(t, course).mapNotNull { topicLink(it, course) }.distinctBy { it.second }.take(4), onRoute)
                }
            }
        }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * What the page about a test holds, for the rows that open it: "Kde sa strácajú body, poradie na jeden večer" only when
 * it has those parts (LA1's skúška has neither: "Typy úloh z 3 skúšok"). [types]: name the kinds of tasks too.
 */
fun pageBlurb(page: TestPageData, w: ExamWord, types: Boolean): String {
    val parts = listOfNotNull(
        if (types || (page.pointsLost.isEmpty() && page.evening.isEmpty())) "typy úloh z ${page.tests} ${w.genPl}" else null,
        "kde sa strácajú body".takeIf { page.pointsLost.isNotEmpty() },
        "poradie na jeden večer".takeIf { page.evening.isNotEmpty() },
    )
    return parts.joinToString(", ").replaceFirstChar { it.uppercase() }
}

/** A kind of task and how often it came, one line (the folded card of a problem). */
@Composable
private fun CountRow(label: String, count: String) {
    val c = T.c
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Rich(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = c.ink)
        Spacer(Modifier.width(8.dp))
        Text(count, style = MaterialTheme.typography.titleSmall, color = c.ink2)
    }
}

/** Where points are lost, one sentence; a tap shows the lecturer's words, the source and where to practise. */
@Composable
private fun LossRow(l: TestPageData.Loss, key: String, links: List<Link>, onRoute: (String) -> Unit) {
    val c = T.c
    val all = LocalUnfoldAll.current
    var open by rememberSaveable(key) { mutableStateOf(all) }
    Column(Modifier.fillMaxWidth().panel(16.dp, fill = c.card).clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button) { open = !open }.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Rich(l.what, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = c.ink)
            Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.size(18.dp).rotate(if (open) -90f else 90f))
        }
        if (open) {
            if (l.quote.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(l.quote, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = c.ink)
                Text("— ${l.quoteSrc}", style = MaterialTheme.typography.bodySmall, color = c.ink2)
            }
            // the quote's own source says it; a longer list of sources gets its own line
            if (l.quote.isBlank() || ';' in l.source) { Spacer(Modifier.height(4.dp)); Source(l.source) }
            LinkRows(links, onRoute)
        }
    }
}

/**
 * Text from the page's data as one reads it: "LN/LZ" is one word, never "LN/" and "LZ" on two lines (a word joiner
 * after the slash), "40 b", "70 min" and "4× z 9" stay whole; outside the formulas.
 */
private fun unbroken(s: String): String = s.split('§').mapIndexed { i, part ->
    if (i % 2 == 1 || '$' in part) part
    else part.replace(Regex("(?<=\\w)/(?=\\w)"), "/\u2060").replace(Regex("(\\d) (b|min)\\b"), "$1\u00A0$2").replace("× z ", "×\u00A0z\u00A0")
}.joinToString("§")

/** "Ešte jedno miesto v appke", "Ďalšie 3 miesta v appke", "Ďalších 5 miest v appke": what the fold holds, with its count. */
private fun morePlaces(n: Int) = when { n == 1 -> "Ešte jedno miesto v appke"; n in 2..4 -> "Ďalšie $n miesta v appke"; else -> "Ďalších $n miest v appke" }

/** The same for the mistakes that cost points. */
private fun moreMistakes(n: Int) = when { n == 1 -> "Ešte jedna chyba"; n in 2..4 -> "Ďalšie $n chyby"; else -> "Ďalších $n chýb" }

/** A small "i" that unfolds what the main flow leaves out (sources, long rules, how the sheets look). */
@Composable
private fun InfoNote(label: String, key: String, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c
    val all = LocalUnfoldAll.current
    var open by rememberSaveable(key) { mutableStateOf(all) }
    val st = MaterialTheme.typography.labelMedium
    Column(Modifier.padding(top = 6.dp)) {
        Row(
            Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { open = !open }.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // the "i" as large as the words beside it, at any font
            Icon24(Res.drawable.ic_info, tint = c.ink3, modifier = Modifier.size(max(16.dp, with(LocalDensity.current) { st.fontSize.toDp() } * 1.2f)))
            Spacer(Modifier.width(6.dp))
            Text(if (open) "Skryť" else label.nb(), style = st, color = c.ink3)
        }
        if (open) Column(Modifier.padding(bottom = 4.dp)) { content() }
    }
}

/** "streda 18. 11. o 18:00, T9:105": the student's sitting (the picked one, else the next in KOS) and its room. */
private fun whenWhere(store: Store, course: Course): String {
    val t = store.testStart(course)
    val room = course.test.terms.firstOrNull { it.start == t }?.room
    return testLine(store, course).substringAfter(", ") + (room?.let { ", $it" } ?: "")
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(26.dp))
    Text(title.nb(), style = MaterialTheme.typography.titleLarge, color = T.c.ink, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun Source(text: String) = Text("Zdroj: $text".nb(), style = MaterialTheme.typography.bodySmall, color = T.c.ink3)

/** A link into the app: what it opens, how far the student is (none for a theorem, only to read), a note, a count. */
private data class Link(val label: String, val route: String, val state: TopicState?, val note: String? = null, val count: String? = null)

/** A kind of task: its name, how often it came and where to practise it. */
@Composable
private fun KindCard(label: String, count: Int, of: Int, links: List<Link>, onRoute: (String) -> Unit) {
    val c = T.c
    Column(Modifier.fillMaxWidth().panel(16.dp, fill = c.card).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Rich(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = c.ink)
            Spacer(Modifier.width(8.dp))
            Text("$count× z $of", style = MaterialTheme.typography.titleSmall, color = c.ink2)
        }
        LinkRows(links, onRoute)
    }
}

/** Several links under one title: the definitions asked, the proofs asked. */
@Composable
private fun GroupCard(title: String, links: List<Link>, onRoute: (String) -> Unit) {
    val c = T.c
    Column(Modifier.fillMaxWidth().panel(16.dp, fill = c.card).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(title.nb(), style = MaterialTheme.typography.bodyLarge, color = c.ink)
        LinkRows(links, onRoute)
    }
}

/**
 * Tappable rows into the app; under each, how far the student is and the note, in the column of the text above. No
 * state dot before the label: the words under it say the state, and the pale dot of "nezačaté" hung outside the
 * column, unseen in the dark (judges and inspector, 18. 11.). What is mastered says so in green.
 */
@Composable
private fun LinkRows(links: List<Link>, onRoute: (String) -> Unit) {
    val c = T.c
    if (links.isNotEmpty()) Spacer(Modifier.height(2.dp))
    links.forEach { l ->
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { onRoute(l.route) }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Rich(l.label, style = MaterialTheme.typography.bodyMedium, color = c.lesson.text)
                listOfNotNull(l.state?.label, l.note).takeIf { it.isNotEmpty() }?.let { Parts(it, style = MaterialTheme.typography.bodySmall, color = if (l.state == TopicState.Done) c.good else c.ink2) }
            }
            l.count?.let { Text(it, style = MaterialTheme.typography.titleSmall, color = c.ink2, modifier = Modifier.padding(start = 8.dp)) }
            Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun dotColor(s: TopicState) = when (s) { TopicState.Done -> T.c.good; TopicState.Started -> T.c.lesson.main.copy(alpha = .42f); TopicState.New -> T.c.line }

@Composable
private fun TopicCard(text: String, state: TopicState, links: List<Pair<String, String>>, onRoute: (String) -> Unit) {
    val c = T.c
    val ink = when (state) { TopicState.Done -> c.good; TopicState.Started -> c.ink2; TopicState.New -> c.ink2 }
    Column(Modifier.fillMaxWidth().panel(18.dp, fill = c.card).padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // not started: an empty ring in the quiet ink, seen on the dark card too (a fill in the hairline colour was not)
            Box(Modifier.size(10.dp).clip(CircleShape).then(if (state == TopicState.New) Modifier.border(1.5.dp, c.ink3, CircleShape) else Modifier.background(dotColor(state))))
            Spacer(Modifier.width(8.dp))
            Text(state.label, style = MaterialTheme.typography.labelMedium, color = ink)
        }
        Spacer(Modifier.height(4.dp))
        Rich(text, style = MaterialTheme.typography.bodyLarge, color = c.ink)
        if (links.isNotEmpty()) Spacer(Modifier.height(6.dp))
        links.forEach { (label, route) ->
            Row(
                Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { onRoute(route) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Rich(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.lesson.text)
                Icon24(Res.drawable.ic_chevron, tint = c.ink3, modifier = Modifier.size(18.dp))
            }
        }
    }
}

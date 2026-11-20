package dev.hamper.tabula.snap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.hamper.tabula.App
import dev.hamper.tabula.AppState
import dev.hamper.tabula.Nav
import dev.hamper.tabula.Tab
import dev.hamper.tabula.Tabula
import dev.hamper.tabula.compat.media.ExoClipPlayer
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.data.LessonById
import dev.hamper.tabula.data.Pic
import dev.hamper.tabula.data.Step
import dev.hamper.tabula.screens.ClipHost
import dev.hamper.tabula.state.Attempt
import dev.hamper.tabula.state.Mistake
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.store
import org.junit.Test
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * The app's look with a student's record behind it, for checking the style (not a golden): `-Psnap.test=LookShots
 * [-Psnap.only=home]`. The record: DML open, six days in a row, three lessons of number theory and the earlier topics
 * done, "Kongruencie a inverzia" at step 6, a mistake made twice last Friday, an exercise solved with help, the LA1
 * test tomorrow at 18:00. Answered steps are set in the lesson's run, as if tapped.
 */
class LookShots : Shots() {
    private val kong = "dml-tc-kongruencie"
    private val rea = "dml-tc-rea"
    private val gemLesson = "la1-k3-hodnost"
    private fun worked(id: String, k: Int) = LessonById.getValue(id).steps.withIndex().filter { it.value is Step.Worked }[k].index

    private val cases: List<Pair<String, (AppState, Store) -> Unit>> = listOf(
        "home" to { _, s -> seed(s, diofant = false) },
        "home-novy" to { _, s -> seed(s, full = false) },
        // the student of "home" after finishing "Kongruencie a inverzia": the same record and one lesson more (it was a
        // record of its own, so "1 lekcia ešte chýba" stood both before and after the lesson; inspector, 18. 11.)
        "home-hotovo" to { _, s -> seed(s, diofant = false); s.finishLesson(kong) },
        "cesta" to { a, s -> seed(s); a.tab = Tab.Cesta },
        "nacvik" to { a, s -> seed(s); a.tab = Tab.Nacvik },
        // the tab "Test" (Nácvik before 18. 11.): LA1 the day before its test; a new student three weeks before the DML one
        "nacvik-la1" to { a, s -> seed(s); s.courseId = "la1"; a.tab = Tab.Nacvik },
        "nacvik-tyzdne" to { a, s -> seed(s, full = false); s.terms[s.course.test.id] = LocalDate.now().plusDays(23).atTime(9, 0).let { dev.hamper.tabula.compat.time.LocalDateTime.parse(it.toString()) }; a.tab = Tab.Nacvik },
        // one level below it: a problem of the test, every exercise
        "test-priklad" to { a, s -> seed(s); s.courseId = "la1"; a.tab = Tab.Nacvik; a.open("priklad:2", s) },
        "cvicenia" to { a, s -> seed(s); a.tab = Tab.Nacvik; a.open("cvicenia", s) },
        "nastavenia" to { a, s -> seed(s); a.push(Nav.Settings) },
        "odhad" to { a, s -> seed(s); lesson(a, s, 0, "pick" to "1") },
        "vysvetlenie" to { a, s -> seed(s); lesson(a, s, 1) },
        "definicia" to { a, s -> seed(s); lesson(a, s, 3) },
        "uloha" to { a, s -> seed(s); lesson(a, s, 10) },
        "spravne" to { a, s -> seed(s); lesson(a, s, 10, "pick" to "0") },
        // the lesson of "home" opened again after it was closed in the middle of a piece (its run stands at step 6)
        "pokracovanie" to { a, s -> seed(s); a.push(Nav.Lesson(kong)) },
        "zle" to { a, s -> seed(s); lesson(a, s, 10, "pick" to "1", "tried" to "1", "missed" to "1") },
        "hodiny" to { a, s -> seed(s); lesson(a, s, 15) },
        "dokaz" to { a, s -> seed(s); a.push(Nav.Item("proof:dml-tc-p-kongruence-ekv")) },
        "klip" to { a, s -> seed(s); a.push(Nav.Item("clip:dml-tc-c-kongruence")) },
        "priklad" to { a, s -> seed(s); a.push(Nav.Template("dml-tc-inv")) },
        "koniec" to { a, s -> seed(s); s.lessonsDone.remove("dml-tc-diofant"); s.finishLesson(kong); a.push(Nav.LessonEnd(kong)) },
        "klavesnica" to { a, s -> seed(s); a.push(Nav.Template("dml-tc-rea")) },
        "temy" to { a, s -> seed(s); s.courseId = "la1"; a.push(Nav.Topics) },
        "temy-dml" to { a, s -> seed(s); a.push(Nav.Topics) },
        "home-la1" to { _, s -> seed(s); s.courseId = "la1" },
        // the day before the LA1 test with no lesson of it: the first step of the quick way the tab offers
        "rychlo-la1" to { a, s -> seed(s); s.courseId = "la1"; a.tab = Tab.Nacvik; a.open(dev.hamper.tabula.screens.testNow(s, dev.hamper.tabula.screens.testProblems(s.course))!!.route, s) },
        // the REA lesson's two walks: Euclid's divisions, then the table
        "euklid" to { a, s -> seed(s); lesson(a, s, worked(rea, 0), id = rea) },
        "rea" to { a, s -> seed(s); lesson(a, s, worked(rea, 1), id = rea) },
        "gem" to { a, s -> seed(s); s.courseId = "la1"; lesson(a, s, LessonById.getValue(gemLesson).steps.indexOfFirst { it is Step.Explore && it.pic is Pic.Gem }, id = gemLesson) },
        "hodnotenie" to { a, s -> seed(s); a.push(Nav.MockGrade(s.course.papers.first().id)) },
        "opakovanie" to { a, s -> seed(s); a.push(Nav.Review) },
        // today's review dose worked through: its row on Dnes is gone, the round's end says it is done for today
        "opakovanie-hotovo" to { _, s -> seed(s, diofant = false); reviewedToday(s) },
        "opakovanie-koniec" to { a, s -> seed(s); reviewedToday(s); a.push(Nav.ReviewEnd(5, today = true)) },
        "aktualizacia" to { _, s ->
            seed(s)
            s.release = dev.hamper.tabula.system.Release(99999, "1.4.0", "2026-11-18", "0".repeat(64), 31_000_000, "https://fitagoras.hamper.dev/fitagoras.apk", "Nový vzhľad a nový názov: FITagoras.")
            s.snoozedCode = 0; s.snoozedUntil = 0
        },
    )

    @Test fun look() {
        val only = Snap.get("only")?.split(',')
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        for ((name, setup) in cases) if (only == null || only.any { name == it }) {
            // the end screen rises in on a stagger the capture does not wait for: shown as with "Remove animations"
            val still = name == "koniec" || name == "opakovanie-koniec"
            if (still) android.provider.Settings.Global.putFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
            try { shoot(name) { Seeded(setup) } } finally { if (still) android.provider.Settings.Global.putFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }
        }
    }

    /**
     * The worked solution of a paper's induction step alone (Nácvik DML, problem 1 of 4. 12. 2025): a chain of matrices
     * that has to wrap on a phone. `-Psnap.test=LookShots -Psnap.only=riesenie-ik`.
     */
    @Test fun solution() {
        val only = Snap.get("only")?.split(',')
        if (only != null && "riesenie-ik" !in only) return
        val p = dev.hamper.tabula.screens.paperById("dml-2025-e").problems.first()
        val sol = p.solution!!
        // the induction step alone, in a column that scrolls as the solution's screen does
        val steps = sol.steps.filter { it.aim.title.startsWith("Indukčný") }
        val ik = dev.hamper.tabula.data.Solution(dev.hamper.tabula.data.Postup(listOf(steps.first().aim)), steps)
        // the steps rise in on a stagger the capture does not wait for: shown as with "Remove animations"
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        android.provider.Settings.Global.putFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        try {
            shoot("riesenie-ik") {
                androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.fillMaxSize().background(dev.hamper.tabula.ui.T.c.bg).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp)) {
                    dev.hamper.tabula.screens.SolutionView(ik)
                }
            }
        } finally { android.provider.Settings.Global.putFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }
    }

    /** Every thing of today's review dose answered right in today's round, a minute ago. */
    private fun reviewedToday(s: Store) {
        for (it in dev.hamper.tabula.data.reviewToday(s).items) {
            s.attempts += Attempt(it.key, System.currentTimeMillis() - 60_000, true, false)
            s.reviewedNow(it.key)
        }
        s.save()
    }

    private fun lesson(a: AppState, s: Store, step: Int, vararg kept: Pair<String, String>, id: String = kong) {
        val l = LessonById.getValue(id)
        s.restartLesson(l, step)
        val run = s.run(l)
        for ((k, v) in kept) run.put(l.steps[step].id, k, v)
        a.push(Nav.Lesson(id))
    }

    private fun ms(daysAgo: Long, hour: Int = 10) = LocalDate.now().minusDays(daysAgo).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * The record above; [full] false: a new student who opened the app for the first time. [diofant] false: "Diofantické
     * rovnice" still ahead (Dnes: the lesson left after the open one; every lesson the course offers so far is done
     * otherwise, and Dnes would show the card for all done).
     */
    private fun seed(s: Store, full: Boolean = true, diofant: Boolean = true) {
        s.courseId = "dml"
        s.attempts.clear(); s.mistakes.clear(); s.lessonsDone.clear(); s.lessonAt.clear(); s.activeDays.clear(); s.doneOn.clear()
        s.mocks.clear(); s.vouched.clear(); s.unwritten.clear(); s.forgetReviewed(); s.release = null
        // no lesson open from an earlier capture (the store outlives it): finishing drops a lesson's run
        LessonById.keys.forEach { s.finishLesson(it) }
        // and no quick way before the test begun (data/BeforeTest.kt)
        (s.lessonAt.keys + s.lessonTouched.keys).filter { it.startsWith("rychlo-") }.toSet().forEach { s.finishLesson(it) }
        s.lessonsDone.clear(); s.doneOn.clear(); s.activeDays.clear()
        s.terms["la1-test2"] = LocalDate.now().plusDays(1).atTime(18, 0).let { dev.hamper.tabula.compat.time.LocalDateTime.parse(it.toString()) }
        s.statsNoticed = full // a returning student has answered the statistics notice; a new one sees it
        s.symbolHintSeen = false // the line about tappable symbols, as on a first statement (an earlier capture saw it)
        if (!full) { s.lessonAt.clear(); s.save(); return }
        for (i in 1L..6L) s.activeDays += LocalDate.now().minusDays(i).toString()
        listOf("dml-mi-slaba", "dml-mi-zapis", "dml-mi-nerovnosti", "dml-mi-silna", "dml-mi-matice", "dml-mz-mnoziny", "dml-mz-identity", "dml-mz-zobrazenia",
            "dml-mz-injsurj", "dml-mz-skladanie", "dml-mz-mohutnost", "dml-tc-delitelnost", "dml-tc-rea", "dml-tc-diofant").forEach { s.lessonsDone += it }
        if (!diofant) s.lessonsDone.remove("dml-tc-diofant")
        val l = LessonById.getValue(kong)
        s.restartLesson(l, 5); s.lessonAt[kong] = 5
        fun ex(gen: String) = Generators.getValue(gen)(Random(0))
        // last Friday twice wrong: the weak spot of "Dnes ešte"
        val inj = ex("dml-mz-injsurj")
        val friday = (1L..7L).first { LocalDate.now().minusDays(it).dayOfWeek == java.time.DayOfWeek.FRIDAY }
        s.attempts += Attempt("gen:${inj.template}", ms(friday, 16), false, false)
        s.attempts += Attempt("gen:${inj.template}", ms(friday, 17), false, false)
        s.mistakes += Mistake("gen:${inj.template}", inj.label, "Chyba v príklade: ${inj.src}", "gen:${inj.template}", ms(friday, 17))
        // yesterday: one wrong, one with help
        val rea = ex("dml-tc-rea")
        s.attempts += Attempt("gen:${rea.template}", ms(1), false, false)
        s.mistakes += Mistake("gen:${rea.template}", rea.label, "Chyba v príklade: ${rea.src}", "gen:${rea.template}", ms(1))
        val del = ex("dml-mi-del")
        s.attempts += Attempt("gen:${del.template}", ms(2), true, true)
        s.save()
    }
}

@Composable
private fun Seeded(setup: (AppState, Store) -> Unit) {
    val ctx = LocalContext.current
    val store = remember { Tabula.store(ctx) }
    val app = remember { AppState().also { setup(it, store) } }
    val clips = remember { ClipHost(ExoClipPlayer(ctx), onPip = {}) }
    DisposableEffect(clips) { onDispose { clips.release() } }
    App(app, store, clips, {}, {})
}

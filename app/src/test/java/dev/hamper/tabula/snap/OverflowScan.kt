package dev.hamper.tabula.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.Courses
import androidx.compose.ui.unit.sp
import dev.hamper.tabula.math.TexBlock
import dev.hamper.tabula.ui.panel
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.math.MathOverflow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import dev.hamper.tabula.ui.ConceptCard
import dev.hamper.tabula.ui.Dokazujes
import dev.hamper.tabula.ui.LocalUnfoldAll
import dev.hamper.tabula.ui.TheoremCard
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every step of every lesson of both courses, rendered at 320 dp and 200 % (the narrowest phone, the largest font):
 * no formula may be wider than its card. Inline math wraps, display math shrinks to 75 % at most and then scrolls
 * inside its own box; anything else is an overflow and fails here with the step and the formula.
 * Run: `./gradlew :app:snap -Psnap.test=OverflowScan*` (both classes; PNGs of every step land in -Psnap.out).
 */
/**
 * Textbook display formulas in statements wider than a 320 dp card, waiting for Teo's approval of the typesetting, per
 * course. LA1: the system of the proof of the HST theorem (Věta 3.7, "α₁a₁ⱼ₁ = 0 … + αₖaₖⱼₖ = 0."), 757 px on a
 * 744 px card at 320 dp since the card of the new look (18. 11.): shown at 98 %, its lines are the skriptá's and stay.
 */
private val PendingSadzbaBy = mapOf("la1" to 1)
private fun pendingSadzba(scope: List<dev.hamper.tabula.data.Course>) = scope.sumOf { PendingSadzbaBy[it.id] ?: 0 }

class OverflowScan : Shots() {
    @Test fun noFormulaRunsPastItsCard() {
        val look = Look(false, 320, Pixel.second, 2f)
        val dir = File(Snap.out, "overflow")
        val bad = ArrayList<String>()
        MathOverflow.on = true
        try {
            for (c in Courses) for (l in c.lessons) for (i in l.steps.indices) {
                MathOverflow.found.clear()
                val route = "lesson:${l.id}:$i"
                shoot(File(dir, "${l.id}-$i-${look.tag}.png"), look) { AppAt(route, c.id) }
                MathOverflow.found.distinct().forEach { bad += "$route (${l.steps[i].javaClass.simpleName}): $it" }
            }
        } finally { MathOverflow.on = false }
        assertTrue("${bad.size} formulas run past their card at 320 dp / 200 %:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

}

/**
 * The statements of [scope]'s courses, in a class (and a JVM, forkEvery = 1) per course: the window stopped drawing
 * after some fifty tall captures in one process (the grey of an empty window, no exception), so no JVM takes more than
 * one course's cards. Run all of them with `-Psnap.test=OverflowScan*`.
 */
abstract class StatementScan(private val scope: List<dev.hamper.tabula.data.Course>) : Shots() {
    /**
     * Statements on the narrowest phone at the normal font (Teo, 16. 11.): every display formula of every theorem,
     * definition and remark card, folds open, and of every "Dokazuješ" bar (its core and each part) should fit its card
     * at full size; a formula that would be shrunk or broken into rows is restructured (an aligned table, parallel
     * things under each other, a core first), never shrunk. Cores and parts are ours: any that does not fit fails.
     * The textbook's own display formulas wait for Teo's approval of the mobile typesetting (docs/ZASADY.md, bod 11):
     * they are listed in build/statements-squeezed.txt and may only get fewer ([PendingSadzbaBy]).
     */
    @Test fun statementsFitUnbroken() {
        // 1600 dp is tall enough for five cards (a taller capture only grew the bitmaps); the layout, which is what this
        // measures, is the same at any height
        val look = Look(false, 320, 1600, 1f)
        val dir = File(Snap.out, "statements-" + scope.joinToString("-") { it.id })
        val ours = Courses.flatMap { c ->
            c.theorems.flatMap { t -> listOfNotNull(t.wording.core) + t.wording.parts.map { it.tex } } +
                c.proofs.flatMap { p -> p.wording?.let { w -> listOfNotNull(w.core) + w.parts.map { it.tex } }.orEmpty() }
        }.map { it.trim() }.toSet()
        val items = ArrayList<Pair<String, @androidx.compose.runtime.Composable () -> Unit>>()
        for (c in scope) {
            for (t in c.theorems) items += t.id to { TheoremCard(t) }
            for (k in c.concepts) items += k.id to { ConceptCard(k) }
            for (p in c.proofs) {
                val w = p.what ?: continue
                for (r in (listOf(0) + w.parts.map { it.from }).distinct()) items += "${p.id}@$r" to { CompositionLocalProvider(LocalUnfoldAll provides false) { Dokazujes(w, r) } }
            }
        }
        // -Psnap.only=dml-tc: only the items whose id has that in it (a rerun of what Robolectric left blank)
        Snap.get("only")?.let { o -> items.retainAll { o in it.first } }
        val squeezed = LinkedHashMap<String, String>() // tex -> where
        val blank = ArrayList<String>()
        MathOverflow.on = true
        try {
            // a few cards a capture: fewer captures, fewer of Robolectric's empty frames; an empty one is taken again
            // one capture of [batch] as statements-[name].png, taken again when empty; false when it stayed empty
            fun capture(name: String, batch: List<Pair<String, @androidx.compose.runtime.Composable () -> Unit>>): Boolean {
                val names = batch.joinToString(", ") { it.first }
                System.gc() // ~220 captures in one run: later ones came out blank under memory pressure
                for (attempt in 1..4) {
                    MathOverflow.squeezed.clear()
                    try {
                        shoot(File(dir, "statements-$name.png"), look) {
                            Column(Modifier.fillMaxSize().background(T.c.bg).padding(horizontal = 20.dp, vertical = 16.dp)) {
                                CompositionLocalProvider(LocalUnfoldAll provides true) { batch.forEach { (_, draw) -> draw(); Spacer(Modifier.height(12.dp)) } }
                            }
                        }
                    } catch (e: AssertionError) {
                        continue
                    }
                    MathOverflow.squeezed.distinct().forEach { squeezed.putIfAbsent(it, names) }
                    return true
                }
                return false
            }
            // a batch that stays empty (Robolectric leaves some combinations blank, the cards alone render) is taken
            // card by card: only a card that stays empty on its own counts as not rendered
            items.chunked(5).forEachIndexed { n, batch ->
                if (!capture("$n", batch)) batch.forEachIndexed { i, item -> if (!capture("$n-$i", listOf(item))) blank += item.first }
            }
        } finally { MathOverflow.on = false }
        fun texOf(entry: String) = entry.substringAfter(": ").removeSuffix("\\text{.}").removeSuffix("\\text{,}").trim()
        val (mine, pending) = squeezed.entries.partition { texOf(it.key) in ours || texOf(it.key).removeSuffix(".") in ours }
        File("build/statements-squeezed-" + scope.joinToString("-") { it.id } + ".txt").writeText(pending.joinToString("\n") { "${it.value}: ${it.key}" })
        // both at once: a card Robolectric left blank must not hide what the rendered ones say
        val tooWide = if (mine.isEmpty()) "" else "${mine.size} cores or parts do not fit a 320 dp card at full size:\n" + mine.joinToString("\n") { "${it.value}: ${it.key}" }
        assertTrue(listOf(tooWide, if (blank.isEmpty()) "" else "not rendered: $blank").filter { it.isNotEmpty() }.joinToString("\n"), blank.isEmpty() && mine.isEmpty())
        assertTrue("${pending.size} textbook formulas do not fit 320 dp, more than the ${pendingSadzba(scope)} waiting for the typesetting (build/statements-squeezed-<course>.txt)", pending.size <= pendingSadzba(scope))
        assertTrue("only ${pending.size} textbook formulas wait for the typesetting now: lower PendingSadzbaBy to ${pending.size}", pending.size >= pendingSadzba(scope))
        // last: a card Robolectric left blank is not measured, but the overflow verdict above stands for every other card
        assertTrue("not rendered (Robolectric left them blank, not an overflow): $blank", blank.isEmpty())
    }

}

class OverflowScanStatementsLa1 : StatementScan(Courses.filter { it.id == "la1" })
class OverflowScanStatementsDml : StatementScan(Courses.filter { it.id != "la1" })

/** The widest formulas, in a JVM of their own (see [StatementScan]). */
class OverflowScanWidest : Shots() {
    private val worst = listOf(
        """A^{n+1}=A^n\cdot A\stackrel{IP}{=}\begin{pmatrix}(-1)^n&3n(-1)^{n-1}\\0&(-1)^n\end{pmatrix}\begin{pmatrix}-1&3\\0&-1\end{pmatrix}=\begin{pmatrix}(-1)^{n+1}&3(-1)^n+3n(-1)^n\\0&(-1)^{n+1}\end{pmatrix}""",
        """\left\{\left(x_0 + k \cdot \frac{m}{\gcd(a,m)}\right) \bmod m \ \Big|\ k\in\{0,1,\dots,\gcd(a,m)-1 \}\right\},""",
        """\begin{pmatrix|3}1&4&3&v_1\\2&2&4&v_2\\3&3&2&v_3\end{pmatrix}\sim\begin{pmatrix|3}1&4&3&v_1\\0&4&3&3v_1+v_2\\0&1&3&2v_1+v_3\end{pmatrix}""",
        """(\exists\alpha_1,\dots,\alpha_m\in T)(\exists k\in\hat{m})\Big(\alpha_k\neq 0\wedge\sum_{i=1}^m\alpha_i\mathbf{x}_i=\theta\Big)""",
    )

    /** The widest display formulas of DML and LA1 as worked lines: shrunk to 75 % at most, the rest scrolls. */
    @Test fun widest() = shoot("floor-widest") {
        Column(Modifier.fillMaxSize().background(T.c.bg).padding(horizontal = 20.dp, vertical = 30.dp)) {
            // as a worked line: a card with 12 dp padding, 18 sp
            for (t in worst) {
                Column(Modifier.fillMaxWidth().padding(bottom = 8.dp).panel(16.dp).padding(horizontal = 12.dp, vertical = 10.dp)) { TexBlock(t, size = 18.sp, color = T.c.ink) }
            }
        }
    }
}

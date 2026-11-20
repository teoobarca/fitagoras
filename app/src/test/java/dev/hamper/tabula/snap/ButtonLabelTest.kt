package dev.hamper.tabula.snap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.data.Courses
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.ui.ButtonRow
import dev.hamper.tabula.ui.Cta
import dev.hamper.tabula.ui.LabelAudit
import dev.hamper.tabula.ui.Second
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * No button label is ever cut (`./gradlew :app:snap -Psnap.test=ButtonLabelTest`): at 200 % text on a 360 dp phone,
 * every literal Cta / Second label of the app in the narrowest place it can get (half of a [ButtonRow]), and the real
 * screens (each step type, the tabs, settings, papers, a generated exercise). A label laid out with an ellipsis or
 * overflow, or wrapped through the middle of a word, fails the test with its text; so does any text on those screens
 * with a line break inside a date, a range, a number and its unit or a name and its number ("Body 1 až / 3").
 */
class ButtonLabelTest : Shots() {
    private val look = Look(dark = false, widthDp = Narrow.first, heightDp = Narrow.second, fontScale = 2f)
    private val out = File(Snap.get("out") ?: "build/snap/labels").absoluteFile

    @Before fun on() { LabelAudit.cut.clear(); LabelAudit.broken.clear(); LabelAudit.split.clear(); LabelAudit.on = true }
    @After fun off() { LabelAudit.on = false }

    /**
     * Every `Cta("…")` / `Second("…")` written out in the main sources of both modules (labels built from data are
     * covered by the screens). Since Compose Multiplatform the screens are in shared/src/commonMain, not in app/.
     */
    private fun labels(): List<String> {
        val root = generateSequence(File("").absoluteFile) { it.parentFile }.first { File(it, "settings.gradle.kts").isFile }
        val srcs = File(root, "shared/src").listFiles { f -> f.isDirectory && f.name.endsWith("Main") }!!.toList() + File(root, "app/src/main")
        val re = Regex("""\b(?:Cta|Second)\("([^"$\\]+)"""")
        return srcs.flatMap { it.walk() }.filter { it.extension == "kt" }.flatMap { f -> re.findAll(f.readText()).map { it.groupValues[1] } }.toSortedSet().toList()
    }

    /** The audit itself: a label forced onto one line in too little room must be reported, or the other tests prove nothing. */
    @Test
    fun auditSeesACutLabel() {
        shoot(File(out, "probe-${look.tag}.png"), look) {
            Column(Modifier.padding(20.dp).then(Modifier.width(80.dp))) {
                androidx.compose.material3.Text(
                    "Skontrolovať", maxLines = 1, softWrap = false, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    onTextLayout = { LabelAudit.check("probe", it) },
                )
                // a range broken by its line (a plain Text, past the typography of Rich and the labels)
                androidx.compose.material3.Text("Body 1 až 3", onTextLayout = { LabelAudit.split(it) })
            }
        }
        assertTrue("the audit missed an ellipsized label", "probe" in LabelAudit.cut)
        assertTrue("the audit missed a range broken across lines", LabelAudit.split.isNotEmpty())
    }

    @Test
    fun everyLabelInHalfARow() {
        val all = labels()
        assertTrue("found only ${all.size} labels: the source scan is broken", all.size > 20)
        // one tall window holds them all: each label twice, as the quiet and the main half of a row (side by side up
        // to 130 % text, stacked above it)
        for (f in listOf(1.3f, 2f)) {
            val tall = look.copy(heightDp = 150 * all.size + 200, fontScale = f)
            shoot(File(out, "labels-${tall.tag}.png"), tall) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (l in all) ButtonRow { Second(l, {}); Cta(l, {}) }
                }
            }
        }
        assertTrue("cut button labels: ${LabelAudit.cut}", LabelAudit.cut.isEmpty())
        assertTrue("button labels with a word broken in two: ${LabelAudit.broken}", LabelAudit.broken.isEmpty())
    }

    @Test
    fun screens() {
        val routes = mutableListOf("home", "cesta", "nacvik", "settings", "papers", "cvicenia", "priklad:1", "priklad:2", "priklad:3", "where")
        val seen = mutableSetOf<String>()
        for (c in Courses) for (l in c.lessons) l.steps.forEachIndexed { i, s -> if (seen.add(s::class.simpleName!!)) routes += "lesson:${l.id}:$i" }
        routes += "gen:${Generators.keys.first()}"
        for (r in routes) shoot(File(out, "${r.replace(Regex("[^A-Za-z0-9._-]+"), "_")}-${look.tag}.png"), look) { AppAt(r) }
        assertTrue("cut button labels at 200 % on ${routes.size} screens: ${LabelAudit.cut}", LabelAudit.cut.isEmpty())
        assertTrue("button labels with a word broken in two on ${routes.size} screens: ${LabelAudit.broken}", LabelAudit.broken.isEmpty())
        assertTrue("a date, range or number broken across lines at 200 % on ${routes.size} screens: ${LabelAudit.split}", LabelAudit.split.isEmpty())
    }
}

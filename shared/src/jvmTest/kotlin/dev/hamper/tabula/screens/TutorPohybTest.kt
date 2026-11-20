package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.Env
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.Odpoved
import dev.hamper.tabula.tutor.Pripojenie
import dev.hamper.tabula.tutor.ResponsesKlient
import dev.hamper.tabula.tutor.Spotreba
import dev.hamper.tabula.tutor.TutorSluzba
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * The tutor's motion frame by frame on a virtual clock (docs/TUTOR.md, "Pohyb"): a model that thinks, calls a tool and
 * streams its answer token by token as ChatGPT does, the real panel and step, a frame every 40 ms. Writes the frames
 * for a strip (`-Ppohyb.out=DIR`, scripts/pas.py), and is skipped without it: it is a look, not a gate.
 */
@OptIn(ExperimentalTestApi::class)
class TutorPohybTest {
    @Before fun setUp() { Env.install(debug = true, clipsGuestToken = "", versionName = "test"); installJvmAssets() }

    private val out = System.getProperty("pohyb.out")?.takeIf { it.isNotBlank() }?.let(::File)

    /** A page of "handwriting" for the thumbnails: a light sheet with a few lines. */
    private fun papier(): ByteArray {
        val img = java.awt.image.BufferedImage(300, 400, java.awt.image.BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.color = java.awt.Color(0xF4, 0xF1, 0xE8); g.fillRect(0, 0, 300, 400)
        g.color = java.awt.Color(0x2A, 0x3A, 0x6A); g.stroke = java.awt.BasicStroke(3f)
        for (i in 0 until 9) g.drawLine(30, 50 + i * 36, 120 + (i * 53) % 150, 50 + i * 36 + (i % 3))
        g.dispose()
        return java.io.ByteArrayOutputStream().also { javax.imageio.ImageIO.write(img, "png", it) }.toByteArray()
    }

    /**
     * ChatGPT as the phone sees it: [mysli] ms before the first token, tokens of ~4 characters every [tokenMs] ms in
     * the bursts a mobile network delivers; a tool round first when [nastroj] is set.
     */
    inner class PomalyTutor(private val odpoved: String, private val nastroj: String? = null, private val prepisTextu: String = "", private val posudok: String = "",
                            private val mysli: Long = 1400, private val tokenMs: Long = 28) : TutorSluzba {
        override val pripojenie = MutableStateFlow<Pripojenie>(Pripojenie.Pripojeny("student@example.com", true))
        override fun pripoj(suhlas: Boolean) {}
        override fun odpoj() {}
        override suspend fun modely() = listOf("gpt-6.2-sol", "gpt-6.1-terra")
        override fun spravovatVyuzitie() {}
        override var poucenyOPlane = true
        override var vyvojar = false
        override fun drz(dovod: String?) {}

        private suspend fun pis(t: String, onText: (String) -> Unit) {
            var i = 0; var n = 0
            while (i < t.length) {
                val k = minOf(t.length, i + 3 + (n % 3)); onText(t.substring(i, k)); i = k; n++
                // a burst every few tokens, as a phone on mobile data gets them
                delay(if (n % 6 == 0) tokenMs * 5 else tokenMs / 2)
            }
        }

        override fun klient(): ResponsesKlient = object : ResponsesKlient {
            override suspend fun odpoved(telo: JSONObject, onText: (String) -> Unit): Odpoved {
                val schema = telo.optJSONObject("text")?.optJSONObject("format")?.optString("name")
                if (schema == null && nastroj != null && telo.optJSONArray("tools") != null && telo.opt("tool_choice") == null) {
                    val inp = telo.optJSONArray("input")
                    val uz = inp != null && (0 until inp.length()).any { inp.optJSONObject(it)?.optString("type") == "function_call_output" }
                    if (!uz) { delay(mysli); return Odpoved("", "gpt-6.2-sol", Spotreba(3000, 0, 50), -1, 150, null,
                        listOf(JSONObject().put("type", "function_call").put("call_id", "c1").put("name", nastroj).put("arguments", "{}"))) }
                }
                return when (schema) {
                    "prepis_textu" -> { delay(2600); Odpoved(prepisTextu, "gpt-6.1-terra", Spotreba(), -1, 2600, null) }
                    "posudok" -> { delay(700); pis(posudok, onText); Odpoved(posudok, "gpt-6.2-sol", Spotreba(), 700, 3000, null) }
                    else -> { delay(mysli); pis(odpoved, onText); Odpoved(odpoved, "gpt-6.2-sol", Spotreba(), mysli, 4000, null) }
                }
            }
            override suspend fun modely() = listOf("gpt-6.2-sol", "gpt-6.1-terra")
        }

        @Composable
        override fun Fotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit) {
            LaunchedEffect(Unit) { onHotovo(listOf(papier())) }
        }
    }

    private val odpoved = "Krok je správny, lebo pripočítať násobok iného riadku nemení riadkový priestor matice. " +
        "Preto sa nezmení ani hodnosť: $\\operatorname{rank} \\mathbf{A} = \\operatorname{rank} \\mathbf{A}'$.\n\n" +
        "Skús si to na druhom riadku: od neho odčítaš dvojnásobok prvého a dostaneš nulu v prvom stĺpci. " +
        "Tá nula je to, o čo ti ide, lebo z nej sa skladá stupňovitý tvar."

    private val dokaz = dev.hamper.tabula.data.ProofById.getValue("p-dim3")
    private val prepisDokazu = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"riadky":["Báza = LN a generuje, LN je predpoklad.","Ak by negeneroval, pridám v a mám 4 LN vektory v P, spor s dim P = 3."],"nejasne":[]}"""
    private val posudokDokazu = """{"verdikt":"s_vyhradou","text":"Uznal by som to s výhradou: spor s dimenziou je presne to, čo treba. Chýba ale veta, prečo je rozšírený súbor stále LN – napíš, že $\\mathbf{v} \\notin \\langle M \\rangle$, a potom to sedí.","body_splnene":[1,2,4],"istota":"vysoka"}"""


    @Test fun answerStreams() {
        assumeTrue("frames only with -Ppohyb.out", out != null)
        for (dark in listOf(false, true)) runDesktopComposeUiTest(width = 900, height = 1700) {
            val t = PomalyTutor(odpoved, nastroj = "tvrdenie")
            val m = dev.hamper.tabula.tutor.MiestoAppky("Hodnosť matice", "la1", "Krok lekcie: riadkové úpravy.")
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(2f)) {
                    TabulaTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) {
                        Box(Modifier.size(411.dp, 820.dp).testTag("ram")) { TutorPanel(m, false, { _, _ -> }, {}) }
                    } }
                }
            }
            waitForIdle()
            onNode(hasSetTextAction()).performTextInput("Prečo je tento krok správny?")
            waitForIdle()
            mainClock.autoAdvance = false
            onNode(hasContentDescription("Poslať")).performClick()
            frames(if (dark) "odpoved-dark" else "odpoved", 9000)
        }
    }

    @Test fun paperVerdictInTheStep() {
        assumeTrue("frames only with -Ppohyb.out", out != null)
        for (dark in listOf(false, true)) runDesktopComposeUiTest(width = 900, height = 1700) {
            val t = PomalyTutor("", nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(2f)) {
                    TabulaTheme(if (dark) ThemeMode.Dark else ThemeMode.Light) { CompositionLocalProvider(LocalTutor provides t) {
                        Box(Modifier.size(411.dp, 600.dp).testTag("ram")) {
                            Column(Modifier.width(371.dp)) { PapierKontrola({ dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)) }) { _, _ -> } }
                        }
                    } }
                }
            }
            waitForIdle()
            mainClock.autoAdvance = false
            onAllNodes(hasText("Odfoť riešenie", substring = true) and hasClickAction()).onFirst().performClick()
            frames(if (dark) "papier-dark" else "papier", 9000)
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.frames(name: String, ms: Long) {
        val dir = File(out, name).also { it.mkdirs() }
        var t = 0L
        while (t <= ms) {
            javax.imageio.ImageIO.write(onNodeWithTag("ram").captureToImage().toAwtImage(), "png", File(dir, "%05d.png".format(t)))
            mainClock.advanceTimeBy(40); t += 40
        }
    }
}

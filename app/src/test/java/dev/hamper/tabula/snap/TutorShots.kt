package dev.hamper.tabula.snap

import kotlinx.coroutines.flow.first
import androidx.compose.runtime.CompositionLocalProvider
import dev.hamper.tabula.la.Field
import dev.hamper.tabula.la.intMat
import dev.hamper.tabula.tutor.GemUloha
import dev.hamper.tabula.tutor.Kontext
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.UkazkovyTutor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.junit.Test

/**
 * The tutor's panel with a fake model and the real engine (Teo's own photographed GEM, a wrong 0 in the 3rd step):
 * `./gradlew :app:snap -Psnap.test=TutorShots -Psnap.theme=light,dark -Psnap.out=/tmp/tutor`.
 */
class TutorShots : Shots() {
    private val uloha = GemUloha(Field.R, intMat(listOf(2, 3, 2, 2), listOf(1, 1, 1, 0), listOf(3, 1, 2, -1), listOf(4, 2, 1, 5)))
    private val kontext = Kontext("Spočtěte hodnost matice A.", "h(A) =", "4", "3", "Priveľa: v HST ti ostal nenulový riadok.", listOf("Uprav na HST"), listOf(uloha))

    private fun prepis(d: String = "0", c: String = "\"0\",\"+2\",\"+1\",\"+1\"") = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"bloky":[{"druh":"gem","strana":1,"popis":null,
        "matice":[{"riadky":[["2","3","2","2"],["1","1","1","0"],["3","1","2","-1"],["4","2","1","5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["2","3","2","2"],["3","1","2","-1"],["0","-2","-3","5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],[$c],["0","2","3","-5"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],["0","0","$d","-3"],["0","0","3","-9"]],"ciara":null,"preskrtnuta":false,"oblast":null},
        {"riadky":[["1","1","1","0"],["0","1","0","2"],["0","0","3","-9"],["0","0","0","1"]],"ciara":null,"preskrtnuta":false,"oblast":null}],
        "operacie":[null,null,null,null],"nejasne":[],"zaver":null}]}"""

    private val vysvetlenie = "V 3. úprave si od 3. riadku odčítal dvojnásobok 2. riadku, $\\text{ř}3-2\\cdot\\text{ř}2$. V treťom stĺpci je to $1-2\\cdot 0=1$, nie $0$. " +
        "Kvôli tomu ti v HST vyšli štyri nenulové riadky. Oprav túto bunku a pokračuj z opravenej matice."

    /** The engine asks instead of marking: a cell the next step contradicts. */
    @Test fun panelOtazka() = shoot("tutor-panel-otazka") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis("1", "\"0\",\"+2\",\"+9\",\"+1\""), "Jedno číslo v 3. matici neviem prečítať isto.", vyvojar = false, nastroj = "over_postup_gem")) {
            dev.hamper.tabula.screens.TutorPanel(gemMiesto, true, { _, _ -> }, {})
        }
    }

    @Test fun panelLimit() = shoot("tutor-panel-limit") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), chyba = TutorChyba(TutorChyba.Druh.LIMIT, "subscription_sharing_usage_limit_exceeded", "limit", 429), vyvojar = false, nastroj = "over_postup_gem")) {
            dev.hamper.tabula.screens.TutorPanel(gemMiesto, true, { _, _ -> }, {})
        }
    }

    /* ---- the general tutor (docs/TUTOR.md, "Všeobecný tútor") ---- */

    private val gemMiesto get() = dev.hamper.tabula.tutor.MiestoAppky("Hodnosť matice", "la1", "Príklad: hodnosť matice.", gem = kontext, odpoved = "4")

    /** A photographed GEM after a wrong answer: the engine's verdict and stepper, the model's words under it. */
    @Test fun panelGem() = shoot("tutor-panel-gem") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vysvetlenie, vyvojar = false, nastroj = "over_postup_gem")) { dev.hamper.tabula.screens.TutorPanel(gemMiesto, true, { _, _ -> }, {}) }
    }

    private val dokaz = dev.hamper.tabula.data.ProofById.getValue("p-dim3")
    private val prepisDokazu = """{"strany":[{"strana":1,"citatelnost":"ok","otocenie":0,"problem":null}],"riadky":["Báza = LN a generuje, LN je predpoklad.","Treba §\\langle x_1,x_2,x_3\\rangle = P§.","§\\subseteq§: §x_i \\in P§, §P§ je podpriestor, teda každá LK je v §P§.","§\\supseteq§: nech §v \\in P§, §v \\notin \\langle x_1,x_2,x_3\\rangle§, potom §(x_1,x_2,x_3,v)§ je LN.","To je spor."],"nejasne":[]}""".replace('§', '$')
    private val posudokDokazu = """{"verdikt":"neuznal_by","text":"Takto by ti to neuznali: prvá inklúzia je v poriadku a opačnú si začal správne, ale nedotiahol si ju do sporu. Napísal si „potom §(x_1,x_2,x_3,v)§ je LN“ – doplň prečo (Veta 2.42: pridaný vektor mimo obalu) a hlavne s čím je to v spore: v §P§ s §\\dim P=3§ je každý súbor dĺžky 4 LZ. Bez toho chýba záver, že súbor generuje §P§.","body_splnene":[1,2,3],"istota":"vysoka"}""".replace('§', '$')

    /** A proof on paper: the checklist judged item by item, with what the student wrote. */
    @Test fun panelPapier() = shoot("tutor-panel-papier") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), "Prvé tri body máš. Pri opačnej inklúzii chýba zdôvodnenie, prečo je súbor LN, a celý spor s dimenziou. <veta cislo=\"Věta 2.42\"/>", vyvojar = false,
            nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)) {
            dev.hamper.tabula.screens.TutorPanel(dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)), true, { _, _ -> }, {})
        }
    }

    /** The panel opened without photos: what it offers. */
    @Test fun panelOtazky() = shoot("tutor-panel-otazky") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false)) { dev.hamper.tabula.screens.TutorPanel(dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz)), false, { _, _ -> }, {}) }
    }

    /** The first use: what is sent, the sign-in. */
    @Test fun panelPrihlasenie() = shoot("tutor-panel-prihlasenie") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false, pripojenie = dev.hamper.tabula.tutor.Pripojenie.Nepripojeny)) { dev.hamper.tabula.screens.TutorPanel(gemMiesto, false, { _, _ -> }, {}) }
    }

    /** A lesson step: the entry in its header. */
    @Test fun krok() = shoot("tutor-krok") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false)) { AppAt("proof:p-dim3") }
    }

    @Test fun nastavenia() = shoot("tutor-nastavenia") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis())) { AppAt("settings") }
    }

    /* ---- the paper's check, two ways (Teo, 18. 11.: "nevidím to zadanie") ---- */

    /** What the step shows around the check: the theorem, the title, the checklist (as ProofFlow draws it). */
    @androidx.compose.runtime.Composable
    private fun KrokPapier(stred: @androidx.compose.runtime.Composable () -> Unit) {
        val c = dev.hamper.tabula.ui.T.c
        androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.fillMaxSize().background(c.bg).statusBarsPadding().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp)) {
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(56.dp))
            dev.hamper.tabula.ui.Label("Dôkaz · na papier")
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(8.dp))
            dev.hamper.tabula.ui.SourceCard(dokaz.src) { dev.hamper.tabula.math.Rich((dokaz.what?.lines?.joinToString(" ") { dev.hamper.tabula.data.lineText(it) } ?: dokaz.title).replace('§', '$'), color = c.ink) }
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(14.dp))
            dev.hamper.tabula.ui.H1("Porovnaj so svojím papierom")
            androidx.compose.foundation.layout.Spacer(androidx.compose.ui.Modifier.height(14.dp))
            stred()
        }
    }

    private val miestoDokazu get() = dev.hamper.tabula.tutor.miesto(dokaz, dev.hamper.tabula.tutor.polozkyDokazu(dokaz))

    /** Built: the judgement in the step, under the theorem, above the checklist; the conversation only when asked. */
    @Test fun papierVKroku() = shoot("tutor-papier-v-kroku") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false, nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)) {
            KrokPapier { dev.hamper.tabula.screens.PapierKontrola({ miestoDokazu }, otvorFotak = true) { _, _ -> } }
        }
    }

    /**
     * The conversation after the judgement ("Opýtať sa ďalej"): the paper's verdict card on top, the follow-up and its
     * answer with a theorem card, the suggestions by the field (for the app's page, landing SHOW_TUTOR).
     */
    @Test fun rozhovor() = shoot("tutor-rozhovor") {
        val t = UkazkovyTutor(prepis(), ("Spor dotiahneš cez dimenziu. Súbor §(x_1,x_2,x_3,v)§ má štyri vektory a leží v §P§, kde §\\dim P = 3§. " +
            "V priestore dimenzie 3 je každý súbor dĺžky 4 lineárne závislý, takže nemôže byť LN.\n\nPrečo je LN, hovorí táto veta: <veta cislo=\"Věta 2.42\"/>").replace('§', '$'),
            vyvojar = false, nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)
        CompositionLocalProvider(LocalTutor provides t) {
            val stav = androidx.compose.runtime.remember { dev.hamper.tabula.screens.TutorStav() }
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                stav.opytaj(t, scope, miestoDokazu, null, listOf(ByteArray(0))) { _, _ -> }
                androidx.compose.runtime.snapshotFlow { stav.bezi }.first { !it }
                stav.opytaj(t, scope, miestoDokazu, "Ako mám dotiahnuť ten spor?", emptyList()) { _, _ -> }
            }
            dev.hamper.tabula.screens.TutorPanel(miestoDokazu, false, { _, _ -> }, {}, stav)
        }
    }

    /** The same step as the app draws it now: the photo, one tap without it, the checklist folded away. */
    @Test fun papierJednaCesta() = shoot("tutor-papier-jedna-cesta") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false)) {
            KrokPapier { dev.hamper.tabula.screens.PorovnanieSPapierom({ miestoDokazu }, dev.hamper.tabula.tutor.polozkyDokazu(dokaz), null, { _, _ -> }) }
        }
    }

    /** Without ChatGPT: "Máš to na papieri?" and two answers, nothing else. */
    @Test fun papierBezTutora() = shoot("tutor-papier-bez-tutora") {
        KrokPapier { dev.hamper.tabula.screens.PorovnanieSPapierom({ miestoDokazu }, dev.hamper.tabula.tutor.polozkyDokazu(dokaz), dev.hamper.tabula.screens.Hodnotenie.MAM, { _, _ -> }) }
    }

    /** Not built, for comparison: the panel as a sheet over two thirds of the step, the theorem still in view above it. */
    @Test fun papierPanelZdola() = shoot("tutor-papier-panel-zdola") {
        CompositionLocalProvider(LocalTutor provides UkazkovyTutor(prepis(), vyvojar = false, nastroj = "posud_papier", prepisTextu = prepisDokazu, posudok = posudokDokazu)) {
            androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize()) {
                KrokPapier { dev.hamper.tabula.ui.Second("Odfoť riešenie, Pytagoras ho skontroluje", {}) }
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f)))
                androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxWidth().fillMaxHeight(0.62f).align(androidx.compose.ui.Alignment.BottomCenter)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
                    dev.hamper.tabula.screens.TutorPanel(miestoDokazu, true, { _, _ -> }, {})
                }
            }
        }
    }
}

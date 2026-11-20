@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_camera
import dev.hamper.tabula.ui.Cta
import dev.hamper.tabula.ui.H1
import dev.hamper.tabula.ui.Quiet
import dev.hamper.tabula.ui.Second
import dev.hamper.tabula.ui.Sub
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.Text
import dev.hamper.tabula.web.jsLog
import dev.hamper.tabula.web.jsOpen
import dev.hamper.tabula.web.jsOpenNow
import dev.hamper.tabula.web.jsOpenSoon
import dev.hamper.tabula.web.jsPickPhotos
import dev.hamper.tabula.web.jsReadClipboard
import kotlinx.browser.localStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64

/**
 * The tutor in the browser (Teo, 18. 11.: "z webu pre iPhone"): the same panels and pipeline as the phone, with the
 * sign-in finished by a pasted address ([WebSiwc]) and photos from the browser's own picker (camera or library).
 * This path is not one OpenAI's docs describe for a web page; when it stops working, every call fails as a TutorChyba
 * and the screens fall back to the way without AI ("Mám to / Nemám"). docs/TUTOR.md, "Web".
 */
object WebTutor : TutorSluzba {
    private val log = Zaznam { u, t -> jsLog("[tutor $u] $t") }
    private val siwc = WebSiwc(log)
    private val scope = MainScope()
    private val stav = MutableStateFlow(siwc.stav())
    override val pripojenie: StateFlow<Pripojenie> = stav
    private var cachedModely: List<String>? = null

    override fun pripoj(suhlas: Boolean) {
        // the new tab opens inside the tap (a browser blocks a later one) and gets its address when the hash is done
        jsOpenSoon()
        scope.launch {
            val url = try { siwc.zacni(suhlas) } catch (e: Throwable) { log.log('!', "Prihlásenie sa nedá začať: ${e.message}"); stav.value = siwc.stav(); return@launch }
            stav.value = Pripojenie.Pripaja
            jsOpenNow(url)
        }
    }

    /** The pasted address: finishes the sign-in, or says in one line why not. */
    suspend fun vloz(adresa: String): String? = try {
        siwc.dokonci(adresa); cachedModely = null; stav.value = siwc.stav(); null
    } catch (e: TutorChyba) {
        log.log('!', "Prihlásenie zlyhalo: ${e.popis()}")
        stav.value = siwc.stav()
        when {
            e.druh == TutorChyba.Druh.OFFLINE -> "Nie je pripojenie na internet. Skús to o chvíľu."
            // our own checks of the address say it in words already
            e.kod in setOf("no_pending", "not_callback", "state_mismatch", "client_mismatch", "registration_incomplete") -> e.message
            e.kod == "invalid_grant" -> "Táto adresa už neplatí: kód z nej sa dá použiť raz a len pár minút. Otvor prihlásenie znova."
            e.kod == "access_denied" -> "Prihlásenie si v ChatGPT zrušil. Otvor ho znova, ak chceš."
            else -> "Prihlásenie sa nepodarilo (${e.kod}). Otvor ho znova."
        }
    }

    override fun zrusPrihlasenie() { siwc.zrus(); stav.value = siwc.stav() }

    override fun odpoj() {
        scope.launch { runCatching { siwc.odhlas() }; cachedModely = null; stav.value = siwc.stav() }
    }

    override fun klient(): ResponsesKlient? {
        if (stav.value !is Pripojenie.Pripojeny) return null
        val http = WebResponses(siwc, log)
        // a revoked or refused token turns the tutor off everywhere at once, as on the phone
        return object : ResponsesKlient {
            override suspend fun odpoved(telo: dev.hamper.tabula.compat.json.JSONObject, onText: (String) -> Unit) =
                try { http.odpoved(telo, onText) } catch (e: TutorChyba) {
                    if (e.druh == TutorChyba.Druh.ODPOJENE || e.druh == TutorChyba.Druh.NEPOVOLENE) stav.value = siwc.stav()
                    throw e
                }
            override suspend fun modely() = http.modely()
        }
    }

    override suspend fun modely(): List<String> = cachedModely ?: WebResponses(siwc, log).modely().also { cachedModely = it }

    override fun spravovatVyuzitie() = jsOpen(WebSiwc.USAGE_URL)

    override var poucenyOPlane: Boolean
        get() = localStorage.getItem("fitagoras.tutor.poucenie") == "1"
        set(v) { localStorage.setItem("fitagoras.tutor.poucenie", if (v) "1" else "0") }

    override var vyvojar: Boolean
        get() = false
        set(_) {}

    override fun drz(dovod: String?) {}

    override val kdePrihlasenie = "len v tomto prehliadači"

    /**
     * The end of the sign-in on the web: the browser lands on http://127.0.0.1:1455/… and says it can't connect; that
     * page's address carries the code. One tap pastes it from the clipboard; the field takes it where the clipboard
     * can't be read.
     */
    @Composable
    override fun CakamNaPrihlasenie() {
        val c = T.c
        val scope = rememberCoroutineScope()
        var adresa by remember { mutableStateOf("") }
        var chyba by remember { mutableStateOf<String?>(null) }
        var pracuje by remember { mutableStateOf(false) }
        fun skus(a: String) {
            if (a.isBlank()) { chyba = "Schránka je prázdna. Skopíruj adresu z konca prihlásenia."; return }
            pracuje = true
            scope.launch { chyba = vloz(a); pracuje = false }
        }
        Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
            for ((i, krok) in listOf(
                "V otvorenej karte sa prihlás do ChatGPT a povoľ FITagoras.",
                "Na konci prehliadač napíše, že sa nedá pripojiť k serveru. To je v poriadku.",
                "Ťukni na adresu hore, skopíruj ju celú a vráť sa sem.",
            ).withIndex()) Text("${i + 1}. $krok", style = MaterialTheme.typography.bodyMedium, color = c.ink, modifier = Modifier.padding(bottom = 6.dp))
            Spacer(Modifier.height(10.dp))
            Cta(if (pracuje) "Pripájam…" else "Vložiť skopírovanú adresu", {
                // read inside the tap (Safari allows the clipboard only there), awaited after it
                if (!pracuje) { val p = jsReadClipboard(); scope.launch { skus(p.await<JsString>().toString()) } }
            })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(adresa, { adresa = it; chyba = null }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("alebo ju vlož sem") }, placeholder = { Text("http://127.0.0.1:1455/auth/callback?…") })
            if (adresa.isNotBlank()) { Spacer(Modifier.height(8.dp)); Second("Pripojiť", { if (!pracuje) skus(adresa) }) }
            chyba?.let { Spacer(Modifier.height(8.dp)); Text(it, style = MaterialTheme.typography.bodyMedium, color = c.badInk) }
            // "Zrušiť" is the common one, under "Čakám na prihlásenie" (zrusPrihlasenie)
        }
    }

    /** The browser's picker: iOS offers the camera and the library; each page comes back as a JPEG ≤ 2048 px. */
    @Composable
    override fun Fotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit) {
        val c = T.c
        val scope = rememberCoroutineScope()
        var strany by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
        var sprava by remember { mutableStateOf<String?>(null) }
        Column(Modifier.fillMaxSize().background(c.bg).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Spacer(Modifier.height(24.dp))
            H1("Odfoť svoje riešenie")
            Spacer(Modifier.height(8.dp))
            Sub("Celý papier, ostro. Môžeš pridať viac strán, najviac $maxStran.")
            Spacer(Modifier.height(20.dp))
            if (strany.isNotEmpty()) { Text("Pridané strany: ${strany.size}", style = MaterialTheme.typography.titleMedium, color = c.ink); Spacer(Modifier.height(12.dp)) }
            if (strany.size < maxStran) Cta(if (strany.isEmpty()) "Odfotiť alebo vybrať fotku" else "Pridať ďalšiu stranu", {
                // the picker opens inside the tap (a browser ignores a later one), its photos are awaited after it
                val vyber = jsPickPhotos(maxStran - strany.size)
                scope.launch {
                    val b64 = vyber.await<JsString>().toString()
                    val nove = b64.split(',').filter { it.isNotEmpty() }.map { Base64.Default.decode(it) }
                    if (nove.isEmpty()) sprava = "Žiadna fotka. Skús znova." else { sprava = null; strany = strany + nove }
                }
            }, icon = Res.drawable.ic_camera)
            if (strany.isNotEmpty()) { Spacer(Modifier.height(10.dp)); Second("Skontrolovať", { onHotovo(strany) }) }
            sprava?.let { Spacer(Modifier.height(8.dp)); Text(it, style = MaterialTheme.typography.bodyMedium, color = c.ink2) }
            Spacer(Modifier.height(10.dp))
            Quiet("Zrušiť", onZavri)
        }
    }
}

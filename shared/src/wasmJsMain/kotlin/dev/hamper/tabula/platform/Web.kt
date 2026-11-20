@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.platform

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import dev.hamper.tabula.ui.Icon24
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_download
import dev.hamper.tabula.res.ic_phone
import dev.hamper.tabula.res.ic_refresh
import dev.hamper.tabula.screens.Divider
import dev.hamper.tabula.screens.Group
import dev.hamper.tabula.screens.NavRow
import dev.hamper.tabula.state.StateFile
import dev.hamper.tabula.state.Store
import dev.hamper.tabula.ui.T
import dev.hamper.tabula.ui.Text
import dev.hamper.tabula.web.jsDownload
import dev.hamper.tabula.web.jsIos
import dev.hamper.tabula.web.jsOpen
import dev.hamper.tabula.web.jsPickText
import dev.hamper.tabula.web.jsStandalone
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val fonts = HashMap<String, ByteArray>()
private val files = HashMap<String, String>()

internal actual fun fontBytes(file: String): ByteArray = fonts[file] ?: error("font $file was not preloaded")

/**
 * The browser reads nothing synchronously: fonts and the files the first screens read are fetched before the first
 * frame, all at once (one after another they cost a round trip each, seconds on a phone's network); the rest
 * ([preloadRest]) right after it.
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun preloadWeb() = coroutineScope {
    val fontJobs = listOf("google_sans_flex", "mjx_tex_n", "mjx_tex_so", "mjx_tex_lo", "mjx_tex_s3", "mjx_tex_s4", "mjx_tex_c")
        .map { f -> async { f to Res.readBytes("font/$f.ttf") } }
    val fileJobs = ResourceIndex.files.filter { (it.endsWith(".json") || it.endsWith(".tsv")) && !it.startsWith("coverage/") }
        .map { f -> async { f to Res.readBytes("files/$f").decodeToString() } }
    fontJobs.awaitAll().forEach { (f, b) -> fonts[f] = b }
    fileJobs.awaitAll().forEach { (f, t) -> files[f] = t }
    Assets.read = { path -> files[path] ?: error("file $path was not preloaded") }
}

/**
 * After the first frame: the coverage register (where a test topic is practised) and the course packs the tutor reads
 * (files/kontext, generated at build time and not in [ResourceIndex], so listed by their own index.txt).
 */
@OptIn(ExperimentalResourceApi::class)
suspend fun preloadRest() {
    for (f in ResourceIndex.files.filter { it.startsWith("coverage/") }) runCatching { files[f] = Res.readBytes("files/$f").decodeToString() }
    val index = runCatching { Res.readBytes("files/kontext/index.txt").decodeToString() }.getOrNull() ?: return
    files["kontext/index.txt"] = index
    for (f in index.lines().filter { it.isNotBlank() }) runCatching { files["kontext/$f"] = Res.readBytes("files/kontext/$f").decodeToString() }
}

/** Progress in the browser's localStorage, one key, the same JSON document as on the phone. */
class LocalStorageStateFile(private val key: String = KEY) : StateFile {
    override fun read(): String? = localStorage.getItem(key)
    override suspend fun write(doc: String) = localStorage.setItem(key, doc)
    companion object { const val KEY = "fitest.state" }
}

/**
 * A progress document of this app (state/Store.kt writes it): version 2, the theme and the lists every save writes,
 * each of the right kind. Anything else would load as an empty progress and replace the real one.
 */
private fun jeZaloha(text: String): Boolean = runCatching {
    val o = JSONObject(text)
    o.optInt("v") == 2 && o.opt("theme") is String && listOf("lessonsDone", "attempts", "mistakes", "mocks").all { o.opt(it) is dev.hamper.tabula.compat.json.JSONArray } &&
        listOf("parallels", "terms", "lessonAt").all { o.opt(it) is JSONObject }
}.getOrDefault(false)

private fun today(): String = js("new Date().toISOString().slice(0, 10)")

/** The web's answers to what screens ask of the device: links open in a new tab; the rest isn't here (Platform.isWeb). */
object WebPlatform : Platform {
    override val isWeb = true

    override fun openUrl(url: String) = jsOpen(url)

    /**
     * Where the progress lives and how not to lose it: Safari deletes a site's storage after 7 days without a visit
     * unless the app is on the home screen, so the card says that and offers a backup file (the same document the
     * phone keeps) and restoring one.
     */
    @Composable
    override fun SettingsRows(store: Store) {
        val c = T.c
        val scope = rememberCoroutineScope()
        var sprava by remember { mutableStateOf<String?>(null) }
        val naPloche = remember { jsStandalone() }
        val ios = remember { jsIos() }
        Group("Postup", if (naPloche) "Je len v tomto zariadení. Občas si stiahni zálohu." else if (ios)
            "Je len v tomto prehliadači. Safari ho zmaže, ak appku 7 dní neotvoríš; na ploche nie." else "Je len v tomto prehliadači.") {
            if (!naPloche) {
                // how, not a button: a page can't add itself to the home screen on iOS
                Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon24(Res.drawable.ic_phone, tint = c.ink2)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pridať na plochu", style = MaterialTheme.typography.titleMedium, color = c.ink)
                        Text(if (ios) "V Safari ťukni na Zdieľať, potom Pridať na plochu. Otvorí sa ako appka a postup sa nezmaže." else
                            "V menu prehliadača: Inštalovať appku alebo Pridať na plochu.", style = MaterialTheme.typography.bodySmall, color = c.ink2)
                    }
                }
                Divider()
            }
            NavRow(Res.drawable.ic_download, "Stiahnuť zálohu", "Súbor s celým postupom") {
                localStorage.getItem(LocalStorageStateFile.KEY)?.let { jsDownload("fitagoras-postup-${today()}.json", it) }
                    ?: run { sprava = "Zatiaľ nie je čo zálohovať." }
            }
            Divider()
            NavRow(Res.drawable.ic_refresh, "Obnoviť zo zálohy", "Nahradí postup v tomto prehliadači") {
                scope.launch {
                    val text = jsPickText().await<JsString>().toString()
                    if (text.isEmpty()) return@launch
                    if (!jeZaloha(text)) { sprava = "Tento súbor nie je záloha FITagoras."; return@launch }
                    // the replaced progress stays one key aside, in case the wrong file was picked
                    localStorage.getItem(LocalStorageStateFile.KEY)?.let { localStorage.setItem(LocalStorageStateFile.KEY + ".pred-obnovou", it) }
                    localStorage.setItem(LocalStorageStateFile.KEY, text)
                    window.location.reload()
                }
            }
            sprava?.let { Column(Modifier.padding(start = 52.dp, end = 14.dp, bottom = 12.dp)) { Text(it, style = MaterialTheme.typography.bodySmall, color = c.badInk) } }
        }
    }
}

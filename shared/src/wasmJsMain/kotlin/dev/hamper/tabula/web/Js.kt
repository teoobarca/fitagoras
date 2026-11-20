@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.web

import kotlin.js.Promise

/*
 * The calls into resources/web.js (window.fitWeb): what the app asks of the browser beyond drawing. Strings in,
 * strings out; web.js says what each one does.
 */

internal fun jsRequest(method: String, url: String, headers: String, body: String?): Promise<JsString> = js("window.fitWeb.request(method, url, headers, body)")

internal external interface JsStream : JsAny {
    val id: Int
    val done: Promise<JsString>
}

/** [onEvent] gets each server-sent event's data; true stops reading. */
internal fun jsStream(url: String, headers: String, body: String, onEvent: (JsString) -> Boolean): JsStream = js("window.fitWeb.stream(url, headers, body, onEvent)")
internal fun jsAbort(id: Int): Unit = js("window.fitWeb.abort(id)")

internal fun jsRandom(bytes: Int): String = js("window.fitWeb.random(bytes)")
internal fun jsSha256(text: String): Promise<JsString> = js("window.fitWeb.sha256(text)")
internal fun jsUuid(): String = js("window.fitWeb.uuid()")
internal fun jsBase64UrlDecode(s: String): String = js("window.fitWeb.base64urlDecode(s)")

internal fun jsReadClipboard(): Promise<JsString> = js("window.fitWeb.readClipboard()")
internal fun jsOpen(url: String): Unit = js("window.fitWeb.open(url)")
internal fun jsDownload(name: String, text: String): Unit = js("window.fitWeb.download(name, text)")
internal fun jsPickText(): Promise<JsString> = js("window.fitWeb.pickText()")
internal fun jsPickPhotos(max: Int): Promise<JsString> = js("window.fitWeb.pickPhotos(max)")

internal fun jsSoundsAt(path: String): Unit = js("window.fitWeb.soundsAt(path)")
internal fun jsPlay(name: String, gain: Double): Unit = js("window.fitWeb.play(name, gain)")

internal fun jsOnBack(cb: () -> Unit): Unit = js("window.fitWeb.onBack(cb)")
internal fun jsRoute(route: String): Unit = js("window.fitWeb.route(route)")
internal fun jsOnRoute(cb: (String) -> Unit): Unit = js("window.fitWeb.onRoute(cb)")
internal fun jsChrome(bg: String, around: String, edge: String): Unit = js("window.fitWeb.chrome(bg, around, edge)")
/** The app's column width in CSS px (ui/AppColumn.kt): the wheel beside it scrolls it (web.js, "the column"). */
internal fun jsColumn(width: Double): Unit = js("window.fitWeb.column(width)")
internal fun jsPersist(): Promise<JsString> = js("window.fitWeb.persist()")
internal fun jsStandalone(): Boolean = js("window.fitWeb.standalone()")
internal fun jsIos(): Boolean = js("window.fitWeb.ios()")
internal fun jsOpenSoon(): Unit = js("window.fitWeb.openSoon()")
internal fun jsOpenNow(url: String): Unit = js("window.fitWeb.openNow(url)")
internal fun jsDecode(s: String): String = js("window.fitWeb.decode(s)")
internal fun jsEncode(s: String): String = js("window.fitWeb.encode(s)")
/** The app's first frame is on screen: the splash in index.html fades out. */
internal fun jsReady(): Unit = js("window.fitWeb.ready()")
/** Starting failed: the splash says so in words and offers "Skúsiť znova". */
internal fun jsFailed(message: String): Unit = js("window.fitWeb.failed(message)")
internal fun jsLog(s: String): Unit = js("console.log(s)")

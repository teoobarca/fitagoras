package dev.hamper.tabula.tutor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.platform.Assets
import kotlinx.coroutines.flow.StateFlow

/** Where the student stands with ChatGPT; the tutor's buttons exist only in [Pripojeny] with [Pripojeny.planPovoleny]. */
sealed interface Pripojenie {
    data object Nepripojeny : Pripojenie
    /** The browser is open for sign-in; the app waits for it on 127.0.0.1. */
    data object Pripaja : Pripojenie
    /** Signed in; [planPovoleny]: the `chatgpt.tokens.use.direct` scope was granted (without it the plan can't be used). */
    data class Pripojeny(val email: String?, val planPovoleny: Boolean) : Pripojenie
}

/**
 * What the tutor needs from the device (Android: androidMain/tutor/AndroidTutor.kt): the ChatGPT connection, the
 * client, the in-app camera; the web: wasmJsMain/tutor/WebTutor.kt. The desktop has none (null [LocalTutor]).
 */
interface TutorSluzba {
    val pripojenie: StateFlow<Pripojenie>

    /** Opens ChatGPT's sign-in in the browser and waits for it on 127.0.0.1; [suhlas]: ask again for the plan. */
    fun pripoj(suhlas: Boolean = false)

    /** "Zrušiť" under "Čakám na prihlásenie": the attempt ends (its listener, its wait), the sign-in button is back. */
    fun zrusPrihlasenie() {}

    /** Disconnects: revokes the token at OpenAI and forgets it here. */
    fun odpoj()

    /** The client for one request, or null when not connected. */
    fun klient(): ResponsesKlient?

    /** The models this account lists, for choosing ([Nastavenie.z]); cached for the session. */
    suspend fun modely(): List<String>

    /** "Spravovať využitie": chatgpt.com/settings/usage. */
    fun spravovatVyuzitie()

    /** The "Používaš svoj ChatGPT plán" notice was confirmed (shown once, OpenAI's UI guidelines). */
    var poucenyOPlane: Boolean

    /** Developer switch: the tutor shows even while the harness gate is closed. */
    var vyvojar: Boolean

    /** Keeps the app alive while a request runs in the background (a foreground service on Android). */
    fun drz(dovod: String?)


    /** How many pages the collection holds. */
    fun zbierkaPocet(): Int = 0

    /** Shares the collection as one file (the system share sheet); nothing is sent anywhere by itself. */
    fun exportujZbierku() {}

    /** Deletes the collection from this phone. */
    fun vymazZbierku() {}

    /** Where the sign-in is kept, as the first-use screen says it ("Prihlásenie ostáva …"). */
    val kdePrihlasenie: String get() = "zašifrované v tomto telefóne"

    /**
     * Under "Čakám na prihlásenie": the phone shows nothing (its 127.0.0.1 listener gets the callback); the web asks
     * for the address the browser landed on (web/WebTutor).
     */
    @Composable
    fun CakamNaPrihlasenie() {}

    /** The in-app camera: one tap a page, several pages, [onHotovo] with the JPEGs (longer side ≤ 2048 px, no EXIF). */
    @Composable
    fun Fotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit)
}

val LocalTutor = staticCompositionLocalOf<TutorSluzba?> { null }

/**
 * The gate (docs/TUTOR.md, "Brána"): the harness writes files/tutor/brana.json with what it measured; the tutor is on
 * for students only when every threshold holds. Below it, only the developer switch shows it.
 */
object Brana {
    /** The harness's numbers (docs/TUTOR.md, "Brána"): reported, no longer a condition for the tutor to be seen (Teo, 18. 11.). */
    val otvorena: Boolean by lazy { runCatching { JSONObject(Assets.read("tutor/brana.json")).optBoolean("otvorena") }.getOrDefault(false) }

    /** The tutor's buttons: wherever the device has the tutor; the first tap without a connection offers the sign-in. */
    fun viditelny(t: TutorSluzba?): Boolean = t != null

    /** The settings card: wherever the tutor could be used. */
    fun karta(t: TutorSluzba?): Boolean = t != null
}

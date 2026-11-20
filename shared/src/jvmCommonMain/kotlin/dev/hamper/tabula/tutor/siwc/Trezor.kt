package dev.hamper.tabula.tutor.siwc

import dev.hamper.tabula.compat.json.JSONObject

/**
 * Where the ChatGPT credentials live: one JSON record (client_id, tokens, account, the pending sign-in). Android keeps
 * it AES-GCM encrypted under a non-exportable Keystore key in noBackupFilesDir (androidMain/KeystoreTrezor), the
 * harness in a 0600 file outside any repo (jvmMain/SuborovyTrezor). Nothing read from here is ever logged.
 */
interface Trezor {
    fun nacitaj(): JSONObject
    fun uloz(data: JSONObject)

    /** Read, change, write as one step (implementations synchronize). */
    fun uprav(zmena: (JSONObject) -> Unit)

    /** Removes the whole record (the student disconnected and the registration should go too). */
    fun zmaz()

    /** Runs [f] holding the vault for itself, across processes where that matters (the harness's file). */
    fun <T> vylucne(f: () -> T): T = f()
}

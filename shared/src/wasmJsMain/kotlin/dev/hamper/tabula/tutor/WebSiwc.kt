@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.TutorChyba.Druh
import dev.hamper.tabula.web.jsBase64UrlDecode
import dev.hamper.tabula.web.jsDecode
import dev.hamper.tabula.web.jsEncode
import dev.hamper.tabula.web.jsRandom
import dev.hamper.tabula.web.jsRequest
import dev.hamper.tabula.web.jsSha256
import dev.hamper.tabula.web.jsUuid
import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Sign in with ChatGPT in the browser: the same public client, PKCE and loopback redirect as the phone
 * (jvmCommonMain/tutor/siwc/Siwc.kt), but nothing listens on 127.0.0.1 here, so the sign-in ends on the browser's
 * "can't connect" page and the student pastes that page's address back ([dokonci]), the phone's own fallback. Tokens
 * live in this browser's localStorage under one key and go only to auth.openai.com and api.openai.com, straight from
 * the page (both answer CORS); no server of ours sees them. docs/TUTOR.md, "Web".
 *
 * The ID token is not checked against OpenAI's keys: the JWKS document sends no CORS header, so a page cannot read it.
 * It comes straight from the token endpoint over TLS, which OpenID Connect Core 3.1.3.7 (6) accepts in place of the
 * signature; issuer, audience, expiry, nonce and subject are still checked.
 */
class WebSiwc(private val zaznam: Zaznam) {
    companion object {
        const val KEY = "fitagoras.tutor"
        const val ISSUER = "https://auth.openai.com"
        const val AUTHORIZE = "$ISSUER/api/accounts/authorize"
        const val TOKEN = "$ISSUER/api/accounts/oauth/token"
        const val REVOKE = "$ISSUER/api/accounts/oauth/revoke"
        const val RESOURCE = "https://api.openai.com/v1"
        const val SCOPES = "openid profile email offline_access resource.invoke chatgpt.tokens.use.direct"
        const val SHARING = "chatgpt.tokens.use.direct"
        const val REDIRECT = "http://127.0.0.1:1455/auth/callback"
        const val USAGE_URL = "https://chatgpt.com/settings/usage"
        /** How long a started sign-in waits for its pasted address. */
        const val CAKANIE_MS = 30 * 60_000L
        private val ODVOLANE = setOf("invalid_grant", "invalid_refresh_token", "token_expired", "refresh_token_expired", "refresh_token_invalidated", "refresh_token_reused")
    }

    private val zamok = Mutex()

    fun nacitaj(): JSONObject = localStorage.getItem(KEY)?.let { runCatching { JSONObject(it) }.getOrNull() } ?: JSONObject()
    private fun uloz(j: JSONObject) = localStorage.setItem(KEY, j.toString())
    private fun uprav(f: (JSONObject) -> Unit) = uloz(nacitaj().also(f))

    /** Opaque and per browser, not a credential (urn:uuid, as the docs ask). */
    private val hostId: String
        get() = nacitaj().optString("host_id").ifEmpty { null } ?: ("urn:uuid:" + jsUuid()).also { id -> uprav { it.put("host_id", id) } }

    fun stav(): Pripojenie {
        val d = nacitaj()
        if (d.optString("refresh_token").isNotEmpty()) return Pripojenie.Pripojeny(d.optString("email").ifEmpty { null }, SHARING in d.optString("scope").split(' '))
        val p = d.optJSONObject("pending")
        return if (p != null && currentTimeMillis() - p.optLong("t") < CAKANIE_MS) Pripojenie.Pripaja else Pripojenie.Nepripojeny
    }

    /** A new attempt (fresh state, nonce, PKCE) saved as pending, and the address of OpenAI's sign-in page. */
    suspend fun zacni(suhlas: Boolean): String {
        val saved = nacitaj()
        val clientId = saved.optString("client_id").ifEmpty { null }
        val verifier = jsRandom(32)
        val state = jsRandom(32)
        val nonce = jsRandom(32)
        val challenge = jsSha256(verifier).await<JsString>().toString()
        val params = linkedMapOf(
            "client_id" to (clientId ?: "dynamic_agent_client"), "response_type" to "code", "redirect_uri" to REDIRECT,
            "scope" to SCOPES, "resource" to RESOURCE, "state" to state, "nonce" to nonce,
            "code_challenge_method" to "S256", "code_challenge" to challenge, "ext_agent_host_id" to hostId,
        )
        if (clientId == null) params["agent_name_hint"] = "FITagoras"
        else saved.optString("email").ifEmpty { null }?.let { params["login_hint"] = it }
        if (suhlas) params["prompt"] = "consent"
        uprav { it.put("pending", JSONObject().put("state", state).put("nonce", nonce).put("verifier", verifier).put("client_id_req", params["client_id"]).put("t", currentTimeMillis())) }
        zaznam.log('i', if (clientId == null) "Prvá registrácia v prehliadači" else "Prihlásenie s uloženým client_id")
        return AUTHORIZE + "?" + params.entries.joinToString("&") { "${jsEncode(it.key)}=${jsEncode(it.value)}" }
    }

    fun zrus() = uprav { it.remove("pending") }

    /**
     * The pasted address (http://127.0.0.1:1455/auth/callback?code=…&state=…): its state must be the pending one;
     * then the code is exchanged for tokens and saved. Returns whether the plan may be used.
     */
    suspend fun dokonci(adresa: String): Boolean = zamok.withLock {
        val pend = nacitaj().optJSONObject("pending") ?: throw chyba("no_pending", "Prihlásenie už neplatí. Začni ho znova.")
        val q = adresa.trim().substringAfter('?', "").substringBefore('#').split('&').filter { it.isNotEmpty() }
            .map { jsDecode(it.substringBefore('=')) to jsDecode(it.substringAfter('=', "")) }
        fun one(k: String) = q.firstOrNull { it.first == k }?.second
        if (q.isEmpty()) throw chyba("not_callback", "Toto nie je adresa z konca prihlásenia.")
        if (one("state") != pend.optString("state")) throw chyba("state_mismatch", "Adresa patrí k inému prihláseniu. Začni ho znova a vlož adresu z jeho konca.")
        one("error")?.let { throw chyba(it, "ChatGPT vrátil „$it“.") }
        val code = one("code") ?: throw chyba("registration_incomplete", "V adrese chýba kód. Skopíruj celú adresu.")
        val first = pend.optString("client_id_req") == "dynamic_agent_client"
        val clientId = when {
            one("client_id") != null && !first && one("client_id") != pend.optString("client_id_req") -> throw chyba("client_mismatch", "Adresa nepatrí k tomuto prihláseniu. Otvor prihlásenie znova a vlož adresu z jeho konca.")
            one("client_id") != null -> one("client_id")!!
            !first -> pend.optString("client_id_req")
            else -> throw chyba("registration_incomplete", "Adresa nie je celá. Skopíruj ju celú, od „http“ po koniec.")
        }
        if (!Regex("^[A-Za-z0-9_-]{1,200}$").matches(clientId) || clientId == "dynamic_agent_client") throw chyba("registration_incomplete", "Adresa nie je celá. Skopíruj ju celú, od „http“ po koniec.")
        // the registration first, so a failed exchange doesn't register the app a second time
        uprav { it.put("client_id", clientId) }
        val data = tokenPost(mapOf("grant_type" to "authorization_code", "client_id" to clientId, "code" to code,
            "code_verifier" to pend.getString("verifier"), "redirect_uri" to REDIRECT, "resource" to RESOURCE), "Výmena kódu")
        val idToken = data.optString("id_token").ifEmpty { throw chyba("invalid_id_token", "Odpoveď neobsahuje id_token") }
        val claims = overIdToken(idToken, clientId, pend.getString("nonce"))
        val old = nacitaj()
        if (old.optString("subject").isNotEmpty() && old.optString("subject") != claims.getString("sub")) {
            old.optString("refresh_token").ifEmpty { null }?.let { revokuj(it, clientId) }
        }
        val rec = zTokenov(data, old).put("client_id", clientId).put("subject", claims.getString("sub"))
            .put("email", claims.optString("email")).put("ext_agent_host_id", hostId)
        rec.remove("pending")
        uloz(rec)
        val plan = SHARING in data.optString("scope").split(' ')
        zaznam.log(if (plan) '+' else '!', if (plan) "Pripojené" else "Pripojené bez povolenia plánu")
        plan
    }

    private fun zTokenov(data: JSONObject, old: JSONObject): JSONObject {
        val access = data.optString("access_token").ifEmpty { throw chyba("invalid_token_response", "Odpoveď neobsahuje access_token") }
        val refresh = data.optString("refresh_token").ifEmpty { old.optString("refresh_token").ifEmpty { throw chyba("invalid_token_response", "Chýba refresh_token") } }
        val expiresIn = data.optLong("expires_in").takeIf { it > 0 } ?: 3600L
        return JSONObject(old.toString()).put("access_token", access).put("refresh_token", refresh)
            .put("expires_at", currentTimeMillis() + expiresIn * 1000).put("scope", data.optString("scope").ifEmpty { old.optString("scope") })
    }

    private fun overIdToken(jwt: String, clientId: String, nonce: String?): JSONObject {
        val p = jwt.split('.')
        if (p.size != 3) throw chyba("invalid_id_token", "id_token nemá tri časti")
        val c = runCatching { JSONObject(jsBase64UrlDecode(p[1])) }.getOrElse { throw chyba("invalid_id_token", "id_token sa nedá prečítať") }
        val aud = c.opt("aud")
        val audOk = aud == clientId || (aud is JSONArray && (0 until aud.length()).any { aud.optString(it) == clientId })
        when {
            c.optString("iss") != ISSUER -> throw chyba("invalid_id_token", "iss nesedí")
            !audOk -> throw chyba("invalid_id_token", "aud nie je vydané client_id")
            c.optLong("exp") < currentTimeMillis() / 1000 - 5 -> throw chyba("invalid_id_token", "id_token vypršal")
            nonce != null && c.optString("nonce") != nonce -> throw chyba("invalid_id_token", "nonce nesedí")
            c.optString("sub").isEmpty() -> throw chyba("invalid_id_token", "chýba sub")
        }
        return c
    }

    /** A valid access token; a refresh at most one at a time, its rotated refresh token saved at once. */
    suspend fun obnov(vynutene: Boolean = false): String = zamok.withLock {
        val d = nacitaj()
        val access = d.optString("access_token")
        if (!vynutene && access.isNotEmpty() && d.optLong("expires_at") > currentTimeMillis() + 60_000) return@withLock access
        val refresh = d.optString("refresh_token").ifEmpty { throw chyba("not_signed_in", "Nie si pripojený k ChatGPT") }
        val clientId = d.getString("client_id")
        val data = try {
            tokenPost(mapOf("grant_type" to "refresh_token", "client_id" to clientId, "refresh_token" to refresh, "resource" to RESOURCE), "Obnova tokenu")
        } catch (e: TutorChyba) {
            if (e.kod in ODVOLANE) uprav { if (it.optString("refresh_token") == refresh) listOf("access_token", "refresh_token", "expires_at").forEach(it::remove) }
            throw e
        }
        data.optString("id_token").ifEmpty { null }?.let { idt ->
            if (overIdToken(idt, clientId, null).optString("sub") != d.optString("subject")) {
                data.optString("refresh_token").ifEmpty { null }?.let { revokuj(it, clientId) }
                uprav { listOf("access_token", "refresh_token", "expires_at").forEach(it::remove) }
                throw chyba("account_mismatch", "Obnovená identita patrí inému účtu")
            }
        }
        val novy = zTokenov(data, nacitaj())
        uloz(novy)
        novy.getString("access_token")
    }

    /** Revokes the refresh token and forgets the tokens here; the registration (client_id, host id) stays. */
    suspend fun odhlas() = zamok.withLock {
        val d = nacitaj()
        val refresh = d.optString("refresh_token")
        if (refresh.isNotEmpty() && d.optString("client_id").isNotEmpty()) revokuj(refresh, d.getString("client_id"))
        uprav { j -> listOf("access_token", "refresh_token", "expires_at", "email", "subject", "scope", "pending").forEach(j::remove) }
    }

    private suspend fun revokuj(refresh: String, clientId: String) {
        val r = http("POST", REVOKE, """{"Content-Type":"application/x-www-form-urlencoded"}""", form(mapOf("token" to refresh, "token_type_hint" to "refresh_token", "client_id" to clientId)))
        if (r.optInt("status") != 200) zaznam.log('!', "Revokácia: HTTP ${r.optInt("status")}")
    }

    private fun form(m: Map<String, String>) = m.entries.joinToString("&") { "${jsEncode(it.key)}=${jsEncode(it.value)}" }

    private suspend fun tokenPost(f: Map<String, String>, krok: String): JSONObject {
        val r = http("POST", TOKEN, """{"Content-Type":"application/x-www-form-urlencoded","Accept":"application/json"}""", form(f))
        val status = r.optInt("status")
        if (status == 0) throw TutorChyba(Druh.OFFLINE, "network", "$krok: sieť nedostupná")
        if (status != 200) throw apiChyba(r.optString("text"), status, r.optString("rid").ifEmpty { null }, krok)
        return JSONObject(r.optString("text"))
    }

    /** One request through web.js: {status, rid, text}, status 0 without a connection. */
    suspend fun http(method: String, url: String, headers: String, body: String?): JSONObject =
        JSONObject(jsRequest(method, url, headers, body).await<JsString>().toString())

    private fun chyba(kod: String, sprava: String, http: Int? = null, rid: String? = null, param: String? = null) =
        TutorChyba(TutorChyba.druhZ(kod, http), kod, sprava, http, param, rid)

    /** The server's error, also when nested (`error`, `detail`), as a [TutorChyba] (the phone's Siwc.apiChyba). */
    fun apiChyba(text: String, http: Int, rid: String?, krok: String): TutorChyba {
        var o: Any? = runCatching { JSONObject(text) }.getOrNull()
        var detail: String? = null
        repeat(4) {
            val j = o as? JSONObject ?: return@repeat
            when {
                j.opt("error") is JSONObject -> o = j.getJSONObject("error")
                j.opt("detail") is JSONObject -> o = j.getJSONObject("detail")
                j.opt("detail") is String -> detail = j.getString("detail")
            }
        }
        val j = o as? JSONObject
        val kod = (j?.opt("error") as? String) ?: j?.optString("code")?.ifEmpty { null }?.takeIf { it != "null" } ?: j?.optString("type")?.ifEmpty { null } ?: "http_$http"
        val param = j?.optString("param")?.ifEmpty { null }?.takeIf { it != "null" }
        val msg = (j?.optString("message")?.ifEmpty { null }?.takeIf { it != "null" } ?: (j?.opt("error_description") as? String) ?: detail
            ?: if (j != null) "server vrátil len kód chyby" else text.take(160)).take(240)
        return chyba(kod, "$krok: $msg", http, rid, param)
    }
}

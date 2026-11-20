package dev.hamper.tabula.tutor.siwc

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.Pripojenie
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.TutorChyba.Druh
import dev.hamper.tabula.tutor.Zaznam
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.UUID

/**
 * Sign in with ChatGPT for a native app (public client, PKCE, loopback redirect to 127.0.0.1), ported from the
 * prototype that passed on Teo's Pixel (siwc-pokus, 17. 11.). The same code signs in the harness on the Mac.
 * Tokens live only in [trezor]; [zaznam] never sees one.
 *
 * [appName]: what ChatGPT shows as the connected app. [spatDoAppky]: the link on the "signed in" page that brings the
 * student back (Android: an intent:// URL to the activity), null on the desktop.
 */
class Siwc(
    val trezor: Trezor,
    private val appName: String,
    private val spatDoAppky: String?,
    private val zaznam: Zaznam,
) {
    companion object {
        const val ISSUER = "https://auth.openai.com"
        const val AUTHORIZE = "$ISSUER/api/accounts/authorize"
        const val TOKEN = "$ISSUER/api/accounts/oauth/token"
        const val REVOKE = "$ISSUER/api/accounts/oauth/revoke"
        const val JWKS = "$ISSUER/.well-known/jwks.json"
        const val RESOURCE = "https://api.openai.com/v1"
        const val SCOPES = "openid profile email offline_access resource.invoke chatgpt.tokens.use.direct"
        const val SHARING = "chatgpt.tokens.use.direct"
        const val CALLBACK_PATH = "/auth/callback"
        const val PREFERRED_PORT = 1455
        const val USAGE_URL = "https://chatgpt.com/settings/usage"
        private val ODVOLANE = setOf("invalid_grant", "invalid_refresh_token", "token_expired", "refresh_token_expired", "refresh_token_invalidated", "refresh_token_reused")
    }

    private val refreshLock = Mutex()
    private val rnd = SecureRandom()
    @Volatile private var jwksCache: JSONArray? = null

    private fun ok(t: String) = zaznam.log('+', t)
    private fun info(t: String) = zaznam.log('i', t)
    private fun pozor(t: String) = zaznam.log('!', t)

    /** Opaque, persisted per install, not a credential: urn:uuid (the server rejects a plain UUID). */
    val hostId: String
        get() = runCatching { trezor.nacitaj().optString("host_id").ifEmpty { null } }.getOrNull()
            ?: ("urn:uuid:" + UUID.randomUUID()).also { id -> runCatching { trezor.uprav { it.put("host_id", id) } } }

    fun stav(): Pripojenie {
        val d = runCatching { trezor.nacitaj() }.getOrNull() ?: return Pripojenie.Nepripojeny
        if (d.optString("refresh_token").isEmpty()) return Pripojenie.Nepripojeny
        return Pripojenie.Pripojeny(d.optString("email").ifEmpty { null }, SHARING in d.optString("scope").split(' '))
    }

    private fun nahodne(): String = ByteArray(32).also { rnd.nextBytes(it) }.b64()
    private fun ByteArray.b64() = Base64.getUrlEncoder().withoutPadding().encodeToString(this)
    private fun b64d(s: String) = Base64.getUrlDecoder().decode(s)
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    // ---------------------------------------------------------------- sign-in

    data class Pokus(val state: String, val nonce: String, val verifier: String, val redirectUri: String, val clientIdVZiadosti: String, val zaciatok: Long)

    data class Callback(val code: String, val clientId: String?, val scope: String?)

    /** The loopback listener on 127.0.0.1 for one sign-in attempt; [vysledok] completes with the callback. */
    inner class Posluchac(val pokus: Pokus, private val server: ServerSocket) {
        val vysledok = CompletableDeferred<Callback>()
        val port: Int get() = server.localPort
        @Volatile private var bezi = true

        fun spusti() = Thread({
            while (bezi) {
                val s = try { server.accept() } catch (_: Exception) { break }
                try { obsluz(s) } catch (e: Exception) { pozor("Poslucháč: chyba pri obsluhe spojenia: ${e.javaClass.simpleName}") }
                finally { runCatching { s.close() } }
            }
        }, "siwc-loopback").apply { isDaemon = true; start() }

        /** Stops listening; a sign-in still waiting for this attempt ends (cancelled). */
        fun zavri() { bezi = false; runCatching { server.close() }; vysledok.cancel() }

        private fun odpovedz(s: Socket, status: String, html: String) {
            val body = html.toByteArray()
            s.getOutputStream().apply {
                write(("HTTP/1.1 $status\r\nContent-Type: text/html; charset=utf-8\r\nCache-Control: no-store\r\n" +
                    "Referrer-Policy: no-referrer\r\nX-Content-Type-Options: nosniff\r\nContent-Length: ${body.size}\r\nConnection: close\r\n\r\n").toByteArray())
                write(body); flush()
            }
        }

        private fun obsluz(s: Socket) {
            s.soTimeout = 10_000
            val r = BufferedReader(InputStreamReader(s.getInputStream()), 8192)
            val line = r.readLine()?.take(8192) ?: return
            val headers = generateSequence { r.readLine()?.takeIf { it.isNotEmpty() } }.take(100).toList()
            val host = headers.firstOrNull { it.startsWith("host:", true) }?.substringAfter(':')?.trim()
            val parts = line.split(' ')
            val target = parts.getOrNull(1) ?: "/"
            val path = target.substringBefore('?')
            if (parts[0] != "GET" || path != CALLBACK_PATH) { odpovedz(s, "404 Not Found", "Not found"); return }
            // the browser sends exactly the redirect's authority; anything else is another page talking to us
            if (host != "127.0.0.1:${server.localPort}") {
                pozor("Poslucháč: hlavička Host nesedí, požiadavku odmietam")
                odpovedz(s, "400 Bad Request", stranka("Neplatná požiadavka", "Vráť sa do appky a skús to znova.")); return
            }
            val q = query(target.substringAfter('?', ""))
            fun one(k: String) = q.filter { it.first == k }.map { it.second }
            val states = one("state")
            if (states.size != 1 || !MessageDigest.isEqual(states[0].toByteArray(), pokus.state.toByteArray())) {
                pozor("Callback má nesprávny state, ignorujem ho (iná karta alebo starý pokus)")
                odpovedz(s, "400 Bad Request", stranka("Neplatný stav prihlásenia", "Vráť sa do appky a skús to znova.")); return
            }
            ok("Poslucháč prijal callback po ${(System.currentTimeMillis() - pokus.zaciatok) / 1000} s")
            val err = one("error").firstOrNull()
            odpovedz(s, "200 OK", if (err != null) stranka("Pripojenie sa nepodarilo", "ChatGPT vrátil „$err“. Vráť sa do appky a skús to znova.".replace("<", "&lt;"))
                else stranka("Prihlásenie prijaté", if (spatDoAppky != null) "Appka dokončuje pripojenie. Ťukni nižšie, ak sa nevrátila sama." else "Môžeš toto okno zavrieť."))
            if (err != null) { vysledok.completeExceptionally(chyba(err, "Autorizácia vrátila chybu $err${one("error_description").firstOrNull()?.let { " ($it)" } ?: ""}")); return }
            val code = one("code")
            if (code.size != 1 || code[0].isEmpty()) { vysledok.completeExceptionally(chyba("registration_incomplete", "Callback neobsahuje kód")); return }
            vysledok.complete(Callback(code[0], one("client_id").firstOrNull(), one("scope").firstOrNull()))
        }

        private fun stranka(nadpis: String, text: String): String {
            val spat = spatDoAppky?.let { u -> """<a href="$u">Späť do appky</a><script>history.replaceState(null,"","/auth/hotovo");setTimeout(function(){location.href="$u"},300)</script>""" }
                ?: """<script>history.replaceState(null,"","/auth/hotovo")</script>"""
            return """<!doctype html><html lang="sk"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>$nadpis</title>
<style>body{font:18px system-ui;max-width:30rem;margin:12vh auto;padding:24px}a{display:inline-block;margin-top:16px;padding:14px 22px;background:#111;color:#fff;border-radius:24px;text-decoration:none}</style>
<h1>$nadpis</h1><p>$text</p>$spat</html>"""
        }
    }

    private fun query(q: String): List<Pair<String, String>> = q.split('&').filter { it.isNotEmpty() }.map {
        URLDecoder.decode(it.substringBefore('='), "UTF-8") to URLDecoder.decode(it.substringAfter('=', ""), "UTF-8")
    }

    /**
     * Starts the listener (before the browser opens, as the docs require) and builds the authorization URL.
     * [suhlas]: ask for consent again (the plan scope was refused before).
     */
    fun pripravPrihlasenie(suhlas: Boolean = false): Pair<Posluchac, String> {
        val server = try {
            ServerSocket(PREFERRED_PORT, 8, InetAddress.getByName("127.0.0.1"))
        } catch (e: Exception) {
            pozor("Port $PREFERRED_PORT je obsadený (${e.javaClass.simpleName}), beriem voľný port")
            ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
        }
        val redirect = "http://127.0.0.1:${server.localPort}$CALLBACK_PATH"
        val saved = runCatching { trezor.nacitaj() }.getOrElse { pozor("Trezor sa nedá prečítať (${it.javaClass.simpleName}), začínam bez uloženého účtu"); JSONObject() }
        val clientId = saved.optString("client_id").ifEmpty { null }
        val verifier = nahodne()
        val p = Pokus(nahodne(), nahodne(), verifier, redirect, clientId ?: "dynamic_agent_client", System.currentTimeMillis())
        val challenge = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()).b64()
        val params = linkedMapOf(
            "client_id" to p.clientIdVZiadosti, "response_type" to "code", "redirect_uri" to redirect,
            "scope" to SCOPES, "resource" to RESOURCE, "state" to p.state, "nonce" to p.nonce,
            "code_challenge_method" to "S256", "code_challenge" to challenge, "ext_agent_host_id" to hostId,
        )
        if (clientId == null) params["agent_name_hint"] = appName
        // the stored ID token is not sent as id_token_hint: the URL ends up in browser history and logs
        else saved.optString("email").ifEmpty { null }?.let { params["login_hint"] = it }
        if (suhlas) params["prompt"] = "consent"
        val url = AUTHORIZE + "?" + params.entries.joinToString("&") { "${enc(it.key)}=${enc(it.value)}" }
        // the pending attempt (verifier included) goes to the vault, so a pasted callback URL still works after the
        // process died while the browser was open
        runCatching {
            trezor.uprav {
                it.put("pending", JSONObject().put("state", p.state).put("nonce", p.nonce).put("verifier", p.verifier)
                    .put("redirect_uri", p.redirectUri).put("client_id_req", p.clientIdVZiadosti).put("t", p.zaciatok))
            }
        }.onFailure { pozor("Uloženie rozpracovaného pokusu zlyhalo: ${it.javaClass.simpleName}") }
        info(if (clientId == null) "Prvá registrácia (agent_name_hint=$appName), poslucháč na porte ${server.localPort}" else "Prihlásenie s uloženým client_id, poslucháč na porte ${server.localPort}")
        return Posluchac(p, server).also { it.spusti() } to url
    }

    /** Fallback when the browser can't reach our 127.0.0.1: the student (or Teo for the harness) pastes the URL. */
    fun callbackZUrl(url: String): Pair<Pokus, Callback> {
        val pend = trezor.nacitaj().optJSONObject("pending") ?: throw chyba("no_pending", "Nie je rozpracované žiadne prihlásenie")
        val pokus = Pokus(pend.getString("state"), pend.getString("nonce"), pend.getString("verifier"), pend.getString("redirect_uri"),
            pend.getString("client_id_req"), pend.getLong("t"))
        val q = query(runCatching { URI(url.trim()).rawQuery }.getOrNull() ?: "")
        fun one(k: String) = q.firstOrNull { it.first == k }?.second
        val st = one("state")
        if (st == null || !MessageDigest.isEqual(st.toByteArray(), pokus.state.toByteArray())) throw chyba("state_mismatch", "Vložená URL má iný state než rozpracované prihlásenie")
        one("error")?.let { throw chyba(it, "Autorizácia vrátila chybu $it") }
        val code = one("code") ?: throw chyba("registration_incomplete", "Vložená URL neobsahuje kód")
        return pokus to Callback(code, one("client_id"), one("scope"))
    }

    /**
     * Callback → issued client id → code exchange → ID token check → save. Returns whether the plan scope was granted.
     * Serialized with refresh and sign-out ([zamok]); an attempt no longer pending (signed out meanwhile, or another
     * attempt started) saves nothing.
     */
    suspend fun dokonci(pokus: Pokus, cb: Callback): Boolean = zamok { dokonciTeraz(pokus, cb) }

    private fun dokonciTeraz(pokus: Pokus, cb: Callback): Boolean {
        val pend = runCatching { trezor.nacitaj().optJSONObject("pending")?.optString("state") }.getOrNull()
        if (pend == null || !MessageDigest.isEqual(pend.toByteArray(), pokus.state.toByteArray())) throw chyba("attempt_stale", "Toto prihlásenie už neplatí (odpojené alebo začaté nové)")
        val first = pokus.clientIdVZiadosti == "dynamic_agent_client"
        val clientId = when {
            cb.clientId != null && !first && cb.clientId != pokus.clientIdVZiadosti -> throw chyba("client_mismatch", "Callback vrátil iné client_id než uložené")
            cb.clientId != null -> cb.clientId
            !first -> pokus.clientIdVZiadosti
            else -> throw chyba("registration_incomplete", "Prvá registrácia nevrátila vydané client_id")
        }
        if (clientId == "dynamic_agent_client" || !Regex("^[A-Za-z0-9_-]{1,200}$").matches(clientId)) throw chyba("registration_incomplete", "Neplatné client_id v callbacku")
        // keep the registration before the one-time code can fail, so a retry doesn't register a second app
        trezor.uprav { it.put("client_id", clientId) }
        val data = tokenPost(mapOf("grant_type" to "authorization_code", "client_id" to clientId, "code" to cb.code,
            "code_verifier" to pokus.verifier, "redirect_uri" to pokus.redirectUri, "resource" to RESOURCE), "Výmena kódu za tokeny")
        val idToken = data.optString("id_token").ifEmpty { throw chyba("invalid_id_token", "Odpoveď neobsahuje id_token") }
        val claims = overIdToken(idToken, clientId, pokus.nonce)
        val old = trezor.nacitaj()
        if (old.optString("subject").isNotEmpty() && old.optString("subject") != claims.getString("sub")) {
            // another account than the one registered on this install: the old one's token is revoked, then replaced
            pozor("Prihlásil sa iný ChatGPT účet než uložený, starý záznam nahrádzam")
            old.optString("refresh_token").ifEmpty { null }?.let { revokuj(it, clientId) }
            listOf("access_token", "refresh_token", "expires_at", "email", "subject").forEach { old.remove(it) }
        }
        val zaznamT = zaznamZTokenov(data, old).put("client_id", clientId).put("subject", claims.getString("sub"))
            .put("email", claims.optString("email")).put("id_token", idToken).put("ext_agent_host_id", hostId)
        zaznamT.remove("pending")
        trezor.uloz(zaznamT)
        val plan = SHARING in data.optString("scope").split(' ')
        if (plan) ok("Pripojené, scope $SHARING udelený") else pozor("Pripojené, ale scope $SHARING chýba (súhlas s plánom odmietnutý?)")
        return plan
    }

    private fun zaznamZTokenov(data: JSONObject, old: JSONObject): JSONObject {
        val access = data.optString("access_token")
        if (access.isEmpty()) throw chyba("invalid_token_response", "Odpoveď neobsahuje access_token (kľúče ${data.keys().asSequence().sorted().joinToString()})")
        val expiresIn = data.optLong("expires_in").takeIf { it > 0 } ?: 3600L
        val refresh = data.optString("refresh_token").ifEmpty {
            old.optString("refresh_token").ifEmpty { throw chyba("invalid_token_response", "Chýba refresh_token napriek offline_access") }
        }
        return JSONObject(old.toString())
            .put("access_token", access).put("refresh_token", refresh)
            .put("expires_at", System.currentTimeMillis() + expiresIn * 1000)
            .put("scope", data.optString("scope").ifEmpty { old.optString("scope") })
            .put("saved_at", System.currentTimeMillis())
    }

    // ---------------------------------------------------------------- tokens

    private fun tokenPost(form: Map<String, String>, krok: String): JSONObject {
        val body = form.entries.joinToString("&") { "${enc(it.key)}=${enc(it.value)}" }
        val c = siet(krok) {
            (URL(TOKEN).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"; doOutput = true; instanceFollowRedirects = false
                connectTimeout = 15_000; readTimeout = 30_000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
                outputStream.use { it.write(body.toByteArray()) }
            }
        }
        val code = siet(krok) { c.responseCode }
        val text = (if (code < 400) c.inputStream else c.errorStream)?.bufferedReader()?.readText() ?: ""
        if (code != 200) throw apiChyba(text, code, c.getHeaderField("x-request-id"), krok)
        return JSONObject(text)
    }

    /** RS256 signature against OpenAI's JWKS + iss, aud, exp, nonce, sub. */
    private fun overIdToken(jwt: String, clientId: String, nonce: String?): JSONObject {
        try {
            val p = jwt.split('.')
            if (p.size != 3) throw chyba("invalid_id_token", "id_token nemá tri časti")
            val header = JSONObject(String(b64d(p[0])))
            if (header.optString("alg") != "RS256") throw chyba("invalid_id_token", "alg ${header.optString("alg")} nie je RS256")
            fun najdi(keys: JSONArray) = (0 until keys.length()).map { keys.getJSONObject(it) }.firstOrNull { it.optString("kid") == header.optString("kid") }
            // keys rotate: a kid we don't know refetches once
            val jwk = jwksCache?.let(::najdi) ?: run {
                val jc = (URL(JWKS).openConnection() as HttpURLConnection).apply { connectTimeout = 15_000; readTimeout = 15_000 }
                val fresh = JSONObject(jc.inputStream.bufferedReader().use { it.readText() }).getJSONArray("keys")
                jwksCache = fresh
                najdi(fresh)
            } ?: throw chyba("invalid_id_token", "kľúč id_token nie je v JWKS")
            val pub = KeyFactory.getInstance("RSA").generatePublic(RSAPublicKeySpec(BigInteger(1, b64d(jwk.getString("n"))), BigInteger(1, b64d(jwk.getString("e")))))
            val ok = Signature.getInstance("SHA256withRSA").run { initVerify(pub); update("${p[0]}.${p[1]}".toByteArray()); verify(b64d(p[2])) }
            if (!ok) throw chyba("invalid_id_token", "podpis id_token nesedí")
            val c = JSONObject(String(b64d(p[1])))
            val aud = c.opt("aud")
            val audOk = aud == clientId || (aud is JSONArray && (0 until aud.length()).any { aud.optString(it) == clientId })
            val now = System.currentTimeMillis() / 1000
            when {
                c.optString("iss") != ISSUER -> throw chyba("invalid_id_token", "iss nesedí")
                !audOk -> throw chyba("invalid_id_token", "aud nie je vydané client_id")
                c.optLong("exp") < now - 5 -> throw chyba("invalid_id_token", "id_token vypršal")
                nonce != null && c.optString("nonce") != nonce -> throw chyba("invalid_id_token", "nonce nesedí")
                c.optString("sub").isEmpty() -> throw chyba("invalid_id_token", "chýba sub")
            }
            return c
        } catch (e: TutorChyba) { throw e } catch (e: Exception) {
            throw chyba("invalid_id_token", "Overenie id_token zlyhalo: ${e.javaClass.simpleName}")
        }
    }

    /** Refresh, sign-in completion and sign-out one at a time: in this process, and across processes when the vault can. */
    private suspend fun <T> zamok(f: () -> T): T = refreshLock.withLock { trezor.vylucne(f) }

    /** A valid access token: serialized; the rotated refresh token is saved before the new access token is used. */
    suspend fun obnov(vynutene: Boolean = false): String = zamok { obnovTeraz(vynutene) }

    private fun obnovTeraz(vynutene: Boolean): String {
        val d = trezor.nacitaj()
        val access = d.optString("access_token")
        if (!vynutene && access.isNotEmpty() && d.optLong("expires_at") > System.currentTimeMillis() + 60_000) return access
        val refresh = d.optString("refresh_token").ifEmpty { throw chyba("not_signed_in", "Nie si pripojený k ChatGPT") }
        val clientId = d.getString("client_id")
        val data = try {
            tokenPost(mapOf("grant_type" to "refresh_token", "client_id" to clientId, "refresh_token" to refresh, "resource" to RESOURCE), "Obnova tokenu")
        } catch (e: TutorChyba) {
            if (e.kod in ODVOLANE) {
                // only the token we sent: another process may have rotated in a new one meanwhile
                trezor.uprav { if (it.optString("refresh_token") == refresh) { it.remove("access_token"); it.remove("refresh_token"); it.remove("expires_at") } }
                pozor("Refresh token je nepoužiteľný (${e.kod}): tokeny zmazané, treba sa pripojiť znova")
            }
            throw e
        }
        // the rotated refresh token first, before anything else can throw
        data.optString("refresh_token").ifEmpty { null }?.let { rt -> trezor.uprav { it.put("refresh_token", rt) } }
        data.optString("id_token").ifEmpty { null }?.let { idt ->
            val c = overIdToken(idt, clientId, null)
            if (c.optString("sub") != d.optString("subject")) {
                // tokens of another account must not stay: revoke and forget them
                data.optString("refresh_token").ifEmpty { null }?.let { revokuj(it, clientId) }
                trezor.uprav { listOf("access_token", "refresh_token", "expires_at", "id_token").forEach(it::remove) }
                throw chyba("account_mismatch", "Obnovená identita patrí inému účtu")
            }
        }
        val novy = zaznamZTokenov(data, trezor.nacitaj())
        data.optString("id_token").ifEmpty { null }?.let { novy.put("id_token", it) }
        trezor.uloz(novy)
        ok("Access token obnovený")
        return novy.getString("access_token")
    }

    /**
     * Disconnect: revoke the refresh token (an empty 200 is success), then clear the tokens locally; the registration
     * (client_id, host id) stays, so connecting again does not register a second app. True when the server confirmed.
     * Serialized with refresh and sign-in, so neither can write tokens back after it.
     */
    suspend fun odhlas(): Boolean = zamok { odhlasTeraz() }

    private fun odhlasTeraz(): Boolean {
        val d = runCatching { trezor.nacitaj() }.getOrElse { JSONObject() }
        val refresh = d.optString("refresh_token")
        val potvrdene = refresh.isNotEmpty() && d.optString("client_id").isNotEmpty() && revokuj(refresh, d.getString("client_id"))
        runCatching { trezor.uprav { listOf("access_token", "refresh_token", "expires_at", "id_token", "email", "subject", "scope", "pending").forEach(it::remove) } }
        if (potvrdene) ok("Odpojené: revokácia potvrdená, tokeny zmazané") else pozor("Tokeny zmazané lokálne, revokácia nepotvrdená")
        return potvrdene
    }

    /** Clears an unfinished sign-in (its PKCE verifier) after it failed or timed out. */
    fun zrusPokus(pokus: Pokus) {
        runCatching { trezor.uprav { j -> if (j.optJSONObject("pending")?.optString("state") == pokus.state) j.remove("pending") } }
    }

    private fun revokuj(refresh: String, clientId: String): Boolean {
        for (pokus in 1..2) {
            try {
                val c = (URL(REVOKE).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"; doOutput = true; instanceFollowRedirects = false
                    connectTimeout = 15_000; readTimeout = 15_000
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }
                c.outputStream.use { it.write("token=${enc(refresh)}&token_type_hint=refresh_token&client_id=${enc(clientId)}".toByteArray()) }
                val code = c.responseCode
                runCatching { (if (code < 400) c.inputStream else c.errorStream)?.readBytes() }
                if (code == 200) return true
                pozor("Revokácia: HTTP $code")
                if (code < 500) return false
            } catch (e: Exception) { pozor("Revokácia: sieťová chyba ${e.javaClass.simpleName}") }
            Thread.sleep(500)
        }
        return false
    }

    // ---------------------------------------------------------------- errors

    private fun chyba(kod: String, sprava: String, http: Int? = null, rid: String? = null, param: String? = null) =
        TutorChyba(TutorChyba.druhZ(kod, http), kod, sprava, http, param, rid)

    /** The server's error, also when nested (`error`, `detail`, `error.error`), as a [TutorChyba]. */
    fun apiChyba(text: String, http: Int, rid: String?, krok: String): TutorChyba {
        var o: Any? = runCatching { JSONObject(text) }.getOrNull()
        var detailText: String? = null
        repeat(4) {
            val j = o as? JSONObject ?: return@repeat
            when {
                j.opt("error") is JSONObject -> o = j.getJSONObject("error")
                j.opt("detail") is JSONObject -> o = j.getJSONObject("detail")
                j.opt("detail") is String -> detailText = j.getString("detail")
            }
        }
        val j = o as? JSONObject
        val kod = (j?.opt("error") as? String) ?: j?.optString("code")?.ifEmpty { null }?.takeIf { it != "null" } ?: j?.optString("type")?.ifEmpty { null } ?: "http_$http"
        val param = j?.optString("param")?.ifEmpty { null }?.takeIf { it != "null" }
        val msg = (j?.optString("message")?.ifEmpty { null }?.takeIf { it != "null" } ?: (j?.opt("error_description") as? String) ?: detailText
            ?: if (j != null) "server vrátil len kód chyby" else text.take(160)).take(240)
        return chyba(kod, "$krok: $msg", http, rid, param)
    }

    /** A network call; no connection at all is [Druh.OFFLINE], not a server error. */
    internal inline fun <T> siet(krok: String, f: () -> T): T = try { f() } catch (e: java.io.IOException) {
        throw TutorChyba(Druh.OFFLINE, "network", "$krok: sieť nedostupná (${e.javaClass.simpleName})")
    }
}

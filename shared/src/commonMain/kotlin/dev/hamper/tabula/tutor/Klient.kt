package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.json.JSONObject

/*
 * The tutor talks to the student's own ChatGPT plan ("Sign in with ChatGPT", token sharing, open-source path:
 * developers.openai.com/siwc/token-sharing-open-source). Sign-in, tokens and HTTP are Android + JVM code
 * (jvmCommonMain/tutor/siwc); common code sees only this interface, so the phone and the harness on the Mac run the
 * same pipeline. docs/TUTOR.md describes the whole feature.
 */

/** What one streamed /v1/responses call produced. */
data class Odpoved(
    val text: String,
    val model: String,
    val usage: Spotreba,
    /** Milliseconds from the request to the first text delta (-1: no text) and to the end. */
    val prvyTextMs: Long,
    val spoluMs: Long,
    val requestId: String?,
    /**
     * The output items other than text (function calls, reasoning with its encrypted content), as the next request of a
     * tool loop must send them back (store:false keeps nothing on the server); empty when the model only wrote.
     */
    val polozky: List<JSONObject> = emptyList(),
) {
    /** The tools the model asked for in this call. */
    val volania: List<Volanie> get() = polozky.filter { it.optString("type") == "function_call" }.map { Volanie(it.optString("call_id"), it.optString("name"), it.optString("arguments")) }
}

/** One function call of the model: its id (for the output), the tool and the JSON arguments. */
data class Volanie(val id: String, val nazov: String, val argumenty: String)

/** Tokens of one call, from `response.completed` (`usage`); what the plan's limit is counted in. */
data class Spotreba(val vstup: Int = 0, val vstupCache: Int = 0, val vystup: Int = 0, val uvazovanie: Int = 0) {
    operator fun plus(o: Spotreba) = Spotreba(vstup + o.vstup, vstupCache + o.vstupCache, vystup + o.vystup, uvazovanie + o.uvazovanie)
}

/**
 * Why a call failed, as the student sees it (docs/TUTOR.md, "Stavy"). [kod] is the server's error code when it gave one
 * (`subscription_sharing_usage_limit_exceeded`, `invalid_grant`…), our own otherwise; nothing here ever holds a token.
 */
class TutorChyba(val druh: Druh, val kod: String, sprava: String, val http: Int? = null, val param: String? = null, val requestId: String? = null) :
    Exception(sprava) {
    enum class Druh {
        /** The plan's limit is used up (429, `…usage_limit_exceeded`, also as `response.failed` in a stream). */
        LIMIT,
        /** ChatGPT does not answer now (503, `usage_unavailable`, `user_unavailable`): try again later. */
        NEDOSTUPNE,
        /** No network on our side (DNS, connect, timeout before any answer). */
        OFFLINE,
        /** The stream ended without `response.completed`. */
        PRERUSENE,
        /** Tokens revoked or expired beyond refresh: connect again. */
        ODPOJENE,
        /** The plan or account does not allow this (`…not_eligible`, missing scope). */
        NEPOVOLENE,
        /** The route refused a parameter (`unsupported_capability`, 400 with `param`). */
        NEPODPOROVANE,
        /** Anything else: a bug on one side or the other. */
        INE,
    }

    /** One line for logs and the harness: no token, no prompt. */
    fun popis() = buildString {
        append(druh).append(' ').append(kod)
        http?.let { append(" (HTTP $it)") }
        param?.let { append(" param=$it") }
        append(": ").append(message)
        requestId?.let { append(" [x-request-id $it]") }
    }

    companion object {
        /** The druh of a server error code and HTTP status (errors-and-recovery in the OpenAI docs). */
        fun druhZ(kod: String, http: Int?): Druh = when {
            "usage_limit" in kod || kod == "rate_limit_exceeded" || http == 429 -> Druh.LIMIT
            kod in setOf("usage_unavailable", "user_unavailable", "subscription_sharing_usage_unavailable", "subscription_sharing_user_unavailable") || "overloaded" in kod || http == 503 || http == 502 || http == 504 -> Druh.NEDOSTUPNE
            kod in setOf("invalid_grant", "invalid_refresh_token", "token_expired", "refresh_token_expired", "refresh_token_invalidated", "refresh_token_reused", "not_signed_in", "subscription_sharing_invalid_user", "account_mismatch") -> Druh.ODPOJENE
            "not_eligible" in kod || kod == "missing_scope" || kod == "insufficient_scope" || http == 403 -> Druh.NEPOVOLENE
            "unsupported" in kod -> Druh.NEPODPOROVANE
            http == 401 -> Druh.ODPOJENE
            else -> Druh.INE
        }
    }
}

/** One call to the model: the request body as the Responses API takes it; `stream` and `store` are set by the client. */
interface ResponsesKlient {
    /** POST /v1/responses with `stream:true, store:false`; [onText] gets each text delta as it arrives. */
    suspend fun odpoved(telo: JSONObject, onText: (String) -> Unit = {}): Odpoved

    /** The model slugs this account may use (GET /v1/models, `visibility == list`). */
    suspend fun modely(): List<String>
}

/** A structured log line without secrets: the phone keeps the last ones for a bug report, the harness prints them. */
fun interface Zaznam {
    fun log(uroven: Char, text: String)
}

object TichyZaznam : Zaznam {
    override fun log(uroven: Char, text: String) {}
}

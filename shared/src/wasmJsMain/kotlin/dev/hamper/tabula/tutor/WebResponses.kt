@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.tutor

import dev.hamper.tabula.compat.currentTimeMillis
import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.TutorChyba.Druh
import dev.hamper.tabula.web.jsAbort
import dev.hamper.tabula.web.jsStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.await

/**
 * POST /v1/responses from the page with the student's token, `stream:true, store:false`, read as server-sent events
 * (web.js); a 401 refreshes the token once and repeats; a cancelled coroutine aborts the request. The phone's
 * HttpResponses (jvmCommonMain) does the same over HttpURLConnection.
 */
class WebResponses(private val siwc: WebSiwc, private val zaznam: Zaznam, private val base: String = "https://api.openai.com/v1") : ResponsesKlient {

    override suspend fun modely(): List<String> {
        val r = siwc.http("GET", "$base/models", JSONObject().put("Authorization", "Bearer ${siwc.obnov()}").put("Accept", "application/json").toString(), null)
        val status = r.optInt("status")
        if (status == 0) throw TutorChyba(Druh.OFFLINE, "network", "GET /models: sieť nedostupná")
        if (status != 200) throw siwc.apiChyba(r.optString("text"), status, r.optString("rid").ifEmpty { null }, "GET /models")
        val j = JSONObject(r.optString("text"))
        val arr = j.optJSONArray("models") ?: j.optJSONArray("data") ?: JSONArray()
        return (0 until arr.length()).map { arr.getJSONObject(it) }
            .filter { it.optString("visibility", "list") == "list" }
            .map { it.optString("slug").ifEmpty { it.optString("id") } }
            .filter { it.isNotEmpty() }
    }

    override suspend fun odpoved(telo: JSONObject, onText: (String) -> Unit): Odpoved {
        val body = JSONObject(telo.toString()).put("stream", true).put("store", false)
        return try { volaj(body, siwc.obnov(), onText) } catch (e: TutorChyba) {
            if (e.http != 401) throw e
            zaznam.log('!', "Responses: 401, obnovujem token a skúšam raz znova")
            volaj(body, siwc.obnov(vynutene = true), onText)
        }
    }

    private suspend fun volaj(body: JSONObject, token: String, onText: (String) -> Unit): Odpoved {
        val krok = "Responses ${body.optString("model")}"
        val t0 = currentTimeMillis()
        val text = StringBuilder()
        var prvy = -1L
        var hotovo: JSONObject? = null
        var chyba: Throwable? = null
        val headers = JSONObject().put("Authorization", "Bearer $token").put("Content-Type", "application/json").put("Accept", "text/event-stream").toString()
        // each event as it arrives; true stops reading (done, or an error to throw after the stream closes)
        val call = jsStream("$base/responses", headers, body.toString()) { data ->
            try {
                val raw = data.toString()
                if (raw.isBlank() || raw == "[DONE]") return@jsStream false
                val e = JSONObject(raw)
                when (e.optString("type")) {
                    "response.output_text.delta" -> { val d = e.optString("delta"); if (prvy < 0) prvy = currentTimeMillis() - t0; text.append(d); onText(d) }
                    "response.completed" -> hotovo = e.optJSONObject("response") ?: JSONObject()
                    "response.failed" -> chyba = siwc.apiChyba(e.optJSONObject("response")?.toString() ?: raw, 200, null, "$krok (response.failed)")
                    "error" -> chyba = siwc.apiChyba(raw, 200, null, "$krok (error v streame)")
                    "response.incomplete" -> chyba = TutorChyba(Druh.PRERUSENE, "response_incomplete", "$krok: odpoveď nedokončená", 200)
                }
                hotovo != null || chyba != null
            } catch (t: Throwable) { chyba = t; true }
        }
        val r = try { JSONObject(call.done.await<JsString>().toString()) } catch (e: CancellationException) { jsAbort(call.id); throw e }
        val status = r.optInt("status")
        val rid = r.optString("rid").ifEmpty { null }
        if (status == 0) throw TutorChyba(Druh.OFFLINE, "network", "$krok: sieť nedostupná (${r.optString("error")})")
        if (status != 200) throw siwc.apiChyba(r.optString("text"), status, rid, krok)
        chyba?.let { throw it }
        val done = hotovo ?: throw TutorChyba(Druh.PRERUSENE, "stream_interrupted", "$krok: stream skončil bez response.completed${r.optString("interrupted").ifEmpty { null }?.let { " ($it)" } ?: ""}", status, null, rid)
        val u = done.optJSONObject("usage")
        val spotreba = Spotreba(
            vstup = u?.optInt("input_tokens") ?: 0,
            vstupCache = u?.optJSONObject("input_tokens_details")?.optInt("cached_tokens") ?: 0,
            vystup = u?.optInt("output_tokens") ?: 0,
            uvazovanie = u?.optJSONObject("output_tokens_details")?.optInt("reasoning_tokens") ?: 0,
        )
        val out = done.optJSONArray("output")
        val items = (0 until (out?.length() ?: 0)).mapNotNull { out!!.optJSONObject(it) }
        val final = text.toString().ifEmpty {
            items.filter { it.optString("type") == "message" }.joinToString("") { m ->
                val cs = m.optJSONArray("content") ?: JSONArray()
                (0 until cs.length()).mapNotNull { cs.optJSONObject(it) }.filter { it.optString("type") == "output_text" }.joinToString("") { it.optString("text") }
            }
        }
        // what a tool loop sends back: every item but the text, without server ids (store:false keeps none of them)
        val polozky = items.filter { it.optString("type") != "message" }.map { JSONObject(it.toString()).apply { remove("id"); remove("status") } }
        return Odpoved(final, done.optString("model").ifEmpty { body.optString("model") }, spotreba, prvy, currentTimeMillis() - t0, rid, polozky)
    }
}

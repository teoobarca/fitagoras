package dev.hamper.tabula.tutor.siwc

import dev.hamper.tabula.compat.json.JSONArray
import dev.hamper.tabula.compat.json.JSONObject
import dev.hamper.tabula.tutor.Odpoved
import dev.hamper.tabula.tutor.ResponsesKlient
import dev.hamper.tabula.tutor.Spotreba
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.tutor.TutorChyba.Druh
import dev.hamper.tabula.tutor.Zaznam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * POST /v1/responses with the student's token, `stream:true` and `store:false` (both required by the preview),
 * read as server-sent events. A 401 refreshes the token once and repeats. Cancelling the coroutine closes the
 * connection. Phone and harness use this same class.
 */
class HttpResponses(private val siwc: Siwc, private val zaznam: Zaznam, private val base: String = "https://api.openai.com/v1") : ResponsesKlient {

    override suspend fun modely(): List<String> = withContext(Dispatchers.IO) {
        val text = get("$base/models", siwc.obnov())
        val j = JSONObject(text)
        val arr = j.optJSONArray("models") ?: j.optJSONArray("data") ?: JSONArray()
        (0 until arr.length()).map { arr.getJSONObject(it) }
            .filter { it.optString("visibility", "list") == "list" }
            .map { it.optString("slug").ifEmpty { it.optString("id") } }
            .filter { it.isNotEmpty() }
    }

    private fun get(url: String, token: String): String {
        val c = siwc.siet("GET /models") {
            (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false; connectTimeout = 15_000; readTimeout = 30_000
                setRequestProperty("Authorization", "Bearer $token"); setRequestProperty("Accept", "application/json")
            }
        }
        val code = siwc.siet("GET /models") { c.responseCode }
        val text = (if (code < 400) c.inputStream else c.errorStream)?.bufferedReader()?.readText() ?: ""
        if (code != 200) throw siwc.apiChyba(text, code, c.getHeaderField("x-request-id"), "GET /models")
        return text
    }

    override suspend fun odpoved(telo: JSONObject, onText: (String) -> Unit): Odpoved = withContext(Dispatchers.IO) {
        val body = JSONObject(telo.toString()).put("stream", true).put("store", false)
        try {
            volaj(body, siwc.obnov(), onText)
        } catch (e: TutorChyba) {
            if (e.http != 401) throw e
            zaznam.log('!', "Responses: 401, obnovujem token a skúšam raz znova")
            volaj(body, siwc.obnov(vynutene = true), onText)
        }
    }

    private suspend fun volaj(body: JSONObject, token: String, onText: (String) -> Unit): Odpoved {
        val krok = "Responses ${body.optString("model")}"
        val t0 = System.currentTimeMillis()
        val c = siwc.siet(krok) {
            (URL("$base/responses").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"; doOutput = true; instanceFollowRedirects = false
                connectTimeout = 15_000; readTimeout = 180_000
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "text/event-stream")
            }
        }
        // a blocking read doesn't see cancellation: a child that waits for it closes the connection, which ends the read
        return kotlinx.coroutines.coroutineScope {
            val closer = launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) { try { kotlinx.coroutines.awaitCancellation() } finally { runCatching { c.disconnect() } } }
            try { cti(c, body, krok, t0, onText) } finally { closer.cancel() }
        }
    }

    private suspend fun cti(c: HttpURLConnection, body: JSONObject, krok: String, t0: Long, onText: (String) -> Unit): Odpoved {
        try {
            siwc.siet(krok) { c.outputStream.use { it.write(body.toString().toByteArray()) } }
            val code = siwc.siet(krok) { c.responseCode }
            val rid = c.getHeaderField("x-request-id")
            if (code != 200) throw siwc.apiChyba(c.errorStream?.bufferedReader()?.readText() ?: "", code, rid, krok)
            val text = StringBuilder()
            var prvy = -1L
            var hotovo: JSONObject? = null
            val data = StringBuilder()
            fun spracuj(raw: String) {
                if (raw.isBlank() || raw == "[DONE]") return
                val e = JSONObject(raw)
                when (e.optString("type")) {
                    "response.output_text.delta" -> {
                        val d = e.optString("delta")
                        if (prvy < 0) prvy = System.currentTimeMillis() - t0
                        text.append(d); onText(d)
                    }
                    "response.completed" -> hotovo = e.optJSONObject("response") ?: JSONObject()
                    "response.failed" -> throw siwc.apiChyba(e.optJSONObject("response")?.toString() ?: raw, code, rid, "$krok (response.failed)")
                    "error" -> throw siwc.apiChyba(raw, code, rid, "$krok (error v streame)")
                    "response.incomplete" -> throw TutorChyba(Druh.PRERUSENE, "response_incomplete",
                        "$krok: odpoveď nedokončená (${e.optJSONObject("response")?.optJSONObject("incomplete_details")?.optString("reason")})", code, null, rid)
                }
            }
            try {
                c.inputStream.bufferedReader().useLines { lines ->
                    for (line in lines) {
                        when {
                            line.startsWith("data:") -> data.append(line.removePrefix("data:").trimStart()).append('\n')
                            line.isEmpty() -> { spracuj(data.toString().trim()); data.clear() }
                        }
                        if (hotovo != null) break
                    }
                }
                if (hotovo == null && data.isNotEmpty()) spracuj(data.toString().trim())
            } catch (e: java.io.IOException) {
                currentCoroutineContext().ensureActive()
                throw TutorChyba(Druh.PRERUSENE, "stream_interrupted", "$krok: spojenie sa prerušilo (${e.javaClass.simpleName})", code, null, rid)
            }
            val r = hotovo ?: throw TutorChyba(Druh.PRERUSENE, "stream_interrupted", "$krok: stream skončil bez response.completed", code, null, rid)
            val u = r.optJSONObject("usage")
            val spotreba = Spotreba(
                vstup = u?.optInt("input_tokens") ?: 0,
                vstupCache = u?.optJSONObject("input_tokens_details")?.optInt("cached_tokens") ?: 0,
                vystup = u?.optInt("output_tokens") ?: 0,
                uvazovanie = u?.optJSONObject("output_tokens_details")?.optInt("reasoning_tokens") ?: 0,
            )
            // a model that answered only in output items (no deltas) still has its text there
            val final = text.toString().ifEmpty { vystupnyText(r) }
            // what a tool loop sends back: every item but the text, without server ids (store:false keeps none of them)
            val out = r.optJSONArray("output")
            val polozky = (0 until (out?.length() ?: 0)).mapNotNull { out!!.optJSONObject(it) }.filter { it.optString("type") != "message" }
                .map { JSONObject(it.toString()).apply { remove("id"); remove("status") } }
            return Odpoved(final, r.optString("model").ifEmpty { body.optString("model") }, spotreba, prvy, System.currentTimeMillis() - t0, rid, polozky)
        } finally {
            runCatching { c.disconnect() }
        }
    }

    private fun vystupnyText(r: JSONObject): String {
        val out = r.optJSONArray("output") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until out.length()) {
            val item = out.optJSONObject(i) ?: continue
            if (item.optString("type") != "message") continue
            val content = item.optJSONArray("content") ?: continue
            for (k in 0 until content.length()) content.optJSONObject(k)?.takeIf { it.optString("type") == "output_text" }?.let { sb.append(it.optString("text")) }
        }
        return sb.toString()
    }
}

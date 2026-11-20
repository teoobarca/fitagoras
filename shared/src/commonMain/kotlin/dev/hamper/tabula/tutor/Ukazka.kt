package dev.hamper.tabula.tutor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.hamper.tabula.compat.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A tutor without a phone or a network, for screenshots and UI tests (snap/TutorShots, screens/TutorFlowUiTest): the
 * camera hands over one page at once, the "model" calls [nastroj] first, returns a fixed transcript, judgement and
 * explanation, or fails as [chyba] says. Everything between them (the engine, the screen, the states) is the real code.
 */
class UkazkovyTutor(
    private val prepis: String,
    private val vysvetlenie: String = "",
    private val chyba: TutorChyba? = null,
    pripojenie: Pripojenie = Pripojenie.Pripojeny("student@example.com", true),
    override var vyvojar: Boolean = true,
    /** The general tutor: the tool its first round calls (over_postup_gem, posud_papier…), and the paper's reading and judgement. */
    private val nastroj: String? = null,
    private val prepisTextu: String = "",
    private val posudok: String = "",
) : TutorSluzba {
    private val stav = MutableStateFlow(pripojenie)
    override val pripojenie: StateFlow<Pripojenie> = stav
    val volania = ArrayList<JSONObject>()

    override fun pripoj(suhlas: Boolean) { stav.value = Pripojenie.Pripojeny("student@example.com", true) }
    override fun odpoj() { stav.value = Pripojenie.Nepripojeny }
    override fun zrusPrihlasenie() { stav.value = Pripojenie.Nepripojeny }
    override suspend fun modely() = listOf("gpt-6.2-sol")
    override fun spravovatVyuzitie() {}
    override var poucenyOPlane = true
    override fun drz(dovod: String?) {}

    override fun klient(): ResponsesKlient = object : ResponsesKlient {
        override suspend fun odpoved(telo: JSONObject, onText: (String) -> Unit): Odpoved {
            volania += telo
            chyba?.let { throw it }
            val schema = telo.optJSONObject("text")?.optJSONObject("format")?.optString("name")
            if (schema == null && nastroj != null && telo.optJSONArray("tools") != null && telo.opt("tool_choice") == null) {
                val inp = telo.optJSONArray("input")
                val uz = inp != null && (0 until inp.length()).any { inp.optJSONObject(it)?.optString("type") == "function_call_output" }
                if (!uz) return Odpoved("", "gpt-6.2-sol", Spotreba(3000, 0, 50), -1, 150, null,
                    listOf(JSONObject().put("type", "function_call").put("call_id", "c1").put("name", nastroj).put("arguments", "{}")))
            }
            val t = when (schema) { "prepis" -> prepis; "prepis_textu" -> prepisTextu; "posudok" -> posudok; else -> vysvetlenie }
            if (schema == null) t.split(' ').forEach { onText("$it ") }
            return Odpoved(t, "gpt-6.2-sol", Spotreba(4000, 0, 400), 100, 200, null)
        }
        override suspend fun modely() = listOf("gpt-6.2-sol")
    }

    @Composable
    override fun Fotak(maxStran: Int, onHotovo: (List<ByteArray>) -> Unit, onZavri: () -> Unit) {
        LaunchedEffect(Unit) { onHotovo(listOf(ByteArray(0))) }
    }
}

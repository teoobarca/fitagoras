package dev.hamper.tabula.data

/**
 * The text of every string literal in Kotlin source, as a student could see it: code inside `${…}` templates and
 * comments is left out (a template becomes a space). For scans of content written in the sources.
 */
class KotlinStrings(private val src: String) {
    private var i = 0
    private val out = ArrayList<String>()

    fun all(): List<String> { i = 0; out.clear(); code(stopAtBrace = false); return out }

    private fun code(stopAtBrace: Boolean) {
        var depth = 0
        while (i < src.length) {
            when {
                src.startsWith("//", i) -> while (i < src.length && src[i] != '\n') i++
                src.startsWith("/*", i) -> i = src.indexOf("*/", i + 2).let { if (it < 0) src.length else it + 2 }
                src[i] == '\'' -> { i++; while (i < src.length && src[i] != '\'') { if (src[i] == '\\') i++; i++ }; i++ }
                src[i] == '"' -> out += string()
                src[i] == '{' -> { depth++; i++ }
                src[i] == '}' -> { if (stopAtBrace && depth == 0) { i++; return }; depth--; i++ }
                else -> i++
            }
        }
    }

    private fun string(): String {
        val raw = src.startsWith("\"\"\"", i)
        i += if (raw) 3 else 1
        val sb = StringBuilder()
        while (i < src.length) {
            if (raw && src.startsWith("\"\"\"", i)) { i += 3; while (i < src.length && src[i] == '"') i++; return sb.toString() }
            if (!raw && src[i] == '"') { i++; return sb.toString() }
            if (!raw && src[i] == '\\') { sb.append(src.getOrNull(i + 1) ?: ' '); i += 2; continue }
            if (src[i] == '$' && i + 1 < src.length && src[i + 1] == '{') { i += 2; code(stopAtBrace = true); sb.append(' '); continue }
            if (src[i] == '$' && i + 1 < src.length && src[i + 1].isLetter()) { i++; while (i < src.length && (src[i].isLetterOrDigit() || src[i] == '_')) i++; sb.append(' '); continue }
            sb.append(src[i]); i++
        }
        return sb.toString()
    }
}

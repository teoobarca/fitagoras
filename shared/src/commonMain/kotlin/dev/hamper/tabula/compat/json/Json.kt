package dev.hamper.tabula.compat.json

/*
 * org.json's API for common code, with Android's org.json semantics (the one the app ran on): insertion order kept,
 * getString coerces numbers and booleans, put(name, null) removes, integral numbers print without ".0", and strings
 * escape the same characters ("/" as "\/"), so the progress document reads and writes exactly as before.
 */

class JSONException(message: String) : RuntimeException(message)

private object Null {
    override fun toString() = "null"
}

class JSONObject() {
    private val map = LinkedHashMap<String, Any>()

    constructor(json: String) : this() {
        val v = Parser(json).document()
        if (v !is JSONObject) throw JSONException("not a JSON object")
        map.putAll(v.map)
    }

    fun length(): Int = map.size
    fun has(name: String): Boolean = map.containsKey(name)
    fun isNull(name: String): Boolean = map[name].let { it == null || it === Null }
    fun keys(): MutableIterator<String> = map.keys.iterator()
    fun remove(name: String): Any? = map.remove(name)

    fun put(name: String, value: Any?): JSONObject {
        if (value == null) map.remove(name) else map[name] = wrap(value)
        return this
    }

    fun opt(name: String): Any? = map[name]
    fun get(name: String): Any = map[name] ?: throw JSONException("No value for $name")

    fun getString(name: String): String = str(get(name)) ?: throw JSONException("No value for $name")
    fun optString(name: String): String = optString(name, "")
    fun optString(name: String, fallback: String): String = opt(name)?.let { str(it) } ?: fallback

    fun getBoolean(name: String): Boolean = bool(get(name)) ?: throw type(name, "boolean")
    fun optBoolean(name: String): Boolean = optBoolean(name, false)
    fun optBoolean(name: String, fallback: Boolean): Boolean = opt(name)?.let { bool(it) } ?: fallback

    fun getInt(name: String): Int = num(get(name))?.toInt() ?: throw type(name, "int")
    fun optInt(name: String): Int = optInt(name, 0)
    fun optInt(name: String, fallback: Int): Int = opt(name)?.let { num(it)?.toInt() } ?: fallback

    fun getLong(name: String): Long = long(get(name)) ?: throw type(name, "long")
    fun optLong(name: String, fallback: Long = 0L): Long = opt(name)?.let { long(it) } ?: fallback

    fun getDouble(name: String): Double = num(get(name)) ?: throw type(name, "double")
    fun optDouble(name: String, fallback: Double = Double.NaN): Double = opt(name)?.let { num(it) } ?: fallback

    fun getJSONObject(name: String): JSONObject = get(name) as? JSONObject ?: throw type(name, "JSONObject")
    fun optJSONObject(name: String): JSONObject? = opt(name) as? JSONObject
    fun getJSONArray(name: String): JSONArray = get(name) as? JSONArray ?: throw type(name, "JSONArray")
    fun optJSONArray(name: String): JSONArray? = opt(name) as? JSONArray

    private fun type(name: String, t: String) = JSONException("Value at $name of type ${map[name]?.let { it::class.simpleName }} cannot be converted to $t")

    override fun toString(): String = StringBuilder().also { write(it) }.toString()

    internal fun write(out: StringBuilder) {
        out.append('{')
        var first = true
        for ((k, v) in map) {
            if (!first) out.append(',')
            first = false
            quote(k, out); out.append(':'); value(v, out)
        }
        out.append('}')
    }

    override fun equals(other: Any?) = other is JSONObject && other.map == map
    override fun hashCode() = map.hashCode()

    companion object {
        /** The JSON null, as org.json's JSONObject.NULL. */
        val NULL: Any = Null
    }
}

class JSONArray() {
    private val list = ArrayList<Any>()

    constructor(json: String) : this() {
        val v = Parser(json).document()
        if (v !is JSONArray) throw JSONException("not a JSON array")
        list.addAll(v.list)
    }

    constructor(items: Collection<*>) : this() {
        items.forEach { list += if (it == null) Null else wrap(it) }
    }

    fun length(): Int = list.size
    fun put(value: Any?): JSONArray { list += if (value == null) Null else wrap(value); return this }
    fun opt(index: Int): Any? = list.getOrNull(index)
    fun get(index: Int): Any = list.getOrNull(index) ?: throw JSONException("Index $index out of range [0..${list.size})")
    fun isNull(index: Int): Boolean = opt(index).let { it == null || it === Null }

    fun getString(index: Int): String = str(get(index)) ?: throw JSONException("null at $index")
    fun optString(index: Int, fallback: String = ""): String = opt(index)?.let { str(it) } ?: fallback
    fun getInt(index: Int): Int = num(get(index))?.toInt() ?: throw JSONException("not an int at $index")
    fun getLong(index: Int): Long = long(get(index)) ?: throw JSONException("not a long at $index")
    fun getDouble(index: Int): Double = num(get(index)) ?: throw JSONException("not a double at $index")
    fun getBoolean(index: Int): Boolean = bool(get(index)) ?: throw JSONException("not a boolean at $index")
    fun getJSONObject(index: Int): JSONObject = get(index) as? JSONObject ?: throw JSONException("not an object at $index")
    fun optJSONObject(index: Int): JSONObject? = opt(index) as? JSONObject
    fun getJSONArray(index: Int): JSONArray = get(index) as? JSONArray ?: throw JSONException("not an array at $index")
    fun optJSONArray(index: Int): JSONArray? = opt(index) as? JSONArray

    override fun toString(): String = StringBuilder().also { write(it) }.toString()

    internal fun write(out: StringBuilder) {
        out.append('[')
        list.forEachIndexed { i, v -> if (i > 0) out.append(','); value(v, out) }
        out.append(']')
    }

    override fun equals(other: Any?) = other is JSONArray && other.list == list
    override fun hashCode() = list.hashCode()
}

// ---- values ----------------------------------------------------------------------------------------------------

private fun wrap(v: Any): Any = when (v) {
    is JSONObject, is JSONArray, is String, is Boolean, Null -> v
    is Double -> { require(!v.isNaN() && !v.isInfinite()) { "Forbidden numeric value: $v" }; v }
    is Float -> wrap(v.toDouble())
    is Number -> v
    is Char -> v.toString()
    is Collection<*> -> JSONArray(v)
    is Array<*> -> JSONArray(v.toList())
    is IntArray -> JSONArray(v.toList())
    is LongArray -> JSONArray(v.toList())
    is Map<*, *> -> JSONObject().also { o -> v.forEach { (k, x) -> o.put(k.toString(), x) } }
    else -> v.toString()
}

private fun str(v: Any): String? = when (v) {
    is String -> v
    Null -> "null"
    is Number -> numberToString(v)
    else -> v.toString()
}

private fun bool(v: Any): Boolean? = when (v) {
    is Boolean -> v
    is String -> when (v.lowercase()) { "true" -> true; "false" -> false; else -> null }
    else -> null
}

private fun num(v: Any): Double? = when (v) {
    is Number -> v.toDouble()
    is String -> v.toDoubleOrNull()
    else -> null
}

private fun long(v: Any): Long? = when (v) {
    is Long -> v
    is Int -> v.toLong()
    is Number -> v.toDouble().toLong()
    is String -> v.toLongOrNull() ?: v.toDoubleOrNull()?.toLong()
    else -> null
}

/** Android's JSONObject.numberToString: integral values without a fraction. */
internal fun numberToString(n: Number): String {
    if (n is Int || n is Long || n is Short || n is Byte) return n.toString()
    val d = n.toDouble()
    if (d == 0.0 && 1.0 / d < 0) return "-0"
    val l = d.toLong()
    return if (d == l.toDouble()) l.toString() else d.toString()
}

private fun value(v: Any, out: StringBuilder) {
    when (v) {
        is JSONObject -> v.write(out)
        is JSONArray -> v.write(out)
        is String -> quote(v, out)
        is Number -> out.append(numberToString(v))
        else -> out.append(v.toString())
    }
}

private fun quote(s: String, out: StringBuilder) {
    out.append('"')
    for (c in s) {
        when (c) {
            '"', '\\', '/' -> out.append('\\').append(c)
            '\t' -> out.append("\\t")
            '\b' -> out.append("\\b")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            '\u000C' -> out.append("\\f")
            else -> if (c.code <= 0x1F) out.append("\\u").append(c.code.toString(16).padStart(4, '0')) else out.append(c)
        }
    }
    out.append('"')
}

// ---- parser ----------------------------------------------------------------------------------------------------

private class Parser(private val s: String) {
    private var i = 0

    fun document(): Any {
        val v = value()
        ws()
        if (i < s.length) fail("trailing characters")
        return v
    }

    private fun fail(why: String): Nothing = throw JSONException("$why at character $i")

    private fun ws() {
        while (i < s.length) {
            val c = s[i]
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') i++ else break
        }
    }

    private fun value(): Any {
        ws()
        if (i >= s.length) fail("End of input")
        return when (val c = s[i]) {
            '{' -> obj()
            '[' -> arr()
            '"' -> string()
            else -> if (c == '-' || c.isDigit()) number() else literal()
        }
    }

    private fun obj(): JSONObject {
        val o = JSONObject()
        i++
        ws()
        if (s.getOrNull(i) == '}') { i++; return o }
        while (true) {
            ws()
            if (s.getOrNull(i) != '"') fail("Expected a name")
            val k = string()
            ws()
            if (s.getOrNull(i) != ':') fail("Expected ':' after $k")
            i++
            val v = value()
            if (v === Null) o.put(k, JSONObject.NULL) else o.put(k, v)
            ws()
            when (s.getOrNull(i)) {
                ',' -> i++
                '}' -> { i++; return o }
                else -> fail("Unterminated object")
            }
        }
    }

    private fun arr(): JSONArray {
        val a = JSONArray()
        i++
        ws()
        if (s.getOrNull(i) == ']') { i++; return a }
        while (true) {
            a.put(value().let { if (it === Null) JSONObject.NULL else it })
            ws()
            when (s.getOrNull(i)) {
                ',' -> i++
                ']' -> { i++; return a }
                else -> fail("Unterminated array")
            }
        }
    }

    private fun string(): String {
        i++
        val b = StringBuilder()
        while (true) {
            if (i >= s.length) fail("Unterminated string")
            val c = s[i++]
            when (c) {
                '"' -> return b.toString()
                '\\' -> {
                    if (i >= s.length) fail("Unterminated escape")
                    when (val e = s[i++]) {
                        'u' -> {
                            if (i + 4 > s.length) fail("Unterminated escape")
                            b.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4
                        }
                        't' -> b.append('\t')
                        'b' -> b.append('\b')
                        'n' -> b.append('\n')
                        'r' -> b.append('\r')
                        'f' -> b.append('\u000C')
                        else -> b.append(e)
                    }
                }
                else -> b.append(c)
            }
        }
    }

    private fun number(): Any {
        val st = i
        if (s[i] == '-') i++
        while (i < s.length && (s[i].isDigit() || s[i] in ".eE+-")) i++
        val t = s.substring(st, i)
        if (t.none { it == '.' || it == 'e' || it == 'E' }) {
            t.toLongOrNull()?.let { l -> return if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l }
        }
        return t.toDoubleOrNull() ?: fail("Bad number $t")
    }

    private fun literal(): Any = when {
        s.startsWith("true", i) -> { i += 4; true }
        s.startsWith("false", i) -> { i += 5; false }
        s.startsWith("null", i) -> { i += 4; Null }
        else -> fail("Unexpected character '${s[i]}'")
    }
}

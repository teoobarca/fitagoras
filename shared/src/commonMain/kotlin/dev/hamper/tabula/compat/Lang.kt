package dev.hamper.tabula.compat

import kotlinx.coroutines.CoroutineDispatcher

/*
 * The JVM calls common code makes (java.lang statics, synchronized, ConcurrentHashMap, BigInteger, Dispatchers.IO).
 * Common code calls these names; on Android and the desktop they are the JVM's own.
 */

/** java.lang.Math's exact arithmetic: throws ArithmeticException on overflow, like the JVM. */
object JMath {
    fun multiplyExact(a: Long, b: Long): Long {
        val r = a * b
        if ((a != 0L && (r / a != b || (a == -1L && b == Long.MIN_VALUE)))) throw ArithmeticException("long overflow")
        return r
    }
    fun multiplyExact(a: Int, b: Int): Int {
        val r = a.toLong() * b
        if (r.toInt().toLong() != r) throw ArithmeticException("integer overflow")
        return r.toInt()
    }
    fun multiplyExact(a: Long, b: Int): Long = multiplyExact(a, b.toLong())
    fun addExact(a: Long, b: Long): Long {
        val r = a + b
        if (((a xor r) and (b xor r)) < 0) throw ArithmeticException("long overflow")
        return r
    }
    fun addExact(a: Int, b: Int): Int {
        val r = a.toLong() + b
        if (r.toInt().toLong() != r) throw ArithmeticException("integer overflow")
        return r.toInt()
    }
    fun subtractExact(a: Long, b: Long): Long {
        val r = a - b
        if (((a xor b) and (a xor r)) < 0) throw ArithmeticException("long overflow")
        return r
    }
    fun subtractExact(a: Int, b: Int): Int {
        val r = a.toLong() - b
        if (r.toInt().toLong() != r) throw ArithmeticException("integer overflow")
        return r.toInt()
    }
    fun negateExact(a: Long): Long { if (a == Long.MIN_VALUE) throw ArithmeticException("long overflow"); return -a }
    fun negateExact(a: Int): Int { if (a == Int.MIN_VALUE) throw ArithmeticException("integer overflow"); return -a }
    fun toIntExact(a: Long): Int { if (a.toInt().toLong() != a) throw ArithmeticException("integer overflow"); return a.toInt() }
    fun floorDiv(a: Long, b: Long): Long = a.floorDiv(b)
    fun floorDiv(a: Int, b: Int): Int = a.floorDiv(b)
    fun floorMod(a: Long, b: Long): Long = a.mod(b)
    fun floorMod(a: Int, b: Int): Int = a.mod(b)
    fun floorMod(a: Long, b: Int): Int = a.mod(b.toLong()).toInt()
    fun abs(a: Int): Int = kotlin.math.abs(a)
    fun abs(a: Long): Long = kotlin.math.abs(a)
    fun abs(a: Double): Double = kotlin.math.abs(a)
    fun abs(a: Float): Float = kotlin.math.abs(a)
}

object JInteger {
    fun numberOfLeadingZeros(i: Int): Int = i.countLeadingZeroBits()
    fun numberOfTrailingZeros(i: Int): Int = i.countTrailingZeroBits()
    fun bitCount(i: Int): Int = i.countOneBits()
}

object JCharacter {
    /** A code point as UTF-16 (one char, or a surrogate pair above U+FFFF). */
    fun toChars(cp: Int): CharArray = if (cp < 0x10000) charArrayOf(cp.toChar()) else {
        val v = cp - 0x10000
        charArrayOf((0xD800 + (v shr 10)).toChar(), (0xDC00 + (v and 0x3FF)).toChar())
    }
}

/** java.lang.String.format: on Android and the JVM the JVM's own (default locale, every pattern); the browser has [formatPortable]. */
expect fun String.format(vararg args: Any?): String

/** String.format for the browser: %d, %s, %f with flags - 0 + space and a width, %.Nf, %% (no locale: a decimal point). */
internal fun String.formatPortable(vararg args: Any?): String {
    val out = StringBuilder()
    var arg = 0
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c != '%') { out.append(c); i++; continue }
        val m = Spec.matchAt(this, i) ?: throw IllegalArgumentException("unsupported format at $i: $this")
        val (flags, width, prec, conv) = m.destructured
        i += m.value.length
        if (conv == "%") { out.append('%'); continue }
        if (conv == "n") { out.append('\n'); continue }
        val v = args[arg++]
        var s = when (conv) {
            "d" -> (v as Number).toLong().toString()
            "s" -> v.toString()
            "f" -> fixed((v as Number).toDouble(), prec.ifEmpty { "6" }.toInt())
            else -> throw IllegalArgumentException("unsupported conversion %$conv")
        }
        if (conv != "s" && !s.startsWith("-")) { if ('+' in flags) s = "+$s" else if (' ' in flags) s = " $s" }
        if (width.isNotEmpty() && s.length < width.toInt()) {
            val w = width.toInt()
            s = when {
                '-' in flags -> s.padEnd(w)
                '0' in flags && conv != "s" -> if (s[0] in "-+ ") s[0] + s.drop(1).padStart(w - 1, '0') else s.padStart(w, '0')
                else -> s.padStart(w)
            }
        }
        out.append(s)
    }
    return out.toString()
}

private val Spec = Regex("""%([-0+ ]*)(\d*)(?:\.(\d+))?([dsfn%])""")

/** Rounds half up on the shortest decimal form of [d], as java.util.Formatter does ("%.1f" of 2.25 is 2.3). */
private fun fixed(d: Double, digits: Int): String {
    val neg = d < 0
    var t = kotlin.math.abs(d).toString()
    if ('E' in t || 'e' in t) t = expand(t)
    val dot = t.indexOf('.').let { if (it < 0) { t += "."; t.length - 1 } else it }
    val whole = t.substring(0, dot)
    val frac = t.substring(dot + 1).padEnd(digits + 1, '0')
    var digitsStr = (whole + frac.substring(0, digits)).trimStart('0').ifEmpty { "0" }
    if (frac[digits] >= '5') digitsStr = increment(digitsStr)
    val all = digitsStr.padStart(digits + 1, '0')
    val r = if (digits == 0) all else all.dropLast(digits) + "." + all.takeLast(digits)
    return if (neg) "-$r" else r
}

private fun increment(n: String): String {
    val c = n.toCharArray()
    var i = c.lastIndex
    while (i >= 0) { if (c[i] == '9') { c[i] = '0'; i-- } else { c[i] = c[i] + 1; return c.concatToString() } }
    return "1" + c.concatToString()
}

/** 1.5E-7 -> 0.00000015, 1.5E7 -> 15000000.0 */
private fun expand(t: String): String {
    val (m, e) = t.uppercase().split('E').let { it[0] to it[1].toInt() }
    val digits = m.replace(".", "")
    val point = (m.indexOf('.').takeIf { it >= 0 } ?: m.length) + e
    return when {
        point <= 0 -> "0." + "0".repeat(-point) + digits
        point >= digits.length -> digits + "0".repeat(point - digits.length) + ".0"
        else -> digits.substring(0, point) + "." + digits.substring(point)
    }
}

expect fun currentTimeMillis(): Long
expect fun nanoTime(): Long

/** kotlin.synchronized on the JVM and Android; the browser has one thread. */
expect inline fun <R> synchronized(lock: Any, block: () -> R): R

/** java.util.concurrent.ConcurrentHashMap on the JVM and Android; a plain map in the browser. */
class ConcurrentHashMap<K, V> : MutableMap<K, V> by concurrentMap()

internal expect fun <K, V> concurrentMap(): MutableMap<K, V>

/** java.util.TreeSet's iteration order: ascending. */
fun <T : Comparable<T>> sortedSetOf(vararg elements: T): MutableSet<T> = SortedSet<T>().also { it.addAll(elements) }

private class SortedSet<T : Comparable<T>> : AbstractMutableSet<T>() {
    private val items = ArrayList<T>()
    override val size get() = items.size
    override fun add(element: T): Boolean {
        val i = items.binarySearch(element)
        if (i >= 0) return false
        items.add(-i - 1, element)
        return true
    }
    override fun iterator(): MutableIterator<T> = items.iterator()
}

/** java.util.Map.putIfAbsent: the value already there, or null after putting [value]. */
fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V): V? = get(key) ?: run { put(key, value); null }

/** Dispatchers.IO where there is one (JVM, Android); the browser writes on Default. */
expect val IoDispatcher: CoroutineDispatcher

/** android.util.LruCache's get/put on Android itself, an access-ordered map elsewhere. */
expect class LruCache<K, V>(maxSize: Int) {
    fun get(key: K): V?
    fun put(key: K, value: V): V?
}

/** java.math.BigInteger on the JVM and Android; a small exact implementation in the browser. */
expect class BigInteger : Comparable<BigInteger> {
    fun pow(exponent: Int): BigInteger
    fun add(other: BigInteger): BigInteger
    fun subtract(other: BigInteger): BigInteger
    fun multiply(other: BigInteger): BigInteger
    fun divide(other: BigInteger): BigInteger
}

expect fun bigIntegerOf(v: Long): BigInteger
expect fun BigInteger.toInt(): Int
expect fun BigInteger.toLong(): Long
val BigIntegerOne: BigInteger get() = bigIntegerOf(1)
val BigIntegerZero: BigInteger get() = bigIntegerOf(0)
operator fun BigInteger.plus(o: BigInteger): BigInteger = add(o)
operator fun BigInteger.minus(o: BigInteger): BigInteger = subtract(o)
operator fun BigInteger.times(o: BigInteger): BigInteger = multiply(o)
operator fun BigInteger.div(o: BigInteger): BigInteger = divide(o)

/** java.util.Collections.synchronizedList on the JVM and Android; the list itself in the browser (one thread). */
expect fun <T> synchronizedList(list: MutableList<T>): MutableList<T>

/** java.net.URI's resolve, for relative links in server answers: java.net.URI on the JVM and Android. */
expect class URI(str: String) {
    fun resolve(str: String): URI
}

/** java.util.Locale for String.format(Locale, ...): only the locales the app names. */
class Locale private constructor(val tag: String) {
    companion object {
        val ROOT = Locale("")
    }
}

/** String.format(Locale, pattern, args): the JVM's own on Android and the desktop. */
expect fun String.Companion.format(locale: Locale, format: String, vararg args: Any?): String

/** java.io.File on the JVM and Android (an update's download); a path elsewhere. */
expect class File

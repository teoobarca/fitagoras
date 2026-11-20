@file:OptIn(ExperimentalWasmJsInterop::class)

package dev.hamper.tabula.compat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private fun dateNow(): Double = js("Date.now()")
private fun perfNow(): Double = js("performance.now()")

actual fun currentTimeMillis(): Long = dateNow().toLong()
actual fun String.format(vararg args: Any?): String = formatPortable(*args)
actual fun nanoTime(): Long = (perfNow() * 1_000_000.0).toLong()

@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> synchronized(lock: Any, block: () -> R): R = block()

internal actual fun <K, V> concurrentMap(): MutableMap<K, V> = HashMap()

actual val IoDispatcher: CoroutineDispatcher get() = Dispatchers.Default

// ---- BigInteger on the browser's BigInt -------------------------------------------------------------------------

private fun bigOf(s: String): JsAny = js("BigInt(s)")
private fun bigAdd(a: JsAny, b: JsAny): JsAny = js("a + b")
private fun bigSub(a: JsAny, b: JsAny): JsAny = js("a - b")
private fun bigMul(a: JsAny, b: JsAny): JsAny = js("a * b")
private fun bigDiv(a: JsAny, b: JsAny): JsAny = js("a / b")
private fun bigPow(a: JsAny, e: Int): JsAny = js("a ** BigInt(e)")
private fun bigCmp(a: JsAny, b: JsAny): Int = js("a < b ? -1 : (a > b ? 1 : 0)")
private fun bigStr(a: JsAny): String = js("a.toString()")

actual class BigInteger internal constructor(internal val v: JsAny) : Comparable<BigInteger> {
    actual fun pow(exponent: Int): BigInteger = BigInteger(bigPow(v, exponent))
    actual fun add(other: BigInteger): BigInteger = BigInteger(bigAdd(v, other.v))
    actual fun subtract(other: BigInteger): BigInteger = BigInteger(bigSub(v, other.v))
    actual fun multiply(other: BigInteger): BigInteger = BigInteger(bigMul(v, other.v))
    /** Truncates toward zero, like java.math.BigInteger. */
    actual fun divide(other: BigInteger): BigInteger = BigInteger(bigDiv(v, other.v))
    override fun compareTo(other: BigInteger): Int = bigCmp(v, other.v)
    override fun equals(other: Any?) = other is BigInteger && bigCmp(v, other.v) == 0
    override fun hashCode() = toString().hashCode()
    override fun toString(): String = bigStr(v)
}

actual fun bigIntegerOf(v: Long): BigInteger = BigInteger(bigOf(v.toString()))
actual fun BigInteger.toInt(): Int = toLong().toInt()
actual fun BigInteger.toLong(): Long = toString().toLong()

actual fun <T> synchronizedList(list: MutableList<T>): MutableList<T> = list

private fun urlResolve(rel: String, base: String): String = js("new URL(rel, base).href")

actual class URI actual constructor(private val str: String) {
    actual fun resolve(str: String): URI = URI(urlResolve(str, this.str))
    override fun toString() = str
}

actual fun String.Companion.format(locale: Locale, format: String, vararg args: Any?): String = format.formatPortable(*args)

actual class File(val path: String)

package dev.hamper.tabula.compat

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun String.format(vararg args: Any?): String = java.lang.String.format(this, *args)
actual fun nanoTime(): Long = System.nanoTime()

@Suppress("NOTHING_TO_INLINE")
actual inline fun <R> synchronized(lock: Any, block: () -> R): R = kotlin.synchronized(lock, block)

internal actual fun <K, V> concurrentMap(): MutableMap<K, V> = java.util.concurrent.ConcurrentHashMap<K, V>()

actual val IoDispatcher: CoroutineDispatcher get() = Dispatchers.IO

actual typealias BigInteger = java.math.BigInteger
actual fun bigIntegerOf(v: Long): BigInteger = java.math.BigInteger.valueOf(v)
actual fun BigInteger.toInt(): Int = (this as Number).toInt()
actual fun BigInteger.toLong(): Long = (this as Number).toLong()

actual fun <T> synchronizedList(list: MutableList<T>): MutableList<T> = java.util.Collections.synchronizedList(list)
actual typealias URI = java.net.URI
actual fun String.Companion.format(locale: Locale, format: String, vararg args: Any?): String =
    java.lang.String.format(java.util.Locale.forLanguageTag(locale.tag), format, *args)
actual typealias File = java.io.File

@file:OptIn(kotlin.time.ExperimentalTime::class)

package dev.hamper.tabula.compat.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/*
 * The java.time the app uses, for common code: kotlinx-datetime types under the java.time names, java.time's methods
 * as extensions (same results: wall-clock arithmetic, ISO text), and the few classes kotlinx has no counterpart for
 * (ZonedDateTime, ZoneId, Duration.between, ChronoUnit, a DateTimeFormatter for the patterns we print).
 * Common code imports these instead of java.time.*.
 */

typealias LocalDate = kotlinx.datetime.LocalDate
typealias LocalDateTime = kotlinx.datetime.LocalDateTime
typealias LocalTime = kotlinx.datetime.LocalTime
typealias DayOfWeek = kotlinx.datetime.DayOfWeek
typealias Month = kotlinx.datetime.Month

private val zone: TimeZone get() = TimeZone.currentSystemDefault()

// ---- LocalDate ------------------------------------------------------------------------------------------------

fun kotlinx.datetime.LocalDate.Companion.of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)
fun kotlinx.datetime.LocalDate.Companion.now(): LocalDate = Clock.System.todayIn(zone)
fun LocalDate.plusDays(days: Long): LocalDate = plus(DatePeriod(days = days.toInt()))
fun LocalDate.plusDays(days: Int): LocalDate = plus(DatePeriod(days = days))
fun LocalDate.minusDays(days: Long): LocalDate = minus(DatePeriod(days = days.toInt()))
fun LocalDate.minusDays(days: Int): LocalDate = minus(DatePeriod(days = days))
fun LocalDate.plusWeeks(weeks: Long): LocalDate = plusDays(weeks * 7)
fun LocalDate.isBefore(other: LocalDate): Boolean = this < other
fun LocalDate.isAfter(other: LocalDate): Boolean = this > other
fun LocalDate.isEqual(other: LocalDate): Boolean = this == other
fun LocalDate.atStartOfDay(): LocalDateTime = LocalDateTime(this, LocalTime(0, 0))
fun LocalDate.atStartOfDay(zone: ZoneId): ZonedDateTime = ZonedDateTime(atStartOfDayIn(zone.tz).toLocalDateTime(zone.tz), zone)
fun LocalDate.atTime(time: LocalTime): LocalDateTime = LocalDateTime(this, time)
fun LocalDate.atTime(hour: Int, minute: Int, second: Int = 0, nanosecond: Int = 0): LocalDateTime = LocalDateTime(this, LocalTime(hour, minute, second, nanosecond))
fun LocalDate.toEpochDay(): Long = LocalDate(1970, 1, 1).daysUntil(this).toLong()
val LocalDate.monthValue: Int get() = month.number
fun LocalDate.format(f: DateTimeFormatter): String = f.format(this, null)

// ---- LocalDateTime --------------------------------------------------------------------------------------------

fun kotlinx.datetime.LocalDateTime.Companion.of(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0): LocalDateTime =
    LocalDateTime(year, month, day, hour, minute, second)
fun kotlinx.datetime.LocalDateTime.Companion.of(date: LocalDate, time: LocalTime): LocalDateTime = LocalDateTime(date, time)
fun kotlinx.datetime.LocalDateTime.Companion.now(): LocalDateTime = Clock.System.now().toLocalDateTime(zone)
/** The latest date-time, a sentinel for "never" in comparisons (java.time's LocalDateTime.MAX). */
val kotlinx.datetime.LocalDateTime.Companion.MAX: LocalDateTime get() = MaxDateTime
private val MaxDateTime = LocalDateTime(LocalDate(999_999, 12, 31), LocalTime(23, 59, 59, 999_999_999))
fun LocalDateTime.toLocalDate(): LocalDate = date
fun LocalDateTime.toLocalTime(): LocalTime = time
fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other
fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other
fun LocalDateTime.isEqual(other: LocalDateTime): Boolean = this == other
/** Wall-clock arithmetic, as java.time: no time zone involved. */
fun LocalDateTime.plusMinutes(minutes: Long): LocalDateTime =
    toInstant(TimeZone.UTC).plus(minutes.minutes).toLocalDateTime(TimeZone.UTC)
fun LocalDateTime.minusMinutes(minutes: Long): LocalDateTime = plusMinutes(-minutes)
fun LocalDateTime.plusHours(hours: Long): LocalDateTime = plusMinutes(hours * 60)
fun LocalDateTime.plusDays(days: Long): LocalDateTime = LocalDateTime(date.plusDays(days), time)
fun LocalDateTime.minusDays(days: Long): LocalDateTime = LocalDateTime(date.minusDays(days), time)
fun LocalDateTime.atZone(zone: ZoneId): ZonedDateTime = ZonedDateTime(this, zone)
fun LocalDateTime.format(f: DateTimeFormatter): String = f.format(date, time)
val LocalDateTime.monthValue: Int get() = month.number

// ---- LocalTime, DayOfWeek -------------------------------------------------------------------------------------

fun kotlinx.datetime.LocalTime.Companion.of(hour: Int, minute: Int, second: Int = 0): LocalTime = LocalTime(hour, minute, second)
val kotlinx.datetime.LocalTime.Companion.MIDNIGHT: LocalTime get() = LocalTime(0, 0)
val kotlinx.datetime.LocalTime.Companion.NOON: LocalTime get() = LocalTime(12, 0)
fun LocalTime.isBefore(other: LocalTime): Boolean = this < other
fun LocalTime.isAfter(other: LocalTime): Boolean = this > other
fun LocalTime.format(f: DateTimeFormatter): String = f.format(null, this)

/** ISO number, Monday 1 ... Sunday 7 (java.time's getValue). */
val DayOfWeek.value: Int get() = isoDayNumber

// ---- zones, instants, durations -------------------------------------------------------------------------------

class ZoneId internal constructor(val tz: TimeZone) {
    override fun equals(other: Any?) = other is ZoneId && other.tz == tz
    override fun hashCode() = tz.hashCode()
    override fun toString() = tz.id

    companion object {
        fun of(id: String): ZoneId = ZoneId(TimeZone.of(id))
        fun systemDefault(): ZoneId = ZoneId(TimeZone.currentSystemDefault())
    }
}

object ZoneOffset {
    val UTC: ZoneId = ZoneId(TimeZone.UTC)
}

class Instant internal constructor(val instant: kotlin.time.Instant) : Comparable<Instant> {
    fun toEpochMilli(): Long = instant.toEpochMilliseconds()
    fun atZone(zone: ZoneId): ZonedDateTime = ZonedDateTime(instant.toLocalDateTime(zone.tz), zone)
    override fun compareTo(other: Instant) = instant.compareTo(other.instant)
    override fun equals(other: Any?) = other is Instant && other.instant == instant
    override fun hashCode() = instant.hashCode()
    override fun toString() = instant.toString()

    companion object {
        fun ofEpochMilli(ms: Long): Instant = Instant(kotlin.time.Instant.fromEpochMilliseconds(ms))
        fun now(): Instant = Instant(Clock.System.now())
    }
}

class ZonedDateTime internal constructor(private val local: LocalDateTime, val zone: ZoneId) {
    fun toLocalDate(): LocalDate = local.date
    fun toLocalTime(): LocalTime = local.time
    fun toLocalDateTime(): LocalDateTime = local
    fun toInstant(): Instant = Instant(local.toInstant(zone.tz))
    override fun equals(other: Any?) = other is ZonedDateTime && other.local == local && other.zone == zone
    override fun hashCode() = local.hashCode() * 31 + zone.hashCode()
    override fun toString() = "$local[$zone]"

    companion object {
        fun now(): ZonedDateTime = ZonedDateTime(Clock.System.now().toLocalDateTime(zone), ZoneId(zone))

        /** ISO with a region, as java.time prints it: 2026-11-11T23:59:59.750+01:00[Europe/Bratislava]. */
        fun parse(text: String): ZonedDateTime {
            val region = text.substringAfter('[', "").substringBefore(']')
            val body = text.substringBefore('[')
            val m = Regex("""^(.*?)(Z|[+-]\d\d:\d\d)?$""").find(body) ?: throw IllegalArgumentException(text)
            val local = LocalDateTime.parse(m.groupValues[1])
            val z = when {
                region.isNotEmpty() -> ZoneId.of(region)
                m.groupValues[2] == "Z" -> ZoneOffset.UTC
                else -> ZoneId.of(m.groupValues[2])
            }
            return ZonedDateTime(local, z)
        }
    }
}

class Duration private constructor(private val d: kotlin.time.Duration) {
    fun toMillis(): Long = d.inWholeMilliseconds
    fun toMinutes(): Long = d.inWholeMinutes
    fun toHours(): Long = d.inWholeHours
    fun toDays(): Long = d.inWholeDays
    fun getSeconds(): Long = d.inWholeSeconds

    companion object {
        fun between(a: ZonedDateTime, b: ZonedDateTime): Duration = Duration(b.toInstant().instant - a.toInstant().instant)
        fun between(a: Instant, b: Instant): Duration = Duration(b.instant - a.instant)
        fun ofMillis(ms: Long): Duration = Duration(ms.milliseconds)
    }
}

enum class ChronoUnit {
    DAYS, WEEKS;

    /** Whole units from [a] to [b], truncated toward zero, as java.time. */
    fun between(a: LocalDate, b: LocalDate): Long {
        val days = a.daysUntil(b).toLong()
        return when (this) { DAYS -> days; WEEKS -> days / 7 }
    }
}

/**
 * The DateTimeFormatter patterns the app prints: d, dd, M, MM, yy, yyyy, H, HH, m, mm, s, ss and literal text
 * ('quoted' or any non-letter). Other letters fail at construction, so a new pattern shows up in a test.
 */
class DateTimeFormatter private constructor(private val parts: List<Pair<Char, Int>>) {
    internal fun format(d: LocalDate?, t: LocalTime?): String = buildString {
        for ((c, n) in parts) when (c) {
            '\u0000' -> append(lit(n))
            'd' -> append(pad(d!!.day, n))
            'M' -> append(pad(d!!.month.number, n))
            'y' -> append(if (n == 2) pad(d!!.year % 100, 2) else pad(d!!.year, n))
            'H' -> append(pad(t!!.hour, n))
            'm' -> append(pad(t!!.minute, n))
            's' -> append(pad(t!!.second, n))
        }
    }

    private val literals = ArrayList<String>()
    private fun lit(i: Int) = literals[i]
    private fun pad(v: Int, n: Int) = v.toString().padStart(n, '0')

    companion object {
        fun ofPattern(pattern: String): DateTimeFormatter {
            val parts = ArrayList<Pair<Char, Int>>()
            val lits = ArrayList<String>()
            var i = 0
            fun literal(s: String) { lits += s; parts += '\u0000' to lits.lastIndex }
            while (i < pattern.length) {
                val c = pattern[i]
                when {
                    c == '\'' -> {
                        val end = pattern.indexOf('\'', i + 1).takeIf { it > 0 } ?: throw IllegalArgumentException("unclosed quote: $pattern")
                        literal(if (end == i + 1) "'" else pattern.substring(i + 1, end)); i = end + 1
                    }
                    c.isLetter() -> {
                        require(c in "dMyHms") { "pattern letter '$c' is not supported: $pattern" }
                        var n = 1
                        while (i + n < pattern.length && pattern[i + n] == c) n++
                        parts += c to n; i += n
                    }
                    else -> { literal(c.toString()); i++ }
                }
            }
            return DateTimeFormatter(parts).also { it.literals.addAll(lits) }
        }
    }
}

package dev.hamper.tabula.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import dev.hamper.tabula.compat.time.Duration
import dev.hamper.tabula.compat.time.LocalDate
import dev.hamper.tabula.compat.time.ZonedDateTime
import dev.hamper.tabula.compat.time.*

/** Re-read on every resume; no timer runs while the app is in the background. */
@Composable
internal fun rememberCurrentDate(): LocalDate {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val today by produceState(LocalDate.now(), lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                val now = ZonedDateTime.now()
                value = now.toLocalDate()
                // Wake at midnight, or within a minute of a clock/time-zone change.
                delay(millisUntilNextDate(now).coerceAtMost(60_000L))
            }
        }
    }
    return today
}

internal fun millisUntilNextDate(now: ZonedDateTime): Long =
    Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay(now.zone))
        .toMillis().coerceAtLeast(1L)

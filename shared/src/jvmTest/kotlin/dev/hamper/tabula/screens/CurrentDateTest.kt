package dev.hamper.tabula.screens

import org.junit.Assert.assertEquals
import org.junit.Test
import dev.hamper.tabula.compat.time.ZonedDateTime
import dev.hamper.tabula.compat.time.*

class CurrentDateTest {
    @Test fun wakesAtMidnight() {
        assertEquals(250L, millisUntilNextDate(ZonedDateTime.parse("2026-11-11T23:59:59.750+01:00[Europe/Bratislava]")))
    }

    @Test fun midnightSchedulesTheFollowingDay() {
        assertEquals(86_400_000L, millisUntilNextDate(ZonedDateTime.parse("2026-11-12T00:00:00+01:00[Europe/Bratislava]")))
    }

    @Test fun handlesShortAndLongDaysAcrossDaylightSavingChanges() {
        assertEquals(23 * 3_600_000L, millisUntilNextDate(ZonedDateTime.parse("2026-03-29T00:00:00+01:00[Europe/Bratislava]")))
        assertEquals(25 * 3_600_000L, millisUntilNextDate(ZonedDateTime.parse("2026-10-25T00:00:00+02:00[Europe/Bratislava]")))
    }

    @Test fun handlesYearRolloverAndSubMillisecondRemainder() {
        assertEquals(1L, millisUntilNextDate(ZonedDateTime.parse("2026-12-31T23:59:59.999999999+01:00[Europe/Bratislava]")))
    }
}

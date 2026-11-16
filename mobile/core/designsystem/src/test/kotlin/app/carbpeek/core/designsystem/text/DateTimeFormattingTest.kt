package app.carbpeek.core.designsystem.text

import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateTimeFormattingTest {

    @Test
    fun `when Time is formatted in UK locale and UTC then short 24 hour time is returned`() {
        assertEquals("18:42", UiArg.Time(INSTANT).format(Locale.UK, UTC))
    }

    @Test
    fun `when Time is formatted in another zone then the hour shifts`() {
        assertEquals("20:42", UiArg.Time(INSTANT).format(Locale.UK, TimeZone.getTimeZone("Europe/Warsaw")))
    }

    @Test
    fun `when Date is formatted in UK locale then medium date is returned`() {
        assertEquals("12 Oct 2026", UiArg.Date(INSTANT).format(Locale.UK, UTC))
    }

    @Test
    fun `when Weekday is formatted in UK locale then the short day name is returned`() {
        assertEquals("Mon", UiArg.Weekday(INSTANT).format(Locale.UK, UTC))
    }

    private companion object {
        val INSTANT = Instant.parse("2026-10-12T18:42:00Z")
        val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    }
}

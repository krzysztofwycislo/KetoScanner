package pl.handsome.club.core.designsystem.text

import java.text.DateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Instant

internal fun UiArg.Time.format(locale: Locale, timeZone: TimeZone): String =
    DateFormat.getTimeInstance(DateFormat.SHORT, locale).formatInstant(value, timeZone)

internal fun UiArg.Date.format(locale: Locale, timeZone: TimeZone): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM, locale).formatInstant(value, timeZone)

private fun DateFormat.formatInstant(instant: Instant, timeZone: TimeZone): String {
    this.timeZone = timeZone
    return format(java.util.Date(instant.toEpochMilliseconds()))
}

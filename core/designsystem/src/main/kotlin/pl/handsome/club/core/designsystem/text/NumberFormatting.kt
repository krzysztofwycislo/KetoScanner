package pl.handsome.club.core.designsystem.text

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

internal fun UiArg.Numeric.format(locale: Locale): String = when (this) {
    is UiArg.Numeric.Grams -> numberFormat(locale, fractionDigits = 1).format(value)
    is UiArg.Numeric.Kcal -> numberFormat(locale, fractionDigits = 0).format(value)
    is UiArg.Numeric.Count -> numberFormat(locale, fractionDigits = 0).format(value)
    is UiArg.Numeric.WholeNumber -> numberFormat(locale, fractionDigits = 0).format(value)
    is UiArg.Numeric.Ratio -> numberFormat(locale, fractionDigits = 1).format(value)
    is UiArg.Numeric.Percent -> NumberFormat.getPercentInstance(locale).withFractionDigits(0).format(fraction)
}

private fun numberFormat(locale: Locale, fractionDigits: Int): NumberFormat =
    NumberFormat.getNumberInstance(locale).withFractionDigits(fractionDigits)

private fun NumberFormat.withFractionDigits(digits: Int): NumberFormat = apply {
    minimumFractionDigits = digits
    maximumFractionDigits = digits
    roundingMode = RoundingMode.HALF_UP
}

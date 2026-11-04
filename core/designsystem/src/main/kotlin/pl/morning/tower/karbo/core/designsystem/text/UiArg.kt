package pl.morning.tower.karbo.core.designsystem.text

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
sealed interface UiArg {

    data class Text(val value: String) : UiArg

    data class Nested(val text: UiText) : UiArg

    data class Time(val value: Instant) : UiArg

    data class Date(val value: Instant) : UiArg

    data class Weekday(val value: Instant) : UiArg

    sealed interface Numeric : UiArg {

        data class Grams(val value: Double) : Numeric

        data class Kcal(val value: Double) : Numeric

        data class Count(val value: Long) : Numeric

        data class WholeNumber(val value: Double) : Numeric

        data class Percent(val fraction: Double) : Numeric

        data class Ratio(val value: Double) : Numeric
    }
}

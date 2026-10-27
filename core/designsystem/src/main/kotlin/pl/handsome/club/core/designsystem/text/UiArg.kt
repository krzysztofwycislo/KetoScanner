package pl.handsome.club.core.designsystem.text

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UiArg {

    data class Text(val value: String) : UiArg

    data class Nested(val text: UiText) : UiArg

    sealed interface Numeric : UiArg {

        data class Grams(val value: Double) : Numeric

        data class Kcal(val value: Double) : Numeric

        data class Count(val value: Long) : Numeric

        data class WholeNumber(val value: Double) : Numeric

        data class Percent(val fraction: Double) : Numeric
    }
}

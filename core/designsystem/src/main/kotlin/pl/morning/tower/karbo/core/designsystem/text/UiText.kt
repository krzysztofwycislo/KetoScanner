package pl.morning.tower.karbo.core.designsystem.text

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
sealed interface UiText {

    data class Res(@StringRes val id: Int, val args: ImmutableList<UiArg>) : UiText {
        constructor(@StringRes id: Int, vararg args: UiArg) : this(id, persistentListOf(*args))
    }

    data class Plural(@PluralsRes val id: Int, val count: Int, val args: ImmutableList<UiArg>) : UiText {
        constructor(@PluralsRes id: Int, count: Int, vararg args: UiArg) : this(id, count, persistentListOf(*args))
    }

    data class Raw(val value: String) : UiText
}

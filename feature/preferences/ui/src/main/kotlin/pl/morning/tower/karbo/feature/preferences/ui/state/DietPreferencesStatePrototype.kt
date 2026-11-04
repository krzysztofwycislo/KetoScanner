package pl.handsome.club.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences

@Immutable
internal data class DietPreferencesStatePrototype(
    val savedPreferences: DietPreferences? = null,
    val option: DietProfileOption? = null,
    val input: String? = null,
    val customMode: DietMode? = null,
    val isInputInvalid: Boolean = false,
    val isSaving: Boolean = false,
    val loadError: AppError? = null,
) {

    fun customAmount(): Int? =
        input?.trim()?.toIntOrNull()?.takeIf { it in DietPreferences.MAX_DAILY_CARBS_RANGE }

    fun chosenPreferences(): DietPreferences? = when (val option = option) {
        null -> null
        DietProfileOption.CUSTOM -> customAmount()?.let { DietPreferences(it, customMode ?: DietMode.KETO) }
        else -> option.preset?.preferences
    }

    companion object {
        val Default = DietPreferencesStatePrototype()
    }
}

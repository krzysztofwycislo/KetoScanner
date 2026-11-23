package app.carbpeek.feature.preferences.ui.component

import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.ui.R
import app.carbpeek.feature.preferences.ui.state.CustomProfileState
import app.carbpeek.feature.preferences.ui.state.DietModeOptionState
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import app.carbpeek.feature.preferences.ui.state.DietProfileOptionState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

private val previewTitles = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto,
    DietProfileOption.KETO to R.string.preferences_preset_keto,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb,
    DietProfileOption.CUSTOM to R.string.preferences_preset_custom,
)

private val previewNotes = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto_note,
    DietProfileOption.KETO to R.string.preferences_preset_keto_note,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb_note,
    DietProfileOption.CUSTOM to R.string.preferences_preset_custom_note,
)

private fun DietProfileOptionState.Companion.example(option: DietProfileOption, selected: Boolean, grams: Int?) =
    DietProfileOptionState(
        option = option,
        title = UiText.Res(previewTitles.getValue(option)),
        note = UiText.Res(previewNotes.getValue(option)),
        trailing = grams?.let { UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(it.toLong())) },
        isSelected = selected,
    )

internal fun previewOptions(selected: DietProfileOption, customGrams: Int?) = DietProfileOption.entries.map {
    DietProfileOptionState.example(
        option = it,
        selected = it == selected,
        grams = it.preset?.preferences?.maxDailyCarbsAmount ?: customGrams,
    )
}.toPersistentList()

private fun DietModeOptionState.Companion.example(mode: DietMode, selected: Boolean) = DietModeOptionState(
    mode = mode,
    label = UiText.Res(
        when (mode) {
            DietMode.KETO -> R.string.preferences_diet_mode_keto
            DietMode.LOW_CARB -> R.string.preferences_diet_mode_low_carb
        },
    ),
    isSelected = selected,
)

internal fun previewCustom(input: String, inputError: UiText?) = CustomProfileState(
    modes = persistentListOf(
        DietModeOptionState.example(DietMode.KETO, selected = true),
        DietModeOptionState.example(DietMode.LOW_CARB, selected = false),
    ),
    input = input,
    inputError = inputError,
)

internal val previewInputError = UiText.Res(
    R.string.preferences_input_error,
    UiArg.Numeric.Count(1),
    UiArg.Numeric.Count(999),
)

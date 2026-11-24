package app.carbpeek.feature.preferences.ui.component

import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.preferences.ui.R
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import app.carbpeek.feature.preferences.ui.state.DietProfileOptionState
import kotlinx.collections.immutable.toPersistentList

private val previewTitles = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto,
    DietProfileOption.KETO to R.string.preferences_preset_keto,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb,
    DietProfileOption.NO_DIET to R.string.preferences_no_diet,
)

private val previewNotes = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto_note,
    DietProfileOption.KETO to R.string.preferences_preset_keto_note,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb_note,
    DietProfileOption.NO_DIET to R.string.preferences_no_diet_note,
)

private fun DietProfileOptionState.Companion.example(option: DietProfileOption, selected: Boolean, grams: Int?) =
    DietProfileOptionState(
        option = option,
        title = UiText.Res(previewTitles.getValue(option)),
        note = UiText.Res(previewNotes.getValue(option)),
        trailing = grams?.let { UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(it.toLong())) },
        isSelected = selected,
    )

internal fun previewOptions(selected: DietProfileOption) = DietProfileOption.entries.map { option ->
    DietProfileOptionState.example(
        option = option,
        selected = option == selected,
        grams = if (option == DietProfileOption.NO_DIET) null else option.preferences.maxDailyCarbsAmount,
    )
}.toPersistentList()

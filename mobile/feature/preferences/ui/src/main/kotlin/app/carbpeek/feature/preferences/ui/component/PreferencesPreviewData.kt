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
)

private val previewNotes = mapOf(
    DietProfileOption.STRICT_KETO to R.string.preferences_preset_strict_keto_note,
    DietProfileOption.KETO to R.string.preferences_preset_keto_note,
    DietProfileOption.LOW_CARB to R.string.preferences_preset_low_carb_note,
)

private fun DietProfileOptionState.Companion.example(option: DietProfileOption, selected: Boolean, grams: Int) =
    DietProfileOptionState(
        option = option,
        title = UiText.Res(previewTitles.getValue(option)),
        note = UiText.Res(previewNotes.getValue(option)),
        trailing = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(grams.toLong())),
        isSelected = selected,
    )

internal fun previewOptions(selected: DietProfileOption) = DietProfileOption.entries.map {
    DietProfileOptionState.example(
        option = it,
        selected = it == selected,
        grams = it.preset.preferences.maxDailyCarbsAmount,
    )
}.toPersistentList()

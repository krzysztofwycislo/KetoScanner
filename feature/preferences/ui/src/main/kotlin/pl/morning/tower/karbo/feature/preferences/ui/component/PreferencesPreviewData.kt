package pl.morning.tower.karbo.feature.preferences.ui.component

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.ui.R
import pl.morning.tower.karbo.feature.preferences.ui.state.CustomProfileState
import pl.morning.tower.karbo.feature.preferences.ui.state.DietModeOptionState
import pl.morning.tower.karbo.feature.preferences.ui.state.DietProfileOption
import pl.morning.tower.karbo.feature.preferences.ui.state.DietProfileOptionState
import pl.morning.tower.karbo.feature.preferences.ui.state.RatingGuideRowState
import pl.morning.tower.karbo.feature.preferences.ui.state.RatingGuideState

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

private fun RatingGuideRowState.Companion.example(rating: Rating, label: Int, value: Int, grams: Double) =
    RatingGuideRowState(
        rating = rating,
        label = UiText.Res(label),
        value = UiText.Res(value, UiArg.Numeric.Grams(grams)),
    )

internal fun previewRatingGuide(dailyCarbs: Int) = RatingGuideState(
    subtitle = UiText.Res(R.string.preferences_rating_subtitle, UiArg.Numeric.Count(dailyCarbs.toLong())),
    rows = persistentListOf(
        RatingGuideRowState.example(
            Rating.Positive,
            R.string.preferences_rating_good_keto,
            R.string.preferences_rating_up_to,
            dailyCarbs / 3.0,
        ),
        RatingGuideRowState.example(
            Rating.Neutral,
            R.string.preferences_rating_neutral,
            R.string.preferences_rating_up_to,
            dailyCarbs / 2.0,
        ),
        RatingGuideRowState.example(
            Rating.Negative,
            R.string.preferences_rating_not_advised_keto,
            R.string.preferences_rating_over,
            dailyCarbs / 2.0,
        ),
    ),
    footnote = UiText.Res(R.string.preferences_rating_footnote_keto),
)

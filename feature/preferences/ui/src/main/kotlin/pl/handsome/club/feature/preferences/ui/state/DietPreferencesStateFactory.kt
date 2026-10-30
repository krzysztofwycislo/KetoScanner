package pl.handsome.club.feature.preferences.ui.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.feature.analysis.api.DietRatingLimits
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.ui.DietPreferencesResources
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Error
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Form
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

internal class DietPreferencesStateFactory(
    private val resources: DietPreferencesResources,
    private val ratingLimits: DietRatingLimits,
) {

    fun createFor(prototype: Prototype): DietPreferencesState = DietPreferencesState(
        content = prototype.content(),
    )

    private fun Prototype.content(): DietPreferencesContentState {
        val loadError = loadError
        val option = option
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            savedPreferences == null || option == null -> InitialLoad
            else -> Form(
                options = options(option),
                custom = if (option == DietProfileOption.CUSTOM) custom() else null,
                ratingGuide = chosenPreferences()?.let(::ratingGuide),
                isSaving = isSaving,
            )
        }
    }

    private fun Prototype.options(selected: DietProfileOption): PersistentList<DietProfileOptionState> =
        DietProfileOption.entries.map { option ->
            val preset = option.preset
            DietProfileOptionState(
                option = option,
                title = resources.optionTitle(option),
                note = resources.optionNote(option),
                trailing = if (preset != null) {
                    resources.grams(preset.preferences.maxDailyCarbsAmount)
                } else {
                    customAmount()?.let(resources::grams)
                },
                isSelected = option == selected,
            )
        }.toPersistentList()

    private fun Prototype.custom(): CustomProfileState = CustomProfileState(
        modes = DietMode.entries.map { mode ->
            DietModeOptionState(
                mode = mode,
                label = resources.dietModeLabel(mode),
                isSelected = mode == customMode,
            )
        }.toPersistentList(),
        input = input.orEmpty(),
        inputError = if (isInputInvalid) resources.inputError() else null,
    )

    private fun ratingGuide(chosen: DietPreferences): RatingGuideState {
        val limits = ratingLimits.carbsPer100g(chosen)
        val mode = chosen.dietMode
        return RatingGuideState(
            subtitle = resources.ratingSubtitle(chosen.maxDailyCarbsAmount),
            rows = persistentListOf(
                RatingGuideRowState(
                    rating = Rating.Positive,
                    label = resources.ratingLabel(DietRate.GOOD, mode),
                    value = resources.upTo(limits.goodMax),
                ),
                RatingGuideRowState(
                    rating = Rating.Neutral,
                    label = resources.ratingLabel(DietRate.NEUTRAL, mode),
                    value = resources.upTo(limits.neutralMax),
                ),
                RatingGuideRowState(
                    rating = Rating.Negative,
                    label = resources.ratingLabel(DietRate.NOT_ADVISED, mode),
                    value = resources.over(limits.neutralMax),
                ),
            ),
            footnote = resources.ratingFootnote(mode),
        )
    }
}

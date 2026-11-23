package app.carbpeek.feature.preferences.ui.state

import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.ui.DietPreferencesResources
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.Error
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.Form
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

internal class DietPreferencesStateFactory(
    private val resources: DietPreferencesResources,
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
                isSaveEnabled = !isSaving,
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
}

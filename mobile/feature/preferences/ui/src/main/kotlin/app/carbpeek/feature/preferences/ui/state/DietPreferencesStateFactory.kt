package app.carbpeek.feature.preferences.ui.state

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
                isSaveEnabled = !isSaving,
            )
        }
    }

    private fun options(selected: DietProfileOption): PersistentList<DietProfileOptionState> =
        DietProfileOption.entries.map { option ->
            DietProfileOptionState(
                option = option,
                title = resources.optionTitle(option),
                note = resources.optionNote(option),
                trailing = resources.grams(option.preset.preferences.maxDailyCarbsAmount),
                isSelected = option == selected,
            )
        }.toPersistentList()
}

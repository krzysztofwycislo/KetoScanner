package pl.handsome.club.feature.preferences.ui.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.ui.DietPreferencesResources
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Error
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.Form
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState.InitialLoad
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

internal class DietPreferencesStateFactory(private val resources: DietPreferencesResources) {

    fun createFor(prototype: Prototype): DietPreferencesState = DietPreferencesState(
        content = prototype.content(),
    )

    private fun Prototype.content(): DietPreferencesContentState {
        val savedAmount = savedAmount
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            savedAmount == null -> InitialLoad
            else -> Form(
                dietModes = dietModes(savedMode),
                summary = resources.summary(savedAmount),
                input = input.orEmpty(),
                inputError = if (isInputInvalid) resources.inputError() else null,
                isSaving = isSaving,
            )
        }
    }

    private fun dietModes(selected: DietMode?): PersistentList<DietModeOptionState> =
        DietMode.entries.map { mode ->
            DietModeOptionState(
                mode = mode,
                label = resources.dietModeLabel(mode),
                description = resources.dietModeDescription(mode),
                isSelected = mode == selected,
            )
        }.toPersistentList()
}

package pl.handsome.club.feature.preferences.ui.state

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
                summary = resources.summary(savedAmount),
                input = input.orEmpty(),
                inputError = if (isInputInvalid) resources.inputError() else null,
            )
        }
    }
}

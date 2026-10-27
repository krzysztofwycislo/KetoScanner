package pl.handsome.club.core.designsystem.event

import pl.handsome.club.core.designsystem.text.UiText

sealed interface AppEvent {
    data object HideKeyboard : AppEvent
    data class ShowMessage(val text: UiText) : AppEvent
}

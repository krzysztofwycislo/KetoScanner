package app.carbpeek.core.designsystem.event

import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.text.UiText

sealed interface AppEvent {
    data object HideKeyboard : AppEvent
    data class ShowMessage(val text: UiText, val tone: MessageTone = MessageTone.Info) : AppEvent
}

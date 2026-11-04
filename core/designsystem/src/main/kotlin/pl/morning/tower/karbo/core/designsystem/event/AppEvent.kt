package pl.morning.tower.karbo.core.designsystem.event

import pl.morning.tower.karbo.core.designsystem.component.MessageTone
import pl.morning.tower.karbo.core.designsystem.text.UiText

sealed interface AppEvent {
    data object HideKeyboard : AppEvent
    data class ShowMessage(val text: UiText, val tone: MessageTone = MessageTone.Info) : AppEvent
}

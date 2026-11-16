package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.core.designsystem.text.UiText

@Immutable
internal data class SendButtonState(
    val label: UiText,
    val isEnabled: Boolean,
    val isLoading: Boolean,
) {
    companion object
}

package app.carbpeek.feature.report.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.text.UiText

@Immutable
internal data class SendButtonState(
    val label: UiText,
    val isEnabled: Boolean,
    val isLoading: Boolean,
) {
    companion object
}

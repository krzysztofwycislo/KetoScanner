package pl.handsome.club.feature.history.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiText

@Immutable
internal data class HistoryState(
    val subtitle: UiText?,
    val content: HistoryContentState,
)

@Immutable
internal sealed interface HistoryContentState {
    data object InitialLoad : HistoryContentState
    data object Empty : HistoryContentState
    data class ShowItems(val items: PersistentList<HistoryItemState>) : HistoryContentState
    data class Error(val message: UiText) : HistoryContentState
}

@Immutable
internal data class HistoryItemState(
    val id: Long,
    val barcode: String,
    val name: UiText,
    val brand: UiText,
    val rating: Rating,
    val ratingDescription: UiText,
) {
    companion object
}

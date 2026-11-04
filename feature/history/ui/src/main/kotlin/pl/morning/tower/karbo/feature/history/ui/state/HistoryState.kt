package pl.morning.tower.karbo.feature.history.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText

@Immutable
internal data class HistoryState(
    val query: String,
    val showControls: Boolean,
    val filters: PersistentList<HistoryFilterState>,
    val content: HistoryContentState,
)

@Immutable
internal data class HistoryFilterState(
    val filter: HistoryFilter,
    val label: UiText,
    val selected: Boolean,
)

@Immutable
internal sealed interface HistoryContentState {
    data object InitialLoad : HistoryContentState
    data object Empty : HistoryContentState
    data object NoResults : HistoryContentState
    data class ShowGroups(val groups: PersistentList<HistoryGroupState>) : HistoryContentState
    data class Error(val message: UiText) : HistoryContentState
}

@Immutable
internal data class HistoryGroupState(
    val key: String,
    val title: UiText,
    val items: PersistentList<HistoryItemState>,
)

@Immutable
internal data class HistoryItemState(
    val id: Long,
    val barcode: String,
    val name: UiText,
    val icon: AppIcon,
    val time: UiText,
    val rating: Rating,
    val carbs: UiText,
    val verdictDescription: UiText,
) {
    companion object
}

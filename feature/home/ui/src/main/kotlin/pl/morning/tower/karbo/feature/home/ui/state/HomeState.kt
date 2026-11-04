package pl.morning.tower.karbo.feature.home.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText

@Immutable
internal data class HomeState(
    val profileLabel: UiText,
    val recent: HomeRecentState,
    val showSeeAll: Boolean,
)

@Immutable
internal sealed interface HomeRecentState {
    data object InitialLoad : HomeRecentState
    data object Empty : HomeRecentState
    data class ShowItems(val items: PersistentList<HomeItemState>) : HomeRecentState
    data class Error(val message: UiText) : HomeRecentState
}

@Immutable
internal data class HomeItemState(
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

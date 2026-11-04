package pl.morning.tower.karbo.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.model.ProductCategory

@Immutable
internal data class FavouritesState(
    val query: String,
    val showSearch: Boolean,
    val content: FavouritesContentState,
)

@Immutable
internal sealed interface FavouritesContentState {
    data object InitialLoad : FavouritesContentState
    data object Empty : FavouritesContentState
    data class NoResults(val message: UiText) : FavouritesContentState
    data class ShowGroups(val groups: PersistentList<FavouriteGroupState>) : FavouritesContentState
    data class Error(val message: UiText) : FavouritesContentState
}

@Immutable
internal data class FavouriteGroupState(
    val category: ProductCategory,
    val icon: AppIcon,
    val label: UiText,
    val count: UiText,
    val expanded: Boolean,
    val canToggle: Boolean,
    val toggleState: UiText,
    val items: PersistentList<FavouriteItemState>,
)

@Immutable
internal data class FavouriteItemState(
    val barcode: String,
    val name: UiText,
    val icon: AppIcon,
    val brand: UiText?,
    val rating: Rating,
    val carbs: UiText,
    val verdictDescription: UiText,
) {
    companion object
}

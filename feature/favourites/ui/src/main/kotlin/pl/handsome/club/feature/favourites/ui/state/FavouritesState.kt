package pl.handsome.club.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiText

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
    data class ShowItems(val items: PersistentList<FavouriteItemState>) : FavouritesContentState
    data class Error(val message: UiText) : FavouritesContentState
}

@Immutable
internal data class FavouriteItemState(
    val barcode: String,
    val name: UiText,
    val brand: UiText?,
    val rating: Rating,
    val carbs: UiText,
    val verdictDescription: UiText,
) {
    companion object
}

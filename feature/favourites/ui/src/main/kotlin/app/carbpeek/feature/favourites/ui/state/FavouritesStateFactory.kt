package app.carbpeek.feature.favourites.ui.state

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.favourites.ui.FavouritesResources
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.Empty
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.Error
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.NoResults
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState.ShowGroups
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

internal class FavouritesStateFactory(private val resources: FavouritesResources) {

    fun createFor(prototype: Prototype): FavouritesState {
        val mode = (prototype.dietPreferences ?: DietPreferences.Default).dietMode
        val content = prototype.content(mode)
        return FavouritesState(
            query = prototype.query,
            showSearch = content is ShowGroups || content is NoResults,
            content = content,
        )
    }

    private fun Prototype.content(mode: DietMode): FavouritesContentState {
        val favourites = favourites
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            favourites == null -> InitialLoad
            favourites.isEmpty() && favouritesQuery.isBlank() -> Empty
            favourites.isEmpty() -> NoResults(resources.noResults(favouritesQuery.trim()))
            else -> ShowGroups(groups(favourites, mode))
        }
    }

    private fun Prototype.groups(
        favourites: List<FavouriteProduct>,
        mode: DietMode,
    ): PersistentList<FavouriteGroupState> {
        val byCategory = favourites.groupBy { it.category }
        val canToggle = favouritesQuery.isBlank()
        return ProductCategory.entries
            .mapNotNull { category ->
                byCategory[category]?.let { products ->
                    val expanded = !canToggle || category !in collapsed
                    FavouriteGroupState(
                        category = category,
                        icon = category.icon,
                        label = category.label,
                        count = resources.groupCount(products.size),
                        expanded = expanded,
                        canToggle = canToggle,
                        toggleState = resources.groupToggleState(expanded),
                        items = products.map { it.toItemState(mode) }.toPersistentList(),
                    )
                }
            }
            .toPersistentList()
    }

    private fun FavouriteProduct.toItemState(mode: DietMode): FavouriteItemState = FavouriteItemState(
        barcode = productBarcode,
        name = resources.productName(productName),
        icon = category.icon,
        brand = resources.brand(productBrand),
        rating = dietRate.toRating(),
        carbs = resources.carbs(carbsPer100g),
        verdictDescription = resources.verdictDescription(dietRate, mode, carbsPer100g),
    )

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }
}

package pl.morning.tower.karbo.feature.favourites.ui.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.icon
import pl.morning.tower.karbo.core.designsystem.text.label
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.favourites.ui.FavouritesResources
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState.Empty
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState.Error
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState.NoResults
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState.ShowGroups
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

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

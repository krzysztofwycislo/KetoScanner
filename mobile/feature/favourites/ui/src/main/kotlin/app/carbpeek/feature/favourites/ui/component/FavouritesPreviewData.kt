package app.carbpeek.feature.favourites.ui.component

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.favourites.ui.FavouritesResources
import app.carbpeek.feature.favourites.ui.R
import app.carbpeek.feature.favourites.ui.state.FavouriteGroupState
import app.carbpeek.feature.favourites.ui.state.FavouriteItemState
import app.carbpeek.feature.preferences.api.model.DietMode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

private val previewResources = FavouritesResources()

private fun FavouriteItemState.Companion.example(
    barcode: String,
    name: String,
    icon: AppIcon,
    brand: String?,
    rating: Rating?,
    carbs: Double?,
) = FavouriteItemState(
    barcode = barcode,
    name = UiText.Raw(name),
    icon = icon,
    brand = brand?.let(UiText::Raw),
    rating = rating,
    carbs = previewResources.carbs(grams = carbs, mode = DietMode.KETO.takeIf { rating != null }),
    verdictDescription = UiText.Res(
        if (rating == null) R.string.favourites_verdict_unknown else R.string.favourites_verdict_good_keto,
    ),
)

private fun previewGroup(
    category: ProductCategory,
    expanded: Boolean,
    canToggle: Boolean,
    items: PersistentList<FavouriteItemState>,
) = FavouriteGroupState(
    category = category,
    icon = category.icon,
    label = category.label,
    count = previewResources.groupCount(items.size),
    expanded = expanded,
    canToggle = canToggle,
    toggleState = previewResources.groupToggleState(expanded),
    items = items,
)

private val previewSpreads = persistentListOf(
    FavouriteItemState.example("5900000000001", "Peanut butter", AppIcons.Jar, "Nuts & Co", Rating.Positive, 14.0),
    FavouriteItemState.example("5900000000005", "Hazelnut spread", AppIcons.Jar, null, Rating.Negative, 57.5),
)

private val previewBiscuits = persistentListOf(
    FavouriteItemState.example("5900000000002", "Oat cookies", AppIcons.Cookie, null, Rating.Neutral, 52.3),
)

private val previewDrinks = persistentListOf(
    FavouriteItemState.example("5900000000003", "Cola", AppIcons.Can, "Fizz", Rating.Negative, 10.6),
)

private val previewOther = persistentListOf(
    FavouriteItemState.example("5900000000004", "Mystery bar", AppIcons.Bag, null, Rating.Unknown, null),
)

internal val previewGroups = persistentListOf(
    previewGroup(ProductCategory.SPREADS, expanded = true, canToggle = true, items = previewSpreads),
    previewGroup(ProductCategory.DRINKS, expanded = false, canToggle = true, items = previewDrinks),
    previewGroup(ProductCategory.BISCUITS, expanded = true, canToggle = true, items = previewBiscuits),
    previewGroup(ProductCategory.OTHER, expanded = true, canToggle = true, items = previewOther),
)

internal val previewNoDietGroups = persistentListOf(
    previewGroup(
        category = ProductCategory.SPREADS,
        expanded = true,
        canToggle = true,
        items = persistentListOf(
            FavouriteItemState.example(
                barcode = "5900000000001",
                name = "Peanut butter",
                icon = AppIcons.Jar,
                brand = "Nuts & Co",
                rating = null,
                carbs = 14.0,
            ),
            FavouriteItemState.example(
                barcode = "5900000000005",
                name = "Hazelnut spread",
                icon = AppIcons.Jar,
                brand = null,
                rating = null,
                carbs = 57.5,
            ),
        ),
    ),
    previewGroup(
        category = ProductCategory.OTHER,
        expanded = true,
        canToggle = true,
        items = persistentListOf(
            FavouriteItemState.example(
                barcode = "5900000000004",
                name = "Mystery bar",
                icon = AppIcons.Bag,
                brand = null,
                rating = null,
                carbs = null,
            ),
        ),
    ),
)

internal val previewSearchGroups = persistentListOf(
    previewGroup(ProductCategory.SPREADS, expanded = true, canToggle = false, items = previewSpreads),
)

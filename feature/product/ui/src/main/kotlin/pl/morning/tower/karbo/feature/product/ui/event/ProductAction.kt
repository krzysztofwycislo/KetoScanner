package pl.morning.tower.karbo.feature.product.ui.event

internal sealed interface ProductAction {
    data object OnRetryClicked : ProductAction
    data object OnBackClicked : ProductAction
    data object OnFavouriteClicked : ProductAction
    data object OnImageClicked : ProductAction
    data object OnImagePreviewDismissed : ProductAction
    data object OnNameClicked : ProductAction
    data object OnPer100gClicked : ProductAction
    data object OnPerServingClicked : ProductAction
    data object OnOpenFoodFactsClicked : ProductAction
    data object OnWebPageUnavailable : ProductAction
}

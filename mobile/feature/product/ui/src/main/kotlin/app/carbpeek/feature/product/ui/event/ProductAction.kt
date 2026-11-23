package app.carbpeek.feature.product.ui.event

internal sealed interface ProductAction {
    data object OnRetryClicked : ProductAction
    data object OnBackClicked : ProductAction
    data object OnFavouriteClicked : ProductAction
    data object OnImageClicked : ProductAction
    data object OnImagePreviewDismissed : ProductAction
    data object OnNameClicked : ProductAction
    data object OnPer100gClicked : ProductAction
    data object OnPerServingClicked : ProductAction
    data object OnAddPhotosClicked : ProductAction
    data object OnReportClicked : ProductAction
    data class OnIngredientClicked(val index: Int) : ProductAction
    data class OnIngredientContentClicked(val index: Int) : ProductAction
    data object OnIngredientBackClicked : ProductAction
    data object OnIngredientSheetDismissed : ProductAction
    data object OnIngredientsInfoClicked : ProductAction
    data object OnIngredientsInfoDismissed : ProductAction
}

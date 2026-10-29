package pl.handsome.club.feature.product.ui.event

internal sealed interface ProductAction {
    data object OnRetryClicked : ProductAction
    data object OnBackClicked : ProductAction
    data object OnFavouriteClicked : ProductAction
    data object OnImageClicked : ProductAction
    data object OnImagePreviewDismissed : ProductAction
    data object OnNameClicked : ProductAction
}

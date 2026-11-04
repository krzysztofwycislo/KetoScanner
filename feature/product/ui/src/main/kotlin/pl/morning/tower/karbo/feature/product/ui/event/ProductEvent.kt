package pl.handsome.club.feature.product.ui.event

sealed interface ProductEvent {

    sealed interface ProductNavigation : ProductEvent {
        data object NavigateBack : ProductNavigation
    }

    data class OpenWebPage(val url: String) : ProductEvent
}

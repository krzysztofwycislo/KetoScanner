package pl.morning.tower.karbo.feature.product.ui.event

sealed interface ProductEvent {

    sealed interface ProductNavigation : ProductEvent {
        data object NavigateBack : ProductNavigation
        data class NavigateToReport(val barcode: String) : ProductNavigation
    }

    data class OpenWebPage(val url: String) : ProductEvent
}

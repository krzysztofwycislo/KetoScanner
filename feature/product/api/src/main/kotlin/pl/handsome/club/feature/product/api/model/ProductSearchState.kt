package pl.handsome.club.feature.product.api.model


sealed class ProductSearchState {

    object NotFound : ProductSearchState()
    data class Success(val product: Product) : ProductSearchState()
    data class Error(val throwable: Throwable) : ProductSearchState()

}

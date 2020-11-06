package pl.handsome.club.domain.product


sealed class ProductSearchState {

    object NotFound : ProductSearchState()
    data class Success(val product: Product) : ProductSearchState()
    data class Error(val throwable: Throwable) : ProductSearchState()

}

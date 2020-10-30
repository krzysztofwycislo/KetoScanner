package pl.handsome.club.domain.product


sealed class SearchProduct {

    object InProgress : SearchProduct()
    object NotFound : SearchProduct()
    data class Success(val product: Product) : SearchProduct()
    data class Error(val throwable: Throwable) : SearchProduct()

}

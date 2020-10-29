package pl.handsome.club.ketoscanner.viewmodel.product

import pl.handsome.club.domain.data.Product


sealed class SearchState {

    object SearchingInProgress : SearchState()
    data class SearchingSuccess(val product: Product) : SearchState()
    data class SearchingError(val throwable: Throwable) : SearchState()

}

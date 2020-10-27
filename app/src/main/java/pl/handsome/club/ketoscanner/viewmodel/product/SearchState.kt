package pl.handsome.club.ketoscanner.viewmodel.product

import pl.handsome.club.domain.data.Product


sealed class SearchState {

    object SearchingInProgress : SearchState()
    class SearchingSuccess(val product: Product) : SearchState()
    class SearchingError(val throwable: Throwable) : SearchState()

}

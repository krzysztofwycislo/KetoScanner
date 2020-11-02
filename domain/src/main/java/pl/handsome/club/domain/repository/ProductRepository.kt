package pl.handsome.club.domain.repository

import pl.handsome.club.domain.product.ProductSearchState


interface ProductRepository {

    suspend fun searchProductByBarcode(barcode: String) : ProductSearchState

}

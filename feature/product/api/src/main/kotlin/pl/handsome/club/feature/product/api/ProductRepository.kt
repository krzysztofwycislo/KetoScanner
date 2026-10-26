package pl.handsome.club.feature.product.api

import pl.handsome.club.feature.product.api.model.ProductSearchState


interface ProductRepository {

    suspend fun searchProductByBarcode(barcode: String) : ProductSearchState

}

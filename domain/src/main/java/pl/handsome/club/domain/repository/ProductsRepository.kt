package pl.handsome.club.domain.repository

import pl.handsome.club.domain.product.SearchProduct


interface ProductsRepository {

    suspend fun searchProductByBarcode(barcode: String) : SearchProduct

}

package pl.handsome.club.domain.repository

import pl.handsome.club.domain.data.Product


interface ProductsRepository {

    suspend fun searchProductByBarcode(barcode: String) : Product

}

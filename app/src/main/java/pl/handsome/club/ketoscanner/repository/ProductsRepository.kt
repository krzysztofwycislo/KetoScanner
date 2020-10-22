package pl.handsome.club.ketoscanner.repository

import pl.handsome.club.domain.data.Product


interface ProductsRepository {

    suspend fun searchProductByBarcode(barcode: String) : Product

}

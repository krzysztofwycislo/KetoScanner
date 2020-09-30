package pl.handsome.club.ketoscanner.repository

import pl.handsome.club.ketoscanner.domain.Product


interface ProductsRepository {

    suspend fun searchProductByName(name: String) : Product

    suspend fun searchProductByBarcode(barcode: String) : Product

}

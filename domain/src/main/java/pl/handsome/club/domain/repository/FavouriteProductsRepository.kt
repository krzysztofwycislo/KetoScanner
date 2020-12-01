package pl.handsome.club.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.product.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product)

    suspend fun search(name: String = ""): Flow<List<FavouriteProduct>>

    suspend fun findByBarcode(barcode: String): FavouriteProduct?

    suspend fun removeFromFavourites(product: Product)

}

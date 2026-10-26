package pl.handsome.club.feature.favourites.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product)

    suspend fun search(name: String = ""): Flow<List<FavouriteProduct>>

    suspend fun findByBarcode(barcode: String): FavouriteProduct?

    suspend fun removeFromFavourites(product: Product)

}

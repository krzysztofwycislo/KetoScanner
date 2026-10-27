package pl.handsome.club.feature.favourites.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product): Outcome<Unit>

    suspend fun search(name: String = ""): Flow<List<FavouriteProduct>>

    suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?>

    suspend fun removeFromFavourites(product: Product): Outcome<Unit>

}

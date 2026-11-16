package app.carbpeek.feature.favourites.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.flow.Flow


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product): Outcome<Unit>

    fun observeFavourites(nameQuery: String): Flow<List<FavouriteProduct>>

    suspend fun isFavourite(barcode: Barcode): Outcome<Boolean>

    suspend fun removeFromFavourites(product: Product): Outcome<Unit>

}

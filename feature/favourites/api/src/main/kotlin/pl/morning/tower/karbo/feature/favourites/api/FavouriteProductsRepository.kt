package pl.morning.tower.karbo.feature.favourites.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(product: Product): Outcome<Unit>

    fun observeFavourites(nameQuery: String): Flow<List<FavouriteProduct>>

    suspend fun isFavourite(barcode: Barcode): Outcome<Boolean>

    suspend fun removeFromFavourites(product: Product): Outcome<Unit>

}

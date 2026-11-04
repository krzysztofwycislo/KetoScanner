package pl.morning.tower.karbo.feature.favourites.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product


interface FavouriteProductsRepository {

    suspend fun addToFavourites(analysis: ProductAnalysis): Outcome<Unit>

    fun observeFavourites(nameQuery: String): Flow<Outcome<List<FavouriteProduct>>>

    suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?>

    suspend fun removeFromFavourites(product: Product): Outcome<Unit>

    suspend fun updateRating(analysis: ProductAnalysis): Outcome<Unit>

}

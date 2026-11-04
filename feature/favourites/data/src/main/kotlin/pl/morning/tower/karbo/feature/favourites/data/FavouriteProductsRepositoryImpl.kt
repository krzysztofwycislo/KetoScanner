package pl.morning.tower.karbo.feature.favourites.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.carbsPer100g
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.favourites.api.FavouriteProductsRepository
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product
import kotlin.coroutines.cancellation.CancellationException

internal class FavouriteProductsRepositoryImpl(
    private val persistence: FavouritesPersistence,
    private val dispatchers: AppDispatchers,
) : FavouriteProductsRepository {

    override suspend fun addToFavourites(analysis: ProductAnalysis): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.insert(analysis.toFavouriteEntity())
            }
        }

    override fun observeFavourites(nameQuery: String): Flow<Outcome<List<FavouriteProduct>>> =
        persistence.observe(nameQuery)
            .map<_, Outcome<List<FavouriteProduct>>> { Outcome.Success(it.toDomain()) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(Outcome.Failure(AppError.Storage))
            }
            .flowOn(dispatchers.io)

    override suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.findByBarcode(barcode)?.toDomain()
            }
        }

    override suspend fun removeFromFavourites(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.delete(product.toFavouriteEntity())
            }
        }

    override suspend fun updateRating(analysis: ProductAnalysis): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.updateRating(
                    analysis.product.barcode,
                    analysis.dietRate?.name,
                    analysis.carbsPer100g,
                    analysis.product.category.name
                )
            }
        }
}

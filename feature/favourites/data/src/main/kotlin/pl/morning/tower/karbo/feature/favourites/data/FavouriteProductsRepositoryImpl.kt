package pl.morning.tower.karbo.feature.favourites.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.mapFailures
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProducts
import pl.morning.tower.karbo.feature.favourites.api.FavouriteProductsRepository
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product

internal class FavouriteProductsRepositoryImpl(
    private val persistence: FavouritesPersistence,
    private val analyzeProducts: AnalyzeProducts,
    private val dispatchers: AppDispatchers,
) : FavouriteProductsRepository {

    override suspend fun addToFavourites(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.insert(product.toFavouriteEntity())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFavourites(nameQuery: String): Flow<List<FavouriteProduct>> =
        persistence.observe(nameQuery)
            .flatMapLatest { entities ->
                val barcodes = entities.map { Barcode(it.productBarcode) }.toSet()
                analyzeProducts(barcodes).map { entities.toDomain(it) }
            }
            .distinctUntilChanged()
            .mapFailures(mapError = { AppError.Storage })
            .flowOn(dispatchers.io)

    override suspend fun isFavourite(barcode: Barcode): Outcome<Boolean> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.findByBarcode(barcode.value) != null
            }
        }

    override suspend fun removeFromFavourites(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.delete(product.toFavouriteEntity())
            }
        }
}

package app.carbpeek.feature.favourites.data

import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.mapFailures
import app.carbpeek.core.common.outcomeOf
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.favourites.api.FavouriteProductsRepository
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

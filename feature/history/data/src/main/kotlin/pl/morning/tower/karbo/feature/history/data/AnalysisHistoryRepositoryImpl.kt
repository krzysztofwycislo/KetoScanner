package pl.morning.tower.karbo.feature.history.data

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
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.product.api.model.Product

internal class AnalysisHistoryRepositoryImpl(
    private val persistence: AnalysisHistoryPersistence,
    private val analyzeProducts: AnalyzeProducts,
    private val dispatchers: AppDispatchers,
) : AnalysisHistoryRepository {

    override suspend fun save(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.insert(product.toEntity())
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntry>> =
        persistence.observeLastUniqueEntries(amount)
            .flatMapLatest { entities ->
                val barcodes = entities.map { Barcode(it.productBarcode) }.toSet()
                analyzeProducts(barcodes).map { entities.toDomain(it) }
            }
            .distinctUntilChanged()
            .mapFailures(mapError = { AppError.Storage })
            .flowOn(dispatchers.io)
}

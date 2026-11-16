package app.carbpeek.feature.history.data

import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.mapFailures
import app.carbpeek.core.common.outcomeOf
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.history.api.AnalysisHistoryRepository
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

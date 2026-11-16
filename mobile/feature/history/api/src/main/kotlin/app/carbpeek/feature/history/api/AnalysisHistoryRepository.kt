package app.carbpeek.feature.history.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.flow.Flow


interface AnalysisHistoryRepository {

    suspend fun save(product: Product): Outcome<Unit>

    fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntry>>

}

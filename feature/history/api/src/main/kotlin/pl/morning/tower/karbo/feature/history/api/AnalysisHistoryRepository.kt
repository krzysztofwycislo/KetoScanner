package pl.morning.tower.karbo.feature.history.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.product.api.model.Product


interface AnalysisHistoryRepository {

    suspend fun save(product: Product): Outcome<Unit>

    fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntry>>

}

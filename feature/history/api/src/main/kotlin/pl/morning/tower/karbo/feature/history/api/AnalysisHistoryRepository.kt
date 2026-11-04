package pl.morning.tower.karbo.feature.history.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry


interface AnalysisHistoryRepository {

    suspend fun save(analysis: ProductAnalysis): Outcome<Unit>

    fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>>

}

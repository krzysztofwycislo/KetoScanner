package pl.handsome.club.feature.history.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry


interface AnalysisHistoryRepository {

    suspend fun save(analysis: ProductAnalysis): Outcome<Unit>

    fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>>

}

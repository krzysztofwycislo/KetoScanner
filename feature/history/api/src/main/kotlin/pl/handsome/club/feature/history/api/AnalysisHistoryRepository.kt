package pl.handsome.club.feature.history.api

import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry


interface AnalysisHistoryRepository {

    suspend fun save(result: ProductAnalysisResult): Outcome<Unit>

    suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>>

}
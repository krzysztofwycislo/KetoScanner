package pl.handsome.club.feature.history.api

import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry


interface AnalysisHistoryRepository {

    suspend fun save(result: ProductAnalysisResult)

    suspend fun getLastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntry>

}
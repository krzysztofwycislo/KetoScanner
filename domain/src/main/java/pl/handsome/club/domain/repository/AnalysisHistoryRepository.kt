package pl.handsome.club.domain.repository

import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry


interface AnalysisHistoryRepository {

    suspend fun save(result: ProductAnalysisResult)

    suspend fun getLastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntry>

}
package pl.handsome.club.domain.repository

import pl.handsome.club.domain.analyze.ProductAnalysisResult


interface AnalysisHistoryRepository {

    suspend fun save(result: ProductAnalysisResult)

    fun getLastEntries(amount: Int): List<ProductAnalysisResult>

}
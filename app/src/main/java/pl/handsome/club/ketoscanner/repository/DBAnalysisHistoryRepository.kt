package pl.handsome.club.ketoscanner.repository

import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.repository.AnalysisHistoryRepository


// TODO temporary solution
class DBAnalysisHistoryRepository : AnalysisHistoryRepository {

    private val history = mutableListOf<ProductAnalysisResult>()


    override fun save(result: ProductAnalysisResult) {
        history.add(result)
    }

    override fun getLastEntries(amount: Int): List<ProductAnalysisResult> {
        return history.takeLast(amount)
    }

}
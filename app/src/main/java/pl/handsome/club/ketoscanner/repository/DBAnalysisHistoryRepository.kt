package pl.handsome.club.ketoscanner.repository

import kotlinx.coroutines.Dispatchers
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.repository.AnalysisHistoryRepository


// TODO temporary solution
class DBAnalysisHistoryRepository : AnalysisHistoryRepository {

    private val history = mutableListOf<ProductAnalysisResult>()


    override suspend fun save(result: ProductAnalysisResult) {
        with(Dispatchers.IO) {
            history.add(result)
        }
    }

    override fun getLastEntries(amount: Int): List<ProductAnalysisResult> {
        return history.takeLast(amount)
    }

}
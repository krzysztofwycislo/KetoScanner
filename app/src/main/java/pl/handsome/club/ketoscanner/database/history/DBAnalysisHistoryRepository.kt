package pl.handsome.club.ketoscanner.database.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.ketoscanner.database.AppDatabase
import pl.handsome.club.ketoscanner.database.toDomain
import pl.handsome.club.ketoscanner.database.toHistoryEntry


// TODO removing old entries
class DBAnalysisHistoryRepository(
    appDatabase: AppDatabase
) : AnalysisHistoryRepository {

    private val productAnalysisHistoryDao: ProductAnalysisHistoryDao =
        appDatabase.productAnalysisHistoryDao()


    override suspend fun save(result: ProductAnalysisResult) {
        withContext(Dispatchers.IO) {
            productAnalysisHistoryDao.insert(result.toHistoryEntry())
        }
    }

    override suspend fun getLastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntry> {
        return withContext(Dispatchers.IO) {
            productAnalysisHistoryDao.getLastUniqueEntries(amount).map { it.toDomain() }
        }
    }

}
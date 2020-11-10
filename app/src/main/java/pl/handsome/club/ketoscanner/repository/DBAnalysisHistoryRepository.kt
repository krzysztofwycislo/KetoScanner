package pl.handsome.club.ketoscanner.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.ketoscanner.repository.database.AppDatabase
import pl.handsome.club.ketoscanner.repository.database.ProductAnalysisHistoryDao
import pl.handsome.club.ketoscanner.repository.database.toDomain
import pl.handsome.club.ketoscanner.repository.database.toHistoryEntry


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
            productAnalysisHistoryDao.getLastEntries(amount).map { it.toDomain() }
        }
    }

}
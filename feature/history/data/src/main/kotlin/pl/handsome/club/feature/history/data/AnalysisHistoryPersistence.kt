package pl.handsome.club.feature.history.data

import pl.handsome.club.core.database.history.ProductAnalysisHistoryDao
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity

internal interface AnalysisHistoryPersistence {
    suspend fun insert(entity: ProductAnalysisHistoryEntryEntity)
    suspend fun lastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity>
}

internal class AnalysisHistoryPersistenceImpl(
    private val dao: ProductAnalysisHistoryDao,
) : AnalysisHistoryPersistence {

    override suspend fun insert(entity: ProductAnalysisHistoryEntryEntity) = dao.insert(entity)

    override suspend fun lastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity> =
        dao.getLastUniqueEntries(amount)
}

package pl.handsome.club.feature.history.data

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.database.history.ProductAnalysisHistoryDao
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity

internal interface AnalysisHistoryPersistence {
    suspend fun insert(entity: ProductAnalysisHistoryEntryEntity)
    fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntryEntity>>
}

internal class AnalysisHistoryPersistenceImpl(
    private val dao: ProductAnalysisHistoryDao,
) : AnalysisHistoryPersistence {

    override suspend fun insert(entity: ProductAnalysisHistoryEntryEntity) = dao.insert(entity)

    override fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntryEntity>> =
        dao.observeLastUniqueEntries(amount)
}

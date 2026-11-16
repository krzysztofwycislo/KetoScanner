package app.carbpeek.feature.history.data

import app.carbpeek.core.database.history.ProductAnalysisHistoryDao
import app.carbpeek.core.database.history.ProductAnalysisHistoryEntryEntity
import kotlinx.coroutines.flow.Flow

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

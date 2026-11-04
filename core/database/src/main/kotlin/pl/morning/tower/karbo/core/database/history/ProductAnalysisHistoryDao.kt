package pl.morning.tower.karbo.core.database.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductAnalysisHistoryDao {

    @Query(
        "SELECT * FROM ${ProductAnalysisHistoryEntryEntity.TABLE_NAME} WHERE id IN " +
            "(SELECT MAX(id) FROM ${ProductAnalysisHistoryEntryEntity.TABLE_NAME} GROUP BY productBarcode) " +
            "ORDER BY updateTime DESC LIMIT :amount"
    )
    fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntryEntity>>

    @Insert
    suspend fun insert(entryEntity: ProductAnalysisHistoryEntryEntity)

}
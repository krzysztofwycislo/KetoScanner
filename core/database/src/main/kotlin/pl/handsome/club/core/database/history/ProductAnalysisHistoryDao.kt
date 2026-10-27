package pl.handsome.club.core.database.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ProductAnalysisHistoryDao {

    @Query("SELECT * FROM ${ProductAnalysisHistoryEntryEntity.TABLE_NAME} GROUP BY productBarcode  ORDER BY updateTime DESC LIMIT (:amount)")
    suspend fun getLastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity>

    @Insert
    suspend fun insert(entryEntity: ProductAnalysisHistoryEntryEntity)

}
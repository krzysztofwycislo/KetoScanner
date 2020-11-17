package pl.handsome.club.ketoscanner.database.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ProductAnalysisHistoryDao {

    @Query("SELECT * FROM ${ProductAnalysisHistoryEntryEntity.TABLE_NAME} GROUP BY productBarcode  ORDER BY updateTime DESC LIMIT (:amount)")
    fun getLastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity>

    @Insert
    suspend fun insert(entryEntity: ProductAnalysisHistoryEntryEntity)

}
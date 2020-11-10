package pl.handsome.club.ketoscanner.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface ProductAnalysisHistoryDao {

    @Query("SELECT * FROM ${ProductAnalysisHistoryEntryEntity.TABLE_NAME} GROUP BY productBarcode  ORDER BY updateTime DESC LIMIT (:amount)")
    fun getLastEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity>

    @Insert
    suspend fun insert(entryEntity: ProductAnalysisHistoryEntryEntity)

    @Delete
    suspend fun delete(entryEntity: ProductAnalysisHistoryEntryEntity)

}
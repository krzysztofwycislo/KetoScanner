package pl.handsome.club.core.database.product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface ProductDao {

    @Query("SELECT * FROM ${ProductEntity.TABLE_NAME} WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM ${ProductEntity.TABLE_NAME} WHERE barcode = :barcode LIMIT 1")
    fun observeByBarcode(barcode: String): Flow<ProductEntity?>

    @Upsert
    suspend fun upsert(product: ProductEntity)

    @Query(
        "DELETE FROM ${ProductEntity.TABLE_NAME} WHERE barcode NOT IN " +
            "(SELECT barcode FROM ${ProductEntity.TABLE_NAME} ORDER BY fetchedAt DESC LIMIT :keep)"
    )
    suspend fun deleteAllExceptNewest(keep: Int)

}

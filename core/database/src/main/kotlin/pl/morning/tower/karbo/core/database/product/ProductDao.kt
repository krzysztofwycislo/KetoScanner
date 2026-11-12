package pl.morning.tower.karbo.core.database.product

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

    @Query("SELECT * FROM ${ProductEntity.TABLE_NAME} WHERE barcode IN (:barcodes)")
    fun observeByBarcodes(barcodes: List<String>): Flow<List<ProductEntity>>

    @Upsert
    suspend fun upsert(product: ProductEntity)
}

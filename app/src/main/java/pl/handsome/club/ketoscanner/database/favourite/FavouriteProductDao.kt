package pl.handsome.club.ketoscanner.database.favourite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.handsome.club.domain.product.FavouriteProduct


@Dao
interface FavouriteProductDao {

    @Query("SELECT * FROM ${FavouriteProductEntity.TABLE_NAME} WHERE productName LIKE '%' || :name || '%' ORDER BY updateTime DESC")
    fun searchAndOrderByUpdateTime(name: String): Flow<List<FavouriteProductEntity>>

    @Insert
    suspend fun insert(favouriteProduct: FavouriteProductEntity)

    @Delete
    suspend fun delete(favouriteProduct: FavouriteProductEntity)

    @Query("SELECT * FROM ${FavouriteProductEntity.TABLE_NAME} WHERE productBarcode = :barcode LIMIT 1")
    fun findByBarcode(barcode: String): FavouriteProduct?

}
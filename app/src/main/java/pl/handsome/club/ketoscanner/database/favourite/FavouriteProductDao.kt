package pl.handsome.club.ketoscanner.database.favourite

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pl.handsome.club.domain.product.FavouriteProduct


@Dao
interface FavouriteProductDao {

    @Query("SELECT * FROM ${FavouriteProductEntity.TABLE_NAME} ORDER BY updateTime DESC")
    fun loadFavouriteProductsOrderByUpdateTime(): DataSource.Factory<Int, FavouriteProductEntity>

    @Insert
    suspend fun insert(favouriteProduct: FavouriteProductEntity)

    @Delete
    suspend fun delete(favouriteProduct: FavouriteProductEntity)

    @Query("SELECT * FROM ${FavouriteProductEntity.TABLE_NAME} WHERE productBarcode = :barcode LIMIT 1")
    fun findByBarcode(barcode: String): FavouriteProduct?

}
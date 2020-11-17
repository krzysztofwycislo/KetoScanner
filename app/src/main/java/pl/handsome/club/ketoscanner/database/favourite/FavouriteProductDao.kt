package pl.handsome.club.ketoscanner.database.favourite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert


@Dao
interface FavouriteProductDao {

//    @Query("SELECT * FROM ${FavouriteProductEntity.TABLE_NAME}")
//    fun getLastUniqueEntries(amount: Int): List<FavouriteProductEntity>

    @Insert
    suspend fun insert(favouriteProduct: FavouriteProductEntity)

    @Delete
    suspend fun delete(favouriteProduct: FavouriteProductEntity)

}
package pl.handsome.club.ketoscanner.database.favourite

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.handsome.club.ketoscanner.database.favourite.FavouriteProductEntity.Companion.TABLE_NAME
import java.util.*


@Entity(tableName = TABLE_NAME)
data class FavouriteProductEntity(
    @PrimaryKey val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val imageUrl: String?
) {

    var updateTime: Date = Calendar.getInstance().time

    companion object {
        const val TABLE_NAME = "favourite_product"
    }

}

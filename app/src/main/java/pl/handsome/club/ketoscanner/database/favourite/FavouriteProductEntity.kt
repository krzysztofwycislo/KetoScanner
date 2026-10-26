package pl.handsome.club.ketoscanner.database.favourite

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.handsome.club.ketoscanner.database.favourite.FavouriteProductEntity.Companion.TABLE_NAME
import kotlin.time.Clock
import kotlin.time.Instant


@Entity(tableName = TABLE_NAME)
data class FavouriteProductEntity(
    @PrimaryKey val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val imageUrl: String?
) {

    var updateTime: Instant = Clock.System.now()

    companion object {
        const val TABLE_NAME = "favourite_product"
    }

}

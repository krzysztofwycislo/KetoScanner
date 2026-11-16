package app.carbpeek.core.database.favourite

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.carbpeek.core.database.favourite.FavouriteProductEntity.Companion.TABLE_NAME
import kotlin.time.Clock
import kotlin.time.Instant


@Entity(tableName = TABLE_NAME)
data class FavouriteProductEntity(
    @PrimaryKey val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val imageUrl: String?,
    val category: String?
) {

    var updateTime: Instant = Clock.System.now()

    companion object {
        const val TABLE_NAME = "favourite_product"
    }

}

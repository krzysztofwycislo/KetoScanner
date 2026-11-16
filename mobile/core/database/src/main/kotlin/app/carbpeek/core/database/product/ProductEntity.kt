package app.carbpeek.core.database.product

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.carbpeek.core.database.product.ProductEntity.Companion.TABLE_NAME
import kotlin.time.Instant


@Entity(tableName = TABLE_NAME)
data class ProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val servingAmount: String?,
    val category: String?,
    val energyPerServing: Double?,
    val energyPer100g: Double?,
    val fatsPerServing: Double?,
    val fatsPer100g: Double?,
    val proteinsPerServing: Double?,
    val proteinsPer100g: Double?,
    val carbohydratesPerServing: Double?,
    val carbohydratesPer100g: Double?,
    val saltPerServing: Double?,
    val saltPer100g: Double?,
    val sugarsPerServing: Double?,
    val sugarsPer100g: Double?,
    val fibrePerServing: Double?,
    val fibrePer100g: Double?,
    val fetchedAt: Instant,
) {

    companion object {
        const val TABLE_NAME = "product"
    }

}

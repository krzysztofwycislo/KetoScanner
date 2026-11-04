package pl.handsome.club.core.database.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity.Companion.TABLE_NAME
import kotlin.time.Clock
import kotlin.time.Instant


@Entity(tableName = TABLE_NAME)
data class ProductAnalysisHistoryEntryEntity(
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    val dietRate: String?,
    val carbsPer100g: Double?,
    val category: String?
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var updateTime: Instant = Clock.System.now()

    companion object {
        const val TABLE_NAME = "product_analysis_history"
    }

}

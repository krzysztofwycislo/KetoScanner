package pl.morning.tower.karbo.core.database.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.morning.tower.karbo.core.database.history.ProductAnalysisHistoryEntryEntity.Companion.TABLE_NAME
import kotlin.time.Clock
import kotlin.time.Instant


@Entity(tableName = TABLE_NAME)
data class ProductAnalysisHistoryEntryEntity(
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    val category: String?
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var updateTime: Instant = Clock.System.now()

    companion object {
        const val TABLE_NAME = "product_analysis_history"
    }

}

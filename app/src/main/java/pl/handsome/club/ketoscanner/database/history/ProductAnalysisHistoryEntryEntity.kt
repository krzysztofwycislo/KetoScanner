package pl.handsome.club.ketoscanner.database.history

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.ketoscanner.database.history.ProductAnalysisHistoryEntryEntity.Companion.TABLE_NAME
import java.util.*


@Entity(tableName = TABLE_NAME)
data class ProductAnalysisHistoryEntryEntity(
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    @Embedded val macronutrientAnalysisResult: MacronutrientAnalysisResult?
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var updateTime: Date = Calendar.getInstance().time

    companion object {
        const val TABLE_NAME = "product_analysis_history"
    }

}

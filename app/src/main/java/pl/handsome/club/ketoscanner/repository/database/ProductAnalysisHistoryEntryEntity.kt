package pl.handsome.club.ketoscanner.repository.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.ketoscanner.repository.database.ProductAnalysisHistoryEntryEntity.Companion.TABLE_NAME
import java.util.*


@Entity(tableName = TABLE_NAME)
data class ProductAnalysisHistoryEntryEntity(
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    @Embedded val macronutrientAnalysisResult: MacronutrientAnalysisResult,
    @Embedded val ingredientAnalysisResult: IngredientAnalysisResult?
) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var updateTime: Date = Calendar.getInstance().time

    companion object {
        const val TABLE_NAME = "product_analysis_history"
    }

}

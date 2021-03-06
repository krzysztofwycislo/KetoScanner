package pl.handsome.club.domain.analyze.history

import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import java.util.*


data class ProductAnalysisHistoryEntry(
    val id: Long,
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    val updateTime: Date,
    val macronutrientAnalysisResult: MacronutrientAnalysisResult?
)

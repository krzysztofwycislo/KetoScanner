package pl.handsome.club.feature.history.api.model

import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import kotlin.time.Instant


data class ProductAnalysisHistoryEntry(
    val id: Long,
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    val updateTime: Instant,
    val macronutrientAnalysisResult: MacronutrientAnalysisResult?
)

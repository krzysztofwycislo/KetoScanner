package pl.morning.tower.karbo.feature.history.api.model

import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import kotlin.time.Instant


data class ProductAnalysisHistoryEntry(
    val id: Long,
    val productName: String,
    val productBarcode: String,
    val productBrand: String,
    val updateTime: Instant,
    val dietRate: DietRate?,
    val carbsPer100g: Double?,
    val category: ProductCategory
)

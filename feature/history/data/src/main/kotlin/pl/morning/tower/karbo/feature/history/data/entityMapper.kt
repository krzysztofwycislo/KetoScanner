package pl.morning.tower.karbo.feature.history.data

import pl.morning.tower.karbo.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.carbsPer100g
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry

internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry =
    ProductAnalysisHistoryEntry(
        id = id,
        productName = productName,
        productBarcode = productBarcode,
        productBrand = productBrand,
        updateTime = updateTime,
        dietRate = DietRate.entries.firstOrNull { it.name == dietRate },
        carbsPer100g = carbsPer100g,
        category = ProductCategory.fromName(category),
    )

internal fun List<ProductAnalysisHistoryEntryEntity>.toDomain(): List<ProductAnalysisHistoryEntry> =
    map(ProductAnalysisHistoryEntryEntity::toDomain)

internal fun ProductAnalysis.toEntity(): ProductAnalysisHistoryEntryEntity =
    ProductAnalysisHistoryEntryEntity(
        productName = product.name,
        productBarcode = product.barcode,
        productBrand = product.brand,
        dietRate = dietRate?.name,
        carbsPer100g = carbsPer100g,
        category = product.category.name,
    )

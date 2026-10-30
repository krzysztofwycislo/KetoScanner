package pl.handsome.club.feature.history.data

import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.carbsPer100g
import pl.handsome.club.feature.analysis.api.model.dietRate
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry

internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry =
    ProductAnalysisHistoryEntry(
        id = id,
        productName = productName,
        productBarcode = productBarcode,
        productBrand = productBrand,
        updateTime = updateTime,
        dietRate = DietRate.entries.firstOrNull { it.name == dietRate },
        carbsPer100g = carbsPer100g,
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
    )

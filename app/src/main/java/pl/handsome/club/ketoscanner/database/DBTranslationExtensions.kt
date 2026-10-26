package pl.handsome.club.ketoscanner.database

import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.database.history.ProductAnalysisHistoryEntryEntity


internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry {
    return ProductAnalysisHistoryEntry(
        id,
        productName,
        productBarcode,
        productBrand,
        updateTime,
        macronutrientAnalysisResult
    )
}

internal fun ProductAnalysisResult.toHistoryEntry(): ProductAnalysisHistoryEntryEntity {
    return ProductAnalysisHistoryEntryEntity(
        product.name,
        product.barcode,
        product.brand,
        macronutrientAnalysisResult
    )
}
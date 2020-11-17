package pl.handsome.club.ketoscanner.database

import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.database.history.ProductAnalysisHistoryEntryEntity


internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry {
    return ProductAnalysisHistoryEntry(
        id,
        productName,
        productBarcode,
        productBrand,
        updateTime,
        macronutrientAnalysisResult,
        ingredientAnalysisResult
    )
}

internal fun ProductAnalysisResult.toHistoryEntry(): ProductAnalysisHistoryEntryEntity {
    return ProductAnalysisHistoryEntryEntity(
        product.name,
        product.barcode,
        product.brand,
        macronutrientAnalysisResult,
        ingredientAnalysisResult
    )
}
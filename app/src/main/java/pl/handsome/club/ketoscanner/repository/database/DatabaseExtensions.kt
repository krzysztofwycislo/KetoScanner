package pl.handsome.club.ketoscanner.repository.database

import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry


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
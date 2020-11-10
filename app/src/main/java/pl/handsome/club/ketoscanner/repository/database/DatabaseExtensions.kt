package pl.handsome.club.ketoscanner.repository.database

import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult


internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry {
    return ProductAnalysisHistoryEntry(
        id,
        productName,
        productBarcode,
        productBrand,
        updateTime,
        ingredientAnalysisResult,
        macronutrientAnalysisResult
    )
}

internal fun ProductAnalysisResult.toHistoryEntry(): ProductAnalysisHistoryEntryEntity {
    return ProductAnalysisHistoryEntryEntity(
        product.name,
        product.barcode,
        product.brand,
        ingredientAnalysisResult as? IngredientAnalysisResult.Success,
        macronutrientAnalysisResult as? MacronutrientAnalysisResult.Success
    )
}
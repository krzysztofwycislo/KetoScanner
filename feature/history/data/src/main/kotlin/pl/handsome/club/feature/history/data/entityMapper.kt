package pl.handsome.club.feature.history.data

import pl.handsome.club.core.database.history.MacronutrientAnalysisEmbedded
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry

internal fun ProductAnalysisHistoryEntryEntity.toDomain(): ProductAnalysisHistoryEntry =
    ProductAnalysisHistoryEntry(
        id = id,
        productName = productName,
        productBarcode = productBarcode,
        productBrand = productBrand,
        updateTime = updateTime,
        macronutrientAnalysisResult = macronutrientAnalysisResult?.toDomain(),
    )

internal fun List<ProductAnalysisHistoryEntryEntity>.toDomain(): List<ProductAnalysisHistoryEntry> =
    map(ProductAnalysisHistoryEntryEntity::toDomain)

internal fun ProductAnalysisResult.toEntity(): ProductAnalysisHistoryEntryEntity =
    ProductAnalysisHistoryEntryEntity(
        productName = product.name,
        productBarcode = product.barcode,
        productBrand = product.brand,
        macronutrientAnalysisResult = macronutrientAnalysisResult?.toEntity(),
    )

private fun MacronutrientAnalysisEmbedded.toDomain(): MacronutrientAnalysisResult? {
    val dietRate = DietRate.entries.firstOrNull { it.name == carbsRate } ?: return null
    return MacronutrientAnalysisResult(
        carbsRate = dietRate,
        maxRateCarbAmount = maxRateCarbAmount,
        maxProductPortion = maxProductPortion,
        dailyCarbConsumption = dailyCarbConsumption,
    )
}

private fun MacronutrientAnalysisResult.toEntity(): MacronutrientAnalysisEmbedded =
    MacronutrientAnalysisEmbedded(
        carbsRate = carbsRate.name,
        maxRateCarbAmount = maxRateCarbAmount,
        maxProductPortion = maxProductPortion,
        dailyCarbConsumption = dailyCarbConsumption,
    )

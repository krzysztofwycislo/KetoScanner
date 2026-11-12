package pl.morning.tower.karbo.feature.history.data

import pl.morning.tower.karbo.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.carbsPer100g
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.product.api.model.Product

internal fun ProductAnalysisHistoryEntryEntity.toDomain(analysis: ProductAnalysis?): ProductAnalysisHistoryEntry =
    ProductAnalysisHistoryEntry(
        id = id,
        productName = productName,
        productBarcode = productBarcode,
        productBrand = productBrand,
        updateTime = updateTime,
        dietRate = analysis?.dietRate,
        carbsPer100g = analysis?.carbsPer100g,
        category = analysis?.product?.category ?: ProductCategory.fromName(category),
    )

internal fun List<ProductAnalysisHistoryEntryEntity>.toDomain(
    analyses: Map<Barcode, ProductAnalysis>,
): List<ProductAnalysisHistoryEntry> =
    map { it.toDomain(analyses[Barcode(it.productBarcode)]) }

internal fun Product.toEntity(): ProductAnalysisHistoryEntryEntity =
    ProductAnalysisHistoryEntryEntity(
        productName = name,
        productBarcode = barcode,
        productBrand = brand,
        category = category.name,
    )

package app.carbpeek.feature.history.data

import app.carbpeek.core.database.history.ProductAnalysisHistoryEntryEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.carbsPer100g
import app.carbpeek.feature.analysis.api.model.dietRate
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.product.api.model.Product

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

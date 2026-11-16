package app.carbpeek.feature.favourites.data

import app.carbpeek.core.database.favourite.FavouriteProductEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.carbsPer100g
import app.carbpeek.feature.analysis.api.model.dietRate
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.product.api.model.Product

internal fun FavouriteProductEntity.toDomain(analysis: ProductAnalysis?): FavouriteProduct =
    FavouriteProduct(
        productBarcode = productBarcode,
        productName = productName,
        productBrand = productBrand,
        updateTime = updateTime,
        imageUrl = imageUrl,
        dietRate = analysis?.dietRate,
        carbsPer100g = analysis?.carbsPer100g,
        category = analysis?.product?.category ?: ProductCategory.fromName(category),
    )

internal fun List<FavouriteProductEntity>.toDomain(analyses: Map<Barcode, ProductAnalysis>): List<FavouriteProduct> =
    map { it.toDomain(analyses[Barcode(it.productBarcode)]) }

internal fun Product.toFavouriteEntity(): FavouriteProductEntity =
    FavouriteProductEntity(
        productBarcode = barcode,
        productName = name,
        productBrand = brand,
        imageUrl = imageUrl,
        category = category.name,
    )

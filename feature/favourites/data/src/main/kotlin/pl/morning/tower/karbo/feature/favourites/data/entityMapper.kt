package pl.morning.tower.karbo.feature.favourites.data

import pl.morning.tower.karbo.core.database.favourite.FavouriteProductEntity
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.carbsPer100g
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product

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

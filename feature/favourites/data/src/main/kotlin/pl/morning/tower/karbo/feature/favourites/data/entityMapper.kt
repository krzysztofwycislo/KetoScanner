package pl.morning.tower.karbo.feature.favourites.data

import pl.morning.tower.karbo.core.database.favourite.FavouriteProductEntity
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.carbsPer100g
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.product.api.model.Product

internal fun FavouriteProductEntity.toDomain(): FavouriteProduct =
    FavouriteProduct(
        productBarcode = productBarcode,
        productName = productName,
        productBrand = productBrand,
        updateTime = updateTime,
        imageUrl = imageUrl,
        dietRate = DietRate.entries.firstOrNull { it.name == dietRate },
        carbsPer100g = carbsPer100g,
        category = ProductCategory.fromName(category),
    )

internal fun List<FavouriteProductEntity>.toDomain(): List<FavouriteProduct> =
    map(FavouriteProductEntity::toDomain)

internal fun ProductAnalysis.toFavouriteEntity(): FavouriteProductEntity =
    product.toFavouriteEntity(dietRate = dietRate, carbsPer100g = carbsPer100g)

internal fun Product.toFavouriteEntity(
    dietRate: DietRate? = null,
    carbsPer100g: Double? = null,
): FavouriteProductEntity =
    FavouriteProductEntity(
        productBarcode = barcode,
        productName = name,
        productBrand = brand,
        imageUrl = imageUrl,
        dietRate = dietRate?.name,
        carbsPer100g = carbsPer100g,
        category = category.name,
    )

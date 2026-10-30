package pl.handsome.club.feature.favourites.data

import pl.handsome.club.core.database.favourite.FavouriteProductEntity
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.carbsPer100g
import pl.handsome.club.feature.analysis.api.model.dietRate
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product

internal fun FavouriteProductEntity.toDomain(): FavouriteProduct =
    FavouriteProduct(
        productBarcode = productBarcode,
        productName = productName,
        productBrand = productBrand,
        updateTime = updateTime,
        imageUrl = imageUrl,
        dietRate = DietRate.entries.firstOrNull { it.name == dietRate },
        carbsPer100g = carbsPer100g,
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
    )

package pl.handsome.club.feature.favourites.data

import pl.handsome.club.core.database.favourite.FavouriteProductEntity
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product

internal fun FavouriteProductEntity.toDomain(): FavouriteProduct =
    FavouriteProduct(
        productBarcode = productBarcode,
        productName = productName,
        productBrand = productBrand,
        updateTime = updateTime,
        imageUrl = imageUrl,
    )

internal fun List<FavouriteProductEntity>.toDomain(): List<FavouriteProduct> =
    map(FavouriteProductEntity::toDomain)

internal fun Product.toFavouriteEntity(): FavouriteProductEntity =
    FavouriteProductEntity(
        productBarcode = barcode,
        productName = name,
        productBrand = brand,
        imageUrl = imageUrl,
    )

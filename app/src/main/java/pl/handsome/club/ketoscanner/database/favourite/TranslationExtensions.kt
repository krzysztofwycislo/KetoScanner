package pl.handsome.club.ketoscanner.database.favourite

import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product


internal fun FavouriteProductEntity.toDomain(): FavouriteProduct {
    return FavouriteProduct(
        productBarcode,
        productName,
        productBrand,
        updateTime,
        imageUrl
    )
}

internal fun Product.toFavouriteProductEntity(): FavouriteProductEntity {
    return FavouriteProductEntity(
        barcode,
        name,
        brand,
        imageUrl
    )
}
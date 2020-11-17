package pl.handsome.club.ketoscanner.database.favourite

import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.product.Product


internal fun FavouriteProductEntity.toDomain(): FavouriteProduct {
    return FavouriteProduct(
        productBarcode,
        productName,
        productBrand,
        updateTime
    )
}

internal fun Product.toFavouriteProductEntity(): FavouriteProductEntity {
    return FavouriteProductEntity(
        barcode,
        name,
        brand
    )
}
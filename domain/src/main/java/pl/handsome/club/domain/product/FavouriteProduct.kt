package pl.handsome.club.domain.product

import java.util.*


data class FavouriteProduct(
    val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val updateTime: Date,
    val imageUrl: String?
)
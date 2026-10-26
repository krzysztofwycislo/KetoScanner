package pl.handsome.club.feature.favourites.api.model

import kotlin.time.Instant


data class FavouriteProduct(
    val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val updateTime: Instant,
    val imageUrl: String?
)
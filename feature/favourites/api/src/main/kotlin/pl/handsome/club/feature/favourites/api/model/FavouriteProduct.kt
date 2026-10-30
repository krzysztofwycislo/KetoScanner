package pl.handsome.club.feature.favourites.api.model

import pl.handsome.club.feature.analysis.api.model.DietRate
import kotlin.time.Instant


data class FavouriteProduct(
    val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val updateTime: Instant,
    val imageUrl: String?,
    val dietRate: DietRate?,
    val carbsPer100g: Double?
)
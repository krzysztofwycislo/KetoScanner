package app.carbpeek.feature.favourites.api.model

import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import kotlin.time.Instant


data class FavouriteProduct(
    val productBarcode: String,
    val productName: String,
    val productBrand: String,
    val updateTime: Instant,
    val imageUrl: String?,
    val dietRate: DietRate?,
    val carbsPer100g: Double?,
    val category: ProductCategory
)
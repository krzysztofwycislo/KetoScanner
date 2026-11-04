package pl.morning.tower.karbo.feature.favourites.api.model

import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
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
package pl.morning.tower.karbo.feature.product.api.model

import pl.morning.tower.karbo.core.model.ProductCategory

data class Product(
    val name: String,
    val barcode: String,
    val brand: String,
    val nutriments: ProductNutriments,
    val imageUrl: String?,
    val servingAmount: String?,
    val category: ProductCategory
)
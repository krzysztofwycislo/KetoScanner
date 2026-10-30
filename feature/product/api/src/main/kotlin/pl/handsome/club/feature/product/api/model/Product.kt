package pl.handsome.club.feature.product.api.model

import pl.handsome.club.core.model.ProductCategory

data class Product(
    val name: String,
    val barcode: String,
    val brand: String,
    val nutriments: ProductNutriments,
    val imageUrl: String?,
    val servingAmount: String?,
    val category: ProductCategory
)
package app.carbpeek.feature.product.api.model

import app.carbpeek.core.model.ProductCategory

data class Product(
    val name: String,
    val barcode: String,
    val brand: String,
    val nutriments: ProductNutriments,
    val imageUrl: String?,
    val servingAmount: String?,
    val category: ProductCategory,
    val ingredients: List<Ingredient>? = null,
    val nutriScore: NutriScoreGrade? = null,
    val novaGroup: NovaGroup? = null,
)
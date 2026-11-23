package app.carbpeek.feature.product.api.model

data class Ingredient(
    val id: String,
    val name: String,
    val labelName: String,
    val declaredPercent: Double?,
    val subIngredients: List<Ingredient>,
)

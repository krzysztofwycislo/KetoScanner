package app.carbpeek.shared.contract

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val barcode: String,
    val name: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val servingQuantity: String? = null,
    val category: ProductCategoryCode = ProductCategoryCode.UNKNOWN,
    val labelLanguage: String? = null,
    val ingredientsText: String? = null,
    val ingredients: List<ProductIngredient>? = null,
    val nutriScore: ProductNutriScore? = null,
    val novaGroup: Int? = null,
    val nutrients: ProductNutrients = ProductNutrients(),
)

@Serializable
data class ProductNutrients(
    val per100g: NutrientAmounts = NutrientAmounts(),
    val perServing: NutrientAmounts = NutrientAmounts(),
)

@Serializable
data class NutrientAmounts(
    val energyKcal: Double? = null,
    val fat: Double? = null,
    val saturatedFat: Double? = null,
    val carbohydrates: Double? = null,
    val sugars: Double? = null,
    val fibre: Double? = null,
    val proteins: Double? = null,
    val salt: Double? = null,
)

@Serializable
data class ProductIngredient(
    val id: String,
    val name: String,
    val labelName: String,
    val declaredPercent: Double? = null,
    val subIngredients: List<ProductIngredient> = emptyList(),
)

@Serializable
enum class ProductCategoryCode {
    SPREADS,
    OILS_AND_SAUCES,
    WATER,
    DRINKS,
    MILK_AND_JUICE,
    YOGURT_AND_CREAM,
    CHOCOLATE_AND_BARS,
    CHEESE,
    EGGS,
    FISH,
    NUTS_AND_SEEDS,
    GRAINS,
    BISCUITS,
    TEA_AND_GREENS,
    MEAT,
    OTHER,
    UNKNOWN,
}

@Serializable
enum class ProductNutriScore {
    A,
    B,
    C,
    D,
    E,
}

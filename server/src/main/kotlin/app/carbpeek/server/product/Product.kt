package app.carbpeek.server.product

internal data class Product(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val servingQuantity: String?,
    val category: ProductCategory,
    val labelLanguage: String?,
    val ingredientsText: String?,
    val ingredients: List<Ingredient>?,
    val nutriScore: NutriScore?,
    val novaGroup: Int?,
    val nutrientsPer100g: Nutrients,
    val nutrientsPerServing: Nutrients,
)

internal data class Nutrients(
    val energyKcal: Double?,
    val fat: Double?,
    val saturatedFat: Double?,
    val carbohydrates: Double?,
    val sugars: Double?,
    val fibre: Double?,
    val proteins: Double?,
    val salt: Double?,
)

internal data class Ingredient(
    val id: String,
    val name: String,
    val labelName: String,
    val declaredPercent: Double?,
    val subIngredients: List<Ingredient>,
)

internal enum class ProductCategory {
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
}

internal enum class NutriScore {
    A,
    B,
    C,
    D,
    E,
}

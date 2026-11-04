package pl.morning.tower.karbo.feature.product.api.model


data class ProductNutriments(
    val energyPerServing: Double?,
    val energyPer100g: Double?,

    val fatsPerServing: Double?,
    val fatsPer100g: Double?,

    val proteinsPerServing: Double?,
    val proteinsPer100g: Double?,

    val carbohydratesPerServing: Double?,
    val carbohydratesPer100g: Double?,

    val saltPerServing: Double?,
    val saltPer100g: Double?,

    val sugarsPerServing: Double? = null,
    val sugarsPer100g: Double? = null,

    val fibrePerServing: Double? = null,
    val fibrePer100g: Double? = null,
)

package pl.handsome.club.domain.product


data class ProductNutriments(
    val energyPer100g: Double,
    val energyPerServing: Double,

    val fatPer100g: Double,
    val fatPerServing: Double,

    val saturatedFatPer100g: Double,
    val saturatedFatPerServing: Double,

    val carbohydratesPer100g: Double,
    val carbohydratesPerServing: Double,

    val sugarsPer100g: Double,
    val sugarsPerServing: Double,

    val proteinsPer100g: Double,
    val proteinsPerServing: Double,

    val saltPer100g: Double,
    val saltPerServing: Double
)

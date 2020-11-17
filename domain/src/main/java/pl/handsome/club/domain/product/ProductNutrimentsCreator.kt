package pl.handsome.club.domain.product

fun createProductNutriments(
    servingAmount: Int,
    energyPer100g: Double,
    fatsPer100g: Double,
    proteinsPer100g: Double,
    carbohydratesPer100g: Double,
    saltPer100g: Double
): ProductNutriments {
    return ProductNutriments(
        calculateServing(servingAmount, energyPer100g),
        energyPer100g,
        calculateServing(servingAmount, fatsPer100g),
        fatsPer100g,
        calculateServing(servingAmount, proteinsPer100g),
        proteinsPer100g,
        calculateServing(servingAmount, carbohydratesPer100g),
        carbohydratesPer100g,
        calculateServing(servingAmount, saltPer100g),
        saltPer100g,
    )
}

private fun calculateServing(servingAmount: Int, energyPer100g: Double) : Double {
    return servingAmount * energyPer100g / 100
}
package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments


private const val PRODUCT_NAME = "test name"
private const val PRODUCT_BARCODE = "test barcode"
private const val PRODUCT_COMPANY_NAME = "test company name"
private const val PRODUCT_URL = "test url"
private const val PRODUCT_SERVING = "50"

internal fun createProduct(name: String, productNutriments: ProductNutriments) =
    Product(name, PRODUCT_BARCODE, PRODUCT_COMPANY_NAME, productNutriments, PRODUCT_URL, PRODUCT_SERVING)

internal fun createProduct(productNutriments: ProductNutriments) =
    createProduct(PRODUCT_NAME, productNutriments)


internal val exampleNutrients = ProductNutriments(250.0, 500.0, 20.0, 40.0, 10.0, 20.0, 5.0, 10.0, 1.0, 2.0)

internal val exampleDietPreferences = DietPreferences(
    40
)

internal fun createProductNutriments(
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

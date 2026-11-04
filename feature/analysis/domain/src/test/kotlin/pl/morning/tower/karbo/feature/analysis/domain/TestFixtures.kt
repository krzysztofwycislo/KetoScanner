package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments


private const val PRODUCT_NAME = "test name"
private const val PRODUCT_BARCODE = "test barcode"
private const val PRODUCT_COMPANY_NAME = "test company name"
private const val PRODUCT_URL = "test url"
private const val PRODUCT_SERVING = "50"

internal fun createProduct(name: String, productNutriments: ProductNutriments) =
    Product(
        name,
        PRODUCT_BARCODE,
        PRODUCT_COMPANY_NAME,
        productNutriments,
        PRODUCT_URL,
        PRODUCT_SERVING,
        ProductCategory.OTHER
    )

internal fun createProduct(productNutriments: ProductNutriments) =
    createProduct(PRODUCT_NAME, productNutriments)

internal fun createProduct(name: String, productNutriments: ProductNutriments, servingAmount: String?) =
    Product(
        name,
        PRODUCT_BARCODE,
        PRODUCT_COMPANY_NAME,
        productNutriments,
        PRODUCT_URL,
        servingAmount,
        ProductCategory.OTHER
    )

internal fun macroNutriments(
    fatsPer100g: Double?,
    proteinsPer100g: Double?,
    carbohydratesPer100g: Double?,
    sugarsPer100g: Double? = null,
    fibrePer100g: Double? = null,
) = ProductNutriments(
    energyPerServing = null,
    energyPer100g = null,
    fatsPerServing = null,
    fatsPer100g = fatsPer100g,
    proteinsPerServing = null,
    proteinsPer100g = proteinsPer100g,
    carbohydratesPerServing = null,
    carbohydratesPer100g = carbohydratesPer100g,
    saltPerServing = null,
    saltPer100g = null,
    sugarsPer100g = sugarsPer100g,
    fibrePer100g = fibrePer100g,
)

internal fun profile(
    fat: Double,
    protein: Double,
    carbs: Double,
    sugars: Double? = null,
    servingGrams: Double? = null,
) = NutritionProfile(carbs = carbs, fat = fat, protein = protein, sugars = sugars, servingGrams = servingGrams)


internal val exampleNutrients = ProductNutriments(250.0, 500.0, 20.0, 40.0, 10.0, 20.0, 5.0, 10.0, 1.0, 2.0)

internal val exampleDietPreferences = DietPreferences(
    40
)

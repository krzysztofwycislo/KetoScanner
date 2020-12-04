package pl.handsome.club.ketoscanner.viewmodel

import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


private const val PRODUCT_NAME: String = "test name"
private const val PRODUCT_BARCODE: String = "test barcode"
private const val PRODUCT_COMPANY_NAME: String = "test company name"
private const val PRODUCT_URL: String = "test url"
private const val SERVING_AMOUNT: String = "50"

val exampleNutrients = ProductNutriments(250.0, 500.0, 20.0, 40.0, 10.0, 20.0, 5.0, 10.0, 1.0, 2.0)

val exampleProduct = Product(
    PRODUCT_NAME,
    PRODUCT_BARCODE,
    PRODUCT_COMPANY_NAME,
    exampleNutrients,
    PRODUCT_URL,
    SERVING_AMOUNT
)

val exampleDietPreferences = DietPreferences( 40)

val exampleMacronutrientAnalysisResult = MacronutrientAnalysisResult(DietRate.GOOD, 10, 20.0, 30)

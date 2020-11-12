package pl.handsome.club.ketoscanner.viewmodel

import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500


private const val PRODUCT_NAME: String = "test name"
private const val PRODUCT_BARCODE: String = "test barcode"
private const val PRODUCT_COMPANY_NAME: String = "test company name"
private const val PRODUCT_URL: String = "test url"

val exampleNutrients = ProductNutriments(40.0, 20.0, 30.0)

val testProduct =
    Product(PRODUCT_NAME, PRODUCT_BARCODE, PRODUCT_COMPANY_NAME, exampleNutrients, PRODUCT_URL,)

val exampleMacronutrientPreferences = MacronutrientPreferences(75, 10, 15, 40)
val exampleIngredientPreferences = IngredientPreferences()
val exampleDietPreferences =
    DietPreferences(KCAL_2500, exampleMacronutrientPreferences, exampleIngredientPreferences)


val exampleMacronutrientAnalysisResult = MacronutrientAnalysisResult(DietRate.GOOD, 1)
val exampleIngredientAnalysisResult = IngredientAnalysisResult()

package pl.handsome.club.ketoscanner.viewmodel

import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500

const val PRODUCT_NAME: String = "test"
const val PRODUCT_BARCODE: String = "12345679"

val exampleNutrients = ProductNutriments(
    40.0,
    20.0,
    30.0
)

val testProduct = Product(PRODUCT_NAME, PRODUCT_BARCODE, exampleNutrients)

val examplePreferences = MacronutrientPreferences(75, 40, 10, 15)
val exampleIngredientPreferences = IngredientPreferences(true, emptyList(), emptyList())
val exampleDietPreferences = DietPreferences(KCAL_2500, exampleIngredientPreferences, examplePreferences)
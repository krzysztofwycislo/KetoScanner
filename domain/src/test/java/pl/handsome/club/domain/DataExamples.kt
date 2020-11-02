package pl.handsome.club.domain

import pl.handsome.club.domain.preferences.*
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


const val KCAL_2500: Int = 2500

const val PRODUCT_NAME: String = "test"
const val PRODUCT_BARCODE: String = "12345679"


val macronutrientPreferences = MacronutrientPreferences(
    70,
    5,
    25
)

val ingredientPreferences = IngredientPreferences(
    true,
    emptyList(),
    emptyList()
)

val exampleNutrients = ProductNutriments(
    512.0,
    206.0,
    30.0,
    15.0,
    14.0,
    7.0,
    44.0,
    22.0,
    10.0,
    5.0,
    20.0,
    10.0,
    3.0,
    1.5
)

package pl.handsome.club.domain

import pl.handsome.club.domain.preferences.*
import pl.handsome.club.domain.product.Product


const val KCAL_2500: Int = 2500

private const val PRODUCT_NAME: String = "test"
private const val PRODUCT_BARCODE: String = "12345679"


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

val exampleProduct = Product(PRODUCT_NAME, PRODUCT_BARCODE)
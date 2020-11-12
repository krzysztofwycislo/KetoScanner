package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.domain.createProduct
import pl.handsome.club.domain.exampleKetoPreferences
import pl.handsome.club.domain.product.ProductNutriments


private val avocadoNutriments = ProductNutriments(15.0, 9.0, 2.0,)
private val salmonFilletNutriments = ProductNutriments(13.6, 0.0, 20.0,)
private val sourCreamNutriments = ProductNutriments(30.0, 3.0, 2.0,)
private val wholemealPastaNutriments = ProductNutriments(1.0, 29.0, 5.7)
private val tomatoNutriments = ProductNutriments(0.2, 2.9, 0.2)
private val carrotNutriments = ProductNutriments(0.2, 5.1, 1.0)
private val coconutMilkNutriments = ProductNutriments(18.0, 8.1, 2.1)

val macronutrientAnalyzerTestParameters = listOf(
    arrayOf(
        createProduct("avocado", avocadoNutriments),
        exampleKetoPreferences,
        DietRate.GOOD,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("salmonFillet", salmonFilletNutriments),
        exampleKetoPreferences,
        DietRate.NEUTRAL,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("sourCream", sourCreamNutriments),
        exampleKetoPreferences,
        DietRate.GOOD,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("wholemealPasta", wholemealPastaNutriments),
        exampleKetoPreferences,
        DietRate.NOT_ADVISED,
        DietRate.NOT_ADVISED
    ),

    arrayOf(
        createProduct("tomato", tomatoNutriments),
        exampleKetoPreferences,
        DietRate.NOT_ADVISED,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("carrot", carrotNutriments),
        exampleKetoPreferences,
        DietRate.NOT_ADVISED,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("coconutMilk", coconutMilkNutriments),
        exampleKetoPreferences,
        DietRate.GOOD,
        DietRate.GOOD
    ),
)
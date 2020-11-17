package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.domain.createProduct
import pl.handsome.club.domain.exampleKetoPreferences
import pl.handsome.club.domain.product.createProductNutriments


private val avocadoNutriments = createProductNutriments(50, 169.0, 15.0, 2.0, 9.0, 0.0)

private val salmonFilletNutriments = createProductNutriments(50, 201.0, 13.6, 20.0, 0.0, 0.0)

private val sourCreamNutriments = createProductNutriments(50, 291.0, 30.0, 2.0, 3.0, 0.0)

private val wholemealPastaNutriments = createProductNutriments(50, 351.0, 1.0, 5.7, 72.0, 0.0)

private val tomatoNutriments = createProductNutriments(50, 19.0, 0.2, 2.9, 0.2, 0.0)

private val carrotNutriments = createProductNutriments(50, 33.0, 0.2, 1.0, 5.1, 0.0)

private val coconutMilkNutriments = createProductNutriments(50, 201.0, 18.0, 8.1, 2.1, 0.0)


val macronutrientAnalyzerTestParameters = listOf(
    arrayOf(
        createProduct("avocado", avocadoNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("salmonFillet", salmonFilletNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("sourCream", sourCreamNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("wholemealPasta", wholemealPastaNutriments),
        exampleKetoPreferences,
        DietRate.NOT_ADVISED
    ),

    arrayOf(
        createProduct("tomato", tomatoNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("carrot", carrotNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),

    arrayOf(
        createProduct("coconutMilk", coconutMilkNutriments),
        exampleKetoPreferences,
        DietRate.GOOD
    ),
)
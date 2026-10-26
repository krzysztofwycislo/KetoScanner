package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class MacronutrientAnalyzerTest {

    private val preferences = DietPreferences(40)

    private val foodCases = listOf(
        FoodCase("avocado", createProductNutriments(50, 169.0, 15.0, 2.0, 9.0, 0.0), DietRate.GOOD),
        FoodCase("salmonFillet", createProductNutriments(50, 201.0, 13.6, 20.0, 0.0, 0.0), DietRate.GOOD),
        FoodCase("sourCream", createProductNutriments(50, 291.0, 30.0, 2.0, 3.0, 0.0), DietRate.GOOD),
        FoodCase("wholemealPasta", createProductNutriments(50, 351.0, 1.0, 5.7, 72.0, 0.0), DietRate.NOT_ADVISED),
        FoodCase("tomato", createProductNutriments(50, 19.0, 0.2, 2.9, 0.2, 0.0), DietRate.GOOD),
        FoodCase("carrot", createProductNutriments(50, 33.0, 0.2, 1.0, 5.1, 0.0), DietRate.GOOD),
        FoodCase("coconutMilk", createProductNutriments(50, 201.0, 18.0, 8.1, 2.1, 0.0), DietRate.GOOD),
    )

    @Test
    fun `when we want to analyze product then result we should get correct ratings`() {
        foodCases.forEach { case ->
            val result = MacronutrientAnalyzer.analyze(preferences, case.nutriments)

            assertNotNull(result, "result for ${case.name}")
            assertEquals(case.expectedRate, result.carbsRate, "carbsRate for ${case.name}")
        }
    }

    @Test
    fun `when carbohydratesPer100g is null then result is null`() {
        val result = MacronutrientAnalyzer.analyze(preferences, nutrimentsWithCarbs(null))

        assertNull(result)
    }

    @Test
    fun `when carbs are above a third and at most half of the daily limit then rate is NEUTRAL`() {
        val result = MacronutrientAnalyzer.analyze(preferences, nutrimentsWithCarbs(15.0))

        assertNotNull(result)
        assertEquals(DietRate.NEUTRAL, result.carbsRate)
        assertEquals(20, result.maxRateCarbAmount)
    }

    @Test
    fun `when carbs equal a third of the daily limit then rate is GOOD`() {
        val result = MacronutrientAnalyzer.analyze(preferences, nutrimentsWithCarbs(13.0))

        assertNotNull(result)
        assertEquals(DietRate.GOOD, result.carbsRate)
    }

    @Test
    fun `when carbs exceed half of the daily limit then rate is NOT_ADVISED`() {
        val result = MacronutrientAnalyzer.analyze(preferences, nutrimentsWithCarbs(21.0))

        assertNotNull(result)
        assertEquals(DietRate.NOT_ADVISED, result.carbsRate)
        assertEquals(40, result.maxRateCarbAmount)
    }

    @Test
    fun `when analyzed then maxProductPortion is daily limit divided by carbs times 100`() {
        val result = MacronutrientAnalyzer.analyze(preferences, nutrimentsWithCarbs(10.0))

        assertNotNull(result)
        assertEquals(400.0, result.maxProductPortion)
        assertEquals(40, result.dailyCarbConsumption)
    }

    private fun nutrimentsWithCarbs(carbohydratesPer100g: Double?) =
        ProductNutriments(null, null, null, null, null, null, null, carbohydratesPer100g, null, null)

    private data class FoodCase(
        val name: String,
        val nutriments: ProductNutriments,
        val expectedRate: DietRate
    )

}

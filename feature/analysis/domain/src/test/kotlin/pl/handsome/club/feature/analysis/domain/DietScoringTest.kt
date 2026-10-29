package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.DietRate.GOOD
import pl.handsome.club.feature.analysis.api.model.DietRate.NEUTRAL
import pl.handsome.club.feature.analysis.api.model.DietRate.NOT_ADVISED
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DietScoringTest {

    private val engine = DietAnalysisEngine()

    private val foods = listOf(
        Food("Coca-Cola", 0.0, 0.0, 10.6, 10.6, null, "330", NOT_ADVISED, NOT_ADVISED),
        Food("Diet cola", 0.0, 0.0, 0.0, 0.0, null, "330", GOOD, GOOD),
        Food("Olive oil", 91.6, 0.0, 0.0, 0.0, null, null, GOOD, GOOD),
        Food("Cheddar", 34.0, 25.0, 0.1, 0.1, null, "30", GOOD, GOOD),
        Food("Walnuts", 65.0, 15.0, 7.0, 2.6, 6.7, "30", GOOD, GOOD),
        Food("Chicken breast", 1.5, 23.0, 0.0, 0.0, null, null, GOOD, GOOD),
        Food("Greek yoghurt 10 %", 10.0, 4.0, 3.5, 3.5, null, "150", GOOD, GOOD),
        Food("Milk 3.2 %", 3.2, 3.3, 4.8, 4.8, null, "250", NOT_ADVISED, NEUTRAL),
        Food("Hummus", 20.0, 7.0, 12.0, 0.5, null, null, NEUTRAL, GOOD),
        Food("Dark chocolate 90 %", 55.0, 10.0, 14.0, 7.0, null, null, NEUTRAL, NEUTRAL),
        Food("White bread", 3.0, 8.0, 49.0, 5.0, null, null, NOT_ADVISED, NOT_ADVISED),
        Food("Tomato", 0.2, 0.9, 2.9, 2.6, null, null, GOOD, GOOD),
    )

    @Test
    fun `when products are rated then each gets the expected rate in both modes`() {
        val failures = foods.flatMap { food ->
            listOf(DietMode.KETO to food.keto, DietMode.LOW_CARB to food.lowCarb).mapNotNull { (mode, expected) ->
                val actual = analyze(food, mode).rate
                if (actual == expected) null else "${food.name} in $mode: expected $expected but was $actual"
            }
        }

        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    @Test
    fun `when Coca-Cola is rated in keto then every failed criterion is listed in table order`() {
        val result = analyze(foods.single { it.name == "Coca-Cola" }, DietMode.KETO)

        assertReasons(
            listOf(
                RatingReason.HighCarbEnergyShare(1.0, NOT_ADVISED),
                RatingReason.LowFatAndProteinToCarbRatio(0.0, NOT_ADVISED),
                RatingReason.HighSugars(10.6, NOT_ADVISED),
                RatingReason.HighCarbsPerServing(330.0, 34.98, NOT_ADVISED),
            ),
            result.reasons,
        )
    }

    @Test
    fun `when chicken breast is rated in keto then it has no reasons`() {
        val result = analyze(foods.single { it.name == "Chicken breast" }, DietMode.KETO)

        assertEquals(GOOD, result.rate)
        assertEquals(emptyList(), result.reasons)
    }

    @Test
    fun `when milk is rated in keto then the ratio is neutral and the carb share not advised`() {
        val result = analyze(foods.single { it.name == "Milk 3.2 %" }, DietMode.KETO)

        assertReasons(
            listOf(
                RatingReason.HighCarbEnergyShare(19.2 / 61.2, NOT_ADVISED),
                RatingReason.LowFatAndProteinToCarbRatio(6.5 / 4.8, NEUTRAL),
                RatingReason.HighCarbsPerServing(250.0, 12.0, NEUTRAL),
            ),
            result.reasons,
        )
    }

    @Test
    fun `when reasons have different rates then not advised ones come first`() {
        val result = engine.analyze(
            DietPreferences(40, DietMode.KETO),
            createProduct("mixed", macroNutriments(10.0, 0.0, 15.0, sugarsPer100g = 20.0), null),
        )

        val rated = assertIs<ProductAnalysis.Rated>(result)
        assertEquals(
            listOf(NOT_ADVISED, NOT_ADVISED, NOT_ADVISED, NEUTRAL),
            rated.macronutrients.reasons.map { it.rate },
        )
        assertIs<RatingReason.HighCarbs>(rated.macronutrients.reasons.last())
    }

    private fun analyze(food: Food, mode: DietMode): MacronutrientAnalysisResult {
        val product = createProduct(
            food.name,
            macroNutriments(food.fat, food.protein, food.carbs, food.sugars, food.fibre),
            food.serving,
        )
        val result = engine.analyze(DietPreferences(40, mode), product)
        return assertIs<ProductAnalysis.Rated>(result, "${food.name} in $mode").macronutrients
    }

    private fun assertReasons(expected: List<RatingReason>, actual: List<RatingReason>) {
        assertEquals(expected.map { it::class to it.rate }, actual.map { it::class to it.rate })
        expected.zip(actual).forEach { (expectedReason, actualReason) ->
            val pairs = values(expectedReason).zip(values(actualReason))
            assertTrue(pairs.all { (e, a) -> abs(e - a) < TOLERANCE }, "expected $expectedReason but was $actualReason")
        }
    }

    private fun values(reason: RatingReason): List<Double> = when (reason) {
        is RatingReason.HighCarbs -> listOf(reason.gramsPer100g)
        is RatingReason.HighCarbEnergyShare -> listOf(reason.share)
        is RatingReason.LowFatAndProteinToCarbRatio -> listOf(reason.ratio)
        is RatingReason.HighSugars -> listOf(reason.gramsPer100g)
        is RatingReason.HighCarbsPerServing -> listOf(reason.servingGrams, reason.carbsGrams)
    }

    private data class Food(
        val name: String,
        val fat: Double,
        val protein: Double,
        val carbs: Double,
        val sugars: Double?,
        val fibre: Double?,
        val serving: String?,
        val keto: DietRate,
        val lowCarb: DietRate,
    )

    private companion object {
        const val TOLERANCE = 0.0001
    }
}

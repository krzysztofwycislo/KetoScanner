package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.DietRate.GOOD
import app.carbpeek.feature.analysis.api.model.DietRate.NEUTRAL
import app.carbpeek.feature.analysis.api.model.DietRate.NOT_ADVISED
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DietScoringTest {

    private val engine = DietAnalysisEngine()

    @Test
    fun `when real products are rated then each gets the expected rate under every preset`() {
        val failures = realProducts.flatMap { realProduct ->
            DietPreset.entries.mapNotNull { preset ->
                val expected = realProduct.rates.getValue(preset)
                val actual = analyze(realProduct, preset).rate
                if (actual == expected) {
                    null
                } else {
                    "${realProduct.name} in $preset: expected $expected but was $actual"
                }
            }
        }

        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    @Test
    fun `when Coca-Cola is rated in keto then every failed criterion is listed in table order`() {
        val result = analyze(realProducts.named("Coca-Cola (can)"), DietPreset.KETO)

        assertReasons(
            listOf(
                RatingReason.HighCarbEnergyShare(1.0, NOT_ADVISED),
                RatingReason.LowFatAndProteinToCarbRatio(0.0, NOT_ADVISED),
                RatingReason.HighSugars(10.6, NOT_ADVISED),
                RatingReason.HighCarbsPerServing(330.0, 34.98, NOT_ADVISED),
                RatingReason.HighCarbs(10.6, NEUTRAL),
            ),
            result.reasons,
        )
    }

    @Test
    fun `when chicken breast is rated in keto then it has no reasons`() {
        val result = analyze(realProducts.named("Chicken breast (Fleury Michon)"), DietPreset.KETO)

        assertEquals(GOOD, result.rate)
        assertEquals(emptyList(), result.reasons)
    }

    @Test
    fun `when milk is rated in keto then the ratio is neutral and the carb share not advised`() {
        val result = analyze(realProducts.named("Mleko świeże 2% (Piątnica)"), DietPreset.KETO)

        assertReasons(
            listOf(
                RatingReason.HighCarbEnergyShare(19.2 / 50.0, NOT_ADVISED),
                RatingReason.LowFatAndProteinToCarbRatio(5.2 / 4.8, NEUTRAL),
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

    private fun analyze(realProduct: RealProduct, preset: DietPreset): MacronutrientAnalysisResult {
        val result = engine.analyze(preset.preferences, realProduct.product)
        return assertIs<ProductAnalysis.Rated>(result, "${realProduct.name} in $preset").macronutrients
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

    private companion object {
        const val TOLERANCE = 0.0001
    }
}

package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.DietRate.GOOD
import app.carbpeek.feature.analysis.api.model.DietRate.NEUTRAL
import app.carbpeek.feature.analysis.api.model.DietRate.NOT_ADVISED
import app.carbpeek.feature.analysis.api.model.RatingReason
import kotlin.test.Test
import kotlin.test.assertEquals

class KetoScorerTest {

    @Test
    fun `carbs per 100 g are rated against a quarter and a half of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 10.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 10.1)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 20.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 20.1)))
    }

    @Test
    fun `a lower daily limit moves the carbs per 100 g thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 5.0), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 5.1), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 10.0), dailyCarbs = 20))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 10.1), dailyCarbs = 20))
    }

    @Test
    fun `carb share of energy is rated against 10 and 25 percent`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 45.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 44.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 15.0, 5.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 14.0, 5.0)))
    }

    @Test
    fun `fat and protein to carb ratio is rated against 2 and 1`() {
        assertEquals(GOOD, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(10.0, 0.0, 5.0)))
        assertEquals(GOOD, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(4.0, 6.0, 5.0)))
        assertEquals(GOOD, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(0.0, 10.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(4.9, 5.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(2.0, 3.0, 5.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(2.0, 2.9, 5.0)))
    }

    @Test
    fun `carb share and fat and protein to carb ratio are skipped at 4 g carbs or less`() {
        val reasons = KetoScorer.reasons(profile(0.0, 0.0, 4.0), DAILY_CARBS)

        assertEquals(emptyList(), reasons.filterIsInstance<RatingReason.HighCarbEnergyShare>())
        assertEquals(emptyList(), reasons.filterIsInstance<RatingReason.LowFatAndProteinToCarbRatio>())
    }

    @Test
    fun `light carbs cap carb share and ratio at neutral`() {
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(0.3, 0.7, 5.7, sugars = 5.0)))
        val sugarsAtLightLimit = profile(fat = 0.3, protein = 0.7, carbs = 5.7, sugars = 7.0)
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(sugarsAtLightLimit))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(sugarsAtLightLimit))
    }

    @Test
    fun `carb share and ratio are not capped with sugars over 7 g or unknown`() {
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 7.1)))
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(0.3, 0.7, 5.7, sugars = 7.1)),
        )
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(profile(0.3, 0.7, 5.7)))
    }

    @Test
    fun `carb share and ratio are capped only while carbs per serving are within a quarter of the daily limit`() {
        val withinQuarter = profile(0.3, 0.7, 5.7, sugars = 5.0, servingGrams = 175.0)
        val overQuarter = profile(0.3, 0.7, 5.7, sugars = 5.0, servingGrams = 200.0)

        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(withinQuarter))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(withinQuarter))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(overQuarter))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(overQuarter))
    }

    @Test
    fun `a serving under 100 g counts as 100 g for light carbs`() {
        val overQuarterPer100g = profile(fat = 0.3, protein = 0.7, carbs = 11.0, sugars = 1.0, servingGrams = 22.0)
        val withinQuarterPer100g = profile(fat = 0.3, protein = 0.7, carbs = 9.0, sugars = 1.0, servingGrams = 22.0)

        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(overQuarterPer100g))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(overQuarterPer100g))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(withinQuarterPer100g))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(withinQuarterPer100g))
    }

    @Test
    fun `light carbs rate the carb share against 25 percent`() {
        assertEquals(
            GOOD,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 15.0, carbs = 5.0, sugars = 1.0)),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 14.0, carbs = 5.0, sugars = 1.0)),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 15.0, carbs = 5.0, sugars = null)),
        )
    }

    @Test
    fun `light carbs rate the fat and protein to carb ratio against 1`() {
        assertEquals(
            GOOD,
            rateOf<RatingReason.LowFatAndProteinToCarbRatio>(
                profile(fat = 0.0, protein = 5.0, carbs = 5.0, sugars = 1.0),
            ),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.LowFatAndProteinToCarbRatio>(
                profile(fat = 0.0, protein = 4.5, carbs = 5.0, sugars = 1.0),
            ),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.LowFatAndProteinToCarbRatio>(
                profile(fat = 0.0, protein = 7.5, carbs = 5.0, sugars = null),
            ),
        )
    }

    @Test
    fun `a whole plant food caps carb share and ratio at neutral`() {
        val orange = profile(fat = 0.1, protein = 1.0, carbs = 9.0, sugars = 9.0, fibre = 2.0)
        val orangeJuice = orange.copy(liquid = true)
        val lowFibreOrange = orange.copy(fibre = 1.34)

        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(orange))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(orange))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(orangeJuice))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(orangeJuice))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(lowFibreOrange))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(lowFibreOrange))
    }

    @Test
    fun `a whole plant food keeps the good thresholds`() {
        val wholePlant = profile(fat = 15.0, protein = 3.0, carbs = 10.0, sugars = 8.0, fibre = 2.0)

        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(wholePlant))
        assertEquals(NEUTRAL, rateOf<RatingReason.LowFatAndProteinToCarbRatio>(wholePlant))
    }

    @Test
    fun `lean protein without carbs has no reasons`() {
        assertEquals(emptyList(), KetoScorer.reasons(profile(1.5, 23.0, 0.0), DAILY_CARBS))
    }

    @Test
    fun `sugars are rated against 5 and 10 g and skipped when unknown`() {
        assertEquals(GOOD, rateOf<RatingReason.HighSugars>(profile(50.0, 0.0, 1.0, sugars = 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(50.0, 0.0, 1.0, sugars = 5.1)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(50.0, 0.0, 1.0, sugars = 10.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighSugars>(profile(50.0, 0.0, 1.0, sugars = 10.1)))
        assertEquals(GOOD, rateOf<RatingReason.HighSugars>(profile(50.0, 0.0, 1.0, sugars = null)))
    }

    @Test
    fun `carbs per serving are rated against a quarter and a half of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 100.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 101.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 200.0)))
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 201.0)),
        )
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = null)))
    }

    @Test
    fun `a lower daily limit moves the carbs per serving thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 50.0), 20))
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 51.0), 20),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 100.0), 20),
        )
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 101.0), 20),
        )
    }

    private inline fun <reified T : RatingReason> rateOf(
        profile: NutritionProfile,
        dailyCarbs: Int = DAILY_CARBS,
    ): DietRate =
        KetoScorer.reasons(profile, dailyCarbs).filterIsInstance<T>().singleOrNull()?.rate ?: GOOD

    private companion object {
        const val DAILY_CARBS = 40
    }
}

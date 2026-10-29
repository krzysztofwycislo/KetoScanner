package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.DietRate.GOOD
import pl.handsome.club.feature.analysis.api.model.DietRate.NEUTRAL
import pl.handsome.club.feature.analysis.api.model.DietRate.NOT_ADVISED
import pl.handsome.club.feature.analysis.api.model.RatingReason
import kotlin.test.Test
import kotlin.test.assertEquals

class KetoScorerTest {

    @Test
    fun `carbs per 100 g are rated against a third and a half of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 40 / 3.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 13.4)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 20.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 20.1)))
    }

    @Test
    fun `a lower daily limit moves the carbs per 100 g thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 20 / 3.0), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(50.0, 0.0, 7.0), dailyCarbs = 20))
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
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 201.0)))
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = null)))
    }

    @Test
    fun `a lower daily limit moves the carbs per serving thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 50.0), 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 51.0), 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 100.0), 20))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbsPerServing>(profile(50.0, 0.0, 10.0, servingGrams = 101.0), 20))
    }

    private inline fun <reified T : RatingReason> rateOf(profile: NutritionProfile, dailyCarbs: Int = DAILY_CARBS): DietRate =
        KetoScorer.reasons(profile, dailyCarbs).filterIsInstance<T>().singleOrNull()?.rate ?: GOOD

    private companion object {
        const val DAILY_CARBS = 40
    }
}

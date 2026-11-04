package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.GOOD
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.NEUTRAL
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.NOT_ADVISED
import pl.morning.tower.karbo.feature.analysis.api.model.RatingReason
import kotlin.test.Test
import kotlin.test.assertEquals

class LowCarbScorerTest {

    @Test
    fun `carbs per 100 g are rated against a third and a half of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 40 / 3.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 13.4)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 20.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 20.1)))
    }

    @Test
    fun `a lower daily limit moves the carbs per 100 g thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 20 / 3.0), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 7.0), dailyCarbs = 20))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 10.1), dailyCarbs = 20))
    }

    @Test
    fun `carb share of energy is rated against 25 and 50 percent`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 15.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 14.0, 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 5.0, 5.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 4.9, 5.0)))
    }

    @Test
    fun `carb share is skipped at 4 g carbs or less`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 0.0, 4.0)))
    }

    @Test
    fun `sugars are rated against 5 and 10 g and skipped when unknown`() {
        assertEquals(GOOD, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 5.1)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 10.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 10.1)))
        assertEquals(GOOD, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = null)))
    }

    @Test
    fun `carbs per serving are rated against a quarter and a half of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 100.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 101.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 200.0)))
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 201.0)),
        )
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = null)))
        assertEquals(GOOD, rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 50.0), 20))
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbsPerServing>(profile(0.0, 50.0, 10.0, servingGrams = 101.0), 20),
        )
    }

    @Test
    fun `fat is never required`() {
        val reasons = LowCarbScorer.reasons(profile(0.0, 50.0, 2.0), DAILY_CARBS)

        assertEquals(emptyList(), reasons)
    }

    private inline fun <reified T : RatingReason> rateOf(
        profile: NutritionProfile,
        dailyCarbs: Int = DAILY_CARBS,
    ): DietRate =
        LowCarbScorer.reasons(profile, dailyCarbs).filterIsInstance<T>().singleOrNull()?.rate ?: GOOD

    private companion object {
        const val DAILY_CARBS = 40
    }
}

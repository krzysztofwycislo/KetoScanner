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
    fun `carbs per 100 g are rated against a quarter and a third of the daily limit`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 10.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 10.1)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 40 / 3.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 13.4)))
    }

    @Test
    fun `a lower daily limit moves the carbs per 100 g thresholds`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 5.0), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 5.1), dailyCarbs = 20))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 20 / 3.0), dailyCarbs = 20))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbs>(profile(0.0, 50.0, 6.7), dailyCarbs = 20))
    }

    @Test
    fun `carb share of energy is flagged only above 50 percent`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 15.0, carbs = 5.0)))
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 14.0, carbs = 5.0)))
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 5.0, carbs = 5.0)))
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 4.9, carbs = 5.0)),
        )
    }

    @Test
    fun `carb share is skipped at 4 g carbs or less`() {
        assertEquals(GOOD, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.0, 0.0, 4.0)))
    }

    @Test
    fun `light carbs cap the carb share at neutral`() {
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 7.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 7.1)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7)))
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 5.0, servingGrams = 175.0)),
        )
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(0.3, 0.7, 5.7, sugars = 5.0, servingGrams = 200.0)),
        )
    }

    @Test
    fun `carb share up to 50 percent has no reason with or without light carbs`() {
        assertEquals(
            GOOD,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 10.0, carbs = 5.0, sugars = 1.0)),
        )
        assertEquals(
            GOOD,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 5.0, carbs = 5.0, sugars = 1.0)),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 4.9, carbs = 5.0, sugars = 1.0)),
        )
        assertEquals(
            GOOD,
            rateOf<RatingReason.HighCarbEnergyShare>(profile(fat = 0.0, protein = 10.0, carbs = 5.0, sugars = null)),
        )
    }

    @Test
    fun `a whole plant food caps the carb share at neutral`() {
        val orange = profile(fat = 0.1, protein = 1.0, carbs = 9.0, sugars = 9.0, fibre = 2.0)
        val starchy = profile(fat = 0.5, protein = 2.0, carbs = 30.0, sugars = 8.0, fibre = 5.0)

        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(orange))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(orange.copy(liquid = true)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighCarbEnergyShare>(starchy))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighCarbEnergyShare>(starchy.copy(sugars = 9.0)))
    }

    @Test
    fun `a serving under 100 g counts as 100 g for light carbs`() {
        assertEquals(
            NOT_ADVISED,
            rateOf<RatingReason.HighCarbEnergyShare>(
                profile(fat = 0.3, protein = 0.7, carbs = 11.0, sugars = 1.0, servingGrams = 22.0),
            ),
        )
        assertEquals(
            NEUTRAL,
            rateOf<RatingReason.HighCarbEnergyShare>(
                profile(fat = 0.3, protein = 0.7, carbs = 9.0, sugars = 1.0, servingGrams = 22.0),
            ),
        )
    }

    @Test
    fun `a light high-protein pudding is low-carb friendly`() {
        val pudding = profile(fat = 1.5, protein = 10.0, carbs = 5.5, sugars = 4.0, servingGrams = 200.0)

        assertEquals(emptyList(), LowCarbScorer.reasons(profile = pudding, dailyCarbs = 100))
    }

    @Test
    fun `sugars are rated against 5 and 15 g and skipped when unknown`() {
        assertEquals(GOOD, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 5.0)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 5.1)))
        assertEquals(NEUTRAL, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 15.0)))
        assertEquals(NOT_ADVISED, rateOf<RatingReason.HighSugars>(profile(0.0, 50.0, 1.0, sugars = 15.1)))
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

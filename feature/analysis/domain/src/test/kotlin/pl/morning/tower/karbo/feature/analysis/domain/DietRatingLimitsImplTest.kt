package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DietRatingLimitsImplTest {

    private val limits = DietRatingLimitsImpl()

    @Test
    fun `carbs limits are a third and a half of the daily limit`() {
        assertEquals(CarbLimits(20 / 3.0, 10.0), limits.carbsPer100g(DietPreferences(20, DietMode.KETO)))
        assertEquals(CarbLimits(40 / 3.0, 20.0), limits.carbsPer100g(DietPreferences(40, DietMode.KETO)))
        assertEquals(CarbLimits(100 / 3.0, 50.0), limits.carbsPer100g(DietPreferences(100, DietMode.LOW_CARB)))
    }

    @Test
    fun `carbs exactly at the good limit give no high carbs reason`() {
        DietMode.entries.forEach { mode ->
            val preferences = DietPreferences(40, mode)
            val goodMax = limits.carbsPer100g(preferences).goodMax

            val reasons = scorerFor(mode).reasons(profile(fat = 80.0, protein = 20.0, carbs = goodMax), 40)

            assertNull(reasons.firstOrNull { it is RatingReason.HighCarbs })
        }
    }

    @Test
    fun `carbs just above the neutral limit give a not advised high carbs reason`() {
        DietMode.entries.forEach { mode ->
            val preferences = DietPreferences(40, mode)
            val carbs = limits.carbsPer100g(preferences).neutralMax + 0.01

            val reasons = scorerFor(mode).reasons(profile(fat = 80.0, protein = 20.0, carbs = carbs), 40)

            assertEquals(
                RatingReason.HighCarbs(carbs, DietRate.NOT_ADVISED),
                reasons.first { it is RatingReason.HighCarbs },
            )
        }
    }
}

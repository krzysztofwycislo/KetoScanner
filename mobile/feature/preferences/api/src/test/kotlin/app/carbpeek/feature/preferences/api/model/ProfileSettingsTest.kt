package app.carbpeek.feature.preferences.api.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileSettingsTest {

    @Test
    fun `the quick card shows only when a diet is picked and details do not open right away`() {
        val keto = DietPreferences(maxDailyCarbsAmount = 40, dietMode = DietMode.KETO)

        assertEquals(
            expected = true,
            actual = ProfileSettings(dietPreferences = keto, opensDetailsRightAway = false).showsQuickCard,
        )
        assertEquals(
            expected = false,
            actual = ProfileSettings(dietPreferences = keto, opensDetailsRightAway = true).showsQuickCard,
        )
        assertEquals(
            expected = false,
            actual = ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = false)
                .showsQuickCard,
        )
        assertEquals(
            expected = false,
            actual = ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = true)
                .showsQuickCard,
        )
    }
}

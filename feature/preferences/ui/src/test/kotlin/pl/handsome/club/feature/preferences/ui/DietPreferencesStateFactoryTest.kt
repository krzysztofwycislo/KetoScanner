package pl.handsome.club.feature.preferences.ui

import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.DietRatingLimits
import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.api.model.DietPreset
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory
import pl.handsome.club.feature.preferences.ui.state.DietProfileOption
import pl.handsome.club.feature.preferences.ui.state.RatingGuideRowState
import pl.handsome.club.feature.preferences.ui.state.RatingGuideState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.handsome.club.core.designsystem.R as DesignR
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

class DietPreferencesStateFactoryTest {

    private val factory = DietPreferencesStateFactory(DietPreferencesResources(), FakeRatingLimits())

    @Test
    fun `when prototype is default then content is initial load`() {
        assertEquals(DietPreferencesContentState.InitialLoad, factory.createFor(Prototype.Default).content)
    }

    @Test
    fun `when only input is restored then content is still initial load`() {
        assertEquals(DietPreferencesContentState.InitialLoad, factory.createFor(Prototype(input = "25")).content)
    }

    @Test
    fun `when load error is set then content is error even if preferences were loaded`() {
        val state = factory.createFor(loaded(DietPreferences(40)).copy(loadError = AppError.Storage))

        assertEquals(
            DietPreferencesContentState.Error(UiText.Res(DesignR.string.designsystem_error_storage)),
            state.content,
        )
    }

    @Test
    fun `presets match only their own profile`() {
        assertEquals(DietPreset.STRICT_KETO, DietPreset.of(DietPreferences(20, DietMode.KETO)))
        assertEquals(DietPreset.KETO, DietPreset.of(DietPreferences(40, DietMode.KETO)))
        assertEquals(DietPreset.LOW_CARB, DietPreset.of(DietPreferences(100, DietMode.LOW_CARB)))
        assertNull(DietPreset.of(DietPreferences(40, DietMode.LOW_CARB)))
        assertNull(DietPreset.of(DietPreferences(35, DietMode.KETO)))
        assertEquals(DietProfileOption.CUSTOM, DietProfileOption.of(DietPreferences(40, DietMode.LOW_CARB)))
        assertEquals(DietProfileOption.CUSTOM, DietProfileOption.of(DietPreferences(35, DietMode.KETO)))
    }

    @Test
    fun `each preset option is selected for its stored profile and the others are not`() {
        mapOf(
            DietPreferences(20, DietMode.KETO) to DietProfileOption.STRICT_KETO,
            DietPreferences(40, DietMode.KETO) to DietProfileOption.KETO,
            DietPreferences(100, DietMode.LOW_CARB) to DietProfileOption.LOW_CARB,
        ).forEach { (preferences, expected) ->
            val options = form(loaded(preferences)).options

            assertEquals(listOf(expected), options.filter { it.isSelected }.map { it.option })
        }
    }

    @Test
    fun `when the stored profile matches no preset then custom is selected`() {
        val options = form(loaded(DietPreferences(35, DietMode.KETO))).options

        assertEquals(listOf(DietProfileOption.CUSTOM), options.filter { it.isSelected }.map { it.option })
    }

    @Test
    fun `options are listed in order with titles notes and preset amounts`() {
        val options = form(loaded(DietPreferences(40))).options

        assertEquals(DietProfileOption.entries, options.map { it.option })
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto), options[0].title)
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto_note), options[0].note)
        assertEquals(grams(20), options[0].trailing)
        assertEquals(grams(40), options[1].trailing)
        assertEquals(grams(100), options[2].trailing)
        assertEquals(UiText.Res(R.string.preferences_preset_custom), options[3].title)
    }

    @Test
    fun `custom trailing text is shown only for a valid number`() {
        val base = loaded(DietPreferences(40)).copy(option = DietProfileOption.CUSTOM)

        assertEquals(grams(35), form(base.copy(input = "35")).options.last().trailing)
        assertNull(form(base.copy(input = "")).options.last().trailing)
        assertNull(form(base.copy(input = "0")).options.last().trailing)
        assertNull(form(base.copy(input = "1000")).options.last().trailing)
    }

    @Test
    fun `custom fields are hidden for presets and shown for custom with the selected mode`() {
        assertNull(form(loaded(DietPreferences(40))).custom)

        val custom = assertNotNull(form(loaded(DietPreferences(80, DietMode.LOW_CARB))).custom)

        assertEquals(DietMode.entries, custom.modes.map { it.mode })
        assertEquals(DietMode.LOW_CARB, custom.modes.single { it.isSelected }.mode)
        assertEquals(UiText.Res(R.string.preferences_diet_mode_low_carb), custom.modes[1].label)
        assertEquals("80", custom.input)
        assertNull(custom.inputError)
    }

    @Test
    fun `input error is shown only when input is invalid`() {
        val custom = assertNotNull(
            form(loaded(DietPreferences(35)).copy(input = "0", isInputInvalid = true)).custom,
        )

        assertEquals(
            UiText.Res(R.string.preferences_input_error, UiArg.Numeric.Count(1), UiArg.Numeric.Count(999)),
            custom.inputError,
        )
    }

    @Test
    fun `rating guide for strict keto shows the carbs limits for 20 g`() {
        val guide = form(loaded(DietPreferences(20, DietMode.KETO))).ratingGuide

        assertEquals(
            RatingGuideState(
                subtitle = UiText.Res(R.string.preferences_rating_subtitle, UiArg.Numeric.Count(20)),
                rows = persistentListOf(
                    RatingGuideRowState(
                        rating = Rating.Positive,
                        label = UiText.Res(R.string.preferences_rating_good_keto),
                        value = UiText.Res(R.string.preferences_rating_up_to, UiArg.Numeric.Grams(20 / 3.0)),
                    ),
                    RatingGuideRowState(
                        rating = Rating.Neutral,
                        label = UiText.Res(R.string.preferences_rating_neutral),
                        value = UiText.Res(R.string.preferences_rating_up_to, UiArg.Numeric.Grams(10.0)),
                    ),
                    RatingGuideRowState(
                        rating = Rating.Negative,
                        label = UiText.Res(R.string.preferences_rating_not_advised_keto),
                        value = UiText.Res(R.string.preferences_rating_over, UiArg.Numeric.Grams(10.0)),
                    ),
                ),
                footnote = UiText.Res(R.string.preferences_rating_footnote_keto),
            ),
            guide,
        )
    }

    @Test
    fun `rating guide uses low carb labels for the low carb preset and for custom low carb`() {
        listOf(
            loaded(DietPreferences(100, DietMode.LOW_CARB)),
            loaded(DietPreferences(40)).copy(
                option = DietProfileOption.CUSTOM,
                input = "80",
                customMode = DietMode.LOW_CARB,
            ),
        ).forEach { prototype ->
            val guide = assertNotNull(form(prototype).ratingGuide)

            assertEquals(UiText.Res(R.string.preferences_rating_good_low_carb), guide.rows[0].label)
            assertEquals(UiText.Res(R.string.preferences_rating_not_advised_low_carb), guide.rows[2].label)
            assertEquals(UiText.Res(R.string.preferences_rating_footnote_low_carb), guide.footnote)
        }
    }

    @Test
    fun `rating guide follows the option selected in the form`() {
        val guide = form(loaded(DietPreferences(40)).copy(option = DietProfileOption.LOW_CARB)).ratingGuide

        assertEquals(
            UiText.Res(R.string.preferences_rating_subtitle, UiArg.Numeric.Count(100)),
            assertNotNull(guide).subtitle,
        )
    }

    @Test
    fun `rating guide is hidden for custom with an empty or zero input`() {
        val base = loaded(DietPreferences(40)).copy(option = DietProfileOption.CUSTOM)

        assertNull(form(base.copy(input = "")).ratingGuide)
        assertNull(form(base.copy(input = "0")).ratingGuide)
    }

    @Test
    fun `saving flag is passed through`() {
        assertTrue(form(loaded(DietPreferences(40)).copy(isSaving = true)).isSaving)
        assertFalse(form(loaded(DietPreferences(40))).isSaving)
    }

    private fun loaded(preferences: DietPreferences) = Prototype(
        savedPreferences = preferences,
        option = DietProfileOption.of(preferences),
        input = preferences.maxDailyCarbsAmount.toString(),
        customMode = preferences.dietMode,
    )

    private fun form(prototype: Prototype): DietPreferencesContentState.Form =
        assertIs<DietPreferencesContentState.Form>(factory.createFor(prototype).content)

    private fun grams(amount: Long) = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(amount))

    private class FakeRatingLimits : DietRatingLimits {
        override fun carbsPer100g(preferences: DietPreferences): CarbLimits =
            CarbLimits(preferences.maxDailyCarbsAmount / 3.0, preferences.maxDailyCarbsAmount / 2.0)
    }
}

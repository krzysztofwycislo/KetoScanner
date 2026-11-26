package app.carbpeek.feature.preferences.data

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DietPreferencesPersistenceImplTest {

    private val job = Job()
    private val directory: Path =
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "dietPreferences-${Random.nextLong().toULong()}"

    private lateinit var persistence: DietPreferencesPersistenceImpl

    @BeforeTest
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { directory / "test.preferences_pb" },
            ),
            scope = CoroutineScope(Dispatchers.IO + job),
        )
        persistence = DietPreferencesPersistenceImpl(dataStore)
    }

    @AfterTest
    fun tearDown() {
        job.cancel()
        FileSystem.SYSTEM.deleteRecursively(directory)
    }

    @Test
    fun `when store is empty then neither amount nor mode is observed`() = runTest {
        assertEquals(StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null), persistence.observe().first())
    }

    @Test
    fun `when preferences are saved then amount and mode are observed together`() = runTest {
        assertEquals(StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null), persistence.observe().first())

        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB", opensDetailsRightAway = false),
        )

        assertEquals(
            StoredDietPreferences(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB"),
            persistence.observe().first(),
        )
    }

    @Test
    fun `when store is empty then setup is not done`() = runTest {
        assertFalse(persistence.observe().first().isSetupDone)
    }

    @Test
    fun `when no diet is saved then amount and mode are observed together`() = runTest {
        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 40, dietMode = "NONE", opensDetailsRightAway = false),
        )

        assertEquals(
            StoredDietPreferences(maxDailyCarbsAmount = 40, dietMode = "NONE"),
            persistence.observe().first(),
        )
    }

    @Test
    fun `when preferences are saved without completing setup then setup is not done`() = runTest {
        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 25, dietMode = "KETO", opensDetailsRightAway = false),
        )

        assertFalse(persistence.observe().first().isSetupDone)
    }

    @Test
    fun `when setup is completed then amount, mode and setup done are observed together`() = runTest {
        persistence.completeSetup(maxDailyCarbsAmount = 20, dietMode = "KETO")

        assertEquals(
            StoredDietPreferences(
                maxDailyCarbsAmount = 20,
                dietMode = "KETO",
                isSetupDone = true,
                pendingTips = setOf(OnboardingTip.FIRST_SCAN_HINT, OnboardingTip.VERDICT_TIP),
            ),
            persistence.observe().first(),
        )
    }

    @Test
    fun `when store is empty then the details setting is off`() = runTest {
        assertFalse(persistence.observe().first().opensDetailsRightAway)
    }

    @Test
    fun `when the details setting is saved on and off then it is observed with amount and mode`() = runTest {
        listOf(true, false).forEach { isOn ->
            persistence.save(
                StoredProfileSettings(
                    maxDailyCarbsAmount = 100,
                    dietMode = "LOW_CARB",
                    opensDetailsRightAway = isOn,
                ),
            )

            assertEquals(
                StoredDietPreferences(
                    maxDailyCarbsAmount = 100,
                    dietMode = "LOW_CARB",
                    opensDetailsRightAway = isOn,
                ),
                persistence.observe().first(),
            )
        }
    }

    @Test
    fun `when setup is completed and a tip is marked shown then the details setting stays on`() = runTest {
        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB", opensDetailsRightAway = true),
        )

        persistence.completeSetup(maxDailyCarbsAmount = 20, dietMode = "KETO")
        assertTrue(persistence.observe().first().opensDetailsRightAway)

        persistence.markTipShown(tip = OnboardingTip.FIRST_SCAN_HINT)
        assertTrue(persistence.observe().first().opensDetailsRightAway)
    }

    @Test
    fun `when no diet is saved with the details setting on then it stays on`() = runTest {
        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 40, dietMode = "NONE", opensDetailsRightAway = true),
        )

        assertEquals(
            StoredDietPreferences(maxDailyCarbsAmount = 40, dietMode = "NONE", opensDetailsRightAway = true),
            persistence.observe().first(),
        )
    }

    @Test
    fun `when store is empty then no tips are pending`() = runTest {
        assertEquals(emptySet(), persistence.observe().first().pendingTips)
    }

    @Test
    fun `when setup is completed then both tips are pending`() = runTest {
        persistence.completeSetup(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB")

        assertEquals(
            setOf(OnboardingTip.FIRST_SCAN_HINT, OnboardingTip.VERDICT_TIP),
            persistence.observe().first().pendingTips,
        )
    }

    @Test
    fun `when the first scan hint is marked shown then only the verdict tip stays pending`() = runTest {
        persistence.completeSetup(maxDailyCarbsAmount = 20, dietMode = "KETO")

        persistence.markTipShown(tip = OnboardingTip.FIRST_SCAN_HINT)

        assertEquals(setOf(OnboardingTip.VERDICT_TIP), persistence.observe().first().pendingTips)
    }

    @Test
    fun `when preferences are saved without completing setup then no tips are pending`() = runTest {
        persistence.save(
            StoredProfileSettings(maxDailyCarbsAmount = 25, dietMode = "KETO", opensDetailsRightAway = false),
        )

        assertEquals(emptySet(), persistence.observe().first().pendingTips)
    }
}

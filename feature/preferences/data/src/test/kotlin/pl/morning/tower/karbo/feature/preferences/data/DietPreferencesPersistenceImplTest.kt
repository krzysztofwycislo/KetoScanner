package pl.morning.tower.karbo.feature.preferences.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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

class DietPreferencesPersistenceImplTest {

    private val job = Job()
    private val directory: Path =
        FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "dietPreferences-${Random.nextLong().toULong()}"

    private lateinit var persistence: DietPreferencesPersistenceImpl

    @BeforeTest
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(Dispatchers.IO + job),
            produceFile = { directory / "test.preferences_pb" },
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

        persistence.save(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB")

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
    fun `when preferences are saved without completing setup then setup is not done`() = runTest {
        persistence.save(maxDailyCarbsAmount = 25, dietMode = "KETO")

        assertFalse(persistence.observe().first().isSetupDone)
    }

    @Test
    fun `when setup is completed then amount, mode and setup done are observed together`() = runTest {
        persistence.completeSetup(maxDailyCarbsAmount = 20, dietMode = "KETO")

        assertEquals(
            StoredDietPreferences(maxDailyCarbsAmount = 20, dietMode = "KETO", isSetupDone = true),
            persistence.observe().first(),
        )
    }
}

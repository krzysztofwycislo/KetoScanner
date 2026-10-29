package pl.handsome.club.feature.preferences.data

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
import kotlin.test.assertNull

class DietPreferencesPersistenceImplTest {

    private val job = Job()
    private val directory: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "dietPreferences-${Random.nextLong().toULong()}"

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
    fun `when store is empty then no amount is observed`() = runTest {
        assertNull(persistence.observeMaxDailyCarbsAmount().first())
    }

    @Test
    fun `when amount is set then it is observed`() = runTest {
        persistence.setMaxDailyCarbsAmount(25)

        assertEquals(25, persistence.observeMaxDailyCarbsAmount().first())
    }

    @Test
    fun `when amount is set after an empty read then the new amount is observed`() = runTest {
        assertNull(persistence.observeMaxDailyCarbsAmount().first())

        persistence.setMaxDailyCarbsAmount(55)

        assertEquals(55, persistence.observeMaxDailyCarbsAmount().first())
    }
}

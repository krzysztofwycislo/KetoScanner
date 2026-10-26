package pl.handsome.club.ketoscanner.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import java.io.File


class DataStoreDietPreferencesRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val job = Job()

    private lateinit var repository: DataStoreDietPreferencesRepository


    @Before
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + job),
            produceFile = { File(tmpFolder.root, "test.preferences_pb") }
        )
        repository = DataStoreDietPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        job.cancel()
    }


    @Test
    fun `when store is empty then default max carbs amount is returned`() = runTest {
        assertThat(repository.getDietPreferences()).isEqualTo(DietPreferences(40))
    }

    @Test
    fun `when max carbs amount is set then it is returned`() = runTest {
        repository.setMaxDailyCarbsAmount(25)

        assertThat(repository.getDietPreferences()).isEqualTo(DietPreferences(25))
    }

    @Test
    fun `when max carbs amount is set then observed preferences reflect it`() = runTest {
        repository.setMaxDailyCarbsAmount(55)

        assertThat(repository.observeDietPreferences().first()).isEqualTo(DietPreferences(55))
    }

}

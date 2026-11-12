package pl.morning.tower.karbo.feature.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.morning.tower.karbo.core.datastore.MAX_CARBS_AMOUNT_KEY_NAME

internal data class StoredDietPreferences(
    val maxDailyCarbsAmount: Int?,
    val dietMode: String?,
    val isSetupDone: Boolean = false,
)

internal interface DietPreferencesPersistence {
    fun observe(): Flow<StoredDietPreferences>
    suspend fun save(maxDailyCarbsAmount: Int, dietMode: String)
    suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String)
}

internal class DietPreferencesPersistenceImpl(
    private val dataStore: DataStore<Preferences>,
) : DietPreferencesPersistence {

    override fun observe(): Flow<StoredDietPreferences> = dataStore.data.map {
        StoredDietPreferences(
            maxDailyCarbsAmount = it[MAX_CARBS_AMOUNT],
            dietMode = it[DIET_MODE],
            isSetupDone = it[SETUP_DONE] == true,
        )
    }

    override suspend fun save(maxDailyCarbsAmount: Int, dietMode: String) {
        dataStore.edit {
            it[MAX_CARBS_AMOUNT] = maxDailyCarbsAmount
            it[DIET_MODE] = dietMode
        }
    }

    override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
        dataStore.edit {
            it[MAX_CARBS_AMOUNT] = maxDailyCarbsAmount
            it[DIET_MODE] = dietMode
            it[SETUP_DONE] = true
        }
    }

    private companion object {
        val MAX_CARBS_AMOUNT = intPreferencesKey(MAX_CARBS_AMOUNT_KEY_NAME)
        val DIET_MODE = stringPreferencesKey("dietMode")
        val SETUP_DONE = booleanPreferencesKey("setupDone")
    }
}

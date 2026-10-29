package pl.handsome.club.feature.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.handsome.club.core.datastore.MAX_CARBS_AMOUNT_KEY_NAME

internal data class StoredDietPreferences(val maxDailyCarbsAmount: Int?, val dietMode: String?)

internal interface DietPreferencesPersistence {
    fun observe(): Flow<StoredDietPreferences>
    suspend fun setMaxDailyCarbsAmount(amount: Int)
    suspend fun setDietMode(name: String)
}

internal class DietPreferencesPersistenceImpl(
    private val dataStore: DataStore<Preferences>,
) : DietPreferencesPersistence {

    override fun observe(): Flow<StoredDietPreferences> = dataStore.data.map {
        StoredDietPreferences(maxDailyCarbsAmount = it[MAX_CARBS_AMOUNT], dietMode = it[DIET_MODE])
    }

    override suspend fun setMaxDailyCarbsAmount(amount: Int) {
        dataStore.edit { it[MAX_CARBS_AMOUNT] = amount }
    }

    override suspend fun setDietMode(name: String) {
        dataStore.edit { it[DIET_MODE] = name }
    }

    private companion object {
        val MAX_CARBS_AMOUNT = intPreferencesKey(MAX_CARBS_AMOUNT_KEY_NAME)
        val DIET_MODE = stringPreferencesKey("dietMode")
    }
}

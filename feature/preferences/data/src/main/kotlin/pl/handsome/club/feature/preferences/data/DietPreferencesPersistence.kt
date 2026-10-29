package pl.handsome.club.feature.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.handsome.club.core.datastore.MAX_CARBS_AMOUNT_KEY_NAME

internal interface DietPreferencesPersistence {
    fun observeMaxDailyCarbsAmount(): Flow<Int?>
    suspend fun setMaxDailyCarbsAmount(amount: Int)
}

internal class DietPreferencesPersistenceImpl(
    private val dataStore: DataStore<Preferences>,
) : DietPreferencesPersistence {

    override fun observeMaxDailyCarbsAmount(): Flow<Int?> = dataStore.data.map { it[MAX_CARBS_AMOUNT] }

    override suspend fun setMaxDailyCarbsAmount(amount: Int) {
        dataStore.edit { it[MAX_CARBS_AMOUNT] = amount }
    }

    private companion object {
        val MAX_CARBS_AMOUNT = intPreferencesKey(MAX_CARBS_AMOUNT_KEY_NAME)
    }
}

package pl.handsome.club.ketoscanner.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import java.io.IOException


class DataStoreDietPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : DietPreferencesRepository {

    override fun observeDietPreferences(): Flow<DietPreferences> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { DietPreferences(it[MAX_CARBS_AMOUNT] ?: DEFAULT_MAX_CARBS_AMOUNT) }
        .distinctUntilChanged()

    override suspend fun getDietPreferences(): DietPreferences = observeDietPreferences().first()

    override suspend fun setMaxDailyCarbsAmount(amount: Int) {
        dataStore.edit { it[MAX_CARBS_AMOUNT] = amount }
    }


    companion object {

        val MAX_CARBS_AMOUNT = intPreferencesKey(MAX_CARBS_AMOUNT_KEY_NAME)

        const val DEFAULT_MAX_CARBS_AMOUNT = 40

    }

}

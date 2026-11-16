package app.carbpeek.feature.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.carbpeek.core.datastore.MAX_CARBS_AMOUNT_KEY_NAME
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal data class StoredDietPreferences(
    val maxDailyCarbsAmount: Int?,
    val dietMode: String?,
    val isSetupDone: Boolean = false,
    val pendingTips: Set<OnboardingTip> = emptySet(),
)

internal interface DietPreferencesPersistence {
    fun observe(): Flow<StoredDietPreferences>
    suspend fun save(maxDailyCarbsAmount: Int, dietMode: String)
    suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String)
    suspend fun markTipShown(tip: OnboardingTip)
}

internal class DietPreferencesPersistenceImpl(
    private val dataStore: DataStore<Preferences>,
) : DietPreferencesPersistence {

    override fun observe(): Flow<StoredDietPreferences> = dataStore.data.map { preferences ->
        StoredDietPreferences(
            maxDailyCarbsAmount = preferences[MAX_CARBS_AMOUNT],
            dietMode = preferences[DIET_MODE],
            isSetupDone = preferences[SETUP_DONE] == true,
            pendingTips = OnboardingTip.entries.filter { preferences[it.pendingKey()] == true }.toSet(),
        )
    }

    override suspend fun save(maxDailyCarbsAmount: Int, dietMode: String) {
        dataStore.edit {
            it[MAX_CARBS_AMOUNT] = maxDailyCarbsAmount
            it[DIET_MODE] = dietMode
        }
    }

    override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
        dataStore.edit { preferences ->
            preferences[MAX_CARBS_AMOUNT] = maxDailyCarbsAmount
            preferences[DIET_MODE] = dietMode
            preferences[SETUP_DONE] = true
            OnboardingTip.entries.forEach { preferences[it.pendingKey()] = true }
        }
    }

    override suspend fun markTipShown(tip: OnboardingTip) {
        dataStore.edit {
            it[tip.pendingKey()] = false
        }
    }

    private companion object {
        val MAX_CARBS_AMOUNT = intPreferencesKey(MAX_CARBS_AMOUNT_KEY_NAME)
        val DIET_MODE = stringPreferencesKey("dietMode")
        val SETUP_DONE = booleanPreferencesKey("setupDone")
        val FIRST_SCAN_HINT_PENDING = booleanPreferencesKey("firstScanHintPending")
        val VERDICT_TIP_PENDING = booleanPreferencesKey("verdictTipPending")

        fun OnboardingTip.pendingKey(): Preferences.Key<Boolean> = when (this) {
            OnboardingTip.FIRST_SCAN_HINT -> FIRST_SCAN_HINT_PENDING
            OnboardingTip.VERDICT_TIP -> VERDICT_TIP_PENDING
        }
    }
}

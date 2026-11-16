package app.carbpeek.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

internal const val LEGACY_SHARED_PREFERENCES_NAME = "KetoSPreferences"
const val MAX_CARBS_AMOUNT_KEY_NAME = "carbAmount"
internal const val DATA_STORE_NAME = "keto_preferences"

internal fun createDietPreferencesDataStore(context: Context): DataStore<Preferences> {
    val appContext = context.applicationContext
    return PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration(
                appContext,
                LEGACY_SHARED_PREFERENCES_NAME,
                keysToMigrate = setOf(MAX_CARBS_AMOUNT_KEY_NAME)
            )
        ),
        produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_NAME) }
    )
}

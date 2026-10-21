package pl.handsome.club.ketoscanner.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile


const val LEGACY_SHARED_PREFERENCES_NAME = "KetoSPreferences"
const val MAX_CARBS_AMOUNT_KEY_NAME = "carbAmount"
const val DATA_STORE_NAME = "keto_preferences"

/**
 * Must be called only once per process (Koin `single`): DataStore fails when two instances use the same file.
 * Migrates the max carbs amount from the legacy SharedPreferences file on first access.
 */
fun createDietPreferencesDataStore(context: Context): DataStore<Preferences> {
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

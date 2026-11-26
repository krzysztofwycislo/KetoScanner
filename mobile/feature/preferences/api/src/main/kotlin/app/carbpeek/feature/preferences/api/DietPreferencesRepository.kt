package app.carbpeek.feature.preferences.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.coroutines.flow.Flow


interface DietPreferencesRepository {

    fun observeDietPreferences(): Flow<DietPreferences>

    suspend fun getDietPreferences(): Outcome<DietPreferences>

}

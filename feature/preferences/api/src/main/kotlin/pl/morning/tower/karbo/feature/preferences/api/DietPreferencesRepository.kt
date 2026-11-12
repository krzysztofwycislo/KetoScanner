package pl.morning.tower.karbo.feature.preferences.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences


interface DietPreferencesRepository {

    fun observeDietPreferences(): Flow<DietPreferences>

    suspend fun getDietPreferences(): Outcome<DietPreferences>

    suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit>

}

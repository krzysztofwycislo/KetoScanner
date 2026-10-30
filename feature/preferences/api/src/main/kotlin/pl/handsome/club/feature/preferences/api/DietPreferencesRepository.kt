package pl.handsome.club.feature.preferences.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.preferences.api.model.DietPreferences


interface DietPreferencesRepository {

    fun observeDietPreferences(): Flow<Outcome<DietPreferences>>

    suspend fun getDietPreferences(): Outcome<DietPreferences>

    suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit>

}

package pl.handsome.club.feature.preferences.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.feature.preferences.api.model.DietPreferences


interface DietPreferencesRepository {

    fun observeDietPreferences(): Flow<DietPreferences>

    suspend fun getDietPreferences(): DietPreferences

    suspend fun setMaxDailyCarbsAmount(amount: Int)

}

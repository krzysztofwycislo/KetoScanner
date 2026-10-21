package pl.handsome.club.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.domain.preferences.DietPreferences


interface DietPreferencesRepository {

    fun observeDietPreferences(): Flow<DietPreferences>

    suspend fun getDietPreferences(): DietPreferences

    suspend fun setMaxDailyCarbsAmount(amount: Int)

}

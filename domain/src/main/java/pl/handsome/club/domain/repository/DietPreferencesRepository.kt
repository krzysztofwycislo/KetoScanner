package pl.handsome.club.domain.repository

import pl.handsome.club.domain.preferences.DietPreferences


interface DietPreferencesRepository {

    suspend fun getDietPreferences() : DietPreferences

    suspend fun setDietPreferences(dietPreferences: DietPreferences)

}
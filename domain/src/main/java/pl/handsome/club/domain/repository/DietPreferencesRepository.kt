package pl.handsome.club.domain.repository

import pl.handsome.club.domain.preferences.DietPreferences


interface DietPreferencesRepository {

    fun getDietPreferences() : DietPreferences

}
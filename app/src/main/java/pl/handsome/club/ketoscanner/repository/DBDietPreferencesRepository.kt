package pl.handsome.club.ketoscanner.repository

import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.repository.DietPreferencesRepository


class DBDietPreferencesRepository : DietPreferencesRepository {

    private var dietPreferences: DietPreferences = initializeDietPreferences()


    // TODO load from db
    // TODO in case of lack preferences in db should be default value?
    private fun initializeDietPreferences(): DietPreferences {
        return TEMPORARY_DIET_PREFERENCES
    }

    override fun getDietPreferences(): DietPreferences = dietPreferences

    override suspend fun setDietPreferences(dietPreferences: DietPreferences) {
        this.dietPreferences = dietPreferences
    }

    // TODO TO REMOVE
    companion object {
        private val TEMPORARY_DIET_PREFERENCES = DietPreferences(2000, null, null)
    }

}
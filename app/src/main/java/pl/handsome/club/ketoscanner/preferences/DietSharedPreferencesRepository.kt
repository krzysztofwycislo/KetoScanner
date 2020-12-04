package pl.handsome.club.ketoscanner.preferences

import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.repository.DietPreferencesRepository


class DietSharedPreferencesRepository(
    private val preferenceMediator: SharedPreferenceMediator
) : DietPreferencesRepository {

    override fun getDietPreferences(): DietPreferences = DietPreferences(
        preferenceMediator.maxCarbsAmount()
    )

}
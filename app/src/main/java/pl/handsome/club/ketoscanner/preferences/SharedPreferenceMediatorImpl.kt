package pl.handsome.club.ketoscanner.preferences

import android.content.Context
import android.content.SharedPreferences
import pl.handsome.club.ketoscanner.preferences.SharedPreferenceMediator.Companion.DEFAULT_PREFERENCES_KEY
import pl.handsome.club.ketoscanner.preferences.SharedPreferenceMediator.Companion.MAX_CARBS_AMOUNT

class SharedPreferenceMediatorImpl(context: Context) : SharedPreferenceMediator {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        DEFAULT_PREFERENCES_KEY,
        Context.MODE_PRIVATE
    )

    override fun maxCarbsAmount(): Int = sharedPreferences.getInt(MAX_CARBS_AMOUNT, 40)

    override fun setMaxCarbsAmount(newValue: Int) {
        sharedPreferences.edit().putInt(MAX_CARBS_AMOUNT, newValue).apply()
    }

}
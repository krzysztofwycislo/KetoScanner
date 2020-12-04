package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import org.koin.android.ext.android.inject
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.preferences.SharedPreferenceMediator


class MacronutrientPreferencesFragment : PreferenceFragmentCompat() {

    private val sharedPreferenceMediator: SharedPreferenceMediator by inject()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initializeIsOnKetoPreference()
        initializeMaxCarbsAmountPreference()

        findPreference<PreferenceCategory>("categoryDiet")?.isIconSpaceReserved = false
    }

    private fun initializeMaxCarbsAmountPreference() {
        val savedCarbAmountPreference = sharedPreferenceMediator.maxCarbsAmount()
        findPreference<EditTextPreference>("maxCarbAmount")?.apply {
            summary = getString(R.string.preferences_max_carb_amount_summary, savedCarbAmountPreference)
            isIconSpaceReserved = false

            setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_NUMBER }

            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? String)?.toInt()
                    ?.also(sharedPreferenceMediator::setMaxCarbsAmount)
                    ?.also { summary = getString(R.string.preferences_max_carb_amount_summary, it) }
                true
            }
        }
    }

    private fun initializeIsOnKetoPreference() {
        val savedIsOnKetoDietPreference = sharedPreferenceMediator.isOnKeto()
        findPreference<SwitchPreference>("isOnKeto")?.apply {
            isIconSpaceReserved = false
            isChecked = savedIsOnKetoDietPreference
            setOnPreferenceChangeListener { _, newValue ->
                (newValue as? Boolean)
                    ?.also(sharedPreferenceMediator::setIsOnKeto)
                true
            }
        }
    }

}
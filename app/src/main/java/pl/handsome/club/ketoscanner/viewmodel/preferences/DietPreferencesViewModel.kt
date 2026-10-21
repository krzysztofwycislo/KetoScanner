package pl.handsome.club.ketoscanner.viewmodel.preferences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pl.handsome.club.domain.repository.DietPreferencesRepository


class DietPreferencesViewModel(
    private val dietPreferencesRepository: DietPreferencesRepository
) : ViewModel() {

    private val maxCarbsAmount = dietPreferencesRepository.observeDietPreferences()
        .map { it.maxDailyCarbsAmount }
        .asLiveData()

    private val invalidInput = MutableLiveData(false)


    fun getMaxCarbsAmount(): LiveData<Int> = maxCarbsAmount

    fun getInvalidInput(): LiveData<Boolean> = invalidInput

    fun saveMaxCarbsAmount(input: String) {
        val value = input.trim().toIntOrNull()
        if (value == null || value !in MIN_MAX_CARBS_AMOUNT..MAX_MAX_CARBS_AMOUNT) {
            invalidInput.value = true
            return
        }

        invalidInput.value = false
        viewModelScope.launch { dietPreferencesRepository.setMaxDailyCarbsAmount(value) }
    }


    companion object {

        const val MIN_MAX_CARBS_AMOUNT = 1

        const val MAX_MAX_CARBS_AMOUNT = 999

    }

}

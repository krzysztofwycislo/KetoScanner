package pl.handsome.club.ketoscanner.viewmodel.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.domain.repository.AnalysisHistoryRepository


class AnalysisHistoryViewModel(
    private val analysisHistoryRepository: AnalysisHistoryRepository
) : ViewModel() {

    private val lastEntries = MutableLiveData<List<ProductAnalysisHistoryEntry>>()


    fun getLastUniqueEntries(): LiveData<List<ProductAnalysisHistoryEntry>> = lastEntries

    fun loadLastUniqueEntries(amount: Int) {
        viewModelScope.launch {
            analysisHistoryRepository.getLastUniqueEntries(amount)
                .also { lastEntries.postValue(it) }
        }
    }

}
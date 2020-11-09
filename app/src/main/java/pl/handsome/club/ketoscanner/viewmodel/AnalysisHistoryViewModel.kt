package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.ViewModel
import pl.handsome.club.domain.repository.AnalysisHistoryRepository


class AnalysisHistoryViewModel(
    private val analysisHistoryRepository: AnalysisHistoryRepository
) : ViewModel() {


    fun getLastEntries(amount: Int) = analysisHistoryRepository.getLastEntries(amount)


}
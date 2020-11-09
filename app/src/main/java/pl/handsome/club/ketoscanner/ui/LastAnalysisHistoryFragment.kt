package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.last_analysis_history_fragment.*
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.ui.adapter.AnalysisHistoryListAdapter
import pl.handsome.club.ketoscanner.viewmodel.AnalysisHistoryViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory


class LastAnalysisHistoryFragment : Fragment(R.layout.last_analysis_history_fragment) {


    private val historyViewModel by viewModels<AnalysisHistoryViewModel> { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         initializeList()
    }

    private fun initializeList() {
        val lastHistoryEntries = historyViewModel.getLastEntries(HISTORY_ENTRIES_AMOUNT)
        lastAnalysisList.adapter = AnalysisHistoryListAdapter(requireContext(), lastHistoryEntries)
    }

    companion object {
        private const val HISTORY_ENTRIES_AMOUNT = 10
    }

}
package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.last_analysis_history_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.ui.adapter.AnalysisHistoryListAdapter
import pl.handsome.club.ketoscanner.viewmodel.AnalysisHistoryViewModel


class LastAnalysisHistoryFragment : Fragment(R.layout.last_analysis_history_fragment) {

    private val historyViewModel: AnalysisHistoryViewModel by viewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyViewModel.loadLastUniqueEntries(HISTORY_ENTRIES_AMOUNT)
        historyViewModel.getLastUniqueEntries().observe(viewLifecycleOwner, ::initializeList)
    }

    private fun initializeList(lastEntries: List<ProductAnalysisHistoryEntry>) {
        lastAnalysisList.adapter = AnalysisHistoryListAdapter(lastEntries)
    }

    companion object {
        private const val HISTORY_ENTRIES_AMOUNT = 10
    }

}
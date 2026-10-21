package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.analyze.history.ProductAnalysisHistoryEntry
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.LastAnalysisHistoryFragmentBinding
import pl.handsome.club.ketoscanner.ui.adapter.AnalysisHistoryListAdapter
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.util.logWarning
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalysisHistoryViewModel
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel


class LastAnalysisHistoryFragment : Fragment(R.layout.last_analysis_history_fragment) {

    private val historyViewModel: AnalysisHistoryViewModel by viewModel()
    private val analyzeProductViewModel: AnalyzeProductViewModel by activityViewModel()

    private var _binding: LastAnalysisHistoryFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = LastAnalysisHistoryFragmentBinding.bind(view)
        historyViewModel.loadLastUniqueEntries(HISTORY_ENTRIES_AMOUNT)
        historyViewModel.getLastUniqueEntries().observe(viewLifecycleOwner, ::initializeList)
    }

    private fun initializeList(lastEntries: List<ProductAnalysisHistoryEntry>) {
        binding.lastAnalysisList.adapter = AnalysisHistoryListAdapter(lastEntries, ::onHistoryEntryClicked)
    }

    private fun onHistoryEntryClicked(productAnalysisResults: ProductAnalysisHistoryEntry) {
        val barcode = productAnalysisResults.productBarcode

        with(analyzeProductViewModel) {
            searchAndAnalyzeProduct(barcode)
            getProductAnalysisState().observe(viewLifecycleOwner, ::onProductSearchStateChanged)
        }
    }

    private fun onProductSearchStateChanged(productAnalysisState: ProductAnalysisState?) {
        when (productAnalysisState) {
            is ProductAnalysisState.InProgress -> {/* TODO progressbar? */ }
            is ProductAnalysisState.Success -> navigateToAnalyzeResult()
            is ProductAnalysisState.Error -> showError(productAnalysisState.throwable)
            else -> logWarning("Unhandled analysis state: $productAnalysisState")
        }
    }

    private fun navigateToAnalyzeResult() {
        HomeFragmentDirections
            .toProductAnalysisResultFragment()
            .let(::safeNavigateTo)
    }

    private fun showError(throwable: Throwable) {
        logException(throwable)
        Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val HISTORY_ENTRIES_AMOUNT = 10
    }

}
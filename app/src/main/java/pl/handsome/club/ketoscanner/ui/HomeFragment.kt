package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import pl.handsome.club.core.designsystem.event.AppEvent.ShowMessage
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.history.ui.HistoryScreen
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.databinding.HomeFragmentBinding
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.analyze.ProductAnalysisState


class HomeFragment : Fragment(R.layout.home_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by activityViewModel()
    private val appEvents: AppEvents by inject()

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private var pendingBarcode: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = HomeFragmentBinding.bind(view)

        pendingBarcode = savedInstanceState?.getString(KEY_PENDING_BARCODE) ?: pendingBarcode
        if (analyzeProductViewModel.getProductAnalysisState().value == null) pendingBarcode = null

        binding.homeMenuButton.setOnClickListener {
            showMenu(it)
        }

        binding.historyComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    // MainActivity already pads its root by the system bars, so the hosted screen must not apply them again.
                    Box(Modifier.consumeWindowInsets(WindowInsets.safeDrawing)) {
                        HistoryScreen(::onHistoryNavigation)
                    }
                }
            }
        }

        analyzeProductViewModel.getProductAnalysisState()
            .observe(viewLifecycleOwner, ::onProductAnalysisStateChanged)
    }

    private fun onHistoryNavigation(navigation: HistoryNavigation) {
        when (navigation) {
            is HistoryNavigation.NavigateToProduct -> {
                if (isLookupRunning()) return
                pendingBarcode = navigation.barcode
                analyzeProductViewModel.searchAndAnalyzeProduct(navigation.barcode)
            }
        }
    }

    private fun isLookupRunning(): Boolean =
        pendingBarcode != null ||
            analyzeProductViewModel.getProductAnalysisState().value is ProductAnalysisState.InProgress

    private fun onProductAnalysisStateChanged(state: ProductAnalysisState) {
        val requestedBarcode = pendingBarcode ?: return
        when (state) {
            is ProductAnalysisState.InProgress -> Unit
            is ProductAnalysisState.Success -> {
                pendingBarcode = null
                if (state.result.product.barcode == requestedBarcode) {
                    safeNavigateTo(HomeFragmentDirections.toProductAnalysisResultFragment())
                }
            }
            is ProductAnalysisState.ProductNotFound -> {
                pendingBarcode = null
                appEvents send ShowMessage(UiText.Res(R.string.product_not_found))
            }
            is ProductAnalysisState.Error -> {
                pendingBarcode = null
                logException(state.throwable)
                appEvents send ShowMessage(UiText.Res(R.string.something_went_wrong))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_BARCODE, pendingBarcode)
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.home_menu, popup.menu)
        popup.setOnMenuItemClickListener {
            onMenuOptionChoose(it.itemId)
            true
        }
        popup.show()
    }

    private fun onMenuOptionChoose(itemId: Int) {
        when (itemId) {
            R.id.homeMenuDietPreferences ->
                safeNavigateTo(HomeFragmentDirections.toMacronutrientPreferencesFragment())

            R.id.homeMenuAbout ->
                safeNavigateTo(HomeFragmentDirections.toAboutFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val KEY_PENDING_BARCODE = "pendingBarcode"
    }

}

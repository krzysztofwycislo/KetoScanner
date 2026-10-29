package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import pl.handsome.club.feature.scanner.ui.ScanScreen
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.util.setHostedScreen


class SearchProductFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { ScanScreen(::onScanNavigation) }
        }

    private fun onScanNavigation(navigation: ScanNavigation) {
        when (navigation) {
            is ScanNavigation.NavigateToProduct ->
                safeNavigateTo(SearchProductFragmentDirections.toProductAnalysisResultFragment(navigation.barcode))
        }
    }

}

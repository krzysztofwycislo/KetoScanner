package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import pl.handsome.club.feature.history.ui.HistoryScreen
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.util.setHostedScreen


class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { HistoryScreen(::onHistoryNavigation) }
        }

    private fun onHistoryNavigation(navigation: HistoryNavigation) {
        when (navigation) {
            is HistoryNavigation.NavigateToProduct ->
                safeNavigateTo(HomeFragmentDirections.toProductAnalysisResultFragment(navigation.barcode))

            HistoryNavigation.NavigateToDietPreferences ->
                safeNavigateTo(HomeFragmentDirections.toMacronutrientPreferencesFragment())

            HistoryNavigation.NavigateToAbout ->
                safeNavigateTo(HomeFragmentDirections.toAboutFragment())
        }
    }

}

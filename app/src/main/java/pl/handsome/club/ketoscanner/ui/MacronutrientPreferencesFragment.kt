package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pl.handsome.club.feature.preferences.ui.DietPreferencesScreen
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import pl.handsome.club.ketoscanner.util.setHostedScreen


class MacronutrientPreferencesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { DietPreferencesScreen(::onDietPreferencesNavigation) }
        }

    private fun onDietPreferencesNavigation(navigation: DietPreferencesNavigation) {
        when (navigation) {
            DietPreferencesNavigation.NavigateBack -> findNavController().navigateUp()
        }
    }

}

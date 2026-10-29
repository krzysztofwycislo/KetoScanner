package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pl.handsome.club.feature.about.ui.AboutScreen
import pl.handsome.club.feature.about.ui.event.AboutEvent.AboutNavigation
import pl.handsome.club.ketoscanner.util.setHostedScreen


class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { AboutScreen(::onAboutNavigation) }
        }

    private fun onAboutNavigation(navigation: AboutNavigation) {
        when (navigation) {
            AboutNavigation.NavigateBack -> findNavController().navigateUp()
        }
    }

}

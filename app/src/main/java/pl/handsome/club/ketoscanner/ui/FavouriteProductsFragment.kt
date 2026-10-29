package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import pl.handsome.club.feature.favourites.ui.FavouritesScreen
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.util.setHostedScreen


class FavouriteProductsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { FavouritesScreen(::onFavouritesNavigation) }
        }

    private fun onFavouritesNavigation(navigation: FavouritesNavigation) {
        when (navigation) {
            is FavouritesNavigation.NavigateToProduct ->
                safeNavigateTo(FavouriteProductsFragmentDirections.toProductAnalysisResultFragment(navigation.barcode))
        }
    }

}

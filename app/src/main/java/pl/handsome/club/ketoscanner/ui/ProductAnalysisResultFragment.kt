package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pl.handsome.club.feature.product.ui.ProductScreen
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.handsome.club.ketoscanner.util.setHostedScreen


class ProductAnalysisResultFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setHostedScreen { ProductScreen(::onProductNavigation) }
        }

    private fun onProductNavigation(navigation: ProductNavigation) {
        when (navigation) {
            ProductNavigation.NavigateBack -> findNavController().navigateUp()
        }
    }

}

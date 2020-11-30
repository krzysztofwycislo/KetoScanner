package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.favorite_products_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.ui.adapter.FavouriteProductsListAdapter
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.util.logWarning
import pl.handsome.club.ketoscanner.util.safeNavigateTo
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel


class FavouriteProductsFragment : Fragment(R.layout.favorite_products_fragment) {

    private val favouriteProductsListViewModel: FavouriteProductsListViewModel by viewModel()
    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFavouriteProductsList()
    }

    private fun initializeFavouriteProductsList() {
        val adapter = FavouriteProductsListAdapter(::onProductClicked)

        favouriteProductsListViewModel.getFavouriteProducts()
            .observe(viewLifecycleOwner, { adapter.submitList(it) })

        favouriteProductsList.adapter = adapter
    }

    private fun onProductClicked(favouriteProduct: FavouriteProduct) {
        val barcode = favouriteProduct.productBarcode

        with(analyzeProductViewModel) {
            searchAndAnalyzeProduct(barcode)
            getProductAnalysisState().observe(viewLifecycleOwner, ::onProductSearchStateChanged)
        }
    }

    private fun onProductSearchStateChanged(productAnalysisState: ProductAnalysisState?) {
        if (productAnalysisState !is ProductAnalysisState.InProgress) {
            progressBar.hide()
        }

        when(productAnalysisState) {
            is ProductAnalysisState.InProgress -> { progressBar.show() }
            is ProductAnalysisState.Success -> navigateToAnalyzeResult()
            is ProductAnalysisState.Error -> showError(productAnalysisState.throwable)
            else -> logWarning("Unhandled analysis state: $productAnalysisState")
        }
    }

    private fun navigateToAnalyzeResult() {
        FavouriteProductsFragmentDirections
            .toProductAnalysisResultFragment()
            .let(::safeNavigateTo)
    }

    private fun showError(throwable: Throwable) {
        logException(throwable)
        Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show()
    }

}
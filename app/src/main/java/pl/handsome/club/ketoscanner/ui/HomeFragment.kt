package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.home_fragment.*
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.getNotEmptyString
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.product.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory
import pl.handsome.club.domain.product.SearchProduct
import pl.handsome.club.ketoscanner.ui.parcelable.ProductParcelable
import pl.handsome.club.ketoscanner.util.logException


class HomeFragment : Fragment(R.layout.home_fragment) {

    private val searchProductViewModel: SearchProductViewModel by viewModels { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBtn.setOnClickListener {
            searchProductByInput()
        }

        openScannerBtn.setOnClickListener {
            navigateTo(HomeFragmentDirections.toBarcodeScannerFragment())
        }

        if (BuildConfig.DEBUG) {
            searchInput.setText(R.string.exampleBarcode)
        }

        searchProductViewModel.getSearchState()
            .observe(viewLifecycleOwner, ::onSearchProductChange)
    }

    private fun searchProductByInput() {
        searchInput.getNotEmptyString()
            ?.let(searchProductViewModel::searchProductByBarcode)
    }

    private fun onSearchProductChange(searchProduct: SearchProduct?) {
        when (searchProduct) {
            is SearchProduct.InProgress -> progressBar.show()
            is SearchProduct.NotFound -> onProductNotFound()
            is SearchProduct.Success -> onSearchSuccess(searchProduct.product)
            is SearchProduct.Error -> onSearchProductError(searchProduct.throwable)
        }
    }

    private fun onSearchSuccess(product: Product) {
        progressBar.hide()
        HomeFragmentDirections
            .toSearchResultFragment(ProductParcelable(product))
            .let(::navigateTo)
    }

    private fun onProductNotFound() {
        progressBar.hide()
        showMessage(R.string.product_not_found)
    }

    private fun onSearchProductError(throwable: Throwable) {
        progressBar.hide()
        showErrorMessage(throwable)
    }

    private fun showErrorMessage(throwable: Throwable) {
        logException(throwable)
        showMessage(R.string.something_went_wrong)
    }

    private fun showMessage(messageId: Int) {
        messageId.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG) }.show()
    }

}
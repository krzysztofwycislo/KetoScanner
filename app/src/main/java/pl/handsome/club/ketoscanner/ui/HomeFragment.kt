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
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.ketoscanner.ui.parcelable.ProductParcelable
import pl.handsome.club.ketoscanner.util.*


class HomeFragment : Fragment(R.layout.home_fragment) {

    private val searchProductViewModel: SearchProductViewModel by viewModels { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBtn.setOnClickListener {
            onSearchButtonClick()
        }

        openScannerBtn.setOnClickListener {
            navigateTo(HomeFragmentDirections.toBarcodeScannerFragment())
        }

        if (BuildConfig.DEBUG) {
            searchInput.setText(R.string.exampleBarcode)
        }
    }

    private fun onSearchButtonClick() {
        val barcode = searchInput.getNotEmptyString() ?: return
        searchBtn.disable()
        searchProduct(barcode)
    }

    private fun searchProduct(barcode: String) {
        with(searchProductViewModel) {
            searchProductByBarcode(barcode)
            getProductSearchState().observe(viewLifecycleOwner, ::onSearchProductChange)
        }
    }

    private fun onSearchProductChange(productSearchState: ProductSearchState?) {
        if (productSearchState !is ProductSearchState.InProgress) {
            progressBar.hide()
            searchBtn.enable()
        }

        when (productSearchState) {
            is ProductSearchState.InProgress -> progressBar.show()
            is ProductSearchState.NotFound -> showMessage(R.string.product_not_found)
            is ProductSearchState.Success -> onSearchSuccess(productSearchState.product)
            is ProductSearchState.Error -> showErrorMessage(productSearchState.throwable)
        }
    }

    private fun onSearchSuccess(product: Product) {
        HomeFragmentDirections
            .toSearchResultFragment(ProductParcelable(product))
            .let(::navigateTo)
    }

    private fun showErrorMessage(throwable: Throwable) {
        logException(throwable)
        showMessage(R.string.something_went_wrong)
    }

    private fun showMessage(messageId: Int) {
        messageId.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG) }.show()
    }

}
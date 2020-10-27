package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.home_fragment.*
import pl.handsome.club.domain.data.Product
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.getNotEmptyString
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.product.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory
import pl.handsome.club.ketoscanner.viewmodel.product.SearchState

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
            searchInput.setText("737628064502")
        }
    }

    private fun searchProductByInput() {
        val searchState = searchProductViewModel.getSearchState()
        if(searchState.value is SearchState.SearchingInProgress) return

        searchInput.getNotEmptyString()
            ?.let(searchProductViewModel::searchProductByBarcode)

        searchState.observe(viewLifecycleOwner, ::onSearchStateChanged)
    }

    private fun onSearchStateChanged(searchState: SearchState?) {
        when (searchState) {
            is SearchState.SearchingInProgress -> showProgressBar()
            is SearchState.SearchingSuccess -> onSearchSuccess(searchState.product)
            is SearchState.SearchingError -> showErrorMessage(searchState.throwable)
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun onSearchSuccess(product: Product) {
        progressBar.visibility = View.GONE
        HomeFragmentDirections
            .toSearchResultFragment(product)
            .let(::navigateTo)
    }

    private fun showErrorMessage(throwable: Throwable) {
        progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show()
    }

}
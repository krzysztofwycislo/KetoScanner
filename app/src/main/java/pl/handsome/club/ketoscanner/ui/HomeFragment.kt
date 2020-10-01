package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.home_fragment.*
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel

class HomeFragment : Fragment(R.layout.home_fragment) {

    private val searchProductViewModel: SearchProductViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBtn.setOnClickListener {
            searchProductByInput()
        }

        openScannerBtn.setOnClickListener {
            navigateTo(HomeFragmentDirections.toBarcodeScannerFragment())
        }
    }

    private fun searchProductByInput() {
        val barcode = searchInput.text.toString()
        searchProductViewModel.searchProductByBarcode(barcode)
    }

}
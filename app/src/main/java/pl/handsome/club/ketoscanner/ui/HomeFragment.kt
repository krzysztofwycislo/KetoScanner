package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.home_fragment.*
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.navigateTo
import pl.handsome.club.ketoscanner.viewmodel.SearchProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory

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

        if(BuildConfig.DEBUG) {
            searchInput.setText("737628064502")
        }
    }

    private fun searchProductByInput() {
        val barcode = searchInput.text.toString()
        if(barcode.isEmpty()) {
            return
        }

        searchProductViewModel.searchProductByBarcode(barcode)
            .observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    val direction = HomeFragmentDirections.toSearchResultFragment(it)
                    navigateTo(direction)
                }
            })
    }

}
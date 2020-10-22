package pl.handsome.club.ketoscanner.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.search_result_fragment.*
import pl.handsome.club.domain.data.Product
import pl.handsome.club.ketoscanner.R

class SearchResultFragment : Fragment(R.layout.search_result_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments
            ?.let(SearchResultFragmentArgs::fromBundle)
            ?.let(SearchResultFragmentArgs::product)
            ?.let(::initializeView)
    }

    @SuppressLint("SetTextI18n")
    private fun initializeView(product: Product) {
        test.text = product.toString()
    }

}
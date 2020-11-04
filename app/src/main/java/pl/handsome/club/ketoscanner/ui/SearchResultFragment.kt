package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.search_result_fragment.*
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R


class SearchResultFragment : Fragment(R.layout.search_result_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments
            ?.let { SearchResultFragmentArgs.fromBundle(it) }
            ?.productParcelable
            ?.product
            ?.also(::initializeView)
    }

    private fun initializeView(product: Product) {
        productName.text = product.name
        productName.setOnClickListener {
            productName.maxLines = if(productName.maxLines == 1) 10 else 1
        }
    }

}
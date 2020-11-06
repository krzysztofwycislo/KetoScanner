package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.search_result_fragment.*
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory


class SearchResultFragment : Fragment(R.layout.search_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by viewModels { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments
            ?.let { SearchResultFragmentArgs.fromBundle(it) }
            ?.productParcelable
            ?.product
            ?.also(::initializeView)
            ?.also(::startProductAnalyze)
    }

    // TODO fix? "StaticLayout: maxLineHeight should not be -1.  maxLines:1 lineCount:1"
    private fun initializeView(product: Product) {
        with(product) {
            productName.text = name
            productName.setOnClickListener {
                productName.maxLines = if (productName.maxLines == 1) 10 else 1
            }

            imageUrl?.also(::loadImage)
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(productImage)
    }

    private fun startProductAnalyze(product: Product) {
        analyzeProductViewModel.analyzeProduct(product)
        analyzeProductViewModel.getProductAnalysisState()
            .observe(this, ::onProductAnalysisStateChanged)
    }

    private fun onProductAnalysisStateChanged(analysisState: ProductAnalysisState?) {
        if (analysisState !is ProductAnalysisState.InProgress) {
            productAnalysisProgressBar.hide()
        }

        when (analysisState) {
            is ProductAnalysisState.InProgress -> productAnalysisProgressBar.show()
            is ProductAnalysisState.Error -> showErrorMessage(analysisState.throwable)
            is ProductAnalysisState.Success -> showAnalysisResults(analysisState.result)
        }
    }

    private fun showAnalysisResults(result: ProductAnalysisResult) {

    }

    private fun showErrorMessage(throwable: Throwable) {
        logException(throwable)
        Toast.makeText(requireContext(), R.string.something_went_wrong, Toast.LENGTH_LONG).show()
    }

}
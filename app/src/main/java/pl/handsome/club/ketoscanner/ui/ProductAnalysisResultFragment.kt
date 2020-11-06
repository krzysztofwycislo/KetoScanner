package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.product_analisis_result_fragment.*
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.ViewModelFactory


class ProductAnalysisResultFragment : Fragment(R.layout.product_analisis_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by activityViewModels { ViewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analyzeProductViewModel.getProductAnalysisState().value
            .let { it as? ProductAnalysisState.Success }
            ?.result
            ?.also(::initializeView)
            ?: findNavController().navigateUp()
    }

    // TODO fix? "StaticLayout: maxLineHeight should not be -1.  maxLines:1 lineCount:1"
    private fun initializeView(result: ProductAnalysisResult) {
        with(result.product) {
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

}
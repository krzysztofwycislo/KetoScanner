package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.macronutrients_result_view.*
import kotlinx.android.synthetic.main.product_analisis_result_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.getColorIdForDietRate
import pl.handsome.club.ketoscanner.util.getDetailTextIdForDietRate
import pl.handsome.club.ketoscanner.util.getImageIdForDietRate
import pl.handsome.club.ketoscanner.util.getSummaryTextIdForDietRate
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel


class ProductAnalysisResultFragment : Fragment(R.layout.product_analisis_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analysisState = analyzeProductViewModel.getProductAnalysisState().value
        if (analysisState is ProductAnalysisState.Success) {
            initializeView(analysisState.result)
        } else {
            findNavController().navigateUp()
        }
    }

    // TODO fix? "StaticLayout: maxLineHeight should not be -1.  maxLines:1 lineCount:1"
    private fun initializeView(result: ProductAnalysisResult) {
        initializeProductInfo(result.product)

        initializeMacronutrientAnalysisResults(result.macronutrientAnalysisResult)

        result.ingredientAnalysisResult
            ?.also(::initializeIngredientAnalysisResults)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeProductInfo(product: Product) {
        productName.text = product.name
        productName.setOnClickListener {
            productName.maxLines = if (productName.maxLines == 1) 10 else 1
        }

        productBrand.text = product.brand

        product.imageUrl?.also(::loadImage)
    }

    // TODO maxPortionResultImage, maxPortionResultDetails
    private fun initializeMacronutrientAnalysisResults(macronutrientAnalysisResult: MacronutrientAnalysisResult) =
        with(macronutrientAnalysisResult) {
            getColor(requireContext(), getColorIdForDietRate(carbsRate))
                .also(carbsResultSummary::setTextColor)
                .also(maxPortionResultSummary::setTextColor)

            getImageIdForDietRate(carbsRate)
                .also(carbsResultImage::setImageResource)

            getSummaryTextIdForDietRate(carbsRate)
                .also(carbsResultSummary::setText)

            getString(
                getDetailTextIdForDietRate(carbsRate),
                maxRateCarbAmount
            ).also(carbsResultDetails::setText)
        }

    // TODO
    private fun initializeIngredientAnalysisResults(ingredientAnalysisResult: IngredientAnalysisResult) {
        /* NOTHING SO FAR */
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(productImage)
    }

}
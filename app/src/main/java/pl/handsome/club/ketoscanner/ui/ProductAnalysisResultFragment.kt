package pl.handsome.club.ketoscanner.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.macronutrients_result_view.*
import kotlinx.android.synthetic.main.nutrients_table_view.*
import kotlinx.android.synthetic.main.product_analisis_result_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.*
import pl.handsome.club.ketoscanner.viewmodel.AddToFavouritesState
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.FavouriteProductsViewModel


// TODO lets consider fragments separation
class ProductAnalysisResultFragment : Fragment(R.layout.product_analisis_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()
    private val favouriteProductsViewModel: FavouriteProductsViewModel by sharedViewModel()


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
        val product = result.product
        initializeProductInfo(result.product)

        initializeMacronutrientAnalysisResults(result.macronutrientAnalysisResult)
        initializeProductMacronutrientTable(product)

        result.ingredientAnalysisResult.also(::initializeIngredientAnalysisResults)


        favouriteProductsViewModel.getAddToFavouritesState().observe(viewLifecycleOwner, ::onAddToFavouritesStateChanged)
        addToFavouritesButton.setOnClickListener {
            favouriteProductsViewModel.addToFavourites(product)
        }

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

    private fun onAddToFavouritesStateChanged(addToFavouritesState: AddToFavouritesState?) {
        when(addToFavouritesState) {
            is AddToFavouritesState.Success -> showMessage(R.string.product_has_been_added_to_favourites)
            is AddToFavouritesState.Error -> {
                logException(addToFavouritesState.throwable)
                showMessage(R.string.something_went_wrong)
            }
            else -> {}
        }
    }

    private fun showMessage(messageId: Int) {
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_LONG).show()
    }

    // TODO maxPortionResultImage, maxPortionResultDetails
    private fun initializeMacronutrientAnalysisResults(macronutrientAnalysisResult: MacronutrientAnalysisResult) =
        with(macronutrientAnalysisResult) {
            getColor(requireContext(), getColorIdForDietRate(carbsRate))
                .also(carbsResultSummary::setTextColor)
                .also(maxServingResultSummary::setTextColor)

            getSummaryResultImageIdForDietRate(carbsRate)
                .also(carbsResultImage::setImageResource)

            getSummaryTextIdForDietRate(carbsRate)
                .also(carbsResultSummary::setText)

            getString(
                getDetailTextIdForDietRate(carbsRate),
                maxRateCarbAmount
            ).also(carbsResultDetails::setText)

            getMaxServingImageIdForDietRate(carbsRate)
                .also(maxServingResultImage::setImageResource)

            getString(
                R.string.maxServingResultDetailsText,
                maxProductPortion,
                dailyCarbConsumption
            ).also(maxServingResultDetails::setText)

        }

    // TODO refactor
    private fun initializeProductMacronutrientTable(product: Product) {
        val nutriments = product.nutriments

        perServingHeader.text = product.servingAmount
            ?.let { getString(R.string.per_serving_with_value, it) }

        energyPerServing.text = getString(R.string.value_with_kcal, nutriments.energyPerServing)
        energyPer100g.text = getString(R.string.value_with_kcal, nutriments.energyPer100g)

        fatsPerServing.text = getString(R.string.value_with_g, nutriments.fatsPerServing)
        fatsPer100g.text = getString(R.string.value_with_g, nutriments.fatsPer100g)

        proteinsPerServing.text = getString(R.string.value_with_g, nutriments.proteinsPerServing)
        proteinsPer100g.text = getString(R.string.value_with_g, nutriments.proteinsPer100g)

        carbsPerServing.text = getString(R.string.value_with_g, nutriments.carbohydratesPerServing)
        carbsPer100g.text = getString(R.string.value_with_g, nutriments.carbohydratesPer100g)

        saltPerServing.text = getString(R.string.value_with_g, nutriments.saltPerServing)
        saltPer100g.text = getString(R.string.value_with_g, nutriments.saltPer100g)
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
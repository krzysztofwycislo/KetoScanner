package pl.handsome.club.ketoscanner.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.macronutrients_result_view.*
import kotlinx.android.synthetic.main.nutrients_table_view.*
import kotlinx.android.synthetic.main.product_analisis_result_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.*
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddFavouriteProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddToFavouritesState


// TODO lets consider fragments separation
class ProductAnalysisResultFragment : Fragment(R.layout.product_analisis_result_fragment) {

    private val analyzeProductViewModel: AnalyzeProductViewModel by sharedViewModel()
    private val addFavouriteProductViewModel: AddFavouriteProductViewModel by viewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val analysisState = analyzeProductViewModel.getProductAnalysisState().value
        if (analysisState is ProductAnalysisState.Success) {
            initializeView(analysisState.result)
        } else {
            findNavController().navigateUp()
        }
    }

    private fun initializeView(result: ProductAnalysisResult) {
        val product = result.product
        initializeProductInfo(result.product)

        val macronutrientAnalysisResult = result.macronutrientAnalysisResult
        if (macronutrientAnalysisResult != null)
            showMacronutrientAnalysisResult(macronutrientAnalysisResult)
        else
            showLackOfMacronutrientAnalysisResult()

        initializeProductMacronutrientTable(product)

        result.ingredientAnalysisResult.also(::initializeIngredientAnalysisResults)

        initializeFavouriteButton(product)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeFavouriteButton(product: Product) {
        addFavouriteProductViewModel.getAddToFavouritesState()
            .observe(viewLifecycleOwner, ::onAddToFavouritesStateChanged)

        addToFavouritesButton.setOnClickListener {
            addFavouriteProductViewModel.addOrRemoveFromFavourites(product)
        }

        addFavouriteProductViewModel.isProductInFavourites(product)
        addFavouriteProductViewModel.getIsProductInFavourites()
            .observe(viewLifecycleOwner, {
                (if (it) R.drawable.favorite_white_36dp
                else R.drawable.favorite_border_white_24dp)
                    .also(addToFavouritesButton::setImageResource)
            })
    }

    private fun initializeProductInfo(product: Product) {
        productName.text = product.name
        productName.setOnClickListener {
            productName.maxLines = if (productName.maxLines == 1) 10 else 1
        }

        productBrand.text = product.brand

        product.imageUrl?.also { imageUrl ->
            loadImagePreview(imageUrl, productImage)

            productImage.setOnClickListener {
                showFullImage(imageUrl)
            }
        }
    }

    private fun showFullImage(imageUrl: String) {
        val dialog = Dialog(requireContext(), R.style.ImagePreviewDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_preview_dialog)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        val imagePreview: ImageView = dialog.findViewById(R.id.imagePreview)

        Glide.with(this)
            .load(imageUrl)
            .into(imagePreview)

        dialog.show()
    }

    private fun onAddToFavouritesStateChanged(addToFavouritesState: AddToFavouritesState) {
        when (addToFavouritesState) {
            is AddToFavouritesState.Success -> showMessage(R.string.product_has_been_added_to_favourites)
            is AddToFavouritesState.Error -> {
                logException(addToFavouritesState.throwable)
                showMessage(R.string.something_went_wrong)
            }
            is AddToFavouritesState.Removed -> showMessage(R.string.product_has_been_removed_from_favourites)
            is AddToFavouritesState.InProgress -> {
            }
        }
    }

    private fun showMessage(messageId: Int) {
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_LONG).show()
    }

    private fun showLackOfMacronutrientAnalysisResult() {
        maxServingResultContainer.visibility = View.GONE

        carbsResultImage.setImageResource(R.drawable.question_outline_negative_24dp)

        lackOfCarbsResultSummary.visibility = View.VISIBLE
    }

    private fun showMacronutrientAnalysisResult(result: MacronutrientAnalysisResult) =
        with(result) {
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

    private fun initializeProductMacronutrientTable(product: Product) {
        val nutriments = product.nutriments

        val servingAmount = product.servingAmount
        if (servingAmount != null) {
            perServingHeader.text = getString(R.string.per_serving_with_value, servingAmount)
        }

        nutriments.energyPerServing
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(energyPerServing::setText)
        nutriments.energyPer100g
            ?.let { getString(R.string.value_with_kcal, it) }
            ?.also(energyPer100g::setText)

        nutriments.fatsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(fatsPerServing::setText)
        nutriments.fatsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(fatsPer100g::setText)

        nutriments.proteinsPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(proteinsPerServing::setText)
        nutriments.proteinsPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(proteinsPer100g::setText)

        nutriments.carbohydratesPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(carbsPerServing::setText)
        nutriments.carbohydratesPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(carbsPer100g::setText)

        nutriments.saltPerServing
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(saltPerServing::setText)
        nutriments.saltPer100g
            ?.let { getString(R.string.value_with_g, it) }
            ?.also(saltPer100g::setText)
    }

    // TODO
    private fun initializeIngredientAnalysisResults(ingredientAnalysisResult: IngredientAnalysisResult) {
        /* NOTHING SO FAR */
    }

    private fun loadImagePreview(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(imageView)
    }

}
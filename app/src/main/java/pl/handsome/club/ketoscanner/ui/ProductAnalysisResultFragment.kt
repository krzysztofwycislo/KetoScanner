package pl.handsome.club.ketoscanner.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.product_analisis_result_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.product.Product
import pl.handsome.club.ketoscanner.R
import pl.handsome.club.ketoscanner.util.logException
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddFavouriteProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddToFavouritesState


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

        (childFragmentManager.findFragmentById(R.id.macronutrientsResultFragment) as MacronutrientsResultFragment)
            .initializeResults(result.macronutrientAnalysisResult)

        (childFragmentManager.findFragmentById(R.id.nutrientsTableContainer) as MacronutrientsTableFragment)
            .initializeProductMacronutrientTable(product)

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

    private fun loadImagePreview(imageUrl: String, imageView: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(imageView)
    }

}
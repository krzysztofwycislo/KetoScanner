package pl.handsome.club.feature.product.ui.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.ui.ProductResources
import pl.handsome.club.feature.product.ui.state.ProductContentState.Error
import pl.handsome.club.feature.product.ui.state.ProductContentState.Loading
import pl.handsome.club.feature.product.ui.state.ProductContentState.ShowProduct
import pl.handsome.club.feature.product.ui.state.ProductStatePrototype as Prototype

internal class ProductStateFactory(private val resources: ProductResources) {

    fun createFor(prototype: Prototype): ProductState = ProductState(
        content = prototype.content(),
    )

    private fun Prototype.content(): ProductContentState {
        val analysis = analysis
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.error(loadError))
            analysis == null -> Loading
            else -> ShowProduct(
                header = header(analysis.product),
                favourite = favourite(),
                verdict = analysis.verdict(),
                nutrients = analysis.product.nutrientTable(),
                imagePreview = imagePreview(analysis.product),
            )
        }
    }

    private fun Prototype.header(product: Product): ProductHeaderState = ProductHeaderState(
        imageUrl = product.imageUrl,
        imageDescription = resources.imageDescription(),
        isImageClickable = product.imageUrl != null,
        brand = resources.brand(product.brand),
        name = resources.productName(product.name),
        nameMaxLines = if (isNameExpanded) EXPANDED_NAME_LINES else COLLAPSED_NAME_LINES,
    )

    private fun Prototype.favourite(): FavouriteButtonState? {
        val isFavourite = isFavourite ?: return null
        return FavouriteButtonState(
            isFavourite = isFavourite,
            description = resources.favouriteDescription(isFavourite),
            isEnabled = !isFavouriteUpdating,
        )
    }

    private fun Prototype.imagePreview(product: Product): ImagePreviewState? {
        val imageUrl = product.imageUrl
        return when {
            !isImagePreviewOpen -> null
            imageUrl == null -> null
            else -> ImagePreviewState(imageUrl, resources.imagePreviewDescription())
        }
    }

    private fun ProductAnalysis.verdict(): VerdictState = when (this) {
        is ProductAnalysis.Rated -> with(macronutrients) {
            VerdictState.Rated(
                rating = rate.toRating(),
                summary = resources.ratingSummary(rate, dietMode),
                reasons = reasons.take(MAX_REASONS).map(resources::reason).toPersistentList()
                    .ifEmpty { persistentListOf(resources.carbsPer100g(carbsPer100g)) },
                portion = resources.portion(maxProductPortion, dailyCarbConsumption),
            )
        }

        is ProductAnalysis.InsufficientData -> VerdictState.InsufficientData(resources.insufficientData())
    }

    private fun Product.nutrientTable(): NutrientTableState = with(nutriments) {
        NutrientTableState(
            perServingHeader = resources.perServingHeader(servingAmount),
            rows = persistentListOf(
                NutrientRowState(resources.energy(), resources.kcal(energyPerServing), resources.kcal(energyPer100g)),
                NutrientRowState(resources.fats(), resources.grams(fatsPerServing), resources.grams(fatsPer100g)),
                NutrientRowState(
                    resources.proteins(),
                    resources.grams(proteinsPerServing),
                    resources.grams(proteinsPer100g),
                ),
                NutrientRowState(
                    resources.carbs(),
                    resources.grams(carbohydratesPerServing),
                    resources.grams(carbohydratesPer100g),
                ),
                NutrientRowState(resources.sugars(), resources.grams(sugarsPerServing), resources.grams(sugarsPer100g)),
                NutrientRowState(resources.fibre(), resources.grams(fibrePerServing), resources.grams(fibrePer100g)),
                NutrientRowState(resources.salt(), resources.grams(saltPerServing), resources.grams(saltPer100g)),
            ),
        )
    }

    private fun DietRate.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
    }

    private companion object {
        const val COLLAPSED_NAME_LINES = 1
        const val EXPANDED_NAME_LINES = 10
        const val MAX_REASONS = 2
    }
}

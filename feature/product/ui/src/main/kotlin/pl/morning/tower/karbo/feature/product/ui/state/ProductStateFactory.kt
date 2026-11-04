package pl.morning.tower.karbo.feature.product.ui.state

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.icon
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.ui.ProductResources
import pl.morning.tower.karbo.feature.product.ui.state.ProductContentState.Error
import pl.morning.tower.karbo.feature.product.ui.state.ProductContentState.Loading
import pl.morning.tower.karbo.feature.product.ui.state.ProductContentState.ShowProduct
import pl.morning.tower.karbo.feature.product.ui.state.ProductStatePrototype as Prototype

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
                glance = glance(analysis),
                imagePreview = imagePreview(analysis.product),
                dataSource = resources.dataSource(analysis.product.barcode),
            )
        }
    }

    private fun Prototype.header(product: Product): ProductHeaderState = ProductHeaderState(
        imageUrl = product.imageUrl,
        imageDescription = resources.imageDescription(),
        isImageClickable = product.imageUrl != null,
        category = resources.categoryLabel(product.category)?.let { CategoryChipState(product.category.icon, it) },
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
            icon = if (isFavourite) AppIcons.HeartFilled else AppIcons.Heart,
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

        is ProductAnalysis.InsufficientData -> VerdictState.InsufficientData(
            summary = resources.insufficientData(),
            detail = resources.insufficientDataDetail(),
            advice = resources.insufficientDataAdvice(),
        )
    }

    private fun Prototype.glance(analysis: ProductAnalysis): GlanceState = with(analysis.product.nutriments) {
        val perServing = isPerServingSelected
        val carbsHighlight = when (analysis) {
            is ProductAnalysis.Rated -> analysis.macronutrients.rate.toRating()
            is ProductAnalysis.InsufficientData -> Rating.Unknown
        }
        GlanceState(
            isPerServingSelected = perServing,
            servingLabel = resources.servingLabel(analysis.product.servingAmount),
            tiles = persistentListOf(
                GlanceTileState(
                    resources.carbs(),
                    resources.grams(if (perServing) carbohydratesPerServing else carbohydratesPer100g),
                    carbsHighlight,
                ),
                GlanceTileState(
                    resources.sugars(),
                    resources.grams(if (perServing) sugarsPerServing else sugarsPer100g),
                    null,
                ),
                GlanceTileState(
                    resources.fibre(),
                    resources.grams(if (perServing) fibrePerServing else fibrePer100g),
                    null,
                ),
                GlanceTileState(
                    resources.proteins(),
                    resources.grams(if (perServing) proteinsPerServing else proteinsPer100g),
                    null,
                ),
                GlanceTileState(
                    resources.fats(),
                    resources.grams(if (perServing) fatsPerServing else fatsPer100g),
                    null,
                ),
                GlanceTileState(
                    resources.energy(),
                    resources.kcal(if (perServing) energyPerServing else energyPer100g),
                    null,
                ),
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

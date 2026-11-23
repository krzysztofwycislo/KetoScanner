package app.carbpeek.feature.product.ui.state

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.usableServingGrams
import app.carbpeek.feature.product.ui.ProductResources
import app.carbpeek.feature.product.ui.state.ProductContentState.Error
import app.carbpeek.feature.product.ui.state.ProductContentState.Loading
import app.carbpeek.feature.product.ui.state.ProductContentState.ShowProduct
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.product.ui.state.ProductStatePrototype as Prototype

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
                macros = macros(analysis),
                scores = scores(analysis.product),
                ingredients = analysis.ingredients?.let { ingredientsCard(it) },
                imagePreview = imagePreview(analysis.product),
                ingredientSheet = analysis.ingredients?.let { openIngredientSheet(it) },
                ingredientsInfoSheet = analysis.ingredients?.let { ingredientsInfoSheet() },
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
            )
        }

        is ProductAnalysis.InsufficientData -> VerdictState.InsufficientData(
            summary = resources.insufficientData(),
            detail = resources.insufficientDataDetail(),
            advice = resources.insufficientDataAdvice(),
        )
    }

    private fun Prototype.macros(analysis: ProductAnalysis): MacrosState = with(analysis.product.nutriments) {
        val servingGrams = analysis.product.usableServingGrams
        val isServingToggleVisible = servingGrams != null && servingGrams != BASIS_GRAMS
        val perServing = isServingToggleVisible && isPerServingSelected
        val carbsHighlight = when (analysis) {
            is ProductAnalysis.Rated -> analysis.macronutrients.rate.toRating()
            is ProductAnalysis.InsufficientData -> Rating.Unknown
        }
        MacrosState(
            isServingToggleVisible = isServingToggleVisible,
            isPerServingSelected = perServing,
            servingLabel = resources.servingLabel(analysis.product.servingAmount),
            values = persistentListOf(
                MacroValueState(
                    label = resources.carbs(),
                    value = resources.grams(if (perServing) carbohydratesPerServing else carbohydratesPer100g),
                    highlight = carbsHighlight,
                ),
                MacroValueState(
                    label = resources.sugars(),
                    value = resources.grams(if (perServing) sugarsPerServing else sugarsPer100g),
                    highlight = null,
                ),
                MacroValueState(
                    label = resources.fibre(),
                    value = resources.grams(if (perServing) fibrePerServing else fibrePer100g),
                    highlight = null,
                ),
                MacroValueState(
                    label = resources.proteins(),
                    value = resources.grams(if (perServing) proteinsPerServing else proteinsPer100g),
                    highlight = null,
                ),
                MacroValueState(
                    label = resources.fats(),
                    value = resources.grams(if (perServing) fatsPerServing else fatsPer100g),
                    highlight = null,
                ),
                MacroValueState(
                    label = resources.energy(),
                    value = resources.kcal(if (perServing) energyPerServing else energyPer100g),
                    highlight = null,
                ),
            ),
        )
    }

    private fun scores(product: Product): ScoresState? {
        val nutriScore = product.nutriScore
        val novaGroup = product.novaGroup
        if (nutriScore == null && novaGroup == null) {
            return null
        }
        return ScoresState(
            nutriScore = nutriScore?.let { nutriScore(it) },
            processing = novaGroup?.let { processing(it) },
        )
    }

    private fun nutriScore(grade: NutriScoreGrade): ScoreState = ScoreState(
        badge = resources.nutriScoreBadge(grade),
        title = resources.nutriScoreTitle(),
        note = resources.nutriScoreNote(grade),
        tone = when (grade) {
            NutriScoreGrade.A -> ScoreTone.NUTRI_SCORE_A
            NutriScoreGrade.B -> ScoreTone.NUTRI_SCORE_B
            NutriScoreGrade.C -> ScoreTone.NUTRI_SCORE_C
            NutriScoreGrade.D -> ScoreTone.NUTRI_SCORE_D
            NutriScoreGrade.E -> ScoreTone.NUTRI_SCORE_E
        },
    )

    private fun processing(group: NovaGroup): ScoreState = ScoreState(
        badge = resources.processingBadge(group),
        title = resources.processingTitle(),
        note = resources.processingNote(group),
        tone = ScoreTone.PROCESSING,
    )

    private fun ingredientsCard(analysis: IngredientsAnalysis): IngredientsCardState = IngredientsCardState(
        chips = ingredientChips(analysis.ingredients),
    )

    private fun ingredientChips(ingredients: List<AnalyzedIngredient>): PersistentList<IngredientChipState> =
        ingredients.map { ingredient ->
            IngredientChipState(
                label = resources.ingredientChipLabel(ingredient),
                count = resources.ingredientContentsCount(ingredient),
                description = resources.ingredientChipDescription(ingredient),
                tone = ingredient.kind.toIngredientTone(),
            )
        }.toPersistentList()

    private fun Prototype.openIngredientSheet(analysis: IngredientsAnalysis): IngredientSheetState? {
        val ingredient = openIngredientIndex?.let { analysis.ingredients.getOrNull(it) } ?: return null
        val content = openIngredientContentIndex?.let { ingredient.contains.getOrNull(it) }
        return if (content == null) {
            ingredientSheet(ingredient = ingredient, back = null)
        } else {
            ingredientSheet(ingredient = content, back = ingredientBack(ingredient))
        }
    }

    private fun ingredientSheet(ingredient: AnalyzedIngredient, back: IngredientBackState?): IngredientSheetState {
        val contents = ingredientChips(ingredient.contains)
        val sections = ingredientSections(ingredient.kind)
        return IngredientSheetState(
            back = back,
            tag = resources.ingredientTag(ingredient.kind),
            tone = ingredient.kind.toIngredientTone(),
            title = resources.ingredientName(ingredient.name),
            labelName = resources.ingredientLabelName(ingredient.name),
            contents = contents,
            sections = sections,
            noDescription = if (sections.isEmpty() && contents.isEmpty()) {
                resources.ingredientNoDescription()
            } else {
                null
            },
        )
    }

    private fun ingredientBack(compound: AnalyzedIngredient): IngredientBackState = IngredientBackState(
        label = resources.ingredientName(compound.name),
        description = resources.ingredientBackDescription(compound.name),
    )

    private fun Prototype.ingredientsInfoSheet(): IngredientsInfoSheetState? {
        if (!isIngredientsInfoOpen) {
            return null
        }
        return IngredientsInfoSheetState(
            legend = legendKinds.mapNotNull { kind ->
                resources.ingredientTag(kind)?.let { label ->
                    IngredientLegendState(tone = kind.toIngredientTone(), label = label)
                }
            }.toPersistentList(),
        )
    }

    private fun ingredientSections(kind: IngredientKind): PersistentList<IngredientSectionState> = listOfNotNull(
        resources.ingredientWhat(kind)?.let { text ->
            IngredientSectionState(heading = resources.ingredientWhatHeading(), text = text)
        },
        resources.ingredientDoes(kind)?.let { text ->
            IngredientSectionState(heading = resources.ingredientDoesHeading(), text = text)
        },
    ).toPersistentList()

    private fun IngredientKind.toIngredientTone(): IngredientTone = when (this) {
        IngredientKind.ADDED_SUGAR -> IngredientTone.ADDED_SUGAR
        IngredientKind.POLYOL,
        IngredientKind.SWEETENER,
        -> IngredientTone.SWEETENER
        IngredientKind.STARCH -> IngredientTone.STARCH
        IngredientKind.MILK -> IngredientTone.MILK
        IngredientKind.ADDITIVE -> IngredientTone.ADDITIVE
        IngredientKind.PLAIN -> IngredientTone.PLAIN
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
        const val BASIS_GRAMS = 100.0
        val legendKinds = listOf(
            IngredientKind.ADDED_SUGAR,
            IngredientKind.SWEETENER,
            IngredientKind.STARCH,
            IngredientKind.MILK,
            IngredientKind.ADDITIVE,
        )
    }
}

package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientName
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.product.api.model.Ingredient

internal object IngredientsAnalyzer {

    private const val MIN_CONTENTS = 2
    private val fatTokens = setOf("oil", "oils", "fat", "fats")
    private val contentIds = setOf("en:salt", "en:water")
    private val contentKinds = setOf(
        IngredientKind.ADDED_SUGAR,
        IngredientKind.POLYOL,
        IngredientKind.SWEETENER,
        IngredientKind.ADDITIVE,
    )

    fun analyze(ingredients: List<Ingredient>?): IngredientsAnalysis? =
        ingredients
            ?.withoutLabelNotes()
            ?.takeIf { it.isNotEmpty() }
            ?.let { realIngredients -> IngredientsAnalysis(ingredients = realIngredients.map(::analyzeListed)) }

    private fun analyzeListed(ingredient: Ingredient): AnalyzedIngredient = analyzeIngredient(
        ingredient = ingredient,
        contains = ingredient.subIngredients
            .takeIf { ingredient.isCompound() }
            .orEmpty()
            .map { content -> analyzeIngredient(ingredient = content, contains = emptyList()) },
    )

    private fun analyzeIngredient(
        ingredient: Ingredient,
        contains: List<AnalyzedIngredient>,
    ): AnalyzedIngredient = AnalyzedIngredient(
        id = ingredient.id.lowercase(),
        name = ingredient.toName(),
        declaredPercent = ingredient.declaredPercent,
        kind = ingredientKindOf(ingredient),
        contains = contains,
    )

    private fun Ingredient.isCompound(): Boolean =
        subIngredients.size >= MIN_CONTENTS &&
            id.lowercase().split(':', '-').none { it in fatTokens } &&
            subIngredients.any { it.showsRealContents() }

    private fun Ingredient.showsRealContents(): Boolean =
        id.lowercase() in contentIds || ingredientKindOf(this) in contentKinds || vitaminOf(this) != null

    private fun Ingredient.toName(): IngredientName = IngredientName(
        name = name,
        labelName = labelName,
        eNumber = eNumberOf(this),
        vitamin = vitaminOf(this),
    )
}

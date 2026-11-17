package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.IngredientCounts
import app.carbpeek.feature.analysis.api.model.IngredientFinding
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.product.api.model.Ingredient

internal object IngredientsAnalyzer {

    private const val MAJOR_PERCENT = 10.0
    private const val MAJOR_POSITIONS = 3

    private val carbSources = setOf(IngredientKind.ADDED_SUGAR, IngredientKind.POLYOL, IngredientKind.STARCH)

    fun analyze(ingredients: List<Ingredient>?): IngredientsAnalysis? {
        if (ingredients.isNullOrEmpty()) {
            return null
        }
        val analyzed = ingredients.mapIndexed { index, ingredient ->
            analyzeIngredient(ingredient = ingredient, position = index + 1)
        }

        return IngredientsAnalysis(
            finding = findingOf(analyzed),
            ingredients = analyzed,
            counts = countsOf(analyzed),
        )
    }

    private fun analyzeIngredient(ingredient: Ingredient, position: Int): AnalyzedIngredient {
        val ownKind = ingredientKindOf(ingredient.id)
        val nested = strongestNested(ingredient.subIngredients).takeIf { ownKind == IngredientKind.PLAIN }
        val isMajor = ingredient.declaredPercent
            ?.let { it >= MAJOR_PERCENT }
            ?: (position <= MAJOR_POSITIONS)

        return AnalyzedIngredient(
            name = ingredient.name,
            declaredPercent = ingredient.declaredPercent,
            position = position,
            kind = nested?.second ?: ownKind,
            foundIn = nested?.first?.name,
            isMajor = isMajor,
        )
    }

    private fun strongestNested(ingredients: List<Ingredient>): Pair<Ingredient, IngredientKind>? =
        ingredients
            .flatMap { it.withNested() }
            .map { it to ingredientKindOf(it.id) }
            .filter { (_, kind) -> kind != IngredientKind.PLAIN }
            .minByOrNull { (_, kind) -> kind.ordinal }

    private fun Ingredient.withNested(): List<Ingredient> = listOf(this) + subIngredients.flatMap { it.withNested() }

    private fun findingOf(analyzed: List<AnalyzedIngredient>): IngredientFinding {
        val carbSources = analyzed.filter { it.kind in carbSources }
        val carbSource = carbSources.firstOrNull { it.isMajor } ?: carbSources.firstOrNull()
        if (carbSource != null) {
            return IngredientFinding.CarbSource(carbSource)
        }
        val sweetener = analyzed.firstOrNull { it.kind == IngredientKind.SWEETENER }
        if (sweetener != null) {
            return IngredientFinding.NoCarbSweetener(sweetener)
        }
        return IngredientFinding.NothingAdded
    }

    private fun countsOf(analyzed: List<AnalyzedIngredient>): IngredientCounts =
        IngredientCounts(
            addedSugars = analyzed.count { it.kind == IngredientKind.ADDED_SUGAR },
            sweeteners = analyzed.count { it.kind == IngredientKind.POLYOL || it.kind == IngredientKind.SWEETENER },
            starches = analyzed.count { it.kind == IngredientKind.STARCH },
            additives = analyzed.count { it.kind == IngredientKind.ADDITIVE },
        )
}

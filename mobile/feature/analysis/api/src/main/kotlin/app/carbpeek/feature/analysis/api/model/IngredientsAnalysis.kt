package app.carbpeek.feature.analysis.api.model

enum class IngredientKind {
    ADDED_SUGAR,
    POLYOL,
    STARCH,
    SWEETENER,
    MILK,
    ADDITIVE,
    PLAIN,
}

enum class Vitamin {
    RIBOFLAVIN,
    NIACIN,
}

data class IngredientName(
    val name: String,
    val labelName: String,
    val eNumber: String?,
    val vitamin: Vitamin?,
)

data class AnalyzedIngredient(
    val name: IngredientName,
    val declaredPercent: Double?,
    val kind: IngredientKind,
    val contains: List<AnalyzedIngredient>,
)

data class IngredientsAnalysis(
    val ingredients: List<AnalyzedIngredient>,
)

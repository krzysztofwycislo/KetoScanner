package app.carbpeek.feature.analysis.api.model

enum class IngredientKind {
    ADDED_SUGAR,
    POLYOL,
    STARCH,
    SWEETENER,
    LACTOSE,
    ADDITIVE,
    PLAIN,
}

data class AnalyzedIngredient(
    val name: String,
    val declaredPercent: Double?,
    val position: Int,
    val kind: IngredientKind,
    val foundIn: String?,
    val isMajor: Boolean,
)

data class IngredientCounts(
    val addedSugars: Int,
    val sweeteners: Int,
    val starches: Int,
    val additives: Int,
)

sealed interface IngredientFinding {

    data object NothingAdded : IngredientFinding

    data class NoCarbSweetener(val ingredient: AnalyzedIngredient) : IngredientFinding

    data class CarbSource(val ingredient: AnalyzedIngredient) : IngredientFinding
}

data class IngredientsAnalysis(
    val finding: IngredientFinding,
    val ingredients: List<AnalyzedIngredient>,
    val counts: IngredientCounts,
)

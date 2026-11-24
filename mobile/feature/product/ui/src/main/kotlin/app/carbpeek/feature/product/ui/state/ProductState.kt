package app.carbpeek.feature.product.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.text.UiText
import kotlinx.collections.immutable.PersistentList

@Immutable
internal data class ProductState(
    val content: ProductContentState,
)

@Immutable
internal sealed interface ProductContentState {
    data object Loading : ProductContentState
    data class Error(val message: UiText) : ProductContentState
    data class ShowProduct(
        val header: ProductHeaderState,
        val favourite: FavouriteButtonState?,
        val verdict: VerdictState?,
        val macros: MacrosState,
        val scores: ScoresState?,
        val ingredients: IngredientsCardState?,
        val imagePreview: ImagePreviewState?,
        val ingredientSheet: IngredientSheetState?,
        val ingredientsInfoSheet: IngredientsInfoSheetState?,
        val dataSource: UiText,
    ) : ProductContentState
}

@Immutable
internal data class ProductHeaderState(
    val imageUrl: String?,
    val imageDescription: UiText,
    val isImageClickable: Boolean,
    val placeholderIcon: AppIcon,
    val category: CategoryChipState?,
    val brand: UiText?,
    val name: UiText,
    val nameMaxLines: Int,
) {
    companion object
}

@Immutable
internal data class CategoryChipState(
    val icon: AppIcon,
    val label: UiText,
)

@Immutable
internal data class FavouriteButtonState(
    val isFavourite: Boolean,
    val description: UiText,
    val isEnabled: Boolean,
    val icon: AppIcon,
) {
    companion object
}

@Immutable
internal sealed interface VerdictState {

    data class Rated(
        val rating: Rating,
        val summary: UiText,
        val reasons: PersistentList<UiText>,
    ) : VerdictState

    data class InsufficientData(
        val summary: UiText,
        val detail: UiText,
        val advice: UiText,
    ) : VerdictState

    companion object
}

@Immutable
internal data class MacrosState(
    val isServingToggleVisible: Boolean,
    val isPerServingSelected: Boolean,
    val servingLabel: UiText,
    val values: PersistentList<MacroValueState>,
) {
    companion object
}

@Immutable
internal data class MacroValueState(
    val label: UiText,
    val value: UiText,
    val highlight: Rating?,
) {
    companion object
}

@Immutable
internal data class ScoresState(
    val title: UiText,
    val note: UiText?,
    val nutriScore: ScoreState?,
    val processing: ScoreState?,
) {
    companion object
}

@Immutable
internal data class ScoreState(
    val badge: UiText,
    val title: UiText,
    val note: UiText,
    val tone: ScoreTone,
) {
    companion object
}

internal enum class ScoreTone {
    NUTRI_SCORE_A,
    NUTRI_SCORE_B,
    NUTRI_SCORE_C,
    NUTRI_SCORE_D,
    NUTRI_SCORE_E,
    PROCESSING,
}

@Immutable
internal data class ImagePreviewState(
    val url: String,
    val description: UiText,
) {
    companion object
}

@Immutable
internal data class IngredientsCardState(
    val chips: PersistentList<IngredientChipState>,
) {
    companion object
}

@Immutable
internal data class IngredientChipState(
    val label: UiText,
    val count: UiText?,
    val description: UiText,
    val tone: IngredientTone,
) {
    companion object
}

@Immutable
internal data class IngredientSheetState(
    val back: IngredientBackState?,
    val tag: UiText?,
    val tone: IngredientTone,
    val title: UiText,
    val labelName: UiText?,
    val contents: PersistentList<IngredientChipState>,
    val sections: PersistentList<IngredientSectionState>,
    val noDescription: UiText?,
) {
    companion object
}

@Immutable
internal data class IngredientBackState(
    val label: UiText,
    val description: UiText,
)

@Immutable
internal data class IngredientSectionState(
    val heading: UiText,
    val text: UiText,
)

@Immutable
internal data class IngredientsInfoSheetState(
    val legend: PersistentList<IngredientLegendState>,
) {
    companion object
}

@Immutable
internal data class IngredientLegendState(
    val tone: IngredientTone,
    val label: UiText,
)

internal enum class IngredientTone {
    ADDED_SUGAR,
    SWEETENER,
    STARCH,
    MILK,
    ADDITIVE,
    PLAIN,
}

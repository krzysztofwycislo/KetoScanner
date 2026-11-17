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
        val verdict: VerdictState,
        val glance: GlanceState,
        val scores: ScoresState?,
        val imagePreview: ImagePreviewState?,
        val dataSource: UiText,
    ) : ProductContentState
}

@Immutable
internal data class ProductHeaderState(
    val imageUrl: String?,
    val imageDescription: UiText,
    val isImageClickable: Boolean,
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
internal data class GlanceState(
    val isServingToggleVisible: Boolean,
    val isPerServingSelected: Boolean,
    val servingLabel: UiText,
    val tiles: PersistentList<GlanceTileState>,
) {
    companion object
}

@Immutable
internal data class GlanceTileState(
    val label: UiText,
    val value: UiText,
    val highlight: Rating?,
) {
    companion object
}

@Immutable
internal data class ScoresState(
    val nutriScore: ScoreTileState?,
    val processing: ScoreTileState?,
) {
    companion object
}

@Immutable
internal data class ScoreTileState(
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

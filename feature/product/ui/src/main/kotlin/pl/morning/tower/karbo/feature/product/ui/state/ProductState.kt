package pl.morning.tower.karbo.feature.product.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText

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
internal data class ImagePreviewState(
    val url: String,
    val description: UiText,
) {
    companion object
}

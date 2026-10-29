package pl.handsome.club.feature.product.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis

@Immutable
internal data class ProductStatePrototype(
    val barcode: String = "",
    val analysis: ProductAnalysis? = null,
    val loadError: AppError? = null,
    val isFavourite: Boolean? = null,
    val isFavouriteUpdating: Boolean = false,
    val isImagePreviewOpen: Boolean = false,
    val isNameExpanded: Boolean = false,
) {

    companion object {
        val Default = ProductStatePrototype()
    }
}

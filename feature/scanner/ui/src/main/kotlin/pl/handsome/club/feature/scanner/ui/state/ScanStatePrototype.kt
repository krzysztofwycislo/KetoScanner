package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis

@Immutable
internal data class ScanStatePrototype(
    val query: String = "",
    val cameraPermission: CameraPermission = CameraPermission.Unchecked,
    val isScanPaused: Boolean = false,
    val isSearching: Boolean = false,
    val hasTorch: Boolean = false,
    val isTorchOn: Boolean = false,
    val isTypeSheetOpen: Boolean = false,
    val foundBarcode: String? = null,
    val foundAnalysis: ProductAnalysis? = null,
) {

    val isScanBlocked: Boolean get() = isScanPaused || isSearching || isTypeSheetOpen || foundBarcode != null

    companion object {
        val Default = ScanStatePrototype()
    }
}

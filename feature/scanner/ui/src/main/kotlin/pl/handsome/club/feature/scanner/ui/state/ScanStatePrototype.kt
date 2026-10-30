package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class ScanStatePrototype(
    val query: String = "",
    val cameraPermission: CameraPermission = CameraPermission.Unchecked,
    val isScanPaused: Boolean = false,
    val isSearching: Boolean = false,
    val hasTorch: Boolean = false,
    val isTorchOn: Boolean = false,
    val isTypeSheetOpen: Boolean = false,
) {

    val isScanBlocked: Boolean get() = isScanPaused || isSearching || isTypeSheetOpen

    companion object {
        val Default = ScanStatePrototype()
    }
}

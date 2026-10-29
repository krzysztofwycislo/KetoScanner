package pl.handsome.club.feature.scanner.ui.event

import pl.handsome.club.feature.scanner.ui.state.CameraPermissionCheck

internal sealed interface ScanAction {
    data class OnStart(val check: CameraPermissionCheck) : ScanAction
    data class OnCameraPermissionResult(val check: CameraPermissionCheck) : ScanAction
    data object OnPermissionButtonClicked : ScanAction
    data class OnQueryChanged(val query: String) : ScanAction
    data object OnSearchSubmitted : ScanAction
    data class OnBarcodeScanned(val barcode: String) : ScanAction
}

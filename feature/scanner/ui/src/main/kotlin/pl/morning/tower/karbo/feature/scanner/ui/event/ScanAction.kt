package pl.morning.tower.karbo.feature.scanner.ui.event

import pl.morning.tower.karbo.feature.scanner.ui.state.CameraPermissionCheck

internal sealed interface ScanAction {
    data class OnStart(val check: CameraPermissionCheck, val hasTorch: Boolean) : ScanAction
    data class OnCameraPermissionResult(val check: CameraPermissionCheck) : ScanAction
    data object OnPermissionButtonClicked : ScanAction
    data class OnQueryChanged(val query: String) : ScanAction
    data object OnSearchSubmitted : ScanAction
    data class OnBarcodeScanned(val barcode: String) : ScanAction
    data object OnTorchClicked : ScanAction
    data object OnTypeItClicked : ScanAction
    data object OnTypeSheetDismissed : ScanAction
    data object OnScanNextClicked : ScanAction
    data object OnDetailsClicked : ScanAction
    data class OnTripItemClicked(val barcode: String) : ScanAction
}

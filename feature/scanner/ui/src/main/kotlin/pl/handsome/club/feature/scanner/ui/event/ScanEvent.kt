package pl.handsome.club.feature.scanner.ui.event

sealed interface ScanEvent {

    sealed interface ScanNavigation : ScanEvent {
        data class NavigateToProduct(val barcode: String) : ScanNavigation
    }
}

internal data object RequestCameraPermission : ScanEvent

internal data object OpenAppSettings : ScanEvent

internal data class PerformHapticFeedback(val feedback: ScanFeedback) : ScanEvent

internal enum class ScanFeedback { Detected, Found, NotFound }

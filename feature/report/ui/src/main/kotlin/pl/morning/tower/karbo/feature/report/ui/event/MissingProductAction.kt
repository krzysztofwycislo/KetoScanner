package pl.morning.tower.karbo.feature.report.ui.event

import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction
import pl.morning.tower.karbo.feature.report.ui.state.PhotoSlot

internal sealed interface MissingProductAction {
    data object OnCloseClicked : MissingProductAction
    data class OnSlotClicked(val slot: PhotoSlot) : MissingProductAction
    data object OnSendClicked : MissingProductAction
    data object OnTryAgainClicked : MissingProductAction
    data object OnBackToReportClicked : MissingProductAction
    data object OnHomeClicked : MissingProductAction
    data object OnScanNextClicked : MissingProductAction
    data class OnCapture(val action: PhotoCaptureAction) : MissingProductAction
}

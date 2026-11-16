package app.carbpeek.feature.report.ui.event

import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.state.PhotoSlot

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

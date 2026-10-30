package pl.handsome.club.feature.scanner.ui.state

import pl.handsome.club.feature.scanner.ui.ScanResources
import pl.handsome.club.feature.scanner.ui.state.ScanStatePrototype as Prototype

internal class ScanStateFactory(private val resources: ScanResources) {

    fun createFor(prototype: Prototype): ScanState = ScanState(
        query = prototype.query,
        showProgress = prototype.isSearching,
        camera = prototype.camera(),
    )

    private fun Prototype.camera(): CameraState = when (cameraPermission) {
        CameraPermission.Granted -> CameraState.Preview(
            isPaused = isScanBlocked,
            torch = torch(),
            isTypeSheetOpen = isTypeSheetOpen,
        )

        CameraPermission.Rationale -> CameraState.PermissionPanel(
            title = resources.rationaleTitle(),
            message = resources.permissionRationale(),
            buttonLabel = resources.allowCamera(),
        )

        CameraPermission.Denied -> CameraState.PermissionPanel(
            title = resources.deniedTitle(),
            message = resources.permissionDenied(),
            buttonLabel = resources.openSettings(),
        )

        CameraPermission.Unchecked, CameraPermission.Requesting -> CameraState.Hidden
    }

    private fun Prototype.torch(): Torch = when {
        !hasTorch -> Torch.Unavailable
        isTorchOn -> Torch.On
        else -> Torch.Off
    }
}

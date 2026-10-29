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
        CameraPermission.Granted -> CameraState.Preview(isPaused = isScanPaused || isSearching)
        CameraPermission.Rationale -> CameraState.PermissionPanel(
            message = resources.permissionRationale(),
            buttonLabel = resources.allowCamera(),
        )

        CameraPermission.Denied -> CameraState.PermissionPanel(
            message = resources.permissionDenied(),
            buttonLabel = resources.openSettings(),
        )

        CameraPermission.Unchecked, CameraPermission.Requesting -> CameraState.Hidden
    }
}

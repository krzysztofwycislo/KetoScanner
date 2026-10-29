package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable

@Immutable
internal data class ScanStatePrototype(
    val query: String = "",
    val cameraPermission: CameraPermission = CameraPermission.Unchecked,
    val isScanPaused: Boolean = false,
    val isSearching: Boolean = false,
) {

    companion object {
        val Default = ScanStatePrototype()
    }
}

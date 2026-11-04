package pl.handsome.club.feature.scanner.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import pl.handsome.club.barcodescanner.BarcodeCamera

@SuppressLint("MissingPermission")
@Composable
internal fun CameraPreview(
    isPaused: Boolean,
    torchEnabled: Boolean,
    scanArea: Rect?,
    onBarcodeScanned: (String, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    BarcodeCamera(
        isPaused = isPaused,
        torchEnabled = torchEnabled,
        scanArea = scanArea,
        onBarcodeScanned = onBarcodeScanned,
        modifier = modifier,
    )
}

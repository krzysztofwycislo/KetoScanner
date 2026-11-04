package pl.morning.tower.karbo.feature.scanner.ui.camera

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import pl.morning.tower.karbo.barcodescanner.BarcodeCamera

@SuppressLint("MissingPermission")
@Composable
internal fun CameraPreview(
    isPaused: Boolean,
    torchEnabled: Boolean,
    scanArea: Rect?,
    onBarcodeScanned: (String) -> Unit,
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
